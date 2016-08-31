package com.loadtestgo.script.editor.swing;

import com.loadtestgo.util.Path;
import org.fife.rsta.ac.LanguageSupportFactory;
import org.fife.ui.rsyntaxtextarea.RSyntaxDocument;
import org.fife.ui.rsyntaxtextarea.parser.AbstractParser;
import org.fife.ui.rsyntaxtextarea.parser.DefaultParseResult;
import org.fife.ui.rsyntaxtextarea.parser.DefaultParserNotice;
import org.fife.ui.rsyntaxtextarea.parser.ParseResult;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import java.awt.*;

public class FilePanel extends PanelWithHeader {
    private MainWindow mainWindow;
    private SourceFile sourceFile;
    private FileTextArea textArea;
    private FileLineNumbers fileLineNumbers;
    private JScrollPane pane;
    private int currentPos;
    private boolean showDebugFrame;
    private JLabel tabLabel;
    private JPanel tabHeader;

    public FilePanel(MainWindow mainWindow, CodeModel codeModel, SourceFile sourceFile) {
        this.mainWindow = mainWindow;
        this.sourceFile = sourceFile;
        this.currentPos = -1;
        this.textArea = new FileTextArea(sourceFile.getSource());
        this.textArea.setFocusTraversalKeysEnabled(false);
        this.textArea.setColumns(80);
        this.pane = new JScrollPane();

        LanguageSupportFactory.get().register(textArea);

        // TODO: Add auto completion for our pizzascript.js
        //
        // RhinoJavaScriptLanguageSupport support1 = new RhinoJavaScriptLanguageSupport();
        // support1.install(textArea);

        this.fileLineNumbers = new FileLineNumbers(this);
        this.pane.setViewportView(this.textArea);
        this.pane.setRowHeaderView(this.fileLineNumbers);

        Document document = this.textArea.getDocument();
        document.addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                updateBreakpoints(e);
                updateLineNumbers();
                syncSourceFile();
                updateUndo();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateBreakpoints(e);
                updateLineNumbers();
                syncSourceFile();
                updateUndo();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updateBreakpoints(e);
                updateLineNumbers();
                syncSourceFile();
                updateUndo();
            }
        });

        setLayout(new GridLayout(1, 1));

        this.textArea.addParser(new SyntaxParser(sourceFile, codeModel));

        add(this.pane);
    }

    private void updateBreakpoints(DocumentEvent e) {
        Document doc = textArea.getDocument();
        Element lineMap = doc.getDefaultRootElement();
        DocumentEvent.ElementChange change = e.getChange(lineMap);
        if (change != null ){
            Element[] added = change.getChildrenAdded();
            Element[] removed = change.getChildrenRemoved();
            int lineStart = lineMap.getElementIndex(e.getOffset());
            int numAdded = 0;
            int numRemove = 0;
            if (added != null) {
                numAdded = added.length;
            }
            if (removed != null) {
                numRemove = removed.length;
            }
            int changed = numAdded - numRemove;
            sourceFile.moveBreakpoints(lineStart + 1, changed);
        }
    }

    private void updateUndo() {
        mainWindow.updateUndoState();
    }

    public void toggleBreakPoint(int line) {
        if (!sourceFile.isBreakpoint(line)) {
            setBreakpoint(line);
        } else {
            clearBreakpoint(line);
        }
    }

    public void toggleBreakPoint() {
        try {
            int caretPos = textArea.getCaretPosition();
            int lineNum = textArea.getLineOfOffset(caretPos);
            toggleBreakPoint(lineNum + 1);
        } catch (BadLocationException e) {
            // Do nothing
        }
    }

    public void setBreakpoint(int line) {
        boolean changed = sourceFile.setBreakpoint(line, true);
        if (changed) {
            fileLineNumbers.repaint();
        }
    }

    public void clearBreakpoint(int line) {
        boolean changed = sourceFile.setBreakpoint(line, false);
        if (changed) {
            fileLineNumbers.repaint();
        }
    }

    public String getFilePath() {
        return sourceFile.getFilePath();
    }

    public synchronized void setPosition(int pos) {
        textArea.selectLine(pos);
        currentPos = pos;
        fileLineNumbers.repaint();
    }

    public FileTextArea getTextArea() {
        return textArea;
    }

    public synchronized int getCurrentPos()  {
        return currentPos;
    }

    public void undo() {
        textArea.undoLastAction();
    }

    public void redo() {
        textArea.redoLastAction();
    }

    public boolean canUndo() {
        return textArea.canUndo();
    }

    public boolean canRedo() {
        return textArea.canRedo();
    }

    private void updateLineNumbers()
    {
        fileLineNumbers.update();
        fileLineNumbers.repaint();
    }

    public SourceFile getSourceFile() {
        return sourceFile;
    }

    public void syncSourceFile() {
        sourceFile.update(textArea.getText());
        if (!sourceFile.isModified()) {
            sourceFile.setIsModified(true);
            updateUIFromFileName();
        }
    }

    public boolean showDebugFrame() {
        return showDebugFrame;
    }

    public void setShowDebugFrame(boolean showDebugFrame) {
        this.showDebugFrame = showDebugFrame;
    }

    public void setTabHeader(JLabel tabLabel, JPanel tabHeader) {
        this.tabLabel = tabLabel;
        this.tabHeader = tabHeader;
        updateUIFromFileName();
    }

    public void updateUIFromFileName() {
        String fileLabel = Path.getFileName(sourceFile.getFilePath());
        if (sourceFile.isModified()) {
            fileLabel += " *";
        }

        if (tabLabel != null) {
            tabLabel.setText(fileLabel);
        }

        if (tabHeader != null) {
            tabHeader.setToolTipText(sourceFile.getFilePath());
        }
    }

    public void selectLine(int lineNumber) {
        if (lineNumber <= 0) {
            return;
        }

        try {
            int start = textArea.getLineStartOffset(lineNumber - 1);
            int end = textArea.getLineEndOffset(lineNumber - 1);
            textArea.setCaretPosition(end);
            textArea.moveCaretPosition(start);
        } catch (javax.swing.text.BadLocationException ignore) {
        }
    }

    public void setDefaultFocus() {
        textArea.requestFocusInWindow();
    }

    /**
     * This reports errors and updates the internal code model that we use to
     * allow breakpoints to be set on lines that have code on them.
     */
    private class SyntaxParser extends AbstractParser
    {
        private SourceFile sourceFile;
        private CodeModel codeModel;

        SyntaxParser(SourceFile sourceFile,
                     CodeModel codeModel) {
            this.sourceFile = sourceFile;
            this.codeModel = codeModel;
        }

        @Override
        public ParseResult parse(RSyntaxDocument doc, String style) {
            DefaultParseResult result = new DefaultParseResult(this);
            CodeModel.ErrorMessage errorMessage = codeModel.compileScript(sourceFile);
            if (errorMessage != null) {
                int line = errorMessage.line - 1;
                try {
                    DefaultParserNotice notice = new DefaultParserNotice(this,
                            errorMessage.message,
                            line,
                            textArea.getLineStartOffset(line),
                            textArea.getLineEndOffset(line));
                    result.addNotice(notice);
                } catch (BadLocationException e) {
                    // Do nothing
                }
            }
            return result;
        }
    }
}
