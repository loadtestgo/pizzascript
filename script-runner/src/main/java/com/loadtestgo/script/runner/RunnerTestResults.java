package com.loadtestgo.script.runner;

import org.pmw.tinylog.Logger;

import static org.fusesource.jansi.Ansi.Color.DEFAULT;
import static org.fusesource.jansi.Ansi.Color.GREEN;
import static org.fusesource.jansi.Ansi.Color.RED;
import static org.fusesource.jansi.Ansi.ansi;

public class RunnerTestResults {
    private int testsRan = 0;
    private int testsFailed = 0;
    private long testStart = 0;
    private long allTestsStart = 0;

    public void startTests() {
        allTestsStart = System.currentTimeMillis();
    }

    public void endTests() {
        long duration = System.currentTimeMillis() - allTestsStart;

        String finishTime = "Finished after " + (duration / 1000) + " s";
        System.out.println(finishTime);
        Logger.info(finishTime);

        String results;
        if (testsFailed > 0) {
            results = String.format("%d/%d tests failed", testsFailed, testsRan);
        } else {
            results = String.format("%d tests succeded", testsRan);
        }

        System.out.println(results);
        Logger.info(results);
    }

    public void startTest(final String filename) {
        testsRan++;

        System.out.print(filename + " ");
        System.out.flush();
        Logger.info("Starting test \'{}\'", filename);

        testStart = System.currentTimeMillis();
    }

    public void endTest(String result) {
        String duration = getTestDuration();
        System.out.println(ansi().fg(GREEN).a("[OK]").fg(DEFAULT).a(duration));
        Logger.info("Test OK after" + duration);
        Logger.info(result);
    }

    public void endTestFailed(final String message) {
        testsFailed++;

        String duration = getTestDuration();

        System.out.println(ansi().fg(RED).a("[FAILED]").fg(DEFAULT).a(duration));
        System.err.println(message);
        Logger.info("Test FAILED after" + duration);
        Logger.error(message);
    }

    private String getTestDuration() {
        long duration = System.currentTimeMillis() - testStart;
        testStart = 0;
        return " " + duration + "ms";
    }

    public int failedTestsCount() {
        return testsFailed;
    }

    public int testCount() {
        return testsRan;
    }
}
