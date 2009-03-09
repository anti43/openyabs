/*
 * MPV5View.java
 */
package mpv5.ui.frames;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Locale;

import javax.swing.Icon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JProgressBar;
import javax.swing.Timer;
import javax.swing.UIManager;
import mpv5.Main;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.db.common.NodataFoundException;
import mpv5.db.common.QueryHandler;
import mpv5.globals.LocalSettings;
import mpv5.globals.Messages;
import mpv5.items.div.Favourite;
import mpv5.logging.Log;
import mpv5.ui.dialogs.DialogForFile;
import mpv5.ui.dialogs.Popup;
import mpv5.ui.menus.ClipboardMenuItem;
import mpv5.ui.menus.FavouritesMenuItem;
import mpv5.ui.panels.ContactPanel;
import mpv5.ui.panels.ContactsList;
import mpv5.ui.panels.DataPanel;
import mpv5.ui.panels.MPControlPanel;
import mpv5.ui.parents.CloseableTabbedPane;
import mpv5.ui.parents.FadeOnChangeLabel;
import mpv5.ui.parents.Position;
import mpv5.usermanagement.User;
import mpv5.utils.print.PrintJob;
import mpv5.utils.text.TypeConversion;
import mpv5.utils.xml.XMLWriter;
import org.jdesktop.application.SingleFrameApplication;
import org.jdesktop.application.FrameView;
import org.jdesktop.application.ResourceMap;
import org.jdesktop.application.TaskMonitor;

/**
 * The application's main frame.
 */
public class MPV5View extends FrameView {

    public static MPV5View identifierView;
    public static JFrame identifierFrame;
    public static CloseableTabbedPane tabPane;
    public static JLabel messagelabel = new JLabel();
    public static User currentUser;
    public static JProgressBar progressbar = new JProgressBar();
    private static JMenu favMenu;
    private static String predefTitle;
    public static DialogForFile filedialog;
    public static SingleFrameApplication identifierApplication;

    /**
     * Display a message at the bottom of the MP frame
     * @param message
     */
    public static void addMessage(String message) {
        messagelabel.setText(message);
    }

