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

import java.awt.Font;
import java.awt.Window;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;
import mpv5.globals.LocalSettings;
import mpv5.logging.Log;
import mpv5.ui.dialogs.subcomponents.ControlPanel_Fonts;

/**
 * Browser style zoom of the whole application (Ctrl+Plus, Ctrl+Minus,
 * Ctrl+0): the default font is scaled; the modern views derive their gaps
 * from it, so the layout grows with the text. Kept in the local settings.
 */
public final class Zoom {

    /**
     * Local setting with the zoom factor, e.g. 1.25
     */
    public static final String SETTING = "zoom";
    private static final float[] STEPS = {0.8f, 0.9f, 1f, 1.1f, 1.25f, 1.5f, 1.75f, 2f};
    public static final KeyStroke IN = KeyStroke.getKeyStroke(KeyEvent.VK_PLUS, InputEvent.CTRL_DOWN_MASK);
    public static final KeyStroke OUT = KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, InputEvent.CTRL_DOWN_MASK);
    public static final KeyStroke RESET = KeyStroke.getKeyStroke(KeyEvent.VK_0, InputEvent.CTRL_DOWN_MASK);

    private Zoom() {
    }

    /**
     * @return the current zoom factor (1 = 100 %)
     */
    public static float factor() {
        try {
            if (LocalSettings.hasProperty(SETTING)) {
                float f = Float.parseFloat(LocalSettings.getProperty(SETTING));
                if (f >= STEPS[0] && f <= STEPS[STEPS.length - 1]) {
                    return f;
                }
            }
        } catch (Exception e) {
            Log.Debug(Zoom.class, e);
        }
        return 1f;
    }

    /**
     * Applies the given (unzoomed) default font, scaled by the zoom factor
     *
     * @param base the default font of the user
     */
    public static void applyFont(Font base) {
        Font f = base.deriveFont(Math.max(6f, Math.round(base.getSize2D() * factor() * 2f) / 2f));
        ControlPanel_Fonts.applyFont(f);
        //FlatLaf derives its fonts from defaultFont
        UIManager.put("defaultFont", new FontUIResource(f));
    }

    /**
     * Applies the default font of the local settings with the zoom factor
     */
    public static void applyStoredFont() {
        applyFont(Font.decode(LocalSettings.getProperty(LocalSettings.DEFAULT_FONT)));
    }

    /**
     * @param direction +1 larger, -1 smaller, 0 back to 100 %
     */
    public static void step(int direction) {
        float now = factor();
        float next = 1f;
        if (direction > 0) {
            next = STEPS[STEPS.length - 1];
            for (float s : STEPS) {
                if (s > now + 0.001f) {
                    next = s;
                    break;
                }
            }
        } else if (direction < 0) {
            next = STEPS[0];
            for (int i = STEPS.length - 1; i >= 0; i--) {
                if (STEPS[i] < now - 0.001f) {
                    next = STEPS[i];
                    break;
                }
            }
        }
        if (Math.abs(next - now) < 0.001f) {
            return;
        }
        LocalSettings.setProperty(SETTING, String.valueOf(next));
        applyStoredFont();
        //open windows: the fonts of the look and feel are replaced by updateComponentTreeUI,
        //fonts set by the program (headings, labels of the forms, the modern views) are
        //scaled by the same factor, the row heights of tables too
        float ratio = next / now;
        for (Window w : Window.getWindows()) {
            if (w.isDisplayable()) {
                scale(w, ratio);
                SwingUtilities.updateComponentTreeUI(w);
                fitAll(w);
                w.validate();
                w.repaint();
            }
        }
        try {
            mpv5.YabsViewProxy.instance().addMessage("Zoom " + Math.round(next * 100) + " %");
        } catch (Exception e) {
            Log.Debug(Zoom.class, e);
        }
    }

    private static boolean fitting;

    /**
     * Fields with a fixed size from the forms (dropdowns, date pickers, text
     * fields, spinners) grow to fit their font when they are shown, so the
     * text stays readable when zoomed
     */
    static synchronized void installFit() {
        if (fitting) {
            return;
        }
        fitting = true;
        java.awt.Toolkit.getDefaultToolkit().addAWTEventListener(e -> {
            if (e instanceof java.awt.event.HierarchyEvent) {
                java.awt.event.HierarchyEvent h = (java.awt.event.HierarchyEvent) e;
                if ((h.getChangeFlags() & java.awt.event.HierarchyEvent.SHOWING_CHANGED) != 0
                        && h.getComponent() == h.getChanged() && h.getComponent().isShowing()) {
                    fitAll(h.getComponent());
                }
            }
        }, java.awt.AWTEvent.HIERARCHY_EVENT_MASK);
    }

    private static void fitAll(java.awt.Component c) {
        if (c instanceof java.awt.Container && !(c instanceof javax.swing.JComboBox) && !(c instanceof mpv5.ui.beans.DateChooser)) {
            for (java.awt.Component x : ((java.awt.Container) c).getComponents()) {
                fitAll(x);
            }
        }
        fit(c);
    }

    private static void fit(java.awt.Component c) {
        if (!(c instanceof JComponent) || !c.isPreferredSizeSet()) {
            return;
        }
        boolean field = c instanceof javax.swing.JComboBox || c instanceof mpv5.ui.beans.DateChooser
                || c instanceof javax.swing.JSpinner
                || (c instanceof javax.swing.JTextField && !(c.getParent() instanceof javax.swing.JSpinner.DefaultEditor))
                || c.getClass().getName().startsWith("mpv5.ui.beans.Labeled");
        if (!field) {
            return;
        }
        JComponent j = (JComponent) c;
        java.awt.Dimension set = j.getPreferredSize();
        j.setPreferredSize(null);
        java.awt.Dimension natural = j.getPreferredSize();
        int h = Math.max(set.height, natural.height);
        //a dropdown or date picker needs the room for its text and button
        int w = j instanceof javax.swing.JTextField || j.getClass().getName().startsWith("mpv5.ui.beans.Labeled") ? set.width : Math.max(set.width, Math.min(natural.width, set.width * 2));
        j.setPreferredSize(new java.awt.Dimension(w, h));
        if (j.isMinimumSizeSet() && j.getMinimumSize().height < h) {
            j.setMinimumSize(new java.awt.Dimension(j.getMinimumSize().width, h));
        }
        if (j.isMaximumSizeSet() && j.getMaximumSize().height < h) {
            j.setMaximumSize(new java.awt.Dimension(j.getMaximumSize().width, h));
        }
        if (set.height != h || set.width != w) {
            java.awt.Container p = j.getParent();
            if (p != null) {
                p.invalidate();
                if (p instanceof JComponent) {
                    ((JComponent) p).revalidate();
                }
            }
        }
    }

    private static void scale(java.awt.Component c, float ratio) {
        Font f = c.getFont();
        //only fonts of the component itself, not inherited from the parent
        if (f != null && c.isFontSet() && !(f instanceof javax.swing.plaf.UIResource)) {
            c.setFont(f.deriveFont(Math.max(6f, Math.round(f.getSize2D() * ratio * 2f) / 2f)));
        }
        if (c instanceof JComponent && ((JComponent) c).getBorder() instanceof javax.swing.border.TitledBorder) {
            javax.swing.border.TitledBorder b = (javax.swing.border.TitledBorder) ((JComponent) c).getBorder();
            Font tf = b.getTitleFont();
            if (tf != null && !(tf instanceof javax.swing.plaf.UIResource)) {
                b.setTitleFont(tf.deriveFont(Math.max(6f, Math.round(tf.getSize2D() * ratio * 2f) / 2f)));
            }
        }
        if (c instanceof javax.swing.JTable) {
            javax.swing.JTable t = (javax.swing.JTable) c;
            t.setRowHeight(Math.max(8, Math.round(t.getRowHeight() * ratio)));
        } else if (c instanceof javax.swing.JTree) {
            javax.swing.JTree t = (javax.swing.JTree) c;
            if (t.getRowHeight() > 0) {
                t.setRowHeight(Math.max(8, Math.round(t.getRowHeight() * ratio)));
            }
        }
        if (c instanceof javax.swing.JMenu) {
            for (java.awt.Component x : ((javax.swing.JMenu) c).getMenuComponents()) {
                scale(x, ratio);
            }
        }
        if (c instanceof java.awt.Container) {
            for (java.awt.Component x : ((java.awt.Container) c).getComponents()) {
                scale(x, ratio);
            }
        }
    }

    /**
     * Ctrl+Plus / Ctrl+Minus / Ctrl+0 (also with the keys of the number pad
     * and Ctrl+= of US keyboards) in the whole window
     *
     * @param root
     */
    public static void install(JRootPane root) {
        installFit();
        javax.swing.InputMap in = root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        int ctrl = InputEvent.CTRL_DOWN_MASK;
        for (int key : new int[]{KeyEvent.VK_PLUS, KeyEvent.VK_ADD, KeyEvent.VK_EQUALS}) {
            in.put(KeyStroke.getKeyStroke(key, ctrl), "yabs.zoom.in");
            in.put(KeyStroke.getKeyStroke(key, ctrl | InputEvent.SHIFT_DOWN_MASK), "yabs.zoom.in");
        }
        for (int key : new int[]{KeyEvent.VK_MINUS, KeyEvent.VK_SUBTRACT}) {
            in.put(KeyStroke.getKeyStroke(key, ctrl), "yabs.zoom.out");
        }
        for (int key : new int[]{KeyEvent.VK_0, KeyEvent.VK_NUMPAD0}) {
            in.put(KeyStroke.getKeyStroke(key, ctrl), "yabs.zoom.reset");
        }
        //these three are the accelerators of the menu items (View > Zoom)
        in.remove(IN);
        in.remove(OUT);
        in.remove(RESET);
        root.getActionMap().put("yabs.zoom.in", action(1));
        root.getActionMap().put("yabs.zoom.out", action(-1));
        root.getActionMap().put("yabs.zoom.reset", action(0));
    }

    private static AbstractAction action(final int direction) {
        return new AbstractAction() {
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                step(direction);
            }
        };
    }
}
