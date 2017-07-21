package com.loadtestgo.script.runner;

import com.loadtestgo.script.engine.TestContext;
import com.loadtestgo.script.runner.config.TestConfig;
import com.loadtestgo.util.StringUtils;
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
    private TestConfig testConfig;

    public void setWriteJUnitXmlFile(boolean writeJUnitXmlFile) {
        this.writeJUnitXmlFile = writeJUnitXmlFile;
    }

    public void startTests(TestConfig testConfig, File outputDir) {
        this.outputDir = outputDir;
        this.testConfig = testConfig;
        this.allTestsStart = System.currentTimeMillis();

        info(String.format("Saving results to '%s'...", outputDir.getAbsolutePath()));
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
                jUnitXmlWriter.writeResults(junitTestResultsFile, tests, allTestsStart, duration, testConfig);

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

        String testName;
        if (StringUtils.isSet(runnerTest.getName())) {
            testName = runnerTest.getName() + " (" + runnerTest.getFileName() + ")";
        } else {
            testName = runnerTest.getFileName();
        }

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
        System.out.println(ansi().fgBright(BLACK).a(outputFormatDate()).fg(WHITE).a(indent(message, 4, 11)).fg(DEFAULT));
        Logger.info("Test FAILED after" + duration);
        Logger.error(message);
    }

    private String indent(String message, int firstIndent, int indent) {
        String[] lines = message.split("\n");
        StringBuilder indented = new StringBuilder();
        for (int i = 0; i < lines.length; ++i) {
            int in = indent;
            if (i == 0) in = firstIndent;
            for (int j = 0; j < in; ++j) {
                indented.append(' ');
            }
            indented.append(lines[i]);
            if (i < lines.length - 1) {
                indented.append('\n');
            }
        }
        return indented.toString();
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

    public void info(String str) {
        System.out.println(ansi().fgBright(BLACK).a(outputFormatDate()).fg(DEFAULT).a(str));
    }
}
