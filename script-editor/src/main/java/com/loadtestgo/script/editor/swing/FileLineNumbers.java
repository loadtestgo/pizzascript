package com.loadtestgo.script.editor.swing;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class FileLineNumbers extends JPanel implements MouseListener {
    private FilePanel filePanel;
    private int pressLine = -1;

    public FileLineNumbers(FilePanel filePanel) {
        this.filePanel = filePanel;
        addMouseListener(this);
        update();
    }

    public void update() {
        FileTextArea textArea = filePanel.getTextArea();
        Font font = textArea.getFont();
        setFont(font);
        FontMetrics metrics = getFontMetrics(font);
        int h = metrics.getHeight();
        int lineCount = textArea.getLineCount();
        String dummy = Integer.toString(lineCount);
        if (dummy.length() < 1) {
            dummy = "00";
        }
        Dimension d = new Dimension();
        d.width = metrics.stringWidth(dummy) + 16;
        d.height = lineCount * h + 100;
        setPreferredSize(d);
        setSize(d);
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        FileTextArea textArea = filePanel.getTextArea();
        SourceFile sourceFile = filePanel.getSourceFile();
        Font font = textArea.getFont();
        g.setFont(font);
        FontMetrics metrics = getFontMetrics(font);
        Rectangle clip = g.getClipBounds();
        g.setColor(getBackground());
        g.fillRect(clip.x, clip.y, clip.width, clip.height);
        int ascent = metrics.getMaxAscent();
        int h = metrics.getHeight();
        int lineCount = textArea.getLineCount();
        int startLine = clip.y / h;
        int endLine = (clip.y + clip.height) / h + 1;
        int width = getWidth();

        if (endLine > lineCount) {
            endLine = lineCount;
        }

        for (int i = startLine; i < endLine; i++) {
            String text;

            int pos = -2;
            try {
                pos = textArea.getLineStartOffset(i);
            } catch (BadLocationException ignored) {
            }

            boolean isBreakPoint = sourceFile.isBreakpoint(i + 1);

            text = Integer.toString(i + 1) + " ";
            int y = i * h;
            g.setColor(new Color(0x0000FF));
            g.drawString(text, 0, y + ascent);
            int x = width - ascent;

            if (isBreakPoint) {
                boolean isValidBreakPoint = sourceFile.breakableLine(i + 1);

                g.setColor(new Color(0x800000));
                int dy = y + ascent - 10;
                g.drawOval(x, dy, 10, 10);
                if (isValidBreakPoint) {
                    g.fillOval(x, dy, 10, 10);
                }
            }

            // Draw the debugger arrow
            if (pos == filePanel.getCurrentPos()) {
                Polygon arrow = new Polygon();
                int dx = x;
                y += ascent - 10;
                int dy = y;
                arrow.addPoint(dx, dy + 3);
                arrow.addPoint(dx + 5, dy + 3);
                for (x = dx + 5; x <= dx + 10; x++, y++) {
                    arrow.addPoint(x, y);
                }
                for (x = dx + 9; x >= dx + 5; x--, y++) {
                    arrow.addPoint(x, y);
                }
                arrow.addPoint(dx + 5, dy + 7);
                arrow.addPoint(dx, dy + 7);
                g.setColor(new Color(0xFFFF00));
                g.fillPolygon(arrow);
                g.setColor(new Color(0x000000));
                g.drawPolygon(arrow);
            }
        }
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mousePressed(MouseEvent e) {
        Font font = filePanel.getTextArea().getFont();
        FontMetrics metrics = getFontMetrics(font);
        int h = metrics.getHeight();
        pressLine = e.getY() / h;
    }

    public void mouseClicked(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mouseReleased(MouseEvent e) {
        if (e.getComponent() == this
                && (e.getModifiers() & MouseEvent.BUTTON1_MASK) != 0) {
            int y = e.getY();
            Font font = filePanel.getTextArea().getFont();
            FontMetrics metrics = getFontMetrics(font);
            int h = metrics.getHeight();
            int line = y/h;
            if (line == pressLine) {
                filePanel.toggleBreakPoint(line + 1);
            } else {
                pressLine = -1;
            }
        }
    }
}
