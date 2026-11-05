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
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import mpv5.globals.Messages;
import mpv5.logging.Log;
import mpv5.mail.MailAttachments;
import mpv5.utils.jobs.Waitable;
import org.mustangproject.Invoice;

/**
 * Creates the mail attachments of an invoice according to the contact's
 * {@link EInvoiceDelivery}: the template PDF, a ZUGFeRD PDF and/or an XRechnung
 * XML. E-invoices are checked with the official validator; an invalid
 * e-invoice is not attached and the job fails, so the mail is not sent.
 */
public class InvoiceMail implements Waitable, MailAttachments {

    private final Waitable pdfJob;
    private final java.util.function.Supplier<File> pdfFile;
    private final Invoice invoice;
    private final EInvoiceDelivery delivery;
    private final List<File> attachments = new ArrayList<File>();

    /**
     * @param pdfExport renders the invoice PDF from the template
     * @param invoice the e-invoice data (see
     * {@link ZugpferdFile#createInvoice}), ignored for {@link EInvoiceDelivery#PDF}
     * @param delivery
     */
    public InvoiceMail(Export pdfExport, Invoice invoice, EInvoiceDelivery delivery) {
        this(pdfExport, pdfExport::getTargetFile, invoice, delivery);
        if (delivery == EInvoiceDelivery.ZUGFERD) {
            pdfExport.setEmbedAllFonts(true);//PDF/A-3
        }
    }

    /**
     * @param pdfJob renders the invoice PDF
     * @param pdfFile the PDF file written by the job
     * @param invoice
     * @param delivery
     */
    InvoiceMail(Waitable pdfJob, java.util.function.Supplier<File> pdfFile, Invoice invoice, EInvoiceDelivery delivery) {
        this.pdfJob = pdfJob;
        this.pdfFile = pdfFile;
        this.invoice = invoice;
        this.delivery = delivery;
    }

    @Override
    public List<File> getAttachments() {
        return Collections.unmodifiableList(attachments);
    }

    public EInvoiceDelivery getDelivery() {
        return delivery;
    }

    @Override
    public Exception waitFor() {
        attachments.clear();
        try {
            File pdf = pdfFile.get();
            //XRechnung only: no PDF needed, its name is used for the XML
            if (delivery != EInvoiceDelivery.XRECHNUNG) {
                Exception ex = pdfJob.waitFor();
                if (ex != null) {
                    return ex;
                }
                //the template engine logs errors instead of throwing them
                if (pdf == null || !pdf.isFile() || pdf.length() == 0) {
                    return new IOException(Messages.ZUGFERD_PDF_FAILED.toString());
                }
            }
            if (!delivery.isEInvoice()) {
                attachments.add(pdf);
                return null;
            }

            //same file name as the PDF (invoice number), in a folder of its own
            File dir = Files.createTempDirectory("einvoice").toFile();
            dir.deleteOnExit();
            String base = pdf.getName().replaceAll("(?i)\\.pdf$", "");
            File einvoice;
            if (delivery == EInvoiceDelivery.ZUGFERD) {
                einvoice = new File(dir, base + ".pdf");
                ZugpferdFile.writePdf(invoice, pdf, einvoice);
            } else {
                einvoice = new File(dir, base + ".xml");
                ZugpferdFile.writeXml(invoice, einvoice);
            }
            einvoice.deleteOnExit();

            Exception invalid = validate(einvoice);
            if (invalid != null) {
                return invalid;
            }
            if (delivery == EInvoiceDelivery.PDF_AND_XRECHNUNG) {
                attachments.add(pdf);
            }
            attachments.add(einvoice);
            return null;
        } catch (Exception e) {
            Log.Debug(e);
            return e;
        }
    }

    /**
     * @return an exception describing the problems if the e-invoice is
     * invalid, null if it is valid or the validator could not run
     */
    static Exception validate(File einvoice) {
        EInvoiceValidator.Result result;
        try {
            result = EInvoiceValidator.validate(einvoice);
        } catch (Throwable t) {
            //a broken validator must not stop all invoice mails
            Log.Debug(InvoiceMail.class, "E-Rechnung could not be validated: " + t);
            return null;
        }
        if (result.isValid()) {
            return null;
        }
        StringBuilder text = new StringBuilder(Messages.EINVOICE_NOT_SENT.toString()).append(": ")
                .append(Messages.ZUGFERD_INVALID);
        for (String problem : result.getProblems()) {
            text.append("\n - ").append(problem);
        }
        return new IllegalStateException(text.toString());
    }
}
