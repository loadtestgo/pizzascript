package com.loadtestgo.util;

import java.io.*;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Very basic template writer class
 *
 * Reads a file and replaces variable in the text file with variables
 * specified in a map.
 */
public class Template {
    private String template;
    private Map<String, String> replaceList;

    public Template(File input,
                    Map<String, String> replaceList) throws IOException {
        this.replaceList = replaceList;
        this.template = FileUtils.readAllText(input, "UTF-8");
    }

    public Template(InputStream input,
                    Map<String, String> replaceList) throws IOException {
        this.replaceList = replaceList;
        this.template = FileUtils.readAllText(input, "UTF-8");
    }

    public Template(String template,
                    Map<String, String> replaceList) throws IOException {
        this.replaceList = replaceList;
        this.template = FileUtils.readAllText(template, "UTF-8");
    }

    public Template(Map<String, String> replaceList) throws IOException {
        this.replaceList = replaceList;
    }

    public void write(File output) throws IOException {
        if (template == null) {
            throw new IllegalArgumentException("template must be set or specified");
        }
        writeOut(output, new BufferedReader(new StringReader(template)));
    }

    public void write(File output, File input) throws IOException {
        try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(new FileInputStream(input), UTF_8)))
        {
            writeOut(output, br);
        }
    }

    public String asString() throws IOException {
        if (template == null) {
            throw new IllegalArgumentException("template must be set or specified");
        }

        BufferedReader reader = new BufferedReader(new StringReader(template));
        StringWriter writer = new StringWriter();
        writeOut(writer, reader);
        return writer.toString();
    }

    private void writeOut(File output, BufferedReader br) throws IOException {
        try (
                BufferedWriter bw = new BufferedWriter(
                        new OutputStreamWriter(
                                new FileOutputStream(output), UTF_8)))
        {
            writeOut(bw, br);
        }
    }

    private void writeOut(Writer writer, BufferedReader br) throws IOException {
        Pattern replace = Pattern.compile("\\{\\{([^\\}]+)\\}\\}");

        String line;
        StringBuilder copy = new StringBuilder();
        while ((line = br.readLine()) != null) {
            int end = 0;
            Matcher matcher = replace.matcher(line);
            while (matcher.find()) {
                String match = matcher.group(1);
                String replacement = replaceList.get(match);
                if (replacement == null) {
                    replacement = "";
                }
                copy.append(line.substring(end, matcher.start()));
                copy.append(replacement);
                end = matcher.end();
            }
            if (end == 0) {
                writer.write(line);
            } else {
                writer.write(copy.toString());
                writer.write(line.substring(end));
                copy.setLength(0);
            }
            writer.write('\n');
        }
    }
}
