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

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.db.common.QueryCriteria;
import mpv5.db.common.QueryHandler;
import mpv5.db.common.ReturnValue;
import mpv5.db.objects.Account;
import mpv5.db.objects.Expense;
import mpv5.db.objects.Revenue;
import mpv5.globals.Constants;
import mpv5.globals.Messages;
import mpv5.handler.ZugpferdExpenseImport.ParsedInvoice;
import mpv5.logging.Log;
import mpv5.usermanagement.MPSecurityManager;
import mpv5.utils.date.DateConverter;

/**
 * Imports e-invoices (ZUGFeRD / Factur-X PDF, XRechnung / CII / UBL XML) as
 * expense or income, for the import buttons, drag and drop and the watch
 * folder alike:
 * <ul>
 * <li>expense or income from the parties: the company (VAT ID, tax number,
 * name of the company info) is the buyer of an expense and the seller of an
 * income</li>
 * <li>an own invoice that exists in YaBS is not booked a second time as
 * income</li>
 * <li>no second import of the same file (SHA-256 of the receipts) or of the
 * same invoice (number and amount)</li>
 * <li>the record gets the dates of the invoice, the file is archived as its
 * receipt ({@link ExpenseReceipts}, {@link RevenueReceipts})</li>
 * </ul>
 */
public final class EInvoiceImport {

    /**
     * What an e-invoice is booked as
     */
    public enum Kind {
        EXPENSE, INCOME
    }

    /**
     * What happened to a file
     */
    public enum Outcome {
        /**
         * a new expense / income with the file as receipt
         */
        IMPORTED,
        /**
         * the file is already a receipt
         */
        DUPLICATE_FILE,
        /**
         * an expense / income with this invoice number (and amount) exists
         */
        DUPLICATE_NUMBER,
        /**
         * an own invoice of YaBS: booked as invoice, not as income
         */
        OWN_INVOICE,
        /**
         * no e-invoice (e.g. a plain PDF)
         */
        NOT_EINVOICE,
        /**
         * could not be saved
         */
        FAILED
    }

    /**
     * The result for one file
     */
    public static final class Result {

        public final File file;
        public final Outcome outcome;
        public final Kind kind;
        /**
         * the new record, or the existing one of a duplicate; may be null
         */
        public final DatabaseObject record;
        public final ParsedInvoice invoice;
        public final String detail;

        Result(File file, Outcome outcome, Kind kind, DatabaseObject record, ParsedInvoice invoice, String detail) {
            this.file = file;
            this.outcome = outcome;
            this.kind = kind;
            this.record = record;
            this.invoice = invoice;
            this.detail = detail == null ? "" : detail;
        }

        /**
         * @return a line for the user: the file, what happened, the record
         */
        public String message() {
            String what = record == null ? "" : describe(record);
            switch (outcome) {
                case IMPORTED:
                    return String.format(kind == Kind.INCOME ? Messages.EINVOICE_IMPORTED_INCOME.toString()
                            : Messages.EINVOICE_IMPORTED_EXPENSE.toString(), file.getName(), what);
                case DUPLICATE_FILE:
                case DUPLICATE_NUMBER:
                    return String.format(Messages.EINVOICE_ALREADY_IMPORTED.toString(), file.getName(), what);
                case OWN_INVOICE:
                    return String.format(Messages.EINVOICE_OWN_INVOICE.toString(), file.getName(), what);
                case NOT_EINVOICE:
                    return String.format(Messages.EINVOICE_NOT_EINVOICE.toString(), file.getName());
                default:
                    return String.format(Messages.EINVOICE_IMPORT_FAILED.toString(), file.getName(), detail);
            }
        }
    }

    private EInvoiceImport() {
    }

    /**
     * @param files the files (folders are not searched)
     * @param fallback the kind when the parties do not tell (e.g. the panel
     * the files were dropped on), null: expense
     * @return one result per file
     */
    public static List<Result> importFiles(Collection<File> files, Kind fallback) {
        List<Result> l = new ArrayList<Result>();
        if (files != null) {
            for (File f : files) {
                if (f != null && f.isFile()) {
                    l.add(importFile(f, fallback));
                }
            }
        }
        return l;
    }

