package com.loadtestgo.util;

import org.apache.commons.io.IOUtils;

import java.io.*;
import java.nio.charset.Charset;

public class FileUtils
{
    public static String readAllText(String filename)
    {
        return readAllText(new File(filename), "UTF-8");
    }

    public static String readAllText(String filename, String encoding)
    {
        return readAllText(new File(filename), encoding);
    }

    public static String readAllText(File filename)
    {
        return readAllText(filename, "UTF-8");
    }

    public static String readAllText(File filename, String encoding)
    {
        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(filename);
        } catch (FileNotFoundException e) {
            return null;
        }

        try {
            return IOUtils.toString(inputStream, Charset.forName(encoding));
        } catch (IOException e) {
            return null;
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                // Ignore
            }
        }
    }

    public static String readAllText(InputStream inputStream) throws IOException {
        return readAllText(inputStream, "UTF-8");
    }

    public static String readAllText(InputStream inputStream, String encoding) throws IOException {
        return IOUtils.toString(inputStream, Charset.forName(encoding));
    }

    static public File findExecutable(String executableName) {
        String systemPath = System.getenv("PATH");
        String[] pathDirs = systemPath.split(File.pathSeparator);

        File fullyQualifiedExecutable = null;
        for (String pathDir : pathDirs)
        {
            File file = new File(pathDir, executableName);
            if (file.isFile() && file.canExecute())
            {
                fullyQualifiedExecutable = file;
                break;
            }
        }
        return fullyQualifiedExecutable;
    }
}
