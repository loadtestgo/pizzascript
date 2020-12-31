package com.loadtestgo.script.tester.tests;

import com.loadtestgo.script.api.ErrorType;
import com.loadtestgo.script.api.Page;
import com.loadtestgo.script.api.TestResult;
import com.loadtestgo.script.tester.framework.JavaScriptTest;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class NewPage extends JavaScriptTest {
    @Test
    public void openAsync() {
        String script = String.format(
            "var b = pizza.open();\n" +
            "b.openAsync('%s');\n" +
            "b.waitPageLoad();",
            getTestUrl("files/basic.html"));

        TestResult result = runScript(script);

        assertNoError(result);
        assertEquals(1, result.getPages().size());

        Page page = result.getPages().get(0);
        assertNumRequests(1, page);
        assertFirstUrlPath("/files/basic.html", page);
        assertUrlPath("/files/basic.html", page.getUrl());
    }

    @Test
    public void openAsyncPreviousPageLoad() {
        String script = String.format(
            "b = pizza.open(\"%s\");\n" +
            // Navigate to a new page, without wait on the page load
            // event explicitly
            "b.click('#button');\n" +
            "b.waitForHttpRequests(1000);\n" +
            // Now try an async navigate
            "b.openAsync(\"%s\");\n" +
            "b.waitPageLoad();",
            getTestUrl("files/buttonNav.html"),
            getTestUrl("files/buttonNav.html"));

        TestResult result = runScript(script);

        assertNoError(result);

        assertEquals(3, result.getPages().size());

        Page page1 = result.getPages().get(0);
        assertOneRequest(page1);
        assertFirstUrlPath("/files/buttonNav.html", page1);
        assertUrlPath("/files/buttonNav.html", page1.getUrl());

        Page page2 = result.getPages().get(1);
        assertOneRequest(page2);
        assertFirstUrlPath("/files/basic.html", page2);
        assertUrlPath("/files/basic.html", page2.getUrl());

        Page page3 = result.getPages().get(2);
        assertOneRequest(page3);
        assertFirstUrlPath("/files/buttonNav.html", page3);
        assertUrlPath("/files/buttonNav.html", page3.getUrl());
    }

    @Test
    public void xhrWait() {
        // Nav to a page and make an XHR request and wait on it.
        // Since we open a new page in between, there should be two pages,
        // the second containing the XHR request
        String script = String.format(
            "b = pizza.open(\"%s\");\n" +
            "b.newPage();\n" +
            "b.jq(\"$('#button').click()\");\n" +
            "b.waitForHttpRequests(1000);",
            getTestUrl("files/buttonXhr.html"));

        TestResult result = runScript(script);

        assertNoError(result);

        assertEquals(2, result.getPages().size());

        Page page1 = result.getPages().get(0);
        assertOneRequest(page1);
        assertUrlPath("/files/buttonXhr.html", page1.getRequests().get(0));
        assertUrlPath("/files/buttonXhr.html", page1.getUrl());

        Page page2 = result.getPages().get(1);
        assertOneRequest(page2);
        assertFirstUrlPath("/post/echo", page2);

        // The URL didn't change it is still the same
        assertUrlPath("/files/buttonXhr.html", page2.getUrl());
    }

    @Test
    public void navigateViaButton() {
        String script = String.format(
            "b = pizza.open(\"%s\");\n" +
            "b.jq(\"$('#button').click()\");\n" +
            "b.waitPageLoad();",
            getTestUrl("files/buttonNav.html"));

        TestResult result = runScript(script);

        assertNoError(result);
        assertButtonNavOk(result);
    }

    private void assertButtonNavOk(TestResult result) {
        assertEquals(2, result.getPages().size());

        Page page1 = result.getPages().get(0);
        assertOneRequest(page1);
        assertFirstUrlPath("/files/buttonNav.html", page1);
        assertUrlPath("/files/buttonNav.html", page1.getUrl());

        Page page2 = result.getPages().get(1);
        assertOneRequest(page2);
        assertFirstUrlPath("/files/basic.html", page2);
        assertUrlPath("/files/basic.html", page2.getUrl());
    }

    @Test
    public void navigateViaLink() {
        String script = String.format(
            "b = pizza.open(\"%s\");\n" +
            // Call click on the native element, jquery.click() won't work
            "b.jq(\"$('#link1')[0].click()\");" +
            "b.waitPageLoad();",
            getTestUrl("files/buttonNav.html"));

        TestResult result = runScript(script);

        assertNoError(result);
        assertButtonNavOk(result);
    }

    @Test
    public void navigateNewPageWithWait() {
        String script = String.format(
            "b = pizza.open(\"%s\");\n" +
            "b.click('#button');\n" +
            // Wait for the new page to finish loading
            "b.waitForHttpRequests(1000);\n" +
            // Now check that a new page did load, waitPageLoad() should return
            // immediately as the page has been loaded.
            "b.waitPageLoad();",
            getTestUrl("files/buttonNav.html"));

        TestResult result = runScript(script);

        assertNoError(result);
        assertButtonNavOk(result);
    }

    @Test
    public void clearPageLoad() {
        String script = String.format(
            "b = pizza.open(\"%s\");\n" +
            "b.click('#button');\n" +
            // Wait for the new page to finish loading
            "b.waitForHttpRequests(1000);\n" +
            // New page has loaded, but now we want to wait on another page to load
            // so clear the previous page load.
            "b.clearPageLoad();\n" +
            // Load the new page
            "b.execute(\"window.location = \'%s\'\");\n" +
            "b.waitPageLoad();",
            getTestUrl("files/buttonNav.html"),
            getTestUrl("files/buttonNav.html"));

        TestResult result = runScript(script);

        assertNoError(result);

        assertEquals(3, result.getPages().size());

        Page page1 = result.getPages().get(0);
        assertOneRequest(page1);
        assertFirstUrlPath("/files/buttonNav.html", page1);
        assertUrlPath("/files/buttonNav.html", page1.getUrl());

        Page page2 = result.getPages().get(1);
        assertOneRequest(page2);
        assertFirstUrlPath("/files/basic.html", page2);
        assertUrlPath("/files/basic.html", page2.getUrl());

        Page page3 = result.getPages().get(2);
        assertOneRequest(page2);
        assertFirstUrlPath("/files/buttonNav.html", page3);
        assertUrlPath("/files/buttonNav.html", page3.getUrl());
    }

    @Test
    public void clearPageLoadTimeout() {
        String script = String.format(
            "b = pizza.open(\"%s\");\n" +
            "b.click('#button');\n" +
            // Wait for the new page to finish loading
            "b.waitForHttpRequests(1000);\n" +
            // Clear the page load, and then wait using waitPageLoad(), this should timeout
            "b.clearPageLoad();\n" +
            "b.waitPageLoad();",
            getTestUrl("files/buttonNav.html"));

        TestResult result = runScript(script, 5000);

        assertError("Script interrupted", ErrorType.Timeout, result);
        assertButtonNavOk(result);
    }
}
