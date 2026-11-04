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
package mpv5.ui.panels;

import mpv5.handler.TemplateHandler;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import mpv5.logging.Log;
import mpv5.ui.dialogs.DialogForFile;
import mpv5.ui.dialogs.Popup;
import mpv5.utils.export.Export;
import mpv5.utils.export.Exportable;
import mpv5.utils.files.FileDirectoryHandler;
import mpv5.utils.jobs.Job;

/**
 *
 *  
 */
public class RemindPanel extends javax.swing.JPanel {

    private static final long serialVersionUID = 1L;
    Reminder dataOwner = new Reminder();
    private java.util.ResourceBundle bundle = mpv5.i18n.LanguageManager.getBundle();

    /** Creates new form GeneralListPanel */
    public RemindPanel() {
        initComponents();
        labeledTextField1.set_ValueClass(Double.class);

        labeledCombobox1.setSearchEnabled(true);
        labeledCombobox1.setContext(Context.getInvoice());

        labeledCombobox3.setSearchEnabled(true);
        labeledCombobox3.setContext(Context.getStage());

        labeledCombobox1.getComboBox().addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    refresh((Item) DatabaseObject.getObject(Context.getInvoice(), Integer.valueOf(labeledCombobox1.getSelectedItem().getId())));
                } catch (Exception ex) {
                    Log.Debug(this, ex.getMessage());
                }
            }
        });

        labeledCombobox3.getComboBox().addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    Stage s = (Stage) DatabaseObject.getObject(Context.getStage(), Integer.valueOf(labeledCombobox3.getSelectedItem().getId()));
                    jTextPane1.setText(s.__getDescription());
                    labeledTextField1.setText(s.__getExtravalue());
                } catch (Exception ex) {
                    Log.Debug(this, ex.getMessage());
                }
            }
        });


        labeledCombobox2.setContext(Context.getGroup());
        labeledCombobox2.setSearchEnabled(true);
        labeledCombobox2.setEditable(true);
        labeledCombobox2.triggerSearch();
    }

    public RemindPanel(Item bill) {
        initComponents();
        labeledCombobox1.getComboBox().addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    refresh((Item) DatabaseObject.getObject(Context.getInvoice(), Integer.valueOf(labeledCombobox1.getSelectedItem().getId())));
                } catch (Exception ex) {
                    Log.Debug(this, ex.getMessage());
                }
            }
        });

        labeledCombobox3.getComboBox().addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    Stage s = (Stage) DatabaseObject.getObject(Context.getStage(), Integer.valueOf(labeledCombobox3.getSelectedItem().getId()));
                    jTextPane1.setText(s.__getDescription());
                    labeledTextField1.setText(s.__getExtravalue());
                } catch (Exception ex) {
                    Log.Debug(this, ex.getMessage());
                }
            }
        });
        labeledTextField1.set_ValueClass(Double.class);
        labeledCombobox3.setSearchEnabled(true);
        labeledCombobox3.setContext(Context.getStage());
        labeledCombobox1.setModel(bill);
        labeledCombobox3.triggerSearch();
        refresh(bill);
        labeledCombobox2.setContext(Context.getGroup());
        labeledCombobox2.setSearchEnabled(true);
        labeledCombobox2.setEditable(true);
        labeledCombobox2.triggerSearch();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jToolBar1 = new javax.swing.JToolBar();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton5 = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JToolBar.Separator();
        jButton4 = new javax.swing.JButton();
        jButton6 = new javax.swing.JButton();
        jSeparator3 = new javax.swing.JToolBar.Separator();
        jButton3 = new javax.swing.JButton();
        jButton7 = new javax.swing.JButton();
        jButton8 = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        labeledCombobox1 = new mpv5.ui.beans.LabeledCombobox();
        labeledCombobox3 = new mpv5.ui.beans.LabeledCombobox();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextPane1 = new javax.swing.JTextPane();
        labeledTextField1 = new mpv5.ui.beans.LabeledTextField();
        labeledCombobox2 = new mpv5.ui.beans.LabeledCombobox();
        jLabel1 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jList1 = new javax.swing.JList();

        mpv5.i18n.LanguageManager.getBundle();
        setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("RemindPanel.border.title"))); // NOI18N
        setName("Form"); // NOI18N

        jToolBar1.setFloatable(false);
        jToolBar1.setRollover(true);
        jToolBar1.setName("jToolBar1"); // NOI18N

        jButton1.setText(bundle.getString("RemindPanel.jButton1.text")); // NOI18N
        jButton1.setName("jButton1"); // NOI18N
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        jToolBar1.add(jButton1);

        jButton2.setText(bundle.getString("RemindPanel.jButton2.text")); // NOI18N
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

        jButton5.setText(bundle.getString("RemindPanel.jButton5.text")); // NOI18N
        jButton5.setFocusable(false);
        jButton5.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton5.setName("jButton5"); // NOI18N
        jButton5.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton5ActionPerformed(evt);
            }
        });
        jToolBar1.add(jButton5);

        jSeparator1.setName("jSeparator1"); // NOI18N
        jToolBar1.add(jSeparator1);

        jButton4.setText(bundle.getString("RemindPanel.jButton4.text")); // NOI18N
        jButton4.setName("jButton4"); // NOI18N
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });
        jToolBar1.add(jButton4);

        jButton6.setText(bundle.getString("RemindPanel.jButton6.text")); // NOI18N
        jButton6.setName("jButton6"); // NOI18N
        jButton6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton6ActionPerformed(evt);
            }
        });
        jToolBar1.add(jButton6);

        jSeparator3.setName("jSeparator3"); // NOI18N
        jToolBar1.add(jSeparator3);

        jButton3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/22/view_text.png"))); // NOI18N
        jButton3.setText(bundle.getString("RemindPanel.jButton3.text")); // NOI18N
        jButton3.setFocusable(false);
        jButton3.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton3.setName("jButton3"); // NOI18N
        jButton3.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });
        jToolBar1.add(jButton3);

        jButton7.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/22/mimetypes/pdf.png"))); // NOI18N
        jButton7.setText(bundle.getString("RemindPanel.jButton7.text")); // NOI18N
        jButton7.setFocusable(false);
        jButton7.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton7.setName("jButton7"); // NOI18N
        jButton7.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton7ActionPerformed(evt);
            }
        });
        jToolBar1.add(jButton7);

        jButton8.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/22/mimetypes/odt.png"))); // NOI18N
        jButton8.setText(bundle.getString("RemindPanel.jButton8.text")); // NOI18N
        jButton8.setFocusable(false);
        jButton8.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton8.setName("jButton8"); // NOI18N
        jButton8.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton8ActionPerformed(evt);
            }
        });
        jToolBar1.add(jButton8);

