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
import java.awt.GridLayout;
import java.awt.Font;
import java.awt.datatransfer.DataFlavor;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import mpv5.handler.PluginUpdateCheck;
import mpv5.handler.PluginUpdateCheck.Available;
import mpv5.handler.PluginUpdateCheck.Catalog;
import mpv5.handler.UpdateCheck;
import mpv5.logging.Log;
import mpv5.plugin.ExtensionManager;
import mpv5.plugin.ExtensionManager.Installed;
import mpv5.plugin.ExtensionManager.State;
import mpv5.ui.dialogs.ControlApplet;
import mpv5.ui.dialogs.PluginUpdateNotice;
import mpv5.ui.dialogs.Popup;
import mpv5.ui.misc.RichList;
import mpv5.plugin.builtin.UpdateManager;
import mpv5.ui.modern.Applets;
import mpv5.ui.modern.Card;
import mpv5.ui.modern.ModernTheme;

/**
 * The plugin manager: the plugins of YaBS and of the plugins folder (jars and
 * Groovy scripts) with their state; enable, disable, reload, remove, install
 * (also by drag and drop or from the latest release catalog) and a new Groovy
 * plugin from a template. The classic plugins of the database are below.
 */
public final class ControlPanel_Plugins extends JPanel implements ControlApplet {

    private static final long serialVersionUID = 1L;
    public final String UNAME = "plugins";
    private final JPanel list = new JPanel();
    private final JPanel availableList = new JPanel();
    private final JLabel availableSource = new JLabel();
    private final JButton refreshCatalog = new JButton(Messages.PLUGINS_AVAILABLE_LOAD.toString());
    private final Card availableCard;
    private final Component availableStrut;
    private final JButton updatesButton;
    private final JPanel actions;
    private final ExtensionManager manager = ExtensionManager.instance();
    private final Runnable refresh = () -> SwingUtilities.invokeLater(this::refresh);
    private boolean wasUpdatePluginActive;

    public ControlPanel_Plugins() {
        super(new BorderLayout());
        setOpaque(false);
        manager.discover();

        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        list.setOpaque(false);
        availableList.setLayout(new BoxLayout(availableList, BoxLayout.Y_AXIS));
        availableList.setOpaque(false);

        //the three ways to add a plugin, side by side and of the same weight
        JPanel add = new JPanel(new GridLayout(1, 3, ModernTheme.px(10), 0));
        add.setOpaque(false);
        add.add(tile(Messages.PLUGINS_NEW_GROOVY.toString(), Messages.PLUGINS_NEW_GROOVY_HINT.toString(), RichList.GREEN, "G", this::newGroovy));
        add.add(tile(Messages.PLUGINS_IMPORT_GROOVY.toString(), Messages.PLUGINS_IMPORT_GROOVY_HINT.toString(), RichList.GREEN, "G",
                () -> chooseAndInstall(Messages.PLUGINS_GROOVY_FILES.toString(), "groovy")));
        add.add(tile(Messages.PLUGINS_IMPORT_JAR.toString(), Messages.PLUGINS_IMPORT_JAR_HINT.toString(), RichList.PURPLE, "J",
                () -> chooseAndInstall(Messages.PLUGINS_JAR_FILES.toString(), "jar")));
        Card adding = new Card(Messages.PLUGINS_ADD.toString())
                .addFull(Applets.hint(Messages.PLUGINS_HINT.toString()))
                .addFull(add);

        JLabel folder = new JLabel(String.format(Messages.PLUGINS_FOLDER.toString(), manager.folder().getAbsolutePath()));
        folder.setForeground(ModernTheme.muted());
        Card plugins = new Card(Messages.PLUGINS_TITLE.toString())
                .addFull(list)
                .addFull(folder);

        availableSource.setForeground(ModernTheme.muted());
        availableSource.setVisible(false);
        refreshCatalog.setToolTipText(Messages.PLUGINS_AVAILABLE_HINT.toString());
        refreshCatalog.addActionListener(e -> PluginUpdateNotice.loadCatalog(this, true));
        availableCard = new Card(Messages.PLUGINS_AVAILABLE.toString())
                .addFull(availableList)
                .addFull(availableSource);
        availableCard.getHeader().add(refreshCatalog, BorderLayout.EAST);

        JPanel cards = new JPanel();
        cards.setLayout(new BoxLayout(cards, BoxLayout.Y_AXIS));
        cards.setOpaque(false);
        cards.setBorder(ModernTheme.padding(12, 14));
        cards.add(adding);
        cards.add(Box.createVerticalStrut(ModernTheme.px(12)));
        cards.add(plugins);
        availableStrut = Box.createVerticalStrut(ModernTheme.px(12));
        cards.add(availableStrut);
        cards.add(availableCard);

        //the classic plugins of the database: rarely needed, so folded
        ControlPanel_ClassicPlugins old = null;
        try {
            old = new ControlPanel_ClassicPlugins();
        } catch (Throwable t) {
            Log.Debug(this, t);
        }
        if (old != null) {
            final JPanel oldPage = new JPanel(new BorderLayout());
            oldPage.setOpaque(false);
            oldPage.add(old, BorderLayout.CENTER);
            Component oldActions = old.getAndRemoveActionPanel();
            if (oldActions != null) {
                oldPage.add(oldActions, BorderLayout.SOUTH);
            }
            oldPage.setVisible(false);
            Card classic = new Card(Messages.PLUGINS_CLASSIC.toString())
                    .addFull(Applets.hint(Messages.PLUGINS_CLASSIC_HINT.toString()))
                    .addFull(oldPage);
            final JButton toggle = new JButton(Messages.PLUGINS_CLASSIC_SHOW.toString());
            toggle.addActionListener(e -> {
                oldPage.setVisible(!oldPage.isVisible());
                toggle.setText(oldPage.isVisible() ? Messages.PLUGINS_CLASSIC_HIDE.toString() : Messages.PLUGINS_CLASSIC_SHOW.toString());
                revalidate();
                repaint();
            });
            classic.getHeader().add(toggle, BorderLayout.EAST);
            cards.add(Box.createVerticalStrut(ModernTheme.px(12)));
            cards.add(classic);
        }
        for (Component c : cards.getComponents()) {
            ((JComponent) c).setAlignmentX(LEFT_ALIGNMENT);
        }
        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(ModernTheme.background());
        top.add(cards, BorderLayout.NORTH);
        add(top, BorderLayout.CENTER);

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
        updatesButton = new JButton(Messages.PLUGIN_UPDATE_CHECK_NOW.toString());
        updatesButton.setToolTipText(Messages.PLUGIN_UPDATE_HINT.toString());
        updatesButton.addActionListener(e -> PluginUpdateNotice.checkNow(this));
        actions = Applets.actionBar(new JComponent[]{openFolder}, new JComponent[]{updatesButton});

        refresh();
    }

