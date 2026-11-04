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

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import mpv5.db.common.NodataFoundException;
import mpv5.db.objects.Contact;
import mpv5.db.objects.Item;
import mpv5.db.objects.SubItem;
import mpv5.db.objects.Template;
import mpv5.db.objects.User;
import mpv5.db.objects.ValueProperty;
import mpv5.globals.Constants;
import mpv5.globals.Messages;
import mpv5.handler.TemplateHandler;
import mpv5.logging.Log;
import mpv5.utils.date.DateConverter;
import mpv5.utils.jobs.Waitable;
import org.mustangproject.BankDetails;
import org.mustangproject.Invoice;
import org.mustangproject.TradeParty;
import org.mustangproject.ZUGFeRD.IZUGFeRDExportableItem;
import org.mustangproject.ZUGFeRD.Profiles;
import org.mustangproject.ZUGFeRD.ZUGFeRD2PullProvider;
import org.mustangproject.ZUGFeRD.ZUGFeRDExporterFromA3;

/**
 * Creates electronic invoices (E-Rechnung, EN 16931) from YaBS invoices using
 * the Mustang library ("ZUGFeRD").
 * <ul>
 * <li>{@link Format#XRECHNUNG}: plain XRechnung XML (UN/CEFACT CII)</li>
 * <li>{@link Format#ZUGFERD}: the regular invoice PDF (from the invoice
 * template) with embedded ZUGFeRD 2 / Factur-X XML, profile EN16931</li>
 * </ul>
 *
 * Seller data is read from the company settings (user properties
 * <code>companyinfo.*</code>), buyer data from the invoice contact. The buyer
 * reference (Leitweg-ID) is read from a value property named
 * {@value #KEY_BUYER_REFERENCE} on the invoice or the contact; if none exists
 * the customer number is used.
 */
public class ZugpferdFile implements Waitable {

    public enum Format {

        XRECHNUNG("xml", "XRechnung"),
        ZUGFERD("pdf", "EN16931");
        private final String extension;
        private final String profile;

        Format(String extension, String profile) {
            this.extension = extension;
            this.profile = profile;
        }

        public String getExtension() {
            return extension;
        }

        public String getProfile() {
            return profile;
        }
    }
    /**
     * Value property (on invoice or contact) holding the buyer reference /
     * Leitweg-ID (BT-10)
     */
    public static final String KEY_BUYER_REFERENCE = "leitweg-id";
    /**
     * Company setting holding the VAT exemption reason used for 0% VAT lines
     */
    public static final String KEY_TAX_EXEMPTION_REASON = "taxexemptionreason";
    public static final String DEFAULT_TAX_EXEMPTION_REASON = "Umsatzsteuerbefreit";
    public static final String DEFAULT_CURRENCY = "EUR";
    private static final String COMPANY_PREFIX = "companyinfo.";
    private final Item item;
    private final File target;
    private final Format format;
    private final Invoice prepared;

    /**
     * @param item the invoice
     * @param target the file to write
     * @param format XML or PDF
     */
    public ZugpferdFile(Item item, File target, Format format) {
        this(item, null, target, format);
    }

    /**
     * @param item the invoice
     * @param prepared an already created invoice (see
     * {@link #createInvoice(Item)}), avoids reloading all data; may be null
     * @param target the file to write
     * @param format XML or PDF
     */
    public ZugpferdFile(Item item, Invoice prepared, File target, Format format) {
        this.item = item;
        this.prepared = prepared;
        this.target = target;
        this.format = format;
    }

    public File getTarget() {
        return target;
    }

    public Format getFormat() {
        return format;
    }

    /**
     * @param item
     * @return true if an electronic invoice can be created for this item type
     */
    public static boolean supports(Item item) {
        if (item == null) {
            return false;
        }
        int type = item.__getInttype();
        return type == Item.TYPE_INVOICE || type == Item.TYPE_PART_PAYMENT || type == Item.TYPE_DEPOSIT;
    }

