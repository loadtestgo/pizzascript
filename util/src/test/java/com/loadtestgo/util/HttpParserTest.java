package com.loadtestgo.util;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@RunWith(JUnit4.class)
public class HttpParserTest {
    @Test
    public void urlParse() {
        Http.UrlDetails urlDetails = Http.parseUrl("http://www.google.com");
        assertEquals("http", urlDetails.protocol);
        assertEquals("www.google.com", urlDetails.host);
        assertEquals(-1, urlDetails.port);
        assertNull(urlDetails.path);

        urlDetails = Http.parseUrl("http://www.google.com/");
        assertEquals("http", urlDetails.protocol);
        assertEquals("www.google.com", urlDetails.host);
        assertEquals(-1, urlDetails.port);
        assertEquals("/", urlDetails.path);

        urlDetails = Http.parseUrl("https://www.google.com/q=something");
        assertEquals("https", urlDetails.protocol);
        assertEquals("www.google.com", urlDetails.host);
        assertEquals(-1, urlDetails.port);
        assertEquals("/q=something", urlDetails.path);

        urlDetails = Http.parseUrl("https://www.google.com:8888/q=something");
        assertEquals("https", urlDetails.protocol);
        assertEquals("www.google.com", urlDetails.host);
        assertEquals(8888, urlDetails.port);
        assertEquals("/q=something", urlDetails.path);

        urlDetails = Http.parseUrl("https://www.google.com:8888");
        assertEquals("https", urlDetails.protocol);
        assertEquals("www.google.com", urlDetails.host);
        assertEquals(8888, urlDetails.port);
        assertNull(urlDetails.path);

        urlDetails = Http.parseUrl("https:/www.google.com:8888");
        assertEquals("https", urlDetails.protocol);
        assertEquals("www.google.com", urlDetails.host);
        assertEquals(8888, urlDetails.port);
        assertNull(urlDetails.path);

        urlDetails = Http.parseUrl("https:www.google.com:8888");
        assertEquals("https", urlDetails.protocol);
        assertEquals("www.google.com", urlDetails.host);
        assertEquals(8888, urlDetails.port);
        assertNull(urlDetails.path);

        urlDetails = Http.parseUrl("https://[2607:f8b0:4007:801::1010]");
        assertEquals("https", urlDetails.protocol);
        assertEquals("[2607:f8b0:4007:801::1010]", urlDetails.host);
        assertEquals(-1, urlDetails.port);
        assertNull(urlDetails.path);

        urlDetails = Http.parseUrl("https://[2607:f8b0:4007:801::1010]/");
        assertEquals("https", urlDetails.protocol);
        assertEquals("[2607:f8b0:4007:801::1010]", urlDetails.host);
        assertEquals(-1, urlDetails.port);
        assertEquals("/", urlDetails.path);

        urlDetails = Http.parseUrl("https://[2607:f8b0:4007:801::1010]/q=something");
        assertEquals("https", urlDetails.protocol);
        assertEquals("[2607:f8b0:4007:801::1010]", urlDetails.host);
        assertEquals(-1, urlDetails.port);
        assertEquals("/q=something", urlDetails.path);

        urlDetails = Http.parseUrl("https://[2607:f8b0:4007:801::1010]:8888");
        assertEquals("https", urlDetails.protocol);
        assertEquals("[2607:f8b0:4007:801::1010]", urlDetails.host);
        assertEquals(8888, urlDetails.port);
        assertNull(urlDetails.path);

        urlDetails = Http.parseUrl("https://[2607:f8b0:4007:801::1010]:8888/");
        assertEquals("https", urlDetails.protocol);
        assertEquals("[2607:f8b0:4007:801::1010]", urlDetails.host);
        assertEquals(8888, urlDetails.port);
        assertEquals("/", urlDetails.path);

        urlDetails = Http.parseUrl("https://[2607:f8b0:4007:801::1010]:8888/q=something");
        assertEquals("https", urlDetails.protocol);
        assertEquals("[2607:f8b0:4007:801::1010]", urlDetails.host);
        assertEquals(8888, urlDetails.port);
        assertEquals("/q=something", urlDetails.path);
    }

    @Test
    public void httpRequest() {
        Http.Request request = Http.parseRequest("GET / HTTP/1.1\r\n" +
            "Host: www.google.com\r\n" +
            "Connection: keep-alive\r\n" +
            "Accept: image/png\r\n" +
            "Accept-Encoding: gzip,deflate\r\n" +
            "Accept-Language: en-US\r\n\r\n");
        assertEquals("HTTP/1.1", request.httpVersion);
        assertEquals("/", request.url);
        assertEquals("GET", request.method);
        assertEquals(5, request.headers.size());
        assertEquals("Host", request.headers.get(0).name);
        assertEquals("www.google.com", request.headers.get(0).value);
        assertEquals("Accept-Language", request.headers.get(4).name);
        assertEquals("en-US", request.headers.get(4).value);

        request = Http.parseRequest("POST /someurl/?blah HTTP/1.0\r\n" +
            "Host: www.google.com\r\n" +
            "Connection: keep-alive\r\n" +
            "Accept: image/png\r\n" +
            "Accept-Encoding: gzip,deflate\r\n" +
            "Accept-Language: en-US\r\n\r\n");
        assertEquals("HTTP/1.0", request.httpVersion);
        assertEquals("/someurl/?blah", request.url);
        assertEquals("POST", request.method);
        assertEquals(5, request.headers.size());
        assertEquals("Host", request.headers.get(0).name);
        assertEquals("www.google.com", request.headers.get(0).value);
        assertEquals("Accept-Language", request.headers.get(4).name);
        assertEquals("en-US", request.headers.get(4).value);
    }

    @Test
    public void httpResponse() {
        Http.Response response = Http.parseResponse("HTTP/1.1 200 OK\r\n" +
            "Date: Tue, 24 Dec 2013 05:20:57 GMT\r\n" +
            "Set-Cookie: B=bupr95t9bi6dp&b=3&s=hq; expires=Fri, 25-Dec-2015 05:20:57 GMT; path=/; domain=.yahoo.com\r\n" +
            "Cache-Control: private\r\n" +
            "X-Frame-Options: SAMEORIGIN\r\n" +
            "Set-Cookie: PH=deleted; expires=Mon, 24-Dec-2012 05:20:56 GMT; path=/; domain=.yahoo.com\r\n" +
            "Vary: Accept-Encoding\r\n" +
            "Age: 0\r\n" +
            "Transfer-Encoding: chunked\r\n" +
            "Connection: keep-alive\r\n" +
            "Server: YTS/1.20.13\r\n\r\n");

        assertEquals("HTTP/1.1", response.httpVersion);
        assertEquals(200, response.statusCode);
        assertEquals("OK", response.statusText);
        assertEquals(10, response.headers.size());
    }
}
