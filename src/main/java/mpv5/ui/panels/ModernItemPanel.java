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
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.KeyboardFocusManager;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.table.TableCellRenderer;
import mpv5.YabsViewProxy;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.db.common.NodataFoundException;
import mpv5.db.objects.Contact;
import mpv5.db.objects.Favourite;
import mpv5.db.objects.Group;
import mpv5.handler.ContactService;
import mpv5.db.objects.Item;
import mpv5.db.objects.MailMessage;
import mpv5.db.objects.Product;
import mpv5.db.objects.ProductList;
import mpv5.db.objects.ProductlistSubItem;
import mpv5.db.objects.SubItem;
import mpv5.db.objects.Template;
import mpv5.db.objects.User;
import mpv5.globals.Constants;
import mpv5.globals.Headers;
import mpv5.globals.Messages;
import mpv5.handler.ItemService;
import mpv5.handler.TemplateHandler;
import mpv5.logging.Log;
import mpv5.ui.beans.LabeledCombobox;
import mpv5.ui.beans.LabeledDateChooser;
import mpv5.ui.beans.LabeledTextField;
import mpv5.ui.beans.MPCBSelectionChangeReceiver;
import mpv5.ui.dialogs.BigPopup;
import mpv5.ui.dialogs.DialogForFile;
import mpv5.ui.dialogs.Notificator;
import mpv5.ui.dialogs.Popup;
import mpv5.ui.dialogs.ScheduleDayEvent;
import mpv5.ui.dialogs.TableView;
import mpv5.ui.dialogs.subcomponents.ItemNumberEditor;
import mpv5.ui.misc.MPTable;
import mpv5.ui.misc.Position;
import mpv5.ui.misc.TableViewPersistenceHandler;
import mpv5.ui.modern.Avatar;
import mpv5.ui.modern.Card;
import mpv5.ui.modern.ModernTheme;
import mpv5.ui.modern.ResponsiveGrid;
import mpv5.ui.modern.WrapLayout;
import mpv5.ui.popups.FileTablePopUp;
import mpv5.ui.toolbars.ModernCommandBar;
import mpv5.usermanagement.MPSecurityManager;
import mpv5.utils.arrays.ArrayUtilities;
import mpv5.utils.date.DateConverter;
import mpv5.utils.export.EInvoiceValidator;
import mpv5.utils.export.Export;
import mpv5.utils.export.ZugpferdFile;
import mpv5.utils.files.FileDirectoryHandler;
import mpv5.utils.jobs.Job;
import mpv5.utils.jobs.Waiter;
import mpv5.utils.models.MPComboBoxModelItem;
import mpv5.utils.models.MPComboboxModel;
import mpv5.utils.models.MPTableModel;
import mpv5.utils.numberformat.FormatNumber;
import mpv5.utils.renderer.ProgressCellRender;
import mpv5.utils.renderer.TextAreaCellRenderer;
import mpv5.utils.tables.TableFormat;
import mpv5.utils.ui.TextFieldUtils;

/**
 * Offers, orders, invoices, deposits, part payments and credit notes in the
 * modern layout: a header with number, short description and status, the
 * command bar with all document actions, the data in cards, and the positions
 * with their totals as the main area.
 * <p>
 * Same functions as {@link ItemPanel2} (the classic view); which one is used
 * is decided by {@link mpv5.YabsViewProxy}. The logic which is not about the
 * screen is in {@link ItemService}, the positions table in
 * {@link ItemPositionsTable}.
 * <p>
 * The public fields ending with an underscore are filled from / copied to the
 * document by name, so they must keep their names.
 */
public class ModernItemPanel extends ModernDataPanel implements MPCBSelectionChangeReceiver, ExportablePanel {

    private static final long serialVersionUID = 1L;
    private Item dataOwner;
    private final ModernCommandBar tb;
    private boolean loading = true;
    private String savedSnapshot;
    private final List<Runnable> afterSaveRunnables = new ArrayList<Runnable>();
    //header
    private final Avatar avatar = new Avatar();
    private final JLabel displayname = new JLabel();
    private final StatusPill statusPill = new StatusPill();
    private final JLabel meta = new JLabel();
    //customer
    private final LabeledCombobox contactname = ModernTheme.plain(new LabeledCombobox());
    private final LabeledTextField prename = field();
    private final LabeledTextField cname = field();
    private final LabeledTextField street = field();
    private final LabeledTextField zip = field();
    private final LabeledTextField city = field();
    //document
    private final LabeledTextField number = field();
    private final LabeledTextField name = field();
    private final LabeledCombobox status = ModernTheme.plain(new LabeledCombobox());
    private final LabeledCombobox groupnameselect = ModernTheme.plain(new LabeledCombobox());
    private final LabeledCombobox accountselect = ModernTheme.plain(new LabeledCombobox());
    private final LabeledCombobox refOrder = ModernTheme.plain(new LabeledCombobox());
    private final JButton refOrderShow = action(text("ItemPanel2.refOrderShow.text"));
    private JComponent refOrderRow;
    /**
     * credit notes: the invoice it corrects or cancels (#211)
     */
    private final LabeledCombobox refInvoice = ModernTheme.plain(new LabeledCombobox());
    private JComponent refInvoiceRow;
    private JButton numberEditButton;
    private JButton removeFileButton;
    //dates
    private final LabeledDateChooser date1 = ModernTheme.plain(new LabeledDateChooser());
    private final LabeledDateChooser date2 = ModernTheme.plain(new LabeledDateChooser());
    private final LabeledDateChooser date3 = ModernTheme.plain(new LabeledDateChooser());
    /**
     * when the document was saved last (not editable)
     */
    private final JLabel lastUpdated = new JLabel();
    //free text
    private final LabeledCombobox messages = ModernTheme.plain(new LabeledCombobox());
    private final JTextArea notes = new JTextArea(4, 20);
    //positions, files, properties
    private final ItemPositionsTable positions = new ItemPositionsTable();
    private final JTable filetable = tooltipTable(this);
    //payments received (part payments) of invoices
    private final javax.swing.table.DefaultTableModel paymentsModel = new javax.swing.table.DefaultTableModel(
            new Object[]{Messages.PAYMENT_DATE.toString(), Messages.PAYMENT_AMOUNT.toString(), Messages.PAYMENT_NOTE.toString()}, 0) {
        @Override
        public boolean isCellEditable(int r, int c) {
            return false;
        }
    };
    private final JTable paymentsTable = tooltipTable(this);
    private final java.util.List<Integer> paymentIds = new java.util.ArrayList<Integer>();
    private final JLabel paymentsInfo = new JLabel();
    private Card paymentsCard;
    //actions
    private final JButton previewButton = new JButton(Messages.ACTION_PREVIEW.toString());
    private final JMenuItem deliveryNote = new JMenuItem(text("ItemPanel2.button_deliverynote.text"));
    private final JMenuItem orderConfirmation = new JMenuItem(text("ItemPanel2.button_orderconf.text"));
    private final javax.swing.JCheckBox printWithConfirmation = new javax.swing.JCheckBox(text("ItemPanel2.checkb_pront_oc.text"));
    /**
     * The user property of "print with order confirmation" (as the
     * {@link mpv5.ui.beans.UserCheckbox} of the classic view stores it)
     */
    private static final String OC_PROPERTY_PREFIX = "org.openyabs.uiproperty";
    private static final String OC_PROPERTY = "orderconfirmationalways";
    private final JButton documentsButton = new JButton(Messages.ITEM_MORE_DOCUMENTS + " ▾");
    private final JButton einvoiceButton = new JButton(Messages.CREATE_X_INVOICE + " ▾");
    private final JButton createButton = new JButton(Messages.ITEM_CREATE + " ▾");
    private final JMenuItem toOrder = new JMenuItem(text("ItemPanel2.toorder.text_1"));
    private final JMenuItem toInvoice = new JMenuItem(text("ItemPanel2.toinvoice._Label"));
    private final JMenuItem toDeposit = new JMenuItem(Messages.CREATE_DEPOSIT.toString());
    private final JMenuItem toPartPayment = new JMenuItem(Messages.CREATE_PART_PAYMENT.toString());
    private final JMenuItem toCredit = new JMenuItem(text("ItemPanel2.tocredit.text"));
    private final JButton remindButton = new JButton(text("ItemPanel2.button_reminders.text"));
    private final JButton scheduleButton = new JButton(text("ItemPanel2.button_schedule.text"));
    private final JButton compareButton = new JButton(text("ItemPanel2.button_compare.text"));
    private final JButton referencesButton = new JButton(text("ItemPanel2.button_references.text"));
    // <editor-fold defaultstate="collapsed" desc="Data fields, filled by name from / copied to the document">
    public String cname_;
    public String cnumber_;
    public String description_;
    public int intaddedby_;
    public int ids_;
    public Date dateadded_;
    public Group group_;
    public int contactsids_;
    public int accountsids_;
    public BigDecimal netvalue_;
    public BigDecimal taxvalue_;
    public BigDecimal shippingvalue_;
    public BigDecimal discountvalue_;
    public BigDecimal discountgrosvalue_;
    public Date datetodo_;
    public Date dateend_;
    public Date datelastupdated_;
    public int intreminders_;
    public int intstatus_;
    public Integer reforderids_;
    public Integer refinvoiceids_;
    public int inttype_;
    // </editor-fold>

    /**
     * @param context the context of the document type
     * @param type Item.TYPE_*, -1 if not known yet
     */
    public ModernItemPanel(Context context, int type) {
        super(new BorderLayout());
        setName("itempanel");
        inttype_ = type;
        dataOwner = (Item) DatabaseObject.getObject(context);
        if (type >= 0) {
            dataOwner.setInttype(type);
        }
        initFields(context);

        JPanel content = new JPanel(new BorderLayout());
        content.setOpaque(false);
        content.add(createHeader(), BorderLayout.NORTH);
        //the positions are the main part of a document
        JSplitPane split = split("item", scroll(createOverview()), createPositions(), 0.45);
        split.setResizeWeight(0.4);
        content.add(split, BorderLayout.CENTER);
        add(content, BorderLayout.CENTER);
        tb = new ModernCommandBar(this);
        add(tb, BorderLayout.NORTH);
        createActions();
        addPropertyChangeListener(PROPERTY_CONTENT_LOCKED, e -> applyContentLock());
        //a copy made or saved: the actions that need a saved record
        addPropertyChangeListener("yabs.copyOf", e -> javax.swing.SwingUtilities.invokeLater(this::updateTypeControls));

        intaddedby_ = User.getCurrentUser().__getIDS();
        dateadded_ = new Date();
        refreshSync();
        updateTypeControls();
        updateHeader();
        loading = false;
    }

