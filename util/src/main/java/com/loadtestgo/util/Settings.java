package com.loadtestgo.util;

import org.pmw.tinylog.Logger;

import java.io.File;
import java.io.IOException;

/**
 * Load a settings.ini from the current directory.
 *
 * Example usage:
 *
 * public class MySettings extends Settings {
 *     static public String getMyConfigMessage()
 *     {
 *         String msg = settings().getString("config.message");
 *         if (msg == null) {
 *            return "defaultMsg""
 *         } else {
 *            return msg;
 *         }
 *     }
 * }
 */
public class Settings {
    protected static IniFile iniFile;
    protected static String rootDir;
    protected static String tmpDir;
    protected static File settingsFile;

    public static String getRootDir()
    {
        if (rootDir == null) {
            rootDir = System.getProperty("user.dir");
        }
        return rootDir;
    }

    public static String getTmpDir() {
        if (tmpDir == null) {
            tmpDir = Path.join(getRootDir(), "tmp");
            File file = new File(tmpDir);
            if (!file.exists()) {
                file.mkdirs();
            }
        }
        return tmpDir;
    }

    /**
     * Force the settings to be loaded.
     * This is useful if you want the file to be read before the some code
     * asks for a setting (normally they are lazy loaded).
     */
    public static void loadSettings() {
        settings();
    }

    /**
     * Override where the settings are read from default
     * @param settingsFile
     */
    public static void setSettingsFile(File settingsFile) { Settings.settingsFile = settingsFile; }

    protected static IniFile settings() {
        if (iniFile == null) {
            iniFile = new IniFile();
            if (settingsFile == null) {
                settingsFile = new File("settings.ini");
            }

            try {
                Logger.info("Loading settings from {}", settingsFile.getAbsolutePath());
                iniFile.load(settingsFile);
            } catch (IOException e) {
                Logger.info("Unable to read settings file");
            }
            iniFile.printSettings();
        }
        return iniFile;
    }
}
