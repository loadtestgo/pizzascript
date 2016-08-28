var b = pizza.open("https://github.com/loadtestgo/pizzascript");

// Verify the following text exists on the page
b.verifyText("Browser Automation");

// Verify that the title contains the following
b.verifyTitle("Github");
