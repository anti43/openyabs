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
package mpv5.utils.renderer;

import java.awt.Color;
import java.awt.Component;
import javax.swing.JList;
import javax.swing.plaf.basic.BasicComboBoxRenderer;

/**
 *
 *  
 */
public class ComboBoxRendererForTooltip extends BasicComboBoxRenderer {
    private static final long serialVersionUID = 2746090194775905713L;

    @Override
    public Component getListCellRendererComponent(JList list, Object value,
        int index, boolean isSelected, boolean cellHasFocus) {
      if (isSelected) {
        setBackground(Color.LIGHT_GRAY);
//        setForeground(list.getSelectionForeground());
        if (-1 < index) {
          list.setToolTipText(String.valueOf(value));
        }
      } else {
            setBackground(Color.white);
        }
        try {
            setFont(list.getFont());
            setText((value == null) ? "" : value.toString());
        } catch (Exception e) {
        }
      return this;
    }
}
