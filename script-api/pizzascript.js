/**
 * Main entry point for tests.
 *
 * Most scripts will start with a call to {@link module:pizza.open}.
 *
 * @example
 * var b = pizza.open("loadtestgo.com");
 *
 * @example
 * var b = pizza.open();
 * b.open("loadtestgo.com");
 *
 * @exports pizza
 */
pizza = { };

/**
 * Open the browser.
 *
 * Takes an optional URL or browser settings parameter.  See the examples below.
 *
 * @example
 * // Open browser at "http://www.mysite.com"
 * var b = pizza.open("www.mysite.com");
 *
 * @example
 * // You may want to setup the browser before navigating
 * var b = pizza.open();
 * ...
 * // Navigate to "http://www.mysite.com"
 * b.open("mysite.com");
 *
 * @example
 * // Open the browser and ignore bad certificates (e.g. unsigned certs)
 * var b = pizza.open({ignoreCertErrors: true});
 * b.open("https://my-unsigned-test-server.com");
 *
 * @example
 * // Pass a command line arg to Chrome when launching the Chrome executable.
 * // List of switches: https://src.chromium.org/svn/trunk/src/chrome/common/chrome_switches.cc
 * var b = pizza.open({args: ["--force-device-scale-factor=1"]});
 * b.open("google.com");
 *
 * @example
 * // Open the browser and enable QUIC (http://en.wikipedia.org/wiki/QUIC)
 * var b = pizza.open({enableQuic: true, forceQuic: "my-site-that-supports-quic.com:443"});
 * b.open("https://my-site-that-supports-quic.com");
 *
 * @example
 * // Go to a 404 page but don't throw an error
 * b.ignoreHttpErrors();
 * b.open("mysite.com/404");
 *
 * @params {string=|Object=} url the URL to open, or settings to use when opening the browser
 * @return {module:Browser} the newly opened browser
 *
 * @see module:Browser#ignoreHttpErrors
 */
pizza.open = function() {};

/**
 * Returns the current browser if one is open
 *
 * @example
 * // Get the currently open browser
 * var b = pizza.browser();
 *
 * @return {module:Browser} The current browser if open, null otherwise
 */
pizza.browser = function() {};

/**
 * Sleep for the given amount of milliseconds.
 *
 * It's almost always better to wait on an element being visible or some
 * change to the DOM to happen, than having a hard wait.  Often sleeps
 * require a significant amount of tweaking and have to be changed
 * several times in response to failures that happen after you have
 * written the script.
 *
 * @example
 * // Sleep for one second
 * pizza.sleep(1000);
 *
 * @param {Number} milliseconds time to wait
 *
 * @see module:pizza.waitFor
 * @see module:Browser#waitForVisible
 * @see module:Browser#waitForHttpRequests
 */
pizza.sleep = function(milliseconds) {};

/**
 * Wait for the given function to return true
 *
 * @example
 * // Wait for window.myVar to be set
 * pizza.waitFor(function() {
 *    return b.execute("window.myVar ? true : false");
 * });
 *
 * @example
 * // Wait for window.myVar to be set, with 1 second between retries
 * pizza.waitFor(function() {
 *     return b.execute("window.myVar ? true : false");
 * }, 1000);
 *
 * @param {Function} func the function to check
 * @param {Number=} waitIterationMilliseconds the milliseconds to wait before calling func again
 */
pizza.waitFor = function(func, waitIterationMilliseconds) {};

/**
 * Open a CSV file for read access.
 *
 * This is useful to parameterise your tests.
 *
 * @param {String} filename the CSV filename to open.
 *
 * @return {module:CSV} The CSV data
 */
pizza.openCSV = function(filename) {};

/**
 * Any pages that were loaded during the test (including the current page or pages
 * added with {@link module:Browser#newPage).
 *
 * @example
 * // Log the pages loaded so far to the console
 * var r = pizza.result;
 * for (var i = 0; i < r.length; ++i) {
 *     console.log(r[i].url);
 * }
 *
 * @type {module:Page[]}
 */
pizza.pages = [];

/**
 * The test results that will be uploaded when the test ends.
 *
 * @example
 * // Log the name of the test to the console
 * console.log(pizza.result.testName);
 *
 * @return {module:TestResult}
 */
pizza.result = null;

/**
 * The public ip of the machine running the test.
 *
 * @example
 * // Log the ip to the console
 * console.log(pizza.getIp());
 *
 * @type {String}
 */
pizza.ip = "";

/**
 * The user id that the script is running under, it's unique across the load
 * test and starts at 0.
 *
 * @type {Number}
 */
pizza.userId = 0;

/**
 * The number of samples ran on this user id so far, starts at 0 and
 * increments by 1 for every sample that is processed by the current user.
 *
 * Unique only to the user.
 *
 * @type {Number}
 */
pizza.sequenceId = 0;

/**
 * The bot index starting at 0 and increasing by one for every bot launched
 * for the duration of the test.
 *
 * @type {Number}
 */
pizza.botId = 0;

/**
 * The version of the bot running the test (this is mostly for diagnostic
 * purposes).
 *
 * @type {String}
 */
pizza.version = "";

/**
 * The location the test is running from.  Our tests run primarily on Google Compute Engine,
 * so this is normally the Google Compute region the script is running in.
 *
 * @type {String}
 */
pizza.location = "";

/**
 * The id of the load test that is running
 *
 * @type {Number}
 */
pizza.loadTestId = 0;

/**
 * Assert Helper Functions
 *
 * Use these functions to verify the script is working as expected.
 *
 * @exports assert
 */
var assert = {};

/**
 * Assert the given object evaluates to true.
 *
 * @example
 * // This will succeed every time
 * assert.ok(true);
 *
 * @example
 * // This will throw an exception every time
 * assert.ok(false);
 *
 * @throws Throws an assertion when check fails
 *
 * @param {Object} object the object to check
 */
assert.ok = function(object) {};

/**
 * Always fail by throwing an exception
 *
 * @example
 * if (!myCriticalCheck()) {
 *     assert.fail("myCriticalCheck failed");
 * }
 *
 * @param {string=} message the message to report as part of the exception
 * @throws Throws an assertion always
 */
assert.fail = function(message) {};

/**
 * Assert two objects are equal
 *
 * @param {Object} o1
 * @param {Object} o2
 *
 * @example
 * // Check that an input element is set to 42
 * assert.equal(b.getValue("#input1"), "42");
 *
 * @throws Throws an assertion when o1 is not equal o2
 */
assert.equal = function(o1, o2) {};

/**
 * Assert two objects are equal
 *
 * @param {Object} o1
 * @param {Object} o2
 *
 * @example
 * // Check that an input element is set to 42
 * assert.eq(b.getValue("#input1"), "42");
 *
 * @throws Throws an assertion when o1 is not equal o2
 */
assert.eq = function(o1, o2) {};

/**
 * Assert two objects are not equal
 *
 * @param {Object} o1
 * @param {Object} o2
 *
 * @example
 * // Check that an input element is not set to 42
 * assert.eq(b.getValue("#input1"), "42");
 *
 * @throws Throws an assertion when o1 is equal o2
 */
assert.notEqual = function(o1, o2) {};

/**
 * Assert the two objects are not equal
 *
 * @example
 * // Check that an input element is not set to 42
 * assert.eq(b.getValue("#input1"), "42");
 *
 * @param {Object} o1
 * @param {Object} o2
 *
 * @throws Throws an assertion when o1 is equal o2
 */
assert.ne = function(o1, o2) {};

/**
 * Utility Functions
 *
 * @exports utils
 */
var utils = {};

/**
 * Return a date in the given format with the given number days offset
 * from the current date.
 *
 * @example
 * // Set a date two days in the future
 * var format = "MM/dd/yyyy";
 * b.type("#departureDate", utils.date(format, 2));
 *
 * @param {String} format The time format
 * @param {Number} daysOffset The day offset
 * @return {String} The new date
 */
utils.date = function(format, daysOffset) {};

/**
 * Return a random integer in the range 0 - (max - 1)
 *
 * @example
 * // get a random element from an array
 * var airports = ['san', 'sfo', 'pdx', 'smf', 'lax'];
 * var origin = airports[util.random(airports.length)];
 *
 * @param {Number} max The (max - 1) of the returned random number
 * @return {Number} Random integer between 0 - (max - 1)
 */
utils.random = function(max) {};

