var b = pizza.open("https://github.com/loadtestgo/pizzascript");

// Verify a specific HTTP request was made
//
// An exception is thrown in the following cases:
// - The request was never made / not found on the current page
// - The request had an network/transport error
// - The request has not completed yet
// - The request had a HTTP status error code (4xx or 5xx status code)
//
b.verifyRequest("https://github.com/loadtestgo/pizzascript/raw/master/script-editor/script-record.gif");
