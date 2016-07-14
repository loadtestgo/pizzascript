package com.loadtestgo.script.editor.swing;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Collections;
import java.util.List;

class FindFunctionDialog extends CustomDialog implements ActionListener {
    private String value;
    private JList<String> list;
    private MainWindow mainWindow;
    private CodeModel codeModel;
    private JButton setButton;
    private JButton cancelButton;

    public FindFunctionDialog(MainWindow mainWindow, CodeModel codeModel) {
        super(mainWindow);
        this.mainWindow = mainWindow;
        this.codeModel = codeModel;

        setTitle("Go to Function");

        cancelButton = new JButton("Cancel");
        setButton = new JButton("Select");
        cancelButton.addActionListener(this);
        setButton.addActionListener(this);
        getRootPane().setDefaultButton(setButton);

        DefaultListModel<String> model = new DefaultListModel<String>();
        list = new JList<String>(model);
        model.clear();

        List<String> functionNames = codeModel.functionNames();
        Collections.sort(functionNames);
        for (String name : functionNames) {
            if (name == null) {
                name = "undefined";
            }
            model.addElement(name + "()");
        }
        list.setSelectedIndex(0);

        setButton.setEnabled(functionNames.size() > 0);
        list.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        list.addMouseListener(new FindFunctionDialog.MouseHandler());
        JScrollPane scrollPane = new JScrollPane(list);
        scrollPane.setPreferredSize(new Dimension(320, 240));
        scrollPane.setMinimumSize(new Dimension(250, 80));
        scrollPane.setAlignmentX(LEFT_ALIGNMENT);

        JPanel listPane = new JPanel();
        listPane.setLayout(new BoxLayout(listPane, BoxLayout.Y_AXIS));
        JLabel label = new JLabel("Function");
        label.setLabelFor(list);
        listPane.add(label);
        listPane.add(Box.createRigidArea(new Dimension(0, 5)));
        listPane.add(scrollPane);
        listPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel buttonPane = new JPanel();
        buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.X_AXIS));
        buttonPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        buttonPane.add(Box.createHorizontalGlue());
        buttonPane.add(cancelButton);
        buttonPane.add(Box.createRigidArea(new Dimension(10, 0)));
        buttonPane.add(setButton);

        Container contentPane = getContentPane();
        contentPane.add(listPane, BorderLayout.CENTER);
        contentPane.add(buttonPane, BorderLayout.SOUTH);
        pack();

        addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent ke) {
                    int code = ke.getKeyCode();
                    if (code == KeyEvent.VK_ESCAPE) {
                        ke.consume();
                        value = null;
                        setVisible(false);
                    }
                }
            });
    }

    public String showDialog(Component comp) {
        value = null;
        setLocationRelativeTo(comp);
        setVisible(true);
        return value;
    }

    public void actionPerformed(ActionEvent e) {
        String cmd = e.getActionCommand();
        if (cmd.equals("Cancel")) {
            setVisible(false);
            value = null;
        } else if (cmd.equals("Select")) {
            if (list.getSelectedIndex() < 0) {
                return;
            }
            try {
                value = list.getSelectedValue();
            } catch (ArrayIndexOutOfBoundsException exc) {
                return;
            }
            setVisible(false);
            FunctionSource item = codeModel.functionSourceByName(value.replace("()", ""));
            if (item != null) {
                SourceFile sourceFile = item.sourceFile();
                String filePath = sourceFile.getFilePath();
                mainWindow.showFilePanel(filePath, item.firstLine());
            }
        }
    }

    class MouseHandler extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent e) {
            if (e.getClickCount() == 2) {
                setButton.doClick();
            }
        }
    }
}
