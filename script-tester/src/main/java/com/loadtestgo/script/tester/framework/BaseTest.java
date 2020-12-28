package com.loadtestgo.script.tester.framework;

import com.loadtestgo.script.api.ErrorType;
import com.loadtestgo.script.api.HttpRequest;
import com.loadtestgo.script.api.Page;
import com.loadtestgo.script.api.TestResult;
import com.loadtestgo.script.tester.server.TestServer;
import com.loadtestgo.util.Http;
import com.loadtestgo.util.IniFile;
import com.loadtestgo.util.Settings;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.pmw.tinylog.Logger;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class BaseTest {
    static TestServer server = null;
    static Settings settings = IniFile.settings();

    @Rule
    public TestName test = new TestName();

    public TestServer getServer() {
        if (server == null) {
            server = new TestServer();
            try {
                server.startDetached();
            } catch (InterruptedException e) {
                Logger.error(e, "Test Server start interrupted");
            }
        }
        return server;
    }

    public String getTestUrl(String url) {
        if (url.contains(":")) {
            return url;
        } else {
            TestServer server = getServer();
            return server.getBaseUrl() + url;
        }
    }

    public static void assertOnePage(TestResult result) {
        assertEquals(1, result.getPages().size());
    }

    /**
     * Check for only one request but ignore favicon.ico!
     */
    public static void assertOneRequest(TestResult result) {
        ArrayList<Page> pages = result.getPages();
        assertEquals(1, pages.size());
        assertOneRequest(pages.get(0));
    }

    /**
     * Check for only one request but ignore favicon.ico!
     */
    public static void assertOneRequest(Page page) {
        int numRequests = getNumRequests(page);
        assertTrue(String.format("Expected 1 but go %d requests (ignoring any /favicon.ico)", numRequests),
            numRequests == 1);
    }

    private static boolean isFavIcon(HttpRequest request) {
        Http.UrlDetails url = Http.parseUrl(request.url);
        return (url.path != null && url.path.equals("/favicon.ico"));
    }

    public static void assertNoPages(TestResult result) {
        assertEquals(0, result.getPages().size());
    }

    public static void assertNumPages(TestResult result, int num) {
        assertEquals(num, result.getPages().size());
    }

    public static void assertNoRequests(TestResult result) {
        ArrayList<Page> pages = result.getPages();
        for (Page page : pages) {
            assertEquals(0, page.getRequests().size());
        }
    }

    public static void assertNumRequests(int numRequests, TestResult result) {
        ArrayList<Page> pages = result.getPages();
        int requests = 0;
        for (Page page : pages) {
            requests += getNumRequests(page);
        }
        assertEquals(numRequests, requests);
    }

    public static void assertNumRequests(int numRequests, Page page) {
        assertEquals(numRequests, getNumRequests(page));
    }

    /**
     * Get the number of request (but ignore favicon.ico, this is downloaded by the
     * browser in the background and can be requested multiple times (0-n times)).
     * @param page
     * @return
     */
    static private int getNumRequests(Page page) {
        int requests = 0;
        for (HttpRequest request : page.getRequests()) {
            if (!isFavIcon(request)) {
                requests += 1;
            }
        }
        return requests;
    }

    public static void assertMoreThanOneRequest(TestResult result) {
        ArrayList<Page> pages = result.getPages();
        int requests = 0;
        for (Page page : pages) {
            requests += getNumNonFaviconRequests(page);
        }
        assert(requests > 1);
    }

    public static HttpRequest getFirstRequest(TestResult result) {
        ArrayList<Page> pages = result.getPages();
        assert(pages.size() >= 1);

        return getFirstRequest(pages.get(0));
    }

    public static HttpRequest getFirstRequest(Page page) {
        ArrayList<HttpRequest> requests = page.getRequests();
        assert(requests.size() >= 1);
        for (HttpRequest httpRequest : requests) {
            if (!isFavIcon(httpRequest)) {
                return httpRequest;
            }
        }
        fail("http request found but it was the fav icon, which is ignored");
        return null;
    }

    private static int getNumNonFaviconRequests(Page page) {
        int numRequests = 0;
        for (HttpRequest httpRequest : page.getRequests()) {
            if (!isFavIcon(httpRequest)) {
                numRequests++;
            }
        }
        return numRequests;
    }

    private static HttpRequest getNonFaviconRequest(Page page, int i) {
        int index = 0;
        for (HttpRequest httpRequest : page.getRequests()) {
            if (!isFavIcon(httpRequest)) {
                if (index == i) {
                    return httpRequest;
                }
                index++;
            }
        }
        return null;
    }

    public static HttpRequest getRequest(int i, TestResult result) {
        ArrayList<Page> pages = result.getPages();
        assert(pages.size() >= 1);

        int j = 0;
        while (i >= getNumNonFaviconRequests(pages.get(j))) {
            i -= getNumNonFaviconRequests(pages.get(j));
            j++;
            assert(pages.size() > j);
        }

        return getNonFaviconRequest(pages.get(j), i);
    }

    public static void assertRequestState(HttpRequest.State state, HttpRequest request) {
        assertEquals(state, request.getState());
    }

    public void assertError(String errorMessage, TestResult result) {
        assertNotNull("Expected error, but no error", result.getError());
        assertEquals(errorMessage, result.getError().message);
    }

    public void assertError(String errorMessage, ErrorType errorType, TestResult result) {
        assertNotNull("Expected error, but no error", result.getError());
        assertEquals(errorMessage, result.getError().message);
        assertEquals(errorType, result.getError().type);
    }

    public void assertErrorStartsWith(String errorMessage, ErrorType errorType, TestResult result) {
        assertNotNull("Expected error, but no error", result.getError());
        assertEquals(errorType, result.getError().type);
        String actualError = result.getError().message;
        assertTrue(String.format("Expected error message to start with %s, but was %s", errorMessage, actualError),
            actualError != null && actualError.startsWith(errorMessage));
    }

    public void assertNoError(TestResult result) {
        assertNull("Error set", result.getError());
    }

    public void assertUrlPath(String path, HttpRequest request) {
        assertUrlPath(path, request.getUrl());
    }

    public void assertUrlPath(String path, String url) {
        assertEquals(path, Http.parseUrl(url).path);
    }

    /**
     * Check the first non favicon request (favicon request can really be issued at any time
     * Linux and OSX differ in favicon request timing pretty consistently!)
     */
    public void assertFirstUrlPath(String expectedUrl, Page page) {
        ArrayList<HttpRequest> requests = page.getRequests();
        int i = 0;
        if (isFavIcon(requests.get(i))) {
            i++;
        }
        assertUrlPath(expectedUrl, requests.get(i));
    }

    public void assertPagePath(TestResult result, int pageIndex, String path) {
        Page page = result.getPage(pageIndex);
        assertPagePath(page, path);
    }

    public void assertPagePath(Page page, String path) {
        assertEquals(path, Http.parseUrl(page.getUrl()).path);
    }

    public void assert200OK(HttpRequest request) {
        assertEquals(HttpRequest.State.Complete, request.getState());
        assertEquals("OK", request.getStatusText());
        assertEquals(200, request.getStatusCode());
    }

    static public long getDefaultScriptTimeout() {
        return settings.getLong("test.timeout", 20000);
    }
}