    @Override
    public Exception waitFor() {
        try {
            mpv5.YabsViewProxy.instance().setWaiting(true);
            Invoice invoice = prepared != null ? prepared : createInvoice(item);
            if (format == Format.XRECHNUNG) {
                writeXml(invoice, target);
            } else {
                File pdf = renderPdf(item);
                try {
                    writePdf(invoice, pdf, target);
                } finally {
                    pdf.delete();
                }
            }
            Log.Debug(this, "E-Rechnung written: " + target);
            return null;
        } catch (Exception ex) {
            Log.Debug(ex);
            return ex;
        } finally {
            mpv5.YabsViewProxy.instance().setWaiting(false);
        }
    }

    /**
     * Build the Mustang invoice from a stored YaBS invoice
     *
     * @param item
     * @return
     * @throws NodataFoundException if the contact does not exist
     */
    public static Invoice createInvoice(Item item) throws NodataFoundException {
        if (!supports(item)) {
            throw new IllegalArgumentException(Messages.ZUGFERD_NOT_AN_INVOICE + ": " + item);
        }
        Contact contact = item.getContact();
        Function<String, String> company = companySettings();
        return createInvoice(item, item.getSubitems(), createSender(company), contact.asZugpferdTradeparty(),
                findBuyerReference(item, contact), contact.__getPayterm(), company.apply(KEY_TAX_EXEMPTION_REASON));
    }

    /**
     * Build the Mustang invoice from the given parts (no database access)
     *
     * @param item
     * @param subitems
     * @param sender
     * @param recipient
     * @param buyerReference
     * @param paytermDays
     * @param taxExemptionReason
     * @return
     */
    public static Invoice createInvoice(Item item, SubItem[] subitems, TradeParty sender, TradeParty recipient,
            String buyerReference, int paytermDays, String taxExemptionReason) {
        Date issueDate = item.__getDateadded() != null ? item.__getDateadded() : new Date();
        Date dueDate = item.__getDatetodo();
        if (dueDate == null || dueDate.before(DateConverter.getStartOfDay(issueDate))) {
            dueDate = paytermDays > 0 ? DateConverter.addDays(issueDate, paytermDays) : null;
        }

        Invoice invoice = new Invoice()
                .setNumber(item.__getCnumber())
                .setIssueDate(issueDate)
                .setDeliveryDate(issueDate)
                .setCurrency(DEFAULT_CURRENCY)
                .setSender(sender)
                .setRecipient(recipient)
                .setReferenceNumber(isEmpty(buyerReference) ? item.__getCnumber() : buyerReference.trim());
        if (dueDate != null) {
            invoice.setDueDate(dueDate);
            invoice.setPaymentTermDescription(Messages.ZUGFERD_PAYABLE_UNTIL + " "
                    + new SimpleDateFormat("dd.MM.yyyy").format(dueDate));
        }
        if (item.__getInttype() == Item.TYPE_PART_PAYMENT) {
            invoice.setDocumentCode("326");//partial invoice
        }

        String reason = isEmpty(taxExemptionReason) ? DEFAULT_TAX_EXEMPTION_REASON : taxExemptionReason;
        int count = 0;
        for (SubItem subItem : subitems) {
            IZUGFeRDExportableItem line = subItem.asZugpferdSubitem(reason);
            if (line != null) {
                invoice.addItem(line);
                count++;
            }
        }
        if (count == 0) {
            throw new IllegalStateException(Messages.ZUGFERD_NO_ITEMS.toString());
        }
        return invoice;
    }

