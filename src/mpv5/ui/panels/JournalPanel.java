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
 * HistoryPanel.java
 *
 * Created on 30.03.2009, 12:05:51
 */
package mpv5.ui.panels;

import enoa.handler.TableHandler;
import enoa.handler.TemplateHandler;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import mpv5.db.common.Context;

import mpv5.db.common.DatabaseObject;
import mpv5.db.common.NodataFoundException;
import mpv5.db.common.QueryCriteria2;
import mpv5.db.common.QueryHandler;
import mpv5.db.common.QueryParameter;
import mpv5.db.objects.Contact;
import mpv5.db.objects.Expense;
import mpv5.globals.Headers;
import mpv5.db.objects.Group;
import mpv5.db.objects.Item;
import mpv5.db.objects.Revenue;
import mpv5.db.objects.Template;
import mpv5.globals.Messages;
import mpv5.logging.Log;
import mpv5.ui.beans.MPCombobox;
import mpv5.ui.dialogs.DialogForFile;
import mpv5.ui.dialogs.Popup;
import mpv5.ui.frames.MPView;
import mpv5.ui.popups.TablePopUp;
import mpv5.utils.arrays.ArrayUtilities;
import mpv5.utils.date.DateConverter;
import mpv5.utils.date.vTimeframe;
import mpv5.utils.export.DTAFile;
import mpv5.utils.export.Export;
import mpv5.utils.jobs.Job;
import mpv5.utils.models.MPComboBoxModelItem;
import mpv5.utils.models.MPTableModel;
import mpv5.utils.models.MPTreeModel;
import mpv5.utils.tables.ExcelAdapter;
import mpv5.utils.tables.TableFormat;

/**
 *
 *  
 */
public class JournalPanel extends javax.swing.JPanel implements ListPanel {

    private static JournalPanel t;
    private static final long serialVersionUID = 1L;

    public static JComponent instanceOf() {
        if (t == null) {
            t = new JournalPanel();
        }
        return t;
    }
    private Contact dataowner;
    private Template preloadedTemplate;

    /** Creates new form HistoryPanel */
    public JournalPanel() {
        initComponents();
        new ExcelAdapter(jTable1);
        setPopup();
        jPanel5.setEnabled(false);
        jTabbedPane1.removeTabAt(0);
        jLabel3.setEnabled(false);
        jLabel4.setEnabled(false);
        jLabel4.setText("");
        jTabbedPane1.setSelectedComponent(jPanel4);
        validate();
        timeframeChooser1.setTime(new vTimeframe(DateConverter.getDate(DateConverter.getYear()), new Date()));

        groups.setSearchEnabled(true);
        groups.setContext(Context.getGroup());
        groups.triggerSearch();

        account1.setSearchEnabled(true);
        account1.setContext(Context.getAccounts());

        statusc.getComboBox().setModel(new DefaultComboBoxModel(new Object[]{Messages.ALL, Messages.STATUS_PAID, Messages.STATUS_UNPAID}));

        prinitingComboBox1.init(jTable1);
//        try {
//            prinitingComboBox1.addAction(this.getClass().getMethod("dta", null), Messages.DTAUS);
//        } catch (NoSuchMethodException ex) {
//            Logger.getLogger(JournalPanel.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (SecurityException ex) {
//            Logger.getLogger(JournalPanel.class.getName()).log(Level.SEVERE, null, ex);
//        }
        refresh(null);
        jButton4.setEnabled(false);
        loadTemplate();
    }

