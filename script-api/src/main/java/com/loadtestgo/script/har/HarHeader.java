package com.loadtestgo.script.har;

import com.fasterxml.jackson.annotation.JsonInclude;

public class HarHeader {
    public String name;
    public String value;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String comment;

    public HarHeader(String name, String value) {
        this.name = name;
        this.value = value;
    }
}
