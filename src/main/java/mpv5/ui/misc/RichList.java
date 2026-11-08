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
package mpv5.ui.misc;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;
import mpv5.db.common.Context;
import mpv5.globals.Messages;
import mpv5.ui.modern.ModernTheme;

/**
 * Lists of records in the modern style: a round badge, a title, a gray line
 * of details and a value on the right; plain strings are group headings
 */
public final class RichList {

    public static final Color GREEN = new Color(0x2E, 0x7D, 0x32);
    public static final Color RED = new Color(0xC6, 0x28, 0x28);
    public static final Color ORANGE = new Color(0xE6, 0x7E, 0x00);
    public static final Color PURPLE = new Color(0x6A, 0x1B, 0x9A);

    private RichList() {
    }

    /**
     * One line of a list
     */
    public static class Row {

        public String badge = "";
        public Color color = ModernTheme.muted();
        public String title = "";
        public String detail = "";
        public String right = "";
        /**
         * bold title (something that can be opened)
         */
        public boolean strong;
        /**
         * what the row stands for
         */
        public Object data;

        @Override
        public String toString() {
            return title;
        }
    }

    /**
     * @param c
     * @return the readable name of the kind of records (Contacts, Documents
     * ...)
     */
    public static String typeName(Context c) {
        if (c == null) {
            return "";
        }
        String id = c.getDbIdentity();
        if (id.equalsIgnoreCase(Context.getContact().getDbIdentity())) {
            return Messages.SEARCH_KIND_CONTACTS.toString();
        } else if (id.equalsIgnoreCase(Context.getInvoice().getDbIdentity())) {
            return Messages.SEARCH_KIND_DOCUMENTS.toString();
        } else if (id.equalsIgnoreCase(Context.getProduct().getDbIdentity())) {
            return Messages.SEARCH_KIND_PRODUCTS.toString();
        } else if (id.equalsIgnoreCase(Context.getSubItem().getDbIdentity())) {
            return Messages.SEARCH_KIND_POSITIONS.toString();
        } else if (id.equalsIgnoreCase(Context.getConversation().getDbIdentity())) {
            return Messages.SEARCH_KIND_CONVERSATIONS.toString();
        } else if (id.equalsIgnoreCase(Context.getSchedule().getDbIdentity())) {
            return Messages.SEARCH_KIND_SCHEDULE.toString();
        }
        return id.isEmpty() ? "" : Character.toUpperCase(id.charAt(0)) + id.substring(1);
    }

    /**
     * @param c
     * @return a short badge text and its color for a kind of records
     */
    public static Object[] typeBadge(Context c) {
        String id = c == null ? "" : c.getDbIdentity();
        if (id.equalsIgnoreCase(Context.getContact().getDbIdentity())) {
            return new Object[]{"K", ModernTheme.accent()};
        } else if (id.equalsIgnoreCase(Context.getInvoice().getDbIdentity())) {
            return new Object[]{"B", GREEN};
        } else if (id.equalsIgnoreCase(Context.getProduct().getDbIdentity())) {
            return new Object[]{"P", ORANGE};
        }
        return new Object[]{id.isEmpty() ? "?" : id.substring(0, 1).toUpperCase(), PURPLE};
    }

    /**
     * A round badge with a short text
     */
    public static final class Badge implements Icon {

        private final String text;
        private final Color color;
        private final int size;

        public Badge(String text, Color color, int size) {
            this.text = text == null ? "" : text;
            this.color = color;
            this.size = size;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setColor(color);
            g2.fillRoundRect(x, y, size, size, size / 2, size / 2);
            g2.setColor(Color.WHITE);
            float fs = text.length() > 2 ? size * 0.34f : text.length() == 2 ? size * 0.42f : size * 0.6f;
            g2.setFont(c.getFont().deriveFont(Font.BOLD, Math.max(7f, fs)));
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(text, x + (size - fm.stringWidth(text)) / 2f, y + (size - fm.getHeight()) / 2f + fm.getAscent());
            g2.dispose();
        }

        @Override
        public int getIconWidth() {
            return size;
        }

        @Override
        public int getIconHeight() {
            return size;
        }
    }

    /**
     * Renders {@link Row}s and headings (strings)
     */
    public static final class Renderer implements ListCellRenderer<Object> {

        private final JPanel row = new JPanel(new BorderLayout(ModernTheme.px(10), 0));
        private final JLabel badge = new JLabel();
        private final JLabel title = new JLabel();
        private final JLabel detail = new JLabel();
        private final JLabel right = new JLabel();
        private final JLabel heading = new JLabel();

        public Renderer() {
            JPanel texts = new JPanel(new BorderLayout(0, ModernTheme.px(1)));
            texts.setOpaque(false);
            texts.add(title, BorderLayout.NORTH);
            texts.add(detail, BorderLayout.SOUTH);
            row.add(badge, BorderLayout.WEST);
            row.add(texts, BorderLayout.CENTER);
            row.add(right, BorderLayout.EAST);
            row.setBorder(ModernTheme.padding(6, 10));
            right.setVerticalAlignment(SwingConstants.TOP);
            heading.setOpaque(false);
        }

        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean selected, boolean focus) {
            if (!(value instanceof Row)) {
                heading.setText(String.valueOf(value).toUpperCase());
                heading.setFont(ModernTheme.font(-2, Font.BOLD));
                heading.setForeground(ModernTheme.muted());
                heading.setBorder(BorderFactory.createEmptyBorder(index == 0 ? ModernTheme.px(4) : ModernTheme.px(14), ModernTheme.px(10), ModernTheme.px(4), 0));
                return heading;
            }
            Row r = (Row) value;
            badge.setIcon(new Badge(r.badge, r.color, ModernTheme.px(28)));
            title.setText(r.title);
            title.setFont(ModernTheme.font(0, r.strong ? Font.BOLD : Font.PLAIN));
            detail.setText(r.detail == null || r.detail.isEmpty() ? " " : r.detail);
            detail.setFont(ModernTheme.font(-1, Font.PLAIN));
            right.setText(r.right);
            right.setFont(ModernTheme.font(-1, Font.PLAIN));
            row.setOpaque(true);
            row.setBackground(selected ? ModernTheme.accent() : list.getBackground());
            title.setForeground(selected ? Color.WHITE : ModernTheme.foreground());
            Color soft = selected ? new Color(225, 232, 250) : ModernTheme.muted();
            detail.setForeground(soft);
            right.setForeground(soft);
            return row;
        }
    }
}
