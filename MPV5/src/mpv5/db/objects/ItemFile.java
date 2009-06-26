/*
 *  This file is part of MP.
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
package mpv5.db.objects;

import java.io.File;
import javax.swing.JComponent;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.db.common.NodataFoundException;
import mpv5.db.common.QueryHandler;
import mpv5.logging.Log;
import mpv5.ui.panels.ItemPanel;
import mpv5.utils.files.FileDirectoryHandler;
import mpv5.utils.images.MPIcon;

/**
 *
 *  anti
 */
public class ItemFile extends DatabaseObject {

    private String description = "";
    private int itemsids;
    private String filename = "";

    public ItemFile() {
        context.setDbIdentity(Context.IDENTITY_FILES_TO_ITEMS);
        context.setIdentityClass(this.getClass());
    }

    @Override
    public String __getCName() {
        return cname;
    }

    @Override
    public JComponent getView() {
        try {
            FileDirectoryHandler.open(QueryHandler.instanceOf().clone(Context.getFiles()).
                    retrieveFile(filename,
                    new File(FileDirectoryHandler.getTempDir() + cname)));
        } catch (Exception e) {
            Log.Debug(e);
        }
        return null;
    }

    @Override
    public void setCName(String name) {
        cname = name;
    }

    /**
     * @return the description
     */
    public String __getDescription() {
        return description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return the filename
     */
    public String __getFilename() {
        return filename;
    }

    /**
     * @param filename the filename to set
     */
    public void setFilename(String filename) {
        this.filename = filename;
    }

    @Override
    public mpv5.utils.images.MPIcon getIcon() {
        return new MPIcon("/mpv5/resources/images/48/folder_tar.png");
    }

    /**
     * @return the itemsids
     */
    public int __getItemsids() {
        return itemsids;
    }

    /**
     * @param itemsids the itemsids to set
     */
    public void setItemsids(int itemsids) {
        this.itemsids = itemsids;
    }
}
