var b = pizza.open();

// Switch the user agent (both the HTTP request headers and JavaScript
// navigator.userAgent)
b.setUserAgent("Googlebot/2.1");

// Now open a page, Google's homepage falls back to a more basic
// version when it doesn't recognise the user agent header.
b.open("www.google.com");
