package mpv5.utils.ui;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
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
import mpv5.utils.files.FileDirectoryHandler;

/**
 * The Component State manager provides functions to store and restore the state of UI Components, e.g. {@link JTable}s
 */
public class ComponentStateManager {

    static File file = new File("persitencetest.txt");

    /**
     * Use this method to persist the state of a component bound to a {@link User}
     * @param view 
     * @param component
     * @param user
     */
    public static void saveSate(JComponent  view, JComponent component, User user) {

        if (component instanceof JTable) {
            persist(view, (JTable) component, user);
        } else {
            throw new UnsupportedOperationException("Not yet supported: " + component.getClass());
        }

    }

    private static void reloadState(JComponent  view, JComponent component, User user) {

        if (component instanceof JTable) {
            reload(view, (JTable) component, user);
        } else {
            throw new UnsupportedOperationException("Not yet supported: " + component.getClass());
        }
    }

    /**
     * @author Florian Strienz
     * @param table
     * @param user
     */
    private static void persist(JComponent  view, JTable table, User user) {
        try {
            XMLEncoder encoder = new XMLEncoder(new FileOutputStream(file));
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
        } catch (Exception e) {
            Log.Debug(e);
        } finally {
        }
    }

    private static void reload(JComponent  view, JTable table, User user) {
        try {

            XMLDecoder decoder = new XMLDecoder(new FileInputStream(file));
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

    public static class TableColumnLayoutInfo implements Serializable, Comparable<TableColumnLayoutInfo> {

        @Override
        public int compareTo(TableColumnLayoutInfo o) {
            return order - o.order;
        }
        private int order;
        private String columnName;
        private int width;

        public TableColumnLayoutInfo() {
        }

        public TableColumnLayoutInfo(String columnName, int order, int width) {
            this.columnName = columnName;
            this.order = order;
            this.width = width;
        }

        public int getOrder() {
            return order;
        }

        public void setOrder(int order) {
            this.order = order;
        }

        public int getWidth() {
            return width;
        }

        public void setWidth(int width) {
            this.width = width;
        }

        public String getColumnName() {
            return columnName;
        }

        public void setColumnName(String columnName) {
            this.columnName = columnName;
        }
    }
}
