package com.loadtestgo.script.tester.tests;

import com.loadtestgo.script.api.ErrorType;
import com.loadtestgo.script.api.Page;
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
    public void waitPageLoad() {
        String script = String.format(
            "var b = pizza.open();\n" +
            "b.openAsync('%s');\n" +
            "b.waitPageLoad();",
            getTestUrl("files/basic.html"));

        TestResult result = runScript(script, 2000);

        assertNoError(result);
        assertEquals(1, result.getPages().size());

        Page page = result.getPages().get(0);
        assertNumRequests(1, page);
        assertFirstUrlPath("/files/basic.html", page);
        assertUrlPath("/files/basic.html", page.getUrl());
    }

    @Test
    public void waitPageLoadTimeout() {
        String script = String.format(
            "var b = pizza.open();\n" +
            "b.openAsync('%s');\n" +
            "b.waitPageLoad(1000);",
            getTestUrl("files/longLoad.html"));

        TestResult result = runScript(script, 3000);

        assertError("Timeout after 1000ms while waiting for page to load",
            ErrorType.Navigation, result);
        assertEquals(1, result.getPages().size());
    }

    @Test
    public void waitPageLoadScriptTimeout() {
        String script = String.format(
            "var b = pizza.open();\n" +
            "b.openAsync('%s');\n" +
            "b.waitPageLoad();",
            getTestUrl("files/longLoad.html"));

        TestResult result = runScript(script, 2000);

        assertError("Script interrupted", ErrorType.Timeout, result);
        assertEquals(1, result.getPages().size());
    }

    @Test
    public void waitHttpIdleTimeout() {
        String script = String.format(
            "var b = pizza.open();\n" +
            "b.openAsync('%s');\n" +
            "b.waitHttpIdle(1000, 1000);",
            getTestUrl("files/longLoad.html"));

        TestResult result = runScript(script, 3000);

        assertError("Timeout after 1000ms while waiting for HTTP requests to complete",
            ErrorType.Timeout, result);
        assertEquals(1, result.getPages().size());
    }

    @Test
    public void waitHttpIdleDefaultTimeout() {
        String script = String.format(
            "var b = pizza.open();\n" +
            "b.setWaitTimeout(1000);\n" +
            "b.openAsync('%s');\n" +
            "b.waitHttpIdle(1000);",
            getTestUrl("files/longLoad.html"));

        TestResult result = runScript(script, 3000);

        assertError("Timeout after 1000ms while waiting for HTTP requests to complete",
            ErrorType.Timeout, result);
        assertEquals(1, result.getPages().size());
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
    public void waitVisibleTimeout() {
        String script = String.format(
            "b = pizza.open(\"%s\");\n" +
            "var v = b.query('#div6');\n" +
            "assert.eq(v.length, 1);\n" +
            "assert.eq(v[0].visible, false);\n" +
            "b.waitVisible('#div6', 500);\n",
            getTestUrl("files/findElements.html"));

        TestResult result = runScript(script, 3000);

        assertEquals(1, result.getPages().size());
        assertError("Timeout after 500ms while waiting for element to be visible '#div6'",
            ErrorType.Timeout, result);
    }

    @Test
    public void waitVisibleScriptTimeout() {
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
    public void waitVisibleTimeoutNonExistingElement() {
        String script = String.format(
            "b = pizza.open(\"%s\");\n" +
            // Non-existing element '#notAndElement'
            "b.waitVisible('#notAndElement');\n",
            getTestUrl("files/findElements.html"));

        TestResult result = runScript(script, 3000);

        assertEquals(1, result.getPages().size());
        assertError("Script interrupted", ErrorType.Timeout, result);
        assertTrue("Runtime greater than 3000ms", result.getRunTime() > 3000);
    }

    @Test
    public void waitNotVisible() {
        String script = String.format(
            "b = pizza.open(\"%s\");\n" +
            "b.click('#revealAfterWait');\n" +
            "b.waitNotVisible('#revealAfterWait');\n" +
            "assert.ok(!b.hasText('Click button to replace DOM'));\n",
            getTestUrl("files/elementLater.html"));

        TestResult result = runScript(script);

        assertNoError(result);
        assertEquals(1, result.getPages().size());
    }

    @Test
    public void waitNotVisibleTimeout() {
        String script = String.format(
            "b = pizza.open(\"%s\");\n" +
            // Visible element '#revealAfterWait'
            "b.waitNotVisible('#revealAfterWait', 500);\n",
            getTestUrl("files/elementLater.html"));

        TestResult result = runScript(script, 3000);

        assertEquals(1, result.getPages().size());
        assertError("Timeout after 500ms while waiting for element to be removed or hidden '#revealAfterWait'",
            ErrorType.Timeout, result);
    }

    @Test
    public void waitNotVisibleScriptTimeout() {
        String script = String.format(
            "b = pizza.open(\"%s\");\n" +
            // Visible element '#revealAfterWait'
            "b.waitNotVisible('#revealAfterWait');\n",
            getTestUrl("files/elementLater.html"));

        TestResult result = runScript(script, 3000);

        assertEquals(1, result.getPages().size());
        assertError("Script interrupted", ErrorType.Timeout, result);
        assertTrue("Runtime greater than 3000ms", result.getRunTime() > 3000);
    }

    @Test
    public void waitNotVisibleNonExistingElement() {
        String script = String.format(
            "b = pizza.open(\"%s\");\n" +
            // Non-existing element '#notAndElement'
            "b.waitNotVisible('#notAndElement');\n",
            getTestUrl("files/elementLater.html"));

        TestResult result = runScript(script, 3000);

        assertNoError(result);
        assertEquals(1, result.getPages().size());
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
    public void waitExistsTimeout() {
        String script = String.format(
            "b = pizza.open(\"%s\");\n" +
            "b.waitElement('#notAnElement', 500);\n",
            getTestUrl("files/basic.html"));

        TestResult result = runScript(script, 2000);

        assertEquals(1, result.getPages().size());
        assertError("Timeout after 500ms while waiting for element '#notAnElement'",
            ErrorType.Timeout, result);
    }

    @Test
    public void waitExistsScriptTimeout() {
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
    public void waitSelectTab() {
        String script = String.format(
            "b = pizza.open(\"%s\");\n" +
            "b.click('#newTabButton');\n" +
            "pizza.sleep(100);\n" +
            "b.selectTab({ index: 1 });\n" +
            "b.waitText('This is the HTML body.')",
            getTestUrl("files/openNew.html"));

        TestResult result = runScript(script);

        assertNoError(result);
        assertEquals(2, result.getPages().size());
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
            "b.waitText('Text doesn\\'t exist', 100);\n",
            getTestUrl("files/basic.html"));

        TestResult result = runScript(script, 2000);

        assertEquals(1, result.getPages().size());
        assertError("Timeout after 100ms while waiting for text matching 'Text doesn't exist'",
            ErrorType.Timeout, result);
    }

    @Test
    public void waitTextScriptTimeout() {
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
    public void waitTextScriptChangeDefaultTimeout() {
        String script = String.format(
            "b = pizza.open(\"%s\");\n" +
            "b.setWaitTimeout(100);\n" +
            "b.waitText('Text doesn\\'t exist');\n",
            getTestUrl("files/basic.html"));

        TestResult result = runScript(script, 2000);

        assertEquals(1, result.getPages().size());
        assertError("Timeout after 100ms while waiting for text matching 'Text doesn't exist'",
            ErrorType.Timeout, result);
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
