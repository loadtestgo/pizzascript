package com.loadtestgo.script.runner;

import java.io.File;

public class RunnerTest {
    private File file;
    private long timeout;
    private String name;

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
}
