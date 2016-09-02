package com.loadtestgo.script.tester.tests;

import com.loadtestgo.script.api.ErrorType;
import com.loadtestgo.script.api.TestResult;
import com.loadtestgo.script.tester.framework.JavaScriptTest;
import org.junit.Test;

import static org.junit.Assert.*;

public class WaitForHttpRequests extends JavaScriptTest {
    @Test
    public void basicWaitNoPage() {
        String script = "b = pizza.open();\n" +
            "b.waitForHttpRequests(1000);\n";

        TestResult result = runScript(script);

        assertNoError(result);
        assertNoPages(result);

        assertTrue("Runtime greater the 1000ms", result.getRunTime() > 1000);
    }

    @Test
    public void basicWaitNoPageTimeout() {
        String script = "b = pizza.open();\n" +
            "b.waitForHttpRequests(2000);\n";

        TestResult result = runScript(script, 2000);

        assertEquals(ErrorType.Timeout, result.getError().type);

        if (!result.getError().message.equals("waitForHttpRequests() interrupted") &&
                !result.getError().message.startsWith("Wait for browser to open interrupted")) {
            fail(result.getError().message);
        }

        assertTrue("Runtime greater the 2000ms", result.getRunTime() > 2000);
    }

    @Test
    public void basicWaitOpenPage() {
        String script = String.format(
            "b = pizza.open(\"%s\");\n" +
            "b.waitForHttpRequests(1000);\n",
            getTestUrl("files/basic.html"));

        TestResult result = runScript(script);

        assertNoError(result);
        assertEquals(1, result.getPages().size());

        assertTrue("Runtime greater the 1000ms", result.getRunTime() > 1000);
    }

    @Test
    public void waitBackgroundXhr() {
        String script = String.format(
            "b = pizza.open(\"%s\");\n" +
            "b.waitForHttpRequests(1000);",
            getTestUrl("files/backgroundXhr.html"));

        TestResult result = runScript(script);

        assertNoError(result);
        assertEquals(1, result.getPages().size());
        assertMoreThanOneRequest(result);

        assertTrue("Runtime greater the 1000ms", result.getRunTime() > 1000);
    }

    @Test
    public void waitBackgroundXhrTimeout() {
        String script = String.format(
            "b = pizza.open(\"%s\");\n" +
            "b.waitForHttpRequests(1000);",
            getTestUrl("files/backgroundXhrTimeout.html"));

        TestResult result = runScript(script, 4000);

        assertEquals(1, result.getPages().size());
        assertEquals(ErrorType.Timeout, result.getError().type);

        if (!result.getError().message.equals("waitForHttpRequests() interrupted") &&
                !result.getError().message.startsWith("Wait for browser to open interrupted")) {
            fail(result.getError().message);
        }

        assertTrue("Runtime greater the 5000ms", result.getRunTime() > 4000);
    }
}
