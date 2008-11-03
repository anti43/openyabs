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
package mp4.frames;

import calculator.Rechner;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import mp4.benutzerverwaltung.visual.login;
import mp4.datenbank.verbindung.Conn;
import mp4.interfaces.Lockable;
import mp4.items.LockableContainer;
import mp4.items.visual.csvProductImporter;
import mp4.items.visual.fastChoice;
import mp4.items.visual.serialLetter;

import mp4.items.Rechnung;
import mp4.main.Main;


import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Date;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

import mp4.globals.Constants;

import mp4.globals.Strings;
import mp4.items.visual.Popup;
import mp4.datenbank.verbindung.ConnectionHandler;


import mp4.logs.*;

import mp4.frames.LicenseWindow;
import mp4.panels.misc.diagrammView;
import mp4.panels.rechnungen.billsView;

import mp4.panels.kontakte.customersView;
//import mp3.classes.visual.sub.*;
import mp4.panels.eur.eurEURPanel;
import mp4.panels.eur.eurOPanel;
import mp4.panels.rechnungen.offersView;


//import mp3.classes.visual.sub.*;
import mp4.panels.misc.startView;



import mp4.benutzerverwaltung.visual.Verwaltung;
import mp4.datenbank.verbindung.Query;
import mp4.items.Kunde;
import mp4.items.Lieferant;
import mp4.einstellungen.Einstellungen;
import mp4.einstellungen.Programmdaten;
import mp4.items.HistoryItem;
import mp4.items.Product;
import mp4.benutzerverwaltung.User;
import mp4.datenbank.verbindung.DataLock;
import mp4.items.Angebot;

import mp4.items.ClipBoard;
import mp4.items.Hersteller;
import mp4.items.People;
import mp4.items.Things;
import mp4.items.visual.CommonPanel;
import mp4.items.visual.csvKontaktImporter;
import mp4.panels.eur.eurAPanel;
import mp4.panels.eur.eurEPanel;
import mp4.panels.kontakte.manufacturerView;
import mp4.panels.kontakte.suppliersView;
import mp4.panels.misc.SplashScreen;
import mp4.panels.produkte.productsView;
import mp4.panels.produkte.servicesView;
import mp4.panels.misc.TaxRatesEditor;
import mp4.panels.misc.backupView;
import mp4.panels.misc.diagrammChooseView;
import mp4.panels.misc.historyView;
import mp4.panels.misc.settingsView;

import mp4.plugin.mpplugin;
import mp4.plugin.pluginHandler;
import mp4.utils.datum.DateConverter;
import mp4.utils.export.textdatei.Kontaktliste;
import mp4.utils.export.textdatei.Produktliste;
import mp4.utils.files.TextDatFile;
import mp4.utils.files.Browser;

import mp4.utils.files.DialogForFile;
import mp4.utils.files.TableHtmlWriter;
import mp4.utils.importe.daten.ImportDaten;
import mp4.utils.listen.ArrayUtils;
import mp4.utils.tasks.Job;
import mp4.utils.ui.FadeOnChangeLabel;
import mp4.utils.ui.Position;
import mp4.utils.ui.TabCloseIcon;

/**
 *
 * @author  anti43
 */
public class mainframe extends javax.swing.JFrame {

    private static final long serialVersionUID = 518894730966031646L;

    public static void setErrorText(String string) {
        try {
            ((FadeOnChangeLabel) nachricht).setFadeColor(Color.RED);
            ((FadeOnChangeLabel) nachricht).setText(string);
        } catch (Exception e) {
            Log.Debug(e);
        }

    }

    public static void setInfoText(String string) {
        try {
            ((FadeOnChangeLabel) nachricht).reset();
        } catch (Exception e) {
            Log.Debug(e);
        }
        nachricht.setText(string);
    }

    public static void setWaiting(boolean b) {
        if (b) {
            identifier.setCursor(new Cursor(Cursor.WAIT_CURSOR));
        } else {
            identifier.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        }
    }
    private startView i;
    private Main loader;
    private Position wt = new Position();
    public static JLabel nachricht = new JLabel();
    public static mainframe identifier;
    private static User currentUser = new User(1);
    private historyView verlaufpanel;
    private eurEURPanel eurpanel;
    private eurEPanel eurEpanel;
    private eurAPanel eurApanel;
    private eurOPanel eurOpanel;
    private backupView backuppanel;
    private settingsView settingspanel;
    private diagrammChooseView diagrammpanel;
    private DialogForFile dialog;
    private ClipBoard clipBoard = new ClipBoard(this);
    private pluginHandler pluginhandler;

    /** Creates new form mainframe
     * @param splash
     * @param mainclass 
     */
    public mainframe(SplashScreen splash, Main mainclass) {

        this.loader = mainclass;
        mainframe.identifier = this;
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

        i = new startView(this);
        mainTabPane.add("Start", i);

        this.setTitle("MP " + Constants.VERSION);

        try {
            this.setSize(Programmdaten.instanceOf().getMAINFRAME_WINDOW_STATE());
            if (wt.isNotMaximized(this)) {
                wt.center(this);
            } else {
                this.setExtendedState(mainframe.MAXIMIZED_BOTH);
            }
        } catch (Exception exception) {
            Log.Debug(this, exception.getMessage(), true);
            this.setExtendedState(mainframe.NORMAL);
        }

        this.addWindowListener(new WindowAdapter() {

            @Override
            public void windowStateChanged(WindowEvent e) {
                if (identifier.getState() != mainframe.MAXIMIZED_BOTH) {
                    wt.center(identifier);
                }
            }

            @Override
            public void windowClosing(WindowEvent e) {
                close();
            }
        });

        nachricht = messagePanel;
        pluginhandler = new pluginHandler(this);
        splash.setMessage("Sitzungswiederherstellung...");
        restoreSession();

        this.setVisible(rootPaneCheckingEnabled);
        if (Programmdaten.instanceOf().getUSE_AUTHENTIFICATION()) {
            this.setEnabled(false);
            new login(this);
        }

        
        this.requestFocus();
        setMessage(Strings.WELCOME_MESSAGE);
    }

    public void addAngebotPanel(Angebot offer) {

        offersView panel = new offersView(this);

        mainTabPane.add("Angebot", panel);
        mainTabPane.setSelectedComponent(panel);
        mainTabPane.setIconAt(mainTabPane.getSelectedIndex(), new TabCloseIcon());

        if (offer != null) {
            panel.setAngebot(offer);
        }
        mainTabPane.validate();
    }

