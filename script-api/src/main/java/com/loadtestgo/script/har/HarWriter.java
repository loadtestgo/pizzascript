package com.loadtestgo.script.har;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.loadtestgo.script.api.HttpRequest;
import com.loadtestgo.script.api.Page;
import com.loadtestgo.script.api.TestResult;
import com.loadtestgo.util.HttpHeader;
import com.loadtestgo.util.StringUtils;
import org.pmw.tinylog.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class HarWriter {
    public static void save(TestResult result, OutputStream output) throws IOException {
        Har har = toHar(result);
        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(output, har);
    }

    public static void save(TestResult testResult, String filePath) throws IOException {
        try (OutputStream output = new FileOutputStream(filePath)) {
            HarWriter.save(testResult, output);
        }
    }

    /**
     * Save a test result to a file & compress using zip
     *
     * @param result the input result to save
     * @param fileName the name of the file once the zip file is unpacked
     * @param outputZip the file path to write to
     * @throws IOException if there was a problem
     */
    public static void saveZip(TestResult result, String fileName, File outputZip) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);

        FileOutputStream fileOutputStream = new FileOutputStream(outputZip);
        ZipOutputStream zipOutputStream = new ZipOutputStream(fileOutputStream);

        ZipEntry zipEntry = new ZipEntry(fileName);
        zipOutputStream.putNextEntry(zipEntry);

        objectMapper.writeValue(zipOutputStream, result);

        zipOutputStream.closeEntry();

        zipOutputStream.close();
        fileOutputStream.close();
    }

    static Har toHar(TestResult result) {
        Har har = new Har();

        ArrayList<Page> pages = result.getPages();
        har.log.browser.name = result.getBrowserName();
        har.log.browser.version = result.getBrowserVersion();

        har.log.creator.name = "Loadcust";
        har.log.creator.version = "0.1";

        DateFormat iso8601 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZZ");

        // Set the page loaded info
        int i = 1;
        for (Page page : pages) {
            HarPage harPage = new HarPage();
            if (page.getNavStartTime() != null) {
                harPage.startedDateTime = iso8601.format(page.getNavStartTime());
            } else {
                harPage.startedDateTime = iso8601.format(page.createdTime);
            }
            harPage.id = String.format("page_%d", i);
            harPage.title = page.getFinalName();
            if (StringUtils.isEmpty(harPage.title)) {
                harPage.title = String.format("Page %d", i);
            }
            harPage.pageTimings.onContentLoad = timeDiff(page.getNavStartTime(), page.getDomContentLoadedTime());
            harPage.pageTimings.onLoad = timeDiff(page.getNavStartTime(), page.getNavEndTime());
            har.log.pages.add(harPage);
            ++i;
        }

        // Set the list of HTTP entries
        i = 1;
        for (Page page : pages) {
            String pageRef = String.format("page_%d", i);
            for (HttpRequest request : page.getRequests()) {
                // Ignore data Urls
                if (request.getUrl().startsWith("data:")) {
                    continue;
                }
                HarEntry entry = new HarEntry();
                entry.pageref = pageRef;
                entry.request.method = request.getMethod();
                entry.request.bodySize = request.getRequestBodySize();
                entry.request.httpVersion = request.getRequestHttpVersion();
                entry.request.headersSize = request.getRequestHeadersSize();
                entry.request.url = request.getUrl();
                for (HttpHeader httpHeader : request.getRequestHeaders()) {
                    entry.request.headers.add(new HarHeader(httpHeader.name, httpHeader.value));
                    if (httpHeader.name.equals(HttpHeader.SET_COOKIE)) {
                        parseCookies(httpHeader.value, entry.request.cookies);
                    }
                }
                if (request.isFromCache()) {
                    // Cache entry
                    if (request.getProtocol() != null) {
                        entry.response.httpVersion = request.getRequestHttpVersion();
                    }
                    entry.response.status = 304;
                    entry.response.statusText = request.getStatusText();
                    entry.response.headersSize = request.getResponseHeadersSize();
                    entry.response.bodySize = 0;
                } else {
                    // Non-cache entry
                    if (request.getProtocol() != null) {
                        entry.response.httpVersion = request.getProtocol();
                    }
                    entry.response.status = request.getStatusCode();
                    if (request.getStatusText() != null) {
                        entry.response.statusText = request.getStatusText();
                    }
                    entry.response.headersSize = request.getResponseHeadersSize();
                    if (request.getRecvHeadersEnd() >= 0) {
                        entry.response.bodySize = request.getRecvHeadersEnd();
                    } else {
                        entry.response.bodySize = request.getBodySize();
                    }
                    entry.connection = String.valueOf(request.getConnectionId());
                }
                if (request.getRedirectUrl() != null) {
                    entry.response.redirectURL = request.getRedirectUrl();
                }
                for (HttpHeader httpHeader : request.getResponseHeaders()) {
                    entry.response.headers.add(new HarHeader(httpHeader.name, httpHeader.value));
                    if (httpHeader.name.equalsIgnoreCase(HttpHeader.COOKIE)) {
                        parseCookies(httpHeader.value, entry.request.cookies);
                    } else if (httpHeader.name.equalsIgnoreCase(httpHeader.CONTENT_TYPE)) {
                        entry.response.content.mimeType = httpHeader.value;
                    }
                }

                entry.response.content.size = request.getBodySize();
                if (request.getRecvHeadersEnd() >= 0) {
                    entry.response.content.compression = request.getBodySize() - request.getRecvHeadersEnd();
                }

                long totalTime = 0;
                if (request.getBlockedTime() >= 0) {
                    entry.timings.blocked = request.getBlockedTime();
                    totalTime += entry.timings.blocked;
                }
                if (request.getDnsStart() >= 0 && request.getDnsEnd() >= 0) {
                    entry.timings.dns = request.getDnsEnd() - request.getDnsStart();
                    totalTime += entry.timings.dns;
                }
                if (request.getConnectStart() >= 0 && request.getConnectEnd() > 0) {
                    entry.timings.connect = request.getConnectEnd() - request.getConnectStart();
                    totalTime += entry.timings.connect;
                }
                if (request.getSslStart() > 0 && request.getSslEnd() >= 0) {
                    entry.timings.ssl = request.getSslEnd() - request.getSslStart();
                }
                if (request.getSendStart() >= 0 && request.getSendEnd() >= 0) {
                    entry.timings.send = request.getSendEnd() - request.getSendStart();
                    totalTime += entry.timings.send;
                } else {
                    entry.timings.send = 0;
                }
                if (request.getSendEnd() >= 0 && request.getRecvHeadersEnd() >= 0) {
                    entry.timings.wait = request.getRecvHeadersEnd() - request.getSendEnd();
                    totalTime += entry.timings.wait;
                } else {
                    entry.timings.wait = 0;
                }
                if (request.getRecvEnd() >= 0) {
                    if (request.getRecvHeadersEnd() >= 0) {
                        entry.timings.receive = request.getRecvEnd() - request.getRecvHeadersEnd();
                    } else {
                        entry.timings.receive = request.getRecvEnd();
                    }
                } else {
                    entry.timings.receive = 0;
                }
                totalTime += entry.timings.receive;

                entry.time = request.getRecvEnd();
                entry.startedDateTime = iso8601.format(request.getStartTime());

                if (entry.time != totalTime) {
                    long diff = entry.time - totalTime;
                    // Print out an error if the diff is too large, small diffs happen due to rounding errors
                    if (diff > 2 || diff < -2) {
                        Logger.error("Total time mismatch: {} {} {}", request.getUrl(), entry.time, totalTime);
                    }
                    entry.time = totalTime;
                }

                entry.serverIPAddress = request.getIp();
                har.log.entries.add(entry);
            }
            ++i;
        }

        return har;
    }

    private static void parseCookies(String value, ArrayList<HarCookie> cookies) {
        // Example cookie:
        // B=06c6oht8uhjrf&b=3&s=ho; expires=Mon, 20-Jul-2015 05:32:31 GMT; path=/; domain=.yahoo.com
        HarCookie cookie = new HarCookie();
        String[] rawCookieParams = value.split(";");
        for (int i = 0; i < rawCookieParams.length; ++i) {
            String cookieParam = rawCookieParams[i];
            int index = cookieParam.indexOf("=");
            if (index <= 0) {
                Logger.error("Unable to parse cookie {}", cookieParam);
            } else {
                String cookieName = cookieParam.substring(0, index).trim();
                String cookieValue = cookieParam.substring(index + 1).trim();
                if (i == 0) {
                    cookie.name = cookieName;
                    cookie.value = cookieValue;
                } else {
                    if (cookieName.equalsIgnoreCase(HarCookie.SECURE)) {
                        cookie.secure = true;
                    } else if (cookieName.equalsIgnoreCase(HarCookie.COMMENT)) {
                        cookie.comment = cookieValue;
                    } else if (cookieName.equalsIgnoreCase(HarCookie.PATH)) {
                        cookie.path = cookieValue;
                    } else if (cookieName.equalsIgnoreCase(HarCookie.DOMAIN)) {
                        cookie.domain = cookieValue;
                    } else if (cookieName.equalsIgnoreCase(HarCookie.EXPIRES)) {
                        cookie.expires = cookieValue;
                    } else if (cookieName.equalsIgnoreCase(HarCookie.HTTP_ONLY)) {
                        cookie.httpOnly = true;
                    }
                }
            }
        }
        cookies.add(cookie);
    }

    private static long timeDiff(Date timeStart, Date timeEnd) {
        if (timeStart == null || timeEnd == null) {
            return -1;
        }
        return timeEnd.getTime() - timeStart.getTime();
    }
}
