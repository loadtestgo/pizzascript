package com.loadtestgo.util;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.nio.ByteBuffer;

@RunWith(JUnit4.class)
public class BufferTest {
    @Test
    public void flipper() {
        ByteBuffer buffer = ByteBuffer.allocateDirect(10);

        for (int i = 0; i < 3; ++i) {
            buffer.put((byte) i);
        }

        // Call flip to switch to reading from the buffer (reset)
        buffer.flip();

        while (buffer.hasRemaining()) {
            byte r = buffer.get();
            System.out.println(String.format("%d", r));
        }

        buffer.compact();

        for (int i = 0; i < 3; ++i) {
            buffer.put((byte) i);
        }

        // Call flip to switch to reading from the buffer (reset)
        buffer.flip();

        while (buffer.hasRemaining()) {
            byte r = buffer.get();
            System.out.println(String.format("%d", r));
        }
    }
}
