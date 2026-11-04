package mpv5.ui.dialogs.subcomponents;

import java.awt.Component;
import mpv5.data.PropertyStore;
import mpv5.db.common.QueryHandler;
import mpv5.globals.LocalSettings;
import mpv5.logging.Log;
import mpv5.ui.dialogs.ControlApplet;
import mpv5.ui.panels.MPControlPanel;
import mpv5.utils.text.TypeConversion;
import mpv5.utils.ui.PanelUtils;

/**
 *
 *
 */
public class ControlPanel_Local extends javax.swing.JPanel implements ControlApplet {

    private static final long serialVersionUID = 1L;
    /**
     * This unique name identifies this control applet
     */
    public final String UNAME = "localsettings";
    private PropertyStore oldvalues;

    public ControlPanel_Local() {
        initComponents();
        setVisible(true);
        setValues(LocalSettings.getPropertyStore());
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        proxy = new javax.swing.JTextField();
        port = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        auth = new javax.swing.JCheckBox();
        authpanel = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        user = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        password = new javax.swing.JPasswordField();
        jPanel3 = new javax.swing.JPanel();
        printdev = new javax.swing.JCheckBox();
        jPanel4 = new javax.swing.JPanel();
        autolock = new javax.swing.JCheckBox();
        escape = new javax.swing.JCheckBox();
        jPanel5 = new javax.swing.JPanel();
        backupbasedir = new mpv5.ui.beans.LabeledTextChooser(false);
        savebasedir = new mpv5.ui.beans.LabeledTextChooser(false);
        jPanel1 = new javax.swing.JPanel();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();

        //\$2java.awt.Color(255, 255, 255));
        setMaximumSize(new java.awt.Dimension(891, 889));
        setName("Form"); // NOI18N
        setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.PAGE_AXIS));

        java.util.ResourceBundle bundle = mpv5.i18n.LanguageManager.getBundle(); // NOI18N
        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("ControlPanel_Local.jPanel2.border.title"))); // NOI18N
        jPanel2.setName("jPanel2"); // NOI18N
        jPanel2.setPreferredSize(new java.awt.Dimension(670, 180));

        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel1.setText(bundle.getString("ControlPanel_Local.jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        jLabel2.setText(bundle.getString("ControlPanel_Local.jLabel2.text")); // NOI18N
        jLabel2.setName("jLabel2"); // NOI18N

        proxy.setName("proxy"); // NOI18N

        port.setName("port"); // NOI18N

        jLabel3.setText(bundle.getString("ControlPanel_Local.jLabel3.text")); // NOI18N
        jLabel3.setName("jLabel3"); // NOI18N

        auth.setText(bundle.getString("ControlPanel_Local.auth.text")); // NOI18N
        auth.setName("auth"); // NOI18N
        auth.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                authItemStateChanged(evt);
            }
        });

