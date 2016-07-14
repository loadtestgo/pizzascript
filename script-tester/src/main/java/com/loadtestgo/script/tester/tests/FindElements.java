package com.loadtestgo.script.tester.tests;

import com.loadtestgo.script.api.ErrorType;
import com.loadtestgo.script.api.TestResult;
import com.loadtestgo.script.tester.framework.JavaScriptTest;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class FindElements extends JavaScriptTest {
    @Test
    public void findElementsCss() {
        String script = String.format(
            "b = pizza.open(\"%s\");\n" +
            "b.verifyExists('div');\n" +
            "b.verifyExists('#div1');\n" +
            "b.verifyExists('.class1');\n" +
            "b.verifyExists('.class1 p');\n" +
            "b.verifyExists('.class1>p');\n" +
            "b.verifyNotExists('#doesnotExist');\n",
            getTestUrl("files/findElements.html"));

        TestResult result = runScript(script);

        assertNoError(result);
        assertEquals(1, result.getPages().size());
    }

    @Test
    public void findElementsCssNth() {
        String script = String.format(
            "b = pizza.open(\"%s\");\n" +
            "b.verifyExists('.class1 p:nth(0)');\n" +
            "b.verifyExists('.class1 p:eq(0)');\n" +
            "b.verifyExists('.class1 p:nth(1)');\n" +
            "b.verifyExists('.class1 p:eq(1)');\n" +
            "b.verifyNotExists('.class1 p:eq(2)');\n" +
            "b.verifyNotExists('.class1 p:nth(2)');\n",
            getTestUrl("files/findElements.html"));

        TestResult result = runScript(script);

        assertNoError(result);
        assertEquals(1, result.getPages().size());
    }

    @Test
    public void findElementsCssContains() {
        String script = String.format(
            "b = pizza.open(\"%s\");\n" +
            "b.verifyExists('button:contains(Button1)');\n" +
            "b.verifyExists('div:contains(this is some text)');\n" +
            "b.verifyExists('div:icontains(this is some text)');\n" +
            "b.verifyExists('div:icontains(THIS IS SOME TEXT)');\n" +
            "b.verifyExists('div:contains(Div with id):nth(0)');\n" +
            "b.verifyExists('div:nth(0):contains(Div with id)');\n" +
            "b.verifyNotExists('div:contains(not a string that exists):nth(0)');\n" +
            "b.verifyNotExists('div:nth(0):contains(not a string that exists)');\n" +
            "b.verifyNotExists('div:contains(Div with id):nth(3)');\n" +
            "b.verifyNotExists('div:nth(3):contains(Div with id)');\n" +
            "b.verifyNotExists('button:contains(NotAString)');\n" +
            "b.verifyNotExists('button:icontains(notastring)');\n" +
            "b.verifyNotExists('button:icontains(NOTASTRING)');\n",
            getTestUrl("files/findElements.html"));

        TestResult result = runScript(script);

        assertNoError(result);
        assertEquals(1, result.getPages().size());
    }

    @Test
    public void findElementsXpath() {
        String script = String.format(
            "b = pizza.open(\"%s\");\n" +
            "b.verifyExists('xpath://div');\n" +
            "b.verifyExists('xpath://div/p');\n" +
            "b.verifyExists('xpath://div[@id=\\'div1\\']');\n" +
            "b.verifyExists('xpath://div[@class=\\'class1\\']');\n" +
            "b.verifyExists('xpath://button[text()=\\'Button1\\']');\n" +
            "b.verifyExists('xpath://p[contains(text(), \\'Div with\\')]');\n",
            getTestUrl("files/findElements.html"));

        TestResult result = runScript(script);

        assertNoError(result);
        assertEquals(1, result.getPages().size());
    }

    @Test
    public void cssNotExists() {
        String script = String.format(
            "b = pizza.open(\"%s\");\n" +
            "b.verifyExists('#notExists');\n",
            getTestUrl("files/findElements.html"));

        TestResult result = runScript(script);

        assertError("Unable to find element '#notExists'", ErrorType.Script, result);
        assertEquals(1, result.getPages().size());
    }

    @Test
    public void xpathNotExists() {
        String script = String.format(
            "b = pizza.open(\"%s\");\n" +
            "b.verifyExists('xpath://notExists');\n",
            getTestUrl("files/findElements.html"));

        TestResult result = runScript(script);

        assertError("Unable to find element 'xpath://notExists'", ErrorType.Script, result);
        assertEquals(1, result.getPages().size());
    }

    @Test
    public void bogusCss() {
        String script = String.format(
            "b = pizza.open(\"%s\");\n" +
            "b.verifyExists('\\'Div with\\')))]');\n",
            getTestUrl("files/findElements.html"));

        TestResult result = runScript(script);

        assertError("''Div with')))]' is not a valid selector.", ErrorType.Script, result);
        assertEquals(1, result.getPages().size());
    }

    @Test
    public void bogusXpath() {
        String script = String.format(
            "b = pizza.open(\"%s\");\n" +
            "b.verifyExists('xpath://p[contains(text(), \\'Div with\\')))]');\n",
            getTestUrl("files/findElements.html"));

        TestResult result = runScript(script);

        assertError("Error: The string '//p[contains(text(), 'Div with')))]' is not a valid XPath expression.",
                ErrorType.Script, result);
        assertEquals(1, result.getPages().size());
    }

    @Test
    public void xpathQuery() {
        String script = String.format(
            "b = pizza.open(\"%s\");\n" +
            "var e = b.query('xpath://div');\n" +
            "assert.ok(e.length > 0);\n",
            getTestUrl("files/findElements.html"));

        TestResult result = runScript(script);

        assertNoError(result);
        assertEquals(1, result.getPages().size());
    }

    @Test
    public void xpathNoQuery() {
        String script = String.format(
            "b = pizza.open(\"%s\");\n" +
            "b.query('xpath://section');\n",
            getTestUrl("files/findElements.html"));

        TestResult result = runScript(script);

        assertError("Unable to find element for xpath '//section'!", result);
        assertEquals(1, result.getPages().size());
    }

    @Test
    public void findElementsCssWeird() {
        // document.querySelector() can't be used to find ids starting with a number,
        // check that we are able.
        String script = String.format(
            "b = pizza.open(\"%s\");\n" +
            "b.verifyExists('#0');\n" +
            "b.verifyNotExists('#999');\n",
            getTestUrl("files/findElements2.html"));

        TestResult result = runScript(script);

        assertNoError(result);
        assertEquals(1, result.getPages().size());
    }
}
