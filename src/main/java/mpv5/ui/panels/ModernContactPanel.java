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
import java.awt.Desktop;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.KeyboardFocusManager;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.net.URI;
import java.util.Date;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JViewport;
import javax.swing.RowFilter;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.db.objects.Address;
import mpv5.db.objects.Contact;
import mpv5.db.objects.Favourite;
import mpv5.db.objects.Group;
import mpv5.db.objects.Item;
import mpv5.db.objects.Schedule;
import mpv5.db.objects.User;
import mpv5.globals.Messages;
import mpv5.handler.ContactService;
import mpv5.handler.ContactService.Related;
import mpv5.i18n.LanguageManager;
import mpv5.logging.Log;
import mpv5.ui.beans.LabeledCombobox;
import mpv5.ui.beans.LabeledTextField;
import mpv5.ui.beans.PrinitingComboBox;
import mpv5.ui.dialogs.DialogForFile;
import mpv5.ui.dialogs.Popup;
import mpv5.ui.dialogs.ScheduleDayEvents;
import mpv5.ui.misc.MPTable;
import mpv5.ui.misc.TableViewPersistenceHandler;
import mpv5.ui.modern.Avatar;
import mpv5.ui.modern.Card;
import mpv5.ui.modern.ChipToggle;
import mpv5.ui.modern.ModernTheme;
import mpv5.ui.modern.ResponsiveGrid;
import mpv5.ui.modern.SplitButton;
import mpv5.ui.modern.WrapLayout;
import mpv5.ui.popups.DOTablePopUp;
import mpv5.ui.popups.FileTablePopUp;
import mpv5.ui.toolbars.ModernCommandBar;
import mpv5.utils.date.DateConverter;
import mpv5.utils.export.EInvoiceDelivery;
import mpv5.utils.export.Export;
import mpv5.utils.export.PaymentTerms;
import mpv5.utils.files.FileDirectoryHandler;
import mpv5.utils.models.MPComboBoxModelItem;
import mpv5.utils.models.MPTableModel;
import mpv5.utils.tables.TableFormat;
import mpv5.utils.ui.TextFieldUtils;

/**
 * The contact view in the modern layout: a header with name and roles, the
 * contact data in cards which flow into one, two or three columns depending
 * on the width, and the documents of the contact below.
 * <p>
 * Same functions as {@link ContactPanel} (the classic view); which one is used
 * is decided by {@link mpv5.YabsViewProxy}. The logic which is not about the
 * screen is in {@link ContactService}.
 * <p>
 * The public fields ending with an underscore are filled from / copied to the
 * contact by name ({@link DatabaseObject#setPanelData(DataPanel)},
 * {@link DatabaseObject#getPanelData(DataPanel)}), so they must keep their
 * names.
 */
public class ModernContactPanel extends ModernDataPanel {

