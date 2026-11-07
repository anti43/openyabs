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
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.KeyboardFocusManager;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.db.common.NodataFoundException;
import mpv5.db.objects.Contact;
import mpv5.db.objects.Favourite;
import mpv5.db.objects.Group;
import mpv5.db.objects.Product;
import mpv5.db.objects.ProductGroup;
import mpv5.db.objects.ProductList;
import mpv5.db.objects.User;
import mpv5.globals.Constants;
import mpv5.globals.Messages;
import mpv5.handler.ProductService;
import mpv5.handler.ProductService.Tier;
import mpv5.handler.ProductService.Usage;
import mpv5.handler.TemplateHandler;
import mpv5.logging.Log;
import mpv5.ui.beans.LabeledCombobox;
import mpv5.ui.beans.LabeledTextField;
import mpv5.ui.beans.MPCBSelectionChangeReceiver;
import mpv5.ui.beans.PrinitingComboBox;
import mpv5.ui.dialogs.BigPopup;
import mpv5.ui.dialogs.DialogForFile;
import mpv5.ui.dialogs.Notificator;
import mpv5.ui.dialogs.Popup;
import mpv5.ui.dialogs.subcomponents.ControlPanel_Groups;
import mpv5.ui.misc.MPTable;
import mpv5.ui.misc.TableViewPersistenceHandler;
import mpv5.ui.modern.Avatar;
import mpv5.ui.modern.Card;
import mpv5.ui.modern.ChipToggle;
import mpv5.ui.modern.ModernTheme;
import mpv5.ui.modern.ResponsiveGrid;
import mpv5.ui.modern.WrapLayout;
import mpv5.ui.popups.FileTablePopUp;
import mpv5.ui.toolbars.ModernCommandBar;
import mpv5.utils.date.DateConverter;
import mpv5.utils.export.Export;
import mpv5.utils.files.FileDirectoryHandler;
import mpv5.utils.jobs.Job;
import mpv5.utils.models.MPComboBoxModelItem;
import mpv5.utils.models.MPTableModel;
import mpv5.utils.numberformat.FormatNumber;
import mpv5.utils.tables.TableFormat;
import mpv5.utils.ui.TextFieldUtils;

/**
 * The product view in the modern layout: a header with image, name and type,
 * the product data in cards which flow into one, two or three columns
 * depending on the width, and below the offers, orders, invoices and credit
 * notes the product was used in.
 * <p>
 * Same functions as {@link ProductPanel} (the classic view); which one is used
 * is decided by {@link mpv5.YabsViewProxy}. The logic which is not about the
 * screen is in {@link ProductService}.
 * <p>
 * The public fields ending with an underscore are filled from / copied to the
 * product by name, so they must keep their names.
 */
public class ModernProductPanel extends ModernDataPanel implements MPCBSelectionChangeReceiver, ExportablePanel {

    private static final long serialVersionUID = 1L;
    private Product dataOwner;
    private final int type;
    private final ModernCommandBar tb;
    private final SearchPanel sp;
    private final JPanel leftpane = new JPanel(new BorderLayout());
    private String old_cnumber = "";
    //header
    private final Avatar avatar = new Avatar();
    private final JLabel displayname = new JLabel();
    private final JLabel meta = new JLabel();
    private final ChipToggle productChip = new ChipToggle(Messages.TYPE_PRODUCT.toString());
    private final ChipToggle serviceChip = new ChipToggle(Messages.TYPE_SERVICE.toString());
    private final JButton previewButton = action(text("ProductPanel.button_preview.text"));
    private final JButton orderButton = action(text("ProductPanel.button_order.text"));
    private final JButton listButton = action(text("ProductPanel.button_listedit.text"));
    private final PrinitingComboBox printing = new PrinitingComboBox();
    //general
    private final LabeledTextField cname = field();
    private final LabeledTextField cnumber = field();
    private final LabeledTextField unit = field();
    private final LabeledTextField ean = field();
    private final LabeledTextField reference = field();
    private final LabeledTextField url = field();
    //prices
    private final LabeledTextField netvalue = field();
    private final LabeledTextField markup = field();
    private final LabeledTextField extvalue = field();
    private final LabeledCombobox selecttax = ModernTheme.plain(new LabeledCombobox());
    private final LabeledTextField grossvalue = field();
    private boolean calculating;
    //classification
    private final LabeledCombobox familyselect = ModernTheme.plain(new LabeledCombobox());
    private final JLabel path = new JLabel();
    private final LabeledCombobox groupnameselect = ModernTheme.plain(new LabeledCombobox());
    private final JTextArea description = new JTextArea(5, 20);
    //manufacturer, suppliers
    private final LabeledCombobox manufacturer = ModernTheme.plain(new LabeledCombobox());
    private final JPanel suppliers = new JPanel();
    private final ButtonGroup defaultSupplier = new ButtonGroup();
    //inventory
    private final LabeledTextField stack = field();
    private final LabeledTextField threshold = field();
    private final JCheckBox inventoryDisabled = new JCheckBox(text("ProductPanel.inventoryDisabled.text"));
    //staggered prices
    private final JPanel tiers = new JPanel();
    //image, files
    private final JLabel image = new JLabel();
    private final JTable filetable = tooltipTable(this);
    private final FileTablePopUp filePopup = new FileTablePopUp();
    private File currentImageFile;
    private String lastimage;
    //documents with the product
    private final JTable dataTable = tooltipTable(this);
    private final JTextField filter = new JTextField(14);
    private final JLabel summary = new JLabel();
    private final Map<Usage, ChipToggle> usageButtons = new EnumMap<Usage, ChipToggle>(Usage.class);
    private final ButtonGroup usageGroup = new ButtonGroup();
    private Usage usage;
    // <editor-fold defaultstate="collapsed" desc="Data fields, filled by name from / copied to the product">
    public String cname_;
    public String cnumber_;
    public String description_;
    public String hierarchiepath_;
    public int intaddedby_;
    public int ids_;
    public Date dateadded_;
    public Group group_;
    public int productgroupsids_ = 1;
    public int productlistsids_ = 0;
    public int taxids_;
    public int inttype_;
    public int manufacturersids_;
    public int suppliersids_;
    public BigDecimal externalnetvalue_;
    public BigDecimal internalnetvalue_;
    public String measure_ = "";
    public String url_ = "";
    public String ean_ = "";
    public String defaultimage_ = "";
    public String reference_ = "";
    public BigDecimal stockvalue_;
    public BigDecimal thresholdvalue_;
    public int intinventorytype_;
    // </editor-fold>

