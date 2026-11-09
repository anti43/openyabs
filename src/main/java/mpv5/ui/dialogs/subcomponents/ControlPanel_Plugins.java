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
package mpv5.ui.dialogs.subcomponents;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.datatransfer.DataFlavor;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.swing.filechooser.FileNameExtensionFilter;
import mpv5.data.PropertyStore;
import mpv5.globals.Messages;
import mpv5.logging.Log;
import mpv5.plugin.ExtensionManager;
import mpv5.plugin.ExtensionManager.Installed;
import mpv5.plugin.ExtensionManager.State;
import mpv5.ui.dialogs.ControlApplet;
import mpv5.ui.dialogs.Popup;
import mpv5.ui.misc.RichList;
import mpv5.ui.modern.Applets;
import mpv5.ui.modern.Card;
import mpv5.ui.modern.ModernTheme;
import mpv5.ui.modern.PrimaryButton;

/**
 * The plugin manager: the plugins of YaBS and of the plugins folder (jars and
 * Groovy scripts) with their state; enable, disable, reload, remove, install
 * (also by drag and drop) and a new Groovy plugin from a template. The classic
 * plugins of the database are below.
 */
public final class ControlPanel_Plugins extends JPanel implements ControlApplet {

    private static final long serialVersionUID = 1L;
    public final String UNAME = "plugins";
    private final JPanel list = new JPanel();
    private final JPanel actions;
    private final ExtensionManager manager = ExtensionManager.instance();
    private final Runnable refresh = () -> SwingUtilities.invokeLater(this::refresh);

    public ControlPanel_Plugins() {
        super(new BorderLayout());
        setOpaque(false);
        manager.discover();

        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        list.setOpaque(false);
        JLabel folder = new JLabel(String.format(Messages.PLUGINS_FOLDER.toString(), manager.folder().getAbsolutePath()));
        folder.setForeground(ModernTheme.muted());
        Card plugins = new Card(Messages.PLUGINS_TITLE.toString())
                .addFull(Applets.hint(Messages.PLUGINS_HINT.toString()))
                .addFull(folder)
                .addFull(list);
        Card classic;
        ControlPanel_ClassicPlugins old = null;
        try {
            old = new ControlPanel_ClassicPlugins();
        } catch (Throwable t) {
            Log.Debug(this, t);
        }
        JPanel page = Applets.page(null, 520, plugins);
        if (old != null) {
            JPanel oldPage = new JPanel(new BorderLayout());
            oldPage.setOpaque(false);
            oldPage.add(old, BorderLayout.CENTER);
            Component oldActions = old.getAndRemoveActionPanel();
            if (oldActions != null) {
                oldPage.add(oldActions, BorderLayout.SOUTH);
            }
            classic = new Card(Messages.PLUGINS_CLASSIC.toString())
                    .addFull(Applets.hint(Messages.PLUGINS_CLASSIC_HINT.toString()))
                    .addFull(oldPage);
            JPanel both = new JPanel(new BorderLayout(0, ModernTheme.px(12)));
            both.setOpaque(false);
            both.add(page, BorderLayout.NORTH);
            JPanel c = new JPanel(new BorderLayout());
            c.setOpaque(false);
            c.setBorder(ModernTheme.padding(0, 14));
            c.add(classic, BorderLayout.NORTH);
            both.add(c, BorderLayout.CENTER);
            add(both, BorderLayout.CENTER);
        } else {
            add(page, BorderLayout.CENTER);
        }

        //drop a jar or a .groovy file to install it
        setTransferHandler(new TransferHandler() {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean canImport(TransferSupport s) {
                return s.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
            }

            @Override
            @SuppressWarnings("unchecked")
            public boolean importData(TransferSupport s) {
                try {
                    for (File f : (List<File>) s.getTransferable().getTransferData(DataFlavor.javaFileListFlavor)) {
                        install(f);
                    }
                    return true;
                } catch (Exception e) {
                    Log.Debug(this, e);
                    return false;
                }
            }
        });

        JButton openFolder = new JButton(Messages.PLUGINS_OPEN_FOLDER.toString());
        openFolder.addActionListener(e -> open(manager.folder()));
        JButton newGroovy = new JButton(Messages.PLUGINS_NEW_GROOVY.toString());
        newGroovy.addActionListener(e -> newGroovy());
        PrimaryButton install = new PrimaryButton(Messages.PLUGINS_INSTALL.toString(), null);
        install.addActionListener(e -> chooseAndInstall());
        actions = Applets.actionBar(new JComponent[]{openFolder}, new JComponent[]{newGroovy, install});

        refresh();
    }

