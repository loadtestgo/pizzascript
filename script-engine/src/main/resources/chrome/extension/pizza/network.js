pizza.main.network = function() {
    var _ws = null,
        _username = null,
        _password = null,
        _listeners = {},
        _suppressNextNav = false,
        _authSent = []; // Used to make sure authentication is sent automatically only once per request.

    var onNavBeforeCallback = function(details) {
        if (_suppressNextNav) {
            return;
        }

        if (pizza.isExternalUrl(details.url)) {
            _ws.send(JSON.stringify({ event: "navigationBegin", details: details}));
            _callListeners("onBeforeNavigate", details);
        }
    };

    var onCreatedNavigationTarget = function(details) {
        if (_suppressNextNav) {
            return;
        }

        _ws.send(JSON.stringify({ event: "onCreatedNavigationTarget", details: details}));
    };

    var onNavCommittedCallback = function(details) {
        if (_suppressNextNav) {
            return;
        }

        // Send the nav update first, so the navigation state for the page is updated,
        // before the navigation is considered completed (in navigation.js)
        _ws.send(JSON.stringify({ event: "navigationCommitted", details: details}));

        _callListeners("onCommitted", details);
    };

    var onNavErrorOccurredCallback = function(details) {
        if (_suppressNextNav) {
            return;
        }

        // Send the nav update first, so the navigation state for the page is updated,
        // before the navigation is considered completed (in navigation.js)
        _ws.send(JSON.stringify({ event: "navigationError", details: details}));

        _callListeners("onErrorOccurred", details);
    };

    var onNavDOMContentLoadedCallback = function(details) {
        if (_suppressNextNav) {
            return;
        }

        _ws.send(JSON.stringify({ event: "navigationDOMContentLoaded", details: details}));
    };

    var onNavCompletedCallback = function(details) {
        if (_suppressNextNav) {
            _suppressNextNav = false;
            return;
        }

        function sendEvents() {
            // Once the detailed load times have been sent, send the nav complete message.
            // Scripts often use this to wait on, so it's good to finish our work before
            // calling this function.
            _ws.send(JSON.stringify({ event: "navigationCompleted", details: details}));
            _callListeners("onCompleted", details);
        }

        // If it's the main frame then send detailed load times, for other frames we don't record
        // this info right now.
        if (details.frameId === 0) {
            var wait = pizza.waitAll();

            console.log("done loading: ", details.url, " gathering stats");
            if (pizza.isExternalUrl(details.url)) {
                // We could used the devtools' Memory.getDOMCounters() but it returns stats for the current
                // devtools tab, not the tab that just navigated, also it counts extension pages and nodes loaded
                // by extension pages.
                chrome.tabs.executeScript(details.tabId,
                    { code: "document.querySelectorAll('*').length", allFrames: true, runAt: 'document_start' },
                    wait.add(function (response) {
                        if (chrome.runtime.lastError) {
                            console.log(chrome.runtime.lastError);
                            return;
                        }
                        if (response && response.length > 0) {
                            try {
                                var numElements = 0;
                                for (var i = 0; i < response.length; ++i) {
                                    numElements += response[i];
                                }
                                _ws.send(JSON.stringify({ event: "calculatedPageStats", details: {
                                    nodes: numElements,
                                    documents: response.length,
                                    tabId: details.tabId,
                                    processId: details.processId,
                                    frameId: details.frameId
                                } }));
                            } catch (e) {
                                console.log(e);
                            }
                        }
                    }));
            }

            wait.done(sendEvents);
        } else {
            sendEvents();
        }
    };

    var onHistoryStateUpdated = function(details) {
        _callListeners("onHistoryStateUpdated", details);
        _ws.send(JSON.stringify({ event: "historyStateUpdated", details: details}));
    };

    var onAuthRequiredCallback = function(details) {
        _ws.send(JSON.stringify({ event: "onAuthRequired", details: details}));
        // If we already tried to auth but failed, then don't supply the password again
        if (!_authSent[details.requestId]) {
            if (_username || _password) {
                _authSent[details.requestId] = true;
                return { authCredentials: { username: _username, password: _password } };
            }
        }
    };

    var _init = function(ws) {
        _ws = ws;

        chrome.webNavigation.onBeforeNavigate.addListener(onNavBeforeCallback, {urls: ["<all_urls>"]});
        chrome.webNavigation.onCommitted.addListener(onNavCommittedCallback, {urls: ["<all_urls>"]});
        chrome.webNavigation.onDOMContentLoaded.addListener(onNavDOMContentLoadedCallback, {urls: ["<all_urls>"]});
        chrome.webNavigation.onCompleted.addListener(onNavCompletedCallback, {urls: ["<all_urls>"]});
        chrome.webNavigation.onErrorOccurred.addListener(onNavErrorOccurredCallback, {urls: ["<all_urls>"]});
        chrome.webNavigation.onCreatedNavigationTarget.addListener(onCreatedNavigationTarget, {urls: ["<all_urls>"]});
        chrome.webNavigation.onHistoryStateUpdated.addListener(onHistoryStateUpdated, {urls: ["<all_urls>"]});

        // Used to grab request IPs from these callbacks, now devtools provides them
        // chrome.webRequest.onCompleted.addListener(onCompletedCallback, {urls: ["<all_urls>"]});
        // chrome.webRequest.onBeforeRedirect.addListener(onBeforeRedirectCallback, {urls: ["<all_urls>"]});
        // chrome.webRequest.onErrorOccurred.addListener(onErrorOccurredCallback, {urls: ["<all_urls>"]});
        chrome.webRequest.onAuthRequired.addListener(onAuthRequiredCallback, {urls: ["<all_urls>"]}, ['blocking']);
    };

    var _setAuth = function(username, password) {
        _username = username;
        _password = password;
        _authSent = [];
    };

    var _addListener = function(id, listener) {
        var listeners;
        if (_listeners.hasOwnProperty(id)) {
            listeners = _listeners[id];
        } else {
            listeners = [];
            _listeners[id] = listeners;
        }
        listeners.push(listener);
    };

    var _callListeners = function(id, details) {
        if (_listeners.hasOwnProperty(id)) {
            var listeners = _listeners[id];
            listeners.forEach(function(entry) {
                entry.apply(null, [details]);
            });
        }
    };

    var _suppressNextNavigation = function() {
        _suppressNextNav = true;
    };

    return {
        init: _init,
        setAuth: _setAuth,
        suppressNextNavigation: _suppressNextNavigation,
        addListener: _addListener
    };
};
