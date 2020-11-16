//
// Handle mobile/tablet emulation
//
pizza.main.emulation = function() {
    var _deviceInfo = null;
    var _networkEmulation = null;

    var devices = [
        {
            "title": "Apple iPhone 4",
            "width": 320,
            "height": 480,
            "deviceScaleFactor": 2,
            "userAgent": "Mozilla/5.0 (iPhone; U; CPU iPhone OS 4_2_1 like Mac OS X; en-us) AppleWebKit/533.17.9 (KHTML, like Gecko) Version/5.0.2 Mobile/8C148 Safari/6533.18.5",
            "touch": true,
            "mobile": true
        },
        {
            "title": "Apple iPhone 5",
            "width": 320,
            "height": 568,
            "deviceScaleFactor": 2,
            "userAgent": "Mozilla/5.0 (iPhone; CPU iPhone OS 7_0 like Mac OS X; en-us) AppleWebKit/537.51.1 (KHTML, like Gecko) Version/7.0 Mobile/11A465 Safari/9537.53",
            "touch": true,
            "mobile": true
        },
        {
            "title": "Apple iPhone 6",
            "width": 375,
            "height": 667,
            "deviceScaleFactor": 2,
            "userAgent": "Mozilla/5.0 (iPhone; CPU iPhone OS 8_0 like Mac OS X) AppleWebKit/600.1.3 (KHTML, like Gecko) Version/8.0 Mobile/12A4345d Safari/600.1.4",
            "touch": true,
            "mobile": true
        },
        {
            "title": "Apple iPhone 6 Plus",
            "width": 414,
            "height": 736,
            "deviceScaleFactor": 3,
            "userAgent": "Mozilla/5.0 (iPhone; CPU iPhone OS 8_0 like Mac OS X) AppleWebKit/600.1.3 (KHTML, like Gecko) Version/8.0 Mobile/12A4345d Safari/600.1.4",
            "touch": true,
            "mobile": true
        },
        {
            "title": "BlackBerry Z30",
            "width": 360,
            "height": 640,
            "deviceScaleFactor": 2,
            "userAgent": "Mozilla/5.0 (BB10; Touch) AppleWebKit/537.10+ (KHTML, like Gecko) Version/10.0.9.2372 Mobile Safari/537.10+",
            "touch": true,
            "mobile": true
        }, {
            "title": "Google Nexus 4",
            "width": 384,
            "height": 640,
            "deviceScaleFactor": 2,
            "userAgent": "Mozilla/5.0 (Linux; Android 4.4.4; en-us; Nexus 4 Build/JOP40D) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/42.0.2307.2 Mobile Safari/537.36",
            "touch": true,
            "mobile": true
        }, {
            "title": "Google Nexus 5",
            "width": 360,
            "height": 640,
            "deviceScaleFactor": 3,
            "userAgent": "Mozilla/5.0 (Linux; Android 4.4.4; en-us; Nexus 5 Build/JOP40D) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/42.0.2307.2 Mobile Safari/537.36",
            "touch": true,
            "mobile": true
        }, {
            "title": "LG Optimus L70",
            "width": 384,
            "height": 640,
            "deviceScaleFactor": 1.25,
            "userAgent": "Mozilla/5.0 (Linux; U; Android 4.4.2; en-us; LGMS323 Build/KOT49I.MS32310c) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/30.0.1599.103 Mobile Safari/537.36",
            "touch": true,
            "mobile": true
        }, {
            "title": "Nokia N9",
            "width": 360,
            "height": 640,
            "deviceScaleFactor": 1,
            "userAgent": "Mozilla/5.0 (MeeGo; NokiaN9) AppleWebKit/534.13 (KHTML, like Gecko) NokiaBrowser/8.5.0 Mobile Safari/534.13",
            "touch": true,
            "mobile": true
        }, {
            "title": "Nokia Lumia 520",
            "width": 320,
            "height": 533,
            "deviceScaleFactor": 1.4,
            "userAgent": "Mozilla/5.0 (compatible; MSIE 10.0; Windows Phone 8.0; Trident/6.0; IEMobile/10.0; ARM; Touch; NOKIA; Lumia 520)",
            "touch": true,
            "mobile": true
        }, {
            "title": "Samsung Galaxy S III",
            "width": 360,
            "height": 640,
            "deviceScaleFactor": 2,
            "userAgent": "Mozilla/5.0 (Linux; U; Android 4.0; en-us; GT-I9300 Build/IMM76D) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30",
            "touch": true,
            "mobile": true
        }, {
            "title": "Samsung Galaxy S4",
            "width": 360,
            "height": 640,
            "deviceScaleFactor": 3,
            "userAgent": "Mozilla/5.0 (Linux; Android 4.4.2; GT-I9505 Build/JDQ39) AppleWebKit/537.36 (KHTML, like Gecko) Version/1.5 Chrome/28.0.1500.94 Mobile Safari/537.36",
            "touch": true,
            "mobile": true
        }, {
            "title": "Amazon Kindle Fire HDX",
            "width": 2560,
            "height": 1600,
            "deviceScaleFactor": 2,
            "userAgent": "Mozilla/5.0 (Linux; U; en-us; KFAPWI Build/JDQ39) AppleWebKit/535.19 (KHTML, like Gecko) Silk/3.13 Safari/535.19 Silk-Accelerated=true",
            "touch": true,
            "mobile": true
        }, {
            "title": "Apple iPad Mini",
            "width": 1024,
            "height": 768,
            "deviceScaleFactor": 1,
            "userAgent": "Mozilla/5.0 (iPad; CPU OS 4_3_5 like Mac OS X; en-us) AppleWebKit/533.17.9 (KHTML, like Gecko) Version/5.0.2 Mobile/8L1 Safari/6533.18.5",
            "touch": true,
            "mobile": true
        }, {
            "title": "Apple iPad",
            "width": 1024,
            "height": 768,
            "deviceScaleFactor": 2,
            "userAgent": "Mozilla/5.0 (iPad; CPU OS 7_0 like Mac OS X) AppleWebKit/537.51.1 (KHTML, like Gecko) Version/7.0 Mobile/11A465 Safari/9537.53",
            "touch": true,
            "mobile": true
        }, {
            "title": "BlackBerry PlayBook",
            "width": 1024,
            "height": 600,
            "deviceScaleFactor": 1,
            "userAgent": "Mozilla/5.0 (PlayBook; U; RIM Tablet OS 2.1.0; en-US) AppleWebKit/536.2+ (KHTML like Gecko) Version/7.2.1.0 Safari/536.2+",
            "touch": true,
            "mobile": true
        }, {
            "title": "Google Nexus 10",
            "width": 1280,
            "height": 800,
            "deviceScaleFactor": 2,
            "userAgent": "Mozilla/5.0 (Linux; Android 4.3; Nexus 10 Build/JSS15Q) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/42.0.2307.2 Mobile Safari/537.36",
            "touch": true,
            "mobile": true
        }, {
            "title": "Google Nexus 7",
            "width": 960,
            "height": 600,
            "deviceScaleFactor": 2,
            "userAgent": "Mozilla/5.0 (Linux; Android 4.3; Nexus 7 Build/JSS15Q) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/42.0.2307.2 Mobile Safari/537.36",
            "touch": true,
            "mobile": true
        }, {
            "title": "Samsung Galaxy Note 3",
            "width": 360,
            "height": 640,
            "deviceScaleFactor": 3,
            "userAgent": "Mozilla/5.0 (Linux; U; Android 4.3; en-us; SM-N900T Build/JSS15J) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30",
            "touch": true,
            "mobile": true
        }, {
            "title": "Samsung Galaxy Note II",
            "width": 360,
            "height": 640,
            "deviceScaleFactor": 2,
            "userAgent": "Mozilla/5.0 (Linux; U; Android 4.1; en-us; GT-N7100 Build/JRO03C) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30",
            "touch": true,
            "mobile": true
        }, {
            "title": "Laptop with touch", "width": 1280,
            "height": 950, "deviceScaleFactor": 1, "userAgent": "", "touch": true, "mobile": false
        }, {
            "title": "Laptop with HiDPI screen",
            "width": 1440,
            "height": 900,
            "deviceScaleFactor": 2,
            "userAgent": "",
            "touch": false,
            "mobile": false
        }, {
            "title": "Laptop with MDPI screen",
            "width": 1280,
            "height": 800,
            "deviceScaleFactor": 1,
            "userAgent": "",
            "touch": false,
            "mobile": false
        }
    ];

    var bytesK = 1024 / 8;
    var bytesM = 1024 * bytesK;

    var networkConditions = [
        {name: "Offline", down: 0, up: 0, latency: 0, offline: true},
        {name: "GPRS", down: 50 * bytesK, up: 15 * bytesK, latency: 500},
        {name: "Regular 2G", down: 250 * bytesK, up: 50 * bytesK, latency: 300},
        {name: "Good 2G", down: 450 * bytesK, up: 90 * bytesK, latency: 150},
        {name: "Regular 3G", down: 750 * bytesK, up: 200 * bytesK, latency: 100},
        {name: "Good 3G", down: 1.5 * bytesM, up: 500 * bytesK, latency: 40},
        {name: "Regular 4G", down: 5 * bytesM, up: 3 * bytesM, latency: 20},
        {name: "DSL", down: 2 * bytesM, up: 200 * bytesK, latency: 5},
        {name: "WiFi", down: 30 * bytesM, up: 12 * bytesM, latency: 2}];

    function getDeviceMetrics(i) {
        var device = devices[i];
        return {
            deviceMetrics: {
                "width": device.width,
                "height": device.height,
                "deviceScaleFactor": device.deviceScaleFactor ? device.deviceScaleFactor : 1,
                "mobile": device.mobile,
                "fitWindow": device.fitWindow ? device.fitWindow : false,
                "textAutosizing": device.textAutosizing ? device.textAutosizing : true,
                "fontScaleFactor": device.fontScaleFactor ? device.fontScaleFactor : 1
            },
            "touch": device.touch,
            "userAgent": device.userAgent
        };
    }

    var applyOverrides = function(wait) {
        if (_deviceInfo) {
            pizza.devtools.sendCommand("Page.setDeviceMetricsOverride", _deviceInfo['deviceMetrics'],
                wait.add(function (r) {
                    var error = chrome.runtime.lastError;
                    if (error) {
                        console.log(error);
                    }
                }));
        } else {
            clearDeviceMetricsOverride(wait);
        }

        applyTouchOverride(wait);
        applyUserAgentOverride(wait);
    };

    function applyUserAgentOverride(wait) {
        var userAgent = "";
        if (_deviceInfo && _deviceInfo.userAgent) {
            userAgent = _deviceInfo.userAgent;
        }

        pizza.devtools.sendCommand(
            'Network.setUserAgentOverride',
            {'userAgent': userAgent},
            wait.add(function () {
                var error = chrome.runtime.lastError;
                if (error) {
                    console.log(error);
                }
            }));
    }

    function applyTouchOverride(wait) {
        var enableTouch = false;
        if (_deviceInfo && _deviceInfo.touch) {
            enableTouch = true;
        }
        pizza.devtools.sendCommand("Page.setTouchEmulationEnabled", {enabled: enableTouch},
            wait.add(function () {
                var error = chrome.runtime.lastError;
                if (error) {
                    console.log(error);
                }
            }));
    }

    function clearDeviceMetricsOverride(wait) {
        pizza.devtools.sendCommand(
            'Page.clearDeviceMetricsOverride',
            {},
            wait.add(function () {
                var error = chrome.runtime.lastError;
                if (error) {
                    console.log(error);
                }
            }));
    }

    var _setDevice = function(deviceName, wait) {
        if (deviceName != null) {
            for (var i = 0; i < devices.length; ++i) {
                var d = devices[i];
                if (d.title === deviceName) {
                    _deviceInfo = getDeviceMetrics(i);
                    applyOverrides(wait);
                    return true;
                }
            }
        }

        _deviceInfo = null;
        applyOverrides(wait);
        return false;
    };

    var _reset = function(wait) {
        if (_deviceInfo) {
            _deviceInfo = null;
            applyOverrides(wait);
        }

        if (_networkEmulation) {
            // Set other params to default if they are not set
            var c = {};
            c.offline = false;
            c.latency = 0;
            // 0 apparently means turn off limiting now
            c.downloadThroughput = 0;
            // 0 apparently means turn off limiting now
            c.uploadThroughput = 0;

            pizza.devtools.sendCommand(
                'Network.emulateNetworkConditions',
                c,
                wait.add(function () {
                    var error = chrome.runtime.lastError;
                    if (error) {
                        console.log(error);
                    } else {
                        // its reset so clear
                        _networkEmulation = null;
                    }
                }));
        }
    };

    var _getDevice = function() {
        return _deviceInfo;
    };

    var _listDevices = function() {
        var r = [];
        for (var i = 0; i < devices.length; ++i) {
            var d = devices[i];
            r.push(d.title);
        }
        return r;
    };

    var _listNetworkConditions = function() {
        return networkConditions;
    };

    function getDevToolsNetworkConditions(n) {
        return {
            offline: n.offline ? n.offline : false,
            latency: n.latency,
            downloadThroughput: n.down,
            uploadThroughput: n.up
        }
    }

    var _setNetworkConditions = function(params, response) {
        var c = null;

        if (params != null) {
            if (params.name) {
                for (var i = 0; i < networkConditions.length; ++i) {
                    var n = networkConditions[i];
                    if (n.name === params.name) {
                        c = getDevToolsNetworkConditions(n);
                    }
                }
                if (c == null) {
                    response({error: "Unknown network condition \'" + params.name + "\'"});
                    return;
                }
            } else {
                c = params;
            }
        } else {
            // Disable
            c = {};
        }

        // Set other params to default if they are not set
        if (!c.hasOwnProperty('offline')) {
            c.offline = false;
        }
        if (!c.hasOwnProperty('latency')) {
            c.latency = 0;
        }
        if (!c.hasOwnProperty('downloadThroughput')) {
            // 0 apparently means turn off limiting now
            c.downloadThroughput = 0;
        }
        if (!c.hasOwnProperty('uploadThroughput')) {
            // 0 apparently means turn off limiting now
            c.uploadThroughput = 0;
        }

        pizza.devtools.sendCommand(
            'Network.emulateNetworkConditions',
            c,
            function () {
                var error = chrome.runtime.lastError;
                if (error) {
                    response({ error: error });
                } else {
                    _networkEmulation = c;
                    response({ value: true });
                }
            });
    };

    return {
        setDevice: _setDevice,
        getDevice: _getDevice,
        listDevices: _listDevices,
        reset: _reset,
        listNetworkConditions: _listNetworkConditions,
        setNetworkCondition: _setNetworkConditions
    };
};

