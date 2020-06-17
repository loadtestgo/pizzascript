package com.loadtestgo.script.editor.swing;

import com.loadtestgo.script.engine.internal.rhino.RhinoContextFactory;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.EvaluatorException;
import org.mozilla.javascript.debug.DebugFrame;
import org.mozilla.javascript.debug.DebuggableScript;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CodeModel {
    private final Map<String, SourceFile> sourceFiles = new ConcurrentHashMap<>();

    public SourceFile newFile(String source, String filePath) {
        SourceFile file = new SourceFile(source, filePath);
        sourceFiles.put(filePath, file);
        if (source != null && source.length() > 0) {
            compileScript(file);
        }
        return file;
    }

    public List<String> functionNames() {
        synchronized (sourceFiles) {
            ArrayList<String> functions = new ArrayList<String>();
            for (SourceFile sourceFile : sourceFiles.values()) {
                for (int i = 0; i < sourceFile.functionSourcesTop(); ++i) {
                    functions.add(sourceFile.functionSource(i).name());
                }
            }
            return functions;
        }
    }

    public FunctionSource functionSourceByName(String functionName) {
        synchronized (sourceFiles) {
            for (SourceFile sourceFile : sourceFiles.values()) {
                for (int i = 0; i < sourceFile.functionSourcesTop(); ++i) {
                    FunctionSource functionSource = sourceFile.functionSource(i);
                    if (functionSource.name().equals(functionName)) {
                        return functionSource;
                    }
                }
            }
            return null;
        }
    }

    public void clearAllBreakpoints() {
        for (SourceFile sourceFile : sourceFiles.values()) {
            sourceFile.removeAllBreakpoints();
        }
    }

    public SourceFile getSourceFile(String filePath) {
        synchronized (sourceFiles) {
            return sourceFiles.get(filePath);
        }
    }

    public void updateFilePath(SourceFile sourceFile) {
        synchronized (sourceFiles) {
            String key = null;
            for (Map.Entry<String, SourceFile> entry : sourceFiles.entrySet()) {
                if (entry.getValue() == sourceFile) {
                    key = entry.getKey();
                    break;
                }
            }
            if (key != null) {
                sourceFiles.remove(key);
            }
            sourceFiles.put(sourceFile.getFilePath(), sourceFile);
        }
    }

    private String getFilePath(DebuggableScript debuggableScript) {
        String filePath = debuggableScript.getSourceName();
        if (filePath == null) {
            return null;
        }

        // Not to produce window for eval from different lines,
        // strip line numbers, i.e. replace all #[0-9]+\(eval\) by
        // (eval)
        // Option: similar treatment for Function?
        char evalSeparator = '#';
        StringBuffer sb = null;
        int urlLength = filePath.length();
        int cursor = 0;
        for (;;) {
            int searchStart = filePath.indexOf(evalSeparator, cursor);
            if (searchStart < 0) {
                break;
            }
            String replace = null;
            int i = searchStart + 1;
            while (i != urlLength) {
                int c = filePath.charAt(i);
                if (!('0' <= c && c <= '9')) {
                    break;
                }
                ++i;
            }
            if (i != searchStart + 1) {
                // i points after #[0-9]+
                if ("(eval)".regionMatches(0, filePath, i, 6)) {
                    cursor = i + 6;
                    replace = "(eval)";
                }
            }
            if (replace == null) {
                break;
            }
            if (sb == null) {
                sb = new StringBuffer();
                sb.append(filePath.substring(0, searchStart));
            }
            sb.append(replace);
        }
        if (sb != null) {
            if (cursor != urlLength) {
                sb.append(filePath.substring(cursor));
            }
            filePath = sb.toString();
        }
        return filePath;
    }

    public class ErrorMessage {
        public int line;
        public String message;
    }

    public ErrorMessage compileScript(SourceFile sourceFile) {
        RhinoContextFactory contextFactory = new RhinoContextFactory();
        final SourceFile finalFile = sourceFile;
        org.mozilla.javascript.debug.Debugger debugger = new org.mozilla.javascript.debug.Debugger() {
            @Override
            public void handleCompilationDone(Context cx, DebuggableScript fnOrScript, String source) {
                if (fnOrScript.isTopLevel()) {
                    finalFile.updateFromNewCompilation(fnOrScript);
                }
            }

            @Override
            public DebugFrame getFrame(Context cx, DebuggableScript fnOrScript) {
                return null;
            }
        };

        Context cx = contextFactory.enterContext();
        try {
            cx.setDebugger(debugger, null);
            cx.setGeneratingDebug(true);
            cx.setOptimizationLevel(-1);
            cx.compileString(finalFile.getSource(), finalFile.getFilePath(), 1, null);
        } catch (EvaluatorException ex) {
            ErrorMessage errorMessage = new ErrorMessage();
            errorMessage.line = ex.lineNumber();
            errorMessage.message = ex.details();
            return errorMessage;
        } finally {
            cx.exit();
        }
        return null;
    }
}
