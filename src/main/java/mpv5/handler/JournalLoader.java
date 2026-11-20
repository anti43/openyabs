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
package mpv5.handler;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.db.common.NodataFoundException;
import mpv5.db.common.QueryCriteria2;
import mpv5.db.common.QueryHandler;
import mpv5.db.common.QueryParameter;
import mpv5.db.objects.Contact;
import mpv5.db.objects.Expense;
import mpv5.db.objects.Group;
import mpv5.db.objects.Item;
import mpv5.db.objects.Revenue;
import mpv5.db.objects.User;
import mpv5.globals.Constants;
import mpv5.globals.GlobalSettings;
import mpv5.handler.JournalCalc.Entry;
import mpv5.handler.JournalCalc.Totals;
import mpv5.logging.Log;
import mpv5.utils.date.DateConverter;
import mpv5.utils.date.vTimeframe;
import mpv5.utils.export.JournalAdviserPackage;
import mpv5.utils.text.TypeConversion;

/**
 * Loads journal entries (invoices, credit notes, deposits, part payments,
 * revenues, expenses, payments, offers, orders) with their amounts split by
 * tax rate and computes their {@link JournalCalc.Totals}.
 * <p>
 * Used by {@link mpv5.ui.panels.JournalPanel} and by plugins (e.g. DATEV
 * export).
 */
public final class JournalLoader {

    public static final int STATUS_ALL = 0;
    public static final int STATUS_PAID = 1;
    public static final int STATUS_OPEN = 2;
    public static final int STATUS_CANCELLED = 3;

    private JournalLoader() {
    }

    /**
     * Filter parameters for loading the journal
     */
    public static final class Filter {

        public vTimeframe time;
        public final List<Context> items = new ArrayList<Context>();
        /**
         * offers and orders: by document date, without the status filter of
         * the income
         */
        public final List<Context> others = new ArrayList<Context>();
        public boolean othersOpenOnly;
        public boolean revenues;
        public boolean expenses;
        public int incomeStatus;
        public int expenseStatus;
        public boolean incomeByPay;
        public boolean expenseByPay;
        public final List<Group> groups = new ArrayList<Group>();
        public boolean childGroups;
        public final Set<Integer> accounts = new LinkedHashSet<Integer>();
        public User user;
        public Contact contact;
        public boolean includeInvoices;
    }

    /**
     * The result of loading the journal
     */
    public static final class Loaded {

        public List<Entry> entries;
        public Totals totals;
        public Map<BigDecimal, String> labels;
    }

