package com.loadtestgo.util;

import java.util.Arrays;

public class DoubleArray {
    private double[] data;
    private int size;

    public DoubleArray() {
        data = new double[32];
        size = 0;
    }

    public DoubleArray(int capacity) {
        data = new double[capacity];
        size = 0;
    }

    public void add(double val) {
        size += 1;
        if (size >= data.length) {
            double[] newData = new double[data.length * 2];
            for (int i = 0; i < data.length; ++i) {
                newData[i] = data[i];
            }
            data = newData;
        }
        data[size - 1] = val;
    }

    public double[] data() {
        return data;
    }

    public int size() {
        return size;
    }

    public void sort() {
        Arrays.sort(data, 0, size);
    }

    public void clear() {
        size = 0;
    }

    public double percentile(double v) {
        int index = (int)Math.floor(size * v);
        return data[index];
    }

    public double mean() {
        double mean = 0;
        if (size == 0) {
            return 0;
        }
        for (int i = 0; i < size; ++i) {
            mean += data[i];
        }
        return (mean / size);
    }
}
