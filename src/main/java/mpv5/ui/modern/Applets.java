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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import mpv5.ui.beans.LabeledTextField;

/**
 * Building blocks of the modern settings panels (control applets)
 */
public final class Applets {

    public static final Color DANGER = new Color(185, 28, 28);

    private Applets() {
    }

    /**
     * @param text
     * @return a gray, wrapping explanation
     */
    public static JLabel hint(String text) {
        JLabel l = new JLabel("<html>" + text.replace("\n", "<br>") + "</html>");
        l.setFont(ModernTheme.font(-1, Font.PLAIN));
        l.setForeground(ModernTheme.muted());
        return l;
    }

    /**
     * @param b
     * @return the button in red (for destructive actions)
     */
    public static JButton danger(JButton b) {
        b.setForeground(DANGER);
        return b;
    }

    /**
     * The bar under a settings panel: destructive actions on the left, reset
     * and save on the right
     *
     * @param left
     * @param right
     * @return the bar
     */
    public static JPanel actionBar(JComponent[] left, JComponent[] right) {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setOpaque(false);
        JPanel l = new JPanel(new FlowLayout(FlowLayout.LEFT, ModernTheme.px(8), ModernTheme.px(6)));
        l.setOpaque(false);
        for (JComponent c : left) {
            l.add(c);
        }
        JPanel r = new JPanel(new FlowLayout(FlowLayout.RIGHT, ModernTheme.px(8), ModernTheme.px(6)));
        r.setOpaque(false);
        for (JComponent c : right) {
            r.add(c);
        }
        bar.add(l, BorderLayout.WEST);
        bar.add(r, BorderLayout.EAST);
        return bar;
    }

    /**
     * @param field
     * @return a card field with the label of the classic field
     */
    public static Card.Field field(LabeledTextField field) {
        String label = field.get_Label();
        return Card.field(label, ModernTheme.plain(field));
    }

    /**
     * @param field
     * @param weight
     * @return a card field with the label of the classic field
     */
    public static Card.Field field(LabeledTextField field, double weight) {
        String label = field.get_Label();
        return Card.field(label, ModernTheme.plain(field), weight);
    }

    /**
     * The page of a settings panel: a grid of cards, optionally with a hint
     * above
     *
     * @param hint may be null
     * @param minWidth the minimal width of a card column
     * @param cards
     * @return the page
     */
    public static JPanel page(String hint, int minWidth, Card... cards) {
        JPanel p = new JPanel(new BorderLayout(0, ModernTheme.px(10)));
        p.setBackground(ModernTheme.background());
        p.setBorder(ModernTheme.padding(12, 14));
        if (hint != null && !hint.isEmpty()) {
            p.add(hint(hint), BorderLayout.NORTH);
        }
        ResponsiveGrid grid = new ResponsiveGrid(ModernTheme.px(minWidth), 3);
        grid.setOpaque(false);
        for (Card c : cards) {
            grid.add(c);
        }
        p.add(grid, BorderLayout.CENTER);
        return p;
    }
}
