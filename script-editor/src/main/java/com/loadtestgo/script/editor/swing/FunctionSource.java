package com.loadtestgo.script.editor.swing;

public class FunctionSource {
    private SourceFile sourceFile;
    private int firstLine;
    private String name;

    public FunctionSource(SourceFile sourceFile, int firstLine, String name) {
        this.sourceFile = sourceFile;
        this.firstLine = firstLine;
        this.name = name;
    }

    public SourceFile sourceFile() {
        return sourceFile;
    }

    public int firstLine() {
        return firstLine;
    }

    public String name() {
        return name;
    }
}
