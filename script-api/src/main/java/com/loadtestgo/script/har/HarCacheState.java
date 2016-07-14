package com.loadtestgo.script.har;

import com.fasterxml.jackson.annotation.JsonInclude;

public class HarCacheState {
    public String expires;
    public String lastAccess;
    public String eTag;

    public int hitCount;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String comment;
}
