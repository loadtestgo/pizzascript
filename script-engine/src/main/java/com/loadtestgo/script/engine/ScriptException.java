package com.loadtestgo.script.engine;

import com.loadtestgo.script.api.ErrorType;
import com.loadtestgo.script.api.StackElement;
import com.loadtestgo.script.api.TestError;
import com.loadtestgo.script.api.TestResult;

import java.util.List;

public class ScriptException extends RuntimeException {
    private ErrorType errorType = ErrorType.Script;
    private String file;
    private int line;
    private int column;
    private List<StackElement> stackTrace;

    public ScriptException(String message)
    {
        super(message);
    }

    public ScriptException(ErrorType errorType, String message) {
        super(message);
        this.errorType = errorType;
    }

    public ScriptException(String message, String file, int line)
    {
        super(message);
        this.file = file;
        this.line = line;
    }

    public ScriptException(ErrorType errorType, String message,
                           String file, int line, int column,
                           List<StackElement> stackTrace)
    {
        super(message);
        this.errorType = errorType;
        this.file = file;
        this.line = line;
        this.column = column;
        this.stackTrace = stackTrace;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public String getFile() {
        return file;
    }

    public void setLine(int num) {
        this.line = num;
    }

    public int getLine() {
        return line;
    }

    public void setColumn(int column) {
        this.column = column;
    }

    public int getColumn() {
        return column;
    }

    public void setJSStackTrace(List<StackElement> stackTrace) {
        this.stackTrace = stackTrace;
    }

    public List<StackElement> getJSStackTrace() {
        return stackTrace;
    }

    public ErrorType getErrorType() {
        return errorType;
    }

    public void setErrorType(ErrorType errorType) {
        this.errorType = errorType;
    }

    public void populateResult(TestResult result) {
        TestError error = new TestError();
        error.type = getErrorType();
        error.message = getMessage();
        error.line = getLine();
        error.stackTrace = getJSStackTrace();
        error.file = getFile();
        result.setError(error);
    }

    public String prettyJSStackTrace() {
        StringBuilder buffer = new StringBuilder();
        String lineSeparator = System.getProperty("line.separator");
        for (StackElement elem : stackTrace) {
            elem.render(buffer);
            buffer.append(lineSeparator);
        }
        return buffer.toString();
    }
}
