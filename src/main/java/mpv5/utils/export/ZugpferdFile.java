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
import org.mustangproject.SchemedID;
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
 * the customer number is used. The buyer electronic address (Peppol-ID, BT-49)
 * is read from {@value #KEY_PEPPOL_ID} on the invoice or contact (falling back
 * to the contact email with scheme {@code EM}); the seller electronic address
 * (BT-34) is read from {@code companyinfo.peppolid} (falling back to the
 * company email with scheme {@code EM}). The due date is the invoice date plus
 * the payment term of the contact ({@value #DEFAULT_PAYTERM_DAYS} days if the
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
     * Value property (on invoice or contact) holding the buyer Peppol-ID /
     * electronic address (BT-49) in the form {@code <EAS-scheme>:<id>}
     */
    public static final String KEY_PEPPOL_ID = "peppol-id";
    /**
     * Company setting ({@code companyinfo.peppolid}) holding the seller
     * Peppol-ID / electronic address (BT-34) in the form {@code <EAS-scheme>:<id>}
     */
    public static final String KEY_COMPANY_PEPPOL_ID = "peppolid";
    /**
     * Contact value property and company setting ({@code companyinfo.eori})
     * holding the EU customs EORI number
     */
    public static final String KEY_EORI = "eori";
    /**
     * Value property on invoice (or contact {@code bankcurrency}) holding the
     * ISO 4217 currency code (BT-5, e.g. {@code EUR}, {@code CHF}, {@code USD})
     */
    public static final String KEY_CURRENCY = "currency";
    /**
     * Value property (on invoice or contact) holding Incoterms® + named place
     * (e.g. {@code DAP Zürich}, {@code FCA München})
     */
    public static final String KEY_INCOTERMS = "incoterms";
    /**
     * Value property on invoice holding the total gross weight in kg
     */
    public static final String KEY_GROSS_WEIGHT = "grossweight";
    /**
     * Value property on invoice holding the total net weight in kg (overrides
     * sum of product weights when set)
     */
    public static final String KEY_NET_WEIGHT = "netweight";
    /**
     * Value property on invoice holding the package count / description
     */
    public static final String KEY_PACKAGES = "packages";
    /**
     * Company setting holding the VAT exemption reason used for 0% VAT lines
     * of category E whose tax rate has no note (else the default note of the
     * category, {@link VatCategory#defaultNote()})
     */
    public static final String KEY_TAX_EXEMPTION_REASON = "taxexemptionreason";
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

    private Template[] appended;

    /**
     * @param appended templates whose pages follow the invoice in the PDF
     * (e.g. the order confirmation), may be null
     */
    public void setAppended(Template[] appended) {
        this.appended = appended;
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
        //credit notes / cancellation invoices: a correction of an e-invoice must be an e-invoice as well
        return type == Item.TYPE_INVOICE || type == Item.TYPE_PART_PAYMENT || type == Item.TYPE_DEPOSIT
                || type == Item.TYPE_CREDIT;
    }

    @Override
    public Exception waitFor() {
        try {
            mpv5.YabsViewProxy.instance().setWaiting(true);
            validation = null;
            if (prepared == null) {
                ExportProgress.step(Messages.EXPORT_STEP_EINVOICE_DATA);
            }
            Invoice invoice = prepared != null ? prepared : createInvoice(item, format);
            //no e-invoice with missing or wrong data (company info, customer): it would be rejected
            List<String> missing = validate(invoice, SmallBusiness.isEnabled());
            try {
                String buyerVatId = checkBuyerVatId(item.getContact(), item.getContact().getInvoiceaddress());
                if (buyerVatId != null) {
                    missing.add(buyerVatId);
                }
            } catch (Exception e) {
                Log.Debug(this, e.getMessage());
            }
            if (!missing.isEmpty()) {
                return new ExportFailedException(Messages.EINVOICE_NOT_CREATED + ", " + Messages.ZUGFERD_MISSING_DATA
                        + "\n - " + String.join("\n - ", missing), null);
            }
            if (format == Format.XRECHNUNG) {
                ExportProgress.step(Messages.EXPORT_STEP_EINVOICE_XML);
                writeXml(invoice, target);
            } else {
                File pdf = renderPdf(item, appended);
                try {
                    ExportProgress.step(Messages.EXPORT_STEP_EINVOICE_EMBED);
                    writePdf(invoice, pdf, target);
                } finally {
                    pdf.delete();
                }
            }
            Log.Debug(this, "E-Rechnung written: " + target);
            try {
                ExportProgress.step(Messages.EXPORT_STEP_VALIDATE);
                validation = EInvoiceValidator.validate(target);
            } catch (Throwable t) {
                //the file has been written, a missing validator must not hide that
                Log.Debug(this, "E-Rechnung could not be validated: " + t);
            }
            if (validation != null && !validation.isValid()) {
                //an invalid e-invoice is not kept: it would be rejected by the recipient
                target.delete();
                return new ExportFailedException(Messages.EINVOICE_NOT_CREATED + ", " + Messages.EINVOICE_INVALID
                        + "\n - " + String.join("\n - ", validation.getErrors()), null);
            }
            //the file of this invoice (history, plugins; the checksum to verify it later)
            mpv5.handler.Activity.file(target, item);
            return null;
        } catch (Exception ex) {
            Log.Debug(ex);
            //no half written file
            if (target != null && target.isFile()) {
                target.delete();
            }
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
        TradeParty recipient = contact.asZugpferdTradeparty();
        applyPeppolId(recipient, findBuyerPeppolId(item, contact));
        Invoice invoice = createInvoice(item, item.getSubitems(), createSender(company, terms.getAccount()),
                recipient, findBuyerReference(item, contact), terms.getDays(),
                company.apply(KEY_TAX_EXEMPTION_REASON), SmallBusiness.note(company), format,
                taxRates(contact, mpv5.db.objects.Tax.all()));
        invoice.setCurrency(findCurrency(item, contact));
        applyDeliveryAddress(invoice, contact);
        applySkonto(invoice, terms);
        applyEoriNotes(invoice, company.apply(KEY_EORI), ContactProperty.get(contact, KEY_EORI));
        applyCustomsNotes(invoice, item, contact);
        Item corrected = item.__getInttype() == Item.TYPE_CREDIT ? item.getCorrectedInvoice() : null;
        if (corrected != null) {
            applyPrecedingInvoice(invoice, corrected.__getCnumber(), corrected.__getDateadded());
        }
        if (item.__getInttype() == Item.TYPE_INVOICE) {
            //a final invoice: the deposits / part payment invoices deducted in it (BG-3, #221)
            for (Item d : mpv5.handler.ItemService.deductedIn(item)) {
                addPrecedingInvoice(invoice, d.__getCnumber(), d.__getDateadded());
            }
        }
        return invoice;
    }

    /**
     * Adds a preceding invoice (BG-3: BT-25 number, BT-26 date), e.g. a
     * deposit invoice deducted in a final invoice; there may be several
     *
     * @param invoice
     * @param number nothing is added if empty
     * @param date may be null
     */
    public static void addPrecedingInvoice(Invoice invoice, String number, Date date) {
        if (isEmpty(number)) {
            return;
        }
        invoice.addInvoiceReferencedDocument(date != null
                ? new org.mustangproject.ReferencedDocument(number.trim(), date)
                : new org.mustangproject.ReferencedDocument(number.trim()));
    }

    /**
     * The invoice a credit note corrects or cancels: preceding invoice
     * reference (BG-3: BT-25 number, BT-26 date)
     *
     * @param invoice
     * @param number the number of the corrected invoice, nothing is added if
     * empty
     * @param date its date, may be null
     */
    public static void applyPrecedingInvoice(Invoice invoice, String number, Date date) {
        if (isEmpty(number)) {
            return;
        }
        invoice.setInvoiceReferencedDocumentID(number.trim());
        if (date != null) {
            invoice.setInvoiceReferencedIssueDate(date);
        }
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
        return createInvoice(item, subitems, sender, recipient, buyerReference, paytermDays, taxExemptionReason,
                null, format);
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
     * @param smallBusinessNote the note of the small business exemption
     * ({@link SmallBusiness#note}), empty or null if it does not apply. With
     * it, it is the exemption reason of the 0 % lines and a note (BT-22) of
     * the invoice
     * @param format
     * @return
     */
    public static Invoice createInvoice(Item item, SubItem[] subitems, TradeParty sender, TradeParty recipient,
            String buyerReference, int paytermDays, String taxExemptionReason, String smallBusinessNote,
            Format format) {
        return createInvoice(item, subitems, sender, recipient, buyerReference, paytermDays, taxExemptionReason,
                smallBusinessNote, format, null);
    }

    /**
     * The tax rate of each position: the rate of the customer (contact), then
     * the rate of the product of the position, then any rate with the value of
     * the position ({@link mpv5.db.objects.Tax#forRate})
     *
     * @param contact the customer, may be null
     * @param taxes all tax rates
     * @return the tax rate of a position (null if none has its value)
     */
    public static Function<SubItem, mpv5.db.objects.Tax> taxRates(Contact contact, List<mpv5.db.objects.Tax> taxes) {
        Integer contactTax = null;
        if (contact != null) {
            try {
                String id = mpv5.handler.ContactService.contactTax(contact);
                contactTax = id.isEmpty() ? null : Integer.valueOf(id);
            } catch (Exception e) {
                contactTax = null;
            }
        }
        final Integer customer = contactTax;
        return s -> mpv5.db.objects.Tax.forRate(s.__getTaxpercentvalue(), taxes, customer, productTax(s));
    }

    /**
     * @param s
     * @return the tax rate id of the product of the position, null if none
     */
    private static Integer productTax(SubItem s) {
        if (s.__getOriginalproductsids() <= 0) {
            return null;
        }
        try {
            return ((mpv5.db.objects.Product) mpv5.db.common.DatabaseObject.getObject(
                    mpv5.db.common.Context.getProduct(), s.__getOriginalproductsids())).__getTaxids();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Build the Mustang invoice from the given parts
     *
     * @param item
     * @param subitems
     * @param sender
     * @param recipient
     * @param buyerReference
     * @param paytermDays payment term in days, 0 for payable immediately
     * (due on the invoice date), &lt; 0 for the default of
     * {@value #DEFAULT_PAYTERM_DAYS} days
     * @param taxExemptionReason the reason of 0 % lines of category E whose
     * tax rate has no note; null or empty for the default of the category
     * @param smallBusinessNote the note of the small business exemption, see
     * {@link #createInvoice(Item, SubItem[], TradeParty, TradeParty, String, int, String, String, Format)}
     * @param format
     * @param taxRates the tax rate of a position (VAT category, exemption
     * reason and note, see {@link #taxRates}); null: category S, or E for 0 %
     * @return
     */
    public static Invoice createInvoice(Item item, SubItem[] subitems, TradeParty sender, TradeParty recipient,
            String buyerReference, int paytermDays, String taxExemptionReason, String smallBusinessNote,
            Format format, Function<SubItem, mpv5.db.objects.Tax> taxRates) {
        Date issueDate = item.__getDateadded() != null ? item.__getDateadded() : new Date();
        //Item#datetodo is the service date, not a due date - always use the payment term
        Date dueDate = dueDate(issueDate, paytermDays);

        Invoice invoice = new Invoice()
                .setNumber(item.__getCnumber())
                .setIssueDate(issueDate)
                //BT-72: the service date (Leistungsdatum), the invoice date if not set
                .setDeliveryDate(item.getServiceDate() != null ? item.getServiceDate() : issueDate)
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

        boolean smallBusiness = !isEmpty(smallBusinessNote);
        if (smallBusiness) {
            invoice.addTaxNote(smallBusinessNote.trim());
        }
        String reason = smallBusiness ? smallBusinessNote.trim() : taxExemptionReason;
        int count = 0;
        java.util.Set<String> notes = new java.util.LinkedHashSet<String>();
        for (SubItem subItem : subitems) {
            //small business: every line without VAT is exempt (E) with the note of the exemption
            mpv5.db.objects.Tax rate = smallBusiness || taxRates == null ? null : taxRates.apply(subItem);
            IZUGFeRDExportableItem line = subItem.asZugpferdSubitem(rate, reason);
            if (line != null) {
                invoice.addItem(line);
                count++;
                String category = line.getProduct().getTaxCategoryCode();
                //the wording the law requires on the invoice (reverse charge, intra-community supply,
                //export ...), also as note of the invoice (BT-22); exempt lines (E) carry it as reason only
                if (!smallBusiness && category != null && !"S".equals(category) && !"E".equals(category)
                        && !isEmpty(line.getProduct().getTaxExemptionReason())) {
                    notes.add(line.getProduct().getTaxExemptionReason().trim());
                }
            }
        }
        for (String note : notes) {
            invoice.addTaxNote(note);
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
                : Messages.ZUGFERD_PAYABLE_UNTIL + " " + mpv5.utils.date.DateConverter.getDay(dueDate);
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
        if (itemType == Item.TYPE_CREDIT) {
            return "381";//credit note (also a cancellation invoice: positive amounts, the type reduces)
        }
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
            if (isEmpty(vatId)) {
                //no VAT ID (e.g. small business): BR-CO-26 needs a seller identifier (BT-29),
                //the tax number (BT-32, schemeID FC) identifies the seller
                sender.setID(taxNumber);
            }
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
        //Peppol-ID (BT-34) overrides email (scheme EM) as SellerElectronicAddress; email stays in DefinedTradeContact (BT-43)
        applyPeppolId(sender, company.apply(KEY_COMPANY_PEPPOL_ID));
        //no empty phone / mail element: invalid in XRechnung (PEPPOL-EN16931-R008)
        sender.setContact(new org.mustangproject.Contact(firstNonEmpty(person, name),
                phone.trim().isEmpty() ? null : phone, email.isEmpty() ? null : email));
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
        return validate(invoice, false);
    }

    /**
     * Check the seller and buyer for data which is required by EN 16931 /
     * XRechnung
     *
     * @param invoice
     * @param smallBusiness true if the small business exemption applies: no
     * line may show VAT
     * @return a list of human readable problems, empty if everything is fine
     */
    public static List<String> validate(Invoice invoice, boolean smallBusiness) {
        List<String> problems = sellerProblems(invoice.getSender());
        buyerProblems(problems, invoice.getRecipient());
        if (smallBusiness && invoice.getZFItems() != null) {
            for (IZUGFeRDExportableItem line : invoice.getZFItems()) {
                BigDecimal rate = line.getProduct().getVATPercent();
                if (rate != null && rate.signum() != 0) {
                    problems.add(Messages.SMALLBUSINESS_LINE_HAS_VAT + ": " + line.getProduct().getName()
                            + " (" + rate.stripTrailingZeros().toPlainString() + " %)");
                }
            }
        }
        return problems;
    }

    /**
     * @param s the seller (the company info, {@link #createSender})
     * @return what is missing or wrong for an e-invoice (EN 16931 / XRechnung)
     */
    public static List<String> sellerProblems(TradeParty s) {
        List<String> problems = new ArrayList<String>();
        String seller = Messages.ZUGFERD_SELLER + ": ";
        check(problems, seller, Messages.ZUGFERD_FIELD_NAME, s.getName());
        check(problems, seller, Messages.ZUGFERD_FIELD_STREET, s.getStreet());
        check(problems, seller, Messages.ZUGFERD_FIELD_ZIP, s.getZIP());
        check(problems, seller, Messages.ZUGFERD_FIELD_CITY, s.getLocation());
        checkCountry(problems, seller, s.getCountry());
        //BR-CO-26 / BR-S-02 / BR-E-02: VAT ID, or the tax number (e.g. small business without VAT ID)
        if (isEmpty(s.getVATID()) && isEmpty(s.getTaxID())) {
            problems.add(seller + Messages.ZUGFERD_FIELD_VATID);
        } else if (!isEmpty(s.getVATID()) && !isValidVatId(s.getVATID())) {
            problems.add(seller + Messages.ZUGFERD_INVALID_VATID + " '" + s.getVATID() + "'");
        }
        checkEndpoint(problems, seller, s);
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

        return problems;
    }

    private static void buyerProblems(List<String> problems, TradeParty r) {
        String buyer = Messages.ZUGFERD_BUYER + (isEmpty(r.getName()) ? "" : " '" + r.getName().trim() + "'") + ": ";
        check(problems, buyer, Messages.ZUGFERD_FIELD_NAME, r.getName());
        check(problems, buyer, Messages.ZUGFERD_FIELD_STREET, r.getStreet());
        check(problems, buyer, Messages.ZUGFERD_FIELD_ZIP, r.getZIP());
        check(problems, buyer, Messages.ZUGFERD_FIELD_CITY, r.getLocation());
        checkCountry(problems, buyer, r.getCountry());
        checkEndpoint(problems, buyer, r);
    }

    private static void checkEndpoint(List<String> problems, String prefix, TradeParty party) {
        String id = party.getUriUniversalCommunicationID();
        String scheme = party.getUriUniversalCommunicationIDScheme();
        if (isEmpty(id)) {
            problems.add(prefix + Messages.ZUGFERD_FIELD_EMAIL);
        } else if (!isValidEasScheme(scheme) || id.trim().matches(".*\\s.*")) {
            String value = isEmpty(scheme) ? id.trim() : scheme.trim() + ":" + id.trim();
            problems.add(prefix + Messages.ZUGFERD_INVALID_PEPPOL_ID + " '" + value + "'");
        }
    }

    /**
     * CEF Electronic Address Scheme (EAS) codes allowed for BT-34 / BT-49
     * (EN 16931 rule BR-CL-25)
     */
    private static final Set<String> EAS_SCHEMES = new HashSet<String>(Arrays.asList(
            "0002", "0007", "0009", "0037", "0060", "0088", "0096", "0097", "0106", "0130", "0135", "0142",
            "0147", "0151", "0154", "0158", "0170", "0177", "0183", "0184", "0188", "0190", "0191", "0192",
            "0193", "0194", "0195", "0196", "0198", "0199", "0200", "0201", "0202", "0203", "0204", "0205",
            "0208", "0209", "0210", "0211", "0212", "0213", "0215", "0216", "0217", "0218", "0219", "0220",
            "0221", "0225", "0230", "0235", "0240", "0242", "0244", "0245", "0246", "0248",
            "9910", "9913", "9914", "9915", "9918", "9919", "9920", "9922", "9923", "9924", "9925", "9926",
            "9927", "9928", "9929", "9930", "9931", "9932", "9933", "9934", "9935", "9936", "9937", "9938",
            "9939", "9940", "9941", "9942", "9943", "9944", "9945", "9946", "9947", "9948", "9949", "9950",
            "9951", "9952", "9953", "9957", "9959",
            "AN", "AQ", "AS", "AU", "EM"));

    private static final String PEPPOL_UPIS_PREFIX = "iso6523-actorid-upis::";

    private static String stripPeppolPrefix(String peppolId) {
        if (peppolId == null) {
            return "";
        }
        String trimmed = peppolId.trim();
        if (trimmed.length() > PEPPOL_UPIS_PREFIX.length()
                && trimmed.substring(0, PEPPOL_UPIS_PREFIX.length()).equalsIgnoreCase(PEPPOL_UPIS_PREFIX)) {
            return trimmed.substring(PEPPOL_UPIS_PREFIX.length()).trim();
        }
        return trimmed;
    }

    /**
     * @param scheme EAS scheme identifier (e.g. {@code 0204}, {@code 9930}, {@code EM})
     * @return true if the scheme is part of the EN 16931 EAS code list (BR-CL-25)
     */
    public static boolean isValidEasScheme(String scheme) {
        return !isEmpty(scheme) && EAS_SCHEMES.contains(scheme.trim().toUpperCase(Locale.ROOT));
    }

    /**
     * Parse a Peppol-ID / electronic address in the form {@code <scheme>:<id>}
     * (optionally prefixed with {@code iso6523-actorid-upis::}).
     *
     * @param peppolId
     * @return the parsed {@link SchemedID} (scheme uppercased, id trimmed), or
     * {@code null} if empty or invalid
     */
    public static SchemedID parsePeppolId(String peppolId) {
        String cleaned = stripPeppolPrefix(peppolId);
        int colon = cleaned.indexOf(':');
        if (colon <= 0 || colon >= cleaned.length() - 1) {
            return null;
        }
        String scheme = cleaned.substring(0, colon).trim().toUpperCase(Locale.ROOT);
        String id = cleaned.substring(colon + 1).trim();
        if (!EAS_SCHEMES.contains(scheme) || id.isEmpty() || id.matches(".*\\s.*")) {
            return null;
        }
        return new SchemedID(scheme, id);
    }

    /**
     * @param peppolId
     * @return true if the value is a valid {@code <EAS-scheme>:<id>} electronic address
     */
    public static boolean isValidPeppolId(String peppolId) {
        return parsePeppolId(peppolId) != null;
    }

    /**
     * Normalize a Peppol-ID (strips optional {@code iso6523-actorid-upis::}
     * prefix and whitespace around scheme/id, uppercases scheme).
     *
     * @param peppolId
     * @return normalized string or {@code ""} if null/empty
     */
    public static String normalizePeppolId(String peppolId) {
        SchemedID parsed = parsePeppolId(peppolId);
        return parsed != null ? parsed.getScheme() + ":" + parsed.getID() : stripPeppolPrefix(peppolId);
    }

    /**
     * Apply a Peppol-ID as the electronic address ({@code URIUniversalCommunication},
     * BT-34 for seller or BT-49 for buyer) of a {@link TradeParty}, overriding any
     * {@code EM} email endpoint set earlier. Empty values are ignored.
     *
     * @param party
     * @param peppolId
     */
    public static void applyPeppolId(TradeParty party, String peppolId) {
        if (party == null || isEmpty(peppolId)) {
            return;
        }
        SchemedID parsed = parsePeppolId(peppolId);
        if (parsed != null) {
            party.addUriUniversalCommunicationID(parsed);
        } else {
            //keep invalid value so validate() reports what is wrong instead of silently falling back
            String cleaned = stripPeppolPrefix(peppolId);
            int colon = cleaned.indexOf(':');
            if (colon > 0 && colon < cleaned.length() - 1) {
                party.addUriUniversalCommunicationID(new SchemedID(
                        cleaned.substring(0, colon).trim().toUpperCase(Locale.ROOT),
                        cleaned.substring(colon + 1).trim()));
            } else {
                party.addUriUniversalCommunicationID(new SchemedID("", cleaned));
            }
        }
    }

    /**
     * Remove spaces, dots and dashes from an EORI number, upper case.
     *
     * @param eori
     * @return
     */
    public static String normalizeEori(String eori) {
        return eori == null ? "" : eori.replaceAll("[\\s.\\-]", "").toUpperCase(Locale.ROOT);
    }

    /**
     * Check the form of an EU/UK EORI number (Union Customs Code Annex 12-01):
     * 2-letter ISO 3166 country code (plus {@code EL} for Greece, {@code XI} for
     * Northern Ireland) followed by 1 to 15 alphanumeric characters (max 17 chars
     * total, e.g. {@code DE123456789012345}, {@code DE1234567}, {@code GB123456789000}).
     *
     * @param eori
     * @return
     */
    public static boolean isValidEori(String eori) {
        String v = normalizeEori(eori);
        if (!v.matches("[A-Z]{2}[A-Z0-9]{1,15}")) {
            return false;
        }
        String prefix = v.substring(0, 2);
        return "EL".equals(prefix) || "XI".equals(prefix) || ISO_COUNTRIES.contains(prefix);
    }

    /**
     * Add Seller and/or Buyer EORI numbers to an e-invoice as EN 16931 customs
     * notes (BG-1 / BT-21 = {@code CUS}, BT-22). EN 16931 and ISO 6523 ICD have
     * no dedicated EORI field.
     *
     * @param invoice
     * @param sellerEori may be null or empty
     * @param buyerEori may be null or empty
     */
    public static void applyEoriNotes(Invoice invoice, String sellerEori, String buyerEori) {
        if (invoice == null) {
            return;
        }
        String seller = normalizeEori(sellerEori);
        if (!seller.isEmpty()) {
            invoice.addCustomsNote("Seller EORI: " + seller);
        }
        String buyer = normalizeEori(buyerEori);
        if (!buyer.isEmpty()) {
            invoice.addCustomsNote("Buyer EORI: " + buyer);
        }
    }

    /**
     * Resolve the ISO 4217 currency code (BT-5) for an invoice:
     * <ol>
     * <li>value property {@value #KEY_CURRENCY} on the invoice,</li>
     * <li>{@link Contact#__getBankcurrency()} on the contact,</li>
     * <li>value property {@value #KEY_CURRENCY} on the contact,</li>
     * <li>{@link #DEFAULT_CURRENCY} ({@code "EUR"}).</li>
     * </ol>
     *
     * @param item may be null
     * @param contact may be null
     * @return valid 3-letter ISO 4217 currency code
     */
    public static String findCurrency(Item item, Contact contact) {
        String itemCur = CurrencyCode.normalizeCode(valueProperty(item, KEY_CURRENCY));
        if (CurrencyCode.isValidCode(itemCur)) {
            return itemCur;
        }
        if (contact != null) {
            String bankCur = CurrencyCode.normalizeCode(contact.__getBankcurrency());
            if (CurrencyCode.isValidCode(bankCur)) {
                return bankCur;
            }
            String contactCur = CurrencyCode.normalizeCode(ContactProperty.get(contact, KEY_CURRENCY));
            if (CurrencyCode.isValidCode(contactCur)) {
                return contactCur;
            }
        }
        return DEFAULT_CURRENCY;
    }

    /**
     * Find the Incoterms® (e.g. {@code DAP Zürich}) of an invoice: value
     * property {@value #KEY_INCOTERMS} on the invoice or on the contact.
     *
     * @param item may be null
     * @param contact may be null
     * @return the Incoterms string, or {@code ""} if none
     */
    public static String findIncoterms(Item item, Contact contact) {
        String v = valueProperty(item, KEY_INCOTERMS);
        if (isEmpty(v) && contact != null) {
            v = ContactProperty.get(contact, KEY_INCOTERMS);
        }
        return isEmpty(v) ? "" : v.trim();
    }

    /**
     * Add the Ship-To / Consignee delivery address (EN 16931 BG-13) to the
     * e-invoice when the contact has an explicit delivery address that differs
     * from the invoice address.
     *
     * @param invoice
     * @param contact
     */
    public static void applyDeliveryAddress(Invoice invoice, Contact contact) {
        if (invoice == null || contact == null) {
            return;
        }
        try {
            List<mpv5.db.objects.Address> all = contact.getAdresses();
            if (all == null || all.isEmpty()) {
                return;
            }
            mpv5.db.objects.Address inv = contact.getInvoiceaddress(all);
            mpv5.db.objects.Address del = null;
            for (mpv5.db.objects.Address a : all) {
                if (a.__getInttype() == 1 || a.__getInttype() == 2) {
                    del = a;
                    break;
                }
            }
            if (del == null) {
                return;
            }
            String name = firstNonEmpty(del.__getCompany(),
                    join(" ", del.__getPrename(), del.__getCname()),
                    contact.__getCompany(), contact.__getCname());
            String street = nvl(del.__getStreet()).trim();
            String zip = nvl(del.__getZip()).trim();
            String city = nvl(del.__getCity()).trim();
            String country = toCountryCode(firstNonEmpty(del.__getCountry(), contact.__getCountry()));
            if (name.isEmpty() || city.isEmpty() || country.isEmpty()) {
                return;
            }
            if (inv != null
                    && street.equalsIgnoreCase(nvl(inv.__getStreet()).trim())
                    && zip.equalsIgnoreCase(nvl(inv.__getZip()).trim())
                    && city.equalsIgnoreCase(nvl(inv.__getCity()).trim())
                    && country.equalsIgnoreCase(toCountryCode(firstNonEmpty(inv.__getCountry(), contact.__getCountry())))) {
                return;
            }
            TradeParty shipTo = new TradeParty(name, street, zip, city, country);
            if (!isEmpty(del.__getDepartment())) {
                shipTo.setAdditionalAddress(del.__getDepartment().trim());
            }
            invoice.setDeliveryAddress(shipTo);
        } catch (Exception e) {
            Log.Debug(ZugpferdFile.class, e.getMessage());
        }
    }

    /**
     * Add customs commercial invoice metadata (Incoterms®, net/gross weight,
     * package count) as EN 16931 customs notes (BG-1 / BT-21 = {@code CUS})
     * when present on the invoice or contact.
     *
     * @param invoice
     * @param item
     * @param contact
     */
    public static void applyCustomsNotes(Invoice invoice, Item item, Contact contact) {
        if (invoice == null) {
            return;
        }
        String incoterms = findIncoterms(item, contact);
        if (!incoterms.isEmpty()) {
            invoice.addCustomsNote("Incoterms: " + incoterms);
        }
        String netW = valueProperty(item, KEY_NET_WEIGHT);
        String grossW = valueProperty(item, KEY_GROSS_WEIGHT);
        String pkgs = valueProperty(item, KEY_PACKAGES);
        List<String> parts = new ArrayList<String>();
        if (!isEmpty(netW)) {
            parts.add("Net weight: " + netW.trim() + (netW.toLowerCase(Locale.ROOT).contains("kg") ? "" : " kg"));
        }
        if (!isEmpty(grossW)) {
            parts.add("Gross weight: " + grossW.trim() + (grossW.toLowerCase(Locale.ROOT).contains("kg") ? "" : " kg"));
        }
        if (!isEmpty(pkgs)) {
            parts.add("Packages: " + pkgs.trim());
        }
        if (!parts.isEmpty()) {
            invoice.addCustomsNote(String.join(", ", parts));
        }
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
     * Create the invoice PDF using the configured invoice template. All fonts
     * are embedded, as required by PDF/A-3.
     */
    private static File renderPdf(Item item, Template[] appended) throws Exception {
        Template template = TemplateHandler.loadTemplate((long) item.templateGroupIds(), item.__getInttype());
        if (template == null) {
            throw new IllegalStateException(Messages.NO_TEMPLATE_LOADED + " (" + User.getCurrentUser() + ")");
        }
        if (appended != null && appended.length > 0) {
            //the invoice followed by the appended documents (e.g. the order confirmation)
            ExportProgress.step(Messages.EXPORT_STEP_FIELDS);
            Template[] all = new Template[appended.length + 1];
            all[0] = template;
            System.arraycopy(appended, 0, all, 1, appended.length);
            File merged = Export.createMergedFile(all, item, true);
            if (merged == null || !merged.isFile() || merged.length() == 0) {
                throw new IOException(Messages.ZUGFERD_PDF_FAILED + ": " + template);
            }
            return merged;
        }
        ExportProgress.step(Messages.EXPORT_STEP_FIELDS);
        Export export = (Export) Export.createFile(item.__getCnumber().replaceAll("[^A-Za-z0-9._-]", "_"), template, item);
        export.setEmbedAllFonts(true);
        //only the PDF the e-invoice is embedded in: the e-invoice is the issued document
        export.setDraft(true);
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
                return mpv5.globals.CompanyInfo.getProperty(key);
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

    /**
     * Find the buyer Peppol-ID (BT-49) of an invoice: value property on the
     * invoice or value property on the contact, or {@code null} if none is set.
     *
     * @param item
     * @param contact
     * @return
     */
    public static String findBuyerPeppolId(Item item, Contact contact) {
        String id = valueProperty(item, KEY_PEPPOL_ID);
        if (isEmpty(id)) {
            id = ContactProperty.get(contact, KEY_PEPPOL_ID);
        }
        return isEmpty(id) ? null : id.trim();
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
