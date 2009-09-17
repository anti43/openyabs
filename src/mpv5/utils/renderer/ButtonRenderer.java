
package mpv5.utils.renderer;

import java.awt.*;
import javax.swing.*;
import javax.swing.table.*;

/**
 * Nobuo Tamemasa (http://www2.gol.com/users/tame/swing/examples/JTableExamples1.html)
 * @version 1.0 11/09/98
 */
public class ButtonRenderer extends JButton implements TableCellRenderer {

  public ButtonRenderer() {
    setOpaque(true);
    setMargin(new Insets(0,0,0,0));
  }

  public Component getTableCellRendererComponent(JTable table, Object value,
                   boolean isSelected, boolean hasFocus, int row, int column) {
    if (isSelected) {
      setForeground(table.getSelectionForeground());
      setBackground(table.getSelectionBackground());
    } else{
      setForeground(table.getForeground());
      setBackground(UIManager.getColor("Button.background"));
    }
    setText( (value ==null) ? "" : value.toString() );
    return this;
  }
}