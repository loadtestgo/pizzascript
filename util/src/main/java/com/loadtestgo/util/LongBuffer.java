package com.loadtestgo.util;

public class LongBuffer {
    private long[] data;
    private int size;
    private int pos;

    public LongBuffer() {
        data = new long[32];
        size = 0;
        pos = -1;
    }

    public LongBuffer(int capacity) {
        data = new long[capacity];
        size = 0;
        pos = -1;
    }

    public void add(long val) {
        pos++;

        if (pos == data.length) {
            pos = 0;
        }

        if (pos >= size) {
            size++;
        }

        data[pos] = val;
    }

    public long[] data() {
        return data;
    }

    public int size() {
        return size;
    }

    public int capacity() { return data.length; }

    public int begin() {
        if (size < data.length) {
            return 0;
        } else if (pos + 1 == size) {
            return 0;
        } else {
            return pos + 1;
        }
    }

    public int end() {
        return pos;
    }

    public long beginValue() {
        return data[begin()];
     }

    public long endValue() {
        return data[end()];
    }
}
