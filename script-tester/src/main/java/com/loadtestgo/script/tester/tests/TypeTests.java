package com.loadtestgo.script.tester.tests;

import com.loadtestgo.script.api.TestResult;
import com.loadtestgo.script.tester.framework.JavaScriptTest;
import org.junit.Test;

/**
 * Tests for the type command.
 */
public class TypeTests extends JavaScriptTest {
    @Test
    public void basicTyping() {
        String script = String.format(
            "b = pizza.open(\"%s\");\n" +
            "b.type('#input1', 'Text1');\n" +
            "assert.eq(b.jq('$(\\'#input1\\').val()'), 'Text1');\n" +
            "b.type('#input2', 'Text2');\n" +
            "b.type('#input2', 'Text3');\n" +
            "assert.eq(b.jq('$(\\'#input2\\').val()'), 'Text2Text3');\n" +
            "b.type('textarea[name=\\'input3\\']', 'text3');\n" +
            "assert.eq(b.jq('$(\\'textarea\\').val()'), 'text3');\n",
            getTestUrl("files/form.html"));

        TestResult result = runScript(script);
        assertNoError(result);
    }

    @Test
    public void email() {
        String script = String.format(
            "b = pizza.open(\"%s\");\n" +
            "b.type('#input1', 'chad.summerchild86+1@vanish.org');\n" +
            "assert.eq(b.getValue('#input1'), 'chad.summerchild86+1@vanish.org');\n",
            getTestUrl("files/inputHandlers.html"));

        TestResult result = runScript(script);
        assertNoError(result);
    }

    @Test
    public void otherCommonChars() {
        String script = String.format(
            "b = pizza.open(\"%s\");\n" +
            "b.type('#input1', '!@#^&()_+=-{}[]:;\"\\',.<>?/*$`~|');\n" +
            "assert.eq(b.getValue('#input1'), '!@#^&()_+=-{}[]:;\"\\',.<>?/*$`~|');\n",
            getTestUrl("files/inputHandlers.html"));

        TestResult result = runScript(script);
        assertNoError(result);
    }

    @Test
    public void specialOnUSKeyboard() {
        String script = String.format(
            "b = pizza.open(\"%s\");\n" +
            "b.type('#input1', '£¢¥€');\n" +
            "assert.eq(b.getValue('#input1'), '£¢¥\\u20ac');\n",
            getTestUrl("files/inputHandlers.html"));

        TestResult result = runScript(script);
        assertNoError(result);
    }

    @Test
    public void percent() {
        // percent is escaped here due to String.format()
        String script = String.format(
            "b = pizza.open(\"%s\");\n" +
            "b.type('#input1', '%%');\n" +
            "assert.eq(b.getValue('#input1'), '%%');\n",
            getTestUrl("files/inputHandlers.html"));

        TestResult result = runScript(script);
        assertNoError(result);
    }

    @Test
    public void multMod() {
        String script = String.format(
            "b = pizza.open(\"%s\");\n" +
            "b.type('#input1', '');\n" +
            "assert.eq(b.getValue('#input1'), '%%*');\n",
            getTestUrl("files/inputHandlers.html"));

        TestResult result = runScript(script);
        assertNoError(result);
    }

    @Test
    public void eventChecksLowercaseLetter() {
        String script = String.format(
            "b = pizza.open(\"%s\");\n" +
            "b.type('#input1', 'a');\n" +
            "assert.eq(b.execute('storedEvents.length'), 3);\n" +
            checkEventFunction() +
            "b.execute('check(storedEvents[0], \\'keydown\\', 65, 0, \\'a\\', \\'KeyA\\')');\n" +
            "b.execute('check(storedEvents[1], \\'keypress\\', 97, 97, \\'a\\', \\'KeyA\\')');\n" +
            "b.execute('check(storedEvents[2], \\'keyup\\', 65, 0, \\'a\\', \\'KeyA\\')');\n",
            getTestUrl("files/inputHandlers.html"));

        TestResult result = runScript(script);
        assertNoError(result);
    }

