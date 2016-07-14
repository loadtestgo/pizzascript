package com.loadtestgo.util;

public class Guid {
    static public String gen() {
        return java.util.UUID.randomUUID().toString().replaceAll("-", "");
    }
}
