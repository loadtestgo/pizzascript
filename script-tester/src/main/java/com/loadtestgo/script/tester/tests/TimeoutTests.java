package com.loadtestgo.script.tester.tests;

import com.loadtestgo.script.api.ErrorType;
import com.loadtestgo.script.api.HttpRequest;
import com.loadtestgo.script.api.TestResult;
import com.loadtestgo.script.tester.framework.JavaScriptTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.assertEquals;

@RunWith(JUnit4.class)
public class TimeoutTests extends JavaScriptTest {
    @Test
    public void serverTimeout() {
        checkTimeout("timeouts/socketTimeoutDuringContent", HttpRequest.State.Recv);
    }

    @Test
    public void timeoutBeforeHeaders() {
        checkTimeout("timeouts/timeoutBeforeHeaders", HttpRequest.State.Send);
    }

    @Test
    public void timeoutAfterResponseLine() {
        checkTimeout("timeouts/timeoutAfterResponseLine", HttpRequest.State.Send);
    }

    @Test
    public void timeoutAfterHeaders() {
        checkTimeout("timeouts/timeoutAfterHeaders", HttpRequest.State.Send);
    }

    void checkTimeout(String url, HttpRequest.State state) {
        long defaultTimeout = 3000;
        TestResult result = basicGetTestUrl(url, defaultTimeout);
        commonTimeoutAsserts(result, state, defaultTimeout);
    }

    void commonTimeoutAsserts(TestResult result, HttpRequest.State state, long timeout) {
        assertEquals(ErrorType.Timeout, result.getError().type);
        assert(result.getRunTime() >= timeout);

        assertOnePage(result);
        assertOneRequest(result);
        HttpRequest request = getFirstRequest(result);
        assertRequestState(state, request);
    }
}
