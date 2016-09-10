package com.loadtestgo.script.runner;

import java.io.File;

public class RunnerTest {
    private File file;
    private long timeout;
    private String name;

    private String screenshotFilePath;
    private String harFilePath;
    private String consoleLogFilePath;

    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    public void setName(String name) {
        this.name = name;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public String getName() {
        return name;
    }

    public String getFileName() {
        return file.getPath();
    }

    public String getScreenshotFilePath() {
        return screenshotFilePath;
    }

    public String getHarFilePath() {
        return harFilePath;
    }

    public String getConsoleLogFilePath() {
        return consoleLogFilePath;
    }

    public void setScreenshotFilePath(String screenshotFilePath) {
        this.screenshotFilePath = screenshotFilePath;
    }

    public void setHarFilePath(String harFilePath) {
        this.harFilePath = harFilePath;
    }

    public void setConsoleLogFilePath(String consoleLogFilePath) {
        this.consoleLogFilePath = consoleLogFilePath;
    }
}
