package com.loadtestgo.script.tester.pages;

import com.loadtestgo.script.tester.annotations.Page;
import com.loadtestgo.script.tester.server.HttpHeaders;
import com.loadtestgo.script.tester.server.HttpRequest;
import com.loadtestgo.util.Base64;
import com.loadtestgo.util.StringUtils;

import java.io.IOException;

public class Auth {
    @Page(desc = "Ask Authentication, then accept any username/password")
    public void accept(HttpRequest request) throws IOException {
        HttpHeaders headers = request.readHeaders();
        String authorization = headers.get("Authorization");
        if (StringUtils.isEmpty(authorization)) {
            request.write("HTTP/1.1 401 Authentication Required\r\n");
            request.writeHtmlHeaders();
            request.writeHeader("WWW-Authenticate", "Basic realm=\'realm\'");
            request.writeln();
            request.write("<html><head></head><body><h1>401 Authentication Required</h1></body></html>");
        } else {
            request.writeOk();
            request.writeHtmlHeaders();
            request.writeln();
            request.write("<html><head></head><body><h1>Authentication Ok</h1></body></html>");
        }
    }

    @Page(desc = "Ask Authentication, then accept only 'username' & 'password'")
    public void check(HttpRequest request) throws IOException {
        HttpHeaders headers = request.readHeaders();
        String authorization = headers.get("Authorization");
        if (!StringUtils.isEmpty(authorization)) {
            if (authorization.startsWith("Basic ")) {
                String authStringProvided = authorization.substring(6);
                String authString = new String(
                        Base64.encodeBase64("username:password".getBytes("UTF-8")),
                        "UTF-8");
                if (authString.equals(authStringProvided)) {
                    request.writeOk();
                    request.writeHtmlHeaders();
                    request.writeln();
                    request.write("<html><head></head><body><h1>Authentication Ok</h1></body></html>");
                    return;
                }
            }
        }
        request.write("HTTP/1.1 401 Authentication Required\r\n");
        request.writeHtmlHeaders();
        request.writeHeader("WWW-Authenticate", "Basic realm=\'realm\'");
        request.writeln();
        request.write("<html><head></head><body><h1>401 Authentication Required</h1></body></html>");
    }

    @Page(desc = "Always ask for Authentication")
    public void reject(HttpRequest request) throws IOException {
        HttpHeaders headers = request.readHeaders();
        request.write("HTTP/1.1 401 Authentication Required\r\n");
        request.writeHtmlHeaders();
        request.writeHeader("WWW-Authenticate", "Basic realm=\'realm\'");
        request.writeln();
        request.write("<html><head></head><body><h1>401 Authentication Required</h1></body></html>");
    }
}
