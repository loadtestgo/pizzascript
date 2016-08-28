// Note this script will fail unless you register a user with the
// username and password below.  Every day the registered users
// are cleared out.

// Open the main page
var b = pizza.open("http://demo.nopcommerce.com/");

// Click on the login page button and wait for it to load
b.click("a.ico-login");
b.waitPageLoad();

// Enter the login details
b.type("#Email", "mark@loadtestgo.com");
b.type("#Password", "pizzascript");

// Click the login button, and wait for the login to complete
b.click("input.login-button");
b.waitPageLoad();