/*
 * MPView.java
 */
package mpv5.ui.frames;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import mpv5.Main;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.db.common.NodataFoundException;
import mpv5.db.common.QueryHandler;
import mpv5.globals.Messages;
import mpv5.db.objects.Favourite;
import mpv5.db.objects.Item;
import mpv5.logging.Log;
import mpv5.pluginhandling.MP5Plugin;
import mpv5.pluginhandling.Plugin;
import mpv5.ui.dialogs.DialogForFile;

import mpv5.ui.dialogs.Popup;
import mpv5.ui.dialogs.Search;
import mpv5.ui.menus.ClipboardMenuItem;
import mpv5.ui.menus.FavouritesMenuItem;
import mpv5.ui.panels.ContactPanel;
import mpv5.ui.panels.ContactsList;
import mpv5.ui.panels.DataPanel;
import mpv5.ui.panels.HistoryPanel;
import mpv5.ui.panels.MPControlPanel;
import mpv5.ui.misc.CloseableTabbedPane;
import mpv5.ui.misc.FadeOnChangeLabel;
import mpv5.ui.misc.Position;

import mpv5.ui.panels.calendar.JCalendar;
import mpv5.usermanagement.MPSecurityManager;
import mpv5.db.objects.User;
import mpv5.globals.LocalSettings;
import mpv5.pluginhandling.MPPLuginLoader;
import mpv5.server.MPServer;
import mpv5.ui.dialogs.Wizard;
import mpv5.ui.dialogs.subcomponents.wizard_XMLImport_1;
import mpv5.ui.dialogs.subcomponents.wizard_XMLImport_2;
import mpv5.ui.panels.ItemPanel;
import mpv5.ui.dialogs.subcomponents.ControlPanel_Advanced;
import mpv5.ui.panels.TrashPanel;
import mpv5.utils.files.TextDatFile;
import mpv5.utils.images.MPIcon;
import mpv5.utils.print.PrintJob;
import mpv5.utils.renderer.ComboBoxRendererForTooltip;
import mpv5.utils.xml.XMLWriter;
import org.jdesktop.application.SingleFrameApplication;
import org.jdesktop.application.FrameView;

/**
 * The application's main frame.
 */
public class MPView extends FrameView {

    public static MPView identifierView;
    public static Dimension initialSize = new Dimension(1100, 900);
    public static JFrame identifierFrame;
    public static CloseableTabbedPane tabPane;
    public static JLabel messagelabel = new FadeOnChangeLabel();
    public static JComboBox history = new JComboBox();
    public static User currentUser;
    public static JProgressBar progressbar = new JProgressBar();
    private static JMenu favMenu;
    private static String predefTitle;
    public static DialogForFile filedialog;
    public static SingleFrameApplication identifierApplication;
    private static boolean navBarAnimated = true;
    private static boolean tabPaneScrolled = false;

    /**
     * Display a message at the bottom of the MP frame
     * @param message
     */
    public static void addMessage(Messages message) {
        addMessage(message.getValue());
    }

    /**
     * Display a message at the bottom of the MP frame
     * @param message
     */
    public static synchronized void addMessage(final String message) {
        Runnable runnable = new Runnable() {

            public void run() {
                messagelabel.setText(message);
                history.addItem(message);
                history.setSelectedItem(message);
            }
        };
        SwingUtilities.invokeLater(runnable);
    }

    /**
     * Returns the currently selected tab on the main tab pane
     * @return
     */
    public static Object getShowingTab() {
        return tabPane.getSelectedComponent();
    }

  

    /**
     * Change the navigation bar behavior
     * @param animated
     */
    public static void setNavBarAnimated(boolean animated) {
        navBarAnimated = animated;
    }

    /**
     * Change the main tabPane behavior
     * @param scroll
     */
    public static void setTabPaneScrolled(boolean scroll) {
        tabPaneScrolled = scroll;
    }

    /**
     * Shows a file save dialog with the selcted file f.
     * If the file's parent directory is not the current directory,
     * changes the current directory to be the file's parent directory.
     * @param f
     */
    public static void showFilesaveDialogFor(File f) {
        filedialog.setSelectedFile(new File(f.getName()));
        filedialog.saveFile(f);
    }

    /**
     * Initialize and show the secondary JFrame.
     * This method is intended for showing "secondary" windows,
     * like message dialogs, about boxes, and so on.
     *
     * Unlike the mainFrame, dismissing a secondary window will not exit the application.
     * Session state is only automatically saved if the specified JFrame has a name,
     * and then only for component descendants that are named.
     * Throws an IllegalArgumentException if c is null
     * @param c
     */
    public static void show(JFrame c) {
        identifierApplication.show(c);
    }
    /**
     * The handler for all plugins
     */
    public static MPPLuginLoader pluginLoader;

    /**
     * Reloads fav menu
     */
    public void refreshFavouritesMenu() {
        if (favMenu != null) {
            favMenu.removeAll();
            favouritesMenu.add(jMenuItem5);
            fillFavouritesmenu();
        }
    }

    /**
     * Sets the max value for the progressbar
     * @param max
     */
    public synchronized static void setProgressMaximumValue(int max) {
        progressbar.setMaximum(max);
    }

    /**
     *  Sets the current value for the progressbar
     * @param val
     */
    public synchronized static void setProgressValue(int val) {
        progressbar.setValue(val);
    }

    /**
     * Reset the progress bar
     */
    public synchronized static void setProgressReset() {
        progressbar.setValue(0);
        progressbar.setIndeterminate(false);
    }

    /**
     * Sets the indeterminate property of the progress bar.
     * @param b
     */
    public synchronized static void setProgressRunning(boolean b) {
        progressbar.setIndeterminate(b);
    }

    /**
     *
     * @return The currently logged in user
     */
    public static User getUser() {
        if (currentUser == null) {
            Log.Debug(MPView.class, "There is no user logged in here, using default user.");
            try {
                currentUser = User.DEFAULT;
                return currentUser;
            } catch (Exception ex) {
                Log.Debug(MPView.class, "Default user is missing.");
                return new User();
            }
        } else {
            return currentUser;
        }
    }

    /**
     * Set the current logged in user
     * @param usern
     */
    public static void setUser(User usern) {
        currentUser = usern;
        predefTitle = (" (" + usern.getName() + ")");
    }

    /**
     * Sets the curser to waiting state if true
     * @param truee
     */
    public static void setWaiting(boolean truee) {
        if (truee) {
            identifierFrame.setCursor(new Cursor(Cursor.WAIT_CURSOR));
        } else {
            identifierFrame.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        }
    }

    /**
     * Returns the curently selected tab
     * @return
     */
    public DataPanel getCurrentTab() {
        if (tabPane.getSelectedComponent() instanceof DataPanel) {
            return (DataPanel) tabPane.getSelectedComponent();
        } else if (tabPane.getSelectedComponent() instanceof JScrollPane) {
            return (DataPanel) ((JScrollPane) tabPane.getSelectedComponent()).getComponent(0);
        } else {
            return null;
        }
    }

    private static void fillFavouritesmenu() {
        Favourite[] favs = Favourite.getUserFavourites();
        for (int i = 0; i < favs.length; i++) {
            Favourite fav = favs[i];
            try {
                favMenu.add(new FavouritesMenuItem(Favourite.getObject(fav.getFavContext(), fav.__getItemsids())));
            } catch (NodataFoundException ex) {
                Log.Debug(ex);
            }
        }
    }

