package com.loadtestgo.script.engine;

import com.loadtestgo.script.api.ErrorType;
import com.loadtestgo.script.api.Key;
import com.loadtestgo.script.api.StackElement;
import com.loadtestgo.script.api.TestResult;
import com.loadtestgo.script.engine.internal.ConsoleResultsNotifier;
import com.loadtestgo.script.engine.internal.api.AssertImpl;
import com.loadtestgo.script.engine.internal.api.Console;
import com.loadtestgo.script.engine.internal.api.PizzaImpl;
import com.loadtestgo.script.engine.internal.api.UtilsImpl;
import com.loadtestgo.script.engine.internal.rhino.DebuggerStopException;
import com.loadtestgo.script.engine.internal.rhino.RhinoContextFactory;
import org.mozilla.javascript.*;
import org.pmw.tinylog.Logger;

import java.util.*;

public class JavaScriptEngine {
    private RhinoContextFactory contextFactory;

    private TestContext testContext;
    private Context context;
    private ScriptableObject scope;
    private Console console;
    private ConsoleNotifier consoleNotifier;

    public JavaScriptEngine() {
        contextFactory = new RhinoContextFactory();
    }

    public void init() {
        init(null);
    }

    public void init(TestContext testContext) {
        if (context != null) {
            finish();
        }

        this.testContext = testContext;

        if (testContext == null) {
            testContext = new EasyTestContext();
            this.testContext = testContext;
        }

        contextFactory.setSandboxed(testContext.sandboxJavaScript());

        context = contextFactory.enterContext();
        JsRuntimeSupport runtimeSupport = new JsRuntimeSupport(testContext);
        scope = context.initStandardObjects(runtimeSupport, false);

        // Remove access to standard Java classes.
        if (testContext.sandboxJavaScript()) {
            String varsToRemove[] = {
                "getClass",
                "Packages",
                "JavaAdapter",
                "JavaImporter",
                "JavaException",
                "ImporterTopLevel",
                "Continuation",
                "java", "javax", "org", "com", "edu", "net"
            };

            for (String var : varsToRemove) {
                scope.delete(var);
            }

        }

        // Setup results notifier to go via the console notifier if none is set
        if (testContext.resultNotifier == null &&
                consoleNotifier != null) {
            this.testContext.setResultNotifier(
                    new ConsoleResultsNotifier(consoleNotifier));
        }

        // MAYBE: If we make PizzaScript package protected, only the interface
        // will be exposed by Rhino, it will ignore all added methods.
        // Maybe this is a good idea...

        Object pizzaScript = Context.javaToJS(new PizzaImpl(testContext, this), scope);
        ScriptableObject.putProperty(scope, "pizza", pizzaScript);

        this.console = new Console(testContext.getTestResult(), this);
        if (consoleNotifier != null) {
            this.console.setOut(consoleNotifier);
        }

        Object console = Context.javaToJS(this.console, scope);
        ScriptableObject.putProperty(scope, "console", console);

        Object assertObj = Context.javaToJS(new AssertImpl(this), scope);
        ScriptableObject.putProperty(scope, "assert", assertObj);

        Object utilsObj = Context.javaToJS(new UtilsImpl(), scope);
        ScriptableObject.putProperty(scope, "utils", utilsObj);

        runtimeSupport.register(scope);

        ScriptableObject.putProperty(scope, "Key", defineStaticClass(Key.class));
    }

    static private class StaticClassWrapper extends NativeJavaClass {
        StaticClassWrapper(Scriptable scope, Class<?> cl) {
            super(scope, cl);
        }
    }

    private Object defineStaticClass(Class<?> cl) {
        return new StaticClassWrapper(scope, cl);
    }

    public void registerStaticFunction(String functionName,java.lang.reflect.Method method) {
        FunctionObject func = new FunctionObject(functionName, method, scope);
        ScriptableObject.putProperty(scope, functionName, func);
    }

