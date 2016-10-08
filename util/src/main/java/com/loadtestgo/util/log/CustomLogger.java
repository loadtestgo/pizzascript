package com.loadtestgo.util.log;

public interface CustomLogger {
    void info(final String message, final Object... arguments);

    void warn(final String message, final Object... arguments);
}
