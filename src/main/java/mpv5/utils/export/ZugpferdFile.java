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
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
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
import mpv5.utils.text.Iban;
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
 * the customer number is used. The due date is the invoice date plus the
 * payment term of the contact ({@value #DEFAULT_PAYTERM_DAYS} days if the
 * contact has none).
 * <p>
 * After writing, the file is checked with the official validator (see
 * {@link EInvoiceValidator}), the result is available via
 * {@link #getValidation()}.
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
    /**
     * Payment term used if the contact has none
     */
    public static final int DEFAULT_PAYTERM_DAYS = 14;
    /**
     * payment term not set (0 days means payable immediately)
     */
    public static final int PAYTERM_UNSET = -1;
    private static final String COMPANY_PREFIX = "companyinfo.";
    private final Item item;
    private final File target;
    private final Format format;
    private final Invoice prepared;
    private EInvoiceValidator.Result validation;

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
     * @return the result of the validation of the written file, null if the
     * file has not been written (yet) or the validator could not run
     */
    public EInvoiceValidator.Result getValidation() {
        return validation;
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
            validation = null;
            Invoice invoice = prepared != null ? prepared : createInvoice(item, format);
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
            try {
                validation = EInvoiceValidator.validate(target);
            } catch (Throwable t) {
                //the file has been written, a missing validator must not hide that
                Log.Debug(this, "E-Rechnung could not be validated: " + t);
            }
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
     * @param format the target format (decides e.g. the document type code)
     * @return
     * @throws NodataFoundException if the contact does not exist
     */
    public static Invoice createInvoice(Item item, Format format) throws NodataFoundException {
        if (!supports(item)) {
            throw new IllegalArgumentException(Messages.ZUGFERD_NOT_AN_INVOICE + ": " + item);
        }
        Contact contact = item.getContact();
        Function<String, String> company = companySettings();
        Template template = null;
        try {
            template = TemplateHandler.loadTemplate((long) item.templateGroupIds(), item.__getInttype());
        } catch (Exception e) {
            Log.Debug(ZugpferdFile.class, e.getMessage());
        }
        //account, payment term and Skonto: contact, else defaults (template, company info)
        PaymentTerms terms = PaymentTerms.forContact(contact, contact.__getPayterm(), paytermOf(template), company);
        Invoice invoice = createInvoice(item, item.getSubitems(), createSender(company, terms.getAccount()),
                contact.asZugpferdTradeparty(), findBuyerReference(item, contact), terms.getDays(),
                company.apply(KEY_TAX_EXEMPTION_REASON), format);
        applySkonto(invoice, terms);
        return invoice;
    }

    /**
     * Build the Mustang invoice (XRechnung) from the given parts (no database
     * access)
     *
     * @see #createInvoice(Item, SubItem[], TradeParty, TradeParty, String,
     * int, String, Format)
     */
    public static Invoice createInvoice(Item item, SubItem[] subitems, TradeParty sender, TradeParty recipient,
            String buyerReference, int paytermDays, String taxExemptionReason) {
        return createInvoice(item, subitems, sender, recipient, buyerReference, paytermDays, taxExemptionReason,
                Format.XRECHNUNG);
    }

    /**
     * Build the Mustang invoice from the given parts (no database access)
     *
     * @param item
     * @param subitems
     * @param sender
     * @param recipient
     * @param buyerReference
     * @param paytermDays payment term in days, 0 for payable immediately
     * (due on the invoice date), &lt; 0 for the default of
     * {@value #DEFAULT_PAYTERM_DAYS} days
     * @param taxExemptionReason
     * @param format
     * @return
     */
    public static Invoice createInvoice(Item item, SubItem[] subitems, TradeParty sender, TradeParty recipient,
            String buyerReference, int paytermDays, String taxExemptionReason, Format format) {
        Date issueDate = item.__getDateadded() != null ? item.__getDateadded() : new Date();
        //Item#datetodo is the "last edited" date, not a due date - always use the payment term
        Date dueDate = dueDate(issueDate, paytermDays);

        Invoice invoice = new Invoice()
                .setNumber(item.__getCnumber())
                .setIssueDate(issueDate)
                .setDeliveryDate(issueDate)
                .setCurrency(DEFAULT_CURRENCY)
                .setSender(sender)
                .setRecipient(recipient)
                .setReferenceNumber(isEmpty(buyerReference) ? item.__getCnumber() : buyerReference.trim());
        invoice.setDueDate(dueDate);
        invoice.setPaymentTermDescription(paymentTermsText(paytermDays, dueDate));
        String code = documentCode(item.__getInttype(), format);
        if (code != null) {
            invoice.setDocumentCode(code);
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
     * Add the Skonto of the payment terms to the e-invoice, in the form
     * required by XRechnung (BR-DE-18): one line "#SKONTO#TAGE=7#PROZENT=2.00#"
     * in the payment terms, each line ending with a line break
     *
     * @param invoice
     * @param terms
     */
    public static void applySkonto(Invoice invoice, PaymentTerms terms) {
        if (terms == null || !terms.hasSkonto()) {
            return;
        }
        String text = nvl(invoice.getPaymentTermDescription()).trim();
        invoice.setPaymentTermDescription((text.isEmpty() ? "" : text + "\n") + terms.xrechnungSkonto() + "\n");
    }

    /**
     * Value property of an invoice template: the payment term (days) written
     * in the template text
     */
    public static final String KEY_TEMPLATE_PAYTERM = "einvoice.payterm";
    /**
     * Company setting (companyinfo.payterm): default payment term (days)
     */
    public static final String KEY_PAYTERM = "payterm";

    /**
     * The payment term of an e-invoice: the contact's term if set, otherwise
     * the term of the invoice template, otherwise the default of the company
     * settings, otherwise {@value #DEFAULT_PAYTERM_DAYS} days. 0 days means
     * payable immediately.
     *
     * @param contactDays {@link #PAYTERM_UNSET} if not set
     * @param templateDays {@link #PAYTERM_UNSET} if not set
     * @param defaultDays {@link #PAYTERM_UNSET} if not set
     * @return the days
     */
    public static int paytermDays(int contactDays, int templateDays, int defaultDays) {
        if (contactDays >= 0) {
            return contactDays;
        }
        if (templateDays >= 0) {
            return templateDays;
        }
        return defaultDays >= 0 ? defaultDays : DEFAULT_PAYTERM_DAYS;
    }

    /**
     * @param days the payment term, 0 for payable immediately
     * @param dueDate
     * @return the payment terms text, e.g. "Zahlbar bis 29.11.2026" or
     * "Zahlbar sofort ohne Abzug"
     */
    public static String paymentTermsText(int days, Date dueDate) {
        return days == 0 ? Messages.ZUGFERD_PAYABLE_IMMEDIATELY.toString()
                : Messages.ZUGFERD_PAYABLE_UNTIL + " " + new SimpleDateFormat("dd.MM.yyyy").format(dueDate);
    }

    /**
     * @param template an invoice template, may be null
     * @return its payment term in days (0 = payable immediately),
     * {@link #PAYTERM_UNSET} if not set
     */
    public static int paytermOf(mpv5.db.common.DatabaseObject template) {
        Object v = template == null ? null : valueProperty(template, KEY_TEMPLATE_PAYTERM);
        return v == null ? PAYTERM_UNSET : parseTerm(String.valueOf(v));
    }

    /**
     * @param template the (saved) template
     * @param days the payment term (0 = payable immediately), &lt; 0 removes it
     */
    public static void savePayterm(mpv5.db.common.DatabaseObject template, int days) {
        if (days >= 0) {
            ValueProperty.addOrUpdateProperty(KEY_TEMPLATE_PAYTERM, String.valueOf(days), template);
        } else if (ValueProperty.hasProperty(template, KEY_TEMPLATE_PAYTERM)) {
            ValueProperty.deleteProperty(template, KEY_TEMPLATE_PAYTERM);
        }
    }

    /**
     * @param value a payment term in days, may be empty
     * @return the days (0 = payable immediately), {@link #PAYTERM_UNSET} if
     * empty or invalid
     */
    public static int parseTerm(String value) {
        try {
            int days = isEmpty(value) ? PAYTERM_UNSET : Integer.parseInt(value.trim());
            return days < 0 ? PAYTERM_UNSET : days;
        } catch (NumberFormatException e) {
            return PAYTERM_UNSET;
        }
    }

    static int parseDays(String value) {
        try {
            return isEmpty(value) ? 0 : Math.max(0, Integer.parseInt(value.trim()));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * @param issueDate
     * @param paytermDays 0 = payable immediately, &lt; 0 for the default
     * @return issue date + payment term (or {@value #DEFAULT_PAYTERM_DAYS}
     * days)
     */
    public static Date dueDate(Date issueDate, int paytermDays) {
        return DateConverter.addDays(issueDate, paytermDays >= 0 ? paytermDays : DEFAULT_PAYTERM_DAYS);
    }

    /**
     * UNTDID 1001 document type code
     *
     * @param itemType
     * @param format
     * @return the code or null for the default (380, commercial invoice)
     */
    static String documentCode(int itemType, Format format) {
        if (itemType == Item.TYPE_PART_PAYMENT) {
            return "326";//partial invoice
        }
        if (itemType == Item.TYPE_DEPOSIT && format == Format.ZUGFERD) {
            //prepayment invoice; not allowed by XRechnung (BR-DE-17), there it is a 380
            return "386";
        }
        return null;
    }

    /**
     * Create the seller from the company settings
     *
     * @param company lookup for <code>companyinfo.*</code> keys (without prefix)
     * @return
     */
    public static TradeParty createSender(Function<String, String> company) {
        return createSender(company, null);
    }

    /**
     * Create the seller from the company settings
     *
     * @param company lookup for <code>companyinfo.*</code> keys (without prefix)
     * @param payTo the account the customer pays to, null for the account of
     * the company info
     * @return
     */
    public static TradeParty createSender(Function<String, String> company, BankAccount payTo) {
        String person = join(" ", company.apply("firstname"), company.apply("name"));
        String name = firstNonEmpty(company.apply("business"), person);

        TradeParty sender = new TradeParty(name, nvl(company.apply("street")), nvl(company.apply("zip")),
                nvl(company.apply("city")), toCountryCode(company.apply("country")));

        //"account1" is labelled VAT ID, "taxnumber" tax number - but users mix them up, so the
        //field holding a well formed VAT ID wins
        String vatField = firstNonEmpty(company.apply("vatid"), company.apply("account1"));
        String taxField = nvl(company.apply("taxnumber")).trim();
        String vatId = isValidVatId(vatField) ? vatField : isValidVatId(taxField) ? taxField : vatField;
        if (!isEmpty(vatId)) {
            //kept even if invalid, validate() explains how to fix it
            sender.addVATID(normalizeVatId(vatId));
        }
        String taxNumber = !taxField.isEmpty() && !taxField.equals(vatId) ? taxField
                : !vatField.isEmpty() && !vatField.equals(vatId) && !isValidVatId(vatField) ? vatField : "";
        if (!taxNumber.isEmpty()) {
            sender.addTaxID(taxNumber);
        }

        //the account the customer pays to (default: the account of the company info)
        BankAccount account = payTo != null ? payTo : BankAccount.mainAccount(company);
        if (account != null) {
            BankDetails bank = new BankDetails(account.getIban());
            if (!account.getBic().isEmpty()) {
                bank.setBIC(account.getBic());
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
        checkCountry(problems, seller, s.getCountry());
        //BR-CO-26: the seller must be identified by the VAT ID, a tax number alone is not enough
        if (isEmpty(s.getVATID())) {
            problems.add(seller + Messages.ZUGFERD_FIELD_VATID);
        } else if (!isValidVatId(s.getVATID())) {
            problems.add(seller + Messages.ZUGFERD_INVALID_VATID + " '" + s.getVATID() + "'");
        }
        check(problems, seller, Messages.ZUGFERD_FIELD_EMAIL, s.getUriUniversalCommunicationID());
        if (s.getContact() == null || isEmpty(s.getContact().getPhone())) {
            problems.add(seller + Messages.ZUGFERD_FIELD_PHONE);
        }
        if (s.getBankDetails() == null || s.getBankDetails().isEmpty()) {
            problems.add(seller + Messages.ZUGFERD_FIELD_IBAN);
        } else {
            for (BankDetails bank : s.getBankDetails()) {
                if (!Iban.isValid(bank.getIBAN())) {
                    problems.add(seller + Messages.ZUGFERD_INVALID_IBAN + ": " + bank.getIBAN());
                }
                if (!isEmpty(bank.getBIC()) && !Iban.isValidBic(bank.getBIC())) {
                    problems.add(seller + Messages.ZUGFERD_INVALID_BIC + ": " + bank.getBIC());
                }
            }
        }

        TradeParty r = invoice.getRecipient();
        String buyer = Messages.ZUGFERD_BUYER + (isEmpty(r.getName()) ? "" : " '" + r.getName().trim() + "'") + ": ";
        check(problems, buyer, Messages.ZUGFERD_FIELD_NAME, r.getName());
        check(problems, buyer, Messages.ZUGFERD_FIELD_STREET, r.getStreet());
        check(problems, buyer, Messages.ZUGFERD_FIELD_ZIP, r.getZIP());
        check(problems, buyer, Messages.ZUGFERD_FIELD_CITY, r.getLocation());
        checkCountry(problems, buyer, r.getCountry());
        check(problems, buyer, Messages.ZUGFERD_FIELD_EMAIL, r.getUriUniversalCommunicationID());
        return problems;
    }

    /**
     * Remove spaces, dots and dashes, upper case
     *
     * @param vatId
     * @return
     */
    public static String normalizeVatId(String vatId) {
        return vatId == null ? "" : vatId.replaceAll("[\\s.\\-]", "").toUpperCase(Locale.ROOT);
    }

    /**
     * Check the form of a VAT identification number: ISO 3166 country prefix
     * (EL for Greece, XI for Northern Ireland) followed by the national number
     * (BR-CO-09). German numbers must have 9 digits.
     *
     * @param vatId
     * @return
     */
    public static boolean isValidVatId(String vatId) {
        String v = normalizeVatId(vatId);
        if (!v.matches("[A-Z]{2}[A-Z0-9+*]{2,14}")) {
            return false;
        }
        String prefix = v.substring(0, 2);
        if ("DE".equals(prefix)) {
            return v.matches("DE[0-9]{9}");
        }
        return "EL".equals(prefix) || "XI".equals(prefix) || ISO_COUNTRIES.contains(prefix);
    }

    /**
     * Check the VAT ID of a contact. EN 16931 only knows a VAT ID for the
     * buyer, anything else in the field (e.g. a tax number) is left out of the
     * e-invoice.
     *
     * @param contact
     * @param address the invoice address, may be null
     * @return a problem description or null
     */
    public static String checkBuyerVatId(Contact contact, mpv5.db.objects.Address address) {
        String value = firstNonEmpty(address == null ? null : address.__getTaxnumber(), contact.__getTaxnumber());
        if (!isEmpty(value) && !isValidVatId(value)) {
            String name = firstNonEmpty(contact.__getCompany(), contact.__getCname());
            return Messages.ZUGFERD_BUYER + (name.isEmpty() ? "" : " '" + name + "'") + ": "
                    + Messages.ZUGFERD_BUYER_VATID_IGNORED + " '" + value.trim() + "'";
        }
        return null;
    }

    private static void checkCountry(List<String> problems, String prefix, String code) {
        if (isEmpty(code) || !ISO_COUNTRIES.contains(code)) {
            problems.add(prefix + Messages.ZUGFERD_FIELD_COUNTRY);
        }
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
        mpv5.handler.Activity.file(target, null);
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
        mpv5.handler.Activity.file(target, null);
    }

    /**
     * Create the invoice PDF using the configured invoice template. All fonts
     * are embedded, as required by PDF/A-3.
     */
    private static File renderPdf(Item item) throws Exception {
        Template template = TemplateHandler.loadTemplate((long) item.templateGroupIds(), item.__getInttype());
        if (template == null) {
            throw new IllegalStateException(Messages.NO_TEMPLATE_LOADED + " (" + User.getCurrentUser() + ")");
        }
        Export export = (Export) Export.createFile(item.__getCnumber().replaceAll("[^A-Za-z0-9._-]", "_"), template, item);
        export.setEmbedAllFonts(true);
        Exception ex = export.waitFor();
        if (ex != null) {
            throw ex;
        }
        File pdf = export.getTargetFile();
        //the template engine logs errors instead of throwing them
        if (pdf == null || !pdf.isFile() || pdf.length() == 0) {
            throw new IOException(Messages.ZUGFERD_PDF_FAILED + ": " + template);
        }
        return pdf;
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
            ref = ContactProperty.get(contact, KEY_BUYER_REFERENCE);
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
    private static final Set<String> ISO_COUNTRIES = new HashSet<String>(Arrays.asList(Locale.getISOCountries()));

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
     * Convert a YaBS country value (ISO numeric code, ISO alpha 2/3 code or
     * country name) to ISO 3166 alpha 2. An empty value defaults to DE.
     *
     * @param country
     * @return the code or an empty String if the country is unknown
     * (reported by {@link #validate(Invoice)})
     */
    public static String toCountryCode(String country) {
        if (isEmpty(country)) {
            return "DE";
        }
        String c = country.trim();
        if (c.matches("\\d{1,3}")) {
            String code = COUNTRIES.get(String.valueOf(Integer.parseInt(c)));
            return code != null ? code : "";
        }
        String upper = c.toUpperCase(Locale.ROOT);
        if (upper.matches("[A-Z]{2}")) {
            return "UK".equals(upper) ? "GB" : ISO_COUNTRIES.contains(upper) ? upper : "";
        }
        for (String iso : ISO_COUNTRIES) {
            Locale l = new Locale("", iso);
            if (c.equalsIgnoreCase(l.getDisplayCountry(Locale.ENGLISH))
                    || c.equalsIgnoreCase(l.getDisplayCountry(Locale.GERMAN))
                    || upper.equals(l.getISO3Country())) {
                return iso;
            }
        }
        String lower = c.toLowerCase(Locale.ROOT);
        if (lower.startsWith("deutsch") || lower.equals("brd")) {
            return "DE";
        }
        if (lower.equals("usa") || lower.startsWith("vereinigte staaten") || lower.startsWith("united states")) {
            return "US";
        }
        if (lower.equals("england") || lower.startsWith("großbritannien") || lower.startsWith("grossbritannien")) {
            return "GB";
        }
        return "";
    }
    private static final Map<String, String> UNITS = new HashMap<String, String>();
    /**
     * UN/ECE Recommendation 20/21 codes accepted as they are
     */
    private static final Set<String> UNIT_CODES = new HashSet<String>(Arrays.asList(
            "C62", "H87", "EA", "XPP", "XPK", "XBX", "XCT", "XPA", "XRO", "XBG", "XBO", "XCN", "XPL", "E48", "P1", "LS",
            "SET", "PR", "PK", "NAR", "NPR", "SEC", "MIN", "HUR", "DAY", "WEE", "MON", "ANN", "QAN",
            "KGM", "GRM", "MGM", "TNE", "MTR", "KMT", "CMT", "MMT", "DMT", "MTK", "CMK", "KMK", "HAR", "MTQ", "CMQ",
            "LTR", "MLT", "HLT", "KWH", "MWH", "KWT", "WTT", "KVA", "AMP", "VLT", "KTM", "KMH", "TKM", "D64",
            "ZP", "ZZ", "A9", "E49", "E50", "E51", "E53", "KT", "KMQ", "B4", "LBR", "ONZ", "FOT", "INH", "GLL"));

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
        if (UNIT_CODES.contains(m.toUpperCase(Locale.ROOT))) {
            return m.toUpperCase(Locale.ROOT);//already a unit code
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
