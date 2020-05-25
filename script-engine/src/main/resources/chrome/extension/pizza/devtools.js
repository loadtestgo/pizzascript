// This code handles attaching each tab to devtools and routing devtools events
pizza.main.devtools = function() {
    var _attachedTabs = [];
    var _currentTab = null;
    var _version = "1.0";
    var _ws = null;
    var _suppressSocketMessages = false;

    var onEvent = function(debugId, method, params) {
        params.tabId = debugId.tabId;

        if (method.startsWith('Runtime')) {
            pizza.contexttracker.handleEvent(method, params);
        } else if (method.startsWith('DOM')) {
            pizza.frametracker.handleEvent(method, params);
        } else if (method.startsWith('Page')) {
            if (method === 'Page.javascriptDialogOpening' ||
                method === 'Page.javascriptDialogClosed') {
                pizza.commands.handleEvent(method, params);
            } else if (method === 'Page.screencastFrame') {
                _ws.send(JSON.stringify({event: method, details: params}));
            }
        } else {
            // Network events go directly to browser
            if (!_suppressSocketMessages) {
                _ws.send(JSON.stringify({event: method, details: params}));
            }
        }
    };

    var onAttach = function(debugId, wait) {
        if (chrome.runtime.lastError) {
            console.log(chrome.runtime.lastError.message);
            return;
        }

        _attachedTabs.push(debugId.tabId);

        chrome.debugger.sendCommand(debugId, "Network.enable", {}, wait.add("network"));
        chrome.debugger.sendCommand(debugId, "Console.enable", {}, wait.add("console"));
        chrome.debugger.sendCommand(debugId, "Page.enable", {}, wait.add("page"));
        // Disable page messages for now, I couldn't find a use for 'em
        // chrome.debugger.sendCommand(debugId, "Page.enable", {},  wait.add("page"));
        chrome.debugger.sendCommand(debugId, "Runtime.enable", {}, wait.add("runtime"));
        // Stable versions don't require you to call the following, that's ok though
        // in future they will, so keep the code below, even though it causes an error
        // to be reported right now
        chrome.debugger.sendCommand(debugId, "DOM.enable", {}, wait.add("dom"));

        wait.callbacksAdded();
    };

    function attachToTab(tabId, wait) {
        var newTabSet = false;
        if (_currentTab == null) {
            _currentTab = tabId;
            newTabSet = true;
        }

        if (_attachedTabs.indexOf(tabId) >= 0) {
            return;
        }

        console.log("Attaching to tab: " + tabId);

        var debugId = { tabId: tabId };
        chrome.debugger.attach(debugId, _version, onAttach.bind(null, debugId, wait));

        if (newTabSet) {
            pizza.webdriver.setTab(tabId);
            pizza.commands.setTab(tabId);
            pizza.frametracker.setTab(tabId);
        }
    }

    function detachFromTab(tabId) {
        pizza.arrayDelete(_attachedTabs, tabId);

        console.log("Detaching from tab: " + tabId);

        var debugId = { tabId:tabId };
        chrome.debugger.detach(debugId, onDetach.bind(null, debugId));

        if (_currentTab == tabId) {
            if (_attachedTabs.length > 0) {
                _currentTab = _attachedTabs[0];
            } else {
                _currentTab = null;
            }
        }
    }

    var onDetach = function(debugId) {
        if (chrome.runtime.lastError) {
            console.log(chrome.runtime.lastError.message);
            return;
        }
        console.log("debugger detached: " + debugId.tabId);
    };

    var _init = function(ws, wait) {
        _ws = ws;

        chrome.debugger.onEvent.addListener(onEvent);
        chrome.debugger.onDetach.addListener(onDetach);

        // Wait for callbacks to be added
        wait.addCallbacksLater();

        // Attach to the first tab we come across, that's the one we will initially control.
        chrome.tabs.query({}, function(tabs) {
            if (tabs.length > 0) {
                var tabId = tabs[0].id;
                attachToTab(tabId, wait);
            }
        });

        chrome.tabs.onCreated.addListener(function(tab) {
            // Attach if a new tab with an external URL is created (or
            // one with the default.
            if (pizza.isExternalUrl(tab.url) || tab.url == "about:blank") {
                attachToTab(tab.id, pizza.waitNull());
                _ws.send(JSON.stringify({event: "tabCreated", details: tab}));
            }
        });

        chrome.tabs.onRemoved.addListener(function(tab) {
            if (_attachedTabs.indexOf(tab) < 0) {
                return;
            }
            detachFromTab(tab);
        });

        pizza.network.addListener("onBeforeNavigate", function(details) {
            if (_attachedTabs.indexOf(details.tabId) > 0) {
                return;
            }
            attachToTab(details.tabId, pizza.waitNull());
        })
    };

    var _setTab = function(tabId) {
        _currentTab = tabId;

        pizza.webdriver.setTab(tabId);
        pizza.commands.setTab(tabId);
        pizza.frametracker.setTab(tabId);
    };

    var _sendCommand = function(cmd, params, respondFunc) {
        params = params || {};
        chrome.debugger.sendCommand({ tabId: _currentTab }, cmd, params,
            function(response) {
                if (response) {
                    respondFunc(response);
                } else {
                    respondFunc(chrome.runtime.lastError);
                }
            });
    };

    var _suppressMessages = function(suppress) {
        _suppressSocketMessages = suppress;
    };

    return {
        init: _init,
        setTab: _setTab,
        sendCommand: _sendCommand,
        suppressMessages: _suppressMessages
    };
};
