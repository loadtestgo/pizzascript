package com.loadtestgo.script.tester.pages;

import com.loadtestgo.script.tester.annotations.Page;
import com.loadtestgo.script.tester.server.HttpHeaders;
import com.loadtestgo.script.tester.server.HttpRequest;
import com.loadtestgo.util.HttpHeader;

import java.io.IOException;

public class Headers {
    @Page(desc = "Show all headers")
    public void all(HttpRequest request) throws IOException {
        HttpHeaders headers = request.readHeaders();
        request.write("HTTP/1.1 200 OK\r\n\r\n");
        request.write("<html><head></head><body><h1>All Headers</h1>");
        for (HttpHeader header : headers) {
            request.write(String.format("<p>%s: %s</p>", header.name, header.value));
        }
        request.write("</body></html>");
    }

    @Page(desc = "Show User Agent header")
    public void userAgent(HttpRequest request) throws IOException {
        HttpHeaders headers = request.readHeaders();
        request.write("HTTP/1.1 200 OK\r\n\r\n");
        request.write("<html><head></head><body><h1>User Agent</h1>");
        request.write(String.format("<p>%s</p>", headers.get("User-Agent")));
        request.write("</body></html>");
    }
}

