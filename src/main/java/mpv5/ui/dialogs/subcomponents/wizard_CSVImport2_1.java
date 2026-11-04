package mpv5.ui.dialogs.subcomponents;

import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import javax.swing.DefaultListModel;
import javax.swing.filechooser.FileFilter;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.db.common.NodataFoundException;
import mpv5.db.objects.*;
import mpv5.logging.Log;

import mpv5.ui.dialogs.Popup;
import mpv5.ui.dialogs.WizardMaster;
import mpv5.ui.dialogs.Wizardable;
import mpv5.utils.files.FileReaderWriter;
import mpv5.utils.models.MPTableModel;
import mpv5.utils.numberformat.FormatNumber;
import mpv5.utils.tables.ExcelAdapter;
import mpv5.utils.tables.TableFormat;
import mpv5.utils.text.RandomText;
import mpv5.utils.ui.TextFieldUtils;

/**
 *
 *
 */
public class wizard_CSVImport2_1 extends javax.swing.JPanel implements Wizardable {

    private static final long serialVersionUID = -8347532498124147821L;
    private WizardMaster master;

    public wizard_CSVImport2_1(WizardMaster w) {
        this.master = w;
        initComponents();
        jTable1.setModel(new MPTableModel(new Object[0][0]));
        labeledTextChooser1.setFilter(new FileFilter() {

            @Override
            public boolean accept(File f) {
                return (f.isDirectory() || ((f.canRead()) && (f.getName().endsWith("csv"))));
            }

            @Override
            public String getDescription() {
                return "Importable files";
            }
        });

        setContactFields();
        jTable1.getTableHeader().setReorderingAllowed(false);
        jTable1.getTableHeader().setResizingAllowed(true);

        labeledCombobox1.setModel(Context.getImportableContexts(), true);
        labeledCombobox1.getComboBox().addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                setContext();
            }
        });

        new ExcelAdapter(jTable1);
        labeledTextChooser1.setModalityParent(this);
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        labeledTextChooser1 = new mpv5.ui.beans.LabeledTextChooser();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        labeledTextField1 = new mpv5.ui.beans.LabeledTextField();
        jScrollPane2 = new javax.swing.JScrollPane();
        jList1 = new javax.swing.JList();
        jButton3 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jRadioButton1 = new javax.swing.JRadioButton();
        jRadioButton2 = new javax.swing.JRadioButton();
        jRadioButton3 = new javax.swing.JRadioButton();
        labeledCombobox1 = new mpv5.ui.beans.LabeledCombobox();

        //\$2java.awt.Color(255, 255, 255));
        setName("Form"); // NOI18N
        setLayout(new java.awt.BorderLayout());

        jTabbedPane1.setName("jTabbedPane1"); // NOI18N

        jPanel1.setName("jPanel1"); // NOI18N

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        jTable1.setAutoCreateRowSorter(true);
        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        jTable1.setName("jTable1"); // NOI18N
        jScrollPane1.setViewportView(jTable1);

        java.util.ResourceBundle bundle = mpv5.i18n.LanguageManager.getBundle(); // NOI18N
        labeledTextChooser1.set_Label(bundle.getString("wizard_CSVImport2_1.labeledTextChooser1._Label")); // NOI18N
        labeledTextChooser1.setName("labeledTextChooser1"); // NOI18N

        jButton1.setText(bundle.getString("wizard_CSVImport2_1.jButton1.text")); // NOI18N
        jButton1.setName("jButton1"); // NOI18N
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton2.setText(bundle.getString("wizard_CSVImport2_1.jButton2.text")); // NOI18N
        jButton2.setName("jButton2"); // NOI18N
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        labeledTextField1.set_Label(bundle.getString("wizard_CSVImport2_1.labeledTextField1._Label")); // NOI18N
        labeledTextField1.set_Text(bundle.getString("wizard_CSVImport2_1.labeledTextField1._Text")); // NOI18N
        labeledTextField1.setName("labeledTextField1"); // NOI18N

        jScrollPane2.setName("jScrollPane2"); // NOI18N

        jList1.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { " " };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        jList1.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jList1.setName("jList1"); // NOI18N
        jScrollPane2.setViewportView(jList1);

        jButton3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/16/arrow-down.png"))); // NOI18N
        jButton3.setName("jButton3"); // NOI18N
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        jButton4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/16/arrow-up.png"))); // NOI18N
        jButton4.setName("jButton4"); // NOI18N
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        buttonGroup1.add(jRadioButton1);
        jRadioButton1.setText(bundle.getString("wizard_CSVImport2_1.jRadioButton1.text")); // NOI18N
        jRadioButton1.setName("jRadioButton1"); // NOI18N
        jRadioButton1.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jRadioButton1ItemStateChanged(evt);
            }
        });
        jRadioButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButton1ActionPerformed(evt);
            }
        });

        buttonGroup1.add(jRadioButton2);
        jRadioButton2.setSelected(true);
        jRadioButton2.setText(bundle.getString("wizard_CSVImport2_1.jRadioButton2.text")); // NOI18N
        jRadioButton2.setName("jRadioButton2"); // NOI18N
        jRadioButton2.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jRadioButton2ItemStateChanged(evt);
            }
        });
        jRadioButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButton2ActionPerformed(evt);
            }
        });

        buttonGroup1.add(jRadioButton3);
        jRadioButton3.setText(bundle.getString("wizard_CSVImport2_1.jRadioButton3.text")); // NOI18N
        jRadioButton3.setName("jRadioButton3"); // NOI18N
        jRadioButton3.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jRadioButton3ItemStateChanged(evt);
            }
        });
        jRadioButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButton3ActionPerformed(evt);
            }
        });

        labeledCombobox1.set_Label(bundle.getString("wizard_CSVImport2_1.labeledCombobox1._Label")); // NOI18N
        labeledCombobox1.setEnabled(false);
        labeledCombobox1.setName("labeledCombobox1"); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 133, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton4, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 454, Short.MAX_VALUE))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addComponent(labeledTextChooser1, javax.swing.GroupLayout.DEFAULT_SIZE, 328, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(labeledTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton1))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jRadioButton1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jRadioButton2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jRadioButton3, javax.swing.GroupLayout.DEFAULT_SIZE, 182, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(labeledCombobox1, javax.swing.GroupLayout.PREFERRED_SIZE, 221, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jButton4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton3))
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 271, Short.MAX_VALUE)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 248, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(labeledCombobox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jRadioButton3)
                    .addComponent(jRadioButton2)
                    .addComponent(jRadioButton1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(labeledTextChooser1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(labeledTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton2)
                    .addComponent(jButton1))
                .addContainerGap())
        );

        jTabbedPane1.addTab(bundle.getString("wizard_CSVImport2_1.jPanel1.TabConstraints.tabTitle"), jPanel1); // NOI18N

        add(jTabbedPane1, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void setContext() {
        if (jRadioButton1.isSelected()) {
            setProductFields();
        } else if (jRadioButton2.isSelected()) {
            setContactFields();
        } else if (jRadioButton3.isSelected()) {
            setFields();
        }
    }
    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed

        master.setCursor(Cursor.WAIT_CURSOR);
        showm();

}//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        File f = new File(labeledTextChooser1.get_Text(true));
        master.setCursor(Cursor.WAIT_CURSOR);
        readcsv(f);
        master.setCursor(Cursor.DEFAULT_CURSOR);
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        try {
            DefaultListModel m = (DefaultListModel) jList1.getModel();
            Object o = m.getElementAt(jList1.getSelectedIndex());
            Object old = m.getElementAt(jList1.getSelectedIndex() + 1);
            int pos = jList1.getSelectedIndex();
            m.set(pos + 1, o);
            m.set(pos, old);
            jList1.setModel(m);
            jList1.setSelectedIndex(pos + 1);
        } catch (Exception e) {
        }
}//GEN-LAST:event_jButton3ActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed

        try {
            DefaultListModel m = (DefaultListModel) jList1.getModel();
            Object o = m.getElementAt(jList1.getSelectedIndex());
            Object old = m.getElementAt(jList1.getSelectedIndex() - 1);
            int pos = jList1.getSelectedIndex();
            m.set(pos - 1, o);
            m.set(pos, old);
            jList1.setModel(m);
            jList1.setSelectedIndex(pos - 1);
        } catch (Exception e) {
        }
}//GEN-LAST:event_jButton4ActionPerformed

    private void jRadioButton1ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jRadioButton1ItemStateChanged
        jRadioButton2ItemStateChanged(evt);
}//GEN-LAST:event_jRadioButton1ItemStateChanged

    private void jRadioButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButton1ActionPerformed
}//GEN-LAST:event_jRadioButton1ActionPerformed

    private void jRadioButton2ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jRadioButton2ItemStateChanged
        setContext();
}//GEN-LAST:event_jRadioButton2ItemStateChanged

    private void jRadioButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButton2ActionPerformed
}//GEN-LAST:event_jRadioButton2ActionPerformed

    private void jRadioButton3ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jRadioButton3ItemStateChanged
        setContext();
        labeledCombobox1.setEnabled(jRadioButton3.isSelected());
    }//GEN-LAST:event_jRadioButton3ItemStateChanged

    private void jRadioButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButton3ActionPerformed
    }//GEN-LAST:event_jRadioButton3ActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JList jList1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JRadioButton jRadioButton1;
    private javax.swing.JRadioButton jRadioButton2;
    private javax.swing.JRadioButton jRadioButton3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTable jTable1;
    private mpv5.ui.beans.LabeledCombobox labeledCombobox1;
    private mpv5.ui.beans.LabeledTextChooser labeledTextChooser1;
    private mpv5.ui.beans.LabeledTextField labeledTextField1;
    // End of variables declaration//GEN-END:variables

    public boolean next() {
        if (labeledTextChooser1.hasText()) {
            master.getStore().addProperty("file", labeledTextChooser1.get_Text(true));
//            master.getStore().addProperty("separator", jComboBox1.getSelectedItem().toString());
            master.getNext().load();
            return true;
        } else {
            return false;
        }
    }

    public boolean back() {
        return false;
    }

    public void load() {
    }

    private void readcsv(File f) {
        String[] lines;
        Log.Debug(this, "Reading : " + f);
        MPTableModel mo;
        if (f.isFile()) {

            String separator = labeledTextField1.getText();
            if (separator.length() > 0) {

                FileReaderWriter r = new FileReaderWriter(f);
                lines = r.readLines();
                String[][] data = new String[lines.length][];
                if (lines != null && lines.length > 0) {

                    for (int i = 0; i < lines.length; i++) {
                        String[] line = lines[i].split(separator);
                        data[i] = line;
                    }
                } else {
                    Log.Debug(this, "No lines found in file : " + f);
                }

                mo = new MPTableModel(data);
                mo.setEditable(true);
                jTable1.setModel(mo);
            } else {
                Log.Debug(this, "No separator ");
                TextFieldUtils.blinkerRed(labeledTextField1);
            }
        } else {
            Log.Debug(this, "Not a file : " + f);
        }
    }

    private void showm() {
        final Context t;
        if (jRadioButton1.isSelected()) {
            t = Context.getProduct();
        } else if (jRadioButton2.isSelected()) {
            t = Context.getContact();
        } else {
            t = (Context) labeledCombobox1.getSelectedItem().getIdObject();
        }
        Runnable runnable = new Runnable() {

            @Override
            public void run() {
                int count = 0;
                try {
                    setCursor(new Cursor(Cursor.WAIT_CURSOR));

                    TableFormat.stopEditing(jTable1);

                    Object[][] data = ((MPTableModel) jTable1.getModel()).getData();
                    Object[] keys = ((DefaultListModel) jList1.getModel()).toArray();
                    count = 0;

                    for (int i = 1; i < data.length; i++) {
                        Object[] line = data[i];
                        DatabaseObject c = DatabaseObject.getObject(t);
                        for (int j = 0; j < keys.length; j++) {
                            Object key = keys[j];
                            if (j < line.length && line[j] != null && line[j].toString().length() > 0) {
                                try {
                                    line[j] = line[j].toString().replace("\"", "");
//                                Log.Debug(this, "Setting " + key + " to " + line[j]);
                                    if (key.toString().equals("TAX")) {
                                        int taxid = Tax.getTaxId(FormatNumber.parseDezimal(line[j].toString().trim()));
                                        if (taxid > 0) {
                                            ((Product) c).setTaxids(taxid);
                                        } else {
                                            Tax tax = new Tax();
                                            tax.setTaxvalue(new BigDecimal(line[j].toString().replace("%", "").trim()));
                                            tax.setIdentifier("autogenerated_" + RandomText.getNumberText());
                                            tax.setCountry(User.getCurrentUser().__getDefcountry());
                                            tax.saveImport();
                                            ((Product) c).setTaxids(tax.__getIDS());
                                        }
                                    } else if (key.toString().equals("NETBUYPRICE")) {
                                        ((Product) c).setInternalnetvalue(FormatNumber.parseDezimal(line[j].toString().trim()));

                                    } else if (key.toString().equals("NETSALEPRICE")) {
                                        ((Product) c).setExternalnetvalue(FormatNumber.parseDezimal(line[j].toString().trim()));

                                    } else if (key.toString().equals("SUPPLIER")) {
                                        Contact sup = null;
                                        try {
                                            sup = (Contact) DatabaseObject.getObject(Context.getContact(), line[j].toString());
                                        } catch (NodataFoundException nodataFoundException) {
                                        }
                                        if (sup != null) {
                                            ((Product) c).setSuppliersids(sup.__getIDS());
                                        } else {
                                            Contact con = new Contact();
                                            con.setCname(line[j].toString());
                                            con.setisSupplier(true);
                                            con.saveImport();
                                            ((Product) c).setSuppliersids(con.__getIDS());
                                        }

                                    } else if (key.toString().equals("MANUFACTURER")) {
                                        Contact sup = null;
                                        try {
                                            sup = (Contact) DatabaseObject.getObject(Context.getContact(), line[j].toString());
                                        } catch (NodataFoundException nodataFoundException) {
                                        }
                                        if (sup != null) {
                                            ((Product) c).setManufacturersids(sup.__getIDS());
                                        } else {
                                            Contact con = new Contact();
                                            con.setCname(line[j].toString());
                                            con.setisSupplier(true);
                                            con.saveImport();
                                            ((Product) c).setManufacturersids(con.__getIDS());
                                        }
                                    } else if (key.toString().equals("GROUP")) {
                                        try {
                                            Group p = (Group) DatabaseObject.getObject(Context.getGroup(), line[j].toString());
                                            c.setGroupsids(p.__getIDS());
                                        } catch (NodataFoundException nodataFoundException) {
                                            Group con = new Group();
                                            con.setCname(line[j].toString());
                                            con.setGroupsids(1);
                                            con.saveImport();
                                            c.setGroupsids(con.__getIDS());
                                        }
                                    } else {
                                        try {
                                            c.parse(key.toString(), line[j]);
                                        } catch (Exception exception) {
                                            Log.Debug(this, key + ": " + line[j] + " ->" + exception.getMessage());
                                        }
                                    }
                                } catch (IndexOutOfBoundsException ex) {
//                                Log.Debug(ex);
                                } catch (Exception ex) {
                                    Log.Debug(ex);
                                }
                            }
                        }
                        c.saveImport();
                        count++;
                    }
                } catch (Exception e) {
                    Popup.error(e);
                    Log.Debug(e);
                } finally {
                    setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                    Popup.notice(count + " rows imported.");
                    master.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                    master.isEnd(true);
                }
            }
        };
        if (t != null) {
            new Thread(runnable).start();
        }
    }

    private void setContactFields() {
        DefaultListModel m = new DefaultListModel();
        List<Method> p = new ArrayList<Method>(new Contact().setVars().values());

        for (int i = 0; i < p.size(); i++) {
            Method method = p.get(i);
            if (!method.getName().toLowerCase().startsWith("setint")
                    && !method.getName().toLowerCase().startsWith("setdate")
                    && !method.getName().toLowerCase().endsWith("ids")
                    && !method.getName().toLowerCase().startsWith("setgroup")) {
                m.addElement(method.getName().toUpperCase().substring(3));
            }
        }

        m.addElement("GROUP");

        jList1.setModel(m);
    }

    private void setProductFields() {

        DefaultListModel m = new DefaultListModel();
        List<Method> p = new ArrayList<Method>(new Product().setVars().values());

        for (int i = 0; i < p.size(); i++) {
            Method method = p.get(i);
            if (!method.getName().toLowerCase().startsWith("setint")
                    && !method.getName().toLowerCase().startsWith("setdate")
                    && !method.getName().toLowerCase().endsWith("ids")
                    && !method.getName().toLowerCase().endsWith("image")
                    && !method.getName().toLowerCase().startsWith("setexternal")
                    && !method.getName().toLowerCase().startsWith("setgroup")) {
                m.addElement(method.getName().toUpperCase().substring(3));
            }
        }

        m.addElement("TAX");
        m.addElement("SUPPLIER");
        m.addElement("MANUFACTURER");
        m.addElement("NETBUYPRICE");
        m.addElement("NETSALEPRICE");

        m.addElement("GROUP");
        jList1.setModel(m);
    }

    private void setFields() {
        Context c = (Context) labeledCombobox1.getSelectedItem().getIdObject();
        if (c != null) {
            DefaultListModel m = new DefaultListModel();
            List<Method> vars = new ArrayList<Method>(DatabaseObject.getObject(c).setVars().values());
            for (int i = 0; i < vars.size(); i++) {
                Method method = vars.get(i);
                if (!method.getName().toLowerCase().startsWith("setint")
                        && !method.getName().toLowerCase().startsWith("setdate")
                        && !method.getName().toLowerCase().endsWith("ids")
                        && !method.getName().toLowerCase().endsWith("image")
                        && !method.getName().toLowerCase().startsWith("setexternal")
                        && !method.getName().toLowerCase().startsWith("setgroup")) {
                    m.addElement(method.getName().toUpperCase().substring(3));
                }
            }

            m.addElement("GROUP");
            jList1.setModel(m);
        }
    }
}
