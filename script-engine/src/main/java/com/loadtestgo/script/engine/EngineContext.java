package com.loadtestgo.script.engine;

import com.loadtestgo.script.engine.internal.server.BrowserWebSocketServer;
import org.pmw.tinylog.Logger;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.ArrayList;
import java.io.IOException;

/**
 * The main EngineContext that is shared between multiple browsers all
 * going at the same time.  It controls & provides the main websocket
 * server that each browser talks to.
 */
public class EngineContext {
    private BrowserWebSocketServer webSocketServer;
    private String localPublicIp; // The IP seen by websites we contact
    private InetAddress webSocketIp; // The ip the websocket is brought up on or null for localhost.
    private String botName;
    private int loadTestBotIndex;
    private int loadTestId;
    private String location;

    public EngineContext()
    {
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
}

