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
            "b.waitVisible('#div6');\n" +
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
            "b.waitVisible('#div6');\n",
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
            "b.waitElement('#divNew');\n" +
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
            "b.waitElement('#notAnElement');\n",
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
    public void waitElementAcrossPageLoad() {
        String script = String.format(
            "b = pizza.open(\"%s\");\n" +
            "b.click('#button');\n" +
            // will stay on current page for 1 second then load a new page
            "b.waitElement('#div1');\n",
            getTestUrl("files/buttonNavLater.html"));

        TestResult result = runScript(script);

        assertNoError(result);
        assertEquals(2, result.getPages().size());
    }

    @Test
    public void waitText() {
        String script = String.format(
            "b = pizza.open(\"%s\");\n" +
            "b.click('#revealAfterWait');\n" +
            "b.waitText('New Text');\n" +
            "assert.ok(b.hasText('New Text'));\n",
            getTestUrl("files/elementLater.html"));

        TestResult result = runScript(script);

        assertNoError(result);
        assertEquals(1, result.getPages().size());
    }

    @Test
    public void waitNotText() {
        String script = String.format(
            "b = pizza.open(\"%s\");\n" +
            "b.click('#revealAfterWait');\n" +
            "b.waitNotText('Click button to replace DOM');\n" +
            "assert.ok(!b.hasText('Click button to replace DOM'));\n",
            getTestUrl("files/elementLater.html"));

        TestResult result = runScript(script);

        assertNoError(result);
        assertEquals(1, result.getPages().size());
    }

    @Test
    public void waitTextTimeout() {
        String script = String.format(
            "b = pizza.open(\"%s\");\n" +
            "b.waitText('Text doesn\\'t exist');\n",
            getTestUrl("files/basic.html"));

        TestResult result = runScript(script, 2000);

        assertEquals(1, result.getPages().size());
        assertError("Script interrupted", ErrorType.Timeout, result);
        assertTrue("Runtime greater than 2000ms", result.getRunTime() > 2000);
    }

    @Test
    public void waitNotTextTimeout() {
        String script = String.format(
            "b = pizza.open(\"%s\");\n" +
            "b.waitNotText('This is the HTML body');\n",
            getTestUrl("files/basic.html"));

        TestResult result = runScript(script, 2000);

        assertEquals(1, result.getPages().size());
        assertError("Script interrupted", ErrorType.Timeout, result);
        assertTrue("Runtime greater than 2000ms", result.getRunTime() > 2000);
    }

    @Test
    public void waitTextFrames() {
        String script = String.format(
            "b = pizza.open(\"%s\");\n" +
            "b.waitText('This is the HTML body.');\n" +
            "assert.ok(b.hasText('This is the HTML body.'));\n",
            getTestUrl("files/frames/nested.html"));

        TestResult result = runScript(script);

        assertNoError(result);
        assertEquals(1, result.getPages().size());
    }

    @Test
    public void waitNotTextFrames() {
        String script = String.format(
            "b = pizza.open(\"%s\");\n" +
            "b.waitNotText('Text doesn\\'t exist on page');\n" +
            "assert.ok(!b.hasText('Text doesn\\'t exist on page'));\n",
            getTestUrl("files/frames/nested.html"));

        TestResult result = runScript(script);

        assertNoError(result);
        assertEquals(1, result.getPages().size());
    }
}
