/*
 * MPView.java
 */
package mpv5.ui.frames;

import com.l2fprod.common.swing.JOutlookBar;
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
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

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
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import mpv5.Main;
import mpv5.bugtracker.ExceptionHandler;
import mpv5.bugtracker.SubmitForm;
import mpv5.data.MPList;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.db.common.NodataFoundException;
import mpv5.db.common.QueryHandler;
import mpv5.db.objects.Contact;
import mpv5.globals.Messages;
import mpv5.db.objects.Favourite;
import mpv5.db.objects.Item;
import mpv5.db.objects.Product;
import mpv5.logging.Log;
import mpv5.ui.dialogs.DialogForFile;

import mpv5.ui.dialogs.Popup;
import mpv5.ui.dialogs.Search;
import mpv5.ui.menus.ClipboardMenuItem;
import mpv5.ui.menus.FavouritesMenuItem;
import mpv5.ui.panels.ContactsList;
import mpv5.ui.panels.DataPanel;
import mpv5.ui.panels.HistoryPanel;
import mpv5.ui.panels.MPControlPanel;
import mpv5.ui.misc.CloseableTabbedPane;
import mpv5.ui.misc.FadeOnChangeLabel;
import mpv5.ui.misc.Position;

import mpv5.ui.panels.calendar.ScheduleCalendar;
import mpv5.usermanagement.MPSecurityManager;
import mpv5.db.objects.User;
import mpv5.db.objects.ValueProperty;
import mpv5.db.sample.SampleData;
import mpv5.globals.Constants;
import mpv5.globals.LocalSettings;
import mpv5.pluginhandling.MPPLuginLoader;
import mpv5.server.MPServer;
import mpv5.ui.dialogs.About;
import mpv5.ui.dialogs.BigPopup;
import mpv5.ui.dialogs.ListView;
import mpv5.ui.dialogs.Search2;
import mpv5.ui.dialogs.Wizard;
import mpv5.ui.dialogs.subcomponents.wizard_CSVImport2_1;
import mpv5.ui.dialogs.subcomponents.wizard_XMLImport_1;
import mpv5.ui.dialogs.subcomponents.wizard_XMLImport_2;
import mpv5.ui.panels.ItemPanel;
import mpv5.ui.dialogs.subcomponents.wizard_MP45_Import;
import mpv5.ui.dialogs.subcomponents.wizard_Yabs1_Import;
import mpv5.ui.misc.MPTable;
import mpv5.ui.panels.ChangeNotApprovedException;
import mpv5.ui.panels.ExpensePanel;
import mpv5.ui.panels.GeneralListPanel;
import mpv5.ui.panels.JournalPanel;
import mpv5.ui.panels.OverviewPanel;
import mpv5.ui.panels.ProductList;
import mpv5.ui.panels.ProductListsPanel;
import mpv5.ui.panels.ProductPanel;
import mpv5.ui.panels.ProductsOverview;
import mpv5.ui.panels.QueryPanel;
import mpv5.ui.panels.RevenuePanel;
import mpv5.ui.panels.StartPage;
import mpv5.ui.panels.TrashPanel;
import mpv5.utils.export.Export;
import mpv5.utils.export.VCFParser;
import mpv5.utils.files.TextDatFile;
import mpv5.utils.images.MPIcon;
import mpv5.utils.print.PrintJob;
import mpv5.utils.renderer.ComboBoxRendererForTooltip;
import mpv5.utils.xml.XMLWriter;
import net.sf.vcard4j.java.VCard;
import org.jdesktop.application.SingleFrameApplication;
import org.jdesktop.application.FrameView;

/**
 * The application's main frame.
 */
public class MPView extends FrameView {

    public static MPView identifierView;
    private static Dimension initialSize = new Dimension(1100, 900);
    public static JFrame identifierFrame;
    private static CloseableTabbedPane tabPane;
    private static JLabel messagelabel = new FadeOnChangeLabel();
    private static JComboBox history = new JComboBox();
    private static JProgressBar progressbar = new JProgressBar();
    private static JMenu favMenu;
    private static String predefTitle;
    private static DialogForFile filedialog;
    public static SingleFrameApplication identifierApplication;
    private static boolean navBarAnimated = true;
    private static boolean tabPaneScrolled = false;
    private static JLabel staterrorlabel = new JLabel();
    private static MPList currentList = new MPList();
    private static ListView clistview = new ListView(currentList);
    private static boolean loaded = false;

    /**
     * Display a message at the bottom of the MP frame
     * @param message
     */
    public static void addMessage(Messages message) {
        addMessage(message.getValue());
    }

    /**
     * We currently support the usage of 1 temporary list of DatabaseObjects.
     * This method shows a popup containing the actual content.
     */
    public static void showCurrentList() {
        try {
            getClistview().validate();
            BigPopup.showPopup(MPView.getIdentifierFrame().getRootPane(), getClistview(), Messages.YABS.toString());
            BigPopup.pack(getClistview());
        } catch (Exception ex) {
            Log.Debug(ex);
        }
    }

    /**
     * Display a message at the bottom of the MP frame
     * @param message
     */
    public static synchronized void addMessage(final String message) {
        if (loaded) {//dont do anything if main frame is not constructed
            Runnable runnable = new Runnable() {

                public void run() {
                    getMessagelabel().setText(message);
                    getHistory().addItem(message);
                    getHistory().setSelectedItem(message);
                    identifierFrame.validate();
                }
            };
            SwingUtilities.invokeLater(runnable);
        }
    }

    /**
     * Returns the currently selected tab on the main tab pane
     * @return
     */
    public static Object getShowingTab() {
        return getTabPane().getSelectedComponent();
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
        getFiledialog().setSelectedFile(new File(f.getName()));
        getFiledialog().saveFile(f);
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
        getIdentifierApplication().show(c);
    }
    /**
     * The handler for all plugins
     */
    private static MPPLuginLoader pluginLoader;

