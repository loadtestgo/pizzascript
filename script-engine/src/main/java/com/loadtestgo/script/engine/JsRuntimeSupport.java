package com.loadtestgo.script.engine;

import org.mozilla.javascript.*;
import org.pmw.tinylog.Logger;

import java.io.*;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

public class JsRuntimeSupport extends ScriptableObject {
    private TestContext testContext;

    public JsRuntimeSupport(TestContext testContext) {
        this.testContext = testContext;
    }

    @Override
    public String getClassName() {
        return "top";
    }

    public static Object load(Context cx, Scriptable thisObj, Object[] args, Function notUsed) {
        Scriptable newScope = cx.newObject(thisObj);
        newScope.setPrototype(null);
        newScope.setParentScope(thisObj);

        if (args.length < 1) {
            throw new ScriptException("load() filename not specified");
        }

        String fileName = Context.toString(args[0]);

        JsRuntimeSupport dis = (JsRuntimeSupport) getTopLevelScope(thisObj);

        File file = dis.testContext.getFile(fileName);
        if (!file.exists()) {
            if (dis.testContext.getIsFileSystemSandboxed()) {
                throw new ScriptException("Unable to find file '" + fileName + "'");
            } else {
                throw new ScriptException("Unable to find file '" + fileName + "'");
            }
        }

        String fullPath = file.getPath();
        Logger.info("Loading file " + fullPath + "...");

        try {
            return cx.evaluateReader(newScope, new InputStreamReader(new FileInputStream(file)), fileName, 1, null);
        } catch (IOException e) {
            if (dis.testContext.getIsFileSystemSandboxed()) {
                throw new ScriptException(e.getMessage());
            } else {
                throw new ScriptException(e.getMessage().replace(fullPath, fileName));
            }
        }
    }

    public void register(ScriptableObject scope) {
        Class<?> clazz = getClass();

        List<String> methodsToExpose = Arrays.asList("load");

        Method[] methods = clazz.getMethods();
        for (Method method : methods) {
            if (methodsToExpose.contains(method.getName())) {
                FunctionObject func = new FunctionObject(method.getName(), method, scope);
                ScriptableObject.putProperty(scope, method.getName(), func);
            }
        }
    }
}
