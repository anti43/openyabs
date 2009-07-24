/*
 *  This file is part of MP.
 *
 *      MP is free software: you can redistribute it and/or modify
 *      it under the terms of the GNU General Public License as published by
 *      the Free Software Foundation, either version 3 of the License, or
 *      (at your option) any later version.
 *
 *      MP is distributed in the hope that it will be useful,
 *      but WITHOUT ANY WARRANTY; without even the implied warranty of
 *      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *      GNU General Public License for more details.
 *
 *      You should have received a copy of the GNU General Public License
 *      along with MP.  If not, see <http://www.gnu.org/licenses/>.
 */
package enoa.handler;

import ag.ion.bion.officelayer.text.ITextDocument;
import ag.ion.bion.officelayer.text.ITextTable;
import ag.ion.bion.officelayer.text.ITextTableService;
import ag.ion.bion.officelayer.text.TextException;

/**
 *
 */
public class TableHandler {

    private final ITextDocument doc;
    private final ITextTableService tableService;
    private final ITextTable table;

    public TableHandler(ITextDocument doc, Object[][] values) throws TextException {
        this.doc = doc;
        this.tableService = doc.getTextTableService();
        table = addTable(values);

    }

    public TableHandler(ITextDocument doc, String tableName) throws TextException {
        this.doc = doc;
        this.tableService = doc.getTextTableService();
        table = tableService.getTextTable(tableName);
    }

    public int getRowCount() {
        return getTable().getRowCount();
    }

    public int getColumnCount() {
        return getTable().getColumnCount();
    }

    public void addRows(int count) throws TextException {
        getTable().addRow(count);
    }

    public void setHeaderLines(int lines) throws TextException {
        getTable().setHeaderRows(3);
    }

    public synchronized void setValueAt(Object value, int column, int row) throws TextException {

        if (getRowCount() <= row) {
            addRows(1);
        }

        if (value != null && value instanceof Double) {
            getTable().getCell(column, row).setValue(Double.valueOf(value.toString()));
        } else {
            getTable().getCell(column, row).getTextService().getText().setText(value.toString());
        }
    }

    public Object getvalueAt(int column, int row) throws TextException {
        return getTable().getCell(column, row).getValue();
    }

    public ITextTable addTable(Object[][] values) throws TextException {
        ITextTable textTable = null;
        try {
            textTable = getTableService().constructTextTable(values.length, values[0].length);
        } catch (IndexOutOfBoundsException ex) {
            textTable = getTableService().constructTextTable(values.length, 0);
        }

        getDoc().getTextService().getTextContentService().insertTextContent(textTable);

        for (int i = 0; i < values.length; i++) {
            Object[] objects = values[i];
            for (int j = 0; j < objects.length; j++) {
                Object object = objects[j];
                setValueAt(object, j, i);
            }
        }
        return textTable;
    }

    /**
     * @return the doc
     */
    public ITextDocument getDoc() {
        return doc;
    }

    /**
     * @return the tableService
     */
    public ITextTableService getTableService() {
        return tableService;
    }

    /**
     * @return the table
     */
    public ITextTable getTable() {
        return table;
    }
}
