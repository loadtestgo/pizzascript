[![Build Status](https://travis-ci.org/loadtestgo/pizzascript.svg?branch=master)](https://travis-ci.org/loadtestgo/pizzascript)
[![License](https://img.shields.io/badge/licence-BSD--3--Clause-blue.svg)](https://github.com/loadtestgo/pizzascript/blob/master/LICENSE.BSD)

#### [Homepage](http://pizzascript.org) | [API Reference](http://docs.loadtestgo.com)

# PizzaScript - Browser Automation & Performance Monitoring

PizzaScript is a JavaScript browser automation framework with built-in performance monitoring.

It includes a script editor, with a REPL and debugger for interactively writing tests.

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

Then download the latest [PizzaScript](https://github.com/loadtestgo/pizzascript/files/365310/script-editor-0.2.4.zip) release.

Open the IDE:

    bin/script-editor

Run a script from the console and save a HAR (HTTP Archive) file:

    bin/script-editor -console samples/google.js -har

The editor will search the path for Chrome, along with well-known locations.  You can specify the location of Chrome in the settings.ini file included in the top level directory of the zip (this is read from the current working directory when the editor is loaded) 

The editor has been tested on Linux, OSX and Windows.


## Examples

To give you a flavour for the API here are some scripts.  The full API is documented [here](http://docs.loadtestgo.com).


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
