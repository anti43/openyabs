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
package mpv5.ui.toolbars;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import mpv5.db.common.DatabaseObject;
import mpv5.db.objects.User;
import mpv5.globals.Messages;
import mpv5.i18n.LanguageManager;
import mpv5.ui.modern.ModernTheme;
import mpv5.ui.modern.PrimaryButton;
import mpv5.ui.modern.WrapLayout;
import mpv5.ui.panels.DataPanel;
import mpv5.ui.panels.ExportablePanel;

/**
 * The one command bar of a modern view: every action on the whole record
 * (save, revert, mail, print, PDF, favourite, delete ...) plus the actions a
 * view adds ({@link #addAction}), with keyboard shortcuts. Same logic as the
 * classic toolbar ({@link RecordActions}).
 * <p>
 * Shortcuts work while the focus is anywhere in the view. Text fields and
 * tables keep their own Ctrl+C / Ctrl+V (copy and paste of text).
 */
public class ModernCommandBar extends JPanel {

    private static final long serialVersionUID = 1L;
    /**
     * Shortcuts of the record actions
     */
    public static final KeyStroke SAVE = KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK);
    public static final KeyStroke SAVE_AS_NEW = KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK);
    public static final KeyStroke REVERT = KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_DOWN_MASK);
    public static final KeyStroke PRINT = KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.CTRL_DOWN_MASK);
    public static final KeyStroke PDF = KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK);
    public static final KeyStroke MAIL = KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.CTRL_DOWN_MASK);
    public static final KeyStroke FIND = KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_DOWN_MASK);
    public static final KeyStroke SEARCH_PANEL = KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK);
    public static final KeyStroke COPY = KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK);
    public static final KeyStroke PASTE = KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_DOWN_MASK);
    public static final KeyStroke FAVOURITE = KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.CTRL_DOWN_MASK);
    public static final KeyStroke DELETE = KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, InputEvent.CTRL_DOWN_MASK);
    /**
     * Shortcut of the help with all shortcuts
     */
    public static final KeyStroke HELP = KeyStroke.getKeyStroke(KeyEvent.VK_F1, InputEvent.CTRL_DOWN_MASK);
    /**
     * Shortcut of the online help (the wiki of the repository)
     */
    public static final KeyStroke WIKI = KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0);
    /**
     * Shortcut of the history of a contact (Ctrl+H is backspace in text
     * fields)
     */
    public static final KeyStroke HISTORY = KeyStroke.getKeyStroke(KeyEvent.VK_H, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK);

    /**
     * The shortcuts of the modern views with their descriptions, for the
     * help ({@link mpv5.ui.dialogs.ShortcutsDialog})
     *
     * @return {KeyStroke, description} pairs
     */
    public static java.util.List<Object[]> shortcuts() {
        java.util.List<Object[]> l = new java.util.ArrayList<Object[]>();
        l.add(new Object[]{SAVE, text("MPV5View.jButton25.toolTipText")});
        l.add(new Object[]{SAVE_AS_NEW, Messages.ACTION_SAVE_AS_NEW.toString()});
        l.add(new Object[]{REVERT, Messages.SHORTCUT_REVERT.toString()});
        l.add(new Object[]{PRINT, text("MPV5View.jButton23.toolTipText")});
        l.add(new Object[]{PDF, text("DataPanelTB.pdfButton.toolTipText")});
        l.add(new Object[]{MAIL, text("DataPanelTB.mailButton.toolTipText")});
        l.add(new Object[]{FIND, Messages.SHORTCUT_FIND_LIST.toString()});
        l.add(new Object[]{SEARCH_PANEL, Messages.SEARCH_ANYTHING.toString()});
        l.add(new Object[]{COPY, Messages.SHORTCUT_COPY_RECORD.toString()});
        l.add(new Object[]{PASTE, Messages.SHORTCUT_PASTE_RECORD.toString()});
        l.add(new Object[]{FAVOURITE, text("MPV5View.jButton24.toolTipText")});
        l.add(new Object[]{DELETE, Messages.SHORTCUT_DELETE_RECORD.toString()});
        l.add(new Object[]{HISTORY, Messages.SHORTCUT_HISTORY.toString()});
        l.add(new Object[]{HELP, Messages.KEYBOARD_SHORTCUTS.toString()});
        l.add(new Object[]{WIKI, text("MPView.jMenuItem27.text")});
        return l;
    }
    private final DataPanel view;
    private final JComponent viewComponent;
    private final mpv5.ui.modern.OverflowBar left = new mpv5.ui.modern.OverflowBar(false, false);
    private boolean hasExtras;
    private final JButton search;
    private final PrimaryButton save;
    private final JMenuItem saveAsNewItem;
    private final JButton favourite;
    private final JButton delete;
    private final JPopupMenu more = new JPopupMenu();
    private final java.util.List<javax.swing.JComponent> permanentMoreItems = new java.util.ArrayList<javax.swing.JComponent>();
    private final JMenuItem lock;
    private final JMenuItem odt;
    private final JButton mail;
    private final JButton pdf;
    private boolean isFavourite;
    private boolean editable = true;
    private Runnable findAction;
    private Runnable saveAsNewAction;
    private final JPanel copyBanner = new JPanel(new BorderLayout(ModernTheme.px(8), 0));
    private final javax.swing.JLabel copyText = new javax.swing.JLabel();
    private final JPanel pluginBanner = new JPanel(new BorderLayout());

    /**
     * Saves as a new record or opens a pending copy depending on the view.
     */
    private void triggerSaveAsNew() {
        if (saveAsNewAction != null) {
            saveAsNewAction.run();
        } else if (view.getDataOwner() == null || !view.getDataOwner().isExisting()) {
            RecordActions.saveAsNew(view);
        } else {
            createCopy();
        }
        updateCopyBanner();
    }

    /**
     * Shows the record as copy (new, not saved yet)
     */
    private void createCopy() {
        //the record stays open here, the copy opens in a new tab
        RecordActions.copyInNewTab(view);
        updateCopyBanner();
    }

    /**
     * Shows or hides the note about a copy which is not saved yet
     */
    public void updateCopyBanner() {
        String original = RecordActions.copyOf(view);
        copyBanner.setVisible(original != null);
        //a copy not saved yet can not be deleted
        if (delete != null) {
            delete.setEnabled(editable && !view.isContentLocked() && original == null);
        }
        if (original != null) {
            copyText.setText("<html>" + String.format(Messages.COPY_PENDING.toString(),
                    original.replace("&", "&amp;").replace("<", "&lt;")) + "</html>");
        }
        revalidate();
        repaint();
    }

    /**
     * Shows or hides the banner set by a plugin on the view
     */
    public void updatePluginBanner() {
        Object c = viewComponent.getClientProperty(DataPanel.PROPERTY_PLUGIN_BANNER);
        pluginBanner.removeAll();
        if (c instanceof JComponent) {
            pluginBanner.add((JComponent) c, BorderLayout.CENTER);
            pluginBanner.setVisible(true);
        } else {
            pluginBanner.setVisible(false);
        }
        revalidate();
        repaint();
    }

    /**
     * Creates the bar and the shortcuts.
     *
     * @param view the view, a JComponent
     */
    public ModernCommandBar(DataPanel view) {
        super(new BorderLayout(ModernTheme.px(8), 0));
        this.view = view;
        this.viewComponent = (JComponent) view;
        setBackground(ModernTheme.cardBackground());
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, ModernTheme.border()),
                BorderFactory.createEmptyBorder(ModernTheme.px(4), ModernTheme.px(8), ModernTheme.px(4), ModernTheme.px(8))));
        left.setOpaque(false);
        left.setBorder(BorderFactory.createEmptyBorder(ModernTheme.px(4), 0, ModernTheme.px(4), 0));

        search = ModernTheme.actionButton(new JButton(), "/images/22/viewmag.png");
        search.setMargin(new java.awt.Insets(ModernTheme.px(4), ModernTheme.px(6), ModernTheme.px(4), ModernTheme.px(6)));
        search.setToolTipText(tip(Messages.SEARCH_ANYTHING.toString(), SEARCH_PANEL));
        //opens "Search anything" right at the button (no side search panel any more)
        search.addActionListener(e -> showSearchPanel());

        save = new PrimaryButton(text("MPV5View.jButton25.toolTipText"), "/images/16/filesave.png");
        save.setToolTipText(tip(text("MPV5View.jButton25.toolTipText"), SAVE));
        save.addActionListener(e -> {
            RecordActions.save(view);
            updateCopyBanner();
        });
        JButton revert = button(null, "/images/22/revert.png", tip(text("MPV5View.jButton22.toolTipText"), REVERT));
        revert.addActionListener(e -> {
            RecordActions.revert(view);
            updateCopyBanner();
        });
        mail = button(text("DataPanelTB.mailButton.toolTipText"), "/images/22/mail_send.png", tip(text("DataPanelTB.mailButton.toolTipText"), MAIL));
        mail.addActionListener(e -> RecordActions.mail(view));
        JButton print = button(text("MPV5View.jButton23.toolTipText"), "/images/22/fileprint.png", tip(text("MPV5View.jButton23.toolTipText"), PRINT));
        print.addActionListener(e -> RecordActions.print(view));
        pdf = button("PDF", "/images/22/fileexport.png", tip(text("DataPanelTB.pdfButton.toolTipText"), PDF));
        pdf.addActionListener(e -> RecordActions.pdf(view));
        pdf.setVisible(view instanceof ExportablePanel);
        //the preview next to PDF (issue #58), shown inside YaBS
        JButton preview = button(Messages.ACTION_PREVIEW.toString(), "/images/22/viewmag.png", Messages.ACTION_PREVIEW.toString());
        preview.addActionListener(e -> RecordActions.preview(view));
        preview.setVisible(view instanceof ExportablePanel);

        left.addPinned(search);
        left.addSeparator();
        left.addPinned(save);
        left.addPinned(revert);
        left.addSeparator();
        left.addOverflowable(mail, MAIL);
        left.addOverflowable(print, PRINT);
        left.addOverflowable(preview, null);
        left.addOverflowable(pdf, PDF);
        add(left, BorderLayout.CENTER);

        //rarely used actions (plus any actions that overflow the bar) in a menu
        saveAsNewItem = menuItem(Messages.ACTION_SAVE_AS_NEW.toString(), "/images/16/filesaveas.png", SAVE_AS_NEW);
        saveAsNewItem.setToolTipText(Messages.ACTION_CREATE_COPY_TIP.toString());
        saveAsNewItem.addActionListener(e -> triggerSaveAsNew());
        //any record (a document, contact, product ...): into the clipboard of the "Ablage"
        JMenuItem copy = menuItem(Messages.ACTION_COPY_TO_CLIPBOARD.toString(), "/images/16/editcopy.png", COPY);
        copy.setToolTipText(Messages.ACTION_COPY_TO_CLIPBOARD_TIP.toString());
        copy.addActionListener(e -> copy());
        JMenuItem paste = menuItem(Messages.ACTION_PASTE.toString(), "/images/16/editpaste.png", PASTE);
        paste.addActionListener(e -> {
            if (!view.isContentLocked()) {
                RecordActions.paste(view);
            }
        });
        lock = menuItem(text("MPV5View.jButton21.toolTipText"), null, null);
        lock.setEnabled(!DatabaseObject.isAutoLockEnabled());
        lock.addActionListener(e -> {
            if (RecordActions.lock(view)) {
                lock.setEnabled(false);
            }
        });
        odt = menuItem(text("DataPanelTB.odtButton.toolTipText"), null, null);
        odt.setVisible(view instanceof ExportablePanel);
        odt.addActionListener(e -> RecordActions.odt(view));
        JMenuItem keys = menuItem(Messages.KEYBOARD_SHORTCUTS.toString(), "/images/16/configure_shortcuts.png", HELP);
        keys.addActionListener(e -> mpv5.ui.dialogs.ShortcutsDialog.show(this));

        permanentMoreItems.add(saveAsNewItem);
        permanentMoreItems.add(copy);
        permanentMoreItems.add(paste);
        permanentMoreItems.add(new JPopupMenu.Separator());
        permanentMoreItems.add(lock);
        permanentMoreItems.add(odt);
        permanentMoreItems.add(new JPopupMenu.Separator());
        permanentMoreItems.add(keys);
        rebuildMoreMenu();

        JButton moreButton = button(Messages.MORE_ACTIONS.toString() + " \u25BE", null, null);
        moreButton.addActionListener(e -> {
            rebuildMoreMenu();
            more.show(moreButton, 0, moreButton.getHeight());
        });

        favourite = button(null, "/images/22/bookmark_add.png", null);
        favourite.addActionListener(e -> {
            if (isFavourite) {
                RecordActions.removeFavourite(view);
            } else {
                RecordActions.addFavourite(view);
            }
        });
        delete = button(text("MPV5View.jButton26.toolTipText"), "/images/22/edittrash.png", tip(text("MPV5View.jButton26.toolTipText"), DELETE));
        delete.setForeground(new java.awt.Color(190, 40, 40));
        delete.addActionListener(e -> RecordActions.delete(view));
        setFavourite(false);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, ModernTheme.px(4), ModernTheme.px(4)));
        right.setOpaque(false);
        right.add(favourite);
        right.add(moreButton);
        right.add(separator());
        right.add(delete);
        add(right, BorderLayout.EAST);

        //a copy which is not saved yet: say so, with a way back
        copyBanner.setOpaque(true);
        //green: a new record, nothing is wrong (not a warning)
        boolean dark = ModernTheme.isDark();
        copyBanner.setBackground(dark ? new java.awt.Color(0x1E3A26) : new java.awt.Color(0xE3F4E6));
        copyBanner.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, dark ? new java.awt.Color(0x2E6B3E) : new java.awt.Color(0xA8D5B0)),
                BorderFactory.createEmptyBorder(ModernTheme.px(6), ModernTheme.px(10), ModernTheme.px(6), ModernTheme.px(10))));
        copyText.setForeground(dark ? new java.awt.Color(0xB9E6C2) : new java.awt.Color(0x1E5C2B));
        JButton discard = new JButton(Messages.COPY_DISCARD.toString());
        discard.setFocusable(false);
        discard.addActionListener(e -> {
            RecordActions.revert(view);
            updateCopyBanner();
        });
        copyBanner.add(copyText, BorderLayout.CENTER);
        copyBanner.add(discard, BorderLayout.EAST);
        copyBanner.setVisible(false);

        pluginBanner.setOpaque(false);
        pluginBanner.setVisible(false);

        JPanel banners = new JPanel();
        banners.setLayout(new javax.swing.BoxLayout(banners, javax.swing.BoxLayout.Y_AXIS));
        banners.setOpaque(false);
        banners.add(copyBanner);
        banners.add(pluginBanner);
        add(banners, BorderLayout.SOUTH);

        //the view became a copy (also when made from another tab)
        viewComponent.addPropertyChangeListener("yabs.copyOf", e -> SwingUtilities.invokeLater(() -> {
            updateCopyBanner();
            if (RecordActions.isPendingCopy(view)) {
                setEditable(true);
            }
        }));
        viewComponent.addPropertyChangeListener(DataPanel.PROPERTY_PLUGIN_BANNER, e -> {
            if (SwingUtilities.isEventDispatchThread()) {
                updatePluginBanner();
            } else {
                SwingUtilities.invokeLater(this::updatePluginBanner);
            }
        });
        viewComponent.addPropertyChangeListener(DataPanel.PROPERTY_CONTENT_LOCKED, e -> {
            if (SwingUtilities.isEventDispatchThread()) {
                setEditable(editable);
            } else {
                SwingUtilities.invokeLater(() -> setEditable(editable));
            }
        });

        //all elements of the bar in one height
        uniformHeight(left);
        uniformHeight(right);

        shortcut(SAVE, () -> {
            if (editable) {
                RecordActions.save(view);
                updateCopyBanner();
            }
        });
        shortcut(SAVE_AS_NEW, this::triggerSaveAsNew);
        shortcut(REVERT, () -> {
            RecordActions.revert(view);
            updateCopyBanner();
        });
        shortcut(PRINT, () -> RecordActions.print(view));
        shortcut(PDF, () -> RecordActions.pdf(view));
        shortcut(MAIL, () -> {
            if (mail.isEnabled()) {
                RecordActions.mail(view);
            }
        });
        shortcut(FIND, () -> {
            if (findAction != null) {
                findAction.run();
            } else {
                mpv5.ui.dialogs.SearchAnything.handleContextualFind();
            }
        });
        shortcut(SEARCH_PANEL, mpv5.ui.dialogs.SearchAnything::openCenter);
        shortcut(COPY, this::copy);
        shortcut(PASTE, () -> {
            if (!view.isContentLocked()) {
                RecordActions.paste(view);
            }
        });
        shortcut(FAVOURITE, favourite::doClick);
        shortcut(HELP, () -> mpv5.ui.dialogs.ShortcutsDialog.show(this));
        shortcut(DELETE, () -> {
            if (editable && !view.isContentLocked()) {
                RecordActions.delete(view);
            }
        });
    }

    private static String text(String key) {
        try {
            return LanguageManager.getBundle().getString(key);
        } catch (Exception e) {
            return key;
        }
    }

    /**
     * @param label
     * @param key may be null
     * @return "label (Ctrl+S)"
     */
    public static String tip(String label, KeyStroke key) {
        return key == null ? label : label + "  (" + keyText(key) + ")";
    }

    /**
     * @param key
     * @return e.g. "Ctrl+Shift+S", localized
     */
    public static String keyText(KeyStroke key) {
        String m = InputEvent.getModifiersExText(key.getModifiers());
        return (m.isEmpty() ? "" : m + "+") + KeyEvent.getKeyText(key.getKeyCode());
    }

    private static JButton button(String label, String icon, String tooltip) {
        JButton b = ModernTheme.actionButton(new JButton(label), icon);
        if (label == null) {
            b.setMargin(new java.awt.Insets(ModernTheme.px(4), ModernTheme.px(6), ModernTheme.px(4), ModernTheme.px(6)));
        }
        b.setToolTipText(tooltip);
        return b;
    }

    private static JMenuItem menuItem(String label, String icon, KeyStroke key) {
        JMenuItem i = new JMenuItem(label);
        i.setFont(ModernTheme.font(1, java.awt.Font.PLAIN));
        java.net.URL url = icon == null ? null : ModernCommandBar.class.getResource(icon);
        if (url != null) {
            i.setIcon(new javax.swing.ImageIcon(url));
        }
        if (key != null) {
            //shown in the menu; the key itself is handled by the bar
            i.setAccelerator(key);
        }
        return i;
    }

    /**
     * Rebuilds the {@code More ▾} popup with any actions currently overflowing
     * the single-row command bar on top, followed by the permanent menu items.
     */
    public void rebuildMoreMenu() {
        more.removeAll();
        if (left.populateMenu(more) > 0) {
            more.addSeparator();
        }
        for (JComponent c : permanentMoreItems) {
            more.add(c);
        }
    }

    private static JComponent separator() {
        JSeparator s = new JSeparator(SwingConstants.VERTICAL);
        s.setPreferredSize(new Dimension(ModernTheme.px(8), ModernTheme.px(26)));
        s.setForeground(ModernTheme.border());
        return s;
    }

    private void copy() {
        RecordActions.copyToClipboard(view);
        if (view.getDataOwner().isExisting()) {
            mpv5.YabsViewProxy.instance().addMessage(Messages.ACTION_COPY + ": " + view.getDataOwner().__getCname(), java.awt.Color.GREEN);
        }
    }

    /**
     * Opens "Search anything" below the search button
     */
    private void showSearchPanel() {
        mpv5.ui.dialogs.SearchAnything.openAt(search);
    }

    /**
     * @return the height of all elements in the bar: room for a 22px icon
     */
    public static int barHeight() {
        return Math.max(ModernTheme.px(34), 22 + ModernTheme.px(10));
    }

    /**
     * Gives the buttons, combo boxes ... in the container the height of the
     * bar; icon-only buttons become square
     */
    private static void uniformHeight(java.awt.Container c) {
        for (java.awt.Component x : c.getComponents()) {
            if (x instanceof JSeparator || x instanceof javax.swing.Box.Filler) {
                continue;
            }
            if (x instanceof AbstractButton || x instanceof mpv5.ui.modern.SplitButton
                    || x instanceof mpv5.ui.beans.PrinitingComboBox || x instanceof javax.swing.JComboBox) {
                fixHeight((JComponent) x);
            } else if (x instanceof java.awt.Container) {
                uniformHeight((java.awt.Container) x);
            }
        }
    }

    private static void fixHeight(JComponent x) {
        int h = barHeight();
        x.setPreferredSize(null);
        Dimension d = x.getPreferredSize();
        boolean iconOnly = x instanceof AbstractButton && ((AbstractButton) x).getIcon() != null
                && (((AbstractButton) x).getText() == null || ((AbstractButton) x).getText().isEmpty());
        Dimension size = new Dimension(iconOnly ? h : d.width, h);
        x.setPreferredSize(size);
        x.setMinimumSize(new Dimension(Math.min(size.width, ModernTheme.px(24)), h));
        x.setMaximumSize(new Dimension(Integer.MAX_VALUE, h));
    }

    /**
     * Binds a key to an action while the focus is in the view. Keys the
     * focused component uses itself (e.g. Ctrl+C in a text field) stay with
     * the component.
     *
     * @param key
     * @param action
     */
    public final void shortcut(KeyStroke key, final Runnable action) {
        String name = "modernbar." + key.toString();
        viewComponent.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(key, name);
        viewComponent.getActionMap().put(name, new AbstractAction() {
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent e) {
                action.run();
            }
        });
    }

    /**
     * Adds an action of the view to the bar (after print / PDF)
     *
     * @param c a button, combo box ...
     * @param key shortcut, may be null
     */
    public void addAction(JComponent c, KeyStroke key) {
        if (!hasExtras) {
            left.addSeparator();
            hasExtras = true;
        }
        if (c instanceof AbstractButton) {
            if (!(c instanceof javax.swing.JCheckBox)) {
                //a check box stays a check box
                ModernTheme.actionButton((AbstractButton) c, null);
            } else {
                c.setOpaque(false);
            }
            final AbstractButton b = (AbstractButton) c;
            if (key != null) {
                b.setToolTipText(tip(b.getToolTipText() != null ? b.getToolTipText() : b.getText(), key));
                shortcut(key, () -> {
                    if (b.isEnabled() && b.isVisible()) {
                        b.doClick();
                    }
                });
            }
        }
        left.addOverflowable(c, key);
        uniformHeight(left);
    }

    /**
     * @param action what Ctrl+F does (e.g. focus the filter of a list); the
     * search panel if not set
     */
    public void setFindAction(Runnable action) {
        this.findAction = action;
    }

    /**
     * @param action what "Save as copy" does on this view
     */
    public void setSaveAsNewAction(Runnable action) {
        this.saveAsNewAction = action;
    }

    /**
     * @return the "Save as copy" menu item inside the {@code More ▾} dropdown
     */
    public JMenuItem getSaveAsNewMenuItem() {
        return saveAsNewItem;
    }

    /**
     * @return the {@code More ▾} popup menu (including any overflowed actions
     * after {@link #rebuildMoreMenu()})
     */
    public JPopupMenu getMoreMenu() {
        return more;
    }

    /**
     * @return the single-row left overflow bar
     */
    public mpv5.ui.modern.OverflowBar getLeftBar() {
        return left;
    }

    /**
     * @param favourite
     */
    public void setFavourite(boolean favourite) {
        isFavourite = favourite;
        java.net.URL url = ModernCommandBar.class.getResource(favourite ? "/images/22/bookmark.png" : "/images/22/bookmark_add.png");
        if (url != null) {
            this.favourite.setIcon(new javax.swing.ImageIcon(url));
        }
        this.favourite.setToolTipText(tip(text("MPV5View.jButton24.toolTipText"), FAVOURITE));
        //another record shown: no note about a copy
        updateCopyBanner();
    }

    /**
     * @param on false for read-only records: no save, delete, lock
     */
    public void setEditable(boolean on) {
        editable = on;
        boolean contentLocked = view.isContentLocked();
        save.setEnabled(on);
        delete.setEnabled(on && !contentLocked && !RecordActions.isPendingCopy(view));
        lock.setEnabled(on && !contentLocked && !DatabaseObject.isAutoLockEnabled());
    }

    /**
     * @param enable mail is possible
     */
    public void setExportButtonsEnabled(boolean enable) {
        mail.setEnabled(enable);
    }
}
