package com.loadtestgo.script.tester.tests;

import com.loadtestgo.script.api.ErrorType;
import com.loadtestgo.script.api.NavigationType;
import com.loadtestgo.script.api.Page;
import com.loadtestgo.script.api.TestResult;
import com.loadtestgo.script.tester.framework.JavaScriptTest;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class FormTests extends JavaScriptTest {
    @Test
    public void submitForm() {
        String formUrl = getTestUrl("files/form.html");
        String script = String.format(
            "b = pizza.open(\"%s\");\n" +
            "b.submit('#form1');\n" +
            "b.waitPageLoad();\n",
            formUrl);

        TestResult result = runScript(script);

        assertNoError(result);
        assertEquals(2, result.getPages().size());

        Page post = result.getLastPage();
        assertEquals(formUrl, post.getUrl());
        assertEquals(NavigationType.FormSubmit, post.getNavigationType());
    }

    @Test
    public void submitFormByElement() {
        String formUrl = getTestUrl("files/form.html");
        String script = String.format(
            "b = pizza.open(\"%s\");\n" +
            "b.submit('.submit-button');\n" +
            "b.waitPageLoad();\n",
            formUrl);

        TestResult result = runScript(script);

        assertNoError(result);
        assertEquals(2, result.getPages().size());

        Page post = result.getLastPage();
        assertEquals(formUrl, post.getUrl());
        assertEquals(NavigationType.FormSubmit, post.getNavigationType());
    }

    @Test
    public void submitFormFail() {
        String script = String.format(
            "b = pizza.open(\"%s\");\n" +
            "b.submit('#not-a-form');\n" +
            "b.waitPageLoad();\n",
            getTestUrl("files/form.html"));

        TestResult result = runScript(script);

        assertError("Unable to find element for selector '#not-a-form'!", ErrorType.Script, result);
        assertEquals(1, result.getPages().size());
    }

    @Test
    public void check() {
        String script = String.format(
            "b = pizza.open(\"%s\");\n" +
            "b.check('#checkbox1');\n" +
            "assert.ok(b.checked('#checkbox1'));\n" +
            "b.check('#checkbox1', false);\n" +
            "assert.ok(!b.checked('#checkbox1'));" +
            "b.check('#checkbox1', true);\n" +
            "assert.ok(b.checked('#checkbox1'));\n",
            getTestUrl("files/form.html"));

        TestResult result = runScript(script);

        assertNoError(result);
        assertEquals(1, result.getPages().size());
    }

    @Test
    public void submitMultiple() {
        // Since submit causes a new page to load and injects javascript to each new page,
        // this is a pretty good test.
        String script = String.format(
            "b = pizza.open(\"%s\");\n" +
            "b.submit('form');\n" +
            "b.waitPageLoad();\n" +
            "b.submit('form');\n" +
            "b.waitPageLoad();\n",
            getTestUrl("files/form.html"));

        TestResult result = runScript(script);

        assertNoError(result);
        assertEquals(3, result.getPages().size());
    }
}
