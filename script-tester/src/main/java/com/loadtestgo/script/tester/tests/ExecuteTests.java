package com.loadtestgo.script.tester.tests;

import com.loadtestgo.script.api.ErrorType;
import com.loadtestgo.script.api.TestResult;
import com.loadtestgo.script.tester.framework.JavaScriptTest;
import org.junit.Test;

/**
 * Tests for the execute command.
 */
public class ExecuteTests extends JavaScriptTest {
    @Test
    public void execute() {
        String script = String.format(
            "b = pizza.open(\"%s\");\n" +
            "url = b.execute('document.URL');\n" +
            "assert.ok(url.indexOf('files/basic.html') > 0);" +
            "text = b.execute('document.getElementsByTagName(\"body\")[0].innerText');\n" +
            "assert.equal(text, 'This is the HTML body.');",
            getTestUrl("files/basic.html"));

        TestResult result = runScript(script);

        assertOnePage(result);
        assertNoError(result);
    }

    @Test
    public void exception() {
        String script = String.format(
            "b = pizza.open(\"%s\");\n" +
            "b.execute('throw new Error(\"error!\")');\n",
            getTestUrl("files/basic.html"));

        TestResult result = runScript(script);

        assertOnePage(result);
        assertErrorStartsWith("Error: error!", ErrorType.Script, result);
    }

    @Test
    public void syntax() {
        String script = String.format(
            "b = pizza.open(\"%s\");\n" +
            "b.execute('\"');\n",
            getTestUrl("files/basic.html"));

        TestResult result = runScript(script);

        assertOnePage(result);
        assertError("SyntaxError: Invalid or unexpected token", ErrorType.Script, result);
    }
}
