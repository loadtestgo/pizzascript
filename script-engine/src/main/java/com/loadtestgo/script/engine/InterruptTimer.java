package com.loadtestgo.script.engine;

import org.pmw.tinylog.Logger;

import java.util.Timer;
import java.util.TimerTask;

public class InterruptTimer {
    private Timer timer;
    private Thread thread;
    private TestContext testContext;
    private long startTime;
    private long timeout;
    private boolean stopped;

    public InterruptTimer(Timer timer, Thread thread, TestContext testContext, long startTime, long timeout) {
        this.timer = timer;
        this.thread = thread;
        this.testContext = testContext;
        this.startTime = startTime;
        this.stopped = false;
        this.timeout = timeout;

        timer.schedule(newTimerTask(), timeout);
    }

    public void checkTimerAndRescheduleIfNecessary() {
        synchronized (this) {
            long currentTime = System.currentTimeMillis();

            long runTime = currentTime - startTime;
            long timeLeft = timeout + testContext.getBrowserOpenTime() - runTime;
            if (timeLeft <= 0) {
                int workerId = testContext.getUserContext().getWorkerId();
                Logger.info("Worker {}: Triggering interrupt after {}ms (runtime: {}ms, browser setup: {}ms)...", workerId, timeout, runTime, testContext.getBrowserOpenTime());
                thread.interrupt();
                stopped = true;
            } else {
                timer.schedule(newTimerTask(), timeLeft);
            }
        }
    }

    private TimerTask newTimerTask() {
        return new TimerTask() {
            @Override
            public void run() {
                checkTimerAndRescheduleIfNecessary();
            }
        };
    }

    public boolean isStopped() {
        synchronized (this) {
            return stopped;
        }
    }
}
