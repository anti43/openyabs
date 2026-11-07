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
import java.math.RoundingMode;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.db.common.DatabaseSearch;
import mpv5.db.common.NodataFoundException;
import mpv5.db.common.QueryCriteria;
import mpv5.db.objects.ActivityList;
import mpv5.db.objects.Contact;
import mpv5.db.objects.Item;
import mpv5.db.objects.Product;
import mpv5.db.objects.SubItem;
import mpv5.db.objects.User;
import mpv5.globals.Messages;
import mpv5.logging.Log;
import mpv5.utils.arrays.ArrayUtilities;
import mpv5.utils.date.DateConverter;
import mpv5.utils.models.MPComboBoxModelItem;
import mpv5.utils.models.MPTableModel;
import mpv5.utils.text.TypeConversion;

/**
 * The non-visual logic of an offer / order / invoice view: converting
 * documents, saving the positions, comparing an invoice with its order, due
 * dates. Contains no Swing code; questions to the user are passed in as
 * callbacks.
 * <p>
 * Same behaviour as the classic view ({@link mpv5.ui.panels.ItemPanel2}).
 */
public final class ItemService {

    private ItemService() {
    }

    /**
     * @param type Item.TYPE_*
     * @return the context of documents of this type
     */
    public static Context contextOf(int type) {
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

    /**
     * @param type
     * @return true for invoices, deposits and part payments (they can be paid)
     */
    public static boolean isBill(int type) {
        return type == Item.TYPE_INVOICE || type == Item.TYPE_DEPOSIT || type == Item.TYPE_PART_PAYMENT;
    }

    /**
     * @param type
     * @param withCredit credit notes count as bills (status list of a loaded
     * document)
     * @return the status which is not offered for this type: paused for
     * bills, paid for the others
     */
    public static List<Integer> statusesToSkip(int type, boolean withCredit) {
        List<Integer> skip = new ArrayList<Integer>();
        if (isBill(type) || (withCredit && type == Item.TYPE_CREDIT)) {
            skip.add(Item.STATUS_PAUSED);
        } else {
            skip.add(Item.STATUS_PAID);
        }
        return skip;
    }

    /**
     * The due date for a new document, if the user wants it calculated
     * (setting "calcenddate"): today + the contact's payment term, else the
     * user's default ("bills.warn.days"), else 14 days.
     *
     * @param contact may be null
     * @param existing the document is saved already
     * @return the date, or null if it is not calculated
     */
    public static Date dueDate(Contact contact, boolean existing) {
        if (!User.getCurrentUser().getProperties().getProperty("org.openyabs.itemproperty", "calcenddate")
                || (User.getCurrentUser().getProperties().getProperty("org.openyabs.itemproperty", "keepmodifiedenddate") && existing)) {
            return null;
        }
        try {
            int payterm;
            if (contact != null && contact.__getPayterm() != 0) {
                payterm = contact.__getPayterm();
            } else {
                payterm = Integer.valueOf(User.getCurrentUser().getProperties().getProperty("bills.warn.days"));
            }
            return DateConverter.addDays(new Date(), payterm);
        } catch (Exception e) {
            return DateConverter.addDays(new Date(), 14);
        }
    }

    /**
     * The orders of a contact an invoice can refer to, with number and date
     *
     * @param contact
     * @return the choices
     */
    public static MPComboBoxModelItem[] referenceOrderChoices(Contact contact) {
        Object[][] data = new DatabaseSearch(Context.getOrder()).getValuesFor(Context.DEFAULT_ITEM_SEARCH + ", inttype", "contactsids", contact.__getIDS());
        Locale l = Locale.getDefault();
        if (User.getCurrentUser().getProperties().hasProperty("item.date.locale")) {
            try {
                Locale ul = TypeConversion.stringToLocale(User.getCurrentUser().getProperties().getProperty("item.date.locale"));
                if (ul != null) {
                    l = ul;
                }
            } catch (Exception e) {
                Log.Debug(ItemService.class, "Error while using item.date.locale");
            }
        }
        DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM, l);
        MPComboBoxModelItem[] items = new MPComboBoxModelItem[data.length];
        for (int a = 0; a < data.length; a++) {
            items[a] = new MPComboBoxModelItem(data[a][0], data[a][1] + "(" + df.format((Date) data[a][2]) + ")");
        }
        return items;
    }

