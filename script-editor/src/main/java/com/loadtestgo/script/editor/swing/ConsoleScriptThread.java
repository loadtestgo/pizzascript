package com.loadtestgo.script.editor.swing;

import com.loadtestgo.script.editor.swing.debug.EvalTask;
import com.loadtestgo.script.editor.swing.debug.InitTask;
import com.loadtestgo.script.editor.swing.debug.TabCompleteTask;
import com.loadtestgo.script.engine.*;


import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ConsoleScriptThread {
    private JavaScriptEngine engine;
    private ConsoleCallbacks callbacks;
    private ExecutorService executorService;
    private EditorTestContext testContext;

    public ConsoleScriptThread(ConsoleCallbacks callbacks, ConsoleOutputStream output) {
        this.engine = new JavaScriptEngine();
        this.callbacks = callbacks;
        this.executorService = Executors.newSingleThreadExecutor();
        this.testContext = new EditorTestContext("Console", 0);
        this.testContext.setResultNotifier(output);
        this.testContext.setCaptureVideo(false);
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

    public void setWindowPosition(EditorTestContext.WindowPosition windowPosition) {
        this.testContext.setWindowPosition(windowPosition);
    }
}
