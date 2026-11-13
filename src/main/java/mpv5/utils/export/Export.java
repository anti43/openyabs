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

import com.lowagie.text.Document;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfImportedPage;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfWriter;

import java.awt.Component;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.db.common.Formattable;
import mpv5.db.common.NodataFoundException;
import mpv5.db.common.QueryCriteria;
import mpv5.db.objects.Contact;
import mpv5.db.objects.MailMessage;
import mpv5.db.objects.Template;
import mpv5.db.objects.User;
import mpv5.globals.GlobalSettings;
import mpv5.globals.Messages;
import mpv5.handler.FormFieldsHandler;
import mpv5.handler.VariablesHandler;
import mpv5.logging.Log;
import mpv5.mail.SimpleMail;
import mpv5.ui.dialogs.Notificator;
import mpv5.ui.dialogs.Popup;
import mpv5.utils.files.FileDirectoryHandler;
import mpv5.utils.jobs.Job;
import mpv5.utils.jobs.Waitable;
import mpv5.utils.jobs.Waiter;
import mpv5.utils.print.PrintJob2;

/**
 * The Export class is the primary handler in Yabs to do exporting tasks, such
 * as printing, generating files etc.
 */
public final class Export extends HashMap<String, Object> implements Waitable {

    private static final long serialVersionUID = 1L;
    private static final String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

    /**
     * Mail a template
     *
     * @param preloadedTemplate
     * @param dataOwner
     * @param to
     * @param log
     */
    /**
     * Mails a document: checks the sender and the recipient first (asks for
     * an address if the contact has none), then the mail template (one is
     * used directly, several to choose, none: send with the document name as
     * subject, if wanted). Nothing is sent without a recipient and a sender.
     *
     * @param preloadedTemplate the document template
     * @param dataOwner the document
     * @param to the contact
     * @param log null: interactive, else messages go there (no questions)
     */
    public static void mail(Template preloadedTemplate, DatabaseObject dataOwner, Contact to, List<String> log) {
        boolean interactive = log == null;
        String sender = mpv5.db.objects.User.getCurrentUser().__getMail();
        if (sender == null || !Pattern.matches(EMAIL_PATTERN, sender.trim())) {
            report(log, dataOwner, Messages.MAIL_NO_SENDER.toString());
            return;
        }
        if (to == null) {
            report(log, dataOwner, Messages.NO_MAIL_DEFINED.toString());
            return;
        }
        if (to.__getMailaddress() == null || !Pattern.matches(EMAIL_PATTERN, to.__getMailaddress().trim())) {
            if (!interactive) {
                report(log, dataOwner, Messages.NO_MAIL_DEFINED.toString());
                return;
            }
            String address = Popup.Enter_Value(String.format(Messages.MAIL_NO_RECIPIENT.toString(), to.__getCname()));
            if (address == null || address.trim().isEmpty()) {
                mpv5.YabsViewProxy.instance().addMessage(Messages.MAIL_CANCELED.toString());
                return;
            }
            address = address.trim();
            if (!Pattern.matches(EMAIL_PATTERN, address)) {
                Popup.notice(String.format(Messages.MAIL_INVALID_ADDRESS.toString(), address));
                return;
            }
            to.setMailaddress(address);//used for this mail
            if (Popup.Y_N_dialog(String.format(Messages.MAIL_SAVE_ADDRESS.toString(), address, to.__getCname()))) {
                to.save(true);
            }
        }
        MailMessage m;
        List<DatabaseObject> templates;
        try {
            templates = DatabaseObject.getObjects(Context.getMessage(),
                    new QueryCriteria("usersids", mpv5.db.objects.User.getCurrentUser().__getIDS()));
        } catch (Exception ex) {
            templates = new java.util.ArrayList<DatabaseObject>();
        }
        if (templates.isEmpty()) {
            if (!interactive) {
                report(log, dataOwner, Messages.MAIL_NO_TEMPLATE_BATCH.toString());
                return;
            }
            if (!Popup.Y_N_dialog(Messages.MAIL_NO_TEMPLATE_ASK.toString())) {
                mpv5.YabsViewProxy.instance().addMessage(Messages.MAIL_CANCELED.toString());
                return;
            }
            //not saved: subject = the document, no text
            m = new MailMessage();
            m.setCname(dataOwner.__getCname());
            m.setDescription("");
        } else if (templates.size() == 1 || !interactive) {
            m = (MailMessage) templates.get(0);
        } else {
            m = (MailMessage) Popup.SelectValue(new java.util.ArrayList<DatabaseObject>(templates), Messages.SELECT_A_TEMPLATE);
            if (m == null) {
                mpv5.YabsViewProxy.instance().addMessage(Messages.MAIL_CANCELED.toString());
                return;
            }
        }
        mail(preloadedTemplate, dataOwner, to, log, m);
    }

