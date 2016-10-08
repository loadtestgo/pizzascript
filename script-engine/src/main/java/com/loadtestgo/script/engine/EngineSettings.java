package com.loadtestgo.script.engine;

import com.loadtestgo.util.Os;
import com.loadtestgo.util.Settings;
import org.pmw.tinylog.Logger;

import java.io.File;

public class EngineSettings {
    private final Settings settings;
    private Boolean verboseLogging;
    private Boolean saveChromeLogs;
    private Long browserWaitConnectionTime;

    public EngineSettings(Settings settings) {
        this.settings = settings;
    }

    public String getChromeExecutable() {
        String location = settings.getString("chrome.binary");
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

    /**
     * Min supported Chrome version.  Not actually tested yet!  Note this is a hard don't run,
     * and not an informational message.
     */
    public int getChromeMinVersion() {
        return 32;
    }

    public String getChromeFileName() {
        if (Os.isMac()) {
            return "Google Chrome";
        } else if (Os.isLinux()) {
            return "google-chrome";
        } else {
            return "chrome";
        }
    }

    public boolean sandboxJavaScript() {
        return settings.getBoolean("sandbox", false);
    }

    public boolean getVerboseLogging() {
        if (verboseLogging == null) {
            verboseLogging = settings.getBoolean("verbose", false);
        }
        return verboseLogging;
    }

    public boolean saveChromeLogs() {
        if (saveChromeLogs == null) {
            saveChromeLogs = settings.getBoolean("chrome.logs", false);
        }
        return saveChromeLogs;
    }

    public long getBrowserWaitConnectionTime() {
        if (browserWaitConnectionTime == null) {
            browserWaitConnectionTime = settings.getLong("browser.connection.wait.time", 20 * 1000L);
        }
        return browserWaitConnectionTime;
    }

    public Settings getSettings() {
        return settings;
    }
}
