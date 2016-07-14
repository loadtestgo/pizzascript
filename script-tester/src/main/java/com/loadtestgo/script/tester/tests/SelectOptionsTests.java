package com.loadtestgo.script.tester.tests;

import com.loadtestgo.script.api.ErrorType;
import com.loadtestgo.script.api.TestResult;
import com.loadtestgo.script.tester.framework.JavaScriptTest;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SelectOptionsTests extends JavaScriptTest {
    @Test
    public void select() {
        String script = String.format(
            "b = pizza.open(\"%s\");\n" +
            "assert.eq(b.getValue('select'), 'value2');\n" +
            // Select by index
            "b.select('select', {index: 2});\n" +
            "assert.eq(b.getValue('select'), 'value3');\n" +
            // Select by value
            "b.select('select', {value: 'value1'});\n" +
            "assert.eq(b.getValue('select'), 'value1');\n" +
            // Select by text
            "b.select('select', {text: 'Value 2'});\n" +
            "assert.eq(b.getValue('select'), 'value2');\n" +
            // Select by text match
            "b.select('select', {match: '3'});\n" +
            "assert.eq(b.getValue('select'), 'value3');\n",
            getTestUrl("files/form.html"));

        TestResult result = runScript(script);

        assertNoError(result);
        assertEquals(1, result.getPages().size());
    }

    @Test
    public void selectValueError() {
        String script = String.format(
            "b = pizza.open(\"%s\");\n" +
            "assert.eq(b.getValue('select'), 'value2');\n" +
            "b.select('select', {value: 'value12'});\n",
            getTestUrl("files/form.html"));

        TestResult result = runScript(script);

        assertError("Error: Unable to find option with value 'value12'!", ErrorType.Script, result);
        assertEquals(1, result.getPages().size());
    }

    @Test
    public void selectTextError() {
        String script = String.format(
            "b = pizza.open(\"%s\");\n" +
            "assert.eq(b.getValue('select'), 'value2');\n" +
            "b.select('select', {text: 'Value 12'});\n",
            getTestUrl("files/form.html"));

        TestResult result = runScript(script);

        assertError("Error: Unable to find option with text 'Value 12'!", ErrorType.Script, result);
        assertEquals(1, result.getPages().size());
    }

    @Test
    public void selectMultiple() {
        String script = String.format(
            "b = pizza.open(\"%s\");\n" +
            // Index
            "b.select('#toppings', { index: [1, 2] });\n" +
            "assert.eq(b.getValue('#toppings'), ['greenpeppers','onions']);\n" +
            // Value
            "b.select('#toppings', { value: ['tomatoes', 'olives'] });\n" +
            "assert.eq(b.getValue('#toppings'), ['tomatoes','olives']);\n" +
            // Text
            "b.select('#toppings', { text: ['Tomatoes', 'Onions'] });\n" +
            "assert.eq(b.getValue('#toppings'), ['onions','tomatoes']);\n" +
            // Text Match
            "b.select('#toppings', { match: ['toes'] });\n" +
            "assert.eq(b.getValue('#toppings'), ['tomatoes']);\n",
            getTestUrl("files/form.html"));

        TestResult result = runScript(script);

        assertNoError(result);
        assertEquals(1, result.getPages().size());
    }

    @Test
    public void selectMultipleNoClear() {
        String script = String.format(
            "b = pizza.open(\"%s\");\n" +
            // Index
            "b.select('#toppings', { index: [1] });\n" +
            "assert.eq(b.getValue('#toppings'), ['greenpeppers']);\n" +
            "b.select('#toppings', { index: [2], clear: false });\n" +
            "assert.eq(b.getValue('#toppings'), ['greenpeppers','onions']);\n" +
            // Value
            "b.select('#toppings', { value: ['olives'], clear: false });\n" +
            "assert.eq(b.getValue('#toppings'), ['greenpeppers','onions', 'olives']);\n" +
            // Text
            "b.select('#toppings', { text: ['Tomatoes'], clear: false });\n" +
            "assert.eq(b.getValue('#toppings'), ['greenpeppers','onions', 'tomatoes', 'olives']);\n" +
            // Text Match
            "b.select('#toppings', { match: ['shrooms'], clear: false });\n" +
            "assert.eq(b.getValue('#toppings'), ['mushrooms', 'greenpeppers','onions', 'tomatoes', 'olives']);\n",
            getTestUrl("files/form.html"));

        TestResult result = runScript(script);

        assertNoError(result);
        assertEquals(1, result.getPages().size());
    }

    @Test
    public void selectMultipleError() {
        String script = String.format(
            "b = pizza.open(\"%s\");\n" +
            "b.select('#toppings', { text: ['Toes', 'Finger Nails'] });\n",
            getTestUrl("files/form.html"));

        TestResult result = runScript(script);

        assertError("Error: Unable to find option with text 'Toes'!", ErrorType.Script, result);
        assertEquals(1, result.getPages().size());
    }

    @Test
    public void selectMultipleValueError() {
        String script = String.format(
            "b = pizza.open(\"%s\");\n" +
            "b.select('#toppings', { value: ['toes'] });\n",
            getTestUrl("files/form.html"));

        TestResult result = runScript(script);

        assertError("Error: Unable to find option with value 'toes'!", ErrorType.Script, result);
        assertEquals(1, result.getPages().size());
    }

    @Test
    public void selectMultipleIndexError() {
        String script = String.format(
            "b = pizza.open(\"%s\");\n" +
            "b.select('#toppings', { index: [20] });\n",
            getTestUrl("files/form.html"));

        TestResult result = runScript(script);

        assertError("Error: Unable to find option with index '20'!", ErrorType.Script, result);
        assertEquals(1, result.getPages().size());
    }

    @Test
    public void selectMultipleMatchError() {
        String script = String.format(
            "b = pizza.open(\"%s\");\n" +
            "b.select('#toppings', { match: ['Toes'] });\n",
            getTestUrl("files/form.html"));

        TestResult result = runScript(script);

        assertError("Error: Unable to find option with text that matches /Toes/!", ErrorType.Script, result);
        assertEquals(1, result.getPages().size());
    }
}
