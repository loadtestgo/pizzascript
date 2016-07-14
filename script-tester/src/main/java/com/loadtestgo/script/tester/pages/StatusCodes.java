package com.loadtestgo.script.tester.pages;

import com.loadtestgo.script.tester.annotations.Page;
import com.loadtestgo.script.tester.server.HttpRequest;

import java.io.IOException;

public class StatusCodes {
    public StatusCodes() {
    }

    @Page(desc = "Status ok")
    public void code200(HttpRequest request) throws IOException {
        request.write200Page();
    }

    @Page(desc = "Page not found")
    public void code404(HttpRequest request) throws IOException {
        request.write404Page();
    }

    @Page(desc = "Internal Server Error")
    public void code500(HttpRequest request) throws IOException {
        request.write500Page();
    }
}