    /**
     * @param context the product context
     * @param type Product.TYPE_PRODUCT or TYPE_SERVICE, -1 if not known yet
     */
    public ModernProductPanel(Context context, int type) {
        super(new BorderLayout());
        setName("productpanel");
        this.type = type;
        sp = new SearchPanel(context, this);
        dataOwner = new Product();
        dataOwner.setInttype(type);
        inttype_ = type;

        initFields();

        JPanel content = new JPanel(new BorderLayout());
        content.setOpaque(false);
        content.add(createHeader(), BorderLayout.NORTH);
        JSplitPane split = split(scroll(createOverview()), createUsagePanel());
        content.add(split, BorderLayout.CENTER);
        add(content, BorderLayout.CENTER);
        add(leftpane, BorderLayout.WEST);
        //the toolbar calls showSearchBar(), so it is created last
        tb = new ModernCommandBar(this);
        add(tb, BorderLayout.NORTH);
        //the actions of a product, next to the record actions
        tb.addAction(previewButton, null);
        tb.addAction(orderButton, null);
        tb.addAction(listButton, null);
        tb.addAction(printing, null);
        //Ctrl+F: find in the documents with the product
        tb.setFindAction(() -> {
            filter.requestFocusInWindow();
            filter.selectAll();
        });

        dateadded_ = new Date();
        intaddedby_ = User.getCurrentUser().__getIDS();
        setType(type);
        addSupplierRow(null, true);
        updateHeader();
        refresh();
    }

    /**
     * @param context the product context
     */
    public ModernProductPanel(Context context) {
        this(context, -1);
    }

