package com.loadtestgo.script.har;

import com.fasterxml.jackson.annotation.JsonInclude;

public class HarCreator {
    public String name;
    public String version;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String comment;
}
