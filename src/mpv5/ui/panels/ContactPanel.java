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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.File;
import java.net.URI;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPopupMenu;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextPane;
import javax.swing.JViewport;
import javax.swing.RowFilter;
import javax.swing.SwingUtilities;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import mpv5.db.common.*;
import mpv5.globals.Headers;
import mpv5.globals.Messages;
import mpv5.db.objects.Address;
import mpv5.db.objects.Contact;
import mpv5.db.objects.Favourite;
import mpv5.db.objects.Group;
import mpv5.db.objects.Item;
import mpv5.db.objects.Product;
import mpv5.db.objects.Schedule;
import mpv5.logging.Log;
import mpv5.i18n.LanguageManager;
import mpv5.ui.dialogs.DialogForFile;
import mpv5.ui.dialogs.Popup;
import mpv5.ui.popups.FileTablePopUp;
import mpv5.ui.toolbars.DataPanelTB;
import mpv5.db.objects.User;
import mpv5.db.objects.ValueProperty;
import mpv5.ui.dialogs.ScheduleDayEvents;
import mpv5.ui.misc.MPTable;
import mpv5.ui.misc.TableViewPersistenceHandler;

import mpv5.ui.popups.DOTablePopUp;
import mpv5.utils.date.DateConverter;
import mpv5.utils.export.Export;
import mpv5.utils.files.FileDirectoryHandler;
import mpv5.utils.models.MPComboBoxModelItem;
import mpv5.utils.models.MPTableModel;
import mpv5.utils.tables.TableFormat;
import mpv5.utils.ui.TextFieldUtils;

/**
 *
 *
 */
public class ContactPanel extends javax.swing.JPanel implements DataPanel {

    private static final long serialVersionUID = 1L;
    public static final int COMPANY = 4;
    private Contact dataOwner;
    private final DataPanelTB tb;
    private final SearchPanel sp;
    private Integer dataTableContent = null;
    private final JPopupMenu itemTablePopup;
    private final java.util.ResourceBundle bundle = mpv5.i18n.LanguageManager.getBundle();
//    private String old_cnumber = "";
    private Context c;

