package com.loadtestgo.script.editor.swing;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.loadtestgo.script.api.ErrorType;
import com.loadtestgo.script.api.TestResult;
import com.loadtestgo.script.engine.ConsoleNotifier;
import com.loadtestgo.script.engine.ScriptException;
import com.loadtestgo.script.engine.internal.browsers.chrome.ChromeProcess;
import com.loadtestgo.script.har.HarWriter;
import com.loadtestgo.util.Os;
import com.loadtestgo.util.Path;
import org.mozilla.javascript.Kit;
import org.mozilla.javascript.SecurityUtilities;
import org.pmw.tinylog.Logger;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class MainWindow extends JFrame implements DebuggerCallbacks, PageClickListener {
    private Debugger debugger;
    private CodeModel codeModel;
    private JTabbedPane tabbedPane;
    private DebugPane debugPane;
    private JToolBar toolBar;
    private ConsolePanel console;
    private JMenu windowMenu;
    private JSplitPane splitPane;
    private JLabel statusBar;
    private Map<String,FilePanel> fileTabs;
    private List<ViewResultsPanel> testResultPanels;
    private FileDialog fileDialog;
    private FileDialog resultsFileDialog;
    private FileDialog exportHarFileDialog;
    private JCheckBoxMenuItem breakOnExceptions;
    private JCheckBoxMenuItem keepBrowserOpenToggle;
    private JCheckBoxMenuItem sideBySideBrowserOpenToggle;
    private JPanel contentPane;
    private HelpWindow helpWindow;
    private int newFileIndex;

    private Action newAction = new NewAction();
    private Action newFileConsoleAction = new NewFileConsoleAction();
    private Action openAction = new OpenAction();
    private Action saveAction = new SaveAction();
    private Action saveAsAction = new SaveAsAction();
    private Action importResultAction = new ImportResultAction();
    private Action exportAction = new ExportAction();
    private Action exportHarAction = new ExportHarAction();
    private Action exitAction = new ExitAction();

    private Action undoAction = new UndoAction();
    private Action redoAction = new RedoAction();
    private Action cutAction = new DefaultEditorKit.CutAction();
    private Action copyAction = new DefaultEditorKit.CopyAction();
    private Action pasteAction = new DefaultEditorKit.PasteAction();
    private Action selectAllAction= new SelectAllAction();
    private Action gotoFunctionAction = new GotoFunctionAction();
    private Action findAction = new FindAction();
    private Action replaceAction = new ReplaceAction();

    private Action runAction = new RunAction();
    private Action stopAction = new StopAction();
    private Action breakAction = new BreakAction();
    private Action stepIntoAction = new StepIntoAction();
    private Action stepOverAction = new StepOverAction();
    private Action stepOutAction = new StepOutAction();
    private Action toggleBreakPointAction = new ToggleBreakPointAction();
    private Action breakOnExceptionsAction = new BreakOnExceptionsAction();
    private Action keepBrowserOpenAction = new KeepBrowserOpenAction();
    private Action openBrowserSideBySideAction = new OpenBrowserSideBySideAction();

    private Action gotoConsoleAction = new GotoConsoleAction();
    private Action resetConsoleAction = new ResetConsoleAction();
    private Action closeTabAction = new CloseTabAction();
    private Action nextTabAction = new NextTabAction();
    private Action prevTabAction = new PrevTabAction();

    private Action helpHomeAction = new OnlineHelpAction("Home Page", "http://pizzascript.org/");
    private Action helpApiAction = new OnlineHelpAction("Script API", "http://pizzascript.org/api/");
    private Action helpWikiAction = new OnlineHelpAction("PizzaScript Wiki", "https://github.com/loadtestgo/pizzascript/wiki");

    private int defaultWindowMenuItems;
    private double debugDividerLocation = 0.66;

    public MainWindow(String title, Debugger debugger) {
        super(title);
        this.debugger = debugger;
        this.codeModel = debugger.getCodeModel();
        this.fileTabs = new ConcurrentHashMap<>();
        this.testResultPanels = new ArrayList<>();
        this.newFileIndex = 1;

        prettyActions();

        setJMenuBar(createMenuBar());
        init();

        debugger.setGuiCallback(this);
    }

    private void prettyActions() {
        cutAction.putValue(Action.NAME, "Cut");
        cutAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_X);
        cutAction.putValue(Action.ACCELERATOR_KEY,
                KeyStroke.getKeyStroke(KeyEvent.VK_X,
                    Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));

        copyAction.putValue(Action.NAME, "Copy");
        copyAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_C);
        copyAction.putValue(Action.ACCELERATOR_KEY,
                KeyStroke.getKeyStroke(KeyEvent.VK_C,
                        Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));

        pasteAction.putValue(Action.NAME, "Paste");
        pasteAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_V);
        pasteAction.putValue(Action.ACCELERATOR_KEY,
            KeyStroke.getKeyStroke(KeyEvent.VK_V,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
    }

    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        JMenu editMenu = new JMenu("Edit");
        JMenu debugMenu = new JMenu("Debug");
        windowMenu = new JMenu("Window");
        JMenu helpMenu = new JMenu("Help");
        menuBar.add(fileMenu);
        menuBar.add(editMenu);
        menuBar.add(debugMenu);
        menuBar.add(windowMenu);
        menuBar.add(helpMenu);

        fileMenu.add(newAction);
        fileMenu.add(newFileConsoleAction);
        fileMenu.addSeparator();
        fileMenu.add(openAction);
        fileMenu.addSeparator();
        fileMenu.add(saveAction);
        fileMenu.add(saveAsAction);
        fileMenu.addSeparator();
        fileMenu.add(importResultAction);
        fileMenu.add(exportAction);
        fileMenu.add(exportHarAction);

        if (!Os.isMac()) {
            fileMenu.addSeparator();
            fileMenu.add(exitAction);
        }

        editMenu.add(undoAction);
        editMenu.add(redoAction);
        editMenu.addSeparator();
        editMenu.add(cutAction);
        editMenu.add(copyAction);
        editMenu.add(pasteAction);
        editMenu.add(selectAllAction);
        editMenu.addSeparator();
        editMenu.add(findAction);
        editMenu.add(replaceAction);
        editMenu.add(gotoFunctionAction);

        debugMenu.add(runAction);
        debugMenu.add(stopAction);
        debugMenu.add(breakAction);
        debugMenu.add(stepIntoAction);
        debugMenu.add(stepOverAction);
        debugMenu.add(stepOutAction);
        debugMenu.addSeparator();

        debugMenu.add(toggleBreakPointAction);

        breakOnExceptions = new JCheckBoxMenuItem();
        breakOnExceptions.setAction(breakOnExceptionsAction);
        breakOnExceptions.setSelected(false);
        debugMenu.add(breakOnExceptions);

        keepBrowserOpenToggle = new JCheckBoxMenuItem();
        keepBrowserOpenToggle.setAction(keepBrowserOpenAction);
        keepBrowserOpenToggle.setSelected(false);
        debugMenu.add(keepBrowserOpenToggle);

        windowMenu.add(nextTabAction);
        windowMenu.add(prevTabAction);
        windowMenu.add(closeTabAction);
        windowMenu.addSeparator();
        windowMenu.add(gotoConsoleAction);
        windowMenu.add(resetConsoleAction);

        sideBySideBrowserOpenToggle = new JCheckBoxMenuItem();
        sideBySideBrowserOpenToggle.setAction(openBrowserSideBySideAction);
        sideBySideBrowserOpenToggle.setSelected(false);
        windowMenu.add(sideBySideBrowserOpenToggle);

        defaultWindowMenuItems = windowMenu.getItemCount();

        helpMenu.add(helpHomeAction);
        helpMenu.add(helpApiAction);
        helpMenu.add(helpWikiAction);

        return menuBar;
    }

    private void init() {
        toolBar = new JToolBar();
        toolBar.setFloatable(false);

        JButton runButton = new JButton("Run");
        runButton.setAction(runAction);
        runButton.setToolTipText("Run (F5)");

        JButton stopButton = new JButton("Stop");
        stopButton.setAction(stopAction);
        stopButton.setToolTipText("Stop (SHIFT+F5)");

        JButton breakButton = new JButton("Break");
        breakButton.setAction(breakAction);
        breakButton.setToolTipText("Break (CTRL+F5)");

        JButton stepIntoButton = new JButton("Step Into");
        stepIntoButton.setAction(stepIntoAction);
        stepIntoButton.setToolTipText("Step Into (F11)");

        JButton stepOverButton = new JButton("Step Over");
        stepOverButton.setAction(stepOverAction);
        stepOverButton.setToolTipText("Step Over (F10)");

        JButton stepOutButton = new JButton("Step Out");
        stepOutButton.setAction(stepOutAction);
        stepOutButton.setToolTipText("Step Out (SHIFT+F11)");

        Dimension dim = stepOverButton.getPreferredSize();
        setMixMaxSize(runButton, dim);
        setMixMaxSize(stopButton, dim);
        setMixMaxSize(breakButton, dim);
        setMixMaxSize(stepIntoButton, dim);
        setMixMaxSize(stepOverButton, dim);
        setMixMaxSize(stepOutButton, dim);

        toolBar.add(runButton);
        toolBar.add(stopButton);
        toolBar.add(breakButton);
        toolBar.add(stepIntoButton);
        toolBar.add(stepOverButton);
        toolBar.add(stepOutButton);

        contentPane = new JPanel();
        contentPane.setLayout(new BorderLayout());
        getContentPane().add(toolBar, BorderLayout.NORTH);
        getContentPane().add(contentPane, BorderLayout.CENTER);

        tabbedPane = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
        tabbedPane.setPreferredSize(new Dimension(600, 300));
        tabbedPane.setMinimumSize(new Dimension(150, 50));
        addConsoleFrame();
        tabbedPane.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                tabChanged();
            }
        });

        debugPane = new DebugPane(this);
        debugPane.setPreferredSize(new Dimension(600, 120));
        debugPane.setMinimumSize(new Dimension(50, 50));

        ConsoleTextArea consoleTextArea = debugPane.getConsoleArea();
        consoleTextArea.setPageClickListener(this);
        debugger.setOutput(consoleTextArea.getConsoleOut());

        sideBySideBrowserOpenToggle.setState(EditorSettings.sideBySideBrowserWindow());

        EditorTestContext.WindowPosition windowPosition = new EditorTestContext.WindowPosition() {
            @Override
            public int[] getWindowPosition() {
                Dimension dimension = getSize();
                Point location = getLocation();
                if (sideBySideBrowserOpenToggle.getState()) {
                    return new int[]{
                        location.x + dimension.width,
                        location.y,
                        dimension.width,
                        dimension.height
                    };
                } else {
                    return null;
                }
            }
        };

        debugger.setWindowPosition(windowPosition);
        console.setWindowPosition(windowPosition);

        splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, tabbedPane, debugPane);
        splitPane.setBorder(new EmptyBorder(0, 0, 0, 0));
        splitPane.setDividerLocation(debugDividerLocation);

        statusBar = new JLabel();
        statusBar.setBorder(new EmptyBorder(0, 2, 2, 2));
        statusBar.setText("Ready");

        contentPane.add(splitPane, BorderLayout.CENTER);
        contentPane.add(statusBar, BorderLayout.SOUTH);

        fileDialog = new FileDialog(this);
        fileDialog.setMode(FileDialog.LOAD);

        FilenameFilter filter =
            new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    int i = name.lastIndexOf('.');
                    if (i > 0 && i < name.length() -1) {
                        String ext = name.substring(i + 1).toLowerCase();
                        if (ext.equals("js")) {
                            return true;
                        }
                    }
                    return false;
                }
            };

