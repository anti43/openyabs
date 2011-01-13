/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package mpv5;

import java.io.File;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JProgressBar;
import mpv5.db.common.DatabaseObject;
import mpv5.globals.Constants;
import mpv5.pluginhandling.MPPLuginLoader;
import mpv5.ui.dialogs.DialogForFile;
import mpv5.ui.panels.DataPanel;

/**
 *
 * @author anti
 */
public interface YabsView{

    public static String WINDOW_TITLE = Constants.VERSION;

    public void addMessage(Object messages);

    public void setWaiting(boolean b);

    public void setProgressRunning(boolean b);

    public JFrame getIdentifierFrame();

    public YabsView getIdentifierView();

    public MPPLuginLoader getPluginLoader();

    public void setProgressMaximumValue(int i);

    public void setProgressReset();

    public void setProgressValue(int f);

    public void showFilesaveDialogFor(File createFile);

    public DataPanel addTab(DatabaseObject object);

    public DialogForFile getFiledialog();

    public JProgressBar getProgressbar();

    public int addTab(JComponent generalListPanel, String label);

    public DataPanel addTab(DatabaseObject d, Object label);

    public DataPanel getCurrentTab();

    public void addOrShowTab(JComponent t, Object label);

    public void addOrShowTab(DatabaseObject dbo);

    public void addToClipBoard(DatabaseObject it);

    public DatabaseObject[] getClipboardItems();

    
}
