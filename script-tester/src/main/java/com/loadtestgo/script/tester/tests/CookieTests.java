package com.loadtestgo.script.tester.tests;

import com.loadtestgo.script.api.ErrorType;
import com.loadtestgo.script.api.TestResult;
import com.loadtestgo.script.tester.framework.JavaScriptTest;
import org.junit.Test;

public class CookieTests extends JavaScriptTest {
    @Test
    public void testCookiesSamePage() {
        String script = String.format(
            "b = pizza.open(\"%s\");\n" +
            "var cookies = b.listCookies();\n" +
            "assert.eq(2, cookies.length);\n" +
            "b.setCookie('a', 'b');\n" +
            "assert.eq('b', b.getCookie('a').value);\n" +
            "assert.eq(3, b.listCookies().length);\n" +
            "b.setCookie('a', 'c');\n" +
            "assert.eq('c', b.getCookie('a').value);\n" +
            "assert.eq(3, b.listCookies().length);\n" +
            "b.clearCookies();\n" +
            "assert.eq(0, b.listCookies().length);\n" +
            "b.setCookie('a', 'c');\n" +
            "assert.eq(1, b.listCookies().length);\n" +
            "assert.eq('c', b.getCookie('a').value);\n" +
            "b.removeCookie('a');\n" +
            "assert.eq(0, b.listCookies().length);\n",
            getTestUrl("files/cookies.html"));

        TestResult result = runScript(script);
        assertNoError(result);
    }

    @Test
    public void testBadUrlSetCookie() {
        String script = String.format(
            "b = pizza.open(\"%s\");\n" +
            "b.setCookie('a', 'b', {url:'http::'});\n",
            getTestUrl("files/cookies.html"));

        TestResult result = runScript(script);
        assertError("Invalid url: \"http::\".", ErrorType.Script, result);
    }

    @Test
    public void testBadUrlGetCookie() {
        String script = String.format(
            "b = pizza.open(\"%s\");\n" +
            "b.getCookie('http::', 'a');\n",
            getTestUrl("files/cookies.html"));

        TestResult result = runScript(script);
        assertError("Invalid url: \"http::\".", ErrorType.Script, result);
    }

    @Test
    public void testBadUrlRemoveCookie() {
        String script = String.format(
            "b = pizza.open(\"%s\");\n" +
            "b.removeCookie('http::', 'a');\n",
            getTestUrl("files/cookies.html"));

        TestResult result = runScript(script);
        assertError("Invalid url: \"http::\".", ErrorType.Script, result);
    }
}
