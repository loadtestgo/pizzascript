package com.loadtestgo.script.har;

import com.fasterxml.jackson.annotation.JsonInclude;

public class HarEntryTimings {
    public long blocked = -1;
    public long dns = -1;
    public long connect = -1;
    public long send = -1;
    public long wait = -1;
    public long receive = -1;
    public long ssl = -1;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String comment;
}