    @Override
    public void addNotify() {
        super.addNotify();
        manager.addChangeListener(refresh);
        PluginUpdateNotice.addListener(refresh);
        maybeAutoLoadCatalog();
        refresh();
    }

    private void maybeAutoLoadCatalog() {
        if (UpdateManager.isActive()
                && PluginUpdateCheck.lastCatalog() == null
                && UpdateCheck.isSwitchedOn()
                && !PluginUpdateNotice.isCatalogLoading()
                && !PluginUpdateNotice.isCatalogFailed()) {
            PluginUpdateNotice.loadCatalog(this, false);
        }
    }

    private void refresh() {
        list.removeAll();
        List<Installed> all = manager.list();
        Map<String, Installed> installedById = new HashMap<String, Installed>();
        if (all.isEmpty()) {
            JLabel none = new JLabel(Messages.PLUGINS_NONE.toString());
            none.setForeground(ModernTheme.muted());
            list.add(none);
        }
        for (Installed i : all) {
            installedById.put(i.id(), i);
            list.add(row(i));
            list.add(Box.createVerticalStrut(ModernTheme.px(8)));
        }
        list.revalidate();
        list.repaint();
        refreshAvailable(installedById);
    }

    private void refreshAvailable(Map<String, Installed> installedById) {
        boolean active = UpdateManager.isActive();
        availableCard.setVisible(active);
        availableStrut.setVisible(active);
        updatesButton.setVisible(active);
        if (!active) {
            wasUpdatePluginActive = false;
            availableList.removeAll();
            availableSource.setVisible(false);
            revalidate();
            repaint();
            return;
        }
        if (!wasUpdatePluginActive && isDisplayable()) {
            maybeAutoLoadCatalog();
        }
        wasUpdatePluginActive = true;

        availableList.removeAll();
        Catalog catalog = PluginUpdateCheck.lastCatalog();
        boolean loading = PluginUpdateNotice.isCatalogLoading();
        refreshCatalog.setEnabled(!loading);
        refreshCatalog.setText(loading ? Messages.PLUGINS_AVAILABLE_LOADING.toString()
                : catalog == null ? Messages.PLUGINS_AVAILABLE_LOAD.toString()
                : Messages.PLUGINS_AVAILABLE_REFRESH.toString());

        if (loading && catalog == null) {
            JLabel l = new JLabel(Messages.PLUGINS_AVAILABLE_LOADING.toString());
            l.setForeground(ModernTheme.muted());
            availableList.add(l);
            availableSource.setVisible(false);
        } else if (catalog == null) {
            if (PluginUpdateNotice.isCatalogFailed()) {
                JLabel err = new JLabel(Messages.UPDATE_CHECK_FAILED.toString());
                err.setForeground(RichList.RED);
                availableList.add(err);
            } else {
                availableList.add(Applets.hint(Messages.PLUGINS_AVAILABLE_HINT.toString()));
            }
            availableSource.setVisible(false);
        } else {
            if (catalog.plugins.isEmpty()) {
                JLabel none = new JLabel(Messages.PLUGINS_AVAILABLE_NONE.toString());
                none.setForeground(ModernTheme.muted());
                availableList.add(none);
            } else {
                for (Available a : catalog.plugins) {
                    availableList.add(availableRow(a, installedById.get(a.id)));
                    availableList.add(Box.createVerticalStrut(ModernTheme.px(8)));
                }
            }
            availableSource.setText(String.format(Messages.PLUGINS_AVAILABLE_SOURCE.toString(), catalog.yabsVersion));
            availableSource.setVisible(true);
        }
        availableList.revalidate();
        availableList.repaint();
        revalidate();
        repaint();
    }

