package com.loadtestgo.script.tester.server;

import com.loadtestgo.util.HttpHeader;
import com.loadtestgo.util.Http;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Date;

public class HttpRequest {
    public HttpRequestLine requestLine;
    public InputStreamReader in;
    public OutputStream out;
    public Socket socket;
    public HttpHeaders headers;
    public String baseUrl;
    public String path;

    public HttpRequest(HttpRequestLine requestLine,
                       InputStreamReader in,
                       OutputStream out,
                       Socket socket,
                       String baseUrl) {
        this.requestLine = requestLine;
        this.in = in;
        this.out = out;
        this.socket = socket;
        this.baseUrl = baseUrl;
    }

    public HttpHeaders readHeaders() throws IOException {
        if (headers != null) {
            return headers;
        }

        headers = new HttpHeaders();
        String line;
        boolean ok = true;
        while (ok) {
            line = in.readLine();
            if (line == null || line.equals("")) {
                ok = false;
            } else {
                HttpHeader header = Http.parseHeader(line);
                if (header != null) {
                    headers.add(header);
                }
            }
        }
        return headers;
    }

    public void writeOk() throws IOException {
        write("HTTP/1.1 200 OK\r\n");
    }

    public void writeln() throws IOException {
        write("\r\n");
    }

    public void writeDefaultHeaders() throws IOException {
        writeHeader("Date", new Date().toString());
        writeHeader("Server", "Test Server");
    }

    public void writeHtmlHeaders() throws IOException {
        writeDefaultHeaders();
        writeHeader("Content-Type", "text/html");
    }

    public void writeHeader(String name, String value) throws IOException {
        write(String.format("%s: %s\r\n", name, value));
    }

    public void write(String s) throws IOException {
        this.out.write(s.getBytes());
    }

    public void write200Page() throws IOException {
        readHeaders();
        write("HTTP/1.1 200 OK\r\n");
        writeHtmlHeaders();
        writeln();
        write("<html><head></head><body><h1>200 OK</h1></body></html>");
    }

    public void write500Page() throws IOException {
        readHeaders();
        write("HTTP/1.1 500 Internal Error\r\n");
        writeHtmlHeaders();
        writeln();
        write("<html><head><title>Internal Error</title></head><body><h1>500 Internal Server Error</h1>");
        write("</body></html>");
    }

    public void write404Page() throws IOException {
        readHeaders();
        write("HTTP/1.1 404 Not Found\r\n");
        writeHtmlHeaders();
        writeln();
        write("<html><head></head><body><h1>404 Not Found</h1></body></html>");
    }

    public void write400Page(String s) throws IOException {
        readHeaders();
        write("HTTP/1.1 400 Bad Request\r\n");
        writeHtmlHeaders();
        writeln();
        write(String.format("<html><head></head><body><h1>400 Bad Request</h1>%s</body></html>", s));
    }

    public void redirect(String url) throws IOException {
        readHeaders();
        write("HTTP/1.1 301 Moved Permanently\r\n");
        writeHeader("Location", url);
        writeHtmlHeaders();
        writeln();
        write("<html><head></head><body><h1>301 Moved Permanently</h1></body></html>");
    }

    public void write411() throws IOException  {
        readHeaders();
        write("HTTP/1.1 411 Length Required\r\n");
        writeHtmlHeaders();
        writeln();
        write("<html><head></head><body><h1>411 Content-Length header required</h1></body></html>");
    }
}