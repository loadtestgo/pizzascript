package com.loadtestgo.script.editor.swing;

import com.loadtestgo.script.api.HttpRequest;
import com.loadtestgo.script.api.Page;
import com.loadtestgo.script.api.TestResult;
import com.loadtestgo.script.api.WebSocketMessage;
import com.loadtestgo.script.engine.ConsoleNotifier;
import com.loadtestgo.script.engine.ResultsNotifier;
import com.loadtestgo.util.HtmlEntities;
import com.loadtestgo.util.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import javax.swing.*;
import java.text.NumberFormat;
import java.util.ArrayList;

public class ConsoleOutputStream implements ConsoleNotifier, ResultsNotifier {
    private ConsoleWriter consoleWriter;
    private ConsoleTextArea textArea;
    private TestResult testResult;

    public ConsoleOutputStream(ConsoleTextArea textArea) {
        this.textArea = textArea;
        this.consoleWriter = new ConsoleWriter();
    }

    @Override
    public void logInfo(String str)
    {
        sendMsg(Type.Info, str);
    }

    @Override
    public void logWarn(String str)
    {
        sendMsg(Type.Warn, str);
    }

    @Override
    public void logError(String str)
    {
        sendMsg(Type.Error, str);
    }

    @Override
    public void onPageStart(Page page) {
        sendMsg(Type.Html,
            String.format("Loading %s...",
                    urlAsPageLink(page, String.format("<- %s", page.getInitialName()))));
    }

    @Override
    public void onPageComplete(Page page) {
        String requestNumStr = null;
        int requestNum = page.getNumRequestsNotCached();
        if (requestNum == 1) {
            requestNumStr = "1 item";
        } else {
            requestNumStr = String.format("%s items", formatNumber(requestNum));
        }

        if (page.getError() == null) {
            sendMsg(Type.Html,
                String.format("Loaded %s in %.3f seconds (%s; %s bytes)",
                        urlAsPageLink(page, String.format("%s ->", page.getFinalName())),
                        page.getLoadTime(),
                        requestNumStr,
                        formatNumber(page.getBytesDownloaded())));
        } else {
            sendMsg(Type.Html,
                String.format("Error loading '%s' %s (%s; %.3f seconds)",
                    urlAsPageLink(page, String.format("%s ->", page.getInitialName())),
                    page.getError().name(),
                    requestNumStr,
                    page.getLoadTime()));
        }
    }

    @Override
    public void onWebSocketFrame(HttpRequest request, WebSocketMessage message) {
        // Ignore
    }

    @Override
    public void onConsoleMessage(Page.ConsoleMessage message) {
        String msg = String.format(" > %s", message.text);

        if (StringUtils.isSet(message.url)) {
            msg += " [" + message.url + "]";
        }

        switch(message.level) {
            case "log":
                sendMsg(Type.Info, msg);
                break;
            case "error":
                sendMsg(Type.Error, msg);
                break;
            default:
                sendMsg(Type.Warn, msg);
        }
    }

    @Override
    public void onInspectElement(JSONObject details) {
        try {
            sendMsg(Type.Inspect, details.toString(2));
        } catch (JSONException e) {
            sendMsg(Type.Warn, "Unable to format inspected element details");
        }
    }

    private String urlAsPageLink(Page page, String url) {
        return String.format("<a href=\"page:%d\">%s</a>",
                page.getPageId(), HtmlEntities.encode(url));
    }

    private String formatNumber(long number) {
        return NumberFormat.getInstance().format(number);
    }

    private void sendMsg(Type type, String msg) {
        int numMessages = 0;
        synchronized (messages) {
            ConsoleMessage message = new ConsoleMessage();
            message.type = type;
            message.str = msg;
            messages.add(message);
            numMessages = messages.size();
        }
        if (numMessages > 100) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
            }
        }
        SwingUtilities.invokeLater(consoleWriter);
    }

    public void setTestResult(TestResult testResult) {
        this.testResult = testResult;
    }

    public TestResult getTestResult() {
        return testResult;
    }

    enum Type {
        Info,
        Warn,
        Error,
        Html,
        Inspect
    }

    static class ConsoleMessage {
        String str;
        Type type;
    }

    ArrayList<ConsoleMessage> messages = new ArrayList<>();

    class ConsoleWriter implements Runnable {
        public ConsoleWriter() {
        }

        public void run() {
            synchronized (messages) {
                for (ConsoleMessage msg : messages) {
                    switch (msg.type) {
                        case Info:
                            textArea.logInfo(msg.str);
                            break;
                        case Warn:
                            textArea.logWarn(msg.str);
                            break;
                        case Error:
                            textArea.logError(msg.str);
                            break;
                        case Html:
                            textArea.appendHtml(msg.str);
                            break;
                        case Inspect:
                            textArea.appendInspect(msg.str);
                            break;
                    }
                }
                messages.clear();
            }
        }
    }
}
