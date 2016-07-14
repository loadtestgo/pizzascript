package com.loadtestgo.script.editor.swing;

import javax.swing.*;

public abstract class PanelWithHeader extends JPanel {
    abstract public void setTabHeader(JLabel tabLabel, JPanel tabHeader);
}
