/*
 *  This file is part of YaBS.
 *
 *      YaBS is free software: you can redistribute it and/or modify
 *      it under the terms of the GNU General Public License as published by
 *      the Free Software Foundation, either version 3 of the License, or
 *      (at your option) any later version.
 *
 *      YaBS is distributed in the hope that it will be useful,
 *      but WITHOUT ANY WARRANTY; without even the implied warranty of
 *      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *      GNU General Public License for more details.
 *
 *      You should have received a copy of the GNU General Public License
 *      along with YaBS.  If not, see <http://www.gnu.org/licenses/>.
 */
package mpv5.db.common;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import mpv5.db.objects.Account;
import mpv5.db.objects.ActivityList;
import mpv5.db.objects.ActivityListSubItem;
import mpv5.db.objects.Address;
import mpv5.db.objects.Company;
import mpv5.db.objects.Contact;
import mpv5.db.objects.Conversation;
import mpv5.db.objects.Expense;
import mpv5.db.objects.Favourite;
import mpv5.db.objects.FileToContact;
import mpv5.db.objects.FileToItem;
import mpv5.db.objects.FileToProduct;
import mpv5.db.objects.Fonts;
import mpv5.db.objects.Group;
import mpv5.db.objects.HistoryItem;
import mpv5.db.objects.Item;
import mpv5.db.objects.MailMessage;
import mpv5.db.objects.MassprintRules;
import mpv5.db.objects.Product;
import mpv5.db.objects.ProductGroup;
import mpv5.db.objects.ProductList;
import mpv5.db.objects.ProductOrder;
import mpv5.db.objects.ProductOrderSubItem;
import mpv5.db.objects.ProductPrice;
import mpv5.db.objects.ProductlistSubItem;
import mpv5.db.objects.ProductsToSuppliers;
import mpv5.db.objects.Reminder;
import mpv5.db.objects.Revenue;
import mpv5.db.objects.Schedule;
import mpv5.db.objects.ScheduleTypes;
import mpv5.db.objects.Stage;
import mpv5.db.objects.SubItem;
import mpv5.db.objects.Tax;
import mpv5.db.objects.Template;
import mpv5.db.objects.User;
import mpv5.db.objects.UserProperty;
import mpv5.db.objects.ValueProperty;
import mpv5.db.objects.WSContactsMapping;
import mpv5.db.objects.WSItemsMapping;
import mpv5.db.objects.WebShop;
import mpv5.logging.Log;
import mpv5.pluginhandling.Plugin;
import mpv5.pluginhandling.UserPlugin;
import mpv5.usermanagement.MPSecurityManager;
import mpv5.utils.text.RandomText;
import mpv5.utils.xml.XMLReader;

/**
 *
 * Context controls Database Queries for all native MP
 * {@link DatabaseObject}s
 */
public class Context implements Serializable, Comparable<Context> {

    //********** tablenames ****************************************************
    public static String IDENTITY_CONTACTS = "contacts";
    public static String IDENTITY_USERS = "users";
    public static String IDENTITY_ITEMS = "items";
    public static String IDENTITY_SUBITEMS = "subitems";
    public static String IDENTITY_LANGUAGES = "languages";
    public static String IDENTITY_COUNTRIES = "countries";
    public static String IDENTITY_PRODUCTS = "products";
    public static String IDENTITY_FILES = "files";
    public static String IDENTITY_LOCK = "tablelock";
    public static String IDENTITY_FAVS = "favourites";
    public static String IDENTITY_ADDRESS = "addresses";
    public static String IDENTITY_GROUPS = "groups";
    public static String IDENTITY_PGROUPS = "productgroups";
    public static String IDENTITY_SCHEDULE = "schedule";
    public static String IDENTITY_SCHEDULE_TYPES = "scheduletypes";
    public static String IDENTITY_HISTORY = "history";
    public static String IDENTITY_FILES_TO_CONTACTS = "filestocontacts";
    public static String IDENTITY_FILES_TO_PRODUCTS = "filestoproducts";
    public static String IDENTITY_SEARCHINDEX = "searchindex";
    public static String IDENTITY_PLUGINS_TO_USERS = "pluginstousers";
    public static String IDENTITY_TEMPLATES_TO_USERS = "templatestousers";
    public static String IDENTITY_PLUGINS = "plugins";
    public static String IDENTITY_PROPERTIES_TO_USERS = "userproperties";
    public static String IDENTITY_ACCOUNTS = "accounts";
    public static String IDENTITY_ITEMS_TO_ACCOUNTS = "itemstoaccounts";
    public static String IDENTITY_PRODUCTSLISTITEMS = "productlistitems";
    public static String IDENTITY_PRODUCTSLIST = "productlists";
    public static String IDENTITY_FORMATS_T_USERS = "formatstousers";
    public static String IDENTITY_FILES_TO_ITEMS = "filestoitems";
    public static String IDENTITY_MAIL = "mails";
    public static String IDENTITY_TAX = "tax";
    public static String IDENTITY_COMPANIES = "comps";
    public static String IDENTITY_GLOBALSETTINGS = "globalsettings";
    public static String IDENTITY_WEBSHOPS = "webshops";
    public static String IDENTITY_WSMAPPING = "wscontactsmapping";
    public static String IDENTITY_WSIMAPPING = "wsitemsmapping";
    public static String IDENTITY_TEMPLATES = "templates";
    public static String IDENTITY_REMINDERS = "reminders";
    public static String IDENTITY_STAGES = "stages";
    public static String IDENTITY_REVENUE = "revenues";
    public static String IDENTITY_EXPENSE = "expenses";
    public static String IDENTITY_PRODUCTS_TO_SUPPLIERS = "productstosuppliers";
    public static String IDENTITY_VALUE_PROPERTIES = "valueproperties";
    public static String IDENTITY_CONVERSATION = "conversations";
    public static String IDENTITY_PRODUCT_PRICES = "productprices";
    public static String IDENTITY_ACTIVITYLISTITEMS = "activitylistitems";
    public static String IDENTITY_ACTIVITYLIST = "activitylists";
    public static String IDENTITY_MASSPRINT = "massprintrules";
    public static String IDENTITY_PRODUCTORDER = "productorders";
    public static String IDENTITY_PRODUCTORDERSUBITEM = "productordersubitems";
    public static String IDENTITY_FONTS = "fontsforitext";
    //********** identity classes **********************************************
    private static Class<Contact> IDENTITY_CONTACTS_CLASS = Contact.class;
    private static Class<Address> IDENTITY_ADDRESS_CLASS = Address.class;
    private static Class<User> IDENTITY_USERS_CLASS = User.class;
    private static Class<Item> IDENTITY_ITEMS_CLASS = Item.class;
    private static Class<FileToContact> IDENTITY_CONTACTS_FILES_CLASS = FileToContact.class;
    private static Class<FileToItem> IDENTITY_ITEM_FILES_CLASS = FileToItem.class;
    private static Class<HistoryItem> IDENTITY_HISTORY_ITEMS_CLASS = HistoryItem.class;
    private static Class<SubItem> IDENTITY_SUBITEMS_CLASS = SubItem.class;
    private static Class<UserPlugin> IDENTITY_USER_PLUGINS_CLASS = UserPlugin.class;
    private static Class<Plugin> IDENTITY_PLUGINS_CLASS = Plugin.class;
    private static Class<UserProperty> IDENTITY_PROPERTIES_CLASS = UserProperty.class;
    private static Class<Account> IDENTITY_ACCOUNTS_CLASS = Account.class;
    private static Class<ProductlistSubItem> IDENTITY_ITEMSLIST_CLASS = ProductlistSubItem.class;
    private static Class<MailMessage> IDENTITY_MAILS_CLASS = MailMessage.class;
    private static Class<Product> IDENTITY_PRODUCTS_CLASS = Product.class;
    private static Class<Group> IDENTITY_GROUPS_CLASS = Group.class;
    private static Class<Company> IDENTITY_COMPANY_CLASS = Company.class;
    private static Class<ProductGroup> IDENTITY_PGROUPS_CLASS = ProductGroup.class;
    private static Class<FileToProduct> IDENTITY_PRODUCTS_FILES_CLASS = FileToProduct.class;
    private static Class<WebShop> IDENTITY_WEBSHOP_CLASS = WebShop.class;
    private static Class<Template> IDENTITY_TEMPLATE_CLASS = Template.class;
    private static Class<Reminder> IDENTITY_REMINDER_CLASS = Reminder.class;
    private static Class<Stage> IDENTITY_STAGE_CLASS = Stage.class;
    private static Class<ValueProperty> IDENTITY_VALUEPROPERTY_CLASS = ValueProperty.class;
    private static Class<ProductPrice> IDENTITY_PRODUCTPRICE_CLASS = ProductPrice.class;
    private static Class<Favourite> IDENTITY_FAVOURITE_CLASS = Favourite.class;
    private static Class<Tax> IDENTITY_TAX_CLASS = Tax.class;
    private static Class<WSContactsMapping> IDENTITY_WSCONTACTSMAPPING_CLASS = WSContactsMapping.class;
    private static Class<WSItemsMapping> IDENTITY_WSITEMSMAPPING_CLASS = WSItemsMapping.class;
    private static Class<Expense> IDENTITY_EXPENSE_CLASS = Expense.class;
    private static Class<ProductList> IDENTITY_PRODUCTLIST_CLASS = ProductList.class;
    private static Class<ProductsToSuppliers> IDENTITY_PRODUCTSTOSUPPLIERS_CLASS = ProductsToSuppliers.class;
    private static Class<Conversation> IDENTITY_CONVERSATION_CLASS = Conversation.class;
    private static Class<Schedule> IDENTITY_SCHEDULE_CLASS = Schedule.class;
    private static Class<ScheduleTypes> IDENTITY_SCHEDULETYPES_CLASS = ScheduleTypes.class;
    private static Class<ActivityListSubItem> IDENTITY_ACTIVITYITEMSLIST_CLASS = ActivityListSubItem.class;
    private static Class<ActivityList> IDENTITY_ACTIVITYLIST_CLASS = ActivityList.class;
    private static Class<MassprintRules> IDENTITY_MASSPRINT_CLASS = MassprintRules.class;
    private static Class<ProductOrder> IDENTITY_PRODUCTORDER_CLASS = ProductOrder.class;
    private static Class<ProductOrderSubItem> IDENTITY_PRODUCTORDERSUBITEM_CLASS = ProductOrderSubItem.class;
    private static Class<Fonts> IDENTITY_FONTS_CLASS = Fonts.class;

