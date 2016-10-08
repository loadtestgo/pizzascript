package com.loadtestgo.util;

import com.loadtestgo.util.log.CustomLogger;

import java.util.HashMap;
import java.util.Map;

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
 *            return "defaultMsg";
 *         } else {
 *            return msg;
 *         }
 *     }
 * }
 */
public class Settings {
    private Map<String, String> settings = new HashMap<>();

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

    public void printSettings(CustomLogger logger) {
        for (Map.Entry<String, String> setting : settings.entrySet()) {
            logger.info(String.format("   %s = %s", setting.getKey(), setting.getValue()));
        }
    }

    public void set(String key, String value) {
        settings.put(key, value);
    }

    public void putAll(Settings overrideSettings) {
        settings.putAll(overrideSettings.settings);
    }

    public int count() {
        return settings.size();
    }
}
