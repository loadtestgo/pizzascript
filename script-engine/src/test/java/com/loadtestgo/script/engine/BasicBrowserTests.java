package com.loadtestgo.script.engine;

import com.loadtestgo.script.api.Page;
import com.loadtestgo.script.engine.internal.api.ChromeBrowser;
import com.loadtestgo.script.engine.internal.browsers.chrome.ChromeProcess;
import com.loadtestgo.script.har.HarWriter;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.IOException;
import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

@RunWith(JUnit4.class)
@Ignore // Too slow at home
public class BasicBrowserTests {
    @Test
    public void launchChrome() {
        EasyTestContext testContext = new EasyTestContext();

        try {
            ChromeProcess process = new ChromeProcess(testContext);
            process.start();
            process.close();
        } finally {
            testContext.cleanup();
        }
    }

    @Test
    public void openUrls() {
        String[] urls = {
            "https://www.yahoo.com/",
            "https://www.bbc.com/news/",
            "http://www.amazon.com/",
            "http://www.google.com/",
            "http://youtube.com/"
        };

        EasyTestContext testContext = new EasyTestContext();

        try {
            ChromeBrowser chrome = new ChromeBrowser(testContext);
            for (String url : urls) {
                chrome.open(url);
            }

            chrome.close();

            ArrayList<Page> pages = testContext.getTestResult().getPages();

            assertEquals(urls.length, pages.size());
            for (int i = 0; i < pages.size(); ++i) {
                Page page = pages.get(i);
                assert(page.getRequests().size() > 0);
                assertEquals(urls[i], page.getOrigUrl());
            }
        } finally {
            testContext.cleanup();
        }
    }

    @Test
    public void saveHar() throws IOException {
        String[] urls = {
            "https://www.yahoo.com/",
            "https://www.bbc.com/news/",
            "http://www.amazon.com/",
            "http://www.google.com/",
            "http://youtube.com/"
        };

        EasyTestContext testContext = new EasyTestContext();

        try {
            ChromeBrowser chrome = new ChromeBrowser(testContext);
            for (String url : urls) {
                chrome.open(url);
            }
            chrome.close();

            HarWriter.save(testContext.getTestResult(), "tmp/har.js");
        } finally {
            testContext.cleanup();
        }
    }
}
