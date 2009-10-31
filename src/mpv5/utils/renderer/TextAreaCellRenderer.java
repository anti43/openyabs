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

import java.awt.Component;
import java.awt.Dimension;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

public class TextAreaCellRenderer extends JTextArea implements TableCellRenderer{

    private final DefaultTableCellRenderer adaptee = new DefaultTableCellRenderer();
    protected JTable table;
    private final static int MAXLINES = 3;
    private final static int MARGIN = 1;
    
    public TextAreaCellRenderer(JTable table){
        super();
        this.table = table;
        setLineWrap(true);
        setWrapStyleWord(true);
        setRows(3);
        setOpaque(true);
        setEditable(false);        
    }

    /**
     * Set this renderer to the given column + editor
     * @param column
     */
    public void setRendererTo(int column) {
        TableColumn col = table.getColumnModel().getColumn(column);
        col.setCellRenderer(this);
    }


    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        adaptee.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        setForeground(adaptee.getForeground());
        setBackground(adaptee.getBackground());
        setBorder(adaptee.getBorder());
        setFont(adaptee.getFont());
        setText(adaptee.getText());

        //find the number of showed lines
        int charwidth = getColumnWidth();
        double cellwidth = table.getCellRect(row, column, false).getWidth();
        int charsproline = (int)(cellwidth/charwidth);
        int charnum = getText().length();
        int lines = charnum/charsproline;
        int textlines = getLineCount();
        if(lines<textlines)lines = textlines;
        if(lines>MAXLINES)lines = MAXLINES;
        if(lines<=0)lines=1;
        //setMaximumSize(new Dimension(30,30));
        int newrowheight  = getRowHeight()*lines+2*MARGIN;
        if(table.getRowHeight(row)!=newrowheight)table.setRowHeight(row,newrowheight);

        return this;
    }


}
