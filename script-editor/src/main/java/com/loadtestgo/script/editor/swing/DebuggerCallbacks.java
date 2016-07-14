package com.loadtestgo.script.editor.swing;

public interface DebuggerCallbacks {
    void enterInterrupt(Debugger.StackFrame lastFrame, Throwable exception);

    void evalScriptStarted();

    void evalScriptStopped(Throwable exception);

    void evalScriptContinue();
}
