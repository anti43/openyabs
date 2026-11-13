/*
 *  This file is part of YaBS.
 *
 *  YaBS is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  YaBS is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with YaBS.  If not, see <http://www.gnu.org/licenses/>.
 */
package mpv5.handler;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.db.common.QueryHandler;
import mpv5.db.common.ReturnValue;
import mpv5.db.objects.Item;
import mpv5.logging.Log;
import mpv5.usermanagement.MPSecurityManager;
import mpv5.utils.date.DateConverter;

/**
 * Payments received on an invoice (table payments): a customer pays an
 * invoice in parts. The sum gives the open amount; when nothing is open any
 * more the invoice is paid (status and pay date), when a payment is removed
 * from a paid invoice it is open again ("Finished").
 */
public final class Payments {

    private Payments() {
    }

    /**
     * One payment
     */
    public static final class Payment {

        public final int id;
        public final int invoice;
        public final BigDecimal amount;
        public final Date date;
        public final String description;

        Payment(int id, int invoice, BigDecimal amount, Date date, String description) {
            this.id = id;
            this.invoice = invoice;
            this.amount = amount;
            this.date = date;
            this.description = description == null ? "" : description;
        }
    }

    private static String q(String s) {
        return s == null ? "NULL" : "'" + s.replace("'", "''") + "'";
    }

    private static QueryHandler db() {
        return QueryHandler.instanceOf().clone(Context.getInvoice());
    }

    /**
     * @param invoice the ids of the invoice
     * @return its payments, oldest first
     */
    public static List<Payment> of(int invoice) {
        return select("WHERE itemsids = " + invoice + " ORDER BY datepaid, ids");
    }

    /**
     * @param from
     * @param to
     * @return the payments with a pay date in the time frame (both days included)
     */
    public static List<Payment> between(Date from, Date to) {
        return select("WHERE datepaid >= " + q(DateConverter.getSQLDateString(DateConverter.getStartOfDay(from)))
                + " AND datepaid <= " + q(DateConverter.getSQLDateString(DateConverter.getEndOfDay(to))) + " ORDER BY datepaid, ids");
    }

    private static List<Payment> select(String where) {
        List<Payment> l = new ArrayList<Payment>();
        if (!mpv5.db.common.DatabaseUpdater.hasTable("payments")) {
            return l;
        }
        try {
            ReturnValue rv = db().freeSelectQuery("SELECT ids, itemsids, amount, datepaid, description FROM payments " + where,
                    MPSecurityManager.VIEW, null);
            if (rv != null && rv.hasData()) {
                for (Object[] r : rv.getData()) {
                    l.add(new Payment(Integer.parseInt(String.valueOf(r[0])), Integer.parseInt(String.valueOf(r[1])),
                            new BigDecimal(String.valueOf(r[2])).setScale(2, RoundingMode.HALF_UP), date(r[3]),
                            r[4] == null ? "" : String.valueOf(r[4])));
                }
            }
        } catch (Exception e) {
            Log.Debug(Payments.class, e);
        }
        return l;
    }

    private static Date date(Object o) {
        if (o instanceof Date) {
            return (Date) o;
        }
        Date d = DateConverter.getDate(String.valueOf(o));
        return d == null ? new Date() : d;
    }

    /**
     * @param invoice
     * @return the sum of its payments
     */
    public static BigDecimal paid(int invoice) {
        BigDecimal s = BigDecimal.ZERO;
        for (Payment p : of(invoice)) {
            s = s.add(p.amount);
        }
        return s;
    }

    /**
     * @param i
     * @return gross amount minus the payments, not below 0
     */
    public static BigDecimal open(Item i) {
        BigDecimal o = i.getGrossAmount().subtract(paid(i.__getIDS())).setScale(2, RoundingMode.HALF_UP);
        return o.signum() < 0 ? BigDecimal.ZERO.setScale(2) : o;
    }

    /**
     * Records a payment and updates the status of the invoice
     *
     * @param i the saved invoice
     * @param amount more than 0
     * @param date the day of the payment
     * @param description e.g. "bank transfer", may be empty
     * @return true if stored
     */
    public static boolean add(Item i, BigDecimal amount, Date date, String description) {
        if (i == null || !i.isExisting() || amount == null || amount.signum() <= 0 || !mpv5.db.common.DatabaseUpdater.hasTable("payments")) {
            return false;
        }
        String now = q(DateConverter.getSQLDateString(new Date()));
        ReturnValue rv = db().freeUpdateQuery("INSERT INTO payments (itemsids, amount, datepaid, description, intaddedby, dateadded) VALUES ("
                + i.__getIDS() + ", " + amount.setScale(2, RoundingMode.HALF_UP).toPlainString() + ", "
                + q(DateConverter.getSQLDateString(date == null ? new Date() : date)) + ", " + q(description == null ? "" : description) + ", "
                + mpv5.db.objects.User.getCurrentUser().__getIDS() + ", " + now + ")",
                MPSecurityManager.RIGHT_TO_CREATE_OR_DELETE, null);
        if (rv == null) {
            return false;
        }
        updateStatus(i);
        return true;
    }

    /**
     * Removes a payment and updates the status of the invoice
     *
     * @param i
     * @param paymentId
     */
    public static void remove(Item i, int paymentId) {
        db().freeUpdateQuery("DELETE FROM payments WHERE ids = " + paymentId + " AND itemsids = " + i.__getIDS(),
                MPSecurityManager.RIGHT_TO_CREATE_OR_DELETE, null);
        updateStatus(i);
    }

    /**
     * Paid in full: status paid, pay date = date of the last payment. A paid
     * invoice with payments that no longer cover it: open again (Finished).
     * An invoice without payments is not touched (it can be set paid by hand).
     *
     * @param i
     */
    public static void updateStatus(Item i) {
        List<Payment> l = of(i.__getIDS());
        if (l.isEmpty()) {
            if (i.__getIntstatus() == Item.STATUS_PAID) {
                //the last payment was removed: no longer paid
                i.setIntstatus(Item.STATUS_FINISHED);
                i.save(true);
            }
            return;
        }
        boolean complete = open(i).signum() == 0;
        if (complete && i.__getIntstatus() != Item.STATUS_PAID) {
            i.setIntstatus(Item.STATUS_PAID);
            i.setDateend(l.get(l.size() - 1).date);
            i.save(true);
        } else if (!complete && i.__getIntstatus() == Item.STATUS_PAID) {
            i.setIntstatus(Item.STATUS_FINISHED);
            i.save(true);
        }
    }

    /**
     * @param i
     * @return true if the invoice has payments
     */
    public static boolean hasPayments(Item i) {
        return i != null && i.isExisting() && !of(i.__getIDS()).isEmpty();
    }

    /**
     * @param invoice ids
     * @return the invoice, null if not found
     */
    public static Item invoice(int invoice) {
        try {
            return (Item) DatabaseObject.getObject(Context.getInvoice(), invoice);
        } catch (Exception e) {
            return null;
        }
    }
}
