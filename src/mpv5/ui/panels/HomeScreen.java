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
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.db.common.NodataFoundException;
import mpv5.db.objects.Group;
import mpv5.db.objects.Item;
import mpv5.db.objects.Reminder;
import mpv5.db.objects.Schedule;
import mpv5.db.objects.SubItem;
import mpv5.db.objects.User;
import mpv5.globals.Messages;
import mpv5.handler.Scheduler;
import mpv5.logging.Log;
import mpv5.ui.dialogs.Popup;
import mpv5.ui.dialogs.ScheduleEvents;
import mpv5.ui.misc.MPTable;
import mpv5.ui.misc.TableViewPersistenceHandler;
import mpv5.ui.panels.calendar.ScheduleCalendar;
import mpv5.utils.date.DateConverter;
import mpv5.utils.date.vTimeframe;
import mpv5.utils.models.MPTableModel;
import mpv5.utils.renderer.TableCellRendererForDatabaseObjects;
import mpv5.utils.tables.TableFormat;

public class HomeScreen
        extends javax.swing.JPanel {

    private static final long serialVersionUID = 8043431014500289718L;
    private List<DatabaseObject> odata;
    private List<DatabaseObject> xdata;
    TableCellRendererForDatabaseObjects rend = new TableCellRendererForDatabaseObjects();
    private final ScheduleDayEventsPanel SDEPanel;
    private final ScheduleCalendar sc;
    private static HomeScreen icke;

    public static HomeScreen getInstance() {
        if (icke == null) {
            icke = new HomeScreen();
        } else {
            icke.refresh();
        }
        return icke;
    }

    /**
     * 
     */
    private HomeScreen() {
        initComponents();

        setName("homescreenpanel");

        labeledCombobox1.setContext(Context.getGroup());
        labeledCombobox2.setModel(Item.getItemEnum());
        labeledCombobox3.setContext(Context.getGroup());
        labeledCombobox4.setModel(Item.getItemEnum());

        SDEPanel = ScheduleDayEventsPanel.instanceOf();
        jSplitPane1.setBottomComponent(SDEPanel);
        sc = ScheduleCalendar.instanceOf();
        jSplitPane1.setTopComponent(sc);
        initTable(overdue);
        initTable(nextEvents);
    }

    public JSplitPane getjSplitPane1() {
        return jSplitPane1;
    }

    private void initTable(JTable table) {
        table.setDefaultRenderer(String.class,
                rend);
        table.setDefaultRenderer(Date.class,
                rend);
        table.setDefaultRenderer(DatabaseObject.class,
                rend);

        ((MPTable) table).setDefaultColumns(new Integer[]{100,
                    100,
                    100,
                    100,
                    100,
                    100},
                new Boolean[]{true,
                    true,
                    true,
                    true,
                    true,
                    true});
        ((MPTable) table).setPersistanceHandler(new TableViewPersistenceHandler(
               (MPTable) table,
                this ));
        TableFormat.hideHeader(table);
    }

    public JTable getNextEvents() {
        return nextEvents;
    }

    public JTable getOverdue() {
        return overdue;
    }

    public <T extends DatabaseObject> void show(HashMap<Color, List<T>> map, JTable table) {

        Iterator<Color> it = map.keySet().
                iterator();
        List<DatabaseObject> ndata = new ArrayList<DatabaseObject>();
        while (it.hasNext()) {
            Color c = it.next();
            List<T> data = map.get(c);
            for (int i = 0; i < data.size(); i++) {
                T databaseObject = data.get(i);
                databaseObject.defineColor(c);
                ndata.add(databaseObject);
            }
        }


        if (table.equals(overdue)) {
            setData(ndata, table, false);
            odata = ndata;
        } else {
            setData(ndata, table, true);
            xdata = ndata;
        }
        labeledCombobox1.setSearchEnabled(true);
        labeledCombobox1.setContext(Context.getGroup());
        labeledCombobox1.triggerSearch();
        timeframeChooser1.setTime(new vTimeframe(DateConverter.getStartOfMonth(new Date()), DateConverter.getEndOfMonth(new Date())));
        
        labeledCombobox3.setSearchEnabled(true);
        labeledCombobox3.setContext(Context.getGroup());
        labeledCombobox3.triggerSearch();
        timeframeChooser2.setTime(new vTimeframe(DateConverter.getStartOfMonth(new Date()), DateConverter.getEndOfMonth(new Date())));
    }

    public <T extends DatabaseObject> void setData(List<T> list, JTable table, Boolean isDBObj) {
        Object[][] data  = null;
        if (list != null) {
            data = new Object[list.size()][6];

            for (int i = 0; i < list.size(); i++) {
                if (isDBObj == false) {
                    DatabaseObject databaseObject = list.get(i);
                    data[i][0] = databaseObject;
                    data[i][1] = User.getUsername(databaseObject.__getIntaddedby());
                    data[i][2] = databaseObject.__getDateadded();
                    try {
                        data[i][3] = DatabaseObject.getObject(Context.getGroup(),
                                databaseObject.__getGroupsids());
                    } catch (NodataFoundException ex) {
                        data[i][3] = "N/A";
                    }
                    data[i][4] = Item.getTypeString(((Item) databaseObject).__getInttype());
                    data[i][5] = databaseObject.getIcon();
                } else {
                    Schedule sched = (Schedule) list.get(i);
                    data[i][0] = sched;
                    data[i][1] = User.getUsername(sched.__getIntaddedby());
                    data[i][2] = sched.__getNextdate();
                    data[i][3] = sched.__getStopdate();
                    // TODO JAN: auch für andere Itemtypes anpassen ...
                    data[i][4] = Item.getTypeString(0);
                    data[i][5] = sched.getIcon();
                }
            }
        } else {
            data = new Object[0][6];
        }
        
        MPTableModel m = new MPTableModel(data);
        m.setTypes(new Class[]{DatabaseObject.class,
                    String.class,
                    Date.class,
                    DatabaseObject.class,
                    String.class,
                    ImageIcon.class
                });
        table.setModel(m);
    }

    /**
     * Filter by group
     * @param g
     */
    @SuppressWarnings("unchecked")
    public void filterByTimeframe(vTimeframe g) {
        setData(odata, overdue, false);
        Object[][] data = ((MPTableModel) overdue.getModel()).getData();
        List<DatabaseObject> list = new ArrayList<DatabaseObject>();
        for (int i = 0; i < data.length; i++) {
            DatabaseObject d = (DatabaseObject) data[i][0];
            if (g.contains(d.__getDateadded())) {
                list.add(d);
            }
        }
        setData(list, overdue, false);
    }

    /**
     * Filter by group
     * @param g
     */
    @SuppressWarnings("unchecked")
    public void filterByTimeframe2(vTimeframe g) {
        HashMap<Color, List<Schedule>> map = Scheduler.getScheduledBills(g);
        Iterator<Color> it = map.keySet().
                iterator();
        while (it.hasNext()) {
            Color c = it.next();
            List<Schedule> data = map.get(c);
            if (!data.isEmpty()) {
                this.show(map, this.getNextEvents());
                break;
            }
        }
    }

    @SuppressWarnings("unchecked")
    public void filterByItemType(Integer Type) {
        setData(odata, overdue, false);
        if (Type >= 0) {
            Object[][] data = ((MPTableModel) overdue.getModel()).getData();
            List<DatabaseObject> list = new ArrayList<DatabaseObject>();
            for (int i = 0; i < data.length; i++) {
                Item d = (Item) data[i][0];
                if (d.__getInttype() == Type) {
                    list.add(d);
                }
            }
            setData(list, overdue, false);
        }
    }

    /**
     * Filter by group
     * @param g
     */
    @SuppressWarnings({"unchecked"})
    public void filterByGroup(Group g) {
        setData(odata, overdue, false);
        Object[][] data = ((MPTableModel) overdue.getModel()).getData();
        List<DatabaseObject> list = new ArrayList<DatabaseObject>();
        if (g.__getIDS().
                intValue() != 1) {
            for (int i = 0; i < data.length; i++) {
                DatabaseObject d = (DatabaseObject) data[i][0];
                if (d.__getGroupsids() == g.__getIDS()) {
                    list.add(d);
                }
            }
        } else {
            for (int i = 0; i < data.length; i++) {
                DatabaseObject d = (DatabaseObject) data[i][0];
                list.add(d);
            }
        }
        setData(list, overdue, false);
    }

    @SuppressWarnings("unchecked")
    public void filterByItemType2(Integer Type) {
        setData(xdata, nextEvents, false);
        if (Type >= 0) {
            Object[][] data = ((MPTableModel) nextEvents.getModel()).getData();
            List<DatabaseObject> list = new ArrayList<DatabaseObject>();
            for (int i = 0; i < data.length; i++) {
                Item d = (Item) data[i][0];
                if (d.__getInttype() == Type) {
                    list.add(d);
                }
            }
            setData(list, nextEvents, false);
        }
    }

    /**
     * Filter by group
     * @param g
     */
    @SuppressWarnings({"unchecked"})
    public void filterByGroup2(Group g) {
        setData(xdata, nextEvents, false);
        Object[][] data = ((MPTableModel) nextEvents.getModel()).getData();
        List<DatabaseObject> list = new ArrayList<DatabaseObject>();
        if (g.__getIDS().
                intValue() != 1) {
            for (int i = 0; i < data.length; i++) {
                DatabaseObject d = (DatabaseObject) data[i][0];
                if (d.__getGroupsids() == g.__getIDS()) {
                    list.add(d);
                }
            }
        } else {
            for (int i = 0; i < data.length; i++) {
                DatabaseObject d = (DatabaseObject) data[i][0];
                list.add(d);
            }
        }
        setData(list, nextEvents, false);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        timeframeChooser1 = new mpv5.ui.beans.TimeframeChooser();
        labeledCombobox1 = new mpv5.ui.beans.LabeledCombobox();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        labeledCombobox2 = new mpv5.ui.beans.LabeledCombobox();
        jButton3 = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        overdue = new  mpv5.ui.misc.MPTable(this) {
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
        jScrollPane1 = new javax.swing.JScrollPane();
        nextEvents = new  mpv5.ui.misc.MPTable(this) {
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
        jSplitPane1 = new javax.swing.JSplitPane();
        labeledCombobox3 = new mpv5.ui.beans.LabeledCombobox();
        timeframeChooser2 = new mpv5.ui.beans.TimeframeChooser();
        jButton4 = new javax.swing.JButton();
        labeledCombobox4 = new mpv5.ui.beans.LabeledCombobox();
        jButton5 = new javax.swing.JButton();
        jButton6 = new javax.swing.JButton();
        but4 = new javax.swing.JButton();
        but5 = new javax.swing.JButton();

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("mpv5/resources/languages/Panels"); // NOI18N
        setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("HomeScreen.border.title"))); // NOI18N
        setName("Form"); // NOI18N

        timeframeChooser1.setName("timeframeChooser1"); // NOI18N

        labeledCombobox1.set_Label(bundle.getString("HomeScreen.labeledCombobox1._Label")); // NOI18N
        labeledCombobox1.setName("labeledCombobox1"); // NOI18N

        jButton1.setText(bundle.getString("HomeScreen.jButton1.text")); // NOI18N
        jButton1.setName("jButton1"); // NOI18N
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton2.setText(bundle.getString("HomeScreen.jButton2.text")); // NOI18N
        jButton2.setName("jButton2"); // NOI18N
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        labeledCombobox2.set_Label(bundle.getString("HomeScreen.labeledCombobox2._Label")); // NOI18N
        labeledCombobox2.setName("labeledCombobox2"); // NOI18N

        jButton3.setText(bundle.getString("HomeScreen.jButton3.text")); // NOI18N
        jButton3.setName("jButton3"); // NOI18N
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        jScrollPane2.setName("jScrollPane2"); // NOI18N

        overdue.setAutoCreateRowSorter(true);
        overdue.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {},
                {},
                {},
                {}
            },
            new String [] {

            }
        ));
        overdue.setName("overdue"); // NOI18N
        overdue.setShowVerticalLines(false);
        overdue.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                overdueMouseClicked(evt);
            }
        });
        jScrollPane2.setViewportView(overdue);

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        nextEvents.setAutoCreateRowSorter(true);
        nextEvents.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {},
                {},
                {},
                {}
            },
            new String [] {

            }
        ));
        nextEvents.setName("nextEvents"); // NOI18N
        nextEvents.setShowVerticalLines(false);
        nextEvents.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                nextEventsMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(nextEvents);

        jSplitPane1.setDividerLocation(200);
        jSplitPane1.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        jSplitPane1.setName("jSplitPane1"); // NOI18N

        labeledCombobox3.set_Label(bundle.getString("HomeScreen.labeledCombobox3._Label")); // NOI18N
        labeledCombobox3.setName("labeledCombobox3"); // NOI18N

        timeframeChooser2.setName("timeframeChooser2"); // NOI18N

        jButton4.setText(bundle.getString("HomeScreen.jButton4.text")); // NOI18N
        jButton4.setName("jButton4"); // NOI18N
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        labeledCombobox4.set_Label(bundle.getString("HomeScreen.labeledCombobox4._Label")); // NOI18N
        labeledCombobox4.setName("labeledCombobox4"); // NOI18N

        jButton5.setText(bundle.getString("HomeScreen.jButton5.text")); // NOI18N
        jButton5.setName("jButton5"); // NOI18N
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton5ActionPerformed(evt);
            }
        });

        jButton6.setText(bundle.getString("HomeScreen.jButton6.text")); // NOI18N
        jButton6.setName("jButton6"); // NOI18N
        jButton6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton6ActionPerformed(evt);
            }
        });

        but4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/mpv5/resources/images/32/view_right.png"))); // NOI18N
        but4.setText(bundle.getString("HomeScreen.but4.text")); // NOI18N
        but4.setToolTipText(bundle.getString("HomeScreen.but4.toolTipText")); // NOI18N
        but4.setContentAreaFilled(false);
        but4.setFocusable(false);
        but4.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        but4.setName("but4"); // NOI18N
        but4.setPressedIcon(new javax.swing.ImageIcon(getClass().getResource("/mpv5/resources/images/32/xclock.png"))); // NOI18N
        but4.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/mpv5/resources/images/32/view_right.png"))); // NOI18N
        but4.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        but4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                but4ActionPerformed(evt);
            }
        });

        but5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/mpv5/resources/images/32/edittrash.png"))); // NOI18N
        but5.setText(bundle.getString("HomeScreen.but5.text")); // NOI18N
        but5.setToolTipText(bundle.getString("HomeScreen.but5.toolTipText")); // NOI18N
        but5.setContentAreaFilled(false);
        but5.setFocusable(false);
        but5.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        but5.setName("but5"); // NOI18N
        but5.setPressedIcon(new javax.swing.ImageIcon(getClass().getResource("/mpv5/resources/images/32/xclock.png"))); // NOI18N
        but5.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/mpv5/resources/images/32/bright_edittrash.png"))); // NOI18N
        but5.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        but5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                but5ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(1, 1, 1)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(timeframeChooser1, javax.swing.GroupLayout.PREFERRED_SIZE, 330, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(6, 6, 6)
                                .addComponent(jButton1))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(labeledCombobox2, javax.swing.GroupLayout.PREFERRED_SIZE, 325, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(11, 11, 11)
                                .addComponent(jButton3)
                                .addGap(6, 6, 6)
                                .addComponent(labeledCombobox1, javax.swing.GroupLayout.PREFERRED_SIZE, 322, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(25, 25, 25)
                                .addComponent(jButton2))
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 831, Short.MAX_VALUE))
                        .addGap(6, 6, 6))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(timeframeChooser2, javax.swing.GroupLayout.PREFERRED_SIZE, 330, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(6, 6, 6)
                        .addComponent(jButton6))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(labeledCombobox4, javax.swing.GroupLayout.PREFERRED_SIZE, 325, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(11, 11, 11)
                        .addComponent(jButton4)
                        .addGap(6, 6, 6)
                        .addComponent(labeledCombobox3, javax.swing.GroupLayout.PREFERRED_SIZE, 322, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(25, 25, 25)
                        .addComponent(jButton5))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 838, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(but4, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 760, Short.MAX_VALUE)
                        .addComponent(but5, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSplitPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 252, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(11, 11, 11)
                .addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 485, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(timeframeChooser1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton1))
                .addGap(9, 9, 9)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(labeledCombobox2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton3)
                    .addComponent(labeledCombobox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(1, 1, 1)
                        .addComponent(jButton2)))
                .addGap(14, 14, 14)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 156, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(timeframeChooser2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton6))
                .addGap(9, 9, 9)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(labeledCombobox4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton4)
                    .addComponent(labeledCombobox3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(1, 1, 1)
                        .addComponent(jButton5)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 136, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(but4, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(but5, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        filterByTimeframe(timeframeChooser1.getTime());
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        try {
            filterByGroup((Group) Group.getObject(Context.getGroup(),
                    Integer.valueOf(labeledCombobox1.getSelectedItem().
                    getId())));
        } catch (Exception ex) {
            Log.Debug(ex);
        }
    }//GEN-LAST:event_jButton2ActionPerformed

    private void nextEventsMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_nextEventsMouseClicked
        if (evt.getClickCount() > 1) {
            try {
                System.err.println(mpv5.YabsViewProxy.instance().
                        getIdentifierView());
                createBill();
            } catch (Exception e) {
                Log.Debug(e);
            }
        }
    }//GEN-LAST:event_nextEventsMouseClicked

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        filterByItemType(Integer.valueOf(labeledCombobox2.getSelectedItem().
                getId()));
    }//GEN-LAST:event_jButton3ActionPerformed

    private void overdueMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_overdueMouseClicked
        Reminder rem;
        DatabaseObject dbo;
        Item itm;
        if (evt.getClickCount() > 1) {
            try {
                System.err.println(mpv5.YabsViewProxy.instance().
                        getIdentifierView());
                dbo = ((DatabaseObject) overdue.getModel().
                        getValueAt(overdue.convertRowIndexToModel(overdue.getSelectedRow()),
                        0));
                if (dbo.getColor().equals(Color.RED)
                        && (dbo instanceof Item)) {
                    itm = (Item) dbo;
                    if (itm.__getInttype() == Item.TYPE_BILL) {
                        if (Popup.Y_N_dialog(Messages.CREATE_REMINDER.toString())) {
                            rem = new Reminder();
                            rem.setCName(Messages.REMINDER.toString() + "@" + dbo.__getCName());
                            rem.setDateadded(new Date());
                            rem.setDescription("");
                            rem.setGroupsids(dbo.__getGroupsids());
                            rem.setIntaddedby(User.getCurrentUser().getID());
                            rem.setItemsids(dbo.__getIDS());
                            rem.save();
                        }
                    }
                }
                mpv5.YabsViewProxy.instance().
                        getIdentifierView().
                        addTab(dbo);
            } catch (Exception e) {
                Log.Debug(e);
            }
        }
    }//GEN-LAST:event_overdueMouseClicked

