package com.loadtestgo.script.editor.swing.debug;

import com.loadtestgo.script.editor.swing.ConsolePanel;
import com.loadtestgo.script.editor.swing.ConsoleTextField;

public class DebugConsolePanel extends ConsolePanel implements DebuggerCallbacks {
    protected Debugger debugger;

    public DebugConsolePanel(Debugger debugger) {
        super();

        this.debugger = debugger;

        debugger.addGuiCallback(this);

        ConsoleTextField.CommandExecutor commandExecutor = new CommandExecutor();
        init(commandExecutor);

        consoleInputPane.setVisible(false);
    }

    @Override
    public void enterInterrupt(Debugger.StackFrame lastFrame, Throwable exception) {
    }

    @Override
    public void evalScriptStarted() {
        consoleInputPane.setVisible(false);
    }

    @Override
    public void evalScriptStopped(Throwable exception, boolean isEngineRunning) {
        consoleInputPane.setVisible(isEngineRunning);
    }

    @Override
    public void evalScriptContinue() {
        consoleInputPane.setVisible(false);
    }

    private class CommandExecutor implements ConsoleTextField.CommandExecutor {
        @Override
        public boolean stringIsCompilableUnit(String partialLine) {
            synchronized (resetScriptThreadLock) {
                return debugger.getJavaScriptEngine().stringIsCompilableUnit(partialLine);
            }
        }

        @Override
        public void evalSource(int i, String partialLine) {
            synchronized (resetScriptThreadLock) {
                debugger.evalSource(i, partialLine, DebugConsolePanel.this);
            }
        }

        @Override
        public void tabComplete(String source, int pos) {
            synchronized (resetScriptThreadLock) {
                debugger.tabComplete(source, pos, DebugConsolePanel.this);
            }
        }
    }
}
