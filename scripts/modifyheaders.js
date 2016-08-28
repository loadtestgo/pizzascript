var b = pizza.open();

// Add a referer header.  Note that if this was already set this
// would override the existing header value.
b.setHeader("referer", "https://pizzascript.org");

// Open a site that displays the referer
b.open("https://www.whatismyreferer.com/");

// Verifty that the referer was set
b.verifyText("https://pizzascript.org");