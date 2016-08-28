package com.loadtestgo.script.tester.tests;

import com.loadtestgo.script.api.TestResult;
import com.loadtestgo.script.tester.framework.JavaScriptTest;
import org.junit.Test;

public class AddHeaders extends JavaScriptTest {
    @Test
    public void defaultHeaders() {
        String script = String.format(
                "b = pizza.open(\"%s\");\n" +
                "b.verifyText(\'deflate\');\n",
                getTestUrl("headers/all"));

        TestResult result = runScript(script);

        assertOnePage(result);
        assertOneRequest(result);
        assertNoError(result);
    }

    @Test
    public void setUserAgent() {
        String script = String.format(
                "b = pizza.open();\n" +
                "b.setHeader('User-Agent', 'blah');\n" +
                "b.open(\"%s\");\n" +
                "b.verifyText(\'User-Agent: blah\');\n",
                getTestUrl("headers/all"));

        TestResult result = runScript(script);

        assertOnePage(result);
        assertOneRequest(result);
        assertNoError(result);
    }

    @Test
    public void setCustomHeader() {
        String script = String.format(
                "b = pizza.open();\n" +
                "b.setHeader('custom', 'blah');\n" +
                "b.open(\"%s\");\n" +
                "b.verifyText(\'custom: blah\');\n",
                getTestUrl("headers/all"));

        TestResult result = runScript(script);

        assertOnePage(result);
        assertOneRequest(result);
        assertNoError(result);
    }

    @Test
    public void removeCustomHeader() {
        String script = String.format(
                "b = pizza.open();\n" +
                "b.setHeader('custom', 'blah');\n" +
                "b.removeHeader('custom');\n" +
                "b.open(\"%s\");\n" +
                "b.verifyNotText(\'custom: blah\');\n",
                getTestUrl("headers/all"));

        TestResult result = runScript(script);

        assertOnePage(result);
        assertOneRequest(result);
        assertNoError(result);
    }

    @Test
    public void removeHeader() {
        String script = String.format(
                "b = pizza.open();\n" +
                    "b.removeHeader('User-Agent');\n" +
                "b.removeHeader('ass');\n" +
                "b.removeHeader('Connection');\n" +
                "b.open(\"%s\");\n" +
                "b.waitForHttpRequests(2000);\n" +
                "b.verifyNotText(\'custom: blah\');\n",
                getTestUrl("headers/all"));

        TestResult result = runScript(script);

        assertOnePage(result);
        assertOneRequest(result);
        assertNoError(result);
    }

    /**
     * See if we can override the host header.
     *
     * Be warned though, SSL connections may be weird with this,
     * as the SSL host name supplied when making the SSL connection wont
     * match.
     */
    @Test
    public void setHostHeader() {
        String script = String.format(
                "b = pizza.open();\n" +
                "b.setHeader('Host', 'www.google.com');\n" +
                "b.open(\"%s\");\n" +
                "b.verifyText(\'Host: www.google.com\');\n",
                getTestUrl("headers/all"));

        TestResult result = runScript(script);

        assertOnePage(result);
        assertOneRequest(result);
        assertNoError(result);
    }
}