    //********** unique constraints *******************************************
    public static String UNIQUECOLUMNS_USER = "cname";
    public static String UNIQUECOLUMNS_ITEMS = "cname";
    public static String UNIQUECOLUMNS_GROUPS = "cname";
    public static String UNIQUECOLUMNS_DEFAULT = "cname";
    public static String DETAIL_CONTACT_SEARCH = "prename,cname,street,city,country,notes,company";
    private static final long serialVersionUID = 1L;

    //********** conditions ****************************************************
    private boolean isCompany = false;
    private boolean isCustomer = false;
    private boolean isManufacturer = false;
    private boolean isSupplier = false;
    private Integer itemStatus = null;
    private Integer itemType = null;
    public static final String CONDITION_DEFAULT = "%%tablename%%" + "." + "IDS>0";
    public static final String CONDITION_CONTACTS_COMPANY = IDENTITY_CONTACTS + "." + "iscompany";
    public static final String CONDITION_CONTACTS_CUSTOMER = IDENTITY_CONTACTS + "." + "iscustomer";
    public static final String CONDITION_CONTACTS_MANUFACTURER = IDENTITY_CONTACTS + "." + "ismanufacturer";
    public static final String CONDITION_CONTACTS_SUPPLIER = IDENTITY_CONTACTS + "." + "issupplier";
    public static final String CONDITION_ITEMS_TYPE = IDENTITY_ITEMS + "." + "inttype";
    public static final String CONDITION_ITEMS_STATUS = IDENTITY_ITEMS + "." + "intstatus";
    //********** searchfields **************************************************
    public static final String SEARCH_NAME = "cname";
//    public static final String SEARCH_CONTACT_NUMBER = "cnumber";
//    public static final String SEARCH_CONTACT_CITY = "city";
    //********** defaults ******************************************************
    public static String DEFAULT_SUBID = "ids, cname";
    public static String DEFAULT_SEARCH = "ids, cname";
    public static String DEFAULT_CONTACT_SEARCH = "ids, cnumber, cname, city, street";
    public static String DEFAULT_USER_SEARCH = "ids, cname, mail, datelastlog";
    public static String DEFAULT_ITEM_SEARCH = "ids, cname, dateadded, netvalue";
    public static String DEFAULT_PRODUCT_SEARCH = "ids, cnumber, cname, description";
    public static String DEFAULT_ACTIVITYLIST_SEARCH = "DISTINCT ids,cname, totalamount";
    public static String DEFAULT_PRODUCTSLIST_SEARCH = "DISTINCT ids,cname,description";
    //********** table fields ********************************************************
    public static String DETAILS_CONTACTS = IDENTITY_CONTACTS + "." + "IDS," + IDENTITY_CONTACTS + "." + "CNUMBER,"
            + IDENTITY_CONTACTS + "." + "TITLE," + IDENTITY_CONTACTS + "." + "PRENAME," + IDENTITY_CONTACTS + "." + "CNAME,"
            + IDENTITY_CONTACTS + "." + "STREET," + IDENTITY_CONTACTS + "." + "ZIP," + IDENTITY_CONTACTS + "." + "CITY,"
            + IDENTITY_CONTACTS + "." + "MAINPHONE," + IDENTITY_CONTACTS + "." + "FAX," + IDENTITY_CONTACTS + "." + "MOBILEPHONE,"
            + IDENTITY_CONTACTS + "." + "WORKPHONE," + IDENTITY_CONTACTS + "." + "COMPANY," + IDENTITY_CONTACTS + "." + "MAILADDRESS,"
            + IDENTITY_CONTACTS + "." + "WEBSITE," + IDENTITY_CONTACTS + "." + "NOTES,"
            + IDENTITY_CONTACTS + "." + "TAXNUMBER";
    public static String DETAILS_PRODUCTS
            = IDENTITY_PRODUCTS + "." + "ids,"
            + IDENTITY_PRODUCTS + "." + "cnumber,"
            + IDENTITY_PRODUCTS + "." + "cname,"
            + IDENTITY_PRODUCTS + "." + "ean,"
            + IDENTITY_PRODUCTS + "." + "reference,"
            + IDENTITY_CONTACTS + "." + "cname,"
            + IDENTITY_GROUPS + "." + "cname,"
            + IDENTITY_PRODUCTS + "." + "internalnetvalue,"
            + IDENTITY_PRODUCTS + "." + "externalnetvalue,"
            + IDENTITY_PRODUCTS + "." + "stockvalue";
    public static String DETAILS_USERS = IDENTITY_USERS + "." + "IDS," + IDENTITY_USERS + "." + "CNAME,"
            + IDENTITY_USERS + "." + "fullname,"
            + IDENTITY_USERS + "." + "mail,"
            + IDENTITY_USERS + "." + "isenabled,"
            + IDENTITY_USERS + "." + "isloggedin";
    public static String DETAILS_ITEMS
            = IDENTITY_ITEMS + "." + "IDS,"
            + IDENTITY_ITEMS + "." + "CNAME,"
            + IDENTITY_ITEMS + "." + "dateadded,"
            + IDENTITY_ITEMS + "." + "netvalue,"
            + IDENTITY_ITEMS + "." + "taxvalue, "
            + IDENTITY_ITEMS + "." + "datetodo";
    public static String DETAILS_JOURNAL 
            = Context.IDENTITY_ITEMS + "." + "IDS," 
            + Context.IDENTITY_CONTACTS + "." + "cname," 
            + Context.IDENTITY_CONTACTS + "." + "prename," 
            + Context.IDENTITY_CONTACTS + "." + "street," 
            + Context.IDENTITY_CONTACTS + "." + "city," 
            + Context.IDENTITY_CONTACTS + "." + "country," 
            + Context.IDENTITY_ITEMS + "." + "{date}," 
            + Context.IDENTITY_GROUPS + "." + "CNAME," 
            + Context.IDENTITY_ACCOUNTS + "." + "cname," 
            + Context.IDENTITY_ITEMS + "." + "CNAME," 
            + Context.IDENTITY_ITEMS + "." + "inttype," 
            + Context.IDENTITY_ITEMS + "." + "intstatus,(" 
                + Context.IDENTITY_ITEMS + "." + "netvalue-" 
                + Context.IDENTITY_ITEMS + "." + "discountvalue)," 
            + Context.IDENTITY_ITEMS + "." + "taxvalue, (" 
                + Context.IDENTITY_ITEMS + "." + "netvalue +" 
                + Context.IDENTITY_ITEMS + "." + "taxvalue -" 
                + Context.IDENTITY_ITEMS + "." + "discountvalue)," 
            + Context.IDENTITY_ITEMS + "." + "discountvalue, ''";
    public static String DETAILS_JOURNAL2 
            = Context.IDENTITY_REVENUE + "." + "IDS," 
            + Context.IDENTITY_CONTACTS + "." + "cname," 
            + Context.IDENTITY_CONTACTS + "." + "prename," 
            + Context.IDENTITY_CONTACTS + "." + "street," 
            + Context.IDENTITY_CONTACTS + "." + "city," 
            + Context.IDENTITY_CONTACTS + "." + "country," 
//            + Context.IDENTITY_GROUPS + "." + "reserve2," 
//            + Context.IDENTITY_GROUPS + "." + "reserve2," 
//            + Context.IDENTITY_GROUPS + "." + "reserve2," 
//            + Context.IDENTITY_GROUPS + "." + "reserve2," 
//            + Context.IDENTITY_GROUPS + "." + "reserve2," 
            + Context.IDENTITY_REVENUE + "." + "{date}," 
            + Context.IDENTITY_GROUPS + "." + "CNAME," 
            + Context.IDENTITY_ACCOUNTS + "." + "cname," 
            + Context.IDENTITY_REVENUE + "." + "CNAME," 
            + Context.IDENTITY_REVENUE + "." + "ids," 
            + Context.IDENTITY_REVENUE + "." + "status," 
            + Context.IDENTITY_REVENUE + "." + "netvalue, (" + Context.IDENTITY_REVENUE + "." + "brutvalue-" + Context.IDENTITY_REVENUE + "." + "netvalue)," 
            + Context.IDENTITY_REVENUE + "." + "brutvalue, 0 ,"
            + Context.IDENTITY_REVENUE + "." + "description";
    public static String DETAILS_JOURNAL3 
            = Context.IDENTITY_EXPENSE + "." + "IDS," 
            + Context.IDENTITY_GROUPS + "." + "reserve2," 
            + Context.IDENTITY_GROUPS + "." + "reserve2," 
            + Context.IDENTITY_GROUPS + "." + "reserve2," 
            + Context.IDENTITY_GROUPS + "." + "reserve2," 
            + Context.IDENTITY_GROUPS + "." + "reserve2," 
            + Context.IDENTITY_EXPENSE + "." + "{date}," 
            + Context.IDENTITY_GROUPS + "." + "CNAME," 
            + Context.IDENTITY_ACCOUNTS + "." + "cname," 
            + Context.IDENTITY_EXPENSE + "." + "CNAME," 
            + Context.IDENTITY_EXPENSE + "." + "ids," 
            + Context.IDENTITY_EXPENSE + "." + "ispaid," 
            + Context.IDENTITY_EXPENSE + "." + "netvalue, (" + Context.IDENTITY_EXPENSE + "." + "brutvalue-" + Context.IDENTITY_EXPENSE + "." + "netvalue)," 
            + Context.IDENTITY_EXPENSE + "." + "brutvalue, 0, " 
            + Context.IDENTITY_EXPENSE + ".description";
    public static String DETAILS_HISTORY 
            = getHistory().getDbIdentity() + ".ids, " 
            + getHistory().getDbIdentity() + ".cname, " 
            + getHistory().getDbIdentity() + ".username, " 
            + Context.getGroup().getDbIdentity() + ".cname," 
            + Context.getHistory().getDbIdentity() + ".dateadded";
    public static String DETAILS_FILES_TO_CONTACTS 
            = Context.getFiles().getDbIdentity() + ".cname," 
            + getFilesToContacts().getDbIdentity() + ".cname, " 
            + Context.getFiles().getDbIdentity() + ".dateadded," 
            + Context.getFilesToContacts().getDbIdentity() + ".description," 
            + Context.getFilesToContacts().getDbIdentity() + ".intsize," 
            + Context.getFilesToContacts().getDbIdentity() + ".mimetype";
    public static String DETAILS_FILES_TO_ITEMS 
            = Context.getFiles().getDbIdentity() + ".cname," 
            + getFilesToItems().getDbIdentity() + ".cname, " 
            + Context.getFiles().getDbIdentity() + ".dateadded," 
            + Context.getFilesToItems().getDbIdentity() + ".description," 
            + Context.getFilesToItems().getDbIdentity() + ".intsize," 
            + Context.getFilesToItems().getDbIdentity() + ".mimetype";
    public static String DETAILS_FILES_TO_PRODUCTS 
            = Context.getFiles().getDbIdentity() + ".cname," 
            + getFilesToProducts().getDbIdentity() + ".cname, " 
            + Context.getFiles().getDbIdentity() + ".dateadded," 
            + Context.getFilesToProducts().getDbIdentity() + ".description," 
            + Context.getFilesToProducts().getDbIdentity() + ".intsize," 
            + Context.getFilesToProducts().getDbIdentity() + ".mimetype";
    public static String DETAILS_MASSPRINT
            = Context.IDENTITY_MASSPRINT + "." + "IDS,"
            + Context.IDENTITY_MASSPRINT + "." + "cname,"
            + Context.IDENTITY_MASSPRINT + "." + "inttype,"
            + Context.IDENTITY_MASSPRINT + "." + "dateadded";
//    public static String DETAILS_FILES_TO_TEMPLATES = Context.getTemplate().getDbIdentity() + ".ids," + getTemplate().getDbIdentity() + ".cname, " + Context.getTemplate().getDbIdentity() + ".dateadded," + Context.getTemplate().getDbIdentity() + ".intsize," + Context.getTemplate().getDbIdentity() + ".mimetype";
//    public static String DETAILS_TEMPLATES = Context.getTemplate().getDbIdentity() + ".ids," + getTemplate().getDbIdentity() + ".cname, " + Context.getTemplate().getDbIdentity() + ".mimetype," + " groups0.cname";
    private static final Map<String, String> FOREIGN_KEY_ALIASES = new HashMap<>();

