package com.loadtestgo.script.api;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TestResult {
    private int nextPageId = 0;

    public ArrayList<Page> pages = new ArrayList<>();

    public List<OutputMessage> output = new ArrayList<>();

    // When the test first begun
    public Date startTime;
    // Total runTime including the time sleeping and spent opening the browser
    public int runTime;
    // number of milliseconds of runTime spent on overhead of opening browser
    public int setupTime = 0;
    public TestError error;
    public String testName;
    public String browserName;
    public String browserVersion;
    // ip address the test ran from
    public String ip;

    public String botName;

    public boolean hasScreenshot;
    public boolean hasVideo;
    public boolean hasPacketFile;

    public ArrayList<TestResultFile> savedFiles = new ArrayList<>();

    public TestResult() {
    }

    public TestResult(String name) {
        this.testName = name;
    }

    public ArrayList<Page> getPages() {
        synchronized(this) {
            return pages;
        }
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public int getRunTime() {
        return runTime;
    }

    public void setRunTime(int runTime) {
        synchronized (this) {
            this.runTime = runTime;
        }
    }

    public int getSetupTime() {
        return setupTime;
    }

    public void setSetupTime(int setupTime) {
        this.setupTime = setupTime;
    }

    public void addSetupTime(int sleepTime) {
        this.setupTime += sleepTime;
    }

    @JsonIgnore
    public boolean isSuccess() {
        return error == null;
    }

    public Page getPage(int pageIndex) {
        synchronized(this) {
            if (pageIndex < pages.size()) {
                return pages.get(pageIndex);
            }
            return null;
        }
    }

    @Override
    public String toString() {
        return "TestResult";
    }

    @JsonIgnore
    public Page getLastPage() {
        synchronized(this) {
            if (pages.size() == 0) {
                return null;
            }
            return pages.get(pages.size() - 1);
        }
    }

    public void addPage(Page page) {
        synchronized(this) {
            pages.add(page);
            page.setPageId(nextPageId++);
        }
    }

    public void setTestName(String testName) {
        this.testName = testName;
    }

    public String getTestName() {
        return testName;
    }

    public void setBrowserName(String browserName) {
        this.browserName = browserName;
    }

    public String getBrowserName() {
        return browserName;
    }

    public void setBrowserVersion(String browserVersion) {
        this.browserVersion = browserVersion;
    }

    public String getBrowserVersion() {
        return browserVersion;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public TestError getError() {
        return error;
    }

    public void setError(TestError error) {
        this.error = error;
    }

    public String getBotName() {
        return botName;
    }

    public void setBotName(String botName) {
        this.botName = botName;
    }

    @JsonSetter("hasScreenshot")
    public void setHasScreenshot(boolean screenshot) {
        this.hasScreenshot = screenshot;
    }

    @JsonGetter("hasScreenshot")
    public boolean hasScreenshot() {
        return hasScreenshot;
    }

    @JsonSetter("hasVideo")
    public void setHasVideo(boolean video) {
        this.hasVideo = video;
    }

    @JsonGetter("hasVideo")
    public boolean hasVideo() {
        return hasVideo;
    }

    @JsonSetter("hasPacketFile")
    public void hasPacketFile(boolean hasPacketFile) {
        this.hasPacketFile = hasPacketFile;
    }

    @JsonGetter("hasPacketFile")
    public boolean hasPacketFile() {
        return hasPacketFile;
    }

    public List<TestResultFile> getSavedFiles() {
        return savedFiles;
    }

    @JsonIgnore
    public Integer getBrowserVersionMajor() {
        String[] versions = browserVersion.split("\\.");
        if (versions.length > 0) {
            return Integer.parseInt(versions[0]);
        } else {
            return null;
        }
    }

    static public class OutputMessage {
        public long time;
        public String msg;
    }

    public void addOutput(String msg) {
        OutputMessage message = new OutputMessage();
        message.msg = msg;
        if (startTime != null) {
            long startTime = this.startTime.getTime();
            message.time = System.currentTimeMillis() - startTime;
        }
        output.add(message);
    }
}
