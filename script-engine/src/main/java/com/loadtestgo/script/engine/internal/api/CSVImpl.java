package com.loadtestgo.script.engine.internal.api;

import com.loadtestgo.script.api.CSV;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class CSVImpl implements CSV {
    private List<String> rows;

    public CSVImpl(File file) throws IOException {
        rows = parseCsvRowsIntoArray(new FileInputStream(file));
    }

    @Override
    public int getLength() {
        return rows.size();
    }

    @Override
    public String[] row(int row) {
        if (row >= 0 && row < rows.size()) {
            List<String> columns = parseCsvRow(rows.get(row));
            return columns.toArray(new String[columns.size()]);
        } else {
            return null;
        }
    }

    @Override
    public String[] randomRow() {
        return row(randomInt(rows.size()));
    }

    @Override
    public String value(int row, int column) {
        String[] columns = row(row);
        if (columns == null) {
            return null;
        } else if (column < columns.length && column >= 0) {
            return columns[column];
        } else {
            return null;
        }
    }

    @Override
    public String randomValue(int column) {
        String[] columns = randomRow();
        if (columns == null) {
            return null;
        } else if (column < columns.length && column >= 0) {
            return columns[column];
        } else {
            return null;
        }
    }

    enum ParseState {
        START,
        INQUOTES,
        INQUOTE_ESCAPE,
        NOQUOTES
    }


    enum ParseAllState {
        START,
        INQUOTES,
        INQUOTE_ESCAPE,
        START_SKIP_R, START_SKIP_N, NOQUOTES
    }

    static List<String> parseCsvRowsIntoArray(FileInputStream file) throws IOException {
        BufferedReader reader = new BufferedReader(
            new InputStreamReader(
                file,
                Charset.forName("UTF-8")));

        return parseCsvRowsIntoArray(reader);
    }

    static List<String> parseCsvRowsIntoArray(BufferedReader reader) throws IOException {
        ArrayList<String> rows = new ArrayList<>();

        int charactorAsInt;
        ParseAllState parseState = ParseAllState.START;
        StringBuilder val = new StringBuilder();

        while((charactorAsInt = reader.read()) != -1) {
            char c = (char)charactorAsInt;
            switch (parseState) {
                case START:
                case START_SKIP_N:
                case START_SKIP_R:
                    if (c == '\"') {
                        parseState = ParseAllState.INQUOTES;
                    } else if (c == '\n') {
                        if (parseState != ParseAllState.START_SKIP_N) {
                            rows.add(val.toString());
                            val = new StringBuilder();
                            parseState = ParseAllState.START_SKIP_R;
                        } else {
                            parseState = ParseAllState.START;
                        }
                    } else if (c == '\r') {
                        if (parseState != ParseAllState.START_SKIP_R) {
                            rows.add(val.toString());
                            val = new StringBuilder();
                            parseState = ParseAllState.START_SKIP_N;
                        } else {
                            parseState = ParseAllState.START;
                        }
                    } else {
                        parseState = ParseAllState.NOQUOTES;
                        val.append(c);
                    }
                    break;
                case NOQUOTES:
                    if (c == ',') {
                        val.append(c);
                        parseState = ParseAllState.START;
                    } else if (c == '\n') {
                        rows.add(val.toString());
                        val = new StringBuilder();
                        parseState = ParseAllState.START_SKIP_R;
                    } else if (c == '\r') {
                        rows.add(val.toString());
                        val = new StringBuilder();
                        parseState = ParseAllState.START_SKIP_N;
                    } else {
                        val.append(c);
                    }
                    break;
                case INQUOTES:
                    if (c == '\"') {
                        parseState = ParseAllState.INQUOTE_ESCAPE;
                    } else {
                        val.append(c);
                    }
                    break;
                case INQUOTE_ESCAPE:
                    if (c == ',') {
                        val.append(c);
                        parseState = ParseAllState.START;
                    } else if (c == '\n') {
                        rows.add(val.toString());
                        val = new StringBuilder();
                        parseState = ParseAllState.START_SKIP_R;
                    } else if (c == '\r') {
                        rows.add(val.toString());
                        val = new StringBuilder();
                        parseState = ParseAllState.START_SKIP_N;
                    } else {
                        val.append(c);
                        parseState = ParseAllState.INQUOTES;
                    }
                    break;
            }
        }

        if (val.length() > 0) {
            rows.add(val.toString());
        }

        return rows;
    }

    static List<String> parseCsvRow(String line) {
        ArrayList<String> columns = new ArrayList<>();
        StringBuilder val = new StringBuilder();
        ParseState parseState = ParseState.START;
        for (int i = 0; i < line.length(); ++i) {
            char c = line.charAt(i);
            switch (parseState) {
                case START:
                    if (c == '\"') {
                        parseState = ParseState.INQUOTES;
                    } else if (c == ',') {
                        columns.add(val.toString());
                        val = new StringBuilder();
                    } else {
                        parseState = ParseState.NOQUOTES;
                        val.append(c);
                    }
                    break;
                case NOQUOTES:
                    if (c == ',') {
                        columns.add(val.toString());
                        val = new StringBuilder();
                        parseState = ParseState.START;
                    } else {
                        val.append(c);
                    }
                    break;
                case INQUOTES:
                    if (c == '\"') {
                        parseState = ParseState.INQUOTE_ESCAPE;
                    } else {
                        val.append(c);
                    }
                    break;
                case INQUOTE_ESCAPE:
                    if (c == ',') {
                        columns.add(val.toString());
                        val = new StringBuilder();
                        parseState = ParseState.START;
                    } else {
                        val.append(c);
                        parseState = ParseState.INQUOTES;
                    }
                    break;
            }
        }
        columns.add(val.toString());
        return columns;
    }

    static int randomInt(int max) {
        if (max <= 0) {
            return 0;
        }
        int r = (int)Math.floor(Math.random() * max);
        if (r == max) {
            return max - 1;
        }
        return r;
    }
}
