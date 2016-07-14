package com.loadtestgo.script.editor.swing;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import java.awt.*;

public class FileTextArea extends RSyntaxTextArea
{
    public FileTextArea(String text) {
        setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVASCRIPT);
        setAntiAliasingEnabled(true);
        setTabSize(2);
        setTabsEmulated(true);
        setText(text);
        discardAllEdits();

        setFont(EditorSettings.getCodeFont());
    }

    public void selectLine(int pos) {
        if (pos >= 0) {
            try {
                int line = getLineOfOffset(pos);
                Rectangle rect = modelToView(pos);
                if (rect == null) {
                    select(pos, pos);
                } else {
                    try {
                        Rectangle nrect =
                            modelToView(getLineStartOffset(line + 1));
                        if (nrect != null) {
                            rect = nrect;
                        }
                    } catch (Exception exc) {
                        // Do nothing
                    }
                    JViewport vp = (JViewport)getParent();
                    Rectangle viewRect = vp.getViewRect();
                    if (viewRect.y + viewRect.height > rect.y) {
                        // need to scroll up
                        select(pos, pos);
                    } else {
                        // need to scroll down
                        rect.y += (viewRect.height - rect.height)/2;
                        scrollRectToVisible(rect);
                        select(pos, pos);
                    }
                }
            } catch (BadLocationException exc) {
                select(pos, pos);
            }
        }
    }
}
