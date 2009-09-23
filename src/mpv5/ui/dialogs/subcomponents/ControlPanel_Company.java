package mpv5.ui.dialogs.subcomponents;

import java.awt.Component;
import mpv5.data.PropertyStore;
import mpv5.db.common.QueryHandler;
import mpv5.globals.LocalSettings;
import mpv5.logging.Log;
import mpv5.ui.dialogs.ControlApplet;
import mpv5.utils.text.TypeConversion;
import mpv5.utils.ui.PanelUtils;

/**
 *
 * 
 */
public class ControlPanel_Company extends javax.swing.JPanel implements ControlApplet {

    private static final long serialVersionUID = 1L;
    /**
     * This unique name identifies this control applet
     */
    public final String UNAME = "ooo";
    private PropertyStore oldvalues;
    private static ControlPanel_Company ident;

    public ControlPanel_Company() {
        initComponents();
        setValues(null);
        setVisible(true);
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel2 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        labeledTextField17 = new mpv5.ui.beans.LabeledTextField();
        labeledTextField3 = new mpv5.ui.beans.LabeledTextField();
        labeledTextField4 = new mpv5.ui.beans.LabeledTextField();
        labeledTextField5 = new mpv5.ui.beans.LabeledTextField();
        labeledTextField6 = new mpv5.ui.beans.LabeledTextField();
        labeledTextField8 = new mpv5.ui.beans.LabeledTextField();
        labeledTextField7 = new mpv5.ui.beans.LabeledTextField();
        labeledTextField9 = new mpv5.ui.beans.LabeledTextField();
        labeledTextField10 = new mpv5.ui.beans.LabeledTextField();
        labeledTextField11 = new mpv5.ui.beans.LabeledTextField();
        labeledTextField12 = new mpv5.ui.beans.LabeledTextField();
        labeledTextField14 = new mpv5.ui.beans.LabeledTextField();
        labeledTextField16 = new mpv5.ui.beans.LabeledTextField();
        labeledTextField13 = new mpv5.ui.beans.LabeledTextField();
        labeledTextField18 = new mpv5.ui.beans.LabeledTextField();
        labeledTextField15 = new mpv5.ui.beans.LabeledTextField();
        jPanel1 = new javax.swing.JPanel();
        jButton1 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();

        setBackground(new java.awt.Color(255, 255, 255));
        setName("Form"); // NOI18N
        setLayout(new java.awt.BorderLayout());

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("mpv5/resources/languages/Panels"); // NOI18N
        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("ControlPanel_Company.jPanel2.border.title"))); // NOI18N
        jPanel2.setName("jPanel2"); // NOI18N

        jPanel3.setName("jPanel3"); // NOI18N
        jPanel3.setLayout(new javax.swing.BoxLayout(jPanel3, javax.swing.BoxLayout.PAGE_AXIS));

        labeledTextField17.set_Label(bundle.getString("ControlPanel_Company.labeledTextField17._Label")); // NOI18N
        labeledTextField17.setName("labeledTextField17"); // NOI18N
        jPanel3.add(labeledTextField17);

        labeledTextField3.set_Label(bundle.getString("ControlPanel_Company.labeledTextField3._Label")); // NOI18N
        labeledTextField3.setName("labeledTextField3"); // NOI18N
        jPanel3.add(labeledTextField3);

        labeledTextField4.set_Label(bundle.getString("ControlPanel_Company.labeledTextField4._Label")); // NOI18N
        labeledTextField4.setName("labeledTextField4"); // NOI18N
        jPanel3.add(labeledTextField4);

        labeledTextField5.set_Label(bundle.getString("ControlPanel_Company.labeledTextField5._Label")); // NOI18N
        labeledTextField5.setName("labeledTextField5"); // NOI18N
        jPanel3.add(labeledTextField5);

        labeledTextField6.set_Label(bundle.getString("ControlPanel_Company.labeledTextField6._Label")); // NOI18N
        labeledTextField6.setName("labeledTextField6"); // NOI18N
        jPanel3.add(labeledTextField6);

        labeledTextField8.set_Label(bundle.getString("ControlPanel_Company.labeledTextField8._Label")); // NOI18N
        labeledTextField8.setName("labeledTextField8"); // NOI18N
        jPanel3.add(labeledTextField8);

        labeledTextField7.set_Label(bundle.getString("ControlPanel_Company.labeledTextField7._Label")); // NOI18N
        labeledTextField7.setName("labeledTextField7"); // NOI18N
        jPanel3.add(labeledTextField7);

        labeledTextField9.set_Label(bundle.getString("ControlPanel_Company.labeledTextField9._Label")); // NOI18N
        labeledTextField9.setName("labeledTextField9"); // NOI18N
        jPanel3.add(labeledTextField9);

        labeledTextField10.set_Label(bundle.getString("ControlPanel_Company.labeledTextField10._Label")); // NOI18N
        labeledTextField10.setName("labeledTextField10"); // NOI18N
        jPanel3.add(labeledTextField10);

        labeledTextField11.set_Label(bundle.getString("ControlPanel_Company.labeledTextField11._Label")); // NOI18N
        labeledTextField11.setName("labeledTextField11"); // NOI18N
        jPanel3.add(labeledTextField11);

        labeledTextField12.set_Label(bundle.getString("ControlPanel_Company.labeledTextField12._Label")); // NOI18N
        labeledTextField12.setName("labeledTextField12"); // NOI18N
        jPanel3.add(labeledTextField12);

        labeledTextField14.set_Label(bundle.getString("ControlPanel_Company.labeledTextField14._Label")); // NOI18N
        labeledTextField14.setName("labeledTextField14"); // NOI18N
        jPanel3.add(labeledTextField14);

        labeledTextField16.set_Label(bundle.getString("ControlPanel_Company.labeledTextField16._Label")); // NOI18N
        labeledTextField16.setName("labeledTextField16"); // NOI18N
        jPanel3.add(labeledTextField16);

        labeledTextField13.set_Label(bundle.getString("ControlPanel_Company.labeledTextField13._Label")); // NOI18N
        labeledTextField13.setName("labeledTextField13"); // NOI18N
        jPanel3.add(labeledTextField13);

        labeledTextField18.set_Label(bundle.getString("ControlPanel_Company.labeledTextField18._Label")); // NOI18N
        labeledTextField18.setName("labeledTextField18"); // NOI18N
        jPanel3.add(labeledTextField18);

        labeledTextField15.set_Label(bundle.getString("ControlPanel_Company.labeledTextField15._Label")); // NOI18N
        labeledTextField15.setName("labeledTextField15"); // NOI18N
        jPanel3.add(labeledTextField15);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, 262, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(66, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, 333, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        add(jPanel2, java.awt.BorderLayout.CENTER);

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setName("jPanel1"); // NOI18N
        jPanel1.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        jButton1.setText(bundle.getString("ControlPanel_Company.jButton1.text")); // NOI18N
        jButton1.setName("jButton1"); // NOI18N
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        jPanel1.add(jButton1);

        jButton3.setText(bundle.getString("ControlPanel_Company.jButton3.text")); // NOI18N
        jButton3.setName("jButton3"); // NOI18N
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });
        jPanel1.add(jButton3);

        add(jPanel1, java.awt.BorderLayout.PAGE_END);
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed

        setSettings();
        LocalSettings.save();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        reset();
    }//GEN-LAST:event_jButton3ActionPerformed

    public void setValues(PropertyStore values) {
        oldvalues = values;
        try {
 
        } catch (Exception e) {
            Log.Debug(this, e);
        }
    }

    public String getUname() {
        return UNAME;
    }

    public void reset() {
        setValues(oldvalues);
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private mpv5.ui.beans.LabeledTextField labeledTextField10;
    private mpv5.ui.beans.LabeledTextField labeledTextField11;
    private mpv5.ui.beans.LabeledTextField labeledTextField12;
    private mpv5.ui.beans.LabeledTextField labeledTextField13;
    private mpv5.ui.beans.LabeledTextField labeledTextField14;
    private mpv5.ui.beans.LabeledTextField labeledTextField15;
    private mpv5.ui.beans.LabeledTextField labeledTextField16;
    private mpv5.ui.beans.LabeledTextField labeledTextField17;
    private mpv5.ui.beans.LabeledTextField labeledTextField18;
    private mpv5.ui.beans.LabeledTextField labeledTextField3;
    private mpv5.ui.beans.LabeledTextField labeledTextField4;
    private mpv5.ui.beans.LabeledTextField labeledTextField5;
    private mpv5.ui.beans.LabeledTextField labeledTextField6;
    private mpv5.ui.beans.LabeledTextField labeledTextField7;
    private mpv5.ui.beans.LabeledTextField labeledTextField8;
    private mpv5.ui.beans.LabeledTextField labeledTextField9;
    // End of variables declaration//GEN-END:variables

    private void setSettings() {
        
    }

    @Override
    public Component getAndRemoveActionPanel() {
        this.remove(jPanel1);
        validate();
        return jPanel1;
    }
}
