/*
 * SearchPanel.java
 *
 * Created on Nov 30, 2008, 6:16:09 PM
 */
package mpv5.ui.dialogs.subcomponents;

import java.awt.Cursor;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import mpv5.Main;
import mpv5.db.common.ConnectionTypeHandler;
import mpv5.db.common.DatabaseConnection;
import mpv5.db.common.DatabaseInstallation;
import mpv5.db.migration.MigrationWB;
import mpv5.globals.Constants;
import mpv5.globals.LocalSettings;
import mpv5.globals.Messages;
import mpv5.i18n.LanguageManager;
import mpv5.logging.Log;
import mpv5.logging.YConsole;
import mpv5.ui.dialogs.*;

/**
 *
 *
 */
public class wizard_DBSettings_1 extends javax.swing.JPanel implements Wizardable {

   private static final long serialVersionUID = 1L;
   private WizardMaster master;
   private Integer forConnId = null;

   public wizard_DBSettings_1(Wizard w, Integer forConnId) {
      this.master = w;
      initComponents(); 
      jComboBox1.setModel(new DefaultComboBoxModel(ConnectionTypeHandler.DRIVERS));
      this.forConnId = forConnId;
      load();
   }

   private boolean DBVerification() {
      DatabaseConnection conn;
      this.master.setCursor(new Cursor(Cursor.WAIT_CURSOR));

      if (labeledTextChooser1.get_Text(false) != null && labeledTextChooser1.get_Text(false).length() > 0) {
         master.getStore().changeProperty("driver", jComboBox1.getSelectedItem().toString());
         master.getStore().changeProperty("url", labeledTextChooser1.get_Text(false));
         master.getStore().changeProperty("user", labeledTextField4.get_Text());
         master.getStore().changeProperty("password", labeledTextField5.get_Text());
         master.getStore().changeProperty("dbname", labeledTextField3.get_Text());
         master.getStore().changeProperty("dbprefix", prefix.get_Text());
         master.setMessage(Messages.CONNECTION_PROBE + master.getStore().getProperty("driver"));
         conn = new DatabaseConnection();
         try {
            LocalSettings.setProperty(LocalSettings.DBPATH, master.getStore().getProperty("url"));
            LocalSettings.setProperty(LocalSettings.DBDRIVER, master.getStore().getProperty("driver"));
            LocalSettings.setProperty(LocalSettings.DBUSER, master.getStore().getProperty("user"));
            LocalSettings.setProperty(LocalSettings.DBPASSWORD, master.getStore().getProperty("password"));
            LocalSettings.setProperty(LocalSettings.DBNAME, master.getStore().getProperty("dbname"));
            LocalSettings.setProperty(LocalSettings.DBPREFIX, master.getStore().getProperty("dbprefix"));

            LocalSettings.setProperty(LocalSettings.OFFICE_USE, "false");
            LocalSettings.setProperty(LocalSettings.OFFICE_ALT, "true");
      
            if (jCheckBox2.isSelected()) {
               LocalSettings.setProperty(LocalSettings.DBTYPE, "network");
            } else {
               LocalSettings.setProperty(LocalSettings.DBTYPE, "single");
            }

            if (restore.isSelected()) {
               master.setMessage(Messages.RESTORING_DATABASE.toString());
               conn.setProgressbar(master.getProgressbar());
               Log.Debug(this, "Running DB restore ...");
               MigrationWB.instanceOf().doRestore(DumpURL.getText(), DumpName.getText(), backupDBName.getText(), conn, master);
               LocalSettings.save(forConnId);
               LocalSettings.apply();
            } else if (conn.connect(master.getStore().getProperty("driver"),
                  master.getStore().getProperty("user"),
                  master.getStore().getProperty("password"),
                  master.getStore().getProperty("url"),
                  master.getStore().getProperty("dbname"),
                  master.getStore().getProperty("dbprefix"),
                  !existingData.isSelected())) {
               Log.Debug(this, master.getStore().getProperty("url") + ": OK");
               master.setMessage(Messages.CONNECTION_VERIFIED.toString());
               LocalSettings.save(forConnId);
               LocalSettings.apply();

               if (!existingData.isSelected()) {
                  master.setMessage(Messages.CREATING_DATABASE.toString());
                  conn.setProgressbar(master.getProgressbar());
                  Log.Debug(this, "Running DB install queries...");
                  if (conn.runQueries(new DatabaseInstallation().getStructure())
                        & conn.runQueries(new DatabaseInstallation().getInitialData())) {
        
                     master.setMessage(Messages.CONNECTION_VERIFIED.toString());
                  } else {
                     LocalSettings.removeInstance(forConnId);
                     LocalSettings.save();
                     throw new Exception(Messages.CREATING_DATABASE_FAILED.toString());
                  }
               }
               master.isEnd(true);
            } else {
               LocalSettings.removeInstance(forConnId);
               LocalSettings.save();
               throw new Exception(Messages.CONNECTION_FAILED.toString());
            }

         } catch (Exception ex) {
            master.setMessage(Messages.CONNECTION_FAILED.toString());
            this.master.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            Log.Debug(ex);
            if (Popup.Y_N_dialog(this, Messages.ERROR_OCCURED + "\n" + Messages.SEE_LOG + "?", Messages.CONNECTION_FAILED)) {
               try {
                  Desktop.getDesktop().open(YConsole.getLogfile());
               } catch (IOException ex1) {
                  Popup.error(ex1);
               }
            }
            return false;
         }
         this.master.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
         return true;
      } else {
         this.master.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
         return false;
      }
   }

