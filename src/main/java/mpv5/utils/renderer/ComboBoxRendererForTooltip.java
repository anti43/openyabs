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
 * Shows the entries of a combo box and the selected one as tooltip. The colors
 * are those of the list (look and feel): text and background always match,
 * also with dark desktop themes (before, the background was always white and
 * the text color whatever was painted last, e.g. white on white on Linux).
 */
public class ComboBoxRendererForTooltip extends BasicComboBoxRenderer {
    private static final long serialVersionUID = 2746090194775905713L;

    @Override
    public Component getListCellRendererComponent(JList list, Object value,
            int index, boolean isSelected, boolean cellHasFocus) {
        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        Color bg = isSelected ? list.getSelectionBackground() : list.getBackground();
        Color fg = isSelected ? list.getSelectionForeground() : list.getForeground();
        if (bg != null && fg != null) {
            setBackground(bg);
            setForeground(fg);
        }
        if (isSelected && -1 < index) {
            list.setToolTipText(String.valueOf(value));
        }
        try {
            setFont(list.getFont());
            setText((value == null) ? "" : value.toString());
        } catch (Exception e) {
        }
        return this;
    }
}