    /**
     * Create the seller from the company settings
     *
     * @param company lookup for <code>companyinfo.*</code> keys (without prefix)
     * @return
     */
    public static TradeParty createSender(Function<String, String> company) {
        String person = join(" ", company.apply("firstname"), company.apply("name"));
        String name = firstNonEmpty(company.apply("business"), person);

        TradeParty sender = new TradeParty(name, nvl(company.apply("street")), nvl(company.apply("zip")),
                nvl(company.apply("city")), toCountryCode(company.apply("country")));

        String vatId = firstNonEmpty(company.apply("vatid"), company.apply("account1"));
        if (!isEmpty(vatId)) {
            sender.addVATID(vatId.replaceAll("\\s", ""));
        }
        String taxNumber = company.apply("taxnumber");
        if (!isEmpty(taxNumber)) {
            sender.addTaxID(taxNumber.trim());
        }

        String iban = firstNonEmpty(company.apply("account"), company.apply("iban"));
        if (!isEmpty(iban)) {
            BankDetails bank = new BankDetails(iban.replaceAll("\\s", ""));
            String bic = firstNonEmpty(company.apply("banknumber"), company.apply("bic"));
            if (!isEmpty(bic)) {
                bank.setBIC(bic.replaceAll("\\s", ""));
            }
            bank.setAccountName(name);
            sender.addBankDetails(bank);
        }

        String email = nvl(company.apply("email")).trim();
        String phone = join(" ", company.apply("phoneprefix"), company.apply("phone"));
        if (!email.isEmpty()) {
            sender.setEmail(email);
        }
        sender.setContact(new org.mustangproject.Contact(firstNonEmpty(person, name), phone, email));
        return sender;
    }

    /**
     * Check the seller and buyer for data which is required by EN 16931 /
     * XRechnung
     *
     * @param invoice
     * @return a list of human readable problems, empty if everything is fine
     */
    public static List<String> validate(Invoice invoice) {
        List<String> problems = new ArrayList<String>();
        TradeParty s = invoice.getSender();
        String seller = Messages.ZUGFERD_SELLER + ": ";
        check(problems, seller, Messages.ZUGFERD_FIELD_NAME, s.getName());
        check(problems, seller, Messages.ZUGFERD_FIELD_STREET, s.getStreet());
        check(problems, seller, Messages.ZUGFERD_FIELD_ZIP, s.getZIP());
        check(problems, seller, Messages.ZUGFERD_FIELD_CITY, s.getLocation());
        check(problems, seller, Messages.ZUGFERD_FIELD_TAXID, firstNonEmpty(s.getVATID(), s.getTaxID()));
        check(problems, seller, Messages.ZUGFERD_FIELD_EMAIL, s.getUriUniversalCommunicationID());
        if (s.getContact() == null || isEmpty(s.getContact().getPhone())) {
            problems.add(seller + Messages.ZUGFERD_FIELD_PHONE);
        }
        if (s.getBankDetails() == null || s.getBankDetails().isEmpty()) {
            problems.add(seller + Messages.ZUGFERD_FIELD_IBAN);
        }

        TradeParty r = invoice.getRecipient();
        String buyer = Messages.ZUGFERD_BUYER + ": ";
        check(problems, buyer, Messages.ZUGFERD_FIELD_NAME, r.getName());
        check(problems, buyer, Messages.ZUGFERD_FIELD_STREET, r.getStreet());
        check(problems, buyer, Messages.ZUGFERD_FIELD_ZIP, r.getZIP());
        check(problems, buyer, Messages.ZUGFERD_FIELD_CITY, r.getLocation());
        check(problems, buyer, Messages.ZUGFERD_FIELD_EMAIL, r.getUriUniversalCommunicationID());
        return problems;
    }

    private static void check(List<String> problems, String prefix, Messages field, String value) {
        if (isEmpty(value)) {
            problems.add(prefix + field);
        }
    }

    /**
     * Write the invoice as XRechnung XML
     *
     * @param invoice
     * @param target
     * @throws IOException
     */
    public static void writeXml(Invoice invoice, File target) throws IOException {
        ZUGFeRD2PullProvider provider = new ZUGFeRD2PullProvider();
        provider.setProfile(Profiles.getByName(Format.XRECHNUNG.getProfile()));
        provider.generateXML(invoice);
        Files.write(target.toPath(), provider.getXML());
    }

    /**
     * Embed the invoice XML into the given PDF (ZUGFeRD 2 / Factur-X)
     *
     * @param invoice
     * @param pdf the visual representation of the invoice
     * @param target
     * @throws IOException
     */
    public static void writePdf(Invoice invoice, File pdf, File target) throws IOException {
        ZUGFeRDExporterFromA3 exporter = new ZUGFeRDExporterFromA3()
                .setZUGFeRDVersion(2)
                .setProfile(Profiles.getByName(Format.ZUGFERD.getProfile()))
                .setProducer(Constants.TITLE)
                .setCreator(Constants.TITLE)
                .ignorePDFAErrors()
                .load(pdf.getAbsolutePath());
        try {
            exporter.setTransaction(invoice);
            exporter.export(target.getAbsolutePath());
        } finally {
            exporter.close();
        }
    }

