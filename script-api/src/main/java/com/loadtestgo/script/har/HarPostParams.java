package com.loadtestgo.script.har;

import com.fasterxml.jackson.annotation.JsonInclude;

public class HarPostParams {
    public String name;
    public String value;
    public String fileName;
    public String contentType;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String comment;
}
