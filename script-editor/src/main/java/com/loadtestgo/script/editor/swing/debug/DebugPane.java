package com.loadtestgo.script.editor.swing.debug;

import com.loadtestgo.script.editor.swing.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DebugPane extends JPanel implements ActionListener {
    private MainWindow mainWindow;
    private JComboBox<String> stackFrame;
    private List<String> toolTips;
    private JTabbedPane tabs;
    private VariableTable thisTable;
    private VariableTable localsTable;
    private WatchedTableModel tableModel;
    private EvalTable evalTable;
    private ConsoleTextArea output;
    private DebugConsolePanel outputPane;
    private boolean enabled;

    public DebugPane(final MainWindow mainWindow) {
        this.mainWindow = mainWindow;
        enabled = false;
        JPanel panel = new JPanel();

        JLabel frameLabel = new JLabel("Stack Frame:");
        stackFrame = new JComboBox<>();
        stackFrame.setLightWeightPopupEnabled(false);
        toolTips = Collections.synchronizedList(new java.util.ArrayList<String>());
        frameLabel.setBorder(stackFrame.getBorder());
        stackFrame.addActionListener(this);
        stackFrame.setActionCommand("ContextSwitch");
        GridBagLayout layout = new GridBagLayout();
        panel.setLayout(layout);
        GridBagConstraints lc = new GridBagConstraints();
        lc.insets.left = 5;
        lc.anchor = GridBagConstraints.WEST;
        lc.ipadx = 5;
        layout.setConstraints(frameLabel, lc);
        panel.add(frameLabel);
        GridBagConstraints c = new GridBagConstraints();
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;
        layout.setConstraints(stackFrame, c);
        panel.add(stackFrame);
        tabs = new JTabbedPane(SwingConstants.BOTTOM);
        tabs.setPreferredSize(new Dimension(500,300));
        thisTable = new VariableTable(new VariableModel(mainWindow.getDebugger()));
        JScrollPane jsp = new JScrollPane(thisTable);
        jsp.getViewport().setViewSize(new Dimension(5, 2));
        tabs.add("this", jsp);
        localsTable = new VariableTable(new VariableModel(mainWindow.getDebugger()));
        localsTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        localsTable.setPreferredSize(null);
        jsp = new JScrollPane(localsTable);
        tabs.add("Locals", jsp);
        c.weightx = c.weighty = 1;
        c.gridheight = GridBagConstraints.REMAINDER;
        c.fill = GridBagConstraints.BOTH;
        c.anchor = GridBagConstraints.WEST;
        layout.setConstraints(tabs, c);
        panel.add(tabs);
        evalTable = new EvalTable(mainWindow);
        tableModel = evalTable.tableModel;
        jsp = new JScrollPane(evalTable);
        tabs.add("Watch", jsp);

        outputPane = new DebugConsolePanel(mainWindow.getDebugger());
        output = outputPane.getConsoleTextArea();
        tabs.add("Output", outputPane);

        tabs.setPreferredSize(new Dimension(500, 300));

        setLayout(new BorderLayout());
        add(panel);

        setEnabled(false);
        output.setEnabled(true);

        tabs.setSelectedComponent(outputPane);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        stackFrame.setEnabled(enabled);
        thisTable.setEnabled(enabled);
        localsTable.setEnabled(enabled);
        evalTable.setEnabled(enabled);
    }

    public void disableUpdate() {
        enabled = false;
    }

    public void enableUpdate() {
        enabled = true;
    }

    public void actionPerformed(ActionEvent e) {
        if (!enabled) return;
        if (e.getActionCommand().equals("ContextSwitch")) {
            contextSwitch();
        }
    }

    public ConsoleTextArea getConsoleArea() {
        return output;
    }

    public JComboBox<String> getStackFrameComboBox() {
        return stackFrame;
    }

    public List<String> getContextToolTips() {
        return toolTips;
    }

    public void clearOutput() {
        output.setText("");
    }

    private void contextSwitch() {
        Debugger debugger = mainWindow.getDebugger();

        Debugger.ContextData contextData = debugger.currentContextData();
        if (contextData == null) {
            return;
        }

        int frameIndex = stackFrame.getSelectedIndex();
        stackFrame.setToolTipText(toolTips.get(frameIndex));
        int frameCount = contextData.frameCount();
        if (frameIndex >= frameCount) {
            return;
        }

        ArrayList<Debugger.Variable> localVars = new ArrayList<>();
        ArrayList<Debugger.Variable> thisVars = new ArrayList<>();
        Debugger.StackFrame frame = contextData.getFrame(frameIndex);
        debugger.getVariables(frame, localVars, thisVars);

        Object scope = frame.scope();
        Object thisObj = frame.thisObj();
        thisTable.resetTree(new VariableModel(debugger, localVars));
        VariableModel scopeModel;
        if (scope != thisObj) {
            scopeModel = new VariableModel(debugger, thisVars);
        } else {
            scopeModel = new VariableModel(debugger);
        }

        localsTable.resetTree(scopeModel);

        debugger.contextSwitch(frameIndex);
        mainWindow.showStopLine(frame);
        tableModel.updateModel();
    }

    public void updateStackTrace(Debugger.ContextData contextData) {
        JComboBox<String> ctx = getStackFrameComboBox();
        List<String> toolTips = getContextToolTips();
        disableUpdate();
        int frameCount = contextData.frameCount();
        ctx.removeAllItems();
        ctx.setSelectedItem(null);
        toolTips.clear();
        int charWidth = EditorSettings.getStackFrameCharWidth();
        for (int i = 0; i < frameCount; i++) {
            Debugger.StackFrame frame = contextData.getFrame(i);
            String url = frame.getFileName();
            int lineNumber = frame.getLineNumber();
            String shortName = url;
            if (url.length() > charWidth) {
                shortName = "..." + url.substring(url.length() + 3 - charWidth);
            }
            String location = "\"" + shortName + "\", line " + lineNumber;
            ctx.insertItemAt(location, i);
            location = "\"" + url + "\", line " + lineNumber;
            toolTips.add(location);
        }

        enableUpdate();
        ctx.setSelectedIndex(0);
        ctx.setMinimumSize(new Dimension(50, ctx.getMinimumSize().height));
        setEnabled(true);
    }

    public void clearStackTrace() {
        disableUpdate();
        toolTips.clear();
        stackFrame.removeAllItems();
        enableUpdate();
        setEnabled(false);
    }
}