    private JComponent availableRow(final Available a, final Installed installed) {
        JPanel r = new JPanel(new BorderLayout(ModernTheme.px(12), 0));
        r.setOpaque(true);
        r.setBackground(ModernTheme.cardBackground());
        r.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(ModernTheme.border()), ModernTheme.padding(10, 12)));
        r.setAlignmentX(LEFT_ALIGNMENT);
        r.add(new JLabel(new RichList.Badge("J", RichList.PURPLE, ModernTheme.px(32))), BorderLayout.WEST);

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        JLabel title = new JLabel(a.name + "   " + a.version);
        title.setFont(ModernTheme.font(1, Font.BOLD));
        text.add(title);
        if (!a.description.isEmpty()) {
            text.add(Applets.hint(a.description));
        }
        JLabel meta = new JLabel((a.author.isEmpty() ? "" : a.author + "  ·  ") + a.jar + "  ·  " + a.id);
        meta.setFont(ModernTheme.font(-1, Font.PLAIN));
        meta.setForeground(ModernTheme.muted());
        text.add(meta);
        if (!a.isCompatible() && a.minYabsVersion != null) {
            JLabel req = new JLabel(String.format(Messages.PLUGIN_UPDATE_NEEDS_YABS.toString(), a.minYabsVersion));
            req.setFont(ModernTheme.font(-1, Font.PLAIN));
            req.setForeground(ModernTheme.muted());
            text.add(req);
        }
        for (Component c : text.getComponents()) {
            ((JComponent) c).setAlignmentX(LEFT_ALIGNMENT);
        }
        text.setMinimumSize(new java.awt.Dimension(ModernTheme.px(120), 0));
        r.add(text, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, ModernTheme.px(6), 0));
        buttons.setOpaque(false);
        if (PluginUpdateNotice.isInstalling(a.id)) {
            JButton busy = new JButton(Messages.PLUGINS_INSTALLING.toString());
            busy.setEnabled(false);
            buttons.add(busy);
        } else if (installed != null) {
            JLabel state = new JLabel(Messages.PLUGINS_ALREADY_INSTALLED.toString());
            state.setForeground(RichList.GREEN);
            state.setFont(ModernTheme.font(-1, Font.BOLD));
            buttons.add(state);
            String installedVersion = safe(() -> installed.extension.version());
            if (!installed.isBuiltIn() && installed.jar != null
                    && UpdateCheck.isNewer(a.version, installedVersion) && a.installable()) {
                JButton update = new JButton(Messages.PLUGIN_UPDATE_BUTTON.toString());
                update.setToolTipText(String.format(Messages.PLUGIN_UPDATE_LINE.toString(), a.name, a.version, installedVersion));
                update.addActionListener(e -> {
                    update.setEnabled(false);
                    PluginUpdateCheck.Update u = new PluginUpdateCheck.Update(
                            a.id, a.name, installedVersion, a.version, a.page,
                            a.download, a.jar, a.sums, a.assetName, a.minYabsVersion, installed.jar);
                    PluginUpdateNotice.install(this, Collections.singletonList(u));
                });
                buttons.add(update);
            }
        } else if (a.installable()) {
            JButton install = new JButton(Messages.PLUGINS_INSTALL.toString());
            install.addActionListener(e -> {
                install.setEnabled(false);
                PluginUpdateNotice.installAvailable(this, a);
            });
            buttons.add(install);
        } else if (a.download == null) {
            JLabel noDl = new JLabel(Messages.PLUGIN_UPDATE_NO_DOWNLOAD.toString());
            noDl.setForeground(ModernTheme.muted());
            noDl.setFont(ModernTheme.font(-1, Font.PLAIN));
            buttons.add(noDl);
        } else {
            JButton install = new JButton(Messages.PLUGINS_INSTALL.toString());
            install.setEnabled(false);
            if (a.minYabsVersion != null) {
                install.setToolTipText(String.format(Messages.PLUGIN_UPDATE_NEEDS_YABS.toString(), a.minYabsVersion));
            }
            buttons.add(install);
        }
        r.add(buttons, BorderLayout.EAST);
        r.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, r.getPreferredSize().height));
        return r;
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
        final PluginUpdateCheck.Update update = PluginUpdateCheck.lastFor(i.id());
        if (update != null) {
            JLabel up = new JLabel(String.format(Messages.PLUGIN_UPDATE_AVAILABLE.toString(), update.version));
            up.setForeground(ModernTheme.accent());
            up.setFont(ModernTheme.font(-1, Font.BOLD));
            text.add(up);
        }
        if (i.getState() == State.FAILED && i.getError() != null) {
            JLabel err = new JLabel(i.getError());
            err.setForeground(RichList.RED);
            text.add(err);
        }
        for (Component c : text.getComponents()) {
            ((JComponent) c).setAlignmentX(LEFT_ALIGNMENT);
        }
        //long descriptions are cut, not making the page wider than the window
        text.setMinimumSize(new java.awt.Dimension(ModernTheme.px(120), 0));
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
        if (update != null && update.installable()) {
            boolean busy = PluginUpdateNotice.isInstalling(i.id());
            JButton install = new JButton(busy ? Messages.PLUGINS_INSTALLING.toString() : Messages.PLUGIN_UPDATE_BUTTON.toString());
            install.setEnabled(!busy);
            install.setToolTipText(String.format(Messages.PLUGIN_UPDATE_LINE.toString(), update.name, update.version, update.installedVersion));
            install.addActionListener(e -> {
                install.setEnabled(false);
                PluginUpdateNotice.install(this, Collections.singletonList(update));
            });
            buttons.add(install);
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

    /**
     * A button with a title, a line of explanation and a badge; the three of
     * them (new Groovy plugin, import Groovy, import jar) have the same size
     */
    private static JButton tile(String title, String hint, Color color, String badge, Runnable action) {
        JButton b = new JButton();
        b.setLayout(new BorderLayout(ModernTheme.px(10), 0));
        b.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(ModernTheme.border()), ModernTheme.padding(10, 12)));
        b.setHorizontalAlignment(JButton.LEFT);
        b.add(new JLabel(new RichList.Badge(badge, color, ModernTheme.px(28))), BorderLayout.WEST);
        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        JLabel t = new JLabel(title.replaceAll("\\.\\.\\.$", ""));
        t.setFont(ModernTheme.font(1, Font.BOLD));
        text.add(t);
        //a fixed width, so the hint wraps and the tiles stay of the same size
        JLabel h = new JLabel("<html><body style='width:" + ModernTheme.px(170) + "px'>" + hint.replace("&", "&amp;").replace("<", "&lt;") + "</html>");
        h.setForeground(ModernTheme.muted());
        h.setFont(ModernTheme.font(-1, Font.PLAIN));
        text.add(h);
        b.add(text, BorderLayout.CENTER);
        b.setToolTipText(title);
        b.getAccessibleContext().setAccessibleName(title);
        b.addActionListener(e -> action.run());
        return b;
    }

    private void chooseAndInstall(String description, String extension) {
        JFileChooser c = new JFileChooser();
        c.setFileFilter(new FileNameExtensionFilter(description, extension));
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
        mpv5.ui.dialogs.PluginUpdateNotice.removeListener(refresh);
        super.removeNotify();
    }
}
