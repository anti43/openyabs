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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.function.Function;
import mpv5.globals.Messages;
import mpv5.logging.Log;

/**
 * Small business VAT exemption (Art. 281 ff. VAT Directive 2006/112/EC; in
 * Germany "Kleinunternehmer", § 19 UStG).
 * <p>
 * Stored in the company info (<code>companyinfo.*</code>):
 * <ul>
 * <li>{@value #KEY_ENABLED}: "true" if the exemption applies</li>
 * <li>{@value #KEY_NOTE}: the note on the invoice; empty for the default
 * wording of the company's country</li>
 * <li>{@value #KEY_KU_ID}: the KU-IdNr. of the EU small business scheme (§ 19a
 * UStG), optional</li>
 * </ul>
 * With the exemption, invoices must not show VAT, e-invoice lines are 0 % with
 * category <code>E</code> and the note as exemption reason, and the note is
 * available to templates as <code>companyinfo.smallbusinessnote</code> (empty
 * without the exemption).
 * <p>
 * Country profiles: the default note per country, and for Germany the revenue
 * limits (since 2025: 25.000 EUR prior year, 100.000 EUR current year; above
 * 100.000 EUR regular taxation starts with the transaction that exceeds it).
 */
public final class SmallBusiness {

    public static final String KEY_ENABLED = "smallbusiness";
    public static final String KEY_NOTE = "smallbusinessnote";
    public static final String KEY_KU_ID = "smallbusinessid";
    /**
     * German limits (§ 19 Abs. 1 UStG, since 2025)
     */
    public static final BigDecimal DE_LIMIT_PRIOR_YEAR = new BigDecimal("25000");
    public static final BigDecimal DE_LIMIT_CURRENT_YEAR = new BigDecimal("100000");

    private SmallBusiness() {
    }

    /**
     * @param company lookup for <code>companyinfo.*</code> keys (without prefix)
     * @return true if the small business exemption is switched on
     */
    public static boolean isEnabled(Function<String, String> company) {
        return company != null && "true".equalsIgnoreCase(trim(company.apply(KEY_ENABLED)));
    }

    /**
     * @return true if the current user's company uses the exemption
     */
    public static boolean isEnabled() {
        try {
            return isEnabled(ZugpferdFile.companySettings());
        } catch (Throwable t) {
            return false;
        }
    }

    /**
     * @return the setting of the current user's company, null if it has never
     * been set (plugins may then apply their own default)
     */
    public static Boolean configured() {
        try {
            String v = trim(mpv5.db.objects.User.getCurrentUser().getProperty("companyinfo." + KEY_ENABLED));
            return v.isEmpty() || "null".equals(v) ? null : Boolean.valueOf("true".equalsIgnoreCase(v));
        } catch (Throwable t) {
            return null;
        }
    }

    /**
     * Switch the exemption on or off for the current user's company (saved)
     *
     * @param enabled
     */
    public static void setEnabled(boolean enabled) {
        try {
            mpv5.db.objects.User u = mpv5.db.objects.User.getCurrentUser();
            u.setProperty("companyinfo." + KEY_ENABLED, String.valueOf(enabled));
            u.saveProperties();
        } catch (Throwable t) {
            Log.Debug(SmallBusiness.class, t);
        }
    }

    /**
     * @param company lookup for <code>companyinfo.*</code> keys (without prefix)
     * @return the note for invoices: the configured one, else the default of
     * the company's country; empty if the exemption is off
     */
    public static String note(Function<String, String> company) {
        if (!isEnabled(company)) {
            return "";
        }
        String own = trim(company.apply(KEY_NOTE));
        if (!own.isEmpty()) {
            return own;
        }
        return defaultNote(ZugpferdFile.toCountryCode(company.apply("country")), trim(company.apply(KEY_KU_ID)));
    }

    /**
     * The legal wording of the country profile
     *
     * @param countryCode ISO 3166 alpha-2, empty for unknown
     * @param kuId the KU-IdNr. of the EU scheme, may be empty
     * @return the note
     */
    public static String defaultNote(String countryCode, String kuId) {
        String note;
        if ("DE".equals(countryCode)) {
            //§ 34a UStDV: reference to the exemption
            note = "Gemäß § 19 UStG wird keine Umsatzsteuer berechnet (Kleinunternehmerregelung).";
        } else if ("AT".equals(countryCode)) {
            note = "Umsatzsteuerbefreit – Kleinunternehmer gemäß § 6 Abs. 1 Z 27 UStG.";
        } else {
            note = Messages.SMALLBUSINESS_DEFAULT_NOTE.toString();
        }
        String id = trim(kuId);
        if (!id.isEmpty()) {
            note += " " + ("DE".equals(countryCode) || "AT".equals(countryCode) ? "KU-IdNr." : "ID") + ": " + id;
        }
        return note;
    }

    /**
     * @param countryCode ISO 3166 alpha-2
     * @return true if the revenue limits of this country are known (and
     * monitored)
     */
    public static boolean hasLimits(String countryCode) {
        return "DE".equals(countryCode);
    }

