var b = pizza.open("www.google.com");

// Get the current DOM for the main frame
var dom = b.getOuterHTML();

// Save DOM with script results
pizza.saveFile("dom.html", dom);