    public void addBillPanel(Rechnung bill) {
        billsView panel = new billsView(this);
        mainTabPane.add("Rechnung", panel);
        mainTabPane.setSelectedComponent(panel);
        mainTabPane.setIconAt(mainTabPane.getSelectedIndex(), new TabCloseIcon());
        if (bill != null) {
            panel.setBill(bill);
        }
        mainTabPane.validate();
    }

    public void addDiagramm(diagrammView panel) {
        mainTabPane.add("Diagramm", panel);
        mainTabPane.setSelectedComponent(panel);
        mainTabPane.setIconAt(mainTabPane.getSelectedIndex(), new TabCloseIcon());
        mainTabPane.validate();
    }

    public void addPlugin(final mpplugin elem) {
        final JMenuItem jmi = new JCheckBoxMenuItem(elem.getName() + " (" + elem.getVendor() + ")");
        jmi.addActionListener(
                new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        identifier.addPluginPanel(elem);
                    }
                });
        jmi.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    Programmdaten.instanceOf().addONLOAD_PLUGIN(elem);
                } else {
                    Programmdaten.instanceOf().removeONLOAD_PLUGIN(elem);
                }
            }
        });

        pluginMenu.add(jmi);
    }

    public void addPluginPanel(mpplugin elem) {
        mainTabPane.remove(elem);
        elem.load(this);
        mainTabPane.add(elem.getName(), elem);
        mainTabPane.setSelectedComponent(elem);
        mainTabPane.setIconAt(mainTabPane.getSelectedIndex(), new TabCloseIcon());
        mainTabPane.validate();
    }

    public void addProductPanel(Product product) {
        productsView panel = new productsView(this);
        mainTabPane.add("Produkt", panel);
        mainTabPane.setSelectedComponent(panel);
        mainTabPane.setIconAt(mainTabPane.getSelectedIndex(), new TabCloseIcon());
        if (product != null) {
            panel.setProduct(product);
        }
        mainTabPane.validate();
    }

    public void addPanel(String string, JPanel panel) {
        mainTabPane.add(string, panel);
        mainTabPane.setSelectedComponent(panel);
        mainTabPane.setIconAt(mainTabPane.getSelectedIndex(), new TabCloseIcon());
        mainTabPane.validate();
    }

    public void removePanel(JPanel panel) {
        try {
            mainTabPane.remove(panel);
        } catch (Exception e) {
        }
        mainTabPane.validate();
    }

    public void resize() {
        wt.center(this);
    }

    public int getShowingTab() {
        return getTabPane().getSelectedIndex();
    }

    public void setShowingTab(int i) {
        getTabPane().setSelectedIndex(i);
    }

    public boolean checkAuth(String user, String password) {

        User usern = new User();

        if (usern.checkUser(user, password)) {
            setEnabled(true);
            usern.setUseAuth(true);
            this.setUser(usern);
            Einstellungen.instanceOf().setUser(usern);
            return true;
        } else {
            setEnabled(false);
            return false;
        }
    }

    public static User getUser() {
        return currentUser;
    }

    private void closeTabs() {
        for (int ixd = 0; ixd < mainTabPane.getComponents().length; ixd++) {
              try {
                ((mpplugin) mainTabPane.getComponentAt(ixd)).unload();
                Log.Debug(this,"Unloaded a Plugin: " + ((mpplugin) mainTabPane.getComponentAt(ixd)).getName());
            } catch (Exception e) {}
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        bottomPanel = new javax.swing.JPanel();
        messagePanel = new FadeOnChangeLabel();
        mainProgressBar = new javax.swing.JProgressBar();
        mainScrollPane = new javax.swing.JScrollPane();
        mainTabPane = new javax.swing.JTabbedPane();
        leftBar = new javax.swing.JPanel();
        outlookBar = new com.l2fprod.common.swing.JOutlookBar();
        jPanel2 = new javax.swing.JPanel();
        jButton4 = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton18 = new javax.swing.JButton();
        jPanel4 = new javax.swing.JPanel();
        jButton9 = new javax.swing.JButton();
        jButton10 = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jButton5 = new javax.swing.JButton();
        jButton6 = new javax.swing.JButton();
        jButton7 = new javax.swing.JButton();
        jButton11 = new javax.swing.JButton();
        jButton8 = new javax.swing.JButton();
        jPanel6 = new javax.swing.JPanel();
        jButton12 = new javax.swing.JButton();
        jButton13 = new javax.swing.JButton();
        jButton14 = new javax.swing.JButton();
        jButton15 = new javax.swing.JButton();
        jButton16 = new javax.swing.JButton();
        closeButton = new javax.swing.JButton();
        logoutButton = new javax.swing.JButton();
        menuBar = new javax.swing.JMenuBar();
        jMenu4 = new javax.swing.JMenu();
        jMenu9 = new javax.swing.JMenu();
        jMenuItem10 = new javax.swing.JMenuItem();
        jMenuItem11 = new javax.swing.JMenuItem();
        jSeparator4 = new javax.swing.JSeparator();
        jMenu6 = new javax.swing.JMenu();
        jMenuItem17 = new javax.swing.JMenuItem();
        jMenuItem2 = new javax.swing.JMenuItem();
        jMenuItem3 = new javax.swing.JMenuItem();
        jMenu12 = new javax.swing.JMenu();
        jMenu11 = new javax.swing.JMenu();
        jMenuItem24 = new javax.swing.JMenuItem();
        jMenuItem9 = new javax.swing.JMenuItem();
        jMenu13 = new javax.swing.JMenu();
        jMenuItem27 = new javax.swing.JMenuItem();
        jMenuItem19 = new javax.swing.JMenuItem();
        jMenu10 = new javax.swing.JMenu();
        jMenuItem28 = new javax.swing.JMenuItem();
        jMenuItem26 = new javax.swing.JMenuItem();
        jMenu8 = new javax.swing.JMenu();
        jMenuItem29 = new javax.swing.JMenuItem();
        jMenuItem12 = new javax.swing.JMenuItem();
        jSeparator3 = new javax.swing.JSeparator();
        jMenuItem13 = new javax.swing.JMenuItem();
        jMenuItem14 = new javax.swing.JMenuItem();
        jMenu5 = new javax.swing.JMenu();
        jMenuItem16 = new javax.swing.JMenuItem();
        clipboard = new javax.swing.JMenu();
        jSeparator2 = new javax.swing.JSeparator();
        jMenuItem4 = new javax.swing.JMenuItem();
        jMenu1 = new javax.swing.JMenu();
        jMenuItem1 = new javax.swing.JMenuItem();
        jMenuItem25 = new javax.swing.JMenuItem();
        jMenuItem5 = new javax.swing.JMenuItem();
        jMenuItem7 = new javax.swing.JMenuItem();
        pluginMenu = new javax.swing.JMenu();
        jMenuItem20 = new javax.swing.JMenuItem();
        jMenuItem18 = new javax.swing.JMenuItem();
        jMenu2 = new javax.swing.JMenu();
        jMenu3 = new javax.swing.JMenu();
        jMenuItem22 = new javax.swing.JMenuItem();
        jMenuItem23 = new javax.swing.JMenuItem();
        jMenuItem30 = new javax.swing.JMenuItem();
        jMenu7 = new javax.swing.JMenu();
        jMenuItem6 = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JSeparator();
        jMenuItem8 = new javax.swing.JMenuItem();
        jMenuItem15 = new javax.swing.JMenuItem();
        jSeparator5 = new javax.swing.JSeparator();
        jMenuItem21 = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("MP");
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        setMinimumSize(new java.awt.Dimension(800, 600));

        bottomPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        mainProgressBar.setBorderPainted(false);

        javax.swing.GroupLayout bottomPanelLayout = new javax.swing.GroupLayout(bottomPanel);
        bottomPanel.setLayout(bottomPanelLayout);
        bottomPanelLayout.setHorizontalGroup(
            bottomPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, bottomPanelLayout.createSequentialGroup()
                .addComponent(messagePanel, javax.swing.GroupLayout.DEFAULT_SIZE, 375, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(mainProgressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        bottomPanelLayout.setVerticalGroup(
            bottomPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, bottomPanelLayout.createSequentialGroup()
                .addGroup(bottomPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(messagePanel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(mainProgressBar, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, 0))
        );

        mainScrollPane.setBorder(null);
        mainScrollPane.setAutoscrolls(true);

        mainTabPane.setTabLayoutPolicy(javax.swing.JTabbedPane.SCROLL_TAB_LAYOUT);
        mainTabPane.setAutoscrolls(true);
        mainTabPane.setDoubleBuffered(true);
        mainTabPane.setFocusCycleRoot(true);
        mainTabPane.setFocusTraversalPolicyProvider(true);
        mainTabPane.setOpaque(true);
        mainScrollPane.setViewportView(mainTabPane);

        leftBar.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        outlookBar.setBackground(new java.awt.Color(204, 204, 204));
        outlookBar.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jPanel2.setBackground(new java.awt.Color(153, 153, 153));

        jButton4.setFont(new java.awt.Font("Tahoma", 0, 10));
        jButton4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/bilder/3232/agt_family.png"))); // NOI18N
        jButton4.setText("Kunden");
        jButton4.setToolTipText("Kunden");
        jButton4.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton4.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        jButton1.setFont(new java.awt.Font("Tahoma", 0, 10));
        jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/bilder/3232/edit_user.png"))); // NOI18N
        jButton1.setText("Lieferanten");
        jButton1.setToolTipText("Lieferanten");
        jButton1.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton1.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton2.setFont(new java.awt.Font("Tahoma", 0, 10));
        jButton2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/bilder/medium/adept_update.png"))); // NOI18N
        jButton2.setText("Angebote");
        jButton2.setToolTipText("Angebote");
        jButton2.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton2.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jButton18.setFont(new java.awt.Font("Tahoma", 0, 10));
        jButton18.setIcon(new javax.swing.ImageIcon(getClass().getResource("/bilder/3232/edit_user.png"))); // NOI18N
        jButton18.setText("Hersteller");
        jButton18.setToolTipText("Hersteller");
        jButton18.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton18.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton18.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton18ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jButton4, javax.swing.GroupLayout.DEFAULT_SIZE, 86, Short.MAX_VALUE)
                        .addContainerGap())
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jButton2, javax.swing.GroupLayout.DEFAULT_SIZE, 85, Short.MAX_VALUE)
                        .addGap(11, 11, 11))))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addGap(11, 11, 11)
                .addComponent(jButton1, 0, 0, Short.MAX_VALUE)
                .addContainerGap())
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jButton18, 0, 0, Short.MAX_VALUE)
                .addGap(11, 11, 11))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jButton4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButton1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButton18)
                .addGap(90, 90, 90)
                .addComponent(jButton2)
                .addGap(102, 102, 102))
        );

        outlookBar.addTab("Kontakte", jPanel2);

        jPanel4.setBackground(new java.awt.Color(153, 153, 153));

        jButton9.setFont(new java.awt.Font("Tahoma", 0, 10));
        jButton9.setIcon(new javax.swing.ImageIcon(getClass().getResource("/bilder/3232/blockdevice.png"))); // NOI18N
        jButton9.setText("Produkte");
        jButton9.setToolTipText("Produkte");
        jButton9.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton9.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton9.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton9ActionPerformed(evt);
            }
        });

        jButton10.setFont(new java.awt.Font("Tahoma", 0, 10));
        jButton10.setIcon(new javax.swing.ImageIcon(getClass().getResource("/bilder/3232/share.png"))); // NOI18N
        jButton10.setText("Services");
        jButton10.setToolTipText("Services");
        jButton10.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton10.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton10.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton10ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jButton10, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 77, Short.MAX_VALUE)
                    .addComponent(jButton9, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jButton10)
                .addGap(11, 11, 11)
                .addComponent(jButton9)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        outlookBar.addTab("Produkte", jPanel4);

        jPanel3.setBackground(new java.awt.Color(153, 153, 153));

        jButton5.setFont(new java.awt.Font("Tahoma", 0, 10));
        jButton5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/bilder/small/next.png"))); // NOI18N
        jButton5.setText("Einnahmen");
        jButton5.setToolTipText("Einnahmen");
        jButton5.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton5.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton5ActionPerformed(evt);
            }
        });

        jButton6.setFont(new java.awt.Font("Tahoma", 0, 10));
        jButton6.setIcon(new javax.swing.ImageIcon(getClass().getResource("/bilder/small/previous.png"))); // NOI18N
        jButton6.setText("Ausgaben");
        jButton6.setToolTipText("Ausgaben");
        jButton6.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton6.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton6ActionPerformed(evt);
            }
        });

        jButton7.setFont(new java.awt.Font("Tahoma", 0, 10));
        jButton7.setIcon(new javax.swing.ImageIcon(getClass().getResource("/bilder/medium/adept_update.png"))); // NOI18N
        jButton7.setText("Offen");
        jButton7.setToolTipText("Offen");
        jButton7.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton7.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton7ActionPerformed(evt);
            }
        });

        jButton11.setFont(new java.awt.Font("Tahoma", 0, 10));
        jButton11.setIcon(new javax.swing.ImageIcon(getClass().getResource("/bilder/small/business.png"))); // NOI18N
        jButton11.setText("Rechnung");
        jButton11.setToolTipText("Rechnungen");
        jButton11.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton11.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton11.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton11ActionPerformed(evt);
            }
        });

        jButton8.setFont(new java.awt.Font("Tahoma", 0, 10));
        jButton8.setIcon(new javax.swing.ImageIcon(getClass().getResource("/bilder/3232/kspread.png"))); // NOI18N
        jButton8.setText("Übersicht");
        jButton8.setToolTipText("Übersicht");
        jButton8.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton8.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton8ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jButton11, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 85, Short.MAX_VALUE)
                    .addComponent(jButton6, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 85, Short.MAX_VALUE)
                    .addComponent(jButton5, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButton7, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 85, Short.MAX_VALUE)
                    .addComponent(jButton8, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 85, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jButton5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton6)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton11)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton7)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton8)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        outlookBar.addTab("Buchhaltung", jPanel3);

        jPanel6.setBackground(new java.awt.Color(153, 153, 153));

        jButton12.setFont(new java.awt.Font("Tahoma", 0, 10));
        jButton12.setIcon(new javax.swing.ImageIcon(getClass().getResource("/bilder/3232/kghostview.png"))); // NOI18N
        jButton12.setText("Verlauf");
        jButton12.setToolTipText("Verlauf");
        jButton12.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton12.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton12.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton12ActionPerformed(evt);
            }
        });

        jButton13.setFont(new java.awt.Font("Tahoma", 0, 10));
        jButton13.setIcon(new javax.swing.ImageIcon(getClass().getResource("/bilder/3232/file-manager.png"))); // NOI18N
        jButton13.setText("Sicherung");
        jButton13.setToolTipText("Sicherung");
        jButton13.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton13.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton13.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton13ActionPerformed(evt);
            }
        });

        jButton14.setFont(new java.awt.Font("Tahoma", 0, 10));
        jButton14.setIcon(new javax.swing.ImageIcon(getClass().getResource("/bilder/3232/configure.png"))); // NOI18N
        jButton14.setText("Einstellung");
        jButton14.setToolTipText("Einstellungen");
        jButton14.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton14.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton14.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton14ActionPerformed(evt);
            }
        });

        jButton15.setFont(new java.awt.Font("Tahoma", 0, 10));
        jButton15.setIcon(new javax.swing.ImageIcon(getClass().getResource("/bilder/3232/log.png"))); // NOI18N
        jButton15.setText("Statistik");
        jButton15.setToolTipText("Auswertungen");
        jButton15.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton15.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton15.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton15ActionPerformed(evt);
            }
        });

        jButton16.setFont(new java.awt.Font("Tahoma", 0, 10));
        jButton16.setIcon(new javax.swing.ImageIcon(getClass().getResource("/bilder/3232/calc.png"))); // NOI18N
        jButton16.setText("Rechner");
        jButton16.setToolTipText("Auswertungen");
        jButton16.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton16.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton16.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton16ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButton14, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButton15, javax.swing.GroupLayout.DEFAULT_SIZE, 83, Short.MAX_VALUE)
                    .addComponent(jButton13, javax.swing.GroupLayout.DEFAULT_SIZE, 83, Short.MAX_VALUE)
                    .addComponent(jButton16, javax.swing.GroupLayout.DEFAULT_SIZE, 83, Short.MAX_VALUE)
                    .addComponent(jButton12, javax.swing.GroupLayout.DEFAULT_SIZE, 83, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jButton12)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButton13)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButton14)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButton15)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButton16)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        outlookBar.addTab("Extras", jPanel6);

        closeButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/bilder/3232/endturn.png"))); // NOI18N
        closeButton.setToolTipText("Datensicherung anlegen und Programm beenden");
        closeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeButtonActionPerformed(evt);
            }
        });

        logoutButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/bilder/3232/lock.png"))); // NOI18N
        logoutButton.setToolTipText("Programm sperren");
        logoutButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                logoutButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout leftBarLayout = new javax.swing.GroupLayout(leftBar);
        leftBar.setLayout(leftBarLayout);
        leftBarLayout.setHorizontalGroup(
            leftBarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(leftBarLayout.createSequentialGroup()
                .addComponent(outlookBar, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, leftBarLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(closeButton, javax.swing.GroupLayout.DEFAULT_SIZE, 90, Short.MAX_VALUE)
                .addContainerGap())
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, leftBarLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(logoutButton, javax.swing.GroupLayout.DEFAULT_SIZE, 90, Short.MAX_VALUE)
                .addContainerGap())
        );
        leftBarLayout.setVerticalGroup(
            leftBarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, leftBarLayout.createSequentialGroup()
                .addComponent(outlookBar, javax.swing.GroupLayout.PREFERRED_SIZE, 461, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 17, Short.MAX_VALUE)
                .addComponent(logoutButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(closeButton)
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
        jMenu4.add(jSeparator4);

        jMenu6.setText("Importieren..");

        jMenuItem17.setText("Kontakte (CSV)");
        jMenuItem17.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem17ActionPerformed(evt);
            }
        });
        jMenu6.add(jMenuItem17);

        jMenuItem2.setText("Produkte (CSV)");
        jMenuItem2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem1ActionPerformed(evt);
            }
        });
        jMenu6.add(jMenuItem2);

        jMenuItem3.setText("Daten (MP Version 3.x)");
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

        jMenu4.add(jMenu6);

        jMenu12.setText("Exportieren..");

        jMenu11.setText("Kunden");

        jMenuItem24.setText("CSV");
        jMenuItem24.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem24ActionPerformed(evt);
            }
        });
        jMenu11.add(jMenuItem24);

        jMenuItem9.setText("HTML");
        jMenuItem9.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem9ActionPerformed(evt);
            }
        });
        jMenu11.add(jMenuItem9);

        jMenu12.add(jMenu11);

        jMenu13.setText("Lieferanten");

        jMenuItem27.setText("CSV");
        jMenuItem27.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem27ActionPerformed(evt);
            }
        });
        jMenu13.add(jMenuItem27);

        jMenuItem19.setText("HTML");
        jMenuItem19.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem19ActionPerformed(evt);
            }
        });
        jMenu13.add(jMenuItem19);

        jMenu12.add(jMenu13);

        jMenu10.setText("Hersteller");

        jMenuItem28.setText("CSV");
        jMenuItem28.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem28ActionPerformed(evt);
            }
        });
        jMenu10.add(jMenuItem28);

        jMenuItem26.setText("HTML");
        jMenuItem26.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem26ActionPerformed(evt);
            }
        });
        jMenu10.add(jMenuItem26);

        jMenu12.add(jMenu10);

        jMenu8.setText("Produkte");

        jMenuItem29.setText("CSV");
        jMenuItem29.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem29ActionPerformed(evt);
            }
        });
        jMenu8.add(jMenuItem29);

        jMenuItem12.setText("HTML");
        jMenuItem12.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem12ActionPerformed(evt);
            }
        });
        jMenu8.add(jMenuItem12);

        jMenu12.add(jMenu8);

        jMenu4.add(jMenu12);
        jMenu4.add(jSeparator3);

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

        menuBar.add(jMenu4);

        jMenu5.setText("Bearbeiten");

        jMenuItem16.setText("Kopieren");
        jMenuItem16.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem16ActionPerformed(evt);
            }
        });
        jMenu5.add(jMenuItem16);

        clipboard.setText("Einfügen");
        jMenu5.add(clipboard);
        jMenu5.add(jSeparator2);

        jMenuItem4.setText("Einstellungen");
        jMenuItem4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem4ActionPerformed(evt);
            }
        });
        jMenu5.add(jMenuItem4);

        menuBar.add(jMenu5);

        jMenu1.setText("Tools");

        jMenuItem1.setText("Authentifizierung Ein/Aus");
        jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem1ActionPerformed1(evt);
            }
        });
        jMenu1.add(jMenuItem1);

        jMenuItem25.setText("Datenbankwerkzeug");
        jMenuItem25.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem25ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem25);

        jMenuItem5.setText("Benutzerverwaltung");
        jMenuItem5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem5ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem5);

        jMenuItem7.setText("Steuersatzverwaltung");
        jMenuItem7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem7ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem7);

        pluginMenu.setText("Plugins");

        jMenuItem20.setText("Liste neu laden");
        jMenuItem20.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem20ActionPerformed(evt);
            }
        });
        pluginMenu.add(jMenuItem20);

        jMenu1.add(pluginMenu);

        jMenuItem18.setText("Serienbrief (Default Drucker)");
        jMenuItem18.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem18ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem18);

        menuBar.add(jMenu1);

        jMenu2.setText("Ansicht");

        jMenu3.setText("Vorlagen");

        jMenuItem22.setIcon(new javax.swing.ImageIcon(getClass().getResource("/bilder/small/ok.png"))); // NOI18N
        jMenuItem22.setText("Normal");
        jMenuItem22.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem22ActionPerformed(evt);
            }
        });
        jMenu3.add(jMenuItem22);

        jMenuItem23.setText("Medizinisch");
        jMenuItem23.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem23ActionPerformed(evt);
            }
        });
        jMenu3.add(jMenuItem23);

        jMenu2.add(jMenu3);

        jMenuItem30.setText("Alle Tabs schließen");
        jMenuItem30.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem30ActionPerformed(evt);
            }
        });
        jMenu2.add(jMenuItem30);

        menuBar.add(jMenu2);

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

        jMenuItem15.setText("Über..");
        jMenuItem15.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem15ActionPerformed(evt);
            }
        });
        jMenu7.add(jMenuItem15);
        jMenu7.add(jSeparator5);

        jMenuItem21.setText("Log Konsole");
        jMenuItem21.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem21ActionPerformed(evt);
            }
        });
        jMenu7.add(jMenuItem21);

        menuBar.add(jMenu7);

        setJMenuBar(menuBar);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(3, 3, 3)
                .addComponent(leftBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(mainScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 410, Short.MAX_VALUE))
            .addComponent(bottomPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(mainScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 586, Short.MAX_VALUE)
                    .addComponent(leftBar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(bottomPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem1ActionPerformed
        csvProductImporter.instanceOf();
    }//GEN-LAST:event_jMenuItem1ActionPerformed

    private void jMenuItem3MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jMenuItem3MouseClicked
    }//GEN-LAST:event_jMenuItem3MouseClicked

    private void jMenuItem3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem3ActionPerformed
        new ImportDaten();
    }//GEN-LAST:event_jMenuItem3ActionPerformed

    private void jMenuItem8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem8ActionPerformed
        LicenseWindow l = new LicenseWindow();
        wt.center(l);
        l.setVisible(rootPaneCheckingEnabled);
    }//GEN-LAST:event_jMenuItem8ActionPerformed

    private void jMenuItem6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem6ActionPerformed

        new Browser("http://groups.google.com/group/mp-rechnungs-und-kundenverwaltung");

    }//GEN-LAST:event_jMenuItem6ActionPerformed

    private void close() {

        saveSession();
        
        if(Programmdaten.instanceOf().getSAVE_DB_ON_EXIT())new backupView(this).saving();

        try {
            DataLock.lateRelease();

            Programmdaten.instanceOf().setMAINFRAME_WINDOW_STATE(this.getSize());
            Programmdaten.instanceOf().setMAINFRAME_TAB(mainTabPane.getSelectedIndex());
            
            closeTabs();

            System.exit(0);
        } catch (Exception exc) {
            Log.Debug(this, exc, true);
            System.exit(0);
        }
    }

    private void jMenuItem10ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem10ActionPerformed
        new fastChoice(this, 0);
    }//GEN-LAST:event_jMenuItem10ActionPerformed

    private void jMenuItem11ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem11ActionPerformed
        new fastChoice(this, 1);
    }//GEN-LAST:event_jMenuItem11ActionPerformed

    private void jMenuItem13ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem13ActionPerformed
        System.exit(0);
    }//GEN-LAST:event_jMenuItem13ActionPerformed

    private void jMenuItem14ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem14ActionPerformed

        close();
    }//GEN-LAST:event_jMenuItem14ActionPerformed

    private void jMenuItem18ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem18ActionPerformed
        serialLetter.instanceOf();
    }//GEN-LAST:event_jMenuItem18ActionPerformed

    private void jMenuItem21ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem21ActionPerformed

        Log.setLogLevel(Log.LOGLEVEL_HIGH);
        Log.getLogger().setVisible(true);
    }//GEN-LAST:event_jMenuItem21ActionPerformed

