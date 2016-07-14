package com.loadtestgo.script.tester.pages;

import com.loadtestgo.script.tester.annotations.Page;
import com.loadtestgo.script.tester.framework.WebRtcRegistry;
import com.loadtestgo.script.tester.server.HttpHeaders;
import com.loadtestgo.script.tester.server.HttpRequest;
import com.loadtestgo.util.HttpHeader;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONWriter;
import org.pmw.tinylog.Logger;

import java.io.IOException;
import java.io.StringWriter;

public class WebRtcApi {
    @Page(desc = "WebRTC session endpoint", path ="/*")
    public void session(HttpRequest request) throws IOException {
        HttpHeaders headers = request.readHeaders();
        String type = request.requestLine.getType();

        WebRtcRegistry registry = WebRtcRegistry.getInstance();

        int callerId = 0;
        if (request.path != null && request.path.length() > 0) {
            callerId = Integer.valueOf(request.path);
        }

        if (type.equals("GET")) {
            WebRtcRegistry.Session session = null;
            if (callerId == 0) {
                session = registry.getUnusedSession();
            } else {
                session = registry.getSession(callerId);
            }

            if (session == null) {
                request.write500Page();
                return;
            }

            if (callerId == 0) {
                callerId = session.clientId;
            }

            request.writeOk();
            request.writeln();
            StringWriter writer = new StringWriter();
            try {
                JSONWriter json = new JSONWriter(writer);
                json.object();
                json.key("sessionId");
                json.value(callerId);
                json.key("messages");
                json.array();
                JSONObject msg = null;
                do {
                    msg = registry.getMessage(callerId);
                    if (msg != null) {
                        json.value(msg);
                    }
                } while (msg != null);
                json.endArray();
                json.endObject();
                request.write(writer.toString());
            } catch (JSONException e) {
                Logger.error(e, "Error construction json session response.");
            }
        } else if (type.equals("POST")) {
            String lengthStr = headers.get(HttpHeader.CONTENT_LENGTH);
            if (lengthStr == null) {
                request.write411();
                return;
            }

            WebRtcRegistry.Session session = null;
            if (callerId == 0) {
                session = registry.createSession();
                callerId = session.hostId;
            } else {
                session = registry.getSession(callerId);
            }

            if (session == null) {
                request.write500Page();
                return;
            }

            int length = Integer.valueOf(lengthStr);
            String body = request.in.readString(length);
            try {
                JSONObject json = new JSONObject(body);
                if (json.has("messages")) {
                    JSONArray array = json.optJSONArray("messages");
                    if (array != null) {
                        for (int i = 0; i < array.length(); ++i) {
                            JSONObject msg = array.optJSONObject(i);
                            registry.addMessage(callerId, msg);
                        }
                    }
                }
            } catch (JSONException e) {
                Logger.error(e, "unable to parse POST request {}", body);
                request.write500Page();
                return;
            }

            request.writeOk();
            request.writeln();
            StringWriter writer = new StringWriter();
            try {
                JSONWriter json = new JSONWriter(writer);
                json.object();
                json.key("sessionId");
                json.value(callerId);
                json.endObject();
                request.write(writer.toString());
            } catch (JSONException e) {
                Logger.error(e, "Error construction json session response.");
            }
        } else {
            request.write500Page();
        }
    }
}
