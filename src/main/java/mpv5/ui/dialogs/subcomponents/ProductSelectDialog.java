package mpv5.ui.dialogs.subcomponents;

import java.awt.BorderLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import javax.swing.JFrame;
import mpv5.db.common.Context;
import mpv5.db.common.NodataFoundException;
import mpv5.db.objects.Product;
import mpv5.db.objects.SubItem;
import mpv5.db.objects.Tax;
import mpv5.globals.Messages;
import mpv5.utils.models.MPTableModel;
import mpv5.utils.numberformat.FormatNumber;

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
     * @param link
     * @param optional
     * @return
     */
    public static ProductSelectDialog instanceOf(MPTableModel model, int row, MouseEvent evt, int productid, Object link, Object optional) {
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
        if (optional != null) {
            me.setOptional(optional.toString());
        }
        if (link != null) {
            me.setLink(link.toString());
        }
        if (productid > 0) {
            try {
                me.labeledCombobox1.setSelectedItem(Integer.valueOf(productid));
                me.statech();
            } catch (Exception e) {
            }
        }
        me.taxrate.setEnabled(!mpv5.db.objects.User.getCurrentUser().getProperties().getProperty("org.openyabs.uiproperty", "hidetaxcolumn"));
//        Point p = evt.getLocationOnScreen();
//                  p.move(evt.getLocationOnScreen().x - me.getWidth(),
//                        mpv5.YabsViewProxy.instance().identifierFrame.getY() + mpv5.YabsViewProxy.instance().identifierFrame.getHeight() - 310);
//                  frame.setLocation(p);
        frame.setLocation(evt.getLocationOnScreen().x - me.getWidth(), evt.getLocationOnScreen().y);
        frame.setResizable(false);
        frame.setVisible(true);
        return me;
    }
    private MPTableModel model;
    private MouseEvent event;
    private int row;
    private int pid;
    private String optionalt;
    private String linkt;

    public ProductSelectDialog() {
        initComponents();
        jButton1.getInsets().set(0, 0, 0, 0);
        jButton2.getInsets().set(0, 0, 0, 0);
        validate();
        link.getTextField().setToolTipText("Google@http://www.google.de");
        taxrate.getComboBox().setEditable(false);
        taxrate.setSearchEnabled(true);
        taxrate.setContext(Context.getTaxes());
        taxrate.triggerSearch();
        value.set_ValueClass(Double.class);
        countt.set_ValueClass(Double.class);
        labeledCombobox1.setContext(Context.getProduct());
        labeledCombobox1.getComboBox().addItemListener(new ItemListener() {

            public void itemStateChanged(ItemEvent e) {
                statech();
            }
        });
        labeledCombobox1.triggerSearch();
        labeledCombobox1.getComboBox().setEditable(true);
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        labeledCombobox1 = new mpv5.ui.beans.LabeledCombobox();
        jButton1 = new javax.swing.JButton();
        measure = new mpv5.ui.beans.LabeledTextField();
        taxrate = new mpv5.ui.beans.LabeledCombobox();
        value = new mpv5.ui.beans.LabeledTextField();
        jButton2 = new javax.swing.JButton();
        link = new mpv5.ui.beans.LabeledTextField();
        countt = new mpv5.ui.beans.LabeledTextField();
        opt = new mpv5.ui.beans.LabeledTextField();

        //\$2java.awt.Color(255, 255, 255));
        setName("Form"); // NOI18N
        setLayout(new java.awt.BorderLayout());

        java.util.ResourceBundle bundle = mpv5.i18n.LanguageManager.getBundle(); // NOI18N
        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("ProductSelectDialog.jPanel1.border.title"))); // NOI18N
        jPanel1.setName("jPanel1"); // NOI18N
        jPanel1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                jPanel1KeyPressed(evt);
            }
        });

        labeledCombobox1.set_Label(bundle.getString("ProductSelectDialog.labeledCombobox1._Label")); // NOI18N
        labeledCombobox1.setName("labeledCombobox1"); // NOI18N
        labeledCombobox1.setSearchOnEnterEnabled(true);

        jButton1.setFont(new java.awt.Font("Dialog", 0, 10));
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

        jButton2.setFont(new java.awt.Font("Dialog", 0, 10));
        jButton2.setText(bundle.getString("ProductSelectDialog.jButton2.text")); // NOI18N
        jButton2.setName("jButton2"); // NOI18N
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        link.set_Label(bundle.getString("ProductSelectDialog.link._Label")); // NOI18N
        link.setName("link"); // NOI18N

        countt.set_Label(bundle.getString("ProductSelectDialog.countt._Label")); // NOI18N
        countt.set_Text(bundle.getString("ProductSelectDialog.countt._Text")); // NOI18N
        countt.setName("countt"); // NOI18N

        opt.set_Label(bundle.getString("ProductSelectDialog.opt._Label")); // NOI18N
        opt.setName("opt"); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addComponent(countt, javax.swing.GroupLayout.DEFAULT_SIZE, 144, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(labeledCombobox1, javax.swing.GroupLayout.PREFERRED_SIZE, 326, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(measure, javax.swing.GroupLayout.DEFAULT_SIZE, 144, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(taxrate, javax.swing.GroupLayout.PREFERRED_SIZE, 176, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(value, javax.swing.GroupLayout.DEFAULT_SIZE, 168, Short.MAX_VALUE))
                    .addComponent(link, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(opt, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jButton1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButton2, javax.swing.GroupLayout.DEFAULT_SIZE, 75, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jButton1, 0, 0, Short.MAX_VALUE)
                    .addComponent(labeledCombobox1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(countt, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jButton2)
                    .addComponent(value, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(taxrate, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(measure, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(link, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(opt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(37, 37, 37))
        );

        add(jPanel1, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void statech() {
        try {
            Product p = (Product) Product.getObject(Context.getProduct(), Integer.valueOf(labeledCombobox1.getSelectedItem().getId()));
            measure.setText(p.__getMeasure());
            value.setText(FormatNumber.formatDezimal(p.findPriceFor(countt.getValue(0d))));
            taxrate.setSelectedItem(p.__getTaxids());
            if (linkt == null || linkt.length() == 0) {
                link.setText(p.__getUrl());
            } else {
                link.setText(linkt);
            }
        } catch (NodataFoundException ex) {
            measure.setText("");
            value.setText("");
            taxrate.setSelectedItem(0);
        }
    }

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        SubItem s;
        try {
            s = new SubItem((Product) Product.getObject(Context.getProduct(), Integer.valueOf(labeledCombobox1.getSelectedItem().getId())));
        } catch (Exception ex) {
            s = SubItem.getDefaultItem();
            if (labeledCombobox1.getText() != null) {
                s.setDescription(labeledCombobox1.getText());
            }
        }
        s.setCountvalue(new BigDecimal(row));
        BigDecimal f = FormatNumber.parseDezimal(countt.getText());
        s.setQuantityvalue(f != null ? f : BigDecimal.ONE);
        s.setLinkurl(link.getText());
        s.setCname(opt.getText());

        s.setMeasure(measure.getText());
        try {
            s.setExternalvalue(FormatNumber.parseDezimal(value.getText()));
        } catch (Exception e) {
        }
        try {
            s.setTaxpercentvalue(Tax.getTaxValue(Integer.valueOf(taxrate.getSelectedItem().getId())));
        } catch (Exception numberFormatException) {
        }

        model.setRowAt(s.getRowData(row), row, 1);
        frame.setVisible(false);
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        frame.setVisible(false);
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jPanel1KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jPanel1KeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ESCAPE) {
            frame.setVisible(false);
        }
    }//GEN-LAST:event_jPanel1KeyPressed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private mpv5.ui.beans.LabeledTextField countt;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JPanel jPanel1;
    private mpv5.ui.beans.LabeledCombobox labeledCombobox1;
    private mpv5.ui.beans.LabeledTextField link;
    private mpv5.ui.beans.LabeledTextField measure;
    private mpv5.ui.beans.LabeledTextField opt;
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

    private void setOptional(String optional) {
        this.optionalt = optional;
        opt.setText(optional);
    }

    private void setLink(String links) {
        this.linkt = links;
        link.setText(links);
    }
}
