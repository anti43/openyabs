/*
This file is part of MP.

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
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
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
import mpv5.db.objects.Item;
import mpv5.handler.FormatHandler;
import mpv5.logging.Log;
import mpv5.i18n.LanguageManager;
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
 * 
 */
public class ItemPanel extends javax.swing.JPanel implements DataPanel {

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
    private Integer dataTableContent = null;

    /** Creates new form ContactPanel
     * @param context
     */
    public ItemPanel(Context context) {
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

    @Override
    public DatabaseObject getDataOwner() {
        return dataOwner;
    }

    @Override
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
        tb.setEditable(!object.isReadOnly());

        dataTable.setModel(new MPTableModel());
        if (object.isReadOnly()) {
            Popup.notice(Messages.LOCKED_BY);
        }
    }



    public void showRequiredFields() {
//        TextFieldUtils.blinkerRed(cname);
//        cname.requestFocus();
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

    private void itemTableClicked(MouseEvent evt) {
        if (evt.getClickCount() > 1) {
            try {
                MPV5View.identifierView.addTab(DatabaseObject.getObject(Context.getItems(), Integer.valueOf(dataTable.getModel().getValueAt(dataTable.getSelectedRow(), 0).toString())));
            } catch (NodataFoundException ex) {
                Log.Debug(ex);
            }
        }
    }

    private void productTableClicked(MouseEvent evt) {
        if (evt.getClickCount() > 1) {
            try {
                MPV5View.identifierView.addTab(DatabaseObject.getObject(Context.getProducts(), Integer.valueOf(dataTable.getModel().getValueAt(dataTable.getSelectedRow(), 0).toString())));
            } catch (NodataFoundException ex) {
                Log.Debug(ex);
            }
        }
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
        number = new mpv5.ui.beans.LabeledTextField();
        dateadded = new javax.swing.JLabel();
        addedby = new javax.swing.JLabel();
        groupnameselect = new javax.swing.JComboBox();
        jLabel4 = new javax.swing.JLabel();
        button_order2 = new javax.swing.JButton();
        status = new mpv5.ui.beans.LabeledCombobox();
        Account = new mpv5.ui.beans.LabeledCombobox();
        jPanel4 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        notes = new javax.swing.JTextPane();
        jPanel3 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        companyselect = new javax.swing.JComboBox();
        jLabel3 = new javax.swing.JLabel();
        taxnumber = new mpv5.ui.beans.LabeledTextField();
        department = new mpv5.ui.beans.LabeledTextField();
        jLabel5 = new javax.swing.JLabel();
        countryselect = new javax.swing.JComboBox();
        jLabel7 = new javax.swing.JLabel();
        jPanel9 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        dataTable = new javax.swing.JTable();
        removefile = new javax.swing.JButton();
        addfile = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();
        jToolBar1 = new javax.swing.JToolBar();
        type = new javax.swing.JLabel();
        jSeparator3 = new javax.swing.JToolBar.Separator();
        button_offer = new javax.swing.JButton();
        button_order = new javax.swing.JButton();
        button_bill = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JToolBar.Separator();
        button_product = new javax.swing.JButton();
        jSeparator2 = new javax.swing.JToolBar.Separator();
        jButton2 = new javax.swing.JButton();
        prinitingComboBox1 = new mpv5.ui.beans.PrinitingComboBox();
        toolbarpane = new javax.swing.JPanel();

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("mpv5/resources/languages/Panels"); // NOI18N
        setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("ItemPanel.border.title_1"))); // NOI18N
        setName("Form"); // NOI18N

        leftpane.setName("leftpane"); // NOI18N
        leftpane.setLayout(new java.awt.BorderLayout());

