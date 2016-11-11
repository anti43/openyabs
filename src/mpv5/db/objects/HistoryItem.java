/*
 *  This file is part of YaBS.
 *
 *      YaBS is free software: you can redistribute it and/or modify
 *      it under the terms of the GNU General Public License as published by
 *      the Free Software Foundation, either version 3 of the License, or
 *      (at your option) any later version.
 *
 *      YaBS is distributed in the hope that it will be useful,
 *      but WITHOUT ANY WARRANTY; without even the implied warranty of
 *      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *      GNU General Public License for more details.
 *
 *      You should have received a copy of the GNU General Public License
 *      along with YaBS.  If not, see <http://www.gnu.org/licenses/>.
 */
package mpv5.db.objects;

import java.util.Date;
import javax.swing.JComponent;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.db.common.QueryHandler;
import mpv5.ui.panels.HistoryPanel;
import mpv5.utils.export.Exportable;

/**
 *
 *
 */
public class HistoryItem extends DatabaseObject {

    private String username = "";
    private String dbidentity = "";
    private Date dateadded = new Date();
    private int intitem;

    public HistoryItem() {
        setContext(Context.getHistory());
    }

    /**
     * @return the user
     */
    public String __getUsername() {
        return username;
    }

    /**
     * @param user the user to set
     */
    public void setUsername(String user) {
        this.username = user;
    }

    /**
     * @return the dbidentity
     */
    public String __getDbidentity() {
        return dbidentity;
    }

    /**
     * @param dbidentity the dbidentity to set
     */
    public void setDbidentity(String dbidentity) {
        this.dbidentity = dbidentity;
    }

    /**
     * @return the item
     */
    public int __getIntitem() {
        return intitem;
    }

    /**
     * @param item the item to set
     */
    public void setIntitem(int item) {
        this.intitem = item;
    }

    @Override
    public JComponent getView() {
        return new HistoryPanel();
    }

    @Override
    public mpv5.utils.images.MPIcon getIcon() {
        return null;
    }
}
