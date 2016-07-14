package com.loadtestgo.script.engine;

import org.pmw.tinylog.Logger;

import java.util.TimerTask;

public class InterruptTimer extends TimerTask {
    Thread thread;
    boolean stopped;
    boolean disabled;

    public InterruptTimer(Thread thread) {
        this.thread = thread;
        this.stopped = false;
        this.disabled = false;
    }

    @Override
    public void run() {
        synchronized (this) {
            if (!disabled) {
                Logger.info("Triggering interrupt...");
                thread.interrupt();
                stopped = true;
            }
        }
    }

    public boolean isStopped() {
        synchronized (this) {
            return stopped;
        }
    }

    public void setDisabled(boolean disable) {
        synchronized (this) {
            this.disabled = disable;
        }
    }

    public boolean isDisabled() {
        synchronized (this) {
            return disabled;
        }
    }
}