    /**
     * Creates new form ContactPanel
     *
     * @param context
     */
    public ContactPanel(Context context) {
        initComponents();
        setName("contactpanel");
        sp = new SearchPanel(context, this);
        tb = new mpv5.ui.toolbars.DataPanelTB(this);
        toolbarpane.add(tb, BorderLayout.CENTER);
        dataOwner = new Contact();
        dataOwner.setCountry(mpv5.db.objects.User.getCurrentUser().__getDefcountry());

        dateadded.setText(DateConverter.getTodayDefDate());
        addedby.setText(mpv5.db.objects.User.getCurrentUser().getName());
        groupnameselect.setSearchEnabled(true);
        groupnameselect.setContext(Context.getGroup());
        companyselect.setSearchEnabled(true);
        companyselect.setContext(Context.getContactsCompanies());
        companyselect.getComboBox().setEditable(true);

        number.setSearchOnEnterEnabled(true);
        number.setParent(this);
        number.setSearchField("cnumber");
        number.setContext(Context.getContact());

        cname.setSearchOnEnterEnabled(true);
        cname.setParent(this);
        cname.setSearchField("cname");
        cname.setContext(Context.getContact());
        countryselect.setModel(LanguageManager.getCountriesAsComboBoxModel());
        refresh();

        itemTablePopup = DOTablePopUp.addDefaultPopupMenu(dataTable, Context.getInvoice(), false);

        if (context.equals(Context.getSupplier()) || context.equals(Context.getManufacturer())) {
            company.setSelected(true);
        }
        ((MPTable) dataTable).setPersistanceHandler(new TableViewPersistenceHandler((MPTable) dataTable, this));
        ((MPTable) proptable).setPersistanceHandler(new TableViewPersistenceHandler((MPTable) proptable, this));
        setupFilter();
        button_bill.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                toInvoice(Item.TYPE_INVOICE);
            }
        });
        button_bill.add(Messages.NEW_DEPOSIT.toString(), new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                toInvoice(Item.TYPE_DEPOSIT);
            }
        });
        button_bill.add(Messages.NEW_PART_PAYMENT.toString(), new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                toInvoice(Item.TYPE_PART_PAYMENT);
            }
        });
    }

    private void toInvoice(int itemType) {
        if (dataOwner.isExisting()) {
            Item i;
            switch (itemType) {
                case Item.TYPE_INVOICE:
                    i = (Item) DatabaseObject.getObject(Context.getInvoice());
                    i.setCname(Messages.NEW_BILL.getValue());
                    break;
                case Item.TYPE_PART_PAYMENT:
                    i = (Item) DatabaseObject.getObject(Context.getPartPayment());
                    i.setCname(Messages.NEW_PART_PAYMENT.getValue());
                    break;
                case Item.TYPE_DEPOSIT:
                    i = (Item) DatabaseObject.getObject(Context.getDeposit());
                    i.setCname(Messages.NEW_DEPOSIT.getValue());
                    break;
                case Item.TYPE_CREDIT:
                    i = (Item) DatabaseObject.getObject(Context.getCredit());
                    i.setCname(Messages.NEW_CREDIT.getValue());
                    break;
                default:
                    return;
            }
            i.setInttype(itemType);
            i.setContactsids(dataOwner.__getIDS());
            i.setDateadded(new Date());
            i.setGroupsids(dataOwner.__getGroupsids());
            mpv5.YabsViewProxy.instance().getIdentifierView().addTab(i);
        }
    }

    @Override
    public DatabaseObject getDataOwner() {
        return dataOwner;
    }

    @Override
    public void setDataOwner(DatabaseObject object, boolean populate) {
        dataOwner = (Contact) object;
        if (populate) {
            dataOwner.setPanelData(this);
            this.exposeData();

            if (object.isExisting() && populate) {
                setTitle(object.__getCname());
            }

            prinitingComboBox1.init(rightpane);

            tb.setFavourite(Favourite.isFavourite(object));
            tb.setEditable(!object.isReadOnly());
            tb.setExportButtonsEnabled(Context.getTemplateableContexts().contains(dataOwner.getContext()));

            addAddresses();
            dataTable.setModel(new MPTableModel());

            if (object.isReadOnly()) {
                Popup.notice(Messages.LOCKED_BY);
            }

            if (dataOwner.isExisting()) {
                if (dataOwner.__getIscustomer()) {
                    button_billsActionPerformed(null);
                } else if (dataOwner.__getIsmanufacturer() || dataOwner.__getIssupplier()) {
                    button_productsActionPerformed(null);
                }
            }

            isCustomer(dataOwner.__getIscustomer());
            isManufacturer(dataOwner.__getIsmanufacturer());
            isSupplier(dataOwner.__getIssupplier());

            properties();
        }
    }

    @Override
    public void showRequiredFields() {
        jTabbedPane1.setSelectedIndex(1);
        TextFieldUtils.blinkerRed(cname);
        cname.requestFocus();
    }

    private void addAddresses() {
        String str = addresspanel.getTitleAt(0);
        Component n = addresspanel.getComponent(0);
        addresspanel.removeAll();
        addresspanel.add(n);
        addresspanel.setTitleAt(0, str);
        try {
            List data = DatabaseObject.getReferencedObjects(dataOwner, Context.getAddress());

            for (int i = 0; i < data.size(); i++) {
                Address databaseObject = (Address) data.get(i);
                AddressPanel p = new AddressPanel();
                p.setName(databaseObject.__getDepartment() + " - " + databaseObject.__getCname());
                p.setDataOwner(databaseObject, true);
                p.setDataParent(dataOwner);
                addresspanel.add(p);
            }
        } catch (NodataFoundException ex) {
            Log.Debug(this, ex);
        }
    }

    private void addFile() {
        DialogForFile d = new DialogForFile(DialogForFile.FILES_ONLY);
        if (d.chooseFile()) {
            String s = Popup.Enter_Value(Messages.ENTER_A_DESCRIPTION);
            if (s != null) {
                QueryHandler.instanceOf().clone(Context.getFiles(), this).insertFile(d.getFile(), dataOwner, QueryCriteria.getSaveStringFor(s));
            }
        }
    }

    private void deleteFile() {
        if (dataOwner.isExisting()) {
            try {
                DatabaseObject.getObject(Context.getFilesToContacts(), "filename", (filetableN.getModel().getValueAt(filetableN.convertRowIndexToModel(filetableN.getSelectedRow()), 0).toString())).delete();
            } catch (Exception e) {
                Log.Debug(this, e.getMessage());
            }
            fillFiles();

        }
    }

    private void fileTableClicked(MouseEvent evt) {
        if (evt.getClickCount() > 1) {
            FileDirectoryHandler.open(QueryHandler.instanceOf().clone(Context.getFiles()).
                    retrieveFile(filetableN.getModel().getValueAt(filetableN.convertRowIndexToModel(filetableN.convertRowIndexToModel(filetableN.getSelectedRow())), 0).
                            toString(), new File(FileDirectoryHandler.getTempDir() + filetableN.getModel().
                                    getValueAt(filetableN.convertRowIndexToModel(filetableN.getSelectedRow()), 1).toString())));
        } else if (evt.getClickCount() == 1 && evt.getButton() == MouseEvent.BUTTON3) {

            JTable source = (JTable) evt.getSource();
            int row = source.rowAtPoint(evt.getPoint());
            int column = source.columnAtPoint(evt.getPoint());

            if (!source.isRowSelected(row)) {
                source.changeSelection(row, column, false, false);
            }

            FileTablePopUp.instanceOf(filetableN).show(source, evt.getX(), evt.getY());
        }
    }

    private void itemTableClicked(MouseEvent evt) {
        if (evt.getButton() == MouseEvent.BUTTON1 && evt.getClickCount() > 1) {
            try {
                mpv5.YabsViewProxy.instance().getIdentifierView().addTab(DatabaseObject.getObject(c, Integer.valueOf(dataTable.getModel().getValueAt(dataTable.convertRowIndexToModel(dataTable.getSelectedRow()), 0).toString())));
            } catch (NodataFoundException ex) {
                Log.Debug(ex);
            }
        } else if (evt.getButton() == MouseEvent.BUTTON3) {
            itemTablePopup.show(dataTable, evt.getX(), evt.getY());
        }
    }

    private void productTableClicked(MouseEvent evt) {
        if (evt.getClickCount() > 1) {
            try {
                mpv5.YabsViewProxy.instance().getIdentifierView().addTab(DatabaseObject.getObject(Context.getProduct(), Integer.valueOf(dataTable.getModel().getValueAt(dataTable.convertRowIndexToModel(dataTable.getSelectedRow()), 0).toString())));
            } catch (NodataFoundException ex) {
                Log.Debug(ex);
            }
        }
    }

    private void activityTableClicked(MouseEvent evt) {
        if (evt.getClickCount() > 1) {
            try {
                mpv5.YabsViewProxy.instance().getIdentifierView().addTab(DatabaseObject.getObject(Context.getActivityList(), Integer.valueOf(dataTable.getModel().getValueAt(dataTable.convertRowIndexToModel(dataTable.getSelectedRow()), 0).toString())));
            } catch (NodataFoundException ex) {
                Log.Debug(ex);
            }
        }
    }

    private void fillFiles() {
        Context cf = Context.getFilesToContacts();
        cf.addReference(Context.getFiles().getDbIdentity(), "cname", "filename");
        Object[][] data = new DatabaseSearch(cf).getValuesFor(Context.DETAILS_FILES_TO_CONTACTS, "contactsids", dataOwner.__getIDS());

        filetableN.setModel(new MPTableModel(data, Headers.FILE_REFERENCES.getValue()));
        TableFormat.stripFirstColumn(filetableN);
    }

    /**
     * This method is called from within the constructor to
     * initialize the form. WARNING: Do NOT modify this
     * code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        leftpane = new javax.swing.JPanel();
        rightpane = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        number = new mpv5.ui.beans.LabeledTextField();
        jToolBar2 = new javax.swing.JToolBar();
        customer = new javax.swing.JCheckBox();
        supplier = new javax.swing.JCheckBox();
        manufacturer = new javax.swing.JCheckBox();
        company = new javax.swing.JCheckBox();
        addedby = new javax.swing.JLabel();
        dateadded = new javax.swing.JLabel();
        groupnameselect = new mpv5.ui.beans.LabeledCombobox();
        taxnumber = new mpv5.ui.beans.LabeledTextField();
        companyselect = new mpv5.ui.beans.LabeledCombobox();
        payterm = new mpv5.ui.beans.LabeledTextField();
        jPanel9 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        dataTable = new  mpv5.ui.misc.MPTable(this) {
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
        jToolBar3 = new javax.swing.JToolBar();
        button_orders = new javax.swing.JButton();
        button_bills = new javax.swing.JButton();
        button_offers = new javax.swing.JButton();
        button_activitylists = new javax.swing.JButton();
        button_products = new javax.swing.JButton();
        filterme = new mpv5.ui.beans.LabeledTextField();
        jToolBar1 = new javax.swing.JToolBar();
        button_offer = new javax.swing.JButton();
        button_order = new javax.swing.JButton();
        button_bill = new mpv5.ui.beans.DropDownButton();
        button_credit = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JToolBar.Separator();
        button_product = new javax.swing.JButton();
        jSeparator2 = new javax.swing.JToolBar.Separator();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jSeparator3 = new javax.swing.JToolBar.Separator();
        prinitingComboBox1 = new mpv5.ui.beans.PrinitingComboBox();
        jScrollPane4 = new javax.swing.JScrollPane();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel3 = new javax.swing.JPanel();
        mainphone = new mpv5.ui.beans.LabeledTextField();
        workphone = new mpv5.ui.beans.LabeledTextField();
        mailaddress = new mpv5.ui.beans.LabeledTextField();
        website = new mpv5.ui.beans.LabeledTextField();
        fax = new mpv5.ui.beans.LabeledTextField();
        jButton4 = new javax.swing.JButton();
        jButton5 = new javax.swing.JButton();
        mobilephone = new mpv5.ui.beans.LabeledTextField();
        addresspanel = new javax.swing.JTabbedPane();
        mainaddress = new javax.swing.JPanel();
        title = new mpv5.ui.beans.LabeledTextField();
        street = new mpv5.ui.beans.LabeledTextField();
        cname = new mpv5.ui.beans.LabeledTextField();
        prename = new mpv5.ui.beans.LabeledTextField();
        zip = new mpv5.ui.beans.LabeledTextField();
        male = new javax.swing.JRadioButton();
        female = new javax.swing.JRadioButton();
        department = new mpv5.ui.beans.LabeledTextField();
        jLabel5 = new javax.swing.JLabel();
        countryselect = new javax.swing.JComboBox();
        city = new mpv5.ui.beans.LabeledTextField();
        button_order1 = new javax.swing.JButton();
        jPanel5 = new javax.swing.JPanel();
        bankid = new mpv5.ui.beans.LabeledTextField();
        bankaccount = new mpv5.ui.beans.LabeledTextField();
        bankcurrency = new mpv5.ui.beans.LabeledTextField();
        bankname = new mpv5.ui.beans.LabeledTextField();
        bankcountry = new mpv5.ui.beans.LabeledTextField();
        jPanel4 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        class NoTabTextArea extends JTextPane {
            protected void processComponentKeyEvent( KeyEvent e ) {
                if ( e.getID() == KeyEvent.KEY_PRESSED &&
                    e.getKeyCode() == KeyEvent.VK_TAB ) {
                    e.consume();
                    if (e.isShiftDown()) {
                        transferFocusBackward();
                    } else {
                        transferFocus();
                    }
                } else {
                    super.processComponentKeyEvent( e );
                }
            }
        }
        notes = new NoTabTextArea();
        propPanel = new javax.swing.JPanel();
        jPanel6 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        proptable = new  mpv5.ui.misc.MPTable(this) {
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
        filetab = new javax.swing.JPanel();
        jScrollPane5 = new javax.swing.JScrollPane();
        filetableN = new  mpv5.ui.misc.MPTable(this) {
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
        addfile1 = new javax.swing.JButton();
        removefile1 = new javax.swing.JButton();
        jButton6 = new javax.swing.JButton();
        toolbarpane = new javax.swing.JPanel();

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("mpv5/resources/languages/Panels"); // NOI18N
        setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("ContactPanel.border.title"))); // NOI18N
        setName("Form"); // NOI18N
        setLayout(new java.awt.BorderLayout());

        leftpane.setName("leftpane"); // NOI18N
        leftpane.setLayout(new java.awt.BorderLayout());
        add(leftpane, java.awt.BorderLayout.WEST);

        rightpane.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("ContactPanel.rightpane.border.title"))); // NOI18N
        rightpane.setName("rightpane"); // NOI18N

        jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel1.setName("jPanel1"); // NOI18N

        jLabel1.setFont(jLabel1.getFont());
        jLabel1.setText(bundle.getString("ContactPanel.jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        number.set_Label(bundle.getString("ContactPanel.number._Label")); // NOI18N
        number.setFocusable(false);
        number.setFont(number.getFont());
        number.setMaximumSize(new java.awt.Dimension(150, 23));
        number.setMinimumSize(new java.awt.Dimension(150, 23));
        number.setName("number"); // NOI18N
        number.setPreferredSize(new java.awt.Dimension(300, 23));

        jToolBar2.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        jToolBar2.setFloatable(false);
        jToolBar2.setRollover(true);
        jToolBar2.setName("jToolBar2"); // NOI18N
        jToolBar2.setOpaque(false);

        customer.setText(bundle.getString("ContactPanel.customer.text")); // NOI18N
        customer.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        customer.setMaximumSize(new java.awt.Dimension(150, 22));
        customer.setMinimumSize(new java.awt.Dimension(100, 22));
        customer.setName("customer"); // NOI18N
        customer.setPreferredSize(new java.awt.Dimension(140, 22));
        customer.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                customerItemStateChanged(evt);
            }
        });
        jToolBar2.add(customer);

        supplier.setText(bundle.getString("ContactPanel.supplier.text")); // NOI18N
        supplier.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        supplier.setMaximumSize(new java.awt.Dimension(150, 22));
        supplier.setMinimumSize(new java.awt.Dimension(100, 22));
        supplier.setName("supplier"); // NOI18N
        supplier.setPreferredSize(new java.awt.Dimension(140, 22));
        supplier.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                supplierItemStateChanged(evt);
            }
        });
        jToolBar2.add(supplier);

        manufacturer.setText(bundle.getString("ContactPanel.manufacturer.text")); // NOI18N
        manufacturer.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        manufacturer.setMaximumSize(new java.awt.Dimension(150, 22));
        manufacturer.setMinimumSize(new java.awt.Dimension(100, 22));
        manufacturer.setName("manufacturer"); // NOI18N
        manufacturer.setPreferredSize(new java.awt.Dimension(140, 22));
        manufacturer.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                manufacturerItemStateChanged(evt);
            }
        });
        jToolBar2.add(manufacturer);

        company.setText(bundle.getString("ContactPanel.company.text")); // NOI18N
        company.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        company.setMaximumSize(new java.awt.Dimension(250, 22));
        company.setMinimumSize(new java.awt.Dimension(100, 22));
        company.setName("company"); // NOI18N
        company.setPreferredSize(new java.awt.Dimension(190, 22));
        company.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                companyItemStateChanged(evt);
            }
        });
        jToolBar2.add(company);

        addedby.setFont(addedby.getFont());
        addedby.setText(bundle.getString("ContactPanel.addedby.text")); // NOI18N
        addedby.setToolTipText(bundle.getString("ContactPanel.addedby.toolTipText")); // NOI18N
        addedby.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        addedby.setEnabled(false);
        addedby.setMaximumSize(new java.awt.Dimension(120, 21));
        addedby.setMinimumSize(new java.awt.Dimension(120, 21));
        addedby.setName("addedby"); // NOI18N
        addedby.setPreferredSize(new java.awt.Dimension(120, 21));
        jToolBar2.add(addedby);

        dateadded.setFont(dateadded.getFont());
        dateadded.setText(bundle.getString("ContactPanel.dateadded.text")); // NOI18N
        dateadded.setToolTipText(bundle.getString("ContactPanel.dateadded.toolTipText")); // NOI18N
        dateadded.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        dateadded.setEnabled(false);
        dateadded.setMaximumSize(new java.awt.Dimension(120, 21));
        dateadded.setMinimumSize(new java.awt.Dimension(120, 21));
        dateadded.setName("dateadded"); // NOI18N
        dateadded.setPreferredSize(new java.awt.Dimension(120, 21));
        jToolBar2.add(dateadded);

        groupnameselect.set_Label(bundle.getString("ContactPanel.groupnameselect._Label")); // NOI18N
        groupnameselect.setMaximumSize(new java.awt.Dimension(150, 23));
        groupnameselect.setMinimumSize(new java.awt.Dimension(150, 23));
        groupnameselect.setName("groupnameselect"); // NOI18N
        groupnameselect.setPreferredSize(new java.awt.Dimension(300, 23));

        taxnumber.set_Label(bundle.getString("ContactPanel.taxnumber._Label")); // NOI18N
        taxnumber.setFont(taxnumber.getFont().deriveFont(taxnumber.getFont().getStyle() | java.awt.Font.BOLD));
        taxnumber.setMaximumSize(new java.awt.Dimension(150, 23));
        taxnumber.setMinimumSize(new java.awt.Dimension(150, 23));
        taxnumber.setName("taxnumber"); // NOI18N
        taxnumber.setPreferredSize(new java.awt.Dimension(300, 23));

        companyselect.set_Label(bundle.getString("ContactPanel.companyselect._Label")); // NOI18N
        companyselect.setMaximumSize(new java.awt.Dimension(150, 23));
        companyselect.setMinimumSize(new java.awt.Dimension(150, 23));
        companyselect.setName("companyselect"); // NOI18N
        companyselect.setPreferredSize(new java.awt.Dimension(300, 23));

        payterm.set_Label(bundle.getString("ContactPanel.payterm._Label")); // NOI18N
        payterm.set_ValueClass(Integer.class);
        payterm.setFont(payterm.getFont().deriveFont(payterm.getFont().getStyle() | java.awt.Font.BOLD));
        payterm.setMaximumSize(new java.awt.Dimension(150, 23));
        payterm.setMinimumSize(new java.awt.Dimension(150, 23));
        payterm.setName("payterm"); // NOI18N
        payterm.setPreferredSize(new java.awt.Dimension(300, 23));

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jToolBar2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(groupnameselect, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(companyselect, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(number, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(taxnumber, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(99, 99, 99)
                        .addComponent(payterm, javax.swing.GroupLayout.PREFERRED_SIZE, 241, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(34, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jToolBar2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(groupnameselect, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(companyselect, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(number, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(taxnumber, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(payterm, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );

        jPanel9.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel9.setName("jPanel9"); // NOI18N

        jScrollPane2.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        jScrollPane2.setName("jScrollPane2"); // NOI18N

        dataTable.setAutoCreateRowSorter(true);
        dataTable.setDragEnabled(true);
        dataTable.setName("dataTable"); // NOI18N
        dataTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                dataTableMouseClicked(evt);
            }
        });
        jScrollPane2.setViewportView(dataTable);

        jToolBar3.setFloatable(false);
        jToolBar3.setRollover(true);
        jToolBar3.setName("jToolBar3"); // NOI18N

        button_orders.setText(bundle.getString("ContactPanel.button_orders.text")); // NOI18N
        button_orders.setEnabled(false);
        button_orders.setFocusable(false);
        button_orders.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        button_orders.setMaximumSize(new java.awt.Dimension(120, 29));
        button_orders.setName("button_orders"); // NOI18N
        button_orders.setPreferredSize(new java.awt.Dimension(100, 29));
        button_orders.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        button_orders.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                button_ordersActionPerformed(evt);
            }
        });
        jToolBar3.add(button_orders);

        button_bills.setText(bundle.getString("ContactPanel.button_bills.text")); // NOI18N
        button_bills.setEnabled(false);
        button_bills.setFocusable(false);
        button_bills.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        button_bills.setMaximumSize(new java.awt.Dimension(120, 29));
        button_bills.setMinimumSize(new java.awt.Dimension(61, 29));
        button_bills.setName("button_bills"); // NOI18N
        button_bills.setPreferredSize(new java.awt.Dimension(100, 29));
        button_bills.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        button_bills.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                button_billsActionPerformed(evt);
            }
        });
        jToolBar3.add(button_bills);

        button_offers.setText(bundle.getString("ContactPanel.button_offers.text")); // NOI18N
        button_offers.setEnabled(false);
        button_offers.setFocusable(false);
        button_offers.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        button_offers.setMaximumSize(new java.awt.Dimension(120, 29));
        button_offers.setMinimumSize(new java.awt.Dimension(61, 29));
        button_offers.setName("button_offers"); // NOI18N
        button_offers.setPreferredSize(new java.awt.Dimension(100, 29));
        button_offers.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        button_offers.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                button_offersActionPerformed(evt);
            }
        });
        jToolBar3.add(button_offers);

        button_activitylists.setText(bundle.getString("ContactPanel.button_activitylists.text")); // NOI18N
        button_activitylists.setEnabled(false);
        button_activitylists.setFocusable(false);
        button_activitylists.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        button_activitylists.setMaximumSize(new java.awt.Dimension(120, 29));
        button_activitylists.setMinimumSize(new java.awt.Dimension(61, 29));
        button_activitylists.setName("button_activitylists"); // NOI18N
        button_activitylists.setPreferredSize(new java.awt.Dimension(100, 29));
        button_activitylists.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        button_activitylists.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                button_activitylistsActionPerformed(evt);
            }
        });
        jToolBar3.add(button_activitylists);

        button_products.setText(bundle.getString("ContactPanel.button_products.text")); // NOI18N
        button_products.setEnabled(false);
        button_products.setFocusable(false);
        button_products.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        button_products.setMaximumSize(new java.awt.Dimension(120, 29));
        button_products.setMinimumSize(new java.awt.Dimension(61, 29));
        button_products.setName("button_products"); // NOI18N
        button_products.setPreferredSize(new java.awt.Dimension(100, 29));
        button_products.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        button_products.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                button_productsActionPerformed(evt);
            }
        });
        jToolBar3.add(button_products);

        filterme.set_Label(bundle.getString("ContactPanel.filterme._Label")); // NOI18N
        filterme.setFocusable(false);
        filterme.setFont(filterme.getFont());
        filterme.setMaximumSize(new java.awt.Dimension(150, 24));
        filterme.setMinimumSize(new java.awt.Dimension(250, 21));
        filterme.setName("filterme"); // NOI18N
        filterme.setPreferredSize(new java.awt.Dimension(250, 24));
        jToolBar3.add(filterme);

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 990, Short.MAX_VALUE)
            .addComponent(jToolBar3, javax.swing.GroupLayout.DEFAULT_SIZE, 990, Short.MAX_VALUE)
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addComponent(jToolBar3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 45, Short.MAX_VALUE))
        );

        jToolBar1.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jToolBar1.setFloatable(false);
        jToolBar1.setRollover(true);
        jToolBar1.setMaximumSize(new java.awt.Dimension(2222, 28));
        jToolBar1.setName("jToolBar1"); // NOI18N
        jToolBar1.setPreferredSize(new java.awt.Dimension(824, 28));

        button_offer.setText(bundle.getString("ContactPanel.button_offer.text")); // NOI18N
        button_offer.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 204)));
        button_offer.setFocusable(false);
        button_offer.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        button_offer.setIconTextGap(5);
        button_offer.setMaximumSize(new java.awt.Dimension(333, 20));
        button_offer.setMinimumSize(new java.awt.Dimension(80, 20));
        button_offer.setName("button_offer"); // NOI18N
        button_offer.setPreferredSize(new java.awt.Dimension(120, 20));
        button_offer.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        button_offer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                button_offerActionPerformed(evt);
            }
        });
        jToolBar1.add(button_offer);

        button_order.setText(bundle.getString("ContactPanel.button_order.text")); // NOI18N
        button_order.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 204)));
        button_order.setFocusable(false);
        button_order.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        button_order.setIconTextGap(5);
        button_order.setMaximumSize(new java.awt.Dimension(333, 20));
        button_order.setMinimumSize(new java.awt.Dimension(80, 20));
        button_order.setName("button_order"); // NOI18N
        button_order.setPreferredSize(new java.awt.Dimension(120, 20));
        button_order.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        button_order.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                button_orderActionPerformed(evt);
            }
        });
        jToolBar1.add(button_order);

        button_bill.setToolTipText(bundle.getString("ContactPanel.button_bill.toolTipText")); // NOI18N
        button_bill.set_Label(bundle.getString("ContactPanel.button_bill._Label")); // NOI18N
        button_bill.setMaximumSize(new java.awt.Dimension(120, 20));
        button_bill.setMinimumSize(new java.awt.Dimension(120, 20));
        button_bill.setName("button_bill"); // NOI18N
        button_bill.setPreferredSize(new java.awt.Dimension(120, 20));
        jToolBar1.add(button_bill);

        button_credit.setText(bundle.getString("ContactPanel.button_credit.text")); // NOI18N
        button_credit.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 204)));
        button_credit.setFocusable(false);
        button_credit.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        button_credit.setIconTextGap(5);
        button_credit.setMaximumSize(new java.awt.Dimension(333, 20));
        button_credit.setMinimumSize(new java.awt.Dimension(80, 20));
        button_credit.setName("button_credit"); // NOI18N
        button_credit.setPreferredSize(new java.awt.Dimension(120, 20));
        button_credit.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        button_credit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                button_creditActionPerformed(evt);
            }
        });
        jToolBar1.add(button_credit);

        jSeparator1.setName("jSeparator1"); // NOI18N
        jToolBar1.add(jSeparator1);

        button_product.setText(bundle.getString("ContactPanel.button_product.text")); // NOI18N
        button_product.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 204)));
        button_product.setFocusable(false);
        button_product.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        button_product.setIconTextGap(5);
        button_product.setMaximumSize(new java.awt.Dimension(333, 20));
        button_product.setMinimumSize(new java.awt.Dimension(80, 20));
        button_product.setName("button_product"); // NOI18N
        button_product.setPreferredSize(new java.awt.Dimension(120, 20));
        button_product.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        button_product.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                button_productActionPerformed(evt);
            }
        });
        jToolBar1.add(button_product);

        jSeparator2.setName("jSeparator2"); // NOI18N
        jToolBar1.add(jSeparator2);

        jButton2.setText(bundle.getString("ContactPanel.jButton2.text")); // NOI18N
        jButton2.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 204)));
        jButton2.setFocusable(false);
        jButton2.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton2.setIconTextGap(5);
        jButton2.setMaximumSize(new java.awt.Dimension(333, 20));
        jButton2.setMinimumSize(new java.awt.Dimension(80, 20));
        jButton2.setName("jButton2"); // NOI18N
        jButton2.setPreferredSize(new java.awt.Dimension(120, 20));
        jButton2.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });
        jToolBar1.add(jButton2);

        jButton3.setText(bundle.getString("ContactPanel.jButton3.text")); // NOI18N
        jButton3.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 204)));
        jButton3.setFocusable(false);
        jButton3.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton3.setIconTextGap(5);
        jButton3.setMaximumSize(new java.awt.Dimension(333, 20));
        jButton3.setMinimumSize(new java.awt.Dimension(80, 20));
        jButton3.setName("jButton3"); // NOI18N
        jButton3.setPreferredSize(new java.awt.Dimension(120, 20));
        jButton3.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });
        jToolBar1.add(jButton3);

        jSeparator3.setName("jSeparator3"); // NOI18N
        jToolBar1.add(jSeparator3);

        prinitingComboBox1.setMaximumSize(new java.awt.Dimension(200, 20));
        prinitingComboBox1.setName("prinitingComboBox1"); // NOI18N
        jToolBar1.add(prinitingComboBox1);

        jScrollPane4.setName("jScrollPane4"); // NOI18N

        jTabbedPane1.setName("jTabbedPane1"); // NOI18N
        jTabbedPane1.setPreferredSize(new java.awt.Dimension(888, 150));

        jPanel3.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel3.setName("jPanel3"); // NOI18N

        mainphone.set_Label(bundle.getString("ContactPanel.phone._Label")); // NOI18N
        mainphone.setMaximumSize(new java.awt.Dimension(120, 21));
        mainphone.setMinimumSize(new java.awt.Dimension(120, 21));
        mainphone.setName("phone"); // NOI18N

        workphone.set_Label(bundle.getString("ContactPanel.[120, 21]._Label")); // NOI18N
        workphone.setMaximumSize(new java.awt.Dimension(120, 21));
        workphone.setMinimumSize(new java.awt.Dimension(120, 21));
        workphone.setName("[120, 21]"); // NOI18N
        workphone.setPreferredSize(new java.awt.Dimension(180, 23));

        mailaddress.set_Label(bundle.getString("ContactPanel.mailer._Label")); // NOI18N
        mailaddress.setMaximumSize(new java.awt.Dimension(120, 21));
        mailaddress.setMinimumSize(new java.awt.Dimension(120, 21));
        mailaddress.setName("mailer"); // NOI18N
        mailaddress.setPreferredSize(new java.awt.Dimension(180, 23));

        website.set_Label(bundle.getString("ContactPanel.web._Label")); // NOI18N
        website.setMaximumSize(new java.awt.Dimension(120, 21));
        website.setMinimumSize(new java.awt.Dimension(120, 21));
        website.setName("web"); // NOI18N
        website.setPreferredSize(new java.awt.Dimension(180, 23));

        fax.set_Label(bundle.getString("ContactPanel.fax._Label")); // NOI18N
        fax.setMaximumSize(new java.awt.Dimension(120, 21));
        fax.setMinimumSize(new java.awt.Dimension(120, 21));
        fax.setName("fax"); // NOI18N
        fax.setPreferredSize(new java.awt.Dimension(180, 23));

        jButton4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/mpv5/resources/images/16/info.png"))); // NOI18N
        jButton4.setText(bundle.getString("ContactPanel.jButton5.text")); // NOI18N
        jButton4.setName("gotoweb"); // NOI18N
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        jButton5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/mpv5/resources/images/16/info.png"))); // NOI18N
        jButton5.setText(bundle.getString("ContactPanel.sendamail.text")); // NOI18N
        jButton5.setName("sendamail"); // NOI18N
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton5ActionPerformed(evt);
            }
        });

        mobilephone.set_Label(bundle.getString("ContactPanel.mobile._Label")); // NOI18N
        mobilephone.setMaximumSize(new java.awt.Dimension(120, 21));
        mobilephone.setMinimumSize(new java.awt.Dimension(120, 21));
        mobilephone.setName("mobile"); // NOI18N
        mobilephone.setPreferredSize(new java.awt.Dimension(180, 23));

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(fax, javax.swing.GroupLayout.PREFERRED_SIZE, 333, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(mainphone, javax.swing.GroupLayout.PREFERRED_SIZE, 333, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(mobilephone, javax.swing.GroupLayout.PREFERRED_SIZE, 333, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(website, javax.swing.GroupLayout.PREFERRED_SIZE, 333, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(mailaddress, javax.swing.GroupLayout.PREFERRED_SIZE, 333, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(workphone, javax.swing.GroupLayout.PREFERRED_SIZE, 333, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jButton4, javax.swing.GroupLayout.PREFERRED_SIZE, 147, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton5, javax.swing.GroupLayout.PREFERRED_SIZE, 147, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(295, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(mailaddress, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(mainphone, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(website, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(fax, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(mobilephone, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(workphone, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(34, 34, 34)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButton4)
                    .addComponent(jButton5))
                .addContainerGap())
        );

        jTabbedPane1.addTab(bundle.getString("ContactPanel.jPanel3.TabConstraints.tabTitle"), jPanel3); // NOI18N

        addresspanel.setTabPlacement(javax.swing.JTabbedPane.RIGHT);
        addresspanel.setAutoscrolls(true);
        addresspanel.setFont(addresspanel.getFont());
        addresspanel.setName("addresspanel"); // NOI18N

        mainaddress.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        mainaddress.setName("mainaddress"); // NOI18N

        title.set_Label(bundle.getString("ContactPanel.title._Label")); // NOI18N
        title.setMaximumSize(new java.awt.Dimension(120, 24));
        title.setMinimumSize(new java.awt.Dimension(120, 21));
        title.setName("title"); // NOI18N
        title.setPreferredSize(new java.awt.Dimension(120, 24));

        street.set_Label(bundle.getString("ContactPanel.street._Label")); // NOI18N
        street.setMaximumSize(new java.awt.Dimension(120, 24));
        street.setMinimumSize(new java.awt.Dimension(120, 21));
        street.setName("street"); // NOI18N
        street.setPreferredSize(new java.awt.Dimension(120, 24));

        cname.set_Label(bundle.getString("ContactPanel.cname._Label")); // NOI18N
        cname.setMaximumSize(new java.awt.Dimension(120, 24));
        cname.setMinimumSize(new java.awt.Dimension(120, 21));
        cname.setName("cname"); // NOI18N
        cname.setPreferredSize(new java.awt.Dimension(120, 24));

        prename.set_Label(bundle.getString("ContactPanel.prename._Label")); // NOI18N
        prename.setMaximumSize(new java.awt.Dimension(120, 24));
        prename.setMinimumSize(new java.awt.Dimension(120, 21));
        prename.setName("prename"); // NOI18N
        prename.setPreferredSize(new java.awt.Dimension(120, 24));

        zip.set_Label(bundle.getString("ContactPanel.zip._Label")); // NOI18N
        zip.setMaximumSize(new java.awt.Dimension(120, 24));
        zip.setMinimumSize(new java.awt.Dimension(120, 21));
        zip.setName("zip"); // NOI18N
        zip.setPreferredSize(new java.awt.Dimension(120, 24));

        buttonGroup1.add(male);
        male.setFont(male.getFont().deriveFont(male.getFont().getStyle() & ~java.awt.Font.BOLD));
        male.setSelected(true);
        male.setText(bundle.getString("ContactPanel.male.text")); // NOI18N
        male.setName("male"); // NOI18N

        buttonGroup1.add(female);
        female.setFont(female.getFont().deriveFont(female.getFont().getStyle() & ~java.awt.Font.BOLD));
        female.setText(bundle.getString("ContactPanel.female.text")); // NOI18N
        female.setName("female"); // NOI18N

        department.set_Label(bundle.getString("ContactPanel.department._Label")); // NOI18N
        department.setFont(department.getFont().deriveFont(department.getFont().getStyle() | java.awt.Font.BOLD));
        department.setMaximumSize(new java.awt.Dimension(120, 24));
        department.setMinimumSize(new java.awt.Dimension(120, 21));
        department.setName("department"); // NOI18N
        department.setPreferredSize(new java.awt.Dimension(120, 24));

        jLabel5.setFont(jLabel5.getFont());
        jLabel5.setText(bundle.getString("ContactPanel.jLabel5.text")); // NOI18N
        jLabel5.setName("jLabel5"); // NOI18N

        countryselect.setMaximumSize(new java.awt.Dimension(120, 24));
        countryselect.setMinimumSize(new java.awt.Dimension(120, 21));
        countryselect.setName("countryselect"); // NOI18N
        countryselect.setPreferredSize(new java.awt.Dimension(120, 24));
        countryselect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                countryselectActionPerformed(evt);
            }
        });

        city.set_Label(bundle.getString("ContactPanel.city._Label")); // NOI18N
        city.setMaximumSize(new java.awt.Dimension(120, 24));
        city.setMinimumSize(new java.awt.Dimension(120, 21));
        city.setName("city"); // NOI18N
        city.setPreferredSize(new java.awt.Dimension(120, 24));

        button_order1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/mpv5/resources/images/16/add.png"))); // NOI18N
        button_order1.setText(bundle.getString("ContactPanel.button_order1.text")); // NOI18N
        button_order1.setFocusable(false);
        button_order1.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        button_order1.setName("button_order1"); // NOI18N
        button_order1.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        button_order1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                button_order1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout mainaddressLayout = new javax.swing.GroupLayout(mainaddress);
        mainaddress.setLayout(mainaddressLayout);
        mainaddressLayout.setHorizontalGroup(
            mainaddressLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainaddressLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(mainaddressLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(title, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 288, Short.MAX_VALUE)
                    .addComponent(street, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(department, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(mainaddressLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, mainaddressLayout.createSequentialGroup()
                        .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(countryselect, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(zip, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(prename, javax.swing.GroupLayout.DEFAULT_SIZE, 262, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(mainaddressLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(mainaddressLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addGroup(mainaddressLayout.createSequentialGroup()
                            .addComponent(male, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(female, javax.swing.GroupLayout.DEFAULT_SIZE, 132, Short.MAX_VALUE))
                        .addComponent(city, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(cname, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(button_order1, javax.swing.GroupLayout.PREFERRED_SIZE, 167, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(64, Short.MAX_VALUE))
        );
        mainaddressLayout.setVerticalGroup(
            mainaddressLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainaddressLayout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addGroup(mainaddressLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER, false)
                    .addComponent(countryselect, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(male)
                    .addComponent(female)
                    .addComponent(department, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(mainaddressLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(title, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(prename, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cname, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(mainaddressLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(street, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(zip, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(city, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(button_order1)
                .addGap(27, 27, 27))
        );

        addresspanel.addTab(bundle.getString("ContactPanel.mainaddress.TabConstraints.tabTitle"), mainaddress); // NOI18N

        jTabbedPane1.addTab(bundle.getString("ContactPanel.addresspanel.TabConstraints.tabTitle"), addresspanel); // NOI18N

        jPanel5.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel5.setName("jPanel5"); // NOI18N

        bankid.set_Label(bundle.getString("ContactPanel.bankid._Label")); // NOI18N
        bankid.setMaximumSize(new java.awt.Dimension(120, 24));
        bankid.setMinimumSize(new java.awt.Dimension(120, 21));
        bankid.setName("bankid"); // NOI18N
        bankid.setPreferredSize(new java.awt.Dimension(120, 24));

        bankaccount.set_Label(bundle.getString("ContactPanel.bankaccount._Label")); // NOI18N
        bankaccount.setMaximumSize(new java.awt.Dimension(120, 24));
        bankaccount.setMinimumSize(new java.awt.Dimension(120, 21));
        bankaccount.setName("bankaccount"); // NOI18N
        bankaccount.setPreferredSize(new java.awt.Dimension(120, 24));

        bankcurrency.set_Label(bundle.getString("ContactPanel.bankcurrency._Label")); // NOI18N
        bankcurrency.setMaximumSize(new java.awt.Dimension(120, 24));
        bankcurrency.setName("bankcurrency"); // NOI18N
        bankcurrency.setPreferredSize(new java.awt.Dimension(120, 24));

        bankname.set_Label(bundle.getString("ContactPanel.bankname._Label")); // NOI18N
        bankname.setMaximumSize(new java.awt.Dimension(120, 24));
        bankname.setMinimumSize(new java.awt.Dimension(120, 21));
        bankname.setName("bankname"); // NOI18N
        bankname.setPreferredSize(new java.awt.Dimension(120, 24));

        bankcountry.set_Label(bundle.getString("ContactPanel.bankcountry._Label")); // NOI18N
        bankcountry.setMaximumSize(new java.awt.Dimension(120, 24));
        bankcountry.setName("bankcountry"); // NOI18N
        bankcountry.setPreferredSize(new java.awt.Dimension(120, 24));

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(bankid, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(bankname, javax.swing.GroupLayout.DEFAULT_SIZE, 319, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(bankcountry, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(bankcurrency, javax.swing.GroupLayout.PREFERRED_SIZE, 124, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(bankaccount, javax.swing.GroupLayout.PREFERRED_SIZE, 353, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(315, 315, 315))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(bankid, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(bankaccount, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(bankcountry, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(bankname, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(bankcurrency, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(98, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab(bundle.getString("ContactPanel.jPanel5.TabConstraints.tabTitle"), jPanel5); // NOI18N

        jPanel4.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel4.setName("jPanel4"); // NOI18N

        jScrollPane1.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        jScrollPane1.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        jScrollPane1.setName("jScrollPane1"); // NOI18N

        notes.setText(bundle.getString("ContactPanel.notes.text")); // NOI18N
        notes.setName("notes"); // NOI18N
        jScrollPane1.setViewportView(notes);

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 981, Short.MAX_VALUE)
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 163, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab(bundle.getString("ContactPanel.jPanel4.TabConstraints.tabTitle"), jPanel4); // NOI18N

        propPanel.setName("propPanel"); // NOI18N

        jPanel6.setName("jPanel6"); // NOI18N
        jPanel6.setLayout(new java.awt.BorderLayout());

        jScrollPane3.setMaximumSize(new java.awt.Dimension(2147483647, 320));
        jScrollPane3.setMinimumSize(new java.awt.Dimension(30, 30));
        jScrollPane3.setName("jScrollPane3"); // NOI18N
        jScrollPane3.setPreferredSize(new java.awt.Dimension(100, 80));

        proptable.setAutoCreateRowSorter(true);
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
        proptable.setMinimumSize(new java.awt.Dimension(30, 30));
        proptable.setName("proptable"); // NOI18N
        proptable.setPreferredSize(new java.awt.Dimension(100, 80));
        proptable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane3.setViewportView(proptable);

        jPanel6.add(jScrollPane3, java.awt.BorderLayout.CENTER);

        javax.swing.GroupLayout propPanelLayout = new javax.swing.GroupLayout(propPanel);
        propPanel.setLayout(propPanelLayout);
        propPanelLayout.setHorizontalGroup(
            propPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(propPanelLayout.createSequentialGroup()
                .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, 539, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(446, Short.MAX_VALUE))
        );
        propPanelLayout.setVerticalGroup(
            propPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, 167, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab(bundle.getString("ContactPanel.propPanel.TabConstraints.tabTitle"), propPanel); // NOI18N

        filetab.setName("filetab"); // NOI18N

        jScrollPane5.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        jScrollPane5.setName("jScrollPane5"); // NOI18N

        filetableN.setAutoCreateRowSorter(true);
        filetableN.setDragEnabled(true);
        filetableN.setName("filetableN"); // NOI18N
        filetableN.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                filetableNMouseClicked(evt);
            }
        });
        jScrollPane5.setViewportView(filetableN);

        addfile1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/mpv5/resources/images/16/add.png"))); // NOI18N
        addfile1.setText(bundle.getString("ContactPanel.addfile1.text")); // NOI18N
        addfile1.setName("addfile1"); // NOI18N
        addfile1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addfile1ActionPerformed(evt);
            }
        });

        removefile1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/mpv5/resources/images/16/remove.png"))); // NOI18N
        removefile1.setText(bundle.getString("ContactPanel.removefile1.text")); // NOI18N
        removefile1.setName("removefile1"); // NOI18N
        removefile1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removefile1ActionPerformed(evt);
            }
        });

        jButton6.setText(bundle.getString("ContactPanel.jButton6.text")); // NOI18N
        jButton6.setName("jButton6"); // NOI18N
        jButton6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton6ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout filetabLayout = new javax.swing.GroupLayout(filetab);
        filetab.setLayout(filetabLayout);
        filetabLayout.setHorizontalGroup(
            filetabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(filetabLayout.createSequentialGroup()
                .addComponent(jButton6, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(addfile1, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(removefile1, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(773, Short.MAX_VALUE))
            .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 985, Short.MAX_VALUE)
        );
        filetabLayout.setVerticalGroup(
            filetabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(filetabLayout.createSequentialGroup()
                .addGroup(filetabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jButton6)
                    .addComponent(removefile1)
                    .addComponent(addfile1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 136, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab(bundle.getString("ContactPanel.filetab.TabConstraints.tabTitle"), filetab); // NOI18N

        jTabbedPane1.setSelectedIndex(1);

        jScrollPane4.setViewportView(jTabbedPane1);

        javax.swing.GroupLayout rightpaneLayout = new javax.swing.GroupLayout(rightpane);
        rightpane.setLayout(rightpaneLayout);
        rightpaneLayout.setHorizontalGroup(
            rightpaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jToolBar1, javax.swing.GroupLayout.DEFAULT_SIZE, 994, Short.MAX_VALUE)
            .addGroup(rightpaneLayout.createSequentialGroup()
                .addGap(2, 2, 2)
                .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 992, Short.MAX_VALUE))
            .addComponent(jPanel9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        rightpaneLayout.setVerticalGroup(
            rightpaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(rightpaneLayout.createSequentialGroup()
                .addComponent(jToolBar1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(4, 4, 4)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 107, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 197, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        add(rightpane, java.awt.BorderLayout.CENTER);

        toolbarpane.setName("toolbarpane"); // NOI18N
        toolbarpane.setLayout(new java.awt.BorderLayout());
        add(toolbarpane, java.awt.BorderLayout.NORTH);
    }// </editor-fold>//GEN-END:initComponents

    private void companyItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_companyItemStateChanged
//        isCompany(company.isSelected());
}//GEN-LAST:event_companyItemStateChanged

    private void manufacturerItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_manufacturerItemStateChanged
        if (!supplier.isSelected()) {
            isManufacturer(manufacturer.isSelected());
        }
}//GEN-LAST:event_manufacturerItemStateChanged

    private void supplierItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_supplierItemStateChanged
        isSupplier(supplier.isSelected());
}//GEN-LAST:event_supplierItemStateChanged

    private void customerItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_customerItemStateChanged
        if (!manufacturer.isSelected()) {
            isCustomer(customer.isSelected());
        }
}//GEN-LAST:event_customerItemStateChanged

    private void button_order1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_button_order1ActionPerformed
        AddressPanel p = new AddressPanel();
        p.setName(Messages.NEW_VALUE.toString());
        ((Address) p.getDataOwner()).setCompany(dataOwner.__getCompany());
        ((Address) p.getDataOwner()).setTaxnumber(dataOwner.__getTaxnumber());
        p.setDataParent(dataOwner);
        p.setDataOwner(p.getDataOwner(), true);
        addresspanel.add(p);
        addresspanel.setSelectedComponent(p);
}//GEN-LAST:event_button_order1ActionPerformed
    private static final int FILES = 0;
    private static final int PRODUCTS = 1;
    private static final int ITEM = 2;
    private static final int ACTIVITYS = 3;

    private void dataTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_dataTableMouseClicked
        if (dataTableContent != null) {
            if (null != dataTableContent) {
                switch (dataTableContent) {
                    case FILES:
                        fileTableClicked(evt);
                        break;
                    case PRODUCTS:
                        productTableClicked(evt);
                        break;
                    case ITEM:
                        itemTableClicked(evt);
                        break;
                    case ACTIVITYS:
                        activityTableClicked(evt);
                        break;
                    default:
                        break;
                }
            }
        }
    }//GEN-LAST:event_dataTableMouseClicked

    private void countryselectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_countryselectActionPerformed
}//GEN-LAST:event_countryselectActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed

        if (dataOwner.isExisting()) {
            mpv5.YabsViewProxy.instance().getIdentifierView().addTab(new JournalPanel(dataOwner), Messages.HISTORY_OF + getDataOwner().__getCname());
        }
    }//GEN-LAST:event_jButton2ActionPerformed

    private void button_billsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_button_billsActionPerformed

        c = Context.getInvoice();
//        c.addReference(Context.getContact().getDbIdentity(), "cname", "filename");
        Object[][] data = new DatabaseSearch(c).getValuesFor(Context.DEFAULT_ITEM_SEARCH, "contactsids", dataOwner.__getIDS());

        MPTableModel mod = new MPTableModel(data, Headers.ITEM_DEFAULT);
        mod.setTypes(new Class[]{Integer.class, Object.class, Date.class, Double.class});
        dataTable.setModel(mod);
        TableFormat.stripFirstColumn(dataTable);
        dataTableContent = ITEM;
        setupFilter();
        dataTable.getRowSorter().toggleSortOrder(2);//fixme doit better
        dataTable.getRowSorter().toggleSortOrder(2);
    }//GEN-LAST:event_button_billsActionPerformed

    private void button_offersActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_button_offersActionPerformed
        c = Context.getOffer();

        Object[][] data = new DatabaseSearch(c).getValuesFor(Context.DEFAULT_ITEM_SEARCH, "contactsids", dataOwner.__getIDS());
        MPTableModel mod = new MPTableModel(data, Headers.ITEM_DEFAULT);
        mod.setTypes(new Class[]{Integer.class, Object.class, Date.class, Double.class});
        dataTable.setModel(mod);
        TableFormat.stripFirstColumn(dataTable);
        dataTableContent = ITEM;

        setupFilter();
        dataTable.getRowSorter().toggleSortOrder(2);//fixme doit better
        dataTable.getRowSorter().toggleSortOrder(2);
    }//GEN-LAST:event_button_offersActionPerformed

    private void button_ordersActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_button_ordersActionPerformed
        c = Context.getOrder();

        Object[][] data = new DatabaseSearch(c).getValuesFor(Context.DEFAULT_ITEM_SEARCH, "contactsids", dataOwner.__getIDS());
        MPTableModel mod = new MPTableModel(data, Headers.ITEM_DEFAULT);
        mod.setTypes(new Class[]{Integer.class, Object.class, Date.class, Double.class});
        dataTable.setModel(mod);
        TableFormat.stripFirstColumn(dataTable);
        dataTableContent = ITEM;
        setupFilter();
        dataTable.getRowSorter().toggleSortOrder(2);//fixme doit better
        dataTable.getRowSorter().toggleSortOrder(2);
    }//GEN-LAST:event_button_ordersActionPerformed

    private void button_productsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_button_productsActionPerformed
        Context cp = Context.getProduct();
        Object[][] data = new DatabaseSearch(cp, 1000).getValuesFor(Context.DEFAULT_PRODUCT_SEARCH, new String[]{"manufacturersids", "suppliersids"}, dataOwner.__getIDS());
        dataTable.setModel(new MPTableModel(data, Headers.PRODUCT_DEFAULT));
        dataTableContent = PRODUCTS;

        TableFormat.stripFirstColumn(dataTable);
        setupFilter();
    }//GEN-LAST:event_button_productsActionPerformed

    private void button_orderActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_button_orderActionPerformed
        if (dataOwner.isExisting()) {
            Item i = (Item) DatabaseObject.getObject(Context.getOrder());
            i.setContactsids(dataOwner.__getIDS());
            i.setCname(Messages.NEW_ORDER.getValue());
            i.setInttype(Item.TYPE_ORDER);
            i.setDateadded(new Date());
            i.setGroupsids(dataOwner.__getGroupsids());
            mpv5.YabsViewProxy.instance().getIdentifierView().addTab(i);
        }
    }//GEN-LAST:event_button_orderActionPerformed

    private void button_offerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_button_offerActionPerformed
        if (dataOwner.isExisting()) {
            Item i = (Item) DatabaseObject.getObject(Context.getOffer());
            i.setContactsids(dataOwner.__getIDS());
            i.setCname(Messages.NEW_OFFER.getValue());
            i.setInttype(Item.TYPE_OFFER);
            i.setGroupsids(dataOwner.__getGroupsids());
            i.setDateadded(new Date());
            mpv5.YabsViewProxy.instance().getIdentifierView().addTab(i);
        }
    }//GEN-LAST:event_button_offerActionPerformed

    private void button_productActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_button_productActionPerformed
        if (dataOwner.isExisting()) {
            Product i = (Product) DatabaseObject.getObject(Context.getProduct());
            i.setSuppliersids(dataOwner.__getIDS());
            i.setManufacturersids(dataOwner.__getIDS());
            i.setCname(Messages.NEW_PRODUCT.getValue());
            i.setInttype(Product.TYPE_PRODUCT);
            i.setGroupsids(dataOwner.__getGroupsids());
            mpv5.YabsViewProxy.instance().getIdentifierView().addTab(i);
        }
    }//GEN-LAST:event_button_productActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        if (dataOwner != null && dataOwner.isExisting()) {
            ScheduleDayEvents p = new ScheduleDayEvents(Schedule.getEvents(dataOwner));
        }
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
        if (mailaddress.hasText()) {
            try {
                Desktop.getDesktop().mail(new URI("mailto:" + mailaddress.getText()));
            } catch (Exception ex) {
                Log.Debug(ex);
                Popup.error(ex);
            }
        }
    }//GEN-LAST:event_jButton5ActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
    }//GEN-LAST:event_jButton4ActionPerformed

    private void filetableNMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_filetableNMouseClicked
        fileTableClicked(evt);
    }//GEN-LAST:event_filetableNMouseClicked

    private void addfile1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addfile1ActionPerformed
        if (dataOwner.isExisting()) {
            addFile();
        }

    }//GEN-LAST:event_addfile1ActionPerformed

    private void removefile1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removefile1ActionPerformed
        deleteFile();

    }//GEN-LAST:event_removefile1ActionPerformed

    private void jButton6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton6ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jButton6ActionPerformed

    private void button_activitylistsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_button_activitylistsActionPerformed
        Context ca = Context.getActivityList();
        Object[][] data = new DatabaseSearch(ca, 1000).getValuesFor(Context.DEFAULT_ACTIVITYLIST_SEARCH, new String[]{"contactsids"}, dataOwner.__getIDS());
        dataTable.setModel(new MPTableModel(data, Headers.ACTIVITY_DEFAULT));
        dataTableContent = ACTIVITYS;

        TableFormat.stripFirstColumn(dataTable);
        setupFilter();
    }//GEN-LAST:event_button_activitylistsActionPerformed

    private void button_creditActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_button_creditActionPerformed
        toInvoice(Item.TYPE_CREDIT);
    }//GEN-LAST:event_button_creditActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel addedby;
    private javax.swing.JButton addfile1;
    private javax.swing.JTabbedPane addresspanel;
    private mpv5.ui.beans.LabeledTextField bankaccount;
    private mpv5.ui.beans.LabeledTextField bankcountry;
    private mpv5.ui.beans.LabeledTextField bankcurrency;
    private mpv5.ui.beans.LabeledTextField bankid;
    private mpv5.ui.beans.LabeledTextField bankname;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JButton button_activitylists;
    private mpv5.ui.beans.DropDownButton button_bill;
    private javax.swing.JButton button_bills;
    private javax.swing.JButton button_credit;
    private javax.swing.JButton button_offer;
    private javax.swing.JButton button_offers;
    private javax.swing.JButton button_order;
    private javax.swing.JButton button_order1;
    private javax.swing.JButton button_orders;
    private javax.swing.JButton button_product;
    private javax.swing.JButton button_products;
    private mpv5.ui.beans.LabeledTextField city;
    private mpv5.ui.beans.LabeledTextField cname;
    private javax.swing.JCheckBox company;
    private mpv5.ui.beans.LabeledCombobox companyselect;
    private javax.swing.JComboBox countryselect;
    private javax.swing.JCheckBox customer;
    private javax.swing.JTable dataTable;
    private javax.swing.JLabel dateadded;
    private mpv5.ui.beans.LabeledTextField department;
    private mpv5.ui.beans.LabeledTextField fax;
    private javax.swing.JRadioButton female;
    private javax.swing.JPanel filetab;
    private javax.swing.JTable filetableN;
    private mpv5.ui.beans.LabeledTextField filterme;
    private mpv5.ui.beans.LabeledCombobox groupnameselect;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JToolBar.Separator jSeparator1;
    private javax.swing.JToolBar.Separator jSeparator2;
    private javax.swing.JToolBar.Separator jSeparator3;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JToolBar jToolBar2;
    private javax.swing.JToolBar jToolBar3;
    private javax.swing.JPanel leftpane;
    private mpv5.ui.beans.LabeledTextField mailaddress;
    private javax.swing.JPanel mainaddress;
    private mpv5.ui.beans.LabeledTextField mainphone;
    private javax.swing.JRadioButton male;
    private javax.swing.JCheckBox manufacturer;
    private mpv5.ui.beans.LabeledTextField mobilephone;
    private javax.swing.JTextPane notes;
    private mpv5.ui.beans.LabeledTextField number;
    private mpv5.ui.beans.LabeledTextField payterm;
    private mpv5.ui.beans.LabeledTextField prename;
    private mpv5.ui.beans.PrinitingComboBox prinitingComboBox1;
    private javax.swing.JPanel propPanel;
    private javax.swing.JTable proptable;
    private javax.swing.JButton removefile1;
    private javax.swing.JPanel rightpane;
    private mpv5.ui.beans.LabeledTextField street;
    private javax.swing.JCheckBox supplier;
    private mpv5.ui.beans.LabeledTextField taxnumber;
    private mpv5.ui.beans.LabeledTextField title;
    private javax.swing.JPanel toolbarpane;
    private mpv5.ui.beans.LabeledTextField website;
    private mpv5.ui.beans.LabeledTextField workphone;
    private mpv5.ui.beans.LabeledTextField zip;
    // End of variables declaration//GEN-END:variables
   private final javax.swing.table.DefaultTableModel tableModel = null;
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

    @Override
    public boolean collectData() {

        city_ = city.get_Text();
        cname_ = cname.get_Text();
        taxnumber_ = taxnumber.get_Text();
        iscompany_ = company.isSelected();

        if (companyselect.getText() != null) {
            company_ = companyselect.getText();
        } else {
            company_ = "";
        }

        if (iscompany_ && company_.length() == 0) {
            company_ = cname_;
        } else if (iscompany_ && cname_.length() == 0) {
            cname_ = company_;
        }

        if (company_.length() == 0 && cname_.length() == 0) {
            return false;
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
        intaddedby_ = User.getUserId(addedby.getText());
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
            if (country_.length() == 0) {
                country_ = mpv5.db.objects.User.getCurrentUser().__getDefcountry();
            }
            countryselect.setSelectedIndex(MPComboBoxModelItem.getItemID(country_, countryselect.getModel()));
        } catch (Exception e) {
            Log.Debug(e);
        }
        customer.setSelected(iscustomer_);
//        enabled.setSelected(isenabled_);
        fax.set_Text(fax_);
        mailaddress.set_Text(mailaddress_);
        male.setSelected(ismale_);
        female.setSelected(!ismale_);
        manufacturer.setSelected(ismanufacturer_);
        mobilephone.set_Text(mobilephone_);
        notes.setText(notes_);
        number.set_Text(cnumber_);
//        old_cnumber = cnumber_;
        mainphone.set_Text(mainphone_);
        prename.set_Text(prename_);
        street.set_Text(street_);
        supplier.setSelected(issupplier_);
        title.set_Text(title_);
        website.set_Text(website_);
        workphone.set_Text(workphone_);
        zip.set_Text(zip_);
        dateadded.setText(DateConverter.getDefDateString(dateadded_));
        addedby.setText(User.getUsername(intaddedby_));
        department.set_Text(department_);

        bankaccount.setText(bankaccount_);
        bankcountry.setText(bankcountry_);
        bankcurrency.setText(bankcurrency_);
        bankid.setText(bankid_);
        bankname.setText(bankname_);
        payterm.setText(payterm_);
        fillFiles();

    }

    @Override
    public void refresh() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    groupnameselect.setModel(DatabaseObject.getObject(Context.getGroup(), mpv5.db.objects.User.getCurrentUser().__getGroupsids()));
                    sp.refresh();
                    fillFiles();
                } catch (Exception e) {
                    Log.Debug(this, e);
                }
            }
        };

        SwingUtilities.invokeLater(runnable);
    }

    public void isCustomer(boolean b) {
        customer.setSelected(b);
        button_offers.setEnabled(b);
        button_offer.setEnabled(b);
        button_bill.setEnabled(b);
        button_bills.setEnabled(b);
        button_order.setEnabled(b);
        button_orders.setEnabled(b);
        button_activitylists.setEnabled(b);
    }

    public void isManufacturer(boolean b) {
        manufacturer.setSelected(b);
        button_product.setEnabled(b);
        button_products.setEnabled(b);
        button_activitylists.setEnabled(!b);
    }

    public void isSupplier(boolean b) {
        supplier.setSelected(b);
        button_product.setEnabled(b);
        button_products.setEnabled(b);
        button_activitylists.setEnabled(!b);
    }

//    public void isCompany(boolean selected) {
//        try {
//            if (selected) {
//                companyselect.setSelectedIndex(0);
//            }
//        } catch (Exception e) {
//        }
//        company.setSelected(selected);
//        companyselect.setEnabled(!selected);
//        title.setEnabled(!selected);
//        male.setEnabled(!selected);
//        female.setEnabled(!selected);
//        prename.setEnabled(!selected);
//        department.setEnabled(!selected);
//
//    }
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
    public void showSearchBar(boolean show) {
        leftpane.removeAll();
        if (show) {
            leftpane.add(sp, BorderLayout.CENTER);
            sp.search();
        }

        validate();
    }

    private void setTitle(String title) {
        if (this.getParent() instanceof JViewport || this.getParent() instanceof JTabbedPane) {
            JTabbedPane jTabbedPane = null;
            String title1 = title;
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
    }

    @Override
    public void actionAfterSave() {
    }

    @Override
    public void actionAfterCreate() {
        sp.refresh();
    }

    @Override
    public void actionBeforeCreate() {
//        if (old_cnumber.equals(cnumber_)) {
//                cnumber_ = null;
//            }
    }

    @Override
    public void actionBeforeSave() throws ChangeNotApprovedException {
        if (dataOwner.isExisting()) {
            if (mpv5.db.objects.User.getCurrentUser().getProperties().getProperty("org.openyabs.uiproperty", "dowarnings")) {

                if (!Popup.Y_N_dialog(Messages.REALLY_CHANGE)) {
                    throw new ChangeNotApprovedException(dataOwner);
                }
            }
        }
    }

    @Override
    public void mail() {
        try {
            Desktop.getDesktop().mail(new URI("mailto:" + dataOwner.__getMailaddress()));
        } catch (Exception uRISyntaxException) {
            Log.Debug(this, uRISyntaxException.getMessage());
            Popup.error(uRISyntaxException);
        }
    }

    @Override
    public void print() {
        Export.print(this);
    }

    private void properties() {
        final MPTableModel m = new MPTableModel(ValueProperty.getProperties(dataOwner));

        if (m.getDataVector().isEmpty()) {
            proptable.setModel(new MPTableModel(
                    Arrays.asList(new ValueProperty[]{new ValueProperty("", "", dataOwner)})));
        } else {
            proptable.setModel(m);
        }

        m.addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
//                Log.Print(String.valueOf(m.getValueAt(e.getLastRow(), 1)).length());
                if (dataOwner.isExisting()) {
                    if (String.valueOf(m.getValueAt(e.getLastRow(), 1)).length() == 0) {
                        ValueProperty.deleteProperty(dataOwner, String.valueOf(m.getData()[e.getLastRow()][0]));
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

    private void setupFilter() {
        final JTable table = dataTable;
        final TableRowSorter<TableModel> sorter = new TableRowSorter<>(table.getModel());
        table.setRowSorter(sorter);
        for (ActionListener a : filterme.getTextField().getActionListeners()) {
            filterme.getTextField().removeActionListener(tb);
        }
        filterme.getTextField().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String text = filterme.getTextField().getText();
                if (text.length() == 0) {
                    sorter.setRowFilter(null);
                } else {
                    sorter.setRowFilter(RowFilter.regexFilter(text));
                }
            }
        });
    }
}
