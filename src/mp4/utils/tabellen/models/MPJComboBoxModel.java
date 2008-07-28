/*
 *  This file is part of MP by anti43 /GPL.
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
package mp4.utils.tabellen.models;

import javax.swing.DefaultComboBoxModel;

/**
 *
 * @author Andreas
 */
public class MPJComboBoxModel extends DefaultComboBoxModel {

    private MPJComboboxModelItem item;

    /**
     * 
     * @param data {integer id, string text}
     */
    public MPJComboBoxModel(Object[][] data) {

        for (int i = 0; i < data.length; i++) {
            item = new MPJComboboxModelItem();
            item.id = (Integer) data[i][0];
            item.text = (String) data[i][1];

            super.addElement(item);
        }

    }

    /**
     * 
     */
    public class MPJComboboxModelItem {

        private Integer id = 0;
        private String text;

        @Override
        public String toString() {
            return text;
        }

        public Integer getId() {
            return id;
        }
    }
}
