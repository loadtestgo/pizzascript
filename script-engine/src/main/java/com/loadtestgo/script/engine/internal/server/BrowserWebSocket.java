package com.loadtestgo.script.engine.internal.server;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.loadtestgo.script.engine.EngineContext;
import com.loadtestgo.script.engine.EngineSettings;
import com.loadtestgo.script.engine.ScriptException;
import com.loadtestgo.script.engine.TestContext;
import org.java_websocket.WebSocket;
import org.java_websocket.exceptions.WebsocketNotConnectedException;
import org.json.JSONException;
import org.json.JSONObject;
import org.pmw.tinylog.Logger;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class BrowserWebSocket {
    protected final int minSupportedVersion;
    protected boolean verboseLogging;
    protected WebSocket conn;
    protected CountDownLatch connectLatch = new CountDownLatch(1);
    protected String version;
    protected AtomicInteger commandId = new AtomicInteger();
    protected LinkedBlockingQueue<JSONObject> commandResponses;
    protected ObjectMapper mapper = new ObjectMapper();
    protected ByteBuffer byteBuffer = null;
    protected EngineSettings engineSettings = null;
    final protected AtomicBoolean closing = new AtomicBoolean(false);

    public BrowserWebSocket(TestContext testContext) {
        this.commandId.set(0);
        this.commandResponses = new LinkedBlockingQueue<>();

        EngineContext engineContext = testContext.getEngineContext();
        this.verboseLogging = engineContext.isVerbose();
        this.minSupportedVersion = engineContext.getChromeMinVersion();
        this.engineSettings = engineContext.getEngineSettings();
    }

    public void initConnection(WebSocket conn, JSONObject json) {
        this.conn = conn;
        if (json.has("version")) {
            try {
                this.version = json.getString("version");
            } catch (JSONException e) {
                Logger.error(e, "Unable to read browser version number");
            }

            if (this.version != null) {
                Logger.info("Launched Chrome version {}", this.version);
                checkVersionOk();
            }
        }
        connectLatch.countDown();
    }

    private void checkVersionOk() {
        int version;

        int firstNumEnd = this.version.indexOf(".");
        if (firstNumEnd > 0) {
            version = Integer.valueOf(this.version.substring(0, firstNumEnd));
        } else {
            version = Integer.valueOf(this.version);
        }

        if (version < minSupportedVersion) {
            throw new RuntimeException(
                    String.format("Chrome version too old.  At least version %d.x.x is needed.", minSupportedVersion));
        }
    }

    public void close() {
        synchronized (closing) {
            if (closing.get()) {
                return;
            }
            closing.set(true);
        }

        synchronized (this) {
            // Sending null to the commandResponses doesn't work,
            // it throws an error so send a special message to
            // indicate that the connection has closed.
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("goodbye", "cruelworld");
            } catch (JSONException e) {
            }
            commandResponses.add(jsonObject);

            if (conn != null) {
                Logger.debug("Closing socket...");
                conn.close();
                conn = null;
            }
        }

        synchronized (this) {
            closing.set(false);
        }
    }

    public void onMessage(ByteBuffer message) {
        // Always just one byte buffer
        byteBuffer = message;
    }

    public synchronized void onMessage(String message) {
        try {
            onMessage(new JSONObject(message));
        } catch (JSONException e) {
            Logger.error(e, "Unable to parse message");
        }
    }

    protected boolean onMessage(JSONObject json) throws JSONException {
        if (json.has("type")) {
            if (json.getString("type").equals("response")) {
                commandResponses.add(json);
                return true;
            }
        }

        return false;
    }

    public synchronized void sendMessage(String message) {
        Logger.info("sendMessage {}", message);

        if (conn == null) {
            throw new ScriptException("Unable to send message to browser: connection not open");
        }

        try {
            conn.send(message);
        } catch (WebsocketNotConnectedException t) {
            throw new ScriptException("Unable to send message to browser: browser connection closed");
        } catch (Throwable t) {
            Logger.warn(t);
            throw t;
        }
    }

    public synchronized void onError(Exception ex) {
        Logger.error(ex, "BrowserWebSocket socket error");
    }

    public boolean waitForConnection() throws InterruptedException {
        return connectLatch.await(engineSettings.getBrowserWaitConnectionTime(), TimeUnit.MILLISECONDS);
    }

    public String getVersion() {
        return version;
    }

    public JSONObject webDriverPost(String path, Map<String,Object> params) {
        int commandId = this.commandId.addAndGet(1);

        try {
            sendMessage(String.format(
                    "{ \"type\": \"webdriver\", \"path\": \"%s\", \"id\": %d, \"method\": \"POST\", \"params\": %s }",
                    path, commandId, mapper.writeValueAsString(params)));
        } catch (JsonProcessingException e) {
        }

        return waitResponse(commandId);
    }

    public JSONObject webDriverGet(String path, Map<String,Object> params) {
        int commandId = this.commandId.addAndGet(1);

        try {
            sendMessage(String.format(
                    "{ \"type\": \"webdriver\", \"path\": \"%s\", \"id\": %d, \"method\": \"GET\", \"params\": %s }",
                    path, commandId, mapper.writeValueAsString(params)));
        } catch (JsonProcessingException e) {
        }

        return waitResponse(commandId);
    }

    public JSONObject sendCommand(String path) {
        long start = 0;
        if (verboseLogging) {
            start = System.currentTimeMillis();
        }

        int commandId = this.commandId.addAndGet(1);

        sendMessage(String.format("{ \"name\": \"%s\", \"id\": %d, \"type\": \"pizza\" }",
                path, commandId));

        JSONObject response = waitResponse(commandId);
        if (verboseLogging) {
            Logger.info("command took: {} ms", System.currentTimeMillis() - start);
        }
        return response;
    }

    public JSONObject sendCommand(String path, Map<String, Object> params) {
        long start = 0;
        if (verboseLogging) {
            start = System.currentTimeMillis();
        }

        int commandId = this.commandId.addAndGet(1);

        try {
            sendMessage(String.format("{ \"name\": \"%s\", \"id\": %d, \"type\": \"pizza\", \"params\": %s }",
                    path, commandId, mapper.writeValueAsString(params)));
        } catch (JsonProcessingException e) {
            // Ignore, shouldn't ever happen, params is simple
        }

        JSONObject response = waitResponse(commandId);
        if (verboseLogging) {
            Logger.info("command took: {} ms", System.currentTimeMillis() - start);
        }
        return response;
    }

    private JSONObject waitResponse(int commandId) {
        while (true) {
            JSONObject obj;
            try {
                obj = commandResponses.take();
            } catch (InterruptedException e) {
                Logger.info("Interrupted while waiting for response for command id: {}", commandId);
                throw new ScriptException("Script interrupted");
            }

            Logger.info(obj);
            if (obj.has("id")) {
                try {
                    int id = obj.getInt("id");
                    if (id == commandId) {
                        return obj;
                    }
                } catch (JSONException e) {
                    Logger.error(e);
                }
            } else if (obj.has("goodbye")) {
                try {
                    String status = obj.getString("goodbye");
                    if (status.equals("cruelworld")) {
                        throw new RuntimeException("Lost connection to browser.");
                    }
                } catch (JSONException e) {
                    Logger.error(e);
                }
            }
        }
    }

    public boolean isOpen() {
        return conn.isOpen();
    }

    public ByteBuffer getByteBuffer() {
        ByteBuffer r = byteBuffer;
        byteBuffer = null;
        return r;
    }
}
