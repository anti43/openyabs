/*
 * 
This file is part of YaBS.

YaBS is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

YaBS is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with YaBS.  If not, see <http://www.gnu.org/licenses/>.

 * 
 */
package mpv5.utils.arrays;

import java.awt.Color;
import java.awt.Component;
import javax.swing.tree.TreeNode;
import mpv5.utils.numbers.Ip;
import java.util.Hashtable;
import mpv5.utils.tables.*;
import java.io.File;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import mpv5.db.common.DatabaseObject;
import mpv5.db.objects.Group;
import mpv5.logging.Log;
import mpv5.ui.frames.MPView;
import mpv5.utils.date.DateConverter;
import mpv5.utils.files.FileDirectoryHandler;
import mpv5.utils.files.FileReaderWriter;
import mpv5.utils.models.MPTableModel;

/**
 *
 * 
 */
public class ArrayUtilities {

    public static void addToTable(JTable table, Object[] row) {

        MPTableModel model = (MPTableModel) table.getModel();
        Object[][] data = new Object[model.getRowCount() + 1][row.length];
        Object[] columnNames = new Object[model.getColumnCount()];



        for (int idx = 0; idx < columnNames.length; idx++) {
            columnNames[idx] = model.getColumnName(idx);
        }

        for (int idx = 0; idx < model.getRowCount(); idx++) {
            for (int i = 0; i < row.length; i++) {
                data[idx][i] = model.getValueAt(idx, i);
            }
        }

        data[model.getRowCount()] = row;
        table.setModel(new MPTableModel(model.getTypes(), model.getCanEdits(), data, columnNames));
    }

    public static void addToTable(JTable table, Object[][] rows) {

        TableModel model = table.getModel();
        Object[][] data = new Object[model.getRowCount() + rows.length][rows[0].length];
        Object[] columnNames = new Object[model.getColumnCount()];

        for (int idx = 0; idx < columnNames.length; idx++) {
            columnNames[idx] = model.getColumnName(idx);
        }
        for (int idx = 0; idx < model.getRowCount(); idx++) {
            for (int i = 0; i < rows[0].length; i++) {
                data[idx][i] = model.getValueAt(idx, i);
            }
        }
        for (int i = 0; i < rows.length; i++) {
            data[model.getRowCount() + i] = rows[i];
        }
        table.setModel(new DefaultTableModel(data, columnNames));
    }

    /**
     * Supports 
     * <li>Date
     * <li>Integer
     * <li>Double
     * <li>String
     * 
     * @param d
     * @param column
     * @param aClass
     * @return
     */
    public static Object[][] changeToClassValue(Object[][] d, int column, Class aClass) {
        for (int i = 0; i < d.length; i++) {
            if (aClass.isInstance(new Integer(0))) {
                d[i][column] = Integer.valueOf(String.valueOf(d[i][column]));
            } else if (aClass.isInstance(new Double(0d))) {
                d[i][column] = Double.valueOf(String.valueOf(d[i][column]));
            } else if (aClass.isInstance(new Date())) {
                d[i][column] = DateConverter.getDate(String.valueOf(d[i][column]));
            } else if (aClass.isInstance(new String())) {
                d[i][column] = String.valueOf(d[i][column]);
            }
        }

        return d;
    }
//
//    /**
//     * 
//     * @param table
//     * @param countcol
//     * @param nettocol
//     * @param taxcol
//     * @return
//     */
//    public static CalculatedTableValues calculateTableCols(JTable table, int countcol, int nettocol, int taxcol) {
//
//        TableModel m = table.getModel();
//        Double bruttobetrag = 0d;
//        Double nettobetrag = 0d;
//        Double allovertax = 0d;
//        boolean error = false;
//
//        for (int i = 0; i < m.getRowCount(); i++) {
//            if (m.getValueAt(i, nettocol) != null && m.getValueAt(i, countcol) != null && m.getValueAt(i, taxcol) != null) {
//                try {
//                    bruttobetrag = bruttobetrag + (FormatNumber.parseDezimal(m.getValueAt(i, nettocol).toString()) * (Double.valueOf((FormatNumber.parseDezimal(m.getValueAt(i, taxcol).toString()) / 100) + 1)));
//                    nettobetrag = nettobetrag + (FormatNumber.parseDezimal(m.getValueAt(i, nettocol).toString()));
//                    allovertax = allovertax + ((FormatNumber.parseDezimal(m.getValueAt(i, taxcol).toString())) + 100);
//                } catch (Exception exception) {
//                    Log.Debug(this,exception);
//                    error = true;
//                }
//            }
//        }
//
//        if (nettobetrag > 0) {
//            allovertax = (allovertax / nettobetrag);
//        } else {
//            allovertax = 0d;
//        }
//
//        if (!error) {
//            return new CalculatedTableValues(bruttobetrag, nettobetrag, allovertax);
//        } else {
//            return null;
//        }
//    }

