// Open the browser (but don't open the web page just yet)
var b = pizza.open();

// Tell browser to block known 3rd party beacons and adverts
b.block3rdPartyUrls();

// Tell browser to block all resources from specific site
b.blockUrl("*://dev.visualwebsiteoptimizer.com/*");

b.open("www.bbc.com");
