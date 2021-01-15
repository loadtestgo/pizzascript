// This is the main entry point for the background script that is always running/
// listening in it's own context.  There is one per Chrome Browser (i.e. one
// context shared between all chrome tabs).

pizza.main.setup = function() {
    // Register a callback function with the commands api, which will be called when
    // one of our registered commands is detected.
    chrome.commands.onCommand.addListener(function(command) {
        // Call 'update' with an empty properties object to get access to the current
        // tab (given to us in the callback function).
        chrome.tabs.update({}, function(tab) {
            if (command === 'debug') {
                chrome.tabs.create({url: "chrome://extensions"});
            } else if (command === 'test') {
                pizza.webdriver.handleCommand( {id: 1, method: "POST", path: "/session/0/url", params: { url: "www.bbc.com" } });
            } else if (command === 'examine') {
                pizza.commands.findElementByClick();
            }
        });
    });

    chrome.browserAction.onClicked.addListener(function(tab) {
        pizza.commands.findElementByClick();
    });

    // These functions should not do much, they are just here to ensure a strict order
    // and to not rely on the manifest.json for the order.  If any actually work has to
    // be done, it should be done in a separate init() method, see pizza.network.init()
    // and pizza.devtools.init() for examples where work is done and callback registered.
    pizza.input = pizza.main.input();
    pizza.frametracker = pizza.main.frametracker();
    pizza.contexttracker = pizza.main.contexttracker();
    pizza.network = pizza.main.network();
    pizza.webdriver = pizza.main.webdriver();
    pizza.commands = pizza.main.commands();
    pizza.devtools = pizza.main.devtools();
    pizza.navigation = pizza.main.navigation();
    pizza.emulation = pizza.main.emulation();

    // Connect back to script-engine, so that the extension can listen for
    // commands and send back information about content that is loaded.
    function startWebSocket(opened)
    {
        if (!pizza.config.host) {
            pizza.config.host = "localhost";
        }
        var ws = new WebSocket("ws://" + pizza.config.host + ":" + pizza.config.port);

        ws.onopen = opened.add("websocket");

        ws.onmessage = function(evt) {
            var msg = evt.data;
            console.log("Message received: ", msg);
            var obj = JSON.parse(msg);
            if (obj.type === "webdriver") {
                pizza.webdriver.handleCommand(obj);
            } else if (obj.type === "pizza") {
                pizza.commands.handleCommand(obj);
            } else {
                console.warn("unknown command type for command", obj)
            }
        };

        ws.onclose = function() {
            console.log("WebSocket connection closed...");
            // Socket closed, our controlling process no longer is connected.
            // So we kill Chrome
            chrome.processes.getProcessInfo([], false, function(processes) {
                for (var pid in processes) {
                    var process = processes[pid];
                    if (process.type === 'browser') {
                        chrome.processes.terminate(process.id);
                    }
                }
            });
        };

        // Route our webdriver server responses back over the websocket
        var responseHandler = function(id, response) {
            var s = JSON.stringify({ type: "response", id: id, response: response});
            console.log(s);
            ws.send(s);
        };

        var binaryResponseHandler = function(response) {
            ws.send(response);
        };

        pizza.webdriver.setResponseHandler(responseHandler);
        pizza.commands.setResponseHandler(responseHandler);
        pizza.commands.setBinaryResponseHandler(binaryResponseHandler);

        return ws;
    }

    // Create a promise like object so we know when we are done
    var waitInit = pizza.waitAll("started");

    // Initialize with id from our config file.
    var ws = startWebSocket(waitInit);

    chrome.runtime.onConnect.addListener(function(port) {
        port.onMessage.addListener(function(msg) {
            let event;
            if (msg.type === "perf") {
                // We dont know which item this is available for
                if (msg.tabId && msg.frameId) {
                    let details = msg.msg;
                    details.tabId = msg.tabId;
                    details.frameId = msg.frameId;
                    console.log(details);
                    event = { event: "navigationLoadTimes", "id": pizza.config.id, "details": details};
                } else {
                    return;
                }
            } else if (msg.type === "PizzaElementSelected") {
                let details = msg.msg;
                details.tabId = msg.tabId;
                details.frameId = msg.frameId;
                event = { event: "Pizza.inspectElement", "id": pizza.config.id, "details": details};
            }
            ws.send(JSON.stringify(event));
        });
    });

    // DevTools connection
    pizza.devtools.init(ws, waitInit);

    // Monitor the network and send events info over websocket
    pizza.network.init(ws);

    // Wait for everyone to be ready.
    waitInit.done(function() {
        // Parse out the Chrome version number from the userAgent and return.
        var p = / Chrome\/(\d+\.\d+\.\d+\.\d+) /;
        var m = navigator.userAgent.match(p);
        if (m != null) {
            ws.send(JSON.stringify({ event: "init", id: pizza.config.id, version: m[1] }));
        } else {
            ws.send(JSON.stringify({ event: "init", id: pizza.config.id }));
        }
    });
};

pizza.main.setup();
