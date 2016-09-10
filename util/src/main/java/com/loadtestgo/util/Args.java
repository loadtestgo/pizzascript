package com.loadtestgo.util;

public class Args {
    /**
     * Removes one or two leading dashes, useful when parsing command line
     * input.
     */
    public static String stripLeadingDashes(String arg) {
        if (arg == null) {
            return null;
        }

        if (arg.length() == 0) {
            return arg;
        }

        if (arg.charAt(0) == '-') {
            int pos = 1;
            if (arg.length() >= 2) {
                if (arg.charAt(1) == '-') {
                    pos = 2;
                }
            }
            return arg.substring(pos);
        }

        return arg;
    }
}
