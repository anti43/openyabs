
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
package mpv5.utils.tables;

//~--- non-JDK imports --------------------------------------------------------
import mpv5.db.common.DatabaseObject.Entity;

import mpv5.globals.LocalSettings;

import mpv5.logging.Log;

import mpv5.utils.models.MPTableModel;

//~--- JDK imports ------------------------------------------------------------
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

/**
 *
 *
 */
public class TableFormat {

   /**
    * Changes table values from text to class values,
    * currently supported classes:
    * - <code>Boolean.class</code>: "1" and "true" will be true, false otherwise
    *
    * @param values The original values
    * @param aClass The class to be changed
    * @param cols The column to change
    * @return
    */
   public static Object[][] changeToClassValue(Object[][] values, Class aClass, int[] cols) {
      try {
         Object[][] data = new Object[values.length][values[0].length];

         for (int idx = 0; idx < values.length; idx++) {
            for (int k = 0; k < cols.length; k++) {
               if (aClass.getName().equalsIgnoreCase("java.lang.Boolean")) {
                  if (String.valueOf(values[idx][cols[k]]).equalsIgnoreCase("1")
                          || String.valueOf(values[idx][cols[k]]).equalsIgnoreCase("true")) {
                     data[idx][cols[k]] = true;
                  } else {
                     data[idx][cols[k]] = false;
                  }
               }
            }

            for (int h = 0; h < values[0].length; h++) {
               if (data[idx][h] == null) {
                  data[idx][h] = values[idx][h];
               }
            }
         }

         return data;
      } catch (Exception e) {

//          Log.Debug(this,e);
         return new Object[0][0];
      }
   }

   public static void makeUneditable(JTable table) {
      try {
         ((MPTableModel) table.getModel()).setCanEdits(new boolean[]{
            false, false, false, false, false, false, false, false, false, false, false, false, false
         });
      } catch (Exception e) {
         Log.Debug(TableFormat.class, "Can not change this table to uneditable.");
      }
   }

   public static void makeUneditableColumns(JTable table, Integer[] desiredCol) {
      boolean[] unedits;

      try {
         unedits = new boolean[desiredCol.length];

         for (int i = 0; i < desiredCol.length; i++) {
            if (desiredCol[i] != null) {
               unedits[i] = false;
            } else {
               unedits[i] = true;
            }
         }

         ((MPTableModel) table.getModel()).setCanEdits(unedits);
      } catch (Exception e) {
         Log.Debug(TableFormat.class, "Can not change this table to uneditable.");
      }
   }

   /**
    * Stops the tables' cell editor
    *
    * @param jTable1
    */
   public static void stopEditing(JTable jTable1) {
      TableCellEditor editor = jTable1.getCellEditor();

      if (editor != null) {
         try {
            editor.stopCellEditing();
         } catch (Exception e) {
         }
      }
   }

   /**
    * Stops the tables' cell editor
    *
     * @param table
     * @param row
     * @param col
    */
   public static void startEditing(JTable table, int row, int col) {
      // Enable the ability to select a single cell
      table.setColumnSelectionAllowed(true);
      table.setRowSelectionAllowed(true);
      boolean success = table.editCellAt(row, col);
      if (success) {
         // Select cell
         boolean toggle = false;
         boolean extend = false;
         table.changeSelection(row, col, toggle, extend);
      }
   }

   /**
    * Resizes a tables cols
    *
    * @param table
    * @param desiredColSizes
    * @param fixed Should the cols be non-resizable
    */
   public static void resizeCols(JTable table, Integer[] desiredColSizes, boolean fixed) {
      for (int i = 0; i < desiredColSizes.length; i++) {
         if (desiredColSizes[i] != null) {
            try {
               table.getColumnModel().getColumn(i).setMinWidth(desiredColSizes[i]);
               table.getColumnModel().getColumn(i).setPreferredWidth(desiredColSizes[i]);

               if (fixed) {
                  table.getColumnModel().getColumn(i).setMaxWidth(desiredColSizes[i]);
               } else {
                  table.getColumnModel().getColumn(i).setMaxWidth(1000);
               }
            } catch (Exception e) {
               Log.Debug(TableFormat.class, e.getMessage());
            }
         }
      }
   }