    /**
     * Returns a table column's data as array
     * @param table
     * @param column
     * @return
     */
    public static Object[] getColumnAsArray(JTable table, int column) {
        MPTableModel model = (MPTableModel) table.getModel();
        Object[] data = new Object[model.getRowCount()];

        for (int idx = 0; idx < model.getRowCount(); idx++) {
            for (int i = 0; i < model.getColumnCount(); i++) {
                if (i == column) {
                    data[idx] = model.getValueAt(idx, i);
                }
            }
        }
        return data;
    }

    /**
     * Converts a Hashtable to an array of keys and values
     * @param table
     * @return
     */
    public static Object[][] hashTableToArray(Hashtable<String, Object> table) {
        Object[][] n = new Object[table.size()][2];
        String[] keys = hashTableKeysToArray(table);
        int i = 0;

        for (int j = 0; j < keys.length; j++) {
            String k = keys[j];
            n[i][1] = table.get(k);
            n[i][0] = k;
            i++;
        }
//
//        Log.PrintArray(n);
        return n;
    }

//    public static MPTableModel getModelCopy(JTable data) {
//
//    }
    public static void removeColumn(JTable table, int vColIndex) {
        MPTableModel model = (MPTableModel) table.getModel();
        TableColumn col = table.getColumnModel().getColumn(vColIndex);
        int columnModelIndex = col.getModelIndex();
        Vector data = model.getDataVector();
        Vector colIds = model.getColumnIdentifiers();

        // Remove the column from the table
        table.removeColumn(col);

        // Remove the column header from the table model
        colIds.removeElementAt(columnModelIndex);

        // Remove the column data
        for (int r = 0; r < data.size(); r++) {
            Vector row = (Vector) data.get(r);
            row.removeElementAt(columnModelIndex);
        }
        model.setDataVector(data, colIds);

        // Correct the model indices in the TableColumn objects
        // by decrementing those indices that follow the deleted column
        Enumeration enumd = table.getColumnModel().getColumns();
        for (; enumd.hasMoreElements();) {
            TableColumn c = (TableColumn) enumd.nextElement();
            if (c.getModelIndex() >= columnModelIndex) {
                c.setModelIndex(c.getModelIndex() - 1);
            }
        }
        model.fireTableStructureChanged();

    }

    /**
     * Replaces a columns data in a table
     * @param data
     * @param column
     * @param columndata
     */
    public static Object[][] replaceColumn(Object[][] data, int column, Object[] columndata) {
        for (int idx = 0; idx < data.length; idx++) {
            for (int i = 0; i < data[idx].length; i++) {
                if (i == column) {
                    if (columndata != null) {
                        data[idx][i] = columndata[idx];
                    } else {
                        data[idx][i] = null;
                    }
                }
            }
        }
        return data;
    }

