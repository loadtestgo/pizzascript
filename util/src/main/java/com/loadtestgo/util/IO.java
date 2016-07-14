package com.loadtestgo.util;

import java.io.*;

public class IO {
    public static byte[] readToByteBuffer(InputStream inStream) throws IOException {
        final int bufferSize = 1024 * 10;
        byte[] buffer = new byte[bufferSize];
        ByteArrayOutputStream outStream = new ByteArrayOutputStream(bufferSize);
        int read;
        while (true) {
            read = inStream.read(buffer);
            if (read == -1) {
                break;
            }
            outStream.write(buffer, 0, read);
        }
        return outStream.toByteArray();
    }

    public static String readFully(InputStream is) throws IOException {
        return readFully(is, 1024);
    }

    public static String readFully(InputStream is, int bufferSize) throws IOException {
        char[] buffer = new char[bufferSize];
        StringBuilder out = new StringBuilder();
        try (Reader in = new InputStreamReader(is, "UTF-8")) {
            while (true) {
                int rsz = in.read(buffer, 0, buffer.length);
                if (rsz < 0) {
                    break;
                }
                out.append(buffer, 0, rsz);
            }
        } catch (UnsupportedEncodingException e) {
            /* Yes, eat it */
        }

        return out.toString();
    }
}
