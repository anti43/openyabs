/*
 *  This file is part of MP by anti43 /GPL.
 *  
 *      MP is free software: you can redistribute it and/or modify
 *      it under the terms of the GNU General Public License as published by
 *      the Free Software Foundation, either version 3 of the License, or
 *      (at your option) any later version.
 *  
 *      MP is distributed in the hope that it will be useful,
 *      but WITHOUT ANY WARRANTY; without even the implied warranty of
 *      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *      GNU General Public License for more details.
 *  
 *      You should have received a copy of the GNU General Public License
 *      along with MP.  If not, see <http://www.gnu.org/licenses/>.
 */
package mp3.classes.visual.main;

import mp4.datenbank.verbindung.Conn;
import mp3.classes.visual.util.csvProductImporter;
import mp3.classes.visual.util.fastChoice;
import mp3.classes.visual.util.serialLetter;
import mp3.classes.visual.util.mp2Importer;
import mp3.classes.visual.util.settingsView;
import mp3.Main;
import java.awt.BorderLayout;

import java.awt.Color;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import javax.swing.JComponent;
import javax.swing.JLabel;
import mp3.classes.interfaces.Constants;

import mp3.classes.interfaces.Strings;
import mp3.classes.layer.Popup;
import mp4.datenbank.verbindung.ConnectionHandler;


import mp3.classes.utils.Browser;

import mp3.classes.utils.Log;
import mp3.classes.utils.SaveAs;
import mp3.classes.utils.SplashScreen;
import mp3.classes.utils.TableHtmlWriter;
import mp3.classes.visual.main.license;
import mp3.classes.visual.sub.backupView;
import mp4.panels.billsView;

import mp3.classes.visual.sub.customersView;
//import mp3.classes.visual.sub.*;
import mp3.classes.visual.sub.historyView;
import mp3.classes.visual.sub.offersView;

import mp3.classes.visual.sub.productsView;
//import mp3.classes.visual.sub.*;
import mp3.classes.visual.sub.startView;
import mp3.classes.visual.sub.suppliersView;
import mp3.classes.visual.util.EinnahmenChart;
import mp3.classes.visual.util.UmsatzChart;
import mp3.classes.visual.sub.eurView;
import mp4.cache.undoCache;
import mp4.datenbank.verbindung.Query;
import mp4.klassen.objekte.Customer;
import mp4.klassen.objekte.Lieferant;
import mp4.einstellungen.Einstellungen;
import mp4.einstellungen.Programmdaten;
import mp4.klassen.objekte.Product;
import mp4.utils.text.FadeOnChangeLabel;
import mp4.utils.windows.Position;

/**
 *
 * @author  anti43
 */
public class mainframe extends javax.swing.JFrame {

    private customersView c;
    private billsView b;
    private productsView d;
    private suppliersView e;
    private offersView f;
    private historyView g;
    private backupView h;
    private startView i;
    private eurView j;
    private Main loader;
    private Position wt = new Position();
    public static JLabel nachricht = new JLabel();
    private mainframe identifier;

