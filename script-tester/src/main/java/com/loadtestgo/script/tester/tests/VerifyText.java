package com.loadtestgo.script.tester.tests;

import com.loadtestgo.script.api.ErrorType;
import com.loadtestgo.script.api.TestResult;
import com.loadtestgo.script.tester.framework.JavaScriptTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class VerifyText extends JavaScriptTest {
    @Test
    public void verifyText() {
        String script = String.format(
            "b = pizza.open(\"%s\");\n" +
            "b.verifyText(\"This is the HTML body\");",
            getTestUrl("files/basic.html"));

        TestResult result = runScript(script);

        assertOnePage(result);
        assertNoError(result);
    }

    @Test
    public void verifyTextError() {
        String script = String.format(
            "b = pizza.open(\"%s\");\n" +
            "b.verifyText(\"This text doesn't exists in the body\");",
            getTestUrl("files/basic.html"));

        TestResult result = runScript(script);

        assertOnePage(result);
        assertError("Unable to find text 'This text doesn't exists in the body'",
                ErrorType.Script, result);
    }

    @Test
    public void verifyTextRegex() {
        String script = String.format(
            "b = pizza.open(\"%s\");\n" +
            "b.verifyText(/html body/i);",
            getTestUrl("files/basic.html"));

        TestResult result = runScript(script);

        assertOnePage(result);
        assertNoError(result);
    }

    @Test
    public void verifyTextErrorRegex() {
        String script = String.format(
            "b = pizza.open(\"%s\");\n" +
            "b.verifyText(/html body/);",
            getTestUrl("files/basic.html"));

        TestResult result = runScript(script);

        assertOnePage(result);
        assertError("Unable to find text matching '/html body/'",
                ErrorType.Script, result);
    }

    @Test
    public void verifyNotText() {
        String script = String.format(
            "b = pizza.open(\"%s\");\n" +
            "b.verifyNotText(\"This text is not on the page\");",
            getTestUrl("files/basic.html"));

        TestResult result = runScript(script);

        assertOnePage(result);
        assertNoError(result);
    }

    @Test
    public void verifyNotTextError() {
        String script = String.format(
            "b = pizza.open(\"%s\");\n" +
            "b.verifyNotText(\"This is the HTML body\");",
            getTestUrl("files/basic.html"));

        TestResult result = runScript(script);

        assertOnePage(result);
        assertError("Found text 'This is the HTML body'",
                ErrorType.Script, result);
    }

    @Test
    public void verifyNotTextRegex() {
        String script = String.format(
            "b = pizza.open(\"%s\");\n" +
            "b.verifyNotText(/html body/);",
            getTestUrl("files/basic.html"));

        TestResult result = runScript(script);

        assertOnePage(result);
        assertNoError(result);
    }

    @Test
    public void verifyNotTextErrorRegex() {
        String script = String.format(
            "b = pizza.open(\"%s\");\n" +
            "b.verifyNotText(/html body/i);",
            getTestUrl("files/basic.html"));

        TestResult result = runScript(script);

        assertOnePage(result);
        assertError("Found text matching '/html body/i'",
                ErrorType.Script, result);
    }
}
