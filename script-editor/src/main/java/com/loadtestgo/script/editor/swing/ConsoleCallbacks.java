package com.loadtestgo.script.editor.swing;

import com.loadtestgo.script.engine.JavaScriptEngine;
import com.loadtestgo.script.engine.ScriptException;

import java.util.ArrayList;

public interface ConsoleCallbacks {
    void autoCompletions(String source,          // The original line
                         int completionStartPos, // Start point for adding completions
                         int insertPos,          // The original cursor position
                         ArrayList<JavaScriptEngine.CompletionGroup> completions);

    void scriptException(ScriptException exception);

    void expressionResult(String result);
}
