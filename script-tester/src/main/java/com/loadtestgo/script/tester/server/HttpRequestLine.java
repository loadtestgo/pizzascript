package com.loadtestgo.script.tester.server;

public class HttpRequestLine {
    private String type;
    private String protocol;
    private String location;

    public boolean parse(String header) {
        String[] r = header.split(" ", 4);
        if (r.length != 3) {
            return false;
        }

        type = r[0];
        location = r[1];
        protocol = r[2];

        return true;
    }

    public String getType() {
        return type;
    }

    public String getProtocol() {
        return protocol;
    }

    public String getLocation() {
        return location;
    }
}
