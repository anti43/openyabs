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

import mpv5.db.common.DatabaseObject;
import mpv5.db.objects.ValueProperty;
import mpv5.globals.Messages;
import mpv5.logging.Log;

/**
 * How invoices are sent to a contact by email. Stored per contact as value
 * property {@value #KEY}; contacts without setting get a normal PDF.
 */
public enum EInvoiceDelivery {

    /**
     * uses the default from the control panel
     */
    DEFAULT(Messages.EINVOICE_DELIVERY_DEFAULT, null),
    /**
     * the invoice PDF from the template (no e-invoice)
     */
    PDF(Messages.EINVOICE_DELIVERY_PDF, null),
    /**
     * the invoice PDF with embedded e-invoice XML - readable by people and
     * machines
     */
    ZUGFERD(Messages.EINVOICE_DELIVERY_ZUGFERD, ZugpferdFile.Format.ZUGFERD),
    /**
     * only the XRechnung XML
     */
    XRECHNUNG(Messages.EINVOICE_DELIVERY_XRECHNUNG, ZugpferdFile.Format.XRECHNUNG),
    /**
     * two attachments: the invoice PDF and the XRechnung XML
     */
    PDF_AND_XRECHNUNG(Messages.EINVOICE_DELIVERY_PDF_XRECHNUNG, ZugpferdFile.Format.XRECHNUNG);
    /**
     * Value property (on the contact) holding the delivery
     */
    public static final String KEY = "einvoice.delivery";
    /**
     * Setting key for making ZUGFeRD the default for PDF export and email
     */
    public static final String KEY_DEFAULT_ZUGFERD = "companyinfo.zugpferd_default";
    private final Messages label;
    private final ZugpferdFile.Format format;

    EInvoiceDelivery(Messages label, ZugpferdFile.Format format) {
        this.label = label;
        this.format = format;
    }

    /**
     * @return the e-invoice format to create, null for {@link #PDF}
     */
    public ZugpferdFile.Format getFormat() {
        return format;
    }

    /**
     * @return true if an e-invoice is created
     */
    public boolean isEInvoice() {
        return format != null;
    }

    /**
     * @return true if ZUGFeRD is configured as the global default for PDF export and email
     */
    public static boolean isZugpferdDefault() {
        try {
            String v = mpv5.db.objects.User.getCurrentUser().getProperty(KEY_DEFAULT_ZUGFERD);
            if (v == null || v.trim().isEmpty()) {
                v = mpv5.globals.GlobalSettings.getProperty(KEY_DEFAULT_ZUGFERD);
            }
            return Boolean.parseBoolean(v);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * @param enabled true to make ZUGFeRD default for PDF export and email
     */
    public static void setZugpferdDefault(boolean enabled) {
        try {
            mpv5.db.objects.User.getCurrentUser().setProperty(KEY_DEFAULT_ZUGFERD, String.valueOf(enabled));
            mpv5.globals.GlobalSettings.setProperty(KEY_DEFAULT_ZUGFERD, String.valueOf(enabled));
        } catch (Exception e) {
            Log.Debug(EInvoiceDelivery.class, e);
        }
    }

    /**
     * @return the effective delivery resolving DEFAULT against the global control panel setting
     */
    public EInvoiceDelivery resolve() {
        if (this == DEFAULT) {
            return isZugpferdDefault() ? ZUGFERD : PDF;
        }
        return this;
    }

    @Override
    public String toString() {
        return label.toString();
    }

    /**
     * @param value stored value (enum name), may be null
     * @return the delivery, {@link #DEFAULT} for unknown values or null
     */
    public static EInvoiceDelivery parse(Object value) {
        if (value != null) {
            for (EInvoiceDelivery d : values()) {
                if (d.name().equalsIgnoreCase(String.valueOf(value).trim())) {
                    return d;
                }
            }
        }
        return DEFAULT;
    }

    /**
     * @param contact
     * @return the delivery configured for the contact, {@link #DEFAULT} if none
     */
    public static EInvoiceDelivery forContact(DatabaseObject contact) {
        if (contact == null) {
            return DEFAULT;
        }
        //independent of the context the contact was opened with (customer, supplier, ...)
        return parse(ContactProperty.get(contact, KEY));
    }

    /**
     * @param contact
     * @return the effective delivery for the contact (resolving DEFAULT to ZUGFERD or PDF)
     */
    public static EInvoiceDelivery effectiveFor(DatabaseObject contact) {
        return forContact(contact).resolve();
    }

    /**
     * @param contact
     * @param item the invoice being exported or sent (may be null)
     * @return the effective delivery for the contact and item; returns
     * {@link #PDF} if a running plugin skips e-invoice delivery for this item
     */
    public static EInvoiceDelivery effectiveFor(DatabaseObject contact, mpv5.db.objects.Item item) {
        if (item != null) {
            mpv5.plugin.ExtensionManager em = mpv5.plugin.ExtensionManager.instanceOrNull();
            if (em != null && em.shouldSkipEInvoice(item)) {
                return PDF;
            }
        }
        return effectiveFor(contact);
    }

    /**
     * Store the delivery for the contact (which must exist)
     *
     * @param contact
     * @param delivery
     */
    public static void save(DatabaseObject contact, EInvoiceDelivery delivery) {
        ContactProperty.set(contact, KEY, delivery == null || delivery == DEFAULT ? null : delivery.name());
    }
}
