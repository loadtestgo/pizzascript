package com.loadtestgo.script.tester.tests;

import com.loadtestgo.script.api.ErrorType;
import com.loadtestgo.script.api.TestResult;
import com.loadtestgo.script.tester.framework.JavaScriptTest;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class WaitTests extends JavaScriptTest {
    @Test
    public void shortSleep() {
        String script = "pizza.sleep(200);\n";

        TestResult result = runScript(script);

        assertNoError(result);
        assertNoPages(result);

        assertTrue("Runtime greater than 200ms", result.getRunTime() > 200);
    }

    @Test
    public void sleepInterrupted() {
        String script = "b = pizza.sleep(3000);";

        TestResult result = runScript(script, 2000);

        assertError("sleep() interrupted", ErrorType.Timeout, result);
        assertTrue("Runtime greater than 2000ms", result.getRunTime() >= 2000);
    }

    @Test
    public void waitVisible() {
        String script = String.format(
            "b = pizza.open(\"%s\");\n" +
            "var v = b.query('#div6');\n" +
            "assert.eq(v.length, 1);\n" +
            "assert.eq(v[0].visible, false);\n" +
            "b.click('#button2');\n" +
            "b.waitForVisible('#div6');\n" +
            "v = b.query('#div6');\n" +
            "assert.eq(v.length, 1);\n" +
            "assert.eq(v[0].visible, true);\n",
            getTestUrl("files/findElements.html"));

        TestResult result = runScript(script);

        assertNoError(result);
        assertEquals(1, result.getPages().size());
    }

    @Test
    public void waitVisibleFail() {
        String script = String.format(
            "b = pizza.open(\"%s\");\n" +
            "var v = b.query('#div6');\n" +
            "assert.eq(v.length, 1);\n" +
            "assert.eq(v[0].visible, false);\n" +
            "b.waitForVisible('#div6');\n",
            getTestUrl("files/findElements.html"));

        TestResult result = runScript(script, 3000);

        assertEquals(1, result.getPages().size());
        assertError("Script interrupted", ErrorType.Timeout, result);
        assertTrue("Runtime greater than 3000ms", result.getRunTime() > 3000);
    }

    @Test
    public void waitExists() {
        String script = String.format(
            "b = pizza.open(\"%s\");\n" +
            "var v = b.query('#divNew');\n" +
            "assert.eq(v.length, 0);\n" +
            "b.click('#reveal');\n" +
            "b.waitForElement('#divNew');\n" +
            "b.verifyExists('#divNew');\n",
            getTestUrl("files/elementLater.html"));

        TestResult result = runScript(script);

        assertNoError(result);
        assertEquals(1, result.getPages().size());
    }

    @Test
    public void waitExistsFail() {
        String script = String.format(
            "b = pizza.open(\"%s\");\n" +
            "b.waitForElement('#notAnElement');\n",
            getTestUrl("files/basic.html"));

        TestResult result = runScript(script, 2000);

        assertEquals(1, result.getPages().size());
        assertError("Script interrupted", ErrorType.Timeout, result);
        assertTrue("Runtime greater than 2000ms", result.getRunTime() > 2000);
    }

    @Test
    // Sometimes our tests won't wait for page loads, but instead wait for an element on the
    // next page to be displayed.  Check that this works.
    // The main failure case here was that the automation framework was not injected into the
    // new page.
    public void waitForElementAcrossPageLoad() {
        String script = String.format(
            "b = pizza.open(\"%s\");\n" +
            "b.click('#button');\n" +
            // will stay on current page for 1 second then load a new page
            "b.waitForElement('#div1');\n",
            getTestUrl("files/buttonNavLater.html"));

        TestResult result = runScript(script);

        assertNoError(result);
        assertEquals(2, result.getPages().size());
    }
}
