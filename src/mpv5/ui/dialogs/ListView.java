/*
 *  This file is part of YaBS.
 *
 *  YaBS is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  YaBS is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with YaBS.  If not, see <http://www.gnu.org/licenses/>.
 */
/*
 * ListView.java
 *
 * Created on 08.11.2009, 20:13:52
 */
package mpv5.ui.dialogs;

import java.awt.Cursor;
import java.util.ArrayList;
import java.util.Vector;
import javax.swing.ListModel;
import javax.swing.SwingUtilities;
import mpv5.data.MPList;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.db.common.NodataFoundException;
import mpv5.globals.Messages;
import mpv5.logging.Log;
import mpv5.ui.frames.MPView;
import mpv5.ui.panels.DataPanel;
import mpv5.usermanagement.MPSecurityManager;
import mpv5.utils.date.DateConverter;
import mpv5.utils.xml.XMLReader;
import mpv5.utils.xml.XMLWriter;

/**
 *
 * @author andreasw
 */
public class ListView extends javax.swing.JPanel {

    private final MPList list;

    public void addElement(DatabaseObject obj) {
        list.add(obj);
        validate();
    }

    /** Creates new form ListView
     * @param list
     */
    public ListView(MPList list) {
        initComponents();
        jList1.setCellRenderer(MPList.getDBORenderer());
        this.list = list;
        validate();
    }

    @Override
    public void validate() {
        jList1.setModel(list.getListModel());
        super.validate();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        jList1 = new javax.swing.JList();
        jToolBar1 = new javax.swing.JToolBar();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jButton5 = new javax.swing.JButton();

        setName("Form"); // NOI18N

        jScrollPane1.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        jScrollPane1.setName("jScrollPane1"); // NOI18N

        jList1.setName("jList1"); // NOI18N
        jList1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jList1MouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(jList1);

        jToolBar1.setRollover(true);
        jToolBar1.setName("jToolBar1"); // NOI18N

        java.util.ResourceBundle bundle = mpv5.i18n.LanguageManager.getBundle(); 
        jButton1.setText(bundle.getString("ListView.jButton1.text_1")); // NOI18N
        jButton1.setName("jButton1"); // NOI18N
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        jToolBar1.add(jButton1);

        jButton2.setText(bundle.getString("ListView.jButton2.text_1")); // NOI18N
        jButton2.setName("jButton2"); // NOI18N
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });
        jToolBar1.add(jButton2);

        jButton3.setText(bundle.getString("ListView.jButton3.text_1")); // NOI18N
        jButton3.setName("jButton3"); // NOI18N
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });
        jToolBar1.add(jButton3);

        jButton4.setText(bundle.getString("ListView.jButton4.text")); // NOI18N
        jButton4.setFocusable(false);
        jButton4.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton4.setName("jButton4"); // NOI18N
        jButton4.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });
        jToolBar1.add(jButton4);

        jButton5.setText(bundle.getString("ListView.jButton5.text")); // NOI18N
        jButton5.setFocusable(false);
        jButton5.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton5.setName("jButton5"); // NOI18N
        jButton5.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton5ActionPerformed(evt);
            }
        });
        jToolBar1.add(jButton5);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jToolBar1, javax.swing.GroupLayout.DEFAULT_SIZE, 330, Short.MAX_VALUE)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 330, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(jToolBar1, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 226, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        try {
            list.remove((DatabaseObject) jList1.getSelectedValue());
            validate();
        } catch (Exception e) {
            Log.Debug(this, e);
        }
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        list.clear();
        validate();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        DataPanel view = MPView.identifierView.getCurrentTab();
        if (view != null) {
            view.paste(list.toArray());
            try {
                BigPopup.hide(this);
            } catch (Exception exception) {
            }
        }
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jList1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jList1MouseClicked
        if (evt.getClickCount() > 1) {
            try {
                if (jList1.getSelectedValue() != null) {
                    MPView.identifierView.addTab((DatabaseObject) jList1.getSelectedValue());
                }
            } catch (Exception e) {
                Log.Debug(e);
            }
        }
    }//GEN-LAST:event_jList1MouseClicked

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        ListModel data;

        if (mpv5.usermanagement.MPSecurityManager.check(Context.getContact(), MPSecurityManager.EXPORT)
                && mpv5.usermanagement.MPSecurityManager.check(Context.getItem(), MPSecurityManager.EXPORT)
                && mpv5.usermanagement.MPSecurityManager.check(Context.getProduct(), MPSecurityManager.EXPORT)) {

            XMLWriter xmlw = new XMLWriter();
            xmlw.newDoc(true);
            String name = Messages.ACTION_EXPORT.toString() + "-" + DateConverter.getTodayDefDate();
            data = jList1.getModel();
            ArrayList<Context> exportable = Context.getImportableContexts();
            for (int i = 0; i < exportable.size(); i++) {
                Context context = exportable.get(i);
                ArrayList<DatabaseObject> d = new ArrayList<DatabaseObject>();
                for (int j = 0; j < data.getSize(); j++) {
                    if (((DatabaseObject) data.getElementAt(j)).getContext().equals(context)) {
                        d.add((DatabaseObject) data.getElementAt(j));
                    }
                }
                xmlw.add(d);
            }
            MPView.showFilesaveDialogFor(xmlw.createFile(name));
        }
    }//GEN-LAST:event_jButton4ActionPerformed

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed

        final ListView v = this;
        Runnable runnable = new Runnable() {

            public void run() {
                try {
                    v.setCursor(new Cursor(Cursor.WAIT_CURSOR));
                    MPView.setWaiting(true);
                    DialogForFile d = new DialogForFile(DialogForFile.FILES_ONLY);
                    d.setFileFilter(DialogForFile.XML_FILES);
                    XMLReader x;
                    ArrayList<ArrayList<DatabaseObject>> objs = null;

                    if (d.chooseFile()) {
                        x = new XMLReader();

                        x.newDoc(d.getFile(), true);
                        x.setOverwriteExisting(true);
                        objs = x.getObjects();

                    }

                    if (objs != null && objs.size() > 0) {
                        for (int i = 0; i < objs.size(); i++) {
                            ArrayList<DatabaseObject> arrayList = objs.get(i);
                            for (int j = 0; j < arrayList.size(); j++) {
                                DatabaseObject databaseObject = arrayList.get(j);
                                Log.Debug(this, "Parsing " + databaseObject.getDbIdentity() + " : " + databaseObject.__getCName() + " from file: " + d.getFile());
                                addElement(databaseObject);
                            }
                        }
                    }
                } catch (Exception e) {
                    Popup.error(e);
                    Log.Debug(e);
                } finally {
                    v.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                    MPView.setWaiting(false);
                }
            }
        };
        SwingUtilities.invokeLater(runnable);
    }//GEN-LAST:event_jButton5ActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JList jList1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JToolBar jToolBar1;
    // End of variables declaration//GEN-END:variables
}
