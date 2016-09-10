package com.loadtestgo.script.runner;

import com.loadtestgo.script.api.TestResult;

public class RunnerTestResult {
    private RunnerTest test;
    private TestResult result;

    public RunnerTestResult(RunnerTest runnerTest, TestResult testResult) {
        this.test = runnerTest;
        this.result = testResult;
    }

    public RunnerTest getTest() {
        return test;
    }

    public void setTest(RunnerTest test) {
        this.test = test;
    }

    public TestResult getResult() {
        return result;
    }

    public void setResult(TestResult result) {
        this.result = result;
    }
}
