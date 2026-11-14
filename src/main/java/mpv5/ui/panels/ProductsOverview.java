/*
 *  This file is part of YaBS.
 *
 *      YaBS is free software: you can redistribute it and/or modify
 *      it under the terms of the GNU General Public License as published by
 *      the Free Software Foundation, either version 3 of the License, or
 *      (at your option) any later version.
 *
 *      YaBS is distributed in the hope that it will be useful,
 *      but WITHOUT ANY WARRANTY; without even the implied warranty of
 *      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *      GNU General Public License for more details.
 *
 *      You should have received a copy of the GNU General Public License
 *      along with YaBS.  If not, see <http://www.gnu.org/licenses/>.
 */
package mpv5.ui.panels;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.tree.DefaultMutableTreeNode;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.db.common.NodataFoundException;
import mpv5.db.common.QueryCriteria2;
import mpv5.db.common.QueryHandler;
import mpv5.db.common.QueryParameter;
import mpv5.db.objects.Product;
import mpv5.db.objects.ProductGroup;
import mpv5.globals.Messages;
import mpv5.logging.Log;
import mpv5.ui.beans.LabeledCombobox;
import mpv5.ui.beans.PrinitingComboBox;
import mpv5.ui.misc.DragNDropTreeForGroups;
import mpv5.ui.misc.DragTableHandlerForDBOs;
import mpv5.ui.misc.MPTable;
import mpv5.ui.misc.TableViewPersistenceHandler;
import mpv5.ui.modern.Applets;
import mpv5.ui.modern.Card;
import mpv5.ui.modern.ChipToggle;
import mpv5.ui.modern.ModernTheme;
import mpv5.ui.modern.PrimaryButton;
import mpv5.utils.models.MPTableModel;
import mpv5.utils.tables.TableFormat;
import mpv5.utils.trees.TreeFormat;

/**
 * Products ("Artikel"): one view for what were "All products" and "Product
 * overview". Search, kind (products / services), supplier and manufacturer,
 * the product groups at the left (click: only this group; drag products onto
 * a group to assign them), the list with the preview of the selected product,
 * "New product" as the main action and printing. Double click opens a
 * product, Enter makes a new invoice with the selected products.
 */
public class ProductsOverview extends JPanel implements ListPanel {

    private static final long serialVersionUID = 2L;
    private final JTextField search = ModernTheme.roomy(new JTextField());
    private final ChipToggle kindAll = new ChipToggle(Messages.PRODUCTS_KIND_ALL.toString());
    private final ChipToggle kindProducts = new ChipToggle(Messages.PRODUCTS_KIND_PRODUCTS.toString());
    private final ChipToggle kindServices = new ChipToggle(Messages.PRODUCTS_KIND_SERVICES.toString());
    private final LabeledCombobox supplier = new LabeledCombobox();
    private final LabeledCombobox manufacturer = new LabeledCombobox();
    private final DragNDropTreeForGroups gtree = new DragNDropTreeForGroups(1);
    private final JTable listtable = new MPTable(this);
    private final JLabel count = new JLabel();
    private final PrinitingComboBox printing = new PrinitingComboBox();
    private final Timer typing = new Timer(300, e -> refresh());
    private boolean loading;

    public ProductsOverview() {
        super(new BorderLayout(ModernTheme.px(12), ModernTheme.px(12)));
        setName("productsoverview");
        setBackground(ModernTheme.background());
        setBorder(ModernTheme.padding(12, 14));
        typing.setRepeats(false);
        loading = true;
        add(header(), BorderLayout.NORTH);
        add(groups(), BorderLayout.WEST);
        add(listCard(), BorderLayout.CENTER);
        fillTree();
        loading = false;
        refresh();
    }

