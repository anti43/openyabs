package mpv5.ui.misc;

//~--- non-JDK imports --------------------------------------------------------
import mpv5.logging.Log;

//~--- JDK imports ------------------------------------------------------------

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import java.util.Set;
import java.util.TreeSet;

import javax.swing.JTable;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

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

            for (int currentColumnIndex = 0; currentColumnIndex < tableColumnModel.getColumnCount();
                    currentColumnIndex++) {
                TableColumn tableColumn = tableColumnModel.getColumn(currentColumnIndex);
                TableColumnLayoutInfo tableColumnLayoutInfo =
                        new TableColumnLayoutInfo(tableColumn.getIdentifier().toString(),
                        tableColumnModel.getColumnIndex(tableColumn.getIdentifier()),
                        tableColumn.getWidth());

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
    public static synchronized void reload(String data, JTable table) throws Exception {

//      Log.Debug(ComponentStateManager.class, data);//
        if (data != null) {
//            Log.Debug(ComponentStateManager.class, "Reloading layout for: " + table.getName() + " data: " + data);

            try {
                ByteArrayInputStream io =
                        new ByteArrayInputStream(data.getBytes("UTF-8"));
                XMLDecoder decoder = new XMLDecoder(io);
                @SuppressWarnings("unchecked")
                Set<TableColumnLayoutInfo> tableColumnLayoutInfos =
                        (Set<TableColumnLayoutInfo>) decoder.readObject();
                TableColumnModel tableColumnModel = new DefaultTableColumnModel();

                int index = 0;
                for (TableColumnLayoutInfo tableColumnLayoutInfo : tableColumnLayoutInfos) {
                    if ((tableColumnLayoutInfo.getColumnName().length() > 0) && (table.getColumnCount() > 0)) {
                        try {
                            TableColumn tableColumn = table.getColumn(tableColumnLayoutInfo.getColumnName());

                            tableColumn.setPreferredWidth(tableColumnLayoutInfo.getWidth());
                            tableColumnModel.addColumn(tableColumn);

//                          Log.Print(tableColumnLayoutInfo.getColumnName() + " : " + tableColumnLayoutInfo.getWidth());
                        } catch (Exception e) {
                            try {
                                Log.Debug(ComponentStateManager.class, e + ": " + tableColumnLayoutInfo.getColumnName());
                                TableColumn tableColumn = table.getColumn(table.getColumnName(index));

                                tableColumn.setPreferredWidth(tableColumnLayoutInfo.getWidth());
                                tableColumnModel.addColumn(tableColumn);
                            } catch (java.lang.ArrayIndexOutOfBoundsException ex) {
                                //ignore
                                //
//                                Log.Debug(ComponentStateManager.class, "Layout for: " + table.getName() + " data: " + data);
                            }
//                            Log.Debug(ComponentStateManager.class, e + " Count: " + table.getColumnCount());
//
//                            int cols = table.getColumnCount();
//
//                            for (int i = 0; i < cols; i++) {
//                                Log.Debug(ComponentStateManager.class, table.getColumnName(i));
//                            }

//                          throw e;
                        }
                        index++;
                    }
                }

                TableColumnModel model = table.getColumnModel();

                for (int i = 0; i < model.getColumnCount(); i++) {
                    boolean found = false;

                    for (int z = 0; z < tableColumnModel.getColumnCount(); z++) {
                        if (tableColumnModel.getColumn(z).getHeaderValue().equals(
                                model.getColumn(i).getHeaderValue())) {
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
                Log.Debug(e);

                throw e;
            }
        } else {
            throw new NullPointerException();
        }
    }
}


//~ Formatted by Jindent --- http://www.jindent.com

