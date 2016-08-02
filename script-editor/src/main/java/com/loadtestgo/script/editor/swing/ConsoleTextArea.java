package com.loadtestgo.script.editor.swing;

import com.loadtestgo.util.HtmlEntities;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import java.awt.*;
import java.io.IOException;

public class ConsoleTextArea extends JEditorPane
{
    private ConsoleOutputStream consoleOut;
    private HTMLDocument document;
    private HTMLEditorKit htmlEditorKit;
    private PageClickListener pageClickListener;
    private JScrollPane scrollPane;

    public ConsoleTextArea() {
        super();
        setEditable(false);
        setAutoscrolls(true);

        consoleOut = new ConsoleOutputStream(this);

        StyleSheet styleSheet = new StyleSheet();
        styleSheet.addRule(".info{color:black;}");
        styleSheet.addRule(".warn{color:#D05050;}");
        styleSheet.addRule(".error{color:#FF0000;}");
        styleSheet.addRule(".warn{color:#D05050;}");
        styleSheet.addRule(".pmt{color:#505050;}");
        styleSheet.addRule(".cmd{color:black;}");
        styleSheet.addRule(".inspect{color:#FF00FF;}");
        styleSheet.addRule("a{color:blue;text-decoration:underline;}");

        Font font = EditorSettings.getCodeFont();
        styleSheet.addRule(String.format("body{font-family:%s;font-size:%dpt;}",
                font.getFamily(), font.getSize()));

        addHyperlinkListener(new HyperlinkListener() {
            public void hyperlinkUpdate(HyperlinkEvent e) {
                if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                    String url = e.getDescription();
                    if (url.startsWith("page:")) {
                        String pageId = url.split(":")[1];
                        pageClickListener.pageClicked(consoleOut.getTestResult(), pageId);
                    }
                }
            }
        });

        htmlEditorKit = new HTMLEditorKit();
        htmlEditorKit.setStyleSheet(styleSheet);
        document = (HTMLDocument) htmlEditorKit.createDefaultDocument();
        setEditorKit(htmlEditorKit);
        setDocument(document);

        // Don't update the caret position unless we say so
        // the caret position controls scrolling
        DefaultCaret caret = (DefaultCaret) getCaret();
        caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
    }

    public ConsoleOutputStream getConsoleOut() {
        return consoleOut;
    }

    public void setPageClickListener(PageClickListener pageClickListener) {
        this.pageClickListener = pageClickListener;
    }

    public void println(String str, String color) {
        appendHtml(String.format("<pre style='color:%s'>%s</pre>", color, encodeHtml(str)));
    }

    public void printlnClass(String str, String type) {
        appendHtml(String.format("<pre class='%s'>%s</pre>", type, encodeHtml(str)));
    }

    public void printCommand(String prompt, String cmd) {
        appendHtml(String.format(
            "<pre><span class='pmt'>%s</span><span class='cmd'>%s</span></pre>", prompt, encodeHtml(cmd)));
    }

    public void logInfo(String info) {
        printlnClass(info, "info");
    }

    public void logWarn(String warn) {
        printlnClass(warn, "warn");
    }

    public void logError(String error) {
        printlnClass(error, "error");
    }

    public void appendInspect(String inspect) {
        printlnClass(inspect, "inspect");
    }

    public int getColumns() {
        int width = getWidth();
        FontMetrics fontMetrics = getFontMetrics(getFont());
        int charWidth = fontMetrics.charWidth('a');
        int columns = 0;
        if (charWidth > 0) {
            columns = width / charWidth;
        }

        if (columns > 120) {
            columns = 120;
        }

        return columns;
    }

    public void appendHtml(String html)
    {
        try {
            // Are we scrolled to the end of the document?
            boolean scrolledToEnd = false;
            if (scrollPane != null) {
                JScrollBar vertical = scrollPane.getVerticalScrollBar();
                int posMax = vertical.getMaximum();
                int posCurrent = vertical.getValue() + vertical.getModel().getExtent();
                scrolledToEnd = (posMax == posCurrent);
            }

            htmlEditorKit.insertHTML(document, document.getLength(), html, 0, 0, null);

            // If we were scrolled to the end of the document before, make sure
            // we are again
            if (scrolledToEnd) {
                setCaretPosition(getDocument().getLength());
            }
        } catch (BadLocationException e) {
            // Do nothing
        } catch (IOException e) {
            // Do nothing
        }
    }

    public void clear() {
        try {
            document.replace(0, document.getLength(), "", null);
        } catch (BadLocationException e) {
            // Do nothing
        }
    }

    private String encodeHtml(String html) {
        if (html != null) {
            return HtmlEntities.encode(html);
        } else {
            return null;
        }
    }

    public void setScrollPane(JScrollPane scrollPane) {
        this.scrollPane = scrollPane;
    }
}