/**
 * Return a random element from the given array
 *
 * @example
 * // get a random element from an array
 * var airports = ['san', 'sfo', 'pdx', 'smf', 'lax'];
 * var origin = util.randomElement(airports);
 *
 * @param {Array} array The array to select a random element from
 * @return {Object} Random element from the given array, or null if the array is empty
 */
utils.randomElement = function(array) {};

/**
 * Browser Automation
 *
 * ### Element Selectors
 *
 *    Many functions here take selector strings to identify elements.  These selectors
 *    are just like CSS selectors but with a few minor additions to make them more
 *    convenient for website automation.
 *
 *    Additions to CSS selectors:
 *
 *    - contains()
 *
 *       Select only elements containing the given text.  This can be at the end of a selector
 *       or in the middle.  The selection is case sensitive.
 *
 *      Select the set of links containing 'Search':
 *
 *          a:contains(Search)
 *
 *    - icontains()
 *
 *      Case insensitive version of :contains().
 *
 *      Select the set of links containing 'Search' or 'search' or 'SEARCH':
 *
 *          a:icontains(Search)
 *
 *    - nth()/eq()
 *
 *      Select the elements at the given index.  This can be at the end of a selector or
 *      in the middle.
 *
 *      Select the 3rd 'a' element:
 *
 *          a:nth(2)
 *          a:eq(2)
 *
 *      From the set of links containing 'Search', select the 3rd one.
 *
 *          a:contains(Search):nth(2)
 *          a:contains(Search):eq(2)
 *
 *      From the set of 'a' element, select the 3rd one if it contains the string 'Search'.
 *
 *          a:nth(2):contains(Search)
 *          a:eq(2):contains(Search)
 *
 *    - Numeric ids are allowed
 *
 *      E.g.
 *          #0
 *
 *      or:
 *          #0_0_0
 *
 *    - XPath
 *
 *      Selectors starting with 'xpath:' or just '//' will do XPath matching instead of CSS selector
 *      matching.  Here are some examples"
 *
 *      Any div element in the document:
 *
 *          //div
 *          xpath://div
 *
 *      Any div element containing a paragraph element:
 *
 *          //div/p
 *          xpath://div/p
 *
 *      The div element with the id 'div1':
 *
 *          //div[@id=\\'div1\\']
 *          xpath://div[@id=\\'div1\\']
 *
 *      A div element with the class 'class1':
 *
 *          //div[@class=\\'class1\\']
 *          xpath://div[@class=\\'class1\\']
 *
 *      Any button with the text 'Button1':
 *
 *          //button[text()=\\'Button1\\']
 *          xpath://button[text()=\\'Button1\\']
 *
 * ### Frames, Tabs and Window selections
 *
 *    Frames can be queried and selected.  By default only elements within the top frame
 *    are scoped for access.  To interact with elements in a sub frame, select the right
 *    frame using {@link module:Browser#selectFrame}.
 *
 *    The concept of windows does not exists, instead all tabs for all windows are listed.
 *    We might break this out in future but for our purposes it doesn't matter if a tab
 *    is really a new window or not.
 *
 * ### Note
 *
 *    It's also important to be aware that while your script is executing the browser is
 *    going about its business: loading the page, executing JavaScript, building the DOM,
 *    animating elements, etc.  As such elements can appear/disappear, become visible/
 *    invisible potentially even after the page has loaded.  Any script you write will
 *    have to keep this in mind.  For example it is possible to wait for an element to be
 *    visible in the DOM, but then when you try to click on the element it is gone again.
 *    In those cases you may need to wait in a loop, catch click failures and try again.
 *    We may add some kind of automatic wait for the item to be visible in future.
 *
 * @exports Browser
 *
 * @see {@link http://www.w3schools.com/cssref/css_selectors.asp|CSS Selectors}
 */
Browser = {};

/**
 * Open the given URL
 *
 * Function blocks until the page is open.
 *
 * An exception is thrown if the navigation fails.
 *
 * 4xx and 5xx responses on the main document trigger a failure.
 *
 * @example
 * var b = pizza.open();
 * b.open("www.google.com");
 *
 * @throws Throws an exception when a 4xx or 5xx status code is returned for
 * main document.
 *
 * @param {String} url the url to open
 * @return {module:Page} a new page object for the given page.
 *
 * @see module:Browser#ignoreHttpErrors
 * @see module:Browser#openAsync
 */
Browser.prototype.open = function(url) {};

/**
 * Open the given URL
 *
 * Does not wait for the page to load.
 *
 * Normally you would call this in conjunction with waitForHttpRequests() or waitPageLoad()
 *
 * @example
 * var b = pizza.open();
 * b.open("www.google.com");
 * b.waitForVisible("input[name='q']");
 *
 * @param {String} url the url to open
 *
 * @see module:Browser#waitPageLoad
 * @see module:Browser#open
 */
Browser.prototype.openAsync = function(url) {};

/**
 * Get the current URL the browser is pointing to
 *
 * @example
 * var b = pizza.open("www.google.com");
 * var url = b.getUrl();
 *
 * @return {String} the URL the browser is pointing to
 */
Browser.prototype.getUrl = function() {};

/**
 * Wait for all ongoing HTTP requests to stop.
 *
 * idleTimeMS is the minimum time to wait, this function will not return
 * before then.
 *
 * Waits for Web Socket connections to fully open if there is one ongoing,
 * but does not wait for idle time on the WebSocket connection or for it to close.
 *
 * @example
 * var b = pizza.open("www.facebook.com");
 * b.waitForHttpRequests(2000);
 *
 * @param {Number} idleTimeMS the time to wait after the last request has completed
 *
 * @see module:Browser#waitPageLoad
 * @see module:Browser#verifyRequest
 */
Browser.prototype.waitForHttpRequests = function(idleTimeMS) {};

/**
 * Wait for a new page to load on the current tab.
 *
 * If a page is recently loaded, and that page was not waited on before (either
 * by open() or this function), then the function will just return.  If the
 * page opens in a new tab or window be sure to switch to that tab or window
 * before calling waitPageLoad().
 *
 * HTTP redirects are waited on too.  So one call on waitPageLoad() will wait
 * on the page redirects and the main redirected page to be loaded.
 *
 * 4xx and 5xx responses on the main document trigger a failure.
 *
 * @example
 * b.click("#button1");
 * b.waitPageLoad();
 *
 * @throws Throws an exception when a 4xx or 5xx status code is returned for
 * main document.
 *
 * @param {Number=} timeoutMS the max time to wait in milliseconds
 * @return {module:Page} the page that was navigated to.
 *
 * @see module:Browser#openAsync
 * @see module:Browser#click
 */
Browser.prototype.waitPageLoad = function(timeoutMS) {};

/**
 * Forget any page loads that happened before this function, used in conjunction
 * with waitPageLoad().
 *
 * Normally you should call this before doing something that triggers a new page
 * to load.  That way when waitPageLoad() is called you know that waitPageLoad()
 * waits on the new page to load, and does not just detect some previous page load.
 *
 * Since JavaScript executing on the page can change the URL at any time, it's not
 * a sure fire way of making waitPageLoad() wait on the right page load.  You can
 * call the version of waitPageLoad() that waits for a specific URL to load, if this
 * is an issue.
 *
 * @example
 * b.openAsync("www.mysite.com");
 * b.waitForVisible("#button1");
 * b.clearPageLoad();
 * b.click("#button1");
 * b.waitPageLoad();
 *
 * @see module:Browser#waitPageLoad
 * @see module:Browser#openAsync
 * @see module:Browser#click
 */
Browser.prototype.clearPageLoad = function() {};

/**
 * Starts a new page object.
 *
 * This allows you to name the page. HTTP requests made by the browser are added to this new
 * page object.  Page names show up in the script/test result page.
 *
 * This function is useful to split out XHR or Ajax requests into a separate page which can
 * be useful for reporting.  With dynamic or single page apps, often user actions don't result
 * in a page navigation, only background HTTP requests.  In these cases you can use this function
 * to split of the timings of various actions from each other and from the main page loading.
 *
 * If no page navigation occurs the page load time is the total time HTTP content was downloaded.
 * That is, the time span from the first item to start downloading until the last item on that
 * page is fully downloaded.
 *
 * @example
 * b.open("www.mysite.com");
 * // Name a page, before submitting form
 * b.newPage("Submit form");
 * b.click("#submitButton");
 * b.waitPageLoad();
 *
 * @example
 * // Name a page, before opening
 * var b = pizza.open();
 * b.newPage("Bing Main Page");
 * b.open("bing.com");
 *
 * @param {string=} pageName the name of the new page
 *
 * @return {module:Page} the page that was navigated to.
 */