   /**
    * Resizes a tables cols
    *
    * @param table
    * @param columnIndex
    * @param desiredColSize
    * @param fixed Should the cols be non-resizable
    */
   public static void resizeCol(JTable table, int columnIndex, int desiredColSize, boolean fixed) {
      try {
         table.getColumnModel().getColumn(columnIndex).setMinWidth(desiredColSize);
         table.getColumnModel().getColumn(columnIndex).setPreferredWidth(desiredColSize);

         if (fixed) {
            table.getColumnModel().getColumn(columnIndex).setMaxWidth(desiredColSize);
         } else {
            table.getColumnModel().getColumn(columnIndex).setMaxWidth(1000);
         }
      } catch (Exception e) {
         Log.Debug(TableFormat.class, e.getMessage());
      }
   }

   public static DefaultTableModel getUneditableTable(String[][] data, String[] header) {
      return new javax.swing.table.DefaultTableModel(data, header) {
          private static final long serialVersionUID = 4873036854682614078L;

         boolean[] canEdits = new boolean[]{
            false, false, false, false, false, false, false, false, false, false, false, false, false, false, false,
            false, false, false, false, false, false, false, false, false, false, false, false, false, false, false
         };

         @Override
         public boolean isCellEditable(int rowIndex, int columnIndex) {
            return canEdits[columnIndex];
         }
      };
   }

   /**
    * Hide a column of a table
    *
    * @param table
    * @param columnToHide from model
    */
   public static void stripColumn(JTable table, int columnToHide) {
      try {
         table.getColumnModel().getColumn(columnToHide).setWidth(0);
         table.getColumnModel().getColumn(columnToHide).setPreferredWidth(0);
         table.getColumnModel().getColumn(columnToHide).setMinWidth(0);
         table.getColumnModel().getColumn(columnToHide).setMaxWidth(0);
         table.doLayout();
      } catch (Exception e) {
      }
   }

   /**
    * Hide the columns
    *
    * @param resulttable
    * @param i
    */
   public static void stripColumns(JTable resulttable, int[] i) {
      for (int j = 0; j < i.length; j++) {
         int k = i[j];

         try {
            stripColumn(resulttable, k);
         } catch (Exception e) {
         }
      }
   }

   /**
    * Hide columns with a specific column class
    *
    * @param table
    * @param aClass
    */
   public static void stripColumn(JTable table, Class<Entity> aClass) {
      for (int i = 0; i < table.getColumnCount(); i++) {
         if (table.getColumnClass(i).equals(aClass)) {
            stripColumn(table, table.convertColumnIndexToModel(i));
         }
      }
   }

   /**
    * Hide the first column of a table
    *
    * @param table
    */
   public static void stripFirstColumn(JTable table) {
      stripColumn(table, 0);
   }

   /**
    *
    * @param table
    * @param column
    * @param width
    */
   public static void format(JTable table, int column, int width) {
      try {
         table.getColumnModel().getColumn(column).setWidth(width);
         table.getColumnModel().getColumn(column).setPreferredWidth(width);
         table.getColumnModel().getColumn(column).setMinWidth(width);
         table.getColumnModel().getColumn(column).setMaxWidth(width);
      } catch (Exception e) {
      }
   }

