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

    @Override
    public String toString() {
        return label.toString();
    }

    /**
     * @param value stored value (enum name), may be null
     * @return the delivery, {@link #PDF} for unknown values
     */
    public static EInvoiceDelivery parse(Object value) {
        if (value != null) {
            for (EInvoiceDelivery d : values()) {
                if (d.name().equalsIgnoreCase(String.valueOf(value).trim())) {
                    return d;
                }
            }
        }
        return PDF;
    }

    /**
     * @param contact
     * @return the delivery configured for the contact, {@link #PDF} if none
     */
    public static EInvoiceDelivery forContact(DatabaseObject contact) {
        //independent of the context the contact was opened with (customer, supplier, ...)
        return parse(ContactProperty.get(contact, KEY));
    }

    /**
     * Store the delivery for the contact (which must exist)
     *
     * @param contact
     * @param delivery
     */
    public static void save(DatabaseObject contact, EInvoiceDelivery delivery) {
        ContactProperty.set(contact, KEY, delivery == null || delivery == PDF ? null : delivery.name());
    }
}
