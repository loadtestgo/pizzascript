package com.loadtestgo.script.engine;

import com.loadtestgo.script.engine.internal.browsers.chrome.ChromeProcess;
import com.loadtestgo.script.engine.internal.browsers.chrome.ChromeWebSocket;

/**
 * Per desktop/user context for running tests, multiple scripts can
 * be run in a UserContext.
 * Holds info about the desktop/user running the tests.
 */
public class UserContext {
    private EngineContext engineContext;
    private int userId;
    private int workerId;
    private int seqId;
    private boolean chromeProfileInitialSetup;
    private ChromeProcess chromeProcess;
    private ChromeWebSocket pizzaHandler;
    private boolean keepBrowserOpen = false;
    private boolean reuseSession = false;

    public UserContext(EngineContext engineContext)
    {
        this.userId = 0;
        this.seqId = 0;
        this.workerId = 0;
        this.engineContext = engineContext;
    }

    public UserContext(EngineContext engineContext, int userId)
    {
        this.userId = userId;
        this.workerId = userId;
        this.seqId = 0;
        this.engineContext = engineContext;
    }

    public void cleanup() {
        if (pizzaHandler != null) {
            pizzaHandler.close();
        }

        if (chromeProcess != null) {
            chromeProcess.close();
        }
    }

    public int getWorkerId() {
        return workerId;
    }

    public int getUserId() {
        return userId;
    }

    public int getSeqId() {
        return seqId;
    }

    public void incrementSeqId() {
        seqId++;
    }

    public EngineContext getEngineContext() {
        return engineContext;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setWorkerId(int workerId) {
        this.workerId = workerId;
    }

    public boolean isChromeProfileInitialSetup() {
        return chromeProfileInitialSetup;
    }

    public void setChromeProfileInitialSetup(boolean chromeProfileInitialSetup) {
        this.chromeProfileInitialSetup = chromeProfileInitialSetup;
    }

    /**
     * Should we keep the browser open between tests?
     *
     * This should provide a speedup in running tests as the browser doesn't
     * have to be launched each time.  Also saves on disk access as the profile
     * is not recreated each time.
     *
     * Off by default as it complicates things an is somewhat experimental right now.
     * Call cleanup() on this class when you want the browser to be closed.
     */
    public boolean keepBrowserOpen() {
        return keepBrowserOpen;
    }

    public void setKeepBrowserOpen(boolean keepBrowserOpen) {
        this.keepBrowserOpen = keepBrowserOpen;
    }

    public ChromeProcess getChromeProcess() {
        return chromeProcess;
    }

    public void setChromeProcess(ChromeProcess chromeProcess) {
        this.chromeProcess = chromeProcess;
    }

    public ChromeWebSocket getPizzaHandler() {
        return pizzaHandler;
    }

    public void setPizzaHandler(ChromeWebSocket pizzaHandler) {
        this.pizzaHandler = pizzaHandler;
    }

    /**
     * Should we reuse the session (keep cookies and local session data) between script runs.
     */
    public boolean reuseSession() {
        return reuseSession;
    }

    public void setReuseSession(boolean reuseSession) {
        this.reuseSession = reuseSession;
    }
}
