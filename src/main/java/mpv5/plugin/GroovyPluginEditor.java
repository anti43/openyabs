/*
 This file is part of YaBS.

 YaBS is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 YaBS is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with YaBS.  If not, see <http://www.gnu.org/licenses/>.
 */
package mpv5.plugin;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;
import javax.swing.undo.UndoManager;
import mpv5.globals.Messages;
import mpv5.logging.Log;
import mpv5.ui.dialogs.Popup;
import mpv5.ui.modern.ModernTheme;
import mpv5.ui.modern.PrimaryButton;

/**
 * Editor for Groovy plugins inside YaBS: Groovy highlighting (the editor of
 * the Groovy console), line numbers, examples to insert, syntax check, and
 * "save and start" which reloads the plugin at once
 */
public final class GroovyPluginEditor extends JDialog {

    private static final long serialVersionUID = 1L;
    private static final String DOCS = "https://github.com/anti43/openyabs/blob/master/docs/Plugins.md";
    private final File file;
    private final JTextComponent editor;
    private final UndoManager undo = new UndoManager();
    private final JLabel status = new JLabel(" ");
    private String saved = "";

    /**
     * Opens the editor for the plugin file
     *
     * @param parent may be null
     * @param file a .groovy file in the plugins folder
     */
    public static void open(Component parent, File file) {
        Window w = parent == null ? null : SwingUtilities.getWindowAncestor(parent);
        GroovyPluginEditor e = new GroovyPluginEditor(w, file);
        e.setVisible(true);
    }