Browser.prototype.newPage = function(pageName) {};

/**
 * Verify the given text exists on the page.
 *
 * An exception is thrown if the text does not exist.
 *
 * @example
 * b.verifyText("Sign in");
 *
 * @example
 * b.verifyText(/Hello, \w+/);
 *
 * @param {string|RegExp} text
 *
 * @see module:Browser#getInnerText
 * @see module:Browser#verifyNotText
 */
Browser.prototype.verifyText = function(text) {};

/**
 * Wait for the element matching the given selector to contain the
 * given text.
 *
 * The element does not need to exist at the time you called this
 * function.
 *
 * @example
 * b.waitForText("#consoleLog", "DISCONNECTED");
 *
 * @param {string} selector the element to wait for
 * @param {string} text the text to wait for
 *
 * @see module:Browser#getInnerText
 * @see module:Browser#verifyText
 * @see module:Browser#verifyNotText
 */
Browser.prototype.waitForText = function(selector, text) {};

/**
 * Verify that the text of the page does not contain the given string.
 *
 * An exception is thrown if the given string exists on the page.
 *
 * @example
 * b.verifyNotText("Error");
 *
 * @example
 * b.verifyNotText(/Error \d+:/);
 *
 * @param {string|RegExp} text The text to look for on the page
 *
 * @see module:Browser#verifyText
 * @see module:Browser#getInnerText
 */
Browser.prototype.verifyNotText = function(text) {};

/**
 * Verify the page title matches the given title.
 *
 * An exception is thrown if no text matches the title.
 *
 * @example
 * b.verifyTitle("Google");
 *
 * @example
 * // Matches Goooogle
 * b.verifyTitle(/Goo+gle/);
 *
 * @param {string|RegExp} title The title to match
 *
 * @see module:Browser#getTitle
 */
Browser.prototype.verifyTitle = function(title) {};

/**
 * Return the page title.
 *
 * @example
 * var title = b.getTitle();
 * console.log("Loaded page '" + title + "'");
 *
 * @return {String} The page title
 *
 * @see module:Browser#verifyTitle
 */
Browser.prototype.getTitle = function() {};

/**
 * Verify a request was made and had no errors on the current page.
 *
 * An exception is thrown in the following cases:
 *
 * - The request was never made / not found on the current page
 * - The request had an network/transport error
 * - The request has not completed yet
 * - The request had a HTTP status error code (4xx or 5xx status code)
 *
 * WebSocket requests do not need to complete (i.e. be closed), but an
 * error will thrown if the WebSocket did not successfully connect.
 *
 * @example
 * // Verify that a websocket connection was made
 * b.verifyRequest(/^ws{1,2}:/);
 *
 * @example
 * b.verifyRequest("https://loadtestgo.com");
 *
 * @param {string|RegExp} url The request URL to check for.
 * @throws Throws an exception if the request is not found or has an
 * error.
 */
Browser.prototype.verifyRequest = function(url) {};

/**
 * Execute the given JavaScript in the current frame.
 *
 * If there is an exception when the script is executed in the
 * browser it will be thrown here.
 *
 * @example
 * var elementExists =
 *     b.execute("document.getElementBy('0') ? true : false");
 *
 * @param {String} script The JavaScript code to execute
 * @returns {Object} The result of the script execution
 *
 * @see module:Browser#jq
 */
Browser.prototype.execute = function(script) {};

/**
 * Execute the given JavaScript jQuery expression.
 *
 * Executes on all frames in the current tab.
 *
 * If the normal automation functions fail or are limited in some way, jQuery to
 * the rescue! (maybe) jQuery selectors are typically a bit richer than the selectors
 * provided by us and since any javascript code can be executed, more complex
 * queries can be built up.
 *
 * @example
 * // Click a button using jQuery
 * // Note the use of single quotes inside the double quoted string
 * b.jq("$($('button')[1]).click()")
 *
 * @example
 * // Change the border on certain buttons to highlight them
 * b.jq("$('button').css('border', '2px solid red')")
 *
 * @param {String}  script The JavaScript code to execute
 * @return {Object[]} Returns an array of results for each frame that exists on the current tab
 *
 * @see module:Browser#execute
 */
Browser.prototype.jq = function(script) {};

/**
 * List the sub-frames for the currently selected frame.
 *
 * See {@link selectFrame()} for details on the selector.
 *
 * @example
 * var frames = b.listFrames();
 * console.log(JSON.stringify(frames));
 *
 * @param {string=} selector list any frames matching this selector
 *
 * @return {module:Frame[]} An array of frames
 *
 * @see module:Browser#listAllFrames
 */
Browser.prototype.listFrames = function(selector) {};

/**
 * List all frames for the current window
 *
 * @example
 * var frames = b.listAllFrames();
 * console.log(JSON.stringify(frames));
 *
 * @return {module:Frame[]} A nested array of frames
 *
 * @see module:Browser#listFrames
 */
Browser.prototype.listAllFrames = function() {};

/**
 * Select the given frame using a special frame selector.
 *
 * An extended/modified css syntax is used.  The :nth operator can be used to
 * select the nth element.  For example, b.selectFrame("iframe:nth(1)") will
 * select the the second iframe in the document.  Spaces are used to select
 * nested frames.  Normally a CSS Selector won't traverse sub frames, but this
 * function will.  For example b.selectFrame("iframe iframe") will select the
 * first iframe within an iframe.  Once a frame is selected new calls to
 * selectFrame() operate on the previously selected frame.  To switch back to
 * the main/top frame for the page call selectTopFrame().
 *
 * An exception is thrown if no frame matching the selector can be found.
 *
 * @example
 * // Select the first iframe
 * b.selectFrame("iframe");
 *
 * @example
 * // Select the second iframe
 * b.selectFrame("iframe:nth(2)");
 *
 * @example
 * // Select the second iframe within the first iframe
 * b.selectFrame("iframe iframe:nth(2)");
 *
 * // OR: you can make two calls to get the same result:
 * b.selectFrame("iframe");
 * b.selectFrame("iframe:nth(2)");
 *
 * @example
 * // Select the iframe with the name 'name1'
 * b.selectFrame("iframe[name='name1']");
 *
 * @example
 * // Select the iframe with the name 'name1'
 * b.selectFrame("iframe[name='name1']");
 *
 * @param {String} selector select the first frame matching this selector
 * @return {module:Frame} Details about the frame selected.
 *
 * @throws Throws an exception if no frame matching the selector can be found.
 */
Browser.prototype.selectFrame = function(selector) {};

/**
 * Select the given frame using a normal CSS Selector.  None of the special
 * syntax allowed by selectFrame() is used.
 *
 * An exception is thrown if no frame matching the selector can be found.
 *
 * @example
 * // Select the first iframe
 * b.selectFrameCss("iframe");
 *
 * @example
 * // Select the iframe with the name 'name1'
 * b.selectFrameCss("iframe[name='name1']");
 *
 * @param {String} selector select the first frame matching this selector
 * @return {module:Frame} Details about the frame selected.
 */
Browser.prototype.selectFrameCss = function(selector) {};

/**
 * Select the top level/main frame for the current window.
 *
 * @example
 * // Switch to subframe
 * b.selectFrame("iframe iframe");
 * // Do stuff in the subframe
 * ...
 * // Switch back to the top frame
 * b.selectTopFrame();
 *
 * @return {module:Frame} The frame selected
 */
Browser.prototype.selectTopFrame = function() {};

/**
 * Block requests to the given URL(s)
 *
 * Accepts wildcards in each URL, but URL must be of the form:
 *
 * "protocol://host/path"
 *
 * The protocol, site and path parts can wildcarded partially or fully.
 *
 * @example
 * var b = pizza.open();
 * // Block any URLs from ad.doubleclick.net
 * // This will actually block any URL with 'ad.doubleclick.net' as part of the URL
 * b.blockUrl("*://ad.doubleclick.net/*");
 * b.open("www.mysite.com");
 *
 * @example
 * // Block any URLs from any doubleclick.net subdomain
 * b.blockUrl("*://*.doubleclick.net/*");
 *
 * @param {...(String)} url The string to match against
 *
 * @see module:Browser#clearRules
 * @see module:Browser#block3rdPartyUrls
 */
