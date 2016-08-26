package com.loadtestgo.script.engine.internal.api;

import com.loadtestgo.script.api.*;
import com.loadtestgo.script.engine.ScriptException;
import com.loadtestgo.script.engine.TestContext;
import com.loadtestgo.script.engine.UserContext;
import com.loadtestgo.script.engine.internal.browsers.chrome.ChromeProcess;
import com.loadtestgo.script.engine.internal.browsers.chrome.ChromeSettings;
import com.loadtestgo.script.engine.internal.browsers.chrome.ChromeWebSocket;
import com.loadtestgo.script.engine.internal.rhino.RhinoUtils;
import com.loadtestgo.script.engine.internal.server.BrowserWebSocketServer;
import com.loadtestgo.util.Http;
import org.json.JSONException;
import org.json.JSONObject;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.regexp.NativeRegExp;
import org.pmw.tinylog.Logger;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;

public class ChromeBrowser implements Browser {
    private ChromeWebSocket pizzaHandler;
    private ChromeProcess chromeProcess;
    private TestContext testContext;
    private boolean ignoreHttpErrors = false;
    private ArrayList<Integer> ignoreHttpErrorCodes = null;

    public ChromeBrowser(TestContext testContext)
    {
        init(testContext, new ChromeSettings());
    }

    public ChromeBrowser(TestContext testContext, ChromeSettings settings)
    {
        init(testContext, settings);
    }

    private void init(TestContext testContext, ChromeSettings settings)
    {
        this.testContext = testContext;

        long startTime = -1;
        try {
            startTime = System.currentTimeMillis();

            UserContext userContext = testContext.getUserContext();

            if (userContext.keepBrowserOpen()) {
                chromeProcess = userContext.getChromeProcess();
                pizzaHandler = userContext.getPizzaHandler();
            }

            /*
            if (chromeProcess == null) {
                if (userContext.isChromeProfileInitialSetup()) {
                    settings.unpackExtension = false;
                    settings.reuseProfile = true;
                }
            }*/

            // Start the websocket listener
            if (pizzaHandler == null) {
                BrowserWebSocketServer webSocket = userContext.getEngineContext().getWebSocketServer();
                pizzaHandler = new ChromeWebSocket(testContext);
                webSocket.initHandler(userContext.getUserId(), pizzaHandler);
            } else {
                pizzaHandler.reset(testContext);
            }

            if (chromeProcess == null) {
                // Only now launch the chrome process, we want our socket to be available for chrome
                // to talk to!
                openBrowserRetryLoop(settings);

                userContext.setChromeProfileInitialSetup(true);
            }

            if (userContext.keepBrowserOpen()) {
                userContext.setChromeProcess(chromeProcess);
                userContext.setPizzaHandler(pizzaHandler);
            }

            TestResult result = testContext.getTestResult();
            result.setBrowserName(getBrowserName());
            result.setBrowserVersion(getBrowserVersion());

            testContext.setOpenBrowser(this);
        } finally {
            long endTime = System.currentTimeMillis();
            TestResult testResult = testContext.getTestResult();
            if (testResult != null) {
                testResult.addSleepTime((int)(endTime - startTime));
            }
        }
    }

    private void openBrowserRetryLoop(ChromeSettings settings) {
        if (openBrowserAndWait(settings)) {
            return;
        }

        int retry = settings.openBrowserRetryCount;
        for (int i = 0; i < retry; ++i) {
            Logger.warn("Chrome didn't start, try #{}...", i);
            if (openBrowserAndWait(settings)) {
                return;
            }
        }

        throw new ScriptException(ErrorType.Internal, "Waiting for browser timed out. " +
                "There is probably a browser configuration error or the browser has hung.");
    }

    private boolean openBrowserAndWait(ChromeSettings settings) {
        if (startBrowserAndWaitForConnection(settings)) {
            return true;
        }

        // Chrome didn't connect to us, but make sure we kill it none
        // the less.
        if (chromeProcess != null) {
            chromeProcess.close();
        }

        return false;
    }