    private static void report(List<String> log, DatabaseObject dataOwner, String message) {
        if (log == null) {
            Popup.notice(message);
        } else {
            log.add(dataOwner.getCname() + "->" + message + "\n");
        }
    }

    public static void mail(Template preloadedTemplate, DatabaseObject dataOwner, Contact to, List<String> log, MailMessage m) {

        try {
            mpv5.YabsViewProxy.instance().setWaiting(true);

            if (m != null) {
                Map<String, Object> hm1 = dataOwner.getFormFields();//new FormFieldsHandler(dataOwner).getFormattedFormFields(null);
                File f2 = FileDirectoryHandler.getTempFile(((Formattable) dataOwner).getFormatHandler().toUserString(), "pdf");
                Export ex = new Export(preloadedTemplate);
                ex.putAll(hm1);

                ex.setTemplate(preloadedTemplate.getExFile());
                ex.setTargetFile(f2);

                try {
                    Contact cont = to;
                    if (mpv5.db.objects.User.getCurrentUser().__getMail() != null
                            && cont.__getMailaddress() != null
                            && Pattern.matches(EMAIL_PATTERN, mpv5.db.objects.User.getCurrentUser().__getMail())
                            && Pattern.matches(EMAIL_PATTERN, cont.__getMailaddress())) {
                        SimpleMail pr = new SimpleMail(log);
                        pr.setMailConfiguration(mpv5.db.objects.User.getCurrentUser().getMailConfiguration());
                        pr.setRecipientsAddress(cont.__getMailaddress());
                        pr.setSenderName(User.getCurrentUser().__getFullname());

                        //copies of every mail, e.g. Bcc to an archive mailbox (user settings > mail)
                        pr.setBccAddress(validAddresses(
                                GlobalSettings.hasProperty(KEY_MAIL_BCC) ? GlobalSettings.getProperty(KEY_MAIL_BCC) : null));
                        pr.setCCAddress(validAddresses(
                                GlobalSettings.hasProperty(KEY_MAIL_CC) ? GlobalSettings.getProperty(KEY_MAIL_CC) : null));

                        try {
                            //invoices: PDF, e-invoice or both, as configured for the contact
                            Waitable attachments = invoiceAttachments(ex, dataOwner, cont, log);
                            if (attachments == null) {
                                return;//e-invoice data incomplete, already reported
                            }
                            pr.setSubject(VariablesHandler.parse(m.__getCname(), dataOwner, hm1));
                            pr.setText(VariablesHandler.parse(m.__getDescription(), dataOwner, hm1));
                            if(log==null) {
                                //single mail: shown before it is sent (recipient, subject,
                                //text, the attached files); nothing goes out without "Send"
                                final SimpleMail mail = pr;
                                new Job(attachments, new Waiter() {
                                    @Override
                                    public void set(Object object, Exception exception) throws Exception {
                                        if (exception != null) {
                                            mail.set(object, exception);
                                        } else if (mpv5.ui.dialogs.MailPreviewDialog.confirm(mail, attachmentFiles(object))) {
                                            mail.set(object, null);
                                        } else {
                                            mpv5.YabsViewProxy.instance().addMessage(Messages.MAIL_PREVIEW_CANCELED.toString());
                                        }
                                    }
                                }).execute();
                            }
                            else{
                                new Job(attachments, (Waiter) pr).executeSync();
                            }
                        } catch (Exception e) {
                            Popup.error(e);
                        }

                    } else {
                        if (log == null)
                            Popup.notice(Messages.NO_MAIL_DEFINED);
                        else
                            log.add(dataOwner.getCname() + "->" + Messages.NO_MAIL_DEFINED + "\n");
                    }
                } catch (UnsupportedOperationException unsupportedOperationException) {
                    Log.Debug(unsupportedOperationException);

                    if (log == null)
                        Popup.notice(Messages.NOT_POSSIBLE + "\n" + Messages.NO_MAIL_CONFIG);
                    else
                        log.add(dataOwner.getCname() + "->" + Messages.NOT_POSSIBLE + "\n" + Messages.NO_MAIL_CONFIG + "\n");
                }
            }
        } catch (Exception e) {
            Log.Debug(e);
            if (log != null)
                log.add(dataOwner.getCname() + "->" + Messages.ERROR_OCCURED + ":" + e.getMessage() + "\n");
            else
                Popup.error(e);
        } finally {
            mpv5.YabsViewProxy.instance().setWaiting(false);
        }
    }

