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
import java.math.RoundingMode;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import mpv5.globals.Constants;
import mpv5.db.objects.Item;

/**
 * The bookkeeping of the journal, without database and user interface: which
 * entries count towards the result (cash basis, EÜR), the sums of income,
 * expenses, the document volume, the open items and the amounts by tax rate.
 */
public final class JournalCalc {

    /**
     * Status of a paid expense (the journal's own code, expenses have no item
     * status)
     */
    public static final int EXPENSE_PAID = 1000;
    /**
     * Status of an unpaid expense
     */
    public static final int EXPENSE_UNPAID = 2000;
    /**
     * Orders the rates; the unknown rate (null: a document without items)
     * comes last
     */
    public static final java.util.Comparator<BigDecimal> RATE_ORDER = java.util.Comparator.nullsLast(java.util.Comparator.<BigDecimal>naturalOrder());

    private JournalCalc() {
    }

    /**
     * One row of the journal
     */
    public static final class Entry {

        /**
         * the record (Item, Expense, Revenue), may be null in tests
         */
        public Object obj;
        /**
         * the id of the invoice (items and payments), 0 if none
         */
        public int itemId;
        /**
         * item type, {@link Constants#TYPE_REVENUE}, {@link Constants#TYPE_EXPENSE} or {@link Constants#TYPE_PAYMENT}
         */
        public int type;
        /**
         * item status, or {@link #EXPENSE_PAID} / {@link #EXPENSE_UNPAID}
         */
        public int status;
        public BigDecimal net = BigDecimal.ZERO;
        public BigDecimal tax = BigDecimal.ZERO;
        public BigDecimal gross = BigDecimal.ZERO;
        public BigDecimal discount = BigDecimal.ZERO;
        /**
         * the open amount (gross) of an unpaid document, null: the gross
         * amount
         */
        public BigDecimal open;
        public Date date;
        public String number = "";
        public String contact = "";
        public String prename = "";
        public String street = "";
        public String city = "";
        public String country = "";
        public String group = "";
        public String account = "";
        public String description = "";
        /**
         * the amounts by tax rate as stored with the record (expenses and
         * income: their rate; documents: their items, scaled to the document
         * amounts): rate in percent (null: unknown) - {net, tax}. Empty: one
         * amount of unknown rate.
         */
        public Map<BigDecimal, BigDecimal[]> rates = new TreeMap<BigDecimal, BigDecimal[]>(RATE_ORDER);
        /**
         * set by {@link #compute}: the effect on the result (net, income
         * positive, expenses negative), 0 if not counted
         */
        public BigDecimal effect = BigDecimal.ZERO;
        /**
         * set by {@link #compute}: true if the entry is part of income or
         * expenses
         */
        public boolean counted;

        /**
         * @return true for documents of the bookkeeping (invoices, credit
         * notes, deposits, part payments, income, expenses, payments), false
         * for offers, orders and others
         */
        public boolean isAccounting() {
            return JournalCalc.isAccounting(type);
        }

        /**
         * @return true for the income side (outgoing documents, income,
         * payments)
         */
        public boolean isIncomeSide() {
            return type != Constants.TYPE_EXPENSE;
        }
    }

    /**
     * @param type
     * @return true for the document types of the bookkeeping
     */
    public static boolean isAccounting(int type) {
        switch (type) {
            case Constants.TYPE_INVOICE:
            case Constants.TYPE_CREDIT:
            case Constants.TYPE_DEPOSIT:
            case Constants.TYPE_PART_PAYMENT:
            case Constants.TYPE_REVENUE:
            case Constants.TYPE_EXPENSE:
            case Constants.TYPE_PAYMENT:
                return true;
            default:
                return false;
        }
    }

    /**
     * The sums of a journal
     */
    public static final class Totals {