Browser.prototype.blockUrl = function(url) {};

/**
 * Block requests to known third party URLs
 *
 * These include analytics, tracking, advertising and RUM beacons.
 *
 * @example
 * var b = pizza.open();
 * // Block known 3rd party beacons and adverts
 * b.block3rdPartyUrls();
 * b.open("www.mysite.com");
 *
 * @see module:Browser#blockUrl
 * @see module:Browser#clearRules
 */
Browser.prototype.block3rdPartyUrls = function() {};

/**
 * Rewrite requests to the given URL
 *
 * Accepts wildcards in the URL, but the URL must be of the form:
 *
 * "protocol://host/path"
 *
 * The protocol, site and path parts can wildcarded partially or fully.
 *
 * Match groups are supported, just put parenthesis around each group.  In the rewriteUrl string,
 * match groups can be referred to via $1, $2, $3, etc.
 *
 * @example
 * var b = pizza.open();
 * // Replace 'mario.jpg' with 'wario.jpg'
 * b.rewriteUrl('(*://www.mysite.com/*)/mario.jpg',  '$1/wario.jpg');
 * b.open("www.mysite.com");
 *
 * @param {String} url The wildcarded URL to match, can use match groups.
 * @param {String} rewriteUrl The rewrite rule with match groups.
 *
 * @see module:Browser#clearRules
 */
Browser.prototype.rewriteUrl = function(url, urlRegex, rewriteUrl) {};

/**
 * Clears all URL block and rewrite rules.
 *
 * @example
 * // Open site with ads blocked, and then without ads blocked
 * var b = pizza.open();
 * b.blockUrl("ad.doubleclick.net");
 * b.open("www.mysite.com");
 * // Remove the rule to block ads
 * b.clearRules();
 * b.open("www.mysite.com");
 *
 * @see module:Browser#rewriteUrl
 * @see module:Browser#blockUrl
 */
Browser.prototype.clearRules = function() {};

/**
 * Override/Add a header
 *
 * @example
 * // Add a custom header
 * var b = pizza.open();
 * b.setHeader('my-custom-header', 'this is the header');
 * b.open("mysite.com");
 *
 * @example
 * // Overwrite an existing header
 * var b = pizza.open();
 * b.setHeader('Host', 'mysite2.com');
 * b.open("mysite.com");
 *
 * @param {String} name The name of the header to set/override
 * @param {String} value The value to set for the header
 */
Browser.prototype.setHeader = function(name, value) {};

/**
 * Remove a given header
 *
 * Removes a header that was previous added/overridden
 *
 * Overridden headers are no longer overridden and the original header is still sent.
 *
 * @example
 * var b = pizza.open();
 * // Load the site with the header set
 * b.setHeader('my-custom-header', 'this is the header');
 * b.open("mysite.com");
 * // Load the site without the header set
 * b.removeHeader('my-custom-header';
 * b.open("mysite.com");
 *
 * @param {String} header The header to remove
 */
Browser.prototype.removeHeader = function(header) {};

/**
 * Set the user agent to something else.
 *
 * Set to an empty string to set back to the default.
 *
 * Sets both the HTTP request headers and the JavaScript navigator.userAgent
 *
 * @example
 * var b = pizza.open();
 * // Pretend to be Internet Explorer 11.
 * b.setUserAgent('Mozilla/5.0 (compatible, MSIE 11, Windows NT 6.3; Trident/7.0; rv:11.0) like Gecko');
 * b.open("www.mysite.com");
 *
 * @param {String} userAgent The user agent to set
 */
Browser.prototype.setUserAgent = function(userAgent) {};

/**
 * Set the browser credentials.
 *
 * These will be automatically entered whenever a credentials dialog
 * would normally show up.  Works with Basic Auth and all other forms
 * of HTTP authentication supported by Chrome.
 *
 * @example
 * // Open with a page with the credentials already set
 * var b = pizza.open();
 * b.setAuth('username', 'password');
 * b.open("www.mysite.com");
 *
 * @param {String} username The username
 * @param {String} password The password
 */
Browser.prototype.setAuth = function(username, password) {};

/**
 * Clear the browser cache
 *
 * Clears both the disk cache and the in memory cache.
 *
 * @example
 * // Load one page
 * var b = pizza.open();
 * b.open("www.mysite.com/page1");
 * // Now load another page on the same site, but clear the cache first
 * b.clearCache();
 * b.open("www.mysite.com/page2");
 */
Browser.prototype.clearCache = function() {};

/**
 * Clear all browser cookies
 *
 * @example
 * // Load one page
 * var b = pizza.open();
 * b.open("www.mysite.com/page1");
 * // Now load another page on the same site, but clear the cookies first
 * b.clearCookies();
 * b.open("www.mysite.com/page2");
 */
Browser.prototype.clearCookies = function() {};

/**
 * Set a cookie
 *
 * Set a cookie with the given name/value for the current domain.
 *
 * Optional info can be given such as the expiry and the url.
 *
 * @example
 * // Set a cookie for the 'www.google.com' domain
 * b.setCookie("a", "b", { url: "http://www.google.com" })
 *
 * @example
 * // Load the site, then fake a login cookie
 * var b = pizza.open("www.mysite.com/");
 * b.setCookie("loginCreds", "dj431g5hgk");
 * b.open("www.mysite.com/");
 *
 * @param {String} name the name of the cookie to be set, can a new name or and existing
 * @param {String} value the value of the cookie to set
 * @param {module:Cookie=} details optional extra info such as the expiry and the domain
 */
Browser.prototype.setCookie = function(name, value, details) {};

/**
 * Get a cookie
 *
 * Given a cookie name, get details about the cookie.  The current domain is used.
 *
 * @param {String} name the name of the cookie to get
 *
 * @return {module:Cookie} the cookie info
 */
Browser.prototype.getCookie = function(name) {};

/**
 * Get a cookie
 *
 * Given a URL and cookie name, get details about the cookie.
 *
 * @param {String} url the url of the cookie
 * @param {String} name the name of the cookie to get
 *
 * @return {module:Cookie} the cookie info
 */
Browser.prototype.getCookie = function(url, name) {};

/**
 * Remove a given cookie from the browser for the current domain
 *
 * @param {String} name the name of the cookie to remove
 */
Browser.prototype.removeCookie = function(name) {};

/**
 * Remove a given cookie from the browser
 *
 * @param {String} url the domain of the cookie to remove
 * @param {String} name the name of the cookie to remove
 */
Browser.prototype.removeCookie = function(url, name) {};

/**
 * Click the first item matching the given selector.
 *
 * If this click() navigates to a new page, call waitPageLoad() to
 * wait for the new page to load.  If the page is opened in a new
 * tab then you need to select the new tab before waiting.
 *
 * @example
 * // Click an element with id 'search'
 * b.click("#search");
 * // Wait for the new page to load
 * b.waitPageLoad();
 *
 * @throws Throws an exception if the element can not be found
 * @throws Throws an exception if the element is not visible
 * @param {String} selector the element to click
 */
Browser.prototype.click = function(selector) {};

/**
 * Click the given item at the given location, emulating all JavaScript events
 *
 * @example
 * b.clickAt("#submit", 20, 20);
 *
 * @param {String} selector the element to click
 * @param {Number} x the x offset within the given element to click on
 * @param {Number} y the y offset within the given element to click on
 *
 * @throws Throws an exception if the element can not be found
 * @throws Throws an exception if the element is not visible
 */
Browser.prototype.clickAt = function(selector, x, y) {};

/**
 * Focus the first item matching the given selector, emulating
 * focus/blur JavaScript events.
 *
 * @param selector the element to focus
 */
Browser.prototype.focus = function(selector) {};

/**
 * Hover over the first item matching the given selector, emulating
 * all JavaScript events.
 *
 * @param selector the element to hover over
 */
Browser.prototype.hover = function(selector) {};

/**
 * Hover over the first item matching the given selector at the given
 * location, emulating all JavaScript events
 *
 * @example
 * // Hover over a drop down
 * b.hover("#myDropDown");
 * // Wait for a link to be displayed
 * b.waitForVisible("#myMenuItem");
 * // Now click the link
 * b.click("#myMenuItem");
 * b.waitPageLoad();
 *
 * @param selector the element to hover over
 * @param x the x offset within the given element
 * @param y the y offset within the given element
 * @throws Throws an exception if the element can not be found
 * @throws Throws an exception if the element is not visible
 */
