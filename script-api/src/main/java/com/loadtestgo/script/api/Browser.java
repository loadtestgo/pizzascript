package com.loadtestgo.script.api;

import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.regexp.NativeRegExp;

import java.util.HashMap;

/**
 * API for controlling the browser.
 *
 * Key Concepts
 *
 * 1. Element Selectors
 *
 *    Many functions here take selector strings to identify elements.  These selectors
 *    are just like CSS selectors but with a few minor additions to make them more
 *    convenient for browser automation.
 *
 * Additions to CSS selectors:
 *
 * - :contains()
 *
 *   Select only elements containing the given text.  This can be at the end of a selector
 *   or in the middle.  The selection is case sensitive.
 *
 *   Select the set of links containing 'Search':
 *
 *     'a:contains(Search)'
 *
 * - :icontains()
 *
 *   Case insensitive version of :contains().
 *
 *   Select the set of links containing 'Search' or 'search' or 'SEARCH':
 *
 *     'a:icontains(Search)'
 *
 * - :nth()/:eq()
 *
 *   Select the elements at the given index.  This can be at the end of a selector or
 *   in the middle.
 *
 *   Select the 3rd 'a' element:
 *
 *     'a:nth(2)'
 *     'a:eq(2)'
 *
 *   From the set of links containing 'Search', select the 3rd one.
 *
 *     'a:contains(Search):nth(2)'
 *     'a:contains(Search):eq(2)'
 *
 *   From the set of 'a' element, select the 3rd one if it contains the string 'Search'.
 *
 *     'a:nth(2):contains(Search)'
 *     'a:eq(2):contains(Search)'
 *
 *   Numeric ids are allowed:
 *
 *     '#0'
 *     '#0_0_0'
 *
 * - XPath
 *
 *   WARNING: XPath doesn't seem to work as it should.  Please send me minimal
 *   test cases that reproduce any problems you run into.
 *
 *   Selectors starting with 'xpath:' will do XPath match instead of CSS.  Some
 *   examples:
 *
 *   Any div element in the document:
 *
 *     'xpath://div'
 *
 *   Any div element containing a paragraph element:
 *
 *     'xpath://div/p'
 *
 *   The div element with the id 'div1':
 *
 *     'xpath://div[@id=\\'div1\\']'
 *
 *   A div element with the class 'class1':
 *
 *     'xpath://div[@class=\\'class1\\']'
 *
 *   Any button with the text 'Button1':
 *
 *     'xpath://button[text()=\\'Button1\\']'
 *
 * 2. Frames, Tabs and Window selections
 *
 *    Frames can be queried and selected.  To interact with elements it is required to
 *    first find the right frame.
 *
 *    The concept of windows does not exists, instead all tabs for all windows are listed.
 *    We might break this out in future but for our purposes it doesn't matter if a tab
 *    is really a new window or not.
 *
 * 3. It's also important to be aware that while your script is executing the browser is
 *    going about its business: loading the page, executing JavaScript, building the DOM,
 *    animating elements, etc.  As such elements can appear/disappear, become visible/
 *    invisible potentially even after the page has loaded.  Any script you write will
 *    have to keep this in mind.  For example it is possible to wait for an element to be
 *    visible in the DOM, but then when you try to click on the element it is gone again.
 *    In those cases you may need to wait in a loop, catch click failures and try again.
 *    We may add some kind of automatic wait for the item to be visible in future.
 */
public interface Browser {
    /**
     * Open the given URL
     * <p>
     * Function blocks until the page is open.
     * <p>
     * An exception is thrown if the navigation fails.
     * <p>
     * 4xx and 5xx responses don't trigger a failure, you have to check those
     * yourself.
     *
     * @param url the url to open
     * @return a new page object for the given page.
     */
    Page open(String url);

    /**
     * Open the given URL
     * <p>
     * Does not wait for the page to load.
     * <p>
     * Normally you would call this in conjunction with waitForHttpRequests() or waitPageLoad()
     *
     * @param url the url to open
     */
    void openAsync(String url);

    /**
     * Get the current URL the browser is pointing to
     *
     * @return the URL the browser is pointing to
     */
    String getUrl();

    /**
     * Wait for all ongoing HTTP requests to stop.
     * <p>
     * idleTimeMS is the minimum time to wait, this function will not return
     * before then.
     *
     * @parm idleTimeMS the time to wait after the last request has completed
     */
    void waitForHttpRequests(long idleTimeMS);