        public BigDecimal incomeNet = BigDecimal.ZERO;
        public BigDecimal incomeTax = BigDecimal.ZERO;
        public BigDecimal incomeGross = BigDecimal.ZERO;
        public BigDecimal expenseNet = BigDecimal.ZERO;
        public BigDecimal expenseTax = BigDecimal.ZERO;
        public BigDecimal expenseGross = BigDecimal.ZERO;
        public BigDecimal volumeNet = BigDecimal.ZERO;
        public BigDecimal volumeTax = BigDecimal.ZERO;
        public BigDecimal volumeGross = BigDecimal.ZERO;
        public BigDecimal discount = BigDecimal.ZERO;
        public BigDecimal openReceivables = BigDecimal.ZERO;
        public BigDecimal openPayables = BigDecimal.ZERO;
        /**
         * open offers and orders (not finished, paid or cancelled): net and
         * gross
         */
        public BigDecimal openOffersNet = BigDecimal.ZERO;
        public BigDecimal openOffersGross = BigDecimal.ZERO;
        public BigDecimal openOrdersNet = BigDecimal.ZERO;
        public BigDecimal openOrdersGross = BigDecimal.ZERO;
        /**
         * counted income by rate: rate (null: unknown) - {net, tax}
         */
        public final Map<BigDecimal, BigDecimal[]> incomeByRate = new TreeMap<BigDecimal, BigDecimal[]>(RATE_ORDER);
        /**
         * counted expenses by rate: rate (null: unknown) - {net, tax}
         */
        public final Map<BigDecimal, BigDecimal[]> expensesByRate = new TreeMap<BigDecimal, BigDecimal[]>(RATE_ORDER);

        public BigDecimal profitNet() {
            return incomeNet.subtract(expenseNet);
        }

        public BigDecimal profitTax() {
            return incomeTax.subtract(expenseTax);
        }

        public BigDecimal profitGross() {
            return incomeGross.subtract(expenseGross);
        }
    }

