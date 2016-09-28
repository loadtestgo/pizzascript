var b = pizza.open();

// Open the given site and return immediately instead of waiting for the site to load
b.openAsync("www.yelp.com");

// Wait for a element matching the CSS selector '#find_desc' (a button with id 'find_desc')
b.waitForVisible("#find_desc");
