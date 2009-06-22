/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mpv5.db.common;

/**
 *
 *  
 */
public class ReturnValue {

    private int id = 0;
    private Object[][] data;
    private String[] columnnames;
    private String message = null;

    public ReturnValue(int idOfIt, Object[][] data) {

        this.id = idOfIt;
        this.data = data;
    }

    public ReturnValue(int idOfIt, Object[][] data, String[] columnnames) {

        this.id = idOfIt;
        this.columnnames = columnnames;
        this.data = data;
    }

    public ReturnValue(int idOfIt, Object[][] data, String[] columnnames, String jobmessage) {

        this.id = idOfIt;
        this.columnnames = columnnames;
        this.data = data;
        this.message = jobmessage;
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
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * All data retrieved by a select query
     * @return the data
     */
    public Object[][] getData() {
        return data;
    }

    /**
     * The column names used
     * @return the columnnames
     */
    public String[] getColumnnames() {
        return columnnames;
    }

    /**
     * Add all the data of the given ReturnValue
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
     * @return
     */
    public boolean hasData() {
        return data != null && data.length > 0;
    }
}