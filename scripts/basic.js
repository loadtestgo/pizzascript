// Open the browser and navigate to the requested URL
// NOTE: A browser instance can be opened without initially
// setting the URL, see script: sitelogin.js
b = pizza.open("www.google.com");

// Verify the following text exists on the page
b.verifyText("Google");
