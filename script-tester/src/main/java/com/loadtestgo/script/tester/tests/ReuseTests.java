package com.loadtestgo.script.tester.tests;

import com.loadtestgo.script.api.ErrorType;
import com.loadtestgo.script.api.TestResult;
import com.loadtestgo.script.engine.EngineContext;
import com.loadtestgo.script.engine.TestContext;
import com.loadtestgo.script.engine.UserContext;
import com.loadtestgo.script.tester.framework.JavaScriptTest;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

// Test reusing the browser between tests
public class ReuseTests extends JavaScriptTest {
    @Test
    public void waitElementTimeout() {
        UserContext userContext = newUserContext();
        try {
            String waitScript = String.format(
                "b = pizza.open(\"%s\");\n" +
                "b.waitVisible('#notAnElement');\n",
                getTestUrl("files/basic.html"));

            TestContext testContext = newTestContext(userContext);
            TestResult result = runScript(testContext, waitScript, 2000);

            assertEquals(1, result.getPages().size());
            assertError("Script interrupted", ErrorType.Timeout, result);
            assertTrue("Runtime greater than 2000ms", result.getRunTime() > 2000);

            String isBrowserOkScript = String.format(
                "b = pizza.open(\"%s\");\n" +
                "b.waitText('This is the HTML body');\n",
                getTestUrl("files/basic.html"));

            testContext = newTestContext(userContext);
            result = runScript(testContext, isBrowserOkScript, 8000);

            assertEquals(1, result.getPages().size());
            assertNoError(result);
        } finally {
            userContext.cleanup();
            userContext.getEngineContext().cleanup();
        }
    }

    @Test
    public void tabbedWaitElementTimeout() {
        UserContext userContext = newUserContext();
        try {
            String script = String.format(
                "b = pizza.open(\"%s\");\n" +
                "b.click('#newTabButton');\n" +
                "b.selectTab({index:1});\n" +
                "b.waitVisible('#notAnElement');\n",
                getTestUrl("files/openNew.html"));

            TestContext testContext = newTestContext(userContext);
            TestResult result = runScript(testContext, script, 3000);

            assertError("Script interrupted", ErrorType.Timeout, result);
            assertTrue("Runtime greater than 3000ms", result.getRunTime() > 3000);

            String isBrowserOkScript = String.format(
                "b = pizza.open(\"%s\");\n" +
                "b.click('#newTabButton');\n" +
                "b.selectTab({index:1});\n" +
                "b.waitVisible('#notAnElement');\n",
                getTestUrl("files/openNew.html"));

            testContext = newTestContext(userContext);
            result = runScript(testContext, isBrowserOkScript, 3000);

            assertError("Script interrupted", ErrorType.Timeout, result);
            assertTrue("Runtime greater than 3000ms", result.getRunTime() > 3000);
        } finally {
            userContext.cleanup();
            userContext.getEngineContext().cleanup();
        }
    }

    @Test
    public void dialogOpenReuse() {
        UserContext userContext = newUserContext();
        try {
            // Can we handle opening a page that calls an alert dialog on document.onload?
            String script = String.format(
                "var b = pizza.open();\n" +
                "b.open(\"%s\");",
                getTestUrl("files/dialog.html"));

            TestContext testContext = newTestContext(userContext);
            TestResult result = runScript(testContext, script, 3000);

            assertError("JavaScript alert \"it's a dialog!\" interrupted navigation. " +
                "See Browser.dismissDialogs() to find out how to ignore this error.", result);

            String isBrowserOkScript = String.format(
                "b = pizza.open(\"%s\");\n",
                getTestUrl("files/basic.html"));

            testContext = newTestContext(userContext);
            result = runScript(testContext, isBrowserOkScript, 3000);

            assertNoError(result);
        } finally {
            userContext.cleanup();
            userContext.getEngineContext().cleanup();
        }
    }

    @Test
    public void dialogLater() {
        UserContext userContext = newUserContext();
        try {
            // Can we handle opening a page that calls an alert dialog on document.onload?
            String script = String.format(
                "var b = pizza.open(\"%s\");\n" +
                "b.click('#alert100ms');\n" +
                "pizza.sleep(90);\n",
                getTestUrl("files/dialogLater.html"));

            TestContext testContext = newTestContext(userContext);
            TestResult result = runScript(testContext, script, 3000);

            assertNoError(result);

            String isBrowserOkScript = String.format(
                "b = pizza.open(\"%s\");\n",
                getTestUrl("files/basic.html"));

            testContext = newTestContext(userContext);
            result = runScript(testContext, isBrowserOkScript, 3000);

            assertNoError(result);
        } finally {
            userContext.cleanup();
            userContext.getEngineContext().cleanup();
        }
    }

