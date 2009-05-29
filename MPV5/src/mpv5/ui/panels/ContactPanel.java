/*
This file is part of MP by anti43 /GPL.

MP is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

MP is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with MP.  If not, see <http://www.gnu.org/licenses/>.
 */
/*
 * ContactPanel.java
 *
 * Created on Nov 20, 2008, 8:17:28 AM
 */
package mpv5.ui.panels;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import mpv5.db.common.*;
import mpv5.globals.Headers;
import mpv5.globals.Messages;
import mpv5.db.objects.Address;
import mpv5.db.objects.Contact;
import mpv5.db.objects.Favourite;
import mpv5.logging.Log;
import mpv5.resources.languages.LanguageManager;
import mpv5.ui.dialogs.BigPopup;
import mpv5.ui.dialogs.DialogForFile;
import mpv5.ui.dialogs.Popup;
import mpv5.ui.dialogs.subcomponents.ControlPanel_Groups;
import mpv5.ui.frames.MPV5View;
import mpv5.ui.popups.FileTablePopUp;
import mpv5.ui.toolbars.DataPanelTB;
import mpv5.db.objects.User;
import mpv5.utils.arrays.ArrayUtilities;
import mpv5.utils.date.DateConverter;
import mpv5.utils.files.FileDirectoryHandler;
import mpv5.utils.models.MPComboBoxModelItem;
import mpv5.utils.models.MPTableModel;
import mpv5.utils.tables.TableFormat;
import mpv5.utils.ui.TextFieldUtils;

/**
 *
 * @author anti43
 */
public class ContactPanel extends javax.swing.JPanel implements DataPanel {

    /**
     * Represents a Contact in general
     */
    public static final int CONTACT = 0;
    /**
     * Is a customer
     */
    public static final int CUSTOMER = 1;
    /**
     * Is a supplier
     */
    public static final int SUPPLIER = 2;
    /**
     * Is a manufacturer
     */
    public static final int MANUFACTURER = 3;
    private static final long serialVersionUID = 1L;
    public static final int COMPANY = 4;
    private Contact dataOwner;
    private DataPanelTB tb;
    private SearchPanel sp;

    /** Creates new form ContactPanel
     * @param context
     */
    public ContactPanel(Context context) {
        initComponents();
        sp = new SearchPanel(context, this);
        tb = new mpv5.ui.toolbars.DataPanelTB(this);
        toolbarpane.add(tb, BorderLayout.CENTER);
        dataOwner = new Contact();
//        showSearchBar(true);
        refresh();
        dateadded.setText(DateConverter.getTodayDefDate());
        addedby.setText(MPV5View.getUser().getName());
    }

    public DatabaseObject getDataOwner() {
        return dataOwner;
    }

    public void setDataOwner(DatabaseObject object) {
        dataOwner = (Contact) object;
        dataOwner.setPanelData(this);
        this.exposeData();

        if (this.getParent() instanceof JTabbedPane) {
            JTabbedPane jTabbedPane = (JTabbedPane) this.getParent();
            jTabbedPane.setTitleAt(jTabbedPane.getSelectedIndex(), Messages.CONTACT + cname_);
        }

        prinitingComboBox1.init(dataOwner);

        tb.setFavourite(Favourite.isFavourite(object));
        addAddresses();
        dataTable.setModel(new MPTableModel());
    }

    public void setType(int type) {
        switch (type) {
            case CONTACT:
                break;
            case CUSTOMER:
                isCustomer(true);
                break;
            case SUPPLIER:
                isSupplier(true);
                break;
            case MANUFACTURER:
                isManufacturer(true);
                break;
            case COMPANY:
                isCompany(true);
                break;
            default:
                ;
        }
    }

    public void showRequiredFields() {
        TextFieldUtils.blinkerRed(cname);
    }

