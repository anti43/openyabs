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

import java.awt.Color;
import java.awt.Component;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

/**
 *
 * @author Andreas
 */
public class MPJComboboxModelItem {

    private Integer id = 0;
    private String text = "";
    private String name = "";
    public boolean state = true;
    public static CheckComboRenderer renderer;

    public MPJComboboxModelItem() {
    }

    public MPJComboboxModelItem(Integer id, String name, String text) {
        this.id = id;
        this.text = text;
        this.name = name;
    }

    public ListCellRenderer getCheckComboRenderer() {
        return new CheckComboRenderer();
    }

    @Override
    public String toString() {
        return text;
    }

    public Integer getId() {
        return id;
    }

    public class CheckComboRenderer implements ListCellRenderer {

        public JCheckBox checkBox;

        public CheckComboRenderer() {
            checkBox = new JCheckBox();
        }

        public Component getListCellRendererComponent(JList list,
                Object value,
                int index,
                boolean isSelected,
                boolean cellHasFocus) {
            MPJComboboxModelItem store = (MPJComboboxModelItem) value;
            checkBox.setText(store.text);
            checkBox.setSelected(store.state);
            checkBox.setBackground(isSelected ? Color.red : Color.white);
            checkBox.setForeground(isSelected ? Color.white : Color.black);
            return checkBox;
        }
    }
}