private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed

    customersView panel = new customersView(this);
    addPanel("Kunde", panel);

}//GEN-LAST:event_jButton4ActionPerformed

private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
    suppliersView panel = new suppliersView(this);
    addPanel("Lieferant", panel);
}//GEN-LAST:event_jButton1ActionPerformed

private void jButton7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton7ActionPerformed

    removePanel(eurOpanel);
    eurOpanel = new eurOPanel();
    addPanel("Offene Posten (Stand: " + DateConverter.getFullDefDateString(new Date()) + ")", eurOpanel);

}//GEN-LAST:event_jButton7ActionPerformed

private void jButton9ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton9ActionPerformed
    productsView panel = new productsView(this);
    addPanel("Produkt", panel);

}//GEN-LAST:event_jButton9ActionPerformed

private void jButton11ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton11ActionPerformed
    addBillPanel(null);
}//GEN-LAST:event_jButton11ActionPerformed

private void jButton8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton8ActionPerformed


    removePanel(eurpanel);
    eurpanel = new eurEURPanel();
    addPanel("EUR", eurpanel);
}//GEN-LAST:event_jButton8ActionPerformed

private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed

    removePanel(eurEpanel);
    eurEpanel = new eurEPanel();
    addPanel("Einnahmen (Stand: " + DateConverter.getFullDefDateString(new Date()) + ")", eurEpanel);
}//GEN-LAST:event_jButton5ActionPerformed

