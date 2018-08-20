package mpv5.ui.dialogs.subcomponents;

import ag.ion.bion.officelayer.application.IApplicationAssistant;
import ag.ion.bion.officelayer.application.ILazyApplicationInfo;
import ag.ion.bion.officelayer.application.OfficeApplicationException;
import ag.ion.bion.officelayer.internal.application.ApplicationAssistant;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import mpv5.Main;
import mpv5.YabsViewProxy;
import mpv5.data.PropertyStore;
import mpv5.globals.LocalSettings;
import mpv5.globals.Messages;
import mpv5.logging.Log;
import mpv5.ui.dialogs.ControlApplet;
import mpv5.ui.dialogs.Popup;

/**
 *
 *
 */
public class ControlPanel_External extends javax.swing.JPanel implements ControlApplet {

    private static final long serialVersionUID = 1L;
    /**
     * This unique name identifies this control applet
     */
    public final String UNAME = "ooo";
    private PropertyStore oldvalues;
    private static ControlPanel_External ident;

    public ControlPanel_External() {
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
        jButton5 = new javax.swing.JButton();
        jCheckBox2 = new javax.swing.JCheckBox();
        auto = new javax.swing.JCheckBox();
        alternate = new javax.swing.JCheckBox();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        labeledTextField3 = new mpv5.ui.beans.LabeledTextField();
        jPanel1 = new javax.swing.JPanel();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();

        setBackground(new java.awt.Color(255, 255, 255));
        setName("Form"); // NOI18N
        setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.Y_AXIS));

        java.util.ResourceBundle bundle = mpv5.i18n.LanguageManager.getBundle(); // NOI18N
        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("ControlPanel_External.jPanel2.border.title"))); // NOI18N
        jPanel2.setName("jPanel2"); // NOI18N

        labeledTextChooser2.set_Label(bundle.getString("ControlPanel_External.labeledTextChooser2._Label")); // NOI18N
        labeledTextChooser2.setName("labeledTextChooser2"); // NOI18N

        jCheckBox3.setText(bundle.getString("ControlPanel_External.jCheckBox3.text")); // NOI18N
        jCheckBox3.setName("jCheckBox3"); // NOI18N
        jCheckBox3.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jCheckBox3ItemStateChanged(evt);
            }
        });

        labeledTextField1.set_Label(bundle.getString("ControlPanel_External.labeledTextField1._Label")); // NOI18N
        labeledTextField1.set_Text(bundle.getString("ControlPanel_External.labeledTextField1._Text")); // NOI18N
        labeledTextField1.setEnabled(false);
        labeledTextField1.setName("labeledTextField1"); // NOI18N

        labeledTextField2.set_Label(bundle.getString("ControlPanel_External.labeledTextField2._Label")); // NOI18N
        labeledTextField2.setEnabled(false);
        labeledTextField2.setName("labeledTextField2"); // NOI18N

        jLabel3.setText(bundle.getString("ControlPanel_External.jLabel3.text")); // NOI18N
        jLabel3.setName("jLabel3"); // NOI18N

        jButton5.setText(bundle.getString("ControlPanel_External.jButton5.text")); // NOI18N
        jButton5.setName(bundle.getString("ControlPanel_External.jButton5.name")); // NOI18N
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton5ActionPerformed(evt);
            }
        });

        jCheckBox2.setText(bundle.getString("ControlPanel_External.jCheckBox2.text")); // NOI18N
        jCheckBox2.setName("jCheckBox2"); // NOI18N

        auto.setText(bundle.getString("ControlPanel_External.auto.text")); // NOI18N
        auto.setName("auto"); // NOI18N
        auto.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                autoItemStateChanged(evt);
            }
        });
        auto.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                autoActionPerformed(evt);
            }
        });

        alternate.setSelected(true);
        alternate.setText(bundle.getString("ControlPanel_External.alternate.text")); // NOI18N
        alternate.setName("alternate"); // NOI18N
        alternate.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                alternateItemStateChanged(evt);
            }
        });

        jLabel4.setText(bundle.getString("ControlPanel_External.jLabel4.text")); // NOI18N
        jLabel4.setName("jLabel4"); // NOI18N

        jLabel5.setText(bundle.getString("ControlPanel_External.jLabel5.text")); // NOI18N
        jLabel5.setName("jLabel5"); // NOI18N

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(jCheckBox3, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(labeledTextField1, javax.swing.GroupLayout.DEFAULT_SIZE, 407, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(labeledTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, 119, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(labeledTextChooser2, javax.swing.GroupLayout.DEFAULT_SIZE, 565, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButton5))
                            .addComponent(jCheckBox2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 668, Short.MAX_VALUE)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(jLabel3)
                                .addGap(0, 648, Short.MAX_VALUE))
                            .addComponent(auto, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addContainerGap())
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel5)
                            .addComponent(jLabel4))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(alternate, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(4, 4, 4)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jButton5, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addComponent(labeledTextChooser2, javax.swing.GroupLayout.DEFAULT_SIZE, 24, Short.MAX_VALUE))
                .addGap(7, 7, 7)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jCheckBox3)
                    .addComponent(labeledTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(labeledTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBox2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(auto)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(alternate)
                .addContainerGap(77, Short.MAX_VALUE))
        );

        add(jPanel2);

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("ControlPanel_External.jPanel3.border.title"))); // NOI18N
        jPanel3.setName("jPanel3"); // NOI18N

        labeledTextField3.set_Label(bundle.getString("ControlPanel_External.labeledTextField3._Label")); // NOI18N
        labeledTextField3.setName("labeledTextField3"); // NOI18N

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(labeledTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, 293, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(381, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(labeledTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(67, Short.MAX_VALUE))
        );

        add(jPanel3);

        jPanel1.setName("jPanel1"); // NOI18N
        jPanel1.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        jButton1.setText(bundle.getString("ControlPanel_External.jButton1.text")); // NOI18N
        jButton1.setName("jButton1"); // NOI18N
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        jPanel1.add(jButton1);

        jButton2.setText(bundle.getString("ControlPanel_External.jButton2.text")); // NOI18N
        jButton2.setName("jButton2"); // NOI18N
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });
        jPanel1.add(jButton2);

        jButton3.setText(bundle.getString("ControlPanel_External.jButton3.text")); // NOI18N
        jButton3.setName("jButton3"); // NOI18N
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });
        jPanel1.add(jButton3);

        add(jPanel1);
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed

        setSettings();
        LocalSettings.save();
        Popup.notice(Messages.RESTART_REQUIRED);
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        reset();
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed

        setSettings();
        Popup.notice(Messages.RESTART_REQUIRED);
}//GEN-LAST:event_jButton2ActionPerformed

    private void jCheckBox3ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jCheckBox3ItemStateChanged
        labeledTextField1.setEnabled(jCheckBox3.isSelected());
        labeledTextField2.setEnabled(jCheckBox3.isSelected());
        labeledTextChooser2.setEnabled(!jCheckBox3.isSelected());
}//GEN-LAST:event_jCheckBox3ItemStateChanged

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed

        Runnable runnable = new Runnable() {
            public void run() {
                try {

                    YabsViewProxy.instance().setWaiting(true);
                    IApplicationAssistant applicationAssistant = new ApplicationAssistant();
                    ILazyApplicationInfo appInfo = applicationAssistant.getLatestLocalLibreOfficeApplication();
                    if (appInfo == null) {
                        appInfo = applicationAssistant.getLatestLocalOpenOfficeOrgApplication();
                    }
                    if (appInfo != null) {
                        labeledTextChooser2.setText(appInfo.getHome());
                    }
                    YabsViewProxy.instance().setWaiting(false);
                } catch (OfficeApplicationException ex) {
                    Log.Debug(ex);
                }
            }
        };
        SwingUtilities.invokeLater(runnable);
    }//GEN-LAST:event_jButton5ActionPerformed

   private void autoItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_autoItemStateChanged
       LocalSettings.setProperty(LocalSettings.OFFICE_AUTOSTART, String.valueOf(auto.isSelected()));
       LocalSettings.save();
   }//GEN-LAST:event_autoItemStateChanged

   private void autoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_autoActionPerformed
       // TODO add your handling code here:
   }//GEN-LAST:event_autoActionPerformed

    private void alternateItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_alternateItemStateChanged
        if (alternate.isSelected()) {
            jCheckBox2.setSelected(false);
            auto.setSelected(false);
        }
    }//GEN-LAST:event_alternateItemStateChanged

    public void setValues(PropertyStore values) {
        oldvalues = values;
        try {
            labeledTextChooser2.set_Text(LocalSettings.getProperty(LocalSettings.OFFICE_HOME));
            jCheckBox3.setSelected(LocalSettings.getBooleanProperty(LocalSettings.OFFICE_REMOTE));
            alternate.setSelected(LocalSettings.getBooleanProperty(LocalSettings.OFFICE_ALT));
            jCheckBox2.setSelected(LocalSettings.getBooleanProperty(LocalSettings.OFFICE_LOCALSERVER));
            labeledTextField1.setText(LocalSettings.getProperty(LocalSettings.OFFICE_HOST));
            labeledTextField2.setText(LocalSettings.getProperty(LocalSettings.OFFICE_PORT));
            labeledTextField3.setText(LocalSettings.getProperty(LocalSettings.CALCULATOR));
            auto.setSelected(LocalSettings.getBooleanProperty(LocalSettings.OFFICE_AUTOSTART));
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
    private javax.swing.JCheckBox alternate;
    private javax.swing.JCheckBox auto;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton5;
    private javax.swing.JCheckBox jCheckBox2;
    private javax.swing.JCheckBox jCheckBox3;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private mpv5.ui.beans.LabeledTextChooser labeledTextChooser2;
    private mpv5.ui.beans.LabeledTextField labeledTextField1;
    private mpv5.ui.beans.LabeledTextField labeledTextField2;
    private mpv5.ui.beans.LabeledTextField labeledTextField3;
    // End of variables declaration//GEN-END:variables

    private void setSettings() {
        File f = new File(labeledTextChooser2.get_Text(false));
        if (!jCheckBox3.isSelected() && !f.isDirectory()) {
            try {
                labeledTextChooser2.setText(f.getParentFile().getCanonicalPath());
            } catch (Exception ex) {
                Log.Debug(this, ex.getMessage());
                labeledTextChooser2.setText("");
            }
        }

        LocalSettings.setProperty(LocalSettings.OFFICE_HOME, labeledTextChooser2.get_Text(false));
        LocalSettings.setProperty(LocalSettings.OFFICE_LOCALSERVER, Boolean.toString(jCheckBox2.isSelected()));
        LocalSettings.setProperty(LocalSettings.OFFICE_USE, Boolean.toString(!alternate.isSelected()));
        LocalSettings.setProperty(LocalSettings.OFFICE_ALT, Boolean.toString(alternate.isSelected()));
        LocalSettings.setProperty(LocalSettings.OFFICE_REMOTE, Boolean.toString(jCheckBox3.isSelected()));
        LocalSettings.setProperty(LocalSettings.OFFICE_HOST, labeledTextField1.getText());
        LocalSettings.setProperty(LocalSettings.OFFICE_PORT, labeledTextField2.getText());
        LocalSettings.setProperty(LocalSettings.CALCULATOR, labeledTextField3.getText());
        LocalSettings.setProperty(LocalSettings.OFFICE_AUTOSTART, String.valueOf(auto.isSelected()));
        checkOS();

        if (jCheckBox2.isSelected()) {
            //turn off autostart if local server
            LocalSettings.setProperty(LocalSettings.OFFICE_AUTOSTART, "false");
            LocalSettings.setProperty(LocalSettings.OFFICE_HOST, "127.0.0.1");
            LocalSettings.setProperty(LocalSettings.OFFICE_PORT, "8100");
        }

        LocalSettings.apply();
    }

    private void checkOS() {
        if (Main.osIsMacOsX) {
            LocalSettings.setProperty(LocalSettings.OFFICE_BINARY_FOLDER, "MacOS");
        } else if (Main.osIsWindows) {
            LocalSettings.setProperty(LocalSettings.OFFICE_BINARY_FOLDER, "program");
        } else if (Main.osIsLinux) {
            LocalSettings.setProperty(LocalSettings.OFFICE_BINARY_FOLDER, "program");
        }
    }

    @Override
    public Component getAndRemoveActionPanel() {
        this.remove(jPanel1);
        validate();
        return jPanel1;
    }
}
