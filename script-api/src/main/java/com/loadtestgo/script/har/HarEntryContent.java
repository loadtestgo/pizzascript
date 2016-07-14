package com.loadtestgo.script.har;

import com.fasterxml.jackson.annotation.JsonInclude;

public class HarEntryContent {
    public int size = -1;
    public int compression = 0;
    public String mimeType = "";

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String text;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String encoding;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String comment;
}
