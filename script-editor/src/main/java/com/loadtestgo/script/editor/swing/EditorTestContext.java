package com.loadtestgo.script.editor.swing;

import com.loadtestgo.script.engine.TestContext;
import com.loadtestgo.script.engine.UserContext;
import com.loadtestgo.script.engine.internal.browsers.chrome.ChromeSettings;

public class EditorTestContext extends TestContext {
    public interface WindowPosition {
        int[] getWindowPosition();
    }

    private WindowPosition windowPosition;

    public EditorTestContext(UserContext userContext, String name) {
        super(userContext, name);

        setCaptureVideo(false);
        if (getReuseBrowser()) {
            getUserContext().setKeepBrowserOpen(true);
            getUserContext().setReuseSession(false);
        }
        setSandboxJavaScript(getEngineSettings().sandboxJavaScript());
    }

    public boolean getReuseBrowser() {
        Object reuseBrowser = getEngineSettings().getSettings().getBoolean("reuse.browser", false);
        if (reuseBrowser == null) {
            return false;
        }
        if (reuseBrowser instanceof Boolean) {
            return (Boolean)reuseBrowser;
        }
        return false;
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