    /**
     * @param context
     */
    public ModernItemPanel(Context context) {
        this(context, -1);
    }

    // <editor-fold defaultstate="collapsed" desc="Layout">
    private void initFields(Context context) {
        contactname.setSearchEnabled(true);
        contactname.setContext(Context.getCustomer());
        //the last entry of the list: a new customer for this document
        contactname.setActionEntry(Messages.ITEM_NEW_CUSTOMER_ENTRY.toString(), this::newCustomer);
        contactname.getComboBox().addActionListener(e -> {
            if (loading) {
                return;
            }
            final MPComboBoxModelItem item = contactname.getSelectedItem();
            if (item != null && item.isValid()) {
                SwingUtilities.invokeLater(() -> {
                    try {
                        Contact dbo = (Contact) DatabaseObject.getObject(Context.getContact(), Integer.valueOf(item.getId()));
                        setContactData(dbo);
                        setEnddate(dbo);
                        updateHeader();
                        updateEInvoiceButton(dbo);
                        BigDecimal rate = ContactService.customerTaxRate(dbo);
                        positions.setCustomerTaxRate(rate);
                        if (rate != null) {
                            positions.applyCustomerTaxRate(rate);
                        }
                    } catch (Exception ex) {
                        Log.Debug(ModernItemPanel.this, ex.getMessage());
                    }
                });
            }
        });
        accountselect.setContext(Context.getAccounts());
        accountselect.setSearchEnabled(true);
        groupnameselect.setContext(Context.getGroup());
        groupnameselect.setSearchEnabled(true);
        date1.setDate(new Date());
        //the service date: empty means the document date
        date2.setDate(null);
        date2.getDateChooser().setToolTipText(Messages.ITEM_DATE_SERVICE_HINT.toString());
        lastUpdated.setForeground(ModernTheme.muted());

        number.setSearchOnEnterEnabled(true);
        number.setParent(this);
        number.setSearchField("cnumber");
        number.setContext(ItemService.contextOf(inttype_ < 0 ? Item.TYPE_INVOICE : inttype_));
        number.getTextField().setEnabled(true);
        onChange(name);
        onChange(number);

        status.getComboBox().addActionListener(e -> statusChanged());

        messages.setContext(Context.getMessage());
        messages.setSearchOnEnterEnabled(true);
        messages.getComboBox().addActionListener(e -> {
            try {
                MailMessage d = (MailMessage) DatabaseObject.getObject(Context.getMessage(), Integer.valueOf(messages.getSelectedItem().getId()));
                notes.setText(d.__getDescription());
            } catch (Exception ex) {
                Log.Debug(ex);
            }
        });
        notes.setLineWrap(true);
        notes.setWrapStyleWord(true);
        ModernTheme.roomy(notes);
        ModernTheme.tabLeaves(notes);
        positions.setTabTargets(notes, date3.getDateChooser());

        refOrderShow.addActionListener(e -> {
            try {
                Item owner = (Item) DatabaseObject.getObject(Context.getOrder(), reforderids_, true);
                YabsViewProxy.instance().addOrShowTab(owner);
            } catch (Exception ex) {
                Log.Debug(ex);
            }
        });
        filetable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evt) {
                fileTableClicked(evt);
            }
        });
    }

    private void onChange(LabeledTextField f) {
        f.getTextField().getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                updateHeader();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                updateHeader();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                updateHeader();
            }
        });
    }

    private JComponent createHeader() {
        JPanel header = new JPanel(new BorderLayout(ModernTheme.px(12), 0));
        header.setOpaque(false);
        header.setBorder(ModernTheme.padding(4, 12));
        avatar.setSquare(true);
        header.add(avatar, BorderLayout.WEST);
        //the main information of the view: large
        displayname.setFont(ModernTheme.font(12, Font.BOLD));
        displayname.setForeground(ModernTheme.foreground());
        displayname.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, ModernTheme.px(10)));
        //contact / date / user on the same line, compact
        meta.setFont(ModernTheme.font(2, Font.PLAIN));
        meta.setBorder(BorderFactory.createEmptyBorder(0, ModernTheme.px(12), 0, 0));
        meta.setForeground(ModernTheme.muted());
        JPanel leftRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        leftRow.setOpaque(false);
        leftRow.add(displayname);
        leftRow.add(statusPill);
        leftRow.add(createPluginBadgeSlot());
        JPanel row = new JPanel(new BorderLayout(ModernTheme.px(10), 0));
        row.setOpaque(false);
        row.add(leftRow, BorderLayout.WEST);
        row.add(meta, BorderLayout.CENTER);
        header.add(row, BorderLayout.CENTER);
        return header;
    }

    private JComponent createOverview() {
        ResponsiveGrid grid = new ResponsiveGrid(320, 3).arrangeable("item");

        JButton showContact = action(text("ItemPanel2.jButton3.text"));
        showContact.addActionListener(e -> showContact());
        JPanel contactRow = new JPanel(new BorderLayout(ModernTheme.px(4), 0));
        contactRow.setOpaque(false);
        contactRow.add(contactname, BorderLayout.CENTER);
        contactRow.add(showContact, BorderLayout.EAST);
        grid.add(new Card(Messages.ITEM_SECTION_CUSTOMER.toString())
                .row(Card.field(text("ItemPanel2.contactname._Label"), contactRow))
                .row(Card.field(text("ItemPanel2.prename._Label"), prename),
                        Card.field(text("ItemPanel2.cname._Label"), cname))
                .row(Card.field(text("ItemPanel2.street._Label"), street))
                .row(Card.field(text("ItemPanel2.zip._Label"), zip, 1),
                        Card.field(text("ItemPanel2.city._Label"), city, 2)));

        JPanel numberRow = new JPanel(new BorderLayout(ModernTheme.px(4), 0));
        numberRow.setOpaque(false);
        numberRow.add(number, BorderLayout.CENTER);
        if (MPSecurityManager.checkAdminAccess()) {
            //change the number (administrators)
            JButton edit = action("…");
            edit.setToolTipText(text("ItemPanel2.number._Label"));
            edit.addActionListener(e -> editNumber());
            numberRow.add(edit, BorderLayout.EAST);
            numberEditButton = edit;
        }
        JPanel refRow = new JPanel(new BorderLayout(ModernTheme.px(4), 0));
        refRow.setOpaque(false);
        refRow.add(refOrder, BorderLayout.CENTER);
        refRow.add(refOrderShow, BorderLayout.EAST);
        refOrderRow = refRow;
        grid.add(new Card(Messages.ITEM_SECTION_DOCUMENT.toString())
                .row(Card.field(text("ItemPanel2.number._Label"), numberRow, 1),
                        Card.field(text("ItemPanel2.status._Label"), status, 1))
                .row(Card.field(text("ItemPanel2.name._Label"), name))
                .row(Card.field(text("ItemPanel2.groupnameselect._Label"), groupnameselect, 1),
                        Card.field(text("ItemPanel2.accountselect._Label"), accountselect, 1))
                .row(Card.field(text("ItemPanel2.refOrder._Label"), refRow))
                .row(Card.field(Messages.ITEM_CORRECTED_INVOICE.toString(), refInvoiceRow = wrap(refInvoice))));
        refInvoice.setToolTipText(Messages.ITEM_CORRECTED_INVOICE_HINT.toString());

        grid.add(new Card(Messages.ITEM_SECTION_DATES.toString())
                .row(Card.field(Messages.ITEM_DATE_DOCUMENT.toString(), date1))
                .row(Card.field(Messages.ITEM_DATE_SERVICE.toString(), date2))
                .row(Card.field(text("ItemPanel2.date3._Label"), date3))
                .row(Card.field(Messages.ITEM_DATE_LASTUPDATED.toString(), lastUpdated)));

        grid.add(new Card(Messages.ITEM_SECTION_TEXT.toString())
                .row(Card.field(text("ItemPanel2.labeledCombobox1._Label"), messages))
                .addFull(new JScrollPane(notes), 1));

        JButton addFile = link(null, "/images/16/add.png");
        addFile.setToolTipText(text("ItemPanel2.addfile.toolTipText"));
        addFile.addActionListener(e -> addFile());
        JButton removeFile = link(null, "/images/16/remove.png");
        removeFile.setToolTipText(text("ItemPanel2.removefile.toolTipText"));
        removeFile.addActionListener(e -> deleteFile());
        removeFileButton = removeFile;
        JPanel fileButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, ModernTheme.px(4), 0));
        fileButtons.setOpaque(false);
        fileButtons.add(addFile);
        fileButtons.add(removeFile);
        JScrollPane files = new JScrollPane(filetable);
        files.setPreferredSize(new java.awt.Dimension(ModernTheme.px(200), ModernTheme.px(100)));
        grid.add(new Card(Messages.ITEM_SECTION_FILES.toString()).headerAction(fileButtons).addFull(files, 1));

        paymentsTable.setModel(paymentsModel);
        ModernTheme.style(paymentsTable);
        amountColumn(paymentsTable, 1);
        JButton addPayment = link(null, "/images/16/add.png");
        addPayment.setToolTipText(Messages.PAYMENT_ADD.toString());
        addPayment.addActionListener(e -> addPayment());
        JButton removePayment = link(null, "/images/16/remove.png");
        removePayment.setToolTipText(Messages.PAYMENT_REMOVE.toString());
        removePayment.addActionListener(e -> removePayment());
        JPanel paymentButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, ModernTheme.px(4), 0));
        paymentButtons.setOpaque(false);
        paymentButtons.add(addPayment);
        paymentButtons.add(removePayment);
        JScrollPane payments = new JScrollPane(paymentsTable);
        payments.setPreferredSize(new java.awt.Dimension(ModernTheme.px(200), ModernTheme.px(100)));
        paymentsInfo.setForeground(ModernTheme.muted());
        paymentsCard = new Card(Messages.PAYMENT_SECTION.toString()).headerAction(paymentButtons)
                .addFull(paymentsInfo).addFull(payments, 1);
        grid.add(paymentsCard);
        return grid;
    }

    private JComponent createPositions() {
        Card card = new Card(Messages.ITEM_SECTION_POSITIONS.toString());
        //the totals end the card: less room below them than below fields
        card.setBorder(BorderFactory.createEmptyBorder(ModernTheme.px(12), ModernTheme.px(14), ModernTheme.px(6), ModernTheme.px(14)));
        card.getHeader().add(positions.detachToolbar(), BorderLayout.CENTER);
        card.addFull(positions, 1);
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(ModernTheme.background());
        p.setBorder(BorderFactory.createEmptyBorder(ModernTheme.px(6), 0, 0, 0));
        p.add(card, BorderLayout.CENTER);
        return p;
    }

    private static JButton menuButton(final JButton b, final JPopupMenu menu) {
        b.putClientProperty(mpv5.ui.modern.OverflowBar.PROPERTY_POPUP_MENU, menu);
        b.addActionListener(e -> menu.show(b, 0, b.getHeight()));
        return b;
    }

    /**
     * The actions of a document: in the bar directly above the positions
     */
    private void createActions() {
        //a locked document with an archived file: show that file, do not create a new one
        //(the preview is in the bar at the top: not here again)
        previewButton.addActionListener(e -> {
            if (!mpv5.ui.toolbars.RecordActions.openLockedDocument(this, true)) {
                preview(dataOwner.__getInttype());
            }
        });

        JPopupMenu docs = new JPopupMenu();
        deliveryNote.addActionListener(e -> preview(Constants.TYPE_DELIVERY_NOTE));
        orderConfirmation.addActionListener(e -> preview(Constants.TYPE_ORDER_CONFIRMATION));
        docs.add(deliveryNote);
        docs.add(orderConfirmation);
        //remembered per user, the same setting as in the classic view
        printWithConfirmation.setSelected(User.getCurrentUser().getProperties().getProperty(OC_PROPERTY_PREFIX, OC_PROPERTY));
        printWithConfirmation.setToolTipText(Messages.PRINT_WITH_OC_TIP.toString());
        printWithConfirmation.addActionListener(e -> User.getCurrentUser().setPropertyAndSave(
                OC_PROPERTY_PREFIX + "$" + OC_PROPERTY, String.valueOf(printWithConfirmation.isSelected())));
        //a setting of print / PDF: next to them in the bar at the top
        printWithConfirmation.setFocusable(false);
        tb.addAction(printWithConfirmation, null);
        positions.addAction(menuButton(documentsButton, docs));

        JPopupMenu einvoice = new JPopupMenu();
        JMenuItem xml = new JMenuItem(Messages.CREATE_XRECHNUNG.toString());
        xml.addActionListener(e -> createEInvoice(ZugpferdFile.Format.XRECHNUNG));
        JMenuItem pdf = new JMenuItem(Messages.CREATE_ZUGFERD.toString());
        pdf.addActionListener(e -> createEInvoice(ZugpferdFile.Format.ZUGFERD));
        JMenuItem check = new JMenuItem(Messages.ZUGFERD_VALIDATE_FILE.toString());
        check.addActionListener(e -> validateEInvoiceFile());
        JMenuItem openLocked = new JMenuItem(Messages.LOCKED_DOCUMENT_OPEN.toString());
        openLocked.addActionListener(e -> mpv5.ui.toolbars.RecordActions.openLockedDocument(this));
        einvoice.add(openLocked);
        einvoice.add(xml);
        einvoice.add(pdf);
        einvoice.addSeparator();
        einvoice.add(check);
        //locked with an archived e-invoice: only open the existing file, no new one
        einvoice.addPopupMenuListener(new javax.swing.event.PopupMenuListener() {
            @Override
            public void popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent e) {
                java.io.File locked = getLockedDocument();
                openLocked.setVisible(locked != null);
                openLocked.setToolTipText(locked == null ? null : locked.getPath());
                xml.setEnabled(locked == null);
                pdf.setEnabled(locked == null);
            }

            @Override
            public void popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent e) {
            }

            @Override
            public void popupMenuCanceled(javax.swing.event.PopupMenuEvent e) {
            }
        });
        positions.addAction(menuButton(einvoiceButton, einvoice));

        JPopupMenu create = new JPopupMenu();
        toOrder.addActionListener(e -> convertToOrder());
        toInvoice.addActionListener(e -> convertToInvoice(Item.TYPE_INVOICE));
        toDeposit.addActionListener(e -> convertToInvoice(Item.TYPE_DEPOSIT));
        toPartPayment.addActionListener(e -> convertToInvoice(Item.TYPE_PART_PAYMENT));
        toCredit.addActionListener(e -> convertToInvoice(Item.TYPE_CREDIT));
        create.add(toOrder);
        create.add(toInvoice);
        //no new deposits (the same as a partial payment); existing ones are still deducted
        create.add(toPartPayment);
        create.addSeparator();
        create.add(toCredit);
        positions.addAction(menuButton(createButton, create));

        remindButton.addActionListener(e -> {
            if (dataOwner != null && dataOwner.isExisting()) {
                BigPopup.showPopup(YabsViewProxy.instance().getIdentifierFrame().getRootPane(), new RemindPanel(dataOwner), Messages.REMINDER.toString(), false);
            }
        });
        scheduleButton.addActionListener(e -> {
            if (dataOwner != null && dataOwner.isExisting()) {
                ScheduleDayEvent.instanceOf().setItem(dataOwner);
            }
        });
        compareButton.addActionListener(e -> compareToOrder());
        referencesButton.addActionListener(e -> showReferences());
        positions.addAction(remindButton);
        positions.addAction(scheduleButton);
        positions.addAction(compareButton);
        positions.addAction(referencesButton);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="State of the controls">
    /**
     * The e-invoice menu: not if PDF and print create the e-invoice anyway
     * (ZUGFeRD for the customer, also as the default)
     *
     * @param contact the customer, null: the one of the document
     */
    private void updateEInvoiceButton(Contact contact) {
        boolean supported = ZugpferdFile.supports(dataOwner);
        boolean zugferdAnyway = false;
        if (supported) {
            try {
                Contact c = contact != null ? contact
                        : dataOwner.__getContactsids() > 0 ? dataOwner.getContact() : null;
                zugferdAnyway = mpv5.utils.export.EInvoiceDelivery.effectiveFor(c, dataOwner)
                        == mpv5.utils.export.EInvoiceDelivery.ZUGFERD;
            } catch (Exception e) {
                Log.Debug(this, e.getMessage());
            }
        }
        einvoiceButton.setVisible(supported && !zugferdAnyway);
    }

    /**
     * Shows the actions and fields which belong to the type of the document
     */
    private void updateTypeControls() {
        int t = inttype_;
        boolean bill = ItemService.isBill(t);
        remindButton.setVisible(t == Item.TYPE_INVOICE);
        scheduleButton.setVisible(t == Item.TYPE_INVOICE);
        compareButton.setVisible(t == Item.TYPE_INVOICE);
        referencesButton.setVisible(t == Item.TYPE_ORDER);
        refOrderShow.setVisible(t == Item.TYPE_INVOICE);
        if (refOrderRow != null) {
            //the whole field row
            Component row = refOrderRow.getParent();
            (row != null ? row : refOrderRow).setVisible(bill);
        }
        if (refInvoiceRow != null) {
            Component row = refInvoiceRow.getParent();
            (row != null ? row : refInvoiceRow).setVisible(t == Item.TYPE_CREDIT);
        }
        toOrder.setEnabled(t != Item.TYPE_ORDER && t != Item.TYPE_INVOICE);
        toInvoice.setEnabled(t != Item.TYPE_INVOICE);
        boolean partialAllowed = t != Item.TYPE_INVOICE && t != Item.TYPE_DEPOSIT && t != Item.TYPE_PART_PAYMENT;
        toDeposit.setEnabled(partialAllowed);
        toPartPayment.setEnabled(partialAllowed);
        //a credit note refers to a saved invoice (not to a copy not saved yet)
        toCredit.setEnabled(t == Item.TYPE_INVOICE && dataOwner != null && dataOwner.isExisting());
        if (paymentsCard != null) {
            paymentsCard.setVisible(bill);
        }
        updateEInvoiceButton(null);
        number.setContext(ItemService.contextOf(t < 0 ? Item.TYPE_INVOICE : t));
    }

    private void updateHeader() {
        String typeName = inttype_ >= 0 ? Item.getTypeString(inttype_) : "";
        String no = number.getTextField().getText();
        String n = name.getTextField().getText();
        String title;
        if (no != null && !no.isEmpty()) {
            title = typeName + " " + no + (n != null && !n.isEmpty() ? "  ·  " + n : "");
        } else {
            title = Messages.ITEM_NEW + (typeName.isEmpty() ? "" : ": " + typeName);
        }
        displayname.setText(title);
        //two letters of the type, e.g. "IN" for invoice
        avatar.setName(typeName.length() >= 2 ? typeName.substring(0, 1) + " " + typeName.substring(1, 2) : "?");
        StringBuilder m = new StringBuilder();
        MPComboBoxModelItem c = contactname.getSelectedItem();
        if (c != null && c.isValid()) {
            m.append(c.getValue()).append("   ·   ");
        }
        if (date1.getDate() != null) {
            m.append(DateConverter.getDefDateString(date1.getDate()));
        }
        String by = User.getUsername(intaddedby_);
        if (by != null && !by.isEmpty()) {
            m.append(" · ").append(by);
        }
        meta.setText(m.toString());
        MPComboBoxModelItem s = status.getSelectedItem();
        try {
            statusPill.setStatus(s == null ? -1 : Integer.parseInt(String.valueOf(s.getId())));
        } catch (NumberFormatException e) {
            statusPill.setStatus(-1);
        }
    }

    private void statusChanged() {
        updateHeader();
        MPComboBoxModelItem s = status.getSelectedItem();
        if (loading || !dataOwner.isExisting() || s == null) {
            return;
        }
        boolean paid = Integer.parseInt(String.valueOf(s.getId())) == Item.STATUS_PAID;
        if (paid && ItemService.isBill(dataOwner.__getInttype())
                && User.getCurrentUser().getProperties().getProperty("org.openyabs.uiproperty", "autocreaterevenue")
                && Popup.Y_N_dialog(Messages.BOOK_NOW)) {
            if (dataOwner.getPanelData(this) && dataOwner.save()) {
                try {
                    actionAfterSave();
                    savedSnapshot = captureSnapshot();
                    dataOwner.createRevenue();
                } catch (Exception ef) {
                    Log.Debug(this, ef);
                }
            } else {
                showRequiredFields();
            }
        }
        if (paid && !User.getCurrentUser().getProperties().getProperty("org.openyabs.itemproperty", "keepmodifiedenddate")) {
            date3.setDate(new Date());
        }
    }

    /**
     * the orders an invoice can refer to, of the customer refChoicesContact
     */
    private MPComboBoxModelItem[] refChoices;
    private int refChoicesContact = -1;

    private void setContactData(Contact dbo) {
        prename.setText(dbo.getDefaultaddress().__getPrename());
        cname.setText(dbo.getDefaultaddress().__getCname());
        zip.setText(dbo.getDefaultaddress().__getZip());
        city.setText(dbo.getDefaultaddress().__getCity());
        street.setText(dbo.getDefaultaddress().__getStreet());
        if (inttype_ == Item.TYPE_CREDIT) {
            //the invoices of the customer the credit note can correct
            refInvoice.setModel(new MPComboboxModel(ItemService.referenceInvoiceChoices(dbo)));
            refInvoice.setSelectedIndex(-1);
            if (refinvoiceids_ != null && refinvoiceids_ > 0) {
                refInvoice.setSelectedItem(refinvoiceids_);
            }
        }
        if (ItemService.isBill(inttype_)) {
            if (dataOwner.__getRefOrderIDS() != null && dataOwner.__getRefOrderIDS() > 0) {
                try {
                    refOrder.setModel((Item) DatabaseObject.getObject(Context.getOrder(), reforderids_, true));
                } catch (NodataFoundException ex) {
                    Log.Debug(ex);
                }
            } else {
                //opening a document set the customer twice (directly and by the selection listener):
                //the orders of the customer (hundreds) were read twice
                if (refChoices == null || refChoicesContact != dbo.__getIDS()) {
                    refChoices = ItemService.referenceOrderChoices(dbo);
                    refChoicesContact = dbo.__getIDS();
                }
                refOrder.setModel(new MPComboboxModel(refChoices.clone()));
                refOrder.setEditable(true);
                refOrder.setSelectedIndex(-1);
            }
        }
    }

    /**
     * @param c
     * @return a panel holding c (its parent row can be hidden)
     */
    private static JComponent wrap(JComponent c) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.add(c, BorderLayout.CENTER);
        return p;
    }

    private void setEnddate(Contact dbo) {
        Date d = ItemService.dueDate(dbo, dataOwner.isExisting());
        if (d != null) {
            date3.setDate(d);
        }
    }

    /**
     * Defaults of a new document; loads the document if it is saved
     */
    private void refreshSync() {
        try {
            groupnameselect.setModel(MPComboBoxModelItem.toModel(DatabaseObject.getObject(Context.getGroup(), User.getCurrentUser().__getGroupsids())));
            groupnameselect.setSelectedIndex(0);
            try {
                accountselect.setModel(DatabaseObject.getObject(Context.getAccounts(), User.getCurrentUser().__getIntdefaultaccount()));
            } catch (NodataFoundException e) {
                Log.Debug(this, e.getMessage());
            }
            status.setModel(Item.getStatusStrings(), MPComboBoxModelItem.COMPARE_BY_ID, ItemService.statusesToSkip(inttype_, false));
            try {
                status.setSelectedIndex(User.getCurrentUser().__getIntdefaultstatus());
            } catch (Exception e) {
                Log.Debug(this, e.getMessage());
            }
            positions.setDefaultPositions();
        } catch (Exception e) {
            Log.Debug(this, e);
        }
        if (dataOwner.isExisting()) {
            setDataOwner(dataOwner, true);
        }
    }

    public void setItemStatus(int statusId) {
        this.intstatus_ = statusId;
        status.setSelectedItem(statusId);
        updateHeader();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Actions">
    private void showContact() {
        try {
            int cid = Integer.parseInt(String.valueOf(contactname.getSelectedItem().getId()));
            YabsViewProxy.instance().getIdentifierView().addTab(DatabaseObject.getObject(Context.getContact(), cid));
        } catch (Exception e) {
            Log.Debug(this, e.getMessage());
        }
    }

    private void editNumber() {
        if (MPSecurityManager.checkAdminAccess()) {
            JDialog d = new JDialog(YabsViewProxy.instance().getIdentifierFrame(), true);
            d.add(new ItemNumberEditor(dataOwner, d, this));
            d.pack();
            d.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            new Position(d).center();
            d.setVisible(true);
        }
    }

    private void convertToOrder() {
        dataOwner.getPanelData(this);
        positions.stopEditing();
        Item i2 = ItemService.toOrder(dataOwner, positions.getModel());
        //the order in a new tab, this one keeps the offer (issue #58)
        mpv5.YabsViewProxy.instance().addTab(i2, true);
        Popup.notice(i2 + Messages.INSERTED.getValue());
    }

    private void convertToInvoice(int itemType) {
        dataOwner.getPanelData(this);
        positions.stopEditing();
        Item i2 = ItemService.toInvoice(dataOwner, itemType, positions.getModel(), q -> Popup.Y_N_dialog(q));
        if (i2 != null) {
            //the new document in a new tab, this one keeps the order (issue #58)
            mpv5.YabsViewProxy.instance().addTab(i2, true);
            Popup.notice(i2 + Messages.INSERTED.getValue());
        }
    }

    /**
     * The payments of the invoice and the amount still open
     */
    private void refreshPayments() {
        paymentsModel.setRowCount(0);
        paymentIds.clear();
        if (dataOwner == null || !dataOwner.isExisting() || !ItemService.isBill(dataOwner.__getInttype())) {
            paymentsInfo.setText(Messages.PAYMENT_HINT.toString());
            return;
        }
        for (mpv5.handler.Payments.Payment p : mpv5.handler.Payments.of(dataOwner.__getIDS())) {
            paymentIds.add(p.id);
            paymentsModel.addRow(new Object[]{DateConverter.getDefDateString(p.date), p.amount, p.description});
        }
        java.math.BigDecimal paid = mpv5.handler.Payments.paid(dataOwner.__getIDS());
        java.math.BigDecimal over = mpv5.handler.Payments.overpayment(dataOwner);
        if (paymentIds.isEmpty()) {
            paymentsInfo.setForeground(ModernTheme.muted());
            paymentsInfo.setText(Messages.PAYMENT_HINT.toString());
        } else if (over.signum() > 0) {
            paymentsInfo.setForeground(new java.awt.Color(180, 83, 9));
            paymentsInfo.setText(String.format(Messages.PAYMENT_STATE_OVERPAID.toString(),
                    FormatNumber.formatLokalCurrency(paid),
                    FormatNumber.formatLokalCurrency(over)));
        } else {
            paymentsInfo.setForeground(ModernTheme.muted());
            paymentsInfo.setText(String.format(Messages.PAYMENT_STATE.toString(),
                    FormatNumber.formatLokalCurrency(paid),
                    FormatNumber.formatLokalCurrency(mpv5.handler.Payments.open(dataOwner))));
        }
    }

    /**
     * Asks for amount (the open amount), date (today) and a note, records the
     * payment; complete: the invoice is paid
     */
    private void addPayment() {
        if (dataOwner == null || !dataOwner.isExisting()) {
            Popup.notice(Messages.PAYMENT_SAVE_FIRST);
            return;
        }
        java.math.BigDecimal open = mpv5.handler.Payments.open(dataOwner);
        javax.swing.JTextField amount = new javax.swing.JTextField(FormatNumber.formatDezimal(open), 12);
        javax.swing.JTextField date = new javax.swing.JTextField(DateConverter.getDefDateString(new Date()), 12);
        javax.swing.JTextField note = new javax.swing.JTextField(20);
        JPanel form = new JPanel(new java.awt.GridLayout(0, 2, ModernTheme.px(8), ModernTheme.px(6)));
        form.add(new JLabel(Messages.PAYMENT_AMOUNT.toString()));
        form.add(amount);
        form.add(new JLabel(Messages.PAYMENT_DATE.toString()));
        form.add(date);
        form.add(new JLabel(Messages.PAYMENT_NOTE.toString()));
        form.add(note);
        if (javax.swing.JOptionPane.showConfirmDialog(this, form, Messages.PAYMENT_ADD.toString(),
                javax.swing.JOptionPane.OK_CANCEL_OPTION, javax.swing.JOptionPane.PLAIN_MESSAGE) != javax.swing.JOptionPane.OK_OPTION) {
            return;
        }
        java.math.BigDecimal a;
        Date d;
        try {
            a = FormatNumber.parseDezimal(amount.getText());
            d = DateConverter.getDate(date.getText().trim());
        } catch (Exception e) {
            a = null;
            d = null;
        }
        if (a == null || a.signum() <= 0 || d == null) {
            Popup.notice(Messages.PAYMENT_INVALID);
            return;
        }
        String noteText = note.getText().trim();
        if (a.compareTo(open) > 0) {
            java.math.BigDecimal surplus = a.subtract(open).setScale(2, java.math.RoundingMode.HALF_UP);
            String msg = String.format(Messages.PAYMENT_OVERPAY_MSG.toString(),
                    FormatNumber.formatLokalCurrency(a),
                    FormatNumber.formatLokalCurrency(open),
                    FormatNumber.formatLokalCurrency(surplus));
            java.util.List<String> opts = new java.util.ArrayList<String>();
            if (open.signum() > 0) {
                opts.add(String.format(Messages.PAYMENT_OVERPAY_OPEN_ONLY.toString(), FormatNumber.formatLokalCurrency(open)));
            }
            opts.add(Messages.PAYMENT_OVERPAY_AS_CREDIT.toString());
            opts.add(Messages.PAYMENT_OVERPAY_AS_IS.toString());
            opts.add(Messages.NUMWIZ_CANCEL.toString());
            int choice = javax.swing.JOptionPane.showOptionDialog(this, msg, Messages.PAYMENT_OVERPAY_TITLE.toString(),
                    javax.swing.JOptionPane.DEFAULT_OPTION, javax.swing.JOptionPane.WARNING_MESSAGE,
                    null, opts.toArray(), opts.get(0));
            if (choice < 0 || choice == opts.size() - 1) {
                return;
            }
            if (open.signum() > 0 && choice == 0) {
                a = open;
            } else if ((open.signum() > 0 && choice == 1) || (open.signum() == 0 && choice == 0)) {
                String creditNote = String.format(Messages.PAYMENT_OVERPAY_NOTE.toString(), FormatNumber.formatLokalCurrency(surplus));
                noteText = noteText.isEmpty() ? creditNote : noteText + " · " + creditNote;
            }
        }
        int before = dataOwner.__getIntstatus();
        if (mpv5.handler.Payments.add(dataOwner, a, d, noteText)) {
            afterPaymentChange(before);
        }
    }

    private void removePayment() {
        int r = paymentsTable.getSelectedRow();
        if (r < 0 || dataOwner == null) {
            return;
        }
        int before = dataOwner.__getIntstatus();
        mpv5.handler.Payments.remove(dataOwner, paymentIds.get(paymentsTable.convertRowIndexToModel(r)));
        afterPaymentChange(before);
    }

    /**
     * The status changed (paid / open again): show the stored record
     */
    private void afterPaymentChange(int statusBefore) {
        if (dataOwner.__getIntstatus() != statusBefore) {
            setDataOwner(dataOwner, true);
            YabsViewProxy.instance().addMessage(dataOwner + ": " + Item.getStatusString(dataOwner.__getIntstatus()), Color.GREEN);
        } else {
            refreshPayments();
        }
    }

    private void compareToOrder() {
        if (reforderids_ == null || reforderids_ <= 0) {
            return;
        }
        TableView tv = new TableView();
        JTable table = tv.getTable();
        table.setModel(new MPTableModel(ItemService.compareToOrder(reforderids_), Headers.ITEM_COMPARE.getValue(),
                new Class[]{String.class, String.class, String.class, String.class, String.class, Object.class}));
        TableFormat.resizeCols(table, new Integer[]{75, 75, 75, 300, 75, 200}, new Boolean[]{false, false, false, false, false, false});
        //red: more used than ordered, or not ordered at all
        TableCellRenderer red = new javax.swing.table.DefaultTableCellRenderer() {
            private static final long serialVersionUID = 1L;

            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, col);
                setHorizontalAlignment(CENTER);
                boolean notOrdered = t.getValueAt(row, 0) instanceof BigDecimal && ((BigDecimal) t.getValueAt(row, 0)).signum() == 0;
                if (notOrdered && col == 4) {
                    setText("---");
                }
                float st = t.getValueAt(row, 5) instanceof Number ? ((Number) t.getValueAt(row, 5)).floatValue() : 0f;
                setBackground(st > 1 || notOrdered ? new Color(255, 200, 200) : Color.WHITE);
                return this;
            }
        };
        for (int c : new int[]{0, 1, 2, 4}) {
            table.getColumnModel().getColumn(c).setCellRenderer(red);
        }
        TextAreaCellRenderer.setMAXLINES(10);
        new TextAreaCellRenderer(table).setRendererTo(3);
        table.getColumnModel().getColumn(5).setCellRenderer(new ProgressCellRender());
        table.setRowHeight(100);
        tv.setVisible(true);
    }

    private void showReferences() {
        List<DatabaseObject> pm = ItemService.references(dataOwner);
        TableView tv = new TableView();
        JTable table = tv.getTable();
        Object[][] d = new Object[pm.size()][2];
        for (int i = 0; i < pm.size(); i++) {
            d[i][0] = pm.get(i);
            d[i][1] = pm.get(i).getType();
        }
        table.setModel(new MPTableModel(d, Headers.ITEM_REFERENCE.getValue(), new Class[]{String.class, String.class, DatabaseObject.class}));
        TableFormat.resizeCols(table, new Integer[]{75, 75}, new Boolean[]{false, false});
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                if (e.getClickCount() == 2 && row >= 0) {
                    YabsViewProxy.instance().addOrShowTab((DatabaseObject) table.getValueAt(row, 0));
                }
            }
        });
        tv.setVisible(true);
    }

    @Override
    public void preview() {
        if (mpv5.ui.toolbars.RecordActions.openLockedDocument(this, true)) {
            return;
        }
        if (dataOwner != null) {
            preview(dataOwner.__getInttype());
        }
    }

    private void preview(int templateType) {
        if (!ensureSavedForExport()) {
            return;
        }
        if (dataOwner != null && dataOwner.isExisting()) {
            if (TemplateHandler.ensureLoaded(Long.valueOf(dataOwner.templateGroupIds()), templateType)) {
                PreviewWaiter pr = new PreviewWaiter();
                pr.setDataOwner(dataOwner);
                Export.createFileWithProgress(() -> TemplateHandler.loadTemplate(dataOwner.templateGroupIds(), templateType), dataOwner, pr);
            } else {
                Popup.notice(Messages.NO_TEMPLATE_LOADED + " (" + User.getCurrentUser() + ")");
            }
        }
    }

    private void preloadTemplates() {
        final Item d = dataOwner;
        new Thread(() -> {
            TemplateHandler.loadTemplateFor(previewButton, d.templateGroupIds(), d.__getInttype());
            TemplateHandler.loadTemplateFor(deliveryNote, d.templateGroupIds(), Constants.TYPE_DELIVERY_NOTE);
            TemplateHandler.loadTemplateFor(new JComponent[]{orderConfirmation, printWithConfirmation}, d.templateGroupIds(), Constants.TYPE_ORDER_CONFIRMATION);
            TemplateHandler.loadTemplateFor(remindButton, d.templateGroupIds(), Constants.TYPE_REMINDER);
        }).start();
    }

    // <editor-fold defaultstate="collapsed" desc="E-invoice (as in ItemPanel2)">
    private void createEInvoice(final ZugpferdFile.Format format) {
        if (mpv5.ui.toolbars.RecordActions.openLockedDocument(this)) {
            return;
        }
        if (!ensureSavedForExport()) {
            return;
        }
        if (!ZugpferdFile.supports(dataOwner)) {
            Popup.notice(Messages.ZUGFERD_NOT_AN_INVOICE);
            return;
        }
        if (format == ZugpferdFile.Format.ZUGFERD
                && !TemplateHandler.ensureLoaded(Long.valueOf(dataOwner.templateGroupIds()), dataOwner.__getInttype())) {
            Popup.notice(Messages.NO_TEMPLATE_LOADED + " (" + User.getCurrentUser() + ")");
            return;
        }
        final mpv5.db.objects.Item item = dataOwner;
        //reading the data (database) and checking it: not on the event dispatch thread
        mpv5.utils.export.ExportProgress.run(Messages.EXPORT_EINVOICE + " " + item.__getCnumber(), () -> {
            mpv5.utils.export.ExportProgress.step(Messages.EXPORT_STEP_EINVOICE_DATA);
            org.mustangproject.Invoice inv = ZugpferdFile.createInvoice(item, format);
            mpv5.utils.export.ExportProgress.step(Messages.EXPORT_STEP_EINVOICE_CHECK);
            List<String> found = ZugpferdFile.validate(inv);
            Contact contact = item.getContact();
            String buyerVatId = ZugpferdFile.checkBuyerVatId(contact, contact.getInvoiceaddress());
            if (buyerVatId != null) {
                found.add(buyerVatId);
            }
            return new Object[]{inv, found};
        }, (r, failure) -> {
            if (failure != null) {
                Log.Debug(this, failure);
                Popup.error(failure);
            } else {
                @SuppressWarnings("unchecked")
                List<String> found = (List<String>) r[1];
                createEInvoice(format, (org.mustangproject.Invoice) r[0], found);
            }
        });
    }

    /**
     * Refuses if data is missing, else asks where to save; creates the e-invoice in
     * the background
     */
    private void createEInvoice(final ZugpferdFile.Format format, final org.mustangproject.Invoice invoice, List<String> problems) {
        if (!problems.isEmpty()) {
            StringBuilder text = new StringBuilder(Messages.ZUGFERD_MISSING_DATA.toString()).append("\n");
            for (String problem : problems) {
                text.append("\n - ").append(problem);
            }
            //an e-invoice with missing data would be rejected: not created
            Popup.warn(Messages.EINVOICE_NOT_CREATED + ", " + text);
            return;
        }
        File dir = User.getSaveDir(dataOwner);
        dir.mkdirs();
        if (!dir.isDirectory()) {
            dir = DialogForFile.CURRENT_DIR;
        }
        String fname = dataOwner.getFormatHandler().toUserString().replaceAll("[\\\\/:*?\"<>|]", "_") + "." + format.getExtension();
        DialogForFile d = new DialogForFile(DialogForFile.FILES_ONLY);
        d.setFileFilter(format == ZugpferdFile.Format.XRECHNUNG ? DialogForFile.XML_FILES : DialogForFile.PDF_FILES);
        d.setSelectedFile(new File(dir, fname));
        if (d.saveFile()) {
            File chosen = d.getFile();
            if (!chosen.getName().toLowerCase().endsWith("." + format.getExtension())) {
                chosen = new File(chosen.getPath() + "." + format.getExtension());
            }
            final File target = chosen;
            ItemService.ensureFinishedAndEndDate(dataOwner);
            loading = true;
            try {
                dataOwner.setPanelData(this);
                exposeData();
                updateHeader();
                savedSnapshot = captureSnapshot();
            } finally {
                loading = false;
            }
            final mpv5.db.objects.Item item = dataOwner;
            mpv5.utils.export.ExportProgress.execute(Messages.EXPORT_EINVOICE + " " + target.getName(), null, () -> new Job(new ZugpferdFile(item, invoice, target, format), new Waiter() {
                @Override
                public void set(Object object, Exception exception) throws Exception {
                    if (exception != null) {
                        Popup.error(exception);
                    } else {
                        showEInvoiceValidation(target, ((ZugpferdFile) object).getValidation());
                    }
                }
            }), null);
        }
    }

    private void validateEInvoiceFile() {
        DialogForFile d = new DialogForFile(DialogForFile.FILES_ONLY);
        d.setFileFilter(new javax.swing.filechooser.FileFilter() {
            @Override
            public boolean accept(File f) {
                String n = f.getName().toLowerCase();
                return f.isDirectory() || n.endsWith(".xml") || n.endsWith(".pdf");
            }

            @Override
            public String getDescription() {
                return Messages.CREATE_X_INVOICE + " (*.xml, *.pdf)";
            }
        });
        if (d.chooseFile()) {
            final File file = d.getFile();
            mpv5.utils.export.ExportProgress.execute(Messages.ZUGFERD_VALIDATE_FILE + " " + file.getName(), Messages.EXPORT_STEP_VALIDATE, () -> new Job(new EInvoiceValidator(file), new Waiter() {
                @Override
                public void set(Object object, Exception exception) throws Exception {
                    if (exception != null) {
                        Popup.error(exception);
                    } else {
                        showEInvoiceValidation(file, ((EInvoiceValidator) object).getResult());
                    }
                }
            }), null);
        }
    }

    private void showEInvoiceValidation(File file, EInvoiceValidator.Result result) {
        if (result == null) {
            Popup.warn(Messages.ZUGFERD_CREATED + ": " + file + "\n\n" + Messages.ZUGFERD_NOT_VALIDATED);
        } else if (result.isValid()) {
            Notificator.raiseNotification(Messages.ZUGFERD_VALID + ": " + file, false);
        } else {
            StringBuilder text = new StringBuilder(Messages.ZUGFERD_INVALID.toString()).append("\n").append(file).append("\n");
            List<String> problems = result.getProblems();
            int max = Math.min(problems.size(), 12);
            for (int i = 0; i < max; i++) {
                text.append("\n - ").append(problems.get(i));
            }
            if (problems.size() > max) {
                text.append("\n ... (").append(problems.size() - max).append(")");
            }
            Popup.warn(text.toString());
        }
    }
    // </editor-fold>

    private void addFile() {
        if (dataOwner.isExisting()) {
            DialogForFile d = new DialogForFile(DialogForFile.FILES_ONLY);
            if (d.chooseFile()) {
                String s = Popup.Enter_Value(Messages.ENTER_A_DESCRIPTION);
                if (s != null) {
                    mpv5.db.common.QueryHandler.instanceOf().clone(Context.getFiles(), this)
                            .insertFile(d.getFile(), dataOwner, mpv5.db.common.QueryCriteria.getSaveStringFor(s));
                }
            }
        }
    }

    private void deleteFile() {
        if (dataOwner.isExisting() && filetable.getSelectedRow() >= 0) {
            try {
                DatabaseObject.getObject(Context.getFilesToItems(), "filename",
                        filetable.getModel().getValueAt(filetable.convertRowIndexToModel(filetable.getSelectedRow()), 0).toString()).delete();
            } catch (Exception e) {
                Log.Debug(this, e.getMessage());
            }
            fillFiles();
        }
    }

    private void fileTableClicked(MouseEvent evt) {
        if (evt.getClickCount() > 1 && filetable.getSelectedRow() >= 0) {
            int row = filetable.convertRowIndexToModel(filetable.getSelectedRow());
            FileDirectoryHandler.open(mpv5.db.common.QueryHandler.instanceOf().clone(Context.getFiles()).retrieveFile(
                    filetable.getModel().getValueAt(row, 0).toString(),
                    new File(FileDirectoryHandler.getTempDir() + filetable.getModel().getValueAt(row, 1).toString())));
        } else if (evt.getClickCount() == 1 && evt.getButton() == MouseEvent.BUTTON3) {
            int row = filetable.rowAtPoint(evt.getPoint());
            if (row >= 0 && !filetable.isRowSelected(row)) {
                filetable.changeSelection(row, filetable.columnAtPoint(evt.getPoint()), false, false);
            }
            FileTablePopUp.instanceOf(filetable).show(filetable, evt.getX(), evt.getY());
        }
    }

    private void fillFiles() {
        if (dataOwner == null || !dataOwner.isExisting()) {
            filetable.setModel(new MPTableModel());
            return;
        }
        Context c = Context.getFilesToItems();
        c.addReference(Context.getFiles().getDbIdentity(), "cname", "filename");
        Object[][] data = new mpv5.db.common.DatabaseSearch(c).getValuesFor(Context.DETAILS_FILES_TO_ITEMS, "itemsids", dataOwner.__getIDS());
        filetable.setModel(new MPTableModel(data, Headers.FILE_REFERENCES.getValue()));
        TableFormat.stripFirstColumn(filetable);
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="DataPanel">
    @Override
    public DatabaseObject getDataOwner() {
        return dataOwner;
    }

    /**
     * Locks or unlocks the content fields of the document while keeping payment
     * status, partial payments, reminders, and preview/export actions usable.
     */
    private void applyContentLock() {
        boolean editable = !isContentLocked();
        contactname.setEnabled(editable);
        prename.setEnabled(editable);
        cname.setEnabled(editable);
        street.setEnabled(editable);
        zip.setEnabled(editable);
        city.setEnabled(editable);
        number.setEnabled(editable);
        if (numberEditButton != null) {
            numberEditButton.setEnabled(editable);
        }
        name.setEnabled(editable);
        groupnameselect.setEnabled(editable);
        accountselect.setEnabled(editable);
        refOrder.setEnabled(editable);
        refInvoice.setEnabled(editable);
        date1.setEnabled(editable);
        date2.setEnabled(editable);
        date3.setEnabled(editable);
        messages.setEnabled(editable);
        notes.setEditable(editable);
        if (removeFileButton != null) {
            removeFileButton.setEnabled(editable);
        }
        positions.setEditable(editable);
    }

    @Override
    public void setDataOwner(DatabaseObject object, boolean populate) {
        loading = true;
        try {
            if (object instanceof Item) {
                dataOwner = (Item) object;
                if (populate) {
                    dataOwner.setPanelData(this);
                    inttype_ = dataOwner.__getInttype();
                    updateTypeControls();
                    exposeData();
                    setTabTitle(cname_);
                    tb.setFavourite(Favourite.isFavourite(object));
                    tb.setEditable(!object.isReadOnly());
                    tb.updateCopyBanner();
                    BigDecimal rate = ContactService.customerTaxRate(dataOwner.__getContactsids());
                    positions.setCustomerTaxRate(rate);
                    positions.setPositions(dataOwner.getSubitems());
                    refreshPayments();
                    if (object.isReadOnly()) {
                        Popup.notice(Messages.LOCKED_BY);
                    }
                    previewButton.setEnabled(false);
                    preloadTemplates();
                    updateHeader();
                    validate();
                    notifyViewOpened();
                    savedSnapshot = dataOwner.isExisting() ? captureSnapshot() : null;
                }
            } else if (object instanceof SubItem) {
                try {
                    setDataOwner(DatabaseObject.getObject(Context.getInvoice(), ((SubItem) object).__getItemsids()), populate);
                } catch (NodataFoundException ex) {
                    Log.Debug(ex);
                }
            }
        } finally {
            loading = false;
        }
    }

    private boolean ensureSavedForExport() {
        return mpv5.ui.toolbars.RecordActions.saveBeforeExport(this);
    }

    @Override
    public boolean hasUnsavedChanges() {
        if (dataOwner == null) {
            return false;
        }
        if (isContentLocked() || getLockedDocument() != null || dataOwner.isReadOnly()) {
            return false;
        }
        positions.stopEditing();
        if (!dataOwner.isExisting() || mpv5.ui.toolbars.RecordActions.isPendingCopy(this)) {
            return true;
        }
        if (pendingCustomerPanel != null || !afterSaveRunnables.isEmpty() || !positions.getUsedOrders().isEmpty()) {
            return true;
        }
        return savedSnapshot == null || !savedSnapshot.equals(captureSnapshot());
    }

    private String captureSnapshot() {
        StringBuilder sb = new StringBuilder();
        sb.append(ItemPanelSnapshot.comboId(contactname)).append('\u001d')
                .append(ItemPanelSnapshot.comboId(accountselect)).append('\u001d')
                .append(ItemPanelSnapshot.comboId(groupnameselect)).append('\u001d')
                .append(ItemPanelSnapshot.text(number.getText())).append('\u001d')
                .append(ItemPanelSnapshot.text(name.getText())).append('\u001d')
                .append(ItemPanelSnapshot.comboId(status)).append('\u001d')
                .append(ItemPanelSnapshot.comboId(refOrder)).append('\u001d')
                .append(ItemPanelSnapshot.comboId(refInvoice)).append('\u001d')
                .append(ItemPanelSnapshot.date(date1)).append('\u001d')
                .append(ItemPanelSnapshot.date(date2)).append('\u001d')
                .append(ItemPanelSnapshot.date(date3)).append('\u001d')
                .append(ItemPanelSnapshot.text(notes.getText())).append('\u001d')
                .append(ItemPanelSnapshot.positions(positions.getModel()));
        return sb.toString();
    }

    // <editor-fold defaultstate="collapsed" desc="New customer from the customer box">
    /**
     * the tab of a new customer opened from the customer box (until saved or closed)
     */
    private DataPanel pendingCustomerPanel;
    private java.util.function.Consumer<DatabaseObject> pendingCustomerListener;

    /**
     * Opens a new customer (named as typed) in a tab. Saving it sets it as the
     * customer of this document and saves the document; saving the document
     * first saves the customer.
     */
    private void newCustomer() {
        dropPendingCustomer();
        Contact c = (Contact) DatabaseObject.getObject(Context.getCustomer());
        c.setisCustomer(true);
        String name = contactname.getTypedText() == null ? "" : contactname.getTypedText().trim();
        if (!name.isEmpty() && !name.equals(Messages.ITEM_NEW_CUSTOMER_ENTRY.toString())) {
            c.setCname(name);
        }
        final DataPanel p = YabsViewProxy.instance().addTab(c, true);
        if (p == null) {
            return;
        }
        pendingCustomerPanel = p;
        pendingCustomerListener = o -> {
            if (o instanceof Contact && pendingCustomerPanel != null && o == pendingCustomerPanel.getDataOwner()) {
                SwingUtilities.invokeLater(() -> customerSaved((Contact) o, true));
            }
        };
        DatabaseObject.addSaveListener(pendingCustomerListener);
        YabsViewProxy.instance().addMessage(Messages.ITEM_NEW_CUSTOMER_HINT.toString(), Color.GREEN);
    }

    private void dropPendingCustomer() {
        if (pendingCustomerListener != null) {
            DatabaseObject.removeSaveListener(pendingCustomerListener);
        }
        pendingCustomerListener = null;
        pendingCustomerPanel = null;
    }

    /**
     * The new customer was saved: it is the customer of this document
     *
     * @param c
     * @param saveDocument save this document now (the customer was saved in its tab)
     */
    private void customerSaved(Contact c, boolean saveDocument) {
        if (pendingCustomerPanel == null) {
            return;
        }
        dropPendingCustomer();
        contactname.setModel(c);
        setContactData(c);
        updateHeader();
        if (saveDocument) {
            mpv5.ui.toolbars.RecordActions.save(this);
            if (dataOwner != null && dataOwner.isExisting()) {
                showThisTab();
                YabsViewProxy.instance().addMessage(String.format(Messages.ITEM_NEW_CUSTOMER_SAVED.toString(), dataOwner, c), Color.GREEN);
            }
        }
    }

    /**
     * The new customer's tab is open and not saved yet: save it first (when
     * this document is saved); a closed tab is forgotten
     */
    private void saveNewCustomerFirst() {
        if (pendingCustomerPanel == null) {
            return;
        }
        if (!((java.awt.Component) pendingCustomerPanel).isDisplayable()) {
            dropPendingCustomer();
            return;
        }
        DatabaseObject o = pendingCustomerPanel.getDataOwner();
        if (o != null && !o.isExisting()) {
            mpv5.ui.toolbars.RecordActions.save(pendingCustomerPanel);
        }
        o = pendingCustomerPanel == null ? null : pendingCustomerPanel.getDataOwner();
        if (o instanceof Contact && o.isExisting()) {
            customerSaved((Contact) o, false);
        }
    }

    private void showThisTab() {
        javax.swing.JTabbedPane tabs = mpv5.ui.frames.MPView.getTabPane();
        for (int i = 0; i < tabs.getTabCount(); i++) {
            java.awt.Component t = tabs.getComponentAt(i);
            if (t == this || (t instanceof java.awt.Container && SwingUtilities.isDescendingFrom(this, t))) {
                tabs.setSelectedIndex(i);
                return;
            }
        }
    }

    /**
     * Cursor in the customer box, the list open (e.g. after creating a
     * document from products)
     */
    public void focusCustomer() {
        SwingUtilities.invokeLater(() -> {
            //the customer card in view (adding positions may have scrolled the overview)
            //with the title of the card above it
            int above = ModernTheme.px(56);
            contactname.scrollRectToVisible(new java.awt.Rectangle(0, -above, contactname.getWidth(), contactname.getHeight() + above));
            contactname.focusAndOpen();
        });
    }
    // </editor-fold>

    /**
     * The document has no customer: a small dialog to choose one or to create
     * one, prefilled with what was typed
     *
     * @return the customer, null if cancelled
     */
    private Contact askForCustomer() {
        mpv5.ui.dialogs.ChooseCustomerDialog.Prefill p = new mpv5.ui.dialogs.ChooseCustomerDialog.Prefill();
        String typed = contactname.getTypedText() == null ? "" : contactname.getTypedText().trim();
        if (typed.equals(Messages.ITEM_NEW_CUSTOMER_ENTRY.toString())) {
            typed = "";
        }
        p.search = typed;
        p.cname = cname.getText();
        p.prename = prename.getText();
        p.street = street.getText();
        p.zip = zip.getText();
        p.city = city.getText();
        return mpv5.ui.dialogs.ChooseCustomerDialog.choose(this, p);
    }

    @Override
    public boolean collectData() {
        saveNewCustomerFirst();
        try {
            contactsids_ = Integer.parseInt(String.valueOf(contactname.getSelectedItem().getId()));
        } catch (Exception e) {
            //no contact chosen: ask (choose an existing one or create a new one)
            Contact owner = askForCustomer();
            if (owner == null) {
                //cancelled: not saved
                return false;
            }
            contactname.setModel(owner);
            setContactData(owner);
            contactsids_ = owner.__getIDS();
        }
        if (contactsids_ <= 0) {
            showRequiredFields();
            return false;
        }
        try {
            accountsids_ = Integer.parseInt(String.valueOf(accountselect.getSelectedItem().getId()));
        } catch (Exception e) {
            accountsids_ = 1;
        }
        if (groupnameselect.getSelectedItem() != null) {
            try {
                group_ = (Group) DatabaseObject.getObject(Context.getGroup(), Integer.parseInt(String.valueOf(groupnameselect.getSelectedItem().getId())));
            } catch (NodataFoundException | NumberFormatException ex) {
                Log.Debug(this, ex);
            }
        }
        if (intaddedby_ <= 0) {
            intaddedby_ = User.getCurrentUser().__getIDS();
        }
        description_ = notes.getText();
        dateadded_ = date1.getDate() != null ? date1.getDate() : new Date();
        cnumber_ = number.getText();
        cname_ = name.getText();
        positions.stopEditing();
        BigDecimal[] totals = positions.getTotals();
        netvalue_ = totals[0];
        taxvalue_ = totals[1];
        discountvalue_ = totals[2];
        discountgrosvalue_ = totals[3];
        shippingvalue_ = BigDecimal.ZERO;
        //the service date (Leistungsdatum): the document date if empty
        datetodo_ = date2.getDate() != null ? date2.getDate() : dateadded_;
        dateend_ = date3.getDate();
        intstatus_ = Integer.parseInt(String.valueOf(status.getSelectedItem().getId()));
        try {
            reforderids_ = Integer.valueOf(String.valueOf(refOrder.getSelectedItem().getId()));
        } catch (Exception e) {
            reforderids_ = -1;
        }
        if (inttype_ == Item.TYPE_CREDIT) {
            try {
                MPComboBoxModelItem ri = refInvoice.getSelectedItem();
                refinvoiceids_ = ri != null && ri.isValid() ? Integer.valueOf(String.valueOf(ri.getId())) : 0;
            } catch (Exception e) {
                refinvoiceids_ = 0;
            }
        } else {
            //only credit notes refer to an invoice; never null (setRefinvoiceids takes an int:
            //null made every save of another document log an IllegalArgumentException)
            Integer stored = dataOwner == null ? null : dataOwner.__getRefinvoiceids();
            refinvoiceids_ = stored == null ? 0 : stored;
        }
        return true;
    }

    @Override
    public void exposeData() {
        refChoicesContact = -1;//a document (again): read the orders of its customer again
        number.setText(cnumber_);
        name.setText(cname_);
        //a document which is not saved yet has no date (it would show 1970)
        if (dateadded_ == null || dateadded_.getTime() <= 0) {
            dateadded_ = new Date();
        }
        date1.setDate(dateadded_);
        //the service date as saved (it used to be set to today when a document was opened)
        date2.setDate(datetodo_);
        lastUpdated.setText(datelastupdated_ == null || datelastupdated_.getTime() <= 0 ? "\u2013"
                : DateConverter.getDefDateString(datelastupdated_) + " " + new java.text.SimpleDateFormat("HH:mm").format(datelastupdated_));
        date3.setDate(User.getCurrentUser().getProperties().getProperty("org.openyabs.itemproperty", "keepmodifiedenddate") ? dateend_ : new Date());
        notes.setText(description_);
        notes.setCaretPosition(0);
        remindButton.setToolTipText(Messages.REMINDERS + String.valueOf(intreminders_));
        status.setModel(Item.getStatusStrings(), MPComboBoxModelItem.COMPARE_BY_ID, ItemService.statusesToSkip(inttype_, true));
        status.setSelectedItem(intstatus_);
        try {
            accountselect.setModel(DatabaseObject.getObject(Context.getAccounts(), accountsids_));
        } catch (NodataFoundException ex) {
            Log.Debug(this, ex.getMessage());
        }
        try {
            groupnameselect.setModel(group_);
        } catch (Exception ex) {
            Log.Debug(this, ex);
        }
        try {
            Contact owner = (Contact) DatabaseObject.getObject(Context.getContact(), contactsids_, true);
            contactname.setModel(owner);
            setContactData(owner);
            contactsids_ = owner.__getIDS();
        } catch (NodataFoundException ex) {
            Log.Debug(ex);
        }
        fillFiles();
        updateHeader();
    }

    @Override
    public final void refresh() {
        SwingUtilities.invokeLater(this::refreshSync);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void paste(DatabaseObject... dbos) {
        if (isContentLocked()) {
            return;
        }
        positions.stopEditing();
        try {
            positions.getModel().removeEmptyRows(new int[]{4});
        } catch (Exception e) {
            Log.Debug(e);
        }
        List<DatabaseObject> dbolist = new ArrayList<DatabaseObject>(Arrays.asList(dbos));
        Collections.sort(dbolist, (o1, o2) -> o1.__getDateadded().compareTo(o2.__getDateadded()));
        for (final DatabaseObject dbo : dbolist) {
            if (dbo.getContext().equals(Context.getInvoice()) || dbo.getContext().equals(Context.getOffer())
                    || dbo.getContext().equals(Context.getOrder())) {
                pasteItem((Item) dbo);
            } else if (dbo.getContext().equals(Context.getContact())) {
                dataOwner.setContactsids(((Contact) dbo).__getIDS());
                setDataOwner(dataOwner, true);
                BigDecimal rate = ContactService.customerTaxRate((Contact) dbo);
                positions.setCustomerTaxRate(rate);
                if (rate != null) {
                    positions.applyCustomerTaxRate(rate);
                }
            } else if (dbo.getContext().equals(Context.getProduct())) {
                positions.addPosition(new SubItem((Product) dbo, positions.getCustomerTaxRate()));
            } else if (dbo.getContext().equals(Context.getProductList())) {
                //the components of a bill of materials: one position each, like pasted products,
                //with the quantity and the price of the list
                try {
                    BigDecimal rate = positions.getCustomerTaxRate();
                    for (ProductlistSubItem c : ProductlistSubItem.getList(dbo.__getIDS())) {
                        SubItem s;
                        try {
                            s = new SubItem((Product) DatabaseObject.getObject(Context.getProduct(), c.__getOriginalproductsids()), rate);
                        } catch (NodataFoundException gone) {
                            //the product was deleted: the stored values
                            s = new SubItem();
                            s.setCname(c.getCname());
                            s.setDescription(c.__getDescription());
                            s.setMeasure(c.__getMeasure());
                            s.setTaxpercentvalue(rate != null ? rate : java.math.BigDecimal.valueOf(c.__getTaxpercentvalue()));
                        }
                        s.setQuantityvalue(java.math.BigDecimal.valueOf(c.__getQuantityvalue()));
                        s.setExternalvalue(java.math.BigDecimal.valueOf(c.__getExternalvalue()));
                        s.setInternalvalue(java.math.BigDecimal.valueOf(c.__getInternalvalue()));
                        positions.addPosition(s);
                    }
                } catch (NodataFoundException ex) {
                    Log.Debug(this, ex.getMessage());
                }
            } else if (dbo.getContext().equals(Context.getSubItem())) {
                positions.addPosition((SubItem) dbo);
            } else {
                YabsViewProxy.instance().addMessage(Messages.NOT_POSSIBLE.toString() + Messages.ACTION_PASTE.toString(), Color.RED);
            }
        }
        try {
            positions.getTable().changeSelection(0, 0, true, false);
        } catch (Exception e) {
            Log.Debug(e);
        }
        positions.recalculate();
    }

    /**
     * Pasting an offer / order / invoice: as one position ("pasten" setting),
     * else the document with its positions as new document of this type
     */
    private void pasteItem(final Item dbo) {
        Item o = (Item) dbo.clone();
        if (mpv5.db.objects.User.pasteAsSummary()) {
            //the document as position(s): one per VAT rate, net after discount
            String text = Messages.GOOSE1 + " " + dbo.__getCnumber() + " " + Messages.GOOSE2 + " " + DateConverter.getDefDateString(o.__getDateadded());
            for (SubItem s : ItemService.asPositions(dbo, text)) {
                positions.addPosition(s);
            }
            afterSaveRunnables.add(() -> {
                dbo.setIntstatus(Item.STATUS_FINISHED);
                dbo.save();
            });
        } else {
            o.setIntstatus(Item.STATUS_IN_PROGRESS);
            o.setInttype(inttype_);
            o.setCnumber("");
            o.setCname("");
            o.setDateadded(new Date());
            //the service date: the document date unless set
            o.setDatetodo(null);
            o.setDatelastupdated(null);
            o.setDateend(new Date());
            //the positions as they are (quantity, unit, price, VAT rate, discount, product)
            SubItem[] subs = ItemService.copyPositions(dbo);
            o.setIDS(-1);
            setDataOwner(o, true);
            positions.setModel(SubItem.toModel(subs, true));
        }
    }

    @Override
    public void showRequiredFields() {
        TextFieldUtils.blink(contactname.getComboBox(), Color.RED);
        contactname.requestFocus();
    }

    @Override
    public void showSearchBar(boolean show) {
        //no side search panel any more: the search button opens "Search anything"
    }

    @Override
    public void actionAfterSave() {
        positions.prepareSave();
        ItemService.savePositions(dataOwner, positions.getModel(), true, positions.getUsedOrders());
        setTabTitle(dataOwner.__getCname());
        for (Runnable r : new ArrayList<Runnable>(afterSaveRunnables)) {
            r.run();
        }
        afterSaveRunnables.clear();
    }

    @Override
    public void actionAfterCreate() {
        //the positions are new as well
        ArrayUtilities.replaceColumn(positions.getTable(), 0, null);
        positions.prepareSave();
        ItemService.savePositions(dataOwner, positions.getModel(), false, positions.getUsedOrders());
        setTabTitle(dataOwner.__getCname());
    }

    @Override
    public void changeSelection(MPComboBoxModelItem to, Context c) {
        try {
            DatabaseObject o = DatabaseObject.getObject(c, Integer.valueOf(to.getId()));
            JTable itemtable = positions.getTable();
            int i = itemtable.getSelectedRow();
            if (i >= 0) {
                Object opt = itemtable.getModel().getValueAt(i, 14);
                positions.getModel().setRowAt(new SubItem((Product) o, positions.getCustomerTaxRate()).getRowData(i), i, 4);
                itemtable.setValueAt(opt, i, 14);
            }
        } catch (Exception ex) {
            Log.Debug(ex);
        }
    }

    @Override
    public void actionBeforeCreate() {
        status.setSelectedIndex(Item.STATUS_IN_PROGRESS);
        date1.setDate(new Date());
        date2.setDate(null);
        lastUpdated.setText("\u2013");
        if (!User.getCurrentUser().getProperties().getProperty("org.openyabs.itemproperty", "keepmodifiedenddate")) {
            Contact dbo = null;
            MPComboBoxModelItem item = contactname.getSelectedItem();
            if (item != null && item.isValid()) {
                try {
                    dbo = (Contact) DatabaseObject.getObject(Context.getContact(), Integer.valueOf(item.getId()));
                } catch (NodataFoundException ex) {
                    Log.Debug(ex);
                }
            }
            setEnddate(dbo);
        }
    }

    @Override
    public void actionBeforeSave() throws ChangeNotApprovedException {
        //no new documents for a blocked contact (GDPR: kept only for the retention of its documents)
        if (!dataOwner.isExisting()) {
            try {
                MPComboBoxModelItem sel = contactname.getSelectedItem();
                if (sel != null && sel.isValid()) {
                    Contact c = (Contact) DatabaseObject.getObject(Context.getContact(), Integer.valueOf(sel.getId()));
                    if (mpv5.handler.Privacy.isBlocked(c)) {
                        Popup.notice(String.format(Messages.PRIVACY_NO_NEW_DOCUMENTS.toString(), c.__getCname()));
                        throw new ChangeNotApprovedException(dataOwner);
                    }
                }
            } catch (ChangeNotApprovedException e) {
                throw e;
            } catch (Exception e) {
                Log.Debug(this, e.getMessage());
            }
        }
        //small business exemption: no VAT on invoices, revenue limits (§ 19 UStG)
        if (dataOwner.__getInttype() == Item.TYPE_INVOICE && mpv5.utils.export.SmallBusiness.isEnabled()) {
            //called before collectData: the values of the form
            positions.stopEditing();
            BigDecimal[] totals = positions.getTotals();
            java.util.List<String> warnings = mpv5.utils.export.SmallBusiness.checkInvoice(
                    date1.getDate() != null ? date1.getDate() : new Date(), totals[0], totals[1],
                    !dataOwner.isExisting());
            if (!warnings.isEmpty() && !Popup.Y_N_dialog(String.join("\n\n", warnings) + "\n\n"
                    + Messages.SMALLBUSINESS_SAVE_ANYWAY)) {
                throw new ChangeNotApprovedException(dataOwner);
            }
        }
        if (dataOwner.isExisting()) {
            boolean done = dataOwner.__getIntstatus() == Item.STATUS_PAID || dataOwner.__getIntstatus() == Item.STATUS_CANCELLED;
            if (done && !Popup.Y_N_dialog(Messages.REALLY_CHANGE_DONE_ITEM)) {
                throw new ChangeNotApprovedException(dataOwner);
            }
            if (User.getCurrentUser().getProperties().getProperty("org.openyabs.uiproperty", "dowarnings")
                    && !Popup.Y_N_dialog(Messages.REALLY_CHANGE)) {
                throw new ChangeNotApprovedException(dataOwner);
            }
        }
    }

    @Override
    public void mail() {
        if (!ensureSavedForExport()) {
            return;
        }
        if (dataOwner != null && dataOwner.isExisting()) {
            if (TemplateHandler.ensureLoaded(Long.valueOf(dataOwner.templateGroupIds()), dataOwner.__getInttype())) {
                try {
                    Contact cont = (Contact) Contact.getObject(Context.getContact(), dataOwner.__getContactsids());
                    Export.mail(TemplateHandler.loadTemplate(dataOwner.templateGroupIds(), dataOwner.__getInttype()), dataOwner, cont, null);
                } catch (NodataFoundException ex) {
                    Log.Debug(ex);
                }
            } else {
                Popup.notice(Messages.NO_TEMPLATE_LOADED + " (" + User.getCurrentUser() + ")");
            }
        } else {
            Popup.notice(Messages.NOT_POSSIBLE + "\n" + Messages.NOT_SAVED_YET);
        }
    }

    @Override
    public void print() {
        if (mpv5.ui.toolbars.RecordActions.openLockedDocument(this)) {
            return;
        }
        if (!ensureSavedForExport()) {
            return;
        }
        if (dataOwner != null && dataOwner.isExisting()) {
            if (TemplateHandler.ensureLoaded(Long.valueOf(dataOwner.templateGroupIds()), dataOwner.__getInttype())) {
                Template[] oc = appendedConfirmation();
                if (oc != null) {
                    Export.print(new Template[]{TemplateHandler.loadTemplate(dataOwner.templateGroupIds(), dataOwner.__getInttype()),
                        oc[0]}, dataOwner);
                } else {
                    Export.print(TemplateHandler.loadTemplate(dataOwner.templateGroupIds(), dataOwner.__getInttype()), dataOwner);
                }
            } else {
                Popup.notice(Messages.NO_TEMPLATE_LOADED + " (" + User.getCurrentUser() + ")");
                Export.print(this);
            }
        } else {
            Export.print(this);
        }
    }

    @Override
    public void pdf() {
        if (mpv5.ui.toolbars.RecordActions.openLockedDocument(this)) {
            return;
        }
        if (!ensureSavedForExport()) {
            return;
        }
        if (dataOwner != null && dataOwner.isExisting()) {
            dataOwner.toPdf(true, appendedConfirmation());
        }
    }

    /**
     * @return the order confirmation template to append to print and PDF if
     * "print with order confirmation" is checked and the template exists,
     * else null
     */
    private Template[] appendedConfirmation() {
        if (!printWithConfirmation.isSelected()
                || dataOwner.__getInttype() == Constants.TYPE_ORDER_CONFIRMATION) {
            return null;
        }
        long group = dataOwner.templateGroupIds();
        Template t = TemplateHandler.loadTemplate(group, Constants.TYPE_ORDER_CONFIRMATION);
        if (t == null || !TemplateHandler.isLoaded(group, Constants.TYPE_ORDER_CONFIRMATION)) {
            mpv5.YabsViewProxy.instance().addMessage(Messages.NO_TEMPLATE_LOADED + ": "
                    + TemplateHandler.getName(Constants.TYPE_ORDER_CONFIRMATION), java.awt.Color.YELLOW);
            return null;
        }
        return new Template[]{t};
    }

    @Override
    public void odt() {
        if (mpv5.ui.toolbars.RecordActions.openLockedDocument(this)) {
            return;
        }
        if (!ensureSavedForExport()) {
            return;
        }
        if (dataOwner != null && dataOwner.isExisting()) {
            dataOwner.toOdt(true);
        }
    }
    // </editor-fold>

    /**
     * The status of the document as colored label next to the title
     */
    private static final class StatusPill extends JLabel {

        private static final long serialVersionUID = 1L;
        private Color color = Color.GRAY;

        StatusPill() {
            setFont(ModernTheme.font(-1, Font.BOLD));
            setForeground(Color.WHITE);
            setBorder(BorderFactory.createEmptyBorder(ModernTheme.px(3), ModernTheme.px(10), ModernTheme.px(3), ModernTheme.px(10)));
        }

        void setStatus(int status) {
            setVisible(status >= 0);
            if (status < 0) {
                return;
            }
            setText(Item.getStatusString(status));
            switch (status) {
                case Item.STATUS_IN_PROGRESS:
                    color = ModernTheme.accent();
                    break;
                case Item.STATUS_PAUSED:
                    color = new Color(214, 140, 20);
                    break;
                case Item.STATUS_FINISHED:
                    color = new Color(70, 140, 90);
                    break;
                case Item.STATUS_PAID:
                    color = new Color(40, 150, 80);
                    break;
                case Item.STATUS_CANCELLED:
                    color = new Color(200, 40, 40);
                    break;
                default:
                    color = new Color(120, 120, 130);
            }
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, getHeight(), getHeight());
            g2.dispose();
            super.paintComponent(g);
        }
    }
}
