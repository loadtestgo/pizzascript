package com.loadtestgo.script.editor.swing;

import com.loadtestgo.script.engine.*;

import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ConsoleScriptThread {
    private JavaScriptEngine engine;
    private ConsoleCallbacks callbacks;
    private ExecutorService executorService;
    private EasyTestContext testContext;

    public ConsoleScriptThread(ConsoleCallbacks callbacks,
                               ConsoleOutputStream output) {
        this.engine = new JavaScriptEngine();
        this.callbacks = callbacks;
        this.executorService = Executors.newSingleThreadExecutor();
        this.testContext = new EasyTestContext("Console");
        this.testContext.setResultNotifier(output);
        this.testContext.setSandboxJavaScript(EngineSettings.sandboxJavaScript());
        output.setTestResult(testContext.getTestResult());
        executorService.submit(new InitTask(engine, testContext, output));
    }

    public void evalSource(int lineNo, String source) {
        executorService.submit(new EvalTask(engine, lineNo, source, callbacks));
    }

    public void shutdown() {
        executorService.shutdownNow();
        testContext.cleanup();
    }

    public void tabComplete(String source, int pos) {
        executorService.submit(new TabCompleteTask(engine, source, pos, callbacks));
    }

    public JavaScriptEngine getJavaScriptEngine() {
        return engine;
    }

    public EasyTestContext getTestContext() {
        return testContext;
    }

    static private class InitTask implements Callable<Object> {
        private JavaScriptEngine engine;
        private TestContext testContext;
        private ConsoleOutputStream output;

        public InitTask(JavaScriptEngine engine,
                        TestContext testContext,
                        ConsoleOutputStream output) {
            this.engine = engine;
            this.testContext = testContext;
            this.output = output;
        }

        @Override
        public Object call() throws Exception {
            engine.setConsole(output);
            engine.init(testContext);
            return null;
        }
    }

    static private class EvalTask implements Callable<Object> {
        private JavaScriptEngine engine;
        private String source;
        private int lineNo;
        private ConsoleCallbacks callbacks;

        public EvalTask(JavaScriptEngine engine,
                        int lineNo,
                        String source,
                        ConsoleCallbacks callbacks) {
            this.engine = engine;
            this.lineNo = lineNo;
            this.source = source;
            this.callbacks = callbacks;
        }

        @Override
        public Object call() throws Exception {
            Object result = null;
            try {
                result = engine.runPartialScript(source, lineNo);
                callbacks.expressionResult(engine.valueToString(result));
            } catch (ScriptException se) {
                callbacks.scriptException(se);
            }

            return result;
        }
    }

    private class TabCompleteTask implements Callable<Object> {
        private JavaScriptEngine engine;
        private String source;
        private int insertPos;
        private ConsoleCallbacks callbacks;

        public TabCompleteTask(JavaScriptEngine engine,
                               String source,
                               int insertPos,
                               ConsoleCallbacks callbacks) {
            this.engine = engine;
            this.source = source;
            this.insertPos = insertPos;
            this.callbacks = callbacks;
        }

        @Override
        public Object call() throws Exception {
            ArrayList<JavaScriptEngine.CompletionGroup> completions = new ArrayList<>();
            int pos = engine.complete(source, insertPos, completions);
            callbacks.autoCompletions(source, pos, insertPos, completions);
            return null;
        }
    }
}
