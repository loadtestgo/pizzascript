/**
 * Experimental WebDriver interface
 *
 * Only a couple of commands are implemented right now.
 *
 * The meat of the browser automation is in command.js, input.js and automation.js
 * these can mostly be reused here to fill in the missing functionality.
 *
 * This class should match the WebDriver json wire protocol pretty closely (the only
 * difference is we added two extra json properties to represent the URL and Http
 * method used by the REST part of WebDriver json protocol).
 */
pizza.main.webdriver = function() {
    var _currentTabId = null,
        _responseHandler = null,
        _commandMap = {};

    var _handleCommand = function(cmd) {
        _execCommand(cmd.id, cmd.method, cmd.path, cmd.params);
    };

    var _setUrl = function(id, pathParams, bodyParams) {
        pizza.navigation.reset(_currentTabId, 0);
        pizza.devtools.sendCommand("Page.navigate", { url: bodyParams.url },
            function(response) {
                // Old way was to return a null response and have the error
                // in chrome.runtime.lastError
                if (response == null) {
                    _sendResponse(id, { error: chrome.runtime.lastError });
                } else if (response.message) {
                    // New way is to return a json string with the error message
                    // and code embedded
                    try {
                        var errorObj = JSON.parse(response.message);
                        if (errorObj && errorObj.message) {
                            _sendResponse(id, { error: errorObj.message });
                        } else {
                            _sendResponse(id, { error: response.message });
                        }
                    } catch (e) {
                        _sendResponse(id, { error: response.message });
                    }
                } else {
                    pizza.navigation.setLoadedHandler(function() {
                        _sendResponse(id, response);
                    });
                }
            });
    };

    var _getUrl = function(id, pathParams, bodyParams) {
        chrome.tabs.get(_currentTabId, function(tab) {
            _sendResponse(id, { url: tab.url });
        });
    };

    var _sendResponse = function(id, response) {
        if (_responseHandler == null) {
            console.log("No response handler set.");
        } else {
            _responseHandler(id, response);
        }
    };

    var _setTab = function(tabId) {
        if (_currentTabId == null) {
            _currentTabId = tabId;
        }
    };

    var _setResponseHandler = function(responseHandler) {
        _responseHandler = responseHandler;
    };

    var _addCommand = function(type, path, func) {
        var n = path.split("/");
        var mapPart = _commandMap;
        for (var i = 1; i < n.length; ++i) {
            var currentMap;
            var part = n[i];
            if (part != null && part.length > 0) {
                if (mapPart[part] == null) {
                    currentMap = { };
                    mapPart[part] = currentMap;
                } else {
                    currentMap = mapPart[part];
                }
                mapPart = currentMap;
            }
        }

        if (mapPart != null) {
            mapPart[type] = {
                func: func
            };
        }
    };

    var _execCommand = function(id, type, path, bodyParams) {
        var pathParams = {};
        var n = path.split("/");
        var mapPart = _commandMap;
        for (var i = 1; i < n.length; ++i) {
            var currentMap;
            var part = n[i];
            if (mapPart[part] == null) {
                for (var prop in mapPart) {
                    if (prop != null && prop.length > 0) {
                        if (prop[0] === ':') {
                           currentMap = mapPart[prop];
                           pathParams[prop.substring(1)] = part;
                        }
                    }
                }
                if (currentMap == null) {
                    throw "Unknown command for path: " + type + " " + path;
                }
            } else {
                currentMap = mapPart[part];
            }
            mapPart = currentMap;
        }

        if (mapPart[type] != null) {
            mapPart[type].func.call(this, id, pathParams, bodyParams);
        } else {
            throw "Unknown command for path: " + type + " " + path;
        }
    };

    // Register commands
    _addCommand("GET", "/session/:sessionId/url", _getUrl);
    _addCommand("POST", "/session/:sessionId/url", _setUrl);

    return {
        handleCommand: _handleCommand,
        setTab: _setTab,
        setResponseHandler: _setResponseHandler
    };
};