        rightpane.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("ItemPanel.rightpane.border.title"))); // NOI18N
        rightpane.setName("rightpane"); // NOI18N

        jPanel1.setBackground(new java.awt.Color(227, 219, 202));
        jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel1.setName("jPanel1"); // NOI18N

        number.set_Label(bundle.getString("ItemPanel.number._Label")); // NOI18N
        number.setFocusable(false);
        number.setFont(number.getFont());
        number.setName("number"); // NOI18N

        dateadded.setFont(dateadded.getFont());
        dateadded.setText(bundle.getString("ItemPanel.dateadded.text")); // NOI18N
        dateadded.setToolTipText(bundle.getString("ItemPanel.dateadded.toolTipText")); // NOI18N
        dateadded.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        dateadded.setEnabled(false);
        dateadded.setName("dateadded"); // NOI18N

        addedby.setFont(addedby.getFont());
        addedby.setText(bundle.getString("ItemPanel.addedby.text")); // NOI18N
        addedby.setToolTipText(bundle.getString("ItemPanel.addedby.toolTipText")); // NOI18N
        addedby.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        addedby.setEnabled(false);
        addedby.setName("addedby"); // NOI18N

        groupnameselect.setName("groupnameselect"); // NOI18N

        jLabel4.setFont(jLabel4.getFont());
        jLabel4.setText(bundle.getString("ItemPanel.jLabel4.text")); // NOI18N
        jLabel4.setName("jLabel4"); // NOI18N

        button_order2.setFont(button_order2.getFont().deriveFont(button_order2.getFont().getStyle() & ~java.awt.Font.BOLD, button_order2.getFont().getSize()-2));
        button_order2.setText(bundle.getString("ItemPanel.button_order2.text")); // NOI18N
        button_order2.setFocusable(false);
        button_order2.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        button_order2.setName("button_order2"); // NOI18N
        button_order2.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        button_order2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                button_order2ActionPerformed(evt);
            }
        });

        status.set_Label(bundle.getString("ItemPanel.status._Label")); // NOI18N
        status.setName("status"); // NOI18N

        Account.set_Label(bundle.getString("ItemPanel.Account._Label")); // NOI18N
        Account.setName("Account"); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(status, javax.swing.GroupLayout.DEFAULT_SIZE, 278, Short.MAX_VALUE)
                    .addComponent(number, javax.swing.GroupLayout.DEFAULT_SIZE, 278, Short.MAX_VALUE)
                    .addComponent(Account, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 278, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 137, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(dateadded, javax.swing.GroupLayout.PREFERRED_SIZE, 97, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(button_order2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(addedby, javax.swing.GroupLayout.PREFERRED_SIZE, 97, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(groupnameselect, javax.swing.GroupLayout.PREFERRED_SIZE, 222, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel4)
                        .addComponent(button_order2, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(status, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(Account, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(dateadded, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(addedby, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)))
                .addGap(3, 3, 3)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(groupnameselect, 0, 0, Short.MAX_VALUE)
                    .addComponent(number, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        jPanel4.setBackground(new java.awt.Color(204, 204, 204));
        jPanel4.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel4.setName("jPanel4"); // NOI18N

        jScrollPane1.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        jScrollPane1.setName("jScrollPane1"); // NOI18N

        notes.setText(bundle.getString("ItemPanel.notes.text")); // NOI18N
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

        jPanel2.setBackground(new java.awt.Color(227, 219, 202));
        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("ItemPanel.jPanel2.border.title"))); // NOI18N
        jPanel2.setName("jPanel2"); // NOI18N

        companyselect.setName("companyselect"); // NOI18N
        companyselect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                companyselectActionPerformed(evt);
            }
        });

        jLabel3.setFont(new java.awt.Font("Dialog", 0, 12));
        jLabel3.setText(bundle.getString("ItemPanel.jLabel3.text")); // NOI18N
        jLabel3.setName("jLabel3"); // NOI18N

        taxnumber.set_Label(bundle.getString("ItemPanel.taxnumber._Label")); // NOI18N
        taxnumber.setFont(taxnumber.getFont().deriveFont(taxnumber.getFont().getStyle() | java.awt.Font.BOLD));
        taxnumber.setName("taxnumber"); // NOI18N

        department.set_Label(bundle.getString("ItemPanel.department._Label")); // NOI18N
        department.setFont(department.getFont().deriveFont(department.getFont().getStyle() | java.awt.Font.BOLD));
        department.setName("department"); // NOI18N

        jLabel5.setFont(new java.awt.Font("Dialog", 0, 12));
        jLabel5.setText(bundle.getString("ItemPanel.jLabel5.text")); // NOI18N
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
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(department, javax.swing.GroupLayout.DEFAULT_SIZE, 282, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, 69, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(companyselect, javax.swing.GroupLayout.PREFERRED_SIZE, 201, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, 69, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(countryselect, javax.swing.GroupLayout.PREFERRED_SIZE, 222, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(taxnumber, javax.swing.GroupLayout.DEFAULT_SIZE, 303, Short.MAX_VALUE))
                .addContainerGap(41, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(countryselect, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(companyselect, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jLabel5)
                    .addComponent(jLabel3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(department, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(taxnumber, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(194, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 661, Short.MAX_VALUE)
            .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel3Layout.createSequentialGroup()
                    .addGap(0, 0, Short.MAX_VALUE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(0, 1, Short.MAX_VALUE)))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 262, Short.MAX_VALUE)
            .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel3Layout.createSequentialGroup()
                    .addGap(0, 0, 0)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGap(0, 0, 0)))
        );

        jLabel7.setFont(jLabel7.getFont());
        jLabel7.setText(bundle.getString("ItemPanel.jLabel7.text")); // NOI18N
        jLabel7.setName("jLabel7"); // NOI18N

        jPanel9.setBackground(new java.awt.Color(204, 204, 204));
        jPanel9.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel9.setName("jPanel9"); // NOI18N

        jScrollPane2.setName("jScrollPane2"); // NOI18N

        dataTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        dataTable.setName("dataTable"); // NOI18N
        jScrollPane2.setViewportView(dataTable);

        removefile.setIcon(new javax.swing.ImageIcon(getClass().getResource("/mpv5/resources/images/16/remove.png"))); // NOI18N
        removefile.setText(bundle.getString("ItemPanel.removefile.text")); // NOI18N
        removefile.setEnabled(false);
        removefile.setName("removefile"); // NOI18N
        removefile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removefileActionPerformed(evt);
            }
        });

        addfile.setIcon(new javax.swing.ImageIcon(getClass().getResource("/mpv5/resources/images/16/add.png"))); // NOI18N
        addfile.setText(bundle.getString("ItemPanel.addfile.text")); // NOI18N
        addfile.setEnabled(false);
        addfile.setName("addfile"); // NOI18N
        addfile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addfileActionPerformed(evt);
            }
        });

        jButton1.setText(bundle.getString("ItemPanel.jButton1.text")); // NOI18N
        jButton1.setName("jButton1"); // NOI18N
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel9Layout.createSequentialGroup()
                .addContainerGap(251, Short.MAX_VALUE)
                .addComponent(jButton1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(addfile, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(removefile, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 168, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(113, 113, 113))
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel9Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 121, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel9Layout.createSequentialGroup()
                        .addGap(27, 27, 27)
                        .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(removefile, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(addfile))))
                .addContainerGap(49, Short.MAX_VALUE))
        );

        jToolBar1.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jToolBar1.setFloatable(false);
        jToolBar1.setRollover(true);
        jToolBar1.setName("jToolBar1"); // NOI18N

        type.setForeground(new java.awt.Color(51, 0, 0));
        type.setText(bundle.getString("ItemPanel.type.text")); // NOI18N
        type.setName("type"); // NOI18N
        jToolBar1.add(type);

        jSeparator3.setName("jSeparator3"); // NOI18N
        jToolBar1.add(jSeparator3);

        button_offer.setText(bundle.getString("ItemPanel.button_offer.text")); // NOI18N
        button_offer.setEnabled(false);
        button_offer.setFocusable(false);
        button_offer.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        button_offer.setName("button_offer"); // NOI18N
        button_offer.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(button_offer);

        button_order.setText(bundle.getString("ItemPanel.button_order.text")); // NOI18N
        button_order.setEnabled(false);
        button_order.setFocusable(false);
        button_order.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        button_order.setName("button_order"); // NOI18N
        button_order.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(button_order);

        button_bill.setText(bundle.getString("ItemPanel.button_bill.text")); // NOI18N
        button_bill.setEnabled(false);
        button_bill.setFocusable(false);
        button_bill.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        button_bill.setName("button_bill"); // NOI18N
        button_bill.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(button_bill);

        jSeparator1.setName("jSeparator1"); // NOI18N
        jToolBar1.add(jSeparator1);

        button_product.setText(bundle.getString("ItemPanel.button_product.text")); // NOI18N
        button_product.setEnabled(false);
        button_product.setFocusable(false);
        button_product.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        button_product.setName("button_product"); // NOI18N
        button_product.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(button_product);

        jSeparator2.setName("jSeparator2"); // NOI18N
        jToolBar1.add(jSeparator2);

        jButton2.setText(bundle.getString("ItemPanel.jButton2.text")); // NOI18N
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

        javax.swing.GroupLayout rightpaneLayout = new javax.swing.GroupLayout(rightpane);
        rightpane.setLayout(rightpaneLayout);
        rightpaneLayout.setHorizontalGroup(
            rightpaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, rightpaneLayout.createSequentialGroup()
                .addGroup(rightpaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel9, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel4, javax.swing.GroupLayout.Alignment.LEADING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(rightpaneLayout.createSequentialGroup()
                        .addComponent(jToolBar1, javax.swing.GroupLayout.DEFAULT_SIZE, 491, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(prinitingComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jLabel7, javax.swing.GroupLayout.Alignment.LEADING))
                .addContainerGap())
        );
        rightpaneLayout.setVerticalGroup(
            rightpaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(rightpaneLayout.createSequentialGroup()
                .addGroup(rightpaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(prinitingComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jToolBar1, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel7)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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
            .addComponent(leftpane, javax.swing.GroupLayout.DEFAULT_SIZE, 687, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addComponent(toolbarpane, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(rightpane, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void countryselectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_countryselectActionPerformed

}//GEN-LAST:event_countryselectActionPerformed

    private void companyselectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_companyselectActionPerformed

}//GEN-LAST:event_companyselectActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed

        if (dataOwner.isExisting()) {
            MPV5View.identifierView.addTab(new JournalPanel(), Messages.JOURNAL + getDataOwner().__getCName());
        }
}//GEN-LAST:event_jButton2ActionPerformed

    private void button_order2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_button_order2ActionPerformed
        BigPopup.showPopup(this, new ControlPanel_Groups());
}//GEN-LAST:event_button_order2ActionPerformed

    private void removefileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removefileActionPerformed
        deleteFile();
}//GEN-LAST:event_removefileActionPerformed

    private void addfileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addfileActionPerformed
        if (dataOwner.isExisting()) {
            addFile();
        }
}//GEN-LAST:event_addfileActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        if (dataOwner.isExisting()) {
            dataTableContent = FILES;
            addfile.setEnabled(true);
            removefile.setEnabled(true);
            fillFiles();
        }
}//GEN-LAST:event_jButton1ActionPerformed
    private static int FILES = 0;
    private static int PRODUCTS = 1;
    private static int ITEM = 2;

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private mpv5.ui.beans.LabeledCombobox Account;
    private javax.swing.JLabel addedby;
    private javax.swing.JButton addfile;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JButton button_bill;
    private javax.swing.JButton button_offer;
    private javax.swing.JButton button_order;
    private javax.swing.JButton button_order2;
    private javax.swing.JButton button_product;
    private javax.swing.JComboBox companyselect;
    private javax.swing.JComboBox countryselect;
    private javax.swing.JTable dataTable;
    private javax.swing.JLabel dateadded;
    private mpv5.ui.beans.LabeledTextField department;
    private javax.swing.JComboBox groupnameselect;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JToolBar.Separator jSeparator1;
    private javax.swing.JToolBar.Separator jSeparator2;
    private javax.swing.JToolBar.Separator jSeparator3;
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JPanel leftpane;
    private javax.swing.JTextPane notes;
    private mpv5.ui.beans.LabeledTextField number;
    private mpv5.ui.beans.PrinitingComboBox prinitingComboBox1;
    private javax.swing.JButton removefile;
    private javax.swing.JPanel rightpane;
    private mpv5.ui.beans.LabeledCombobox status;
    private mpv5.ui.beans.LabeledTextField taxnumber;
    private javax.swing.JPanel toolbarpane;
    private javax.swing.JLabel type;
    // End of variables declaration//GEN-END:variables

    public String cname_;
    public int intaddedby_;
    public int ids_;
    public Date dateadded_;
    public int groupsids_ = 1;


    @Override
    public void collectData() {
       

        if (groupnameselect.getSelectedItem() != null) {
            groupsids_ = Integer.valueOf(((MPComboBoxModelItem) groupnameselect.getSelectedItem()).getId());
        } else {
            groupsids_ = 1;
        }

        if (dateadded_ == null) {
            dateadded_ = new Date();
        }
        intaddedby_ = User.getUserId(addedby.getText());


        cname_ = dataOwner.getFormatHandler().toString(dataOwner.getFormatHandler().getNextNumber());
    }


    public void exposeData() {
      
//        cname.set_Text(cname_);

        try {
          
            groupnameselect.setSelectedIndex(MPComboBoxModelItem.getItemID(groupsids_, groupnameselect.getModel()));

        } catch (Exception e) {
        }

        dateadded.setText(DateConverter.getDefDateString(dateadded_));
        addedby.setText(User.getUsername(intaddedby_));
  

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
