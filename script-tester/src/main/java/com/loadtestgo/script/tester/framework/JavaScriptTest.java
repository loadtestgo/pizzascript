package com.loadtestgo.script.tester.framework;

import com.loadtestgo.script.api.TestResult;
import com.loadtestgo.script.engine.EasyTestContext;
import com.loadtestgo.script.engine.JavaScriptEngine;

import java.util.concurrent.atomic.AtomicInteger;

public class JavaScriptTest extends BaseTest {
    public static AtomicInteger testId = new AtomicInteger();

    public TestResult basicGetTestUrl(String url) {
        return basicGetTestUrl(url, TestSettings.getDefaultScriptTimeout());
    }

    public TestResult basicGetTestUrl(String url, long timeout) {
        return basicGetUrl(getTestUrl(url), timeout);
    }

    public TestResult basicGetUrl(String url) {
        return basicGetTestUrl(url, TestSettings.getDefaultScriptTimeout());
    }

    public TestResult basicGetUrl(String url, long timeout) {
        String script = String.format(
                "pizza.open(\"%s\");", url);

        return runScript(script, timeout);
    }

    public TestResult runScript(String script) {
        return runScript(script, TestSettings.getDefaultScriptTimeout());
    }

    public TestResult runScript(String script, long timeout) {
        EasyTestContext testContext = new EasyTestContext();
        TestResult results = testContext.getTestResult();
        JavaScriptEngine javaScriptEngine = new JavaScriptEngine();
        javaScriptEngine.init(testContext);
        try {
            javaScriptEngine.runScript(script, test.getMethodName(), timeout);
        } finally {
            javaScriptEngine.finish();
        }
        return results;
    }
}