    /**
     * Let the view notify the user about an unexpected error
     */
    public static void showError() {
        if (Main.INSTANTIATED) {
            try {
                getStaterrorlabel().setIcon(new javax.swing.ImageIcon(MPView.class.getResource("/mpv5/resources/images/16/remove.png"))); // NOI18N
                getIdentifierFrame().validate();
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
    public static JProgressBar getProgressbar() {
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
    public static MPView getIdentifierView() {
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
    public static JFrame getIdentifierFrame() {
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
    public static DialogForFile getFiledialog() {
        return filedialog;
    }

    /**
     * @return the identifierApplication
     */
    public static SingleFrameApplication getIdentifierApplication() {
        return identifierApplication;
    }

    /**
     * @return the navBarAnimated
     */
    public static boolean isNavBarAnimated() {
        return navBarAnimated;
    }

    /**
     * @return the tabPaneScrolled
     */
    public static boolean isTabPaneScrolled() {
        return tabPaneScrolled;
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
     * @return the clistview
     */
    public static ListView getClistview() {
        return clistview;
    }

    /**
     * @return the pluginLoader
     */
    public static MPPLuginLoader getPluginLoader() {
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

    /**
     * Reloads fav menu
     */
    public void refreshFavouritesMenu() {
        if (getFavMenu() != null) {
            getFavMenu().removeAll();
            favouritesMenu.add(jMenuItem5);
            fillFavouritesmenu();
        }
    }

    /**
     * Sets the max value for the progressbar
     * @param max
     */
    public synchronized static void setProgressMaximumValue(int max) {
        getProgressbar().setMaximum(max);
    }

    /**
     *  Sets the current value for the progressbar
     * @param val
     */
    public synchronized static void setProgressValue(int val) {
        getProgressbar().setValue(val);
    }

    /**
     * Reset the progress bar
     */
    public synchronized static void setProgressReset() {
        getProgressbar().setValue(0);
        getProgressbar().setIndeterminate(false);
    }

    /**
     * Sets the indeterminate property of the progress bar.
     * @param b
     */
    public synchronized static void setProgressRunning(boolean b) {
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
     * @param truee
     */
    public static void setWaiting(boolean truee) {
        if (Main.INSTANTIATED && getIdentifierFrame() != null) {
            if (truee) {
                getIdentifierFrame().setCursor(new Cursor(Cursor.WAIT_CURSOR));
            } else {
                getIdentifierFrame().setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }
        }
    }

    /**
     * Returns the curently selected tab or null if this is not a {@link DataPanel}
     * @return
     */
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
     * Returns the tab at the specified position, or NULL if the tab is not existing OR not a {@link DataPanel}
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

    private static void fillFavouritesmenu() {
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
     * @param obj
     */
    public void addToClipBoard(DatabaseObject obj) {
        getClipboardMenu().add(new ClipboardMenuItem(obj));
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
        staterrorlabel = errorlabel;
        history = xhistory;
        history.setRenderer(new ComboBoxRendererForTooltip());
        if (tabPaneScrolled) {
            tabpanePanel.add(new JScrollPane(getTabPane()), BorderLayout.CENTER);
            jMenuItem25.setIcon(MPIcon.ICON_ENABLED);
        } else {
            tabpanePanel.add(tabPane, BorderLayout.CENTER);
        }
        favMenu = favouritesMenu;
        identifierView = this;
        filedialog = new DialogForFile(DialogForFile.FILES_ONLY);
        jMenuItem24.setEnabled(!LocalSettings.getBooleanProperty(LocalSettings.OFFICE_REMOTE));

        nav_outlookbar.setAnimated(navBarAnimated);
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

        addTab(new StartPage(), Messages.WELCOME);
        LocalSettings.save();

        if (Log.getLoglevel() != Log.LOGLEVEL_DEBUG) {
            sampleDataItem.getParent().remove(sampleDataItem);
        }

        identifierFrame.validate();
        loaded = true;
    }

    /**
     * Set the state of the main toolbar
     * @param enable
     */
    public void enableToolBar(boolean enable) {
        getMainToolbar().setEnabled(enable);
    }

    private void setNaviPanelSize() {
        getNav_outlookbar().setPreferredSize(new Dimension(getNav_outlookbar().getWidth(), getNaviPanel().getHeight() - 20));
        getNav_outlookbar().setMaximumSize(new Dimension(getNav_outlookbar().getWidth(), getNaviPanel().getHeight() - 20));
        getNav_outlookbar().setMinimumSize(new Dimension(getNav_outlookbar().getWidth(), getNaviPanel().getHeight() - 20));
        getNav_outlookbar().setSize(new Dimension(getNav_outlookbar().getWidth(), getNaviPanel().getHeight() - 20));
        getNaviPanel().revalidate();
        getNaviPanel().repaint();
    }

    /**
     * Add a tab to the main tab pane, automatically determines the needed View
     * @param item
     * @param tabTitle
     * @return The added tab
     */
    public DataPanel addTab(final DatabaseObject item, Object tabTitle) {
        setWaiting(true);
        boolean found = false;
        boolean proceed = true;
        if (mpv5.db.objects.User.getCurrentUser().getProperties().getProperty(MPView.getTabPane(), "avoidmultipleviews")) {
            Log.Debug(this, "Looking for an existing view for: " + item);
            int count = getTabPane().getTabCount();
            for (int i = 0; i < count; i++) {
                if (getTabAt(i) != null) {
                    DataPanel panel = getTabAt(i);
                    if (item.equals(panel.getDataOwner())) {
                        getTabPane().setSelectedIndex(i);
                        if (tabTitle == null) {
                            getTabPane().setTitleAt(i, item.__getCName());
                        } else {
                            getTabPane().setTitleAt(i, tabTitle + ": " + item.__getCName());
                        }
                        proceed = false;
                        break;
                    }
                }
            }
        }

        if (proceed) {
            if (item.getView() != null && mpv5.db.objects.User.getCurrentUser().getProperties().getProperty(MPView.getTabPane(), "norecycletabs")) {
                if (tabTitle == null) {
                    final DataPanel p = ((DataPanel) item.getView());
                    addTab((JComponent) p, item.__getCName());
                    Runnable runnable = new Runnable() {

                        public void run() {
                            p.setDataOwner(item, true);
                        }
                    };
                    SwingUtilities.invokeLater(runnable);
                } else {
                    final DataPanel p = ((DataPanel) item.getView());
                    addTab((JComponent) p, tabTitle + ": " + item.__getCName());
                    Runnable runnable = new Runnable() {

                        public void run() {
                            p.setDataOwner(item, true);
                        }
                    };
                    SwingUtilities.invokeLater(runnable);
                }
                getCurrentTab().setDataOwner(item, true);
            } else if (item.getView() != null) {
                int count = getTabPane().getTabCount();
                for (int i = 0; i < count; i++) {
                    if (getTabAt(i) != null) {
                        DataPanel panel = getTabAt(i);
                        if (!panel.getDataOwner().isExisting()
                                && panel.getDataOwner().getContext().equals(item.getContext())
                                && panel.getClass() == item.getView().getClass()) {
//                        if (!panel.getDataOwner().isExisting() && panel.getDataOwner().getContext().equals(item.getContext())) {
                            getTabPane().setSelectedIndex(i);
                            panel.setDataOwner(item, true);
                            if (tabTitle == null) {
                                getTabPane().setTitleAt(i, item.__getCName());
                            } else {
                                getTabPane().setTitleAt(i, tabTitle + ": " + item.__getCName());
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
                            addTab((JComponent) p, item.__getCName());
                        } else {
                            addTab((JComponent) p, tabTitle + ": " + item.__getCName());
                        }
                        Runnable runnable = new Runnable() {

                            public void run() {
                                p.setDataOwner(item, true);
                            }
                        };
                        SwingUtilities.invokeLater(runnable);
                    } catch (ClassCastException e) {
                        if (tabTitle == null) {
                            addTab(item.getView(), item.__getCName());
                        } else {
                            addTab(item.getView(), tabTitle + ": " + item.__getCName());
                        }
                    }
                }
            }
        }

        setWaiting(false);
        return getCurrentTab();
    }

    /**
     * Add a tab to the main tab pane, automatically determines the needed View
     * @param item
     * @return
     */
    public DataPanel addTab(DatabaseObject item) {
        return addTab(item, null);
    }

    /**
     * Shows a tab or adds it if needed
     * @param instanceOf
     * @param label
     */
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
     * @param tab
     * @param name
     * @return The now selected index
     */
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
        nav_outlookbar = new com.l2fprod.common.swing.JOutlookBar();
        parent_nav_contacts = new javax.swing.JPanel();
        nav_contacts = new javax.swing.JPanel();
        jButton5 = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();
        jButton18 = new javax.swing.JButton();
        parent_nav_accounting = new javax.swing.JPanel();
        nav_accounting = new javax.swing.JPanel();
        jButton8 = new javax.swing.JButton();
        jButton11 = new javax.swing.JButton();
        jButton10 = new javax.swing.JButton();
        jButton15 = new javax.swing.JButton();
        jButton16 = new javax.swing.JButton();
        jButton17 = new javax.swing.JButton();
        parent_nav_products = new javax.swing.JPanel();
        nav_products = new javax.swing.JPanel();
        jButton13 = new javax.swing.JButton();
        jButton14 = new javax.swing.JButton();
        jButton19 = new javax.swing.JButton();
        jButton20 = new javax.swing.JButton();
        jButton12 = new javax.swing.JButton();
        parent_nav_extras = new javax.swing.JPanel();
        nav_extras = new javax.swing.JPanel();
        jButton6 = new javax.swing.JButton();
        jButton7 = new javax.swing.JButton();
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
        jMenu8 = new javax.swing.JMenu();
        jMenuItem11 = new javax.swing.JMenuItem();
        jMenuItem38 = new javax.swing.JMenuItem();
        jMenuItem40 = new javax.swing.JMenuItem();
        jMenu12 = new javax.swing.JMenu();
        jMenuItem28 = new javax.swing.JMenuItem();
        jMenuItem29 = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JSeparator();
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
        jMenuItem2 = new javax.swing.JMenuItem();
        jMenuItem39 = new javax.swing.JMenuItem();
        jMenuItem25 = new javax.swing.JMenuItem();
        jMenuItem19 = new javax.swing.JMenuItem();
        jMenuItem4 = new javax.swing.JMenuItem();
        jMenu15 = new javax.swing.JMenu();
        jMenuItem35 = new javax.swing.JMenuItem();
        jMenuItem36 = new javax.swing.JMenuItem();
        toolsMenu = new javax.swing.JMenu();
        jMenuItem3 = new javax.swing.JMenuItem();
        jMenuItem23 = new javax.swing.JMenuItem();
        jMenuItem31 = new javax.swing.JMenuItem();
        jMenuItem24 = new javax.swing.JMenuItem();
        jSeparator6 = new javax.swing.JSeparator();
        jMenuItem18 = new javax.swing.JMenuItem();
        jMenuItem37 = new javax.swing.JMenuItem();
        sampleDataItem = new javax.swing.JMenuItem();
        favouritesMenu = new javax.swing.JMenu();
        jMenuItem5 = new javax.swing.JMenuItem();
        clipboardMenu = new javax.swing.JMenu();
        jMenu6 = new javax.swing.JMenu();
        jMenuItem7 = new javax.swing.JMenuItem();
        helpmenu = new javax.swing.JMenu();
        jMenuItem10 = new javax.swing.JMenuItem();
        jMenuItem27 = new javax.swing.JMenuItem();
        jMenuItem30 = new javax.swing.JMenuItem();
        statusPanel = new javax.swing.JPanel();
        statusMessageLabel = new FadeOnChangeLabel();
        pluginIcons = new javax.swing.JPanel();
        separator = new javax.swing.JPanel();
        progressBar = new javax.swing.JProgressBar();
        separator1 = new javax.swing.JPanel();
        xhistory = new javax.swing.JComboBox();
        serverlabel = new javax.swing.JPanel();
        errorlabel = new javax.swing.JLabel();
        mainToolbar = new javax.swing.JToolBar();
        closeButton = new javax.swing.JButton();
        lockButton = new javax.swing.JButton();
        jSeparator2 = new javax.swing.JToolBar.Separator();
        calculatorButton = new javax.swing.JButton();

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

        nav_outlookbar.setAnimated(false);
        nav_outlookbar.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        nav_outlookbar.setName("nav_outlookbar"); // NOI18N

        parent_nav_contacts.setBackground(new java.awt.Color(153, 153, 153));
        parent_nav_contacts.setName("parent_nav_contacts"); // NOI18N
        parent_nav_contacts.setPreferredSize(new java.awt.Dimension(110, 400));

        nav_contacts.setBackground(new java.awt.Color(153, 153, 153));
        nav_contacts.setName("nav_contacts"); // NOI18N
        nav_contacts.setLayout(new java.awt.GridLayout(0, 1, 2, 5));

        jButton5.setFont(new java.awt.Font("Tahoma", 0, 10));
        jButton5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/mpv5/resources/images/32/agt_family.png"))); // NOI18N
        java.util.ResourceBundle bundle = mpv5.i18n.LanguageManager.getBundle(); // NOI18N
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
        nav_contacts.add(jButton5);

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
        nav_contacts.add(jButton1);

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
        nav_contacts.add(jButton18);

        javax.swing.GroupLayout parent_nav_contactsLayout = new javax.swing.GroupLayout(parent_nav_contacts);
        parent_nav_contacts.setLayout(parent_nav_contactsLayout);
        parent_nav_contactsLayout.setHorizontalGroup(
            parent_nav_contactsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(nav_contacts, javax.swing.GroupLayout.DEFAULT_SIZE, 110, Short.MAX_VALUE)
        );
        parent_nav_contactsLayout.setVerticalGroup(
            parent_nav_contactsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(parent_nav_contactsLayout.createSequentialGroup()
                .addComponent(nav_contacts, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(226, Short.MAX_VALUE))
        );

        nav_outlookbar.addTab(bundle.getString("MPView.parent_nav_contacts.TabConstraints.tabTitle_1"), parent_nav_contacts); // NOI18N

        parent_nav_accounting.setBackground(new java.awt.Color(153, 153, 153));
        parent_nav_accounting.setName("parent_nav_accounting"); // NOI18N
        parent_nav_accounting.setPreferredSize(new java.awt.Dimension(110, 400));

        nav_accounting.setBackground(new java.awt.Color(153, 153, 153));
        nav_accounting.setName("nav_accounting"); // NOI18N
        nav_accounting.setLayout(new java.awt.GridLayout(0, 1, 2, 5));

        jButton8.setFont(new java.awt.Font("Tahoma", 0, 10));
        jButton8.setIcon(new javax.swing.ImageIcon(getClass().getResource("/mpv5/resources/images/32/folder_blue.png"))); // NOI18N
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
        nav_accounting.add(jButton8);

        jButton11.setFont(new java.awt.Font("Tahoma", 0, 10));
        jButton11.setIcon(new javax.swing.ImageIcon(getClass().getResource("/mpv5/resources/images/32/folder_green.png"))); // NOI18N
        jButton11.setText(bundle.getString("MPView.jButton11.text")); // NOI18N
        jButton11.setToolTipText(bundle.getString("MPView.jButton11.toolTipText")); // NOI18N
        jButton11.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton11.setName("jButton11"); // NOI18N
        jButton11.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton11.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton11ActionPerformed(evt);
            }
        });
        nav_accounting.add(jButton11);

        jButton10.setFont(new java.awt.Font("Tahoma", 0, 10));
        jButton10.setIcon(new javax.swing.ImageIcon(getClass().getResource("/mpv5/resources/images/32/folder_grey.png"))); // NOI18N
        jButton10.setText(bundle.getString("MPView.jButton10.text")); // NOI18N
        jButton10.setToolTipText(bundle.getString("MPView.jButton10.toolTipText")); // NOI18N
        jButton10.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton10.setName("jButton10"); // NOI18N
        jButton10.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton10.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton10ActionPerformed(evt);
            }
        });
        nav_accounting.add(jButton10);

        jButton15.setFont(new java.awt.Font("Tahoma", 0, 10));
        jButton15.setIcon(new javax.swing.ImageIcon(getClass().getResource("/mpv5/resources/images/32/folder_red.png"))); // NOI18N
        jButton15.setText(bundle.getString("MPView.jButton15.text")); // NOI18N
        jButton15.setToolTipText(bundle.getString("MPView.jButton15.toolTipText")); // NOI18N
        jButton15.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton15.setName("jButton15"); // NOI18N
        jButton15.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton15.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton15ActionPerformed(evt);
            }
        });
        nav_accounting.add(jButton15);

        jButton16.setFont(new java.awt.Font("Tahoma", 0, 10));
        jButton16.setIcon(new javax.swing.ImageIcon(getClass().getResource("/mpv5/resources/images/32/folder_yellow.png"))); // NOI18N
        jButton16.setText(bundle.getString("MPView.jButton16.text")); // NOI18N
        jButton16.setToolTipText(bundle.getString("MPView.jButton16.toolTipText")); // NOI18N
        jButton16.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton16.setName("jButton16"); // NOI18N
        jButton16.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton16.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton16ActionPerformed(evt);
            }
        });
        nav_accounting.add(jButton16);

