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

    public ReturnValue(int idOfIt, Object[][] data) {

        this.id = idOfIt;
        this.data = data;
    }

    public ReturnValue(int idOfIt, Object[][] data, String[] columnnames) {

        this.id = idOfIt;
        this.columnnames = columnnames;
        this.data = data;
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
}