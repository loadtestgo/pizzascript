package com.loadtestgo.util;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

@RunWith(JUnit4.class)
public class TemplateTest {
    @Test
    public void basic() throws IOException {
        Map<String, String> map = new HashMap<String, String>();
        map.put("hello.there", "hi");
        map.put("hello.world", "low");
        URL urlIn = this.getClass().getResource("/template_in.txt");
        File templateIn = new File(urlIn.getFile());

        Template template = new Template(templateIn, map);
        File output = File.createTempFile("template", ".txt");
        template.write(output);

        URL urlOut = this.getClass().getResource("/template_out.txt");
        File templateOut = new File(urlOut.getFile());

        assertEquals("The files differ!",
                     FileUtils.readAllText(output),
                     FileUtils.readAllText(templateOut));
    }
}
