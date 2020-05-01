// Object that waits for a specific tab or frame to load.
pizza.main.navigation = function() {
    var _loadedHandler = null;
    var _isNavigating = false;
    var _expectCancel = false;
    var _tabId = null;
    var _frameId = null;
    var _navDetails = null;
    var _url = null;

    var _reset = function(tabId, frameId) {
        _tabId = tabId;
        _frameId = frameId;
        _loadedHandler = null;
        _expectCancel = _isNavigating;
        _navDetails = null;
        _url = null;
    };

    var _setLoadedHandler = function(loadedHandler) {
        _loadedHandler = loadedHandler;
    };

    var _onNavBeforeCallback = function(details) {
        if (_tabId === details.tabId && _frameId === details.frameId) {
            _isNavigating = true;
            _url = details.url;
        }
    };

    var _onNavCommittedCallback = function(details) {
        if (_tabId === details.tabId && _frameId === details.frameId) {
            _url = details.url;
        }
    };

    var _onNHistoryUpdatedCallback = function(details) {
        if (_tabId === details.tabId && _frameId === details.frameId) {
            _url = details.url;
        }
    };

    var _onNavCompletedCallback = function(details) {
        if (_tabId === details.tabId && _frameId === details.frameId && pizza.baseUrlEquals(_url, details.url)) {
            _isNavigating = false;
            _url = null;
            _navDetails = details;

            if (_loadedHandler != null) {
                var loadedHandler = _loadedHandler;
                _loadedHandler = null;
                _navDetails = null;
                loadedHandler(details);
            }
        }
    };

    var _onNavErrorOccurredCallback = function(details) {
        if (_tabId === details.tabId && _frameId === details.frameId && pizza.baseUrlEquals(_url, details.url)) {
            _error(details);
        }
    };

    var _previousNavigation = function() {
        return _navDetails;
    };

    var _isNavigationPending = function(tabId) {
        return (tabId === _tabId && _isNavigating && _loadedHandler);
    };

    var _error = function(details) {
        _isNavigating = false;
        _url = null;
        _navDetails = details;

        if (_expectCancel) {
            _expectCancel = false;
            console.log("ExpectCancel Ignoring...");
            return;
        }

        if (_loadedHandler != null) {
            var loadedHandler = _loadedHandler;
            _loadedHandler = null;
            _navDetails = null;
            loadedHandler(details);
        }
    };

    pizza.network.addListener("onBeforeNavigate", _onNavBeforeCallback);
    pizza.network.addListener("onCompleted", _onNavCompletedCallback);
    pizza.network.addListener("onCommitted", _onNavCommittedCallback);
    pizza.network.addListener("onErrorOccurred", _onNavErrorOccurredCallback);
    pizza.network.addListener("onHistoryStateUpdated", _onNHistoryUpdatedCallback);

    return {
        setLoadedHandler: _setLoadedHandler,
        reset: _reset,
        previousNavigation: _previousNavigation,
        isNavigationPending: _isNavigationPending,
        error: _error
    };
};
