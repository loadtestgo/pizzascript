package com.loadtestgo.script.editor.swing.debug;

public interface DebuggerCallbacks {
    void enterInterrupt(Debugger.StackFrame lastFrame, Throwable exception);

    void evalScriptStarted();

    void evalScriptStopped(Throwable exception, boolean isEngineRunning);

    void evalScriptContinue();
}