    /**
     * @param file
     * @param fallback the kind when the parties do not tell, null: expense
     * @return the result
     */
    public static Result importFile(File file, Kind fallback) {
        String sha;
        try {
            sha = ExpenseReceipts.sha256(file);
        } catch (Exception e) {
            Log.Debug(EInvoiceImport.class, e);
            return new Result(file, Outcome.FAILED, fallback, null, null, e.getMessage());
        }
        //the same file again: whatever it is
        DatabaseObject existing = receiptOwner("expense_receipts", "expensesids", Context.getExpense(), sha);
        if (existing != null) {
            return new Result(file, Outcome.DUPLICATE_FILE, Kind.EXPENSE, existing, null, null);
        }
        existing = receiptOwner("revenue_receipts", "revenuesids", Context.getRevenue(), sha);
        if (existing != null) {
            return new Result(file, Outcome.DUPLICATE_FILE, Kind.INCOME, existing, null, null);
        }
        ParsedInvoice inv = ZugpferdExpenseImport.parse(file);
        if (inv == null) {
            return new Result(file, Outcome.NOT_EINVOICE, fallback, null, null, null);
        }
        Kind detected = kindOf(inv, ZugpferdFileCompany.get());
        Kind kind = detected != null ? detected : fallback != null ? fallback : Kind.EXPENSE;
        if (kind == Kind.INCOME) {
            DatabaseObject own = ownInvoice(inv.invoiceNumber);
            if (own != null) {
                return new Result(file, Outcome.OWN_INVOICE, kind, own, inv, null);
            }
        }
        existing = sameInvoice(kind, inv);
        if (existing != null) {
            return new Result(file, Outcome.DUPLICATE_NUMBER, kind, existing, inv, null);
        }
        try {
            DatabaseObject record = save(kind, inv, file);
            return new Result(file, Outcome.IMPORTED, kind, record, inv, null);
        } catch (Exception e) {
            Log.Debug(EInvoiceImport.class, e);
            return new Result(file, Outcome.FAILED, kind, null, inv, e.getMessage());
        }
    }

    /**
     * Expense or income from the parties of the invoice: the company (VAT ID,
     * tax number, else the name) is the buyer of an expense and the seller of
     * an income
     *
     * @param inv
     * @param company lookup of the company info (without "companyinfo.")
     * @return the kind, null if the parties do not tell
     */
    public static Kind kindOf(ParsedInvoice inv, Function<String, String> company) {
        List<String> ids = new ArrayList<String>();
        for (String key : new String[]{"vatid", "account1", "taxnumber"}) {
            String v = id(company.apply(key));
            if (!v.isEmpty() && !ids.contains(v)) {
                ids.add(v);
            }
        }
        boolean seller = matchesId(inv.sellerIds, ids);
        boolean buyer = matchesId(inv.buyerIds, ids);
        if (!seller && !buyer) {
            List<String> names = new ArrayList<String>();
            for (String key : new String[]{"business", "name"}) {
                String v = name(company.apply(key));
                if (!v.isEmpty()) {
                    names.add(v);
                }
            }
            seller = names.contains(name(inv.sellerName));
            buyer = names.contains(name(inv.buyerName));
        }
        if (seller == buyer) {
            return null;
        }
        return seller ? Kind.INCOME : Kind.EXPENSE;
    }

    private static boolean matchesId(List<String> invoiceIds, List<String> companyIds) {
        for (String i : invoiceIds) {
            if (companyIds.contains(id(i))) {
                return true;
            }
        }
        return false;
    }

    /**
     * @return the ID without spaces, dots, slashes; upper case
     */
    static String id(String s) {
        return s == null ? "" : s.replaceAll("[^A-Za-z0-9]", "").toUpperCase(Locale.ROOT);
    }

    static String name(String s) {
        return s == null ? "" : s.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }

    /**
     * The company info of the current user
     */
    static final class ZugpferdFileCompany {

        static Function<String, String> get() {
            try {
                return mpv5.utils.export.ZugpferdFile.companySettings();
            } catch (Throwable t) {
                return k -> null;
            }
        }
    }

    private static Object[][] rows(String sql) {
        try {
            ReturnValue rv = QueryHandler.getConnection().freeQuery(sql, MPSecurityManager.VIEW, null);
            return rv == null || !rv.hasData() || rv.getData() == null ? new Object[0][] : rv.getData();
        } catch (Exception e) {
            Log.Debug(EInvoiceImport.class, e);
            return new Object[0][];
        }
    }

    private static String q(String s) {
        return "'" + (s == null ? "" : s.replace("'", "''")) + "'";
    }

