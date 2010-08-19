/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mpv5.utils.renderer;

import com.michaelbaranov.microba.calendar.DatePicker;
import java.awt.Component;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultCellEditor;
import javax.swing.JFormattedTextField;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.logging.Log;

/**
 *
 * @author andreas.weber
 */
public class TableCellEditorForAutoSave extends DefaultCellEditor {

    private final DatabaseObject type;
    private final HashMap<String, Integer> mapping;
    private final int triggerColumn;

    /**
     * Not ready yet
     * @param tf
     * @param type
     * @param mapping type.field, column.id
     * @param triggerColumn This column will trigger the save on cell.stopCellEditing()
     */
    public TableCellEditorForAutoSave(final JFormattedTextField tf, final DatabaseObject type, final HashMap<String, Integer> mapping, int triggerColumn) {
        super(tf);
        super.setClickCountToStart(1);

        tf.setFocusLostBehavior(JFormattedTextField.COMMIT);
        tf.setHorizontalAlignment(SwingConstants.LEFT);
        tf.setBorder(null);
        this.type = type;
        this.mapping = mapping;
        this.triggerColumn = triggerColumn;
    }

//    @Override
//    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
//        if (value == null) {
//            value = "";
//        }
//        if (value instanceof Date) {
//            return new DatePicker();
//        } else {
//            return super.getTableCellEditorComponent(table, value, isSelected, row, column);
//        }
//    }

}