    public JournalPanel(Contact dataOwner) {
        initComponents();
        setPopup();
        jLabel4.setText(dataOwner.__getCName());
        timeframeChooser1.setTime(new vTimeframe(DateConverter.getDate(DateConverter.getYear()), new Date()));
        prinitingComboBox1.init(jTable1);

        dataowner = dataOwner;
        groups.setSearchEnabled(true);
        groups.setContext(Context.getGroup());
        groups.triggerSearch();

        account1.setSearchEnabled(true);
        account1.setContext(Context.getAccounts());

        statusc.getComboBox().setModel(new DefaultComboBoxModel(new Object[]{Messages.ALL, Messages.STATUS_PAID, Messages.STATUS_UNPAID}));

        refresh(null);

        jTree1.setCellRenderer(MPTreeModel.getRenderer());
        jTree1.setModel(new MPTreeModel(dataowner));
        jTree1.addMouseListener(MPTreeModel.getDefaultTreeListener(jTree1));
        prinitingComboBox1.init(jTable1);
//        TreeFormat.expandTree(jTree1);
        loadTemplate();
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
        jPanel1 = new javax.swing.JPanel();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel5 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTree1 = new javax.swing.JTree();
        jPanel4 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jPanel2 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        timeframeChooser1 = new mpv5.ui.beans.TimeframeChooser();
        jLabel2 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        jLabel6 = new javax.swing.JLabel();
        statusc = new mpv5.ui.beans.MPCombobox();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        includechildgroups = new javax.swing.JCheckBox();
        groups = new mpv5.ui.beans.MPCombobox();
        jScrollPane3 = new javax.swing.JScrollPane();
        accountsp = new javax.swing.JPanel();
        account1 = new mpv5.ui.beans.MPCombobox();
        jLabel7 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jButton4 = new javax.swing.JButton();
        prinitingComboBox1 = new mpv5.ui.beans.PrinitingComboBox();
        revenue = new mpv5.ui.beans.LabeledTextField();
        volume = new mpv5.ui.beans.LabeledTextField();
        jButton5 = new javax.swing.JButton();

        setName("Form"); // NOI18N

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("mpv5/resources/languages/Panels"); // NOI18N
        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("JournalPanel.jPanel1.border.title"))); // NOI18N
        jPanel1.setName("jPanel1"); // NOI18N

        jTabbedPane1.setName("jTabbedPane1"); // NOI18N

        jPanel5.setName("jPanel5"); // NOI18N
        jPanel5.setLayout(new java.awt.BorderLayout());

        jScrollPane2.setName("jScrollPane2"); // NOI18N

        javax.swing.tree.DefaultMutableTreeNode treeNode1 = new javax.swing.tree.DefaultMutableTreeNode("root");
        jTree1.setModel(new javax.swing.tree.DefaultTreeModel(treeNode1));
        jTree1.setName("jTree1"); // NOI18N
        jScrollPane2.setViewportView(jTree1);

        jPanel5.add(jScrollPane2, java.awt.BorderLayout.CENTER);

        jTabbedPane1.addTab(bundle.getString("JournalPanel.jPanel5.TabConstraints.tabTitle"), jPanel5); // NOI18N

        jPanel4.setName("jPanel4"); // NOI18N
        jPanel4.setLayout(new java.awt.BorderLayout());

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        jTable1.setAutoCreateRowSorter(true);
        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        jTable1.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_ALL_COLUMNS);
        jTable1.setCellSelectionEnabled(true);
        jTable1.setFillsViewportHeight(true);
        jTable1.setName("jTable1"); // NOI18N
        jTable1.setSelectionMode(javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        jTable1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTable1MouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(jTable1);

        jPanel4.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("JournalPanel.jPanel2.border.title"))); // NOI18N
        jPanel2.setName("jPanel2"); // NOI18N

        jLabel3.setFont(new java.awt.Font("Dialog", 0, 12));
        jLabel3.setText(bundle.getString("JournalPanel.jLabel3.text")); // NOI18N
        jLabel3.setName("jLabel3"); // NOI18N

        jLabel4.setFont(new java.awt.Font("Dialog", 0, 12));
        jLabel4.setText(bundle.getString("JournalPanel.jLabel4.text")); // NOI18N
        jLabel4.setName("jLabel4"); // NOI18N

        jLabel5.setFont(new java.awt.Font("Dialog", 0, 12));
        jLabel5.setText(bundle.getString("JournalPanel.jLabel5.text")); // NOI18N
        jLabel5.setName("jLabel5"); // NOI18N

        timeframeChooser1.setName("timeframeChooser1"); // NOI18N

        jLabel2.setFont(new java.awt.Font("Dialog", 0, 12));
        jLabel2.setText(bundle.getString("JournalPanel.jLabel2.text")); // NOI18N
        jLabel2.setName("jLabel2"); // NOI18N

        jButton1.setText(bundle.getString("JournalPanel.jButton1.text")); // NOI18N
        jButton1.setName("jButton1"); // NOI18N
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jLabel6.setFont(new java.awt.Font("Dialog", 0, 12));
        jLabel6.setText(bundle.getString("JournalPanel.jLabel6.text")); // NOI18N
        jLabel6.setName("jLabel6"); // NOI18N

        statusc.setName("statusc"); // NOI18N

        jButton2.setText(bundle.getString("JournalPanel.jButton2.text")); // NOI18N
        jButton2.setName("jButton2"); // NOI18N
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jButton3.setText(bundle.getString("JournalPanel.jButton3.text")); // NOI18N
        jButton3.setName("jButton3"); // NOI18N
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        includechildgroups.setSelected(true);
        includechildgroups.setText(bundle.getString("JournalPanel.includechildgroups.text")); // NOI18N
        includechildgroups.setName("includechildgroups"); // NOI18N

        groups.setName("groups"); // NOI18N

        jScrollPane3.setName("jScrollPane3"); // NOI18N

        accountsp.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        accountsp.setName("accountsp"); // NOI18N
        accountsp.setLayout(new javax.swing.BoxLayout(accountsp, javax.swing.BoxLayout.PAGE_AXIS));

        account1.setName("account1"); // NOI18N
        accountsp.add(account1);

        jScrollPane3.setViewportView(accountsp);

        jLabel7.setFont(new java.awt.Font("Dialog", 0, 12));
        jLabel7.setText(bundle.getString("JournalPanel.jLabel7.text")); // NOI18N
        jLabel7.setName("jLabel7"); // NOI18N

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 137, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, 148, Short.MAX_VALUE)
                    .addComponent(jLabel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel6, javax.swing.GroupLayout.DEFAULT_SIZE, 122, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 269, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(timeframeChooser1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(statusc, javax.swing.GroupLayout.PREFERRED_SIZE, 243, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 242, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton3)
                        .addGap(18, 18, 18)
                        .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(groups, javax.swing.GroupLayout.PREFERRED_SIZE, 243, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(includechildgroups, javax.swing.GroupLayout.PREFERRED_SIZE, 176, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(171, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel4)
                    .addComponent(jLabel3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(timeframeChooser1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(groups, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2)
                    .addComponent(includechildgroups))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(statusc, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                            .addComponent(jButton3)
                            .addComponent(jButton2)
                            .addComponent(jButton1))
                        .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jLabel7)))
        );

        jPanel4.add(jPanel2, java.awt.BorderLayout.PAGE_START);

        jPanel3.setName("jPanel3"); // NOI18N

        jButton4.setText(bundle.getString("JournalPanel.jButton4.text")); // NOI18N
        jButton4.setName("jButton4"); // NOI18N
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        prinitingComboBox1.setName("prinitingComboBox1"); // NOI18N

        revenue.set_Label(bundle.getString("JournalPanel.revenue._Label")); // NOI18N
        revenue.setName("revenue"); // NOI18N

        volume.set_Label(bundle.getString("JournalPanel.volume._Label")); // NOI18N
        volume.setName("volume"); // NOI18N

        jButton5.setText(bundle.getString("JournalPanel.jButton5.text")); // NOI18N
        jButton5.setName("jButton5"); // NOI18N
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton5ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(prinitingComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, 196, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(volume, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(revenue, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(prinitingComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(volume, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(revenue, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton4, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton5, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel4.add(jPanel3, java.awt.BorderLayout.PAGE_END);

        jTabbedPane1.addTab(bundle.getString("JournalPanel.jPanel4.TabConstraints.tabTitle"), jPanel4); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 789, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 334, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        setData();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jTable1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTable1MouseClicked
        if (evt.getClickCount() > 1) {
            try {
                DatabaseObject obj = DatabaseObject.getObject((Context) jTable1.getValueAt(jTable1.getSelectedRow(), 9), Integer.valueOf(jTable1.getValueAt(jTable1.getSelectedRow(), 0).toString()));
                MPView.identifierView.addTab(obj);
            } catch (NodataFoundException ex) {
                Log.Debug(ex);
            }
        }
    }//GEN-LAST:event_jTable1MouseClicked

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        MPCombobox b = new MPCombobox();
        b.setSearchEnabled(true);
        b.setContext(Context.getAccounts());
        accountsp.add(b);
        validate();
        repaint();
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        try {
            accountsp.remove(accountsp.getComponentCount() - 1);
        } catch (Exception e) {
        }
        validate();
        repaint();
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed

        preview();
    }//GEN-LAST:event_jButton4ActionPerformed

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
      dta();

    }//GEN-LAST:event_jButton5ActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private mpv5.ui.beans.MPCombobox account1;
    private javax.swing.JPanel accountsp;
    private javax.swing.ButtonGroup buttonGroup1;
    private mpv5.ui.beans.MPCombobox groups;
    private javax.swing.JCheckBox includechildgroups;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JTree jTree1;
    private mpv5.ui.beans.PrinitingComboBox prinitingComboBox1;
    private mpv5.ui.beans.LabeledTextField revenue;
    private mpv5.ui.beans.MPCombobox statusc;
    private mpv5.ui.beans.TimeframeChooser timeframeChooser1;
    private mpv5.ui.beans.LabeledTextField volume;
    // End of variables declaration//GEN-END:variables

    private Object[][] parse(Object[][] data) {
        //ids date group account number type status value
        double val = 0d;
        double val1 = 0d;
        Object[][] d = new Object[data.length][10];
        try {
            for (int i = 0; i < d.length; i++) {
                d[i][9] = data[i][9];
                d[i][3] = data[i][3];
                d[i][4] = data[i][4];
                d[i][2] = data[i][2];
                d[i][1] = DateConverter.getDate(data[i][1].toString());
                d[i][0] = Integer.valueOf(data[i][0].toString());

                int type = Integer.valueOf(data[i][5].toString());
                if (type == Revenue.TYPE_REVENUE) {
                    d[i][5] = Messages.TYPE_REVENUE.toString();
                } else if (type == Expense.TYPE_EXPENSE) {
                    d[i][5] = Messages.TYPE_EXPENSE.toString();
                } else {
                    d[i][5] = Item.getTypeString(type);
                }

                double brutto = 0;
                if (type == Revenue.TYPE_REVENUE || type == Expense.TYPE_EXPENSE) {
                    brutto = Double.valueOf(data[i][8].toString());
                } else {
                    brutto = Double.valueOf(data[i][7].toString()) + Double.valueOf(data[i][8].toString());
                }

                d[i][6] = Item.getStatusString(Integer.valueOf(data[i][6].toString()));


                if (Integer.valueOf(data[i][6].toString()).intValue() == Item.STATUS_PAID) {
                    val += brutto;
                    d[i][8] = "<html><p align=center>" + mpv5.utils.numberformat.FormatNumber.formatDezimal(brutto);
                } else if (Integer.valueOf(data[i][6].toString()).intValue() == 1000) {
                    d[i][8] = "<html><p align=center>" + mpv5.utils.numberformat.FormatNumber.formatDezimal(-1 * brutto);
                    val -= brutto;
                } else {
                    d[i][8] = "<html><p align=center>" + mpv5.utils.numberformat.FormatNumber.formatDezimal(0d);
                }
                d[i][7] = "<html><p align=center>" + mpv5.utils.numberformat.FormatNumber.formatDezimal(brutto);
                val1 += brutto;
            }
        } catch (Exception numberFormatException) {
            Log.Debug(numberFormatException);
        }

        volume.setText(mpv5.utils.numberformat.FormatNumber.formatDezimal(val1));
        revenue.setText(mpv5.utils.numberformat.FormatNumber.formatDezimal(val));

        return d;
    }

    private void refresh(final Group forGroup) {
        Runnable runnable = new Runnable() {

            public void run() {
                try {


                    QueryCriteria2 dh = new QueryCriteria2();
                    Object[][] d = new Object[0][0];
                    Object[][] d1 = new Object[0][0];
                    Object[][] d2 = new Object[0][0];

                    if (!includechildgroups.isSelected()) {
                        if (forGroup != null && !forGroup.__getCName().equals("")) {
                            dh.and(new QueryParameter(Context.getItem(), forGroup.getDbIdentity() + "ids", forGroup.__getIDS(), QueryParameter.EQUALS));
                        }
                    } else {
                        if (forGroup != null && !forGroup.__getCName().equals("")) {
                            List<Group> gs = forGroup.getChildGroups();
                            QueryParameter[] params = new QueryParameter[gs.size() + 1];
                            params[0] = (new QueryParameter(Context.getItem(), forGroup.getDbIdentity() + "ids", forGroup.__getIDS(), QueryParameter.EQUALS));
                            if (gs.size() >= 1) {
                                for (int i = 0; i < gs.size(); i++) {
                                    Group group = gs.get(i);
                                    params[i + 1] = (new QueryParameter(Context.getItem(), group.getDbIdentity() + "ids", group.__getIDS(), QueryParameter.EQUALS));
                                }
                                dh.or(params);
                            } else {
                                dh.and(new QueryParameter(Context.getItem(), forGroup.getDbIdentity() + "ids", forGroup.__getIDS(), QueryParameter.EQUALS));
                            }
                        }
                    }


                    List<QueryParameter> l = new Vector<QueryParameter>();
                    for (int i = 0; i < accountsp.getComponentCount(); i++) {
                        MPCombobox b = (MPCombobox) accountsp.getComponent(i);
                        MPComboBoxModelItem acc = b.getSelectedItem();

                        if (acc != null && acc.isValid() && Integer.valueOf(acc.getId()).intValue() > 0) {
                            l.add(new QueryParameter(Context.getItem(), "accountsids", Integer.valueOf(acc.getId()).intValue(), QueryParameter.EQUALS));
                        }
                    }
                    if (l.size() > 1) {
                        dh.or(l.toArray(new QueryParameter[0]));
                    } else {
                        dh.and(l.toArray(new QueryParameter[0]));
                    }

                    if (dataowner != null) {
                        dh.and(new QueryParameter(dataowner.getContext(), "ids", dataowner.__getIDS(), QueryParameter.EQUALS));
                    }

                    boolean additional = true;
                    if (statusc.getComboBox().getSelectedItem().equals(Messages.STATUS_PAID)) {
                        dh.and(new QueryParameter(Context.getItem(), "intstatus", Item.STATUS_PAID, QueryParameter.EQUALS));
                    } else if (statusc.getComboBox().getSelectedItem().equals(Messages.STATUS_UNPAID)) {
                        additional = false;
                    }

                    try {
                        Context c = Context.getItem();
                        dh.setOrder("accountsids", true);
                        c.addReference(Context.getGroup());
                        c.addReference(Context.getAccounts());
                        if (dataowner != null) {
                            c.addReference(Context.getContact());
                        }
                        try {
                            d = QueryHandler.instanceOf().clone(c).select(Context.DETAILS_JOURNAL, dh, timeframeChooser1.getTime(), "dateend").getData();
                        } catch (NodataFoundException nodataFoundException) {
                            d = new Object[0][10];
                        }
                        d = ArrayUtilities.inserValue(d, Context.getItem(), 9);
                        if (!additional) {
                            d = ArrayUtilities.removeRows(d, 6, Item.STATUS_PAID);
                        }
                    } catch (Exception ex) {
                        Log.Debug(ex);
                    }
                    if (dataowner == null && additional) {
                        try {
                            Context c = Context.getExpense();
                            dh.setOrder("dateadded", true);
                            c.addReference(Context.getGroup());
                            c.addReference(Context.getAccounts());
                            QueryCriteria2 dd = new QueryCriteria2();
                            List<QueryParameter> l1 = new Vector<QueryParameter>();
                            for (int i = 0; i < accountsp.getComponentCount(); i++) {
                                MPCombobox b = (MPCombobox) accountsp.getComponent(i);
                                MPComboBoxModelItem acc = b.getSelectedItem();
                                if (acc != null && acc.isValid() && Integer.valueOf(acc.getId()).intValue() > 0) {
                                    l1.add(new QueryParameter(c, "accountsids", Integer.valueOf(acc.getId()).intValue(), QueryParameter.EQUALS));
                                }
                            }
                            if (l1.size() > 1) {
                                dd.or(l1.toArray(new QueryParameter[0]));
                            } else {
                                dd.and(l1.toArray(new QueryParameter[0]));
                            }
                            if (!includechildgroups.isSelected()) {
                                if (forGroup != null && !forGroup.__getCName().equals("")) {
                                    dd.and(new QueryParameter(c, forGroup.getDbIdentity() + "ids", forGroup.__getIDS(), QueryParameter.EQUALS));
                                }
                            } else {
                                if (forGroup != null) {
                                    List<Group> gs = forGroup.getChildGroups();
                                    QueryParameter[] params = new QueryParameter[gs.size() + 1];
                                    params[0] = (new QueryParameter(c, forGroup.getDbIdentity() + "ids", forGroup.__getIDS(), QueryParameter.EQUALS));
                                    if (gs.size() >= 1) {
                                        for (int i = 0; i < gs.size(); i++) {
                                            Group group = gs.get(i);
                                            params[i + 1] = (new QueryParameter(c, group.getDbIdentity() + "ids", group.__getIDS(), QueryParameter.EQUALS));
                                        }
                                        dh.or(params);
                                    } else {
                                        dd.and(new QueryParameter(c, forGroup.getDbIdentity() + "ids", forGroup.__getIDS(), QueryParameter.EQUALS));
                                    }
                                }
                            }
                            try {
                                d1 = QueryHandler.instanceOf().clone(c).select(Context.DETAILS_JOURNAL3, dd, timeframeChooser1.getTime()).getData();
                            } catch (NodataFoundException nodataFoundException) {
                                d1 = new Object[0][10];
                            }
                            for (int i = 0; i < d1.length; i++) {
                                d1[i][5] = Expense.TYPE_EXPENSE;
                                d1[i][6] = 1000;
                            }
                            d1 = ArrayUtilities.inserValue(d1, Context.getExpense(), 9);
                        } catch (Exception ex) {
                            Log.Debug(this, ex);
                        }

                        try {
                            Context c = Context.getRevenue();
                            dh.setOrder("dateadded", true);
                            c.addReference(Context.getGroup());
                            c.addReference(Context.getAccounts());
                            QueryCriteria2 dd = new QueryCriteria2();
                            List<QueryParameter> l2 = new Vector<QueryParameter>();
                            for (int i = 0; i < accountsp.getComponentCount(); i++) {
                                MPCombobox b = (MPCombobox) accountsp.getComponent(i);
                                MPComboBoxModelItem acc = b.getSelectedItem();
                                if (acc != null && acc.isValid() && Integer.valueOf(acc.getId()).intValue() > 0) {
                                    l2.add(new QueryParameter(c, "accountsids", Integer.valueOf(acc.getId()).intValue(), QueryParameter.EQUALS));
                                }
                            }
                            if (l2.size() > 1) {
                                dd.or(l2.toArray(new QueryParameter[0]));
                            } else {
                                dd.and(l2.toArray(new QueryParameter[0]));
                            }
                            if (!includechildgroups.isSelected()) {
                                if (forGroup != null && !forGroup.__getCName().equals("")) {
                                    dd.and(new QueryParameter(c, forGroup.getDbIdentity() + "ids", forGroup.__getIDS(), QueryParameter.EQUALS));
                                }
                            } else {
                                if (forGroup != null) {
                                    List<Group> gs = forGroup.getChildGroups();
                                    QueryParameter[] params = new QueryParameter[gs.size() + 1];
                                    params[0] = (new QueryParameter(c, forGroup.getDbIdentity() + "ids", forGroup.__getIDS(), QueryParameter.EQUALS));
                                    if (gs.size() >= 1) {
                                        for (int i = 0; i < gs.size(); i++) {
                                            Group group = gs.get(i);
                                            params[i + 1] = (new QueryParameter(c, group.getDbIdentity() + "ids", group.__getIDS(), QueryParameter.EQUALS));
                                        }
                                        dd.or(params);
                                    } else {
                                        dd.and(new QueryParameter(c, forGroup.getDbIdentity() + "ids", forGroup.__getIDS(), QueryParameter.EQUALS));
                                    }
                                }
                            }
                            try {
                                d2 = QueryHandler.instanceOf().clone(c).select(Context.DETAILS_JOURNAL2, dd, timeframeChooser1.getTime()).getData();
                            } catch (NodataFoundException nodataFoundException) {
                                d2 = new Object[0][10];
                            }
                            for (int i = 0; i < d2.length; i++) {
                                d2[i][5] = Revenue.TYPE_REVENUE;
                                d2[i][6] = Item.STATUS_PAID;
                            }
                            d2 = ArrayUtilities.inserValue(d2, Context.getRevenue(), 9);
                        } catch (Exception ex) {
                            Log.Debug(this, ex);
                        }

                        d = ArrayUtilities.merge(ArrayUtilities.merge(d, d1), d2);
                    }
                    d = parse(d);
                    jTable1.setModel(new MPTableModel(d, Headers.JOURNAL.getValue(), new Class[]{Integer.class, Date.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class, Object.class}));
                    TableFormat.stripColumn(jTable1, 0);
                    TableFormat.stripColumn(jTable1, 9);
                } catch (Exception e) {
                    Log.Debug(this, e);
                }
            }
        };
        SwingUtilities.invokeLater(runnable);
    }

    @Override
    public void refresh() {
        refresh(null);
    }

    @Override
    public void flush() {
        jTable1.setModel(new DefaultTableModel());
    }

    private void setPopup() {
        TablePopUp tablePopUp = new TablePopUp(jTable1, new String[]{Messages.DELETE.toString(), Messages.CHANGE_STATUS.toString(), null, Messages.RELOAD.getValue()}, new ActionListener[]{new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    for (int i = 0; i < jTable1.getSelectedRows().length; i++) {
                        try {
                            DatabaseObject obj = DatabaseObject.getObject((Context) jTable1.getValueAt(jTable1.getSelectedRows()[i], 9), Integer.valueOf(jTable1.getValueAt(jTable1.getSelectedRows()[i], 0).toString()));
                            obj.delete();
                        } catch (NodataFoundException ex) {
                            Log.Debug(ex);
                        }
                    }
                    setData();
                }
            }, new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    for (int i = 0; i < jTable1.getSelectedRows().length; i++) {
                        try {
                            DatabaseObject obj = DatabaseObject.getObject((Context) jTable1.getValueAt(jTable1.getSelectedRows()[i], 9), Integer.valueOf(jTable1.getValueAt(jTable1.getSelectedRows()[i], 0).toString()));
                            if (obj.getContext().equals(Context.getItem())) {
                                Item item = (Item) obj;
                                if (item.__getIntstatus() == Item.STATUS_PAID) {
                                    item.setIntstatus(Item.STATUS_FINISHED);
                                    item.save();
                                } else {
                                    item.setIntstatus(Item.STATUS_PAID);
                                    item.save();
                                }
                            } else if (obj.getContext().equals(Context.getRevenue())) {
                            } else if (obj.getContext().equals(Context.getExpense())) {
                            }
                        } catch (NodataFoundException ex) {
                            Log.Debug(ex);
                        }
                    }
                    setData();
                }
            }, null, new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    setData();
                }
            }});
    }

    private void setData() {
        try {
            if (groups.getSelectedItem().isValid()) {
                refresh((Group) Group.getObject(Context.getGroup(), Integer.valueOf(groups.getSelectedItem().getId())));
            } else {
                refresh(null);
            }
        } catch (Exception ignore) {
            refresh(null);
        }
    }

    private void preview() {
        PreviewPanel pr;
//        if (dataowner != null && dataowner.isExisting()) {
        if (preloadedTemplate != null) {
            pr = new PreviewPanel();
            pr.setDataOwner(dataowner);
            preloadedTemplate.injectTable(TableHandler.KEY_TABLE + 1, jTable1.getModel());
            new Job(Export.createFile(preloadedTemplate, dataowner), pr).execute();
        } else {
            Popup.notice(Messages.NO_TEMPLATE_LOADED + " (" + mpv5.db.objects.User.getCurrentUser() + ")");
        }
//        }
    }

    private void loadTemplate() {
        Runnable runnable = new Runnable() {

            public void run() {
                if (dataowner == null) {
                    preloadedTemplate = TemplateHandler.loadTemplate(dataowner, TemplateHandler.TYPE_JOURNAL);
                    TemplateHandler.loadTemplateFor(jButton4, dataowner, TemplateHandler.TYPE_JOURNAL);
                } else {
                    preloadedTemplate = TemplateHandler.loadTemplate(dataowner, TemplateHandler.TYPE_CONTACT);
                    TemplateHandler.loadTemplateFor(jButton4, dataowner, TemplateHandler.TYPE_CONTACT);
                }
            }
        };
        new Thread(runnable).start();
    }

    private void dta() {
          if (jTable1.getSelectedRowCount() < 1) {
            Popup.notice(Messages.SELECT_AN_INVOICE);

        } else {
            List<Item> items = new Vector<Item>();
            for (int i = 0; i < jTable1.getSelectedRows().length; i++) {
                try {
                    DatabaseObject obj = DatabaseObject.getObject((Context) jTable1.getValueAt(jTable1.getSelectedRows()[i], 9), Integer.valueOf(jTable1.getValueAt(jTable1.getSelectedRows()[i], 0).toString()));
                    if (obj.getContext().equals(Context.getItem())) {
                        Item item = (Item) obj;
                        if (item.__getIntstatus() != Item.STATUS_PAID) {
                            items.add(item);
                        }
                    }
                } catch (NodataFoundException ex) {
                    Log.Debug(ex);
                }
            }

            HashMap<String, Object> map = new HashMap<String, Object>();
            for (int i = 0; i < items.size(); i++) {
                Item item = items.get(i);
                map.put(item.__getCnumber(), item);
            }

            DialogForFile d = new DialogForFile(DialogForFile.FILES_ONLY, "export.dta");
            DTAFile dta = new DTAFile(map);
            Job job = new Job(dta, d, d.getSelectedFile().getPath() + " " + Messages.SAVED);
            job.execute();

        }
    }

}
