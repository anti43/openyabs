/*
 * MPView.java
 */
package mpv5.ui.frames;

import com.l2fprod.common.swing.JOutlookBar;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import mpv5.Main;
import mpv5.YabsApplication;
import mpv5.YabsView;
import mpv5.YabsViewProxy;
import mpv5.YabsViewProxy.FlowProvider;
import mpv5.bugtracker.ExceptionHandler;
import mpv5.bugtracker.SubmitForm;
import mpv5.data.MPList;
import mpv5.data.PropertyStore;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.db.common.NodataFoundException;
import mpv5.db.common.QueryHandler;
import mpv5.db.migration.MigrationWB;
import mpv5.db.objects.Contact;
import mpv5.db.objects.Favourite;
import mpv5.db.objects.Item;
import mpv5.db.objects.Product;
import mpv5.db.objects.User;
import mpv5.db.objects.ValueProperty;
import mpv5.globals.Constants;
import mpv5.globals.GlobalSettings;
import mpv5.globals.LocalSettings;
import mpv5.globals.Messages;
import mpv5.handler.Scheduler;
import mpv5.i18n.LanguageManager;
import mpv5.logging.Log;
import mpv5.pluginhandling.YabsPluginLoader;

import mpv5.ui.dialogs.About;
import mpv5.ui.dialogs.BigPopup;
import mpv5.ui.dialogs.DialogForFile;
import mpv5.ui.dialogs.ListView;
import mpv5.ui.dialogs.LoginToInstanceScreen;
import mpv5.ui.dialogs.Notificator;
import mpv5.ui.dialogs.Popup;
import mpv5.ui.dialogs.Search;
import mpv5.ui.dialogs.Search2;
import mpv5.ui.dialogs.Wizard;
import mpv5.ui.dialogs.subcomponents.ControlPanel_MailTemplates;
import mpv5.ui.dialogs.subcomponents.wizard_CSVImport2_1;
import mpv5.ui.dialogs.subcomponents.wizard_FirstSettings1;
import mpv5.ui.dialogs.subcomponents.wizard_MP45_Import;
import mpv5.ui.dialogs.subcomponents.wizard_Toolbar1;
import mpv5.ui.dialogs.subcomponents.wizard_XMLImport_1;
import mpv5.ui.dialogs.subcomponents.wizard_XMLImport_2;
import mpv5.ui.dialogs.subcomponents.wizard_Yabs1_Import;
import mpv5.ui.menus.ClipboardMenuItem;
import mpv5.ui.menus.FavouritesMenuItem;
import mpv5.ui.misc.CloseableTabbedPane;
import mpv5.ui.misc.FadeOnChangeLabel;
import mpv5.ui.misc.MPTable;
import mpv5.ui.misc.Position;
import mpv5.ui.panels.ActivityConfirmationPanel;
import mpv5.ui.panels.ChangeNotApprovedException;
import mpv5.ui.panels.ContactsList;
import mpv5.ui.panels.ConversationPanel;
import mpv5.ui.panels.DataPanel;
import mpv5.ui.panels.ExpensePanel;
import mpv5.ui.panels.GeneralListPanel;
import mpv5.ui.panels.HistoryPanel;
import mpv5.ui.panels.ItemPanel2;
import mpv5.ui.panels.JournalPanel;
import mpv5.ui.panels.MPControlPanel;
import mpv5.ui.panels.MassPrintPanel;
import mpv5.ui.panels.ProductList;
import mpv5.ui.panels.ProductListsPanel;
import mpv5.ui.panels.ProductOrderPanel;
import mpv5.ui.panels.ProductPanel;
import mpv5.ui.panels.ProductsOverview;
import mpv5.ui.panels.QueryPanel;
import mpv5.ui.panels.ReceiptLookup;
import mpv5.ui.panels.RevenuePanel;
import mpv5.ui.panels.StartPage;
import mpv5.ui.panels.Stockmanager;
import mpv5.ui.panels.TrashPanel;
import mpv5.usermanagement.MPSecurityManager;
import mpv5.utils.export.Export;
import mpv5.utils.export.VCFParser;
import mpv5.utils.files.TextDatFile;
import mpv5.utils.images.MPIcon;
import mpv5.utils.renderer.ComboBoxRendererForTooltip;
import mpv5.utils.xml.XMLWriter;
import net.sf.vcard4j.java.VCard;
import org.jdesktop.application.FrameView;
import org.jdesktop.application.SingleFrameApplication;

/**
 * The application's main frame.
 */
public final class MPView extends FrameView implements YabsView, FlowProvider {

    public static MPView identifierView;
    private static Dimension initialSize = new Dimension(1100, 900);
    public static JFrame identifierFrame;
    private static CloseableTabbedPane tabPane;
    private static JLabel messagelabel = new JLabel();
    private static JComboBox history = new JComboBox();
    private static JProgressBar progressbar = new JProgressBar();
    private static JMenu favMenu;
    private static String predefTitle;
    private static DialogForFile filedialog;
    public static SingleFrameApplication identifierApplication;
    private static boolean navBarAnimated = true;
    private static JLabel staterrorlabel = new JLabel();
    private static MPList currentList = new MPList();
    private static ListView clistview = new ListView(currentList);
    private static JFrame clipboard = null;
    private static boolean loaded = false;
    private GroovyShell groovyShell;
    public static HashMap<String, Object> BINDING_VARS;

    /**
     * Display a message at the bottom of the MP frame
     *
     * @param message
     * @param color
     */
    @Override
    public void addMessage(Object message, Color color) {
        addMessage(String.valueOf(message));
    }

    /**
     * Display a message at the bottom of the MP frame
     *
     * @param message
     */
    @Override
    public void addMessage(Object message) {
        MPView.addMessage(String.valueOf(message), Color.GREEN);
    }

