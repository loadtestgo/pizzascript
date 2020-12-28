package com.loadtestgo.script.tester.tests;

import com.loadtestgo.script.api.TestResult;
import com.loadtestgo.script.tester.framework.JavaScriptTest;
import org.junit.Test;

public class FrameTests extends JavaScriptTest {
    @Test
    public void selectFrame() {
        String script = String.format(
            "b = pizza.open(\"%s\");\n" +
            "r = b.selectFrame(\"iframe\");\n" +
            "assert.equal(r.type, 'iframe');\n" +
            "assert.equal(r.name, 'name_1');\n" +
            "assert.equal(r.src, '/files/basic.html');\n" +
            "url = b.execute('document.URL');\n" +
            "assert.ok(url.indexOf('/files/basic.html') >= 0);",
            getTestUrl("files/frames/nested.html"));

        TestResult result = runScript(script);

        assertOnePage(result);
        assertNoError(result);
    }

    @Test
    public void selectFrameWithIndex() {
        String script = String.format(
            "b = pizza.open(\"%s\");\n" +
            "r = b.selectFrame(\"iframe:nth(1)\");\n" +
            "assert.equal(r.type, 'iframe');\n" +
            "assert.equal(r.name, 'name_2');\n" +
            "assert.equal(r.src, 'nested2.html');\n",
            getTestUrl("files/frames/nested.html"));

        TestResult result = runScript(script);

        assertOnePage(result);
        assertNoError(result);
    }

    @Test
    public void selectFrameByName() {
        String script = String.format(
            "b = pizza.open(\"%s\");\n" +
            "r = b.selectFrame(\"iframe[name='name_2']\");\n" +
            "assert.equal(r.type, 'iframe');\n" +
            "assert.equal(r.name, 'name_2');\n" +
            "assert.equal(r.src, 'nested2.html');\n",
            getTestUrl("files/frames/nested.html"));

        TestResult result = runScript(script);

        assertOnePage(result);
        assertNoError(result);
    }

    @Test
    public void selectTopFrame() {
        String script = String.format(
            "b = pizza.open(\"%s\");\n" +
            "r = b.selectFrame(\"iframe\");\n" +
            "url = b.execute('document.URL');\n" +
            "assert.ok(url.indexOf('basic.html') > 0);" +
            "b.selectTopFrame();\n" +
            "url = b.execute('document.URL');\n" +
            "assert.ok(url.indexOf('nested.html') > 0);",
            getTestUrl("files/frames/nested.html"));

        TestResult result = runScript(script);

        assertOnePage(result);
        assertNoError(result);
    }

    @Test
    public void selectNestedFrame() {
        String script = String.format(
            "b = pizza.open(\"%s\");\n" +
            "b.selectFrame(\"iframe[name='name_2'] iframe\");\n" +
            "url = b.execute('document.URL');\n" +
            "assert.ok(url.indexOf('/files/basic.html') > 0);",
            getTestUrl("files/frames/nested.html"));

        TestResult result = runScript(script);

        assertOnePage(result);
        assertNoError(result);
    }

    @Test
    public void selectNestedWithSeparateSelectFrameCalls() {
        String script = String.format(
            "b = pizza.open(\"%s\");\n" +
            "b.selectFrame(\"iframe:nth(1)\");\n" +
            "b.selectFrame(\"iframe\");\n" +
            "url = b.execute('document.URL');\n" +
            "assert.ok(url.indexOf('/files/basic.html') > 0);",
            getTestUrl("files/frames/nested.html"));

        TestResult result = runScript(script);

        assertOnePage(result);
        assertNoError(result);
    }

    @Test
    public void listFrames() {
        String script = String.format(
            "b = pizza.open(\"%s\");\n" +
            "r = b.listFrames();\n" +
            // assert two frames at top level
            "assert.equal(r.length, 2);\n" +
            "r = b.listAllFrames();\n" +
            // assert two frames at top level
            "assert.equal(r.length, 2);\n" +
            // assert the second top level frame should have 1 sub-frame
            "assert.equal(r[1].frames.length, 1);",
            getTestUrl("files/frames/nested.html"));

        TestResult result = runScript(script);

        assertOnePage(result);
        assertNoError(result);
    }

    @Test
    public void frameRemoved() {
        String script = String.format(
            "b = pizza.open(\"%s\");\n" +
            "b.click(\"#closeFrame1sBtn\");\n" +
            "r = b.selectFrame(\"iframe:nth(0)\");\n" +
            "b.type(\"#input1\", \"hello\");\n" +
            "assert.equal(b.getValue(\"#input1\"), 'hello');\n" +
            "pizza.sleep(1000);\n" +
            "b.hover(\"#closeFrame1sBtn\");\n",
            getTestUrl("files/frames/forms.html"));

        TestResult result = runScript(script);

        assertOnePage(result);
        assertError("Unable to find frame", result);
    }

    @Test
    public void selectSubFrameAndNavigate() {
        String script = String.format(
            "b = pizza.open(\"%s\");\n" +
            "r = b.selectFrame(\"iframe:nth(0)\");\n" +
            "b.type(\"#input1\", \"hello\");\n" +
            "assert.equal(b.getValue(\"#input1\"), 'hello');\n" +
            "b.open(\"%s\");" +
            "b.type(\"#input1\", \"hello\");\n" +
            "assert.equal(b.getValue(\"#input1\"), 'hello');\n",
            getTestUrl("files/frames/forms.html"),
            getTestUrl("files/form.html"));

        TestResult result = runScript(script);

        assertNumPages(result, 2);
        assertNoError(result);
    }
}