    /**
     * Add something to the clipboard menu
     * @param obj
     */
    public void addToClipBoard(DatabaseObject obj) {
        clipboardMenu.add(new ClipboardMenuItem(obj));
    }

    public MPView(SingleFrameApplication app) {
        super(app);

        MPView.identifierApplication = app;

        initComponents();

        tabPane = new CloseableTabbedPane(this);
        identifierFrame = this.getFrame();
        Popup.identifier = identifierFrame;
        progressbar = this.progressBar;
        progressbar.setMinimum(0);
        messagelabel = statusMessageLabel;
        history = xhistory;
        history.setRenderer(new ComboBoxRendererForTooltip());
        if (tabPaneScrolled) {
            tabpanePanel.add(new JScrollPane(tabPane), BorderLayout.CENTER);
            jMenuItem25.setIcon(MPIcon.ICON_ENABLED);
        } else {
            tabpanePanel.add(tabPane, BorderLayout.CENTER);
        }
        favMenu = favouritesMenu;
        identifierView = this;
        filedialog = new DialogForFile(DialogForFile.FILES_ONLY);

        jOutlookBar1.setAnimated(navBarAnimated);
        getFrame().addComponentListener(new ComponentAdapter() {

            @Override
            public void componentResized(ComponentEvent event) {
                setNaviPanelSize();
            }
        });

        if (predefTitle != null) {
            identifierFrame.setTitle(identifierFrame.getTitle() + predefTitle);
        }

        fillFavouritesmenu();
        QueryHandler.setWaitCursorFor(identifierFrame);

       pluginLoader = new MPPLuginLoader();
    }

    /**
     * Set the state of the main toolbar
     * @param enable
     */
    public void enableToolBar(boolean enable) {
        mainToolbar.setEnabled(enable);
    }

    private void setNaviPanelSize() {
        jOutlookBar1.setPreferredSize(new Dimension(jOutlookBar1.getWidth(), naviPanel.getHeight() - 20));
        jOutlookBar1.setMaximumSize(new Dimension(jOutlookBar1.getWidth(), naviPanel.getHeight() - 20));
        jOutlookBar1.setMinimumSize(new Dimension(jOutlookBar1.getWidth(), naviPanel.getHeight() - 20));
        jOutlookBar1.setSize(new Dimension(jOutlookBar1.getWidth(), naviPanel.getHeight() - 20));
        naviPanel.revalidate();
        naviPanel.repaint();
    }

    /**
     * Add a tab to the main tab pane, automatically determines the needed View
     * @param item
     */
    public void addTab(DatabaseObject item) {
        if (item.getView() != null) {
            addTab(item.getView(), item.__getCName());
        }
    }

    /**
     * Shows a tab or adds it if needed
     * @param instanceOf
     * @param label
     */
    public void addOrShowTab(JComponent instanceOf, String label) {
        Component[] tabs = tabPane.getComponents();
        boolean found = false;
        JScrollPane scroll = null;
        for (int i = 0; i < tabs.length; i++) {
            JComponent component = (JComponent) tabs[i];
            if (component instanceof JScrollPane) {
                scroll = (JScrollPane) component;
                component = (JComponent) ((JScrollPane) component).getComponent(0);
            }
            if (component.getComponent(0).equals(instanceOf)) {
                if (scroll == null) {
                    tabPane.setSelectedComponent(instanceOf);
                } else {
                    tabPane.setSelectedComponent(scroll);
                }
                found = true;
            }
        }

        if (!found) {
            addTab(instanceOf, label);
        }
    }

    /**
     * Add a tab to the main tab pane, with new JScrollPane
     * @param tab
     * @param name
     */
    public void addTab(JComponent tab, String name) {
        JScrollPane spane = new JScrollPane(tab);
        tabPane.addTab(name, spane);
        tabPane.setSelectedComponent(spane);
    }

    private void addTab(JComponent tab, Messages name) {
        addTab(tab, name.toString());
    }