    /**
     * Wait for a new page to load on current tab.
     * <p>
     * If a page recently loaded, and that page was not waited on before (either
     * by open() or this function), then the function will just return.  If the
     * page opens in a new tab or window be sure to switch to that tab or window
     * first!
     */
    Page waitPageLoad();

    /**
     * Wait for a new page to load on the current tab.
     * <p>
     * If a page is recently loaded, and that page was not waited on before (either
     * by open() or this function), then the function will just return.  If the
     * page opens in a new tab or window be sure to switch to that tab or window
     * before calling waitPageLoad().
     *
     * @param timeoutMS the max time to wait in milliseconds
     */
    Page waitPageLoad(long timeoutMS);

    /**
     * Tells the browser to disregard any page loads that happened before this function
     * when waiting with waitPageLoad().
     * <p>
     * Normally you should call this before calling a function that triggers a new page
     * to load.  That way when waitPageLoad() is called you know that waitPageLoad()
     * waits on the new page to load, and does not just detect some previous page load.
     * <p>
     * Since JavaScript executing on the page can change the URL at any time, there
     * still be race conditions of course.  If you suspect this may happen or are
     * concerned that it may, then you can call the version of waitPageLoad() that
     * waits for a specific URL to load.
     */
    void clearPageLoad();

    /**
     * Starts a new page object.  Newly made requests are added to this page object.
     * Mostly you will only need to call this when a page makes XHR or Ajax requests
     * in the background in response to user interaction.  In this case the page won't
     * have any of the navigation info filled out because a navigation did not occur.
     */
    Page newPage();

    /**
     * Starts a new page object.
     * <p>
     * Newly made requests are added to this page object.
     * <p>
     * Mostly you will only need to call this for single page apps or when a page
     * makes XHR or Ajax requests in the background in response to user interaction.
     * In these cases the page won't have any of the navigation info filled out because
     * a navigation did not occur.
     * <p>
     * If no page navigation occurs the page load time is the total time content is
     * being downloaded.  That the time span from the first item to start download
     * until the last item is fully downloaded.
     *
     * @param pageName
     */
    Page newPage(String pageName);

    /**
     * Verify the given text exists on the page.
     * <p>
     * An exception is thrown if the text does not exist.
     *
     * @param text
     */
    void verifyText(String text);

    /**
     * Verify some text on the page matches the given regular expression.
     * <p>
     * An exception is thrown if no text matches the regexp.
     *
     * @param regexp the regex to search for in the page text
     */
    void verifyText(NativeRegExp regexp);

    /**
     * Verify that the text of the page does not contain the given string.
     * <p>
     * An exception is thrown if the given string exists on the page.
     *
     * @param text the text to look for on the page
     */
    void verifyNotText(String text);

    /**
     * Verify that the text of the page does not match the given regular expression.
     * <p>
     * An exception is thrown if there is a match.
     *
     * @param regexp the regex to search for in the page text
     */
    void verifyNotText(NativeRegExp regexp);

    /**
     * Verify the page title matches the given title.
     * <p>
     * An exception is thrown if no text matches the regexp.
     *
     * @param title the title to match
     */
    void verifyTitle(String title);

    /**
     * Check if the given text exists somewhere on the page.
     *
     * @param text the text to match
     */
    boolean hasText(String text);

    /**
     * Check if the given text exists somewhere on the page.
     *
     * @param regexp the regex to search for in the page text
     */
    boolean hasText(NativeRegExp regexp);

    /**
     * Wait for the current window / tab to contain the given text.
     */
    void waitText(String text);

    /**
     * Wait for the current window / tab to contain the given text.
     */
    void waitText(NativeRegExp regexp);

    /**
     * Wait for the current window / tab to not contain the given text.
     */
    void waitNotText(String text);

    /**
     * Wait for the current window / tab to not contain the given text.
     */
    void waitNotText(NativeRegExp regexp);

    /**
     * Return the page title.
     */
    String getTitle();

    /**
     * Verify the page title matches the given regular expression.
     *
     * @param regexp the regex to match against the title
     */
    void verifyTitle(NativeRegExp regexp);

    /**
     * Execute the given JavaScript in the current frame.
     * <p>
     * If there is an exception when the script is executed in the
     * browser it will be thrown here.
     *
     * @param script the JavaScript code to execute
     * @returns the result of the script execution
     */
    Object execute(String script);

