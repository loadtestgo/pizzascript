package com.bric;

import com.bric.qt.io.JPEGMovWriter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class MjpegWriterTest {
    @Test
    public void basic() throws IOException {
        JPEGMovWriter movWriter = new JPEGMovWriter(new File("output5.mov"));

        String[] files = { "1480880207035.jpeg",
            "1480880207046.jpeg",
            "1480880207101.jpeg",
            "1480880207148.jpeg",
            "1480880207368.jpeg",
            "1480880207390.jpeg",
            "1480880207417.jpeg",
            "1480880207431.jpeg",
            "1480880207453.jpeg",
            "1480880207464.jpeg",
            "1480880207480.jpeg",
            "1480880207520.jpeg",
            "1480880207522.jpeg",
            "1480880207538.jpeg",
            "1480880207600.jpeg",
            "1480880207625.jpeg",
            "1480880207631.jpeg",
            "1480880207640.jpeg",
            "1480880207662.jpeg",
            "1480880207681.jpeg",
            "1480880207700.jpeg",
            "1480880207716.jpeg",
            "1480880207731.jpeg",
            "1480880207748.jpeg",
            "1480880207779.jpeg",
            "1480880207785.jpeg",
            "1480880207804.jpeg",
            "1480880207854.jpeg",
            "1480880207860.jpeg",
            "1480880207871.jpeg",
            "1480880208286.jpeg",
            "1480880208555.jpeg",
            "1480880208594.jpeg",
            "1480880209348.jpeg",
            "1480880209723.jpeg",
            "1480880213234.jpeg",
            "1480880213683.jpeg",
            "1480880213783.jpeg",
            "1480880213882.jpeg",
            "1480880217773.jpeg" };

        Arrays.sort(files);
        for (String file : files) {
            // movWriter.addFrame(0.1f, new File("/Users/watsonmw/pizzascript/bbc.com/" + file));
        }
        movWriter.close();
    }
}
