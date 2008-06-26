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

import java.awt.event.WindowListener;
import mp3.Main;
import java.awt.BorderLayout;

import java.awt.Color;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import mp3.classes.interfaces.Constants;
import mp3.database.util.Conn;
import mp3.classes.interfaces.Strings;
import mp3.classes.layer.Popup;
import mp3.classes.layer.QueryClass;

import mp3.classes.objects.eur.Customer;
import mp3.classes.objects.ungrouped.MyData;
import mp3.classes.objects.product.Product;
import mp3.classes.objects.product.Supplier;
import mp3.classes.utils.Browser;
import mp3.classes.utils.WindowTools;
import mp3.classes.utils.Log;
import mp3.classes.utils.SaveAs;
import mp3.classes.utils.SplashScreen;
import mp3.classes.utils.TableHtmlWriter;
import mp3.classes.visual.sub.backupView;
import mp3.classes.visual.sub.billsView;

import mp3.classes.visual.sub.customersView;
//import mp3.classes.visual.sub.*;
import mp3.classes.visual.sub.historyView;
import mp3.classes.visual.sub.offersView;

import mp3.classes.visual.sub.productsView;
//import mp3.classes.visual.sub.*;
import mp3.classes.visual.sub.startView;
import mp3.classes.visual.sub.suppliersView;
import mp3.classes.visual.main.EinnahmenChart;
import mp3.classes.visual.main.UmsatzChart;
import mp3.classes.visual.sub.eurView;

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

    /** Creates new form mainframe
     * @param splash
     * @param mainclass 
     */
    public mainframe(SplashScreen splash, Main mainclass) {

        this.loader = mainclass;
        splash.setMessage("Initialisiere Oberfläche...");
        initComponents();

        splash.setMessage("Initialisiere Datenbank...");

        try {
            QueryClass.instanceOf();

        } catch (Exception exception) {
            this.dispose();
            splash.dispose();
            loader = null;
            System.gc();
            System.gc();
        }

        QueryClass.setProgressBarOn(this.mainProgress, this);


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


        jPanel4.add(d, BorderLayout.CENTER);
        jPanel3.add(b, BorderLayout.CENTER);
        jPanel7.add(f, BorderLayout.CENTER);
        jPanel2.add(c, BorderLayout.CENTER);
        jPanel9.add(e, BorderLayout.CENTER);
        jPanel6.add(g, BorderLayout.CENTER);
        jPanel8.add(h, BorderLayout.CENTER);
        jPanel11.add(i, BorderLayout.CENTER);
        jPanel12.add(j, BorderLayout.CENTER);

        try {
            jTabbedPane1.setSelectedIndex(MyData.instanceOf().getLasttab());
        } catch (Exception exception) {
        }

        try {
            this.setTitle("MP " + Constants.VERSION);
            this.setVisible(rootPaneCheckingEnabled);

        } catch (Exception exception) {
            splash.dispose();
            loader = null;
            System.gc();
            System.gc();
        }

        try {
            this.setSize(MyData.instanceOf().getMainframeSize());

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

        nachricht("Anmerkungen, Bugs und Feedback zu MP bitte an mp-rechnungs-und-kundenverwaltung@googlegroups.com senden. Vielen Dank!");
    }

    public customersView getCustomersView() {


        return c;
    }

    /** Creates new form mainframe
     * @param firststart
     * @param splash 
     */
    public mainframe(boolean firststart, SplashScreen splash) {
        initComponents();
        this.setExtendedState(mainframe.MAXIMIZED_BOTH);


        c = new customersView(this);
        b = new billsView(this);
        d = new productsView(this);
        e = new suppliersView(this);
        f = new offersView(this);
        g = new historyView(this);
        h = new backupView(this);
        i = new startView(this);
        j = new eurView(this);
        //c.setCustomer(new Customer(QueryClass.instanceOf(), "1"));



        jPanel4.add(d, BorderLayout.CENTER);
        jPanel3.add(b, BorderLayout.CENTER);
        jPanel7.add(f, BorderLayout.CENTER);
        jPanel2.add(c, BorderLayout.CENTER);
        jPanel9.add(e, BorderLayout.CENTER);
        jPanel6.add(g, BorderLayout.CENTER);
        jPanel8.add(h, BorderLayout.CENTER);
        jPanel11.add(i, BorderLayout.CENTER);
        jPanel12.add(j, BorderLayout.CENTER);

        QueryClass.setProgressBarOn(this.mainProgress, this);

        this.setVisible(rootPaneCheckingEnabled);

        if (firststart) {
            new settingsView(this).setVisible(firststart);
        }
        this.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent e) {
                close();
            }
        });
    }

    /**
     * 
     * @return 0:kunden
     *          1:rechnungen
     *          2:angebote
     *          3:produkte
     *          4:lieferanten
     *          5:verlauf  
     *          6:sicherung
     *          7:eur
     *          8:start 
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
     * 
     * 0:kunden
     *          1:rechnungen
     *          2:angebote
     *          3:produkte
     *          4:lieferanten
     *          5:verlauf  
     *          6:sicherung
     *          7:start 
     *          
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
        nachricht = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jPanel5 = new javax.swing.JPanel();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel2 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jPanel7 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        jPanel9 = new javax.swing.JPanel();
        jPanel6 = new javax.swing.JPanel();
        jPanel8 = new javax.swing.JPanel();
        jPanel12 = new javax.swing.JPanel();
        jPanel11 = new javax.swing.JPanel();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jMenu2 = new javax.swing.JMenu();
        jMenu3 = new javax.swing.JMenu();
        jMenuItem1 = new javax.swing.JMenuItem();
        jPanel10 = new javax.swing.JPanel();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jButton5 = new javax.swing.JButton();
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

        mainProgress.setBackground(new java.awt.Color(255, 255, 255));
        mainProgress.setBorderPainted(false);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addComponent(nachricht, javax.swing.GroupLayout.DEFAULT_SIZE, 715, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(mainProgress, javax.swing.GroupLayout.PREFERRED_SIZE, 61, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(mainProgress, javax.swing.GroupLayout.DEFAULT_SIZE, 16, Short.MAX_VALUE)
            .addComponent(nachricht, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 16, Short.MAX_VALUE)
        );

        jScrollPane1.setBorder(null);
        jScrollPane1.setAutoscrolls(true);

        jTabbedPane1.setTabPlacement(javax.swing.JTabbedPane.BOTTOM);
        jTabbedPane1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTabbedPane1MouseClicked(evt);
            }
        });
        jTabbedPane1.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                jTabbedPane1PropertyChange(evt);
            }
        });

        jPanel2.setLayout(new java.awt.BorderLayout());
        jTabbedPane1.addTab("Kunden        ", null, jPanel2, "Kunden bearbeiten");

        jPanel3.setLayout(new java.awt.BorderLayout());
        jTabbedPane1.addTab("Rechnungen", jPanel3);

        jPanel7.setLayout(new java.awt.BorderLayout());
        jTabbedPane1.addTab("Angebote ", jPanel7);

        jPanel4.setLayout(new java.awt.BorderLayout());
        jTabbedPane1.addTab("Produkte      ", jPanel4);

        jPanel9.setLayout(new java.awt.BorderLayout());
        jTabbedPane1.addTab("Lieferanten    ", jPanel9);

        jPanel6.setLayout(new java.awt.BorderLayout());
        jTabbedPane1.addTab("Verlauf           ", jPanel6);

        jPanel8.setLayout(new java.awt.BorderLayout());
        jTabbedPane1.addTab("Sicherung", jPanel8);

        jPanel12.setLayout(new java.awt.BorderLayout());
        jTabbedPane1.addTab("EUR/ Ausgabenbelege", jPanel12);

        jPanel11.setLayout(new java.awt.BorderLayout());
        jTabbedPane1.addTab("Start", jPanel11);

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 709, Short.MAX_VALUE)
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 392, Short.MAX_VALUE)
        );

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

        jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/bilder/exit.png"))); // NOI18N
        jButton1.setToolTipText("Sicherungsdatei anlegen und Anwendung beenden");
        jButton1.setBorderPainted(false);
        jButton1.setContentAreaFilled(false);
        jButton1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButton1MouseClicked(evt);
            }
        });
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/bilder/assistant.png"))); // NOI18N
        jButton2.setToolTipText("Hilfeforum");
        jButton2.setBorderPainted(false);
        jButton2.setContentAreaFilled(false);
        jButton2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButton2MouseClicked(evt);
            }
        });
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jButton3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/bilder/acroread.png"))); // NOI18N
        jButton3.setToolTipText("Adobe Reader öffnen");
        jButton3.setBorderPainted(false);
        jButton3.setContentAreaFilled(false);
        jButton3.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButton3MouseClicked(evt);
            }
        });

        jButton5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/bilder/calc.png"))); // NOI18N
        jButton5.setToolTipText("Hilfeforum");
        jButton5.setBorderPainted(false);
        jButton5.setContentAreaFilled(false);
        jButton5.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButton5MouseClicked(evt);
            }
        });

        javax.swing.GroupLayout jPanel10Layout = new javax.swing.GroupLayout(jPanel10);
        jPanel10.setLayout(jPanel10Layout);
        jPanel10Layout.setHorizontalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton5, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel10Layout.setVerticalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addGap(58, 58, 58)
                .addComponent(jButton3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 143, Short.MAX_VALUE)
                .addComponent(jButton1)
                .addContainerGap())
        );

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

        jMenuItem8.setIcon(new javax.swing.ImageIcon(getClass().getResource("/bilder/about.gif"))); // NOI18N
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
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 709, Short.MAX_VALUE))
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 392, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
    private void jTabbedPane1PropertyChange (java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_jTabbedPane1PropertyChange
    }//GEN-LAST:event_jTabbedPane1PropertyChange

    private void jTabbedPane1MouseClicked (java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTabbedPane1MouseClicked
    }//GEN-LAST:event_jTabbedPane1MouseClicked

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
        new WindowTools(l);
        l.setVisible(rootPaneCheckingEnabled);
    }//GEN-LAST:event_jMenuItem8ActionPerformed

    private void jMenuItem6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem6ActionPerformed
        try {


            Process proc = Runtime.getRuntime().exec(MyData.instanceOf().getBrowser() + " http://www.supernature-forum.de");
        } catch (IOException ex) {

            new Popup("Kein Browser angegeben. Wählen Sie Ihren Internetbrowser unter 'Programmeinstellungen'.");
        // Logger.getLogger(mainFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_jMenuItem6ActionPerformed

    private void jButton3MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton3MouseClicked

        MyData l = MyData.instanceOf();
        try {

//new out(settings.getPdfViewer() +" "+filename);
            Process proc = Runtime.getRuntime().exec(l.getPdfviewer());
        } catch (Exception ex) {

            Popup.notice("Kein PDF-Programm angegeben. Wählen Sie Ihren PDF Reader unter 'Programmeinstellungen'.");

        }
    }//GEN-LAST:event_jButton3MouseClicked

    private void jButton2MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton2MouseClicked
        try {


            Process proc = Runtime.getRuntime().exec(MyData.instanceOf().getBrowser() + " http://www.supernature-forum.de");
        } catch (IOException ex) {

            new Popup("Kein Browser angegeben. Wählen Sie Ihren Internetbrowser unter 'Programmeinstellungen'.");
        // Logger.getLogger(mainFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_jButton2MouseClicked

    private void jButton1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton1MouseClicked

        close();
    }//GEN-LAST:event_jButton1MouseClicked

    private void close() {


        try {
            MyData.instanceOf().setState(this.getHeight(), this.getWidth());
            h.saving();

            MyData.instanceOf().setLasttab(String.valueOf(jTabbedPane1.getSelectedIndex()));
            MyData.instanceOf().save();

            System.exit(0);
        } catch (Exception e) {
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
         * @return 0:kunden
         *          1:rechnungen
         *          2:angebote
         *          3:produkte
         *          4:lieferanten
         *          5:verlauf  
         *          6:sicherung
         *          7:start 
         *          
         */
        switch (this.getShowingTab()) {

            case 0:

                this.getCustomersView().save();

                break;
            case 1:

                this.getB().save();

                break;
            case 2:
                this.getOrdersView().save();
                break;
            case 3:
                this.getProductsView().save();

                break;
            case 4:
                this.getC().save();
                break;
        }




    }//GEN-LAST:event_jMenuItem12ActionPerformed

    private void jMenuItem13ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem13ActionPerformed
        System.exit(0);
    }//GEN-LAST:event_jMenuItem13ActionPerformed

    private void jMenuItem14ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem14ActionPerformed

        MyData.instanceOf().setState(this.getHeight(), this.getWidth());

        h.saving();

        MyData.instanceOf().setLasttab(String.valueOf(jTabbedPane1.getSelectedIndex()));
        MyData.instanceOf().save();

        System.exit(0);

    }//GEN-LAST:event_jMenuItem14ActionPerformed

    private void jButton5MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton5MouseClicked
        calculator.instanceOf().setVisible(rootPaneCheckingEnabled);
    }//GEN-LAST:event_jButton5MouseClicked

    private void jMenuItem15ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem15ActionPerformed
        String fils = "";
        try {

            Customer k = new Customer(QueryClass.instanceOf());

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

            Supplier k = new Supplier(QueryClass.instanceOf());

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

            Product k = new Product(QueryClass.instanceOf());

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

private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
// TODO add your handling code here:
}//GEN-LAST:event_jButton2ActionPerformed

