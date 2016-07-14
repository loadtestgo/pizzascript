package com.loadtestgo.script.har;

import com.fasterxml.jackson.annotation.JsonInclude;

public class HarEntry {
    public String pageref;
    public String startedDateTime;
    public long time;
    public HarRequest request;
    public HarResponse response;

    @JsonInclude(JsonInclude.Include.ALWAYS)
    public HarCache cache;

    public HarEntryTimings timings;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String serverIPAddress;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String connection;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String comment;

    public HarEntry() {
        this.request = new HarRequest();
        this.response = new HarResponse();
        this.cache = new HarCache();
        this.timings = new HarEntryTimings();
    }
}
