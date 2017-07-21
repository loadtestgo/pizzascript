var b = pizza.open();

// Open the main page
b.open("loadtestgo.com");

// Click the login button (button containing the text 'Sign In')
b.click("button:contains(Sign In)");

// Wait for the navigation to the login page to complete
b.waitPageLoad();

// Input user name and password
b.type("#inputEmail", "demo@loadtestgo.com");
b.type("#inputPassword", "password");

// Click the second button on the page containing the text 'Sign In'
// Note that :nth is 0 based, so 1 is the second item
b.click("button:contains(Sign In):nth(1)");

// Wait for the login to complete
// This waits for the POST & the POST redirect to complete,
// redirects are automatically waited on by Browser.waitPageLoad()
b.waitPageLoad();
