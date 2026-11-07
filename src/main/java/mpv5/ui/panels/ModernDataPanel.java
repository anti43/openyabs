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
package mpv5.ui.panels;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.LayoutManager;
import java.awt.Rectangle;
import java.util.regex.Pattern;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JViewport;
import javax.swing.RowFilter;
import javax.swing.Scrollable;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import mpv5.i18n.LanguageManager;
import mpv5.ui.beans.LabeledTextField;
import mpv5.ui.misc.MPTable;
import mpv5.ui.modern.ModernTheme;

/**
 * Common parts of the modern views ({@link ModernContactPanel},
 * {@link ModernProductPanel} ...): filling the visible area of the main
 * window's scroll pane, the split between data and documents, the live
 * filter, tables with tooltips and the muted details column.
 */
public abstract class ModernDataPanel extends JPanel implements DataPanel, Scrollable {

    private static final long serialVersionUID = 1L;
    /*
     * The main window puts every tab into a scroll pane. The panel fills the
     * visible area (the cards and the documents scroll by themselves) and
     * only scrolls as a whole if the window is smaller than this.
     */
    private static final int MIN_WIDTH = 560;
    private static final int MIN_HEIGHT = 520;

    protected ModernDataPanel(LayoutManager layout) {
        super(layout);
        setBackground(ModernTheme.background());
    }

    // <editor-fold defaultstate="collapsed" desc="Helpers">
    /**
     * @param key
     * @return the text of the language bundle, the key if missing
     */
    protected static String text(String key) {
        try {
            return LanguageManager.getBundle().getString(key);
        } catch (Exception e) {
            return key;
        }
    }

    /**
     * @return a classic labeled field prepared for a card
     */
    protected static LabeledTextField field() {
        return ModernTheme.plain(new LabeledTextField());
    }

    /**
     * @param label
     * @return a button which does not take the focus
     */
    protected static JButton action(String label) {
        JButton b = new JButton(label);
        b.setFocusable(false);
        return b;
    }

    /**
     * @param label may be null
     * @param icon resource path, may be null
     * @return the button
     */
    protected static JButton link(String label, String icon) {
        JButton b = new JButton(label);
        java.net.URL url = icon == null ? null : ModernDataPanel.class.getResource(icon);
        if (url != null) {
            b.setIcon(new javax.swing.ImageIcon(url));
        }
        b.setFocusable(false);
        return b;
    }

    /**
     * @param c
     * @return c in a borderless scroll pane with the view background
     */
    protected static JScrollPane scroll(JComponent c) {
        JScrollPane s = new JScrollPane(c);
        s.setBorder(BorderFactory.createEmptyBorder());
        //the view background instead of the content color of the tabbed pane
        s.getViewport().setOpaque(true);
        s.getViewport().setBackground(ModernTheme.background());
        s.getVerticalScrollBar().setUnitIncrement(ModernTheme.px(16));
        return s;
    }

