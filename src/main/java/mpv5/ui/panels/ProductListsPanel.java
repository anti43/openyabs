/*
 *  This file is part of YaBS.
 *
 *  YaBS is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  YaBS is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with YaBS.  If not, see <http://www.gnu.org/licenses/>.
 */
package mpv5.ui.panels;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.db.common.NodataFoundException;
import mpv5.db.common.QueryCriteria;
import mpv5.db.objects.Group;
import mpv5.db.objects.Item;
import mpv5.db.objects.Product;
import mpv5.db.objects.ProductList;
import mpv5.db.objects.ProductlistSubItem;
import mpv5.db.objects.Tax;
import mpv5.db.objects.User;
import mpv5.globals.Messages;
import mpv5.logging.Log;
import mpv5.ui.beans.LabeledCombobox;
import mpv5.ui.beans.MPCBSelectionChangeReceiver;
import mpv5.ui.beans.LabeledTextField;
import mpv5.ui.dialogs.Popup;
import mpv5.ui.dialogs.Search2;
import mpv5.ui.modern.Card;
import mpv5.ui.modern.ModernTheme;
import mpv5.ui.toolbars.ModernCommandBar;
import mpv5.utils.models.MPComboBoxModelItem;
import mpv5.utils.numberformat.FormatNumber;
import mpv5.utils.ui.TextFieldUtils;

/**
 * A bill of materials (product list): products with quantities. It can be
 * sold as one product (the "BOM product": name of the list, a price, the
 * components in the description) or its components can be added to an
 * invoice as positions.
 */
public class ProductListsPanel extends ModernDataPanel implements MPCBSelectionChangeReceiver {

    private static final long serialVersionUID = 2L;
    private ProductList dataOwner;
    private final SearchPanel sp;
    private final JPanel leftpane = new JPanel(new BorderLayout());
    private final ModernCommandBar tb;
    private final LabeledTextField listname = field();
    private final LabeledCombobox groupnameselect = ModernTheme.plain(new LabeledCombobox());
    private final JTextArea notes = ModernTheme.roomy(new JTextArea(3, 30));
    private final ComponentsModel model = new ComponentsModel();
    private final JTable table = tooltipTable(this);
    private final JLabel purchaseTotal = new JLabel();
    private final JLabel saleTotal = new JLabel();
    private final LabeledTextField price = field();
    private final JLabel productInfo = new JLabel();
    private final JButton productButton = action(Messages.BOM_PRODUCT_CREATE.toString());
    private final JButton openProduct = action(Messages.BOM_PRODUCT_OPEN.toString());
    private final JButton toInvoice = action(Messages.BOM_TO_INVOICE.toString());
    /**
     * the price set automatically (the sales total): as long as the field shows
     * it, the price follows the sales total; a typed price stays
     */
    private BigDecimal autoPrice;
    private Product product;

