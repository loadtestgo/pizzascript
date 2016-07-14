package com.loadtestgo.script.engine.internal;

import com.loadtestgo.script.api.HttpRequest;
import com.loadtestgo.script.api.Page;
import com.loadtestgo.script.api.WebSocketMessage;
import com.loadtestgo.script.engine.ConsoleNotifier;
import com.loadtestgo.script.engine.ResultsNotifier;
import com.loadtestgo.util.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;

public class ConsoleResultsNotifier implements ResultsNotifier {
    ConsoleNotifier consoleNotifier;

    public ConsoleResultsNotifier(ConsoleNotifier consoleNotifier) {
        this.consoleNotifier = consoleNotifier;
    }

    @Override
    public void onPageStart(Page page) {
        logInfo(String.format("Loading %s...", page.getInitialName()));
    }

    @Override
    public void onPageComplete(Page page) {
        String requestNumStr = null;
        int requestNum = page.getNumRequests();
        if (requestNum == 1) {
            requestNumStr = "1 item";
        } else {
            requestNumStr = String.format("%s items",
                    formatNumber(page.getNumRequests()));
        }

        if (page.getError() == null) {
            logInfo(String.format("Loaded %s in %.3f seconds (%s; %s bytes)",
                    page.getFinalName(),
                    page.getLoadTime(),
                    requestNumStr,
                    formatNumber(page.getBytesDownloaded())));
        } else {
            logErr(String.format("Error loading '%s' %s (%s; %.3f seconds)",
                   page.getInitialName(),
                   page.getError().name(),
                   requestNumStr,
                   page.getLoadTime()));
        }
    }

    @Override
    public void onWebSocketFrame(HttpRequest request, WebSocketMessage message) {
    }

    @Override
    public void onConsoleMessage(Page.ConsoleMessage message) {
        String msg = String.format("Console %s: %s", message.level, message.text);

        if (StringUtils.isSet(message.url)) {
            msg += " " + message.url;
        }

        switch(message.level) {
            case "log":
                logInfo(msg);
                break;
            case "error":
                logErr(msg);
                break;
            default:
                logErr(msg);
        }
    }

    @Override
    public void onInspectElement(JSONObject details) {
        try {
            logInfo(details.toString(2));
        } catch (JSONException e) {
            logErr("Unable to format inspected element details");
        }
    }

    private void logInfo(String str) {
        if (consoleNotifier != null) {
            consoleNotifier.logInfo(str);
        }
    }

    private void logErr(String str) {
        if (consoleNotifier != null) {
            consoleNotifier.logError(str);
        }
    }

    private String formatNumber(long number) {
        return NumberFormat.getInstance().format(number);
    }
}