    private GroovyPluginEditor(Window owner, File file) {
        super(owner, String.format(Messages.GEDIT_TITLE.toString(), file.getName()), ModalityType.MODELESS);
        this.file = file;
        this.editor = createEditor();
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                close();
            }
        });

        editor.setFont(new Font(Font.MONOSPACED, Font.PLAIN, ModernTheme.px(13)));
        JScrollPane scroll = new JScrollPane(editor);
        scroll.setRowHeaderView(new LineNumbers(editor));
        scroll.getVerticalScrollBar().setUnitIncrement(ModernTheme.px(16));

        PrimaryButton run = new PrimaryButton(Messages.GEDIT_SAVE_RUN.toString(), "/images/16/filesave.png");
        run.setToolTipText(Messages.GEDIT_SAVE_RUN + " (Ctrl+S)");
        run.addActionListener(e -> saveAndRun());
        JButton check = new JButton(Messages.GEDIT_CHECK.toString());
        check.setToolTipText(Messages.GEDIT_CHECK + " (F5)");
        check.addActionListener(e -> check(true));
        JButton insert = new JButton(Messages.GEDIT_INSERT + " ▾");
        JPopupMenu snippets = new JPopupMenu();
        for (Map.Entry<String, String> s : snippets().entrySet()) {
            JMenuItem i = new JMenuItem(s.getKey());
            i.addActionListener(e -> insert(s.getValue()));
            snippets.add(i);
        }
        insert.addActionListener(e -> snippets.show(insert, 0, insert.getHeight()));
        JButton help = new JButton(Messages.GEDIT_HELP.toString());
        help.addActionListener(e -> browse(DOCS));
        JButton external = new JButton(Messages.GEDIT_EXTERNAL.toString());
        external.addActionListener(e -> external());
        JButton closeButton = new JButton(Messages.GEDIT_CLOSE.toString());
        closeButton.addActionListener(e -> close());

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, ModernTheme.px(6), 0));
        left.setOpaque(false);
        left.add(run);
        left.add(check);
        left.add(insert);
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, ModernTheme.px(6), 0));
        right.setOpaque(false);
        right.add(help);
        right.add(external);
        right.add(closeButton);
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBorder(BorderFactory.createEmptyBorder(ModernTheme.px(6), ModernTheme.px(6), ModernTheme.px(6), ModernTheme.px(6)));
        bar.add(left, BorderLayout.WEST);
        bar.add(right, BorderLayout.EAST);

        status.setBorder(BorderFactory.createEmptyBorder(ModernTheme.px(4), ModernTheme.px(8), ModernTheme.px(4), ModernTheme.px(8)));
        status.setFont(ModernTheme.font(-1, Font.PLAIN));
        hint();

        JPanel content = new JPanel(new BorderLayout());
        content.add(bar, BorderLayout.NORTH);
        content.add(scroll, BorderLayout.CENTER);
        content.add(status, BorderLayout.SOUTH);
        setContentPane(content);

        keys();
        load();
        setSize(new Dimension(ModernTheme.px(900), ModernTheme.px(680)));
        setLocationRelativeTo(owner);
    }

    /**
     * The editor of the Groovy console (highlighting, auto indent), else a
     * plain text area
     */
    private static JTextComponent createEditor() {
        try {
            groovy.console.ui.text.TextEditor t = new groovy.console.ui.text.TextEditor(true, true, true);
            DefaultStyledDocument doc = new DefaultStyledDocument();
            t.setDocument(doc);
            doc.setDocumentFilter(new SafeFilter(new groovy.console.ui.text.SmartDocumentFilter(doc)));
            return t;
        } catch (Throwable e) {
            Log.Debug(GroovyPluginEditor.class, "Plain editor: " + e);
            JTextArea a = new JTextArea();
            a.setTabSize(4);
            return a;
        }
    }

    /**
     * The highlighting of the Groovy console needs further libraries
     * (JavaParser, ASM); if one is missing (NoClassDefFoundError), the
     * highlighting switches itself off instead of breaking the editor
     */
    private static final class SafeFilter extends javax.swing.text.DocumentFilter {

        private javax.swing.text.DocumentFilter filter;

        SafeFilter(javax.swing.text.DocumentFilter filter) {
            this.filter = filter;
        }

        private void off(LinkageError e) {
            Log.Debug(GroovyPluginEditor.class, "Highlighting off: " + e);
            filter = null;
        }

        @Override
        public void insertString(FilterBypass fb, int offset, String text, javax.swing.text.AttributeSet attr) throws javax.swing.text.BadLocationException {
            if (filter != null) {
                try {
                    filter.insertString(fb, offset, text, attr);
                    return;
                } catch (LinkageError e) {
                    off(e);
                }
            }
            fb.insertString(offset, text, attr);
        }

        @Override
        public void remove(FilterBypass fb, int offset, int length) throws javax.swing.text.BadLocationException {
            if (filter != null) {
                try {
                    filter.remove(fb, offset, length);
                    return;
                } catch (LinkageError e) {
                    off(e);
                }
            }
            fb.remove(offset, length);
        }

        @Override
        public void replace(FilterBypass fb, int offset, int length, String text, javax.swing.text.AttributeSet attrs) throws javax.swing.text.BadLocationException {
            if (filter != null) {
                try {
                    filter.replace(fb, offset, length, text, attrs);
                    return;
                } catch (LinkageError e) {
                    off(e);
                }
            }
            fb.replace(offset, length, text, attrs);
        }
    }

    private void keys() {
        bind(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK), "save", this::saveAndRun);
        bind(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0), "check", () -> check(true));
        bind(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK), "undo", () -> {
            if (undo.canUndo()) {
                undo.undo();
            }
        });
        bind(KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_DOWN_MASK), "redo", () -> {
            if (undo.canRedo()) {
                undo.redo();
            }
        });
        bind(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "close", this::close);
        editor.getDocument().addUndoableEditListener(e -> undo.addEdit(e.getEdit()));
        editor.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                changed();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                changed();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
            }
        });
    }

    private void bind(KeyStroke k, String name, Runnable r) {
        JComponent root = getRootPane();
        root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(k, name);
        root.getActionMap().put(name, new AbstractAction() {
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                r.run();
            }
        });
        //the editor itself may use the key (undo of the console editor)
        editor.getInputMap().put(k, name);
        editor.getActionMap().put(name, root.getActionMap().get(name));
    }

    private void changed() {
        setTitle((isDirty() ? "* " : "") + String.format(Messages.GEDIT_TITLE.toString(), file.getName()));
    }

    private boolean isDirty() {
        return !editor.getText().equals(saved);
    }

    private void hint() {
        status.setForeground(ModernTheme.muted());
        status.setText(Messages.GEDIT_HINT.toString());
    }

    private void load() {
        try {
            saved = file.isFile() ? new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8) : "";
        } catch (Exception e) {
            saved = "";
            error(e.getMessage());
        }
        //a new or empty plugin starts with the skeleton (not saved yet)
        boolean empty = saved.trim().isEmpty();
        editor.setText(saved);
        //the console editor normalizes line ends: compare with what it holds
        saved = editor.getText();
        if (empty) {
            String name = file.getName().replaceAll("\\.groovy$", "");
            editor.setText(skeleton(name));
            saved = "";
        }
        editor.setCaretPosition(0);
        undo.discardAllEdits();
        changed();
    }

    private void insert(String snippet) {
        try {
            int pos = editor.getDocument().getLength();
            String text = editor.getText();
            String prefix = text.isEmpty() || text.endsWith("\n\n") ? "" : text.endsWith("\n") ? "\n" : "\n\n";
            editor.getDocument().insertString(pos, prefix + snippet, null);
            editor.setCaretPosition(pos + prefix.length());
            editor.requestFocusInWindow();
            Rectangle r = editor.modelToView2D(pos + prefix.length()).getBounds();
            r.height = editor.getVisibleRect().height;
            editor.scrollRectToVisible(r);
        } catch (BadLocationException e) {
            Log.Debug(this, e);
        }
    }

    /**
     * @param tellOk show "no errors" as well
     * @return true if the script compiles
     */
    private boolean check(boolean tellOk) {
        try {
            new groovy.lang.GroovyShell().parse(editor.getText(), "Plugin");
            if (tellOk) {
                ok(Messages.GEDIT_OK_SYNTAX.toString());
            }
            return true;
        } catch (Throwable e) {
            String m = e.getMessage() == null ? e.toString() : e.getMessage();
            Matcher l = Pattern.compile("@ line (\\d+), column (\\d+)").matcher(m);
            if (l.find()) {
                int line = Integer.parseInt(l.group(1));
                String first = m.replaceAll("(?s)^startup failed:\\s*", "").replaceAll("(?s)\\s*@ line.*", "").replaceAll("^Plugin: \\d+: ", "").trim();
                error(String.format(Messages.GEDIT_ERROR_LINE.toString(), line, first));
                gotoLine(line);
            } else {
                error(m.replaceAll("\\s+", " "));
            }
            return false;
        }
    }

    private void gotoLine(int line) {
        Element root = editor.getDocument().getDefaultRootElement();
        if (line >= 1 && line <= root.getElementCount()) {
            Element e = root.getElement(line - 1);
            editor.setCaretPosition(e.getStartOffset());
            editor.moveCaretPosition(Math.max(e.getStartOffset(), e.getEndOffset() - 1));
            editor.requestFocusInWindow();
        }
    }

    /**
     * Saves; if it compiles the plugin is (re)loaded and started
     */
    private void saveAndRun() {
        if (!save()) {
            return;
        }
        if (!check(false)) {
            return;
        }
        ExtensionManager m = ExtensionManager.instance();
        try {
            ExtensionManager.Installed installed = null;
            for (ExtensionManager.Installed i : m.list()) {
                if (i.jar != null && i.jar.getCanonicalFile().equals(file.getCanonicalFile())) {
                    installed = i;
                }
            }
            if (installed != null) {
                installed = m.reload(installed);
            } else {
                java.util.List<ExtensionManager.Installed> l = m.install(file);
                installed = l.isEmpty() ? null : l.get(0);
            }
            if (installed != null && installed.getError() != null) {
                error(String.format(Messages.GEDIT_SAVED_FAILED.toString(), installed.getError()));
                Matcher l = Pattern.compile("(?:Script\\w*|Plugin|\\.groovy):(\\d+)").matcher(installed.getError());
                if (l.find()) {
                    gotoLine(Integer.parseInt(l.group(1)));
                }
            } else {
                ok(Messages.GEDIT_SAVED_RUNNING.toString());
            }
        } catch (Exception e) {
            Log.Debug(this, e);
            error(String.format(Messages.GEDIT_SAVED_FAILED.toString(), e.getMessage()));
        }
    }

    private boolean save() {
        try {
            file.getParentFile().mkdirs();
            Files.write(file.toPath(), editor.getText().getBytes(StandardCharsets.UTF_8));
            saved = editor.getText();
            changed();
            return true;
        } catch (Exception e) {
            Log.Debug(this, e);
            error(e.getMessage());
            return false;
        }
    }

    private void close() {
        if (isDirty()) {
            int a = javax.swing.JOptionPane.showConfirmDialog(this, Messages.GEDIT_UNSAVED.toString(), getTitle(),
                    javax.swing.JOptionPane.YES_NO_CANCEL_OPTION);
            if (a == javax.swing.JOptionPane.CANCEL_OPTION || a == javax.swing.JOptionPane.CLOSED_OPTION) {
                return;
            }
            if (a == javax.swing.JOptionPane.YES_OPTION) {
                saveAndRun();
            }
        }
        dispose();
    }

    private void external() {
        if (isDirty()) {
            save();
        }
        try {
            if (java.awt.Desktop.getDesktop().isSupported(java.awt.Desktop.Action.EDIT)) {
                java.awt.Desktop.getDesktop().edit(file);
            } else {
                java.awt.Desktop.getDesktop().open(file);
            }
        } catch (Exception e) {
            Log.Debug(this, e);
            error(file.getAbsolutePath());
        }
    }

    private static void browse(String url) {
        try {
            java.awt.Desktop.getDesktop().browse(new java.net.URI(url));
        } catch (Exception e) {
            Popup.notice(url);
        }
    }

    private void ok(String text) {
        status.setForeground(new Color(0x2E7D32));
        status.setText("✓ " + text);
    }

    private void error(String text) {
        status.setForeground(new Color(0xC62828));
        status.setText("✗ " + text);
    }

    /**
     * Line numbers left of the editor
     */
    private static final class LineNumbers extends JComponent {

        private static final long serialVersionUID = 1L;
        private final JTextComponent text;

        LineNumbers(JTextComponent text) {
            this.text = text;
            setOpaque(true);
            text.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    update();
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    update();
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                }
            });
            text.addComponentListener(new java.awt.event.ComponentAdapter() {
                @Override
                public void componentResized(java.awt.event.ComponentEvent e) {
                    update();
                }
            });
        }

        private void update() {
            SwingUtilities.invokeLater(() -> {
                revalidate();
                repaint();
            });
        }

        @Override
        public Dimension getPreferredSize() {
            int lines = text.getDocument().getDefaultRootElement().getElementCount();
            FontMetrics fm = getFontMetrics(text.getFont());
            return new Dimension(fm.stringWidth(String.valueOf(Math.max(99, lines))) + ModernTheme.px(14), text.getPreferredSize().height);
        }

        @Override
        protected void paintComponent(Graphics g) {
            g.setColor(ModernTheme.blend(ModernTheme.background(), ModernTheme.cardBackground(), 0.5f));
            Rectangle clip = g.getClipBounds();
            g.fillRect(clip.x, clip.y, clip.width, clip.height);
            g.setFont(text.getFont());
            g.setColor(ModernTheme.muted());
            FontMetrics fm = g.getFontMetrics();
            Element root = text.getDocument().getDefaultRootElement();
            int start = text.viewToModel2D(new java.awt.Point(0, clip.y));
            int end = text.viewToModel2D(new java.awt.Point(0, clip.y + clip.height));
            for (int i = root.getElementIndex(start); i <= root.getElementIndex(end) && i < root.getElementCount(); i++) {
                try {
                    Rectangle r = text.modelToView2D(root.getElement(i).getStartOffset()).getBounds();
                    String n = String.valueOf(i + 1);
                    g.drawString(n, getWidth() - fm.stringWidth(n) - ModernTheme.px(6), r.y + fm.getAscent());
                } catch (BadLocationException e) {
                    //outside
                }
            }
        }
    }

    // <editor-fold defaultstate="collapsed" desc="Skeleton and examples">
    /**
     * @param name the name of the plugin
     * @return a working plugin to start from, with the possibilities as
     * comments
     */
    public static String skeleton(String name) {
        String n = name.replace("'", "").replace("\\", "");
        return "// @name " + n + "\n"
                + "// @description What this plugin does\n"
                + "// @version 1.0\n"
                + "// @author \n"
                + "//\n"
                + "// A YaBS plugin in Groovy. The variable 'yabs' is the plugin API:\n"
                + "//   yabs.addCommand(title, description, { ... })     command in the menu Plugins and in Search anything\n"
                + "//   yabs.addSearchProvider({ query -> [results] })     own results in Search anything (Shift Shift)\n"
                + "//   yabs.onActivity({ entry -> ... })                  new / changed / deleted records, created files\n"
                + "//   yabs.addSettingsPage(title, description, { component })   page in Settings\n"
                + "//   yabs.openTab(title, component)                     own tab in the main window\n"
                + "//   yabs.addRecordModifier(modifier)                   change records when they are saved\n"
                + "//   yabs.notify(text), yabs.settings(), yabs.dataFolder(), yabs.background({ work }, { result -> })\n"
                + "// \"Insert example\" adds working examples of each. Ctrl+S saves and starts the plugin.\n"
                + "\n"
                + "yabs.addCommand('" + n + ": hello', 'A first command', {\n"
                + "    yabs.notify('Hello from " + n + "!')\n"
                + "})\n"
                + "\n"
                + "// called when the plugin stops (disabled, reloaded, YaBS closes)\n"
                + "onStop = {\n"
                + "}\n";
    }

    /**
     * @return title -> example code
     */
    static Map<String, String> snippets() {
        Map<String, String> m = new LinkedHashMap<>();
        m.put(Messages.GEDIT_SN_COMMAND.toString(),
                "// a command in the menu Plugins and in Search anything\n"
                + "yabs.addCommand('Count my contacts', 'Shows how many contacts there are', {\n"
                + "    def all = mpv5.db.common.DatabaseObject.getObjects(mpv5.db.common.Context.getContact())\n"
                + "    yabs.notify(\"${all.size()} contacts\")\n"
                + "})\n");
        m.put(Messages.GEDIT_SN_SEARCH.toString(),
                "// own results in Search anything (Shift Shift)\n"
                + "yabs.addSearchProvider({ String query ->\n"
                + "    if (!query.toLowerCase().startsWith('wiki ')) {\n"
                + "        return []\n"
                + "    }\n"
                + "    def term = query.substring(5)\n"
                + "    [new mpv5.plugin.api.SearchResult(\"Wikipedia: $term\", 'opens the browser', {\n"
                + "        java.awt.Desktop.desktop.browse(new URI('https://de.wikipedia.org/wiki/' + URLEncoder.encode(term, 'UTF-8')))\n"
                + "    } as Runnable)]\n"
                + "})\n");
        m.put(Messages.GEDIT_SN_ACTIVITY.toString(),
                "// told about new, changed and deleted records and created files\n"
                + "yabs.onActivity({ entry ->\n"
                + "    if (entry.kind == mpv5.handler.Activity.Kind.CREATED && entry.context == mpv5.db.common.Context.getInvoice()) {\n"
                + "        yabs.notify(\"New invoice: ${entry.text}\")\n"
                + "    }\n"
                + "    if (entry.kind == mpv5.handler.Activity.Kind.FILE) {\n"
                + "        // entry.file: the created PDF or ODT\n"
                + "    }\n"
                + "})\n");
        m.put(Messages.GEDIT_SN_PAGE.toString(),
                "// a page in Settings, with a value stored for the plugin\n"
                + "yabs.addSettingsPage('My plugin', 'Settings of my plugin', {\n"
                + "    def field = new javax.swing.JTextField(yabs.settings().get('greeting', 'Hello'), 20)\n"
                + "    def save = new javax.swing.JButton('Save')\n"
                + "    save.addActionListener({ yabs.settings().set('greeting', field.text); yabs.notify('Saved') } as java.awt.event.ActionListener)\n"
                + "    def p = new javax.swing.JPanel()\n"
                + "    p.add(new javax.swing.JLabel('Greeting:'))\n"
                + "    p.add(field)\n"
                + "    p.add(save)\n"
                + "    p\n"
                + "})\n");
        m.put(Messages.GEDIT_SN_TAB.toString(),
                "// an own tab in the main window\n"
                + "yabs.addCommand('Open my tab', 'Shows a tab of the plugin', {\n"
                + "    def area = new javax.swing.JTextArea('Text of my plugin', 10, 40)\n"
                + "    yabs.openTab('My tab', new javax.swing.JScrollPane(area))\n"
                + "})\n");
        m.put(Messages.GEDIT_SN_MODIFIER.toString(),
                "// change records when they are saved (here: contact names in capitals)\n"
                + "yabs.addRecordModifier([\n"
                + "    modifyOnSave      : { o ->\n"
                + "        if (o instanceof mpv5.db.objects.Contact && o.cname) {\n"
                + "            o.cname = o.cname.toUpperCase()\n"
                + "        }\n"
                + "        o\n"
                + "    },\n"
                + "    executeAfterSave  : { o -> o },\n"
                + "    executeAfterCreate: { o -> o },\n"
                + "    modifyOnDelete    : { o -> o },\n"
                + "    modifyOnExplode   : { o -> o },\n"
                + "    modifyOnResolve   : { map -> map }\n"
                + "] as mpv5.db.common.DatabaseObjectModifier)\n");
        m.put(Messages.GEDIT_SN_SETTINGS.toString(),
                "// store values of the plugin (kept across starts)\n"
                + "def runs = (yabs.settings().get('runs', '0') as int) + 1\n"
                + "yabs.settings().set('runs', String.valueOf(runs))\n"
                + "// own files: yabs.dataFolder()\n"
                + "new File(yabs.dataFolder(), 'log.txt') << \"start $runs: ${new Date()}\\n\"\n");
        m.put(Messages.GEDIT_SN_BACKGROUND.toString(),
                "// slow work in the background, the result on the Swing thread\n"
                + "yabs.addCommand('Fetch something', 'Loads a web page in the background', {\n"
                + "    yabs.background({ new URL('https://example.org').text.length() }, { len ->\n"
                + "        yabs.notify(\"Loaded $len characters\")\n"
                + "    })\n"
                + "})\n");
        return m;
    }
    // </editor-fold>
}