    /** Creates new form mainframe
     * @param splash
     * @param mainclass 
     */
    public mainframe(SplashScreen splash, Main mainclass) {

        this.loader = mainclass;
        this.identifier = this;
        splash.setMessage("Initialisiere Oberfläche...");
        initComponents();

        splash.setMessage("Initialisiere Datenbank...");

        try {
            ConnectionHandler.instanceOf();

        } catch (Exception exception) {
            this.dispose();
            splash.dispose();
            loader = null;
            System.gc();
            System.gc();

        }

        Query.setWaitCursorFor(this);


        splash.setMessage("Initialisiere Komponenten...");

        c = new customersView(this);
        b = new billsView(this);
        d = new productsView(this);
        e = new suppliersView(this);
        f = new offersView(this);
        g = new historyView(this);
        h = new backupView(this);
        i = new startView(this);
        j = new eurView(this);


//        jPanel4.add(d, BorderLayout.CENTER);
//        jPanel3.add(b, BorderLayout.CENTER);
//        jPanel7.add(f, BorderLayout.CENTER);
//        jPanel2.add(c, BorderLayout.CENTER);
//        jPanel9.add(e, BorderLayout.CENTER);
//        jPanel6.add(g, BorderLayout.CENTER);
//        jPanel8.add(h, BorderLayout.CENTER);
        jPanel11.add(i, BorderLayout.CENTER);
//        jPanel12.add(j, BorderLayout.CENTER);

        try {
            mainTabPane.setSelectedIndex(Programmdaten.instanceOf().getMAINFRAME_TAB());
        } catch (Exception exception) {
        }

        try {
            this.setTitle("MP " + Constants.VERSION);


        } catch (Exception exception) {
            splash.dispose();
            loader = null;
            System.gc();
            System.gc();
        }

        try {
            this.setSize(Programmdaten.instanceOf().getMAINFRAME_WINDOW_STATE());

            if (wt.isNotMaximized(this)) {
                wt.center(this);
            } else {
                this.setExtendedState(mainframe.MAXIMIZED_BOTH);
            }

        } catch (Exception exception) {
            Log.Debug(exception.getMessage(), true);
            this.setExtendedState(mainframe.MAXIMIZED_BOTH);
        }


        this.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent e) {
                close();
            }
        });


        undoCache.setMenu(jMenu11, this);

        setMessage("Anmerkungen, Bugs und Feedback zu MP bitte an mp-rechnungs-und-kundenverwaltung@googlegroups.com senden. Vielen Dank!");

        nachricht = messagePanel;

        this.setVisible(rootPaneCheckingEnabled);
    }

    public customersView getCustomersView() {


        return c;
    }

    public eurView getEURPanel() {
        return j;
    }

