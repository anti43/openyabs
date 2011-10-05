package enoa.handler;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.swing.JComponent;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.db.common.NodataFoundException;
import mpv5.db.common.QueryHandler;
import mpv5.db.common.ReturnValue;
import mpv5.db.common.SaveString;
import mpv5.db.common.Templateable;
import mpv5.db.objects.*;
import mpv5.globals.LocalSettings;
import mpv5.globals.Messages;
import mpv5.handler.MPEnum;
import mpv5.logging.Log;
import mpv5.ui.panels.DataPanel;
import mpv5.usermanagement.MPSecurityManager;
import mpv5.utils.export.ODTFile;
import mpv5.utils.export.PDFFile;

/**
 * This class provides {@link Template} loading and caching functionality, thread-safe
 */
public class TemplateHandler {
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
    public static final int TYPE_CONTRACT = 11;
    public static final int TYPE_CONVERSATION = 12;
    /**
     * Return true if the Template for the currently logged in user, with the given type, and matching the targets group is loaded
     * @param group
     * @param type
     * @return
     */
    public static synchronized boolean isLoaded(Long group, int type) {
        String key = null;
        if (group != null) {
            key = mpv5.db.objects.User.getCurrentUser() + "@" + type + "@" + group;
        } else {
            key = mpv5.db.objects.User.getCurrentUser() + "@" + type + "@" + 1;
        }

        if (TEMPLATE_CACHE.containsKey(key)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Return true if the Template for the currently logged in user, with the given type, and matching the targets group is loaded
     * @param target
     * @param type
     * @return
     */
    public static synchronized boolean isLoaded(Templateable target) {
        String key = null;
        if (target != null) {
            key = mpv5.db.objects.User.getCurrentUser() + "@" + target.templateType() + "@" + target.templateGroupIds();
        } else {
            key = mpv5.db.objects.User.getCurrentUser() + "@" + target.templateType() + "@" + 1;
        }

        if (TEMPLATE_CACHE.containsKey(key)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Loads a template including neccesary files, target's group
     * @param target
     * @param typ
     * @return
     */
    public static synchronized Template loadTemplate(Templateable target) {

        int groupsids = 0;
        if (target != null) {
            groupsids = target.templateGroupIds();
        } else {
            groupsids = 1;
        }

        return loadTemplate(groupsids, target.templateType());
    }

    /**
     *
     * @param t
     * @return
     */
    public static synchronized Template loadTemplate(Template t) {
        return loadTemplate(t.__getGroupsids(), Integer.valueOf(t.__getMimetype()));
    }

    /**
     * 
     * @param group
     * @param typ
     * @return
     */
    public static synchronized Template loadTemplate(int group, int typ) {
        return loadTemplate(Long.valueOf(group), typ);
    }

    /**
     * Loads a template including necessary files
     * @param target
     * @param groupsids
     * @param typ
     * @return
     */
    public static synchronized Template loadTemplate(long groupsids, int typ) {
        if (LocalSettings.getBooleanProperty(LocalSettings.OFFICE_USE)) {
            Integer type = new Integer(typ);

            if (groupsids < 0) {
                groupsids = 1;
            }

            String key = mpv5.db.objects.User.getCurrentUser() + "@" + type + "@" + groupsids;
            if (TEMPLATE_CACHE.containsKey(key)) {
                return TEMPLATE_CACHE.get(key);
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
                            Log.Debug(TemplateHandler.class, preloadedTemplate.getFile());
                            if (preloadedTemplate.getFile().getName().endsWith("odt")) {
                                if (LocalSettings.getBooleanProperty(LocalSettings.OFFICE_USE)) {
                                    preloadedTemplate.defineExFile(new ODTFile(preloadedTemplate.getFile().getPath()));
                                    Log.Debug(Template.class, "Loaded template: " + preloadedTemplate);
                                    mpv5.YabsViewProxy.instance().addMessage(preloadedTemplate + Messages.LOADED.toString());
                                } else {
//                                Popup.notice(Messages.NOT_POSSIBLE + "\n" + Messages.OOCONNERROR);
                                    return null;
                                }
                            } else {
                                preloadedTemplate.defineExFile(new PDFFile(preloadedTemplate.getFile().getPath()));
                            }
                            TEMPLATE_CACHE.put(key, preloadedTemplate);
                        } catch (Exception ex) {
                            Log.Debug(Template.class, "Invalid template: " + data.getData()[data.getData().length - 1][0].toString());
                            try {
                                Log.Debug(TemplateHandler.class, "Removing invalid template " + preloadedTemplate);
                                preloadedTemplate.delete();
                            } catch (Exception e) {
                                Log.Debug(e);
                            }
                            return null;
                        }
                    } else {
                        try {
                            if (!(TEMPLATE_MISSING_NOTIFICATIONS.containsKey(type.toString()) && TEMPLATE_MISSING_NOTIFICATIONS.get(type.toString()).equals(Group.getObject(Context.getGroup(), (int) groupsids)))) {
                                mpv5.YabsViewProxy.instance().addMessage(Messages.OO_NO_TEMPLATE + ": " + TemplateHandler.getName(type) + " [" + mpv5.db.objects.User.getCurrentUser() + "] [" + Group.getObject(Context.getGroup(), (int) groupsids) + "]");
                                Log.Debug(Template.class, "No template found for type: " + type + " for user: " + mpv5.db.objects.User.getCurrentUser() + " in GROUP " + Group.getObject(Context.getGroup(), (int) groupsids));
                                TEMPLATE_MISSING_NOTIFICATIONS.put(type.toString(), (Group) Group.getObject(Context.getGroup(), (int) groupsids));
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
        } else {
            return null;
        }
    }

    /**
     * An enum over the available template types and their String representation
     * @return
     */
    public static MPEnum[] getTypes() {
        MPEnum[] types = new MPEnum[13];
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

        types[11] = new MPEnum() {

            public Integer getId() {
                return TYPE_CONTACT;
            }

            public String getName() {
                return Messages.TYPE_CONTRACT.toString();
            }
        };
            
        types[12] = new MPEnum() {

            public Integer getId() {
                return TYPE_CONVERSATION;
            }

            public String getName() {
                return Messages.TYPE_CONVERSATION.toString();
            }            
        };


        return types;
    }

//    /**
//     * (P)reload the template files
//     * @deprecated performance..
//     */
//    public static synchronized void cacheTemplates() {
//        TEMPLATE_CACHE.clear();
//        final List<Group> groups = new ArrayList<Group>();
//        try {
//            ArrayList<DatabaseObject> tlist = Group.getObjects(Context.getGroup(), true);
//            for (int i = 0; i < tlist.size(); i++) {
//                DatabaseObject databaseObject = tlist.get(i);
//                groups.add((Group) databaseObject);
//            }
//        } catch (NodataFoundException ex) {
//        }
//
//        List<Templateable> targets = new ArrayList<Templateable>();
//        List<Integer> typs = new ArrayList<Integer>();
//        Item it1 = new Item();
//        it1.setInttype(Item.TYPE_BILL);
//        targets.add(it1);
//        typs.add(TYPE_BILL);
//
//        Item it2 = new Item();
//        it2.setInttype(Item.TYPE_OFFER);
//        targets.add(it2);
//        typs.add(TYPE_OFFER);
//
//        Item it3 = new Item();
//        it3.setInttype(Item.TYPE_ORDER);
//        targets.add(it3);
//        typs.add(TYPE_ORDER);
//
//        Item it4 = new Item();
//        it4.setInttype(TYPE_DELIVERY_NOTE);
//        targets.add(it4);
//        typs.add(TYPE_DELIVERY_NOTE);
//
//        Product it5 = new Product();
//        it5.setInttype(Product.TYPE_PRODUCT);
//        targets.add(it5);
//        typs.add(TYPE_PRODUCT);
//
//        Product it6 = new Product();
//        it6.setInttype(Product.TYPE_SERVICE);
//        targets.add(it6);
//        typs.add(TYPE_SERVICE);
//
//        Reminder it7 = new Reminder();
//        targets.add(it7);
//        typs.add(TYPE_REMINDER);
//
//        Contact it8 = new Contact();
//        targets.add(it8);
//        typs.add(TYPE_CONTACT);
//
//        targets.add(null);
//        typs.add(TYPE_JOURNAL);
//
//        for (int i = 0; i < targets.size(); i++) {
////            final Templateable databaseObject = targets.get(i);
//            final int type = typs.get(i);
//            Runnable runnable = new Runnable() {
//
//                public void run() {
//                    for (int j = 0; j < groups.size(); j++) {
//                        Group group = groups.get(j);
////                        if (databaseObject != null && group != null) {
////                            ((DatabaseObject)databaseObject).setGroupsids(group.__getIDS());
////                        }
//                        loadTemplate(group.__getIDS(), type);
//                    }
//                }
//            };
//            new Thread(runnable).start();
//        }
//    }

    /**
     * The cache of the templates
     */
    public static HashMap<String, Template> TEMPLATE_CACHE = new HashMap<String, Template>();
    public static HashMap<String, Group> TEMPLATE_MISSING_NOTIFICATIONS = new HashMap<String, Group>();

    /**
     * Load a template (if not already done) and enable the given button after loading.
     * @param button
     * @param dataOwner
     */
    public static void loadTemplateFor(final JComponent button, final Templateable dataOwner) {
        loadTemplate(dataOwner.templateGroupIds(), dataOwner.templateType());
    }

    /**
     * Load a template for a specific GROUP rather than the dataOwners group (if not already done) and enable the given button after loading.
     * @param button
     * @param typ
     * @param groupsids
     */
    public static void loadTemplateFor(final JComponent button, final long groupsids, final int typ) {
        button.setEnabled(false);
        Runnable runnable = new Runnable() {

            @Override
            public void run() {
                loadTemplate(groupsids, typ);
                button.setEnabled(isLoaded(groupsids, typ));
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

    /**
     * Load a template (if not already done) and enable the given button after loading.
     * @param jComponent
     * @param dataOwner
     * @param TYPE
     */
    public static void loadTemplateFor(final JComponent[] jComponent, final Templateable dataOwner) {
        for (int i = 0; i < jComponent.length; i++) {
            JComponent jComponent1 = jComponent[i];
            jComponent1.setEnabled(false);
        }
        Runnable runnable = new Runnable() {

            public void run() {
                loadTemplate(dataOwner);
                for (int i = 0; i < jComponent.length; i++) {
                    JComponent jComponent1 = jComponent[i];
                    jComponent1.setEnabled(isLoaded(dataOwner));
                }
            }
        };
        new Thread(runnable).start();
    }

    /**
     * Load a template (if not already done) and enable the given button after loading.
     * @param jComponent
     * @param typ
     * @param groupsids
     */
    public static void loadTemplateFor(final JComponent[] jComponent, final long groupsids, final int typ) {
        for (int i = 0; i < jComponent.length; i++) {
            JComponent jComponent1 = jComponent[i];
            jComponent1.setEnabled(false);
        }
        Runnable runnable = new Runnable() {

            @Override
            public void run() {
                loadTemplate(groupsids, typ);
                for (int i = 0; i < jComponent.length; i++) {
                    JComponent jComponent1 = jComponent[i];
                    jComponent1.setEnabled(isLoaded(groupsids, typ));
                }
            }
        };
        new Thread(runnable).start();
    }

    /**
     * Imports a template file
     * @param file
     * @return
     */
    public static boolean importTemplate(File file) {
        Template t = new Template();

        return QueryHandler.instanceOf().clone(Context.getFiles(), (DataPanel) null).insertFile(file, t, new SaveString(file.getName(), true));

//                User object = mpv5.db.objects.User.getCurrentUser();
//
//                QueryCriteria d = new QueryCriteria();
//                d.add("cname", dataOwner.__getIDS() + "@" + object.__getIDS() + "@" + mpv5.db.objects.User.getCurrentUser().__getGroupsids());
//                QueryHandler.instanceOf().clone(Context.getTemplatesToUsers()).delete(d);
//
//                QueryData c = new QueryData();
//                c.add("usersids", object.__getIDS());
//                c.add("templatesids", dataOwner.__getIDS());
//                c.add("groupsids", mpv5.db.objects.User.getCurrentUser().__getGroupsids());
//                c.add("cname", dataOwner.__getIDS() + "@" + object.__getIDS() + "@" + mpv5.db.objects.User.getCurrentUser().__getGroupsids());
//                QueryHandler.instanceOf().clone(Context.getTemplatesToUsers()).insert(c, null);

    }
}
