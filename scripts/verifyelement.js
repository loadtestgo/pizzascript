var b = pizza.open("https://github.com/loadtestgo/pizzascript");

// Verify that there's an element matching the selector on the page.
// Search for a <a> tag containing the text 'Wiki'.  Note that this is
// an extended CSS selector, 'contains' is our own 'psuedo class' (in
// W3 selector lingo) that selects elements based on their inner text
// of elements.
b.verifyExists("a:contains(Wiki)");
