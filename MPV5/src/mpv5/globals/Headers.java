package mpv5.globals;

public enum Headers {

    SEARCH_DEFAULT(new String[]{"Internal ID", "Name"}),
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
    ITEM_DEFAULT(new String[]{"Internal ID", "ID", "Date", "Net Value", "Tax value"}),
    ITEM_DETAIL(new String[]{"Internal ID", "ID", "Date", "Value"}),
    CONTACT_FILES(new String[]{"Internal ID", "Filename", "Date", "Description"}),
    HISTORY(new String[]{"Internal ID", "Description", "User", "Group", "Date"}),
    IMPORT(new String[]{"Internal ID", "Import", "Type", "Name", "Data"}),
    JOURNAL(new String[]{"Internal ID", "Date", "Group", "Number", "Type", "Status", "Value", "Paid"}),
    TRASHBIN(new String[]{"Type", "Internal ID", "Description"}),
    PRODUCT_DEFAULT(new String[]{"Internal ID", "Name", "Number","Description"}),
    SUBITEMS(new String[]{"Internal ID", "ID", "Count", "Measure", "Description", "Netto Price", "Tax Value", "Total Price"});

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