Browser.prototype.hoverAt = function(selector, x, y) {};

/**
 * Double click the given item, emulating all JavaScript events
 *
 * @example
 * b.doubleClick("#submit");
 *
 * @throws Throws an exception if the element can not be found
 * @throws Throws an exception if the element is not visible
 * @param {String} selector the element to click
 */
Browser.prototype.doubleClick = function(selector) {};

/**
 * Double click the given item at the given location, emulating all JavaScript events
 *
 * @param {String} selector the element to click
 * @param {Number} x the x offset within the given element to click on
 * @param {Number} y the y offset within the given element to click on
 *
 * @throws Throws an exception if the element can not be found
 * @throws Throws an exception if the element is not visible
 */
Browser.prototype.doubleClickAt = function(selector, x, y) {};

/**
 * Type the given text into the first item matching the given selector
 *
 * First it focuses the element (blurring the previously focused element),
 * then it emulates key presses for the given text.
 *
 * @example
 * // Type the text 'Hello' into a text input with id 'id1'
 * b.type("#id1", "Hello");
 *
 * @example
 * // Type Pizza and then press enter, submitting the form.
 * b.type("#search", "Pizza\n");
 *
 * @example
 * // Type Pizza and then press tab, moving focus to next input element
 * b.type("#search", "Pizza\t");
 *
 * @example
 * // Can also send shortcuts (This only works to web elements, not
 * // the browser and certain system shortcuts like copy and paste
 * // are disabled).
 * b.type("#search", [Key.Control, Key.K]);
 *
 * @param {String} selector the element to type text into
 * @param {...String} text the text to type
 */
Browser.prototype.type = function(selector, text) {};

/**
 * Clear the input element text matching the given selector
 *
 * @example
 * // Clear the text in an input element with id 'id1'
 * b.clear("#id1");
 *
 * @example
 * // Clear the text in an input element with id 'id1'
 * b.clear("#input1");
 * b.type("#input1", Key.Backspace);
 *
 * @param {String} selector the element to type text into
 * @throws Throws an exception if the element can not be found
 */
Browser.prototype.clear = function(selector) {};

/**
 * Select the content of the given element
 *
 * Right now, basically only useful in combination with type()
 * as an alternative to clear().
 *
 * @example
 * // Clear the text in an input element with id 'id1'
 * b.selectContent("#input1");
 * b.type("#input1", Key.Backspace);
 *
 * @param {String} selector the element to select
 * @throws Throws an exception if the element can not be found
 */
Browser.prototype.selectContent = function(selector) {};

/**
 * Check the a checkbox matching the given selector
 *
 * By default this function will check the checkbox.
 *
 * @example
 * // Check the checkbox with id 'checkBox1'
 * b.check("#checkBox1");
 *
 * @throws Throws an exception if the element can not be found
 * @param {String} selector the checkbox element to check
 * @param {Boolean=} on Check on/true(default) or off/false.
 */
Browser.prototype.check = function(selector, on) {};

/**
 * Select the given item/items of a dropdown or combobox
 *
 * @example
 * // Select by index
 * select("#toppings", {index: 1})
 *
 * @example
 * // Select by value
 * select("#toppings", {value: "option1"})
 *
 * @example
 * // Select by text
 * select("#toppings", {text: "Onions"})
 *
 * @example
 * // Select by text matching Regex
 * select("#toppings", {match: ".nions"})
 *
 * @example
 * // Select multiple
 * select("#toppings", {value: ["option1", "option2"]})
 * select("#toppings", {text: ["Onions", "Pepperoni"})
 * select("#toppings", {match: [".nions", ".epperoni"})
 * select("#toppings", {index: [2, 3})
 *
 * @example
 * // Select multiple (but don't clear previous selection)
 * select("#toppings", {value: ["option1", "option2"], clear: false})
 * select("#toppings", {text: ["Onions", "Pepperoni"], clear: false})
 * select("#toppings", {match: [".nions", ".epperoni"], clear: false})
 * select("#toppings", {index: [2, 3], clear: false})
 *
 * @param {String} selector the select element to select from
 * @param {Select} value the values to select
 * @throws Throws an exception if the element can not be found
 */
Browser.prototype.select = function(selector, value) {};

/**
 * Gets the value of given input form item
 *
 * @example
 * assert.eq(b.getValue('#checkbox1'), 'on');
 *
 * @param {String} selector the select element to select from
 * @return {Object} the value of the given input form item
 * @throws Throws an exception if the element can not be found
 */
Browser.prototype.getValue = function(selector) {};

/**
 * Set the given input form item to the given value
 *
 * Directly sets without causing any input events to be fired.
 *
 * @example
 * b.setValue('#textbox1', 'something');
 *
 * @param {String} selector the select element to select from
 * @param {String} value the values to select
 * @throws Throws an exception if the element can not be found
 */
Browser.prototype.setValue = function(selector, value) {};

/**
 * Get the html inside the given element
 *
 * @example
 * var html = b.getInnerHTML('#input1');
 *
 * @param {String} selector the element to get the HTML from
 * @return {String} the inner HTML for the given element
 * @throws Throws an exception if the element can not be found
 */
Browser.prototype.getInnerHTML = function(selector) {};

/**
 * Get the text inside the given element
 *
 * @example
 * var text = b.getInnerText('#button1');
 *
 * @param {String} selector the element to get the inner text from
 * @return {String} the inner text for the given element
 * @throws Throws an exception if the element can not be found
 */
Browser.prototype.getInnerText = function(selector) {};

/**
 * Submit a form
 *
 * The selector can point to any element in the form as well as the form itself.
 *
 * This calls .submit() on the form, if this fails you can also try click() or
 * type() on the submit button element.
 *
 * @example
 * // Submit a form
 * b.submit('#form1');
 *
 * @param {String} selector the form element to submit
 * @throws Throws an exception if the element can not be found
 */
Browser.prototype.submit = function(selector) {};

/**
 * Test if an element matching the given selector exists in the current frame/tab.
 *
 * @example
 * // Submit form1 if it exists otherwise submit form2.
 * if (b.exists('#form1')) {
 *     b.submit('#form1');
 * } else {
 *     b.submit('#form2');
 * }
 *
 * @param {String} selector the element to search for
 * @return {Boolean} true if the element exists
 */
Browser.prototype.exists = function(selector) {};

/**
 * Test if an element matching the given selector is visible
 * in the current frame/tab.
 *
 * @example
 * // Click button1 if it exists otherwise click button2.
 * if (b.isVisible('#button1')) {
 *     b.click('#button1');
 * } else {
 *     b.click('#button2');
 * }
 *
 * @param {String} selector the element to search for
 * @return {Boolean} true if the element is visible
 */
Browser.prototype.isVisible = function(selector) {};

/**
 * Verify an element matching the given selector exists in the current frame/tab.
 *
 * An exception is thrown if the element does not exist.
 *
 * @example
 * // Verify that the given element exists on the page
 * b.verifyExists('#button1');
 *
 * @param {String} selector the element to verify
 * @throws Throws an exception if the element can not be found
 */
Browser.prototype.verifyExists = function(selector) {};

/**
 * Verify no element matches the given selector exists in the current frame/tab.
 *
 * An exception is thrown if the element exists.
 *
 * @example
 * // Verify that the given element does not exist on the page
 * b.verifyNotExists('#errorDiv');
 *
 * @param {String} selector the element to verify
 * @throws Throws an exception if the element is found
 */
Browser.prototype.verifyNotExists = function(selector) {};

/**
 * Find the elements matching the given selector in the current frame.
 *
 * Does *not* throw an exception if the selector is not matched.  Instead an empty
 * array is returned.
 *
 * @param {String} selector the elements to query
 * @return {Element[]} an array containing info about each of the elements that matched the selector
 */
Browser.prototype.query = function(selector) {};

/**
 * Find the elements matching the given selector in the current frame.
 *
 * Does *not* throw an exception if the selector is not matched.  Instead an empty
 * array is returned.
 *
 * Returns only visible elements.
 *
 * @param {String} selector The elements to query
 * @return {Element[]} An array containing info about each of the elements that matched the selector
 */
Browser.prototype.queryVisible = function(selector) {};

/**
 * Wait for the given selector to be matched.
 *
 * @example
 * // Wait for an element to exist in the DOM
 * b.waitForElement('#divNew');
 *
 * @param {String} selector The element(s) to wait for
 */
