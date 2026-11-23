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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import mpv5.db.common.Context;
import mpv5.db.common.QueryHandler;
import mpv5.db.objects.Item;
import mpv5.db.objects.Template;
import mpv5.db.objects.User;
import mpv5.globals.CompanyInfo;
import mpv5.globals.LocalSettings;
import mpv5.globals.Messages;
import mpv5.handler.Activity;
import mpv5.handler.TemplateHandler;
import mpv5.handler.VariablesHandler;
import mpv5.logging.Log;
import mpv5.YabsViewProxy;
import mpv5.mail.MailAttachments;
import mpv5.mail.MailConfiguration;
import mpv5.mail.SimpleMail;
import mpv5.utils.files.FileDirectoryHandler;
import mpv5.utils.text.TypeConversion;
import javax.mail.internet.InternetAddress;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDDocumentNameDictionary;
import org.apache.pdfbox.pdmodel.PDEmbeddedFilesNameTreeNode;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.mustangproject.Invoice;

/**
 * Automatically sends exported or finalized invoices and cancellation/credit
 * notes ({@link ZugpferdFile#supports(Item)}) to the configured archive email
 * address ({@code companyinfo.mailarchive.*}) with both a ZUGFeRD PDF and an
 * XRechnung XML attachment, and records the result in the YaBS history log.
 */
public final class InvoiceMailArchive {

    public static final String KEY_ADDRESS = "mailarchive.address";
    public static final String KEY_SUBJECT = "mailarchive.subject";
    public static final String KEY_BODY = "mailarchive.body";

    public static final String DEFAULT_SUBJECT = "[type] [cnumber] - [contact.cname]";
    public static final String DEFAULT_BODY = "[type] [cnumber] ([grosvaluef])";

    /**
     * Deduplication set of {@code "<ids>:<cnumber>"} keys already archived in
     * the current session so an export that writes multiple files (.xml + .pdf)
     * or triggers GoBD finalization sends the archive mail only once.
     */
    private static final Set<String> SENT_KEYS = ConcurrentHashMap.newKeySet();
    private static final ThreadLocal<Boolean> IN_PROGRESS = ThreadLocal.withInitial(() -> Boolean.FALSE);

    private static volatile Consumer<SimpleMail> customMailSender;
    private static volatile Throwable lastError;

    private InvoiceMailArchive() {
    }

    /**
     * Installs a custom mail sender (e.g. for unit tests). Pass {@code null} to
     * restore the default {@link SimpleMail} sender.
     */
    public static void setMailSender(Consumer<SimpleMail> sender) {
        customMailSender = sender;
    }

    public static Throwable getLastError() {
        return lastError;
    }

    public static void clearSentHistory() {
        SENT_KEYS.clear();
        lastError = null;
    }

    /**
     * @return validated archive recipient email addresses from
     *         {@link CompanyInfo#getProperty(String)} ({@link #KEY_ADDRESS}).
     */
    public static List<String> getValidAddresses() {
        String raw = CompanyInfo.getProperty(KEY_ADDRESS);
        if (raw == null || raw.trim().isEmpty()) {
            return Collections.emptyList();
        }
        List<String> list = new ArrayList<>();
        for (String part : raw.split("[;,]")) {
            InternetAddress ia = TypeConversion.stringToMail(part.trim());
            String addr = ia != null ? ia.getAddress() : null;
            if (addr != null && !addr.isEmpty() && !list.contains(addr)) {
                list.add(addr);
            }
        }
        return list;
    }

    /**
     * @return {@code true} if at least one valid archive email address is
     *         configured in Company Info.
     */
    public static boolean isEnabled() {
        return !getValidAddresses().isEmpty();
    }

    /**
     * Resolves the configured archive email subject for {@code item}, parsing
     * YaBS variables via {@link VariablesHandler}.
     */
    public static String resolveSubject(Item item) {
        String tpl = CompanyInfo.getProperty(KEY_SUBJECT);
        if (tpl == null || tpl.trim().isEmpty()) {
            tpl = DEFAULT_SUBJECT;
        }
        if (item == null) {
            return tpl.trim();
        }
        String parsed = VariablesHandler.parse(tpl, item, item.getFormFields());
        parsed = parsed == null ? "" : parsed.replaceAll("\\s+", " ").trim();
        if (parsed.isEmpty() || parsed.equals("-")) {
            return (Item.getTypeString(item.__getInttype()) + " " + safeNumber(item)).trim();
        }
        return parsed;
    }

