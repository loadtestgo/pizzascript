package com.loadtestgo.script.tester.pages;

import com.loadtestgo.script.tester.annotations.Page;
import com.loadtestgo.script.tester.server.HttpHeaders;
import com.loadtestgo.script.tester.server.HttpRequest;
import com.loadtestgo.util.Http;
import com.loadtestgo.util.HttpHeader;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

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

    @Page(desc = "Called with file upload data")
    public void fileUpload(HttpRequest request) throws IOException {
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

        String contentType = headers.get(HttpHeader.CONTENT_TYPE);
        int comma = contentType.indexOf(";");
        String mainType;
        Map<String,String> params = new HashMap<>();
        if (comma > 0) {
            mainType = contentType.substring(0, comma);
            populateParamMapFromHeaderValue(contentType, comma, params);
        } else {
            mainType = contentType;
        }

        if (!mainType.equalsIgnoreCase("multipart/form-data")) {
            request.write400Page("Expecting content type \'multipart/form-data\'");
            return;
        }

        if (!params.containsKey("boundary")) {
            request.write400Page("Expecting content type \'multipart/form-data\' to contain boundry marker");
            return;
        }

        int length = Integer.valueOf(lengthStr);
        if (length <= 0) {
            request.write400Page("Expecting content length to be greater than 0");
            return;
        }

        String body = request.in.readString(length);

        String boundary = params.get("boundary");
        int firstBoundary = body.indexOf(boundary);
        if (firstBoundary < 0) {
            request.write400Page("Expecting content body to contain boundary");
            return;
        }

        // Find the first form item and read it's headers
        int pos = firstBoundary + boundary.length();

        int endFormInput = body.indexOf(boundary, pos);

        Scanner scanner = new Scanner(body.substring(pos, endFormInput));
        int emptyLines = 0;
        String contentDisposition = null;
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            if (line.length() == 0) {
                emptyLines++;
            } else {
                emptyLines = 0;
                HttpHeader header = Http.parseHeader(line);
                if (header != null && header.name != null) {
                    if (header.name.equalsIgnoreCase("Content-Disposition")) {
                        contentDisposition = header.value;
                    }
                }
            }

            // Finished reading headers
            if (emptyLines == 2) {
                break;
            }
        }
        scanner.close();

        // Check the content disposition on this form item,
        // each form item will contain a name and a type.
        // We expect a file type here and we expect a filename as well.
        if (contentDisposition == null) {
            request.write400Page("Expecting Content-Disposition header");
            return;
        }

        Map<String,String> param = new HashMap<>();
        int firstComma = contentDisposition.indexOf(";");
        if (firstComma > 0) {
            String value = contentDisposition.substring(0, firstComma);
            if (!value.equalsIgnoreCase("form-data")) {
                request.write400Page("Expecting Content-Disposition header to contain 'form-data'");
                return;
            }
            populateParamMapFromHeaderValue(contentDisposition, firstComma, param);
            if (!param.containsKey("name")) {
                request.write400Page("Expecting Content-Disposition header to contain 'name' param");
                return;
            }
            if (!param.containsKey("filename")) {
                request.write400Page("Expecting Content-Disposition header to contain 'filename' param");
                return;
            }
            String filename = param.get("filename");
            if (filename.startsWith("\"")) {
                filename = filename.substring(1);
            }
            if (filename.endsWith("\"")) {
                filename = filename.substring(0, filename.length()-1);
            }
            if (filename.length() == 0) {
                request.write400Page("Expecting Content-Disposition header to contain non empty 'filename' param");
                return;
            }
        }

        request.writeOk();
        request.writeDefaultHeaders();
        request.writeHeader(HttpHeader.CONTENT_TYPE, "text/plain");
        request.writeln();
        request.write(body);
    }

    private void populateParamMapFromHeaderValue(String contentType, int comma, Map<String, String> params) {
        String[] paramArray = contentType.substring(comma + 1, contentType.length()).split(";");
        for (String parameter : paramArray) {
            int equals = parameter.indexOf("=");
            if (equals >= 0) {
                String attribute = parameter.substring(0, equals).trim();
                String value = parameter.substring(equals + 1).trim();
                params.put(attribute, value);
            }
        }
    }
}
