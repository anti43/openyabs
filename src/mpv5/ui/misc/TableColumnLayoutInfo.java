
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
package mpv5.ui.misc;

//~--- non-JDK imports --------------------------------------------------------

import mpv5.utils.text.RandomText;

//~--- JDK imports ------------------------------------------------------------

import java.io.Serializable;

/**
 *
 * @author Florian Strienz
 */
public class TableColumnLayoutInfo implements Serializable, Comparable<TableColumnLayoutInfo> {
    private String columnName;
    private int    order;
    private int    width;

    public TableColumnLayoutInfo() {}

    public TableColumnLayoutInfo(String columnName, int order, int width) {
        this.columnName = columnName;
        this.order      = order;
        this.width      = width;
    }

    @Override
    public int compareTo(TableColumnLayoutInfo o) {
        return order - o.order;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }
}


//~ Formatted by Jindent --- http://www.jindent.com