//        fileDialog.setFilenameFilter(filter);

        FilenameFilter filterJson =
            new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    int i = name.lastIndexOf('.');
                    if (i > 0 && i < name.length() -1) {
                        String ext = name.substring(i + 1).toLowerCase();
                        if (ext.equals("js") || ext.equals("json")) {
                            return true;
                        }
                    }
                    return false;
                }
            };

        resultsFileDialog = new FileDialog(this);
        resultsFileDialog.setMode(FileDialog.SAVE);
        resultsFileDialog.setFilenameFilter(filterJson);

        exportHarFileDialog = new FileDialog(this);
        exportHarFileDialog.setMode(FileDialog.SAVE);
        exportHarFileDialog.setFilenameFilter(filterJson);

        updateUndoState();
        updateDebugState();
        updateTabState();

        registerShortcuts();

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent ev) {
                closeAllTabsAndExit();
            }
        });

        // Anytime the user switches back to this app set the focus to be
        // the console line edit or the file panel text input.
        addWindowFocusListener(new WindowAdapter() {
            public void windowGainedFocus(WindowEvent ev) {
                setTabDefaultFocus();
            }
        });
    }

    private void registerShortcuts() {
        // Shortcuts without Actions associated
        getRootPane().registerKeyboardAction(
                new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        console.setSnippet("var b = pizza.open(\"www.google.com\");");
                    }
                },
                KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK),
                JComponent.WHEN_IN_FOCUSED_WINDOW);
        getRootPane().registerKeyboardAction(
            new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    console.setSnippet("var b = pizza.open(\"localhost:3000/files/findElements.html\");");
                }
            },
            KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK),
            JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    public void setTabDefaultFocus() {
        Component component = tabbedPane.getSelectedComponent();
        if (component == null) {
            return;
        }

        if (component instanceof FilePanel) {
            FilePanel panel = (FilePanel)component;
            panel.setDefaultFocus();
            return;
        }

        if (component instanceof ConsolePanel) {
            ConsolePanel panel = (ConsolePanel)component;
            panel.setDefaultFocus();
        }
    }

    private ConsolePanel getConsolePanel() {
        Component component = tabbedPane.getSelectedComponent();
        if (component == null) {
            return null;
        }

        if (component instanceof ConsolePanel) {
            return (ConsolePanel)component;
        }

        return null;
    }

    static private void setMixMaxSize(JButton button, Dimension dim) {
        button.setMinimumSize(dim);
        button.setMaximumSize(dim);
        button.setPreferredSize(dim);
    }

    // Called when the tab changes
    private void tabChanged() {
        updateUndoState();
        updateDebugState();
        setTabDefaultFocus();
    }

    private void addConsoleFrame() {
        console = new ConsolePanel();
        console.getTextArea().setPageClickListener(this);

        tabbedPane.addTab(null, console);

        int pos = tabbedPane.indexOfComponent(console);

        FlowLayout layout = new FlowLayout(FlowLayout.CENTER, 5, 0);

        JPanel tabHeader = new JPanel(layout);
        tabHeader.setOpaque(false);
        tabHeader.setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));
        tabHeader.setToolTipText("PizzaScript Console");

        JLabel tabLabel = new JLabel("Console");
        tabLabel.setFocusable(false);

        final JTabbedPane finalTabbedPane = tabbedPane;
        final JComponent finalConsole = console;

        tabHeader.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) { }

            @Override
            public void mousePressed(MouseEvent e) { }

            @Override
            public void mouseReleased(MouseEvent e) {
                finalTabbedPane.setSelectedComponent(finalConsole);
            }

            @Override
            public void mouseEntered(MouseEvent e) { }

            @Override
            public void mouseExited(MouseEvent e) { }
        });

        tabHeader.add(tabLabel);

        tabbedPane.setTabComponentAt(pos, tabHeader);
    }

    public void showTestResultsTab(TestResult testResult, String pageId) {
        ViewResultsPanel panel = getTestResultsPanel(testResult);

        if (panel == null) {
            panel = new ViewResultsPanel(testResult);
            testResultPanels.add(panel);
            addClosableTab(tabbedPane, panel);
        }

        panel.showPage();

        tabbedPane.setSelectedComponent(panel);
        updateTabState();
    }

    private ViewResultsPanel getTestResultsPanel(TestResult testResult) {
        for (ViewResultsPanel panel : testResultPanels) {
            if (panel.getTestResult() == testResult) {
                return panel;
            }
        }
        return null;
    }

    public FilePanel getFilePanel(String filePath) {
        return fileTabs.get(filePath);
    }

    public void showStopLine(Debugger.StackFrame frame) {
        String fileName = frame.getFileName();
        FilePanel panel = getFilePanel(fileName);
        int lineNumber = frame.getLineNumber();
        if (panel != null) {
            showFilePanel(panel);
            setFilePosition(panel, lineNumber);
        }
    }

    private void setFilePosition(FilePanel filePanel, int line) {
        if (filePanel == null) {
            return;
        }

        JTextArea textArea = filePanel.getTextArea();
        try {
            if (line < 0) {
                filePanel.setPosition(-1);
            } else {
                int location = textArea.getLineStartOffset(line - 1);
                filePanel.setPosition(location);
            }
        } catch (BadLocationException ignored) {
        }
    }

    public void showFilePanel(String fileName, int lineNumber, boolean showDebugFrame) {
        FilePanel filePanel = getFilePanel(fileName);
        if (filePanel == null) {
            SourceFile sourceFile = debugger.getSourceFile(fileName);
            if (sourceFile != null) {
                filePanel = createFilePanel(sourceFile, showDebugFrame);
            } else {
                filePanel = openFile(fileName, showDebugFrame);
            }
        }

        filePanel.setDefaultFocus();

        showFilePanel(filePanel);

        if (lineNumber > 0) {
            filePanel.selectLine(lineNumber);
        }
    }

    private void showFilePanel(FilePanel panel) {
        tabbedPane.setSelectedComponent(panel);
    }

    private FilePanel createFilePanel(SourceFile sourceFile, boolean showDebugFrame) {
        String filePath = sourceFile.getFilePath();
        FilePanel panel = new FilePanel(this, codeModel, sourceFile);
        panel.setShowDebugFrame(showDebugFrame);
        fileTabs.put(filePath, panel);
        addClosableTab(tabbedPane, panel);
        addFileToMenu(panel);
        tabbedPane.setSelectedComponent(panel);
        updateTabState();
        return panel;
    }

    private void addFileToMenu(FilePanel filePanel) {
        int count = windowMenu.getItemCount();
        if (count == defaultWindowMenuItems) {
            windowMenu.addSeparator();
        }

        JMenuItem item = new FilePanelMenuItem(filePanel);
        windowMenu.add(item);
    }

    void closeFileTabWithSaveCheck(FilePanel filePanel) {
        SourceFile file = filePanel.getSourceFile();
        if (file.isModified()) {
            if (!trySaveFile(filePanel)) {
                return;
            }
        }
        closeFileTab(filePanel);
    }

    private void closeFileTab(FilePanel filePanel) {
        fileTabs.remove(filePanel.getFilePath());
        filePanel.getSourceFile().clearBreakpoints();
        closeTab(filePanel);
    }

    private void closeTab(PanelWithHeader panel) {
        int count = windowMenu.getItemCount();
        if (count == defaultWindowMenuItems) {
            windowMenu.addSeparator();
            count++;
        }

        for (int i = 1; i < windowMenu.getItemCount(); ++i) {
            JMenuItem item = windowMenu.getItem(i);
            if (item instanceof FilePanelMenuItem) {
                FilePanelMenuItem filePanelItem = (FilePanelMenuItem) item;
                if (filePanelItem.getFilePanel() == panel) {
                    windowMenu.remove(i);
                    break;
                }
            }
        }

        // Remove the separator if there are no windows open
        if (windowMenu.getItemCount() == defaultWindowMenuItems + 1) {
            windowMenu.remove(defaultWindowMenuItems);
        }

        tabbedPane.remove(panel);

        updateTabState();

        setTabDefaultFocus();
    }

    private void showNextTab() {
        showTabOffset(1);
    }

    private void showPrevTab() {
        showTabOffset(-1);
    }

    private void showTabOffset(int offset) {
        if (tabbedPane.getTabCount() > 1) {
            int index = (tabbedPane.getSelectedIndex() + offset) % tabbedPane.getTabCount();
            if (index < 0) {
                index += tabbedPane.getTabCount();
            }
            tabbedPane.setSelectedIndex(index);
        }
        updateTabState();
    }

    private String chooseFile(String title, int mode) {
        return chooseFile(fileDialog, title, mode);
    }

    private String chooseFile(FileDialog fileDialog, String title, int mode) {
        fileDialog.setTitle(title);
        fileDialog.setMode(mode);
        String dir = SecurityUtilities.getSystemProperty("user.dir");
        if (dir != null) {
            fileDialog.setDirectory(dir);
        }
        fileDialog.setVisible(true);
        String file = fileDialog.getFile();
        if (file != null) {
            try {
                Properties props = System.getProperties();
                props.put("user.dir", fileDialog.getDirectory());
                System.setProperties(props);
                return Path.join(fileDialog.getDirectory(), file);
            } catch (SecurityException ignored) {
            }
        }
        return null;
    }

    private void saveAs(FilePanel filePanel) {
        String filePath = chooseFile("Save As", FileDialog.SAVE);
        if (filePath != null) {
            save(filePanel, filePath);
        }
    }

    private void save(FilePanel filePanel) {
        save(filePanel, filePanel.getFilePath());
    }

    private void save(FilePanel filePanel, String filePath) {
        File file = new File(filePath);
        File renamedFile = null;
        int i = 0;
        while (true) {
            if (!file.exists()) {
                break;
            }

            renamedFile = new File(file.getParent(),
                    String.format(".%s.%d", file.getName(), i));

            if (file.renameTo(renamedFile)) {
                break;
            }
        }
        FileWriter writer = null;
        try {
            writer = new FileWriter(file);
            filePanel.getTextArea().write(writer);
            if (renamedFile != null) {
                renamedFile.delete();
                renamedFile = null;
            }
            SourceFile source = filePanel.getSourceFile();
            fileTabs.remove(source.getFilePath());
            fileTabs.put(filePath, filePanel);
            source.setNewFile(false);
            source.setIsModified(false);
            source.setFilePath(filePath);
            updateFilePanelName(filePanel);
        } catch (IOException ex) {
            if (renamedFile != null) {
                renamedFile.renameTo(file);
            }

            MessageDialog.show(MainWindow.this,
                    "File Not Saved", "ERROR", JOptionPane.ERROR_MESSAGE);
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException x) {
                    // Do nothing
                }
            }
        }
    }

    private void importResults() {
        String filePath = chooseFile(resultsFileDialog, "Import Results", FileDialog.LOAD);
        if (filePath != null) {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

            try {
                TestResult result = objectMapper.readValue(new File(filePath), TestResult.class);
                showTestResultsTab(result, "");
            } catch (Exception e) {
                Logger.warn(e, "Problem reading json from {}", filePath);
                MessageDialog.show(MainWindow.this,
                    String.format("Problem loading file: %s", e.getMessage()),
                    "ERROR", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private TestResult getTestResult() {
        ViewResultsPanel panel = getCurrentResultsPanel();
        if (panel != null) {
            return panel.getTestResult();
        }

        ConsolePanel console = getConsolePanel();
        if (console != null) {
            return console.getTestResult();
        }

        return debugger.getTestResult();
    }

    private void exportResults() {
        TestResult result = getTestResult();
        if (result == null) {
            return;
        }

        String filePath = chooseFile(resultsFileDialog, "Export Results", FileDialog.SAVE);
        if (filePath != null) {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false);
            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            try {
                objectMapper.writeValue(new File(filePath), result);
            } catch (Exception e) {
                Logger.warn(e, "Problem writing json to {}", filePath);
                MessageDialog.show(MainWindow.this,
                    String.format("File Not Saved: %s", e.getMessage()),
                    "ERROR", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void exportHar() {
        TestResult result = getTestResult();
        if (result == null) {
            return;
        }

        String filePath = chooseFile(exportHarFileDialog, "Export HAR", FileDialog.SAVE);
        if (filePath != null) {
            try {
                HarWriter.save(result, filePath);
            } catch (Exception e) {
                Logger.warn(e, "Problem writing Http Archive file to {}", filePath);
                MessageDialog.show(MainWindow.this,
                    String.format("File Not Saved: %s", e.getMessage()),
                    "ERROR", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void updateFilePanelName(FilePanel filePanel) {
        filePanel.updateUIFromFileName();

        String name = Path.getFileName(filePanel.getFilePath());
        for (int i = 1; i < windowMenu.getItemCount(); ++i) {
            JMenuItem item = windowMenu.getItem(i);
            if (item instanceof FilePanelMenuItem) {
                FilePanelMenuItem filePanelItem = (FilePanelMenuItem) item;
                if (filePanelItem.getFilePanel() == filePanel) {
                    filePanelItem.updateFileName(name);
                }
            }
        }
    }

    public FilePanel openFile(String filePath, boolean showDebugPane) {
        String source;
        try {
            Reader r = new FileReader(filePath);
            try {
                source = Kit.readReader(r);
            } finally {
                r.close();
            }
            SourceFile sourceFile = codeModel.newFile(source, filePath);
            sourceFile.setNewFile(false);
            sourceFile.setIsModified(false);
            return createFilePanel(sourceFile, showDebugPane);
        } catch (IOException ex) {
            MessageDialog.show(MainWindow.this,
                    ex.getMessage(),
                    "Error reading " + filePath,
                    JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }

    private void newFile() {
        newFile("");
    }

    private void newFileConsoleCopy() {
        newFile(console.getHistory());
    }

    private void newFile(String contents) {
        SourceFile sourceFile = codeModel.newFile(contents,
            String.format("unnamed%s.js", newFileIndex++));
        sourceFile.setNewFile(true);
        sourceFile.setIsModified(true); // It's not saved, so mark it to be saved
        createFilePanel(sourceFile, false);
    }

    public void enterInterrupt(Debugger.StackFrame lastFrame, Throwable exception) {
        final Debugger.StackFrame finalLastFrame = lastFrame;
        final Throwable finalException = exception;
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                showStopLine(finalLastFrame);

                if (finalException != null) {
                    handleException(finalException);
                }

                debugPane.updateStackTrace(finalLastFrame.contextData());
                updateDebugState();
                statusBar.setText("Break");
            }
        });
    }

    @Override
    public void evalScriptContinue() {
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                updateDebugState();
                setFilePosition(getCurrentFilePanel(), -1);
                statusBar.setText("Running...");
            }
        });
    }

    @Override
    public void evalScriptStarted() {
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                debugPane.clearOutput();
                updateDebugState();
                setFilePosition(getCurrentFilePanel(), -1);
                statusBar.setText("Running...");
            }
        });
    }

    @Override
    public void evalScriptStopped(Throwable exception) {
        final Throwable finalException = exception;
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                debugPane.clearStackTrace();
                updateDebugState();
                setFilePosition(getCurrentFilePanel(), -1);
                statusBar.setText("Done");
                handleException(finalException);
            }
        });
    }

    private void handleException(Throwable throwable) {
        if (throwable == null) {
            return;
        }

        ScriptException se = null;
        if (throwable instanceof ScriptException) {
            se = (ScriptException)throwable;
        }

        if (se != null && se.getErrorType() == ErrorType.Stopped) {
            return;
        }

        ConsoleNotifier out = debugPane.getConsoleArea().getConsoleOut();
        out.logError(String.format("Error: %s", throwable.getMessage()));

        if (se != null) {
            showFilePanel(se.getFile(), se.getLine(), true);

            if (se.getJSStackTrace() != null) {
                out.logError(se.prettyJSStackTrace());
            }
        }
    }

    public JPanel getCurrentPanel() {
        Component component = tabbedPane.getSelectedComponent();
        if (component == null) {
            return null;
        }

        if (component instanceof JPanel) {
            return ((JPanel)component);
        }

        return null;
    }

    public FilePanel getCurrentFilePanel() {
        Component component = tabbedPane.getSelectedComponent();
        if (component == null) {
            return null;
        }

        if (component instanceof FilePanel) {
            return ((FilePanel)component);
        }

        return null;
    }

    public ViewResultsPanel getCurrentResultsPanel() {
        Component component = tabbedPane.getSelectedComponent();
        if (component == null) {
            return null;
        }

        if (component instanceof ViewResultsPanel) {
            return ((ViewResultsPanel)component);
        }

        return null;
    }

    public JTextComponent getCurrentTextArea() {
        Component component = tabbedPane.getSelectedComponent();
        if (component == null) {
            return null;
        }

        if (component instanceof FilePanel) {
            return ((FilePanel)component).getTextArea();
        } else if (component instanceof ConsolePanel) {
            return ((ConsolePanel)component).getTextArea();
        }

        return null;
    }

    private void addClosableTab(JTabbedPane tabbedPane, PanelWithHeader panel) {
        tabbedPane.addTab(null, panel);

        int pos = tabbedPane.indexOfComponent(panel);

        FlowLayout layout = new FlowLayout(FlowLayout.CENTER, 5, 0);

        JPanel tabHeader = new JPanel(layout);
        tabHeader.setOpaque(false);
        tabHeader.setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));

        JLabel tabLabel = new JLabel();
        tabLabel.setFocusable(false);

        panel.setTabHeader(tabLabel, tabHeader);

        final PanelWithHeader finalPanel = panel;

        tabHeader.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) { }

            @Override
            public void mousePressed(MouseEvent e) { }

            @Override
            public void mouseReleased(MouseEvent e) {
                switchToPanel(finalPanel);
            }

            @Override
            public void mouseEntered(MouseEvent e) { }

            @Override
            public void mouseExited(MouseEvent e) { }
        });

        JButton closeButton = new JButton();
        closeButton.setOpaque(false);
        closeButton.setRolloverEnabled(true);
        closeButton.setFocusable(false);

        if (Os.isWin()) {
            // The Windows theme JLabel's have a nice rollover effect already
            closeButton.setText("X");
            // But the margin is a bit wide so narrow it...
            closeButton.setMargin(new Insets(0,5,0,4));
        } else {
            closeButton.setIcon(Resources.TAB_CLOSE_HIGHLIGHT_ICON);
            closeButton.setRolloverIcon(Resources.TAB_CLOSE_ICON);
            closeButton.setBorder(null);
        }

        tabHeader.add(tabLabel);
        tabHeader.add(closeButton);

        tabbedPane.setTabComponentAt(pos, tabHeader);

        closeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                closePanel(finalPanel);
            }
        });

        KeyStroke closeShortcut = KeyStroke.getKeyStroke(KeyEvent.VK_W,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());

        InputMap inputMap = panel.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        inputMap.put(closeShortcut, "closeTab");
        panel.getActionMap().put("closeTab",  new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                closePanel(finalPanel);
            }
        });
    }

    private void switchToPanel(PanelWithHeader panel) {
        if (panel instanceof ViewResultsPanel) {
            ViewResultsPanel viewResultsPanel = (ViewResultsPanel)panel;
            viewResultsPanel.showPage();
        }
        tabbedPane.setSelectedComponent(panel);
    }

    private void closePanel(PanelWithHeader panel) {
        if (panel instanceof FilePanel) {
            closeFileTabWithSaveCheck((FilePanel)panel);
        } else if (panel instanceof ViewResultsPanel) {
            testResultPanels.remove(panel);
            closeTab(panel);
        }
    }

    public Debugger getDebugger() {
        return debugger;
    }

    public void updateUndoState() {
        FilePanel filePanel = getCurrentFilePanel();
        boolean undoEnable = false;
        boolean redoEnable = false;
        if (filePanel != null) {
            if (filePanel.canRedo()) {
                redoEnable = true;
            }

            if (filePanel.canUndo()) {
                undoEnable = true;
            }
        }
        redoAction.setEnabled(redoEnable);
        undoAction.setEnabled(undoEnable);
    }

    public void updateDebugState() {
        boolean showDebugPane = false;
        FilePanel panel = getCurrentFilePanel();
        if (panel == null) {
            runAction.setEnabled(false);
            stopAction.setEnabled(false);
            breakAction.setEnabled(false);
            stepIntoAction.setEnabled(false);
            stepOverAction.setEnabled(false);
            stepOutAction.setEnabled(false);
            toggleBreakPointAction.setEnabled(false);
        } else {
            boolean enableRun = !debugger.isRunning() || debugger.isInterrupted();
            runAction.setEnabled(enableRun);
            stopAction.setEnabled(debugger.isRunning());
            boolean enableBreak = debugger.isRunning() && !debugger.isInterrupted();
            breakAction.setEnabled(enableBreak);
            stepIntoAction.setEnabled(debugger.isInterrupted());
            stepOverAction.setEnabled(debugger.isInterrupted());
            stepOutAction.setEnabled(debugger.isInterrupted());
            toggleBreakPointAction.setEnabled(true);
            showDebugPane = debugger.isRunning() || panel.showDebugFrame();
        }

        debugPane.setVisible(showDebugPane);
        if (showDebugPane) {
            panel.setShowDebugFrame(showDebugPane);
            splitPane.setDividerSize(8);
            splitPane.setDividerLocation(debugDividerLocation);
        } else {
            splitPane.setDividerSize(0);
        }
    }

    private void updateTabState() {
        boolean moreThanOne = (tabbedPane.getTabCount() > 1);
        gotoConsoleAction.setEnabled(moreThanOne);
        nextTabAction.setEnabled(moreThanOne);
        prevTabAction.setEnabled(moreThanOne);

        boolean isFileTab = (getCurrentFilePanel() != null);
        gotoFunctionAction.setEnabled(isFileTab);
        replaceAction.setEnabled(isFileTab);
        closeTabAction.setEnabled(isFileTab);
    }

    private void debugScript(FilePanel filePanel) {
        if (filePanel == null) {
            return;
        }
        SourceFile sourceFile = filePanel.getSourceFile();
        getDebugger().debugScript(sourceFile);
    }

    private void closeAllTabsAndExit() {
        Collection<FilePanel> panels = new ArrayList<>(fileTabs.values());
        for (FilePanel panel : panels) {
            SourceFile file = panel.getSourceFile();
            if (file.isModified()) {
                if (!trySaveFile(panel)) {
                    return;
                }
            }
            closeFileTab(panel);
        }

        console.close();

        dispose();
        System.exit(0);
    }

    private boolean trySaveFile(FilePanel panel) {
        SourceFile file = panel.getSourceFile();
        MessageDialog.Result result = MessageDialog.confirm(this,
                String.format("%s has unsaved changes.  Save?", file.getFilePath()),
                "Save changes...", JOptionPane.YES_NO_CANCEL_OPTION);
        if (result == MessageDialog.Result.Cancel) {
            return false;
        } else if (result == MessageDialog.Result.Yes) {
            save(panel);
            return true;
        }

        return true;
    }

    @Override
    public void pageClicked(TestResult testResult, String pageId) {
        showTestResultsTab(testResult, pageId);
    }

    private void resetConsole() {
        console.reset();
    }

    class NewAction extends AbstractAction {
        public NewAction() {
            super("New File");
            putValue(MNEMONIC_KEY, KeyEvent.VK_N);
            putValue(ACCELERATOR_KEY,
                     KeyStroke.getKeyStroke(KeyEvent.VK_N,
                            Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        }

        public void actionPerformed(ActionEvent ev) {
            newFile();
        }
    }

    class NewFileConsoleAction extends AbstractAction {
        public NewFileConsoleAction() {
            super("Copy Console New File");
        }

        public void actionPerformed(ActionEvent ev) {
            newFileConsoleCopy();
        }
    }

    class OpenAction extends AbstractAction {
        public OpenAction() {
            super("Open...");
            putValue(MNEMONIC_KEY, KeyEvent.VK_O);
            putValue(ACCELERATOR_KEY,
                     KeyStroke.getKeyStroke(KeyEvent.VK_O,
                            Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        }

        public void actionPerformed(ActionEvent ev) {
            String filePath = chooseFile("Select a file to open", FileDialog.LOAD);
            if (filePath != null) {
                openFile(filePath, false);
            }
        }
    }

    // An action that saves the document to a file
    class SaveAction extends AbstractAction {
        public SaveAction() {
            super("Save");
            putValue(MNEMONIC_KEY, KeyEvent.VK_S);
            putValue(ACCELERATOR_KEY,
                     KeyStroke.getKeyStroke(KeyEvent.VK_S,
                            Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        }

        public void actionPerformed(ActionEvent ev) {
            FilePanel filePanel = getCurrentFilePanel();
            if (filePanel == null) {
                return;
            }

            if (filePanel.getSourceFile().isNewFile()) {
                saveAs(filePanel);
            } else {
                save(filePanel);
            }
        }
    }

    class SaveAsAction extends AbstractAction {
        public SaveAsAction() {
            super("Save As...");
            putValue(MNEMONIC_KEY, KeyEvent.VK_A);
            putValue(ACCELERATOR_KEY,
                     KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.SHIFT_DOWN_MASK |
                            Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        }

        public void actionPerformed(ActionEvent ev) {
            FilePanel filePanel = getCurrentFilePanel();
            if (filePanel == null) {
                return;
            }

            saveAs(filePanel);
        }
    }

    private class ImportResultAction extends AbstractAction {
        public ImportResultAction() {
            super("Import Results...");
            putValue(MNEMONIC_KEY, KeyEvent.VK_I);
            putValue(ACCELERATOR_KEY,
                KeyStroke.getKeyStroke(KeyEvent.VK_I,
                    Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            importResults();
        }
    }

    private class ExportAction extends AbstractAction {
        public ExportAction() {
            super("Export Results...");
            putValue(MNEMONIC_KEY, KeyEvent.VK_E);
            putValue(ACCELERATOR_KEY,
                KeyStroke.getKeyStroke(KeyEvent.VK_E,
                    Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            exportResults();
        }
    }

    private class ExportHarAction extends AbstractAction {
        public ExportHarAction() {
            super("Export HAR...");
            putValue(MNEMONIC_KEY, KeyEvent.VK_H);
            putValue(ACCELERATOR_KEY,
                KeyStroke.getKeyStroke(KeyEvent.VK_H,
                    Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            exportHar();
        }
    }

    public class ExitAction extends AbstractAction {
        public ExitAction() {
            super("Exit");
            putValue(MNEMONIC_KEY, KeyEvent.VK_Q);
            putValue(ACCELERATOR_KEY,
                     KeyStroke.getKeyStroke(KeyEvent.VK_Q,
                            Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        }

        public void actionPerformed(ActionEvent ev) {
            closeAllTabsAndExit();
        }
    }

    private class ResetConsoleAction extends AbstractAction {
        public ResetConsoleAction() {
            super("Reset Console");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            resetConsole();
        }
    }

    private class UndoAction extends AbstractAction  {
        public UndoAction() {
            super("Undo");
            putValue(MNEMONIC_KEY, KeyEvent.VK_Z);
            putValue(ACCELERATOR_KEY,
                     KeyStroke.getKeyStroke(KeyEvent.VK_Z,
                            Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            FilePanel filePanel = getCurrentFilePanel();
            if (filePanel != null) {
                filePanel.undo();
                updateUndoState();
            }
        }
    }

    private class RedoAction extends AbstractAction  {
        public RedoAction() {
            super("Redo");
            putValue(MNEMONIC_KEY, KeyEvent.VK_R);
            putValue(ACCELERATOR_KEY,
                     KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.SHIFT_DOWN_MASK |
                            Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            FilePanel filePanel = getCurrentFilePanel();
            if (filePanel != null) {
                filePanel.redo();
                updateUndoState();
            }
        }
    }

    private class SelectAllAction extends AbstractAction {
        public SelectAllAction() {
            super("Select All");
            putValue(MNEMONIC_KEY, KeyEvent.VK_A);
            putValue(ACCELERATOR_KEY,
                     KeyStroke.getKeyStroke(KeyEvent.VK_A,
                            Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            JTextComponent textArea = MainWindow.this.getCurrentTextArea();
            if (textArea != null) {
                textArea.selectAll();
            }
        }
    }

    private class GotoFunctionAction extends AbstractAction  {
        public GotoFunctionAction() {
            super("Go to Function...");
            putValue(MNEMONIC_KEY, KeyEvent.VK_G);
            putValue(ACCELERATOR_KEY,
                     KeyStroke.getKeyStroke(KeyEvent.VK_G,
                            Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            FindFunctionDialog dialog =
                    new FindFunctionDialog(MainWindow.this, codeModel);
            dialog.showDialog(MainWindow.this);
        }
    }

    private class FindAction extends AbstractAction  {
        public FindAction() {
            super("Find...");
            putValue(MNEMONIC_KEY, KeyEvent.VK_F);
            putValue(ACCELERATOR_KEY,
                    KeyStroke.getKeyStroke(KeyEvent.VK_F,
                            Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            JPanel panel = getCurrentPanel();
            if (panel != null) {
                if (panel instanceof ConsolePanel) {
                    ConsoleFindDialog dialog =
                        new ConsoleFindDialog(MainWindow.this,(ConsolePanel)panel);
                    dialog.showDialog(MainWindow.this);
                } else if (panel instanceof FilePanel) {
                    FindReplaceDialog dialog =
                        new FindReplaceDialog(MainWindow.this,
                            (FilePanel)panel, FindReplaceDialog.Mode.FIND);
                    dialog.showDialog(MainWindow.this);
                }
            }
        }
    }

    private class ReplaceAction extends AbstractAction  {
        public ReplaceAction() {
            super("Replace...");
            putValue(MNEMONIC_KEY, KeyEvent.VK_R);
            putValue(ACCELERATOR_KEY,
                    KeyStroke.getKeyStroke(KeyEvent.VK_R,
                            Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            FilePanel filePanel = getCurrentFilePanel();
            if (filePanel != null) {
                FindReplaceDialog dialog =
                        new FindReplaceDialog(MainWindow.this,
                                filePanel,
                                FindReplaceDialog.Mode.REPLACE);
                dialog.showDialog(MainWindow.this);
            }
        }
    }

    private class RunAction extends AbstractAction  {
        public RunAction() {
            super("Run");
            putValue(MNEMONIC_KEY, KeyEvent.VK_G);
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (debugger.isDebugging()) {
                debugger.doContinue();
            } else {
                FilePanel filePanel = getCurrentFilePanel();
                if (filePanel != null) {
                    debugScript(filePanel);
                }
            }
        }
    }

    private class StopAction extends AbstractAction {
        public StopAction() {
            super("Stop");
            putValue(MNEMONIC_KEY, KeyEvent.VK_S);
            putValue(ACCELERATOR_KEY,
                     KeyStroke.getKeyStroke(KeyEvent.VK_F5, InputEvent.SHIFT_DOWN_MASK));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            debugger.stop();
        }
    }

    private class BreakAction extends AbstractAction  {
        public BreakAction() {
            super("Break");
            putValue(MNEMONIC_KEY, KeyEvent.VK_B);
            putValue(ACCELERATOR_KEY,
                     KeyStroke.getKeyStroke(KeyEvent.VK_F5, InputEvent.CTRL_DOWN_MASK));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (!debugger.isDebugging()) {
                debugger.doBreak();
                statusBar.setText("Breaking (Will break on next JavaScript statement)...");
            }
        }
    }

    private class StepIntoAction extends AbstractAction  {
        public StepIntoAction() {
            super("Step Into");
            putValue(MNEMONIC_KEY, KeyEvent.VK_S);
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F11, 0));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            debugger.stepInto();
        }
    }

    private class StepOverAction extends AbstractAction  {
        public StepOverAction() {
            super("Step Over");
            putValue(MNEMONIC_KEY, KeyEvent.VK_O);
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F10, 0));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            debugger.stepOver();
        }
    }

    private class StepOutAction extends AbstractAction  {
        public StepOutAction() {
            super("Step Out");
            putValue(MNEMONIC_KEY, KeyEvent.VK_U);
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F11, InputEvent.SHIFT_DOWN_MASK));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            debugger.stepOut();
        }
    }

    private class BreakOnExceptionsAction extends AbstractAction  {
        public BreakOnExceptionsAction() {
            super("Break on Exceptions");
            putValue(MNEMONIC_KEY, KeyEvent.VK_X);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            getDebugger().setBreakOnExceptions(breakOnExceptions.isSelected());
        }
    }

    private class ToggleBreakPointAction extends AbstractAction  {
        public ToggleBreakPointAction() {
            super("Toggle Breakpoint");
            putValue(MNEMONIC_KEY, KeyEvent.VK_T);
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F9, 0));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            FilePanel filePanel = getCurrentFilePanel();
            if (filePanel != null) {
                filePanel.toggleBreakPoint();
            }
        }
    }

    private class KeepBrowserOpenAction extends AbstractAction {
        public KeepBrowserOpenAction() {
            super("Keep Browser Open");
            putValue(MNEMONIC_KEY, KeyEvent.VK_K);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            FilePanel filePanel = getCurrentFilePanel();
            if (filePanel != null) {
                getDebugger().setCleanupWhenDone(!keepBrowserOpenToggle.isSelected());
            }
        }
    }

    private class OpenBrowserSideBySideAction extends AbstractAction {
        public OpenBrowserSideBySideAction() {
            super("Open Browser Side by Side");
            putValue(MNEMONIC_KEY, KeyEvent.VK_K);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
        }
    }

    private class GotoConsoleAction extends AbstractAction  {
        public GotoConsoleAction() {
            super("Console");
            putValue(MNEMONIC_KEY, KeyEvent.VK_C);
            putValue(ACCELERATOR_KEY,
                     KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.SHIFT_DOWN_MASK |
                            Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            tabbedPane.setSelectedComponent(console);
            updateTabState();
        }
    }

    private class SelectFilePanelAction extends AbstractAction  {
        private FilePanel panel;

        public SelectFilePanelAction(FilePanel panel) {
            super(Path.getFileName(panel.getFilePath()));
            this.panel = panel;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            showFilePanel(panel);
        }
    }

    private class FilePanelMenuItem extends JMenuItem {
        private FilePanel filePanel;

        public FilePanelMenuItem(FilePanel filePanel) {
            this.filePanel = filePanel;
            setAction(new SelectFilePanelAction(filePanel));
        }

        public FilePanel getFilePanel() {
            return filePanel;
        }

        public void updateFileName(String name) {
            setText(name);
        }
    }

    private class CloseTabAction extends AbstractAction  {
        public CloseTabAction() {
            super("Close Tab");
            putValue(MNEMONIC_KEY, KeyEvent.VK_W);
            putValue(ACCELERATOR_KEY,
                    KeyStroke.getKeyStroke(KeyEvent.VK_W,
                            Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            FilePanel filePanel = getCurrentFilePanel();
            if (filePanel != null) {
                closeFileTabWithSaveCheck(filePanel);
            }
        }
    }

    private class NextTabAction extends AbstractAction  {
        public NextTabAction() {
            super("Next Tab");
            putValue(MNEMONIC_KEY, KeyEvent.VK_N);
            putValue(ACCELERATOR_KEY,
                     KeyStroke.getKeyStroke(KeyEvent.VK_TAB, InputEvent.CTRL_MASK));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            showNextTab();
        }
    }

    private class PrevTabAction extends AbstractAction  {
        public PrevTabAction() {
            super("Prev Tab");
            putValue(MNEMONIC_KEY, KeyEvent.VK_P);
            putValue(ACCELERATOR_KEY,
                     KeyStroke.getKeyStroke(KeyEvent.VK_TAB, InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            showPrevTab();
        }
    }

    public class OnlineHelpAction extends AbstractAction {
        private String url;

        public OnlineHelpAction(String name, String url) {
            super(name);
            this.url = url;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            showOnlineHelp(url);
        }
    }

    private void showOnlineHelp(String url) {
        String path = getJavaScriptDocsPath("/dummy.txt");
        boolean validPath = false;

        if (path != null && new File(path).exists()) {
            validPath = true;
        }

        // On OSX choose the default browser, don't have code for the other
        // OSes right now.
        if (Os.isMac()) {
            ArrayList<String> args = new ArrayList<>();
            args.add("/usr/bin/open");
            if (validPath) {
                args.add(path);
            } else {
                args.add(url);
            }
            ProcessBuilder builder = new ProcessBuilder(args);
            try {
                Process start = builder.start();
                start.waitFor();
                return;
            } catch (IOException | InterruptedException e) {
                Logger.error(e);
            }
        }

        // Fallback to using the configured version of chrome
        File chrome = ChromeProcess.findChrome();
        if (chrome != null) {
            ArrayList<String> args = new ArrayList<>();
            args.add(chrome.getPath());
            if (validPath) {
                args.add(path);
            } else {
                args.add(url);
            }
            ProcessBuilder builder = new ProcessBuilder(args);
            try {
                builder.start();
                return;
            } catch (IOException e) {
                Logger.error(e);
            }
        }

        // Ok even that didn't work, use the internal JavaFX browser then...
        if (helpWindow == null) {
            helpWindow = new HelpWindow();
        }
        if (validPath) {
            helpWindow.setUrl("file:" + path);
        } else {
            helpWindow.setUrl(url);
        }
        helpWindow.setVisible(true);
    }

    /**
     * Try to find the path to the docs folder, so we can open the local
     * docs.  Works in dev mode and in as a JAR.
     * @param file a dummy file that's in the top level of the resources directory
     * @return The path to the index.html
     */
    private String getJavaScriptDocsPath(String file) {
        URL classUrl = MainWindow.class.getResource(file);

        // The 'Paths.get(classUrl.toURI()).toFile()' thing is to handle Windows paths correctly
        if (classUrl == null) {
            return null;
        }

        String path = classUrl.toString();
        if (path.startsWith("jar:file:")) {
            int appPathEnd = path.lastIndexOf("lib/script-editor");
            if (appPathEnd > 0) {
                path = path.substring(4, appPathEnd) + "docs/index.html";
            } else {
                return null;
            }
        } else if (path.startsWith("file:")) {
            int appPathEnd = path.lastIndexOf("script-editor/build/resources");
            if (appPathEnd > 0) {
                path = path.substring(0, appPathEnd) + "jsdocs/index.html";
            } else {
                return null;
            }
        }

        try {
            return Paths.get(new URI(path)).toFile().getAbsolutePath();
        } catch (URISyntaxException e) {
            // ignore
        }

        return null;
    }
}