    /**
     * Replaces a columns data in a table
     * @param table
     * @param column
     * @param columndata
     */
    public static void replaceColumn(JTable table, int column, Object[] columndata) {
        TableModel model = table.getModel();
        Object[][] data = new Object[model.getRowCount()][model.getColumnCount()];
        String[] columnNames = new String[model.getColumnCount()];

        for (int idx = 0; idx < columnNames.length; idx++) {
            columnNames[idx] = model.getColumnName(idx);
        }

        for (int idx = 0; idx < model.getRowCount(); idx++) {
            for (int i = 0; i < model.getColumnCount(); i++) {
                if (i != column) {
                    data[idx][i] = model.getValueAt(idx, i);
                } else {
                    if (columndata != null) {
                        data[idx][i] = columndata[idx];
                    } else {
                        data[idx][i] = null;
                    }
                }
            }
        }

        table.setModel(new MPTableModel(data, columnNames));
    }

    /**
     *
     * @param table
     * @param columns
     * @param separator
     * @param filename
     * @param suffix
     * @return
     */
    public static File tableModelToFile(JTable table, int[] columns, String separator, String filename, String suffix) {
        File file = FileDirectoryHandler.getTempFile(filename, suffix);
        FileReaderWriter rw = new FileReaderWriter(file, "UTF8");
        TableModel model = table.getModel();
        String[] data = new String[model.getRowCount()];
        String line = "";

        for (int idx = 0; idx < model.getRowCount(); idx++) {
            line = "";
            for (int i = 0; i < model.getColumnCount(); i++) {
                for (int j = 0; j < columns.length; j++) {
                    if (i == columns[j]) {
                        if (model.getValueAt(idx, i) != null) {
                            line += model.getValueAt(idx, i) + separator;
                        } else {
                            line += separator;
                        }
                    }
                }
            }
            data[idx] = line.substring(0, line.length() - separator.length());
        }

        rw.writeWCharset(data);
        Log.Debug(ArrayUtilities.class, file);
        return file;
    }

    public static DefaultTableModel toTableModel(HashMap data) {

        Object[] array = data.keySet().toArray();
        Object[][] table = new Object[array.length][1];

        for (int i = 0; i < array.length; i++) {
            table[i][0] = array[i];
        }

        return new DefaultTableModel(table, new Object[]{"Devices"});
    }

