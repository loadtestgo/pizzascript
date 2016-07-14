package com.loadtestgo.script.editor.swing;

import org.mozilla.javascript.ObjArray;
import org.mozilla.javascript.debug.DebuggableScript;

import java.util.HashSet;
import java.util.Set;

public class SourceFile {
    private static final boolean[] EMPTY_BOOLEAN_ARRAY = new boolean[0];
    private String source;
    private String filePath;
    private boolean[] breakableLines;
    private final Set<Integer> breakpoints;
    private FunctionSource[] functionSources;
    private boolean upToDate;
    private boolean newFile;
    private boolean modified;

    public SourceFile(String source, String filePath) {
        this.source = source;
        this.filePath = filePath;
        this.upToDate = false;
        this.breakpoints = new HashSet<>();
        this.modified = false;
    }

    public void update(String text) {
        source = text;
        upToDate = false;
    }

    public void updateFromNewCompilation(DebuggableScript debugInfo) {
        DebuggableScript[] functions = getAllFunctions(debugInfo);

        int N = functions.length;
        int[][] lineArrays = new int[N][];
        for (int i = 0; i != N; ++i) {
            lineArrays[i] = functions[i].getLineNumbers();
        }

        int minAll = 0;
        int maxAll = -1;
        int[] firstLines = new int[N];
        for (int i = 0; i != N; ++i) {
            int[] lines = lineArrays[i];
            if (lines == null || lines.length == 0) {
                firstLines[i] = -1;
            } else {
                int min, max;
                min = max = lines[0];
                for (int j = 1; j != lines.length; ++j) {
                    int line = lines[j];
                    if (line < min) {
                        min = line;
                    } else if (line > max) {
                        max = line;
                    }
                }
                firstLines[i] = min;
                if (minAll > maxAll) {
                    minAll = min;
                    maxAll = max;
                } else {
                    if (min < minAll) {
                        minAll = min;
                    }
                    if (max > maxAll) {
                        maxAll = max;
                    }
                }
            }
        }

        if (minAll > maxAll) {
            // No line information
            this.breakableLines = EMPTY_BOOLEAN_ARRAY;
        } else {
            if (minAll < 0) {
                // Line numbers can not be negative
                throw new IllegalStateException(String.valueOf(minAll));
            }
            int linesTop = maxAll + 1;
            this.breakableLines = new boolean[linesTop];
            for (int i = 0; i != N; ++i) {
                int[] lines = lineArrays[i];
                if (lines != null && lines.length != 0) {
                    for (int j = 0; j != lines.length; ++j) {
                        int line = lines[j];
                        this.breakableLines[line] = true;
                    }
                }
            }
        }

        this.functionSources = new FunctionSource[N];
        for (int i = 0; i != N; ++i) {
            String name = functions[i].getFunctionName();
            if (name == null) {
                name = "";
            }
            this.functionSources[i] = new FunctionSource(this, firstLines[i], name);
        }

        this.upToDate = true;
    }

    public String getSource() {
        return this.source;
    }

    public String getFilePath() {
        return this.filePath;
    }

    public int functionSourcesTop() {
        if (functionSources == null) {
            return 0;
        }
        return functionSources.length;
    }

    public FunctionSource functionSource(int i) {
        return functionSources[i];
    }

    public boolean breakableLine(int line) {
        synchronized (breakpoints) {
            if (breakableLines == null) {
                return false;
            }

            if (line >= breakableLines.length) {
                return false;
            }

            if (line < 0) {
                return false;
            }

            return breakableLines[line];
        }
    }

    public boolean isBreakpoint(int line) {
        synchronized (breakpoints) {
            return breakpoints.contains(Integer.valueOf(line));
        }
    }

    public boolean setBreakpoint(int line, boolean value) {
        synchronized (breakpoints) {
            Integer val = Integer.valueOf(line);

            if (value) {
                return breakpoints.add(val);
            } else {
                return breakpoints.remove(val);
            }
        }
    }

    public void removeAllBreakpoints() {
        synchronized (breakpoints) {
            breakpoints.clear();
        }
    }

    public static DebuggableScript[] getAllFunctions(DebuggableScript function) {
        ObjArray functions = new ObjArray();
        collectFunctions(function, functions);
        DebuggableScript[] result = new DebuggableScript[functions.size()];
        functions.toArray(result);
        return result;
    }

    private static void collectFunctions(DebuggableScript function,
                                         ObjArray array) {
        array.add(function);
        for (int i = 0; i != function.getFunctionCount(); ++i) {
            collectFunctions(function.getFunction(i), array);
        }
    }

    public boolean isUpToDate() {
        return upToDate;
    }

    public void setUpToDate(boolean upToDate) {
        this.upToDate = upToDate;
    }

    public boolean isNewFile() {
        return newFile;
    }

    public void setNewFile(boolean newFile) {
        this.newFile = newFile;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public boolean isModified() {
        return modified;
    }

    public void setIsModified(boolean modified) {
        this.modified = modified;
    }

    public void moveBreakpoints(int lineStart, int changed) {
        synchronized (breakpoints) {
            if (changed > 0) {
                HashSet<Integer> copy = new HashSet<>();
                for (Integer i : breakpoints) {
                    if (i > lineStart) {
                        copy.add(i + changed);
                    } else {
                        copy.add(i);
                    }
                }
                breakpoints.clear();
                breakpoints.addAll(copy);
            } else if (changed < 0) {
                HashSet<Integer> copy = new HashSet<>();
                for (Integer i : breakpoints) {
                    if (i > lineStart - changed) {
                        copy.add(i + changed);
                    } else if (i < lineStart) {
                        copy.add(i);
                    }
                }
                breakpoints.clear();
                breakpoints.addAll(copy);
            }
        }
    }
}