private void jButton6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton6ActionPerformed

    removePanel(eurApanel);
    eurApanel = new eurAPanel();
    addPanel("Ausgaben (Stand: " + DateConverter.getFullDefDateString(new Date()) + ")", eurApanel);

}//GEN-LAST:event_jButton6ActionPerformed

private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
    addAngebotPanel(null);
}//GEN-LAST:event_jButton2ActionPerformed

private void logoutButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_logoutButtonActionPerformed

    this.setEnabled(false);
    new login(this);
}//GEN-LAST:event_logoutButtonActionPerformed

private void closeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeButtonActionPerformed

    close();
}//GEN-LAST:event_closeButtonActionPerformed

private void jMenuItem1ActionPerformed1(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem1ActionPerformed1

    boolean useauth = Programmdaten.instanceOf().getUSE_AUTHENTIFICATION();

    if (!useauth || getUser().isIsAdmin()) {
        if (!useauth) {
            if (Popup.Y_N_dialog("Wenn Sie die Benutzerauthentifizierung einschalten,\nmuessen Sie sich sofort anmelden.\nWollen Sie dies wirklich,\nbzw. kennen Sie das default Passwort (im Handbuch)?")) {
                Programmdaten.instanceOf().setUSE_AUTHENTIFICATION(!useauth);
                this.setMessage("Benutzerauthentifizierung eingeschaltet");
                new HistoryItem("Benutzerauthentifizierung", "Eingeschaltet", mainframe.getUser());
                Programmdaten.instanceOf().setBILLPANEL_MASK("MEDICAL");
                jMenuItem1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/bilder/small/ok.png")));
                this.setEnabled(false);
                new login(this);
            }
        } else {
            Programmdaten.instanceOf().setUSE_AUTHENTIFICATION(!useauth);
            this.setMessage("Benutzerauthentifizierung ausgeschaltet");
            new HistoryItem("Benutzerauthentifizierung", "Ausgeschaltet", mainframe.getUser());
            jMenuItem1.setIcon(null);

        }
    } else {
        Popup.notice("Die Benutzerauthentifizierung kann nur von einem Administrator ausgeschaltet werden!");
    }


}//GEN-LAST:event_jMenuItem1ActionPerformed1