   private boolean DirectoryCreate() {
      ////////////// The cache dir //////////////////////
      LocalSettings.setProperty(LocalSettings.CACHE_DIR, Main.MPPATH + File.separator + "Cache");
      LocalSettings.save(forConnId);
      File file1 = new File(LocalSettings.getProperty(LocalSettings.CACHE_DIR));
      file1.mkdirs();
      ///////////////////////////////////////////////////////////

      return file1.exists();
   }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jComboBox1 = new javax.swing.JComboBox();
        labeledTextField3 = new mpv5.ui.beans.LabeledTextField();
        existingData = new javax.swing.JCheckBox();
        jLabel2 = new javax.swing.JLabel();
        jCheckBox2 = new javax.swing.JCheckBox();
        jPanel4 = new javax.swing.JPanel();
        labeledTextField4 = new mpv5.ui.beans.LabeledTextField();
        labeledTextField5 = new mpv5.ui.beans.LabeledTextField();
        jButton2 = new javax.swing.JButton();
        prefix = new mpv5.ui.beans.LabeledTextField();
        jButton3 = new javax.swing.JButton();
        jLabel4 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        labeledTextChooser1 = new mpv5.ui.beans.LabeledTextChooser();
        jPanel5 = new javax.swing.JPanel();
        restore = new javax.swing.JCheckBox();
        DumpURL = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        backupDBName = new mpv5.ui.beans.LabeledTextField();
        DumpURLBtn = new javax.swing.JButton();
        DumpName = new mpv5.ui.beans.LabeledTextField();