    /**
     * @return the files a finished attachment job produced
     */
    static List<File> attachmentFiles(Object object) {
        List<File> files = new java.util.ArrayList<File>();
        if (object instanceof mpv5.mail.MailAttachments) {
            files.addAll(((mpv5.mail.MailAttachments) object).getAttachments());
        } else if (object instanceof Export) {
            files.add(((Export) object).getTargetFile());
        } else if (object instanceof File) {
            files.add((File) object);
        }
        return files;
    }

    /**
     * Global setting: BCC recipient(s) of all mails (";" separated)
     */
    public static final String KEY_MAIL_BCC = "org.openyabs.exportproperty.mailbcc";
    /**
     * Global setting: CC recipient(s) of all mails (";" separated)
     */
    public static final String KEY_MAIL_CC = "org.openyabs.exportproperty.mailcc";
    /**
     * @param lists ";" separated address lists, may be null
     * @return the valid addresses as ";" separated list (invalid ones are
     * logged and left out)
     */
    static String validAddresses(String... lists) {
        StringBuilder b = new StringBuilder();
        for (String list : lists) {
            if (list == null) {
                continue;
            }
            for (String a : list.split(";")) {
                String t = a.trim();
                if (t.isEmpty()) {
                    continue;
                }
                if (mpv5.utils.text.TypeConversion.stringToMail(t) == null) {
                    Log.Debug(Export.class, "Invalid mail address ignored: " + t);
                } else if (!(";" + b + ";").contains(";" + t + ";")) {
                    b.append(b.length() > 0 ? ";" : "").append(t);
                }
            }
        }
        return b.toString();
    }

    /**
     * The attachments of a mail: the template PDF, or for invoices the format
     * configured for the contact ({@link EInvoiceDelivery}).
     *
     * @param pdf renders the template PDF
     * @param dataOwner
     * @param contact the recipient
     * @param log the batch log, null for a single mail
     * @return the job creating the attachments, null if the e-invoice can not
     * be created (reported to the user / batch log)
     */
    static Waitable invoiceAttachments(Export pdf, DatabaseObject dataOwner, Contact contact, List<String> log) {
        if (!(dataOwner instanceof mpv5.db.objects.Item) || !ZugpferdFile.supports((mpv5.db.objects.Item) dataOwner)) {
            return pdf;
        }
        EInvoiceDelivery delivery = EInvoiceDelivery.forContact(contact);
        if (!delivery.isEInvoice()) {
            return pdf;
        }
        mpv5.db.objects.Item item = (mpv5.db.objects.Item) dataOwner;
        List<String> problems;
        org.mustangproject.Invoice invoice;
        try {
            invoice = ZugpferdFile.createInvoice(item, delivery.getFormat());
            problems = ZugpferdFile.validate(invoice);
            String buyerVatId = ZugpferdFile.checkBuyerVatId(contact, contact.getInvoiceaddress());
            if (buyerVatId != null) {
                problems.add(buyerVatId);
            }
        } catch (Exception e) {
            Log.Debug(e);
            problems = new java.util.ArrayList<String>();
            problems.add(String.valueOf(e.getLocalizedMessage()));
            invoice = null;
        }
        if (!problems.isEmpty()) {
            //never send an incomplete e-invoice to a customer
            StringBuilder text = new StringBuilder(Messages.EINVOICE_NOT_SENT.toString())
                    .append(" (").append(item.__getCnumber()).append(", ").append(delivery).append("): ")
                    .append(Messages.ZUGFERD_MISSING_DATA);
            for (String problem : problems) {
                text.append("\n - ").append(problem);
            }
            if (log == null) {
                Popup.warn(text.toString());
            } else {
                log.add(text.append("\n").toString());
            }
            return null;
        }
        return new InvoiceMail(pdf, invoice, delivery);
    }

