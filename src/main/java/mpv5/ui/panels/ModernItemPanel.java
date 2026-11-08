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
import mpv5.db.objects.Item;
import mpv5.db.objects.MailMessage;
import mpv5.db.objects.Product;
import mpv5.db.objects.ProductList;
import mpv5.db.objects.ProductlistSubItem;
import mpv5.db.objects.SubItem;
import mpv5.db.objects.Template;
import mpv5.db.objects.User;
import mpv5.db.objects.ValueProperty;
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
    private final SearchPanel sp;
    private final JPanel leftpane = new JPanel(new BorderLayout());
    private boolean loading = true;
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
    //dates
    private final LabeledDateChooser date1 = ModernTheme.plain(new LabeledDateChooser());
    private final LabeledDateChooser date2 = ModernTheme.plain(new LabeledDateChooser());
    private final LabeledDateChooser date3 = ModernTheme.plain(new LabeledDateChooser());
    //free text
    private final LabeledCombobox messages = ModernTheme.plain(new LabeledCombobox());
    private final JTextArea notes = new JTextArea(4, 20);
    //positions, files, properties
    private final ItemPositionsTable positions = new ItemPositionsTable();
    private final JTable filetable = tooltipTable(this);
    private final JTable proptable = tooltipTable(this);
    //actions
    private final JButton previewButton = new JButton(Messages.ACTION_PREVIEW.toString());
    private final JMenuItem deliveryNote = new JMenuItem(text("ItemPanel2.button_deliverynote.text"));
    private final JMenuItem orderConfirmation = new JMenuItem(text("ItemPanel2.button_orderconf.text"));
    private final JCheckBoxMenuItem printWithConfirmation = new JCheckBoxMenuItem(text("ItemPanel2.checkb_pront_oc.text"));
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
    public int intreminders_;
    public int intstatus_;
    public Integer reforderids_;
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
        sp = new SearchPanel(context, this);
        dataOwner = (Item) DatabaseObject.getObject(context);
        if (type >= 0) {
            dataOwner.setInttype(type);
        }
        initFields(context);

        JPanel content = new JPanel(new BorderLayout());
        content.setOpaque(false);
        content.add(createHeader(), BorderLayout.NORTH);
        //the positions are the main part of a document
        JSplitPane split = split(scroll(createOverview()), createPositions(), 0.45);
        split.setResizeWeight(0.4);
        content.add(split, BorderLayout.CENTER);
        add(content, BorderLayout.CENTER);
        add(leftpane, BorderLayout.WEST);
        tb = new ModernCommandBar(this);
        add(tb, BorderLayout.NORTH);
        createActions();
        tb.setFindAction(() -> positions.getTable().requestFocusInWindow());

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
        contactname.getComboBox().addActionListener(e -> {
            final MPComboBoxModelItem item = contactname.getSelectedItem();
            if (item != null && item.isValid()) {
                SwingUtilities.invokeLater(() -> {
                    try {
                        Contact dbo = (Contact) DatabaseObject.getObject(Context.getContact(), Integer.valueOf(item.getId()));
                        setContactData(dbo);
                        setEnddate(dbo);
                        updateHeader();
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
        date2.setDate(new Date());

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
        notes.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, null);
        notes.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, null);
        positions.setTabTargets(notes, date3.getDateChooser());

        refOrderShow.addActionListener(e -> {
            try {
                Item owner = (Item) DatabaseObject.getObject(Context.getOrder(), reforderids_, true);
                YabsViewProxy.instance().addOrShowTab(owner);
            } catch (Exception ex) {
                Log.Debug(ex);
            }
        });
        ((MPTable) proptable).setPersistanceHandler(new TableViewPersistenceHandler((MPTable) proptable, this));
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
        displayname.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, ModernTheme.px(14)));
        //contact / date / user in the title line, readable (there is room for it)
        meta.setFont(ModernTheme.font(3, Font.PLAIN));
        meta.setBorder(BorderFactory.createEmptyBorder(0, ModernTheme.px(20), 0, 0));
        meta.setForeground(ModernTheme.muted());
        JPanel titleRow = new JPanel(new WrapLayout(FlowLayout.LEFT, 0, ModernTheme.px(2)));
        titleRow.setOpaque(false);
        titleRow.add(displayname);
        titleRow.add(statusPill);
        titleRow.add(meta);
        titleRow.setAlignmentX(LEFT_ALIGNMENT);
        center.add(titleRow);
        header.add(center, BorderLayout.CENTER);
        return header;
    }

    private JComponent createOverview() {
        ResponsiveGrid grid = new ResponsiveGrid(320, 3);

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
                .row(Card.field(text("ItemPanel2.refOrder._Label"), refRow)));

        grid.add(new Card(Messages.ITEM_SECTION_DATES.toString())
                .row(Card.field(text("ItemPanel2.date1._Label"), date1))
                .row(Card.field(text("ItemPanel2.date2._Label"), date2))
                .row(Card.field(text("ItemPanel2.date3._Label"), date3)));

        grid.add(new Card(Messages.ITEM_SECTION_TEXT.toString())
                .row(Card.field(text("ItemPanel2.labeledCombobox1._Label"), messages))
                .addFull(new JScrollPane(notes), 1));

        JButton addFile = link(null, "/images/16/add.png");
        addFile.setToolTipText(text("ItemPanel2.addfile.toolTipText"));
        addFile.addActionListener(e -> addFile());
        JButton removeFile = link(null, "/images/16/remove.png");
        removeFile.setToolTipText(text("ItemPanel2.removefile.toolTipText"));
        removeFile.addActionListener(e -> deleteFile());
        JPanel fileButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, ModernTheme.px(4), 0));
        fileButtons.setOpaque(false);
        fileButtons.add(addFile);
        fileButtons.add(removeFile);
        JScrollPane files = new JScrollPane(filetable);
        files.setPreferredSize(new java.awt.Dimension(ModernTheme.px(200), ModernTheme.px(100)));
        grid.add(new Card(Messages.ITEM_SECTION_FILES.toString()).headerAction(fileButtons).addFull(files, 1));

        JScrollPane props = new JScrollPane(proptable);
        props.setPreferredSize(new java.awt.Dimension(ModernTheme.px(200), ModernTheme.px(100)));
        grid.add(new Card(Messages.ITEM_SECTION_PROPERTIES.toString()).addFull(props, 1));
        return grid;
    }

    private JComponent createPositions() {
        Card card = new Card(Messages.ITEM_SECTION_POSITIONS.toString());
        card.addFull(positions, 1);
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(ModernTheme.background());
        p.setBorder(BorderFactory.createEmptyBorder(ModernTheme.px(8), 0, 0, 0));
        p.add(card, BorderLayout.CENTER);
        return p;
    }

    private static JButton menuButton(final JButton b, final JPopupMenu menu) {
        b.addActionListener(e -> menu.show(b, 0, b.getHeight()));
        return b;
    }

    /**
     * The actions of a document in the command bar
     */
    private void createActions() {
        previewButton.addActionListener(e -> preview(dataOwner.__getInttype()));
        tb.addAction(previewButton, null);

        JPopupMenu docs = new JPopupMenu();
        deliveryNote.addActionListener(e -> preview(Constants.TYPE_DELIVERY_NOTE));
        orderConfirmation.addActionListener(e -> preview(Constants.TYPE_ORDER_CONFIRMATION));
        docs.add(deliveryNote);
        docs.add(orderConfirmation);
        docs.addSeparator();
        docs.add(printWithConfirmation);
        tb.addAction(menuButton(documentsButton, docs), null);

        JPopupMenu einvoice = new JPopupMenu();
        JMenuItem xml = new JMenuItem(Messages.CREATE_XRECHNUNG.toString());
        xml.addActionListener(e -> createEInvoice(ZugpferdFile.Format.XRECHNUNG));
        JMenuItem pdf = new JMenuItem(Messages.CREATE_ZUGFERD.toString());
        pdf.addActionListener(e -> createEInvoice(ZugpferdFile.Format.ZUGFERD));
        JMenuItem check = new JMenuItem(Messages.ZUGFERD_VALIDATE_FILE.toString());
        check.addActionListener(e -> validateEInvoiceFile());
        einvoice.add(xml);
        einvoice.add(pdf);
        einvoice.addSeparator();
        einvoice.add(check);
        tb.addAction(menuButton(einvoiceButton, einvoice), null);

        JPopupMenu create = new JPopupMenu();
        toOrder.addActionListener(e -> convertToOrder());
        toInvoice.addActionListener(e -> convertToInvoice(Item.TYPE_INVOICE));
        toDeposit.addActionListener(e -> convertToInvoice(Item.TYPE_DEPOSIT));
        toPartPayment.addActionListener(e -> convertToInvoice(Item.TYPE_PART_PAYMENT));
        toCredit.addActionListener(e -> convertToInvoice(Item.TYPE_CREDIT));
        create.add(toOrder);
        create.add(toInvoice);
        create.add(toDeposit);
        create.add(toPartPayment);
        create.addSeparator();
        create.add(toCredit);
        tb.addAction(menuButton(createButton, create), null);

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
        tb.addAction(remindButton, null);
        tb.addAction(scheduleButton, null);
        tb.addAction(compareButton, null);
        tb.addAction(referencesButton, null);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="State of the controls">
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
        toOrder.setEnabled(t != Item.TYPE_ORDER && t != Item.TYPE_INVOICE);
        toInvoice.setEnabled(t != Item.TYPE_INVOICE);
        boolean partialAllowed = t != Item.TYPE_INVOICE && t != Item.TYPE_DEPOSIT && t != Item.TYPE_PART_PAYMENT;
        toDeposit.setEnabled(partialAllowed);
        toPartPayment.setEnabled(partialAllowed);
        toCredit.setEnabled(t == Item.TYPE_INVOICE);
        einvoiceButton.setVisible(ZugpferdFile.supports(dataOwner));
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

    private void setContactData(Contact dbo) {
        prename.setText(dbo.getDefaultaddress().__getPrename());
        cname.setText(dbo.getDefaultaddress().__getCname());
        zip.setText(dbo.getDefaultaddress().__getZip());
        city.setText(dbo.getDefaultaddress().__getCity());
        street.setText(dbo.getDefaultaddress().__getStreet());
        if (ItemService.isBill(inttype_)) {
            if (dataOwner.__getRefOrderIDS() != null && dataOwner.__getRefOrderIDS() > 0) {
                try {
                    refOrder.setModel((Item) DatabaseObject.getObject(Context.getOrder(), reforderids_, true));
                } catch (NodataFoundException ex) {
                    Log.Debug(ex);
                }
            } else {
                refOrder.setModel(new MPComboboxModel(ItemService.referenceOrderChoices(dbo)));
                refOrder.setEditable(true);
                refOrder.setSelectedIndex(-1);
            }
        }
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
            sp.refresh();
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
        setDataOwner(i2, true);
        Popup.notice(i2 + Messages.INSERTED.getValue());
    }

    private void convertToInvoice(int itemType) {
        dataOwner.getPanelData(this);
        positions.stopEditing();
        Item i2 = ItemService.toInvoice(dataOwner, itemType, positions.getModel(), q -> Popup.Y_N_dialog(q));
        if (i2 != null) {
            setDataOwner(i2, true);
            Popup.notice(i2 + Messages.INSERTED.getValue());
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
        if (dataOwner == null || !dataOwner.isExisting()) {
            Popup.notice(Messages.NOT_POSSIBLE + "\n" + Messages.NOT_SAVED_YET);
            return;
        }
        if (!ZugpferdFile.supports(dataOwner)) {
            Popup.notice(Messages.ZUGFERD_NOT_AN_INVOICE);
            return;
        }
        if (format == ZugpferdFile.Format.ZUGFERD
                && !TemplateHandler.isLoaded(Long.valueOf(dataOwner.templateGroupIds()), dataOwner.__getInttype())) {
            Popup.notice(Messages.NO_TEMPLATE_LOADED + " (" + User.getCurrentUser() + ")");
            return;
        }
        List<String> problems;
        final org.mustangproject.Invoice invoice;
        try {
            invoice = ZugpferdFile.createInvoice(dataOwner, format);
            problems = ZugpferdFile.validate(invoice);
            Contact contact = dataOwner.getContact();
            String buyerVatId = ZugpferdFile.checkBuyerVatId(contact, contact.getInvoiceaddress());
            if (buyerVatId != null) {
                problems.add(buyerVatId);
            }
        } catch (Exception ex) {
            Log.Debug(this, ex);
            Popup.error(ex);
            return;
        }
        if (!problems.isEmpty()) {
            StringBuilder text = new StringBuilder(Messages.ZUGFERD_MISSING_DATA.toString()).append("\n");
            for (String problem : problems) {
                text.append("\n - ").append(problem);
            }
            text.append("\n\n").append(Messages.ZUGFERD_CONTINUE_ANYWAY);
            if (!Popup.Y_N_dialog(text.toString())) {
                return;
            }
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
            new Job(new ZugpferdFile(dataOwner, invoice, target, format), new Waiter() {
                @Override
                public void set(Object object, Exception exception) throws Exception {
                    if (exception != null) {
                        Popup.error(exception);
                    } else {
                        showEInvoiceValidation(target, ((ZugpferdFile) object).getValidation());
                    }
                }
            }).execute();
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
            new Job(new EInvoiceValidator(file), new Waiter() {
                @Override
                public void set(Object object, Exception exception) throws Exception {
                    if (exception != null) {
                        Popup.error(exception);
                    } else {
                        showEInvoiceValidation(file, ((EInvoiceValidator) object).getResult());
                    }
                }
            }).execute();
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

    private void properties() {
        final MPTableModel m = new MPTableModel(ValueProperty.getProperties(dataOwner));
        final MPTableModel mold = m.clone();
        if (m.getDataVector().isEmpty()) {
            proptable.setModel(new MPTableModel(Arrays.asList(new ValueProperty[]{new ValueProperty("", "", dataOwner)})));
        } else {
            proptable.setModel(m);
        }
        m.addTableModelListener(new javax.swing.event.TableModelListener() {
            @Override
            public void tableChanged(javax.swing.event.TableModelEvent e) {
                if (!dataOwner.isExisting()) {
                    return;
                }
                if (e.getColumn() == 0 && e.getType() == javax.swing.event.TableModelEvent.DELETE) {
                    ValueProperty.deleteProperty(dataOwner, String.valueOf(mold.getData()[e.getLastRow()][0]));
                    m.removeTableModelListener(this);
                    properties();
                } else if (e.getColumn() == 1 && m.getValueAt(e.getLastRow(), 0) != null && String.valueOf(m.getValueAt(e.getLastRow(), 0)).length() > 0) {
                    ValueProperty.addOrUpdateProperty(String.valueOf(m.getData()[e.getLastRow()][0]).replaceAll("[^\\w]", ""), String.valueOf(m.getData()[e.getLastRow()][1]), dataOwner);
                    m.removeTableModelListener(this);
                    properties();
                }
            }
        });
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="DataPanel">
    @Override
    public DatabaseObject getDataOwner() {
        return dataOwner;
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
                    positions.setPositions(dataOwner.getSubitems());
                    if (object.isReadOnly()) {
                        Popup.notice(Messages.LOCKED_BY);
                    }
                    previewButton.setEnabled(false);
                    preloadTemplates();
                    updateHeader();
                    validate();
                }
            } else if (object instanceof SubItem) {
                try {
                    setDataOwner(DatabaseObject.getObject(Context.getInvoice(), ((SubItem) object).__getItemsids()), populate);
                } catch (NodataFoundException ex) {
                    Log.Debug(ex);
                }
            }
            properties();
        } finally {
            loading = false;
        }
    }

    @Override
    public boolean collectData() {
        try {
            contactsids_ = Integer.parseInt(String.valueOf(contactname.getSelectedItem().getId()));
        } catch (Exception e) {
            //no contact chosen: the typed name, a new customer if there is none
            try {
                Contact owner = ItemService.contactByNameOrNew(cname.getText(true, Messages.ENTER_VALUE),
                        prename.getText(), zip.getText(), city.getText());
                contactname.setModel(owner);
                setContactData(owner);
                contactsids_ = owner.__getIDS();
            } catch (Exception ex) {
                Notificator.raiseNotification(ex, false);
                return false;
            }
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
        datetodo_ = date2.getDate();
        dateend_ = date3.getDate();
        intstatus_ = Integer.parseInt(String.valueOf(status.getSelectedItem().getId()));
        try {
            reforderids_ = Integer.valueOf(String.valueOf(refOrder.getSelectedItem().getId()));
        } catch (Exception e) {
            reforderids_ = -1;
        }
        return true;
    }

    @Override
    public void exposeData() {
        number.setText(cnumber_);
        name.setText(cname_);
        //a document which is not saved yet has no date (it would show 1970)
        if (dateadded_ == null || dateadded_.getTime() <= 0) {
            dateadded_ = new Date();
        }
        date1.setDate(dateadded_);
        date2.setDate(User.getCurrentUser().getProperties().getProperty("org.openyabs.itemproperty", "keepmodifiedtransdate") ? datetodo_ : new Date());
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
            } else if (dbo.getContext().equals(Context.getProduct())) {
                positions.addPosition(new SubItem((Product) dbo));
            } else if (dbo.getContext().equals(Context.getProductList())) {
                try {
                    SubItem[] subs = dataOwner != null ? dataOwner.getSubitems() : new SubItem[0];
                    List<ProductlistSubItem> l = ProductList.getReferencedObjects(dbo, Context.getProductListItems(), new ProductlistSubItem());
                    MPTableModel t = SubItem.toModel(subs);
                    int count = t.getRowCount();
                    for (int i = 0; i < l.size(); i++) {
                        l.get(i).setIDS(-1);
                        t.addRow(l.get(i).getRowData(i + count + 1));
                    }
                    positions.setModel(t);
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
        if (User.getCurrentUser().getProperties().getProperty("org.openyabs.uiproperty", "pasten")) {
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
            o.setDatetodo(new Date());
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
        leftpane.removeAll();
        if (show) {
            leftpane.add(sp, BorderLayout.CENTER);
            sp.search();
        }
        validate();
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
        sp.refresh();
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
                positions.getModel().setRowAt(new SubItem((Product) o).getRowData(i), i, 4);
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
        date2.setDate(new Date());
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
        if (dataOwner != null && dataOwner.isExisting()) {
            if (TemplateHandler.isLoaded(Long.valueOf(dataOwner.templateGroupIds()), dataOwner.__getInttype())) {
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
        if (dataOwner != null && dataOwner.isExisting()) {
            if (TemplateHandler.isLoaded(Long.valueOf(dataOwner.templateGroupIds()), dataOwner.__getInttype())) {
                if (printWithConfirmation.isSelected() && printWithConfirmation.isEnabled()) {
                    Export.print(new Template[]{TemplateHandler.loadTemplate(dataOwner.templateGroupIds(), dataOwner.__getInttype()),
                        TemplateHandler.loadTemplate(dataOwner.templateGroupIds(), Constants.TYPE_ORDER_CONFIRMATION)}, dataOwner);
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
