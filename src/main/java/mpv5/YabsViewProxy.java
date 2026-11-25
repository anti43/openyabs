/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mpv5;

import java.awt.Color;
import java.io.File;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JProgressBar;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.db.objects.Contact;
import mpv5.db.objects.Item;
import mpv5.db.objects.Product;
import mpv5.db.objects.User;
import mpv5.logging.Log;
import mpv5.pluginhandling.YabsPluginLoader;
import mpv5.ui.dialogs.DialogForFile;
import mpv5.ui.panels.DataPanel;
import mpv5.ui.panels.ModernContactPanel;
import mpv5.ui.panels.ModernProductPanel;
import org.jdesktop.application.Application;

/**
 *
 * @author anti
 */
public class YabsViewProxy implements YabsView {


    public static interface SessionSaver {

        public void saveSession();

        public void restoreSession();
    }

    public static interface LookupProvider {

        public void setLookupVisible(boolean show);
    }

    public static interface FlowProvider {

        public void showPreviousDatabaseObject();

        public boolean hasPreviousDatabaseObject();

        public void showNextDatabaseObject();

        public boolean hasNextDatabaseObject();

        public DatabaseObject getCurrentDatabaseObject();
    }
    private LinkedList<YabsView> views = new LinkedList<YabsView>();
    private static YabsViewProxy instance;
    private ConcurrentHashMap<Class, Class> viewCache = new ConcurrentHashMap<Class, Class>();

    /**
     * 
     * @return
     */
    public static synchronized YabsViewProxy instance() {
        if (instance == null) {
            instance = new YabsViewProxy();
        }
        return instance;
    }

    /**
     *
     * @param view
     */
    public synchronized void register(YabsView view) {
        Log.Print("Registering " + view.getClass());
        views.addLast(view);
    }

    @Override
    public synchronized void addMessage(Object messages, Color c) {
        if (paused > 0) {
            skipped++;
            return;
        }
        for (int i = 0; i < views.size(); i++) {
            views.get(i).addMessage(messages, c);
        }
    }
    
      @Override
    public synchronized void addMessage(Object messages ) {
        if (paused > 0) {
            skipped++;
            return;
        }
        for (int i = 0; i < views.size(); i++) {
            views.get(i).addMessage(messages );
        }
    }

    private int paused;
    private int skipped;

    /**
     * Stops showing status messages (e.g. during an import, which would post
     * one per record and flood the message list); pairs with
     * {@link #resumeMessages()}
     */
    public synchronized void pauseMessages() {
        if (paused++ == 0) {
            skipped = 0;
        }
    }

    /**
     * Shows status messages again, with one line about the skipped ones
     */
    public void resumeMessages() {
        int n;
        synchronized (this) {
            if (paused == 0 || --paused > 0) {
                return;
            }
            n = skipped;
            skipped = 0;
        }
        if (n > 0) {
            addMessage(String.format(mpv5.globals.Messages.MESSAGES_PAUSED.toString(), n));
        }
    }

    @Override
    public synchronized void setWaiting(boolean b) {
        for (int i = 0; i < views.size(); i++) {
            views.get(i).setWaiting(b);
        }
    }

    @Override
    public synchronized void setProgressRunning(boolean b) {
        for (int i = 0; i < views.size(); i++) {
            views.get(i).setProgressRunning(b);
        }
    }
    
    @Override
    public Application getApplication() {
        if (views.isEmpty()) {
            return null;
        } else {
            return views.getFirst().getApplication();
        }
    }
    
    @Override
    public YabsApplication getYabsApplication() {
        if (views.isEmpty()) {
            return null;
        } else {
            return views.getFirst().getYabsApplication();
        }
    }

    @Override
    public synchronized JFrame getIdentifierFrame() {
        if (views.isEmpty()) {
            return null;
        } else {
            return views.getFirst().getIdentifierFrame();
        }
    }

    @Override
    public synchronized YabsView getIdentifierView() {
        if (views.isEmpty()) {
            return null;
        } else {
            return views.getFirst().getIdentifierView();
        }
    }

    @Override
    public synchronized YabsPluginLoader getPluginLoader() {
        if (views.isEmpty()) {
            return null;
        } else {
            return views.getFirst().getPluginLoader();
        }
    }

    @Override
    public synchronized void setProgressMaximumValue(int val) {
        for (int i = 0; i < views.size(); i++) {
            views.get(i).setProgressMaximumValue(val);
        }
    }

