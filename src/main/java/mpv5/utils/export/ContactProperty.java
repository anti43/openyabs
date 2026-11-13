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
package mpv5.utils.export;

import java.util.ArrayList;
import java.util.List;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.db.common.QueryCriteria2;
import mpv5.db.common.QueryParameter;
import mpv5.db.objects.ValueProperty;
import mpv5.logging.Log;

/**
 * Value properties of contacts, independent of the context the contact was
 * opened with.
 * <p>
 * A value property is stored with the id of the owner's context. The same
 * contact can be a customer (context 3), supplier (5), manufacturer (4) or a
 * plain contact (6), depending on where it was opened - the e-invoice settings
 * saved in the customer view were not found when the invoice loaded its
 * contact as plain contact. Values are therefore read from all contact
 * contexts and written with the plain contact context.
 */
public final class ContactProperty {

    private ContactProperty() {
    }

    /**
     * @return the ids of all contexts of the contacts table, the plain contact
     * context first
     */
    static int[] contactContextIds() {
        return new int[]{Context.getContact().getId(), Context.getCustomer().getId(), Context.getSupplier().getId(),
            Context.getManufacturer().getId(), Context.getContactsCompanies().getId()};
    }

    /**
     * @param owner a contact (or any other object)
     * @return true if the owner is stored in the contacts table
     */
    static boolean isContact(DatabaseObject owner) {
        return owner instanceof mpv5.db.objects.Contact;
    }

    /**
     * @param owner the contact
     * @param key
     * @return all properties with this key of the contact, of any contact
     * context
     */
    private static List<ValueProperty> find(DatabaseObject owner, String key) {
        final int[] ids = isContact(owner) ? contactContextIds() : new int[]{owner.getContext().getId()};
        List<ValueProperty> list = new ArrayList<ValueProperty>();
        //one query for all contexts (was one per context: five per value, 30 to open a contact)
        QueryCriteria2 c = new QueryCriteria2();
        c.and(new QueryParameter(Context.getValueProperties(), "cname", key, QueryParameter.EQUALS),
                new QueryParameter(Context.getValueProperties(), "objectids", owner.__getIDS(), QueryParameter.EQUALS));
        QueryParameter[] contexts = new QueryParameter[ids.length];
        for (int i = 0; i < ids.length; i++) {
            contexts[i] = new QueryParameter(Context.getValueProperties(), "contextids", ids[i], QueryParameter.EQUALS);
        }
        if (contexts.length == 1) {
            c.and(contexts[0]);
        } else {
            c.or(contexts[0], java.util.Arrays.copyOfRange(contexts, 1, contexts.length));
        }
        try {
            for (DatabaseObject o : DatabaseObject.getObjects(Context.getValueProperties(), c)) {
                list.add((ValueProperty) o);
            }
        } catch (Exception e) {
            //none
        }
        //in the order of the contexts: the plain contact context first
        list.sort(java.util.Comparator.comparingInt(p -> indexOf(ids, p.__getContextids())));
        return list;
    }

    private static int indexOf(int[] ids, int id) {
        for (int i = 0; i < ids.length; i++) {
            if (ids[i] == id) {
                return i;
            }
        }
        return ids.length;
    }

    /**
     * @param owner the contact, may be null or unsaved
     * @param key
     * @return the value as String, null if none
     */
    public static String get(DatabaseObject owner, String key) {
        if (owner == null || !owner.isExisting()) {
            return null;
        }
        try {
            for (ValueProperty p : find(owner, key)) {
                Object v = p.getValue();
                if (v != null) {
                    return String.valueOf(v);
                }
            }
        } catch (Exception e) {
            Log.Debug(ContactProperty.class, e.getMessage());
        }
        return null;
    }

    /**
     * Store a value at the (saved) contact, null or empty removes it
     *
     * @param owner
     * @param key
     * @param value
     */
    public static void set(DatabaseObject owner, String key, String value) {
        if (owner == null || !owner.isExisting()) {
            return;
        }
        String v = value == null ? "" : value.trim();
        int canonicalId = isContact(owner) ? Context.getContact().getId() : owner.getContext().getId();
        for (ValueProperty p : find(owner, key)) {
            if (v.isEmpty()) {
                p.delete();
            } else if (p.__getContextids() != canonicalId) {
                //saved with another context (e.g. customer) before: moved to the contact context
                p.delete();
            } else if (v.equals(String.valueOf(p.getValue()))) {
                return;//unchanged
            }
        }
        if (v.isEmpty()) {
            return;
        }
        DatabaseObject canonical = owner;
        if (isContact(owner) && owner.getContext().getId() != Context.getContact().getId()) {
            try {
                canonical = DatabaseObject.getObject(Context.getContact(), owner.__getIDS());
            } catch (Exception e) {
                Log.Debug(ContactProperty.class, e.getMessage());
            }
        }
        ValueProperty.addOrUpdateProperty(key, v, canonical);
    }
}
