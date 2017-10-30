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
 /*
 * ContactPanel.java
 *
 * Created on Nov 20, 2008, 8:17:28 AM
 */
package mpv5.ui.panels;

import enoa.handler.TemplateHandler;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableCellRenderer;
import mpv5.YabsViewProxy;
import mpv5.db.common.*;
import mpv5.db.objects.ActivityList;
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
import mpv5.handler.FormatHandler;
import mpv5.logging.Log;
import mpv5.ui.beans.MPCBSelectionChangeReceiver;
import mpv5.ui.beans.UserCheckbox;
import mpv5.ui.dialogs.BigPopup;
import mpv5.ui.dialogs.DialogForFile;
import mpv5.ui.dialogs.Notificator;
import mpv5.ui.dialogs.Popup;
import mpv5.ui.dialogs.ScheduleDayEvent;
import mpv5.ui.dialogs.subcomponents.*;
import mpv5.ui.misc.MPTable;
import mpv5.ui.misc.Position;
import mpv5.ui.misc.TableViewPersistenceHandler;
import mpv5.ui.popups.FileTablePopUp;
import mpv5.ui.popups.TablePopUp;
import mpv5.ui.toolbars.DataPanelTB;
import mpv5.usermanagement.MPSecurityManager;
import mpv5.utils.arrays.ArrayUtilities;
import mpv5.utils.date.DateConverter;
import mpv5.utils.export.Export;
import mpv5.utils.files.FileDirectoryHandler;
import mpv5.utils.jobs.Job;
import mpv5.utils.models.MPComboBoxModelItem;
import mpv5.utils.models.MPComboboxModel;
import mpv5.utils.models.MPTableModel;
import mpv5.utils.numberformat.FormatNumber;
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
import mpv5.utils.text.TypeConversion;
import mpv5.utils.ui.TextFieldUtils;

/**
 *
 *
 */
public class ItemPanel2 extends javax.swing.JPanel implements DataPanel, MPCBSelectionChangeReceiver, ExportablePanel {

    private static final long serialVersionUID = 1L;
    private Item dataOwner;
    private DataPanelTB tb;
    private SearchPanel sp;
    private DynamicTableCalculator itemMultiplier;
    private DynamicTableCalculator taxcalculator;
    private DynamicTableCalculator netCalculator;
    private DynamicTableCalculator discnetcalculator;
    private DynamicTableCalculator discbrutcalculator;
    private JLabel hidden = new JLabel();
    private boolean loading = true;
    private java.util.ResourceBundle bundle = mpv5.i18n.LanguageManager.getBundle();
    private List<Runnable> afterSaveRunnables = new ArrayList<>();

    /**
     * Creates new form ContactPanel
     *
     * @param context
     * @param type
     */
    public ItemPanel2(Context context, int type) {
        initComponents();
        jButton1.setEnabled(MPSecurityManager.checkAdminAccess());

        inttype_ = type;
        sp = new SearchPanel(context, this);
        sp.setVisible(true);
        tb = new mpv5.ui.toolbars.DataPanelTB(this);
        toolbarpane.add(tb, BorderLayout.CENTER);
        dataOwner = (Item) DatabaseObject.getObject(context);
        if (type >= 0) {
            dataOwner.setInttype(type);
            this.type.setText(Item.getTypeString(type));
        } else {
            this.type.setText("");
        }

        refreshSync();

        addedby.setText(mpv5.db.objects.User.getCurrentUser().getName());
        contactname.setSearchEnabled(true);
        contactname.setContext(Context.getCustomer());
        contactname.getComboBox().addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                final MPComboBoxModelItem item = contactname.getSelectedItem();
                if (item != null && item.isValid()) {
                    Runnable runnable = new Runnable() {

                        @Override
                        public void run() {
                            try {
                                Contact dbo = (Contact) DatabaseObject.getObject(Context.getContact(), Integer.valueOf(item.getId()));
                                setContactData(dbo);
                                setEnddate(dbo);
                            } catch (NodataFoundException ex) {
                            }
                        }
                    };
                    SwingUtilities.invokeLater(runnable);
                }
            }
        });

        accountselect.setContext(Context.getAccounts());
        accountselect.setSearchEnabled(true);
        groupnameselect.setContext(Context.getGroup());
        groupnameselect.setSearchEnabled(true);

        date1.setDate(new Date());
        date2.setDate(new Date());

//        itemtable.getTableHeader().addMouseListener(new MouseListener() {
//
//            @Override
//            public void mouseClicked(MouseEvent e) {
//                if (e.getButton() == MouseEvent.BUTTON2) {
//                    MPTableModel m = (MPTableModel) itemtable.getModel();
//                    if (m.getRowCount() > 0) {
//                        m.addRow(5);
//                    } else {
//                        itemtable.setModel(SubItem.toModel(new SubItem[]{
//                                    SubItem.getDefaultItem(), SubItem.getDefaultItem(),
//                                    SubItem.getDefaultItem(), SubItem.getDefaultItem(),
//                                    SubItem.getDefaultItem(), SubItem.getDefaultItem()
//                                }));
//                        formatTable();
//                    }
//                } else if (e.getButton() == MouseEvent.BUTTON3) {
//                    MPTableModel m = (MPTableModel) itemtable.getModel();
//                    Product p = (Product) Popup.SelectValue(Context.getProduct());
//                    if (p != null) {
//                        int row = m.getLastValidRow(new int[]{4});
//                        m.setRowAt(new SubItem(p).getRowData(row), row + 1, 1);
//                    }
//                }
//            }
//
//            @Override
//            public void mousePressed(MouseEvent e) {
//            }
//
//            @Override
//            public void mouseReleased(MouseEvent e) {
//            }
//
//            @Override
//            public void mouseEntered(MouseEvent e) {
//            }
//
//            @Override
//            public void mouseExited(MouseEvent e) {
//            }
//        });
        InputMap inputMap = itemtable.getInputMap(JTable.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        KeyStroke tab = KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0);
        Action oldAction = itemtable.getActionMap().get(inputMap.get(tab));
        itemtable.getActionMap().put(inputMap.get(tab), new TableTabAction(notes, oldAction, false));
        KeyStroke shifttab = KeyStroke.getKeyStroke(KeyEvent.VK_TAB, InputEvent.SHIFT_MASK);
        oldAction = itemtable.getActionMap().get(inputMap.get(shifttab));
        itemtable.getActionMap().put(inputMap.get(shifttab), new TableTabAction(date3, oldAction, true));

//        KeyStroke right = KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0);
//        oldAction = itemtable.getActionMap().get(inputMap.get(right));
//        itemtable.getActionMap().put(inputMap.get(right), new TableTabAction(notes, oldAction, false));
//        KeyStroke left = KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0);
//        oldAction = itemtable.getActionMap().get(inputMap.get(left));
//        itemtable.getActionMap().put(inputMap.get(left), new TableTabAction(date3, oldAction, true));
        number.setSearchOnEnterEnabled(true);
        number.setParent(this);
        number.setSearchField("cnumber");
        switch (inttype_) {
            case Item.TYPE_OFFER:
                number.setContext(Context.getOffer());
                break;
            case Item.TYPE_ORDER:
                number.setContext(Context.getOrder());
                break;
            case Item.TYPE_INVOICE:
                number.setContext(Context.getInvoice());
                break;
            case Item.TYPE_PART_PAYMENT:
                number.setContext(Context.getPartPayment());
                break;
            case Item.TYPE_DEPOSIT:
                number.setContext(Context.getDeposit());
                break;
            default:
                number.setContext(Context.getInvoice());
                break;
        }

        final DataPanel p = this;
        status.getComboBox().addActionListener(new ActionListener() {

            Item dato = (Item) getDataOwner();

            @Override
            public void actionPerformed(ActionEvent e) {
                if ((dato.__getInttype() == Item.TYPE_INVOICE
                        || dato.__getInttype() == Item.TYPE_DEPOSIT
                        || dato.__getInttype() == Item.TYPE_PART_PAYMENT)
                        && !loading
                        && dataOwner.isExisting()
                        && Integer.valueOf(status.getSelectedItem().getId()) == Item.STATUS_PAID
                        && mpv5.db.objects.User.getCurrentUser().getProperties().getProperty("org.openyabs.uiproperty", "autocreaterevenue")) {
                    if (Popup.Y_N_dialog(Messages.BOOK_NOW)) {

                        if (dato.getPanelData(p) && dato.save()) {
                            try {
                                actionAfterSave();
                                dato.createRevenue();
                            } catch (Exception ef) {
                                Log.Debug(this, ef);
                            }
                        } else {
                            showRequiredFields();
                        }
                    }
                }

                if (!loading && dataOwner.isExisting()
                        && Integer.valueOf(status.getSelectedItem().getId()) == Item.STATUS_PAID) {
                    //set dateend
                    if (!User.getCurrentUser().getProperties().getProperty("org.openyabs.itemproperty", "keepmodifiedenddate")) {
                        date3.setDate(new Date());
                    }
                }

//                if (!loading && dataOwner.isExisting()
//                        && Integer.valueOf(status.getSelectedItem().getId()) == Item.STATUS_IN_PROGRESS) {
//                    //set date edited
//                    date2.setDate(new Date());
//                }
            }
        });

        labeledCombobox1.setContext(Context.getMessage());
        labeledCombobox1.getComboBox().addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    MailMessage d = (MailMessage) DatabaseObject.getObject(Context.getMessage(), Integer.valueOf(labeledCombobox1.getSelectedItem().getId()));
                    notes.setText(d.__getDescription());
                } catch (Exception ex) {
                    Log.Debug(ex);
                }
            }
        });
        labeledCombobox1.setSearchOnEnterEnabled(true);

        ((MPTable) dataTable).setPersistanceHandler(new TableViewPersistenceHandler((MPTable) dataTable, this));
        ((MPTable) proptable).setPersistanceHandler(new TableViewPersistenceHandler((MPTable) proptable, this));
        jSplitPane1.setDividerLocation((User.getCurrentUser().getProperties().getProperty("org.openyabs.uiproperty.itempanel.divider1.dividerLocation", 150)));