    // <editor-fold defaultstate="collapsed" desc="Layout">
    private void initFields() {
        cnumber.setSearchOnEnterEnabled(true);
        cnumber.setParent(this);
        cnumber.setSearchField("cnumber");
        cnumber.setContext(Context.getProduct());
        cname.setSearchOnEnterEnabled(true);
        cname.setParent(this);
        cname.setSearchField("cname");
        cname.setContext(Context.getProduct());

        netvalue.set_ValueClass(Double.class);
        extvalue.set_ValueClass(Double.class);
        markup.set_ValueClass(Double.class);
        grossvalue.set_ValueClass(Double.class);
        grossvalue.setEnabled(false);
        stack.set_ValueClass(BigDecimal.class);
        threshold.set_ValueClass(BigDecimal.class);

        selecttax.setSearchEnabled(true);
        selecttax.setContext(Context.getTaxes());
        selecttax.getComboBox().setEditable(false);
        //loaded now (a few rows), so the rate of the product can always be selected
        selecttax.setModel(ProductService.loadTaxes());
        selecttax.getComboBox().addActionListener(e -> updateGross());

        familyselect.setContext(Context.getProductGroup());
        familyselect.setSearchEnabled(true);
        familyselect.getComboBox().addActionListener(e -> updatePath());
        groupnameselect.setContext(Context.getGroup());
        groupnameselect.setSearchEnabled(true);
        manufacturer.setContext(Context.getManufacturer());
        manufacturer.setSearchEnabled(true);
        manufacturer.setEditable(true);
        path.setFont(ModernTheme.font(-1, Font.PLAIN));
        path.setForeground(ModernTheme.muted());

        description.setLineWrap(true);
        description.setWrapStyleWord(true);
        ModernTheme.roomy(description);
        //tab moves on to the next field instead of inserting a tab
        description.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, null);
        description.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, null);

        inventoryDisabled.setOpaque(false);
        inventoryDisabled.addItemListener(e -> {
            stack.setEnabled(!inventoryDisabled.isSelected());
            threshold.setEnabled(!inventoryDisabled.isSelected());
        });

        //prices: markup -> sale price, sale price -> markup, both -> gross
        onChange(netvalue, () -> {
            if (markup.getTextField().getText().trim().length() > 0) {
                applyMarkup();
            } else {
                updateMarkup();
            }
        });
        onChange(markup, this::applyMarkup);
        onChange(extvalue, () -> {
            updateMarkup();
            updateGross();
        });
        onChange(cname, this::updateHeader);

        ButtonGroup types = new ButtonGroup();
        types.add(productChip);
        types.add(serviceChip);
        productChip.addActionListener(e -> setType(Product.TYPE_PRODUCT));
        serviceChip.addActionListener(e -> setType(Product.TYPE_SERVICE));

        previewButton.setEnabled(false);
        orderButton.setEnabled(false);
        listButton.setEnabled(false);
        previewButton.addActionListener(e -> preview(dataOwner.__getInttype()));
        orderButton.addActionListener(e -> preview(Constants.TYPE_PRODUCT_ORDER));
        listButton.addActionListener(e -> editList());
    }

    private void onChange(LabeledTextField f, final Runnable r) {
        f.getTextField().getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                run();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                run();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                run();
            }

            private void run() {
                if (!calculating) {
                    SwingUtilities.invokeLater(r);
                }
            }
        });
    }

    private JComponent createHeader() {
        JPanel header = new JPanel(new BorderLayout(ModernTheme.px(14), 0));
        header.setOpaque(false);
        header.setBorder(ModernTheme.padding(8, 16));
        avatar.setSquare(true);
        header.add(avatar, BorderLayout.WEST);

        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        displayname.setFont(ModernTheme.font(8, Font.BOLD));
        displayname.setForeground(ModernTheme.foreground());
        meta.setFont(ModernTheme.font(-1, Font.PLAIN));
        meta.setForeground(ModernTheme.muted());
        JPanel chips = new JPanel(new FlowLayout(FlowLayout.LEFT, ModernTheme.px(6), 0));
        chips.setOpaque(false);
        chips.add(productChip);
        chips.add(serviceChip);
        //name and the buttons in one line (the buttons go below the name if there is no room)
        JPanel titleRow = new JPanel(new WrapLayout(FlowLayout.LEFT, 0, ModernTheme.px(2)));
        titleRow.setOpaque(false);
        displayname.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, ModernTheme.px(14)));
        titleRow.add(displayname);
        titleRow.add(chips);
        titleRow.setAlignmentX(LEFT_ALIGNMENT);
        meta.setAlignmentX(LEFT_ALIGNMENT);
        center.add(titleRow);
        center.add(meta);
        header.add(center, BorderLayout.CENTER);
        return header;
    }

    private JComponent createOverview() {
        ResponsiveGrid grid = new ResponsiveGrid(340, 3);

        grid.add(new Card(Messages.PRODUCT_SECTION_GENERAL.toString())
                .row(Card.field(text("ProductPanel.cname._Label_1"), cname))
                .row(Card.field(text("ProductPanel.cnumber._Label").replace(":", ""), cnumber, 1),
                        Card.field(text("ProductPanel.unit._Label"), unit, 1))
                .row(Card.field(text("ProductPanel.ean._Label_1"), ean, 1),
                        Card.field(text("ProductPanel.reference._Label"), reference, 1))
                .row(Card.field(text("ProductPanel.url._Label_1"), url)));

        grid.add(new Card(Messages.PRODUCT_SECTION_PRICES.toString())
                .row(Card.field(text("ProductPanel.netvalue._Label_1"), netvalue, 1),
                        Card.field(Messages.PRODUCT_MARKUP.toString(), markup, 1))
                .row(Card.field(text("ProductPanel.extvalue._Label"), extvalue, 1),
                        Card.field(text("ProductPanel.selecttax._Label_1"), selecttax, 1))
                .row(Card.field(Messages.PRODUCT_GROSS.toString(), grossvalue)));

        JButton groupAdmin = link("+", null);
        groupAdmin.setToolTipText(text("ProductPanel.jLabel4.text"));
        groupAdmin.addActionListener(e -> BigPopup.showPopup(this, new ControlPanel_Groups(), null));
        JPanel group = new JPanel(new BorderLayout(ModernTheme.px(4), 0));
        group.setOpaque(false);
        group.add(groupnameselect, BorderLayout.CENTER);
        group.add(groupAdmin, BorderLayout.EAST);
        grid.add(new Card(Messages.PRODUCT_SECTION_CLASSIFICATION.toString())
                .row(Card.field(text("ProductPanel.familyselect._Label"), familyselect))
                .addFull(path)
                .row(Card.field(text("ProductPanel.jLabel4.text").replace(":", ""), group)));

        grid.add(new Card(Messages.PRODUCT_SECTION_DESCRIPTION.toString())
                .addFull(new JScrollPane(description), 1));

        suppliers.setOpaque(false);
        suppliers.setLayout(new BoxLayout(suppliers, BoxLayout.Y_AXIS));
        JButton addSupplier = link(Messages.PRODUCT_ADD_SUPPLIER.toString(), "/images/16/add.png");
        addSupplier.addActionListener(e -> addSupplierRow(null, suppliers.getComponentCount() == 0));
        grid.add(new Card(Messages.PRODUCT_SECTION_SUPPLIERS.toString())
                .row(Card.field(text("ProductPanel.contactname1._Label"), manufacturer))
                .addFull(ModernTheme.fieldLabel(text("ProductPanel.supplierpanel.border.title")))
                .addFull(suppliers)
                .addFull(leftAligned(addSupplier)));

        grid.add(new Card(Messages.PRODUCT_SECTION_STOCK.toString())
                .row(Card.field(text("ProductPanel.stack._Label_1"), stack),
                        Card.field(text("ProductPanel.threshold._Label_1"), threshold))
                .row(Card.field(null, inventoryDisabled)));

        tiers.setOpaque(false);
        tiers.setLayout(new BoxLayout(tiers, BoxLayout.Y_AXIS));
        JButton addTier = link(Messages.PRODUCT_ADD_TIER.toString(), "/images/16/add.png");
        addTier.addActionListener(e -> addTierRow(BigDecimal.ZERO, BigDecimal.ZERO));
        JButton removeTiers = link(text("ProductPanel.jButton2.text"), null);
        removeTiers.addActionListener(e -> removeAllTiers());
        JPanel tierButtons = leftAligned(addTier);
        tierButtons.add(removeTiers);
        grid.add(new Card(Messages.PRODUCT_SECTION_TIERS.toString())
                .addFull(tiers)
                .addFull(tierButtons));

        grid.add(createImageCard());
        return grid;
    }

    private static JPanel leftAligned(JComponent c) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        p.setOpaque(false);
        p.add(c);
        return p;
    }

    private Card createImageCard() {
        image.setHorizontalAlignment(SwingConstants.CENTER);
        image.setForeground(ModernTheme.muted());
        image.setFont(ModernTheme.font(-1, Font.PLAIN));
        image.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        image.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (currentImageFile != null) {
                    FileDirectoryHandler.open(currentImageFile);
                }
            }
        });

        filetable.setAutoCreateRowSorter(true);
        filetable.setDragEnabled(true);
        JMenuItem useAsImage = new JMenuItem(Messages.PRODUCT_SET_IMAGE.toString());
        useAsImage.addActionListener(e -> useSelectedFileAsImage());
        filePopup.addDefaultPopupMenu(filetable);
        filePopup.addSeparator();
        filePopup.add(useAsImage);
        filetable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evt) {
                fileTableClicked(evt);
            }
        });
        JScrollPane files = new JScrollPane(filetable);
        files.setPreferredSize(new java.awt.Dimension(ModernTheme.px(200), ModernTheme.px(110)));

        JButton add = link(null, "/images/16/add.png");
        add.setToolTipText(Messages.ACTION_ADD.toString());
        add.addActionListener(e -> addFile());
        JButton remove = link(null, "/images/16/remove.png");
        remove.setToolTipText(Messages.ACTION_REMOVE.toString());
        remove.addActionListener(e -> deleteFile());
        JButton setImage = link(Messages.PRODUCT_SET_IMAGE.toString(), null);
        setImage.addActionListener(e -> useSelectedFileAsImage());
        JPanel buttons = leftAligned(add);
        buttons.add(remove);
        buttons.add(setImage);

        return new Card(Messages.PRODUCT_SECTION_IMAGE.toString())
                .addFull(image)
                .addFull(files, 1)
                .addFull(buttons);
    }

    private JComponent createUsagePanel() {
        dataTable.setAutoCreateRowSorter(true);
        ((MPTable) dataTable).setPersistanceHandler(new TableViewPersistenceHandler((MPTable) dataTable, this));
        dataTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evt) {
                if (evt.getClickCount() > 1 && dataTable.getSelectedRow() >= 0 && usage != null) {
                    openItem(dataTable.getModel().getValueAt(dataTable.convertRowIndexToModel(dataTable.getSelectedRow()), 0));
                }
            }
        });
        String[] labels = {
            text("ContactPanel.button_offers.text"), text("ContactPanel.button_orders.text"),
            text("ContactPanel.button_bills.text"), text("ContactPanel.button_credits.text")};
        JPanel segments = new JPanel(new FlowLayout(FlowLayout.LEFT, ModernTheme.px(4), 0));
        segments.setOpaque(false);
        for (Usage u : Usage.values()) {
            final Usage us = u;
            ChipToggle b = new ChipToggle(labels[u.ordinal()]);
            b.addActionListener(e -> showUsage(us));
            usageButtons.put(u, b);
            usageGroup.add(b);
            segments.add(b);
        }
        liveFilter(dataTable, filter);
        filter.setToolTipText(text("ContactPanel.filterme._Label"));
        JPanel filterPanel = new JPanel(new BorderLayout(ModernTheme.px(6), 0));
        filterPanel.setOpaque(false);
        filterPanel.add(ModernTheme.fieldLabel(text("ContactPanel.filterme._Label")), BorderLayout.WEST);
        filterPanel.add(filter, BorderLayout.CENTER);
        summary.setFont(ModernTheme.font(-1, Font.PLAIN));
        summary.setForeground(ModernTheme.muted());

        JPanel bar = new JPanel(new BorderLayout(ModernTheme.px(12), ModernTheme.px(6)));
        bar.setOpaque(false);
        bar.add(segments, BorderLayout.CENTER);
        bar.add(filterPanel, BorderLayout.EAST);
        bar.add(summary, BorderLayout.SOUTH);

        Card card = new Card(Messages.CONTACT_SECTION_DOCUMENTS.toString());
        card.addFull(bar);
        card.addFull(new JScrollPane(dataTable), 1);
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(ModernTheme.background());
        p.setBorder(BorderFactory.createEmptyBorder(ModernTheme.px(8), 0, 0, 0));
        p.add(card, BorderLayout.CENTER);
        return p;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Rows of suppliers and staggered prices">
    /**
     * A supplier of the product; the default one is stored in the product,
     * the others as further suppliers
     */
    private final class SupplierRow extends JPanel {

        private static final long serialVersionUID = 1L;
        final LabeledCombobox contact = ModernTheme.plain(new LabeledCombobox());
        final JRadioButton isDefault = new JRadioButton(Messages.PRODUCT_DEFAULT_SUPPLIER.toString());

        SupplierRow(Contact c, boolean def) {
            super(new BorderLayout(ModernTheme.px(6), 0));
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(0, 0, ModernTheme.px(4), 0));
            contact.setSearchEnabled(true);
            contact.setEditable(true);
            contact.setContext(Context.getSupplier());
            if (c != null && c.isExisting()) {
                contact.setModel(c);
            } else {
                contact.setModel(new Object[0][0]);
            }
            isDefault.setOpaque(false);
            isDefault.setSelected(def);
            defaultSupplier.add(isDefault);
            JButton remove = link(null, "/images/16/remove.png");
            remove.setToolTipText(Messages.ACTION_REMOVE.toString());
            remove.addActionListener(e -> removeSupplierRow(this));
            JPanel east = new JPanel(new FlowLayout(FlowLayout.RIGHT, ModernTheme.px(4), 0));
            east.setOpaque(false);
            east.add(isDefault);
            east.add(remove);
            add(contact, BorderLayout.CENTER);
            add(east, BorderLayout.EAST);
        }

        Contact getContact() {
            MPComboBoxModelItem item = contact.getSelectedItem();
            if (item == null) {
                return null;
            }
            try {
                return ProductService.getContact(Integer.parseInt(String.valueOf(item.getId())));
            } catch (NumberFormatException e) {
                return null;
            }
        }
    }

    /**
     * A staggered price: from a minimum quantity on
     */
    private final class TierRow extends JPanel {

        private static final long serialVersionUID = 1L;
        final JSpinner minimum = new JSpinner(new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 1));
        final LabeledTextField price = field();

        TierRow(BigDecimal min, BigDecimal p) {
            super(new BorderLayout(ModernTheme.px(6), 0));
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(0, 0, ModernTheme.px(4), 0));
            minimum.setValue(min.intValue());
            price.set_ValueClass(Double.class);
            price.setText(p);
            JButton remove = link(null, "/images/16/remove.png");
            remove.setToolTipText(Messages.ACTION_REMOVE.toString());
            remove.addActionListener(e -> {
                tiers.remove(this);
                tiers.revalidate();
                tiers.repaint();
            });
            JPanel fields = new JPanel(new java.awt.GridLayout(1, 2, ModernTheme.px(6), 0));
            fields.setOpaque(false);
            fields.add(labeled(Messages.PRODUCT_TIER_FROM.toString(), minimum));
            fields.add(labeled(Messages.PRODUCT_TIER_PRICE.toString(), price));
            add(fields, BorderLayout.CENTER);
            add(remove, BorderLayout.EAST);
        }

        Tier getTier() {
            BigDecimal p;
            try {
                p = FormatNumber.parseDezimal(price.getText());
            } catch (Exception e) {
                p = BigDecimal.ZERO;
            }
            return new Tier(new BigDecimal(String.valueOf(minimum.getValue())), p);
        }
    }

    private static JComponent labeled(String label, JComponent c) {
        JPanel p = new JPanel(new BorderLayout(0, ModernTheme.px(2)));
        p.setOpaque(false);
        p.add(ModernTheme.fieldLabel(label), BorderLayout.NORTH);
        p.add(c, BorderLayout.CENTER);
        return p;
    }

    private void addSupplierRow(Contact c, boolean def) {
        suppliers.add(new SupplierRow(c, def));
        relayout(suppliers);
    }

    private void removeSupplierRow(SupplierRow row) {
        boolean wasDefault = row.isDefault.isSelected();
        defaultSupplier.remove(row.isDefault);
        suppliers.remove(row);
        if (wasDefault && suppliers.getComponentCount() > 0) {
            ((SupplierRow) suppliers.getComponent(0)).isDefault.setSelected(true);
        }
        relayout(suppliers);
    }

    private List<SupplierRow> supplierRows() {
        List<SupplierRow> rows = new ArrayList<SupplierRow>();
        for (Component c : suppliers.getComponents()) {
            rows.add((SupplierRow) c);
        }
        return rows;
    }

    private void addTierRow(BigDecimal min, BigDecimal price) {
        tiers.add(new TierRow(min, price));
        relayout(tiers);
    }

    private void loadTiers() {
        tiers.removeAll();
        for (Tier t : ProductService.loadTiers(dataOwner)) {
            tiers.add(new TierRow(t.minimum, t.price));
        }
        relayout(tiers);
    }

    private void removeAllTiers() {
        ProductService.deleteTiers(dataOwner);
        tiers.removeAll();
        relayout(tiers);
    }

    private static void relayout(JComponent c) {
        //the card height changes: lay out the grid again
        Component grid = SwingUtilities.getAncestorOfClass(ResponsiveGrid.class, c);
        (grid != null ? (JComponent) grid : c).revalidate();
        c.repaint();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="State of the controls">
    private void setType(int t) {
        inttype_ = t;
        productChip.setSelected(t == Product.TYPE_PRODUCT);
        serviceChip.setSelected(t == Product.TYPE_SERVICE);
        //ordering from suppliers is for products only
        orderButton.setVisible(t != Product.TYPE_SERVICE);
        updateHeader();
    }

    private void updateHeader() {
        String name = cname.getTextField().getText();
        if (name == null || name.trim().isEmpty()) {
            name = (inttype_ == Product.TYPE_SERVICE ? Messages.NEW_SERVICE : Messages.NEW_PRODUCT).toString();
        }
        displayname.setText(name);
        avatar.setName(name);
        StringBuilder m = new StringBuilder();
        if (cnumber.getTextField().getText().length() > 0) {
            m.append(cnumber.getTextField().getText()).append("   ·   ");
        }
        if (path.getText() != null && path.getText().length() > 0) {
            m.append(path.getText()).append("   ·   ");
        }
        if (dateadded_ != null) {
            m.append(DateConverter.getDefDateString(dateadded_));
        }
        String by = User.getUsername(intaddedby_);
        if (by != null && by.length() > 0) {
            m.append(" · ").append(by);
        }
        meta.setText(m.toString());
    }

    private void updatePath() {
        MPComboBoxModelItem item = familyselect.getSelectedItem();
        String p = "";
        if (item != null) {
            try {
                ProductGroup g = ProductService.getProductGroup(Integer.parseInt(String.valueOf(item.getId())));
                if (g != null && g.__getHierarchypath() != null) {
                    p = g.__getHierarchypath();
                }
            } catch (NumberFormatException e) {
                Log.Debug(this, e.getMessage());
            }
        }
        path.setText(p);
        updateHeader();
    }

    private static BigDecimal number(LabeledTextField f) {
        String t = f.getTextField().getText();
        if (t == null || t.trim().isEmpty()) {
            return null;
        }
        try {
            return FormatNumber.parseDezimal(t.trim());
        } catch (Exception e) {
            return null;
        }
    }

    private void setNumber(LabeledTextField f, BigDecimal v) {
        calculating = true;
        try {
            f.getTextField().setText(v == null ? "" : FormatNumber.formatDezimal(v));
        } finally {
            calculating = false;
        }
    }

    /**
     * Sale price = cost + markup
     */
    private void applyMarkup() {
        BigDecimal m = number(markup);
        BigDecimal c = number(netvalue);
        if (m != null && c != null) {
            setNumber(extvalue, ProductService.salePrice(c, m));
            updateGross();
        }
    }

    /**
     * Markup from cost and sale price
     */
    private void updateMarkup() {
        setNumber(markup, ProductService.markup(number(netvalue), number(extvalue)));
    }

    private void updateGross() {
        int tax = taxids_;
        if (selecttax.getSelectedItem() != null) {
            try {
                tax = Integer.parseInt(String.valueOf(selecttax.getSelectedItem().getId()));
            } catch (NumberFormatException e) {
                Log.Debug(this, e.getMessage());
            }
        }
        BigDecimal net = number(extvalue);
        try {
            setNumber(grossvalue, ProductService.gross(net == null ? BigDecimal.ZERO : net, tax));
        } catch (Exception e) {
            //no tax rate loaded yet
            Log.Debug(this, e.getMessage());
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Actions">
    private void showUsage(Usage u) {
        usage = u;
        if (u == null) {
            usageGroup.clearSelection();
            dataTable.setModel(new MPTableModel());
            resetFilter(dataTable, filter);
            summary.setText(" ");
            return;
        }
        usageButtons.get(u).setSelected(true);
        MPTableModel m = ProductService.loadUsage(dataOwner, u);
        dataTable.setModel(m);
        TableFormat.stripFirstColumn(dataTable);
        detailsColumn(dataTable);
        //contact gets room, the net value of the position is an amount
        dataTable.getColumnModel().getColumn(3).setPreferredWidth(ModernTheme.px(220));
        amountColumn(dataTable, ProductService.USAGE_NET);
        resetFilter(dataTable, filter);
        BigDecimal[] totals = ProductService.usageTotals(m);
        summary.setText(String.format(Messages.PRODUCT_USAGE_SUMMARY.toString(), totals[0].intValue(),
                FormatNumber.formatDezimal(totals[1]), FormatNumber.formatDezimal(totals[2])));
    }

    private void openItem(Object id) {
        Context c;
        switch (usage) {
            case OFFERS:
                c = Context.getOffer();
                break;
            case ORDERS:
                c = Context.getOrder();
                break;
            case CREDITS:
                c = Context.getCredit();
                break;
            default:
                c = Context.getInvoice();
        }
        try {
            mpv5.YabsViewProxy.instance().getIdentifierView().addTab(
                    DatabaseObject.getObject(c, Integer.parseInt(String.valueOf(id))));
        } catch (NodataFoundException | NumberFormatException e) {
            Log.Debug(this, e);
        }
    }

    private void editList() {
        try {
            ProductList l = (ProductList) DatabaseObject.getObject(Context.getProductList(), productlistsids_);
            mpv5.YabsViewProxy.instance().addTab(l);
        } catch (NodataFoundException ex) {
            Popup.error(ex);
        }
    }

    private void preview(int templateType) {
        if (dataOwner != null && dataOwner.isExisting()) {
            if (TemplateHandler.isLoaded(Long.valueOf(dataOwner.templateGroupIds()), templateType)) {
                PreviewWaiter pr = new PreviewWaiter();
                pr.setDataOwner(dataOwner);
                new Job(Export.createFile(TemplateHandler.loadTemplate(dataOwner.templateGroupIds(), templateType), dataOwner), pr).execute();
            } else {
                Popup.notice(Messages.NO_TEMPLATE_LOADED + " (" + User.getCurrentUser() + ")");
            }
        }
    }

    private void preloadTemplate() {
        final Product p = dataOwner;
        new Thread(() -> {
            if (p.__getInttype() == Product.TYPE_PRODUCT) {
                TemplateHandler.loadTemplateFor(previewButton, Long.valueOf(p.templateGroupIds()), Constants.TYPE_PRODUCT);
                TemplateHandler.loadTemplateFor(orderButton, Long.valueOf(p.templateGroupIds()), Constants.TYPE_PRODUCT_ORDER);
            } else {
                TemplateHandler.loadTemplateFor(previewButton, Long.valueOf(p.templateGroupIds()), Constants.TYPE_SERVICE);
            }
        }).start();
    }

    private void fileTableClicked(MouseEvent evt) {
        if (evt.getClickCount() > 1 && filetable.getSelectedRow() >= 0) {
            int row = filetable.convertRowIndexToModel(filetable.getSelectedRow());
            FileDirectoryHandler.open(ProductService.retrieveFile(
                    filetable.getModel().getValueAt(row, 0).toString(),
                    new File(FileDirectoryHandler.getTempDir() + filetable.getModel().getValueAt(row, 1).toString())));
        } else if (evt.getClickCount() == 1 && evt.getButton() == MouseEvent.BUTTON3) {
            int row = filetable.rowAtPoint(evt.getPoint());
            if (row >= 0 && !filetable.isRowSelected(row)) {
                filetable.changeSelection(row, filetable.columnAtPoint(evt.getPoint()), false, false);
            }
            filePopup.show(filetable, evt.getX(), evt.getY());
        }
    }

    private void addFile() {
        //the product must be saved to keep files
        if (!dataOwner.getPanelData(this) || !dataOwner.save()) {
            return;
        }
        DialogForFile d = new DialogForFile(DialogForFile.FILES_ONLY);
        if (d.chooseFile()) {
            String s = Popup.Enter_Value(Messages.ENTER_A_DESCRIPTION);
            if (s != null) {
                ProductService.addFile(dataOwner, d.getFile(), s, this);
            }
        }
    }

    private void deleteFile() {
        if (dataOwner.isExisting() && filetable.getSelectedRow() >= 0) {
            ProductService.deleteFile(String.valueOf(filetable.getModel().getValueAt(
                    filetable.convertRowIndexToModel(filetable.getSelectedRow()), 0)));
            fillFiles();
            updateImage();
        }
    }

    private void useSelectedFileAsImage() {
        if (filetable.getSelectedRow() < 0) {
            return;
        }
        defaultimage_ = String.valueOf(filetable.getModel().getValueAt(
                filetable.convertRowIndexToModel(filetable.getSelectedRow()), 0));
        updateImage();
        if (dataOwner != null) {
            dataOwner.getPanelData(this);
            dataOwner.save(false);
        }
    }

    private void fillFiles() {
        filetable.setModel(ProductService.loadFiles(dataOwner));
        TableFormat.stripFirstColumn(filetable);
        if (filetable.getModel().getRowCount() == 1 && defaultimage_ == null) {
            defaultimage_ = filetable.getModel().getValueAt(0, 0).toString();
            updateImage();
        }
    }

    /**
     * Loads the product image in the background and shows it in the header
     * and the image card
     */
    private void updateImage() {
        if (defaultimage_ != null && defaultimage_.equals(lastimage)) {
            return;
        }
        lastimage = defaultimage_;
        final String name = defaultimage_;
        if (name == null || name.isEmpty()) {
            showImage(null, null);
            return;
        }
        new Thread(() -> {
            File f = ProductService.retrieveImage(name);
            java.awt.Image img = null;
            if (f != null) {
                try {
                    img = javax.imageio.ImageIO.read(f);
                } catch (Exception e) {
                    Log.Debug(ModernProductPanel.this, e.getMessage());
                }
            }
            final File file = f;
            final java.awt.Image loaded = img;
            SwingUtilities.invokeLater(() -> showImage(file, loaded));
        }).start();
    }

    private void showImage(File file, java.awt.Image img) {
        currentImageFile = file;
        avatar.setImage(img);
        if (img == null) {
            image.setIcon(null);
            image.setText(Messages.PRODUCT_NO_IMAGE.toString());
        } else {
            int max = ModernTheme.px(180);
            double f = Math.min(1d, Math.min((double) max / img.getWidth(null), (double) max / img.getHeight(null)));
            image.setText(null);
            image.setIcon(new ImageIcon(img.getScaledInstance((int) Math.max(1, img.getWidth(null) * f),
                    (int) Math.max(1, img.getHeight(null) * f), java.awt.Image.SCALE_SMOOTH)));
        }
        relayout(image);
    }

    private void saveRelations() {
        List<Contact> further = new ArrayList<Contact>();
        for (SupplierRow r : supplierRows()) {
            Contact c = r.getContact();
            if (c != null && !r.isDefault.isSelected()) {
                further.add(c);
            }
        }
        ProductService.saveFurtherSuppliers(dataOwner, further);
        List<Tier> list = new ArrayList<Tier>();
        for (Component c : tiers.getComponents()) {
            list.add(((TierRow) c).getTier());
        }
        ProductService.saveTiers(dataOwner, list);
        loadTiers();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="DataPanel">
    @Override
    public DatabaseObject getDataOwner() {
        return dataOwner;
    }

    @Override
    public void setDataOwner(DatabaseObject object, boolean populate) {
        dataOwner = (Product) object;
        if (!populate) {
            return;
        }
        dataOwner.setPanelData(this);
        exposeData();
        if (object.isExisting()) {
            setTabTitle(cname_);
        }
        printing.init(dataOwner);
        tb.setFavourite(Favourite.isFavourite(object));
        tb.setEditable(!object.isReadOnly());
        if (object.isReadOnly()) {
            Popup.notice(Messages.LOCKED_BY);
        }
        previewButton.setEnabled(false);
        orderButton.setEnabled(false);
        preloadTemplate();
        showUsage(dataOwner.isExisting() ? Usage.INVOICES : null);
    }

    @Override
    public boolean collectData() {
        if (cname.getText() == null || cname.getText().length() == 0) {
            showRequiredFields();
            return false;
        }
        cnumber_ = cnumber.get_Text();
        suppliersids_ = -1;
        for (SupplierRow r : supplierRows()) {
            Contact c = r.getContact();
            if (c != null && r.isDefault.isSelected()) {
                suppliersids_ = c.__getIDS();
            }
        }
        try {
            manufacturersids_ = Integer.parseInt(String.valueOf(manufacturer.getSelectedItem().getId()));
        } catch (Exception e) {
            //no manufacturer
        }
        try {
            productgroupsids_ = Integer.parseInt(String.valueOf(familyselect.getSelectedItem().getId()));
        } catch (Exception e) {
            productgroupsids_ = 1;
        }
        if (groupnameselect.getSelectedItem() != null) {
            try {
                group_ = (Group) DatabaseObject.getObject(Context.getGroup(),
                        Integer.parseInt(String.valueOf(groupnameselect.getSelectedItem().getId())));
            } catch (NodataFoundException | NumberFormatException ex) {
                Log.Debug(this, ex);
            }
        }
        if (dateadded_ == null) {
            dateadded_ = new Date();
        }
        if (intaddedby_ <= 0) {
            intaddedby_ = User.getCurrentUser().__getIDS();
        }
        description_ = description.getText();
        cname_ = cname.getText();
        measure_ = unit.getText();
        url_ = url.getText();
        ean_ = ean.getText();
        reference_ = reference.getText();
        try {
            internalnetvalue_ = FormatNumber.parseDezimal(netvalue.getText());
        } catch (NumberFormatException e) {
            internalnetvalue_ = BigDecimal.ZERO;
        }
        try {
            externalnetvalue_ = FormatNumber.parseDezimal(extvalue.getText());
        } catch (NumberFormatException e) {
            externalnetvalue_ = BigDecimal.ZERO;
        }
        try {
            taxids_ = Integer.parseInt(String.valueOf(selecttax.getSelectedItem().getId()));
        } catch (Exception e) {
            taxids_ = 1;
        }
        if (inttype_ < 0) {
            inttype_ = Product.TYPE_PRODUCT;
        }
        stockvalue_ = stack.getValue(BigDecimal.ONE);
        thresholdvalue_ = threshold.getValue(BigDecimal.ONE);
        intinventorytype_ = inventoryDisabled.isSelected() ? 0 : 1;
        return true;
    }

    @Override
    public void exposeData() {
        calculating = true;
        try {
            cname.setText(cname_);
            cnumber.setText(cnumber_);
            old_cnumber = cnumber_ == null ? "" : cnumber_;
            description.setText(description_);
            description.setCaretPosition(0);
            unit.setText(measure_);
            url.setText(url_);
            ean.setText(ean_);
            reference.setText(reference_);
            netvalue.setText(FormatNumber.formatDezimal(internalnetvalue_ == null ? BigDecimal.ZERO : internalnetvalue_));
            extvalue.setText(FormatNumber.formatDezimal(externalnetvalue_ == null ? BigDecimal.ZERO : externalnetvalue_));
            stack.setText(FormatNumber.formatDezimal(stockvalue_ == null ? BigDecimal.ZERO : stockvalue_));
            threshold.setText(FormatNumber.formatDezimal(thresholdvalue_ == null ? BigDecimal.ZERO : thresholdvalue_));
        } finally {
            calculating = false;
        }
        inventoryDisabled.setSelected(intinventorytype_ == 0);
        setType(inttype_);
        ProductGroup g = ProductService.getProductGroup(productgroupsids_);
        if (g != null) {
            familyselect.setModel(g);
            path.setText(g.__getHierarchypath() == null ? "" : g.__getHierarchypath());
        }
        try {
            groupnameselect.setModel(group_);
        } catch (Exception ex) {
            Log.Debug(this, ex.getMessage());
        }
        selectById(selecttax, taxids_);
        Contact m = ProductService.getContact(manufacturersids_);
        if (m != null) {
            manufacturer.setModel(m);
        } else {
            manufacturer.setModel(new Object[0][0]);
        }
        for (Component c : suppliers.getComponents()) {
            defaultSupplier.remove(((SupplierRow) c).isDefault);
        }
        suppliers.removeAll();
        addSupplierRow(ProductService.getContact(suppliersids_), true);
        for (Contact c : ProductService.loadFurtherSuppliers(dataOwner)) {
            addSupplierRow(c, false);
        }
        listButton.setEnabled(productlistsids_ > 0);
        loadTiers();
        fillFiles();
        updateImage();
        updateMarkup();
        updateGross();
        updateHeader();
    }

    @Override
    public void refresh() {
        SwingUtilities.invokeLater(() -> {
            try {
                sp.refresh();
                if (!dataOwner.isExisting()) {
                    groupnameselect.setModel(DatabaseObject.getObject(Context.getGroup(), User.getCurrentUser().__getGroupsids()));
                    try {
                        familyselect.setModel(DatabaseObject.getObject(Context.getProductGroup(), User.getCurrentUser().__getIntdefaultaccount()));
                    } catch (NodataFoundException e) {
                        Log.Debug(ModernProductPanel.this, e.getMessage());
                    }
                    updatePath();
                } else {
                    //e.g. after a file was added
                    fillFiles();
                }
                updateGross();
            } catch (Exception e) {
                Log.Debug(ModernProductPanel.this, e);
            }
        });
    }

    @Override
    public void paste(DatabaseObject... dbos) {
        for (DatabaseObject dbo : dbos) {
            if (dbo.getDbIdentity().equals(Context.getProduct().getDbIdentity())) {
                setDataOwner(dbo, true);
            } else {
                mpv5.YabsViewProxy.instance().addMessage(Messages.NOT_POSSIBLE.toString() + Messages.ACTION_PASTE.toString(), Color.RED);
            }
        }
    }

    @Override
    public void showRequiredFields() {
        TextFieldUtils.blinkerRed(cname);
        cname.requestFocus();
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
        saveRelations();
    }

    @Override
    public void actionAfterCreate() {
        saveRelations();
        sp.refresh();
    }

    @Override
    public void actionBeforeCreate() {
        if (old_cnumber.equals(cnumber_)) {
            cnumber_ = null;
        }
    }

    @Override
    public void actionBeforeSave() throws ChangeNotApprovedException {
        if (dataOwner.isExisting()
                && User.getCurrentUser().getProperties().getProperty("org.openyabs.uiproperty", "dowarnings")
                && !Popup.Y_N_dialog(Messages.REALLY_CHANGE)) {
            throw new ChangeNotApprovedException(dataOwner);
        }
    }

    @Override
    public void mail() {
        if (dataOwner != null && dataOwner.isExisting()) {
            if (TemplateHandler.isLoaded(Long.valueOf(dataOwner.templateGroupIds()), dataOwner.__getInttype())) {
                try {
                    Contact cont = (Contact) Popup.SelectValue(Context.getContact());
                    Export.mail(TemplateHandler.loadTemplate(Long.valueOf(dataOwner.templateGroupIds()), dataOwner.__getInttype()), dataOwner, cont, null);
                } catch (Exception ex) {
                    Log.Debug(ex);
                }
            } else {
                Popup.notice(Messages.NO_TEMPLATE_LOADED + " (" + User.getCurrentUser() + ") : " + TemplateHandler.getName(Constants.TYPE_PRODUCT));
            }
        }
    }

    @Override
    public void print() {
        if (dataOwner != null && dataOwner.isExisting()
                && TemplateHandler.isLoaded(Long.valueOf(dataOwner.templateGroupIds()), dataOwner.__getInttype())) {
            Export.print(TemplateHandler.loadTemplate(Long.valueOf(dataOwner.templateGroupIds()), dataOwner.__getInttype()), dataOwner);
        } else {
            if (dataOwner != null && dataOwner.isExisting()) {
                Notificator.raiseNotification(Messages.NO_TEMPLATE_LOADED + " (" + User.getCurrentUser() + ") : " + TemplateHandler.getName(Constants.TYPE_PRODUCT), false, false, dataOwner);
            }
            Export.print(this);
        }
    }

    @Override
    public void pdf() {
        if (dataOwner != null && dataOwner.isExisting()) {
            dataOwner.toPdf(true);
        }
    }

    @Override
    public void odt() {
        if (dataOwner != null && dataOwner.isExisting()) {
            dataOwner.toOdt(true);
        }
    }

    @Override
    public void changeSelection(MPComboBoxModelItem to, Context c) {
    }
    // </editor-fold>
}