    /**
     * Find an alias (eg originalproducstids is an alias for
     * productsids in table subitems)
     *
     * @param context
     * @param exct
     * @return
     */
    public static String getAliasFor(Context context, Context exct) {
        if (FOREIGN_KEY_ALIASES.containsKey(context + "@" + exct)) {
            return FOREIGN_KEY_ALIASES.get(context + "@" + exct);
        } else {
            return context.getDbIdentity();
        }
    }
    //**************************************************************************

    /**
     * Contexts which can have an export template
     *
     * @return
     */
    public static ArrayList<Context> getTemplateableContexts() {
        ArrayList<Context> list = new ArrayList<>();
        list.add(getInvoice());
        list.add(getDeposit());
        list.add(getPartPayment());
        list.add(getCredit());
        list.add(getOrder());
        list.add(getOffer());
        list.add(getProduct());
        list.add(getReminder());
        list.add(getActivityList());
        list.add(getConversation());
        return list;
    }
    
     public static ArrayList<Context> getSecuredContexts() {
        ArrayList<Context> list = new ArrayList<>();
        list.add(getContactsCompanies());
//        list.add(getUser()); Needs to be non-secure, to update user details on close
        list.add(getCustomer());
        list.add(getManufacturer());
        list.add(getSupplier());
        list.add(getAddress());
        list.add(getInvoice());
        list.add(getDeposit());
        list.add(getPartPayment());
        list.add(getCredit());
        list.add(getOrder());
        list.add(getOffer());
        list.add(getSchedule());
        list.add(getCountries());
        list.add(getContact());
        list.add(getProduct());
        list.add(getAccounts());
        list.add(getCompany());
        list.add(getWebShop());
        list.add(getTemplate());
        list.add(getReminder());
        list.add(getSubItem());
        list.add(getProductOrderSubitem());
        list.add(getProductOrder());
        list.add(getProductPrice());
        list.add(getConversation());
        list.add(getFonts());
        return list;
    }


    /**
     * Contexts which are protected by the Securitymanager
     *
     * @return
     */
    public static ArrayList<Context> getModifiableContexts() {
        ArrayList<Context> list = new ArrayList<>();
        list.add(getContactsCompanies()); 
        list.add(getCustomer());
        list.add(getManufacturer());
        list.add(getSupplier());
        list.add(getAddress());
        list.add(getInvoice());
        list.add(getDeposit());
        list.add(getPartPayment());
        list.add(getCredit());
        list.add(getOrder());
        list.add(getOffer()); 
        list.add(getCountries());
        list.add(getContact());
        list.add(getProduct());
        list.add(getAccounts());
        list.add(getCompany());   
        list.add(getSubItem());
        list.add(getProductOrderSubitem());
        list.add(getProductOrder());
        list.add(getProductPrice());
        list.add(getConversation()); 
        return list;
    }

    /**
     * Contexts which can get cached
     *
     * @return
     */
    public static ArrayList<Context> getCacheableContexts() {
        return cacheableContexts;
    }
    private static ArrayList<Context> cacheableContexts = new ArrayList<>(Arrays.asList(new Context[]{
        getContact(),
        getFavourite(),
        getUser(),
        getAddress(),
        getOffer(),
        getOrder(),
        getInvoice(), 
        getGroup(),
        getSchedule(),
        getFilesToContacts(),
        getFilesToItems(),
        getFilesToProducts(),
        getProduct(),
        getAccounts(),
        getMessage(),
        getProductListItems(),
        getCompany(),
        getWebShop(),
        getTemplate(),
        getReminder(),
        getStage(),
        getTaxes(),
        getProductGroup(),
        getProductPrice(),
        getActivityListItems(),
        getProductOrderSubitem(),
        getProductOrder(),
        getConversation()
    }));

    /**
     * Contexts which are groupable
     *
     * @return
     */
    public static ArrayList<Context> getGroupableContexts() {
        ArrayList<Context> list = new ArrayList<>();
        list.add(getContactsCompanies());
        list.add(getCustomer());
        list.add(getManufacturer());
        list.add(getSupplier());
        list.add(getInvoice());
        list.add(getDeposit());
        list.add(getPartPayment());
        list.add(getCredit());
        list.add(getOrder());
        list.add(getOffer());
        list.add(getSchedule());
        list.add(getContact());
        list.add(getProduct());
        list.add(getAccounts());
        list.add(getMessage());
        list.add(getProductListItems());
        list.add(getSearchIndex());
        list.add(getGlobalSettings());
        list.add(getCompany());
        list.add(getWebShop());
        list.add(getTemplate());
        list.add(getReminder());
        list.add(getStage());
        list.add(getExpense());
        list.add(getRevenue());
        list.add(getTaxes());
        list.add(getProductPrice());
        list.add(getActivityListItems());
        list.add(getProductOrderSubitem());
        list.add(getProductOrder());
        list.add(getConversation());
        list.add(getFonts());
        return list;
    }