   /**
    *
    * @param table
    * @param desiredColSizes
    * @param fixedCols
    */
   public static void resizeCols(JTable table, Integer[] desiredColSizes, Boolean[] fixedCols) {
      for (int i = 0; i < desiredColSizes.length; i++) {
         if (desiredColSizes[i] != null) {
            table.getColumnModel().getColumn(i).setPreferredWidth(desiredColSizes[i]);

            if ((fixedCols.length > 0) && (fixedCols[i] != null) && fixedCols[i]) {
               table.getColumnModel().getColumn(i).setMinWidth(desiredColSizes[i]);
               table.getColumnModel().getColumn(i).setMaxWidth(desiredColSizes[i]);
            } else {
               table.getColumnModel().getColumn(i).setMinWidth(0);
               table.getColumnModel().getColumn(i).setMaxWidth(1000);
            }
         }
      }
   }

   /**
    * Overwrites any other renderer for this column..
    *
    * @param table
    * @param column
    * @param color
    */
   public static void changeBackground(final JTable table, final int column, final Color color) {
      class ColorRenderer extends JLabel implements TableCellRenderer {

           private static final long serialVersionUID = -3009391069720793167L;

         public ColorRenderer() {
            this.setOpaque(true);
            this.setHorizontalAlignment(JLabel.CENTER);
         }

         @Override
         public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                 boolean hasFocus, int row, int col) {
            try {
               this.setText(value.toString());
            } catch (Exception e) {
            }

             if (isSelected) {
                 this.setBackground(Color.RED);
             } else {
                 this.setBackground(color);
             }

            return this;
         }
      }
      table.getColumnModel().getColumn(column).setCellRenderer(new ColorRenderer());
   }

   /**
    * Overwrites any other renderer for this column..
    *
    * @param table
    * @param col
    * @param row
    * @param color
    */
   public static void changeBackground(final JTable table, final int col, final int row, final Color color) {
      class ColorRenderer extends JLabel implements TableCellRenderer {

           private static final long serialVersionUID = -3009391069720793167L;

         public ColorRenderer() {
            this.setOpaque(true);
            this.setHorizontalAlignment(JLabel.CENTER);
         }

         @Override
         public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                 boolean hasFocus, int nrow, int ncol) {
            try {
               this.setText(value.toString());
            } catch (Exception e) {
            }

            if (row == nrow && col == ncol) {
               this.setBackground(color);
            }else{
               this.setBackground(Color.white);
            }

            return this;
         }
      }
      table.getColumnModel().getColumn(col).setCellRenderer(new ColorRenderer());
   }

   /**
    * Hides the header of a table by setting it's header height to 0
    *
    * @param table
    */
   public static void hideHeader(JTable table) {
      table.getTableHeader().setPreferredSize(new Dimension(table.getColumnModel().getTotalColumnWidth(), 15));
      table.getTableHeader().setFont(new Font(LocalSettings.getProperty(LocalSettings.DEFAULT_FONT), Font.PLAIN, 1));
      class ColumnListener implements TableColumnModelListener {

         private final JTable table;

         private ColumnListener(JTable target) {
            this.table = target;
         }

         @Override
         public void columnAdded(TableColumnModelEvent e) {
            table.getTableHeader().setPreferredSize(new Dimension(table.getColumnModel().getTotalColumnWidth(),
                    15));
         }

         @Override
         public void columnRemoved(TableColumnModelEvent e) {
            table.getTableHeader().setPreferredSize(new Dimension(table.getColumnModel().getTotalColumnWidth(),
                    15));
         }

         @Override
         public void columnMoved(TableColumnModelEvent e) {
            table.getTableHeader().setPreferredSize(new Dimension(table.getColumnModel().getTotalColumnWidth(),
                    15));
         }

         @Override
         public void columnMarginChanged(ChangeEvent e) {
            table.getTableHeader().setPreferredSize(new Dimension(table.getColumnModel().getTotalColumnWidth(),
                    15));
         }

         @Override
         public void columnSelectionChanged(ListSelectionEvent e) {
         }
      }
      table.getColumnModel().addColumnModelListener(new ColumnListener(table));
   }
}
//~ Formatted by Jindent --- http://www.jindent.com

