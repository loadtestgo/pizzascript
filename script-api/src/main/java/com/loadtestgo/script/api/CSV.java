package com.loadtestgo.script.api;

public interface CSV {
    int getLength();

    String[] row(int row);
    String[] randomRow();

    String value(int row, int column);
    String randomValue(int column);
}
