package com.loadtestgo.script.runner;

import com.loadtestgo.script.engine.EngineSettings;

public class RunnerSettings {
    /**
     * Should we write timestamps to the consolo.log() output?
     */
    public static boolean consoleWriteTimeStamps() {
        return EngineSettings.settings().getBoolean("runner.console.writetimestamps", true);
    }

    /**
     * The file type of screenshots to write.  Can be jpeg, png or webp.
     */
    public static String screenshotType() {
        return EngineSettings.settings().getString("runner.screenshot.type", "jpeg");
    }
}
