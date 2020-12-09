package com.loadtestgo.script.tester.tests;

import com.loadtestgo.script.api.TestResult;
import com.loadtestgo.script.tester.framework.JavaScriptTest;
import org.junit.Test;

public class DialogTests extends JavaScriptTest {
    @Test
    public void alertOnLoad() {
        // Do we get an error when an alert dialog is opened on document.onload?
        // This is useful to error out on as it could be malware or a bug in the
        // users page.
        String script = String.format(
            "var b = pizza.open(\"%s\");\n",
            getTestUrl("files/dialog.html"));

        TestResult result = runScript(script);

        assertError("JavaScript alert \"it's a dialog!\" interrupted navigation. " +
            "See Browser.dismissDialogs() to find out how to ignore this error.", result);
    }

    @Test
    public void dismissDialogs() {
        // Can we handle opening a page that calls an alert dialog on document.onload?
        String script = String.format(
            "var b = pizza.open();\n" +
            "b.dismissDialogs();\n" +
            "b.open(\"%s\");",
            getTestUrl("files/dialog.html"));

        TestResult result = runScript(script);

        assertNoError(result);
    }

    @Test
    public void handleAcceptDialog() {
        String script = String.format(
            "var b = pizza.open();\n" +
            "b.openAsync(\"%s\");\n" +
            // Wait for dialog to open
            "pizza.waitFor(function() { return b.isDialogOpen(); });" +
            // Check that getOpenDialog() is working correctly
            "var d = b.getOpenDialog();\n" +
            "assert.ne(null, d);\n" +
            "assert.eq(\"alert\", d.type);\n" +
            "assert.eq(\"it's a dialog!\", d.message);\n" +
            // Close the dialog
            "b.handleDialog(true);\n" +
            // Make sure dialog is closed
            "assert.eq(false, b.isDialogOpen());" +
            // Check that getOpenDialog() is working correctly
            "d = b.getOpenDialog();\n" +
            "assert.eq(null, d);\n",
            getTestUrl("files/dialog.html"));

        TestResult result = runScript(script);

        assertNoError(result);
    }

    @Test
    public void dialogOpen() {
        String script = String.format(
            "var b = pizza.open(\"%s\");\n" +
            "b.click('#alert100ms');\n" +
            "pizza.sleep(1000);",
            getTestUrl("files/dialogLater.html"));

        TestResult result = runScript(script, 3000);
        assertNoError(result);
    }

    @Test
    public void dialogLater() {
        String script = String.format(
            "var b = pizza.open(\"%s\");\n" +
            "b.click('#alert100ms');\n" +
            "pizza.sleep(1000);",
            getTestUrl("files/dialogLater.html"));

        TestResult result = runScript(script, 3000);

        assertNoError(result);
    }
}
