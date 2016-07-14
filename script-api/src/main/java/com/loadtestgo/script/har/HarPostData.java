package com.loadtestgo.script.har;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.ArrayList;

public class HarPostData {
    public String mimeType;
    public ArrayList<HarPostParams> params;
    public String text;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String comment;
}
