package com.loadtestgo.script.runner.config;

import com.loadtestgo.script.runner.RunnerTest;

import java.util.ArrayList;
import java.util.List;

public class JsonConfig {
    private String filename;
    private List<RunnerTest> tests;

    public String getFilename() {
        return filename;
    }

    public void setFileName(String filename) {
        this.filename = filename;
    }

    public List<RunnerTest> getTests() {
        return tests;
    }

    public void setTests(List<RunnerTest> tests) {
        this.tests = tests;
    }

    public void addTest(RunnerTest test) {
        if (tests == null) {
            tests = new ArrayList<>();
        }
        tests.add(test);
    }
}