    @Override
    public synchronized void setProgressReset() {
        for (int i = 0; i < views.size(); i++) {
            views.get(i).setProgressReset();
        }
    }

    @Override
    public synchronized void setProgressValue(int f) {
        for (int i = 0; i < views.size(); i++) {
            views.get(i).setProgressValue(f);
        }
    }

    @Override
    public synchronized void showFilesaveDialogFor(File createFile) {
        views.getFirst().showFilesaveDialogFor(createFile);
    }

    @Override
    public synchronized DataPanel addTab(DatabaseObject object) {
        if (views.size() > 1) {
            for (int i = 1; i < views.size(); i++) {
                views.get(i).addTab(object);
            }
        }
        if (views.isEmpty()) {
            return null;
        } else {
            return views.getFirst().addTab(object);
        }
    }
    
        @Override
    public synchronized DataPanel addTab(DatabaseObject object, boolean forceNew) {
        if (views.size() > 1) {
            for (int i = 1; i < views.size(); i++) {
                views.get(i).addTab(object, forceNew);
            }
        }
        if (views.isEmpty()) {
            return null;
        } else {
            return views.getFirst().addTab(object, forceNew);
        }
    }

    @Override
    public synchronized DialogForFile getFiledialog() {
        if (views.isEmpty()) {
            return null;
        } else {
            return views.getFirst().getFiledialog();
        }
    }

    @Override
    public synchronized JProgressBar getProgressbar() {
        if (views.isEmpty()) {
            return null;
        } else {
            return views.getFirst().getProgressbar();
        }
    }

    @Override
    public synchronized int addTab(JComponent generalListPanel, String label) {
        if (views.size() > 1) {
            for (int i = 1; i < views.size(); i++) {
                views.get(i).addTab(generalListPanel, label);
            }
        }
        if (views.isEmpty()) {
            return -1;
        } else {
            return views.getFirst().addTab(generalListPanel, label);
        }
    }

    @Override
    public synchronized DataPanel addTab(DatabaseObject d, Object label) {
        if (views.size() > 1) {
            for (int i = 1; i < views.size(); i++) {
                views.get(i).addTab(d, label);
            }
        }
        if (views.isEmpty()) {
            return null;
        } else {
            return views.getFirst().addTab(d, label);
        }
    }

    @Override
    public synchronized DataPanel getCurrentTab() {
        if (views.isEmpty()) {
            return null;
        } else {
            return views.getFirst().getCurrentTab();
        }
    }

    @Override
    public synchronized void addOrShowTab(JComponent t, Object label) {
        for (int i = 0; i < views.size(); i++) {
            views.get(i).addOrShowTab(t, label);
        }
    }

    @Override
    public synchronized void addOrShowTab(DatabaseObject dbo) {
        for (int i = 0; i < views.size(); i++) {
            views.get(i).addOrShowTab(dbo);
        }
    }

    @Override
    public synchronized void addToClipBoard(DatabaseObject it) {
        for (int i = 0; i < views.size(); i++) {
            views.get(i).addToClipBoard(it);
        }
    }

    @Override
    public synchronized void setClipBoardVisible(boolean show) {
        for (int i = 0; i < views.size(); i++) {
            views.get(i).setClipBoardVisible(show);
        }
    }

    @Override
    public synchronized DatabaseObject[] getClipboardItems() {
        if (views.isEmpty()) {
            return null;
        } else {
            return views.getFirst().getClipboardItems();
        }
    }

    /**
     * 
     * @return All registered views
     */
    public synchronized List<YabsView> getViews() {
        if (views.isEmpty()) {
            return null;
        } else {
            return Collections.unmodifiableList(views);
        }
    }

    /**
     * Creates the view of a database object in the modern layout
     */
    public static interface ViewFactory {

        /**
         * @param item the object to show (its context, type ...); the view is
         * not filled with it yet
         * @return the view
         */
        public DataPanel create(DatabaseObject item);
    }
    /**
     * System property to choose the views at start: "modern" or "classic"
     * (overrides the user setting), e.g. java -Dyabs.ui=modern -jar
     * openyabs.jar
     */
    public static final String UI_PROPERTY = "yabs.ui";
    /**
     * User setting (group org.openyabs.uiproperty) to use the modern views
     */
    public static final String MODERN_UI_SETTING = "modernui";
    private final ConcurrentHashMap<Class, ViewFactory> modernViews = new ConcurrentHashMap<Class, ViewFactory>();