//    /** Creates new form mainframe
//     * @param firststart
//     * @param splash 
//     */
//    public mainframe(boolean firststart, SplashScreen splash) {
//        initComponents();
//        this.setExtendedState(mainframe.MAXIMIZED_BOTH);
//
//
//        c = new customersView(this);
//        b = new billsView(this);
//        d = new productsView(this);
//        e = new suppliersView(this);
//        f = new offersView(this);
//        g = new historyView(this);
//        h = new backupView(this);
//        i = new startView(this);
//        j = new eurView(this);
//        //c.setCustomer(new Customer(ConnectionHandler.instanceOf(), "1"));
//
//
//
//        jPanel4.add(d, BorderLayout.CENTER);
//        jPanel3.add(b, BorderLayout.CENTER);
//        jPanel7.add(f, BorderLayout.CENTER);
//        jPanel2.add(c, BorderLayout.CENTER);
//        jPanel9.add(e, BorderLayout.CENTER);
//        jPanel6.add(g, BorderLayout.CENTER);
//        jPanel8.add(h, BorderLayout.CENTER);
//        jPanel11.add(i, BorderLayout.CENTER);
//        jPanel12.add(j, BorderLayout.CENTER);
//
//        ConnectionHandler.setProgressBarOn(this.mainProgress, this);
//
//        this.setVisible(rootPaneCheckingEnabled);
//
//        if (firststart) {
//            new settingsView(this).setVisible(firststart);
//        }
//        this.addWindowListener(new WindowAdapter() {
//
//            @Override
//            public void windowClosing(WindowEvent e) {
//                close();
//            }
//        });
//    }
    /**
     * 
     * @return  
     *          0:start 
     *          1:kunden
     *          2:rechnungen
     *          3:angebote
     *          4:eur
     *          5:produkte
     *          6:lieferanten
     *          7:verlauf  
     *          8:sicherung
     *          
     */
    public int getShowingTab() {
        return getJTabbedPane1().getSelectedIndex();
    }

    /**
     * 
     * @return
     */
    public offersView getOrdersView() {
        return f;
    }

    public productsView getProductsView() {
        return d;
    }

    /**
     * 
     * @param i
     *          0:start 
     *          1:kunden
     *          2:rechnungen
     *          3:angebote
     *          4:eur
     *          5:produkte
     *          6:lieferanten
     *          7:verlauf  
     *          8:sicherung
     */
    public void setShowingTab(int i) {
        getJTabbedPane1().setSelectedIndex(i);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        mainProgress = new javax.swing.JProgressBar();
        messagePanel = new FadeOnChangeLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jPanel5 = new javax.swing.JPanel();
        mainTabPane = new javax.swing.JTabbedPane();
        jPanel11 = new javax.swing.JPanel();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jMenu2 = new javax.swing.JMenu();
        jMenu3 = new javax.swing.JMenu();
        jMenuItem1 = new javax.swing.JMenuItem();
        jPanel10 = new javax.swing.JPanel();
        jOutlookBar1 = new com.l2fprod.common.swing.JOutlookBar();
        jPanel2 = new javax.swing.JPanel();
        jButton4 = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();
        jPanel4 = new javax.swing.JPanel();
        jButton9 = new javax.swing.JButton();
        jButton10 = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jButton5 = new javax.swing.JButton();
        jButton6 = new javax.swing.JButton();
        jButton7 = new javax.swing.JButton();
        jMenuBar2 = new javax.swing.JMenuBar();
        jMenu4 = new javax.swing.JMenu();
        jMenu9 = new javax.swing.JMenu();
        jMenuItem10 = new javax.swing.JMenuItem();
        jMenuItem11 = new javax.swing.JMenuItem();
        jSeparator3 = new javax.swing.JSeparator();
        jMenuItem12 = new javax.swing.JMenuItem();
        jSeparator2 = new javax.swing.JSeparator();
        jMenuItem13 = new javax.swing.JMenuItem();
        jMenuItem14 = new javax.swing.JMenuItem();
        jSeparator4 = new javax.swing.JSeparator();
        jMenu10 = new javax.swing.JMenu();
        jMenuItem15 = new javax.swing.JMenuItem();
        jMenuItem16 = new javax.swing.JMenuItem();
        jMenuItem17 = new javax.swing.JMenuItem();
        jMenuItem18 = new javax.swing.JMenuItem();
        jMenu5 = new javax.swing.JMenu();
        jMenu11 = new javax.swing.JMenu();
        jMenu6 = new javax.swing.JMenu();
        jMenuItem2 = new javax.swing.JMenuItem();
        jMenuItem3 = new javax.swing.JMenuItem();
        jMenuItem4 = new javax.swing.JMenuItem();
        jMenu8 = new javax.swing.JMenu();
        jMenuItem9 = new javax.swing.JMenuItem();
        jMenuItem19 = new javax.swing.JMenuItem();
        jMenu7 = new javax.swing.JMenu();
        jMenuItem6 = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JSeparator();
        jMenuItem8 = new javax.swing.JMenuItem();
        jSeparator5 = new javax.swing.JSeparator();
        jMenuItem21 = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("MP");
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        setExtendedState(this.MAXIMIZED_BOTH);
        setLocationByPlatform(true);

        jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        mainProgress.setForeground(new java.awt.Color(255, 0, 51));
        mainProgress.setBorderPainted(false);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addComponent(messagePanel, javax.swing.GroupLayout.DEFAULT_SIZE, 612, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(mainProgress, javax.swing.GroupLayout.PREFERRED_SIZE, 13, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(mainProgress, javax.swing.GroupLayout.DEFAULT_SIZE, 16, Short.MAX_VALUE)
            .addComponent(messagePanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 16, Short.MAX_VALUE)
        );

        jScrollPane1.setBorder(null);
        jScrollPane1.setAutoscrolls(true);

        jPanel5.setLayout(new java.awt.BorderLayout());

        mainTabPane.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                mainTabPaneMouseClicked(evt);
            }
        });
        mainTabPane.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                mainTabPanePropertyChange(evt);
            }
        });

        jPanel11.setLayout(new java.awt.BorderLayout());
        mainTabPane.addTab("Start", jPanel11);

        jPanel5.add(mainTabPane, java.awt.BorderLayout.CENTER);

        jScrollPane1.setViewportView(jPanel5);

        jMenu1.setText("File");
        jMenuBar1.add(jMenu1);

        jMenu2.setText("Bearbeiten");

        jMenu3.setText("Importieren..");

        jMenuItem1.setText("Produkte");
        jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem1ActionPerformed(evt);
            }
        });
        jMenu3.add(jMenuItem1);

        jMenu2.add(jMenu3);

        jMenuBar1.add(jMenu2);

        jPanel10.setBackground(new java.awt.Color(204, 204, 204));
        jPanel10.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel10.setLayout(new javax.swing.BoxLayout(jPanel10, javax.swing.BoxLayout.LINE_AXIS));

        jOutlookBar1.setBackground(new java.awt.Color(204, 204, 204));

        jPanel2.setLayout(new javax.swing.BoxLayout(jPanel2, javax.swing.BoxLayout.Y_AXIS));

        jButton4.setText("Kunden");
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });
        jPanel2.add(jButton4);

        jButton1.setText("Lieferanten");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        jPanel2.add(jButton1);

        jOutlookBar1.addTab("Kontakte", jPanel2);

        jPanel4.setLayout(new javax.swing.BoxLayout(jPanel4, javax.swing.BoxLayout.Y_AXIS));

        jButton9.setText("Produkte");
        jButton9.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton9ActionPerformed(evt);
            }
        });
        jPanel4.add(jButton9);

        jButton10.setText("Services");
        jPanel4.add(jButton10);

        jOutlookBar1.addTab("Produkte", jPanel4);

        jPanel3.setLayout(new javax.swing.BoxLayout(jPanel3, javax.swing.BoxLayout.Y_AXIS));

        jButton5.setText("Einnahmen");
        jPanel3.add(jButton5);

        jButton6.setText("Ausgaben");
        jPanel3.add(jButton6);

        jButton7.setText("Übersicht");
        jButton7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton7ActionPerformed(evt);
            }
        });
        jPanel3.add(jButton7);

        jOutlookBar1.addTab("Buchhaltung", jPanel3);

        jPanel10.add(jOutlookBar1);

        jMenu4.setText("Datei");

        jMenu9.setText("Direktwahl");

        jMenuItem10.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_K, java.awt.event.InputEvent.ALT_MASK));
        jMenuItem10.setText("Kunden");
        jMenuItem10.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem10ActionPerformed(evt);
            }
        });
        jMenu9.add(jMenuItem10);

        jMenuItem11.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_R, java.awt.event.InputEvent.ALT_MASK));
        jMenuItem11.setText("Rechnungen");
        jMenuItem11.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem11ActionPerformed(evt);
            }
        });
        jMenu9.add(jMenuItem11);

        jMenu4.add(jMenu9);
        jMenu4.add(jSeparator3);

        jMenuItem12.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem12.setText("Speichern");
        jMenuItem12.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem12ActionPerformed(evt);
            }
        });
        jMenu4.add(jMenuItem12);
        jMenu4.add(jSeparator2);

        jMenuItem13.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_X, java.awt.event.InputEvent.ALT_MASK));
        jMenuItem13.setText("Beenden");
        jMenuItem13.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem13ActionPerformed(evt);
            }
        });
        jMenu4.add(jMenuItem13);

        jMenuItem14.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_X, java.awt.event.InputEvent.ALT_MASK | java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem14.setText("Beenden und Sichern");
        jMenuItem14.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem14ActionPerformed(evt);
            }
        });
        jMenu4.add(jMenuItem14);
        jMenu4.add(jSeparator4);

        jMenu10.setText("Listen Export");

        jMenuItem15.setText("Kunden");
        jMenuItem15.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem15ActionPerformed(evt);
            }
        });
        jMenu10.add(jMenuItem15);

        jMenuItem16.setText("Lieferanten");
        jMenuItem16.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem16ActionPerformed(evt);
            }
        });
        jMenu10.add(jMenuItem16);

        jMenuItem17.setText("Produkte");
        jMenuItem17.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem17ActionPerformed(evt);
            }
        });
        jMenu10.add(jMenuItem17);

        jMenu4.add(jMenu10);

        jMenuItem18.setText("Serienbrief (Default Drucker)");
        jMenuItem18.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem18ActionPerformed(evt);
            }
        });
        jMenu4.add(jMenuItem18);

        jMenuBar2.add(jMenu4);

        jMenu5.setText("Bearbeiten");

        jMenu11.setText("Undo");
        jMenu5.add(jMenu11);

        jMenu6.setText("Importieren..");

        jMenuItem2.setText("Produkte (CSV)");
        jMenuItem2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem1ActionPerformed(evt);
            }
        });
        jMenu6.add(jMenuItem2);

        jMenuItem3.setText("Daten (Version 2.x)");
        jMenuItem3.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jMenuItem3MouseClicked(evt);
            }
        });
        jMenuItem3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem3ActionPerformed(evt);
            }
        });
        jMenu6.add(jMenuItem3);

        jMenu5.add(jMenu6);

        jMenuItem4.setText("Einstellungen");
        jMenuItem4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem4ActionPerformed(evt);
            }
        });
        jMenu5.add(jMenuItem4);

        jMenuBar2.add(jMenu5);

        jMenu8.setText("Auswertungen");

        jMenuItem9.setText("Umsatz p.A.");
        jMenuItem9.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem9ActionPerformed(evt);
            }
        });
        jMenu8.add(jMenuItem9);

        jMenuItem19.setText("Gewinn p.A.");
        jMenuItem19.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem19ActionPerformed(evt);
            }
        });
        jMenu8.add(jMenuItem19);

        jMenuBar2.add(jMenu8);

        jMenu7.setText("Hilfe");

        jMenuItem6.setText("Hilfe...");
        jMenuItem6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem6ActionPerformed(evt);
            }
        });
        jMenu7.add(jMenuItem6);
        jMenu7.add(jSeparator1);

        jMenuItem8.setText("Lizenzhinweise");
        jMenuItem8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem8ActionPerformed(evt);
            }
        });
        jMenu7.add(jMenuItem8);
        jMenu7.add(jSeparator5);

        jMenuItem21.setText("Log Konsole");
        jMenuItem21.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem21ActionPerformed(evt);
            }
        });
        jMenu7.add(jMenuItem21);

        jMenuBar2.add(jMenu7);

        setJMenuBar(jMenuBar2);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(3, 3, 3)
                .addComponent(jPanel10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 521, Short.MAX_VALUE)
                .addContainerGap())
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel10, javax.swing.GroupLayout.DEFAULT_SIZE, 435, Short.MAX_VALUE)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 435, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
    private void mainTabPanePropertyChange (java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_mainTabPanePropertyChange
}//GEN-LAST:event_mainTabPanePropertyChange

    private void mainTabPaneMouseClicked (java.awt.event.MouseEvent evt) {//GEN-FIRST:event_mainTabPaneMouseClicked
}//GEN-LAST:event_mainTabPaneMouseClicked

    private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem1ActionPerformed
        csvProductImporter.instanceOf();
