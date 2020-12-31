package com.loadtestgo.script.api;

import org.mozilla.javascript.NativeFunction;
import org.mozilla.javascript.NativeObject;

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
     * @param url url to navigate to
     * @return Browser object for handling interactions with the page.
     */
    Browser open(String url);

    /**
     * Open the browser and navigate to the given url.
     * <p>
     * Function blocks until the page is open.
     * <p>
     * The url can be fully qualified or not.  If not 'http://' is prepended.
     *
     * @param url url to navigate to
     * @param timeoutMS timeout in milliseconds
     * @return Browser object for handling interactions with the page.
     */
    Browser open(String url, Long timeoutMS);

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
     * @param timeoutMS timeout in milliseconds
     */
    void waitFor(NativeFunction func, Long timeoutMS);

    /**
     * Wait for the given function to return true
     *
     * @param func
     * @param waitIterationMS the milliseconds to wait before calling func again
     * @param timeoutMS timeout in milliseconds
     */
    void waitFor(NativeFunction func, Long timeoutMS, long waitIterationMS);

    /**
     * Set the default timeout for wait functions in milliseconds.
     *
     * Initially this value is set to never timeout.
     *
     * Overall script timeout applies independently.
     *
     * @param timeoutMS the timeout in milliseconds.  Set to null or <=0 to never timeout.
     */
    void setWaitTimeout(Long timeoutMS);

    /**
     * Return the default timeout for wait functions in milliseconds.
     *
     * If the timeout is set to null or <=0 then wait functions that don't otherwise specify a timeout
     * never timeout on their own.
     */
    Long getWaitTimeout();

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

    long nextSeqId(String namedSequence);

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
