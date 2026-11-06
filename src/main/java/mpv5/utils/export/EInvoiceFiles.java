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
import java.util.ArrayList;
import java.util.List;
import mpv5.db.objects.Contact;
import mpv5.db.objects.Item;
import mpv5.globals.Messages;
import mpv5.logging.Log;
import org.mustangproject.Invoice;

/**
 * Writes the e-invoice of an invoice as file, in the format configured at the
 * contact (tab "E-Invoice"), into the folder of the PDF export: ZUGFeRD = the
 * invoice PDF with embedded XML, XRechnung and PDF + XRechnung = the XML file.
 * Incomplete e-invoices are not written; every result is reported as one line.
 */
public final class EInvoiceFiles {

    private EInvoiceFiles() {
    }

    /**
     * @param contact
     * @return true if the e-invoice of the contact replaces the normal PDF
     * (ZUGFeRD: the PDF with embedded XML has the same file name)
     */
    public static boolean replacesPdf(Contact contact) {
        return EInvoiceDelivery.forContact(contact) == EInvoiceDelivery.ZUGFERD;
    }

    /**
     * Create the e-invoice configured for the contact of the (saved) invoice
     *
     * @param item
     * @param contact
     * @param dir the target folder
     * @return the report lines, empty if the contact gets a normal PDF
     */
    public static List<String> write(Item item, Contact contact, File dir) {
        EInvoiceDelivery delivery = EInvoiceDelivery.forContact(contact);
        if (!delivery.isEInvoice()) {
            return new ArrayList<String>();
        }
        if (!ZugpferdFile.supports(item)) {
            List<String> report = new ArrayList<String>();
            report.add(item.__getCnumber() + ": " + Messages.ZUGFERD_NOT_AN_INVOICE);
            return report;
        }
        Invoice invoice;
        List<String> problems;
        try {
            invoice = ZugpferdFile.createInvoice(item, delivery.getFormat());
            problems = ZugpferdFile.validate(invoice);
            String buyerVatId = ZugpferdFile.checkBuyerVatId(contact, contact.getInvoiceaddress());
            if (buyerVatId != null) {
                problems.add(buyerVatId);
            }
        } catch (Exception e) {
            Log.Debug(EInvoiceFiles.class, e);
            invoice = null;
            problems = new ArrayList<String>();
            problems.add(String.valueOf(e.getLocalizedMessage()));
        }
        File target = new File(dir, fileName(item) + "." + delivery.getFormat().getExtension());
        return write(item, invoice, delivery, problems, target);
    }

    /**
     * @param item the YaBS invoice (number, file name, PDF template)
     * @param invoice the e-invoice data, null if it could not be built
     * @param delivery the format of the contact
     * @param problems missing data found before ({@link ZugpferdFile#validate})
     * @param target the file to write
     * @return the report lines
     */
    static List<String> write(Item item, Invoice invoice, EInvoiceDelivery delivery, List<String> problems, File target) {
        List<String> report = new ArrayList<String>();
        String label = item.__getCnumber() + " (" + delivery + ")";
        if (invoice == null || !problems.isEmpty()) {
            StringBuilder text = new StringBuilder(label).append(": ").append(Messages.EINVOICE_NOT_CREATED)
                    .append(", ").append(Messages.ZUGFERD_MISSING_DATA);
            for (String p : problems) {
                text.append("\n - ").append(p);
            }
            report.add(text.toString());
            return report;
        }
        target.getAbsoluteFile().getParentFile().mkdirs();
        ZugpferdFile file = new ZugpferdFile(item, invoice, target, delivery.getFormat());
        Exception error = file.waitFor();
        if (error != null) {
            report.add(label + ": " + Messages.EINVOICE_NOT_CREATED + ": " + error.getLocalizedMessage());
            return report;
        }
        EInvoiceValidator.Result result = file.getValidation();
        if (result != null && !result.isValid()) {
            StringBuilder text = new StringBuilder(label).append(": ").append(Messages.EINVOICE_INVALID)
                    .append(" ").append(target);
            for (String e : result.getErrors()) {
                text.append("\n - ").append(e);
            }
            report.add(text.toString());
        } else {
            report.add(label + ": " + Messages.EINVOICE_CREATED + " " + target);
        }
        return report;
    }

    /**
     * @return the file name of the PDF export, without extension
     */
    static String fileName(Item item) {
        return item.getFormatHandler().toUserString().replaceAll("[\\\\/:*?\"<>|]", "_");
    }
}
