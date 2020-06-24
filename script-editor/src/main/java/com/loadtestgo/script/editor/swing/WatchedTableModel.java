package com.loadtestgo.script.editor.swing;

import javax.swing.table.AbstractTableModel;
import java.util.List;
import java.util.Vector;

public class WatchedTableModel extends AbstractTableModel {
    private MainWindow mainWindow;
    private List<String> expressions;
    private List<String> values;

    public WatchedTableModel(MainWindow mainWindow) {
        this.mainWindow = mainWindow;
        this.expressions = new Vector<>();
        this.values = new Vector<>();
        this.expressions.add("");
        this.values.add("");
    }

    public int getColumnCount() {
        return 2;
    }

    public int getRowCount() {
        return expressions.size();
    }

    @Override
    public String getColumnName(int column) {
        switch (column) {
        case 0:
            return "Expression";
        case 1:
            return "Value";
        }
        return null;
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return true;
    }

    public Object getValueAt(int row, int column) {
        switch (column) {
        case 0:
            return expressions.get(row);
        case 1:
            return values.get(row);
        }
        return "";
    }

    @Override
    public void setValueAt(Object value, int row, int column) {
        switch (column) {
        case 0:
            String expr = value.toString();
            expressions.set(row, expr);
            String result = "";
            if (expr.length() > 0) {
                result = mainWindow.getDebugger().evalString(expr);
                if (result == null) result = "";
            }
            values.set(row, result);
            updateModel();
            if (row + 1 == expressions.size()) {
                expressions.add("");
                values.add("");
                fireTableRowsInserted(row + 1, row + 1);
            }
            break;
        case 1:
            // just reset column 2; ignore edits
            fireTableDataChanged();
        }
    }

    public void updateModel() {
        for (int i = 0; i < expressions.size(); ++i) {
            String expr = expressions.get(i);
            String result;
            if (expr.length() > 0) {
                result = mainWindow.getDebugger().evalString(expr);
                if (result == null) result = "";
            } else {
                result = "";
            }
            result = result.replace('\n', ' ');
            values.set(i, result);
        }
        fireTableDataChanged();
    }
}
