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
package mpv5.ui.panels;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import mpv5.logging.Log;

/**
 * The single options of the settings pages (check boxes, field labels, card
 * titles), for "Search anything": the pages are built once in the
 * background, one per event, and their texts collected. A hit opens the page
 * and shows the option.
 */
public final class SettingsIndex {

    /**
     * An option on a settings page
     */
    public static final class Option {

        public final String text;
        public final MPControlPanel.Entry entry;

        Option(String text, MPControlPanel.Entry entry) {
            this.text = text;
            this.entry = entry;
        }
    }

    private static volatile List<Option> options;
    private static boolean building;

    private SettingsIndex() {
    }

    /**
     * @return the options found so far (empty until the index is built)
     */
    public static List<Option> options() {
        List<Option> o = options;
        return o == null ? Collections.<Option>emptyList() : o;
    }

    /**
     * Builds the index in the background on the event thread, one page per
     * event (once). Only for administrators: other users would get "no
     * access" messages from the pages.
     */
    public static void build() {
        if (options != null || building) {
            return;
        }
        try {
            if (mpv5.db.objects.User.getCurrentUser().__getInthighestright()
                    > mpv5.usermanagement.MPSecurityManager.RIGHT_TO_ADMINISTRATE) {
                return;
            }
        } catch (Exception e) {
            return;
        }
        building = true;
        final List<MPControlPanel.Entry> todo = new ArrayList<MPControlPanel.Entry>();
        for (MPControlPanel.Entry e : MPControlPanel.entries()) {
            //no plugin pages (code of others), no template manager (renders a PDF preview)
            if (e.panel != null && e.panelClass != null && e.panelClass != MPControlPanel.PluginPage.class
                    && e.panelClass != mpv5.ui.dialogs.subcomponents.ControlPanel_Templates.class) {
                todo.add(e);
            }
        }
        final List<Option> found = new ArrayList<Option>();
        final Set<Class<?>> done = new HashSet<Class<?>>();
        SwingUtilities.invokeLater(new Runnable() {
            int i = 0;

            @Override
            public void run() {
                if (i >= todo.size()) {
                    options = Collections.unmodifiableList(found);
                    building = false;
                    Log.Debug(SettingsIndex.class, "Settings index: " + found.size() + " options");
                    return;
                }
                MPControlPanel.Entry e = todo.get(i++);
                //two entries may show the same page: index it once, under the first
                if (done.add(e.panelClass)) {
                    try {
                        Object p = e.panel.get();
                        if (p instanceof Component) {
                            collect((Component) p, e, found, new HashSet<String>());
                        }
                    } catch (Throwable t) {
                        Log.Debug(SettingsIndex.class, t.getMessage());
                    }
                }
                SwingUtilities.invokeLater(this);
            }
        });
    }

    static void collect(Component c, MPControlPanel.Entry e, List<Option> out, Set<String> seen) {
        String t = textOf(c);
        if (t != null) {
            String key = t.toLowerCase(Locale.ROOT);
            if (!key.equals(e.title.toLowerCase(Locale.ROOT)) && seen.add(key)) {
                out.add(new Option(t, e));
            }
        }
        if (c instanceof JComponent) {
            String b = borderTitle(((JComponent) c).getBorder());
            if (b != null && seen.add(b.toLowerCase(Locale.ROOT))) {
                out.add(new Option(b, e));
            }
        }
        if (c instanceof Container) {
            for (Component x : ((Container) c).getComponents()) {
                collect(x, e, out, seen);
            }
        }
    }

    /**
     * @return the text of an option (check box, radio button, label), null if
     * none: buttons are actions, long texts are hints
     */
    static String textOf(Component c) {
        String t = null;
        if (c instanceof AbstractButton && !(c instanceof JButton)) {
            t = ((AbstractButton) c).getText();
        } else if (c instanceof JLabel) {
            t = ((JLabel) c).getText();
        }
        return clean(t);
    }

    private static String borderTitle(Border b) {
        if (b instanceof TitledBorder) {
            return clean(((TitledBorder) b).getTitle());
        }
        if (b instanceof javax.swing.border.CompoundBorder) {
            String o = borderTitle(((javax.swing.border.CompoundBorder) b).getOutsideBorder());
            return o != null ? o : borderTitle(((javax.swing.border.CompoundBorder) b).getInsideBorder());
        }
        return null;
    }

    static String clean(String t) {
        if (t == null) {
            return null;
        }
        String s = t.replaceAll("<[^>]*>", " ").replace("&nbsp;", " ").replace("&amp;", "&")
                .replaceAll("\\s+", " ").trim();
        if (s.endsWith(":")) {
            s = s.substring(0, s.length() - 1).trim();
        }
        //no empty texts, numbers or symbols, no long hints
        if (s.length() < 3 || s.length() > 90 || !s.matches(".*\\p{L}{2,}.*")) {
            return null;
        }
        return s;
    }

    /**
     * Opens the page of the option and shows the option (scrolled into view,
     * highlighted for a moment)
     *
     * @param o
     */
    public static void show(final Option o) {
        MPControlPanel.show(o.entry);
        SwingUtilities.invokeLater(() -> SwingUtilities.invokeLater(() -> {
            Component c = find(MPControlPanel.instanceOf(), o.text);
            if (c instanceof JComponent) {
                final JComponent j = (JComponent) c;
                j.scrollRectToVisible(new java.awt.Rectangle(0, 0, j.getWidth(), j.getHeight()));
                if (j instanceof AbstractButton) {
                    j.requestFocusInWindow();
                }
                final boolean opaque = j.isOpaque();
                final Color bg = j.getBackground();
                j.setOpaque(true);
                j.setBackground(new Color(255, 236, 153));
                javax.swing.Timer t = new javax.swing.Timer(1800, ev -> {
                    j.setOpaque(opaque);
                    j.setBackground(bg);
                    j.repaint();
                });
                t.setRepeats(false);
                t.start();
            }
        }));
    }

    private static Component find(Component c, String text) {
        if (c.isShowing() && text.equals(textOf(c))) {
            return c;
        }
        if (c instanceof JComponent && c.isShowing() && text.equals(borderTitle(((JComponent) c).getBorder()))) {
            return c;
        }
        if (c instanceof Container) {
            for (Component x : ((Container) c).getComponents()) {
                Component f = find(x, text);
                if (f != null) {
                    return f;
                }
            }
        }
        return null;
    }
}
