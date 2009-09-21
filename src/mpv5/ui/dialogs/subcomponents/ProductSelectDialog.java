package mpv5.ui.dialogs.subcomponents;

import java.awt.BorderLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import mpv5.db.common.Context;
import mpv5.db.common.NodataFoundException;
import mpv5.db.objects.Item;
import mpv5.db.objects.Product;
import mpv5.db.objects.SubItem;
import mpv5.globals.Messages;
import mpv5.logging.Log;
import mpv5.utils.models.MPTableModel;

/**
 *
 * 
 */
public class ProductSelectDialog extends javax.swing.JPanel {

    private static ProductSelectDialog me;
    private static JFrame frame;

    /**
     * @param model
     * @param row
     * @param evt
     * @param productid
     * @return
     */
    public static ProductSelectDialog instanceOf(MPTableModel model, int row, MouseEvent evt, int productid) {
        if (me == null) {
            me = new ProductSelectDialog();
            frame = new JFrame();
            frame.setUndecorated(true);
            frame.setAlwaysOnTop(true);
            frame.setLayout(new BorderLayout());
            frame.add(me, BorderLayout.CENTER);
            frame.pack();
            frame.setTitle(Messages.ACTION_OPEN.toString());
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        }
        me.setModel(model);
        me.setEvent(evt);
        me.setRow(row);
        me.setProductid(productid);
        try {
            me.labeledCombobox1.setSelectedItem(Integer.valueOf(productid));
            me.statech();
        } catch (Exception e) {
        }
        frame.setLocation(evt.getLocationOnScreen().x - me.getWidth(), evt.getLocationOnScreen().y);
        frame.setVisible(true);
        return me;
    }
    private MPTableModel model;
    private MouseEvent event;
    private int row;
    private int pid;

