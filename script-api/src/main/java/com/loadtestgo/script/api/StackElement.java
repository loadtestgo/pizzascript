package com.loadtestgo.script.api;

public class StackElement {
    public String file;
    public String func;
    public int line;

    public void render(StringBuilder sb) {
        sb.append("\tat ").append(file);
        if (line > -1) {
            sb.append(':').append(line);
        }
        if (func != null) {
            sb.append(" (").append(func).append(')');
        }
    }
}