    /**
     * Prints a template
     *
     * @param preloadedTemplate
     * @param dataOwner
     */
    public static void print(final Template preloadedTemplate, final DatabaseObject dataOwner) {
        print(preloadedTemplate, dataOwner, documentName(preloadedTemplate, dataOwner));
    }

    /**
     * Prints a template: the PDF is created with the given name, which is also
     * the name of the print job (a PDF printer suggests it as file name)
     *
     * @param preloadedTemplate
     * @param dataOwner
     * @param name the file name without extension, e.g.
     * "Kontaktdatenblatt-000001-Muster GmbH"
     */
    public static void print(final Template preloadedTemplate, final DatabaseObject dataOwner, final String name) {
        Runnable runnable = new Runnable() {

            @Override
            public void run() {
                Map<String, Object> hm1 = dataOwner.getFormFields();//new FormFieldsHandler(dataOwner).getFormattedFormFields(null);
                File f2 = FileDirectoryHandler.getTempFile(name, "pdf");
                final Export ex = new Export(preloadedTemplate);
                ex.putAll(hm1);
                ex.putDataOwner(dataOwner);
                ex.setTemplate(preloadedTemplate.getExFile());
                ex.setTargetFile(f2);
//        new Job(ex, (Waiter) new PrintJob()).execute();

                Exception failed = ex.waitFor();
                if (failed != null || !ex.getTargetFile().isFile() || ex.getTargetFile().length() == 0) {
                    //no broken file to the printer
                    Log.Debug(Export.class, "Not printed: " + failed);
                    Popup.error(failed != null ? failed : new Exception(String.format(Messages.EXPORT_FAILED.toString(),
                            preloadedTemplate.__getCname(), ex.getTargetFile())));
                    return;
                }
                try {
                    PrintJob2.print(ex.getTargetFile(), preloadedTemplate.__getPrinter());
                } catch (Exception ex1) {
                    Log.Debug(ex1);
                }
            }
        };
        new Thread(runnable).start();
    }

