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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.db.common.NodataFoundException;
import mpv5.globals.Messages;
import mpv5.logging.Log;
import mpv5.ui.frames.MPView;

/**
 *
 *  
 */
public class DOTablePopUp extends JPopupMenu {

    private static final long serialVersionUID = 1L;
    private JTable source;
    private Context c;
    private Context context;
    private MouseListener pop;

    /**
     * Adds a popup menu to the given table
     * @param source
     * @param items
     * @param c
     * @param withMouseListener
     */
    public DOTablePopUp(JTable source, JMenuItem[] items, Context c, boolean withMouseListener) {
        super();
        this.source = source;
        this.context = c;

        for (int i = 0; i < items.length; i++) {
            JMenuItem item = items[i];
            item.setHorizontalTextPosition(JMenuItem.LEFT);
            this.add(item);
        }

        if (withMouseListener) {
            pop = new MousePopupListener();
            addMouseListener(pop);
        }
        this.c = c;
    }

    /**
     * Adds a Popup menu with default action (delete, open) to the given table
     * 
     * @param to A table holding DatabaseObjects, first column MUST be the ID
     * @param c The context of the DatabaeObjects
     * @param withMouseListener
     */
    public static JPopupMenu addDefaultPopupMenu(final JTable to, final Context c, boolean withMouseListener) {

        JMenuItem i = new JMenuItem(Messages.ACTION_DELETE.getValue());
        i.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    DatabaseObject.getObject(c, Integer.valueOf(to.getModel().getValueAt(to.convertRowIndexToModel(to.getSelectedRow()), 0).toString())).delete();
                } catch (NodataFoundException ex) {
                    Log.Debug(DOTablePopUp.class, ex);
                }
            }
        });

        JMenuItem i2 = new JMenuItem(Messages.ACTION_OPEN.getValue());
        i2.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                try {
                    mpv5.YabsViewProxy.instance().getIdentifierView().addTab(DatabaseObject.getObject(c, Integer.valueOf(to.getModel().getValueAt(to.convertRowIndexToModel(to.getSelectedRow()), 0).toString())));
                } catch (NodataFoundException ex) {
                    Log.Debug(DOTablePopUp.class, ex);
                }
            }
        });

        JMenuItem i3 = new JMenuItem(Messages.ACTION_ADDLIST.getValue());
        i3.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                try {
                    mpv5.YabsViewProxy.instance().addToClipBoard(DatabaseObject.getObject(c, Integer.valueOf(to.getModel().getValueAt(to.convertRowIndexToModel(to.getSelectedRow()), 0).toString())));
                } catch (NodataFoundException ex) {
                    Log.Debug(DOTablePopUp.class, ex);
                }
            }
        });


       return new DOTablePopUp(to, new JMenuItem[]{i, i2, i3}, c,withMouseListener);
    }

    class MousePopupListener extends MouseAdapter {

        @Override
        public void mouseClicked(MouseEvent e) {
            if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 1) {
                show(source, e.getX(), e.getY());
            }
        }
    }
}
