/**
 * Track JavaScript contexts are they are created.
 *
 * Associate frameIds with contextIds using Devtools Runtime.* APIs
 */
pizza.main.contexttracker = function() {
    var _frameToContextMap = {};
    var _contextDestroyedHandlers = [];

    function isInjectedExtensionTab(params) {
        // Pre Chrome 52 check for context type
        if (params.context.type && params.context.type == "Extension") {
            return true;
        }
        // Chrome 52 & 53 we can just use 'isDefault' which is exactly for this purpose
        if (params.context.isDefault != undefined && !params.context.isDefault) {
            return true;
        }
        // Chrome 54 isDefault has moved to auxData
        if (params.context.auxData && !params.context.auxData.isDefault) {
            return true;
        }
        return false;
    }

    var _executionContextCreated = function(params) {
        // Ignore injected extension tabs.  We inject our own scripting context and so can other
        // extensions.  We are not interested in these and we don't want to overwrite the main
        // context, as we use this as the default context target for commands.execute().
        if (isInjectedExtensionTab(params)) {
            return;
        }

        var tab = _frameToContextMap[params.tabId];
        if (!tab) {
            tab = { };
            _frameToContextMap[params.tabId] = tab;
        }

        // First frame overrides as it usually the main frame
        // except in cases where it is destroyed
        var frameId;
        if (params.context.auxData) {
            frameId = params.context.auxData.frameId;
        } else {
            frameId = params.context.frameId;
        }
        var frameIdSplit = frameId.split(".");
        if (frameIdSplit.length > 1) {
            if (frameIdSplit[1] === "1") {
                tab.firstContext = params.context.id;
                console.log("Setting main context", tab.firstContext);
            }
        }

        if (!tab.firstContext) {
            tab.firstContext = params.context.id;
            console.log("Setting main context", tab.firstContext);
        }

        tab[params.context.id] = frameId;
    };

    var _executionContextDestroyed = function(params) {
        for (var i = 0; i < _contextDestroyedHandlers.length; ++i) {
            _contextDestroyedHandlers[i].apply(null, [params.executionContextId]);
        }
        var tab = _frameToContextMap[params.tabId];
        if (tab) {
            delete tab[params.executionContextId];
            if (tab.firstContext == params.executionContextId) {
                console.log("Main context deleted", params.executionContextId);
                delete tab.firstContext;
                var firstId = null;
                for (var id in tab) {
                    if (!firstId || (pizza.isNumber(id) && id < firstId)) {
                        firstId = id;
                    }
                }
                if (firstId) {
                    tab.firstContext = firstId;
                }
            }
        }
    };

    var _executionContextsCleared = function(params) {
        if (params.parentId) {
            return;
        }
        _frameToContextMap[params.tabId] = {};
    };

    var _getContextIdForFrame = function(tabId, frameId) {
        var tab = _frameToContextMap[tabId];
        if (frameId == null) {
            return tab.firstContext;
        }
        var keys = Object.keys(tab);
        for (var i = 0; i < keys.length; ++i) {
            var item = tab[keys[i]];
            if (item && item === frameId) {
                return parseInt(keys[i]);
            }
        }
        return null;
    };

    var _addContextDestroyedHandler = function(handler) {
        _contextDestroyedHandlers.push(handler);
    };

    var _handleEvent = function(method, params) {
        // console.log(method, params);
        switch (method) {
            case 'Runtime.executionContextDestroyed':
                _executionContextDestroyed(params);
                break;
            case 'Runtime.executionContextCreated':
                _executionContextCreated(params);
                break;
            case 'Runtime.executionContextsCleared':
                _executionContextsCleared(params);
                break;
            default:
                // do nothing
                break;
        }
    };

    return {
        handleEvent: _handleEvent,
        addContextDestroyedHandler: _addContextDestroyedHandler,
        getContextIdForFrame: _getContextIdForFrame
    };
};
