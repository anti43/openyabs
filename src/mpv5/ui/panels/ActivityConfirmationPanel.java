package mpv5.ui.panels;

import enoa.handler.TemplateHandler;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.TableCellRenderer;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.db.common.NodataFoundException;
import mpv5.db.objects.ActivityList;
import mpv5.db.objects.ActivityListSubItem;
import mpv5.db.objects.Contact;
import mpv5.db.objects.Favourite;
import mpv5.db.objects.Item;
import mpv5.db.objects.Product;
import mpv5.db.objects.User;
import mpv5.globals.Messages;
import mpv5.logging.Log;
import mpv5.ui.beans.LightMPComboBox;
import mpv5.ui.dialogs.DialogForFile;
import mpv5.ui.dialogs.Popup;
import mpv5.ui.dialogs.subcomponents.ActivityTextAreaDialog;
import mpv5.ui.misc.TextFieldUtils;
import mpv5.ui.toolbars.DataPanelTB;
import mpv5.utils.arrays.ArrayUtilities;
import mpv5.utils.export.Export;
import mpv5.utils.jobs.Job;
import mpv5.utils.models.MPComboBoxModelItem;
import mpv5.utils.models.MPTableModel;
import mpv5.utils.renderer.TableCellEditorForDate;
import mpv5.utils.renderer.TableCellRendererForDezimal;
import mpv5.utils.renderer.TextAreaCellEditor;
import mpv5.utils.renderer.TextAreaCellRenderer;
import mpv5.utils.tables.DynamicTableCalculator;
import mpv5.utils.tables.TableFormat;

public final class ActivityConfirmationPanel extends javax.swing.JPanel implements DataPanel, ExportablePanel {

    private static final long serialVersionUID = 1L;
    private java.util.ResourceBundle bundle = mpv5.i18n.LanguageManager.getBundle();
    private static ActivityConfirmationPanel me;
    private ActivityList dataOwner;
    private DataPanelTB tb;
    private final SearchPanel sp;
    private MPTableModel omodel = null;
    Thread t = null;

    /**
     * Singleton
     * @return
     */
    public static ActivityConfirmationPanel instanceOf() {
        if (me == null) {
            me = new ActivityConfirmationPanel();
        }
        return me;
    }
    // Variablen ...
    public int contactsids_;
    public int orderids_;
    public BigDecimal totalamount_;
    public String cnumber_;
    public String cname_;
    public String description_;
    public int intaddedby_;
    public int ids_;
    public Date dateadded_;
    public int groupsids_ = 1;
    public boolean asproduct_;
    private DynamicTableCalculator itemMultiplier;
    private LightMPComboBox cb = new LightMPComboBox();
    private boolean HEADER_FAILD = false;

    /** Creates new form ListPanel */
    private ActivityConfirmationPanel() {
        initComponents();
        setName("ActivityConfirmation");
        dataOwner = new ActivityList();
        itemtable.getTableHeader().setReorderingAllowed(false);
        sp = new SearchPanel(Context.getActivityList(), this);
        sp.setVisible(true);
        tb = new DataPanelTB(this);
        toolbarpanetbp.add(tb, BorderLayout.CENTER);
        contact.setContext(Context.getContact());
        contact.setSearchEnabled(true);
        contact.getComboBox().addActionListener(new ActionListener() {

            @Override
            @SuppressWarnings("unchecked")
            public void actionPerformed(ActionEvent e) {
                if (e.getModifiers() == 16) {
                    final MPComboBoxModelItem item = contact.getSelectedItem();
                    if (item != null && item.isValid()) {
                        t = new Thread() {

                            @Override
                            public void run() {
                                try {
                                    Log.Debug(this, "Kontakt Zeile :" + item.getId() + " ausgewählt.");
                                    Contact ct = (Contact) DatabaseObject.getObject(Context.getContact(), Integer.valueOf(item.getId()));
                                    List data = DatabaseObject.getReferencedObjects(ct,
                                            Context.getOrder());
                                    order.setModel(data);
                                } catch (NodataFoundException ex) {
                                    Log.Debug(this, ex);
                                }
                            }
                        };
                        t.start();
                    }
                }
            }
        });
        refresh();
        preloadTemplate();
        number.set_ValueClass(Integer.class);
    }

