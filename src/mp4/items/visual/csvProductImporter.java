/*
 * productImporter.java
 *
 * Created on 27. Januar 2008, 21:56
 */
package mp4.items.visual;

import com.Ostermiller.util.CSVParser;
import java.awt.Cursor;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;
import mp4.globals.Strings;

import mp4.interfaces.ContactPanel;
import mp4.items.People;

import mp4.items.Steuersatz;
import mp4.items.visual.Help;
import mp4.logs.*;
import mp4.items.visual.Popup;
import mp4.datenbank.verbindung.ConnectionHandler;
import mp4.interfaces.DataPanel;
import mp4.items.Hersteller;
import mp4.items.visual.SupplierPicker;

import mp4.items.ProductImporteur;
import mp4.items.HistoryItem;
import mp4.items.Product;
import mp4.items.ProductGroupCategory;
import mp4.items.ProductGroupFamily;
import mp4.items.ProductGroupGroup;
import mp4.items.handler.ProductGroupHandler;
import mp4.items.Lieferant;
import mp4.utils.tabellen.models.DefaultHelpModel;
import mp4.utils.ui.Position;
import org.supercsv.cellprocessor.constraint.StrMinMax;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.exception.SuperCSVException;
import org.supercsv.io.CsvBeanReader;
import org.supercsv.io.ICsvBeanReader;
import org.supercsv.prefs.CsvPreference;

/**
 *
 * @author  anti43
 */
public class csvProductImporter extends javax.swing.JFrame implements ContactPanel, DataPanel {

    private static csvProductImporter frame;

    public static void instanceOf() {

        if (frame == null) {
            frame = new csvProductImporter();
        }

        frame.setVisible(true);
    }
    private CSVParser p;
    private ArrayList liste;
    private String[][] datstr;
    private String[] header;
    public ProductImporteur[] data;
    public Lieferant supplier = null;
    private Task task;
    private Hersteller hersteller = null;

    /** Creates new form productImporter */
    public csvProductImporter() {
        initComponents();
        new Position(this);

    }

    @SuppressWarnings("unchecked")
    public csvProductImporter(File file) {
        initComponents();
        new Position(this);
        this.jTextField1.setText(file.getPath());
        this.setVisible(rootPaneCheckingEnabled);
        boolean succ = true;
        ProductImporteur user = new ProductImporteur();
        liste = new ArrayList();
        header = new String[]{"produktnummer", "name", "text", "vk",
                    "ek", "tax", "herstellerid", "warengruppenkategorie", "warengruppenfamilie",
                    "warengruppe", "url", "ean", "lieferantenid"
                };

        try {

            final CellProcessor[] processors = new CellProcessor[]{
                new StrMinMax(0, 49),
                new StrMinMax(0, 49),
                new StrMinMax(0, 499),
                new StrMinMax(0, 99),
                new StrMinMax(0, 99),
                new StrMinMax(0, 99),
                new StrMinMax(0, 99),
                new StrMinMax(0, 99),
                new StrMinMax(0, 99),
                new StrMinMax(0, 99),
                new StrMinMax(0, 135),
                new StrMinMax(0, 19),
                new StrMinMax(0, 19)
            };


            CsvPreference pref = CsvPreference.STANDARD_PREFERENCE;
            if (jCheckBox2.isSelected()) {
                pref = CsvPreference.EXCEL_NORTH_EUROPE_PREFERENCE;
            }

            this.setCursor(new Cursor(Cursor.WAIT_CURSOR));

            ICsvBeanReader inFile = new CsvBeanReader(new FileReader(jTextField1.getText()), pref);
            try {
//                final String[] header = inFile.getCSVHeader(true);


                while ((user = inFile.read(ProductImporteur.class, header, processors)) != null) {
                    liste.add(user);
                }


            } catch (SuperCSVException ex) {
                succ = false;
                new Popup(ex.getMessage(), Popup.ERROR);
                ex.printStackTrace();
            } catch (IOException ex) {
                succ = false;
                new Popup(ex.getMessage(), Popup.ERROR);

                ex.printStackTrace();
            } finally {
                this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                inFile.close();

            }

        } catch (Exception ex) {
            succ = false;
            new Popup(ex.getMessage(), Popup.ERROR);
            ex.printStackTrace();
        }

        try {
            this.setCursor(new Cursor(Cursor.WAIT_CURSOR));
            user = new ProductImporteur();
            data = ProductImporteur.listToImporteurArray(liste, this.supplier, this.hersteller);
            datstr = user.getData(data);

            Thread.sleep(5000);//Wait for the data..
            this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));