    /**
     * Create the invoice PDF using the configured invoice template
     */
    private static File renderPdf(Item item) throws Exception {
        Template template = TemplateHandler.loadTemplate((long) item.templateGroupIds(), item.__getInttype());
        if (template == null) {
            throw new IllegalStateException(Messages.NO_TEMPLATE_LOADED + " (" + User.getCurrentUser() + ")");
        }
        Export export = (Export) Export.createFile(item.__getCnumber().replaceAll("[^A-Za-z0-9._-]", "_"), template, item);
        Exception ex = export.waitFor();
        if (ex != null) {
            throw ex;
        }
        return export.getTargetFile();
    }

    /**
     * Lookup for the company settings of the current user
     *
     * @return
     */
    public static Function<String, String> companySettings() {
        return new Function<String, String>() {
            @Override
            public String apply(String key) {
                return User.getCurrentUser().getProperty(COMPANY_PREFIX + key);
            }
        };
    }

    /**
     * Find the buyer reference (Leitweg-ID) of an invoice: value property on
     * the invoice, value property on the contact or the customer number
     *
     * @param item
     * @param contact
     * @return
     */
    public static String findBuyerReference(Item item, Contact contact) {
        String ref = valueProperty(item, KEY_BUYER_REFERENCE);
        if (isEmpty(ref)) {
            ref = valueProperty(contact, KEY_BUYER_REFERENCE);
        }
        if (isEmpty(ref)) {
            ref = contact.__getCNumber();
        }
        return ref;
    }

    private static String valueProperty(mpv5.db.common.DatabaseObject owner, String key) {
        try {
            if (owner != null && owner.isExisting() && ValueProperty.hasProperty(owner, key)) {
                Object v = ValueProperty.getProperty(owner, key).getValue();
                return v == null ? null : String.valueOf(v).trim();
            }
        } catch (Exception e) {
            Log.Debug(ZugpferdFile.class, e.getMessage());
        }
        return null;
    }
    private static final Map<String, String> COUNTRIES = new HashMap<String, String>();

    static {
        //ISO 3166 numeric codes as used in the YaBS country list -> alpha 2
        String[][] c = {
            {"40", "AT"}, {"56", "BE"}, {"100", "BG"}, {"191", "HR"}, {"196", "CY"}, {"203", "CZ"},
            {"208", "DK"}, {"233", "EE"}, {"246", "FI"}, {"250", "FR"}, {"276", "DE"}, {"300", "GR"},
            {"348", "HU"}, {"352", "IS"}, {"372", "IE"}, {"380", "IT"}, {"428", "LV"}, {"438", "LI"},
            {"440", "LT"}, {"442", "LU"}, {"470", "MT"}, {"492", "MC"}, {"528", "NL"}, {"578", "NO"},
            {"616", "PL"}, {"620", "PT"}, {"642", "RO"}, {"643", "RU"}, {"703", "SK"}, {"705", "SI"},
            {"724", "ES"}, {"752", "SE"}, {"756", "CH"}, {"792", "TR"}, {"804", "UA"}, {"826", "GB"},
            {"840", "US"}, {"891", "RS"}, {"688", "RS"}
        };
        for (String[] e : c) {
            COUNTRIES.put(e[0], e[1]);
        }
    }

