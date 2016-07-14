package com.loadtestgo.script.api;

public class Data {
    String type;
    byte[] bytes;

    public Data(String type, byte[] bytes) {
        this.bytes = bytes;
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public byte[] getBytes() {
        return bytes;
    }
}
