package com.loadtestgo.script.runner.config;

import com.loadtestgo.script.runner.RunnerTest;
import com.loadtestgo.util.FileUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

public class JsonConfigParser {
    public static TestConfig parseFile(File file) throws JSONException {
        TestConfig testConfig = new TestConfig();
        testConfig.setFileName(file.getName());

        String source = FileUtils.readAllText(file);

        parseSource(testConfig, source);

        return testConfig;
    }

    public static void parseSource(TestConfig testConfig, String source) throws JSONException {
        JSONObject root = new JSONObject(source);

        testConfig.setName(root.optString("name"));

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
            if (jTest.has("timeout")) {
                Double timeout = jTest.getDouble("timeout");

                // Timeouts in the json file are decimal seconds
                long timeoutInMs = (long)(timeout * 1000);
                test.setTimeout(timeoutInMs);
            }

            String jFile = jTest.optString("file", null);
            if (jFile == null) {
                throw new JSONException("test must contain 'file' property");
            }

            test.setFile(new File(jFile));

            if (jTest.has("name")) {
                test.setName(jTest.getString("name"));
            }

            testConfig.addTest(test);
        }
    }
}
