/*
 This file is part of YaBS.

 YaBS is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 YaBS is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with YaBS.  If not, see <http://www.gnu.org/licenses/>.
 */
package mpv5.pos;

import java.io.File;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.db.objects.Item;
import mpv5.db.objects.Template;
import mpv5.utils.export.Export;
import mpv5.utils.export.ODTFile2;
import mpv5.utils.export.PDFFile;
import mpv5.utils.files.FileDirectoryHandler;

/**
 * Receipts of the cash register: a PDF of the sale from a YaBS template,
 * printed on the printer of the template (or the default printer)
 */
public final class Receipts {

    private Receipts() {
    }

    /**
     * @param invoiceId the sale
     * @param templateId the receipt template
     * @return the receipt as PDF
     * @throws Exception if the template or LibreOffice is missing
     */
    public static File pdf(int invoiceId, int templateId) throws Exception {
        return mpv5.ui.dialogs.Popup.silently(() -> {
            Item inv = (Item) DatabaseObject.getObject(Context.getInvoice(), invoiceId);
            Template t = template(templateId);
            File out = FileDirectoryHandler.getTempFile("receipt-" + inv.__getCnumber().replaceAll("[^A-Za-z0-9_-]", "_"), "pdf");
            Export ex = (Export) Export.createFile(t, inv);
            //fields of the receipt: the time of the sale and the seller
            java.util.Date when = inv.__getDateadded() == null ? new java.util.Date() : inv.__getDateadded();
            ex.put("pos.time", new java.text.SimpleDateFormat("HH:mm").format(when));
            String d = inv.__getDescription() == null ? "" : inv.__getDescription();
            ex.put("pos.seller", d.contains("·") ? d.substring(d.indexOf('·') + 1).trim() : d);
            ex.setTargetFile(out);
            Exception e = ex.waitFor();
            if (e != null) {
                throw e;
            }
            if (!out.isFile() || out.length() == 0) {
                throw new IllegalStateException("No receipt (is LibreOffice installed?)");
            }
            return out;
        });
    }

    /**
     * Prints a receipt on the printer of its template, or the default printer
     *
     * @param pdf
     * @param templateId
     * @throws Exception
     */
    public static void print(File pdf, int templateId) throws Exception {
        String printer = null;
        try {
            printer = ((Template) DatabaseObject.getObject(Context.getTemplate(), templateId)).__getPrinter();
        } catch (Exception e) {
            //default printer
        }
        mpv5.utils.print.PrintJob2.print(pdf, printer == null || printer.isEmpty() || "printer_undefined".equals(printer) ? null : printer);
    }

    private static Template template(int id) throws Exception {
        Template t = (Template) DatabaseObject.getObject(Context.getTemplate(), id);
        File f = t.getFile();
        t.defineExFile(f.getName().toLowerCase().endsWith("pdf") ? new PDFFile(f.getPath()) : new ODTFile2(f.getPath()));
        return t;
    }
}
