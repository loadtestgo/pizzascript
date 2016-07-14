package com.loadtestgo.script.tester.tests;

import com.loadtestgo.script.api.TestResult;
import com.loadtestgo.script.tester.framework.JavaScriptTest;
import org.junit.Test;

/**
 * Tests for the type command, you can also use jquery to call the handlers
 * directly but they don't always work because not all javascript input event
 * events are generated.
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
    public void eventChecks() {
        String script = String.format(
            "b = pizza.open(\"%s\");\n" +
            "b.type('#input1', 'a');\n" +
            "assert.eq(b.execute('storedEvents.length'), 3);\n" +
            "b.execute('' + function check(event, type, keyCode) {\n" +
                "if (event.type != type) \n" +
                "  return false;\n" +
                "else if (event.keyCode != keyCode)\n" +
                "  return false;\n" +
                "else if (event.which != keyCode)\n" +
                "  return false;\n" +
                "else return true;\n" +
            "});\n" +
            "assert.ok(b.execute('check(storedEvents[0], \\\'keydown\\\', 65)'));\n" +
            "assert.ok(b.execute('check(storedEvents[1], \\\'keypress\\\', 97)'));\n" +
            "assert.ok(b.execute('check(storedEvents[2], \\\'keyup\\\', 65)'));\n",
            getTestUrl("files/inputHandlers.html"));

        TestResult result = runScript(script);
        assertNoError(result);
    }

    @Test
    public void eventChecksTab() {
        String script = String.format(
            "b = pizza.open(\"%s\");\n" +
            "b.type('#input1', '\\t');\n" +
            "assert.eq(b.execute('storedEvents.length'), 2);\n" +
            "b.execute('' + function check(event, type, keyCode) {\n" +
                "if (event.type != type)\n" +
                "  return false;\n" +
                "else if (event.keyCode != keyCode)\n" +
                "  return false;\n" +
                "else if (event.which != keyCode)\n" +
                "  return false;\n" +
                "else return true;\n" +
            "});\n" +
            "assert.ok(b.execute('check(storedEvents[0], \\\'keydown\\\', 9)'));\n" +
            "assert.ok(b.execute('check(storedEvents[1], \\\'keyup\\\', 9)'));\n" +
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
            "assert.eq(b.execute('storedEvents.length'), 3);\n" +
            "b.execute('' + function check(event, type, keyCode, charCode, keyIdentifier) {\n" +
                "if (event.type != type)\n" +
                "  return false;\n" +
                "else if (event.keyCode != keyCode)\n" +
                "  return false;\n" +
                "else if (event.which != keyCode)\n" +
                "  return false;\n" +
                "else if (event.charCode != charCode)\n" +
                "  return false;\n" +
                "else if (event.keyIdentifier != keyIdentifier)\n" +
                "  return false;\n" +
                "else return true;\n" +
            "});\n" +
            "assert.ok(b.execute('check(storedEvents[0], \\\'keydown\\\', 13, 0, \\'Enter\\')'));\n" +
            "assert.ok(b.execute('check(storedEvents[1], \\\'keypress\\\', 13, 13, \\'Enter\\')'));\n" +
            "assert.ok(b.execute('check(storedEvents[2], \\\'keyup\\\', 13, 0, \\'Enter\\')'));\n",
            getTestUrl("files/inputHandlers.html"));

        TestResult result = runScript(script);
        assertNoError(result);
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

    /*
    var b = pizza.open("http://localhost:3000/files/inputHandlers.html");
b.type("#input1", "\n");
console.log(b.execute("storedEvents[0].keyCode"));
console.log(b.execute("storedEvents[1].keyCode"));
console.log(b.execute("storedEvents[2].keyCode"));
console.log(b.execute("storedEvents[0].charCode"));
console.log(b.execute("storedEvents[1].charCode"));
console.log(b.execute("storedEvents[2].charCode"));
console.log(b.execute("storedEvents[0].keyIdentifier"));
console.log(b.execute("storedEvents[1].keyIdentifier"));
console.log(b.execute("storedEvents[2].keyIdentifier"));
     */
}
