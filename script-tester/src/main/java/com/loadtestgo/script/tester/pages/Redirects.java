package com.loadtestgo.script.tester.pages;

import com.loadtestgo.script.tester.annotations.Page;
import com.loadtestgo.script.tester.server.HttpRequest;

import java.io.IOException;

public class Redirects {
    @Page(desc = "A redirect request")
    public void basic(HttpRequest request) throws IOException {
        request.redirect(request.baseUrl + "status/code200");
    }

    @Page(desc = "Redirect to 404")
    public void status404(HttpRequest request) throws IOException {
        request.redirect(request.baseUrl + "status/code404");
    }

    @Page(desc = "Redirect to 500")
    public void status500(HttpRequest request) throws IOException {
        request.redirect(request.baseUrl + "status/code500");
    }

    @Page(desc = "Infinite Redirect")
    public void infinite(HttpRequest request) throws IOException {
        request.redirect(request.baseUrl + "redirect/infinite");
    }

    @Page(desc = "Bad Redirect")
    public void bad(HttpRequest request) throws IOException {
        request.redirect("sadad:sadad.com");
    }
}
