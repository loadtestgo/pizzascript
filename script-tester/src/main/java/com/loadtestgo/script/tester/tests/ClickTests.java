package com.loadtestgo.script.tester.tests;

import com.loadtestgo.script.api.ErrorType;
import com.loadtestgo.script.api.HttpRequest;
import com.loadtestgo.script.api.Page;
import com.loadtestgo.script.api.TestResult;
import com.loadtestgo.script.tester.framework.JavaScriptTest;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Tests for the click command, you can also use jquery to call the handlers
 * directly but they don't always work because not all javascript input event
 * handlers are called.  Also jquery can't launch new tabs/windows and whatnot
 * due to the popup blocker.
 */
public class ClickTests extends JavaScriptTest {
    @Test
    public void navigateViaButton() {
        String script = String.format(
            "b = pizza.open(\"%s\");\n" +
            "b.click('#button');\n" +
            "b.waitPageLoad(10000);",
            getTestUrl("files/buttonNav.html"));

        TestResult result = runScript(script);

        assertNoError(result);

        assertEquals(2, result.getPages().size());

        Page page1 = result.getPages().get(0);
        assertOneRequest(page1);
        assertUrlPath("/files/buttonNav.html", page1.getRequests().get(0));
        assertUrlPath("/files/buttonNav.html", page1.getUrl());

        Page page2 = result.getPages().get(1);
        assertOneRequest(page2);
        HttpRequest request2 = page2.getRequests().get(0);
        assertUrlPath("/files/basic.html", request2);
        assertUrlPath("/files/basic.html", page2.getUrl());
    }

    @Test
     public void navigateViaLink() {
        String script = String.format(
            "b = pizza.open(\"%s\");\n" +
            "b.click('#link1');\n" +
            "b.waitPageLoad(10000);",
            getTestUrl("files/buttonNav.html"));

        TestResult result = runScript(script);

        assertNoError(result);

        assertEquals(2, result.getPages().size());

        Page page1 = result.getPages().get(0);
        assertOneRequest(page1);
        assertUrlPath("/files/buttonNav.html", page1.getRequests().get(0));
        assertUrlPath("/files/buttonNav.html", page1.getUrl());

        Page page2 = result.getPages().get(1);
        assertOneRequest(page2);
        HttpRequest request2 = page2.getRequests().get(0);
        assertUrlPath("/files/basic.html", request2);
        assertUrlPath("/files/basic.html", page2.getUrl());
    }

    @Test
    public void clickHidden() {
        String script = String.format(
            "b = pizza.open(\"%s\");\n" +
            "b.click('#link2');\n",
            getTestUrl("files/buttonNav.html"));

        TestResult result = runScript(script);

        assertError("Element found but not visible", ErrorType.Script, result);
        assertEquals(1, result.getPages().size());
    }

    @Test
    public void clickHidden2() {
        String script = String.format(
            "b = pizza.open(\"%s\");\n" +
            "b.click('#div6');\n",
            getTestUrl("files/findElements.html"));

        TestResult result = runScript(script);

        assertError("Element '#div6' hidden by '#div7'", ErrorType.Script, result);
        assertEquals(1, result.getPages().size());
    }
}
