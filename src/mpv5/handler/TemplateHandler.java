package mpv5.handler;

import java.awt.Color;
import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
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
import mpv5.utils.export.PDFFile;

import static mpv5.globals.Constants.*;

import mpv5.utils.export.ODTFile2;

/**
 * This class provides {@link Template} loading and caching functionality,
 * thread-safe
 */
public class TemplateHandler {

    /**
     * Return true if the Template for the currently logged in user, with the
     * given type, and matching the targets group is loaded
     *
     * @param group
     * @param type
     * @return
     */
    public static synchronized boolean isLoaded(Long group, int type) {
        String key;

        if (group != null) {
            key = mpv5.db.objects.User.getCurrentUser() + "@" + type + "@" + group;
        } else {
            key = mpv5.db.objects.User.getCurrentUser() + "@" + type + "@" + 1;
        }

        return TEMPLATE_CACHE.containsKey(key);
    }

    /**
     * Return true if the Template for the currently logged in user, with the
     * given type, and matching the targets group is loaded
     *
     * @param target
     * @return
     */
    public static synchronized boolean isLoaded(Templateable target) {
        String key;
        if (target == null)
            return false;

        key = mpv5.db.objects.User.getCurrentUser() + "@" + target.templateType() + "@" + target.templateGroupIds();

        return TEMPLATE_CACHE.containsKey(key);
    }

    /**
     * Loads a template including neccesary files, target's group
     *
     * @param target
     * @return
     */
    public static synchronized Template loadTemplate(Templateable target) {

        int groupsids;
        if (target != null) {
            groupsids = target.templateGroupIds();
            return loadTemplate(groupsids, target.templateType());
        } else {
            groupsids = 1;
            return loadTemplate(groupsids, 1);
        }
    }

    /**
     * @param t
     * @return
     */
    public static synchronized Template loadTemplate(Template t) {
        return loadTemplate(t.__getGroupsids(), Integer.valueOf(t.__getMimetype()));
    }

    /**
     * @param group
     * @param typ
     * @return
     */
    public static synchronized Template loadTemplate(int group, int typ) {
        return loadTemplate(Long.valueOf(group), typ);
    }

    /**
     * Loads a template including necessary files
     *
     * @param groupsids
     * @param typ
     * @return
     */
    public static synchronized Template loadTemplate(long groupsids, int typ) {
        ReturnValue data;
        if (LocalSettings.getBooleanProperty(LocalSettings.OFFICE_USE)
                || LocalSettings.getBooleanProperty(LocalSettings.OFFICE_ALT)) {
            Integer type = typ;

            if (groupsids < 0) {
                groupsids = 1;
            }

            String key = mpv5.db.objects.User.getCurrentUser() + "@" + type + "@" + groupsids;
            if (TEMPLATE_CACHE.containsKey(key)) {
                return TEMPLATE_CACHE.get(key);
            } else {

                data = TemplateHandler.getDefinedTemplatesFor(groupsids, type);
                Template preloadedTemplate = null;
                if (data.hasData()) {
                    try {
                        preloadedTemplate = (Template) DatabaseObject.getObject(Context.getTemplate(), Integer.valueOf(data.getData()[data.getData().length - 1][0].toString()));
                        Log.Debug(TemplateHandler.class, preloadedTemplate.getFile());
                        if (preloadedTemplate.getFile().getName().endsWith("pdf")) {
                            preloadedTemplate.defineExFile(new PDFFile(preloadedTemplate.getFile().getPath()));
                        } else {
                            preloadedTemplate.defineExFile(new ODTFile2(preloadedTemplate.getFile().getPath()));
                            Log.Debug(Template.class, "Loaded template: " + preloadedTemplate);
                            mpv5.YabsViewProxy.instance().addMessage(preloadedTemplate + Messages.LOADED.toString(), Color.GREEN);
                        }
                        TEMPLATE_CACHE.put(key, preloadedTemplate);
                    } catch (Exception ex) {
                        Log.Debug(ex);
                        Log.Debug(Template.class, "Possibly invalid template: " + data.getData()[data.getData().length - 1][0].toString());
                        return null;
                    }
                } else {
                    try {
                        if (!(TEMPLATE_MISSING_NOTIFICATIONS.containsKey(type.toString()) && TEMPLATE_MISSING_NOTIFICATIONS.get(type.toString()).equals(Group.getObject(Context.getGroup(), (int) groupsids)))) {
                            mpv5.YabsViewProxy.instance().addMessage(Messages.OO_NO_TEMPLATE + ": " + TemplateHandler.getName(type) + " [" + mpv5.db.objects.User.getCurrentUser() + "] [" + Group.getObject(Context.getGroup(), (int) groupsids) + "]", Color.YELLOW);
                            Log.Debug(Template.class, "No template found for type: " + type + " for user: " + mpv5.db.objects.User.getCurrentUser() + " in GROUP " + Group.getObject(Context.getGroup(), (int) groupsids));
                            TEMPLATE_MISSING_NOTIFICATIONS.put(type.toString(), (Group) Group.getObject(Context.getGroup(), (int) groupsids));
                        }
                    } catch (NodataFoundException nodataFoundException) {
                        Log.Debug(Template.class, nodataFoundException.getMessage());
                    }
                }
                return preloadedTemplate;
            }
        } else {
            return null;
        }
    }

