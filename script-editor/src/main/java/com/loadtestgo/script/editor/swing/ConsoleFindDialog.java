package com.loadtestgo.script.editor.swing;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Document;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConsoleFindDialog extends CustomDialog implements ActionListener
{
    private JTextField searchField;
    private JCheckBox regexCB;
    private JCheckBox matchCaseCB;
    private ConsoleTextArea textArea;
    private ConsolePanel consolePanel;
    private int pos = 0;
    private int matchLen = 0;
    private DefaultHighlighter.DefaultHighlightPainter lastHighlight;

    private enum Direction {
        FORWARD,
        BACK
    }

    public ConsoleFindDialog(Frame parent, ConsolePanel consolePanel) {
        super(parent);
        this.consolePanel = consolePanel;
        this.textArea = this.consolePanel.getTextArea();

        JPanel panel = new JPanel(new BorderLayout());

        GroupLayout layout = new GroupLayout(panel);
        panel.setLayout(layout);
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(this);

        searchField = new JTextField(30);

        JButton nextButton = new JButton();
        nextButton.addActionListener(this);
        getRootPane().setDefaultButton(nextButton);

        JButton prevButton = new JButton();
        prevButton.addActionListener(this);

        regexCB = new JCheckBox("Regex");
        matchCaseCB = new JCheckBox("Match Case");

        nextButton.setText("Find Next");
        nextButton.setActionCommand("FindNext");
        prevButton.setText("Find Previous");
        prevButton.setActionCommand("FindPrev");

        layout.setHorizontalGroup(
            layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(searchField)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(regexCB)
                        .addComponent(matchCaseCB)))
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(prevButton, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(nextButton, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(cancelButton, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addComponent(searchField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
                        GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(regexCB)
                        .addComponent(matchCaseCB))
                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED,
                        GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGroup(layout.createSequentialGroup()
                    .addComponent(nextButton)
                    .addComponent(prevButton)
                    .addComponent(cancelButton))
        );

        setContentPane(panel);
        setTitle("Find");
        pack();

        addComponentListener(new ComponentListener() {
            @Override
            public void componentResized(ComponentEvent e) {
            }

            @Override
            public void componentMoved(ComponentEvent e) {
            }

            @Override
            public void componentShown(ComponentEvent e) {
            }

            public void componentHidden(ComponentEvent e) {
                textArea.getHighlighter().removeAllHighlights();
            }
        });
    }

    private void search(Direction direction) {
        String matchText = searchField.getText();
        if (matchText.length() == 0) {
            return;
        }

        if (lastHighlight != null) {
            textArea.getHighlighter().removeAllHighlights();
            lastHighlight = null;
        }

        try {
            boolean matchCase = matchCaseCB.isSelected();

            Pattern pattern = null;
            if (regexCB.isSelected()) {
                pattern = Pattern.compile(matchText, matchCase ? 0 : Pattern.CASE_INSENSITIVE);
            } else {
                pattern = Pattern.compile(matchText, Pattern.LITERAL | (matchCase ? 0 : Pattern.CASE_INSENSITIVE));
            }

            Document document = textArea.getDocument();

            if (!search(document, pattern, direction)) {
                matchLen = 0;
                if (direction == Direction.FORWARD) {
                    pos = 0;
                } else {
                    pos = document.getLength() - 1;
                }
                if (!search(document, pattern, direction)) {
                    matchLen = 0;
                    if (direction == Direction.FORWARD) {
                        pos = 0;
                    } else {
                        pos = document.getLength() - 1;
                    }
                    JOptionPane.showMessageDialog(this,
                        String.format("Text \"%s\" not found", matchText));
                }
            }
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    private boolean search(Document document, Pattern pattern, Direction direction) throws BadLocationException {
        String text = null;
        int start = 0;
        int end = 0;

        if (direction == Direction.FORWARD) {
            pos = pos + matchLen;
            if (pos >= document.getLength()) {
                pos = 0;
            }

            start = pos;
            end = (document.getLength() - 1) - pos;
        } else {
            if (pos >= document.getLength()) {
                pos = 0;
            }

            start = 0;
            end = pos;
        }

        text = document.getText(start, end);

        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            int matchStart = matcher.start();
            int matchEnd = matcher.end();
            if (direction == Direction.BACK) {
                while (matcher.find()) {
                    matchStart = matcher.start();
                    matchEnd = matcher.end();
                }
            }

            pos = start + matchStart;
            matchLen = matchEnd - matchStart;

            Rectangle viewRect = textArea.modelToView(pos);
            textArea.scrollRectToVisible(viewRect);

            lastHighlight = new DefaultHighlighter.DefaultHighlightPainter(Color.LIGHT_GRAY);
            textArea.getHighlighter().addHighlight(pos, pos + matchLen, lastHighlight);
            return true;
        }

        return false;
    }

    public void showDialog(Component comp) {
        setLocationRelativeTo(comp);
        setVisible(true);
    }

    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
        if (command.equals("Cancel")) {
            close();
        } else if (command.equals("FindNext")) {
            search(Direction.FORWARD);
        } else if (command.equals("FindPrev")) {
            search(Direction.BACK);
        }
    }
}
