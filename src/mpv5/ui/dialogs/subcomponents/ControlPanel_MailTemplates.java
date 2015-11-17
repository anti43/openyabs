package mpv5.ui.dialogs.subcomponents;

import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import javax.swing.DefaultListModel;
import mpv5.data.PropertyStore;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.db.common.NodataFoundException;
import mpv5.db.common.QueryCriteria;
import mpv5.db.common.QueryHandler;

import mpv5.db.objects.Group;
import mpv5.db.objects.MailMessage;
import mpv5.globals.Headers;
import mpv5.globals.Messages;
import mpv5.logging.Log;
import mpv5.ui.dialogs.ControlApplet;
import mpv5.ui.dialogs.Popup;
import mpv5.ui.frames.MPView;
import mpv5.ui.panels.DataPanel;
import mpv5.db.objects.User;

import mpv5.utils.models.MPComboBoxModelItem;
import mpv5.utils.models.MPTableModel;
import mpv5.utils.ui.TextFieldUtils;

/**
 *
 * 
 */
public class ControlPanel_MailTemplates extends javax.swing.JPanel implements ControlApplet, DataPanel {

    private static final long serialVersionUID = 1L;
    /**
     * This unique name identifies this control applet
     */
    public final String UNAME = "mailtemplates";
    private MailMessage dataOwner;

    public ControlPanel_MailTemplates() {
        initComponents();
        refresh();
        groupname.setSearchEnabled(true);
        groupname.setContext(Context.getGroup());
        groupname.triggerSearch();
        setVisible(true);
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel6 = new javax.swing.JPanel();
        jButton4 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton7 = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        fullname = new mpv5.ui.beans.LabeledTextField();
        jLabel1 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        descr = new javax.swing.JTextArea();
        groupname = new mpv5.ui.beans.LabeledCombobox();
        jScrollPane4 = new javax.swing.JScrollPane();
        templates = new javax.swing.JTable();

        //\$2java.awt.Color(255, 255, 255));
        java.util.ResourceBundle bundle = mpv5.i18n.LanguageManager.getBundle();
        setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("ControlPanel_MailTemplates.border.title_1"))); // NOI18N
        setName("Form"); // NOI18N
        setPreferredSize(new java.awt.Dimension(495, 183));
        setLayout(new java.awt.BorderLayout());

//$2java.awt.Color(255, 255, 255));
        jPanel6.setName("jPanel6"); // NOI18N
        jPanel6.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        jButton4.setText(bundle.getString("ControlPanel_MailTemplates.jButton4.text_1")); // NOI18N
        jButton4.setName("jButton4"); // NOI18N
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });
        jPanel6.add(jButton4);

        jButton3.setText(bundle.getString("ControlPanel_MailTemplates.jButton3.text_1")); // NOI18N
        jButton3.setName("jButton3"); // NOI18N
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });
        jPanel6.add(jButton3);

        jButton2.setText(bundle.getString("ControlPanel_MailTemplates.jButton2.text_1")); // NOI18N
        jButton2.setName("jButton2"); // NOI18N
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });
        jPanel6.add(jButton2);

        jButton7.setText(bundle.getString("ControlPanel_MailTemplates.jButton7.text_1")); // NOI18N
        jButton7.setName("jButton7"); // NOI18N
        jButton7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton7ActionPerformed(evt);
            }
        });
        jPanel6.add(jButton7);

        add(jPanel6, java.awt.BorderLayout.PAGE_END);

        jPanel1.setName("jPanel1"); // NOI18N

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("ControlPanel_MailTemplates.jPanel2.border.title_1"))); // NOI18N
        jPanel2.setName("jPanel2"); // NOI18N
        jPanel2.setPreferredSize(new java.awt.Dimension(425, 100));

