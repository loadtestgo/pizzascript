package com.loadtestgo.script.editor.swing;

import javax.swing.*;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreePath;
import java.awt.*;

class VariableTable extends JTreeTable {
    private VariableModel model;

    public VariableTable(VariableModel model) {
        super(model);
        this.model = model;
    }

    public JTree resetTree(TreeTableModel treeTableModel) {
        tree = new TreeTableCellRenderer(treeTableModel);

        tree.addTreeWillExpandListener(new TreeWillExpandListener() {
            @Override
            public void treeWillExpand(TreeExpansionEvent event) throws ExpandVetoException {
                TreePath path = event.getPath();
                Object lastPathComponent = path.getLastPathComponent();
                if (lastPathComponent instanceof VariableModel.VariableNode) {
                    VariableModel.VariableNode node = (VariableModel.VariableNode)lastPathComponent;
                    expandNode(node);
                }
            }

            @Override
            public void treeWillCollapse(TreeExpansionEvent event) throws ExpandVetoException {
            }
        });


        super.setModel(new TreeTableModelAdapter(treeTableModel, tree));

        ListToTreeSelectionModelWrapper selectionWrapper = new
            ListToTreeSelectionModelWrapper();
        tree.setSelectionModel(selectionWrapper);
        setSelectionModel(selectionWrapper.getListSelectionModel());

        if (tree.getRowHeight() < 1) {
            setRowHeight(18);
        }

        setDefaultRenderer(TreeTableModel.class, tree);
        setDefaultEditor(TreeTableModel.class, new TreeTableCellEditor());
        setShowGrid(true);
        setIntercellSpacing(new Dimension(1,1));
        tree.setRootVisible(false);
        tree.setShowsRootHandles(true);
        DefaultTreeCellRenderer r = (DefaultTreeCellRenderer)tree.getCellRenderer();
        r.setOpenIcon(null);
        r.setClosedIcon(null);
        r.setLeafIcon(null);
        return tree;
    }

    private void expandNode(VariableModel.VariableNode node) {
        if (node.areChildrenLoaded()) {
            return;
        }
        model.loadVariables(node);
    }
}