    @SuppressWarnings("unchecked")
    public static List removeDuplicates(List arlList) {
        Set set = new HashSet();
        for (Iterator<Ip> iter = arlList.iterator(); iter.hasNext();) {
            Object element = iter.next();
            set.add(element);
        }
        arlList.clear();
        arlList.addAll(set);

        return arlList;
    }

//    /**
//     *
//     * @param data
//     * @return
//     */
//    @SuppressWarnings("unchecked")
//    public static DefaultTreeModel toTreeModel(HashMap<String, Ip> data) {
////
////        data = new HashMap<String, Ip>();
////        data.put("f", new Ip("192.168.8.1"));
////        data.put("fg", new Ip("192.168.8.2"));
////        data.put("g", new Ip("192.168.8.3"));
////        data.put("d", new Ip("192.168.8.4"));
////        data.put("gdfg", new Ip("192.168.8.5"));
////
////        data.put("dfgfdg", new Ip("192.168.9.1"));
////        data.put("yrtytry", new Ip("192.168.9.2"));
////        data.put("tryty", new Ip("192.168.9.4"));
////
////        data.put("ertertrt", new Ip("191.168.9.1"));
////        data.put("56g", new Ip("191.168.9.2"));
////        data.put("fghtyi", new Ip("191.168.9.4"));
////
////        data.put("pop", new Ip("192.162.9.1"));
////        data.put("ijlkghh", new Ip("192.162.9.2"));
////        data.put("uyiouy", new Ip("192.167.9.4"));
//
//
//        DefaultMutableTreeNode node1 = new DefaultMutableTreeNode("Devices");
//        System.out.println("Converting to tree: " + data.keySet().size() + " values");
//
//        DefaultMutableTreeNode subnode1 = null;
//
//        try {
//            Collection<Ip> ips = data.values();
//            Iterator<Ip> ipIterator = ips.iterator();
//
//            ArrayList<Ip> allIps = new ArrayList<Ip>(data.values());
//            ArrayList<Ip> subnet1 = new ArrayList<Ip>();
//            ArrayList<Ip> subnet2 = new ArrayList<Ip>();
//            ArrayList<Ip> subnet3 = new ArrayList<Ip>();
//
//            subnode1 = new DefaultMutableTreeNode("IP");
//
//            while (ipIterator.hasNext()) {
//                Ip ip = ipIterator.next();
//                subnet1.add(new Ip(String.valueOf(ip.getPart0()) + ".0.0.0"));
//                subnet2.add(new Ip(String.valueOf(ip.getPart0()) + "." + String.valueOf(ip.getPart1()) + ".0.0"));
//                subnet3.add(new Ip(String.valueOf(ip.getPart0()) + "." + String.valueOf(ip.getPart1()) + "." + String.valueOf(ip.getPart2()) + ".0"));
//
//            }
//
//            subnet1 = removeDuplicates(subnet1);
//            subnet2 = removeDuplicates(subnet2);
//            subnet3 = removeDuplicates(subnet3);
//
//
////*********************SUBNET 1  xxx.0.0.0 ******************
//            for (int i = 0; i < subnet1.size(); i++) {
//                Ip subn1 = subnet1.get(i);
//                DefaultMutableTreeNode nodex = new DefaultMutableTreeNode(subn1);
//                subnode1.add(nodex);
//
//
////*********************SUBNET 2  0.xxx.0.0 ******************
//                for (int ii = 0; ii < subnet2.size(); ii++) {
//                    Ip subn2 = subnet2.get(ii);
//                    if (subn2.getPart0() == subn1.getPart0()) {
//                        DefaultMutableTreeNode nodey = new DefaultMutableTreeNode(subn2);
//                        nodex.add(nodey);
//
//
////*********************SUBNET 3  0.0.xxx.0 ******************
//                        for (int iii = 0; iii < subnet3.size(); iii++) {
//                            Ip subn3 = subnet3.get(iii);
//                            if (subn3.getPart0() == subn2.getPart0() &&
//                                    subn3.getPart1() == subn2.getPart1()) {
//                                DefaultMutableTreeNode nodez = new DefaultMutableTreeNode(subn3);
//                                nodey.add(nodez);
//
//
//
////*********************SUBNET 4  0.0.0.xxx ******************
//                                for (int iiii = 0; iiii < allIps.size(); iiii++) {
//                                    Ip ip = allIps.get(iiii);
//
//                                    if (!new Ip("0.0.0.0").equals(ip) && ip.getPart0() == subn3.getPart0() &&
//                                            ip.getPart1() == subn3.getPart1() &&
//                                            ip.getPart2() == subn3.getPart2()) {
//                                        nodez.add(new DefaultMutableTreeNode(ip));
//                                    }
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        node1.add(subnode1);
//        DefaultTreeModel model = new DefaultTreeModel(node1);
//        return model;
//    }
    public static ArrayList<String> getSelectionFromTree(JTree tree) {
        ArrayList<String> list = new ArrayList<String>();
        TreePath[] nodes = tree.getSelectionPaths();

        for (int i = 0; i < nodes.length; i++) {
            TreePath temp = nodes[i];
            Object tempObj = temp.getLastPathComponent();
            DefaultMutableTreeNode treNode = (DefaultMutableTreeNode) tempObj;
            String device = String.valueOf(treNode.getUserObject());
            list.add(device);
        }

        return list;
    }

    public static Object[][] tableModelToArray(JTable table) {
        return tableModelToArray(table, false);
    }

