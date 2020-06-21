package com.loadtestgo.script.tester.server;

import java.io.IOException;
import java.io.InputStream;

import static java.nio.charset.StandardCharsets.UTF_8;

public class InputStreamReader {
    private InputStream stream;
    private final static int BUFFER_SIZE = 8192;
    private byte[] buf;
    private int start;
    private int end;

    public InputStreamReader(InputStream stream) {
        this.stream = stream;
        this.buf = new byte[BUFFER_SIZE];
        this.start = 0;
        this.end = 0;
    }

    public String readLine() throws IOException {
        fill();

        if (end < 0) {
            return null;
        }

        StringBuilder stringBuilder = new StringBuilder();

        while (start < end) {
            for (int i = start; i < end; ++i) {
                if (buf[i] == '\n') {
                    int len = i - start;
                    if (i > 0 && buf[i - 1] == '\r') {
                        len--;
                    }
                    if (len > 0) {
                        stringBuilder.append(new String(buf, start, len, UTF_8));
                    }
                    start = i + 1;
                    return stringBuilder.toString();
                }
            }

            stringBuilder.append(new String(buf, start, end - start, UTF_8))
            start = end;

            fill();
        }

        return stringBuilder.toString();
    }

    public String readString(int length) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        int readSoFar = 0;
        while (length > readSoFar) {
            fill();

            // No more to read
            if (start >= end) {
                break;
            }

            int amountToRead = end - start;
            if (amountToRead + readSoFar > length) {
                amountToRead = length - readSoFar;
            }
            stringBuilder.append(new String(buf, start, amountToRead, UTF_8));
            start += amountToRead + 1;
            readSoFar += amountToRead;
        }

        return stringBuilder.toString();
    }

    public boolean isEOF() {
        return end < 0;
    }

    private void fill() throws IOException {
        if (start >= end) {
            start = 0;
            end = stream.read(buf, 0, BUFFER_SIZE);
        }
    }
}
