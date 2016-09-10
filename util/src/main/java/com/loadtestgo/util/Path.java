package com.loadtestgo.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

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

    static public File getCurrentWorkingDirectory() {
        return Paths.get(".").toAbsolutePath().normalize().toFile();
    }

    public static File getParentDirectory(String filePath) {
        return getParentDirectory(new File(filePath));
    }

    public static File getParentDirectory(File scriptFile) {
        File absolute = null;
        try {
            absolute = scriptFile.getCanonicalFile();
        } catch (IOException e) {
            absolute = scriptFile.getAbsoluteFile();
        }
        return absolute.getParentFile();
    }

    public static String getCanonicalPath(File file) {
        return getCanonicalFile(file).getPath();
    }

    public static File getCanonicalFile(File file) {
        File absolute = null;
        try {
            absolute = file.getCanonicalFile();
        } catch (IOException e) {
            absolute = file.getAbsoluteFile();
        }
        return absolute;
    }
}