//$2java.awt.Color(255, 255, 255));
        authpanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        authpanel.setName("authpanel"); // NOI18N

        jLabel5.setText(bundle.getString("ControlPanel_Local.jLabel5.text")); // NOI18N
        jLabel5.setEnabled(false);
        jLabel5.setName("jLabel5"); // NOI18N

        user.setEnabled(false);
        user.setName("user"); // NOI18N

        jLabel4.setText(bundle.getString("ControlPanel_Local.jLabel4.text")); // NOI18N
        jLabel4.setEnabled(false);
        jLabel4.setName("jLabel4"); // NOI18N

        password.setText(bundle.getString("ControlPanel_Local.password.text")); // NOI18N
        password.setEnabled(false);
        password.setName("password"); // NOI18N

        javax.swing.GroupLayout authpanelLayout = new javax.swing.GroupLayout(authpanel);
        authpanel.setLayout(authpanelLayout);
        authpanelLayout.setHorizontalGroup(
            authpanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(authpanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(authpanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel4)
                    .addComponent(jLabel5))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(authpanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(user)
                    .addComponent(password, javax.swing.GroupLayout.DEFAULT_SIZE, 292, Short.MAX_VALUE))
                .addContainerGap())
        );
        authpanelLayout.setVerticalGroup(
            authpanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(authpanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(authpanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(user, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(authpanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(password, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(auth, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(jLabel2)
                                .addGap(18, 18, 18)
                                .addComponent(proxy, javax.swing.GroupLayout.PREFERRED_SIZE, 240, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 69, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(port, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jLabel1)
                            .addComponent(authpanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 220, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addComponent(jLabel1)
                .addGap(12, 12, 12)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(proxy, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3)
                    .addComponent(port, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(auth)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(authpanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(42, 42, 42))
        );

        add(jPanel2);

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("ControlPanel_Local.jPanel3.border.title"))); // NOI18N
        jPanel3.setName("jPanel3"); // NOI18N

        printdev.setText(bundle.getString("ControlPanel_Local.printdev.text")); // NOI18N
        printdev.setName("printdev"); // NOI18N

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(printdev, javax.swing.GroupLayout.DEFAULT_SIZE, 658, Short.MAX_VALUE)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addComponent(printdev)
                .addGap(0, 12, Short.MAX_VALUE))
        );

        add(jPanel3);

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("ControlPanel_Local.jPanel4.border.title"))); // NOI18N
        jPanel4.setName("jPanel4"); // NOI18N

        autolock.setText(bundle.getString("ControlPanel_Local.autolock.text")); // NOI18N
        autolock.setName("autolock"); // NOI18N

        escape.setText(bundle.getString("ControlPanel_Local.escape.text")); // NOI18N
        escape.setName("escape"); // NOI18N

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(autolock, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 652, Short.MAX_VALUE)
                    .addComponent(escape, javax.swing.GroupLayout.DEFAULT_SIZE, 652, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addComponent(autolock)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(escape)
                .addGap(0, 10, Short.MAX_VALUE))
        );

        add(jPanel4);

        jPanel5.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("ControlPanel_Local.jPanel5.border.title"))); // NOI18N
        jPanel5.setName("jPanel5"); // NOI18N

        backupbasedir.setLabelWidth(200);
        backupbasedir.doInitComponents();
        backupbasedir.set_Label(bundle.getString("ControlPanel_Local.backupbasedir._Label")); // NOI18N
        backupbasedir.setLabelWidth(200);
        backupbasedir.setName("backupbasedir"); // NOI18N

        savebasedir.setLabelWidth(200);
        savebasedir.doInitComponents();
        savebasedir.set_Label(bundle.getString("ControlPanel_Local.savebasedir._Label")); // NOI18N
        savebasedir.setLabelWidth(200);
        savebasedir.setName("savebasedir"); // NOI18N

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel5Layout.createSequentialGroup()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(backupbasedir, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 648, Short.MAX_VALUE)
                    .addComponent(savebasedir, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addComponent(savebasedir, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(backupbasedir, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(21, Short.MAX_VALUE))
        );

        add(jPanel5);

//$2java.awt.Color(255, 255, 255));
        jPanel1.setName("jPanel1"); // NOI18N
        jPanel1.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        jButton1.setText(bundle.getString("ControlPanel_Local.jButton1.text")); // NOI18N
        jButton1.setName("jButton1"); // NOI18N
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        jPanel1.add(jButton1);

        jButton2.setText(bundle.getString("ControlPanel_Local.jButton2.text")); // NOI18N
        jButton2.setName("jButton2"); // NOI18N
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });
        jPanel1.add(jButton2);

        jButton3.setText(bundle.getString("ControlPanel_Local.jButton3.text")); // NOI18N
        jButton3.setName("jButton3"); // NOI18N
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });
        jPanel1.add(jButton3);

        jButton4.setText(bundle.getString("ControlPanel_Local.jButton4.text")); // NOI18N
        jButton4.setName("jButton4"); // NOI18N
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });
        jPanel1.add(jButton4);

        add(jPanel1);
    }// </editor-fold>//GEN-END:initComponents

    private void authItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_authItemStateChanged
        PanelUtils.enableSubComponents(authpanel, auth.isSelected());
}//GEN-LAST:event_authItemStateChanged

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

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed

        ControlPanel_AdvancedLocalProperties c = new ControlPanel_AdvancedLocalProperties();
        MPControlPanel.instanceOf().openDetails(c);
    }//GEN-LAST:event_jButton4ActionPerformed

    @Override
    public final void setValues(PropertyStore values) {
        oldvalues = values;
        proxy.setText(values.getProperty("proxy"));
        port.setText(values.getProperty("proxy"));
        auth.setSelected(TypeConversion.stringToBoolean(values.getProperty("useproxy")));
        user.setText(values.getProperty("username"));
        password.setText(values.getProperty("password"));
        printdev.setSelected(TypeConversion.stringToBoolean(values.getProperty("devappprint")));
        autolock.setSelected(TypeConversion.stringToBoolean(values.getProperty(LocalSettings.DBAUTOLOCK)));
        escape.setSelected(TypeConversion.stringToBoolean(values.getProperty(LocalSettings.DBESCAPE)));

        savebasedir.set_Text(values.getProperty(LocalSettings.BASE_DIR));
        backupbasedir.set_Text(values.getProperty(LocalSettings.BACKUP_DIR));
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
    private javax.swing.JCheckBox auth;
    private javax.swing.JPanel authpanel;
    private javax.swing.JCheckBox autolock;
    private mpv5.ui.beans.LabeledTextChooser backupbasedir;
    private javax.swing.JCheckBox escape;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPasswordField password;
    private javax.swing.JTextField port;
    private javax.swing.JCheckBox printdev;
    private javax.swing.JTextField proxy;
    private mpv5.ui.beans.LabeledTextChooser savebasedir;
    private javax.swing.JTextField user;
    // End of variables declaration//GEN-END:variables

    private void setSettings() {
        LocalSettings.setProperty(LocalSettings.PROXYHOST, proxy.getText());
        LocalSettings.setProperty(LocalSettings.PROXYPORT, port.getText());
        LocalSettings.setProperty(LocalSettings.DBESCAPE, TypeConversion.booleanToString(escape.isSelected()));

        if (auth.isSelected()) {
            LocalSettings.setProperty(LocalSettings.PROXYUSE, TypeConversion.booleanToString(true));
            LocalSettings.setProperty(LocalSettings.PROXYUSER, user.getText());
            LocalSettings.setProperty(LocalSettings.PROXYPASSWORD, new String(password.getPassword()));
        }

        LocalSettings.setProperty(LocalSettings.PRINT_DEVAPP, TypeConversion.booleanToString(printdev.isSelected()));
        LocalSettings.setProperty(LocalSettings.DBAUTOLOCK, TypeConversion.booleanToString(autolock.isSelected()));
        LocalSettings.setProperty(LocalSettings.BASE_DIR, savebasedir.get_Text(true));
        LocalSettings.setProperty(LocalSettings.BACKUP_DIR, backupbasedir.get_Text(true));

        LocalSettings.apply();
    }

    @Override
    public Component getAndRemoveActionPanel() {
        this.remove(jPanel1);
        validate();
        return jPanel1;
    }
}