//        xx
    }//GEN-LAST:event_jMenuItem1ActionPerformed

    private void jMenuItem3MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jMenuItem3MouseClicked
    }//GEN-LAST:event_jMenuItem3MouseClicked

    private void jMenuItem3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem3ActionPerformed
        new mp2Importer(this);
    }//GEN-LAST:event_jMenuItem3ActionPerformed

    private void jMenuItem4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem4ActionPerformed
        new settingsView(this).setVisible(rootPaneCheckingEnabled);
    }//GEN-LAST:event_jMenuItem4ActionPerformed

    private void jMenuItem8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem8ActionPerformed
        license l = new license();
        wt.center(l);
        l.setVisible(rootPaneCheckingEnabled);
    }//GEN-LAST:event_jMenuItem8ActionPerformed

    private void jMenuItem6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem6ActionPerformed
        try {


            Process proc = Runtime.getRuntime().exec(Einstellungen.instanceOf().getBrowser() + " http://www.supernature-forum.de");
        } catch (IOException ex) {

            new Popup("Kein Browser angegeben. Wählen Sie Ihren Internetbrowser unter 'Programmeinstellungen'.");
            // Logger.getLogger(mainFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_jMenuItem6ActionPerformed

    private void close() {
        try {

            Programmdaten.instanceOf().setMAINFRAME_WINDOW_STATE(this.getSize());

            Programmdaten.instanceOf().setMAINFRAME_TAB(mainTabPane.getSelectedIndex());
            Einstellungen.instanceOf().save();
            System.exit(0);
        } catch (Exception exc) {
            Log.Debug(exc, true);
            System.exit(0);
        }
    }

    private void jMenuItem10ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem10ActionPerformed
        new fastChoice(this, 0);
    }//GEN-LAST:event_jMenuItem10ActionPerformed

    private void jMenuItem11ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem11ActionPerformed
        new fastChoice(this, 1);
    }//GEN-LAST:event_jMenuItem11ActionPerformed

    private void jMenuItem12ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem12ActionPerformed
        /**
         * 
         * @return 0:start 
         *          1:kunden
         *          2:rechnungen
         *          3:angebote
         *          4:eur
         *          5:produkte
         *          6:lieferanten
         *          7:verlauf  
         *          8:sicherung
         *          
         */
        switch (this.getShowingTab()) {

            case 1:

                this.getCustomersView().save();

                break;
            case 2:

                this.getBillPanel().save();

                break;
            case 3:
                this.getOrdersView().save();
                break;
            case 5:
                this.getProductsView().save();

                break;
            case 6:
                this.getC().save();
                break;
        }




    }//GEN-LAST:event_jMenuItem12ActionPerformed

    private void jMenuItem13ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem13ActionPerformed
        System.exit(0);
    }//GEN-LAST:event_jMenuItem13ActionPerformed

    private void jMenuItem14ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem14ActionPerformed


        Programmdaten.instanceOf().setMAINFRAME_WINDOW_STATE(this.getSize());

        Programmdaten.instanceOf().setMAINFRAME_TAB(mainTabPane.getSelectedIndex());
        Einstellungen.instanceOf().save();
        h.saving();
        System.exit(0);

    }//GEN-LAST:event_jMenuItem14ActionPerformed

    private void jMenuItem15ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem15ActionPerformed
        String fils = "";
        try {

            Customer k = new Customer();

            TableHtmlWriter writ = new TableHtmlWriter(k.getPrintModel(), new File("Kundenliste.html"), Strings.TABLE_CUSTOMER_PRINT_HEADER.split(","), "Kundenliste");

            fils = new SaveAs(writ.createHtml(1, Color.LIGHT_GRAY)).getName();
        } catch (Exception ex) {
            Popup.notice(ex.getMessage());
            Log.Debug(ex);
        }

        new Browser(fils);

    }//GEN-LAST:event_jMenuItem15ActionPerformed

    private void jMenuItem16ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem16ActionPerformed


        String fils = "";
        try {

            Lieferant k = new Lieferant();

            TableHtmlWriter writ = new TableHtmlWriter(k.getPrintModel(), new File("Lieferantenliste.html"), Strings.TABLE_SUPPLIER_PRINT_HEADER.split(","), "Lieferantenliste");

            fils = new SaveAs(writ.createHtml(1, Color.LIGHT_GRAY)).getName();
        } catch (Exception ex) {
            Popup.notice(ex.getMessage());
            Log.Debug(ex);
        }

        new Browser(fils);


    }//GEN-LAST:event_jMenuItem16ActionPerformed

    private void jMenuItem17ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem17ActionPerformed
        String fils = "";
        try {

            Product k = new Product();

            TableHtmlWriter writ = new TableHtmlWriter(k.getPrintModel(), new File("Produktliste.html"), Strings.TABLE_PRODUCTS_LIST_PRINT_HEADER.split(","), "Lieferantenliste");

            fils = new SaveAs(writ.createHtml(1, Color.LIGHT_GRAY)).getName();
        } catch (Exception ex) {
            Popup.notice(ex.getMessage());
            Log.Debug(ex);
        }

        new Browser(fils);

    }//GEN-LAST:event_jMenuItem17ActionPerformed

    private void jMenuItem18ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem18ActionPerformed
        serialLetter.instanceOf();
    }//GEN-LAST:event_jMenuItem18ActionPerformed

    private void jMenuItem21ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem21ActionPerformed

        Log.setLogLevel(Log.LOGLEVEL_HIGH);
        Log.getLogger().setVisible(true);

    }//GEN-LAST:event_jMenuItem21ActionPerformed

    private void jMenuItem9ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem9ActionPerformed
        new UmsatzChart();
    }//GEN-LAST:event_jMenuItem9ActionPerformed

    private void jMenuItem19ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem19ActionPerformed
        new EinnahmenChart();
    }//GEN-LAST:event_jMenuItem19ActionPerformed

