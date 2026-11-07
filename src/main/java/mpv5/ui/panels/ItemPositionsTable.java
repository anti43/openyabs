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
import mpv5.ui.dialogs.subcomponents.ProductSelectDialog3;
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

    private static final long serialVersionUID = 1L;
    private final JTable itemtable = new JTable();
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

    public ItemPositionsTable() {
        super(new BorderLayout(0, ModernTheme.px(6)));
        setOpaque(false);
        itemtable.setName("itemtable");
        itemtable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        itemtable.setSurrendersFocusOnKeystroke(true);
        itemtable.getColumnModel().getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        itemtable.setSelectionBackground(ModernTheme.accentSoft());
        itemtable.setSelectionForeground(ModernTheme.foreground());
        itemtable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evt) {
                //right click: the position to the clipboard, or new rows
                MPTableModel m = getModel();
                if (evt.getButton() != MouseEvent.BUTTON1 && itemtable.getSelectedRow() >= 0) {
                    SubItem it = m.getRowAt(itemtable.getSelectedRow(), SubItem.getDefaultItem());
                    if (it != null) {
                        mpv5.YabsViewProxy.instance().addToClipBoard(it);
                    } else if (!m.hasEmptyRows(new int[]{4})) {
                        m.addRow(2);
                    }
                }
            }
        });

        JButton add = button("/images/16/add.png", Messages.ACTION_ADD.toString());
        add.addActionListener(e -> getModel().addRow(1));
        JButton remove = button("/images/16/remove.png", Messages.ACTION_REMOVE.toString());
        remove.addActionListener(e -> removeSelected());
        JButton up = button("/images/16/arrow-up.png", Messages.ITEM_MOVE_UP.toString());
        up.addActionListener(e -> move(-1));
        JButton down = button("/images/16/arrow-down.png", Messages.ITEM_MOVE_DOWN.toString());
        down.addActionListener(e -> move(1));
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, ModernTheme.px(4), 0));
        buttons.setOpaque(false);
        buttons.add(add);
        buttons.add(remove);
        buttons.add(up);
        buttons.add(down);

        JScrollPane scroll = new JScrollPane(itemtable);
        scroll.getViewport().setBackground(ModernTheme.cardBackground());
        add(buttons, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
        add(createTotals(), BorderLayout.SOUTH);
        setDefaultPositions();
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
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, ModernTheme.border()),
                BorderFactory.createEmptyBorder(ModernTheme.px(6), 0, 0, 0)));
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

    /**
     * Six empty positions (a new document)
     */
    public final void setDefaultPositions() {
        itemtable.setModel(SubItem.toModel(new SubItem[]{
            SubItem.getDefaultItem(), SubItem.getDefaultItem(),
            SubItem.getDefaultItem(), SubItem.getDefaultItem(),
            SubItem.getDefaultItem(), SubItem.getDefaultItem()
        }));
        formatTable();
    }

    /**
     * Shows the positions of a document, with an empty row to add one
     *
     * @param items
     */
    public void setPositions(SubItem[] items) {
        itemtable.setModel(SubItem.toModel(items));
        if (getModel().getEmptyRows(new int[]{4}) < 2) {
            getModel().addRow(1);
        }
        formatTable();
        fireUpdate();
    }

    /**
     * Uses the given model (e.g. positions taken over from other documents)
     *
     * @param model
     */
    public void setModel(MPTableModel model) {
        itemtable.setModel(model);
        fireUpdate();
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
     * Adds a position at the end
     *
     * @param s
     */
    public void addPosition(SubItem s) {
        getModel().addRow(s.getRowData(getModel().getRowCount() + 1));
        fireUpdate();
    }

    private void removeSelected() {
        int index = itemtable.getSelectedRow();
        if (index < 0) {
            return;
        }
        SubItem.addToDeletionQueue(getModel().getValueAt(index, 0));
        getModel().removeRow(index);
    }

    private void move(int direction) {
        int index = itemtable.getSelectedRow();
        int target = index + direction;
        if (index < 0 || target < 0 || target >= itemtable.getRowCount()) {
            return;
        }
        getModel().moveRow(index, index, target);
        itemtable.changeSelection(target, itemtable.getSelectedColumn(), false, false);
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
        //"Internal ID", Position, "Count", "Measure", "Text", "Netto Price", "Tax Rate", "Total Price", "Tax value", "Net 2", "Product ID", "", "", "Link", "Optional", "Discount", ??, Discount w. Tax
        TableFormat.resizeCols(itemtable, new Integer[]{0, 23, 53, 63, 100, 63, 63, 63, 0, 0, 63, 20, 0, 0, 100, 63, 0, 0},
                new Boolean[]{true, true, true, true, false, true, true, true, true, true, true, true, true, true, false, true, true, true});
        MPTableModel model = getModel();
        model.setCanEdits(new boolean[]{false, false, true, true, true, true, true, true, false, false, true, true, false, false, true, true, false, false});
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

        TextAreaCellEditor r = new TextAreaCellEditor(itemtable);
        ProductSelectDialog3 productSelectDialog = new ProductSelectDialog3(mpv5.YabsViewProxy.instance().getIdentifierFrame(), true, itemtable);
        productSelectDialog.okButton.addActionListener(r);
        productSelectDialog.cancelButton.addActionListener(r);
        r.setDialog(productSelectDialog, productSelectDialog.getIDTextField());
        r.setEditorTo(10);

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
        //column move is the very last thing to do, in newcol.asc order
        itemtable.moveColumn(10, 3);
        itemtable.moveColumn(14, 5);
        itemtable.moveColumn(15, 7);
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
        TextAreaCellEditor r = new TextAreaCellEditor(itemtable);
        ItemTextAreaDialog itemTextAreaDialog = new ItemTextAreaDialog(mpv5.YabsViewProxy.instance().getIdentifierFrame(), true);
        itemTextAreaDialog.setParentTable(itemtable);
        itemTextAreaDialog.okButton.addActionListener(r);
        itemTextAreaDialog.cancelButton.addActionListener(r);
        r.setDialog(itemTextAreaDialog, itemTextAreaDialog.textArea);
        r.setEditorTo(4);

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
        taxcalculator.setSumFunction(m -> invoiceTotals(m, optional).tax);
        itemMultiplier.setSumFunction(m -> invoiceTotals(m, optional).gross);

        //"+" in a row: choose a product (or an order), "-": clear the row
        JButton b1 = new JButton();
        b1.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (!User.getCurrentUser().getProperties().getProperty("org.openyabs.uiproperty", "ordersoverproducts")) {
                    ProductSelectDialog.instanceOf(getModel(), itemtable.getSelectedRow(), e, 0, null, null);
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

        new TablePopUp(itemtable, new String[]{
            Messages.ACTION_COPY.getValue(),
            Messages.ACTION_PASTE.getValue(),
            null,
            Messages.ACTION_ADD.getValue(),
            Messages.ACTION_REMOVE.getValue()},
                new ActionListener[]{
                    e -> {
                        SubItem it = getModel().getRowAt(itemtable.getSelectedRow(), SubItem.getDefaultItem());
                        if (it != null) {
                            mpv5.YabsViewProxy.instance().addToClipBoard(it);
                        }
                    },
                    e -> {
                    },
                    null,
                    e -> getModel().addRow(1),
                    e -> removeSelected()});
        ModernTheme.style(itemtable);
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
    // </editor-fold>
}