    private boolean startBrowserAndWaitForConnection(ChromeSettings settings) {
        chromeProcess = new ChromeProcess(testContext, settings);
        chromeProcess.start(testContext.getProcessLauncher());

        // Wait for the Browser to connect back to us
        return pizzaHandler.waitForConnection();
    }

    public void close() {
        if (testContext.getUserContext().keepBrowserOpen()) {
            checkResponseForErrors(pizzaHandler.sendCommand("reset"));
            pizzaHandler.reset();
        } else {
            pizzaHandler.close();
            chromeProcess.close();
        }
        testContext.setOpenBrowser(null);
    }

    @Override
    public void emulateDevice(String name) {
        HashMap<String,Object> params = new HashMap<>();
        params.put("name", name);
        checkResponseForErrors(pizzaHandler.sendCommand("emulateDevice", params));
    }

    @Override
    public Object listDevices() {
        return getResponseData(pizzaHandler.sendCommand("listDevices"));
    }

    @Override
    public void emulateNetworkCondition(NativeObject conditions) {
        checkResponseForErrors(pizzaHandler.sendCommand("emulateNetworkCondition", RhinoUtils.deserialize(conditions)));
    }

    @Override
    public void emulateNetworkCondition(String name) {
        HashMap<String,Object> params = new HashMap<>();
        params.put("name", name);
        checkResponseForErrors(pizzaHandler.sendCommand("emulateNetworkCondition", params));
    }

    @Override
    public Object listNetworkConditions() {
        return getResponseData(pizzaHandler.sendCommand("listNetworkConditions"));
    }

    @Override
    public void ignoreHttpErrors() {
        this.ignoreHttpErrors(true);
    }

    @Override
    public void ignoreHttpErrors(boolean ignore) {
        this.ignoreHttpErrors = ignore;
        this.ignoreHttpErrorCodes = null;
    }

    @Override
    public void ignoreHttpErrors(Integer... statusCodesToIgnore) {
        this.ignoreHttpErrorCodes = new ArrayList<>();
        for (Integer i : statusCodesToIgnore) {
            this.ignoreHttpErrorCodes.add(i);
        }
    }

    @Override
    public Page open(String url) {
        HashMap<String,Object> params = new HashMap<>();
        params.put("url", Http.prependHttpToUrl(url));
        return checkNavigationSuccess(pizzaHandler.sendCommand("open", params));
    }

    private Page checkNavigationSuccess(JSONObject result) {
        try {
            JSONObject response = result.getJSONObject("response");
            String navUrl = response.optString("url");
            if (response.has("error")) {
                if (navUrl == null || navUrl.isEmpty()) {
                    throw new ScriptException(ErrorType.Navigation, response.getString("error"));
                }
                if (!ignoreHttpErrors) {
                    throw new ScriptException(ErrorType.Navigation,
                        String.format("Error navigating to '%s': %s", navUrl, response.getString("error")));
                }
            }
            int tabId = response.optInt("tabId");
            int frameId = response.optInt("frameId");
            int processId = response.optInt("processId");

            TestResult testResult = testContext.getTestResult();
            synchronized (testResult) {
                ArrayList<Page> pages = testContext.getTestResult().getPages();
                for (int i = pages.size() - 1; i >= 0; --i) {
                    Page page = pages.get(i);
                    if (page.getUrl().equals(navUrl) && page.getFrameId() == frameId &&
                        page.getTabId() == tabId && page.getProcessId() == processId) {
                        if (!ignoreHttpErrors) {
                            checkPageForInvalidHttpStatusCodes(page);
                        }
                        return page;
                    }
                }
            }
        } catch (JSONException e) {
            throw new ScriptException(ErrorType.Internal, "Unable to parse command response.");
        }

        return testContext.getTestResult().getLastPage();
    }

