/*
 This file is part of YaBS.

 YaBS is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 YaBS is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with YaBS.  If not, see <http://www.gnu.org/licenses/>.
 */
package mpv5.handler;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.db.common.DatabaseSearch;
import mpv5.db.common.QueryHandler;
import mpv5.db.objects.Contact;
import mpv5.db.objects.Item;
import mpv5.db.objects.Product;
import mpv5.db.objects.ProductPrice;
import mpv5.db.objects.ValueProperty;
import mpv5.globals.Messages;
import mpv5.logging.Log;
import mpv5.utils.date.DateConverter;
import mpv5.utils.numberformat.FormatNumber;

/**
 * What deleting a record does, for the confirmation before: what goes to the
 * trash with it (can be restored), what is deleted permanently, and what stays.
 * <p>
 * Mirrors the delete logic of the records: {@link Contact#delete()} trashes
 * the offers, orders, invoices, deposits, part payments and credit notes of
 * the contact; {@link Item#delete()} trashes the positions;
 * {@link Product#delete()} deletes the staggered prices; every delete
 * removes the stored properties of the record
 * ({@link ValueProperty#deleteProperty}).
 */
public final class DeletionPreview {

    private DeletionPreview() {
    }

    /**
     * What happens to a group of data
     */
    public enum Effect {

        /**
         * moved to the trash, can be restored
         */
        TRASH,
        /**
         * deleted permanently
         */
        PERMANENT,
        /**
         * not affected
         */
        KEPT
    }

    /**
     * A group of affected data, e.g. "3 x Invoice" with the invoices
     */
    public static final class Group {

        public final Effect effect;
        public final String title;
        public final List<String> entries = new ArrayList<String>();

        Group(Effect effect, String title) {
            this.effect = effect;
            this.title = title;
        }
    }

    /**
     * Shown entries per group, the rest is counted
     */
    public static final int MAX_ENTRIES = 30;
    private static final int[] CONTACT_ITEM_TYPES = {Item.TYPE_OFFER, Item.TYPE_ORDER, Item.TYPE_INVOICE,
        Item.TYPE_DEPOSIT, Item.TYPE_PART_PAYMENT, Item.TYPE_CREDIT};

    /**
     * @param o the record to delete
     * @return the groups, most important first
     */
    public static List<Group> of(DatabaseObject o) {
        List<Group> groups = new ArrayList<Group>();
        if (o == null || !o.isExisting()) {
            return groups;
        }
        boolean trash = Context.getTrashableContexts().contains(o.getContext());
        Group self = new Group(trash ? Effect.TRASH : Effect.PERMANENT, String.valueOf(o.__getCname()));
        groups.add(self);
        if (o instanceof Contact) {
            contact((Contact) o, groups);
        } else if (o instanceof Product) {
            product((Product) o, groups);
        } else if (o instanceof Item) {
            item((Item) o, groups);
        } else {
            properties(groups, propertyNames(o), 0);
        }
        return groups;
    }

    private static void contact(Contact c, List<Group> groups) {
        List<Integer> allItems = new ArrayList<Integer>();
        int itemProperties = 0;
        for (int type : CONTACT_ITEM_TYPES) {
            Context ctx = contextOf(type);
            Object[][] data;
            try {
                data = new DatabaseSearch(ctx).getValuesFor(Context.DEFAULT_ITEM_SEARCH, "contactsids", c.__getIDS());
            } catch (Exception e) {
                Log.Debug(DeletionPreview.class, e);
                continue;
            }
            if (data == null || data.length == 0) {
                continue;
            }
            Group g = new Group(Effect.TRASH, String.format(Messages.DELETE_PREVIEW_COUNT.toString(), data.length, Item.getTypeString(type)));
            List<Integer> ids = new ArrayList<Integer>();
            for (Object[] row : data) {
                Integer id = toInt(row[0]);
                if (id != null) {
                    ids.add(id);
                }
                add(g, describeItem(row), data.length);
            }
            groups.add(g);
            allItems.addAll(ids);
            itemProperties += count(Context.getValueProperties(), "SELECT COUNT(*) FROM %%tablename%% WHERE contextids = "
                    + ctx.getId() + " AND objectids IN (" + in(ids) + ")", !ids.isEmpty());
        }
        int positions = count(Context.getSubItem(), "SELECT COUNT(*) FROM %%tablename%% WHERE itemsids IN ("
                + in(allItems) + ") AND invisible = 0", !allItems.isEmpty());
        if (positions > 0) {
            groups.add(new Group(Effect.TRASH, String.format(Messages.DELETE_PREVIEW_POSITIONS.toString(), positions)));
        }
        properties(groups, propertyNames(c), itemProperties);

        List<String> kept = new ArrayList<String>();
        keptCount(kept, Messages.DELETE_PREVIEW_ADDRESSES, count(Context.getAddress(),
                "SELECT COUNT(*) FROM %%tablename%% WHERE contactsids = " + c.__getIDS(), true));
        keptCount(kept, Messages.DELETE_PREVIEW_FILES, count(Context.getFilesToContacts(),
                "SELECT COUNT(*) FROM %%tablename%% WHERE contactsids = " + c.__getIDS(), true));
        keptCount(kept, Messages.DELETE_PREVIEW_ACTIVITIES, count(Context.getActivityList(),
                "SELECT COUNT(*) FROM %%tablename%% WHERE contactsids = " + c.__getIDS() + " AND invisible = 0",
                mpv5.globals.Features.activityLists()));
        kept(groups, kept);
    }

