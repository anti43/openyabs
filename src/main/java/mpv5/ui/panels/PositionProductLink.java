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
package mpv5.ui.panels;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.db.common.QueryHandler;
import mpv5.db.objects.Product;
import mpv5.db.objects.SubItem;
import mpv5.globals.Messages;
import mpv5.logging.Log;
import mpv5.ui.dialogs.Popup;
import mpv5.usermanagement.MPSecurityManager;

/**
 * From a position of a document (offer, order, invoice, ...) to the product it
 * was added from: the entry "Open product" in the menu of the positions and
 * Ctrl+click on the product number.
 */
public final class PositionProductLink {

    /**
     * The column of the product in the positions model
     */
    public static final int PRODUCT_COLUMN = 10;

    /**
     * What has become of the product of a position
     */
    public enum State {
        /**
         * not added from a product
         */
        NONE,
        /**
         * the product exists
         */
        AVAILABLE,
        /**
         * the product is in the trash
         */
        TRASHED,
        /**
         * the product was deleted for good
         */
        DELETED,
        /**
         * the user may not view products
         */
        DENIED
    }

    private PositionProductLink() {
    }

    /**
     * @param table a positions table
     * @param viewRow a row of the table (view index)
     * @return the id of the product of the row, 0 if none
     */
    public static int productId(JTable table, int viewRow) {
        if (table == null || viewRow < 0 || viewRow >= table.getRowCount()) {
            return 0;
        }
        int row = table.convertRowIndexToModel(viewRow);
        if (table.getModel().getColumnCount() <= PRODUCT_COLUMN) {
            return 0;
        }
        return SubItem.productIdOf(table.getModel().getValueAt(row, PRODUCT_COLUMN));
    }

    /**
     * @param productId
     * @return what has become of the product (from the database, not the
     * cache: a cached product may have been moved to the trash)
     */
    public static State state(int productId) {
        if (productId <= 0) {
            return State.NONE;
        }
        if (!Boolean.TRUE.equals(MPSecurityManager.check(Context.getProduct(), MPSecurityManager.VIEW))) {
            return State.DENIED;
        }
        try {
            Object[][] d = QueryHandler.instanceOf().clone(Context.getProduct()).freeSelectQuery(
                    "SELECT invisible FROM %%tablename%% WHERE ids = " + productId, MPSecurityManager.VIEW, null).getData();
            if (d == null || d.length == 0 || d[0] == null || d[0].length == 0) {
                return State.DELETED;
            }
            Object invisible = d[0][0];
            boolean trashed = invisible instanceof Number ? ((Number) invisible).intValue() != 0
                    : invisible != null && !"0".equals(String.valueOf(invisible)) && !"false".equalsIgnoreCase(String.valueOf(invisible));
            return trashed ? State.TRASHED : State.AVAILABLE;
        } catch (Exception e) {
            Log.Debug(PositionProductLink.class, e);
            return State.DELETED;
        }
    }

    /**
     * @param state
     * @param productId
     * @return the explanation for the menu entry
     */
    public static String hint(State state, int productId) {
        switch (state) {
            case NONE:
                return Messages.POSITION_NO_PRODUCT.toString();
            case DELETED:
                return String.format(Messages.POSITION_PRODUCT_DELETED.toString(), productId);
            case DENIED:
                return Messages.SECURITYMANAGER_DENIED.toString().replace("\n", " ").trim();
            default:
                return Messages.POSITION_OPEN_PRODUCT_TIP.toString();
        }
    }

    /**
     * Opens the product in its own tab (the product view as configured); a
     * message instead if the position has no product or it is gone, a
     * question if it is in the trash
     *
     * @param productId
     * @return the product if it was opened
     */
    public static Product open(int productId) {
        State state = state(productId);
        switch (state) {
            case AVAILABLE:
                try {
                    Product p = (Product) DatabaseObject.getObject(Context.getProduct(), productId);
                    mpv5.YabsViewProxy.instance().addTab(p);
                    return p;
                } catch (Exception e) {
                    Log.Debug(PositionProductLink.class, e);
                    Popup.notice(String.format(Messages.POSITION_PRODUCT_DELETED.toString(), productId));
                }
                return null;
            case TRASHED:
                try {
                    Product p = (Product) DatabaseObject.getObject(Context.getProduct(), productId, true);
                    if (Popup.Y_N_dialog(String.format(Messages.POSITION_PRODUCT_TRASHED.toString(), p.__getCname()), Messages.TRASH_TITLE)) {
                        //as the trash opens its records
                        mpv5.YabsViewProxy.instance().addTab(p);
                        return p;
                    }
                } catch (Exception e) {
                    Log.Debug(PositionProductLink.class, e);
                    Popup.notice(String.format(Messages.POSITION_PRODUCT_DELETED.toString(), productId));
                }
                return null;
            default:
                Popup.notice(hint(state, productId));
                return null;
        }
    }

    /**
     * Adds "Open product" at the top of the menu of the positions (enabled
     * when the selected position has a product) and Ctrl+click on the
     * product number. Works in read-only documents as well.
     *
     * @param table the positions
     * @param menu the menu of the positions
     * @return the new menu entry
     */
    public static JMenuItem install(final JTable table, JPopupMenu menu) {
        final JMenuItem item = new JMenuItem(Messages.POSITION_OPEN_PRODUCT.toString());
        item.setName("openproduct");
        try {
            item.setIcon(new ImageIcon(PositionProductLink.class.getResource("/images/16/knewstuff.png")));
        } catch (Exception e) {
            //no icon
        }
        item.addActionListener(e -> open(productId(table, table.getSelectedRow())));
        menu.insert(item, 0);
        menu.insert(new JPopupMenu.Separator(), 1);
        menu.addPopupMenuListener(new PopupMenuListener() {
            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                update(item, productId(table, table.getSelectedRow()));
            }

            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
            }

            @Override
            public void popupMenuCanceled(PopupMenuEvent e) {
            }
        });
        //the table menu is made again with each new model: Ctrl+click only once
        if (table.getClientProperty(PositionProductLink.class) != null) {
            return item;
        }
        table.putClientProperty(PositionProductLink.class, Boolean.TRUE);
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e) && e.isControlDown() && e.getClickCount() == 1) {
                    int row = table.rowAtPoint(e.getPoint());
                    int col = table.columnAtPoint(e.getPoint());
                    if (row >= 0 && col >= 0 && table.convertColumnIndexToModel(col) == PRODUCT_COLUMN) {
                        int id = productId(table, row);
                        if (id > 0) {
                            e.consume();
                            open(id);
                        }
                    }
                }
            }
        });
        return item;
    }

    /**
     * Enables the entry for the given product
     *
     * @param item
     * @param productId
     */
    static void update(JMenuItem item, int productId) {
        State state = state(productId);
        boolean enabled = state == State.AVAILABLE || state == State.TRASHED;
        item.setEnabled(enabled);
        String text = Messages.POSITION_OPEN_PRODUCT.toString();
        if (state == State.DELETED) {
            text += " " + Messages.POSITION_PRODUCT_GONE;
        } else if (state == State.TRASHED) {
            text += " (" + Messages.TRASH_TITLE + ")";
        }
        item.setText(text);
        item.setToolTipText(hint(state, productId));
    }
}
