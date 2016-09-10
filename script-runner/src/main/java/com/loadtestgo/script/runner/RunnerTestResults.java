package com.loadtestgo.script.runner;

import com.loadtestgo.script.engine.TestContext;
import org.pmw.tinylog.Logger;

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.fusesource.jansi.Ansi.Color.*;
import static org.fusesource.jansi.Ansi.ansi;

public class RunnerTestResults {
    private int testsRan = 0;
    private int testsFailed = 0;
    private long allTestsStart = 0;
    private boolean writeJUnitXmlFile;
    private List<RunnerTestResult> tests = new ArrayList<>();
    private File outputDir;

    public void setWriteJUnitXmlFile(boolean writeJUnitXmlFile) {
        this.writeJUnitXmlFile = writeJUnitXmlFile;
    }

    public void startTests(File outputDir) {
        this.outputDir = outputDir;
        this.allTestsStart = System.currentTimeMillis();
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

        if (writeJUnitXmlFile) {
            try {
                File junitTestResultsFile = new File(outputDir, "junit.xml");

                JUnitXmlWriter jUnitXmlWriter = new JUnitXmlWriter();
                jUnitXmlWriter.writeResults(junitTestResultsFile, tests, allTestsStart, duration);

            } catch (FileNotFoundException e) {
                String error = String.format("Unable to write JUnit test results %s", e.getMessage());

                System.err.println(error);

                Logger.error(error);
            } catch (XMLStreamException e) {
                String error = String.format("Unable to write JUnit test results %s", e.getMessage());

                System.err.println(error);

                Logger.error("Unable to write JUnit test results", e);
            }
        }
    }

    public void startTest(RunnerTest runnerTest, TestContext testContext) {
        testsRan++;

        tests.add(new RunnerTestResult(runnerTest, testContext.getTestResult()));

        String testName = runnerTest.getName();

        System.out.print(ansi().fgBright(BLACK).a(outputFormatDate()).fg(DEFAULT).a(testName + " "));
        System.out.flush();
        Logger.info("Starting test \'{}\'", testName);
    }

    public void endTest(TestContext testContext, String result) {
        String duration = getTestDuration(testContext);
        System.out.println(ansi().fg(GREEN).a("[OK]").fgBright(BLACK).a(duration).fg(DEFAULT));
        Logger.info("Test OK after" + duration);
        Logger.info(result);
    }

    public void endTestFailed(TestContext testContext, final String message) {
        testsFailed++;

        String duration = getTestDuration(testContext);

        System.out.println(ansi().fg(RED).a("[FAILED]").fgBright(BLACK).a(duration));
        System.out.println(ansi().fgBright(BLACK).a(outputFormatDate()).fg(WHITE).a(message).fg(DEFAULT));
        Logger.info("Test FAILED after" + duration);
        Logger.error(message);
    }

    private String getTestDuration(TestContext testContext) {
        int duration = testContext.getTestResult().getRunTime();
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
