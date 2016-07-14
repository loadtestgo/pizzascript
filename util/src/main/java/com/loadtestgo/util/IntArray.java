package com.loadtestgo.util;

import java.util.Arrays;

public class IntArray {
    private int[] data;
    private int size;

    public IntArray() {
        data = new int[32];
        size = 0;
    }

    public IntArray(int capacity) {
        data = new int[capacity];
        size = 0;
    }

    public void add(int val) {
        size += 1;
        if (size >= data.length) {
            int[] newData = new int[data.length * 2];
            for (int i = 0; i < data.length; ++i) {
                newData[i] = data[i];
            }
            data = newData;
        }
        data[size - 1] = val;
    }

    public int[] data() {
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

    public int percentile(float v) {
        int index = (int)Math.floor(size * v);
        return data[index];
    }

    public int mean() {
        long mean = 0;
        if (size == 0) {
            return 0;
        }
        for (int i = 0; i < size; ++i) {
            mean += data[i];
        }
        return (int)(mean / size);
    }

    public int[] copyData() {
        return Arrays.copyOf(data, size);
    }
}