    private void addAddresses() {
        String str = addresspanel.getTitleAt(0);
        Component n = addresspanel.getComponent(0);
        addresspanel.removeAll();
        addresspanel.add(n);
        addresspanel.setTitleAt(0, str);
        try {
            ArrayList<Contact> data = DatabaseObject.getReferencedObjects(dataOwner, Context.getAddress());

            for (int i = 0; i < data.size(); i++) {
                DatabaseObject databaseObject = data.get(i);
                AddressPanel p = new AddressPanel();
                p.setName(databaseObject.__getCName());
                p.setDataOwner(databaseObject);
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
        try {
            QueryHandler.instanceOf().clone(Context.getFiles()).removeFile(dataTable.getModel().getValueAt(dataTable.getSelectedRow(), 0).toString());
        } catch (Exception e) {
            Log.Debug(this, e.getMessage());
        }
        fillFiles();
    }

    private void fillFiles() {
        Context c = Context.getFilesToContacts();
        c.addReference(Context.getFiles().getDbIdentity(), "cname", "filename");
        Object[][] data = new DatabaseSearch(c).getValuesFor(Context.DETAILS_FILES, "contactsids", dataOwner.__getIDS());

        dataTable.setModel(new MPTableModel(data, Headers.CONTACT_FILES.getValue()));
        TableFormat.stripFirstColumn(dataTable);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        leftpane = new javax.swing.JPanel();
        rightpane = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        enabled = new javax.swing.JCheckBox();
        jLabel2 = new javax.swing.JLabel();
        customer = new javax.swing.JCheckBox();
        supplier = new javax.swing.JCheckBox();
        manufacturer = new javax.swing.JCheckBox();
        number = new mpv5.ui.beans.LabeledTextField();
        dateadded = new javax.swing.JLabel();
        company = new javax.swing.JCheckBox();
        addedby = new javax.swing.JLabel();
        groupnameselect = new javax.swing.JComboBox();
        jLabel4 = new javax.swing.JLabel();
        button_order2 = new javax.swing.JButton();
        jPanel4 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        notes = new javax.swing.JTextPane();
        jPanel3 = new javax.swing.JPanel();
        mobilephone = new mpv5.ui.beans.LabeledTextField();
        mainphone = new mpv5.ui.beans.LabeledTextField();
        workphone = new mpv5.ui.beans.LabeledTextField();
        mailaddress = new mpv5.ui.beans.LabeledTextField();
        website = new mpv5.ui.beans.LabeledTextField();
        fax = new mpv5.ui.beans.LabeledTextField();
        jLabel9 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jPanel9 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        dataTable = new javax.swing.JTable();
        button_bills = new javax.swing.JButton();
        button_offers = new javax.swing.JButton();
        button_products = new javax.swing.JButton();
        button_orders = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();
        removefile = new javax.swing.JButton();
        addfile = new javax.swing.JButton();
        jToolBar1 = new javax.swing.JToolBar();
        button_offer = new javax.swing.JButton();
        button_bill = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JToolBar.Separator();
        button_product = new javax.swing.JButton();
        button_order = new javax.swing.JButton();
        jSeparator2 = new javax.swing.JToolBar.Separator();
        jButton2 = new javax.swing.JButton();
        prinitingComboBox1 = new mpv5.ui.beans.PrinitingComboBox();
        addresspanel = new javax.swing.JTabbedPane();
        jPanel2 = new javax.swing.JPanel();
        title = new mpv5.ui.beans.LabeledTextField();
        street = new mpv5.ui.beans.LabeledTextField();
        cname = new mpv5.ui.beans.LabeledTextField();
        prename = new mpv5.ui.beans.LabeledTextField();
        city = new mpv5.ui.beans.LabeledTextField();
        companyselect = new javax.swing.JComboBox();
        jLabel3 = new javax.swing.JLabel();
        zip = new mpv5.ui.beans.LabeledTextField();
        male = new javax.swing.JRadioButton();
        female = new javax.swing.JRadioButton();
        taxnumber = new mpv5.ui.beans.LabeledTextField();
        department = new mpv5.ui.beans.LabeledTextField();
        jLabel5 = new javax.swing.JLabel();
        countryselect = new javax.swing.JComboBox();
        button_order1 = new javax.swing.JButton();
        toolbarpane = new javax.swing.JPanel();

        java.util.ResourceBundle bundle = mpv5.resources.languages.LanguageManager.getBundle(); // NOI18N
        setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("ContactPanel.border.title"))); // NOI18N
        setName("Form"); // NOI18N

        leftpane.setName("leftpane"); // NOI18N
        leftpane.setLayout(new java.awt.BorderLayout());

        rightpane.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("ContactPanel.rightpane.border.title"))); // NOI18N
        rightpane.setName("rightpane"); // NOI18N

        jPanel1.setBackground(new java.awt.Color(227, 219, 202));
        jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel1.setName("jPanel1"); // NOI18N

        jLabel1.setFont(jLabel1.getFont());
        jLabel1.setText(bundle.getString("ContactPanel.jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        enabled.setFont(enabled.getFont().deriveFont(enabled.getFont().getStyle() & ~java.awt.Font.BOLD));
        enabled.setSelected(true);
        enabled.setText(bundle.getString("ContactPanel.enabled.text")); // NOI18N
        enabled.setName("enabled"); // NOI18N
        enabled.setOpaque(false);
        enabled.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                enabledItemStateChanged(evt);
            }
        });

        jLabel2.setFont(jLabel2.getFont());
        jLabel2.setText(bundle.getString("ContactPanel.jLabel2.text")); // NOI18N
        jLabel2.setName("jLabel2"); // NOI18N

        customer.setFont(customer.getFont().deriveFont(customer.getFont().getStyle() & ~java.awt.Font.BOLD));
        customer.setText(bundle.getString("ContactPanel.customer.text")); // NOI18N
        customer.setName("customer"); // NOI18N
        customer.setOpaque(false);
        customer.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                customerItemStateChanged(evt);
            }
        });

        supplier.setFont(supplier.getFont().deriveFont(supplier.getFont().getStyle() & ~java.awt.Font.BOLD));
        supplier.setText(bundle.getString("ContactPanel.supplier.text")); // NOI18N
        supplier.setName("supplier"); // NOI18N
        supplier.setOpaque(false);
        supplier.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                supplierItemStateChanged(evt);
            }
        });

        manufacturer.setFont(manufacturer.getFont().deriveFont(manufacturer.getFont().getStyle() & ~java.awt.Font.BOLD));
        manufacturer.setText(bundle.getString("ContactPanel.manufacturer.text")); // NOI18N
        manufacturer.setName("manufacturer"); // NOI18N
        manufacturer.setOpaque(false);
        manufacturer.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                manufacturerItemStateChanged(evt);
            }
        });

        number.set_Label(bundle.getString("ContactPanel.number._Label")); // NOI18N
        number.setFont(number.getFont());
        number.setName("number"); // NOI18N

        dateadded.setFont(dateadded.getFont());
        dateadded.setText(bundle.getString("ContactPanel.dateadded.text")); // NOI18N
        dateadded.setToolTipText(bundle.getString("ContactPanel.dateadded.toolTipText")); // NOI18N
        dateadded.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        dateadded.setEnabled(false);
        dateadded.setName("dateadded"); // NOI18N

        company.setFont(company.getFont().deriveFont(company.getFont().getStyle() & ~java.awt.Font.BOLD));
        company.setText(bundle.getString("ContactPanel.company.text")); // NOI18N
        company.setName("company"); // NOI18N
        company.setOpaque(false);
        company.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                companyItemStateChanged(evt);
            }
        });

        addedby.setFont(addedby.getFont());
        addedby.setText(bundle.getString("ContactPanel.addedby.text")); // NOI18N
        addedby.setToolTipText(bundle.getString("ContactPanel.addedby.toolTipText")); // NOI18N
        addedby.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        addedby.setEnabled(false);
        addedby.setName("addedby"); // NOI18N

        groupnameselect.setName("groupnameselect"); // NOI18N

        jLabel4.setFont(jLabel4.getFont());
        jLabel4.setText(bundle.getString("ContactPanel.jLabel4.text")); // NOI18N
        jLabel4.setName("jLabel4"); // NOI18N

        button_order2.setFont(button_order2.getFont().deriveFont(button_order2.getFont().getStyle() & ~java.awt.Font.BOLD, button_order2.getFont().getSize()-2));
        button_order2.setText(bundle.getString("ContactPanel.button_order2.text")); // NOI18N
        button_order2.setFocusable(false);
        button_order2.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        button_order2.setName("button_order2"); // NOI18N
        button_order2.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        button_order2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                button_order2ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(number, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1)
                            .addComponent(jLabel2))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(customer)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(supplier))
                            .addComponent(enabled))
                        .addGap(2, 2, 2)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(company, javax.swing.GroupLayout.PREFERRED_SIZE, 127, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(manufacturer))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 74, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(dateadded, javax.swing.GroupLayout.PREFERRED_SIZE, 97, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(button_order2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(addedby, javax.swing.GroupLayout.PREFERRED_SIZE, 97, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(groupnameselect, javax.swing.GroupLayout.PREFERRED_SIZE, 222, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(enabled)
                            .addComponent(jLabel1)
                            .addComponent(company))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(customer)
                            .addComponent(supplier)
                            .addComponent(manufacturer)
                            .addComponent(jLabel2))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(number, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(dateadded, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(addedby, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(jLabel4)
                                .addComponent(button_order2, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(groupnameselect, 0, 0, Short.MAX_VALUE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel4.setBackground(new java.awt.Color(204, 204, 204));
        jPanel4.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel4.setName("jPanel4"); // NOI18N

        jScrollPane1.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        jScrollPane1.setName("jScrollPane1"); // NOI18N

        notes.setText(bundle.getString("ContactPanel.notes.text")); // NOI18N
        notes.setDragEnabled(true);
        notes.setName("notes"); // NOI18N
        jScrollPane1.setViewportView(notes);

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 661, Short.MAX_VALUE)
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        jPanel3.setBackground(new java.awt.Color(227, 219, 202));
        jPanel3.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel3.setName("jPanel3"); // NOI18N

        mobilephone.set_Label(bundle.getString("ContactPanel.mobilephone._Label")); // NOI18N
        mobilephone.setName("mobilephone"); // NOI18N

        mainphone.set_Label(bundle.getString("ContactPanel.mainphone._Label")); // NOI18N
        mainphone.setName("mainphone"); // NOI18N

        workphone.set_Label(bundle.getString("ContactPanel.workphone._Label")); // NOI18N
        workphone.setName("workphone"); // NOI18N

        mailaddress.set_Label(bundle.getString("ContactPanel.mailaddress._Label")); // NOI18N
        mailaddress.setName("mailaddress"); // NOI18N

        website.set_Label(bundle.getString("ContactPanel.website._Label")); // NOI18N
        website.setName("website"); // NOI18N

        fax.set_Label(bundle.getString("ContactPanel.fax._Label")); // NOI18N
        fax.setName("fax"); // NOI18N

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(mainphone, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(fax, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(mailaddress, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(workphone, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(mobilephone, javax.swing.GroupLayout.DEFAULT_SIZE, 201, Short.MAX_VALUE)
                    .addComponent(website, javax.swing.GroupLayout.DEFAULT_SIZE, 201, Short.MAX_VALUE))
                .addContainerGap(50, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(mainphone, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(workphone, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(mobilephone, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(mailaddress, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(fax, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(website, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        jLabel9.setFont(jLabel9.getFont());
        jLabel9.setText(bundle.getString("ContactPanel.jLabel9.text")); // NOI18N
        jLabel9.setName("jLabel9"); // NOI18N

        jLabel8.setFont(jLabel8.getFont());
        jLabel8.setText(bundle.getString("ContactPanel.jLabel8.text")); // NOI18N
        jLabel8.setName("jLabel8"); // NOI18N

        jLabel7.setFont(jLabel7.getFont());
        jLabel7.setText(bundle.getString("ContactPanel.jLabel7.text")); // NOI18N
        jLabel7.setName("jLabel7"); // NOI18N

        jPanel9.setBackground(new java.awt.Color(204, 204, 204));
        jPanel9.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel9.setName("jPanel9"); // NOI18N

        jScrollPane2.setName("jScrollPane2"); // NOI18N

        dataTable.setDragEnabled(true);
        dataTable.setName("dataTable"); // NOI18N
        dataTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                dataTableMouseClicked(evt);
            }
        });
        jScrollPane2.setViewportView(dataTable);

        button_bills.setText(bundle.getString("ContactPanel.button_bills.text")); // NOI18N
        button_bills.setEnabled(false);
        button_bills.setName("button_bills"); // NOI18N

        button_offers.setText(bundle.getString("ContactPanel.button_offers.text")); // NOI18N
        button_offers.setEnabled(false);
        button_offers.setName("button_offers"); // NOI18N

        button_products.setText(bundle.getString("ContactPanel.button_products.text")); // NOI18N
        button_products.setEnabled(false);
        button_products.setName("button_products"); // NOI18N

        button_orders.setText(bundle.getString("ContactPanel.button_orders.text")); // NOI18N
        button_orders.setEnabled(false);
        button_orders.setName("button_orders"); // NOI18N

        jButton1.setText(bundle.getString("ContactPanel.jButton1.text")); // NOI18N
        jButton1.setName("jButton1"); // NOI18N
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        removefile.setIcon(new javax.swing.ImageIcon(getClass().getResource("/mpv5/resources/images/16/remove.png"))); // NOI18N
        removefile.setText(bundle.getString("ContactPanel.removefile.text")); // NOI18N
        removefile.setEnabled(false);
        removefile.setName("removefile"); // NOI18N
        removefile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removefileActionPerformed(evt);
            }
        });

        addfile.setIcon(new javax.swing.ImageIcon(getClass().getResource("/mpv5/resources/images/16/add.png"))); // NOI18N
        addfile.setText(bundle.getString("ContactPanel.addfile.text")); // NOI18N
        addfile.setEnabled(false);
        addfile.setName("addfile"); // NOI18N
        addfile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addfileActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 637, Short.MAX_VALUE)
                    .addGroup(jPanel9Layout.createSequentialGroup()
                        .addComponent(button_bills)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(button_offers)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(button_products)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(button_orders)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 198, Short.MAX_VALUE)
                        .addComponent(jButton1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(addfile, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(removefile, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(button_bills)
                        .addComponent(button_offers)
                        .addComponent(button_products)
                        .addComponent(button_orders)
                        .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(removefile, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(addfile, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 41, Short.MAX_VALUE)
                .addContainerGap())
        );

        jToolBar1.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jToolBar1.setFloatable(false);
        jToolBar1.setRollover(true);
        jToolBar1.setName("jToolBar1"); // NOI18N

        button_offer.setText(bundle.getString("ContactPanel.button_offer.text")); // NOI18N
        button_offer.setEnabled(false);
        button_offer.setFocusable(false);
        button_offer.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        button_offer.setName("button_offer"); // NOI18N
        button_offer.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(button_offer);

        button_bill.setText(bundle.getString("ContactPanel.button_bill.text")); // NOI18N
        button_bill.setEnabled(false);
        button_bill.setFocusable(false);
        button_bill.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        button_bill.setName("button_bill"); // NOI18N
        button_bill.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(button_bill);

        jSeparator1.setName("jSeparator1"); // NOI18N
        jToolBar1.add(jSeparator1);

        button_product.setText(bundle.getString("ContactPanel.button_product.text")); // NOI18N
        button_product.setEnabled(false);
        button_product.setFocusable(false);
        button_product.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        button_product.setName("button_product"); // NOI18N
        button_product.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(button_product);

        button_order.setText(bundle.getString("ContactPanel.button_order.text")); // NOI18N
        button_order.setEnabled(false);
        button_order.setFocusable(false);
        button_order.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        button_order.setName("button_order"); // NOI18N
        button_order.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(button_order);

        jSeparator2.setName("jSeparator2"); // NOI18N
        jToolBar1.add(jSeparator2);

        jButton2.setText(bundle.getString("ContactPanel.jButton2.text")); // NOI18N
        jButton2.setFocusable(false);
        jButton2.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton2.setName("jButton2"); // NOI18N
        jButton2.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });
        jToolBar1.add(jButton2);

        prinitingComboBox1.setName("prinitingComboBox1"); // NOI18N

        addresspanel.setBackground(new java.awt.Color(255, 255, 255));
        addresspanel.setAutoscrolls(true);
        addresspanel.setFont(new java.awt.Font("Dialog", 0, 11));
        addresspanel.setName("addresspanel"); // NOI18N

        jPanel2.setBackground(new java.awt.Color(227, 219, 202));
        jPanel2.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        jPanel2.setName("jPanel2"); // NOI18N

        title.set_Label(bundle.getString("ContactPanel.title._Label")); // NOI18N
        title.setName("title"); // NOI18N

        street.set_Label(bundle.getString("ContactPanel.street._Label")); // NOI18N
        street.setName("street"); // NOI18N

        cname.set_Label(bundle.getString("ContactPanel.cname._Label")); // NOI18N
        cname.setName("cname"); // NOI18N

        prename.set_Label(bundle.getString("ContactPanel.prename._Label")); // NOI18N
        prename.setName("prename"); // NOI18N

        city.set_Label(bundle.getString("ContactPanel.city._Label")); // NOI18N
        city.setName("city"); // NOI18N

        companyselect.setName("companyselect"); // NOI18N
        companyselect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                companyselectActionPerformed(evt);
            }
        });

        jLabel3.setFont(new java.awt.Font("Dialog", 0, 12));
        jLabel3.setText(bundle.getString("ContactPanel.jLabel3.text")); // NOI18N
        jLabel3.setName("jLabel3"); // NOI18N

        zip.set_Label(bundle.getString("ContactPanel.zip._Label")); // NOI18N
        zip.setName("zip"); // NOI18N

        buttonGroup1.add(male);
        male.setFont(male.getFont().deriveFont(male.getFont().getStyle() & ~java.awt.Font.BOLD));
        male.setSelected(true);
        male.setText(bundle.getString("ContactPanel.male.text")); // NOI18N
        male.setName("male"); // NOI18N
        male.setOpaque(false);

        buttonGroup1.add(female);
        female.setFont(female.getFont().deriveFont(female.getFont().getStyle() & ~java.awt.Font.BOLD));
        female.setText(bundle.getString("ContactPanel.female.text")); // NOI18N
        female.setName("female"); // NOI18N
        female.setOpaque(false);

        taxnumber.set_Label(bundle.getString("ContactPanel.taxnumber._Label")); // NOI18N
        taxnumber.setFont(taxnumber.getFont().deriveFont(taxnumber.getFont().getStyle() | java.awt.Font.BOLD));
        taxnumber.setName("taxnumber"); // NOI18N

        department.set_Label(bundle.getString("ContactPanel.department._Label")); // NOI18N
        department.setFont(department.getFont().deriveFont(department.getFont().getStyle() | java.awt.Font.BOLD));
        department.setName("department"); // NOI18N

        jLabel5.setFont(new java.awt.Font("Dialog", 0, 12));
        jLabel5.setText(bundle.getString("ContactPanel.jLabel5.text")); // NOI18N
        jLabel5.setName("jLabel5"); // NOI18N

        countryselect.setName("countryselect"); // NOI18N
        countryselect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                countryselectActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(street, javax.swing.GroupLayout.PREFERRED_SIZE, 221, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(title, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 171, Short.MAX_VALUE)
                                    .addComponent(male))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(female)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(prename, javax.swing.GroupLayout.Alignment.TRAILING, 0, 0, Short.MAX_VALUE)
                            .addComponent(city, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 182, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(cname, 0, 0, Short.MAX_VALUE)
                            .addComponent(zip, javax.swing.GroupLayout.DEFAULT_SIZE, 174, Short.MAX_VALUE)))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(department, javax.swing.GroupLayout.DEFAULT_SIZE, 267, Short.MAX_VALUE)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(companyselect, javax.swing.GroupLayout.PREFERRED_SIZE, 201, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(jLabel5)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(countryselect, javax.swing.GroupLayout.PREFERRED_SIZE, 182, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(taxnumber, javax.swing.GroupLayout.DEFAULT_SIZE, 244, Short.MAX_VALUE))))
                .addContainerGap(49, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel3)
                        .addComponent(countryselect, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(companyselect, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jLabel5))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(department, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(taxnumber, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(male)
                    .addComponent(female))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(prename, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cname, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(title, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(city, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(zip, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(street, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        addresspanel.addTab(bundle.getString("ContactPanel.jPanel2.TabConstraints.tabTitle"), jPanel2); // NOI18N

        button_order1.setFont(button_order1.getFont().deriveFont(button_order1.getFont().getStyle() & ~java.awt.Font.BOLD, button_order1.getFont().getSize()-2));
        button_order1.setText(bundle.getString("ContactPanel.button_order1.text")); // NOI18N
        button_order1.setFocusable(false);
        button_order1.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        button_order1.setName("button_order1"); // NOI18N
        button_order1.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        button_order1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                button_order1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout rightpaneLayout = new javax.swing.GroupLayout(rightpane);
        rightpane.setLayout(rightpaneLayout);
        rightpaneLayout.setHorizontalGroup(
            rightpaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, rightpaneLayout.createSequentialGroup()
                .addGroup(rightpaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel9, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, rightpaneLayout.createSequentialGroup()
                        .addComponent(jLabel9)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(button_order1))
                    .addComponent(jLabel8, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel7, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel4, javax.swing.GroupLayout.Alignment.LEADING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 665, Short.MAX_VALUE)
                    .addComponent(addresspanel, javax.swing.GroupLayout.Alignment.LEADING, 0, 0, Short.MAX_VALUE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, rightpaneLayout.createSequentialGroup()
                        .addComponent(jToolBar1, javax.swing.GroupLayout.PREFERRED_SIZE, 435, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(prinitingComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        rightpaneLayout.setVerticalGroup(
            rightpaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(rightpaneLayout.createSequentialGroup()
                .addGroup(rightpaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jToolBar1, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(prinitingComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(rightpaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel9)
                    .addComponent(button_order1, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(addresspanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel8)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel7)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        toolbarpane.setName("toolbarpane"); // NOI18N
        toolbarpane.setLayout(new java.awt.BorderLayout());

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(leftpane, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addComponent(rightpane, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(toolbarpane, javax.swing.GroupLayout.DEFAULT_SIZE, 693, Short.MAX_VALUE)))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(leftpane, javax.swing.GroupLayout.DEFAULT_SIZE, 607, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addComponent(toolbarpane, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(rightpane, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void companyItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_companyItemStateChanged
        isCompany(company.isSelected());
}//GEN-LAST:event_companyItemStateChanged

    private void manufacturerItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_manufacturerItemStateChanged
        if (!supplier.isSelected()) {
            isManufacturer(manufacturer.isSelected());
        }
}//GEN-LAST:event_manufacturerItemStateChanged

    private void supplierItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_supplierItemStateChanged
        isSupplier(supplier.isSelected());
}//GEN-LAST:event_supplierItemStateChanged

    private void enabledItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_enabledItemStateChanged
        isenabled_ = enabled.isSelected();
}//GEN-LAST:event_enabledItemStateChanged

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
        p.setDataOwner(p.getDataOwner());
        p.setDataParent(dataOwner);
        addresspanel.add(p);
        addresspanel.setSelectedComponent(p);
}//GEN-LAST:event_button_order1ActionPerformed

    private void companyselectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_companyselectActionPerformed
}//GEN-LAST:event_companyselectActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        if (dataOwner.isExisting()) {
            addfile.setEnabled(true);
            removefile.setEnabled(true);
            fillFiles();
        }
    }//GEN-LAST:event_jButton1ActionPerformed

    private void addfileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addfileActionPerformed
        if (dataOwner.isExisting()) {
            addFile();
        }
    }//GEN-LAST:event_addfileActionPerformed

    private void dataTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_dataTableMouseClicked
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


    }//GEN-LAST:event_dataTableMouseClicked

    private void removefileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removefileActionPerformed
        deleteFile();
    }//GEN-LAST:event_removefileActionPerformed

    private void button_order2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_button_order2ActionPerformed
        BigPopup.showPopup(this, new ControlPanel_Groups());
    }//GEN-LAST:event_button_order2ActionPerformed

    private void countryselectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_countryselectActionPerformed
}//GEN-LAST:event_countryselectActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed

        if (dataOwner.isExisting()) {
            MPV5View.identifierView.addTab(new JournalPanel(), Messages.JOURNAL + getDataOwner().__getCName());
        }
    }//GEN-LAST:event_jButton2ActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel addedby;
    private javax.swing.JButton addfile;
    private javax.swing.JTabbedPane addresspanel;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JButton button_bill;
    private javax.swing.JButton button_bills;
    private javax.swing.JButton button_offer;
    private javax.swing.JButton button_offers;
    private javax.swing.JButton button_order;
    private javax.swing.JButton button_order1;
    private javax.swing.JButton button_order2;
    private javax.swing.JButton button_orders;
    private javax.swing.JButton button_product;
    private javax.swing.JButton button_products;
    private mpv5.ui.beans.LabeledTextField city;
    private mpv5.ui.beans.LabeledTextField cname;
    private javax.swing.JCheckBox company;
    private javax.swing.JComboBox companyselect;
    private javax.swing.JComboBox countryselect;
    private javax.swing.JCheckBox customer;
    private javax.swing.JTable dataTable;
    private javax.swing.JLabel dateadded;
    private mpv5.ui.beans.LabeledTextField department;
    private javax.swing.JCheckBox enabled;
    private mpv5.ui.beans.LabeledTextField fax;
    private javax.swing.JRadioButton female;
    private javax.swing.JComboBox groupnameselect;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JToolBar.Separator jSeparator1;
    private javax.swing.JToolBar.Separator jSeparator2;
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JPanel leftpane;
    private mpv5.ui.beans.LabeledTextField mailaddress;
    private mpv5.ui.beans.LabeledTextField mainphone;
    private javax.swing.JRadioButton male;
    private javax.swing.JCheckBox manufacturer;
    private mpv5.ui.beans.LabeledTextField mobilephone;
    private javax.swing.JTextPane notes;
    private mpv5.ui.beans.LabeledTextField number;
    private mpv5.ui.beans.LabeledTextField prename;
    private mpv5.ui.beans.PrinitingComboBox prinitingComboBox1;
    private javax.swing.JButton removefile;
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
    private javax.swing.table.DefaultTableModel tableModel = null;
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
    public int groupsids_ = 1;
    public String country_;

    public void collectData() {
        city_ = city.get_Text();
        cname_ = cname.get_Text();
        taxnumber_ = taxnumber.get_Text();
        iscompany_ = company.isSelected();
        if (companyselect.getSelectedItem() != null) {
            company_ = String.valueOf(((MPComboBoxModelItem) companyselect.getSelectedItem()).getValue());
        } else {
            company_ = "";
        }

        if (groupnameselect.getSelectedItem() != null) {
            groupsids_ = Integer.valueOf(((MPComboBoxModelItem) groupnameselect.getSelectedItem()).getId());
        } else {
            groupsids_ = 1;
        }

        if (countryselect.getSelectedItem() != null) {
            country_ = String.valueOf(((MPComboBoxModelItem) countryselect.getSelectedItem()).getValue());
        } else {
            country_ = "";
        }

        iscustomer_ = customer.isSelected();
        isenabled_ = enabled.isSelected();
        fax_ = fax.get_Text();
        mailaddress_ = mailaddress.get_Text();
        ismale_ = male.isSelected();
        ismanufacturer_ = manufacturer.isSelected();
        mobilephone_ = mobilephone.get_Text();
        notes_ = notes.getText();
        cnumber_ = number.get_Text();
        mainphone_ = mainphone.get_Text();
        prename_ = prename.get_Text();
        street_ = street.get_Text();
        issupplier_ = supplier.isSelected();
        title_ = title.get_Text();
        website_ = website.get_Text();
        workphone_ = workphone.get_Text();
        zip_ = zip.get_Text();
        if (dateadded_ == null) {
            dateadded_ = new Date();
        }
        intaddedby_ = User.getUserId(addedby.getText());
        department_ = department.get_Text();
    }
//ids label?

    public void exposeData() {
        city.set_Text(city_);
        cname.set_Text(cname_);
        taxnumber.set_Text(taxnumber_);
        company.setSelected(iscompany_);
        try {
            companyselect.setSelectedIndex(MPComboBoxModelItem.getItemIDfromValue(company_, companyselect.getModel()));
            groupnameselect.setSelectedIndex(MPComboBoxModelItem.getItemID(groupsids_, groupnameselect.getModel()));
            countryselect.setSelectedIndex(MPComboBoxModelItem.getItemIDfromValue(country_, countryselect.getModel()));
        } catch (Exception e) {
        }
        customer.setSelected(iscustomer_);
        enabled.setSelected(isenabled_);
        fax.set_Text(fax_);
        mailaddress.set_Text(mailaddress_);
        male.setSelected(ismale_);
        female.setSelected(!ismale_);
        manufacturer.setSelected(ismanufacturer_);
        mobilephone.set_Text(mobilephone_);
        notes.setText(notes_);
        number.set_Text(cnumber_);
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

//        dataTable.setModel(new MPTableModel(new DatabaseSearch(Context.getCompany()).getValuesFor(prename_, fax_, phone_), header));
    }

    public void refresh() {
        Runnable runnable = new Runnable() {

            public void run() {
                try {
                    companyselect.setModel(new DefaultComboBoxModel(ArrayUtilities.merge(new Object[]{new MPComboBoxModelItem("<no_value>", "")},
                            MPComboBoxModelItem.toItems(new DatabaseSearch(Context.getCompany()).getValuesFor(Context.getCompany().getSubID(), null, "")))));

                    if (!MPV5View.getUser().__getIsrgrouped()) {
                        groupnameselect.setModel(new DefaultComboBoxModel(
                                MPComboBoxModelItem.toItems(new DatabaseSearch(Context.getGroup()).getValuesFor(Context.getGroup().getSubID(), null, ""))));
                    } else {
                        groupnameselect.setModel(new DefaultComboBoxModel(
                                MPComboBoxModelItem.toItems(new DatabaseSearch(Context.getGroup()).getValuesFor(Context.getGroup().getSubID(), "ids", MPV5View.getUser().__getGroupsids()))));
                    }

                    groupnameselect.setSelectedIndex(MPComboBoxModelItem.getItemID(MPV5View.getUser().__getGroupsids(), groupnameselect.getModel()));

                    sp.refresh();

                    if (jButton1.isEnabled()) {
                        fillFiles();
                    }

                    countryselect.setModel(LanguageManager.getCountriesAsComboBoxModel());
                    countryselect.setSelectedIndex(MPComboBoxModelItem.getItemIDfromValue(MPV5View.getUser().__getDefcountry(), countryselect.getModel()));

                } catch (Exception e) {
                    Log.Debug(this, e);
                }
            }
        };

        SwingUtilities.invokeLater(runnable);
    }

    private void isCustomer(boolean b) {
        customer.setSelected(b);
        button_offers.setEnabled(b);
        button_offer.setEnabled(b);
        button_bill.setEnabled(b);
        button_bills.setEnabled(b);
    }

    private void isManufacturer(boolean b) {
        manufacturer.setSelected(b);
        button_product.setEnabled(b);
        button_products.setEnabled(b);
        button_order.setEnabled(b);
        button_orders.setEnabled(b);
    }

    private void isSupplier(boolean b) {
        supplier.setSelected(b);
        button_product.setEnabled(b);
        button_products.setEnabled(b);
        button_order.setEnabled(b);
        button_orders.setEnabled(b);
    }

    private void isCompany(boolean selected) {
        try {
            if (selected) {
                companyselect.setSelectedIndex(0);
            }
        } catch (Exception e) {
        }
        company.setSelected(selected);
        companyselect.setEnabled(!selected);
        title.setEnabled(!selected);
        male.setEnabled(!selected);
        female.setEnabled(!selected);
        prename.setEnabled(!selected);
        department.setEnabled(!selected);

    }

    public void paste(DatabaseObject dbo) {
        if (dbo.getDbIdentity().equals(Context.getContact().getDbIdentity())) {
            setDataOwner(dbo);
        } else {
            MPV5View.addMessage(Messages.NOT_POSSIBLE.toString() + Messages.ACTION_PASTE.toString());
        }
    }

    public void showSearchBar(boolean show) {
        leftpane.removeAll();
        if (show) {
            leftpane.add(sp, BorderLayout.CENTER);
        }

        validate();
    }
}
