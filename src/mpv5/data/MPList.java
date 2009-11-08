/*
 *  This file is part of YaBS.
 *
 *  YaBS is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  YaBS is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with YaBS.  If not, see <http://www.gnu.org/licenses/>.
 */
package mpv5.data;

import java.util.Vector;
import javax.swing.DefaultListModel;
import mpv5.db.common.DatabaseObject;

/**
 * This is our own implementation of a list holding {@link DatabaseObject}s
 * @author andreasw
 */
public class MPList extends Vector<DatabaseObject> {

    /**
     * Invokes the save method on every entry in the list. If the list is empty, returns true
     * @return true if every item in the last was saved successfully
     */
    public boolean saveAll() {
        boolean success = false;
        for (int i = 0; i < this.size(); i++) {
            if (success) {
                success = this.get(i).save();
            } else {//save state
                this.get(i).save();
            }
        }
        return success;
    }

    /**
     * Invokes the delete method on every entry in the list. If the list is empty, returns true
     * @return true if every item in the last was deleted successfully
     */
    public boolean deleteAll() {
        boolean success = false;
        for (int i = 0; i < this.size(); i++) {
            if (success) {
                success = this.get(i).delete();
            } else {//save state
                this.get(i).delete();
            }
        }
        return success;
    }

    /**
     * Creates a new list model from the actual values in the list
     * @return
     */
    public DefaultListModel getListModel() {
        DefaultListModel model = new DefaultListModel();
        for (int i = 0; i < this.size(); i++) {
            DatabaseObject databaseObject = this.get(i);
            model.addElement(databaseObject);
        }
        return model;
    }

    @Override
    public DatabaseObject[] toArray() {
        return super.toArray(new DatabaseObject[0]);
    }
}