    /**
     * The file name of a document created from a template: kind, number and
     * name, e.g. "Kontaktdatenblatt-000001-Muster GmbH" or
     * "Rechnung-RE-2026-0001"; characters not allowed in file names are
     * replaced
     *
     * @param template
     * @param dataOwner
     * @return the name without extension
     */
    public static String documentName(Template template, DatabaseObject dataOwner) {
        int type = -1;
        try {
            type = Integer.parseInt(String.valueOf(template.__getMimetype()).trim());
        } catch (Exception e) {
            //unknown type: no kind in the name
        }
        String kind = type == mpv5.globals.Constants.TYPE_CONTACT ? Messages.CONTACT_SHEET.toString()
                : type >= 0 ? mpv5.handler.TemplateHandler.getName(type) : "";
        //the plain number: the formatters give e.g. "name (number)" or "name-number (total)"
        String number = "";
        if (dataOwner instanceof Contact) {
            number = ((Contact) dataOwner).__getCNumber();
        } else if (dataOwner instanceof mpv5.db.objects.Item) {
            number = ((mpv5.db.objects.Item) dataOwner).__getCnumber();
        } else if (dataOwner instanceof mpv5.db.objects.Product) {
            number = ((mpv5.db.objects.Product) dataOwner).__getCnumber();
        }
        number = number == null ? "" : number;
        String name;
        if (dataOwner instanceof Contact) {
            Contact c = (Contact) dataOwner;
            String company = c.__getCompany() == null ? "" : c.__getCompany().trim();
            String person = ((c.__getPrename() == null ? "" : c.__getPrename().trim()) + " "
                    + (c.__getCname() == null ? "" : c.__getCname().trim())).trim();
            name = company.isEmpty() ? person : company;
        } else {
            name = dataOwner == null || dataOwner.__getCname() == null ? "" : dataOwner.__getCname().trim();
        }
        return fileName(kind, number, name.equals(number) ? "" : name);
    }

    /**
     * @param parts
     * @return the non empty parts joined with "-", without characters not
     * allowed in file names; "document" if all are empty
     */
    static String fileName(String... parts) {
        StringBuilder b = new StringBuilder();
        for (String p : parts) {
            //an unknown template type ("<undefined> [99]") is no part of the name
            String t = p == null || p.trim().startsWith("<undefined>") ? "" : p.replaceAll("[\\\\/:*?\"<>|\\p{Cntrl}]", "_").replaceAll("\\s+", " ").trim();
            if (!t.isEmpty()) {
                b.append(b.length() > 0 ? "-" : "").append(t);
            }
        }
        String n = b.length() > 0 ? b.toString() : "document";
        return n.length() > 120 ? n.substring(0, 120).trim() : n;
    }

    public static File print2(Template preloadedTemplate, DatabaseObject dataOwner, HashMap<String, Object> hm) {

        Map<String, Object> hm1 = dataOwner.getFormFields();//new FormFieldsHandler(dataOwner).getFormattedFormFields(null);
        File f2 = FileDirectoryHandler.getTempFile("pdf");
        final Export ex = new Export(preloadedTemplate);
        ex.putAll(hm1);
        ex.putAll(hm);
        ex.setTemplate(preloadedTemplate.getExFile());
        ex.setTargetFile(f2);
        ex.waitFor();
        return ex.getTargetFile();
    }

    /**
     * Prints collected pages at one time
     *
     * @param prints
     * @param Printer
     */
    public static void print3(final List<File> prints, final String Printer) {
        Runnable runnable = new Runnable() {

            @Override
            public void run() {
                try {
                    PrintJob2.print(mergeFiles(prints), Printer);
                } catch (Exception fileNotFoundException) {
                    Popup.error(fileNotFoundException);
                    Log.Debug(fileNotFoundException);
                }
            }
        };
        new Thread(runnable).start();
    }

    /**
     * Print multiple templates
     *
     * @param preloadedTemplate
     * @param dataOwner
     */
    public static void print(final Template[] preloadedTemplate, final DatabaseObject dataOwner) {
        Runnable runnable = new Runnable() {

            @Override
            public void run() {
                List<File> files = new ArrayList<File>();
                if (preloadedTemplate != null && preloadedTemplate.length > 0) {
                    for (int i = 0; i < preloadedTemplate.length; i++) {
                        Template template = preloadedTemplate[i];
                        Map<String, Object> hm1 = dataOwner.getFormFields();//new FormFieldsHandler(dataOwner).getFormattedFormFields(null);
                        File f2 = FileDirectoryHandler.getTempFile("pdf");
                        Export ex = new Export(template);
                        ex.putAll(hm1);
                        ex.putDataOwner(dataOwner);
                        try {
                            ex.processData(f2);
                            files.add(f2);
                        } catch (ExportFailedException failed) {
                            //not printed: say why, instead of printing an empty file
                            Popup.notice(failed.getMessage());
                        } catch (NodataFoundException ex1) {
                            Log.Debug(ex1);
                        } catch (FileNotFoundException ex1) {
                            Log.Debug(ex1);
                        }
                    }

                    try {
                        PrintJob2.print(mergeFiles(files), preloadedTemplate[0].__getPrinter());
                    } catch (Exception fileNotFoundException) {
                        Popup.error(fileNotFoundException);
                        Log.Debug(fileNotFoundException);
                    }
                }
            }
        };
        new Thread(runnable).start();
    }

