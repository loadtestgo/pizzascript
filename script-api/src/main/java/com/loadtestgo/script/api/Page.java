package com.loadtestgo.script.api;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.ArrayList;
import java.util.Date;

public class Page {
    public ArrayList<HttpRequest> requests = new ArrayList<>();

    // Time the page object itself was created
    public Date createdTime;
    public Date navStartTime;
    public Date navCommitTime;
    public Date domContentLoadedTime;
    public Date navEndTime;
    public Date unloadTime;

    public int frameId;
    public int processId;
    public int tabId;
    public NavigationType navigationType;
    public String origUrl;
    public String url;

    public State state;
    public Error error;

    public ArrayList<ConsoleMessage> consoleMessages = new ArrayList<>();
    public Object pageId;
    public String protocol;
    public Date firstPaintTime;
    public Date firstPaintAfterLoadTime;
    public String name;
    public int numDomElements;
    public int numFrames;

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setFirstPaintTime(Date firstPaintTime) {
        this.firstPaintTime = firstPaintTime;
    }

    public Date getFirstPaintTime() {
        return firstPaintTime;
    }

    public void setFirstPaintAfterLoadTime(Date firstPaintAfterLoadTime) {
        this.firstPaintAfterLoadTime = firstPaintAfterLoadTime;
    }