    /**
     * Resolves the configured archive email body for {@code item}, parsing YaBS
     * variables via {@link VariablesHandler}.
     */
    public static String resolveBody(Item item) {
        String tpl = CompanyInfo.getProperty(KEY_BODY);
        if (tpl == null || tpl.trim().isEmpty()) {
            tpl = DEFAULT_BODY;
        }
        if (item == null) {
            return tpl.trim();
        }
        String parsed = VariablesHandler.parse(tpl, item, item.getFormFields());
        parsed = parsed == null ? "" : parsed.trim();
        if (parsed.isEmpty()) {
            return (Item.getTypeString(item.__getInttype()) + " " + safeNumber(item)).trim();
        }
        return parsed;
    }

    /**
     * Hook invoked when a non-draft file (PDF / XML / ODT) is exported for an
     * {@link Item}.
     */
    public static void onExported(Item item, File exportedFile) {
        if (item == null || Boolean.TRUE.equals(IN_PROGRESS.get())) {
            return;
        }
        if (exportedFile != null) {
            String name = exportedFile.getName().toLowerCase();
            if (!name.endsWith(".pdf") && !name.endsWith(".xml") && !name.endsWith(".odt")) {
                return;
            }
        }
        archiveIfEligible(item, exportedFile);
    }

    /**
     * Hook invoked when an {@link Item} is finalized (e.g. via GoBD
     * finalization or cancellation/Storno).
     */
    public static void onFinalized(Item item, File artifactFile) {
        if (item == null || Boolean.TRUE.equals(IN_PROGRESS.get())) {
            return;
        }
        archiveIfEligible(item, artifactFile);
    }

    /**
     * Archives {@code item} by sending the ZUGFeRD PDF and XRechnung XML to the
     * configured archive email address if eligible and not already sent for
     * this document number.
     *
     * @return {@code true} if an archive email was sent, {@code false} if
     *         skipped or failed
     */
    public static boolean archiveIfEligible(Item item, File exportedFile) {
        if (item == null || !ZugpferdFile.supports(item) || !isEnabled()) {
            return false;
        }
        if (Boolean.TRUE.equals(IN_PROGRESS.get())) {
            return false;
        }
        String num = safeNumber(item);
        // Do not archive unfinalized GoBD draft numbers; wait until final number is assigned
        if (num.toUpperCase().startsWith("ENTWURF")) {
            return false;
        }
        String dedupKey = item.__getIDS() + ":" + num;
        if (!SENT_KEYS.add(dedupKey)) {
            return false;
        }
        boolean ok = sendArchiveMail(item, exportedFile);
        if (!ok) {
            SENT_KEYS.remove(dedupKey);
        }
        return ok;
    }

    /**
     * Generates the two archive attachments (1. ZUGFeRD PDF with embedded XML,
     * 2. XRechnung XML) in {@code targetDir}.
     */
    public static List<File> createArchiveAttachments(Item item, File exportedFile, Path targetDir) throws Exception {
        IN_PROGRESS.set(Boolean.TRUE);
        try {
            Files.createDirectories(targetDir);
            String baseName = sanitizeFileName(safeNumber(item));
            if (baseName.isEmpty()) {
                baseName = "invoice-" + item.__getIDS();
            }

            File zugferdPdf = targetDir.resolve(baseName + ".pdf").toFile();
            File xrechnungXml = targetDir.resolve(baseName + ".xml").toFile();

            // 1. Create XRechnung XML file
            Invoice xrechnungInv = ZugpferdFile.createInvoice(item, ZugpferdFile.Format.XRECHNUNG);
            ZugpferdFile.writeXml(xrechnungInv, xrechnungXml);

            // 2. Create ZUGFeRD PDF file
            if (exportedFile != null && exportedFile.isFile()
                    && exportedFile.getName().toLowerCase().endsWith(".pdf")
                    && hasEmbeddedXml(exportedFile)) {
                FileDirectoryHandler.copyFile(exportedFile, zugferdPdf);
            } else {
                Invoice zugferdInv = ZugpferdFile.createInvoice(item, ZugpferdFile.Format.ZUGFERD);
                File basePdf = obtainBasePdf(item, exportedFile, targetDir);
                ZugpferdFile.writePdf(zugferdInv, basePdf, zugferdPdf);
            }

            return Arrays.asList(zugferdPdf, xrechnungXml);
        } finally {
            IN_PROGRESS.set(Boolean.FALSE);
        }
    }

