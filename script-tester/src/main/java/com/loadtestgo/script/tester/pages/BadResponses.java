package com.loadtestgo.script.tester.pages;

import com.loadtestgo.script.tester.annotations.Page;
import com.loadtestgo.script.tester.server.HttpRequest;

import java.io.IOException;

public class BadResponses {
    @Page(desc = "Send HTML but no response headers")
    public void headersNone(HttpRequest request) throws IOException {
        request.readHeaders();
        request.write("HTTP/1.1 200 Ok\r\n");
        request.write("<html><head></head><body>No headers</body></html>");
    }

    @Page(desc = "Send back response with badly formatted headers")
    public void headersBad(HttpRequest request) throws IOException {
        request.readHeaders();
        request.write("HTTP/1.1 200 Ok\r\n");
        request.write("header header header\r\n\r\n");
        request.write("<html><head></head><body>Bad headers</body></html>");
    }

    @Page(desc = "Close socket before any response is sent")
    public void closeBeforeHeadersRead(HttpRequest request) throws IOException {
        // Don't read headers and don't send response
    }

    @Page(desc = "Read headers then close socket")
    public void closeAfterHeadersRead(HttpRequest request) throws IOException {
        request.readHeaders();
        // Don't send response
    }

    @Page(desc = "Close socket while writing headers")
    public void closeWritingHeaders(HttpRequest request) throws IOException {
        request.readHeaders();
        request.write("HTTP/1.1 200 Ok\r\n");
        request.write("Content-Type: text/plain\r\n");
    }

    @Page(desc = "Write partial content then close socket")
    public void closeDuringContent(HttpRequest request) throws IOException {
        request.readHeaders();
        request.write("HTTP/1.1 200 Ok\r\n\r\n");
        request.write("<html><head></head>");
    }

    @Page(desc = "Reset socket before any response is sent")
    public void resetBeforeHeadersRead(HttpRequest request) throws IOException {
        // Don't read headers and don't send response
        request.socket.setSoLinger(true, 0);
    }

    @Page(desc = "Read headers then reset socket")
    public void resetAfterHeadersRead(HttpRequest request) throws IOException {
        request.readHeaders();
        // Don't send any response
        request.socket.setSoLinger(true, 0);
    }

    @Page(desc = "Reset socket while writing headers")
    public void resetWritingHeaders(HttpRequest request) throws IOException {
        request.readHeaders();
        request.write("HTTP/1.1 200 Ok\r\n");
        request.write("Content-Type: text/plain\r\n");
        request.socket.setSoLinger(true, 0);
    }

    @Page(desc = "Write partial content then reset socket")
    public void resetDuringContent(HttpRequest request) throws IOException {
        request.readHeaders();
        request.write("HTTP/1.1 200 Ok\r\n\r\n");
        request.write("<html><head></head>");
        request.socket.setSoLinger(true, 0);
    }

    @Page(desc = "Send back a badly formatted response line, with valid content")
    public void responseLineBad(HttpRequest request) throws IOException {
        request.readHeaders();
        request.write("404 DRUM\r\n");
        request.write("<html><head></head><body></body></html>");
    }

    @Page(desc = "Send invalid response line")
    public void responseLineInvalid(HttpRequest request) throws IOException {
        request.write("SLOWLYv110101 9001 Overninethousand\r\n\r\n");
    }
}