    public static boolean eq(Object object1, Object object2) {
        try {
            // We have to manually compare arrays, Rhino doesn't do it properly.
            if (object1 != null && object2 != null) {
                if (object1 instanceof NativeArray &&
                        object2 instanceof NativeArray) {
                    NativeArray array1 = (NativeArray)object1;
                    NativeArray array2 = (NativeArray)object2;
                    if (array1.getLength() != array2.getLength()) {
                        return false;
                    }
                    for (int i = 0; i < array1.getLength(); ++i) {
                        if (!eq(array1.get(i), array2.get(i))) {
                            return false;
                        }
                    }
                    return true;
                }
            }
            return ScriptRuntime.eq(object1, object2);
        } catch (Exception e) {
            return false;
        }
    }

    static public class CompletionGroup {
        public List<String> completions;
        public String name;
    }

    /**
     * Generates a list of candidate completions for the given string.
     *
     * Returns the number of chars that can be auto completed
     */
    public int complete(String str, int cursor, List<CompletionGroup> candidates) {
        // Parse all valid variable characters and periods from the current
        // position back until there is no more.
        int pos = cursor - 1;
        while (pos >= 0) {
            char c = str.charAt(pos);
            if (!Character.isJavaIdentifierPart(c) && c != '.') {
                break;
            }
            pos--;
        }

        // Split the object name & property chain, we will try to
        // match these in the current scope.
        String scopePath = str.substring(pos + 1, cursor);
        String partialName = "";
        ArrayList<String> names = new ArrayList<>();
        int lastIndex = 0;
        boolean bDone = false;
        while (!bDone) {
            int index = scopePath.indexOf('.', lastIndex);
            if (index > 0) {
                names.add(scopePath.substring(lastIndex, index));
                lastIndex = index + 1;
            } else {
                if (lastIndex < scopePath.length()) {
                    partialName = scopePath.substring(lastIndex);
                }
                bDone = true;
            }
        }

        // Resolves the scope given as far as we can
        Scriptable scope = this.scope;
        for (int i = 0; i < names.size(); ++i) {
            Object val = scope.get(names.get(i), scope);
            if (val == null) {
                return str.length();
            } else if (val instanceof Scriptable) {
                scope = (Scriptable) val;
            } else {
                scope = Context.toObject(val, scope);
            }
        }

        // Now list all objects at the scope calculated in the previous
        // step that match the current partial string (to the right of
        // the dot).
        Object[] ids = null;

        // Walk up the prototype chain gathering all the properties.
        // We normally exclude the Object prototype properties though,
        // just to make the output cleaner.
        ArrayList<String> allIds = new ArrayList<>();

        boolean prototype = false;
        while (scope != null) {
            CompletionGroup group = new CompletionGroup();
            group.completions = new ArrayList<>();

            if (scope instanceof ScriptableObject) {
                ScriptableObject scriptObject = ((ScriptableObject)scope);
                ids = scriptObject.getAllIds();
                group.name = scriptObject.getClassName();
            } else {
                ids = scope.getIds();
                group.name = scope.getClassName();
            }

            if (prototype) {
                group.name += " Prototype";
            }

            for (int i = 0; i < ids.length; ++i) {
                if (ids[i] instanceof String) {
                    String id = (String)ids[i];
                    if (id.startsWith(partialName)) {
                        // Only add if this variable isn't hidden
                        if (!allIds.contains(id)) {
                            allIds.add(id);
                            try {
                                Object o = scope.get(id, scope);
                                if (o instanceof Function && !(o instanceof StaticClassWrapper)) {
                                    id += "()";
                                }
                            } catch (Throwable e) {
                                Logger.error(e);
                            }
                            group.completions.add(id);
                        }
                    }
                }
            }

            if (group.completions.size() > 0) {
                Collections.sort(group.completions);
                candidates.add(group);
            }

            scope = scope.getPrototype();
            prototype = true;
        }

        return cursor - partialName.length();
    }

    public void finish() {
        if (context != null) {
            context.exit();
            context = null;
        }

        if (testContext != null) {
            testContext.cleanup();
            testContext = null;
        }
    }

    public Object runScript(String script, String filename) throws ScriptException {
        return runScript(script, filename, 0);
    }

