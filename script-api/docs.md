## PizzaScript

PizzaScript is a JavaScript API for Web Page Automation.

Key features:

  - Inspect page load times and HTTP traffic
  - Wait on page load, HTTP traffic, elements to be visible
  - Reference elements by CSS selector or XPATH
  - CSS selector extensions (e.g. "a:contains(Click me!)" selects a link with the text "Click me!")
  - Take screenshots
  - Block/redirect certain URLs (or block all beacon URLs)
  - Modify request headers
  - Emulate Mobile and Tablet devices
  - Emulate network conditions (e.g. 3G/4G/Offline)
  - Record WebSockets requests


## Examples

### Website login script:

    var b = pizza.open();
    b.open("loadtestgo.com");
    b.click("button:contains(Login)");
    b.waitPageLoad();
    b.type("#inputUsername", "demo@loadtestgo.com");
    b.type("#inputPassword", "password");
    b.click("button:contains(Login):nth(1)");
    b.waitPageLoad();

### Google 'hot pocket' and click first result:

    // Load the page and verify that the right page was loaded
    var b = pizza.open("www.google.com");
    b.verifyText(/Abou./);
    b.verifyTitle(/Google/);

    // Show the search Ajax in another page
    b.newPage();
    b.type("input[name='q']", "hot pocket\n");

    // Click on the first link for 'hot pocket'
    b.waitForVisible("a:icontains(hot pocket)");
    b.click("a:icontains(hot pocket)");

    // Wait for content to finish loading
    b.waitPageLoad();

### View mobile version of [cnn.com](http://cnn.com/ "CNN News"):

    var b = pizza.open();
    b.emulateDevice("Apple iPhone 6")
    b.open("cnn.com");
