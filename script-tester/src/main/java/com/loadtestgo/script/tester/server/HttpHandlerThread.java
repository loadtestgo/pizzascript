package com.loadtestgo.script.tester.server;

import com.loadtestgo.util.Path;
import org.apache.commons.io.IOUtils;
import org.pmw.tinylog.Logger;
import sun.net.www.protocol.file.FileURLConnection;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class HttpHandlerThread {
    private LinkedBlockingQueue<Socket> connectionQueue;
    private int threadNum;
    private PageRegistry pageRegistry;
    private Map<PageRegistry.PageInfo, Object> pages;
    private String baseUrl;

    public HttpHandlerThread(LinkedBlockingQueue<Socket> connectionQueue,
                             int threadNum,
                             PageRegistry pageRegistry,
                             String baseUrl) {
        this.connectionQueue = connectionQueue;
        this.threadNum = threadNum;
        this.pageRegistry = pageRegistry;
        this.pages = new HashMap<>();
        this.baseUrl = baseUrl;
    }

    public void start() {
        while (true) {
            Socket socket = null;
            try {
                socket = connectionQueue.take();
            } catch (InterruptedException e) {
                return;
            }

            try (InputStream input = socket.getInputStream()) {
                handleRequest(socket, new InputStreamReader(input));
            } catch (InterruptedException e) {
                Logger.error(e);
                // Thread interrupted, abort processing
                return;
            } catch (IOException e) {
                Logger.error(e);
            } catch (Throwable e) {
                Logger.error(e);
            }
        }
    }

    private void handleRequest(Socket socket, InputStreamReader input) throws IOException, InterruptedException {
        String line = input.readLine();
        if (line == null || line.isEmpty()) {
            // Speculative connections are often made by the browser, to improve page load times
            // for large sites where multiple connections are needed.
            Logger.info("{}: HTTP request line is empty", threadNum);
            return;
        }

        HttpRequestLine requestLine = new HttpRequestLine();
        if (!requestLine.parse(line)) {
            Logger.info("Unable to parse request line {}", line);
            return;
        }

        String location = requestLine.getLocation();
        Logger.info("{}: {} {}", threadNum, requestLine.getType(), location);

        try (OutputStream output = socket.getOutputStream()) {
            HttpRequest request = new HttpRequest(requestLine, input, output, socket, baseUrl);

            if (location.equals("/")) {
                handleRoot(request);
            } else if (location.startsWith("/files")) {
                handleFileRequest(request);
            } else {
                handlePageRequest(request);
            }
        }
    }

    private InputStream getResourceInputStream(String filename) throws FileNotFoundException {
        File userDir = new File(System.getProperty("user.dir"));
        String devPath = Path.join(userDir.getPath(), "script-tester/src/main/resources", filename);
        if (new File(devPath).exists()) {
            return new FileInputStream(devPath);
        } else {
            return HttpHandlerThread.class.getResourceAsStream(filename);
        }
    }

    private void handleFileRequest(HttpRequest request) throws IOException {
        String location = request.requestLine.getLocation();
        String remaining = location.substring("/files".length());
        if (remaining.length() == 0) {
            listFiles(request, "");
        } else if (remaining.startsWith("/")) {
            if (remaining.endsWith("/")) {
                listFiles(request, remaining.substring(1));
            } else {
                InputStream input = getResourceInputStream(remaining);
                if (input == null) {
                    handle404(request);
                } else {
                    request.readHeaders();

                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    IOUtils.copy(input, out);

                    request.writeOk();
                    request.writeDefaultHeaders();
                    // Cache data, this allow things like the prefetch test to work
                    request.writeHeader("Cache-Control", "max-age=2592000");
                    request.writeHeader("Content-Length", String.valueOf(out.size()));
                    if (remaining.endsWith(".json")) {
                        request.writeHeader("Access-Control-Allow-Origin", "*");
                    }
                    request.writeln();

                    out.writeTo(request.out);
                }
            }
        } else {
            handle404(request);
        }
    }

    private void listFiles(HttpRequest request, String directory) throws IOException {
        ArrayList<String> fileListing = new ArrayList<>();
        Enumeration<URL> en = getClass().getClassLoader().getResources(directory);
        if (en.hasMoreElements()) {
            URL url = en.nextElement();
            URLConnection urlConnection = url.openConnection();
            if (urlConnection instanceof JarURLConnection) {
                JarURLConnection jarConnection = (JarURLConnection)urlConnection;
                try (JarFile jar = jarConnection.getJarFile()) {
                    Enumeration<JarEntry> entries = jar.entries();
                    while (entries.hasMoreElements()) {
                        JarEntry entry = entries.nextElement();
                        if (entry.isDirectory()) {
                            fileListing.add(entry.getName() + "/");
                        } else {
                            fileListing.add(entry.getName());
                        }
                    }
                }
            } else if (urlConnection instanceof FileURLConnection) {
                try {
                    File dir = new File(url.toURI());
                    File[] files = dir.listFiles();
                    if (files != null) {
                        for (File file : files) {
                            if (file.isDirectory()) {
                                fileListing.add(file.getName() + "/");
                            } else {
                                fileListing.add(file.getName());
                            }
                        }
                    }
                } catch (URISyntaxException e) {
                    // Do nothing
                }
            }
        }

        request.readHeaders();
        request.write("HTTP/1.1 200 OK\r\n\r\n");

        request.write(String.format(
                "<html><head><title>List files</title></head><body><h1>Test Server</h1><h3>%s</h3>",
                directory));

        for (String file : fileListing) {
            request.write(String.format("<a href=\"%s\">%s</a><br/>", file, file));
        }

        request.write("</body></html>");
    }

    private void handlePageRequest(HttpRequest request) throws IOException {
        Object page = null;
        PageRegistry.PageInfo pageInfo = null;
        String url = request.requestLine.getLocation();
        for (PageRegistry.PageInfo pageInfoI : pages.keySet()) {
            if (url.startsWith(pageInfoI.baseUrl)) {
                pageInfo = pageInfoI;
                page = pages.get(pageInfo);
                break;
            }
        }

        if (page == null) {
            pageInfo = pageRegistry.getPageInfoForUrl(url);
            if (pageInfo == null) {
                try {
                    request.readHeaders();
                    handle404(request);
                } catch (Exception e) {
                    // ignore
                }
                return;
            }
            try {
                page = pageInfo.pageClass.newInstance();
            } catch (InstantiationException e) {
                handle500(request, e);
            } catch (IllegalAccessException e) {
                handle500(request, e);
            }
            pages.put(pageInfo, page);
        }

        handlePageRequest0(page, pageInfo, request);
    }

    private void handlePageRequest0(Object page, PageRegistry.PageInfo pageInfo, HttpRequest request) throws IOException {
        String location = request.requestLine.getLocation();

        String remaining = location.substring(pageInfo.baseUrl.length());
        request.path = remaining;

        PageRegistry.EndPoint endPoint = pageInfo.methodsMap.get(remaining);

        // Handle paths that end with *
        if (endPoint == null) {
            for (String key : pageInfo.methodsMap.keySet()) {
                if (key.endsWith("*")) {
                    String starPattern = key.substring(0, key.length() - 1);
                    if (remaining.startsWith(starPattern)) {
                        endPoint = pageInfo.methodsMap.get(key);
                        break;
                    }
                }
            }
        }

        if (endPoint == null) {
            handle404(request);
        } else {
            try {
                endPoint.method.invoke(page, request);
            } catch (IllegalAccessException e) {
                Logger.error(e, "Error handling request");
                handle500(request, e);
            } catch (InvocationTargetException e) {
                Logger.error(e, "Error handling request");
                handle500(request, e);
            }
        }
    }

    private void handle404(HttpRequest request) throws IOException {
        request.write("HTTP/1.1 404 Not Found\r\n\r\n");
        request.write("<html><head></head><body><h2>404 Not Found</h2>");
        request.write(String.format("No resource at %s", request.requestLine.getLocation()));
        request.write("</body></html>");
    }

    private void handle500(HttpRequest request, Exception e) throws IOException {
        request.write("HTTP/1.1 500 Internal Error\r\n\r\n");
        request.write("<html><head><title>Internal Error</title></head><body><h1>500 Internal Server Error</h1>");
        request.write(e.toString());
        request.write("</body></html>");
    }

    private void handleRoot(HttpRequest request) throws IOException {
        request.readHeaders();
        request.writeOk();
        request.writeln();
        request.write("<html><head><title>Test Server</title></head><body><h1>Test Server</h1>");

        request.write("<h2>Bad Listeners</h2>");
        request.write("<table>");

        for (Server server : pageRegistry.servers()) {
            String fullUrl = server.getUrl();
            String desc = server.getDescription();
            request.write(String.format(
                    "<tr><td><a href=\"%s\">%s</a></td><td><a href=\"%s\">%s</a></td></tr>",
                    fullUrl, fullUrl, fullUrl, desc));
        }

        request.write("</table>");

        for (PageRegistry.PageInfo pageInfo : pageRegistry.getPageInfo()) {
            request.write(String.format("<h2>%s</h2>", pageInfo.pageClass.getSimpleName()));
            request.write("<table>");
            for (String url : pageInfo.methodsMap.keySet()) {
                PageRegistry.EndPoint endPoint = pageInfo.methodsMap.get(url);
                String fullUrl = pageInfo.baseUrl + url;
                request.write(String.format(
                        "<tr><td><a href=\"%s\">%s</a></td><td><a href=\"%s\">%s</a></td></tr>",
                        fullUrl, fullUrl, fullUrl, endPoint.desc));
            }
            request.write("</table>");
        }
        request.write("<h2>Static Files</h2><a href=\"files/\">List</a>");
        request.write("</body></html>");
    }
}