    public Object runScript(String script, String filename, long timeout) throws ScriptException {
        TestResult result = testContext.getTestResult();
        Date startTime = new Date();
        result.setStartTime(startTime);

        Timer timer = null;
        InterruptTimer interruptTimer = null;
        if (timeout > 0) {
            timer = new Timer();
            interruptTimer = new InterruptTimer(timer, Thread.currentThread(),
                testContext, startTime.getTime(), timeout);
        }

        try {
            return context.evaluateString(scope, script, filename, 1, null);
        } catch (Exception e) {
            ScriptException se = asScriptException(e);
            if (interruptTimer != null && interruptTimer.isStopped()) {
                se.setErrorType(ErrorType.Timeout);
            }
            se.populateResult(result);
            throw se;
        } finally {
            long now = System.currentTimeMillis();
            result.setRunTime((int) (now - result.getStartTime().getTime()));
            if (timer != null) {
                timer.cancel();
            }
        }
    }

    public Object runPartialScript(String script, int startLine) throws ScriptException {
        try {
            Object object = context.evaluateString(scope, script, "", startLine, null);

            if (object != Context.getUndefinedValue()) {
                return object;
            }
            return null;
        } catch (Exception we) {
            throw asScriptException(we);
        }
    }

    private ScriptException asScriptException(Throwable e) {
        ScriptException se = null;
        if (e instanceof WrappedException) {
            WrappedException we = (WrappedException)e;
            Throwable throwable = we.getWrappedException();
            String errorMessage = "";
            if (throwable != null) {
                errorMessage = throwable.getMessage();
                if (errorMessage == null) {
                    errorMessage = throwable.toString();
                }
            }
            if (throwable instanceof ScriptException) {
                se = (ScriptException)throwable;
                se.setFile(we.sourceName());
                se.setLine(we.lineNumber());
                se.setColumn(we.columnNumber());
                se.setJSStackTrace(getStackTrace(we));
            } else {
                se = new ScriptException(
                    ErrorType.Script,
                    errorMessage, we.sourceName(),
                    we.lineNumber(), we.columnNumber(),
                    getStackTrace(we));
            }
        } else if (e instanceof RhinoException) {
            RhinoException re = (RhinoException)e;
            se = new ScriptException(
                    ErrorType.Script,
                    re.details(), re.sourceName(),
                    re.lineNumber(), re.columnNumber(),
                    getStackTrace(re));
        } else if (e instanceof DebuggerStopException) {
            se = new ScriptException("Debugger stopped program execution");
            se.setErrorType(ErrorType.Stopped);
        } else {
            String msg = e.getMessage();
            if (msg == null) {
                msg = e.toString();
            }
            se = new ScriptException(msg);
        }

        return se;
    }

    private List<StackElement> getStackTrace(RhinoException re) {
        ScriptStackElement[] stackTrace = re.getScriptStack();
        if (stackTrace == null) {
             return null;
        }
        ArrayList<StackElement> ourStackTrace = new ArrayList<>();
        for (ScriptStackElement traceElement : stackTrace) {
            StackElement ourElement = new StackElement();
            ourStackTrace.add(ourElement);
            ourElement.file = traceElement.fileName;
            ourElement.line = traceElement.lineNumber;
            ourElement.func = traceElement.functionName;
        }
        return ourStackTrace;
    }

    public Script compile(String script, String scriptName) {
        return context.compileString(script, scriptName, 1, null);
    }

    public Scriptable getScope() {
        return scope;
    }

    public boolean stringIsCompilableUnit(String source) {
        return context.stringIsCompilableUnit(source);
    }

    public String valueToString(Object result) {
        if (result == null) {
            return "undefined";
        }
        if (result instanceof NativeArray ||
                result instanceof NativeObject) {
            try {
                return (String) NativeJSON.stringify(context, scope, result, null, 2);
            } catch (EvaluatorException ev) {
                return ev.getMessage();
            }
        }
        return Context.toString(result);
    }

    public void setConsole(ConsoleNotifier consoleNotifier) {
        this.consoleNotifier = consoleNotifier;
        if (console != null) {
            console.setOut(consoleNotifier);
        }
    }

    public Context getContext() {
        return context;
    }
}
