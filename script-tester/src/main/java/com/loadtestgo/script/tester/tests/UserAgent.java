package com.loadtestgo.script.tester.tests;

import com.loadtestgo.script.api.TestResult;
import com.loadtestgo.script.tester.framework.JavaScriptTest;
import org.junit.Test;

public class UserAgent extends JavaScriptTest {
    @Test
    public void defaultUserAgent() {
        String script = String.format(
            "b = pizza.open(\"%s\");\n" +
            "b.verifyText(\'Chrome\');\n",
            getTestUrl("headers/userAgent"));

        TestResult result = runScript(script);

        assertOnePage(result);
        assertOneRequest(result);
        assertNoError(result);
    }

    @Test
    public void setUserAgent() {
        String script = String.format(
            "b = pizza.open();\n" +
            "b.setUserAgent('blah blah');\n" +
            "b.open(\"%s\");\n" +
            "b.verifyText('blah blah');\n" +
            "assert.eq(b.execute('navigator.userAgent'), 'blah blah');\n",
            getTestUrl("headers/userAgent"));

        TestResult result = runScript(script);

        assertOnePage(result);
        assertOneRequest(result);
        assertNoError(result);
    }

    @Test
    public void unsetUserAgent() {
        String script = String.format(
            "b = pizza.open();\n" +
            "b.setUserAgent('blah blah');\n" +
            "b.setUserAgent('');\n" +
            "b.open(\"%s\");\n" +
            "b.verifyText(\'Chrome\');\n" +
            "assert.ok(b.execute('navigator.userAgent').contains('Chrome'));\n",
            getTestUrl("headers/userAgent"));

        TestResult result = runScript(script);

        assertOnePage(result);
        assertOneRequest(result);
        assertNoError(result);
    }
}
