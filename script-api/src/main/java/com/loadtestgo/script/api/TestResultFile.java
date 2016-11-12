package com.loadtestgo.script.api;

import java.io.File;

public class TestResultFile {
    private String name;
    private File file;
    private Data data;

    public TestResultFile(String name, Data data) {
        this.name = name;
        this.data = data;
    }

    public TestResultFile(String name, File file) {
        this.name = name;
        this.file = file;
    }

    public TestResultFile(File file) {
        this.file = file;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }
}
