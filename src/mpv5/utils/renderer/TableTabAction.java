 /*
 * This file is part of YaBS.
 *
 *    YaBS is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    YaBS is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with YaBS.  If not, see <http://www.gnu.org/licenses/>.
 */
package mpv5.utils.renderer;

import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JTable;

/**
 * Tabs out of the table and overtab non editable columns
 */
public class TableTabAction extends AbstractAction {

    private JComponent componentToFocus;
    private Action originalTabAction;
    private boolean backward;

    public TableTabAction(JComponent c, Action a,boolean backward) {
        super("TableTabAction");
        componentToFocus = c;
        originalTabAction = a;
        this.backward = backward;
    }


    private boolean isFirstEditableColumn(JTable table,int column){
        if(column==0)return true;
        for(int i=0;i<column;i++){
            if(table.isCellEditable(0, i))return false;
        }
        return true;
    }

    private boolean isLastEditableColumn(JTable table,int column){
       if(column == table.getColumnCount() - 1)return true;
       for(int i=column+1;i<table.getColumnCount();i++){
           if(table.isCellEditable(0, i))return false;
       }
       return true;
    }

    public void actionPerformed(ActionEvent e) {
        JTable table = (JTable) e.getSource();
        int origRow = table.getSelectedRow();
        int origColumn = table.getSelectedColumn();
        originalTabAction.actionPerformed(e);
        int row = table.getSelectedRow();
        int originalRow = row;
        int column = table.getSelectedColumn();
        int originalColumn = column;
        if (backward) {
            if (origRow == 0 && isFirstEditableColumn(table,origColumn)) {
                componentToFocus.requestFocus();
            } else {
                while (!table.isCellEditable(row, column)) {
                    originalTabAction.actionPerformed(e);
                    row = table.getSelectedRow();
                    column = table.getSelectedColumn();
                    //  Back to where we started, get out.
                    if (row == originalRow && column == originalColumn) {
                        break;
                    }
                }
            }
        } else {
            if (origRow == table.getRowCount() - 1 && isLastEditableColumn(table,origColumn)) {
                table.changeSelection(0, 0, false, false);
                componentToFocus.requestFocus();
            }else{
                while (!table.isCellEditable(row, column)) {
                    originalTabAction.actionPerformed(e);
                    row = table.getSelectedRow();
                    column = table.getSelectedColumn();
                    //  Back to where we started, get out.
                    if (row == originalRow && column == originalColumn) {
                        break;
                    }
                }
            }
        }
    }
}
