package com.loadtestgo.script.editor;

import com.loadtestgo.script.editor.swing.debug.Debugger;
import com.loadtestgo.script.editor.swing.MainWindow;
import javafx.application.Platform;
import org.pmw.tinylog.Logger;

import javax.swing.*;

public class Gui {
    private Debugger debugger;
    private MainWindow mainWindow;
    private static boolean isJavaFXInstalled = false;

    static {
        checkJavaFXInstalled();
    }

    public Gui() {
        init();
    }

    private void init() {
        // Don't allow JavaFX to unload
        if (javaFXInstalled()) {
            Platform.setImplicitExit(false);
        }

        debugger = new Debugger();
        mainWindow = new MainWindow(PizzaScript.AppName, debugger);
        mainWindow.pack();

        mainWindow.setSize(900, 800);
        mainWindow.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        mainWindow.setTabDefaultFocus();
        mainWindow.setVisible(true);
    }

    private static void checkJavaFXInstalled() {
        try {
            Class.forName("javafx.application.Platform");
            // it exists on the classpath
            isJavaFXInstalled = true;
        } catch(ClassNotFoundException e) {
            // it does not exist on the classpath
            isJavaFXInstalled = false;

            Logger.info("JavaFX is not in the classpath.  Some features of the editor will not be available.");

            String vmName = System.getProperty("java.vm.name");
            if (vmName != null && vmName.startsWith("OpenJDK")) {
                Logger.info("Try installing OpenJFX via your package manager.");
            }
        }
    }

    public static boolean javaFXInstalled() {
        return isJavaFXInstalled;
    }

    public void openFile(String fileName) {
        mainWindow.openFile(fileName, false);
    }
}