    /**
     * Execute the given JavaScript jQuery expression.
     * <p>
     * Executes on all frames in the current tab.
     * <p>
     * If the normal automation functions fail or are limited in some way, jQuery to
     * the rescue!  jQuery selectors are typically a bit richer than the selectors
     * provided by us and since any javascript code can be executed, more complex
     * queries can be built up.
     * <p>
     * Some examples:
     * <p>
     * b.jq("$($('button')[1]).click()")
     * b.jq("$('button').css('border', '2px solid red')")
     *
     * @param script the JavaScript code to execute
     * @return returns an array of results for each frame that exists on the current tab
     */
    Object jq(String script);

    /**
     * List the sub-frames for the currently selected frame.
     *
     * @return An array of frames
     */
    Object listFrames();

    /**
     * List the sub-frames matching the given selector.
     * <p>
     * See selectFrame() for details on the selector.
     *
     * @param selector list any frames matching this selector
     * @return An array of frames
     */
    Object listFrames(String selector);

    /**
     * List all frames for the current window
     *
     * @return A nested array of frames
     */
    Object listAllFrames();

    /**
     * Select the given frame using a special frame selector.
     * <p>
     * An extended/modified css syntax is used.  The :nth operator can be used to
     * select the nth element.  For example, b.selectFrame("iframe:nth(1)") will
     * select the the second iframe in the document.  Spaces are used to select
     * nested frames.  Normally a css selector won't traverse sub frames, but this
     * will.  For example b.selectFrame("iframe iframe") will select the first iframe
     * within an iframe.  Once a frame is selected new calls to selectFrame()
     * operate on the previously selected frame.  To switch back to the main/top
     * frame for the page call selectTopFrame().
     * <p>
     * An exception is thrown if no frame matching the selector can be found.
     *
     * @param selector select the first frame matching this selector
     * @return Details about the frame selected.
     */
    Object selectFrame(String selector);

    /**
     * Select the given frame using a normal CSS selector.  None of the special
     * syntax allowed by selectFrame() is used.
     * <p>
     * An exception is thrown if no frame matching the selector can be found.
     *
     * @param selector select the first frame matching this selector
     * @return Details about the frame selected.
     */
    Object selectFrameCss(String selector);

    /**
     * Select the top level/main frame for the current window.
     *
     * @return the frame selected
     */
    Object selectTopFrame();

    /**
     * Block requests to the given URL..
     */
    void blockUrl(String url);

    void blockUrl(String... urls);

    void block3rdPartyUrls();

    /**
     * Rewrite requests to the given URL, match groups are supported.
     */
    void rewriteUrl(String url, String rewriteUrl);

    /**
     * Clears all URL block and rewrite rules.
     */
    void clearRules();

    /**
     * Override/Add a header
     */
    void setHeader(String name, String value);

    /**
     * Remove a given header
     */
    void removeHeader(String header);

    /**
     * Set the user agent to something else.
     * Set to an empty string to set back to the default.
     *
     * @param userAgent
     */
    void setUserAgent(String userAgent);

    /**
     * Set the browser credentials.
     * <p>
     * These will be automatically entered whenever a credentials dialog
     * would normally show up.
     *
     * @param username
     * @param password
     */
    void setAuth(String username, String password);

    /**
     * Clear the browser cache
     */
    void clearCache();

    /**
     * Clear all browser cookies
     */
    void clearCookies();

    /**
     * Set a cookie
     * <p>
     * Set a cookie with the given name/value for the current domain.
     *
     * @param name  the name of the cookie to set
     * @param value the value of the cookie to set
     */
    void setCookie(String name, String value);

    /**
     * Set a cookie.
     * <p>
     * Set a cookie with the given name/value for the current domain.
     * <p>
     * Optional info can be given such as the expiry and the url.
     * <p>
     * b.setCookie("a", "b", { url: "http://www.google.com" })
     *
     * @param name    the name of the cookie to be set, can a new name or and existing.
     * @param value   the value to assign to the cookie
     * @param details optional extra info such as the expiry and the domain
     */
    void setCookie(String name, String value, NativeObject details);

    /**
     * Get a cookie
     * <p>
     * Given a cookie name, get details about the cookie.  The current domain is used.
     *
     * @param name the name of the cookie to get
     * @return the cookie info
     */
    Object getCookie(String name);

