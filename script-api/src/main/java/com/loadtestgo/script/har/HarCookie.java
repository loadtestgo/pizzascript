package com.loadtestgo.script.har;

import com.fasterxml.jackson.annotation.JsonInclude;

public class HarCookie {
    public static final String SECURE = "Secure";
    public static final String COMMENT = "Comment";
    public static final String PATH = "Path";
    public static final String DOMAIN = "Domain";
    public static final String EXPIRES = "Expires";
    public static final String HTTP_ONLY = "HttpOnly";

    public String name;
    public String value;
    public String path;
    public String domain;
    public String expires;
    public boolean httpOnly;
    public boolean secure;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String comment;
}
