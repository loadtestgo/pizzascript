package com.loadtestgo.script.engine;

import com.loadtestgo.script.api.TestResult;
import com.loadtestgo.script.engine.internal.api.CSVImpl;
import com.loadtestgo.script.engine.internal.api.ChromeBrowser;
import com.loadtestgo.script.engine.internal.browsers.chrome.ChromeSettings;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

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
    protected boolean sandboxJavaScript = true;

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
        return String.format("%s/%d", EngineSettings.getTmpDir(), userContext.getUserId());
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
        try {
            return new File(filename).getCanonicalFile();
        } catch (IOException e) {
            throw new ScriptException("Unable to find file '" + filename + "'");
        }
    }

    public boolean sandboxJavaScript() {
        return sandboxJavaScript;
    }

    public void setSandboxJavaScript(boolean sandboxJavaScript) {
        this.sandboxJavaScript = sandboxJavaScript;
    }
}
