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

import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JComponent;

/**
 * A circle with the initials of a name
 */
public class Avatar extends JComponent {

    private static final long serialVersionUID = 1L;
    private String initials = "";
    private java.awt.Image image;
    private boolean square;

    public Avatar() {
        int d = ModernTheme.px(48);
        setPreferredSize(new Dimension(d, d));
        setMinimumSize(new Dimension(d, d));
        setFont(ModernTheme.font(6, Font.BOLD));
    }

    /**
     * @param name e.g. "Smith, John" or "ACME Ltd."
     */
    public void setName(String name) {
        super.setName(name);
        initials = initials(name);
        repaint();
    }

    /**
     * @param image shown instead of the initials, null for the initials
     */
    public void setImage(java.awt.Image image) {
        this.image = image;
        repaint();
    }

    /**
     * @param square a rounded square instead of a circle (e.g. products)
     */
    public void setSquare(boolean square) {
        this.square = square;
        repaint();
    }

    /**
     * @param name
     * @return up to two upper case initials
     */
    static String initials(String name) {
        if (name == null) {
            return "";
        }
        StringBuilder b = new StringBuilder();
        for (String part : name.trim().split("[\\s,]+")) {
            if (!part.isEmpty() && Character.isLetterOrDigit(part.charAt(0))) {
                b.append(Character.toUpperCase(part.charAt(0)));
                if (b.length() == 2) {
                    break;
                }
            }
        }
        return b.toString();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        int d = Math.min(getWidth(), getHeight()) - 1;
        java.awt.Shape shape = square
                ? new java.awt.geom.RoundRectangle2D.Float(0, 0, d, d, d / 4f, d / 4f)
                : new java.awt.geom.Ellipse2D.Float(0, 0, d, d);
        if (image != null && image.getWidth(null) > 0) {
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.setColor(ModernTheme.cardBackground());
            g2.fill(shape);
            g2.clip(shape);
            //fit the whole image into the badge
            int w = image.getWidth(null);
            int h = image.getHeight(null);
            double f = Math.min((double) d / w, (double) d / h);
            int iw = (int) Math.round(w * f);
            int ih = (int) Math.round(h * f);
            g2.drawImage(image, (d - iw) / 2, (d - ih) / 2, iw, ih, null);
            g2.setClip(null);
            g2.setColor(ModernTheme.border());
            g2.draw(shape);
            g2.dispose();
            return;
        }
        g2.setColor(ModernTheme.accent());
        g2.fill(shape);
        g2.setColor(java.awt.Color.WHITE);
        g2.setFont(getFont());
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(initials, (d - fm.stringWidth(initials)) / 2, (d - fm.getHeight()) / 2 + fm.getAscent());
        g2.dispose();
    }
}