            jTable1.setModel(new DefaultTableModel(datstr, header));


        } catch (Exception ex) {
            succ = false;
            new Popup(ex.getMessage(), Popup.ERROR);
            ex.printStackTrace();
        }


        if (succ) {
            getJButton4().setEnabled(true);
        }
    }

    public void setSupplier(Lieferant supplier) {
        this.jTextField2.setText(supplier.getFirma());
        this.supplier = supplier;
    }

    public void setContact2(Hersteller hersteller) {
        this.jTextField3.setText(hersteller.getFirma());
        this.hersteller = hersteller;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
// <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
private void initComponents() {

buttonGroup1 = new javax.swing.ButtonGroup();
jPanel1 = new javax.swing.JPanel();
jButton1 = new javax.swing.JButton();
jButton2 = new javax.swing.JButton();
jTextField1 = new javax.swing.JTextField();
jButton3 = new javax.swing.JButton();
jLabel2 = new javax.swing.JLabel();
jScrollPane1 = new javax.swing.JScrollPane();
jTable1 = new javax.swing.JTable();
jCheckBox1 = new javax.swing.JCheckBox();
jCheckBox2 = new javax.swing.JCheckBox();
jButton4 = new javax.swing.JButton();
jLabel1 = new javax.swing.JLabel();
jProgressBar1 = new javax.swing.JProgressBar();
jLabel3 = new javax.swing.JLabel();
jTextField2 = new javax.swing.JTextField();
jButton5 = new javax.swing.JButton();
jButton6 = new javax.swing.JButton();
jLabel4 = new javax.swing.JLabel();
jTextField3 = new javax.swing.JTextField();
jButton7 = new javax.swing.JButton();
jCheckBox3 = new javax.swing.JCheckBox();

setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
setTitle("MP CSV Import");

jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Import: Daten aus einer CSV -Datei"));

jButton1.setText("Start");
jButton1.addMouseListener(new java.awt.event.MouseAdapter() {
public void mouseClicked(java.awt.event.MouseEvent evt) {
jButton1MouseClicked(evt);
}
});

jButton2.setText("Abbruch");
jButton2.addMouseListener(new java.awt.event.MouseAdapter() {
public void mouseClicked(java.awt.event.MouseEvent evt) {
jButton2MouseClicked(evt);
}
});

jTextField1.setText("Datei wählen!");
jTextField1.addActionListener(new java.awt.event.ActionListener() {
public void actionPerformed(java.awt.event.ActionEvent evt) {
jTextField1ActionPerformed(evt);
}
});

jButton3.setText("...");
jButton3.addMouseListener(new java.awt.event.MouseAdapter() {
public void mouseClicked(java.awt.event.MouseEvent evt) {
jButton3MouseClicked(evt);
}
});

jLabel2.setText("Trennzeichen:");

jTable1.setAutoCreateRowSorter(true);
jTable1.setModel(new javax.swing.table.DefaultTableModel(
	new Object [][] {
		{null, null, null, null, null, null, null, null, null, null, null},
		{null, null, null, null, null, null, null, null, null, null, null},
		{null, null, null, null, null, null, null, null, null, null, null},
		{null, null, null, null, null, null, null, null, null, null, null}
	},
	new String [] {
		"Produknummer", "Produktname", "Langtext", "VK", "EK", "Mehrwertsteuer", "Warengruppenkategorie", "Warengruppenfamilie", "Warengruppe", "Produktbild-URL", "EAN"
	}
));
jScrollPane1.setViewportView(jTable1);

buttonGroup1.add(jCheckBox1);
jCheckBox1.setText("Komma");
jCheckBox1.addActionListener(new java.awt.event.ActionListener() {
public void actionPerformed(java.awt.event.ActionEvent evt) {
jCheckBox1ActionPerformed(evt);
}
});

buttonGroup1.add(jCheckBox2);
jCheckBox2.setSelected(true);
jCheckBox2.setText("Semikolon");

jButton4.setText("Daten übernehmen");
jButton4.setEnabled(false);
jButton4.addActionListener(new java.awt.event.ActionListener() {
public void actionPerformed(java.awt.event.ActionEvent evt) {
jButton4ActionPerformed(evt);
}
});

jLabel1.setText("Produktimport: Wählen Sie eine CSV-Datei.");
jLabel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());

jProgressBar1.setStringPainted(true);

jLabel3.setFont(new java.awt.Font("Dialog", 1, 11)); // NOI18N
jLabel3.setText("Lieferant: ");

jTextField2.addActionListener(new java.awt.event.ActionListener() {
public void actionPerformed(java.awt.event.ActionEvent evt) {
jTextField2ActionPerformed(evt);
}
});

jButton5.setText("Wählen");
jButton5.addActionListener(new java.awt.event.ActionListener() {
public void actionPerformed(java.awt.event.ActionEvent evt) {
jButton5ActionPerformed(evt);
}
});

jButton6.setText("Hilfe");
jButton6.addActionListener(new java.awt.event.ActionListener() {
public void actionPerformed(java.awt.event.ActionEvent evt) {
jButton6ActionPerformed(evt);
}
});

jLabel4.setFont(new java.awt.Font("Dialog", 1, 11)); // NOI18N
jLabel4.setText("Hersteller:");

jTextField3.addActionListener(new java.awt.event.ActionListener() {
public void actionPerformed(java.awt.event.ActionEvent evt) {
jTextField3ActionPerformed(evt);
}
});

jButton7.setText("Wählen");
jButton7.addActionListener(new java.awt.event.ActionListener() {
public void actionPerformed(java.awt.event.ActionEvent evt) {
jButton7ActionPerformed(evt);
}
});

jCheckBox3.setSelected(true);
jCheckBox3.setText("Ohne 1. Zeile");

javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
jPanel1.setLayout(jPanel1Layout);
jPanel1Layout.setHorizontalGroup(
jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
.addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 774, Short.MAX_VALUE)
.addGroup(jPanel1Layout.createSequentialGroup()
.addComponent(jLabel1)
.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
.addComponent(jProgressBar1, javax.swing.GroupLayout.DEFAULT_SIZE, 561, Short.MAX_VALUE))
.addGroup(jPanel1Layout.createSequentialGroup()
.addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
.addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 308, javax.swing.GroupLayout.PREFERRED_SIZE)
.addGroup(jPanel1Layout.createSequentialGroup()
.addComponent(jLabel2)
.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
.addComponent(jCheckBox1)
.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
.addComponent(jCheckBox2)
.addGap(18, 18, 18)
.addComponent(jCheckBox3)))
.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
.addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
.addGroup(jPanel1Layout.createSequentialGroup()
.addComponent(jLabel3)
.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
.addComponent(jTextField2, javax.swing.GroupLayout.DEFAULT_SIZE, 82, Short.MAX_VALUE))
.addGroup(jPanel1Layout.createSequentialGroup()
.addComponent(jButton3)
.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
.addComponent(jButton6)))
.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
.addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
.addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
.addComponent(jButton2)
.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
.addComponent(jButton1)
.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
.addComponent(jButton4))
.addGroup(jPanel1Layout.createSequentialGroup()
.addComponent(jButton5)
.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
.addComponent(jLabel4)
.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
.addComponent(jTextField3, javax.swing.GroupLayout.DEFAULT_SIZE, 87, Short.MAX_VALUE)
.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
.addComponent(jButton7)
.addContainerGap())))
);
jPanel1Layout.setVerticalGroup(
jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
.addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
.addContainerGap()
.addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 283, Short.MAX_VALUE)
.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
.addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
.addComponent(jLabel2)
.addComponent(jCheckBox1)
.addComponent(jCheckBox2)
.addComponent(jButton5)
.addComponent(jTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
.addComponent(jButton7)
.addComponent(jLabel4)
.addComponent(jCheckBox3)
.addComponent(jLabel3)
.addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
.addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
.addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
.addComponent(jButton2)
.addComponent(jButton1)
.addComponent(jButton4)
.addComponent(jButton3)
.addComponent(jButton6))
.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
.addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
.addComponent(jProgressBar1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
.addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
);

javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
getContentPane().setLayout(layout);
layout.setHorizontalGroup(
layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
.addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
);
layout.setVerticalGroup(
layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
.addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
);

pack();
}// </editor-fold>//GEN-END:initComponents
    private void jButton2MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton2MouseClicked

        this.dispose();
    }//GEN-LAST:event_jButton2MouseClicked

    @SuppressWarnings({"unchecked", "unchecked", "unchecked"})
    private void jButton1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton1MouseClicked
        boolean succ = true;
        ProductImporteur importeur = new ProductImporteur();
        liste = new ArrayList();
        header = new String[]{"produktnummer", "name", "text", "vk",
                    "ek", "tax", "warengruppenkategorie", "warengruppenfamilie",
                    "warengruppe", "url", "ean"
                };

        try {

            final CellProcessor[] processors = new CellProcessor[]{
                new StrMinMax(0, 49),
                new StrMinMax(0, 49),
                new StrMinMax(0, 499),
                new StrMinMax(0, 99),
                new StrMinMax(0, 99),
                new StrMinMax(0, 99),
                new StrMinMax(0, 99),
                new StrMinMax(0, 99),
                new StrMinMax(0, 99),
                new StrMinMax(0, 99),
                new StrMinMax(0, 135)
            };


            CsvPreference pref = CsvPreference.STANDARD_PREFERENCE;
            if (jCheckBox2.isSelected()) {
                pref = CsvPreference.EXCEL_NORTH_EUROPE_PREFERENCE;
            }

            this.setCursor(new Cursor(Cursor.WAIT_CURSOR));

            ICsvBeanReader inFile = null;
            try {
                inFile = new CsvBeanReader(new FileReader(jTextField1.getText()), pref);
            } catch (FileNotFoundException fileNotFoundException) {
                Popup.notice("Bitte Datei auswählen!");
            }
            try {
//                final String[] header = inFile.getCSVHeader(true);
                if(jCheckBox3.isSelected())inFile.read(ProductImporteur.class, header, processors);
                while ((importeur = inFile.read(ProductImporteur.class, header, processors)) != null) {
                    liste.add(importeur);
                }


            } catch (SuperCSVException ex) {
                succ = false;
                new Popup(ex.getMessage(), Popup.ERROR);
                ex.printStackTrace();
            } catch (IOException ex) {
                succ = false;
                new Popup(ex.getMessage(), Popup.ERROR);

                ex.printStackTrace();
            } finally {
                this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                inFile.close();

            }

        } catch (Exception ex) {
            succ = false;
            new Popup(ex.getMessage(), Popup.ERROR);
            ex.printStackTrace();
        }

        try {
            importeur = new ProductImporteur();
            data = ProductImporteur.listToImporteurArray(liste, this.supplier, this.hersteller);
            datstr = importeur.getData(data);
            jTable1.setModel(new DefaultTableModel(datstr, header));
        } catch (Exception ex) {
            succ = false;
            new Popup(ex.getMessage(), Popup.ERROR);
            ex.printStackTrace();
        }


        if (succ) {
            getJButton4().setEnabled(true);
        }
    }//GEN-LAST:event_jButton1MouseClicked

    private void jTextField1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField1ActionPerformed
    }//GEN-LAST:event_jTextField1ActionPerformed

    private void jButton3MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton3MouseClicked

        JFileChooser fc = new JFileChooser();
        fc.setFileSelectionMode(JFileChooser.FILES_ONLY);

        if (fc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            jTextField1.setText(fc.getSelectedFile().toString());
        }

    }//GEN-LAST:event_jButton3MouseClicked

    private void jCheckBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox1ActionPerformed
    }//GEN-LAST:event_jCheckBox1ActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        if (jButton4.isEnabled()) {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            task = new Task(this);
            task.execute();

            jButton2.setText("Beenden");
        }

    }//GEN-LAST:event_jButton4ActionPerformed

    private void jButton6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton6ActionPerformed

        new Help("/helpfiles/produktimport1");
    }//GEN-LAST:event_jButton6ActionPerformed