    /** This me4thod is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        contact = new mpv5.ui.beans.LabeledCombobox();
        order = new mpv5.ui.beans.LabeledCombobox();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        Project = new mpv5.ui.beans.LabeledTextField();
        number = new mpv5.ui.beans.LabeledTextField();
        jScrollPane4 = new javax.swing.JScrollPane();
        itemtable = new JTable() {
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
        itemPanel = new javax.swing.JPanel();
        addItem = new javax.swing.JButton();
        delItem = new javax.swing.JButton();
        upItem = new javax.swing.JButton();
        upItem1 = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        total = new javax.swing.JTextField();
        toolbarpanetbp = new javax.swing.JPanel();
        SearchBarPane = new javax.swing.JPanel();

        setName("Form"); // NOI18N
        setLayout(new java.awt.BorderLayout());

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("mpv5/resources/languages/Panels"); // NOI18N
        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("ActivityConfirmationPanel.jPanel1.border.title"))); // NOI18N
        jPanel1.setName("jPanel1"); // NOI18N

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("ActivityConfirmationPanel.jPanel2.border.title"))); // NOI18N
        jPanel2.setName("jPanel2"); // NOI18N

        contact.set_Label(bundle.getString("ActivityConfirmationPanel.contact._Label")); // NOI18N
        contact.setName("contact"); // NOI18N

        order.set_Label(bundle.getString("ActivityConfirmationPanel.order._Label")); // NOI18N
        order.setName("order"); // NOI18N

        jButton2.setText(bundle.getString("ActivityConfirmationPanel.jButton2.text")); // NOI18N
        jButton2.setName("jButton2"); // NOI18N
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jButton3.setText(bundle.getString("ActivityConfirmationPanel.jButton3.text")); // NOI18N
        jButton3.setName("jButton3"); // NOI18N
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        Project.set_Label(bundle.getString("ActivityConfirmationPanel.Project._Label")); // NOI18N
        Project.setName("Project"); // NOI18N

        number.set_Label(bundle.getString("ActivityConfirmationPanel.number._Label")); // NOI18N
        number.setFocusable(false);
        number.setFont(number.getFont());
        number.setName("number"); // NOI18N

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(contact, javax.swing.GroupLayout.DEFAULT_SIZE, 360, Short.MAX_VALUE)
                    .addComponent(number, javax.swing.GroupLayout.DEFAULT_SIZE, 360, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 76, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(order, javax.swing.GroupLayout.PREFERRED_SIZE, 292, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 77, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(Project, javax.swing.GroupLayout.PREFERRED_SIZE, 457, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(Project, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(number, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(contact, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton3)
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(order, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jButton2))))
        );

        jScrollPane4.setName("jScrollPane4"); // NOI18N

        itemtable.setAutoCreateRowSorter(true);
        itemtable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        itemtable.setCellSelectionEnabled(true);
        itemtable.setName("itemtable"); // NOI18N
        itemtable.setSurrendersFocusOnKeystroke(true);
        jScrollPane4.setViewportView(itemtable);

        itemPanel.setName("itemPanel"); // NOI18N
        itemPanel.setOpaque(false);

        addItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/mpv5/resources/images/16/add.png"))); // NOI18N
        addItem.setName("addItem"); // NOI18N
        addItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addItemActionPerformed(evt);
            }
        });

        delItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/mpv5/resources/images/16/remove.png"))); // NOI18N
        delItem.setName("delItem"); // NOI18N
        delItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                delItemActionPerformed(evt);
            }
        });

        upItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/mpv5/resources/images/16/arrow-up.png"))); // NOI18N
        upItem.setEnabled(false);
        upItem.setName("upItem"); // NOI18N
        upItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                upItemActionPerformed(evt);
            }
        });

        upItem1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/mpv5/resources/images/16/arrow-down.png"))); // NOI18N
        upItem1.setEnabled(false);
        upItem1.setName("upItem1"); // NOI18N
        upItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                upItem1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout itemPanelLayout = new javax.swing.GroupLayout(itemPanel);
        itemPanel.setLayout(itemPanelLayout);
        itemPanelLayout.setHorizontalGroup(
            itemPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(addItem, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(delItem, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(upItem, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(upItem1, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        itemPanelLayout.setVerticalGroup(
            itemPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(itemPanelLayout.createSequentialGroup()
                .addComponent(addItem)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(delItem)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(upItem)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(upItem1)
                .addContainerGap(31, Short.MAX_VALUE))
        );

        jLabel1.setText(bundle.getString("ActivityConfirmationPanel.jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        total.setText(bundle.getString("ActivityConfirmationPanel.total.text")); // NOI18N
        total.setName("total"); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(itemPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 851, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(639, Short.MAX_VALUE)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(total, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(18, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(itemPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 149, Short.MAX_VALUE))
                .addGap(11, 11, 11)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(total, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        add(jPanel1, java.awt.BorderLayout.CENTER);

        toolbarpanetbp.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        toolbarpanetbp.setName("toolbarpanetbp"); // NOI18N
        toolbarpanetbp.setLayout(new java.awt.BorderLayout());
        add(toolbarpanetbp, java.awt.BorderLayout.NORTH);

        SearchBarPane.setName("SearchBarPane"); // NOI18N
        SearchBarPane.setLayout(new java.awt.BorderLayout());
        add(SearchBarPane, java.awt.BorderLayout.WEST);
    }// </editor-fold>//GEN-END:initComponents

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        try {
            int cid = Integer.valueOf(order.getSelectedItem().getId());
            Item i = (Item) DatabaseObject.getObject(Context.getOrder(), cid);
            ItemPanel ip = new ItemPanel(Context.getItem());
            mpv5.YabsViewProxy.instance().getIdentifierView().addOrShowTab(ip, Messages.TYPE_ORDER.toString());
            ip.setDataOwner(i, true);
        } catch (Exception e) {
            Log.Debug(this, e);
        }
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        try {
            int cid = Integer.valueOf(contact.getSelectedItem().getId());
            Contact c = (Contact) DatabaseObject.getObject(Context.getContact(), cid);
            ContactPanel cp = new ContactPanel(Context.getContact());
            mpv5.YabsViewProxy.instance().getIdentifierView().addOrShowTab(cp, Messages.TYPE_CONTACT.toString());
            cp.setDataOwner(c, true);
        } catch (Exception e) {
            Log.Debug(this, e);
        }
    }//GEN-LAST:event_jButton3ActionPerformed

    private void addItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addItemActionPerformed
        ((MPTableModel) itemtable.getModel()).addRow(1);
    }//GEN-LAST:event_addItemActionPerformed

    private void delItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_delItemActionPerformed
        int index = itemtable.getSelectedRow();
        if (index < 0) {
            return;
        }
        MPTableModel m = (MPTableModel) itemtable.getModel();
        ActivityListSubItem.addToDeletionQueue(m.getValueAt(index, 0));
        ((MPTableModel) itemtable.getModel()).removeRow(index);
    }//GEN-LAST:event_delItemActionPerformed

    private void upItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_upItemActionPerformed
        int index = itemtable.getSelectedRow();
        if (index <= 0) {
            return;
        }
        ((MPTableModel) itemtable.getModel()).moveRow(index, index, index - 1);
        itemtable.changeSelection(index - 1, itemtable.getSelectedColumn(), false, false);
    }//GEN-LAST:event_upItemActionPerformed

    private void upItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_upItem1ActionPerformed
        int index = itemtable.getSelectedRow();
        if (index < 0 || index >= itemtable.getRowCount() - 1) {
            return;
        }
        ((MPTableModel) itemtable.getModel()).moveRow(index, index, index + 1);
        itemtable.changeSelection(index + 1, itemtable.getSelectedColumn(), false, false);
    }//GEN-LAST:event_upItem1ActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private mpv5.ui.beans.LabeledTextField Project;
    private javax.swing.JPanel SearchBarPane;
    private javax.swing.JButton addItem;
    private mpv5.ui.beans.LabeledCombobox contact;
    private javax.swing.JButton delItem;
    private javax.swing.JPanel itemPanel;
    private javax.swing.JTable itemtable;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane4;
    private mpv5.ui.beans.LabeledTextField number;
    private mpv5.ui.beans.LabeledCombobox order;
    private javax.swing.JPanel toolbarpanetbp;
    private javax.swing.JTextField total;
    private javax.swing.JButton upItem;
    private javax.swing.JButton upItem1;
    // End of variables declaration//GEN-END:variables

    public void refresh() {
        Runnable runnable = new Runnable() {

            @Override
            public void run() {
                try {
                    contact.setSelectedIndex(0);
                    addModel();
                    formatTable();

                } catch (Exception e) {
                    Log.Debug(this, e);
                }
            }
        };

        SwingUtilities.invokeLater(runnable);
    }

    public boolean collectData() {
        cname_ = Project.getText();
        if (cname_.length() > 0) {
            try {
                groupsids_ = User.getCurrentUser().__getGroupsids();
                contactsids_ = Integer.parseInt(contact.getSelectedItem().getId());
                orderids_ = Integer.parseInt(order.getSelectedItem().getId());
                totalamount_ = new BigDecimal(total.getText().replace(",", "."));
                cnumber_ = number.getText();
                HEADER_FAILD = false;
            } catch (Exception exception) {
                Log.Debug(exception);
                HEADER_FAILD = true;
                return false;
            }

        } else {
            HEADER_FAILD = true;
            return false;
        }

        List<Object[]> rowsl = ((MPTableModel) itemtable.getModel()).getValidRows(new int[]{5});
        for (int i = 0; i < rowsl.size(); i++) {
            Object[] row = rowsl.get(i);
            if (row[2] == null || row[9].equals(0)) {
                Popup.error(this, Messages.ACTIVITY_EMPTY_DATE.toString());
                return false;
            }
        }
        return true;
    }

    public DatabaseObject getDataOwner() {
        return dataOwner;
    }

    public void setDataOwner(DatabaseObject object, boolean populateData) {
        dataOwner = (ActivityList) object;
        if (populateData) {
            dataOwner.setPanelData(this);
            this.exposeData();

            tb.setFavourite(Favourite.isFavourite(object));
            tb.setEditable(!object.isReadOnly());
            try {
                itemtable.setModel(ActivityListSubItem.toModel(ActivityListSubItem.getList(dataOwner.__getIDS()).toArray(new ActivityListSubItem[0])));
            } catch (NodataFoundException ex) {
                Log.Debug(this, ex.getMessage());
                addModel();
            }
            if (((MPTableModel) itemtable.getModel()).getEmptyRows(new int[]{4}) < 2) {
                ((MPTableModel) itemtable.getModel()).addRow(1);
            }
            omodel = (MPTableModel) itemtable.getModel();

            formatTable();
            ((MPTableModel) itemtable.getModel()).fireTableCellUpdated(0, 0);
            total.setText(ActivityListSubItem.getModelSum(omodel, dataOwner.__getIDS()).toString());
            if (object.isReadOnly()) {
                Popup.notice(Messages.LOCKED_BY);
            }
            validate();
            itemMultiplier.calculateOnce();
        }
    }

    public void exposeData() {
        Project.setText(cname_);

        try {
            contact.setModel(DatabaseObject.getObject(Context.getContact(), contactsids_));
            order.setModel(DatabaseObject.getObject(Context.getOrder(), orderids_));
            total.setText(totalamount_.toString());
            number.setText(cnumber_);
        } catch (NodataFoundException ex) {
            Log.Debug(this, ex.getMessage());
        }
    }

    public void paste(DatabaseObject... dbos) {
        for (DatabaseObject dbo : dbos) {
            if (dbo.getContext().equals(Context.getProduct())) {
                MPTableModel m = (MPTableModel) itemtable.getModel();
                m.addRow(ActivityListSubItem.toRow((Product) dbo).getRowData(m.getValidRows(new int[]{5}).size()));

                itemtable.setModel(m);
                omodel = m;

            } else if (dbo.getContext().equals(Context.getProductlist())) {
                setDataOwner(dbo, true);
            } else {
                mpv5.YabsViewProxy.instance().addMessage(Messages.NOT_POSSIBLE.toString() + Messages.ACTION_PASTE.toString());
            }
        }
    }

    public void showRequiredFields() {
        if (HEADER_FAILD) {
            TextFieldUtils.blink(Project,
                    Color.RED);
            TextFieldUtils.blink(contact,
                    Color.RED);
            TextFieldUtils.blink(order,
                    Color.RED);
            Project.requestFocus();
        }
    }

    public void showSearchBar(boolean show) {
        SearchBarPane.removeAll();

        if (show) {
            SearchBarPane.add(sp,
                    BorderLayout.CENTER);
            Log.Debug(this,
                    "Suchfenster eingeblendet");
        } else {
            Log.Debug(this,
                    "Suchfenster ausgeblendet");
        }

        validate();
    }

    public void actionAfterSave() {
        saveActivityListSubItems();
        omodel = (MPTableModel) itemtable.getModel();
    }

    public void actionAfterCreate() {
        ArrayUtilities.replaceColumn(itemtable, 0, null);
        saveActivityListSubItems();
        omodel = (MPTableModel) itemtable.getModel();
    }

    public void actionBeforeCreate() {
    }

    public void actionBeforeSave() throws ChangeNotApprovedException {
    }

    public void mail() {
        Popup.notice(Messages.NOT_YET_IMPLEMENTED.toString());
    }

    public void print() {
        if (dataOwner != null && dataOwner.isExisting()) {
            if (TemplateHandler.isLoaded(Long.valueOf(dataOwner.templateGroupIds()), dataOwner.templateType())) {
                Export.print(TemplateHandler.loadTemplate(dataOwner.templateGroupIds(), dataOwner.templateType()), dataOwner);
            } else {
                Popup.notice(Messages.NO_TEMPLATE_LOADED + " (" + mpv5.db.objects.User.getCurrentUser() + ")");
                Export.print(this);
            }
        } else {
            Export.print(this);
        }
    }

    private void saveActivityListSubItems() {
        if (itemtable.getCellEditor() != null) {
            try {
                itemtable.getCellEditor().stopCellEditing();
            } catch (Exception e) {
                Log.Debug(this, e);
            }
        }
        ActivityListSubItem.saveModel((MPTableModel) itemtable.getModel(), dataOwner.__getIDS());
    }

    /**
     * 
     */
    public void formatTable() {
        //"Internal ID", "ID", "Date", "Count", "Description", "Netto Price", "Tax Value", "Total Price", "Product", "cname"     
        prepareTable();
        TableFormat.resizeCols(itemtable, new Integer[]{0, 23, 80, 50, 50, 100, 60, 60, 60, 20, 0, 0}, new Boolean[]{true, true, true, true, true, false, true, true, true, true, true, true});
        TableFormat.changeBackground(itemtable, 1, Color.LIGHT_GRAY);
        if (mpv5.db.objects.User.getCurrentUser().getProperties().getProperty("org.openyabs.uiproperty", "hidecolumnquantity")) {
            TableFormat.stripColumn(itemtable, 4);
        }
    }

