package com.loadtestgo.script.har;

import com.fasterxml.jackson.annotation.JsonInclude;

public class HarPageTimings {
    public long onContentLoad;
    public long onLoad;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String comment;
}
