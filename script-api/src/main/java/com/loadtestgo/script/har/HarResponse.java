package com.loadtestgo.script.har;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.ArrayList;

public class HarResponse {
    public int status;
    public String statusText = "";
    public String httpVersion = "HTTP/1.1";
    public ArrayList<HarCookie> cookies;
    public ArrayList<HarHeader> headers;
    public HarEntryContent content;

    @JsonInclude(JsonInclude.Include.ALWAYS)
    public String redirectURL;

    public int headersSize;
    public int bodySize;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String comment;

    public HarResponse() {
        this.headers = new ArrayList<>();
        this.cookies = new ArrayList<>();
        this.content = new HarEntryContent();
        this.redirectURL = "";
        this.headersSize = 1;
        this.status = -1;
    }
}
