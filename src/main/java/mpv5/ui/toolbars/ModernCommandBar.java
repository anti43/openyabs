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
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.JToggleButton;
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
        l.add(new Object[]{SAVE_AS_NEW, text("MPV5View.jButton4.toolTipText")});
        l.add(new Object[]{REVERT, Messages.SHORTCUT_REVERT.toString()});
        l.add(new Object[]{PRINT, text("MPV5View.jButton23.toolTipText")});
        l.add(new Object[]{PDF, text("DataPanelTB.pdfButton.toolTipText")});
        l.add(new Object[]{MAIL, text("DataPanelTB.mailButton.toolTipText")});
        l.add(new Object[]{FIND, Messages.SHORTCUT_FIND_LIST.toString()});
        l.add(new Object[]{SEARCH_PANEL, text("DataPanelTB.but1.toolTipText")});
        l.add(new Object[]{COPY, Messages.SHORTCUT_COPY_RECORD.toString()});
        l.add(new Object[]{PASTE, Messages.SHORTCUT_PASTE_RECORD.toString()});
        l.add(new Object[]{FAVOURITE, text("MPV5View.jButton24.toolTipText")});
        l.add(new Object[]{DELETE, Messages.SHORTCUT_DELETE_RECORD.toString()});
        l.add(new Object[]{HISTORY, Messages.SHORTCUT_HISTORY.toString()});
        l.add(new Object[]{HELP, Messages.KEYBOARD_SHORTCUTS.toString()});
        return l;
    }
    private final DataPanel view;
    private final JComponent viewComponent;
    private final JPanel left = new JPanel(new WrapLayout(FlowLayout.LEFT, ModernTheme.px(4), ModernTheme.px(4)));
    private final JPanel extras = new JPanel(new FlowLayout(FlowLayout.LEFT, ModernTheme.px(4), 0));
    private final JToggleButton search;
    private final PrimaryButton save;
    private final JButton favourite;
    private final JButton delete;
    private final JMenuItem lock;
    private final JMenuItem odt;
    private final JButton mail;
    private final JButton pdf;
    private boolean isFavourite;
    private boolean editable = true;
    private Runnable findAction;

    /**
     * Creates the bar and the shortcuts. Create it after the rest of the view:
     * it shows or hides the search panel of the view.
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
        extras.setOpaque(false);

        search = ModernTheme.actionButton(new JToggleButton(), "/images/22/viewmag.png");
        search.setMargin(new java.awt.Insets(ModernTheme.px(4), ModernTheme.px(6), ModernTheme.px(4), ModernTheme.px(6)));
        search.setToolTipText(tip(text("DataPanelTB.but1.toolTipText"), SEARCH_PANEL));
        //same setting as the classic toolbar
        search.setSelected(User.getCurrentUser().getProperties().getProperty(DataPanelTB.class.getName(), "jToggleButton1"));
        search.addActionListener(e -> {
            User.getCurrentUser().getProperties().changeProperty(DataPanelTB.class.getName(), "jToggleButton1", search.isSelected());
            view.showSearchBar(search.isSelected());
        });
        view.showSearchBar(search.isSelected());

        save = new PrimaryButton(text("MPV5View.jButton25.toolTipText"), "/images/16/filesave.png");
        save.setToolTipText(tip(text("MPV5View.jButton25.toolTipText"), SAVE));
        save.addActionListener(e -> RecordActions.save(view));
        JButton revert = button(null, "/images/22/revert.png", tip(text("MPV5View.jButton22.toolTipText"), REVERT));
        revert.addActionListener(e -> RecordActions.revert(view));
        mail = button(text("DataPanelTB.mailButton.toolTipText"), "/images/22/mail_send.png", tip(text("DataPanelTB.mailButton.toolTipText"), MAIL));
        mail.addActionListener(e -> RecordActions.mail(view));
        JButton print = button(text("MPV5View.jButton23.toolTipText"), "/images/22/fileprint.png", tip(text("MPV5View.jButton23.toolTipText"), PRINT));
        print.addActionListener(e -> RecordActions.print(view));
        pdf = button("PDF", "/images/22/fileexport.png", tip(text("DataPanelTB.pdfButton.toolTipText"), PDF));
        pdf.addActionListener(e -> RecordActions.pdf(view));
        pdf.setVisible(view instanceof ExportablePanel);

        left.add(search);
        left.add(separator());
        left.add(save);
        left.add(revert);
        left.add(separator());
        left.add(mail);
        left.add(print);
        left.add(pdf);
        left.add(extras);
        add(left, BorderLayout.CENTER);

        //rarely used actions in a menu
        JPopupMenu more = new JPopupMenu();
        JMenuItem saveAsNew = item(more, text("MPV5View.jButton4.toolTipText"), "/images/16/filesaveas.png", SAVE_AS_NEW);
        saveAsNew.addActionListener(e -> RecordActions.saveAsNew(view));
        JMenuItem copy = item(more, text("DataPanelTB.but10.toolTipText"), "/images/16/editcopy.png", COPY);
        copy.addActionListener(e -> copy());
        JMenuItem paste = item(more, Messages.ACTION_PASTE.toString(), "/images/16/editpaste.png", PASTE);
        paste.addActionListener(e -> RecordActions.paste(view));
        more.addSeparator();
        lock = item(more, text("MPV5View.jButton21.toolTipText"), null, null);
        lock.setEnabled(!DatabaseObject.isAutoLockEnabled());
        lock.addActionListener(e -> {
            if (RecordActions.lock(view)) {
                lock.setEnabled(false);
            }
        });
        odt = item(more, text("DataPanelTB.odtButton.toolTipText"), null, null);
        odt.setVisible(view instanceof ExportablePanel);
        odt.addActionListener(e -> RecordActions.odt(view));
        more.addSeparator();
        JMenuItem keys = item(more, Messages.KEYBOARD_SHORTCUTS.toString(), "/images/16/configure_shortcuts.png", HELP);
        keys.addActionListener(e -> mpv5.ui.dialogs.ShortcutsDialog.show(this));
        JButton moreButton = button(Messages.MORE_ACTIONS.toString() + " \u25BE", null, null);
        moreButton.addActionListener(e -> more.show(moreButton, 0, moreButton.getHeight()));

        favourite = button(null, "/images/22/bookmark_add.png", null);
        favourite.addActionListener(e -> {
            if (isFavourite) {
                RecordActions.removeFavourite(view);
            } else {
                RecordActions.addFavourite(view);
            }
        });
        setFavourite(false);
        delete = button(text("MPV5View.jButton26.toolTipText"), "/images/22/edittrash.png", tip(text("MPV5View.jButton26.toolTipText"), DELETE));
        delete.setForeground(new java.awt.Color(190, 40, 40));
        delete.addActionListener(e -> RecordActions.delete(view));

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, ModernTheme.px(4), ModernTheme.px(4)));
        right.setOpaque(false);
        right.add(favourite);
        right.add(moreButton);
        right.add(separator());
        right.add(delete);
        add(right, BorderLayout.EAST);

        shortcut(SAVE, () -> {
            if (editable) {
                RecordActions.save(view);
            }
        });
        shortcut(SAVE_AS_NEW, () -> RecordActions.saveAsNew(view));
        shortcut(REVERT, () -> RecordActions.revert(view));
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
                showSearchPanel();
            }
        });
        shortcut(SEARCH_PANEL, this::showSearchPanel);
        shortcut(COPY, this::copy);
        shortcut(PASTE, () -> RecordActions.paste(view));
        shortcut(FAVOURITE, favourite::doClick);
        shortcut(HELP, () -> mpv5.ui.dialogs.ShortcutsDialog.show(this));
        shortcut(DELETE, () -> {
            if (editable) {
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

    private static JMenuItem item(JPopupMenu menu, String label, String icon, KeyStroke key) {
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
        menu.add(i);
        return i;
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

    private void showSearchPanel() {
        if (!search.isSelected()) {
            search.doClick();
        }
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
        if (extras.getComponentCount() == 0) {
            extras.add(separator());
        }
        if (c instanceof AbstractButton) {
            ModernTheme.actionButton((AbstractButton) c, null);
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
        extras.add(c);
    }

    /**
     * @param action what Ctrl+F does (e.g. focus the filter of a list); the
     * search panel if not set
     */
    public void setFindAction(Runnable action) {
        this.findAction = action;
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
    }

    /**
     * @param on false for read-only records: no save, delete, lock
     */
    public void setEditable(boolean on) {
        editable = on;
        save.setEnabled(on);
        delete.setEnabled(on);
        lock.setEnabled(on && !DatabaseObject.isAutoLockEnabled());
    }

    /**
     * @param enable mail is possible
     */
    public void setExportButtonsEnabled(boolean enable) {
        mail.setEnabled(enable);
    }
}