    /**
     * @param c
     * @return c with some space around, on the view background
     */
    protected static JComponent padded(JComponent c) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(ModernTheme.background());
        p.setBorder(ModernTheme.padding(12, 12));
        p.add(c, BorderLayout.CENTER);
        return p;
    }

    /**
     * @param top the data
     * @param bottom the documents
     * @return a split pane which starts with 60% for the data
     */
    protected static JSplitPane split(JComponent top, JComponent bottom) {
        final JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, top, bottom);
        split.setResizeWeight(0.62);
        split.setBorder(BorderFactory.createEmptyBorder(0, ModernTheme.px(8), ModernTheme.px(8), ModernTheme.px(8)));
        split.setOpaque(false);
        split.setContinuousLayout(true);
        split.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                if (split.getHeight() > 0) {
                    split.removeComponentListener(this);
                    split.setDividerLocation(0.6);
                }
            }
        });
        return split;
    }

    /**
     * @param identifier the view (for the stored column layout)
     * @return a table which shows the content of a cell as tooltip
     */
    protected static MPTable tooltipTable(JComponent identifier) {
        MPTable t = new MPTable(identifier) {
            private static final long serialVersionUID = 1L;

            @Override
            public Component prepareRenderer(javax.swing.table.TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                if (c instanceof JComponent) {
                    Object v = getValueAt(row, column);
                    ((JComponent) c).setToolTipText(v == null || String.valueOf(v).isEmpty() ? null : String.valueOf(v));
                }
                return c;
            }
        };
        ModernTheme.style(t);
        return t;
    }

    /**
     * Gives the last column of the table the space and the muted look of a
     * details column
     *
     * @param table
     */
    protected static void detailsColumn(JTable table) {
        int details = table.getColumnModel().getColumnCount() - 1;
        if (details <= 0) {
            return;
        }
        for (int i = 0; i < details; i++) {
            if (table.getColumnModel().getColumn(i).getMaxWidth() > 0) {
                table.getColumnModel().getColumn(i).setPreferredWidth(ModernTheme.px(100));
            }
        }
        table.getColumnModel().getColumn(details).setPreferredWidth(ModernTheme.px(900));
        table.getColumnModel().getColumn(details).setCellRenderer(new DetailsRenderer());
    }

    /**
     * Selects the entry with the given id (ids from the database may be Long
     * or Integer, so they are compared as text)
     *
     * @param box
     * @param id
     * @return true if found
     */
    protected static boolean selectById(mpv5.ui.beans.LabeledCombobox box, Object id) {
        javax.swing.JComboBox c = box.getComboBox();
        for (int i = 0; i < c.getItemCount(); i++) {
            Object o = c.getItemAt(i);
            if (o instanceof mpv5.utils.models.MPComboBoxModelItem
                    && String.valueOf(((mpv5.utils.models.MPComboBoxModelItem) o).getId()).equals(String.valueOf(id))) {
                c.setSelectedIndex(i);
                return true;
            }
        }
        return false;
    }

    /**
     * Shows a number column with two decimals, right aligned
     *
     * @param table
     * @param column view index
     */
    protected static void amountColumn(JTable table, int column) {
        if (column < 0 || column >= table.getColumnModel().getColumnCount()) {
            return;
        }
        table.getColumnModel().getColumn(column).setCellRenderer(new javax.swing.table.DefaultTableCellRenderer() {
            private static final long serialVersionUID = 1L;

            @Override
            protected void setValue(Object value) {
                setHorizontalAlignment(RIGHT);
                if (value instanceof Number) {
                    super.setValue(mpv5.utils.numberformat.FormatNumber.formatDezimal(
                            java.math.BigDecimal.valueOf(((Number) value).doubleValue())));
                } else {
                    super.setValue(value);
                }
            }
        });
    }

    /**
     * Filters the table with the text of the field while typing (case
     * insensitive, plain text); call {@link #resetFilter} after a new model
     *
     * @param table
     * @param filter
     */
    protected static void liveFilter(final JTable table, final JTextField filter) {
        ModernTheme.roomy(filter);
        filter.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                applyFilter(table, filter);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                applyFilter(table, filter);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                applyFilter(table, filter);
            }
        });
    }

    /**
     * A new sorter for the current model of the table, filtered with the
     * field
     *
     * @param table
     * @param filter
     */
    protected static void resetFilter(JTable table, JTextField filter) {
        table.setRowSorter(new TableRowSorter<TableModel>(table.getModel()));
        applyFilter(table, filter);
    }

    @SuppressWarnings("unchecked")
    private static void applyFilter(JTable table, JTextField filter) {
        if (!(table.getRowSorter() instanceof TableRowSorter)) {
            return;
        }
        TableRowSorter<TableModel> sorter = (TableRowSorter<TableModel>) table.getRowSorter();
        String t = filter.getText();
        sorter.setRowFilter(t == null || t.trim().isEmpty()
                ? null : RowFilter.<TableModel, Integer>regexFilter("(?i)" + Pattern.quote(t.trim())));
    }

    /**
     * Shows the title in the tab of the main window (or the window title)
     *
     * @param title
     */
    protected void setTabTitle(String title) {
        if (title == null || !(getParent() instanceof JViewport || getParent() instanceof JTabbedPane)) {
            return;
        }
        JTabbedPane tabs = null;
        //this->viewport->scrollpane->tabbedpane
        if (getParent().getParent() != null && getParent().getParent().getParent() instanceof JTabbedPane) {
            tabs = (JTabbedPane) getParent().getParent().getParent();
        } else if (getParent() instanceof JTabbedPane) {
            tabs = (JTabbedPane) getParent();
        } else if (getRootPane() != null && getRootPane().getParent() instanceof JFrame) {
            ((JFrame) getRootPane().getParent()).setTitle(title);
        }
        if (tabs != null && tabs.getSelectedIndex() >= 0) {
            tabs.setTitleAt(tabs.getSelectedIndex(), title);
        }
    }

    /**
     * The details column: muted, with some space to the column before
     */
    protected static final class DetailsRenderer extends javax.swing.table.DefaultTableCellRenderer {

        private static final long serialVersionUID = 1L;

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setBorder(BorderFactory.createEmptyBorder(0, ModernTheme.px(12), 0, ModernTheme.px(4)));
            if (!isSelected) {
                c.setForeground(ModernTheme.blend(table.getForeground(), table.getBackground(), 0.3f));
            }
            return c;
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Scrollable">
    @Override
    public Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();
    }

    @Override
    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
        return ModernTheme.px(16);
    }

    @Override
    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
        return ModernTheme.px(160);
    }

    @Override
    public boolean getScrollableTracksViewportWidth() {
        return getParent() instanceof JViewport && getParent().getWidth() >= ModernTheme.px(MIN_WIDTH);
    }

    @Override
    public boolean getScrollableTracksViewportHeight() {
        return getParent() instanceof JViewport && getParent().getHeight() >= ModernTheme.px(MIN_HEIGHT);
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        //when the window is too small: scroll, but not over the whole preferred size of the cards
        return new Dimension(Math.min(d.width, ModernTheme.px(MIN_WIDTH)), Math.min(d.height, ModernTheme.px(MIN_HEIGHT)));
    }
    // </editor-fold>
}
