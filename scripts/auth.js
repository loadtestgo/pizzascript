// Open the browser, but don't go to the page until we set the username / password
var b = pizza.open();

// Set username and password to be used for HTTP Authenication
// This will work with both Basic and Digest HTTP Authernication
b.setAuth("httpwatch", utils.randomString());

// Go to a page that requires authenication
b.open("http://www.httpwatch.com/httpgallery/authentication/authenticatedimage/default.aspx");