    public ProductSelectDialog() {
        initComponents();
        jButton1.getInsets().set(0, 0, 0, 0);
        jButton2.getInsets().set(0, 0, 0, 0);
        validate();
        taxrate.getComboBox().setEditable(false);
        taxrate.setSearchOnEnterEnabled(true);
        taxrate.setContext(Context.getTaxes());
        taxrate.triggerSearch();
        value.set_ValueClass(Double.class);
        labeledCombobox1.setContext(Context.getProducts());

        labeledCombobox1.getComboBox().addItemListener(new ItemListener() {

            public void itemStateChanged(ItemEvent e) {
                statech();
            }
        });
        labeledCombobox1.triggerSearch();
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        labeledSpinner1 = new mpv5.ui.beans.LabeledSpinner();
        labeledCombobox1 = new mpv5.ui.beans.LabeledCombobox();
        jButton1 = new javax.swing.JButton();
        measure = new mpv5.ui.beans.LabeledTextField();
        taxrate = new mpv5.ui.beans.LabeledCombobox();
        value = new mpv5.ui.beans.LabeledTextField();
        jButton2 = new javax.swing.JButton();
        link = new mpv5.ui.beans.LabeledTextField();

        setBackground(new java.awt.Color(255, 255, 255));
        setName("Form"); // NOI18N
        setLayout(new java.awt.BorderLayout());

        jPanel1.setBackground(new java.awt.Color(182, 201, 201));
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("mpv5/resources/languages/Panels"); // NOI18N
        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("ProductSelectDialog.jPanel1.border.title"))); // NOI18N
        jPanel1.setName("jPanel1"); // NOI18N

        labeledSpinner1.set_Label(bundle.getString("ProductSelectDialog.labeledSpinner1._Label")); // NOI18N
        labeledSpinner1.set_Value(1);
        labeledSpinner1.setName("labeledSpinner1"); // NOI18N

        labeledCombobox1.set_Label(bundle.getString("ProductSelectDialog.labeledCombobox1._Label")); // NOI18N
        labeledCombobox1.setName("labeledCombobox1"); // NOI18N
        labeledCombobox1.setSearchOnEnterEnabled(true);

        jButton1.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        jButton1.setText(bundle.getString("ProductSelectDialog.jButton1.text")); // NOI18N
        jButton1.setName("jButton1"); // NOI18N
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        measure.set_Label(bundle.getString("ProductSelectDialog.measure._Label")); // NOI18N
        measure.setName("measure"); // NOI18N

        taxrate.set_Label(bundle.getString("ProductSelectDialog.taxrate._Label")); // NOI18N
        taxrate.setName("taxrate"); // NOI18N

        value.set_Label(bundle.getString("ProductSelectDialog.value._Label")); // NOI18N
        value.setName("value"); // NOI18N

        jButton2.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        jButton2.setText(bundle.getString("ProductSelectDialog.jButton2.text")); // NOI18N
        jButton2.setName("jButton2"); // NOI18N
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        link.set_Label(bundle.getString("ProductSelectDialog.link._Label")); // NOI18N
        link.setName("link"); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(link, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 476, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(labeledSpinner1, javax.swing.GroupLayout.DEFAULT_SIZE, 144, Short.MAX_VALUE)
                            .addComponent(measure, javax.swing.GroupLayout.DEFAULT_SIZE, 144, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(taxrate, javax.swing.GroupLayout.PREFERRED_SIZE, 152, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(value, javax.swing.GroupLayout.DEFAULT_SIZE, 168, Short.MAX_VALUE))
                            .addComponent(labeledCombobox1, javax.swing.GroupLayout.DEFAULT_SIZE, 326, Short.MAX_VALUE))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jButton1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 52, Short.MAX_VALUE)
                    .addComponent(jButton2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 52, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jButton1, 0, 0, Short.MAX_VALUE)
                    .addComponent(labeledCombobox1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(labeledSpinner1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(value, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(taxrate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(measure, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(link, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(11, 11, 11))
        );

        add(jPanel1, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void statech() {
        try {
            Product p = (Product) Product.getObject(Context.getProducts(), Integer.valueOf(labeledCombobox1.getSelectedItem().getId()));
            measure.setText(p.__getMeasure());
            value.setText(p.__getExternalnetvalue());
            taxrate.setSelectedItem(p.__getTaxids());
            link.setText(p.__getUrl());
        } catch (NodataFoundException ex) {
            measure.setText("");
            value.setText("");
            taxrate.setSelectedItem(0);
        }
    }
    
    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        SubItem s;
        try {
            s = new SubItem((Product) Product.getObject(Context.getProducts(), Integer.valueOf(labeledCombobox1.getSelectedItem().getId())));
        } catch (Exception ex) {
            s = SubItem.getDefaultItem();
            s.setDescription(labeledCombobox1.getText());
        }
        s.setCountvalue(row);
        s.setQuantityvalue(Integer.valueOf(labeledSpinner1.get_Value().toString()));
        s.setLinkurl(link.getText());

        if (measure.getText().length() > 0) {
            s.setMeasure(measure.getText());
            s.setExternalvalue(Double.valueOf(value.getText()));
            try {
                s.setTaxpercentvalue(Item.getTaxValue(Integer.valueOf(taxrate.getSelectedItem().getId())));
            } catch (Exception numberFormatException) {
            }
        }
        model.setRowAt(s.getRowData(row), row, 1);

        frame.setVisible(false);
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        frame.setVisible(false);
    }//GEN-LAST:event_jButton2ActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JPanel jPanel1;
    private mpv5.ui.beans.LabeledCombobox labeledCombobox1;
    private mpv5.ui.beans.LabeledSpinner labeledSpinner1;
    private mpv5.ui.beans.LabeledTextField link;
    private mpv5.ui.beans.LabeledTextField measure;
    private mpv5.ui.beans.LabeledCombobox taxrate;
    private mpv5.ui.beans.LabeledTextField value;
    // End of variables declaration//GEN-END:variables

    private void setModel(MPTableModel mode) {
        model = mode;
    }

    private void setEvent(MouseEvent evt) {
        event = evt;
    }

    private void setRow(int row) {
        this.row = row;
    }

    private void setProductid(int productid) {
        this.pid = productid;
    }
}
