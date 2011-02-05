package mpv5.ui.misc;


import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.List;
import mpv5.db.common.DatabaseObject;
import mpv5.logging.Log;

public class DatabaseObjectTransferable implements Transferable {

    /**
     * The drag n drop Flavor for DatabaseObjects
     */
    public static final DataFlavor FLAVOR = new DataFlavor(DatabaseObject[].class, "DatabaseObject Transfer");
    private List<DatabaseObject> obj;

    public DatabaseObjectTransferable(List<DatabaseObject> value) {
        obj = value;
    }

    @Override
    public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[]{FLAVOR};
    }

    @Override
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return flavor == FLAVOR;
    }

    @Override
    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
        if (isDataFlavorSupported(flavor)) {
            Log.Debug(this, "Returning array: " + obj);
            return obj.toArray(new DatabaseObject[0]);
        } else {
            throw new UnsupportedFlavorException(flavor);
        }
    }
}
