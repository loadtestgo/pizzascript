// Load a file (from the current working directory)
load("foo.js");

// foo.js sets a and doStuff on the top level scope
// so we have access to them here.
console.log(a);
doStuff();