    /**
     * Loads the journal for the given filter: entries (with {@link Entry#rates}
     * populated and {@link Entry#counted} / {@link Entry#effect} set), totals
     * and tax rate labels.
     *
     * @param f the filter
     * @return the loaded journal
     * @throws Exception
     */
    public static Loaded load(Filter f) throws Exception {
        Set<Integer> groupIds = new HashSet<Integer>();
        for (Group g : f.groups) {
            groupIds.add(g.__getIDS());
            if (f.childGroups) {
                try {
                    for (Group c : g.getChildGroups()) {
                        groupIds.add(c.__getIDS());
                    }
                } catch (NodataFoundException e) {
                    Log.Debug(JournalLoader.class, e.getMessage());
                }
            }
        }
        List<Entry> list = new ArrayList<Entry>();
        String incomeDate = f.incomeByPay ? "dateend" : "dateadded"; //NOI18N
        for (Context c : f.items) {
            c.addReference(Context.getGroup());
            c.addReference(Context.getAccounts());
            c.addReference(Context.getContact());
            QueryCriteria2 p = criteria(c, f, groupIds);
            if (f.contact != null) {
                p.and(new QueryParameter(f.contact.getContext(), "ids", f.contact.__getIDS(), QueryParameter.EQUALS)); //NOI18N
            }
            for (Object[] r : select(c, Context.DETAILS_JOURNAL.replace("{date}", incomeDate), p, f.time, incomeDate)) { //NOI18N
                Entry e = entry(r, DatabaseObject.getObject(c, Integer.valueOf(r[0].toString())));
                if (matchesIncome(e, f.incomeStatus)) {
                    list.add(e);
                }
            }
        }
        for (Context c : f.others) {
            c.addReference(Context.getGroup());
            c.addReference(Context.getAccounts());
            c.addReference(Context.getContact());
            QueryCriteria2 p = criteria(c, f, groupIds);
            if (f.contact != null) {
                p.and(new QueryParameter(f.contact.getContext(), "ids", f.contact.__getIDS(), QueryParameter.EQUALS)); //NOI18N
            }
            for (Object[] r : select(c, Context.DETAILS_JOURNAL.replace("{date}", "dateadded"), p, f.time, "dateadded")) { //NOI18N
                Entry e = entry(r, DatabaseObject.getObject(c, Integer.valueOf(r[0].toString())));
                if (!f.othersOpenOnly || JournalCalc.isOpenOfferOrOrder(e)) {
                    list.add(e);
                }
            }
        }
        if (f.revenues) {
            Context c = Context.getRevenue();
            c.addReference(Context.getGroup());
            c.addReference(Context.getAccounts());
            c.addReference(Context.getContact());
            for (Object[] r : select(c, Context.DETAILS_JOURNAL2.replace("{date}", incomeDate), criteria(c, f, groupIds), f.time, incomeDate)) { //NOI18N
                r[10] = Constants.TYPE_REVENUE;
                Entry e = entry(r, DatabaseObject.getObject(c, Integer.valueOf(r[0].toString())));
                if (matchesIncome(e, f.incomeStatus)) {
                    list.add(e);
                }
            }
        }
        Set<Integer> paidInParts = paidInParts();
        if (f.includeInvoices && (f.incomeStatus == STATUS_ALL || f.incomeStatus == STATUS_PAID)) {
            list.addAll(payments(f, groupIds, paidInParts));
        }
        if (f.expenses) {
            Context c = Context.getExpense();
            c.addReference(Context.getGroup());
            c.addReference(Context.getAccounts());
            String date = f.expenseByPay ? "dateend" : "dateadded"; //NOI18N
            for (Object[] r : select(c, Context.DETAILS_JOURNAL3.replace("{date}", date), criteria(c, f, groupIds), f.time, date)) { //NOI18N
                r[10] = Constants.TYPE_EXPENSE;
                boolean paid = TypeConversion.stringToBoolean(String.valueOf(r[11]));
                r[11] = paid ? JournalCalc.EXPENSE_PAID : JournalCalc.EXPENSE_UNPAID;
                if (f.expenseStatus == STATUS_PAID && !paid || f.expenseStatus == STATUS_OPEN && paid) {
                    continue;
                }
                Entry e = entry(r, DatabaseObject.getObject(c, Integer.valueOf(r[0].toString())));
                e.contact = "";
                e.prename = "";
                e.street = "";
                e.city = "";
                e.country = "";
                list.add(e);
            }
        }
        list.sort((a, b) -> a.date == null || b.date == null ? 0 : a.date.compareTo(b.date));
        //open amounts of invoices paid in parts
        for (Entry e : list) {
            if (e.obj instanceof Item && paidInParts.contains(e.itemId) && JournalCalc.isOpen(e)) {
                try {
                    e.open = Payments.open((Item) e.obj);
                } catch (Exception ex) {
                    Log.Debug(JournalLoader.class, ex.getMessage());
                }
            }
        }
        Loaded l = new Loaded();
        l.labels = JournalAdviserPackage.rateLabels();
        rates(list, l.labels.keySet());
        l.entries = list;
        l.totals = JournalCalc.compute(list, paidInParts);
        return l;
    }

    /**
     * The whole journal of a period, without filters: all outgoing documents
     * (also offers and orders), income, expenses and payments, any status,
     * with their amounts by tax rate and whether they count.
     *
     * @param time the period
     * @param byPayDate true: by payment date (EÜR), false: by document date
     * @return the entries, sorted by date
     * @throws Exception
     */
    public static List<Entry> loadAll(vTimeframe time, boolean byPayDate) throws Exception {
        return loadAllResult(time, byPayDate).entries;
    }

