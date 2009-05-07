package mpv5.ui.dialogs.subcomponents;

import java.awt.Cursor;
import java.io.File;
import java.util.ArrayList;


import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.text.TableView.TableRow;
import mpv5.db.common.DatabaseObject;
import mpv5.globals.Messages;
import mpv5.logging.Log;
import mpv5.ui.dialogs.Popup;
import mpv5.ui.dialogs.WizardMaster;
import mpv5.ui.dialogs.Wizardable;
import mpv5.utils.models.ImportModel;
import mpv5.utils.tables.TableFormat;
import mpv5.utils.xml.XMLReader;

/**
 *
 * @author anti43
 */
public class wizard_XMLImport_2 extends javax.swing.JPanel implements Wizardable {

    private static final long serialVersionUID = -8347532498124147821L;
    private WizardMaster master;
    private boolean isConsumed = false;

    public wizard_XMLImport_2(WizardMaster w) {
        this.master = w;
        initComponents();

    }

    private void importXML() {
        isConsumed = true;
        XMLReader x;
        ArrayList<ArrayList<DatabaseObject>> objs = null;

        if (master.getStore().hasProperty("file")) {
            x = new XMLReader();
            try {
                setCursor(new Cursor(Cursor.WAIT_CURSOR));
                x.newDoc(new File(master.getStore().getProperty("file")), true);
                objs = x.getObjects();
                jTable1.setModel(ImportModel.getModel(objs));
                jLabel2.setText(jLabel2.getText() + " " + master.getStore().getProperty("file"));
                TableFormat.format(jTable1, 0, 33);
                TableFormat.format(jTable1, 1, 33);
                TableFormat.format(jTable1, 2, 100);

            } catch (Exception ex) {
                Popup.error(ex);
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

    private void imports() {
//        final ImportModel m = (ImportModel) jTable1.getModel();
        for (int i = 0; i < jTable1.getRowCount(); i++) {
            if ((Boolean) jTable1.getValueAt(i, 1)) {
                final int p = i;
                SwingWorker<String, Void> runnable = new SwingWorker<String, Void>() {

                    @Override
                    public void done() {
                        String s;
                        try {
                            s=get();
                            jTable1.setValueAt(s, p, 4);
                        } catch (InterruptedException ex) {
                            Logger.getLogger(wizard_XMLImport_2.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (ExecutionException ex) {
                            Logger.getLogger(wizard_XMLImport_2.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }

                    @Override
                    protected String doInBackground() throws Exception {
                       String val;
                        try {
                            if (!((DatabaseObject) jTable1.getValueAt(p, 0)).saveImport()) {
                                val = "<html><p><font color =red>" + Messages.ERROR_OCCURED;
                            } else {
                                val = "<html><p><font color =green>" + Messages.IMPORTED;
                            }
                        } catch (Exception e) {
                            val = "<html><p><font color =red>" + Messages.ERROR_OCCURED + ": " + e.getMessage();
                        } finally {
                        }
                        return val;
                    }

                };
             runnable.execute();
            }
        }
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jTable1 = new javax.swing.JTable();
        jLabel2 = new javax.swing.JLabel();

        setBackground(new java.awt.Color(255, 255, 255));
        setName("Form"); // NOI18N
        setLayout(new java.awt.BorderLayout());

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("mpv5/resources/languages/Panels"); // NOI18N
        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("wizard_XMLImport_2.jPanel1.border.title"))); // NOI18N
        jPanel1.setName("jPanel1"); // NOI18N

        jTable1.setBorder(javax.swing.BorderFactory.createEtchedBorder());
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

        jLabel2.setText(bundle.getString("wizard_XMLImport_2.jLabel2.text")); // NOI18N
        jLabel2.setName("jLabel2"); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, 399, Short.MAX_VALUE)
                .addContainerGap())
            .addComponent(jTable1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 423, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTable1, javax.swing.GroupLayout.DEFAULT_SIZE, 357, Short.MAX_VALUE))
        );

        add(jPanel1, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JTable jTable1;
    // End of variables declaration//GEN-END:variables

    public boolean next() {

        imports();
        master.isEnd(true);
        return false;
    }

    public boolean back() {
        return true;
    }

    /**
     *
     */
    public void load() {
        if (!isConsumed) {
            importXML();
        }
    }
}
