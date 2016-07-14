package com.loadtestgo.script.tester.tests;

import com.loadtestgo.script.api.ErrorType;
import com.loadtestgo.script.api.TestResult;
import com.loadtestgo.script.tester.framework.JavaScriptTest;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ScriptTests extends JavaScriptTest {
    @Test
    public void syntaxError() {
        TestResult result = runScript("!#wallawalla");
        assertEquals(ErrorType.Script, result.getError().type);
        assertEquals(test.getMethodName(), result.getError().file);
        assertEquals(1, result.getError().line);
    }

    @Test
    public void unknownFunction1() {
        TestResult result = runScript("s.cheese()");
        assertEquals(ErrorType.Script, result.getError().type);
        assertEquals(test.getMethodName(), result.getError().file);
        assertEquals(1, result.getError().line);
    }

    @Test
    public void unknownFunction2() {
        TestResult result = runScript("cheese()");
        assertEquals(ErrorType.Script, result.getError().type);
        assertEquals(test.getMethodName(), result.getError().file);
        assertEquals(1, result.getError().line);
    }

    @Test
    public void propAsFunction() {
        TestResult result = runScript(
            "var a = {};\n" +
            "a.x = 'hello';\n" +
            "a.x()");
        assertEquals(ErrorType.Script, result.getError().type);
        assertEquals(test.getMethodName(), result.getError().file);
        assertEquals(3, result.getError().line);
    }
}
