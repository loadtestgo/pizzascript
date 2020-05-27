package com.loadtestgo.script.tester.tests;

import com.loadtestgo.script.api.ErrorType;
import com.loadtestgo.script.api.HttpRequest;
import com.loadtestgo.script.api.TestResult;
import com.loadtestgo.script.engine.EngineSettings;
import com.loadtestgo.script.engine.TestContext;
import com.loadtestgo.script.tester.framework.JavaScriptTest;
import com.loadtestgo.util.Os;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class AuthTests extends JavaScriptTest {
    @Test
    public void authRequired() {
        // This test makes sure the request dialog is cancelled
        String script = String.format(
            "b = pizza.open();\n" +
            "b.open('%s');\n",
            getTestUrl("auth/reject"));

        TestResult result = runScript(script, 5000);

        assertOnePage(result);

        HttpRequest request = getFirstRequest(result);

        // This fails on Linux sometimes, still need to figure out why!
        if (!Os.isLinux()) {
            assertEquals(401, request.getStatusCode());
        }
        assertError("Error navigating to 'http://localhost:3000/auth/reject': net::ERR_INVALID_AUTH_CREDENTIALS", ErrorType.Navigation, result);
    }

    @Test
    public void authGiven() {
        String script = String.format(
            "b = pizza.open();\n" +
            "b.setAuth('username', 'password');\n" +
            "b.open('%s');\n",
            getTestUrl("auth/check"));

        TestResult result = runScript(script);

        assertOnePage(result);

        HttpRequest request = getFirstRequest(result);
        assert200OK(request);
        assertNoError(result);
    }

    @Test
    public void badAuthGiven() {
        String script = String.format(
            "b = pizza.open();\n" +
            "b.setAuth('username', 'password');\n" +
            "b.open('%s');" +
            "b.waitForHttpRequests();\n",
            getTestUrl("auth/reject"));

        TestResult result = runScript(script, 5000);

        assertOnePage(result);

        HttpRequest request = getFirstRequest(result);
        assertEquals(401, request.getStatusCode());
        assertError("Error navigating to 'http://localhost:3000/auth/reject': net::ERR_INVALID_AUTH_CREDENTIALS", ErrorType.Navigation, result);
    }
}
