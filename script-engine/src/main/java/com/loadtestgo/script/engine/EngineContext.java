package com.loadtestgo.script.engine;

import com.loadtestgo.script.engine.internal.browsers.chrome.ChromeFinder;
import com.loadtestgo.script.engine.internal.server.BrowserWebSocketServer;
import com.loadtestgo.util.IniFile;
import com.loadtestgo.util.Settings;
import org.pmw.tinylog.Logger;

import java.io.File;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * The main EngineContext that is shared between multiple browsers / users / sessions.
 * It controls & provides the main websocket server that each browser talks to.
 * It also provides access any global services that an individual browser / user / session
 * requires.
 */
public class EngineContext {
    private BrowserWebSocketServer webSocketServer;
    private String localPublicIp; // The IP seen by websites we contact
    private InetAddress webSocketIp; // The ip the websocket is brought up on or null for localhost.
    private String botName;
    private int loadTestBotIndex;
    private int loadTestId;
    private String location;
    private EngineSettings engineSettings;
    private File chromeExecutable;
    final private Map<String,Long> seqIds = new HashMap<>();

    public EngineContext() {
        this.engineSettings = new EngineSettings(IniFile.settings());
    }

    public EngineContext(Settings settings) {
        this.engineSettings = new EngineSettings(settings);
    }

    public synchronized BrowserWebSocketServer getWebSocketServer()
    {
        if (webSocketServer == null) {
            try {
                webSocketServer = new BrowserWebSocketServer();
                webSocketServer.start();
                Logger.info("WebSocket listening on port {}", String.valueOf(webSocketServer.getPort()));
            } catch (Exception e) {
                Logger.error(e, "Unable to start WebSocketServer");
            }
        }

        return webSocketServer;
    }

    public synchronized void cleanup() {
        try {
            if (webSocketServer != null) {
                webSocketServer.stop(3000);
            }
        } catch (InterruptedException e) {
            Logger.error(e, "while cleaning up");
        }
    }

    public String getLocalPublicIp() {
        return localPublicIp;
    }

    public void setLocalPublicIp(String localPublicIp) {
        this.localPublicIp = localPublicIp;
    }

    public void setWebSocketIp(ArrayList<InetAddress> webSocketIp) {
        for (InetAddress inetAddress : webSocketIp) {
            if (inetAddress instanceof Inet4Address) {
                this.webSocketIp = inetAddress;
            }
        }
        if (this.webSocketIp == null && webSocketIp.size() > 0) {
            this.webSocketIp = webSocketIp.get(0);
        }
    }

    public InetAddress getWebSocketIp() {
        return webSocketIp;
    }

    public String getBotName() {
        return botName;
    }

    public void setBotName(String botName) {
        this.botName = botName;
    }

    public int getLoadTestBotId() {
        return loadTestBotIndex;
    }

    public void setLoadTestBotIndex(int loadTestBotIndex) {
        this.loadTestBotIndex = loadTestBotIndex;
    }

    public int getLoadTestId() {
        return loadTestId;
    }

    public void setLoadTestId(int loadTestId) {
        this.loadTestId = loadTestId;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public boolean isVerbose() {
        return engineSettings.getVerboseLogging();
    }

    public String getAPIVersion() {
        Package thisPackage = EngineSettings.class.getPackage();
        String version = thisPackage.getImplementationVersion();
        if (version == null) {
            version = "dev";
        }
        return version;
    }

    public EngineSettings getEngineSettings() {
        return engineSettings;
    }

    public int getChromeMinVersion() {
        return engineSettings.getChromeMinVersion();
    }

    public void setChromeExecutable(File chromeExecutable) {
        this.chromeExecutable = chromeExecutable;
    }

    public File getChromeExecutable() {
        if (chromeExecutable == null) {
            return ChromeFinder.findChrome(engineSettings);
        }
        return chromeExecutable;
    }

    public long nextSeqId(String namedSequence) {
        synchronized (seqIds) {
            Long val = seqIds.getOrDefault(namedSequence, 0L);
            seqIds.put(namedSequence, val+1);
            return val;
        }
    }
}

