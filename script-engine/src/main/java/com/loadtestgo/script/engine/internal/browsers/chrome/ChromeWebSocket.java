package com.loadtestgo.script.engine.internal.browsers.chrome;

import com.bric.qt.io.JPEGMovWriter;
import com.loadtestgo.script.api.*;
import com.loadtestgo.script.engine.ResultsNotifier;
import com.loadtestgo.script.engine.ScriptException;
import com.loadtestgo.script.engine.TestContext;
import com.loadtestgo.script.engine.internal.server.BrowserWebSocket;
import com.loadtestgo.util.HttpHeader;
import com.loadtestgo.util.Http;
import com.loadtestgo.util.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.mozilla.javascript.regexp.NativeRegExp;
import org.pmw.tinylog.Logger;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChromeWebSocket extends BrowserWebSocket {
    private TestResult testResult;
    private TestContext testContext;
    private ResultsNotifier resultNotifier;
    private String authRequestId;

    // Requests that we've decided are chrome internal requests and shouldn't be logged
    private Set<String> internalRequestIds;

    private Map<String, HttpRequest> ongoingRequests = new HashMap<>();
    private Map<String, JSONObject> deferredRequestExtraInfo = new HashMap<>();
    private Map<String, JSONObject> deferredResponseExtraInfo = new HashMap<>();

    private JPEGMovWriter videoWriter;
    private byte[] previousImage;
    private long previousFrameTime;
    private boolean capturingVideo;
    private Object videoLock = new Object();

    // These events are not written out as they make the verbose logs too busy
    static List<String> DONT_LOG_THESE_EVENTS = Arrays.asList("Page.screencastFrame", "Network.dataReceived");

    public ChromeWebSocket(TestContext testContext) {
        super(testContext);
        this.testContext = testContext;
        this.testResult = testContext.getTestResult();
        this.resultNotifier = testContext.getResultNotifier();
        this.internalRequestIds = new HashSet<>();
    }

    public void reset() {
        this.testResult = null;
        this.resultNotifier = null;

        closeVideo();
    }

    public void reset(TestContext testContext) {
        this.testContext = testContext;
        this.testResult = testContext.getTestResult();
        this.resultNotifier = testContext.getResultNotifier();
        this.internalRequestIds = new HashSet<>();

        closeVideo();
    }

    public void close() {
        closeVideo();
    }

    private void closeVideo() {
        synchronized (videoLock) {
            capturingVideo = false;
            if (videoWriter != null) {
                try {
                    writeLastFrame();
                    videoWriter.close();
                    testContext.addFile(videoWriter.getFile().getAbsoluteFile());
                } catch (IOException e) {
                    Logger.error("Unable to save video", e);
                }
                videoWriter = null;
            }
        }
    }

    @Override
    protected boolean onMessage(JSONObject json) throws JSONException {
        synchronized (this) {
            // Too late if the conn is closed
            if (conn == null) {
                return false;
            }

            if (super.onMessage(json)) {
                return true;
            }

            if (testResult == null) {
                return true;
            }

            String event = json.getString("event");
            JSONObject details = json.getJSONObject("details");

            if (verboseLogging) {
                if (!DONT_LOG_THESE_EVENTS.contains(event)) {
                    Logger.info("{} {}", event, details.toString());
                }
            }

            switch (event) {
                case "navigationBegin":
                    navigationBegin(details);
                    break;
                case "navigationCommitted":
                    navigationCommitted(details);
                    break;
                case "Network.requestWillBeSent":
                    networkRequestWillBeSent(details);
                    break;
                case "Network.requestWillBeSentExtraInfo":
                    networkRequestWillBeSentExtraInfo(details);
                    break;
                case "Network.dataReceived":
                    networkDataReceived(details);
                    break;
                case "Network.responseReceived":
                    networkResponseReceived(details);
                    break;
                case "Network.responseReceivedExtraInfo":
                    networkResponseReceivedExtraInfo(details);
                    break;
                case "Network.loadingFinished":
                    networkLoadingFinished(details);
                    break;
                case "Network.loadingFailed":
                    networkLoadingFailed(details);
                    break;
                case "Network.resourceChangedPriority":
                    networkResourceChangedPriority(details);
                    break;
                case "Network.requestServedFromCache":
                    networkRequestServedFromCache(details);
                    break;
                case "Network.webSocketCreated":
                    networkWebSocketCreated(details);
                    break;
                case "Network.webSocketWillSendHandshakeRequest":
                    networkWebSocketWillSendHandshakeRequest(details);
                    break;
                case "Network.webSocketHandshakeResponseReceived":
                    networkWebSocketHandshakeResponseReceived(details);
                    break;
                case "Network.webSocketFrameSent":
                    networkWebSocketFrameSent(details);
                    break;
                case "Network.webSocketFrameReceived":
                    networkWebSocketFrameReceived(details);
                    break;
                case "Network.webSocketFrameError":
                    networkWebSocketFrameError(details);
                    break;
                case "Network.webSocketClosed":
                    networkWebSocketClosed(details);
                    break;
                case "navigationDOMContentLoaded":
                    navigationDOMContentLoaded(details);
                    break;
                case "historyStateUpdated":
                    historyStateUpdated(details);
                    break;
                case "navigationCompleted":
                    navigationCompleted(details);
                    break;
                case "navigationError":
                    navigationError(details);
                    break;
                case "navigationLoadTimes":
                    navigationLoadTimes(details);
                    break;
                case "onAuthRequired":
                    onAuthRequired(details);
                    break;
                case "tabCreated":
                    tabCreated(details);
                    break;
                case "tabUpdated":
                    tabUpdated(details);
                    break;
                case "tabRemoved":
                    tabRemoved(details);
                    break;
                case "debuggerDetached":
                    debuggerDetached(details);
                    break;
                case "Console.messagesCleared":
                    consoleMessagesCleared(details);
                    break;
                case "Console.messageAdded":
                    consoleMessagesAdded(details);
                    break;
                case "Console.messageRepeatCountUpdated":
                    messageRepeatCountUpdated(details);
                    break;
                case "Page.screencastFrame":
                    screencastFrame(details);
                    break;
                case "Pizza.inspectElement":
                    inspectElement(details);
                    break;
                case "Inspector.targetCrashed":
                    Logger.error("tab crashed: {}", event, details.toString());
                    break;
                case "calculatedPageStats":
                    calculatedPageStats(details);
                    break;
                default:
                    Logger.error("unhandled: {} {}", event, details.toString());
                    return false;
            }

            return true;
        }
    }

    private void tabUpdated(JSONObject details) {
        // Tab moved, changed display, was activated, etc
    }

    private void debuggerDetached(JSONObject details) {
        // Same as tabRemoved essentially
    }

    private void tabRemoved(JSONObject details) {
        int tabId = -1;
        if (details.has("tabId")) {
            tabId = details.optInt("tabId");
        }
        if (tabId == -1) {
            return;
        }
        // Tab is removed cancel all ongoing requests for that tab, we've no way of getting information
        // about these requests - however once the tab is closed, the connections are also closed.
        synchronized(testResult) {
            List<String> requestsToRemove = new ArrayList<>();
            for (Map.Entry<String, HttpRequest> requestEntry : ongoingRequests.entrySet()) {
                HttpRequest request = requestEntry.getValue();
                if (request.getTabId() == tabId) {
                    request.setError("Cancelled");
                    request.setState(HttpRequest.State.Complete);
                    requestsToRemove.add(requestEntry.getKey());
                }
            }

            for (String requestId : requestsToRemove) {
                ongoingRequests.remove(requestId);
            }
        }
    }

    private void screencastFrame(JSONObject details) {
        String data = details.getString("data");

        synchronized (videoLock) {
            if (capturingVideo) {
                JSONObject metadata = details.getJSONObject("metadata");
                double timestamp = metadata.getDouble("timestamp");

                long frameTime = convertToMillisFromSeconds(timestamp);

                Base64.Decoder decoder = Base64.getDecoder();

                byte[] rawData = decoder.decode(data);

                try {
                    if (videoWriter == null) {
                        String filePath = testContext.getVideoFilePath();
                        File file = null;
                        if (filePath == null) {
                            String baseName = testContext.getTestName();
                            if (baseName == null) {
                                baseName = "video";
                            }
                            file = new File(testContext.getOutputDirectory(), baseName + ".mov");
                        } else {
                            file = new File(filePath);
                        }
                        Logger.info("Saving video to {}", file);
                        videoWriter = new JPEGMovWriter(file);
                    }

                    if (previousImage != null) {
                        int diff = (int) (frameTime - this.previousFrameTime);
                        if (diff <= 0) {
                            diff = 1;
                        }
                        videoWriter.addFrame(diff, previousImage);
                    }

                    previousImage = rawData;
                    previousFrameTime = frameTime;
                } catch (IOException e) {
                    Logger.error("unable to add frame to video", e);
                }
            }
        }
        int sessionId = details.getInt("sessionId");
        sendMessage(String.format("{ \"name\": \"screencastAck\", \"id\": null, \"type\": \"pizza\"," +
            "\"params\": {\"sessionId\": %d} }", sessionId));
    }

    private void writeLastFrame() {
        try {
            if (videoWriter != null && previousImage != null) {
                long timestamp = System.currentTimeMillis();
                int diff = (int) (timestamp - this.previousFrameTime);
                if (diff <= 0) {
                    diff = 1;
                }
                videoWriter.addFrame(diff, previousImage);
            }

            previousImage = null;
            previousFrameTime = 0;
        } catch (IOException e) {
            Logger.error("Problem writing last frame of video", e);
        }
    }

    private void networkResourceChangedPriority(JSONObject details) {
        // Nothing for now
    }

    private void inspectElement(JSONObject details) {
        if (resultNotifier != null) {
            resultNotifier.onInspectElement(details);
        }
    }

    private void tabCreated(JSONObject details) {
        // Dummy for now, the openerTabId is interesting...
    }

    private void onAuthRequired(JSONObject details) throws JSONException {
        double endTime = details.getDouble("timeStamp");

        HttpRequest request = getHttpRequestByUrl(details);
        if (request == null) {
            request = new HttpRequest();

            int tabId = details.getInt("tabId");
            request.setTabId(tabId);

            String url = details.getString("url");
            request.parseUrl(url);

            request.setMethod(details.getString("method"));

            request.setStartTime((long)endTime);

            String requestId = details.getString("requestId");

            Page page = getCurrentPageForTab(tabId);
            if (page == null) {
                Logger.error("Unable to find page for tab: {}", tabId);
                return;
            }

            synchronized (testResult) {
                authRequestId = requestId;
                page.addRequest(request);
                ongoingRequests.put(requestId, request);
            }
        } else {
            request.setRecvEnd((int)((long)endTime - request.getStartTime()));
            request.setState(HttpRequest.State.Complete);
        }

        request.setResourceType(convertResourceType(details.getString("type")));

        // Fill out the details we have, these value will be overridden if the creds are supplied.
        // Unfortunately devtools only provides one request per request/auth response/auth request/response
        // cycle, really two requests happen.
        String statusLine = details.getString("statusLine");
        Http.Response response = Http.parseResponse(statusLine);
        request.setProtocol(response.httpVersion);
        request.setStatusCode(response.statusCode);
        request.setStatusText(response.statusText);
    }

    public Page newPage() {
        return newPage(null);
    }

    public Page newPage(String pageName) {
        synchronized (testResult) {
            Page page = new Page();
            if (pageName != null) {
                page.setName(pageName);
            }
            if (getPages().size() == 0) {
                addPage(page);
            } else {
                Page current = getCurrentPage();
                page.setFrameId(current.getFrameId());
                page.setProcessId(current.getProcessId());
                page.setTabId(current.getTabId());
                page.setUrl(current.getUrl());
                addPage(page);
            }
            return page;
        }
    }

    public boolean checkPendingRequests() {
        synchronized (testResult) {
            Page page = getCurrentPage();
            if (page == null) {
                return false;
            }

            for (HttpRequest request : page.getRequests()) {
                if (checkIsRequestPending(request)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean checkIsRequestPending(HttpRequest httpRequest) {
        synchronized (testResult) {
            HttpRequest.State state = httpRequest.getState();
            if (httpRequest.isWebSocket()) {
                // Once a websocket connection is established don't wait on it,
                // it may be kept open for the lifetime of the page.
                if (state != HttpRequest.State.Recv &&
                    state != HttpRequest.State.Complete) {
                    return true;
                }
            } else {
                if (state != HttpRequest.State.Complete) {
                    return true;
                }
            }
        }
        return false;
    }

    public long getLastRequestTime() {
        Page page = getCurrentPage();
        if (page == null) {
            return -1;
        }

        long lastTime = -1;
        synchronized (testResult) {
            for (HttpRequest request : page.getRequests()) {
                if (request.getState() == HttpRequest.State.Complete) {
                    if (request.getEndTime() > lastTime) {
                        lastTime = request.getEndTime();
                    }
                }
            }
        }

        return lastTime;
    }

    private Page getUnitializedPage() {
        synchronized (testResult) {
            for (int i = getPages().size() - 1; i >= 0; --i) {
                Page page = getPages().get(i);
                if (page.getState() == Page.State.Uninitialised) {
                    return page;
                }
            }
            return null;
        }
    }

    public void verifyRequest(NativeRegExp regExp) {
        if (regExp == null) {
            throw new ScriptException("regex null");
        }

        String regexp = regExp.toString();
        int start = 0;
        int end = 0;
        if (regexp.startsWith("/")) {
            start = 1;
        }

        if (regexp.endsWith("/")) {
            end = regexp.length() - 1;
        }

        regexp = regexp.substring(start, end);
        Pattern p = Pattern.compile(regexp);
        boolean found = false;
        synchronized (testResult) {
            Page page = getCurrentPage();
            if (page == null) {
                throw new ScriptException("No page found");
            }

            for (HttpRequest request : page.requests) {
                String url = request.getUrl();
                if (url != null) {
                    Matcher m = p.matcher(url);
                    if (m.find()) {
                        verifyRequest(url, request);
                        found = true;
                    }
                }
            }
        }

        if (!found) {
            throw new ScriptException(String.format("Unable to find request matching '%s'",
                regexp));
        }
    }

    public void verifyRequest(String url) {
        boolean found = false;
        synchronized (testResult) {
            if (url == null) {
                throw new ScriptException("No URL specified");
            }
            Page page = getCurrentPage();
            if (page == null) {
                throw new ScriptException("No page found");
            }
            for (HttpRequest request : page.requests) {
                if (url.equals(request.getUrl())) {
                    verifyRequest(url, request);
                    found = true;
                    break;
                }
            }
        }

        if (!found) {
            throw new ScriptException(String.format("Unable to find request matching '%s'", url));
        }
    }

    private void verifyRequest(String url, HttpRequest request) {
        if (request.error != null) {
            throw new ScriptException(String.format("%s: %s", url, request.error));
        }

        int statusCode = request.getStatusCode();
        if (statusCode >= 400 && statusCode <= 600) {
            throw new ScriptException(String.format("%s: HTTP status '%d %s'",
                    url, request.getStatusCode(), request.getStatusText()));
        }

        if (request.isWebSocket()) {
            if (request.state != HttpRequest.State.Recv &&
                request.state != HttpRequest.State.Complete) {
                throw new ScriptException(
                    String.format("%s: websocket not initialized (%s)", url, request.state));
            }
        } else {
            if (request.state != HttpRequest.State.Complete) {
                throw new ScriptException(
                    String.format("%s: request not completed (%s)", url, request.state));
            }
        }
    }

    public void startVideoCapture() {
        synchronized (videoLock) {
            capturingVideo = true;
        }
        sendCommand("startVideoCapture");
    }

    public void stopVideoCapture() {
        sendCommand("stopVideoCapture");
        synchronized (videoLock) {
            capturingVideo = false;
        }
    }

    class FrameInfo {
        int processId;
        int tabId;
        int frameId;
        int parentFrameId;

        FrameInfo() {
            this.processId = -1;
            this.tabId = -1;
            this.frameId = -1;
            this.parentFrameId = -1;
        }
    }

    private FrameInfo getNavFrameInfo(JSONObject details) throws JSONException {
        FrameInfo frameInfo = new FrameInfo();
        frameInfo.tabId = details.getInt("tabId");
        frameInfo.processId = details.getInt("processId");
        frameInfo.frameId = details.getInt("frameId");
        if (details.has("parentFrameId")) {
            frameInfo.parentFrameId = details.getInt("parentFrameId");
        }
        return frameInfo;
    }

    class RequestInfo {
        int processId;
        int tabId;
        String frameId;
        String requestId;

        RequestInfo() {
            this.processId = -1;
            this.tabId = -1;
            this.frameId = null;
            this.requestId = null;
        }

        String getRequestId() {
            return String.format("%d:%s", tabId, requestId);
        }
    }

    RequestInfo getDevToolsRequestInfo(JSONObject details) throws JSONException {
        RequestInfo requestInfo = new RequestInfo();
        requestInfo.tabId = details.getInt("tabId");
        if (details.has("frameId")) {
            String frameId = details.getString("frameId");
            int dotPos = frameId.indexOf(".");
            if (dotPos > 0) {
                // Old style was "x.y" where x was the process id and y was the frame id (unique only per process)
                String processId = frameId.substring(0, dotPos);
                String shortFrameId = frameId.substring(dotPos + 1);
                requestInfo.frameId = shortFrameId;
                requestInfo.processId = Integer.valueOf(processId);
            } else {
                // New style - process id is not available and string is long
                requestInfo.frameId = frameId;
            }
        }

        String longRequestId = details.getString("requestId");
        int dotPos = longRequestId.indexOf(".");
        if (dotPos > 0) {
            // Old style, < chrome 60 ?
            requestInfo.requestId = longRequestId;
            String processId = longRequestId.substring(0, dotPos);
            try {
                requestInfo.processId = Integer.valueOf(processId);
            } catch (NumberFormatException e) {
                Logger.error("Unable to parse processId {} as int", processId);
            }
        } else {
            // New style, > chrome 70 ?
            requestInfo.requestId = longRequestId;
            String processId = details.optString("processId", null);
            if (StringUtils.isSet(processId)) {
                try {
                    requestInfo.processId = Integer.valueOf(processId);
                } catch (NumberFormatException e) {
                    Logger.error("Unable to parse processId {} as int", processId);
                }
            }
        }
        return requestInfo;
    }

    private HttpRequest getHttpRequestByUrl(JSONObject details) throws JSONException {
        String url = details.getString("url");
        int tabId = details.getInt("tabId");
        Page page = getCurrentPageForTab(tabId);
        if (page == null) {
            return null;
        }
        ArrayList<HttpRequest> requests = page.getRequests();
        for (int i = 0; i < requests.size(); ++i) {
            HttpRequest request = requests.get(i);
            String requestUrl = request.getUrl();
            if (StringUtils.isSet(requestUrl) && requestUrl.equals(url)) {
                return request;
            }
        }
        return null;
    }

    private void networkRequestWillBeSent(JSONObject details) throws JSONException {
        JSONObject requestObj = details.getJSONObject("request");

        RequestInfo requestInfo = getDevToolsRequestInfo(details);
        Page page = getPageForRequest(requestInfo);
        if (page == null) {
            Logger.error("Unable to find frame for process: {} tab: {} frame: {}",
                    requestInfo.processId, requestInfo.tabId, requestInfo.frameId);
            return;
        }

        String url = requestObj.getString("url");
        if (isInternalUrl(url)) {
            addInternalRequestId(requestInfo);
            return;
        }

        if (details.has("redirectResponse")) {
            JSONObject redirectResponse = details.getJSONObject("redirectResponse");
            HttpRequest redirect = getRequestForTab(requestInfo);
            if (redirect != null) {
                finishResponse(requestInfo, redirect, redirectResponse);
                redirect.setRedirectUrl(url);
                redirect.setRecvEnd(redirect.getRecvHeadersEnd());
                redirect.setState(HttpRequest.State.Complete);
                removeOngoingRequest(requestInfo);
            }
        }

        HttpRequest request;
        boolean authRequestAlreadyMade = false;

        synchronized (testResult) {
            // Handle the case where the auth request is received before 'requestWillBeSent'
            if (authRequestId != null) {
                request = ongoingRequests.remove(authRequestId);
                authRequestId = null;
                authRequestAlreadyMade = true;
            } else {
                request = new HttpRequest();
            }
        }

        request.setTabId(requestInfo.tabId);
        request.setFrameId(requestInfo.frameId);
        request.setRequestId(requestInfo.requestId);

        request.parseUrl(url);
        request.setMethod(requestObj.getString("method"));

        if (authRequestAlreadyMade) {
            long endTime = request.getStartTime();
            setRequestStartTime(details, request);
            request.setRecvEnd((int)(endTime - request.getStartTime()));
            request.setState(HttpRequest.State.Complete);
        } else {
            request.setState(HttpRequest.State.Send);
            setRequestStartTime(details, request);
        }

        if (requestObj.has("postData")) {
            request.setPostData(requestObj.getString("postData"));
        }

        setRequestHeaders(requestObj, request);

        JSONObject initiator = details.optJSONObject("initiator");
        if (initiator != null) {
            String initiatorUrl = initiator.optString("url");
            if (initiatorUrl != null && !initiatorUrl.isEmpty()) {
                request.setInitiatorUrl(initiatorUrl);
            } else {
                JSONArray stackTrace = initiator.optJSONArray("stackTrace");
                if (stackTrace != null) {
                    JSONObject obj = stackTrace.optJSONObject(0);
                    if (obj != null) {
                        initiatorUrl = obj.optString("url");
                        if (initiatorUrl != null && !initiatorUrl.isEmpty()) {
                            request.setInitiatorUrl(initiatorUrl);
                        }
                    }
                }
            }
        }

        JSONObject deferredDetails = deferredRequestExtraInfo.get(request.requestId);
        if (deferredDetails != null) {
            setRequestHeaders(deferredDetails, request);
            deferredRequestExtraInfo.remove(request.requestId);
        }

        synchronized (testResult) {
            page.addRequest(request);
            ongoingRequests.put(requestInfo.getRequestId(), request);
        }
    }

    private void networkRequestWillBeSentExtraInfo(JSONObject details) {
        RequestInfo requestInfo = getDevToolsRequestInfo(details);

        if (isInternalRequestId(requestInfo)) {
            return;
        }

        HttpRequest request = getRequestForTab(requestInfo);
        if (request == null) {
            String requestId = details.getString("requestId");
            if (!StringUtils.isEmpty(requestId)) {
                deferredRequestExtraInfo.put(requestId, details);
            }
            return;
        }

        setRequestHeaders(details, request);
    }

    private void finishResponse(RequestInfo requestInfo, HttpRequest request, JSONObject responseObj) throws JSONException {
        // If the raw headers exist parse those
        if (responseObj.has("requestHeadersText")) {
            String headersText = responseObj.getString("requestHeadersText");
            Http.Request headerInfo = Http.parseRequest(responseObj.getString("requestHeadersText"));
            request.setRequestHeaders(headerInfo.headers);
            request.setRequestHeadersSize(headersText.getBytes().length);
        } else if (responseObj.has("requestHeaders")) {
            // Headers are not always available in raw form, fallback to
            // grabbing the ones parsed by Chrome.
            JSONObject headersObj = responseObj.getJSONObject("requestHeaders");
            ArrayList<HttpHeader> headers = getHeadersFromJson(headersObj);
            request.setRequestHeaders(headers);

            // Calc response headers len
            int size = getHeadersSize(headers);
            size += request.getMethod().getBytes().length;
            size += 1; // " "
            size += request.getUrl().getBytes().length;
            size += 11; // " HTTP/1.1\r\n"
            request.setRequestHeadersSize(size);
        }

        setResponseHeaders(request, responseObj);

        request.setConnectionReused(responseObj.getBoolean("connectionReused"));
        request.setConnectionId(responseObj.getInt("connectionId"));
        request.setState(HttpRequest.State.Recv);
        request.setMimeType(responseObj.getString("mimeType"));
        if (responseObj.has("remoteIPAddress")) {
            request.setIp(responseObj.getString("remoteIPAddress"));
        }

        request.setProtocol(responseObj.optString("protocol"));

        if (responseObj.getBoolean("fromDiskCache")) {
            request.setFromCache(true);
        }

        if (!request.isFromCache()) {
            if (responseObj.has("timing")) {
                JSONObject timing = responseObj.getJSONObject("timing");
                long startTime = convertToMillisFromSeconds(timing.getDouble("requestTime"));
                startTime += request.getWallTimeOffset();
                request.setStartTime(startTime);

                updateRequestPosition(request);

                request.setDnsStart((int) timing.getDouble("dnsStart"));
                request.setDnsEnd((int) timing.getDouble("dnsEnd"));
                request.setSslStart((int) timing.getDouble("sslStart"));
                request.setSslEnd((int) timing.getDouble("sslEnd"));
                request.setConnectEnd((int) timing.getDouble("connectEnd"));
                request.setConnectStart((int) timing.getDouble("connectStart"));
                request.setConnectEnd((int) timing.getDouble("connectEnd"));
                request.setSendStart((int) timing.getDouble("sendStart"));
                request.setSendEnd((int) timing.getDouble("sendEnd"));
                request.setRecvHeadersEnd((int) timing.getDouble("receiveHeadersEnd"));
                padBlockedTime(request);
            }
        }
    }

    private void setResponseHeaders(HttpRequest request, JSONObject responseObj) {
        if (responseObj.has("headersText")) {
            processHeadersText(request, responseObj);
        } else {
            // Headers are not always available in raw form, fallback to
            // grabbing the ones parsed by Chrome
            JSONObject headersObj = responseObj.getJSONObject("headers");
            ArrayList<HttpHeader> headers = getHeadersFromJson(headersObj);
            request.setResponseHeaders(headers);

            String statusText = responseObj.getString("statusText");
            int statusCode = responseObj.getInt("status");
            request.setStatusText(statusText);
            request.setStatusCode(statusCode);

            // Calc response headers len
            int size = getHeadersSize(headers);
            size += 9; // "HTTP/1.1 "
            size += Math.log10(statusCode);
            size += 1; // " "
            size += statusText.getBytes().length;
            size += 2; // "\r\n"
            request.setResponseHeadersSize(size);
        }
    }

    private void processHeadersText(HttpRequest request, JSONObject responseObj) {
        String headersText = responseObj.getString("headersText");
        Http.Response responseInfo = Http.parseResponse(headersText);
        request.setResponseHeaders(responseInfo.headers);
        request.setProtocol(responseInfo.httpVersion); // Note the protocol may be overwritten later
        // if we get the value directly from chrome
        request.setStatusCode(responseInfo.statusCode);
        request.setStatusText(responseInfo.statusText);
        request.setResponseHeadersSize(headersText.getBytes().length);
    }

    private void networkWebSocketCreated(JSONObject details) {
        RequestInfo requestInfo = getDevToolsRequestInfo(details);
        Page page = getPageForRequest(requestInfo);
        if (page == null) {
            Logger.error("Unable to find frame for process: {} tab: {} frame: {}",
                requestInfo.processId, requestInfo.tabId, requestInfo.frameId);
            return;
        }

        String url = details.getString("url");
        if (isInternalUrl(url)) {
            addInternalRequestId(requestInfo);
            return;
        }

        HttpRequest request = new HttpRequest();
        request.setTabId(requestInfo.tabId);
        request.setFrameId(requestInfo.frameId);
        request.setRequestId(requestInfo.requestId);

        request.parseUrl(url);
        request.setMethod("GET");
        request.setState(HttpRequest.State.Send);
        request.setIsWebSocket();

        request.setStartTime(System.currentTimeMillis());

        synchronized (testResult) {
            page.addRequest(request);
            ongoingRequests.put(requestInfo.getRequestId(), request);
        }
    }

    private void setRequestStartTime(JSONObject details, HttpRequest request) {
        if (details.has("wallTime")) {
            double wallTime = details.getDouble("wallTime");
            double timestamp = details.getDouble("timestamp");
            long offset = convertToMillisFromSeconds(wallTime - timestamp);
            request.setWallTimeOffset(offset);
            request.setStartTime(convertToMillisFromSeconds(wallTime));
        } else {
            request.setStartTime(convertToMillisFromSeconds(details.getDouble("timestamp")));
        }
    }

    private void networkWebSocketWillSendHandshakeRequest(JSONObject details) {
        JSONObject requestObj = details.getJSONObject("request");
        RequestInfo requestInfo = getDevToolsRequestInfo(details);
        HttpRequest request = getRequestForTab(requestInfo);

        setRequestStartTime(details, request);
        updateRequestPosition(request);
        setRequestHeaders(requestObj, request);

        request.setSendStart(0);
        request.setSendEnd(0);
    }

    private void setRequestHeaders(JSONObject requestObj, HttpRequest request) {
        ArrayList<HttpHeader> headers = new ArrayList<>();
        JSONObject jsonHeaders = requestObj.getJSONObject("headers");
        for (Iterator keys = jsonHeaders.keys(); keys.hasNext();) {
            Object key =  keys.next();
            String name = key.toString();
            String value = jsonHeaders.getString(name);
            headers.add(new HttpHeader(name, value));
        }
        request.setRequestHeaders(headers);

        // Calc request headers len
        int size = getHeadersSize(headers);

        // Guard against the method not being filled out - sometimes happens with Chrome 77
        if (request.getMethod() != null) {
            size += request.getMethod().getBytes().length;
            size += 1; // " "
            if (request.getUrl() != null) {
                size += request.getUrl().getBytes().length;
                size += 11; // " HTTP/1.1\r\n"
            }
        }
        request.setRequestHeadersSize(size);
    }

    private void networkWebSocketHandshakeResponseReceived(JSONObject details) {
        JSONObject responseObj = details.getJSONObject("response");
        RequestInfo requestInfo = getDevToolsRequestInfo(details);
        HttpRequest request = getRequestForTab(requestInfo);

        long timestamp = (long)(details.getDouble("timestamp") * 1000);
        request.setRecvHeadersEnd((int) (timestamp -
            (request.getStartTime() - request.getWallTimeOffset())));

        request.setState(HttpRequest.State.Recv);

        setResponseHeaders(request, responseObj);
    }

    private void networkWebSocketFrameSent(JSONObject details) {
        RequestInfo requestInfo = getDevToolsRequestInfo(details);
        HttpRequest request = getRequestForTab(requestInfo);
        JSONObject response = details.getJSONObject("response");
        WebSocketMessage message = createWebSocketMessage(details, request, response);
        message.flow = WebSocketMessage.Flow.Sent;
        if (resultNotifier != null) {
            resultNotifier.onWebSocketFrame(request, message);
        }
        request.addWebSocketMessage(message);
        request.addBytesSent(message.len);
        request.setRecvEnd(message.time);
    }

    private void networkWebSocketFrameReceived(JSONObject details) {
        RequestInfo requestInfo = getDevToolsRequestInfo(details);
        HttpRequest request = getRequestForTab(requestInfo);
        JSONObject response = details.getJSONObject("response");
        WebSocketMessage message = createWebSocketMessage(details, request, response);
        message.flow = WebSocketMessage.Flow.Received;
        if (resultNotifier != null) {
            resultNotifier.onWebSocketFrame(request, message);
        }
        request.addWebSocketMessage(message);
        request.addBytesRecv(message.len, 0);
        request.setRecvEnd(message.time);
    }

    private void networkWebSocketFrameError(JSONObject details) {
        RequestInfo requestInfo = getDevToolsRequestInfo(details);
        HttpRequest request = getRequestForTab(requestInfo);
        if (details.has("errorMessage")) {
            request.setError(details.getString("errorMessage"));
        }
    }

    private WebSocketMessage createWebSocketMessage(JSONObject details, HttpRequest request, JSONObject msg) {
        WebSocketMessage message = new WebSocketMessage();

        long timestamp = request.getWallTimeOffset() +
            convertToMillisFromSeconds(details.getDouble("timestamp"));

        message.time = (int)(timestamp - request.getStartTime());
        message.data = msg.getString("payloadData");
        if (message.data != null) {
            message.len = message.data.length();
            if (message.len > 100) {
                message.data = message.data.substring(0, 100);
            }
        }
        return message;
    }

    private void networkWebSocketClosed(JSONObject details) {
        RequestInfo requestInfo = getDevToolsRequestInfo(details);
        HttpRequest request = getRequestForTab(requestInfo);

        request.setState(HttpRequest.State.Complete);
        long timestamp = request.getWallTimeOffset() +
            convertToMillisFromSeconds(details.getDouble("timestamp"));
        request.setRecvEnd((int) (timestamp - request.getStartTime()));

        removeOngoingRequest(requestInfo);
    }

    private void updateRequestPosition(HttpRequest request) {
        // Update the position in the list of requests so that is sorted by
        // startTime
        synchronized (testResult) {
            Page page = getPageForExistingRequest(request);
            if (page != null) {
                updateRequestPosition(page, request);
            }
        }
    }

    private void updateRequestPosition(Page page, HttpRequest request) {
        ArrayList<HttpRequest> requests = page.getRequests();

        // Keep the list in order of start time
        int pos = requests.indexOf(request);
        if (pos < 0) {
            return;
        }
        int newPos = pos;
        for (int i = requests.size(); i > 0; --i) {
            HttpRequest r = requests.get(i - 1);
            newPos = i - 1;
            if (request.getStartTime() >= r.getStartTime()) {
                break;
            }
        }
        if (pos != newPos) {
            requests.remove(pos);
            requests.add(newPos, request);
        }
    }

    private Page getPageForExistingRequest(HttpRequest request) {
        for (Page page : getPages()) {
            if (page.getRequests().contains(request)) {
                return page;
            }
        }
        return null;
    }

    private void padBlockedTime(HttpRequest request) {
        // Sometimes Chrome has to wait before sending a request.  From tcpdumps we've
        // noticed this happen when:
        //
        // 1. A DNS request for the host ip is ongoing and is not finished yet.
        //    Speculative DNS requests can be issued by Chrome and this is not part of
        //    request as far as devtools is concerned.
        // 2. A connection is currently being used for a different request.  Only six
        //    connections can be used per host, and 30-60 overall at any one time.
        //
        // Often the blocked reason is not immediately obvious from the HAR data, until the
        // tcpdump is looked at.
        if (request.getDnsStart() == -1) {
            if (request.getConnectStart() == -1) {
                if (request.getSendStart() > 0) {
                    request.setBlockedTime(request.getSendStart());
                }
            } else if (request.getConnectStart() > 0) {
                request.setBlockedTime(request.getConnectStart());
            }
        } else if (request.getDnsStart() > 0) {
            request.setBlockedTime(request.getDnsStart());
        }
    }

    private int getHeadersSize(ArrayList<HttpHeader> headers) {
        int size = 0;
        for (HttpHeader header : headers) {
            size += header.name.getBytes().length;
            size += 3; // " : "
            size += header.value.getBytes().length;
            size += 2; // "\r\n"
        }
        size += 2; // "\r\n"
        return size;
    }

    private ArrayList<HttpHeader> getHeadersFromJson(JSONObject headersObj) throws JSONException {
        Iterator iterator = headersObj.keys();
        ArrayList<HttpHeader> headers = new ArrayList<>();
        while (iterator.hasNext()) {
            String key = (String)iterator.next();
            String value = headersObj.getString(key);
            headers.add(new HttpHeader(key, value));
        }
        return headers;
    }

    private void networkResponseReceived(JSONObject details) throws JSONException {
        JSONObject responseObj = details.getJSONObject("response");
        RequestInfo requestInfo = getDevToolsRequestInfo(details);
        if (isInternalUrl(responseObj.getString("url"))) {
            addInternalRequestId(requestInfo);
            return;
        }
        if (isInternalRequestId(requestInfo)) {
            return;
        }

        HttpRequest request = getRequestForTab(requestInfo);
        if (request == null) {
            JSONObject deferredDetails = deferredRequestExtraInfo.get(requestInfo.requestId);
            if (deferredDetails == null) {
                Logger.error("networkResponseReceived event, but no request found: {}", responseObj.getString("url"));
                return;
            } else {
                // We have a deferred request but no actual request, create one.
                // This can happen when opening a new tab, and we miss the initial network request because
                // it takes time to attach.  Ideally Chrome would queue these up and then send to us but that is not
                // happening.
                request = addNewRequestFromDefer(responseObj, requestInfo);
                if (request == null) {
                    return;
                }
            }
        }

        request.setResourceType(convertResourceType(details.getString("type")));
        finishResponse(requestInfo, request, responseObj);

        JSONObject deferredDetails = deferredResponseExtraInfo.get(request.requestId);
        if (deferredDetails != null) {
            processHeadersText(request, deferredDetails);
            deferredResponseExtraInfo.remove(request.requestId);
        }
    }

    private HttpRequest addNewRequestFromDefer(JSONObject responseObj, RequestInfo requestInfo) {
        HttpRequest request;
        Page page = getPageForRequest(requestInfo);
        if (page == null) {
            Logger.error("Unable to find frame for process: {} tab: {} frame: {}",
                    requestInfo.processId, requestInfo.tabId, requestInfo.frameId);
            return null;
        }

        String url = responseObj.getString("url");
        if (isInternalUrl(url)) {
            addInternalRequestId(requestInfo);
            return null;
        }

        request = new HttpRequest();
        request.setTabId(requestInfo.tabId);
        request.setFrameId(requestInfo.frameId);
        request.setRequestId(requestInfo.requestId);

        request.parseUrl(url);

        request.setState(HttpRequest.State.Send);

        setRequestHeaders(responseObj, request);

        synchronized (testResult) {
            page.addRequest(request);
            ongoingRequests.put(requestInfo.getRequestId(), request);
        }

        deferredRequestExtraInfo.remove(request.requestId);
        return request;
    }

    private void networkResponseReceivedExtraInfo(JSONObject details) {
        RequestInfo requestInfo = getDevToolsRequestInfo(details);
        if (isInternalRequestId(requestInfo)) {
            return;
        }

        HttpRequest request = getRequestForTab(requestInfo);
        if (request == null) {
            String requestId = details.getString("requestId");
            if (!StringUtils.isEmpty(requestId)) {
                deferredRequestExtraInfo.put(requestId, details);
            }
        } else {
            if (details.has("headersText")) {
                processHeadersText(request, details);
            }
        }

        // Blocked cookies also available
    }

    private void networkRequestServedFromCache(JSONObject details) throws JSONException {
        RequestInfo requestInfo = getDevToolsRequestInfo(details);
        if (isInternalRequestId(requestInfo)) {
            return;
        }

        HttpRequest request = getRequestForTab(requestInfo);
        if (request == null) {
            Logger.error("networkRequestServedFromCache event, but no request found: process {}, tab {}, request: {}",
                    requestInfo.processId, requestInfo.tabId, requestInfo.requestId);
            return;
        }
        request.setFromCache(true);
    }

    private void networkLoadingFinished(JSONObject details) throws JSONException {
        RequestInfo requestInfo = getDevToolsRequestInfo(details);
        if (isInternalRequestId(requestInfo)) {
            removeInternalRequestId(requestInfo);
            return;
        }

        HttpRequest request = getRequestForTab(requestInfo);
        if (request == null) {
            Logger.error("networkLoadingFinished event, but no request found: process {}, tab {}, request: {}",
                    requestInfo.processId, requestInfo.tabId, requestInfo.requestId);
            return;
        }

        setRecvEnd(details, request);
        request.setState(HttpRequest.State.Complete);
        removeOngoingRequest(requestInfo);
    }

    private void removeOngoingRequest(String requestId) {
        synchronized (testResult) {
            ongoingRequests.remove(requestId);
        }
    }

    private void removeOngoingRequest(RequestInfo requestInfo) {
        synchronized (testResult) {
            ongoingRequests.remove(requestInfo.getRequestId());
        }
    }

    private void networkLoadingFailed(JSONObject details) throws JSONException {
        RequestInfo requestInfo = getDevToolsRequestInfo(details);
        HttpRequest request = getRequestForTab(requestInfo);
        if (request == null) {
            Logger.error("networkLoadingFailed event, but no request found: process {}, tab {}, request: {}",
                    requestInfo.processId, requestInfo.tabId, requestInfo.requestId);
            return;
        }

        // The end time for failures, is usually the point at which the request
        // was scheduled to be made, but failed.  The start time is the time
        // the browser first got the request and added it to the queue.
        setRecvEnd(details, request);
        request.setState(HttpRequest.State.Complete);

        boolean errorSet = false;
        if (details.has("errorText")) {
            String error = details.getString("errorText");
            if (error.length() > 0) {
                errorSet = true;
                if (error.equals("net::ERR_BLOCKED_BY_CLIENT")) {
                    request.setError("Blocked");
                    // Since blocked requests weren't actually made, set their duration to zero
                    // you could made a case for leaving it or bumping up the start time to be
                    // the time the request was made, but i don't think this info is particularly
                    // useful and just leads to confusion. I.E. questions about why it took so long
                    // block a request.
                    request.setRecvEnd(0);
                } else {
                    request.setError(error);
                }
            }
        }

        if (details.has("canceled")) {
            if (!errorSet && details.getBoolean("canceled")) {
                errorSet = true;
                request.setError("Cancelled");
            }
        }

        // Handle case where browser blocks due to mixed content
        if (details.has("blockedReason")) {
            if (!errorSet) {
                request.setError("Blocked: " + details.getString("blockedReason"));
                errorSet = true;
            }
        }

        if (!errorSet) {
            request.setError("Unknown");
        }

        removeOngoingRequest(requestInfo);
    }

    private static void setRecvEnd(JSONObject details, HttpRequest request) {
        // Take into account wall time offsets being sent separately (this is stored in request.wallTimeOffset)
        // Timestamp is in seconds (with micro second accuracy)
        long endTime = (long)(details.getDouble("timestamp") * 1000);
        request.setRecvEnd((int) (endTime - (request.getStartTime() - request.getWallTimeOffset())));
    }

    private void networkDataReceived(JSONObject details) throws JSONException {
        RequestInfo requestInfo = getDevToolsRequestInfo(details);
        if (isInternalRequestId(requestInfo)) {
            return;
        }
        HttpRequest request = getRequestForTab(requestInfo);
        if (request == null) {
            Logger.error("networkDataReceived event, but no request found: process {}, tab {}, request: {}",
                requestInfo.processId, requestInfo.tabId, requestInfo.requestId);
            return;
        }

        request.addBytesRecv(details.getInt("dataLength"), details.getInt("encodedDataLength"));
    }

    private void navigationBegin(JSONObject details) throws JSONException {
        if (!shouldRecordNavigation(details)) {
            return;
        }

        FrameInfo frameInfo = getNavFrameInfo(details);

        // Is a top level frame?
        if (frameInfo.parentFrameId == -1) {
            Date navStartTime = convertToDateFromMillis(details.getDouble("timeStamp"));

            Page page = getPageForTab(frameInfo);
            // Check if we already processed the message.
            if (page != null) {
                if (page.getTabId() == frameInfo.tabId &&
                    page.getState() == Page.State.NavigationBegin &&
                    page.getNavStartTime().equals(navStartTime)) {
                    return;
                }
            }

            setupPageForNewNavigation(details, details.getString("url"), page, frameInfo);
        }
    }

    private void navigationCommitted(JSONObject details) throws JSONException {
        String url = details.getString("url");
        Page page = null;
        FrameInfo frameInfo = getNavFrameInfo(details);
        if (isAboutUrl(url)) {
            if (frameInfo.parentFrameId != -1) {
                // Not a top level frame, ignore
                return;
            }
            page = getPageForTab(frameInfo);
            page = setupPageForNewNavigation(details, url, page, frameInfo);
        } else {
            page = getPageForTab(details);
            // ignore
            if (page == null) {
                return;
            }

            if (details.getInt("frameId") != page.getFrameId()) {
                return;
            }
        }

        // Chrome stopped sending us the process id on navigationBegin at
        // around version 52, so we fill out the process id based on this
        // message.
        if (page.getProcessId() == -1) {
            page.setProcessId(frameInfo.processId);
        }

        page.setNavigationType(convertNavigationType(details.getString("transitionType")));
        page.setNavCommitTime(convertToDateFromMillis(details.getDouble("timeStamp")));
        page.setUrl(url); // URL may have redirected, record the new URL.
        page.setState(Page.State.NavigationCommitted);
    }

    private Page setupPageForNewNavigation(JSONObject details, String url, Page page, FrameInfo frameInfo) {
        // Check if there's already a page (created by the user using browser.newPage())
        // if so use it, if not create one.
        if (page == null || page.getState() != Page.State.Uninitialised) {
            page = getUnitializedPage();
            if (page == null) {
                page = new Page();
                addPage(page);
            }
        }

        Date navStartTime = convertToDateFromMillis(details.getDouble("timeStamp"));
        page.setFrameId(frameInfo.frameId);
        page.setTabId(frameInfo.tabId);
        page.setProcessId(frameInfo.processId);
        page.setOrigUrl(url);
        page.setNavStartTime(navStartTime);
        page.setState(Page.State.NavigationBegin);

        if (resultNotifier != null) {
            resultNotifier.onPageStart(page);
        }
        return page;
    }

    private void historyStateUpdated(JSONObject details) throws JSONException {
        Page page = getPageForTab(details);
        if (page == null) {
            return;
        }

        if (details.getInt("frameId") != page.getFrameId()) {
            return;
        }

        page.setUrl(details.getString("url"));
    }

    private void navigationDOMContentLoaded(JSONObject details) throws JSONException {
        synchronized(testResult) {
            Page page = getPageForTab(details);
            if (page == null) {
                return;
            }

            String url = details.getString("url");
            if (StringUtils.isSet(url)) {
                url = Http.stripAnchor(url);
                // HTTP2 document requests are missing a load event - generate one here instead
                for (Map.Entry<String,HttpRequest> requestEntry : ongoingRequests.entrySet()) {
                    HttpRequest request = requestEntry.getValue();
                    if ("h2".equalsIgnoreCase(request.getProtocol()) && url.equalsIgnoreCase(request.url)) {
                        if (request.getResourceType().equals(ResourceType.Document)) {
                            boolean isNavigate = false;
                            for (HttpHeader header : request.getRequestHeaders()) {
                                if (StringUtils.equalsIgnoreCase(header.name, "sec-fetch-mode")) {
                                    if (StringUtils.equalsIgnoreCase(header.value, "navigate")) {
                                        isNavigate = true;
                                        break;
                                    }
                                }
                            }
                            if (isNavigate) {
                                Date date = convertToDateFromMillis(details.getDouble("timeStamp"));
                                request.setRecvEnd((int) (date.getTime() - (request.getStartTime() - request.getWallTimeOffset())));
                                request.setState(HttpRequest.State.Complete);
                                removeOngoingRequest(requestEntry.getKey());
                                break;
                            }
                        }
                    }
                }
            }

            if (details.getInt("frameId") != page.getFrameId()) {
                return;
            }

            page.setDomContentLoadedTime(convertToDateFromMillis(details.getDouble("timeStamp")));
            page.setState(Page.State.DOMContentLoaded);
        }
    }

    private void navigationCompleted(JSONObject details) throws JSONException {
        synchronized(testResult) {
            Page page = null;
            Page match = null;
            FrameInfo frameInfo = getNavFrameInfo(details);
            String url = details.getString("url");
            for (int i = getPages().size() - 1; i >= 0; --i) {
                page = getPages().get(i);
                if (page.getTabId() == frameInfo.tabId &&
                    page.getFrameId() == frameInfo.frameId) {
                    if (page.getUrl() != null && page.getUrl().equals(url)) {
                        match = page;
                        break;
                    }
                    if (match == null) {
                        match = page;
                    }
                }
            }

            page = match;

            if (page == null) {
                return;
            }

            page.setNavEndTime(convertToDateFromMillis(details.getDouble("timeStamp")));
            setPageStateAndNotify(page, Page.State.NavigationCompleted);
        }
    }

    private void navigationError(JSONObject details) throws JSONException {
        Page page = getPageForFrameAndProcess(details);
        if (page == null) {
            return;
        }

        // If not the top level page frame discard
        if (details.getInt("frameId") != page.getFrameId()) {
            return;
        }

        page.setNavEndTime(convertToDateFromMillis(details.getDouble("timeStamp")));
        String error = details.getString("error");
        if (error != null) {
            page.setError(convertNavigationError(error));
        }

        setPageStateAndNotify(page, Page.State.NavigationError);
    }

    private Page.Error convertNavigationError(String error) {
        switch (error) {
        case "net::ERR_EMPTY_RESPONSE":
            return Page.Error.EmptyResponse;
        case "net::ERR_ABORTED":
            return Page.Error.Aborted;
        case "net::ERR_TIMED_OUT":
            return Page.Error.TimedOut;
        case "net::ERR_ACCESS_DENIED":
            return Page.Error.AccessDenied;
        case "net::ERR_OUT_OF_MEMORY":
            return Page.Error.OutOfMemory;
        case "net::ERR_INSUFFICIENT_RESOURCES":
            return Page.Error.InsufficientResources;
        case "net::ERR_CONNECTION_CLOSED":
            return Page.Error.ConnectionClosed;
        case "net::ERR_CONNECTION_RESET":
            return Page.Error.ConnectionReset;
        case "net::ERR_CONNECTION_REFUSED":
            return Page.Error.ConnectionRefused;
        case "net::ERR_CONNECTION_ABORTED":
            return Page.Error.ConnectionAborted;
        case "net::ERR_CONNECTION_FAILED":
            return Page.Error.ConnectionFailed;
        case "net::ERR_NAME_NOT_RESOLVED":
            return Page.Error.UnknownHostname;
        case "net::ERR_INTERNET_DISCONNECTED":
            return Page.Error.InternetDisconnected;
        case "net::ERR_SSL_PROTOCOL_ERROR":
            return Page.Error.SslProtocolError;
        case "net::ERR_ADDRESS_INVALID":
            return Page.Error.NetworkAddressInvalid;
        case "net::ERR_ADDRESS_UNREACHABLE":
            return Page.Error.NetworkAddressUnreachable;
        case "net::ERR_SSL_CLIENT_AUTH_CERT_NEEDED":
            return Page.Error.SslClientAuthCertNeeded;
        case "net::ERR_TUNNEL_CONNECTION_FAILED":
            return Page.Error.TunnelConnectionFailed;
        case "net::ERR_NO_SSL_VERSIONS_ENABLED":
            return Page.Error.NoSslVersionsEnabled;
        case "net::ERR_SSL_VERSION_OR_CIPHER_MISMATCH":
            return Page.Error.SslVersionOrCipherMismatch;
        case "net::ERR_SSL_RENEGOTIATION_REQUESTED":
            return Page.Error.SslRenegotiationRequested;
        case "net::ERR_BAD_SSL_CLIENT_AUTH_CERT":
            return Page.Error.BadSslClientAuthCert;
        case "net::ERR_CERT_ERROR_IN_SSL_RENEGOTIATION":
            return Page.Error.SslCertErrorInSslRenegotiation;
        case "net::ERR_CONNECTION_TIMED_OUT":
            return Page.Error.ConnectionTimedOut;
        case "net::ERR_CERT_COMMON_NAME_INVALID":
            return Page.Error.CertCommonNameInvalid;
        case "net::ERR_CERT_DATE_INVALID":
            return Page.Error.CertDateInvalid;
        case "net::ERR_CERT_AUTHORITY_INVALID":
            return Page.Error.CertAuthorityInvalid;
        case "net::ERR_CERT_CONTAINS_ERRORS":
            return Page.Error.CertContainsErrors;
        case "net::ERR_CERT_NO_REVOCATION_MECHANISM":
            return Page.Error.CertNoRevocationMechanism;
        case "net::ERR_CERT_UNABLE_TO_CHECK_REVOCATION":
            return Page.Error.CertUnableToCheckRevocation;
        case "net::ERR_CERT_CERT_REVOKED":
            return Page.Error.CertRevoked;
        case "net::ERR_CERT_INVALID":
            return Page.Error.CertInvalid;
        case "net::ERR_CERT_WEAK_SIGNATURE_ALGORITHM":
            return Page.Error.CertWeakSignatureAlgorithm;
        case "net::ERR_CERT_END":
            return Page.Error.CertEnd;
        case "net::ERR_DISALLOWED_URL_SCHEME":
            return Page.Error.DisallowedUrlScheme;
        case "net::ERR_UNKNOWN_URL_SCHEME":
            return Page.Error.UnknownUrlScheme;
        case "net::ERR_INVALID_URL":
            return Page.Error.InvalidUrl;
        case "net::ERR_TOO_MANY_REDIRECTS":
            return Page.Error.TooManyRedirects;
        case "net::ERR_UNSAFE_PORT":
            return Page.Error.UnsafePort;
        case "net::ERR_INVALID_RESPONSE":
            return Page.Error.InvalidResponse;
        case "net::ERR_INVALID_CHUNKED_ENCODING":
            return Page.Error.InvalidChunkedEncoding;
        case "net::ERR_METHOD_NOT_SUPPORTED":
            return Page.Error.MethodNotSupported;
        case "net::ERR_UNEXPECTED_PROXY_AUTH":
            return Page.Error.UnexpectedProxyAuth;
        case "net::ERR_RESPONSE_HEADERS_TOO_BIG":
            return Page.Error.ResponseHeadersTooBig;
        case "net::ERR_PAC_STATUS_NOT_OK":
            return Page.Error.PacStatusNotOk;
        case "net::ERR_PAC_SCRIPT_FAILED":
            return Page.Error.PacScriptFailed;
        case "net::ERR_MALFORMED_IDENTITY":
            return Page.Error.MalformedIdentity;
        case "net::ERR_CONTENT_DECODING_FAILED":
            return Page.Error.ContentDecodingFailed;
        case "net::ERR_NETWORK_IO_SUSPENDED":
            return Page.Error.NetworkIOSuspended;
        case "net::ERR_SYN_REPLY_NOT_RECEIVED":
            return Page.Error.SynReplyNotReceived;
        case "net::ERR_ENCODING_CONVERSION_FAILED":
            return Page.Error.EncodingConversionFailed;
        case "net::ERR_UNRECOGNIZED_FTP_DIRECTORY_LISTING_FORMAT":
            return Page.Error.UnrecognizedFtpDirectListingFormat;
        case "net::ERR_INVALID_FLIP_STREAM":
            return Page.Error.InvalidFlipStream;
        case "net::ERR_CACHE_MISS":
            return Page.Error.CacheMiss;
        case "net::ERR_CACHE_READ_FAILURE":
            return Page.Error.CacheReadFailure;
        case "net::ERR_CACHE_OPERATION_NOT_SUPPORTED":
            return Page.Error.CacheOperationNotSupported;
        case "net::ERR_INSECURE_RESPONSE":
            return Page.Error.InsecureResponse;
        case "net::ERR_BLOCKED_BY_CLIENT":
            return Page.Error.BlockedByClient;
        case "net::ERR_NETWORK_CHANGED":
            return Page.Error.NetworkChanged;
        case "net::ERR_INVALID_HTTP_RESPONSE":
            return Page.Error.InvalidResponse;
        case "net::ERR_FILE_NOT_FOUND":
            return Page.Error.UploadFileNotFound;
        case "net::ERR_INVALID_ARGUMENT":
            return Page.Error.InvalidArgument;
        default:
            Logger.error("Unknown page load error type {}", error);
            return Page.Error.Unclassified;
        }
    }

    private void navigationLoadTimes(JSONObject details) throws JSONException {
        Page page = getPageForTab(details);
        if (page == null) {
            return;
        }

        String connectionInfo = details.optString("connectionInfo");
        if (connectionInfo != null) {
            page.setProtocol(connectionInfo);
        }

        double firstPaintTime = details.optDouble("firstPaintTime");
        if (firstPaintTime != 0.0) {
            page.setFirstPaintTime(convertToDateFromSecs(firstPaintTime));
        }
        double firstPaintAfterLoadTime = details.optDouble("firstPaintAfterLoadTime");
        if (firstPaintAfterLoadTime != 0.0) {
            page.setFirstPaintAfterLoadTime(convertToDateFromSecs(firstPaintAfterLoadTime));
        }
    }

    private void calculatedPageStats(JSONObject details) throws JSONException {
        Page page = getPageForTab(details);
        if (page == null) {
            return;
        }

        page.setNumDomElements(details.optInt("nodes"));
        page.setNumFrames(details.optInt("documents"));
    }

    private void consoleMessagesCleared(JSONObject details) throws JSONException {
        Page page = getCurrentPageForTab(details.getInt("tabId"));
        if (page != null) {
            page.clearConsoleMessages();
        }
    }

    private void consoleMessagesAdded(JSONObject details) throws JSONException {
        Page.ConsoleMessage msg = new Page.ConsoleMessage();
        JSONObject messageObj = details.getJSONObject("message");
        Double timestamp = messageObj.optDouble("timestamp");
        if (timestamp.isNaN()) {
            msg.timestamp = System.currentTimeMillis();
        } else {
            msg.timestamp = convertToMillisFromSeconds(messageObj.getDouble("timestamp"));
        }
        msg.level = messageObj.getString("level");
        msg.text = messageObj.getString("text");
        msg.url = messageObj.getString("url");
        msg.line = messageObj.getInt("line");
        msg.column = messageObj.getInt("column");

        Page page = getCurrentPageForTab(details.getInt("tabId"));
        if (page != null) {
            page.addConsoleMessage(msg);
        }

        if (resultNotifier != null) {
            resultNotifier.onConsoleMessage(msg);
        }
    }

    private void messageRepeatCountUpdated(JSONObject details) throws JSONException {
        Page page = getCurrentPageForTab(details.getInt("tabId"));
        if (page != null) {
            page.addConsoleMessage(page.getLastConsoleMessage());
        }
    }

    private Date convertToDateFromMillis(double timestamp) {
        // Well Chrome is more precise than Date(), but then we are doing stuff
        // over sockets and hogging CPU, so I don't think sub millisecond times
        // are really relevant.
        return new Date((long)timestamp);
    }

    private Date convertToDateFromSecs(double timestamp) {
        return new Date((long)(timestamp * 1000));
    }

    /**
     * yesThisIsActuallyAFunctionAndIDontCareIfItsStupid()
     */
    private long convertToMillisFromSeconds(double timestamp) {
        return (long)(timestamp * 1000);
    }

    private NavigationType convertNavigationType(String type) {
        NavigationType navigationType = NavigationType.Unknown;
        switch (type) {
            case "link":
                navigationType = NavigationType.Link;
                break;
            case "typed":
                navigationType = NavigationType.Typed;
                break;
            case "auto_bookmark":
                navigationType = NavigationType.AutoBookmark;
                break;
            case "auto_subframe":
                navigationType = NavigationType.AutoSubFrame;
                break;
            case "manual_subframe":
                navigationType = NavigationType.ManualSubFrame;
                break;
            case "generated":
                navigationType = NavigationType.Generated;
                break;
            case "start_page":
                navigationType = NavigationType.StartPage;
                break;
            case "form_submit":
                navigationType = NavigationType.FormSubmit;
                break;
            case "reload":
                navigationType = NavigationType.Reload;
                break;
            case "keyword":
                navigationType = NavigationType.Keyword;
                break;
            case "keyword_generated":
                navigationType = NavigationType.KeywordGenerated;
                break;
        }

        return navigationType;
    }

    private ResourceType convertInitiator(String type) {
        ResourceType use = ResourceType.Other;
        switch (type) {
            case "image":
                use = ResourceType.Image;
                break;
            case "script":
                use = ResourceType.Script;
                break;
            case "stylesheet":
                use = ResourceType.Stylesheet;
                break;
            case "object":
                use = ResourceType.Object;
                break;
            case "xmlhttprequest":
                use = ResourceType.XmlHttpRequest;
                break;
        }

        return use;
    }

    private ResourceType convertResourceType(String type) {
        ResourceType t = ResourceType.Other;
        switch (type) {
            case "Document":
                t = ResourceType.Document;
                break;
            case "Image":
                t = ResourceType.Image;
                break;
            case "Script":
                t = ResourceType.Script;
                break;
            case "Stylesheet":
                t = ResourceType.Stylesheet;
                break;
            case "Object":
                t = ResourceType.Object;
                break;
            case "Font":
                t = ResourceType.Font;
                break;
            case "WebSocket":
                t = ResourceType.WebSocket;
                break;
            case "XHR":
                t = ResourceType.XmlHttpRequest;
                break;
        }

        return t;
    }

    private boolean shouldRecordNavigation(JSONObject details) throws JSONException {
        // Check if top level frame, we only record top level frame page loads for now.
        int parentFrameId = details.getInt("parentFrameId");
        if (parentFrameId == -1) {
            return true;
        }
        return false;
    }

    private Page getCurrentPage() {
        synchronized(testResult) {
            if (getPages().size() == 0) {
                return null;
            }

            return getPages().get(getPages().size() - 1);
        }
    }

    private void addPage(Page page) {
        synchronized(testResult) {
            testResult.addPage(page);
        }
    }

    private void setPageStateAndNotify(Page page, Page.State state) {
        synchronized(testResult) {
            if (page.getState() != state) {
                page.setState(state);
                if (resultNotifier != null) {
                    resultNotifier.onPageComplete(page);
                }
            }
        }
    }

    private Page getPageForTab(JSONObject details) throws JSONException {
        synchronized(testResult) {
            FrameInfo frameInfo = getNavFrameInfo(details);
            return getPageForTab(frameInfo);
        }
    }

    private Page getPageForFrameAndProcess(JSONObject details) throws JSONException {
        synchronized(testResult) {
            FrameInfo frameInfo = getNavFrameInfo(details);
            return getPageForFrameAndProcess(frameInfo);
        }
    }

    private Page getPageForFrameAndProcess(FrameInfo frameInfo) throws JSONException {
        if (frameInfo.processId != -1) {
            synchronized(testResult) {
                for (int i = getPages().size() - 1; i >= 0; --i) {
                    Page page = getPages().get(i);
                    if (page.getTabId() == frameInfo.tabId && page.frameId == frameInfo.frameId && page.processId == frameInfo.processId) {
                        return page;
                    }
                }
                return null;
            }
        } else {
            for (int i = getPages().size() - 1; i >= 0; --i) {
                Page page = getPages().get(i);
                if (page.getTabId() == frameInfo.tabId && page.frameId == frameInfo.frameId) {
                    return page;
                }
            }
            return null;
        }
    }

    private Page getPageForTab(FrameInfo frameInfo) throws JSONException {
        return getCurrentPageForTab(frameInfo.tabId);
    }

    private Page getPageForRequest(RequestInfo requestInfo) {
        return getCurrentPageForTab(requestInfo.tabId);
    }

    private HttpRequest getRequestForTab(RequestInfo requestInfo) {
        synchronized(testResult) {
            String id = requestInfo.getRequestId();
            return ongoingRequests.get(id);
        }
    }

    private Page getCurrentPageForTab(int tabId) {
        synchronized(testResult) {
            for (int i = getPages().size() - 1; i >= 0; --i) {
                Page page = getPages().get(i);
                if (page.getTabId() == tabId) {
                    return page;
                }
            }
            return null;
        }
    }

    private boolean isInternalUrl(String url) {
        if (url.startsWith("data:")) {
            return true;
        } else if (url.startsWith("chrome-extension:")) {
            return true;
        }
        return false;
    }

    private boolean isAboutUrl(String url) {
        if (url.startsWith("about:")) {
            return true;
        }
        return false;
    }

    private void addInternalRequestId(RequestInfo requestInfo) {
        internalRequestIds.add(requestInfo.getRequestId());
    }

    private boolean isInternalRequestId(RequestInfo requestInfo) {
        return internalRequestIds.contains(requestInfo.getRequestId());
    }

    private void removeInternalRequestId(RequestInfo requestInfo) {
        internalRequestIds.remove(requestInfo.getRequestId());
    }

    private ArrayList<Page> getPages() {
        return testResult.getPages();
    }
}