    {
        //the views which exist in the modern layout already, all others stay classic
        registerModernView(Contact.class, item -> new ModernContactPanel(item.getContext()));
        registerModernView(Product.class, item -> new ModernProductPanel(item.getContext(), ((Product) item).__getInttype()));
        registerModernView(Item.class, item -> new mpv5.ui.panels.ModernItemPanel(
                mpv5.handler.ItemService.contextOf(((Item) item).__getInttype()), ((Item) item).__getInttype()));
        registerModernView(mpv5.db.objects.Expense.class, item -> new mpv5.ui.panels.ModernExpensePanel());
        registerModernView(mpv5.db.objects.Revenue.class, item -> new mpv5.ui.panels.ModernRevenuePanel());
    }

    /**
     * Registers the modern view of a kind of database object. Used instead of
     * the classic view ({@link DatabaseObject#getView()}) when the modern views
     * are enabled ({@link #isModernUi()}).
     *
     * @param clazz e.g. Contact.class
     * @param factory creates the view
     */
    public void registerModernView(Class<? extends DatabaseObject> clazz, ViewFactory factory) {
        modernViews.put(clazz, factory);
    }

    /**
     * A new, empty view for a product or service: modern or classic, as
     * configured
     *
     * @param type Product.TYPE_PRODUCT or TYPE_SERVICE
     * @return the view
     */
    public JComponent newProductView(int type) {
        Product p = new Product();
        p.setInttype(type);
        DataPanel modern = getModernView(p);
        return modern != null ? (JComponent) modern : new mpv5.ui.panels.ProductPanel(Context.getProduct(), type);
    }

    /**
     * A new, empty view for an offer, order, invoice ...: modern or classic,
     * as configured
     *
     * @param type Item.TYPE_*
     * @return the view
     */
    public JComponent newItemView(int type) {
        Item i = (Item) DatabaseObject.getObject(mpv5.handler.ItemService.contextOf(type));
        i.setInttype(type);
        DataPanel modern = getModernView(i);
        return modern != null ? (JComponent) modern : new mpv5.ui.panels.ItemPanel2(mpv5.handler.ItemService.contextOf(type), type);
    }

    /**
     * @param type Item.TYPE_*
     * @param status Item.STATUS_*
     * @return the view with initial status set
     */
    public JComponent newItemView(int type, int status) {
        JComponent v = newItemView(type);
        if (v instanceof mpv5.ui.panels.ModernItemPanel) {
            ((mpv5.ui.panels.ModernItemPanel) v).setItemStatus(status);
        } else if (v instanceof mpv5.ui.panels.ItemPanel2) {
            ((mpv5.ui.panels.ItemPanel2) v).setItemStatus(status);
        }
        return v;
    }

    /**
     * The view of a product, filled with it: modern or classic, as configured
     *
     * @param p
     * @return the view
     */
    public javax.swing.JPanel productView(Product p) {
        DataPanel modern = getModernView(p);
        if (modern != null) {
            modern.setDataOwner(p, true);
            return (javax.swing.JPanel) modern;
        }
        return new mpv5.ui.panels.ProductPanel(p);
    }

    /**
     * @return true if the modern views and look are used: the system property
     * {@value #UI_PROPERTY} if set, else the setting of the current user -
     * modern unless the user switched to the classic views (Control Panel &gt;
     * User settings); independent of the look (View &gt; Layout)
     */
    public boolean isModernUi() {
        String p = System.getProperty(UI_PROPERTY);
        if (p != null && p.trim().length() > 0) {
            return p.trim().equalsIgnoreCase("modern");
        }
        try {
            return User.getCurrentUser().getProperties().getProperty("org.openyabs.uiproperty$" + MODERN_UI_SETTING, Boolean.TRUE);
        } catch (Exception e) {
            Log.Debug(this, e);
            return true;
        }
    }

    /**
     * Switches the current user to the modern or the classic views of
     * contacts, products and documents (saved with the user); open tabs keep
     * their view
     *
     * @param modern
     */
    public void setModernUi(boolean modern) {
        User u = User.getCurrentUser();
        u.getProperties().changeProperty("org.openyabs.uiproperty", MODERN_UI_SETTING, modern);
        u.saveProperties();
        //the look (View > Layout) stays as it is
    }

