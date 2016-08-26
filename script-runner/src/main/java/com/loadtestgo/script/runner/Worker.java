package com.loadtestgo.script.runner;

import com.loadtestgo.script.api.Browser;
import com.loadtestgo.script.api.Data;
import com.loadtestgo.script.api.TestResult;
import com.loadtestgo.script.engine.*;
import com.loadtestgo.script.har.HarWriter;
import com.loadtestgo.util.FileUtils;
import com.loadtestgo.util.Path;
import com.loadtestgo.util.Settings;
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
import java.util.List;

public class Worker {
    private File outputDir;
    private RunnerTestResults runnerTestResults;

    public Worker() {
    }

    public void init(String outputDir, RunnerTestResults runnerTestResults) {
        this.runnerTestResults = runnerTestResults;

        this.outputDir = new File(outputDir);
        this.outputDir.mkdirs();

        Configurator.defaultConfig()
            .writer(new FileWriter(new File(this.outputDir, "log.txt").getAbsolutePath()))
            .level(Level.INFO)
            .activate();

        Settings.loadSettings();
    }

    public boolean runJobs(List<File> files, long timeout) {
        EngineContext engineContext = new EngineContext();
        try {
            engineContext.setLocalPublicIp(Inet4Address.getLocalHost().getHostAddress());
        } catch (UnknownHostException e) {
            Logger.error(e);
        }

        runnerTestResults.startTests();

        UserContext userContext = new UserContext(engineContext);
        try {
            for (File file : files) {
                processFile(userContext, file, timeout);
            }
        } finally {
            engineContext.cleanup();
            runnerTestResults.endTests();
        }

        return (runnerTestResults.failedTestsCount() == 0);
    }

    private boolean processFile(UserContext userContext, File file, long timeout) {
        boolean success;
        TestContext testContext = new TestContext(userContext);
        testContext.setBaseDirectory(Path.getParentDirectory(file));
        try {
            success = processFile(file, testContext, timeout);
        } finally {
            testContext.cleanup();
        }
        return success;
    }

    private boolean processFile(File file, TestContext testContext, long timeout) {
        boolean success = false;

        String filename = file.getName();

        JavaScriptEngine engine = new JavaScriptEngine();
        ConsoleOutputWriter outputWriter = null;
        try {
            engine.init(testContext);
            File outputText = new File(outputDir, filename + ".txt");
            try {
                outputWriter = new ConsoleOutputWriter(outputText);
                engine.setConsole(outputWriter);
            } catch (IOException e) {
                Logger.error("Unable to write output {}", outputText);
            }

            success = runScript(file, engine, timeout);
        } finally {
            Browser browser = testContext.getOpenBrowser();
            try {
                if (browser != null) {
                    Data screenshot = browser.screenshot("jpeg");
                    File screenshotFile = new File(outputDir, filename + ".jpeg");
                    try (FileOutputStream fileOutputStream = new FileOutputStream(screenshotFile)) {
                        DataOutputStream os = new DataOutputStream(fileOutputStream);
                        os.write(screenshot.getBytes());
                        os.close();
                    }
                    Logger.info("Wrote screenshot: {}", screenshotFile.getPath());
                }
            } catch (Exception e) {
                Logger.error("Error capturing screenshot: {}", e.getMessage());
            }

            if (outputWriter != null) {
                outputWriter.close();
            }

            engine.finish();
        }

        TestResult testResult = testContext.getTestResult();
        File harFile = new File(outputDir, filename + ".har");
        try {
            HarWriter.save(testResult, harFile);
        } catch (IOException e) {
            Logger.error(String.format("Unable to save har file: %s", e.getMessage()));
        }

        return success;
    }

    private boolean runScript(File file, JavaScriptEngine engine, long timeout) {
        try {
            runnerTestResults.startTest(file.getName());
            String scriptContexts = FileUtils.readAllText(file);
            if (scriptContexts == null) {
                throw new IOException("Error reading '" + file.getPath() + "'");
            }
            Object result = engine.runScript(scriptContexts, file.getPath(), timeout);
            runnerTestResults.endTest(engine.valueToString(result));
            return true;
        } catch (IOException|ScriptException e) {
            runnerTestResults.endTestFailed(e.getMessage());
            return false;
        }
    }
}
