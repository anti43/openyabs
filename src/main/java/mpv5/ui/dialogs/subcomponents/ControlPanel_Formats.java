package mpv5.ui.dialogs.subcomponents;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.MessageFormat;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import mpv5.data.PropertyStore;
import mpv5.db.common.*;
import mpv5.db.objects.Group;
import mpv5.db.objects.Item;
import mpv5.globals.Messages;
import mpv5.logging.Log;
import mpv5.i18n.LanguageManager;
import mpv5.ui.dialogs.ControlApplet;
import mpv5.ui.dialogs.Popup;
import mpv5.usermanagement.MPSecurityManager;
import mpv5.db.objects.User;
import mpv5.globals.Constants;
import mpv5.globals.GlobalSettings;
import mpv5.handler.FormatHandler;
import mpv5.handler.MPEnum;
import mpv5.handler.VariablesHandler;
import mpv5.ui.dialogs.Notificator;
import mpv5.utils.models.MPComboBoxModelItem;
import mpv5.utils.models.MPTableModel;
import mpv5.utils.tables.TableFormat;

/**
 *
 *
 */
public final class ControlPanel_Formats extends javax.swing.JPanel implements ControlApplet {

    private static final long serialVersionUID = 1L;
    /**
     * This unique name identifies this control applet
     */
    public final String UNAME = "formats";
    private Integer currentUser;
    private final java.util.ResourceBundle bundle = mpv5.i18n.LanguageManager.getBundle();

    public ControlPanel_Formats() {
        if (MPSecurityManager.checkAdminAccess()) {
            initComponents();
            refresh();
            setTable();
            setVisible(true);
            setacL();
        }
    }