    /**
     * Contexts which can be moved to trash rather than
     * delete
     *
     * @return
     */
    public static ArrayList<Context> getTrashableContexts() {
        ArrayList<Context> list = new ArrayList<>();
        list.add(getContactsCompanies());
        list.add(getCustomer());
        list.add(getManufacturer());
        list.add(getSupplier());
        list.add(getInvoice());
        list.add(getDeposit());
        list.add(getPartPayment());
        list.add(getCredit());
        list.add(getOrder());
        list.add(getOffer());
        list.add(getSubItem());
        list.add(getContact());
        list.add(getProduct());
        list.add(getFiles());
        list.add(getExpense());
        list.add(getRevenue());
        list.add(getActivityList());
        list.add(getProductOrderSubitem());
        list.add(getProductOrder());
        list.add(getConversation());
        return list;
    }

    /**
     * Contexts which are monitored by the History
     *
     * @return
     */
    public static ArrayList<Context> getArchivableContexts() {
        return getSecuredContexts();
    }

    /**
     * Contexts which can be used in a user's Search
     *
     * @return
     */
    public static ArrayList<Context> getSearchableContexts() {
        ArrayList<Context> list = new ArrayList<>();
        list.add(getSubItem());
        list.add(getInvoice());
        list.add(getDeposit());
        list.add(getPartPayment());
        list.add(getCredit());
        list.add(getOrder());
        list.add(getOffer());
        list.add(getSchedule());
        list.add(getContact());
        list.add(getProduct());
        list.add(getProductOrderSubitem());
        list.add(getProductOrder());
        list.add(getConversation());
////        list.add(getAccounts());
        return list;
    }

    /**
     * LOckable Contexts
     *
     * @return
     */
    public static ArrayList<Context> getLockableContexts() {
        ArrayList<Context> list = new ArrayList<>();
//        list.add(getUser());
        list.add(getInvoice());
        list.add(getDeposit());
        list.add(getPartPayment());
        list.add(getCredit());
        list.add(getOrder());
        list.add(getOffer());
        list.add(getSchedule());
        list.add(getContact());
        list.add(getProduct());
        list.add(getContactsCompanies());
        list.add(getCustomer());
        list.add(getSupplier());
        list.add(getManufacturer());
        list.add(getWebShop());
        list.add(getConversation());
        return list;
    }

    /**
     * Importable Contexts
     *
     * @return
     */
    public static ArrayList<Context> getImportableContexts() {
        ArrayList<Context> list = new ArrayList<>();
        list.add(getInvoice());
        list.add(getDeposit());
        list.add(getPartPayment());
        list.add(getCredit());
        list.add(getOrder());
        list.add(getOffer());
        list.add(getMessage());
        list.add(getContact());
        list.add(getProduct());
        list.add(getAccounts());
        list.add(getGroup());
        return list;
    }

    /**
     *
     * @return All availbale contexts
     */
    public static ArrayList<Context> getContexts() {
        return allContexts;
    }
    /**
     * A list of all available contexts
     */
    private static ArrayList<Context> allContexts = new ArrayList<>(Arrays.asList(new Context[]{
        getContactsCompanies(),
        getContact(),
        getCustomer(),
        getFavourite(),
        getFiles(),
        getLanguage(),
        getLock(),
        getManufacturer(),
        getSupplier(),
        getUser(),
        getAddress(),
        getInvoice(),
        getDeposit(),
        getPartPayment(),
        getCredit(),
        getOrder(),
        getOffer(),
        getSubItem(),
        getGroup(),
        getSchedule(),
        getFilesToContacts(),
        getHistory(),
        getCountries(),
        getProduct(),
        getPlugins(),
        getPluginsToUsers(),
        getTemplatesToUsers(),
        getUserProperties(),
        getAccounts(),
        getItemsToAccounts(),
        getMessage(),
        getProductListItems(),
        getFormats(),
        getTaxes(),
        getGlobalSettings(),
        getCompany(),
        getWebShop(),
        getWebShopItemMapping(),
        getWebShopContactMapping(),
        getTemplate(),
        getReminder(),
        getStage(),
        getProductList(),
        getExpense(),
        getRevenue(),
        getProductsToSuppliers(),
        getValueProperties(),
        getProductPrice(),
        getMassprint(),
        getProductOrderSubitem(),
        getProductOrder(),
        getConversation(),
        getFonts()
    }));

    /**
     * Contains all formattable Contexts
     */
    public static ArrayList<Context> FORMATTABLE_CONTEXTS = new ArrayList<Context>(Arrays.asList(new Context[]{
        getContact(),
        getCustomer(),
        getManufacturer(),
        getSupplier(), 
        getProduct(),
        getExpense(), 
        getRevenue(), 
        getOffer(),
        getOrder(), 
        getInvoice(), 
        getDeposit(),
        getPartPayment(),
        getCredit(),
        getCompany(),
        getActivityList(), 
        getProductOrder()
    }));

//    private String[] searchHeaders;
    private volatile ArrayList<String[]> references = new ArrayList<>();
    private boolean exclusiveConditionsAvailable = false;
    private volatile String exclusiveCondition;
    private volatile String uniqueColumns;
    private volatile int id = -1;

    /**
     * Constructor now private
     */
    private Context() {
        this.setSearchFields(DEFAULT_SEARCH);
    }

    /**
     * Constructs a new Context and adds it to
     * allcontexts-collection.
     *
     * @param tablename The name of the table to use
     * @param targetObjectClass The class of the
     * {@link DatabaseObject} child to be used with this
     * Context
     * @param cacheable If true, the {@link DatabaseObject}s
     * related to this context will be cached
     * @param secured If true, the {@link DatabaseObject}s
     * related to this context will be protected by the
     * {@link MPSecurityManager}
     * @param importable If true, the
     * {@link DatabaseObject}s related to this context will
     * be importable by the {@link XMLReader}
     * @param lockable If true, the {@link DatabaseObject}s
     * related to this context will be loackable to avoid
     * concurrent access
     * @param groupable If true, the {@link DatabaseObject}s
     * related to this context will apply to a users group
     * restriction
     * @param trashable If true, the {@link DatabaseObject}s
     * related to this context will be moved to the trashbin
     * on delete
     * @param archivable If true, actions on the
     * {@link DatabaseObject}s related to this context will
     * be monitored
     */
    public Context(String tablename, Class targetObjectClass, boolean cacheable,
            boolean secured, boolean importable, boolean lockable, boolean groupable,
            boolean trashable, boolean archivable) {
        setId(allContexts.size() + 1);
        this.setSubID(DEFAULT_SUBID);
        this.setDbIdentity(tablename);
        this.setIdentityClass(targetObjectClass);
        if (archivable) {
            getArchivableContexts().add(this);
        }
        if (cacheable) {
            getCacheableContexts().add(this);
        }
        if (secured) {
            getSecuredContexts().add(this);
        }
        if (importable) {
            getImportableContexts().add(this);
        }
        if (lockable) {
            getLockableContexts().add(this);
        }
        if (groupable) {
            getGroupableContexts().add(this);
        }
        if (trashable) {
            getTrashableContexts().add(this);
        }
        if (archivable) {
            getArchivableContexts().add(this);
        }

        allContexts.add(this);
    }

    /**
     * Constructs a new Context with a random ID
     *
     * @param tablename The name of the table to use
     * @param targetObjectClass The class of the
     * {@link DatabaseObject} child to be used with this
     * Context
     */
    public Context(String tablename, Class targetObjectClass) {
        setId(Integer.valueOf(RandomText.getNumberText()));
        this.setSubID(DEFAULT_SUBID);
        this.setDbIdentity(tablename);
        this.setIdentityClass(targetObjectClass);
    }

    /**
     * Constructs a new Context with a random ID
     *
     * @param targetObjectClass The tablename will be
     * assumed from the Class' simple name
     */
    public Context(Class targetObjectClass) {
        this(targetObjectClass.getSimpleName(), targetObjectClass);
    }

    /**
     * Constructs a new Context with a random ID. The
     * tablename will be assumed from the targetObject's
     * Class simple name
     *
     * @param targetObject
     */
    public Context(DatabaseObject targetObject) {
        setId(Integer.valueOf(RandomText.getNumberText()));
        this.setSubID(DEFAULT_SUBID);
        this.setDbIdentity(targetObject.getClass().getSimpleName());
        this.setIdentityClass(targetObject.getClass());
    }
    /**
     * A default, empty Context without id
     */
    public static Context DEFAULT = new Context();
    private Class identityClass = null;
    /*
     * The DB Identity name - usually the table
     */
    private String dbIdentity = null;

    /*
     * The DB Sub Identity name - maybe a column name
     */
    private String subID = null;

    /*
     * The search default result columns
     */
    private String defResultFields = null;

    /**
     *
     * @return The unique constraints
     */
    public String getUniqueColumns() {
        return uniqueColumns;
    }

    /**
     *
     * @param customer
     * @param supplier
     * @param manufacturer
     * @param company
     */
    public void setContactConditions(boolean customer, boolean supplier, boolean manufacturer, boolean company) {
        setCustomer(customer);
        setSupplier(supplier);
        setManufacturer(manufacturer);
        setCompany(company);
    }

