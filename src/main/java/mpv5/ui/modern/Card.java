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
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * A rounded section with an optional title. Fields are added in rows with
 * {@link #row(Field...)}; each field shows its label above the input and the
 * fields of a row share the width by their weight.
 */
public class Card extends JPanel {

    private static final long serialVersionUID = 1L;
    private final JPanel body = new JPanel(new GridBagLayout());
    private final JPanel header = new JPanel(new BorderLayout());
    private int rows;

    /**
     * A field of a form row
     */
    public static final class Field {

        final String label;
        final Component component;
        final double weight;

        Field(String label, Component component, double weight) {
            this.label = label;
            this.component = component;
            this.weight = weight;
        }
    }

    /**
     * @param label shown above the component, may be null
     * @param component
     * @return a field with weight 1
     */
    public static Field field(String label, Component component) {
        return new Field(clean(label), component, 1);
    }

    /**
     * @return the label without the colon of classic forms ("Name:")
     */
    static String clean(String label) {
        return label == null ? null : label.replaceAll("\\s*:\\s*$", "");
    }

    /**
     * @param label shown above the component, may be null
     * @param component
     * @param weight share of the row width
     * @return the field
     */
    public static Field field(String label, Component component, double weight) {
        return new Field(clean(label), component, weight);
    }

    /**
     * @param title may be null
     */
    private final String title;

    /**
     * @return the title, null if none
     */
    public String getTitle() {
        return title;
    }

    /**
     * @return the title bar (for moving the card)
     */
    public JPanel getHeader() {
        return header;
    }

    public Card(String title) {
        super(new BorderLayout(0, ModernTheme.px(8)));
        this.title = title;
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(ModernTheme.px(12), ModernTheme.px(14), ModernTheme.px(14), ModernTheme.px(14)));
        header.setOpaque(false);
        if (title != null) {
            header.add(ModernTheme.heading(title), BorderLayout.WEST);
            add(header, BorderLayout.NORTH);
        }
        body.setOpaque(false);
        //takes the extra height (cards of a grid row get the same height), so the fields stay at the top
        GridBagConstraints g = new GridBagConstraints();
        g.gridy = FILLER_ROW;
        g.weighty = 0.0001;
        g.fill = GridBagConstraints.VERTICAL;
        JPanel filler = new JPanel();
        filler.setOpaque(false);
        filler.setPreferredSize(new java.awt.Dimension(0, 0));
        body.add(filler, g);
        add(body, BorderLayout.CENTER);
    }
    private static final int FILLER_ROW = 10000;

    private boolean flat;

    /**
     * A flat card has no background, border and padding: a form inside
     * another card
     *
     * @return this
     */
    public Card flat() {
        flat = true;
        setBorder(BorderFactory.createEmptyBorder());
        return this;
    }

    /**
     * Puts a component (e.g. a button) at the right of the title
     *
     * @param c
     * @return this
     */
    public Card headerAction(JComponent c) {
        header.add(c, BorderLayout.EAST);
        if (header.getParent() == null) {
            add(header, BorderLayout.NORTH);
        }
        return this;
    }

    /**
     * Adds a row of fields
     *
     * @param fields
     * @return this
     */
    public Card row(Field... fields) {
        JPanel row = new JPanel(new GridBagLayout());
        row.setOpaque(false);
        int gap = ModernTheme.px(10);
        for (int i = 0; i < fields.length; i++) {
            Field f = fields[i];
            GridBagConstraints c = new GridBagConstraints();
            c.gridx = i;
            c.weightx = f.weight;
            c.fill = GridBagConstraints.HORIZONTAL;
            c.anchor = GridBagConstraints.WEST;
            c.insets = new Insets(0, i == 0 ? 0 : gap, 0, 0);
            c.gridy = 0;
            JLabel l = ModernTheme.fieldLabel(f.label == null ? " " : f.label);
            row.add(l, c);
            c.gridy = 1;
            c.insets = new Insets(ModernTheme.px(2), i == 0 ? 0 : gap, 0, 0);
            row.add(f.component, c);
        }
        return addFull(row);
    }

    /**
     * Starts a sub-section of the card: a thin line and a small heading
     *
     * @param title
     * @return this
     */
    public Card section(String title) {
        JPanel p = new JPanel(new BorderLayout(0, ModernTheme.px(6)));
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(ModernTheme.px(6), 0, 0, 0));
        javax.swing.JSeparator line = new javax.swing.JSeparator();
        line.setForeground(ModernTheme.border());
        p.add(line, BorderLayout.NORTH);
        JLabel l = new JLabel(title);
        l.setFont(ModernTheme.font(0, java.awt.Font.BOLD));
        l.setForeground(ModernTheme.muted());
        p.add(l, BorderLayout.CENTER);
        return addFull(p);
    }

    /**
     * Adds a component over the full width
     *
     * @param c
     * @return this
     */
    public Card addFull(JComponent c) {
        return addFull(c, 0);
    }

    /**
     * Adds a component over the full width which takes extra height
     *
     * @param c
     * @param weighty
     * @return this
     */
    public Card addFull(JComponent c, double weighty) {
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0;
        g.gridy = rows++;
        g.weightx = 1;
        g.weighty = weighty;
        g.fill = weighty > 0 ? GridBagConstraints.BOTH : GridBagConstraints.HORIZONTAL;
        g.anchor = GridBagConstraints.NORTH;
        g.insets = new Insets(rows == 1 ? 0 : ModernTheme.px(8), 0, 0, 0);
        body.add(c, g);
        return this;
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (flat) {
            super.paintComponent(g);
            return;
        }
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int arc = ModernTheme.px(12);
        g2.setColor(ModernTheme.cardBackground());
        g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);
        g2.setColor(ModernTheme.border());
        g2.setStroke(new BasicStroke(1f));
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);
        g2.dispose();
        super.paintComponent(g);
    }
}
