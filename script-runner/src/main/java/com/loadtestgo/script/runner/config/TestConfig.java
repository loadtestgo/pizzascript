package com.loadtestgo.script.runner.config;

import com.loadtestgo.script.runner.RunnerTest;
import com.loadtestgo.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestConfig {
    private String filename;
    private List<RunnerTest> tests;
    private String name;
    private Map<String, String> settings = new HashMap<>();
    private double defaultTimeout;

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

    public void setSettings(Map<String, String> settings) {
        this.settings = settings;
    }

    public Map<String, String> getSettings() {
        return settings;
    }

    public boolean getKeepBrowserOpen() {
        return getReuseSession();
    }

    public boolean getReuseSession() {
        Object reuseSession = settings.get("reuse.session");
        if (reuseSession == null) {
            return false;
        }
        if (reuseSession instanceof Boolean) {
            return (Boolean)reuseSession;
        }
        return false;
    }

    public void setDefaultTimeout(double defaultTimeout) {
        this.defaultTimeout = defaultTimeout;
    }

    public double getDefaultTimeout() {
        return defaultTimeout;
    }
}
