package mpv5.ui.dialogs.subcomponents;

import java.awt.Cursor;
import java.io.File;
import java.util.ArrayList;


import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.text.TableView.TableRow;
import mpv5.db.common.DatabaseObject;
import mpv5.db.objects.Account;
import mpv5.globals.Messages;
import mpv5.logging.Log;
import mpv5.ui.dialogs.Popup;
import mpv5.ui.dialogs.WizardMaster;
import mpv5.ui.dialogs.Wizardable;
import mpv5.utils.models.ImportTableModel;
import mpv5.utils.tables.TableFormat;
import mpv5.utils.xml.XMLReader;

/**
 *
 * 
 */
public class wizard_AccountXMLImport_1 extends javax.swing.JPanel implements Wizardable {

    private static final long serialVersionUID = -8347532498124147821L;
    private WizardMaster master;
    private boolean isConsumed = false;

    public wizard_AccountXMLImport_1(WizardMaster w) {
        this.master = w;
        initComponents();

    }

    private void importXML() {
        master.setCursor(new Cursor(Cursor.WAIT_CURSOR));
        isConsumed = true;
        XMLReader x;
        ArrayList<ArrayList<DatabaseObject>> objs = null;

        if (master.getStore().hasProperty("file")) {
            x = new XMLReader();
            x.setOverwriteExisting(true);
            try {
                x.newDoc(new File(master.getStore().getProperty("file")), false);
                objs = x.getObjects();
                jTable1.setModel(ImportTableModel.getModel(objs, false));
                jLabel2.setText(jLabel2.getText() + " " + master.getStore().getProperty("file") + " (" + jTable1.getRowCount() + ")");
                TableFormat.format(jTable1, 0, 0);
                TableFormat.format(jTable1, 1, 0);
                TableFormat.format(jTable1, 2, 100);
            } catch (Exception ex) {
                Popup.error(ex);
                Log.Debug(ex);
            } finally {
                master.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }
        }
    }

    private void imports() {
        jProgressBar1.setMinimum(0);
        jProgressBar1.setMaximum(jTable1.getRowCount());

        final List<Account> accs = new Vector<Account>();
        final HashMap<Integer, Account> daccs = new HashMap<Integer, Account>();
        Runnable runnable = new Runnable() {

            public void run() {
                int imp = 0;
                String val = null;
                for (int i = 0; i < jTable1.getRowCount(); i++) {
                    master.setCursor(new Cursor(Cursor.WAIT_CURSOR));
                    final int p = i;
                    try {
                        DatabaseObject dog = ((DatabaseObject) jTable1.getValueAt(p, 0));
                        accs.add((Account) dog);

                        val = "<html><p><font color =green>Analysed ID: " + dog.__getIDS();
                        imp++;
                    } catch (Exception e) {
                        val = "<html><p><font color =red>" + Messages.ERROR_OCCURED + ": " + e.getMessage();
                    } finally {
                        jTable1.setValueAt(val, p, 4);
                        jProgressBar1.setValue(p);
                        master.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                    }
                }

                jProgressBar1.setValue(0);
                 master.setCursor(new Cursor(Cursor.WAIT_CURSOR));
                //find lowest parents (children of all accounts)
                for (int j = 0; j < accs.size(); j++) {
                    Account account = accs.get(j);
                    daccs.put(account.__getIDS(), account);
                    account.setIDS(-1);
                }

                for (int i = 0; i < accs.size(); i++) {
                    Account account = accs.get(i);
                    account.setIntparentaccount(findParentAccount(account));
                    account.saveImport();
                    try {
                        val = "<html><p><font color =green>Saved account " + account + " [" + account.__getIDS() +"]";
                    } catch (Exception e) {
                        val = "<html><p><font color =red>" + Messages.ERROR_OCCURED + ": " + e.getMessage();
                    } finally {
                        jTable1.setValueAt(val, i, 4);
                        jProgressBar1.setValue(i);
                        master.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                    }
                }

                jProgressBar1.setString(imp + " " + Messages.IMPORTED);
            }

            //recursion is a powerful, but slow beast..
            private int findParentAccount(Account account) {
                Account pa = daccs.get(account.__getIntparentaccount());
                if (pa == null) {
                    return 1;
                } else if (pa.isExisting()) {
                    return pa.__getIDS();
                } else {
                    pa.setIntparentaccount(findParentAccount(pa));
                    pa.saveImport();
                    return pa.__getIDS();
                }
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
        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("wizard_AccountXMLImport_1.jPanel1.border.title"))); // NOI18N
        jPanel1.setName("jPanel1"); // NOI18N

        jLabel2.setText(bundle.getString("wizard_AccountXMLImport_1.jLabel2.text")); // NOI18N
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
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 343, Short.MAX_VALUE)
                .addComponent(jProgressBar1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel1Layout.createSequentialGroup()
                    .addGap(20, 20, 20)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 337, Short.MAX_VALUE)
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
            Log.Debug(this, "Overwrite is set to " + master.getStore().getProperty("overwrite", false));
            importXML();
        }
    }
}
