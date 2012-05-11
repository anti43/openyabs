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
package mpv5.pluginhandling;

import javax.swing.JComponent;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.db.common.QueryHandler;
import mpv5.logging.Log;
import mpv5.utils.images.MPIcon;

/**
 * This is the bridge between MP5 Plugins and jars in the database
 *  
 */
public class Plugin extends DatabaseObject {

    private String description;
    private String filename;

    public Plugin() {
        setContext(Context.getPlugins());
    }

    @Override
    public JComponent getView() {
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

    @Override
    public boolean delete() {
        try {
            QueryHandler.instanceOf().clone(Context.getFiles()).removeFile(__getFilename());
            return super.delete();
        } catch (Exception ex) {
            Log.Debug(ex);
            return false;
        }
    }

    @Override
    public String toString(){
    return __getCname() + " (" +  __getDescription() + ")";
    }

    @Override
    public MPIcon getIcon() {
        return null;
    }
}
