/*
 * SearchPanel.java
 *
 * Created on Nov 30, 2008, 6:16:09 PM
 */
package mpv5.ui.dialogs.subcomponents;

import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.util.Properties;
import mpv5.data.PropertyStore;
import mpv5.globals.LocalSettings;
import mpv5.ui.dialogs.ControlApplet;
import mpv5.utils.text.TypeConversion;
import mpv5.utils.ui.PanelUtils;

/**
 *
 * @author anti43
 */
public class ControlPanel_Internet extends javax.swing.JPanel implements ControlApplet {

    /**
     * This unique name identifies this control applet
     */
    public final String UNAME = "internet";
    private PropertyStore oldvalues;

    public ControlPanel_Internet() {
        initComponents();
        setVisible(true);
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        proxy = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        port = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        auth = new javax.swing.JCheckBox();
        authpanel = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        user = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        password = new javax.swing.JPasswordField();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();

        setBackground(new java.awt.Color(255, 255, 255));
        setName("Form"); // NOI18N
        setLayout(new java.awt.BorderLayout());

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("mpv5/resources/languages/Panels"); // NOI18N
        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("ControlPanel_Internet.jPanel1.border.title"))); // NOI18N
        jPanel1.setName("jPanel1"); // NOI18N

        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 11));
        jLabel1.setText(bundle.getString("ControlPanel_Internet.jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        proxy.setText(bundle.getString("ControlPanel_Internet.proxy.text")); // NOI18N
        proxy.setName("proxy"); // NOI18N

        jLabel2.setText(bundle.getString("ControlPanel_Internet.jLabel2.text")); // NOI18N
        jLabel2.setName("jLabel2"); // NOI18N

        port.setText(bundle.getString("ControlPanel_Internet.port.text")); // NOI18N
        port.setName("port"); // NOI18N

        jLabel3.setText(bundle.getString("ControlPanel_Internet.jLabel3.text")); // NOI18N
        jLabel3.setName("jLabel3"); // NOI18N

        auth.setBackground(new java.awt.Color(255, 255, 255));
        auth.setText(bundle.getString("ControlPanel_Internet.auth.text")); // NOI18N
        auth.setName("auth"); // NOI18N
        auth.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                authItemStateChanged(evt);
            }
        });

        authpanel.setBackground(new java.awt.Color(255, 255, 255));
        authpanel.setName("authpanel"); // NOI18N

        jLabel5.setText(bundle.getString("ControlPanel_Internet.jLabel5.text")); // NOI18N
        jLabel5.setEnabled(false);
        jLabel5.setName("jLabel5"); // NOI18N

        user.setText(bundle.getString("ControlPanel_Internet.user.text")); // NOI18N
        user.setEnabled(false);
        user.setName("user"); // NOI18N

        jLabel4.setText(bundle.getString("ControlPanel_Internet.jLabel4.text")); // NOI18N
        jLabel4.setEnabled(false);
        jLabel4.setName("jLabel4"); // NOI18N

        password.setText(bundle.getString("ControlPanel_Internet.password.text")); // NOI18N
        password.setEnabled(false);
        password.setName("password"); // NOI18N

        javax.swing.GroupLayout authpanelLayout = new javax.swing.GroupLayout(authpanel);
        authpanel.setLayout(authpanelLayout);
        authpanelLayout.setHorizontalGroup(
            authpanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(authpanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(authpanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(authpanelLayout.createSequentialGroup()
                        .addComponent(jLabel5)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 42, Short.MAX_VALUE)
                        .addComponent(user, javax.swing.GroupLayout.PREFERRED_SIZE, 147, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(authpanelLayout.createSequentialGroup()
                        .addComponent(jLabel4)
                        .addGap(18, 18, 18)
                        .addComponent(password, javax.swing.GroupLayout.DEFAULT_SIZE, 147, Short.MAX_VALUE)))
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
                .addContainerGap(43, Short.MAX_VALUE))
        );

        jButton1.setText(bundle.getString("ControlPanel_Internet.jButton1.text")); // NOI18N
        jButton1.setName("jButton1"); // NOI18N
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton2.setText(bundle.getString("ControlPanel_Internet.jButton2.text")); // NOI18N
        jButton2.setName("jButton2"); // NOI18N
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(auth)
                    .addComponent(authpanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel2)
                            .addComponent(jLabel3))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(proxy)
                            .addComponent(port, javax.swing.GroupLayout.DEFAULT_SIZE, 157, Short.MAX_VALUE))))
                .addContainerGap(143, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(252, Short.MAX_VALUE)
                .addComponent(jButton2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton1)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addGap(12, 12, 12)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel2)
                        .addComponent(proxy, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(26, 26, 26)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel3)
                            .addComponent(port, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(13, 13, 13)
                .addComponent(auth)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(authpanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 14, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton1)
                    .addComponent(jButton2))
                .addContainerGap())
        );

        add(jPanel1, java.awt.BorderLayout.CENTER);
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

    public PropertyStore getValues() {
        password.setSelectionStart(0);
        return new PropertyStore(new String[][]{
                    {"proxy", proxy.getText()},
                    {"port", port.getText()},
                    {"authenticated", TypeConversion.booleanToString(auth.isSelected())},
                    {"username", user.getText()},
                    {"password", password.getSelectedText().toString()}
                });
    }

    public void setValues(PropertyStore values) {
        oldvalues = values;
        proxy.setText(values.getProperty("proxy"));
        port.setText(values.getProperty("proxy"));
        auth.setSelected(TypeConversion.stringToBoolean(values.getProperty("authenticated")));
        user.setText(values.getProperty("username"));
        password.setText(values.getProperty("password"));
    }

    public String getUname() {
        return UNAME;
    }

    public void reset() {
        setValues(oldvalues);
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox auth;
    private javax.swing.JPanel authpanel;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPasswordField password;
    private javax.swing.JTextField port;
    private javax.swing.JTextField proxy;
    private javax.swing.JTextField user;
    // End of variables declaration//GEN-END:variables

    private void setSettings() {
        LocalSettings.setProperty(LocalSettings.PROXYHOST, proxy.getText());
        LocalSettings.setProperty(LocalSettings.PROXYPORT, port.getText());

        if (auth.isSelected()) {
            LocalSettings.setProperty(LocalSettings.PROXYUSER, user.getText());
            LocalSettings.setProperty(LocalSettings.PROXYPASSWORD, new String(password.getPassword()));
        }

        LocalSettings.apply();
    }
}
