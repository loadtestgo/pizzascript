package com.loadtestgo.script.editor;

import com.loadtestgo.script.engine.JavaScriptEngine;
import jline.console.completer.Completer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class JavaScriptCompleter implements Completer {
    private JavaScriptEngine engine;

    JavaScriptCompleter(JavaScriptEngine engine)
    {
        this.engine = engine;
    }

    @Override
    public int complete(String buffer, int cursor, List<CharSequence> candidates) {
        List<JavaScriptEngine.CompletionGroup> completions = new ArrayList<>();
        int completionAmount = engine.complete(buffer, cursor, completions);

        // Add all the completions to one list and sort
        List<String> fullList = new ArrayList<>();
        for (JavaScriptEngine.CompletionGroup group : completions) {
            for (String name : group.completions) {
                if (!fullList.contains(name)) {
                    fullList.add(name);
                }
            }
        }

        Collections.sort(fullList);
        for (String name : fullList) {
            candidates.add(name);
        }

        return completionAmount;
    }
}