    /**
     * Check the revenue limits of the country profile before an invoice is
     * saved
     *
     * @param countryCode ISO 3166 alpha-2 of the company
     * @param priorYear net revenue of the prior year
     * @param currentYear net revenue of the current year without this invoice
     * @param thisInvoice net amount of this invoice
     * @return warnings for the user, empty if all is fine
     */
    public static List<String> checkLimits(String countryCode, BigDecimal priorYear, BigDecimal currentYear,
            BigDecimal thisInvoice) {
        List<String> warnings = new ArrayList<String>();
        if (!hasLimits(countryCode)) {
            return warnings;
        }
        BigDecimal prior = nz(priorYear);
        BigDecimal before = nz(currentYear);
        BigDecimal after = before.add(nz(thisInvoice));
        if (prior.compareTo(DE_LIMIT_PRIOR_YEAR) > 0) {
            warnings.add(String.format(Messages.SMALLBUSINESS_PRIOR_YEAR_EXCEEDED.toString(),
                    money(prior), money(DE_LIMIT_PRIOR_YEAR)));
        }
        if (after.compareTo(DE_LIMIT_CURRENT_YEAR) > 0) {
            warnings.add(String.format(before.compareTo(DE_LIMIT_CURRENT_YEAR) > 0
                    ? Messages.SMALLBUSINESS_CURRENT_YEAR_EXCEEDED.toString()
                    : Messages.SMALLBUSINESS_THIS_INVOICE_EXCEEDS.toString(),
                    money(after), money(DE_LIMIT_CURRENT_YEAR)));
        }
        return warnings;
    }

    /**
     * Check an invoice of the current user's company before it is saved:
     * revenue limits and positions with VAT
     *
     * @param date the invoice date
     * @param net the net amount of the invoice
     * @param tax the VAT of the invoice
     * @param newInvoice true for a new invoice: only then the revenue limits
     * are checked (no repeated warning each time an invoice is saved again)
     * @return warnings, empty if the exemption is off or all is fine
     */
    public static List<String> checkInvoice(Date date, BigDecimal net, BigDecimal tax, boolean newInvoice) {
        List<String> warnings = new ArrayList<String>();
        Function<String, String> company = ZugpferdFile.companySettings();
        if (!isEnabled(company)) {
            return warnings;
        }
        if (tax != null && tax.signum() != 0) {
            warnings.add(Messages.SMALLBUSINESS_INVOICE_HAS_VAT.toString());
        }
        String country = ZugpferdFile.toCountryCode(company.apply("country"));
        if (newInvoice && hasLimits(country)) {
            Calendar c = Calendar.getInstance();
            c.setTime(date != null ? date : new Date());
            int year = c.get(Calendar.YEAR);
            warnings.addAll(checkLimits(country, revenue(year - 1, -1), revenue(year, -1), net));
        }
        return warnings;
    }

    /**
     * The net revenue of a year: invoices minus credit notes, without
     * cancelled ones and the trash bin
     *
     * @param year
     * @param excludeId an invoice not to count, &lt;= 0 for none
     * @return
     */
    public static BigDecimal revenue(int year, int excludeId) {
        BigDecimal sum = BigDecimal.ZERO;
        try {
            String from = "'" + year + "-01-01'";
            String to = "'" + (year + 1) + "-01-01'";
            mpv5.db.common.ReturnValue rv = mpv5.db.common.QueryHandler.instanceOf()
                    .clone(mpv5.db.common.Context.getInvoice()).freeSelectQuery(
                    "SELECT inttype, SUM(netvalue) FROM items WHERE invisible = 0 AND inttype IN ("
                    + mpv5.db.objects.Item.TYPE_INVOICE + "," + mpv5.db.objects.Item.TYPE_CREDIT + ")"
                    + " AND intstatus <> " + mpv5.db.objects.Item.STATUS_CANCELLED
                    + " AND dateadded >= " + from + " AND dateadded < " + to
                    + (excludeId > 0 ? " AND ids <> " + excludeId : "")
                    + " GROUP BY inttype", mpv5.usermanagement.MPSecurityManager.VIEW, null);
            if (rv != null && rv.hasData()) {
                for (Object[] r : rv.getData()) {
                    BigDecimal v = r[1] == null ? BigDecimal.ZERO : new BigDecimal(String.valueOf(r[1]));
                    boolean credit = Integer.parseInt(String.valueOf(r[0])) == mpv5.db.objects.Item.TYPE_CREDIT;
                    //credit notes may be stored with a positive or a negative amount
                    sum = credit ? sum.subtract(v.abs()) : sum.add(v);
                }
            }
        } catch (Exception e) {
            Log.Debug(SmallBusiness.class, e);
        }
        return sum;
    }

    private static String money(BigDecimal v) {
        return mpv5.utils.numberformat.FormatNumber.formatDezimal(v.doubleValue());
    }

    private static BigDecimal nz(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }

    private static String trim(String s) {
        return s == null ? "" : s.trim();
    }
}