    /**
     * The modern view of the object, if the modern views are enabled and one
     * exists for this kind of object
     *
     * @param item
     * @return the view (not yet filled with the object), or null to use the
     * classic view
     */
    public DataPanel getModernView(DatabaseObject item) {
        if (item == null || !isModernUi()) {
            return null;
        }
        for (Class c = item.getClass(); c != null && DatabaseObject.class.isAssignableFrom(c); c = c.getSuperclass()) {
            ViewFactory f = modernViews.get(c);
            if (f != null) {
                try {
                    return f.create(item);
                } catch (Exception e) {
                    //never lose the view: fall back to the classic one
                    Log.Debug(this, e);
                    return null;
                }
            }
        }
        return null;
    }

    public DataPanel getViewFor(DatabaseObject item) {
        if (viewCache.containsKey(item.getClass())) {
            try {
                return (DataPanel) viewCache.get(item.getClass()).newInstance();
            } catch (Exception ex) {
                Log.Debug(ex);
                return (DataPanel) item.getView();
            }
        } else {
            return (DataPanel) item.getView();
        }
    }

    /**
     *
     * @param clazz
     * @param view
     */
    public void registerViewFor(Class clazz, Class view) {
        viewCache.put(clazz, view);
    }

    public void saveSession() {
        for (int i = 0; i < views.size(); i++) {
            if (views.get(i) instanceof SessionSaver) {
                ((SessionSaver) views.get(i)).saveSession();
            }
        }
    }

    public void restoreSession() {
        for (int i = 0; i < views.size(); i++) {
            if (views.get(i) instanceof SessionSaver) {
                ((SessionSaver) views.get(i)).restoreSession();
            }
        }
    }

    public void setLookupVisible(boolean show) {
        for (int i = 0; i < views.size(); i++) {
            if (views.get(i) instanceof LookupProvider) {
                ((LookupProvider) views.get(i)).setLookupVisible(show);
            }
        }
    }

    @Override
    public void reloadFavorites() {
        for (int i = 0; i < views.size(); i++) {
            views.get(i).reloadFavorites();
        }
    }
    
    @Override
    public void showOfficeStatus(boolean b, String description) {
        for (int i = 0; i < views.size(); i++) {
            views.get(i).showOfficeStatus(b, description);
        }
    }
    
    @Override
    public void showServerStatus(boolean b) {
        for (int i = 0; i < views.size(); i++) {
            views.get(i).showServerStatus(b);
        }
    }
    
     @Override
    public void removeTab(DataPanel b) {
        for (int i = 0; i < views.size(); i++) {
            views.get(i).removeTab(b);
        }
    }

    /**
     * Notifies all open tabs referencing {@code updated} so contact edits are
     * immediately reflected without closing or reopening tabs.
     */
    public void refreshOpenTabsForContact(final mpv5.db.objects.Contact updated) {
        refreshOpenTabsForContact(updated, null);
    }

    /**
     * Notifies all other open tabs referencing {@code updated} so contact edits
     * are immediately reflected across documents, products, duplicate contact
     * tabs, and contact lists.
     *
     * @param updated saved contact
     * @param source panel that triggered the save (skipped), may be null
     */
    public void refreshOpenTabsForContact(final mpv5.db.objects.Contact updated, final DataPanel source) {
        if (updated == null || updated.__getIDS() <= 0) {
            return;
        }
        runOnEdt(() -> {
            for (int v = 0; v < views.size(); v++) {
                if (views.get(v) instanceof mpv5.ui.frames.MPView) {
                    mpv5.ui.frames.MPView view = (mpv5.ui.frames.MPView) views.get(v);
                    int count = view.getTabPane().getTabCount();
                    for (int i = 0; i < count; i++) {
                        java.awt.Component comp = view.getTabContentAt(i);
                        if (comp == null || comp == source) {
                            continue;
                        }
                        if (comp instanceof mpv5.ui.panels.ModernItemPanel) {
                            ((mpv5.ui.panels.ModernItemPanel) comp).refreshContact(updated);
                        } else if (comp instanceof mpv5.ui.panels.ItemPanel2) {
                            ((mpv5.ui.panels.ItemPanel2) comp).refreshContact(updated);
                        } else if (comp instanceof mpv5.ui.panels.ModernContactPanel) {
                            ((mpv5.ui.panels.ModernContactPanel) comp).refreshContact(updated);
                        } else if (comp instanceof mpv5.ui.panels.ModernProductPanel) {
                            ((mpv5.ui.panels.ModernProductPanel) comp).refreshContact(updated);
                        } else if (comp instanceof mpv5.ui.panels.ContactsList) {
                            ((mpv5.ui.panels.ContactsList) comp).refresh();
                        }
                    }
                }
            }
        });
    }

