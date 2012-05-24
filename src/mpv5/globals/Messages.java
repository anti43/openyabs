/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mpv5.globals;

import java.util.*;
import mpv5.logging.Log;
import mpv5.i18n.LanguageManager;

/**
 * This enum holds all messages for the mp5 main application Start with -license
 * for details.\nStart with -help for command line options. : YaBS (
 *
 * @version) (C) 2006-2009 Openyabs.org
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.\n\nThis program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.\n\nYou should have received a copy of the
 * GNU General Public License along with this program; if not, write to the Free
 * Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 * USA\n\nStart with -license for details.\nStart with -help for command line
 * options.
 */
public enum Messages {

    START_MESSAGE(
    "\n"
    + "YaBS " + Constants.VERSION + " (C) 2006-2012 Openyabs.org\n\n"
    + "******************************************************************************\n"
    + "* This program is free software; you can redistribute it and/or modify it \n"
    + "* under the terms of the GNU General Public License as published by the \n"
    + "* Free Software Foundation; either version 2 of the License, or (at your option) \n"
    + "* any later version.\n*\n"
    + "* This program is distributed in the hope that it will be useful, \n"
    + "* but WITHOUT ANY WARRANTY; without even the implied warranty of \n"
    + "* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General \n"
    + "* Public License for more details.\n*\n"
    + "* You should have received a copy of the GNU General Public License along with \n"
    + "* this program; if not, write to the Free Software Foundation, Inc., \n"
    + "* 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA\n*\n"
    + "* Start with -license for details.\n"
    + "* Start with -help for command line options.\n"
    + "******************************************************************************"),
    CONTACT("Contact: "),
    /**
     * Start MAIN
     */
    INIT("Initialising.."),
    LOCAL_SETTINGS("Reading local settings.. if this runs forever, run Yabs as Administrator once"),
    LAUNCH("Launching application.."),
    FIRST_INSTANCE("Checking for running instances.."),
    DB_CHECK("Checking database connectivity.."),
    INIT_GUI("Initialising Graphical User Interface.."),
    INIT_LOGIN("Logging in user.."),
    CACHE("Caching data.."),
    INIT_PLUGINS("Initialising Plugins.."),
    NEW_CONTACT("New Contact"),
    NEW_CUSTOMER("New Customer"),
    NEW_SUPPLIER("New Supplier"),
    NEW_MANUFACTURER("New Manufacturer"),
    NEW_OFFER("New Offer"),
    NEW_ORDER("New Order"),
    NEW_BILL("New Invoice"),
    NEW_VALUE("New Value"),
    NEW_PRODUCT("New Product"),
    NEW_SERVICE("New Service"),
    NEW_COMPANY("New Company"),
    NEW_LIST("New Product List"),
    ACCESS_DENIED("Access denied."),
    /**
     * Actions
     */
    ACTION_CREATE("Create"),
    ACTION_PREVIEW("Preview"),
    ACTION_EDIT("Edit"),
    ACTION_EXPORT("Export"),
    ACTION_VIEW("View"),
    ACTION_PASTE("Paste"),
    ACTION_COPY("Copy"),
    ACTION_DELETE("Delete"),
    ACTION_OPEN("Open"),
    ACTION_REMOVE("Remove"),
    ACTION_TEST("Test"),
    SECURITYMANAGER_DENIED("The Security Manager has DENIED your request.\nAction "),
    SECURITYMANAGER_ALLOWED("The Security Manager has ALLOWED your request.\nAction "),
    THE_RESULT("The resulting value will look like:\n\n"),
    CONTEXT(", in context: "),
    INSERTED(": created"),
    UPDATED(": updated"),
    SAVED(": saved"),
    IMPORTED(": imported"),
    DELETED(": removed from database"),
    TRASHED(": moved to trashbin"),
    CNAME_CANNOT_BE_NULL("You must set a NAME for this dataset."),
    FILE_SAVED("File saved: "),
    ERROR_OCCURED("An error occurred\n"),
    SEE_LOG("See Logs for details"),
    FILE_NOT_SAVED("File NOT saved: "),
    WARNING("Warning"),
    NOTICE("Notice"),
    FILE_EXISTS("The file already exists, overwrite?"),
    ARE_YOU_SURE("Are you sure?"),
    FINISH("Finish "),
    SAVE_AS("Save as.. "),
    CONNECTION_VERIFIED("Database Connection established."),
    CONNECTION_FAILED("The Database Connection failed."),
    CONNECTION_PROBE("Checking Database Connection: "),
    ERROR_SAVING_LOCALSETTINGS("Could not save local settings"),
    DB_DRIVER_INVALID("The driver for the specified database type could not be loaded\n"),
    CREATING_DATABASE("Creating database structure.."),
    CREATING_DATABASE_FAILED("Creating database structure failed, see logs for details."),
    RESTORING_DATABASE("Restoring database structure.."),
    RESTORING_DATABASE_FAILED("Restoring database structure failed, see logs for details."),
    CONTACTS_LIST("List of contacts"),
    CONTROL_PANEL("Control Panel"),
    REALLY_CLOSE("Do you really want to close the entire application?\nUnsaved data will be lost!"),
    USER_NOT_FOUND("User not found: "),
    NO_DATA_FOUND("Sorry, but there was no matching data in the database."),
    REALLY_WIPE("Do you really want to wipe all data?"),
    WIPED_LOCALSETTINGS("Local settings have been marked for deletion. Please close Yabs now."),
    NO_DB_CONNECTION("Welcome to Yabs!\nYou need to set up the database connection before you can use the application, start wizard now?"),
    REALLY_CHANGE("Do you really want to alter the selected dataset? This may affect all users!"),
    REALLY_CHANGE_TYPE("Do you really want to change the type of selected dataset? This is irreversible!"),
    REALLY_CHANGE_DONE_ITEM("This item has been previously marked as 'Cancelled' or 'Paid'; Do you really want to alter it anyway?"),
    DONE("Task finished successfully."),
    PROCESSING("Processing: "),
    LOCKED(" is now locked"),
    VALUE_NOT_VALID("You have entered an invalid value: "),
    VALUE_ALREADY_EXISTS("The value already exists in the database and must not be duplicated: "),
    ADMIN_ACCESS("You need administrative rights to perform this action."),
    NOT_POSSIBLE("Requested action is not possible: "),
    DEFAULT_USER("Default user can't do this,\nlog in as real user to perform this action."),
    REALLY_DELETE("Do you really want to delete this dataset?"),
    NOT_SAVED_YET("The data has not been saved yet."),
    ADDED_TO_FAVOURITES("The item has been added to your favourites list: "),
    NO_PRINTER_FOUND("No printer found on this system."),
    UNGROUPED("Ungrouped"),
    ADMIN_USER("User Administrator can not be deleted."),
    GROUPNAMES("All Groups"),
    PRODUCTGROUPNAMES("All Products"),
    ACCOUNTNAMES("All Accounts"),
    IN_USE("The dataset is in use, locked or has subitems assigned to it."),
    ENTER_A_DESCRIPTION("Please enter a description:"),
    FILE_OPEN_FAILED("Unable to open the file: \n"),
    HISTORY_PANEL("History"),
    NEW_TAB("New Tab"),
    CALENDAR("Calendar"),
    USER_DISABLED("The user is disabled. Contact your Administrator."),
    HISTORY_OF("History of :"),
    MISC("Other"),
    COST("Cost"),
    TAXES("Taxes"),
    EXPENSE("Payables"),
    REVENUE("Revenues"),
    INCOME("Income"),
    RESERVE("Not used"),
    LOCK_FAILED("Lock failed!"),
    LOCKED_BY("This dataset is currently locked by another user\nand can not be edited.."),
    LIABILITY("Liability"),
    STATUS_QUEUED("Queued"),
    STATUS_IN_PROGRESS("In progress"),
    STATUS_PAUSED("Paused"),
    STATUS_FINISHED("Finished"),
    STATUS_CANCELLED("Cancelled"),
    TYPE_BILL("Invoice"),
    TYPE_OFFER("Offer"),
    TYPE_CONTACT("Contact"),
    TYPE_CUSTOMER("Customer"),
    TYPE_MANUFACTURER("Manufacturer"),
    TYPE_SUPPLIER("Supplier"),
    TYPE_PRODUCT("Product"),
    TYPE_SERVICE("Service"),
    TYPE_ORDER("Order"),
    TYPE_DELIVERY("Delivery Note"),
    TYPE_REMINDER("Reminder"),
    TYPE_REVENUE("Revenue"),
    TYPE_EXPENSE("Payables"),
    TYPE_JOURNAL("Journal"),
    TYPE_CONFIRMATION("Order Confirmation"), 
    STATUS_PAID("Paid"),
    STATUS_UNPAID("Not Paid"),
    TRASHBIN("Trashbin"),
    CONVERSATION("Conversation"),
    MASSPRINT("Massprint"),
    DELETE("Delete"),
    RESTORE("Restore"),
    RESTORED(" restored"),
    RELOAD("Reload"),
    LOADED(": loaded"),
    UNLOAD("Unload"),
    FILES("Files"),
    RESTART_REQUIRED("You need to restart the application before changes take place"),
    CACHED_OBJECTS("Objects cached"),
    ADVANCED_SETTINGS("Advanced Settings"),
    ADVANCED_SETTINGS_SAVE("Changing this settings may lead to a non-functional application. Really save?"),
    LOADING_OOO("Loading OpenOffice.."),
    REMINDERS("Times called: "),
    REMINDER("Reminder"),
    OO_DONE_LOADING("OpenOffice loaded"),
    OOCONNERROR("Could not open a connection to OpenOffice."),
    OO_NO_TEMPLATE("No template"),
    SET_AS_DEFAULT("Set as default"),
    NO_ACCOUNTS("No accounts or companies are configured for you."),
    CONTACTS_TRANSMITTED("contacts have been transmitted to"),
    ORDERS_RECEIVED("orders have been received from"),
    LOAD_NOW("Load now?"),
    NO_TEMPLATE_DEFINDED("There is no template assigned to your account matching this export type."),
    NO_TEMPLATE_LOADED("The template is not loaded yet. Make sure there is a matching template assigned to your user"),
    SCHEDULE_NEXT("Next date for this item: "),
    SCHEDULE("Schedule"),
    SCHEDULE_GENERATED("Autogenerated Item. Original:"),
    SCHEDULE_NEW_ITEMS("A new item will be generated by the scheduler. Create and open now?"),
    SCHEDULE_NEW_WARNINGS("unpaid bills have reached the warning treshold. Open now?"),
    SCHEDULE_NEW_ALERTS("unpaid bills have reached the alert treshold. Open now?"),
    SCHEDULE_ITEM_REMOVED("Scheduled item reached end-date:"),
    WELCOME("Welcome"),
    SELECT_A_TEMPLATE("Select a template:"),
    SELECT_A_CONTACT("You must select a contact."),
    UNPAID_BILLS("Open invoices"),
    UNPAID_BILLS_OVERDUE("Overdues"),
    NO_MAIL_CONFIG("SMTP host configuration not set."),
    MAIL_SENT("Mail sent to:"),
    CHANGE_STATUS("Change status"),
    ALL("All"),
    OVERVIEW("Overview"),
    ASSIGN_TEMPLATE("Please assign at least one user to the imported template by selecting one or more users from the list, and the press 'Save'. The template will not be usable otherwise."),
    NO_MAIL_DEFINED("Either you or the contact you want to send the mail to (or both) have no valid mail address defined."),
    AUTO_GENERATED_VALUE("Auto generated value"),
    BOOK_NOW("Save & book now?"),
    FIRST_START("This seems to be the first start"),
    ALL_PRODUCTS("All Products"),
    ACTION_ADDLIST("Add to listboard"),
    GOOSE1("Order Nr."),
    GOOSE2("created on"),
    YABS("Yet another business software"),
    QUERY_WINDOW("Query Window"),
    LOGIN("The template system will not work until you log in with a valid user."),
    CONTACT_TYPE_MALE("Mr."),
    CONTACT_INTRO_MALE("Dear"),
    CONTACT_TYPE_FEMALE("Mrs."),
    CONTACT_INTRO_FEMALE("Dear"),
    INVENTORY_STOCK_TRESHOLD("The stock count of the following product has reached the alarm threshold: "),
    PRINT_NOT_POSSIBLE("Could not find a print service for the give file. Try 'print through default application' setting instead:"),
    UPDATE_AVAILABLE("There is an update available for Yabs, check openyabs.org"),
    SEARCHABLE("This is a search-enabled field - type a phrase and hit 'Enter'"),
    TYPE_PRODUCT_ORDER("Product Order"),
    BOM_CREATED("A new product has been created out of this productlist and is now ready to be used: "),
    DTAUS_REASON("Invoice number: "),
    DTAUS_CREATED("The following entries have been created \n(only unpaid invoices with a value >0.15 have been used):"),
    SELECT_AN_INVOICE("You need to select at least one invoice to export."),
    DTAUS_NOT_SET("Your user account is missing mandatory banking information for DTA file creation."),
    PROPERTY("Property"),
    VALUE("Value"),
    DTA_CREATE("Create DTAUS"),
    REALLY_DELETE2("Do you really want to delete all selected datasets?"),
    REALLY_CHANGE2("Do you really want to change all selected datasets?"),
    PRINTED("Printed item: "),
    ODT("Created .odt file from item: "),
    PDF("Created .pdf file from item: "),
    IMPORT_LANGUAGES("Checking for additional languages"),
    IMPORT_PLUGINS("Checking for additional plugins"),
    IMPORT_TEMPLATES("Checking for additional templates"),
    CHECK_LIBS("Checking for needed libs"),
    MISSING_FILE("Missing File: "),
    MISSING_LIBS("Missing needed lib(s)! Programm may crash!"),
    LANG_EMPTY("Language is Empty! Please select a Language!"),
    LANG_USED("Language is in use and can't be deleted!"),
    TAXES_NOT_EQUAL("You can not use different tax values within the same invoice."),
    ADDRESS_TYPE_BOTH("Invoicing and delivery address"),
    ADDRESS_TYPE_INVOICE("Invoicing address"),
    ADDRESS_TYPE_DELIVERY("Delivery address"),
    TASKS_CONTACTS("Your contacts.."),
    TASKS_OFFERS("Your offers.."),
    TASKS_INVOICES("Your invoices.."),
    TASKS_SETTINGS("Configure Yabs.."),
    PDF_CREATE("Create PDF"),
    ODT_CREATE("Create ODT"),
    IN_GROUP("In group:"),
    CHECK_OVERDUES("Show unpaid invoices"),
    NA("N/A"),
    ACTION_ADD("Add"),
    NO_MAIL_TEMPLATE_DEFINED(
    "You do not have any mail template definded yet. "
    + "Create mail templates at Tools->Control Panel->User Properties->Mail Templates. "
    + "The mail will be send without any text body now."),
    ADD_CONTACT("Add contact.."),
    SEARCH_CONTACT("Search contact.."),
    LANGUAGE_FILE("Your language file is not up to date! Visit openyabs.org for a new language file or edit the existing one via Tools->Control Panel->Babelfish"),
    IMPORT_LANGUAGES_DONE("A new language has been imported, do you want to assign the language to a user?"),
    IMPORT_TEMPLATES_DONE("A new template has been imported; before you can use it, you need to go to Tools->Control Panel->Templates to assign the new template to a user!"),
    IMPORT_PLUGINS_DONE("A new plugin has been imported; before you can use it, you need to go to Tools->Control Panel->Plugins to assign the new plugin to a user!"),
    ENTER_VALUE("You need to enter a value for: "),
    NAME("Name"),
    UNTRASHED(": moved out of trashbin"),
    PLUGIN_INCOMPATIBLE("Incompatible plugin found. Shall I try to load it anyway?"),
    TYPE_CONTRACT("Contract"),
    HOMESCREEN("Homescreen"),
    DAY_EVENTS("This Day has Events!"),
    TYPE_CONVERSATION("Conversation"),
    Conversation_MainAddress("Main-Address"),
    CREATE_REMINDER("This Item is overdue! Should a reminder be created?"),
    TYPE_MASSPRINT("Serial-Mail (Massprint)"),
    CNAME_EXISTS("Please adjust the Subject!\nActual Subject exists!"),
    CONTACT_COMPLETE("Please complete the Contact-Data!"),
    DATAOWNER_EMPTY("No Entry selected!\nPlease select an Tableentry."),
    DBCONN_UPDATE_BEPATIENT("Updating database (may take a longer time)..."),
    ACTIVITYCONFIRMATION("Activity Confirmation"),
    ACTIVITY_EMPTY_DATE("Missing Product or Date for Activity-Entry!"),
    NOT_YET_IMPLEMENTED("This Function is actualy not implemented!"),
    TYPE_ACTIVITY("Activity Confirmation"),
    ActivityList_Existing("There's an Activity List Existing. Add this to the Invoice?"),
    ActivityList_toSubItem("See attached Acitivy Confirmation for Project: "),
    CHECK_TPLUPDATE("Checking Templates to update from Filechange ..."),
    MASSPRINT_FILTER("Filter Entries by:"),
    MASSPRINT_ENTER_NAME("Enter a Name for the Filter-Rule:"),
    MASSPRINT_FILTER_ALL("All Contacts"),
    SCRIPT_ERROR("A script did not run successfully:"),
    SELECT_A_TYPE("Select a Type!"),
    NOTIFICATION("Notification"),
    OO_WAITING("Please wait while connecting to OO/LO"),
    OO_NOT_CONFIGURED("Office is not configured yet."),
    PRINTER_NAMES("Possible printer names:"), 
    VARIABLES("Resolved vars for:"),
    ACTION_RESTORE("In Order to do a Restore of your DB you need to restart YABS!\nYABS will ask you for the Backup-Path at the next restart!"),
    ACTION_CALCULATING("Calculating ..."),
    ACTION_CREATING("Creating ..."),
    ACTION_CONVERTING("Converting ..."),
    ACTION_RESTORING("Restoring ..."), 
    ACTION_IMPORTING("Importing ..."), 
    ACTION_SAVING("Saving ..."),
    ACTION_SHUTINGDOWN("Shuting down ..."),
    ACTION_UPDATING("Updating ..."),
    ENTER_SCHEMA("Enter Value for MYSQL Databasename:"),
    CREATE_DATABASE_OWN("You must create the database as specified on the previous screen yourself."), 
    DELETE_DATABASE("Shall YABS delete the existing DB and create a new one?"),
    CREATE_DATABASE("Shall YABS create the Database for you?"),
    Alert("Alert"),
    OPEN_BACKUP("Select File/Directory for Backup restore:"),
    SAVE_BACKUP("Select Directory for Backup save:"),
    INIT_TMP_DERBY("Creating temporary Derby-DB!"),
    EXPORT_TO_DERBY("Exporting the active Database from current to DERBY."),
    EXPORT_TO_MYSQL("Exporting the active Database from current to MySQL."),
    FLUSHING_TMP_TABLE("Flush targettable for inserting"),
    OVERRITE_DATABASE("This will override the actuall Database!\nIt's recommend to make a Backup befor you proceed.\nAre You sure?"), 
    SET_STATUS_PAID("Set to status: 'Paid'");

    /**
     * @return the missing
     */
    public static List<String> getMissing() {
        return Collections.unmodifiableList(missing);
    }

    private Messages(String message) {
        this.message = message;
    }
    private String message;
    private static List<String> missing = new ArrayList<String>();

    public void addMessage(String message) {
        missing.add(message);
    }

    @Override
    public String toString() {

        if(missing.contains(name())) {
            return message;
        }
        try {
            return LanguageManager.getBundle().getString(name());
        } catch (Exception e) {
            Log.Debug(this, e.getMessage());
            addMessage(name());
        }

        if (message == null) {
            return super.toString();
        } else {
            return message;
        }
    }

    /**
     * Refers to toString()
     *
     * @return The value for this enum
     */
    public String getValue() {
        return toString();
    }
}
