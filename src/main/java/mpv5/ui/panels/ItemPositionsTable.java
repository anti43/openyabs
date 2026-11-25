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
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableModel;
import mpv5.db.common.Context;
import mpv5.db.objects.Item;
import mpv5.db.objects.SubItem;
import mpv5.db.objects.User;
import mpv5.globals.Messages;
import mpv5.logging.Log;
import mpv5.ui.dialogs.Popup;
import mpv5.ui.dialogs.subcomponents.ItemTextAreaDialog;
import mpv5.ui.dialogs.subcomponents.ProductSelectDialog;
import mpv5.ui.modern.ModernTheme;
import mpv5.ui.popups.TablePopUp;
import mpv5.utils.date.DateConverter;
import mpv5.utils.models.MPTableModel;
import mpv5.utils.numberformat.FormatNumber;
import mpv5.utils.numberformat.InvoiceMath;
import mpv5.utils.renderer.ButtonEditor;
import mpv5.utils.renderer.ButtonRenderer;
import mpv5.utils.renderer.LazyCellEditor;
import mpv5.utils.renderer.LazyCellRenderer;
import mpv5.utils.renderer.TableCellRendererForDezimal;
import mpv5.utils.renderer.TableCellRendererForProducts;
import mpv5.utils.renderer.TableTabAction;
import mpv5.utils.renderer.TextAreaCellEditor;
import mpv5.utils.renderer.TextAreaCellRenderer;
import mpv5.utils.tables.DynamicTableCalculator;
import mpv5.utils.tables.TableFormat;

/**
 * The positions of an offer / order / invoice: the table with product
 * selection, text dialog and live totals (net, discount, tax, gross per EN
 * 16931), buttons to add, remove and move positions, and the totals bar.
 * <p>
 * The table setup and the calculation are the same as in the classic view
 * ({@link ItemPanel2}).
 */
public class ItemPositionsTable extends JPanel {

    public static final String PINNED_PROPERTY = "org.openyabs.layout.positions.editor.pinned";
    public static final String SPLIT_PROPERTY = "org.openyabs.layout.hsplit.positions.editor";

    private static final long serialVersionUID = 1L;
    private final JTable itemtable = new JTable();
    private final JScrollPane scroll;
    private final JLabel netvalue = total();
    private final JLabel discount = total();
    private final JLabel taxvalue = total();
    private final JLabel value = total();
    /**
     * discount with tax (no label in the bar, read when saving)
     */
    private final JLabel hidden = new JLabel();
    private DynamicTableCalculator itemMultiplier;
    private DynamicTableCalculator taxcalculator;
    private DynamicTableCalculator netCalculator;
    private DynamicTableCalculator discnetcalculator;
    private DynamicTableCalculator discbrutcalculator;
    private final List<Item> usedOrders = new ArrayList<Item>();
    private final JButton addButton;
    private final JButton removeButton;
    private final JButton upButton;
    private final JButton downButton;
    private final javax.swing.JToggleButton pinButton;
    private boolean editable = true;
    private boolean editorPinned;
    private boolean restoredPinState;
    private ItemTextAreaDialog editorDialog;
    private TextAreaCellEditor currentTextEditor;
    private javax.swing.JSplitPane editorSplit;
    private JPanel dockedContainer;
    private JLabel dockedTitle;
    private int dockedRow = -1;
    private boolean syncingDocked;
    private MPTableModel listenedModel;
    private final javax.swing.event.TableModelListener dockedModelListener = e -> onDockedModelChanged(e);
    private Runnable onPositionsEdited;

