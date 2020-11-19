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
