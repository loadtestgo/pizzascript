package com.loadtestgo.util;

public class MathUtils {
    public static int divideRoundUp(int num, int div) {
        return (num + (div - 1)) / div;
    }
}
