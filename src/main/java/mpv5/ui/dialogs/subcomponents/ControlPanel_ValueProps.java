package mpv5.ui.dialogs.subcomponents;

import java.awt.Component;
import javax.swing.DefaultListModel;
import javax.swing.SwingUtilities;
import mpv5.data.PropertyStore;
import mpv5.db.common.*;
import mpv5.db.objects.Group;
import mpv5.db.objects.ValueProperty;
import mpv5.globals.GlobalSettings;
import mpv5.globals.Messages;
import mpv5.logging.Log;
import mpv5.ui.dialogs.ControlApplet;
import mpv5.ui.dialogs.Popup;
import mpv5.ui.dialogs.Search2;

/**
 *
 *
 */
public class ControlPanel_ValueProps extends javax.swing.JPanel implements ControlApplet, DatabaseObejctReceiver {

    private static final long serialVersionUID = 1L;
    /**
     * This unique name identifies this control applet
     */
    public final String UNAME = "valueproperties";
    private PropertyStore oldvalues;
    private ValueProperty dataOwner;

    public ControlPanel_ValueProps() {
        initComponents();
        setVisible(true);
        labeledCombobox1.setEditable(false);
        labeledCombobox1.setContext(Context.getValueProperties());
        labeledCombobox1.setNullSelection();
        labeledCombobox1.setReceiver(this);
        refresh();
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane6 = new javax.swing.JScrollPane();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane4 = new javax.swing.JScrollPane();
        jList1 = new javax.swing.JList();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jScrollPane5 = new javax.swing.JScrollPane();
        jList2 = new javax.swing.JList();
        labeledCombobox1 = new mpv5.ui.beans.LabeledCombobox();
        jPanel6 = new javax.swing.JPanel();
        jButton6 = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTextPane2 = new javax.swing.JTextPane();
        labeledTextField1 = new mpv5.ui.beans.LabeledTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        jPanel1 = new javax.swing.JPanel();
        jButton1 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jButton5 = new javax.swing.JButton();

        setName("Form"); // NOI18N
        setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.PAGE_AXIS));

        jScrollPane6.setName("jScrollPane6"); // NOI18N

        java.util.ResourceBundle bundle = mpv5.i18n.LanguageManager.getBundle();  // NOI18N
        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("ControlPanel_ValueProps.jPanel3.border.title"))); // NOI18N
        jPanel3.setName("jPanel3"); // NOI18N

        jScrollPane4.setName("jScrollPane4"); // NOI18N

        jList1.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jList1.setName("jList1"); // NOI18N
        jScrollPane4.setViewportView(jList1);

        jLabel1.setText(bundle.getString("ControlPanel_ValueProps.jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        jLabel2.setText(bundle.getString("ControlPanel_ValueProps.jLabel2.text")); // NOI18N
        jLabel2.setName("jLabel2"); // NOI18N

        jScrollPane5.setName("jScrollPane5"); // NOI18N

        jList2.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jList2.setName("jList2"); // NOI18N
        jScrollPane5.setViewportView(jList2);

        labeledCombobox1.set_Label(bundle.getString("ControlPanel_ValueProps.labeledCombobox1._Label")); // NOI18N
        labeledCombobox1.setName("labeledCombobox1"); // NOI18N

        jPanel6.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("ControlPanel_ValueProps.jPanel6.border.title"))); // NOI18N
        jPanel6.setName("jPanel6"); // NOI18N
        jPanel6.setPreferredSize(new java.awt.Dimension(200, 100));

        jButton6.setText(bundle.getString("ControlPanel_ValueProps.jButton6.text")); // NOI18N
        jButton6.setName("jButton6"); // NOI18N
        jButton6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton6ActionPerformed(evt);
            }
        });

        jScrollPane2.setName("jScrollPane2"); // NOI18N

        jTextPane2.setText(bundle.getString("ControlPanel_ValueProps.jTextPane2.text")); // NOI18N
        jTextPane2.setName("jTextPane2"); // NOI18N
        jScrollPane2.setViewportView(jTextPane2);

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addComponent(jButton6)
                        .addGap(417, 417, 417))
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 940, Short.MAX_VALUE)
                        .addContainerGap())))
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel6Layout.createSequentialGroup()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 151, Short.MAX_VALUE)
                .addGap(13, 13, 13)
                .addComponent(jButton6))
        );

        labeledTextField1.set_Label(bundle.getString("ControlPanel_ValueProps.labeledTextField1._Label")); // NOI18N
        labeledTextField1.setName("labeledTextField1"); // NOI18N

        jScrollPane1.setBorder(null);
        jScrollPane1.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        jScrollPane1.setName("jScrollPane1"); // NOI18N

        jTextArea1.setBackground(new java.awt.Color(-986896,true));
        jTextArea1.setColumns(20);
        jTextArea1.setLineWrap(true);
        jTextArea1.setRows(5);
        jTextArea1.setText(bundle.getString("ControlPanel_ValueProps.jTextArea1.text")); // NOI18N
        jTextArea1.setWrapStyleWord(true);
        jTextArea1.setName("jTextArea1"); // NOI18N
        jScrollPane1.setViewportView(jTextArea1);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(labeledCombobox1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(labeledTextField1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 169, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 277, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addGap(89, 89, 89)
                                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 169, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jScrollPane5)))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 415, Short.MAX_VALUE)
                .addContainerGap())
            .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, 972, Short.MAX_VALUE)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(labeledCombobox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1)
                            .addComponent(jLabel2))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 187, Short.MAX_VALUE)
                            .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 187, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(labeledTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 259, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, 210, Short.MAX_VALUE))
        );

        jScrollPane6.setViewportView(jPanel3);

        add(jScrollPane6);

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setName("jPanel1"); // NOI18N
        jPanel1.setLayout(new java.awt.FlowLayout(0));

        jButton1.setText(bundle.getString("ControlPanel_ValueProps.jButton1.text")); // NOI18N
        jButton1.setName("jButton1"); // NOI18N
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        jPanel1.add(jButton1);

        jButton4.setText(bundle.getString("ControlPanel_ValueProps.jButton4.text")); // NOI18N
        jButton4.setName("jButton4"); // NOI18N
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });
        jPanel1.add(jButton4);

        jButton5.setText(bundle.getString("ControlPanel_ValueProps.jButton5.text")); // NOI18N
        jButton5.setName("jButton5"); // NOI18N
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton5ActionPerformed(evt);
            }
        });
        jPanel1.add(jButton5);

        add(jPanel1);
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed

        if (dataOwner != null) {
            dataOwner.delete();
            refresh();
            dataOwner = null;
        }
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        prepare();
        String cname = labeledTextField1.getText(true, "key");
        String val = "#" + jTextPane2.getText() + "#";

        if (dataOwner == null) {
            dataOwner = new ValueProperty(cname, val, ((ContextWrap) jList1.getSelectedValue()).c, (Group) jList2.getSelectedValue());
        } else {
            dataOwner.setValueObj(val);
            dataOwner.setCname(cname);
            dataOwner.setContextids((((ContextWrap) jList1.getSelectedValue()).c.getId()));
            dataOwner.setGroup((Group) jList2.getSelectedValue());
        }

        dataOwner.save();
        refresh();
    }//GEN-LAST:event_jButton4ActionPerformed

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
        prepare();
        String cname = labeledTextField1.getText(true, "key");
        String val = "#" + jTextPane2.getText() + "#";
        ValueProperty xx = new ValueProperty(cname, val, ((ContextWrap) jList1.getSelectedValue()).c, (Group) jList2.getSelectedValue());
        if (xx.save()) {
            dataOwner = xx;
        }
        refresh();
    }//GEN-LAST:event_jButton5ActionPerformed

    private void jButton6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton6ActionPerformed
        DatabaseObject s = Search2.showSearchFor(((ContextWrap) jList1.getSelectedValue()).c);
        String sm = GlobalSettings.getProperty("org.openyabs.config.scriptsymbol", "#");
        if (s != null) {
            try {
                String t = jTextPane2.getText();
                if (!t.contains(sm)) {
                    t = sm + t + sm;
                }
                Popup.notice(s.evaluateAll(t, true));
            } catch (Exception e) {
                Popup.error(e);
            }
        }
    }//GEN-LAST:event_jButton6ActionPerformed

    @Override
    public void setValues(PropertyStore values) {
        oldvalues = values;

    }

    @Override
    public String getUname() {
        return UNAME;
    }

    @Override
    public void reset() {
        setValues(oldvalues);
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JList jList1;
    private javax.swing.JList jList2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JTextPane jTextPane2;
    private mpv5.ui.beans.LabeledCombobox labeledCombobox1;
    private mpv5.ui.beans.LabeledTextField labeledTextField1;
    // End of variables declaration//GEN-END:variables

    private void setSettings() {
    }

    @Override
    public Component getAndRemoveActionPanel() {
        this.remove(jPanel1);
        validate();
        return jPanel1;
    }

    private void refresh() {

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                DefaultListModel def = new DefaultListModel();
                def.addElement(new ContextWrap(Messages.TYPE_INVOICE.getValue(), Context.getInvoice()));
                def.addElement(new ContextWrap(Messages.TYPE_ORDER.getValue(), Context.getOrder()));
                def.addElement(new ContextWrap(Messages.TYPE_OFFER.getValue(), Context.getOffer()));
                def.addElement(new ContextWrap(Messages.TYPE_DEPOSIT.getValue(), Context.getDeposit()));
                def.addElement(new ContextWrap(Messages.TYPE_PART_PAYMENT.getValue(), Context.getPartPayment()));
                def.addElement(new ContextWrap(Messages.TYPE_CREDIT.getValue(), Context.getCredit()));
//            def.addElement(new ContextWrap(Messages.TYPE_SUBITEM.getValue(), Context.getSubItem()));
                def.addElement(new ContextWrap(Messages.TYPE_PRODUCT.getValue(), Context.getProduct()));
                def.addElement(new ContextWrap(Messages.TYPE_PRODUCT_ORDER.getValue(), Context.getProductOrder()));
                def.addElement(new ContextWrap(Messages.TYPE_SUPPLIER.getValue(), Context.getSupplier()));
                def.addElement(new ContextWrap(Messages.TYPE_MANUFACTURER.getValue(), Context.getManufacturer()));
                def.addElement(new ContextWrap(Messages.TYPE_CUSTOMER.getValue(), Context.getCustomer()));
                def.addElement(new ContextWrap(Messages.TYPE_REMINDER.getValue(), Context.getReminder()));
                def.addElement(new ContextWrap(Messages.TYPE_CONVERSATION.getValue(), Context.getConversation()));
                def.addElement(new ContextWrap(Messages.TYPE_MASSPRINT.getValue(), Context.getMassprint()));

                jList1.setModel(def);

                DefaultListModel def1 = new DefaultListModel();
                try {
                    for (DatabaseObject c : Group.getObjects(Context.getGroup())) {
                        def1.addElement((Group) c);
                    }
                } catch (NodataFoundException ex) {
                    Log.Debug(ex);
                }
                jList2.setModel(def1);

                labeledCombobox1.setModel(DatabaseObject.toObjectList(ValueProperty.getGroupProperties()));

            }
        };
        SwingUtilities.invokeLater(runnable);
    }

    @Override
    public void receive(final DatabaseObject obj) {

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                dataOwner = (ValueProperty) obj;
                jTextPane2.setText(dataOwner.getValue().toString().replace("#", ""));
                labeledTextField1.setText(dataOwner.getKey());
                jList1.setSelectedValue(new ContextWrap(null, Context.getByID(((ValueProperty) obj).__getContextids())), true);
                //jList1.setSelectedValue(new ContextWrap(null, Context.getByID(((ValueProperty) obj).__getContextids())), true);
                try {
                    jList2.setSelectedValue(((ValueProperty) obj).getGroup(), true);
                } catch (Exception ex) {
                    Log.Debug(ex);
                }
            }
        };
        SwingUtilities.invokeLater(runnable);
    }

    private void prepare() {

        if (jList1.getSelectedValue() == null) {
            jList1.setSelectedIndex(0);
        }
        if (jList2.getSelectedValue() == null) {
            jList2.setSelectedIndex(0);
        }
        if (jTextPane2.getText().length() == 0) {
            jTextPane2.setText("return \"\"");
        }

    }

}
