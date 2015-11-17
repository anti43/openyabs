/*
 *  This file is part of YaBS.
 *
 *      YaBS is free software: you can redistribute it and/or modify
 *      it under the terms of the GNU General Public License as published by
 *      the Free Software Foundation, either version 3 of the License, or
 *      (at your option) any later version.
 *
 *      YaBS is distributed in the hope that it will be useful,
 *      but WITHOUT ANY WARRANTY; without even the implied warranty of
 *      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *      GNU General Public License for more details.
 *
 *      You should have received a copy of the GNU General Public License
 *      along with YaBS.  If not, see <http://www.gnu.org/licenses/>.
 */
/*
 * GeneralListPanel.java
 *
 * Created on 03.04.2009, 15:26:37
 */
package mpv5.ui.dialogs.subcomponents;

import java.awt.Component;
import mpv5.data.PropertyStore;
import mpv5.ui.panels.*;
import enoa.handler.TemplateHandler;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultListModel;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.db.common.NodataFoundException;
import mpv5.db.objects.Item;
import mpv5.db.objects.Reminder;
import mpv5.db.objects.Stage;
import mpv5.db.objects.Template;
import mpv5.globals.Constants;
import mpv5.globals.Messages;
import mpv5.handler.FormFieldsHandler;
import mpv5.logging.Log;
import mpv5.ui.dialogs.ControlApplet;
import mpv5.ui.dialogs.DialogForFile;
import mpv5.ui.dialogs.Popup;
import mpv5.ui.frames.MPView;
import mpv5.utils.export.Export;
import mpv5.utils.export.Exportable;
import mpv5.utils.files.FileDirectoryHandler;
import mpv5.utils.jobs.Job;
import mpv5.utils.models.MPTableModel;
import mpv5.utils.numberformat.FormatNumber;
import mpv5.utils.tables.Selection;

/**
 *
 *  
 */
public class ControlPanel_Reminder extends javax.swing.JPanel implements DataPanel, ControlApplet {

    private static final long serialVersionUID = 1L;
    Stage dataOwner = new Stage();
    private java.util.ResourceBundle bundle = mpv5.i18n.LanguageManager.getBundle();

    /** Creates new form GeneralListPanel */
    public ControlPanel_Reminder() {
        initComponents();
        labeledTextField1.set_ValueClass(Double.class);

        labeledCombobox2.setContext(Context.getGroup());
        labeledCombobox2.setSearchEnabled(true);
        labeledCombobox2.setEditable(true);
        labeledCombobox2.triggerSearch();

        try {
            jTable1.setModel(new MPTableModel(Context.getStage(), jTable1));
        } catch (Exception e) {
            Log.Debug(this, e.getMessage());
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextPane1 = new javax.swing.JTextPane();
        labeledTextField1 = new mpv5.ui.beans.LabeledTextField();
        labeledCombobox2 = new mpv5.ui.beans.LabeledCombobox();
        jLabel1 = new javax.swing.JLabel();
        labeledTextField2 = new mpv5.ui.beans.LabeledTextField();
        jPanel2 = new javax.swing.JPanel();
        jButton3 = new javax.swing.JButton();
        jButton7 = new javax.swing.JButton();
        jButton8 = new javax.swing.JButton();
        jButton6 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JSeparator();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();

        java.util.ResourceBundle bundle = mpv5.i18n.LanguageManager.getBundle(); // NOI18N
        setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("RemindPanel.border.title"))); // NOI18N
        setLayout(new java.awt.BorderLayout());

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("ControlPanel_Reminder.jPanel1.border.title"))); // NOI18N
        jPanel1.setName("jPanel1"); // NOI18N

        jScrollPane1.setName("jScrollPane1"); // NOI18N

