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
public class ControlPanel_OO extends javax.swing.JPanel implements ControlApplet {

    private static final long serialVersionUID = 1L;
    /**
     * This unique name identifies this control applet
     */
    public final String UNAME = "ooo";
    private PropertyStore oldvalues;
    private static ControlPanel_OO ident;

    public ControlPanel_OO() {
        initComponents();
        setValues(null);
        setVisible(true);
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel2 = new javax.swing.JPanel();
        labeledTextChooser2 = new mpv5.ui.beans.LabeledTextChooser();
        jCheckBox3 = new javax.swing.JCheckBox();
        labeledTextField1 = new mpv5.ui.beans.LabeledTextField();
        labeledTextField2 = new mpv5.ui.beans.LabeledTextField();
        jLabel3 = new javax.swing.JLabel();
        jCheckBox1 = new javax.swing.JCheckBox();
        jPanel1 = new javax.swing.JPanel();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();

        setBackground(new java.awt.Color(255, 255, 255));
        setName("Form"); // NOI18N
        setLayout(new java.awt.BorderLayout());

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("mpv5/resources/languages/Panels"); // NOI18N
        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("ControlPanel_OO.jPanel2.border.title"))); // NOI18N
        jPanel2.setName("jPanel2"); // NOI18N

        labeledTextChooser2.set_Label(bundle.getString("ControlPanel_OO.labeledTextChooser2._Label")); // NOI18N
        labeledTextChooser2.setName("labeledTextChooser2"); // NOI18N

        jCheckBox3.setText(bundle.getString("ControlPanel_OO.jCheckBox3.text")); // NOI18N
        jCheckBox3.setName("jCheckBox3"); // NOI18N
        jCheckBox3.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jCheckBox3ItemStateChanged(evt);
            }
        });

        labeledTextField1.set_Label(bundle.getString("ControlPanel_OO.labeledTextField1._Label")); // NOI18N
        labeledTextField1.set_Text(bundle.getString("ControlPanel_OO.labeledTextField1._Text")); // NOI18N
        labeledTextField1.setEnabled(false);
        labeledTextField1.setName("labeledTextField1"); // NOI18N

        labeledTextField2.set_Label(bundle.getString("ControlPanel_OO.labeledTextField2._Label")); // NOI18N
        labeledTextField2.setEnabled(false);
        labeledTextField2.setName("labeledTextField2"); // NOI18N

        jLabel3.setText(bundle.getString("ControlPanel_OO.jLabel3.text")); // NOI18N
        jLabel3.setName("jLabel3"); // NOI18N

        jCheckBox1.setText(bundle.getString("ControlPanel_OO.jCheckBox1.text")); // NOI18N
        jCheckBox1.setName("jCheckBox1"); // NOI18N

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jCheckBox3, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(labeledTextField1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(labeledTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, 119, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jCheckBox1, javax.swing.GroupLayout.DEFAULT_SIZE, 454, Short.MAX_VALUE)
                        .addContainerGap())
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(labeledTextChooser2, javax.swing.GroupLayout.DEFAULT_SIZE, 450, Short.MAX_VALUE)
                            .addComponent(jLabel3))
                        .addContainerGap())))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(labeledTextChooser2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jCheckBox3)
                    .addComponent(labeledTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(labeledTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 34, Short.MAX_VALUE)
                .addComponent(jCheckBox1)
                .addContainerGap())
        );

        add(jPanel2, java.awt.BorderLayout.CENTER);

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setName("jPanel1"); // NOI18N
        jPanel1.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        jButton1.setText(bundle.getString("ControlPanel_OO.jButton1.text")); // NOI18N
        jButton1.setName("jButton1"); // NOI18N
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        jPanel1.add(jButton1);

        jButton2.setText(bundle.getString("ControlPanel_OO.jButton2.text")); // NOI18N
        jButton2.setName("jButton2"); // NOI18N
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });
        jPanel1.add(jButton2);

        jButton3.setText(bundle.getString("ControlPanel_OO.jButton3.text")); // NOI18N
        jButton3.setName("jButton3"); // NOI18N
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });
        jPanel1.add(jButton3);

        add(jPanel1, java.awt.BorderLayout.PAGE_END);
    }// </editor-fold>//GEN-END:initComponents

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed

        setSettings();
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed

        setSettings();
        LocalSettings.save();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        reset();
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jCheckBox3ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jCheckBox3ItemStateChanged
        labeledTextField1.setEnabled(jCheckBox3.isSelected());
        labeledTextField2.setEnabled(jCheckBox3.isSelected());
        labeledTextChooser2.setEnabled(!jCheckBox3.isSelected());
}//GEN-LAST:event_jCheckBox3ItemStateChanged

    public void setValues(PropertyStore values) {
        oldvalues = values;
        try {
            labeledTextChooser2.set_Text(LocalSettings.getProperty(LocalSettings.OFFICE_HOME));
            jCheckBox3.setSelected(LocalSettings.getBooleanProperty(LocalSettings.OFFICE_REMOTE));
            jCheckBox1.setSelected(!LocalSettings.getBooleanProperty(LocalSettings.OFFICE_USE));
            labeledTextField1.setText(LocalSettings.getProperty(LocalSettings.OFFICE_HOST));
            labeledTextField2.setText(LocalSettings.getProperty(LocalSettings.OFFICE_PORT));
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
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JCheckBox jCheckBox3;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private mpv5.ui.beans.LabeledTextChooser labeledTextChooser2;
    private mpv5.ui.beans.LabeledTextField labeledTextField1;
    private mpv5.ui.beans.LabeledTextField labeledTextField2;
    // End of variables declaration//GEN-END:variables

    private void setSettings() {
        LocalSettings.setProperty(LocalSettings.OFFICE_HOME, labeledTextChooser2.get_Text(true));
        LocalSettings.setProperty(LocalSettings.OFFICE_USE, Boolean.toString(!jCheckBox1.isSelected()));
        LocalSettings.setProperty(LocalSettings.OFFICE_REMOTE, Boolean.toString(jCheckBox3.isSelected()));
        LocalSettings.setProperty(LocalSettings.OFFICE_HOST, labeledTextField1.getText());
        LocalSettings.setProperty(LocalSettings.OFFICE_PORT, labeledTextField2.getText());
        LocalSettings.apply();
    }

    @Override
    public Component getAndRemoveActionPanel() {
        this.remove(jPanel1);
        validate();
        return jPanel1;
    }
}
