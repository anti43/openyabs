package mpv5.utils.ui;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.Serializable;
import java.util.Set;
import java.util.TreeSet;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import mpv5.db.objects.User;
import mpv5.logging.Log;
import org.apache.derby.iapi.services.io.AccessibleByteArrayOutputStream;

/**
 * The Component State manager provides functions to store and restore the state of UI Components, yet only {@link JTable}
 */
public class ComponentStateManager {


    /**
     * @author Florian Strienz
     * @param table
     * @param user
     */
    public static synchronized String persist(JTable table) {
        try {
            ByteArrayOutputStream io = new ByteArrayOutputStream();
            XMLEncoder encoder = new XMLEncoder(io);
            TableColumnModel tableColumnModel = table.getColumnModel();
            final Set<TableColumnLayoutInfo> tableColumnLayoutInfos = new TreeSet<TableColumnLayoutInfo>();
            for (int currentColumnIndex = 0; currentColumnIndex < tableColumnModel.getColumnCount(); currentColumnIndex++) {
                TableColumn tableColumn = tableColumnModel.getColumn(currentColumnIndex);
                TableColumnLayoutInfo tableColumnLayoutInfo = new TableColumnLayoutInfo(
                        tableColumn.getIdentifier().toString(),
                        tableColumnModel.getColumnIndex(tableColumn.getIdentifier()), tableColumn.getWidth());
                tableColumnLayoutInfos.add(tableColumnLayoutInfo);
            }
            encoder.writeObject(tableColumnLayoutInfos);
            encoder.flush();
            encoder.close();
            return io.toString("UTF-8");
        } catch (Exception e) {
            Log.Debug(e);
        }
        return null;
    }

    /**
     * @author Florian Strienz
     *
     * @param data
     * @param view
     * @param table
     * @param user
     */
    public static synchronized void reload(String data, JTable table) {
        try {
            ByteArrayInputStream io = new ByteArrayInputStream(data.getBytes("UTF-8"));
            XMLDecoder decoder = new XMLDecoder(io);
            @SuppressWarnings("unchecked")
            Set<TableColumnLayoutInfo> tableColumnLayoutInfos = (Set<TableColumnLayoutInfo>) decoder.readObject();

            TableColumnModel tableColumnModel = new DefaultTableColumnModel();

            for (TableColumnLayoutInfo tableColumnLayoutInfo : tableColumnLayoutInfos) {
                TableColumn tableColumn = table.getColumn(tableColumnLayoutInfo.getColumnName());
                tableColumn.setPreferredWidth(tableColumnLayoutInfo.getWidth());
                tableColumnModel.addColumn(tableColumn);
            }

            TableColumnModel model = table.getColumnModel();
            for (int i = 0; i < model.getColumnCount(); i++) {
                boolean found = false;
                for (int z = 0; z < tableColumnModel.getColumnCount(); z++) {
                    if (tableColumnModel.getColumn(z).getHeaderValue().equals(model.getColumn(i).getHeaderValue())) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    tableColumnModel.addColumn(model.getColumn(i));
                }
            }

            table.setColumnModel(tableColumnModel);
            decoder.close();
        } catch (Exception e) {
        }
    }

   
}
