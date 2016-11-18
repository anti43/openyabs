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
import mpv5.db.common.DatabaseObject;
import mpv5.logging.Log;
import mpv5.pluginhandling.YabsPluginLoader;
import mpv5.ui.dialogs.DialogForFile;
import mpv5.ui.panels.DataPanel;
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
        for (int i = 0; i < views.size(); i++) {
            views.get(i).addMessage(messages, c);
        }
    }
    
      @Override
    public synchronized void addMessage(Object messages ) {
        for (int i = 0; i < views.size(); i++) {
            views.get(i).addMessage(messages );
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
    
}