    /**
     * Display a message at the bottom of the MP frame
     *
     * @param message
     * @param color
     */
    public static synchronized void addMessage(final String message, final Color color) {
        if (loaded && messagelabel != null) {//dont do anything if main frame is not constructed
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    if (messagelabel instanceof FadeOnChangeLabel) {
                        ((FadeOnChangeLabel) getMessagelabel()).setFadeColor(color);
                    }
                    messagelabel.setText(message);
                    messagelabel.invalidate();
                    getHistory().addItem(message);
                    getHistory().setSelectedItem(message);
                    messagelabel.repaint();
                }
            };
            SwingUtilities.invokeLater(runnable);
        }
    }

    /**
     * Returns the currently selected tab on the main tab pane
     *
     * @return
     */
    public static Object getShowingTab() {
        return getTabPane().getSelectedComponent();
    }

    /**
     * Change the navigation bar behavior
     *
     * @param animated
     */
    public static void setNavBarAnimated(boolean animated) {
        navBarAnimated = animated;
    }

    /**
     * Shows a file save dialog with the selcted file f. If the file's parent
     * directory is not the current directory, changes the current directory to
     * be the file's parent directory.
     *
     * @param f
     */
    @Override
    public void showFilesaveDialogFor(File f) {
        getFiledialog().setSelectedFile(new File(f.getName()));
        getFiledialog().saveFile(f);
    }

    /**
     * Initialize and show the secondary JFrame. This method is intended for
     * showing "secondary" windows, like message dialogs, about boxes, and so
     * on.
     *
     * Unlike the mainFrame, dismissing a secondary window will not exit the
     * application. Session state is only automatically saved if the specified
     * JFrame has a name, and then only for component descendants that are
     * named. Throws an IllegalArgumentException if c is null
     *
     * @param c
     */
    public static void show(JFrame c) {
        Main.getApplication().show(c);
    }
    /**
     * The handler for all plugins
     */
    private static YabsPluginLoader pluginLoader;

    /**
     * Let the view notify the user about an unexpected error
     */
    public static void showError() {
        if (Main.INSTANTIATED) {
            try {
                getStaterrorlabel().setIcon(new javax.swing.ImageIcon(MPView.class.getResource("/images/16/remove.png"))); // NOI18N
            } catch (Exception e) {
            }
        }
    }

    /**
     * @return the tabPane
     */
    public static CloseableTabbedPane getTabPane() {
        return tabPane;
    }

    /**
     * @return the messagelabel
     */
    public static JLabel getMessagelabel() {
        return messagelabel;
    }

    /**
     * @return the progressbar
     */
    @Override
    public JProgressBar getProgressbar() {
        return progressbar;
    }

    /**
     * @return the tabpanePanel
     */
    public static javax.swing.JPanel getTabpanePanel() {
        return tabpanePanel;
    }

    /**
     * @return the identifierView
     */
    @Override
    public MPView getIdentifierView() {
        return identifierView;
    }

    /**
     * @return the initialSize
     */
    public static Dimension getInitialSize() {
        return initialSize;
    }

    /**
     * @return the identifierFrame
     */
    @Override
    public JFrame getIdentifierFrame() {
        return identifierFrame;
    }

    /**
     * @return the history
     */
    public static JComboBox getHistory() {
        return history;
    }

    /**
     * @return the favMenu
     */
    public static JMenu getFavMenu() {
        return favMenu;
    }

    /**
     * @return the predefTitle
     */
    public static String getPredefTitle() {
        return predefTitle;
    }

    /**
     * @return the filedialog
     */
    @Override
    public DialogForFile getFiledialog() {
        return filedialog;
    }

    /**
     * @return the identifierApplication
     */
    public YabsApplication getIdentifierApplication() {
        return (YabsApplication) identifierApplication;
    }

    /**
     * @return the navBarAnimated
     */
    public static boolean isNavBarAnimated() {
        return navBarAnimated;
    }

  
    /**
     * @return the staterrorlabel
     */
    public static JLabel getStaterrorlabel() {
        return staterrorlabel;
    }

    /**
     * @return the currentList
     */
    public static MPList getCurrentList() {
        return currentList;
    }

    /**
     * @return the pluginLoader
     */
    @Override
    public YabsPluginLoader getPluginLoader() {
        return pluginLoader;
    }

    /**
     * @param c
     * @return the clisttab
     */
    public static synchronized ContactsList getClisttab(Context c) {
        if (c.equals(Context.getCustomer())) {
            if (clisttabc == null) {
                clisttabc = new ContactsList(c);
            }
            return clisttabc;
        } else if (c.equals(Context.getSupplier())) {
            if (clisttabs == null) {
                clisttabs = new ContactsList(c);
            }
            return clisttabs;
        } else if (c.equals(Context.getManufacturer())) {
            if (clisttabm == null) {
                clisttabm = new ContactsList(c);
            }
            return clisttabm;
        } else {
            return new ContactsList();
        }
    }

    /**
     * @return the plisttab
     */
    public static ProductList getPlisttab() {
        return plisttab;
    }

    public static void setPredefTitle(String string) {
        predefTitle = string;
    }

    /**
     * Sets the max value for the progressbar
     *
     * @param max
     */
    @Override
    public synchronized void setProgressMaximumValue(int max) {
        getProgressbar().setMaximum(max);
    }

    /**
     * Sets the current value for the progressbar
     *
     * @param val
     */
    @Override
    public synchronized void setProgressValue(int val) {
        getProgressbar().setValue(val);
    }

    /**
     * Reset the progress bar
     */
    @Override
    public synchronized void setProgressReset() {
        getProgressbar().setValue(0);
        getProgressbar().setIndeterminate(false);
    }

    /**
     * Sets the indeterminate property of the progress bar.
     *
     * @param b
     */
    @Override
    public synchronized void setProgressRunning(boolean b) {
        getProgressbar().setIndeterminate(b);
    }

    /**
     *
     * @return The currently logged in user
     * @deprecated Use User.getCurrentUser instead
     */
    @Deprecated
    public static User getUser() {
        return mpv5.db.objects.User.getCurrentUser();
    }

    /**
     * Sets the cursor to waiting state if true
     *
     * @param truee
     */
    @Override
    public void setWaiting(boolean truee) {
        if (Main.INSTANTIATED && getIdentifierFrame() != null) {
            if (truee) {
                getIdentifierFrame().setCursor(new Cursor(Cursor.WAIT_CURSOR));
            } else {
                getIdentifierFrame().setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }
        }
    }

    /**
     * Returns the curently selected tab or null if this is not a
     * {@link DataPanel}
     *
     * @return
     */
    @Override
    public DataPanel getCurrentTab() {
        if (getTabPane().getSelectedComponent() instanceof DataPanel) {
            return (DataPanel) getTabPane().getSelectedComponent();
        } else if (getTabPane().getSelectedComponent() instanceof JScrollPane) {
            try {
                return (DataPanel) ((JScrollPane) getTabPane().getSelectedComponent()).getComponent(0);
            } catch (ClassCastException e) {
                try {
                    return (DataPanel) ((JViewport) ((JScrollPane) getTabPane().getSelectedComponent()).getComponent(0)).getComponent(0);
                } catch (Exception ek) {
                    return null;
                }
            }
        } else {
            return null;
        }
    }

    /**
     * Returns the tab at the specified position, or NULL if the tab is not
     * existing OR not a {@link DataPanel}
     *
     * @param position
     * @return
     */
    public DataPanel getTabAt(int position) {
        if (getTabPane().getComponent(position) instanceof DataPanel) {
            return (DataPanel) getTabPane().getComponent(position);
        } else if (getTabPane().getComponent(position) instanceof JScrollPane) {
            try {
                return (DataPanel) ((JScrollPane) getTabPane().getComponent(position)).getComponent(0);
            } catch (ClassCastException e) {
                try {
                    return (DataPanel) ((JViewport) ((JScrollPane) getTabPane().getComponent(position)).getComponent(0)).getComponent(0);
                } catch (Exception et) {
                    return null;
                }
            }
        } else {
            return null;
        }
    }

    @Override
    public final void reloadFavorites() {
        Favourite[] favs = Favourite.getUserFavourites();
        for (int i = 0; i < favs.length; i++) {
            Favourite fav = favs[i];
            try {
                getFavMenu().add(new FavouritesMenuItem(Favourite.getObject(fav.getFavContext(), fav.__getItemsids())));
            } catch (NodataFoundException ex) {
//                Log.Debug(this, ex.getMessage());
            }
        }
    }

    /**
     * Add something to the clipboard menu
     *
     * @param obj
     */
    @Override
    public void addToClipBoard(DatabaseObject obj) {
        getClipboardMenu().add(new ClipboardMenuItem(obj));
        getCurrentList().add(obj);
        setClipBoardVisible(true);
    }

    public MPView(SingleFrameApplication app) {
        super(app);

        MPView.identifierApplication = app;

        initComponents();

        initNav();

        try {
            Image icon = Toolkit.getDefaultToolkit().getImage(this.getFrame().getClass().getResource(Constants.ICON));
            this.getFrame().setIconImage(icon);
            this.getFrame().setTitle(Constants.TITLE);
        } catch (Exception e) {
            Log.Debug(e);
        }

        tabPane = new CloseableTabbedPane(this);
        identifierFrame = this.getFrame();
        Popup.identifier = identifierFrame;
        progressbar = this.progressBar;
        progressbar.setMinimum(0);
        messagelabel = statusMessageLabel;
        staterrorlabel = errorlabel;
        history = xhistory;
        history.setRenderer(new ComboBoxRendererForTooltip());
        tabpanePanel.add(new JScrollPane(getTabPane()), BorderLayout.CENTER);

        favMenu = favouritesMenu;
        reloadFavorites();
        identifierView = this;
        filedialog = new DialogForFile(DialogForFile.FILES_ONLY);
//        jMenuItem24.setEnabled(!LocalSettings.getBooleanProperty(LocalSettings.OFFICE_REMOTE));

        nav_outlookbar.setAnimated(navBarAnimated);
        getFrame().addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent event) {
                setNaviPanelSize();
            }
        });

        //build bigdrop Button
        ActionListener l = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addTab(new ItemPanel2(Context.getInvoice(), Item.TYPE_INVOICE), Messages.NEW_INVOICE);
            }
        };
        ddb1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/32/folder_blue.png")));
        ddb1.setLabel(Item.getTypeString(Item.TYPE_INVOICE));
        ddb1.addActionListener(l);

        ddb1.add(
                new javax.swing.ImageIcon(getClass().getResource("/images/32/folder_blue.png")),
                Item.getTypeString(Item.TYPE_DEPOSIT),
                new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addTab(new ItemPanel2(Context.getDeposit(), Item.TYPE_DEPOSIT), Messages.NEW_DEPOSIT);
            }
        });
        ddb1.add(
                new javax.swing.ImageIcon(getClass().getResource("/images/32/folder_blue.png")),
                Item.getTypeString(Item.TYPE_PART_PAYMENT),
                new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addTab(new ItemPanel2(Context.getPartPayment(), Item.TYPE_PART_PAYMENT), Messages.NEW_PART_PAYMENT);
            }
        });

        if (predefTitle != null) {
            identifierFrame.setTitle(identifierFrame.getTitle() + predefTitle);
        }

        QueryHandler.setWaitCursorFor(identifierFrame);

        pluginLoader = new YabsPluginLoader();

        addTab(new StartPage(), Messages.WELCOME);
        LocalSettings.save();

        if (Log.getLoglevel() == Log.LOGLEVEL_DEBUG) {
//            jMenuItem45.setEnabled(true);
        }

        jMenuItem41.setEnabled(false);//not yet implemented
        identifierFrame.validate();
        BINDING_VARS = new HashMap<String, Object>();
        BINDING_VARS.put("MPView", this);
        BINDING_VARS.put("Application", getApplication());
        BINDING_VARS.put("QueryHandler", QueryHandler.instanceOf());
        BINDING_VARS.put("tabPane", tabPane);
        BINDING_VARS.put("dbo", DatabaseObject.getObject(Context.getContact()));//just a dummy to get a hold on the dbo class

        enhanceToolbars();
        loaded = true;
        String fs = System.getProperty("yabs_firststart");
        if (fs != null) {
            wizard_FirstSettings1.build(getIdentifierFrame());
        }else{
             if(Constants.VERSION.equals(System.getProperty("yabsFirstStartVersion"))){
                 try {
                     if (LanguageManager.checkAndReplaceLangFiles()) {
                         Notificator.raiseNotification(Messages.LANGUAGE_UPDATED, true);
                     } else {
                         Log.Debug(this, "Language file up to date");
                     }
                 } catch (Exception e) {
                     Log.Debug(e);
                 }
             }
        }
    }

    private GroovyShell getGroovyShell() {
        synchronized (this) {
            if (groovyShell == null) {
                Binding binding = new Binding(BINDING_VARS);
                groovyShell = new GroovyShell(binding);
            }
            return groovyShell;
        }
    }

    /**
     * Set the state of the main toolbar
     *
     * @param enable
     */
    public void enableToolBar(boolean enable) {
        getMainToolbar().setEnabled(enable);
    }

    private void setNaviPanelSize() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                getNav_outlookbar().setPreferredSize(new Dimension(getNav_outlookbar().getWidth(), getFrame().getHeight() - 200));
                getNav_outlookbar().setMaximumSize(new Dimension(getNav_outlookbar().getWidth(), getFrame().getHeight() - 200));
                getNav_outlookbar().setMinimumSize(new Dimension(getNav_outlookbar().getWidth(), getFrame().getHeight() - 200));
                getNav_outlookbar().setSize(new Dimension(getNav_outlookbar().getWidth(), getFrame().getHeight() - 200));
                getNaviPanel().revalidate();
                getNaviPanel().repaint();
            }
        };

        SwingUtilities.invokeLater(runnable);
    }

    /**
     * Add a tab to the main tab pane, automatically determines the needed View
     *
     * @param item
     * @param tabTitle
     * @return The added tab
     */
    @Override
    public DataPanel addTab(final DatabaseObject item, Object tabTitle) {
        return addTab(item, tabTitle, false);
    }

    private DataPanel addTab(final DatabaseObject item, Object tabTitle, boolean forceNew) {
        setWaiting(true);
        boolean found = false;
        boolean proceed = true;
        if (!forceNew && mpv5.db.objects.User.getCurrentUser().getProperties().getProperty("org.openyabs.uiproperty", "avoidmultipleviews")) {
            Log.Debug(this, "Looking for an existing view for: " + item);
            int count = getTabPane().getTabCount();
            for (int i = 0; i < count; i++) {
                if (getTabAt(i) != null) {
                    DataPanel panel = getTabAt(i);
                    if (item.equals(panel.getDataOwner())) {
                        getTabPane().setSelectedIndex(i);
                        if (tabTitle == null) {
                            getTabPane().setTitleAt(i, item.__getCname());
                        } else {
                            getTabPane().setTitleAt(i, tabTitle + ": " + item.__getCname());
                        }
                        proceed = false;
                        break;
                    }
                }
            }
        }

        if (proceed) {
            if (item.hasView() && (forceNew || mpv5.db.objects.User.getCurrentUser().getProperties().getProperty("org.openyabs.uiproperty", "norecycletabs"))) {
                if (tabTitle == null) {
                    final DataPanel p = ((DataPanel) item.getView());
                    addTab((JComponent) p, item.__getCname());
                    Runnable runnable = new Runnable() {
                        @Override
                        public void run() {
                            p.setDataOwner(item, true);
                        }
                    };
                    SwingUtilities.invokeLater(runnable);
                } else {
                    final DataPanel p = ((DataPanel) item.getView());
                    addTab((JComponent) p, tabTitle + ": " + item.__getCname());
                    Runnable runnable = new Runnable() {
                        @Override
                        public void run() {
                            p.setDataOwner(item, true);
                        }
                    };
                    SwingUtilities.invokeLater(runnable);
                }
                getCurrentTab().setDataOwner(item, true);
            } else if (item.hasView()) {
                int count = getTabPane().getTabCount();
                for (int i = 0; i < count; i++) {
                    if (getTabAt(i) != null) {
                        DataPanel panel = getTabAt(i);
                        if (panel.getDataOwner() != null && !panel.getDataOwner().isExisting()
                                && panel.getDataOwner().getContext().equals(item.getContext())
                                && panel.getClass() == item.getView().getClass()) {
//                        if (!panel.getDataOwner().isExisting() && panel.getDataOwner().getContext().equals(item.getContext())) {
                            getTabPane().setSelectedIndex(i);
                            panel.setDataOwner(item, true);
                            if (tabTitle == null) {
                                getTabPane().setTitleAt(i, item.__getCname());
                            } else {
                                getTabPane().setTitleAt(i, tabTitle + ": " + item.__getCname());
                            }
                            found = true;
                            break;
                        }
                    }
                }
                if (!found) {
                    try {
                        final DataPanel p = (DataPanel) item.getView();

                        if (tabTitle == null) {
                            addTab((JComponent) p, item.__getCname());
                        } else {
                            addTab((JComponent) p, tabTitle + ": " + item.__getCname());
                        }
                        Runnable runnable = new Runnable() {
                            @Override
                            public void run() {
                                p.setDataOwner(item, true);
                            }
                        };
                        SwingUtilities.invokeLater(runnable);
                    } catch (ClassCastException e) {
                        if (tabTitle == null) {
                            addTab(item.getView(), item.__getCname());
                        } else {
                            addTab(item.getView(), tabTitle + ": " + item.__getCname());
                        }
                    }
                }
            }
        }

        setPointInFlow(addToFlow(item));
        setWaiting(false);
        return getCurrentTab();
    }

    /**
     * Add a tab to the main tab pane, automatically determines the needed View
     *
     * @param item
     * @return
     */
    @Override
    public DataPanel addTab(DatabaseObject item) {
        return addTab(item, null);
    }

    @Override
    public DataPanel addTab(DatabaseObject object, boolean forceNew) {
        return addTab(object, null, forceNew);
    }

    /**
     * Shows a tab or adds it if needed
     *
     * @param instanceOf
     * @param label
     */
    @Override
    public void addOrShowTab(JComponent instanceOf, Object label) {
        Component[] tabs = getTabPane().getComponents();
        boolean found = false;
        JScrollPane scroll = null;
        for (int i = 0; i < tabs.length; i++) {
            JComponent component = (JComponent) tabs[i];
            if (component instanceof JScrollPane) {
                scroll = (JScrollPane) component;
                component = (JComponent) ((JScrollPane) component).getComponent(0);
            }
            try {
                if (component.getComponent(0).equals(instanceOf)) {
                    if (scroll == null) {
                        getTabPane().setSelectedComponent(instanceOf);
                    } else {
                        getTabPane().setSelectedComponent(scroll);
                    }
                    found = true;
                }
            } catch (Exception e) {
                Log.Debug(this, e.getMessage());
            }
        }

        if (!found) {
            addTab(instanceOf, label.toString());
        }
    }

    /**
     * Add a tab to the main tab pane, with new JScrollPane
     *
     * @param tab
     * @param name
     * @return The now selected index
     */
    @Override
    public int addTab(JComponent tab, String name) {
        JScrollPane spane = new JScrollPane(tab);
        getTabPane().addTab(name, spane);
        getTabPane().setSelectedComponent(spane);
        return getTabPane().getSelectedIndex();
    }

    private void addTab(JComponent tab, Messages name) {
        addTab(tab, name.toString());
    }

    private void importXML() {
        Wizard w = new Wizard(false);
        w.addPanel(new wizard_XMLImport_1(w));
        w.addPanel(new wizard_XMLImport_2(w));
        w.showWiz();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mainPanel = new javax.swing.JPanel();
        naviPanel = new javax.swing.JPanel();
        nav_outlookbar = new com.l2fprod.common.swing.JOutlookBar();
        parent_nav_simple = new javax.swing.JPanel();
        nav_simple_inner = new javax.swing.JPanel();
        scustomers = new javax.swing.JButton();
        sinvoices = new javax.swing.JButton();
        sproducts = new javax.swing.JButton();
        sjournal = new javax.swing.JButton();
        parent_nav_contacts = new javax.swing.JPanel();
        nav_contacts = new javax.swing.JPanel();
        jButton5 = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();
        jButton18 = new javax.swing.JButton();
        parent_nav_accounting = new javax.swing.JPanel();
        nav_accounting = new javax.swing.JPanel();
        jButton10 = new javax.swing.JButton();
        jButton11 = new javax.swing.JButton();
        ddb1 = new mpv5.ui.beans.BigDropDownButton(
        );
        jButton21 = new javax.swing.JButton();
        jButton30 = new javax.swing.JButton();
        jButton15 = new javax.swing.JButton();
        jButton16 = new javax.swing.JButton();
        jButton17 = new javax.swing.JButton();
        jButton23 = new javax.swing.JButton();
        parent_nav_products = new javax.swing.JPanel();
        nav_products = new javax.swing.JPanel();
        jButton13 = new javax.swing.JButton();
        jButton14 = new javax.swing.JButton();
        jButton19 = new javax.swing.JButton();
        jButton20 = new javax.swing.JButton();
        jButton12 = new javax.swing.JButton();
        jButton22 = new javax.swing.JButton();
        parent_nav_extras = new javax.swing.JPanel();
        nav_extras = new javax.swing.JPanel();
        jButton6 = new javax.swing.JButton();
        jButton7 = new javax.swing.JButton();
        jButton9 = new javax.swing.JButton();
        menuBar = new javax.swing.JMenuBar();
        javax.swing.JMenu fileMenu = new javax.swing.JMenu();
        jMenu14 = new javax.swing.JMenu();
        jMenuItem33 = new javax.swing.JMenuItem();
        jMenuItem34 = new javax.swing.JMenuItem();
        jMenuItem13 = new javax.swing.JMenuItem();
        jMenuItem14 = new javax.swing.JMenuItem();
        jSeparator3 = new javax.swing.JSeparator();
        jMenuItem6 = new javax.swing.JMenuItem();
        jMenu4 = new javax.swing.JMenu();
        jMenu5 = new javax.swing.JMenu();
        jMenuItem8 = new javax.swing.JMenuItem();
        jMenuItem22 = new javax.swing.JMenuItem();
        jMenu10 = new javax.swing.JMenu();
        jMenuItem20 = new javax.swing.JMenuItem();
        jMenuItem21 = new javax.swing.JMenuItem();
        jMenu11 = new javax.swing.JMenu();
        jMenuItem26 = new javax.swing.JMenuItem();
        jMenu13 = new javax.swing.JMenu();
        jMenuItem32 = new javax.swing.JMenuItem();
        jMenuItem41 = new javax.swing.JMenuItem();
        jSeparator8 = new javax.swing.JPopupMenu.Separator();
        jMenu8 = new javax.swing.JMenu();
        jMenuItem11 = new javax.swing.JMenuItem();
        jMenuItem38 = new javax.swing.JMenuItem();
        jMenuItem40 = new javax.swing.JMenuItem();
        jMenu12 = new javax.swing.JMenu();
        jMenuItem28 = new javax.swing.JMenuItem();
        jMenuItem29 = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JSeparator();
        jMenuItem2 = new javax.swing.JMenuItem();
        jSeparator9 = new javax.swing.JPopupMenu.Separator();
        jMenuItem12 = new javax.swing.JMenuItem();
        editMenu = new javax.swing.JMenu();
        jMenuItem15 = new javax.swing.JMenuItem();
        jSeparator4 = new javax.swing.JSeparator();
        jMenuItem9 = new javax.swing.JMenuItem();
        jMenuItem16 = new javax.swing.JMenuItem();
        jSeparator5 = new javax.swing.JSeparator();
        jMenuItem17 = new javax.swing.JMenuItem();
        viewMenu = new javax.swing.JMenu();
        jMenu2 = new javax.swing.JMenu();
        jMenuItem1 = new javax.swing.JMenuItem();
        jMenuItem39 = new javax.swing.JMenuItem();
        jMenuItem19 = new javax.swing.JMenuItem();
        jMenuItem4 = new javax.swing.JMenuItem();
        jMenu15 = new javax.swing.JMenu();
        jMenuItem35 = new javax.swing.JMenuItem();
        jMenuItem36 = new javax.swing.JMenuItem();
        jMenuItem49 = new javax.swing.JMenuItem();
        simplenav = new javax.swing.JMenuItem();
        toolsMenu = new javax.swing.JMenu();
        jMenuItem3 = new javax.swing.JMenuItem();
        jMenuItem23 = new javax.swing.JMenuItem();
        jMenuItem31 = new javax.swing.JMenuItem();
        jMenu3 = new javax.swing.JMenu();
        jMenuItem18 = new javax.swing.JMenuItem();
        jMenuItem37 = new javax.swing.JMenuItem();
        jMenuItem46 = new javax.swing.JMenuItem();
        jSeparator6 = new javax.swing.JSeparator();
        jMenuItem43 = new javax.swing.JMenuItem();
        jMenuItem45 = new javax.swing.JMenuItem();
        jMenuItem48 = new javax.swing.JMenuItem();
        jMenu1 = new javax.swing.JMenu();
        jMenuItem42 = new javax.swing.JMenuItem();
        jMenuItem50 = new javax.swing.JMenuItem();
        jMenuItem44 = new javax.swing.JMenuItem();
        favouritesMenu = new javax.swing.JMenu();
        clipboardMenu = new javax.swing.JMenu();
        jMenu6 = new javax.swing.JMenu();
        jMenuItem7 = new javax.swing.JMenuItem();
        jMenuItem5 = new javax.swing.JMenuItem();
        helpmenu = new javax.swing.JMenu();
        jMenuItem10 = new javax.swing.JMenuItem();
        jMenuItem27 = new javax.swing.JMenuItem();
        jMenuItem30 = new javax.swing.JMenuItem();
        statusPanel = new javax.swing.JPanel();
        statusMessageLabel =   new FadeOnChangeLabel();
        pluginIcons = new javax.swing.JPanel();
        officelabel = new javax.swing.JPanel();
        serverlabel = new javax.swing.JPanel();
        separator = new javax.swing.JPanel();
        progressBar = new javax.swing.JProgressBar();
        separator1 = new javax.swing.JPanel();
        xhistory = new javax.swing.JComboBox();
        errorlabel = new javax.swing.JLabel();
        mainToolbar = new javax.swing.JToolBar();
        closeButton1 = new javax.swing.JButton();
        closeButton2 = new javax.swing.JButton();
        jSeparator7 = new javax.swing.JToolBar.Separator();
        closeButton = new javax.swing.JButton();
        lockButton = new javax.swing.JButton();
        jSeparator2 = new javax.swing.JToolBar.Separator();
        calculatorButton = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();

        mainPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        mainPanel.setName("mainPanel"); // NOI18N

        tabpanePanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        tabpanePanel.setName("tabpanePanel"); // NOI18N
        tabpanePanel.setLayout(new java.awt.BorderLayout());

        naviPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        naviPanel.setAutoscrolls(true);
        naviPanel.setFocusCycleRoot(true);
        naviPanel.setName("naviPanel"); // NOI18N

        nav_outlookbar.setAnimated(false);
        nav_outlookbar.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        nav_outlookbar.setName("nav_outlookbar"); // NOI18N

        parent_nav_simple.setName("parent_nav_simple"); // NOI18N
        parent_nav_simple.setPreferredSize(new java.awt.Dimension(110, 400));
        parent_nav_simple.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 0, 0));

        nav_simple_inner.setName("nav_simple_inner"); // NOI18N
        nav_simple_inner.setLayout(new java.awt.GridLayout(0, 1, 2, 5));

        scustomers.setFont(scustomers.getFont().deriveFont(scustomers.getFont().getSize()-1f));
        scustomers.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/32/agt_family.png"))); // NOI18N
        java.util.ResourceBundle bundle = mpv5.i18n.LanguageManager.getBundle(); // NOI18N
        scustomers.setText(bundle.getString("MPView.scustomers.text")); // NOI18N
        scustomers.setToolTipText(bundle.getString("MPView.scustomers.toolTipText")); // NOI18N
        scustomers.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        scustomers.setMaximumSize(new java.awt.Dimension(250, 60));
        scustomers.setMinimumSize(new java.awt.Dimension(80, 50));
        scustomers.setName("scustomers"); // NOI18N
        scustomers.setPreferredSize(new java.awt.Dimension(110, 57));
        scustomers.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        scustomers.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                scustomersActionPerformed(evt);
            }
        });
        nav_simple_inner.add(scustomers);

        sinvoices.setFont(sinvoices.getFont().deriveFont(sinvoices.getFont().getSize()-1f));
        sinvoices.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/32/abiword.png"))); // NOI18N
        sinvoices.setText(bundle.getString("MPView.sinvoices.text_1")); // NOI18N
        sinvoices.setToolTipText(bundle.getString("MPView.sinvoices.toolTipText_1")); // NOI18N
        sinvoices.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        sinvoices.setMaximumSize(new java.awt.Dimension(250, 60));
        sinvoices.setMinimumSize(new java.awt.Dimension(80, 50));
        sinvoices.setName("sinvoices"); // NOI18N
        sinvoices.setPreferredSize(new java.awt.Dimension(110, 57));
        sinvoices.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        sinvoices.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sinvoicesActionPerformed(evt);
            }
        });
        nav_simple_inner.add(sinvoices);

        sproducts.setFont(sproducts.getFont().deriveFont(sproducts.getFont().getSize()-1f));
        sproducts.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/32/warehause.png"))); // NOI18N
        sproducts.setText(bundle.getString("MPView.sproducts.text_1")); // NOI18N
        sproducts.setToolTipText(bundle.getString("MPView.sproducts.toolTipText_1")); // NOI18N
        sproducts.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        sproducts.setMaximumSize(new java.awt.Dimension(250, 60));
        sproducts.setMinimumSize(new java.awt.Dimension(80, 50));
        sproducts.setName("sproducts"); // NOI18N
        sproducts.setPreferredSize(new java.awt.Dimension(110, 57));
        sproducts.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        sproducts.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sproductsActionPerformed(evt);
            }
        });
        nav_simple_inner.add(sproducts);

        sjournal.setFont(sjournal.getFont().deriveFont(sjournal.getFont().getSize()-1f));
        sjournal.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/32/reload.png"))); // NOI18N
        sjournal.setText(bundle.getString("MPView.sjournal.text_1")); // NOI18N
        sjournal.setToolTipText(bundle.getString("MPView.sjournal.toolTipText_1")); // NOI18N
        sjournal.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        sjournal.setMaximumSize(new java.awt.Dimension(250, 60));
        sjournal.setMinimumSize(new java.awt.Dimension(80, 50));
        sjournal.setName("sjournal"); // NOI18N
        sjournal.setPreferredSize(new java.awt.Dimension(110, 57));
        sjournal.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        sjournal.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sjournalActionPerformed(evt);
            }
        });
        nav_simple_inner.add(sjournal);

        parent_nav_simple.add(nav_simple_inner);

        nav_outlookbar.addTab(bundle.getString("MPView.parent_nav_simple.TabConstraints.tabTitle"), parent_nav_simple); // NOI18N

        parent_nav_contacts.setName("parent_nav_contacts"); // NOI18N
        parent_nav_contacts.setPreferredSize(new java.awt.Dimension(110, 400));
        parent_nav_contacts.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 0, 0));

        nav_contacts.setName("nav_contacts"); // NOI18N
        nav_contacts.setLayout(new java.awt.GridLayout(0, 1, 2, 5));

        jButton5.setFont(jButton5.getFont().deriveFont(jButton5.getFont().getSize()-1f));
        jButton5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/32/agt_family.png"))); // NOI18N
        jButton5.setText(bundle.getString("MPView.jButton5.text_1")); // NOI18N
        jButton5.setToolTipText(bundle.getString("MPView.jButton5.toolTipText_1")); // NOI18N
        jButton5.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton5.setMaximumSize(new java.awt.Dimension(250, 60));
        jButton5.setMinimumSize(new java.awt.Dimension(80, 50));
        jButton5.setName("jButton5"); // NOI18N
        jButton5.setPreferredSize(new java.awt.Dimension(110, 57));
        jButton5.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton5ActionPerformed(evt);
            }
        });
        nav_contacts.add(jButton5);

        jButton1.setFont(jButton1.getFont().deriveFont(jButton1.getFont().getSize()-1f));
        jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/32/edit_group.png"))); // NOI18N
        jButton1.setText(bundle.getString("MPView.jButton1.text_1")); // NOI18N
        jButton1.setToolTipText(bundle.getString("MPView.jButton1.toolTipText_1")); // NOI18N
        jButton1.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton1.setMaximumSize(new java.awt.Dimension(250, 60));
        jButton1.setMinimumSize(new java.awt.Dimension(80, 50));
        jButton1.setName("jButton1"); // NOI18N
        jButton1.setPreferredSize(new java.awt.Dimension(110, 57));
        jButton1.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        nav_contacts.add(jButton1);

        jButton18.setFont(jButton18.getFont().deriveFont(jButton18.getFont().getSize()-1f));
        jButton18.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/32/edit_group.png"))); // NOI18N
        jButton18.setText(bundle.getString("MPView.jButton18.text_1")); // NOI18N
        jButton18.setToolTipText(bundle.getString("MPView.jButton18.toolTipText_1")); // NOI18N
        jButton18.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton18.setMaximumSize(new java.awt.Dimension(250, 60));
        jButton18.setMinimumSize(new java.awt.Dimension(80, 50));
        jButton18.setName("jButton18"); // NOI18N
        jButton18.setPreferredSize(new java.awt.Dimension(110, 57));
        jButton18.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton18.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton18ActionPerformed(evt);
            }
        });
        nav_contacts.add(jButton18);

        parent_nav_contacts.add(nav_contacts);

        nav_outlookbar.addTab(bundle.getString("MPView.parent_nav_contacts.TabConstraints.tabTitle_1"), parent_nav_contacts); // NOI18N

        parent_nav_accounting.setAutoscrolls(true);
        parent_nav_accounting.setName("parent_nav_accounting"); // NOI18N
        parent_nav_accounting.setPreferredSize(new java.awt.Dimension(110, 400));
        parent_nav_accounting.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 0, 0));

        nav_accounting.setAutoscrolls(true);
        nav_accounting.setName("nav_accounting"); // NOI18N
        nav_accounting.setLayout(new java.awt.GridLayout(0, 1, 2, 5));

        jButton10.setFont(jButton10.getFont().deriveFont(jButton10.getFont().getSize()-1f));
        jButton10.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/32/folder_grey.png"))); // NOI18N
        jButton10.setText(bundle.getString("MPView.jButton10.text")); // NOI18N
        jButton10.setToolTipText(bundle.getString("MPView.jButton10.toolTipText")); // NOI18N
        jButton10.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton10.setMaximumSize(new java.awt.Dimension(250, 60));
        jButton10.setMinimumSize(new java.awt.Dimension(80, 50));
        jButton10.setName("jButton10"); // NOI18N
        jButton10.setPreferredSize(new java.awt.Dimension(110, 57));
        jButton10.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton10.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton10ActionPerformed(evt);
            }
        });
        nav_accounting.add(jButton10);

        jButton11.setFont(jButton11.getFont().deriveFont(jButton11.getFont().getSize()-1f));
        jButton11.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/32/folder_green.png"))); // NOI18N
        jButton11.setText(bundle.getString("MPView.jButton11.text")); // NOI18N
        jButton11.setToolTipText(bundle.getString("MPView.jButton11.toolTipText")); // NOI18N
        jButton11.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton11.setMaximumSize(new java.awt.Dimension(250, 60));
        jButton11.setMinimumSize(new java.awt.Dimension(80, 50));
        jButton11.setName("jButton11"); // NOI18N
        jButton11.setPreferredSize(new java.awt.Dimension(110, 57));
        jButton11.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton11.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton11ActionPerformed(evt);
            }
        });
        nav_accounting.add(jButton11);

        ddb1.setName("ddb1"); // NOI18N
        nav_accounting.add(ddb1);

        jButton21.setFont(jButton21.getFont().deriveFont(jButton21.getFont().getSize()-1f));
        jButton21.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/32/kdisknav.png"))); // NOI18N
        jButton21.setText(bundle.getString("MPView.jButton21.text")); // NOI18N
        jButton21.setToolTipText(bundle.getString("MPView.jButton21.toolTipText")); // NOI18N
        jButton21.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton21.setMaximumSize(new java.awt.Dimension(250, 60));
        jButton21.setMinimumSize(new java.awt.Dimension(80, 50));
        jButton21.setName("jButton21"); // NOI18N
        jButton21.setPreferredSize(new java.awt.Dimension(110, 57));
        jButton21.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton21.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton21ActionPerformed(evt);
            }
        });
        nav_accounting.add(jButton21);

        jButton30.setFont(jButton30.getFont().deriveFont(jButton30.getFont().getSize()-1f));
        jButton30.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/32/folder_locked.png"))); // NOI18N
        jButton30.setText(bundle.getString("MPView.jButton30.text")); // NOI18N
        jButton30.setToolTipText(bundle.getString("MPView.jButton30.toolTipText")); // NOI18N
        jButton30.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton30.setMaximumSize(new java.awt.Dimension(250, 60));
        jButton30.setMinimumSize(new java.awt.Dimension(80, 50));
        jButton30.setName("jButton30"); // NOI18N
        jButton30.setPreferredSize(new java.awt.Dimension(110, 57));
        jButton30.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton30.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton30ActionPerformed(evt);
            }
        });
        nav_accounting.add(jButton30);

        jButton15.setFont(jButton15.getFont().deriveFont(jButton15.getFont().getSize()-1f));
        jButton15.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/32/folder_red.png"))); // NOI18N
        jButton15.setText(bundle.getString("MPView.jButton15.text")); // NOI18N
        jButton15.setToolTipText(bundle.getString("MPView.jButton15.toolTipText")); // NOI18N
        jButton15.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton15.setMaximumSize(new java.awt.Dimension(250, 60));
        jButton15.setMinimumSize(new java.awt.Dimension(80, 50));
        jButton15.setName("jButton15"); // NOI18N
        jButton15.setPreferredSize(new java.awt.Dimension(110, 57));
        jButton15.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton15.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton15ActionPerformed(evt);
            }
        });
        nav_accounting.add(jButton15);

        jButton16.setFont(jButton16.getFont().deriveFont(jButton16.getFont().getSize()-1f));
        jButton16.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/32/folder_yellow.png"))); // NOI18N
        jButton16.setText(bundle.getString("MPView.jButton16.text")); // NOI18N
        jButton16.setToolTipText(bundle.getString("MPView.jButton16.toolTipText")); // NOI18N
        jButton16.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton16.setMaximumSize(new java.awt.Dimension(250, 60));
        jButton16.setMinimumSize(new java.awt.Dimension(80, 50));
        jButton16.setName("jButton16"); // NOI18N
        jButton16.setPreferredSize(new java.awt.Dimension(110, 57));
        jButton16.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton16.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton16ActionPerformed(evt);
            }
        });
        nav_accounting.add(jButton16);

        jButton17.setFont(jButton17.getFont().deriveFont(jButton17.getFont().getSize()-1f));
        jButton17.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/32/reload.png"))); // NOI18N
        jButton17.setText(bundle.getString("MPView.jButton17.text")); // NOI18N
        jButton17.setToolTipText(bundle.getString("MPView.jButton17.toolTipText")); // NOI18N
        jButton17.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton17.setMaximumSize(new java.awt.Dimension(250, 60));
        jButton17.setMinimumSize(new java.awt.Dimension(80, 50));
        jButton17.setName("jButton17"); // NOI18N
        jButton17.setPreferredSize(new java.awt.Dimension(110, 57));
        jButton17.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton17.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton17ActionPerformed(evt);
            }
        });
        nav_accounting.add(jButton17);

        jButton23.setFont(jButton23.getFont().deriveFont(jButton23.getFont().getSize()-1f));
        jButton23.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/32/reload.png"))); // NOI18N
        jButton23.setText(bundle.getString("MPView.jButton23.text")); // NOI18N
        jButton23.setToolTipText(bundle.getString("MPView.jButton23.toolTipText")); // NOI18N
        jButton23.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton23.setMaximumSize(new java.awt.Dimension(250, 60));
        jButton23.setMinimumSize(new java.awt.Dimension(80, 50));
        jButton23.setName("jButton23"); // NOI18N
        jButton23.setPreferredSize(new java.awt.Dimension(110, 57));
        jButton23.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton23.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton23ActionPerformed(evt);
            }
        });
        nav_accounting.add(jButton23);

        parent_nav_accounting.add(nav_accounting);

        nav_outlookbar.addTab(bundle.getString("MPView.parent_nav_accounting.TabConstraints.tabTitle_1"), parent_nav_accounting); // NOI18N

        parent_nav_products.setName("parent_nav_products"); // NOI18N
        parent_nav_products.setPreferredSize(new java.awt.Dimension(110, 400));
        parent_nav_products.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 0, 0));

        nav_products.setName("nav_products"); // NOI18N
        nav_products.setLayout(new java.awt.GridLayout(0, 1, 2, 5));

        jButton13.setFont(jButton13.getFont().deriveFont(jButton13.getFont().getSize()-1f));
        jButton13.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/32/tux.png"))); // NOI18N
        jButton13.setText(bundle.getString("MPView.jButton13.text")); // NOI18N
        jButton13.setToolTipText(bundle.getString("MPView.jButton13.toolTipText")); // NOI18N
        jButton13.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton13.setMaximumSize(new java.awt.Dimension(250, 60));
        jButton13.setMinimumSize(new java.awt.Dimension(80, 50));
        jButton13.setName("jButton13"); // NOI18N
        jButton13.setPreferredSize(new java.awt.Dimension(110, 57));
        jButton13.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton13.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton13ActionPerformed(evt);
            }
        });
        nav_products.add(jButton13);

        jButton14.setFont(jButton14.getFont().deriveFont(jButton14.getFont().getSize()-1f));
        jButton14.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/32/playlist.png"))); // NOI18N
        jButton14.setText(bundle.getString("MPView.jButton14.text")); // NOI18N
        jButton14.setToolTipText(bundle.getString("MPView.jButton14.toolTipText")); // NOI18N
        jButton14.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton14.setMaximumSize(new java.awt.Dimension(250, 60));
        jButton14.setMinimumSize(new java.awt.Dimension(80, 50));
        jButton14.setName("jButton14"); // NOI18N
        jButton14.setPreferredSize(new java.awt.Dimension(110, 57));
        jButton14.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton14.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton14ActionPerformed(evt);
            }
        });
        nav_products.add(jButton14);

        jButton19.setFont(jButton19.getFont().deriveFont(jButton19.getFont().getSize()-1f));
        jButton19.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/32/kghostview.png"))); // NOI18N
        jButton19.setText(bundle.getString("MPView.jButton19.text")); // NOI18N
        jButton19.setToolTipText(bundle.getString("MPView.jButton19.toolTipText")); // NOI18N
        jButton19.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton19.setMaximumSize(new java.awt.Dimension(250, 60));
        jButton19.setMinimumSize(new java.awt.Dimension(80, 50));
        jButton19.setName("jButton19"); // NOI18N
        jButton19.setPreferredSize(new java.awt.Dimension(110, 57));
        jButton19.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton19.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton19ActionPerformed(evt);
            }
        });
        nav_products.add(jButton19);

        jButton20.setFont(jButton20.getFont().deriveFont(jButton20.getFont().getSize()-1f));
        jButton20.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/32/window_list.png"))); // NOI18N
        jButton20.setText(bundle.getString("MPView.jButton20.text")); // NOI18N
        jButton20.setToolTipText(bundle.getString("MPView.jButton20.toolTipText")); // NOI18N
        jButton20.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton20.setMaximumSize(new java.awt.Dimension(250, 60));
        jButton20.setMinimumSize(new java.awt.Dimension(80, 50));
        jButton20.setName("jButton20"); // NOI18N
        jButton20.setPreferredSize(new java.awt.Dimension(110, 57));
        jButton20.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton20.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton20ActionPerformed(evt);
            }
        });
        nav_products.add(jButton20);

        jButton12.setFont(jButton12.getFont().deriveFont(jButton12.getFont().getSize()-1f));
        jButton12.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/32/gear.png"))); // NOI18N
        jButton12.setText(bundle.getString("MPView.jButton12.text")); // NOI18N
        jButton12.setToolTipText(bundle.getString("MPView.jButton12.toolTipText")); // NOI18N
        jButton12.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton12.setMaximumSize(new java.awt.Dimension(250, 60));
        jButton12.setMinimumSize(new java.awt.Dimension(80, 50));
        jButton12.setName("jButton12"); // NOI18N
        jButton12.setPreferredSize(new java.awt.Dimension(110, 57));
        jButton12.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton12.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton12ActionPerformed(evt);
            }
        });
        nav_products.add(jButton12);

        jButton22.setFont(jButton22.getFont().deriveFont(jButton22.getFont().getSize()-1f));
        jButton22.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/32/readme.png"))); // NOI18N
        jButton22.setText(bundle.getString("MPView.jButton22.text")); // NOI18N
        jButton22.setToolTipText(bundle.getString("MPView.jButton22.toolTipText")); // NOI18N
        jButton22.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton22.setMaximumSize(new java.awt.Dimension(250, 60));
        jButton22.setMinimumSize(new java.awt.Dimension(80, 50));
        jButton22.setName("jButton22"); // NOI18N
        jButton22.setPreferredSize(new java.awt.Dimension(110, 57));
        jButton22.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton22.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton22ActionPerformed(evt);
            }
        });
        nav_products.add(jButton22);

        parent_nav_products.add(nav_products);

        nav_outlookbar.addTab(bundle.getString("MPView.parent_nav_products.TabConstraints.tabTitle_1"), parent_nav_products); // NOI18N

        parent_nav_extras.setName("parent_nav_extras"); // NOI18N
        parent_nav_extras.setPreferredSize(new java.awt.Dimension(110, 400));
        parent_nav_extras.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 0, 0));

        nav_extras.setName("nav_extras"); // NOI18N
        nav_extras.setLayout(new java.awt.GridLayout(0, 1, 2, 5));

        jButton6.setFont(jButton6.getFont().deriveFont(jButton6.getFont().getSize()-1f));
        jButton6.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/32/kservices.png"))); // NOI18N
        jButton6.setText(bundle.getString("MPView.jButton6.text")); // NOI18N
        jButton6.setToolTipText(bundle.getString("MPView.jButton6.toolTipText")); // NOI18N
        jButton6.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton6.setMaximumSize(new java.awt.Dimension(250, 60));
        jButton6.setMinimumSize(new java.awt.Dimension(80, 50));
        jButton6.setName("jButton6"); // NOI18N
        jButton6.setPreferredSize(new java.awt.Dimension(110, 57));
        jButton6.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton6ActionPerformed(evt);
            }
        });
        nav_extras.add(jButton6);

        jButton7.setFont(jButton7.getFont().deriveFont(jButton7.getFont().getSize()-1f));
        jButton7.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/32/edittrash.png"))); // NOI18N
        jButton7.setText(bundle.getString("MPView.jButton7.text")); // NOI18N
        jButton7.setToolTipText(bundle.getString("MPView.jButton7.toolTipText")); // NOI18N
        jButton7.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton7.setMaximumSize(new java.awt.Dimension(250, 60));
        jButton7.setMinimumSize(new java.awt.Dimension(80, 50));
        jButton7.setName("jButton7"); // NOI18N
        jButton7.setPreferredSize(new java.awt.Dimension(110, 57));
        jButton7.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton7ActionPerformed(evt);
            }
        });
        nav_extras.add(jButton7);

        jButton9.setFont(jButton9.getFont().deriveFont(jButton9.getFont().getSize()-1f));
        jButton9.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/32/folder_txt.png"))); // NOI18N
        jButton9.setText(bundle.getString("MPView.jButton9.text")); // NOI18N
        jButton9.setToolTipText(bundle.getString("MPView.jButton9.toolTipText")); // NOI18N
        jButton9.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton9.setMaximumSize(new java.awt.Dimension(250, 60));
        jButton9.setMinimumSize(new java.awt.Dimension(80, 50));
        jButton9.setName("jButton9"); // NOI18N
        jButton9.setPreferredSize(new java.awt.Dimension(110, 57));
        jButton9.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton9.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton9ActionPerformed(evt);
            }
        });
        nav_extras.add(jButton9);
        jButton9.getAccessibleContext().setAccessibleDescription(bundle.getString("MPView.jButton9.AccessibleContext.accessibleDescription")); // NOI18N

        parent_nav_extras.add(nav_extras);

        nav_outlookbar.addTab(bundle.getString("MPView.parent_nav_extras.TabConstraints.tabTitle"), parent_nav_extras); // NOI18N

        javax.swing.GroupLayout naviPanelLayout = new javax.swing.GroupLayout(naviPanel);
        naviPanel.setLayout(naviPanelLayout);
        naviPanelLayout.setHorizontalGroup(
            naviPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(nav_outlookbar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        naviPanelLayout.setVerticalGroup(
            naviPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(naviPanelLayout.createSequentialGroup()
                .addComponent(nav_outlookbar, javax.swing.GroupLayout.PREFERRED_SIZE, 497, Short.MAX_VALUE)
                .addContainerGap())
        );

        javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addComponent(naviPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tabpanePanel, javax.swing.GroupLayout.DEFAULT_SIZE, 661, Short.MAX_VALUE))
        );
        mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(naviPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(tabpanePanel, javax.swing.GroupLayout.DEFAULT_SIZE, 512, Short.MAX_VALUE)
        );

        menuBar.setName("menuBar"); // NOI18N

        fileMenu.setText(bundle.getString("MPView.fileMenu.text_1")); // NOI18N
        fileMenu.setName("fileMenu"); // NOI18N

        jMenu14.setText(bundle.getString("MPView.jMenu14.text")); // NOI18N
        jMenu14.setName("jMenu14"); // NOI18N

        jMenuItem33.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem33.setText(bundle.getString("MPView.jMenuItem33.text")); // NOI18N
        jMenuItem33.setName("jMenuItem33"); // NOI18N
        jMenuItem33.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem33ActionPerformed(evt);
            }
        });
        jMenu14.add(jMenuItem33);

        jMenuItem34.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_I, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem34.setText(bundle.getString("MPView.jMenuItem34.text")); // NOI18N
        jMenuItem34.setName("jMenuItem34"); // NOI18N
        jMenuItem34.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem34ActionPerformed(evt);
            }
        });
        jMenu14.add(jMenuItem34);

        fileMenu.add(jMenu14);

        jMenuItem13.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem13.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/16/filesave.png"))); // NOI18N
        jMenuItem13.setText(bundle.getString("MPView.jMenuItem13.text")); // NOI18N
        jMenuItem13.setName("jMenuItem13"); // NOI18N
        jMenuItem13.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem13ActionPerformed(evt);
            }
        });
        fileMenu.add(jMenuItem13);

        jMenuItem14.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_N, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem14.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/16/filesaveas.png"))); // NOI18N
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
        jMenuItem6.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/22/printer1.png"))); // NOI18N
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

        jMenu5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/16/kontact_contacts.png"))); // NOI18N
        jMenu5.setText(bundle.getString("MPView.jMenu5.text")); // NOI18N
        jMenu5.setName("jMenu5"); // NOI18N

        jMenuItem8.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/16/xml.png"))); // NOI18N
        jMenuItem8.setText(bundle.getString("MPView.jMenuItem8.text")); // NOI18N
        jMenuItem8.setName("jMenuItem8"); // NOI18N
        jMenuItem8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem8ActionPerformed(evt);
            }
        });
        jMenu5.add(jMenuItem8);

        jMenuItem22.setText(bundle.getString("MPView.jMenuItem22.text")); // NOI18N
        jMenuItem22.setName("jMenuItem22"); // NOI18N
        jMenuItem22.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem22ActionPerformed(evt);
            }
        });
        jMenu5.add(jMenuItem22);

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

        jMenu13.setText(bundle.getString("MPView.jMenu13.text")); // NOI18N
        jMenu13.setName("jMenu13"); // NOI18N

        jMenuItem32.setText(bundle.getString("MPView.jMenuItem32.text")); // NOI18N
        jMenuItem32.setName("jMenuItem32"); // NOI18N
        jMenuItem32.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem32ActionPerformed(evt);
            }
        });
        jMenu13.add(jMenuItem32);

        jMenuItem41.setText(bundle.getString("MPView.jMenuItem41.text")); // NOI18N
        jMenuItem41.setName("jMenuItem41"); // NOI18N
        jMenuItem41.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem41ActionPerformed(evt);
            }
        });
        jMenu13.add(jMenuItem41);

        jMenu4.add(jMenu13);

        jSeparator8.setName("jSeparator8"); // NOI18N
        jMenu4.add(jSeparator8);

        fileMenu.add(jMenu4);

        jMenu8.setText(bundle.getString("MPView.jMenu8.text")); // NOI18N
        jMenu8.setName("jMenu8"); // NOI18N

        jMenuItem11.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/16/xml.png"))); // NOI18N
        jMenuItem11.setText(bundle.getString("MPView.jMenuItem11.text")); // NOI18N
        jMenuItem11.setName("jMenuItem11"); // NOI18N
        jMenuItem11.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem11ActionPerformed(evt);
            }
        });
        jMenu8.add(jMenuItem11);

        jMenuItem38.setText(bundle.getString("MPView.jMenuItem38.text")); // NOI18N
        jMenuItem38.setName("jMenuItem38"); // NOI18N
        jMenuItem38.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem38ActionPerformed(evt);
            }
        });
        jMenu8.add(jMenuItem38);

        jMenuItem40.setText(bundle.getString("MPView.jMenuItem40.text_1")); // NOI18N
        jMenuItem40.setName("jMenuItem40"); // NOI18N
        jMenuItem40.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem40ActionPerformed(evt);
            }
        });
        jMenu8.add(jMenuItem40);

        jMenu12.setText(bundle.getString("MPView.jMenu12.text")); // NOI18N
        jMenu12.setName("jMenu12"); // NOI18N

        jMenuItem28.setText(bundle.getString("MPView.jMenuItem28.text")); // NOI18N
        jMenuItem28.setName("jMenuItem28"); // NOI18N
        jMenuItem28.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem28ActionPerformed(evt);
            }
        });
        jMenu12.add(jMenuItem28);

        jMenuItem29.setText(bundle.getString("MPView.jMenuItem29.text")); // NOI18N
        jMenuItem29.setName("jMenuItem29"); // NOI18N
        jMenuItem29.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem29ActionPerformed(evt);
            }
        });
        jMenu12.add(jMenuItem29);

        jMenu8.add(jMenu12);

        fileMenu.add(jMenu8);

        jSeparator1.setName("jSeparator1"); // NOI18N
        fileMenu.add(jSeparator1);

        jMenuItem2.setText(bundle.getString("MPView.jMenuItem2.text")); // NOI18N
        jMenuItem2.setName("jMenuItem2"); // NOI18N
        jMenuItem2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem2ActionPerformed(evt);
            }
        });
        fileMenu.add(jMenuItem2);

        jSeparator9.setName("jSeparator9"); // NOI18N
        fileMenu.add(jSeparator9);

        jMenuItem12.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_X, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem12.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/16/remove.png"))); // NOI18N
        jMenuItem12.setText(bundle.getString("MPView.jMenuItem12.text")); // NOI18N
        jMenuItem12.setName("jMenuItem12"); // NOI18N
        jMenuItem12.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem12ActionPerformed(evt);
            }
        });
        fileMenu.add(jMenuItem12);

        menuBar.add(fileMenu);

        editMenu.setText(bundle.getString("MPView.editMenu.text")); // NOI18N
        editMenu.setName("editMenu"); // NOI18N

        jMenuItem15.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_R, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem15.setText(bundle.getString("MPView.jMenuItem15.text")); // NOI18N
        jMenuItem15.setName("jMenuItem15"); // NOI18N
        jMenuItem15.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem15ActionPerformed(evt);
            }
        });
        editMenu.add(jMenuItem15);

        jSeparator4.setName("jSeparator4"); // NOI18N
        editMenu.add(jSeparator4);

        jMenuItem9.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_N, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem9.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/16/editcopy.png"))); // NOI18N
        jMenuItem9.setText(bundle.getString("MPView.jMenuItem9.text")); // NOI18N
        jMenuItem9.setName("jMenuItem9"); // NOI18N
        jMenuItem9.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem9ActionPerformed(evt);
            }
        });
        editMenu.add(jMenuItem9);

        jMenuItem16.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_B, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem16.setText(bundle.getString("MPView.jMenuItem16.text")); // NOI18N
        jMenuItem16.setName("jMenuItem16"); // NOI18N
        jMenuItem16.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem16ActionPerformed(evt);
            }
        });
        editMenu.add(jMenuItem16);

        jSeparator5.setName("jSeparator5"); // NOI18N
        editMenu.add(jSeparator5);

        jMenuItem17.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem17.setText(bundle.getString("MPView.jMenuItem17.text")); // NOI18N
        jMenuItem17.setName("jMenuItem17"); // NOI18N
        jMenuItem17.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem17ActionPerformed(evt);
            }
        });
        editMenu.add(jMenuItem17);

        menuBar.add(editMenu);

        viewMenu.setText(bundle.getString("MPView.viewMenu.text_1")); // NOI18N
        viewMenu.setName("viewMenu"); // NOI18N

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

        jMenuItem39.setText(bundle.getString("MPView.jMenuItem39.text")); // NOI18N
        jMenuItem39.setName("jMenuItem39"); // NOI18N
        jMenuItem39.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem39ActionPerformed(evt);
            }
        });
        jMenu2.add(jMenuItem39);

        viewMenu.add(jMenu2);

        jMenuItem19.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F5, 0));
        jMenuItem19.setText(bundle.getString("MPView.jMenuItem19.text")); // NOI18N
        jMenuItem19.setName("jMenuItem19"); // NOI18N
        jMenuItem19.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem19ActionPerformed(evt);
            }
        });
        viewMenu.add(jMenuItem19);

        jMenuItem4.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F11, 0));
        jMenuItem4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/16/2uparrow.png"))); // NOI18N
        jMenuItem4.setText(bundle.getString("MPView.jMenuItem4.text")); // NOI18N
        jMenuItem4.setName("jMenuItem4"); // NOI18N
        jMenuItem4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem4ActionPerformed(evt);
            }
        });
        viewMenu.add(jMenuItem4);

        jMenu15.setText(bundle.getString("MPView.jMenu15.text")); // NOI18N
        jMenu15.setName("jMenu15"); // NOI18N

        jMenuItem35.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_L, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem35.setText(bundle.getString("MPView.jMenuItem35.text")); // NOI18N
        jMenuItem35.setName("jMenuItem35"); // NOI18N
        jMenuItem35.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem35ActionPerformed(evt);
            }
        });
        jMenu15.add(jMenuItem35);

        jMenuItem36.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_M, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem36.setText(bundle.getString("MPView.jMenuItem36.text")); // NOI18N
        jMenuItem36.setName("jMenuItem36"); // NOI18N
        jMenuItem36.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem36ActionPerformed(evt);
            }
        });
        jMenu15.add(jMenuItem36);

        viewMenu.add(jMenu15);

        jMenuItem49.setText(bundle.getString("MPView.jMenuItem49.text")); // NOI18N
        jMenuItem49.setName("jMenuItem49"); // NOI18N
        jMenuItem49.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem49ActionPerformed(evt);
            }
        });
        viewMenu.add(jMenuItem49);

        simplenav.setText(bundle.getString("MPView.simplenav.text")); // NOI18N
        simplenav.setName("simplenav"); // NOI18N
        simplenav.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                simplenavActionPerformed(evt);
            }
        });
        viewMenu.add(simplenav);

        menuBar.add(viewMenu);

        toolsMenu.setText(bundle.getString("MPView.toolsMenu.text_1")); // NOI18N
        toolsMenu.setName("toolsMenu"); // NOI18N

        jMenuItem3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/16/configure_shortcuts.png"))); // NOI18N
        jMenuItem3.setText(bundle.getString("MPView.jMenuItem3.text_1")); // NOI18N
        jMenuItem3.setName("jMenuItem3"); // NOI18N
        jMenuItem3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem3ActionPerformed(evt);
            }
        });
        toolsMenu.add(jMenuItem3);

        jMenuItem23.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/16/edittrash.png"))); // NOI18N
        jMenuItem23.setText(bundle.getString("MPView.jMenuItem23.text")); // NOI18N
        jMenuItem23.setName("jMenuItem23"); // NOI18N
        jMenuItem23.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem23ActionPerformed(evt);
            }
        });
        toolsMenu.add(jMenuItem23);

        jMenuItem31.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/16/cal.png"))); // NOI18N
        jMenuItem31.setText(bundle.getString("MPView.jMenuItem31.text")); // NOI18N
        jMenuItem31.setName("jMenuItem31"); // NOI18N
        jMenuItem31.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem31ActionPerformed(evt);
            }
        });
        toolsMenu.add(jMenuItem31);

        jMenu3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/22/exec.png"))); // NOI18N
        jMenu3.setText(bundle.getString("MPView.jMenu3.text")); // NOI18N
        jMenu3.setName("jMenu3"); // NOI18N

        jMenuItem18.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F9, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem18.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/16/fileexport.png"))); // NOI18N
        jMenuItem18.setText(bundle.getString("MPView.jMenuItem18.text")); // NOI18N
        jMenuItem18.setName("jMenuItem18"); // NOI18N
        jMenuItem18.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                actionExportToDerby(evt);
            }
        });
        jMenu3.add(jMenuItem18);

        jMenuItem37.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F10, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem37.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/16/cal.png"))); // NOI18N
        jMenuItem37.setText(bundle.getString("MPView.jMenuItem37.text")); // NOI18N
        jMenuItem37.setName("jMenuItem37"); // NOI18N
        jMenuItem37.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                actionMigrateToMySQL(evt);
            }
        });
        jMenu3.add(jMenuItem37);

        jMenuItem46.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F11, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem46.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/16/editpaste.png"))); // NOI18N
        jMenuItem46.setText(bundle.getString("MPView.jMenuItem46.text_1")); // NOI18N
        jMenuItem46.setName("jMenuItem46"); // NOI18N
        jMenuItem46.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                actionImport(evt);
            }
        });
        jMenu3.add(jMenuItem46);

        toolsMenu.add(jMenu3);

        jSeparator6.setName("jSeparator6"); // NOI18N
        toolsMenu.add(jSeparator6);

        jMenuItem43.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/22/kontact_journal.png"))); // NOI18N
        jMenuItem43.setText(bundle.getString("MPView.jMenuItem43.text")); // NOI18N
        jMenuItem43.setName("jMenuItem43"); // NOI18N
        jMenuItem43.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem43ActionPerformed(evt);
            }
        });
        toolsMenu.add(jMenuItem43);

        jMenuItem45.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/22/kontact_mail.png"))); // NOI18N
        jMenuItem45.setText(bundle.getString("MPView.jMenuItem45.text")); // NOI18N
        jMenuItem45.setName("jMenuItem45"); // NOI18N
        jMenuItem45.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem45ActionPerformed(evt);
            }
        });
        toolsMenu.add(jMenuItem45);

        jMenuItem48.setText(bundle.getString("MPView.jMenuItem48.text_1")); // NOI18N
        jMenuItem48.setName("jMenuItem48"); // NOI18N
        jMenuItem48.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem48ActionPerformed(evt);
            }
        });
        toolsMenu.add(jMenuItem48);

        menuBar.add(toolsMenu);

        jMenu1.setText(bundle.getString("MPView.jMenu1.text")); // NOI18N
        jMenu1.setName("jMenu1"); // NOI18N

        jMenuItem42.setText(bundle.getString("MPView.jMenuItem42.text")); // NOI18N
        jMenuItem42.setName("jMenuItem42"); // NOI18N
        jMenuItem42.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem42ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem42);

        jMenuItem50.setText(bundle.getString("MPView.jMenuItem50.text")); // NOI18N
        jMenuItem50.setName("jMenuItem50"); // NOI18N
        jMenuItem50.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem50ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem50);

        jMenuItem44.setText(bundle.getString("MPView.jMenuItem44.text")); // NOI18N
        jMenuItem44.setName("jMenuItem44"); // NOI18N
        jMenuItem44.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem44ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem44);

        menuBar.add(jMenu1);

        favouritesMenu.setText(bundle.getString("MPView.favouritesMenu.text")); // NOI18N
        favouritesMenu.setName("favouritesMenu"); // NOI18N
        menuBar.add(favouritesMenu);

        clipboardMenu.setText(bundle.getString("MPView.clipboardMenu.text")); // NOI18N
        clipboardMenu.setName("clipboardMenu"); // NOI18N

        jMenu6.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/16/finish.png"))); // NOI18N
        jMenu6.setName("jMenu6"); // NOI18N

        jMenuItem7.setText(bundle.getString("MPView.jMenuItem7.text")); // NOI18N
        jMenuItem7.setName("jMenuItem7"); // NOI18N
        jMenuItem7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem7ActionPerformed(evt);
            }
        });
        jMenu6.add(jMenuItem7);

        jMenuItem5.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Y, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem5.setText(bundle.getString("MPView.jMenuItem5.text")); // NOI18N
        jMenuItem5.setName("jMenuItem5"); // NOI18N
        jMenuItem5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem5ActionPerformed(evt);
            }
        });
        jMenu6.add(jMenuItem5);

        clipboardMenu.add(jMenu6);

        menuBar.add(clipboardMenu);

        helpmenu.setText(bundle.getString("MPView.helpmenu.text")); // NOI18N
        helpmenu.setName("helpmenu"); // NOI18N
        helpmenu.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                helpmenuMouseClicked(evt);
            }
        });
        helpmenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                helpmenuActionPerformed(evt);
            }
        });

        jMenuItem10.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_L, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem10.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/16/cal.png"))); // NOI18N
        jMenuItem10.setText(bundle.getString("MPView.jMenuItem10.text")); // NOI18N
        jMenuItem10.setName("jMenuItem10"); // NOI18N
        jMenuItem10.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem10ActionPerformed(evt);
            }
        });
        helpmenu.add(jMenuItem10);

        jMenuItem27.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F1, 0));
        jMenuItem27.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/16/info.png"))); // NOI18N
        jMenuItem27.setText(bundle.getString("MPView.jMenuItem27.text")); // NOI18N
        jMenuItem27.setName("jMenuItem27"); // NOI18N
        jMenuItem27.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem27ActionPerformed(evt);
            }
        });
        helpmenu.add(jMenuItem27);

        jMenuItem30.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/about.gif"))); // NOI18N
        jMenuItem30.setText(bundle.getString("MPView.jMenuItem30.text")); // NOI18N
        jMenuItem30.setName("jMenuItem30"); // NOI18N
        jMenuItem30.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem30ActionPerformed(evt);
            }
        });
        helpmenu.add(jMenuItem30);

        menuBar.add(helpmenu);

        statusPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.RAISED));
        statusPanel.setMaximumSize(new java.awt.Dimension(1914, 50));
        statusPanel.setMinimumSize(new java.awt.Dimension(457, 25));
        statusPanel.setName("statusPanel"); // NOI18N
        statusPanel.setPreferredSize(new java.awt.Dimension(800, 25));
        statusPanel.setLayout(new javax.swing.BoxLayout(statusPanel, javax.swing.BoxLayout.LINE_AXIS));

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

        officelabel.setMaximumSize(new java.awt.Dimension(20, 100));
        officelabel.setMinimumSize(new java.awt.Dimension(20, 10));
        officelabel.setName("officelabel"); // NOI18N
        officelabel.setPreferredSize(new java.awt.Dimension(20, 18));
        officelabel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 5, 0));
        statusPanel.add(officelabel);

        serverlabel.setMaximumSize(new java.awt.Dimension(20, 100));
        serverlabel.setMinimumSize(new java.awt.Dimension(20, 10));
        serverlabel.setName("serverlabel"); // NOI18N
        serverlabel.setPreferredSize(new java.awt.Dimension(20, 18));
        serverlabel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 5, 0));
        statusPanel.add(serverlabel);

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
            .addGap(0, 21, Short.MAX_VALUE)
        );

        statusPanel.add(separator);

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
            .addGap(0, 21, Short.MAX_VALUE)
        );

        statusPanel.add(separator1);

        xhistory.setMaximumRowCount(100);
        xhistory.setAutoscrolls(true);
        xhistory.setEditor(null);
        xhistory.setFocusable(false);
        xhistory.setMaximumSize(new java.awt.Dimension(500, 32767));
        xhistory.setMinimumSize(new java.awt.Dimension(100, 15));
        xhistory.setName("xhistory"); // NOI18N
        xhistory.setPreferredSize(new java.awt.Dimension(300, 25));
        xhistory.setRequestFocusEnabled(false);
        xhistory.setVerifyInputWhenFocusTarget(false);
        statusPanel.add(xhistory);

        errorlabel.setText(bundle.getString("MPView.errorlabel.text")); // NOI18N
        errorlabel.setName("errorlabel"); // NOI18N
        errorlabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                errorlabelMouseClicked(evt);
            }
        });
        statusPanel.add(errorlabel);

        mainToolbar.setFloatable(false);
        mainToolbar.setRollover(true);
        mainToolbar.setName("mainToolbar"); // NOI18N
        mainToolbar.setPreferredSize(new java.awt.Dimension(203, 25));

        closeButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/22/back.png"))); // NOI18N
        closeButton1.setToolTipText(bundle.getString("MPView.closeButton1.toolTipText")); // NOI18N
        closeButton1.setFocusable(false);
        closeButton1.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        closeButton1.setName("closeButton1"); // NOI18N
        closeButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeButton1ActionPerformed(evt);
            }
        });
        mainToolbar.add(closeButton1);

        closeButton2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/22/forward.png"))); // NOI18N
        closeButton2.setToolTipText(bundle.getString("MPView.closeButton2.toolTipText")); // NOI18N
        closeButton2.setFocusable(false);
        closeButton2.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        closeButton2.setName("closeButton2"); // NOI18N
        closeButton2.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        closeButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeButton2ActionPerformed(evt);
            }
        });
        mainToolbar.add(closeButton2);

        jSeparator7.setName("jSeparator7"); // NOI18N
        mainToolbar.add(jSeparator7);

        closeButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/22/exit.png"))); // NOI18N
        closeButton.setToolTipText(bundle.getString("MPView.closeButton.toolTipText_1")); // NOI18N
        closeButton.setFocusable(false);
        closeButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        closeButton.setName("closeButton"); // NOI18N
        closeButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        closeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeButtonActionPerformed(evt);
            }
        });
        mainToolbar.add(closeButton);

        lockButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/22/lock.png"))); // NOI18N
        lockButton.setToolTipText(bundle.getString("MPView.lockButton.toolTipText_1")); // NOI18N
        lockButton.setFocusable(false);
        lockButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        lockButton.setName("lockButton"); // NOI18N
        lockButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        lockButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                lockButtonActionPerformed(evt);
            }
        });
        mainToolbar.add(lockButton);

        jSeparator2.setName("jSeparator2"); // NOI18N
        mainToolbar.add(jSeparator2);

        calculatorButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/22/run.png"))); // NOI18N
        calculatorButton.setToolTipText(bundle.getString("MPView.calculatorButton.toolTipText_1")); // NOI18N
        calculatorButton.setFocusable(false);
        calculatorButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        calculatorButton.setName("calculatorButton"); // NOI18N
        calculatorButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        calculatorButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                calculatorButtonActionPerformed(evt);
            }
        });
        mainToolbar.add(calculatorButton);

        jButton2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/22/printer1.png"))); // NOI18N
        jButton2.setText(bundle.getString("MPView.jButton2.text")); // NOI18N
        jButton2.setToolTipText(bundle.getString("MPView.jButton2.toolTipText")); // NOI18N
        jButton2.setFocusable(false);
        jButton2.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton2.setName("jButton2"); // NOI18N
        jButton2.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });
        mainToolbar.add(jButton2);

        setComponent(mainPanel);
        setMenuBar(menuBar);
        setStatusBar(statusPanel);
        setToolBar(mainToolbar);
    }// </editor-fold>//GEN-END:initComponents

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed

        DatabaseObject d = DatabaseObject.getObject(Context.getCustomer());
        ((mpv5.db.objects.Contact) d).setisCustomer(true);

        ContactsList t = getClisttab(Context.getCustomer());
        t.showType((Contact) d);
        addOrShowTab(t, Messages.CONTACTS_LIST.toString());
    }//GEN-LAST:event_jButton5ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed

        DatabaseObject d = DatabaseObject.getObject(Context.getSupplier());
        ((mpv5.db.objects.Contact) d).setisSupplier(true);

        ContactsList t = getClisttab(Context.getSupplier());
        t.showType((Contact) d);
        addOrShowTab(t, Messages.CONTACTS_LIST.toString());

}//GEN-LAST:event_jButton1ActionPerformed
    private static ContactsList clisttabc;
    private static ContactsList clisttabs;
    private static ContactsList clisttabm;
    private static ProductList plisttab;
    private static ProductsOverview polisttab;
    private void jButton18ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton18ActionPerformed

        DatabaseObject d = DatabaseObject.getObject(Context.getManufacturer());
        ((mpv5.db.objects.Contact) d).setisManufacturer(true);

        ContactsList t = getClisttab(Context.getManufacturer());
        t.showType((Contact) d);
        addOrShowTab(t, Messages.CONTACTS_LIST.toString());
    }//GEN-LAST:event_jButton18ActionPerformed

    private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem1ActionPerformed
        Main.setLaF(null);
        User.getCurrentUser().setLaf(UIManager.getSystemLookAndFeelClassName());
        User.getCurrentUser().save();
    }//GEN-LAST:event_jMenuItem1ActionPerformed

    private void jMenuItem3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem3ActionPerformed
        addOrShowTab(MPControlPanel.instanceOf(), Messages.CONTROL_PANEL.toString());

    }//GEN-LAST:event_jMenuItem3ActionPerformed

    private void calculatorButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_calculatorButtonActionPerformed
        if (LocalSettings.hasProperty(LocalSettings.CALCULATOR) && LocalSettings.getProperty(LocalSettings.CALCULATOR).length() >= 1) {
            try {
                Runtime.getRuntime().exec(LocalSettings.getProperty(LocalSettings.CALCULATOR));
            } catch (Exception e) {
                Popup.error(e);
                Log.Debug(e);
            }
        } else {
            MPCalculator2.instanceOf();
        }
    }//GEN-LAST:event_calculatorButtonActionPerformed

    private void lockButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_lockButtonActionPerformed

        mpv5.usermanagement.Lock.lock(this.getFrame());
        User.getCurrentUser().logout();
}//GEN-LAST:event_lockButtonActionPerformed

    private void closeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeButtonActionPerformed
        if (Popup.Y_N_dialog(Messages.REALLY_CLOSE)) {
            Main.getApplication().exit();
        }
}//GEN-LAST:event_closeButtonActionPerformed

    private void jMenuItem4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem4ActionPerformed
        selectedTabInNewFrame();
    }//GEN-LAST:event_jMenuItem4ActionPerformed

    private void jMenuItem6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem6ActionPerformed
        DataPanel pane = getCurrentTab();
        if (pane != null) {
            pane.print();
        } else {
            Export.print(getTabPane().getSelectedComponent());
        }
    }//GEN-LAST:event_jMenuItem6ActionPerformed

    private void jMenuItem7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem7ActionPerformed

        getClipboardMenu().removeAll();
        getClipboardMenu().add(jMenu6);

    }//GEN-LAST:event_jMenuItem7ActionPerformed

    private void jMenuItem8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem8ActionPerformed
        XMLWriter.export(Context.getContact());
    }//GEN-LAST:event_jMenuItem8ActionPerformed

    private void jMenuItem9ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem9ActionPerformed

        getClipboardMenu().add(new ClipboardMenuItem(getCurrentTab().getDataOwner()));

    }//GEN-LAST:event_jMenuItem9ActionPerformed

    private void jMenuItem11ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem11ActionPerformed
        importXML();
    }//GEN-LAST:event_jMenuItem11ActionPerformed

    private void jButton6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton6ActionPerformed
        addTab((JComponent) HistoryPanel.instanceOf(), Messages.HISTORY_PANEL.toString());

    }//GEN-LAST:event_jButton6ActionPerformed

    private void jMenuItem12ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem12ActionPerformed
        getIdentifierApplication().exit(evt);
    }//GEN-LAST:event_jMenuItem12ActionPerformed

    private void jMenuItem13ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem13ActionPerformed
        try {
            DataPanel pane = getCurrentTab();
            if (pane != null) {
                pane.actionBeforeSave();
                try {
                    DatabaseObject dato = (pane).getDataOwner();
                    dato.getPanelData((pane));
                    if (dato.save()) {
                        (pane).actionAfterSave();
                        (pane).setDataOwner(dato, true);
                    } else {
                        (pane).showRequiredFields();
                    }
                } catch (Exception e) {
                    Log.Debug(this, e);
                }
            }
        } catch (ChangeNotApprovedException changeNotApprovedException) {
            Log.Debug(this, changeNotApprovedException.getMessage());
        }
    }//GEN-LAST:event_jMenuItem13ActionPerformed

    private void jMenuItem14ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem14ActionPerformed

        DataPanel pane = getCurrentTab();
        if (pane != null) {
            try {
                pane.actionBeforeCreate();
                DatabaseObject dato = (pane).getDataOwner();
                dato.getPanelData((pane));
                dato.setIDS(-1);
                if (dato.save()) {
                    pane.actionAfterSave();
                    (pane).setDataOwner(dato, true);
                } else {
                    (pane).showRequiredFields();
                }
            } catch (Exception e) {
                Log.Debug(this, e);
            }
        }
    }//GEN-LAST:event_jMenuItem14ActionPerformed

    private void jMenuItem16ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem16ActionPerformed
        getCurrentTab().paste(getClipboardItems());
    }//GEN-LAST:event_jMenuItem16ActionPerformed

    private void jMenuItem15ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem15ActionPerformed
        try {
            DataPanel tab = getCurrentTab();
            DatabaseObject dato = tab.getDataOwner();

            if (dato.isExisting()) {
                dato.getPanelData((tab));
                dato.reset();
                tab.refresh();
                tab.setDataOwner(dato, true);
            }
        } catch (Exception ignore) {
        }
    }//GEN-LAST:event_jMenuItem15ActionPerformed

    private void jMenuItem17ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem17ActionPerformed

        Search.instanceOf();

    }//GEN-LAST:event_jMenuItem17ActionPerformed

    private void jMenuItem19ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem19ActionPerformed
        try {
            getCurrentTab().refresh();
        } catch (Exception ignore) {
        }
    }//GEN-LAST:event_jMenuItem19ActionPerformed

    private void jMenuItem20ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem20ActionPerformed

        XMLWriter.export(Context.getAccounts());
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

    private void jMenuItem23ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem23ActionPerformed
        addOrShowTab(TrashPanel.instanceOf(), Messages.TRASHBIN.getValue());
    }//GEN-LAST:event_jMenuItem23ActionPerformed

    private void jButton7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton7ActionPerformed
        addOrShowTab(TrashPanel.instanceOf(), Messages.TRASHBIN.toString());
    }//GEN-LAST:event_jButton7ActionPerformed

    private void jMenuItem26ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem26ActionPerformed
        XMLWriter.export(Context.getInvoice());
    }//GEN-LAST:event_jMenuItem26ActionPerformed

    private void jButton10ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton10ActionPerformed

        addTab(new ItemPanel2(Context.getOffer(), Item.TYPE_OFFER), Messages.NEW_OFFER);
    }//GEN-LAST:event_jButton10ActionPerformed

    private void jButton11ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton11ActionPerformed
        addTab(new ItemPanel2(Context.getOrder(), Item.TYPE_ORDER), Messages.NEW_ORDER);
    }//GEN-LAST:event_jButton11ActionPerformed

    private void jButton12ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton12ActionPerformed
        addTab(new ProductPanel(Context.getProduct(), Product.TYPE_SERVICE), Messages.NEW_SERVICE);
    }//GEN-LAST:event_jButton12ActionPerformed

    private void jButton13ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton13ActionPerformed
        addTab(new ProductPanel(Context.getProduct(), Product.TYPE_PRODUCT), Messages.NEW_PRODUCT);
    }//GEN-LAST:event_jButton13ActionPerformed

    private void errorlabelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_errorlabelMouseClicked
        SubmitForm submitForm = new SubmitForm(ExceptionHandler.getExceptions());
        BigPopup.showPopup(getFrame().getRootPane(), submitForm, "Bughunter");
        getErrorlabel().setIcon(null);
        getIdentifierFrame().validate();
    }//GEN-LAST:event_errorlabelMouseClicked

    private void jButton14ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton14ActionPerformed
        addTab(new ProductListsPanel(), Messages.NEW_LIST);

    }//GEN-LAST:event_jButton14ActionPerformed

    private void jButton15ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton15ActionPerformed
        addOrShowTab(ExpensePanel.instanceOf(), Messages.EXPENSE);
    }//GEN-LAST:event_jButton15ActionPerformed

    private void jButton16ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton16ActionPerformed
        addOrShowTab(RevenuePanel.instanceOf(), Messages.REVENUE);

    }//GEN-LAST:event_jButton16ActionPerformed

    private void jButton17ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton17ActionPerformed
        addOrShowTab(JournalPanel.instanceOf(), Messages.OVERVIEW);
    }//GEN-LAST:event_jButton17ActionPerformed

    private void helpmenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_helpmenuActionPerformed
}//GEN-LAST:event_helpmenuActionPerformed

    private void helpmenuMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_helpmenuMouseClicked
    }//GEN-LAST:event_helpmenuMouseClicked

    private void jMenuItem27ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem27ActionPerformed

        try {
            java.awt.Desktop.getDesktop().browse(new URI(Constants.WEBSITE));
        } catch (Exception ex) {
            Log.Debug(ex);
        }
}//GEN-LAST:event_jMenuItem27ActionPerformed

    private void jMenuItem10ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem10ActionPerformed
        addOrShowTab(Log.getLogger().get(0).open(), "Logs");
}//GEN-LAST:event_jMenuItem10ActionPerformed

    private void jMenuItem30ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem30ActionPerformed
        new About(new ImageIcon(About.class.getResource(mpv5.globals.Constants.ABOUT_IMAGE)));
    }//GEN-LAST:event_jMenuItem30ActionPerformed

    private void jButton19ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton19ActionPerformed

        if (getPlisttab() == null) {
            plisttab = new ProductList();
        }
        addOrShowTab(getPlisttab(), Messages.ALL_PRODUCTS.toString());
    }//GEN-LAST:event_jButton19ActionPerformed

    private void jMenuItem31ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem31ActionPerformed

        addTab(new QueryPanel(), Messages.QUERY_WINDOW);
    }//GEN-LAST:event_jMenuItem31ActionPerformed

    private void jMenuItem32ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem32ActionPerformed

        XMLWriter.export(Context.getProduct());
    }//GEN-LAST:event_jMenuItem32ActionPerformed

    private void jMenuItem33ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem33ActionPerformed
        DatabaseObject d = Search2.showSearchFor(Context.getContact());
        if (d != null) {
            addTab(d);
        }
    }//GEN-LAST:event_jMenuItem33ActionPerformed

    private void jMenuItem34ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem34ActionPerformed

        DatabaseObject d = Search2.showSearchFor(Context.getInvoice());
        if (d != null) {
            addTab(d);
        }
    }//GEN-LAST:event_jMenuItem34ActionPerformed

    private void jMenuItem35ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem35ActionPerformed
        tabPane.removeAll();
        tabPane.validate();

    }//GEN-LAST:event_jMenuItem35ActionPerformed

    private void jMenuItem36ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem36ActionPerformed
        tabPane.removeAllButSelected();
    }//GEN-LAST:event_jMenuItem36ActionPerformed

    private void jMenuItem22ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem22ActionPerformed
        DialogForFile f = getFiledialog();
        if (f.saveFile()) {
            try {
                VCFParser.toVCard(DatabaseObject.getObjects(Context.getContact(), true), f.getFile());
            } catch (NodataFoundException ex) {
            }
        }
    }//GEN-LAST:event_jMenuItem22ActionPerformed

    private void jMenuItem38ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem38ActionPerformed

        DialogForFile f = getFiledialog();
        List<Contact> contacts = null;
        if (f.chooseFile()) {
            try {
                List<VCard> l = VCFParser.parse(f.getFile());
                contacts = VCFParser.toContacts(l);

            } catch (Exception ex) {
//                        Log.Debug(ex);
                Popup.error(ex);
            }

            if (contacts != null) {
                for (int i = 0; i < contacts.size(); i++) {
                    Contact contact = null;
                    try {
                        contact = contacts.get(i);
                        contact.saveImport();
                    } catch (Exception ec) {
                        Popup.error(ec);
                        contact.setCname("ERROR");
                    }
                }

                GeneralListPanel pl = new GeneralListPanel(contacts);
                MPView.identifierView.addTab(pl, "Imported contacts");
            }
        }
    }//GEN-LAST:event_jMenuItem38ActionPerformed

    private void jMenuItem39ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem39ActionPerformed
        User.getCurrentUser().getLayoutProperties().clear();
        try {
            ValueProperty.deleteProperty(User.getCurrentUser(), "layoutinfo");
        } catch (Exception ex) {
            Log.Debug(ex);
        }

        resettables(getTabpanePanel());
    }//GEN-LAST:event_jMenuItem39ActionPerformed

    private void jButton20ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton20ActionPerformed

        if (polisttab == null) {
            polisttab = new ProductsOverview();
        }
        addOrShowTab(polisttab, Messages.ALL_PRODUCTS.toString());
    }//GEN-LAST:event_jButton20ActionPerformed

    private void jMenuItem41ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem41ActionPerformed
        //TODO product to html wiz
    }//GEN-LAST:event_jMenuItem41ActionPerformed

    private void jMenuItem40ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem40ActionPerformed

        Wizard w = new Wizard(false);
        w.addPanel(new wizard_CSVImport2_1(w));
        w.showWiz();

    }//GEN-LAST:event_jMenuItem40ActionPerformed

    private void jMenuItem42ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem42ActionPerformed
        addOrShowTab(MPControlPanel.instanceOf(), Messages.CONTROL_PANEL.toString());
        MPControlPanel.instanceOf().openDetails(new ControlPanel_MailTemplates());
    }//GEN-LAST:event_jMenuItem42ActionPerformed

    private void jMenuItem44ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem44ActionPerformed
        mpv5.usermanagement.Lock.lock(this.getFrame());
        User.getCurrentUser().logout();

    }//GEN-LAST:event_jMenuItem44ActionPerformed

    private void jMenuItem5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem5ActionPerformed
        mpv5.YabsViewProxy.instance().setClipBoardVisible(true);
    }//GEN-LAST:event_jMenuItem5ActionPerformed

    private void jMenuItem43ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem43ActionPerformed
        try {
            (new Scheduler()).start();
        } catch (Exception e) {
            Log.Debug(e);
        }
    }//GEN-LAST:event_jMenuItem43ActionPerformed

    private void closeButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeButton1ActionPerformed
        showPreviousDatabaseObject();
    }//GEN-LAST:event_closeButton1ActionPerformed

    private void closeButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeButton2ActionPerformed
        showNextDatabaseObject();
    }//GEN-LAST:event_closeButton2ActionPerformed

    private void jButton9ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton9ActionPerformed
        addOrShowTab(ConversationPanel.instanceOf(), Messages.CONVERSATION.toString());
    }//GEN-LAST:event_jButton9ActionPerformed

    private void jMenuItem45ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem45ActionPerformed
        addOrShowTab(MassPrintPanel.instanceOf(), Messages.MASSPRINT.toString());
    }//GEN-LAST:event_jMenuItem45ActionPerformed

    private void jButton21ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton21ActionPerformed
        addOrShowTab(new ActivityConfirmationPanel(), Messages.ACTIVITYCONFIRMATION.toString());
    }//GEN-LAST:event_jButton21ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        DataPanel pane = getCurrentTab();
        if (pane != null) {
            pane.print();
        } else {
            Export.print(getTabPane().getSelectedComponent());
        }
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton22ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton22ActionPerformed
        addTab(new ProductOrderPanel(), Messages.NEW_ORDER);

    }//GEN-LAST:event_jButton22ActionPerformed

    private void actionImport(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_actionImport
        MigrationWB.instanceOf().doImport();
    }//GEN-LAST:event_actionImport

    private void actionMigrateToMySQL(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_actionMigrateToMySQL
        MigrationWB.instanceOf().doExportToMySQL();
    }//GEN-LAST:event_actionMigrateToMySQL

    private void actionExportToDerby(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_actionExportToDerby
        MigrationWB.instanceOf().doExportToDerby();
    }//GEN-LAST:event_actionExportToDerby

    private void jMenuItem28ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem28ActionPerformed
        Wizard w = new Wizard(false);
        w.addPanel(new wizard_Yabs1_Import(w));
        w.showWiz();
    }//GEN-LAST:event_jMenuItem28ActionPerformed

    private void jMenuItem29ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem29ActionPerformed
        Wizard w = new Wizard(false);
        w.addPanel(new wizard_MP45_Import(w));
        w.showWiz();
    }//GEN-LAST:event_jMenuItem29ActionPerformed

    private void jMenuItem48ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem48ActionPerformed

        addOrShowTab(Stockmanager.instanceOf(), Messages.STOCK_MANAGER);
        Stockmanager.instanceOf().start();
    }//GEN-LAST:event_jMenuItem48ActionPerformed

    private void jMenuItem49ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem49ActionPerformed
        Wizard w = new Wizard(false);
        w.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
        w.setAlwaysOnTop(false);
        w.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        w.setLocationRelativeTo(getIdentifierFrame());
        w.addPanel(new wizard_Toolbar1(w));
        w.showWizAsChild();

    }//GEN-LAST:event_jMenuItem49ActionPerformed

   private void jMenuItem50ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem50ActionPerformed
       wizard_FirstSettings1.build(getIdentifierFrame());
   }//GEN-LAST:event_jMenuItem50ActionPerformed

   private void jMenuItem2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem2ActionPerformed
       LoginToInstanceScreen.load();
   }//GEN-LAST:event_jMenuItem2ActionPerformed

