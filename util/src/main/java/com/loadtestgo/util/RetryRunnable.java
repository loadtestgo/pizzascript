package com.loadtestgo.util;

import org.pmw.tinylog.Logger;

public abstract class RetryRunnable implements Runnable {
    @Override
    public void run() {
        boolean bInterrupted = false;
        while (!bInterrupted) {
            try {
                runSafely();
            } catch (InterruptedException e) {
                Logger.error(e, "{}: interrupted, exiting...", Thread.currentThread().getName());
                bInterrupted = true;
            } catch (Throwable t) {
                Logger.error(t, "{}: Caught exception and retrying...", Thread.currentThread().getName());
            }
        }
    }

    public abstract void runSafely() throws InterruptedException;
}
