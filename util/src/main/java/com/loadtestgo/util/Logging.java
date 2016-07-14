package com.loadtestgo.util;

import java.util.logging.Level;

public class Logging {
    public static void disable(Class... classes) {
        for (Class c : classes) {
            disable(c.getName());
        }
    }

    public static void disable(String name) {
        java.util.logging.Logger logger = java.util.logging.Logger.getLogger(name);
        logger.setLevel(Level.OFF);
    }
}