private void jMenuItem5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem5ActionPerformed

    boolean useauth = Programmdaten.instanceOf().getUSE_AUTHENTIFICATION();
    if (!useauth || getUser().isIsAdmin()) {

        new Verwaltung(this);
    } else {
        Popup.notice("Die Benutzerverwaltung kann nur von einem Administrator geöffnet werden!");
    }
}//GEN-LAST:event_jMenuItem5ActionPerformed

private void jButton10ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton10ActionPerformed

    servicesView panel = new servicesView(this);
    addPanel("Dienstleistung", panel);
}//GEN-LAST:event_jButton10ActionPerformed

private void jMenuItem7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem7ActionPerformed
    boolean useauth = Programmdaten.instanceOf().getUSE_AUTHENTIFICATION();

    if (!useauth || getUser().isIsAdmin()) {
        new TaxRatesEditor(this);
    } else {
        Popup.notice("Die Steuersatzverwaltung kann nur von einem Administrator geöffnet werden!");
    }
}//GEN-LAST:event_jMenuItem7ActionPerformed

private void jButton18ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton18ActionPerformed

    manufacturerView panel = new manufacturerView(this);
    addPanel("Hersteller", panel);

}//GEN-LAST:event_jButton18ActionPerformed

private void jButton12ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton12ActionPerformed


    removePanel(verlaufpanel);
    verlaufpanel = new historyView(this);
    addPanel("Verlauf", verlaufpanel);

}//GEN-LAST:event_jButton12ActionPerformed