    /**
     * Like {@link #loadAll(vTimeframe, boolean)}, returning the full
     * {@link Loaded} object (entries, totals and tax rate labels).
     *
     * @param time the period
     * @param byPayDate true: by payment date (EÜR), false: by document date
     * @return the loaded journal
     * @throws Exception
     */
    public static Loaded loadAllResult(vTimeframe time, boolean byPayDate) throws Exception {
        Filter f = new Filter();
        f.time = time;
        f.items.addAll(Arrays.asList(Context.getInvoice(), Context.getCredit(), Context.getDeposit(), Context.getPartPayment()));
        f.others.addAll(Arrays.asList(Context.getOrder(), Context.getOffer()));
        f.includeInvoices = true;
        f.revenues = true;
        f.expenses = true;
        f.incomeStatus = STATUS_ALL;
        f.expenseStatus = STATUS_ALL;
        f.incomeByPay = byPayDate;
        f.expenseByPay = byPayDate;
        return load(f);
    }

    /**
     * The amounts by tax rate as stored: expenses and income have one rate,
     * documents (and their payments) are split by the rates of their items.
     */
    private static void rates(List<Entry> list, Set<BigDecimal> configured) {
        Set<Integer> ids = new HashSet<Integer>();
        for (Entry e : list) {
            if (e.obj instanceof Expense) {
                e.rates.put(JournalCalc.normalizeRate(((Expense) e.obj).__getTaxpercentvalue(), configured), new BigDecimal[]{e.net, e.tax});
            } else if (e.obj instanceof Revenue) {
                e.rates.put(JournalCalc.normalizeRate(((Revenue) e.obj).__getTaxpercentvalue(), configured), new BigDecimal[]{e.net, e.tax});
            } else if (e.itemId > 0) {
                ids.add(e.itemId);
            }
        }
        Map<Integer, Map<BigDecimal, BigDecimal[]>> lines = itemRates(ids, configured);
        for (Entry e : list) {
            if (e.itemId > 0 && lines.containsKey(e.itemId)) {
                e.rates = JournalCalc.split(e.net, e.tax, lines.get(e.itemId));
            }
        }
    }

    /**
     * @param ids
     * @return the net and tax of the items of the documents by rate:
     * document - rate - {net, tax}
     */
    private static Map<Integer, Map<BigDecimal, BigDecimal[]>> itemRates(Set<Integer> ids, Set<BigDecimal> configured) {
        Map<Integer, Map<BigDecimal, BigDecimal[]>> m = new HashMap<Integer, Map<BigDecimal, BigDecimal[]>>();
        List<Integer> all = new ArrayList<Integer>(ids);
        for (int from = 0; from < all.size(); from += 500) {
            List<Integer> chunk = all.subList(from, Math.min(all.size(), from + 500));
            StringBuilder in = new StringBuilder();
            for (Integer id : chunk) {
                in.append(in.length() > 0 ? "," : "").append(id.intValue());
            }
            try {
                mpv5.db.common.ReturnValue rv = QueryHandler.instanceOf().clone(Context.getSubItem()).freeSelectQuery(
                        "SELECT itemsids, taxpercentvalue, SUM(totalnetvalue), SUM(totalbrutvalue) FROM subitems WHERE invisible = 0 AND itemsids IN (" //NOI18N
                        + in + ") GROUP BY itemsids, taxpercentvalue", mpv5.usermanagement.MPSecurityManager.VIEW, null); //NOI18N
                if (rv == null || !rv.hasData()) {
                    continue;
                }
                for (Object[] r : rv.getData()) {
                    Integer id = Integer.valueOf(String.valueOf(r[0]));
                    BigDecimal rate = JournalCalc.normalizeRate(new BigDecimal(String.valueOf(r[1])), configured);
                    BigDecimal net = JournalCalc.money(r[2]);
                    BigDecimal tax = JournalCalc.money(r[3]).subtract(net);
                    Map<BigDecimal, BigDecimal[]> rates = m.get(id);
                    if (rates == null) {
                        rates = new TreeMap<BigDecimal, BigDecimal[]>(JournalCalc.RATE_ORDER);
                        m.put(id, rates);
                    }
                    BigDecimal[] v = rates.get(rate);
                    rates.put(rate, v == null ? new BigDecimal[]{net, tax} : new BigDecimal[]{v[0].add(net), v[1].add(tax)});
                }
            } catch (Exception e) {
                Log.Debug(JournalLoader.class, e);
            }
        }
        return m;
    }

