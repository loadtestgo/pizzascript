package com.loadtestgo.script.editor.swing.debug;

import com.loadtestgo.script.editor.swing.ConsoleCallbacks;
import com.loadtestgo.script.engine.JavaScriptEngine;
import com.loadtestgo.script.engine.ScriptException;

import java.util.concurrent.Callable;

public class EvalTask implements Callable<Object> {
    private JavaScriptEngine engine;
    private String source;
    private int lineNo;
    private ConsoleCallbacks callbacks;

    public EvalTask(JavaScriptEngine engine,
                    int lineNo,
                    String source,
                    ConsoleCallbacks callbacks) {
        this.engine = engine;
        this.lineNo = lineNo;
        this.source = source;
        this.callbacks = callbacks;
    }

    @Override
    public Object call() throws Exception {
        Object result = null;
        try {
            result = engine.runPartialScript(source, lineNo);
            callbacks.expressionResult(engine.valueToString(result));
        } catch (ScriptException se) {
            callbacks.scriptException(se);
        }

        return result;
    }
}
