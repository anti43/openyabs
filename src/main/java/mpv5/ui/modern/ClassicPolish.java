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
package mpv5.ui.modern;

import com.formdev.flatlaf.ui.FlatLineBorder;
import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Container;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.WindowEvent;
import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.RootPaneContainer;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.SoftBevelBorder;
import javax.swing.border.TitledBorder;
import javax.swing.plaf.UIResource;

/**
 * Brings the classic (NetBeans form) panels and dialogs in line with the
 * modern look and feel, without touching the forms: the old etched, bevel and
 * hard line borders the forms set explicitly are replaced by the rounded
 * borders of the look and feel.
 * <p>
 * Only active with {@link YabsLookAndFeel}; panels of the modern views are
 * left alone.
 */
public final class ClassicPolish {

    private static final String DONE = "ClassicPolish.done";
    private static boolean windowsHooked;

    private ClassicPolish() {
    }

    /**
     * Polishes every dialog and frame when it opens (once per application)
     */
    public static synchronized void hookWindows() {
        if (windowsHooked) {
            return;
        }
        windowsHooked = true;
        Toolkit.getDefaultToolkit().addAWTEventListener(e -> {
            if (e.getID() == WindowEvent.WINDOW_OPENED && e.getSource() instanceof RootPaneContainer) {
                apply(((RootPaneContainer) e.getSource()).getContentPane());
                ((Window) e.getSource()).repaint();
            }
        }, AWTEvent.WINDOW_EVENT_MASK);
    }

    /**
     * Polishes the component and its children now and once more after
     * pending events (panels often add components later)
     *
     * @param c
     */
    public static void applyLater(Component c) {
        apply(c);
        SwingUtilities.invokeLater(() -> apply(c));
    }

    /**
     * Polishes the component and its children
     *
     * @param c
     */
    public static void apply(Component c) {
        if (!YabsLookAndFeel.isActive() || c == null) {
            return;
        }
        walk(c);
    }

    private static void walk(Component c) {
        if (c.getClass().getName().startsWith("mpv5.ui.panels.Modern")
                || c.getClass().getName().startsWith("mpv5.ui.modern.")
                || c.getClass().getName().startsWith("mpv5.ui.toolbars.Modern")) {
            return;
        }
        if (c instanceof JComponent) {
            polish((JComponent) c);
        }
        if (c instanceof Container) {
            for (Component x : ((Container) c).getComponents()) {
                walk(x);
            }
        }
    }

    private static void polish(JComponent c) {
        if (Boolean.TRUE.equals(c.getClientProperty(DONE))) {
            return;
        }
        c.putClientProperty(DONE, Boolean.TRUE);
        Border b = c.getBorder();
        if (c instanceof JButton && !(c.getParent() instanceof JToolBar)) {
            AbstractButton btn = (AbstractButton) c;
            if (isOld(b)) {
                //a button of the form with a hard frame: a normal, rounded button
                btn.setBorder(UIManager.getBorder("Button.border"));
                btn.setContentAreaFilled(true);
                btn.setOpaque(false);
            }
        } else if (b instanceof TitledBorder) {
            TitledBorder t = (TitledBorder) b;
            if (isOld(t.getBorder()) || t.getBorder() == null) {
                t.setBorder(line(1));
            }
            //section headings of the forms: bold, unless the form chose a font
            java.awt.Font f = t.getTitleFont();
            java.awt.Font dflt = UIManager.getFont("TitledBorder.font");
            if (f == null || f instanceof UIResource || f.equals(dflt)) {
                java.awt.Font base = dflt != null ? dflt : ModernTheme.baseFont();
                t.setTitleFont(base.deriveFont(java.awt.Font.BOLD));
            }
        } else if (isOld(b) && !(c instanceof AbstractButton)) {
            c.setBorder(line(insetOf(b)));
        } else if (b instanceof CompoundBorder) {
            CompoundBorder cb = (CompoundBorder) b;
            if (isOld(cb.getOutsideBorder())) {
                c.setBorder(new CompoundBorder(line(insetOf(cb.getOutsideBorder())), cb.getInsideBorder()));
            }
        }
        if (c instanceof JTable) {
            JTable t = (JTable) c;
            t.setShowVerticalLines(false);
            t.setShowHorizontalLines(true);
            t.setGridColor(UIManager.getColor("Table.gridColor"));
            int h = UIManager.getInt("Table.rowHeight");
            if (h > 0 && t.getRowHeight() < h) {
                t.setRowHeight(h);
            }
        }
    }

    private static boolean isOld(Border b) {
        return b != null && !(b instanceof UIResource)
                && (b instanceof EtchedBorder || b instanceof BevelBorder
                || b instanceof SoftBevelBorder || b.getClass() == LineBorder.class);
    }

    private static int insetOf(Border b) {
        try {
            Insets i = b.getBorderInsets(null);
            return Math.max(1, Math.min(i.top, 4));
        } catch (Exception e) {
            return 1;
        }
    }

    private static Border line(int inset) {
        int arc = Math.max(0, UIManager.getInt("Component.arc"));
        return new FlatLineBorder(new Insets(inset, inset, inset, inset), ModernTheme.border(), 1, arc);
    }
}
