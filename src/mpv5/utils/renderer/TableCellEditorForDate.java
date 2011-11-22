/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mpv5.utils.renderer;

import java.awt.event.MouseEvent;
import java.util.Date;
import java.util.EventObject;
import javax.swing.DefaultCellEditor;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentListener;
import mpv5.ui.dialogs.MiniCalendarDialog;
import mpv5.utils.models.MPTableModel;

/**
 *
 * @author Jan Hahnisch
 */
public class TableCellEditorForDate extends DefaultCellEditor {

    private static final long serialVersionUID = 1L;
    private final JTable tbl;
    private static JTextField tf = new JTextField();

    public TableCellEditorForDate(JTable tbl) {
        super(tf);
        this.tbl = tbl;
    }

    @Override
    public boolean isCellEditable(EventObject anEvent) {
        boolean isEditable = super.isCellEditable(anEvent);
        if (isEditable && anEvent instanceof MouseEvent) {
            delegate = new DatePickerDelegate(this, tf);
        } else {
            return false;
        }
        return isEditable;
    }

    public void setEditorTo(int i) {
        setClickCountToStart(1);
        tbl.getColumnModel().getColumn(i).setCellEditor(this);
    }

    class DatePickerDelegate extends EditorDelegate {

        private static final long serialVersionUID = 1L;
        TableCellEditorForDate cellEditor;
        MiniCalendarDialog datePicker;
        JTextField textField;

        DatePickerDelegate(final TableCellEditorForDate cellEditor, JTextField tf) {
            this.cellEditor = cellEditor;
            this.textField = tf;
            textField.getDocument().addDocumentListener(new DocumentListener() {

                public void insertUpdate(javax.swing.event.DocumentEvent e) {
                    fireStopped();
                }

                public void removeUpdate(javax.swing.event.DocumentEvent e) {
                    fireStopped();
                }

                public void changedUpdate(javax.swing.event.DocumentEvent e) {
                    fireStopped();
                }
            });
            datePicker = new MiniCalendarDialog(textField, true);
            datePicker.setLocation(tbl.getLocationOnScreen());
        }

        @Override
        public void setValue(Object value) {
            if (value == null) {
                datePicker.setDate(new Date());
            } else {
                datePicker.setDate((Date) value);
            }
            datePicker.setDate((Date) value);
            datePicker.setVisible(true);
        }

        @Override
        public Object getCellEditorValue() {
            return datePicker.getDate();
        }

        public void fireStopped() {
            this.cellEditor.stopCellEditing();
            fireEditingStopped();    // Make the renderer reappear
        }
    }
}