    @Override
    public void addNotify() {
        super.addNotify();
        manager.addChangeListener(refresh);
        refresh();
    }

    private void refresh() {
        list.removeAll();
        List<Installed> all = manager.list();
        if (all.isEmpty()) {
            JLabel none = new JLabel(Messages.PLUGINS_NONE.toString());
            none.setForeground(ModernTheme.muted());
            list.add(none);
        }
        for (Installed i : all) {
            list.add(row(i));
            list.add(Box.createVerticalStrut(ModernTheme.px(8)));
        }
        list.revalidate();
        list.repaint();
    }

    private JComponent row(final Installed i) {
        JPanel r = new JPanel(new BorderLayout(ModernTheme.px(12), 0));
        r.setOpaque(true);
        r.setBackground(ModernTheme.cardBackground());
        r.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(ModernTheme.border()), ModernTheme.padding(10, 12)));
        r.setAlignmentX(LEFT_ALIGNMENT);
        boolean groovy = i.jar != null && i.jar.getName().endsWith(".groovy");
        String kind = i.isBuiltIn() ? "Y" : groovy ? "G" : "J";
        Color kc = i.isBuiltIn() ? ModernTheme.accent() : groovy ? RichList.GREEN : RichList.PURPLE;
        r.add(new JLabel(new RichList.Badge(kind, kc, ModernTheme.px(32))), BorderLayout.WEST);

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        JLabel title = new JLabel(safe(() -> i.extension.name()) + "   " + safe(() -> i.extension.version()));
        title.setFont(ModernTheme.font(1, Font.BOLD));
        text.add(title);
        String desc = safe(() -> i.extension.description());
        if (!desc.isEmpty()) {
            text.add(Applets.hint(desc));
        }
        String author = safe(() -> i.extension.author());
        String source = i.isBuiltIn() ? Messages.PLUGINS_BUILTIN.toString() : i.jar.getName();
        JLabel meta = new JLabel((author.isEmpty() ? "" : author + "  ·  ") + source + "  ·  " + i.id()
                + (i.getState() == State.RUNNING ? "  ·  " + String.format(Messages.PLUGINS_CONTRIBUTIONS.toString(), manager.contributions(i.id())) : ""));
        meta.setFont(ModernTheme.font(-1, Font.PLAIN));
        meta.setForeground(ModernTheme.muted());
        text.add(meta);
        if (i.getState() == State.FAILED && i.getError() != null) {
            JLabel err = new JLabel(i.getError());
            err.setForeground(RichList.RED);
            text.add(err);
        }
        r.add(text, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, ModernTheme.px(6), 0));
        buttons.setOpaque(false);
        JLabel state = new JLabel(stateText(i.getState()));
        state.setForeground(i.getState() == State.RUNNING ? RichList.GREEN : i.getState() == State.FAILED ? RichList.RED : ModernTheme.muted());
        state.setFont(ModernTheme.font(-1, Font.BOLD));
        buttons.add(state);
        JToggleButton on = new JToggleButton(i.getState() == State.DISABLED ? Messages.PLUGINS_ENABLE.toString() : Messages.PLUGINS_DISABLE.toString());
        on.addActionListener(e -> manager.setEnabled(i, i.getState() == State.DISABLED));
        buttons.add(on);
        if (i.getState() != State.DISABLED) {
            JButton reload = new JButton(Messages.PLUGINS_RELOAD.toString());
            reload.addActionListener(e -> manager.reload(i));
            buttons.add(reload);
        }
        if (groovy) {
            JButton edit = new JButton(Messages.PLUGINS_EDIT.toString());
            edit.addActionListener(e -> edit(i.jar));
            buttons.add(edit);
        }
        if (!i.isBuiltIn()) {
            JButton remove = Applets.danger(new JButton(Messages.PLUGINS_REMOVE.toString()));
            remove.addActionListener(e -> {
                if (Popup.Y_N_dialog(String.format(Messages.PLUGINS_REALLY_REMOVE.toString(), i.jar.getName()))) {
                    manager.uninstall(i);
                }
            });
            buttons.add(remove);
        }
        r.add(buttons, BorderLayout.EAST);
        r.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, r.getPreferredSize().height));
        return r;
    }

    private static String stateText(State s) {
        switch (s) {
            case RUNNING:
                return Messages.PLUGINS_RUNNING.toString();
            case FAILED:
                return Messages.PLUGINS_STATE_FAILED.toString();
            case DISABLED:
                return Messages.PLUGINS_DISABLED.toString();
            default:
                return Messages.PLUGINS_STOPPED.toString();
        }
    }

    private static String safe(java.util.function.Supplier<String> s) {
        try {
            String v = s.get();
            return v == null ? "" : v;
        } catch (Throwable t) {
            return "";
        }
    }

    private void chooseAndInstall() {
        JFileChooser c = new JFileChooser();
        c.setFileFilter(new FileNameExtensionFilter(Messages.PLUGINS_FILES.toString(), "jar", "groovy"));
        if (c.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            install(c.getSelectedFile());
        }
    }

    private void install(File f) {
        try {
            List<Installed> l = manager.install(f);
            mpv5.YabsViewProxy.instance().addMessage(String.format(Messages.PLUGINS_INSTALLED.toString(), l.size(), f.getName()));
        } catch (Exception e) {
            Log.Debug(this, e);
            Popup.notice(Messages.NOT_POSSIBLE + ": " + e.getMessage());
        }
    }

    private void newGroovy() {
        String name = Popup.Enter_Value(Messages.PLUGINS_NEW_GROOVY_NAME.toString());
        if (name == null || name.trim().isEmpty()) {
            return;
        }
        String base = name.trim().replaceAll("[^A-Za-z0-9_-]", "_");
        File f = new File(manager.folder(), base + ".groovy");
        if (f.exists()) {
            Popup.notice(Messages.NOT_POSSIBLE + ": " + f.getName());
            return;
        }
        try {
            manager.folder().mkdirs();
            Files.write(f.toPath(), template(name.trim()).getBytes(StandardCharsets.UTF_8));
            install(f);
            edit(f);
        } catch (Exception e) {
            Log.Debug(this, e);
            Popup.error(e);
        }
    }

    /**
     * @param name
     * @return a working Groovy plugin to start from
     */
    static String template(String name) {
        return mpv5.plugin.GroovyPluginEditor.skeleton(name);
    }

    private void edit(File f) {
        //the editor inside YaBS (it can also open another editor)
        mpv5.plugin.GroovyPluginEditor.open(this, f);
    }

    private void open(File dir) {
        try {
            dir.mkdirs();
            Desktop.getDesktop().open(dir);
        } catch (Exception e) {
            Log.Debug(this, e);
            Popup.notice(dir.getAbsolutePath());
        }
    }

    @Override
    public Component getAndRemoveActionPanel() {
        return actions;
    }

    @Override
    public void setValues(PropertyStore values) {
    }

    @Override
    public String getUname() {
        return UNAME;
    }

    @Override
    public void reset() {
        refresh();
    }

    @Override
    public void removeNotify() {
        manager.removeChangeListener(refresh);
        super.removeNotify();
    }
}
