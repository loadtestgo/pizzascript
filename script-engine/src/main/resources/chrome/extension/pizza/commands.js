/**
 * The command processor handles commands sent over the websocket.
 * The commands are in json and are a custom protocol, that corresponds
 * closely to our Pizza API language binding.
 */
pizza.main.commands = function() {
    var _currentTabId = null,
        _responseHandler = null,
        _binaryResponseHandler = null,
        _commandMap = {},
        _currentFrameId = null,
        _currentContextId = null,
        _automationAPI = null,
        _headers = {},
        _autoDismissDialogs = false,
        _dialogInfo = null,
        _webRequestModifyCallbacks = [];

    pizza.contexttracker.addContextDestroyedHandler(function(contextId) {
        if (contextId === _currentContextId) {
            _automationAPI = null;
            _currentContextId = null;
        }
    });

    var _handleCommand = function(cmd) {
        var f = _commandMap[cmd.name];
        if (f) {
            try {
                if (cmd.params) {
                    f.call(this, cmd.id, cmd.params);
                } else {
                    // For null params or missing params provide an empty object
                    // so functions don't have to check if params is set or not
                    // when they take optional params
                    f.call(this, cmd.id, {});
                }
            } catch (e) {
                var lastError = chrome.runtime.lastError;
                if (lastError) {
                    console.log(lastError);
                }
                sendResponse(cmd.id, { error: e.toString(), stack: e.stack });
            }
        } else {
            sendResponse(cmd.id, { error: "Unable to find command " + cmd.name });
        }
    };

    var _setResponseHandler = function(responseHandler) {
        _responseHandler = responseHandler;
    };

    var _setBinaryResponseHandler = function(binaryResponseHandler) {
        _binaryResponseHandler = binaryResponseHandler;
    };

    function sendResponse(id, response) {
        if (_responseHandler == null) {
            console.log("No response handler set.");
        } else {
            _responseHandler(id, response);
        }
    }

    var relayChromeError = function(id) {
        var lastError = chrome.runtime.lastError;
        if (lastError) {
            if (lastError.message) {
                sendResponse(id, { error: lastError.message });
            } else {
                sendResponse(id, { error: lastError });
            }
            return true;
        }
        return false;
    };

    var addCommand = function(name, func) {
        _commandMap[name] = func;
    };

    var _open = function(id, params) {
        pizza.navigation.reset(_currentTabId, 0);
        pizza.navigation.setLoadedHandler(function(details) {
            sendResponse(id, details);
        });
        pizza.devtools.sendCommand("Page.navigate", { url: params.url },
            function(response) {
                // Old way was to return a null response and have the error
                // in chrome.runtime.lastError
                if (response == null) {
                    pizza.navigation.setLoadedHandler(null);
                    sendResponse(id, { error: chrome.runtime.lastError });
                } else if (response.message) {
                    // New way is to return a json string with the error message
                    // and code embedded
                    pizza.navigation.setLoadedHandler(null);
                    try {
                        var errorObj = JSON.parse(response.message);
                        if (errorObj && errorObj.message) {
                            sendResponse(id, { error: errorObj.message });
                        } else {
                            sendResponse(id, { error: response.message });
                        }
                    } catch (e) {
                        sendResponse(id, { error: response.message });
                    }
                }
            });
    };

    var _openAsync = function(id, params) {
        pizza.navigation.reset(_currentTabId, 0);
        pizza.devtools.sendCommand("Page.navigate", { url: params.url },
            function(response) {
                if (response == null) {
                    sendResponse(id, { error: chrome.runtime.lastError });
                } else {
                    sendResponse(id, { });
                }
            });
    };

    var _getUrl = function(id, pathParams, bodyParams) {
        chrome.tabs.get(_currentTabId, function(tab) {
            sendResponse(id, { value: tab.url });
        });
    };

    var _back = function(id, params) {
        var error = false;
        pizza.navigation.reset(_currentTabId, 0);
        pizza.navigation.setLoadedHandler(function(details) {
            if (!error) {
                sendResponse(id, details);
            }
        });

        chrome.tabs.executeScript(_currentTabId, {
            code: 'window.history.back();'
        }, function(response) {
            if (chrome.extension.lastError) {
                error = true;
                sendResponse(id, { error: chrome.extension.lastError });
            }
        });
    };

    var _forward = function(id, params) {
        var error = false;
        pizza.navigation.reset(_currentTabId, 0);
        pizza.navigation.setLoadedHandler(function(details) {
            if (!error) {
                sendResponse(id, details);
            }
        });

        chrome.tabs.executeScript(_currentTabId, {
            code: 'window.history.forward();'
        }, function() {
            if (chrome.extension.lastError) {
                error = true;
                sendResponse(id, { error: chrome.extension.lastError });
            }
        });
    };

    var _reload = function(id, params) {
        pizza.navigation.reset(_currentTabId, 0);
        var error = false;
        pizza.navigation.setLoadedHandler(function(details) {
            if (!error) {
                sendResponse(id, details);
            }
        });

        pizza.devtools.sendCommand("Page.reload", { url: params.url },
            function() {
                if (chrome.runtime.lastError) {
                    error = true;
                    sendResponse(id, { error: chrome.runtime.lastError });
                }
            });
    };

    var _verifyTextInternal = function(textSearch, callback) {
        chrome.tabs.executeScript(_currentTabId, {
            code: 'document.body.innerText'
        }, function(response) {
            var found = false;
            if (pizza.isArray(response)) {
                for (var i = 0; i < response.length; ++i) {
                    if (response[i].search(textSearch) != -1) {
                        found = true;
                        break;
                    }
                }
            } else {
                // If you try to access a 'data:' url there will be an error, this is
                // fine though we don't care about content loaded from chrome internals.
                var lastError = chrome.runtime.lastError;
                if (!lastError) {
                    console.log("executeScript() response not array: ", response);
                } else {
                    console.log(lastError);
                }
            }
            callback(found);
        });
    };

    var _verifyText = function(id, params) {
        var searchRegexp = null;
        if (params.text) {
            searchRegexp = new RegExp(pizza.regexEscape(params.text), 'i');
        } else {
            searchRegexp = pizza.regexFromString(params.regexp);
        }
        _verifyTextInternal(searchRegexp, function(found) {
            sendResponse(id, { value: found });
        });
    };

    var _verifyTitle = function(id, params) {
        var searchRegexp = null;
        if (params.text) {
            searchRegexp = new RegExp(pizza.regexEscape(params.text), 'i');
        } else {
            searchRegexp = pizza.regexFromString(params.regexp);
        }
        chrome.tabs.get(_currentTabId, function(tab) {
            var found = tab.title.search(searchRegexp) != -1;
            sendResponse(id, { value: found });
        });
    };

    var _getTitle = function(id, params) {
        chrome.tabs.get(_currentTabId, function(tab) {
            sendResponse(id, { value: tab.title });
        });
    };

    var _record = function(id, params) {
        var connection;
        function setupRTCMultiConnection(stream) {
            connection = new RTCMultiConnection('webrtc-tab-sharing');
            connection.bandwidth.video = false;
            connection.session = {
                video: true,
                oneway: true
            };
            connection.openSignalingChannel = openSignalingChannel;
            connection.dontAttachStream = true;
            connection.attachStreams.push(stream);
            connection.open();
        }

        // using websockets for signaling
        function openSignalingChannel(config) {
            config.channel = config.channel || this.channel;
            var websocket = new WebSocket('wss://www.webrtc-experiment.com:8563');
            websocket.onopen = function() {
                websocket.push(JSON.stringify({
                    open: true,
                    channel: config.channel
                }));
                if (config.callback) config.callback(websocket);
            };
            websocket.onmessage = function(event) {
                config.onmessage(JSON.parse(event.data));
            };
            websocket.push = websocket.send;
            websocket.send = function(data) {
                websocket.push(JSON.stringify({
                    data: data,
                    channel: config.channel
                }));
            };
        }

        chrome.tabCapture.capture({
                audio: true,
                video: true },
            function(stream) {
                var src = window.URL.createObjectURL(stream);
                console.log(src);
                console.log(stream);
                setupRTCMultiConnection(stream);
                sendResponse(id, { url: src, success: true });
            });
    };

    function devtoolsCaptureScreenshot(id, params) {
         pizza.devtools.sendCommand('Page.captureScreenshot', null, function(response) {
             if (response.wasThrown) {
                 sendResponse(id, { error: formatWasThrownException(response) });
             } else if (response.data) {
                 console.time("encode");
                 var rawData = atob(response.data);
                 var ab = new Uint8Array(rawData.length);
                 for (var i = 0; i < rawData.length; i++) {
                     ab[i] = rawData.charCodeAt(i);
                 }
                 console.timeEnd("encode");
                 _binaryResponseHandler(ab);
                 sendResponse(id, {});
             } else {
                 sendResponse(id, { error: response.message });
             }
         });
    }

    var _screenshot = function(id, params) {
        console.time("capture");
        var format = params.format;
        if (!format) {
            format = "png";
        }

        function convertToBinaryAndSend(dataUrl) {
            console.time("encode");
            var marker = "base64,";
            var base64begin = dataUrl.indexOf(marker);
            base64begin += marker.length;
            var s = dataUrl.substr(base64begin);
            var rawData = atob(s);
            var ab = new Uint8Array(rawData.length);
            for (var i = 0; i < rawData.length; i++) {
                ab[i] = rawData.charCodeAt(i);
            }
            console.timeEnd("encode");
            _binaryResponseHandler(ab);
            sendResponse(id, {});
        }

        // We scale down by devicePixelRatio, so we don't get huge image files
        var scale = window.devicePixelRatio;
        var deviceInfo = pizza.emulation.getDevice();

        var renderToCanvas = (format == "webp" || scale > 1.0 || deviceInfo != null);
        var captureVisTabOptions;
        if (renderToCanvas) {
            captureVisTabOptions = {format: "jpeg" };
        } else {
            captureVisTabOptions = { format: format };
            if (format === "jpeg" && params.quality) {
                captureVisTabOptions.quality = Math.floor(params.quality * 100);
            }
        }

        chrome.tabs.captureVisibleTab(null, captureVisTabOptions, function(dataUrl) {
            console.timeEnd("capture");
            var lastError = chrome.runtime.lastError;
            if (lastError) {
                if (lastError.message && lastError.message.indexOf("unknown error") != -1) {
                    sendResponse(id, {error: "Failed to capture tab: window has to be visible"});
                } else {
                    sendResponse(id, {error: lastError});
                }
            } else if (renderToCanvas) {
                console.time("draw");
                var canvas = document.createElement('canvas');
                var img = new Image();
                img.onload = function() {
                    if (deviceInfo) {
                        var srcWidth = deviceInfo.deviceMetrics.width;
                        var srcHeight = deviceInfo.deviceMetrics.height;
                        canvas.width = srcWidth;
                        canvas.height = srcHeight;
                        canvas.getContext('2d').drawImage(img,
                            0, 0, srcWidth * scale, srcHeight * scale,
                            0, 0, canvas.width, canvas.height);
                    } else {
                        canvas.width = img.width / scale;
                        canvas.height = img.height / scale;
                        canvas.getContext('2d').drawImage(img, 0, 0, canvas.width, canvas.height);
                    }
                    var quality = 1.0;
                    if (params.quality) {
                        quality = params.quality;
                    }
                    var format = params.format;
                    var newDataUrl = canvas.toDataURL('image/' + format, quality);
                    console.timeEnd("draw");
                    convertToBinaryAndSend(newDataUrl);
                };
                img.src = dataUrl;
            } else {
                // No scaling/cropping required just return image directly
                convertToBinaryAndSend(dataUrl);
            }
        });
    };

    var _executeJQuery = function(id, params) {
        var script = "(" + function() {
            var injected = function() {
                var s = "##script##";
                var r = eval(s);
                console.log(r);
                return r;
            };

            if (typeof(pizza) == "undefined") {
                pizza = { state:"LOADING", result: null };
                pizza.include = function(url) {
                    var xmlHttp = new XMLHttpRequest();
                    xmlHttp.onreadystatechange = function() {
                        if (xmlHttp.readyState == 4) {
                            if (xmlHttp.status == 200) {
                                try {
                                    eval(xmlHttp.responseText);
                                    pizza.state = "LOADED";
                                } catch (e) {
                                    console.log(e);
                                }
                            }
                        }
                    };
                    xmlHttp.open("GET", url, true);
                    xmlHttp.send();
                };
                pizza.include(chrome.extension.getURL("jquery-2.1.1.js"));
            } else if (pizza.state === "LOADED") {
                try {
                    pizza.state = 'DONE';
                    pizza.result = injected();
                } catch (e) {
                    pizza.state = 'ERROR';
                    pizza.result = e.toString();
                }
            }
            return [pizza.state, pizza.result];
        } + ")();";

        script = script.replace(/##script##/, params.script);

        // Has to be called twice to load jquery into any frames that don't have jquery loaded.
        // If JQuery is loaded this works the first time.
        function executeRound() {
            chrome.tabs.executeScript(_currentTabId, { code: script, allFrames: true }, function (resultArray) {
                if (chrome.extension.lastError) {
                    sendResponse(id, { error: chrome.extension.lastError });
                } else {
                    var isDone = true;
                    for (var i = 0; i < resultArray.length; ++i) {
                        var state = resultArray[i][0];
                        if (state !== 'DONE' && state != 'ERROR') {
                            isDone = false;
                            break;
                        }
                    }
                    if (!isDone) {
                        return executeRound();
                    } else  {
                        var result = [];
                        for (var j = 0; j < resultArray.length; ++j) {
                            if (resultArray[j]) {
                                result.push(resultArray[j][1]);
                            } else {
                                result.push(null);
                            }
                        }
                        if (result.length == 1) {
                            sendResponse(id, { value: result[0] });
                        } else {
                            sendResponse(id, { value: result });
                        }
                    }
                }
            });
        }

        // Reset any stored values
        var resetScript = "(" + function() {
            if (typeof(pizza) !== "undefined") {
                pizza.result = null;
                pizza.state = 'LOADED';
            }
        } + ")()";

        chrome.tabs.executeScript(_currentTabId, { code: resetScript, allFrames: true }, function (resultArray) {
            if (chrome.extension.lastError) {
                sendResponse(id, { error: chrome.extension.lastError });
            } else {
                executeRound();
            }
        });
    };

    var _execute = function(id, params) {
        _currentContextId = pizza.contexttracker.getContextIdForFrame(_currentTabId, _currentFrameId);

        var loc = {
            expression: params.script,
            contextId: _currentContextId
        };

        pizza.devtools.sendCommand('Runtime.evaluate', loc, function(response) {
            if (response.wasThrown) {
                console.log(response);
                sendResponse(id, { error: formatWasThrownException(response) });
            } else if (response.result) {
                sendResponse(id, response.result);
            } else {
                console.log(response);
                sendResponse(id, { error: response.message });
            }
        });
    };

    var _selectFrame = function(id, params) {
        pizza.frametracker.queryFrameFrameSelector(params.selector, _currentFrameId, function(response) {
            if (response.value) {
                _currentFrameId = response.value.frameId;
                _currentContextId = null;
                _automationAPI = null;
            }
            sendResponse(id, response);
        });
    };

    var _selectFrameCss = function(id, params) {
        pizza.frametracker.queryFrame(params.selector, _currentFrameId, function(response) {
            if (response.value) {
                _currentFrameId = response.value.frameId;
                _currentContextId = null;
                _automationAPI = null;
            }
            sendResponse(id, response);
        });
    };

    var _selectTopFrame = function(id, params) {
        _currentFrameId = null;
        _currentContextId = null;
        _automationAPI = null;
        sendResponse(id, { });
    };

    var _listFrames = function(id, params) {
        var selector = null;
        if (params && params.selector) {
            selector = params.selector;
        } else {
            selector = "frame,iframe";
        }
        pizza.frametracker.queryFrames(selector, _currentFrameId, function(response) {
            sendResponse(id, response);
        });
    };

    var _listAllFrames = function(id, params) {
        pizza.frametracker.queryAllFrames("frame,iframe", function(response) {
            sendResponse(id, response);
        });
    };

    var _blockUrl = function(id, params) {
        var urls = [];

        var url = params.url;
        if (pizza.isArray(url)) {
            for (var i = 0; i < url.length; ++i) {
                urls.push(url[i]);
            }
        } else {
            urls.push(url);
        }

        var cancel = function(details) { return {cancel: true}; };
        _webRequestModifyCallbacks.push(cancel);

        chrome.webRequest.onBeforeRequest.addListener(
            cancel,
            { urls: urls },
            [ "blocking" ]);

        sendResponse(id, {});
    };

    // Strip the '/'s from the beginning and end of a regex string,
    // so that the resulting string can be used in RegExp() constructor.
    function cleanRegexp(regex) {
        var firstIndex = regex.indexOf("/");
        var lastIndex = regex.lastIndexOf("/");
        if (firstIndex < 0 || lastIndex <= firstIndex) {
            return regex;
        } else {
            return regex.substring(firstIndex + 1, lastIndex);
        }
    }

    function stripMatchGroups(url) {
        var PARSE = 1;
        var ESCAPE = 2;
        var state = PARSE;
        var depth = 0;
        var r = "";
        for (var i = 0; i < url.length; ++i) {
            var c = url[i];
            switch (state) {
                case PARSE:
                    if (c == '\\') {
                        state = "ESCAPE";
                    } else if (c == '(') {
                        depth++;
                    } else if (c == ')') {
                        depth--;
                    } else {
                        r += c;
                    }
                    break;
                case ESCAPE:
                    r += c;
                    state = PARSE;
                    break;
            }
        }
        return r;
    }

    function regexpifyGlobWithMatchGroups(url) {
        var PARSE = 1;
        var ESCAPE = 2;
        var state = PARSE;
        var r = "";

        function addChar(c) {
            switch (c) {
                case '*':
                    r += ".*";
                    break;
                case '[':
                case ']':
                case '(':
                case ')':
                case '.':
                case '+':
                case '{':
                case '}':
                    r += "\\" + c;
                    break;
                default:
                    r += c;
            }
        }

        for (var i = 0; i < url.length; ++i) {
            var c = url[i];
            switch (state) {
                case PARSE:
                    if (c == '\\') {
                        state = "ESCAPE";
                    } else if (c == '(' || c == ')') {
                        r += c;
                    } else {
                        addChar(c);
                    }
                    break;
                case ESCAPE:
                    addChar(c);
                    state = PARSE;
                    break;
            }
        }

        return new RegExp(r);
    }

    var _rewriteUrl = function(id, params) {
        var origUrl = params.url;

        var glob = stripMatchGroups(origUrl);
        var match = regexpifyGlobWithMatchGroups(origUrl);

        console.log(glob, match, origUrl);
        var rewriteRequest = function(details) {
            var url = details.url;
            console.log(url);
            if (url.match(match)) {
                return {redirectUrl: url.replace(match, params.rewriteUrl) };
            } else {
                return {};
            }
        };
        _webRequestModifyCallbacks.push(rewriteRequest);

        chrome.webRequest.onBeforeRequest.addListener(
            rewriteRequest,
            { urls: [glob] },
            [ "blocking" ]);

        sendResponse(id, {});
    };

    var _clearRules = function(id, params) {
        for (var i = 0; i < _webRequestModifyCallbacks.length; ++ i) {
            var o = _webRequestModifyCallbacks[i];
            chrome.webRequest.onBeforeRequest.removeListener(o);
        }
        _webRequestModifyCallbacks = [];

        sendResponse(id, {});
    };

    var _setHeader = function(id, params) {
        _headers[params.name] = params.value;
        _setAllHeaders(id)
    };

    var _removeHeader = function(id, params) {
        delete _headers[params.name];
        _setAllHeaders(id);
    };

    var _removeAllHeaders = function(id, params) {
        _headers = { };
        _setAllHeaders(id);
    };

    var _setAuth = function(id, params) {
        pizza.network.setAuth(params.username, params.password);
        sendResponse(id, {});
    };

    var _setAllHeaders = function(id) {
        pizza.devtools.sendCommand(
            'Network.setExtraHTTPHeaders',
            { 'headers': _headers },
            function(response) {
                if (response.error) {
                    sendResponse(id, { error: response.error });
                } else {
                    sendResponse(id, {});
                }
            });
    };

    var _setUserAgent = function(id, params) {
        pizza.devtools.sendCommand(
                     'Network.setUserAgentOverride',
                     { 'userAgent': params.userAgent },
                     function(response) {
            if (response.error) {
                sendResponse(id, { error: response.error });
            } else {
                sendResponse(id, {});
            }
        });
    };

    var _clearCache = function(id, params) {
        pizza.devtools.sendCommand(
            'Network.clearBrowserCache',
            null,
            function(response) {
                if (response.error) {
                    sendResponse(id, { error: response.error });
                } else {
                    sendResponse(id, {});
                }
            });
    };

    var _clearCookies = function(id, params) {
        pizza.devtools.sendCommand(
            'Network.clearBrowserCookies',
            null,
            function(response) {
                if (response.error) {
                    sendResponse(id, { error: response.error });
                } else {
                    sendResponse(id, {});
                }
            });
    };

    function setCookie(id, cookie) {
        chrome.cookies.set(cookie, function() {
            if (!relayChromeError(id)) {
                sendResponse(id, {});
            }
        });
    }

    var _setCookie = function(id, params) {
        var cookie;
        if (params.details) {
            cookie = params.details;
        } else {
            cookie = {};
        }
        cookie.name = params.name;
        cookie.value = params.value;
        if (cookie.url) {
            setCookie(id, cookie);
        } else {
            getUrl(id, function(url) {
                cookie.url = url;
                setCookie(id, cookie);
            });
        }
    };

    function getCookie(id, name, url) {
        chrome.cookies.get({ url: url, name: name }, function(cookie) {
            if (!relayChromeError(id)) {
                sendResponse(id, { value: cookie });
            }
        });
    };

    var _getCookie = function(id, params) {
        if (params.url) {
            getCookie(id, params.name, params.url);
        } else {
            getUrl(id, function(url) {
                getCookie(id, params.name, url);
            });
        }
    };

    function getUrl(id, callback) {
        chrome.tabs.executeScript(_currentTabId, { code: "document.URL", allFrames: false },
            function (resultArray) {
                if (!relayChromeError(id)) {
                    if (resultArray.length < 1) {
                        sendResponse(id, { error: "No URL set, and no URL returned" });
                    } else {
                        callback(resultArray[0]);
                    }
                }
            });
    }

    var _removeCookie = function(id, params) {
        if (params.url) {
            removeCookie(id, { name: params.name, url: params.url });
        } else {
            getUrl(id, function(url) {
                removeCookie(id, { url: url, name: params.name });
            });
        }
    };

    function removeCookie(id, cookie) {
        chrome.cookies.remove(cookie, function() {
            if (!relayChromeError(id)) {
                sendResponse(id, {});
            }
        });
    }

    var _listCookies = function(id, params) {
        chrome.cookies.getAll({ }, function(cookies) {
            sendResponse(id, { value: cookies });
        });
    };

    var _listTabs = function(id, params) {
        chrome.tabs.query({ }, function(tabs) {
            sendResponse(id, { value: _filterTabs(tabs) });
        });
    };

    var _sortTabs = function(tabs) {
        return tabs.sort(function(a, b) {
            if (a.id < b.id) {
                return -1;
            } else if (a.id == b.id) {
                return 0;
            } else {
                return 1;
            }
        });
    };

    var _filterTabs = function(tabs) {
        var newTabs = [];
        tabs = _sortTabs(tabs);
        for (var i = 0; i < tabs.length; ++i) {
            var tab = tabs[i];
            newTabs.push({
                "active": tab.active,
                "index": i,
                "title": tab.title,
                "url": tab.url,
                "width": tab.width,
                "height": tab.height,
                "status": tab.status
            });
        }
        return newTabs;
    };

    function selectTab(id, windowId, tabId) {
        chrome.windows.update(windowId, { focused: true }, function(window) {
            chrome.tabs.update(tabId, { active: true }, function(tab) {
                pizza.devtools.setTab(tabId);
                sendResponse(id, { value: convertTab(tab) });
            });
        });
    }

    var _selectTab = function(id, params) {
        var selector = params.tab;
        var selectors = ['index', 'title', 'url'];
        if (!pizza.hasAtLeastOneProperty(selector, selectors)) {
            sendResponse(id,
                { error: "Unable to find tab: selector must be one of [\'" + selectors.join("\', \'") + "\']" });
            return;
        }
        pizza.navigation.reset(_currentTabId, 0);

        chrome.tabs.query({ }, function(tabs) {
            if (tabs.length <= 0) {
                sendResponse(id,
                    { error: "Unable to find tab " + JSON.stringify(selector) + ": No tabs" });
                return;
            }
            tabs = _sortTabs(tabs);
            var tabId = null;
            var windowId = null;
            var tab = null;
            if (selector.hasOwnProperty("index")) {
                if (selector.index < 0) {
                    sendResponse(id,
                        { error: "Unable to find tab " + JSON.stringify(selector) + ": Index must be >= 0" });
                    return;
                } else if (selector.index >= tabs.length) {
                    sendResponse(id,
                        { error: "Unable to find tab " + JSON.stringify(selector) + ": Only " + tabs.length + " tab(s)" });
                    return;
                } else {
                    tab = tabs[selector.index];
                    tabId = tab.id;
                    windowId = tab.windowId;
                }
            } else {
                for (var i = 0; i < tabs.length; ++i) {
                    tab = tabs[i];
                    var selectorToMatch = null;
                    var tabParamToMatch = null;
                    if (selector.title) {
                        tabParamToMatch = tab.title;
                        selectorToMatch = selector.title;
                    } else {
                        tabParamToMatch = tab.url;
                        selectorToMatch = selector.url;
                    }

                    if (selector.full) {
                        if (tabParamToMatch === selectorToMatch) {
                            tabId = tab.id;
                        }
                    } else if (selector.regex) {
                        if (tabParamToMatch.match(selectorToMatch)) {
                            tabId = tab.id;
                        }
                    } else {
                        if (tabParamToMatch.indexOf(selectorToMatch) >= 0) {
                            tabId = tab.id;
                        }
                    }

                    if (tabId != null) {
                        windowId = tab.windowId;
                        break;
                    }
                }
            }
            if (tabId != null) {
                selectTab(id, windowId, tabId);
            } else {
                sendResponse(id, { error: "Unable to find tab " + JSON.stringify(selector) });
            }
        });
    };

    var _selectMainTab = function(id, params) {
        pizza.navigation.reset(_currentTabId, 0);
        chrome.tabs.query({ }, function(tabs) {
            selectTab(id, tabs[0].windowId, tabs[0].id);
        });
    };

    var _selectLatestTab = function(id, params) {
        pizza.navigation.reset(_currentTabId, 0);
        chrome.tabs.query({ }, function(tabs) {
            var lastWindowId = 0;
            var lastId = 0;
            for (var i = 0; i < tabs.length; ++i) {
                var tab = tabs[i];
                if (tab.id > lastId) {
                    lastId = tab.id;
                    lastWindowId = tab.windowId;
                }
            }
            selectTab(id, lastWindowId, lastId);
        });
    };

    function convertTab(tab) {
        return {
            active: tab.active,
            url: tab.url,
            title: tab.title,
            width: tab.width,
            height: tab.height,
            status: tab.status
        };
    }

    var _newTab = function(id, params) {
        pizza.network.suppressNextNavigation();
        chrome.tabs.create({ url: "about:blank" }, function(tab) {
            sendResponse(id, { value: convertTab(tab) });
        });
    };

    function injectAutomationAPI(callback) {
        if (_automationAPI) {
            callback(_automationAPI);
        } else {
            _currentContextId =
                pizza.contexttracker.getContextIdForFrame(_currentTabId, _currentFrameId);

            var loc = {
                expression: "(" + pizza.automation.init.toString() + ")();",
                contextId: _currentContextId
            };

            pizza.devtools.sendCommand('Runtime.evaluate', loc, function (response) {
                if (response.error) {
                    // Runtime.evaluate threw an error before executing the script
                    callback({error: response.error});
                } else if (response.message) {
                    // Runtime.evaluate threw an error before executing the script
                    callback({error: response.message});
                } else if (response.wasThrown) {
                    // The script threw an error
                    callback({ error: formatWasThrownException(response) });
                } else if (response.result) {
                    _automationAPI = response.result.objectId;
                    callback(_automationAPI);
                } else {
                    callback(id, { error: "no response for injectAutomationAPI" });
                }
            });
        }
    }

    function formatWasThrownException(response) {
        var t;
        if (response.result.value) {
            return response.result.value;
        } else if (response.exceptionDetails) {
            t = response.exceptionDetails.text;
            var uncaught = "Uncaught ";
            if (t.indexOf(uncaught) == 0) {
                t = t.substr(uncaught.length);
            }
        } else {
            t = response.result.description;
            var stackTraceBeginIx = t.indexOf("\n    at");
            if (stackTraceBeginIx > 0) {
                t = t.substr(0, stackTraceBeginIx);
            }
        }
        return t;
    }

    function executeAutomationAPI(callback, errorCallback, byValue, script) {
        var args = [];
        for (var i = 4; i < arguments.length; ++i) {
            var o = arguments[i];
            var arg = { value: o };
            if (pizza.isString(o)) {
                arg.type = "string";
            } else if (pizza.isNumber(o)) {
                arg.type = "number";
            } else {
                arg.type = "object";
                arg.value = JSON.stringify(o);
            }
            args.push(arg);
        }
        injectAutomationAPI(function(response) {
            if (response && response.error) {
                errorCallback(response.error);
            } else {
                var loc = {
                    objectId: response,
                    functionDeclaration: script,
                    arguments: args,
                    returnByValue: byValue
                };
                pizza.devtools.sendCommand('Runtime.callFunctionOn', loc, function (response) {
                    if (response.message) {
                        errorCallback(response.message);
                    } else if (response.wasThrown) {
                        console.log(response);
                        errorCallback(formatWasThrownException(response));
                    } else {
                        try {
                            callback(response);
                        } catch (e) {
                            console.log(e);
                            errorCallback(e.message);
                        }
                    }
                });
            }
        });
    }

    var _click = function(id, params) {
        executeAutomationAPI(
            function(response) {
                var pos = response.result.value;
                if (pos.left < 0 || pos.top < 0 ||
                    pos.width <= 0 || pos.height <= 0) {
                    sendResponse(id, { error: "Unable to click element at " + JSON.stringify(pos) });
                } else {
                    chrome.tabs.getZoom(_currentTabId, function(zoomFactor) {
                        pos.top *= zoomFactor;
                        pos.left *= zoomFactor;
                        pos.width *= zoomFactor;
                        pos.height *= zoomFactor;

                        var x = Math.floor(pos.left + ((params.x) ? params.x : (pos.width / 2)));
                        var y = Math.floor(pos.top + ((params.y) ? params.y : (pos.height / 2)));
                        pizza.input.click(x, y, function () {
                            sendResponse(id, {value: {}});
                        });
                    });
                }
            },
            function(error) { sendResponse(id, { error: error }); },
            true,
            "function(selector) { return this.getElementRegion(selector); }",
            params.selector);
    };

    var _hover = function(id, params) {
        executeAutomationAPI(
            function(response) {
                var pos = response.result.value;
                if (pos.left < 0 || pos.top < 0 ||
                    pos.width <= 0 || pos.height <= 0) {
                    sendResponse(id, { error: "Unable to move mouse to element at " + JSON.stringify(pos) });
                } else {
                    chrome.tabs.getZoom(_currentTabId, function(zoomFactor) {
                        pos.top *= zoomFactor;
                        pos.left *= zoomFactor;
                        pos.width *= zoomFactor;
                        pos.height *= zoomFactor;

                        var x = Math.floor(pos.left + ((params.x) ? params.x : (pos.width / 2)));
                        var y = Math.floor(pos.top + ((params.y) ? params.y : (pos.height / 2)));
                        pizza.input.mouseMove(x, y, function () {
                            sendResponse(id, {value: {}});
                        });
                    });
                }
            },
            function(error) { sendResponse(id, { error: error }); },
            true,
            "function(selector) { return this.getElementRegion(selector); }",
            params.selector);
    };

    var _focus = function(id, params) {
        // Focus the element using injected JS
        executeAutomationAPI(
            function(response) {
                sendResponse(id, { value: {} });
            },
            function(error) { sendResponse(id, { error: error }); },
            true,
            "function(selector) { return this.focus(selector); }",
            params.selector);
    };

    var _type = function(id, params) {
        // Focus the element using injected JS, then type using the
        // Devtools automation/input APIs
        executeAutomationAPI(
            function(response) {
                pizza.input.type(params.text, function () {
                    sendResponse(id, { value: {} });
                });
            },
            function(error) { sendResponse(id, { error: error }); },
            true,
            "function(selector) { return this.focus(selector); }",
            params.selector);
    };

    var _clear = function(id, params) {
        executeAutomationAPI(
            function(response) { sendResponse(id, { value: {} }); },
            function(error) { sendResponse(id, { error: error }); },
            true,
            "function(selector) { var e = this.findElement(selector); e.value = \"\"; }",
            params.selector
        );
    };

    var _selectContent = function(id, params) {
        executeAutomationAPI(
            function(response) { sendResponse(id, { value: {} }); },
            function(error) { sendResponse(id, { error: error }); },
            true,
            "function(selector, checked) { var e = this.findElement(selector); e.select(); }",
            params.selector
        );
    };

    var _check = function(id, params) {
        var checkState = true;
        if (params.hasOwnProperty("check")) {
            checkState = params.check;
        }
        executeAutomationAPI(
            function(response) { sendResponse(id, { value: {} }); },
            function(error) { sendResponse(id, { error: error }); },
            true,
            "function(selector, checked) { var e = this.findElement(selector); e.checked = checked; }",
            params.selector,
            checkState
        );
    };

    var _getValue = function(id, params) {
        var script = "" + function(selector) {
          var e = this.findElement(selector);
          if (e.tagName == 'SELECT' && e.hasAttribute('multiple')) {
            var a = e.selectedOptions;
            var v = [];
            for (var i = 0; i < a.length; ++i) {
              v.push(a[i].value);
            }
            return v;
          } else {
            return e.value;
          }
        };

        executeAutomationAPI(
            function(response) { sendResponse(id, { value: response.result.value }); },
            function(error) { sendResponse(id, { error: error }); },
            true,
            script,
            params.selector
        );
    };

    var _setValue = function(id, params) {
        var script = "" + function(selector, value) {
          var e = this.findElement(selector);
          e.value = value;
        };

        executeAutomationAPI(
            function(response) { sendResponse(id, { value: {} }); },
            function(error) { sendResponse(id, { error: error }); },
            true,
            script,
            params.selector,
            params.value
        );
    };

    var scriptSelectSingle = "" + function(selector, jsonParams) {
      var i, o;
      var e = this.findElement(selector);
      var params = JSON.parse(jsonParams);
      if (params.index) {
        i = params.index;
        if (i >= 0 && i < e.options.length) {
          o = e.options[i];
          o.selected = true;
          return;
        }
        throw "Error: Unable to find option with index '" + i + "'!";
      }
      var match;
      if (params.match) {
        match = new RegExp(params.match);
      }
      for (i = 0; i < e.options.length; ++i) {
        o = e.options[i];
        if (params.text && o.text == params.text ||
              params.value && o.value == params.value ||
              match && o.text && o.text.match(match)) {
          o.selected = true;
          return;
        }
      }
      if (params.text) {
        throw "Error: Unable to find option with text '" + params.text + "'!";
      } else if (params.value) {
        throw "Error: Unable to find option with value '" + params.value + "'!";
      } else if (params.match) {
         throw "Error: Unable to find option with text that matches " + params.match + "!";
      } else {
        throw "Error: Unable to find option!";
      }
    };

    var scriptSelectMultiple = "" + function(selector, jsonParams) {
      var e = this.findElement(selector);
      var a = null;
      var params = JSON.parse(jsonParams);
      if (params.text) {
        a = params.text;
      } else if (params.value) {
        a = params.value;
      } else if (params.match) {
        a = params.match;
        for (var i = 0; i < a.length; ++i) {
          var t = a[i];
          a[i] = new RegExp(t);
        }
      } else if (params.index) {
        a = params.index;
      }
      if (params.clear) {
        for (var i = 0; i < e.options.length; ++i) {
          var o = e.options[i];
          o.selected = false;
        }
      }
      for (var i = 0; i < a.length; ++i) {
         var t = a[i];
         var set = false;
         for (var j = 0; j < e.options.length; ++j) {
           var o = e.options[j];
           console.log(t, j);
           if (params.text && t == o.text ||
                params.value && t == o.value ||
                params.match && o.text && o.text.match(t) ||
                params.index && j == t) {
             o.selected = true;
             set = true;
           }
         }
         if (!set) {
           if (params.text) {
             throw "Error: Unable to find option with text '" + t + "'!";
           } else if (params.value) {
             throw "Error: Unable to find option with value '" + t + "'!";
           } else if (params.index) {
             throw "Error: Unable to find option with index '" + t + "'!";
           } else if (params.match) {
             throw "Error: Unable to find option with text that matches " + params.match + "!";
           }
         }
      }
    };

    var _select = function(id, params) {
        var value = params.value;
        if (!value.text && !value.value && !value.index && !value.match) {
            sendResponse(id, { error: "Error: None of text, value, index or match properties set!"});
            return;
        }
        var script = null;
        if (value.text && value.text instanceof Array ||
            value.value && value.value instanceof Array ||
            value.index && value.index instanceof Array ||
            value.match && value.match instanceof Array) {
            script = scriptSelectMultiple;
        } else {
            script = scriptSelectSingle;
        }
        // Default the clear property to true
        if (!value.hasOwnProperty("clear")) {
            value.clear = true;
        }
        executeAutomationAPI(
            function(response) { sendResponse(id, { value: {} }) },
            function(error) { sendResponse(id, { error: error }); },
            true,
            script,
            params.selector,
            value);
    };

    var _submit = function(id, params) {
        executeAutomationAPI(
            function(response) { sendResponse(id, { value: {} }) },
            function(error) { sendResponse(id, { error: error }); },
            true,
            "function(selector) { return this.submitForm(selector); }",
            params.selector);
    };

    var existsScript = "" + function(selector) {
        try {
            var v = this.findElement(selector);
            return v ? true : false;
        } catch (e) {
            return false
        }
    };

    var _exists = function(id, params) {
        executeAutomationAPI(
            function(response) { sendResponse(id, { value: response.result.value }); },
            function(error) { sendResponse(id, { error: error }); },
            true,
            existsScript,
            params.selector
        );
    };

    var _waitForElement = function(id, params) {
        var pollTime = 200;
        if (params.poll) {
            pollTime = params.poll;
        }

        var check = function(selector) {
            executeAutomationAPI(
                function (response) {
                    if (response.result.value) {
                        sendResponse(id, { });
                    } else {
                        setTimeout(check, pollTime);
                    }
                },
                function (error) {
                    sendResponse(id, { error: error });
                },
                true,
                existsScript,
                params.selector
            );
        };

        check();
    };

    var _waitForText = function(id, params) {
        var textCheckScript = "" + function(selector, text) {
                try {
                    var v = this.findElement(selector);
                    if (v) {
                        var t = v.innerText;
                        if (t) {
                            return (t.indexOf(text) >= 0);
                        }
                    }
                    return false;
                } catch (e) {
                    return false
                }
            };

        var pollTime = 200;
        if (params.poll) {
            pollTime = params.poll;
        }

        var check = function() {
            executeAutomationAPI(
                function (response) {
                    if (response.result.value) {
                        sendResponse(id, { });
                    } else {
                        setTimeout(check, pollTime);
                    }
                },
                function (error) {
                    sendResponse(id, { error: error });
                },
                true,
                textCheckScript,
                params.selector,
                params.text
            );
        };

        check();
    };

    var isVisibleScript = "" + function(selector) {
        var elements = this.findElementAll(selector);
        for (var i = 0; i < elements.length; ++i) {
            var v = this.queryElementWrap(elements[i]);
            if (v.pos && !v.hiddenBy) {
                return true;
            }
        }
        return false;
    };

    var _isVisible = function(id, params) {
        executeAutomationAPI(
            function(response) { sendResponse(id, { value: response.result.value }); },
            function(error) { sendResponse(id, { error: error }); },
            true,
            isVisibleScript,
            params.selector
        );
    };

    var _waitForVisible = function(id, params) {
        var selector = params.selector;

        var pollTime = 500;
        if (params.poll) {
            pollTime = params.poll;
        }

        var check = function(selector) {
            executeAutomationAPI(
                function (response) {
                    if (response.result.value) {
                        sendResponse(id, { });
                    } else {
                        setTimeout(function () {
                            check(selector);
                        }, pollTime);
                    }
                },
                function (error) {
                    sendResponse(id, { error: error });
                },
                true,
                isVisibleScript,
                params.selector
            );
        };

        check(selector);
    };

    function fixPositionOnElements(elements, zoomFactor) {
        for (var i = 0; i < elements.length; ++i) {
            var e = elements[i];
            if (e.pos) {
                e.pos.top *= zoomFactor;
                e.pos.left *= zoomFactor;
                e.pos.width *= zoomFactor;
                e.pos.height *= zoomFactor;
            }
        }
    }

    var _query = function(id, params) {
        var script = null;
        if (params.visibleOnly) {
            script = "" + function(selector) {
                var elements = this.findElementAll(selector);
                var v = [];
                for (var i = 0; i < elements.length; ++i) {
                    var r = this.queryElementWrap(elements[i]);
                    if (r.visible) {
                        v.push(r);
                    }
                }
                return v;
            };
        } else {
            script = "" + function(selector) {
                var elements = this.findElementAll(selector);
                var v = [];
                for (var i = 0; i < elements.length; ++i) {
                    v.push(this.queryElementWrap(elements[i]));
                }
                return v;
            };
        }

        executeAutomationAPI(
            function(response) {
                chrome.tabs.getZoom(_currentTabId, function(zoomFactor) {
                    var result = response.result.value;
                    fixPositionOnElements(result, zoomFactor);
                    sendResponse(id, { value: result });
                });
            },
            function(error) { sendResponse(id, { error: error }); },
            true,
            script,
            params.selector
        );
    };

    var _highlight = function(id, params) {
        var script = "" + function(selector) {
            return this.findElement(selector);
        };

        var color = params.color;
        if (!color) {
            color = { a: 0.8, r: 255, g: 230, b: 200 };
        }
        if (!color.a) {
            color.a = 0.8;
        }
        executeAutomationAPI(
            function(response) {
                console.log(response);
                pizza.frametracker.highlight(response.result.objectId, color, function(response) {
                    console.log(response);
                    sendResponse(id, { value: {} });
                });
            },
            function(error) { sendResponse(id, { error: error }); },
            false,
            script,
            params.selector,
            params.value
        );
    };

    var _clearHighlight = function(id, params) {
        pizza.frametracker.clearHighlight();
        sendResponse(id, { value: {} });
    };

    var _getInnerText = function(id, params) {
        var script = "" + function(selector) {
            return this.findElement(selector).innerText;
        };

        executeAutomationAPI(
            function(response) { sendResponse(id, { value: response.result.value }); },
            function(error) { sendResponse(id, { error: error }); },
            true,
            script,
            params.selector
        );
    };

    var _getInnerHTML = function(id, params) {
        var script = "" + function(selector) {
            return this.findElement(selector).innerHTML;
        };

        executeAutomationAPI(
            function(response) { sendResponse(id, { value: response.result.value }); },
            function(error) { sendResponse(id, { error: error }); },
            true,
            script,
            params.selector
        );
    };

    var _clearPageLoad = function(id, params) {
        pizza.navigation.reset(_currentTabId, 0);
        sendResponse(id, { });
    };

    var _waitPageLoad = function(id, params) {
        var previousNav = pizza.navigation.previousNavigation();
        if (previousNav) {
            sendResponse(id, previousNav);
        } else {
            var timeout = -1;
            if (params.timeout) {
                timeout = params.timeout;
            }
            var timedOut = false;
            var timeoutId;
            if (timeout > 0) {
                timeoutId = setTimeout(function () {
                    timedOut = true;
                    sendResponse(id,
                        { "error": "Timeout after " + timeout + "ms while waiting for page load" });
                }, timeout);
            }

            pizza.navigation.setLoadedHandler(function(response) {
                if (timeoutId) {
                    clearTimeout(timeoutId);
                }
                if (!timedOut) {
                    sendResponse(id, response);
                }
            });
        }
    };

    var _verifyExists = function(id, params) {
        executeAutomationAPI(
            function(response) { sendResponse(id, { value: response.result.value }) },
            function(error) { sendResponse(id, { error: error }); },
            true,
            "function(selector) { return this.elementExists(selector); }",
            params.selector);
    };

    var _listDevices = function(id) {
        sendResponse(id, { value: pizza.emulation.listDevices() });
    };

    var _emulateDevice = function(id, params) {
        if (!params.hasOwnProperty('name')) {
            sendResponse(id, { error: "must specify a device to emulate"});
            return;
        }
        var wait = pizza.waitAll();
        var r = pizza.emulation.setDevice(params.name, wait);
        if (r) {
            wait.done(function() {
                sendResponse(id, {value: true});
            });
        } else {
            sendResponse(id, { error: "Unknown device: " + params.name });
        }
    };

    var _listNetworkConditions = function(id) {
        sendResponse(id, { value: pizza.emulation.listNetworkConditions() });
    };

    var _emulateNetworkCondition = function(id, params) {
        pizza.emulation.setNetworkCondition(params, function(r) {
            if (!r.error) {
                sendResponse(id, { value: r.value });
            } else {
                sendResponse(id, { error: r.error });
            }
        });
    };

    var _reset = function(id) {
        // close dialogs
        var firstTab = null;

        if (_dialogInfo) {
            dismissDialog();
        }

        _autoDismissDialogs = false;

        // close all tabs & windows but the current
        function closeTabs(next) {
            var wait = pizza.waitAll();
            wait.addCallbacksLater();
            chrome.tabs.query({}, wait.add(function(tabs) {
                for (var i = 0; i < tabs.length; ++i) {
                    var tab = tabs[i];
                    if (i == 0) {
                        firstTab = tab;
                    } else {
                        chrome.tabs.remove(tab.id, wait.add());
                    }
                }
                wait.callbacksAdded();
            }));
            wait.done(next);
        }

        function gotoAboutBlank(next) {
            pizza.devtools.suppressMessages(true);
            pizza.network.suppressNextNavigation();

            if (firstTab) {
                chrome.tabs.update(firstTab.id, { url: "about:blank"}, function() {
                    pizza.devtools.setTab(firstTab.id);
                    next();
                });
            } else {
                // open a tab if no one is open
                chrome.tabs.create({ url: "about:blank" }, function(tab) {
                    pizza.devtools.setTab(tab.id);
                    next();
                });
            }
        }

        function resetEmulation(next) {
            var wait = pizza.waitAll();
            pizza.emulation.reset(wait);
            wait.done(next);
        }

        function clearBrowsingData(next) {
            chrome.browsingData.remove({}, {
                "appcache": true,
                "cache": true,
                "cookies": true,
                "downloads": true,
                "fileSystems": true,
                "formData": true,
                "history": true,
                "indexedDB": true,
                "localStorage": true,
                "serviceWorkers": true,
                "serverBoundCertificates": true,
                "pluginData": true,
                "passwords": true,
                "webSQL": true
            }, next);
        }

        function enableMessages(next) {
            pizza.devtools.suppressMessages(false);
            next();
        }

        var operations = [
            closeTabs,
            gotoAboutBlank,
            clearBrowsingData,
            resetEmulation,
            enableMessages
        ];

        var j = 0;
        function next() {
            if (j >= operations.length) {
                return;
            }
            var op = operations[j++];
            if (j == operations.length) {
                op(function() {
                    sendResponse(id, {});
                });
            } else {
                op(next);
            }
        }

        next();
    };

    var _setTab = function(tabId) {
        _currentTabId = tabId;
        pizza.navigation.reset(_currentTabId, 0);
    };

    var _handleDialog = function(id, params) {
        validateCloseDialogParams(params);
        pizza.devtools.sendCommand("Page.handleJavaScriptDialog", params,
            function(response) {
                var lastError = chrome.runtime.lastError;
                if (lastError) {
                    sendResponse(id, { error: lastError });
                } else {
                    _dialogInfo = null;
                    sendResponse(id, { });
                }
            });
    };

    function validateCloseDialogParams(params) {
        if (_dialogInfo) {
            // If we don't accept alert dialogs Chrome on Mac crashes
            if (_dialogInfo.type == "alert") {
                params.accept = true;
            }
        }
    }

    function dismissDialog(callback) {
        if (!callback) {
            callback = function(response) {
                var lastError = chrome.runtime.lastError;
                if (lastError) {
                    console.log(lastError);
                } else {
                    _dialogInfo = null;
                }
            };
        }

        var params = { accept: false };
        validateCloseDialogParams(params);
        pizza.devtools.sendCommand("Page.handleJavaScriptDialog", params, callback);
    }

    var _dismissDialogs = function(id, params) {
        _autoDismissDialogs = true;
        if (_dialogInfo) {
            dismissDialog(function(response) {
                var lastError = chrome.runtime.lastError;
                if (lastError) {
                    sendResponse(id, { error: lastError });
                } else {
                    _dialogInfo = null;
                    sendResponse(id, { });
                }
            });
        } else {
            sendResponse(id, { });
        }
    };

    var _isDialogOpen = function(id, params) {
        sendResponse(id, { value: (_dialogInfo != null) });
    };

    var _getOpenDialog = function(id, params) {
        sendResponse(id, { value: _dialogInfo });
    };

    /**
     * This should be a content script, so it contact the extension directly
     * Once you can call chrome.tab.execute() with a frameId we can add it here:
     *
     *   https://code.google.com/p/chromium/issues/detail?id=63979
     */
    var _findElementByClick = function() {

        var script = "" + function(zoomFactor) {
            if (!this.findElementListener) {
                var that = this;
                this.findElementListener = function (e) {
                    e.stopPropagation();
                    e.preventDefault();
                    if (e.target) {
                        var details = that.queryElementWrap(e.target);
                        if (details.pos) {
                            details.pos.top *= zoomFactor;
                            details.pos.left *= zoomFactor;
                            details.pos.width *= zoomFactor;
                            details.pos.height *= zoomFactor;
                        }
                        window.postMessage({ type: "PizzaElementSelected", msg: details }, window.location);
                    }
                    if (that.findElementListener) {
                        document.removeEventListener('click', that.findElementListener, true);
                        that.findElementListener = null;
                    }
                };
                document.addEventListener('click', this.findElementListener, true);
            }
        };

        chrome.tabs.getZoom(_currentTabId, function(zoomFactor) {
            executeAutomationAPI(
                function(response) { },
                function(error) { console.log(error); },
                true,
                script,
                zoomFactor);
        });
    };

    function handleEvent(method, params) {
        switch (method) {
            case 'Page.javascriptDialogOpening':
                _dialogInfo = {
                    'type': params.type,
                    'message': params.message
                };
                if (_autoDismissDialogs) {
                    dismissDialog();
                } else if (pizza.navigation.isNavigationPending(_currentTabId)) {
                    pizza.navigation.error({
                        error: "JavaScript " + params.type + " \"" + params.message + "\" interrupted navigation. " +
                        "See Browser.dismissDialogs() to find out how to ignore this error."
                    });
                    dismissDialog();
                }
                break;
            case 'Page.javascriptDialogClosed':
                _dialogInfo = null;
                break;
            default:
                // do nothing
                break;
        }
    }

    addCommand("open", _open);
    addCommand("openAsync", _openAsync);
    addCommand("getUrl", _getUrl);
    addCommand("back", _back);
    addCommand("forward", _forward);
    addCommand("reload", _reload);

    addCommand("verifyText", _verifyText);
    addCommand("getInnerText", _getInnerText);
    addCommand("waitForText", _waitForText);

    addCommand("verifyTitle", _verifyTitle);
    addCommand("getTitle", _getTitle);

    addCommand("record", _record);
    addCommand("screenshot", _screenshot);

    addCommand("selectFrame", _selectFrame);
    addCommand("selectFrameCss", _selectFrameCss);
    addCommand("selectTopFrame", _selectTopFrame);
    addCommand("listFrames", _listFrames);
    addCommand("listAllFrames", _listAllFrames);

    addCommand("execute", _execute);
    addCommand("jq", _executeJQuery);

    addCommand("blockUrl", _blockUrl);
    addCommand("clearRules", _clearRules);
    addCommand("rewriteUrl", _rewriteUrl);
    addCommand("setHeader", _setHeader);
    addCommand("removeHeader", _removeHeader);
    addCommand("setUserAgent", _setUserAgent);
    addCommand("removeAllHeaders", _removeAllHeaders);
    addCommand("setAuth", _setAuth);

    addCommand("clearCookies", _clearCookies);
    addCommand("clearCache", _clearCache);

    addCommand("setCookie", _setCookie);
    addCommand("getCookie", _getCookie);
    addCommand("removeCookie", _removeCookie);
    addCommand("listCookies", _listCookies);

    addCommand("listTabs", _listTabs);
    addCommand("selectTab", _selectTab);
    addCommand("selectMainTab", _selectMainTab);
    addCommand("selectLatestTab", _selectLatestTab);
    addCommand("newTab", _newTab);

    addCommand("click", _click);
    addCommand("focus", _focus);
    addCommand("hover", _hover);
    addCommand("type", _type);
    addCommand("clear", _clear);
    addCommand("selectContent", _selectContent);
    addCommand("check", _check);
    addCommand("getValue", _getValue);
    addCommand("setValue", _setValue);
    addCommand("select", _select);
    addCommand("submit", _submit);

    addCommand("exists", _exists);
    addCommand("waitForElement", _waitForElement);
    addCommand("isVisible", _isVisible);
    addCommand("waitForVisible", _waitForVisible);
    addCommand("query", _query);

    addCommand("clearHighlight", _clearHighlight);
    addCommand("highlight", _highlight);

    addCommand("getInnerHTML", _getInnerHTML);

    addCommand("clearPageLoad", _clearPageLoad);
    addCommand("waitPageLoad", _waitPageLoad);

    addCommand("verifyExists", _verifyExists);

    addCommand("listDevices", _listDevices);
    addCommand("emulateDevice", _emulateDevice);

    addCommand("handleDialog", _handleDialog);
    addCommand("dismissDialogs", _dismissDialogs);
    addCommand("isDialogOpen", _isDialogOpen);
    addCommand("getOpenDialog", _getOpenDialog);

    addCommand("listNetworkConditions", _listNetworkConditions);
    addCommand("emulateNetworkCondition", _emulateNetworkCondition);

    addCommand("reset", _reset);

    return {
        handleCommand: _handleCommand,
        handleEvent: handleEvent,
        setResponseHandler: _setResponseHandler,
        setBinaryResponseHandler: _setBinaryResponseHandler,
        setTab: _setTab,
        findElementByClick: _findElementByClick,
        commands: _commandMap
    };
};
