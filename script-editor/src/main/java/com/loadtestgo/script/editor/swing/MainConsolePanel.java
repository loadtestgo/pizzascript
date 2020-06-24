package com.loadtestgo.script.editor.swing;

import com.loadtestgo.script.api.TestResult;
import com.loadtestgo.script.editor.PizzaScript;

public class MainConsolePanel extends ConsolePanel {
    protected ConsoleScriptThread scriptThread;

    public MainConsolePanel() {
        super();

        scriptThread = newScriptThread();
        ConsoleTextField.CommandExecutor commandExecutor = new CommandExecutor();

        init(commandExecutor);
    }

    public void close() {
        synchronized (resetScriptThreadLock) {
            scriptThread.shutdown();
        }
    }

    public void reset() {
        synchronized (resetScriptThreadLock) {
            scriptThread.shutdown();

            consoleTextArea.clear();

            printGreeting();

            scriptThread = newScriptThread();
        }
    }

    public void setWindowPosition(EditorTestContext.WindowPosition windowPosition) {
        synchronized (resetScriptThreadLock) {
            this.windowPosition = windowPosition;
            scriptThread.setWindowPosition(windowPosition);
        }
    }

    public TestResult getTestResult() {
        synchronized (resetScriptThreadLock) {
            return scriptThread.getTestContext().getTestResult();
        }
    }

    public ConsoleScriptThread getScriptThread() {
        return scriptThread;
    }

    @Override
    protected void printGreeting() {
        consoleTextArea.println(
                String.format("Welcome to %s Interactive Console %s!",
                        PizzaScript.AppName,
                        PizzaScript.getVersion()),
                "#800000");
        consoleTextArea.println(
                "Type JavaScript to evaluate or open a new window in the File menu.",
                "darkGray");
    }

    private class CommandExecutor implements ConsoleTextField.CommandExecutor {
        @Override
        public boolean stringIsCompilableUnit(String partialLine) {
            synchronized (resetScriptThreadLock) {
                return scriptThread.getJavaScriptEngine().stringIsCompilableUnit(partialLine);
            }
        }

        @Override
        public void evalSource(int i, String partialLine) {
            synchronized (resetScriptThreadLock) {
                scriptThread.evalSource(i, partialLine);
            }
        }

        @Override
        public void tabComplete(String source, int pos) {
            synchronized (resetScriptThreadLock) {
                scriptThread.tabComplete(source, pos);
            }
        }
    }
}
