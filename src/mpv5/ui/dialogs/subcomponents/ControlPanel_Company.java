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
        jPanel1 = new javax.swing.JPanel();
        jButton1 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();

        setBackground(new java.awt.Color(255, 255, 255));
        setName("Form"); // NOI18N
        setLayout(new java.awt.BorderLayout());

        java.util.ResourceBundle bundle = mpv5.i18n.LanguageManager.getBundle(); 
        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("ControlPanel_Company.jPanel2.border.title"))); // NOI18N
        jPanel2.setName("jPanel2"); // NOI18N

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 474, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 333, Short.MAX_VALUE)
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