    /**
     * Make sure you are synchronised on the test result before calling this
     * @param page
     */
    private void checkPageForInvalidHttpStatusCodes(Page page) {
        ArrayList<HttpRequest> requests = page.getRequests();
        for (HttpRequest request : requests) {
            if (request.getParentFrameId() == 0 &&
                request.getResourceType() == ResourceType.Document) {
                int statusCode = request.getStatusCode();
                if (statusCode >= 400 && statusCode <= 600) {
                    if (this.ignoreHttpErrorCodes == null ||
                        !this.ignoreHttpErrorCodes.contains(statusCode)) {
                        throw new ScriptException(ErrorType.Navigation,
                            String.format("Error fetching '%s': HTTP status '%d %s'",
                                request.getUrl(), request.getStatusCode(), request.getStatusText()));
                    }
                }
            }
        }
    }

    @Override
    public void openAsync(String url) {
        HashMap<String,Object> params = new HashMap<>();
        params.put("url", Http.prependHttpToUrl(url));
        checkResponseForErrors(pizzaHandler.sendCommand("openAsync", params));
    }

    @Override
    public void newPage() {
        pizzaHandler.newPage();
    }

    @Override
    public void newPage(String pageName) {
        pizzaHandler.newPage(pageName);
    }

    @Override
    public String getUrl() {
        HashMap<String,Object> params = new HashMap<>();
        return getResponseString(pizzaHandler.sendCommand("getUrl", params));
    }

    @Override
    public void verifyText(String text) {
        HashMap<String,Object> params = new HashMap<>();
        params.put("text", text);
        JSONObject result = pizzaHandler.sendCommand("verifyText", params);
        if (!getResponseBoolean(result)) {
            throw new ScriptException(String.format("Unable to find text '%s'", text));
        }
    }

    @Override
    public void verifyNotText(String text) {
        HashMap<String,Object> params = new HashMap<>();
        params.put("text", text);
        JSONObject result = pizzaHandler.sendCommand("verifyText", params);
        if (getResponseBoolean(result)) {
            throw new ScriptException(String.format("Found text '%s'", text));
        }
    }

    @Override
    public void verifyText(NativeRegExp regexp) {
        HashMap<String,Object> params = new HashMap<>();
        String text = regexp.toString();
        params.put("regexp", text);
        JSONObject result = pizzaHandler.sendCommand("verifyText", params);
        if (!getResponseBoolean(result)) {
            throw new ScriptException(String.format("Unable to find text matching '%s'", text));
        }
    }

    @Override
    public void verifyNotText(NativeRegExp regexp) {
        HashMap<String,Object> params = new HashMap<>();
        String text = regexp.toString();
        params.put("regexp", text);
        JSONObject result = pizzaHandler.sendCommand("verifyText", params);
        if (getResponseBoolean(result)) {
            throw new ScriptException(String.format("Found text matching '%s'", text));
        }
    }

    @Override
    public void verifyTitle(String title) {
        HashMap<String,Object> params = new HashMap<>();
        params.put("text", title);
        JSONObject result = pizzaHandler.sendCommand("verifyTitle", params);
        if (!getResponseBoolean(result)) {
            throw new ScriptException(String.format("Page title did not match '%s'", title));
        }
    }

    @Override
    public String getTitle() {
        return getResponseString(pizzaHandler.sendCommand("getTitle"));
    }

    @Override
    public void verifyTitle(NativeRegExp regexp) {
        HashMap<String,Object> params = new HashMap<>();
        String text = regexp.toString();
        params.put("regexp", text);
        JSONObject result = pizzaHandler.sendCommand("verifyTitle", params);
        if (!getResponseBoolean(result)) {
            throw new ScriptException(String.format("Page title did not match %s", text));
        }
    }

    @Override
    public Object execute(String script) {
        HashMap<String,Object> params = new HashMap<>();
        params.put("script", script);
        return getResponseData(pizzaHandler.sendCommand("execute", params));
    }

    @Override
    public Object jq(String script) {
        HashMap<String,Object> params = new HashMap<>();
        params.put("script", script);
        return getResponseData(pizzaHandler.sendCommand("jq", params));
    }

    @Override
    public Object listFrames() {
        return getResponseData(pizzaHandler.sendCommand("listFrames"));
    }