private void jButton13ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton13ActionPerformed

    removePanel(backuppanel);
    backuppanel = new backupView(this);
    addPanel("Datensicherung (lokal)", backuppanel);
}//GEN-LAST:event_jButton13ActionPerformed

private void jButton14ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton14ActionPerformed

    removePanel(settingspanel);
    settingspanel = new settingsView(this);
    addPanel("Einstellungen", settingspanel);
}//GEN-LAST:event_jButton14ActionPerformed

private void jButton15ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton15ActionPerformed

    removePanel(diagrammpanel);
    diagrammpanel = new diagrammChooseView(this);
    addPanel("Auswertungen", diagrammpanel);
}//GEN-LAST:event_jButton15ActionPerformed

private void jMenuItem20ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem20ActionPerformed

    pluginMenu.removeAll();
    pluginMenu.add(jMenuItem20);
    new pluginHandler(this);
}//GEN-LAST:event_jMenuItem20ActionPerformed

private void jMenuItem22ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem22ActionPerformed
    Programmdaten.instanceOf().setBILLPANEL_MASK("Default");
    Programmdaten.instanceOf().setSERVICEPANEL_MASK("Default");
    this.setMessage("Ansicht: 'Normal' gewählt.");
    jMenuItem22.setIcon(new javax.swing.ImageIcon(getClass().getResource("/bilder/small/ok.png")));
    jMenuItem23.setIcon(null);
}//GEN-LAST:event_jMenuItem22ActionPerformed

private void jMenuItem23ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem23ActionPerformed
    Programmdaten.instanceOf().setBILLPANEL_MASK("MEDICAL");
    Programmdaten.instanceOf().setSERVICEPANEL_MASK("MEDICAL");
    this.setMessage("Ansicht: 'Medizinisch' gewählt.");
    jMenuItem23.setIcon(new javax.swing.ImageIcon(getClass().getResource("/bilder/small/ok.png")));
    jMenuItem22.setIcon(null);
}//GEN-LAST:event_jMenuItem23ActionPerformed

