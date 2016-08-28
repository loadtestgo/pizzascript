package com.loadtestgo.script.engine.internal.api;

import com.loadtestgo.script.api.TestResult;
import com.loadtestgo.script.engine.ConsoleNotifier;
import com.loadtestgo.script.engine.JavaScriptEngine;
import com.loadtestgo.util.StringUtils;
import org.mozilla.javascript.NativeObject;

import java.io.PrintStream;

public class Console {
    ConsoleNotifier consoleNotifier;
    TestResult testResult;
    JavaScriptEngine engine;

    public Console(TestResult testResult,
                   JavaScriptEngine engine) {
        this.consoleNotifier = new DefaultOutput();
        this.testResult = testResult;
        this.engine = engine;
    }

    public void log(String str) {
        consoleNotifier.logInfo(str);
        testResult.addOutput(str);
    }

    public void log(Object obj) {
        String str = engine.valueToString(obj);
        consoleNotifier.logInfo(str);
        testResult.addOutput(str);
    }

    public void log(Object... objs) {
        String concat = "";
        int i = 0;
        for (Object obj : objs) {
            String str = engine.valueToString(obj);
            if (i > 0) {
                concat += ", ";
            }
            concat += str;
            i++;
        }
        consoleNotifier.logInfo(concat);
        testResult.addOutput(concat);
    }

    public void info(Object str) {
        log(str);
    }

    public void warn(Object obj) {
        String str = engine.valueToString(obj);
        consoleNotifier.logWarn(str);
    }

    public void error(Object obj) {
        String str = engine.valueToString(obj);
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
