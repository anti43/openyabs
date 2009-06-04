/*
 * SearchPanel.java
 *
 * Created on Nov 30, 2008, 6:16:09 PM
 */
package mpv5.ui.dialogs.subcomponents;

import java.awt.Cursor;
import java.io.File;
import javax.swing.DefaultComboBoxModel;
import mpv5.Main;
import mpv5.db.common.ConnectionTypeHandler;
import mpv5.db.common.DatabaseConnection;
import mpv5.db.common.DatabaseInstallation;
import mpv5.globals.Constants;
import mpv5.globals.LocalSettings;
import mpv5.globals.Messages;

import mpv5.logging.Log;
import mpv5.logging.LogConsole;
import mpv5.ui.dialogs.WizardMaster;
import mpv5.ui.dialogs.Wizardable;

/**
 *
 *  anti43
 */
public class wizard_DBSettings_1 extends javax.swing.JPanel implements Wizardable {

    private static final long serialVersionUID = 1L;
    private WizardMaster master;

    public wizard_DBSettings_1(WizardMaster w) {
        this.master = w;
        initComponents();
        jComboBox1.setModel(new DefaultComboBoxModel(ConnectionTypeHandler.DRIVERS));
        load();
    }

    private boolean DBVerification() {
        DatabaseConnection conn;
        this.master.setCursor(new Cursor(Cursor.WAIT_CURSOR));

        if (labeledTextChooser1.get_Text(false) != null && labeledTextChooser1.get_Text(false).length() > 0) {
            master.getStore().changeProperty("driver", jComboBox1.getSelectedItem().toString());
            master.getStore().changeProperty("url", labeledTextChooser1.get_Text(false));
            master.getStore().changeProperty("user", labeledTextField2.get_Text());
            master.getStore().changeProperty("password", labeledTextField1.get_Text());
            master.getStore().changeProperty("dbname", labeledTextField3.get_Text());
            master.setMessage(Messages.CONNECTION_PROBE + master.getStore().getProperty("driver"));
            conn = new DatabaseConnection();
            try {

                LocalSettings.setProperty(LocalSettings.DBPATH, master.getStore().getProperty("url"));
                LocalSettings.setProperty(LocalSettings.DBDRIVER, master.getStore().getProperty("driver"));
                LocalSettings.setProperty(LocalSettings.DBUSER, master.getStore().getProperty("user"));
                LocalSettings.setProperty(LocalSettings.DBPASSWORD, master.getStore().getProperty("password"));
                LocalSettings.setProperty(LocalSettings.DBNAME, master.getStore().getProperty("dbname"));

                if (conn.connect(master.getStore().getProperty("driver"),
                        master.getStore().getProperty("user"),
                        master.getStore().getProperty("password"),
                        master.getStore().getProperty("url"),
                        master.getStore().getProperty("dbname"),
                        true)) {
                    master.setMessage(Messages.CONNECTION_VERIFIED.toString());
                    LocalSettings.save();
                    LocalSettings.apply();

                    if (!jCheckBox1.isSelected()) {
                        master.setMessage(Messages.CREATING_DATABASE.toString());
                        if (conn.runQueries(new DatabaseInstallation().getStructure())) {
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
        LocalSettings.setProperty(LocalSettings.CACHE_DIR, LocalSettings.getProperty(LocalSettings.DBPATH) + File.separator + "Cache");
        LocalSettings.save();
        File file1 = new File(LocalSettings.getProperty(LocalSettings.CACHE_DIR));
        file1.mkdirs();
        ///////////////////////////////////////////////////////////

        return file1.exists();
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        labeledTextField2 = new mpv5.ui.beans.LabeledTextField();
        labeledTextField1 = new mpv5.ui.beans.LabeledTextField();
        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jComboBox1 = new javax.swing.JComboBox();
        labeledTextChooser1 = new mpv5.ui.beans.LabeledTextChooser();
        labeledTextField3 = new mpv5.ui.beans.LabeledTextField();
        jCheckBox1 = new javax.swing.JCheckBox();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();

        setBackground(new java.awt.Color(255, 255, 255));
        java.util.ResourceBundle bundle = mpv5.i18n.LanguageManager.getBundle(); // NOI18N
        setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("wizard_DBSettings_1.border.title"))); // NOI18N
        setName("Form"); // NOI18N

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("wizard_DBSettings_1.jPanel1.border.title"))); // NOI18N
        jPanel1.setName("jPanel1"); // NOI18N

        labeledTextField2.set_Label(bundle.getString("wizard_DBSettings_1.labeledTextField2._Label")); // NOI18N
        labeledTextField2.setName("labeledTextField2"); // NOI18N

        labeledTextField1.set_Label(bundle.getString("wizard_DBSettings_1.labeledTextField1._Label")); // NOI18N
        labeledTextField1.setName("labeledTextField1"); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(labeledTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(labeledTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(286, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(labeledTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(labeledTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(20, Short.MAX_VALUE))
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("wizard_DBSettings_1.jPanel2.border.title"))); // NOI18N
        jPanel2.setName("jPanel2"); // NOI18N

        jLabel1.setText(bundle.getString("wizard_DBSettings_1.jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        jComboBox1.setName("jComboBox1"); // NOI18N

        labeledTextChooser1.set_Label(bundle.getString("wizard_DBSettings_1.labeledTextChooser1._Label")); // NOI18N
        labeledTextChooser1.setName("labeledTextChooser1"); // NOI18N

        labeledTextField3.set_Label(bundle.getString("wizard_DBSettings_1.labeledTextField3._Label")); // NOI18N
        labeledTextField3.setName("labeledTextField3"); // NOI18N

        jCheckBox1.setText(bundle.getString("wizard_DBSettings_1.jCheckBox1.text")); // NOI18N
        jCheckBox1.setName("jCheckBox1"); // NOI18N

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(labeledTextChooser1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 479, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addGap(28, 28, 28)
                        .addComponent(jComboBox1, 0, 305, Short.MAX_VALUE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(labeledTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jCheckBox1)))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(labeledTextChooser1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jCheckBox1)
                    .addComponent(labeledTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("wizard_DBSettings_1.jPanel3.border.title"))); // NOI18N
        jPanel3.setName("jPanel3"); // NOI18N

        jScrollPane1.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        jScrollPane1.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        jScrollPane1.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        jScrollPane1.setName("jScrollPane1"); // NOI18N

        jTextArea1.setBackground(new java.awt.Color(212, 208, 200));
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
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 491, Short.MAX_VALUE)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 97, Short.MAX_VALUE)
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
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JComboBox jComboBox1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextArea jTextArea1;
    private mpv5.ui.beans.LabeledTextChooser labeledTextChooser1;
    private mpv5.ui.beans.LabeledTextField labeledTextField1;
    private mpv5.ui.beans.LabeledTextField labeledTextField2;
    private mpv5.ui.beans.LabeledTextField labeledTextField3;
    // End of variables declaration//GEN-END:variables

    public boolean next() {
        if (DBVerification() & DirectoryCreate()) {
          
            Main.getApplication().go(true);
//              try {
//                Log.setLogLevel(Log.LOGLEVEL_NONE);
//                LogConsole.setLogFile(null);
//            } catch (Exception ignore) {
//            }
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
        labeledTextChooser1.set_Text(Main.MPPATH);
        labeledTextField3.set_Text(Constants.DATABASENAME);
    }
}
