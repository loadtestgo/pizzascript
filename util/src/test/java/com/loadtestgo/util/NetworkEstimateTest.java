package com.loadtestgo.util;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

@RunWith(JUnit4.class)
public class NetworkEstimateTest {
    /*
     * Function to reset idle time:
     *
     * $> sysctl net.ipv4.tcp_window_scaling
     * $> sysctl -w net.ipv4.tcp_window_scaling=1
     */
    @Test
    public void basic() throws IOException {
        int time = downloadEstimateInMillis((1 << 16) - 1, 56, 4, 1460);
        assertEquals(224, time);

        int time2 = downloadEstimateInMillis((1 << 16) - 1, 100, 10, 1460);
        assertEquals(300, time2);

        checkLog2Ceil(1, 2);
        checkLog2Ceil(4, (1 << 4) - 1);
        checkLog2Ceil(10, (1 << 10) - 1);
        checkLog2Ceil(16, (1 << 16) - 1);
        checkLog2Ceil(30, (1 << 30) - 1);
    }

    private void checkLog2Ceil(int result, int v) {
        assertEquals(result, log2CeilSlow(v));
        assertEquals(result, log2Ceil(v));
    }

    /**
     * Function to calculate the time to download a resource over HTTP.
     *
     * Ignores bandwidth and tcp window, so it's only remotely accurate for small assets.
     *
     * We'd probably need a simulation to handling things like bandwidth, packet loss
     * and variable round trip times.
     *
     * cwnd originally set to 1
     * cwnd April 1999 vendors were advised to to set to 4
     * cwnd April 2013 vendors were advised to to set to 10
     */
    private static int downloadEstimateInMillis(int sizeBytes, int rrtMilliseconds,
                                                int cwndStart, int packetSize) {
        int n = (int)Math.ceil((double)sizeBytes/(double)packetSize);
        int windows = (int)Math.ceil((double)(n)/(double)cwndStart);
        return rrtMilliseconds * log2Ceil(windows);
    }

    private static int log2Ceil(int x) {
        if (x < 0) {
            return log2Ceil(-x);
        }

        int t[] = new int[] {
            0xFFFF0000,
            0x0000FF00,
            0x000000F0,
            0x0000000C,
            0x00000002
        };

        int y = (((x & (x - 1)) == 0) ? 0 : 1);
        int j = 16;
        int i;

        for (i = 0; i < 5; i++) {
            int k = (((x & t[i]) == 0) ? 0 : j);
            y += k;
            x >>= k;
            j >>= 1;
        }

        return y;
    }

    private static int log2CeilSlow(int value) {
        // There's a bit version of this of course
        return (int)Math.ceil(Math.log(value) / Math.log(2));
    }
}
