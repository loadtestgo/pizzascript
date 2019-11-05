var b = pizza.open("www.google.com");

// The page has loaded at this point, find the first requesting matching google.com
var r = pizza.getRequestByUrl("https://www.google.com/images/branding/googlelogo/2x/googlelogo_color_120x44dp.png");

// Fetch the HTTP response body.
var data = b.getResponseBody(r);

// Save that to a file.  This file will be attached to the results for this script.
pizza.saveFile("google.png", data);
