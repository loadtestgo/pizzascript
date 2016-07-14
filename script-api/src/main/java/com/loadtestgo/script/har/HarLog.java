package com.loadtestgo.script.har;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.ArrayList;

public class HarLog {
    public String version;
    public HarCreator creator;
    public HarBrowser browser;
    public ArrayList<HarPage> pages;
    public ArrayList<HarEntry> entries;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String comment;

    public HarLog() {
        this.version = "1.2";
        this.creator = new HarCreator();
        this.browser = new HarBrowser();
        this.pages = new ArrayList<>();
        this.entries = new ArrayList<>();
    }
}