private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
// TODO add your handling code here:
}//GEN-LAST:event_jButton1ActionPerformed

    @Override
    public void finalize() {
        Conn.shutdown();

        System.gc();

    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton5;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu10;
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
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel12;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JSeparator jSeparator4;
    private javax.swing.JSeparator jSeparator5;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JProgressBar mainProgress;
    private javax.swing.JLabel nachricht;
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

    public javax.swing.JPanel getJPanel2() {
        return jPanel2;
    }

    public javax.swing.JPanel getJPanel3() {
        return jPanel3;
    }

    public javax.swing.JPanel getJPanel4() {
        return jPanel4;
    }

    public javax.swing.JPanel getJPanel5() {
        return jPanel5;
    }

    public javax.swing.JPanel getJPanel6() {
        return jPanel6;
    }

    public javax.swing.JPanel getJPanel7() {
        return jPanel7;
    }

    public javax.swing.JPanel getJPanel9() {
        return jPanel9;
    }

    public javax.swing.JScrollPane getJScrollPane1() {
        return jScrollPane1;
    }

    public javax.swing.JTabbedPane getJTabbedPane1() {
        return jTabbedPane1;
    }

    public javax.swing.JProgressBar getMainProgress() {
        return mainProgress;
    }

    public suppliersView getC() {
        return e;
    }

    public billsView getB() {
        return b;
    }

    /**
     * 
     * @param message
     */
    public void nachricht(String message) {

        nachricht.setText(message);
    }

    public javax.swing.JLabel getNachricht() {
        return nachricht;
    }

    @Override
    public void dispose() {

        MyData.instanceOf().setState(this.getHeight(), this.getWidth());

        MyData.instanceOf().setLasttab(String.valueOf(jTabbedPane1.getSelectedIndex()));
        MyData.instanceOf().save();

        Conn.shutdown();

        super.dispose();

    }
}
