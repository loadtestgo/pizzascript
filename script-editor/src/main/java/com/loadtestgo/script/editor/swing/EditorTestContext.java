package com.loadtestgo.script.editor.swing;

import com.loadtestgo.script.engine.EasyTestContext;
import com.loadtestgo.script.engine.EngineSettings;
import com.loadtestgo.script.engine.internal.browsers.chrome.ChromeSettings;

public class EditorTestContext extends EasyTestContext {
    public interface WindowPosition {
        int[] getWindowPosition();
    }

    private WindowPosition windowPosition;

    public EditorTestContext(String name, int userId) {
        super(name, userId);

        setSandboxJavaScript(getEngineSettings().sandboxJavaScript());
    }

    public void setWindowPosition(WindowPosition windowPosition) {
        this.windowPosition = windowPosition;
    }

    @Override
    public ChromeSettings getDefaultChromeSettings() {
        ChromeSettings chromeSettings = new ChromeSettings();
        if (windowPosition != null) {
            int[] pos = windowPosition.getWindowPosition();
            if (pos != null) {
                chromeSettings.args = new String[]{
                    String.format("--window-position=%d,%d", pos[0], pos[1]),
                    String.format("--window-size=%d,%d", pos[2], pos[3])
                };
            }
        }
        return chromeSettings;
    }
}
