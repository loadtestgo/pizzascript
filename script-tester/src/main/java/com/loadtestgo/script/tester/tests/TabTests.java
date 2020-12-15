package com.loadtestgo.script.tester.tests;

import com.loadtestgo.script.api.TestResult;
import com.loadtestgo.script.tester.framework.JavaScriptTest;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Test stuff related to opening new tabs and switching between tabs
 */
public class TabTests extends JavaScriptTest {
    @Test
    public void openNewTab() {
        String script = String.format(
            "b = pizza.open(\"%s\");\n" +
            "b.click('#newTabButton');\n" +
            "b.waitForHttpRequests(2000);",
            getTestUrl("files/openNew.html"));

        TestResult result = runScript(script);

        assertNoError(result);
        assertEquals(2, result.getPages().size());
        assertPagePath(result, 0, "/files/openNew.html");
        assertPagePath(result, 1, "/files/basic.html");
    }

    @Test
    public void openNewWindow() {
        String script = String.format(
            "b = pizza.open(\"%s\");\n" +
            "b.click('#newWindowButton');\n" +
            "b.waitForHttpRequests(2000);",
            getTestUrl("files/openNew.html"));

        TestResult result = runScript(script);

        assertNoError(result);
        assertEquals(2, result.getPages().size());
        assertPagePath(result, 0, "/files/openNew.html");
        assertPagePath(result, 1, "/files/basic.html");
    }

    @Test
    public void selectNewTab() {
        String script = String.format(
            "b = pizza.open(\"%s\");\n" +
            "b.click('#newTabButton');\n" +
            "pizza.sleep(1000);\n" +
            "var t = b.listTabs();\n" +
            "assert.eq(t.length, 2);\n" +
            "assert.eq(t[0].title, 'Open New Tab');\n" +
            "assert.eq(t[1].title, 'Test Page');\n" +
            "b.selectTab({ index: 1 });\n" +
            "b.verifyTitle('Test Page');\n" +
            "b.selectTab({ index: 0 });\n" +
            "b.verifyTitle('Open New Tab');\n" +
            "b.selectTab({ url: 'basic.html' });\n" +
            "b.verifyTitle('Test Page');\n" +
            "b.selectTab({ url: 'openNew.html' });\n" +
            "b.verifyTitle('Open New Tab');\n" +
            "b.selectTab({ title: 'Test Page' });\n" +
            "b.verifyTitle('Test Page');\n" +
            "b.selectTab({ title: 'Open New Tab' });\n" +
            "b.verifyTitle('Open New Tab');\n" +
            "b.selectTab(1);\n" +
            "b.verifyTitle('Test Page');\n" +
            "b.selectTab(0);\n" +
            "b.verifyTitle('Open New Tab');\n",
            getTestUrl("files/openNew.html"));

        TestResult result = runScript(script);

        assertNoError(result);
        assertEquals(2, result.getPages().size());
        assertPagePath(result, 0, "/files/openNew.html");
        assertPagePath(result, 1, "/files/basic.html");
    }

    @Test
    public void newTab() {
        String script = String.format(
            "b = pizza.open(\"%s\");\n" +
            "b.newTab();\n" +
            "b.selectTab({index: 1});" +
            "b.open(\"%s\");\n",
            getTestUrl("files/openNew.html"),
            getTestUrl("files/basic.html"));

        TestResult result = runScript(script);

        assertNoError(result);
        assertEquals(2, result.getPages().size());
        assertPagePath(result, 0, "/files/openNew.html");
        assertPagePath(result, 1, "/files/basic.html");
    }

    @Test
    public void selectNewWindow() {
        String script = String.format(
            "b = pizza.open(\"%s\");\n" +
            "b.click('#newWindowButton');\n" +
            "pizza.sleep(1000);\n" +
            "var t = b.listTabs();\n" +
            "assert.eq(t.length, 2);\n" +
            "assert.eq(t[0].title, 'Open New Tab');\n" +
            "assert.eq(t[1].title, 'Test Page');\n" +
            "b.selectTab({ index: 1 });\n" +
            "b.verifyTitle('Test Page');\n" +
            "b.selectTab({ index: 0 });\n" +
            "b.verifyTitle('Open New Tab');\n" +
            "b.selectTab({ url: 'basic.html' });\n" +
            "b.verifyTitle('Test Page');\n" +
            "b.selectTab({ url: 'openNew.html' });\n" +
            "b.verifyTitle('Open New Tab');\n" +
            "b.selectTab({ title: 'Test Page' });\n" +
            "b.verifyTitle('Test Page');\n" +
            "b.selectTab({ title: 'Open New Tab' });\n" +
            "b.verifyTitle('Open New Tab');\n",
            getTestUrl("files/openNew.html"));

        TestResult result = runScript(script);

        assertNoError(result);
        assertEquals(2, result.getPages().size());
        assertPagePath(result, 0, "/files/openNew.html");
        assertPagePath(result, 1, "/files/basic.html");
    }

    @Test
    public void newWindowBasicOps() {
        String script = String.format(
            "b = pizza.open(\"%s\");\n" +
            "b.click('#newWindowButton');\n" +
            "pizza.sleep(1000);\n" +
            "var t = b.listTabs();\n" +
            "assert.eq(t.length, 2);\n" +
            "b.selectTab({ index: 1 });\n" +
            "b.verifyText('This is the HTML body.');\n" +
            "b.query('button');\n" +
            "b.execute('console.log(10)');\n",
            getTestUrl("files/openNew.html"));

        TestResult result = runScript(script);

        assertNoError(result);
        assertEquals(2, result.getPages().size());
        assertPagePath(result, 0, "/files/openNew.html");
        assertPagePath(result, 1, "/files/basic.html");
    }
}