        jButton17.setFont(new java.awt.Font("Tahoma", 0, 10));
        jButton17.setIcon(new javax.swing.ImageIcon(getClass().getResource("/mpv5/resources/images/32/reload.png"))); // NOI18N
        jButton17.setText(bundle.getString("MPView.jButton17.text")); // NOI18N
        jButton17.setToolTipText(bundle.getString("MPView.jButton17.toolTipText")); // NOI18N
        jButton17.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton17.setName("jButton17"); // NOI18N
        jButton17.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton17.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton17ActionPerformed(evt);
            }
        });
        nav_accounting.add(jButton17);

        javax.swing.GroupLayout parent_nav_accountingLayout = new javax.swing.GroupLayout(parent_nav_accounting);
        parent_nav_accounting.setLayout(parent_nav_accountingLayout);
        parent_nav_accountingLayout.setHorizontalGroup(
            parent_nav_accountingLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(nav_accounting, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        parent_nav_accountingLayout.setVerticalGroup(
            parent_nav_accountingLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(parent_nav_accountingLayout.createSequentialGroup()
                .addComponent(nav_accounting, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        nav_outlookbar.addTab(bundle.getString("MPView.parent_nav_accounting.TabConstraints.tabTitle_1"), parent_nav_accounting); // NOI18N

        parent_nav_products.setBackground(new java.awt.Color(153, 153, 153));
        parent_nav_products.setName("parent_nav_products"); // NOI18N
        parent_nav_products.setPreferredSize(new java.awt.Dimension(110, 400));

        nav_products.setBackground(new java.awt.Color(153, 153, 153));
        nav_products.setName("nav_products"); // NOI18N
        nav_products.setLayout(new java.awt.GridLayout(0, 1, 2, 5));

        jButton13.setFont(new java.awt.Font("Tahoma", 0, 10));
        jButton13.setIcon(new javax.swing.ImageIcon(getClass().getResource("/mpv5/resources/images/32/tux.png"))); // NOI18N
        jButton13.setText(bundle.getString("MPView.jButton13.text")); // NOI18N
        jButton13.setToolTipText(bundle.getString("MPView.jButton13.toolTipText")); // NOI18N
        jButton13.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton13.setName("jButton13"); // NOI18N
        jButton13.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton13.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton13ActionPerformed(evt);
            }
        });
        nav_products.add(jButton13);

        jButton14.setFont(new java.awt.Font("Tahoma", 0, 10));
        jButton14.setIcon(new javax.swing.ImageIcon(getClass().getResource("/mpv5/resources/images/32/playlist.png"))); // NOI18N
        jButton14.setText(bundle.getString("MPView.jButton14.text")); // NOI18N
        jButton14.setToolTipText(bundle.getString("MPView.jButton14.toolTipText")); // NOI18N
        jButton14.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton14.setName("jButton14"); // NOI18N
        jButton14.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton14.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton14ActionPerformed(evt);
            }
        });
        nav_products.add(jButton14);

        jButton19.setFont(new java.awt.Font("Tahoma", 0, 10));
        jButton19.setIcon(new javax.swing.ImageIcon(getClass().getResource("/mpv5/resources/images/32/kghostview.png"))); // NOI18N
        jButton19.setText(bundle.getString("MPView.jButton19.text")); // NOI18N
        jButton19.setToolTipText(bundle.getString("MPView.jButton19.toolTipText")); // NOI18N
        jButton19.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton19.setName("jButton19"); // NOI18N
        jButton19.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton19.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton19ActionPerformed(evt);
            }
        });
        nav_products.add(jButton19);

        jButton20.setFont(new java.awt.Font("Tahoma", 0, 10));
        jButton20.setIcon(new javax.swing.ImageIcon(getClass().getResource("/mpv5/resources/images/32/window_list.png"))); // NOI18N
        jButton20.setText(bundle.getString("MPView.jButton20.text")); // NOI18N
        jButton20.setToolTipText(bundle.getString("MPView.jButton20.toolTipText")); // NOI18N
        jButton20.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton20.setName("jButton20"); // NOI18N
        jButton20.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton20.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton20ActionPerformed(evt);
            }
        });
        nav_products.add(jButton20);

        jButton12.setFont(new java.awt.Font("Tahoma", 0, 10));
        jButton12.setIcon(new javax.swing.ImageIcon(getClass().getResource("/mpv5/resources/images/32/gear.png"))); // NOI18N
        jButton12.setText(bundle.getString("MPView.jButton12.text")); // NOI18N
        jButton12.setToolTipText(bundle.getString("MPView.jButton12.toolTipText")); // NOI18N
        jButton12.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton12.setName("jButton12"); // NOI18N
        jButton12.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton12.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton12ActionPerformed(evt);
            }
        });
        nav_products.add(jButton12);

        javax.swing.GroupLayout parent_nav_productsLayout = new javax.swing.GroupLayout(parent_nav_products);
        parent_nav_products.setLayout(parent_nav_productsLayout);
        parent_nav_productsLayout.setHorizontalGroup(
            parent_nav_productsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(nav_products, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        parent_nav_productsLayout.setVerticalGroup(
            parent_nav_productsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(parent_nav_productsLayout.createSequentialGroup()
                .addComponent(nav_products, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        nav_outlookbar.addTab(bundle.getString("MPView.parent_nav_products.TabConstraints.tabTitle_1"), parent_nav_products); // NOI18N

        parent_nav_extras.setBackground(new java.awt.Color(153, 153, 153));
        parent_nav_extras.setName("parent_nav_extras"); // NOI18N
        parent_nav_extras.setPreferredSize(new java.awt.Dimension(110, 400));

        nav_extras.setBackground(new java.awt.Color(153, 153, 153));
        nav_extras.setName("nav_extras"); // NOI18N
        nav_extras.setLayout(new java.awt.GridLayout(0, 1, 2, 5));

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
        nav_extras.add(jButton6);

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
        nav_extras.add(jButton7);

        javax.swing.GroupLayout parent_nav_extrasLayout = new javax.swing.GroupLayout(parent_nav_extras);
        parent_nav_extras.setLayout(parent_nav_extrasLayout);
        parent_nav_extrasLayout.setHorizontalGroup(
            parent_nav_extrasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(nav_extras, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        parent_nav_extrasLayout.setVerticalGroup(
            parent_nav_extrasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(parent_nav_extrasLayout.createSequentialGroup()
                .addComponent(nav_extras, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

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
                .addComponent(nav_outlookbar, javax.swing.GroupLayout.DEFAULT_SIZE, 503, Short.MAX_VALUE)
                .addContainerGap())
        );

        javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addComponent(naviPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tabpanePanel, javax.swing.GroupLayout.DEFAULT_SIZE, 678, Short.MAX_VALUE))
        );
        mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(naviPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(tabpanePanel, javax.swing.GroupLayout.DEFAULT_SIZE, 518, Short.MAX_VALUE)
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
        jMenuItem9.setIcon(new javax.swing.ImageIcon(getClass().getResource("/mpv5/resources/images/16/editcopy.png"))); // NOI18N
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

        jMenuItem2.setText(bundle.getString("MPView.jMenuItem2.text_1")); // NOI18N
        jMenuItem2.setName("jMenuItem2"); // NOI18N
        jMenuItem2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem2ActionPerformed(evt);
            }
        });
        jMenu2.add(jMenuItem2);

        jMenuItem39.setText(bundle.getString("MPView.jMenuItem39.text")); // NOI18N
        jMenuItem39.setName("jMenuItem39"); // NOI18N
        jMenuItem39.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem39ActionPerformed(evt);
            }
        });
        jMenu2.add(jMenuItem39);

        viewMenu.add(jMenu2);

        jMenuItem25.setText(bundle.getString("MPView.jMenuItem25.text")); // NOI18N
        jMenuItem25.setName("jMenuItem25"); // NOI18N
        jMenuItem25.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem25ActionPerformed(evt);
            }
        });
        viewMenu.add(jMenuItem25);

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
        jMenuItem4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/mpv5/resources/images/16/2uparrow.png"))); // NOI18N
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

        menuBar.add(viewMenu);

        toolsMenu.setText(bundle.getString("MPView.toolsMenu.text_1")); // NOI18N
        toolsMenu.setName("toolsMenu"); // NOI18N

        jMenuItem3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/mpv5/resources/images/16/configure_shortcuts.png"))); // NOI18N
        jMenuItem3.setText(bundle.getString("MPView.jMenuItem3.text_1")); // NOI18N
        jMenuItem3.setName("jMenuItem3"); // NOI18N
        jMenuItem3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem3ActionPerformed(evt);
            }
        });
        toolsMenu.add(jMenuItem3);

        jMenuItem23.setIcon(new javax.swing.ImageIcon(getClass().getResource("/mpv5/resources/images/16/edittrash.png"))); // NOI18N
        jMenuItem23.setText(bundle.getString("MPView.jMenuItem23.text")); // NOI18N
        jMenuItem23.setName("jMenuItem23"); // NOI18N
        jMenuItem23.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem23ActionPerformed(evt);
            }
        });
        toolsMenu.add(jMenuItem23);

        jMenuItem31.setIcon(new javax.swing.ImageIcon(getClass().getResource("/mpv5/resources/images/16/cal.png"))); // NOI18N
        jMenuItem31.setText(bundle.getString("MPView.jMenuItem31.text")); // NOI18N
        jMenuItem31.setName("jMenuItem31"); // NOI18N
        jMenuItem31.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem31ActionPerformed(evt);
            }
        });
        toolsMenu.add(jMenuItem31);

        jMenuItem24.setIcon(new javax.swing.ImageIcon(getClass().getResource("/mpv5/resources/images/16/kwikdisk.png"))); // NOI18N
        jMenuItem24.setText(bundle.getString("MPView.jMenuItem24.text")); // NOI18N
        jMenuItem24.setName("jMenuItem24"); // NOI18N
        jMenuItem24.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem24ActionPerformed(evt);
            }
        });
        toolsMenu.add(jMenuItem24);

        jSeparator6.setName("jSeparator6"); // NOI18N
        toolsMenu.add(jSeparator6);

        jMenuItem18.setIcon(new javax.swing.ImageIcon(getClass().getResource("/mpv5/resources/images/16/kalarm.png"))); // NOI18N
        jMenuItem18.setText(bundle.getString("MPView.jMenuItem18.text")); // NOI18N
        jMenuItem18.setName("jMenuItem18"); // NOI18N
        jMenuItem18.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem18ActionPerformed(evt);
            }
        });
        toolsMenu.add(jMenuItem18);

        jMenuItem37.setIcon(new javax.swing.ImageIcon(getClass().getResource("/mpv5/resources/images/16/cal.png"))); // NOI18N
        jMenuItem37.setText(bundle.getString("MPView.jMenuItem37.text")); // NOI18N
        jMenuItem37.setName("jMenuItem37"); // NOI18N
        jMenuItem37.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem37ActionPerformed(evt);
            }
        });
        toolsMenu.add(jMenuItem37);

        sampleDataItem.setText(bundle.getString("MPView.sampleDataItem.text")); // NOI18N
        sampleDataItem.setName("sampleDataItem"); // NOI18N
        sampleDataItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sampleDataItemActionPerformed(evt);
            }
        });
        toolsMenu.add(sampleDataItem);

        menuBar.add(toolsMenu);

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
        jMenuItem10.setIcon(new javax.swing.ImageIcon(getClass().getResource("/mpv5/resources/images/16/cal.png"))); // NOI18N
        jMenuItem10.setText(bundle.getString("MPView.jMenuItem10.text")); // NOI18N
        jMenuItem10.setName("jMenuItem10"); // NOI18N
        jMenuItem10.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem10ActionPerformed(evt);
            }
        });
        helpmenu.add(jMenuItem10);

        jMenuItem27.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F1, 0));
        jMenuItem27.setIcon(new javax.swing.ImageIcon(getClass().getResource("/mpv5/resources/images/16/info.png"))); // NOI18N
        jMenuItem27.setText(bundle.getString("MPView.jMenuItem27.text")); // NOI18N
        jMenuItem27.setName("jMenuItem27"); // NOI18N
        jMenuItem27.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem27ActionPerformed(evt);
            }
        });
        helpmenu.add(jMenuItem27);

        jMenuItem30.setIcon(new javax.swing.ImageIcon(getClass().getResource("/mpv5/resources/images/about.gif"))); // NOI18N
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
        xhistory.setMaximumRowCount(30);
        xhistory.setAutoscrolls(true);
        xhistory.setEditor(null);
        xhistory.setFocusable(false);
        xhistory.setMaximumSize(new java.awt.Dimension(500, 32767));
        xhistory.setMinimumSize(new java.awt.Dimension(100, 14));
        xhistory.setName("xhistory"); // NOI18N
        xhistory.setPreferredSize(new java.awt.Dimension(300, 23));
        xhistory.setRequestFocusEnabled(false);
        xhistory.setVerifyInputWhenFocusTarget(false);
        statusPanel.add(xhistory);

        serverlabel.setMaximumSize(new java.awt.Dimension(20, 100));
        serverlabel.setMinimumSize(new java.awt.Dimension(20, 10));
        serverlabel.setName("serverlabel"); // NOI18N
        serverlabel.setPreferredSize(new java.awt.Dimension(20, 18));
        serverlabel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 5, 0));
        statusPanel.add(serverlabel);

        errorlabel.setText(bundle.getString("MPView.errorlabel.text")); // NOI18N
        errorlabel.setName("errorlabel"); // NOI18N
        errorlabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                errorlabelMouseClicked(evt);
            }
        });
        statusPanel.add(errorlabel);

        mainToolbar.setRollover(true);
        mainToolbar.setName("mainToolbar"); // NOI18N

        closeButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/mpv5/resources/images/32/endturn.png"))); // NOI18N
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

        lockButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/mpv5/resources/images/32/lock.png"))); // NOI18N
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

        calculatorButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/mpv5/resources/images/32/kcalc.png"))); // NOI18N
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

    private void jMenuItem2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem2ActionPerformed
        Main.setLaF("de.muntjak.tinylookandfeel.TinyLookAndFeel");
        User.getCurrentUser().setLaf("de.muntjak.tinylookandfeel.TinyLookAndFeel");
        User.getCurrentUser().save();
    }//GEN-LAST:event_jMenuItem2ActionPerformed

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

    private void jMenuItem5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem5ActionPerformed
        Favourite.flush(User.getCurrentUser());
        refreshFavouritesMenu();

    }//GEN-LAST:event_jMenuItem5ActionPerformed

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
        pasteClipboardItems();

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

    private void jMenuItem18ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem18ActionPerformed
        addTab(ScheduleCalendar.instanceOf(), Messages.CALENDAR.toString());
    }//GEN-LAST:event_jMenuItem18ActionPerformed

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

    private void jButton8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton8ActionPerformed
        addTab(new ItemPanel(Context.getInvoice(), Item.TYPE_BILL), Messages.NEW_BILL);
    }//GEN-LAST:event_jButton8ActionPerformed
    private MPServer mpserver;
    private void jMenuItem24ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem24ActionPerformed
        mpserver = new MPServer();
        getMpserver().start();
        MPView.getIdentifierView().showServerStatus(getMpserver().isAlive());
        jMenuItem24.setEnabled(!mpserver.isAlive());
    }//GEN-LAST:event_jMenuItem24ActionPerformed

    private void jMenuItem25ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem25ActionPerformed
        LocalSettings.setProperty(LocalSettings.SCROLL_ALWAYS, String.valueOf(!isTabPaneScrolled()));
        LocalSettings.save();
        Popup.notice(Messages.RESTART_REQUIRED);
    }//GEN-LAST:event_jMenuItem25ActionPerformed

    private void jMenuItem26ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem26ActionPerformed

        XMLWriter.export(Context.getItem());
    }//GEN-LAST:event_jMenuItem26ActionPerformed

    private void jButton10ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton10ActionPerformed

        addTab(new ItemPanel(Context.getOffer(), Item.TYPE_OFFER), Messages.NEW_OFFER);
    }//GEN-LAST:event_jButton10ActionPerformed

    private void jButton11ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton11ActionPerformed
        addTab(new ItemPanel(Context.getOrder(), Item.TYPE_ORDER), Messages.NEW_ORDER);
    }//GEN-LAST:event_jButton11ActionPerformed

    private void jButton12ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton12ActionPerformed
        addTab(new ProductPanel(Context.getProduct(), Product.TYPE_SERVICE), Messages.NEW_SERVICE);
    }//GEN-LAST:event_jButton12ActionPerformed

    private void jButton13ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton13ActionPerformed
        addTab(new ProductPanel(Context.getProduct(), Product.TYPE_PRODUCT), Messages.NEW_PRODUCT);
    }//GEN-LAST:event_jButton13ActionPerformed

    private void errorlabelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_errorlabelMouseClicked
        SubmitForm submitForm = new SubmitForm(ExceptionHandler.getExceptions());
        BigPopup.showPopup(MPView.getIdentifierFrame().getRootPane(), submitForm, "Bughunter");
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
        MPView.getIdentifierView().addOrShowTab(JournalPanel.instanceOf(), Messages.OVERVIEW);
    }//GEN-LAST:event_jButton17ActionPerformed

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
        addOrShowTab(Log.getLogger().open(), "Logs");
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

        DatabaseObject d = Search2.showSearchFor(Context.getItem());
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

    private void jMenuItem37ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem37ActionPerformed
        addOrShowTab(OverviewPanel.instanceOf(), Messages.OVERVIEW);
    }//GEN-LAST:event_jMenuItem37ActionPerformed

    private void jMenuItem22ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem22ActionPerformed
        DialogForFile f = MPView.getFiledialog();
        if (f.saveFile()) {
            try {
                VCFParser.toVCard(DatabaseObject.getObjects(Context.getContact(), true), f.getFile());
            } catch (NodataFoundException ex) {
            }
        }
    }//GEN-LAST:event_jMenuItem22ActionPerformed

    private void jMenuItem38ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem38ActionPerformed

        DialogForFile f = MPView.getFiledialog();
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
                        contact.setCName("ERROR");
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

    private void sampleDataItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sampleDataItemActionPerformed
        SampleData sampleData = new SampleData(1, 1, true, true);

    }//GEN-LAST:event_sampleDataItemActionPerformed

    private void jMenuItem41ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem41ActionPerformed
        //TODO product to html wiz
    }//GEN-LAST:event_jMenuItem41ActionPerformed

    private void jMenuItem40ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem40ActionPerformed

        Wizard w = new Wizard(false);
        w.addPanel(new wizard_CSVImport2_1(w));
        w.showWiz();

    }//GEN-LAST:event_jMenuItem40ActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton calculatorButton;
    public javax.swing.JMenu clipboardMenu;
    private javax.swing.JButton closeButton;
    private javax.swing.JMenu editMenu;
    private javax.swing.JLabel errorlabel;
    public javax.swing.JMenu favouritesMenu;
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
    private javax.swing.JButton jButton20;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton7;
    private javax.swing.JButton jButton8;
    private javax.swing.JMenu jMenu10;
    private javax.swing.JMenu jMenu11;
    private javax.swing.JMenu jMenu12;
    private javax.swing.JMenu jMenu13;
    private javax.swing.JMenu jMenu14;
    private javax.swing.JMenu jMenu15;
    private javax.swing.JMenu jMenu2;
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
    private javax.swing.JMenuItem jMenuItem24;
    private javax.swing.JMenuItem jMenuItem25;
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
    private javax.swing.JMenuItem jMenuItem5;
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
    private javax.swing.JButton lockButton;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JToolBar mainToolbar;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JPanel nav_accounting;
    private javax.swing.JPanel nav_contacts;
    private javax.swing.JPanel nav_extras;
    private com.l2fprod.common.swing.JOutlookBar nav_outlookbar;
    private javax.swing.JPanel nav_products;
    private javax.swing.JPanel naviPanel;
    private javax.swing.JPanel parent_nav_accounting;
    private javax.swing.JPanel parent_nav_contacts;
    private javax.swing.JPanel parent_nav_extras;
    private javax.swing.JPanel parent_nav_products;
    public javax.swing.JPanel pluginIcons;
    private javax.swing.JProgressBar progressBar;
    private javax.swing.JMenuItem sampleDataItem;
    private javax.swing.JPanel separator;
    private javax.swing.JPanel separator1;
    private javax.swing.JPanel serverlabel;
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
     * @return the favouritesMenu
     */
    public javax.swing.JMenu getFavouritesMenu() {
        return favouritesMenu;
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
     * @param running
     */
    public void showServerStatus(boolean running) {
        if (running) {
            getServerlabel().setMaximumSize(new Dimension(18, 18));
            getServerlabel().setPreferredSize(new Dimension(18, 18));
            getServerlabel().setMinimumSize(new Dimension(18, 18));
            getServerlabel().setSize(18, 18);
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
                                MPServer.shutdown();
                                showServerStatus(false);
                                jMenuItem24.setEnabled(true);
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
            getServerlabel().add(plab);
        } else {
            getServerlabel().setMaximumSize(new Dimension(0, 0));
            getServerlabel().setPreferredSize(new Dimension(0, 0));
            getServerlabel().setMinimumSize(new Dimension(0, 0));
            getServerlabel().setSize(0, 0);

        }
        MPView.getIdentifierFrame().validate();
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
     * @return the mpserver
     */
    public MPServer getMpserver() {
        return mpserver;
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
     * @param TARGET The target navigation section, which can be one of the following:<br/>
     * <li>NAV_CONTACTS
     * <li>NAV_PRODUCTS
     * <li>NAV_ACCOUNTING
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

    /**
     * Paste the current clipboard items (if any)
     */
    public void pasteClipboardItems() {

        for (int i = 1; i < getClipboardMenu().getItemCount(); i++) {
            try {
                if (getClipboardMenu().getItemCount() > 1) {
                    ClipboardMenuItem item = (ClipboardMenuItem) getClipboardMenu().getItem(i);
                    getCurrentTab().paste(item.getItem());
                }
            } catch (Exception ignore) {
//            Log.Debug(ignore);
            }
        }
    }
}
