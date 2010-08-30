package mpv5.ui.misc;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.TransferHandler;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.db.common.NodataFoundException;

/**
 *
 */
public class DragTableHandlerForDBOs extends TransferHandler {

    private final JTable table;
    private final Context context;

    public DragTableHandlerForDBOs(JTable table, Context context) {
        this.table = table;
        this.table.setTransferHandler(this);
        this.table.setDragEnabled(true);
        this.context = context;
    }

    @Override
    public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) {
        if (table == comp) {
            return false;
        }
        return true;
    }

    @Override
    protected Transferable createTransferable(JComponent c) {
        if (c == table) {
            int[] row = table.getSelectedRows();

            List<DatabaseObject> l = new ArrayList<DatabaseObject>();
            if (table.getModel().getValueAt(row[0], 0) instanceof DatabaseObject) {
                for (int i = 0; i < row.length; i++) {
                    int ro = row[i];
                    l.add((DatabaseObject) table.getModel().getValueAt(ro, 0));

                }

                return new DatabaseObjectTransferable(l);
            } else {
                try {
                    for (int i = 0; i < row.length; i++) {
                        int ro = row[i];
                        l.add(DatabaseObject.getObject(context, Integer.valueOf(table.getModel().getValueAt(ro, 0).toString())));
                    }
            
                    return new DatabaseObjectTransferable(l);
                } catch (NodataFoundException ex) {
                    l.add(DatabaseObject.getObject(context));
          
                    return new DatabaseObjectTransferable(l);
                }
            }
        }
        return null;
    }

    @Override
    public boolean importData(JComponent comp, Transferable t) {
//        if (comp == table) {
//            try {
//                DatabaseObject value = t.getTransferData(DatabaseObjectTransferable.FLAVOR);
//
//                int row = table.getSelectedRow();
//                int col = table.getSelectedColumn();
//                table.getModel().setValueAt(value, row, col);
//                return true;
//            } catch (Exception e) {
//            }
//        }
//        return super.importData(comp, t);
        return false;
    }

    @Override
    public int getSourceActions(JComponent c) {
        if (table == c) {
            return DnDConstants.ACTION_COPY_OR_MOVE;
        } else {
            return super.getSourceActions(c);
        }
    }
}
