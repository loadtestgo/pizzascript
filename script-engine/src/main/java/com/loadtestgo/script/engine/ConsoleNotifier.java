package com.loadtestgo.script.engine;

public interface ConsoleNotifier {
    void logInfo(String str);
    void logWarn(String str);
    void logError(String str);
}
