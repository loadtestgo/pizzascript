var port = chrome.runtime.connect();
var frameId, tabId;

/* Injected into each page - so that messages from injected code can get back to our extension */
window.addEventListener("message", function(event) {
    if (event.source !== window) {
        return;
    }
    if (event.data.type && (event.data.type === "PizzaElementSelected")) {
        port.postMessage({type: event.data.type, tabId: tabId, frameId: frameId, msg: event.data.msg});
    }
}, false);

/* Grab paint timings */
var processPerfEvents = function (entries) {
    let loadTimes = { "timeOrigin": performance.timeOrigin };
    for (let i = 0; i < entries.length; i++) {
        let entry = entries[i];
        let entryType = entry.entryType;
        if (entryType === "paint") {
            let startTime = entry.startTime;
            if (entry.name === "first-paint") {
                loadTimes.firstPaint = startTime;
            } else if (entry.name === "first-contentful-paint") {
                loadTimes.firstContentfulPaint = startTime;
            }
        } else if (entryType === "navigation") {
            let copy = [
                "connectEnd", "connectStart", "domComplete", "domContentLoadedEventEnd",
                "domContentLoadedEventStart", "domInteractive", "domainLookupEnd",
                "domainLookupStart", "duration", "fetchStart", "loadEventEnd",
                "loadEventStart", "nextHopProtocol", "requestStart", "requestEnd",
                "responseEnd", "responseStart"
            ];
            for (let j = 0; j < copy.length; ++j) {
                let str = copy[j];
                loadTimes[str] = entry[str];
            }
        }
    }
    port.postMessage({type: "perf", tabId: tabId, frameId: frameId, msg: loadTimes });
}


window.setPizzaScriptTabAndFrameId = function(tabIdIn, frameIdIn) {
    tabId = tabIdIn;
    frameId = frameIdIn;

    /* Record future events */
    new PerformanceObserver(function(list, obj) {
        processPerfEvents(list.getEntries());
    }).observe({ entryTypes: ["navigation", "paint"] });

    /* Process existing events */
    processPerfEvents(performance.getEntriesByType("navigation").concat(
        performance.getEntriesByType("paint")));
};

