/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mpv5.globals;

/**
 *
 * @author Andreas
 */
public class Messages {

    public static final String START_MESSAGE =
            "\n" +
            "\n  MP " + Constants.VERSION + " Copyright (C) 2006-2008 Andreas Weber\n" +
            "\n  This program comes with ABSOLUTELY NO WARRANTY." +
            "\n  MP is free software, and you are welcome to redistribute it " +
            "\n  under certain conditions;" +
            "\n  Start with -license for details.\n" +
            "\n  Start with -help for command line options.\n" +
            "*****************************************************************\n\n";

//    public static final String GPL = new FileReaderWriter("license.txt").read();
    public static String NEW_CONTACT = "New Contact";
    public static String NEW_CUSTOMER = "New Customer";
    public static String NEW_SUPPLIER = "New Supplier";
    public static String NEW_MANUFACTURER = "New Manufacturer";
    public static String NEW_OFFER = "New Offer";
    public static String NEW_BILL = "New Bill";
    public static String ACCESS_DENIED = "Access denied.";
    public static String ACTION_CREATE = "Create";
    public static String ACTION_EDIT = "Edit";
    public static String ACTION_EXPORT = "Export";
    public static String ACTION_VIEW = "View";
    public static String SECURITYMANAGER_DENIED = "The Security Manager has DENIED your request.\nAction ";
    public static String SECURITYMANAGER_ALLOWED = "The Security Manager has ALLOWED your request.\nAction ";
    public static String CONTEXT = ", in context: ";
    public static String ROW_INSERTED = " successful created";
    public static String ROW_UPDATED = " successful saved";
    public static String ROW_DELETED = " successful removed from database";
    public static String CNAME_CANNOT_BE_NULL = "You must set a NAME for this item.";
    public static String FILE_SAVED = "File saved: ";
    public static String ERROR_OCCURED = "An error occurred";
    public static String FILE_NOT_SAVED = "File NOT saved: ";
    public static String WARNING = "Warning";
    public static String NOTICE = "Notice";
    public static String FILE_EXISTS = "The file already exists, overwrite?";
    public static String ARE_YOU_SURE = "Are you sure?";
    public static String FINISH = "Finish";
    public static String CONNECTION_VERIFIED = "The Database Connection has been verified.";
    public static String CONNECTION_FAILED = "The Database Connection failed.";
    public static String CONNECTION_PROBE = "Checking Database Connection: ";
    public static String ERROR_SAVING_LOCALSETTINGS = "Could not save local settings";
    public static String DB_DRIVER_INVALID = "The driver for the specified database type could not be loaded\n";
    public static String CREATING_DATABASE = "Creating database structure..";
    public static String CREATING_DATABASE_FAILED = "Creating database structure failed, see logs for details.";
    public static String CONTACTS_LIST = "List of contacts";
    public static String CONTROL_PANEL = "Control Panel";
    public static String REALLY_CLOSE = "Do you really want to close the entire application?\nUnsaved data will be lost!";
    public static String USER_NOT_FOUND = "User not found: ";
    public static String NO_DATA_FOUND = "Sorry, but there was no matching data in the database.";
    public static String REALLY_WIPE = "Do you really want to wipe all data?\nYou may need to restart the application before changes take place.";
    public static String WIPED_LOCALSETTINGS = "Local settings have been marked for deletion. Please close MP now.";
    public static String NO_DB_CONNECTION = "Could not connect to database, start configuration wizard?";
    public static String REALLY_CHANGE = "Do you really want to alter the selected dataset? This will affect all users!";
    public static String DONE = "Task finished successfully.";
    public static String PROCESSING = "Processing: ";
    public static String LOCKED = " is now locked";
    public static String VALUE_NOT_VALID = "You have entered an invalid value: ";
    public static String VALUE_ALREADY_EXISTS = "The value already exists in the database and must not be duplicated: ";
    public static String ADMIN_ACCESS = "You need administrative rights to perform this action.";
    public static String NOT_POSSIBLE = "Requested action is not possible.";
    public static String DEFAULT_USER = "Default user can`t be changed,\nlog in as real user to perform this action.";
    public static String REALLY_DELETE = "Do you really want to delete this dataset?";
}
