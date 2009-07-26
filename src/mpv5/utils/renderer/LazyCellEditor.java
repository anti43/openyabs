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
package mpv5.utils.renderer;

import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JTextField;

/**
 *
 */
public class LazyCellEditor extends DefaultCellEditor {

    private boolean fire = true;

    public LazyCellEditor(JComboBox c) {
        super(c);
    }

    public LazyCellEditor(JTextField tf) {
        super(tf);
    }

    @Override
    public boolean stopCellEditing() {
        if (fire) {
            super.stopCellEditing();
        }
        return true;
    }

    public void stopCellEditingSilent() {
        fire = false;
        stopCellEditing();
        fire = true;
    }
}
