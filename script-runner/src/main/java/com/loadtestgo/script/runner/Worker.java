package com.loadtestgo.script.runner;

import com.loadtestgo.script.api.Browser;
import com.loadtestgo.script.api.Data;
import com.loadtestgo.script.api.TestResult;
import com.loadtestgo.script.engine.*;
import com.loadtestgo.script.engine.internal.api.ChromeBrowser;
import com.loadtestgo.script.engine.internal.browsers.chrome.ChromeProcess;
import com.loadtestgo.script.har.HarWriter;
import com.loadtestgo.script.runner.config.TestConfig;
import com.loadtestgo.util.FileUtils;
import com.loadtestgo.util.Path;
import com.loadtestgo.util.Settings;
import com.loadtestgo.util.StringUtils;
import org.pmw.tinylog.Configurator;
import org.pmw.tinylog.Level;
import org.pmw.tinylog.Logger;
import org.pmw.tinylog.writers.FileWriter;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.nio.file.Files;

public class Worker {
    private File outputDir;
    private RunnerTestResults runnerTestResults;
    private RunnerSettings runnerSettings;
    private File chromeExecutable;

    public Worker(Settings settings) {
        runnerSettings = new RunnerSettings(settings);
    }

    public void init(String outputDir, RunnerTestResults runnerTestResults, File chromeExecutable) {
        this.runnerTestResults = runnerTestResults;
        this.chromeExecutable = chromeExecutable;

        this.outputDir = new File(outputDir);
        this.outputDir.mkdirs();

        Configurator.defaultConfig()
            .writer(new FileWriter(new File(this.outputDir, "log.txt").getAbsolutePath()))
            .level(Level.INFO)
            .activate();
    }

    public boolean runJobs(TestConfig testConfig) {
        EngineContext engineContext = new EngineContext();
        try {
            engineContext.setLocalPublicIp(Inet4Address.getLocalHost().getHostAddress());
        } catch (UnknownHostException e) {
            Logger.error(e);
        }
        engineContext.setChromeExecutable(chromeExecutable);

        runnerTestResults.startTests(testConfig, outputDir);

        UserContext userContext = new UserContext(engineContext);
        userContext.setKeepBrowserOpen(testConfig.getKeepBrowserOpen());
        userContext.setReuseSession(testConfig.getReuseSession());

        try {
            for (RunnerTest test : testConfig.getTests()) {
                processTest(userContext, test);
            }
        } finally {
            userContext.cleanup();
            engineContext.cleanup();
            runnerTestResults.endTests();
        }

        return (runnerTestResults.failedTestsCount() == 0);
    }

    private boolean processTest(UserContext userContext, RunnerTest test) {
        boolean success;
        String testName = test.getName();
        if (StringUtils.isEmpty(testName)) {
            testName = test.getFileName();
        }
        TestContext testContext = new TestContext(userContext, testName);
        testContext.setBaseDirectory(Path.getParentDirectory(test.getFile()));
        try {
            success = processTest(testContext, test);
        } finally {
            testContext.cleanup();
        }
        return success;
    }

    private boolean processTest(TestContext testContext, RunnerTest test) {
        boolean success = false;

        String fileName = test.getFileName();

        JavaScriptEngine engine = new JavaScriptEngine();
        ConsoleOutputWriter consoleLogWriter = null;

        File outputDir = new File(this.outputDir, fileName);
        outputDir.mkdirs();

        testContext.setOutputDirectory(outputDir);

        File consoleLogFilePath = Path.getCanonicalFile(new File(outputDir, fileName + ".txt"));
        try {
            engine.init(testContext);
            try {
                consoleLogWriter = new ConsoleOutputWriter(consoleLogFilePath);
                consoleLogWriter.setWriteTimestamps(runnerSettings.consoleWriteTimeStamps());
                engine.setConsole(consoleLogWriter);
            } catch (IOException e) {
                Logger.error("Unable to write output {}", consoleLogFilePath);
            }

            success = runScript(testContext, engine, test);
        } finally {
            Browser browser = testContext.getOpenBrowser();
            try {
                if (browser != null) {
                    Data screenshot = browser.screenshot(runnerSettings.screenshotType());
                    File screenshotFile = Path.getCanonicalFile(
                        new File(outputDir, fileName + "." + runnerSettings.screenshotType()));
                    try (FileOutputStream fileOutputStream = new FileOutputStream(screenshotFile)) {
                        DataOutputStream os = new DataOutputStream(fileOutputStream);
                        os.write(screenshot.getBytes());
                        os.close();
                    }
                    testContext.addFile(screenshotFile);
                    Logger.info("Wrote screenshot: {}", screenshotFile.getPath());

                    if (testContext.getEngineSettings().saveChromeLogs()) {
                        ChromeProcess process = ((ChromeBrowser)browser).getProcess();
                        File browserLogFile = process.getBrowserLogFile();

                        File destFile = Path.getCanonicalFile(
                            new File(outputDir, browserLogFile.getName()));

                        Files.copy(browserLogFile.toPath(), destFile.toPath());

                        testContext.addFile(destFile);
                    }
                }
            } catch (Exception e) {
                Logger.error("Error capturing screenshot: {}", e.getMessage());
            }

            engine.finish();

            if (consoleLogWriter != null) {
                if (consoleLogWriter.outputWritten()) {
                    testContext.addFile(consoleLogFilePath);
                }
                consoleLogWriter.close();
            }
        }

        TestResult testResult = testContext.getTestResult();
        File harFile = Path.getCanonicalFile(new File(outputDir, fileName + ".har"));
        try {
            HarWriter.save(testResult, harFile);
            testContext.addFile(harFile);
        } catch (IOException e) {
            Logger.error(String.format("Unable to save har file: %s", e.getMessage()));
        }

        return success;
    }

    private boolean runScript(TestContext testContext, JavaScriptEngine engine, RunnerTest test) {
        try {
            runnerTestResults.startTest(test, testContext);
            if (!test.getFile().exists()) {
                throw new IOException("File not found '" + test.getFile().getAbsolutePath() + "'");
            }
            String scriptContexts = FileUtils.readAllText(test.getFile());
            if (scriptContexts == null) {
                throw new IOException("Error reading '" + test.getFile().getAbsolutePath() + "'");
            }
            Object result = engine.runScript(scriptContexts, test.getFileName(), test.getTimeout());
            runnerTestResults.endTest(testContext, engine.valueToString(result));
            return true;
        } catch (IOException|ScriptException e) {
            runnerTestResults.endTestFailed(testContext, e.getMessage());
            return false;
        }
    }
}
