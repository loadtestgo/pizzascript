package com.loadtestgo.script.tester.framework;

import com.loadtestgo.util.Settings;

public class TestSettings extends Settings {
    public enum RunScriptMethod {
        LoadBot,
        InProcess
    }

    static public RunScriptMethod getRunScriptMethod()
    {
        String method = settings().getString("test.method");
        if (method == null) {
            return RunScriptMethod.InProcess;
        }
        return RunScriptMethod.valueOf(method);
    }

    static public String getLoadBotUrl()
    {
        String url = settings().getString("test.loadbot.url", "http://localhost:10666/");
        if (!url.endsWith("/")) {
            return url + "/";
        } else {
            return url;
        }
    }

    static public long getDefaultScriptTimeout()
    {
        return settings().getLong("test.timeout", 10000);
    }
}
