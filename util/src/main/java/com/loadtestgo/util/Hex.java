package com.loadtestgo.util;

import java.nio.ByteBuffer;

public class Hex {
    final protected static char[] hexArray = "0123456789abcdef".toCharArray();

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int i = 0; i < bytes.length; i++) {
            int v = bytes[i] & 0xFF;
            hexChars[i * 2] = hexArray[v >>> 4];
            hexChars[i * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static String bytesToHex(byte[] bytes, int limit, int position) {
        if (limit <= position) {
            return null;
        }
        int size = limit - position;
        char[] hexChars = new char[size * 2];
        for (int i = 0; i < size; i++) {
            int v = bytes[i + position] & 0xFF;
            hexChars[i * 2] = hexArray[v >>> 4];
            hexChars[i * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static String bytesToHex(ByteBuffer buffer) {
        if (!buffer.hasRemaining()) {
            return null;
        }

        int limit = buffer.limit();
        int position = buffer.position();

        int size = limit - position;
        char[] hexChars = new char[size * 2];
        for (int i = 0; i < size; i++) {
            int v = buffer.get(i + position) & 0xFF;
            hexChars[i * 2] = hexArray[v >>> 4];
            hexChars[i * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static byte[] hexToBytes(String hex) {
        final int len = hex.length();

        if ((len & 0x01) != 0) {
            throw new IllegalArgumentException("Odd number of characters.");
        }

        final byte[] out = new byte[len >> 1];

        // two characters form the hex value.
        for (int i = 0, j = 0; j < len; i++) {
            int f = toDigit(hex.charAt(j), j) << 4;
            j++;
            f = f | toDigit(hex.charAt(j), j);
            j++;
            out[i] = (byte) (f & 0xFF);
        }

        return out;
    }

    private static int toDigit(char c, int index) {
        final int digit = Character.digit(c, 16);
        if (digit == -1) {
            throw new IllegalArgumentException("Illegal hexadecimal character " + c + " at index " + index);
        }
        return digit;
    }
}
