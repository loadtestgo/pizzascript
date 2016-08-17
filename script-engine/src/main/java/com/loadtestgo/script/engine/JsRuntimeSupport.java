package com.loadtestgo.script.engine;

import com.loadtestgo.script.engine.internal.api.CSVImpl;
import org.mozilla.javascript.*;
import org.pmw.tinylog.Logger;

import java.io.*;
import java.lang.reflect.Method;

public class JsRuntimeSupport extends ScriptableObject {
    private TestContext testContext;

    public JsRuntimeSupport(TestContext testContext) {
        this.testContext = testContext;
    }

    @Override
    public String getClassName() {
        return "top";
    }

    public static Object importFunc(Context cx, Scriptable thisObj, Object[] args, Function notUsed) {
        Scriptable newScope = cx.newObject(thisObj);
        newScope.setPrototype(null);
        newScope.setParentScope(thisObj);

        if (args.length < 1) {
            throw new ScriptException("import() filename not specified");
        }

        String origFilename = Context.toString(args[0]);
        String filename = origFilename;

        JsRuntimeSupport dis = (JsRuntimeSupport) getTopLevelScope(thisObj);
        File file = dis.testContext.getFile(origFilename );

        if (!dis.testContext.getIsFileSystemSandboxed()) {
            filename = file.getAbsolutePath();
        }

        if (!file.exists()) {
            throw new ScriptException("Unable to find file '" + filename + "'");
        }

        Logger.info("Loading file " + filename + "...");

        try {
            return cx.evaluateReader(newScope, new InputStreamReader(new FileInputStream(file)), filename, 1, null);
        } catch (IOException e) {
            if (dis.testContext.getIsFileSystemSandboxed()) {
                throw new ScriptException(e.getMessage());
            } else {
                throw new ScriptException(e.getMessage().replace(origFilename, filename));
            }
        }
    }

    public void register(ScriptableObject scope) {
        Class<?> clazz = getClass();

        Method[] methods = clazz.getMethods();
        for (Method method : methods) {
            if (method.getName().equals("importFunc")) {
                FunctionObject func = new FunctionObject("import", method, scope);
                ScriptableObject.putProperty(scope, "import", func);
            }
        }
    }
}
