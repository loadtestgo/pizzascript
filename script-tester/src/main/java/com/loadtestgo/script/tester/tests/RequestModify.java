package com.loadtestgo.script.tester.tests;

import com.loadtestgo.script.api.ErrorType;
import com.loadtestgo.script.api.HttpRequest;
import com.loadtestgo.script.api.TestResult;
import com.loadtestgo.script.tester.framework.JavaScriptTest;
import org.junit.Test;

import static org.junit.Assert.*;

public class RequestModify extends JavaScriptTest {
    @Test
    public void blockUrlSpecific() {
        String mario = getTestUrl("files/mario.jpg");

        String script = String.format(
            "b = pizza.open();\n" +
            "b.blockUrl('%s');\n" +
            "b.open('%s');\n",
            mario,
            getTestUrl("files/links.html"));

        TestResult result = runScript(script);

        assertOnePage(result);
        assertNoError(result);

        boolean foundMario = false;
        for (HttpRequest request : result.getLastPage().getRequests()) {
            if (request.getUrl().equals(mario)) {
                assertNotNull(request.getError());
                assertEquals("Blocked", request.getError());
                foundMario = true;
            }
        }

        assertTrue("Unable to find mario.jpg", foundMario);
    }

    @Test
    public void blockUrlMario() {
        String script = String.format(
            "b = pizza.open();\n" +
            "b.blockUrl('*://*/*mario\\.jpg');\n" +
            "b.open('%s');\n",
            getTestUrl("files/links.html"));

        TestResult result = runScript(script);

        assertOnePage(result);
        assertNoError(result);

        boolean foundMario = false;
        for (HttpRequest request : result.getLastPage().getRequests()) {
            if (request.getUrl().endsWith("mario.jpg")) {
                assertNotNull(request.getError());
                assertEquals("Blocked", request.getError());
                foundMario = true;
            }
        }

        assertTrue("Unable to find mario.jpg", foundMario);
    }

    @Test
    public void blockAll() {
        String script = String.format(
            "b = pizza.open();\n" +
            "b.blockUrl('*://*/*');\n" +
            "b.open('%s');\n",
            getTestUrl("files/links.html"));

        TestResult result = runScript(script);

        assertOnePage(result);
        assertError(
            String.format("Error navigating to '%s': net::ERR_BLOCKED_BY_CLIENT", getTestUrl("files/links.html")),
            ErrorType.Navigation, result);
        assertOneRequest(result);

        HttpRequest request = result.getLastPage().getRequests().get(0);
        assertNotNull(request.getError());
        assertEquals("Blocked", request.getError());
    }

    @Test
    public void blockString() {
        // Make sure we don't treat strings as regexps
        String script = String.format(
            "b = pizza.open();\n" +
            "b.blockUrl('links...');\n" + // this will match links.html if we are incorrectly treating strings
                                          // as regexps
            "b.open('%s');\n",
            getTestUrl("files/links.html"));

        TestResult result = runScript(script);

        assertOnePage(result);
        assertNoError(result);
        assertNumRequests(3, result.getLastPage());

        HttpRequest request = result.getLastPage().getRequests().get(0);
        assertNull(request.getError());
    }

    @Test
    public void rewriteUrl() {
        String script = String.format(
            "b = pizza.open();\n" +
            "b.rewriteUrl('*://*/*/mario.jpg', /(.*)\\/mario\\.jpg/, '$1/wario.jpg');\n" +
            "b.open('%s');\n",
            getTestUrl("files/links.html"));

        TestResult result = runScript(script);

        assertOnePage(result);
        assertNoError(result);

        boolean foundWario = false;
        for (HttpRequest request : result.getLastPage().getRequests()) {
            if (request.getUrl().endsWith("wario.jpg")) {
                assertNull(request.getError());
                foundWario = true;
            }
        }

        assertTrue("Unable to find wario.jpg", foundWario);
    }

    @Test
    public void clearRules() {
        String script = String.format(
            "b = pizza.open();\n" +
            "b.blockUrl('.*');\n" +
            "b.rewriteUrl('*://*/*/mario.jpg', /(.*)\\/mario\\.jpg/, '$1/wario.jpg');\n" +
            "b.clearRules();\n" +
            "b.open('%s');\n",
            getTestUrl("files/links.html"));

        TestResult result = runScript(script);

        assertNoError(result);
        assertOnePage(result);

        boolean foundMario = false;
        for (HttpRequest request : result.getLastPage().getRequests()) {
            if (request.getUrl().endsWith("mario.jpg")) {
                assertNull(request.getError());
                foundMario = true;
            }
        }

        assertTrue("Unable to find mario.jpg", foundMario);
    }
}