    /**
     * Get a cookie
     * <p>
     * Given a URL and cookie name, get details about the cookie.
     *
     * @param url  the url of the cookie
     * @param name the name of the cookie to get
     * @return the cookie info
     */
    Object getCookie(String url, String name);

    /**
     * Remove a given cookie from the browser for the current domain
     *
     * @param name the name of the cookie to remove
     */
    void removeCookie(String name);

    /**
     * Remove a given cookie from the browser
     *
     * @param url  the domain of the cookie to remove
     * @param name the name of the cookie to remove
     */
    void removeCookie(String url, String name);

    /**
     * Click the first item matching the given selector.
     * <p>
     * If this click() navigates to a new page, call waitPageLoad() to
     * wait for the new page to load.  If the page is opened in a new
     * tab then you need to select the new tab before waiting.
     *
     * @param selector the element to click
     */
    void click(String selector);
    void click(String selector, NativeObject params);

    /**
     * Click the given item at the given location, emulating all JavaScript events
     *
     * @param selector the element to click
     * @param x        the x offset within the given element to click on
     * @param y        the y offset within the given element to click on
     */
    void clickAt(String selector, int x, int y);

    /**
     * Click the given item,, emulating all JavaScript events
     *
     * @param selector the element to click
     */
    void doubleClick(String selector);

    /**
     * Double click the given item at the given location, emulating all JavaScript events
     *
     * @param selector the element to click
     * @param x        the x offset within the given element to click on
     * @param y        the y offset within the given element to click on
     */
    void doubleClickAt(String selector, int x, int y);

    /**
     * Focus the first item matching the given selector, emulating
     * JavaScript focus/blur events.
     *
     * @param selector the element to focus
     */
    void focus(String selector);

    /**
     * Hover over the first item matching the given selector, emulating
     * all JavaScript events.
     *
     * @param selector the element to hover over
     */
    void hover(String selector);
    void hover(String selector, NativeObject params);

    /**
     * Hover over the first item matching the given selector at the given
     * location, emulating all JavaScript events
     *
     * @param selector the element to hover over
     * @param x        the x offset within the given element
     * @param y        the y offset within the given element
     */
    void hoverAt(String selector, int x, int y);

    /**
     * Type the given text into the first item matching the given selector
     *
     * @param selector the element to type text into
     * @param text     the text to type
     */
    void type(String selector, String text);    // Overloads are needed to get the right behavior

    void type(String selector, Number text);    // otherwise arrays are expanded to be the argument list

    void type(String selector, NativeArray text);

    void type(String selector, Object... text); // in this function <-

    /**
     * Clear the input element text matching the given selector
     *
     * @param selector the element to type text into
     */
    void clear(String selector);

    /**
     * Select the content of the given element
     * <p>
     * Basically only useful in combination with type() as an alternative to clear().
     *
     * @param selector the element to select
     * @example // Clear a textbox with id 'input1'
     * b.selectConnect("#input1");
     * b.type("#input1", Keys.Backspace);
     */
    void selectContent(String selector);

    /**
     * Check the a checkbox matching the given selector
     *
     * @param selector the checkbox element to check
     */
    void check(String selector);

    /**
     * Check/Uncheck the a checkbox matching the given selector
     *
     * @param selector the checkbox element to check on/off
     * @param on       check on(true)/off(false)
     */
    void check(String selector, boolean on);

    /**
     * Return checkbox state for the given selector
     *
     * @param selector the checkbox element to check on/off
     * @return true if checked
     */
    boolean checked(String selector);

    /**
     * Select the given item/items of a dropdown or combobox
     * <p>
     * Select by index:
     * select("#toppings", {index: 1})
     * <p>
     * Select by value:
     * select("#toppings", {value: "option1"})
     * <p>
     * Select by text:
     * select("#toppings", {text: "Onions"})
     * <p>
     * Select by text matching Regex:
     * select("#toppings", {match: ".nions"})
     * <p>
     * Select multiple:
     * select("#toppings", {value: ["option1", "option2"]})
     * select("#toppings", {text: ["Onions", "Pepperoni"]})
     * select("#toppings", {match: [".nions", ".epperoni"]})
     * select("#toppings", {index: [2, 3})
     * <p>
     * Select multiple (but don't clear previous selection):
     * select("#toppings", {value: ["option1", "option2"], clear: false})
     * select("#toppings", {text: ["Onions", "Pepperoni"], clear: false})
     * select("#toppings", {match: [".nions", ".epperoni"], clear: false})
     * select("#toppings", {index: [2, 3], clear: false})
     *
     * @param selector the select element to select from
     * @param value    the values to select
     */
    void select(String selector, NativeObject value);