    /**
     * The contact with this name, or a new customer with these data (when a
     * name is typed instead of choosing a contact)
     *
     * @param cname
     * @param prename
     * @param zip
     * @param city
     * @return the saved contact
     * @throws Exception if it cannot be saved
     */
    public static Contact contactByNameOrNew(String cname, String prename, String zip, String city) throws Exception {
        Contact c = Contact.findByCname(cname);
        if (c == null) {
            c = new Contact();
            c.setCname(cname);
            c.setPrename(prename);
            c.setZip(zip);
            c.setCity(city);
            c.setisCustomer(true);
            c.save();
        }
        return (Contact) DatabaseObject.getObject(Context.getContact(), c.__getIDS(), true);
    }

    /**
     * Saves the positions of a saved document; for offers, orders ... new
     * products are created from positions without product
     *
     * @param item
     * @param positions
     * @param deleteRemoved delete positions removed from the table
     * @param usedOrders orders taken over as positions: set to finished
     */
    public static void savePositions(Item item, MPTableModel positions, boolean deleteRemoved, List<Item> usedOrders) {
        if (item.__getInttype() != Item.TYPE_INVOICE) {
            Product.createProducts(SubItem.saveModel(item, positions, deleteRemoved), item);
        } else {
            SubItem.saveModel(item, positions, deleteRemoved);
        }
        if (usedOrders != null) {
            for (Item o : usedOrders) {
                o.setIntstatus(Item.STATUS_FINISHED);
                o.save(true);
            }
        }
    }

    /**
     * Creates an order from an offer: the offer is set to finished
     *
     * @param source with the data of the view (getPanelData)
     * @param positions the positions of the view
     * @return the new, saved order
     */
    public static Item toOrder(Item source, MPTableModel positions) {
        source.setIntstatus(Item.STATUS_FINISHED);
        source.save();
        Item i2 = (Item) source.clone(Context.getOrder());
        i2.setInttype(Item.TYPE_ORDER);
        i2.setIDS(-1);
        i2.defineFormatHandler(new FormatHandler(i2));
        i2.save();
        SubItem.saveModel(i2, positions, true, true);
        return i2;
    }