//$2java.awt.Color(254, 254, 254));
        jTextPane1.setName("jTextPane1"); // NOI18N
        jScrollPane1.setViewportView(jTextPane1);

        labeledTextField1.set_Label(bundle.getString("ControlPanel_Reminder.labeledTextField1._Label")); // NOI18N
        labeledTextField1.setName("labeledTextField1"); // NOI18N

        labeledCombobox2.set_Label(bundle.getString("ControlPanel_Reminder.labeledCombobox2._Label")); // NOI18N
        labeledCombobox2.setName("labeledCombobox2"); // NOI18N

        jLabel1.setText(bundle.getString("ControlPanel_Reminder.jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        labeledTextField2.set_Label(bundle.getString("ControlPanel_Reminder.labeledTextField2._Label")); // NOI18N
        labeledTextField2.setName("labeledTextField2"); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(labeledTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(2, 2, 2)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 426, Short.MAX_VALUE)
                    .addComponent(labeledTextField2, javax.swing.GroupLayout.DEFAULT_SIZE, 426, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, 187, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(labeledCombobox2, javax.swing.GroupLayout.PREFERRED_SIZE, 227, javax.swing.GroupLayout.PREFERRED_SIZE))))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(labeledCombobox2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(labeledTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 179, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(labeledTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        add(jPanel1, java.awt.BorderLayout.EAST);

//$2java.awt.Color(255, 255, 255));
        jPanel2.setName("jPanel2"); // NOI18N
        jPanel2.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        jButton3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/mpv5/resources/images/22/view_text.png"))); // NOI18N
        jButton3.setText(bundle.getString("ControlPanel_Reminder.jButton3.text")); // NOI18N
        jButton3.setFocusable(false);
        jButton3.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton3.setName("jButton3"); // NOI18N
        jButton3.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });
        jPanel2.add(jButton3);

        jButton7.setIcon(new javax.swing.ImageIcon(getClass().getResource("/mpv5/resources/images/22/mimetypes/pdf.png"))); // NOI18N
        jButton7.setText(bundle.getString("ControlPanel_Reminder.jButton7.text")); // NOI18N
        jButton7.setFocusable(false);
        jButton7.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton7.setName("jButton7"); // NOI18N
        jButton7.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton7ActionPerformed(evt);
            }
        });
        jPanel2.add(jButton7);

        jButton8.setIcon(new javax.swing.ImageIcon(getClass().getResource("/mpv5/resources/images/22/mimetypes/odt.png"))); // NOI18N
        jButton8.setText(bundle.getString("ControlPanel_Reminder.jButton8.text")); // NOI18N
        jButton8.setFocusable(false);
        jButton8.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton8.setName("jButton8"); // NOI18N
        jButton8.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton8ActionPerformed(evt);
            }
        });
        jPanel2.add(jButton8);

        jButton6.setText(bundle.getString("ControlPanel_Reminder.jButton6.text")); // NOI18N
        jButton6.setName("jButton6"); // NOI18N
        jButton6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton6ActionPerformed(evt);
            }
        });
        jPanel2.add(jButton6);

        jButton2.setText(bundle.getString("ControlPanel_Reminder.jButton2.text")); // NOI18N
        jButton2.setName("jButton2"); // NOI18N
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });
        jPanel2.add(jButton2);

        jButton4.setText(bundle.getString("ControlPanel_Reminder.jButton4.text")); // NOI18N
        jButton4.setName("jButton4"); // NOI18N
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });
        jPanel2.add(jButton4);

        jSeparator1.setName("jSeparator1"); // NOI18N
        jPanel2.add(jSeparator1);

        add(jPanel2, java.awt.BorderLayout.PAGE_END);

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("ControlPanel_Reminder.jPanel3.border.title"))); // NOI18N
        jPanel3.setName("jPanel3"); // NOI18N
        jPanel3.setLayout(new java.awt.BorderLayout());

        jScrollPane2.setName("jScrollPane2"); // NOI18N

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        jTable1.setName("jTable1"); // NOI18N
        jTable1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTable1MouseClicked(evt);
            }
        });
        jScrollPane2.setViewportView(jTable1);

        jPanel3.add(jScrollPane2, java.awt.BorderLayout.CENTER);

        add(jPanel3, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        String msg = Popup.Enter_Value(Messages.ENTER_A_DESCRIPTION);
        if (msg != null) {
            Stage s = new Stage();
            s.setCname(msg);
            s.setGroupsids(mpv5.db.objects.User.getCurrentUser().__getGroupsids());
            s.setDescription(jTextPane1.getText());
            s.setExtravalue(FormatNumber.parseDezimal(labeledTextField1.getText()).doubleValue());
            if (labeledCombobox2.getSelectedItem() != null) {
                s.setTemplategroup(Integer.valueOf(labeledCombobox2.getSelectedItem().getId()));
            }

            s.save();
        }
    }//GEN-LAST:event_jButton4ActionPerformed

    private void jButton6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton6ActionPerformed

        dataOwner.delete();
    }//GEN-LAST:event_jButton6ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed

        int group = 1;

        PreviewPanel pr;
        if (dataOwner != null && dataOwner.isExisting()) {

            if (labeledCombobox2.getSelectedItem() != null) {
                group = Integer.valueOf(labeledCombobox2.getSelectedItem().getId());
            }

            Template t = TemplateHandler.loadTemplate(group, Constants.TYPE_REMINDER);
            if (t != null) {
                Exportable te = TemplateHandler.loadTemplate(group, Constants.TYPE_REMINDER).getExFile();
                Map<String, Object> hm1 = dataOwner.getFormFields();//new FormFieldsHandler(dataOwner).getFormattedFormFields(null);
                File f2 = FileDirectoryHandler.getTempFile("pdf");
                Export ex = new Export(t);
                ex.putAll(hm1);
                ex.setTemplate(te);
                ex.setTargetFile(f2);
//                try {
//                    Stage g = (Stage) DatabaseObject.getObject(Context.getStage(), dataOwner.__getStagesids());
//                    ex.put("reminder.stage", g.__getCname());
//                } catch (NodataFoundException ex1) {
//                    Log.Debug(ex1);
//                }
//                try {
//                    Item i = (Item) DatabaseObject.getObject(Context.getItem(), dataOwner.__getItemsids());
//                    ex.put("reminder.count", Reminder.getRemindersOf(i).size());
//                } catch (NodataFoundException nodataFoundException) {
//                    Log.Debug(nodataFoundException);
//                }

                pr = new PreviewPanel();
                pr.setDataOwner(dataOwner);
                new Job(ex, pr).execute();
            } else {
                Popup.notice(Messages.NO_TEMPLATE_DEFINDED);
            }
        }
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jButton7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton7ActionPerformed
        int group = 1;

        if (dataOwner != null && dataOwner.isExisting()) {

            if (labeledCombobox2.getSelectedItem() != null) {
                group = Integer.valueOf(labeledCombobox2.getSelectedItem().getId());
            }

            Template t = TemplateHandler.loadTemplate(group, Constants.TYPE_REMINDER);
            if (t != null) {
                Exportable te = TemplateHandler.loadTemplate(group, Constants.TYPE_REMINDER).getExFile();
                HashMap<String, Object> hm1 = new HashMap<String, Object>();
                File f2 = FileDirectoryHandler.getTempFile("pdf");

//                try {
//                    Stage g = (Stage) DatabaseObject.getObject(Context.getStage(), dataOwner.__getStagesids());
//                    hm1.put("reminder.stage", g.__getCname());
//                } catch (NodataFoundException ex1) {
//                    Log.Debug(ex1);
//                }
//                try {
//                    Item i = (Item) DatabaseObject.getObject(Context.getItem(), dataOwner.__getItemsids());
//                    hm1.put("reminder.count", Reminder.getRemindersOf(i).size());
//                } catch (NodataFoundException nodataFoundException) {
//                    Log.Debug(nodataFoundException);
//                }

                new Job(Export.createFile(dataOwner.__getCname(), t, dataOwner, hm1), new DialogForFile(DialogForFile.FILES_ONLY, dataOwner.__getCname())).execute();
            } else {
                Popup.notice(Messages.NO_TEMPLATE_DEFINDED);
            }
        }
    }//GEN-LAST:event_jButton7ActionPerformed

    private void jButton8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton8ActionPerformed

        int group = 1;

        PreviewPanel pr;
        if (dataOwner != null && dataOwner.isExisting()) {

            if (labeledCombobox2.getSelectedItem() != null) {
                group = Integer.valueOf(labeledCombobox2.getSelectedItem().getId());
            }

            Template t = TemplateHandler.loadTemplate(group, Constants.TYPE_REMINDER);
            if (t != null) {
                Exportable te = TemplateHandler.loadTemplate(group, Constants.TYPE_REMINDER).getExFile();
                HashMap<String, Object> hm1 = new HashMap<String, Object>();
                File f2 = FileDirectoryHandler.getTempFile("odt");
//
//                try {
//                    Stage g = (Stage) DatabaseObject.getObject(Context.getStage(), dataOwner.__getStagesids());
//                    hm1.put("reminder.stage", g.__getCname());
//                } catch (NodataFoundException ex1) {
//                    Log.Debug(ex1);
//                }
//                try {
//                    Item i = (Item) DatabaseObject.getObject(Context.getItem(), dataOwner.__getItemsids());
//                    hm1.put("reminder.count", Reminder.getRemindersOf(i).size());
//                } catch (NodataFoundException nodataFoundException) {
//                    Log.Debug(nodataFoundException);
//                }

                new Job(Export.sourceFile(dataOwner.__getCname(), t, dataOwner, hm1), new DialogForFile(DialogForFile.FILES_ONLY, dataOwner.__getCname())).execute();
            } else {
                Popup.notice(Messages.NO_TEMPLATE_DEFINDED);
            }
        }
    }//GEN-LAST:event_jButton8ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed

        if (dataOwner != null) {
            DatabaseObject dato = dataOwner;
            dato.getPanelData(this);
            if (dato.save()) {
            } else {
                showRequiredFields();
            }
        }
}//GEN-LAST:event_jButton2ActionPerformed

    private void jTable1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTable1MouseClicked
        Selection s = new Selection(jTable1);
        if (s.checkID()) {
            try {
                setDataOwner(DatabaseObject.getObject(Context.getStage(), s.getId()), true);
            } catch (NodataFoundException ex) {
            }
        }
    }//GEN-LAST:event_jTable1MouseClicked
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton7;
    private javax.swing.JButton jButton8;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JTable jTable1;
    private javax.swing.JTextPane jTextPane1;
    private mpv5.ui.beans.LabeledCombobox labeledCombobox2;
    private mpv5.ui.beans.LabeledTextField labeledTextField1;
    private mpv5.ui.beans.LabeledTextField labeledTextField2;
    // End of variables declaration//GEN-END:variables
    private double extravalue_;
    public String cname_;
    public String description_;
    public int intaddedby_;
    public int ids_;
    public Date dateadded_;
    public int groupsids_ = 1;
    public int templategroup_;

    public boolean save() {

        Selection sel = new Selection(jTable1);
        if (sel.checkID()) {
            try {

                Stage s = null;
                try {
                    s = (Stage) DatabaseObject.getObject(Context.getStage(), sel.getId());
                } catch (Exception nodataFoundException) {
                    String msg = Popup.Enter_Value(Messages.ENTER_A_DESCRIPTION);
                    if (msg == null) {
                        msg = "autogenerated stage";
                    }
                    s = new Stage();
                    s.setCname(msg);
                    s.setGroupsids(mpv5.db.objects.User.getCurrentUser().__getGroupsids());
                    s.setDescription(jTextPane1.getText());
                    try {
                        s.setExtravalue(labeledTextField1.getValue(0d));
                    } catch (NumberFormatException numberFormatException) {
                        s.setExtravalue(0d);
                    }
                    s.save();
                }
                dataOwner.setDescription(jTextPane1.getText());
                try {
                    dataOwner.setExtravalue(Double.valueOf(labeledTextField1.getText()));
                } catch (NumberFormatException numberFormatException) {
                    dataOwner.setExtravalue(0d);
                }
            } catch (Exception ex) {
                Log.Debug(this, ex);
                return false;
            }
        }
        return true;
    }

    private void refresh(Item bill) {
        if (bill != null) {
            DefaultListModel d = new DefaultListModel();
            List<Reminder> data = Reminder.getRemindersOf(bill);
            for (int i = 0; i < data.size(); i++) {
                Reminder reminder = data.get(i);
                d.addElement(reminder);
            }

        } else {
        }
    }

    private void expose(Stage r) {
        dataOwner = r;
        try {
            labeledCombobox2.setModel(Item.getObject(Context.getGroup(), r.__getGroupsids()));
        } catch (NodataFoundException ex) {
        }

        labeledTextField1.setText(r.__getExtravalue());
        jTextPane1.setText(r.__getDescription());
        labeledTextField2.setText(r.__getCname());
    }

    @Override
    public boolean collectData() {
        try {
            extravalue_ = labeledTextField1.getValue(0d);
            cname_ = labeledTextField2.getText(true, Messages.NAME.getValue());
            description_ = jTextPane1.getText();
            if (labeledCombobox2.getSelectedItem() != null) {
                templategroup_ = (Integer.valueOf(labeledCombobox2.getSelectedItem().getId()));
            } else {
                templategroup_ = 1;
            }
        } catch (Exception exception) {
            return false;
        }
        return false;
    }

    @Override
    public DatabaseObject getDataOwner() {
        return dataOwner;
    }

    @Override
    public void setDataOwner(DatabaseObject object, boolean populateData) {
        dataOwner = (Stage) object;
        if (populateData) {
            expose(dataOwner);
        }
    }

    @Override
    public void refresh() {
        refresh(null);
    }

    @Override
    public void exposeData() {
        expose(dataOwner);
    }

    @Override
    public void paste(DatabaseObject... dbo) {
        if (dbo.length > 0) {
            setDataOwner(dbo[0], true);
        }
    }

    @Override
    public void showRequiredFields() {
    }

    @Override
    public void showSearchBar(boolean show) {
    }

    @Override
    public void actionAfterSave() {
    }

    @Override
    public void actionAfterCreate() {
    }

    @Override
    public void actionBeforeCreate() {
    }

    @Override
    public void actionBeforeSave() throws ChangeNotApprovedException {
    }

    @Override
    public void mail() {
    }

    @Override
    public void print() {
    }

    @Override
    public Component getAndRemoveActionPanel() {
        this.remove(jPanel2);
        validate();
        return jPanel2;
    }

    @Override
    public void setValues(PropertyStore values) {
    }

    @Override
    public String getUname() {
        return "Stages";
    }

    @Override
    public void reset() {
    }
}
