package mpv5.ui.dialogs.subcomponents;

import java.awt.Cursor;
import java.io.File;
import java.util.ArrayList;

import mpv5.db.common.DatabaseObject;
import mpv5.logging.Log;
import mpv5.ui.dialogs.WizardMaster;
import mpv5.ui.dialogs.Wizardable;
import mpv5.ui.frames.MPV5View;
import mpv5.utils.models.ImportModel;
import mpv5.utils.xml.XMLReader;

/**
 *
 * @author anti43
 */
public class wizard_XMLImport_2 extends javax.swing.JPanel implements Wizardable {

    private static final long serialVersionUID = -8347532498124147821L;
    private WizardMaster master;

    public wizard_XMLImport_2(WizardMaster w) {
        this.master = w;
        initComponents();

    }

    private void importXML() {
        XMLReader x;
        ArrayList<ArrayList<DatabaseObject>> objs = null;
        ImportModel[] mods;

        if (master.getStore().hasProperty("file")) {
            x = new XMLReader();
            try {
                setCursor(new Cursor(Cursor.WAIT_CURSOR));
                x.newDoc(new File(master.getStore().getProperty("file")), true);
                objs = x.getObjects();

                 jTable1.setModel(ImportModel.getModel(objs));

            } catch (Exception ex) {
                MPV5View.addMessage(ex.getMessage());
                Log.Debug(ex);
            } finally {
                setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }
        }
//
//
//        if (objs != null && objs.size() < 0) {
//        } else {
//            Popup.notice(Messages.NO_DATA_FOUND);
//        }
//      for (int i = 0; i < objs.size(); i++) {
//                ArrayList<DatabaseObject> arrayList = objs.get(i);
//                for (int j = 0; j < arrayList.size(); j++) {
//                    DatabaseObject databaseObject = arrayList.get(j);
//                    Log.Debug(this, "Parsing " + databaseObject.getDbIdentity() + " : " + databaseObject.__getCName() + " from file: " + master.getStore().getProperty("file"));
//                    databaseObject.save();
//                }
//            }
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();

        setBackground(new java.awt.Color(255, 255, 255));
        setName("Form"); // NOI18N
        setLayout(new java.awt.BorderLayout());

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("mpv5/resources/languages/Panels"); // NOI18N
        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("wizard_XMLImport_2.jPanel1.border.title"))); // NOI18N
        jPanel1.setName("jPanel1"); // NOI18N

        jLabel1.setText(bundle.getString("wizard_XMLImport_2.jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        jTextField1.setText(bundle.getString("wizard_XMLImport_2.jTextField1.text")); // NOI18N
        jTextField1.setName("jTextField1"); // NOI18N

        jLabel2.setText(bundle.getString("wizard_XMLImport_2.jLabel2.text")); // NOI18N
        jLabel2.setName("jLabel2"); // NOI18N

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {},
                {},
                {},
                {}
            },
            new String [] {

            }
        ));
        jTable1.setName("jTable1"); // NOI18N
        jScrollPane1.setViewportView(jTable1);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 399, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 182, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jLabel2))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 318, Short.MAX_VALUE))
        );

        add(jPanel1, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JTextField jTextField1;
    // End of variables declaration//GEN-END:variables

    public boolean next() {
        importXML();
        return false;
    }

    public boolean back() {
        return true;
    }

    public void load() {
       importXML();
    }
}