    @Test
    public void eventChecksUppercaseLetter() {
        String script = String.format(
            "b = pizza.open(\"%s\");\n" +
            "b.type('#input1', 'A');\n" +
            "assert.eq(b.execute('storedEvents.length'), 3);\n" +
            checkEventFunction() +
            "b.execute('check(storedEvents[0], \\'keydown\\', 65, 0, \\'A\\', \\'KeyA\\')');\n" +
            "b.execute('check(storedEvents[1], \\'keypress\\', 65, 65, \\'A\\', \\'KeyA\\')');\n" +
            "b.execute('check(storedEvents[2], \\'keyup\\', 65, 0, \\'A\\', \\'KeyA\\')');\n",
            getTestUrl("files/inputHandlers.html"));

        TestResult result = runScript(script);
        assertNoError(result);
    }

    @Test
    public void eventChecksTab() {
        // Test tab moves focus and generates correct events
        String script = String.format(
            "b = pizza.open(\"%s\");\n" +
            "b.type('#input1', '\\t');\n" +
            checkTabEventsCode() +
            "assert.eq(b.execute('document.activeElement.id'), 'input2');\n",
            getTestUrl("files/inputHandlers.html"));

        TestResult result = runScript(script);
        assertNoError(result);
    }

    @Test
    public void eventChecksTabKey() {
        // Test tab moves focus and generates correct events
        String script = String.format(
            "b = pizza.open(\"%s\");\n" +
            "b.type('#input1', Key.Tab);\n" +
            checkTabEventsCode() +
            "assert.eq(b.execute('document.activeElement.id'), 'input2');\n",
            getTestUrl("files/inputHandlers.html"));

        TestResult result = runScript(script);
        assertNoError(result);
    }

    @Test
    public void eventEnter() {
        String script = String.format(
            "b = pizza.open(\"%s\");\n" +
            "b.type('#input1', '\\n');\n" +
            checkReturnEventsCode(),
            getTestUrl("files/inputHandlers.html"));

        TestResult result = runScript(script);
        assertNoError(result);
    }

    @Test
    public void eventEnterKey() {
        String script = String.format(
            "b = pizza.open(\"%s\");\n" +
            "b.type('#input1', Key.Return);\n" +
            "assert.eq(b.execute('storedEvents.length'), 3);\n" +
            checkReturnEventsCode(),
            getTestUrl("files/inputHandlers.html"));

        TestResult result = runScript(script);
        assertNoError(result);
    }

    private static String checkEventFunction() {
        return
            "b.execute('' + function check(event, type, keyCode, charCode, key, code) {\n" +
            "if (event.type != type)\n" +
            "  throw 'type diff';\n" +
            "else if (event.keyCode != keyCode)\n" +
            "  throw 'keyCode diff';\n" +
            "else if (event.which != keyCode)\n" +
            "  throw 'which diff';\n" +
            "else if (event.charCode != charCode)\n" +
            "  throw 'charCode diff';\n" +
            "else if (event.key != key)\n" +
            "  throw 'key diff';\n" +
            "else if (event.code != code)\n" +
            "  throw 'code diff';\n" +
            "});\n";
    }

    private static String checkReturnEventsCode() {
        return
            checkEventFunction() +
            "b.execute('check(storedEvents[0], \\'keydown\\', 13, 0, \\'Enter\\', \\'Enter\\')');\n" +
            "b.execute('check(storedEvents[1], \\'keypress\\', 13, 13, \\'Enter\\', \\'Enter\\')');\n" +
            "b.execute('check(storedEvents[2], \\'keyup\\', 13, 0, \\'Enter\\', \\'Enter\\')');\n";
    }

    private static String checkTabEventsCode() {
        return
            checkEventFunction() +
            "assert.eq(b.execute('storedEvents.length'), 2);\n" +
            "b.execute('check(storedEvents[0], \\'keydown\\', 9, 0, \\'Tab\\', \\'Tab\\')');\n" +
            "b.execute('check(storedEvents[1], \\'keyup\\', 9, 0, \\'Tab\\', \\'Tab\\')');\n";
    }

    @Test
    public void eventChecksCtrlA() {
        // Not working correctly just yet, Chrome does not allow us to pass
        // shortcuts using the devtools API :(
        String script = String.format(
            "b = pizza.open(\"%s\");\n" +
            "b.type('#input1', 'p');\n" +
            "b.type('#input1', [Key.Command, 65]);\n",
            getTestUrl("files/inputHandlers.html"));

        TestResult result = runScript(script);
        assertNoError(result);
    }
}
