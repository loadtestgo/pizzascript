package com.loadtestgo.script.engine.internal.api;

import com.loadtestgo.script.api.CSV;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;

public class CSVImpl implements CSV {
    private List<String> rows;
    private Map<String,Integer> columnNames = new HashMap<>();

    public class Row implements CSV.Row {
        private String values[];

        Row(String[] values) {
            this.values = values;
        }

        @Override
        public String get(String columnName) {
            if (columnName == null) {
                return null;
            }

            Integer i = CSVImpl.this.columnNames.get(columnName.toLowerCase());
            if (i == null || i >= values.length) {
                return null;
            }

            return values[i];
        }

        @Override
        public String get(int columnIndex) {
            if (columnIndex < values.length) {
                return values[columnIndex];
            } else {
                return null;
            }
        }

        @Override
        public int getNumColumns() {
            return values.length;
        }
    }

    public CSVImpl(File file) throws IOException {
        rows = parseCsvRowsIntoArray(new FileInputStream(file));

        if (rows.size() > 0) {
            List<String> columns = parseCsvRow(rows.get(0));
            for (int i = 0; i < columns.size(); ++i) {
                columnNames.put(columns.get(i).toLowerCase(), i);
            }
        }
    }

    @Override
    public int getNumRows() {
        return rows.size();
    }

    @Override
    public Row row(int row) {
        if (row >= 0 && row < rows.size()) {
            List<String> columns = parseCsvRow(rows.get(row));
            return new Row(columns.toArray(new String[0]));
        } else {
            return null;
        }
    }

    @Override
    public Row randomRow() {
        if (rows.size() < 2) {
            return null;
        }
        return row(1 + randomInt(rows.size() - 1));
    }

    @Override
    public Row randomRow(boolean header) {
        if (!header) {
            return row(randomInt(rows.size()));
        } else {
            return randomRow();
        }
    }

    @Override
    public String value(int rowIndex, int columnIndex) {
        Row row = row(rowIndex);
        if (row == null) {
            return null;
        } else if (columnIndex < row.getNumColumns() && columnIndex >= 0) {
            return row.get(columnIndex);
        } else {
            return null;
        }
    }

    @Override
    public String value(int rowIndex, String columnName) {
        Row row = row(rowIndex);
        if (row == null) {
            return null;
        } else {
            return row.get(columnName);
        }
    }

    @Override
    public String randomValue(int columnIndex) {
        Row row = randomRow();
        if (row == null) {
            return null;
        } else {
            return row.get(columnIndex);
        }
    }

    @Override
    public String randomValue(String columnName) {
        Row row = randomRow();
        if (row == null) {
            return null;
        } else {
            return row.get(columnName);
        }
    }

    @Override
    public List<String> getColumnNames() {
        return new ArrayList<>(columnNames.keySet());
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