private void jButton16ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton16ActionPerformed
    Rechner.instanceOf().setVisible(rootPaneCheckingEnabled);
}//GEN-LAST:event_jButton16ActionPerformed

private void jMenuItem24ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem24ActionPerformed

    try {
        dialog = new DialogForFile(DialogForFile.FILES_ONLY, "Kontaktliste.csv");
        TextDatFile file = new TextDatFile(ArrayUtils.ObjectToStringArray(new Kontaktliste(new Kunde().getClass()).getData()), Kontaktliste.header);
        new Job(file, dialog, mainProgressBar).execute();
    } catch (Exception e) {
        Popup.notice("Keine Kunden vorhanden.");
    }
}//GEN-LAST:event_jMenuItem24ActionPerformed

private void jMenuItem9ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem9ActionPerformed

    try {
        TableHtmlWriter writ = new TableHtmlWriter(new Kunde().getPrintModel());
        writ.setHeader(Strings.TABLE_Kunde_PRINT_HEADER.split(","));
        writ.createHtml(1, Color.LIGHT_GRAY);

        DialogForFile dialogd = new DialogForFile(DialogForFile.FILES_ONLY, "Kundenliste.html");
        dialogd.saveFile(writ.getFile());
        new Browser(dialogd.getFile());
    } catch (Exception ex) {
        Log.Debug(this, ex);
    }
}//GEN-LAST:event_jMenuItem9ActionPerformed

private void jMenuItem12ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem12ActionPerformed

    try {
        TableHtmlWriter writ = new TableHtmlWriter(new Product().getPrintModel());
        writ.setHeader(Strings.TABLE_PRODUCTS_LIST_PRINT_HEADER.split(","));
        writ.createHtml(1, Color.LIGHT_GRAY);

        DialogForFile dialogd = new DialogForFile(DialogForFile.FILES_ONLY, "Produktliste.html");
        dialogd.saveFile(writ.getFile());
        new Browser(dialogd.getFile());
    } catch (Exception ex) {
        Log.Debug(this, ex);
    }
}//GEN-LAST:event_jMenuItem12ActionPerformed

private void jMenuItem19ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem19ActionPerformed

    try {
        TableHtmlWriter writ = new TableHtmlWriter(new Lieferant().getPrintModel());
        writ.setHeader(Strings.TABLE_SUPPLIER_PRINT_HEADER.split(","));
        writ.createHtml(1, Color.LIGHT_GRAY);

        DialogForFile dialogd = new DialogForFile(DialogForFile.FILES_ONLY, "Lieferantenliste.html");
        dialogd.saveFile(writ.getFile());
        new Browser(dialogd.getFile());
    } catch (Exception ex) {
        Log.Debug(this, ex);
    }
}//GEN-LAST:event_jMenuItem19ActionPerformed

private void jMenuItem16ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem16ActionPerformed

    try {
        Lockable data = ((CommonPanel) mainTabPane.getSelectedComponent()).getLockable();
        if (data != null) {
            try {
                try {
                    clipBoard.add((People) data);
                    setInfoText("Datensatz in die Zwischenablage kopiert: " + ((People) data).getNummer());
                } catch (ClassCastException e) {
                    clipBoard.add((Product) data);
                    setInfoText("Datensatz in die Zwischenablage kopiert. " + ((Things) data).getNummer());
                }
            } catch (Exception e) {
                Popup.notice("Kopieren von hier nicht moeglich.");
//                Log.Debug(this, e.getMessage());
            }
        } else {
            setErrorText("Keine Daten zum Kopieren vorhanden.");
        }
    } catch (Exception e) {
    }
}//GEN-LAST:event_jMenuItem16ActionPerformed

private void jMenuItem4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem4ActionPerformed
    jButton14ActionPerformed(evt);
}//GEN-LAST:event_jMenuItem4ActionPerformed

private void jMenuItem17ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem17ActionPerformed

    csvKontaktImporter.instanceOf();
}//GEN-LAST:event_jMenuItem17ActionPerformed

private void jMenuItem15ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem15ActionPerformed

    new AboutWindow();
}//GEN-LAST:event_jMenuItem15ActionPerformed

private void jMenuItem25ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem25ActionPerformed

    if (getUser().isIsAdmin()) {
        new DatabaseToolWindow();
    } else {
        Popup.notice("Das Datenbankwerkzeug kann nur von einem Administrator gestarted werden!");
    }
}//GEN-LAST:event_jMenuItem25ActionPerformed

private void jMenuItem26ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem26ActionPerformed

    
    try {
        TableHtmlWriter writ = new TableHtmlWriter(new Hersteller().getPrintModel());
        writ.setHeader(Strings.TABLE_MANUFACTURER_PRINT_HEADER.split(","));
        writ.createHtml(1, Color.LIGHT_GRAY);

        DialogForFile dialogd = new DialogForFile(DialogForFile.FILES_ONLY, "Herstellerliste.html");
        dialogd.saveFile(writ.getFile());
        new Browser(dialogd.getFile());
    } catch (Exception ex) {
        Log.Debug(this, ex);
    }
}//GEN-LAST:event_jMenuItem26ActionPerformed

private void jMenuItem27ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem27ActionPerformed
  try {
        dialog = new DialogForFile(DialogForFile.FILES_ONLY, "Lieferantenliste.csv");
        TextDatFile file = new TextDatFile(ArrayUtils.ObjectToStringArray(new Kontaktliste(new Lieferant().getClass()).getData()), Kontaktliste.header);
        new Job(file, dialog, mainProgressBar).execute();
    } catch (Exception e) {
        Popup.notice("Keine Lieferanten vorhanden.");
    }
}//GEN-LAST:event_jMenuItem27ActionPerformed

private void jMenuItem28ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem28ActionPerformed
  try {
        dialog = new DialogForFile(DialogForFile.FILES_ONLY, "Herstellerliste.csv");
        TextDatFile file = new TextDatFile(ArrayUtils.ObjectToStringArray(new Kontaktliste(new Hersteller().getClass()).getData()), Kontaktliste.header);
        new Job(file, dialog, mainProgressBar).execute();
    } catch (Exception e) {
        Popup.notice("Keine Hersteller vorhanden.");
    }
}//GEN-LAST:event_jMenuItem28ActionPerformed

