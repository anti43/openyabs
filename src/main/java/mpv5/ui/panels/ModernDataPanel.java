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
     * The explanation above a properties table: what the own fields are and
     * how to use them in templates
     *
     * @param prefix the template prefix of the record, e.g. "item"
     * @param extra more text (e.g. the field in documents of a contact), may be empty
     * @return a wrapping, muted label
     */
    protected static JComponent propertiesHint(String prefix, String extra) {
        String t = String.format(mpv5.globals.Messages.PROPERTIES_HINT.toString(), prefix,
                extra == null || extra.trim().isEmpty() ? "" : " " + extra.trim());
        javax.swing.JTextArea a = new javax.swing.JTextArea(t);
        a.setEditable(false);
        a.setFocusable(false);
        a.setLineWrap(true);
        a.setWrapStyleWord(true);
        a.setOpaque(false);
        a.setBorder(javax.swing.BorderFactory.createEmptyBorder());
        a.setFont(ModernTheme.baseFont());
        a.setForeground(ModernTheme.muted());
        return a;
    }

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
        return split(top, bottom, 0.6);
    }

    /**
     * @param top
     * @param bottom
     * @param ratio the part of the height the top gets initially
     * @return the split pane
     */
    protected static JSplitPane split(JComponent top, JComponent bottom, final double ratio) {
        return split(null, top, bottom, ratio);
    }

    /**
     * @param key the view (e.g. "contact"): the position of the divider is
     * saved for the user and used again; null: not saved
     * @param top
     * @param bottom
     * @param ratio the part of the height the top gets the first time
     * @return the split pane
     */
    protected static JSplitPane split(final String key, JComponent top, JComponent bottom, final double ratio) {
        final String prop = "org.openyabs.layout.split." + key;
        double start = ratio;
        if (key != null) {
            try {
                String s = mpv5.db.objects.User.getCurrentUser().getProperties().getProperty(prop);
                if (s != null) {
                    double d = Double.parseDouble(s);
                    if (d >= 0.05 && d <= 0.95) {
                        start = d;
                    }
                }
            } catch (Exception e) {
                //default
            }
        }
        final double first = start;
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
                    split.setDividerLocation(first);
                    if (key != null) {
                        //from now on: moving the divider is remembered
                        split.addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY, ev -> {
                            int h = split.getHeight() - split.getDividerSize();
                            if (h > 0 && split.isShowing()) {
                                double r = (double) split.getDividerLocation() / h;
                                if (r >= 0.05 && r <= 0.95) {
                                    mpv5.db.objects.User.getCurrentUser().getProperties().changeProperty(prop, String.format(java.util.Locale.ROOT, "%.3f", r));
                                }
                            }
                        });
                    }
                }
            }
        });
        return split;
    }

    /**
     * @param left
     * @param right
     * @param ratio the part of the width the left side gets initially
     * @return the horizontal split pane (left/right)
     */
    protected static JSplitPane horizontalSplit(JComponent left, JComponent right, final double ratio) {
        return horizontalSplit(null, left, right, ratio);
    }

    /**
     * @param key the view (e.g. "expense"): the position of the divider is
     * saved for the user and used again; null: not saved
     * @param left
     * @param right
     * @param ratio the part of the width the left side gets the first time
     * @return the horizontal split pane (left/right)
     */
    protected static JSplitPane horizontalSplit(final String key, JComponent left, JComponent right, final double ratio) {
        final String prop = "org.openyabs.layout.hsplit." + key;
        double start = ratio;
        if (key != null) {
            try {
                String s = mpv5.db.objects.User.getCurrentUser().getProperties().getProperty(prop);
                if (s != null) {
                    double d = Double.parseDouble(s);
                    if (d >= 0.05 && d <= 0.95) {
                        start = d;
                    }
                }
            } catch (Exception e) {
                //default
            }
        }
        final double first = start;
        final JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, right);
        split.setResizeWeight(ratio);
        split.setBorder(BorderFactory.createEmptyBorder(0, ModernTheme.px(8), ModernTheme.px(8), ModernTheme.px(8)));
        split.setOpaque(false);
        split.setContinuousLayout(true);
        split.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                if (split.getWidth() > 0) {
                    split.removeComponentListener(this);
                    split.setDividerLocation(first);
                    if (key != null) {
                        split.addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY, ev -> {
                            int w = split.getWidth() - split.getDividerSize();
                            if (w > 0 && split.isShowing()) {
                                double r = (double) split.getDividerLocation() / w;
                                if (r >= 0.05 && r <= 0.95) {
                                    mpv5.db.objects.User.getCurrentUser().getProperties().changeProperty(prop, String.format(java.util.Locale.ROOT, "%.3f", r));
                                }
                            }
                        });
                    }
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
        //keyboard: Tab leaves the table
        ModernTheme.tabLeaves(t);
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
                    //formatDezimal rounds exactly (BigDecimal as is, others via their decimal string)
                    super.setValue(mpv5.utils.numberformat.FormatNumber.formatDezimal((Number) value));
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
        filter.putClientProperty(mpv5.ui.misc.Autosave.IGNORE_PROPERTY, Boolean.TRUE);
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
        if (title == null) {
            return;
        }
        //the tab which holds this view (not the selected one: the view may be filled later)
        Component child = this;
        Component parent = getParent();
        while (parent != null && !(parent instanceof JTabbedPane)) {
            child = parent;
            parent = parent.getParent();
        }
        if (parent != null) {
            JTabbedPane tabs = (JTabbedPane) parent;
            int i = tabs.indexOfComponent(child);
            if (i >= 0) {
                tabs.setTitleAt(i, title);
            }
        } else if (getRootPane() != null && getRootPane().getParent() instanceof JFrame) {
            ((JFrame) getRootPane().getParent()).setTitle(title);
        }
    }

    /**
     * @return a header slot which shows the component set via
     * {@link #setPluginBadge(JComponent)}
     */
    protected final JComponent createPluginBadgeSlot() {
        final JPanel slot = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, ModernTheme.px(6), 0));
        slot.setOpaque(false);
        slot.setVisible(false);
        Runnable update = () -> {
            Object b = getClientProperty(PROPERTY_PLUGIN_BADGE);
            slot.removeAll();
            if (b instanceof JComponent) {
                slot.add((JComponent) b);
                slot.setVisible(true);
            } else {
                slot.setVisible(false);
            }
            slot.revalidate();
            slot.repaint();
        };
        addPropertyChangeListener(PROPERTY_PLUGIN_BADGE, e -> {
            if (javax.swing.SwingUtilities.isEventDispatchThread()) {
                update.run();
            } else {
                javax.swing.SwingUtilities.invokeLater(update);
            }
        });
        return slot;
    }

    /**
     * Set while the selection of a record list is changed by code, so the
     * selection listener does not load the record again
     */
    private boolean syncingRecordSelection = false;

    /**
     * Makes a table a list of records (the first column holds the
     * {@link mpv5.db.common.DatabaseObject}): selecting a row with a single
     * click, the arrow keys or Enter shows that record in this view.
     *
     * @param table
     */
    protected final void installRecordList(final JTable table) {
        table.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        table.setRowSelectionAllowed(true);
        //the row of the shown record stays highlighted while editing the details (the look and
        //feel shows a pale selection when the table has no focus, unless the colours are set)
        Runnable colours = () -> {
            java.awt.Color bg = javax.swing.UIManager.getColor("Table.selectionBackground");
            java.awt.Color fg = javax.swing.UIManager.getColor("Table.selectionForeground");
            if (bg != null) {
                table.setSelectionBackground(new java.awt.Color(bg.getRGB(), true));
            }
            if (fg != null) {
                table.setSelectionForeground(new java.awt.Color(fg.getRGB(), true));
            }
        };
        colours.run();
        table.addPropertyChangeListener("UI", e -> colours.run());
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && !syncingRecordSelection) {
                showRecordOfRow(table, false);
            }
        });
        //a click on the row already selected shows it, if the view shows another record (e.g. a new one)
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseReleased(java.awt.event.MouseEvent e) {
                if (javax.swing.SwingUtilities.isLeftMouseButton(e) && table.rowAtPoint(e.getPoint()) == table.getSelectedRow()) {
                    showRecordOfRow(table, false);
                }
            }
        });
        //Enter shows the selected record instead of moving to the next row
        table.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
                .put(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ENTER, 0), "yabs.showRecord");
        table.getInputMap(JComponent.WHEN_FOCUSED)
                .put(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ENTER, 0), "yabs.showRecord");
        table.getActionMap().put("yabs.showRecord", new javax.swing.AbstractAction() {
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                showRecordOfRow(table, true);
            }
        });
    }

    /**
     * Shows the record of the selected row
     *
     * @param table
     * @param always false: only if this view shows another record now
     */
    private void showRecordOfRow(JTable table, boolean always) {
        mpv5.db.common.DatabaseObject o = recordAt(table, table.getSelectedRow());
        if (o == null) {
            return;
        }
        mpv5.db.common.DatabaseObject current = getDataOwner();
        if (!always && current != null && current.getClass() == o.getClass()
                && current.__getIDS() > 0 && current.__getIDS() == o.__getIDS()) {
            return;
        }
        setDataOwner(o, true);
    }

    private static mpv5.db.common.DatabaseObject recordAt(JTable table, int viewRow) {
        if (viewRow < 0 || viewRow >= table.getRowCount()) {
            return null;
        }
        Object val = table.getModel().getValueAt(table.convertRowIndexToModel(viewRow), 0);
        return val instanceof mpv5.db.common.DatabaseObject ? (mpv5.db.common.DatabaseObject) val : null;
    }

    /**
     * Selects (highlights) the row of the record in a list installed with
     * {@link #installRecordList(JTable)}, without loading it again; clears the
     * selection if the record is not in the (filtered) list.
     *
     * @param table
     * @param record may be null
     */
    protected final void selectRecordRow(JTable table, mpv5.db.common.DatabaseObject record) {
        syncingRecordSelection = true;
        try {
            int found = -1;
            if (record != null && record.__getIDS() > 0) {
                for (int i = 0; i < table.getRowCount(); i++) {
                    mpv5.db.common.DatabaseObject o = recordAt(table, i);
                    if (o != null && o.getClass() == record.getClass() && o.__getIDS() == record.__getIDS()) {
                        found = i;
                        break;
                    }
                }
            }
            if (found >= 0) {
                table.getSelectionModel().setSelectionInterval(found, found);
                Rectangle r = table.getCellRect(found, 0, true);
                table.scrollRectToVisible(r);
            } else {
                table.clearSelection();
            }
        } finally {
            syncingRecordSelection = false;
        }
    }

    private boolean autosaveEnabledPanel;
    private boolean autosaving;
    private long saveGeneration;
    private Class<?> lastOpenedClass;
    private int lastOpenedId = Integer.MIN_VALUE;
    private mpv5.db.common.DatabaseObject lastOpenedRef;

    /**
     * Enables optional focus-leave autosave and newly-opened statusbar logging
     * for this modern view (contacts, products, documents).
     */
    protected final void enableAutosave() {
        autosaveEnabledPanel = true;
        mpv5.ui.misc.Autosave.ensureInstalled();
    }

    public final boolean isAutosaveEnabledPanel() {
        return autosaveEnabledPanel;
    }

    public final boolean isAutosaving() {
        return autosaving;
    }

    public boolean isLoadingData() {
        return false;
    }

    public final long getSaveGeneration() {
        return saveGeneration;
    }

    protected boolean canAutosave() {
        return false;
    }

    protected boolean saveInPlaceForAutosave() {
        return false;
    }

    /**
     * Performs a silent in-place save when autosave is enabled and the current
     * dataset is valid and editable.
     *
     * @return true if saved
     */
    public final boolean performAutosave() {
        if (!autosaveEnabledPanel || !mpv5.ui.misc.Autosave.isEnabled()
                || autosaving || isLoadingData() || !isShowing()
                || mpv5.ui.toolbars.RecordActions.isPendingCopy(this)
                || !canAutosave()) {
            return false;
        }
        autosaving = true;
        try {
            if (saveInPlaceForAutosave()) {
                saveGeneration++;
                mpv5.db.common.DatabaseObject owner = getDataOwner();
                if (owner != null) {
                    lastOpenedClass = owner.getClass();
                    lastOpenedId = owner.isExisting() ? owner.__getIDS() : Integer.MIN_VALUE;
                    lastOpenedRef = owner;
                    String label = owner.__getCname();
                    if (label == null || label.trim().isEmpty()) {
                        label = owner.toString();
                    }
                    mpv5.ui.frames.MPView.setStatusMessage(
                            String.format(mpv5.globals.Messages.AUTOSAVE_SAVED.toString(), label));
                }
                return true;
            }
            return false;
        } catch (Exception e) {
            mpv5.logging.Log.Debug(this, e);
            return false;
        } finally {
            autosaving = false;
        }
    }

    private boolean recordNewlyOpened(mpv5.db.common.DatabaseObject obj) {
        if (obj == null) {
            return false;
        }
        boolean same;
        if (obj == lastOpenedRef) {
            same = true;
        } else if (obj.isExisting() && lastOpenedClass == obj.getClass() && lastOpenedId == obj.__getIDS()) {
            same = true;
        } else {
            same = false;
        }
        lastOpenedClass = obj.getClass();
        lastOpenedId = obj.isExisting() ? obj.__getIDS() : Integer.MIN_VALUE;
        lastOpenedRef = obj;
        return !same;
    }

    /**
     * Resets plugin decorations from the previous record and notifies the
     * running plugins that this view now shows the current record
     */
    protected final void notifyViewOpened() {
        saveGeneration++;
        setPluginBanner(null);
        setPluginBadge(null);
        setContentLocked(false);
        setLockedDocument(null);
        mpv5.plugin.ExtensionManager.instance().fireView(this);
        if (autosaveEnabledPanel && recordNewlyOpened(getDataOwner())) {
            mpv5.ui.misc.Autosave.logStatus();
        }
        revalidate();
        repaint();
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
