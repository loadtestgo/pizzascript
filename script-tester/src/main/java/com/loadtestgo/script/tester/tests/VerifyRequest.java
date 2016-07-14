package com.loadtestgo.script.tester.tests;

import com.loadtestgo.script.api.TestResult;
import com.loadtestgo.script.tester.framework.JavaScriptTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class VerifyRequest extends JavaScriptTest {
    @Test
    public void verifyRequest() {
        String url = getTestUrl("files/basic.html");
        String script = String.format(
            "b = pizza.open(\"%s\");\n" +
            "b.verifyRequest(\"%s\");\n",
            url, url);

        TestResult result = runScript(script);

        assertOnePage(result);
        assertNoError(result);
    }

    @Test
    public void verifyRequest404() {
        String url = getTestUrl("status/code404");
        String script = String.format(
            "var b = pizza.open();\n" +
            "b.ignoreHttpErrors(true);\n" +
            "b.open(\"%s\");\n" +
            "b.verifyRequest(\"%s\");\n",
            url, url);

        TestResult result = runScript(script);

        String error = String.format("%s: HTTP status '404 Not Found'", url);
        assertOnePage(result);
        assertError(error, result);
    }

    @Test
    public void verifyRequestRegex() {
        String url = getTestUrl("files/basic.html");
        String script = String.format(
            "b = pizza.open(\"%s\");\n" +
            "b.verifyRequest(/files\\/basic\\.html$/);\n",
            url, url);

        TestResult result = runScript(script);

        assertOnePage(result);
        assertNoError(result);
    }
}
