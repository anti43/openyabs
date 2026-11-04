/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

 /*
 * SplashScreen.java
 *
 * Created on 30.03.2009, 21:55:52
 */
package mpv5.ui.dialogs;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import javax.swing.JComboBox;
import javax.swing.JSpinner;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.db.common.DatabaseSearch;
import mpv5.db.common.NodataFoundException;
import mpv5.db.common.QueryCriteria;
import mpv5.db.objects.Contact;
import mpv5.db.objects.Item;
import mpv5.db.objects.Schedule;
import mpv5.db.objects.ScheduleTypes;
import mpv5.db.objects.User;
import mpv5.globals.Headers;
import mpv5.globals.Messages;
import mpv5.logging.Log;
import mpv5.ui.misc.Position;
import mpv5.ui.panels.HomeScreen;
import mpv5.ui.panels.calendar.ScheduleCalendarDayChooser;
import mpv5.utils.date.DateConverter;
import mpv5.utils.models.MPComboBoxModelItem;
import mpv5.utils.models.MPComboboxModel;
import mpv5.utils.models.MPTableModel;
import mpv5.utils.tables.TableFormat;

/**
 *
 *
 */
public class ScheduleEvents extends javax.swing.JFrame {

    private static final long serialVersionUID = 1L;
    private static ScheduleEvents icke;
    private Schedule dataOwner;

    public static ScheduleEvents instanceOf() {
        if (icke == null) {
            icke = new ScheduleEvents();
        }
        icke.setVisible(true);
        icke.clearViews();
        Log.Debug(ScheduleEvents.class, "Anzeige erfolgt ...");
        return icke;
    }

