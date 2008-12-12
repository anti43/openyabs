/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mpv5.ui.panels;

import mpv5.db.common.DatabaseObject;

/**
 *
 * @author Andreas
 */
public interface DataPanel {

    public void showList();
    public void reset();
    public void collectData();
    public DatabaseObject getDataOwner();
}
