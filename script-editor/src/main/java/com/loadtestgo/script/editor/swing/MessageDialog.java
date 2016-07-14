package com.loadtestgo.script.editor.swing;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

class MessageDialog {
    public static void show(Component parent,
                            String message,
                            String title,
                            int optionType) {
        JOptionPane.showMessageDialog(parent,
                formatMessage(message),
                title,
                optionType);
    }

    enum Result {
        Ok,
        Yes,
        No,
        Cancel
    }

    public static Result confirm(Component parent,
                                 String message,
                                 String title,
                                 int optionType) {
        ArrayList<String> options = new ArrayList<>();
        Object defaultOption;
        switch (optionType){
            case JOptionPane.OK_CANCEL_OPTION:
                options.add(UIManager.getString("OptionPane.okButtonText"));
                options.add(UIManager.getString("OptionPane.cancelButtonText"));
                defaultOption = UIManager.getString("OptionPane.okButtonText");
                break;
            case JOptionPane.YES_NO_OPTION:
                options.add(UIManager.getString("OptionPane.yesButtonText"));
                options.add(UIManager.getString("OptionPane.noButtonText"));
                defaultOption = UIManager.getString("OptionPane.yesButtonText");
                break;
            case JOptionPane.YES_NO_CANCEL_OPTION:
                options.add(UIManager.getString("OptionPane.yesButtonText"));
                options.add(UIManager.getString("OptionPane.noButtonText"));
                options.add(UIManager.getString("OptionPane.cancelButtonText"));
                defaultOption = UIManager.getString("OptionPane.yesButtonText");
                break;
            default:
                throw new IllegalArgumentException("Unknown optionType " + optionType);
        }

        int button = JOptionPane.showOptionDialog(parent,
                        formatMessage(message),
                        title,
                        optionType,
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        options.toArray(),
                        defaultOption);

        switch (optionType){
            case JOptionPane.OK_CANCEL_OPTION:
                if (button == 0) {
                    return Result.Ok;
                } else {
                    return Result.Cancel;
                }
            case JOptionPane.YES_NO_OPTION:
                if (button == 0) {
                    return Result.Yes;
                } else if (button == 1) {
                    return Result.No;
                }
            case JOptionPane.YES_NO_CANCEL_OPTION:
                if (button == 0) {
                    return Result.Yes;
                } else if (button == 1) {
                    return Result.No;
                } else if (button == 2) {
                    return Result.Cancel;
                }
            default:
                break;
        }

        return Result.Cancel;
    }

    // Wrap the text in some html to cause wrapping
    static private String formatMessage(String message) {
        return String.format("<html><body><p style='width: 400px;'>%s</p></body></html>", message);
    }
}
