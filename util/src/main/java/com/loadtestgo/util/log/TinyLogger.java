package com.loadtestgo.util.log;

import org.pmw.tinylog.Level;
import org.pmw.tinylog.LogEntryForwarder;

public class TinyLogger implements CustomLogger {
    @Override
    public void info(final String message, final Object... arguments) {
        LogEntryForwarder.forward(4, Level.INFO, null, message, arguments);
    }

    @Override
    public void warn(final String message, final Object... arguments) {
        LogEntryForwarder.forward(4, Level.WARNING, null, message, arguments);
    }
}