    private void prepareTable() {
        itemtable.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        //"Internal ID", "ID", "Date", "Count", "Description", "Netto Price", "Tax Value", "Total Price", "Product", "cname"     
        TableCellRendererForDezimal tcr = new TableCellRendererForDezimal(itemtable);
        TableCellEditorForDate de = new TableCellEditorForDate(itemtable);
        TextAreaCellEditor tace = new TextAreaCellEditor(itemtable);
        ActivityTextAreaDialog actTextAreaDialog = new ActivityTextAreaDialog(mpv5.YabsViewProxy.instance().getIdentifierFrame(), true);
        TextAreaCellRenderer.setMAXLINES(10);
        TextAreaCellRenderer tacr = new TextAreaCellRenderer(itemtable);
        actTextAreaDialog.setParentTable(itemtable);
        actTextAreaDialog.okButton.addActionListener(tace);
        actTextAreaDialog.cancelButton.addActionListener(tace);
        tace.setDialog(actTextAreaDialog, actTextAreaDialog.textArea);
        tace.setClickCountToStart(1);
        de.setEditorTo(2);
        tace.setEditorTo(5);
        tcr.setRendererTo(3);
        tacr.setRendererTo(5);
        tcr.setRendererTo(6);
        tcr.setRendererTo(7);
        tcr.setRendererTo(8);

        itemMultiplier = new DynamicTableCalculator(itemtable, "(([3]*[6])-([3]*[6]%[7]))", new int[]{8});
        ((MPTableModel) itemtable.getModel()).addCalculator(itemMultiplier);
        itemMultiplier.addLabel(total, 8);
    }

