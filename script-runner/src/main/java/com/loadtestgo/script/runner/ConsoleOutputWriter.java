package com.loadtestgo.script.runner;

import com.loadtestgo.script.engine.ConsoleNotifier;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ConsoleOutputWriter implements ConsoleNotifier {
    private File file;
    private FileWriter writer;
    private boolean writeTimestamps;

    public ConsoleOutputWriter(File file) throws IOException {
        this.file = file;
    }

    public void setWriteTimestamps(boolean writeTimestamp) {
        this.writeTimestamps = writeTimestamp;
    }

    public void close() {
        try {
            if (writer != null) {
                writer.close();
            }
        } catch (IOException e) {
            // ignore
        }
    }

    @Override
    public void logInfo(String str) {
        write(" INFO", str);
    }

    @Override
    public void logWarn(String str) {
        write(" WARN", str);
    }

    @Override
    public void logError(String str) {
        write("ERROR", str);
    }

    private void write(String type, String str) {
        try {
            if (writer == null) {
                writer = new FileWriter(file);
            }
            if (writeTimestamps) {
                writer.append(String.format("[%s] %s: %s\n", consoleFormatDate(), type, str));
            } else {
                writer.append(str);
            }
        } catch (IOException e) {
            // ignore, we should have write permission, if not, oh well
        }
    }

    private static String consoleFormatDate() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH.mm.ss.SSS");
        return simpleDateFormat.format(new Date());
    }

    public boolean outputWritten() {
        return writer != null;
    }
}