    /**
     * Set conditions to get exclusive data (e.g. customer =
     * false results in all data without any customer)
     *
     * @param customer
     * @param supplier
     * @param manufacturer
     * @param company
     */
    public void setExclusiveContactConditions(boolean customer, boolean supplier, boolean manufacturer, boolean company) {

        String cond = "    ";
        boolean first = true;

        if (customer) {
            if (first) {
                cond += "WHERE ";
            }
            first = false;
            cond += " " + CONDITION_CONTACTS_CUSTOMER + "=1 AND ";
        } else {
            if (first) {
                cond += "WHERE ";
            }
            first = false;
            cond += " " + CONDITION_CONTACTS_CUSTOMER + "=0 AND ";
        }
        if (supplier) {
            if (first) {
                cond += "WHERE ";
            }
            first = false;
            cond += " " + CONDITION_CONTACTS_SUPPLIER + "=1 AND ";
        } else {
            if (first) {
                cond += "WHERE ";
            }
            first = false;
            cond += " " + CONDITION_CONTACTS_SUPPLIER + "=0 AND ";
        }
        if (manufacturer) {
            if (first) {
                cond += "WHERE ";
            }
            first = false;
            cond += " " + CONDITION_CONTACTS_MANUFACTURER + "=1 AND ";
        } else {
            if (first) {
                cond += "WHERE ";
            }
            first = false;
            cond += " " + CONDITION_CONTACTS_MANUFACTURER + "=0 AND ";
        }
        if (company) {
            if (first) {
                cond += "WHERE ";
            }
            first = false;
            cond += " " + CONDITION_CONTACTS_COMPANY + "=1 AND ";
        } else {
            if (first) {
                cond += "WHERE ";
            }
            first = false;
            cond += " " + CONDITION_CONTACTS_COMPANY + "=0 AND ";
        }

        if (!first) {
            cond = cond.substring(4, cond.length() - 4);
            if (mpv5.db.objects.User.getCurrentUser().isGroupRestricted() && getGroupableContexts().contains(this)) {
                cond += "AND   (" + dbIdentity + "." + "GROUPSIDS = " + mpv5.db.objects.User.getCurrentUser().__getGroupsids() + " OR " + dbIdentity + "." + "GROUPSIDS = 1)";
            }
        } else {

            if (mpv5.db.objects.User.getCurrentUser().isGroupRestricted() && getGroupableContexts().contains(this)) {
                cond = "WHERE (" + dbIdentity + "." + "GROUPSIDS = " + mpv5.db.objects.User.getCurrentUser().__getGroupsids() + " OR " + dbIdentity + "." + "GROUPSIDS = 1)";
            } else {
                cond = "WHERE " + CONDITION_DEFAULT;
            }
        }

        if (getTrashableContexts().contains(this)) {
            cond += " AND " + getDbIdentity() + ".invisible = 0 ";
        }

        exclusiveCondition = cond;
    }

    /**
     *
     * @param withInvisible
     * @return DB condition string
     */
    public String getConditions(boolean withInvisible) {
        if (!exclusiveConditionsAvailable) {
            String cond = "    ";
            boolean first = true;
            if (isCompany()) {
                if (first) {
                    cond += "(";
                }
                first = false;
                cond += " " + CONDITION_CONTACTS_COMPANY + "=1 OR ";
            }
            if (isCustomer()) {
                if (first) {
                    cond += "(";
                }
                first = false;
                cond += " " + CONDITION_CONTACTS_CUSTOMER + "=1 OR ";
            }
            if (isManufacturer()) {
                if (first) {
                    cond += "(";
                }
                first = false;
                cond += " " + CONDITION_CONTACTS_MANUFACTURER + "=1 OR ";
            }
            if (isSupplier()) {
                if (first) {
                    cond += "(";
                }
                first = false;
                cond += " " + CONDITION_CONTACTS_SUPPLIER + "=1 OR ";
            }
            if (itemType != null) {
                if (first) {
                    cond += "(";

                }
                first = false;
                cond += " " + CONDITION_ITEMS_TYPE + "=" + getItemType() + " OR ";
            }
            if (itemStatus != null) {
                if (first) {
                    cond += "(";

                }
                first = false;
                cond += " " + CONDITION_ITEMS_STATUS + "=" + getItemStatus() + " OR ";
            }
            if (!first) {
                cond = "WHERE" + cond.substring(0, cond.length() - 3) + ")";
                if (mpv5.db.objects.User.getCurrentUser().isGroupRestricted() && getGroupableContexts().contains(this)) {
                    cond += " AND (" + dbIdentity + "." + "GROUPSIDS = " + mpv5.db.objects.User.getCurrentUser().__getGroupsids() + " OR " + dbIdentity + "." + "GROUPSIDS = 1)";
                }
            } else {
                if (mpv5.db.objects.User.getCurrentUser().isGroupRestricted() && getGroupableContexts().contains(this)) {
                    cond = "WHERE (" + dbIdentity + "." + "GROUPSIDS = " + mpv5.db.objects.User.getCurrentUser().__getGroupsids() + " OR " + dbIdentity + "." + "GROUPSIDS = 1)";
                } else {
                    cond = "WHERE " + CONDITION_DEFAULT;
                }
            }
            if (!withInvisible && getTrashableContexts().contains(this)) {
                cond += " AND " + dbIdentity + ".invisible = 0 ";
            }
//            if (!QueryHandler.isMatchingBraces(cond)) {
//                cond += ")";
//            }
            return cond;
        } else {
            return exclusiveCondition;
        }
    }

    /**
     * Generates a SQL String (WHERE clause) which can be
     * used to implement multi-client capability.
     * 
     * <b>If the current Context does not support grouping,
     * or the current user is not Group restricted, this
     * will return NULL.</b>
     *
     * @return
     */
    public synchronized String getGroupRestrictionSQLString() {
        if (mpv5.db.objects.User.getCurrentUser().isGroupRestricted() && getGroupableContexts().contains(this)) {
            return " (" + dbIdentity + "." + "GROUPSIDS = " + mpv5.db.objects.User.getCurrentUser().__getGroupsids() + " OR " + dbIdentity + "." + "GROUPSIDS = 1)";
        } else {
            return null;
        }
    }

    /**
     * Generates a SQL String (WHERE clause) which can be
     * used to avoid having already trashed elements in the
     * resulting data.
     *
     * @return " invisible = 0 " or NULL if Context is not
     * trashable
     */
    public synchronized String getNoTrashSQLString() {
        if (getTrashableContexts().contains(this)) {
            return " " + dbIdentity + ".invisible = 0 ";
        } else {
            return "";
        }
    }

    /**
     * Generates a SQL String (WHERE clause) which can be
     * used to implement multi-client capability.
     * 
     * <b>If the current Context does not support grouping,
     * or the current user is not Group restricted, this
     * will return NULL.</b>
     *
     * @param tableName
     * @return
     */
    public synchronized String getGroupRestrictionSQLString(String tableName) {
        if (mpv5.db.objects.User.getCurrentUser().isGroupRestricted() && getGroupableContexts().contains(this)) {
            return " (" + tableName + "." + "GROUPSIDS = " + mpv5.db.objects.User.getCurrentUser().__getGroupsids() + " OR " + tableName + "." + "GROUPSIDS = 1)";
        } else {
            return null;
        }
    }

    /**
     * Generates a SQL String (WHERE clause) which can be
     * used to avoid having already trashed elements in the
     * resulting data.
     *
     * @param tableName
     * @return " invisible = 0 " or NULL if Context is not
     * trashable
     */
    public synchronized String getNoTrashSQLString(String tableName) {
        if (getTrashableContexts().contains(this)) {
            return " " + tableName + ".invisible = 0 ";
        } else {
            return null;
        }
    }

    /**
     * Add MP specific conditions to a sql query
     *
     * @param query
     * @return The query
     */
    public synchronized String prepareSQLString(String query) {

        if (getGroupRestrictionSQLString() != null) {
            if (query.toUpperCase().contains("WHERE")) {
                query = query + " AND" + getGroupRestrictionSQLString();
            } else {
                query = query + " WHERE" + getGroupRestrictionSQLString();
            }
        }
        if (getNoTrashSQLString() != null) {
            if (query.toUpperCase().contains("WHERE")) {
                query = query + " AND" + getNoTrashSQLString();
            } else {
                query = query + " WHERE" + getNoTrashSQLString();
            }
        }
        return query;
    }

    /**
     * Add MP specific conditions to a sql query
     *
     * @param query
     * @param tableName
     * @return The query
     */
    public synchronized String prepareSQLString(String query, String tableName) {

        if (getGroupRestrictionSQLString(tableName) != null) {
            if (query.toUpperCase().contains("WHERE")) {
                query = query + " AND" + getGroupRestrictionSQLString(tableName);
            } else {
                query = query + " WHERE" + getGroupRestrictionSQLString(tableName);
            }
        }
        if (getNoTrashSQLString(tableName) != null) {
            if (query.toUpperCase().contains("WHERE")) {
                query = query + " AND" + getNoTrashSQLString(tableName);
            } else {
                query = query + " WHERE" + getNoTrashSQLString(tableName);
            }
        }
        return query;
    }

//    /**
//     * Define the owner of this Context
//     * @param parentobject
//     */
//    public void setOwner(DatabaseObject parentobject) {
//        this.parent = parentobject;
//    }
    /**
     * Remove all exclusive conditions or reuse them
     *
     * @param bool
     */
    public void useExclusiveConditions(boolean bool) {
        exclusiveConditionsAvailable = bool;
    }

//    /**
//     * Add a self-table reference to this context
//     * @param referencekey
//     * @param referenceidkey
//     */
//    public void addReference(String referencekey, String referenceidkey) {
//        String alias = this.getDbIdentity();
//        references.add(new String[]{this.getDbIdentity(), referencekey, referenceidkey, alias});
//    }
    /**
     * Add a foreign table reference to this context
     *
     * @param c
     */
    public void addReference(Context c) {
        if (!refs.contains(c)) {
            String alias = c.getDbIdentity();
            refs.add(c);
            references.add(new String[]{alias, "ids", alias + "ids", alias, this.getDbIdentity()});
        }
    }
    List<Context> refs = new ArrayList<>();

