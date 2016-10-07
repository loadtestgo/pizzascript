package com.loadtestgo.util;

import java.io.File;

public class Dirs {
    protected static String rootDir;
    protected static String tmpDir;

    public static String getRoot() {
        if (rootDir == null) {
            rootDir = System.getProperty("user.dir");
        }
        return rootDir;
    }

    public static String getTmp() {
        if (tmpDir == null) {
            tmpDir = Path.join(getRoot(), "tmp");
            File file = new File(tmpDir);
            if (!file.exists()) {
                file.mkdirs();
            }
        }
        return tmpDir;
    }
}
