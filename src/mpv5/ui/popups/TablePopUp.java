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
package mpv5.ui.popups;

import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.JTable;

/**
 */
public class TablePopUp extends JPopupMenu {

    private ActionListener[] actions;

    /**
     * Adds a popup menu to the given table
     * @param table
     * @param Items
     * @param actions
     */
    public TablePopUp(JTable table, String[] Items, ActionListener[] actions) {
        super();

        JMenuItem[] items = new JMenuItem[Items.length];
        for (int j = 0; j < actions.length; j++) {
            JMenuItem i = null;
            if (actions[j] != null) {
                ActionListener actionListener = actions[j];
                i = new JMenuItem(Items[j]);
                i.addActionListener(actionListener);
            }
            items[j] = i;
        }

        for (int i = 0; i < items.length; i++) {
            JMenuItem item = items[i];
            if (item != null) {
                item.setHorizontalTextPosition(JMenuItem.LEFT);
                this.add(item);
            } else {
                add(new JSeparator(JSeparator.HORIZONTAL));
            }
        }


        table.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseReleased(MouseEvent e) {

                if (e.getButton() == MouseEvent.BUTTON2 || e.getButton() == MouseEvent.BUTTON3) {
                    JTable source = (JTable) e.getSource();
                    int row = source.rowAtPoint(e.getPoint());
                    int column = source.columnAtPoint(e.getPoint());

                    if (!source.isRowSelected(row)) {
                        source.changeSelection(row, column, false, false);
                    }

                    show(source, e.getX(), e.getY());
                }
            }
        });
        this.actions = actions;
        this.actions = actions;
    }
}
