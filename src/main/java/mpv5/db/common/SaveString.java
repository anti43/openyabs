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
    private String raw = "";
    private String wrapper = "";

    /**
     * Constructs a ne save string
     * @param str The original String
     * @param wrapped If true, the String will be wrapped with a single quote on
     * insertion time; then it is escaped for SQL (' as '', see {@link SqlText}),
     * an unwrapped one (numbers, ...) must not contain quotes at all (' as `)
     */
    public SaveString(String str, boolean wrapped) {
        if (str == null) {
            this.raw = "";
        } else {
            this.raw = str;
        }
        if (wrapped) {
            this.wrapper = "'";
            this.string = SqlText.literal(raw);
        } else {
            this.string = SqlText.legacy(raw);
        }
    }

    /**
     * Gets the String value of this SaveString, as it goes into the SQL text
     * @return the save, escaped string
     */
    @Override
    public String toString() {
        return string;
    }

    /**
     * @return the original string (e.g. for prepared statements)
     */
    public String getRaw() {
        return raw;
    }

    public String getWrapper() {
        return wrapper;
    }
}
