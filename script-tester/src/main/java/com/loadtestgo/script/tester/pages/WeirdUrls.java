package com.loadtestgo.script.tester.pages;

import com.loadtestgo.script.tester.annotations.Page;
import com.loadtestgo.script.tester.server.HttpRequest;

import java.io.IOException;

public class WeirdUrls {
    @Page(desc = "URL with case", path ="/A_b_C_d")
    public void cases(HttpRequest request) throws IOException {
        request.write200Page();
    }

    @Page(desc = "URL with underscores", path ="/_abc_def_")
    public void underscore(HttpRequest request) throws IOException {
        request.write200Page();
    }

    @Page(desc = "URL with params", path ="/?a=1&b=2")
    public void cgiParams(HttpRequest request) throws IOException {
        request.write200Page();
    }

    @Page(desc = "URL with spaces", path ="/?a= &b= ")
    public void spaces(HttpRequest request) throws IOException {
        request.write200Page();
    }
}
