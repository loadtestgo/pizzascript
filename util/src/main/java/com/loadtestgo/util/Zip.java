package com.loadtestgo.util;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class Zip {
    public static void zipFiles(File dir, File outputFile) throws IOException {
        FileOutputStream outputStream = new FileOutputStream(outputFile);
        zipTestFiles(dir, outputStream);
    }

    public static void zipTestFiles(File dir, OutputStream outputStream) throws IOException {
        String[] entries = dir.list();
        byte[] buffer = new byte[4096];

        try (ZipOutputStream out = new ZipOutputStream(outputStream)) {
            for (String entry : entries) {
                File f = new File(dir, entry);
                if (f.isDirectory()) {
                    continue;
                }
                try (FileInputStream in = new FileInputStream(f)) {
                    ZipEntry zipEntry = new ZipEntry(entry);
                    out.putNextEntry(zipEntry);
                    int bytesRead;
                    while ((bytesRead = in.read(buffer)) != -1) {
                        out.write(buffer, 0, bytesRead);
                    }
                }
            }
        }
    }

    public static void unzipTestFiles(File zipFile, File outputDir) throws IOException {
        unzipFiles(new FileInputStream(zipFile), outputDir);
    }

    public static void unzipFiles(InputStream inputStream, File outputDir) throws IOException {
        if (!outputDir.exists()) {
            outputDir.mkdir();
        }
        try (ZipInputStream zipIn = new ZipInputStream(inputStream)) {
            ZipEntry entry = zipIn.getNextEntry();
            while (entry != null) {
                File filePath = new File(outputDir, entry.getName());
                if (!entry.isDirectory()) {
                    extractFile(zipIn, filePath);
                } else {
                    filePath.mkdir();
                }
                zipIn.closeEntry();
                entry = zipIn.getNextEntry();
            }
        }
    }

    public static void extractFile(ZipInputStream zipIn, File filePath) throws IOException {
        final int BUFFER_SIZE = 4096;
        try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath))) {
            byte[] bytesIn = new byte[BUFFER_SIZE];
            int read = 0;
            while ((read = zipIn.read(bytesIn)) != -1) {
                bos.write(bytesIn, 0, read);
            }
        }
    }
}
