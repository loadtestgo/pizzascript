package com.loadtestgo.util;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import sun.net.www.protocol.file.FileURLConnection;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ResourceUtils {
     public static void copyDirectory(URL originUrl, File destination) throws IOException {
        URLConnection urlConnection = originUrl.openConnection();
        if (urlConnection instanceof JarURLConnection) {
            copyJarDirectory(destination, (JarURLConnection) urlConnection);
        } else if (urlConnection instanceof FileURLConnection) {
            FileUtils.copyDirectory(new File(originUrl.getPath()), destination);
        } else {
            throw new IOException("URLConnection[" + urlConnection.getClass().getSimpleName() +
                    "] is not a recognized/implemented connection type.");
        }
    }

    public static void copyJarDirectory(File destination, JarURLConnection jarConnection) throws IOException {
        JarFile jarFile = jarConnection.getJarFile();

        String resourceDir = StringUtils.addForwardSlash(jarConnection.getEntryName());

        Enumeration<JarEntry> entries = jarFile.entries();
        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            if (entry.getName().startsWith(resourceDir)) {
                String fileName = StringUtils.removeStart(entry.getName(), resourceDir);
                File dest = new File(destination, fileName);
                if (!entry.isDirectory()) {
                    try (InputStream entryInputStream = jarFile.getInputStream(entry)) {
                        FileUtils.copyInputStreamToFile(entryInputStream, dest);
                    }
                } else {
                    FileUtils.forceMkdir(new File(destination, fileName));
                }
            }
        }
    }

    public static String loadResourceAsString(Class klass, String filename) throws IOException {
        InputStream stream = klass.getResourceAsStream(filename);
        if (stream == null) {
            throw new IOException(String.format("Unable to laod %s", filename));
        }
        return IOUtils.toString(stream);
    }
}
