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
import java.math.RoundingMode;
import java.util.Date;
import java.util.Locale;
import java.util.function.Function;
import mpv5.db.common.DatabaseObject;
import mpv5.db.objects.ValueProperty;
import mpv5.logging.Log;
import mpv5.utils.date.DateConverter;

/**
 * Payment terms of an invoice: the bank account the customer pays to, the
 * payment term and the cash discount (Skonto). Everything can be set per
 * contact (tab "E-Invoice"); the defaults come from the company info.
 * <ul>
 * <li>payment term: contact, invoice template, company info, 14 days; 0
 * days (or "payable immediately" at the contact) means due on the invoice
 * date</li>
 * <li>Skonto: contact, company info, none</li>
 * <li>bank account: contact, account of the company info</li>
 * </ul>
 */
public class PaymentTerms {

    /**
     * Value property of a contact / company setting: Skonto percent
     */
    public static final String KEY_SKONTO_PERCENT = "skontopercent";
    /**
     * Value property of a contact / company setting: Skonto days
     */
    public static final String KEY_SKONTO_DAYS = "skontodays";
    /**
     * Value property of a contact: "1" if invoices are payable immediately
     * (the payment term column of the contact can not hold "0 days": 0 means
     * not set there)
     */
    public static final String KEY_IMMEDIATE = "immediate";
    private static final String CONTACT_PREFIX = "einvoice.";
    private final int days;
    private final BigDecimal skontoPercent;
    private final int skontoDays;
    private final BankAccount account;

    public PaymentTerms(int days, BigDecimal skontoPercent, int skontoDays, BankAccount account) {
        this.days = days;
        this.skontoPercent = skontoPercent == null || skontoPercent.signum() <= 0 || skontoDays <= 0
                ? BigDecimal.ZERO : skontoPercent.setScale(2, RoundingMode.HALF_UP);
        this.skontoDays = this.skontoPercent.signum() > 0 ? skontoDays : 0;
        this.account = account;
    }

    /**
     * @param contact
     * @param contactPayterm the contact's payment term (days), 0 if none
     * @param templateDays the payment term of the invoice template
     * ({@link ZugpferdFile#paytermOf}), {@link ZugpferdFile#PAYTERM_UNSET} if
     * none
     * @param company lookup for companyinfo.* keys (without prefix)
     * @return the payment terms for an invoice to this contact
     */
    public static PaymentTerms forContact(DatabaseObject contact, int contactPayterm, int templateDays,
            Function<String, String> company) {
        int contactDays = isImmediate(contact) ? 0
                : contactPayterm > 0 ? contactPayterm : ZugpferdFile.PAYTERM_UNSET;
        int days = ZugpferdFile.paytermDays(contactDays, templateDays,
                ZugpferdFile.parseTerm(company.apply(ZugpferdFile.KEY_PAYTERM)));
        BigDecimal percent = parsePercent(contactValue(contact, KEY_SKONTO_PERCENT));
        int sdays = ZugpferdFile.parseDays(contactValue(contact, KEY_SKONTO_DAYS));
        if (percent.signum() <= 0 || sdays <= 0) {
            //no complete Skonto at the contact: default of the company info
            percent = parsePercent(company.apply(KEY_SKONTO_PERCENT));
            sdays = ZugpferdFile.parseDays(company.apply(KEY_SKONTO_DAYS));
        }
        return new PaymentTerms(days, percent, sdays, BankAccount.forContact(contact, company));
    }

    /**
     * @param contact
     * @param key {@link #KEY_SKONTO_PERCENT} or {@link #KEY_SKONTO_DAYS}
     * @return the value stored at the contact, null if none
     */
    public static String contactValue(DatabaseObject contact, String key) {
        return ContactProperty.get(contact, CONTACT_PREFIX + key);
    }

    /**
     * @param contact
     * @return true if invoices to the contact are payable immediately
     */
    public static boolean isImmediate(DatabaseObject contact) {
        return "1".equals(contactValue(contact, KEY_IMMEDIATE));
    }

    /**
     * @param contact the (saved) contact
     * @param immediate true: payable immediately, false: its payment term
     */
    public static void saveImmediate(DatabaseObject contact, boolean immediate) {
        saveContactValue(contact, KEY_IMMEDIATE, immediate ? "1" : null);
    }

    /**
     * Store a value at the (saved) contact, empty removes it
     */
    public static void saveContactValue(DatabaseObject contact, String key, String value) {
        ContactProperty.set(contact, CONTACT_PREFIX + key, value);
    }

    /**
     * @param value e.g. "2", "2,5" or "2.5 %"
     * @return the percent, 0 if empty or invalid
     */
    public static BigDecimal parsePercent(String value) {
        if (ZugpferdFile.isEmpty(value)) {
            return BigDecimal.ZERO;
        }
        try {
            BigDecimal p = new BigDecimal(value.replace("%", "").replace(",", ".").trim());
            return p.signum() > 0 && p.compareTo(new BigDecimal(100)) < 0 ? p : BigDecimal.ZERO;
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }

    public int getDays() {
        return days;
    }

    /**
     * @return true if payable immediately (due on the invoice date)
     */
    public boolean isImmediate() {
        return days == 0;
    }

    /**
     * @param issueDate
     * @return "Zahlbar bis 29.11.2026" or "Zahlbar sofort ohne Abzug"
     */
    public String text(Date issueDate) {
        return ZugpferdFile.paymentTermsText(days, dueDate(issueDate));
    }

    /**
     * @param issueDate
     * @return issue date + payment term
     */
    public Date dueDate(Date issueDate) {
        return DateConverter.addDays(issueDate, days);
    }

    public boolean hasSkonto() {
        return skontoPercent.signum() > 0;
    }

    /**
     * @return Skonto in percent (2 decimals), 0 if none
     */
    public BigDecimal getSkontoPercent() {
        return skontoPercent;
    }

    public int getSkontoDays() {
        return skontoDays;
    }

    /**
     * @param gross the gross amount of the invoice
     * @return the Skonto amount (2 decimals)
     */
    public BigDecimal skontoAmount(BigDecimal gross) {
        return gross.multiply(skontoPercent).divide(new BigDecimal(100), 2, RoundingMode.HALF_UP);
    }

    /**
     * @return the account the customer pays to, null if the company has none
     */
    public BankAccount getAccount() {
        return account;
    }

    /**
     * @return the Skonto line for the payment terms of an XRechnung
     * (BR-DE-18), e.g. "#SKONTO#TAGE=7#PROZENT=2.00#", "" if none
     */
    public String xrechnungSkonto() {
        if (!hasSkonto()) {
            return "";
        }
        return "#SKONTO#TAGE=" + skontoDays + "#PROZENT=" + skontoPercent.toPlainString() + "#";
    }

    /**
     * @return Skonto in percent for templates, e.g. "2" or "2,5"
     */
    public String formatPercent() {
        return skontoPercent.stripTrailingZeros().toPlainString().replace(".", ",");
    }

    @Override
    public String toString() {
        return String.format(Locale.ROOT, "%d days, Skonto %s%% %d days, %s", days, skontoPercent, skontoDays, account);
    }
}