    /**
     * Notifies all other open tabs sharing data with {@code updated} when a
     * product is saved.
     *
     * @param updated saved product
     * @param source panel that triggered the save (skipped), may be null
     */
    public void refreshOpenTabsForProduct(final mpv5.db.objects.Product updated, final DataPanel source) {
        if (updated == null || updated.__getIDS() <= 0) {
            return;
        }
        runOnEdt(() -> {
            for (int v = 0; v < views.size(); v++) {
                if (views.get(v) instanceof mpv5.ui.frames.MPView) {
                    mpv5.ui.frames.MPView view = (mpv5.ui.frames.MPView) views.get(v);
                    int count = view.getTabPane().getTabCount();
                    for (int i = 0; i < count; i++) {
                        java.awt.Component comp = view.getTabContentAt(i);
                        if (comp == null || comp == source) {
                            continue;
                        }
                        if (comp instanceof mpv5.ui.panels.ModernProductPanel) {
                            ((mpv5.ui.panels.ModernProductPanel) comp).refreshProduct(updated);
                        } else if (comp instanceof mpv5.ui.panels.ModernContactPanel) {
                            ((mpv5.ui.panels.ModernContactPanel) comp).refreshProduct(updated);
                        } else if (comp instanceof mpv5.ui.panels.ProductsOverview) {
                            ((mpv5.ui.panels.ProductsOverview) comp).refresh();
                        }
                    }
                }
            }
        });
    }

    /**
     * Notifies all other open tabs sharing data with {@code updated} when an
     * offer, order, invoice, or credit note is saved.
     *
     * @param updated saved item
     * @param source panel that triggered the save (skipped), may be null
     */
    public void refreshOpenTabsForItem(final mpv5.db.objects.Item updated, final DataPanel source) {
        if (updated == null || updated.__getIDS() <= 0) {
            return;
        }
        runOnEdt(() -> {
            for (int v = 0; v < views.size(); v++) {
                if (views.get(v) instanceof mpv5.ui.frames.MPView) {
                    mpv5.ui.frames.MPView view = (mpv5.ui.frames.MPView) views.get(v);
                    int count = view.getTabPane().getTabCount();
                    for (int i = 0; i < count; i++) {
                        java.awt.Component comp = view.getTabContentAt(i);
                        if (comp == null || comp == source) {
                            continue;
                        }
                        if (comp instanceof mpv5.ui.panels.ModernItemPanel) {
                            ((mpv5.ui.panels.ModernItemPanel) comp).refreshItem(updated);
                        } else if (comp instanceof mpv5.ui.panels.ModernContactPanel) {
                            ((mpv5.ui.panels.ModernContactPanel) comp).refreshItem(updated);
                        } else if (comp instanceof mpv5.ui.panels.ModernProductPanel) {
                            ((mpv5.ui.panels.ModernProductPanel) comp).refreshItem(updated);
                        } else if (comp instanceof mpv5.ui.panels.HomeScreen) {
                            ((mpv5.ui.panels.HomeScreen) comp).refresh();
                        }
                    }
                }
            }
        });
    }

    private static void runOnEdt(Runnable task) {
        if (javax.swing.SwingUtilities.isEventDispatchThread()) {
            task.run();
        } else {
            javax.swing.SwingUtilities.invokeLater(task);
        }
    }

    /**
     * Re-checks template availability on all open document tabs (called after
     * template manager changes so no restart is needed).
     */
    public void refreshOpenTabsTemplates() {
        Runnable task = () -> {
            for (int v = 0; v < views.size(); v++) {
                if (views.get(v) instanceof mpv5.ui.frames.MPView) {
                    mpv5.ui.frames.MPView view = (mpv5.ui.frames.MPView) views.get(v);
                    int count = view.getTabPane().getTabCount();
                    for (int i = 0; i < count; i++) {
                        DataPanel panel = view.getTabAt(i);
                        if (panel instanceof mpv5.ui.panels.ModernItemPanel) {
                            ((mpv5.ui.panels.ModernItemPanel) panel).refreshTemplates();
                        } else if (panel instanceof mpv5.ui.panels.ItemPanel2) {
                            ((mpv5.ui.panels.ItemPanel2) panel).refreshTemplates();
                        }
                    }
                }
            }
        };
        if (javax.swing.SwingUtilities.isEventDispatchThread()) {
            task.run();
        } else {
            javax.swing.SwingUtilities.invokeLater(task);
        }
    }
    
}
