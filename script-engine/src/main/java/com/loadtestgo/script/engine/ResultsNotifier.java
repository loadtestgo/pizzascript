package com.loadtestgo.script.engine;

import com.loadtestgo.script.api.HttpRequest;
import com.loadtestgo.script.api.Page;
import com.loadtestgo.script.api.WebSocketMessage;
import org.json.JSONObject;

public interface ResultsNotifier {
    void onPageStart(Page page);
    void onPageComplete(Page page);
    void onWebSocketFrame(HttpRequest request, WebSocketMessage message);
    void onConsoleMessage(Page.ConsoleMessage message);
    void onInspectElement(JSONObject details);
}
