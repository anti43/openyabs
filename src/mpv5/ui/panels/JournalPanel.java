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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComponent;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import mpv5.data.TrashHandler;
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
import mpv5.globals.Messages;
import mpv5.logging.Log;
import mpv5.ui.beans.LabeledCombobox;
import mpv5.ui.beans.MPCombobox;
import mpv5.ui.frames.MPView;
import mpv5.ui.popups.TablePopUp;
import mpv5.utils.arrays.ArrayUtilities;
import mpv5.utils.date.DateConverter;
import mpv5.utils.date.vTimeframe;
import mpv5.utils.models.MPComboBoxModelItem;
import mpv5.utils.models.MPTableModel;
import mpv5.utils.models.MPTreeModel;
import mpv5.utils.tables.ExcelAdapter;
import mpv5.utils.tables.TableFormat;
import mpv5.utils.trees.TreeFormat;

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
        Object[] dat2;

        try {
            dat2 = DatabaseObject.getObjects(Context.getGroup()).toArray();
            dat2 = ArrayUtilities.merge(new Object[]{new Group("")}, dat2);
            groups.setModel(new DefaultComboBoxModel(dat2));
        } catch (NodataFoundException ex) {
            Log.Debug(ex);
            groups.setModel(new DefaultComboBoxModel());
        }

        account1.setSearchOnEnterEnabled(true);
        account1.setContext(Context.getAccounts());

        statusc.getComboBox().setModel(new DefaultComboBoxModel(new Object[]{Messages.ALL, Messages.STATUS_PAID, Messages.STATUS_UNPAID}));

        prinitingComboBox1.init(jTable1);
        refresh(null);
    }

    public JournalPanel(Contact dataOwner) {
        initComponents();
        setPopup();
        jLabel4.setText(dataOwner.__getCName());
        timeframeChooser1.setTime(new vTimeframe(DateConverter.getDate(DateConverter.getYear()), new Date()));
        prinitingComboBox1.init(jTable1);
        Object[] dat;
        Object[] dat2;
        dataowner = dataOwner;
        try {
            dat2 = DatabaseObject.getObjects(Context.getGroup()).toArray();
            dat2 = ArrayUtilities.merge(new Object[]{new Group("")}, dat2);
            groups.setModel(new DefaultComboBoxModel(dat2));
        } catch (NodataFoundException ex) {
            Log.Debug(ex);
            groups.setModel(new DefaultComboBoxModel());
        }

        account1.setSearchOnEnterEnabled(true);
        account1.setContext(Context.getAccounts());

        statusc.getComboBox().setModel(new DefaultComboBoxModel(new Object[]{Messages.ALL, Messages.STATUS_PAID, Messages.STATUS_UNPAID}));

        refresh(null);

        jTree1.setCellRenderer(MPTreeModel.getRenderer());
        jTree1.setModel(new MPTreeModel(dataowner));
        jTree1.addMouseListener(MPTreeModel.getDefaultTreeListener(jTree1));
        prinitingComboBox1.init(jTable1);
//        TreeFormat.expandTree(jTree1);
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
        groups = new javax.swing.JComboBox();
        jButton1 = new javax.swing.JButton();
        jLabel6 = new javax.swing.JLabel();
        statusc = new mpv5.ui.beans.MPCombobox();
        accountsp = new javax.swing.JPanel();
        account1 = new mpv5.ui.beans.MPCombobox();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        volume = new mpv5.ui.beans.LabeledTextField();
        revenue = new mpv5.ui.beans.LabeledTextField();
        prinitingComboBox1 = new mpv5.ui.beans.PrinitingComboBox();

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

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        jTable1.setAutoCreateRowSorter(true);
        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        jTable1.setCellSelectionEnabled(true);
        jTable1.setName("jTable1"); // NOI18N
        jTable1.setSelectionMode(javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        jTable1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTable1MouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(jTable1);

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));
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

        timeframeChooser1.setBackground(new java.awt.Color(255, 255, 255));
        timeframeChooser1.setName("timeframeChooser1"); // NOI18N

        jLabel2.setFont(new java.awt.Font("Dialog", 0, 12));
        jLabel2.setText(bundle.getString("JournalPanel.jLabel2.text")); // NOI18N
        jLabel2.setName("jLabel2"); // NOI18N

        groups.setMaximumSize(new java.awt.Dimension(224, 20));
        groups.setName("groups"); // NOI18N
        groups.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                groupsActionPerformed(evt);
            }
        });

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

        accountsp.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("JournalPanel.accountsp.border.title"))); // NOI18N
        accountsp.setName("accountsp"); // NOI18N
        accountsp.setLayout(new javax.swing.BoxLayout(accountsp, javax.swing.BoxLayout.PAGE_AXIS));

        account1.setName("account1"); // NOI18N
        accountsp.add(account1);

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

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(accountsp, javax.swing.GroupLayout.PREFERRED_SIZE, 391, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, 137, Short.MAX_VALUE)
                            .addComponent(jLabel5))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(groups, javax.swing.GroupLayout.PREFERRED_SIZE, 237, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                .addGroup(jPanel2Layout.createSequentialGroup()
                                    .addComponent(timeframeChooser1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGap(257, 257, 257))
                                .addComponent(jLabel4, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 269, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(statusc, javax.swing.GroupLayout.PREFERRED_SIZE, 141, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(397, 397, 397)
                        .addComponent(jButton2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton3)
                        .addGap(18, 18, 18)
                        .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(298, 298, 298))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel4)
                    .addComponent(jLabel3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel5)
                    .addComponent(timeframeChooser1, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                            .addComponent(jLabel2)
                            .addComponent(groups, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                            .addComponent(jLabel6)
                            .addComponent(statusc, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(accountsp, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                        .addComponent(jButton1)
                        .addComponent(jButton3)
                        .addComponent(jButton2)))
                .addContainerGap())
        );

        volume.set_Label(bundle.getString("JournalPanel.volume._Label")); // NOI18N
        volume.setName("volume"); // NOI18N

        revenue.set_Label(bundle.getString("JournalPanel.revenue._Label")); // NOI18N
        revenue.setName("revenue"); // NOI18N

        prinitingComboBox1.setBackground(new java.awt.Color(255, 255, 255));
        prinitingComboBox1.setName("prinitingComboBox1"); // NOI18N

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(prinitingComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, 168, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 50, Short.MAX_VALUE)
                .addComponent(volume, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(revenue, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, 634, Short.MAX_VALUE)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 634, Short.MAX_VALUE)
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 70, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(volume, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(revenue, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(prinitingComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        jTabbedPane1.addTab(bundle.getString("JournalPanel.jPanel4.TabConstraints.tabTitle"), jPanel4); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 639, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 327, Short.MAX_VALUE)
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
        try {
            if (groups.getSelectedIndex() > 0) {
                refresh((Group) groups.getSelectedItem());
            } else {
                refresh(null);
            }
        } catch (Exception ignore) {
            refresh(null);
        }
    }//GEN-LAST:event_jButton1ActionPerformed

    private void groupsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_groupsActionPerformed
    }//GEN-LAST:event_groupsActionPerformed

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
        b.setSearchOnEnterEnabled(true);
        b.setContext(Context.getAccounts());
        accountsp.add(b);
        validate();
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        accountsp.remove(accountsp.getComponentCount() - 1);
        validate();
    }//GEN-LAST:event_jButton3ActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private mpv5.ui.beans.MPCombobox account1;
    private javax.swing.JPanel accountsp;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JComboBox groups;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
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
                d[i][1] = DateConverter.getDefDateString(DateConverter.getDate(data[i][1].toString()));
                d[i][0] = data[i][0];

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

                    if (forGroup != null && !forGroup.__getCName().equals("")) {
                        dh.and(new QueryParameter(forGroup.getContext(), forGroup.getDbIdentity() + "ids", forGroup.__getIDS(), QueryParameter.EQUALS));
                    }
                    List<QueryParameter> l = new Vector<QueryParameter>();
                    for (int i = 0; i < accountsp.getComponentCount(); i++) {
                        MPCombobox b = (MPCombobox) accountsp.getComponent(i);
                        MPComboBoxModelItem acc = b.getSelectedItem();

                        if (acc != null && acc.isValid() && Integer.valueOf(acc.getId()).intValue() >0) {
                            l.add(new QueryParameter(Context.getItems(), "accountsids", Integer.valueOf(acc.getId()).intValue(), QueryParameter.EQUALS));
                        }
                    }
                    if (l.size() > 1) {
                        dh.or(l.toArray(new QueryParameter[0]));
                    } else {
                        dh.and(l.toArray(new QueryParameter[0]));
                    }

                    if (dataowner != null) {
                        dh.and(new QueryParameter(dataowner.getContext(), dataowner.getDbIdentity() + "ids", dataowner.__getIDS(), QueryParameter.EQUALS));
                    }

                    boolean additional = true;
                    if (statusc.getComboBox().getSelectedItem().equals(Messages.STATUS_PAID)) {
                        dh.and(new QueryParameter(Context.getItems(),"intstatus", Item.STATUS_PAID, QueryParameter.EQUALS));
                    } else if (statusc.getComboBox().getSelectedItem().equals(Messages.STATUS_UNPAID)) {
                        additional = false;
                    }

                    try {
                        Context c = Context.getItems();
                        dh.setOrder("accountsids", true);
                        c.addReference(Context.getGroup());
                        c.addReference(Context.getAccounts());
                        try {
                            d = QueryHandler.instanceOf().clone(c).select(Context.DETAILS_JOURNAL, dh, timeframeChooser1.getTime()).getData();
                        } catch (NodataFoundException nodataFoundException) {
                            d = new Object[0][10];
                        }
                        d = ArrayUtilities.inserValue(d, Context.getItems(), 9);
                        if (!additional) {
                            d = ArrayUtilities.removeRows(d, 6, Item.STATUS_PAID);
                        }
                    } catch (Exception ex) {
                        Log.Debug(ex);
                    }
                    if (dataowner == null && additional) {
                        try {
                            Context c = Context.getExpenses();
                            dh.setOrder("dateadded", true);
                            c.addReference(Context.getGroup());
                            c.addReference(Context.getAccounts());
                            QueryCriteria2 dd = new QueryCriteria2();
                            List<QueryParameter> l1 = new Vector<QueryParameter>();
                            for (int i = 0; i < accountsp.getComponentCount(); i++) {
                                MPCombobox b = (MPCombobox) accountsp.getComponent(i);
                                MPComboBoxModelItem acc = b.getSelectedItem();
                                if (acc != null && acc.isValid() && Integer.valueOf(acc.getId()).intValue() > 0) {
                                    l1.add(new QueryParameter(c,"accountsids", Integer.valueOf(acc.getId()).intValue(), QueryParameter.EQUALS));
                                }
                            }
                            if (l1.size() > 1) {
                                dd.or(l1.toArray(new QueryParameter[0]));
                            } else {
                                dd.and(l1.toArray(new QueryParameter[0]));
                            }
                            if (forGroup != null && !forGroup.__getCName().equals("")) {
                                dd.and(new QueryParameter(forGroup.getContext(), forGroup.getDbIdentity() + "ids", forGroup.__getIDS(), QueryParameter.EQUALS));
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
                            d1 = ArrayUtilities.inserValue(d1, Context.getExpenses(), 9);
                        } catch (Exception ex) {
                            Log.Debug(this, ex);
                        }

                        try {
                            Context c = Context.getRevenues();
                            dh.setOrder("dateadded", true);
                            c.addReference(Context.getGroup());
                            c.addReference(Context.getAccounts());
                            QueryCriteria2 dd = new QueryCriteria2();
                            List<QueryParameter> l2 = new Vector<QueryParameter>();
                            for (int i = 0; i < accountsp.getComponentCount(); i++) {
                                MPCombobox b = (MPCombobox) accountsp.getComponent(i);
                                MPComboBoxModelItem acc = b.getSelectedItem();
                                if (acc != null && acc.isValid() && Integer.valueOf(acc.getId()).intValue() > 0) {
                                    l2.add(new QueryParameter(c,"accountsids", Integer.valueOf(acc.getId()).intValue(), QueryParameter.EQUALS));
                                }
                            }
                            if (l2.size() > 1) {
                                dd.or(l2.toArray(new QueryParameter[0]));
                            } else {
                                dd.and(l2.toArray(new QueryParameter[0]));
                            }
                            if (forGroup != null && !forGroup.__getCName().equals("")) {
                                dd.and(new QueryParameter(forGroup.getContext(), forGroup.getDbIdentity() + "ids", forGroup.__getIDS(), QueryParameter.EQUALS));
                            }
                            try {
                                d1 = QueryHandler.instanceOf().clone(c).select(Context.DETAILS_JOURNAL2, dd, timeframeChooser1.getTime()).getData();
                            } catch (NodataFoundException nodataFoundException) {
                                d1 = new Object[0][10];
                            }
                            for (int i = 0; i < d2.length; i++) {
                                d2[i][5] = Revenue.TYPE_REVENUE;
                                d2[i][6] = Item.STATUS_PAID;
                            }
                            d2 = ArrayUtilities.inserValue(d2, Context.getRevenues(), 9);
                        } catch (Exception ex) {
                            Log.Debug(this, ex);
                        }

                        d = ArrayUtilities.merge(ArrayUtilities.merge(d, d1), d2);
                    }
                    d = parse(d);
                    jTable1.setModel(new MPTableModel(d, Headers.JOURNAL.getValue(), new Class[]{Integer.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class, Object.class}));
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
                            if (obj.getContext().equals(Context.getItems())) {
                                Item item = (Item) obj;
                                if (item.__getIntstatus() == Item.STATUS_PAID) {
                                    item.setIntstatus(Item.STATUS_FINISHED);
                                    item.save();
                                } else {
                                    item.setIntstatus(Item.STATUS_PAID);
                                    item.save();
                                }
                            } else if (obj.getContext().equals(Context.getRevenues())) {
                            } else if (obj.getContext().equals(Context.getExpenses())) {
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
            if (groups.getSelectedIndex() > 0) {
                refresh((Group) groups.getSelectedItem());
            } else {
                refresh(null);
            }
        } catch (Exception ignore) {
            refresh(null);
        }
    }
}