    /**
     * Returns the currently selected tab on the main tab pane
     * @return
     */
    public static Object getShowingTab() {
        return tabPane.getSelectedComponent();
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
    public static void setProgressMaximumValue(int max) {
        progressbar.setMaximum(max);
    }

    /**
     *  Sets the current value for the progressbar
     * @param val
     */
    public static void setProgressValue(int val) {
        progressbar.setValue(val);
    }

    /**
     * Reset the progress bar
     */
    public static void setProgressReset() {
        progressbar.setValue(0);
        progressbar.setIndeterminate(false);
    }

    /**
     * Sets the indeterminate property of the progress bar.
     * @param b
     */
    public static void setProgressRunning(boolean b) {
        progressbar.setIndeterminate(b);
    }

    /**
     *
     * @return The currently logged in user
     */
    public static User getUser() {
        if (currentUser == null) {
            Log.Debug(MPV5View.class, "There is no user logged in here, using default user.");
            try {
                currentUser = User.DEFAULT;
                return currentUser;
            } catch (Exception ex) {
                Log.Debug(MPV5View.class, "Default user is missing.");
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
        Main.setLaF(usern.__getLaf());
        Locale.setDefault(TypeConversion.stringToLocale(usern.__getLocale()));
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
        return (DataPanel) tabPane.getSelectedComponent();
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

    public MPV5View(SingleFrameApplication app) {
        super(app);

        MPV5View.identifierApplication = app;

        initComponents();

        tabPane = new CloseableTabbedPane(this);
        identifierFrame = this.getFrame();
        Popup.identifier = identifierFrame;
        progressbar = this.progressBar;
        progressbar.setMinimum(0);
        messagelabel = statusMessageLabel;
        tabpanePanel.add(tabPane, BorderLayout.CENTER);
        favMenu = favouritesMenu;
        identifierView = this;
        filedialog = new DialogForFile(DialogForFile.FILES_ONLY);

        if (predefTitle != null) {
            identifierFrame.setTitle(identifierFrame.getTitle() + predefTitle);
        }

        fillFavouritesmenu();
        QueryHandler.setWaitCursorFor(identifierFrame);

//        setStatusBar();
//        setStatusBar(statusPanel);
    }

    /**
     * Set the state of the main toolbar
     * @param enable
     */
    public void enableToolBar(boolean enable) {
        mainToolbar.setEnabled(enable);
    }

    /**
     * Add a tab to the main tab pane
     * @param item
     */
    public void addTab(DatabaseObject item) {
        if (item.getDbIdentity().equalsIgnoreCase(Context.getContact().getDbIdentity())) {
            addContactTab(item);
        }
    }

    private void addContactTab(DatabaseObject item) {
        ContactPanel tab = new ContactPanel(Context.getCustomer());
        tab.setType(ContactPanel.CUSTOMER);
        tabPane.addTab(item.__getCName(), tab);
        tab.setDataOwner(item);
        tabPane.setSelectedComponent(tab);
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
        jScrollPane1 = new javax.swing.JScrollPane();
        naviPanel = new javax.swing.JPanel();
        jOutlookBar1 = new com.l2fprod.common.swing.JOutlookBar();
        jPanel2 = new javax.swing.JPanel();
        jButton5 = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton18 = new javax.swing.JButton();
        jButton20 = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        jPanel6 = new javax.swing.JPanel();
        menuBar = new javax.swing.JMenuBar();
        javax.swing.JMenu fileMenu = new javax.swing.JMenu();
        jMenuItem6 = new javax.swing.JMenuItem();
        jMenu4 = new javax.swing.JMenu();
        jMenu5 = new javax.swing.JMenu();
        jMenuItem8 = new javax.swing.JMenuItem();
        jMenu1 = new javax.swing.JMenu();
        jMenu2 = new javax.swing.JMenu();
        jMenuItem1 = new javax.swing.JMenuItem();
        jMenuItem2 = new javax.swing.JMenuItem();
        jMenuItem4 = new javax.swing.JMenuItem();
        jMenu3 = new javax.swing.JMenu();
        jMenuItem3 = new javax.swing.JMenuItem();
        favouritesMenu = new javax.swing.JMenu();
        jMenuItem5 = new javax.swing.JMenuItem();
        clipboardMenu = new javax.swing.JMenu();
        jMenu6 = new javax.swing.JMenu();
        jMenuItem7 = new javax.swing.JMenuItem();
        jMenuItem9 = new javax.swing.JMenuItem();
        statusPanel = new javax.swing.JPanel();
        statusMessageLabel = new FadeOnChangeLabel();
        progressBar = new javax.swing.JProgressBar();
        statusAnimationLabel = new javax.swing.JLabel();
        mainToolbar = new javax.swing.JToolBar();
        jButton24 = new javax.swing.JButton();
        jButton26 = new javax.swing.JButton();
        jSeparator2 = new javax.swing.JToolBar.Separator();
        jButton25 = new javax.swing.JButton();

        mainPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        mainPanel.setName("mainPanel"); // NOI18N

        jScrollPane1.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        jScrollPane1.setName("jScrollPane1"); // NOI18N

        tabpanePanel.setBackground(new java.awt.Color(255, 255, 255));
        tabpanePanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        tabpanePanel.setName("tabpanePanel"); // NOI18N
        tabpanePanel.setLayout(new java.awt.BorderLayout());
        jScrollPane1.setViewportView(tabpanePanel);

        naviPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        naviPanel.setName("naviPanel"); // NOI18N

        jOutlookBar1.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        jOutlookBar1.setName("jOutlookBar1"); // NOI18N

        jPanel2.setBackground(new java.awt.Color(153, 153, 153));
        jPanel2.setName("jPanel2"); // NOI18N
        jPanel2.setPreferredSize(new java.awt.Dimension(110, 400));

        jButton5.setFont(new java.awt.Font("Tahoma", 0, 10));
        jButton5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/mpv5/resources/images/32/agt_family.png"))); // NOI18N
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("mpv5/resources/languages/Panels"); // NOI18N
        jButton5.setText(bundle.getString("MPV5View.jButton5.text_1")); // NOI18N
        jButton5.setToolTipText(bundle.getString("MPV5View.jButton5.toolTipText_1")); // NOI18N
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
        jButton1.setText(bundle.getString("MPV5View.jButton1.text_1")); // NOI18N
        jButton1.setToolTipText(bundle.getString("MPV5View.jButton1.toolTipText_1")); // NOI18N
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
        jButton2.setText(bundle.getString("MPV5View.jButton2.text_1")); // NOI18N
        jButton2.setToolTipText(bundle.getString("MPV5View.jButton2.toolTipText_1")); // NOI18N
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
        jButton18.setText(bundle.getString("MPV5View.jButton18.text_1")); // NOI18N
        jButton18.setToolTipText(bundle.getString("MPV5View.jButton18.toolTipText_1")); // NOI18N
        jButton18.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton18.setName("jButton18"); // NOI18N
        jButton18.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton18.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton18ActionPerformed(evt);
            }
        });

        jButton20.setFont(new java.awt.Font("Tahoma", 0, 10));
        jButton20.setIcon(new javax.swing.ImageIcon(getClass().getResource("/mpv5/resources/images/32/agt_internet.png"))); // NOI18N
        jButton20.setText(bundle.getString("MPV5View.jButton20.text_1")); // NOI18N
        jButton20.setToolTipText(bundle.getString("MPV5View.jButton20.toolTipText_1")); // NOI18N
        jButton20.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton20.setName("jButton20"); // NOI18N
        jButton20.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton20.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton20ActionPerformed(evt);
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
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jButton20, javax.swing.GroupLayout.DEFAULT_SIZE, 89, Short.MAX_VALUE)
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
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButton20)
                .addGap(119, 119, 119))
        );

        jOutlookBar1.addTab(bundle.getString("MPV5View.jPanel2.TabConstraints.tabTitle_1"), jPanel2); // NOI18N

        jPanel3.setBackground(new java.awt.Color(153, 153, 153));
        jPanel3.setName("jPanel3"); // NOI18N
        jPanel3.setPreferredSize(new java.awt.Dimension(110, 400));

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        jOutlookBar1.addTab(bundle.getString("MPV5View.jPanel3.TabConstraints.tabTitle_1"), jPanel3); // NOI18N

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

        jOutlookBar1.addTab(bundle.getString("MPV5View.jPanel4.TabConstraints.tabTitle_1"), jPanel4); // NOI18N

        jPanel6.setBackground(new java.awt.Color(153, 153, 153));
        jPanel6.setName("jPanel6"); // NOI18N
        jPanel6.setPreferredSize(new java.awt.Dimension(110, 400));

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        jOutlookBar1.addTab(bundle.getString("MPV5View.jPanel6.TabConstraints.tabTitle"), jPanel6); // NOI18N

        javax.swing.GroupLayout naviPanelLayout = new javax.swing.GroupLayout(naviPanel);
        naviPanel.setLayout(naviPanelLayout);
        naviPanelLayout.setHorizontalGroup(
            naviPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jOutlookBar1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        naviPanelLayout.setVerticalGroup(
            naviPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(naviPanelLayout.createSequentialGroup()
                .addComponent(jOutlookBar1, javax.swing.GroupLayout.PREFERRED_SIZE, 490, Short.MAX_VALUE)
                .addContainerGap())
        );

        javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addComponent(naviPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 335, Short.MAX_VALUE))
        );
        mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(naviPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 506, Short.MAX_VALUE)
        );

        menuBar.setName("menuBar"); // NOI18N

        fileMenu.setText(bundle.getString("MPV5View.fileMenu.text_1")); // NOI18N
        fileMenu.setName("fileMenu"); // NOI18N

        jMenuItem6.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_P, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem6.setIcon(new javax.swing.ImageIcon(getClass().getResource("/mpv5/resources/images/22/printer1.png"))); // NOI18N
        jMenuItem6.setText(bundle.getString("MPV5View.jMenuItem6.text")); // NOI18N
        jMenuItem6.setName("jMenuItem6"); // NOI18N
        jMenuItem6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem6ActionPerformed(evt);
            }
        });
        fileMenu.add(jMenuItem6);

        jMenu4.setText(bundle.getString("MPV5View.jMenu4.text")); // NOI18N
        jMenu4.setName("jMenu4"); // NOI18N

        jMenu5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/mpv5/resources/images/16/kontact_contacts.png"))); // NOI18N
        jMenu5.setText(bundle.getString("MPV5View.jMenu5.text")); // NOI18N
        jMenu5.setName("jMenu5"); // NOI18N

        jMenuItem8.setIcon(new javax.swing.ImageIcon(getClass().getResource("/mpv5/resources/images/16/xml.png"))); // NOI18N
        jMenuItem8.setText(bundle.getString("MPV5View.jMenuItem8.text")); // NOI18N
        jMenuItem8.setName("jMenuItem8"); // NOI18N
        jMenuItem8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem8ActionPerformed(evt);
            }
        });
        jMenu5.add(jMenuItem8);

        jMenu4.add(jMenu5);

        fileMenu.add(jMenu4);

        menuBar.add(fileMenu);

        jMenu1.setText(bundle.getString("MPV5View.jMenu1.text_1")); // NOI18N
        jMenu1.setName("jMenu1"); // NOI18N

        jMenu2.setText(bundle.getString("MPV5View.jMenu2.text_1")); // NOI18N
        jMenu2.setName("jMenu2"); // NOI18N

        jMenuItem1.setText(bundle.getString("MPV5View.jMenuItem1.text_1")); // NOI18N
        jMenuItem1.setName("jMenuItem1"); // NOI18N
        jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem1ActionPerformed(evt);
            }
        });
        jMenu2.add(jMenuItem1);

        jMenuItem2.setText(bundle.getString("MPV5View.jMenuItem2.text_1")); // NOI18N
        jMenuItem2.setName("jMenuItem2"); // NOI18N
        jMenuItem2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem2ActionPerformed(evt);
            }
        });
        jMenu2.add(jMenuItem2);

        jMenu1.add(jMenu2);

        jMenuItem4.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/mpv5/resources/images/16/2uparrow.png"))); // NOI18N
        jMenuItem4.setText(bundle.getString("MPV5View.jMenuItem4.text")); // NOI18N
        jMenuItem4.setName("jMenuItem4"); // NOI18N
        jMenuItem4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem4ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem4);

        menuBar.add(jMenu1);

        jMenu3.setText(bundle.getString("MPV5View.jMenu3.text_1")); // NOI18N
        jMenu3.setName("jMenu3"); // NOI18N

        jMenuItem3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/mpv5/resources/images/16/configure_shortcuts.png"))); // NOI18N
        jMenuItem3.setText(bundle.getString("MPV5View.jMenuItem3.text_1")); // NOI18N
        jMenuItem3.setName("jMenuItem3"); // NOI18N
        jMenuItem3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem3ActionPerformed(evt);
            }
        });
        jMenu3.add(jMenuItem3);

        menuBar.add(jMenu3);

        favouritesMenu.setText(bundle.getString("MPV5View.favouritesMenu.text")); // NOI18N
        favouritesMenu.setName("favouritesMenu"); // NOI18N

        jMenuItem5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/mpv5/resources/images/16/remove.png"))); // NOI18N
        jMenuItem5.setText(bundle.getString("MPV5View.jMenuItem5.text")); // NOI18N
        jMenuItem5.setName("jMenuItem5"); // NOI18N
        jMenuItem5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem5ActionPerformed(evt);
            }
        });
        favouritesMenu.add(jMenuItem5);

        menuBar.add(favouritesMenu);

        clipboardMenu.setText(bundle.getString("MPV5View.clipboardMenu.text")); // NOI18N
        clipboardMenu.setName("clipboardMenu"); // NOI18N

        jMenu6.setText(bundle.getString("MPV5View.jMenu6.text")); // NOI18N
        jMenu6.setName("jMenu6"); // NOI18N

        jMenuItem7.setText(bundle.getString("MPV5View.jMenuItem7.text")); // NOI18N
        jMenuItem7.setName("jMenuItem7"); // NOI18N
        jMenuItem7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem7ActionPerformed(evt);
            }
        });
        jMenu6.add(jMenuItem7);

        jMenuItem9.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_N, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem9.setIcon(new javax.swing.ImageIcon(getClass().getResource("/mpv5/resources/images/16/editcopy.png"))); // NOI18N
        jMenuItem9.setText(bundle.getString("MPV5View.jMenuItem9.text")); // NOI18N
        jMenuItem9.setName("jMenuItem9"); // NOI18N
        jMenuItem9.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem9ActionPerformed(evt);
            }
        });
        jMenu6.add(jMenuItem9);

        clipboardMenu.add(jMenu6);

        menuBar.add(clipboardMenu);

        statusPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        statusPanel.setName("statusPanel"); // NOI18N
        statusPanel.setPreferredSize(new java.awt.Dimension(800, 20));

        statusMessageLabel.setName("statusMessageLabel"); // NOI18N

        progressBar.setBorder(new javax.swing.border.LineBorder(javax.swing.UIManager.getDefaults().getColor("Button.darkShadow"), 1, true));
        progressBar.setName("progressBar"); // NOI18N

        statusAnimationLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        statusAnimationLabel.setName("statusAnimationLabel"); // NOI18N

        javax.swing.GroupLayout statusPanelLayout = new javax.swing.GroupLayout(statusPanel);
        statusPanel.setLayout(statusPanelLayout);
        statusPanelLayout.setHorizontalGroup(
            statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(statusPanelLayout.createSequentialGroup()
                .addGroup(statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, statusPanelLayout.createSequentialGroup()
                        .addComponent(statusMessageLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 368, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, statusPanelLayout.createSequentialGroup()
                        .addContainerGap(443, Short.MAX_VALUE)
                        .addComponent(statusAnimationLabel)))
                .addContainerGap())
        );
        statusPanelLayout.setVerticalGroup(
            statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, statusPanelLayout.createSequentialGroup()
                .addGroup(statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(statusPanelLayout.createSequentialGroup()
                        .addGap(3, 3, 3)
                        .addComponent(statusMessageLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 13, Short.MAX_VALUE))
                    .addComponent(progressBar, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 16, Short.MAX_VALUE)
                    .addComponent(statusAnimationLabel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 0, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, 0))
        );

        mainToolbar.setRollover(true);
        mainToolbar.setName("mainToolbar"); // NOI18N

        jButton24.setIcon(new javax.swing.ImageIcon(getClass().getResource("/mpv5/resources/images/32/endturn.png"))); // NOI18N
        jButton24.setToolTipText(bundle.getString("MPV5View.jButton24.toolTipText_1")); // NOI18N
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
        jButton26.setToolTipText(bundle.getString("MPV5View.jButton26.toolTipText_1")); // NOI18N
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
        jButton25.setToolTipText(bundle.getString("MPV5View.jButton25.toolTipText_1")); // NOI18N
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
        tabPane.addTab(Messages.NEW_CUSTOMER, tab);
        tabPane.setSelectedComponent(tab);
    }//GEN-LAST:event_jButton5ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        ContactPanel tab = new ContactPanel(Context.getSupplier());
        tab.setType(ContactPanel.SUPPLIER);
        tabPane.addTab(Messages.NEW_SUPPLIER, tab);
        tabPane.setSelectedComponent(tab);
}//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed

        ContactsList tab = new ContactsList(Context.getContact());
        tabPane.addTab(Messages.CONTACTS_LIST, tab);
        tabPane.setSelectedComponent(tab);
}//GEN-LAST:event_jButton2ActionPerformed

    private void jButton20ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton20ActionPerformed

        ContactPanel tab = new ContactPanel(Context.getCompany());
        tab.setType(ContactPanel.COMPANY);
        tabPane.addTab(Messages.NEW_COMPANY, tab);
        tabPane.setSelectedComponent(tab);
    }//GEN-LAST:event_jButton20ActionPerformed

    private void jButton18ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton18ActionPerformed
        ContactPanel tab = new ContactPanel(Context.getManufacturer());
        tab.setType(ContactPanel.MANUFACTURER);
        tabPane.addTab(Messages.NEW_MANUFACTURER, tab);
        tabPane.setSelectedComponent(tab);
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
        tabPane.addTab(Messages.CONTROL_PANEL, MPControlPanel.instanceOf());
        tabPane.setSelectedComponent(MPControlPanel.instanceOf());
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
        try {
            XMLWriter xmlw = new XMLWriter();
            xmlw.newDoc(Context.getContact().getDbIdentity());
            String name = Context.getContact().getDbIdentity();
            ArrayList<DatabaseObject> dbobjarr = DatabaseObject.getObjects(Context.getContact());
            xmlw.add(dbobjarr);
            showFilesaveDialogFor(xmlw.createFile(name));
        } catch (NodataFoundException ex) {
            Log.Debug(this, ex);
        }
    }//GEN-LAST:event_jMenuItem8ActionPerformed

    private void jMenuItem9ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem9ActionPerformed

        clipboardMenu.add(new ClipboardMenuItem(getCurrentTab().getDataOwner()));

    }//GEN-LAST:event_jMenuItem9ActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    public javax.swing.JMenu clipboardMenu;
    public javax.swing.JMenu favouritesMenu;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton18;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton20;
    private javax.swing.JButton jButton24;
    private javax.swing.JButton jButton25;
    private javax.swing.JButton jButton26;
    private javax.swing.JButton jButton5;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenu jMenu3;
    private javax.swing.JMenu jMenu4;
    private javax.swing.JMenu jMenu5;
    private javax.swing.JMenu jMenu6;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem2;
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
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JToolBar.Separator jSeparator2;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JToolBar mainToolbar;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JPanel naviPanel;
    private javax.swing.JProgressBar progressBar;
    private javax.swing.JLabel statusAnimationLabel;
    private javax.swing.JLabel statusMessageLabel;
    private javax.swing.JPanel statusPanel;
    public static final javax.swing.JPanel tabpanePanel = new javax.swing.JPanel();
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
    private Timer messageTimer;
    private Timer busyIconTimer;
    private Icon idleIcon;
    private Icon[] busyIcons = new Icon[15];
    private int busyIconIndex = 0;

    private void setStatusBar() {
        // status bar initialization - message timeout, idle icon and busy animation, etc
        ResourceMap resourceMap = getResourceMap();
        int messageTimeout = resourceMap.getInteger("StatusBar.messageTimeout");
        messageTimer = new Timer(messageTimeout, new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                statusMessageLabel.setText("");
            }
        });
        messageTimer.setRepeats(false);
        int busyAnimationRate = resourceMap.getInteger("StatusBar.busyAnimationRate");
        for (int i = 0; i < busyIcons.length; i++) {
            busyIcons[i] = resourceMap.getIcon("StatusBar.busyIcons[" + i + "]");
        }
        busyIconTimer = new Timer(busyAnimationRate, new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                busyIconIndex = (busyIconIndex + 1) % busyIcons.length;
                statusAnimationLabel.setIcon(busyIcons[busyIconIndex]);
            }
        });
        idleIcon = resourceMap.getIcon("StatusBar.idleIcon");
        statusAnimationLabel.setIcon(idleIcon);
        progressBar.setVisible(false);

        // connecting action tasks to status bar via TaskMonitor
        TaskMonitor taskMonitor = new TaskMonitor(getApplication().getContext());
        taskMonitor.addPropertyChangeListener(new java.beans.PropertyChangeListener() {

            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                String propertyName = evt.getPropertyName();
                if ("started".equals(propertyName)) {
                    if (!busyIconTimer.isRunning()) {
                        statusAnimationLabel.setIcon(busyIcons[0]);
                        busyIconIndex = 0;
                        busyIconTimer.start();
                    }
                    progressBar.setVisible(true);
                    progressBar.setIndeterminate(true);
                } else if ("done".equals(propertyName)) {
                    busyIconTimer.stop();
                    statusAnimationLabel.setIcon(idleIcon);
                    progressBar.setVisible(false);
                    progressBar.setValue(0);
                } else if ("message".equals(propertyName)) {
                    String text = (String) (evt.getNewValue());
                    statusMessageLabel.setText((text == null) ? "" : text);
                    messageTimer.restart();
                } else if ("progress".equals(propertyName)) {
                    int value = (Integer) (evt.getNewValue());
                    progressBar.setVisible(true);
                    progressBar.setIndeterminate(false);
                    progressBar.setValue(value);
                }
            }
        });
    }
}