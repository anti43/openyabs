package mpv5.ui.dialogs.subcomponents;

import java.awt.Cursor;
import java.util.List;
import java.util.Vector;
import javax.swing.DefaultComboBoxModel;

import mpv5.db.common.ConnectionTypeHandler;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseConnection;
import mpv5.db.common.DatabaseObject;
import mpv5.db.common.NodataFoundException;
import mpv5.db.common.QueryHandler;
import mpv5.db.common.ReturnValue;
import mpv5.globals.Messages;
import mpv5.logging.Log;
import mpv5.ui.dialogs.DialogForFile;
import mpv5.ui.dialogs.Popup;
import mpv5.ui.dialogs.WizardMaster;
import mpv5.ui.dialogs.Wizardable;

/**
 *
 * 
 */
public class wizard_Yabs1_Import extends javax.swing.JPanel implements Wizardable {

    private static final long serialVersionUID = -8347532498124147821L;
    private WizardMaster master;
    private DatabaseConnection conn;
    private QueryHandler qh;
    private int imports;

    public wizard_Yabs1_Import(WizardMaster w) {
        this.master = w;
        initComponents();
        path.setFilter(DialogForFile.DIRECTORIES);
        jComboBox1.setModel(new DefaultComboBoxModel(ConnectionTypeHandler.DRIVERS));
        path.setModalityParent(this);
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        path = new mpv5.ui.beans.LabeledTextChooser();
        jLabel1 = new javax.swing.JLabel();
        contacts = new javax.swing.JCheckBox();
        products = new javax.swing.JCheckBox();
        accounts = new javax.swing.JCheckBox();
        groups = new javax.swing.JCheckBox();
        adresses = new javax.swing.JCheckBox();
        users = new javax.swing.JCheckBox();
        jComboBox1 = new javax.swing.JComboBox();
        jLabel2 = new javax.swing.JLabel();
        dbname = new mpv5.ui.beans.LabeledTextField();
        dbuser = new mpv5.ui.beans.LabeledTextField();
        dbpassword = new mpv5.ui.beans.LabeledTextField();
        dbprefix = new mpv5.ui.beans.LabeledTextField();

        //\$2java.awt.Color(255, 255, 255));
        setName("Form"); // NOI18N
        setLayout(new java.awt.BorderLayout());

//$2java.awt.Color(255, 255, 255));
         java.util.ResourceBundle bundle = mpv5.i18n.LanguageManager.getBundle(); // NOI18N
        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("wizard_Yabs1_Import.jPanel1.border.title"))); // NOI18N
        jPanel1.setName("jPanel1"); // NOI18N

        path.set_Label(bundle.getString("wizard_Yabs1_Import.path._Label")); // NOI18N
        path.setName("path"); // NOI18N

        jLabel1.setFont(jLabel1.getFont().deriveFont(jLabel1.getFont().getStyle() & ~java.awt.Font.BOLD));
        jLabel1.setText(bundle.getString("wizard_Yabs1_Import.jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

//$2java.awt.Color(255, 255, 255));
        contacts.setSelected(true);
        contacts.setText(bundle.getString("wizard_Yabs1_Import.contacts.text")); // NOI18N
        contacts.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        contacts.setName("contacts"); // NOI18N

//$2java.awt.Color(255, 255, 255));
        products.setSelected(true);
        products.setText(bundle.getString("wizard_Yabs1_Import.products.text")); // NOI18N
        products.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        products.setName("products"); // NOI18N

//$2java.awt.Color(255, 255, 255));
        accounts.setSelected(true);
        accounts.setText(bundle.getString("wizard_Yabs1_Import.accounts.text")); // NOI18N
        accounts.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        accounts.setName("accounts"); // NOI18N

//$2java.awt.Color(255, 255, 255));
        groups.setSelected(true);
        groups.setText(bundle.getString("wizard_Yabs1_Import.groups.text")); // NOI18N
        groups.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        groups.setName("groups"); // NOI18N

//$2java.awt.Color(255, 255, 255));
        adresses.setSelected(true);
        adresses.setText(bundle.getString("wizard_Yabs1_Import.adresses.text")); // NOI18N
        adresses.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        adresses.setName("adresses"); // NOI18N

//$2java.awt.Color(255, 255, 255));
        users.setSelected(true);
        users.setText(bundle.getString("wizard_Yabs1_Import.users.text")); // NOI18N
        users.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        users.setName("users"); // NOI18N

        jComboBox1.setEditable(true);
        jComboBox1.setFont(new java.awt.Font("Dialog", 0, 11));
        jComboBox1.setName("jComboBox1"); // NOI18N
        jComboBox1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox1ActionPerformed(evt);
            }
        });

        jLabel2.setText(bundle.getString("wizard_Yabs1_Import.jLabel2.text")); // NOI18N
        jLabel2.setName("jLabel2"); // NOI18N

        dbname.set_Label(bundle.getString("wizard_Yabs1_Import.dbname._Label")); // NOI18N
        dbname.setName("dbname"); // NOI18N

        dbuser.set_Label(bundle.getString("wizard_Yabs1_Import.dbuser._Label")); // NOI18N
        dbuser.setName("dbuser"); // NOI18N

        dbpassword.set_Label(bundle.getString("wizard_Yabs1_Import.dbpassword._Label")); // NOI18N
        dbpassword.setName("dbpassword"); // NOI18N

        dbprefix.set_Label(bundle.getString("wizard_Yabs1_Import.dbprefix._Label")); // NOI18N
        dbprefix.setName("dbprefix"); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(contacts)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(adresses))
                    .addComponent(products)
                    .addComponent(accounts)
                    .addComponent(groups)
                    .addComponent(users)
                    .addComponent(path, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 505, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, 70, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, 431, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jLabel1)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(dbuser, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 119, Short.MAX_VALUE)
                        .addComponent(dbname, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(dbpassword, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 119, Short.MAX_VALUE)
                        .addComponent(dbprefix, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(users)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(groups)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(accounts)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(products)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(contacts)
                    .addComponent(adresses))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 77, Short.MAX_VALUE)
                .addComponent(jLabel1)
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(path, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(dbuser, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(dbpassword, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(dbname, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(dbprefix, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );

        add(jPanel1, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void jComboBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox1ActionPerformed
    }//GEN-LAST:event_jComboBox1ActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox accounts;
    private javax.swing.JCheckBox adresses;
    private javax.swing.JCheckBox contacts;
    private mpv5.ui.beans.LabeledTextField dbname;
    private mpv5.ui.beans.LabeledTextField dbpassword;
    private mpv5.ui.beans.LabeledTextField dbprefix;
    private mpv5.ui.beans.LabeledTextField dbuser;
    private javax.swing.JCheckBox groups;
    private javax.swing.JComboBox jComboBox1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private mpv5.ui.beans.LabeledTextChooser path;
    private javax.swing.JCheckBox products;
    private javax.swing.JCheckBox users;
    // End of variables declaration//GEN-END:variables

    public boolean next() {
        master.setCursor(new Cursor(Cursor.WAIT_CURSOR));
        if (path.hasText()) {

            imports();

            master.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            master.isEnd(true);

            Popup.notice(imports + " " + Messages.IMPORTED.toString());
            Popup.notice(errors, mpv5.globals.Messages.ERROR_OCCURED);
            return false;
        } else {
            return false;
        }
    }

    public boolean back() {
        return false;
    }

    public void load() {
    }

    private void imports() {
        try {
            conn = new DatabaseConnection();

            conn.connect(jComboBox1.getSelectedItem().toString(),
                    dbuser.getText(),
                    dbpassword.getText(),
                    path.get_Text(false),
                    dbname.getText(), dbprefix.getText(),
                    false);
            conn.setProgressbar(master.getProgressbar());

            if (conn.getConnection().isValid(1000)) {

                qh = new QueryHandler(conn);
                if (users.isSelected()) {
                    importObjects(Context.getUser());
                }

                if (groups.isSelected()) {
                    importObjects(Context.getGroup());
                }

                if (accounts.isSelected()) {
                    importObjects(Context.getAccounts());
                }

                if (contacts.isSelected()) {
                    importObjects(Context.getContact());
                }

                if (accounts.isSelected()) {
                    importObjects(Context.getAccounts());
                }

                if (products.isSelected()) {
                    importObjects(Context.getProduct());
                }
            }


        } catch (Exception exception) {
            Popup.error(exception);
            Log.Debug(exception);
        }
    }
    List<DatabaseObject> errors = new Vector<DatabaseObject>();

    private void importObjects(Context c) {
        try {
            ReturnValue data = qh.clone(c).select(false);
            DatabaseObject[] objs = DatabaseObject.explode(data, DatabaseObject.getObject(c), false, false);
            for (int i = 0; i < objs.length; i++) {
                DatabaseObject databaseObject = objs[i];
                try {
                    if (!databaseObject.saveImport()) {
                        errors.add(databaseObject);
                    } else {
                        imports++;
                    }
                } catch (Exception e) {
                    errors.add(databaseObject);
                }
            }
        } catch (NodataFoundException nodataFoundException) {
            Log.Debug(this, nodataFoundException.getMessage());
        }
    }
}
