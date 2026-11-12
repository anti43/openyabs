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
            Item inv = sale(invoiceId);
            Template t = template(templateId);
            File out = FileDirectoryHandler.getTempFile("receipt-" + inv.__getCnumber().replaceAll("[^A-Za-z0-9_-]", "_"), "pdf");
            Export ex = (Export) Export.createFile(t, inv);
            //fields of the receipt: the time of the sale and the seller
            java.util.Date when = inv.__getDateadded() == null ? new java.util.Date() : inv.__getDateadded();
            ex.put("pos.time", new java.text.SimpleDateFormat("HH:mm").format(when));
            String d = inv.__getDescription() == null ? "" : inv.__getDescription();
            ex.put("pos.seller", d.contains("·") ? d.substring(d.indexOf('·') + 1).trim() : d);
            tseFields(ex, invoiceId);
            //a cancellation: "STORNO of <number>" on the receipt
            ex.put("pos.storno", inv.__getInttype() == mpv5.globals.Constants.TYPE_CREDIT
                    ? mpv5.globals.Messages.POS_STORNO_OF + " " + referenced(inv) : "");
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
     * @param id a sale (invoice) or a cancellation (credit note)
     * @return the item
     * @throws Exception if there is none
     */
    private static String referenced(Item credit) {
        try {
            return ((Item) DatabaseObject.getObject(Context.getInvoice(), credit.__getRefOrderIDS())).__getCnumber();
        } catch (Exception e) {
            return "";
        }
    }

    static Item sale(int id) throws Exception {
        try {
            return (Item) DatabaseObject.getObject(Context.getInvoice(), id);
        } catch (mpv5.db.common.NodataFoundException e) {
            return (Item) DatabaseObject.getObject(Context.getCredit(), id);
        }
    }

    /**
     * The TSE fields of a receipt (pos.tse.*): transaction, times, counter,
     * signature, serials, and a note when the sale is not protected by a
     * certified TSE (no TSE, test TSE, TSE failed)
     */
    static void tseFields(Export ex, int invoiceId) {
        java.util.Map<String, String> r = mpv5.pos.tse.PosTransactions.of(invoiceId);
        String type = r.getOrDefault("tsetype", "none");
        boolean failed = "1".equals(r.get("tsefailed"));
        String note;
        if (r.isEmpty() || "none".equals(type) || type.isEmpty()) {
            note = mpv5.globals.Messages.TSE_NOTE_NONE.toString();
        } else if (failed) {
            note = mpv5.globals.Messages.TSE_NOTE_FAILED.toString();
        } else if ("Dummy".equals(type)) {
            note = mpv5.globals.Messages.TSE_NOTE_DUMMY.toString();
        } else {
            note = "";
        }
        ex.put("pos.tse.note", note);
        ex.put("pos.tse.failed", failed ? mpv5.globals.Messages.TSE_NOTE_FAILED.toString() : "");
        ex.put("pos.tse.transaction", r.getOrDefault("txnumber", "").equals("0") ? "" : r.getOrDefault("txnumber", ""));
        ex.put("pos.tse.counter", r.getOrDefault("sigcounter", "").equals("0") ? "" : r.getOrDefault("sigcounter", ""));
        ex.put("pos.tse.start", local(r.get("starttime")));
        ex.put("pos.tse.end", local(r.get("endtime")));
        ex.put("pos.tse.signature", r.getOrDefault("signature", ""));
        ex.put("pos.tse.serial", r.getOrDefault("tseserial", ""));
        ex.put("pos.tse.algorithm", r.getOrDefault("algorithm", ""));
        ex.put("pos.register.serial", r.getOrDefault("clientid", ""));
        ex.put("pos.tse.processdata", r.getOrDefault("processdata", ""));
        //content of the receipt QR code (DSFinV-K), for a QR code in the template
        if (!r.isEmpty() && !failed && !r.getOrDefault("signature", "").isEmpty()) {
            ex.put("pos.tse.qrdata", String.join(";", "V0", r.get("clientid"), r.get("processtype"), r.get("processdata"),
                    r.get("txnumber"), r.get("sigcounter"), r.get("starttime"), r.get("endtime"), r.get("algorithm"),
                    r.get("timeformat"), r.get("signature"), r.getOrDefault("publickey", "")));
        } else {
            ex.put("pos.tse.qrdata", "");
        }
    }

    /**
     * @param utc 2026-11-12T09:15:02.123Z
     * @return 12.11.2026 10:15:02 (local time), "" if none
     */
    static String local(String utc) {
        if (utc == null || utc.isEmpty()) {
            return "";
        }
        try {
            return java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")
                    .withZone(java.time.ZoneId.systemDefault()).format(java.time.Instant.parse(utc));
        } catch (Exception e) {
            return utc;
        }
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
