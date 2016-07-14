package com.loadtestgo.script.tester.tests;

import com.loadtestgo.script.api.ErrorType;
import com.loadtestgo.script.api.TestResult;
import com.loadtestgo.script.tester.framework.JavaScriptTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class VerifyTitle extends JavaScriptTest {
    @Test
    public void verifyTitle() {
        String script = String.format(
            "b = pizza.open(\"%s\");\n" +
            "b.verifyTitle(\"Test Page\");",
            getTestUrl("files/basic.html"));

        TestResult result = runScript(script);

        assertOnePage(result);
        assertNoError(result);
    }

    @Test
    public void getTitle() {
        String script = String.format(
            "b = pizza.open(\"%s\");\n" +
            "assert.eq(\"Test Page\", b.getTitle());",
            getTestUrl("files/basic.html"));

        TestResult result = runScript(script);

        assertOnePage(result);
        assertNoError(result);
    }

    @Test
    public void verifyTextError() {
        String script = String.format(
            "b = pizza.open(\"%s\");\n" +
            "b.verifyTitle(\"This is not the title\");",
            getTestUrl("files/basic.html"));

        TestResult result = runScript(script);

        assertOnePage(result);
        assertError("Page title did not match 'This is not the title'", ErrorType.Script, result);
    }

    @Test
    public void verifyTitleRegex() {
        String script = String.format(
            "b = pizza.open(\"%s\");\n" +
            "b.verifyTitle(/test page/i);",
            getTestUrl("files/basic.html"));

        TestResult result = runScript(script);

        assertOnePage(result);
        assertNoError(result);
    }

    @Test
    public void verifyTextRegexError() {
        String script = String.format(
            "b = pizza.open(\"%s\");\n" +
            "b.verifyTitle(/test page/);",
            getTestUrl("files/basic.html"));

        TestResult result = runScript(script);

        assertOnePage(result);
        assertError("Page title did not match /test page/", ErrorType.Script, result);
    }
}
