package com.loadtestgo.util;

import org.pmw.tinylog.Logger;

import java.util.ArrayList;

public class Http {
    static public class Request {
        public ArrayList<HttpHeader> headers = new ArrayList<>();
        public String httpVersion;
        public String method;
        public String url;
    }

    static public Request parseRequest(String text) {
        if (text == null) {
            return null;
        }

        Request request = new Request();

        String[] lines = text.split("\r\n|\n");
        if (lines.length > 0) {
            parseRequestLine(lines[0], request);
        }

        if (parseHeaders(lines, 1, request.headers)) {
            return request;
        }

        return null;
    }

    static public class Response {
        public ArrayList<HttpHeader> headers = new ArrayList<>();
        public String httpVersion;
        public int statusCode;
        public String statusText;
    }

    static public Response parseResponse(String text) {
        if (text == null) {
            return null;
        }

        Response response = new Response();

        String[] lines = text.split("\r\n|\n");
        if (lines.length > 0) {
            parseStatusLine(lines[0], response);
        }

        if (parseHeaders(lines, 1, response.headers)) {
            return response;
        }

        return null;
    }

    static public class UrlDetails {
        public String host;
        public String protocol;
        public int port = -1;
        public String path;

        public String toUrl() {
            StringBuilder stringBuilder = new StringBuilder();
            if (protocol == null) {
                stringBuilder.append("http:/");
            } else {
                stringBuilder.append(protocol);
                stringBuilder.append("://");
            }

            if (host != null) {
                stringBuilder.append(host);
            }

            if (port > -1) {
                stringBuilder.append(":");
                stringBuilder.append(port);
            }

            if (path != null) {
                stringBuilder.append(path);
            }

            return stringBuilder.toString();
        }
    }

    static public UrlDetails parseUrl(String url) {
        UrlDetails urlDetails = new UrlDetails();
        int firstColon = url.indexOf(":");
        if (firstColon < 0) {
            return null;
        }

        urlDetails.protocol = url.substring(0, firstColon);
        int startHost = firstColon + 1;

        if (url.charAt(startHost) == '/') {
            startHost += 1;
        }
        if (url.charAt(startHost) == '/') {
            startHost += 1;
        }

        int endHost = url.length();

        // ipv6 are wrapped with [fe80::6aa8:6dff:fe4d:6d7a]
        if (url.charAt(startHost) == '[') {
            endHost = url.indexOf(']', startHost + 1) + 1;

            int beginPath = url.indexOf("/", endHost);
            if (beginPath < 0) {
                beginPath = url.length();
            }

            int portColon = url.indexOf(":", endHost);
            if (portColon >= 0 && beginPath > portColon) {
                String number = url.substring(portColon + 1, beginPath);
                try {
                    urlDetails.port = Integer.parseInt(number);
                } catch (NumberFormatException e) {
                    // Ignore
                }
            }

            urlDetails.host = url.substring(startHost, endHost);
            if (beginPath > 0 && beginPath < url.length()) {
                urlDetails.path = url.substring(beginPath);
            }
        } else {
            int firstSlash = url.indexOf("/", startHost);
            if (firstSlash >= 0 && endHost > firstSlash) {
                endHost = firstSlash;
            }

            int portColon = url.indexOf(":", startHost);
            if (portColon >= 0 && endHost > portColon) {
                String number = url.substring(portColon + 1, endHost);
                try {
                    urlDetails.port = Integer.parseInt(number);
                } catch (NumberFormatException e) {
                    // Ignore
                }
                endHost = portColon;
            }
            urlDetails.host = url.substring(startHost, endHost);
            if (firstSlash > 0) {
                urlDetails.path = url.substring(firstSlash);
            }
        }

        return urlDetails;
    }

    static public String stripAnchor(String url) {
        if (url == null) {
            return null;
        }
        int lastHash = url.lastIndexOf('#');
        if (lastHash == -1) {
            return url;
        }
        return url.substring(0, lastHash);
    }

    static private void parseRequestLine(String statusLine, Request request) {
        int firstSpace = statusLine.indexOf(" ");
        if (firstSpace > 0) {
            request.method = statusLine.substring(0, firstSpace);

            for (int i = firstSpace + 1; i < statusLine.length(); i++) {
                if (statusLine.charAt(i) != ' ') {
                    break;
                } else {
                    firstSpace = i;
                }
            }

            int secondsSpace = statusLine.indexOf(" ", firstSpace + 1);
            request.url = statusLine.substring(firstSpace + 1, secondsSpace);
            request.httpVersion = statusLine.substring(secondsSpace + 1);
        }
    }

    public static HttpHeader parseHeader(String line) {
        int firstColon = line.indexOf(":");
        if (firstColon > 0) {
            return new HttpHeader(
                line.substring(0, firstColon).trim(),
                line.substring(firstColon + 1).trim());
        }
        return null;
    }

    static public String prependHttpToUrl(String url) {
        if (url == null) {
            return null;
        }
        String lowerUrl = url.toLowerCase();
        if (lowerUrl.startsWith("about:")) {
            return url;
        } else if (lowerUrl.startsWith("chrome:")) {
            return url;
        } else if (!lowerUrl.startsWith("http:") &&
            !lowerUrl.startsWith("https:")) {
            return "http://" + url;
        }
        return url;
    }

    static private boolean parseHeaders(String[] lines, int start, ArrayList<HttpHeader> headers) {
        for (int i = start; i < lines.length; ++i) {
            String line = lines[i];
            HttpHeader header = parseHeader(line);
            if (header != null) {
                headers.add(header);
            } else {
                Logger.info("Unable to parse header: {}", line);
            }
        }
        return true;
    }

    static private void parseStatusLine(String statusLine, Response response) {
        int firstSpace = statusLine.indexOf(" ");
        if (firstSpace < 0) {
            response.statusText = statusLine;
        } else {
            response.httpVersion = statusLine.substring(0, firstSpace);

            for (int i = firstSpace + 1; i < statusLine.length(); i++) {
                if (statusLine.charAt(i) != ' ') {
                    break;
                } else {
                    firstSpace = i;
                }
            }

            int secondsSpace = statusLine.indexOf(" ", firstSpace + 1);
            if (secondsSpace > 0) {
                String statusCode = statusLine.substring(firstSpace + 1, secondsSpace);
                try {
                    response.statusCode = Integer.parseInt(statusCode.trim());
                } catch (NumberFormatException nfe) {
                    System.out.println("Unable to parse status code: " + statusCode);
                }
                response.statusText = statusLine.substring(secondsSpace + 1);
            } else {
                // Sometimes the status text is missing, e.g. status line of the form 'HTTP/1.1 200'
                String statusCode = statusLine.substring(firstSpace + 1);
                try {
                    response.statusCode = Integer.parseInt(statusCode.trim());
                } catch (NumberFormatException nfe) {
                    System.out.println("Unable to parse status code: " + statusCode);
                    response.statusText = statusCode;
                }
            }
        }
    }
}
