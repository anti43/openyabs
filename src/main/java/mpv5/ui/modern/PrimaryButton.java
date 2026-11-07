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

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.BorderFactory;
import javax.swing.JButton;

/**
 * The highlighted main action of a view (e.g. Save): filled with the accent
 * color, white text
 */
public class PrimaryButton extends JButton {

    private static final long serialVersionUID = 1L;

    /**
     * @param text
     * @param icon resource path, may be null
     */
    public PrimaryButton(String text, String icon) {
        super(text);
        java.net.URL url = icon == null ? null : PrimaryButton.class.getResource(icon);
        if (url != null) {
            setIcon(new javax.swing.ImageIcon(url));
        }
        setFont(ModernTheme.font(1, Font.BOLD));
        setForeground(Color.WHITE);
        setFocusable(false);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setOpaque(false);
        setRolloverEnabled(true);
        setIconTextGap(ModernTheme.px(6));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setBorder(BorderFactory.createEmptyBorder(ModernTheme.px(7), ModernTheme.px(16), ModernTheme.px(7), ModernTheme.px(16)));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Color c = ModernTheme.accent();
        if (!isEnabled()) {
            c = ModernTheme.muted();
        } else if (getModel().isPressed()) {
            c = c.darker();
        } else if (getModel().isRollover()) {
            c = ModernTheme.blend(c, Color.WHITE, 0.12f);
        }
        g2.setColor(c);
        int arc = ModernTheme.px(10);
        g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);
        g2.dispose();
        super.paintComponent(g);
    }
}