private void jMenuItem29ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem29ActionPerformed

    try {
        dialog = new DialogForFile(DialogForFile.FILES_ONLY, "Produktliste.csv");
        TextDatFile file = new TextDatFile(ArrayUtils.ObjectToStringArray(new Produktliste(new Product().getClass()).getData()), Produktliste.header);
        new Job(file, dialog, mainProgressBar).execute();
    } catch (Exception e) {
        Popup.notice("Keine Produkte vorhanden.");
    }
}//GEN-LAST:event_jMenuItem29ActionPerformed

private void jMenuItem30ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem30ActionPerformed

    mainTabPane.removeAll();
}//GEN-LAST:event_jMenuItem30ActionPerformed

    @Override
    public void finalize() {
        Conn.shutdown();
        System.gc();
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    public javax.swing.JPanel bottomPanel;
    public javax.swing.JMenu clipboard;
    public javax.swing.JButton closeButton;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton10;
    private javax.swing.JButton jButton11;
    private javax.swing.JButton jButton12;
    private javax.swing.JButton jButton13;
    private javax.swing.JButton jButton14;
    private javax.swing.JButton jButton15;
    private javax.swing.JButton jButton16;
    private javax.swing.JButton jButton18;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton7;
    private javax.swing.JButton jButton8;
    private javax.swing.JButton jButton9;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu10;
    private javax.swing.JMenu jMenu11;
    private javax.swing.JMenu jMenu12;
    private javax.swing.JMenu jMenu13;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenu jMenu3;
    private javax.swing.JMenu jMenu4;
    private javax.swing.JMenu jMenu5;
    private javax.swing.JMenu jMenu6;
    private javax.swing.JMenu jMenu7;
    private javax.swing.JMenu jMenu8;
    private javax.swing.JMenu jMenu9;
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
    private javax.swing.JMenuItem jMenuItem20;
    private javax.swing.JMenuItem jMenuItem21;
    private javax.swing.JMenuItem jMenuItem22;
    private javax.swing.JMenuItem jMenuItem23;
    private javax.swing.JMenuItem jMenuItem24;
    private javax.swing.JMenuItem jMenuItem25;
    private javax.swing.JMenuItem jMenuItem26;
    private javax.swing.JMenuItem jMenuItem27;
    private javax.swing.JMenuItem jMenuItem28;
    private javax.swing.JMenuItem jMenuItem29;
    private javax.swing.JMenuItem jMenuItem3;
    private javax.swing.JMenuItem jMenuItem30;
    private javax.swing.JMenuItem jMenuItem4;
    private javax.swing.JMenuItem jMenuItem5;
    private javax.swing.JMenuItem jMenuItem6;
    private javax.swing.JMenuItem jMenuItem7;
    private javax.swing.JMenuItem jMenuItem8;
    private javax.swing.JMenuItem jMenuItem9;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JSeparator jSeparator4;
    private javax.swing.JSeparator jSeparator5;
    public javax.swing.JPanel leftBar;
    public javax.swing.JButton logoutButton;
    public javax.swing.JProgressBar mainProgressBar;
    public javax.swing.JScrollPane mainScrollPane;
    public javax.swing.JTabbedPane mainTabPane;
    public javax.swing.JMenuBar menuBar;
    public javax.swing.JLabel messagePanel;
    public com.l2fprod.common.swing.JOutlookBar outlookBar;
    private javax.swing.JMenu pluginMenu;
    // End of variables declaration//GEN-END:variables
    public javax.swing.JPanel getJPanel1() {
        return bottomPanel;
    }

    public javax.swing.JPanel getJPanel10() {
        return leftBar;
    }

    public javax.swing.JScrollPane getJScrollPane1() {
        return mainScrollPane;
    }

    public javax.swing.JTabbedPane getTabPane() {
        return mainTabPane;
    }

    public javax.swing.JProgressBar getMainProgress() {
        return mainProgressBar;
    }

    /**
     * 
     * @param message
     */
    public void setMessage(String message) {
        setInfoText(message);
    }

    public javax.swing.JLabel getNachricht() {
        return messagePanel;
    }

    @Override
    public void dispose() {
        Programmdaten.instanceOf().setMAINFRAME_WINDOW_STATE(this.getSize());
        Programmdaten.instanceOf().setMAINFRAME_TAB(mainTabPane.getSelectedIndex());

        Conn.shutdown();
        Log.getLogger().flush();

        super.dispose();
    }

    private void restoreSession() {
        try {
            ArrayList<LockableContainer> sessiondata = Programmdaten.instanceOf().getSESSIONDATA();

            for (int ifh = 0; ifh < sessiondata.size(); ifh++) {
                Log.Debug(this, "Restoring session: " + sessiondata.get(ifh).getClazz() + " | " + sessiondata.get(ifh).getID());
                addPanel(sessiondata.get(ifh));
            }
        } catch (Exception e) {
            Log.Debug(this, "RestoreSession: " + e.getMessage());
        }
    }

    public void addPanel(LockableContainer data) {

        if (data.getClazz().isInstance(new Kunde())) {
            customersView panel = new customersView(this);
            addPanel("Kunde", panel);

            panel.setContact(new Kunde(data.getID()));


        } else if (data.getClazz().isInstance(new Rechnung())) {
            billsView panel = new billsView(this);
            addPanel("Rechnung", panel);
            panel.setBill(new Rechnung(data.getID()));


        } else if (data.getClazz().isInstance(new Angebot())) {
            offersView panel = new offersView(this);
            addPanel("Angebot", panel);
            panel.setAngebot(new Angebot(data.getID()));


        } else if (data.getClazz().isInstance(new Lieferant())) {
            suppliersView panel = new suppliersView(this);
            addPanel("Lieferant", panel);
            panel.setContact(new Lieferant(data.getID()));


        } else if (data.getClazz().isInstance(new Hersteller())) {
            manufacturerView panel = new manufacturerView(this);
            addPanel("Hersteller", panel);
            panel.setContact(new Hersteller(data.getID()));

        } else if (data.getClazz().isInstance(new Product())) {
            productsView panel = new productsView(this);
            addPanel("Produkt", panel);
            panel.setProduct(new Product(data.getID()));

        } else {
            Log.Debug(this,"RestoreSession: " +  "Nothing to restore.");
        }
    }

    private void saveSession() {
        try {
            Programmdaten.instanceOf().setSESSIONDATA(new LockableContainer[]{((CommonPanel) mainTabPane.getSelectedComponent()).getLockable().getLockableContainer()});
        } catch (Exception e) {
            Log.Debug("Could not save session. No saveable data selected.");
        }
    }

    private void setUser(User usern) {
        setTitle(getTitle() + " (Angemeldet als: " + usern + ")");
        currentUser = usern;
    }
}
