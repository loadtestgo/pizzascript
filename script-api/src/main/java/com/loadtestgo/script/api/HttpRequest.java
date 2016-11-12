package com.loadtestgo.script.api;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.loadtestgo.util.HttpHeader;
import com.loadtestgo.util.Http;

import java.util.ArrayList;
import java.util.List;

public class HttpRequest {
    private static final int MAX_POST_DATA_LEN = 2048;

    // Request
    public String method;
    public String url;
    public String protocol;
    public ArrayList<HttpHeader> requestHeaders = new ArrayList<>();
    public int requestBodySize;

    // Response
    public int statusCode;
    public String statusText;
    public ArrayList<HttpHeader> responseHeaders = new ArrayList<>();
    public int bodySize;
    public int bytesRecvCompressed = -1;

    // WebSocket messages if this is a websocket
    public List<WebSocketMessage> wsMessages;

    // The redirect URL in the response body if there was one
    public String redirectUrl;

    // Browser data about the origin of the request
    public String requestId;
    public int frameId;
    public int parentFrameId;
    public int tabId;
    public ResourceType resourceType;

    // Was the data already cached?
    public boolean fromCache;

    // The actual ipv4 or ipv6 that the request was made to
    public String ip;
    public String host;

    // Error message when there was an error making the request
    // or an invalid response was reported.
    public String error;

    // Timings
    public long startTime;
    // Each of these timings is the offset from the start time in milliseconds
    public int dnsStart = -1;
    public int dnsEnd = -1;
    public int connectStart = -1;
    public int sslStart = -1;
    public int sslEnd = -1;
    public int connectEnd = -1;
    public int sendStart = -1;
    public int sendEnd = -1;
    public int recvHeadersEnd = -1;
    public int recvEnd = -1;

    // Connection details
    public boolean connectionReused;
    public int connectionId;
    public int responseHeadersSize;
    public int requestHeadersSize;
    public int blockedTime;

    // State of the request
    public State state = State.Init;
    public String postData;
    public String mimeType;
    public String initiatorUrl;

    // Milliseconds offset to wall clock at request start
    // DevTools starting using this method rather than giving wall time
    // for every timing value
    private long wallTimeOffset;

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public ArrayList<HttpHeader> getRequestHeaders() {
        return requestHeaders;
    }

