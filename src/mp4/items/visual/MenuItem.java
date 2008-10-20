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
package mp4.items.visual;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import mp4.interfaces.ActionHandler;
import mp4.logs.Log;

/**
 *
 * @author anti43
 */
public class MenuItem extends JMenuItem {

    private ActionHandler aktion;
    private Object item;
    private JMenu menu;

    public MenuItem(ActionHandler aktion, String text, Object item, JMenu menu) {
        super(text);
        this.aktion = aktion;
        this.item = item;
        this.menu = menu;
        this.addActionListener(new java.awt.event.ActionListener() {

            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                doAction();
            }
        });

    }

    private void doAction() {
        try {
            aktion.doAction(this);
        } catch (Exception e) {
            Log.Debug(this, e);
        }
    }

    public Object getItem() {
        return item;
    }

    public void removeFromParent() {
        menu.remove(this);
        menu.validate();
    }
}

