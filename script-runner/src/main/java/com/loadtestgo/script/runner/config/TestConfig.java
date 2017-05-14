package com.loadtestgo.script.runner.config;

import com.loadtestgo.script.runner.RunnerTest;
import com.loadtestgo.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TestConfig {
    private String filename;
    private List<RunnerTest> tests;
    private String name;
    private Map<String, Object> settings;

    public void initTestNames() {
        for (RunnerTest test : tests) {
            if (StringUtils.isEmpty(test.getName())) {
                test.setName(test.getFileName());
            }
        }
    }

    public String getFileName() {
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSettings(Map<String, Object> settings) {
        this.settings = settings;
    }

    public Map<String, Object> getSettings() {
        return settings;
    }

    public boolean getKeepBrowserOpen() {
        return getReuseSession();
    }

    public boolean getReuseSession() {
        Object resuseSession = settings.get("reuse.session");
        if (resuseSession == null) {
            return false;
        }
        if (resuseSession instanceof Boolean) {
            return (Boolean)resuseSession;
        }
        return false;
    }
}
