package com.loadtestgo.util;

import com.loadtestgo.util.log.CustomLogger;
import com.loadtestgo.util.log.TinyLogger;

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
        return loadSettings(new TinyLogger());
    }

    public static Settings loadSettings(CustomLogger logger) {
        if (settings == null) {
            if (settingsFile == null) {
                settingsFile = new File("settings.ini");
            }

            try {
                logger.info(String.format("Loading settings from %s", settingsFile.getAbsolutePath()));

                settings = IniFile.load(settingsFile);
            } catch (IOException e) {
                String msg = e.getMessage();
                if (e instanceof FileNotFoundException) {
                    msg = "no such file";
                }

                logger.info(String.format("Unable to read settings file '%s', %s", settingsFile.getAbsolutePath(), msg));
                logger.info("Using default settings...");

                settings = new Settings();
            }
        }
        return settings;
    }

    public static Settings settings() {
        if (settings == null) {
            return loadSettings(new TinyLogger());
        } else {
            return settings;
        }
    }
}
