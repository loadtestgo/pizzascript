package com.loadtestgo.script.engine.internal.api;

import com.loadtestgo.script.api.Assert;
import com.loadtestgo.script.api.ErrorType;
import com.loadtestgo.script.engine.JavaScriptEngine;
import com.loadtestgo.script.engine.ScriptException;

public class AssertImpl implements Assert {
    private JavaScriptEngine engine;

    public AssertImpl(JavaScriptEngine engine) {
        this.engine = engine;
    }

    @Override
    public void ok(Object object) {
        if (!JavaScriptEngine.eq(object, true)) {
            throw new ScriptException(ErrorType.Script,
                    String.format("assert.ok: '%s' did not evaluate to true",
                            engine.valueToString(object)));
        }
    }

    @Override
    public void fail() {
        throw new ScriptException(ErrorType.Script, "assert.fail()");
    }

    @Override
    public void fail(String message) {
        throw new ScriptException(ErrorType.Script, message);
    }

    @Override
    public void equal(Object o1, Object o2) {
        if (!JavaScriptEngine.eq(o1, o2)) {
            throw new ScriptException(ErrorType.Script,
                    String.format("assert.equal: '%s' not equal '%s'",
                            engine.valueToString(o1),
                            engine.valueToString(o2)));
        }
    }

    @Override
    public void eq(Object o1, Object o2) {
        if (!JavaScriptEngine.eq(o1, o2)) {
            throw new ScriptException(ErrorType.Script,
                    String.format("assert.eq: '%s' not equal '%s'",
                            engine.valueToString(o1),
                            engine.valueToString(o2)));
        }
    }

    @Override
    public void notEqual(Object o1, Object o2) {
        if (JavaScriptEngine.eq(o1, o2)) {
            throw new ScriptException(ErrorType.Script,
                    String.format("assert.notEqual: '%s' and '%s' are equal",
                            engine.valueToString(o1),
                            engine.valueToString(o2)));
        }
    }

    @Override
    public void ne(Object o1, Object o2) {
        if (JavaScriptEngine.eq(o1, o2)) {
            throw new ScriptException(ErrorType.Script,
                    String.format("assert.ne: '%s' and '%s' are equal",
                            engine.valueToString(o1),
                            engine.valueToString(o2)));
        }
    }
}
