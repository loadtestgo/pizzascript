package com.loadtestgo.script.runner;

public class MainLog {
    public static void error(final String message) {
        System.err.println(message);
        org.pmw.tinylog.Logger.error(message);
    }

    public static void logInfo() {
        logInfo("");
    }

    public static void logInfo(String message) {
        System.out.println(message);
        org.pmw.tinylog.Logger.info(message);
    }

    public static void logError(String message) {
        System.err.println(message);
        org.pmw.tinylog.Logger.error(message);
    }
}