    public ProductListsPanel() {
        super(new BorderLayout());
        setName("productlistpanel");
        sp = new SearchPanel(Context.getProductList(), this);
        sp.setVisible(true);
        leftpane.setOpaque(false);
        add(leftpane, BorderLayout.WEST);

        groupnameselect.setContext(Context.getGroup());
        groupnameselect.setSearchEnabled(true);
        notes.setLineWrap(true);
        notes.setWrapStyleWord(true);
        price.set_ValueClass(BigDecimal.class);
        table.setModel(model);
        ModernTheme.style(table);
        table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        table.setRowHeight(ModernTheme.px(24));
        table.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        for (int c = ComponentsModel.QTY; c < model.getColumnCount(); c++) {
            if (c != ComponentsModel.UNIT) {
                amountColumn(table, c);
            }
        }
        int[] widths = {30, 80, 220, 70, 60, 90, 90, 60, 90, 90};
        for (int c = 0; c < widths.length; c++) {
            table.getColumnModel().getColumn(c).setPreferredWidth(ModernTheme.px(widths[c]));
        }
        table.getColumnModel().getColumn(0).setMaxWidth(ModernTheme.px(40));
        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.setPreferredSize(new java.awt.Dimension(ModernTheme.px(700), ModernTheme.px(220)));

        JButton add = action(Messages.BOM_ADD.toString());
        add.addActionListener(e -> addProduct());
        JButton remove = action(Messages.BOM_REMOVE.toString());
        remove.addActionListener(e -> removeSelected());
        JPanel tableButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, ModernTheme.px(6), 0));
        tableButtons.setOpaque(false);
        tableButtons.add(add);
        tableButtons.add(remove);

        productButton.addActionListener(e -> saveProduct());
        openProduct.addActionListener(e -> {
            if (product != null) {
                mpv5.YabsViewProxy.instance().addTab(product);
            }
        });
        toInvoice.addActionListener(e -> toInvoice());
        JPanel productButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        productButtons.setOpaque(false);
        productButtons.setBorder(javax.swing.BorderFactory.createEmptyBorder());
        productButtons.add(productButton);
        productButtons.add(javax.swing.Box.createHorizontalStrut(ModernTheme.px(6)));
        productButtons.add(openProduct);
        JLabel hint = new JLabel("<html>" + Messages.BOM_HINT + "</html>");
        hint.setForeground(ModernTheme.muted());

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.add(new Card(Messages.BOM_SECTION_GENERAL.toString())
                .row(Card.field(text("ProductListsPanel.listname._Label"), listname, 2),
                        Card.field(text("ProductListsPanel.groupnameselect._Label"), groupnameselect, 1))
                .row(Card.field(text("ProductListsPanel.notes.text"), new JScrollPane(notes))));
        content.add(new Card(Messages.BOM_SECTION_ITEMS.toString())
                .headerAction(tableButtons)
                .row(Card.field(null, tableScroll))
                .row(Card.field(Messages.BOM_TOTAL_PURCHASE.toString(), purchaseTotal, 1),
                        Card.field(Messages.BOM_TOTAL_SALE.toString(), saleTotal, 1)));
        content.add(new Card(Messages.BOM_SECTION_PRODUCT.toString())
                .row(Card.field(null, hint))
                .row(Card.field(Messages.BOM_PRICE.toString(), price, 1), Card.field(null, productInfo, 2))
                .row(Card.field(null, productButtons)));
        JPanel invoiceRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        invoiceRow.setOpaque(false);
        invoiceRow.add(toInvoice);
        content.add(new Card(Messages.BOM_SECTION_INVOICE.toString())
                .row(Card.field(null, new JLabel("<html>" + Messages.BOM_INVOICE_HINT + "</html>")))
                .row(Card.field(null, invoiceRow)));
        for (java.awt.Component c : content.getComponents()) {
            ((JComponent) c).setAlignmentX(LEFT_ALIGNMENT);
        }
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(content, BorderLayout.NORTH);
        add(scroll(padded(top)), BorderLayout.CENTER);

        //the toolbar calls showSearchBar(), so it is created last
        tb = new ModernCommandBar(this);
        add(tb, BorderLayout.NORTH);

        dataOwner = new ProductList();
        intaddedby_ = User.getCurrentUser().__getIDS();
        dateadded_ = new Date();
        refresh();
        updateTotals();
        updateProduct();
    }

    // <editor-fold defaultstate="collapsed" desc="Components">
    /**
     * The components: the ProductlistSubItems of the list
     */
    final class ComponentsModel extends AbstractTableModel {

        private static final long serialVersionUID = 1L;
        static final int NR = 0, NUMBER = 1, NAME = 2, QTY = 3, UNIT = 4, PURCHASE = 5, SALE = 6, TAX = 7, SUM_PURCHASE = 8, SUM_SALE = 9;
        private final List<ProductlistSubItem> rows = new ArrayList<ProductlistSubItem>();
        private final List<String> numbers = new ArrayList<String>();

        @Override
        public int getRowCount() {
            return rows.size();
        }

        @Override
        public int getColumnCount() {
            return 10;
        }

        @Override
        public String getColumnName(int c) {
            switch (c) {
                case NR:
                    return "#";
                case NUMBER:
                    return Messages.BOM_COL_NUMBER.toString();
                case NAME:
                    return Messages.BOM_COL_NAME.toString();
                case QTY:
                    return Messages.BOM_COL_QTY.toString();
                case UNIT:
                    return Messages.BOM_COL_UNIT.toString();
                case PURCHASE:
                    return Messages.BOM_COL_PURCHASE.toString();
                case SALE:
                    return Messages.BOM_COL_SALE.toString();
                case TAX:
                    return Messages.BOM_COL_TAX.toString();
                case SUM_PURCHASE:
                    return Messages.BOM_COL_SUM_PURCHASE.toString();
                default:
                    return Messages.BOM_COL_SUM_SALE.toString();
            }
        }

        @Override
        public Class<?> getColumnClass(int c) {
            return c == NR || c == NUMBER || c == NAME || c == UNIT ? String.class : BigDecimal.class;
        }

        @Override
        public boolean isCellEditable(int r, int c) {
            return c == QTY || c == PURCHASE || c == SALE;
        }

        @Override
        public Object getValueAt(int r, int c) {
            ProductlistSubItem s = rows.get(r);
            switch (c) {
                case NR:
                    return String.valueOf(r + 1);
                case NUMBER:
                    return numbers.get(r);
                case NAME:
                    return s.getCname();
                case QTY:
                    return bd(s.__getQuantityvalue());
                case UNIT:
                    return s.__getMeasure();
                case PURCHASE:
                    return bd(s.__getInternalvalue());
                case SALE:
                    return bd(s.__getExternalvalue());
                case TAX:
                    return bd(s.__getTaxpercentvalue());
                case SUM_PURCHASE:
                    return bd(s.__getQuantityvalue()).multiply(bd(s.__getInternalvalue())).setScale(2, RoundingMode.HALF_UP);
                default:
                    return bd(s.__getQuantityvalue()).multiply(bd(s.__getExternalvalue())).setScale(2, RoundingMode.HALF_UP);
            }
        }

        @Override
        public void setValueAt(Object v, int r, int c) {
            BigDecimal d;
            try {
                d = v instanceof BigDecimal ? (BigDecimal) v : FormatNumber.parseDezimal(String.valueOf(v));
            } catch (Exception e) {
                return;
            }
            if (d == null || d.signum() < 0) {
                return;
            }
            ProductlistSubItem s = rows.get(r);
            if (c == QTY) {
                s.setQuantityvalue(d.doubleValue());
            } else if (c == PURCHASE) {
                s.setInternalvalue(d.doubleValue());
            } else if (c == SALE) {
                s.setExternalvalue(d.doubleValue());
            }
            fireTableRowsUpdated(r, r);
            updateTotals();
        }

        void add(ProductlistSubItem s, String number) {
            rows.add(s);
            numbers.add(number == null ? "" : number);
            fireTableDataChanged();
        }

        void remove(int r) {
            rows.remove(r);
            numbers.remove(r);
            fireTableDataChanged();
        }

        void clear() {
            rows.clear();
            numbers.clear();
            fireTableDataChanged();
        }

        List<ProductlistSubItem> rows() {
            return rows;
        }
    }

    /**
     * @param q
     * @return 100, 2,5 (no ",00" for whole quantities)
     */
    static String quantity(double q) {
        BigDecimal b = bd(q);
        return b.stripTrailingZeros().scale() <= 0 ? b.setScale(0, RoundingMode.HALF_UP).toPlainString() : FormatNumber.formatDezimal(b);
    }

    private static BigDecimal bd(double d) {
        return BigDecimal.valueOf(d);
    }

    /**
     * @param p
     * @return a new component for the product (quantity 1)
     */
    static ProductlistSubItem component(Product p) {
        ProductlistSubItem s = new ProductlistSubItem();
        s.setIDS(-1);
        s.setOriginalproductsids(p.__getIDS());
        s.setCname(p.__getCname());
        s.setDescription(p.__getDescription() == null ? "" : p.__getDescription());
        s.setMeasure(p.__getMeasure() == null ? "" : p.__getMeasure());
        s.setLinkurl(p.__getUrl() == null ? "" : p.__getUrl());
        s.setQuantityvalue(1);
        s.setCountvalue(1);
        s.setInternalvalue(p.__getInternalnetvalue().doubleValue());
        s.setExternalvalue(p.__getExternalnetvalue().doubleValue());
        s.setTaxpercentvalue(Tax.getTaxValue(p.__getTaxids()).doubleValue());
        return s;
    }

    private static String numberOf(int productId) {
        try {
            return ((Product) DatabaseObject.getObject(Context.getProduct(), productId)).__getCnumber();
        } catch (Exception e) {
            return "";
        }
    }

    private void addProduct() {
        DatabaseObject o = Search2.showSearchFor(Context.getProduct());
        if (o instanceof Product) {
            paste(o);
        }
    }

    private void removeSelected() {
        stopEditing();
        int[] sel = table.getSelectedRows();
        for (int i = sel.length - 1; i >= 0; i--) {
            model.remove(table.convertRowIndexToModel(sel[i]));
        }
        updateTotals();
    }

    private void stopEditing() {
        if (table.isEditing()) {
            table.getCellEditor().stopCellEditing();
        }
    }

    private BigDecimal sum(boolean sale) {
        BigDecimal s = BigDecimal.ZERO;
        for (int r = 0; r < model.getRowCount(); r++) {
            s = s.add((BigDecimal) model.getValueAt(r, sale ? ComponentsModel.SUM_SALE : ComponentsModel.SUM_PURCHASE));
        }
        return s;
    }

    private void updateTotals() {
        purchaseTotal.setText(FormatNumber.formatLokalCurrency(sum(false)));
        saleTotal.setText(FormatNumber.formatLokalCurrency(sum(true)));
        BigDecimal shown = parsedPrice();
        if (shown == null || autoPrice == null || shown.compareTo(autoPrice) == 0) {
            autoPrice = sum(true);
            price.setText(FormatNumber.formatDezimal(autoPrice));
        }
    }

    private BigDecimal parsedPrice() {
        try {
            String t = price.getText();
            return t == null || t.trim().isEmpty() ? null : FormatNumber.parseDezimal(t);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Shows the price of the product; if it differs from the sales total, it
     * no longer follows it
     */
    private void showProductPrice() {
        autoPrice = null;
        if (product != null && product.__getExternalnetvalue().compareTo(sum(true)) != 0) {
            price.setText(FormatNumber.formatDezimal(product.__getExternalnetvalue()));
            autoPrice = sum(true);
        } else {
            price.setText("");
        }
        updateTotals();
    }

    /**
     * @return the price of the BOM product: as typed, else the sales total
     */
    public BigDecimal getPrice() {
        BigDecimal v = parsedPrice();
        return v == null ? sum(true) : v;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="The BOM product">
    /**
     * @return the product made from this list, null if none
     */
    private Product findProduct() {
        if (dataOwner == null || !dataOwner.isExisting()) {
            return null;
        }
        try {
            ArrayList<DatabaseObject> l = DatabaseObject.getObjects(Context.getProduct(), new QueryCriteria("productlistsids", dataOwner.__getIDS()));
            return l.isEmpty() ? null : (Product) l.get(0);
        } catch (NodataFoundException e) {
            return null;
        }
    }

    private void updateProduct() {
        product = findProduct();
        productButton.setText(product == null ? Messages.BOM_PRODUCT_CREATE.toString() : Messages.BOM_PRODUCT_UPDATE.toString());
        openProduct.setVisible(product != null);
        productInfo.setText(product == null ? Messages.BOM_PRODUCT_NONE.toString()
                : String.format(Messages.BOM_PRODUCT_LINKED.toString(), product.__getCnumber() + " " + product.__getCname(),
                        FormatNumber.formatLokalCurrency(product.__getExternalnetvalue())));
        productInfo.setForeground(ModernTheme.muted());
    }

    /**
     * Saves the list, then creates or updates its product: name of the list,
     * the price, purchase price = purchase total, the tax rate of the largest
     * component, the components in the description
     */
    private void saveProduct() {
        stopEditing();
        if (model.getRowCount() == 0) {
            Popup.notice(Messages.BOM_EMPTY);
            return;
        }
        //the price as shown now: saving the list shows the record again (with the old product price)
        BigDecimal newPrice = getPrice();
        if (!save()) {
            return;
        }
        Product p = findProduct();
        if (p == null) {
            p = new Product();
            p.setInttype(Product.TYPE_PRODUCT);
            p.setIntinventorytype(0);
            p.setDateadded(new Date());
            p.setGroupsids(dataOwner.__getGroupsids());
            p.setProductlistsids(dataOwner.__getIDS());
        }
        p.setCname(dataOwner.__getCname());
        p.setExternalnetvalue(newPrice);
        p.setInternalnetvalue(sum(false));
        p.setTaxids(Tax.getTaxId(mainTaxRate()));
        p.setMeasure(Messages.BOM_UNIT.toString());
        p.setDescription(description());
        if (p.save()) {
            mpv5.YabsViewProxy.instance().addMessage(String.format(Messages.BOM_PRODUCT_SAVED.toString(), p.__getCnumber() + " " + p.__getCname()), Color.GREEN);
        }
        updateProduct();
        showProductPrice();
    }

    private BigDecimal mainTaxRate() {
        BigDecimal rate = BigDecimal.ZERO;
        BigDecimal biggest = BigDecimal.valueOf(-1);
        for (int r = 0; r < model.getRowCount(); r++) {
            BigDecimal s = (BigDecimal) model.getValueAt(r, ComponentsModel.SUM_SALE);
            if (s.compareTo(biggest) > 0) {
                biggest = s;
                rate = (BigDecimal) model.getValueAt(r, ComponentsModel.TAX);
            }
        }
        return rate;
    }

    /**
     * @return the components, one per line: quantity unit name
     */
    private String description() {
        StringBuilder b = new StringBuilder();
        for (ProductlistSubItem s : model.rows()) {
            if (b.length() > 0) {
                b.append('\n');
            }
            b.append(quantity(s.__getQuantityvalue()));
            if (s.__getMeasure() != null && !s.__getMeasure().isEmpty()) {
                b.append(' ').append(s.__getMeasure());
            }
            b.append(' ').append(s.getCname());
        }
        return b.toString();
    }

    /**
     * Saves the list and adds its components to an invoice (chosen in a search)
     */
    private void toInvoice() {
        stopEditing();
        if (model.getRowCount() == 0) {
            Popup.notice(Messages.BOM_EMPTY);
            return;
        }
        if (!save()) {
            return;
        }
        final Item i = (Item) Search2.showSearchFor(Context.getInvoice());
        if (i != null) {
            mpv5.YabsViewProxy.instance().addTab(i);
            SwingUtilities.invokeLater(() -> mpv5.YabsViewProxy.instance().getCurrentTab().paste(dataOwner));
        }
    }

    /**
     * Saves the list like the save button
     *
     * @return true if saved
     */
    private boolean save() {
        mpv5.ui.toolbars.RecordActions.save(this);
        return dataOwner.isExisting();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="DataPanel">
    public String cname_;
    public String description_;
    public int intaddedby_;
    public int ids_;
    public int groupsids_ = 1;
    public Date dateadded_;
    public Group group_;

    @Override
    public DatabaseObject getDataOwner() {
        return dataOwner;
    }

    @Override
    public void setDataOwner(DatabaseObject object, boolean populate) {
        dataOwner = (ProductList) object;
        if (populate) {
            dataOwner.setPanelData(this);
            exposeData();
            model.clear();
            if (dataOwner.isExisting()) {
                try {
                    for (ProductlistSubItem s : ProductlistSubItem.getList(dataOwner.__getIDS())) {
                        model.add(s, numberOf(s.__getOriginalproductsids()));
                    }
                } catch (NodataFoundException ex) {
                    Log.Debug(this, ex.getMessage());
                }
            }
            updateProduct();
            showProductPrice();
            setTabTitle(dataOwner.__getCname());
            tb.setFavourite(mpv5.db.objects.Favourite.isFavourite(object));
            tb.setEditable(!object.isReadOnly());
            if (object.isReadOnly()) {
                Popup.notice(Messages.LOCKED_BY);
            }
            validate();
        }
    }

    @Override
    public void showRequiredFields() {
        TextFieldUtils.blinkerRed(listname);
        listname.requestFocus();
    }

    @Override
    public boolean collectData() {
        stopEditing();
        cname_ = listname.getText().trim();
        if (cname_.isEmpty()) {
            showRequiredFields();
            return false;
        }
        description_ = notes.getText();
        if (groupnameselect.getSelectedItem() != null) {
            try {
                groupsids_ = Integer.valueOf(groupnameselect.getSelectedItem().getId());
            } catch (NumberFormatException e) {
                groupsids_ = 1;
            }
        }
        if (dateadded_ == null) {
            dateadded_ = new Date();
        }
        if (intaddedby_ <= 0) {
            intaddedby_ = User.getCurrentUser().__getIDS();
        }
        return true;
    }

    @Override
    public void exposeData() {
        listname.setText(cname_);
        notes.setText(description_ == null ? "" : description_);
        try {
            groupnameselect.setModel(DatabaseObject.getObject(Context.getGroup(), groupsids_ > 0 ? groupsids_ : 1));
        } catch (Exception ex) {
            Log.Debug(this, ex.getMessage());
        }
    }

    @Override
    public final void refresh() {
        SwingUtilities.invokeLater(() -> {
            if (groupnameselect.getSelectedItem() == null) {
                try {
                    groupnameselect.setModel(MPComboBoxModelItem.toModel(DatabaseObject.getObject(Context.getGroup(), User.getCurrentUser().__getGroupsids())));
                    groupnameselect.setSelectedIndex(0);
                } catch (Exception e) {
                    Log.Debug(this, e);
                }
            }
        });
    }

    @Override
    public void paste(DatabaseObject... dbos) {
        for (DatabaseObject dbo : dbos) {
            if (dbo.getContext().equals(Context.getProduct())) {
                Product p = (Product) dbo;
                if (dataOwner != null && dataOwner.isExisting() && p.__getProductlistsids() == dataOwner.__getIDS()) {
                    //its own product
                    mpv5.YabsViewProxy.instance().addMessage(Messages.NOT_POSSIBLE.toString() + Messages.ACTION_PASTE.toString(), Color.YELLOW);
                    continue;
                }
                model.add(component(p), p.__getCnumber());
            } else if (dbo.getContext().equals(Context.getProductList())) {
                setDataOwner(dbo, true);
            } else {
                mpv5.YabsViewProxy.instance().addMessage(Messages.NOT_POSSIBLE.toString() + Messages.ACTION_PASTE.toString(), Color.YELLOW);
            }
        }
        updateTotals();
    }

    @Override
    public void showSearchBar(boolean show) {
        leftpane.removeAll();
        if (show) {
            leftpane.add(sp, BorderLayout.CENTER);
            sp.search();
        }
        validate();
    }

    @Override
    public void actionAfterSave() {
        saveComponents();
    }

    @Override
    public void actionAfterCreate() {
        //saved as a new list: new components
        for (ProductlistSubItem s : model.rows()) {
            s.setIDS(-1);
        }
        saveComponents();
    }

    /**
     * Saves the components and deletes the removed ones
     */
    private void saveComponents() {
        stopEditing();
        int list = dataOwner.__getIDS();
        Set<Integer> kept = new HashSet<Integer>();
        int pos = 0;
        for (ProductlistSubItem s : model.rows()) {
            s.setProductlistsids(list);
            s.setCountvalue(++pos);
            s.setTotalnetvalue(s.__getQuantityvalue() * s.__getExternalvalue());
            s.setTotalbrutvalue(s.__getQuantityvalue() * s.__getExternalvalue() * (1 + s.__getTaxpercentvalue() / 100));
            if (!s.isExisting()) {
                s.setDateadded(new Date());
                s.setGroupsids(dataOwner.__getGroupsids());
            }
            if (s.save(true)) {
                kept.add(s.__getIDS());
            }
        }
        try {
            for (ProductlistSubItem old : ProductlistSubItem.getList(list)) {
                if (!kept.contains(old.__getIDS())) {
                    //quietly: "<product>: removed from database" would read as if the product was deleted
                    mpv5.db.common.QueryHandler.instanceOf().clone(Context.getProductListItems()).delete(new QueryCriteria("ids", old.__getIDS()));
                }
            }
        } catch (NodataFoundException ex) {
            Log.Debug(this, ex.getMessage());
        }
    }

    @Override
    public void actionBeforeCreate() {
    }

    @Override
    public void actionBeforeSave() {
    }

    @Override
    public void mail() {
    }

    @Override
    public void print() {
        try {
            table.print();
        } catch (java.awt.print.PrinterException ex) {
            Log.Debug(this, ex);
        }
    }

    @Override
    public void changeSelection(MPComboBoxModelItem to, Context c) {
    }
    // </editor-fold>
}