private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
    filterByItemType2(Integer.valueOf(labeledCombobox4.getSelectedItem().
            getId()));
}//GEN-LAST:event_jButton4ActionPerformed

private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
    try {
        filterByGroup2((Group) Group.getObject(Context.getGroup(),
                Integer.valueOf(labeledCombobox3.getSelectedItem().
                getId())));
    } catch (Exception ex) {
        Log.Debug(ex);
    }
}//GEN-LAST:event_jButton5ActionPerformed

private void jButton6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton6ActionPerformed
    filterByTimeframe2(timeframeChooser2.getTime());
}//GEN-LAST:event_jButton6ActionPerformed

    private void but4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_but4ActionPerformed
        Log.Debug(this, "adding new schedule from Homescreen");
        ScheduleEvents.instanceOf();
        
    }//GEN-LAST:event_but4ActionPerformed

    private void but5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_but5ActionPerformed
        Schedule d = (Schedule) nextEvents.getModel().getValueAt(nextEvents.convertRowIndexToModel(nextEvents.getSelectedRow()), 0);
        Log.Debug(this, "deleting the marked entry from Homescreen (and database :-) )");
        d.delete();
        refresh();
    }//GEN-LAST:event_but5ActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton but4;
    private javax.swing.JButton but5;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSplitPane jSplitPane1;
    private mpv5.ui.beans.LabeledCombobox labeledCombobox1;
    private mpv5.ui.beans.LabeledCombobox labeledCombobox2;
    private mpv5.ui.beans.LabeledCombobox labeledCombobox3;
    private mpv5.ui.beans.LabeledCombobox labeledCombobox4;
    private javax.swing.JTable nextEvents;
    private javax.swing.JTable overdue;
    private mpv5.ui.beans.TimeframeChooser timeframeChooser1;
    private mpv5.ui.beans.TimeframeChooser timeframeChooser2;
    // End of variables declaration//GEN-END:variables

    private void createBill() {
        if (Popup.Y_N_dialog(Messages.SCHEDULE_NEW_ITEMS)) {
            MPTableModel m = (MPTableModel) nextEvents.getModel();
            Schedule schedule = (Schedule) m.getValueAt(nextEvents.convertRowIndexToModel(nextEvents.getSelectedRow()),
                    0);
            Log.Debug(this, !schedule.__getIsdone());
            if (!schedule.__getIsdone()) {
                Log.Debug(this, "Objekt muss generiert werden ...");
                try {
                    Item item = schedule.getItem();
                    SubItem[] subs = item.getSubitems();
                    item.setIDS(-1);
                    item.setDateadded(new Date());
                    try {
                        item.setDatetodo(DateConverter.addDays(new Date(), Integer.valueOf(mpv5.db.objects.User.getCurrentUser().getProperties().getProperty("bills.warn.days"))));
                        item.setDateend(DateConverter.addDays(new Date(), Integer.valueOf(mpv5.db.objects.User.getCurrentUser().getProperties().getProperty("bills.alert.days"))));
                    } catch (Exception e) {
                        item.setDatetodo(DateConverter.addDays(new Date(), 14));
                        item.setDateend(DateConverter.addDays(new Date(), 30));
                    }
                    item.setIntreminders(0);
                    item.setIntstatus(Item.STATUS_IN_PROGRESS);
                    item.setDescription(item.__getDescription()
                            + "\n"
                            + Messages.SCHEDULE_GENERATED
                            + "\n"
                            + item.__getCName());
                    Log.Debug(this, "Objekt wird gesichert ...");
                    item.save();
                    for (int j = 0; j < subs.length; j++) {
                        SubItem subItem = subs[j];
                        subItem.setItemsids(item.__getIDS());
                        subItem.setIDS(-1);
                        subItem.save(true);
                    }
                    Log.Debug(this, "Posten wurden gesichert ...");
                    Date olddate = schedule.__getNextdate();
                    schedule.setNextdate(DateConverter.addMonths(schedule.__getNextdate(), schedule.__getIntervalmonth()));
                    if (schedule.__getNextdate().before(schedule.__getStopdate())) {
                        Schedule s2 = (Schedule) schedule.clone();
                        s2.setIsdone(false);
                        s2.setIDS(-1);
                        s2.save(true);
                        schedule.setNextdate(olddate);
                        schedule.setIsdone(true);
                        schedule.save(true);
                    } else {
                        schedule.setNextdate(olddate);
                        schedule.setIsdone(true);
                        schedule.save(true);
                        mpv5.YabsViewProxy.instance().addMessage(Messages.SCHEDULE_ITEM_REMOVED + " " + schedule);
                    }
                    Log.Debug(this, "Schedule wurde gesichert ...");
                    mpv5.YabsViewProxy.instance().
                            getIdentifierView().
                            addTab(item);
                    m.removeRow(nextEvents.convertRowIndexToModel(nextEvents.getSelectedRow()));
                } catch (NodataFoundException ex) {
                    Log.Debug(ex);
                }
            }
        }
    }

    public void refresh() {
        // überfällige Objekte zurücksetzen ...
        timeframeChooser1.setTime(new vTimeframe(DateConverter.getStartOfMonth(new Date()), DateConverter.getEndOfMonth(new Date())));
        labeledCombobox1.getComboBox().setSelectedIndex(-1);
        labeledCombobox2.getComboBox().setSelectedIndex(-1);
        setData(odata, overdue, false);
        // Zyklische Objekte zurücksetzen ...
        timeframeChooser2.setTime(new vTimeframe(DateConverter.getStartOfMonth(new Date()), DateConverter.getEndOfMonth(new Date())));
        labeledCombobox3.getComboBox().setSelectedIndex(-1);
        labeledCombobox4.getComboBox().setSelectedIndex(-1);
        setData(xdata, nextEvents, true);
        // Navigation rechts aktualisieren ...
        sc.setDate(new Date());
        SDEPanel.setDayEvents(null);
    }
}
