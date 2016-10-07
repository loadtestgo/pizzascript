package com.loadtestgo.script.runner;

import com.loadtestgo.util.Settings;

public class RunnerSettings {
    private Settings settings;

    public RunnerSettings(Settings settings) {
        this.settings = settings;
    }

    /**
     * Should we write timestamps to the console.log() output?
     */
    public boolean consoleWriteTimeStamps() {
        return settings.getBoolean("runner.console.writetimestamps", true);
    }

    /**
     * The file type of screenshots to write.  Can be jpeg, png or webp.
     */
    public String screenshotType() {
        return settings.getString("runner.screenshot.type", "jpeg");
    }
}
