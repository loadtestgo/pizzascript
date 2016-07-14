package com.loadtestgo.script.engine.internal.server;

import org.java_websocket.WebSocket;
import org.java_websocket.exceptions.InvalidDataException;
import org.java_websocket.framing.Framedata;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.java_websocket.util.Charsetfunctions;
import org.json.JSONException;
import org.json.JSONObject;
import org.pmw.tinylog.Logger;

import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BrowserWebSocketServer extends WebSocketServer {
    private Map<WebSocket, BrowserWebSocket> clientMap = new ConcurrentHashMap<>();
    private Map<Integer, BrowserWebSocket> idMap = new ConcurrentHashMap<>();
    private final Map<WebSocket, List<Framedata>> fragmentsPerConnection = new HashMap<>();

    public BrowserWebSocketServer() throws UnknownHostException {
        super(new InetSocketAddress(0));
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
    }

    @Override
    public void start() {
        super.start();

        // Wait for the port to be bound.
        // If there's a better way to do this added to the WebSocket lib we should do it.
        int port = 0;
        while (port == 0) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                return;
            }
            port = getPort();
        }
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        BrowserWebSocket browserCommsHandler = clientMap.get(conn);
        if (browserCommsHandler != null) {
            browserCommsHandler.close();
        }
        synchronized (fragmentsPerConnection) {
            fragmentsPerConnection.remove(conn);
        }
    }

    public void onMessage(WebSocket conn, ByteBuffer message) {
        BrowserWebSocket browserWebSocket = clientMap.get(conn);
        if (browserWebSocket != null) {
            browserWebSocket.onMessage(message);
        }
    }

    public void onFragment(WebSocket conn, Framedata fragment) {
        // Handle large responses, why this isn't handled by the websocket library I don't know...
        List<Framedata> fragmentList = null;
        synchronized (fragmentsPerConnection) {
            fragmentList = fragmentsPerConnection.get(conn);
            if (fragmentList == null) {
                fragmentList = new ArrayList<>();
                fragmentsPerConnection.put(conn, fragmentList);
            }
        }
        synchronized (fragmentList) {
            fragmentList.add(fragment);
            if (fragment.isFin()) {
                try {
                    Framedata first = fragmentList.get(0);
                    if (first.getOpcode() == Framedata.Opcode.BINARY) {
                        int size = 0;
                        for (Framedata frame : fragmentList) {
                            size += frame.getPayloadData().limit();
                        }
                        ByteBuffer buffer = ByteBuffer.allocate(size);
                        for (Framedata frame : fragmentList) {
                            buffer.put(frame.getPayloadData());
                        }
                        onMessage(conn, buffer);
                    } else {
                        StringBuilder buffer = new StringBuilder();
                        for (Framedata frame : fragmentList) {
                            buffer.append(Charsetfunctions.stringUtf8(frame.getPayloadData()));
                        }
                        onMessage(conn, buffer.toString());
                    }
                } catch (InvalidDataException e) {
                    Logger.error("Error decoding frame {}", e.getMessage());
                } finally {
                    fragmentList.clear();
                }
            }
        }
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        BrowserWebSocket browserWebSocket = clientMap.get(conn);
        if (browserWebSocket == null) {
            // No existing comms handler, check that this is the init event (it better be)
            try {
                JSONObject json = new JSONObject(message);
                if (json.has("event") && json.getString("event").equals("init")) {
                    browserWebSocket = idMap.get(json.getInt("id"));
                    if (browserWebSocket == null) {
                        Logger.error("Unknown BrowserCommsHandler for id {}", json.getString("id"));
                    } else {
                        browserWebSocket.initConnection(conn, json);
                        clientMap.put(conn, browserWebSocket);
                    }
                } else {
                    Logger.error("No handler for connection {}", conn);
                }
            } catch (JSONException e) {
                Logger.error(e);
            }
        } else {
            browserWebSocket.onMessage(message);
        }
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        if (conn != null) {
            BrowserWebSocket browserWebSocket = clientMap.get(conn);
            if (browserWebSocket != null) {
                browserWebSocket.onError(ex);
            }
        } else {
            Logger.error(ex, "WebSocket error");
        }
    }

    public void initHandler(int userId, BrowserWebSocket browserWebSocket) {
        idMap.put(userId, browserWebSocket);
    }
}