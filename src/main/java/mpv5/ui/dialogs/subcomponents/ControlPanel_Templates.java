package mpv5.ui.dialogs.subcomponents;

import mpv5.handler.TemplateHandler;
import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
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

import mpv5.db.objects.*;
import mpv5.db.objects.SubItem;
import mpv5.db.objects.Template;
import mpv5.globals.Headers;
import mpv5.globals.Messages;
import mpv5.logging.Log;
import mpv5.ui.dialogs.ControlApplet;
import mpv5.ui.dialogs.Popup;
import mpv5.ui.panels.DataPanel;
import mpv5.usermanagement.MPSecurityManager;
import mpv5.db.objects.User;

import mpv5.globals.Constants;
import mpv5.globals.LocalSettings;
import mpv5.handler.VariablesHandler;
import mpv5.ui.dialogs.BigPopup;
import mpv5.ui.dialogs.DialogForFile;
import mpv5.ui.dialogs.Notificator;
import mpv5.ui.dialogs.Search2;
import mpv5.ui.panels.PreviewWaiter;
import mpv5.utils.export.Export;
import mpv5.utils.export.ODTFile2;
import mpv5.utils.files.FileDirectoryHandler;
import mpv5.utils.files.FileExecutor;
import mpv5.utils.files.FileMonitor;
import mpv5.utils.jobs.Job;
import mpv5.utils.models.MPComboBoxModelItem;
import mpv5.utils.models.MPComboboxModel;
import mpv5.utils.models.MPTableModel;
import mpv5.utils.ui.TextFieldUtils;

/**
 *
 *
 */
public final class ControlPanel_Templates extends javax.swing.JPanel implements ControlApplet, DataPanel {

    private static final long serialVersionUID = 1L;
    /**
     * This unique name identifies this control applet
     */
    public final String UNAME = "templates";
    private Template dataOwner;
    private File lastImportedFile;
    private long lastmodified;