    /**
     * Builds and sends the archive email with ZUGFeRD PDF + XRechnung XML
     * attachments, and logs the entry in the YaBS history table.
     */
    public static boolean sendArchiveMail(Item item, File exportedFile) {
        List<String> recipients = getValidAddresses();
        if (recipients.isEmpty() || item == null || !ZugpferdFile.supports(item)) {
            return false;
        }
        String recipientsLabel = String.join("; ", recipients);
        String docLabel = (Item.getTypeString(item.__getInttype()) + " " + safeNumber(item)).trim();
        Path tempDir = null;
        IN_PROGRESS.set(Boolean.TRUE);
        try {
            File cacheBase = new File(FileDirectoryHandler.getTempDir2());
            cacheBase.mkdirs();
            tempDir = Files.createTempDirectory(cacheBase.toPath(), "yabs-mailarchive-");

            List<File> attachments = createArchiveAttachments(item, exportedFile, tempDir);

            List<String> mailLog = new ArrayList<>();
            SimpleMail mail = new SimpleMail(mailLog);
            try {
                MailConfiguration cfg = User.getCurrentUser().getMailConfiguration();
                if (cfg != null) {
                    mail.setMailConfiguration(cfg);
                }
            } catch (Throwable t) {
                Log.Debug(InvoiceMailArchive.class, t.getMessage());
            }
            if (mail.getSenderAddress() == null || mail.getSenderAddress().trim().isEmpty()) {
                InternetAddress ia = TypeConversion.stringToMail(CompanyInfo.getProperty("email"));
                if (ia != null && ia.getAddress() != null) {
                    mail.setSenderAddress(ia.getAddress());
                }
            }

            mail.setRecipientsAddress(String.join(";", recipients));
            mail.setSubject(resolveSubject(item));
            mail.setText(resolveBody(item));
            mail.setAttachments(attachments);

            Consumer<SimpleMail> sender = customMailSender;
            if (sender != null) {
                sender.accept(mail);
            } else {
                mail.set((MailAttachments) () -> attachments, null);
            }

            String msg = String.format(Messages.COMPANY_MAILARCHIVE_HISTORY_SENT.toString(),
                    recipientsLabel, docLabel);
            recordHistory(item, msg);
            lastError = null;
            return true;
        } catch (Exception e) {
            lastError = e;
            Log.Debug(InvoiceMailArchive.class, e);
            String err = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
            String msg = String.format(Messages.COMPANY_MAILARCHIVE_HISTORY_FAILED.toString(),
                    recipientsLabel, docLabel, err);
            recordHistory(item, msg);
            return false;
        } finally {
            IN_PROGRESS.set(Boolean.FALSE);
            cleanupTempDir(tempDir);
        }
    }

    private static void recordHistory(Item item, String message) {
        try {
            String userName = User.getCurrentUser() != null ? User.getCurrentUser().__getCname() : "System";
            QueryHandler.instanceOf().clone(Context.getHistory()).insertHistoryItem(
                    message,
                    userName,
                    item.getDbIdentity(),
                    item.__getIDS(),
                    item.__getGroupsids());
        } catch (Throwable t) {
            Log.Debug(InvoiceMailArchive.class, t.getMessage());
        }
        try {
            Activity.record(item, Activity.Kind.MESSAGE, message);
        } catch (Throwable t) {
            Log.Debug(InvoiceMailArchive.class, t.getMessage());
        }
        try {
            YabsViewProxy.instance().addMessage(message);
        } catch (Throwable t) {
            Log.Debug(InvoiceMailArchive.class, t.getMessage());
        }
    }

    private static File obtainBasePdf(Item item, File exportedFile, Path targetDir) throws Exception {
        if (exportedFile != null && exportedFile.isFile()
                && exportedFile.getName().toLowerCase().endsWith(".pdf")) {
            return exportedFile;
        }
        Template t = TemplateHandler.loadTemplate((long) item.templateGroupIds(), item.__getInttype());
        if (t != null) {
            try {
                Export exp = (Export) Export.createFile(t, item);
                exp.setDraft(true);
                Exception ex = exp.waitFor();
                File rendered = ex == null ? exp.getTargetFile() : null;
                if (rendered != null && rendered.isFile() && rendered.length() > 0) {
                    return rendered;
                }
            } catch (Throwable ex) {
                Log.Debug(InvoiceMailArchive.class, ex.getMessage());
            }
        }
        File fallbackPdf = targetDir.resolve("base-" + sanitizeFileName(safeNumber(item)) + ".pdf").toFile();
        writeFallbackSummaryPdf(item, fallbackPdf);
        return fallbackPdf;
    }

