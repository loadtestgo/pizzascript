package com.loadtestgo.script.engine;

import com.loadtestgo.util.Os;
import com.loadtestgo.util.Settings;
import org.pmw.tinylog.Logger;

import java.io.File;

public class EngineSettings extends Settings {
    private static Boolean verboseLogging;
    private static Boolean saveChromeLogs;

    static public String getChromeExecutable() {
        String location = settings().getString("chrome.binary");
        if (location == null) {
            if (Os.isMac()) {
                return "/Applications/Google Chrome.app/Contents/MacOS/Google Chrome";
            } else if (Os.isWin()) {
                String programFiles = "c:\\Program Files\\Google\\Chrome\\Application\\chrome.exe";
                if (new File(programFiles).exists()) {
                    return programFiles;
                }

                String programX86Files = "c:\\Program Files (x86)\\Google\\Chrome\\Application\\chrome.exe";
                if (new File(programX86Files).exists()) {
                    return programX86Files;
                }

                String home = System.getProperty("user.home");
                String appData = home + "\\AppData\\Local\\Google\\Chrome\\Application\\chrome.exe";
                if (new File(appData).exists()) {
                    return appData;
                }

                Logger.info("Can't find google chrome at {}", programFiles);
                return programFiles;
            }
        }
        return location;
    }

    static public int getChromeMinVersion() {
        return 32;
    }

    public static String getChromeFileName() {
        if (Os.isMac()) {
            return "Google Chrome";
        } else if (Os.isLinux()) {
            return "google-chrome";
        } else {
            return "chrome";
        }
    }

    public static String getVersion() {
        return "0.2";
    }

    public static boolean sandboxJavaScript() {
        return settings().getBoolean("sandbox", true);
    }

    public static boolean getVerboseLogging() {
        if (verboseLogging == null) {
            verboseLogging = settings().getBoolean("verbose", false);
        }
        return verboseLogging;
    }

    public static boolean saveChromeLogs() {
        if (saveChromeLogs == null) {
            saveChromeLogs = settings().getBoolean("chrome.logs", false);
        }
        return saveChromeLogs;
    }
}
