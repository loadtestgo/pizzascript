package com.loadtestgo.script.har;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.ArrayList;

public class HarRequest {
    public String method;
    public String url;
    public String httpVersion;
    public ArrayList<HarCookie> cookies;
    public ArrayList<HarHeader> headers;

    @JsonInclude(JsonInclude.Include.ALWAYS)
    public ArrayList<HarQueryString> queryString;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public HarPostData postData;

    public int headersSize;
    public int bodySize;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String comment;

    HarRequest() {
        this.headers = new ArrayList<>();
        this.cookies = new ArrayList<>();
        this.queryString = new ArrayList<>();
        this.headersSize = 0;
        this.bodySize = 0;
    }
}
