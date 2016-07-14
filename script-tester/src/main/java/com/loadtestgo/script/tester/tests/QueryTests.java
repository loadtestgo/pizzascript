package com.loadtestgo.script.tester.tests;

import com.loadtestgo.script.api.TestResult;
import com.loadtestgo.script.tester.framework.JavaScriptTest;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class QueryTests extends JavaScriptTest {
    @Test
    public void innerTextAndHtml() {
        String script = String.format(
            "b = pizza.open(\"%s\");\n" +
            "var v = b.getInnerText('#div1');\n" +
            "assert.eq(v.trim(), 'Div with id \\'div1\\'');\n" +
            "v = b.getInnerHTML('#div1');\n" +
            "assert.eq(v, '<p>Div with id \\'div1\\'</p>');\n",
            getTestUrl("files/findElements.html"));

        TestResult result = runScript(script);

        assertNoError(result);
        assertEquals(1, result.getPages().size());
    }

    @Test
    public void query1() {
        String script = String.format(
            "b = pizza.open(\"%s\");\n" +
            "var v = b.query('#div1');\n" +
            "assert.eq(v.length, 1);\n" +
            "assert.eq(v[0].tag, 'div');\n" +
            "assert.eq(v[0].attributes.id, 'div1');\n" +
            "assert.eq(v[0].path, '#div1');\n",
            getTestUrl("files/findElements.html"));

        TestResult result = runScript(script);

        assertNoError(result);
        assertEquals(1, result.getPages().size());
    }

    @Test
    public void query2() {
        String script = String.format(
            "b = pizza.open(\"%s\");\n" +
            "var v = b.query('div');\n" +
            "assert.eq(v.length, 11);\n" +
            "v = b.query('notatag');\n" +
            "assert.eq(v.length, 0);\n",
            getTestUrl("files/findElements.html"));

        TestResult result = runScript(script);

        assertNoError(result);
        assertEquals(1, result.getPages().size());
    }

    @Test
    public void queryVisible() {
        String script = String.format(
            "b = pizza.open(\"%s\");\n" +
            "var v = b.queryVisible('#div1');\n" +
            "assert.eq(v.length, 1);\n" +
            "var v = b.queryVisible('#div3');\n" +
            "assert.eq(v.length, 0);\n" +
            "var v = b.queryVisible('#div4');\n" +
            "assert.eq(v.length, 0);\n" +
            "var v = b.queryVisible('#div6');\n" +
            "assert.eq(v.length, 0);\n",
            getTestUrl("files/findElements.html"));

        TestResult result = runScript(script);

        assertNoError(result);
        assertEquals(1, result.getPages().size());
    }

    @Test
    public void queryHidden() {
        String script = String.format(
            "b = pizza.open(\"%s\");\n" +
            "var v = b.query('#div4');\n" +
            "assert.eq(v.length, 1);\n" +
            "assert.eq(v[0].pos, undefined);\n" +
            "assert.eq(v[0].hiddenParent, '#div3');\n" +
            "assert.eq(v[0].visible, false);\n" +
            "v = b.query('#div6');\n" +
            "assert.eq(v.length, 1);\n" +
            "assert.eq(v[0].visible, false);\n" +
            "assert.eq(v[0].hiddenBy, '#div7');\n",
            getTestUrl("files/findElements.html"));

        TestResult result = runScript(script);

        assertNoError(result);
        assertEquals(1, result.getPages().size());
    }
}

