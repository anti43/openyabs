package mpv5.ui.dialogs.subcomponents;

import ag.ion.bion.officelayer.application.IApplicationAssistant;
import ag.ion.bion.officelayer.application.ILazyApplicationInfo;
import ag.ion.bion.officelayer.application.OfficeApplicationException;
import ag.ion.bion.officelayer.internal.application.ApplicationAssistant;

import java.awt.Cursor;
import java.io.File;
import java.util.List;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import mpv5.Main;
import mpv5.YabsViewProxy;
import mpv5.data.PropertyStore;
import mpv5.globals.LocalSettings;
import mpv5.globals.Messages;
import mpv5.logging.Log;
import mpv5.ui.dialogs.Popup;
import mpv5.ui.dialogs.WizardMaster;
import mpv5.ui.dialogs.Wizardable;

/**
 *
 *
 */
public class wizard_FirstSettings3 extends javax.swing.JPanel implements Wizardable {

   private static final long serialVersionUID = -8347532498124147821L;
   private WizardMaster master;
   private PropertyStore nactions;
   private List<JToolBar> nbars;

   public wizard_FirstSettings3(WizardMaster w) {
      this.master = w;
      initComponents();
      setModels();
      labeledTextChooser2.set_Text("");
      jCheckBox3.setSelected(false);

      labeledTextField1.setText("127.0.0.1");
      labeledTextField2.setText("8100");
   }

   // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
   private void initComponents() {

      jPanel1 = new javax.swing.JPanel();
      jPanel2 = new javax.swing.JPanel();
      jScrollPane1 = new javax.swing.JScrollPane();
      jTextPane1 = new javax.swing.JTextPane();
      jPanel3 = new javax.swing.JPanel();
      labeledTextChooser2 = new mpv5.ui.beans.LabeledTextChooser();
      jCheckBox3 = new javax.swing.JCheckBox();
      labeledTextField1 = new mpv5.ui.beans.LabeledTextField();
      labeledTextField2 = new mpv5.ui.beans.LabeledTextField();
      jLabel3 = new javax.swing.JLabel();
      jButton3 = new javax.swing.JButton();

      //\$2java.awt.Color(255, 255, 255));
      setName("Form"); // NOI18N
      setLayout(new java.awt.BorderLayout());

//$2java.awt.Color(255, 255, 255));
      java.util.ResourceBundle bundle = mpv5.i18n.LanguageManager.getBundle();// NOI18N
      jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("wizard_FirstSettings3.jPanel1.border.title"))); // NOI18N
      jPanel1.setName("jPanel1"); // NOI18N

      jPanel2.setName("jPanel2"); // NOI18N

      jScrollPane1.setName("jScrollPane1"); // NOI18N

      jTextPane1.setText(bundle.getString("wizard_FirstSettings3.jTextPane1.text")); // NOI18N
      jTextPane1.setName("jTextPane1"); // NOI18N
      jScrollPane1.setViewportView(jTextPane1);

      jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("wizard_FirstSettings3.jPanel3.border.title"))); // NOI18N
      jPanel3.setName("jPanel3"); // NOI18N

      labeledTextChooser2.set_Label(bundle.getString("wizard_FirstSettings3.labeledTextChooser2._Label")); // NOI18N
      labeledTextChooser2.setName("labeledTextChooser2"); // NOI18N

      jCheckBox3.setText(bundle.getString("wizard_FirstSettings3.jCheckBox3.text")); // NOI18N
      jCheckBox3.setName("jCheckBox3"); // NOI18N
      jCheckBox3.addItemListener(new java.awt.event.ItemListener() {
         public void itemStateChanged(java.awt.event.ItemEvent evt) {
            jCheckBox3ItemStateChanged(evt);
         }
      });

      labeledTextField1.set_Label(bundle.getString("wizard_FirstSettings3.labeledTextField1._Label")); // NOI18N
      labeledTextField1.set_Text(bundle.getString("wizard_FirstSettings3.labeledTextField1._Text")); // NOI18N
      labeledTextField1.setEnabled(false);
      labeledTextField1.setName("labeledTextField1"); // NOI18N

      labeledTextField2.set_Label(bundle.getString("wizard_FirstSettings3.labeledTextField2._Label")); // NOI18N
      labeledTextField2.setEnabled(false);
      labeledTextField2.setName("labeledTextField2"); // NOI18N

      jLabel3.setText(bundle.getString("wizard_FirstSettings3.jLabel3.text")); // NOI18N
      jLabel3.setName("jLabel3"); // NOI18N

      javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
      jPanel3.setLayout(jPanel3Layout);
      jPanel3Layout.setHorizontalGroup(
         jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(jPanel3Layout.createSequentialGroup()
            .addContainerGap()
            .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
               .addComponent(labeledTextChooser2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
               .addGroup(jPanel3Layout.createSequentialGroup()
                  .addComponent(jCheckBox3, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                  .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                  .addComponent(labeledTextField1, javax.swing.GroupLayout.DEFAULT_SIZE, 244, Short.MAX_VALUE)
                  .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                  .addComponent(labeledTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, 119, javax.swing.GroupLayout.PREFERRED_SIZE))
               .addGroup(jPanel3Layout.createSequentialGroup()
                  .addComponent(jLabel3)
                  .addGap(0, 0, Short.MAX_VALUE)))
            .addContainerGap())
      );
      jPanel3Layout.setVerticalGroup(
         jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(jPanel3Layout.createSequentialGroup()
            .addComponent(labeledTextChooser2, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(3, 3, 3)
            .addComponent(jLabel3)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
               .addComponent(jCheckBox3)
               .addComponent(labeledTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
               .addComponent(labeledTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addContainerGap(17, Short.MAX_VALUE))
      );

      javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
      jPanel2.setLayout(jPanel2Layout);
      jPanel2Layout.setHorizontalGroup(
         jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
         .addGroup(jPanel2Layout.createSequentialGroup()
            .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(0, 0, Short.MAX_VALUE))
      );
      jPanel2Layout.setVerticalGroup(
         jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(jPanel2Layout.createSequentialGroup()
            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 72, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
      );

      jButton3.setText(bundle.getString("wizard_FirstSettings3.jButton3.text")); // NOI18N
      jButton3.setName("jButton3"); // NOI18N
      jButton3.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            jButton3ActionPerformed(evt);
         }
      });

      javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
      jPanel1.setLayout(jPanel1Layout);
      jPanel1Layout.setHorizontalGroup(
         jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
         .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
            .addGap(0, 0, Short.MAX_VALUE)
            .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 101, javax.swing.GroupLayout.PREFERRED_SIZE))
      );
      jPanel1Layout.setVerticalGroup(
         jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(jPanel1Layout.createSequentialGroup()
            .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jButton3)
            .addContainerGap())
      );

      add(jPanel1, java.awt.BorderLayout.CENTER);
   }// </editor-fold>//GEN-END:initComponents

   private void jCheckBox3ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jCheckBox3ItemStateChanged
      labeledTextField1.setEnabled(jCheckBox3.isSelected());
      labeledTextField2.setEnabled(jCheckBox3.isSelected());
      labeledTextChooser2.setEnabled(!jCheckBox3.isSelected());
   }//GEN-LAST:event_jCheckBox3ItemStateChanged

   private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed

      Runnable runnable2 = new Runnable() {
         @Override
         public void run() {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

            LocalSettings.setProperty(LocalSettings.OFFICE_HOST, labeledTextField1.getText());
            LocalSettings.setProperty(LocalSettings.OFFICE_PORT, jCheckBox3.isSelected() ? labeledTextField2.getText() : "0");
            LocalSettings.setProperty(LocalSettings.OFFICE_REMOTE, Boolean.toString(jCheckBox3.isSelected()));
            LocalSettings.setProperty(LocalSettings.OFFICE_USE, "true");
            LocalSettings.setProperty(LocalSettings.OFFICE_LOCALSERVER, "false");
            LocalSettings.setProperty(LocalSettings.OFFICE_HOME, labeledTextChooser2.get_Text(false));
            checkOS();
            LocalSettings.save();
            mpv5.YabsViewProxy.instance().setWaiting(true);
            try {

               Popup.notice(Messages.OO_DONE_LOADING);
            } catch (Exception e) {
               Popup.notice(Messages.ERROR_OCCURED);


            } finally {
               mpv5.YabsViewProxy.instance().setWaiting(false);
               setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            }
         }
      };
      final Thread startServerThread = new Thread(runnable2);
      startServerThread.start();
   }//GEN-LAST:event_jButton3ActionPerformed
   // Variables declaration - do not modify//GEN-BEGIN:variables
   private javax.swing.JButton jButton3;
   private javax.swing.JCheckBox jCheckBox3;
   private javax.swing.JLabel jLabel3;
   private javax.swing.JPanel jPanel1;
   private javax.swing.JPanel jPanel2;
   private javax.swing.JPanel jPanel3;
   private javax.swing.JScrollPane jScrollPane1;
   private javax.swing.JTextPane jTextPane1;
   private mpv5.ui.beans.LabeledTextChooser labeledTextChooser2;
   private mpv5.ui.beans.LabeledTextField labeledTextField1;
   private mpv5.ui.beans.LabeledTextField labeledTextField2;
   // End of variables declaration//GEN-END:variables

   public boolean next() {
      LocalSettings.setProperty(LocalSettings.OFFICE_HOST, labeledTextField1.getText());
      LocalSettings.setProperty(LocalSettings.OFFICE_PORT, jCheckBox3.isSelected() ? labeledTextField2.getText() : "0");
      LocalSettings.setProperty(LocalSettings.OFFICE_REMOTE, Boolean.toString(jCheckBox3.isSelected()));
      LocalSettings.setProperty(LocalSettings.OFFICE_USE, "true");
      LocalSettings.setProperty(LocalSettings.OFFICE_AUTOSTART, Boolean.toString(!jCheckBox3.isSelected()));
      LocalSettings.setProperty(LocalSettings.OFFICE_HOME, labeledTextChooser2.get_Text(false));
      LocalSettings.setProperty(LocalSettings.OFFICE_LOCALSERVER, "false");
      LocalSettings.save();
      return true;

   }

   public boolean back() {
      return true;
   }

   public void load() {
   }

   private void setModels() {
      Runnable runnable = new Runnable() {
         public void run() {
            try {

               YabsViewProxy.instance().setWaiting(true);
               IApplicationAssistant applicationAssistant = new ApplicationAssistant();
               ILazyApplicationInfo appInfo = applicationAssistant.getLatestLocalLibreOfficeApplication();
               if (appInfo == null) {
                  appInfo = applicationAssistant.getLatestLocalOpenOfficeOrgApplication();
               }
               labeledTextChooser2.setText(appInfo.getHome());
               YabsViewProxy.instance().setWaiting(false);
            } catch (OfficeApplicationException ex) {
               Log.Debug(ex);
            }
         }
      };
      SwingUtilities.invokeLater(runnable);
   }

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
      LocalSettings.setProperty(LocalSettings.OFFICE_LOCALSERVER, "false");
      LocalSettings.setProperty(LocalSettings.OFFICE_USE, "true");
      LocalSettings.setProperty(LocalSettings.OFFICE_REMOTE, Boolean.toString(jCheckBox3.isSelected()));
      LocalSettings.setProperty(LocalSettings.OFFICE_HOST, labeledTextField1.getText());
      LocalSettings.setProperty(LocalSettings.OFFICE_PORT, labeledTextField2.getText());
      LocalSettings.setProperty(LocalSettings.OFFICE_AUTOSTART, "true");
      checkOS();

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

   public void setValues(PropertyStore values) {
      try {
         labeledTextChooser2.set_Text(LocalSettings.getProperty(LocalSettings.OFFICE_HOME));
         jCheckBox3.setSelected(LocalSettings.getBooleanProperty(LocalSettings.OFFICE_REMOTE));
         labeledTextField1.setText(LocalSettings.getProperty(LocalSettings.OFFICE_HOST));
         labeledTextField2.setText(LocalSettings.getProperty(LocalSettings.OFFICE_PORT));
      } catch (Exception e) {
         Log.Debug(this, e);
      }
   }
}
