// Open the browser, but don't go to the page until we set the username / password
var b = pizza.open();

// Set username and password to be used for HTTP Authentication
// This will work with both Basic and Digest HTTP Authentication
b.setAuth("httpwatch", utils.randomString());

// Go to a page that requires authentication
b.open("http://www.httpwatch.com/httpgallery/authentication/authenticatedimage/default.aspx");
