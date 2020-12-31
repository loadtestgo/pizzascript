package com.loadtestgo.script.engine.internal;

public class FormatUtils {
    public static String formatTimeout(long timeoutMS) {
        String timeoutStr;
        if (timeoutMS > 1000) {
            timeoutStr = String.format("%ds", timeoutMS);
        } else {
            timeoutStr = String.format("%dms", timeoutMS);
        }
        return timeoutStr;
    }
}
