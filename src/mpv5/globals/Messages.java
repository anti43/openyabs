/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mpv5.globals;

import mpv5.logging.Log;
import mpv5.i18n.LanguageManager;

/**
 * This enum holds all messages for the mp5 main application
 *Start with -license for details.\nStart with -help for command line options.
 *  :
 * YaBS (@version) (C) 2006-2009 Openyabs.org
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 2 of the License, or (at your option) any later version.\n\nThis program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.\n\nYou should have received a copy of the GNU General Public License along with this program; if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA\n\nStart with -license for details.\nStart with -help for command line options.
 */
public enum Messages {

    START_MESSAGE(
    "\n" +
    "YaBS " + Constants.VERSION + " (C) 2006-2009 Openyabs.org\n\n" +
    "******************************************************************************\n" +
    "* This program is free software; you can redistribute it and/or modify it \n" +
    "* under the terms of the GNU General Public License as published by the \n" +
    "* Free Software Foundation; either version 2 of the License, or (at your option) \n" +
    "* any later version.\n*\n" +
    "* This program is distributed in the hope that it will be useful, \n" +
    "* but WITHOUT ANY WARRANTY; without even the implied warranty of \n" +
    "* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General \n" +
    "* Public License for more details.\n*\n" +
    "* You should have received a copy of the GNU General Public License along with \n" +
    "* this program; if not, write to the Free Software Foundation, Inc., \n" +
    "* 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA\n*\n" +
    "* Start with -license for details.\n" +
    "* Start with -help for command line options.\n" +
    "******************************************************************************"),
    CONTACT("Contact: "),
    /**
     * Start MAIN
     */
    INIT("Initialising.."),
    LOCAL_SETTINGS("Reading local settings.."),
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
    NEW_BILL("New Bill"),
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
    CNAME_CANNOT_BE_NULL("You must set a NAME for this item."),
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
    CONTACTS_LIST("List of contacts"),
    CONTROL_PANEL("Control Panel"),
    REALLY_CLOSE("Do you really want to close the entire application?\nUnsaved data will be lost!"),
    USER_NOT_FOUND("User not found: "),
    NO_DATA_FOUND("Sorry, but there was no matching data in the database."),
    REALLY_WIPE("Do you really want to wipe all data?"),
    WIPED_LOCALSETTINGS("Local settings have been marked for deletion. Please close MP now."),
    NO_DB_CONNECTION("Could not connect to database, start configuration wizard?"),
    REALLY_CHANGE("Do you really want to alter the selected dataset? This may affect all users!"),
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
    HISTORY_OF("History of "),
    ASSET("Asset"),
    COST("Cost"),
    EQUITY("Equity"),
    EXPENSE("Expenses"),
    REVENUE("Revenues"),
    INCOME("Income"),
    LOCK_FAILED("Lock failed!"),
    LOCKED_BY("This dataset is currently locked by another user\nand can not be edited.."),
    LIABILITY("Liability"),
    STATUS_QUEUED("Queued"),
    STATUS_IN_PROGRESS("In progress"),
    STATUS_PAUSED("Paused"),
    STATUS_FINISHED("Finished"),
    STATUS_CANCELLED("Cancelled"),
    TYPE_BILL("Bill"),
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
    TYPE_EXPENSE("Expense"),
    STATUS_PAID("Paid"),
    TRASHBIN("Trashbin"),
    DELETE("Delete"),
    RESTORE("Restore"),
    RESTORED(" restored"),
    RELOAD("Reload"),
    LOADED(" loaded"),
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
    OO_NOT_CONFIGURED("OO not available"),
    SET_AS_DEFAULT("Set as default"),
    NO_ACCOUNTS("No accounts or companies are configured for you."),
    CONTACTS_TRANSMITTED("contacts have been transmitted to"),
    ORDERS_RECEIVED("orders have been received from"),
    LOAD_NOW("Load now?"),
    NO_TEMPLATE_DEFINDED("There is no template assigned to your account matching this export type."),
    NO_TEMPLATE_LOADED("The template is not loaded yet. Please try again in a few seconds."),
    SCHEDULE_NEXT("Next date for this item: "),
    SCHEDULE("Schedule"),
    SCHEDULE_GENERATED("Autogenerated Item. Original:"),
    SCHEDULE_NEW_ITEMS("new items have been generated by the scheduler. Open now?"),
    SCHEDULE_NEW_WARNINGS("unpaid bills have reached the warning treshold. Open now?"),
    SCHEDULE_NEW_ALERTS("unpaid bills have reached the alert treshold. Open now?"),
    SCHEDULE_ITEM_REMOVED("Scheduled item reached end-date:"),
    WELCOME("Welcome"),
    SELECT_A_TEMPLATE("Select a template:"),
    UNPAID_BILLS("Unpaid bills"),
    UNPAID_BILLS_OVERDUE("Overdues"),
    NO_MAIL_CONFIG("Mail not properly configured."),
    MAIL_SENT("Mail sent to:"),
    NO_MAIL_DEFINED("Either you or the contact you want to send the mail to (or both) have no valid mail adress defined.");

    private Messages(String message) {
        this.message = message;
    }
    private String message;

    public void addMessage(String message) {
    }

    @Override
    public String toString() {

        try {
            message = LanguageManager.getBundle().getString(this.name());
        } catch (Exception e) {
            Log.Debug(this, e.getMessage());

        }

        if (message == null) {
            return super.toString();
        } else {
            return message;
        }
    }

    public String getValue() {
        return toString();
    }
}