    public static Object[][] tableModelToArray(JTable table, boolean onlyTheSelectedRows) {

        DefaultTableModel model = (DefaultTableModel) table.getModel();
        Object[][] data;
        int[] selections = table.getSelectedRows();
        if (onlyTheSelectedRows) {
            data = new Object[selections.length][model.getColumnCount()];
        } else {
            data = new Object[model.getRowCount()][model.getColumnCount()];
        }

        if (!onlyTheSelectedRows) {
            for (int idx = 0; idx < model.getRowCount(); idx++) {
                for (int i = 0; i < model.getColumnCount(); i++) {
                    data[idx][i] = model.getValueAt(idx, i);
                }
            }
        } else {
            for (int idx = 0; idx < selections.length; idx++) {
                int row = selections[idx];
                for (int i = 0; i < model.getColumnCount(); i++) {
                    data[idx][i] = model.getValueAt(row, i);
                }

            }
        }
        return data;
    }

    public static void removeSelectedRowFromTable(JTable table) {
        TableModel model = table.getModel();
        Object[][] data = new Object[model.getRowCount()][model.getColumnCount()];
        Object[] columnNames = new Object[model.getColumnCount()];

        for (int idx = 0; idx < columnNames.length; idx++) {
            columnNames[idx] = model.getColumnName(idx);
        }
        for (int idx = 0; idx < model.getRowCount(); idx++) {
            for (int i = 0; i < model.getColumnCount(); i++) {
                if (idx != table.getSelectedRow()) {
                    data[idx][i] = model.getValueAt(idx, i);
                }
            }
        }

        table.setModel(new DefaultTableModel(data, columnNames));
    }

    public static Object[][] reverseArray(Object[][] array) {
        //Reverse order
        int i = 0, j = array.length - 1;
        while (i < j) {
            Object[] h = array[i];
            array[i] = array[j];
            array[j] = h;
            i++;
            j--;
        }
        return array;
    }

    public static void addRowToTable(JTable table) {
        Object[] o = new Object[table.getModel().getColumnCount()];
        for (int idx = 0; idx < o.length; idx++) {
            o[idx] = null;
        }
        DefaultTableModel m = (DefaultTableModel) table.getModel();
        m.addRow(o);
    }

    /**
     * 
     * @param original_array
     * @param value
     * @param place If smaller than< 0, value will be placed before all others
     * @return
     */
    public static Object[][] inserValue(Object[][] original_array, Object value, Integer place) {
        Object[][] array_formatiert = new Object[0][0];
        if (original_array.length > 0) {
            array_formatiert = new Object[original_array.length][original_array[0].length + 1];
            for (int zeile = 0; zeile < array_formatiert.length; zeile++) {
                int merker = 0;
                if (place != null) {
                    if (place >= 0) {
                        for (int spalte = 0; spalte < array_formatiert[zeile].length; spalte++, merker++) {
                            if (spalte == place) {
                                array_formatiert[zeile][place] = value;
                                merker--;
                            } else {
                                array_formatiert[zeile][spalte] = original_array[zeile][merker];
                            }
                        }
                    } else {
                        array_formatiert[zeile][0] = value;
                        for (int spalte = 0; spalte < original_array[zeile].length; spalte++) {
                            array_formatiert[zeile][spalte + 1] = original_array[zeile][spalte];
                        }
                    }
                } else {
                    for (int spalte = 0; spalte < original_array[zeile].length - 1; spalte++) {
                        array_formatiert[zeile][spalte] = original_array[zeile][spalte];
                        if (spalte == original_array[zeile].length - 2) {
                            array_formatiert[zeile][spalte + 1] = value;
                        }
                    }
                }
            }
        }
        return array_formatiert;
    }

