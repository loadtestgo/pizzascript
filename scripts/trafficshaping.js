var b = pizza.open();

// Emulate 3G connection, adds latency for each packet plus limits download
// and upload link rate.
b.emulateNetworkCondition("Regular 3G");

var site = "www.google.com";
var p1 = b.open(site);

// Disable network emulation and load the site normally
b.emulateNetworkCondition({});

var p2 = b.open(site);

console.log(site + " loaded in " + p2.getLoadTime()  + " seconds natively and\n" +
  "in " + p1.getLoadTime() + " with 3G emulation");
