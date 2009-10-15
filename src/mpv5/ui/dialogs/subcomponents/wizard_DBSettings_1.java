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
import java.net.URISyntaxException;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JFileChooser;
import mpv5.Main;
import mpv5.db.common.ConnectionTypeHandler;
import mpv5.db.common.DatabaseConnection;
import mpv5.db.common.DatabaseInstallation;
import mpv5.globals.Constants;
import mpv5.globals.LocalSettings;
import mpv5.globals.Messages;

import mpv5.i18n.LanguageManager;
import mpv5.logging.Log;
import mpv5.logging.LogConsole;
import mpv5.ui.dialogs.Popup;
import mpv5.ui.dialogs.Wizard;
import mpv5.ui.dialogs.WizardMaster;
import mpv5.ui.dialogs.Wizardable;
import mpv5.utils.files.FileDirectoryHandler;
import mpv5.utils.files.JarFinder;

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
        labeledTextChooser2.setMode(JFileChooser.DIRECTORIES_ONLY);
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
                if (!jCheckBox3.isSelected() && labeledTextChooser2.hasText()) {
                    LocalSettings.setProperty(LocalSettings.OFFICE_HOME, labeledTextChooser2.get_Text(true));
                    LocalSettings.setProperty(LocalSettings.OFFICE_USE, "true");
                    LocalSettings.setProperty(LocalSettings.OFFICE_REMOTE, "false");
                } else if (labeledTextField1.getText().length() > 0 && labeledTextField2.getText().length() > 0) {
                    LocalSettings.setProperty(LocalSettings.OFFICE_HOST, labeledTextField1.getText());
                    LocalSettings.setProperty(LocalSettings.OFFICE_PORT, labeledTextField2.getText());
                    LocalSettings.setProperty(LocalSettings.OFFICE_USE, "true");
                    LocalSettings.setProperty(LocalSettings.OFFICE_REMOTE, "true");
                } else {
                    LocalSettings.setProperty(LocalSettings.OFFICE_USE, "false");
                }
                if (jCheckBox2.isSelected()) {
                    LocalSettings.setProperty(LocalSettings.DBTYPE, "network");
                } else {
                    LocalSettings.setProperty(LocalSettings.DBTYPE, "single");
                }

                if (conn.connect(master.getStore().getProperty("driver"),
                        master.getStore().getProperty("user"),
                        master.getStore().getProperty("password"),
                        master.getStore().getProperty("url"),
                        master.getStore().getProperty("dbname"),
                        master.getStore().getProperty("dbprefix"),
                        true)) {
                    master.setMessage(Messages.CONNECTION_VERIFIED.toString());
                    LocalSettings.save(forConnId);
                    LocalSettings.apply();

                    if (!jCheckBox1.isSelected()) {
                        master.setMessage(Messages.CREATING_DATABASE.toString());
                        conn.setProgressbar(master.getProgressbar());
                        if (conn.runQueries(new DatabaseInstallation().getStructure())) {
                            try {
                                Log.Debug(this, "Importing languages..");
                                File[] langfiles = new File(this.getClass().getResource("/mpv5/resources/languagefiles").toURI()).listFiles();
                                for (int i = 0; i < langfiles.length; i++) {
                                    File file = langfiles[i];
                                    LanguageManager.importLanguage(file.getName(), file);
                                }
                            } catch (Exception uRISyntaxException) {
                                Log.Debug(uRISyntaxException);
                            }
                            master.setMessage(Messages.CONNECTION_VERIFIED.toString());
                        } else {
                            master.setMessage(Messages.CREATING_DATABASE_FAILED.toString());
                            return false;
                        }
                    }
                    master.isEnd(true);
                } else {
                    master.setMessage(Messages.CONNECTION_FAILED.toString());
                }

            } catch (Exception ex) {
                master.setMessage(Messages.CONNECTION_FAILED.toString());
                this.master.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                Log.Debug(ex);
                if (Popup.Y_N_dialog(this, Messages.ERROR_OCCURED + "\n" + Messages.SEE_LOG + "?", Messages.CONNECTION_FAILED)) {
                    try {
                        Desktop.getDesktop().open(LogConsole.getLogfile());
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

        jPanel1 = new javax.swing.JPanel();
        labeledTextChooser2 = new mpv5.ui.beans.LabeledTextChooser();
        jCheckBox3 = new javax.swing.JCheckBox();
        labeledTextField1 = new mpv5.ui.beans.LabeledTextField();
        labeledTextField2 = new mpv5.ui.beans.LabeledTextField();
        jLabel3 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jComboBox1 = new javax.swing.JComboBox();
        labeledTextChooser1 = new mpv5.ui.beans.LabeledTextChooser();
        labeledTextField3 = new mpv5.ui.beans.LabeledTextField();
        jCheckBox1 = new javax.swing.JCheckBox();
        jLabel2 = new javax.swing.JLabel();
        jCheckBox2 = new javax.swing.JCheckBox();
        jPanel4 = new javax.swing.JPanel();
        labeledTextField4 = new mpv5.ui.beans.LabeledTextField();
        labeledTextField5 = new mpv5.ui.beans.LabeledTextField();
        jButton2 = new javax.swing.JButton();
        prefix = new mpv5.ui.beans.LabeledTextField();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();

        setBackground(new java.awt.Color(255, 255, 255));
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("mpv5/resources/languages/Panels"); // NOI18N
        setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("wizard_DBSettings_1.border.title"))); // NOI18N
        setName("Form"); // NOI18N

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("wizard_DBSettings_1.jPanel1.border.title"))); // NOI18N
        jPanel1.setName("jPanel1"); // NOI18N

        labeledTextChooser2.set_Label(bundle.getString("wizard_DBSettings_1.labeledTextChooser2._Label")); // NOI18N
        labeledTextChooser2.setName("labeledTextChooser2"); // NOI18N

        jCheckBox3.setText(bundle.getString("wizard_DBSettings_1.jCheckBox3.text")); // NOI18N
        jCheckBox3.setName("jCheckBox3"); // NOI18N
        jCheckBox3.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jCheckBox3ItemStateChanged(evt);
            }
        });

        labeledTextField1.set_Label(bundle.getString("wizard_DBSettings_1.labeledTextField1._Label")); // NOI18N
        labeledTextField1.set_Text(bundle.getString("wizard_DBSettings_1.labeledTextField1._Text")); // NOI18N
        labeledTextField1.setEnabled(false);
        labeledTextField1.setName("labeledTextField1"); // NOI18N

        labeledTextField2.set_Label(bundle.getString("wizard_DBSettings_1.labeledTextField2._Label")); // NOI18N
        labeledTextField2.setEnabled(false);
        labeledTextField2.setName("labeledTextField2"); // NOI18N

        jLabel3.setText(bundle.getString("wizard_DBSettings_1.jLabel3.text")); // NOI18N
        jLabel3.setName("jLabel3"); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(labeledTextChooser2, javax.swing.GroupLayout.DEFAULT_SIZE, 505, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jCheckBox3, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(labeledTextField1, javax.swing.GroupLayout.DEFAULT_SIZE, 206, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(labeledTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, 157, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jLabel3))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(labeledTextChooser2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 6, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(labeledTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jCheckBox3)
                    .addComponent(labeledTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

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

        labeledTextChooser1.set_Label(bundle.getString("wizard_DBSettings_1.labeledTextChooser1._Label")); // NOI18N
        labeledTextChooser1.setFocusCycleRoot(true);
        labeledTextChooser1.setFocusTraversalPolicyProvider(true);
        labeledTextChooser1.setName("labeledTextChooser1"); // NOI18N

        labeledTextField3.set_Label(bundle.getString("wizard_DBSettings_1.labeledTextField3._Label")); // NOI18N
        labeledTextField3.setName("labeledTextField3"); // NOI18N

        jCheckBox1.setFont(new java.awt.Font("Dialog", 0, 12));
        jCheckBox1.setText(bundle.getString("wizard_DBSettings_1.jCheckBox1.text")); // NOI18N
        jCheckBox1.setName("jCheckBox1"); // NOI18N

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
        prefix.setName("prefix"); // NOI18N

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(labeledTextChooser1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 515, Short.MAX_VALUE)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(labeledTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jCheckBox1, javax.swing.GroupLayout.DEFAULT_SIZE, 320, Short.MAX_VALUE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(jLabel2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 183, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jCheckBox2, javax.swing.GroupLayout.DEFAULT_SIZE, 328, Short.MAX_VALUE)
                            .addComponent(jComboBox1, javax.swing.GroupLayout.Alignment.LEADING, 0, 328, Short.MAX_VALUE)))
                    .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, 515, Short.MAX_VALUE)
                    .addComponent(prefix, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE, false)
                    .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(jCheckBox2))
                .addGap(7, 7, 7)
                .addComponent(labeledTextChooser1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(1, 1, 1)
                        .addComponent(jCheckBox1, 0, 0, Short.MAX_VALUE))
                    .addComponent(labeledTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(prefix, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 9, Short.MAX_VALUE)
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, 61, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("wizard_DBSettings_1.jPanel3.border.title"))); // NOI18N
        jPanel3.setName("jPanel3"); // NOI18N

        jScrollPane1.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        jScrollPane1.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        jScrollPane1.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        jScrollPane1.setName("jScrollPane1"); // NOI18N

        jTextArea1.setBackground(new java.awt.Color(236, 233, 216));
        jTextArea1.setColumns(20);
        jTextArea1.setEditable(false);
        jTextArea1.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jTextArea1.setLineWrap(true);
        jTextArea1.setRows(5);
        jTextArea1.setText(bundle.getString("wizard_DBSettings_1.jTextArea1.text")); // NOI18N
        jTextArea1.setWrapStyleWord(true);
        jTextArea1.setName("jTextArea1"); // NOI18N
        jScrollPane1.setViewportView(jTextArea1);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 525, Short.MAX_VALUE)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 112, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, 227, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 100, Short.MAX_VALUE)
                .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jComboBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox1ActionPerformed
        if (jComboBox1.getSelectedItem().toString().contains("mysql")) {
            labeledTextChooser1.set_Text("localhost:3306");
        } else if (jComboBox1.getSelectedItem().toString().contains("custom")) {
            labeledTextChooser1.set_Text("jdbc:sql://<path>:port");
        } else if (jComboBox1.getSelectedItem().toString().contains("derby")) {
            labeledTextChooser1.set_Text(Main.MPPATH);
        }

    }//GEN-LAST:event_jComboBox1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed

        labeledTextField4.setEnabled(true);
        labeledTextField5.setEnabled(true);

    }//GEN-LAST:event_jButton2ActionPerformed

    private void jCheckBox3ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jCheckBox3ItemStateChanged
        labeledTextField1.setEnabled(jCheckBox3.isSelected());
        labeledTextField2.setEnabled(jCheckBox3.isSelected());
        labeledTextChooser2.setEnabled(!jCheckBox3.isSelected());
    }//GEN-LAST:event_jCheckBox3ItemStateChanged
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton2;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JCheckBox jCheckBox2;
    private javax.swing.JCheckBox jCheckBox3;
    private javax.swing.JComboBox jComboBox1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextArea jTextArea1;
    private mpv5.ui.beans.LabeledTextChooser labeledTextChooser1;
    private mpv5.ui.beans.LabeledTextChooser labeledTextChooser2;
    private mpv5.ui.beans.LabeledTextField labeledTextField1;
    private mpv5.ui.beans.LabeledTextField labeledTextField2;
    private mpv5.ui.beans.LabeledTextField labeledTextField3;
    private mpv5.ui.beans.LabeledTextField labeledTextField4;
    private mpv5.ui.beans.LabeledTextField labeledTextField5;
    private mpv5.ui.beans.LabeledTextField prefix;
    // End of variables declaration//GEN-END:variables

    public boolean next() {
        if (DBVerification() & DirectoryCreate()) {
            Main.getApplication().go(true);
            this.master.dispose();
            return true;
        } else {
            this.master.setCursor(Cursor.DEFAULT_CURSOR);
            return false;
        }
    }

    public boolean back() {
        return false;
    }

    public void load() {
        String dbn = Constants.DATABASENAME;
        if (forConnId != null) {
            dbn += forConnId;
        }
        labeledTextChooser1.set_Text(Main.MPPATH);
        labeledTextField3.set_Text(dbn);
        labeledTextField1.requestFocusInWindow();
    }
}
