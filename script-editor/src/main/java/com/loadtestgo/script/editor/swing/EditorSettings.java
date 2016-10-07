package com.loadtestgo.script.editor.swing;

import com.loadtestgo.util.IniFile;
import com.loadtestgo.util.Settings;

import java.awt.*;

public class EditorSettings {
    static private Font codeFont;
    static private Settings settings = IniFile.settings();

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
        return settings.getBoolean("editor.sidebyside", true);
    }
}
