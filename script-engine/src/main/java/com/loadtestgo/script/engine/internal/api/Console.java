package com.loadtestgo.script.engine.internal.api;

import com.loadtestgo.script.api.TestResult;
import com.loadtestgo.script.engine.ConsoleNotifier;
import com.loadtestgo.util.StringUtils;

import java.io.PrintStream;

public class Console {
    ConsoleNotifier consoleNotifier;
    TestResult testResult;

    public Console(TestResult testResult) {
        this.consoleNotifier = new DefaultOutput();
        this.testResult = testResult;
    }

    public void log(String str) {
        consoleNotifier.logInfo(str);
        testResult.addOutput(str);
    }

    public void log(String ...str) {
        String concat = StringUtils.join(" ", str);
        consoleNotifier.logInfo(concat);
        testResult.addOutput(concat);
    }

    public void info(String str) {
        log(str);
    }

    public void warn(String str) {
        consoleNotifier.logWarn(str);
    }

    public void error(String str) {
        consoleNotifier.logError(str);
    }

    @Override
    public String toString() {
        return "Console";
    }

    public void setOut(ConsoleNotifier consoleNotifier) {
        this.consoleNotifier = consoleNotifier;
    }

    class DefaultOutput implements ConsoleNotifier {
        private PrintStream out;
        private PrintStream err;

        DefaultOutput() {
            out = System.out;
            err = System.err;
        }

        @Override
        public void logInfo(String str) {
            out.println(str);
        }

        @Override
        public void logWarn(String str) {
            out.println(str);
        }

        @Override
        public void logError(String str) {
            err.println(str);
        }
    }
}
