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

import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.db.common.NodataFoundException;
import mpv5.db.objects.Group;
import mpv5.db.objects.User;
import mpv5.logging.Log;
import mpv5.ui.frames.MPView;
import mpv5.utils.date.vTimeframe;
import mpv5.utils.images.MPIcon;
import mpv5.utils.models.MPTableModel;
import mpv5.utils.tables.TableFormat;

/**
 *
 *  
 */
public class GeneralListPanel extends javax.swing.JPanel {

    private static final long serialVersionUID = 1L;
    List odata;

    /** Creates new form GeneralListPanel
     * @param <T>
     * @param list
     */
    public <T extends DatabaseObject> GeneralListPanel(List<T> list) {
        initComponents();
        jTable1.setDefaultRenderer(String.class, new ccr());
        jTable1.setDefaultRenderer(Object.class, new ccr());
        jTable1.setDefaultRenderer(DatabaseObject.class, new ccr());
        labeledCombobox1.setSearchOnEnterEnabled(true);
        labeledCombobox1.setContext(Context.getGroup());
        labeledCombobox1.triggerSearch();
        setData(list);
        odata = list;

    }

    /**
     * 
     */
    public GeneralListPanel() {
        initComponents();
    }

    /**
     * Show the data
     * @param <T>
     * @param map
     */
    public <T extends DatabaseObject> void show(HashMap<Color, List<T>> map) {
        jTable1.setDefaultRenderer(String.class, new ccr());
        jTable1.setDefaultRenderer(Object.class, new ccr());
        jTable1.setDefaultRenderer(DatabaseObject.class, new ccr());
        Iterator<Color> it = map.keySet().iterator();
        List<coloredObject> ndata = new Vector<coloredObject>();
        while (it.hasNext()) {
            Color c = it.next();
            List<T> data = map.get(c);
            for (int i = 0; i < data.size(); i++) {
                T databaseObject = data.get(i);
                ndata.add(new coloredObject(c, databaseObject));
            }
        }

        setData(ndata);
        odata = ndata;
        labeledCombobox1.setSearchOnEnterEnabled(true);
        labeledCombobox1.setContext(Context.getGroup());
        labeledCombobox1.triggerSearch();
    }

    class coloredObject extends DatabaseObject {

        private Color color;
        private DatabaseObject object;

        public coloredObject(Color color, DatabaseObject object) {
            this.object = object;
            this.color = color;
            ArrayList<Object[]> data = object.getValues2();
            for (int i = 0; i < data.size(); i++) {
                Object[] objects = data.get(i);
                try {
                    super.parse(objects[0].toString(), objects[1]);
                } catch (Exception ex) {
                    Log.Debug(ex);
                }
            }
        }

        /**
         * @return the color
         */
        public Color getColor() {
            return color;
        }

        /**
         * @return the object
         */
        public DatabaseObject getObject() {
            return object;
        }

        @Override
        public JComponent getView() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public MPIcon getIcon() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }

    /**
     * Set the data of the list
     * @param <T>
     * @param list
     */
    public <T extends DatabaseObject> void setData(List<T> list) {

        Object[][] data = new Object[list.size()][5];

        for (int i = 0; i < list.size(); i++) {
            coloredObject databaseObject = null;
            Color c = null;
            if (list.get(i) instanceof coloredObject) {
                databaseObject = ((coloredObject) list.get(i));
                c = ((coloredObject) list.get(i)).getColor();
            } else {
                databaseObject = new coloredObject(Color.white, (DatabaseObject) list.get(i));
            }
            if (databaseObject != null) {

                data[i][0] = databaseObject;
                data[i][1] = User.getUsername(databaseObject.__getIntaddedby());
                data[i][2] = databaseObject.__getDateadded();
                try {
                    data[i][3] = DatabaseObject.getObject(Context.getGroup(), databaseObject.__getGroupsids());
                } catch (NodataFoundException ex) {
                    data[i][3] = "N/A";
                }

                data[i][4] = c;
            }
        }

        jTable1.setModel(new MPTableModel(data));
        TableFormat.stripColumn(jTable1, 4);
    }

    class ccr extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            cell.setBackground((Color) table.getValueAt(row, 4));
            return cell;
        }
    }

    /**
     * Filter by group
     * @param g
     */
    @SuppressWarnings({"unchecked"})
    public void filterByGroup(Group g) {
        setData(odata);
        Object[][] data = ((MPTableModel) jTable1.getModel()).getData();
        List<coloredObject> list = new Vector<coloredObject>();
        for (int i = 0; i < data.length; i++) {
            coloredObject d = (coloredObject) data[i][0];
            if (d.__getGroupsids() == g.__getIDS()) {
                list.add(new coloredObject(((coloredObject) d).getColor(), d));
            }
        }
        setData(list);
    }

    /**
     * Filter by group
     * @param g
     */
    @SuppressWarnings("unchecked")
    public void filterByTimeframe(vTimeframe g) {
        setData(odata);
        Object[][] data = ((MPTableModel) jTable1.getModel()).getData();
        List<coloredObject> list = new Vector<coloredObject>();
        for (int i = 0; i < data.length; i++) {
            coloredObject d = (coloredObject) data[i][0];
            if (g.contains(d.__getDateadded())) {
                list.add(new coloredObject(d.getColor(), d));
            }
        }
        setData(list);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new JTable() {
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
        timeframeChooser1 = new mpv5.ui.beans.TimeframeChooser();
        labeledCombobox1 = new mpv5.ui.beans.LabeledCombobox();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();

        java.util.ResourceBundle bundle = mpv5.i18n.LanguageManager.getBundle();
        setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("GeneralListPanel.border.title"))); // NOI18N
        setName("Form"); // NOI18N

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
        jTable1.setName("jTable1"); // NOI18N
        jTable1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTable1MouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(jTable1);

        timeframeChooser1.setName("timeframeChooser1"); // NOI18N

        labeledCombobox1.set_Label(bundle.getString("GeneralListPanel.labeledCombobox1._Label")); // NOI18N
        labeledCombobox1.setName("labeledCombobox1"); // NOI18N

        jButton1.setText(bundle.getString("GeneralListPanel.jButton1.text")); // NOI18N
        jButton1.setName("jButton1"); // NOI18N
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton2.setText(bundle.getString("GeneralListPanel.jButton2.text")); // NOI18N
        jButton2.setName("jButton2"); // NOI18N
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(labeledCombobox1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(timeframeChooser1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButton1)
                    .addComponent(jButton2))
                .addGap(33, 33, 33))
            .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 451, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jButton1, 0, 0, Short.MAX_VALUE)
                        .addGap(1, 1, 1))
                    .addComponent(timeframeChooser1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(labeledCombobox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 238, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed

        filterByTimeframe(timeframeChooser1.getTime());
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        try {
            filterByGroup((Group) Group.getObject(Context.getGroup(), Integer.valueOf(labeledCombobox1.getSelectedItem().getId())));
        } catch (Exception ex) {
//            Log.Debug(ex);
        }
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jTable1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTable1MouseClicked
        if (evt.getClickCount() > 1) {
            try {
                MPView.identifierView.addTab(((coloredObject) jTable1.getModel().getValueAt(jTable1.getSelectedRow(), 0)).getObject());
            } catch (Exception e) {
                Log.Debug(e);
            }
        }
    }//GEN-LAST:event_jTable1MouseClicked
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    private mpv5.ui.beans.LabeledCombobox labeledCombobox1;
    private mpv5.ui.beans.TimeframeChooser timeframeChooser1;
    // End of variables declaration//GEN-END:variables
}
