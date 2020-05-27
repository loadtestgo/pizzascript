package com.loadtestgo.script.engine.internal.api;

import com.loadtestgo.script.api.*;
import com.loadtestgo.script.engine.*;
import com.loadtestgo.script.engine.internal.browsers.chrome.ChromeSettings;
import com.loadtestgo.script.engine.internal.rhino.RhinoUtils;
import org.mozilla.javascript.NativeFunction;
import org.mozilla.javascript.NativeObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

public class PizzaImpl implements Pizza {
    private TestContext testContext;
    private JavaScriptEngine javaScriptEngine;

    public PizzaImpl(TestContext testContext,
                     JavaScriptEngine javaScriptEngine) {
        this.testContext = testContext;
        this.javaScriptEngine = javaScriptEngine;
    }

    @Override
    public Browser open() {
        return open((NativeObject)null);
    }

    @Override
    public Browser open(NativeObject settings) {
        Browser browser = testContext.getOpenBrowser();
        if (browser != null) {
            browser.close();
        }

        ChromeSettings chromeSettings = testContext.getDefaultChromeSettings();
        if (settings != null) {
            chromeSettings.enableQuic =
                RhinoUtils.toBoolean(settings.get("enableQuic"));

            chromeSettings.ignoreCertErrors =
                RhinoUtils.toBoolean(settings.get("ignoreCertErrors"));

            chromeSettings.forceQuicForHost =
                RhinoUtils.toString(settings.get("forceQuicForHost"));

            chromeSettings.startMaximized =
                RhinoUtils.toBoolean(settings.get("startMaximized"));

            chromeSettings.args =
                RhinoUtils.toStringList(settings.get("args"));

            // Deserialize preference overrides into Map<String,Object>
            Object preferences = settings.get("preferences");
            if (preferences instanceof NativeObject) {
                chromeSettings.preferences =
                    RhinoUtils.deserialize((NativeObject) preferences);
            }
        }

        browser = new ChromeBrowser(testContext, chromeSettings);

        return browser;
    }

    @Override
    public Browser open(String url) {
        Browser browser = open();
        browser.open(url);
        return browser;
    }

    @Override
    public Browser browser() {
        return testContext.getOpenBrowser();
    }

    @Override
    public void sleep(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            throw new ScriptException(ErrorType.Timeout, "sleep() interrupted");
        }
    }

    @Override
    public void waitFor(NativeFunction func) {
        waitFor(func, 100);
    }

    @Override
    public void waitFor(NativeFunction func, long waitIterationMilliseconds) {
        while (!RhinoUtils.toBoolean(func.call(javaScriptEngine.getContext(),
            javaScriptEngine.getScope(),
            javaScriptEngine.getScope(),
            new Object[0]))) {
            try {
                Thread.sleep(waitIterationMilliseconds);
            } catch (InterruptedException e) {
                throw new ScriptException(ErrorType.Timeout, "waitFor() interrupted");
            }
        }
    }

    @Override
    public Page[] getPages() {
        ArrayList<Page> pages = testContext.getTestResult().getPages();
        return pages.toArray(new Page[pages.size()]);
    }

    @Override
    public TestResult getResult() {
        return testContext.getTestResult();
    }

    @Override
    public String getIp() {
        return testContext.getIp();
    }

    @Override
    public String getVersion() {
        return testContext.getEngineContext().getAPIVersion();
    }

    @Override
    public int getUserId() {
        return testContext.getUserContext().getUserId();
    }

    @Override
    public int getSequenceId() {
        return testContext.getUserContext().getSeqId();
    }

    @Override
    public CSV openCSV(String filename) {
        File file = testContext.getFile(filename);
        try {
            return new CSVImpl(file);
        } catch (IOException e) {
            throw new ScriptException(e.getMessage());
        }
    }

    @Override
    public void saveFile(String name, Data data) {
        testContext.saveFile(name, data);
    }

    @Override
    public void saveFile(String name, String data) {
        try {
            testContext.saveFile(name, new Data("", data.getBytes("UTF-8")));
        } catch (UnsupportedEncodingException e) {
            // ignore
        }
    }

    @Override
    public HttpRequest getRequest(int requestIndex) {
        Page page = getResult().getPage(0);
        if (page != null) {
            if (requestIndex >= 0 && requestIndex < page.getRequests().size()) {
                return page.getRequests().get(requestIndex);
            }
        }
        return null;
    }

    @Override
    public HttpRequest getRequestByUrl(String partialUrl) {
        for (int i = 0; i < getResult().getPages().size(); ++i) {
            HttpRequest request = getRequestByUrl(i, partialUrl);
            if (request != null) {
                return request;
            }
        }
        return null;
    }

    @Override
    public HttpRequest getRequestByFullUrl(String fullUrl) {
        for (int i = 0; i < getResult().getPages().size(); ++i) {
            HttpRequest request = getRequestByFullUrl(i, fullUrl);
            if (request != null) {
                return request;
            }
        }
        return null;
    }

    @Override
    public HttpRequest getRequest(int pageIndex, int requestIndex) {
        Page page = getResult().getPage(pageIndex);
        if (page != null) {
            if (requestIndex >= 0 && requestIndex < page.getRequests().size()) {
                return page.getRequests().get(requestIndex);
            }
        }
        return null;
    }

    @Override
    public HttpRequest getRequestByUrl(int pageIndex, String partialUrl) {
        Page page = getResult().getPage(pageIndex);
        if (page != null) {
            for (HttpRequest httpRequest : page.getRequests()) {
                if (httpRequest.getUrl().contains(partialUrl)) {
                    return httpRequest;
                }
            }
        }
        return null;
    }

    @Override
    public HttpRequest getRequestByFullUrl(int pageIndex, String fullUrl) {
        Page page = getResult().getPage(pageIndex);
        if (page != null) {
            for (HttpRequest httpRequest : page.getRequests()) {
                if (httpRequest.getUrl().equals(fullUrl)) {
                    return httpRequest;
                }
            }
        }
        return null;
    }

    @Override
    public int getLoadTestId() {
        return getEngineContext().getLoadTestId();
    }

    @Override
    public String getLocation() {
        return getEngineContext().getLocation();
    }

    @Override
    public int getBotId() {
        return getEngineContext().getLoadTestBotId();
    }

    @Override
    public String toString() {
        return String.format("Pizza %s", getVersion());
    }

    private EngineContext getEngineContext() {
        return testContext.getUserContext().getEngineContext();
    }
}
