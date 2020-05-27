package com.loadtestgo.script.api;

import org.mozilla.javascript.NativeArray;

import java.util.Date;

public interface Util {
    String date(String format);

    String date(String format, int daysOffset);

    String dateTime(String format);

    String dateTime(String format, Date date);

    int random(int max);

    String randomString();

    String randomString(String chars);

    String randomString(String chars, int len);

    String randomString(int len);

    Object randomElement(NativeArray array);
}
