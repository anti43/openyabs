package enoa.handler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;
import javax.swing.JButton;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.db.common.NodataFoundException;
import mpv5.db.common.QueryHandler;
import mpv5.db.common.ReturnValue;
import mpv5.db.objects.*;
import mpv5.globals.LocalSettings;
import mpv5.globals.Messages;
import mpv5.handler.MPEnum;
import mpv5.logging.Log;
import mpv5.ui.frames.MPView;
import mpv5.usermanagement.MPSecurityManager;
import mpv5.utils.export.ODTFile;
import mpv5.utils.export.PDFFile;

/**
 * This class provides {@link Template} loading and caching functionality, thread-safe
 */
public class TemplateHandler {

    /**
     * Return true if the Template for the currently logged in user, with the given type, and matching the targets group is loaded
     * @param target
     * @param type
     * @return
     */
    public static synchronized boolean isLoaded(DatabaseObject target, int type) {
        String key = null;
        if (target != null) {
            key = mpv5.db.objects.User.getCurrentUser() + "@" + type + "@" + target.__getGroupsids();
        } else {
            key = mpv5.db.objects.User.getCurrentUser() + "@" + type + "@" + 1;
        }

        if (templateCache.containsKey(key)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Loads a template including neccesary files
     * @param target
     * @param typ 
     * @return
     */
    public static synchronized Template loadTemplate(DatabaseObject target, int typ) {
        Integer type = new Integer(typ);
        int groupsids = 0;
        if (target != null) {
            groupsids = target.__getGroupsids();
        } else {
            groupsids = 1;
        }
        String key = mpv5.db.objects.User.getCurrentUser() + "@" + type + "@" + groupsids;
        if (templateCache.containsKey(key)) {
            return templateCache.get(key);
        } else {

            if (type != null) {
                ReturnValue data = QueryHandler.getConnection().freeQuery(
                        "SELECT templatesids FROM templatestousers  LEFT OUTER JOIN templates AS templates0 ON "
                        + "templates0.ids = templatestousers.templatesids WHERE templatestousers.usersids="
                        + mpv5.db.objects.User.getCurrentUser().__getIDS()
                        + " AND "
                        + "templates0.mimetype='" + type
                        + "' AND templatestousers.IDS>0 "
                        + "AND templates0.groupsids =" + groupsids, MPSecurityManager.VIEW, null);
                if (!data.hasData()) {
                    data = QueryHandler.getConnection().freeQuery(
                            "SELECT templatesids FROM templatestousers  LEFT OUTER JOIN templates AS templates0 ON "
                            + "templates0.ids = templatestousers.templatesids WHERE templatestousers.usersids="
                            + mpv5.db.objects.User.getCurrentUser().__getIDS()
                            + " AND "
                            + "templates0.mimetype='" + type
                            + "' AND templatestousers.IDS>0 "
                            + "AND templates0.groupsids = 1", MPSecurityManager.VIEW, null);
                }

                Template preloadedTemplate = null;
                if (data.hasData()) {
                    try {
                        preloadedTemplate = (Template) DatabaseObject.getObject(Context.getTemplate(), Integer.valueOf(data.getData()[data.getData().length - 1][0].toString()));
                        if (preloadedTemplate.getFile().getName().endsWith("odt")) {
                            if (LocalSettings.getBooleanProperty(LocalSettings.OFFICE_USE)) {
                                preloadedTemplate.defineExFile(new ODTFile(preloadedTemplate.getFile().getPath()));
                                Log.Debug(Template.class, "Loaded template: " + preloadedTemplate);
                                MPView.addMessage(preloadedTemplate + Messages.LOADED.toString());
                            } else {
//                                Popup.notice(Messages.NOT_POSSIBLE + "\n" + Messages.OOCONNERROR);
                                return null;
                            }
                        } else {
                            preloadedTemplate.defineExFile(new PDFFile(preloadedTemplate.getFile().getPath()));
                        }
                        templateCache.put(key, preloadedTemplate);
                    } catch (NodataFoundException ex) {
                        Log.Debug(Template.class, "Invalid template: " + data.getData()[data.getData().length - 1][0].toString());
                        return null;
                    }
                } else {
                    try {
                        if (!(notification.containsKey(type.toString()) && notification.get(type.toString()).equals(Group.getObject(Context.getGroup(), groupsids)))) {
                            MPView.addMessage(Messages.OO_NO_TEMPLATE + ": " + TemplateHandler.getName(type) + " [" + mpv5.db.objects.User.getCurrentUser() + "] [" + Group.getObject(Context.getGroup(), groupsids) + "]");
                            Log.Debug(Template.class, "No template found for type: " + type + " for user: " + mpv5.db.objects.User.getCurrentUser() + " in GROUP " + Group.getObject(Context.getGroup(), groupsids));
                            notification.put(type.toString(), (Group) Group.getObject(Context.getGroup(), groupsids));
                        }

                    } catch (NodataFoundException nodataFoundException) {
                        Log.Debug(Template.class, nodataFoundException.getMessage());
                    }
                }
                return preloadedTemplate;
            } else {
                return null;
            }
        }
    }

    /**
     * An enum over the available template types and their String representation
     * @return
     */
    public static MPEnum[] getTypes() {
        MPEnum[] types = new MPEnum[11];
        types[0] = new MPEnum() {

            public Integer getId() {
                return TYPE_BILL;
            }

            public String getName() {
                return Messages.TYPE_BILL.toString();
            }
        };

        types[1] = new MPEnum() {

            public Integer getId() {
                return TYPE_OFFER;
            }

            public String getName() {
                return Messages.TYPE_OFFER.toString();
            }
        };

        types[2] = new MPEnum() {

            public Integer getId() {
                return TYPE_ORDER;
            }

            public String getName() {
                return Messages.TYPE_ORDER.toString();
            }
        };

        types[3] = new MPEnum() {

            public Integer getId() {
                return TYPE_CONTACT;
            }

            public String getName() {
                return Messages.TYPE_CONTACT.toString();
            }
        };

        types[4] = new MPEnum() {

            public Integer getId() {
                return TYPE_DELIVERY_NOTE;
            }

            public String getName() {
                return Messages.TYPE_DELIVERY.toString();
            }
        };

        types[5] = new MPEnum() {

            public Integer getId() {
                return TYPE_ORDER_CONFIRMATION;
            }

            public String getName() {
                return Messages.TYPE_CONFIRMATION.toString();
            }
        };

        types[6] = new MPEnum() {

            public Integer getId() {
                return TYPE_PRODUCT;
            }

            public String getName() {
                return Messages.TYPE_PRODUCT.toString();
            }
        };

        types[7] = new MPEnum() {

            public Integer getId() {
                return TYPE_SERVICE;
            }

            public String getName() {
                return Messages.TYPE_SERVICE.toString();
            }
        };

        types[8] = new MPEnum() {

            public Integer getId() {
                return TYPE_REMINDER;
            }

            public String getName() {
                return Messages.TYPE_REMINDER.toString();
            }
        };

        types[9] = new MPEnum() {

            public Integer getId() {
                return TYPE_JOURNAL;
            }

            public String getName() {
                return Messages.TYPE_JOURNAL.toString();
            }
        };

        types[10] = new MPEnum() {

            public Integer getId() {
                return TYPE_PRODUCT_ORDER;
            }

            public String getName() {
                return Messages.TYPE_PRODUCT_ORDER.toString();
            }
        };

        return types;
    }

    /**
     * (P)reload the template files
     */
    public static synchronized void cacheTemplates() {
        templateCache.clear();
        final List<Group> groups = new Vector<Group>();
        try {
            ArrayList<DatabaseObject> tlist = Group.getObjects(Context.getGroup(), true);
            for (int i = 0; i < tlist.size(); i++) {
                DatabaseObject databaseObject = tlist.get(i);
                groups.add((Group) databaseObject);
            }
        } catch (NodataFoundException ex) {
        }

        List<DatabaseObject> l = new Vector<DatabaseObject>();
        List<Integer> typs = new Vector<Integer>();
        Item it1 = new Item();
        it1.setInttype(Item.TYPE_BILL);
        l.add(it1);
        typs.add(TYPE_BILL);

        Item it2 = new Item();
        it2.setInttype(Item.TYPE_OFFER);
        l.add(it2);
        typs.add(TYPE_OFFER);

        Item it3 = new Item();
        it3.setInttype(Item.TYPE_ORDER);
        l.add(it3);
        typs.add(TYPE_ORDER);

        Item it4 = new Item();
        it4.setInttype(Item.TYPE_DELIVERY_NOTE);
        l.add(it4);
        typs.add(TYPE_DELIVERY_NOTE);

        Product it5 = new Product();
        it5.setInttype(Product.TYPE_PRODUCT);
        l.add(it5);
        typs.add(TYPE_PRODUCT);

        Product it6 = new Product();
        it6.setInttype(Product.TYPE_SERVICE);
        l.add(it6);
        typs.add(TYPE_SERVICE);

        Reminder it7 = new Reminder();
        l.add(it7);
        typs.add(TYPE_REMINDER);

        Contact it8 = new Contact();
        l.add(it8);
        typs.add(TYPE_CONTACT);

        l.add(null);
        typs.add(TYPE_JOURNAL);

        for (int i = 0; i < l.size(); i++) {
            final DatabaseObject databaseObject = l.get(i);
            final int type = typs.get(i);
            Runnable runnable = new Runnable() {

                public void run() {
                    for (int j = 0; j < groups.size(); j++) {
                        Group group = groups.get(j);
                        if (databaseObject != null && group != null) {
                            databaseObject.setGroupsids(group.__getIDS());
                        }
                        loadTemplate(databaseObject, type);
                    }
                }
            };
            new Thread(runnable).start();
        }
    }
    public static final int TYPE_BILL = 0;
    public static final int TYPE_ORDER = 1;
    public static final int TYPE_OFFER = 2;
    public static final int TYPE_DELIVERY_NOTE = 3;
    public static final int TYPE_ORDER_CONFIRMATION = 4;
    public static final int TYPE_PRODUCT = 5;
    public static final int TYPE_SERVICE = 6;
    public static final int TYPE_REMINDER = 7;
    public static final int TYPE_CONTACT = 8;
    public static final int TYPE_JOURNAL = 9;
    public static final int TYPE_PRODUCT_ORDER = 10;
    /**
     * The cache of the templates
     */
    public static HashMap<String, Template> templateCache = new HashMap<String, Template>();
    public static HashMap<String, Group> notification = new HashMap<String, Group>();

    /**
     * Load a template (if not already done) and enable the given button after loading.
     * @param button
     * @param dataOwner
     * @param TYPE
     */
    public static void loadTemplateFor(final JButton button, final DatabaseObject dataOwner, final int TYPE) {
        Runnable runnable = new Runnable() {

            public void run() {
                loadTemplate(dataOwner, TYPE);
                button.setEnabled(isLoaded(dataOwner, TYPE));
            }
        };
        new Thread(runnable).start();
    }

    /**
     * Gets the String representation of the given template type
     * @param type
     * @return
     */
    public static String getName(int type) {
        MPEnum[] e = getTypes();
        for (int i = 0; i < e.length; i++) {
            MPEnum mPEnum = e[i];
            if (mPEnum.getId().intValue() == type) {
                return mPEnum.getName();
            }
        }
        return "<undefined> [" + type + "]";
    }
}