    /**
     * 
     * @param table
     * @param columns
     * @return
     */
    public static String[][] formatTableArrayYesNo(String[][] table, int[] columns) {
        for (int i = 0; i < table.length; i++) {

            for (int z = 0; z < table[i].length; z++) {


                for (int h = 0; h < columns.length; h++) {

                    if (z == columns[h]) {

                        if (table[i][z].equals("0")) {
                            table[i][z] = "Nein";
                        } else {
                            table[i][z] = "Ja";
                        }
                    }


                }


            }

        }


        return table;

    }

    /**
     * Copies an array to a new one
     * @param originalarray
     * @return
     */
    public static Object[][] toObjectArray(String[][] originalarray) {
        Object[][] data = new Object[originalarray.length][];
        for (int idx = 0; idx < originalarray.length; idx++) {
            for (int i = 0; i < originalarray[idx].length; i++) {
                data[idx][i] = String.valueOf(originalarray[idx][i]);
            }
        }
        return data;
    }

    public static String[] hashTableKeysToArray(Hashtable<String, Object> data) {
        Object[] array = data.keySet().toArray();
        String[] keyz = new String[array.length];

        for (int i = 0; i < array.length; i++) {
            keyz[i] = (array[i]).toString();
        }

        return keyz;
    }

    /**
     * Remove certain rows out of an array
     * @param array
     * @param column
     * @param removeTrigger
     * @return
     */
    public static Object[][] removeRows(Object[][] array, int column, Object removeTrigger) {
        List<Object[]> list = new Vector<Object[]>();
        for (int i = 0; i < array.length; i++) {
            Object[] objects = array[i];
            if (objects[column] != removeTrigger && !removeTrigger.equals(objects[column])) {
                list.add(objects);
            }
        }
        return list.toArray(new Object[][]{});
    }

    /**
     * Converts a HashMap to a 2-column array {key, value}
     * @param map
     * @return
     */
    public static Object[][] hashMapToArray(HashMap<String, Object> map) {
        return mapToArray(map);
    }
     /**
     * Converts a HashMap to a 2-column array {key, value}
     * @param map
     * @return
     */
    public static Object[][] mapToArray(Map<String, Object> map) {
        Object[][] data = new Object[map.size()][2];
        String[] arr = map.keySet().toArray(new String[]{});

        for (int idx = 0; idx < arr.length; idx++) {
            data[idx][0] = arr[idx];
            data[idx][1] = map.get(arr[idx]);
        }

        return data;
    }

    public static Object[][] merge(Object[][] array1, Object[][] array2) {
        if (array1 == null) {
            array1 = new Object[0][0];
        }
        if (array2 == null) {
            array2 = new Object[0][0];
        }

        int z = 0;
        if (array1 != null && array1.length > 0) {
            z = array1[0].length;
        }

        if (array2 != null && array2.length > 0) {
            if (z < array2[0].length) {
                z = array2[0].length;
            }
        }

        Object[][] mergedArray = new Object[array1.length + array2.length][z];
        int i = 0;

        for (i = 0; i < array1.length; i++) {
            for (int k = 0; k < array1[i].length; k++) {
                mergedArray[i][k] = array1[i][k];
            }
        }

        for (int l = 0; l < array2.length; l++) {
            for (int k = 0; k < array2[l].length; k++) {
                mergedArray[i + l][k] = array2[l][k];
            }
        }
        return mergedArray;
    }

    public static Object[][] merge(Object[] array1, Object[][] array2) {
        if (array1 == null) {
            array1 = new Object[0];
        }
        if (array2 == null) {
            array2 = new Object[0][0];
        }

        int z = 0;
        if (array1 != null && array1.length > 0) {
            z = array1.length;
        } else if (array2 != null && array2.length > 0) {
            z = array2[0].length;
        } else {
            z = 0;
        }

        Object[][] mergedArray = new Object[array1.length + array2.length][z];
        int i = 0;
        for (i = 0; i < array1.length; i++) {
            mergedArray[0][i] = array1[i];
        }

        for (int l = 0; l < array2.length; l++) {
            for (int k = 0; k < array2[l].length; k++) {
                mergedArray[i + l][k] = array2[l][k];
            }
        }
        return mergedArray;
    }