private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
    new SupplierPicker(this);
}//GEN-LAST:event_jButton5ActionPerformed

private void jTextField2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField2ActionPerformed
}//GEN-LAST:event_jTextField2ActionPerformed

private void jTextField3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField3ActionPerformed
// TODO add your handling code here:
}//GEN-LAST:event_jTextField3ActionPerformed

private void jButton7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton7ActionPerformed

    new ManufacturerPicker(this);
}//GEN-LAST:event_jButton7ActionPerformed
// Variables declaration - do not modify//GEN-BEGIN:variables
private javax.swing.ButtonGroup buttonGroup1;
private javax.swing.JButton jButton1;
private javax.swing.JButton jButton2;
private javax.swing.JButton jButton3;
private javax.swing.JButton jButton4;
private javax.swing.JButton jButton5;
private javax.swing.JButton jButton6;
private javax.swing.JButton jButton7;
private javax.swing.JCheckBox jCheckBox1;
private javax.swing.JCheckBox jCheckBox2;
private javax.swing.JCheckBox jCheckBox3;
public javax.swing.JLabel jLabel1;
private javax.swing.JLabel jLabel2;
private javax.swing.JLabel jLabel3;
private javax.swing.JLabel jLabel4;
private javax.swing.JPanel jPanel1;
public javax.swing.JProgressBar jProgressBar1;
private javax.swing.JScrollPane jScrollPane1;
private javax.swing.JTable jTable1;
private javax.swing.JTextField jTextField1;
private javax.swing.JTextField jTextField2;
private javax.swing.JTextField jTextField3;
// End of variables declaration//GEN-END:variables
    public javax.swing.JButton getJButton4() {
        return jButton4;
    }

    class Task extends SwingWorker<Void, Void> {

        private csvProductImporter thisa;
        private ProductGroupCategory newcat;
        private ProductGroupFamily newfam;
        private ProductGroupGroup newgrp;
        private String fam;
        private String grp;
        /*
         * Main task. Executed in background thread.
         */

        public Task(csvProductImporter thisa) {

            this.thisa = thisa;
        }

        @Override
        public Void doInBackground() {
            int h = 0;
            SimpleDateFormat df = new SimpleDateFormat();
            Date datum = new Date();
            String cat;
            boolean news = false;

            if ((JOptionPane.showConfirmDialog(thisa, "Wirklich alle Daten übernehmen? Dies wird möglicherweise einige Zeit dauern!", "Sicher?", JOptionPane.YES_NO_OPTION)) == JOptionPane.YES_OPTION) {

                if (thisa.data != null) {
                    Date d = new Date();
//                    Log.setLogLevel(Log.LOGLEVEL_DEBUG);
                    Log.Debug(this, "Einlesen gestartet: " + d, true);
                    thisa.setCursor(new Cursor(Cursor.WAIT_CURSOR));

                    Log.Debug(this, "Expected data: " + thisa.data.length);
                    thisa.jProgressBar1.setMaximum(thisa.data.length);
                    thisa.jProgressBar1.setMinimum(0);

                    for (int i = 0; i < thisa.data.length; i++) {

                        Log.Debug(this, "Getting grouphandler...");
                        Product pg = new Product(ConnectionHandler.instanceOf());
                        ProductGroupHandler handler = ProductGroupHandler.instanceOf();

                        try {
                            Log.Debug(this, "Try: " + (i + 1));
                            Log.Debug(this, "Step " + 1 + ": ");
                            pg.setNummer(thisa.data[i].getProduktnummer());
                            Log.Debug(this, "Step " + 2 + ": ");
                            pg.setName(thisa.data[i].getName());
                            Log.Debug(this, "Step " + 3 + ": ");
                            pg.setDatum(datum);
                            Log.Debug(this, "Step " + 4 + ": ");
                            pg.setEK(Double.valueOf(thisa.data[i].getEk()));
                            Log.Debug(this, "Step " + 5 + ": ");
                            pg.setVK(Double.valueOf(thisa.data[i].getVk()));
                            Log.Debug(this, "Step " + 6 + ": ");
                            pg.setEan(thisa.data[i].getEan());
                            Log.Debug(this, "Step " + 7 + ": ");
                            pg.setHersteller(new Hersteller(thisa.data[i].getHersteller()));
                            Log.Debug(this, "Step " + 8 + ": ");
                            pg.setTaxID(new Steuersatz().findIDWithOrCreate(Double.valueOf(thisa.data[i].getTax())).getId());
                            Log.Debug(this, "Step " + 9 + ": ");
                            pg.setText(thisa.data[i].getText());
                            Log.Debug(this, "Step " + 10 + ": ");
                            pg.setUrl(thisa.data[i].getUrl());
                            Log.Debug(this, "Step " + 11 + ": ");
                            pg.setLieferant(new Lieferant(Integer.valueOf(thisa.data[i].getLieferantenid())));
                        } catch (NumberFormatException numberFormatException) {
                            Log.Debug(numberFormatException);
                        }

                        Log.Debug(this, "Getting groups...");
                        cat = thisa.data[i].getWarengruppenkategorie();
                        fam = thisa.data[i].getWarengruppenfamilie();
                        grp = thisa.data[i].getWarengruppe();

                        Log.Debug(this, pg.getName() + ":Produkt \n" + cat, true);
                        Log.Debug(this, fam, true);
                        Log.Debug(this, grp, true);

                        Log.Debug(this, "---------------------------", true);


                        if (!cat.equals("null") && !fam.equals("null") && !grp.equals("null")) {

                            int z = handler.exists(cat, handler.CATEGORY);
                            if (z == 0) {

                                newcat = new ProductGroupCategory(ConnectionHandler.instanceOf());

                                newcat.setName(cat);
                                newcat.save();
                                z = newcat.getID();
                                news = true;
                            } else {//                            
                                newcat = handler.getCategory(z);
                            }

                            int f = handler.existFam(fam);
                            if (f == 0) {
                                Log.Debug(this, "Creating Productfamily: " + fam + " " + f, true);
                                newfam = new ProductGroupFamily(ConnectionHandler.instanceOf());
                                newfam.setName(fam);
                                newfam.setKategorieid(z);
                                newfam.save();
                                f = newfam.getID();
                                news = true;
                            } else {
                                Log.Debug(this, "Existing Productfamily: " + fam + " " + f, true);
                                newfam = handler.getFamily(f);
                            }



                            int l = handler.exists(grp, handler.GROUP);
                            if (l == 0) {

                                newgrp = new ProductGroupGroup(ConnectionHandler.instanceOf());
                                newgrp.setName(grp);
                                newgrp.setFamilienid(f);
                                newgrp.save();
                                news = true;
                            } else {
                                newgrp = handler.getGroup(l);
                            }


                            pg.setWarengruppenId(newgrp.getID());


                            if (news) {
                                handler.getCats(true); //speed?
                            } //speed?

                            news = false;


                        } else {

                            pg.setWarengruppenId(1);

                        }



                        pg.save();
                        h++;
                        thisa.jProgressBar1.setValue(i);

                        thisa.jLabel1.setText(h + " Produkte angelegt");
                        pg = null;
                    }
                    d = new Date();

                    Popup.notice("Einlesen beendet: " + d + " Produkte: " + h);
                    new HistoryItem(ConnectionHandler.instanceOf(), "Datenimport", h + " Produkte importiert.");


                    thisa.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                    thisa.getJButton4().setEnabled(false);
                    thisa.jProgressBar1.setValue(0);

                }

            }


            return null;
        }

        /*
         * Executed in event dispatching thread
         */
        @Override
        public void done() {
//        Toolkit.getDefaultToolkit().beep();
            thisa.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));


        }
    }

    @Override
    public void update() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setProduct(Product p) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setContact(People contact) {
        jTextField2.setText(contact.getFirma());
        this.supplier = (Lieferant) contact;
    }

    public void setTax(Steuersatz steuersatz) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