Browser.prototype.waitForElement = function(selector) {};

/**
 * Wait for the given selector to match a visible item.
 *
 * @example
 * // Wait for an element to be visible
 * b.waitForVisible('#button1');
 *
 * @param {String} selector The element(s) to wait for
 */
Browser.prototype.waitForVisible = function(selector) {};

/**
 * Highlight the first element matching the selector
 *
 * Only one item at a time can be highlighted.  Selecting a second will remove the
 * highlight from the first.  Call clearHighlight() to remove the highlight.
 *
 * An optional color can be specified for the highlight.
 *
 *   a - The alpha component, in the [0-1] range
 *   r - The red component, in the [0-255] range
 *   g - The green component, in the [0-255] range
 *   b - The blue component, in the [0-255] range
 *
 * @example
 * // Highlight button with id 'button1'
 * b.highlight("#button1")
 *
 * @example
 * // Transparent blue highlight on first <button>:
 * b.highlight("button", { a: 0.2, r: 0, g: 0, b: 255 })
 *
 * @param {String} selector The element to select
 * @param {Color=} color The color of the highlight
 */
Browser.prototype.highlight = function(selector, color) {};

/**
 * Clears the highlight
 *
 * @example
 * b.clearHighlight();
 */
Browser.prototype.clearHighlight = function() {};

/**
 * Accept / Reject the currently displayed JavaScript dialog, if any.
 *
 * @param {Boolean|String=} val If true accept the dialog, if false reject, if a string fill out the
 * window.prompt() dialog with the given text.
 *
 * @example
 * var b = pizza.open();
 * b.openAsync("mysitewithdialog.com");
 * // Wait for dialog to open
 * pizza.waitFor(function() { return b.isDialogOpen(); });
 * b.handleDialog();
 *
 * @see module:Browser#dismissDialogs
 */
Browser.prototype.handleDialog = function(val) {};

/**
 * Dismiss all current and future JavaScript dialogs.
 *
 * @example
 * var b = pizza.open();
 * b.dismissDialogs();
 * b.open("mysitewithdialog.com");
 *
 * @see module:Browser#handleDialog
 */
Browser.prototype.dismissDialogs = function() {};

/**
 * Is a JavaScript modal dialog open?
 *
 * @example
 * var b = pizza.open();
 * b.openAsync("mysitewithdialog.com");
 * // Wait for dialog to open
 * pizza.waitFor(function() { return b.isDialogOpen(); });
 * b.handleDialog(true);
 *
 * @return {Boolean} true if a JavaScript modal dialog is open, false otherwise
 *
 * @see module:Browser#getOpenDialog
 */
Browser.prototype.isDialogOpen = function() {};

/**
 * Get details about the currently open JavaScript dialog.
 *
 * @example
 * // Make sure an alert dialog is opened with specific text
 * var b = pizza.open();
 * b.openAsync("mysitewithdialog.com");
 * var dialog = b.getOpenDialog();
 * assert.eq("alert\", dialog.type);
 * assert.eq("it's a dialog!", dialog.message);
 *
 * @return {module:DialogInfo} dialog info or null if no dialog open
 *
 * @see module:Browser#isDialogOpen
 */
Browser.prototype.getOpenDialog = function() {};

/**
 * List all cookies set in the browser
 *
 * @return {module:Cookie[]} array of cookies
 */
Browser.prototype.listCookies = function() {};

/**
 * List all tabs/windows open in browser
 *
 * @return {module:Tab[]} array of tabs
 */
Browser.prototype.listTabs = function() {};

/**
 * Select the given tab
 *
 * @example
 * // By partial URL
 * selectTab({url: 'url'});
 *
 * @example
 * // By full URL
 * selectTab({url: 'url', full=true});
 *
 * @example
 * // By regex URL
 * selectTab({url: 'regex', regex=true});
 *
 * @example
 * // By partial title
 * selectTab({title: 'title'});
 *
 * @example
 * // By full title
 * selectTab({title: 'title', full=true});
 *
 * @example
 * // By regex title
 * selectTab({title: 'regex', regex=true});
 *
 * @example
 * // By Index (1st tab starts at 0)
 * selectTab({index: 0});
 *
 * @param {Object} obj the tab selector
 * @return {module:Tab} info about the selected tab
 */
Browser.prototype.selectTab = function(obj) {};

/**
 * Select the most recently opened new tab.
 *
 * @return {module:Tab} info about the selected tab
 */
Browser.prototype.selectLatestTab = function() {};

/**
 * Select the main tab.
 *
 * Select the main tab, this is the first tab opened, if the first tab was closed
 * the oldest tab still around is select.
 *
 * @return {module:Tab} info about the selected tab.
 */
Browser.prototype.selectMainTab = function() {};

/**
 * Open a new tab
 *
 * @return {module:Tab} info about the selected tab.
 */
Browser.prototype.newTab = function() {};

/**
 * Get the browser name.
 *
 * E.g. "Google Chrome".
 *
 * @example
 * console.log("Running test with browser: " + b.getBrowserName());
 *
 * @return {String} the browser name
 */
Browser.prototype.getBrowserName = function() {};

/**
 * Get Version of the browser we are using
 *
 * @example
 * console.log("Running test with browser version: " + b.getBrowserVersion());
 *
 * @return {String} the version of the browser that was launched
 */
Browser.prototype.getBrowserVersion = function() {};

/**
 * Go back to the previous page.
 *
 * 4xx and 5xx responses on the main document trigger a failure after the page
 * has loaded.
 *
 * @throws Throws an exception when a 4xx or 5xx status code is returned for
 * main document.
 *
 * @see module:Browser#ignoreHttpErrors
 */
Browser.prototype.back = function() {};

/**
 * Go forward in the history.
 *
 * 4xx and 5xx responses on the main document trigger a failure after the page
 * has loaded.
 *
 * @throws Throws an exception when a 4xx or 5xx status code is returned for
 * main document.
 *
 * @see module:Browser#ignoreHttpErrors
 */
Browser.prototype.forward = function() {};

/**
 * Reload the current page.
 *
 * 4xx and 5xx responses on the main document trigger a failure after the page
 * has loaded.
 *
 * @throws Throws an exception when a 4xx or 5xx status code is returned for
 * main document.
 *
 * @see module:Browser#ignoreHttpErrors
 */
Browser.prototype.reload = function() {};

/**
 * Don't trigger an error on HTTP 4xx or 5xx status codes during a
 * call to open(), waitPageLoad(), reload(), back() or forward().
 *
 * @param {(Boolean|...Number)} [ignore=true] if true errors will be ignored (the default)
 * if set to false, errors will trigger the above methods to throw an exception again.
 * If set to a list of numbers any status codes that match these numbers won't throw
 * an exception.
 *
 * @example
 * b.ignoreHttpErrors(); // same as b.ignoreHttpErrors(true)
 * // Go to a 404 page but don't throw an error
 * b.open("mysite.com/404");
 *
 * @example
 * // Only ignore 404 errors, other 4xx and 5xx status codes will still
 * // throw an error
 * b.ignoreHttpErrors(404);
 * b.open("mysite.com/404");
 *
 * @example
 * // Turn back on http errors for all 4xx and 5xx status codes.
 * // You only need to do this if you turned off the error check previously.
 * b.ignoreHttpErrors(false);
 * b.open("mysite.com/404");
 *
 * @see module:pizza.open
 * @see module:Browser#open
 * @see module:Browser#openAsync
 */
Browser.prototype.ignoreHttpErrors = function(ignore) {};

/**
 * Take a screenshot.
 *
 * The format can be any of 'webp', 'png' or 'jpeg'.
 *
 * If on a high DPI display, the screenshot is scaled down by the devicePixelRatio.
 *
 * @param {String} [format='png'] The format of the image.  Can be any of 'webp', 'png'
 * or 'jpeg'.
 *
 * @param {Number} [quality='1.0'] The quality of the image.  0.0 is least and smallest
 * size, 1.0 is best quality and largest image size.
 *
 * @example
 * var d = b.screenshot();
 */
Browser.prototype.screenshot = function(format, quality) {};

/**
 * List devices available for emulation
 *
 * @example
 * b = pizza.open();
 * b.listDevices();
 *
 * @see module:Browser#emulateDevice
 * @see module:Browser#listNetworkConditions
 */
Browser.prototype.listDevices = function() {};

