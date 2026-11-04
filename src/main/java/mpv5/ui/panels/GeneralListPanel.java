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
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.Vector;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.db.common.DatabaseSearch;
import mpv5.db.common.NodataFoundException;
import mpv5.db.common.QueryCriteria2;
import mpv5.db.objects.Group;
import mpv5.db.objects.Item;
import mpv5.db.objects.User;
import mpv5.globals.GlobalSettings;
import mpv5.globals.Messages;
import mpv5.logging.Log;
import mpv5.ui.frames.MPView;
import mpv5.ui.misc.MPTable;
import mpv5.utils.date.vTimeframe;
import mpv5.utils.models.MPTableModel;
import mpv5.utils.renderer.TableCellRendererForDatabaseObjects;
import mpv5.utils.tables.TableFormat;
import mpv5.ui.misc.TableViewPersistenceHandler;

/**
 *
 *
 */
public final class GeneralListPanel extends javax.swing.JPanel {

    private static final long serialVersionUID = 1L;
    private List odata;
    TableCellRendererForDatabaseObjects rend = new TableCellRendererForDatabaseObjects();
    private java.util.ResourceBundle bundle = mpv5.i18n.LanguageManager.getBundle();
    private DatabaseSearch d;
    private Context groupBy;
    private Random random = new Random(43);

    /**
     * Creates new form GeneralListPanel
     *
     * @param <T>
     * @param list
     */
    public <T extends DatabaseObject> GeneralListPanel(List<T> list) {
        this();
        setData(list);
    }

    /**
     *
     */
    public GeneralListPanel() {
        initComponents();
        setName("generallistpanel");
        rend.setDbColumn(1);

        listtable.setDefaultRenderer(String.class, rend);
        listtable.setDefaultRenderer(Object.class, rend);
        listtable.setDefaultRenderer(Date.class, rend);
        listtable.setDefaultRenderer(DatabaseObject.class, rend);
        listtable.setDefaultRenderer(Context.class, rend);
        listtable.setGridColor(Color.LIGHT_GRAY);
        //listtable.setDefaultRenderer(ImageIcon.class, rend);

        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, c.get(Calendar.YEAR) - 1);
        timeframeChooser1.setTime(new vTimeframe(c.getTime(), new Date()));

        labeledCombobox1.setSearchEnabled(true);
        labeledCombobox1.setContext(Context.getGroup());
        labeledCombobox1.triggerSearch();