    /**
     * Add a foreign table reference to this
     * context
     * Context c= Context.getFilesToContacts();
     * c.addReference(Context.getFiles().getDbIdentity(),
     * "cname", "filename");
     *
     * @param referencetable The table which will be joined
     * @param referencekey The key column of the joined
     * table
     * @param referenceidkey They key column in the original
     * table
     */
    public void addReference(String referencetable, String referencekey, String referenceidkey) {
        synchronized (this) {
            String alias = referencetable;
            references.add(new String[]{referencetable, referencekey, referenceidkey, alias, getDbIdentity()});
        }
    }

    /**
     * @return the dbIdentity
     */
    public String getDbIdentity() {
        return dbIdentity;
    }

    public String getSearchFields() {
        return defResultFields;
    }
//
//    public String[] getSearchHeaders() {
//        return searchHeaders;
//    }
//
//    public DatabaseObject getParent() {
//        return parent;
//    }

    /**
     * @param dbIdentity the dbIdentity to set
     */
    public final void setDbIdentity(String dbIdentity) {
        this.dbIdentity = dbIdentity;
    }

    /**
     * @return the subID
     */
    public String getSubID() {
        return subID;
    }

    /**
     * @return the IDENTITY CLASS
     */
    public Class getIdentityClass() {
        return identityClass;
    }

    /**
     * @param subID the subID to set
     */
    public final void setSubID(String subID) {
        this.subID = subID;
    }

    public String getReferences() {
        String cond = "";
        if (references.size() > 0) {
            for (int i = 0; i < references.size(); i++) {
                if (references.get(i).length == 4) {
                    cond += " LEFT OUTER JOIN " + references.get(i)[0] + " ON " + references.get(i)[3] + "." + references.get(i)[1] + " = " + references.get(i)[3] + "." + references.get(i)[2];
                } else if (references.get(i).length == 5) {
                    cond += " LEFT OUTER JOIN " + references.get(i)[0] + " ON " + references.get(i)[3] + "." + references.get(i)[1] + " = " + references.get(i)[4] + "." + references.get(i)[2];
                }
            }
        }
        return cond;
    }

    /**
     * @return the Company
     */
    public boolean isCompany() {
        return isCompany;
    }

    /**
     * @param Company the Company to set
     */
    private void setCompany(boolean Company) {
        this.isCompany = Company;
    }

    /**
     * @return the Customer
     */
    private boolean isCustomer() {
        return isCustomer;
    }

    /**
     * @param Customer the Customer to set
     */
    private void setCustomer(boolean Customer) {
        this.isCustomer = Customer;
    }

    /**
     * @return the Manufacturer
     */
    private boolean isManufacturer() {
        return isManufacturer;
    }

    private void setId(int id) {
        this.id = id;
    }

    /**
     * @param Manufacturer the Manufacturer to set
     */
    private void setManufacturer(boolean Manufacturer) {
        this.isManufacturer = Manufacturer;
    }

    /**
     * @return the Supplier
     */
    private boolean isSupplier() {
        return isSupplier;
    }

    /**
     * @param Supplier the Supplier to set
     */
    private void setSupplier(boolean Supplier) {
        this.isSupplier = Supplier;
    }

    /**
     * @return the itemStatus
     */
    public int getItemStatus() {
        return itemStatus;
    }

    /**
     * @param itemStatus the itemStatus to set
     */
    public void setItemStatus(int itemStatus) {
        this.itemStatus = itemStatus;
    }

    /**
     * @return the itemType
     */
    public Integer getItemType() {
        return itemType;
    }

    /**
     * @param itemType the itemType to set
     */
    public void setItemType(int itemType) {
        this.itemType = itemType;
    }

    /**
     * Returns an empty "sample" Object of the context type
     *
     * @param <T>
     * @return An empty {@link DatabaseObject}
     */
    @SuppressWarnings("unchecked")
    public <T extends DatabaseObject> T getNewObject() {
        return (T) DatabaseObject.getObject(this);
    }
////////////////////////////////////////////////////////////////////////////////
//generate a new Context Object on each call here, callers may alter them to variants

    public static final Context getContactsCompanies() {
        Context c = new Context();
        c.setCompany(true);
        c.setSubID(DEFAULT_SUBID);
        c.setDbIdentity(IDENTITY_CONTACTS);
        c.setSearchFields(DEFAULT_CONTACT_SEARCH);
//        c.setSearchHeaders(Headers.CONTACT_DEFAULT.getValue());
        c.setIdentityClass(IDENTITY_CONTACTS_CLASS);
        c.setId(0);

        return c;
    }

    public static final Context getSubItem() {
        Context subitem = new Context();
        subitem.setSubID(DEFAULT_SUBID);
        subitem.setDbIdentity(IDENTITY_SUBITEMS);
        subitem.setIdentityClass(IDENTITY_SUBITEMS_CLASS);
        subitem.setId(2);

        return subitem;
    }

    public static final Context getCustomer() {
        Context c = new Context();
        c.setCustomer(true);
        c.setSubID(DEFAULT_SUBID);
        c.setDbIdentity(IDENTITY_CONTACTS);
        c.setSearchFields(DEFAULT_CONTACT_SEARCH);
//        c.setSearchHeaders(Headers.CONTACT_DEFAULT.getValue());
        c.setIdentityClass(IDENTITY_CONTACTS_CLASS);
        c.setId(3);

        return c;
    }

    public static final Context getManufacturer() {
        Context c = new Context();
        c.setManufacturer(true);
        c.setSubID(DEFAULT_SUBID);
        c.setDbIdentity(IDENTITY_CONTACTS);
        c.setSearchFields(DEFAULT_CONTACT_SEARCH);
//        c.setSearchHeaders(Headers.CONTACT_DEFAULT.getValue());
        c.setIdentityClass(IDENTITY_CONTACTS_CLASS);
        c.setId(4);

        return c;
    }

    public static final Context getSupplier() {
        Context c = new Context();
        c.setSupplier(true);
        c.setSubID(DEFAULT_SUBID);
        c.setDbIdentity(IDENTITY_CONTACTS);
        c.setSearchFields(DEFAULT_CONTACT_SEARCH);
//        c.setSearchHeaders(Headers.CONTACT_DEFAULT.getValue());
        c.setIdentityClass(IDENTITY_CONTACTS_CLASS);
        c.setId(5);

        return c;
    }

    public static final Context getContact() {
        Context c = new Context();
        c.setSubID(DEFAULT_SUBID);
        c.setDbIdentity(IDENTITY_CONTACTS);
        c.setSearchFields(DEFAULT_CONTACT_SEARCH);
//        c.setSearchHeaders(Headers.CONTACT_DEFAULT.getValue());
        c.setIdentityClass(IDENTITY_CONTACTS_CLASS);
        c.setId(6);

        return c;
    }

    public static final Context getUser() {
        Context c = new Context();
        c.setSubID(DEFAULT_SUBID);
        c.setDbIdentity(IDENTITY_USERS);
        c.setSearchFields(DEFAULT_USER_SEARCH);
//        c.setSearchHeaders(Headers.USER_DEFAULT.getValue());
        c.setIdentityClass(IDENTITY_USERS_CLASS);
        c.uniqueColumns = UNIQUECOLUMNS_USER;
        c.setId(7);

        return c;
    }

    public static final Context getSchedule() {
        Context c = new Context();
        c.setSubID(DEFAULT_SUBID);
        c.setDbIdentity(IDENTITY_SCHEDULE);
        c.setIdentityClass(IDENTITY_SCHEDULE_CLASS);
        c.setId(8);

        return c;
    }

    public static final Context getLanguage() {
        Context c = new Context();
        c.setSubID(DEFAULT_SUBID);
        c.setDbIdentity(IDENTITY_LANGUAGES);
        c.setId(9);

        return c;
    }

    public static final Context getFiles() {
        Context c = new Context();
        c.setSubID(DEFAULT_SUBID);
        c.setDbIdentity(IDENTITY_FILES);
        c.setId(10);

        return c;
    }

    public static final Context getLock() {
        Context c = new Context();
        c.setSubID(DEFAULT_SUBID);
        c.setDbIdentity(IDENTITY_LOCK);
        c.setId(11);

        return c;
    }

    public static final Context getGroup() {
        Context c = new Context();
        c.setSubID(DEFAULT_SUBID);
        c.setDbIdentity(IDENTITY_GROUPS);
        c.setIdentityClass(IDENTITY_GROUPS_CLASS);
        c.uniqueColumns = UNIQUECOLUMNS_GROUPS;
        c.setId(14);

        return c;
    }

    public static final Context getFavourite() {
        Context c = new Context();
        c.setSubID(DEFAULT_SUBID);
        c.setDbIdentity(IDENTITY_FAVS);
        c.setIdentityClass(IDENTITY_FAVOURITE_CLASS);
        c.setId(15);

        return c;
    }

