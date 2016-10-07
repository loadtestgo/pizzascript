package com.loadtestgo.script.engine;

import com.loadtestgo.script.api.TestResult;
import com.loadtestgo.script.engine.internal.api.ChromeBrowser;
import com.loadtestgo.script.engine.internal.browsers.chrome.ChromeSettings;
import com.loadtestgo.util.Dirs;

import java.io.File;

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
    protected File baseDirectory;

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
        return String.format("%s/%d", Dirs.getTmp(), userContext.getUserId());
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
}
