# PizzaScript - Browser Automation & Performance Monitoring

PizzaScript is a browser automation framework with built-in performance monitoring.

It allows you to write web performance tests and automation scripts in JavaScript,
and save off performance metrics for later use.

## Features

- Inspect page load times and HTTP traffic
- Wait on page load, HTTP traffic, elements to be visible
- Many useful CSS selector extensions (e.g. "a:contains(Click me!)" selects a link with the text "Click me!")
- Take screenshots
- Block/redirect certain URLs (or block all beacon URLs)
- Modify request headers
- Emulate Mobile and Tablet devices
- Emulate network conditions (e.g. 3G/4G/Offline)
- Record WebSocket request/responses

## FAQ

Q: How does this differ from Selenium / WebDriver?
A: The main thing is perforamce metrics (page load, first render time, http requests)
are recorded as the page is interacted with.  You can use a proxy with Selenium, but doing
can cause the timing / concurrency to change and introduce subtle changes to how requests
are made.

Other differences:

- The PizzaScript API is simple and to the point, above all else.  We want you to be
  able write tests without the API getting in your way.  We're not trying to make the
  API Object Orientated, or limiting what we will automate to what a user can do.
- It works with modern dynamic apps without requiring complex wait logic and wrapper
  functions.
- Since the API is more focused and use case driven, internally the source code is much
  much smaller, and easier to work with.
- Element visibility errors are more detailed, making certain failures easier to debug.

There are tons of things Selenium does better though, not least their browser support,
bit we still believe we are on the right track as far our API is concerned.

Q: What browsers are supported?
A: Currently only Chrome is supported.  Firefox and Edge support is on the roadmap.
Support for emulating mobile browsers in Chrome is available right now.

Q: Is there a script recorder?
A: There's an IDE for writing and debugging scripts. CSS selectors can be generated
for HTML elements by clicking on them.  This makes it very efficient to write scripts
once you have a basic understanding of API.

## Documentation

Scripting API docs can be found here:

  https://docs.loadtestgo.com

## Requirements

+ Chrome 40+

## Run

Run the script-editor and this will bring up an interactive JavaScript console.

    b = pizza.open("www.google.com");
    b.verifyText("Search");

## Implementation Details

- Script are written in JavaScript and executed in the JRE using Rhino..

- The Chrome process is launched and controlled from the Java process.

- Communication to Chrome is over a WebSocket, a Chrome extension creates the WebSocket and connects to
  the Java process.  From there Rhino calls to the browser object are sent over the WebSocket using
  an RPC mechanism.  Our Chrome extension handles these requests.

- The Chrome extension is writen in JavaScript and talks to the Chrome Extension API & the DevTools API to implement its automation features.
