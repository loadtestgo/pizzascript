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
            "b.waitPageLoad();",
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
            "b.waitPageLoad();",
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

        assertError("Element '#link2' found but not visible", ErrorType.Script, result);
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

    @Test
    // One frequent test pattern is to wait on a button / or other clickable object to be visible
    // and then click on it.  However when a website uses a framework or complicated code for
    // rendering the button HTML / updating styles, the button can be hidden / shown multiple
    // times.  If we wait for the first time the button is shown and then immediately click the
    // button can be hidden again, and the click will fail.  To get around this issue, click()
    // has a retry mechanism built-in, where it will wait and retry the click() several times.
    // This test verifies that the retry is working.
    public void clickButtonRepeatedlyShownAndHidden() {
        String script = String.format(
            "b = pizza.open(\"%s\");\n" +
            "b.click('#showDiv3');\n" +
            "b.waitVisible('#div3');\n" +
            "b.click('#hideThenShowDiv3');\n" +
            "b.click('#div3');\n",
            getTestUrl("files/clipFlip.html"));

        TestResult result = runScript(script);

        assertNoError(result);
        assertEquals(1, result.getPages().size());
    }

    @Test
    public void clickCheckboxesInSubFrames() {
        String script = String.format(
            "b = pizza.open(\"%s\");\n" +
            "b.listFrames();\n" +
            "b.selectFrame('iframe[name=name_2]');\n" +
            "assert.equal(b.checked('#checkbox1'), false);\n" +
            "b.click('#checkbox1');\n" +
            "assert.equal(b.checked('#checkbox1'), true);\n" +
            "b.click('#checkbox1');\n" +
            "assert.equal(b.checked('#checkbox1'), false);\n" +
            "b.selectTopFrame();\n" +
            "b.selectFrame('iframe[name=name_1]');\n" +
            "assert.equal(b.checked('#checkbox1'), false);\n" +
            "b.click('#checkbox1');\n" +
            "assert.equal(b.checked('#checkbox1'), true);\n" +
            "b.click('#checkbox1');\n" +
            "assert.equal(b.checked('#checkbox1'), false);\n",
            getTestUrl("files/frames/forms.html"));

        TestResult result = runScript(script);

        assertOnePage(result);
        assertNoError(result);
    }
}