    @Override
    public Object listFrames(String selector) {
        HashMap<String,Object> params = new HashMap<>();
        params.put("selector", selector);
        return getResponseData(pizzaHandler.sendCommand("listFrames", params));
    }

    @Override
    public Object listAllFrames() {
        return getResponseData(pizzaHandler.sendCommand("listAllFrames"));
    }

    @Override
    public Object selectFrame(String selector) {
        HashMap<String,Object> params = new HashMap<>();
        params.put("selector", selector);
        return getResponseData(pizzaHandler.sendCommand("selectFrame", params));
    }

    @Override
    public Object selectFrameCss(String selector) {
        HashMap<String,Object> params = new HashMap<>();
        params.put("selector", selector);
        return getResponseData(pizzaHandler.sendCommand("selectFrameCss", params));
    }

    @Override
    public Object selectTopFrame() {
        return getResponseData(pizzaHandler.sendCommand("selectTopFrame"));
    }

    @Override
    public void blockUrl(String url) {
        HashMap<String,Object> params = new HashMap<>();
        params.put("url", url);
        checkResponseForErrors(pizzaHandler.sendCommand("blockUrl", params));
    }

    @Override
    public void blockUrl(String... urls) {
        HashMap<String,Object> params = new HashMap<>();
        params.put("url", urls);
        checkResponseForErrors(pizzaHandler.sendCommand("blockUrl", params));
    }

    @Override
    public void block3rdPartyUrls() {
        HashMap<String,Object> params = new HashMap<>();
        params.put("url", Globals.ThirdParty);
        checkResponseForErrors(pizzaHandler.sendCommand("blockUrl", params));
    }

    @Override
    public void rewriteUrl(String url, String rewriteUrl) {
        HashMap<String,Object> params = new HashMap<>();
        params.put("url", url);
        params.put("rewriteUrl", rewriteUrl);
        checkResponseForErrors(pizzaHandler.sendCommand("rewriteUrl", params));
    }

    @Override
    public void clearRules() {
        checkResponseForErrors(pizzaHandler.sendCommand("clearRules"));
    }

    @Override
    public Object listTabs() {
        return getResponseData(pizzaHandler.sendCommand("listTabs"));
    }

    @Override
    public Object selectTab(NativeObject tab) {
        HashMap<String,Object> params = new HashMap<>();
        params.put("tab", RhinoUtils.deserialize(tab));
        return getResponseData(pizzaHandler.sendCommand("selectTab", params));
    }

    @Override
    public Object selectLatestTab() {
        return getResponseData(pizzaHandler.sendCommand("selectLatestTab"));
    }

    @Override
    public Object selectMainTab() {
        return getResponseData(pizzaHandler.sendCommand("selectMainTab"));
    }

    @Override
    public Object newTab() {
        return getResponseData(pizzaHandler.sendCommand("newTab"));
    }

    @Override
    public void setHeader(String name, String value) {
        HashMap<String,Object> params = new HashMap<>();
        params.put("name", name);
        params.put("value", value);
        JSONObject response = pizzaHandler.sendCommand("setHeader", params);
        checkResponseForErrors(response);
    }

    @Override
    public void removeHeader(String key) {
        HashMap<String,Object> params = new HashMap<>();
        params.put("name", key);
        JSONObject response = pizzaHandler.sendCommand("removeHeader", params);
        checkResponseForErrors(response);
    }

    @Override
    public void setUserAgent(String userAgent) {
        HashMap<String,Object> params = new HashMap<>();
        params.put("userAgent", userAgent);
        checkResponseForErrors(pizzaHandler.sendCommand("setUserAgent", params));
    }

    @Override
    public void setCredentials(String username, String password) {
        HashMap<String,Object> params = new HashMap<>();
        params.put("username", username);
        params.put("password", password);
        checkResponseForErrors(pizzaHandler.sendCommand("setCredentials", params));
    }

