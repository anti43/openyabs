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
import java.math.BigDecimal;
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
import mpv5.db.objects.Revenue;
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
import mpv5.utils.numberformat.FormatNumber;
import mpv5.utils.renderer.TableCellRendererForDatabaseObjects;
import mpv5.utils.tables.TableFormat;

public class HomeScreen extends javax.swing.JPanel {

    private static final long serialVersionUID = 8043431014500289718L;
    private List<DatabaseObject> odata;
    private List<DatabaseObject> xdata;
    TableCellRendererForDatabaseObjects rend = new TableCellRendererForDatabaseObjects();
    private final ScheduleDayEventsPanel SDEPanel;
    private final ScheduleCalendar sc;
    private static HomeScreen icke;
    private BigDecimal billsSum = new BigDecimal(0);
    private BigDecimal orderSum = new BigDecimal(0);
    private BigDecimal offerSum = new BigDecimal(0);

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

        timeframeChooser1.setTime(new vTimeframe(DateConverter.getStartOfMonth(new Date()), DateConverter.getEndOfMonth(new Date())));
        timeframeChooser2.setTime(new vTimeframe(DateConverter.getStartOfMonth(new Date()), DateConverter.getEndOfMonth(new Date())));

        SDEPanel = ScheduleDayEventsPanel.instanceOf();
        jSplitPane1.setBottomComponent(SDEPanel);
        sc = ScheduleCalendar.instanceOf();
        sc.setDate(new Date());
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
                    15,
                    15},
                new Boolean[]{true,
                    true,
                    true,
                    true,
                    true,
                    true,
                    true});
        ((MPTable) table).setPersistanceHandler(new TableViewPersistenceHandler(
                (MPTable) table,
                this));
        TableFormat.hideHeader(table);
    }

    public JTable getNextEvents() {
        return nextEvents;
    }

    public JTable getOverdue() {
        return overdue;
    }

    public <T extends DatabaseObject> void show(HashMap<Color, List<? extends DatabaseObject>> map, JTable table) {

        Iterator<Color> it = map.keySet().
                iterator();
        List<DatabaseObject> ndata = new ArrayList<DatabaseObject>();
        while (it.hasNext()) {
            Color c = it.next();
            List<? extends DatabaseObject> data = map.get(c);
            for (int i = 0; i < data.size(); i++) {
                DatabaseObject databaseObject = data.get(i);
                databaseObject.defineColor(c);
                ndata.add(databaseObject);
            }
        }

        labeledCombobox1.setSearchEnabled(true);
        labeledCombobox1.setContext(Context.getGroup());
        labeledCombobox1.triggerSearch();
//        timeframeChooser1.setTime(new vTimeframe(DateConverter.getStartOfMonth(new Date()), DateConverter.getEndOfMonth(new Date())));

        labeledCombobox3.setSearchEnabled(true);
        labeledCombobox3.setContext(Context.getGroup());
        labeledCombobox3.triggerSearch();
//        timeframeChooser2.setTime(new vTimeframe(DateConverter.getStartOfMonth(new Date()), DateConverter.getEndOfMonth(new Date())));

        if (table.equals(overdue)) {
            setData(ndata, table);
            odata = ndata;
        } else {
            setData(ndata, table);
            xdata = ndata;
        }
        validate();
    }

    public <T extends DatabaseObject> void setData(List<T> list, JTable table) {
        Object[][] data;
        int type;
        orderSum = new BigDecimal(0);
        offerSum = new BigDecimal(0);
        billsSum = new BigDecimal(0);
        BigDecimal tax;
        if (list != null) {
            data = new Object[list.size()][7];

            for (int i = 0; i < list.size(); i++) {
                DatabaseObject databaseObject = list.get(i);
                if (databaseObject instanceof Item) {
                    data[i][0] = databaseObject;
                    data[i][1] = User.getUsername(databaseObject.__getIntaddedby());
                    data[i][2] = databaseObject.__getDateadded();
                    try {
                        data[i][3] = DatabaseObject.getObject(Context.getGroup(),
                                databaseObject.__getGroupsids());
                    } catch (NodataFoundException ex) {
                        data[i][3] = "N/A";
                    }
                    type = ((Item) databaseObject).__getInttype();
                    //tax = Constants.BD100.add(((Item) databaseObject).__getTaxvalue()).divide(Constants.BD100, 9, BigDecimal.ROUND_HALF_UP);
                    BigDecimal value = ((Item) databaseObject).__getNetvalue().add(((Item) databaseObject).__getTaxvalue()).subtract(((Item) databaseObject).__getDiscountvalue());
                    //  <li>TYPE_INVOICE = 0;
                    //  <li>TYPE_ORDER = 1;
                    //  <li>TYPE_OFFER = 2;
                    switch (type) {
                        case Item.TYPE_INVOICE:
                        case Item.TYPE_PART_PAYMENT:
                        case Item.TYPE_DEPOSIT:
                            billsSum = billsSum.add(value);
                            break;
                        case Item.TYPE_OFFER:
                            offerSum = offerSum.add(value);
                            break;
                        case Item.TYPE_ORDER:
                            orderSum = orderSum.add(value);
                            break;
                    }

                    data[i][4] = Item.getTypeString(type);
                    data[i][5] = FormatNumber.formatLokalCurrency(value);
                    data[i][6] = databaseObject.getIcon();
                    ((ImageIcon) data[i][6]).setDescription(Item.getStatusString(((Item) databaseObject).__getIntstatus()));
                } else if (databaseObject instanceof Schedule) {
                    Schedule sched = (Schedule) databaseObject;
                    data[i][0] = sched;
                    data[i][1] = User.getUsername(sched.__getIntaddedby());
                    data[i][2] = sched.__getNextdate();
                    data[i][3] = sched.__getStopdate();
                    data[i][4] = Item.getTypeString(0);
                    data[i][5] = 0;
                    data[i][6] = sched.getIcon();
                    ((ImageIcon) data[i][6]).setDescription(Messages.NEW_INVOICE.toString());
                } else if (databaseObject instanceof Revenue) {
                    Revenue rev = (Revenue) databaseObject;
                    data[i][0] = rev;
                    data[i][1] = User.getUsername(rev.__getIntaddedby());
                    data[i][2] = rev.__getDateadded();
                    try {
                        data[i][3] = DatabaseObject.getObject(Context.getGroup(),
                                rev.__getGroupsids());
                    } catch (NodataFoundException ex) {
                        data[i][3] = "N/A";
                    }
                    data[i][4] = Messages.TYPE_REVENUE.toString();
                    BigDecimal value = rev.__getBrutvalue();
                    billsSum = billsSum.add(value);
                    data[i][5] = FormatNumber.formatLokalCurrency(value);
                    data[i][6] = rev.getIcon();
                    ((ImageIcon) data[i][6]).setDescription(Item.getStatusString(rev.__getStatus()));
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
                    BigDecimal.class,
                    ImageIcon.class
                });
        table.setModel(m);

        orders.setText(FormatNumber.formatLokalCurrency(orderSum));
        offers.setText(FormatNumber.formatLokalCurrency(offerSum));
        bills.setText(FormatNumber.formatLokalCurrency(billsSum));
    }

    /**
     * Filter by group
     * @param g
     */
    @SuppressWarnings("unchecked")
    public void filterByTimeframe(vTimeframe g) {
        HashMap<Color, List<? extends DatabaseObject>> map = Scheduler.getOverdueEvents(g);
        this.show(map, this.getOverdue());
    }

    /**
     * Filter by group
     * @param g
     */
    @SuppressWarnings("unchecked")
    public void filterByTimeframe2(vTimeframe g) {
        HashMap<Color, List<? extends DatabaseObject>> map = Scheduler.getScheduledBills(g);
        this.show(map, this.getNextEvents());
    }

    @SuppressWarnings("unchecked")
    public void filterByItemType(Integer Type) {
        setData(odata, overdue);
        if (Type >= 0) {
            Object[][] data = ((MPTableModel) overdue.getModel()).getData();
            List<DatabaseObject> list = new ArrayList<DatabaseObject>();
            for (int i = 0; i < data.length; i++) {
                Item d = (Item) data[i][0];
                if (d.__getInttype() == Type) {
                    list.add(d);
                }
            }
            setData(list, overdue);
        }
    }

    /**
     * Filter by group
     * @param g
     */
    @SuppressWarnings({"unchecked"})
    public void filterByGroup(Group g) {
        setData(odata, overdue);
        Object[][] data = ((MPTableModel) overdue.getModel()).getData();
        List<DatabaseObject> list = new ArrayList<DatabaseObject>();
        if (g.__getIDS() != 1) {
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
        setData(list, overdue);
    }

    @SuppressWarnings("unchecked")
    public void filterByItemType2(Integer Type) {
        setData(xdata, nextEvents);
        if (Type >= 0) {
            Object[][] data = ((MPTableModel) nextEvents.getModel()).getData();
            List<DatabaseObject> list = new ArrayList<DatabaseObject>();
            for (int i = 0; i < data.length; i++) {
                Item d = (Item) data[i][0];
                if (d.__getInttype() == Type) {
                    list.add(d);
                }
            }
            setData(list, nextEvents);
        }
    }

    /**
     * Filter by group
     * @param g
     */
    @SuppressWarnings({"unchecked"})
    public void filterByGroup2(Group g) {
        setData(xdata, nextEvents);
        Object[][] data = ((MPTableModel) nextEvents.getModel()).getData();
        List<DatabaseObject> list = new ArrayList<DatabaseObject>();
        if (g.__getIDS() != 1) {
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
        setData(list, nextEvents);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jSplitPane1 = new javax.swing.JSplitPane();
        jPanel1 = new javax.swing.JPanel();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        timeframeChooser1 = new mpv5.ui.beans.TimeframeChooser();
        labeledCombobox1 = new mpv5.ui.beans.LabeledCombobox();
        jScrollPane2 = new javax.swing.JScrollPane();
        overdue = new  mpv5.ui.misc.MPTable(this) {
            private static final long serialVersionUID = 1L;
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
        labeledCombobox2 = new mpv5.ui.beans.LabeledCombobox();
        jButton3 = new javax.swing.JButton();
        offers = new mpv5.ui.beans.LabeledTextField();
        bills = new mpv5.ui.beans.LabeledTextField();
        orders = new mpv5.ui.beans.LabeledTextField();
        but6 = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jButton6 = new javax.swing.JButton();
        jButton5 = new javax.swing.JButton();
        timeframeChooser2 = new mpv5.ui.beans.TimeframeChooser();
        labeledCombobox3 = new mpv5.ui.beans.LabeledCombobox();
        labeledCombobox4 = new mpv5.ui.beans.LabeledCombobox();
        jButton4 = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        nextEvents = new  mpv5.ui.misc.MPTable(this) {
            private static final long serialVersionUID = 1L;
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
        but5 = new javax.swing.JButton();
        but4 = new javax.swing.JButton();

        setName("Form"); // NOI18N

        jSplitPane1.setDividerLocation(200);
        jSplitPane1.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        jSplitPane1.setName("jSplitPane1"); // NOI18N

        java.util.ResourceBundle bundle =  mpv5.i18n.LanguageManager.getBundle(); // NOI18N
        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("HomeScreen.jPanel1.border.title"))); // NOI18N
        jPanel1.setName("jPanel1"); // NOI18N

        jButton1.setText(bundle.getString("HomeScreen.jButton1.text")); // NOI18N
        jButton1.setName("jButton1"); // NOI18N
        jButton1.setPreferredSize(new java.awt.Dimension(60, 23));
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton2.setText(bundle.getString("HomeScreen.jButton2.text")); // NOI18N
        jButton2.setName("jButton2"); // NOI18N
        jButton2.setPreferredSize(new java.awt.Dimension(60, 23));
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        timeframeChooser1.setName("timeframeChooser1"); // NOI18N

        labeledCombobox1.set_Label(bundle.getString("HomeScreen.labeledCombobox1._Label")); // NOI18N
        labeledCombobox1.setName("labeledCombobox1"); // NOI18N

        jScrollPane2.setName("jScrollPane2"); // NOI18N

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
        overdue.setAutoCreateRowSorter(true);
        overdue.setName("overdue"); // NOI18N
        overdue.setShowVerticalLines(false);
        overdue.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                overdueMouseClicked(evt);
            }
        });
        jScrollPane2.setViewportView(overdue);

        labeledCombobox2.set_Label(bundle.getString("HomeScreen.labeledCombobox2._Label")); // NOI18N
        labeledCombobox2.setName("labeledCombobox2"); // NOI18N

        jButton3.setText(bundle.getString("HomeScreen.jButton3.text")); // NOI18N
        jButton3.setName("jButton3"); // NOI18N
        jButton3.setPreferredSize(new java.awt.Dimension(60, 23));
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        offers.set_Label(bundle.getString("HomeScreen.offers._Label")); // NOI18N
        offers.setEnabled(false);
        offers.setName("offers"); // NOI18N

        bills.set_Label(bundle.getString("HomeScreen.bills._Label")); // NOI18N
        bills.setEnabled(false);
        bills.setName("bills"); // NOI18N

        orders.set_Label(bundle.getString("HomeScreen.orders._Label")); // NOI18N
        orders.setEnabled(false);
        orders.setName("orders"); // NOI18N

        but6.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/32/agt_reload.png"))); // NOI18N
        but6.setText(bundle.getString("HomeScreen.but6.text")); // NOI18N
        but6.setToolTipText(bundle.getString("HomeScreen.but6.toolTipText")); // NOI18N
        but6.setContentAreaFilled(false);
        but6.setFocusable(false);
        but6.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        but6.setName("but6"); // NOI18N
        but6.setPressedIcon(new javax.swing.ImageIcon(getClass().getResource("/images/32/xclock.png"))); // NOI18N
        but6.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/images/32/agt_reload.png"))); // NOI18N
        but6.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        but6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                but6ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 841, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addComponent(offers, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 124, Short.MAX_VALUE)
                .addComponent(orders, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(117, 117, 117)
                .addComponent(bills, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(timeframeChooser1, javax.swing.GroupLayout.PREFERRED_SIZE, 330, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 395, Short.MAX_VALUE)
                        .addComponent(but6, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(labeledCombobox2, javax.swing.GroupLayout.PREFERRED_SIZE, 325, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(labeledCombobox1, javax.swing.GroupLayout.PREFERRED_SIZE, 322, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                            .addComponent(timeframeChooser1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(but6, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(labeledCombobox2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(labeledCombobox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 52, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(orders, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(bills, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(offers, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("HomeScreen.jPanel2.border.title"))); // NOI18N
        jPanel2.setName("jPanel2"); // NOI18N

        jButton6.setText(bundle.getString("HomeScreen.jButton6.text")); // NOI18N
        jButton6.setName("jButton6"); // NOI18N
        jButton6.setPreferredSize(new java.awt.Dimension(60, 23));
        jButton6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton6ActionPerformed(evt);
            }
        });

        jButton5.setText(bundle.getString("HomeScreen.jButton5.text")); // NOI18N
        jButton5.setName("jButton5"); // NOI18N
        jButton5.setPreferredSize(new java.awt.Dimension(60, 23));
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton5ActionPerformed(evt);
            }
        });

        timeframeChooser2.setName("timeframeChooser2"); // NOI18N

        labeledCombobox3.set_Label(bundle.getString("HomeScreen.labeledCombobox3._Label")); // NOI18N
        labeledCombobox3.setName("labeledCombobox3"); // NOI18N

        labeledCombobox4.set_Label(bundle.getString("HomeScreen.labeledCombobox4._Label")); // NOI18N
        labeledCombobox4.setName("labeledCombobox4"); // NOI18N

        jButton4.setText(bundle.getString("HomeScreen.jButton4.text")); // NOI18N
        jButton4.setName("jButton4"); // NOI18N
        jButton4.setPreferredSize(new java.awt.Dimension(60, 23));
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        jScrollPane1.setName("jScrollPane1"); // NOI18N

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
        nextEvents.setAutoCreateRowSorter(true);
        nextEvents.setName("nextEvents"); // NOI18N
        nextEvents.setShowVerticalLines(false);
        nextEvents.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                nextEventsMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(nextEvents);

        but5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/32/edittrash.png"))); // NOI18N
        but5.setText(bundle.getString("HomeScreen.but5.text")); // NOI18N
        but5.setToolTipText(bundle.getString("HomeScreen.but5.toolTipText")); // NOI18N
        but5.setContentAreaFilled(false);
        but5.setFocusable(false);
        but5.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        but5.setName("but5"); // NOI18N
        but5.setPressedIcon(new javax.swing.ImageIcon(getClass().getResource("/images/32/xclock.png"))); // NOI18N
        but5.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/images/32/bright_edittrash.png"))); // NOI18N
        but5.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        but5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                but5ActionPerformed(evt);
            }
        });

        but4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/32/view_right.png"))); // NOI18N
        but4.setText(bundle.getString("HomeScreen.but4.text")); // NOI18N
        but4.setToolTipText(bundle.getString("HomeScreen.but4.toolTipText")); // NOI18N
        but4.setContentAreaFilled(false);
        but4.setFocusable(false);
        but4.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        but4.setName("but4"); // NOI18N
        but4.setPressedIcon(new javax.swing.ImageIcon(getClass().getResource("/images/32/xclock.png"))); // NOI18N
        but4.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/images/32/view_right.png"))); // NOI18N
        but4.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        but4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                but4ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 841, Short.MAX_VALUE)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(but4, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 763, Short.MAX_VALUE)
                .addComponent(but5, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(timeframeChooser2, javax.swing.GroupLayout.PREFERRED_SIZE, 330, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(labeledCombobox4, javax.swing.GroupLayout.PREFERRED_SIZE, 325, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jButton4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(labeledCombobox3, javax.swing.GroupLayout.PREFERRED_SIZE, 322, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(51, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(timeframeChooser2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton6, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(9, 9, 9)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(labeledCombobox4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(labeledCombobox3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton5, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 50, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(but4, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(but5, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSplitPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 294, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 397, Short.MAX_VALUE)
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
                    if (itm.__getInttype() == Item.TYPE_INVOICE ||
                           itm.__getInttype() == Item.TYPE_DEPOSIT ||
                            itm.__getInttype() == Item.TYPE_PART_PAYMENT) {
                        if (Popup.Y_N_dialog(Messages.CREATE_REMINDER.toString())) {
                            rem = new Reminder();
                            rem.setCname(Messages.REMINDER.toString() + "@" + dbo.__getCname());
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

    private void but6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_but6ActionPerformed
        odata = xdata = null;
        refresh();
        HashMap<Color, List<? extends DatabaseObject>> map1 = Scheduler.getScheduledBills(null);
        Iterator<Color> it1 = map1.keySet().
                iterator();
        while (it1.hasNext()) {
            Color c = it1.next();
            List<? extends DatabaseObject> data = map1.get(c);
            if (!data.isEmpty()) {
                this.show(map1, this.getNextEvents());
                break;
            }
        }

        HashMap<Color, List<? extends DatabaseObject>> map2 = Scheduler.getOverdueEvents(null);
        Iterator<Color> it2 = map2.keySet().
                iterator();
        while (it2.hasNext()) {
            Color c = it2.next();
            List<? extends DatabaseObject> data = map2.get(c);
            if (!data.isEmpty()) {
                this.show(map2, this.getOverdue());
                break;
            }
        }
    }//GEN-LAST:event_but6ActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private mpv5.ui.beans.LabeledTextField bills;
    private javax.swing.JButton but4;
    private javax.swing.JButton but5;
    private javax.swing.JButton but6;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSplitPane jSplitPane1;
    private mpv5.ui.beans.LabeledCombobox labeledCombobox1;
    private mpv5.ui.beans.LabeledCombobox labeledCombobox2;
    private mpv5.ui.beans.LabeledCombobox labeledCombobox3;
    private mpv5.ui.beans.LabeledCombobox labeledCombobox4;
    private javax.swing.JTable nextEvents;
    private mpv5.ui.beans.LabeledTextField offers;
    private mpv5.ui.beans.LabeledTextField orders;
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
                            + item.__getCname());
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
        setData(odata, overdue);
        // Zyklische Objekte zurücksetzen ...
        timeframeChooser2.setTime(new vTimeframe(DateConverter.getStartOfMonth(new Date()), DateConverter.getEndOfMonth(new Date())));
        labeledCombobox3.getComboBox().setSelectedIndex(-1);
        labeledCombobox4.getComboBox().setSelectedIndex(-1);
        setData(xdata, nextEvents);
        // Navigation rechts aktualisieren ...
        sc.setDate(new Date());
    }
}
