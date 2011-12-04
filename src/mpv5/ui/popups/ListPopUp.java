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
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import mpv5.globals.Messages;
import mpv5.ui.menus.ListMenuItem;

/**
 */
public class ListPopUp extends JPopupMenu {
    private static final long serialVersionUID = 1L;

    /**
     * Adds a popup menu to the given table
     * @param table
     * @param Items
     * @param actions
     */
    public ListPopUp(JList list, String[][] Items, ActionListener[] actions) {
        super();
        this.add(new JLabel(Messages.MASSPRINT_FILTER.toString()));
        ListMenuItem[] items = new ListMenuItem[Items.length];
        for (int j = 0; j < actions.length; j++) {
            ListMenuItem i = null;
            if (actions[j] != null) {
                ActionListener actionListener = actions[j];
                i = new ListMenuItem(Items[j][0]);
                i.addActionListener(actionListener);
                i.setID(Integer.parseInt(Items[j][1]));
            }
            items[j] = i;
        }

        for (int i = 0; i < items.length; i++) {
            ListMenuItem item = items[i];
            if (item != null) {
                item.setHorizontalTextPosition(JMenuItem.LEFT);
                this.add(item);
            } else {
                add(new JSeparator(JSeparator.HORIZONTAL));
            }
        }


        list.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseReleased(MouseEvent e) {

                if (e.getButton() == MouseEvent.BUTTON2 || e.getButton() == MouseEvent.BUTTON3) {
                    JList source = (JList) e.getSource();
                    show(source, e.getX(), e.getY());
                }
            }
        });
    }
}