        ((MPTable) listtable).setDefaultColumns(new Integer[]{50, 100, 50, 100, 100, 20}, new Boolean[]{true, true, true, true, true, true});
        ((MPTable) listtable).setPersistanceHandler(new TableViewPersistenceHandler((MPTable) listtable, this));
        TableFormat.hideHeader(listtable);
    }

    /**
     *
     * @param c
     * @throws NodataFoundException
     */
    public GeneralListPanel(Context c) throws NodataFoundException {
        this(DatabaseObject.getObjects(c));
    }

    /**
     * Show the data
     *
     * @param <T>
     * @param map
     */
    public <T extends DatabaseObject> void show(HashMap<Color, List<T>> map) {

        Iterator<Color> it = map.keySet().iterator();
        List<DatabaseObject> ndata = new Vector<DatabaseObject>();
        while (it.hasNext()) {
            Color c = it.next();
            List<T> data = map.get(c);
            for (int i = 0; i < data.size(); i++) {
                T databaseObject = data.get(i);
                databaseObject.defineColor(c);
                ndata.add(databaseObject);
            }
        }

        setData(ndata);
    }

    /**
     * Set the data of the list
     *
     * @param <T>
     * @param list
     */
    public <T extends DatabaseObject> void setData(List<T> list) {
        jLabel1.setText(list.size() + " " + Messages.ENTRIES);
        odata = list;
        List<Integer> groupingRows = new ArrayList<>();

        Object[][] model = null;
        if (groupBy != null) {
            List<Object[]> datalist = new ArrayList<>();
            Map<DatabaseObject, List<DatabaseObject>> sorted = new TreeMap<>();
            for (int i = 0; i < list.size(); i++) {
                DatabaseObject root = list.get(i);
                DatabaseObject key = root.getRelationObject(groupBy);
                if (key != null && !sorted.containsKey(key)) {
                    sorted.put(key, new ArrayList<DatabaseObject>());
                }
                if (key != null) {
                    sorted.get(key).add(root);
                } else {
                    Log.Debug(this, "Missing RelationObject(" + groupBy + ") for " + root);
                }
            }

            int overallindex = 0;

            for (Map.Entry<DatabaseObject, List<DatabaseObject>> en : sorted.entrySet()) {
                DatabaseObject key = en.getKey();
                List<DatabaseObject> val = en.getValue();

                Object[] data0 = new Object[6];
                datalist.add(data0);

                data0[0] = "<html><b>" + groupBy.getIdentityClass().getSimpleName() + "</b></html>";
                data0[1] = key;
                data0[2] = null;
                data0[3] = null;
                data0[4] = null;
                

//            data[i][4] = databaseObject.getColor();
                data0[5] = key.getIcon();
                //data[overallindex][6] = Item.getTypeString(key.getContext().getItemType());
                groupingRows.add(overallindex);
                overallindex++;

                final float hue = random.nextFloat();
                final float saturation = (random.nextInt(2000) + 1000) / 10000f;
                final float luminance = 0.8f;
                final Color color = Color.getHSBColor(hue, saturation, luminance);

                key.defineColor(color);

                Color lastColor = key.getColor();
                for (int i = 0; i < val.size(); i++) {
                    lastColor = Color.getHSBColor(hue, saturation / (i + 1), 1.0f);
                    DatabaseObject databaseObject = val.get(i);
                    databaseObject.defineColor(lastColor);

                    Object[] data1 = new Object[6];
                    datalist.add(data1);

                    data1[0] = i + 1;
                    data1[1] = databaseObject;
                    data1[2] = User.getUsername(databaseObject.__getIntaddedby());
                    data1[3] = databaseObject.__getDateadded();
                    try {
                        data1[4] = DatabaseObject.getObject(Context.getGroup(), databaseObject.__getGroupsids());
                    } catch (NodataFoundException ex) {
                        data1[4] = "N/A";
                    }

//            data[i][4] = databaseObject.getColor();
                    data1[5] = databaseObject.getIcon();
                    //data[overallindex][6] = Item.getTypeString(key.getContext().getItemType());

                    overallindex++;
                }
            }

            model = new Object[overallindex][6];
            for (int i = 0; i < datalist.size(); i++) {
                Object[] objects = datalist.get(i);
                model[i] = objects;
            }
        } else {
            Object[][] data = new Object[list.size()][6];
            for (int i = 0; i < list.size(); i++) {
                DatabaseObject databaseObject = list.get(i);
                data[i][0] = i + 1;
                data[i][1] = databaseObject;
                data[i][2] = User.getUsername(databaseObject.__getIntaddedby());
                data[i][3] = databaseObject.__getDateadded();
                try {
                    data[i][4] = DatabaseObject.getObject(Context.getGroup(), databaseObject.__getGroupsids());
                } catch (NodataFoundException ex) {
                    data[i][4] = "N/A";
                }

//            data[i][4] = databaseObject.getColor();
                data[i][5] = databaseObject.getIcon();
                //data[i][6] = Item.getTypeString(databaseObject.getContext().getItemType());
            }
            model = data;
        }

        MPTableModel m = new MPTableModel(model);
        m.setTypes(new Class[]{Object.class, DatabaseObject.class, String.class, Date.class, DatabaseObject.class, ImageIcon.class});
        rend.setGroupingRows(groupingRows);
        listtable.setModel(m);

//        TableFormat.hideHeader(listtable);
//        TableFormat.stripColumn(listtable, 4);
//        TableFormat.stripColumn(jTable1, 5);
    }

    /**
     * Filter by group
     *
     * @param g
     */
    @SuppressWarnings({"unchecked"})
    public void filterByGroup(Group g) {
        List fullData = odata;
        List<DatabaseObject> list = new Vector<DatabaseObject>();
        if (g.__getIDS().intValue() != 1) {
            for (int i = 0; i < odata.size(); i++) {
                DatabaseObject d = (DatabaseObject) odata.get(i);
                if (d.__getGroupsids() == g.__getIDS()) {
                    list.add(d);
                }
            }
        } else {
            list = odata;
        }
        setData(list);
        odata = fullData;
    }

    /**
     * Filter by group
     *
     * @param g
     */
    @SuppressWarnings("unchecked")
    public void filterByTimeframe(vTimeframe g) {
        List fullData = odata;
        List<DatabaseObject> list = new Vector<DatabaseObject>();
        for (int i = 0; i < odata.size(); i++) {
            DatabaseObject d = (DatabaseObject) odata.get(i);
            if (g.contains(d.__getDateadded())) {
                list.add(d);
            }
        }
        setData(list);
        odata = fullData;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        listtable = new  mpv5.ui.misc.MPTable(this) {
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
        jLabel1 = new javax.swing.JLabel();

        java.util.ResourceBundle bundle =  mpv5.i18n.LanguageManager.getBundle(); // NOI18N
        setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("GeneralListPanel.border.title"))); // NOI18N
        setName("Form"); // NOI18N

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        listtable.setAutoCreateRowSorter(true);
        listtable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {},
                {},
                {},
                {}
            },
            new String [] {

            }
        ));
        listtable.setName("listtable"); // NOI18N
        listtable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                listtableMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(listtable);

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

        jLabel1.setText(bundle.getString("GeneralListPanel.jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

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
            .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 451, Short.MAX_VALUE)
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
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 217, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE))
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

    private void listtableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_listtableMouseClicked
        if (evt.getClickCount() > 1) {
            try {
                //System.err.println(mpv5.YabsViewProxy.instance().getIdentifierView());
                mpv5.YabsViewProxy.instance().getIdentifierView().addTab(((DatabaseObject) listtable.getModel().getValueAt(listtable.getSelectedRow(), 1)));
            } catch (Exception e) {
                Log.Debug(e);
            }
        }
    }//GEN-LAST:event_listtableMouseClicked
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private mpv5.ui.beans.LabeledCombobox labeledCombobox1;
    private javax.swing.JTable listtable;
    private mpv5.ui.beans.TimeframeChooser timeframeChooser1;
    // End of variables declaration//GEN-END:variables

    void setData(Context context) {
        try {
            setData(DatabaseObject.getObjects(context));
        } catch (NodataFoundException ex) {
            Log.Debug(ex);
            setData(Collections.EMPTY_LIST);
        }
    }

    void setData(Context context, QueryCriteria2 q) {
        try {
            setData(DatabaseObject.getObjects(context, q));
        } catch (NodataFoundException ex) {
            Log.Debug(ex);
            setData(Collections.EMPTY_LIST);
        }
    }

    void setData(Context context, String needle) {
        d = new DatabaseSearch(context);
        setData(d.searchDataFor(new Context[]{Context.getSubItem()}, new Context[]{Context.getCustomer()}, needle));
    }

    /**
     * @return the groupBy
     */
    public Context getGroupBy() {
        return groupBy;
    }

    /**
     * @param groupBy the groupBy to set
     */
    public void setGroupBy(Context groupBy) {
        this.groupBy = groupBy;
    }

    public void setSortable(final boolean b) {
        TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(listtable.getModel()) {

            @Override
            public boolean isSortable(int column) {
                return b;
            }
        };
        listtable.setRowSorter(sorter);
    }

    public JTable getTable() {
        return listtable;
    }
}
