package com.loadtestgo.script.api;

import org.mozilla.javascript.NativeArray;

public interface Util {
    String date(String format, int daysOffset);

    int random(int max);

    Object randomElement(NativeArray array);
}
