package com.loadtestgo.script.editor.swing.debug;

import com.loadtestgo.script.editor.swing.ConsoleCallbacks;
import com.loadtestgo.script.engine.JavaScriptEngine;

import java.util.ArrayList;
import java.util.concurrent.Callable;

public class TabCompleteTask implements Callable<Object> {
    private JavaScriptEngine engine;
    private String source;
    private int insertPos;
    private ConsoleCallbacks callbacks;

    public TabCompleteTask(JavaScriptEngine engine,
                           String source,
                           int insertPos,
                           ConsoleCallbacks callbacks) {
        this.engine = engine;
        this.source = source;
        this.insertPos = insertPos;
        this.callbacks = callbacks;
    }

    @Override
    public Object call() throws Exception {
        ArrayList<JavaScriptEngine.CompletionGroup> completions = new ArrayList<>();
        int pos = engine.complete(source, insertPos, completions);
        callbacks.autoCompletions(source, pos, insertPos, completions);
        return null;
    }
}