    @Test
    public void dialogBeforeUnload() {
        UserContext userContext = newUserContext();
        try {
            // The final beforeunload
            String script = String.format(
                "var b = pizza.open(\"%s\");\n",
                getTestUrl("files/dialogUnload.html"));

            TestContext testContext = newTestContext(userContext);
            TestResult result = runScript(testContext, script, 3000);

            assertNoError(result);

            String isBrowserOkScript = String.format(
                "b = pizza.open(\"%s\");\n",
                getTestUrl("files/basic.html"));

            testContext = newTestContext(userContext);
            result = runScript(testContext, isBrowserOkScript, 3000);

            assertNoError(result);
        } finally {
            userContext.cleanup();
            userContext.getEngineContext().cleanup();
        }
    }

    @Test
    public void dialogBeforeUnloadWithAlert() {
        UserContext userContext = newUserContext();
        try {
            // Cause the beforeunload dialog to be displayed,
            // and make sure we properly dismiss it.
            String script = String.format(
                "var b = pizza.open(\"%s\");\n" +
                "b.click('a:contains(Leave)');\n",
                getTestUrl("files/dialogUnload.html")
            );

            TestContext testContext = newTestContext(userContext);
            TestResult result = runScript(testContext, script, 3000);

            assertError("Script interrupted", ErrorType.Timeout, result);

            String isBrowserOkScript = String.format(
                "b = pizza.open(\"%s\");\n",
                getTestUrl("files/basic.html"));

            testContext = newTestContext(userContext);
            result = runScript(testContext, isBrowserOkScript, 3000);

            assertNoError(result);
        } finally {
            userContext.cleanup();
            userContext.getEngineContext().cleanup();
        }
    }

    @Test
    public void verifyText() {
        UserContext userContext = newUserContext();
        try {
            String script = String.format(
                "b = pizza.open(\"%s\");\n" +
                "b.verifyText(\"This text doesn't exists in the body\");",
                getTestUrl("files/basic.html"));

            TestContext testContext = newTestContext(userContext);
            TestResult result = runScript(testContext, script, 3000);

            assertOnePage(result);
            assertError("Unable to find text 'This text doesn't exists in the body'",
                ErrorType.Script, result);

            String clickScript = String.format(
                "b = pizza.open(\"%s\");\n" +
                    "b.hover('#menu1');\n" +
                    "b.waitVisible('#menu1drop');\n" +
                    "b.click('a:contains(Item1)');\n" +
                    "b.waitPageLoad();",
                getTestUrl("files/navMenu.html"));

            testContext = newTestContext(userContext);
            result = runScript(testContext, clickScript, 3000);

            assertNoError(result);
        } finally {
            userContext.cleanup();
            userContext.getEngineContext().cleanup();
        }
    }

    @Test
    public void selectSubFrame() {
        UserContext userContext = newUserContext();
        try {
            // Select a sub frame, issue a command to load the automation framework, and then run another test that
            // requires the automation framework but for a different page
            String script = String.format(
                "b = pizza.open(\"%s\");\n" +
                "r = b.selectFrame(\"iframe:nth(1)\");\n" +
                "b.hover(\"body\");\n" +
                "assert.equal(r.type, 'iframe');\n",
                getTestUrl("files/frames/nested.html"));

            TestContext testContext = newTestContext(userContext);
            TestResult result = runScript(testContext, script);

            assertOnePage(result);
            assertNoError(result);

            String clickScript = String.format(
                "b = pizza.open(\"%s\");\n" +
                "b.hover('#menu1');\n" +
                "b.waitVisible('#menu1drop');\n" +
                "b.click('a:contains(Item1)');\n" +
                "b.waitPageLoad();",
                getTestUrl("files/navMenu.html"));

            testContext = newTestContext(userContext);
            result = runScript(testContext, clickScript, 3000);

            assertNoError(result);
        } finally {
            userContext.cleanup();
            userContext.getEngineContext().cleanup();
        }
    }

    private UserContext newUserContext() {
        UserContext userContext = new UserContext(new EngineContext());
        userContext.setKeepBrowserOpen(true);
        userContext.setReuseSession(false);
        return userContext;
    }

    private TestContext newTestContext(UserContext userContext) {
        TestContext testContext = new TestContext(userContext);
        setupTestContext(testContext);
        return testContext;
    }
}