    void select(String selector, String text);

    /**
     * Gets the value of given input form item
     *
     * @param selector the select element to select from
     * @return the value of the given input form item
     */
    Object getValue(String selector);

    /**
     * Set the given input form item to the given value
     * <p>
     * Directly sets without causing any input events to be fired.
     *
     * @param selector the select element to select from
     * @param value    the values to select
     */
    void setValue(String selector, String value);

    /**
     * Set the file name for the given input element
     *
     * @param selector the input file element to set
     * @param file     the file path of the file to upload
     */
    void setFile(String selector, String file);

    /**
     * Get the html for the inner part of currently selected frame.
     * <p>
     * By default the main frame of the page.
     *
     * @return the inner HTML for the current frame.
     */
    String getInnerHTML();

    /**
     * Get the html inside the given element
     *
     * @param selector the element to get the HTML from
     * @return the inner HTML for the given element
     */
    String getInnerHTML(String selector);

    /**
     * Get the html for the entire currently selected frame.
     * <p>
     * By default the main frame of the page.
     *
     * @return the outer HTML for the current frame.
     */
    String getOuterHTML();

    /**
     * Get the html for the given element
     *
     * @param selector the element to get the HTML for
     * @return the outer HTML for the given element
     */
    String getOuterHTML(String selector);

    /**
     * Get the all text for the current frame
     *
     * @return the text for the inner frame
     */
    String getInnerText();

    /**
     * Get the text inside the given element
     *
     * @param selector the element to get the inner text from
     * @return the inner text for the given element
     */
    String getInnerText(String selector);

    /**
     * Submit a form
     * <p>
     * The selector can point to any item in the form.
     * <p>
     * This calls .submit() on the form, if this fails you can also try click() or
     * keys() on the submit button element.
     *
     * @param selector the form element to submit
     */
    void submit(String selector);

    /**
     * Test if an element matching the given selector exists in the current frame/tab.
     *
     * @param selector the element to search for
     */
    boolean exists(String selector);

    /**
     * Test if an element matching the given selector exists in the current frame/tab.
     *
     * @param selector the element to search for
     */
    boolean isVisible(String selector);

    /**
     * Verify an element matching the given selector exists in the current frame/tab.
     * <p>
     * An exception is thrown if the element does not exist.
     *
     * @param selector the element to verify
     */
    void verifyExists(String selector);

    /**
     * Verify no element matches the given selector exists in the current frame/tab.
     * <p>
     * An exception is thrown if the element exists.
     *
     * @param selector the element to verify
     */
    void verifyNotExists(String selector);

    /**
     * Find the elements matching the given selector in the current frame.
     * <p>
     * Does *not* throw an exception if the selector is not matched.  Instead an empty
     * array is returned.
     *
     * @param selector the elements to query
     * @return an array containing info about each of the elements that matched the selector
     */
    Object query(String selector);

    /**
     * Find the elements matching the given selector in the current frame.
     * <p>
     * Does *not* throw an exception if the selector is not matched.  Instead an empty
     * array is returned.
     * <p>
     * Returns only visible elements.
     *
     * @param selector the elements to query
     * @return an array containing info about each of the elements that matched the selector
     */
    Object queryVisible(String selector);

    /**
     * Wait for the given selector to be matched.
     *
     * @param selector the element(s) to wait for
     */
    void waitElement(String selector);

    /**
     * Wait for the given selector to be matched.
     *
     * @param selector the element(s) to wait for
     */
    void waitVisible(String selector);

    /**
     * Wait for the element matching the given selector to contain the
     * given text
     *
     * @param selector the element(s) to wait for
     * @param text     the text to wait for
     */
    void waitElementText(String selector, String text);

    /**
     * Highlight the first element matching the selector
     * <p>
     * Only one item at a time can be highlighted.  Selecting a second will remove the
     * highlight from the first.  Call clearHighlight() to remove the highlight.
     *
     * @param selector the element to select
     */
    void highlight(String selector);