    public void pdf() {
        if (dataOwner != null && dataOwner.isExisting()) {
            if (TemplateHandler.isLoaded(Long.valueOf(dataOwner.templateGroupIds()), dataOwner.templateType())) {
                new Job(Export.createFile(dataOwner.getFormatHandler().toUserString(),
                        TemplateHandler.loadTemplate(
                        dataOwner.templateGroupIds(), dataOwner.templateType()),
                        dataOwner), new DialogForFile(User.getSaveDir(dataOwner))).execute();
            } else {
                Popup.notice(Messages.NO_TEMPLATE_LOADED + " (" + mpv5.db.objects.User.getCurrentUser() + ")");
            }
        }
    }

    public void odt() {
        if (dataOwner != null && dataOwner.isExisting()) {
            if (TemplateHandler.isLoaded(Long.valueOf(dataOwner.templateGroupIds()), dataOwner.templateType())) {
                new Job(Export.sourceFile(
                        dataOwner.getFormatHandler().toUserString(),
                        TemplateHandler.loadTemplate(dataOwner.templateGroupIds(), dataOwner.templateType()), dataOwner), new DialogForFile(User.getSaveDir(dataOwner))).execute();
            } else {
                Popup.notice(Messages.NO_TEMPLATE_LOADED + " (" + mpv5.db.objects.User.getCurrentUser() + ")");
            }
        }
    }

    private void addModel() {
        itemtable.setModel(ActivityListSubItem.toModel(new ActivityListSubItem[]{
                    ActivityListSubItem.getDefaultItem(), ActivityListSubItem.getDefaultItem(),
                    ActivityListSubItem.getDefaultItem(), ActivityListSubItem.getDefaultItem()
                }));

        formatTable();
    }

    private void preloadTemplate() {
        Runnable runnable = new Runnable() {

            public void run() {
                TemplateHandler.loadTemplate(Long.valueOf(dataOwner.templateGroupIds()), dataOwner.templateType());
            }
        };
        new Thread(runnable).start();
    }
}