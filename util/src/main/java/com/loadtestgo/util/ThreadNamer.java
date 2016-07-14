package com.loadtestgo.util;

import java.util.concurrent.ThreadFactory;

public class ThreadNamer implements ThreadFactory {
    final private String format;
    private int threadNum;

    public ThreadNamer(String format) {
        this.format = format;
        this.threadNum = 0;
    }

    @Override
    public Thread newThread(Runnable r) {
        synchronized (format) {
            threadNum++;
        }
        return new Thread(r, String.format(format, threadNum));
    }
}
