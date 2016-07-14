package com.loadtestgo.util;

public class Os {
    private enum OS {
        Mac,
        Linux,
        Windows,
        Unsupported
    }

    static private OS os = getOS();

    public static OS getOS() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.startsWith("mac")) {
            return OS.Mac;
        } else if (os.startsWith("windows")) {
            return OS.Windows;
        } else if (os.startsWith("linux")) {
            return OS.Linux;
        }
        return OS.Unsupported;
    }

    public static boolean isMac() {
        return os == OS.Mac;
    }

    public static boolean isWin() {
        return os == OS.Windows;
    }

    public static boolean isLinux() {
        return os == OS.Linux;
    }
}
