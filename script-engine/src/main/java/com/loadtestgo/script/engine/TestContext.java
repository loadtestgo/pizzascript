package com.loadtestgo.script.engine;

import com.loadtestgo.script.api.Data;
import com.loadtestgo.script.api.TestResult;
import com.loadtestgo.script.api.TestResultFile;
import com.loadtestgo.script.engine.internal.api.ChromeBrowser;
import com.loadtestgo.script.engine.internal.browsers.chrome.ChromeSettings;
import com.loadtestgo.util.Dirs;
import org.apache.commons.io.IOUtils;
import org.pmw.tinylog.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A context used in a single script/test run.
 *
 * A new test context is created for every test.
 */
public class TestContext {
    protected UserContext userContext;
    protected ChromeBrowser openBrowser;
    protected TestResult testResult;
    protected ResultsNotifier resultNotifier;
    protected ProcessLauncher processLauncher;
    protected BrowserLifeCycleNotifier browserNotifier;
    protected boolean sandboxJavaScript = false;
    protected File baseDirectory; // base directory for reading files (such as script includes, or CSV files)
    protected File outputDirectory;

    protected final AtomicBoolean duringBrowserOpen = new AtomicBoolean(false);
    protected long browserOpenStartTime;
    protected String videoFilePath;
    protected boolean captureVideo = true;

    public TestContext(UserContext userContext) {
        this.userContext = userContext;
        this.testResult = new TestResult();
        commonTestResultSetup();
    }

    public TestContext(UserContext userContext, String name) {
        this.userContext = userContext;
        this.testResult = new TestResult(name);
        commonTestResultSetup();
    }

    public TestContext(UserContext userContext, TestResult result) {
        this.userContext = userContext;
        this.testResult = result;
        commonTestResultSetup();
    }

    protected void commonTestResultSetup() {
        EngineContext engineContext = userContext.getEngineContext();
        this.testResult.setIp(engineContext.getLocalPublicIp());
        this.testResult.setBotName(engineContext.getBotName());
        captureVideo = getEngineSettings().captureVideo();
    }

    public UserContext getUserContext() {
        return userContext;
    }

    public EngineContext getEngineContext() {
        return userContext.getEngineContext();
    }

    public ResultsNotifier getResultNotifier() {
        return resultNotifier;
    }

    public void setResultNotifier(ResultsNotifier resultNotifier) {
        this.resultNotifier = resultNotifier;
    }

    public TestResult getTestResult() {
        return testResult;
    }

    public void setOpenBrowser(ChromeBrowser openBrowser) {
        if (openBrowser != null && this.openBrowser != openBrowser) {
            if (browserNotifier != null) {
                browserNotifier.browserOpened();
            }
        }

        this.openBrowser = openBrowser;
    }

    public ChromeBrowser getOpenBrowser() {
        return openBrowser;
    }

    public ProcessLauncher getProcessLauncher() {
        return processLauncher;
    }

    public void setProcessLauncher(ProcessLauncher processLauncher) {
        this.processLauncher = processLauncher;
    }

    public void cleanup() {
        if (openBrowser != null) {
            openBrowser.close();
            openBrowser = null;
        }
    }

    public String getIp() {
        return userContext.getEngineContext().getLocalPublicIp();
    }

    public String getTestTmpDir() {
        return String.format("%s/%d", Dirs.getTmp(), userContext.getWorkerId());
    }

    public ChromeSettings getDefaultChromeSettings() {
        return new ChromeSettings();
    }

    public BrowserLifeCycleNotifier getBrowserNotifier() {
        return browserNotifier;
    }

    public void setBrowserNotifier(BrowserLifeCycleNotifier browserNotifier) {
        this.browserNotifier = browserNotifier;
    }

    public boolean getIsFileSystemSandboxed() {
        return false;
    }

    public File getFile(String filename) {
        File file = new File(filename);
        if (file.isAbsolute()) {
            return file;
        } else {
            return new File(baseDirectory, filename);
        }
    }

    public boolean sandboxJavaScript() {
        return sandboxJavaScript;
    }

    public void setSandboxJavaScript(boolean sandboxJavaScript) {
        this.sandboxJavaScript = sandboxJavaScript;
    }

    public File getBaseDirectory() {
        return baseDirectory;
    }

    public void setBaseDirectory(File baseDirectory) {
        this.baseDirectory = baseDirectory;
    }

    public EngineSettings getEngineSettings() {
        return getEngineContext().getEngineSettings();
    }

    public void startBrowserOpen() {
        synchronized (duringBrowserOpen) {
            browserOpenStartTime = System.currentTimeMillis();
            duringBrowserOpen.set(true);
        }
    }

    public void endBrowserOpen() {
        synchronized (duringBrowserOpen) {
            long endTime = System.currentTimeMillis();
            TestResult testResult = getTestResult();
            if (testResult != null) {
                testResult.addSetupTime((int) (endTime - browserOpenStartTime));
            }
            duringBrowserOpen.set(false);
        }
    }

    public long getBrowserOpenTime() {
        synchronized (duringBrowserOpen) {
            long setupTime = testResult.getSetupTime();
            long currentTime = System.currentTimeMillis();
            if (duringBrowserOpen.get()) {
                setupTime += currentTime - browserOpenStartTime;
            }
            return setupTime;
        }
    }

    public void setOutputDirectory(File outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    public File getOutputDirectory() {
        if (outputDirectory != null) return outputDirectory;
        if (getIsFileSystemSandboxed()) return new File(getTestTmpDir());
        return baseDirectory;
    }

    public void saveFile(String name, Data data) {
        File file = new File(getOutputDirectory(), name);
        try (FileOutputStream output = new FileOutputStream(file)) {
            IOUtils.write(data.getBytes(), output);
            this.testResult.getSavedFiles().add(new TestResultFile(name, file));
        } catch (IOException e) {
            Logger.error(e, "Unable to save script file {}", file);
            throw new ScriptException(String.format("Unable to save file %s", name));
        }
    }

    public void addFile(File file) {
        this.testResult.getSavedFiles().add(new TestResultFile(file));
    }

    public void setVideoFilePath(String videoFilePath) {
        this.videoFilePath = videoFilePath;
    }

    public String getVideoFilePath() {
        return videoFilePath;
    }

    public void setCaptureVideo(boolean captureVideo) {
        this.captureVideo = captureVideo;
    }

    public boolean getCaptureVideo() {
        return captureVideo;
    }

    public String getTestName() {
        return testResult.getTestName();
    }
}
