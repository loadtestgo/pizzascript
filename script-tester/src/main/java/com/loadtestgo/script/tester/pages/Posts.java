package com.loadtestgo.script.tester.pages;

import com.loadtestgo.script.tester.annotations.Page;
import com.loadtestgo.script.tester.server.HttpHeaders;
import com.loadtestgo.script.tester.server.HttpRequest;
import com.loadtestgo.util.HttpHeader;

import java.io.IOException;

public class Posts {
    @Page(desc = "Echo the post data")
    public void echo(HttpRequest request) throws IOException {
        HttpHeaders headers = request.readHeaders();

        if (!request.requestLine.getType().equals("POST")) {
            request.write500Page();
            return;
        }

        String lengthStr = headers.get(HttpHeader.CONTENT_LENGTH);
        if (lengthStr == null) {
            request.write411();
            return;
        }

        int length = Integer.valueOf(lengthStr);
        String body = request.in.readString(length);

        request.writeOk();
        request.writeDefaultHeaders();
        request.writeHeader(HttpHeader.CONTENT_TYPE, headers.get(HttpHeader.CONTENT_TYPE));
        request.writeln();
        request.write(body);
    }
}
