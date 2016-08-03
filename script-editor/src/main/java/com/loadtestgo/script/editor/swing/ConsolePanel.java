package com.loadtestgo.script.editor.swing;

import com.loadtestgo.script.api.TestResult;
import com.loadtestgo.script.editor.PizzaScript;
import com.loadtestgo.script.engine.EngineSettings;
import com.loadtestgo.script.engine.JavaScriptEngine;
import com.loadtestgo.script.engine.ScriptException;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ConsolePanel extends JPanel implements ConsoleCallbacks {
    private ConsoleTextArea consoleTextArea;
    private ConsoleTextField consoleTextField;
    private ConsoleScriptThread scriptThread;
    private final Object resetScriptThreadLock = new Object();
    private EditorTestContext.WindowPosition windowPosition;

    public ConsolePanel() {
        super();

        setFocusTraversalKeysEnabled(false);

        consoleTextArea = new ConsoleTextArea();
        consoleTextArea.setEditable(false);

        printGreeting();

        scriptThread = newScriptThread();

        JLabel promptLabel = new JLabel();
        promptLabel.setFont(EditorSettings.getCodeFont());
        promptLabel.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 0));

        consoleTextField = new ConsoleTextField(new CommandExecutor(), consoleTextArea, promptLabel);
        consoleTextField.setFocusTraversalKeysEnabled(false);
        JScrollPane consoleInputScroll = new JScrollPane(consoleTextField,
            ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER,
            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        consoleInputScroll.getHorizontalScrollBar().setPreferredSize(new Dimension(0,0));
        consoleInputScroll.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        consoleTextField.setScrollPane(consoleInputScroll);

        JPanel consoleInputPane = new JPanel();
        consoleTextField.setResizeParent(consoleInputPane);
        BoxLayout layout = new BoxLayout(consoleInputPane, BoxLayout.X_AXIS);
        consoleInputPane.setLayout(layout);
        consoleInputPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        consoleInputPane.add(promptLabel);
        consoleInputPane.add(consoleInputScroll);

        consoleTextField.syncHeight();

        JScrollPane scrollPane = new JScrollPane(consoleTextArea);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        add(scrollPane);
        add(consoleInputPane);
        consoleTextArea.setScrollPane(scrollPane);

        consoleTextField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                consoleTextField.syncHeight();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                consoleTextField.syncHeight();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                consoleTextField.syncHeight();
            }
        });
    }

    private void printGreeting() {
        consoleTextArea.println(
                String.format("Welcome to %s Interactive Console %s!",
                        PizzaScript.AppName,
                        EngineSettings.getVersion()),
                "#800000");
        consoleTextArea.println(
                "Type JavaScript to evaluate or open a new window in the File menu.",
                "darkGray");
    }

    public ConsoleTextArea getTextArea() {
        return consoleTextArea;
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
    public void autoCompletions(String source,
                                int completionStartPos,
                                int insertPos,
                                ArrayList<JavaScriptEngine.CompletionGroup> completions) {
        if (completions.size() == 0) {
            return;
        } else {
            final ArrayList<JavaScriptEngine.CompletionGroup> finalCompletions = completions;
            final String finalSource = source;
            final int finalPos = completionStartPos;
            final int finalInsertPos = insertPos;
            final String finalResult = longestMatchingString(completions);

            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    if (!consoleTextField.autoComplete(finalSource, finalPos, finalInsertPos, finalResult)) {
                        displayCompletions(finalSource, finalCompletions);
                    }
                }
            });
        }
    }

    private String longestMatchingString(ArrayList<JavaScriptEngine.CompletionGroup> completions) {
        String match = null;
        for (JavaScriptEngine.CompletionGroup completionGroup : completions) {
            for (String chars : completionGroup.completions) {
                if (match == null) {
                    match = chars;
                } else {
                    if (chars.length() < match.length()) {
                        for (int i = 0; i < chars.length(); ++i) {
                            if (chars.charAt(i) != match.charAt(i)) {
                                if (i == 0) {
                                    return null;
                                } else {
                                    match = chars.substring(0, i);
                                }
                                break;
                            }
                        }
                    } else {
                        for (int i = 0; i < match.length(); ++i) {
                            if (match.charAt(i) != chars.charAt(i)) {
                                if (i == 0) {
                                    return null;
                                } else {
                                    match = match.substring(0, i);
                                }
                                break;
                            }
                        }
                    }
                    if (match.length() == 0) {
                        return null;
                    }
                }
            }
        }

        return match;
    }

    private void displayCompletions(String source, ArrayList<JavaScriptEngine.CompletionGroup> completions) {
        int columns = consoleTextArea.getColumns();
        StringBuilder buffer = new StringBuilder();

        boolean first = true;
        for (JavaScriptEngine.CompletionGroup group : completions) {
            if (completions.size() > 1) {
                if (!first) {
                    buffer.append("\n");
                }
                buffer.append("=== ");
                buffer.append(group.name);
                buffer.append(" ===\n");
            }

            int maxWidth = 0;
            for (String str : group.completions) {
                if (str.length() > maxWidth) {
                    maxWidth = str.length();
                }
            }
            maxWidth += 1;
            int numColumns = columns/maxWidth;
            if (numColumns == 0) {
                numColumns = 1;
            }

            for (int i = 0; i < group.completions.size(); ++i) {
                String str = group.completions.get(i);
                int padding = maxWidth - str.length();
                buffer.append(str);
                for (int k = 0; k < padding; ++k) {
                    buffer.append(" ");
                }
                if (((i + 1) % numColumns) == 0 &&
                        ((i + 1) < group.completions.size())) {
                    buffer.append("\n");
                }
            }
            first = false;
        }
        consoleTextArea.printCommand(PizzaScript.PromptCompleteLine, source);
        consoleTextArea.println(buffer.toString(), "#5050FF");
    }

    @Override
    public void scriptException(ScriptException exception) {
        final String message = exception.getMessage();
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                consoleTextArea.println(message, "#d04070");
            }
        });
    }

    @Override
    public void expressionResult(String result) {
        final String finalResult = result;
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                consoleTextArea.println(finalResult, "#40A040");
            }
        });
    }

    public void setSnippet(String text) {
        consoleTextField.setText(text);
    }

    public String getHistory() {
        StringBuilder buffer = new StringBuilder();
        List<String> history = consoleTextField.getHistory();
        for (String item : history) {
            buffer.append(item);
            buffer.append("\n");
        }
        return buffer.toString();
    }

    public void setDefaultFocus() {
        consoleTextField.requestFocusInWindow();
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

    private ConsoleScriptThread newScriptThread() {
        ConsoleScriptThread scriptThread = new ConsoleScriptThread(this, consoleTextArea.getConsoleOut());
        scriptThread.setWindowPosition(windowPosition);
        return scriptThread;
    }

    public void setWindowPosition(EditorTestContext.WindowPosition windowPosition) {
        synchronized (resetScriptThreadLock) {
            this.windowPosition = windowPosition;
            scriptThread.setWindowPosition(windowPosition);
        }
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
