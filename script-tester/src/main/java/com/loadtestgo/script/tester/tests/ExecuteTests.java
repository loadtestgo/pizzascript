package com.loadtestgo.script.tester.tests;

import com.loadtestgo.script.api.ErrorType;
import com.loadtestgo.script.api.TestResult;
import com.loadtestgo.script.tester.framework.JavaScriptTest;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

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
    public void globals() {
        String script = String.format(
            "b = pizza.open(\"%s\");\n" +
                "assert.eq(b.execute('myGlobalNumber'), 42);\n" +
                "assert.eq(b.execute('myGlobalNumber = 43'), 43);\n" +
                "assert.eq(b.execute('myGlobalNumber'), 43);\n" +
                "assert.eq(b.execute('myGlobalString'), 'Pizza');\n" +
                "assert.eq(b.execute('myGlobalString = \"Script\"'), 'Script');\n" +
                "assert.eq(b.execute('myGlobalString'), 'Script');\n" +
                "assert.eq(b.execute('myGlobalFunction(1)'), '1');\n" +
                "assert.eq(b.execute('myGlobalFunction(2)'), '2');\n",
            getTestUrl("files/globals.html"));

        TestResult result = runScript(script);
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

        //
        assertNotNull("Expected error, but no error", result.getError());
        assertEquals(ErrorType.Script, result.getError().type);
        String errorMessage = result.getError().message;
        assertTrue(errorMessage.equals("SyntaxError: Unexpected token ILLEGAL") ||
            errorMessage.equals("SyntaxError: Invalid or unexpected token"));
    }
}
