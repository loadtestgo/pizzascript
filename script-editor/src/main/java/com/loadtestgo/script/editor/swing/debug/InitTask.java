package com.loadtestgo.script.editor.swing.debug;

import com.loadtestgo.script.editor.swing.ConsoleOutputStream;
import com.loadtestgo.script.engine.JavaScriptEngine;
import com.loadtestgo.script.engine.TestContext;

import java.util.concurrent.Callable;

public class InitTask implements Callable<Object> {
    private JavaScriptEngine engine;
    private TestContext testContext;
    private ConsoleOutputStream output;

    public InitTask(JavaScriptEngine engine, TestContext testContext, ConsoleOutputStream output) {
        this.engine = engine;
        this.testContext = testContext;
        this.output = output;
    }

    @Override
    public Object call() throws Exception {
        engine.setConsole(output);
        engine.init(testContext);
        return null;
    }
}
