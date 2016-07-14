package com.loadtestgo.script.har;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

public class HarBrowser {
    public String name;
    public String version;

    @JsonInclude(Include.NON_NULL)
    public String comment;
}
