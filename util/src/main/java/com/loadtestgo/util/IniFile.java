package com.loadtestgo.util;

import org.pmw.tinylog.Logger;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IniFile {
    /**
     * key = value pattern for parsing property
     */
    public static Pattern KeyValuePattern = Pattern.compile("^([^=]*)=(.*)$");

    public static Settings load(String path) throws IOException {
        return load(new File(path));
    }

    public static Settings load(File path) throws IOException {
        Settings settings = new Settings();
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = br.readLine()) != null) {
                Matcher m = KeyValuePattern.matcher(line);
                if (m.matches()) {
                    String key = m.group(1).trim();
                    String value = m.group(2).trim();
                    settings.set(key, value);
                }
            }
        }
        return settings;
    }

    protected static Settings settings;
    protected static File settingsFile;

    /**
     * Override where the settings are read from default
     * @param settingsFile
     */
    public static void setSettingsFile(File settingsFile) { IniFile.settingsFile = settingsFile; }

    public static Settings loadSettings() {
        return settings();
    }

    public static Settings settings() {
        if (settings == null) {
            IniFile iniFile = new IniFile();
            if (settingsFile == null) {
                settingsFile = new File("settings.ini");
            }

            try {
                settings = iniFile.load(settingsFile);
                settings.printSettings();

                Logger.info("Loaded settings from {}", settingsFile.getAbsolutePath());
            } catch (IOException e) {
                String msg = e.getMessage();
                if (e instanceof FileNotFoundException) {
                    msg = "no such file";
                }
                Logger.info("Unable to read settings file '{}', {}", settingsFile.getAbsolutePath(), msg);
                Logger.info("Using default settings...");
            }
        }
        return settings;
    }
}
