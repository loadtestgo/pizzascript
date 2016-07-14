package com.loadtestgo.util;

import java.util.Arrays;

public class LongArray {

    private long[] data;
    private int size;

    public LongArray() {
        data = new long[32];
        size = 0;
    }

    public LongArray(int capacity) {
        data = new long[capacity];
        size = 0;
    }

    public void add(long val) {
        size += 1;
        if (size >= data.length) {
            long[] newData = new long[data.length * 2];
            for (int i = 0; i < data.length; ++i) {
                newData[i] = data[i];
            }
            data = newData;
        }
        data[size - 1] = val;
    }

    public int capacity() { return data.length; }

    public long[] data() {
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

    public long percentile(float v) {
        int index = (int)Math.floor(size * v);
        return data[index];
    }

    public long mean() {
        long mean = 0;
        if (size == 0) {
            return 0;
        }
        for (int i = 0; i < size; ++i) {
            mean += data[i];
        }
        return (int)(mean / size);
    }

    public long[] copyData() {
        return Arrays.copyOf(data, size);
    }
}
