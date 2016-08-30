package com.loadtestgo.script.runner;

import org.omg.CORBA.BAD_CONTEXT;
import org.pmw.tinylog.Logger;

import java.text.SimpleDateFormat;
import java.util.Date;

import static org.fusesource.jansi.Ansi.Color.*;
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
        String results =
            String.format("%d/%d tests succeeded", testsRan - testsFailed, testsRan);

        System.out.print(ansi().fgBright(BLACK).a(outputFormatDate()));
        if (testsFailed == 0) {
            System.out.println(ansi().fg(GREEN).a(results).fg(DEFAULT));
        } else {
            System.out.println(ansi().fg(RED).a(results).fg(DEFAULT));
        }
        Logger.info(results);

        long duration = System.currentTimeMillis() - allTestsStart;
        String finishTime = "Finished after " + (duration / 1000) + " s";
        System.out.println(ansi().fgBright(BLACK).a(outputFormatDate()).fg(DEFAULT).a(finishTime));
        Logger.info(finishTime);
    }

    public void startTest(final String filename) {
        testsRan++;

        System.out.print(ansi().fgBright(BLACK).a(outputFormatDate()).fg(DEFAULT).a(filename + " "));
        System.out.flush();
        Logger.info("Starting test \'{}\'", filename);

        testStart = System.currentTimeMillis();
    }

    public void endTest(String result) {
        String duration = getTestDuration();
        System.out.println(ansi().fg(GREEN).a("[OK]").fgBright(BLACK).a(duration).fg(DEFAULT));
        Logger.info("Test OK after" + duration);
        Logger.info(result);
    }

    public void endTestFailed(final String message) {
        testsFailed++;

        String duration = getTestDuration();

        System.out.println(ansi().fg(RED).a("[FAILED]").fgBright(BLACK).a(duration));
        System.out.println(ansi().fgBright(BLACK).a(outputFormatDate()).fg(WHITE).a(message).fg(DEFAULT));
        Logger.info("Test FAILED after" + duration);
        Logger.error(message);
    }

    private String getTestDuration() {
        long duration = System.currentTimeMillis() - testStart;
        testStart = 0;
        return " " + duration + " ms";
    }

    public int failedTestsCount() {
        return testsFailed;
    }

    public int testCount() {
        return testsRan;
    }

    private static String outputFormatDate() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("[HH.mm.ss] ");
        return simpleDateFormat.format(new Date());
    }
}
