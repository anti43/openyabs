package mpv5.ui.dialogs.subcomponents;

import enoa.handler.TableHandler;
import java.awt.Component;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import mpv5.data.PropertyStore;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.db.common.DatabaseSearch;
import mpv5.db.common.NodataFoundException;
import mpv5.db.common.QueryCriteria;
import mpv5.db.common.QueryData;
import mpv5.db.common.QueryHandler;
import mpv5.db.common.SaveString;
import mpv5.db.objects.Contact;
import mpv5.db.objects.Item;
import mpv5.db.objects.SubItem;
import mpv5.db.objects.Template;
import mpv5.globals.Headers;
import mpv5.globals.Messages;
import mpv5.logging.Log;
import mpv5.ui.dialogs.ControlApplet;
import mpv5.ui.dialogs.Popup;
import mpv5.ui.frames.MPView;
import mpv5.ui.panels.DataPanel;
import mpv5.usermanagement.MPSecurityManager;
import mpv5.db.objects.User;

import mpv5.globals.Constants;
import mpv5.handler.FormFieldsHandler;
import mpv5.ui.dialogs.DialogForFile;
import mpv5.ui.panels.PreviewPanel;
import mpv5.utils.export.Export;
import mpv5.utils.export.ODTFile;
import mpv5.utils.export.PDFFile;
import mpv5.utils.files.FileDirectoryHandler;
import mpv5.utils.jobs.Job;
import mpv5.utils.models.MPComboBoxModelItem;
import mpv5.utils.models.MPTableModel;
import mpv5.utils.tables.Selection;
import mpv5.utils.tables.TableFormat;
import mpv5.utils.ui.TextFieldUtils;

/**
 *
 * 
 */
public class ControlPanel_Templates extends javax.swing.JPanel implements ControlApplet, DataPanel {

    private static final long serialVersionUID = 1L;
    /**
     * This unique name identifies this control applet
     */
    public final String UNAME = "templates";
    private PropertyStore oldvalues;
    private Template dataOwner;
    private static ControlPanel_Templates ident;

