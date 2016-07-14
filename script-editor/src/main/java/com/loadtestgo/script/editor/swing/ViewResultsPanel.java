package com.loadtestgo.script.editor.swing;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.loadtestgo.script.api.TestResult;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import netscape.javascript.JSException;
import netscape.javascript.JSObject;
import org.pmw.tinylog.Logger;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import java.util.concurrent.atomic.AtomicBoolean;

public class ViewResultsPanel extends PanelWithHeader {
    private static boolean customUrlLoader = false;
    private TestResult testResult;
    private WebEngine webEngine;
    private AtomicBoolean loaded = new AtomicBoolean(false);

    public ViewResultsPanel(TestResult testResult) {
        this.testResult = testResult;
        init();
    }

    private Scene createScene() {
        WebView browser = new WebView();
        webEngine = browser.getEngine();

        setupPizzaURLLoader();

        webEngine.load("pizza://pizza/testresults.html");
        webEngine.getLoadWorker().stateProperty().addListener(new ChangeListener<Worker.State>() {
            @Override
            public void changed(ObservableValue<? extends Worker.State> observableValue, Worker.State state, Worker.State newState) {
                if (newState.equals(Worker.State.SUCCEEDED)) {
                    loaded.set(true);
                    loadResults();
                }
            }
        });
        return new Scene(browser);
    }

    public static void setupPizzaURLLoader() {
        if (!customUrlLoader) {
            URL.setURLStreamHandlerFactory(new URLStreamHandlerFactory() {
                @Override
                public URLStreamHandler createURLStreamHandler(String protocol) {
                    if (protocol.equals("pizza")) {
                        return new PizzaUrlHandler();
                    } else {
                        return null;
                    }
                }
            });

            customUrlLoader = true;
        }
    }

    private void init() {
        final JFXPanel fxPanel = new JFXPanel();
        setLayout(new BorderLayout());
        add(fxPanel, BorderLayout.CENTER);

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                initFX(fxPanel);
            }

            private void initFX(JFXPanel fxPanel) {
                // This method is invoked on the JavaFX thread
                Scene scene = createScene();
                fxPanel.setScene(scene);
            }
        });
    }

    @Override
    public void setTabHeader(JLabel tabLabel, JPanel tabHeader) {
        String testName = testResult.getTestName();
        if (testName == null) {
            testName = "unknown";
        }

        testName += " - results";

        if (tabLabel != null) {
            tabLabel.setText(testName);
        }

        if (tabHeader != null) {
            tabHeader.setToolTipText(testName);
        }
    }

    public TestResult getTestResult() {
        return testResult;
    }

    public void loadResults() {
        try {
            JSObject windowObj = (JSObject)webEngine.executeScript("window");
            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(testResult);
            windowObj.setMember("result", json);
            try {
                webEngine.executeScript("refreshResult()");
            } catch (JSException js) {
                Logger.error(js, "error updating result");
            }

        } catch (JsonProcessingException e) {
            return;
        }
    }

    public void showPage() {
        if (loaded.get()) {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    loadResults();
                }
            });
        }
    }

    private static class PizzaUrlHandler extends URLStreamHandler {
        private final ClassLoader classLoader;
        private final ClassLoader classLoaderWebShared;

        public PizzaUrlHandler() {
            this.classLoader = getClass().getClassLoader();
            this.classLoaderWebShared = webshared.Resource.class.getClassLoader();
        }

        @Override
        protected URLConnection openConnection(URL u) throws IOException {
            URL resourceUrl = classLoader.getResource("html" + u.getPath());
            if (resourceUrl == null) {
                String path = u.getPath();
                if (path.charAt(0) == '/') {
                    path = path.substring(1);
                }
                resourceUrl = classLoaderWebShared.getResource(path);
            }

            if (resourceUrl == null) {
                throw new IOException("Resource not found: " + u);
            }

            return resourceUrl.openConnection();
        }
    }
}
