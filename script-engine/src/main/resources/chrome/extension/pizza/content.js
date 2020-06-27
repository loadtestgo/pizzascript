var port = chrome.runtime.connect();

/* Injected into each page - so that messages from injected code can get back to our extension */
window.addEventListener("message", function(event) {
    if (event.source != window) {
        return;
    }

    if (event.data.type && (event.data.type == "PizzaElementSelected")) {
        port.postMessage(event.data.msg);
    }
}, false);
