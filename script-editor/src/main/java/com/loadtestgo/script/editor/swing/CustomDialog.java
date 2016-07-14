package com.loadtestgo.script.editor.swing;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

public class CustomDialog extends JDialog {
    CustomDialog(Frame parent) {
        super(parent);
        addEscapeListener();
    }

    public void close() {
        setVisible(false);
    }

    private void addEscapeListener() {
        ActionListener escListener = new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                close();
            }
        };

        getRootPane().registerKeyboardAction(escListener,
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);
    }
}
