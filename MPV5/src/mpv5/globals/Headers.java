/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mpv5.globals;

/**
 *
 * @author Andreas
 */
public class Headers {

    public static String[] SEARCH_DEFAULT = new String[]{"Internal ID", "Name", "Number"};
    public static String[] CONTACT_DEFAULT = new String[]{"Internal ID", "ID", "Name", "City"};
    public static String[] CONTACT_DETAILS = new String[]{
        "Internal ID", "ID", "Title",
        "Prename", "Name", "Street",
        "Zip", "City", "Mainphone",
        "Fax", "Mobilephone", "Workphone",
        "Mailaddress", "Company",
        "Website", "Notes", "Taxnumber"};
    public static String[] USER_DEFAULT = new String[]{"Internal ID", "ID", "User Name", "Mail", "Last logged in"};
    public static String[] USER_DETAILS = new String[]{"Internal ID", "User Name", "Fullname", "Mail", "Enabled", "Logged in"};

    public static String[] BABELFISH = new String[]{"Component", "Language Value", "New Value"};
    public static String[] ITEM_DEFAULT = new String[]{"Internal ID", "ID", "Date", "Value"};
    public static String[] CONTACT_FILES = new String[]{"id", "Filename", "Date", "Description"};
}