//$2java.awt.Color(255, 255, 255));
        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("ControlPanel_MailTemplates.jPanel4.border.title_1"))); // NOI18N
        jPanel4.setName("jPanel4"); // NOI18N

        fullname.set_Label(bundle.getString("ControlPanel_MailTemplates.fullname._Label_1")); // NOI18N
        fullname.setFont(fullname.getFont());
        fullname.setName("fullname"); // NOI18N

        jLabel1.setFont(jLabel1.getFont());
        jLabel1.setText(bundle.getString("ControlPanel_MailTemplates.jLabel1.text_1")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        jScrollPane2.setName("jScrollPane2"); // NOI18N

        descr.setColumns(20);
        descr.setRows(5);
        descr.setName("descr"); // NOI18N
        jScrollPane2.setViewportView(descr);

        groupname.set_Label(bundle.getString("ControlPanel_MailTemplates.groupname._Label")); // NOI18N
        groupname.setName("groupname"); // NOI18N

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 415, Short.MAX_VALUE)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 157, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(258, Short.MAX_VALUE))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(fullname, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(groupname, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 265, Short.MAX_VALUE))
                        .addContainerGap())))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addComponent(fullname, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(groupname, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(9, 9, 9)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 127, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        jScrollPane4.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("ControlPanel_MailTemplates.jScrollPane4.border.title_1"))); // NOI18N
        jScrollPane4.setName("jScrollPane4"); // NOI18N

        templates.setAutoCreateRowSorter(true);
        templates.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {},
                {},
                {},
                {}
            },
            new String [] {

            }
        ));
        templates.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_ALL_COLUMNS);
        templates.setDragEnabled(true);
        templates.setFillsViewportHeight(true);
        templates.setName("templates"); // NOI18N
        templates.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                templatesMouseClicked(evt);
            }
        });
        jScrollPane4.setViewportView(templates);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 121, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, 447, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 250, Short.MAX_VALUE)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, 250, Short.MAX_VALUE)
        );

        add(jPanel1, java.awt.BorderLayout.CENTER);
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

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        if (Popup.Y_N_dialog(Messages.REALLY_DELETE)) {
            if (dataOwner != null) {
                DatabaseObject dato = dataOwner;
                dato.getPanelData(this);
                dato.delete();
            }
            try {
                Thread.sleep(333);
            } catch (InterruptedException ex) {
            }
            refresh();
        }
    }//GEN-LAST:event_jButton4ActionPerformed

    private void jButton7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton7ActionPerformed

        if (dataOwner == null) {
            dataOwner = new MailMessage();
        }

        dataOwner.getPanelData(this);
        dataOwner.setIDS(-1);
        dataOwner.save();

        refresh();
    }//GEN-LAST:event_jButton7ActionPerformed

    private void templatesMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_templatesMouseClicked
        try {
            setDataOwner((DatabaseObject) templates.getValueAt(templates.getSelectedRow(), 0), true);
        } catch (Exception e) {
            Log.Debug(e);
        }
    }//GEN-LAST:event_templatesMouseClicked

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
    private mpv5.ui.beans.LabeledTextField fullname;
    private mpv5.ui.beans.LabeledCombobox groupname;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton7;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JTable templates;
    // End of variables declaration//GEN-END:variables
    public String description_ = "";
    public String cname_;
    public String format_;
    public int ids_;
    public int intaddedby_;
    public Group group_;
    public int usersids_;
    public java.util.Date dateadded_ = new java.util.Date();

    public boolean collectData() {
         if (groupname.getSelectedItem() != null) {
            int groupsids_ = Integer.valueOf(((MPComboBoxModelItem) groupname.getSelectedItem()).getId());
            try {
                group_ = (Group) DatabaseObject.getObject(Context.getGroup(), groupsids_);
            } catch (NodataFoundException ex) {
                Log.Debug(this, ex);
                group_ = Group.getDefault();
            }
        }

        description_ = descr.getText();
        cname_ = fullname.getText();
        usersids_ = mpv5.db.objects.User.getCurrentUser().__getIDS();

        return true;
    }

    public void exposeData() {

        try {
            if(group_!=null)
                groupname.setSelectedIndex(MPComboBoxModelItem.getItemID(String.valueOf(group_.__getIDS()), groupname.getModel()));
            
            fullname.setText(cname_);
            descr.setText(description_);
        } catch (Exception e) {
            Log.Debug(this, e);
        }
    }

    public DatabaseObject getDataOwner() {
        return dataOwner;
    }

    public void setDataOwner(DatabaseObject object, boolean p) {
        dataOwner = (MailMessage) object;
        if (p) {
            dataOwner.setPanelData(this);
            this.exposeData();
        }
    }

    public void refresh() {
        ArrayList<DatabaseObject> temps;
        try {
            QueryCriteria c = new QueryCriteria("usersids", mpv5.db.objects.User.getCurrentUser().__getIDS());
            temps = DatabaseObject.getObjects(Context.getMessage());
            Object[][] data = new Object[temps.size()][3];

            for (int i = 0; i < temps.size(); i++) {
                MailMessage t = (MailMessage) temps.get(i);
                data[i][0] = t;
                data[i][1] = t.__getDescription();
                data[i][2] = Group.getObject(Context.getGroup(), t.__getGroupsids());
            }

            templates.setModel(new MPTableModel(data, Headers.MAILTEMPLATES.getValue()));
        } catch (NodataFoundException ex) {
            Log.Debug(this, ex.getMessage());
        }
    }

    public void paste(DatabaseObject... dbos) {
        for (DatabaseObject dbo : dbos) {
            if (dbo.getContext().equals(Context.getTemplate())) {
                setDataOwner(dbo, true);
            } else {
                mpv5.YabsViewProxy.instance().addMessage(Messages.NOT_POSSIBLE.toString() + Messages.ACTION_PASTE, Color.RED);
            }
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
    }

    @Override
    public void actionAfterCreate() {
    }

    public void actionBeforeCreate() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void actionBeforeSave() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void mail() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void print() {
        mpv5.utils.export.Export.print(this);
    }
}
