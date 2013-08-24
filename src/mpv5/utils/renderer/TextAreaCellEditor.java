
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
package mpv5.utils.renderer;

//~--- non-JDK imports --------------------------------------------------------

import mpv5.logging.Log;


//~--- JDK imports ------------------------------------------------------------

import java.applet.Applet;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.math.BigDecimal;

import javax.swing.DefaultCellEditor;
import javax.swing.JDialog;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.table.TableColumn;
import javax.swing.text.JTextComponent;
import mpv5.db.objects.Product;
import mpv5.db.objects.SubItem;
import mpv5.ui.dialogs.subcomponents.ActivityTextAreaDialog;
import mpv5.ui.dialogs.subcomponents.ItemTextAreaDialog;
import mpv5.ui.dialogs.subcomponents.ProductSelectDialog3;
import mpv5.utils.models.MPTableModel;
import mpv5.utils.numberformat.FormatNumber;

public class TextAreaCellEditor extends DefaultCellEditor implements ActionListener {
    protected final static String CANCEL = "CANCEL";
    protected final static String OK     = "OK";
    private static final long serialVersionUID = 1L;
    protected JDialog             dialog;
    protected JTextComponent      dialogsTextComponent;
    private final JTable          table;
    protected JTextArea           textArea;

    public TextAreaCellEditor(JTable table) {
        super(new JTextField());
        this.table = table;
        textArea   = new JTextArea() {
            private static final long serialVersionUID = 1L;
            @Override
            protected boolean processKeyBinding(KeyStroke ks, KeyEvent e, int condition, boolean pressed) {
                boolean retValue = super.processKeyBinding(ks, e, condition, pressed);

                if (dialogsTextComponent != null) {
                    simulateKey(e, dialogsTextComponent);
                }

                return retValue;
            }
        };
        textArea.setWrapStyleWord(true);
        textArea.setLineWrap(true);
        editorComponent = textArea;
        delegate        = new DefaultCellEditor.EditorDelegate() {
            private static final long serialVersionUID = 1L;
            @Override
            public void setValue(Object value) {

                if (value != null && dialog instanceof ProductSelectDialog3 && value instanceof  Product){
                    value = ((Product)value).__getCnumber();
                }
                textArea.setText((value != null)
                                 ? value.toString()
                                 : "");
            }
            @Override
            public Object getCellEditorValue() {
                return textArea.getText();
            }           
        };
    }

    public void simulateKey(KeyEvent e, Component c) {
        try {
            KeyboardFocusManager.getCurrentKeyboardFocusManager().redispatchEvent(c, e);
        } catch (Exception ex) {
            Log.Debug(this, ex);
        }
    }

    /**
     * Set this renderer to the given column
     * @param column
     * @param editable
     */
    public void setEditorTo(int column) {
        TableColumn col = table.getColumnModel().getColumn(column);

        col.setCellEditor(this);
    }

    public void setDialog(JDialog dialog, JTextComponent dialogsTextComponent) {
        this.dialog               = dialog;
        this.dialogsTextComponent = dialogsTextComponent;
    }

    @Override
    public Component getTableCellEditorComponent(final JTable table, Object value, boolean isSelected, final int row, final int column) {
        if (value != null && dialog instanceof ProductSelectDialog3 && value instanceof  Product){
            MPTableModel m = (MPTableModel) table.getModel();
            SubItem it = m.getRowAt(row, SubItem.getDefaultItem());
            ((ProductSelectDialog3) dialog).setTempSubItem(it);
            value = ((Product)value).__getCnumber();
        }
        if (value == null) {
            value = "";
        }

        Component component = super.getTableCellEditorComponent(table, value, isSelected, row, column);

        dialogsTextComponent.setText(value.toString());    
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {

                // Point p =textArea.getLocationOnScreen();
                // p.move((int)p.getX(),mpv5.YabsViewProxy.instance().identifierFrame.getY() + mpv5.YabsViewProxy.instance().identifierFrame.getHeight() - 310);
                // dialog.setLocation(p);
                setDialogLocation(textArea, dialog);
                if (dialog instanceof ActivityTextAreaDialog && table.getValueAt(row, 9) != null && !table.getValueAt(row, 9).equals(0)) {
                    Log.Debug(this, "ActivityDialog");
                    ((ActivityTextAreaDialog) dialog).setSelectedProduct(
                            Integer.valueOf(table.getValueAt(row, 9).toString()), table.getValueAt(row, column).toString());
                }
                if (dialog instanceof ItemTextAreaDialog && !table.getValueAt(row, 2).equals(0)) {
                    Log.Debug(this, "ItemTextDialog");
                    ((ItemTextAreaDialog) dialog).setQuantity(
                            Double.parseDouble(table.getValueAt(row, 2).toString()));
                }
                dialog.setVisible(true);
            }
        });

        return component;
    }

    @Override
    public boolean stopCellEditing() {
        dialog.setVisible(false);

        return super.stopCellEditing();
    }

    @Override
    public void cancelCellEditing() {
        dialog.setVisible(false);
        super.cancelCellEditing();
    }

    public void actionPerformed(ActionEvent e) {
        if (OK.equals(e.getActionCommand())) {
            dialog.setVisible(false);

            if (dialogsTextComponent != null) {
                textArea.setText(dialogsTextComponent.getText());
            } else {
                Log.Debug(this, "dialogsTextArea is null error");
            }
        } else if (CANCEL.equals(e.getActionCommand())) {
            dialog.setVisible(false);
        }

        fireEditingStopped();    // Make the renderer reappear
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                table.requestFocusInWindow();
            }
        });
    }

    protected void setDialogLocation(Component c, JDialog dialog) {
        Container root = null;

        if ((c instanceof Window) || (c instanceof Applet)) {
            root = (Container) c;
        } else {
            Container parent;

            for (parent = c.getParent(); parent != null; parent = parent.getParent()) {
                if ((parent instanceof Window) || (parent instanceof Applet)) {
                    root = parent;

                    break;
                }
            }
        }

        Dimension invokerSize           = c.getSize();
        Point invokerScreenLocation = new Point(0, 0);
        try {
            invokerScreenLocation = c.getLocationOnScreen();
        } catch (Exception e) {
            Log.Debug(e);
        }
        Rectangle windowBounds          = dialog.getBounds();
        int       dx                    = invokerScreenLocation.x;
        int       dy                    = invokerScreenLocation.y;
        Rectangle ss                    = root.getGraphicsConfiguration().getBounds();

        ss.grow(-32, -32);

        if (dy + windowBounds.height > ss.y + ss.height) {
            dy = ss.y + ss.height - windowBounds.height;

            if (invokerScreenLocation.x - ss.x + invokerSize.width / 2 < ss.width / 2) {
                dx = invokerScreenLocation.x + invokerSize.width;
            } else {
                dx = invokerScreenLocation.x - windowBounds.width;
            }
        }

        if (dx + windowBounds.width > ss.x + ss.width) {
            dx = ss.x + ss.width - windowBounds.width;
        }

        if (dx < ss.x) {
            dx = ss.x;
        }

        if (dy < ss.y) {
            dy = ss.y;
        }

        dialog.setLocation(dx, dy);
    }
}


//~ Formatted by Jindent --- http://www.jindent.com
