package mpv5.ui.dialogs.subcomponents;

import java.awt.Cursor;
import java.io.File;


import java.util.List;

import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.db.common.NodataFoundException;
import mpv5.db.objects.ProductGroup;
import mpv5.globals.Messages;
import mpv5.logging.Log;
import mpv5.ui.dialogs.Popup;
import mpv5.ui.dialogs.WizardMaster;
import mpv5.ui.dialogs.Wizardable;
import mpv5.utils.arrays.ArrayUtilities;
import mpv5.utils.csv.CSVReaderWriter;
import mpv5.utils.models.MPTableModel;

/**
 *
 * 
 */
public class wizard_CSV_ProductGroups_Import_1 extends javax.swing.JPanel implements Wizardable {

    private static final long serialVersionUID = -8347532498124147821L;
    private WizardMaster master;
    private boolean isConsumed = false;

    public wizard_CSV_ProductGroups_Import_1(WizardMaster w) {
        this.master = w;
        initComponents();

    }

    @SuppressWarnings("unchecked")
    private void importCsv() {
        master.setCursor(new Cursor(Cursor.WAIT_CURSOR));
        isConsumed = true;
        CSVReaderWriter x;
        Object[][] arr;

        if (master.getStore().hasProperty("file")) {
            x = new CSVReaderWriter(new File(master.getStore().getProperty("file")), master.getStore().getProperty("separator"));
            List<Object[]> d = x.read();
            try {
                d = ArrayUtilities.removeDuplicates(d);
                arr = ArrayUtilities.listToTableArray(d);
                arr = ArrayUtilities.inserValue(arr, null, null);

                jTable1.setModel(new MPTableModel(arr));
                jLabel2.setText(jLabel2.getText() + " " + master.getStore().getProperty("file") + " (" + jTable1.getRowCount() + ")");

            } catch (Exception ex) {
                Popup.error(ex);
                Log.Debug(ex);
            } finally {
                master.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
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
//                    Log.Debug(this, "Parsing " + databaseObject.getDbIdentity() + " : " + databaseObject.__getCname() + " from file: " + master.getStore().getProperty("file"));
//                    databaseObject.save();
//                }
//            }
    }

    private void imports() {

        jProgressBar1.setMinimum(0);
        jProgressBar1.setMaximum(jTable1.getRowCount() * jTable1.getColumnCount());


        Runnable runnable = new Runnable() {

            public void run() {
                int imp = 0;

                for (int i = 0; i < jTable1.getRowCount(); i++) {
                    master.setCursor(new Cursor(Cursor.WAIT_CURSOR));

                    for (int j = 0; j < jTable1.getColumnCount() - 1; j++) {
                        final int p = i;
                        boolean existing = false;
                        String val = null;
                        try {
                            if (jTable1.getValueAt(p, j) != null) {
                                ProductGroup dog = null;
                                try {
                                    Log.Debug(this, "Checking for group: " + jTable1.getValueAt(p, j));
                                    dog = (ProductGroup) DatabaseObject.getObject(Context.getProductGroup(), jTable1.getValueAt(p, j).toString());
                                    existing = true;
                                    Log.Debug(this, "Already existing: " + jTable1.getValueAt(p, j));
                                } catch (NodataFoundException nodataFoundException) {
                                    Log.Debug(this, "Create a new group: " + jTable1.getValueAt(p, j));
                                    dog = new ProductGroup(jTable1.getValueAt(p, j).toString());
                                    dog.setProductgroupsids(1);
                                }

                                if (j > 0) {
                                    try {
                                        Log.Debug(this, "Checking for parent group: " + jTable1.getValueAt(p, j - 1));
                                        dog.setProductgroupsids(DatabaseObject.getObject(Context.getProductGroup(), jTable1.getValueAt(p, j - 1).toString()).__getIDS());
                                    } catch (NodataFoundException nodataFoundException1) {
                                        throw new IllegalStateException("Parent unknown: " + jTable1.getValueAt(p, j - 1));
                                    }
                                }

                                if (!existing) {
                                    if (!dog.saveImport()) {
                                        val = "<html><p><font color =red>" + Messages.ERROR_OCCURED;
                                    } else {
                                        val = "<html><p><font color =green>" + Messages.IMPORTED + " ID: " + dog.__getIDS();
                                        imp++;
                                    }
                                } else {
                                    val = "<html><p><font color =blue> Existing ID: " + dog.__getIDS();
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            val = "<html><p><font color =red>" + Messages.ERROR_OCCURED + ": " + e.getMessage();
                        } finally {
                            if (val != null) {
                                jTable1.setValueAt(val, p, jTable1.getColumnCount() - 1);
                            }
                            jProgressBar1.setValue(p * j);
                            master.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                        }
                    }

                }
                jProgressBar1.setString(imp + " " + Messages.IMPORTED);
            }
        };
        new Thread(runnable).start();

        master.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        master.isEnd(true);
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jProgressBar1 = new javax.swing.JProgressBar();

        //\$2java.awt.Color(255, 255, 255));
        setName("Form"); // NOI18N
        setLayout(new java.awt.BorderLayout());

//$2java.awt.Color(255, 255, 255));
        java.util.ResourceBundle bundle = mpv5.i18n.LanguageManager.getBundle();
        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("wizard_CSV_ProductGroups_Import_1.jPanel1.border.title"))); // NOI18N
        jPanel1.setName("jPanel1"); // NOI18N

        jLabel2.setText(bundle.getString("wizard_CSV_ProductGroups_Import_1.jLabel2.text")); // NOI18N
        jLabel2.setName("jLabel2"); // NOI18N

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        jTable1.setAutoCreateRowSorter(true);
        jTable1.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null}
            },
            new String [] {
                "Title 1"
            }
        ));
        jTable1.setCellSelectionEnabled(true);
        jTable1.setDoubleBuffered(true);
        jTable1.setFillsViewportHeight(true);
        jTable1.setName("jTable1"); // NOI18N
        jScrollPane1.setViewportView(jTable1);

        jProgressBar1.setName("jProgressBar1"); // NOI18N
        jProgressBar1.setStringPainted(true);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, 399, Short.MAX_VALUE)
                .addContainerGap())
            .addComponent(jProgressBar1, javax.swing.GroupLayout.DEFAULT_SIZE, 423, Short.MAX_VALUE)
            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 423, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 342, Short.MAX_VALUE)
                .addComponent(jProgressBar1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel1Layout.createSequentialGroup()
                    .addGap(20, 20, 20)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 336, Short.MAX_VALUE)
                    .addGap(21, 21, 21)))
        );

        add(jPanel1, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JProgressBar jProgressBar1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    // End of variables declaration//GEN-END:variables

    public boolean next() {
        try {
            imports();
        } catch (Exception e) {
            Log.Debug(e);
            master.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        }
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
            importCsv();
        }
    }
}
