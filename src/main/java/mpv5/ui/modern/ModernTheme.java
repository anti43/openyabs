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
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.border.Border;
import mpv5.ui.beans.LabeledCombobox;
import mpv5.ui.beans.LabeledTextField;

/**
 * Colors, fonts and sizes of the modern views.
 * <p>
 * Everything is derived from the current look and feel (so dark themes work)
 * and from the default font size (so the views scale with HiDPI / larger
 * fonts) - no fixed pixel sizes.
 */
public final class ModernTheme {

    private ModernTheme() {
    }

    /**
     * @return the default font of labels
     */
    public static Font baseFont() {
        Font f = UIManager.getFont("Label.font");
        return f != null ? f : new Font(Font.SANS_SERIF, Font.PLAIN, 12);
    }

    /**
     * @return the ratio of the default font size to 12pt, used to scale gaps
     */
    public static float scale() {
        return Math.max(1f, baseFont().getSize2D() / 12f);
    }

    /**
     * @param px size at 12pt default font
     * @return the size scaled to the current font
     */
    public static int px(int px) {
        return Math.round(px * scale());
    }

    /**
     * @return the background of the view (behind the cards)
     */
    public static Color background() {
        Color c = UIManager.getColor("Panel.background");
        return c != null ? c : new Color(238, 238, 238);
    }

    /**
     * @return true if the look and feel has a dark background
     */
    public static boolean isDark() {
        Color c = background();
        return (c.getRed() * 299 + c.getGreen() * 587 + c.getBlue() * 114) / 1000 < 128;
    }

    /**
     * @return the background of cards: a bit lighter (darker on dark themes)
     * than the view
     */
    public static Color cardBackground() {
        Color c = UIManager.getColor("TextField.background");
        if (c == null || c.equals(background())) {
            c = isDark() ? background().brighter() : Color.WHITE;
        }
        return new Color(c.getRGB());
    }

    /**
     * @return the color of card borders and separators
     */
    public static Color border() {
        return blend(background(), foreground(), isDark() ? 0.25f : 0.15f);
    }

    /**
     * @return the text color
     */
    public static Color foreground() {
        Color c = UIManager.getColor("Label.foreground");
        return c != null ? new Color(c.getRGB()) : Color.BLACK;
    }

    /**
     * @return the color of secondary text (field labels, hints)
     */
    public static Color muted() {
        return blend(foreground(), cardBackground(), 0.45f);
    }

    /**
     * @return the accent color (selected chips, avatar)
     */
    public static Color accent() {
        return isDark() ? new Color(90, 150, 255) : new Color(37, 99, 235);
    }

    /**
     * @return a light variant of the accent color, e.g. for hover
     */
    public static Color accentSoft() {
        return blend(accent(), cardBackground(), 0.85f);
    }

    /**
     * @param a
     * @param b
     * @param ratio 0 = a, 1 = b
     * @return the mixed color
     */
    public static Color blend(Color a, Color b, float ratio) {
        float r = Math.max(0f, Math.min(1f, ratio));
        return new Color(
                Math.round(a.getRed() * (1 - r) + b.getRed() * r),
                Math.round(a.getGreen() * (1 - r) + b.getGreen() * r),
                Math.round(a.getBlue() * (1 - r) + b.getBlue() * r));
    }

    /**
     * @param size points added to the default size
     * @param style Font.PLAIN, Font.BOLD ...
     * @return a font relative to the default font
     */
    public static Font font(float size, int style) {
        Font f = baseFont();
        return f.deriveFont(style, f.getSize2D() + size);
    }

    /**
     * @param vertical at 12pt
     * @param horizontal at 12pt
     * @return an empty border scaled to the font
     */
    public static Border padding(int vertical, int horizontal) {
        return BorderFactory.createEmptyBorder(px(vertical), px(horizontal), px(vertical), px(horizontal));
    }

