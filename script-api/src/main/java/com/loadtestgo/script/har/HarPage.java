package com.loadtestgo.script.har;

import com.fasterxml.jackson.annotation.JsonInclude;

public class HarPage {
    public String startedDateTime;
    public String id;
    public String title;
    public HarPageTimings pageTimings;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String comment;

    public HarPage() {
        this.pageTimings = new HarPageTimings();
    }
}