    public ControlPanel_Formats(User user) {
        if (MPSecurityManager.checkAdminAccess()) {
            initComponents();
            refresh();
            setdata(user);
            setVisible(true);
            setacL();
        }
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jButton5 = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jComboBox1 = new javax.swing.JComboBox();
        jLabel1 = new javax.swing.JLabel();
        labeledTextField1 = new mpv5.ui.beans.LabeledTextField();
        minstart = new mpv5.ui.beans.LabeledSpinner();
        jLabel4 = new javax.swing.JLabel();
        positionstart = new mpv5.ui.beans.LabeledSpinner();
        jLabel3 = new javax.swing.JLabel();
        locales = new javax.swing.JComboBox();
        labeledCombobox1 = new mpv5.ui.beans.LabeledCombobox();
        labeledCombobox2 = new mpv5.ui.beans.LabeledCombobox();
        labeledCombobox3 = new mpv5.ui.beans.LabeledCombobox();
        jLabel2 = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jPanel6 = new javax.swing.JPanel();
        jButton1 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();

        setBackground(new java.awt.Color(255, 255, 255));
        setName("Form"); // NOI18N
        setLayout(new java.awt.BorderLayout());

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        java.util.ResourceBundle bundle =  mpv5.i18n.LanguageManager.getBundle(); // NOI18N
        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("ControlPanel_Formats.jPanel1.border.title"))); // NOI18N
        jPanel1.setName("jPanel1"); // NOI18N
        jPanel1.setLayout(new java.awt.BorderLayout());

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("ControlPanel_Formats.jPanel2.border.title"))); // NOI18N
        jPanel2.setName("jPanel2"); // NOI18N

        jButton5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/32/2leftarrow.png"))); // NOI18N
        jButton5.setText(bundle.getString("ControlPanel_Formats.jButton5.text")); // NOI18N
        jButton5.setToolTipText(bundle.getString("ControlPanel_Formats.jButton5.toolTipText")); // NOI18N
        jButton5.setName("jButton5"); // NOI18N
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton5ActionPerformed(evt);
            }
        });

        jPanel3.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel3.setName("jPanel3"); // NOI18N

        jComboBox1.setName("jComboBox1"); // NOI18N
        jComboBox1.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jComboBox1ItemStateChanged(evt);
            }
        });
        jComboBox1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox1ActionPerformed(evt);
            }
        });

        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/32/1downarrow.png"))); // NOI18N
        jLabel1.setText(bundle.getString("ControlPanel_Formats.jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        labeledTextField1.set_Label(bundle.getString("ControlPanel_Formats.labeledTextField1._Label")); // NOI18N
        labeledTextField1.setName("labeledTextField1"); // NOI18N

        minstart.set_Label(bundle.getString("ControlPanel_Formats.minstart._Label")); // NOI18N
        minstart.setName("minstart"); // NOI18N

        jLabel4.setText(bundle.getString("ControlPanel_Formats.jLabel4.text")); // NOI18N
        jLabel4.setName("jLabel4"); // NOI18N

        positionstart.set_Label(bundle.getString("ControlPanel_Formats.positionstart._Label")); // NOI18N
        positionstart.setName("positionstart"); // NOI18N

        jLabel3.setText(bundle.getString("ControlPanel_Formats.jLabel3.text")); // NOI18N
        jLabel3.setName("jLabel3"); // NOI18N

        locales.setName("locales"); // NOI18N

        labeledCombobox1.set_Label(bundle.getString("ControlPanel_Formats.labeledCombobox1._Label_1")); // NOI18N
        labeledCombobox1.setName("labeledCombobox1"); // NOI18N

        labeledCombobox2.set_Label(bundle.getString("ControlPanel_Formats.labeledCombobox2._Label_1")); // NOI18N
        labeledCombobox2.setName("labeledCombobox2"); // NOI18N

        labeledCombobox3.set_Label(bundle.getString("ControlPanel_Formats.labeledCombobox3._Label_1")); // NOI18N
        labeledCombobox3.setName("labeledCombobox3"); // NOI18N

        jLabel2.setText(bundle.getString("ControlPanel_Formats.jLabel2.text")); // NOI18N
        jLabel2.setName("jLabel2"); // NOI18N

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel4, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(labeledTextField1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addComponent(labeledCombobox3, javax.swing.GroupLayout.DEFAULT_SIZE, 243, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, 165, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(labeledCombobox1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(labeledCombobox2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel1))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(minstart, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(positionstart, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(locales, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(labeledCombobox3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(labeledCombobox1, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(labeledCombobox2, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(labeledTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel2)
                .addGap(11, 11, 11)
                .addComponent(minstart, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(positionstart, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(locales, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(40, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(jButton5)
                .addContainerGap(213, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton5))
        );

        jPanel1.add(jPanel2, java.awt.BorderLayout.EAST);

        jPanel4.setName("jPanel4"); // NOI18N
        jPanel4.setLayout(new java.awt.BorderLayout());

        jScrollPane1.setName("jScrollPane1"); // NOI18N

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
        jScrollPane1.setViewportView(jTable1);

        jPanel4.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        jPanel1.add(jPanel4, java.awt.BorderLayout.CENTER);

        add(jPanel1, java.awt.BorderLayout.CENTER);

        jPanel6.setBackground(new java.awt.Color(255, 255, 255));
        jPanel6.setName("jPanel6"); // NOI18N
        jPanel6.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        jButton1.setText(bundle.getString("ControlPanel_Formats.jButton1.text")); // NOI18N
        jButton1.setName("jButton1"); // NOI18N
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        jPanel6.add(jButton1);

        jButton3.setText(bundle.getString("ControlPanel_Formats.jButton3.text")); // NOI18N
        jButton3.setName("jButton3"); // NOI18N
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });
        jPanel6.add(jButton3);

        add(jPanel6, java.awt.BorderLayout.PAGE_END);
    }// </editor-fold>//GEN-END:initComponents

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        test();
}//GEN-LAST:event_jButton3ActionPerformed

    private void jComboBox1ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jComboBox1ItemStateChanged
    }//GEN-LAST:event_jComboBox1ItemStateChanged

    private void jComboBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox1ActionPerformed

        currentUser = Integer.valueOf(((MPComboBoxModelItem) jComboBox1.getSelectedItem()).getId());
        try {
            setLocale(currentUser);
        } catch (Exception e) {
            Log.Debug(e);
        }
        setTable();
    }//GEN-LAST:event_jComboBox1ActionPerformed

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
        if (test()) {
            save();
        }
    }//GEN-LAST:event_jButton5ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        if (test()) {
            save();
        }
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jTable1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTable1MouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_jTable1MouseClicked

    @Override
    public void setValues(PropertyStore values) {
    }

    @Override
    public String getUname() {
        return UNAME;
    }

    @Override
    public void reset() {
        
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton5;
    private javax.swing.JComboBox jComboBox1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    private mpv5.ui.beans.LabeledCombobox labeledCombobox1;
    private mpv5.ui.beans.LabeledCombobox labeledCombobox2;
    private mpv5.ui.beans.LabeledCombobox labeledCombobox3;
    private mpv5.ui.beans.LabeledTextField labeledTextField1;
    private javax.swing.JComboBox locales;
    private mpv5.ui.beans.LabeledSpinner minstart;
    private mpv5.ui.beans.LabeledSpinner positionstart;
    // End of variables declaration//GEN-END:variables

    public void refresh() {
        locales.setModel(LanguageManager.getLocalesAsComboBoxModel());
        setLocale(mpv5.db.objects.User.getCurrentUser().__getIDS());
        minstart.getSpinner().setModel(new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 1));
        minstart.getSpinner().setEditor(new JSpinner.NumberEditor(minstart.getSpinner()));
        minstart.getSpinner().updateUI();

        positionstart.getSpinner().setModel(new SpinnerNumberModel(0, -1000, Integer.MAX_VALUE, 1));
        positionstart.getSpinner().setEditor(new JSpinner.NumberEditor(positionstart.getSpinner()));
        positionstart.getSpinner().updateUI();

        try {
            QueryCriteria2 q = new QueryCriteria2();
            q.and(new QueryParameter(Context.getUser(), "groupsids", User.getCurrentUser().__getGroupsids(), QueryParameter.EQUALS));
            jComboBox1.setModel(MPComboBoxModelItem.toModel(QueryHandler.instanceOf().clone(Context.getUser()).getColumns(new String[]{"ids", "cname"}, 0, q)));
        } catch (NodataFoundException ex) {
        }
        try {
            labeledCombobox1.setModel(QueryHandler.instanceOf().clone(Context.getFormats()).select("inttype,cname", (String[]) null));
        } catch (Exception e) {
        }
        labeledCombobox2.setModel(VariablesHandler.GENERIC_VARS_FORMAT.values());
        try {
            labeledCombobox3.setModel(MPComboBoxModelItem.toModel((MPEnum[]) FormatHandler.TYPES.values()));
        } catch (Exception e) {
        }

        labeledTextField1.setText(FormatHandler.INTEGERPART_IDENTIFIER);
        labeledCombobox3.getComboBox().setSelectedIndex(0);

        setUser();
        setTable();

    }

    @Override
    public Component getAndRemoveActionPanel() {
        this.remove(jPanel6);
        validate();
        return jPanel6;
    }

    private void save() {
        String oval = labeledTextField1.getText();
        //set start value
        if (minstart.getSpinner().getValue() != null && Integer.valueOf(minstart.getSpinner().getValue().toString()) > 0) {
            labeledTextField1.setText(FormatHandler.START_VALUE_IDENTIFIER + Integer.valueOf(minstart.getSpinner().getValue().toString()) + FormatHandler.START_VALUE_IDENTIFIER + oval);
        }

        QueryCriteria c = new QueryCriteria("inttype", Integer.valueOf(labeledCombobox3.getSelectedItem().getId()));
        c.addAndCondition("usersids", Integer.valueOf(((MPComboBoxModelItem) jComboBox1.getSelectedItem()).getId()));
        QueryHandler.instanceOf().clone(Context.getFormats()).delete(c);
        QueryData what = new QueryData();
        what.add("inttype", Integer.valueOf(labeledCombobox3.getSelectedItem().getId()));
        what.add("cname", labeledTextField1.getText());
        what.add("usersids", Integer.valueOf(((MPComboBoxModelItem) jComboBox1.getSelectedItem()).getId()));
        QueryHandler.instanceOf().clone(Context.getFormats()).insert(what, null);
        try {
            User u = (User) User.getObject(Context.getUser(), Integer.valueOf(((MPComboBoxModelItem) jComboBox1.getSelectedItem()).getId()));
            u.getProperties().changeProperty("item.date.locale", ((MPComboBoxModelItem) locales.getSelectedItem()).getId());
            u.saveProperties();
        } catch (NumberFormatException ex) {
            Log.Debug(ex);
        } catch (NodataFoundException ex) {
            Log.Debug(ex);
        }

        if (oval.contains("[")
                && String.valueOf(positionstart.get_Value()).equals("0")) {
            Notificator.raiseNotification("If you use dynamic parts in your number format, you MUST define a position from where to parse the number from!\n "
                    + "If you do not define a parse position, the defined format may not work as expected.", true);//FIXME i18n
        }
        try {
            GlobalSettings.setProperty(new MessageFormat(oval).toPattern() + "_startposition", String.valueOf(positionstart.get_Value()));
            GlobalSettings.save();
        } catch (Exception ex) {
            Log.Debug(ex);
        }

        setTable();
        labeledTextField1.setText("");
        minstart.getSpinner().setValue(0);
        positionstart.getSpinner().setValue(0);
        labeledCombobox1.setModel(QueryHandler.instanceOf().clone(Context.getFormats()).select("inttype,cname", (String[]) null));
        Popup.notice(Messages.RESTART_REQUIRED);
    }

    private void setTable() {
        try {
            Object[][] d = QueryHandler.instanceOf().clone(Context.getFormats()).select("cname, inttype", new QueryCriteria("usersids", currentUser));
            for (Object[] d1 : d) {
                MPEnum[] t = FormatHandler.TYPES.values();
                for (MPEnum mPEnum : t) {
                    if (mPEnum.getId() == Integer.parseInt(d1[1].toString())) {
                        d1[1] = mPEnum.getName();
                        break;
                    }
                }
            }
            jTable1.setModel(new MPTableModel(d));
            TableFormat.hideHeader(jTable1);
        } catch (NodataFoundException ex) {
            Log.Debug(ex);
        }
    }

    private void setUser() {
        try {
            if (currentUser == null) {
                currentUser = mpv5.db.objects.User.getCurrentUser().getID();
                jComboBox1.setSelectedIndex(MPComboBoxModelItem.getItemID(String.valueOf(currentUser), jComboBox1.getModel()));
            } else {
                currentUser = Integer.valueOf(((MPComboBoxModelItem) jComboBox1.getSelectedItem()).getId());
            }

            setLocale(currentUser);
        } catch (NumberFormatException e) {
        }
    }

    private void setacL() {

        labeledCombobox1.getComboBox().addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                labeledTextField1.setText(labeledCombobox1.getSelectedItem());
                String prop = GlobalSettings.getProperty(new MessageFormat(labeledTextField1.getText()).toPattern() + "_startposition");
                if (prop != null) {
                    try {
                        int pos = Integer.valueOf(prop);
                        positionstart.set_Value(pos);
                    } catch (NumberFormatException numberFormatException) {
                        Log.Debug(numberFormatException);
                    }
                }
            }
        });

        labeledCombobox2.getComboBox().addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                labeledTextField1.set_Text(labeledCombobox2.getSelectedItem().getValue() + labeledTextField1.get_Text());
            }
        });
    }

    private void setdata(User user) {
        jComboBox1.setSelectedIndex(MPComboBoxModelItem.getItemID(String.valueOf(user.getID()), jComboBox1.getModel()));
        setLocale(user.__getIDS());
    }

    private boolean test() {

//        if (!labeledTextField1.getText().contains(FormatHandler.INTEGERPART_IDENTIFIER.substring(0, 3))) {
//            labeledTextField1.set_Text(labeledTextField1.getText() + FormatHandler.INTEGERPART_IDENTIFIER);
//        }
        try {
            User u = (User) User.getObject(Context.getUser(), Integer.valueOf(((MPComboBoxModelItem) jComboBox1.getSelectedItem()).getId()));
            u.getProperties().changeProperty("item.date.locale", ((MPComboBoxModelItem) locales.getSelectedItem()).getId());
            u.save();
        } catch (NumberFormatException ex) {
            Log.Debug(ex);
        } catch (NodataFoundException ex) {
            Log.Debug(ex);
        }

        String str;
        Item b;
        switch (Integer.parseInt(this.labeledCombobox3.getSelectedItem().getId())) {
            case Constants.TYPE_ORDER:
                b = (Item) DatabaseObject.getObject(Context.getOrder());
                break;
            case Constants.TYPE_OFFER:
                b = (Item) DatabaseObject.getObject(Context.getOffer());
                break;
            case Constants.TYPE_INVOICE:
                b = (Item) DatabaseObject.getObject(Context.getInvoice());
                break;
            case Constants.TYPE_DEPOSIT:
                b = (Item) DatabaseObject.getObject(Context.getDeposit());
                break;
            case Constants.TYPE_PART_PAYMENT:
                b = (Item) DatabaseObject.getObject(Context.getPartPayment());
                break;
            case Constants.TYPE_CREDIT:
                b = (Item) DatabaseObject.getObject(Context.getCredit());
                break;
            default:
                b = (Item) DatabaseObject.getObject(Context.getInvoice());
                break;
        }
        
        b.avoidNulls();
        b.setGroup(Group.getDefault());

        try {
            FormatHandler fh = new FormatHandler(b);
            String xx = VariablesHandler.parse(labeledTextField1.getText(), b);
            str = fh.toString(new FormatHandler.YMessageFormat((xx).substring(Integer.valueOf(positionstart.get_Value().toString())), null), Integer.valueOf(minstart.get_Value().toString()));
            str = Messages.THE_RESULT + xx.substring(0, Integer.valueOf(positionstart.get_Value().toString())) + str;

            return Popup.OK_dialog(str, Messages.NOTICE.getValue());
        } catch (NumberFormatException exception) {
            Popup.error(exception);
            return false;
        }
    }

    private void setLocale(int userid) {
        User u;
        try {
            u = new User(userid);
            Log.Debug(this, u + ": " + u.getProperties().getProperty("item.date.locale"));
            if (u.getProperties().hasProperty("item.date.locale")) {
                locales.setSelectedIndex(MPComboBoxModelItem.getItemID(u.getProperties().getProperty("item.date.locale"), locales.getModel()));
            } else {
                locales.setSelectedIndex(MPComboBoxModelItem.getItemID(mpv5.db.objects.User.getCurrentUser().__getLocale(), locales.getModel()));
            }
        } catch (NodataFoundException ex) {
            Log.Debug(ex);
        }
    }
}