private void jMenuItem51ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem51ActionPerformed

}//GEN-LAST:event_jMenuItem51ActionPerformed

private void jButton23ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton23ActionPerformed

    addOrShowTab(new ReceiptLookup(), Messages.OVERVIEW);
}//GEN-LAST:event_jButton23ActionPerformed

    private void jButton30ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton30ActionPerformed
        addTab(new ItemPanel2(Context.getCredit(), Item.TYPE_CREDIT), Messages.NEW_CREDIT);
    }//GEN-LAST:event_jButton30ActionPerformed

    private void scustomersActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_scustomersActionPerformed

        DatabaseObject d = DatabaseObject.getObject(Context.getCustomer());
        ((mpv5.db.objects.Contact) d).setisCustomer(true);

        ContactsList t = getClisttab(Context.getCustomer());
        t.showType((Contact) d);
        addOrShowTab(t, Messages.CONTACTS_LIST.toString());
    }//GEN-LAST:event_scustomersActionPerformed

    private void sjournalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sjournalActionPerformed
        addOrShowTab(JournalPanel.instanceOf(), Messages.OVERVIEW);
    }//GEN-LAST:event_sjournalActionPerformed

    private void sproductsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sproductsActionPerformed
        addTab(new ProductPanel(Context.getProduct(), Product.TYPE_PRODUCT), Messages.NEW_PRODUCT);
    }//GEN-LAST:event_sproductsActionPerformed

    private void sinvoicesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sinvoicesActionPerformed
        addTab(new ItemPanel2(Context.getInvoice(), Item.TYPE_INVOICE), Messages.NEW_INVOICE);
    }//GEN-LAST:event_sinvoicesActionPerformed

    private void simplenavActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_simplenavActionPerformed

        boolean oldval = GlobalSettings.getBooleanProperty("navigation.simplified");
        GlobalSettings.setProperty("navigation.simplified", String.valueOf(!oldval));

        initNav();
    }//GEN-LAST:event_simplenavActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton calculatorButton;
    public javax.swing.JMenu clipboardMenu;
    private javax.swing.JButton closeButton;
    private javax.swing.JButton closeButton1;
    private javax.swing.JButton closeButton2;
    private mpv5.ui.beans.BigDropDownButton ddb1;
    private javax.swing.JMenu editMenu;
    private javax.swing.JLabel errorlabel;
    private javax.swing.JMenu favouritesMenu;
    private javax.swing.JMenu helpmenu;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton10;
    private javax.swing.JButton jButton11;
    private javax.swing.JButton jButton12;
    private javax.swing.JButton jButton13;
    private javax.swing.JButton jButton14;
    private javax.swing.JButton jButton15;
    private javax.swing.JButton jButton16;
    private javax.swing.JButton jButton17;
    private javax.swing.JButton jButton18;
    private javax.swing.JButton jButton19;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton20;
    private javax.swing.JButton jButton21;
    private javax.swing.JButton jButton22;
    private javax.swing.JButton jButton23;
    private javax.swing.JButton jButton30;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton7;
    private javax.swing.JButton jButton9;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu10;
    private javax.swing.JMenu jMenu11;
    private javax.swing.JMenu jMenu12;
    private javax.swing.JMenu jMenu13;
    private javax.swing.JMenu jMenu14;
    private javax.swing.JMenu jMenu15;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenu jMenu3;
    private javax.swing.JMenu jMenu4;
    private javax.swing.JMenu jMenu5;
    private javax.swing.JMenu jMenu6;
    private javax.swing.JMenu jMenu8;
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
    private javax.swing.JMenuItem jMenuItem26;
    private javax.swing.JMenuItem jMenuItem27;
    private javax.swing.JMenuItem jMenuItem28;
    private javax.swing.JMenuItem jMenuItem29;
    private javax.swing.JMenuItem jMenuItem3;
    private javax.swing.JMenuItem jMenuItem30;
    private javax.swing.JMenuItem jMenuItem31;
    private javax.swing.JMenuItem jMenuItem32;
    private javax.swing.JMenuItem jMenuItem33;
    private javax.swing.JMenuItem jMenuItem34;
    private javax.swing.JMenuItem jMenuItem35;
    private javax.swing.JMenuItem jMenuItem36;
    private javax.swing.JMenuItem jMenuItem37;
    private javax.swing.JMenuItem jMenuItem38;
    private javax.swing.JMenuItem jMenuItem39;
    private javax.swing.JMenuItem jMenuItem4;
    private javax.swing.JMenuItem jMenuItem40;
    private javax.swing.JMenuItem jMenuItem41;
    private javax.swing.JMenuItem jMenuItem42;
    private javax.swing.JMenuItem jMenuItem43;
    private javax.swing.JMenuItem jMenuItem44;
    private javax.swing.JMenuItem jMenuItem45;
    private javax.swing.JMenuItem jMenuItem46;
    private javax.swing.JMenuItem jMenuItem48;
    private javax.swing.JMenuItem jMenuItem49;
    private javax.swing.JMenuItem jMenuItem5;
    private javax.swing.JMenuItem jMenuItem50;
    private javax.swing.JMenuItem jMenuItem6;
    private javax.swing.JMenuItem jMenuItem7;
    private javax.swing.JMenuItem jMenuItem8;
    private javax.swing.JMenuItem jMenuItem9;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JToolBar.Separator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JSeparator jSeparator4;
    private javax.swing.JSeparator jSeparator5;
    private javax.swing.JSeparator jSeparator6;
    private javax.swing.JToolBar.Separator jSeparator7;
    private javax.swing.JPopupMenu.Separator jSeparator8;
    private javax.swing.JPopupMenu.Separator jSeparator9;
    private javax.swing.JButton lockButton;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JToolBar mainToolbar;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JPanel nav_accounting;
    private javax.swing.JPanel nav_contacts;
    private javax.swing.JPanel nav_extras;
    private com.l2fprod.common.swing.JOutlookBar nav_outlookbar;
    private javax.swing.JPanel nav_products;
    private javax.swing.JPanel nav_simple_inner;
    private javax.swing.JPanel naviPanel;
    private javax.swing.JPanel officelabel;
    private javax.swing.JPanel parent_nav_accounting;
    private javax.swing.JPanel parent_nav_contacts;
    private javax.swing.JPanel parent_nav_extras;
    private javax.swing.JPanel parent_nav_products;
    private javax.swing.JPanel parent_nav_simple;
    public javax.swing.JPanel pluginIcons;
    private javax.swing.JProgressBar progressBar;
    private javax.swing.JButton scustomers;
    private javax.swing.JPanel separator;
    private javax.swing.JPanel separator1;
    private javax.swing.JPanel serverlabel;
    private javax.swing.JMenuItem simplenav;
    private javax.swing.JButton sinvoices;
    private javax.swing.JButton sjournal;
    private javax.swing.JButton sproducts;
    private javax.swing.JLabel statusMessageLabel;
    private javax.swing.JPanel statusPanel;
    public static final javax.swing.JPanel tabpanePanel = new javax.swing.JPanel();
    private javax.swing.JMenu toolsMenu;
    private javax.swing.JMenu viewMenu;
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
     * Open the currently selected tab in a new frame
     */
    public void selectedTabInNewFrame() {
        final Component pane = getTabPane().getSelectedComponent();
        if (pane != null) {
            final String title = getTabPane().getTitleAt(getTabPane().getSelectedIndex());
            getTabPane().remove(pane);
            JFrame fr = new JFrame(title) {
                private static final long serialVersionUID = 1L;

                @Override
                public void dispose() {
                    getTabPane().addTab(title, pane);
                    getTabPane().setSelectedComponent(pane);
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
     *
     * @param running
     */
//    @Override
//    public void showServerStatus(boolean running) {
//        if (running) {
//            getServerlabel().setMaximumSize(new Dimension(18, 18));
//            getServerlabel().setPreferredSize(new Dimension(18, 18));
//            getServerlabel().setMinimumSize(new Dimension(18, 18));
//            getServerlabel().setSize(18, 18);
//            final JLabel plab = new JLabel();
//            plab.setIcon(new MPIcon("/mpv5/resources/images/16/kwikdisk.png"));
//            plab.setEnabled(true);
//            plab.setToolTipText("<html><b>MP Server " + Messages.LOADED + "</b><br/>Port: " + LocalSettings.getProperty(LocalSettings.SERVER_PORT) + "</html>");
//            plab.addMouseListener(new MouseAdapter() {
//                @Override
//                public void mouseReleased(MouseEvent e) {
//                    if (e.getButton() == MouseEvent.BUTTON2 || e.getButton() == MouseEvent.BUTTON3) {
//                        JLabel source = (JLabel) e.getSource();
//                        final JPopupMenu m = new JPopupMenu();
//                        JMenuItem n = new JMenuItem(Messages.UNLOAD.getValue());
//                        n.addActionListener(new ActionListener() {
//                            @Override
//                            public void actionPerformed(ActionEvent e) {
//                                MPServer.stopServer();
//                                showServerStatus(false);
//                                jMenuItem24.setEnabled(true);
//                                plab.setToolTipText("<html><b>MP Server</b>");
//                                MouseListener[] ml = plab.getMouseListeners();
//                                for (int i = 0; i < ml.length; i++) {
//                                    MouseListener mouseListener = ml[i];
//                                    plab.removeMouseListener(mouseListener);
//                                }
//                            }
//                        });
//                        m.add(n);
//                        m.show(plab, e.getX(), e.getY());
//                    }
//                }
//            });
//            getServerlabel().add(plab);
//        } else {
//            getServerlabel().setMaximumSize(new Dimension(0, 0));
//            getServerlabel().setPreferredSize(new Dimension(0, 0));
//            getServerlabel().setMinimumSize(new Dimension(0, 0));
//            getServerlabel().setSize(0, 0);
//
//        }
//        getFrame().validate();
//    }
    /**
     * Triggers the MP Server notification to the view
     *
     * @param running
     * @param description
     */
    @Override
    public void showOfficeStatus(boolean running, final String description) {
        if (running) {
            officelabel.setMaximumSize(new Dimension(18, 18));
            officelabel.setPreferredSize(new Dimension(18, 18));
            officelabel.setMinimumSize(new Dimension(18, 18));
            officelabel.setSize(18, 18);
            final JLabel plab = new JLabel();
            plab.setIcon(new MPIcon("/images/16/info.png"));
            plab.setEnabled(true);
            plab.setToolTipText("<html><b>" + Messages.OO_DONE_LOADING + "<br>" + description + "</html>");
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

                                showOfficeStatus(false, description);

                                plab.setToolTipText("<html><b>Office</b>");
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
            officelabel.add(plab);
        } else {
            officelabel.setMaximumSize(new Dimension(0, 0));
            officelabel.setPreferredSize(new Dimension(0, 0));
            officelabel.setMinimumSize(new Dimension(0, 0));
            officelabel.setSize(0, 0);

        }
        getFrame().validate();
    }

    /**
     * @return the mainToolbar
     */
    public javax.swing.JToolBar getMainToolbar() {
        return mainToolbar;
    }

    /**
     * @return the naviPanel
     */
    public javax.swing.JPanel getNaviPanel() {
        return naviPanel;
    }

    /**
     * @return the outlookbar
     */
    public JOutlookBar getOutlookBar() {
        return getNav_outlookbar();
    }

    /**
     * @return the pluginIcons
     */
    public javax.swing.JPanel getPluginIcons() {
        return pluginIcons;
    }

    /**
     * @return the separator
     */
    public javax.swing.JPanel getSeparator() {
        return separator;
    }

    /**
     * @return the serverlabel
     */
    public javax.swing.JPanel getServerlabel() {
        return serverlabel;
    }

    /**
     * @return the xhistory
     */
    public javax.swing.JComboBox getXhistory() {
        return xhistory;
    }

    /**
     * @return the calculatorButton
     */
    public javax.swing.JButton getCalculatorButton() {
        return calculatorButton;
    }

    /**
     * @return the clipboardMenu
     */
    public javax.swing.JMenu getClipboardMenu() {
        return clipboardMenu;
    }

    /**
     * @return the closeButton
     */
    public javax.swing.JButton getCloseButton() {
        return closeButton;
    }

    /**
     * @return the editMenu
     */
    public javax.swing.JMenu getEditMenu() {
        return editMenu;
    }

    /**
     * @return the errorlabel
     */
    public javax.swing.JLabel getErrorlabel() {
        return errorlabel;
    }

    /**
     * @return the helpmenu
     */
    public javax.swing.JMenu getHelpmenu() {
        return helpmenu;
    }

    /**
     * @return the lockButton
     */
    public javax.swing.JButton getLockButton() {
        return lockButton;
    }

    /**
     * @return the nav_accounting
     */
    public javax.swing.JPanel getNav_accounting() {
        return nav_accounting;
    }

    /**
     * @return the nav_contacts
     */
    public javax.swing.JPanel getNav_contacts() {
        return nav_contacts;
    }

    /**
     * @return the nav_extras
     */
    public javax.swing.JPanel getNav_extras() {
        return nav_extras;
    }

    /**
     * @return the nav_outlookbar
     */
    public com.l2fprod.common.swing.JOutlookBar getNav_outlookbar() {
        return nav_outlookbar;
    }

    /**
     * @return the nav_products
     */
    public javax.swing.JPanel getNav_products() {
        return nav_products;
    }

    /**
     * @return the parent_nav_accounting
     */
    public javax.swing.JPanel getParent_nav_accounting() {
        return parent_nav_accounting;
    }

    /**
     * @return the parent_nav_contacts
     */
    public javax.swing.JPanel getParent_nav_contacts() {
        return parent_nav_contacts;
    }

    /**
     * @return the parent_nav_extras
     */
    public javax.swing.JPanel getParent_nav_extras() {
        return parent_nav_extras;
    }

    /**
     * @return the parent_nav_products
     */
    public javax.swing.JPanel getParent_nav_products() {
        return parent_nav_products;
    }

    /**
     * @return the toolsMenu
     */
    public javax.swing.JMenu getToolsMenu() {
        return toolsMenu;
    }

    /**
     * @return the viewMenu
     */
    public javax.swing.JMenu getViewMenu() {
        return viewMenu;
    }
    /**
     * Indicates the contact navigation section
     */
    public static final int NAV_CONTACTS = 0;
    /**
     * Indicates the product navigation section
     */
    public static final int NAV_PRODUCTS = 1;
    /**
     * Indicates the accounting navigation section
     */
    public static final int NAV_ACCOUNTING = 2;
    /**
     * Indicates the extras navigation section
     */
    public static final int NAV_EXTRAS = 3;

    /**
     * Add a Button to the navigation panel
     *
     * @param TARGET The target navigation section, which can be one of the
     * following:<li>NAV_CONTACTS
     * <li>NAV_PRODUCTS <li>NAV_ACCOUNTING
     * <li>NAV_EXTRAS
     *
     * @param button The button to add
     */
    public void addButton(int TARGET, JButton button) {

        switch (TARGET) {

            case NAV_CONTACTS:
                getNav_contacts().add(button);
                identifierFrame.validate();
                break;

            case NAV_ACCOUNTING:
                getNav_accounting().add(button);
                identifierFrame.validate();
                break;

            case NAV_PRODUCTS:
                getNav_products().add(button);
                identifierFrame.validate();
                break;

            case NAV_EXTRAS:
                getNav_extras().add(button);
                identifierFrame.validate();
                break;

            default:
                throw new UnsupportedOperationException("Target not defined.");
        }
    }

    private void resettables(JComponent component) {

        try {
            if (component instanceof MPTable) {
                ((MPTable) component).reset();
            } else if (component instanceof JTable) {
//                ((JTable) component).createDefaultColumnsFromModel();
            } else {
                Component[] comps = component.getComponents();
                for (int i = 0; i < comps.length; i++) {
                    Component component1 = comps[i];
                    if (component1 instanceof JComponent) {
                        resettables((JComponent) component1);
                    }
                }
            }
        } catch (Exception e) {
            Log.Debug(e);

        }
    }

    /*
    * Reset all table layouts
     */
    public void resetTables() {
        resettables(getTabpanePanel());
    }

    /**
     * Paste the current clipboard items (if any)
     *
     * @return
     */
    @Override
    public DatabaseObject[] getClipboardItems() {

        List<DatabaseObject> list = new ArrayList<DatabaseObject>();

        for (int i = 1; i < getClipboardMenu().getItemCount(); i++) {
            try {
                if (getClipboardMenu().getItemCount() > 1) {
                    ClipboardMenuItem item = (ClipboardMenuItem) getClipboardMenu().getItem(i);
                    list.add(item.getItem());
                }
            } catch (Exception ignore) {
//            Log.Debug(ignore);
            }
        }

        return list.toArray(new DatabaseObject[]{});
    }

    @Override
    public void addOrShowTab(DatabaseObject dbo) {
        addTab(dbo);
    }

    @Override
    public void removeTab(DataPanel t) {
        int count = getTabPane().getTabCount();
        for (int i = 0; i < count; i++) {
            Component c = getTabPane().getComponent(i);
            if (c == t) {
                getTabPane().remove(c);
            } else if (c instanceof JScrollPane && ((JScrollPane) c).getViewport().getView() == t) {
                getTabPane().remove(c);
            }
        }
        getTabPane().validate();
    }

    @Override
    public synchronized void setClipBoardVisible(boolean show) {
        if (clipboard == null) {
            clipboard = new JFrame(Messages.YABS.getValue());
            clipboard.setLayout(new BorderLayout());
            clipboard.add(clistview, BorderLayout.CENTER);
            clipboard.pack();
            clipboard.setLocationRelativeTo(YabsViewProxy.instance().getIdentifierFrame());
            clipboard.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
            clipboard.setAlwaysOnTop(User.getCurrentUser().getProperty("org.openyabs.uiproperty", "clipboardontop"));
        }
        try {
            clistview.validate();
            if (User.getCurrentUser().getProperty("org.openyabs.uiproperty", "clipboardtab")) {
                addOrShowTab(clistview, "Clipboard");
            } else {
                clipboard.setVisible(show);
            }
        } catch (Exception ex) {
            Log.Debug(ex);
        }
    }
    private static LinkedList<DatabaseObject> FLOW = new LinkedList<DatabaseObject>();
    private int pif = 0;

    public int getPointInFlow() {
        return pif;
    }

    public int addToFlow(DatabaseObject d) {
        if (!FLOW.contains(d)) {
            FLOW.addLast(d);
        } else {
            FLOW.set(FLOW.lastIndexOf(d), d);
        }
        return FLOW.lastIndexOf(d);
    }

    @Override
    public void showPreviousDatabaseObject() {
        if (hasPreviousDatabaseObject()) {
            addOrShowTab(FLOW.get(pif - 1));
        }
    }

    @Override
    public boolean hasPreviousDatabaseObject() {
        return !FLOW.isEmpty() && pif > 0;
    }

    @Override
    public void showNextDatabaseObject() {
        if (hasNextDatabaseObject()) {
            addOrShowTab(FLOW.get(pif + 1));
        }
    }

    @Override
    public boolean hasNextDatabaseObject() {
        return pif + 1 < FLOW.size();
    }

    @Override
    public DatabaseObject getCurrentDatabaseObject() {
        return FLOW.get(pif);
    }

    private void setPointInFlow(int npif) {
        pif = npif;
    }

    public void enhanceToolbars() {//FIXME handle more nicely, and somewhere else
        PropertyStore nactions = GlobalSettings.getProperties("org.openyabs.uiproperty.toolbaraction");
        Map<String, String> m = nactions.getMap();
        for (String f : m.keySet()) {
            try {
                final String val = m.get(f);
                if (val != null && val.length() > 0 && !val.equals("null")) {
                    f = f.substring(1);
                    String[] x = f.split("\\.");
                    String tb = x[0];
                    String act = x[1];
                    for (JToolBar t : getToolBars()) {
                        if (t.getName() == null ? tb == null : t.getName().equals(tb)) {
                            t.add(new AbstractAction(act) {
                                private static final long serialVersionUID = 1L;

                                @Override
                                public void actionPerformed(ActionEvent e) {
                                    try {
                                        getGroovyShell().evaluate(val);
                                    } catch (Exception exception) {
                                        Notificator.raiseNotification(exception, true);
                                    }
                                }
                            });
                        }
                    }
                }
            } catch (Exception e) {
                Log.Debug(e);
            }
        }

//        if (Platform.isMacOS()) {
        try {
            Class util = Class.forName("com.apple.eawt.FullScreenUtilities");
            Class params[] = new Class[]{Window.class, Boolean.TYPE};
            Method method = util.getMethod("setWindowCanFullScreen", params);
            method.invoke(util, this, true);
        } catch (Exception e) {
            // Messages.loge("Could not enable OSX fullscreen", e);
        }
        //     }
    }

    @Override
    public YabsApplication getYabsApplication() {
        if (getApplication() instanceof YabsApplication) {
            return (YabsApplication) getApplication();
        }
        throw new RuntimeException("Not a YabsApplication: " + getApplication());
    }

    @Override
    public void showServerStatus(boolean b) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private void initNav() {
        java.util.ResourceBundle bundle = mpv5.i18n.LanguageManager.getBundle(); // NOI18N            
        nav_outlookbar.remove(parent_nav_accounting);
        nav_outlookbar.remove(parent_nav_products);
        nav_outlookbar.remove(parent_nav_contacts);
        nav_outlookbar.remove(parent_nav_extras);
        nav_outlookbar.remove(parent_nav_simple);

        if (GlobalSettings.getBooleanProperty("navigation.simplified", false)) {

            nav_outlookbar.addTab(bundle.getString("MPView.parent_nav_simple.TabConstraints.tabTitle"), parent_nav_simple); // NOI18N
            nav_outlookbar.addTab(bundle.getString("MPView.parent_nav_extras.TabConstraints.tabTitle"), parent_nav_extras); // NOI18N

            simplenav.setIcon(MPIcon.ICON_ENABLED);
        } else {

            nav_outlookbar.addTab(bundle.getString("MPView.parent_nav_contacts.TabConstraints.tabTitle_1"), parent_nav_contacts); // NOI18N 
            nav_outlookbar.addTab(bundle.getString("MPView.parent_nav_accounting.TabConstraints.tabTitle_1"), parent_nav_accounting); // NOI18N
            nav_outlookbar.addTab(bundle.getString("MPView.parent_nav_products.TabConstraints.tabTitle_1"), parent_nav_products); // NOI18N
            nav_outlookbar.addTab(bundle.getString("MPView.parent_nav_extras.TabConstraints.tabTitle"), parent_nav_extras); // NOI18N

            simplenav.setIcon(MPIcon.ICON_DISABLED);
        }
    }

}