    private static ReturnValue getDefinedTemplatesFor(long groupsids, Integer type) {
        ReturnValue data = QueryHandler.getConnection().freeQuery(
                "SELECT templatesids FROM templatestousers  LEFT OUTER JOIN templates AS templates0 ON "
                        + "templates0.ids = templatestousers.templatesids WHERE templatestousers.usersids="
                        + mpv5.db.objects.User.getCurrentUser().__getIDS()
                        + " AND "
                        + "templates0.mimetype='" + type
                        + "' AND templatestousers.IDS>0 "
                        + "AND templates0.groupsids = " + groupsids, MPSecurityManager.VIEW, null);
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
        Log.Debug(TemplateHandler.class, "gefundene Daten: " + data.hasData());
        return data;
    }

    /**
     * An enum over the available template types and their String representation
     *
     * @return
     */
    public static MPEnum[] getTypes() {
        MPEnum[] types = new MPEnum[18];
        types[0] = new MPEnum() {
            @Override
            public Integer getId() {
                return TYPE_INVOICE;
            }

            @Override
            public String getName() {
                return Messages.TYPE_INVOICE.toString();
            }
        };

        types[1] = new MPEnum() {
            @Override
            public Integer getId() {
                return TYPE_OFFER;
            }

            @Override
            public String getName() {
                return Messages.TYPE_OFFER.toString();
            }
        };

        types[2] = new MPEnum() {
            @Override
            public Integer getId() {
                return TYPE_ORDER;
            }

            @Override
            public String getName() {
                return Messages.TYPE_ORDER.toString();
            }
        };

        types[3] = new MPEnum() {
            @Override
            public Integer getId() {
                return TYPE_CONTACT;
            }

            @Override
            public String getName() {
                return Messages.TYPE_CONTACT.toString();
            }
        };

        types[4] = new MPEnum() {
            @Override
            public Integer getId() {
                return TYPE_DELIVERY_NOTE;
            }

            @Override
            public String getName() {
                return Messages.TYPE_DELIVERY.toString();
            }
        };

        types[5] = new MPEnum() {
            @Override
            public Integer getId() {
                return TYPE_ORDER_CONFIRMATION;
            }

            @Override
            public String getName() {
                return Messages.TYPE_CONFIRMATION.toString();
            }
        };

        types[6] = new MPEnum() {
            @Override
            public Integer getId() {
                return TYPE_PRODUCT;
            }

            @Override
            public String getName() {
                return Messages.TYPE_PRODUCT.toString();
            }
        };

        types[7] = new MPEnum() {
            @Override
            public Integer getId() {
                return TYPE_SERVICE;
            }

            @Override
            public String getName() {
                return Messages.TYPE_SERVICE.toString();
            }
        };

        types[8] = new MPEnum() {
            @Override
            public Integer getId() {
                return TYPE_REMINDER;
            }

            @Override
            public String getName() {
                return Messages.TYPE_REMINDER.toString();
            }
        };

        types[9] = new MPEnum() {
            @Override
            public Integer getId() {
                return TYPE_JOURNAL;
            }

            @Override
            public String getName() {
                return Messages.TYPE_JOURNAL.toString();
            }
        };

        types[10] = new MPEnum() {
            @Override
            public Integer getId() {
                return TYPE_PRODUCT_ORDER;
            }

            @Override
            public String getName() {
                return Messages.TYPE_PRODUCT_ORDER.toString();
            }
        };

        types[11] = new MPEnum() {
            @Override
            public Integer getId() {
                return TYPE_CONTRACT;
            }

            @Override
            public String getName() {
                return Messages.TYPE_CONTRACT.toString();
            }
        };

        types[12] = new MPEnum() {
            @Override
            public Integer getId() {
                return TYPE_CONVERSATION;
            }

            @Override
            public String getName() {
                return Messages.TYPE_CONVERSATION.toString();
            }
        };

        types[13] = new MPEnum() {
            @Override
            public Integer getId() {
                return TYPE_MASSPRINT;
            }

            @Override
            public String getName() {
                return Messages.TYPE_MASSPRINT.toString();
            }
        };

        types[14] = new MPEnum() {
            @Override
            public Integer getId() {
                return TYPE_ACTIVITY;
            }

            @Override
            public String getName() {
                return Messages.TYPE_ACTIVITY.toString();
            }
        };

        types[15] = new MPEnum() {
            @Override
            public Integer getId() {
                return TYPE_CREDIT;
            }

            @Override
            public String getName() {
                return Messages.TYPE_CREDIT.toString();
            }
        };

        types[16] = new MPEnum() {
            @Override
            public Integer getId() {
                return TYPE_PART_PAYMENT;
            }

            @Override
            public String getName() {
                return Messages.TYPE_PART_PAYMENT.toString();
            }
        };

        types[17] = new MPEnum() {
            @Override
            public Integer getId() {
                return TYPE_DEPOSIT;
            }

            @Override
            public String getName() {
                return Messages.TYPE_DEPOSIT.toString();
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
//        it1.setInttype(Item.TYPE_INVOICE);
//        targets.add(it1);
//        typs.add(TYPE_INVOICE);
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
     *
     * @param button
     * @param dataOwner
     */
    public static void loadTemplateFor(final JComponent button, final Templateable dataOwner) {
        loadTemplate(dataOwner.templateGroupIds(), dataOwner.templateType());
    }

    /**
     * Load a template for a specific GROUP rather than the dataOwners group (if not
     * already done) and enable the given button after loading.
     *
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
     *
     * @param type
     * @return
     */
    public static String getName(int type) {
        MPEnum[] e = getTypes();
        for (MPEnum mPEnum : e) {
            if (mPEnum.getId() == type) {
                return mPEnum.getName();
            }
        }
        return "<undefined> [" + type + "]";
    }

    /**
     * Load a template (if not already done) and enable the given button after loading.
     *
     * @param jComponent
     * @param dataOwner
     */
    public static void loadTemplateFor(final JComponent[] jComponent, final Templateable dataOwner) {
        for (JComponent jComponent1 : jComponent) {
            jComponent1.setEnabled(false);
        }
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                loadTemplate(dataOwner);
                for (JComponent jComponent1 : jComponent) {
                    jComponent1.setEnabled(isLoaded(dataOwner));
                }
            }
        };
        new Thread(runnable).start();
    }

    /**
     * Load a template (if not already done) and enable the given button after loading.
     *
     * @param jComponent
     * @param typ
     * @param groupsids
     */
    public static void loadTemplateFor(final JComponent[] jComponent, final long groupsids, final int typ) {
        for (JComponent jComponent1 : jComponent) {
            jComponent1.setEnabled(false);
        }
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                loadTemplate(groupsids, typ);
                for (JComponent jComponent1 : jComponent) {
                    jComponent1.setEnabled(isLoaded(groupsids, typ));
                }
            }
        };
        new Thread(runnable).start();
    }

    /**
     * Imports a template file
     *
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

    /**
     * Exports all template Objects for give template-Type
     *
     * @param groupsids
     * @param typ
     * @return template[] - the template assoziated to the give templatetype
     */
    public static Template[] getTemplatesForType(long groupsids, int typ) {
        String key = mpv5.db.objects.User.getCurrentUser() + "@" + typ + "@" + groupsids;
        ReturnValue data = TemplateHandler.getDefinedTemplatesFor(groupsids, typ);
        Iterator<Object[]> it = data.getDataIterator();
        Template[] templates = new Template[data.getData().length];
        int i = 0;
        while (it.hasNext()) {
            Object[] ret = it.next();
            try {
                templates[i++] = (Template) DatabaseObject.getObject(Context.getTemplate(), Integer.valueOf(ret[0].toString()));
            } catch (NumberFormatException ex) {
                Log.Debug(Template.class, ex);
            } catch (NodataFoundException ex) {
                Log.Debug(Template.class, ex);
            }
        }
        return templates;
    }

    /**
     * Clear the template cache
     */
    public static void clearCache() {
        TEMPLATE_CACHE.clear();
    }

    public static boolean importTemplateAndAssign(File file, FutureCallback<Template> f) {
        Template t = new Template();
        return QueryHandler.instanceOf().clone(Context.getFiles(), (DataPanel) null).insertFile(file, t, new SaveString(file.getName(), true), f);
    }

}
