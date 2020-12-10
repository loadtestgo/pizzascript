package com.loadtestgo.script.engine;

import org.pmw.tinylog.Logger;

import java.util.Timer;
import java.util.TimerTask;

public class InterruptTimer {
    private Timer timer;
    private Thread thread;
    private TestContext testContext;
    private long startTime;
    private long timeoutMS;
    private boolean stopped;

    public InterruptTimer(Timer timer, Thread thread, TestContext testContext, long startTime, long timeoutMS) {
        this.timer = timer;
        this.thread = thread;
        this.testContext = testContext;
        this.startTime = startTime;
        this.stopped = false;
        this.timeoutMS = timeoutMS;

        timer.schedule(newTimerTask(), timeoutMS);
    }

    public void checkTimerAndRescheduleIfNecessary() {
        synchronized (this) {
            long currentTime = System.currentTimeMillis();

            long runTime = currentTime - startTime;
            long timeLeft = timeoutMS + testContext.getBrowserOpenTime() - runTime;
            if (timeLeft <= 0) {
                int workerId = testContext.getUserContext().getWorkerId();
                Logger.info("Worker {}: Triggering interrupt after {}ms (runtime: {}ms, browser setup: {}ms)...", workerId, timeoutMS, runTime, testContext.getBrowserOpenTime());
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
