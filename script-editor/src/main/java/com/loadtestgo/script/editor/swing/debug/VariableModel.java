package com.loadtestgo.script.editor.swing.debug;

import com.loadtestgo.script.editor.swing.TreeTableModel;

import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreePath;
import java.util.ArrayList;
import java.util.List;

class VariableModel implements TreeTableModel {
    private static final String[] cNames = { " Name", " Value" };
    private static final Class<?>[] cTypes = { TreeTableModel.class, String.class };

    private Debugger debugger;
    private VariableModel.VariableNode root;

    public VariableModel(Debugger debugger) {
        this.debugger = debugger;
    }

    public VariableModel(Debugger debugger, ArrayList<Debugger.Variable> variables) {
        this.debugger = debugger;
        this.root = new VariableModel.VariableNode();
        for (Debugger.Variable variable : variables) {
            root.addChild(new VariableNode(variable));
        }
    }

    public Object getRoot() {
        if (debugger == null) {
            return null;
        }
        return root;
    }

    public int getChildCount(Object nodeObj) {
        if (debugger == null) {
            return 0;
        }
        VariableModel.VariableNode node = (VariableModel.VariableNode) nodeObj;

        List<VariableModel.VariableNode> children = getChildren(node);
        if (children != null) {
            return children.size();
        }

        return 0;
    }

    public Object getChild(Object nodeObj, int i) {
        if (debugger == null) {
            return null;
        }
        VariableModel.VariableNode node = (VariableModel.VariableNode) nodeObj;
        if (node.children == null) {
            return null;
        }

        return node.children.get(i);
    }

    public boolean isLeaf(Object nodeObj) {
        if (debugger == null) {
            return true;
        }

        VariableModel.VariableNode node = (VariableModel.VariableNode)nodeObj;
        if (node.variable == null) {
            return false;
        }

        if (node.variable.hasProperties()) {
            return false;
        }

        return true;
    }

    public int getIndexOfChild(Object parentObj, Object childObj) {
        if (debugger == null) {
            return -1;
        }
        VariableModel.VariableNode parent = (VariableModel.VariableNode) parentObj;
        VariableModel.VariableNode child = (VariableModel.VariableNode) childObj;
        List<VariableModel.VariableNode> children = getChildren(parent);
        for (int i = 0; i != children.size(); i++) {
            if (children.get(i) == child) {
                return i;
            }
        }
        return -1;
    }

    public boolean isCellEditable(Object node, int column) {
        return column == 0;
    }

    public void setValueAt(Object value, Object node, int column) { }
    public void addTreeModelListener(TreeModelListener l) { }
    public void removeTreeModelListener(TreeModelListener l) { }
    public void valueForPathChanged(TreePath path, Object newValue) { }

    public int getColumnCount() {
        return cNames.length;
    }

    public String getColumnName(int column) {
        return cNames[column];
    }

    public Class<?> getColumnClass(int column) {
        return cTypes[column];
    }

    public Object getValueAt(Object nodeObj, int column) {
        if (debugger == null) {
            return null;
        }

        VariableModel.VariableNode node = (VariableModel.VariableNode)nodeObj;
        switch (column) {
        case 0: // Name
            return node.variable.name;
        case 1: // Value
            return node.variable.value;
        }
        return null;
    }

    private List<VariableModel.VariableNode> getChildren(VariableModel.VariableNode node) {
        if (node.children != null) {
            return node.children;
        }

        return null;
    }

    public void loadVariables(VariableNode node) {
        debugger.getVariableChildren(node.variable);
        if (node.variable.children != null) {
            for (Debugger.Variable child : node.variable.children) {
                node.addChild(new VariableNode(child));
            }
        }
    }

    public static class VariableNode {
        private Debugger.Variable variable;
        private List<VariableNode> children;
        private boolean childrenLoaded;

        public VariableNode(Debugger.Variable variable) {
            this.variable = variable;
        }

        public VariableNode() {
        }

        public void addChild(VariableNode node) {
            if (children == null) {
                children = new ArrayList<>();
            }
            children.add(node);
        }

        @Override
        public String toString() {
            if (variable == null) {
                return "null";
            }
            return variable.name;
        }

        public boolean areChildrenLoaded() {
            return childrenLoaded;
        }
    }
}
