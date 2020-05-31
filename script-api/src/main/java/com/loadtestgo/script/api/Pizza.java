package com.loadtestgo.script.api;

import org.mozilla.javascript.NativeFunction;
import org.mozilla.javascript.NativeObject;

import java.io.FileNotFoundException;
import java.io.IOException;


/**
 * Main scripting interface
 */
public interface Pizza {
    /**
     * Open the browser.
     *
     * @return the newly opened browser.
     */
    Browser open();

    /**
     * Open the browser.
     *
     * @return the newly opened browser.
     */
    Browser open(NativeObject settings);

    /**
     * Open the browser and navigate to the given url.
     * <p>
     * Function blocks until the page is open.
     * <p>
     * The url can be fully qualified or not.  If not 'http://' is prepended.
     *
     * @param url
     * @return Browser object for handling interactions with the page.
     */
    Browser open(String url);

    /**
     * Returns the current browser if one is open
     *
     * @return the current browser if open, null otherwise
     */
    Browser browser();

    /**
     * Sleep for the given amount of milliseconds.
     * <p>
     * It's almost always better to wait on an element being visible or some
     * change to the DOM to happen, that having a hard wait.  If you run the
     * test enough times it's likely the sleep time won't be enough.
     * <p>
     * See also: waitFor(), Browser.waitForVisible(), Browser.waitForHttpRequests()
     *
     * @param milliseconds
     */
    void sleep(long milliseconds);

    /**
     * Wait for the given function to return true
     *
     * @param func
     */
    void waitFor(NativeFunction func);

    /**
     * Wait for the given function to return true
     *
     * @param func
     * @param waitIterationMilliseconds the milliseconds to wait before calling func again
     */
    void waitFor(NativeFunction func, long waitIterationMilliseconds);

    Page[] getPages();

    TestResult getResult();

    String getIp();

    String getVersion();

    String getLocation();

    int getLoadTestId();

    int getBotId();

    int getWorkerId();

    int getUserId();

    int getSequenceId();

    CSV openCSV(String filename) throws IOException;

    void saveFile(String name, Data data);

    void saveFile(String name, String data);

    HttpRequest getRequest(int requestIndex);

    HttpRequest getRequestByUrl(String partialUrl);

    HttpRequest getRequestByFullUrl(String fullUrl);

    HttpRequest getRequest(int pageIndex, int requestIndex);

    HttpRequest getRequestByUrl(int pageIndex, String partialUrl);

    HttpRequest getRequestByFullUrl(int pageIndex, String fullUrl);
}
