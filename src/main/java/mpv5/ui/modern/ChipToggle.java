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

import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.BorderFactory;
import javax.swing.JToggleButton;

/**
 * A pill shaped toggle button, used for the roles of a contact (customer,
 * supplier ...) and as segmented control (offers, orders, invoices ...)
 */
public class ChipToggle extends JToggleButton {

    private static final long serialVersionUID = 1L;

    /**
     * @param text
     */
    public ChipToggle(String text) {
        super(text);
        setFont(ModernTheme.font(0, Font.PLAIN));
        setFocusPainted(false);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setOpaque(false);
        setRolloverEnabled(true);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setBorder(BorderFactory.createEmptyBorder(ModernTheme.px(4), ModernTheme.px(12), ModernTheme.px(4), ModernTheme.px(12)));
        addChangeListener(e -> setForeground(isSelected() ? java.awt.Color.WHITE : ModernTheme.foreground()));
        setForeground(ModernTheme.foreground());
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int h = getHeight() - 1;
        if (isSelected()) {
            g2.setColor(isEnabled() ? ModernTheme.accent() : ModernTheme.muted());
            g2.fillRoundRect(0, 0, getWidth() - 1, h, h, h);
        } else {
            g2.setColor(getModel().isRollover() ? ModernTheme.accentSoft() : ModernTheme.cardBackground());
            g2.fillRoundRect(0, 0, getWidth() - 1, h, h, h);
            g2.setColor(ModernTheme.border());
            g2.drawRoundRect(0, 0, getWidth() - 1, h, h, h);
        }
        g2.dispose();
        super.paintComponent(g);
    }
}