    public ItemPositionsTable() {
        super(new BorderLayout(0, ModernTheme.px(6)));
        setOpaque(false);
        itemtable.setName("itemtable");
        itemtable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        itemtable.setSurrendersFocusOnKeystroke(true);
        itemtable.getColumnModel().getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        itemtable.setSelectionBackground(ModernTheme.accentSoft());
        itemtable.setSelectionForeground(ModernTheme.foreground());
        //right click: the menu of the positions (see prepareTable)

        addButton = button("/images/16/add.png", Messages.ACTION_ADD.toString());
        addButton.addActionListener(e -> {
            if (editable) {
                getModel().addRow(1);
            }
        });
        removeButton = button("/images/16/remove.png", Messages.ACTION_REMOVE.toString());
        removeButton.addActionListener(e -> removeSelected());
        upButton = button("/images/16/arrow-up.png", Messages.ITEM_MOVE_UP.toString());
        upButton.addActionListener(e -> move(-1));
        downButton = button("/images/16/arrow-down.png", Messages.ITEM_MOVE_DOWN.toString());
        downButton.addActionListener(e -> move(1));
        pinButton = new javax.swing.JToggleButton(ItemTextAreaDialog.createPinIcon(false));
        pinButton.putClientProperty(mpv5.ui.misc.Autosave.IGNORE_PROPERTY, Boolean.TRUE);
        pinButton.setSelectedIcon(ItemTextAreaDialog.createPinIcon(true));
        pinButton.setToolTipText(Messages.ITEM_TEXT_PIN.toString());
        pinButton.setFocusable(false);
        pinButton.setMargin(new java.awt.Insets(ModernTheme.px(3), ModernTheme.px(6), ModernTheme.px(3), ModernTheme.px(6)));
        pinButton.addActionListener(e -> setEditorPinned(pinButton.isSelected()));

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, ModernTheme.px(4), 0));
        buttons.setOpaque(false);
        buttons.add(addButton);
        buttons.add(removeButton);
        buttons.add(upButton);
        buttons.add(downButton);
        buttons.add(pinButton);

        //the actions of the document right of the position buttons, directly above the list
        bar.setOpaque(false);
        bar.add(buttons, BorderLayout.WEST);
        actions.setOpaque(false);
        bar.add(actions, BorderLayout.CENTER);
        bar.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, ModernTheme.px(4), 0));

        scroll = new JScrollPane(itemtable);
        scroll.getViewport().setBackground(ModernTheme.cardBackground());
        add(bar, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
        add(createTotals(), BorderLayout.SOUTH);

        itemtable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && editorPinned && !syncingDocked) {
                onTableSelectionChanged();
            }
        });
        itemtable.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (!editorPinned || syncingDocked) {
                    return;
                }
                int row = itemtable.rowAtPoint(e.getPoint());
                int col = itemtable.columnAtPoint(e.getPoint());
                if (row >= 0 && row == dockedRow && col >= 0 && itemtable.convertColumnIndexToModel(col) != 4) {
                    commitDockedIfDirty(false);
                }
            }
        });
        setDefaultPositions();
    }

    @Override
    public void addNotify() {
        super.addNotify();
        if (!restoredPinState) {
            restoredPinState = true;
            if (loadPinnedPreference() && !java.awt.GraphicsEnvironment.isHeadless()) {
                setEditorPinned(true, false);
            }
        }
    }

    @Override
    public void removeNotify() {
        if (editorDialog != null && !editorPinned && editorDialog.isVisible()) {
            editorDialog.setVisible(false);
        }
        super.removeNotify();
    }

    /**
     * @return true if the text/product editor is currently pinned left of the table
     */
    public boolean isEditorPinned() {
        return editorPinned;
    }

    /**
     * Pins or unpins the position text/product editor left of the positions table
     * and persists the preference for the current user.
     *
     * @param pinned true to dock left of the table, false to use the floating popup
     */
    public void setEditorPinned(boolean pinned) {
        setEditorPinned(pinned, true);
    }

    private void setEditorPinned(boolean pinned, boolean persist) {
        if (persist) {
            savePinnedPreference(pinned);
        }
        pinButton.setSelected(pinned);
        pinButton.setToolTipText((pinned ? Messages.ITEM_TEXT_UNPIN : Messages.ITEM_TEXT_PIN).toString());
        if (pinned == editorPinned) {
            return;
        }
        if (pinned) {
            ItemTextAreaDialog dlg = ensureEditorDialog();
            boolean wasVisible = dlg.isVisible();
            int wasRow = dlg.getEditedRow();
            String wasText = dlg.textArea.getText();
            Number wasQty = dlg.getQuantityValue();
            mpv5.db.objects.Product wasProduct = dlg.getSelectedProduct();
            boolean wasDirty = dlg.isDirty();
            boolean wasQtyDirty = dlg.isQuantityDirty();

            if (itemtable.isEditing() && itemtable.getCellEditor() == currentTextEditor) {
                itemtable.getCellEditor().cancelCellEditing();
            }

            dlg.setDocked(true);
            dlg.setEditorEditable(editable);

            JPanel container = ensureDockedContainer();
            container.add(dlg.getEditorPanel(), BorderLayout.CENTER);
            remove(scroll);
            editorSplit = createEditorSplit(container, scroll);
            add(editorSplit, BorderLayout.CENTER);
            editorPinned = true;

            if (wasVisible && wasRow >= 0 && wasRow < itemtable.getRowCount()) {
                syncingDocked = true;
                try {
                    dockedRow = wasRow;
                    dlg.restoreInProgressState(wasRow, wasText, wasQty, wasProduct, wasDirty, wasQtyDirty);
                    updateDockedTitle();
                } finally {
                    syncingDocked = false;
                }
                javax.swing.SwingUtilities.invokeLater(dlg::focusTextArea);
            } else {
                loadDockedRow(fallbackDockedRow());
            }
            revalidate();
            repaint();
        } else {
            commitDockedIfDirty(false);
            editorPinned = false;
            dockedRow = -1;
            if (editorDialog != null) {
                editorDialog.setDocked(false);
            }
            if (editorSplit != null) {
                remove(editorSplit);
                editorSplit.remove(scroll);
                editorSplit = null;
            }
            add(scroll, BorderLayout.CENTER);
            revalidate();
            repaint();
        }
    }

    ItemTextAreaDialog getEditorDialog() {
        return editorDialog;
    }

    private ItemTextAreaDialog ensureEditorDialog() {
        if (editorDialog == null) {
            java.awt.Frame owner = null;
            try {
                owner = mpv5.YabsViewProxy.instance().getIdentifierFrame();
            } catch (Exception ignored) {
                // tests without main window
            }
            editorDialog = new ItemTextAreaDialog(owner, false);
            editorDialog.setParentTable(itemtable);
            editorDialog.setPinToggleAction(() -> setEditorPinned(!editorPinned));
            editorDialog.setDockedCallbacks(this::onDockedApply, this::onDockedCancel);
            editorDialog.setEditorEditable(editable);
            if (currentTextEditor != null) {
                editorDialog.okButton.addActionListener(currentTextEditor);
                editorDialog.cancelButton.addActionListener(currentTextEditor);
            }
        }
        return editorDialog;
    }

    private JPanel ensureDockedContainer() {
        if (dockedContainer == null) {
            dockedContainer = new JPanel(new BorderLayout());
            dockedContainer.setBackground(ModernTheme.cardBackground());
            dockedContainer.setBorder(BorderFactory.createLineBorder(ModernTheme.border()));
            dockedContainer.setMinimumSize(new java.awt.Dimension(ModernTheme.px(220), ModernTheme.px(120)));

            JPanel header = new JPanel(new BorderLayout(ModernTheme.px(6), 0));
            header.setBackground(ModernTheme.blend(ModernTheme.background(), ModernTheme.cardBackground(), 0.45f));
            header.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, ModernTheme.border()),
                    BorderFactory.createEmptyBorder(ModernTheme.px(3), ModernTheme.px(8), ModernTheme.px(3), ModernTheme.px(4))));

            dockedTitle = new JLabel(Messages.ITEM_SECTION_POSITIONS.toString());
            dockedTitle.setFont(ModernTheme.font(0, Font.BOLD));
            dockedTitle.setForeground(ModernTheme.foreground());
            header.add(dockedTitle, BorderLayout.WEST);

            JButton unpin = ItemTextAreaDialog.createFlatPinButton(true, Messages.ITEM_TEXT_UNPIN.toString(),
                    () -> setEditorPinned(false));
            header.add(unpin, BorderLayout.EAST);

            dockedContainer.add(header, BorderLayout.NORTH);
        }
        return dockedContainer;
    }

    private javax.swing.JSplitPane createEditorSplit(JComponent left, JComponent right) {
        right.setMinimumSize(new java.awt.Dimension(ModernTheme.px(260), ModernTheme.px(120)));
        final javax.swing.JSplitPane split = new javax.swing.JSplitPane(javax.swing.JSplitPane.HORIZONTAL_SPLIT, left, right);
        split.setBorder(BorderFactory.createEmptyBorder());
        split.setOpaque(false);
        split.setContinuousLayout(true);
        split.setResizeWeight(0.0);

        final Double savedRatio = loadSplitRatio();
        final Runnable initDivider = () -> {
            int avail = split.getWidth() - split.getDividerSize();
            if (avail <= 0) {
                return;
            }
            int minLeft = ModernTheme.px(220);
            int maxLeft = Math.max(minLeft, avail - ModernTheme.px(260));
            int loc;
            if (savedRatio != null) {
                loc = (int) Math.round(avail * savedRatio);
            } else {
                loc = Math.max(ModernTheme.px(300), (int) Math.round(avail * 0.28));
            }
            loc = Math.max(minLeft, Math.min(loc, maxLeft));
            split.setDividerLocation(loc);
            split.addPropertyChangeListener(javax.swing.JSplitPane.DIVIDER_LOCATION_PROPERTY, ev -> {
                int w = split.getWidth() - split.getDividerSize();
                if (w > 0 && split.isShowing()) {
                    double r = (double) split.getDividerLocation() / w;
                    if (r >= 0.05 && r <= 0.95) {
                        saveSplitRatio(r);
                    }
                }
            });
        };

        if (split.getWidth() > 0) {
            initDivider.run();
        } else {
            split.addComponentListener(new java.awt.event.ComponentAdapter() {
                @Override
                public void componentResized(java.awt.event.ComponentEvent e) {
                    if (split.getWidth() > 0) {
                        split.removeComponentListener(this);
                        initDivider.run();
                    }
                }
            });
        }
        return split;
    }

    private static boolean loadPinnedPreference() {
        try {
            String v = User.getCurrentUser().getProperties().getProperty(PINNED_PROPERTY);
            return Boolean.parseBoolean(v);
        } catch (Exception e) {
            return false;
        }
    }

    private static void savePinnedPreference(boolean pinned) {
        try {
            User.getCurrentUser().getProperties().changeProperty(PINNED_PROPERTY, String.valueOf(pinned));
        } catch (Exception ignored) {
            // tests without user
        }
    }

    private static Double loadSplitRatio() {
        try {
            String s = User.getCurrentUser().getProperties().getProperty(SPLIT_PROPERTY);
            if (s != null) {
                double d = Double.parseDouble(s);
                if (d >= 0.05 && d <= 0.95) {
                    return d;
                }
            }
        } catch (Exception ignored) {
            // default
        }
        return null;
    }

    private static void saveSplitRatio(double ratio) {
        try {
            User.getCurrentUser().getProperties().changeProperty(SPLIT_PROPERTY,
                    String.format(java.util.Locale.ROOT, "%.3f", ratio));
        } catch (Exception ignored) {
            // tests without user
        }
    }

    private int fallbackDockedRow() {
        int sel = itemtable.getSelectedRow();
        if (sel >= 0 && sel < itemtable.getRowCount()) {
            return sel;
        }
        return itemtable.getRowCount() > 0 ? 0 : -1;
    }

    private void loadDockedRow(int row) {
        if (!editorPinned || editorDialog == null) {
            return;
        }
        syncingDocked = true;
        try {
            MPTableModel m = itemtable.getModel() instanceof MPTableModel ? getModel() : null;
            if (m == null || row < 0 || row >= m.getRowCount()) {
                dockedRow = -1;
                editorDialog.loadRowState(-1, "", 1);
                editorDialog.setEditorEditable(false);
                updateDockedTitle();
                return;
            }
            dockedRow = row;
            Object descObj = m.getValueAt(row, 4);
            String desc = descObj == null ? "" : descObj.toString();
            Number qty = parseQuantity(m.getValueAt(row, 2));
            editorDialog.loadRowState(row, desc, qty);
            editorDialog.setEditorEditable(editable);
            updateDockedTitle();
        } finally {
            syncingDocked = false;
        }
    }

    private static Number parseQuantity(Object val) {
        if (val instanceof Number) {
            return (Number) val;
        }
        if (val != null) {
            try {
                return new BigDecimal(val.toString().trim());
            } catch (Exception ignored) {
            }
        }
        return Integer.valueOf(1);
    }

    private void updateDockedTitle() {
        if (dockedTitle == null) {
            return;
        }
        if (dockedRow >= 0 && dockedRow < itemtable.getRowCount()) {
            Object posNum = itemtable.getModel().getValueAt(dockedRow, 1);
            int displayNum = dockedRow + 1;
            if (posNum instanceof Number && ((Number) posNum).intValue() > 0) {
                displayNum = ((Number) posNum).intValue();
            } else if (posNum != null) {
                try {
                    int parsed = Integer.parseInt(posNum.toString().trim());
                    if (parsed > 0) {
                        displayNum = parsed;
                    }
                } catch (NumberFormatException ignored) {
                }
            }
            dockedTitle.setText(String.format(Messages.ITEM_TEXT_POSITION.toString(), displayNum));
        } else {
            dockedTitle.setText(Messages.ITEM_SECTION_POSITIONS.toString());
        }
    }

    private void commitDockedIfDirty(boolean restoreTableFocus) {
        if (!editorPinned || editorDialog == null || syncingDocked || !editable) {
            return;
        }
        if (dockedRow < 0 || dockedRow >= itemtable.getRowCount()) {
            editorDialog.clearDirty();
            return;
        }
        syncingDocked = true;
        try {
            editorDialog.setEditedRow(dockedRow);
            editorDialog.applyIfDirty(restoreTableFocus);
        } finally {
            syncingDocked = false;
        }
    }

    private void onTableSelectionChanged() {
        int newRow = itemtable.getSelectedRow();
        if (newRow < 0 && itemtable.getRowCount() > 0) {
            newRow = 0;
        }
        if (newRow == dockedRow) {
            return;
        }
        commitDockedIfDirty(false);
        loadDockedRow(newRow);
    }

    private void onDockedApply(boolean restoreTableFocus) {
        if (dockedRow >= 0 && dockedRow < itemtable.getRowCount()) {
            loadDockedRow(dockedRow);
        }
        notifyPositionsEdited();
        if (restoreTableFocus) {
            javax.swing.SwingUtilities.invokeLater(itemtable::requestFocusInWindow);
        }
    }

    private void onDockedCancel() {
        if (dockedRow >= 0 && dockedRow < itemtable.getRowCount()) {
            loadDockedRow(dockedRow);
        }
        javax.swing.SwingUtilities.invokeLater(itemtable::requestFocusInWindow);
    }

    private void onDockedModelChanged(javax.swing.event.TableModelEvent e) {
        if (!editorPinned || syncingDocked || editorDialog == null) {
            return;
        }
        int count = itemtable.getRowCount();
        if (dockedRow < 0 || dockedRow >= count) {
            loadDockedRow(fallbackDockedRow());
            return;
        }
        if (e.getType() == javax.swing.event.TableModelEvent.UPDATE
                && dockedRow >= e.getFirstRow() && dockedRow <= e.getLastRow()) {
            if (!editorDialog.isDirty()) {
                loadDockedRow(dockedRow);
            } else {
                updateDockedTitle();
            }
        } else {
            updateDockedTitle();
        }
    }

    private void focusDockedTextArea(int row) {
        if (!editorPinned || editorDialog == null) {
            return;
        }
        int target = row >= 0 && row < itemtable.getRowCount() ? row : fallbackDockedRow();
        if (target >= 0 && target != dockedRow) {
            commitDockedIfDirty(false);
            loadDockedRow(target);
        }
        if (target >= 0 && itemtable.getSelectedRow() != target) {
            itemtable.setRowSelectionInterval(target, target);
        }
        javax.swing.SwingUtilities.invokeLater(editorDialog::focusTextArea);
    }

    private void resetDockedBeforeModelChange() {
        if (editorDialog != null) {
            editorDialog.clearDirty();
        }
        dockedRow = -1;
    }

    private final JPanel bar = new JPanel(new BorderLayout(ModernTheme.px(10), 0));
    private final mpv5.ui.modern.OverflowBar actions = new mpv5.ui.modern.OverflowBar(true, true);

    /**
     * Detaches the position/document action toolbar from above the table so it
     * can be placed inline in the surrounding {@link mpv5.ui.modern.Card}
     * header row on compact screens.
     *
     * @return the toolbar component
     */
    public JComponent detachToolbar() {
        remove(bar);
        bar.setBorder(null);
        revalidate();
        return bar;
    }

    /**
     * Adds an action of the document to the bar above the positions (right)
     *
     * @param c a button
     */
    public void addAction(javax.swing.JComponent c) {
        if (c instanceof javax.swing.AbstractButton) {
            ModernTheme.actionButton((javax.swing.AbstractButton) c, null);
        }
        actions.addOverflowable(c, null);
    }

    private static JLabel total() {
        JLabel l = new JLabel("0");
        l.setFont(ModernTheme.font(1, Font.PLAIN));
        l.setForeground(ModernTheme.foreground());
        return l;
    }

    private static JButton button(String icon, String tooltip) {
        JButton b = new JButton(new javax.swing.ImageIcon(ItemPositionsTable.class.getResource(icon)));
        b.setToolTipText(tooltip);
        b.setFocusable(false);
        b.setMargin(new java.awt.Insets(ModernTheme.px(3), ModernTheme.px(6), ModernTheme.px(3), ModernTheme.px(6)));
        return b;
    }

    private static String text(String key) {
        try {
            return mpv5.i18n.LanguageManager.getBundle().getString(key);
        } catch (Exception e) {
            return key;
        }
    }

    /**
     * Net  -  Discount  +  Tax  =  Total, the total large
     */
    private JComponent createTotals() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, ModernTheme.px(14), 0));
        p.setOpaque(false);
        p.add(pair(text("ItemPanel2.jLabel1.text"), netvalue));
        p.add(op("\u2212"));
        p.add(pair(text("ItemPanel2.jLabel5.text_1"), discount));
        p.add(op("+"));
        p.add(pair(text("ItemPanel2.jLabel2.text"), taxvalue));
        p.add(op("="));
        value.setFont(ModernTheme.font(6, Font.BOLD));
        value.setForeground(ModernTheme.accent());
        p.add(pair(text("ItemPanel2.jLabel3.text"), value));
        //directly below the list: no line, no extra room
        p.setBorder(BorderFactory.createEmptyBorder(ModernTheme.px(4), 0, 0, 0));
        return p;
    }

    private static JComponent pair(String label, JLabel v) {
        JPanel p = new JPanel(new BorderLayout(0, ModernTheme.px(1)));
        p.setOpaque(false);
        JLabel l = ModernTheme.fieldLabel(label);
        l.setHorizontalAlignment(JLabel.RIGHT);
        v.setHorizontalAlignment(JLabel.RIGHT);
        p.add(l, BorderLayout.NORTH);
        p.add(v, BorderLayout.CENTER);
        return p;
    }

    private static JComponent op(String s) {
        JLabel l = new JLabel(s);
        l.setFont(ModernTheme.font(2, Font.PLAIN));
        l.setForeground(ModernTheme.muted());
        return l;
    }

    // <editor-fold defaultstate="collapsed" desc="Positions">
    /**
     * @return the table
     */
    public JTable getTable() {
        return itemtable;
    }

    /**
     * @return the model of the positions
     */
    public MPTableModel getModel() {
        return (MPTableModel) itemtable.getModel();
    }

    /**
     * @return orders taken over as positions ("orders over products"), set to
     * finished when the document is saved
     */
    public List<Item> getUsedOrders() {
        return usedOrders;
    }

    private BigDecimal customerTaxRate = null;

    /**
     * @return the customer tax rate override, or null if documents use product-specific rates
     */
    public BigDecimal getCustomerTaxRate() {
        return customerTaxRate;
    }

    /**
     * @param rate customer tax rate override, or null
     */
    public void setCustomerTaxRate(BigDecimal rate) {
        this.customerTaxRate = rate;
    }

    /**
     * Sets the customer tax rate override, updates existing positions to this rate,
     * and recalculates totals.
     *
     * @param rate customer tax rate override
     */
    public void applyCustomerTaxRate(BigDecimal rate) {
        this.customerTaxRate = rate;
        if (rate == null) {
            return;
        }
        MPTableModel model = getModel();
        for (int r = 0; r < model.getRowCount(); r++) {
            Object desc = model.getValueAt(r, 4);
            Object ext = model.getValueAt(r, 5);
            if ((desc != null && !desc.toString().trim().isEmpty()) || (ext != null && !ext.toString().trim().isEmpty())) {
                model.setValueAt(rate, r, 6);
            }
        }
        recalculate();
    }

    /**
     * Six empty positions (a new document)
     */
    public final void setDefaultPositions() {
        resetDockedBeforeModelChange();
        SubItem[] items = new SubItem[]{
            SubItem.getDefaultItem(), SubItem.getDefaultItem(),
            SubItem.getDefaultItem(), SubItem.getDefaultItem(),
            SubItem.getDefaultItem(), SubItem.getDefaultItem()
        };
        if (customerTaxRate != null) {
            for (SubItem s : items) {
                s.setTaxpercentvalue(customerTaxRate);
            }
        }
        itemtable.setModel(SubItem.toModel(items));
        formatTable();
        if (editorPinned) {
            loadDockedRow(fallbackDockedRow());
        }
    }

    /**
     * Shows the positions of a document, with an empty row to add one
     *
     * @param items
     */
    public void setPositions(SubItem[] items) {
        resetDockedBeforeModelChange();
        itemtable.setModel(SubItem.toModel(items));
        if (editable && getModel().getEmptyRows(new int[]{4}) < 2) {
            getModel().addRow(1);
        }
        formatTable();
        fireUpdate();
        if (editorPinned) {
            loadDockedRow(fallbackDockedRow());
        }
    }

    /**
     * Uses the given model (e.g. positions taken over from other documents)
     *
     * @param model
     */
    public void setModel(MPTableModel model) {
        resetDockedBeforeModelChange();
        itemtable.setModel(model);
        keepTrailingEmptyRow(model);
        formatTable();
        fireUpdate();
        if (editorPinned) {
            loadDockedRow(fallbackDockedRow());
        }
    }

    /**
     * Registers a callback invoked when positions are modified via dialog or
     * toolbar/context actions (used by optional autosave).
     */
    public void setOnPositionsEdited(Runnable onPositionsEdited) {
        this.onPositionsEdited = onPositionsEdited;
    }

    private void notifyPositionsEdited() {
        if (onPositionsEdited != null) {
            onPositionsEdited.run();
        }
    }

    /**
     * @return true if a table cell editor is currently active and owns keyboard focus
     */
    public boolean isEditingFocusedCell() {
        if (!itemtable.isEditing()) {
            return false;
        }
        java.awt.Component editor = itemtable.getEditorComponent();
        java.awt.Component focus = java.awt.KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
        return editor != null && focus != null
                && (focus == editor || javax.swing.SwingUtilities.isDescendingFrom(focus, editor));
    }

    /**
     * Ends editing a cell, so its value is in the model
     */
    public void stopEditing() {
        if (itemtable.getCellEditor() != null) {
            try {
                itemtable.getCellEditor().stopCellEditing();
            } catch (Exception e) {
                Log.Debug(this, e.getMessage());
            }
        }
        commitDockedIfDirty(false);
    }

    /**
     * Ends editing and selects the first cell (as before saving)
     */
    public void prepareSave() {
        stopEditing();
        try {
            itemtable.changeSelection(0, 0, true, false);
        } catch (Exception e) {
            Log.Debug(e);
        }
    }

    private void fireUpdate() {
        try {
            getModel().fireTableCellUpdated(0, 0);
        } catch (Exception e) {
            Log.Debug(this, e.getMessage());
        }
    }

    /**
     * Calculates all totals once (after positions were added)
     */
    public void recalculate() {
        for (DynamicTableCalculator c : new DynamicTableCalculator[]{itemMultiplier, netCalculator, taxcalculator, discnetcalculator, discbrutcalculator}) {
            if (c != null) {
                c.calculateOnce();
            }
        }
    }

    /**
     * Enables or disables editing the positions (cells, buttons, context menu mutations)
     *
     * @param editable true if positions can be modified
     */
    public void setEditable(boolean editable) {
        this.editable = editable;
        if (!editable) {
            stopEditing();
        }
        addButton.setEnabled(editable);
        removeButton.setEnabled(editable);
        upButton.setEnabled(editable);
        downButton.setEnabled(editable);
        if (editorDialog != null) {
            editorDialog.setEditorEditable(editable && (!editorPinned || (dockedRow >= 0 && dockedRow < itemtable.getRowCount())));
        }
        if (itemtable.getModel() instanceof MPTableModel) {
            applyCanEdits();
        }
    }

    /**
     * @return true if positions can be modified
     */
    public boolean isEditable() {
        return editable;
    }

    /**
     * Aligns the trailing empty row with the current editability: read-only
     * documents drop trailing unsaved empty rows so no phantom empty position
     * is shown; editable documents ensure a free row at the bottom.
     */
    public void syncTrailingEmptyRow() {
        if (!(itemtable.getModel() instanceof MPTableModel)) {
            return;
        }
        MPTableModel model = getModel();
        if (!editable) {
            while (model.getRowCount() > 0 && isTrailingEmptyRow(model, model.getRowCount() - 1)) {
                model.removeRow(model.getRowCount() - 1);
            }
        } else if (model.getRowCount() == 0 || !isTrailingEmptyRow(model, model.getRowCount() - 1)) {
            model.addRow(1);
            if (customerTaxRate != null) {
                model.setValueAt(customerTaxRate, model.getRowCount() - 1, 6);
            }
        }
    }

    private static boolean isTrailingEmptyRow(MPTableModel model, int row) {
        Object id = model.getValueAt(row, 0);
        if (id instanceof Number && ((Number) id).intValue() > 0) {
            return false;
        }
        if (id != null && !(id instanceof Number)) {
            try {
                if (Integer.parseInt(id.toString().trim()) > 0) {
                    return false;
                }
            } catch (NumberFormatException ignored) {
            }
        }
        Object desc = model.getValueAt(row, 4);
        if (desc != null && !desc.toString().trim().isEmpty()) {
            return false;
        }
        Object price = model.getValueAt(row, 5);
        if (price != null && !price.toString().trim().isEmpty()) {
            try {
                BigDecimal p = price instanceof Number
                        ? FormatNumber.getBigDecimal(price)
                        : FormatNumber.parseDezimal(price.toString());
                if (p != null && p.signum() != 0) {
                    return false;
                }
            } catch (Exception ignored) {
            }
        }
        if (SubItem.productIdOf(model.getValueAt(row, 10)) > 0) {
            return false;
        }
        Object opt = model.getValueAt(row, 14);
        return opt == null || opt.toString().trim().isEmpty();
    }

    private void applyCanEdits() {
        MPTableModel model = getModel();
        if (!editable) {
            model.setCanEdits(new boolean[18]);
            return;
        }
        //the product column is a link only: products are chosen in the dialog of the text column
        model.setCanEdits(new boolean[]{false, false, true, true, true, true, true, true, false, false, false, true, false, false, true, true, false, false});
        if (User.getCurrentUser().getProperties().getProperty("org.openyabs.uiproperty", "hidecolumnquantity")) {
            model.setCellEditable(0, 2, false);
        }
        if (User.getCurrentUser().getProperties().getProperty("org.openyabs.uiproperty", "hidecolumnmeasure")) {
            model.setCellEditable(0, 3, false);
        }
        if (User.getCurrentUser().getProperties().getProperty("org.openyabs.uiproperty", "hideproductscolumn")) {
            model.setCellEditable(0, 10, false);
        }
        if (User.getCurrentUser().getProperties().getProperty("org.openyabs.uiproperty", "hidetaxcolumn")) {
            model.setCellEditable(0, 6, false);
        }
        if (User.getCurrentUser().getProperties().getProperty("org.openyabs.uiproperty", "hidediscountcolumn")) {
            model.setCellEditable(0, 15, false);
        }
        if (!User.getCurrentUser().getProperties().getProperty("org.openyabs.uiproperty", "showoptionalcolumn")) {
            model.setCellEditable(0, 14, false);
        }
    }

    /**
     * Adds a position at the end
     *
     * @param s
     */
    public void addPosition(SubItem s) {
        if (!editable) {
            return;
        }
        if (customerTaxRate != null) {
            s.setTaxpercentvalue(customerTaxRate);
            SubItem.calculate(s);
        }
        getModel().addRow(s.getRowData(getModel().getRowCount() + 1));
        fireUpdate();
    }

    private void removeSelected() {
        if (!editable) {
            return;
        }
        int index = itemtable.getSelectedRow();
        if (index < 0) {
            return;
        }
        if (editorPinned && index == dockedRow && editorDialog != null) {
            editorDialog.clearDirty();
        }
        SubItem.addToDeletionQueue(getModel().getValueAt(index, 0));
        getModel().removeRow(index);
        if (editorPinned) {
            int next = Math.min(index, itemtable.getRowCount() - 1);
            if (next >= 0) {
                itemtable.setRowSelectionInterval(next, next);
            }
            loadDockedRow(next);
        }
        notifyPositionsEdited();
    }

    private void move(int direction) {
        if (!editable) {
            return;
        }
        int index = itemtable.getSelectedRow();
        int target = index + direction;
        if (index < 0 || target < 0 || target >= itemtable.getRowCount()) {
            return;
        }
        commitDockedIfDirty(false);
        getModel().moveRow(index, index, target);
        itemtable.changeSelection(target, itemtable.getSelectedColumn(), false, false);
        if (editorPinned) {
            loadDockedRow(target);
        }
        notifyPositionsEdited();
    }

    /**
     * Tab in the last cell moves to the next field, Shift+Tab in the first to
     * the previous one
     *
     * @param next
     * @param previous
     */
    public void setTabTargets(JComponent next, JComponent previous) {
        InputMap inputMap = itemtable.getInputMap(JTable.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        KeyStroke tab = KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0);
        Action oldAction = itemtable.getActionMap().get(inputMap.get(tab));
        itemtable.getActionMap().put(inputMap.get(tab), new TableTabAction(next, oldAction, false));
        KeyStroke shifttab = KeyStroke.getKeyStroke(KeyEvent.VK_TAB, InputEvent.SHIFT_DOWN_MASK);
        oldAction = itemtable.getActionMap().get(inputMap.get(shifttab));
        itemtable.getActionMap().put(inputMap.get(shifttab), new TableTabAction(previous, oldAction, true));
    }

    /**
     * @return the totals shown: net, tax, discount (net), discount with tax
     */
    public BigDecimal[] getTotals() {
        return new BigDecimal[]{parse(netvalue), parse(taxvalue), parse(discount), parse(hidden)};
    }

    private static BigDecimal parse(JLabel l) {
        try {
            return FormatNumber.parseDezimal(l.getText());
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Table setup (as in ItemPanel2)">
    /**
     * Columns, editors, renderers and calculators of the positions
     */
    public void formatTable() {
        prepareTable();
        sizeColumns();
        MPTableModel model = getModel();
        //the product column is a link only: products are chosen in the dialog of the text column
        model.setCanEdits(new boolean[]{false, false, true, true, true, true, true, true, false, false, false, true, false, false, true, true, false, false});
        //position number: muted, the selected row in the accent color
        itemtable.getColumnModel().getColumn(1).setCellRenderer(new javax.swing.table.DefaultTableCellRenderer() {
            private static final long serialVersionUID = 1L;

            @Override
            public java.awt.Component getTableCellRendererComponent(JTable t, Object v, boolean selected, boolean focus, int row, int col) {
                super.getTableCellRendererComponent(t, v, selected, false, row, col);
                setHorizontalAlignment(CENTER);
                setBackground(selected ? ModernTheme.accent() : ModernTheme.blend(ModernTheme.background(), ModernTheme.cardBackground(), 0.3f));
                setForeground(selected ? java.awt.Color.WHITE : ModernTheme.muted());
                return this;
            }
        });
        if (User.getCurrentUser().getProperties().getProperty("org.openyabs.uiproperty", "hidecolumnquantity")) {
            TableFormat.stripColumn(itemtable, 2);
            model.setCellEditable(0, 2, false);
        }
        if (User.getCurrentUser().getProperties().getProperty("org.openyabs.uiproperty", "hidecolumnmeasure")) {
            TableFormat.stripColumn(itemtable, 3);
            model.setCellEditable(0, 3, false);
        }
        if (User.getCurrentUser().getProperties().getProperty("org.openyabs.uiproperty", "hideproductscolumn")) {
            TableFormat.stripColumn(itemtable, 10);
            model.setCellEditable(0, 10, false);
        }
        if (User.getCurrentUser().getProperties().getProperty("org.openyabs.uiproperty", "hidetaxcolumn")) {
            TableFormat.stripColumn(itemtable, 6);
            model.setCellEditable(0, 6, false);
        }
        if (User.getCurrentUser().getProperties().getProperty("org.openyabs.uiproperty", "hidediscountcolumn")) {
            TableFormat.stripColumn(itemtable, 15);
            model.setCellEditable(0, 15, false);
        }

        if (!User.getCurrentUser().getProperties().getProperty("org.openyabs.uiproperty", "showoptionalcolumn")) {
            TableFormat.stripColumn(itemtable, 14);
            model.setCellEditable(0, 14, false);
        } else {
            int widthc = User.getCurrentUser().getProperties().getProperty("org.openyabs.uiproperty.optionalcolumn.width", 0);
            if (widthc <= 0) {
                widthc = 121;
            }
            TableFormat.resizeCol(itemtable, 14, widthc, widthc != 121);
        }
        applyCanEdits();
        //column move is the very last thing to do, in newcol.asc order
        itemtable.moveColumn(10, 3);
        itemtable.moveColumn(14, 5);
        itemtable.moveColumn(15, 7);
    }

    /**
     * Widths from the font: every column wide enough for its header and a
     * typical value; the text takes all the remaining width, so the table
     * always fills the window. Internal columns are hidden.
     */
    private void sizeColumns() {
        java.awt.FontMetrics cell = itemtable.getFontMetrics(itemtable.getFont());
        java.awt.FontMetrics head = itemtable.getFontMetrics(ModernTheme.font(1, Font.BOLD));
        //model column -> a typical value
        Object[][] cols = {{1, "999"}, {2, "9.999,99"}, {3, "Stunden"}, {5, "99.999,99 \u20ac"}, {6, "19,00"},
            {7, "999.999,99 \u20ac"}, {10, "ART-000001"}, {14, "Optional"}, {15, "100,00"}};
        int pad = ModernTheme.px(18);
        for (Object[] c : cols) {
            int i = (Integer) c[0];
            javax.swing.table.TableColumn tc = itemtable.getColumnModel().getColumn(i);
            int w = Math.max(cell.stringWidth((String) c[1]), head.stringWidth(String.valueOf(tc.getHeaderValue()))) + pad;
            tc.setMinWidth(w);
            tc.setPreferredWidth(w);
            tc.setMaxWidth(i == 1 ? w : w * 2);
        }
        javax.swing.table.TableColumn text = itemtable.getColumnModel().getColumn(4);
        text.setMinWidth(ModernTheme.px(160));
        text.setPreferredWidth(ModernTheme.px(360));
        text.setMaxWidth(Integer.MAX_VALUE);
        //internal: id, tax value, net 2, the old buttons, link, discount values
        for (int i : new int[]{0, 8, 9, SubItem.COLUMNINDEX_ADD, SubItem.COLUMNINDEX_REMOVE, 13, 16, 17}) {
            TableFormat.stripColumn(itemtable, i);
        }
        itemtable.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
    }

    /**
     * The selected row, or -1
     */
    private int selectedRow() {
        return itemtable.getSelectedRow();
    }

    /**
     * Choose a product for the row (the dialog of the text column, with the
     * product search), or an order if orders are preferred
     */
    private void choose(int row) {
        if (!editable || row < 0) {
            return;
        }
        if (!User.getCurrentUser().getProperties().getProperty("org.openyabs.uiproperty", "ordersoverproducts")) {
            if (editorPinned && editorDialog != null) {
                focusDockedTextArea(row);
                javax.swing.SwingUtilities.invokeLater(editorDialog::focusProductSearch);
                return;
            }
            itemtable.editCellAt(row, itemtable.convertColumnIndexToView(4));
            return;
        }
        Item o = (Item) Popup.SelectValue(Context.getOrder());
        if (o != null) {
            //the order as position(s): one per VAT rate, net after discount
            MPTableModel m = getModel();
            java.util.List<SubItem> ps = mpv5.handler.ItemService.asPositions(o,
                    Messages.TYPE_ORDER + " " + o.__getCnumber() + " " + DateConverter.getDefDateString(o.__getDateadded()));
            for (int k = 0; k < ps.size(); k++) {
                if (k > 0) {
                    m.insertRow(row + k);
                }
                m.setRowAt(ps.get(k).getRowData(row + k), row + k, 1);
            }
            usedOrders.add(o);
        }
        if (getModel().getEmptyRows(new int[]{4}) < 2) {
            getModel().addRow(1);
        }
    }

    /**
     * A new empty row at the given index
     */
    private void insertAt(int row) {
        if (!editable) {
            return;
        }
        int r = Math.max(0, Math.min(row, getModel().getRowCount()));
        getModel().insertRow(r);
        getModel().setRowAt(SubItem.getDefaultItem().getRowData(r), r, 1);
        itemtable.changeSelection(r, itemtable.convertColumnIndexToView(4), false, false);
    }

    /**
     * Puts a copy of a position below the given row
     */
    private void insertCopy(SubItem it, int row) {
        if (!editable) {
            return;
        }
        Object[] data = it.getRowData(row + 1);
        data[0] = -1; //a new position
        getModel().insertRow(row + 1);
        getModel().setRowAt(data, row + 1, 1);
        itemtable.changeSelection(row + 1, itemtable.convertColumnIndexToView(4), false, false);
    }

    private void duplicate() {
        int row = selectedRow();
        SubItem it = row < 0 ? null : getModel().getRowAt(row, SubItem.getDefaultItem());
        if (it != null) {
            insertCopy(it, row);
            notifyPositionsEdited();
        }
    }

    private void copy() {
        int row = selectedRow();
        SubItem it = row < 0 ? null : getModel().getRowAt(row, SubItem.getDefaultItem());
        if (it != null) {
            mpv5.YabsViewProxy.instance().addToClipBoard(it);
        }
    }

    /**
     * The positions of the clipboard below the selected row (or at the end)
     */
    private void paste() {
        int row = selectedRow() < 0 ? getModel().getRowCount() - 1 : selectedRow();
        boolean pasted = false;
        for (mpv5.db.common.DatabaseObject o : mpv5.YabsViewProxy.instance().getClipboardItems()) {
            if (o instanceof SubItem) {
                insertCopy((SubItem) o, row++);
                pasted = true;
            }
        }
        if (pasted) {
            notifyPositionsEdited();
        }
    }

    /**
     * Empties the row (the old "C" button)
     */
    private void clearRow() {
        if (!editable) {
            return;
        }
        int row = selectedRow();
        if (row >= 0) {
            if (editorPinned && row == dockedRow && editorDialog != null) {
                editorDialog.clearDirty();
            }
            SubItem.addToDeletionQueue(getModel().getValueAt(row, 0));
            getModel().setRowAt(SubItem.getDefaultItem().getRowData(row), row, 1);
            if (editorPinned && row == dockedRow) {
                loadDockedRow(row);
            }
            notifyPositionsEdited();
        }
    }

    private void prepareTable() {
        boolean calcoptionalcol = User.getCurrentUser().getProperty("org.openyabs.uiproperty", "itempanel.calculateoptionalcol");

        TableCellRendererForProducts tx = new TableCellRendererForProducts(itemtable);
        tx.setRendererTo(10);
        TableCellRendererForDezimal ti = new TableCellRendererForDezimal(itemtable, User.getCurrentUser().getProperty("org.openyabs.uiproperty$defquantityformat"));
        ti.setRendererTo(2);
        LazyCellRenderer lcr = new LazyCellRenderer(itemtable);
        lcr.setRendererTo(3);
        TableCellRendererForDezimal t = new TableCellRendererForDezimal(itemtable);
        t.setRendererTo(6);
        t.setRendererTo(5);
        t.setRendererTo(15);
        t.setRendererTo(16);
        TableCellRendererForDezimal tc = new TableCellRendererForDezimal(itemtable, ModernTheme.blend(ModernTheme.accentSoft(), ModernTheme.cardBackground(), 0.3f));
        tc.setRendererTo(7);
        if (calcoptionalcol) {
            tc.setRendererTo(14);
        }

        TextAreaCellRenderer textAreaCellRenderer = new TextAreaCellRenderer(itemtable);
        textAreaCellRenderer.setRendererTo(4);
        TextAreaCellEditor previousEditor = currentTextEditor;
        TextAreaCellEditor r = new TextAreaCellEditor(itemtable) {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean isCellEditable(java.util.EventObject e) {
                if (!super.isCellEditable(e)) {
                    return false;
                }
                if (editorPinned) {
                    if (!editable) {
                        return false;
                    }
                    int row = itemtable.getSelectedRow();
                    if (e instanceof MouseEvent) {
                        row = itemtable.rowAtPoint(((MouseEvent) e).getPoint());
                    }
                    if (row >= 0) {
                        focusDockedTextArea(row);
                    }
                    return false;
                }
                return true;
            }

            @Override
            public boolean stopCellEditing() {
                boolean stopped = super.stopCellEditing();
                if (stopped && !editorPinned) {
                    notifyPositionsEdited();
                }
                return stopped;
            }
        };
        r.setClickCountToStart(1);
        currentTextEditor = r;
        if (editorDialog != null) {
            if (previousEditor != null) {
                editorDialog.okButton.removeActionListener(previousEditor);
                editorDialog.cancelButton.removeActionListener(previousEditor);
            }
            editorDialog.okButton.addActionListener(r);
            editorDialog.cancelButton.addActionListener(r);
            r.setDialog(editorDialog, editorDialog.textArea);
        } else {
            //created when the text cell is edited the first time
            r.setDialog(() -> ensureEditorDialog(), d -> ((ItemTextAreaDialog) d).textArea);
        }
        r.setEditorTo(4);

        if (listenedModel != null) {
            listenedModel.removeTableModelListener(dockedModelListener);
        }
        listenedModel = getModel();
        listenedModel.addTableModelListener(dockedModelListener);

        itemtable.getColumnModel().getColumn(3).setCellEditor(new LazyCellEditor(new JTextField()));
        String quantXnet = "[2]*[5]" + (calcoptionalcol ? "*[14]" : "");

        itemMultiplier = new DynamicTableCalculator(itemtable, "((" + quantXnet + ")-(" + quantXnet + "%[15]))+((" + quantXnet + ")-(" + quantXnet + "%[15]))%[6]", new int[]{7});
        getModel().addCalculator(itemMultiplier);
        itemMultiplier.addLabel(value, 7);
        netCalculator = new DynamicTableCalculator(itemtable, quantXnet, new int[]{9});
        getModel().addCalculator(netCalculator);
        netCalculator.addLabel(netvalue, 9);
        taxcalculator = new DynamicTableCalculator(itemtable, "(((" + quantXnet + ")-(" + quantXnet + "%[15]))+((" + quantXnet + ")-(" + quantXnet + "%[15]))%[6])-((" + quantXnet + ")-(" + quantXnet + "%[15]))", new int[]{8});
        getModel().addCalculator(taxcalculator);
        taxcalculator.addLabel(taxvalue, 8);
        discnetcalculator = new DynamicTableCalculator(itemtable, quantXnet + "%[15]", new int[]{16});
        getModel().addCalculator(discnetcalculator);
        discnetcalculator.addLabel(discount, 16);
        discbrutcalculator = new DynamicTableCalculator(itemtable, "((" + quantXnet + "%[15])+(" + quantXnet + "%[15]" + "%[6]))", new int[]{17});
        getModel().addCalculator(discbrutcalculator);
        discbrutcalculator.addLabel(hidden, 17);

        //EN 16931 arithmetic (as in the e-invoice): each line rounded to 2 decimals, tax per VAT rate on the sum of the lines
        final boolean optional = calcoptionalcol;
        netCalculator.setRowFunction(v -> InvoiceMath.lineAmount(quantity(v, optional), cell(v, 5)));
        discnetcalculator.setRowFunction(v -> InvoiceMath.lineDiscount(quantity(v, optional), cell(v, 5), cell(v, 15)));
        discbrutcalculator.setRowFunction(v -> InvoiceMath.round2(
                InvoiceMath.lineDiscount(quantity(v, optional), cell(v, 5), cell(v, 15))
                        .multiply(BigDecimal.ONE.add(cell(v, 6).movePointLeft(2)))));
        taxcalculator.setRowFunction(v -> InvoiceMath.lineTax(quantity(v, optional), cell(v, 5), cell(v, 15), cell(v, 6)));
        itemMultiplier.setRowFunction(v -> InvoiceMath.lineNet(quantity(v, optional), cell(v, 5), cell(v, 15))
                .add(InvoiceMath.lineTax(quantity(v, optional), cell(v, 5), cell(v, 15), cell(v, 6))));
        //the optional column holds text ("3", "3 months"): its number is the factor
        if (optional) {
            for (DynamicTableCalculator c : new DynamicTableCalculator[]{netCalculator, discnetcalculator, discbrutcalculator, taxcalculator, itemMultiplier}) {
                c.setColumnParser(14, SubItem::optionalFactor);
            }
        }
        //the price of a product follows its staggered prices when the quantity changes
        mpv5.handler.ItemService.followStaggeredPrices(getModel());
        keepTrailingEmptyRow(getModel());
        followCustomerTaxRate();
        taxcalculator.setSumFunction(m -> invoiceTotals(m, optional).tax);
        itemMultiplier.setSumFunction(m -> invoiceTotals(m, optional).gross);

        //the old "+" ("A") and "-" buttons in the rows: now in the menu (columns hidden)
        JButton b1 = new JButton();
        b1.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (!User.getCurrentUser().getProperties().getProperty("org.openyabs.uiproperty", "ordersoverproducts")) {
                    ProductSelectDialog.instanceOf(getModel(), itemtable.getSelectedRow(), e, 0, null, null, customerTaxRate);
                } else {
                    Item o = (Item) Popup.SelectValue(Context.getOrder());
                    if (o != null) {
                        //the order as position(s): one per VAT rate, net after discount
                        //(before: the gross amount as net price without VAT)
                        int row = itemtable.getSelectedRow();
                        MPTableModel m = getModel();
                        java.util.List<SubItem> ps = mpv5.handler.ItemService.asPositions(o,
                                Messages.TYPE_ORDER + " " + o.__getCnumber() + " " + DateConverter.getDefDateString(o.__getDateadded()));
                        for (int k = 0; k < ps.size(); k++) {
                            if (k > 0) {
                                m.insertRow(row + k);
                            }
                            m.setRowAt(ps.get(k).getRowData(row + k), row + k, 1);
                        }
                        usedOrders.add(o);
                    }
                }
                if (getModel().getEmptyRows(new int[]{4}) < 2) {
                    getModel().addRow(1);
                }
            }
        });
        JButton b2 = new JButton();
        b2.addActionListener(e -> {
            int row = itemtable.getSelectedRow();
            SubItem.addToDeletionQueue(getModel().getValueAt(row, 0));
            getModel().setRowAt(SubItem.getDefaultItem().getRowData(row), row, 1);
        });
        itemtable.getColumnModel().getColumn(SubItem.COLUMNINDEX_ADD).setCellRenderer(new ButtonRenderer());
        itemtable.getColumnModel().getColumn(SubItem.COLUMNINDEX_ADD).setCellEditor(new ButtonEditor(b1));
        itemtable.getColumnModel().getColumn(SubItem.COLUMNINDEX_REMOVE).setCellRenderer(new ButtonRenderer());
        itemtable.getColumnModel().getColumn(SubItem.COLUMNINDEX_REMOVE).setCellEditor(new ButtonEditor(b2));

        boolean orders = User.getCurrentUser().getProperties().getProperty("org.openyabs.uiproperty", "ordersoverproducts");
        TablePopUp menu = new TablePopUp(itemtable, new String[]{
            (orders ? Messages.ITEM_CHOOSE_ORDER : Messages.ITEM_CHOOSE_PRODUCT).getValue(),
            null,
            Messages.ITEM_INSERT_ABOVE.getValue(),
            Messages.ITEM_INSERT_BELOW.getValue(),
            Messages.ITEM_DUPLICATE.getValue(),
            null,
            Messages.ITEM_MOVE_UP.getValue(),
            Messages.ITEM_MOVE_DOWN.getValue(),
            null,
            Messages.ACTION_COPY.getValue(),
            Messages.ACTION_PASTE.getValue(),
            null,
            Messages.ITEM_CLEAR.getValue(),
            Messages.ACTION_REMOVE.getValue()},
                new ActionListener[]{
                    e -> choose(selectedRow()),
                    null,
                    e -> insertAt(Math.max(0, selectedRow())),
                    e -> insertAt(selectedRow() < 0 ? getModel().getRowCount() : selectedRow() + 1),
                    e -> duplicate(),
                    null,
                    e -> move(-1),
                    e -> move(1),
                    null,
                    e -> copy(),
                    e -> paste(),
                    null,
                    e -> clearRow(),
                    e -> removeSelected()});
        //from the position to its product (also in read-only documents)
        PositionProductLink.install(itemtable, menu);
        tx.setLinkHint(true);
        //positions are the main content: larger than the default table font
        Font font = ModernTheme.font(2, Font.PLAIN);
        itemtable.setFont(font);
        for (int i = 0; i < itemtable.getColumnModel().getColumnCount(); i++) {
            javax.swing.table.TableCellEditor ed = itemtable.getColumnModel().getColumn(i).getCellEditor();
            if (ed instanceof javax.swing.DefaultCellEditor) {
                ((javax.swing.DefaultCellEditor) ed).getComponent().setFont(font);
            }
        }
        ModernTheme.style(itemtable);
        itemtable.getTableHeader().setFont(ModernTheme.font(1, Font.BOLD));
    }

    private static BigDecimal cell(Map<Integer, BigDecimal> values, int column) {
        BigDecimal v = values.get(column);
        return v == null ? BigDecimal.ZERO : v;
    }

    /**
     * quantity of a row, times the optional column if it is used in the
     * calculation
     */
    private static BigDecimal quantity(Map<Integer, BigDecimal> values, boolean optional) {
        BigDecimal q = cell(values, 2);
        if (optional) {
            BigDecimal o = values.get(14);
            q = q.multiply(o == null ? BigDecimal.ONE : o);
        }
        return q;
    }

    /**
     * Totals of the table according to EN 16931 (see InvoiceMath)
     */
    private static InvoiceMath.Totals invoiceTotals(TableModel m, boolean optional) {
        List<InvoiceMath.Line> lines = new ArrayList<InvoiceMath.Line>();
        for (int r = 0; r < m.getRowCount(); r++) {
            Map<Integer, BigDecimal> v = new HashMap<Integer, BigDecimal>();
            for (int c : new int[]{2, 5, 6, 15}) {
                if (c < m.getColumnCount() && m.getValueAt(r, c) instanceof Number) {
                    v.put(c, FormatNumber.getBigDecimal(m.getValueAt(r, c)));
                }
            }
            if (optional && 14 < m.getColumnCount()) {
                v.put(14, SubItem.optionalFactor(m.getValueAt(r, 14)));
            }
            lines.add(new InvoiceMath.Line(quantity(v, optional), cell(v, 5), cell(v, 15), cell(v, 6)));
        }
        return InvoiceMath.totals(lines);
    }

    private void keepTrailingEmptyRow(MPTableModel model) {
        mpv5.handler.ItemService.keepTrailingEmptyRow(model, this::isEditable, row -> {
            if (customerTaxRate != null) {
                model.setValueAt(customerTaxRate, row, 6);
            }
        });
    }

    private void followCustomerTaxRate() {
        getModel().addTableModelListener(e -> {
            if (customerTaxRate == null || e.getType() != javax.swing.event.TableModelEvent.UPDATE) {
                return;
            }
            int col = e.getColumn();
            if (col == 10 || col == javax.swing.event.TableModelEvent.ALL_COLUMNS) {
                for (int r = Math.max(0, e.getFirstRow()); r <= Math.min(e.getLastRow(), getModel().getRowCount() - 1); r++) {
                    Object currentTax = getModel().getValueAt(r, 6);
                    BigDecimal tax = currentTax != null ? FormatNumber.getBigDecimal(currentTax) : null;
                    if (tax == null || tax.compareTo(customerTaxRate) != 0) {
                        getModel().setValueAt(customerTaxRate, r, 6);
                    }
                }
            }
        });
    }
    // </editor-fold>
}
