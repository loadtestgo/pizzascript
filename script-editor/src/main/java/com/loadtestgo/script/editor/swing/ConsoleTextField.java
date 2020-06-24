package com.loadtestgo.script.editor.swing;

import com.loadtestgo.script.editor.PizzaScript;
import com.loadtestgo.script.engine.JavaScriptEngine;
import com.loadtestgo.util.Os;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.List;

public class ConsoleTextField extends RSyntaxTextArea implements KeyListener {
    private CommandExecutor commandExecutor;
    private java.util.List<String> history;
    private int historyIndex = -1;
    private String lineWas;
    private boolean bContinueReading = false;
    private int startLine = 0;
    private String partialLine = "";
    private JLabel promptText;
    private ConsoleTextArea textArea;
    private boolean multiLineMode = false;
    private int prevHeight = 0;
    private JScrollPane scrollPane;
    private JPanel resizeParent;

    public interface CommandExecutor {
        boolean stringIsCompilableUnit(String partialLine);

        void evalSource(int i, String partialLine);

        void tabComplete(String source, int pos);
    }

    public ConsoleTextField(CommandExecutor commandExecutor,
                            ConsoleTextArea textArea,
                            JLabel promptText)
    {
        setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVASCRIPT);
        setAntiAliasingEnabled(true);
        setHighlightCurrentLine(false);
        discardAllEdits();
        setFont(EditorSettings.getCodeFont());

        this.history = new ArrayList<>();
        this.lineWas = "";
        this.commandExecutor = commandExecutor;
        this.promptText = promptText;
        this.textArea = textArea;
        addKeyListener(this);
        setFont(EditorSettings.getCodeFont());
        setInput("");
    }

    public void syncHeight() {
        int height = getPreferredSize().height;
        if (prevHeight == height) {
            return;
        }
        prevHeight = height;
        int rowHeight = getRowHeight();
        Insets margin = getMargin();
        if (margin != null) {
            rowHeight += margin.top + margin.bottom;
        }
        multiLineMode = height > rowHeight;

        Dimension dimension = new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
        if (scrollPane != null) {
            scrollPane.setMaximumSize(dimension);
            scrollPane.setMinimumSize(dimension);
            resizeParent.revalidate();
        }
        setMaximumSize(dimension);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        switch (code) {
            case KeyEvent.VK_HOME:
                int caretPos = getCaretPosition();
                if (caretPos == 0) {
                    e.consume();
                } else if (caretPos > 0) {
                    if (!e.isControlDown()) {
                        if (e.isShiftDown()) {
                            moveCaretPosition(0);
                        } else {
                            setCaretPosition(0);
                        }
                        e.consume();
                    }
                }
                break;
            case KeyEvent.VK_A:
                // On OSX only, otherwise Ctrl-A is select all
                if (e.isControlDown() && !e.isShiftDown() && Os.isMac()) {
                    setCaretPosition(0);
                    e.consume();
                }
                break;
            case KeyEvent.VK_E:
                if (e.isControlDown()) {
                    deselectAndMoveCaretToEnd();
                    e.consume();
                }
                break;
            case KeyEvent.VK_UP:
                if (!multiLineMode) {
                    if (historyIndex == history.size()) {
                        lineWas = getText();
                    }
                    if (historyIndex > 0) {
                        historyIndex--;
                        setInput(history.get(historyIndex));
                        deselectAndMoveCaretToEnd();
                    }
                    e.consume();
                }
                break;
            case KeyEvent.VK_DOWN:
                if (!multiLineMode) {
                    if (historyIndex + 1 < history.size()) {
                        historyIndex++;
                        setInput(history.get(historyIndex));
                        deselectAndMoveCaretToEnd();
                    } else {
                        historyIndex = history.size();
                        setInput(lineWas);
                        deselectAndMoveCaretToEnd();
                    }
                    e.consume();
                }
                break;
            case KeyEvent.VK_TAB:
                if (!e.isControlDown()) {
                    tabComplete(getText(), getCaretPosition());
                    e.consume();
                }
                break;
            case KeyEvent.VK_ENTER:
                returnPressed();
                e.consume();
                break;
        }
    }

    private void returnPressed() {
        String input = getText();
        textArea.printCommand(getPrompt(), input);

        int lineNo = history.size() + 1;
        partialLine = partialLine + input + "\n";

        bContinueReading = !commandExecutor.stringIsCompilableUnit(partialLine);
        if (!bContinueReading) {
            commandExecutor.evalSource(startLine + 1, partialLine);
            startLine = lineNo;
            partialLine = "";
        }

        setInput("");
        if (input.length() > 0) {
            history.add(input);
        }
        lineWas = "";
        historyIndex = history.size();
    }

    private void deselectAndMoveCaretToEnd() {
        int end = getText().length();
        select(end, end);
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    private void setInput(String str) {
        promptText.setText(getPrompt());

        if (str != null) {
            setText(str);
        }
    }

    private String getPrompt() {
        return (bContinueReading) ? PizzaScript.PromptContinueLine : PizzaScript.PromptNewLine;
    }

    private void tabComplete(String source, int pos) {
        commandExecutor.tabComplete(source, pos);
    }

    public boolean autoComplete(String source, int completionStartPos, int insertPos, String completion) {
        if (completion == null) {
            return false;
        }

        // If it's a function remove the last ')'
        if (completion.endsWith(")")) {
            completion = completion.substring(0, completion.length() - 1);
        }

        // Find the amount of completion we need to do
        int completionStart = insertPos - completionStartPos;
        if (completionStart > 0 && completion.length() > completionStart) {

            // Add the completion to the existing string
            String text = source.substring(0, insertPos) + completion.substring(completionStart);

            // Get the new cursor position
            int newCursorPos = text.length();

            // If we are doing a completion in the middle of an existing string
            if (source.length() > insertPos) {
                String rest = source.substring(insertPos);
                if (rest.startsWith("(")) {
                    text += rest.substring(1);
                } else {
                    text += rest;
                }
            }

            // If the new string is different from the old one update it
            if (!text.equals(source)) {
                setText(text);
                setCaretPosition(newCursorPos);
            }
        }

        return (completionStart >= 0 && (completionStart < completion.length()));
    }

    public void setScrollPane(JScrollPane scrollPane) {
        this.scrollPane = scrollPane;
    }

    public void setResizeParent(JPanel resizeParent) {
        this.resizeParent = resizeParent;
    }

    public List<String> getHistory() {
        return history;
    }
}