    private static final long serialVersionUID = 1L;
    private final java.util.ResourceBundle bundle = LanguageManager.getBundle();
    private Contact dataOwner;
    private final ModernCommandBar tb;
    private final SearchPanel sp;
    //search panel on the left
    private final JPanel leftpane = new JPanel(new BorderLayout());
    //header
    private final Avatar avatar = new Avatar();
    private final JLabel displayname = new JLabel();
    private final JLabel meta = new JLabel();
    private final ChipToggle company = new ChipToggle(text("ContactPanel.company.text"));
    private final ChipToggle customer = new ChipToggle(text("ContactPanel.customer.text"));
    private final ChipToggle supplier = new ChipToggle(text("ContactPanel.supplier.text"));
    private final ChipToggle manufacturer = new ChipToggle(text("ContactPanel.manufacturer.text"));
    private final PrinitingComboBox printing = new PrinitingComboBox();
    //fields
    private final LabeledTextField number = field();
    private final LabeledTextField cname = field();
    private final LabeledTextField prename = field();
    private final LabeledTextField title = field();
    private final LabeledTextField department = field();
    private final JRadioButton male = new JRadioButton(text("ContactPanel.male.text"));
    private final JRadioButton female = new JRadioButton(text("ContactPanel.female.text"));
    private final LabeledTextField street = field();
    private final LabeledTextField zip = field();
    private final LabeledTextField city = field();
    private final JComboBox countryselect = new JComboBox();
    private final LabeledTextField mainphone = field();
    private final LabeledTextField workphone = field();
    private final LabeledTextField mobilephone = field();
    private final LabeledTextField fax = field();
    private final LabeledTextField mailaddress = field();
    private final LabeledTextField website = field();
    private final LabeledCombobox companyselect = ModernTheme.plain(new LabeledCombobox());
    private final LabeledCombobox groupnameselect = ModernTheme.plain(new LabeledCombobox());
    private final LabeledTextField taxnumber = field();
    private final LabeledTextField payterm = field();
    private final LabeledTextField bankaccount = field();
    private final LabeledTextField bankid = field();
    private final LabeledTextField bankname = field();
    private final LabeledTextField bankcurrency = field();
    private final LabeledTextField bankcountry = field();
    private final JTextArea notes = new JTextArea(6, 20);
    //payment / e-invoice (value properties of the contact)
    private final JComboBox<EInvoiceDelivery> einvoiceDelivery = new JComboBox<EInvoiceDelivery>(EInvoiceDelivery.values());
    private final JTextField einvoiceLeitweg = new JTextField(20);
    private final JComboBox<Object> einvoiceAccount = new JComboBox<Object>();
    private final JCheckBox einvoiceImmediate = new JCheckBox(Messages.EINVOICE_PAY_IMMEDIATELY.toString());
    private final JTextField einvoiceSkontoPercent = new JTextField(6);
    private final JTextField einvoiceSkontoDays = new JTextField(6);
    //tabs
    private final JTabbedPane tabs = new JTabbedPane();
    //main address and additional addresses in one card, chosen with the selector
    private final JPanel addressSelector = new JPanel(new WrapLayout(FlowLayout.LEFT, ModernTheme.px(4), ModernTheme.px(2)));
    private final ButtonGroup addressGroup = new ButtonGroup();
    private final java.awt.CardLayout addressCards = new java.awt.CardLayout();
    private final JPanel addressBody = new JPanel(addressCards) {
        private static final long serialVersionUID = 1L;

        @Override
        public java.awt.Dimension getPreferredSize() {
            //the height of the shown address, not of the largest one
            for (Component c : getComponents()) {
                if (c.isVisible()) {
                    return c.getPreferredSize();
                }
            }
            return super.getPreferredSize();
        }
    };
    private final ChipToggle mainAddressChip = new ChipToggle(text("ContactPanel.mainaddress.TabConstraints.tabTitle"));
    private final JButton addAddressButton = link(text("ContactPanel.button_order1.text"), "/images/16/add.png");
    private final Map<ModernAddressForm, ChipToggle> addressChips = new java.util.LinkedHashMap<ModernAddressForm, ChipToggle>();
    private static final String MAIN_ADDRESS = "main";
    /**
     * The files of a contact are not used currently, the tab is hidden
     */
    private static final boolean SHOW_FILES = false;
    private final JTable proptable = ModernTheme.tabLeaves(new MPTable(this));
    private final JTable filetable = ModernTheme.tabLeaves(new MPTable(this));
    //documents of the contact
    private final JTable dataTable = tooltipTable(this);
    private final JPopupMenu itemTablePopup;
    private final JTextField filter = new JTextField(14);
    private final Map<Related, ChipToggle> relatedButtons = new EnumMap<Related, ChipToggle>(Related.class);
    private final ButtonGroup relatedGroup = new ButtonGroup();
    private Related related;
    //the list the user chose last, shown again when its role is switched back on
    private Related chosenRelated;
    private boolean loading;
    private final JButton newOffer = ModernTheme.actionButton(new JButton(text("ContactPanel.button_offer.text")), "/images/16/add.png");
    private final JButton newOrder = ModernTheme.actionButton(new JButton(text("ContactPanel.button_order.text")), "/images/16/add.png");
    private final SplitButton newInvoice = new SplitButton(text("ContactPanel.button_bill._Label"), "/images/16/add.png");
    private final JButton newCredit = ModernTheme.actionButton(new JButton(text("ContactPanel.button_credit.text")), "/images/16/add.png");
    private final JButton newProduct = ModernTheme.actionButton(new JButton(text("ContactPanel.button_product.text")), "/images/16/add.png");
    private final JPanel relatedPanel = new JPanel(new BorderLayout());
    private final JSplitPane split;
    // <editor-fold defaultstate="collapsed" desc="Data fields, filled by name from / copied to the contact">
    public String city_;
    public String cname_;
    public String taxnumber_;
    public String department_;
    public boolean iscompany_;
    public boolean iscustomer_;
    public boolean isenabled_;
    public String fax_;
    public String mailaddress_;
    public boolean ismale_;
    public boolean ismanufacturer_;
    public String mobilephone_;
    public String notes_;
    public String cnumber_;
    public String mainphone_;
    public String prename_;
    public String street_;
    public boolean issupplier_;
    public String title_;
    public String website_;
    public String workphone_;
    public String zip_;
    public int intaddedby_;
    public String company_;
    public int ids_;
    public Date dateadded_;
    public Group group_;
    public String country_;
    public String bankaccount_;
    public String bankid_;
    public String bankname_;
    public String bankcurrency_;
    public String bankcountry_;
    public int payterm_;
    // </editor-fold>

    /**
     * @param context the contact context (contact, customer, supplier,
     * manufacturer)
     */
    public ModernContactPanel(Context context) {
        super(new BorderLayout());
        setName("contactpanel");

        sp = new SearchPanel(context, this);
        dataOwner = new Contact();
        dataOwner.setCountry(User.getCurrentUser().__getDefcountry());

        initFields();
        itemTablePopup = DOTablePopUp.addDefaultPopupMenu(dataTable, Context.getInvoice(), false);

        JPanel content = new JPanel(new BorderLayout());
        content.setOpaque(false);
        content.add(createHeader(), BorderLayout.NORTH);

        //flat tabs like the main window
        tabs.setUI(new mpv5.ui.modern.ModernTabbedPaneUI(javax.swing.SwingConstants.TRAILING));
        tabs.addTab(Messages.CONTACT_OVERVIEW.toString(), scroll(createOverview()));
        tabs.addTab(text("ContactPanel.propPanel.TabConstraints.tabTitle"), createPropertiesTab());
        if (SHOW_FILES) {
            tabs.addTab(text("ContactPanel.filetab.TabConstraints.tabTitle"), createFilesTab());
        }

        createRelatedPanel();
        split = split("contact", tabs, relatedPanel, 0.6);
        content.add(split, BorderLayout.CENTER);

        add(content, BorderLayout.CENTER);
        add(leftpane, BorderLayout.WEST);
        //the toolbar calls showSearchBar(), so it is created last
        tb = new ModernCommandBar(this);
        add(tb, BorderLayout.NORTH);
        //the actions of a contact, next to the record actions
        JButton web = new JButton(text("ContactPanel.jButton5.text"));
        web.addActionListener(e -> openWebsite());
        JButton history = new JButton(text("ContactPanel.jButton2.text"));
        history.addActionListener(e -> showHistory());
        JButton schedule = new JButton(text("ContactPanel.jButton3.text"));
        schedule.addActionListener(e -> showSchedule());
        tb.addAction(web, null);
        tb.addAction(history, ModernCommandBar.HISTORY);
        tb.addAction(schedule, null);
        tb.addAction(printing, null);
        //Ctrl+F: find in the documents of the contact
        tb.setFindAction(() -> {
            filter.requestFocusInWindow();
            filter.selectAll();
        });

        dateadded_ = new Date();
        intaddedby_ = User.getCurrentUser().__getIDS();
        if (context.equals(Context.getSupplier()) || context.equals(Context.getManufacturer())) {
            company.setSelected(true);
        }
        if (context.equals(Context.getCustomer())) {
            customer.setSelected(true);
        } else if (context.equals(Context.getSupplier())) {
            supplier.setSelected(true);
        } else if (context.equals(Context.getManufacturer())) {
            manufacturer.setSelected(true);
        }
        updateHeader();
        updateRoleControls();
        refresh();
    }

