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
package mpv5.ui.menus;

import javax.swing.JMenuItem;
import mpv5.db.common.DatabaseObject;
import mpv5.ui.frames.MPView;

/**
 *
 *  
 */
public class FavouritesMenuItem extends JMenuItem {

    private static final long serialVersionUID = 1L;
    private DatabaseObject item;

    public FavouritesMenuItem(final DatabaseObject dbo) {
        this.item = dbo;
        this.setText(dbo.__getCname());
        this.setIcon(dbo.getIcon());

        addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mpv5.YabsViewProxy.instance().getIdentifierView().addTab(item);
            }
        });

    }

    public FavouritesMenuItem(DatabaseObject dbo, String name) {
        this.item = dbo;
        this.setText(name);
        this.setIcon(dbo.getIcon());
    }
}
