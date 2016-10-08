package com.loadtestgo.script.engine.internal.browsers.chrome;

import com.loadtestgo.script.engine.EngineSettings;
import com.loadtestgo.util.log.CustomLogger;
import com.loadtestgo.util.Settings;
import com.loadtestgo.util.log.TinyLogger;

import java.io.File;

public class ChromeFinder {
    static public File findChrome(Settings settings) {
        EngineSettings engineSettings = new EngineSettings(settings);
        return findChrome(engineSettings, new TinyLogger());
    }

    static public File findChrome(Settings settings, CustomLogger logger) {
        EngineSettings engineSettings = new EngineSettings(settings);
        return findChrome(engineSettings, logger);
    }

    static public File findChrome(EngineSettings engineSettings) {
        return findChrome(engineSettings, new TinyLogger());
    }

    static public File findChrome(EngineSettings engineSettings, CustomLogger logger) {
        String fileName = engineSettings.getChromeExecutable();
        String fallingBack = "falling back to looking for Chrome in PATH";

        if (fileName != null) {
            File file = new File(fileName);
            if (!file.exists()) {
                logger.warn(String.format("Settings location does not point to Chrome executable: %s, %s",
                    file.getAbsolutePath(), fallingBack));
            } else if (file.isDirectory()) {
                logger.warn(String.format("Settings location points to directory instead of Chrome executable: %s, %s",
                    file.getAbsolutePath(), fallingBack));
            } else if (!file.canExecute()) {
                logger.warn(String.format("Do not have execute permissions on: %s, %s",
                    file.getAbsolutePath(), fallingBack));
            } else {
                // ok
                return file;
            }
        } else {
            logger.info(String.format("Chrome location not set, %s.", fallingBack));
        }

        File file = com.loadtestgo.util.FileUtils.findExecutable(engineSettings.getChromeFileName());

        if (file != null) {
            logger.info(String.format("Found Chrome at %s", file.getAbsolutePath()));
        } else {
            logger.warn(String.format("Unable to find '%s' on PATH or in settings (use the 'chrome.binary' setting to override))",
                engineSettings.getChromeFileName()));
        }

        return file;
    }
}
