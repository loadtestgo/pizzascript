var port = chrome.runtime.connect();

window.addEventListener("message", function(event) {
    if (event.source != window) {
        return;
    }

    if (event.data.type && (event.data.type == "PizzaElementSelected")) {
        port.postMessage(event.data.msg);
    }
}, false);
