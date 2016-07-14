package com.loadtestgo.script.tester.pages;

import com.loadtestgo.script.tester.annotations.Page;
import com.loadtestgo.script.tester.server.HttpRequest;

import java.io.IOException;

public class Timeouts {
    @Page(desc = "Send partial HTML content then timeout (wait 62 seconds)")
    public void socketTimeoutDuringContent(HttpRequest request) throws IOException, InterruptedException {
        request.readHeaders();
        request.write("HTTP/1.1 200 Ok\r\n\r\n");
        request.write("<html><head></head>");
        Thread.sleep(62000);
    }

    @Page(desc = "Send response line, but no headers or content then timeout (wait 62 seconds)")
    public void timeoutAfterResponseLine(HttpRequest request) throws IOException, InterruptedException {
        request.readHeaders();
        request.write("HTTP/1.1 200 OK\r\n\r\n");
        Thread.sleep(62000);
    }

    @Page(desc = "Timeout before reading headers (wait 62 seconds)")
    public void timeoutBeforeHeaders(HttpRequest request) throws IOException, InterruptedException {
        Thread.sleep(62000);
        // Don't read headers and don't send response
    }

    @Page(desc = "Read headers then timeout (wait 62 seconds)")
    public void timeoutAfterHeaders(HttpRequest request) throws IOException, InterruptedException {
        request.readHeaders();
        Thread.sleep(62000);
        // Don't send response
    }
}
