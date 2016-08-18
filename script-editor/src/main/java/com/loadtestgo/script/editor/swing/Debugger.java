package com.loadtestgo.script.editor.swing;

import com.loadtestgo.script.api.TestResult;
import com.loadtestgo.script.engine.EasyTestContext;
import com.loadtestgo.script.engine.EngineSettings;
import com.loadtestgo.script.engine.JavaScriptEngine;
import com.loadtestgo.script.engine.ScriptException;
import com.loadtestgo.script.engine.internal.browsers.chrome.ChromeSettings;
import com.loadtestgo.script.engine.internal.rhino.DebuggerStopException;
import com.loadtestgo.util.Path;
import org.mozilla.javascript.*;
import org.mozilla.javascript.debug.DebugFrame;
import org.mozilla.javascript.debug.DebuggableScript;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class Debugger {
    private enum DebugRunMode {
        NONE,
        BREAK,
        CONTINUE,
        STOP,
        STEP_OVER,
        STEP_INTO,
        STEP_OUT
    }

    private DebuggerCallbacks callback;
    private int frameIndex = -1;
    private volatile ContextData interruptedContextData;
    private final Object stateMonitor = new Object();
    private final Object debugMonitor = new Object();

    private String evalRequest;
    private StackFrame evalFrame;
    private String evalResult;
    private ArrayList<Variable> evalThisVars;
    private ArrayList<Variable> evalLocalVars;
    private Variable enumVariable;
    private boolean evalComplete;

    private boolean breakOnExceptions;
    private boolean cleanupWhenDone = true;
    private JavaScriptEngine javaScriptEngine;
    private TestResult testResult;
    private CodeModel codeModel;

    private volatile DebugRunMode debugRunMode = DebugRunMode.NONE;
    private boolean insideInterruptLoop = false;
    private boolean isRunning = false;
    private boolean isInterrupted = false;

    private Thread debuggerThread;
    private DebuggerExecution debuggerExecution;

    static public class Variable {
        public String name;
        public String value;
        public Object jsVariable;
        public List<Variable> children;

        public Variable(String name, String value, Object jsVariable) {
            this.value = value;
            this.name = name;
            this.jsVariable = jsVariable;
        }

        public boolean hasProperties() {
            return true;
        }

        public Object variable() {
            return jsVariable;
        }
    }

    public Debugger() {
        codeModel = new CodeModel();

        javaScriptEngine = new JavaScriptEngine();

        debuggerExecution = new DebuggerExecution(javaScriptEngine);
        debuggerThread = new Thread(debuggerExecution, "JavaScript Debugger");
        debuggerThread.start();
    }

    public void setOutput(ConsoleOutputStream output) {
        debuggerExecution.setOutput(output);
    }

    public void setWindowPosition(EditorTestContext.WindowPosition windowPosition) {
        debuggerExecution.setWindowPosition(windowPosition);
    }

    public void setSourceFileLookup(SourceFileLookup sourceFileLookup) {
    }

    public void setGuiCallback(DebuggerCallbacks callback) {
        this.callback = callback;
    }

    public CodeModel getCodeModel() {
        return codeModel;
    }

    public TestResult getTestResult() {
        return testResult;
    }

    public void contextSwitch(int frameIndex) {
        this.frameIndex = frameIndex;
    }

    public void setBreakOnExceptions(boolean breakOnExceptions) {
        this.breakOnExceptions = breakOnExceptions;
    }

    public void setCleanupWhenDone(boolean cleanupWhenDone) {
        this.cleanupWhenDone = cleanupWhenDone;
    }

    public SourceFile getSourceFile(String filePath) {
        return codeModel.getSourceFile(filePath);
    }

    public void doBreak() {
        setDebugRunMode(DebugRunMode.BREAK);
    }

    public void stop() {
        setDebugRunMode(DebugRunMode.STOP);
        debuggerThread.interrupt();
    }

    public void doContinue() {
        synchronized (debugMonitor) {
            this.debugRunMode = DebugRunMode.CONTINUE;
            debugMonitor.notifyAll();
        }
    }

    public void stepOver() {
        setDebugRunMode(DebugRunMode.STEP_OVER);
    }

    public void stepOut() {
        setDebugRunMode(DebugRunMode.STEP_OUT);
    }

    public void stepInto() {
        setDebugRunMode(DebugRunMode.STEP_INTO);
    }

    public boolean isDebugging() {
        synchronized (stateMonitor) {
            return insideInterruptLoop;
        }
    }

    public boolean isRunning() {
        synchronized (stateMonitor) {
            return isRunning;
        }
    }

    public boolean isInterrupted() {
        synchronized (stateMonitor) {
            return isInterrupted;
        }
    }

    public ContextData currentContextData() {
        return interruptedContextData;
    }

    public String evalString(String expr) {
        String result = "undefined";
        if (expr == null) {
            return result;
        }
        ContextData contextData = currentContextData();
        if (contextData == null || frameIndex >= contextData.frameCount()) {
            return result;
        }
        StackFrame frame = contextData.getFrame(frameIndex);
        synchronized (debugMonitor) {
            if (insideInterruptLoop) {
                evalRequest = expr;
                evalFrame = frame;
                debugMonitor.notify();
                do {
                    try {
                        debugMonitor.wait();
                    } catch (InterruptedException exc) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                } while (evalRequest != null);
                result = evalResult;
                evalRequest = null;
                evalResult = null;
            }
        }
        return result;
    }

    public void debugScript(SourceFile sourceFile) {
        setDebugRunMode(DebugRunMode.NONE);
        synchronized (stateMonitor) {
            isRunning = true;
        }
        debuggerExecution.startRun(sourceFile);
        callback.evalScriptStarted();
    }

    public String objectToString(Object object) {
        String result;
        try {
            if (object == Undefined.instance) {
                return "undefined";
            } else if (object == null) {
                return "null";
            } else if (object instanceof NativeCall) {
                return "[object Call]";
            } else {
                result = Context.toString(object);
            }
        } catch (RuntimeException exc) {
            return exc.getMessage();
        }

        StringBuffer buf = new StringBuffer();
        int len = result.length();
        for (int i = 0; i < len; i++) {
            char ch = result.charAt(i);
            if (Character.isISOControl(ch)) {
                ch = ' ';
            }
            buf.append(ch);
        }
        return buf.toString();
    }

    public void getVariables(Debugger.StackFrame frame,
                             ArrayList<Variable> localVars,
                             ArrayList<Variable> thisVars) {
        synchronized (debugMonitor) {
            if (insideInterruptLoop) {
                this.evalFrame = frame;
                this.evalThisVars = thisVars;
                this.evalLocalVars = localVars;
                this.evalComplete = false;
                debugMonitor.notify();
                do {
                    try {
                        debugMonitor.wait();
                    } catch (InterruptedException exc) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                } while (!evalComplete);
                this.evalThisVars = null;
                this.evalLocalVars = null;
            }
        }
    }

    public void getVariableChildren(Variable variable) {
        synchronized (debugMonitor) {
            if (insideInterruptLoop) {
                this.enumVariable = variable;
                this.evalComplete = false;
                debugMonitor.notify();
                do {
                    try {
                        debugMonitor.wait();
                    } catch (InterruptedException exc) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                } while (!evalComplete);
                this.enumVariable = null;
            }
        }
    }

    private void handleBreakpointHit(StackFrame frame, Context cx) {
        interrupted(cx, frame, null);
    }

    private void handleExceptionThrown(Context cx, Throwable ex, StackFrame frame) {
        if (!breakOnExceptions) {
            return;
        }

        if (ex instanceof DebuggerStopException) {
            // It's our own exception we use to force a script stop
            return;
        }

        ContextData cd = frame.contextData();
        if (cd.lastProcessedException != ex) {
            interrupted(cx, frame, ex);
            cd.lastProcessedException = ex;
        }
    }

    private void setDebugRunMode(DebugRunMode debugRunMode) {
        synchronized (debugMonitor) {
            this.debugRunMode = debugRunMode;
            debugMonitor.notify();
        }
    }

    private void interrupted(Context cx, final StackFrame frame, Throwable scriptException) {
        ContextData contextData = frame.contextData();

        synchronized (stateMonitor) {
            isInterrupted = true;
            interruptedContextData = contextData;
        }

        try {
            this.frameIndex = contextData.frameCount() - 1;

            DebugRunMode debugRunMode = DebugRunMode.NONE;
            synchronized (debugMonitor) {
                this.insideInterruptLoop = true;
                this.evalRequest = null;
                this.debugRunMode = DebugRunMode.NONE;
                callback.enterInterrupt(frame, scriptException);
                try {
                    while (true) {
                        try {
                            debugMonitor.wait();
                        } catch (InterruptedException exc) {
                            Thread.currentThread().interrupt();
                            break;
                        }

                        if (evalRequest != null) {
                            this.evalResult = null;
                            try {
                                evalResult = doEvaluation(cx, evalFrame, evalRequest);
                            } finally {
                                evalRequest = null;
                                evalFrame = null;
                                debugMonitor.notify();
                            }
                            continue;
                        }

                        if (evalLocalVars != null || evalThisVars != null) {
                            try {
                                doEvalVars(evalFrame, evalLocalVars, evalThisVars);
                            } finally {
                                evalComplete = true;
                                debugMonitor.notify();
                            }
                            continue;
                        }

                        if (enumVariable != null) {
                            try {
                                doEnumVar(enumVariable);
                            } finally {
                                evalComplete = true;
                                debugMonitor.notify();
                            }
                            continue;
                        }

                        if (this.debugRunMode != DebugRunMode.NONE) {
                            debugRunMode = this.debugRunMode;
                            break;
                        }
                    }
                } finally {
                    insideInterruptLoop = false;
                }
            }

            switch (debugRunMode) {
                case STEP_OVER:
                    contextData.breakNextLine = true;
                    contextData.stopAtFrameDepth = contextData.frameCount();
                    break;
                case STEP_INTO:
                    contextData.breakNextLine = true;
                    contextData.stopAtFrameDepth = -1;
                    break;
                case STEP_OUT:
                    if (contextData.frameCount() > 1) {
                        contextData.breakNextLine = true;
                        contextData.stopAtFrameDepth = contextData.frameCount() - 1;
                    }
                    break;
                case STOP:
                    throw new DebuggerStopException();
            }
            callback.evalScriptContinue();
        } finally {
            synchronized (stateMonitor) {
                interruptedContextData = null;
                isInterrupted = false;
            }
        }
    }

    private void doEnumVar(Variable enumVariable) {
        ArrayList<Variable> children = new ArrayList<>();
        doEvalVars(enumVariable.variable(), children);
        enumVariable.children = children;
    }

    private void doEvalVars(StackFrame frame,
                            ArrayList<Variable> evalLocalVars,
                            ArrayList<Variable> evalThisVars) {
        Object scope = frame.scope();
        doEvalVars(scope, evalLocalVars);

        Object thisObj = frame.thisObj();
        doEvalVars(thisObj, evalThisVars);
    }

    private void doEvalVars(Object scope, ArrayList<Variable> variables) {
        Object[] ids = getObjectIds(scope);
        Arrays.sort(ids, new Comparator<Object>() {
            public int compare(Object l, Object r) {
                // Integers before strings
                if (l instanceof String) {
                    if (r instanceof Integer) {
                        return -1;
                    }
                    return ((String) l).compareTo((String) r);
                } else {
                    if (r instanceof String) {
                        return 1;
                    }
                    int lint = ((Integer) l).intValue();
                    int rint = ((Integer) r).intValue();
                    return lint - rint;
                }
            }
        });

        for (Object id : ids) {
            String name = id.toString();
            Object value = getObjectProperty(scope, id);
            String valueString = null;
            try {
                valueString = objectToString(value);
            } catch (Exception e) {
                valueString = e.getMessage();
            }
            Variable variable = new Variable(name, valueString, value);
            variables.add(variable);
        }
    }

    private Object[] getObjectIds(Object object) {
        if (!(object instanceof Scriptable) || object == Undefined.instance) {
            return Context.emptyArgs;
        }

        Scriptable scriptable = (Scriptable)object;
        Object[] ids = scriptable.getIds();

        Scriptable prototype = scriptable.getPrototype();
        Scriptable parent = scriptable.getParentScope();
        int extra = 0;
        if (prototype != null) {
            ++extra;
        }
        if (parent != null) {
            ++extra;
        }
        if (extra != 0) {
            Object[] tmp = new Object[extra + ids.length];
            System.arraycopy(ids, 0, tmp, extra, ids.length);
            ids = tmp;
            extra = 0;
            if (prototype != null) {
                ids[extra++] = "__proto__";
            }
            if (parent != null) {
                ids[extra++] = "__parent__";
            }
        }

        return ids;
    }

    private Object getObjectProperty(Object object, Object id) {
        Scriptable scriptable = (Scriptable)object;
        Object result;
        try {
            if (id instanceof String) {
                String name = (String)id;
                if (name.equals("this")) {
                    result = scriptable;
                } else if (name.equals("__proto__")) {
                    result = scriptable.getPrototype();
                } else if (name.equals("__parent__")) {
                    result = scriptable.getParentScope();
                } else {
                    result = ScriptableObject.getProperty(scriptable, name);
                    if (result == ScriptableObject.NOT_FOUND) {
                        result = Undefined.instance;
                    }
                }
            } else {
                int index = ((Integer)id).intValue();
                result = ScriptableObject.getProperty(scriptable, index);
                if (result == ScriptableObject.NOT_FOUND) {
                    result = Undefined.instance;
                }
            }
        } catch (Exception e) {
            result = e.getMessage();
        }
        return result;
    }

    private static String doEvaluation(Context cx, StackFrame frame, String expr) {
        String resultString;
        org.mozilla.javascript.debug.Debugger savedDebugger = cx.getDebugger();
        Object savedData = cx.getDebuggerContextData();
        int savedLevel = cx.getOptimizationLevel();

        cx.setDebugger(null, null);
        cx.setOptimizationLevel(-1);
        cx.setGeneratingDebug(false);
        try {
            Callable script = (Callable)cx.compileString(expr, "", 0, null);
            Object result = script.call(cx, frame.scope, frame.thisObj,
                                        ScriptRuntime.emptyArgs);
            if (result == Undefined.instance) {
                resultString = "";
            } else {
                resultString = ScriptRuntime.toString(result);
            }
        } catch (Exception e) {
            resultString = e.getMessage();
        } finally {
            cx.setGeneratingDebug(true);
            cx.setOptimizationLevel(savedLevel);
            cx.setDebugger(savedDebugger, savedData);
        }
        if (resultString == null) {
            resultString = "null";
        }
        return resultString;
    }

    public static class ContextData {
        private ObjArray frameStack = new ObjArray();
        private boolean breakNextLine;
        private int stopAtFrameDepth = -1;
        private Throwable lastProcessedException;

        public static ContextData get(Context cx) {
            return (ContextData) cx.getDebuggerContextData();
        }

        public int frameCount() {
            return frameStack.size();
        }

        public StackFrame getFrame(int frameNumber) {
            int num = frameStack.size() - frameNumber - 1;
            return (StackFrame) frameStack.get(num);
        }

        private void pushFrame(StackFrame frame) {
            frameStack.push(frame);
        }

        private void popFrame() {
            frameStack.pop();
        }
    }

    public static class StackFrame implements DebugFrame {
        private Debugger debugger;
        private ContextData contextData;
        private Scriptable scope;
        private Scriptable thisObj;
        private FunctionSource functionSource;
        private int lineNumber;
        private SourceFile sourceFile;

        private StackFrame(Context cx, Debugger debugger, FunctionSource functionSource) {
            this.debugger = debugger;
            this.contextData = ContextData.get(cx);
            this.functionSource = functionSource;
            this.sourceFile = functionSource.sourceFile();
            this.lineNumber = functionSource.firstLine();
        }

        public void onEnter(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
            contextData.pushFrame(this);
            this.scope = scope;
            this.thisObj = thisObj;
        }

        public void onLineChange(Context cx, int lineNo) {
            this.lineNumber = lineNo;

            if (debugger.debugRunMode == DebugRunMode.STOP) {
                throw new DebuggerStopException();
            } else if (debugger.debugRunMode == DebugRunMode.BREAK) {
                debugger.handleBreakpointHit(this, cx);
            } else if (sourceFile.isBreakpoint(lineNo)) {
                debugger.handleBreakpointHit(this, cx);
            } else if (contextData.breakNextLine) {
                if (contextData.stopAtFrameDepth >= 0 &&
                        (contextData.frameCount() > contextData.stopAtFrameDepth)) {
                    return;
                }
                contextData.stopAtFrameDepth = -1;
                contextData.breakNextLine = false;
                debugger.handleBreakpointHit(this, cx);
            }
        }

        public void onExceptionThrown(Context cx, Throwable exception) {
            debugger.handleExceptionThrown(cx, exception, this);
        }

        public void onExit(Context cx, boolean byThrow,
                           Object resultOrException) {
            contextData.popFrame();
        }

        public void onDebuggerStatement(Context cx) {
            debugger.handleBreakpointHit(this, cx);
        }

        public ContextData contextData() {
            return contextData;
        }

        public Object scope() {
            return scope;
        }

        public Object thisObj() {
            return thisObj;
        }

        public String getFileName() {
            return functionSource.sourceFile().getFilePath();
        }

        public int getLineNumber() {
            return lineNumber;
        }

        public String getFunctionName() {
            return functionSource.name();
        }
    }

    public enum Command {
        NONE,
        RUN,
        STOP,
        BREAK
    }

    private class DebuggerExecution implements Runnable {
        private SourceFile file;
        private JavaScriptEngine engine;
        private ConsoleOutputStream consoleOutputStream;
        private EditorTestContext.WindowPosition windowPosition;
        private Map<DebuggableScript, FunctionSource> functionToSource;
        private final Object monitor = new Object();
        private Command command;

        public DebuggerExecution(JavaScriptEngine engine) {
            this.engine = engine;
            this.functionToSource = new ConcurrentHashMap<>();
            this.command = Command.NONE;
        }

        private FunctionSource getFunctionSource(DebuggableScript fnOrScript) {
            int firstLine = -1;
            int[] lineNumbers = fnOrScript.getLineNumbers();
            if (lineNumbers != null && lineNumbers.length > 0) {
                firstLine = lineNumbers[0];
                for (int j = 1; j != lineNumbers.length; ++j) {
                    int line = lineNumbers[j];
                    if (line < firstLine) {
                        firstLine = line;
                    }
                }
            }

            SourceFile file = null;
            String sourceName = fnOrScript.getSourceName();
            if (sourceName == null || sourceName.equals(this.file.getFilePath())) {
                file = this.file;
            } else {
                file = getSourceFile(sourceName);
            }

            if (file == null) {
                file = this.file;
            }

            return new FunctionSource(file, firstLine, fnOrScript.getFunctionName());
        }

        @Override
        public void run() {
            while (true) {
                try {
                    synchronized (monitor) {
                        monitor.wait();
                    }
                } catch (InterruptedException e) {
                    continue;
                }
                if (readCommand() == Command.RUN) {
                    execute(file);
                }
            }
        }

        private synchronized Command readCommand() {
            Command command = this.command;
            this.command = Command.NONE;
            return command;
        }

        public synchronized void startRun(SourceFile file) {
            this.file = file;
            this.command = Command.RUN;
            synchronized (monitor) {
                monitor.notify();
            }
        }

        private void execute(SourceFile file) {
            functionToSource.clear();

            org.mozilla.javascript.debug.Debugger debugger = new org.mozilla.javascript.debug.Debugger() {
                @Override
                public void handleCompilationDone(Context cx, DebuggableScript fnOrScript, String source) {
                }

                @Override
                public DebugFrame getFrame(Context cx, DebuggableScript fnOrScript) {
                    FunctionSource item = functionToSource.get(fnOrScript);
                    if (item == null) {
                        item = getFunctionSource(fnOrScript);
                        functionToSource.put(fnOrScript, item);
                    }
                    return new StackFrame(cx, Debugger.this, item);
                }
            };

            Throwable exception = null;
            EditorTestContext testContext = new EditorTestContext(Path.getFileName(file.getFilePath()), 1);
            testContext.setWindowPosition(windowPosition);
            testContext.setResultNotifier(consoleOutputStream);
            engine.setConsole(consoleOutputStream);
            testResult = testContext.getTestResult();
            consoleOutputStream.setTestResult(testResult);
            engine.init(testContext);
            try {
                Context cx = engine.getContext();
                ContextData contextData = new ContextData();
                cx.setDebugger(debugger, contextData);
                cx.setGeneratingDebug(true);
                cx.setOptimizationLevel(-1);
                Object out = engine.runScript(file.getSource(), file.getFilePath());
                if (out instanceof ScriptException) {
                    throw (ScriptException)out;
                }
            } catch (Throwable t) {
                exception = t;
            } finally {
                if (exception == null && cleanupWhenDone) {
                    engine.finish();
                }
                synchronized (stateMonitor) {
                    isRunning = false;
                    isInterrupted = false;
                }
                callback.evalScriptStopped(exception);
            }
        }

        public void setOutput(ConsoleOutputStream console) {
            this.consoleOutputStream = console;
        }

        public void setWindowPosition(EditorTestContext.WindowPosition windowPosition) {
            this.windowPosition = windowPosition;
        }
    }
}
