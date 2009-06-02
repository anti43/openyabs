/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mpv5.globals;

/**
 *
 *  Andreas
 */
public enum Headers {

    SEARCH_DEFAULT(new String[]{"Internal ID", "Name", "Number"}),
    CONTACT_DEFAULT(new String[]{"Internal ID", "ID", "Name", "City"}),
    CONTACT_DETAILS(new String[]{
"Internal ID", "ID", "Title",
"Prename", "Name", "Street",
"Zip", "City", "Mainphone",
"Fax", "Mobilephone", "Workphone",
"Mailaddress", "Company",
"Website", "Notes", "Taxnumber"}),
    USER_DEFAULT(new String[]{"Internal ID", "ID", "User Name", "Mail", "Last logged in"}),
    USER_DETAILS(new String[]{"Internal ID", "User Name", "Fullname", "Mail", "Enabled", "Logged in"}),
    BABELFISH(new String[]{"Component", "Language Value", "New Value"}),
    ITEM_DEFAULT(new String[]{"Internal ID", "ID", "Date", "Value"}),
    CONTACT_FILES(new String[]{"id", "Filename", "Date", "Description"}),
    HISTORY(new String[]{"id", "Description", "User", "Group", "Date"}),
    IMPORT(new String[]{"Id", "Import", "Type", "Name", "Data"}),
    JOURNAL(new String[]{"id", "Description", "User", "Group", "Date"});

    private Headers(String[] header) {
        this.header = header;
    }
    private String[] header;

    public String[] getValue() {
        return header;
    }

    public void setValue(String[] header) {
        this.header = header;
    }
}
