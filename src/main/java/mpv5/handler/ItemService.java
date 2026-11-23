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
     * @param contact
     * @return the invoices, deposit and part payment invoices of the contact a
     * credit note can correct ("number (date)"), the newest first
     */
    public static MPComboBoxModelItem[] referenceInvoiceChoices(Contact contact) {
        List<MPComboBoxModelItem> items = new ArrayList<MPComboBoxModelItem>();
        DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM);
        List<Item> bills = new ArrayList<Item>();
        for (int type : new int[]{Item.TYPE_INVOICE, Item.TYPE_DEPOSIT, Item.TYPE_PART_PAYMENT}) {
            try {
                for (DatabaseObject o : DatabaseObject.getObjects(contextOf(type), new QueryCriteria("contactsids", contact.__getIDS()))) {
                    bills.add((Item) o);
                }
            } catch (NodataFoundException e) {
                //none of this type
            }
        }
        bills.sort((a, b) -> Integer.compare(b.__getIDS(), a.__getIDS()));
        for (Item i : bills) {
            items.add(new MPComboBoxModelItem(i.__getIDS(), i.__getCnumber()
                    + (i.__getDateadded() == null ? "" : " (" + df.format(i.__getDateadded()) + ")")));
        }
        return items.toArray(new MPComboBoxModelItem[0]);
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
        Item i2 = (Item) source.clone(Context.getOrder());
        i2.setInttype(Item.TYPE_ORDER);
        i2.setIDS(-1);
        i2.defineFormatHandler(new FormatHandler(i2));
        if (!i2.save()) {
            return null;
        }
        source.setIntstatus(Item.STATUS_FINISHED);
        source.save();
        SubItem.saveModel(i2, positions, true, true);
        return i2;
    }

    /**
     * Ensures an invoice has status "finished" (abgeschlossen) and a plausible
     * end date, and saves the item if something changed. Orders, offers and
     * other non-invoice types are left untouched.
     *
     * @param item the item to finalize
     * @return true if the item is (now) finished with a valid end date
     * @see #ensureFinishedAndEndDate(Item, boolean)
     */
    public static boolean ensureFinishedAndEndDate(Item item) {
        return ensureFinishedAndEndDate(item, true);
    }

    /**
     * Ensures an invoice has status "finished" (abgeschlossen) and end date is set.
     * If end date is empty or older than the service date (datetodo / dateadded),
     * it is updated to the due date (if calculable and not before worked-on) or the worked-on date.
     * The method is idempotent: if nothing needs to change, nothing is saved,
     * so already finalized (locked) documents are not written again.
     *
     * @param item the item to finalize
     * @param save false if the caller saves the item itself
     * @return true if the item is (now) finished with a valid end date
     */
    public static boolean ensureFinishedAndEndDate(Item item, boolean save) {
        if (item == null || !isInvoiceLike(item.__getInttype())) {
            return false;
        }
        boolean changed = false;
        if (item.__getIntstatus() != Item.STATUS_FINISHED) {
            item.setIntstatus(Item.STATUS_FINISHED);
            changed = true;
        }
        Date workedOn = item.__getDatetodo() != null ? item.__getDatetodo()
                : (item.__getDateadded() != null ? item.__getDateadded() : new Date());
        Date end = item.__getDateend();
        if (end == null || end.before(workedOn)) {
            Date targetEnd = null;
            try {
                Contact contact = item.getContact();
                int payterm = contact != null ? contact.__getPayterm() : 0;
                if (payterm > 0) {
                    Date base = item.__getDateadded() != null ? item.__getDateadded() : workedOn;
                    Date due = DateConverter.addDays(base, payterm);
                    if (due != null && !due.before(workedOn)) {
                        targetEnd = due;
                    }
                }
            } catch (Exception ex) {
                Log.Debug(ItemService.class, ex);
            }
            item.setDateend(targetEnd != null ? targetEnd : workedOn);
            changed = true;
        }
        if (changed && save && item.__getIDS() > 0) {
            try {
                if (!item.save(true)) {
                    Log.Debug(ItemService.class, "Could not save finished status / end date of item " + item.__getIDS());
                    return false;
                }
            } catch (Exception ex) {
                Log.Debug(ItemService.class, ex);
                return false;
            }
        }
        return true;
    }

    private static boolean isInvoiceLike(int type) {
        return isInvoiceType(type);
    }

    /**
     * The kinds of documents which are invoices in the sense of the VAT law:
     * invoice, credit note (correction, cancellation), deposit and part
     * payment invoice. Their numbers must be unique among all of them (§ 14
     * Abs. 4 Nr. 4 UStG), #212.
     */
    public static final int[] INVOICE_TYPES = {Item.TYPE_INVOICE, Item.TYPE_CREDIT, Item.TYPE_DEPOSIT, Item.TYPE_PART_PAYMENT};

    /**
     * @param type
     * @return true for the kinds of {@link #INVOICE_TYPES}
     */
    public static boolean isInvoiceType(int type) {
        for (int t : INVOICE_TYPES) {
            if (t == type) {
                return true;
            }
        }
        return false;
    }

    /**
     * @return the {@link #INVOICE_TYPES} for SQL, e.g. "0, 20, 22, 21"
     */
    public static String invoiceTypesSql() {
        StringBuilder b = new StringBuilder();
        for (int t : INVOICE_TYPES) {
            b.append(b.length() == 0 ? "" : ", ").append(t);
        }
        return b.toString();
    }

    /**
     * The document of one of the {@link #INVOICE_TYPES} with this number
     *
     * @param number
     * @param exceptIds a document not to count (the one being numbered), &lt;= 0 for none
     * @return its kind and id ({inttype, ids}), null if the number is free
     */
    public static int[] invoiceNumberUsedBy(String number, int exceptIds) {
        if (number == null || number.trim().isEmpty()) {
            return null;
        }
        String q = number.trim().replace("'", "''");
        try {
            mpv5.db.common.ReturnValue rv = mpv5.db.common.QueryHandler.getConnection().freeQuery(
                    "SELECT inttype, ids FROM " + Context.getInvoice().getDbIdentity() + " WHERE cnumber IN ('" + q + "', '" + FormatHandler.DELETED_IDENTIFIER + q + "') AND inttype IN ("
                    + invoiceTypesSql() + ") AND ids <> " + exceptIds, mpv5.usermanagement.MPSecurityManager.VIEW, null);
            if (rv.hasData()) {
                Object[] r = rv.getData()[0];
                return new int[]{Integer.parseInt(String.valueOf(r[0])), Integer.parseInt(String.valueOf(r[1]))};
            }
        } catch (Exception e) {
            Log.Debug(ItemService.class, e.getMessage());
        }
        return null;
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
        //a credit note from an invoice refers to it (BT-25 of the e-invoice, #211)
        i2.setRefinvoiceids(itemType == Item.TYPE_CREDIT && isBill(source.__getInttype()) ? source.__getIDS() : 0);
        i2.setIDS(-1);
        i2.defineFormatHandler(new FormatHandler(i2));
        if (!i2.save()) {
            return null;
        }
        source.setIntstatus(Item.STATUS_FINISHED);
        source.save();
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
        //positions saved so far: the deductions follow them
        int next;
        if (itemType == Item.TYPE_INVOICE
                && (source.__getInttype() == Item.TYPE_DEPOSIT || source.__getInttype() == Item.TYPE_PART_PAYMENT)) {
            //final invoice from a deposit / part payment: the positions of the order,
            //minus this deposit (and, if wanted, the other ones below)
            Item order = referencedOrder(source);
            next = 0;
            if (order != null) {
                next = SubItem.saveModel(i2, SubItem.toModel(copyPositions(order), true), true, true).size();
            }
            //the deposit / part payment itself (as invoiced: its saved positions)
            next += deduct(i2, source, next, ask);
        } else {
            next = SubItem.saveModel(i2, positions, true, true).size();
        }

        //the order whose deposits / part payments are deducted: the source itself,
        //or the order a deposit / part payment refers to
        int orderIDS = 0;
        if (source.__getInttype() == Item.TYPE_ORDER) {
            orderIDS = source.__getIDS();
        } else if ((source.__getInttype() == Item.TYPE_PART_PAYMENT || source.__getInttype() == Item.TYPE_DEPOSIT)
                && source.__getRefOrderIDS() != null) {
            orderIDS = source.__getRefOrderIDS();
        }
        if (itemType == Item.TYPE_INVOICE && orderIDS > 0) {
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
            //earlier deposits / part payments of the order (the source itself excluded)
            int others = 0;
            if (data2 != null) {
                for (Item i : data2) {
                    if (!Objects.equals(i.__getIDS(), source.__getIDS())) {
                        others++;
                    }
                }
            }
            if (others > 0 && ask.test(Messages.PartPayment_Existing.toString())) {
                for (Item i : data2) {
                    if (Objects.equals(i.__getIDS(), source.__getIDS())) {
                        continue;
                    }
                    next += deduct(i2, i, next, ask);
                }
            }
        }
        i2.setReforderids(source.__getIDS());
        //the totals of the saved positions (the clone has those of the source)
        updateTotals(i2);
        return i2;
    }

    /**
     * Cancels an invoice (also a deposit or part payment invoice): a
     * cancellation - a credit note over the full amount that refers to it -
     * is created, the invoice gets the status "cancelled". The invoice itself
     * is not changed or deleted otherwise.
     *
     * @param invoice the saved invoice, with the data of the view
     * @param positions the positions of the view
     * @return the cancellation (saved), null if not possible
     */
    public static Item cancel(Item invoice, MPTableModel positions) {
        if (invoice == null || !invoice.isExisting() || !isBill(invoice.__getInttype())
                || invoice.__getIntstatus() == Item.STATUS_CANCELLED) {
            return null;
        }
        //no questions (activity lists, deductions): the cancellation is the invoice as it is
        Item storno = toInvoice(invoice, Item.TYPE_CREDIT, positions, q -> false);
        if (storno == null) {
            return null;
        }
        storno.setDateadded(new java.util.Date());
        storno.setDescription(String.format(Messages.ITEM_CANCELLATION_OF.toString(), invoice.__getCnumber()));
        storno.save();
        invoice.setIntstatus(Item.STATUS_CANCELLED);
        invoice.save();
        return storno;
    }

    /**
     * Deducts a deposit / part payment invoice in a final invoice (§ 14 Abs. 5
     * Satz 2 UStG, #221): one position per VAT rate with the net after
     * discount, named with the kind, number and date of the deposit ("Less
     * deposit invoice AZ-2026-00001 of 01.10.2026"), marked if it is not paid.
     * Asks before deducting a deposit which is not paid or was deducted in
     * another final invoice already. The deposit remembers the final invoice
     * (items.finalinvoiceids, also for the e-invoice: BG-3).
     *
     * @param finalInvoice the saved final invoice
     * @param deposit the deposit / part payment invoice
     * @param next the number of the first position
     * @param ask yes/no question
     * @return the number of positions added, 0 if not deducted
     */
    static int deduct(Item finalInvoice, Item deposit, int next, Predicate<String> ask) {
        String type = Item.getTypeString(deposit.__getInttype());
        String number = deposit.__getCnumber();
        DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM);
        String date = deposit.__getDateadded() == null ? "" : df.format(deposit.__getDateadded());
        Integer before = deposit.__getFinalinvoiceids();
        if (before != null && before.intValue() != finalInvoice.__getIDS()) {
            String other = null;
            try {
                other = ((Item) DatabaseObject.getObject(Context.getInvoice(), before)).__getCnumber();
            } catch (Exception e) {
                //the final invoice was deleted for good: deduct as if never deducted
            }
            if (other != null && !other.startsWith(FormatHandler.DELETED_IDENTIFIER)
                    && !ask.test(String.format(Messages.FINAL_DEPOSIT_DEDUCTED_ASK.toString(), type, number, other))) {
                return 0;
            }
        }
        BigDecimal open = BigDecimal.ZERO;
        try {
            open = nz(Payments.open(deposit));
        } catch (Exception e) {
            Log.Debug(ItemService.class, e.getMessage());
        }
        boolean unpaid = deposit.__getIntstatus() != Item.STATUS_PAID && open.signum() > 0;
        if (unpaid && !ask.test(String.format(Messages.FINAL_DEPOSIT_UNPAID_ASK.toString(), type, number,
                mpv5.utils.numberformat.FormatNumber.formatLokalCurrency(open)))) {
            return 0;
        }
        String text = String.format(Messages.FINAL_DEDUCTION.toString(), type, number, date)
                + (unpaid ? Messages.FINAL_DEDUCTION_UNPAID.toString() : "");
        int n = 0;
        for (SubItem s : asPositions(deposit, text)) {
            BigDecimal net = nz(s.__getExternalvalue());
            s.setExternalvalue(net.negate());
            s.setInternalvalue(net.negate());
            s.setItemsids(finalInvoice.__getIDS());
            s.setOrdernr(next + n);
            //the position number goes on (it restarted at 1 for each deposit)
            s.setCountvalue(BigDecimal.valueOf(next + n + 1));
            s.setDatedelivery(finalInvoice.__getDatetodo());
            s.setDateadded(new Date());
            s.setGroupsids(finalInvoice.__getGroupsids());
            SubItem.calculate(s);
            s.save(true);
            n++;
        }
        //remember the final invoice (a column of its own, set without saving the deposit:
        //no save hooks on a finalized deposit, not part of the GoBD seal)
        mpv5.db.common.QueryData d = new mpv5.db.common.QueryData();
        d.add("finalinvoiceids", finalInvoice.__getIDS());
        mpv5.db.common.QueryHandler.instanceOf().clone(contextOf(deposit.__getInttype()))
                .update(d, new String[]{"ids", String.valueOf(deposit.__getIDS()), ""}, null);
        deposit.setFinalinvoiceids(finalInvoice.__getIDS());
        return n;
    }

    /**
     * @param finalInvoice
     * @return the deposits / part payment invoices deducted in the final
     * invoice, the oldest first (#221)
     */
    public static List<Item> deductedIn(Item finalInvoice) {
        List<Item> l = new ArrayList<Item>();
        if (finalInvoice == null || finalInvoice.__getIDS() <= 0) {
            return l;
        }
        for (int t : new int[]{Item.TYPE_DEPOSIT, Item.TYPE_PART_PAYMENT}) {
            try {
                for (DatabaseObject o : DatabaseObject.getObjects(contextOf(t), new QueryCriteria("finalinvoiceids", finalInvoice.__getIDS()))) {
                    l.add((Item) o);
                }
            } catch (NodataFoundException e) {
                //none of this kind
            }
        }
        l.sort((a, b) -> Integer.compare(a.__getIDS(), b.__getIDS()));
        return l;
    }

    /**
     * @return the order a deposit / part payment refers to, null if none
     */
    private static Item referencedOrder(Item source) {
        Integer id = source.__getRefOrderIDS();
        if (id == null || id <= 0) {
            return null;
        }
        try {
            return (Item) DatabaseObject.getObject(Context.getOrder(), id);
        } catch (NodataFoundException e) {
            Log.Debug(ItemService.class, e.getMessage());
            return null;
        }
    }

    /**
     * Sets net, discount and VAT of the document from its saved positions
     * (EN 16931, as the view and the e-invoice compute them) and saves it
     *
     * @param item
     */
    public static void updateTotals(Item item) {
        List<mpv5.utils.numberformat.InvoiceMath.Line> lines = new ArrayList<mpv5.utils.numberformat.InvoiceMath.Line>();
        BigDecimal discountGross = BigDecimal.ZERO;
        for (SubItem si : item.getSubitems()) {
            lines.add(new mpv5.utils.numberformat.InvoiceMath.Line(si.effectiveQuantity(), si.__getExternalvalue(), si.__getDiscount(), si.__getTaxpercentvalue()));
            BigDecimal d = mpv5.utils.numberformat.InvoiceMath.lineDiscount(si.effectiveQuantity(), si.__getExternalvalue(), si.__getDiscount());
            discountGross = discountGross.add(mpv5.utils.numberformat.InvoiceMath.round2(
                    d.multiply(BigDecimal.ONE.add(mpv5.utils.numberformat.InvoiceMath.rate(si.__getTaxpercentvalue()).movePointLeft(2)))));
        }
        mpv5.utils.numberformat.InvoiceMath.Totals t = mpv5.utils.numberformat.InvoiceMath.totals(lines);
        item.setNetvalue(t.amount);
        item.setDiscountvalue(t.discount);
        item.setTaxvalue(t.tax);
        item.setDiscountGrosvalue(discountGross);
        item.save(true);
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

    /**
     * The positions of a document as new positions (e.g. for a new document
     * made from it): quantity, unit, price, VAT rate, discount, product and
     * texts as they are - only not linked to the source
     *
     * @param source
     * @return new positions in the order of the source
     */
    public static SubItem[] copyPositions(Item source) {
        SubItem[] src = source.getSubitems();
        SubItem[] out = new SubItem[src.length];
        for (int i = 0; i < src.length; i++) {
            out[i] = copyPosition(src[i]);
            out[i].setOrdernr(i);
        }
        return out;
    }

    /**
     * @param si
     * @return a new, unsaved copy of the position
     */
    public static SubItem copyPosition(SubItem si) {
        SubItem c = new SubItem();
        c.setCname(si.getCname());
        c.setDescription(si.__getDescription());
        c.setMeasure(si.__getMeasure());
        c.setLinkurl(si.__getLinkurl());
        c.setCountvalue(si.__getCountvalue());
        c.setQuantityvalue(si.__getQuantityvalue());
        c.setInttype(si.getInttype());
        c.setExternalvalue(si.__getExternalvalue());
        c.setInternalvalue(si.__getInternalvalue());
        c.setTaxpercentvalue(si.__getTaxpercentvalue());
        c.setDiscount(si.__getDiscount());
        c.setOriginalproductsids(si.__getOriginalproductsids());
        c.setDatedelivery(si.__getDatedelivery());
        c.setIDS(-1);
        return c;
    }

    /**
     * A whole document as position(s) of another document: one position per
     * VAT rate with the net amount after discount, so that net, VAT and gross
     * stay exactly those of the source (also with mixed VAT rates)
     *
     * @param source
     * @param description the text of the position(s)
     * @return at least one position
     */
    public static List<SubItem> asPositions(Item source, String description) {
        return asPositions(source.getSubitems(), source.__getCname(), source.__getNetvalue(),
                source.__getDiscountvalue(), source.__getTaxvalue(), description);
    }

    /**
     * @param positions the positions of the document
     * @param cname the name of the document
     * @param netvalue stored net (before discount), used without positions
     * @param discountvalue stored discount, used without positions
     * @param taxvalue stored VAT, used without positions
     * @param description the text of the position(s)
     * @return see {@link #asPositions(Item, String)}
     */
    static List<SubItem> asPositions(SubItem[] positions, String cname, BigDecimal netvalue,
            BigDecimal discountvalue, BigDecimal taxvalue, String description) {
        List<mpv5.utils.numberformat.InvoiceMath.Line> lines = new ArrayList<mpv5.utils.numberformat.InvoiceMath.Line>();
        for (SubItem si : positions) {
            lines.add(new mpv5.utils.numberformat.InvoiceMath.Line(si.effectiveQuantity(), si.__getExternalvalue(), si.__getDiscount(), si.__getTaxpercentvalue()));
        }
        Map<BigDecimal, BigDecimal[]> byRate = new LinkedHashMap<BigDecimal, BigDecimal[]>();
        for (Map.Entry<BigDecimal, BigDecimal[]> e : mpv5.utils.numberformat.InvoiceMath.totals(lines).byRate.entrySet()) {
            if (e.getValue()[0].signum() != 0) {
                byRate.put(e.getKey(), e.getValue());
            }
        }
        List<SubItem> out = new ArrayList<SubItem>();
        if (byRate.isEmpty()) {
            //no positions (or only text): the stored totals
            BigDecimal net = nz(netvalue).subtract(nz(discountvalue));
            BigDecimal rate = net.signum() == 0 ? BigDecimal.ZERO
                    : nz(taxvalue).multiply(new BigDecimal(100)).divide(net, 2, RoundingMode.HALF_UP);
            byRate.put(rate, new BigDecimal[]{net, nz(taxvalue)});
        }
        for (Map.Entry<BigDecimal, BigDecimal[]> e : byRate.entrySet()) {
            SubItem s = new SubItem();
            s.setCname(cname);
            s.setDescription(byRate.size() > 1
                    ? description + " (" + e.getKey().stripTrailingZeros().toPlainString() + " %)"
                    : description);
            s.setQuantityvalue(BigDecimal.ONE);
            s.setExternalvalue(e.getValue()[0]);
            s.setInternalvalue(e.getValue()[0]);
            s.setTaxpercentvalue(e.getKey());
            s.setIDS(-1);
            out.add(s);
        }
        return out;
    }

    private static BigDecimal nz(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }

    /**
     * The staggered price for a new quantity, unless the user typed a price of
     * their own
     *
     * @param tiers {minimum quantity, price} of the product
     * @param normalPrice the price of the product without tier
     * @param quantity the new quantity
     * @param currentPrice the price of the position now
     * @return the new price, or null to keep the current one (it is not one of
     * the product's prices, or it is already right)
     */
    public static BigDecimal staggeredPrice(List<BigDecimal[]> tiers, BigDecimal normalPrice, BigDecimal quantity, BigDecimal currentPrice) {
        if (tiers.isEmpty()) {
            return null;
        }
        boolean productPrice = currentPrice == null || currentPrice.signum() == 0
                || (normalPrice != null && normalPrice.compareTo(currentPrice) == 0);
        for (BigDecimal[] t : tiers) {
            productPrice |= t[1] != null && t[1].compareTo(currentPrice) == 0;
        }
        if (!productPrice) {
            return null;
        }
        BigDecimal p = Product.priceFor(tiers, quantity, normalPrice);
        return p == null || (currentPrice != null && p.compareTo(currentPrice) == 0) ? null : p;
    }

    /**
     * Keeps the prices of the positions with a product at the staggered price
     * of their quantity (column 2 quantity, 5 price, 10 product): when the
     * quantity changes, the price follows - not if the user typed another price
     *
     * @param model the positions
     */
    public static void followStaggeredPrices(final MPTableModel model) {
        for (javax.swing.event.TableModelListener l : model.getTableModelListeners()) {
            if (l instanceof StaggeredPrices) {
                return;
            }
        }
        model.addTableModelListener(new StaggeredPrices(model));
    }

    private static final class StaggeredPrices implements javax.swing.event.TableModelListener {

        private final MPTableModel model;

        StaggeredPrices(MPTableModel model) {
            this.model = model;
        }

        @Override
        public void tableChanged(javax.swing.event.TableModelEvent e) {
            if (e.getType() != javax.swing.event.TableModelEvent.UPDATE || e.getColumn() != 2
                    || e.getFirstRow() < 0 || e.getLastRow() >= model.getRowCount()) {
                return;
            }
            for (int r = e.getFirstRow(); r <= e.getLastRow(); r++) {
                Object product = model.getValueAt(r, 10);
                Object quantity = model.getValueAt(r, 2);
                if (!(product instanceof Product) || !(quantity instanceof Number)) {
                    continue;
                }
                Product p = (Product) product;
                List<BigDecimal[]> tiers = new ArrayList<BigDecimal[]>();
                for (mpv5.db.objects.ProductPrice pp : p.findProductPrices()) {
                    tiers.add(new BigDecimal[]{pp.getMincountvalue(), pp.getExternalnetvalue()});
                }
                Object price = model.getValueAt(r, 5);
                BigDecimal now = price instanceof Number ? mpv5.utils.numberformat.FormatNumber.getBigDecimal(price) : null;
                BigDecimal next = staggeredPrice(tiers, p.__getExternalnetvalue(),
                        mpv5.utils.numberformat.FormatNumber.getBigDecimal(quantity), now);
                if (next != null) {
                    model.setValueAt(next, r, 5);
                }
            }
        }
    }

    /**
     * Appends a new empty row whenever a value is entered into the last row of
     * a positions table so there is always a free row at the bottom to add text
     * to.
     *
     * @param model the positions
     */
    public static void keepTrailingEmptyRow(final MPTableModel model) {
        keepTrailingEmptyRow(model, () -> true, null);
    }

    /**
     * Appends a new empty row whenever a value is entered into the last row of
     * a positions table (when editable) so there is always a free row at the
     * bottom to add text to.
     *
     * @param model the positions
     * @param editable whether the table is currently editable
     * @param onRowAdded optional callback invoked with the index of the newly
     * added row
     */
    public static void keepTrailingEmptyRow(final MPTableModel model,
            final java.util.function.BooleanSupplier editable,
            final java.util.function.IntConsumer onRowAdded) {
        for (javax.swing.event.TableModelListener l : model.getTableModelListeners()) {
            if (l instanceof TrailingEmptyRow) {
                return;
            }
        }
        model.addTableModelListener(new TrailingEmptyRow(model, editable, onRowAdded));
    }

    private static final class TrailingEmptyRow implements javax.swing.event.TableModelListener {

        private final MPTableModel model;
        private final java.util.function.BooleanSupplier editable;
        private final java.util.function.IntConsumer onRowAdded;
        private boolean adding;

        TrailingEmptyRow(MPTableModel model,
                java.util.function.BooleanSupplier editable,
                java.util.function.IntConsumer onRowAdded) {
            this.model = model;
            this.editable = editable != null ? editable : () -> true;
            this.onRowAdded = onRowAdded;
        }

        @Override
        public void tableChanged(javax.swing.event.TableModelEvent e) {
            if (adding || e.getType() != javax.swing.event.TableModelEvent.UPDATE) {
                return;
            }
            int rowCount = model.getRowCount();
            if (rowCount <= 0) {
                return;
            }
            int lastRow = rowCount - 1;
            if (e.getFirstRow() < 0 || e.getLastRow() < lastRow || e.getFirstRow() > lastRow) {
                return;
            }
            if (!editable.getAsBoolean() || !model.isCellEditable(lastRow, 4)) {
                return;
            }
            if (!hasEnteredValue(model, lastRow, e.getColumn())) {
                return;
            }
            adding = true;
            try {
                model.addRow(1);
                if (onRowAdded != null) {
                    onRowAdded.accept(model.getRowCount() - 1);
                }
            } finally {
                adding = false;
            }
        }

        private static boolean hasEnteredValue(MPTableModel model, int row, int col) {
            if (col == javax.swing.event.TableModelEvent.ALL_COLUMNS) {
                return hasText(model, row, 4) || hasNonZeroNumber(model, row, 5)
                        || hasProduct(model, row) || hasText(model, row, 14)
                        || hasNonZeroNumber(model, row, 15);
            }
            switch (col) {
                case 4:
                case 14:
                    return hasText(model, row, col);
                case 5:
                case 15:
                    return hasNonZeroNumber(model, row, col);
                case 10:
                    return hasProduct(model, row);
                case 3:
                    return hasNonDefaultUnit(model, row);
                case 2:
                    return hasNonDefaultQuantity(model, row);
                default:
                    return false;
            }
        }

        private static boolean hasText(MPTableModel model, int row, int col) {
            if (col >= model.getColumnCount()) {
                return false;
            }
            Object v = model.getValueAt(row, col);
            return v != null && !v.toString().trim().isEmpty();
        }

        private static boolean hasNonZeroNumber(MPTableModel model, int row, int col) {
            if (col >= model.getColumnCount()) {
                return false;
            }
            Object v = model.getValueAt(row, col);
            if (v == null) {
                return false;
            }
            try {
                BigDecimal n = v instanceof Number
                        ? mpv5.utils.numberformat.FormatNumber.getBigDecimal(v)
                        : mpv5.utils.numberformat.FormatNumber.parseDezimal(v.toString());
                return n != null && n.signum() != 0;
            } catch (Exception ex) {
                return false;
            }
        }

        private static boolean hasProduct(MPTableModel model, int row) {
            if (10 >= model.getColumnCount()) {
                return false;
            }
            Object v = model.getValueAt(row, 10);
            if (v == null) {
                return false;
            }
            if (v instanceof Product) {
                return true;
            }
            if (v instanceof Number) {
                return ((Number) v).intValue() > 0;
            }
            return !v.toString().trim().isEmpty();
        }

        private static boolean hasNonDefaultUnit(MPTableModel model, int row) {
            if (3 >= model.getColumnCount()) {
                return false;
            }
            Object v = model.getValueAt(row, 3);
            if (v == null) {
                return false;
            }
            String s = v.toString().trim();
            if (s.isEmpty()) {
                return false;
            }
            String def = "";
            try {
                if (User.getCurrentUser().getProperties().hasProperty("defunit")) {
                    String d = User.getCurrentUser().getProperties().getProperty("defunit");
                    def = d == null ? "" : d.trim();
                }
            } catch (Exception ignored) {
            }
            return !s.equals(def);
        }

        private static boolean hasNonDefaultQuantity(MPTableModel model, int row) {
            if (2 >= model.getColumnCount()) {
                return false;
            }
            Object v = model.getValueAt(row, 2);
            if (v == null) {
                return false;
            }
            BigDecimal q;
            try {
                q = v instanceof Number
                        ? mpv5.utils.numberformat.FormatNumber.getBigDecimal(v)
                        : mpv5.utils.numberformat.FormatNumber.parseDezimal(v.toString());
            } catch (Exception ex) {
                return false;
            }
            if (q == null || q.signum() == 0) {
                return false;
            }
            BigDecimal def = BigDecimal.ONE;
            try {
                if (User.getCurrentUser().getProperties().hasProperty("defcount")) {
                    def = BigDecimal.valueOf(User.getCurrentUser().getProperties().getProperty("defcount", 0d));
                }
            } catch (Exception ignored) {
            }
            return q.compareTo(def) != 0;
        }
    }
}
