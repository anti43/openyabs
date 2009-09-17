package mpv5.utils.renderer;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;

/**
 * Nobuo Tamemasa (http://www2.gol.com/users/tame/swing/examples/JTableExamples1.html)
 * @version 1.0 11/09/98
 */
public class ButtonEditor extends DefaultCellEditor {

    protected JButton button;
    private String label;
    private boolean isPushed;

    public ButtonEditor(JButton button) {
        super(new JCheckBox());
        setClickCountToStart(1);
        this.button = button;
        button.setOpaque(true);
        button.setMargin(new Insets(0, 0, 0, 0));
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                fireEditingStopped();
            }
        });
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value,
            boolean isSelected, int row, int column) {
        if (isSelected) {
            button.setForeground(table.getSelectionForeground());
            button.setBackground(table.getSelectionBackground());
        } else {
            button.setForeground(table.getForeground());
            button.setBackground(table.getBackground());
        }
        label = (value == null) ? "" : value.toString();
        button.setText(label);
        isPushed = true;
        
        return button;
    }

    @Override
    public Object getCellEditorValue() {
        if (isPushed) {
        }
        isPushed = false;
        return new String(label);
    }

    @Override
    public boolean stopCellEditing() {
        isPushed = false;
        return super.stopCellEditing();
    }

    @Override
    protected void fireEditingStopped() {
        super.fireEditingStopped();
    }
}