    public static Object[] merge(Object[] array1, Object[] array2) {
        if (array1 == null) {
            array1 = new Object[0];
        }
        if (array2 == null) {
            array2 = new Object[0];
        }

        Object[] mergedArray = new Object[array1.length + array2.length];
        int i = 0;

        for (i = 0; i < array1.length; i++) {
            mergedArray[i] = array1[i];
        }

        for (int l = 0; l < array2.length; l++) {
            mergedArray[i + l] = array2[l];
        }

        return mergedArray;
    }

    public static String[] reverseArray(String[] str) {
        int i = 0, j = str.length - 1;
        while (i < j) {
            String h = str[i];
            str[i] = str[j];
            str[j] = h;
            i++;
            j--;
        }
        return str;

    }

    public static String[][] reverseArray(String[][] str) {
        int i = 0, j = str.length - 1;
        while (i < j) {
            String[] h = str[i];
            str[i] = str[j];
            str[j] = h;
            i++;
            j--;
        }
        return str;

    }

    public static String[][] ObjectToStringArray(Object[][] array1) {
        if (array1 == null || array1.length < 1) {
            array1 = new Object[0][0];
        }

        String[][] mergedArray = null;
        try {
            mergedArray = new String[array1.length][array1[0].length];
            int i = 0;
            for (i = 0; i < array1.length; i++) {

                for (int k = 0; k < array1[i].length; k++) {

                    mergedArray[i][k] = array1[i][k].toString();
                }
            }
        } catch (Exception e) {
            return new String[0][0];
        }
        return mergedArray;
    }

    public static String[] SmallObjectToStringArray(Object[] array1) {
        if (array1 == null) {
            array1 = new Object[0];
        }

        String[] mergedArray = new String[array1.length];
        int i = 0;
        for (i = 0; i < array1.length; i++) {

            mergedArray[i] = array1[i].toString();
        }
        return mergedArray;
    }

    public static Object[] ObjectToSingleColumnArray(Object[][] array1) {
        if (array1 == null) {
            array1 = new Object[0][0];
        }

        Object[] mergedArray = new Object[array1.length];
        int i = 0;
        for (i = 0; i < array1.length; i++) {
            mergedArray[i] = array1[i][0];
        }
        return mergedArray;

    }

    public static Object[] sort(Object[] items) {
        Arrays.sort(items);
        return items;
    }
//
//    public static Object[][] sort(Object[][] items, int columnToSortBy) {
//
//        for (int i = 0; i < items.length; i++) {
//            Object[] objects = items[i];
//
//        }
//
//        Arrays.sort(items);
//
//        return items;
//    }

    public static ArrayList<String[]> StringArrayToList(String[][] array) {
        ArrayList<String[]> list = new ArrayList<String[]>();

        for (int i = 0; i < array.length; i++) {

            list.add(array[i]);
        }

        return list;
    }

    public static ArrayList TableModelToList(JTable mode) {
        ArrayList<String[]> list = new ArrayList<String[]>();
        DefaultTableModel model = (DefaultTableModel) mode.getModel();

        for (int i = 0; i < model.getRowCount(); i++) {
            String[] str = null;
            for (int j = 0; j < model.getColumnCount(); j++) {
                str = new String[model.getColumnCount()];
                str[j] = model.getValueAt(i, j).toString();
            }
            list.add(str);
        }

        return list;
    }

    /**
     *
     * @param list
     * @return
     */
    public static Integer[][][] listToIntegerArray(List list) {

        Integer[][][] str = new Integer[list.size()][][];

        ArrayList a, b;

        for (int i = 0; i < list.size(); i++) {

            a = (ArrayList) list.get(i);
            str[i] = new Integer[a.size()][];

            for (int m = 0; m < a.size(); m++) {

                b = (ArrayList) a.get(m);

                str[i][m] = new Integer[b.size()];

                for (int k = 0; k < a.size(); k++) {

                    str[i][m][k] = (Integer) b.get(k);
                }


            }

        }

        return str;
    }

