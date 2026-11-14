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
    public static JComponent hint(String text) {
        return plainHint(text.replaceAll("<br\\s*/?>", "\n").replaceAll("<[^>]+>", ""));
    }

    /**
     * Like {@link #hint(String)}, the text as it is (e.g. with "&lt;name&gt;")
     *
     * @param text plain text
     * @return the hint
     */
    public static JComponent plainHint(String text) {
        //a wrapping text: as wide as its place, as high as its lines
        javax.swing.JTextArea t = new javax.swing.JTextArea(text);
        t.setLineWrap(true);
        t.setWrapStyleWord(true);
        t.setEditable(false);
        t.setFocusable(false);
        t.setOpaque(false);
        t.setBorder(null);
        t.setFont(ModernTheme.font(-1, Font.PLAIN));
        t.setForeground(ModernTheme.muted());
        t.setColumns(10);
        return t;
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

    /**
     * A page with a tree (or list) card on the left and the details card on
     * the right
     *
     * @param treeTitle
     * @param tree the scroll pane of the tree
     * @param treeHint may be null
     * @param details
     * @return the page
     */
    public static JPanel treePage(String treeTitle, JComponent tree, String treeHint, Card details) {
        JPanel p = new JPanel(new BorderLayout(ModernTheme.px(12), 0));
        p.setBackground(ModernTheme.background());
        p.setBorder(ModernTheme.padding(12, 14));
        Card left = new Card(Card.clean(treeTitle));
        tree.setPreferredSize(new java.awt.Dimension(ModernTheme.px(260), ModernTheme.px(320)));
        left.addFull(tree, 1);
        if (treeHint != null && !treeHint.isEmpty()) {
            left.addFull(hint(treeHint));
        }
        left.setPreferredSize(new java.awt.Dimension(ModernTheme.px(300), ModernTheme.px(400)));
        p.add(left, BorderLayout.WEST);
        JPanel right = new JPanel(new BorderLayout());
        right.setOpaque(false);
        right.add(details, BorderLayout.NORTH);
        p.add(right, BorderLayout.CENTER);
        return p;
    }

    /**
     * A panel of a classic form as a card: its border (a title) goes, the
     * panels in it become transparent
     *
     * @param title the title of the card, null: the title of its border
     * @param panel
     * @return the card
     */
    public static Card wrap(String title, JComponent panel) {
        if (title == null && panel.getBorder() instanceof javax.swing.border.TitledBorder) {
            title = ((javax.swing.border.TitledBorder) panel.getBorder()).getTitle();
        }
        panel.setBorder(null);
        transparent(panel);
        return new Card(Card.clean(title)).addFull(panel);
    }

    private static void transparent(java.awt.Component c) {
        if (c instanceof JPanel) {
            ((JPanel) c).setOpaque(false);
            for (java.awt.Component k : ((JPanel) c).getComponents()) {
                transparent(k);
            }
        } else if (c instanceof javax.swing.JCheckBox || c instanceof javax.swing.JRadioButton) {
            ((JComponent) c).setOpaque(false);
        }
    }

    /**
     * @param root a classic form
     * @return a card for every titled panel of the form that is not inside
     * another titled panel, in the order of the form
     */
    public static Card[] cardsOf(java.awt.Container root) {
        java.util.List<JComponent> found = new java.util.ArrayList<JComponent>();
        collectTitled(root, found);
        Card[] cards = new Card[found.size()];
        for (int i = 0; i < cards.length; i++) {
            JComponent c = found.get(i);
            if (c.getParent() != null) {
                c.getParent().remove(c);
            }
            cards[i] = wrap(null, c);
        }
        return cards;
    }

    private static void collectTitled(java.awt.Container c, java.util.List<JComponent> found) {
        for (java.awt.Component k : c.getComponents()) {
            if (k instanceof JPanel && ((JPanel) k).getBorder() instanceof javax.swing.border.TitledBorder) {
                found.add((JComponent) k);
            } else if (k instanceof java.awt.Container) {
                collectTitled((java.awt.Container) k, found);
            }
        }
    }

    /**
     * @param bar the button bar of a classic form
     * @return its buttons, the one with the text of save as the main action
     */
    public static JComponent[] buttonsOf(java.awt.Container bar, String saveText) {
        java.util.List<JComponent> l = new java.util.ArrayList<JComponent>();
        for (java.awt.Component k : bar.getComponents()) {
            if (k instanceof JButton) {
                final JButton b = (JButton) k;
                if (saveText != null && saveText.equals(b.getText())) {
                    PrimaryButton p = new PrimaryButton(b.getText(), "/images/16/filesave.png");
                    p.addActionListener(e -> b.doClick());
                    l.add(p);
                } else {
                    l.add(b);
                }
            }
        }
        return l.toArray(new JComponent[0]);
    }
}
