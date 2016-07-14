package com.loadtestgo.script.editor.swing;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

import javax.swing.*;
import java.awt.*;

public class HelpWindow extends JFrame {
    private String url;
    private final JPanel panel = new JPanel(new BorderLayout());

    public HelpWindow() {
        init();
    }

    private Scene createScene() {
        WebView browser = new WebView();
        WebEngine webEngine = browser.getEngine();
        webEngine.load(url);
        return new Scene(browser);
    }

    private void init() {
        final JFXPanel fxPanel = new JFXPanel();
        panel.add(fxPanel, BorderLayout.CENTER);
        getContentPane().add(panel);
        setPreferredSize(new Dimension(1024, 700));
        pack();

        setVisible(true);
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

    public void setUrl(String url) {
        this.url = url;
    }
}