    public static final Context getAddress() {
        Context c = new Context();
        c.setSubID(DEFAULT_SUBID);
        c.setDbIdentity(IDENTITY_ADDRESS);
        c.setIdentityClass(IDENTITY_ADDRESS_CLASS);
        c.setId(16);

        return c;
    }

    public static final Context getInvoice() {
        Context c = new Context();
        c.setSubID(DEFAULT_SUBID);
        c.setDbIdentity(IDENTITY_ITEMS);
        c.setSearchFields(DEFAULT_ITEM_SEARCH);
        c.setIdentityClass(IDENTITY_ITEMS_CLASS);
        c.setItemType(Item.TYPE_INVOICE);
        c.setId(1);

        return c;
    }

    public static final Context getOrder() {
        Context c = new Context();
        c.setSubID(DEFAULT_SUBID);
        c.setDbIdentity(IDENTITY_ITEMS);
        c.setSearchFields(DEFAULT_ITEM_SEARCH);
        c.setIdentityClass(IDENTITY_ITEMS_CLASS);
        c.setItemType(Item.TYPE_ORDER);
        c.setId(18);

        return c;
    }

    public static final Context getConfirmation() {
        Context c = new Context();
        c.setSubID(DEFAULT_SUBID);
        c.setDbIdentity(IDENTITY_ITEMS);
        c.setSearchFields(DEFAULT_ITEM_SEARCH);
        c.setIdentityClass(IDENTITY_ITEMS_CLASS);
        c.setItemType(Item.TYPE_ORDER_CONFIRMATION);
        c.setId(1);

        return c;
    }
        
    public static final Context getOffer() {
        Context c = new Context();
        c.setSubID(DEFAULT_SUBID);
        c.setDbIdentity(IDENTITY_ITEMS);
        c.setSearchFields(DEFAULT_ITEM_SEARCH);
        c.setIdentityClass(IDENTITY_ITEMS_CLASS);
        c.setItemType(Item.TYPE_OFFER);
        c.setId(19);

        return c;
    }

    public static final Context getDelivery() {
        Context c = new Context();
        c.setSubID(DEFAULT_SUBID);
        c.setDbIdentity(IDENTITY_ITEMS);
        c.setSearchFields(DEFAULT_ITEM_SEARCH);
        c.setIdentityClass(IDENTITY_ITEMS_CLASS);
        c.setItemType(Item.TYPE_DELIVERY_NOTE);
        c.setId(1);

        return c;
    }
    
    public static final Context getFilesToContacts() {
        Context c = new Context();
        c.setSubID(DEFAULT_SUBID);
        c.setDbIdentity(IDENTITY_FILES_TO_CONTACTS);
        c.setIdentityClass(IDENTITY_CONTACTS_FILES_CLASS);
        c.setId(20);

        return c;
    }

    public static final Context getHistory() {
        Context c = new Context();
        c.setSubID(DEFAULT_SUBID);
        c.setDbIdentity(IDENTITY_HISTORY);
        c.setIdentityClass(IDENTITY_HISTORY_ITEMS_CLASS);
        c.setId(21);

        return c;
    }

    public static final Context getCountries() {
        Context c = new Context();
        c.setSubID(DEFAULT_SUBID);
        c.setDbIdentity(IDENTITY_COUNTRIES);
        c.setId(22);

        return c;
    }

    public static final Context getProduct() {
        Context c = new Context();
        c.setSubID(DEFAULT_SUBID);
        c.setDbIdentity(IDENTITY_PRODUCTS);
        c.setIdentityClass(IDENTITY_PRODUCTS_CLASS);
        c.setSearchFields(DEFAULT_PRODUCT_SEARCH);
        c.setId(23);
        FOREIGN_KEY_ALIASES.put(c + "@" + getSubItem(), "originalproducts");

        return c;
    }

    public static final Context getSearchIndex() {
        Context c = new Context();
        c.setSubID(DEFAULT_SUBID);
        c.setDbIdentity(IDENTITY_SEARCHINDEX);
        c.setId(24);

        return c;
    }

    public static final Context getPluginsToUsers() {
        Context c = new Context();
        c.setSubID(DEFAULT_SUBID);
        c.setDbIdentity(IDENTITY_PLUGINS_TO_USERS);
        c.setIdentityClass(IDENTITY_USER_PLUGINS_CLASS);
        c.setId(25);

        return c;
    }

    public static final Context getPlugins() {
        Context c = new Context();
        c.setSubID(DEFAULT_SUBID);
        c.setDbIdentity(IDENTITY_PLUGINS);
        c.setIdentityClass(IDENTITY_PLUGINS_CLASS);
        c.setId(26);

        return c;
    }

    public static final Context getUserProperties() {
        Context c = new Context();
        c.setSubID(DEFAULT_SUBID);
        c.setDbIdentity(IDENTITY_PROPERTIES_TO_USERS);
        c.setIdentityClass(IDENTITY_PROPERTIES_CLASS);
        c.setId(27);

        return c;
    }

    public static final Context getAccounts() {
        Context c = new Context();
        c.setSubID(DEFAULT_SUBID);
        c.setDbIdentity(IDENTITY_ACCOUNTS);
        c.setIdentityClass(IDENTITY_ACCOUNTS_CLASS);
        c.setId(28);
        c.uniqueColumns = UNIQUECOLUMNS_DEFAULT;

        return c;
    }

    public static final Context getItemsToAccounts() {
        Context c = new Context();
        c.setSubID(DEFAULT_SUBID);
        c.setDbIdentity(IDENTITY_ITEMS_TO_ACCOUNTS);
        c.setId(29);

        return c;
    }

    public static final Context getMessage() {
        Context c = new Context();
        c.setSubID(DEFAULT_SUBID);
        c.setDbIdentity(IDENTITY_MAIL);
        c.setIdentityClass(IDENTITY_MAILS_CLASS);
        c.setId(30);
        c.uniqueColumns = UNIQUECOLUMNS_DEFAULT;

        return c;
    }

    public static final Context getProductListItems() {
        Context c = new Context();
        c.setSubID(DEFAULT_SUBID);
        c.setDbIdentity(IDENTITY_PRODUCTSLISTITEMS);
        c.setIdentityClass(IDENTITY_ITEMSLIST_CLASS);
        c.setId(32);

        return c;
    }

    public static final Context getFormats() {
        Context c = new Context();
        c.setSubID(DEFAULT_SUBID);
        c.setDbIdentity(IDENTITY_FORMATS_T_USERS);
        c.setId(33);

        return c;
    }

    public static final Context getFilesToItems() {
        Context c = new Context();
        c.setSubID(DEFAULT_SUBID);
        c.setDbIdentity(IDENTITY_FILES_TO_ITEMS);
        c.setIdentityClass(IDENTITY_ITEM_FILES_CLASS);
        c.setId(34);

        return c;
    }

    public static final Context getTaxes() {
        Context c = new Context();
        c.setSubID(DEFAULT_SUBID);
        c.setDbIdentity(IDENTITY_TAX);
        c.setIdentityClass(IDENTITY_TAX_CLASS);
        c.setId(36);

        return c;
    }

    public static final Context getGlobalSettings() {
        Context c = new Context();
        c.setSubID(DEFAULT_SUBID);
        c.setDbIdentity(IDENTITY_GLOBALSETTINGS);
        c.setId(37);

        return c;
    }

    public static final Context getProductGroup() {
        Context c = new Context();
        c.setSubID(DEFAULT_SUBID);
        c.setDbIdentity(IDENTITY_PGROUPS);
        c.setIdentityClass(IDENTITY_PGROUPS_CLASS);
        c.uniqueColumns = UNIQUECOLUMNS_GROUPS;
        c.setId(38);

        return c;
    }

    public static final Context getFilesToProducts() {
        Context c = new Context();
        c.setSubID(DEFAULT_SUBID);
        c.setDbIdentity(IDENTITY_FILES_TO_PRODUCTS);
        c.setIdentityClass(IDENTITY_PRODUCTS_FILES_CLASS);
        c.setId(20);

        return c;
    }

    public static final Context getCompany() {
        Context c = new Context();
        c.setSubID(DEFAULT_SUBID);
        c.setDbIdentity(IDENTITY_COMPANIES);
        c.setIdentityClass(IDENTITY_COMPANY_CLASS);
        c.setId(39);

        return c;
    }

    public static final Context getWebShop() {
        Context c = new Context();
        c.setSubID(DEFAULT_SUBID);
        c.setDbIdentity(IDENTITY_WEBSHOPS);
        c.setIdentityClass(IDENTITY_WEBSHOP_CLASS);
        c.setId(40);

        return c;
    }

    public static final Context getWebShopContactMapping() {
        Context c = new Context();
        c.setSubID(DEFAULT_SUBID);
        c.setDbIdentity(IDENTITY_WSMAPPING);
        c.setIdentityClass(IDENTITY_WSCONTACTSMAPPING_CLASS);
        c.setId(41);

        return c;
    }

    public static final Context getWebShopItemMapping() {
        Context c = new Context();
        c.setSubID(DEFAULT_SUBID);
        c.setDbIdentity(IDENTITY_WSIMAPPING);
        c.setIdentityClass(IDENTITY_WSITEMSMAPPING_CLASS);
        c.setId(42);

        return c;
    }

    public static final Context getTemplate() {
        Context c = new Context();
        c.setSubID(DEFAULT_SUBID);
        c.setDbIdentity(IDENTITY_TEMPLATES);
        c.setIdentityClass(IDENTITY_TEMPLATE_CLASS);
        c.setId(43);

        return c;
    }

