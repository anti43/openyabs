
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

//~--- non-JDK imports --------------------------------------------------------
import mpv5.ui.beans.LabeledTextField;

//~--- JDK imports ------------------------------------------------------------

import java.awt.Color;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.text.JTextComponent;

/**
 * Utility to briefly highlight/blink input fields on validation errors or search misses.
 */
public class TextFieldUtils {

    private static final String BLINK_STATE_KEY = "yabs.textfield.blinkState";
    private static final String FLAT_OUTLINE_KEY = "JComponent.outline";
    private static final int BLINK_INTERVAL_MS = 550;

    public static void blinkerGrey(LabeledTextField field) {
        if (field != null) {
            blink(field.getTextField(), Color.GRAY);
        }
    }

    /**
     * Lets a text field blink
     * @param field
     */
    public static void blinkerRed(JTextField field) {
        blink(field, Color.RED);
    }

    /**
     * Lets a text field blink
     * @param lfield
     */
    public static void blinkerRed(LabeledTextField lfield) {
        if (lfield != null) {
            blink(lfield.getTextField(), Color.RED);
        }
    }

    /**
     *
     * @param component
     * @param color
     */
    public static void blink(JComponent component, Color color) {
        blink(component, 2, color);
    }

    public static void blinker(JTextField jTextField1, Color color) {
        blink(jTextField1, color);
    }

    private static void blink(final JComponent component, final int count, final Color color) {
        if (component == null || count <= 0) {
            return;
        }
        Runnable start = () -> startBlinkOnEdt(component, count, color);
        if (SwingUtilities.isEventDispatchThread()) {
            start.run();
        } else {
            SwingUtilities.invokeLater(start);
        }
    }

    private static boolean useFlatOutline(JComponent component) {
        if (!(component instanceof JTextComponent || component instanceof JComboBox)) {
            return false;
        }
        return UIManager.getLookAndFeel() instanceof com.formdev.flatlaf.FlatLaf;
    }

    private static void startBlinkOnEdt(JComponent component, int count, Color color) {
        Object existing = component.getClientProperty(BLINK_STATE_KEY);
        BlinkState state;
        if (existing instanceof BlinkState) {
            state = (BlinkState) existing;
            if (state.timer != null) {
                state.timer.stop();
            }
        } else {
            state = new BlinkState(
                    component.getBorder(),
                    component.getBackground(),
                    component.isOpaque(),
                    component.getClientProperty(FLAT_OUTLINE_KEY),
                    useFlatOutline(component));
            component.putClientProperty(BLINK_STATE_KEY, state);
        }

        state.applyHighlight(component, color);
        final int[] remainingToggles = {count * 2 - 1};
        final boolean[] highlighted = {true};

        Timer timer = new Timer(BLINK_INTERVAL_MS, e -> {
            if (highlighted[0]) {
                state.restore(component);
                highlighted[0] = false;
            } else {
                state.applyHighlight(component, color);
                highlighted[0] = true;
            }
            remainingToggles[0]--;
            if (remainingToggles[0] <= 0) {
                if (highlighted[0]) {
                    state.restore(component);
                }
                ((Timer) e.getSource()).stop();
                if (component.getClientProperty(BLINK_STATE_KEY) == state) {
                    component.putClientProperty(BLINK_STATE_KEY, null);
                }
            }
        });
        timer.setRepeats(true);
        state.timer = timer;
        timer.start();
    }

    private static final class BlinkState {
        final Border originalBorder;
        final Color originalBackground;
        final boolean originalOpaque;
        final Object originalOutline;
        final boolean flatOutline;
        final boolean rounded;
        final int thickness;
        Timer timer;

        BlinkState(Border originalBorder, Color originalBackground, boolean originalOpaque,
                Object originalOutline, boolean flatOutline) {
            this.originalBorder = originalBorder;
            this.originalBackground = originalBackground;
            this.originalOpaque = originalOpaque;
            this.originalOutline = originalOutline;
            this.flatOutline = flatOutline;
            if (originalBorder instanceof LineBorder) {
                this.rounded = ((LineBorder) originalBorder).getRoundedCorners();
                this.thickness = ((LineBorder) originalBorder).getThickness();
            } else {
                this.rounded = true;
                this.thickness = 1;
            }
        }

        void applyHighlight(JComponent component, Color color) {
            if (flatOutline) {
                component.putClientProperty(FLAT_OUTLINE_KEY, Color.RED.equals(color) ? "error" : color);
                component.repaint();
                return;
            }
            component.setOpaque(true);
            try {
                component.setBorder(new LineBorder(color, thickness, rounded));
            } catch (Exception e) {
                component.setBackground(color);
            }
            component.repaint();
        }

        void restore(JComponent component) {
            if (flatOutline) {
                component.putClientProperty(FLAT_OUTLINE_KEY, originalOutline);
                component.repaint();
                return;
            }
            component.setOpaque(originalOpaque);
            component.setBackground(originalBackground);
            try {
                component.setBorder(originalBorder);
            } catch (Exception ignored) {
            }
            component.revalidate();
            component.repaint();
        }
    }
}
//~ Formatted by Jindent --- http://www.jindent.com

