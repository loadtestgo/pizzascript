package com.loadtestgo.script.runner;

import com.loadtestgo.script.runner.config.TestConfig;
import com.loadtestgo.script.runner.config.JsonConfigParser;
import org.json.JSONException;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;

public class JsonConfigParserTests {
    @Test
    public void basic() {
        String json = "{ tests: [ { file: 'abc.js',  'timeout': 1.2 } ] }";

        TestConfig config = new TestConfig();
        config.setFileName("");

        JsonConfigParser.parseSource(config, json);
        Assert.assertEquals(1, config.getTests().size());
        Assert.assertEquals(1200, config.getTests().get(0).getTimeout());
        Assert.assertEquals(new File("abc.js"), config.getTests().get(0).getFile());
    }

    @Test
    public void parseError() {
        String json = "{ ";

        expectException("A JSONObject text must end with '}' at 3 [character 4 line 1]", json);
    }

    @Test
    public void missingTests1() {
        String json = "{ }";

        expectException("must contain 'tests' array at top level", json);
    }

    @Test
    public void missingTests2() {
        String json = "{ tests: [] }";

        expectException("test array must contain one or more tests", json);
    }

    @Test
    public void testsNotArray() {
        String json = "{ tests: 1 }";

        expectException("must contain 'tests' array at top level", json);
    }

    @Test
    public void testsFileMissing() {
        String json = "{ tests: [ {} ] }";

        expectException("test must contain 'file' property", json);
    }

    private void expectException(String expectedError, String json) {
        TestConfig config = new TestConfig();
        config.setFileName("");

        try {
            JsonConfigParser.parseSource(config, json);
            Assert.fail("Parse exception expected");
        } catch (JSONException e) {
            Assert.assertEquals(expectedError, e.getMessage());
        }
    }
}