        setBackground(new java.awt.Color(255, 255, 255));
        java.util.ResourceBundle bundle = mpv5.i18n.LanguageManager.getBundle();  // NOI18N
        setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("wizard_DBSettings_1.border.title"))); // NOI18N
        setName("Form"); // NOI18N
        setLayout(new java.awt.BorderLayout());

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("wizard_DBSettings_1.jPanel2.border.title"))); // NOI18N
        jPanel2.setName("jPanel2"); // NOI18N

        jLabel1.setText(bundle.getString("wizard_DBSettings_1.jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        jComboBox1.setEditable(true);
        jComboBox1.setFont(new java.awt.Font("Dialog", 0, 11));
        jComboBox1.setName("jComboBox1"); // NOI18N
        jComboBox1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox1ActionPerformed(evt);
            }
        });

        labeledTextField3.set_Label(bundle.getString("wizard_DBSettings_1.labeledTextField3._Label")); // NOI18N
        labeledTextField3.setName("labeledTextField3"); // NOI18N

        existingData.setText(bundle.getString("wizard_DBSettings_1.existingData.text")); // NOI18N
        existingData.setName("existingData"); // NOI18N

        jLabel2.setText(bundle.getString("wizard_DBSettings_1.jLabel2.text")); // NOI18N
        jLabel2.setName("jLabel2"); // NOI18N

        jCheckBox2.setFont(new java.awt.Font("Dialog", 0, 12));
        jCheckBox2.setText(bundle.getString("wizard_DBSettings_1.jCheckBox2.text")); // NOI18N
        jCheckBox2.setName("jCheckBox2"); // NOI18N

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("wizard_DBSettings_1.jPanel4.border.title"))); // NOI18N
        jPanel4.setName("jPanel4"); // NOI18N

        labeledTextField4.set_Label(bundle.getString("wizard_DBSettings_1.labeledTextField4._Label")); // NOI18N
        labeledTextField4.setEnabled(false);
        labeledTextField4.setName("labeledTextField4"); // NOI18N
        jPanel4.add(labeledTextField4);

        labeledTextField5.set_Label(bundle.getString("wizard_DBSettings_1.labeledTextField5._Label")); // NOI18N
        labeledTextField5.setEnabled(false);
        labeledTextField5.setName("labeledTextField5"); // NOI18N
        jPanel4.add(labeledTextField5);

        jButton2.setFont(new java.awt.Font("Dialog", 0, 12));
        jButton2.setText(bundle.getString("wizard_DBSettings_1.jButton2.text")); // NOI18N
        jButton2.setName("jButton2"); // NOI18N
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });
        jPanel4.add(jButton2);

        prefix.set_Label(bundle.getString("wizard_DBSettings_1.prefix._Label")); // NOI18N
        prefix.setEnabled(false);
        prefix.setName("prefix"); // NOI18N

        jButton3.setFont(new java.awt.Font("Dialog", 0, 12));
        jButton3.setText(bundle.getString("wizard_DBSettings_1.jButton3.text")); // NOI18N
        jButton3.setName("jButton3"); // NOI18N
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        jLabel4.setFont(new java.awt.Font("Dialog", 0, 12));
        jLabel4.setText(bundle.getString("wizard_DBSettings_1.jLabel4.text")); // NOI18N
        jLabel4.setName("jLabel4"); // NOI18N

        jButton1.setText(bundle.getString("wizard_DBSettings_1.jButton1.text")); // NOI18N
        jButton1.setName("jButton1"); // NOI18N
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        labeledTextChooser1.set_Label(bundle.getString("wizard_DBSettings_1.labeledTextChooser1._Label")); // NOI18N
        labeledTextChooser1.setFocusCycleRoot(true);
        labeledTextChooser1.setFocusTraversalPolicyProvider(true);
        labeledTextChooser1.setName("labeledTextChooser1"); // NOI18N

        jPanel5.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("wizard_DBSettings_1.jPanel5.border.title"))); // NOI18N
        jPanel5.setName("jPanel5"); // NOI18N

        restore.setText(bundle.getString("wizard_DBSettings_1.restore.text")); // NOI18N
        restore.setName("restore"); // NOI18N
        restore.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                restoreActionPerformed(evt);
            }
        });

        DumpURL.setEditable(false);
        DumpURL.setText(bundle.getString("wizard_DBSettings_1.DumpURL.text")); // NOI18N
        DumpURL.setName("DumpURL"); // NOI18N

        jLabel5.setText(bundle.getString("wizard_DBSettings_1.jLabel5.text")); // NOI18N
        jLabel5.setName("jLabel5"); // NOI18N

        backupDBName.set_Label(bundle.getString("wizard_DBSettings_1.backupDBName._Label")); // NOI18N
        backupDBName.setEnabled(false);
        backupDBName.setName("backupDBName"); // NOI18N

        DumpURLBtn.setText(bundle.getString("wizard_DBSettings_1.DumpURLBtn.text")); // NOI18N
        DumpURLBtn.setEnabled(false);
        DumpURLBtn.setName("DumpURLBtn"); // NOI18N
        DumpURLBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                DumpURLBtnActionPerformed(evt);
            }
        });

        DumpName.set_Label(bundle.getString("wizard_DBSettings_1.DumpName._Label")); // NOI18N
        DumpName.setEnabled(false);
        DumpName.setName("DumpName"); // NOI18N

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(backupDBName, javax.swing.GroupLayout.DEFAULT_SIZE, 600, Short.MAX_VALUE)
            .addComponent(DumpName, javax.swing.GroupLayout.DEFAULT_SIZE, 600, Short.MAX_VALUE)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, 139, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(DumpURL, javax.swing.GroupLayout.PREFERRED_SIZE, 434, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(DumpURLBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addComponent(restore, javax.swing.GroupLayout.DEFAULT_SIZE, 600, Short.MAX_VALUE)
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addComponent(restore)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(DumpURL, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(DumpURLBtn)
                    .addComponent(jLabel5))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(backupDBName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(DumpName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0))
        );

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 183, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(4, 4, 4)
                        .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, 352, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(10, 10, 10)
                        .addComponent(jButton1))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 183, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(4, 4, 4)
                        .addComponent(jCheckBox2, javax.swing.GroupLayout.PREFERRED_SIZE, 415, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(labeledTextChooser1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(existingData, javax.swing.GroupLayout.PREFERRED_SIZE, 286, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(labeledTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(prefix, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jButton3)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel4))
                    .addComponent(jPanel4, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(4, 4, 4)
                        .addComponent(jLabel1))
                    .addComponent(jComboBox1)
                    .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addGap(4, 4, 4)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addComponent(jLabel2))
                    .addComponent(jCheckBox2))
                .addGap(7, 7, 7)
                .addComponent(labeledTextChooser1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(existingData)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(labeledTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(prefix, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel4)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, 61, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 15, Short.MAX_VALUE)
                .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0))
        );

        add(jPanel2, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void jComboBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox1ActionPerformed
       if (jComboBox1.getSelectedItem().toString().contains("mysql")) {
          labeledTextChooser1.set_Text("localhost:3306");
          if (restore.isSelected()) {
             backupDBName.setText("");
             backupDBName.setEnabled(false);
             DumpName.setEnabled(true);
          }
       } else if (jComboBox1.getSelectedItem().toString().contains("custom")) {
          if (restore.isSelected()) {
             DumpName.setText("");
             DumpName.setEnabled(false);
             backupDBName.setText("");
             backupDBName.setEnabled(false);
          }
          labeledTextChooser1.set_Text("jdbc:sql://<path>:port");
       } else if (jComboBox1.getSelectedItem().toString().contains("derby")) {
          labeledTextChooser1.set_Text(Main.MPPATH);
          if (restore.isSelected()) {
             DumpName.setText("");
             DumpName.setEnabled(false);
             backupDBName.setEnabled(true);
          }
       }

    }//GEN-LAST:event_jComboBox1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed

       labeledTextField4.setEnabled(true);
       labeledTextField5.setEnabled(true);

    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed

       prefix.setEnabled(true);
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed

       try {
          ConnectionTypeHandler.testDriver(jComboBox1.getSelectedItem().toString());
          Popup.notice("OK!");
       } catch (ClassNotFoundException ex) {
          Popup.error(ex);
       }
    }//GEN-LAST:event_jButton1ActionPerformed

    private void DumpURLBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_DumpURLBtnActionPerformed
       DialogForFile df = new DialogForFile();
       df.setFileFilter(DialogForFile.SQL_FILES);
       if (df.chooseFile()) {
          DumpURL.setText(df.getSelectedFile().getAbsolutePath());
       }
    }//GEN-LAST:event_DumpURLBtnActionPerformed

    private void restoreActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_restoreActionPerformed
       JCheckBox box = (JCheckBox) evt.getSource();
       if (box.isSelected()) {
          DumpURL.setEditable(true);
          DumpURLBtn.setEnabled(true);
          if (jComboBox1.getSelectedItem().toString().contains("mysql")) {
             DumpName.setEnabled(true);
             backupDBName.setEnabled(false);
          } else if (jComboBox1.getSelectedItem().toString().contains("derby")) {
             DumpName.setEnabled(false);
             backupDBName.setEnabled(true);
          } else {
             DumpName.setEnabled(false);
             backupDBName.setEnabled(false);
          }
       } else {
          DumpURL.setText("");
          DumpURL.setEditable(false);
          DumpURLBtn.setEnabled(false);
          DumpName.setText("");
          DumpName.setEnabled(false);
          backupDBName.setText("");
          backupDBName.setEnabled(false);
       }
    }//GEN-LAST:event_restoreActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private mpv5.ui.beans.LabeledTextField DumpName;
    private javax.swing.JTextField DumpURL;
    private javax.swing.JButton DumpURLBtn;
    private mpv5.ui.beans.LabeledTextField backupDBName;
    private javax.swing.JCheckBox existingData;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JCheckBox jCheckBox2;
    private javax.swing.JComboBox jComboBox1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private mpv5.ui.beans.LabeledTextChooser labeledTextChooser1;
    private mpv5.ui.beans.LabeledTextField labeledTextField3;
    private mpv5.ui.beans.LabeledTextField labeledTextField4;
    private mpv5.ui.beans.LabeledTextField labeledTextField5;
    private mpv5.ui.beans.LabeledTextField prefix;
    private javax.swing.JCheckBox restore;
    // End of variables declaration//GEN-END:variables

   public boolean next() {
      if (DBVerification() & DirectoryCreate()) {
         this.master.dispose();
         Log.setLogLevel(Log.LOGLEVEL_NONE);
         Main.start();
         return true;
      } else {
         this.master.setCursor(Cursor.DEFAULT_CURSOR);
         return false;
      }
   }

   public boolean back() {
      return false;
   }

   public final void load() {
      String dbn = Constants.DATABASENAME;
      if (forConnId != null) {
         dbn += forConnId;
      }
      labeledTextChooser1.set_Text(Main.MPPATH);
      labeledTextField3.set_Text(dbn); 
   }
}