    private ScheduleEvents() {
        initComponents();
        ItemTypeBox.setModel(Item.getItemEnum());
        ItemTypeBox.getComboBox().addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                ItemBox.getComboBox().removeAllItems();
                JComboBox cb = (JComboBox) e.getSource();
                Integer id;
                if (cb.getSelectedIndex() >= 0) {
                    Log.Debug(this, "ItemType-ID: " + Integer.valueOf(ItemTypeBox.getSelectedItem().getId()));
                    switch (Integer.valueOf(ItemTypeBox.getSelectedItem().getId())) {
                        case Item.TYPE_ORDER:
                            ItemBox.setContext(Context.getOrder());
                            break;
                        case Item.TYPE_OFFER:
                            ItemBox.setContext(Context.getOffer());
                            break;
                        case Item.TYPE_INVOICE:
                            ItemBox.setContext(Context.getInvoice());
                            break;
                         case Item.TYPE_DEPOSIT:
                            ItemBox.setContext(Context.getDeposit());
                            break;
                        case Item.TYPE_PART_PAYMENT:
                            ItemBox.setContext(Context.getPartPayment());
                            break;
                        case Item.TYPE_CREDIT:
                            ItemBox.setContext(Context.getCredit());
                            break;
                        default:
                            ItemBox.setContext(Context.getInvoice());
                    }

                    ItemBox.setSearchEnabled(true);
                    ItemBox.getComboBox().addActionListener(new ActionListener() {

                        @Override
                        public void actionPerformed(ActionEvent e) {
                            JComboBox cb = (JComboBox) e.getSource();
                            Integer id;
                            try {
                                if (cb.getSelectedIndex() >= 0) {
                                    Log.Debug(this, "Item-ID: " + Integer.valueOf(ItemBox.getSelectedItem().getId()));
                                    Item i = (Item) DatabaseObject.getObject(Context.getInvoice(),
                                            Integer.valueOf(ItemBox.getSelectedItem().getId()));
                                    icke.refreshFromItem(i);
                                }
                            } catch (NodataFoundException ex) {
                                Log.Debug(this, ex);
                            }
                        }
                    });
                }
            }
        });
        for (ActionListener a : ItemTypeBox.getComboBox().getActionListeners()) {
            a.actionPerformed(new ActionEvent(ItemTypeBox.getComboBox(), 0, ""));
        }
        initSchedTyps();
        labeledCombobox3.setSearchEnabled(true);
        labeledCombobox3.setContext(Context.getContact());
        labeledCombobox3.getComboBox().addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                JComboBox cb = (JComboBox) e.getSource();
                String s;
                Integer id;
                try {
                    if (cb.getSelectedIndex() >= 0) {
                        id = Integer.valueOf(labeledCombobox3.getSelectedItem().getId());
                        Log.Debug(this, "Contact-ID: " + id);
                        Contact c = (Contact) DatabaseObject.getObject(Context.getContact(),
                                id);
                        icke.refreshFromContact(c);
                        Context i = Context.getInvoice();
                        s = Context.DEFAULT_ITEM_SEARCH + ", inttype";
                        Object[][] data = new DatabaseSearch(i).getValuesFor(s, "contactsids", c.__getIDS());
                        Log.Debug(this, "gefundene Items: " + data.length);
                        MPComboBoxModelItem[] items = new MPComboBoxModelItem[data.length];
                        for (int a = 0; a < data.length; a++) {
                            items[a] = new MPComboBoxModelItem(data[a][0],
                                    Item.getTypeString((Integer) data[a][4]) + ": " + data[a][1]);
                        }
                        item.setModel(new MPComboboxModel(items));
                    }
                } catch (NodataFoundException ex) {
                    Log.Debug(this, ex);
                }
            }
        });
        item.setSearchEnabled(false);
        new Position(this);
        contact.setSearchEnabled(true);
        contact.setContext(Context.getContact());
        contact.getComboBox().addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                JComboBox cb = (JComboBox) e.getSource();
                String s;
                Integer id;
                try {
                    if (cb.getSelectedIndex() >= 0) {
                        id = Integer.valueOf(contact.getSelectedItem().getId());
                        Log.Debug(this, "Contact-ID: " + id);
                        Contact c = (Contact) DatabaseObject.getObject(Context.getContact(),
                                id);
                        icke.refreshFromFree(c, null);
                    }
                } catch (NodataFoundException ex) {
                    Log.Debug(this, ex);
                }
            }
        });
        appointment.getDateChooser().getSpinner().addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                if (contact.getComboBox().getSelectedIndex() < 0) {
                    JSpinner src = (JSpinner) e.getSource();
                    refreshFromFree(null, (Date) src.getValue());
                }
            }
        });
        but5.setEnabled(false);
        but6.setEnabled(false);
        appType.setSelectedIndex(-1);
    }

    @Override
    public void dispose() {
        setVisible(false);
    }

    public void setDate(Date tday) {
        labeledDateChooser1.setDate(DateConverter.addDays(tday, 0));
        labeledDateChooser2.setDate(DateConverter.addYear(tday));
        labeledDateChooser3.setDate(tday);
        appointment.setDate(tday);
    }

    /**
     * This method is called from within the constructor to
     * initialize the form. WARNING: Do NOT modify this
     * code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        but4 = new javax.swing.JButton();
        but6 = new javax.swing.JButton();
        but5 = new javax.swing.JButton();
        jSeparator3 = new javax.swing.JSeparator();
        but8 = new javax.swing.JButton();
        but7 = new javax.swing.JButton();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        cyclic_invoice = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jLabel3 = new javax.swing.JLabel();
        labeledDateChooser2 = new mpv5.ui.beans.LabeledDateChooser();
        labeledDateChooser1 = new mpv5.ui.beans.LabeledDateChooser();
        labeledSpinner1 = new mpv5.ui.beans.LabeledSpinner();
        jLabel1 = new javax.swing.JLabel();
        ItemBox = new mpv5.ui.beans.LabeledCombobox();
        jButton5 = new javax.swing.JButton();
        ItemTypeBox = new mpv5.ui.beans.LabeledCombobox();
        single_invoice = new javax.swing.JPanel();
        labeledDateChooser3 = new mpv5.ui.beans.LabeledDateChooser();
        jLabel4 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTable2 = new javax.swing.JTable();
        labeledCombobox2 = new mpv5.ui.beans.LabeledCombobox();
        labeledCombobox3 = new mpv5.ui.beans.LabeledCombobox();
        item = new mpv5.ui.beans.LabeledCombobox();
        free_event = new javax.swing.JPanel();
        appointment = new mpv5.ui.beans.LabeledDateChooser();
        free_existing_events = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        jTable3 = new javax.swing.JTable();
        appType = new mpv5.ui.beans.LabeledCombobox();
        contact = new mpv5.ui.beans.LabeledCombobox();
        subject = new javax.swing.JTextField();
        subject_label = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        java.util.ResourceBundle bundle =  mpv5.i18n.LanguageManager.getBundle(); // NOI18N
        setTitle(bundle.getString("ScheduleEvents.title")); // NOI18N
        setAlwaysOnTop(true);
        setName("Form"); // NOI18N

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("ScheduleEvents.jPanel1.border.title"))); // NOI18N
        jPanel1.setName("jPanel1"); // NOI18N

        jPanel2.setName("jPanel2"); // NOI18N
        jPanel2.setLayout(new javax.swing.BoxLayout(jPanel2, javax.swing.BoxLayout.LINE_AXIS));

        but4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/32/filenew.png"))); // NOI18N
        but4.setText(bundle.getString("ScheduleEvents.but4.text")); // NOI18N
        but4.setToolTipText(bundle.getString("ScheduleEvents.but4.toolTipText")); // NOI18N
        but4.setContentAreaFilled(false);
        but4.setFocusable(false);
        but4.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        but4.setName("but4"); // NOI18N
        but4.setPressedIcon(new javax.swing.ImageIcon(getClass().getResource("/images/32/xclock.png"))); // NOI18N
        but4.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/images/32/bright_filenew.png"))); // NOI18N
        but4.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        but4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                but4ActionPerformed(evt);
            }
        });
        jPanel2.add(but4);

        but6.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/32/3floppy_unmount.png"))); // NOI18N
        but6.setText(bundle.getString("ScheduleEvents.but6.text")); // NOI18N
        but6.setToolTipText(bundle.getString("ScheduleEvents.but6.toolTipText")); // NOI18N
        but6.setContentAreaFilled(false);
        but6.setFocusable(false);
        but6.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        but6.setName("but6"); // NOI18N
        but6.setPressedIcon(new javax.swing.ImageIcon(getClass().getResource("/images/32/xclock.png"))); // NOI18N
        but6.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/images/32/bright_3floppy_unmount.png"))); // NOI18N
        but6.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        but6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                but6ActionPerformed(evt);
            }
        });
        jPanel2.add(but6);

        but5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/32/edittrash.png"))); // NOI18N
        but5.setText(bundle.getString("ScheduleEvents.but5.text")); // NOI18N
        but5.setToolTipText(bundle.getString("ScheduleEvents.but5.toolTipText")); // NOI18N
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
        jPanel2.add(but5);

        jSeparator3.setName("jSeparator3"); // NOI18N
        jPanel2.add(jSeparator3);

        but8.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/32/revert.png"))); // NOI18N
        but8.setText(bundle.getString("ScheduleEvents.but8.text")); // NOI18N
        but8.setToolTipText(bundle.getString("ScheduleEvents.but8.toolTipText")); // NOI18N
        but8.setContentAreaFilled(false);
        but8.setFocusable(false);
        but8.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        but8.setName("but8"); // NOI18N
        but8.setPressedIcon(new javax.swing.ImageIcon(getClass().getResource("/images/32/xclock.png"))); // NOI18N
        but8.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/images/32/bright_revert.png"))); // NOI18N
        but8.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        but8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                but8ActionPerformed(evt);
            }
        });
        jPanel2.add(but8);

        but7.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/32/finish.png"))); // NOI18N
        but7.setText(bundle.getString("ScheduleEvents.but7.text")); // NOI18N
        but7.setToolTipText(bundle.getString("ScheduleEvents.but7.toolTipText")); // NOI18N
        but7.setContentAreaFilled(false);
        but7.setFocusable(false);
        but7.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        but7.setName("but7"); // NOI18N
        but7.setPressedIcon(new javax.swing.ImageIcon(getClass().getResource("/images/32/xclock.png"))); // NOI18N
        but7.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/images/32/finish.png"))); // NOI18N
        but7.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        but7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                but7ActionPerformed(evt);
            }
        });
        jPanel2.add(but7);

        jTabbedPane1.setTabPlacement(javax.swing.JTabbedPane.BOTTOM);
        jTabbedPane1.setName("jTabbedPane1"); // NOI18N

        cyclic_invoice.setName("cyclic_invoice"); // NOI18N

        jScrollPane1.setName("jScrollPane1"); // NOI18N

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

        jLabel3.setText(bundle.getString("ScheduleEvents.jLabel3.text")); // NOI18N
        jLabel3.setName("jLabel3"); // NOI18N

        labeledDateChooser2.set_Label(bundle.getString("ScheduleEvents.labeledDateChooser2._Label")); // NOI18N
        labeledDateChooser2.setName("labeledDateChooser2"); // NOI18N

        labeledDateChooser1.setToolTipText(bundle.getString("ScheduleEvents.labeledDateChooser1.toolTipText")); // NOI18N
        labeledDateChooser1.set_Label(bundle.getString("ScheduleEvents.labeledDateChooser1._Label")); // NOI18N
        labeledDateChooser1.setName("labeledDateChooser1"); // NOI18N

        labeledSpinner1.set_Label(bundle.getString("ScheduleEvents.labeledSpinner1._Label")); // NOI18N
        labeledSpinner1.setName("labeledSpinner1"); // NOI18N

        jLabel1.setText(bundle.getString("ScheduleEvents.jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        ItemBox.set_Label(bundle.getString("ScheduleEvents.ItemBox._Label")); // NOI18N
        ItemBox.setName("ItemBox"); // NOI18N

        jButton5.setText(bundle.getString("ScheduleEvents.jButton5.text")); // NOI18N
        jButton5.setName("jButton5"); // NOI18N
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton5ActionPerformed(evt);
            }
        });

        ItemTypeBox.set_Label(bundle.getString("ScheduleEvents.ItemTypeBox._Label")); // NOI18N
        ItemTypeBox.setName("ItemTypeBox"); // NOI18N

        javax.swing.GroupLayout cyclic_invoiceLayout = new javax.swing.GroupLayout(cyclic_invoice);
        cyclic_invoice.setLayout(cyclic_invoiceLayout);
        cyclic_invoiceLayout.setHorizontalGroup(
            cyclic_invoiceLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(cyclic_invoiceLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(cyclic_invoiceLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addComponent(ItemBox, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 258, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(cyclic_invoiceLayout.createSequentialGroup()
                        .addComponent(labeledSpinner1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 57, Short.MAX_VALUE)
                        .addComponent(jButton5))
                    .addComponent(labeledDateChooser1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(labeledDateChooser2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(ItemTypeBox, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        cyclic_invoiceLayout.setVerticalGroup(
            cyclic_invoiceLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, cyclic_invoiceLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(ItemTypeBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(ItemBox, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(cyclic_invoiceLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(labeledSpinner1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(labeledDateChooser1, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(labeledDateChooser2, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 425, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jTabbedPane1.addTab(bundle.getString("ScheduleEvents.cyclic_invoice.TabConstraints.tabTitle"), cyclic_invoice); // NOI18N

        single_invoice.setName("single_invoice"); // NOI18N

        labeledDateChooser3.set_Label(bundle.getString("ScheduleEvents.labeledDateChooser3._Label")); // NOI18N
        labeledDateChooser3.setName("labeledDateChooser3"); // NOI18N

        jLabel4.setText(bundle.getString("ScheduleEvents.jLabel4.text")); // NOI18N
        jLabel4.setName("jLabel4"); // NOI18N

        jScrollPane2.setName("jScrollPane2"); // NOI18N

        jTable2.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {},
                {},
                {},
                {}
            },
            new String [] {

            }
        ));
        jTable2.setName("jTable2"); // NOI18N
        jTable2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTable2MouseClicked(evt);
            }
        });
        jScrollPane2.setViewportView(jTable2);

        labeledCombobox2.set_Label(bundle.getString("ScheduleEvents.labeledCombobox2._Label")); // NOI18N
        labeledCombobox2.setName("labeledCombobox2"); // NOI18N

        labeledCombobox3.set_Label(bundle.getString("ScheduleEvents.labeledCombobox3._Label")); // NOI18N
        labeledCombobox3.setName("labeledCombobox3"); // NOI18N

        item.set_Label(bundle.getString("ScheduleEvents.item._Label")); // NOI18N
        item.setName("item"); // NOI18N

        javax.swing.GroupLayout single_invoiceLayout = new javax.swing.GroupLayout(single_invoice);
        single_invoice.setLayout(single_invoiceLayout);
        single_invoiceLayout.setHorizontalGroup(
            single_invoiceLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(single_invoiceLayout.createSequentialGroup()
                .addGroup(single_invoiceLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(single_invoiceLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(single_invoiceLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(labeledCombobox3, javax.swing.GroupLayout.DEFAULT_SIZE, 365, Short.MAX_VALUE)
                            .addComponent(item, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(labeledDateChooser3, javax.swing.GroupLayout.DEFAULT_SIZE, 365, Short.MAX_VALUE)
                            .addComponent(labeledCombobox2, javax.swing.GroupLayout.DEFAULT_SIZE, 365, Short.MAX_VALUE)
                            .addGroup(single_invoiceLayout.createSequentialGroup()
                                .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 258, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, Short.MAX_VALUE))))
                    .addGroup(single_invoiceLayout.createSequentialGroup()
                        .addGap(12, 12, 12)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 365, Short.MAX_VALUE)))
                .addContainerGap())
        );
        single_invoiceLayout.setVerticalGroup(
            single_invoiceLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, single_invoiceLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(labeledCombobox3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(item, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(6, 6, 6)
                .addComponent(labeledDateChooser3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(labeledCombobox2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 343, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab(bundle.getString("ScheduleEvents.single_invoice.TabConstraints.tabTitle"), single_invoice); // NOI18N

        free_event.setName("free_event"); // NOI18N

        appointment.set_Label(bundle.getString("ScheduleEvents.appointment._Label")); // NOI18N
        appointment.setName("appointment"); // NOI18N

        free_existing_events.setText(bundle.getString("ScheduleEvents.free_existing_events.text")); // NOI18N
        free_existing_events.setName("free_existing_events"); // NOI18N

        jScrollPane3.setName("jScrollPane3"); // NOI18N

        jTable3.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {},
                {},
                {},
                {}
            },
            new String [] {

            }
        ));
        jTable3.setName("jTable3"); // NOI18N
        jTable3.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTable3MouseClicked(evt);
            }
        });
        jScrollPane3.setViewportView(jTable3);

        appType.set_Label(bundle.getString("ScheduleEvents.appType._Label")); // NOI18N
        appType.setName("appType"); // NOI18N

        contact.set_Label(bundle.getString("ScheduleEvents.contact._Label")); // NOI18N
        contact.setName("contact"); // NOI18N

        subject.setText(bundle.getString("ScheduleEvents.subject.text")); // NOI18N
        subject.setBounds(new java.awt.Rectangle(0, 0, 63, 25));
        subject.setName("subject"); // NOI18N
        subject.setPreferredSize(new java.awt.Dimension(63, 20));

        subject_label.setText(bundle.getString("ScheduleEvents.subject_label.text")); // NOI18N
        subject_label.setName("subject_label"); // NOI18N

        javax.swing.GroupLayout free_eventLayout = new javax.swing.GroupLayout(free_event);
        free_event.setLayout(free_eventLayout);
        free_eventLayout.setHorizontalGroup(
            free_eventLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(free_eventLayout.createSequentialGroup()
                .addGroup(free_eventLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(free_eventLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(free_eventLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(contact, javax.swing.GroupLayout.DEFAULT_SIZE, 365, Short.MAX_VALUE)
                            .addGroup(free_eventLayout.createSequentialGroup()
                                .addComponent(subject_label, javax.swing.GroupLayout.PREFERRED_SIZE, 58, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(41, 41, 41)
                                .addComponent(subject, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addComponent(appointment, javax.swing.GroupLayout.DEFAULT_SIZE, 365, Short.MAX_VALUE)
                            .addComponent(appType, javax.swing.GroupLayout.DEFAULT_SIZE, 365, Short.MAX_VALUE)
                            .addComponent(free_existing_events, javax.swing.GroupLayout.PREFERRED_SIZE, 258, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(free_eventLayout.createSequentialGroup()
                        .addGap(12, 12, 12)
                        .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 365, Short.MAX_VALUE)))
                .addContainerGap())
        );
        free_eventLayout.setVerticalGroup(
            free_eventLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, free_eventLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(contact, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(free_eventLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(subject_label, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(subject, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(appointment, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(appType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(free_existing_events)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 468, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jTabbedPane1.addTab(bundle.getString("ScheduleEvents.free_event.TabConstraints.tabTitle"), free_event); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jTabbedPane1)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(0, 0, 0)
                        .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 505, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        getContentPane().add(jPanel1, java.awt.BorderLayout.PAGE_START);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
        try {
            mpv5.YabsViewProxy.instance().getIdentifierView().addTab(DatabaseObject.getObject(Context.getInvoice(), Integer.valueOf(ItemBox.getSelectedItem().getId())));
        } catch (Exception ex) {
            Log.Debug(this, ex.getMessage());
        }
    }//GEN-LAST:event_jButton5ActionPerformed

    private void jTable1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTable1MouseClicked
        Log.Debug(this, "CYCLIC-Event geklicked...");
        Schedule d = (Schedule) jTable1.getModel().getValueAt(jTable1.convertRowIndexToModel(jTable1.getSelectedRow()), 0);
        Log.Debug(this, "CYCLIC-Event geklicked...0");
        if (d != null) {
            Log.Debug(this, "CYCLIC-Event geklicked...1");
            try {
                ItemBox.setModel(d.getItem());
                Log.Debug(this, "CYCLIC-Event geklicked...2");
            } catch (NodataFoundException ex) {
                Log.Debug(ex);
            }
            Log.Debug(this, "CYCLIC-Event geklicked...3");
            labeledDateChooser1.setDate(d.__getStartdate());
            Log.Debug(this, "CYCLIC-Event geklicked...4");
            labeledDateChooser2.setDate(d.__getStopdate());
            Log.Debug(this, "CYCLIC-Event geklicked...5");
            labeledSpinner1.setValue(d.__getIntervalmonth());
            Log.Debug(this, "CYCLIC-Event geklicked...6");
            dataOwner = d;
            but5.setEnabled(true);
            but6.setEnabled(true);
            Log.Debug(this, "CYCLIC-Event geklicked...fertig");
        }
    }//GEN-LAST:event_jTable1MouseClicked

    private void jTable2MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTable2MouseClicked
        Log.Debug(this, "Single-Event geklicked...");
        Schedule d = (Schedule) jTable2.getModel().getValueAt(jTable2.convertRowIndexToModel(jTable2.getSelectedRow()), 0);
        Log.Debug(this, "Single-Event geklicked...0");
        if (d != null) {
            Log.Debug(this, "Single-Event geklicked...1");
            try {
                labeledCombobox3.setModel(d.getContact());
                Log.Debug(this, "Single-Event geklicked...2");
            } catch (NodataFoundException ex) {
                Log.Debug(ex);
            }
            Log.Debug(this, "Single-Event geklicked...3");
            labeledCombobox2.setSelectedItem(d.__getEventtype());
            Log.Debug(this, "Single-Event geklicked...4");
            labeledDateChooser3.setDate(d.__getStartdate());
            Log.Debug(this, "Single-Event geklicked...5");
            dataOwner = d;
            but5.setEnabled(true);
            but6.setEnabled(true);
            Log.Debug(this, "Single-Event geklicked...fertig");
        }
    }//GEN-LAST:event_jTable2MouseClicked

    private void but6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_but6ActionPerformed
        Log.Debug(this, "ändern ... geklickt");
        Calendar cal = Calendar.getInstance();
        cal.setTime(dataOwner.__getStartdate());
        Date oldDate = cal.getTime();
        Log.Debug(this, "ändern ... 1");
        save(dataOwner);
        Log.Debug(this, "ändern ... 2");
        try {
            switch (jTabbedPane1.getSelectedIndex()) {
                case 0:
                    Log.Debug(this, "ändern ... 2a");
                    refreshFromItem(dataOwner.getItem());
                    break;
                case 1:
                    Log.Debug(this, "ändern ... 2b");
                    refreshFromContact(dataOwner.getContact());
                    Log.Debug(this, "ändern ... 2b.");
                    Log.Debug(this, oldDate + " / " + dataOwner.__getStartdate());
                    Log.Debug(this, "ändern ... 2b..");
                    break;
                case 2:
                    Log.Debug(this, "ändern ... 2c");
                    if (dataOwner.__getContactsids() != null && dataOwner.__getContactsids() > 0) {
                        refreshFromFree(dataOwner.getContact(), dataOwner.__getStartdate());
                    } else {
                        refreshFromFree(null, dataOwner.__getStartdate());
                    }
                    Log.Debug(this, "ändern ... 2c.");
                    Log.Debug(this, oldDate + " / " + dataOwner.__getStartdate());
                    Log.Debug(this, "ändern ... 2c..");
                    break;
                default:
                    break;
            }
        } catch (Exception ex) {
            Log.Debug(this,
                    ex);
        }
        Log.Debug(this, "ändern ... fertig");
    }//GEN-LAST:event_but6ActionPerformed

    private void but4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_but4ActionPerformed
        Log.Debug(this, "neu Anlage ... geklickt");
        save(dataOwner = new Schedule());
        Log.Debug(this, "neu Anlage ... 1");
        if (dataOwner != null) {
            try {
                switch (jTabbedPane1.getSelectedIndex()) {
                    case 0:
                        Log.Debug(this, "neu Anlage ... 1a");
                        refreshFromItem(dataOwner.getItem());
                        break;
                    case 1:
                        Log.Debug(this, "neu Anlage ... 1b");
                        refreshFromContact(dataOwner.getContact());
                        break;
                    case 2:
                        Log.Debug(this, "neu Anlage ... 1c");
                        if (dataOwner.__getContactsids() != null && dataOwner.__getContactsids() > 0) {
                            refreshFromFree(dataOwner.getContact(), dataOwner.__getStartdate());
                        } else {
                            refreshFromFree(null, dataOwner.__getStartdate());
                        }
                        break;
                    default:
                        break;
                }
                Log.Debug(this, "neu Anlage ... 2");
            } catch (NodataFoundException ex) {
                Log.Debug(this,
                        ex);
            }
            Log.Debug(this, "neu Anlage ... 3");
            ScheduleCalendarDayChooser.instanceOf();
            Log.Debug(this, "neu Anlage ... fertig");
        } else {
            Popup.error(this, Messages.ENTER_VALUE.toString());
        }
    }//GEN-LAST:event_but4ActionPerformed

    private void but5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_but5ActionPerformed
        Log.Debug(this, "löschen ... geklicked");
        if (dataOwner != null && dataOwner.isExisting()) {
            Log.Debug(this, "löschen ... 1");
            dataOwner.delete();
            Log.Debug(this, "löschen ... 2");
            try {
                switch (jTabbedPane1.getSelectedIndex()) {
                    case 0:
                        Log.Debug(this, "löschen ... 3a");
                        refreshFromItem(dataOwner.getItem());
                        break;
                    case 1: {
                        Log.Debug(this, "löschen ... 3b");
                        refreshFromContact(dataOwner.getContact());
                        Log.Debug(this, "löschen ... 3b.");
                        Calendar cal = Calendar.getInstance();
                        cal.setTime(dataOwner.__getStartdate());
                        Date oldDate = cal.getTime();
                        break;
                    }
                    case 2: {
                        Log.Debug(this, "löschen ... 3c");
                        if (dataOwner.__getContactsids() != null && dataOwner.__getContactsids() > 0) {
                            refreshFromFree(dataOwner.getContact(), dataOwner.__getStartdate());
                        } else {
                            refreshFromFree(null, dataOwner.__getStartdate());
                        }
                        Log.Debug(this, "löschen ... 3c.");
                        Calendar cal = Calendar.getInstance();
                        cal.setTime(dataOwner.__getStartdate());
                        Date oldDate = cal.getTime();
                        break;
                    }
                    default:
                        break;
                }
            } catch (NodataFoundException ex) {
                Log.Debug(ex);
            }
        }
        Log.Debug(this, "löschen ... fertig");
    }//GEN-LAST:event_but5ActionPerformed

    private void but7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_but7ActionPerformed
        this.setVisible(false);
        HomeScreen.getInstance();
    }//GEN-LAST:event_but7ActionPerformed

    private void but8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_but8ActionPerformed
        but5.setEnabled(false);
        but6.setEnabled(false);
        clearViews();
    }//GEN-LAST:event_but8ActionPerformed

    private void jTable3MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTable3MouseClicked
        Log.Debug(this, "Free-Event geklicked...");
        Schedule d = (Schedule) jTable3.getModel().getValueAt(jTable3.convertRowIndexToModel(jTable3.getSelectedRow()), 0);
        Log.Debug(this, "Free-Event geklicked...0");
        if (d != null) {
            Log.Debug(this, "Free-Event geklicked...1");
            if (d.__getContactsids() > 0) {
                try {

                    contact.setModel(d.getContact());
                    Log.Debug(this, "Free-Event geklicked...2");
                } catch (NodataFoundException ex) {
                    Log.Debug(ex);
                }
            }
            Log.Debug(this, "Free-Event geklicked...3");
            appType.setSelectedItem(d.__getEventtype());
            Log.Debug(this, "Free-Event geklicked...4");
            appointment.setDate(d.__getStartdate());
            Log.Debug(this, "Free-Event geklicked...5");
            subject.setText(d.__getCname());
            Log.Debug(this, "Free-Event geklicked...6");
            dataOwner = d;
            but5.setEnabled(true);
            but6.setEnabled(true);
            Log.Debug(this, "Free-Event geklicked...fertig");
        }
    }//GEN-LAST:event_jTable3MouseClicked

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private mpv5.ui.beans.LabeledCombobox ItemBox;
    private mpv5.ui.beans.LabeledCombobox ItemTypeBox;
    private mpv5.ui.beans.LabeledCombobox appType;
    private mpv5.ui.beans.LabeledDateChooser appointment;
    private javax.swing.JButton but4;
    private javax.swing.JButton but5;
    private javax.swing.JButton but6;
    private javax.swing.JButton but7;
    private javax.swing.JButton but8;
    private mpv5.ui.beans.LabeledCombobox contact;
    private javax.swing.JPanel cyclic_invoice;
    private javax.swing.JPanel free_event;
    private javax.swing.JLabel free_existing_events;
    private mpv5.ui.beans.LabeledCombobox item;
    private javax.swing.JButton jButton5;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JTable jTable2;
    private javax.swing.JTable jTable3;
    private mpv5.ui.beans.LabeledCombobox labeledCombobox2;
    private mpv5.ui.beans.LabeledCombobox labeledCombobox3;
    private mpv5.ui.beans.LabeledDateChooser labeledDateChooser1;
    private mpv5.ui.beans.LabeledDateChooser labeledDateChooser2;
    private mpv5.ui.beans.LabeledDateChooser labeledDateChooser3;
    private mpv5.ui.beans.LabeledSpinner labeledSpinner1;
    private javax.swing.JPanel single_invoice;
    private javax.swing.JTextField subject;
    private javax.swing.JLabel subject_label;
    // End of variables declaration//GEN-END:variables

    private void refreshFromItem(Item dao) {
        Log.Debug(this, "Anzeige der zyklischen Eventtabelle aktualisiert!");
        ArrayList<Schedule> data = Schedule.getEvents2(dao);
        Object[][] d = new Object[data.size()][];
        for (int i = 0; i < data.size(); i++) {
            Schedule schedule = data.get(i);
            d[i] = schedule.toArray();
        }
        jTable1.setModel(new MPTableModel(d, Headers.SCHEDULE_LIST));
        TableFormat.resizeCols(jTable1, new Integer[]{150}, false);
    }

    private void refreshFromContact(Contact dao) {
        Log.Debug(this, "Anzeige der einmaligen Eventtabelle aktualisiert!");
        ArrayList<Schedule> data = Schedule.getEvents(dao);
        Object[][] d = new Object[data.size()][4];
        QueryCriteria qc;
        for (int i = 0; i < data.size(); i++) {
            try {
                Schedule schedule = data.get(i);
                d[i][0] = schedule;
                d[i][1] = schedule.__getCname();
                d[i][2] = schedule.__getStartdate();
                qc = new QueryCriteria("USERSIDS", User.getCurrentUser().getID());
                qc.addAndCondition("IDS", schedule.__getEventtype());
                Log.Debug(this, "Eventtype: " + schedule.__getEventtype());
                ArrayList<DatabaseObject> SType = DatabaseObject.getObjects(Context.getScheduleTypes(),
                        qc);
                d[i][3] = SType.get(0).__getCname();
            } catch (NodataFoundException ex) {
                Log.Debug(this, ex);
            }
        }
        jTable2.setModel(new MPTableModel(d, Headers.SCHEDULE_PANEL));
        TableFormat.stripColumn(jTable2, 0);
        TableFormat.resizeCols(jTable2, new Integer[]{150}, false);
    }

    private void refreshFromFree(Contact dao, Date day) {
        Log.Debug(this, "Anzeige der freien Eventtabelle aktualisiert!");
        ArrayList<Schedule> data = Schedule.getEventsFree(day, dao);
        Object[][] d = new Object[data.size()][4];
        QueryCriteria qc;
        for (int i = 0; i < data.size(); i++) {
            try {
                Schedule schedule = data.get(i);
                d[i][0] = schedule;
                d[i][1] = schedule.__getCname();
                d[i][2] = schedule.__getStartdate();
                qc = new QueryCriteria("USERSIDS", User.getCurrentUser().getID());
                qc.addAndCondition("IDS", schedule.__getEventtype());
                Log.Debug(this, "Eventtype: " + schedule.__getEventtype());
                ArrayList<DatabaseObject> SType = DatabaseObject.getObjects(Context.getScheduleTypes(),
                        qc);
                d[i][3] = SType.get(0).__getCname();
            } catch (NodataFoundException ex) {
                Log.Debug(this, ex);
            }
        }
        jTable3.setModel(new MPTableModel(d, Headers.SCHEDULE_PANEL));
        TableFormat.stripColumn(jTable3, 0);
        TableFormat.resizeCols(jTable3, new Integer[]{150}, false);
    }

    private void save(Schedule s) {
        Component tmp;
        if (s != null) {
            try {
                switch (jTabbedPane1.getSelectedIndex()) {
                    case 0:
                        Item i = (Item) DatabaseObject.getObject(Context.getInvoice(),
                                Integer.valueOf(ItemBox.getSelectedItem().getId()));
                        s.setCname("(" + Messages.SCHEDULE + ") " + i.toString());
                        s.setItemsids(i.__getIDS());
                        s.setContactsids(i.__getContactsids());
                        s.setGroupsids(i.__getGroupsids());
                        s.setIntervalmonth(Integer.valueOf(labeledSpinner1.getSpinner().getValue().toString()));
                        s.setStartdate(labeledDateChooser1.getDate());
                        s.setStopdate(labeledDateChooser2.getDate());
                        s.setUsersids(mpv5.db.objects.User.getCurrentUser().__getIDS());
                        s.setNextdate(DateConverter.addMonths(labeledDateChooser1.getDate(),
                                s.__getIntervalmonth()));
                        s.setEventtype(1);
                        s.save();
                        dataOwner = s;
                        /*
                        * Hack um das POPUP ganz nach oben zu bringen ...
                         */
                        setAlwaysOnTop(false);
                        tmp = Popup.identifier;
                        Popup.identifier = this;
                        Popup.notice(Messages.SCHEDULE_NEXT
                                + DateConverter.getDefDateString(DateConverter.addMonths(labeledDateChooser1.getDate(),
                                        s.__getIntervalmonth())));
                        Popup.identifier = tmp;
                        setAlwaysOnTop(true);
                        break;
                    case 1: {
                        if (labeledCombobox3.getSelectedItem() == null) {
                            Popup.error(this, Messages.SELECT_A_CONTACT.toString());
                            dataOwner = null;
                            return;
                        }
                        if (item.getSelectedItem() == null) {
                            Popup.error(this, Messages.SELECT_AN_INVOICE.toString());
                            dataOwner = null;
                            return;
                        }
                        if (labeledCombobox2.getSelectedItem() == null) {
                            Popup.error(this, Messages.SELECT_A_TYPE.toString());
                            dataOwner = null;
                            return;
                        }
                        Contact c = (Contact) DatabaseObject.getObject(Context.getContact(),
                                Integer.valueOf(labeledCombobox3.getSelectedItem().getId()));
                        s.setCname("(" + Messages.SCHEDULE + ") " + c.toString());
                        s.setItemsids(Integer.parseInt(item.getSelectedItem().getId()));
                        s.setContactsids(c.__getIDS());
                        s.setEventtype(Integer.valueOf(labeledCombobox2.getSelectedItem().getId()));
                        s.setGroupsids(c.__getGroupsids());
                        s.setIntervalmonth(0);
                        s.setStartdate(labeledDateChooser3.getDate());
                        s.setStopdate(labeledDateChooser3.getDate());
                        s.setUsersids(mpv5.db.objects.User.getCurrentUser().__getIDS());
                        s.setNextdate(labeledDateChooser3.getDate());
                        s.save();
                        dataOwner = s;
                        break;
                    }
                    case 2: {
                        if (appType.getSelectedItem() == null) {
                            Popup.error(this, Messages.SELECT_A_TYPE.toString());
                            dataOwner = null;
                            return;
                        }
                        if (contact.getSelectedItem() != null) {
                            Contact c = (Contact) DatabaseObject.getObject(Context.getContact(),
                                    Integer.valueOf(contact.getSelectedItem().getId()));
                            s.setContactsids(c.__getIDS());
                        } else {
                            s.setContactsids(-1);
                            s.setGroupsids(0);
                        }
                        s.setCname(subject.getText());
                        s.setItemsids(-1);
                        s.setEventtype(Integer.valueOf(appType.getSelectedItem().getId()));
                        s.setIntervalmonth(0);
                        s.setStartdate(appointment.getDate());
                        s.setStopdate(appointment.getDate());
                        s.setUsersids(User.getCurrentUser().__getIDS());
                        s.setNextdate(appointment.getDate());
                        s.save();
                        dataOwner = s;
                        break;
                    }
                    default:
                        break;
                }
            } catch (Exception ex) {
                Log.Debug(this, ex.getMessage());
            }
        }
    }

    private void initSchedTyps() {
        try {
            final ArrayList<DatabaseObject> STypes = DatabaseObject.getObjects(Context.getScheduleTypes(),
                    new QueryCriteria("USERSIDS", User.getCurrentUser().getID()));
            MPComboBoxModelItem[] SchedTyps = new MPComboBoxModelItem[STypes.size()];

            for (int i = 0; i < STypes.size(); i++) {
                ScheduleTypes st = (ScheduleTypes) STypes.get(i);
                SchedTyps[i] = new MPComboBoxModelItem(st.__getIDS(),
                        st.__getCname());
            }
            labeledCombobox2.setModel(new MPComboboxModel(SchedTyps));
            appType.setModel(new MPComboboxModel(SchedTyps));
        } catch (NodataFoundException ex) {
            Log.Debug(this, ex);
        }
    }

    private void clearViews() {
        dataOwner = null;
        refreshFromItem(null);
        refreshFromContact(null);
        refreshFromFree(null, new Date());
        Log.Debug(this, "Views zurückgesetzt ...1");
        ItemBox.setSelectedIndex(-1);
        Log.Debug(this, "Views zurückgesetzt ...2");
        labeledCombobox2.setSelectedIndex(-1);
        Log.Debug(this, "Views zurückgesetzt ...3");
        labeledCombobox3.setSelectedIndex(-1);
        Log.Debug(this, "Views zurückgesetzt ...4");
        item.setSelectedIndex(-1);
        Log.Debug(this, "Views zurückgesetzt ...5");
        setDate(new Date());
        Log.Debug(this, "Views zurückgesetzt ...6");
        labeledSpinner1.setValue(1);
        Log.Debug(this, "Views zurückgesetzt ...7");
        contact.setSelectedIndex(-1);
        Log.Debug(this, "Views zurückgesetzt ...8");
        subject.setText("");
        Log.Debug(this, "Views zurückgesetzt ...9");
        appType.setSelectedIndex(-1);
        Log.Debug(this, "Views zurückgesetzt ...10");
    }
}
