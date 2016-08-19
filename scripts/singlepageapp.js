// Load the page and verify that the right page was loaded
var b = pizza.open("www.google.com");
b.verifyText(/Abou./);
b.verifyTitle(/Google/);

// Show the search Ajax in another page
b.newPage();
b.type("input[name='q']", "hot pocket\n");

// Click on the first link for hot pockets, turns out we are pretty hungry
b.waitForVisible("a:icontains(hot pocket)");

b.newPage();
b.click("a:icontains(hot pocket)");

// Wait for content to finish loading
b.waitPageLoad();