//$2java.awt.Color(255, 255, 255));
        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("RemindPanel.jPanel1.border.title"))); // NOI18N
        jPanel1.setName("jPanel1"); // NOI18N

        labeledCombobox1.set_Label(bundle.getString("RemindPanel.labeledCombobox1._Label")); // NOI18N
        labeledCombobox1.setName("labeledCombobox1"); // NOI18N

        labeledCombobox3.set_Label(bundle.getString("RemindPanel.labeledCombobox3._Label")); // NOI18N
        labeledCombobox3.setName("labeledCombobox3"); // NOI18N

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        jTextPane1.setName("jTextPane1"); // NOI18N
        jScrollPane1.setViewportView(jTextPane1);

        labeledTextField1.set_Label(bundle.getString("RemindPanel.labeledTextField1._Label")); // NOI18N
        labeledTextField1.setName("labeledTextField1"); // NOI18N

        labeledCombobox2.set_Label(bundle.getString("RemindPanel.labeledCombobox2._Label")); // NOI18N
        labeledCombobox2.setName("labeledCombobox2"); // NOI18N

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
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 554, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(labeledCombobox3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(labeledCombobox1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 270, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(labeledCombobox2, javax.swing.GroupLayout.DEFAULT_SIZE, 278, Short.MAX_VALUE))))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(labeledCombobox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(labeledCombobox3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(labeledCombobox2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 138, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(labeledTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jLabel1.setText(bundle.getString("RemindPanel.jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        jScrollPane2.setName("jScrollPane2"); // NOI18N

        jList1.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jList1.setName("jList1"); // NOI18N
        jList1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jList1MouseClicked(evt);
            }
        });
        jScrollPane2.setViewportView(jList1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 568, Short.MAX_VALUE)
            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 568, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jToolBar1, javax.swing.GroupLayout.DEFAULT_SIZE, 558, Short.MAX_VALUE)
                .addContainerGap())
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jToolBar1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 143, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        if (dataOwner == null) {
            dataOwner = new Reminder();

        }
        dataOwner.setIDS(-1);
        save();
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
        dataOwner.delete();
        try {
            refresh((Item) Item.getObject(Context.getInvoice(), dataOwner.__getItemsids()));
        } catch (NodataFoundException ex) {
            refresh(null);
        }
    }//GEN-LAST:event_jButton5ActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        String msg = Popup.Enter_Value(Messages.ENTER_A_DESCRIPTION);
        if (msg != null) {
            Stage s = new Stage();
            s.setCname(msg);
            s.setGroupsids(mpv5.db.objects.User.getCurrentUser().__getGroupsids());
            s.setDescription(jTextPane1.getText());
            s.setTemplategroup(mpv5.db.objects.User.getCurrentUser().__getGroupsids());
            try {
                s.setExtravalue(Double.valueOf(labeledTextField1.getText()));
            } catch (NumberFormatException e) {
                s.setExtravalue(0d);
            }
            s.save();
            labeledCombobox3.triggerSearch();
        }
    }//GEN-LAST:event_jButton4ActionPerformed

    private void jButton6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton6ActionPerformed
        try {
            Stage s = (Stage) DatabaseObject.getObject(Context.getStage(), Integer.valueOf(labeledCombobox3.getSelectedItem().getId()));
            s.delete();
            labeledCombobox3.triggerSearch();
        } catch (NodataFoundException ex) {
            Log.Debug(this, ex.getMessage());
        }

    }//GEN-LAST:event_jButton6ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed

        int group = 1;

        PreviewWaiter pr;
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
                try {
                    Stage g = (Stage) DatabaseObject.getObject(Context.getStage(), dataOwner.__getStagesids());
                    ex.put("reminder.stage", g.__getCname());
                } catch (NodataFoundException ex1) {
                    Log.Debug(ex1);
                }
                try {
                    Item i = (Item) DatabaseObject.getObject(Context.getInvoice(), dataOwner.__getItemsids());
                    ex.put("reminder.count", Reminder.getRemindersOf(i).size());
                } catch (NodataFoundException nodataFoundException) {
                    Log.Debug(nodataFoundException);
                }

                pr = new PreviewWaiter();
                pr.setDataOwner(dataOwner);
                new Job(ex, pr).execute();
            } else {
                Popup.notice(Messages.NO_TEMPLATE_DEFINDED);
            }
        }
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jList1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jList1MouseClicked
        try {
            expose((Reminder) jList1.getSelectedValue());
        } catch (Exception e) {
        }
    }//GEN-LAST:event_jList1MouseClicked

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed

        save();
    }//GEN-LAST:event_jButton1ActionPerformed

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

                try {
                    Stage g = (Stage) DatabaseObject.getObject(Context.getStage(), dataOwner.__getStagesids());
                    hm1.put("reminder.stage", g.__getCname());
                } catch (NodataFoundException ex1) {
                    Log.Debug(ex1);
                }
                try {
                    Item i = (Item) DatabaseObject.getObject(Context.getInvoice(), dataOwner.__getItemsids());
                    hm1.put("reminder.count", Reminder.getRemindersOf(i).size());
                } catch (NodataFoundException nodataFoundException) {
                    Log.Debug(nodataFoundException);
                }

                new Job(Export.createFile(dataOwner.__getCname(), t, dataOwner, hm1), new DialogForFile(DialogForFile.FILES_ONLY, dataOwner.__getCname())).execute();
            } else {
                Popup.notice(Messages.NO_TEMPLATE_DEFINDED);
            }
        }
    }//GEN-LAST:event_jButton7ActionPerformed

    private void jButton8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton8ActionPerformed

        int group = 1;

        PreviewWaiter pr;
        if (dataOwner != null && dataOwner.isExisting()) {

            if (labeledCombobox2.getSelectedItem() != null) {
                group = Integer.valueOf(labeledCombobox2.getSelectedItem().getId());
            }

            Template t = TemplateHandler.loadTemplate(group, Constants.TYPE_REMINDER);
            if (t != null) {
                Exportable te = TemplateHandler.loadTemplate(group, Constants.TYPE_REMINDER).getExFile();
                HashMap<String, Object> hm1 = new HashMap<String, Object>();
                File f2 = FileDirectoryHandler.getTempFile("odt");

                try {
                    Stage g = (Stage) DatabaseObject.getObject(Context.getStage(), dataOwner.__getStagesids());
                    hm1.put("reminder.stage", g.__getCname());
                } catch (NodataFoundException ex1) {
                    Log.Debug(ex1);
                }
                try {
                    Item i = (Item) DatabaseObject.getObject(Context.getInvoice(), dataOwner.__getItemsids());
                    hm1.put("reminder.count", Reminder.getRemindersOf(i).size());
                } catch (NodataFoundException nodataFoundException) {
                    Log.Debug(nodataFoundException);
                }

                new Job(Export.sourceFile(dataOwner.__getCname(), t, dataOwner, hm1), new DialogForFile(DialogForFile.FILES_ONLY, dataOwner.__getCname())).execute();
            } else {
                Popup.notice(Messages.NO_TEMPLATE_DEFINDED);
            }
        }
    }//GEN-LAST:event_jButton8ActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton7;
    private javax.swing.JButton jButton8;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JList jList1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JToolBar.Separator jSeparator1;
    private javax.swing.JToolBar.Separator jSeparator3;
    private javax.swing.JTextPane jTextPane1;
    private javax.swing.JToolBar jToolBar1;
    private mpv5.ui.beans.LabeledCombobox labeledCombobox1;
    private mpv5.ui.beans.LabeledCombobox labeledCombobox2;
    private mpv5.ui.beans.LabeledCombobox labeledCombobox3;
    private mpv5.ui.beans.LabeledTextField labeledTextField1;
    // End of variables declaration//GEN-END:variables

    private void save() {
        try {
            Item i = (Item) DatabaseObject.getObject(Context.getInvoice(), Integer.valueOf(labeledCombobox1.getSelectedItem().getId()));
            Stage s;
            try {
                s = (Stage) DatabaseObject.getObject(Context.getStage(), Integer.valueOf(labeledCombobox3.getSelectedItem().getId()));
            } catch (Exception nodataFoundException) {
                String msg = Popup.Enter_Value(Messages.ENTER_A_DESCRIPTION);
                if (msg == null) {
                    msg = "autogenerated stage";
                }
                s = new Stage();
                s.setCname(msg);
                s.setGroupsids(mpv5.db.objects.User.getCurrentUser().__getGroupsids());
                s.setTemplategroup(mpv5.db.objects.User.getCurrentUser().__getGroupsids());
                s.setDescription(jTextPane1.getText());
                try {
                    s.setExtravalue(labeledTextField1.getValue(0d));
                } catch (NumberFormatException numberFormatException) {
                    s.setExtravalue(0d);
                }
                s.save();
                labeledCombobox3.triggerSearch();

            }
            if(dataOwner==null){
                dataOwner = new Reminder();
            }
            dataOwner.setDescription(jTextPane1.getText());
            try {
                dataOwner.setExtravalue(Double.valueOf(labeledTextField1.getText()));
            } catch (NumberFormatException numberFormatException) {
                dataOwner.setExtravalue(0d);
            }
            dataOwner.setItemsids(i.__getIDS());
            dataOwner.setStagesids(s.__getIDS());
            dataOwner.setCname(s + "@" + i);
            dataOwner.save();
            refresh(i);
        } catch (Exception ex) {
            Log.Debug(this, ex);
        }
    }

    private void refresh(Item bill) {
        if (bill != null) {
            DefaultListModel d = new DefaultListModel();
            List<Reminder> data = Reminder.getRemindersOf(bill);
            for (int i = 0; i < data.size(); i++) {
                Reminder reminder = data.get(i);
                d.addElement(reminder);
            }
            jList1.setModel(d);
        } else {
            jList1.setModel(new DefaultListModel());
        }
    }

    private void expose(Reminder r) {
        dataOwner = r;
        try {
            labeledCombobox1.setModel(Item.getObject(Context.getInvoice(), r.__getItemsids()));
        } catch (NodataFoundException ex) {
        }
        try {
            labeledCombobox2.setModel(Item.getObject(Context.getGroup(), r.__getGroupsids()));
        } catch (NodataFoundException ex) {
        }


        labeledTextField1.setText(r.__getExtravalue());
        jTextPane1.setText(r.__getDescription());
    }
}
