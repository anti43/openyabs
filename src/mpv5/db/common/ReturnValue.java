/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mpv5.db.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 *
 *
 */
public class ReturnValue {

    private int id = 0;
    private int updateCount = 0;
    private Object[][] data;
    private String[] columnnames;
    private String message = null;
    private String[] fullColumnNames = null;

    public ReturnValue(int idOfIt, Object[][] data) {

        this.id = idOfIt;
        this.data = data;
    }

    public ReturnValue(int idOfIt, Object[][] data, String[] columnnames) {

        this.id = idOfIt;
        this.columnnames = columnnames;
        this.data = data;
    }

    public ReturnValue(int idOfIt, Object[][] data, String[] columnnames, String[] fullcolnames, String jobmessage) {

        this.id = idOfIt;
        this.columnnames = columnnames;
        this.data = data;
        this.message = jobmessage;
        this.fullColumnNames = fullcolnames;
    }

    public ReturnValue(Integer id) {
        this.id = id;
        this.columnnames = new String[0];
        this.data = new Object[0][0];
    }

    /**
     *
     */
    public ReturnValue() {
    }

    /**
     * If an inserting query has generated an ID, it will be available here
     *
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * First row of data retrieved by a select query
     *
     * @return the data
     */
    public Object[] getFirstRow() {
        if (hasData()) {
            return data[0];
        } else {
            return null;
        }
    }

    /**
     * First row of data retrieved by a select query
     *
     * @return the data
     */
    public Object[] getFirstColumn() {
        if (hasData()) {
            Object[] res = new Object[data.length];
            for (int i = 0; i < data.length; i++) {
                res[i] = data[i][0];
            }
            return res;
        } else {
            return null;
        }
    }

    /**
     * First field of data retrieved by a select query
     *
     * @return the data
     */
    public Object getFirstEntry() {
        if (hasData()) {
            return data[0][0];
        } else {
            return null;
        }
    }

    /**
     * All data retrieved by a select query
     *
     * @return the data
     */
    public Object[][] getData() {
        return data;
    }

    /**
     * All data retrieved by a select query
     *
     * @return the data
     */
    public List<Object[]> getDataAsList() {
        return Arrays.asList(getData());
    }

    /**
     * All data retrieved by a select query
     *
     * @return the data
     */
    public List<String> getDataAsStringList() {
        ArrayList<String> l = new ArrayList<String>(data.length);
        for (int i = 0; i < data.length; i++) {
            Object[] objects = data[i];
            StringBuilder a = new StringBuilder("");
            for (int j = 0; j < objects.length; j++) {
                a.append(objects[j]);
                if (j != objects.length - 1) {
                    a.append(", ");
                }
            }
            l.add(a.toString());
        }
        return l;
    }

    /**
     * All data retrieved by a select query
     *
     * @return the data
     */
    public Iterator<Object[]> getDataIterator() {
        return Arrays.asList(getData()).iterator();
    }

    /**
     * The column names used
     *
     * @return the columnnames
     */
    public String[] getColumnnames() {
        return columnnames;
    }

    /**
     * Add all the data of the given ReturnValue
     *
     * @param returnValue
     */
    public void set(ReturnValue returnValue) {
        this.setId(returnValue.id);
        this.setColumnnames(returnValue.columnnames);
        this.setData(returnValue.data);
        this.setMessage(returnValue.message);
    }

    /**
     * If the query had a message, this will be non-NULL
     *
     * @return the message
     */
    public String getMessage() {
        return message;
    }

    /**
     * @param message the message to set
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * @param id the id to set
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * @param data the data to set
     */
    public void setData(Object[][] data) {
        this.data = data;
    }

    /**
     * @param columnnames the columnnames to set
     */
    public void setColumnnames(String[] columnnames) {
        this.columnnames = columnnames;
    }

    /**
     * Checks if the ReturnValue has any data at all
     *
     * @return
     */
    public boolean hasData() {
        return data != null && data.length > 0 && data[0].length > 0;
    }

    @Override
    public String toString() {
        if (data != null) {
            return "Rowcount: " + data.length;
        }
        return "no rows";
    }

    public String[] getFullColumnNames() {
        return fullColumnNames;
    }

    public void setFullColumnNames(String[] fullColumnNames) {
        this.fullColumnNames = fullColumnNames;
    }

    public void setUpdateCount(int updateCount) {
        this.updateCount = updateCount;
    }

    /**
     * @return the updateCount
     */
    public int getUpdateCount() {
        return updateCount;
    }

    public int size() {
        return data.length;
    }
}