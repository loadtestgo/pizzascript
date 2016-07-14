package com.loadtestgo.util;

import java.io.File;

public class Path {
    static public String join(String path1, String path2) {
        return new File(path1, path2).toString();
    }

    public static String join(String one, String two, String three) {
        return new File(new File(one, two), three).toString();
    }

    static public String getFileName(String filePath) {
        return new File(filePath).getName();
    }
}