/**
 * Emulate the given device's rendering.
 *
 * The viewport & device pixel ratio will be scaled to match the rendering
 * of the device.  The user agent is also changed.
 *
 * Only applies to new pages that are loaded (not the current).
 *
 * @param {String} name The device to emulate.  Set to null to disable device
 * emulation.
 *
 * @example
 * // Open "apple.com" while emulating an iPhone 6.
 * b = pizza.open();
 * b.emulateDevice("Apple iPhone 6");
 * b.open("apple.com");
 *
 * @example
 * // Open normally in desktop mode, then reload emulating a Galaxy S4
 * b = pizza.open("samsung.com");
 * b.emulateDevice("Samsung Galaxy S4");
 * b.reload();
 *
 * @see module:Browser#listDevices
 * @see module:Browser#emulateNetworkCondition
 */
Browser.prototype.emulateDevice = function(name) {};

/**
 * List network conditions available for emulation
 *
 * @example
 * b = pizza.open();
 * b.listNetworkConditions();
 *
 * @see module:Browser#emulateNetworkCondition
 */
Browser.prototype.listNetworkConditions = function() {};

/**
 * Emulate the given network condition.
 *
 * @param {String|Object} name The network condition to emulate.  Set to {} to
 * disable network condition emulation.
 *
 * @example
 * // Open "bbc.com" emulating 3G latency and bandwidth constraints
 * b = pizza.open();
 * b.emulateNetworkCondition("Regular 3G");
 * b.open("bbc.com");
 *
 * @example
 * // Open "bbc.com" emulating a network latency of 50 milliseconds
 * b = pizza.open();
 * b.emulateNetworkCondition({latency: 50});
 * b.open("bbc.com");
 *
 * @example
 * // Disable a previously set network condition
 * b.emulateNetworkCondition({});
 * b.open("bbc.com");
 *
 * @see module:Browser#listNetworkConditions
 * @see module:Browser#emulateDevice
 */
Browser.prototype.emulateNetworkCondition = function(name) {};

/**
 * Close the browser process.
 *
 * The browser object cannot be used after this call.
 *
 * @example
 * // Open the browser at a URL close and open a new URL in a
 * // newly launched browser.
 * var b = pizza.open("www.google.com");
 * b.close();
 * b = pizza.open("www.bing.com");
 *
 * @see module:Browser#open
 */
Browser.prototype.close = function() {};

/**
 * Stats about a page
 *
 * Any page that is navigated to is saved in the test results.
 *
 * Provides access to requests made by the browser.
 *
 * Allows the page name to be queried/changed.
 *
 * See {@link module:Browser#open} {@link module:Browser#newPage}
 *
 * @exports Page
 */
Page = {};

/**
 * The page name.
 *
 * By default the page name is null.
 *
 * @see module:Browser#newPage
 *
 * @type {String}
 */
Page.prototype.name = null;

/**
 * The requests made by the browser
 *
 * @type {module:Request[]}
 */
Page.prototype.requests = [];

/**
 * The error if any that occurred when navigating to this page
 *
 * @type {String}
 */
Page.prototype.error = "";

/**
 * The main HTTP protocol used to issue the page navigation.
 *
 * @type {String}
 */
Page.prototype.protocol = "";

/**
 * The initial URL issue to navigate to this page.
 *
 * @type {String}
 */
Page.prototype.origUrl = "";

/**
 * The URL of the page once any redirects have been followed.
 *
 * @type {String}
 */
Page.prototype.url = "";

/**
 * The internal frame id of the page's root frame.
 *
 * @type {String}
 */
Page.prototype.frameId = "";

/**
 * The internal tab id of the page.
 *
 * @type {String}
 */
Page.prototype.tabId = "";

/**
 * Number of frames on the page after it finished loading
 *
 * @type {String}
 */
Page.prototype.numFrames = "";

/**
 *  The internal process id that loaded the page
 *
 * @type {String}
 */
Page.prototype.processId = "";

/**
 *  The unique id of the page in this test
 *
 * @type {String}
 */
Page.prototype.pageId = "";

/**
 * The state of the page, let's you know if the page has
 * loaded fully or if there has been an error.
 *
 * @type {String}
 */
Page.prototype.state = "";

/**
 * Number of dom elements once the page has fully loaded.
 *
 * @type {String}
 */
Page.prototype.numDomElements = "";

/**
 * The navigation event that caused the page to be loaded.
 * Examples include: Unknown, Link, Typed, AutoBookmark, AutoSubFrame, ManualSubFrame, Generated,
 * StartPage, FormSubmit, Reload, Keyword, KeywordGenerated
 *
 * @type {String}
 */
Page.prototype.navigationType = "";

/**
 *
 * @type {Date}
 */
Page.prototype.navStartTime = new Date();

/**
 *
 * @type {Date}
 */
Page.prototype.navCommitTime = new Date();

/**
 *
 * @type {Date}
 */
Page.prototype.domContentLoadedTime = new Date();

/**
 *
 * @type {Date}
 */
Page.prototype.navEndTime = new Date();

/**
 *
 * @type {Date}
 */
Page.prototype.firstPaintTime = new Date();

/**
 *
 * @type {Date}
 */
Page.prototype.firstPaintAfterLoadTime = new Date();

/**
 * A browser tab
 *
 * See {@link module:Browser#listTabs}
 *
 * @exports Tab
 */
Tab = {};

/**
 *
 * @type {String}
 */
Tab.prototype.title = "";

/**
 *
 * @type {String}
 */
Tab.prototype.url = "";

/**
 *
 * @type {Number}
 */
Tab.prototype.width = 0;

/**
 *
 * @type {Number}
 */
Tab.prototype.height = 0;

/**
 *
 * @type {Boolean}
 */
Tab.prototype.active = false;

/**
 *
 * @type {Number}
 */
Tab.prototype.index = 0;

/**
 *
 * @type {Number}
 */
Tab.prototype.status = 0;


/**
 * Web Cookie
 *
 * See {@link module:Browser#listCookies}
 *
 * @exports Cookie
 */
Cookie = {};

/**
 *
 * @type {String}
 */
Cookie.prototype.name = "";

/**
 *
 * @type {String}
 */
Cookie.prototype.value = "";

/**
 *
 * @type {String}
 */
Cookie.prototype.path = "";

/**
 *
 * @type {String}
 */
Cookie.prototype.domain = "";

/**
 *
 * @type {String}
 */
Cookie.prototype.expirationDate = "";

/**
 *
 * @type {Boolean}
 */
Cookie.prototype.session = false;

/**
 *
 * @type {Boolean}
 */
Cookie.prototype.hostOnly = false;

/**
 *
 * @type {Boolean}
 */
Cookie.prototype.secure = false;

/**
 *
 * @type {Number}
 */
Cookie.prototype.storeId = 0;

/**
 * Frame Info Object
 *
 * See {@link module:Browser#listFrames}
 *
 * @exports Frame
 */
Frame = {};

/**
 *
 * @type {String}
 */
Frame.prototype.src = "";

/**
 *
 * @type {String}
 */
Frame.prototype.frameId = "";

/**
 *
 * @type {String}
 */
Frame.prototype.frame = "";

/**
 * The result of this sample/test.
 *
 * See {@link module:pizza.result}
 *
 * @exports TestResult
 */
TestResult = {};

/**
 *
 * @type {String}
 */
TestResult.prototype.startTime = "";

/**
 * The time spent opening the browser
 *
 * @type {number}
 */
TestResult.prototype.setupTime = "";

/**
 *
 * @type {number}
 */
TestResult.prototype.runTime = "";

/**
 *
 * @type {TestError}
 */
TestResult.prototype.error = "";

/**
 *
 * @type {String}
 */
TestResult.prototype.browserName = "";

/**
 *
 * @type {String}
 */
TestResult.prototype.botName = "";

/**
 *
 * @type {String}
 */
TestResult.prototype.browserVersion = "";

/**
 *
 * @type {String}
 */
TestResult.prototype.ip = "";

/**
 *
 * @type {String}
 */
TestResult.prototype.testName = "";

/**
 * The test error to report
 *
 * See {@link module:TestResult#error}
 *
 * @exports TestError
 */
TestError = {};

/**
 *
 * @type {String}
 */
TestError.prototype.message = "";

/**
 *
 * @type {Number}
 */
TestError.prototype.line = 1;

/**
 *
 * @type {Number}
 */
TestError.prototype.column = 1;

/**
 *
 * @type {String}
 */
