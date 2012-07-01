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

import java.io.File;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.db.common.QueryHandler;
import mpv5.logging.Log;
import mpv5.utils.files.FileDirectoryHandler;
import mpv5.utils.images.MPIcon;

/**
 *
 *
 */
public class FileToItem extends DatabaseObject {

    private String description = "";
    private int itemsids;
    private String filename = "";
    private File file;

    public FileToItem() {
        setContext(Context.getFilesToItems());
    }

    @Override
    public JComponent getView() {
        try {
            FileDirectoryHandler.open(getFile());
        } catch (Exception e) {
            Log.Debug(e);
        }
        return null;
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
    MPIcon icon;

    @Override
    public mpv5.utils.images.MPIcon getIcon() {
        if (icon == null) {
            try {
                Log.Debug(this, "Determining Icon for " + __getCname());
                icon = new MPIcon(MPIcon.DIRECTORY_DEFAULT_ICONS + __getCname().substring(__getCname().lastIndexOf(".") + 1, __getCname().length()) + ".png");
                return icon;
            } catch (Exception e) {
                Log.Debug(this, "Icon file not existing in " + MPIcon.DIRECTORY_DEFAULT_ICONS);
                try {
                    JFileChooser chooser = new JFileChooser();
                    icon = new MPIcon(chooser.getIcon(new File(filename)));
                    return icon;
                } catch (Exception ez) {
                    Log.Debug(this, ez);
                    icon = new MPIcon(MPIcon.DIRECTORY_DEFAULT_ICONS + "folder_tar.png");
                    return icon;
                }
            }
        } else {
            return icon;
        }
    }

    /**
     * Fetches the physical file from db
     *
     * @return
     */
    public synchronized File getFile() {
        if (file == null) {
            try {
                file = QueryHandler.instanceOf().clone(Context.getFiles()).retrieveFile(filename,
                        new File(FileDirectoryHandler.getTempDir() + getCname()));
            } catch (Exception e) {
                Log.Debug(e);
            }
        }
        return file;
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

    @Override
    public boolean delete() {
        try {
            QueryHandler.instanceOf().clone(Context.getFiles()).removeFile(filename);
        } catch (Exception ex) {
            Log.Debug(ex);
        }
        return super.delete();
    }
}