    /**
     * Highlight the first element matching the selector
     * <p>
     * Only one item at a time can be highlighted.  Selecting a second will remove the
     * highlight from the first.  Call clearHighlight() to remove the highlight.
     * <p>
     * An optional color can be specified for the highlight.
     * <p>
     * a - The alpha component, in the [0-1] range
     * r - The red component, in the [0-255] range
     * g - The green component, in the [0-255] range
     * b - The blue component, in the [0-255] range
     * <p>
     * E.g. for a transparent blue highlight:
     * highlight("button", { a: 0.2, r: 0, g: 0, b: 255 })
     *
     * @param selector the element to select
     * @param color    the color
     */
    void highlight(String selector, NativeObject color);

    /**
     * Clears the highlight
     */
    void clearHighlight();

    /**
     * Accept / Reject the currently displayed dialog, if any.
     */
    void handleDialog(boolean accept);

    /**
     * Fill the current window.prompt() JavaScript dialog with the given text.
     */
    void handleDialog(String msg);

    /**
     * Dismiss all current and future dialogs.
     */
    void dismissDialogs();

    /**
     * Is a JavaScript dialog open?
     */
    boolean isDialogOpen();

    /**
     * Get details about the currently open JavaScript dialog.
     * <p>
     * Returns null if no dialog open.
     */
    Object getOpenDialog();

    /**
     * List all cookies set in the browser
     *
     * @return array of cookies
     */
    Object listCookies();

    /**
     * List all tabs/windows open in browser
     *
     * @return array of tabs
     */
    Object listTabs();

    /**
     * Select the given tab
     * <p>
     * Some examples:
     * <p>
     * By partial URL:
     * {url: 'url'}
     * <p>
     * By full URL:
     * {url: 'url', full=true}
     * <p>
     * By regex URL:
     * {url: 'regex', regex=true}
     * <p>
     * By partial title:
     * {title: 'title'}
     * <p>
     * By full title
     * {title: 'title', full=true}
     * <p>
     * By regex title:
     * {title: 'regex', regex=true}
     * <p>
     * By Index (1st tab starts at 0):
     * {index: 0}
     * <p>
     * Returns info about the selected tab.
     */
    Object selectTab(NativeObject tab);

    /**
     * Select the most recently opened new tab.
     *
     * @return info about the selected tab.
     */
    Object selectLatestTab();

    /**
     * Select the main tab.
     * <p>
     * Select the main tab, this is the first tab opened, if the first tab was closed
     * the oldest tab still around is select.
     *
     * @return info about the selected tab.
     */
    Object selectMainTab();

    /**
     * Open a new tab
     *
     * @return info about the selected tab.
     */
    Object newTab();

    /**
     * Get the browser name.
     * <p>
     * E.g. "Google Chrome".
     *
     * @return the browser name
     */
    String getBrowserName();

    /**
     * Get Version of the browser we are using
     *
     * @return the version of the browser that was launched
     */
    String getBrowserVersion();

    /**
     * Go back to the previous page.
     */
    Page back();

    /**
     * Go forward in the history.
     */
    Page forward();

    /**
     * Reload the current page
     */
    Page reload();

    /**
     * Close the browser process.
     * <p>
     * The browser object cannot be used after this call.
     */
    void close();

    /**
     * Emulate the given device
     */
    void emulateDevice(String name);

    /**
     * List devices available for emulation
     */
    Object listDevices();

    void emulateNetworkCondition(NativeObject conditions);

    /**
     * Emulate the given network conditions
     */
    void emulateNetworkCondition(String name);

    /**
     * List network conditions available for emulation
     */
    Object listNetworkConditions();

    /**
     * Don't trigger an error on HTTP 4xx or 5xx status codes during a
     * call to open(), waitPageLoad(), reload(), back() or forward().
     */
    void ignoreHttpErrors();

    void ignoreHttpErrors(boolean ignore);

    void ignoreHttpErrors(Integer... statusCodesToIgnore);

    /*
     * Take a screenshot.
     *
     * Format can be any of 'webp', 'png' or 'jpeg'.
     *
     * If on a high DPI display, the screenshot is scaled down by the devicePixelRatio.
     */
    Data screenshot();

    Data screenshot(String format);

    Data screenshot(String format, double quality);

    Data getResponseBody(HttpRequest httpRequest);

    void verifyRequest(NativeRegExp regExp);

    void verifyRequest(String url);

    void startVideoCapture();

    void stopVideoCapture();
}