    // <editor-fold defaultstate="collapsed" desc="Layout">
    private void initFields() {
        number.setEnabled(false);
        number.setFocusable(false);
        number.setSearchOnEnterEnabled(true);
        number.setParent(this);
        number.setSearchField("cnumber");
        number.setContext(Context.getContact());

        cname.setSearchOnEnterEnabled(true);
        cname.setParent(this);
        cname.setSearchField("cname");
        cname.setContext(Context.getContact());

        payterm.set_ValueClass(Integer.class);
        payterm.getTextField().setColumns(4);
        mpv5.utils.ui.FieldCheck.attach(taxnumber, mpv5.utils.export.ZugpferdFile::isValidVatId,
                Messages.CONTACT_VATID_TOOLTIP.toString(), Messages.CONTACT_VATID_INVALID.toString());

        groupnameselect.setSearchEnabled(true);
        groupnameselect.setContext(Context.getGroup());
        companyselect.setSearchEnabled(true);
        companyselect.setContext(Context.getContactsCompanies());
        companyselect.getComboBox().setEditable(true);
        countryselect.setModel(LanguageManager.getCountriesAsComboBoxModel());

        ButtonGroup gender = new ButtonGroup();
        gender.add(male);
        gender.add(female);
        male.setSelected(true);
        male.setOpaque(false);
        female.setOpaque(false);

        ModernTheme.roomy(notes);
        ModernTheme.roomy(einvoiceLeitweg);
        ModernTheme.roomy(einvoiceSkontoPercent);
        ModernTheme.roomy(einvoiceSkontoDays);
        notes.setLineWrap(true);
        notes.setWrapStyleWord(true);
        //tab moves on to the next field instead of inserting a tab
        ModernTheme.tabLeaves(notes);

        java.awt.event.ItemListener roles = e -> updateRoleControls();
        customer.addItemListener(roles);
        supplier.addItemListener(roles);
        manufacturer.addItemListener(roles);
        company.addItemListener(e -> updateHeader());

        einvoiceImmediate.setOpaque(false);
        einvoiceImmediate.setToolTipText(Messages.EINVOICE_PAY_IMMEDIATELY_TOOLTIP.toString());
        einvoiceImmediate.addActionListener(e -> updateImmediate());
        einvoiceAccount.setToolTipText(Messages.EINVOICE_ACCOUNT_TOOLTIP.toString());
        payterm.getTextField().setToolTipText(Messages.EINVOICE_PAYTERM_TOOLTIP.toString());
        einvoiceSkontoPercent.setToolTipText(Messages.EINVOICE_SKONTO_TOOLTIP.toString());
        einvoiceSkontoDays.setToolTipText(Messages.EINVOICE_SKONTO_TOOLTIP.toString());
        //the explanation of the former e-invoice tab
        String einvoiceInfo = "<html><div style='width:360px'>" + Messages.EINVOICE_TAB_INFO + "</div></html>";
        einvoiceDelivery.setToolTipText(einvoiceInfo);
        einvoiceLeitweg.setToolTipText(einvoiceInfo);

        DocumentListener nameChanged = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                updateHeader();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateHeader();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updateHeader();
            }
        };
        cname.getTextField().getDocument().addDocumentListener(nameChanged);
        prename.getTextField().getDocument().addDocumentListener(nameChanged);
    }

    private JComponent createHeader() {
        JPanel header = new JPanel(new BorderLayout(ModernTheme.px(14), 0));
        header.setOpaque(false);
        header.setBorder(ModernTheme.padding(8, 16));

        header.add(avatar, BorderLayout.WEST);

        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        //the main information of the view: large
        displayname.setFont(ModernTheme.font(13, Font.BOLD));
        displayname.setForeground(ModernTheme.foreground());
        //contact / date / user in the title line, readable (there is room for it)
        meta.setFont(ModernTheme.font(5, Font.PLAIN));
        meta.setBorder(BorderFactory.createEmptyBorder(0, ModernTheme.px(20), 0, 0));
        meta.setForeground(ModernTheme.muted());
        //the roles; the actions are in the command bar
        JPanel chips = new JPanel(new FlowLayout(FlowLayout.LEFT, ModernTheme.px(6), 0));
        chips.setOpaque(false);
        chips.add(company);
        chips.add(customer);
        chips.add(supplier);
        chips.add(manufacturer);
        //two fixed lines: the name (shortened with "..." if there is no room), below the roles
        //and the meta data. Before, name, roles and meta wrapped in one row whose height was
        //not recomputed: with a long name in a narrow window the second line was cut off
        displayname.setAlignmentX(LEFT_ALIGNMENT);
        displayname.setMinimumSize(new java.awt.Dimension(0, displayname.getPreferredSize().height));
        displayname.setPreferredSize(new java.awt.Dimension(ModernTheme.px(80), displayname.getFontMetrics(displayname.getFont()).getHeight()));
        displayname.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, displayname.getFontMetrics(displayname.getFont()).getHeight()));
        JPanel second = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        second.setOpaque(false);
        second.add(chips);
        second.add(meta);
        second.setAlignmentX(LEFT_ALIGNMENT);
        center.add(displayname);
        center.add(javax.swing.Box.createVerticalStrut(ModernTheme.px(4)));
        center.add(second);
        header.add(center, BorderLayout.CENTER);
        return header;
    }

    private JComponent createOverview() {
        ResponsiveGrid grid = new ResponsiveGrid(340, 3).arrangeable("contact");

        JPanel gender = new JPanel(new FlowLayout(FlowLayout.LEFT, ModernTheme.px(8), 0));
        gender.setOpaque(false);
        gender.add(male);
        gender.add(female);
        grid.add(new Card(Messages.CONTACT_SECTION_PERSON.toString())
                .row(Card.field(text("ContactPanel.title._Label"), title, 1),
                        Card.field(text("ContactPanel.prename._Label"), prename, 2))
                .row(Card.field(text("ContactPanel.cname._Label"), cname))
                .row(Card.field(text("ContactPanel.companyselect._Label"), companyselect))
                .row(Card.field(text("ContactPanel.department._Label"), department, 2),
                        Card.field(null, gender, 1)));

        addressSelector.setOpaque(false);
        addressBody.setOpaque(false);
        mainAddressChip.addActionListener(e -> showAddress(MAIN_ADDRESS));
        addressGroup.add(mainAddressChip);
        mainAddressChip.setSelected(true);
        addAddressButton.setToolTipText(text("ContactPanel.button_order1.text"));
        addAddressButton.addActionListener(e -> addAddress());
        addressBody.add(new Card(null).flat()
                .row(Card.field(text("ContactPanel.street._Label"), street))
                .row(Card.field(text("ContactPanel.zip._Label"), zip, 1),
                        Card.field(text("ContactPanel.city._Label"), city, 2))
                .row(Card.field(text("ContactPanel.jLabel5.text"), countryselect)), MAIN_ADDRESS);
        resetAddressSelector();
        grid.add(new Card(Messages.CONTACT_SECTION_ADDRESS.toString())
                .addFull(addressSelector)
                .addFull(addressBody));

        grid.add(new Card(Messages.CONTACT_SECTION_COMMUNICATION.toString())
                .row(Card.field(text("ContactPanel.phone._Label"), mainphone),
                        Card.field(text("ContactPanel.mobile._Label"), mobilephone))
                .row(Card.field(text("ContactPanel.work._Label"), workphone),
                        Card.field(text("ContactPanel.fax._Label"), fax))
                .row(Card.field(text("ContactPanel.mailer._Label"), mailaddress))
                .row(Card.field(text("ContactPanel.web._Label"), website)));

        //business data and bank account in one card
        grid.add(new Card(Messages.CONTACT_SECTION_BUSINESS.toString())
                .row(Card.field(text("ContactPanel.number._Label"), number, 1),
                        Card.field(text("ContactPanel.groupnameselect._Label"), groupnameselect, 2))
                .row(Card.field(text("ContactPanel.taxnumber._Label"), taxnumber))
                .section(Messages.CONTACT_SECTION_OWN_BANK.toString())
                .addFull(mpv5.ui.modern.Applets.hint(Messages.CONTACT_OWN_BANK_HINT.toString()))
                .row(Card.field(text("ContactPanel.bankaccount._Label"), bankaccount))
                .row(Card.field(text("ContactPanel.bankid._Label"), bankid, 1),
                        Card.field(text("ContactPanel.bankname._Label"), bankname, 2))
                .row(Card.field(text("ContactPanel.bankcurrency._Label"), bankcurrency),
                        Card.field(text("ContactPanel.bankcountry._Label"), bankcountry)));

        grid.add(new Card(Messages.CONTACT_SECTION_TERMS.toString())
                .addFull(mpv5.ui.modern.Applets.hint(Messages.CONTACT_TERMS_HINT.toString()))
                .row(Card.field(Messages.EINVOICE_PAYTERM.toString(), payterm, 1),
                        Card.field(null, einvoiceImmediate, 2))
                .row(Card.field(Messages.EINVOICE_ACCOUNT.toString(), einvoiceAccount))
                .row(Card.field(Messages.COMPANY_SKONTO_PERCENT.toString(), einvoiceSkontoPercent),
                        Card.field(Messages.COMPANY_SKONTO_DAYS.toString(), einvoiceSkontoDays))
                .row(Card.field(Messages.EINVOICE_DELIVERY.toString(), einvoiceDelivery))
                .row(Card.field(Messages.EINVOICE_LEITWEG.toString(), einvoiceLeitweg)));

        JScrollPane notesScroll = new JScrollPane(notes);
        grid.add(new Card(text("ContactPanel.jPanel4.TabConstraints.tabTitle")).addFull(notesScroll, 1));
        return grid;
    }

    private JComponent createPropertiesTab() {
        ModernTheme.style(proptable);
        ((MPTable) proptable).setPersistanceHandler(new TableViewPersistenceHandler((MPTable) proptable, this));
        proptable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        Card card = new Card(text("ContactPanel.propPanel.TabConstraints.tabTitle"));
        card.addFull(new JScrollPane(proptable), 1);
        return padded(card);
    }

    private JComponent createFilesTab() {
        ModernTheme.style(filetable);
        filetable.setAutoCreateRowSorter(true);
        filetable.setDragEnabled(true);
        filetable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evt) {
                fileTableClicked(evt);
            }
        });
        JButton add = link(null, "/images/16/add.png");
        add.setToolTipText(Messages.ACTION_ADD.toString());
        add.addActionListener(e -> addFile());
        JButton remove = link(null, "/images/16/remove.png");
        remove.setToolTipText(Messages.ACTION_REMOVE.toString());
        remove.addActionListener(e -> deleteFile());
        JButton reload = link(text("ContactPanel.jButton6.text"), null);
        reload.addActionListener(e -> fillFiles());
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, ModernTheme.px(4), 0));
        buttons.setOpaque(false);
        buttons.add(add);
        buttons.add(remove);
        buttons.add(reload);
        Card card = new Card(text("ContactPanel.filetab.TabConstraints.tabTitle"));
        card.headerAction(buttons);
        card.addFull(new JScrollPane(filetable), 1);
        return padded(card);
    }

    private void createRelatedPanel() {
        dataTable.setAutoCreateRowSorter(true);
        dataTable.setDragEnabled(true);
        ((MPTable) dataTable).setPersistanceHandler(new TableViewPersistenceHandler((MPTable) dataTable, this));
        dataTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evt) {
                dataTableClicked(evt);
            }
        });

        String[] labels = {
            text("ContactPanel.button_offers.text"), text("ContactPanel.button_orders.text"),
            text("ContactPanel.button_bills.text"), text("ContactPanel.button_credits.text"),
            text("ContactPanel.button_activitylists.text"), text("ContactPanel.button_products.text")};
        JPanel segments = new JPanel(new FlowLayout(FlowLayout.LEFT, ModernTheme.px(4), 0));
        segments.setOpaque(false);
        for (Related r : Related.values()) {
            final Related rel = r;
            ChipToggle b = new ChipToggle(labels[r.ordinal()]);
            b.addActionListener(e -> {
                chosenRelated = rel;
                showRelated(rel);
            });
            relatedButtons.put(r, b);
            relatedGroup.add(b);
            segments.add(b);
        }

        filter.setToolTipText(text("ContactPanel.filterme._Label"));
        liveFilter(dataTable, filter);
        JPanel filterPanel = new JPanel(new BorderLayout(ModernTheme.px(6), 0));
        filterPanel.setOpaque(false);
        filterPanel.add(ModernTheme.fieldLabel(text("ContactPanel.filterme._Label")), BorderLayout.WEST);
        filterPanel.add(filter, BorderLayout.CENTER);

        newOffer.addActionListener(e -> openNew(ContactService.newItem(dataOwner, Item.TYPE_OFFER)));
        newOrder.addActionListener(e -> openNew(ContactService.newItem(dataOwner, Item.TYPE_ORDER)));
        newInvoice.setToolTipText(text("ContactPanel.button_bill.toolTipText"));
        newInvoice.addActionListener(e -> openNew(ContactService.newItem(dataOwner, Item.TYPE_INVOICE)));
        newInvoice.add(Messages.NEW_DEPOSIT.toString(), e -> openNew(ContactService.newItem(dataOwner, Item.TYPE_DEPOSIT)));
        newInvoice.add(Messages.NEW_PART_PAYMENT.toString(), e -> openNew(ContactService.newItem(dataOwner, Item.TYPE_PART_PAYMENT)));
        newCredit.addActionListener(e -> openNew(ContactService.newItem(dataOwner, Item.TYPE_CREDIT)));
        newProduct.addActionListener(e -> openNew(ContactService.newProduct(dataOwner)));
        JPanel create = new JPanel(new WrapLayout(FlowLayout.RIGHT, ModernTheme.px(8), ModernTheme.px(4)));
        create.setOpaque(false);
        create.add(newOffer);
        create.add(newOrder);
        create.add(newInvoice);
        create.add(newCredit);
        create.add(newProduct);

        JPanel bar = new JPanel(new BorderLayout(ModernTheme.px(12), ModernTheme.px(8)));
        bar.setOpaque(false);
        bar.add(segments, BorderLayout.CENTER);
        bar.add(filterPanel, BorderLayout.EAST);
        bar.add(create, BorderLayout.SOUTH);

        Card card = new Card(Messages.CONTACT_SECTION_DOCUMENTS.toString());
        card.addFull(bar);
        card.addFull(new JScrollPane(dataTable), 1);
        relatedPanel.setBackground(ModernTheme.background());
        relatedPanel.setBorder(BorderFactory.createEmptyBorder(ModernTheme.px(8), 0, 0, 0));
        relatedPanel.add(card, BorderLayout.CENTER);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="State of the controls">
    private void updateHeader() {
        String name;
        if (company.isSelected() && companyselect.getText() != null && companyselect.getText().length() > 0) {
            name = companyselect.getText();
        } else {
            Address a = new Address();
            a.setCname(cname.getTextField().getText());
            a.setPrename(prename.getTextField().getText());
            name = a.getDisplayName();
        }
        if (name == null || name.trim().isEmpty()) {
            name = Messages.CONTACT_NEW.toString();
        }
        displayname.setText(name);
        displayname.setToolTipText(name);
        avatar.setName(name);

        StringBuilder m = new StringBuilder();
        if (number.getTextField().getText().length() > 0) {
            m.append(text("ContactPanel.number._Label")).append(' ').append(number.getTextField().getText()).append("   ·   ");
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

    /**
     * Shows the documents and actions which suit the roles of the contact:
     * offers, invoices ... for customers, products for suppliers and
     * manufacturers
     */
    private void updateRoleControls() {
        boolean isCustomer = customer.isSelected();
        boolean hasProducts = supplier.isSelected() || manufacturer.isSelected();
        for (Map.Entry<Related, ChipToggle> e : relatedButtons.entrySet()) {
            e.getValue().setVisible((e.getKey().isForCustomers() ? isCustomer : hasProducts)
                    && (e.getKey() != Related.ACTIVITIES || mpv5.globals.Features.activityLists()));
        }
        newOffer.setVisible(isCustomer);
        newOrder.setVisible(isCustomer);
        newInvoice.setVisible(isCustomer);
        newCredit.setVisible(isCustomer);
        newProduct.setVisible(hasProducts);
        //the roles changed which lists exist: keep a list on screen, prefer the one chosen last
        Related wanted = pickRelated(isCustomer, hasProducts);
        if (loading) {
            //setDataOwner shows the default list afterwards
        } else if (wanted != related && dataOwner != null && dataOwner.isExisting()) {
            showRelated(wanted);
        } else if (wanted == null && related != null) {
            showRelated(null);
        }
        relatedPanel.revalidate();
        relatedPanel.repaint();
    }

    /**
     * @return the list to show for the given roles: the one chosen last (or
     * shown first) if available, else the current one, else invoices (customers) or
     * products (suppliers, manufacturers); null if none is available
     */
    private Related pickRelated(boolean isCustomer, boolean hasProducts) {
        for (Related r : new Related[]{chosenRelated, related}) {
            if (r != null && (r.isForCustomers() ? isCustomer : hasProducts)) {
                return r;
            }
        }
        if (isCustomer) {
            return Related.INVOICES;
        }
        return hasProducts ? Related.PRODUCTS : null;
    }

    /**
     * Payable immediately: the payment term does not apply
     */
    private void updateImmediate() {
        payterm.getTextField().setEnabled(!einvoiceImmediate.isSelected());
    }

    private void showRelated(Related r) {
        related = r;
        if (r == null) {
            relatedGroup.clearSelection();
            dataTable.setModel(new MPTableModel());
            resetFilter(dataTable, filter);
            return;
        }
        relatedButtons.get(r).setSelected(true);
        dataTable.setModel(ContactService.loadRelated(dataOwner, r));
        TableFormat.stripFirstColumn(dataTable);
        if (r != Related.PRODUCTS) {
            detailsColumn(dataTable);
            //net value: column 3 of offers, invoices ..., column 2 of activity lists
            amountColumn(dataTable, r.isItem() ? 3 : 2);
        }
        resetFilter(dataTable, filter);
        if (r.isItem()) {
            //newest first
            dataTable.getRowSorter().toggleSortOrder(2);
            dataTable.getRowSorter().toggleSortOrder(2);
        }
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Actions">
    private void openNew(DatabaseObject o) {
        if (o != null) {
            mpv5.YabsViewProxy.instance().getIdentifierView().addTab(o);
        }
    }

    private void dataTableClicked(MouseEvent evt) {
        if (related == null) {
            return;
        }
        if (evt.getButton() == MouseEvent.BUTTON1 && evt.getClickCount() > 1 && dataTable.getSelectedRow() >= 0) {
            Object id = dataTable.getModel().getValueAt(dataTable.convertRowIndexToModel(dataTable.getSelectedRow()), 0);
            try {
                openNew(ContactService.getRelated(related, Integer.parseInt(String.valueOf(id))));
            } catch (NumberFormatException e) {
                Log.Debug(this, e);
            }
        } else if (evt.getButton() == MouseEvent.BUTTON3 && related.isItem()) {
            itemTablePopup.show(dataTable, evt.getX(), evt.getY());
        }
    }

    private void fileTableClicked(MouseEvent evt) {
        if (evt.getClickCount() > 1 && filetable.getSelectedRow() >= 0) {
            int row = filetable.convertRowIndexToModel(filetable.getSelectedRow());
            FileDirectoryHandler.open(ContactService.retrieveFile(
                    filetable.getModel().getValueAt(row, 0).toString(),
                    filetable.getModel().getValueAt(row, 1).toString()));
        } else if (evt.getClickCount() == 1 && evt.getButton() == MouseEvent.BUTTON3) {
            JTable source = (JTable) evt.getSource();
            int row = source.rowAtPoint(evt.getPoint());
            int column = source.columnAtPoint(evt.getPoint());
            if (row >= 0 && !source.isRowSelected(row)) {
                source.changeSelection(row, column, false, false);
            }
            FileTablePopUp.instanceOf(filetable).show(source, evt.getX(), evt.getY());
        }
    }

    private void addFile() {
        if (!dataOwner.isExisting()) {
            return;
        }
        DialogForFile d = new DialogForFile(DialogForFile.FILES_ONLY);
        if (d.chooseFile()) {
            String s = Popup.Enter_Value(Messages.ENTER_A_DESCRIPTION);
            if (s != null) {
                ContactService.addFile(dataOwner, d.getFile(), s, this);
            }
        }
    }

    private void deleteFile() {
        if (dataOwner.isExisting() && filetable.getSelectedRow() >= 0) {
            ContactService.deleteFile(dataOwner, String.valueOf(filetable.getModel().getValueAt(
                    filetable.convertRowIndexToModel(filetable.getSelectedRow()), 0)));
            fillFiles();
        }
    }

    private void fillFiles() {
        if (!SHOW_FILES) {
            return;
        }
        filetable.setModel(ContactService.loadFiles(dataOwner));
        TableFormat.stripFirstColumn(filetable);
    }

    /**
     * Shows the additional addresses of the contact as entries of the address
     * selector
     */
    private void addAddresses() {
        for (ModernAddressForm f : addressChips.keySet()) {
            addressBody.remove(f);
        }
        addressChips.clear();
        for (Address a : ContactService.getAddresses(dataOwner)) {
            ModernAddressForm f = newAddressForm();
            f.setDataOwner(a, true);
            addAddressForm(f);
        }
        resetAddressSelector();
        showAddress(MAIN_ADDRESS);
    }

    private ModernAddressForm newAddressForm() {
        ModernAddressForm f = new ModernAddressForm(new ModernAddressForm.Listener() {
            @Override
            public void saved(ModernAddressForm form) {
                ChipToggle chip = addressChips.get(form);
                if (chip != null) {
                    chip.setText(form.getLabel());
                    addressSelector.revalidate();
                }
            }

            @Override
            public void deleted(ModernAddressForm form) {
                addressBody.remove(form);
                ChipToggle chip = addressChips.remove(form);
                if (chip != null) {
                    addressGroup.remove(chip);
                }
                resetAddressSelector();
                showAddress(MAIN_ADDRESS);
            }
        });
        f.setDataParent(dataOwner);
        return f;
    }

    private void addAddressForm(final ModernAddressForm f) {
        final String key = "address" + System.identityHashCode(f);
        f.setName(key);
        addressBody.add(f, key);
        ChipToggle chip = new ChipToggle(f.getLabel());
        chip.addActionListener(e -> showAddress(key));
        addressGroup.add(chip);
        addressChips.put(f, chip);
    }

    private void resetAddressSelector() {
        addressSelector.removeAll();
        addressSelector.add(mainAddressChip);
        for (ChipToggle c : addressChips.values()) {
            addressSelector.add(c);
        }
        addressSelector.add(addAddressButton);
        //additional addresses can be saved for saved contacts only
        addAddressButton.setEnabled(dataOwner == null || dataOwner.isExisting());
        addressSelector.revalidate();
        addressSelector.repaint();
    }

    private void showAddress(String key) {
        addressCards.show(addressBody, key);
        if (MAIN_ADDRESS.equals(key)) {
            mainAddressChip.setSelected(true);
        }
        //the card height follows the shown address
        Component grid = SwingUtilities.getAncestorOfClass(ResponsiveGrid.class, addressBody);
        if (grid != null) {
            grid.revalidate();
            grid.repaint();
        }
    }

    private void addAddress() {
        ModernAddressForm f = newAddressForm();
        f.setDataOwner(ContactService.newAddress(dataOwner), true);
        addAddressForm(f);
        resetAddressSelector();
        addressChips.get(f).setSelected(true);
        showAddress(f.getName());
    }

    private void properties() {
        final MPTableModel m = ContactService.loadProperties(dataOwner);
        proptable.setModel(m);
        m.addTableModelListener(e -> {
            int row = e.getLastRow();
            if (!dataOwner.isExisting() || row < 0 || row >= m.getRowCount()) {
                return;
            }
            Object k = m.getData()[row][0];
            Object v = m.getValueAt(row, 1);
            String key = k == null ? "" : String.valueOf(k);
            String value = v == null ? "" : String.valueOf(v);
            boolean changed;
            if (value.length() == 0) {
                changed = ContactService.updateProperty(dataOwner, key, "");
            } else {
                changed = e.getColumn() == 1 && key.length() > 0
                        && ContactService.updateProperty(dataOwner, key, String.valueOf(m.getData()[row][1]));
            }
            if (changed) {
                SwingUtilities.invokeLater(this::properties);
            }
        });
    }

    private void sendMail() {
        if (mailaddress.hasText()) {
            try {
                Desktop.getDesktop().mail(new URI("mailto:" + mailaddress.getText().trim()));
            } catch (Exception ex) {
                Log.Debug(ex);
                Popup.error(ex);
            }
        }
    }

    private void openWebsite() {
        if (website.hasText()) {
            String url = website.getText().trim();
            if (!url.matches("(?i)^[a-z][a-z0-9+.-]*://.*")) {
                url = "https://" + url;
            }
            try {
                Desktop.getDesktop().browse(new URI(url));
            } catch (Exception ex) {
                Log.Debug(ex);
                Popup.error(ex);
            }
        }
    }

    private void showHistory() {
        if (dataOwner.isExisting()) {
            mpv5.YabsViewProxy.instance().getIdentifierView().addTab(new JournalPanel(dataOwner), Messages.HISTORY_OF + dataOwner.__getCname());
        }
    }

    private void showSchedule() {
        if (dataOwner.isExisting()) {
            new ScheduleDayEvents(Schedule.getEvents(dataOwner));
        }
    }

    private void loadEInvoiceSettings() {
        List<ContactService.AccountOption> options = ContactService.accountOptions(dataOwner);
        einvoiceAccount.setModel(new DefaultComboBoxModel<Object>(options.toArray()));
        einvoiceAccount.setSelectedIndex(ContactService.selectedAccount(options, mpv5.utils.export.BankAccount.contactIban(dataOwner)));
        einvoiceImmediate.setSelected(PaymentTerms.isImmediate(dataOwner));
        updateImmediate();
        einvoiceSkontoPercent.setText(ContactService.paymentValue(dataOwner, PaymentTerms.KEY_SKONTO_PERCENT));
        einvoiceSkontoDays.setText(ContactService.paymentValue(dataOwner, PaymentTerms.KEY_SKONTO_DAYS));
        einvoiceDelivery.setSelectedItem(EInvoiceDelivery.forContact(dataOwner));
        einvoiceLeitweg.setText(ContactService.leitweg(dataOwner));
    }

    private void saveEInvoiceSettings() {
        Object account = einvoiceAccount.getSelectedItem();
        ContactService.saveEInvoiceSettings(dataOwner,
                (EInvoiceDelivery) einvoiceDelivery.getSelectedItem(),
                einvoiceLeitweg.getText(),
                account instanceof ContactService.AccountOption ? ((ContactService.AccountOption) account).iban : "",
                einvoiceImmediate.isSelected(),
                einvoiceSkontoPercent.getText(),
                einvoiceSkontoDays.getText());
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="DataPanel">
    @Override
    public DatabaseObject getDataOwner() {
        return dataOwner;
    }

    @Override
    public void setDataOwner(DatabaseObject object, boolean populate) {
        dataOwner = (Contact) object;
        if (!populate) {
            return;
        }
        //the role chips are set while loading: no list reloads until the end
        loading = true;
        try {
            dataOwner.setPanelData(this);
            exposeData();
        } finally {
            loading = false;
        }

        if (object.isExisting()) {
            setTabTitle(object.__getCname());
        }
        //a newly loaded contact starts at the top of the overview
        javax.swing.SwingUtilities.invokeLater(() -> {
            java.awt.Component c = tabs.getComponentAt(0);
            if (c instanceof javax.swing.JScrollPane) {
                ((javax.swing.JScrollPane) c).getVerticalScrollBar().setValue(0);
            }
        });
        printing.init(dataOwner);
        tb.setFavourite(Favourite.isFavourite(object));
        tb.setEditable(!object.isReadOnly());

        addAddresses();
        if (object.isReadOnly()) {
            Popup.notice(Messages.LOCKED_BY);
        }
        related = null;
        chosenRelated = ContactService.defaultRelated(dataOwner);
        updateRoleControls();
        showRelated(chosenRelated);
        properties();
        loadEInvoiceSettings();
        updateHeader();
    }

    @Override
    public boolean collectData() {
        String[] companyAndName = ContactService.companyAndName(company.isSelected(),
                companyselect.getText(), cname.get_Text());
        iscompany_ = company.isSelected();
        company_ = companyAndName[0];
        cname_ = companyAndName[1];
        if (company_.length() == 0 && cname_.length() == 0) {
            return false;
        }
        city_ = city.get_Text();
        taxnumber_ = taxnumber.get_Text();

        if (groupnameselect.getSelectedItem() != null) {
            Group g = ContactService.getGroup(groupnameselect.getSelectedItem().getId());
            if (g != null) {
                group_ = g;
            }
        }
        if (countryselect.getSelectedItem() != null) {
            country_ = String.valueOf(((MPComboBoxModelItem) countryselect.getSelectedItem()).getId());
        } else {
            country_ = "";
        }
        issupplier_ = supplier.isSelected();
        ismanufacturer_ = manufacturer.isSelected();
        iscustomer_ = customer.isSelected();
        isenabled_ = true;
        fax_ = fax.get_Text();
        mailaddress_ = mailaddress.get_Text();
        ismale_ = male.isSelected();
        mobilephone_ = mobilephone.get_Text();
        notes_ = notes.getText();
        cnumber_ = number.get_Text();
        mainphone_ = mainphone.get_Text();
        prename_ = prename.get_Text();
        street_ = street.get_Text();
        title_ = title.get_Text();
        website_ = website.get_Text();
        workphone_ = workphone.get_Text();
        zip_ = zip.get_Text();
        bankaccount_ = bankaccount.getText();
        bankcountry_ = bankcountry.getText();
        bankcurrency_ = bankcurrency.getText();
        bankid_ = bankid.getText();
        bankname_ = bankname.getText();
        payterm_ = payterm.getValue(0);
        if (dateadded_ == null) {
            dateadded_ = new Date();
        }
        if (intaddedby_ <= 0) {
            intaddedby_ = User.getCurrentUser().__getIDS();
        }
        department_ = department.get_Text();

        dataOwner.setisCustomer(iscustomer_);
        dataOwner.setisManufacturer(ismanufacturer_);
        dataOwner.setisSupplier(issupplier_);
        return true;
    }

    @Override
    public void exposeData() {
        city.set_Text(city_);
        cname.set_Text(cname_);
        taxnumber.set_Text(taxnumber_);
        company.setSelected(iscompany_);
        try {
            companyselect.setModel(new String[][]{{company_, company_}});
        } catch (Exception e) {
            Log.Debug(e);
        }
        try {
            groupnameselect.setModel(group_);
        } catch (Exception e) {
            Log.Debug(e);
        }
        try {
            if (country_ == null || country_.length() == 0) {
                country_ = User.getCurrentUser().__getDefcountry();
            }
            countryselect.setSelectedIndex(MPComboBoxModelItem.getItemID(country_, countryselect.getModel()));
        } catch (Exception e) {
            Log.Debug(e);
        }
        customer.setSelected(iscustomer_);
        supplier.setSelected(issupplier_);
        manufacturer.setSelected(ismanufacturer_);
        fax.set_Text(fax_);
        mailaddress.set_Text(mailaddress_);
        male.setSelected(ismale_);
        female.setSelected(!ismale_);
        mobilephone.set_Text(mobilephone_);
        notes.setText(notes_);
        notes.setCaretPosition(0);
        number.set_Text(cnumber_);
        mainphone.set_Text(mainphone_);
        prename.set_Text(prename_);
        street.set_Text(street_);
        title.set_Text(title_);
        website.set_Text(website_);
        workphone.set_Text(workphone_);
        zip.set_Text(zip_);
        department.set_Text(department_);
        bankaccount.setText(bankaccount_);
        bankcountry.setText(bankcountry_);
        bankcurrency.setText(bankcurrency_);
        bankid.setText(bankid_);
        bankname.setText(bankname_);
        payterm.setText(payterm_);
        fillFiles();
        updateHeader();
    }

    @Override
    public void refresh() {
        SwingUtilities.invokeLater(() -> {
            try {
                groupnameselect.setModel(DatabaseObject.getObject(Context.getGroup(), User.getCurrentUser().__getGroupsids()));
                sp.refresh();
                fillFiles();
            } catch (Exception e) {
                Log.Debug(ModernContactPanel.this, e);
            }
        });
    }

    @Override
    public void paste(DatabaseObject... dbos) {
        for (DatabaseObject dbo : dbos) {
            if (dbo.getDbIdentity().equals(Context.getContact().getDbIdentity())) {
                setDataOwner(dbo, true);
            } else {
                mpv5.YabsViewProxy.instance().addMessage(Messages.NOT_POSSIBLE.toString() + Messages.ACTION_PASTE.toString(), Color.RED);
            }
        }
    }

    @Override
    public void showRequiredFields() {
        tabs.setSelectedIndex(0);
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
        saveEInvoiceSettings();
    }

    @Override
    public void actionAfterCreate() {
        sp.refresh();
        saveEInvoiceSettings();
    }

    @Override
    public void actionBeforeCreate() {
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
        //the address as typed, also before it is saved
        if (mailaddress.hasText()) {
            sendMail();
            return;
        }
        try {
            Desktop.getDesktop().mail(new URI("mailto:" + dataOwner.__getMailaddress()));
        } catch (Exception ex) {
            Log.Debug(this, ex.getMessage());
            Popup.error(ex);
        }
    }

    @Override
    public void print() {
        Export.print(this);
    }
    // </editor-fold>
}
