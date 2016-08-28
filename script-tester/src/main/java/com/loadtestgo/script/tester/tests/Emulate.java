package com.loadtestgo.script.tester.tests;

import com.loadtestgo.script.api.ErrorType;
import com.loadtestgo.script.api.TestResult;
import com.loadtestgo.script.tester.framework.JavaScriptTest;
import org.junit.Test;

public class Emulate extends JavaScriptTest {
    @Test
    public void emulate2G() {
        String script = String.format(
            "b = pizza.open();\n" +
            "b.emulateNetworkCondition('Regular 2G');\n" +
            "b.open(\"%s\");\n",
            getTestUrl("files/basic.html"));

        TestResult result = runScript(script);

        assertOnePage(result);
        assertOneRequest(result);
        assertNoError(result);
    }

    @Test
    public void emulateUnknown() {
        String script = String.format(
            "b = pizza.open();\n" +
            "b.emulateNetworkCondition('Not a type');\n" +
            "b.open(\"%s\");\n",
            getTestUrl("files/basic.html"));

        TestResult result = runScript(script);

        assertError("Unknown network condition \'Not a type\'", ErrorType.Script, result);
    }

    @Test
    public void emulateReset() {
        String script = String.format(
            "b = pizza.open();\n" +
            "b.emulateNetworkCondition('Regular 2G');\n" +
            "b.emulateNetworkCondition({});\n" +
            "b.open(\"%s\");\n",
            getTestUrl("files/basic.html"));

        TestResult result = runScript(script);

        assertOnePage(result);
        assertOneRequest(result);
        assertNoError(result);
    }
}
