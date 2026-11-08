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
        //open windows take the new fonts; views opened from now on fully
        for (Window w : Window.getWindows()) {
            if (w.isDisplayable()) {
                SwingUtilities.updateComponentTreeUI(w);
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

    /**
     * Ctrl+Plus / Ctrl+Minus / Ctrl+0 (also with the keys of the number pad
     * and Ctrl+= of US keyboards) in the whole window
     *
     * @param root
     */
    public static void install(JRootPane root) {
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