    public ControlPanel_Templates() {
        if (MPSecurityManager.checkAdminAccess()) {
            initComponents();
            type.getComboBox().setModel(new DefaultComboBoxModel(
                    new String[]{
                        Messages.TYPE_BILL.toString(),
                        Messages.TYPE_OFFER.toString(),
                        Messages.TYPE_ORDER.toString(),
                        Messages.TYPE_PRODUCT.toString(),
                        Messages.TYPE_SERVICE.toString(),
                        Messages.TYPE_DELIVERY.toString(),}));
            refresh();
            setVisible(true);
        }
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jPanel2 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        fullname = new mpv5.ui.beans.LabeledTextField();
        size = new mpv5.ui.beans.LabeledTextField();
        jLabel3 = new javax.swing.JLabel();
        groupname = new javax.swing.JComboBox();
        jLabel6 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        descr = new javax.swing.JTextArea();
        jScrollPane3 = new javax.swing.JScrollPane();
        jList1 = new javax.swing.JList();
        type = new mpv5.ui.beans.LabeledCombobox();
        format = new mpv5.ui.beans.LabeledTextField();
        jPanel6 = new javax.swing.JPanel();
        jButton6 = new javax.swing.JButton();
        jButton5 = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JSeparator();
        jSeparator2 = new javax.swing.JSeparator();
        jButton4 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();

        setBackground(new java.awt.Color(255, 255, 255));
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("mpv5/resources/languages/Panels"); // NOI18N
        setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("ControlPanel_Templates.border.title"))); // NOI18N
        setName("Form"); // NOI18N
        setPreferredSize(new java.awt.Dimension(495, 183));
        setLayout(new java.awt.BorderLayout());

        jScrollPane1.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("ControlPanel_Templates.jScrollPane1.border.title"))); // NOI18N
        jScrollPane1.setName("jScrollPane1"); // NOI18N

        jTable1.setAutoCreateRowSorter(true);
        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {},
                {},
                {},
                {}
            },
            new String [] {

            }
        ));
        jTable1.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_ALL_COLUMNS);
        jTable1.setDragEnabled(true);
        jTable1.setFillsViewportHeight(true);
        jTable1.setName("jTable1"); // NOI18N
        jTable1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTable1MouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(jTable1);

        add(jScrollPane1, java.awt.BorderLayout.CENTER);

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("ControlPanel_Templates.jPanel2.border.title"))); // NOI18N
        jPanel2.setName("jPanel2"); // NOI18N
        jPanel2.setPreferredSize(new java.awt.Dimension(425, 100));

        jPanel4.setBackground(new java.awt.Color(255, 255, 255));
        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("ControlPanel_Templates.jPanel4.border.title"))); // NOI18N
        jPanel4.setName("jPanel4"); // NOI18N

        fullname.set_Label(bundle.getString("ControlPanel_Templates.fullname._Label")); // NOI18N
        fullname.setFont(fullname.getFont());
        fullname.setName("fullname"); // NOI18N

        size.set_Label(bundle.getString("ControlPanel_Templates.size._Label")); // NOI18N
        size.setName("size"); // NOI18N

        jLabel3.setFont(new java.awt.Font("Dialog", 0, 12));
        jLabel3.setText(bundle.getString("ControlPanel_Templates.jLabel3.text")); // NOI18N
        jLabel3.setName("jLabel3"); // NOI18N

        groupname.setName("groupname"); // NOI18N

        jLabel6.setFont(new java.awt.Font("Dialog", 0, 12));
        jLabel6.setText(bundle.getString("ControlPanel_Templates.jLabel6.text")); // NOI18N
        jLabel6.setName("jLabel6"); // NOI18N

        jLabel1.setFont(jLabel1.getFont());
        jLabel1.setText(bundle.getString("ControlPanel_Templates.jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        jScrollPane2.setName("jScrollPane2"); // NOI18N

        descr.setColumns(20);
        descr.setRows(5);
        descr.setName("descr"); // NOI18N
        jScrollPane2.setViewportView(descr);

        jScrollPane3.setName("jScrollPane3"); // NOI18N

        jList1.setName("jList1"); // NOI18N
        jScrollPane3.setViewportView(jList1);

        type.set_Label(bundle.getString("ControlPanel_Templates.type._Label")); // NOI18N
        type.setName("type"); // NOI18N

        format.set_Label(bundle.getString("ControlPanel_Templates.format._Label")); // NOI18N
        format.set_Text(bundle.getString("ControlPanel_Templates.format._Text")); // NOI18N
        format.setName("format"); // NOI18N

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(format, javax.swing.GroupLayout.DEFAULT_SIZE, 381, Short.MAX_VALUE)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, 72, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 297, Short.MAX_VALUE))
                    .addComponent(fullname, javax.swing.GroupLayout.DEFAULT_SIZE, 381, Short.MAX_VALUE)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(size, javax.swing.GroupLayout.PREFERRED_SIZE, 159, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 20, Short.MAX_VALUE)
                        .addComponent(type, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel4Layout.createSequentialGroup()
                                .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, 72, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED))
                            .addGroup(jPanel4Layout.createSequentialGroup()
                                .addComponent(jLabel6)
                                .addGap(48, 48, 48)))
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(groupname, 0, 297, Short.MAX_VALUE)
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 297, Short.MAX_VALUE))))
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addComponent(fullname, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(type, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(size, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(format, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(groupname, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel3, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 41, Short.MAX_VALUE))
                .addContainerGap())
        );

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel4, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        add(jPanel2, java.awt.BorderLayout.EAST);

        jPanel6.setBackground(new java.awt.Color(255, 255, 255));
        jPanel6.setName("jPanel6"); // NOI18N
        jPanel6.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        jButton6.setText(bundle.getString("ControlPanel_Templates.jButton6.text")); // NOI18N
        jButton6.setName("jButton6"); // NOI18N
        jButton6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton6ActionPerformed(evt);
            }
        });
        jPanel6.add(jButton6);

        jButton5.setText(bundle.getString("ControlPanel_Templates.jButton5.text")); // NOI18N
        jButton5.setName("jButton5"); // NOI18N
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton5ActionPerformed(evt);
            }
        });
        jPanel6.add(jButton5);

        jButton1.setText(bundle.getString("ControlPanel_Templates.jButton1.text")); // NOI18N
        jButton1.setName("jButton1"); // NOI18N
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        jPanel6.add(jButton1);

        jSeparator1.setName("jSeparator1"); // NOI18N
        jPanel6.add(jSeparator1);

        jSeparator2.setName("jSeparator2"); // NOI18N
        jPanel6.add(jSeparator2);

        jButton4.setText(bundle.getString("ControlPanel_Templates.jButton4.text")); // NOI18N
        jButton4.setName("jButton4"); // NOI18N
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });
        jPanel6.add(jButton4);

        jButton3.setText(bundle.getString("ControlPanel_Templates.jButton3.text")); // NOI18N
        jButton3.setName("jButton3"); // NOI18N
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });
        jPanel6.add(jButton3);

        jButton2.setText(bundle.getString("ControlPanel_Templates.jButton2.text")); // NOI18N
        jButton2.setName("jButton2"); // NOI18N
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });
        jPanel6.add(jButton2);

        add(jPanel6, java.awt.BorderLayout.PAGE_END);
    }// </editor-fold>//GEN-END:initComponents

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        reset();
}//GEN-LAST:event_jButton3ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed

        if (dataOwner != null) {
            DatabaseObject dato = dataOwner;
            dato.getPanelData(this);
            if (dato.save()) {
                actionAfterSave();
                refresh();
            } else {
                showRequiredFields();
            }
        }
}//GEN-LAST:event_jButton2ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        DialogForFile d = new DialogForFile(DialogForFile.FILES_ONLY);
        d.setFileFilter(DialogForFile.TEMPLATE_FILES);

        if (d.chooseFile()) {
            Template t = new Template();
            if (QueryHandler.instanceOf().clone(Context.getFiles(), this).insertFile(d.getFile(), t, new SaveString(d.getFile().getName(), true))) {
            }
        }
}//GEN-LAST:event_jButton1ActionPerformed

    private void jTable1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTable1MouseClicked
        Selection s = new Selection(jTable1);
        DatabaseObject obj;
        if (s.checkID()) {
            try {
                obj = DatabaseObject.getObject(Context.getTemplate(), s.getId());
                setDataOwner(obj, true);
            } catch (NodataFoundException ex) {
                Log.Debug(this, ex);
            }
        }
    }//GEN-LAST:event_jTable1MouseClicked

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        if (Popup.Y_N_dialog(Messages.REALLY_DELETE)) {
            if (dataOwner != null) {
                Template.templateCache.clear();
                DatabaseObject dato = dataOwner;
                dato.getPanelData(this);
                dato.delete();
            }
            refresh();
        }
    }//GEN-LAST:event_jButton4ActionPerformed

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
        test();
    }//GEN-LAST:event_jButton5ActionPerformed

    private void jButton6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton6ActionPerformed
        try {
            Desktop.getDesktop().browse(new URI(Constants.WEBSITE));
        } catch (Exception ex) {
            Log.Debug(ex);
        }
    }//GEN-LAST:event_jButton6ActionPerformed

    public void setValues(PropertyStore values) {
    }

    public String getUname() {
        return UNAME;
    }

    public void reset() {
        if (dataOwner != null) {
            DatabaseObject dato = dataOwner;

            dato.getPanelData(this);
            dato.reset();
            setDataOwner(dato, true);
        }
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextArea descr;
    private mpv5.ui.beans.LabeledTextField format;
    private mpv5.ui.beans.LabeledTextField fullname;
    private javax.swing.JComboBox groupname;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JList jList1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JTable jTable1;
    private mpv5.ui.beans.LabeledTextField size;
    private mpv5.ui.beans.LabeledCombobox type;
    // End of variables declaration//GEN-END:variables
    public String description_ = "";
    public String filename_ = "";
    public String cname_;
    public String format_;
    public File file_;
    public int intsize_;
    public String mimetype_;
    public int intaddedby_ = 4343;
    public int ids_;
    public int groupsids_;
    public int compsids_;
    public java.util.Date dateadded_ = new java.util.Date();

    public boolean collectData() {
        if (groupname.getSelectedItem() != null) {
            groupsids_ = Integer.valueOf(((MPComboBoxModelItem) groupname.getSelectedItem()).getId());
        } else {
            groupsids_ = 1;
        }

        description_ = descr.getText();
        cname_ = fullname.getText();
        mimetype_ = String.valueOf(type.getComboBox().getSelectedItem());
        format_ = format.getText();

        return true;
    }

    public void exposeData() {

        try {
            groupname.setSelectedIndex(MPComboBoxModelItem.getItemID(String.valueOf(groupsids_), groupname.getModel()));

            fullname.setText(cname_);
            descr.setText(description_);
            type.getComboBox().setSelectedItem(mimetype_);
            size.setText(String.valueOf(intsize_));
            format.setText(format_);

            DefaultListModel m = new DefaultListModel();
            ArrayList<DatabaseObject> li = DatabaseObject.getObjects(Context.getUser());

            QueryCriteria c = new QueryCriteria("templatesids", dataOwner.__getIDS());
            Object[][] data = QueryHandler.instanceOf().clone(Context.getTemplatesToUsers()).select("usersids", c);

            List<Integer> l = new Vector<Integer>();
            for (int i = 0; i < li.size(); i++) {
                User databaseObject = (User) li.get(i);

                for (int j = 0; j < data.length; j++) {
                    int id = Integer.valueOf(data[j][0].toString());
                    if (id == databaseObject.__getIDS().intValue()) {
                        l.add(Integer.valueOf(i));
                    }
                }
                m.addElement(databaseObject);
            }

            jList1.setModel(m);

            int[] ix = new int[l.size()];
            for (int i = 0; i < l.size(); i++) {
                Integer integer = l.get(i);
                ix[i] = integer.intValue();
            }

            jList1.setSelectedIndices(ix);

        } catch (Exception e) {
            Log.Debug(this, e);
        }
    }

    public DatabaseObject getDataOwner() {
        return dataOwner;
    }

    public void setDataOwner(DatabaseObject object, boolean p) {
        dataOwner = (Template) object;
        if (p) {
            dataOwner.setPanelData(this);
            this.exposeData();
        }
    }

    public void refresh() {
        format.setText(Template.DEFAULT_FORMAT);
        Context c = Context.getTemplate();
        c.addReference(Context.getFiles().getDbIdentity(), "cname", "filename");
        jTable1.setModel(new MPTableModel(new Class[]{Integer.class, String.class, String.class, String.class, String.class, Long.class, String.class},
                QueryHandler.instanceOf().clone(c).
                select(Context.DETAILS_FILES_TO_TEMPLATES, (String[]) null),
                Headers.TEMPLATES.getValue()));

        groupname.setModel(new DefaultComboBoxModel(
                MPComboBoxModelItem.toItems(new DatabaseSearch(Context.getGroup()).getValuesFor(Context.getGroup().getSubID(), null, ""))));

        TableFormat.stripFirstColumn(jTable1);
        TableFormat.format(jTable1, 1, 120);
        TableFormat.format(jTable1, 5, 120);
    }

    public void paste(DatabaseObject dbo) {
        if (dbo.getContext().equals(Context.getTemplate())) {
            setDataOwner(dbo, true);
        } else {
            MPView.addMessage(Messages.NOT_POSSIBLE.toString() + Messages.ACTION_PASTE);
        }
    }

    public void showRequiredFields() {
        TextFieldUtils.blinkerRed(fullname);
    }

    public void showSearchBar(boolean show) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Component getAndRemoveActionPanel() {
        this.remove(jPanel6);
        validate();
        return jPanel6;
    }

    @Override
    public void actionAfterSave() {
        Object[] selectedValues = jList1.getSelectedValues();
        for (int i = 0; i < selectedValues.length; i++) {
            User object = (User) selectedValues[i];

            QueryCriteria d = new QueryCriteria();
            d.add("cname", dataOwner.__getIDS() + "@" + object.__getIDS());
            QueryHandler.instanceOf().clone(Context.getTemplatesToUsers()).delete(d);

            QueryData c = new QueryData();
            c.add("usersids", object.__getIDS());
            c.add("templatesids", dataOwner.__getIDS());
            c.add("cname", dataOwner.__getIDS() + "@" + object.__getIDS());
            QueryHandler.instanceOf().clone(Context.getTemplatesToUsers()).insert(c, null);
        }
    }

    @Override
    public void actionAfterCreate() {
    }

    private void test() {
        DatabaseObject t;
        if (dataOwner != null) {
            try {
                t = DatabaseObject.getObject(Context.getItems(), 1);
            } catch (NodataFoundException ex) {
                t = new Item();
                Contact k = new Contact();
                k.avoidNulls();
                k.fillSampleData();
                t.avoidNulls();
                t.fillSampleData();
                ((Item) t).setContactsids(k.__getIDS());
            }

            try {
                HashMap<String, String> hm1 = new FormFieldsHandler(t).getFormattedFormFields(null);
                Log.Print(hm1.entrySet().toArray());
                File f = dataOwner.getFile();
                File f2 = FileDirectoryHandler.getTempFile("pdf");
                Export ex = new Export();
                ex.putAll(hm1);

                Vector<String[]> l = new Vector<String[]>();

                for (int i = 0; i < 20; i++) {
                    l.add(SubItem.getRandomItem().toStringArray(dataOwner));
                }

                ex.put(TableHandler.KEY_TABLE + "1", l);

                if (f.getName().endsWith("odt")) {
                    ex.setTemplate(new ODTFile(f.getPath()));
                } else {
                    ex.setTemplate(new PDFFile(f.getPath()));
                }

                ex.setTargetFile(f2);

                new Job(ex, new PreviewPanel()).execute();
            } catch (Exception ex1) {
                Log.Debug(ex1);
                Popup.error(ex1);
            }
        }
    }
}
