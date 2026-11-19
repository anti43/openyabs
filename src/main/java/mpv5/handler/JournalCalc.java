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
     * The tax rates the amounts are sorted to (percent); others are "mixed"
     */
    public static final int[] RATES = {0, 5, 7, 16, 19};
    /**
     * Key of the amounts whose tax does not fit one rate (several rates in one
     * document)
     */
    public static final int MIXED = -1;

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
         * counted income by rate: rate (or {@link #MIXED}) - {net, tax}
         */
        public final Map<Integer, BigDecimal[]> incomeByRate = new TreeMap<Integer, BigDecimal[]>();
        /**
         * counted expenses by rate: rate (or {@link #MIXED}) - {net, tax}
         */
        public final Map<Integer, BigDecimal[]> expensesByRate = new TreeMap<Integer, BigDecimal[]>();

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
     * The tax rate of an amount: the rate of {@link #RATES} the tax fits (half
     * a cent per line tolerance: 0.3 percent points), else {@link #MIXED}
     *
     * @param net
     * @param tax
     * @return the rate in percent or {@link #MIXED}
     */
    public static int rateOf(BigDecimal net, BigDecimal tax) {
        if (tax == null || tax.signum() == 0) {
            return 0;
        }
        if (net == null || net.signum() == 0) {
            return MIXED;
        }
        double r = tax.doubleValue() / net.doubleValue() * 100d;
        for (int rate : RATES) {
            if (Math.abs(r - rate) < 0.3d) {
                return rate;
            }
        }
        return MIXED;
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
            //the volume: all shown documents except payments (part of their invoice) and cancelled ones;
            //credit notes and expenses reduce it
            if (e.type != Constants.TYPE_PAYMENT && !isCancelled(e)) {
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

    private static void byRate(Map<Integer, BigDecimal[]> map, Entry e, boolean minus) {
        int rate = rateOf(e.net, e.tax);
        BigDecimal[] v = map.get(rate);
        if (v == null) {
            v = new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO};
            map.put(rate, v);
        }
        v[0] = add(v[0], e.net, minus);
        v[1] = add(v[1], e.tax, minus);
    }

    private static BigDecimal add(BigDecimal sum, BigDecimal value, boolean minus) {
        return minus ? sum.subtract(value) : sum.add(value);
    }
}
