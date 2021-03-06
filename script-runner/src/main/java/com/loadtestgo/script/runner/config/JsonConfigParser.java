package com.loadtestgo.script.runner.config;

import com.fasterxml.jackson.databind.util.JSONPObject;
import com.loadtestgo.script.runner.RunnerTest;
import com.loadtestgo.util.FileUtils;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JsonConfigParser {
    public static TestConfig parseFile(File file) throws JSONException, IOException {
        TestConfig testConfig = new TestConfig();
        testConfig.setFileName(file.getName());

        List<String> lines = null;
        try (FileInputStream input = new FileInputStream(file)){
            lines = IOUtils.readLines(input);
            StringBuffer source = new StringBuffer();

            // Remove comments
            for (String line : lines) {
                source.append(line.replaceAll("//.*$", ""));
            }

            parseSource(testConfig, source.toString(), file.getParentFile());
        }

        return testConfig;
    }

    public static void parseSource(TestConfig testConfig, String source, File dir) throws JSONException {
        JSONObject root = new JSONObject(source);

        testConfig.setDefaultTimeout(root.optDouble("timeout", 0));

        testConfig.setName(root.optString("name"));

        JSONObject settings = root.optJSONObject("settings");
        if (settings != null) {
            Map<String,String> settingsMap = new HashMap<>();
            for (String key : JSONObject.getNames(settings)) {
                settingsMap.put(key, settings.get(key).toString());
            }
            testConfig.setSettings(settingsMap);
        }

        JSONArray jTests = root.optJSONArray("tests");
        if (jTests == null) {
            throw new JSONException("must contain \'tests\' array at top level");
        }
        if (jTests.length() == 0) {
            throw new JSONException("test array must contain one or more tests");
        }
        for (int i = 0; i < jTests.length(); i++) {
            RunnerTest test = new RunnerTest();
            JSONObject jTest = jTests.getJSONObject(i);

            // Set the timeout
            double timeout = 0;
            if (jTest.has("timeout")) {
                timeout = jTest.getDouble("timeout");
            } else {
                timeout = testConfig.getDefaultTimeout();
            }
            // Timeouts in the json file are decimal seconds
            long timeoutInMs = (long)(timeout * 1000);
            test.setTimeout(timeoutInMs);

            String jFile = jTest.optString("file", null);
            if (jFile == null) {
                throw new JSONException("test must contain 'file' property");
            }

            test.setFile(new File(dir, jFile));

            if (jTest.has("name")) {
                test.setName(jTest.getString("name"));
            }

            testConfig.addTest(test);
        }
    }
}
