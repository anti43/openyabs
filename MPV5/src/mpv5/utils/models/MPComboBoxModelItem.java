/*
 *  This file is part of MP by anti43 /GPL.
 *
 *  MP is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  MP is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with MP.  If not, see <http://www.gnu.org/licenses/>.
 */
package mpv5.utils.models;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import mpv5.logging.Log;

/**
 *
 * @author anti43
 */
public class MPComboBoxModelItem extends DefaultComboBoxModel {

    public static int getItemID(int uid, ComboBoxModel model) {
        for (int i = 0; i < model.getSize(); i++) {
            Log.Debug(MPComboBoxModelItem.class,((MPComboBoxModelItem) model.getElementAt(i)).id + " comparing with: " + uid);
            if (((MPComboBoxModelItem) model.getElementAt(i)).id.equals(Integer.valueOf(uid)) ) {
                Log.Debug(MPComboBoxModelItem.class, "Found at Index:" + i);
                return i;
            }
        }
        return 0;
    }

    public static MPComboBoxModelItem[] toItems(Object[][] items) {
        MPComboBoxModelItem[] array = new MPComboBoxModelItem[items.length];
        for (int i = 0; i < array.length; i++) {
            array[i] = new MPComboBoxModelItem(Integer.valueOf(items[i][0].toString()), String.valueOf(items[i][1]));
        }
        return array;
    }
    private Integer id;
    private String name;

    public MPComboBoxModelItem(int id, String name) {
        this.id = id;
        this.name = name;
    }

    /**
     * @return the id
     */
    public Integer getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