    public void setRequestHeaders(ArrayList<HttpHeader> requestHeaders) {
        this.requestHeaders = requestHeaders;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public ArrayList<HttpHeader> getResponseHeaders() {
        return responseHeaders;
    }

    public void setResponseHeaders(ArrayList<HttpHeader> responseHeaders) {
        this.responseHeaders = responseHeaders;
    }

    public String getRedirectUrl() {
        return redirectUrl;
    }

    public void setRedirectUrl(String redirectUrl) {
        this.redirectUrl = redirectUrl;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public int getFrameId() {
        return frameId;
    }

    public void setFrameId(int frameId) {
        this.frameId = frameId;
    }

    public int getParentFrameId() {
        return parentFrameId;
    }

    public void setParentFrameId(int parentFrameId) {
        this.parentFrameId = parentFrameId;
    }

    public int getTabId() {
        return tabId;
    }

    public void setTabId(int tabId) {
        this.tabId = tabId;
    }

    public ResourceType getResourceType() {
        return resourceType;
    }

    public void setResourceType(ResourceType resourceType) {
        this.resourceType = resourceType;
    }

    public boolean isFromCache() {
        return fromCache;
    }

    public void setFromCache(boolean fromCache) {
        this.fromCache = fromCache;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public int getBodySize() {
        return bodySize;
    }

    @JsonIgnore
    public String getRequestHttpVersion() {
        return "HTTP/1.1";
    }

    public int getRequestBodySize() {
        return requestBodySize;
    }

    public String getStatusText() {
        return statusText;
    }

    public void setStatusText(String statusText) {
        this.statusText = statusText;
    }

    public void setRequestBodySize(int requestBodySize) {
        this.requestBodySize = requestBodySize;
    }

    public void setBodySize(int bodySize) {
        this.bodySize = bodySize;
    }

    public void setConnectionReused(boolean connectionReused) {
        this.connectionReused = connectionReused;
    }

    public boolean isConnectionReused() {
        return connectionReused;
    }

    public void setConnectionId(int connectionId) {
        this.connectionId = connectionId;
    }

    public int getConnectionId() {
        return connectionId;
    }

    public int getDnsStart() {
        return dnsStart;
    }

    public void setDnsStart(int dnsStart) {
        this.dnsStart = dnsStart;
    }

    public int getDnsEnd() {
        return dnsEnd;
    }

    public void setDnsEnd(int dnsEnd) {
        this.dnsEnd = dnsEnd;
    }

    public int getConnectStart() {
        return connectStart;
    }

    public void setConnectStart(int connectStart) {
        this.connectStart = connectStart;
    }

    public int getSslStart() {
        return sslStart;
    }

    public void setSslStart(int sslStart) {
        this.sslStart = sslStart;
    }

    public int getSslEnd() {
        return sslEnd;
    }

    public void setSslEnd(int sslEnd) {
        this.sslEnd = sslEnd;
    }

    public int getConnectEnd() {
        return connectEnd;
    }

    public void setConnectEnd(int connectEnd) {
        this.connectEnd = connectEnd;
    }

    public int getSendStart() {
        return sendStart;
    }

    public void setSendStart(int sendStart) {
        this.sendStart = sendStart;
    }

    public int getSendEnd() {
        return sendEnd;
    }

    public void setSendEnd(int sendEnd) {
        this.sendEnd = sendEnd;
    }

    public int getRecvHeadersEnd() {
        return recvHeadersEnd;
    }

    public void setRecvHeadersEnd(int recvHeadersEnd) {
        this.recvHeadersEnd = recvHeadersEnd;
    }

    public int getRecvEnd() {
        return recvEnd;
    }

    public void setRecvEnd(int recvEnd) {
        this.recvEnd = recvEnd;
    }

    public void addBytesRecv(int bytesRecv, int bytesRecvCompressed) {
        if (bytesRecvCompressed > 0) {
            if (this.bytesRecvCompressed == -1) {
                this.bytesRecvCompressed = bytesRecvCompressed;
            } else {
                this.bytesRecvCompressed += bytesRecvCompressed;
            }
        }

        bodySize += bytesRecv;
    }

    public void addBytesSent(int len) {
        requestBodySize += len;
    }

    public String getHost() {
        return host;
    }

    public void parseUrl(String url) {
        this.url = url;
        Http.UrlDetails details = Http.parseUrl(url);
        this.host = details.host;
    }

    public int getBytesRecvCompressed() {
        return bytesRecvCompressed;
    }

    public void setResponseHeadersSize(int responseHeadersSize) {
        this.responseHeadersSize = responseHeadersSize;
    }

    public int getResponseHeadersSize() {
        return responseHeadersSize;
    }

    public void setRequestHeadersSize(int requestHeadersSize) {
        this.requestHeadersSize = requestHeadersSize;
    }

    public int getRequestHeadersSize() {
        return requestHeadersSize;
    }

    public void setBlockedTime(int blockedTime) {
        this.blockedTime = blockedTime;
    }

    public int getBlockedTime() {
        return blockedTime;
    }

    public void setState(State state) {
        this.state = state;
    }

    public State getState() {
        return state;
    }

    public void setPostData(String postData) {
        if (postData != null && postData.length() > MAX_POST_DATA_LEN) {
            this.postData = postData.substring(0, MAX_POST_DATA_LEN);
        } else {
            this.postData = postData;
        }
    }

    public String getPostData() {
        return postData;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getMimeType() {
        return mimeType;
    }

    @JsonIgnore
    public long getEndTime() {
        if (recvEnd > 0) {
            return recvEnd + startTime;
        }
        return startTime;
    }

    public void setInitiatorUrl(String initiatorUrl) {
        this.initiatorUrl = initiatorUrl;
    }

    public String getInitiatorUrl() {
        return initiatorUrl;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getProtocol() {
        return protocol;
    }

    @JsonIgnore
    public int getTotalBytesUploaded() {
        int bytes = 0;
        if (!isFromCache()) {
            bytes += getRequestBodySize();
            bytes += getRequestHeadersSize();
        }
        return bytes;
    }

    @JsonIgnore
    public int getTotalBytesDownloaded() {
        int bytes = 0;
        if (!isFromCache()) {
            int size = getBytesRecvCompressed();
            if (size > 0) {
                bytes += size;
            } else {
                bytes += getBodySize();
            }
            bytes += getRecvHeadersEnd();
        }
        return bytes;
    }

    @JsonIgnore
    public void setWallTimeOffset(long wallTimeOffset) {
        this.wallTimeOffset = wallTimeOffset;
    }

    @JsonIgnore
    public long getWallTimeOffset() {
        return wallTimeOffset;
    }

    public void addWebSocketMessage(WebSocketMessage message) {
        if (wsMessages == null) {
            setIsWebSocket();
        }
        wsMessages.add(message);
    }

    @JsonIgnore
    public void setIsWebSocket() {
        if (wsMessages == null) {
            wsMessages = new ArrayList<>();
        }
    }

    @JsonIgnore
    public boolean isWebSocket() {
        return wsMessages != null;
    }

    public enum State {
        Init,
        Send,
        Recv,
        Complete
    }

    @Override
    public String toString() {
        return "HttpRequest";
    }
}