    private static File mergeFiles(List<File> p) {

        Document document = new Document();
        try {
            List<InputStream> pdfs = new ArrayList<InputStream>();
            for (int i = 0; i < p.size(); i++) {
                File inputStream = p.get(i);
                pdfs.add(new FileInputStream(inputStream));
            }
            List<PdfReader> readers = new ArrayList<PdfReader>();
            int totalPages = 0;
            Iterator<InputStream> iteratorPDFs = pdfs.iterator();

            while (iteratorPDFs.hasNext()) {
                InputStream pdf = iteratorPDFs.next();
                PdfReader pdfReader = new PdfReader(pdf);
                readers.add(pdfReader);
                totalPages += pdfReader.getNumberOfPages();
            }

            File f = FileDirectoryHandler.getTempFile("pdf");
            FileOutputStream outputstream = new FileOutputStream(f);
            PdfWriter writer = PdfWriter.getInstance(document, outputstream);

            document.open();
            BaseFont bf = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
            PdfContentByte cb = writer.getDirectContent();

            PdfImportedPage page;
            int currentPageNumber = 0;
            int pageOfCurrentReaderPDF = 0;
            Iterator<PdfReader> iteratorPDFReader = readers.iterator();

            // Loop through the PDF files and add to the output.
            while (iteratorPDFReader.hasNext()) {
                PdfReader pdfReader = iteratorPDFReader.next();

                // Create a new page in the target for each source page.
                while (pageOfCurrentReaderPDF < pdfReader.getNumberOfPages()) {
                    document.newPage();
                    pageOfCurrentReaderPDF++;
                    currentPageNumber++;
                    page = writer.getImportedPage(pdfReader, pageOfCurrentReaderPDF);
                    cb.addTemplate(page, 0, 0);

                }
                pageOfCurrentReaderPDF = 0;
            }
            outputstream.flush();
            document.close();
            outputstream.close();

            return f;
        } catch (Exception e) {
            Log.Debug(e);
        } finally {
            if (document.isOpen()) {
                document.close();
            }
        }

        return null;
    }

    /**
     * Create a {@link Waitable} which is able to create a file PDF
     *
     * @param preloadedTemplate
     * @param dataOwner
     * @return
     */
    public static Waitable createFile(Template preloadedTemplate, DatabaseObject dataOwner) {
        return createFile(null, preloadedTemplate, dataOwner);
    }

    /**
     * Create a {@link Waitable} which is able to create a file ODT
     *
     * @param preloadedTemplate
     * @param dataOwner
     * @return
     */
    public static Waitable sourceFile(Template preloadedTemplate, DatabaseObject dataOwner) {
        return sourceFile(null, preloadedTemplate, dataOwner);
    }

    /**
     * Create a {@link Waitable} which is able to create a file PDF
     *
     * @param aname
     * @param preloadedTemplate
     * @param dataOwner
     * @return
     */
    public static Waitable createFile(String aname, Template preloadedTemplate, DatabaseObject dataOwner) {
        Map<String, Object> hm1 = null;
        if (dataOwner != null) {
            hm1 = dataOwner.getFormFields();
        }
        File f2;
        if (aname == null) {
            f2 = FileDirectoryHandler.getTempFile("pdf");
        } else {
            f2 = FileDirectoryHandler.getTempFile(aname, "pdf");
        }
        Export ex = new Export(preloadedTemplate);
        if (dataOwner != null) {
            ex.putAll(hm1);
        }
        ex.putDataOwner(dataOwner);
        ex.setTemplate(preloadedTemplate.getExFile());
        ex.setTargetFile(f2);
        Log.Debug(Export.class, "createFile target file: " + f2);
        return ex;
    }