    // ------------------------------------------------------------------ layout
    private JComponent header() {
        JLabel title = new JLabel(Messages.PRODUCTS_TITLE.toString());
        title.setFont(ModernTheme.font(9, Font.BOLD));
        count.setForeground(ModernTheme.muted());
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, ModernTheme.px(10), 0));
        left.setOpaque(false);
        left.add(title);
        left.add(count);

        //the main action: a new product (blue, Shift+Enter)
        JButton add = new PrimaryButton(Messages.NEW_PRODUCT.toString(), "/images/16/add.png");
        add.addActionListener(e -> mpv5.YabsViewProxy.instance().getIdentifierView()
                .addTab(DatabaseObject.getObject(Context.getProduct()), Messages.NEW_PRODUCT));
        printing.init(listtable);
        printing.setOpaque(false);
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, ModernTheme.px(8), 0));
        right.setOpaque(false);
        right.add(printing);
        right.add(add);

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(left, BorderLayout.WEST);
        top.add(right, BorderLayout.EAST);

        //filters
        search.putClientProperty("JTextField.placeholderText", Messages.PRODUCTS_SEARCH.toString());
        search.putClientProperty("JTextField.showClearButton", true);
        search.setToolTipText(Messages.PRODUCTS_SEARCH_TIP.toString());
        search.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                typing.restart();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                typing.restart();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                typing.restart();
            }
        });
        search.addActionListener(e -> refresh());
        ButtonGroup g = new ButtonGroup();
        JPanel chips = new JPanel(new FlowLayout(FlowLayout.LEFT, ModernTheme.px(6), 0));
        chips.setOpaque(false);
        for (ChipToggle c : new ChipToggle[]{kindAll, kindProducts, kindServices}) {
            g.add(c);
            chips.add(c);
            c.addActionListener(e -> refresh());
        }
        kindAll.setSelected(true);
        for (LabeledCombobox c : new LabeledCombobox[]{supplier, manufacturer}) {
            c.setSearchEnabled(true);
            c.setEditable(true);
            c.getComboBox().addActionListener(e -> refresh());
        }
        ModernTheme.roomyCombo(supplier);
        ModernTheme.roomyCombo(manufacturer);
        supplier.setContext(Context.getSupplier());
        manufacturer.setContext(Context.getManufacturer());
        supplier.setSelectedIndex(-1);
        manufacturer.setSelectedIndex(-1);
        JButton reset = new JButton(Messages.PRODUCTS_RESET.toString());
        reset.putClientProperty("JButton.buttonType", "toolBarButton");
        reset.setForeground(ModernTheme.accent());
        reset.addActionListener(e -> resetFilters());

        Card filters = new Card(null)
                .row(Card.field(Messages.PRODUCTS_SEARCH_LABEL.toString(), search, 3), Card.field(Messages.PRODUCTS_KIND.toString(), chips, 2))
                .row(Card.field(Messages.PRODUCTS_SUPPLIER.toString(), supplier.getComboBox()),
                        Card.field(Messages.PRODUCTS_MANUFACTURER.toString(), manufacturer.getComboBox()),
                        Card.field(null, reset));

        JPanel p = new JPanel(new BorderLayout(0, ModernTheme.px(10)));
        p.setOpaque(false);
        p.add(top, BorderLayout.NORTH);
        p.add(filters, BorderLayout.CENTER);
        return p;
    }

    private JComponent groups() {
        gtree.setLargeModel(true);
        gtree.setContainerToNotify(this);
        gtree.addTreeSelectionListener(e -> refresh());
        JScrollPane sp = new JScrollPane(gtree);
        sp.setBorder(BorderFactory.createLineBorder(ModernTheme.border()));
        Card c = new Card(Messages.PRODUCTS_GROUPS.toString())
                .addFull(sp, 1)
                .addFull(Applets.hint(Messages.PRODUCTS_GROUPS_HINT.toString()));
        c.setPreferredSize(new Dimension(ModernTheme.px(240), ModernTheme.px(300)));
        return c;
    }

    private JComponent listCard() {
        new DragTableHandlerForDBOs(listtable, Context.getProduct());
        listtable.setAutoCreateRowSorter(true);
        listtable.setDragEnabled(true);
        listtable.setFillsViewportHeight(true);
        listtable.setSelectionMode(javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        ModernTheme.style(listtable);
        ((MPTable) listtable).setPersistanceHandler(new TableViewPersistenceHandler((MPTable) listtable, this));
        //double click opens the product
        listtable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    openSelected();
                }
            }
        });
        JScrollPane sp = new JScrollPane(listtable);
        sp.setBorder(BorderFactory.createLineBorder(ModernTheme.border()));
        JPanel host = new JPanel(new BorderLayout());
        host.setOpaque(false);
        host.add(sp, BorderLayout.CENTER);
        //the preview of the selected product at the right, its "New invoice" button above the list
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, ModernTheme.px(6), 0));
        buttons.setOpaque(false);
        ProductPreview.attach(listtable, 0, host, buttons);
        JPanel north = new JPanel(new BorderLayout());
        north.setOpaque(false);
        north.add(buttons, BorderLayout.WEST);
        host.add(north, BorderLayout.NORTH);
        return new Card(null).addFull(host, 1);
    }

    // ------------------------------------------------------------------ data
    private void fillTree() {
        ArrayList<ProductGroup> data = null;
        try {
            data = DatabaseObject.getObjects(Context.getProductGroup());
        } catch (NodataFoundException ex) {
            Log.Debug(this, ex.getMessage());
        }
        ProductGroup g;
        try {
            g = (ProductGroup) DatabaseObject.getObject(Context.getProductGroup(), 1);
        } catch (NodataFoundException ex) {
            g = new ProductGroup(Messages.GROUPNAMES.toString());
            g.setIDS(-1);
        }
        gtree.setModel(ProductGroup.toTreeModel(data, g));
        SwingUtilities.invokeLater(() -> TreeFormat.expandTree(gtree));
    }

    private void resetFilters() {
        loading = true;
        try {
            search.setText("");
            kindAll.setSelected(true);
            supplier.setSelectedIndex(-1);
            manufacturer.setSelectedIndex(-1);
            gtree.clearSelection();
        } finally {
            loading = false;
        }
        refresh();
    }

    private void openSelected() {
        int v = listtable.getSelectedRow();
        if (v < 0) {
            return;
        }
        try {
            Object id = listtable.getModel().getValueAt(listtable.convertRowIndexToModel(v), 0);
            mpv5.YabsViewProxy.instance().getIdentifierView().addTab(
                    DatabaseObject.getObject(Context.getProduct(), Integer.valueOf(String.valueOf(id))));
        } catch (Exception ex) {
            Log.Debug(this, ex.getMessage());
        }
    }

    /**
     * The list again with all filters: search (every word; "-word" excludes),
     * kind, supplier, manufacturer and the selected product group
     */
    @Override
    public void refresh() {
        if (loading) {
            return;
        }
        typing.stop();
        QueryCriteria2 qc = new QueryCriteria2();
        String text = search.getText() == null ? "" : search.getText().trim();
        if (!text.isEmpty()) {
            List<List<QueryParameter>> ps = new ArrayList<List<QueryParameter>>();
            List<QueryParameter> not = new ArrayList<QueryParameter>();
            for (String word : text.split("\\s+")) {
                ArrayList<QueryParameter> psx = new ArrayList<QueryParameter>();
                ps.add(psx);
                for (String en : new Product().getStringVars()) {
                    if (!word.startsWith("-")) {
                        psx.add(new QueryParameter(Context.getProduct(), en, word, QueryParameter.LIKE));
                    } else if (word.length() > 1) {
                        not.add(new QueryParameter(Context.getProduct(), en, word.substring(1), QueryParameter.NOTLIKE));
                    }
                }
            }
            for (List<QueryParameter> p : ps) {
                if (!p.isEmpty()) {
                    qc.or(p);
                }
            }
            if (!not.isEmpty()) {
                qc.and(not);
            }
        }
        if (!kindAll.isSelected()) {
            qc.and(new QueryParameter(Context.getProduct(), "inttype",
                    kindServices.isSelected() ? Product.TYPE_SERVICE : Product.TYPE_PRODUCT, QueryParameter.EQUALS));
        }
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) gtree.getLastSelectedPathComponent();
        if (node != null && !node.isRoot() && node.getUserObject() instanceof ProductGroup) {
            qc.and(new QueryParameter(Context.getProduct(), "productgroupsids",
                    ((ProductGroup) node.getUserObject()).__getIDS(), QueryParameter.EQUALS));
        }
        Integer s = selectedId(supplier);
        if (s != null) {
            qc.and(new QueryParameter(Context.getProduct(), "suppliersids", s, QueryParameter.EQUALS));
        }
        Integer m = selectedId(manufacturer);
        if (m != null) {
            qc.and(new QueryParameter(Context.getProduct(), "manufacturersids", m, QueryParameter.EQUALS));
        }
        Context c = Context.getProduct();
        c.addReference(Context.getContact().getDbIdentity(), "ids", "suppliersids");
        c.addReference(Context.getGroup());
        String[] header = ProductPreview.productColumns();
        Object[][] data;
        try {
            data = QueryHandler.instanceOf().clone(c).select(Context.DETAILS_PRODUCTS, qc).getData();
        } catch (NodataFoundException ex) {
            data = new Object[0][header.length];
        }
        listtable.setModel(new MPTableModel(data, header));
        //the internal id is not shown
        TableFormat.stripColumn(listtable, 0);
        count.setText(String.format(Messages.PRODUCTS_COUNT.toString(), data.length));
    }

    private static Integer selectedId(LabeledCombobox c) {
        try {
            if (c.getComboBox().getSelectedIndex() >= 0 && c.getSelectedItem() != null) {
                return Integer.valueOf(c.getSelectedItem().getId());
            }
        } catch (Exception e) {
            //typed text that is no contact
        }
        return null;
    }

    @Override
    public void flush() {
        refresh();
    }
}
