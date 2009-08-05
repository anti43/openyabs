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
package mpv5.utils.models;

import javax.swing.DefaultComboBoxModel;

/**
 * MP Implementation of a {@link DefaultComboBoxModel}
 */
public class MPComboboxModel extends DefaultComboBoxModel {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a DefaultComboBoxModel object initialized with an array of {@link MPComboboxModelItem}objects.
     * @param items
     */
    public MPComboboxModel(MPComboBoxModelItem[] items) {
        super(items);
    }

    /**
     * Returns all elements in the model
     * @return
     */
    public MPComboBoxModelItem[] getElements() {
        MPComboBoxModelItem[] m = new MPComboBoxModelItem[getSize()];
        for (int i = 0; i < m.length; i++) {
            m[i] = (MPComboBoxModelItem) getElementAt(i);
        }
        return m;
    }

    @Override
    public MPComboBoxModelItem getSelectedItem() {
        if (super.getSelectedItem() != null) {
            if (super.getSelectedItem() instanceof MPComboBoxModelItem) {
                return (MPComboBoxModelItem) super.getSelectedItem();
            } else {
                return new MPComboBoxModelItem(0, super.getSelectedItem().toString());
            }
        } else {
            return null;
        }
    }

    /**
     * Set the value of the selected item. The selected item may be null.
     * @param item - The combo box value or null for no selection.
     */
    public void setSelectedItem(MPComboBoxModelItem item) {
        super.setSelectedItem(item);
    }
}