TestError.prototype.file = "";

/**
 * A HTTP request made by the browser
 *
 * @exports HttpRequest
 */
HttpRequest = {};

/**
 *
 * The HTTP method used in the request (GET, PUT, POST, etc)
 *
 * @type {String}
 */
HttpRequest.prototype.method = "";

/**
 *
 * @type {String}
 */
HttpRequest.prototype.url;

/**
 *
 * @type {String}
 */
HttpRequest.prototype.protocol;

/**
 *
 * @type {HttpHeader[]}
 */
HttpRequest.prototype.requestHeaders = [];

/**
 *
 * @type {String}
 */
HttpRequest.prototype.requestBodySize = "";

/**
 *
 * @type {Number}
 */
HttpRequest.prototype.statusCode = "";

/**
 *
 * @type {String}
 */
HttpRequest.prototype.statusText = "";

/**
 *
 * @type {HttpHeader[]}
 */
HttpRequest.prototype.responseHeaders = "";

/**
 *
 * @type {Number}
 */
HttpRequest.prototype.bodySize;

/**
 *
 * @type {Number}
 */
HttpRequest.prototype.bytesRecvCompressed = -1;

/**
 * The redirect URL in the 'Location:' response header if there
 * was one.
 *
 * @type {String}
 */
HttpRequest.prototype.redirectUrl = "";

/**
 *
 * @type {Number}
 */
HttpRequest.prototype.requestId = 0;

/**
 *
 * @type {Number}
 */
HttpRequest.prototype.frameId = 0;

/**
 *
 * @type {Number}
 */
HttpRequest.prototype.parentFrameId = 0;

/**
 *
 * @type {Number}
 */
HttpRequest.prototype.tabId = 0;

/**
 *
 * One of Document, Image, Script, Font, Stylesheet, Object,
 * XmlHttpRequest, WebSocket, Other
 *
 * @type {String}
 */
HttpRequest.prototype.resourceType = "";

/**
 * Was the data already cached?
 *
 * @type {Boolean}
 */
HttpRequest.prototype.fromCache = false;

/**
 * The actual ipv4 or ipv6 that the request was made to
 *
 * @type {String}
 */
HttpRequest.prototype.ip = "";

/**
 *
 * @type {String}
 */
HttpRequest.prototype.host = "";

/**
 * Error message when there was an error making the request
 * or an invalid response was reported.
 *
 * @type {String}
 */
HttpRequest.prototype.error = "";

/**
 *
 * @type {Number}
 */
HttpRequest.prototype.startTime = 0;

/**
 *
 * @type {Number}
 */
HttpRequest.prototype.dnsStart = -1;

/**
 *
 * @type {Number}
 */
HttpRequest.prototype.dnsEnd = -1;

/**
 *
 * @type {Number}
 */
HttpRequest.prototype.connectStart = -1;

/**
 *
 * @type {Number}
 */
HttpRequest.prototype.sslStart = -1;

/**
 *
 * @type {Number}
 */
HttpRequest.prototype.sslEnd = -1;

/**
 *
 * @type {Number}
 */
HttpRequest.prototype.connectEnd = -1;

/**
 *
 * @type {Number}
 */
HttpRequest.prototype.sendStart = -1;

/**
 *
 * @type {Number}
 */
HttpRequest.prototype.sendEnd = -1;

/**
 *
 * @type {Number}
 */
HttpRequest.prototype.recvHeadersEnd = -1;

/**
 *
 * @type {Number}
 */
HttpRequest.prototype.recvEnd = -1;

/**
 *
 * @type {Boolean}
 */
HttpRequest.prototype.connectionReused;

/**
 *
 * @type {Number}
 */
HttpRequest.prototype.connectionId;

/**
 *
 * @type {Number}
 */
HttpRequest.prototype.responseHeadersSize;

/**
 *
 * @type {Number}
 */
HttpRequest.prototype.requestHeadersSize;

/**
 *
 * @type {Number}
 */
HttpRequest.prototype.blockedTime;

/**
 * One of Init, Send, Recv, Complete
 *
 * @type {String}
 */
HttpRequest.prototype.state = "";

/**
 *
 * @type {String}
 */
HttpRequest.prototype.postData = "";

/**
 *
 * @type {String}
 */
HttpRequest.prototype.mimeType = "";

/**
 *
 * @type {String}
 */
HttpRequest.prototype.initiatorUrl = "";

/**
 * A HTTP request/response header
 *
 * @exports HttpHeader
 */
HttpHeader = {};

/**
 *
 * @type {String}
 */
HttpHeader.prototype.SET_COOKIE = "Set-Cookie";

/**
 *
 * @type {String}
 */
HttpHeader.prototype.COOKIE = "Cookie";

/**
 *
 * @type {String}
 */
HttpHeader.prototype.CONTENT_TYPE = "Content-Type";

/**
 *
 * @type {String}
 */
HttpHeader.prototype.CONTENT_LENGTH = "Content-Length";

/**
 *
 * @type {String}
 */
HttpHeader.prototype.name = "";

/**
 *
 * @type {String}
 */
HttpHeader.prototype.value = "";

/**
 * Info about JavaScript dialogs
 */
DialogInfo = {};

/**
 * The type of JavaScript dialog.
 *
 * Valid dialog types are: "alert, "confirm", "prompt", "beforeunload".
 *
 * @type {String}
 */
DialogInfo.type = "";

/**
 * The message displayed in the dialog.
 *
 * @type {String}
 */
DialogInfo.message = "";

/**
 * Read CSV data
 *
 * See {@link module:pizza.openCSV}
 *
 * @exports CSV
 */
CSV = {};

/**
 * Return the number of rows in the CSV file
 *
 * @return {Number} Number of rows in CSV file
 */
CSV.prototype.getLength = function() {};

/**
 * Returns the given row of the CSV file as an array of strings (one
 * value for each column).
 *
 * @param {Number} row The row to retrieve
 *
 * @return {String[]}
 */
CSV.prototype.row = function(row) {};

/**
 * Retrieve a random row in the CSV file
 *
 * @return {String[]}
 */
CSV.prototype.randomRow = function() {};

/**
 * Retrieve the value in the CSV table at the given row and column
 *
 * @param {Number} row The row to retrieve
 * @param {Number} column The column to retrieve
 *
 * @return {String}
 */
CSV.prototype.value = function(row, column) {};

/**
 * Retrieve a value for the given column in the CSV file
 *
 * @param {Number} column The column to retrieve
 *
 * @return {String}
 */
CSV.prototype.randomValue = function(column) {};

/**
 * Debug console for scripts.
 *
 * Can be used to debug scripts.  The debug output is displayed in the results page
 * for any scripts that are ran via the website.  Also works in the script editor.
 *
 * @exports console
 */
console = {};

/**
 * Logs the given parameters to the output.
 *
 * Takes an optional URL or browser settings parameter.  See the examples below.
 *
 * @example
 * console.log("hello"); // Displays: 'hello'
 *
 * @example
 * console.log("ello", "ello"); // Displays: 'ello ello'
 *
 * @example
 * var x = 1;
 * console.log("x:", x); // Displays: 'x: 1'
 *
 * @example
 * var x = 1;
 * var y = 2;
 * console.log("x:", x, " y: ", y); // Displays: 'x: 1 y: 2'
 *
 * @example
 * console.log({ a: 'b'}); // Displays: '{a: "b"}'
 *
 * @param {...Object} o The objects to print
 */
console.log = function(o) {};

/**
 * Loads a JavaScript file from the local file system and runs it.
 *
 * The file is loaded at the same scope, so any function or variables
 * defined in it will be available from the calling script.
 *
 * If the script is running sandboxed, the file must be specified as one of the
 * files used by the script.
 *
 * Files are loaded relative to the main script file being executed.
 *
 * @example
 * // Load the file 'foo.js', and execute it's JavaScript
 * load("foo.js"); // If 'foo.js' contains just 'x = "hello"', then the following will
 * console.log(x); // display the string 'hello'
 *
 * @example
 * // Load the file 'foo.js', and execute it's JavaScript
 * load("bar.js"); // If 'bar.js' contains:
 *                 //   function x() {
 *                 //     console.log("hello");
 *                 //   }
 * x();            // then the function x() can be called
 *
 * @example
 * // Load 'dir/foo.js'
 * load("dir/foo.js");
 *
 * @param file {String} the file to load
 * @exports load
 */
load = function(file) {};