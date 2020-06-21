package com.loadtestgo.script.api;

import java.util.List;

public interface CSV {
    interface Row {
        String get(String columnName);
        String get(int columnIndex);
        int getNumColumns();
    }

    int getNumRows();
    int size();

    Row row(int row);
    Row randomRow();
    Row randomRow(boolean header);

    String value(int row, int column);
    String value(int row, String columnName);

    String randomValue(int column);
    String randomValue(String columnName);

    List<String> getColumnNames();
}