    private static boolean matchesIncome(Entry e, int status) {
        switch (status) {
            case STATUS_PAID:
                return e.status == Item.STATUS_PAID;
            case STATUS_OPEN:
                return e.status != Item.STATUS_PAID && e.status != Item.STATUS_CANCELLED;
            case STATUS_CANCELLED:
                return e.status == Item.STATUS_CANCELLED;
            default:
                return true;
        }
    }

    /**
     * Groups, accounts and the user: the filters of all records
     */
    private static QueryCriteria2 criteria(Context c, Filter f, Set<Integer> groupIds) {
        QueryCriteria2 p = new QueryCriteria2();
        List<QueryParameter> g = new ArrayList<QueryParameter>();
        for (Integer id : groupIds) {
            g.add(new QueryParameter(c, "groupsids", id, QueryParameter.EQUALS)); //NOI18N
        }
        or(p, g);
        List<QueryParameter> a = new ArrayList<QueryParameter>();
        for (Integer id : f.accounts) {
            a.add(new QueryParameter(c, "accountsids", id, QueryParameter.EQUALS)); //NOI18N
        }
        or(p, a);
        if (f.user != null) {
            p.and(new QueryParameter(c, "intaddedby", f.user.__getIDS(), QueryParameter.EQUALS)); //NOI18N
        }
        //qualified: groups, accounts and contacts (joined) have a dateadded too
        p.setOrder(c.getDbIdentity() + ".dateadded", true); //NOI18N
        return p;
    }

    private static void or(QueryCriteria2 p, List<QueryParameter> l) {
        if (l.size() == 1) {
            p.and(l.get(0));
        } else if (l.size() > 1) {
            p.or(l.get(0), l.subList(1, l.size()));
        }
    }

    private static Object[][] select(Context c, String columns, QueryCriteria2 p, vTimeframe t, String date) {
        try {
            return QueryHandler.instanceOf().clone(c).select(columns, p, t, date).getData();
        } catch (NodataFoundException e) {
            return new Object[0][17];
        }
    }

    private static Entry entry(Object[] r, Object obj) {
        Entry e = new Entry();
        e.obj = obj;
        e.itemId = obj instanceof Item ? ((Item) obj).__getIDS() : 0;
        e.contact = str(r[1]);
        e.prename = str(r[2]);
        e.street = str(r[3]);
        e.city = str(r[4]);
        e.country = str(r[5]);
        e.date = r[6] instanceof Date ? (Date) r[6] : DateConverter.getDate(String.valueOf(r[6]));
        e.group = str(r[7]);
        e.account = str(r[8]);
        e.number = str(r[9]);
        e.type = Integer.parseInt(String.valueOf(r[10]));
        e.status = Integer.parseInt(String.valueOf(r[11]));
        e.net = JournalCalc.money(r[12]);
        e.tax = JournalCalc.money(r[13]);
        e.gross = JournalCalc.money(r[14]);
        e.discount = JournalCalc.money(r[15]);
        switch (e.type) {
            case Constants.TYPE_REVENUE:
            case Constants.TYPE_EXPENSE:
            case Constants.TYPE_PAYMENT:
                e.description = str(r[16]);
                break;
            default:
                if (GlobalSettings.getBooleanProperty("org.openyabs.exportproperty.showitemasdesc")) { //NOI18N
                    e.description = String.valueOf(obj);
                }
        }
        return e;
    }

    private static String str(Object o) {
        return o == null || "null".equals(String.valueOf(o)) ? "" : String.valueOf(o); //NOI18N
    }

