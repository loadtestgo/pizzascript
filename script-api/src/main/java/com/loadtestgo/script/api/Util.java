package com.loadtestgo.script.api;

import org.mozilla.javascript.NativeArray;

public interface Util {
    String date(String format, int daysOffset);

    int random(int max);

    String randomString();

    String randomString(String chars);

    String randomString(String chars, int len);

    String randomString(int len);

    Object randomElement(NativeArray array);
}
