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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.plaf.basic.BasicTabbedPaneUI;

/**
 * Flat tabs for the main window: one scrollable row, the selected tab on the
 * card color with an accent line, the others muted; no 3D borders.
 */
public class ModernTabbedPaneUI extends BasicTabbedPaneUI {

    private final int horizontalTextPosition;

    /**
     * @param horizontalTextPosition position of the title relative to the
     * icon (SwingConstants.LEFT: title left, close icon right)
     */
    public ModernTabbedPaneUI(int horizontalTextPosition) {
        this.horizontalTextPosition = horizontalTextPosition;
    }

    @Override
    protected void installDefaults() {
        super.installDefaults();
        tabInsets = new Insets(ModernTheme.px(6), ModernTheme.px(12), ModernTheme.px(6), ModernTheme.px(8));
        selectedTabPadInsets = new Insets(0, 0, 0, 0);
        tabAreaInsets = new Insets(ModernTheme.px(4), ModernTheme.px(6), 0, ModernTheme.px(6));
        contentBorderInsets = new Insets(1, 0, 0, 0);
        textIconGap = ModernTheme.px(8);
        tabPane.setFont(ModernTheme.font(0, Font.PLAIN));
        tabPane.setBackground(ModernTheme.background());
        tabPane.setOpaque(true);
    }

    @Override
    protected int calculateTabHeight(int tabPlacement, int tabIndex, int fontHeight) {
        return super.calculateTabHeight(tabPlacement, tabIndex, fontHeight) + ModernTheme.px(2);
    }

    /**
     * @return the color of the strip behind the tabs: a bit darker than the
     * view, so the tab row stands out from the bars above and below
     */
    static Color stripColor() {
        return ModernTheme.blend(ModernTheme.background(), ModernTheme.foreground(), ModernTheme.isDark() ? 0.12f : 0.07f);
    }

    /**
     * @return the color of tabs which are not selected
     */
    static Color tabColor() {
        return ModernTheme.blend(stripColor(), ModernTheme.cardBackground(), 0.55f);
    }

    @Override
    protected void paintTabArea(Graphics g, int tabPlacement, int selectedIndex) {
        g.setColor(stripColor());
        Rectangle clip = g.getClipBounds();
        if (clip != null) {
            g.fillRect(clip.x, clip.y, clip.width, clip.height);
        }
        super.paintTabArea(g, tabPlacement, selectedIndex);
    }

