[![Build Status](https://travis-ci.org/loadtestgo/pizzascript.svg?branch=master)](https://travis-ci.org/loadtestgo/pizzascript)
[![License](https://img.shields.io/badge/licence-BSD--3--Clause-blue.svg)](https://github.com/loadtestgo/pizzascript/blob/master/LICENSE.BSD)

#### [Homepage](http://pizzascript.org) | [API Reference](http://pizzascript.org/api) | [Examples](https://github.com/loadtestgo/pizzascript/wiki/Examples)

# PizzaScript - Browser Automation & Performance Monitoring

PizzaScript is a JavaScript browser automation framework with built-in performance monitoring.

It includes:

- A JavaScript IDE, with a [REPL](https://en.wikipedia.org/wiki/Read%E2%80%93eval%E2%80%93print_loop) and debugger for
  interactively writing tests.
- A standalone script runner, for running a set of tests, capturing screenshots & performance metrics.
- A Java API, for embedding the script runner.

## Features

- Inspect page load times and HTTP traffic
- Wait on page load, HTTP traffic, elements to be visible
- Many useful CSS selector extensions (e.g. "a:contains(Click me!)" selects a link with the
  text "Click me!")
- Take screenshots
- Block/redirect certain URLs (e.g. block 3rd party beacon URLs)
- Modify request headers
- Emulate Mobile and Tablet devices
- Emulate network conditions (e.g. 3G/4G/Offline)
- Record WebSocket request/responses

![PizzaScript Editor](script-editor/script-record.gif)

## Try It!

First make sure the following are installed.

+ Chrome 40+
+ Java 8 JRE

Then download the latest [PizzaScript](https://github.com/loadtestgo/pizzascript/releases/download/v0.2.5/script-editor-0.2.5.zip) release.

Open the IDE:

    bin/script-editor

Run a batch of scripts and saving error messages, HAR (HTTP Archive) and screenshots to a directory:

    bin/script-runner scripts

Both will search the path for Chrome, along with well-known Chrome install locations. See [Chrome Setup](https://github.com/loadtestgo/pizzascript/wiki/Chrome-Setup) for troubleshooting.

Tested on Linux, OSX and Windows.


## Examples

### Load Page

```javascript
b = pizza.open("www.google.com");
b.verifyText("Search");
```

### Site Login

```javascript
var b = pizza.open();
b.open("loadtestgo.com");
b.click("button:contains(Login)");
b.waitPageLoad();
b.type("#inputUsername", "demo@loadtestgo.com");
b.type("#inputPassword", "password");
b.click("button:contains(Login):nth(1)");
b.waitPageLoad();
```

### Load Mobile Site

```javascript
var b = pizza.open();
b.emulateDevice("Apple iPhone 6")
b.open("cnn.com");
```

## Alternatives

* [WebPageTest](https://www.webpagetest.org) - Test a site's peformance from many different locations with a wide variety of browsers.
* [PhantomJS](http://phantomjs.org) - Drive a headless browser using JavaScript, take screenshots and capture performance metrics.
* [Browsertime](https://github.com/sitespeedio/browsertime) - Access the Web Performance Timeline, from your browser, in your terminal!