private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed

    mainTabPane.add("Kunde xy",new customersView(this));
}//GEN-LAST:event_jButton4ActionPerformed

private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
    mainTabPane.add("Lieferant xy",new suppliersView(this));
}//GEN-LAST:event_jButton1ActionPerformed

private void jButton7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton7ActionPerformed
    mainTabPane.add("EUR Übersicht",new eurView(this));
}//GEN-LAST:event_jButton7ActionPerformed

private void jButton9ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton9ActionPerformed
    mainTabPane.add("Produkt xy",new productsView(this));
}//GEN-LAST:event_jButton9ActionPerformed

    @Override
    public void finalize() {
        Conn.shutdown();
        System.gc();
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton10;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton7;
    private javax.swing.JButton jButton9;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu10;
    private javax.swing.JMenu jMenu11;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenu jMenu3;
    private javax.swing.JMenu jMenu4;
    private javax.swing.JMenu jMenu5;
    private javax.swing.JMenu jMenu6;
    private javax.swing.JMenu jMenu7;
    private javax.swing.JMenu jMenu8;
    private javax.swing.JMenu jMenu9;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuBar jMenuBar2;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem10;
    private javax.swing.JMenuItem jMenuItem11;
    private javax.swing.JMenuItem jMenuItem12;
    private javax.swing.JMenuItem jMenuItem13;
    private javax.swing.JMenuItem jMenuItem14;
    private javax.swing.JMenuItem jMenuItem15;
    private javax.swing.JMenuItem jMenuItem16;
    private javax.swing.JMenuItem jMenuItem17;
    private javax.swing.JMenuItem jMenuItem18;
    private javax.swing.JMenuItem jMenuItem19;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JMenuItem jMenuItem21;
    private javax.swing.JMenuItem jMenuItem3;
    private javax.swing.JMenuItem jMenuItem4;
    private javax.swing.JMenuItem jMenuItem6;
    private javax.swing.JMenuItem jMenuItem8;
    private javax.swing.JMenuItem jMenuItem9;
    private com.l2fprod.common.swing.JOutlookBar jOutlookBar1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JSeparator jSeparator4;
    private javax.swing.JSeparator jSeparator5;
    private javax.swing.JProgressBar mainProgress;
    private javax.swing.JTabbedPane mainTabPane;
    private javax.swing.JLabel messagePanel;
    // End of variables declaration//GEN-END:variables
    public javax.swing.JMenu getJMenu1() {
        return jMenu1;
    }

    public javax.swing.JMenu getJMenu2() {
        return jMenu2;
    }

    public javax.swing.JMenuBar getJMenuBar1() {
        return jMenuBar1;
    }

    public javax.swing.JPanel getJPanel1() {
        return jPanel1;
    }

    public javax.swing.JPanel getJPanel10() {
        return jPanel10;
    }

    public javax.swing.JPanel getJPanel11() {
        return jPanel11;
    }

//    public javax.swing.JPanel getJPanel2() {
//        return jPanel2;
//    }
//
//    public javax.swing.JPanel getJPanel3() {
//        return jPanel3;
//    }
//
//    public javax.swing.JPanel getJPanel4() {
//        return jPanel4;
//    }
//
//    public javax.swing.JPanel getJPanel5() {
//        return jPanel5;
//    }
//
//    public javax.swing.JPanel getJPanel6() {
//        return jPanel6;
//    }
//
//    public javax.swing.JPanel getJPanel7() {
//        return jPanel7;
//    }
//
//    public javax.swing.JPanel getJPanel9() {
//        return jPanel9;
//    }
    public javax.swing.JScrollPane getJScrollPane1() {
        return jScrollPane1;
    }

    public javax.swing.JTabbedPane getJTabbedPane1() {
        return mainTabPane;
    }

    public javax.swing.JProgressBar getMainProgress() {
        return mainProgress;
    }

    public suppliersView getC() {
        return e;
    }

    public billsView getBillPanel() {
        return b;
    }

//    /**
//     * 
//     * @param message
//     */
//    public void nachricht(String message) {
//
//        nachricht.setText(message);
//    }
    /**
     * 
     * @param message
     */
    public void setMessage(String message) {
        this.getNachricht().setText(message);
    }

    public javax.swing.JLabel getNachricht() {
        return messagePanel;
    }

    @Override
    public void dispose() {

        Programmdaten.instanceOf().setMAINFRAME_WINDOW_STATE(this.getSize());

        Programmdaten.instanceOf().setMAINFRAME_TAB(mainTabPane.getSelectedIndex());
        Einstellungen.instanceOf().save();

        Conn.shutdown();

        super.dispose();

    }
}