    /**
     * An amount of the journal query: exact, rounded to cents (the database
     * computes net - discount etc. in floating point)
     *
     * @param value
     * @return the amount, 0 if empty
     */
    public static BigDecimal money(Object value) {
        if (value == null || String.valueOf(value).trim().length() == 0 || "null".equals(String.valueOf(value))) {
            return BigDecimal.ZERO.setScale(2);
        }
        if (value instanceof BigDecimal) {
            return ((BigDecimal) value).setScale(2, RoundingMode.HALF_UP);
        }
        return new BigDecimal(String.valueOf(value).trim()).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * @param rate
     * @return the rate as key: without trailing zeros (19.00 and 19 are the
     * same), null stays null
     */
    public static BigDecimal rateKey(BigDecimal rate) {
        return rate == null ? null : rate.stripTrailingZeros().scale() < 0 ? rate.setScale(0) : rate.stripTrailingZeros();
    }

    /**
     * The rate a stored rate is booked under: the nearest rate of the control
     * panel if it is at most 0.1 percent points away, else the rate rounded
     * to one decimal place. Stored rates are not always exact (e.g. 18.96 or
     * 19.04 from imported or recalculated documents).
     *
     * @param rate the stored rate, null stays null
     * @param configured the rates of the control panel (Taxes)
     * @return the rate as key ({@link #rateKey})
     */
    public static BigDecimal normalizeRate(BigDecimal rate, java.util.Collection<BigDecimal> configured) {
        if (rate == null) {
            return null;
        }
        BigDecimal best = null;
        BigDecimal bestDistance = null;
        if (configured != null) {
            for (BigDecimal c : configured) {
                if (c == null) {
                    continue;
                }
                BigDecimal d = c.subtract(rate).abs();
                if (d.compareTo(new BigDecimal("0.1")) <= 0 && (bestDistance == null || d.compareTo(bestDistance) < 0)) {
                    best = c;
                    bestDistance = d;
                }
            }
        }
        return rateKey(best != null ? best : rate.setScale(1, RoundingMode.HALF_UP));
    }

    /**
     * Splits an amount in the proportion of weights, in cents, so that the
     * parts add up to the amount exactly (the remaining cents go to the parts
     * with the largest remainders)
     *
     * @param total
     * @param weights
     * @return the parts, in the order of the weights
     */
    public static List<BigDecimal> distribute(BigDecimal total, List<BigDecimal> weights) {
        List<BigDecimal> parts = new java.util.ArrayList<BigDecimal>();
        if (weights.isEmpty()) {
            return parts;
        }
        BigDecimal sum = BigDecimal.ZERO;
        for (BigDecimal w : weights) {
            sum = sum.add(w);
        }
        long cents = total.setScale(2, RoundingMode.HALF_UP).movePointRight(2).longValueExact();
        if (sum.signum() == 0) {
            //no proportion: all on the first part
            parts.add(BigDecimal.valueOf(cents, 2));
            for (int i = 1; i < weights.size(); i++) {
                parts.add(BigDecimal.ZERO.setScale(2));
            }
            return parts;
        }
        long[] base = new long[weights.size()];
        BigDecimal[] rest = new BigDecimal[weights.size()];
        long assigned = 0;
        for (int i = 0; i < base.length; i++) {
            BigDecimal exact = BigDecimal.valueOf(cents).multiply(weights.get(i)).divide(sum, 10, RoundingMode.HALF_UP);
            base[i] = exact.setScale(0, RoundingMode.FLOOR).longValueExact();
            rest[i] = exact.subtract(BigDecimal.valueOf(base[i]));
            assigned += base[i];
        }
        long left = cents - assigned;
        while (left != 0) {
            int best = 0;
            for (int i = 1; i < base.length; i++) {
                if (left > 0 ? rest[i].compareTo(rest[best]) > 0 : rest[i].compareTo(rest[best]) < 0) {
                    best = i;
                }
            }
            base[best] += left > 0 ? 1 : -1;
            rest[best] = rest[best].subtract(BigDecimal.valueOf(left > 0 ? 1 : -1));
            left += left > 0 ? -1 : 1;
        }
        for (long b : base) {
            parts.add(BigDecimal.valueOf(b, 2));
        }
        return parts;
    }

    /**
     * The amounts by rate of a record: its net and tax (as stored) split in
     * the proportion of the amounts of its lines (items) by rate
     *
     * @param net the stored net amount of the record
     * @param tax the stored tax amount of the record
     * @param lines rate - {net, tax} of the lines, may be empty
     * @return rate - {net, tax}, adding up to net and tax exactly; empty if
     * there are no lines
     */
    public static Map<BigDecimal, BigDecimal[]> split(BigDecimal net, BigDecimal tax, Map<BigDecimal, BigDecimal[]> lines) {
        Map<BigDecimal, BigDecimal[]> r = new TreeMap<BigDecimal, BigDecimal[]>(RATE_ORDER);
        if (lines == null || lines.isEmpty()) {
            return r;
        }
        List<BigDecimal> keys = new java.util.ArrayList<BigDecimal>(lines.keySet());
        List<BigDecimal> nw = new java.util.ArrayList<BigDecimal>();
        List<BigDecimal> tw = new java.util.ArrayList<BigDecimal>();
        for (BigDecimal k : keys) {
            nw.add(lines.get(k)[0]);
            tw.add(lines.get(k)[1]);
        }
        List<BigDecimal> n = distribute(net, nw);
        //the tax by the tax of the lines; without tax on the lines (e.g. all 0 %) by the net
        boolean noLineTax = true;
        for (BigDecimal w : tw) {
            noLineTax &= w.signum() == 0;
        }
        List<BigDecimal> t = distribute(tax, noLineTax ? nw : tw);
        for (int i = 0; i < keys.size(); i++) {
            r.put(keys.get(i), new BigDecimal[]{n.get(i), t.get(i)});
        }
        return r;
    }

    /**
     * Decides for each entry whether and how it counts (cash basis: paid
     * documents; credit notes and expenses reduce the result; invoices paid in
     * parts count through their payments) and sums up.
     *
     * @param entries the rows; {@link Entry#effect} and {@link Entry#counted}
     * are set
     * @param paidInParts ids of the invoices with recorded payments
     * @return the sums
     */
    public static Totals compute(List<Entry> entries, Set<Integer> paidInParts) {
        Totals t = new Totals();
        for (Entry e : entries) {
            e.effect = BigDecimal.ZERO;
            e.counted = false;
            boolean accounting = e.isAccounting();
            if (accounting) {
                if (e.type == Constants.TYPE_PAYMENT) {
                    income(t, e);
                } else if (e.type == Constants.TYPE_EXPENSE) {
                    if (e.status == EXPENSE_PAID) {
                        expense(t, e);
                    }
                } else if (e.status == Item.STATUS_PAID) {
                    if (e.type != Constants.TYPE_REVENUE && paidInParts != null && paidInParts.contains(e.itemId)) {
                        //paid in parts: the payments are the income (on their pay dates), not the invoice
                    } else if (e.type == Constants.TYPE_CREDIT) {
                        expense(t, e);
                    } else {
                        income(t, e);
                    }
                }
            }
            if (isOpenOfferOrOrder(e)) {
                if (e.type == Constants.TYPE_OFFER) {
                    t.openOffersNet = t.openOffersNet.add(e.net);
                    t.openOffersGross = t.openOffersGross.add(e.gross);
                } else {
                    t.openOrdersNet = t.openOrdersNet.add(e.net);
                    t.openOrdersGross = t.openOrdersGross.add(e.gross);
                }
            }
            //the volume: the documents of the bookkeeping except payments (part of their invoice)
            //and cancelled ones; credit notes and expenses reduce it
            if (accounting && e.type != Constants.TYPE_PAYMENT && !isCancelled(e)) {
                boolean minus = e.type == Constants.TYPE_CREDIT || e.type == Constants.TYPE_EXPENSE;
                t.volumeNet = add(t.volumeNet, e.net, minus);
                t.volumeTax = add(t.volumeTax, e.tax, minus);
                t.volumeGross = add(t.volumeGross, e.gross, minus);
                t.discount = add(t.discount, e.discount, minus);
            }
            //open items
            if (accounting && isOpen(e)) {
                BigDecimal open = e.open != null ? e.open : e.gross;
                if (e.type == Constants.TYPE_EXPENSE) {
                    t.openPayables = t.openPayables.add(open);
                } else {
                    t.openReceivables = add(t.openReceivables, open, e.type == Constants.TYPE_CREDIT);
                }
            }
        }
        return t;
    }

    /**
     * @param e
     * @return true if the entry is cancelled (expenses cannot be)
     */
    public static boolean isCancelled(Entry e) {
        return e.type != Constants.TYPE_EXPENSE && e.type != Constants.TYPE_PAYMENT && e.status == Item.STATUS_CANCELLED;
    }

    /**
     * @param e
     * @return true if the entry is neither paid nor cancelled
     */
    public static boolean isOpen(Entry e) {
        if (e.type == Constants.TYPE_PAYMENT) {
            return false;
        }
        if (e.type == Constants.TYPE_EXPENSE) {
            return e.status == EXPENSE_UNPAID;
        }
        return e.status != Item.STATUS_PAID && e.status != Item.STATUS_CANCELLED;
    }

    /**
     * @param e
     * @return true for an offer or order which is not finished, paid or
     * cancelled
     */
    public static boolean isOpenOfferOrOrder(Entry e) {
        return (e.type == Constants.TYPE_OFFER || e.type == Constants.TYPE_ORDER)
                && e.status != Item.STATUS_FINISHED && e.status != Item.STATUS_PAID && e.status != Item.STATUS_CANCELLED;
    }

    /**
     * @param e
     * @return true if paid (expenses, items, income; payments always)
     */
    public static boolean isPaid(Entry e) {
        if (e.type == Constants.TYPE_PAYMENT) {
            return true;
        }
        if (e.type == Constants.TYPE_EXPENSE) {
            return e.status == EXPENSE_PAID;
        }
        return e.status == Item.STATUS_PAID;
    }

    private static void income(Totals t, Entry e) {
        e.counted = true;
        e.effect = e.net;
        t.incomeNet = t.incomeNet.add(e.net);
        t.incomeTax = t.incomeTax.add(e.tax);
        t.incomeGross = t.incomeGross.add(e.gross);
        byRate(t.incomeByRate, e, false);
    }

    private static void expense(Totals t, Entry e) {
        e.counted = true;
        e.effect = e.net.negate();
        if (e.type == Constants.TYPE_CREDIT) {
            //a credit note reduces the income (and its output tax)
            t.incomeNet = t.incomeNet.subtract(e.net);
            t.incomeTax = t.incomeTax.subtract(e.tax);
            t.incomeGross = t.incomeGross.subtract(e.gross);
            byRate(t.incomeByRate, e, true);
            return;
        }
        t.expenseNet = t.expenseNet.add(e.net);
        t.expenseTax = t.expenseTax.add(e.tax);
        t.expenseGross = t.expenseGross.add(e.gross);
        byRate(t.expensesByRate, e, false);
    }

    private static void byRate(Map<BigDecimal, BigDecimal[]> map, Entry e, boolean minus) {
        Map<BigDecimal, BigDecimal[]> rates = e.rates;
        if (rates == null || rates.isEmpty()) {
            rates = new TreeMap<BigDecimal, BigDecimal[]>(RATE_ORDER);
            rates.put(null, new BigDecimal[]{e.net, e.tax});
        }
        for (Map.Entry<BigDecimal, BigDecimal[]> r : rates.entrySet()) {
            BigDecimal[] v = map.get(r.getKey());
            if (v == null) {
                v = new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO};
                map.put(r.getKey(), v);
            }
            v[0] = add(v[0], r.getValue()[0], minus);
            v[1] = add(v[1], r.getValue()[1], minus);
        }
    }

    private static BigDecimal add(BigDecimal sum, BigDecimal value, boolean minus) {
        return minus ? sum.subtract(value) : sum.add(value);
    }
}