    private static void product(Product p, List<Group> groups) {
        List<ProductPrice> prices = p.findProductPrices();
        if (!prices.isEmpty()) {
            Group g = new Group(Effect.PERMANENT, String.format(Messages.DELETE_PREVIEW_TIERS.toString(), prices.size()));
            for (ProductPrice pp : prices) {
                add(g, String.format(Messages.DELETE_PREVIEW_TIER.toString(),
                        FormatNumber.formatDezimal(pp.getMincountvalue()), FormatNumber.formatDezimal(pp.getExternalnetvalue())), prices.size());
            }
            groups.add(g);
        }
        properties(groups, propertyNames(p), 0);
        List<String> kept = new ArrayList<String>();
        keptCount(kept, Messages.DELETE_PREVIEW_USED, count(Context.getSubItem(),
                "SELECT COUNT(*) FROM %%tablename%% WHERE originalproductsids = " + p.__getIDS() + " AND invisible = 0", true));
        keptCount(kept, Messages.DELETE_PREVIEW_FILES, count(Context.getFilesToProducts(),
                "SELECT COUNT(*) FROM %%tablename%% WHERE productsids = " + p.__getIDS(), true));
        kept(groups, kept);
    }

    private static void item(Item i, List<Group> groups) {
        int positions = count(Context.getSubItem(), "SELECT COUNT(*) FROM %%tablename%% WHERE itemsids = "
                + i.__getIDS() + " AND invisible = 0", true);
        if (positions > 0) {
            groups.add(new Group(Effect.TRASH, String.format(Messages.DELETE_PREVIEW_POSITIONS.toString(), positions)));
        }
        properties(groups, propertyNames(i), 0);
    }

    private static void properties(List<Group> groups, List<String> own, int further) {
        int total = own.size() + further;
        if (total == 0) {
            return;
        }
        Group g = new Group(Effect.PERMANENT, String.format(Messages.DELETE_PREVIEW_PROPERTIES.toString(), total));
        for (String n : own) {
            add(g, n, own.size());
        }
        if (further > 0) {
            g.entries.add(String.format(Messages.DELETE_PREVIEW_PROPERTIES_DOCUMENTS.toString(), further));
        }
        groups.add(g);
    }

    private static void kept(List<Group> groups, List<String> kept) {
        if (kept.isEmpty()) {
            return;
        }
        Group g = new Group(Effect.KEPT, Messages.DELETE_PREVIEW_KEPT.toString());
        g.entries.addAll(kept);
        groups.add(g);
    }

    private static void keptCount(List<String> kept, Messages label, int count) {
        if (count > 0) {
            kept.add(String.format(label.toString(), count));
        }
    }

    private static void add(Group g, String entry, int total) {
        if (g.entries.size() < MAX_ENTRIES) {
            g.entries.add(entry);
        } else if (g.entries.size() == MAX_ENTRIES) {
            g.entries.add(String.format(Messages.DELETE_PREVIEW_MORE.toString(), total - MAX_ENTRIES));
        }
    }

    private static List<String> propertyNames(DatabaseObject o) {
        List<String> names = new ArrayList<String>();
        try {
            for (ValueProperty v : ValueProperty.getProperties(o)) {
                names.add(v.getKey());
            }
        } catch (Exception e) {
            Log.Debug(DeletionPreview.class, e);
        }
        return names;
    }

    /**
     * @param row ids, number, date, net value
     * @return e.g. "R-2026-040 · 12.06.2026 · 59.80"
     */
    static String describeItem(Object[] row) {
        StringBuilder b = new StringBuilder(String.valueOf(row.length > 1 ? row[1] : row[0]));
        if (row.length > 2 && row[2] != null) {
            b.append(" \u00b7 ").append(row[2] instanceof Date ? DateConverter.getDefDateString((Date) row[2]) : String.valueOf(row[2]));
        }
        if (row.length > 3 && row[3] != null) {
            try {
                b.append(" \u00b7 ").append(FormatNumber.formatDezimal(new BigDecimal(String.valueOf(row[3]))));
            } catch (NumberFormatException e) {
                b.append(" \u00b7 ").append(row[3]);
            }
        }
        return b.toString();
    }

    private static Context contextOf(int type) {
        switch (type) {
            case Item.TYPE_OFFER:
                return Context.getOffer();
            case Item.TYPE_ORDER:
                return Context.getOrder();
            case Item.TYPE_DEPOSIT:
                return Context.getDeposit();
            case Item.TYPE_PART_PAYMENT:
                return Context.getPartPayment();
            case Item.TYPE_CREDIT:
                return Context.getCredit();
            default:
                return Context.getInvoice();
        }
    }

    static String in(List<Integer> ids) {
        StringBuilder b = new StringBuilder();
        for (Integer id : ids) {
            b.append(b.length() == 0 ? "" : ",").append(id);
        }
        return b.toString();
    }

    private static int count(Context c, String sql, boolean run) {
        if (!run) {
            return 0;
        }
        try {
            Object v = QueryHandler.instanceOf().clone(c).freeSelectQuery(sql, mpv5.usermanagement.MPSecurityManager.VIEW, null).getFirstEntry();
            return v == null ? 0 : Integer.parseInt(String.valueOf(v).trim());
        } catch (Exception e) {
            Log.Debug(DeletionPreview.class, e);
            return 0;
        }
    }

    private static Integer toInt(Object o) {
        try {
            return Integer.valueOf(String.valueOf(o).trim());
        } catch (Exception e) {
            return null;
        }
    }
}