    private void importXML() {
        Wizard w = new Wizard(false);
        w.addPanel(new wizard_XMLImport_1(w));
        w.addPanel(new wizard_XMLImport_2(w));
        w.showWiz();


//        DialogForFile d = new DialogForFile(DialogForFile.FILES_ONLY);
//        d.setFileFilter(DialogForFile.XML_FILES);
//        XMLReader x;
//        ArrayList<ArrayList<DatabaseObject>> objs = null;
//
//        if (d.chooseFile()) {
//            x = new XMLReader();
//            try {
//                x.newDoc(d.getFile(), true);
//                objs = x.getObjects();
//            } catch (Exception ex) {
//                addMessage(ex.getMessage());
//                Log.Debug(ex);
//            }
//        }
//
//
//        if (objs != null && objs.size() < 0) {
//            for (int i = 0; i < objs.size(); i++) {
//                ArrayList<DatabaseObject> arrayList = objs.get(i);
//                for (int j = 0; j < arrayList.size(); j++) {
//                    DatabaseObject databaseObject = arrayList.get(j);
//                    Log.Debug(this, "Parsing " + databaseObject.getDbIdentity() + " : " + databaseObject.__getCName() + " from file: " + d.getFile());
//                    databaseObject.save();
//                }
//            }
//        } else {
//            addMessage(Messages.NO_DATA_FOUND);
//        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mainPanel = new javax.swing.JPanel();
        naviPanel = new javax.swing.JPanel();
        jOutlookBar1 = new com.l2fprod.common.swing.JOutlookBar();
        jPanel2 = new javax.swing.JPanel();
        jButton5 = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton18 = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jButton8 = new javax.swing.JButton();
        jButton9 = new javax.swing.JButton();
        jPanel4 = new javax.swing.JPanel();
        jPanel6 = new javax.swing.JPanel();
        jButton6 = new javax.swing.JButton();
        jButton7 = new javax.swing.JButton();
        menuBar = new javax.swing.JMenuBar();
        javax.swing.JMenu fileMenu = new javax.swing.JMenu();
        jMenuItem13 = new javax.swing.JMenuItem();
        jMenuItem14 = new javax.swing.JMenuItem();
        jSeparator3 = new javax.swing.JSeparator();
        jMenuItem6 = new javax.swing.JMenuItem();
        jMenu4 = new javax.swing.JMenu();
        jMenu5 = new javax.swing.JMenu();
        jMenuItem8 = new javax.swing.JMenuItem();
        jMenu10 = new javax.swing.JMenu();
        jMenuItem20 = new javax.swing.JMenuItem();
        jMenuItem21 = new javax.swing.JMenuItem();
        jMenu11 = new javax.swing.JMenu();
        jMenuItem26 = new javax.swing.JMenuItem();
        jMenu8 = new javax.swing.JMenu();
        jMenuItem11 = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JSeparator();
        jMenuItem12 = new javax.swing.JMenuItem();
        jMenu9 = new javax.swing.JMenu();
        jMenuItem15 = new javax.swing.JMenuItem();
        jSeparator4 = new javax.swing.JSeparator();
        jMenuItem9 = new javax.swing.JMenuItem();
        jMenuItem16 = new javax.swing.JMenuItem();
        jSeparator5 = new javax.swing.JSeparator();
        jMenuItem17 = new javax.swing.JMenuItem();
        jMenu1 = new javax.swing.JMenu();
        jMenu2 = new javax.swing.JMenu();
        jMenuItem1 = new javax.swing.JMenuItem();
        jMenuItem2 = new javax.swing.JMenuItem();
        jMenuItem25 = new javax.swing.JMenuItem();
        jMenuItem19 = new javax.swing.JMenuItem();
        jMenuItem4 = new javax.swing.JMenuItem();
        jMenu3 = new javax.swing.JMenu();
        jMenuItem3 = new javax.swing.JMenuItem();
        jMenuItem18 = new javax.swing.JMenuItem();
        jMenuItem23 = new javax.swing.JMenuItem();
        jMenuItem24 = new javax.swing.JMenuItem();
        jSeparator6 = new javax.swing.JSeparator();
        jMenuItem22 = new javax.swing.JMenuItem();
        favouritesMenu = new javax.swing.JMenu();
        jMenuItem5 = new javax.swing.JMenuItem();
        clipboardMenu = new javax.swing.JMenu();
        jMenu6 = new javax.swing.JMenu();
        jMenuItem7 = new javax.swing.JMenuItem();
        jMenu7 = new javax.swing.JMenu();
        jMenuItem10 = new javax.swing.JMenuItem();
        statusPanel = new javax.swing.JPanel();
        statusMessageLabel = new FadeOnChangeLabel();
        pluginIcons = new javax.swing.JPanel();
        separator = new javax.swing.JPanel();
        progressBar = new javax.swing.JProgressBar();
        separator1 = new javax.swing.JPanel();
        xhistory = new javax.swing.JComboBox();
        serverlabel = new javax.swing.JPanel();
        mainToolbar = new javax.swing.JToolBar();
        jButton24 = new javax.swing.JButton();
        jButton26 = new javax.swing.JButton();
        jSeparator2 = new javax.swing.JToolBar.Separator();
        jButton25 = new javax.swing.JButton();

        mainPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        mainPanel.setName("mainPanel"); // NOI18N

        tabpanePanel.setBackground(new java.awt.Color(255, 255, 255));
        tabpanePanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        tabpanePanel.setName("tabpanePanel"); // NOI18N
        tabpanePanel.setLayout(new java.awt.BorderLayout());

        naviPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        naviPanel.setAutoscrolls(true);
        naviPanel.setFocusCycleRoot(true);
        naviPanel.setName("naviPanel"); // NOI18N

        jOutlookBar1.setAnimated(false);
        jOutlookBar1.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        jOutlookBar1.setName("jOutlookBar1"); // NOI18N

        jPanel2.setBackground(new java.awt.Color(153, 153, 153));
        jPanel2.setName("jPanel2"); // NOI18N
        jPanel2.setPreferredSize(new java.awt.Dimension(110, 400));

        jButton5.setFont(new java.awt.Font("Tahoma", 0, 10));
        jButton5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/mpv5/resources/images/32/agt_family.png"))); // NOI18N
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("mpv5/resources/languages/Panels"); // NOI18N
        jButton5.setText(bundle.getString("MPView.jButton5.text_1")); // NOI18N
        jButton5.setToolTipText(bundle.getString("MPView.jButton5.toolTipText_1")); // NOI18N
        jButton5.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton5.setName("jButton5"); // NOI18N
        jButton5.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton5ActionPerformed(evt);
            }
        });

        jButton1.setFont(new java.awt.Font("Tahoma", 0, 10));
        jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/mpv5/resources/images/32/edit_group.png"))); // NOI18N
        jButton1.setText(bundle.getString("MPView.jButton1.text_1")); // NOI18N
        jButton1.setToolTipText(bundle.getString("MPView.jButton1.toolTipText_1")); // NOI18N
        jButton1.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton1.setName("jButton1"); // NOI18N
        jButton1.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton2.setFont(new java.awt.Font("Tahoma", 0, 10));
        jButton2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/mpv5/resources/images/32/edit_user.png"))); // NOI18N
        jButton2.setText(bundle.getString("MPView.jButton2.text_1")); // NOI18N
        jButton2.setToolTipText(bundle.getString("MPView.jButton2.toolTipText_1")); // NOI18N
        jButton2.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton2.setName("jButton2"); // NOI18N
        jButton2.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jButton18.setFont(new java.awt.Font("Tahoma", 0, 10));
        jButton18.setIcon(new javax.swing.ImageIcon(getClass().getResource("/mpv5/resources/images/32/edit_group.png"))); // NOI18N
        jButton18.setText(bundle.getString("MPView.jButton18.text_1")); // NOI18N
        jButton18.setToolTipText(bundle.getString("MPView.jButton18.toolTipText_1")); // NOI18N
        jButton18.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton18.setName("jButton18"); // NOI18N
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
                .addComponent(jButton5, javax.swing.GroupLayout.DEFAULT_SIZE, 88, Short.MAX_VALUE)
                .addContainerGap())
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addGap(11, 11, 11)
                .addComponent(jButton1, 0, 0, Short.MAX_VALUE)
                .addContainerGap())
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jButton18, 0, 0, Short.MAX_VALUE)
                .addGap(11, 11, 11))
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 89, Short.MAX_VALUE)
                .addGap(11, 11, 11))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jButton5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButton1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButton18)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButton2)
                .addGap(131, 131, 131))
        );

        jOutlookBar1.addTab(bundle.getString("MPView.jPanel2.TabConstraints.tabTitle_1"), jPanel2); // NOI18N

        jPanel3.setBackground(new java.awt.Color(153, 153, 153));
        jPanel3.setName("jPanel3"); // NOI18N
        jPanel3.setPreferredSize(new java.awt.Dimension(110, 400));

        jButton8.setFont(new java.awt.Font("Tahoma", 0, 10));
        jButton8.setIcon(new javax.swing.ImageIcon(getClass().getResource("/mpv5/resources/images/32/binary.png"))); // NOI18N
        jButton8.setText(bundle.getString("MPView.jButton8.text")); // NOI18N
        jButton8.setToolTipText(bundle.getString("MPView.jButton8.toolTipText")); // NOI18N
        jButton8.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton8.setName("jButton8"); // NOI18N
        jButton8.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton8ActionPerformed(evt);
            }
        });

        jButton9.setFont(new java.awt.Font("Tahoma", 0, 10));
        jButton9.setIcon(new javax.swing.ImageIcon(getClass().getResource("/mpv5/resources/images/32/kspread.png"))); // NOI18N
        jButton9.setText(bundle.getString("MPView.jButton9.text")); // NOI18N
        jButton9.setToolTipText(bundle.getString("MPView.jButton9.toolTipText")); // NOI18N
        jButton9.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton9.setName("jButton9"); // NOI18N
        jButton9.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton9.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton9ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
            .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel3Layout.createSequentialGroup()
                    .addGap(11, 11, 11)
                    .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jButton8, javax.swing.GroupLayout.DEFAULT_SIZE, 79, Short.MAX_VALUE)
                        .addComponent(jButton9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGap(11, 11, 11)))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 136, Short.MAX_VALUE)
            .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel3Layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(jButton8)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(jButton9)
                    .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );

        jOutlookBar1.addTab(bundle.getString("MPView.jPanel3.TabConstraints.tabTitle_1"), jPanel3); // NOI18N

        jPanel4.setBackground(new java.awt.Color(153, 153, 153));
        jPanel4.setName("jPanel4"); // NOI18N
        jPanel4.setPreferredSize(new java.awt.Dimension(110, 400));

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        jOutlookBar1.addTab(bundle.getString("MPView.jPanel4.TabConstraints.tabTitle_1"), jPanel4); // NOI18N

        jPanel6.setBackground(new java.awt.Color(153, 153, 153));
        jPanel6.setName("jPanel6"); // NOI18N
        jPanel6.setPreferredSize(new java.awt.Dimension(110, 400));

        jButton6.setFont(new java.awt.Font("Tahoma", 0, 10));
        jButton6.setIcon(new javax.swing.ImageIcon(getClass().getResource("/mpv5/resources/images/32/kservices.png"))); // NOI18N
        jButton6.setText(bundle.getString("MPView.jButton6.text")); // NOI18N
        jButton6.setToolTipText(bundle.getString("MPView.jButton6.toolTipText")); // NOI18N
        jButton6.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton6.setName("jButton6"); // NOI18N
        jButton6.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton6ActionPerformed(evt);
            }
        });

        jButton7.setFont(new java.awt.Font("Tahoma", 0, 10));
        jButton7.setIcon(new javax.swing.ImageIcon(getClass().getResource("/mpv5/resources/images/32/edittrash.png"))); // NOI18N
        jButton7.setText(bundle.getString("MPView.jButton7.text")); // NOI18N
        jButton7.setToolTipText(bundle.getString("MPView.jButton7.toolTipText")); // NOI18N
        jButton7.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton7.setName("jButton7"); // NOI18N
        jButton7.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton7ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButton6, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButton7, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 68, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jButton6)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButton7)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jOutlookBar1.addTab(bundle.getString("MPView.jPanel6.TabConstraints.tabTitle"), jPanel6); // NOI18N

        javax.swing.GroupLayout naviPanelLayout = new javax.swing.GroupLayout(naviPanel);
        naviPanel.setLayout(naviPanelLayout);
        naviPanelLayout.setHorizontalGroup(
            naviPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jOutlookBar1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        naviPanelLayout.setVerticalGroup(
            naviPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(naviPanelLayout.createSequentialGroup()
                .addComponent(jOutlookBar1, javax.swing.GroupLayout.DEFAULT_SIZE, 499, Short.MAX_VALUE)
                .addContainerGap())
        );

        javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addComponent(naviPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tabpanePanel, javax.swing.GroupLayout.DEFAULT_SIZE, 544, Short.MAX_VALUE))
        );
        mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(naviPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(tabpanePanel, javax.swing.GroupLayout.DEFAULT_SIZE, 515, Short.MAX_VALUE)
        );

        menuBar.setName("menuBar"); // NOI18N

        fileMenu.setText(bundle.getString("MPView.fileMenu.text_1")); // NOI18N
        fileMenu.setName("fileMenu"); // NOI18N

        jMenuItem13.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem13.setIcon(new javax.swing.ImageIcon(getClass().getResource("/mpv5/resources/images/16/filesave.png"))); // NOI18N
        jMenuItem13.setText(bundle.getString("MPView.jMenuItem13.text")); // NOI18N
        jMenuItem13.setName("jMenuItem13"); // NOI18N
        jMenuItem13.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem13ActionPerformed(evt);
            }
        });
        fileMenu.add(jMenuItem13);

        jMenuItem14.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_N, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem14.setIcon(new javax.swing.ImageIcon(getClass().getResource("/mpv5/resources/images/16/filesaveas.png"))); // NOI18N
        jMenuItem14.setText(bundle.getString("MPView.jMenuItem14.text")); // NOI18N
        jMenuItem14.setName("jMenuItem14"); // NOI18N
        jMenuItem14.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem14ActionPerformed(evt);
            }
        });
        fileMenu.add(jMenuItem14);

        jSeparator3.setName("jSeparator3"); // NOI18N
        fileMenu.add(jSeparator3);

        jMenuItem6.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_P, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem6.setIcon(new javax.swing.ImageIcon(getClass().getResource("/mpv5/resources/images/22/printer1.png"))); // NOI18N
        jMenuItem6.setText(bundle.getString("MPView.jMenuItem6.text")); // NOI18N
        jMenuItem6.setName("jMenuItem6"); // NOI18N
        jMenuItem6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem6ActionPerformed(evt);
            }
        });
        fileMenu.add(jMenuItem6);

        jMenu4.setText(bundle.getString("MPView.jMenu4.text")); // NOI18N
        jMenu4.setName("jMenu4"); // NOI18N

        jMenu5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/mpv5/resources/images/16/kontact_contacts.png"))); // NOI18N
        jMenu5.setText(bundle.getString("MPView.jMenu5.text")); // NOI18N
        jMenu5.setName("jMenu5"); // NOI18N

        jMenuItem8.setIcon(new javax.swing.ImageIcon(getClass().getResource("/mpv5/resources/images/16/xml.png"))); // NOI18N
        jMenuItem8.setText(bundle.getString("MPView.jMenuItem8.text")); // NOI18N
        jMenuItem8.setName("jMenuItem8"); // NOI18N
        jMenuItem8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem8ActionPerformed(evt);
            }
        });
        jMenu5.add(jMenuItem8);

        jMenu4.add(jMenu5);

        jMenu10.setText(bundle.getString("MPView.jMenu10.text")); // NOI18N
        jMenu10.setName("jMenu10"); // NOI18N

        jMenuItem20.setText(bundle.getString("MPView.jMenuItem20.text")); // NOI18N
        jMenuItem20.setName("jMenuItem20"); // NOI18N
        jMenuItem20.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem20ActionPerformed(evt);
            }
        });
        jMenu10.add(jMenuItem20);

        jMenuItem21.setText(bundle.getString("MPView.jMenuItem21.text")); // NOI18N
        jMenuItem21.setName("jMenuItem21"); // NOI18N
        jMenuItem21.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem21ActionPerformed(evt);
            }
        });
        jMenu10.add(jMenuItem21);

        jMenu4.add(jMenu10);

        jMenu11.setText(bundle.getString("MPView.jMenu11.text")); // NOI18N
        jMenu11.setName("jMenu11"); // NOI18N

        jMenuItem26.setText(bundle.getString("MPView.jMenuItem26.text")); // NOI18N
        jMenuItem26.setName("jMenuItem26"); // NOI18N
        jMenuItem26.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem26ActionPerformed(evt);
            }
        });
        jMenu11.add(jMenuItem26);

        jMenu4.add(jMenu11);

        fileMenu.add(jMenu4);

        jMenu8.setText(bundle.getString("MPView.jMenu8.text")); // NOI18N
        jMenu8.setName("jMenu8"); // NOI18N

        jMenuItem11.setIcon(new javax.swing.ImageIcon(getClass().getResource("/mpv5/resources/images/16/xml.png"))); // NOI18N
        jMenuItem11.setText(bundle.getString("MPView.jMenuItem11.text")); // NOI18N
        jMenuItem11.setName("jMenuItem11"); // NOI18N
        jMenuItem11.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem11ActionPerformed(evt);
            }
        });
        jMenu8.add(jMenuItem11);

        fileMenu.add(jMenu8);

        jSeparator1.setName("jSeparator1"); // NOI18N
        fileMenu.add(jSeparator1);

        jMenuItem12.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_X, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem12.setIcon(new javax.swing.ImageIcon(getClass().getResource("/mpv5/resources/images/16/remove.png"))); // NOI18N
        jMenuItem12.setText(bundle.getString("MPView.jMenuItem12.text")); // NOI18N
        jMenuItem12.setName("jMenuItem12"); // NOI18N
        jMenuItem12.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem12ActionPerformed(evt);
            }
        });
        fileMenu.add(jMenuItem12);

        menuBar.add(fileMenu);

        jMenu9.setText(bundle.getString("MPView.jMenu9.text")); // NOI18N
        jMenu9.setName("jMenu9"); // NOI18N

        jMenuItem15.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_R, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem15.setText(bundle.getString("MPView.jMenuItem15.text")); // NOI18N
        jMenuItem15.setName("jMenuItem15"); // NOI18N
        jMenuItem15.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem15ActionPerformed(evt);
            }
        });
        jMenu9.add(jMenuItem15);

        jSeparator4.setName("jSeparator4"); // NOI18N
        jMenu9.add(jSeparator4);

        jMenuItem9.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_N, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem9.setIcon(new javax.swing.ImageIcon(getClass().getResource("/mpv5/resources/images/16/editcopy.png"))); // NOI18N
        jMenuItem9.setText(bundle.getString("MPView.jMenuItem9.text")); // NOI18N
        jMenuItem9.setName("jMenuItem9"); // NOI18N
        jMenuItem9.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem9ActionPerformed(evt);
            }
        });
        jMenu9.add(jMenuItem9);

        jMenuItem16.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_B, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem16.setText(bundle.getString("MPView.jMenuItem16.text")); // NOI18N
        jMenuItem16.setName("jMenuItem16"); // NOI18N
        jMenuItem16.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem16ActionPerformed(evt);
            }
        });
        jMenu9.add(jMenuItem16);

        jSeparator5.setName("jSeparator5"); // NOI18N
        jMenu9.add(jSeparator5);

        jMenuItem17.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem17.setText(bundle.getString("MPView.jMenuItem17.text")); // NOI18N
        jMenuItem17.setName("jMenuItem17"); // NOI18N
        jMenuItem17.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem17ActionPerformed(evt);
            }
        });
        jMenu9.add(jMenuItem17);

        menuBar.add(jMenu9);

        jMenu1.setText(bundle.getString("MPView.jMenu1.text_1")); // NOI18N
        jMenu1.setName("jMenu1"); // NOI18N

        jMenu2.setText(bundle.getString("MPView.jMenu2.text_1")); // NOI18N
        jMenu2.setName("jMenu2"); // NOI18N

        jMenuItem1.setText(bundle.getString("MPView.jMenuItem1.text_1")); // NOI18N
        jMenuItem1.setName("jMenuItem1"); // NOI18N
        jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem1ActionPerformed(evt);
            }
        });
        jMenu2.add(jMenuItem1);

        jMenuItem2.setText(bundle.getString("MPView.jMenuItem2.text_1")); // NOI18N
        jMenuItem2.setName("jMenuItem2"); // NOI18N
        jMenuItem2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem2ActionPerformed(evt);
            }
        });
        jMenu2.add(jMenuItem2);

        jMenu1.add(jMenu2);

        jMenuItem25.setText(bundle.getString("MPView.jMenuItem25.text")); // NOI18N
        jMenuItem25.setName("jMenuItem25"); // NOI18N
        jMenuItem25.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem25ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem25);

        jMenuItem19.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F5, 0));
        jMenuItem19.setText(bundle.getString("MPView.jMenuItem19.text")); // NOI18N
        jMenuItem19.setName("jMenuItem19"); // NOI18N
        jMenuItem19.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem19ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem19);

        jMenuItem4.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F11, 0));
        jMenuItem4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/mpv5/resources/images/16/2uparrow.png"))); // NOI18N
        jMenuItem4.setText(bundle.getString("MPView.jMenuItem4.text")); // NOI18N
        jMenuItem4.setName("jMenuItem4"); // NOI18N
        jMenuItem4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem4ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem4);

        menuBar.add(jMenu1);

        jMenu3.setText(bundle.getString("MPView.jMenu3.text_1")); // NOI18N
        jMenu3.setName("jMenu3"); // NOI18N

        jMenuItem3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/mpv5/resources/images/16/configure_shortcuts.png"))); // NOI18N
        jMenuItem3.setText(bundle.getString("MPView.jMenuItem3.text_1")); // NOI18N
        jMenuItem3.setName("jMenuItem3"); // NOI18N
        jMenuItem3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem3ActionPerformed(evt);
            }
        });
        jMenu3.add(jMenuItem3);

        jMenuItem18.setIcon(new javax.swing.ImageIcon(getClass().getResource("/mpv5/resources/images/16/kalarm.png"))); // NOI18N
        jMenuItem18.setText(bundle.getString("MPView.jMenuItem18.text")); // NOI18N
        jMenuItem18.setName("jMenuItem18"); // NOI18N
        jMenuItem18.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem18ActionPerformed(evt);
            }
        });
        jMenu3.add(jMenuItem18);

        jMenuItem23.setIcon(new javax.swing.ImageIcon(getClass().getResource("/mpv5/resources/images/16/edittrash.png"))); // NOI18N
        jMenuItem23.setText(bundle.getString("MPView.jMenuItem23.text")); // NOI18N
        jMenuItem23.setName("jMenuItem23"); // NOI18N
        jMenuItem23.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem23ActionPerformed(evt);
            }
        });
        jMenu3.add(jMenuItem23);

        jMenuItem24.setIcon(new javax.swing.ImageIcon(getClass().getResource("/mpv5/resources/images/16/kwikdisk.png"))); // NOI18N
        jMenuItem24.setText(bundle.getString("MPView.jMenuItem24.text")); // NOI18N
        jMenuItem24.setName("jMenuItem24"); // NOI18N
        jMenuItem24.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem24ActionPerformed(evt);
            }
        });
        jMenu3.add(jMenuItem24);

        jSeparator6.setName("jSeparator6"); // NOI18N
        jMenu3.add(jSeparator6);

        jMenuItem22.setText(bundle.getString("MPView.jMenuItem22.text")); // NOI18N
        jMenuItem22.setName("jMenuItem22"); // NOI18N
        jMenuItem22.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem22ActionPerformed(evt);
            }
        });
        jMenu3.add(jMenuItem22);

        menuBar.add(jMenu3);

        favouritesMenu.setText(bundle.getString("MPView.favouritesMenu.text")); // NOI18N
        favouritesMenu.setName("favouritesMenu"); // NOI18N

        jMenuItem5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/mpv5/resources/images/16/remove.png"))); // NOI18N
        jMenuItem5.setText(bundle.getString("MPView.jMenuItem5.text")); // NOI18N
        jMenuItem5.setName("jMenuItem5"); // NOI18N
        jMenuItem5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem5ActionPerformed(evt);
            }
        });
        favouritesMenu.add(jMenuItem5);

        menuBar.add(favouritesMenu);

        clipboardMenu.setText(bundle.getString("MPView.clipboardMenu.text")); // NOI18N
        clipboardMenu.setName("clipboardMenu"); // NOI18N

        jMenu6.setText(bundle.getString("MPView.jMenu6.text")); // NOI18N
        jMenu6.setName("jMenu6"); // NOI18N

        jMenuItem7.setText(bundle.getString("MPView.jMenuItem7.text")); // NOI18N
        jMenuItem7.setName("jMenuItem7"); // NOI18N
        jMenuItem7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem7ActionPerformed(evt);
            }
        });
        jMenu6.add(jMenuItem7);

        clipboardMenu.add(jMenu6);

        menuBar.add(clipboardMenu);

        jMenu7.setText(bundle.getString("MPView.jMenu7.text")); // NOI18N
        jMenu7.setName("jMenu7"); // NOI18N

        jMenuItem10.setText(bundle.getString("MPView.jMenuItem10.text")); // NOI18N
        jMenuItem10.setName("jMenuItem10"); // NOI18N
        jMenuItem10.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem10ActionPerformed(evt);
            }
        });
        jMenu7.add(jMenuItem10);

        menuBar.add(jMenu7);

        statusPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.RAISED));
        statusPanel.setName("statusPanel"); // NOI18N
        statusPanel.setPreferredSize(new java.awt.Dimension(800, 20));
        statusPanel.setLayout(new javax.swing.BoxLayout(statusPanel, javax.swing.BoxLayout.LINE_AXIS));

        statusMessageLabel.setFont(new java.awt.Font("Dialog", 0, 11));
        statusMessageLabel.setText(bundle.getString("MPView.statusMessageLabel.text")); // NOI18N
        statusMessageLabel.setMaximumSize(new java.awt.Dimension(1000, 25));
        statusMessageLabel.setMinimumSize(new java.awt.Dimension(300, 14));
        statusMessageLabel.setName("statusMessageLabel"); // NOI18N
        statusPanel.add(statusMessageLabel);

        pluginIcons.setMaximumSize(new java.awt.Dimension(250, 32767));
        pluginIcons.setMinimumSize(new java.awt.Dimension(1, 1));
        pluginIcons.setName("pluginIcons"); // NOI18N
        pluginIcons.setPreferredSize(new java.awt.Dimension(10, 15));
        pluginIcons.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 5, 0));
        statusPanel.add(pluginIcons);

        separator.setMaximumSize(new java.awt.Dimension(10, 32767));
        separator.setMinimumSize(new java.awt.Dimension(1, 1));
        separator.setName("separator"); // NOI18N
        separator.setPreferredSize(new java.awt.Dimension(10, 15));

        javax.swing.GroupLayout separatorLayout = new javax.swing.GroupLayout(separator);
        separator.setLayout(separatorLayout);
        separatorLayout.setHorizontalGroup(
            separatorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 10, Short.MAX_VALUE)
        );
        separatorLayout.setVerticalGroup(
            separatorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 16, Short.MAX_VALUE)
        );

        statusPanel.add(separator);

        progressBar.setBorder(new javax.swing.border.LineBorder(javax.swing.UIManager.getDefaults().getColor("Button.darkShadow"), 1, true));
        progressBar.setMaximumSize(new java.awt.Dimension(100, 25));
        progressBar.setName("progressBar"); // NOI18N
        progressBar.setPreferredSize(new java.awt.Dimension(100, 14));
        statusPanel.add(progressBar);

        separator1.setMaximumSize(new java.awt.Dimension(10, 32767));
        separator1.setMinimumSize(new java.awt.Dimension(1, 1));
        separator1.setName("separator1"); // NOI18N
        separator1.setPreferredSize(new java.awt.Dimension(10, 15));

        javax.swing.GroupLayout separator1Layout = new javax.swing.GroupLayout(separator1);
        separator1.setLayout(separator1Layout);
        separator1Layout.setHorizontalGroup(
            separator1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 10, Short.MAX_VALUE)
        );
        separator1Layout.setVerticalGroup(
            separator1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 16, Short.MAX_VALUE)
        );

        statusPanel.add(separator1);

        xhistory.setFont(new java.awt.Font("Dialog", 0, 11));
        xhistory.setEditor(null);
        xhistory.setFocusable(false);
        xhistory.setMaximumSize(new java.awt.Dimension(200, 32767));
        xhistory.setMinimumSize(new java.awt.Dimension(55, 14));
        xhistory.setName("xhistory"); // NOI18N
        xhistory.setPreferredSize(new java.awt.Dimension(200, 23));
        xhistory.setRequestFocusEnabled(false);
        xhistory.setVerifyInputWhenFocusTarget(false);
        statusPanel.add(xhistory);

        serverlabel.setMaximumSize(new java.awt.Dimension(20, 100));
        serverlabel.setMinimumSize(new java.awt.Dimension(20, 10));
        serverlabel.setName("serverlabel"); // NOI18N
        serverlabel.setPreferredSize(new java.awt.Dimension(20, 18));
        serverlabel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 5, 0));
        statusPanel.add(serverlabel);

        mainToolbar.setRollover(true);
        mainToolbar.setName("mainToolbar"); // NOI18N

        jButton24.setIcon(new javax.swing.ImageIcon(getClass().getResource("/mpv5/resources/images/32/endturn.png"))); // NOI18N
        jButton24.setToolTipText(bundle.getString("MPView.jButton24.toolTipText_1")); // NOI18N
        jButton24.setFocusable(false);
        jButton24.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton24.setName("jButton24"); // NOI18N
        jButton24.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton24.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton24ActionPerformed(evt);
            }
        });
        mainToolbar.add(jButton24);

        jButton26.setIcon(new javax.swing.ImageIcon(getClass().getResource("/mpv5/resources/images/32/lock.png"))); // NOI18N
        jButton26.setToolTipText(bundle.getString("MPView.jButton26.toolTipText_1")); // NOI18N
        jButton26.setFocusable(false);
        jButton26.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton26.setName("jButton26"); // NOI18N
        jButton26.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton26.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton26ActionPerformed(evt);
            }
        });
        mainToolbar.add(jButton26);

        jSeparator2.setName("jSeparator2"); // NOI18N
        mainToolbar.add(jSeparator2);

        jButton25.setIcon(new javax.swing.ImageIcon(getClass().getResource("/mpv5/resources/images/32/kcalc.png"))); // NOI18N
        jButton25.setToolTipText(bundle.getString("MPView.jButton25.toolTipText_1")); // NOI18N
        jButton25.setFocusable(false);
        jButton25.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton25.setName("jButton25"); // NOI18N
        jButton25.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton25.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton25ActionPerformed(evt);
            }
        });
        mainToolbar.add(jButton25);

        setComponent(mainPanel);
        setMenuBar(menuBar);
        setStatusBar(statusPanel);
        setToolBar(mainToolbar);
    }// </editor-fold>//GEN-END:initComponents

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
        ContactPanel tab = new ContactPanel(Context.getCustomer());
        tab.setType(ContactPanel.CUSTOMER);
        addTab(tab, Messages.NEW_CUSTOMER.toString());

    }//GEN-LAST:event_jButton5ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        ContactPanel tab = new ContactPanel(Context.getSupplier());
        tab.setType(ContactPanel.SUPPLIER);
        addTab(tab, Messages.NEW_SUPPLIER.toString());

}//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed

        ContactsList tab = new ContactsList(Context.getContact());
        addTab(tab, Messages.CONTACTS_LIST.toString());

}//GEN-LAST:event_jButton2ActionPerformed

    private void jButton18ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton18ActionPerformed
        ContactPanel tab = new ContactPanel(Context.getManufacturer());
        tab.setType(ContactPanel.MANUFACTURER);
        addTab(tab, Messages.NEW_MANUFACTURER.toString());

    }//GEN-LAST:event_jButton18ActionPerformed

    private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem1ActionPerformed
        Main.setLaF(null);
        getUser().setLaf(UIManager.getSystemLookAndFeelClassName());
        getUser().save();
    }//GEN-LAST:event_jMenuItem1ActionPerformed

    private void jMenuItem2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem2ActionPerformed
        Main.setLaF("de.muntjak.tinylookandfeel.TinyLookAndFeel");
        getUser().setLaf("de.muntjak.tinylookandfeel.TinyLookAndFeel");
        getUser().save();
    }//GEN-LAST:event_jMenuItem2ActionPerformed

    private void jMenuItem3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem3ActionPerformed
        addOrShowTab(MPControlPanel.instanceOf(), Messages.CONTROL_PANEL.toString());

    }//GEN-LAST:event_jMenuItem3ActionPerformed

    private void jButton25ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton25ActionPerformed
        MPCalculator2.instanceOf();
    }//GEN-LAST:event_jButton25ActionPerformed

    private void jButton26ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton26ActionPerformed

        mpv5.usermanagement.Lock.lock(this.getFrame());
        getUser().logout();
}//GEN-LAST:event_jButton26ActionPerformed

    private void jButton24ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton24ActionPerformed
        if (Popup.Y_N_dialog(Messages.REALLY_CLOSE)) {
            Main.getApplication().exit();
        }
}//GEN-LAST:event_jButton24ActionPerformed

    private void jMenuItem4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem4ActionPerformed
        selectedTabInNewFrame();
    }//GEN-LAST:event_jMenuItem4ActionPerformed

    private void jMenuItem5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem5ActionPerformed
        Favourite.flush(getUser());
        refreshFavouritesMenu();

    }//GEN-LAST:event_jMenuItem5ActionPerformed

    private void jMenuItem6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem6ActionPerformed
        Component pane = tabPane.getSelectedComponent();
        if (pane instanceof DataPanel) {
            try {
                new PrintJob().print((((DataPanel) pane)).getDataOwner());
            } catch (Exception e) {
                Log.Debug(this, e);
            }
        }
    }//GEN-LAST:event_jMenuItem6ActionPerformed

    private void jMenuItem7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem7ActionPerformed

        clipboardMenu.removeAll();
        clipboardMenu.add(jMenu6);

    }//GEN-LAST:event_jMenuItem7ActionPerformed

    private void jMenuItem8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem8ActionPerformed
        if (mpv5.usermanagement.MPSecurityManager.check(Context.getContact(), MPSecurityManager.EXPORT)) {
            try {
                XMLWriter xmlw = new XMLWriter();
                xmlw.newDoc(true);
                String name = Context.getContact().getDbIdentity();
                ArrayList<DatabaseObject> dbobjarr = DatabaseObject.getObjects(Context.getContact());
                xmlw.add(dbobjarr);
                showFilesaveDialogFor(xmlw.createFile(name));
            } catch (NodataFoundException ex) {
                Log.Debug(this, ex);
            }
        }
    }//GEN-LAST:event_jMenuItem8ActionPerformed

    private void jMenuItem9ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem9ActionPerformed

        clipboardMenu.add(new ClipboardMenuItem(getCurrentTab().getDataOwner()));

    }//GEN-LAST:event_jMenuItem9ActionPerformed

    private void jMenuItem10ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem10ActionPerformed

        addTab(Log.getLogger().open(), "MP Logger");
    }//GEN-LAST:event_jMenuItem10ActionPerformed

    private void jMenuItem11ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem11ActionPerformed
        importXML();
    }//GEN-LAST:event_jMenuItem11ActionPerformed

    private void jButton6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton6ActionPerformed
        addTab((JComponent) HistoryPanel.instanceOf(), Messages.HISTORY_PANEL.toString());

    }//GEN-LAST:event_jButton6ActionPerformed

    private void jMenuItem12ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem12ActionPerformed
        identifierApplication.exit(evt);
    }//GEN-LAST:event_jMenuItem12ActionPerformed

    private void jMenuItem13ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem13ActionPerformed

        Component pane = tabPane.getSelectedComponent();
        if (pane instanceof DataPanel) {
            try {

                DatabaseObject dato = ((DataPanel) pane).getDataOwner();

                dato.getPanelData(((DataPanel) pane));
                if (dato.save()) {
                    ((DataPanel) pane).refresh();
                    ((DataPanel) pane).setDataOwner(dato,true);
                } else {
                    ((DataPanel) pane).showRequiredFields();
                }
            } catch (Exception e) {
                Log.Debug(this, e);
            }
        }
    }//GEN-LAST:event_jMenuItem13ActionPerformed

    private void jMenuItem14ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem14ActionPerformed

        Component pane = tabPane.getSelectedComponent();
        if (pane instanceof DataPanel) {
            try {

                DatabaseObject dato = ((DataPanel) pane).getDataOwner();
                dato.getPanelData(((DataPanel) pane));
                dato.setIDS(-1);
                if (dato.save()) {
                    ((DataPanel) pane).refresh();
                    ((DataPanel) pane).setDataOwner(dato,true);
                } else {
                    ((DataPanel) pane).showRequiredFields();
                }
            } catch (Exception e) {
                Log.Debug(this, e);
            }
        }
    }//GEN-LAST:event_jMenuItem14ActionPerformed

    private void jMenuItem16ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem16ActionPerformed
        ClipboardMenuItem item;
        try {
            if (clipboardMenu.getItemCount() > 1) {
                item = (ClipboardMenuItem) clipboardMenu.getItem(clipboardMenu.getItemCount() - 1);
                getCurrentTab().paste(item.getItem());
            }
        } catch (Exception ignore) {
            Log.Debug(ignore);
        }

    }//GEN-LAST:event_jMenuItem16ActionPerformed

    private void jMenuItem15ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem15ActionPerformed
        try {
            DataPanel tab = getCurrentTab();
            DatabaseObject dato = tab.getDataOwner();

            if (dato.isExisting()) {
                dato.getPanelData((tab));
                dato.reset();
                tab.refresh();
                tab.setDataOwner(dato,true);
            }
        } catch (Exception ignore) {
        }
    }//GEN-LAST:event_jMenuItem15ActionPerformed

    private void jMenuItem17ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem17ActionPerformed

        Search.instanceOf();

    }//GEN-LAST:event_jMenuItem17ActionPerformed

    private void jMenuItem18ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem18ActionPerformed
        addTab(new JCalendar(), Messages.CALENDAR.toString());
    }//GEN-LAST:event_jMenuItem18ActionPerformed

    private void jMenuItem19ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem19ActionPerformed
        try {
            getCurrentTab().refresh();
        } catch (Exception ignore) {
        }
    }//GEN-LAST:event_jMenuItem19ActionPerformed

    private void jMenuItem20ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem20ActionPerformed

        if (mpv5.usermanagement.MPSecurityManager.check(Context.getAccounts(), MPSecurityManager.EXPORT)) {
            try {
                XMLWriter xmlw = new XMLWriter();
                xmlw.newDoc(true);
                String name = Context.getAccounts().getDbIdentity();
                ArrayList<DatabaseObject> dbobjarr = DatabaseObject.getObjects(Context.getAccounts());
                xmlw.add(dbobjarr);
                showFilesaveDialogFor(xmlw.createFile(name));
            } catch (NodataFoundException ex) {
                Log.Debug(this, ex);
            }
        }
    }//GEN-LAST:event_jMenuItem20ActionPerformed

    private void jMenuItem21ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem21ActionPerformed


        if (mpv5.usermanagement.MPSecurityManager.check(Context.getAccounts(), MPSecurityManager.EXPORT)) {
            try {

                String name = Context.getAccounts().getDbIdentity();
                ArrayList<DatabaseObject> dbobjarr = DatabaseObject.getObjects(Context.getAccounts());

                TextDatFile t = new TextDatFile();
                t.parse(dbobjarr);

                showFilesaveDialogFor(t.createFile(name));
            } catch (NodataFoundException ex) {
                Log.Debug(this, ex);
            }
        }
    }//GEN-LAST:event_jMenuItem21ActionPerformed

    private void jMenuItem22ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem22ActionPerformed
        if (Popup.Y_N_dialog("Fill database with sample data?")) {

            int x = 1;
            try {
                x = Integer.valueOf(Popup.Enter_Value("Enter a default account ID for bills."));
            } catch (NumberFormatException numberFormatException) {
            }
            try {
                new mpv5.db.sample.SampleData(Integer.valueOf(Popup.Enter_Value("Enter a valid Integer value [3]")), x, true,true);
            } catch (NumberFormatException numberFormatException) {
                new mpv5.db.sample.SampleData(3, 1, true,true);
            }
        }
    }//GEN-LAST:event_jMenuItem22ActionPerformed

    private void jMenuItem23ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem23ActionPerformed
        addTab(TrashPanel.instanceOf(), Messages.TRASHBIN);
    }//GEN-LAST:event_jMenuItem23ActionPerformed

    private void jButton7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton7ActionPerformed
        addTab(TrashPanel.instanceOf(), Messages.TRASHBIN);
    }//GEN-LAST:event_jButton7ActionPerformed

    private void jButton8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton8ActionPerformed
        addTab(new ItemPanel(Context.getBill(), Item.TYPE_BILL), Messages.NEW_BILL);
    }//GEN-LAST:event_jButton8ActionPerformed

    private void jMenuItem24ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem24ActionPerformed
        MPServer serv = new MPServer();
        serv.start();
        MPView.identifierView.showServerStatus(serv.isAlive());
    }//GEN-LAST:event_jMenuItem24ActionPerformed

    private void jMenuItem25ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem25ActionPerformed
        LocalSettings.setProperty(LocalSettings.SCROLL_ALWAYS, String.valueOf(!tabPaneScrolled));
        LocalSettings.save();
        Popup.notice(Messages.RESTART_REQUIRED);
    }//GEN-LAST:event_jMenuItem25ActionPerformed

    private void jButton9ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton9ActionPerformed
        addTab(new mpv5.ui.panels.EURPanel(), Messages.COST);
    }//GEN-LAST:event_jButton9ActionPerformed

    private void jMenuItem26ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem26ActionPerformed


        if (mpv5.usermanagement.MPSecurityManager.check(Context.getAccounts(), MPSecurityManager.EXPORT)) {
            try {
                XMLWriter xmlw = new XMLWriter();
                xmlw.newDoc(true);
                String name = Context.getItems().getDbIdentity();
                ArrayList<DatabaseObject> dbobjarr = DatabaseObject.getObjects(Context.getItems());
                xmlw.add(dbobjarr);
                showFilesaveDialogFor(xmlw.createFile(name));
            } catch (NodataFoundException ex) {
                Log.Debug(this, ex);
            }
        }
    }//GEN-LAST:event_jMenuItem26ActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    public javax.swing.JMenu clipboardMenu;
    public javax.swing.JMenu favouritesMenu;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton18;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton24;
    private javax.swing.JButton jButton25;
    private javax.swing.JButton jButton26;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton7;
    private javax.swing.JButton jButton8;
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
    private javax.swing.JMenuItem jMenuItem3;
    private javax.swing.JMenuItem jMenuItem4;
    private javax.swing.JMenuItem jMenuItem5;
    private javax.swing.JMenuItem jMenuItem6;
    private javax.swing.JMenuItem jMenuItem7;
    private javax.swing.JMenuItem jMenuItem8;
    private javax.swing.JMenuItem jMenuItem9;
    private com.l2fprod.common.swing.JOutlookBar jOutlookBar1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JToolBar.Separator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JSeparator jSeparator4;
    private javax.swing.JSeparator jSeparator5;
    private javax.swing.JSeparator jSeparator6;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JToolBar mainToolbar;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JPanel naviPanel;
    public javax.swing.JPanel pluginIcons;
    private javax.swing.JProgressBar progressBar;
    private javax.swing.JPanel separator;
    private javax.swing.JPanel separator1;
    private javax.swing.JPanel serverlabel;
    private javax.swing.JLabel statusMessageLabel;
    private javax.swing.JPanel statusPanel;
    public static final javax.swing.JPanel tabpanePanel = new javax.swing.JPanel();
    private javax.swing.JComboBox xhistory;
    // End of variables declaration//GEN-END:variables

    /**
     * @return the mainPanel
     */
    public javax.swing.JPanel getMainPanel() {
        return mainPanel;
    }

    /**
     * @return the progressBar
     */
    public javax.swing.JProgressBar getProgressBar() {
        return progressBar;
    }

    /**
     * @return the statusMessageLabel
     */
    public javax.swing.JLabel getStatusMessageLabel() {
        return statusMessageLabel;
    }

    /**
     * @return the statusPanel
     */
    public javax.swing.JPanel getStatusPanel() {
        return statusPanel;
    }

    /**
     * @return the favouritesMenu
     */
    public javax.swing.JMenu getFavouritesMenu() {
        return favouritesMenu;
    }

    /**
     * Open the currently selected tab in a new frame
     */
    public void selectedTabInNewFrame() {
        final Component pane = tabPane.getSelectedComponent();
        if (pane != null) {
            final String title = tabPane.getTitleAt(tabPane.getSelectedIndex());
            tabPane.remove(pane);
            JFrame fr = new JFrame(title) {

                @Override
                public void dispose() {
                    tabPane.addTab(title, pane);
                    tabPane.setSelectedComponent(pane);
                    super.dispose();
                }
            };
            fr.add(pane, BorderLayout.CENTER);
            fr.setSize(pane.getSize());
            new Position(fr);
            fr.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            fr.setVisible(true);
        }
    }


    

    /**
     * Triggers the MP Server notification to the view
     * @param running
     */
    public void showServerStatus(boolean running) {
        if (running) {
            serverlabel.setMaximumSize(new Dimension(18, 18));
            serverlabel.setPreferredSize(new Dimension(18, 18));
            serverlabel.setMinimumSize(new Dimension(18, 18));
            serverlabel.setSize(18, 18);
            final JLabel plab = new JLabel();
            plab.setIcon(new MPIcon("/mpv5/resources/images/16/kwikdisk.png"));
            plab.setEnabled(true);
            plab.setToolTipText("<html><b>MP Server " + Messages.LOADED + "</b><br/>Port: " + LocalSettings.getProperty(LocalSettings.SERVER_PORT) + "</html>");
            plab.addMouseListener(new MouseAdapter() {

                @Override
                public void mouseReleased(MouseEvent e) {
                    if (e.getButton() == MouseEvent.BUTTON2 || e.getButton() == MouseEvent.BUTTON3) {
                        JLabel source = (JLabel) e.getSource();
                        final JPopupMenu m = new JPopupMenu();
                        JMenuItem n = new JMenuItem(Messages.UNLOAD.getValue());
                        n.addActionListener(new ActionListener() {

                            @Override
                            public void actionPerformed(ActionEvent e) {
                                MPServer.stopAllInstances();
                                plab.setEnabled(false);
                                plab.setToolTipText("<html><b>MP Server</b>");
                                MouseListener[] ml = plab.getMouseListeners();
                                for (int i = 0; i < ml.length; i++) {
                                    MouseListener mouseListener = ml[i];
                                    plab.removeMouseListener(mouseListener);
                                }
                            }
                        });
                        m.add(n);
                        m.show(plab, e.getX(), e.getY());
                    }
                }
            });
            serverlabel.add(plab);
        } else {
            serverlabel.setMaximumSize(new Dimension(0, 0));
            serverlabel.setPreferredSize(new Dimension(0, 0));
            serverlabel.setMinimumSize(new Dimension(0, 0));
            serverlabel.setSize(0, 0);
        }
        MPView.identifierFrame.validate();
    }

  
}
