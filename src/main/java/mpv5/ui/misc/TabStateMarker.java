/*
 *  This file is part of YaBS.
 *
 *      YaBS is free software: you can redistribute it and/or modify
 *      it under the terms of the GNU General Public License as published by
 *      the Free Software Foundation, either version 3 of the License, or
 *      (at your option) any later version.
 *
 *      YaBS is distributed in the hope that it will be useful,
 *      but WITHOUT ANY WARRANTY; without even the implied warranty of
 *      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *      GNU General Public License for more details.
 *
 *      You should have received a copy of the GNU General Public License
 *      along with YaBS.  If not, see <http://www.gnu.org/licenses/>.
 */
package mpv5.ui.misc;

import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Component;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.IntFunction;
import javax.swing.AbstractButton;
import javax.swing.JComboBox;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTabbedPane;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.text.JTextComponent;
import mpv5.db.common.DatabaseObject;
import mpv5.globals.Messages;
import mpv5.ui.panels.DataPanel;

/**
 * Colours the tabs of new and changed records: new = never saved, changed =
 * typed or clicked in an editable field of the tab since it was loaded or
 * saved. Saved again (or another record loaded): normal colour.
 */
public final class TabStateMarker {

    private static final Color CHANGED_LIGHT = new Color(196, 98, 0);
    private static final Color CHANGED_DARK = new Color(255, 170, 70);
    private final JTabbedPane tabs;
    private final IntFunction<DataPanel> panelAt;
    /**
     * tab content -> the record it had when it was marked changed
     */
    private final Map<Component, DatabaseObject> changed = new WeakHashMap<Component, DatabaseObject>();

    /**
     * @param tabs the tab pane of the main window
     * @param panelAt the data panel of a tab (null: not a record)
     */
    public static void install(JTabbedPane tabs, IntFunction<DataPanel> panelAt) {
        new TabStateMarker(tabs, panelAt);
    }

    private TabStateMarker(JTabbedPane tabs, IntFunction<DataPanel> panelAt) {
        this.tabs = tabs;
        this.panelAt = panelAt;
        Toolkit.getDefaultToolkit().addAWTEventListener(e -> edited(e), AWTEvent.KEY_EVENT_MASK | AWTEvent.MOUSE_EVENT_MASK);
        DatabaseObject.addSaveListener(o -> SwingUtilities.invokeLater(() -> saved(o)));
        new Timer(600, e -> update()).start();
    }

    /**
     * A key typed or a click in an editable field of a tab marks it changed
     */
    private void edited(AWTEvent e) {
        boolean edit = (e.getID() == KeyEvent.KEY_TYPED && !((KeyEvent) e).isControlDown() && !((KeyEvent) e).isAltDown())
                || (e.getID() == MouseEvent.MOUSE_RELEASED);
        if (!edit || !(e.getSource() instanceof Component)) {
            return;
        }
        Component src = (Component) e.getSource();
        if (!editable(src, e.getID() == KeyEvent.KEY_TYPED)) {
            return;
        }
        int i = tabs.getSelectedIndex();
        if (i < 0) {
            return;
        }
        Component content = tabs.getComponentAt(i);
        DataPanel p = panelAt.apply(i);
        if (content != null && p != null && p.getDataOwner() != null && SwingUtilities.isDescendingFrom(src, content)) {
            changed.put(content, p.getDataOwner());
        }
    }

    private static boolean editable(Component c, boolean typed) {
        //combo boxes and spinners first: their arrows are buttons
        for (Component k = c; k != null && !(k instanceof DataPanel); k = k.getParent()) {
            if (k instanceof JComboBox || k instanceof JSpinner) {
                return k.isEnabled();
            }
        }
        for (Component k = c; k != null; k = k.getParent()) {
            if (k instanceof JTextComponent) {
                return ((JTextComponent) k).isEditable() && k.isEnabled();
            }
            if (k instanceof JTable) {
                return typed || ((JTable) k).isEditing();
            }
            if (k instanceof AbstractButton) {
                //check boxes and radio buttons, not the buttons (save, print ...)
                return !typed && k instanceof JToggleButton && k.isEnabled();
            }
            if (k instanceof DataPanel) {
                return false;
            }
        }
        return false;
    }

    private void saved(DatabaseObject o) {
        changed.values().removeIf(d -> d == o || (d.getContext().equals(o.getContext()) && d.__getIDS() > 0
                && d.__getIDS().equals(o.__getIDS())));
    }

    private void update() {
        boolean dark = mpv5.ui.modern.ModernTheme.isDark();
        for (int i = 0; i < tabs.getTabCount(); i++) {
            DataPanel p = panelAt.apply(i);
            Component content = tabs.getComponentAt(i);
            DatabaseObject owner = p == null ? null : p.getDataOwner();
            DatabaseObject marked = changed.get(content);
            if (marked != null && marked != owner) {
                //another record loaded into the tab
                changed.remove(content);
                marked = null;
            }
            boolean isNew = owner != null && !owner.isExisting();
            boolean isChanged = marked != null;
            Color fg = isNew || isChanged ? (dark ? CHANGED_DARK : CHANGED_LIGHT) : null;
            if (!java.util.Objects.equals(tabs.getForegroundAt(i), fg == null ? tabs.getForeground() : fg)) {
                tabs.setForegroundAt(i, fg);
            }
            String tip = isNew ? Messages.TAB_NEW.toString() : isChanged ? Messages.TAB_CHANGED.toString() : null;
            if (!java.util.Objects.equals(tabs.getToolTipTextAt(i), tip)) {
                tabs.setToolTipTextAt(i, tip);
            }
        }
    }
}
