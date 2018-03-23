package mpv5.globals;

import mpv5.i18n.LanguageManager;
import mpv5.logging.Log;

public enum Headers {
//empty columnnames not allowed
    SEARCH_DEFAULT(new String[]{"Internal ID", "A", "B"}),
    CONTACT_DEFAULT(new String[]{"Internal ID", "ID", "Name", "City"}),
    CONTACT_DETAILS(new String[]{"Internal ID", "ID", "Title", "Prename", "Name", "Street", "Zip", "City", "Mainphone", "Fax", "Mobilephone", "Workphone", "Company", "Mailaddress", "Website", "Notes", "Taxnumber"}),
    USER_DEFAULT(new String[]{"Internal ID", "ID", "User Name", "Mail", "Last logged in"}),
    USER_DETAILS(new String[]{"Internal ID", "User Name", "Fullname", "Mail", "Enabled", "Logged in"}),
    BABELFISH(new String[]{"Component", "Language Value", "New Value"}),
    ITEM_DEFAULT(new String[]{"Internal ID", "ID", "Date", "Net Value"}),
    ITEM_DETAIL(new String[]{"Internal ID", "ID", "Date", "Value"}),
    ITEM_COMPARE(new String[]{"Ordered", "Used", "Measure", "Description","%", "Status"}),
    ITEM_REFERENCE(new String[]{"Name", "Type"}),
    FILE_REFERENCES(new String[]{"Internal ID", "Filename", "Date", "Description", "Size", "Type"}),
    HISTORY(new String[]{"Internal ID", "Description", "User", "Group", "Date"}),
    IMPORT(new String[]{"Internal ID", "Import", "Type", "Name", "Data"}),
    JOURNAL(new String[]{"Internal ID", "Name", "Surname", "Street", "City", "Country", "Date", "Group", "Account", "Number", "Type", "Status", "Net", "Tax","Brut","Revenue", "Discount", "Description"}),
    TRASHBIN(new String[]{"Type", "Internal ID", "Description"}),
    PRODUCT_DEFAULT(new String[]{"Internal ID", "Name", "Number", "Description"}),
    SUBITEMS(new String[]{"Internal ID", "Nr", "Count", "Measure", "Text", "Netto Price", "Tax Rate", "Total Price", "Tax value", "Net 2", "Product ID", "A", "C", "Link", "Optional", "Discount", "Discount Value", "Discount Value with Tax"}),
    TEMPLATES(new String[]{"Name", "Type", "Group"}),
    MAILTEMPLATES(new String[]{"Name", "Description", "Group"}),
    SCHEDULE_LIST(new String[]{"Item ID", "Interval (Months)", "End date", "Added by"}),
    SCHEDULE_PANEL(new String[]{"IDS","Schedule ID", "Date","Type"}),
    ACTIVITY(new String[]{"Internal ID", "Nr", "Date", "Count", "Measure", "Text", "Netto Price", "Tax Rate", "Total Price", "Product", "Name", "object"}),
    ACTIVITY_DEFAULT(new String[]{"Internal ID", "Project", "Net Value"}),
    EXPENSE(new String[]{"Number", "Description", "Account", "Value", "Tax Rate", "Paid", "Date", "Pay-Date"}),
    REVENUE(new String[]{"Number", "ext.Ref", "Description", "Account", "Value", "Tax Rate", "Status", "Date", "Pay-Date", "Contact", "Ref. Order"}),
    LIBRARIEST(new String[]{"File", "Description", "License", "Author"}), 
    MASSPRINT(new String[]{"Object", "Name", "Item-Type", "Date Added"});

    private Headers(String[] header) {
        this.header = header;
    }
    private String[] header;

    public String[] getValue() {
      
           if (LanguageManager.isReady()) {
            for (int i = 0; i < header.length; i++) {
                try {
                    header[i] = LanguageManager.getBundle().getString(this.name() + "." + i);
                } catch (Exception e) {
                    Log.Debug(this, e.getMessage());
                }
            }
        }
        
        return header;
    }

    public String[] getKeys() {
      String[] keys = new String[header.length];
      
            for (int i = 0; i < header.length; i++) {
                try {
                    keys[i] = this.name() + "." + i;
                } catch (Exception e) {
                    Log.Debug(this, e.getMessage());
                }
            }
        
        return keys;
    }
    
    public void setValue(String[] header) {
        this.header = header;
    }
    
    public String getValue(int col) {
        return this.header[col];
    }    

    /**
     * Print out the values and their keys
     */
    public void printValues(){
         for (int k = 0; k < Headers.values().length; k++) {
            Headers data = Headers.values()[k];
            for (int i = 0; i < data.getValue().length; i++) {
                Log.Print(data.name() + "." + i + "=" + data.getValue()[i]);
            }
        }
    }
}
