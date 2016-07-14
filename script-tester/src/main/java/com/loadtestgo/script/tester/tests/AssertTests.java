package com.loadtestgo.script.tester.tests;

import com.loadtestgo.script.api.ErrorType;
import com.loadtestgo.script.api.TestResult;
import com.loadtestgo.script.tester.framework.JavaScriptTest;
import org.junit.Test;

public class AssertTests extends JavaScriptTest {
    @Test
    public void ok() {
        String script =
            "var a = 1;\n" +
            "assert.ok(a);\n" +
            "assert.ok(true);\n" +
            "var b = 1;\n" +
            "assert.equal(a, b);\n" +
            "assert.eq(a, b);\n" +
            "var c = 'a';\n" +
            "var d = 'a';\n" +
            "assert.equal(c, d);\n" +
            "assert.eq(c, d);\n" +
            "assert.notEqual(a, d);\n" +
            "assert.ne(a, d);\n";

        TestResult result = runScript(script);

        assertNoError(result);
    }

    @Test
    public void equalFail1() {
        TestResult result = runScript("assert.equal(1, 2);");
        assertError("assert.equal: '1' not equal '2'", ErrorType.Script, result);
    }

    @Test
    public void eqFail2() {
        TestResult result = runScript("assert.eq('a', 'b');");
        assertError("assert.eq: 'a' not equal 'b'", ErrorType.Script, result);
    }

    @Test
    public void neFail() {
        TestResult result = runScript("assert.ne('a', 'a');");
        assertError("assert.ne: 'a' and 'a' are equal", ErrorType.Script, result);
    }

    @Test
    public void equalFail2() {
        TestResult result = runScript("assert.equal('a', 'b');");
        assertError("assert.equal: 'a' not equal 'b'", ErrorType.Script, result);
    }

    @Test
    public void notEqualFail() {
        TestResult result = runScript("assert.notEqual('a', 'a');");
        assertError("assert.notEqual: 'a' and 'a' are equal", ErrorType.Script, result);
    }

    @Test
    public void fail() {
        TestResult result = runScript("assert.fail();");
        assertError("assert.fail()", ErrorType.Script, result);
    }

    @Test
    public void failMessage() {
        TestResult result = runScript("assert.fail('message');");
        assertError("message", ErrorType.Script, result);
    }

    @Test
    public void arrayTest() {
        TestResult result = runScript(
            "assert.eq([], []);\n" +
            "assert.ne([1], [2]);\n" +
            "assert.eq([1,2], [1,2]);\n" +
            "assert.ne([2,1], [1,2]);\n" +
            "assert.ne([1], [1,2]);\n");

        assertNoError(result);
    }
}