    public Date getFirstPaintAfterLoadTime() {
        return firstPaintAfterLoadTime;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setNumDomElements(int numDomElements) {
        this.numDomElements = numDomElements;
    }

    public int getNumDomElements() {
        return numDomElements;
    }

    public void setNumFrames(int numFrames) {
        this.numFrames = numFrames;
    }

    public int getNumFrames() {
        return numFrames;
    }

    public enum State {
        Uninitialised,
        NavigationBegin,
        NavigationCommitted,
        DOMContentLoaded,
        NavigationCompleted,
        NavigationError
    }

    public Page() {
        this.state = State.Uninitialised;
        this.createdTime = new Date();
    }

    public void setState(State state) {
        this.state = state;
    }

    public State getState() {
        return state;
    }

    public void setRequests(ArrayList<HttpRequest> requests) {
        this.requests = requests;
    }

    public ArrayList<HttpRequest> getRequests() {
        return requests;
    }

    public Date getNavStartTime() {
        return navStartTime;
    }

    public void setNavStartTime(Date navStartTime) {
        this.navStartTime = navStartTime;
    }

    public Date getNavCommitTime() {
        return navCommitTime;
    }

    public void setNavCommitTime(Date navCommitTime) {
        this.navCommitTime = navCommitTime;
    }

    public Date getDomContentLoadedTime() {
        return domContentLoadedTime;
    }

    public void setDomContentLoadedTime(Date domContentLoadedTime) {
        this.domContentLoadedTime = domContentLoadedTime;
    }

    public Date getNavEndTime() {
        return navEndTime;
    }

    public void setNavEndTime(Date navEndTime) {
        this.navEndTime = navEndTime;
    }

    public Date getUnloadTime() {
        return unloadTime;
    }

    public void setUnloadTime(Date unloadTime) {
        this.unloadTime = unloadTime;
    }

    public int getFrameId() {
        return frameId;
    }

    public void setFrameId(int frameId) { this.frameId = frameId; }

    public int getProcessId() {
        return processId;
    }

    public void setProcessId(int processId) {
        this.processId = processId;
    }

    public int getTabId() {
        return tabId;
    }

    public void setTabId(int tabId) {
        this.tabId = tabId;
    }

    public NavigationType getNavigationType() {
        return navigationType;
    }

    public void setNavigationType(NavigationType navigationType) {
        this.navigationType = navigationType;
    }

    public String getOrigUrl() {
        return origUrl;
    }

    public void setOrigUrl(String origUrl) {
        this.origUrl = origUrl;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void addRequest(HttpRequest request) {
        // Keep the list in order of start time
        int i = requests.size();
        for (; i > 0; --i) {
            HttpRequest r = requests.get(i - 1);
            if (request.getStartTime() >= r.getStartTime()) {
                break;
            }
        }
        if (i < 0) {
            i = 0;
        }
        requests.add(i, request);

        // If the page is currently uninitialized set the URL to be of this
        // request.
        if (getUrl() == null) {
            setUrl(request.getUrl());
        }
    }

    public enum Error {
        Aborted,
        AccessDenied,
        BadSslClientAuthCert,
        CacheMiss,
        CacheOperationNotSupported,
        CacheReadFailure,
        CertAuthorityInvalid,
        CertCommonNameInvalid,
        CertContainsErrors,
        CertDateInvalid,
        CertEnd,
        CertInvalid,
        CertNoRevocationMechanism,
        CertRevoked,
        CertUnableToCheckRevocation,
        CertWeakSignatureAlgorithm,
        ConnectionAborted,
        ConnectionClosed,
        ConnectionFailed,
        ConnectionRefused,
        ConnectionReset,
        ConnectionTimedOut,
        ContentDecodingFailed,
        DisallowedUrlScheme,
        EmptyResponse,
        EncodingConversionFailed,
        InsecureResponse,
        InsufficientResources,
        InternetDisconnected,
        InvalidChunkedEncoding,
        InvalidFlipStream,
        InvalidResponse,
        MalformedIdentity,
        MethodNotSupported,
        NetworkAddressInvalid,
        NetworkAddressUnreachable,
        NetworkIOSuspended,
        NoSslVersionsEnabled,
        OutOfMemory,
        PacScriptFailed,
        PacStatusNotOk,
        ResponseHeadersTooBig,
        SslCertErrorInSslRenegotiation,
        SslClientAuthCertNeeded,
        SslProtocolError,
        SslRenegotiationRequested,
        SslVersionOrCipherMismatch,
        SynReplyNotReceived,
        TimedOut,
        TooManyRedirects,
        TunnelConnectionFailed,
        Unclassified,
        UnexpectedProxyAuth,
        UnknownHostname,
        UnknownUrlScheme,
        UnrecognizedFtpDirectListingFormat,
        InvalidUrl,
        BlockedByClient,
        NetworkChanged,
        UnsafePort,
        UploadFileNotFound,
        InvalidArgument
    }

    public void setError(Error error) {
        this.error = error;
    }

    public Error getError() {
        return error;
    }

    public void setPageId(Object pageId) {
        this.pageId = pageId;
    }

    public Object getPageId() {
        return pageId;
    }

    public static class ConsoleMessage {
        public String level;
        public long timestamp;
        public String text;
        public String url;
        public int column;
        public int line;
    }

    public ArrayList<ConsoleMessage> getConsoleMessages() {
        if (consoleMessages.isEmpty()) {
            return null;
        } else {
            return consoleMessages;
        }
    }

    public void setConsoleMessages(ArrayList<ConsoleMessage> consoleMessages) {
        this.consoleMessages = consoleMessages;
    }

    public void addConsoleMessage(ConsoleMessage message) {
        consoleMessages.add(message);
    }

    public void clearConsoleMessages() {
        consoleMessages.clear();
    }

    @JsonIgnore
    public ConsoleMessage getLastConsoleMessage() {
        if (consoleMessages.size() == 0) {
            return null;
        }

        return consoleMessages.get(consoleMessages.size() - 1);
    }

    @JsonIgnore
    public double getLoadTime() {
        // Return the nav end time if there was one
        if (navEndTime != null && navStartTime != null) {
            double loadTime = navEndTime.getTime() - navStartTime.getTime();
            if (loadTime > 0) {
                return loadTime / 1000.0;
            }
        }

        return 0.0;
    }

    @JsonIgnore
    public long getBytesDownloaded() {
        long bytes = 0;
        for (HttpRequest request : requests) {
            if (!request.isFromCache()) {
                int size = request.getBytesRecvCompressed();
                if (size > 0) {
                    bytes += size;
                } else {
                    bytes += request.getBodySize();
                }
            }
        }
        return bytes;
    }

    @JsonIgnore
    public long getBytesUploaded() {
        long bytes = 0;
        for (HttpRequest request : requests) {
            if (!request.isFromCache()) {
                bytes += request.getRequestBodySize();
            }
        }
        return bytes;
    }

    @JsonIgnore
    public int getTotalBytesDownloaded() {
        int bytes = 0;
        for (HttpRequest request : requests) {
            bytes += request.getTotalBytesDownloaded();
        }
        return bytes;
    }

    @JsonIgnore
    public int getTotalBytesUploaded() {
        int bytes = 0;
        for (HttpRequest request : requests) {
            bytes += request.getTotalBytesUploaded();
        }
        return bytes;
    }

    @JsonIgnore
    public int getNumRequests() {
        return requests.size();
    }

    @JsonIgnore
    public int getNumRequestsNotCached() {
        int items = 0;
        for (HttpRequest request : requests) {
            if (!request.isFromCache()) {
                items++;
            }
        }
        return items;
    }

    @JsonIgnore
    public String getInitialName() {
        String name = getOrigUrl();
        if (name == null) {
            name = getUrl();
            if (name == null) {
                return getName();
            }
        }
        return name;
    }

    @JsonIgnore
    public String getFinalName() {
        String name = getUrl();
        if (name == null) {
            name = getOrigUrl();
            if (name == null) {
                return getName();
            }
        }
        return name;
    }

    @Override
    public String toString() {
        return "Page";
    }
}