    public ControlPanel_Templates() {
        if (MPSecurityManager.checkAdminAccess()) {
            initComponents();
            type.getComboBox().setModel(new MPComboboxModel(MPComboBoxModelItem.toItems(TemplateHandler.getTypes())));
            refresh();
            groupname.setModel(new DefaultComboBoxModel(
                    MPComboBoxModelItem.toItems(new DatabaseSearch(Context.getGroup()).getValuesFor(Context.getGroup().getSubID()))));
            java.util.ResourceBundle bundle1 = mpv5.i18n.LanguageManager.getBundle();
            format.setText(Template.DEFAULT_FORMAT);
            format.getTextField().setEditable(true);
            format.getTextField().setToolTipText(bundle1.getString("ControlPanel_Templates.format.toolTipText_1")); // NOI18N
            setVisible(true);
        }
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel6 = new javax.swing.JPanel();
        jButton6 = new javax.swing.JButton();
        jButton10 = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JSeparator();
        jSeparator2 = new javax.swing.JSeparator();
        jButton4 = new javax.swing.JButton();
        jButton7 = new javax.swing.JButton();
        jButton9 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        fullname = new mpv5.ui.beans.LabeledTextField();
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
        jButton8 = new javax.swing.JButton();
        printern = new mpv5.ui.beans.LabeledTextField();
        jButton11 = new javax.swing.JButton();
        updateService = new javax.swing.JCheckBox();
        pathtofile = new mpv5.ui.beans.LabeledTextField();
        jScrollPane4 = new javax.swing.JScrollPane();
        templates = new javax.swing.JTable();

        java.util.ResourceBundle bundle = mpv5.i18n.LanguageManager.getBundle(); // NOI18N
        setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("ControlPanel_Templates.border.title"))); // NOI18N
        setName("Form"); // NOI18N
        setPreferredSize(new java.awt.Dimension(495, 183));
        setLayout(new java.awt.BorderLayout());

        jPanel6.setName("jPanel6"); // NOI18N
        jPanel6.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        jButton6.setText(bundle.getString("ControlPanel_Templates.JButton6.text")); // NOI18N
        jButton6.setName("JButton6"); // NOI18N
        jButton6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton6ActionPerformed(evt);
            }
        });
        jPanel6.add(jButton6);

        jButton10.setText(bundle.getString("ControlPanel_Templates.jButton10.text")); // NOI18N
        jButton10.setName("jButton10"); // NOI18N
        jButton10.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton10ActionPerformed(evt);
            }
        });
        jPanel6.add(jButton10);

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

        jButton7.setText(bundle.getString("ControlPanel_Templates.jButton7.text")); // NOI18N
        jButton7.setName(bundle.getString("ControlPanel_Templates.jButton7.name")); // NOI18N
        jButton7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton7ActionPerformed(evt);
            }
        });
        jPanel6.add(jButton7);

        jButton9.setText(bundle.getString("ControlPanel_Templates.jButton9.text")); // NOI18N
        jButton9.setName("jButton9"); // NOI18N
        jButton9.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton9ActionPerformed(evt);
            }
        });
        jPanel6.add(jButton9);

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

        jPanel1.setName("jPanel1"); // NOI18N

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("ControlPanel_Templates.jPanel2.border.title"))); // NOI18N
        jPanel2.setName("jPanel2"); // NOI18N
        jPanel2.setPreferredSize(new java.awt.Dimension(425, 100));

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("ControlPanel_Templates.jPanel4.border.title"))); // NOI18N
        jPanel4.setName("jPanel4"); // NOI18N

        fullname.set_Label(bundle.getString("ControlPanel_Templates.fullname._Label")); // NOI18N
        fullname.setFont(fullname.getFont());
        fullname.setName("fullname"); // NOI18N

        jLabel3.setFont(jLabel3.getFont());
        jLabel3.setText(bundle.getString("ControlPanel_Templates.jLabel3.text")); // NOI18N
        jLabel3.setName("jLabel3"); // NOI18N

        groupname.setName("groupname"); // NOI18N

        jLabel6.setFont(jLabel6.getFont());
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

        jList1.setForeground(new java.awt.Color(0, 153, 0));
        jList1.setName("jList1"); // NOI18N
        jScrollPane3.setViewportView(jList1);

        type.set_Label(bundle.getString("ControlPanel_Templates.type._Label")); // NOI18N
        type.setName("type"); // NOI18N

        format.set_Label(bundle.getString("ControlPanel_Templates.format._Label")); // NOI18N
        format.set_Text("1,2,3,4,5,6,7,8,9"); // NOI18N
        format.setName("format"); // NOI18N

        jButton8.setText(bundle.getString("ControlPanel_Templates.jButton8.text")); // NOI18N
        jButton8.setName("jButton8"); // NOI18N
        jButton8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton8ActionPerformed(evt);
            }
        });

        printern.set_Label(bundle.getString("ControlPanel_Templates.printern._Label")); // NOI18N
        printern.setFont(printern.getFont());
        printern.setName("printern"); // NOI18N

        jButton11.setText(bundle.getString("ControlPanel_Templates.jButton11.text")); // NOI18N
        jButton11.setName(bundle.getString("ControlPanel_Templates.jButton11.name")); // NOI18N
        jButton11.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton11ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(type, javax.swing.GroupLayout.DEFAULT_SIZE, 383, Short.MAX_VALUE)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(printern, javax.swing.GroupLayout.DEFAULT_SIZE, 254, Short.MAX_VALUE)
                            .addComponent(format, javax.swing.GroupLayout.DEFAULT_SIZE, 254, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jButton8, javax.swing.GroupLayout.DEFAULT_SIZE, 123, Short.MAX_VALUE)
                            .addComponent(jButton11, javax.swing.GroupLayout.DEFAULT_SIZE, 123, Short.MAX_VALUE)))
                    .addComponent(fullname, javax.swing.GroupLayout.DEFAULT_SIZE, 383, Short.MAX_VALUE)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 79, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, 82, Short.MAX_VALUE)
                            .addComponent(jLabel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 303, Short.MAX_VALUE)
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 303, Short.MAX_VALUE)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                                .addGap(0, 0, Short.MAX_VALUE)
                                .addComponent(groupname, javax.swing.GroupLayout.PREFERRED_SIZE, 291, javax.swing.GroupLayout.PREFERRED_SIZE))))))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addComponent(fullname, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(printern, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton11, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(format, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton8, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(type, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(groupname, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 173, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1))
                .addContainerGap())
        );

        updateService.setText(bundle.getString("ControlPanel_Templates.updateService.text")); // NOI18N
        updateService.setEnabled(false);
        updateService.setName("updateService"); // NOI18N
        updateService.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                updateServiceItemStateChanged(evt);
            }
        });

        pathtofile.set_Label(bundle.getString("ControlPanel_Templates.pathtofile._Label")); // NOI18N
        pathtofile.setFont(pathtofile.getFont());
        pathtofile.setName("pathtofile"); // NOI18N

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(pathtofile, javax.swing.GroupLayout.DEFAULT_SIZE, 395, Short.MAX_VALUE))
            .addComponent(updateService, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(updateService)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pathtofile, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12))
        );

        jScrollPane4.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("ControlPanel_Templates.jScrollPane4.border.title"))); // NOI18N
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
                .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 508, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, 417, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane4)
            .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 530, Short.MAX_VALUE)
        );

        add(jPanel1, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        reset();
}//GEN-LAST:event_jButton3ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed

        if (jList1.getSelectedIndex() < 0) {
            Notificator.raiseNotification(Messages.SELECT_A_USER, true);
            return;
        }
        if (dataOwner != null) {
            DatabaseObject dato = dataOwner;
            dato.getPanelData(this);
            if (dato.save()) {
                actionAfterSave();
                refresh();
//                TemplateHandler.cacheTemplates();
            } else {
                showRequiredFields();
            }
        }
}//GEN-LAST:event_jButton2ActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        if (dataOwner != null && dataOwner.isExisting()) {
            if (Popup.Y_N_dialog(Messages.REALLY_DELETE)) {

                TemplateHandler.clearCache();
                DatabaseObject dato = dataOwner;
                dato.getPanelData(this);
                dato.delete();

                try {
                    Thread.sleep(333);
                } catch (InterruptedException ex) {
                }
                refresh();
            }
        }
    }//GEN-LAST:event_jButton4ActionPerformed

    private void templatesMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_templatesMouseClicked
        try {
            setDataOwner((DatabaseObject) templates.getValueAt(templates.convertRowIndexToModel(templates.getSelectedRow()), 0), true);
            if (!pathtofile_.equals("")) {
                updateService.setEnabled(true);
            }
        } catch (Exception e) {
            Log.Debug(this, e.getMessage());
        }
    }//GEN-LAST:event_templatesMouseClicked

    private void jButton6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton6ActionPerformed

        try {
            DatabaseObject t = null;
            switch (Integer.parseInt(type.getSelectedItem().getId())) {
                case Constants.TYPE_OFFER:
                    t = Search2.showSearchFor(Context.getOffer());
                    break;
                case Constants.TYPE_ORDER:
                    t = Search2.showSearchFor(Context.getOrder());
                    break;
                case Constants.TYPE_INVOICE:
                    t = Search2.showSearchFor(Context.getInvoice());
                    break;
                case Constants.TYPE_DEPOSIT:
                    t = Search2.showSearchFor(Context.getDeposit());
                    break;
                case Constants.TYPE_PART_PAYMENT:
                    t = Search2.showSearchFor(Context.getPartPayment());
                    break;
                case Constants.TYPE_CREDIT:
                    t = Search2.showSearchFor(Context.getCredit());
                    break;
            }
            if (t != null) {
                List<String[]> vars = VariablesHandler.resolveVarsFor(t);
                List<List<String>> varsl = new ArrayList<List<String>>();
                for (int i = 0; i < vars.size(); i++) {
                    String[] strings = vars.get(i);
                    varsl.add(Arrays.asList(strings));
                }
                Popup.notice(varsl, Messages.VARIABLES + "\n\n" + t.toString());
            }
        } catch (Exception ex) {
            Log.Debug(ex);
        }
}//GEN-LAST:event_jButton6ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        DialogForFile di = new DialogForFile(DialogForFile.FILES_ONLY);
        di.setFileFilter(DialogForFile.TEMPLATE_FILES);

        if (di.chooseFile()) {
            Template t = new Template();
            File fi = di.getFile();
            if (QueryHandler.instanceOf().clone(Context.getFiles(), this).insertFile(fi, t, new SaveString(fi.getName(), true))) {
                Popup.notice(Messages.ASSIGN_TEMPLATE);
                configureUpdateService(fi);
            }
        }
}//GEN-LAST:event_jButton1ActionPerformed

    private void jButton8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton8ActionPerformed
        BigPopup.showPopup(this, TemplateFormatEditor.instanceOf(format.getTextField()), "", true);
    }//GEN-LAST:event_jButton8ActionPerformed

    private void jButton9ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton9ActionPerformed

        if (dataOwner != null && dataOwner.isExisting()) {
            try {
                mpv5.YabsViewProxy.instance().showFilesaveDialogFor(dataOwner.getFile());

                DialogForFile d = mpv5.YabsViewProxy.instance().getFiledialog();
                File nFile = d.saveFile(dataOwner.getFile());
                if (nFile != null) {
                    configureUpdateService(nFile);
                    updateService.setSelected(true);
                }
            } catch (Exception e) {
                Log.Debug(e);
            }
        }
    }//GEN-LAST:event_jButton9ActionPerformed

    private void jButton10ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton10ActionPerformed
        if (dataOwner != null && dataOwner.isExisting()) {
            Template tpl = (Template) templates.getValueAt(templates.convertRowIndexToModel(templates.getSelectedRow()), 0);
            if (Popup.Y_N_dialog(Messages.REALLY_CHANGE.toString(), tpl.__getCname())) {
                DialogForFile di = new DialogForFile(DialogForFile.FILES_ONLY);
                di.setFileFilter(DialogForFile.TEMPLATE_FILES);
                if (di.chooseFile()) {
                    File fi = di.getFile();
                    QueryHandler.instanceOf().clone(Context.getFiles(), this).updateFile(fi, tpl.__getFilename());
                    tpl.setDescription(tpl.__getDescription() + "\n - Updated: " + new Date());
                    tpl.save(true);
                    TemplateHandler.clearCache();

                    try {
                        Thread.sleep(333);
                    } catch (InterruptedException ex) {
                    }
                    refresh();
                    lastImportedFile = fi;
                    updateService.setEnabled(true);
                    try {
                        pathtofile.setText(fi.getCanonicalPath());
                    } catch (IOException ex) {
                        Log.Debug(this, ex);
                    }
                    lastmodified = fi.lastModified();
                }
            }
        }
    }//GEN-LAST:event_jButton10ActionPerformed

    private void updateServiceItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_updateServiceItemStateChanged

        final DataPanel x = this;
        final Template tpl = dataOwner;
        FileMonitor.FileChangeListener filecl = new FileMonitor.FileChangeListener() {
            @Override
            public void fileChanged(String fileName) {
                QueryHandler.instanceOf().clone(Context.getFiles(), x).updateFile(new File(fileName), tpl.__getFilename());
                tpl.setDescription(tpl.__getDescription() + "\n - Updated: " + new Date());
                tpl.save(true);
                TemplateHandler.clearCache();

                try {
                    Thread.sleep(333);
                } catch (InterruptedException ex) {
                }
                refresh();
            }
        };

        String file = null;
        if (lastImportedFile != null) {
            try {
                file = lastImportedFile.getCanonicalPath();
            } catch (IOException ex) {
                Log.Debug(ex);
            }
        } else if (!pathtofile.getText().equals("") && new File(pathtofile.getText()).canRead()) {
            file = pathtofile.getText();
        }
        if (updateService.isSelected() && file != null) {
            FileMonitor.getInstance().addFileChangeListener(filecl, file, 1000l);
        } else {
            FileMonitor.getInstance().removeFileChangeListener(filecl, file);
        }
        lastImportedFile = null;
    }//GEN-LAST:event_updateServiceItemStateChanged

    private void jButton7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton7ActionPerformed

        if (dataOwner != null && dataOwner.isExisting()) {
            try {
 
                updateService.setSelected(false);
                updateService.setSelected(true);
                if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
                    Log.Debug(this, "Desktop open odt");
                    Desktop.getDesktop().open(dataOwner.getFile());
                } else if (LocalSettings.getBooleanProperty(LocalSettings.OFFICE_USE)) {
                    Log.Debug(this, "Manual LO open odt");
                    FileExecutor.run(LocalSettings.getProperty(LocalSettings.OFFICE_HOME) + File.separator + dataOwner.getFile().getPath());
                }else{
                    Log.Debug(this, "Failed open odt");
                    Notificator.raiseNotification("I do not know how to open .odt documents on this system");
                }
            } catch (Exception e) {
                Log.Debug(e);
            }
        }
    }//GEN-LAST:event_jButton7ActionPerformed

    private void jButton11ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton11ActionPerformed
        PrintService[] services3 = PrintServiceLookup.lookupPrintServices(null, null);
        List<String> l = new ArrayList<String>();
        for (int i = 0; i < services3.length; i++) {
            PrintService printService = services3[i];
            l.add(printService.getName());
        }
        Popup.notice(l, Messages.PRINTER_NAMES);
    }//GEN-LAST:event_jButton11ActionPerformed

    @Override
    public void setValues(PropertyStore values) {
    }

    @Override
    public String getUname() {
        return UNAME;
    }

    @Override
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
    private javax.swing.JButton jButton10;
    private javax.swing.JButton jButton11;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton7;
    private javax.swing.JButton jButton8;
    private javax.swing.JButton jButton9;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JList jList1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private mpv5.ui.beans.LabeledTextField pathtofile;
    private mpv5.ui.beans.LabeledTextField printern;
    private javax.swing.JTable templates;
    private mpv5.ui.beans.LabeledCombobox type;
    private javax.swing.JCheckBox updateService;
    // End of variables declaration//GEN-END:variables
    public String description_ = "";
    public String filename_ = "";
    public String cname_;
    public String format_;
    public int intsize_;
    public String mimetype_;
    public int intaddedby_ = 4343;
    public int ids_;
    public Group group_;
    public int compsids_;
    public String printer_;
    public java.util.Date dateadded_ = new java.util.Date();
    public boolean isupdateenabled_ = false;
    public String pathtofile_ = "";
    public long lastmodified_;

    @Override
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
        mimetype_ = String.valueOf(type.getSelectedItem().getId());
        format_ = format.getText();
        printer_ = printern.getText();
        pathtofile_ = pathtofile.getText();
        lastmodified_ = lastmodified;
        isupdateenabled_ = updateService.isSelected();

        return true;
    }

    @Override
    public void exposeData() {

        try {
            if (group_ != null) {
                groupname.setSelectedIndex(MPComboBoxModelItem.getItemID(String.valueOf(group_.__getIDS()), groupname.getModel()));
            }
            fullname.setText(cname_);
            descr.setText(description_);
            try {
                type.setSelectedItem(Integer.valueOf(mimetype_));
            } catch (NumberFormatException numberFormatException) {
            }
            format.setText(format_);
            printern.setText(printer_);

            DefaultListModel m = new DefaultListModel();
            ArrayList<DatabaseObject> li = DatabaseObject.getObjects(Context.getUser());

            QueryCriteria c = new QueryCriteria("templatesids", dataOwner.__getIDS());
            Object[][] data = QueryHandler.instanceOf().clone(Context.getTemplatesToUsers()).select("usersids", c);

            List<Integer> l = new ArrayList<Integer>();
            for (int i = 0; i < li.size(); i++) {
                User databaseObject = (User) li.get(i);

                for (int j = 0; j < data.length; j++) {
                    int id = Integer.valueOf(data[j][0].toString());
                    if (id == databaseObject.__getIDS()) {
                        l.add(i);
                    }
                }
                m.addElement(databaseObject);
            }

            jList1.setModel(m);

            int[] ix = new int[l.size()];
            for (int i = 0; i < l.size(); i++) {
                Integer integer = l.get(i);
                ix[i] = integer;
            }

            jList1.setSelectedIndices(ix);
            pathtofile.setText(dataOwner.getFile().getPath());
            lastImportedFile = null;
            lastmodified = lastmodified_;
            updateService.setSelected(isupdateenabled_);

        } catch (Exception e) {
            Log.Debug(this, e);
            if (Popup.Y_N_dialog("There was a problem loading the template, delete?")) {
                dataOwner.delete();
            }
        }
    }

    @Override
    public DatabaseObject getDataOwner() {
        return dataOwner;
    }

    @Override
    public void setDataOwner(DatabaseObject object, boolean p) {
        dataOwner = (Template) object;
        if (p) {
            dataOwner.setPanelData(this);
            this.exposeData();
        }
    }

    @Override
    public void refresh() {

        ArrayList<DatabaseObject> temps;
        try {
            temps = DatabaseObject.getObjects(Context.getTemplate());
            Object[][] data = new Object[temps.size()][3];

            for (int i = 0; i < temps.size(); i++) {
                Template t = (Template) temps.get(i);
                data[i][0] = t;
                data[i][1] = t.__getMimetype();
                data[i][2] = Group.getObject(Context.getGroup(), t.__getGroupsids());
            }

            templates.setModel(new MPTableModel(data, Headers.TEMPLATES.getValue()));
        } catch (NodataFoundException ex) {
            Log.Debug(this, ex.getMessage());
        }
//        Context c1 = Context.getTemplatesToUsers();
//        c1.addReference(Context.getGroup());
//        c1.addReference(Context.getTemplate());
//        jTable1.setModel(new MPTableModel(new Class[]{Integer.class, String.class, String.class, String.class, String.class, String.class, String.class},
//                QueryHandler.instanceOf().clone(c1).
//                select(Context.DETAILS_TEMPLATES, (String[]) null),
//                Headers.TEMPLATES.getValue()));
//
//        TableFormat.stripFirstColumn(jTable1);
//        TableFormat.format(jTable1, 1, 120);
//        TableFormat.format(jTable1, 5, 80);
//        TableFormat.format(jTable1, 6, 150);

    }

    @Override
    public void paste(DatabaseObject... dbos) {
        for (DatabaseObject dbo : dbos) {
            if (dbo.getContext().equals(Context.getTemplate())) {
                setDataOwner(dbo, true);
            } else {
                mpv5.YabsViewProxy.instance().addMessage(Messages.NOT_POSSIBLE.toString() + Messages.ACTION_PASTE, Color.RED);
            }
        }
    }

    @Override
    public void showRequiredFields() {
        TextFieldUtils.blinkerRed(fullname);
    }

    @Override
    public void showSearchBar(boolean show) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Component getAndRemoveActionPanel() {
        this.remove(jPanel6);
        validate();
        return jPanel6;
    }

    @Override
    public void actionAfterSave() {
        Object[] selectedValues = jList1.getSelectedValues();
        Integer groups;
        if (groupname.getSelectedItem() != null) {
            groups = Integer.valueOf(((MPComboBoxModelItem) groupname.getSelectedItem()).getId());
        } else {
            groups = 1;
        }

        Object[][] UtT = new Object[0][0];
        QueryCriteria d = new QueryCriteria("templatesids", dataOwner.__getIDS());
        try {
            UtT = QueryHandler.instanceOf().clone(Context.getTemplatesToUsers()).select(d).getData();
        } catch (NodataFoundException ex) {
            Log.Debug(this, ex.getMessage());
        }
        for (int i = 0; i < selectedValues.length; i++) {
            User object = (User) selectedValues[i];
            boolean found = false;

            for (int j = 0; j < UtT.length; j++) {
                if (Integer.parseInt(UtT[j][2].toString()) == object.__getIDS()) {
                    found = true;
                    UtT[j][1] = "found";
                    break;
                }
            }

            if (!found) {
                QueryData c = new QueryData();
                c.add("usersids", object.__getIDS());
                c.add("templatesids", dataOwner.__getIDS());
                c.add("groupsids", groups);
                c.add("cname", dataOwner.__getIDS() + "@" + object.__getIDS() + "@" + groups);
                QueryHandler.instanceOf().clone(Context.getTemplatesToUsers()).insert(c, null);
            }
        }

        for (int j = 0; j < UtT.length; j++) {
            if (!UtT[j][1].equals("found")) {
                QueryCriteria d2 = new QueryCriteria("cname", UtT[j][1].toString());
                QueryHandler.instanceOf().clone(Context.getTemplatesToUsers()).delete(d2);
            }
        }

        TemplateHandler.clearCache();
    }

    @Override
    public void actionAfterCreate() {
    }

    private void test() {
        DatabaseObject t = null;
        if (dataOwner != null) {
            switch (Integer.parseInt(type.getSelectedItem().getId())) {
                case Constants.TYPE_OFFER:
                    t = Search2.showSearchFor(Context.getOffer());
                    break;
                case Constants.TYPE_ORDER:
                    t = Search2.showSearchFor(Context.getOrder());
                    break;
                case Constants.TYPE_INVOICE:
                     t = Search2.showSearchFor(Context.getInvoice());
                     break;
                case Constants.TYPE_DEPOSIT:
                    t = Search2.showSearchFor(Context.getDeposit());
                    break;
                case Constants.TYPE_PART_PAYMENT:
                    t = Search2.showSearchFor(Context.getPartPayment());
                    break;
                case Constants.TYPE_CREDIT:
                     t = Search2.showSearchFor(Context.getCredit());
                     break;
            }

            if (t == null) {
                try {
                    t = DatabaseObject.getObject(Context.getInvoice(), 1);
                } catch (NodataFoundException ex) {
                    t = (Item) Context.getInvoice().getNewObject();
                    Contact k = new Contact();
                    k.avoidNulls();
                    k.fillSampleData();
                    t.avoidNulls();
                    t.fillSampleData();
                    ((Item) t).setContactsids(k.__getIDS());
                }
            }

            try {
                Map<String, Object> hm1 = t.getFormFields();
                File f = dataOwner.getFile();
                File f2 = FileDirectoryHandler.getTempFile("pdf");
                Export ex = new Export(dataOwner);
                ex.putAll(hm1);

                ArrayList<String[]> l = new ArrayList<String[]>();

                for (int i = 0; i < 20; i++) {
                    l.add(SubItem.getRandomItem().toStringArray());
                }

                ex.put(ODTFile2.KEY_TABLE + "1", l);
                ex.setTemplate(new ODTFile2(f.getPath()));

                ex.setTargetFile(f2);

                new Job(ex, new PreviewWaiter()).execute();
            } catch (Exception ex1) {
                Log.Debug(ex1);
                Popup.error(ex1);
            }
        }
    }

    @Override
    public void actionBeforeCreate() {
    }

    @Override
    public void actionBeforeSave() {
    }

    @Override
    public void mail() {
    }

    @Override
    public void print() {
    }

    private void configureUpdateService(File fi) {
        lastImportedFile = fi;
        updateService.setEnabled(true);
        try {
            pathtofile.setText(fi.getCanonicalPath());
        } catch (IOException ex) {
            Log.Debug(this, ex);
        }
        lastmodified = fi.lastModified();
    }
}