    /**
     * Convert a YaBS country value (ISO numeric code, ISO alpha 2 code or
     * country name) to ISO 3166 alpha 2. Defaults to DE.
     *
     * @param country
     * @return
     */
    public static String toCountryCode(String country) {
        if (isEmpty(country)) {
            return "DE";
        }
        String c = country.trim();
        if (c.matches("\\d+")) {
            String code = COUNTRIES.get(String.valueOf(Integer.parseInt(c)));
            return code != null ? code : "DE";
        }
        if (c.matches("[A-Za-z]{2}")) {
            return c.toUpperCase(Locale.ROOT);
        }
        for (String iso : Locale.getISOCountries()) {
            Locale l = new Locale("", iso);
            if (c.equalsIgnoreCase(l.getDisplayCountry(Locale.ENGLISH))
                    || c.equalsIgnoreCase(l.getDisplayCountry(Locale.GERMAN))) {
                return iso;
            }
        }
        if (c.toLowerCase(Locale.ROOT).startsWith("deutsch")) {
            return "DE";
        }
        return "DE";
    }
    private static final Map<String, String> UNITS = new HashMap<String, String>();

    static {
        //UN/ECE Recommendation 20 unit codes
        String[][] u = {
            {"C62", "stk", "stk.", "stück", "stueck", "st", "st.", "pcs", "pc", "piece", "pieces", "x", "einheit", "unit"},
            {"HUR", "h", "std", "std.", "stunde", "stunden", "hour", "hours", "hr", "hrs"},
            {"MIN", "min", "min.", "minute", "minuten", "minutes"},
            {"DAY", "tag", "tage", "day", "days", "d", "pt", "personentag", "personentage"},
            {"WEE", "woche", "wochen", "week", "weeks"},
            {"MON", "monat", "monate", "month", "months", "mon"},
            {"ANN", "jahr", "jahre", "year", "years"},
            {"KGM", "kg"}, {"GRM", "g"}, {"TNE", "t"},
            {"MTR", "m"}, {"KMT", "km"}, {"CMT", "cm"}, {"MMT", "mm"},
            {"MTK", "m2", "m²", "qm"}, {"MTQ", "m3", "m³", "cbm"},
            {"LTR", "l", "liter"},
            {"KWH", "kwh"},
            {"LS", "pauschal", "psch", "psch.", "pausch.", "pauschale", "lump sum", "flat"},
            {"SET", "set", "sets", "satz"},
            {"PR", "paar", "pair"},
            {"PK", "pack", "packung", "pkg", "paket"}
        };
        for (String[] row : u) {
            for (int i = 1; i < row.length; i++) {
                UNITS.put(row[i], row[0]);
            }
        }
    }

    /**
     * Convert a YaBS measure to a UN/ECE Recommendation 20 unit code. Defaults
     * to C62 (one/piece).
     *
     * @param measure
     * @return
     */
    public static String toUnitCode(String measure) {
        if (isEmpty(measure)) {
            return "C62";
        }
        String m = measure.trim();
        String code = UNITS.get(m.toLowerCase(Locale.ROOT));
        if (code != null) {
            return code;
        }
        if (m.matches("[A-Z0-9]{2,3}")) {
            return m;//assume it already is a unit code
        }
        return "C62";
    }

    /**
     * Remove html markup from item texts
     *
     * @param text
     * @return
     */
    public static String plainText(String text) {
        if (text == null) {
            return "";
        }
        String t = text;
        if (t.contains("<")) {
            t = t.replaceAll("(?i)<br\\s*/?>|</p>|</div>|</li>", "\n")
                    .replaceAll("<[^>]*>", "")
                    .replace("&nbsp;", " ").replace("&lt;", "<").replace("&gt;", ">")
                    .replace("&quot;", "\"").replace("&amp;", "&");
        }
        return t.replaceAll("[ \\t]+", " ").replaceAll("\\s*\\n\\s*", "\n").trim();
    }

    public static boolean isEmpty(String s) {
        return s == null || s.trim().isEmpty();
    }

    public static String nvl(String s) {
        return s == null ? "" : s;
    }

    public static String firstNonEmpty(String... values) {
        for (String v : values) {
            if (!isEmpty(v)) {
                return v.trim();
            }
        }
        return "";
    }

    public static String join(String separator, String... values) {
        StringBuilder b = new StringBuilder();
        for (String v : values) {
            if (!isEmpty(v)) {
                if (b.length() > 0) {
                    b.append(separator);
                }
                b.append(v.trim());
            }
        }
        return b.toString();
    }
}
