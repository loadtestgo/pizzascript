package com.loadtestgo.script.editor.swing;

import javax.swing.*;

public class EvalTable extends JTable {
    public WatchedTableModel tableModel;

    public EvalTable(MainWindow mainWindow) {
        super(new WatchedTableModel(mainWindow));
        tableModel = (WatchedTableModel)getModel();
    }
}
