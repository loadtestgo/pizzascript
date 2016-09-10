package com.loadtestgo.script.runner;

import com.loadtestgo.script.api.StackElement;
import com.loadtestgo.script.api.TestError;
import com.loadtestgo.script.api.TestResult;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


/**
 * TODO Allow the name to be set
 */
public class JUnitXmlWriter {
    private String testSuiteName = "PizzaScript";

    public void writeResults(File file,
                             List<RunnerTestResult> tests,
                             long startTime,
                             long duration) throws FileNotFoundException, XMLStreamException {
        XMLOutputFactory output = XMLOutputFactory.newFactory();

        XMLStreamWriter writer = output.createXMLStreamWriter(new FileOutputStream(file));
        writer.writeStartDocument("UTF-8", "1.0");
        writer.writeStartElement("testsuites");
        writer.writeStartElement("testsuite");
        Date time = new Date(startTime);

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("YYYY-MM-dd'T'HH:mm:ss");
        simpleDateFormat.format(time);
        writer.writeAttribute("timestamp", simpleDateFormat.format(time));
        writer.writeAttribute("package", testSuiteName);
        writer.writeAttribute("id", "0");
        writer.writeAttribute("name", testSuiteName);
        writer.writeAttribute("hostname", "localhost");
        writer.writeAttribute("skipped", "0");

        int failures = 0;
        int testsThatRan = tests.size();

        for (RunnerTestResult test : tests) {
            if (test.getResult().getError() != null) {
                failures++;
            }
        }

        writer.writeAttribute("tests", String.valueOf(testsThatRan));
        writer.writeAttribute("errors", "0");
        writer.writeAttribute("failures", String.valueOf(failures));
        writer.writeAttribute("time",  formatDurationMS(duration));

        for (RunnerTestResult test : tests) {
            RunnerTest runnerTest = test.getTest();
            TestResult testResult = test.getResult();

            writer.writeStartElement("testcase");
            writer.writeAttribute("name", runnerTest.getName());
            writer.writeAttribute("classname", testSuiteName);
            writer.writeAttribute("time", formatDurationMS(testResult.getRunTime()));

            TestError testError = testResult.getError();
            if (testError != null) {
                writer.writeStartElement("error");

                writer.writeAttribute("message", testError.message);
                writer.writeAttribute("type", testError.type.name());

                writer.writeCharacters(getStackTrace(testError.stackTrace));

                writer.writeEndElement();
            }

            if (testResult.output != null ||
                runnerTest.getScreenshotFilePath() != null ||
                runnerTest.getHarFilePath() != null ||
                runnerTest.getConsoleLogFilePath() != null) {
                writer.writeStartElement("system-out");
                for (TestResult.OutputMessage outputMessage : testResult.output) {
                    writer.writeCharacters(outputMessage.msg);
                    writer.writeCharacters("\n");

                }

                writeAttachmentIfNecessary(writer, runnerTest.getScreenshotFilePath());
                writeAttachmentIfNecessary(writer, runnerTest.getHarFilePath());
                writeAttachmentIfNecessary(writer, runnerTest.getConsoleLogFilePath());

                writer.writeEndElement();
            }

            writer.writeEndElement();
        }

        writer.writeEndElement();
        writer.writeEndElement();
        writer.writeEndDocument();
        writer.close();
    }

    private void writeAttachmentIfNecessary(XMLStreamWriter writer, String path) throws XMLStreamException {
        if (path != null) {
            writer.writeCharacters(String.format("[[ATTACHMENT|%s]]\n", path));
        }
    }

    private String formatDurationMS(long duration) {
        return String.valueOf(((double)duration) / 1000);
    }

    private String getStackTrace(List<StackElement> stackTrace) {
        StringBuilder s = new StringBuilder();
        for (StackElement stackElement : stackTrace) {
            stackElement.render(s);
            s.append("\n");
        }
        return s.toString();
    }
}