    /**
     * Creates an invoice, deposit, part payment or credit note from the
     * document: the source is set to finished. Asks (with the given callback)
     * whether activity lists of the order and earlier deposits / part
     * payments are to be taken over.
     *
     * @param source with the data of the view (getPanelData)
     * @param itemType Item.TYPE_INVOICE, _DEPOSIT, _PART_PAYMENT or _CREDIT
     * @param positions the positions of the view; activity lists are added
     * to it
     * @param ask shows a yes/no question
     * @return the new, saved document, null for other types
     */
    public static Item toInvoice(Item source, int itemType, MPTableModel positions, Predicate<String> ask) {
        source.setIntstatus(Item.STATUS_FINISHED);
        source.save();
        Item i2;
        switch (itemType) {
            case Item.TYPE_INVOICE:
            case Item.TYPE_PART_PAYMENT:
            case Item.TYPE_DEPOSIT:
            case Item.TYPE_CREDIT:
                i2 = (Item) source.clone(contextOf(itemType));
                break;
            default:
                return null;
        }
        i2.setIntstatus(Item.STATUS_QUEUED);
        i2.setInttype(itemType);
        i2.setIDS(-1);
        i2.defineFormatHandler(new FormatHandler(i2));
        i2.save();
        try {
            ArrayList<ActivityList> data = mpv5.globals.Features.activityLists()
                    ? DatabaseObject.getObjects(Context.getActivityList(), new QueryCriteria("orderids", source.__getIDS()))
                    : new ArrayList<ActivityList>();
            if (!data.isEmpty() && ask.test(Messages.ActivityList_Existing.toString())) {
                Iterator<ActivityList> it = data.iterator();
                while (it.hasNext()) {
                    SubItem s = it.next().getDataForInvoice();
                    s.setOrdernr(positions.getRowCount());
                    s.setItemsids(i2.__getIDS());
                    positions.insertRow(positions.getRowCount(), s.getRowData(positions.getRowCount()));
                }
            }
        } catch (NodataFoundException ex) {
            Log.Debug(ItemService.class, ex.getMessage());
        }
        if (itemType == Item.TYPE_INVOICE
                && (source.__getInttype() == Item.TYPE_DEPOSIT || source.__getInttype() == Item.TYPE_PART_PAYMENT)) {
            SubItem.saveModelMinus(i2, positions, true, true, source);
        } else {
            SubItem.saveModel(i2, positions, true, true);
        }

        if (itemType == Item.TYPE_INVOICE && source.__getRefOrderIDS() != null) {
            int orderIDS = 0;
            if (source.__getInttype() == Item.TYPE_PART_PAYMENT || source.__getInttype() == Item.TYPE_DEPOSIT) {
                orderIDS = source.__getRefOrderIDS();
            } else if (source.__getInttype() == Item.TYPE_ORDER) {
                orderIDS = source.__getIDS();
            }
            ArrayList<Item> d1 = null;
            try {
                d1 = DatabaseObject.getObjects(Context.getDeposit(), new QueryCriteria("reforderids", orderIDS));
            } catch (NodataFoundException ex) {
                Log.Debug(ItemService.class, ex.getMessage());
            }
            ArrayList<Item> d2 = null;
            try {
                d2 = DatabaseObject.getObjects(Context.getPartPayment(), new QueryCriteria("reforderids", orderIDS));
            } catch (NodataFoundException ex) {
                Log.Debug(ItemService.class, ex.getMessage());
            }
            ArrayList<Item> data2 = null;
            if (d1 != null && d2 != null) {
                data2 = ArrayUtilities.merge(d1, d2);
            } else if (d1 != null) {
                data2 = d1;
            } else if (d2 != null) {
                data2 = d2;
            }
            if (data2 != null && data2.size() > 1 && ask.test(Messages.PartPayment_Existing.toString())) {
                for (Item i : data2) {
                    if (Objects.equals(i.__getIDS(), source.__getIDS())) {
                        continue;
                    }
                    SubItem.saveModelMinus(i2, SubItem.toModel(i.getSubitems()), true, true, i);
                }
            }
        }
        i2.setReforderids(source.__getIDS());
        return i2;
    }

    /**
     * The documents which refer to an order: invoices, part payments,
     * deposits and activity lists
     *
     * @param order
     * @return the documents
     */
    public static List<DatabaseObject> references(Item order) {
        List<DatabaseObject> list = new ArrayList<DatabaseObject>();
        if (order == null || order.__getIDS() == null) {
            return list;
        }
        for (Context c : new Context[]{Context.getInvoice(), Context.getPartPayment(), Context.getDeposit()}) {
            try {
                list.addAll(DatabaseObject.getObjects(c, new QueryCriteria("reforderids", order.__getIDS())));
            } catch (NodataFoundException ex) {
                Log.Debug(ItemService.class, ex.getMessage());
            }
        }
        if (mpv5.globals.Features.activityLists()) {
            try {
                list.addAll(DatabaseObject.getObjects(Context.getActivityList(), new QueryCriteria("orderids", order.__getIDS())));
            } catch (NodataFoundException ex) {
                Log.Debug(ItemService.class, ex.getMessage());
            }
        }
        return list;
    }