    @Override
    protected void paintTabBackground(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h, boolean isSelected) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int arc = ModernTheme.px(8);
        int gap = ModernTheme.px(2);
        if (isSelected) {
            g2.setColor(ModernTheme.cardBackground());
            //rounded at the top, open at the bottom (joins the content)
            g2.fillRoundRect(x + gap, y, w - 2 * gap, h + arc, arc, arc);
        } else {
            g2.setColor(tabIndex == getRolloverTab() ? ModernTheme.blend(tabColor(), ModernTheme.cardBackground(), 0.6f) : tabColor());
            g2.fillRoundRect(x + gap, y + gap, w - 2 * gap, h - 2 * gap, arc, arc);
        }
        g2.dispose();
    }

    @Override
    protected void paintTabBorder(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h, boolean isSelected) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int arc = ModernTheme.px(8);
        int gap = ModernTheme.px(2);
        if (isSelected) {
            //outline open at the bottom, accent line on top
            g2.setColor(ModernTheme.border());
            g2.setClip(x, y, w, h);
            g2.drawRoundRect(x + gap, y, w - 2 * gap - 1, h + arc, arc, arc);
            g2.setClip(null);
            g2.setColor(ModernTheme.accent());
            g2.setStroke(new BasicStroke(ModernTheme.px(3), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            int ly = y + ModernTheme.px(1);
            g2.drawLine(x + gap + ModernTheme.px(4), ly, x + w - gap - ModernTheme.px(4), ly);
        } else {
            g2.setColor(ModernTheme.blend(ModernTheme.border(), stripColor(), 0.2f));
            g2.drawRoundRect(x + gap, y + gap, w - 2 * gap - 1, h - 2 * gap - 1, arc, arc);
        }
        g2.dispose();
    }

    @Override
    protected void paintFocusIndicator(Graphics g, int tabPlacement, Rectangle[] rects, int tabIndex,
            Rectangle iconRect, Rectangle textRect, boolean isSelected) {
    }

    @Override
    protected void paintContentBorder(Graphics g, int tabPlacement, int selectedIndex) {
        int y = calculateTabAreaHeight(tabPlacement, runCount, maxTabHeight);
        Insets in = tabPane.getInsets();
        g.setColor(ModernTheme.border());
        g.drawLine(in.left, in.top + y, tabPane.getWidth() - in.right, in.top + y);
        //no line under the selected tab: it joins the content
        if (selectedIndex >= 0) {
            Rectangle r = getTabBounds(tabPane, selectedIndex);
            int gap = ModernTheme.px(2);
            g.setColor(ModernTheme.cardBackground());
            g.drawLine(r.x + gap + 1, in.top + y, r.x + r.width - gap - 2, in.top + y);
        }
    }

    @Override
    protected void paintText(Graphics g, int tabPlacement, Font font, FontMetrics metrics, int tabIndex,
            String title, Rectangle textRect, boolean isSelected) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        Font f = isSelected ? font.deriveFont(Font.BOLD) : font;
        g2.setFont(f);
        g2.setColor(isSelected ? ModernTheme.foreground() : ModernTheme.muted());
        FontMetrics m = g2.getFontMetrics(f);
        g2.drawString(title, textRect.x, textRect.y + m.getAscent());
    }

    @Override
    protected int getTabLabelShiftX(int tabPlacement, int tabIndex, boolean isSelected) {
        return 0;
    }

    @Override
    protected int getTabLabelShiftY(int tabPlacement, int tabIndex, boolean isSelected) {
        return 0;
    }

    @Override
    protected int calculateTabWidth(int tabPlacement, int tabIndex, FontMetrics metrics) {
        //room for the bold title of the selected tab
        return super.calculateTabWidth(tabPlacement, tabIndex, metrics) + ModernTheme.px(6);
    }

    @Override
    protected void layoutLabel(int tabPlacement, FontMetrics metrics, int tabIndex, String title, Icon icon,
            Rectangle tabRect, Rectangle iconRect, Rectangle textRect, boolean isSelected) {
        textRect.x = textRect.y = iconRect.x = iconRect.y = 0;
        SwingUtilities.layoutCompoundLabel((JComponent) tabPane, metrics, title, icon,
                SwingConstants.CENTER, SwingConstants.CENTER, SwingConstants.CENTER, horizontalTextPosition,
                tabRect, iconRect, textRect, textIconGap);
    }

    /**
     * A drawn close cross for the tabs: muted, on hover with a round
     * background
     */
    public static final class CloseIcon implements Icon {

        private final int state;

        /**
         * @param state 0 normal, 1 hover, 2 pressed
         */
        public CloseIcon(int state) {
            this.state = state;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int s = getIconWidth();
            if (state > 0) {
                g2.setColor(state == 2 ? ModernTheme.blend(ModernTheme.border(), ModernTheme.foreground(), 0.3f) : ModernTheme.border());
                g2.fillOval(x, y, s - 1, s - 1);
            }
            Color c2 = state > 0 ? ModernTheme.foreground() : ModernTheme.muted();
            g2.setColor(c2);
            g2.setStroke(new BasicStroke(Math.max(1.4f, ModernTheme.scale() * 1.4f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            int p = Math.round(s * 0.32f);
            g2.drawLine(x + p, y + p, x + s - 1 - p, y + s - 1 - p);
            g2.drawLine(x + s - 1 - p, y + p, x + p, y + s - 1 - p);
            g2.dispose();
        }

        @Override
        public int getIconWidth() {
            return 16;
        }

        @Override
        public int getIconHeight() {
            return 16;
        }
    }
}
