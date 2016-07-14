package com.loadtestgo.script.editor;

import com.loadtestgo.script.editor.swing.Debugger;
import com.loadtestgo.script.editor.swing.MainWindow;
import javafx.application.Platform;

import javax.swing.*;

public class Gui {
    private Debugger debugger;
    private MainWindow mainWindow;

    public Gui() {
        init();
    }

    private void init() {
        // Don't allow JavaFX to unload
        Platform.setImplicitExit(false);

        debugger = new Debugger();
        mainWindow = new MainWindow(PizzaScript.AppName, debugger);
        mainWindow.pack();

        mainWindow.setSize(900, 800);
        mainWindow.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        mainWindow.setTabDefaultFocus();
        mainWindow.setVisible(true);
    }

    public void openFile(String fileName) {
        mainWindow.openFile(fileName);
    }
}