    /**
     * Compares an order with what has been invoiced of it (invoices, part
     * payments, activity lists), per product
     *
     * @param orderId the referenced order
     * @return rows: ordered, used, measure, description, percent, status
     * (used / ordered)
     */
    public static Object[][] compareToOrder(int orderId) {
        Map<Product, CompareRow> pm = new LinkedHashMap<Product, CompareRow>();
        try {
            DatabaseObject dob = DatabaseObject.getObject(Context.getOrder(), orderId);
            List<SubItem> data = DatabaseObject.getReferencedObjects(dob, Context.getSubItem(), new SubItem(), false);
            Collections.sort(data, SubItem.ORDER_COMPARATOR);
            for (SubItem s : data) {
                CompareRow r = row(pm, s, false);
                if (r != null) {
                    r.ordered = r.ordered.add(s.__getQuantityvalue());
                    r.measure = s.__getMeasure();
                    r.description = s.__getDescription();
                }
            }
        } catch (NodataFoundException ex) {
            Log.Debug(ItemService.class, ex);
            return new Object[0][];
        }
        for (Context c : new Context[]{Context.getInvoice(), Context.getPartPayment()}) {
            try {
                for (Object o : DatabaseObject.getObjects(c, new QueryCriteria("reforderids", orderId))) {
                    used(pm, ((Item) o).getSubitems());
                }
            } catch (NodataFoundException ex) {
                Log.Debug(ItemService.class, ex.getMessage());
            }
        }
        //activity lists are no items: their entries count by product
        //(the classic view failed with a ClassCastException here)
        if (mpv5.globals.Features.activityLists()) {
            try {
                for (Object o : DatabaseObject.getObjects(Context.getActivityList(), new QueryCriteria("orderids", orderId))) {
                    if (!(o instanceof ActivityList)) {
                        continue;
                    }
                    for (mpv5.db.objects.ActivityListSubItem a : ((ActivityList) o).getPositions()) {
                        try {
                            Product p = (Product) DatabaseObject.getObject(Context.getProduct(), a.__getProductsids());
                            CompareRow r = pm.get(p);
                            if (r == null) {
                                r = new CompareRow(p);
                                r.measure = a.__getMeasure();
                                r.description = a.__getDescription();
                                pm.put(p, r);
                            }
                            r.used = r.used.add(a.__getQuantityvalue());
                        } catch (NodataFoundException ex) {
                            Log.Debug(ItemService.class, ex.getMessage());
                        }
                    }
                }
            } catch (Exception ex) {
                Log.Debug(ItemService.class, ex.getMessage());
            }
        }
        Object[][] d = new Object[pm.size()][];
        int i = 0;
        for (CompareRow r : pm.values()) {
            d[i++] = r.toArray();
        }
        return d;
    }

    private static void used(Map<Product, CompareRow> pm, SubItem[] items) {
        for (SubItem si : items) {
            CompareRow r = row(pm, si, true);
            if (r != null) {
                r.used = r.used.add(si.__getQuantityvalue());
            }
        }
    }

    private static CompareRow row(Map<Product, CompareRow> pm, SubItem s, boolean fromUse) {
        try {
            Product p = (Product) DatabaseObject.getObject(Context.getProduct(), s.__getOriginalproductsids());
            CompareRow r = pm.get(p);
            if (r == null) {
                r = new CompareRow(p);
                if (fromUse) {
                    r.measure = s.__getMeasure();
                    r.description = s.__getDescription();
                }
                pm.put(p, r);
            }
            return r;
        } catch (NodataFoundException ex) {
            Log.Debug(ItemService.class, ex);
            return null;
        }
    }

    /**
     * A row of the comparison with the order
     */
    static final class CompareRow {

        BigDecimal ordered = BigDecimal.ZERO;
        BigDecimal used = BigDecimal.ZERO;
        String measure;
        String description;

        CompareRow(Product p) {
            this.description = p.__getDescription();
        }

        Object[] toArray() {
            float status = 0;
            if (ordered.signum() != 0) {
                status = used.divide(ordered, 2, RoundingMode.HALF_UP).floatValue();
            }
            return new Object[]{ordered, used, measure, description, (float) Math.round(status * 100f), status};
        }
    }
}
