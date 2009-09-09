/*
 *  This file is part of MP.
 *
 *      MP is free software: you can redistribute it and/or modify
 *      it under the terms of the GNU General Public License as published by
 *      the Free Software Foundation, either version 3 of the License, or
 *      (at your option) any later version.
 *
 *      MP is distributed in the hope that it will be useful,
 *      but WITHOUT ANY WARRANTY; without even the implied warranty of
 *      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *      GNU General Public License for more details.
 *
 *      You should have received a copy of the GNU General Public License
 *      along with MP.  If not, see <http://www.gnu.org/licenses/>.
 */
package mpv5.sandbox;

import java.awt.BorderLayout;
import java.awt.Component;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import mpv5.utils.models.MPTableModel;

/**
 *
 */
public class Test {

    static class CellRendererWithComboBox extends JLabel implements TableCellRenderer {

        JLabel label = new JLabel();

        /**
         * Set this renderer to the given column
         * @param column
         * @param r
         * @param editable
         */
        public void setRendererTo(JTable table, int column, boolean editable) {
            TableColumn col = table.getColumnModel().getColumn(column);
            JComboBox xc = new JComboBox(new DefaultComboBoxModel(new Object[]{1, 2, 3, 4, 5, 6, 7}));
            col.setCellEditor(new ComboBoxEditor(xc));
            col.setCellRenderer(this);
            xc.setEditable(editable);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            if (hasFocus && isSelected) {
                if (isSelected) {
                    label.setForeground(table.getSelectionForeground());
                    label.setBackground(table.getSelectionBackground());
                } else {
                    label.setForeground(table.getForeground());
                    label.setBackground(table.getBackground());
                }
                label.setText((value == null) ? "" : value.toString());
                return label;
            } else {
                label.setText((value == null) ? "" : value.toString());
                return label;
            }
        }

        class ComboBoxEditor extends DefaultCellEditor {

            private final JComboBox box;
            private boolean fire = true;

            public ComboBoxEditor(JComboBox b) {
                super(b);
                this.box = b;
                b.setLightWeightPopupEnabled(false);
            }

            @Override
            public boolean stopCellEditing() {
                if (fire) {
                    super.stopCellEditing();
                }
                return true;
            }

            public void stopCellEditingSilent() {
                fire = false;
                stopCellEditing();
                fire = true;
            }
        }
    }

    public static void main(String... aArgs) throws NoSuchAlgorithmException, IOException {


        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (ClassNotFoundException classNotFoundException) {
        } catch (InstantiationException instantiationException) {
        } catch (IllegalAccessException illegalAccessException) {
        } catch (UnsupportedLookAndFeelException unsupportedLookAndFeelException) {
        }

        JFrame p = new JFrame();
        JTable t = new JTable();
        MPTableModel m = new MPTableModel(new Object[][]{new Object[]{1, 2, 3, 4, 5, 6, 7}, new Object[]{1, 2, 3, 4, 5, 6, 7}, new Object[]{1, 2, 3, 4, 5, 6, 7}, new Object[]{1, 2, 3, 4, 5, 6, 7}, new Object[]{1, 2, 3, 4, 5, 6, 7}});
        m.setEditable(true);
        t.setModel(m);

        Test.CellRendererWithComboBox f = new CellRendererWithComboBox();
        f.setRendererTo(t, 1, true);
        p.setLayout(new BorderLayout());
        p.add(t, BorderLayout.CENTER);
        p.pack();
        p.setVisible(true);
    }

    private static void sortList(List<String> aItems) {
        Collections.sort(aItems, String.CASE_INSENSITIVE_ORDER);
    }

    private static void log(Object aObject) {
        System.out.println(String.valueOf(aObject));
    }

    private static Map<String, String> sortMapByKey(Map<String, String> aItems) {
        TreeMap<String, String> result =
                new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER);
        result.putAll(aItems);
        return result;
    }
}