    @Override
    public void waitForHttpRequests(long idleTimeMS) {
        long startTime = System.currentTimeMillis();
        while (true) {
            long now =  System.currentTimeMillis();
            if (!pizzaHandler.isOpen()) {
                throw new ScriptException(ErrorType.Internal, "Browser closed");
            }
            if (!pizzaHandler.checkPendingRequests()) {
                long lastRequestTime = pizzaHandler.getLastRequestTime();
                if (lastRequestTime > 0) {
                    if (lastRequestTime > startTime) {
                        startTime = lastRequestTime;
                    }
                }

                if (startTime + idleTimeMS <= now) {
                    return;
                }
            }
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                throw new ScriptException(ErrorType.Timeout, "waitForHttpRequests() interrupted");
            }
        }
    }

    @Override
    public Page waitPageLoad() {
        return checkNavigationSuccess(pizzaHandler.sendCommand("waitPageLoad"));
    }

    @Override
    public Page waitPageLoad(long timeoutMS) {
        HashMap<String,Object> params = new HashMap<>();
        params.put("timeout", timeoutMS);
        return checkNavigationSuccess(pizzaHandler.sendCommand("waitPageLoad", params));
    }

    @Override
    public void clearPageLoad() {
        checkResponseForErrors(pizzaHandler.sendCommand("clearPageLoad"));
    }

    @Override
    public void clearCache() {
        checkResponseForErrors(pizzaHandler.sendCommand("clearCache"));
    }

    @Override
    public void clearCookies() {
        checkResponseForErrors(pizzaHandler.sendCommand("clearCookies"));
    }

    @Override
    public void setCookie(String name, String value) {
        HashMap<String,Object> params = new HashMap<>();
        params.put("name", name);
        params.put("value", value);
        checkResponseForErrors(pizzaHandler.sendCommand("setCookie", params));
    }

    @Override
    public void setCookie(String name, String value, NativeObject details) {
        HashMap<String,Object> params = new HashMap<>();
        params.put("name", name);
        params.put("value", value);
        params.put("details", RhinoUtils.deserialize(details));
        checkResponseForErrors(pizzaHandler.sendCommand("setCookie", params));
    }

    @Override
    public Object getCookie(String name) {
        HashMap<String,Object> params = new HashMap<>();
        params.put("name", name);
        return getResponseData(pizzaHandler.sendCommand("getCookie", params));
    }

    @Override
    public Object getCookie(String url, String name) {
        HashMap<String,Object> params = new HashMap<>();
        params.put("url", url);
        params.put("name", name);
        return getResponseData(pizzaHandler.sendCommand("getCookie", params));
    }

    @Override
    public void removeCookie(String name) {
        HashMap<String,Object> params = new HashMap<>();
        params.put("name", name);
        checkResponseForErrors(pizzaHandler.sendCommand("removeCookie", params));
    }

    @Override
    public void removeCookie(String url, String name) {
        HashMap<String,Object> params = new HashMap<>();
        params.put("url", Http.prependHttpToUrl(url));
        params.put("name", name);
        checkResponseForErrors(pizzaHandler.sendCommand("removeCookie", params));
    }

    @Override
    public void click(String selector) {
        HashMap<String,Object> params = new HashMap<>();
        params.put("selector", selector);
        checkResponseForErrors(pizzaHandler.sendCommand("click", params));
    }

    @Override
    public void clickAt(String selector, int x, int y) {
        HashMap<String,Object> params = new HashMap<>();
        params.put("selector", selector);
        params.put("x", x);
        params.put("y", y);
        checkResponseForErrors(pizzaHandler.sendCommand("click", params));
    }

    @Override
    public void doubleClick(String selector) {
        HashMap<String,Object> params = new HashMap<>();
        params.put("selector", selector);
        checkResponseForErrors(pizzaHandler.sendCommand("doubleClick", params));
    }

    @Override
    public void doubleClickAt(String selector, int x, int y) {
        HashMap<String,Object> params = new HashMap<>();
        params.put("selector", selector);
        params.put("x", x);
        params.put("y", y);
        checkResponseForErrors(pizzaHandler.sendCommand("doubleClick", params));
    }

    @Override
    public void focus(String selector) {
        HashMap<String,Object> params = new HashMap<>();
        params.put("selector", selector);
        checkResponseForErrors(pizzaHandler.sendCommand("focus", params));
    }

    @Override
    public void hover(String selector) {
        HashMap<String,Object> params = new HashMap<>();
        params.put("selector", selector);
        checkResponseForErrors(pizzaHandler.sendCommand("hover", params));
    }

    @Override
    public void hoverAt(String selector, int x, int y) {
        HashMap<String,Object> params = new HashMap<>();
        params.put("selector", selector);
        params.put("x", x);
        params.put("y", y);
        checkResponseForErrors(pizzaHandler.sendCommand("hover", params));
    }

    @Override
    public void clear(String selector) {
        HashMap<String,Object> params = new HashMap<>();
        params.put("selector", selector);
        checkResponseForErrors(pizzaHandler.sendCommand("clear", params));
    }

    @Override
    public void selectContent(String selector) {
        HashMap<String,Object> params = new HashMap<>();
        params.put("selector", selector);
        checkResponseForErrors(pizzaHandler.sendCommand("selectContent", params));
    }

    @Override
    public void type(String selector, String text) {
        HashMap<String,Object> params = new HashMap<>();
        params.put("selector", selector);
        params.put("text", text);
        checkResponseForErrors(pizzaHandler.sendCommand("type", params));
    }

    @Override
    public void type(String selector, Number text) {
        HashMap<String,Object> params = new HashMap<>();
        params.put("selector", selector);
        params.put("text", text);
        checkResponseForErrors(pizzaHandler.sendCommand("type", params));
    }

    @Override
    public void type(String selector, NativeArray text) {
        HashMap<String,Object> params = new HashMap<>();
        params.put("selector", selector);
        params.put("text", new Object[] {text});
        checkResponseForErrors(pizzaHandler.sendCommand("type", params));
    }

    @Override
    public void type(String selector, Object... text) {
        HashMap<String,Object> params = new HashMap<>();
        params.put("selector", selector);
        params.put("text", text);
        checkResponseForErrors(pizzaHandler.sendCommand("type", params));
    }

    @Override
    public void check(String selector) {
        HashMap<String,Object> params = new HashMap<>();
        params.put("selector", selector);
        checkResponseForErrors(pizzaHandler.sendCommand("check", params));
    }

    @Override
    public void check(String selector, boolean on) {
        HashMap<String,Object> params = new HashMap<>();
        params.put("selector", selector);
        params.put("check", on);
        checkResponseForErrors(pizzaHandler.sendCommand("check", params));
    }

    @Override
    public void select(String selector, NativeObject value) {
        HashMap<String,Object> params = new HashMap<>();
        params.put("selector", selector);
        params.put("value", RhinoUtils.deserialize(value));
        checkResponseForErrors(pizzaHandler.sendCommand("select", params));
    }

    @Override
    public Object getValue(String selector) {
        HashMap<String,Object> params = new HashMap<>();
        params.put("selector", selector);
        return getResponseData(pizzaHandler.sendCommand("getValue", params));
    }

    @Override
    public void setValue(String selector, String value) {
        HashMap<String,Object> params = new HashMap<>();
        params.put("selector", selector);
        params.put("value", value);
        checkResponseForErrors(pizzaHandler.sendCommand("setValue", params));
    }

    @Override
    public String getInnerHTML(String selector) {
        HashMap<String,Object> params = new HashMap<>();
        params.put("selector", selector);
        return getResponseString(pizzaHandler.sendCommand("getInnerHTML", params));
    }

    @Override
    public String getInnerText(String selector) {
        HashMap<String,Object> params = new HashMap<>();
        params.put("selector", selector);
        return getResponseString(pizzaHandler.sendCommand("getInnerText", params));
    }

    @Override
    public void submit(String selector) {
        HashMap<String,Object> params = new HashMap<>();
        params.put("selector", selector);
        checkResponseForErrors(pizzaHandler.sendCommand("submit", params));
    }

    @Override
    public boolean exists(String selector) {
        HashMap<String,Object> params = new HashMap<>();
        params.put("selector", selector);
        JSONObject result = pizzaHandler.sendCommand("exists", params);
        return getResponseBoolean(result);
    }

    @Override
    public boolean isVisible(String selector) {
        HashMap<String,Object> params = new HashMap<>();
        params.put("selector", selector);
        JSONObject result = pizzaHandler.sendCommand("isVisible", params);
        return getResponseBoolean(result);
    }

    @Override
    public void verifyExists(String selector) {
        HashMap<String,Object> params = new HashMap<>();
        params.put("selector", selector);
        JSONObject result = pizzaHandler.sendCommand("verifyExists", params);
        if (!getResponseBoolean(result)) {
            throw new ScriptException(String.format("Unable to find element '%s'", selector));
        }
    }

    @Override
    public void verifyNotExists(String selector) {
        HashMap<String,Object> params = new HashMap<>();
        params.put("selector", selector);
        JSONObject result = pizzaHandler.sendCommand("verifyExists", params);
        if (getResponseBoolean(result)) {
            throw new ScriptException(String.format("Found element matching selector '%s'", selector));
        }
    }

    @Override
    public Object query(String selector) {
        HashMap<String,Object> params = new HashMap<>();
        params.put("selector", selector);
        params.put("visibleOnly", false);
        return getResponseData(pizzaHandler.sendCommand("query", params));
    }

    public Object queryVisible(String selector) {
        HashMap<String,Object> params = new HashMap<>();
        params.put("selector", selector);
        params.put("visibleOnly", true);
        return getResponseData(pizzaHandler.sendCommand("query", params));
    }

    @Override
    public void waitForElement(String selector) {
        HashMap<String,Object> params = new HashMap<>();
        params.put("selector", selector);
        checkResponseForErrors(pizzaHandler.sendCommand("waitForElement", params));
    }

    @Override
    public void waitForText(String selector, String text) {
        HashMap<String,Object> params = new HashMap<>();
        params.put("selector", selector);
        params.put("text", text);
        checkResponseForErrors(pizzaHandler.sendCommand("waitForText", params));
    }

    @Override
    public void waitForVisible(String selector) {
        HashMap<String,Object> params = new HashMap<>();
        params.put("selector", selector);
        checkResponseForErrors(pizzaHandler.sendCommand("waitForVisible", params));
    }

    @Override
    public void highlight(String selector) {
        HashMap<String,Object> params = new HashMap<>();
        params.put("selector", selector);
        checkResponseForErrors(pizzaHandler.sendCommand("highlight", params));
    }

    @Override
    public void highlight(String selector, NativeObject color) {
        HashMap<String,Object> params = new HashMap<>();
        params.put("selector", selector);
        params.put("color", RhinoUtils.deserialize(color));
        checkResponseForErrors(pizzaHandler.sendCommand("highlight", params));
    }

    @Override
    public void clearHighlight() {
        checkResponseForErrors(pizzaHandler.sendCommand("clearHighlight"));
    }

    @Override
    public void handleDialog(boolean accept) {
        HashMap<String,Object> params = new HashMap<>();
        params.put("accept", accept);
        checkResponseForErrors(pizzaHandler.sendCommand("handleDialog", params));
    }

    @Override
    public void handleDialog(String text) {
        HashMap<String,Object> params = new HashMap<>();
        params.put("promptText", text);
        checkResponseForErrors(pizzaHandler.sendCommand("handleDialog", params));
    }

    @Override
    public void dismissDialogs() {
        checkResponseForErrors(pizzaHandler.sendCommand("dismissDialogs"));
    }

    @Override
    public boolean isDialogOpen() {
        return getResponseBoolean(pizzaHandler.sendCommand("isDialogOpen"));
    }

    @Override
    public Object getOpenDialog() {
        return getResponseData(pizzaHandler.sendCommand("getOpenDialog"));
    }

    @Override
    public Object listCookies() {
        return getResponseData(pizzaHandler.sendCommand("listCookies"));
    }

    @Override
    public String getBrowserName() {
        return "Google Chrome";
    }

    @Override
    public String getBrowserVersion() {
        return pizzaHandler.getVersion();
    }

    @Override
    public Page back() {
        return checkNavigationSuccess(pizzaHandler.sendCommand("back"));
    }

    @Override
    public Page forward() {
        return checkNavigationSuccess(pizzaHandler.sendCommand("forward"));
    }

    @Override
    public Page reload() {
        return checkNavigationSuccess(pizzaHandler.sendCommand("reload"));
    }

    @Override
    public Data screenshot() {
        return screenshot("png");
    }

    @Override
    public Data screenshot(String format) {
        return screenshot(format, 1.0);
    }

    @Override
    public Data screenshot(String format, double quality) {
        if (!format.equals("webp") && !format.equals("png") && !format.equals("jpeg")) {
            throw new ScriptException("Screenshot format must be one of \"webp\", \"jpeg\" or \"png\"");
        }
        HashMap<String,Object> params = new HashMap<>();
        params.put("format", format);
        params.put("quality", quality);
        checkResponseForErrors(pizzaHandler.sendCommand("screenshot", params));
        ByteBuffer buffer = pizzaHandler.fetchScreenshot();
        return new Data(String.format("image/%s", format), buffer.array());
    }

    @Override
    public void verifyRequest(NativeRegExp regExp) {
        pizzaHandler.verifyRequest(regExp);
    }

    @Override
    public void verifyRequest(String url) {
        pizzaHandler.verifyRequest(url);
    }

    public void record() {
        checkResponseForErrors(pizzaHandler.sendCommand("record"));
    }

    @Override
    public String toString() {
        return "ChromeBrowser";
    }

    private boolean getResponseBoolean(JSONObject result) {
        try {
            JSONObject response = result.getJSONObject("response");
            if (response.has("error")) {
                throw new ScriptException(ErrorType.Script, response.getString("error"));
            }
            return response.getBoolean("value");
        } catch (JSONException e) {
            throw new ScriptException(ErrorType.Internal, "Unable to parse command response.");
        }
    }

    private String getResponseString(JSONObject result) {
        try {
            JSONObject response = result.getJSONObject("response");
            if (response.has("error")) {
                throw new ScriptException(ErrorType.Script, response.getString("error"));
            }
            return response.getString("value");
        } catch (JSONException e) {
            throw new ScriptException(ErrorType.Internal, "Unable to parse command response.");
        }
    }

    private Object getResponseData(JSONObject result) {
        try {
            JSONObject response = result.getJSONObject("response");
            if (response.has("value") && !response.isNull("value")) {
                return RhinoUtils.serialize(response.opt("value"));
            } else if (response.has("description") && !response.isNull("description")) {
                return RhinoUtils.serialize(response.opt("description"));
            } else if (response.has("error")) {
                throw new ScriptException(ErrorType.Script, response.getString("error"));
            } else {
                return null;
            }
        } catch (JSONException e) {
            throw new ScriptException(ErrorType.Internal, "Unable to parse command response.");
        }
    }

    private void checkResponseForErrors(JSONObject result) {
        try {
            if (result.has("response")) {
                JSONObject response = result.getJSONObject("response");
                if (response.has("error") && !response.isNull("error")) {
                    String errorMsg = null;
                    JSONObject error = response.optJSONObject("error");
                    if (error == null) {
                        errorMsg = response.optString("error");
                    } else {
                        errorMsg = error.optString("message");
                    }
                    if (errorMsg != null) {
                        throw new ScriptException(ErrorType.Script, errorMsg);
                    } else {
                        throw new ScriptException(ErrorType.Script, error.toString());
                    }
                }
            }
        } catch (JSONException e) {
            throw new ScriptException(ErrorType.Internal, "Unable to parse command response.");
        }
    }
}
