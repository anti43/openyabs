/*
 *  This file is part of YaBS.
 *
 *  YaBS is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  YaBS is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with YaBS.  If not, see <http://www.gnu.org/licenses/>.
 */
package mpv5.ui.dialogs;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.KeyStroke;
import mpv5.globals.Messages;
import mpv5.logging.Log;
import mpv5.ui.modern.ModernTheme;

/**
 * The clipboard as a floating window, like "Search anything": no frame,
 * moved by its title, "Pin" keeps it on top and open (remembered per user),
 * else a click elsewhere or Esc closes it.
 */
public final class ClipboardWindow extends JDialog {

    private static final long serialVersionUID = 1L;
    /**
     * the user setting "clipboard on top" (also in the settings)
     */
    private static final String PROPERTY = "clipboardontop"; //NOI18N
    private final JToggleButton pin = new JToggleButton(Messages.CLIPBOARD_PIN.toString());
    private boolean placed;

    /**
     * @param owner the main window
     * @param view the clipboard
     */
    public ClipboardWindow(Frame owner, ListView view) {
        super(owner, Messages.CLIPBOARD_TITLE.toString(), false);
        setUndecorated(true);
        JPanel root = new JPanel(new BorderLayout());
        root.setBorder(BorderFactory.createLineBorder(ModernTheme.border()));
        root.add(view, BorderLayout.CENTER);
        setContentPane(root);

        //pin and close at the right of the title
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, ModernTheme.px(4), 0));
        controls.setOpaque(false);
        pin.setFont(ModernTheme.font(-1, Font.PLAIN));
        pin.setFocusable(false);
        pin.setToolTipText(Messages.CLIPBOARD_PIN_TIP.toString());
        pin.setMargin(new java.awt.Insets(ModernTheme.px(2), ModernTheme.px(6), ModernTheme.px(2), ModernTheme.px(6)));
        pin.addActionListener(e -> setPinned(pin.isSelected(), true));
        JButton close = new JButton("✕");
        close.setFont(ModernTheme.font(-1, Font.BOLD));
        close.setFocusable(false);
        close.setToolTipText(Messages.CLIPBOARD_CLOSE_TIP.toString());
        close.setMargin(new java.awt.Insets(ModernTheme.px(2), ModernTheme.px(6), ModernTheme.px(2), ModernTheme.px(6)));
        close.addActionListener(e -> setVisible(false));
        controls.add(pin);
        controls.add(close);
        view.getTitleBar().add(controls, BorderLayout.EAST);

        //moved by the title
        MouseAdapter drag = new MouseAdapter() {
            private Point origin;

            @Override
            public void mousePressed(MouseEvent e) {
                origin = javax.swing.SwingUtilities.convertPoint(e.getComponent(), e.getPoint(), getContentPane());
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (origin != null) {
                    Point p = e.getLocationOnScreen();
                    setLocation(p.x - origin.x, p.y - origin.y);
                    placed = true;
                }
            }
        };
        movesBy(view.getTitleBar(), drag, controls);

        //Esc and a click elsewhere close it, unless pinned
        getRootPane().registerKeyboardAction(e -> {
            if (!pin.isSelected()) {
                setVisible(false);
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowDeactivated(WindowEvent e) {
                if (!pin.isSelected()) {
                    setVisible(false);
                }
            }
        });
        boolean pinned = false;
        try {
            pinned = mpv5.db.objects.User.getCurrentUser().getProperty("org.openyabs.uiproperty", PROPERTY);
        } catch (Exception e) {
            Log.Debug(this, e.getMessage());
        }
        setPinned(pinned, false);
        pack();
        setMinimumSize(getSize());
    }

    private static void movesBy(Container c, MouseAdapter drag, Component except) {
        if (c == except) {
            return;
        }
        c.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
        c.addMouseListener(drag);
        c.addMouseMotionListener(drag);
        for (Component x : c.getComponents()) {
            if (x instanceof Container && x != except) {
                movesBy((Container) x, drag, except);
            }
        }
    }

    /**
     * @param on keep on top and open
     * @param remember save it for the user
     */
    private void setPinned(boolean on, boolean remember) {
        pin.setSelected(on);
        setAlwaysOnTop(on);
        if (remember) {
            try {
                mpv5.db.objects.User.getCurrentUser().getProperties().changeProperty("org.openyabs.uiproperty", PROPERTY, on);
            } catch (Exception e) {
                Log.Debug(this, e.getMessage());
            }
        }
    }

    /**
     * Shows it; the first time at the top right of the main window, then
     * where it was moved to
     */
    public void open() {
        if (!placed) {
            Component owner = getOwner();
            if (owner != null && owner.isShowing()) {
                Rectangle r = owner.getBounds();
                setLocation(Math.max(r.x, r.x + r.width - getWidth() - ModernTheme.px(24)), r.y + ModernTheme.px(90));
            } else {
                setLocationRelativeTo(null);
            }
            placed = true;
        }
        setVisible(true);
        toFront();
    }
}