    private static Set<Integer> paidInParts() {
        Set<Integer> s = new HashSet<Integer>();
        if (!mpv5.db.common.DatabaseUpdater.hasTable("payments")) { //NOI18N
            return s;
        }
        try {
            mpv5.db.common.ReturnValue rv = QueryHandler.instanceOf().clone(Context.getInvoice()).freeSelectQuery(
                    "SELECT DISTINCT itemsids FROM payments", mpv5.usermanagement.MPSecurityManager.VIEW, null); //NOI18N
            if (rv != null && rv.hasData()) {
                for (Object[] r : rv.getData()) {
                    s.add(Integer.valueOf(String.valueOf(r[0])));
                }
            }
        } catch (Exception e) {
            Log.Debug(JournalLoader.class, e);
        }
        return s;
    }

    /**
     * @return the invoices with payments that are in the trash bin
     */
    private static Set<Integer> trashedInvoices() {
        Set<Integer> s = new HashSet<Integer>();
        try {
            mpv5.db.common.ReturnValue rv = QueryHandler.instanceOf().clone(Context.getInvoice()).freeSelectQuery(
                    "SELECT ids FROM items WHERE invisible = 1 AND ids IN (SELECT itemsids FROM payments)", //NOI18N
                    mpv5.usermanagement.MPSecurityManager.VIEW, null);
            if (rv != null && rv.hasData()) {
                for (Object[] r : rv.getData()) {
                    s.add(Integer.valueOf(String.valueOf(r[0])));
                }
            }
        } catch (Exception e) {
            Log.Debug(JournalLoader.class, e);
        }
        return s;
    }

    private static List<Entry> payments(Filter f, Set<Integer> groupIds, Set<Integer> paidInParts) {
        List<Entry> rows = new ArrayList<Entry>();
        if (paidInParts.isEmpty()) {
            return rows;
        }
        Set<Integer> trashed = trashedInvoices();
        for (Payments.Payment p : Payments.between(f.time.getStart(), f.time.getEnd())) {
            //the payments of an invoice in the trash bin are no income
            Item inv = trashed.contains(p.invoice) ? null : Payments.invoice(p.invoice);
            if (inv == null
                    || (f.contact != null && inv.__getContactsids() != f.contact.__getIDS())
                    || (!groupIds.isEmpty() && !groupIds.contains(inv.__getGroupsids()))
                    || (!f.accounts.isEmpty() && !f.accounts.contains(inv.__getAccountsids()))
                    || (f.user != null && inv.__getIntaddedby() != f.user.__getIDS())) {
                continue;
            }
            BigDecimal gross = inv.getGrossAmount();
            BigDecimal net = gross.signum() == 0 ? p.amount
                    : p.amount.multiply(inv.__getNetvalue().subtract(inv.__getDiscountvalue())).divide(gross, 2, java.math.RoundingMode.HALF_UP);
            Entry e = new Entry();
            e.obj = inv;
            e.itemId = inv.__getIDS();
            try {
                Contact k = inv.getContact();
                e.contact = k.__getCname();
                e.prename = k.__getPrename();
                e.street = k.__getStreet();
                e.city = k.__getCity();
                e.country = k.__getCountry();
            } catch (Exception ex) {
                Log.Debug(JournalLoader.class, ex.getMessage());
            }
            try {
                e.account = inv.getAccount().__getCname();
            } catch (Exception ex) {
                Log.Debug(JournalLoader.class, ex.getMessage());
            }
            e.group = inv.getGroup() == null ? "" : inv.getGroup().__getCname();
            e.date = p.date;
            e.number = inv.__getCnumber();
            e.type = Constants.TYPE_PAYMENT;
            e.status = Item.STATUS_PAID;
            e.net = net.setScale(2, java.math.RoundingMode.HALF_UP);
            e.gross = p.amount.setScale(2, java.math.RoundingMode.HALF_UP);
            e.tax = e.gross.subtract(e.net);
            e.description = p.description;
            rows.add(e);
        }
        return rows;
    }
}
