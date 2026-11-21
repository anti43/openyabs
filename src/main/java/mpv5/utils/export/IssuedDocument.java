/*
 * This file is part of YaBS.
 *
 *    YaBS is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    YaBS is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with YaBS.  If not, see <http://www.gnu.org/licenses/>.
 */
package mpv5.utils.export;

import java.io.File;
import java.util.List;
import mpv5.db.objects.Contact;
import mpv5.db.objects.Item;
import mpv5.db.objects.Template;
import mpv5.globals.Messages;
import mpv5.handler.TemplateHandler;
import mpv5.logging.Log;
import mpv5.utils.files.FileDirectoryHandler;

/**
 * Issues an invoice from the preview (print, save as, open in the PDF
 * viewer): the document the customer gets, as set for the contact (tab
 * E-Invoice): ZUGFeRD, XRechnung (XML next to the PDF) or a plain PDF. A
 * running plugin (GoBD) finalizes the invoice on it. The preview itself is
 * only a draft.
 */
public final class IssuedDocument {

    private IssuedDocument() {
    }

    /**
     * @param item
     * @return true if the document is issued from the preview (an invoice
     * type that can have an e-invoice)
     */
    public static boolean applies(Item item) {
        try {
            return item != null && item.__getIDS() > 0 && ZugpferdFile.supports(item);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Creates the issued document; not on the event dispatch thread
     *
     * @param item the invoice
     * @param pdf the PDF to write (ZUGFeRD or plain); an XRechnung goes next
     * to it (same name, .xml)
     * @return the PDF
     * @throws Exception (an {@link ExportFailedException} with the reason if
     * the e-invoice can not be created: then nothing is written)
     */
    public static File issue(Item item, File pdf) throws Exception {
        Contact contact = item.getContact();
        EInvoiceDelivery delivery = EInvoiceDelivery.effectiveFor(contact, item);
        Template template = TemplateHandler.loadTemplate((long) item.templateGroupIds(), item.__getInttype());
        if (template == null) {
            throw new ExportFailedException(Messages.NO_TEMPLATE_LOADED.toString(), null);
        }
        if (!delivery.isEInvoice()) {
            return plainPdf(item, template, pdf);
        }
        ExportProgress.step(Messages.EXPORT_STEP_EINVOICE_DATA);
        org.mustangproject.Invoice invoice;
        List<String> problems;
        try {
            invoice = ZugpferdFile.createInvoice(item, delivery.getFormat());
            ExportProgress.step(Messages.EXPORT_STEP_EINVOICE_CHECK);
            problems = ZugpferdFile.validate(invoice, SmallBusiness.isEnabled());
            String buyerVatId = ZugpferdFile.checkBuyerVatId(contact, contact.getInvoiceaddress());
            if (buyerVatId != null) {
                problems.add(buyerVatId);
            }
        } catch (Exception e) {
            Log.Debug(IssuedDocument.class, e);
            throw new ExportFailedException(Messages.EINVOICE_NOT_CREATED + ", " + Messages.ZUGFERD_MISSING_DATA
                    + "\n - " + e.getLocalizedMessage(), e);
        }
        if (!problems.isEmpty()) {
            //never an incomplete e-invoice, and no plain PDF instead
            throw new ExportFailedException(Messages.EINVOICE_NOT_CREATED + ", " + Messages.ZUGFERD_MISSING_DATA
                    + "\n - " + String.join("\n - ", problems), null);
        }
        ExportProgress.step(Messages.EXPORT_STEP_EINVOICE_STATUS);
        mpv5.handler.ItemService.ensureFinishedAndEndDate(item);
        if (delivery.getFormat() == ZugpferdFile.Format.ZUGFERD) {
            Exception failure = new ZugpferdFile(item, invoice, pdf, ZugpferdFile.Format.ZUGFERD).waitFor();
            if (failure != null) {
                throw failure;
            }
            return pdf;
        }
        //XRechnung: the XML first (it is the issued document), then the PDF to print or look at
        String name = pdf.getName();
        File xml = new File(pdf.getParentFile(), (name.contains(".") ? name.substring(0, name.lastIndexOf('.')) : name) + ".xml");
        Exception failure = new ZugpferdFile(item, invoice, xml, ZugpferdFile.Format.XRECHNUNG).waitFor();
        if (failure != null) {
            throw failure;
        }
        return plainPdf(item, template, pdf);
    }

    /**
     * The PDF of the template, written to the file
     */
    private static File plainPdf(Item item, Template template, File pdf) throws Exception {
        ExportProgress.step(Messages.EXPORT_STEP_FIELDS);
        Export export = (Export) Export.createFile(template, item);
        //the temporary file is not the issued one: the copy below is
        export.setDraft(true);
        Exception failure = export.waitFor();
        if (failure != null) {
            throw failure;
        }
        File tmp = export.getTargetFile();
        if (tmp == null || !tmp.isFile() || tmp.length() == 0) {
            throw new ExportFailedException(Messages.ZUGFERD_PDF_FAILED + ": " + template, null);
        }
        if (pdf.getParentFile() != null) {
            pdf.getParentFile().mkdirs();
        }
        FileDirectoryHandler.copyFile2(tmp, pdf, true);
        mpv5.handler.Activity.file(pdf, item);
        return pdf;
    }

    /**
     * @param item
     * @return the file in the folder of the invoice, with its usual name
     */
    public static File defaultFile(Item item) {
        File dir = mpv5.db.objects.User.getSaveDir(item);
        dir.mkdirs();
        String name = item.getFormatHandler().toUserString().replaceAll("[\\\\/:*?\"<>|]", "_");
        return new File(dir, name + ".pdf");
    }
}