    /**
     * @param text
     * @return a small, muted label as used above fields
     */
    public static JLabel fieldLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(font(-1, Font.PLAIN));
        l.setForeground(muted());
        return l;
    }

    /**
     * @param text
     * @return a bold heading (e.g. the title of a card)
     */
    public static JLabel heading(String text) {
        JLabel l = new JLabel(text);
        l.setFont(font(1, Font.BOLD));
        l.setForeground(foreground());
        return l;
    }

    /**
     * Prepares a classic labeled field for a modern form: the label is shown
     * above the field by the form, so the built-in label is hidden and the
     * fixed sizes are removed.
     *
     * @param field
     * @return the field
     */
    public static LabeledTextField plain(LabeledTextField field) {
        field.getLabelField().setVisible(false);
        field.getTextField().setPreferredSize(null);
        field.setPreferredSize(null);
        field.setMinimumSize(null);
        field.setMaximumSize(null);
        field.getTextField().setColumns(8);
        roomy(field.getTextField());
        return field;
    }

    /**
     * Makes a button a well sized action button (e.g. "New invoice"): larger
     * font and padding, optional icon
     *
     * @param b
     * @param icon resource path, may be null
     * @return b
     */
    public static <T extends javax.swing.AbstractButton> T actionButton(T b, String icon) {
        b.setFont(font(1, Font.PLAIN));
        b.setMargin(new java.awt.Insets(px(6), px(14), px(6), px(14)));
        b.setIconTextGap(px(6));
        java.net.URL url = icon == null ? null : ModernTheme.class.getResource(icon);
        if (url != null) {
            b.setIcon(new javax.swing.ImageIcon(url));
        }
        b.setFocusable(false);
        return b;
    }

    /**
     * Gives a text component some inner space
     *
     * @param t
     * @return t
     */
    public static <T extends javax.swing.text.JTextComponent> T roomy(T t) {
        t.setMargin(new java.awt.Insets(px(3), px(5), px(3), px(5)));
        return t;
    }

    /**
     * Same as {@link #plain(LabeledTextField)} for combo boxes
     *
     * @param field
     * @return the field
     */
    public static LabeledCombobox plain(LabeledCombobox field) {
        hideLabels(field);
        field.setPreferredSize(null);
        field.setMinimumSize(null);
        field.setMaximumSize(null);
        return field;
    }

    /**
     * Same as {@link #plain(LabeledTextField)} for date choosers
     *
     * @param field
     * @return the field
     */
    public static mpv5.ui.beans.LabeledDateChooser plain(mpv5.ui.beans.LabeledDateChooser field) {
        hideLabels(field);
        field.setPreferredSize(null);
        field.setMinimumSize(null);
        field.setMaximumSize(null);
        return field;
    }

    private static void hideLabels(Container c) {
        for (Component child : c.getComponents()) {
            if (child instanceof JLabel) {
                child.setVisible(false);
            }
        }
    }

    /**
     * Gives a table more air: row height scaled to the font, no vertical grid
     * lines
     *
     * @param table
     */
    public static void style(JTable table) {
        table.setRowHeight(Math.max(table.getRowHeight(), table.getFontMetrics(table.getFont()).getHeight() + px(8)));
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(blend(border(), cardBackground(), 0.4f));
        table.setIntercellSpacing(new java.awt.Dimension(0, 1));
        table.setFillsViewportHeight(true);
        if (table.getTableHeader() != null) {
            table.getTableHeader().setFont(font(-1, Font.BOLD));
        }
    }

    /**
     * Makes a component and its children transparent where they are plain
     * panels, so the card background shows
     *
     * @param c
     */
    public static void transparent(JComponent c) {
        c.setOpaque(false);
    }

    /**
     * Tab and Shift+Tab move the focus on instead of moving inside the
     * component (tables, text areas); Ctrl+Tab too
     *
     * @param <T>
     * @param c
     * @return c
     */
    public static <T extends javax.swing.JComponent> T tabLeaves(T c) {
        java.util.Set<java.awt.AWTKeyStroke> fw = new java.util.HashSet<>();
        fw.add(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_TAB, 0));
        fw.add(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_TAB, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        java.util.Set<java.awt.AWTKeyStroke> bw = new java.util.HashSet<>();
        bw.add(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_TAB, java.awt.event.InputEvent.SHIFT_DOWN_MASK));
        bw.add(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_TAB, java.awt.event.InputEvent.SHIFT_DOWN_MASK | java.awt.event.InputEvent.CTRL_DOWN_MASK));
        c.setFocusTraversalKeys(java.awt.KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, fw);
        c.setFocusTraversalKeys(java.awt.KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, bw);
        return c;
    }
}
