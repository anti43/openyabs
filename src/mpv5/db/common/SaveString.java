/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mpv5.db.common;

/**
 *
 *  
 */
/**
 * Represents a <code>SaveString</code> value which <br><br/>
 * <li>Does not contain single quotes and<br/>
 * is considered to be safe to be inserted into a database<li/>
 */
public class SaveString {

    private static final long serialVersionUID = 1L;
    private String string = "";
    private String wrapper = "";

    /**
     * Constructs a ne save string
     * @param str The original String
     * @param wrapped If true, the String will be wrapped with a single quote on insertion time
     */
    public SaveString(String str, boolean wrapped) {
        if (str == null) {
            this.string = "";
        } else {
            this.string = str.replace("'", "`");
        }
        if (wrapped) {
            this.wrapper = "'";
        }
    }

    /**
     * Gets the String value of this SaveString
     * @return the save, decoded string
     */
    @Override
    public String toString() {
        return string;
    }

    public String getWrapper() {
        return wrapper;
    }
}