    /**
     * Create a {@link Waitable} which is able to create a file ODT
     *
     * @param aname
     * @param preloadedTemplate
     * @param dataOwner
     * @return
     */
    public static Waitable sourceFile(String aname, Template preloadedTemplate, DatabaseObject dataOwner) {
        Map<String, Object> hm1 = dataOwner == null
                ? new FormFieldsHandler(null).getFormattedFormFields(null)
                : dataOwner.getFormFields();
        File f2;
        if (aname == null) {
            f2 = FileDirectoryHandler.getTempFile("odt");
        } else {
            f2 = FileDirectoryHandler.getTempFile(aname, "odt");
        }

        Export ex = new Export(preloadedTemplate);
        ex.putAll(hm1);
        ex.setTemplate(preloadedTemplate.getExFile());
        ex.setTargetFile(f2);
        return ex;
    }

    /**
     * Create a {@link Waitable} which is able to create a file PDF
     *
     * @param aname
     * @param preloadedTemplate
     * @param dataOwner
     * @param adddata
     * @return
     */
    public static Waitable createFile(String aname, Template preloadedTemplate, DatabaseObject dataOwner, HashMap<String, Object> adddata) {
        Map<String, Object> hm1 = dataOwner.getFormFields();//new FormFieldsHandler(dataOwner).getFormattedFormFields(null);
        File f2;
        if (aname == null) {
            f2 = FileDirectoryHandler.getTempFile("pdf");
        } else {
            f2 = FileDirectoryHandler.getTempFile(aname, "pdf");
        }
        Export ex = new Export(preloadedTemplate);
        ex.putAll(hm1);
        ex.putAll(adddata);
        ex.setTemplate(preloadedTemplate.getExFile());
        ex.setTargetFile(f2);
        return ex;
    }

    /**
     * Create a {@link Waitable} which is able to create a file ODT
     *
     * @param aname
     * @param preloadedTemplate
     * @param dataOwner
     * @param adddata
     * @return
     */
    public static Waitable sourceFile(String aname, Template preloadedTemplate, DatabaseObject dataOwner, HashMap<String, Object> adddata) {
        Map<String, Object> hm1 = dataOwner.getFormFields();//new FormFieldsHandler(dataOwner).getFormattedFormFields(null);
        File f2;
        if (aname == null) {
            f2 = FileDirectoryHandler.getTempFile("odt");
        } else {
            f2 = FileDirectoryHandler.getTempFile(aname, "odt");
        }

        Export ex = new Export(preloadedTemplate);
        ex.putAll(hm1);
        ex.putAll(adddata);
        ex.setTemplate(preloadedTemplate.getExFile());
        ex.setTargetFile(f2);
        return ex;
    }

    /**
     * (Pre)load a template. Do not run this from the EDT, as the fetching of
     * the templatefile from the database might take a while.
     *
     * @param dataOwner
     * @return
     * @deprecated DO NOT use this anymore, will always return NULL
     */
    public static Template loadTemplate(final DatabaseObject dataOwner) {
//        Template preloadedTemplate;
//        preloadedTemplate = TemplateHandler.loadTemplate(dataOwner);
//        Exportable preloadedExportFile;
//        if (preloadedTemplate != null) {
//            try {
//                preloadedExportFile = preloadedTemplate.getExFile();
//            } catch (Exception e) {
//                Log.Debug(e);
//            }
//        }
        return null;
    }

    /*
     * Prints a screen capture of the given component
     */
    public static void print(final Component c) {
        new PrintJob2(c);
    }

