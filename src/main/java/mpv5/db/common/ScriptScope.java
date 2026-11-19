package mpv5.db.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import mpv5.db.objects.Contact;
import mpv5.db.objects.Group;
import mpv5.db.objects.Item;
import mpv5.db.objects.ValueProperty;
import mpv5.globals.Constants;
import mpv5.logging.Log;

/**
 * Which scripts of the script manager apply to a record.
 * <p>
 * A script is stored for a kind of record ("For": a context id) and a group.
 * It applies to a record if
 * <ul>
 * <li>its kind is the kind of the record: for documents the kind of their
 * type (an offer is an offer, however it was loaded) or <b>all documents</b>;
 * for contacts all contacts or a role the contact has (customer, supplier,
 * manufacturer); otherwise the context of the record</li>
 * <li>and its group is "All groups", the group of the record or a parent
 * group of it.</li>
 * </ul>
 * Order: general first, specific last (all documents, then the type; "All
 * groups", then deeper groups) - a field of the same name defined more
 * specifically wins, hooks run in this order.
 */
public final class ScriptScope {

    /**
     * "For" of scripts that apply to every document (invoice, offer, order,
     * credit note ...)
     */
    public static final int ALL_DOCUMENTS = 900;

    private static volatile List<ValueProperty> cache;

    private ScriptScope() {
    }

    /**
     * Forgets the cached scripts (called when a value property is saved or
     * deleted, or the database changes)
     */
    public static void clear() {
        cache = null;
    }

    private static List<ValueProperty> all() {
        List<ValueProperty> c = cache;
        if (c == null) {
            try {
                ArrayList<DatabaseObject> objects = DatabaseObject.getObjects(Context.getValueProperties(),
                        new QueryCriteria("objectids", 0));
                c = DatabaseObject.toObjectList(objects, new ValueProperty());
            } catch (NodataFoundException ex) {
                c = Collections.emptyList();
            } catch (Exception ex) {
                Log.Debug(ScriptScope.class, ex);
                return Collections.emptyList();
            }
            cache = c;
        }
        return c;
    }

    /**
     * @param type the type of a document (Constants.TYPE_*)
     * @param fallback the context id of the record
     * @return the context id scripts of this type are stored for
     */
    public static int documentKind(int type, int fallback) {
        switch (type) {
            case Constants.TYPE_INVOICE:
            case Constants.TYPE_DELIVERY_NOTE:
            case Constants.TYPE_ORDER_CONFIRMATION:
                return 1;
            case Constants.TYPE_ORDER:
                return 18;
            case Constants.TYPE_OFFER:
                return 19;
            case Constants.TYPE_CREDIT:
                return 60;
            case Constants.TYPE_PART_PAYMENT:
                return 61;
            case Constants.TYPE_DEPOSIT:
                return 62;
            default:
                return fallback;
        }
    }

    /**
     * @param o
     * @return the "For" ids that apply to the record, general first
     */
    public static List<Integer> kinds(DatabaseObject o) {
        List<Integer> k = new ArrayList<Integer>();
        int own = -1;
        try {
            //a new record may have no context yet (an Item before it is loaded)
            Context c = o.getContext();
            own = c == null ? -1 : c.getId();
        } catch (RuntimeException e) {
            Log.Debug(ScriptScope.class, e.getMessage());
        }
        if (o instanceof Item) {
            k.add(ALL_DOCUMENTS);
            k.add(documentKind(((Item) o).__getInttype(), own));
        } else if (o instanceof Contact) {
            Contact c = (Contact) o;
            k.add(Context.getContact().getId());
            if (c.__getIscustomer()) {
                k.add(Context.getCustomer().getId());
            }
            if (c.__getIssupplier()) {
                k.add(Context.getSupplier().getId());
            }
            if (c.__getIsmanufacturer()) {
                k.add(Context.getManufacturer().getId());
            }
        } else {
            k.add(own);
        }
        return k;
    }

    /**
     * @param o
     * @return the group of the record and its parent groups (the group
     * first)
     */
    public static List<Integer> groups(DatabaseObject o) {
        List<Integer> l = new ArrayList<Integer>();
        int id = o.__getGroupsids();
        for (int i = 0; id > 0 && i < 32 && !l.contains(id); i++) {
            l.add(id);
            try {
                Group g = (Group) DatabaseObject.getObject(Context.getGroup(), id);
                id = g.__getGroupsids();
            } catch (Exception e) {
                break;
            }
        }
        return l;
    }

    /**
     * @param scriptGroup the group of a script
     * @param recordGroups the group of the record and its parents, see
     * {@link #groups(DatabaseObject)}
     * @return -1: does not apply, else how specific (0 = all groups)
     */
    public static int groupRank(int scriptGroup, List<Integer> recordGroups) {
        if (scriptGroup <= 1) {
            return 0;
        }
        int i = recordGroups.indexOf(scriptGroup);
        //the root of the record's groups counts as "All groups"
        return i < 0 ? -1 : recordGroups.size() - 1 - i;
    }

    /**
     * @param o
     * @return the scripts that apply to the record, general first
     */
    public static List<ValueProperty> scripts(DatabaseObject o) {
        List<ValueProperty> all = all();
        if (all.isEmpty()) {
            return Collections.emptyList();
        }
        List<Integer> kinds = kinds(o);
        List<Integer> groups = groups(o);
        final Map<ValueProperty, Integer> rank = new java.util.IdentityHashMap<ValueProperty, Integer>();
        List<ValueProperty> l = new ArrayList<ValueProperty>();
        for (ValueProperty p : all) {
            int k = kinds.indexOf(p.__getContextids());
            if (k < 0) {
                continue;
            }
            int g = groupRank(p.__getGroupsids(), groups);
            if (g < 0) {
                continue;
            }
            rank.put(p, k * 100 + g);
            l.add(p);
        }
        Collections.sort(l, (a, b) -> Integer.compare(rank.get(a), rank.get(b)));
        return l;
    }

    /**
     * @param o
     * @param key e.g. executeAfterSave, IntItemLabel
     * @return the scripts of this name that apply to the record, general
     * first
     */
    public static List<String> scripts(DatabaseObject o, String key) {
        List<String> l = new ArrayList<String>();
        for (ValueProperty p : scripts(o)) {
            if (key.equals(p.getKey())) {
                Object v = p.getValue();
                if (v != null) {
                    l.add(String.valueOf(v));
                }
            }
        }
        return l;
    }

    /**
     * @param o
     * @param key
     * @return the most specific script of this name, null if none
     */
    public static String script(DatabaseObject o, String key) {
        List<String> l = scripts(o, key);
        return l.isEmpty() ? null : l.get(l.size() - 1);
    }
}
