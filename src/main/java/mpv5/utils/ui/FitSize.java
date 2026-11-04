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
package mpv5.utils.ui;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.AWTEventListener;
import java.awt.event.HierarchyEvent;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import javax.swing.AbstractButton;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.SwingUtilities;

/**
 * Makes components with a fixed size (set by the GUI builder for the old look
 * and feel) large enough for their text, so that nothing is cut off with
 * larger fonts, other look and feels or high resolution displays.
 * <p>
 * Components are only enlarged, never shrunk. The original size is kept as
 * client property, so the check can be repeated after a look and feel change
 * ({@link #fitAll()}).
 */
public final class FitSize {

    private static final String BASE_PREF = "FitSize.basePreferred";
    private static final String BASE_MAX = "FitSize.baseMaximum";
    /**
     * Set this client property to {@link Boolean#TRUE} to leave a component
     * alone
     */
    public static final String IGNORE = "FitSize.ignore";
    private static boolean installed;

    private FitSize() {
    }

    /**
     * Check every component when it is shown for the first time
     */
    public static synchronized void install() {
        if (installed) {
            return;
        }
        installed = true;
        Toolkit.getDefaultToolkit().addAWTEventListener(new AWTEventListener() {
            @Override
            public void eventDispatched(AWTEvent event) {
                if (event instanceof HierarchyEvent) {
                    HierarchyEvent e = (HierarchyEvent) event;
                    //only once per change, at the component that became visible
                    if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0
                            && e.getComponent() == e.getChanged() && e.getComponent().isShowing()
                            && e.getComponent() instanceof Container) {
                        fit((Container) e.getComponent());
                    }
                }
            }
        }, AWTEvent.HIERARCHY_EVENT_MASK);
    }

    /**
     * Check all open windows again (e.g. after a look and feel change)
     */
    public static void fitAll() {
        for (Window w : Window.getWindows()) {
            fit(w);
        }
    }

    /**
     * Check the given container and all its children
     *
     * @param root
     */
    public static void fit(Container root) {
        if (fitTree(root)) {
            root.revalidate();
            root.repaint();
        }
    }

    private static boolean fitTree(Component c) {
        boolean changed = false;
        if (c instanceof Container) {
            for (Component child : ((Container) c).getComponents()) {
                changed |= fitTree(child);
            }
        }
        if (!(c instanceof JComponent) || Boolean.TRUE.equals(((JComponent) c).getClientProperty(IGNORE))) {
            return changed;
        }
        JComponent j = (JComponent) c;
        Dimension needed = needed(j);
        return apply(j, needed) || changed;
    }

    /**
     * @param j
     * @return the size the component needs to show its text completely, null
     * for components which are not checked (text fields, tables, ...: they
     * keep their designed size)
     */
    static Dimension needed(JComponent j) {
        if (j instanceof JSpinner && ((JSpinner) j).getEditor() instanceof JSpinner.DateEditor) {
            return dateSpinnerSize((JSpinner) j);
        } else if (j instanceof JLabel || j instanceof AbstractButton) {
            return j.getUI() == null ? null : j.getUI().getPreferredSize(j);
        } else if (j.getClass().getName().startsWith("mpv5.ui.beans.") && j.getLayout() != null) {
            //composite fields (date chooser, labeled fields): make room for their (enlarged) parts
            return j.getLayout().preferredLayoutSize(j);
        }
        return null;
    }

    /**
     * For GroupLayout code of the GUI builder: the designed width, or more if
     * the component needs it to show its text
     *
     * @param c the component
     * @param designed the width from the GUI builder
     * @return
     */
    public static int w(Component c, int designed) {
        Dimension d = neededForLayout(c);
        return d == null ? designed : Math.max(designed, d.width);
    }

    /**
     * For GroupLayout code of the GUI builder: the designed height, or more if
     * the component needs it to show its text
     *
     * @param c the component
     * @param designed the height from the GUI builder
     * @return
     */
    public static int h(Component c, int designed) {
        Dimension d = neededForLayout(c);
        return d == null ? designed : Math.max(designed, d.height);
    }

    private static Dimension neededForLayout(Component c) {
        if (!(c instanceof JComponent) || Boolean.TRUE.equals(((JComponent) c).getClientProperty(IGNORE))) {
            return null;
        }
        try {
            fitTree(c);//enlarge the parts of composite fields first
            return needed((JComponent) c);
        } catch (RuntimeException e) {
            //never break a form because of the size check
            return null;
        }
    }

    /**
     * Enlarge the fixed preferred (and maximum) size to the needed size
     */
    private static boolean apply(JComponent j, Dimension needed) {
        if (needed == null || !j.isPreferredSizeSet()) {
            return false;
        }
        Dimension base = (Dimension) j.getClientProperty(BASE_PREF);
        if (base == null) {
            base = new Dimension(j.getPreferredSize());
            j.putClientProperty(BASE_PREF, base);
            if (j.isMaximumSizeSet()) {
                j.putClientProperty(BASE_MAX, new Dimension(j.getMaximumSize()));
            }
        }
        Dimension target = new Dimension(Math.max(base.width, needed.width), Math.max(base.height, needed.height));
        if (target.equals(j.getPreferredSize())) {
            return false;
        }
        j.setPreferredSize(target);
        Dimension max = (Dimension) j.getClientProperty(BASE_MAX);
        if (max != null) {
            j.setMaximumSize(new Dimension(Math.max(max.width, target.width), Math.max(max.height, target.height)));
        }
        return true;
    }

    /**
     * Size of a date spinner which fits every date of its format (the longest
     * month name)
     */
    static Dimension dateSpinnerSize(JSpinner spinner) {
        JSpinner.DateEditor editor = (JSpinner.DateEditor) spinner.getEditor();
        JFormattedTextField field = editor.getTextField();
        SimpleDateFormat format = editor.getFormat();
        FontMetrics fm = field.getFontMetrics(field.getFont());
        Calendar cal = Calendar.getInstance();
        int widest = 0;
        for (int month = 0; month < 12; month++) {
            cal.set(2028, month, 28, 23, 58, 58);//two digit day, hour, ...
            widest = Math.max(widest, fm.stringWidth(format.format(cal.getTime())));
        }
        int current = fm.stringWidth(field.getText() == null ? "" : field.getText());
        //spinner UIs leave the size to their layout
        Dimension size = spinner.getLayout() == null ? null : spinner.getLayout().preferredLayoutSize(spinner);
        if (size == null) {
            return null;
        }
        //the UI sized the spinner for the current text, add the difference to the widest date
        size.width += Math.max(0, widest - current) + fm.charWidth('0');
        return size;
    }

    /**
     * Run {@link #fitAll()} on the event dispatch thread
     */
    public static void fitAllLater() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                fitAll();
            }
        });
    }
}
