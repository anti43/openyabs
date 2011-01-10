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
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import mpv5.db.common.Context;
import mpv5.db.common.QueryHandler;
import mpv5.globals.Messages;
import mpv5.ui.frames.MPView;
import mpv5.ui.panels.DataPanel;
import mpv5.utils.files.FileDirectoryHandler;

/**
 *
 *  
 */
public class FileTablePopUp extends JPopupMenu {

    private static final long serialVersionUID = 1L;
    private static MouseListener pop;
    private static FileTablePopUp p;

    /**
     *
     * @param t
     * @return
     */
    public static FileTablePopUp instanceOf(final JTable t) {
        if (p == null) {
            p = FileTablePopUp.getDefaultPopupMenu(t);
        }
        return p;
    }

    private static FileTablePopUp getDefaultPopupMenu(final JTable dataTable) {
        FileTablePopUp t = new FileTablePopUp();
        t.addDefaultPopupMenu(dataTable);
        return t;
    }

    /**
     * Adds a popup menu to the given table
     * @param items
     */
    public FileTablePopUp(JMenuItem[] items) {
        super();

        for (int i = 0; i < items.length; i++) {
            JMenuItem item = items[i];
            item.setHorizontalTextPosition(JMenuItem.LEFT);
            this.add(item);
        }
    }

    /**
     * Adds a Popup menu with default actions (delete, open, edit) to the given table
     * 
     * @param dataTable A table holding File Objects, first column MUST be the ID, <br/>
     * second column the desired file name
     */
    public void addDefaultPopupMenu(final JTable dataTable) {

        JMenuItem x = new JMenuItem(Messages.SAVE_AS.getValue());
        x.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                FileDirectoryHandler.save(QueryHandler.instanceOf().clone(Context.getFiles()).
                        retrieveFile(dataTable.getModel().getValueAt(dataTable.getSelectedRow(), 0).
                        toString(), new File(FileDirectoryHandler.getTempDir() + dataTable.getModel().
                        getValueAt(dataTable.getSelectedRow(), 1).toString())));
            }
        });

        JMenuItem i = new JMenuItem(Messages.ACTION_OPEN.getValue());
        i.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                FileDirectoryHandler.open(QueryHandler.instanceOf().clone(Context.getFiles()).
                        retrieveFile(dataTable.getModel().getValueAt(dataTable.getSelectedRow(), 0).
                        toString(), new File(FileDirectoryHandler.getTempDir() + dataTable.getModel().
                        getValueAt(dataTable.getSelectedRow(), 1).toString())));
            }
        });

        JMenuItem h = new JMenuItem(Messages.ACTION_EDIT.getValue());
        h.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                FileDirectoryHandler.edit(QueryHandler.instanceOf().clone(Context.getFiles()).
                        retrieveFile(dataTable.getModel().getValueAt(dataTable.getSelectedRow(), 0).
                        toString(), new File(FileDirectoryHandler.getTempDir() + dataTable.getModel().
                        getValueAt(dataTable.getSelectedRow(), 1).toString())));
            }
        });

        JMenuItem g = new JMenuItem(Messages.ACTION_DELETE.getValue());
        g.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                try {
                    QueryHandler.instanceOf().clone(Context.getFiles()).removeFile(dataTable.getModel().getValueAt(dataTable.getSelectedRow(), 0).toString());

                    ((DataPanel) mpv5.YabsViewProxy.instance().getCurrentTab()).refresh();
                } catch (Exception ex) {
                    mpv5.logging.Log.Debug(ex);//Logger.getLogger(FileTablePopUp.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });

        this.add(x);
        this.add(i);
        this.add(h);
        this.add(g);
    }

    /**
     * Remove the previously added Popup Mouselistener, if any
     * @param to
     */
    public static void clear(final JTable to) {
        to.removeMouseListener(pop);
    }

    /**
     * Create a new, empty popup
     */
    public FileTablePopUp() {
        super();
    }

    class MousePopupListener extends MouseAdapter {

        @Override
        public void mouseReleased(MouseEvent e) {
            if (e.isPopupTrigger()) {
                JTable source = (JTable) e.getSource();
                int row = source.rowAtPoint(e.getPoint());
                int column = source.columnAtPoint(e.getPoint());

                if (!source.isRowSelected(row)) {
                    source.changeSelection(row, column, false, false);
                }

                show(source, e.getX(), e.getY());
            }
        }
    }
}
