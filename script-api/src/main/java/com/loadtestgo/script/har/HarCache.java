package com.loadtestgo.script.har;

import com.fasterxml.jackson.annotation.JsonInclude;

public class HarCache {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public HarCacheState beforeRequest;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public HarCacheState afterRequest;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String comment;

    public HarCache() {
    }
}
