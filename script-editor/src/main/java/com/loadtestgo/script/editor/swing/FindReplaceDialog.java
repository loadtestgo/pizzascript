package com.loadtestgo.script.editor.swing;


import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rtextarea.SearchContext;
import org.fife.ui.rtextarea.SearchEngine;
import org.fife.ui.rtextarea.SearchResult;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class FindReplaceDialog extends CustomDialog implements ActionListener {
    private JTextField searchField;
    private JTextField replaceField;
    private JCheckBox regexCB;
    private JCheckBox matchCaseCB;
    private RSyntaxTextArea textArea;
    private FilePanel filePanel;

    public enum Mode {
        REPLACE,
        FIND
    }

    private enum Direction {
        FORWARD,
        BACK
    }

    public FindReplaceDialog(Frame parent, FilePanel filePanel, Mode mode) {
        super(parent);
        this.filePanel = filePanel;
        this.textArea = this.filePanel.getTextArea();

        JPanel panel = new JPanel(new BorderLayout());

        GroupLayout layout = new GroupLayout(panel);
        panel.setLayout(layout);
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(this);

        searchField = new JTextField(30);
        replaceField = new JTextField(30);

        JButton nextButton = new JButton();
        nextButton.addActionListener(this);
        getRootPane().setDefaultButton(nextButton);

        JButton prevButton = new JButton();
        prevButton.addActionListener(this);

        regexCB = new JCheckBox("Regex");
        matchCaseCB = new JCheckBox("Match Case");

        if (mode == Mode.FIND) {
            nextButton.setText("Find Next");
            nextButton.setActionCommand("FindNext");
            prevButton.setText("Find Previous");
            prevButton.setActionCommand("FindPrev");
            replaceField.setVisible(false);
        } else {
            nextButton.setText("Replace Next");
            nextButton.setActionCommand("ReplaceNext");
            prevButton.setText("Replace Previous");
            prevButton.setActionCommand("ReplacePrev");
            replaceField.setVisible(true);
        }

        layout.setHorizontalGroup(
            layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addComponent(searchField)
                        .addComponent(replaceField)
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
                        .addComponent(replaceField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
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
        if (mode == Mode.REPLACE) {
            setTitle("Replace");
        } else {
            setTitle("Find");
        }
        pack();
    }

    private void searchReplace(Mode mode, Direction direction) {
        SearchContext context = new SearchContext();
        String text = searchField.getText();
        if (text.length() == 0) {
            return;
        }

        context.setSearchFor(text);
        context.setMatchCase(matchCaseCB.isSelected());
        context.setRegularExpression(regexCB.isSelected());
        context.setSearchForward(direction == Direction.FORWARD);
        context.setWholeWord(false);

        SearchResult searchResult;

        if (mode == Mode.FIND) {
            searchResult = SearchEngine.find(textArea, context);
        } else {
            context.setReplaceWith(replaceField.getText());
            searchResult = SearchEngine.replace(textArea, context);
        }

        if (!searchResult.wasFound()) {
            if (direction == Direction.FORWARD) {
                textArea.setCaretPosition(0);
            } else {
                textArea.setCaretPosition(
                        textArea.getText().length());

            }

            if (mode == Mode.FIND) {
                searchResult = SearchEngine.find(textArea, context);
            } else {
                searchResult = SearchEngine.replace(textArea, context);
            }

            if (!searchResult.wasFound()) {
                JOptionPane.showMessageDialog(this,
                        String.format("Text \"%s\" not found in file", text));
            }
        }
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
            searchReplace(Mode.FIND, Direction.FORWARD);
        } else if (command.equals("FindPrev")) {
            searchReplace(Mode.FIND, Direction.BACK);
        } else if (command.equals("ReplaceNext")) {
            searchReplace(Mode.REPLACE, Direction.FORWARD);
        } else if (command.equals("ReplacePrev")) {
            searchReplace(Mode.REPLACE, Direction.BACK);
        }
    }
}
