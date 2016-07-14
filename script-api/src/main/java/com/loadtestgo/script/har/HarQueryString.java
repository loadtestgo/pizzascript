package com.loadtestgo.script.har;

import com.fasterxml.jackson.annotation.JsonInclude;

public class HarQueryString {
    public String name;
    public String value;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String comment;
}