    /**
     * Generates a waiter for export
     *
     * @param targetFile
     * @return
     */
    public static Waiter wait(final File targetFile) {
        return new Waiter() {

            @Override
            public void set(Object object, Exception exceptions) throws Exception {
                if (exceptions == null) {
                    if (object instanceof Export) {
                        FileDirectoryHandler.copyFile2(((Export) object).getTargetFile(), targetFile);
                        mpv5.handler.Activity.file(targetFile, ((Export) object).getDob());
                    } else if (object instanceof File) {
                        FileDirectoryHandler.copyFile2((File) object, targetFile);
                        mpv5.handler.Activity.file(targetFile, null);
                    } else {
                        Log.Debug(this, "Not a file: " + String.valueOf(object));
                    }
                } else {
                    Notificator.raiseNotification(exceptions, false);
                    Log.Debug(exceptions);
                }
            }
        };
    }

    private Exportable fromFile;
    private File toFile;
    private final Template template;
    private DatabaseObject dob;
    private boolean embedAllFonts;

    /**
     * @param t
     */
    public Export(Template t) {
        super();
        this.template = t;
        setTemplate(t.getExFile());
    }

    /**
     * Add the data, must be key - value pairs
     *
     * @param data
     */
    public void addData(String[][] data) {
        for (int i = 0; i < data.length; i++) {
            String[] strings = data[i];
            super.put(strings[0], strings[1]);
        }
    }

    /**
     * Set the file to be filled.. not required to be called explicitely
     *
     * @param <T>
     * @param templateFile
     */
    public <T extends Exportable> void setTemplate(T templateFile) {
        this.fromFile = templateFile;
    }

    /**
     * Exports the given data to the target file using the given template file.
     * <br/>
     *
     * @param toFile The target file. If the file exists, will be overwritten if
     *               possible.
     * @throws NodataFoundException  If no data has been previously added
     * @throws FileNotFoundException If no file was given as template
     */
    public void processData(File toFile) throws NodataFoundException, FileNotFoundException {
        if (this.isEmpty()) {
//            throw new NodataFoundException();
//            return;
        }

        if (fromFile == null) {
            throw new FileNotFoundException("You must call setFile(Exportable file) first!");
        } else if (!this.fromFile.exists()) {
            throw new FileNotFoundException(fromFile.getPath());
        }
        synchronized (fromFile) {
            if (toFile.exists()) {
                toFile.delete();
                Log.Debug(this, "File exists, will be replaced: " + toFile);
            } else {
                toFile.getParentFile().mkdirs();
            }

            fromFile.setTarget(toFile);
            fromFile.setData(this);
            fromFile.setTemplate(template);
            fromFile.setEmbedAllFonts(embedAllFonts);
            fromFile.setFailure(null);
            fromFile.run();//dont call this on another thread; Exportable is _NOT_ threadsafe
            Exception failure = fromFile.getFailure();
            fromFile.reset();
            if (failure != null) {
                Throwable root = failure;
                while (root.getCause() != null && root.getCause() != root) {
                    root = root.getCause();
                }
                String name = template != null ? template.__getCname() : fromFile.getName();
                throw new ExportFailedException(String.format(Messages.EXPORT_FAILED.toString(), name, root.getMessage()), failure);
            }
        }
        if (toFile.exists()) {
            mpv5.handler.Activity.file(toFile, dob);
        }
    }

    @Override
    public Exception waitFor() {
        try {
            Log.Debug(Export.class, "processData to " + getTargetFile());
            processData(getTargetFile());
        } catch (Exception ex) {
            return ex;
        }
        return null;
    }

    /**
     * The target file. If the file exists, will be overwritten if possible.
     *
     * @param toFile the toFile to set
     */
    public void setTargetFile(File toFile) {
        this.toFile = toFile;
    }

    /**
     * @return the toFile
     */
    public File getTargetFile() {
        return toFile;
    }

    /**
     * @param embedAllFonts true to embed every font into the created PDF
     * (required for PDF/A, e.g. ZUGFeRD)
     */
    public void setEmbedAllFonts(boolean embedAllFonts) {
        this.embedAllFonts = embedAllFonts;
    }

    private void putDataOwner(DatabaseObject dataOwner) {
        this.dob = dataOwner;
    }

    public DatabaseObject getDob() {
        return dob;
    }
}
