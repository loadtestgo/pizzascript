package com.loadtestgo.script.tester.server;

import com.loadtestgo.util.HttpHeader;

import java.util.ArrayList;

public class HttpHeaders extends ArrayList<HttpHeader> {
    public String get(String name) {
        for (HttpHeader header : this) {
            if (header.name.equalsIgnoreCase(name)) {
                return header.value;
            }
        }
        return null;
    }
}
