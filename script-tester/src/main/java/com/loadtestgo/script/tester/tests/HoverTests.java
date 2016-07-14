package com.loadtestgo.script.tester.tests;

import com.loadtestgo.script.api.ErrorType;
import com.loadtestgo.script.api.HttpRequest;
import com.loadtestgo.script.api.Page;
import com.loadtestgo.script.api.TestResult;
import com.loadtestgo.script.tester.framework.JavaScriptTest;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Tests for the hover command
 */
public class HoverTests extends JavaScriptTest {
    @Test
    public void clickDropDown() {
        String script = String.format(
            "b = pizza.open(\"%s\");\n" +
            "b.hover('#menu1');\n" +
            "b.waitForVisible('#menu1drop');\n" +
            "b.click('a:contains(Item1)');\n" +
            "b.waitPageLoad();",
            getTestUrl("files/navMenu.html"));

        TestResult result = runScript(script);

        assertNoError(result);

        assertEquals(2, result.getPages().size());

        Page page1 = result.getPages().get(0);
        assertOneRequest(page1);
        assertUrlPath("/files/navMenu.html", page1.getRequests().get(0));
        assertUrlPath("/files/navMenu.html", page1.getUrl());

        Page page2 = result.getPages().get(1);
        assertOneRequest(page2);
        HttpRequest request2 = page2.getRequests().get(0);
        assertUrlPath("/files/basic.html", request2);
        assertUrlPath("/files/basic.html", page2.getUrl());
    }

    @Test
    public void hoverNotExists() {
        String script = String.format(
            "b = pizza.open(\"%s\");\n" +
            "b.hover('#menu4');\n",
            getTestUrl("files/navMenu.html"));

        TestResult result = runScript(script);

        assertError("Unable to find element for selector '#menu4'!", ErrorType.Script, result);
        assertEquals(1, result.getPages().size());
    }

    @Test
    public void hoverHidden() {
        String script = String.format(
            "b = pizza.open(\"%s\");\n" +
            "b.hover('#hidden');\n",
            getTestUrl("files/navMenu.html"));

        TestResult result = runScript(script);

        assertError("Element found but not visible", ErrorType.Script, result);
        assertEquals(1, result.getPages().size());
    }
}
