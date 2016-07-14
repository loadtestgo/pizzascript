package com.loadtestgo.util;

import org.pmw.tinylog.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IniFile {
    private Pattern keyValuePattern = Pattern.compile("^([^=]*)=(.*)$");
    private Map<String, String> settings = new HashMap<>();

    public IniFile() {
    }

    public void load(String path) throws IOException {
        load(new File(path));
    }

    public void load(File path) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = br.readLine()) != null) {
                Matcher m = keyValuePattern.matcher(line);
                if (m.matches()) {
                    String key = m.group(1).trim();
                    String value = m.group(2).trim();
                    settings.put(key, value);
                }
            }
        }
    }

    public String getString(String key) {
        return settings.get(key);
    }

    public String getString(String key, String defaultValue) {
        String value = settings.get(key);
        if (value == null) {
            return defaultValue;
        }
        return value;
    }

    public int getInt(String key, int defaultValue) {
        String value = settings.get(key);
        if (value == null) {
            return defaultValue;
        }

        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public long getLong(String key, long defaultValue) {
        String value = settings.get(key);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public float getFloat(String key, float defaultValue) {
        String value = settings.get(key);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Float.parseFloat(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public double getDouble(String key, double defaultValue) {
        String value = settings.get(key);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        String value = settings.get(key);
        if (value == null) {
            return defaultValue;
        }
        return Boolean.parseBoolean(value);
    }

    public void printSettings() {
        for (Map.Entry<String, String> setting : settings.entrySet()) {
            Logger.info("{} = {}", setting.getKey(), setting.getValue());
        }
    }
}