    public static final Context getTemplatesToUsers() {
        Context c = new Context();
        c.setSubID(DEFAULT_SUBID);
        c.setDbIdentity(IDENTITY_TEMPLATES_TO_USERS);
        c.setId(44);

        return c;
    }

    public static final Context getReminder() {
        Context c = new Context();
        c.setSubID(DEFAULT_SUBID);
        c.setDbIdentity(IDENTITY_REMINDERS);
        c.setIdentityClass(IDENTITY_REMINDER_CLASS);
        c.setId(45);

        return c;
    }

    public static final Context getStage() {
        Context c = new Context();
        c.setSubID(DEFAULT_SUBID);
        c.setDbIdentity(IDENTITY_STAGES);
        c.setIdentityClass(IDENTITY_STAGE_CLASS);
        c.setId(46);

        return c;
    }

    public static final Context getRevenue() {
        Context c = new Context();
        c.setSubID(DEFAULT_SUBID);
        c.setDbIdentity(IDENTITY_REVENUE);
        c.setIdentityClass(Revenue.class);
        c.setId(47);

        return c;
    }

    public static final Context getExpense() {
        Context c = new Context();
        c.setSubID(DEFAULT_SUBID);
        c.setDbIdentity(IDENTITY_EXPENSE);
        c.setIdentityClass(IDENTITY_EXPENSE_CLASS);
        c.setId(48);

        return c;
    }

    public static final Context getProductList() {
        Context c = new Context();
        c.setSubID(DEFAULT_SUBID);
        c.setDbIdentity(IDENTITY_PRODUCTSLIST);
        c.setSearchFields(DEFAULT_PRODUCTSLIST_SEARCH);
        c.setIdentityClass(IDENTITY_PRODUCTLIST_CLASS);
        c.setId(48);

        return c;
    }

    public static final Context getProductsToSuppliers() {
        Context c = new Context();
        c.setSubID(DEFAULT_SUBID);
        c.setDbIdentity(IDENTITY_PRODUCTS_TO_SUPPLIERS);
        c.setIdentityClass(IDENTITY_PRODUCTSTOSUPPLIERS_CLASS);
        c.setId(49);

        return c;
    }

    public static final Context getValueProperties() {
        Context c = new Context();
        c.setSubID(DEFAULT_SUBID);
        c.setDbIdentity(IDENTITY_VALUE_PROPERTIES);
        c.setIdentityClass(IDENTITY_VALUEPROPERTY_CLASS);
        c.setId(50);

        return c;
    }

    public static final Context getScheduleTypes() {
        Context c = new Context();
        c.setSubID(DEFAULT_SUBID);
        c.setDbIdentity(IDENTITY_SCHEDULE_TYPES);
        c.setIdentityClass(IDENTITY_SCHEDULETYPES_CLASS);
        c.setId(51);

        return c;
    }

    public static final Context getConversation() {
        Context c = new Context();
        c.setSubID(DEFAULT_SUBID);
        c.setDbIdentity(IDENTITY_CONVERSATION);
        c.setIdentityClass(IDENTITY_CONVERSATION_CLASS);
        c.setSearchFields(DEFAULT_SUBID);
        c.setId(52);

        return c;
    }

    public static final Context getProductPrice() {
        Context c = new Context();
        c.setSubID(DEFAULT_SUBID);
        c.setDbIdentity(IDENTITY_PRODUCT_PRICES);
        c.setIdentityClass(IDENTITY_PRODUCTPRICE_CLASS);
        c.setId(53);

        return c;
    }

    public static final Context getActivityList() {
        Context c = new Context();
        c.setSubID(DEFAULT_SUBID);
        c.setDbIdentity(IDENTITY_ACTIVITYLIST);
        c.setSearchFields(DEFAULT_ACTIVITYLIST_SEARCH);
        c.setIdentityClass(IDENTITY_ACTIVITYLIST_CLASS);
        c.setId(54);

        return c;
    }

    public static final Context getActivityListItems() {
        Context c = new Context();
        c.setSubID(DEFAULT_SUBID);
        c.setDbIdentity(IDENTITY_ACTIVITYLISTITEMS);
        c.setIdentityClass(IDENTITY_ACTIVITYITEMSLIST_CLASS);
        c.setId(55);

        return c;
    }

    public static final Context getMassprint() {
        Context c = new Context();
        c.setSubID(DEFAULT_SUBID);
        c.setDbIdentity(IDENTITY_MASSPRINT);
        c.setIdentityClass(IDENTITY_MASSPRINT_CLASS);
        c.setId(56);

        return c;
    }

    public static final Context getProductOrder() {
        Context c = new Context();
        c.setSubID(DEFAULT_SUBID);
        c.setDbIdentity(IDENTITY_PRODUCTORDER);
        c.setIdentityClass(IDENTITY_PRODUCTORDER_CLASS);
        c.setId(57);

        return c;
    }

    public static final Context getProductOrderSubitem() {
        Context c = new Context();
        c.setSubID(DEFAULT_SUBID);
        c.setDbIdentity(IDENTITY_PRODUCTORDERSUBITEM);
        c.setIdentityClass(IDENTITY_PRODUCTORDERSUBITEM_CLASS);
        c.setId(58);

        return c;
    }

    public static final Context getFonts() {
        Context c = new Context();
        c.setSubID(DEFAULT_SUBID);
        c.setDbIdentity(IDENTITY_FONTS);
        c.setIdentityClass(IDENTITY_FONTS_CLASS);
        c.setId(59);

        return c;
    }
    
    public static final Context getCredit() {
        Context c = new Context();
        c.setSubID(DEFAULT_SUBID);
        c.setDbIdentity(IDENTITY_ITEMS);
        c.setSearchFields(DEFAULT_ITEM_SEARCH);
        c.setIdentityClass(IDENTITY_ITEMS_CLASS);
        c.setItemType(Item.TYPE_CREDIT);
        c.setId(60);

        return c;
    }

    public static final Context getPartPayment() {
        Context c = new Context();
        c.setSubID(DEFAULT_SUBID);
        c.setDbIdentity(IDENTITY_ITEMS);
        c.setSearchFields(DEFAULT_ITEM_SEARCH);
        c.setIdentityClass(IDENTITY_ITEMS_CLASS);
        c.setItemType(Item.TYPE_PART_PAYMENT);
        c.setId(61);

        return c;
    }

    public static final Context getDeposit() {
        Context c = new Context();
        c.setSubID(DEFAULT_SUBID);
        c.setDbIdentity(IDENTITY_ITEMS);
        c.setSearchFields(DEFAULT_ITEM_SEARCH);
        c.setIdentityClass(IDENTITY_ITEMS_CLASS);
        c.setItemType(Item.TYPE_DEPOSIT);
        c.setId(62);

        return c;
    }


    /////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     *
     * @param contextdbidentity
     * @return The matching context or null if not existing
     */
    public static Context getMatchingContext(String contextdbidentity) {
        if (contextdbidentity.toLowerCase().endsWith("ids")) {
            contextdbidentity = contextdbidentity.replace("ids", "");
        }
        for (int i = 0; i < allContexts.size(); i++) {
            Context context = allContexts.get(i);
            if (context.getDbIdentity().equalsIgnoreCase(contextdbidentity)) {
                return context;
            }
        }
        Log.Debug(Context.class, "Context not found for name: " + contextdbidentity);
        return null;
    }

    /**
     *
     * @param contextId
     * @return The matching context or null if not existing
     */
    public static Context getMatchingContext(int contextId) {
        for (int i = 0; i < allContexts.size(); i++) {
            Context context = allContexts.get(i);
            if (context.getId() == contextId) {
                return context;
            }
        }
        Log.Debug(Context.class, "Context not found for id: " + contextId);
        return null;
    }

    public void setSearchFields(String fields) {
        defResultFields = fields;
    }

    /**
     * @param identityClass the identityClass to set
     */
    public final void setIdentityClass(Class identityClass) {
        this.identityClass = identityClass;
    }

    @Override
    public String toString() {
        if (true/*getIdentityClass() == null*/) {
            return String.valueOf(dbIdentity).toUpperCase() + " [" + id + "]";
        } else {
            return "<html>" + String.valueOf(dbIdentity).toUpperCase() + " [" + id + "] (<b>" + getIdentityClass().getSimpleName() + "</b>)</html>";
        }
    }

    /**
     * Looks for a Context with the specified id
     *
     * @param value
     * @return A Context or null if no matching Context was
     * found
     */
    public static synchronized Context getByID(int value) {
        Log.Debug(Context.class, "getByID: " + value);
        for (int i = 0; i < allContexts.size(); i++) {
            Context context = allContexts.get(i);
            if (context.getId() == value) {
                Log.Debug(Context.class, "getByID-> " + context);
                return context;
            }
        }
        throw new IllegalArgumentException("Not a known Context id:" + value);
    }

    /**
     * @return the id
     */
    public int getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof Context)) {
            return false;
        }
        return Integer.valueOf(getId()).equals(((Context) o).getId());
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 47 * hash + this.id;
        hash = 47 * hash + (this.dbIdentity != null ? this.dbIdentity.hashCode() : 0);
        return hash;
    }

    @Override
    public int compareTo(Context t) {
        return Integer.compare(this.hashCode(), t.hashCode());
    }
}