    /**
     *
     * @param list
     * @return
     */
    public static String[] listToStringArray(List list) {

        String[] str = new String[list.size()];
        for (int i = 0; i < list.size(); i++) {

            str[i] = (String) list.get(i);
        }
        return str;
    }

    public static String[][] listToStringArrayArray(List<String[]> list) {

        String[][] str = new String[list.size()][];

        for (int i = 0; i < list.size(); i++) {
            str[i] = list.get(i);
        }

        return str;

    }

    public static Object[][] listToTableArray(List<Object[]> list) {

        int width = 0;
        for (int i = 0; i < list.size(); i++) {
            Object[] objects = list.get(i);
            if (objects.length > width) {
                width = objects.length;
            }
        }

        Object[][] arr = new Object[list.size()][width];
        if (list.size() > 0) {
            for (int i = 0; i < list.size(); i++) {
                Object[] objects = list.get(i);
                for (int j = 0; j < objects.length; j++) {
                    Object object = objects[j];
                    arr[i][j] = object;
                }
            }
        }
        return arr;
    }

    public static String[][] StringListToTableArray(ArrayList list) {

        String[][] str = new String[list.size()][];

        for (int i = 0; i < list.size(); i++) {
            str[i] = (String[]) list.get(i);
        }

        return str;
    }

    @SuppressWarnings("unchecked")
    public static ArrayList merge(ArrayList list1, ArrayList list2) {
        Iterator it1 = list1.iterator();
        Iterator it2 = list2.iterator();

        ArrayList<Double> list3 = new ArrayList();

        while (it1.hasNext()) {
            list3.add((Double) it1.next());
        }

        while (it2.hasNext()) {
            list3.add((Double) it2.next());
        }

        return list3;
    }

    /**
     * list 1 +2 must have same element count!
     *
     * @param list1
     * @param list2
     * @return list1.values(n) - list2.values(n)
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public static ArrayList substract(ArrayList<Double> list1, ArrayList<Double> list2) throws Exception {
        Iterator it1 = list1.iterator();
        Iterator it2 = list2.iterator();

        if (list1.size() != list2.size()) {
            throw new Exception("list 1 + 2 must have same element count!");
        }

        ArrayList<Double> list3 = new ArrayList();

        while (it1.hasNext() && it2.hasNext()) {
            Double value = (Double) it1.next() - (Double) it2.next();
            list3.add(value);
        }

        return list3;
    }

    /**
     * list 1 + 2 must have same element count!
     *
     * @param list1
     * @param list2
     * @return list1.values(n) + list2.values(n)
     * @throws Exception
     */
    public static ArrayList<Double> add(ArrayList<Double> list1, ArrayList<Double> list2) throws Exception {

        Iterator it1 = list1.iterator();
        Iterator it2 = list2.iterator();

        if (list1.size() != list2.size()) {
            throw new Exception("list 1 + 2 must have same element count!");
        }
        @SuppressWarnings("unchecked")
        ArrayList<Double> list3 = new ArrayList();

        while (it1.hasNext() && it2.hasNext()) {
            Double value = (Double) it1.next() + (Double) it2.next();
            list3.add(value);
        }

        return list3;
    }

    /**
     * Create an Enumeration from an Array
     * @param obj
     * @return
     */
    public static Enumeration arrayToEnumeration(final Object obj) {
        Class type = obj.getClass();
        if (!type.isArray()) {
            throw new IllegalArgumentException(obj.getClass().toString());
        } else {
            return (new Enumeration() {

                int size = Array.getLength(obj);
                int cursor;

                public boolean hasMoreElements() {
                    return (cursor < size);
                }

                public Object nextElement() {
                    return Array.get(obj, cursor++);
                }
            });
        }
    }
}