    private static DatabaseObject load(Context c, Object id) {
        try {
            return DatabaseObject.getObject(c, Integer.parseInt(String.valueOf(id)));
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * @return the expense / income the file with this hash is a receipt of
     */
    private static DatabaseObject receiptOwner(String table, String column, Context c, String sha) {
        for (Object[] r : rows("SELECT " + column + " FROM " + table + " WHERE sha256 = " + q(sha.toLowerCase(Locale.ROOT)))) {
            DatabaseObject o = load(c, r[0]);
            if (o != null) {
                return o;
            }
        }
        return null;
    }

    /**
     * @return an invoice (credit note, deposit, part payment) of YaBS with this
     * number, not in the trash bin
     */
    private static DatabaseObject ownInvoice(String number) {
        if (number == null || number.trim().isEmpty()) {
            return null;
        }
        for (Object[] r : rows("SELECT ids FROM items WHERE cnumber = " + q(number.trim()) + " AND invisible = 0 AND inttype IN ("
                + Constants.TYPE_INVOICE + ", " + Constants.TYPE_CREDIT + ", " + Constants.TYPE_PART_PAYMENT + ", " + Constants.TYPE_DEPOSIT + ")")) {
            DatabaseObject o = load(Context.getInvoice(), r[0]);
            if (o != null) {
                return o;
            }
        }
        return null;
    }

    /**
     * @return an expense / income with the number of the invoice; expenses
     * also with its gross amount (two suppliers may use the same number)
     */
    private static DatabaseObject sameInvoice(Kind kind, ParsedInvoice inv) {
        if (inv.invoiceNumber.isEmpty()) {
            return null;
        }
        Context c = kind == Kind.INCOME ? Context.getRevenue() : Context.getExpense();
        for (Object[] r : rows("SELECT ids, brutvalue FROM " + (kind == Kind.INCOME ? "revenues" : "expenses")
                + " WHERE cnumber = " + q(inv.invoiceNumber) + " AND invisible = 0")) {
            if (kind == Kind.EXPENSE) {
                try {
                    if (new java.math.BigDecimal(String.valueOf(r[1])).compareTo(inv.grossAmount) != 0) {
                        continue;
                    }
                } catch (Exception e) {
                    continue;
                }
            }
            DatabaseObject o = load(c, r[0]);
            if (o != null) {
                return o;
            }
        }
        return null;
    }

    private static int defaultAccount(int type) {
        try {
            List<DatabaseObject> l = DatabaseObject.getObjects(Context.getAccounts(), new QueryCriteria("intaccounttype", type));
            if (!l.isEmpty()) {
                return l.get(0).__getIDS();
            }
        } catch (Exception e) {
            //none of this type
        }
        return 1;
    }

    /**
     * Saves the expense / income with the number and dates of the invoice and
     * archives the file as its receipt; without the receipt nothing is left
     */
    private static DatabaseObject save(Kind kind, ParsedInvoice inv, File file) throws Exception {
        DatabaseObject record = kind == Kind.INCOME ? inv.toRevenue(defaultAccount(Account.INCOME), 1)
                : inv.toExpense(defaultAccount(Account.EXPENSE), 1);
        if (!record.save(true) || record.__getIDS() <= 0) {
            throw new IllegalStateException(Messages.RECEIPT_SAVE_FAILED.toString());
        }
        //a new record gets the next own number and the current date on its first save
        String num = inv.invoiceNumber.length() > 250 ? inv.invoiceNumber.substring(0, 250) : inv.invoiceNumber;
        if (record instanceof Expense) {
            Expense e = (Expense) record;
            if (!num.isEmpty()) {
                e.setCnumber(num);
                e.setCname(num);
            }
            e.setDateend(inv.dueDate);
        } else {
            Revenue r = (Revenue) record;
            if (!num.isEmpty()) {
                r.setCnumber(num);
                r.setCname(num);
            }
            r.setDateend(inv.dueDate);
        }
        record.setDateadded(inv.issueDate);
        record.save(true);
        try {
            if (record instanceof Expense) {
                ExpenseReceipts.attach((Expense) record, file);
            } else {
                RevenueReceipts.attach((Revenue) record, file);
            }
        } catch (Exception e) {
            //no booking without its receipt: a new import must find nothing
            try {
                record.delete();
            } catch (Exception ex) {
                Log.Debug(EInvoiceImport.class, ex);
            }
            throw e;
        }
        Activity.record(record, Activity.Kind.CREATED, String.format(kind == Kind.INCOME
                ? Messages.RECEIPT_IMPORTED_SINGLE_REVENUE.toString() : Messages.RECEIPT_IMPORTED_SINGLE_EXPENSE.toString(), file.getAbsolutePath()));
        return record;
    }

    /**
     * @return number and date of an expense, income or invoice
     */
    public static String describe(DatabaseObject o) {
        String c = o instanceof Expense ? ((Expense) o).__getCnumber() : o instanceof Revenue ? ((Revenue) o).__getCnumber()
                : o instanceof mpv5.db.objects.Item ? ((mpv5.db.objects.Item) o).__getCnumber() : null;
        String n = c == null || c.isEmpty() ? "#" + o.__getIDS() : c;
        Date d = o.__getDateadded();
        return n + (d == null ? "" : " (" + DateConverter.getDefDateString(d) + ")");
    }
}