//        jSplitPane2.setDividerLocation((User.getCurrentUser().getProperties().getProperty("org.openyabs.uiproperty.itempanel.divider2.dividerLocation", -1)));
        toinvoice.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                toInvoice(Item.TYPE_INVOICE);
            }
        });
        toinvoice.add(Messages.CREATE_DEPOSIT.toString(), new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                toInvoice(Item.TYPE_DEPOSIT);
            }
        });
        toinvoice.add(Messages.CREATE_PART_PAYMENT.toString(), new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                toInvoice(Item.TYPE_PART_PAYMENT);
            }
        });
    }

    private void setContactData(Contact dbo) {
        prename.setText(dbo.__getPrename());
        cname.setText(dbo.__getCname());
        zip.setText(dbo.__getZip());
        city.setText(dbo.__getCity());

        //contactcity.setText(dbo.__getStreet() + ", " + dbo.__getCity() + ", " + dbo.__getCountry() + " (" + dbo.__getCNumber() + ")");
        if (inttype_ == Item.TYPE_INVOICE
                || inttype_ == Item.TYPE_PART_PAYMENT
                || inttype_ == Item.TYPE_DEPOSIT) {
            Context i = Context.getOrder();
            String s = Context.DEFAULT_ITEM_SEARCH + ", inttype";
            Object[][] data = new DatabaseSearch(i).getValuesFor(s, "contactsids", dbo.__getIDS());
            Log.Debug(this, "gefundene Items: " + data.length);
            MPComboBoxModelItem[] items = new MPComboBoxModelItem[data.length];
            Locale l = Locale.getDefault();
            if (mpv5.db.objects.User.getCurrentUser().getProperties().hasProperty("item.date.locale")) {
                try {
                    l = TypeConversion.stringToLocale(mpv5.db.objects.User.getCurrentUser().getProperties().getProperty("item.date.locale"));
                } catch (Exception e) {
                }
                if (l == null) {
                    Log.Debug(this, "Error while using item.date.locale");
                }
            }
            DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM, l);
            for (int a = 0; a < data.length; a++) {
                items[a] = new MPComboBoxModelItem(data[a][0], (String) data[a][1] + "(" + df.format((Date) data[a][2]) + ")");
            }
            if (dataOwner.__getRefOrderIDS() != null && dataOwner.__getRefOrderIDS() > 0) {
                try {
                    Item owner = (Item) DatabaseObject.getObject(Context.getOrder(), reforderids_, true);
                    refOrder.setModel(owner);
                } catch (NodataFoundException ex) {
                    Log.Debug(ex);
                }
            } else {
                refOrder.setModel(new MPComboboxModel(items));
                refOrder.setEditable(true);
                refOrder.setSelectedIndex(-1);
            }
        } else {
            refOrder.setVisible(false);
        }
    }

    private void setEnddate(Contact dbo) {
        if (User.getCurrentUser().getProperties().getProperty("org.openyabs.itemproperty", "calcenddate")
                && (!User.getCurrentUser().getProperties().getProperty("org.openyabs.itemproperty", "keepmodifiedenddate")
                || !dataOwner.isExisting())) {
            if (dbo != null) {
                int payterm = dbo.__getPayterm() == 0 ? 14 : dbo.__getPayterm();
                date3.setDate(DateConverter.addDays(new Date(), payterm));
            } else {
                try {
                    date3.setDate(DateConverter.addDays(new Date(), Integer.valueOf(User.getCurrentUser().getProperties().getProperty("bills.warn.days"))));
                } catch (Exception e) {
                    date3.setDate(DateConverter.addDays(new Date(), 14));
                }
            }
        }
    }

    /**
     *
     * @param items
     */
    public ItemPanel2(Context items) {
        this(items, -1);
    }

    @Override
    public DatabaseObject getDataOwner() {
        return dataOwner;
    }

    @Override
    public void setDataOwner(DatabaseObject object, boolean populate) {
        loading = true;

        if (object instanceof Item) {
            dataOwner = (Item) object;
            if (populate) {
                dataOwner.setPanelData(this);
                inttype_ = dataOwner.__getInttype();
                button_reminders.setEnabled(inttype_ == Item.TYPE_INVOICE);
                button_schedule.setEnabled(inttype_ == Item.TYPE_INVOICE);
                toorder.setEnabled(inttype_ != Item.TYPE_ORDER && inttype_ != Item.TYPE_INVOICE);
                toinvoice.setEnabled(inttype_ != Item.TYPE_INVOICE);
                tocredit.setEnabled(inttype_ == Item.TYPE_INVOICE);
                toinvoice.hidePopUP(inttype_ == Item.TYPE_DEPOSIT || inttype_ == Item.TYPE_PART_PAYMENT);
                type.setText(Item.getTypeString(inttype_));
                //            typelabel.setIcon(dataOwner.getIcon());
                this.exposeData();

                setTitle();

                tb.setFavourite(Favourite.isFavourite(object));
                tb.setEditable(!object.isReadOnly());

                itemtable.setModel(SubItem.toModel(((Item) object).getSubitems()));
                if (((MPTableModel) itemtable.getModel()).getEmptyRows(new int[]{4}) < 2) {
                    ((MPTableModel) itemtable.getModel()).addRow(1);
                }

                omodel = (MPTableModel) itemtable.getModel();

                formatTable();
                try {
                    ((MPTableModel) itemtable.getModel()).fireTableCellUpdated(0, 0);
                } catch (Exception e) {
                }
                if (object.isReadOnly()) {
                    Popup.notice(Messages.LOCKED_BY);
                }
                button_preview.setEnabled(false);
                preloadTemplates();
                validate();
            }
        } else if (object instanceof SubItem) {
            Item i;
            try {
                i = (Item) DatabaseObject.getObject(Context.getInvoice(), ((SubItem) object).__getItemsids());
                setDataOwner(i, populate);
            } catch (NodataFoundException ex) {
                Log.Debug(ex);
            }
        }

        properties();
        loading = false;

    }

    private void setTitle() {
        try {
            if (this.getParent() instanceof JViewport || this.getParent() instanceof JTabbedPane) {
                JTabbedPane jTabbedPane = null;
                String title1 = cname_;
                //this->viewport->scrollpane->tabbedpane
                if (this.getParent().getParent().getParent() instanceof JTabbedPane) {
                    jTabbedPane = (JTabbedPane) this.getParent().getParent().getParent();
                } else {
                    try {
                        jTabbedPane = (JTabbedPane) this.getParent();
                    } catch (Exception e) {
                        //Free floating window
                        ((JFrame) this.getRootPane().getParent()).setTitle(title1);
                    }
                }
                if (jTabbedPane != null) {
                    jTabbedPane.setTitleAt(jTabbedPane.getSelectedIndex(), title1);
                }
            }
        } catch (Exception e) {
            // Log.Debug(e);
        }
    }

    @Override
    public void showRequiredFields() {
        TextFieldUtils.blink(contactname.getComboBox(), Color.RED);
        contactname.requestFocus();
//        Popup.notice(Messages.SELECT_A_CONTACT);
    }

    private void addFile() {
        if (dataOwner.isExisting()) {
            DialogForFile d = new DialogForFile(DialogForFile.FILES_ONLY);
            if (d.chooseFile()) {
                String s = Popup.Enter_Value(Messages.ENTER_A_DESCRIPTION);
                if (s != null) {
                    QueryHandler.instanceOf().clone(Context.getFiles(), this).insertFile(d.getFile(), dataOwner, QueryCriteria.getSaveStringFor(s));
                }
            }
        }
    }

    private void deleteFile() {
        if (dataOwner.isExisting()) {
            try {
                DatabaseObject.getObject(Context.getFilesToItems(), "filename", (dataTable.getModel().getValueAt(dataTable.getSelectedRow(), 0).toString())).delete();
            } catch (Exception e) {
                Log.Debug(this, e.getMessage());
            }
            fillFiles();

        }
    }

    private void fileTableClicked(MouseEvent evt) {
        if (evt.getClickCount() > 1) {
            FileDirectoryHandler.open(QueryHandler.instanceOf().clone(Context.getFiles()).
                    retrieveFile(dataTable.getModel().getValueAt(dataTable.getSelectedRow(), 0).
                            toString(), new File(FileDirectoryHandler.getTempDir() + dataTable.getModel().
                                    getValueAt(dataTable.getSelectedRow(), 1).toString())));
        } else if (evt.getClickCount() == 1 && evt.getButton() == MouseEvent.BUTTON3) {

            JTable source = (JTable) evt.getSource();
            int row = source.rowAtPoint(evt.getPoint());
            int column = source.columnAtPoint(evt.getPoint());

            if (!source.isRowSelected(row)) {
                source.changeSelection(row, column, false, false);
            }

            FileTablePopUp.instanceOf(dataTable).show(source, evt.getX(), evt.getY());
        }
    }

    private void fillFiles() {

        Runnable runnable = new Runnable() {

            @Override
            public void run() {
                Context c = Context.getFilesToItems();
                c.addReference(Context.getFiles().getDbIdentity(), "cname", "filename");
                Object[][] data = new DatabaseSearch(c).getValuesFor(Context.DETAILS_FILES_TO_ITEMS, "itemsids", dataOwner.__getIDS());

                dataTable.setModel(new MPTableModel(data, Headers.FILE_REFERENCES.getValue()));
                TableFormat.stripFirstColumn(dataTable);
            }
        };
        new Thread(runnable).start();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        leftpane = new javax.swing.JPanel();
        jTabbedPane2 = new javax.swing.JTabbedPane();
        rightpane = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        jPanel9 = new javax.swing.JPanel();
        type = new javax.swing.JLabel();
        addedby = new javax.swing.JLabel();
        staus_icon = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        contactname = new mpv5.ui.beans.LabeledCombobox();
        jButton3 = new javax.swing.JButton();
        prename = new mpv5.ui.beans.LabeledTextField();
        cname = new mpv5.ui.beans.LabeledTextField();
        street = new mpv5.ui.beans.LabeledTextField();
        zip = new mpv5.ui.beans.LabeledTextField();
        city = new mpv5.ui.beans.LabeledTextField();
        jPanel12 = new javax.swing.JPanel();
        number = new mpv5.ui.beans.LabeledTextField();
        button_order2 = new javax.swing.JButton();
        groupnameselect = new mpv5.ui.beans.LabeledCombobox();
        refOrder = new mpv5.ui.beans.LabeledCombobox();
        jButton1 = new javax.swing.JButton();
        name = new mpv5.ui.beans.LabeledTextField();
        status = new mpv5.ui.beans.LabeledCombobox();
        accountselect = new mpv5.ui.beans.LabeledCombobox();
        jButton4 = new javax.swing.JButton();
        date1 = new mpv5.ui.beans.LabeledDateChooser();
        jToolBar1 = new javax.swing.JToolBar();
        toorder = new javax.swing.JButton();
        toinvoice = new mpv5.ui.beans.DropDownButton();
        tocredit = new javax.swing.JButton();
        jSeparator4 = new javax.swing.JToolBar.Separator();
        typelabel = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JToolBar.Separator();
        button_reminders = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jSeparator7 = new javax.swing.JToolBar.Separator();
        button_schedule = new javax.swing.JButton();
        button_preview = new javax.swing.JButton();
        button_deliverynote = new javax.swing.JButton();
        button_orderconf = new javax.swing.JButton();
        jPanel11 = new javax.swing.JPanel();
        jPanel6 = new javax.swing.JPanel();
        jToolBar2 = new alignRightToolbar();
        jLabel1 = new javax.swing.JLabel();
        jSeparator3 = new javax.swing.JToolBar.Separator();
        netvalue = new javax.swing.JLabel();
        jSeparator9 = new javax.swing.JToolBar.Separator();
        jLabel7 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jSeparator8 = new javax.swing.JToolBar.Separator();
        discount = new javax.swing.JLabel();
        jSeparator13 = new javax.swing.JToolBar.Separator();
        jLabel8 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jSeparator6 = new javax.swing.JToolBar.Separator();
        taxvalue = new javax.swing.JLabel();
        jSeparator10 = new javax.swing.JToolBar.Separator();
        jLabel9 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jSeparator5 = new javax.swing.JToolBar.Separator();
        value = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        itemtable = new javax.swing.JTable();
        itemPanel = new javax.swing.JPanel();
        addItem = new javax.swing.JButton();
        delItem = new javax.swing.JButton();
        upItem = new javax.swing.JButton();
        upItem1 = new javax.swing.JButton();
        panel2 = new javax.swing.JPanel();
        jPanel7 = new javax.swing.JPanel();
        jSplitPane1 = new javax.swing.JSplitPane();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        class NoTabTextArea extends JTextPane {
            private static final long serialVersionUID = 1L;
            protected void processComponentKeyEvent( KeyEvent e ) {
                if ( e.getID() == KeyEvent.KEY_PRESSED &&
                    e.getKeyCode() == KeyEvent.VK_TAB ) {
                    e.consume();
                    if (e.isShiftDown()) {
                        transferFocusBackward();
                    } else {
                        itemtable.requestFocusInWindow();
                    }
                } else {
                    super.processComponentKeyEvent( e );
                }
            }
        }
        notes = new NoTabTextArea();
        labeledCombobox1 = new mpv5.ui.beans.LabeledCombobox();
        jScrollPane4 = new javax.swing.JScrollPane();
        proptable = new  mpv5.ui.misc.MPTable(this) {
            private static final long serialVersionUID = 1L;
            public Component prepareRenderer(TableCellRenderer renderer,
                int rowIndex, int vColIndex) {
                Component c = super.prepareRenderer(renderer, rowIndex, vColIndex);
                if (c instanceof JComponent) {
                    JComponent jc = (JComponent)c;
                    jc.setToolTipText(String.valueOf(getValueAt(rowIndex, vColIndex)));
                }
                return c;
            }
        };
        jPanel8 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        dataTable = new  mpv5.ui.misc.MPTable(this) {
            private static final long serialVersionUID = 1L;
            public Component prepareRenderer(TableCellRenderer renderer,
                int rowIndex, int vColIndex) {
                Component c = super.prepareRenderer(renderer, rowIndex, vColIndex);
                if (c instanceof JComponent) {
                    JComponent jc = (JComponent)c;
                    jc.setToolTipText(String.valueOf(getValueAt(rowIndex, vColIndex)));
                }
                return c;
            }
        }
        ;
        removefile = new javax.swing.JButton();
        addfile = new javax.swing.JButton();
        jPanel10 = new javax.swing.JPanel();
        checkb_pront_oc = new UserCheckbox("orderconfirmationalways");
        date3 = new mpv5.ui.beans.LabeledDateChooser();
        date2 = new mpv5.ui.beans.LabeledDateChooser();
        toolbarpane = new javax.swing.JPanel();

        java.util.ResourceBundle bundle = mpv5.i18n.LanguageManager.getBundle(); // NOI18N
        setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("ItemPanel2.border.title_1"))); // NOI18N
        setName("ItemPanel"); // NOI18N
        setPreferredSize(new java.awt.Dimension(500, 300));
        setLayout(new java.awt.BorderLayout());

        leftpane.setName("leftpane"); // NOI18N
        leftpane.setLayout(new java.awt.BorderLayout());
        add(leftpane, java.awt.BorderLayout.WEST);

        jTabbedPane2.setName("jTabbedPane2"); // NOI18N

        rightpane.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("ItemPanel2.rightpane.border.title"))); // NOI18N
        rightpane.setName("rightpane"); // NOI18N

        jPanel1.setName("jPanel1"); // NOI18N
        jPanel1.setPreferredSize(new java.awt.Dimension(843, 100));

        jPanel9.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        jPanel9.setName("jPanel9"); // NOI18N
        jPanel9.setPreferredSize(new java.awt.Dimension(500, 23));

        type.setFont(type.getFont().deriveFont(type.getFont().getStyle() | java.awt.Font.BOLD, type.getFont().getSize()+4));
        type.setText(bundle.getString("ItemPanel2.type.text")); // NOI18N
        type.setMaximumSize(new java.awt.Dimension(250, 23));
        type.setMinimumSize(new java.awt.Dimension(150, 23));
        type.setName("type"); // NOI18N
        type.setPreferredSize(new java.awt.Dimension(200, 23));

        addedby.setFont(addedby.getFont());
        addedby.setToolTipText(bundle.getString("ItemPanel2.addedby.toolTipText")); // NOI18N
        addedby.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        addedby.setMaximumSize(new java.awt.Dimension(250, 23));
        addedby.setMinimumSize(new java.awt.Dimension(150, 23));
        addedby.setName("addedby"); // NOI18N
        addedby.setPreferredSize(new java.awt.Dimension(200, 30));

        staus_icon.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        staus_icon.setMaximumSize(new java.awt.Dimension(50, 50));
        staus_icon.setMinimumSize(new java.awt.Dimension(20, 20));
        staus_icon.setName("staus_icon"); // NOI18N
        staus_icon.setPreferredSize(new java.awt.Dimension(23, 23));

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addComponent(staus_icon, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(type, javax.swing.GroupLayout.PREFERRED_SIZE, 1284, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(addedby, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel9Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(type, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(staus_icon, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(addedby, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel2.setName("jPanel2"); // NOI18N

        contactname.set_Label(bundle.getString("ItemPanel2.contactname._Label")); // NOI18N
        contactname.setMaximumSize(new java.awt.Dimension(350, 23));
        contactname.setMinimumSize(new java.awt.Dimension(150, 23));
        contactname.setName("contactname"); // NOI18N
        contactname.setPreferredSize(new java.awt.Dimension(300, 23));

        jButton3.setFont(jButton3.getFont().deriveFont(jButton3.getFont().getStyle() & ~java.awt.Font.BOLD));
        jButton3.setText(bundle.getString("ItemPanel2.jButton3.text")); // NOI18N
        jButton3.setMaximumSize(new java.awt.Dimension(250, 23));
        jButton3.setMinimumSize(new java.awt.Dimension(50, 23));
        jButton3.setName("jButton3"); // NOI18N
        jButton3.setPreferredSize(new java.awt.Dimension(100, 23));
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        prename.set_Label(bundle.getString("ItemPanel2.prename._Label")); // NOI18N
        prename.setMaximumSize(new java.awt.Dimension(120, 24));
        prename.setMinimumSize(new java.awt.Dimension(120, 21));
        prename.setName("prename"); // NOI18N
        prename.setPreferredSize(new java.awt.Dimension(120, 24));

        cname.set_Label(bundle.getString("ItemPanel2.cname._Label")); // NOI18N
        cname.setMaximumSize(new java.awt.Dimension(120, 24));
        cname.setMinimumSize(new java.awt.Dimension(120, 21));
        cname.setName("cname"); // NOI18N
        cname.setPreferredSize(new java.awt.Dimension(120, 24));

        street.set_Label(bundle.getString("ItemPanel2.street._Label")); // NOI18N
        street.setMaximumSize(new java.awt.Dimension(120, 24));
        street.setMinimumSize(new java.awt.Dimension(120, 21));
        street.setName("street"); // NOI18N
        street.setPreferredSize(new java.awt.Dimension(120, 24));

        zip.set_Label(bundle.getString("ItemPanel2.zip._Label")); // NOI18N
        zip.setMaximumSize(new java.awt.Dimension(120, 24));
        zip.setMinimumSize(new java.awt.Dimension(120, 21));
        zip.setName("zip"); // NOI18N
        zip.setPreferredSize(new java.awt.Dimension(120, 24));

        city.set_Label(bundle.getString("ItemPanel2.city._Label")); // NOI18N
        city.setMaximumSize(new java.awt.Dimension(120, 24));
        city.setMinimumSize(new java.awt.Dimension(120, 21));
        city.setName("city"); // NOI18N
        city.setPreferredSize(new java.awt.Dimension(120, 24));

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                        .addComponent(contactname, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(prename, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(cname, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(zip, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(city, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(street, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(contactname, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButton3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(25, 25, 25)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(prename, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cname, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(street, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(zip, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(city, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel12.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel12.setName("jPanel12"); // NOI18N
        jPanel12.setOpaque(false);

        number.set_Label(bundle.getString("ItemPanel2.number._Label")); // NOI18N
        number.setFocusable(false);
        number.setFont(number.getFont());
        number.setMaximumSize(new java.awt.Dimension(350, 23));
        number.setMinimumSize(new java.awt.Dimension(150, 23));
        number.setName("number"); // NOI18N
        number.setPreferredSize(new java.awt.Dimension(300, 23));

        button_order2.setFont(button_order2.getFont().deriveFont(button_order2.getFont().getStyle() | java.awt.Font.BOLD));
        button_order2.setLabel(bundle.getString("ItemPanel2.button_order2.label")); // NOI18N
        button_order2.setMaximumSize(new java.awt.Dimension(23, 23));
        button_order2.setMinimumSize(new java.awt.Dimension(23, 23));
        button_order2.setName("button_order2"); // NOI18N
        button_order2.setPreferredSize(new java.awt.Dimension(23, 23));
        button_order2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                button_order2ActionPerformed(evt);
            }
        });

        groupnameselect.set_Label(bundle.getString("ItemPanel2.groupnameselect._Label")); // NOI18N
        groupnameselect.setName("groupnameselect"); // NOI18N
        groupnameselect.setPreferredSize(new java.awt.Dimension(300, 23));

        refOrder.set_Label(bundle.getString("ItemPanel2.refOrder._Label")); // NOI18N
        refOrder.setMaximumSize(new java.awt.Dimension(350, 23));
        refOrder.setMinimumSize(new java.awt.Dimension(150, 23));
        refOrder.setName("refOrder"); // NOI18N
        refOrder.setPreferredSize(new java.awt.Dimension(300, 23));
        refOrder.setSearchOnEnterEnabled(false);

        jButton1.setText(bundle.getString("ItemPanel2.jButton1.text")); // NOI18N
        jButton1.setMaximumSize(new java.awt.Dimension(23, 23));
        jButton1.setMinimumSize(new java.awt.Dimension(23, 23));
        jButton1.setName(bundle.getString("ItemPanel.jButton1.name")); // NOI18N
        jButton1.setPreferredSize(new java.awt.Dimension(23, 23));
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        name.set_Label(bundle.getString("ItemPanel2.name._Label")); // NOI18N
        name.setFocusable(false);
        name.setFont(name.getFont());
        name.setLabelWidth(120);
        name.setMaximumSize(new java.awt.Dimension(0, 0));
        name.setMinimumSize(new java.awt.Dimension(0, 0));
        name.setName("name"); // NOI18N
        name.setPreferredSize(new java.awt.Dimension(0, 0));

        status.set_Label(bundle.getString("ItemPanel2.status._Label")); // NOI18N
        status.setMaximumSize(new java.awt.Dimension(350, 23));
        status.setMinimumSize(new java.awt.Dimension(150, 23));
        status.setName("status"); // NOI18N
        status.setPreferredSize(new java.awt.Dimension(300, 23));

        accountselect.set_Label(bundle.getString("ItemPanel2.accountselect._Label")); // NOI18N
        accountselect.setMaximumSize(new java.awt.Dimension(350, 23));
        accountselect.setMinimumSize(new java.awt.Dimension(150, 23));
        accountselect.setName("accountselect"); // NOI18N
        accountselect.setPreferredSize(new java.awt.Dimension(300, 23));
        accountselect.setSearchOnEnterEnabled(false);

        jButton4.setFont(jButton4.getFont().deriveFont(jButton4.getFont().getStyle() & ~java.awt.Font.BOLD));
        jButton4.setText(bundle.getString("ItemPanel2.jButton4.text")); // NOI18N
        jButton4.setMaximumSize(new java.awt.Dimension(250, 23));
        jButton4.setMinimumSize(new java.awt.Dimension(50, 23));
        jButton4.setName("jButton4"); // NOI18N
        jButton4.setPreferredSize(new java.awt.Dimension(100, 23));
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        date1.set_Label(bundle.getString("ItemPanel2.date1._Label")); // NOI18N
        date1.setName("date1"); // NOI18N
        date1.setPreferredSize(new java.awt.Dimension(280, 20));

        javax.swing.GroupLayout jPanel12Layout = new javax.swing.GroupLayout(jPanel12);
        jPanel12.setLayout(jPanel12Layout);
        jPanel12Layout.setHorizontalGroup(
            jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel12Layout.createSequentialGroup()
                .addGap(6, 6, 6)
                .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel12Layout.createSequentialGroup()
                        .addComponent(groupnameselect, javax.swing.GroupLayout.PREFERRED_SIZE, 301, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(button_order2, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel12Layout.createSequentialGroup()
                        .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel12Layout.createSequentialGroup()
                                .addComponent(number, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addComponent(status, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(accountselect, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                        .addGap(32, 32, 32)
                        .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(jPanel12Layout.createSequentialGroup()
                                .addComponent(refOrder, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButton4, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(date1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(name, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        jPanel12Layout.setVerticalGroup(
            jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(name, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(jPanel12Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(refOrder, javax.swing.GroupLayout.DEFAULT_SIZE, 35, Short.MAX_VALUE)
                    .addComponent(number, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButton1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButton4, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(28, 28, 28)
                .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(groupnameselect, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(button_order2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(accountselect, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(status, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(date1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel9, javax.swing.GroupLayout.DEFAULT_SIZE, 1540, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jPanel12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(5, 5, 5)
                .addComponent(jPanel9, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel12, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        jToolBar1.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jToolBar1.setFloatable(false);
        jToolBar1.setRollover(true);
        jToolBar1.setName("jToolBar1"); // NOI18N

        toorder.setText(bundle.getString("ItemPanel2.toorder.text_1")); // NOI18N
        toorder.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 204)));
        toorder.setEnabled(false);
        toorder.setFocusable(false);
        toorder.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        toorder.setMaximumSize(new java.awt.Dimension(250, 30));
        toorder.setMinimumSize(new java.awt.Dimension(50, 30));
        toorder.setName(bundle.getString("ItemPanel.toorder.name")); // NOI18N
        toorder.setPreferredSize(new java.awt.Dimension(150, 30));
        toorder.setRequestFocusEnabled(false);
        toorder.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        toorder.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                toorderActionPerformed(evt);
            }
        });
        jToolBar1.add(toorder);

        toinvoice.set_Label(bundle.getString("ItemPanel2.toinvoice._Label")); // NOI18N
        toinvoice.setEnabled(false);
        toinvoice.setMaximumSize(new java.awt.Dimension(250, 30));
        toinvoice.setMinimumSize(new java.awt.Dimension(50, 30));
        toinvoice.setName("toinvoice"); // NOI18N
        toinvoice.setPreferredSize(new java.awt.Dimension(150, 30));
        toinvoice.setRequestFocusEnabled(false);
        jToolBar1.add(toinvoice);

        tocredit.setText(bundle.getString("ItemPanel2.tocredit.text")); // NOI18N
        tocredit.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 204)));
        tocredit.setEnabled(false);
        tocredit.setFocusable(false);
        tocredit.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        tocredit.setMaximumSize(new java.awt.Dimension(250, 30));
        tocredit.setMinimumSize(new java.awt.Dimension(50, 30));
        tocredit.setName("tocredit"); // NOI18N
        tocredit.setPreferredSize(new java.awt.Dimension(150, 30));
        tocredit.setRequestFocusEnabled(false);
        tocredit.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        tocredit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tocreditActionPerformed(evt);
            }
        });
        jToolBar1.add(tocredit);

        jSeparator4.setName("jSeparator4"); // NOI18N
        jToolBar1.add(jSeparator4);

        typelabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/mpv5/resources/images/22/editcopy.png"))); // NOI18N
        typelabel.setName("typelabel"); // NOI18N
        jToolBar1.add(typelabel);

        jSeparator1.setName("jSeparator1"); // NOI18N
        jToolBar1.add(jSeparator1);

        button_reminders.setText(bundle.getString("ItemPanel2.button_reminders.text")); // NOI18N
        button_reminders.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 204)));
        button_reminders.setEnabled(false);
        button_reminders.setFocusable(false);
        button_reminders.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        button_reminders.setMaximumSize(new java.awt.Dimension(250, 30));
        button_reminders.setMinimumSize(new java.awt.Dimension(50, 30));
        button_reminders.setName("button_reminders"); // NOI18N
        button_reminders.setPreferredSize(new java.awt.Dimension(150, 30));
        button_reminders.setRequestFocusEnabled(false);
        button_reminders.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        button_reminders.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                button_remindersActionPerformed(evt);
            }
        });
        jToolBar1.add(button_reminders);

        jButton2.setText(bundle.getString("ItemPanel2.jButton2.text")); // NOI18N
        jButton2.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 204)));
        jButton2.setFocusable(false);
        jButton2.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton2.setMaximumSize(new java.awt.Dimension(250, 30));
        jButton2.setMinimumSize(new java.awt.Dimension(50, 30));
        jButton2.setName("jButton2"); // NOI18N
        jButton2.setPreferredSize(new java.awt.Dimension(150, 30));
        jButton2.setRequestFocusEnabled(false);
        jButton2.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });
        jToolBar1.add(jButton2);

        jSeparator7.setName("jSeparator7"); // NOI18N
        jToolBar1.add(jSeparator7);

        button_schedule.setIcon(new javax.swing.ImageIcon(getClass().getResource("/mpv5/resources/images/16/kalarm.png"))); // NOI18N
        button_schedule.setText(bundle.getString("ItemPanel2.button_schedule.text")); // NOI18N
        button_schedule.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 204)));
        button_schedule.setEnabled(false);
        button_schedule.setMaximumSize(new java.awt.Dimension(250, 30));
        button_schedule.setMinimumSize(new java.awt.Dimension(50, 30));
        button_schedule.setName("button_schedule"); // NOI18N
        button_schedule.setPreferredSize(new java.awt.Dimension(150, 30));
        button_schedule.setRequestFocusEnabled(false);
        button_schedule.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                button_scheduleMouseClicked(evt);
            }
        });
        button_schedule.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                button_scheduleActionPerformed(evt);
            }
        });
        jToolBar1.add(button_schedule);

        button_preview.setText(bundle.getString("ItemPanel2.button_preview.text")); // NOI18N
        button_preview.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 204)));
        button_preview.setEnabled(false);
        button_preview.setFocusable(false);
        button_preview.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        button_preview.setMaximumSize(new java.awt.Dimension(250, 30));
        button_preview.setMinimumSize(new java.awt.Dimension(50, 30));
        button_preview.setName("button_preview"); // NOI18N
        button_preview.setPreferredSize(new java.awt.Dimension(150, 30));
        button_preview.setRequestFocusEnabled(false);
        button_preview.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        button_preview.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                button_previewActionPerformed(evt);
            }
        });
        jToolBar1.add(button_preview);

        button_deliverynote.setText(bundle.getString("ItemPanel2.button_deliverynote.text")); // NOI18N
        button_deliverynote.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 204)));
        button_deliverynote.setEnabled(false);
        button_deliverynote.setFocusable(false);
        button_deliverynote.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        button_deliverynote.setMaximumSize(new java.awt.Dimension(250, 30));
        button_deliverynote.setMinimumSize(new java.awt.Dimension(50, 30));
        button_deliverynote.setName("button_deliverynote"); // NOI18N
        button_deliverynote.setPreferredSize(new java.awt.Dimension(150, 30));
        button_deliverynote.setRequestFocusEnabled(false);
        button_deliverynote.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        button_deliverynote.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                button_deliverynoteActionPerformed(evt);
            }
        });
        jToolBar1.add(button_deliverynote);

        button_orderconf.setText(bundle.getString("ItemPanel2.button_orderconf.text")); // NOI18N
        button_orderconf.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 204)));
        button_orderconf.setEnabled(false);
        button_orderconf.setFocusable(false);
        button_orderconf.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        button_orderconf.setMaximumSize(new java.awt.Dimension(250, 30));
        button_orderconf.setMinimumSize(new java.awt.Dimension(50, 30));
        button_orderconf.setName("button_orderconf"); // NOI18N
        button_orderconf.setPreferredSize(new java.awt.Dimension(150, 30));
        button_orderconf.setRequestFocusEnabled(false);
        button_orderconf.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        button_orderconf.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                button_orderconfActionPerformed(evt);
            }
        });
        jToolBar1.add(button_orderconf);

        jPanel11.setName("jPanel11"); // NOI18N
        jPanel11.setLayout(new java.awt.BorderLayout());

        jPanel6.setAutoscrolls(true);
        jPanel6.setFocusCycleRoot(true);
        jPanel6.setFocusTraversalPolicyProvider(true);
        jPanel6.setName("jPanel6"); // NOI18N
        jPanel6.setLayout(new java.awt.BorderLayout());

        jToolBar2.setFloatable(false);
        jToolBar2.setRollover(true);
        jToolBar2.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jToolBar2.setMaximumSize(new java.awt.Dimension(310, 50));
        jToolBar2.setName("jToolBar2"); // NOI18N
        jToolBar2.setPreferredSize(new java.awt.Dimension(210, 30));

        jLabel1.setText(bundle.getString("ItemPanel2.jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N
        jToolBar2.add(jLabel1);

        jSeparator3.setName("jSeparator3"); // NOI18N
        jToolBar2.add(jSeparator3);

        netvalue.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        netvalue.setText(bundle.getString("ItemPanel2.netvalue.text")); // NOI18N
        netvalue.setName("netvalue"); // NOI18N
        jToolBar2.add(netvalue);

        jSeparator9.setName("jSeparator9"); // NOI18N
        jToolBar2.add(jSeparator9);

        jLabel7.setText(bundle.getString("ItemPanel2.jLabel7.text")); // NOI18N
        jLabel7.setName("jLabel7"); // NOI18N
        jToolBar2.add(jLabel7);

        jLabel5.setText(bundle.getString("ItemPanel2.jLabel5.text_1")); // NOI18N
        jLabel5.setName("jLabel5"); // NOI18N
        jToolBar2.add(jLabel5);

        jSeparator8.setName("jSeparator8"); // NOI18N
        jToolBar2.add(jSeparator8);

        discount.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        discount.setText(bundle.getString("ItemPanel2.discount.text")); // NOI18N
        discount.setName("discount"); // NOI18N
        jToolBar2.add(discount);

        jSeparator13.setName("jSeparator13"); // NOI18N
        jToolBar2.add(jSeparator13);

        jLabel8.setText(bundle.getString("ItemPanel2.jLabel8.text")); // NOI18N
        jLabel8.setName("jLabel8"); // NOI18N
        jToolBar2.add(jLabel8);

        jLabel2.setText(bundle.getString("ItemPanel2.jLabel2.text")); // NOI18N
        jLabel2.setName("jLabel2"); // NOI18N
        jToolBar2.add(jLabel2);

        jSeparator6.setName("jSeparator6"); // NOI18N
        jToolBar2.add(jSeparator6);

        taxvalue.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        taxvalue.setText(bundle.getString("ItemPanel2.taxvalue.text")); // NOI18N
        taxvalue.setName("taxvalue"); // NOI18N
        jToolBar2.add(taxvalue);

        jSeparator10.setName("jSeparator10"); // NOI18N
        jToolBar2.add(jSeparator10);

        jLabel9.setText(bundle.getString("ItemPanel2.jLabel9.text")); // NOI18N
        jLabel9.setName("jLabel9"); // NOI18N
        jToolBar2.add(jLabel9);

        jLabel3.setText(bundle.getString("ItemPanel2.jLabel3.text")); // NOI18N
        jLabel3.setName("jLabel3"); // NOI18N
        jToolBar2.add(jLabel3);

        jSeparator5.setName("jSeparator5"); // NOI18N
        jToolBar2.add(jSeparator5);

        value.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        value.setText(bundle.getString("ItemPanel2.value.text")); // NOI18N
        value.setName("value"); // NOI18N
        jToolBar2.add(value);

        jPanel6.add(jToolBar2, java.awt.BorderLayout.PAGE_END);

        jPanel4.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel4.setName("jPanel4"); // NOI18N

        jScrollPane3.setName("jScrollPane3"); // NOI18N

        itemtable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        itemtable.setName("itemtable"); // NOI18N
        itemtable.setSelectionBackground(new java.awt.Color(161, 176, 190));
        itemtable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        itemtable.setSurrendersFocusOnKeystroke(true);
        itemtable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                itemtableMouseClicked(evt);
            }
        });
        jScrollPane3.setViewportView(itemtable);
        itemtable.getColumnModel().getSelectionModel().setSelectionMode(javax.swing.ListSelectionModel.SINGLE_INTERVAL_SELECTION);

        itemPanel.setName("itemPanel"); // NOI18N
        itemPanel.setOpaque(false);

        addItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/mpv5/resources/images/16/add.png"))); // NOI18N
        addItem.setFocusable(false);
        addItem.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        addItem.setMaximumSize(new java.awt.Dimension(50, 50));
        addItem.setMinimumSize(new java.awt.Dimension(30, 30));
        addItem.setName("addItem"); // NOI18N
        addItem.setPreferredSize(new java.awt.Dimension(40, 40));
        addItem.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        addItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addItemActionPerformed(evt);
            }
        });
        itemPanel.add(addItem);

        delItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/mpv5/resources/images/16/remove.png"))); // NOI18N
        delItem.setFocusable(false);
        delItem.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        delItem.setMaximumSize(new java.awt.Dimension(50, 50));
        delItem.setMinimumSize(new java.awt.Dimension(30, 30));
        delItem.setName("delItem"); // NOI18N
        delItem.setPreferredSize(new java.awt.Dimension(40, 40));
        delItem.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        delItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                delItemActionPerformed(evt);
            }
        });
        itemPanel.add(delItem);

        upItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/mpv5/resources/images/16/arrow-up.png"))); // NOI18N
        upItem.setFocusable(false);
        upItem.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        upItem.setMaximumSize(new java.awt.Dimension(50, 50));
        upItem.setMinimumSize(new java.awt.Dimension(30, 30));
        upItem.setName("upItem"); // NOI18N
        upItem.setPreferredSize(new java.awt.Dimension(40, 40));
        upItem.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        upItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                upItemActionPerformed(evt);
            }
        });
        itemPanel.add(upItem);

        upItem1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/mpv5/resources/images/16/arrow-down.png"))); // NOI18N
        upItem1.setFocusable(false);
        upItem1.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        upItem1.setMaximumSize(new java.awt.Dimension(50, 50));
        upItem1.setMinimumSize(new java.awt.Dimension(30, 30));
        upItem1.setName("upItem1"); // NOI18N
        upItem1.setPreferredSize(new java.awt.Dimension(40, 40));
        upItem1.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        upItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                upItem1ActionPerformed(evt);
            }
        });
        itemPanel.add(upItem1);

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addComponent(itemPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 1486, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
            .addComponent(itemPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 303, Short.MAX_VALUE)
        );

        jPanel6.add(jPanel4, java.awt.BorderLayout.CENTER);

        jPanel11.add(jPanel6, java.awt.BorderLayout.CENTER);

        javax.swing.GroupLayout rightpaneLayout = new javax.swing.GroupLayout(rightpane);
        rightpane.setLayout(rightpaneLayout);
        rightpaneLayout.setHorizontalGroup(
            rightpaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel11, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 1546, Short.MAX_VALUE)
            .addComponent(jToolBar1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        rightpaneLayout.setVerticalGroup(
            rightpaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(rightpaneLayout.createSequentialGroup()
                .addComponent(jToolBar1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(3, 3, 3)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 221, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel11, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jTabbedPane2.addTab(bundle.getString("ItemPanel2.rightpane.TabConstraints.tabTitle"), rightpane); // NOI18N

        panel2.setName("panel2"); // NOI18N
        panel2.setLayout(new java.awt.BorderLayout());

        jPanel7.setName("jPanel7"); // NOI18N
        jPanel7.setLayout(new java.awt.BorderLayout());

        jSplitPane1.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 1));
        jSplitPane1.setLastDividerLocation(150);
        jSplitPane1.setName("jSplitPane1"); // NOI18N
        jSplitPane1.setOneTouchExpandable(true);
        jSplitPane1.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                jSplitPane1PropertyChange(evt);
            }
        });

        jTabbedPane1.setMinimumSize(new java.awt.Dimension(400, 61));
        jTabbedPane1.setName("jTabbedPane1"); // NOI18N

        jPanel3.setName("jPanel3"); // NOI18N
        jPanel3.setLayout(new java.awt.BorderLayout());

        jScrollPane1.setBorder(null);
        jScrollPane1.setName("jScrollPane1"); // NOI18N

        notes.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        notes.setToolTipText(bundle.getString("ItemPanel2.notes.toolTipText")); // NOI18N
        notes.setName("notes"); // NOI18N
        jScrollPane1.setViewportView(notes);

        jPanel3.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        labeledCombobox1.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        labeledCombobox1.set_Label(bundle.getString("ItemPanel2.labeledCombobox1._Label")); // NOI18N
        labeledCombobox1.setName("labeledCombobox1"); // NOI18N
        labeledCombobox1.setSearchEnabled(false);
        labeledCombobox1.setSearchOnEnterEnabled(false);
        jPanel3.add(labeledCombobox1, java.awt.BorderLayout.PAGE_START);

        jTabbedPane1.addTab(bundle.getString("ItemPanel2.jPanel3.TabConstraints.tabTitle"), jPanel3); // NOI18N

        jScrollPane4.setName("jScrollPane4"); // NOI18N

        proptable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null}
            },
            new String [] {
                "", ""
            }
        ));
        proptable.setAutoCreateRowSorter(true);
        proptable.setName("proptable"); // NOI18N
        proptable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane4.setViewportView(proptable);

        jTabbedPane1.addTab(bundle.getString("ItemPanel2.jScrollPane4.TabConstraints.tabTitle"), jScrollPane4); // NOI18N

        jSplitPane1.setLeftComponent(jTabbedPane1);

        jPanel8.setName("jPanel8"); // NOI18N

        jScrollPane2.setToolTipText(bundle.getString("ItemPanel2.jScrollPane2.toolTipText")); // NOI18N
        jScrollPane2.setName("jScrollPane2"); // NOI18N

        dataTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        dataTable.setToolTipText(bundle.getString("ItemPanel2.dataTable.toolTipText")); // NOI18N
        dataTable.setName("dataTable"); // NOI18N
        dataTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                dataTableMouseClicked(evt);
            }
        });
        jScrollPane2.setViewportView(dataTable);

        removefile.setIcon(new javax.swing.ImageIcon(getClass().getResource("/mpv5/resources/images/16/remove.png"))); // NOI18N
        removefile.setToolTipText(bundle.getString("ItemPanel2.removefile.toolTipText")); // NOI18N
        removefile.setName("removefile"); // NOI18N
        removefile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removefileActionPerformed(evt);
            }
        });

        addfile.setIcon(new javax.swing.ImageIcon(getClass().getResource("/mpv5/resources/images/16/add.png"))); // NOI18N
        addfile.setToolTipText(bundle.getString("ItemPanel2.addfile.toolTipText")); // NOI18N
        addfile.setName("addfile"); // NOI18N
        addfile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addfileActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel8Layout.createSequentialGroup()
                .addContainerGap(1121, Short.MAX_VALUE)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(addfile, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(removefile, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)))
            .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel8Layout.createSequentialGroup()
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 1114, Short.MAX_VALUE)
                    .addGap(31, 31, 31)))
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addComponent(addfile)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(removefile)
                .addContainerGap(456, Short.MAX_VALUE))
            .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 526, Short.MAX_VALUE))
        );

        jSplitPane1.setRightComponent(jPanel8);

        jPanel7.add(jSplitPane1, java.awt.BorderLayout.CENTER);

        panel2.add(jPanel7, java.awt.BorderLayout.CENTER);

        jPanel10.setName("jPanel10"); // NOI18N

        checkb_pront_oc.setText(bundle.getString("ItemPanel2.checkb_pront_oc.text")); // NOI18N
        checkb_pront_oc.setFocusable(false);
        checkb_pront_oc.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        checkb_pront_oc.setMaximumSize(new java.awt.Dimension(333, 20));
        checkb_pront_oc.setMinimumSize(new java.awt.Dimension(80, 20));
        checkb_pront_oc.setName("checkb_pront_oc"); // NOI18N
        checkb_pront_oc.setPreferredSize(new java.awt.Dimension(120, 20));
        checkb_pront_oc.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);

        date3.set_Label(bundle.getString("ItemPanel2.date3._Label")); // NOI18N
        date3.setName("date3"); // NOI18N
        date3.setPreferredSize(new java.awt.Dimension(280, 20));

        date2.set_Label(bundle.getString("ItemPanel2.date2._Label")); // NOI18N
        date2.setName("date2"); // NOI18N
        date2.setPreferredSize(new java.awt.Dimension(280, 20));

        javax.swing.GroupLayout jPanel10Layout = new javax.swing.GroupLayout(jPanel10);
        jPanel10.setLayout(jPanel10Layout);
        jPanel10Layout.setHorizontalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(date3, javax.swing.GroupLayout.PREFERRED_SIZE, 290, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(date2, javax.swing.GroupLayout.PREFERRED_SIZE, 290, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(checkb_pront_oc, javax.swing.GroupLayout.PREFERRED_SIZE, 323, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(1227, Short.MAX_VALUE))
        );
        jPanel10Layout.setVerticalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(checkb_pront_oc, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(date2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(date3, javax.swing.GroupLayout.DEFAULT_SIZE, 21, Short.MAX_VALUE)
                .addContainerGap())
        );

        panel2.add(jPanel10, java.awt.BorderLayout.PAGE_START);

        jTabbedPane2.addTab(bundle.getString("ItemPanel2.panel2.TabConstraints.tabTitle"), panel2); // NOI18N

        add(jTabbedPane2, java.awt.BorderLayout.CENTER);

        toolbarpane.setName("toolbarpane"); // NOI18N
        toolbarpane.setLayout(new java.awt.BorderLayout());
        add(toolbarpane, java.awt.BorderLayout.NORTH);
    }// </editor-fold>//GEN-END:initComponents

    private void button_order2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_button_order2ActionPerformed
        BigPopup.showPopup(this, new ControlPanel_Groups(), null);
}//GEN-LAST:event_button_order2ActionPerformed

    private void removefileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removefileActionPerformed
        deleteFile();
}//GEN-LAST:event_removefileActionPerformed

    private void addfileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addfileActionPerformed
        if (dataOwner.isExisting()) {
            addFile();
        }
}//GEN-LAST:event_addfileActionPerformed

    private void dataTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_dataTableMouseClicked
        fileTableClicked(evt);
    }//GEN-LAST:event_dataTableMouseClicked

    private void itemtableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_itemtableMouseClicked
        if (evt != null) {
            MPTableModel m = (MPTableModel) itemtable.getModel();
            if (evt.getButton() != MouseEvent.BUTTON1) {
                SubItem it = m.getRowAt(itemtable.getSelectedRow(), SubItem.getDefaultItem());

                if (it != null) {
                    mpv5.YabsViewProxy.instance().addToClipBoard(it);

                } else if (!m.hasEmptyRows(new int[]{4})) {
                    m.addRow(2);
                }
            }
        }
    }//GEN-LAST:event_itemtableMouseClicked
    MPTableModel omodel = null;
    private void addItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addItemActionPerformed
        ((MPTableModel) itemtable.getModel()).addRow(1);
    }//GEN-LAST:event_addItemActionPerformed

    private void delItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_delItemActionPerformed
        try {
            int index = itemtable.getSelectedRow();
            if (index < 0) {
                return;
            }

            MPTableModel m = (MPTableModel) itemtable.getModel();
            SubItem.addToDeletionQueue(m.getValueAt(index, 0));
            ((MPTableModel) itemtable.getModel()).removeRow(index);
        } catch (Exception e) {
            Log.Debug(e);
        }
    }//GEN-LAST:event_delItemActionPerformed

    private void upItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_upItemActionPerformed
        try {
            int index = itemtable.getSelectedRow();
            if (index <= 0) {
                return;
            }
            ((MPTableModel) itemtable.getModel()).moveRow(index, index, index - 1);
            itemtable.changeSelection(index - 1, itemtable.getSelectedColumn(), false, false);
        } catch (Exception e) {
            Log.Debug(e);
        }
    }//GEN-LAST:event_upItemActionPerformed

    private void upItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_upItem1ActionPerformed
        try {
            int index = itemtable.getSelectedRow();
            if (index < 0 || index >= itemtable.getRowCount() - 1) {
                return;
            }
            ((MPTableModel) itemtable.getModel()).moveRow(index, index, index + 1);
            itemtable.changeSelection(index + 1, itemtable.getSelectedColumn(), false, false);
        } catch (Exception e) {
            Log.Debug(e);
        }
    }//GEN-LAST:event_upItem1ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed

        try {
            int cid = Integer.valueOf(contactname.getSelectedItem().getId());
            Contact c = (Contact) DatabaseObject.getObject(Context.getContact(), cid);
            mpv5.YabsViewProxy.instance().getIdentifierView().addTab(c);
        } catch (NumberFormatException e) {
            //Nothing to show
        } catch (NodataFoundException e) {
            //Nothing to show
        }
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        if (MPSecurityManager.checkAdminAccess()) {
            JDialog d = new JDialog(YabsViewProxy.instance().getIdentifierFrame(), true);
            d.add(new ItemNumberEditor(dataOwner, d, this));
            d.pack();
            d.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            Position p = new Position(d);
            p.center();
            d.setVisible(true);
        }
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jSplitPane1PropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_jSplitPane1PropertyChange
        if (evt.getPropertyName().equals("dividerLocation")) {
            User.getCurrentUser().setProperty("org.openyabs.uiproperty.itempanel.divider1.dividerLocation", evt.getNewValue().toString());
        }
    }//GEN-LAST:event_jSplitPane1PropertyChange

    private void button_orderconfActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_button_orderconfActionPerformed
        confirmation();
    }//GEN-LAST:event_button_orderconfActionPerformed

    private void button_deliverynoteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_button_deliverynoteActionPerformed
        delivery();
    }//GEN-LAST:event_button_deliverynoteActionPerformed

    private void button_previewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_button_previewActionPerformed
        preview();
    }//GEN-LAST:event_button_previewActionPerformed

    private void button_scheduleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_button_scheduleActionPerformed

    }//GEN-LAST:event_button_scheduleActionPerformed

    private void button_scheduleMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_button_scheduleMouseClicked

        //        JCalendar.instanceOf(300, evt.getLocationOnScreen());
        if (dataOwner != null && dataOwner.isExisting()) {
            ScheduleDayEvent.instanceOf().setItem(dataOwner);
        }
    }//GEN-LAST:event_button_scheduleMouseClicked

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed

        if (dataOwner.isExisting()) {
            try {
                mpv5.YabsViewProxy.instance().getIdentifierView().addTab(DatabaseObject.getObject(Context.getContact(), dataOwner.__getContactsids()));
            } catch (NodataFoundException ex) {
                Log.Debug(ex);
            }
        }
    }//GEN-LAST:event_jButton2ActionPerformed

    private void button_remindersActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_button_remindersActionPerformed
        if (dataOwner != null && dataOwner.isExisting()) {
            BigPopup.showPopup(mpv5.YabsViewProxy.instance().getIdentifierFrame().getRootPane(), new RemindPanel(dataOwner), Messages.REMINDER.toString(), false);
        }
    }//GEN-LAST:event_button_remindersActionPerformed

    private void toorderActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_toorderActionPerformed
        toOrder();
    }//GEN-LAST:event_toorderActionPerformed

    private void tocreditActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tocreditActionPerformed
        toInvoice(Item.TYPE_CREDIT);
    }//GEN-LAST:event_tocreditActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        try {
            Item owner = (Item) DatabaseObject.getObject(Context.getOrder(), reforderids_, true);
            YabsViewProxy.instance().addOrShowTab(owner);
        } catch (NodataFoundException ex) {
            Log.Debug(ex);
        }
    }//GEN-LAST:event_jButton4ActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private mpv5.ui.beans.LabeledCombobox accountselect;
    private javax.swing.JButton addItem;
    private javax.swing.JLabel addedby;
    private javax.swing.JButton addfile;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JButton button_deliverynote;
    private javax.swing.JButton button_order2;
    private javax.swing.JButton button_orderconf;
    private javax.swing.JButton button_preview;
    private javax.swing.JButton button_reminders;
    private javax.swing.JButton button_schedule;
    private javax.swing.JCheckBox checkb_pront_oc;
    private mpv5.ui.beans.LabeledTextField city;
    private mpv5.ui.beans.LabeledTextField cname;
    private mpv5.ui.beans.LabeledCombobox contactname;
    private javax.swing.JTable dataTable;
    private mpv5.ui.beans.LabeledDateChooser date1;
    private mpv5.ui.beans.LabeledDateChooser date2;
    private mpv5.ui.beans.LabeledDateChooser date3;
    private javax.swing.JButton delItem;
    private javax.swing.JLabel discount;
    private mpv5.ui.beans.LabeledCombobox groupnameselect;
    private javax.swing.JPanel itemPanel;
    private javax.swing.JTable itemtable;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel12;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JToolBar.Separator jSeparator1;
    private javax.swing.JToolBar.Separator jSeparator10;
    private javax.swing.JToolBar.Separator jSeparator13;
    private javax.swing.JToolBar.Separator jSeparator3;
    private javax.swing.JToolBar.Separator jSeparator4;
    private javax.swing.JToolBar.Separator jSeparator5;
    private javax.swing.JToolBar.Separator jSeparator6;
    private javax.swing.JToolBar.Separator jSeparator7;
    private javax.swing.JToolBar.Separator jSeparator8;
    private javax.swing.JToolBar.Separator jSeparator9;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTabbedPane jTabbedPane2;
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JToolBar jToolBar2;
    private mpv5.ui.beans.LabeledCombobox labeledCombobox1;
    private javax.swing.JPanel leftpane;
    private mpv5.ui.beans.LabeledTextField name;
    private javax.swing.JLabel netvalue;
    private javax.swing.JTextPane notes;
    private mpv5.ui.beans.LabeledTextField number;
    private javax.swing.JPanel panel2;
    private mpv5.ui.beans.LabeledTextField prename;
    private javax.swing.JTable proptable;
    private mpv5.ui.beans.LabeledCombobox refOrder;
    private javax.swing.JButton removefile;
    private javax.swing.JPanel rightpane;
    private mpv5.ui.beans.LabeledCombobox status;
    private javax.swing.JLabel staus_icon;
    private mpv5.ui.beans.LabeledTextField street;
    private javax.swing.JLabel taxvalue;
    private javax.swing.JButton tocredit;
    private mpv5.ui.beans.DropDownButton toinvoice;
    private javax.swing.JPanel toolbarpane;
    private javax.swing.JButton toorder;
    private javax.swing.JLabel type;
    private javax.swing.JLabel typelabel;
    private javax.swing.JButton upItem;
    private javax.swing.JButton upItem1;
    private javax.swing.JLabel value;
    private mpv5.ui.beans.LabeledTextField zip;
    // End of variables declaration//GEN-END:variables
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

    @Override
    public boolean collectData() {
        try {
            contactsids_ = Integer.valueOf(contactname.getSelectedItem().getId());
        } catch (Exception e) {
            try {
                String cn = cname.getText(true, Messages.ENTER_VALUE);
                Contact c = Contact.findByCname(cn);

                if (c == null) {
                    c = new Contact();
                    c.setCname(cn);
                    c.setPrename(prename.getText());
                    c.setZip(zip.getText());
                    c.setCity(city.getText());
                    c.setisCustomer(true);
                    c.save();
                }

                contactsids_ = c.__getIDS();
                Contact owner = (Contact) DatabaseObject.getObject(Context.getContact(), c.__getIDS(), true);
                contactname.setModel(owner);
                setContactData(owner);
                contactsids_ = owner.__getIDS();
            } catch (Exception ex) {
                Notificator.raiseNotification(ex, false);
                return false;
            }
        }
        if (contactsids_ > 0) {
            try {
                accountsids_ = Integer.valueOf(accountselect.getSelectedItem().getId());
            } catch (Exception e) {
                accountsids_ = 1;
            }

            if (groupnameselect.getSelectedItem() != null) {
                try {
                    int group = Integer.valueOf(groupnameselect.getSelectedItem().getId());
                    group_ = (Group) DatabaseObject.getObject(Context.getGroup(), group);
                    Log.Debug(this, groupnameselect.getSelectedItem().getId());
                } catch (NodataFoundException ex) {
                    Log.Debug(this, ex);
                }
            }

            if (dateadded_ == null) {
                dateadded_ = new Date();
            }
            intaddedby_ = User.getUserId(addedby.getText());
            description_ = notes.getText();
            dateadded_ = date1.getDate();

            netvalue_ = FormatNumber.parseDezimal(netvalue.getText());
            taxvalue_ = FormatNumber.parseDezimal(taxvalue.getText());
            discountvalue_ = FormatNumber.parseDezimal(discount.getText());
            discountgrosvalue_ = FormatNumber.parseDezimal(hidden.getText());
//            try {
//                shippingvalue_ = FormatNumber.parseDezimal(shipping.getText());
//            } catch (Exception e) {
            shippingvalue_ = BigDecimal.ZERO;
//            }

            datetodo_ = date2.getDate();
            dateend_ = date3.getDate();
            intstatus_ = Integer.valueOf(status.getSelectedItem().getId());

            try {
                reforderids_ = Integer.valueOf(refOrder.getSelectedItem().getId());
            } catch (Exception e) {
                reforderids_ = -1;
            }

        } else {
            showRequiredFields();
            return false;
        }

        return true;
    }

    @Override
    public void exposeData() {

        number.setText(cnumber_);
        name.setText(cname_);
        date1.setDate(dateadded_);
        if (User.getCurrentUser().getProperties().getProperty("org.openyabs.itemproperty", "keepmodifiedtransdate")) {
            date2.setDate(datetodo_);
        } else {
            date2.setDate(new Date());
        }
        if (User.getCurrentUser().getProperties().getProperty("org.openyabs.itemproperty", "keepmodifiedenddate")) {
            date3.setDate(dateend_);
        } else {
            date3.setDate(new Date());
        }
        notes.setText(description_);

//        shipping.setText(FormatNumber.formatDezimal(shippingvalue_));
        button_reminders.setToolTipText(Messages.REMINDERS + String.valueOf(intreminders_));
        //  discountpercent.setValue(discountvalue_);
        List<Integer> skip = new ArrayList<>();
        if (inttype_ == Item.TYPE_INVOICE
                || inttype_ == Item.TYPE_DEPOSIT
                || inttype_ == Item.TYPE_PART_PAYMENT
                || inttype_ == Item.TYPE_CREDIT) {
            skip.add(Item.STATUS_PAUSED);
        } else {
            skip.add(Item.STATUS_PAID);
        }
        status.setModel(Item.getStatusStrings(), MPComboBoxModelItem.COMPARE_BY_ID, skip);
        status.setSelectedItem(intstatus_);
        staus_icon.setIcon(dataOwner.getIcon());
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

        addedby.setText(User.getUsername(intaddedby_));
        try {
            Contact owner = (Contact) DatabaseObject.getObject(Context.getContact(), contactsids_, true);
            contactname.setModel(owner);
            setContactData(owner);
            contactsids_ = owner.__getIDS();
        } catch (NodataFoundException ex) {
            Log.Debug(ex);
        }

        fillFiles();
    }

    private void refreshSync() {
        try {

            groupnameselect.setModel(MPComboBoxModelItem.toModel(DatabaseObject.getObject(Context.getGroup(), mpv5.db.objects.User.getCurrentUser().__getGroupsids())));
            groupnameselect.setSelectedIndex(0);
            sp.refresh();

            try {
                accountselect.setModel(DatabaseObject.getObject(Context.getAccounts(), mpv5.db.objects.User.getCurrentUser().__getIntdefaultaccount()));
            } catch (NodataFoundException nodataFoundException) {
                Log.Debug(this, nodataFoundException.getMessage());
            }

            List<Integer> skip = new ArrayList<>();
            if (inttype_ == Item.TYPE_INVOICE
                    || inttype_ == Item.TYPE_DEPOSIT
                    || inttype_ == Item.TYPE_PART_PAYMENT) {
                skip.add(Item.STATUS_PAUSED);
            } else {
                skip.add(Item.STATUS_PAID);
            }
            status.setModel(Item.getStatusStrings(), MPComboBoxModelItem.COMPARE_BY_ID, skip);
            try {
                status.setSelectedIndex(mpv5.db.objects.User.getCurrentUser().__getIntdefaultstatus());
            } catch (Exception e) {
                Log.Debug(this, e.getMessage());
            }
            itemtable.setModel(SubItem.toModel(new SubItem[]{
                SubItem.getDefaultItem(), SubItem.getDefaultItem(),
                SubItem.getDefaultItem(), SubItem.getDefaultItem(),
                SubItem.getDefaultItem(), SubItem.getDefaultItem()
            }));
            formatTable();
//                    shipping.setText(FormatNumber.formatDezimal(0d));

        } catch (Exception e) {
            Log.Debug(this, e);
        }

        if (dataOwner.isExisting()) {
            setDataOwner(dataOwner, true);
        }
    }

    @Override
    public final void refresh() {

        Runnable runnable = new Runnable() {

            @Override
            public void run() {
                refreshSync();
            }
        };

        SwingUtilities.invokeLater(runnable);

    }

    /**
     *
     */
    public void formatTable() {

        prepareTable();

        //"Internal ID", Position, "Count", "Measure", "Text", "Netto Price", "Tax Rate", "Total Price", "Tax value", "Net 2", "Product ID", "", "", "Link", "Optional", "Discount", ??, Discount w. Tax
        TableFormat.resizeCols(itemtable, new Integer[]{0, 23, 53, 63, 100, 63, 63, 63, 0, 0, 63, 20, 0, 0, 100, 63, 0, 0},
                new Boolean[]{true, true, true, true, false, true, true, true, true, true, true, true, true, true, false, true, true, true});
        MPTableModel model = (MPTableModel) itemtable.getModel();
        model.setCanEdits(new boolean[]{false, false, true, true, true, true, true, true, false, false, true, true, false, false, true, true, false, false});
        TableFormat.changeBackground(itemtable, 1, Color.LIGHT_GRAY);
        if (mpv5.db.objects.User.getCurrentUser().getProperties().getProperty("org.openyabs.uiproperty", "hidecolumnquantity")) {
            TableFormat.stripColumn(itemtable, 2);
            model.setCellEditable(0, 2, false);
        }
        if (mpv5.db.objects.User.getCurrentUser().getProperties().getProperty("org.openyabs.uiproperty", "hidecolumnmeasure")) {
            TableFormat.stripColumn(itemtable, 3);
            model.setCellEditable(0, 3, false);
        }

        if (mpv5.db.objects.User.getCurrentUser().getProperties().getProperty("org.openyabs.uiproperty", "hideproductscolumn")) {
            TableFormat.stripColumn(itemtable, 10);
            model.setCellEditable(0, 10, false);
        }

        if (mpv5.db.objects.User.getCurrentUser().getProperties().getProperty("org.openyabs.uiproperty", "hidetaxcolumn")) {
            TableFormat.stripColumn(itemtable, 6);
            model.setCellEditable(0, 6, false);
        }

        if (mpv5.db.objects.User.getCurrentUser().getProperties().getProperty("org.openyabs.uiproperty", "hidediscountcolumn")) {
            TableFormat.stripColumn(itemtable, 15);
            model.setCellEditable(0, 15, false);
        }

        TextAreaCellEditor r = new TextAreaCellEditor(itemtable);
        ProductSelectDialog3 productSelectDialog = new ProductSelectDialog3(mpv5.YabsViewProxy.instance().getIdentifierFrame(), true, itemtable);
        productSelectDialog.okButton.addActionListener(r);
        productSelectDialog.cancelButton.addActionListener(r);
        r.setDialog(productSelectDialog, productSelectDialog.getIDTextField());
        r.setEditorTo(10);

        if (!mpv5.db.objects.User.getCurrentUser().getProperties().getProperty("org.openyabs.uiproperty", "showoptionalcolumn")) {
            TableFormat.stripColumn(itemtable, 14);
            model.setCellEditable(0, 14, false);
        } else {
            int widthc = mpv5.db.objects.User.getCurrentUser().getProperties().getProperty("org.openyabs.uiproperty.optionalcolumn.width", 0);
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

    @Override
    @SuppressWarnings("unchecked")
    public void paste(DatabaseObject... dbos) {

        if (itemtable.getCellEditor() != null) {
            try {
                itemtable.getCellEditor().stopCellEditing();
            } catch (Exception e) {
            }
        }

        try {
            ((MPTableModel) itemtable.getModel()).removeEmptyRows(new int[]{4});
        } catch (Exception e) {
            Log.Debug(e);
        }

        BigDecimal tpvs = null;
        List<DatabaseObject> dbolist = Arrays.asList(dbos);
        Collections.sort(dbolist, new Comparator<DatabaseObject>() {

            @Override
            public int compare(DatabaseObject o1, DatabaseObject o2) {
                return o1.__getDateadded().compareTo(o2.__getDateadded());
            }
        });
        for (final DatabaseObject dbo : dbolist) {
            if (dbo.getContext().equals(Context.getInvoice())
                    || dbo.getContext().equals(Context.getOffer())
                    || dbo.getContext().equals(Context.getOrder())) {
                Item o = (Item) dbo.clone();

                if (mpv5.db.objects.User.getCurrentUser().getProperties().getProperty("org.openyabs.uiproperty", "pasten")) {
                    SubItem s = new SubItem();
                    s.setQuantityvalue(BigDecimal.ONE);
//                    s.setItemsids(o.__getIDS());
                    s.setInternalvalue(((Item) dbo).__getNetvalue());
                    s.setExternalvalue(((Item) dbo).__getNetvalue());
                    s.setTotalnetvalue(((Item) dbo).__getNetvalue());
                    s.setTotalbrutvalue(((Item) dbo).__getNetvalue().add(((Item) dbo).__getTaxvalue()));
                    if (s.__getTotalnetvalue().doubleValue() > 0d) {
                        BigDecimal tp = s.__getTotalbrutvalue().subtract(s.__getTotalnetvalue()).multiply(Constants.BD100).divide(s.__getTotalnetvalue(), 9, RoundingMode.HALF_UP);
                        if (tpvs == null) {
                            tpvs = tp;
                        }
//                        if (tpvs.equals(tpvs)) {
                        s.setTaxpercentvalue(tp);
//                        } else {
//                            Popup.warn(Messages.TAXES_NOT_EQUAL);
//                            break;
//                        }
                    }
                    s.setCname(((Item) dbo).__getCname());
                    s.setDescription(Messages.GOOSE1 + " " + ((Item) dbo).__getCnumber() + " " + Messages.GOOSE2 + " " + DateConverter.getDefDateString(o.__getDateadded()));
//                   if (mpv5.db.objects.User.getCurrentUser().getProperties().hasProperty("deftax")) {
//                        int taxid = mpv5.db.objects.User.getCurrentUser().getProperties().getProperty("deftax", new Integer(0));
//                        BigDecimal deftax = Tax.getTaxValue(taxid);
//                        s.setTaxpercentvalue(deftax);
//                    }

//                    Log.PrintArray(s.toStringArray());
                    afterSaveRunnables.add(new Runnable() {

                        @Override
                        public void run() {
                            ((Item) dbo).setIntstatus(Item.STATUS_FINISHED);
                            dbo.save();
                        }
                    });

                    omodel = (MPTableModel) itemtable.getModel();
                    omodel.addRow(s.getRowData(omodel.getRowCount() + 1));
                    ((MPTableModel) itemtable.getModel()).fireTableCellUpdated(0, 0);

                } else {
                    o.setIntstatus(Item.STATUS_IN_PROGRESS);
                    o.setInttype(inttype_);
                    o.setCnumber("");
                    o.setCname("");
                    o.setDateadded(new Date());
                    o.setDatetodo(new Date());
                    o.setDateend(new Date());

                    SubItem[] sitems = ((Item) dbo).getSubitems();
                    SubItem[] subs = new SubItem[sitems.length];
                    for (int i = 0; i < sitems.length; i++) {
                        SubItem si = sitems[i];
                        SubItem psi = new SubItem();
                        psi.setQuantityvalue(BigDecimal.ONE);
                        psi.setInternalvalue(si.__getInternalvalue());
                        psi.setExternalvalue(si.__getExternalvalue());
                        psi.setTotalnetvalue(si.__getTotalnetvalue());
                        psi.setTotalbrutvalue(((Item) dbo).__getNetvalue().add(((Item) dbo).__getTaxvalue()));
                        if (psi.__getTotalnetvalue().doubleValue() > 0d) {
                            BigDecimal tp = psi.__getTotalbrutvalue().subtract(psi.__getTotalnetvalue()).multiply(Constants.BD100).divide(psi.__getTotalnetvalue(), 9, RoundingMode.HALF_UP);
                            psi.setTaxpercentvalue(tp);
                        }
                        psi.setCname(si.__getCname());
                        psi.setDescription(si.__getDescription());
                        psi.setCountvalue(si.__getCountvalue());
                        psi.setOrdernr(si.__getCountvalue().intValue());
                        subs[i] = psi;
                    }
                    o.setIDS(-1);
                    setDataOwner(o, true);

                    MPTableModel t = SubItem.toModel(subs, true);

                    itemtable.setModel(t);
                    omodel = (MPTableModel) itemtable.getModel();
                    ((MPTableModel) itemtable.getModel()).fireTableCellUpdated(0, 0);
                }
            } else if (dbo.getContext().equals(Context.getContact())) {
                dataOwner.setContactsids(((Contact) dbo).__getIDS());
                setDataOwner(dataOwner, true);
            } else if (dbo.getContext().equals(Context.getProduct())) {
                ((MPTableModel) itemtable.getModel()).addRow(
                        new SubItem((Product) dbo).getRowData(((MPTableModel) itemtable.getModel()).getRowCount() + 1));
                ((MPTableModel) itemtable.getModel()).fireTableCellUpdated(0, 0);
            } else if (dbo.getContext().equals(Context.getProductList())) {
                try {
                    SubItem[] subs = new SubItem[0];
                    if (dataOwner != null) {
                        subs = dataOwner.getSubitems();
                    }
                    List<ProductlistSubItem> l = ProductList.getReferencedObjects(dbo, Context.getProductListItems(), new ProductlistSubItem());
                    MPTableModel t = SubItem.toModel(subs);
                    int count = t.getRowCount();
                    for (int i = 0; i < l.size(); i++) {
                        ProductlistSubItem productlistSubItem = l.get(i);
                        productlistSubItem.setIDS(-1);
                        t.addRow(productlistSubItem.getRowData(i + count + 1));
                    }
                    itemtable.setModel(t);
                    omodel = (MPTableModel) itemtable.getModel();
                    ((MPTableModel) itemtable.getModel()).fireTableCellUpdated(0, 0);
                } catch (NodataFoundException ex) {
                    Log.Debug(this, ex.getMessage());
                }
            } else if (dbo.getContext().equals(Context.getSubItem())) {
                try {
                    SubItem sub = (SubItem) dbo;

                    ((MPTableModel) itemtable.getModel()).addRow(
                            sub.getRowData(((MPTableModel) itemtable.getModel()).getRowCount() + 1));

                    ((MPTableModel) itemtable.getModel()).fireTableCellUpdated(0, 0);
                } catch (Exception ex) {
                    Log.Debug(this, ex.getMessage());
                }
            } else {
                mpv5.YabsViewProxy.instance().addMessage(Messages.NOT_POSSIBLE.toString() + Messages.ACTION_PASTE.toString(), Color.RED);
                Log.Debug(this, dbo.getContext() + " to " + Context.getInvoice());
            }
        }

        try {
            itemtable.changeSelection(0, 0, true, false);
        } catch (Exception e) {
            Log.Debug(e);
        }

        itemMultiplier.calculateOnce();
        netCalculator.calculateOnce();
        taxcalculator.calculateOnce();
        discnetcalculator.calculateOnce();
        discbrutcalculator.calculateOnce();
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
        saveSubItems(true);
        omodel = (MPTableModel) itemtable.getModel();
        setTitle();
        for (int i = 0; i < afterSaveRunnables.size(); i++) {
            Runnable runnable = afterSaveRunnables.get(i);
            runnable.run();
        }
        afterSaveRunnables.clear();
    }

    @Override
    public void actionAfterCreate() {

        sp.refresh();
        ArrayUtilities.replaceColumn(itemtable, 0, null);
        saveSubItems(false);
        omodel = (MPTableModel) itemtable.getModel();
        setTitle();

    }

    private void saveSubItems(boolean deleteRemovedSubitems) {
        if (itemtable.getCellEditor() != null) {
            try {
                itemtable.getCellEditor().stopCellEditing();
            } catch (Exception e) {
            }
        }

        try {
            itemtable.changeSelection(0, 0, true, false);
        } catch (Exception e) {
            Log.Debug(e);
        }

        if (dataOwner.__getInttype() != Item.TYPE_INVOICE) {
            Product.createProducts(SubItem.saveModel(dataOwner, (MPTableModel) itemtable.getModel(), deleteRemovedSubitems), dataOwner);
        } else {
            SubItem.saveModel(dataOwner, (MPTableModel) itemtable.getModel(), deleteRemovedSubitems);
        }

        for (int i = 0; i < usedOrders.size(); i++) {
            Item o = usedOrders.get(i);
            o.setIntstatus(Item.STATUS_FINISHED);
            o.save(true);
        }
    }
    List<Item> usedOrders = new ArrayList<>();

    @Override
    public void changeSelection(MPComboBoxModelItem to, Context c) {
        try {
            DatabaseObject o = DatabaseObject.getObject(c, Integer.valueOf(to.getId()));
            int i = itemtable.getSelectedRow();
            if (i >= 0) {
                Object opt = itemtable.getModel().getValueAt(i, 14);
                ((MPTableModel) itemtable.getModel()).setRowAt(new SubItem((Product) o).getRowData(i), i, 4);
                itemtable.setValueAt(opt, i, 14);
            }
        } catch (NumberFormatException ex) {
        } catch (NodataFoundException ex) {
        }
    }

    @Override
    public void actionBeforeCreate() {
        status.setSelectedIndex(Item.STATUS_IN_PROGRESS);
        date1.setDate(new Date());
        date2.setDate(new Date());
        if (!User.getCurrentUser().getProperties().getProperty("org.openyabs.itemproperty", "keepmodifiedenddate")) {
            Contact dbo = null;
            final MPComboBoxModelItem item = contactname.getSelectedItem();
            if (item != null && item.isValid()) {
                try {
                    dbo = (Contact) DatabaseObject.getObject(Context.getContact(), Integer.valueOf(item.getId()));
                } catch (NodataFoundException ex) {
                    Logger.getLogger(ItemPanel2.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            setEnddate(dbo);
        }
    }

    @Override
    public void actionBeforeSave() throws ChangeNotApprovedException {
        if (dataOwner.isExisting()) {
            if ((dataOwner.__getIntstatus() != Item.STATUS_PAID && dataOwner.__getIntstatus() != Item.STATUS_CANCELLED) || Popup.Y_N_dialog(Messages.REALLY_CHANGE_DONE_ITEM)) {

                if (mpv5.db.objects.User.getCurrentUser().getProperties().getProperty("org.openyabs.uiproperty", "dowarnings")) {

                    if (!Popup.Y_N_dialog(Messages.REALLY_CHANGE)) {
                        throw new ChangeNotApprovedException(dataOwner);
                    }
                }
            } else {
                throw new ChangeNotApprovedException(dataOwner);
            }
        }
    }

    private void prepareTable() {
        //"Internal ID", Position, "Count", "Measure", "Text", "Netto Price", "Tax Rate", "Total Price", "Tax value", "Net 2", "Product ID", "", "", "Link", "Optional", "Discount"

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
        TableCellRendererForDezimal tc = new TableCellRendererForDezimal(itemtable, new java.awt.Color(161, 176, 190));
        tc.setRendererTo(7);
        if (calcoptionalcol) {
            tc.setRendererTo(14);
        }

//        CellEditorWithMPComboBox r = new CellEditorWithMPComboBox(Context.getProduct(), itemtable);
//        r.setEditorTo(4, this);
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
        ((MPTableModel) itemtable.getModel()).addCalculator(itemMultiplier);
        itemMultiplier.addLabel(value, 7);

        netCalculator = new DynamicTableCalculator(itemtable, quantXnet, new int[]{9});
        ((MPTableModel) itemtable.getModel()).addCalculator(netCalculator);
        netCalculator.addLabel(netvalue, 9);

        taxcalculator = new DynamicTableCalculator(itemtable, "(((" + quantXnet + ")-(" + quantXnet + "%[15]))+((" + quantXnet + ")-(" + quantXnet + "%[15]))%[6])-((" + quantXnet + ")-(" + quantXnet + "%[15]))", new int[]{8});
        ((MPTableModel) itemtable.getModel()).addCalculator(taxcalculator);
        taxcalculator.addLabel(taxvalue, 8);

        discnetcalculator = new DynamicTableCalculator(itemtable, quantXnet + "%[15]", new int[]{16});
        ((MPTableModel) itemtable.getModel()).addCalculator(discnetcalculator);
        discnetcalculator.addLabel(discount, 16);

        discbrutcalculator = new DynamicTableCalculator(itemtable, "((" + quantXnet + "%[15])+(" + quantXnet + "%[15]" + "%[6]))", new int[]{17});
        ((MPTableModel) itemtable.getModel()).addCalculator(discbrutcalculator);
        discbrutcalculator.addLabel(hidden, 17);

//        if (mpv5.db.objects.User.getCurrentUser().getProperties().hasProperty("shiptax")) {
//            int taxid = mpv5.db.objects.User.getCurrentUser().getProperties().getProperty("shiptax", new Integer(0));
//            Double shiptax = Tax.getTaxValue(taxid).doubleValue();
//            Double shipt = (shiptax / 100);
//            itemMultiplier.addAsRow(1, new LabeledTextField(1d, Double.class), 2);
//            itemMultiplier.addAsRow(1, new LabeledTextField(shiptax, Double.class), 6);
//            itemMultiplier.addAsRow(1, shipping, 5);
//
//            netCalculator.addAsRow(1, new LabeledTextField(1d, Double.class), 2);
//            netCalculator.addAsRow(1, new LabeledTextField(shiptax, Double.class), 6);
//            netCalculator.addAsRow(1, shipping, 5);
//            taxcalculator.addAsRow(1, new LabeledTextField(1d, Double.class), 2);
//            taxcalculator.addAsRow(1, new LabeledTextField(shiptax, Double.class), 6);
//            taxcalculator.addAsRow(1, shipping, 5);
//        } else {
//            itemMultiplier.addToSum(shipping);
//            netCalculator.addToSum(shipping);
//        }
        JButton b1 = new JButton();
        b1.addMouseListener(new MouseListener() {

            @Override
            public void mouseClicked(MouseEvent e) {
            }

            @Override
            public void mousePressed(MouseEvent e) {
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (!mpv5.db.objects.User.getCurrentUser().getProperties().getProperty("org.openyabs.uiproperty", "ordersoverproducts")) {
                    ProductSelectDialog.instanceOf((MPTableModel) itemtable.getModel(), itemtable.getSelectedRow(), e, 0, null, null);
                } else {
                    SubItem s = new SubItem();
                    Item o = (Item) Popup.SelectValue(Context.getOrder());
                    if (o != null) {
                        s.setQuantityvalue(BigDecimal.ONE);
                        s.setItemsids(o.__getIDS());
                        s.setExternalvalue(o.__getNetvalue().add(o.__getTaxvalue()));
                        s.setTotalnetvalue(o.__getNetvalue());
                        s.setCname(o.__getCname());
                        s.setDescription(Messages.TYPE_ORDER + " " + o.__getCnumber() + " " + DateConverter.getDefDateString(o.__getDateadded()));

                        ((MPTableModel) itemtable.getModel()).setRowAt(s.getRowData(itemtable.getSelectedRow()), itemtable.getSelectedRow(), 1);

                        usedOrders.add(o);
                    }
                }

                if (((MPTableModel) itemtable.getModel()).getEmptyRows(new int[]{4}) < 2) {
                    ((MPTableModel) itemtable.getModel()).addRow(1);
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
            }

            @Override
            public void mouseExited(MouseEvent e) {
            }
        });

        JButton b2 = new JButton();
        b2.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                MPTableModel m = (MPTableModel) itemtable.getModel();
                int row = itemtable.getSelectedRow();
                SubItem.addToDeletionQueue(m.getValueAt(row, 0));
                m.setRowAt(SubItem.getDefaultItem().getRowData(row), row, 1);
            }
        });

        itemtable.getColumnModel().getColumn(SubItem.COLUMNINDEX_ADD).setCellRenderer(new ButtonRenderer());
        itemtable.getColumnModel().getColumn(SubItem.COLUMNINDEX_ADD).setCellEditor(new ButtonEditor(b1));
        itemtable.getColumnModel().getColumn(SubItem.COLUMNINDEX_REMOVE).setCellRenderer(new ButtonRenderer());
        itemtable.getColumnModel().getColumn(SubItem.COLUMNINDEX_REMOVE).setCellEditor(new ButtonEditor(b2));

        TablePopUp tpu = new TablePopUp(itemtable, new String[]{
            Messages.ACTION_COPY.getValue(),
            Messages.ACTION_PASTE.getValue(),
            null,
            Messages.ACTION_ADD.getValue(),
            Messages.ACTION_REMOVE.getValue()},
                new ActionListener[]{new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    MPTableModel m = (MPTableModel) itemtable.getModel();
                    SubItem it = m.getRowAt(itemtable.getSelectedRow(), SubItem.getDefaultItem());

                    if (it != null) {
                        mpv5.YabsViewProxy.instance().addToClipBoard(it);

                    }
                }
            }, new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
//                    mpv5.YabsViewProxy.instance().pasteClipboardItems();
                }
            },
                    null,
                    new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    ((MPTableModel) itemtable.getModel()).addRow(1);
                }
            }, new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    int index = itemtable.getSelectedRow();
                    if (index < 0) {
                        return;
                    }

                    MPTableModel m = (MPTableModel) itemtable.getModel();
                    SubItem.addToDeletionQueue(m.getValueAt(index, 0));
                    ((MPTableModel) itemtable.getModel()).removeRow(index);
                }
            }});
    }

    private void delivery() {
        PreviewPanel pr;
        if (dataOwner != null && dataOwner.isExisting()) {
            if (TemplateHandler.isLoaded(Long.valueOf(dataOwner.templateGroupIds()), Constants.TYPE_DELIVERY_NOTE)) {
                pr = new PreviewPanel();
                pr.setDataOwner(dataOwner);
                new Job(Export.createFile(TemplateHandler.loadTemplate(dataOwner.templateGroupIds(), Constants.TYPE_DELIVERY_NOTE), dataOwner), pr).execute();
            } else {
                Popup.notice(Messages.NO_TEMPLATE_LOADED + " (" + mpv5.db.objects.User.getCurrentUser() + ")");
            }
        }
    }

    private void confirmation() {
        PreviewPanel pr;
        if (dataOwner != null && dataOwner.isExisting()) {
            if (TemplateHandler.isLoaded(Long.valueOf(dataOwner.templateGroupIds()), Constants.TYPE_ORDER_CONFIRMATION)) {

                pr = new PreviewPanel();
                pr.setDataOwner(dataOwner);
                new Job(Export.createFile(TemplateHandler.loadTemplate(dataOwner.templateGroupIds(), Constants.TYPE_ORDER_CONFIRMATION), dataOwner), pr).execute();
            } else {
                Popup.notice(Messages.NO_TEMPLATE_LOADED + " (" + mpv5.db.objects.User.getCurrentUser() + ")");
            }
        }
    }

    @Override
    public void mail() {

        if (dataOwner != null && dataOwner.isExisting()) {
            if (TemplateHandler.isLoaded(Long.valueOf(dataOwner.templateGroupIds()), dataOwner.__getInttype())) {

                try {
                    Contact cont = (Contact) (Contact.getObject(Context.getContact(), dataOwner.__getContactsids()));
                    Export.mail(TemplateHandler.loadTemplate(dataOwner.templateGroupIds(), dataOwner.__getInttype()), dataOwner, cont);
                } catch (NodataFoundException ex) {
                    Log.Debug(ex);
                }
            } else {
                Popup.notice(Messages.NO_TEMPLATE_LOADED + " (" + mpv5.db.objects.User.getCurrentUser() + ")");
            }
        } else {
            Popup.notice(Messages.NOT_POSSIBLE + "\n" + Messages.NOT_SAVED_YET);
        }
    }

    @Override
    public void print() {
        if (dataOwner != null && dataOwner.isExisting()) {
            if (TemplateHandler.isLoaded(Long.valueOf(dataOwner.templateGroupIds()), dataOwner.__getInttype())) {
                if (checkb_pront_oc.isSelected() && checkb_pront_oc.isEnabled()) {
                    Export.print(new Template[]{TemplateHandler.loadTemplate(dataOwner.templateGroupIds(), dataOwner.__getInttype()), TemplateHandler.loadTemplate(dataOwner.templateGroupIds(), Constants.TYPE_ORDER_CONFIRMATION)}, dataOwner);
                } else {
                    Export.print(TemplateHandler.loadTemplate(dataOwner.templateGroupIds(), dataOwner.__getInttype()), dataOwner);
                }
            } else {
                Popup.notice(Messages.NO_TEMPLATE_LOADED + " (" + mpv5.db.objects.User.getCurrentUser() + ")");
                Export.print(this);
            }
        } else {
            Export.print(this);
        }
    }

    private void preview() {
        PreviewPanel pr;
        if (dataOwner != null && dataOwner.isExisting()) {
            if (TemplateHandler.isLoaded(Long.valueOf(dataOwner.templateGroupIds()), dataOwner.__getInttype())) {
                pr = new PreviewPanel();
                pr.setDataOwner(dataOwner);
                new Job(Export.createFile(TemplateHandler.loadTemplate(dataOwner.templateGroupIds(), dataOwner.__getInttype()), dataOwner), pr).execute();
            } else {
                Popup.notice(Messages.NO_TEMPLATE_LOADED + " (" + mpv5.db.objects.User.getCurrentUser() + ")");
            }
        }
    }

    private void preloadTemplates() {
        Runnable runnable = new Runnable() {

            @Override
            public void run() {
                TemplateHandler.loadTemplateFor(button_preview, dataOwner.templateGroupIds(), dataOwner.__getInttype());
                TemplateHandler.loadTemplateFor(button_deliverynote, dataOwner.templateGroupIds(), Constants.TYPE_DELIVERY_NOTE);
                TemplateHandler.loadTemplateFor(new JComponent[]{button_orderconf, checkb_pront_oc}, dataOwner.templateGroupIds(), Constants.TYPE_ORDER_CONFIRMATION);
                TemplateHandler.loadTemplateFor(button_reminders, dataOwner.templateGroupIds(), Constants.TYPE_REMINDER);

                if (TemplateHandler.isLoaded(Long.valueOf(dataOwner.templateGroupIds()), dataOwner.__getInttype())) {
                    button_preview.setText(Messages.ACTION_PREVIEW.getValue());
                } else {
                    button_preview.setText(Messages.OO_NO_TEMPLATE.getValue());
                }
            }
        };
        new Thread(runnable).start();
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

    private void properties() {
        final MPTableModel m = new MPTableModel(ValueProperty.getProperties(dataOwner));
        final MPTableModel mold = m.clone();

        if (m.getDataVector().isEmpty()) {
            proptable.setModel(new MPTableModel(
                    Arrays.asList(new ValueProperty[]{new ValueProperty("", "", dataOwner)})));
        } else {
            proptable.setModel(m);
        }

        m.addTableModelListener(new TableModelListener() {

            @Override
            public void tableChanged(TableModelEvent e) {
                if (dataOwner.isExisting()) {
                    if (e.getColumn() == 0 && e.getType() == TableModelEvent.DELETE) {
                        ValueProperty.deleteProperty(dataOwner, String.valueOf(mold.getData()[e.getLastRow()][0]));
                        m.removeTableModelListener(this);
                        properties();
                    } else if (e.getColumn() == 1 && m.getValueAt(e.getLastRow(), 0) != null && String.valueOf(m.getValueAt(e.getLastRow(), 0)).length() > 0) {
                        ValueProperty.addOrUpdateProperty(String.valueOf(m.getData()[e.getLastRow()][0]).replaceAll("[^\\w]", ""), String.valueOf(m.getData()[e.getLastRow()][1]), dataOwner);
                        m.removeTableModelListener(this);
                        properties();
                    }
                }
            }
        });
    }

    private void toOrder() {

        dataOwner.getPanelData(this);
        dataOwner.setIntstatus(Item.STATUS_FINISHED);
        dataOwner.save();

        Item i2 = (Item) dataOwner.clone(Context.getOrder());
        i2.setInttype(Item.TYPE_ORDER);
        i2.setIDS(-1);
        i2.defineFormatHandler(new FormatHandler(i2));
        i2.save();
        if (itemtable.getCellEditor() != null) {
            try {
                itemtable.getCellEditor().stopCellEditing();
            } catch (Exception e) {
            }
        }
        SubItem.saveModel(i2, (MPTableModel) itemtable.getModel(), true, true);
        setDataOwner(i2, true);
        Popup.notice(i2 + Messages.INSERTED.getValue());
    }

    private void toInvoice(int itemType) {
        dataOwner.getPanelData(this);
        dataOwner.setIntstatus(Item.STATUS_FINISHED);
        dataOwner.save();
        ArrayList<ActivityList> data;
        Object[] row;
        Item i2;
        switch (itemType) {
            case Item.TYPE_INVOICE:
                i2 = (Item) dataOwner.clone(Context.getInvoice());
                break;
            case Item.TYPE_PART_PAYMENT:
                i2 = (Item) dataOwner.clone(Context.getPartPayment());
                break;
            case Item.TYPE_DEPOSIT:
                i2 = (Item) dataOwner.clone(Context.getDeposit());
                break;
            case Item.TYPE_CREDIT:
                i2 = (Item) dataOwner.clone(Context.getCredit());
                break;
            default:
                return;
        }
        i2.setIntstatus(Item.STATUS_QUEUED);
        i2.setInttype(itemType);
        i2.setIDS(-1);
        i2.defineFormatHandler(new FormatHandler(i2));
        boolean test = i2.save();
        if (itemtable.getCellEditor() != null) {
            try {
                itemtable.getCellEditor().stopCellEditing();
            } catch (Exception e) {
            }
        }
        try {
            data = DatabaseObject.getObjects(Context.getActivityList(), new QueryCriteria("orderids", dataOwner.__getIDS()));
            if (Popup.Y_N_dialog(Messages.ActivityList_Existing.toString())) {
                MPTableModel model = (MPTableModel) itemtable.getModel();
                Iterator<ActivityList> it = data.iterator();
                while (it.hasNext()) {
                    row = it.next().getDataForInvoice();
                    row[1] = model.getRowCount();
                    model.insertRow(model.getRowCount(), row);
                }
            }
        } catch (NodataFoundException ex) {
            Log.Debug(this, ex.getMessage());
        }
        if (itemType == Item.TYPE_INVOICE
                && (dataOwner.__getInttype() == Item.TYPE_DEPOSIT
                || dataOwner.__getInttype() == Item.TYPE_PART_PAYMENT)) {
            SubItem.saveModelMinus(i2, (MPTableModel) itemtable.getModel(), true, true, dataOwner);
        } else {
            SubItem.saveModel(i2, (MPTableModel) itemtable.getModel(), true, true);
        }

        if (itemType == Item.TYPE_INVOICE && dataOwner.__getRefOrderIDS() != null) {
            ArrayList<Item> data2 = null;
            int orderIDS = 0;
            if (dataOwner.__getInttype() == Item.TYPE_PART_PAYMENT
                    || dataOwner.__getInttype() == Item.TYPE_DEPOSIT) {
                orderIDS = dataOwner.__getRefOrderIDS();
            } else if (dataOwner.__getInttype() == Item.TYPE_ORDER) {
                orderIDS = dataOwner.__getIDS();
            }
            ArrayList<Item> d1 = null;
            try {
                d1 = DatabaseObject.getObjects(Context.getDeposit(), new QueryCriteria("reforderids", orderIDS));
            } catch (NodataFoundException ex) {
                Log.Debug(this, ex.getMessage());
            }
            ArrayList<Item> d2 = null;
            try {
                d2 = DatabaseObject.getObjects(Context.getPartPayment(), new QueryCriteria("reforderids", orderIDS));
            } catch (NodataFoundException ex) {
                Log.Debug(this, ex.getMessage());
            }

            if (d1 != null && d2 != null) {
                data2 = ArrayUtilities.merge(d1, d2);
            } else if (d1 != null) {
                data2 = d1;
            } else if (d2 != null) {
                data2 = d2;
            }
            if (data2 != null && data2.size() > 1 && Popup.Y_N_dialog(Messages.PartPayment_Existing.toString())) {
                MPTableModel model = (MPTableModel) itemtable.getModel();
                Iterator<Item> it = data2.iterator();
                while (it.hasNext()) {
                    Item i = it.next();
                    if (Objects.equals(i.__getIDS(), dataOwner.__getIDS())) {
                        continue;
                    }
                    SubItem.saveModelMinus(i2, SubItem.toModel(i.getSubitems()), true, true, i);
                }
            }
        }
        setDataOwner(i2, true);
        Popup.notice(i2 + Messages.INSERTED.getValue());
    }

    private class alignRightToolbar extends JToolBar {

        private static final long serialVersionUID = 1L;

        public alignRightToolbar() {
            add(Box.createHorizontalGlue());
        }
    }
}
