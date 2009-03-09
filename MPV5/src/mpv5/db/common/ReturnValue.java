/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mpv5.db.common;

/**
 *
 * @author Andreas
 */
public class ReturnValue {

    private int id  = 0;
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
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * @return the data
     */
    public Object[][] getData() {
        return data;
    }

    /**
     * @return the columnnames
     */
    public String[] getColumnnames() {
        return columnnames;
    }

    public void set(ReturnValue returnValue) {
         this.setId(returnValue.id);
        this.setColumnnames(returnValue.columnnames);
        this.setData(returnValue.data);
    }

    /**
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
}