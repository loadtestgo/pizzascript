package com.loadtestgo.script.editor.swing;

import com.loadtestgo.script.engine.EngineSettings;

import java.awt.*;

public class EditorSettings {
    static private Font codeFont;

    static {
        codeFont = new Font("Monospaced", 0, 12);
    }

    public static Font getCodeFont() {
        return codeFont;
    }

    public static int getStackFrameCharWidth() {
        return 50;
    }

    public static boolean sideBySideBrowserWindow() {
        return EngineSettings.settings().getBoolean("editor.sidebyside", true);
    }
}
