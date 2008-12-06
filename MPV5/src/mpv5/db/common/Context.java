/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package mpv5.db.common;

/**
 *
 * @author Administrator
 */
public class Context {

    /*
     * The DB Identity name - usually the table
     */
    private String dbIdentity = null;



    /**
     * @return the dbIdentity
     */
    public String getDbIdentity() {
        return dbIdentity;
    }

    /**
     * @param dbIdentity the dbIdentity to set
     */
    public void setDbIdentity(String dbIdentity) {
        this.dbIdentity = dbIdentity;
    }
}
