/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package compat.mp3;

import javax.swing.table.DefaultTableModel;

/**
 * Tablemodel for bills and orders
 * @author anti43
 */
    public class PostenTableModel extends DefaultTableModel {

        public PostenTableModel(Object[][] data, Object[] columnNames) {
            super(data, columnNames);
        }
        Class[] types = new Class[]{java.lang.Integer.class, java.lang.Double.class, java.lang.String.class, java.lang.Double.class, java.lang.Double.class, java.lang.Double.class};

        @Override
        public Class getColumnClass(int columnIndex) {
            return types[columnIndex];
        }
    }
