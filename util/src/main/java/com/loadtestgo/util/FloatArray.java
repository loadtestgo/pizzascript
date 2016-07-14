package com.loadtestgo.util;

import java.util.Arrays;

public class FloatArray {
    private float[] data;
    private int size;

    public FloatArray() {
        data = new float[32];
        size = 0;
    }

    public FloatArray(int capacity) {
        data = new float[capacity];
        size = 0;
    }

    public void add(float val) {
        size += 1;
        if (size >= data.length) {
            float[] newData = new float[data.length * 2];
            for (int i = 0; i < data.length; ++i) {
                newData[i] = data[i];
            }
            data = newData;
        }
        data[size - 1] = val;
    }

    public float[] data() {
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

    public float percentile(float v) {
        int index = (int)Math.floor(size * v);
        return data[index];
    }

    public float mean() {
        float mean = 0;
        if (size == 0) {
            return 0;
        }
        for (int i = 0; i < size; ++i) {
            mean += data[i];
        }
        return (mean / size);
    }
}