    private static void writeFallbackSummaryPdf(Item item, File target) throws Exception {
        try (PDDocument doc = new PDDocument()) {
            PDDocumentCatalog catalog = doc.getDocumentCatalog();
            // Add PDF/A-1b XMP metadata and sRGB OutputIntent so Mustang ZUGFeRDExporterFromA1 accepts it
            org.apache.pdfbox.pdmodel.common.PDMetadata metadata = new org.apache.pdfbox.pdmodel.common.PDMetadata(doc);
            String xmp = "<?xpacket begin=\"\uFEFF\" id=\"W5M0MpGFRHzreSzNTczkc9d\"?>"
                    + "<x:xmpmeta xmlns:x=\"adobe:ns:meta/\">"
                    + "<rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\">"
                    + "<rdf:Description rdf:about=\"\" xmlns:pdfaid=\"http://www.aiim.org/pdfa/ns/id/\">"
                    + "<pdfaid:part>1</pdfaid:part><pdfaid:conformance>B</pdfaid:conformance>"
                    + "</rdf:Description></rdf:RDF></x:xmpmeta><?xpacket end=\"w\"?>";
            try (java.io.OutputStream os = metadata.createOutputStream()) {
                os.write(xmp.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            }
            catalog.setMetadata(metadata);
            java.awt.color.ICC_Profile rgbProfile = java.awt.color.ICC_Profile.getInstance(java.awt.color.ColorSpace.CS_sRGB);
            byte[] profileBytes = rgbProfile.getData();
            org.apache.pdfbox.pdmodel.graphics.color.PDOutputIntent oi =
                    new org.apache.pdfbox.pdmodel.graphics.color.PDOutputIntent(doc, new java.io.ByteArrayInputStream(profileBytes));
            oi.setInfo("sRGB IEC61966-2.1");
            oi.setOutputCondition("sRGB IEC61966-2.1");
            oi.setOutputConditionIdentifier("sRGB IEC61966-2.1");
            oi.setRegistryName("http://www.color.org");
            catalog.addOutputIntent(oi);

            PDPage page = new PDPage(PDRectangle.A4);
            page.setResources(new org.apache.pdfbox.pdmodel.PDResources());
            doc.addPage(page);
            doc.save(target);
        }
    }

    private static String sanitizePdfText(String s) {
        if (s == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c >= 32 && c <= 255) {
                sb.append(c);
            } else {
                sb.append('?');
            }
        }
        return sb.toString();
    }

    public static boolean hasEmbeddedXml(File pdfFile) {
        if (pdfFile == null || !pdfFile.isFile()) {
            return false;
        }
        try (PDDocument doc = Loader.loadPDF(pdfFile)) {
            PDDocumentCatalog catalog = doc.getDocumentCatalog();
            if (catalog == null) {
                return false;
            }
            PDDocumentNameDictionary names = catalog.getNames();
            if (names == null) {
                return false;
            }
            PDEmbeddedFilesNameTreeNode ef = names.getEmbeddedFiles();
            if (ef == null) {
                return false;
            }
            if (ef.getNames() != null && !ef.getNames().isEmpty()) {
                return true;
            }
            if (ef.getKids() != null && !ef.getKids().isEmpty()) {
                return true;
            }
        } catch (Throwable ignored) {
        }
        return false;
    }

    private static String safeNumber(Item item) {
        if (item == null) {
            return "";
        }
        String num = item.__getCnumber();
        if (num == null || num.trim().isEmpty()) {
            num = item.__getCname();
        }
        return num == null ? "" : num.trim();
    }

    private static String sanitizeFileName(String raw) {
        if (raw == null) {
            return "";
        }
        return raw.replaceAll("[\\\\/:*?\"<>|\\s]+", "_");
    }

    private static void cleanupTempDir(Path tempDir) {
        if (tempDir == null) {
            return;
        }
        try {
            File[] files = tempDir.toFile().listFiles();
            if (files != null) {
                for (File f : files) {
                    f.delete();
                }
            }
            tempDir.toFile().delete();
        } catch (Throwable ignored) {
        }
    }
}
