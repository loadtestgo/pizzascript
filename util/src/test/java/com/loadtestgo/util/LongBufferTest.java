package com.loadtestgo.util;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.assertEquals;

@RunWith(JUnit4.class)
public class LongBufferTest {
    @Test
    public void basic() {
        LongBuffer buffer = new LongBuffer(4);

        assertEquals(4, buffer.capacity());
        assertEquals(0, buffer.size());

        buffer.add(1);
        assertEquals(1, buffer.size());
        assertEquals(0, buffer.begin());

        buffer.add(2);
        assertEquals(2, buffer.size());
        assertEquals(0, buffer.begin());
        assertEquals(1, buffer.end());

        buffer.add(3);
        assertEquals(3, buffer.size());
        assertEquals(0, buffer.begin());
        assertEquals(2, buffer.end());

        buffer.add(4);
        assertEquals(4, buffer.size());
        assertEquals(0, buffer.begin());
        assertEquals(3, buffer.end());

        buffer.add(5);
        assertEquals(4, buffer.size());

        assertEquals(1, buffer.begin());
        assertEquals(2, buffer.beginValue());

        assertEquals(0, buffer.end());
        assertEquals(5, buffer.endValue());

        buffer.add(6);
        assertEquals(4, buffer.size());

        assertEquals(2, buffer.begin());
        assertEquals(3, buffer.beginValue());

        assertEquals(1, buffer.end());
        assertEquals(6, buffer.endValue());
    }
}
