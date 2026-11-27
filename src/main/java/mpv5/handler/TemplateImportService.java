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
package mpv5.handler;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.db.common.NodataFoundException;
import mpv5.db.common.QueryCriteria;
import mpv5.db.common.QueryData;
import mpv5.db.common.QueryHandler;
import mpv5.db.objects.Template;
import mpv5.db.objects.User;
import mpv5.globals.Constants;
import mpv5.logging.Log;
import mpv5.utils.export.ZugpferdFile;
import mpv5.utils.files.FileDirectoryHandler;

/**
 * Bulk import (multi-file and ZIP extraction), filename-based template type
 * detection, default assignment to all users, bulk edit, and multi-delete for
 * templates.
 */
public final class TemplateImportService {

    private static final long MAX_EXTRACTED_ODT_BYTES = 50L * 1024L * 1024L;

    private TemplateImportService() {
    }

    /**
     * One template file ready to be imported (either a user-selected .odt or
     * extracted from a .zip archive into a temporary file).
     */
    public static final class ExtractedTemplate {

        public final File file;
        public final String displayName;
        public final boolean temporary;

        public ExtractedTemplate(File file, String displayName, boolean temporary) {
            this.file = file;
            this.displayName = displayName;
            this.temporary = temporary;
        }
    }

    /**
     * Specifies which attributes should be applied during a bulk edit.
     * Only properties whose {@code apply*} flag is {@code true} are modified.
     */
    public static final class BulkEditSpec {

        public boolean applyType;
        public String mimetype;

        public boolean applyGroup;
        public int groupId = 1;

        public boolean applyUsers;
        public List<User> users = Collections.emptyList();

        public boolean applyFormat;
        public String format = Template.DEFAULT_FORMAT;

        public boolean applyPrinter;
        public String printer = Template.PRINTER_UNDEFINED;

        public boolean applyPayterm;
        public int paytermDays = ZugpferdFile.PAYTERM_UNSET;

        public boolean hasAnyChange() {
            return applyType || applyGroup || applyUsers || applyFormat || applyPrinter || applyPayterm;
        }
    }

    /**
     * Derives the default template display name from a filename by stripping
     * any path prefix and trailing {@code .odt} extension.
     *
     * @param filename original filename or zip entry name
     * @return clean template name
     */
    public static String defaultTemplateName(String filename) {
        if (filename == null) {
            return "Template";
        }
        String base = baseName(filename).trim();
        if (base.toLowerCase(Locale.ROOT).endsWith(".odt")) {
            base = base.substring(0, base.length() - 4).trim();
        }
        return base.isEmpty() ? "Template" : base;
    }

    /**
     * Guesses the {@link Constants} / {@link TemplateHandler} document type
     * from a template filename (supporting German and English naming conventions).
     * More specific document types (e.g. order confirmation, credit note, part
     * payment, deposit, purchase order) are checked before generic ones (order,
     * invoice).
     *
     * @param filename filename or path of the template
     * @return matching {@code Constants.TYPE_*} id, or {@code null} if unknown
     */
    public static Integer guessTemplateType(String filename) {
        if (filename == null || filename.trim().isEmpty()) {
            return null;
        }
        String stem = defaultTemplateName(filename);
        String norm = normalize(stem);
        Set<String> tokens = new HashSet<String>(Arrays.asList(norm.split("[^a-z0-9]+")));
        String compact = norm.replaceAll("[^a-z0-9]+", "");

        // 1. Order confirmation (must come before "auftrag"/"order")
        if (containsAny(compact, "auftragsbestaetigung", "auftragsbest", "orderconfirmation", "bestaetigung")
                || (tokens.contains("order") && tokens.contains("confirmation"))
                || tokens.contains("ab")) {
            return Constants.TYPE_ORDER_CONFIRMATION;
        }
        // 2. Purchase / product order (must come before "order")
        if (containsAny(compact, "bestellung", "warenbestellung", "lieferantenbestellung", "productorder", "purchaseorder")
                || (tokens.contains("product") && tokens.contains("order"))
                || (tokens.contains("purchase") && tokens.contains("order"))
                || tokens.contains("po")) {
            return Constants.TYPE_PRODUCT_ORDER;
        }
        // 3. Delivery note
        if (containsAny(compact, "lieferschein", "deliverynote", "delivery", "versandschein")
                || tokens.contains("ls")) {
            return Constants.TYPE_DELIVERY_NOTE;
        }
        // 4. Part payment / progress invoice (must come before "rechnung"/"invoice")
        if (containsAny(compact, "abschlagsrechnung", "abschlag", "teilrechnung", "partpayment", "progressinvoice", "partialinvoice")
                || (tokens.contains("part") && tokens.contains("payment"))
                || tokens.contains("ar")) {
            return Constants.TYPE_PART_PAYMENT;
        }
        // 5. Deposit invoice (must come before "rechnung"/"invoice")
        if (containsAny(compact, "anzahlungsrechnung", "anzahlung", "vorauszahlung", "deposit", "downpayment")
                || (tokens.contains("down") && tokens.contains("payment"))
                || tokens.contains("az")) {
            return Constants.TYPE_DEPOSIT;
        }
        // 6. Credit note / cancellation (must come before "rechnung"/"invoice")
        if (containsAny(compact, "gutschrift", "stornorechnung", "storno", "korrekturrechnung", "creditnote", "cancellation", "credit")
                || tokens.contains("gs")
                || tokens.contains("st")) {
            return Constants.TYPE_CREDIT;
        }
        // 7. Reminder / dunning
        if (containsAny(compact, "mahnung", "zahlungserinnerung", "reminder", "dunning")) {
            return Constants.TYPE_REMINDER;
        }
        // 8. Invoice
        if (containsAny(compact, "rechnung", "invoice", "faktura", "bill")
                || tokens.contains("re")
                || tokens.contains("inv")) {
            return Constants.TYPE_INVOICE;
        }
        // 9. Offer / quote
        if (containsAny(compact, "angebot", "kostenvoranschlag", "offer", "quotation", "quote")
                || tokens.contains("ag")
                || tokens.contains("an")) {
            return Constants.TYPE_OFFER;
        }
        // 10. Order
        if (containsAny(compact, "auftrag", "order")
                || tokens.contains("au")) {
            return Constants.TYPE_ORDER;
        }
        // 11. Contract
        if (containsAny(compact, "vertrag", "contract")) {
            return Constants.TYPE_CONTRACT;
        }
        // 12. Conversation / protocol
        if (containsAny(compact, "gespraech", "conversation", "protokoll", "protocol")) {
            return Constants.TYPE_CONVERSATION;
        }
        // 13. Activity list / timesheet
        if (containsAny(compact, "taetigkeit", "stundenzettel", "leistungsnachweis", "activity", "timesheet")) {
            return Constants.TYPE_ACTIVITY;
        }
        // 14. Mass print / mail merge
        if (containsAny(compact, "massendruck", "serienbrief", "massprint", "mailmerge")) {
            return Constants.TYPE_MASSPRINT;
        }
        // 15. Journal
        if (containsAny(compact, "journal", "buchungsjournal")) {
            return Constants.TYPE_JOURNAL;
        }
        // 16. Contact / customer letter / sheet
        if (containsAny(compact, "kontakt", "stammblatt", "anschreiben", "brief", "contact", "customer", "letter")) {
            return Constants.TYPE_CONTACT;
        }
        // 17. Service
        if (containsAny(compact, "dienstleistung", "service")) {
            return Constants.TYPE_SERVICE;
        }
        // 18. Product / article
        if (containsAny(compact, "produkt", "artikel", "product", "article")) {
            return Constants.TYPE_PRODUCT;
        }
        return null;
    }

    /**
     * Collects all {@code .odt} template files from a list of user-selected
     * files, expanding any {@code .zip} archives into temporary files.
     *
     * @param selectedFiles selected {@code .odt} and/or {@code .zip} files
     * @return extracted template descriptors (callers must clean up temporary files)
     * @throws IOException if reading a zip archive fails
     */
    public static List<ExtractedTemplate> collectOdtFiles(File[] selectedFiles) throws IOException {
        List<ExtractedTemplate> out = new ArrayList<ExtractedTemplate>();
        if (selectedFiles == null) {
            return out;
        }
        for (File f : selectedFiles) {
            if (f == null || !f.isFile()) {
                continue;
            }
            String lower = f.getName().toLowerCase(Locale.ROOT);
            if (lower.endsWith(".odt")) {
                out.add(new ExtractedTemplate(f, f.getName(), false));
            } else if (lower.endsWith(".zip")) {
                extractZipOdtFiles(f, out);
            }
        }
        return out;
    }

    private static void extractZipOdtFiles(File zipFile, List<ExtractedTemplate> out) throws IOException {
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile))) {
            ZipEntry entry;
            byte[] buffer = new byte[8192];
            while ((entry = zis.getNextEntry()) != null) {
                try {
                    if (entry.isDirectory()) {
                        continue;
                    }
                    String entryName = entry.getName();
                    if (entryName == null) {
                        continue;
                    }
                    String normalizedPath = entryName.replace('\\', '/');
                    if (normalizedPath.startsWith("__MACOSX/") || normalizedPath.contains("/__MACOSX/")) {
                        continue;
                    }
                    String simpleName = baseName(normalizedPath);
                    if (simpleName.isEmpty() || simpleName.startsWith(".") || simpleName.startsWith("._")) {
                        continue;
                    }
                    if (!simpleName.toLowerCase(Locale.ROOT).endsWith(".odt")) {
                        continue;
                    }
                    File tmp = Files.createTempFile("yabs-tpl-", ".odt").toFile();
                    tmp.deleteOnExit();
                    long total = 0;
                    try (FileOutputStream fos = new FileOutputStream(tmp)) {
                        int read;
                        while ((read = zis.read(buffer)) != -1) {
                            total += read;
                            if (total > MAX_EXTRACTED_ODT_BYTES) {
                                throw new IOException("ZIP entry exceeds maximum allowed template size: " + simpleName);
                            }
                            fos.write(buffer, 0, read);
                        }
                    } catch (IOException ex) {
                        tmp.delete();
                        throw ex;
                    }
                    out.add(new ExtractedTemplate(tmp, simpleName, true));
                } finally {
                    zis.closeEntry();
                }
            }
        }
    }

    /**
     * Imports one or more {@code .odt} or {@code .zip} files into the database,
     * guessing each template's document type from its filename and assigning
     * every existing user by default.
     *
     * @param selectedFiles selected {@code .odt} and/or {@code .zip} files
     * @param defaultGroupId group id for imported templates (falls back to 1)
     * @return list of created {@link Template} records
     * @throws Exception if file extraction or storage fails
     */
    public static List<Template> importFiles(File[] selectedFiles, int defaultGroupId) throws Exception {
        int groupId = defaultGroupId > 0 ? defaultGroupId : 1;
        List<ExtractedTemplate> extracted = collectOdtFiles(selectedFiles);
        List<Template> created = new ArrayList<Template>();
        if (extracted.isEmpty()) {
            return created;
        }
        List<User> allUsers = loadAllUsers();
        int addedBy = User.getCurrentUser() != null && User.getCurrentUser().__getIDS() > 0
                ? User.getCurrentUser().__getIDS() : 1;
        try {
            for (ExtractedTemplate item : extracted) {
                String storedFilename = QueryHandler.instanceOf().clone(Context.getFiles()).insertFileNow(item.file);
                if (storedFilename == null || storedFilename.trim().isEmpty()) {
                    throw new IOException("Failed to store template file: " + item.displayName);
                }
                Template tpl = new Template();
                tpl.setCname(defaultTemplateName(item.displayName));
                tpl.setFilename(storedFilename);
                tpl.setIntsize((int) Math.min(Integer.MAX_VALUE, item.file.length()));
                Integer guessed = guessTemplateType(item.displayName);
                tpl.setMimetype(guessed != null ? String.valueOf(guessed) : "odt");
                tpl.setFormat(Template.DEFAULT_FORMAT);
                tpl.setPrinter(Template.PRINTER_UNDEFINED);
                tpl.setGroupsids(groupId);
                tpl.setDateadded(new Date());
                tpl.setIntaddedby(addedBy);
                tpl.setIsupdateenabled(false);
                if (tpl.save(true)) {
                    assignUsers(tpl, allUsers, groupId);
                    created.add(tpl);
                }
            }
        } finally {
            for (ExtractedTemplate item : extracted) {
                if (item.temporary && item.file != null && item.file.exists()) {
                    item.file.delete();
                }
            }
            TemplateHandler.clearCache();
        }
        return created;
    }

    /**
     * Replaces the assigned users of a template in {@code templatestousers}.
     *
     * @param template saved template
     * @param users users to assign (may be empty)
     * @param groupId group id stored on the assignment rows
     */
    public static void assignUsers(Template template, Collection<User> users, int groupId) {
        if (template == null || !template.isExisting()) {
            return;
        }
        int gid = groupId > 0 ? groupId : 1;
        try {
            QueryHandler.instanceOf().clone(Context.getTemplatesToUsers())
                    .delete(new QueryCriteria("templatesids", template.__getIDS()));
        } catch (Exception ex) {
            Log.Debug(TemplateImportService.class, ex);
        }
        Set<Integer> seen = new LinkedHashSet<Integer>();
        if (users != null) {
            for (User u : users) {
                if (u != null && u.__getIDS() != null && u.__getIDS() > 0) {
                    seen.add(u.__getIDS());
                }
            }
        }
        for (Integer uid : seen) {
            QueryData row = new QueryData();
            row.add("usersids", uid);
            row.add("templatesids", template.__getIDS());
            row.add("groupsids", gid);
            row.add("cname", template.__getIDS() + "@" + uid + "@" + gid);
            QueryHandler.instanceOf().clone(Context.getTemplatesToUsers()).insert(row, null);
        }
        DatabaseObject.uncacheObject(Context.getTemplate(), template.__getIDS());
    }

    /**
     * Deletes multiple templates at once, stopping any active LibreOffice
     * edit watchers and clearing template caches.
     *
     * @param templates templates to delete
     * @return number of deleted templates
     */
    public static int deleteTemplates(Collection<Template> templates) {
        if (templates == null || templates.isEmpty()) {
            return 0;
        }
        int deleted = 0;
        for (Template t : templates) {
            if (t == null || !t.isExisting()) {
                continue;
            }
            TemplateEditing.stop(t);
            try {
                QueryHandler.instanceOf().clone(Context.getTemplatesToUsers())
                        .delete(new QueryCriteria("templatesids", t.__getIDS()));
            } catch (Exception ex) {
                Log.Debug(TemplateImportService.class, ex);
            }
            if (t.delete()) {
                deleted++;
            }
            DatabaseObject.uncacheObject(Context.getTemplate(), t.__getIDS());
        }
        TemplateHandler.clearCache();
        return deleted;
    }

    /**
     * Applies all enabled fields of {@code spec} to every template in
     * {@code templates}.
     *
     * @param templates templates to modify
     * @param spec changes to apply
     * @return number of templates updated
     */
    public static int applyBulkEdit(Collection<Template> templates, BulkEditSpec spec) {
        if (templates == null || templates.isEmpty() || spec == null || !spec.hasAnyChange()) {
            return 0;
        }
        int updated = 0;
        for (Template t : templates) {
            if (t == null || !t.isExisting()) {
                continue;
            }
            boolean needsSave = false;
            if (spec.applyType && spec.mimetype != null) {
                t.setMimetype(spec.mimetype);
                needsSave = true;
            }
            if (spec.applyGroup && spec.groupId > 0) {
                t.setGroupsids(spec.groupId);
                needsSave = true;
            }
            if (spec.applyFormat && spec.format != null) {
                t.setFormat(spec.format);
                needsSave = true;
            }
            if (spec.applyPrinter && spec.printer != null) {
                t.setPrinter(spec.printer);
                needsSave = true;
            }
            if (needsSave) {
                t.save(true);
            }
            if (spec.applyPayterm) {
                ZugpferdFile.savePayterm(t, spec.paytermDays);
            }
            int effectiveGroup = t.__getGroupsids() > 0 ? t.__getGroupsids() : 1;
            if (spec.applyUsers) {
                assignUsers(t, spec.users, effectiveGroup);
            } else if (spec.applyGroup) {
                // Keep existing user assignments aligned with the updated template group
                List<User> existing = loadAssignedUsers(t);
                if (!existing.isEmpty()) {
                    assignUsers(t, existing, effectiveGroup);
                }
            }
            DatabaseObject.uncacheObject(Context.getTemplate(), t.__getIDS());
            updated++;
        }
        TemplateHandler.clearCache();
        return updated;
    }

    /**
     * @return all users currently stored in the database
     */
    public static List<User> loadAllUsers() {
        List<User> users = new ArrayList<User>();
        try {
            for (DatabaseObject o : DatabaseObject.getObjects(Context.getUser())) {
                if (o instanceof User) {
                    users.add((User) o);
                }
            }
        } catch (NodataFoundException ex) {
            Log.Debug(TemplateImportService.class, ex.getMessage());
        }
        return users;
    }

    /**
     * @param template template to query
     * @return users currently assigned to {@code template}
     */
    public static List<User> loadAssignedUsers(Template template) {
        List<User> out = new ArrayList<User>();
        if (template == null || !template.isExisting()) {
            return out;
        }
        Set<Integer> ids = new LinkedHashSet<Integer>();
        try {
            Object[][] data = QueryHandler.instanceOf().clone(Context.getTemplatesToUsers())
                    .select("usersids", new QueryCriteria("templatesids", template.__getIDS()));
            for (Object[] row : data) {
                ids.add(Integer.valueOf(String.valueOf(row[0])));
            }
        } catch (Exception ex) {
            Log.Debug(TemplateImportService.class, ex);
        }
        for (User u : loadAllUsers()) {
            if (ids.contains(u.__getIDS())) {
                out.add(u);
            }
        }
        return out;
    }

    /**
     * Outcome of importing a bundled plugin template.
     */
    public static final class PluginTemplateResult {

        public final Template template;
        public final boolean created;
        public final boolean assigned;

        public PluginTemplateResult(Template template, boolean created, boolean assigned) {
            this.template = template;
            this.created = created;
            this.assigned = assigned;
        }
    }

    /**
     * Ensures {@code user} is assigned to {@code template} in
     * {@code templatestousers} without removing existing assignments for other
     * users.
     *
     * @param template saved template
     * @param user user to assign
     * @return {@code true} if a new assignment row was added
     */
    public static boolean ensureUserAssigned(Template template, User user) {
        if (template == null || !template.isExisting() || user == null || user.__getIDS() == null || user.__getIDS() <= 0) {
            return false;
        }
        int uid = user.__getIDS();
        for (User existing : loadAssignedUsers(template)) {
            if (existing != null && existing.__getIDS() != null && existing.__getIDS() == uid) {
                return false;
            }
        }
        int gid = template.__getGroupsids() > 0 ? template.__getGroupsids()
                : (user.__getGroupsids() > 0 ? user.__getGroupsids() : 1);
        QueryData row = new QueryData();
        row.add("usersids", uid);
        row.add("templatesids", template.__getIDS());
        row.add("groupsids", gid);
        row.add("cname", template.__getIDS() + "@" + uid + "@" + gid);
        QueryHandler.instanceOf().clone(Context.getTemplatesToUsers()).insert(row, null);
        DatabaseObject.uncacheObject(Context.getTemplate(), template.__getIDS());
        TemplateHandler.clearCache();
        refreshOpenTabsSafely();
        return true;
    }

    /**
     * Finds a template by its exact display name ({@code cname}), or returns
     * {@code null} if none exists.
     */
    public static Template findTemplateByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return null;
        }
        try {
            for (DatabaseObject o : DatabaseObject.getObjects(Context.getTemplate())) {
                if (o instanceof Template && name.equals(o.__getCname())) {
                    return (Template) o;
                }
            }
        } catch (Exception ex) {
            Log.Debug(TemplateImportService.class, ex.getMessage());
        }
        return null;
    }

    /**
     * Imports a bundled plugin template ({@code .fodt} or {@code .odt}) into
     * OpenYaBS (converting {@code .fodt} into a valid XDocReport/ODF
     * {@code .odt} package) and assigns it to the current user. Idempotent: if
     * a template with {@code templateName} already exists, ensures the current
     * user is assigned without creating a duplicate.
     *
     * @param resourceOwner class whose classloader provides {@code resourcePath}
     * @param resourcePath classpath path to the bundled {@code .fodt} or {@code .odt}
     * @param templateName display name stored in OpenYaBS templates
     * @param documentType {@code Constants.TYPE_*} document type
     * @param format position table column order (defaults to {@link FirstStartSetup#ITEM_FORMAT})
     * @param description template description stored in the database
     * @return result with the imported/existing template and flags
     * @throws Exception if reading, converting, or storing fails
     */
    public static PluginTemplateResult importPluginTemplate(
            Class<?> resourceOwner,
            String resourcePath,
            String templateName,
            int documentType,
            String format,
            String description) throws Exception {
        User currentUser = User.getCurrentUser();
        Template existing = findTemplateByName(templateName);
        if (existing != null) {
            boolean assigned = ensureUserAssigned(existing, currentUser);
            return new PluginTemplateResult(existing, false, assigned);
        }

        byte[] raw;
        try (InputStream in = resourceOwner.getResourceAsStream(resourcePath)) {
            if (in == null) {
                throw new IOException("Template resource not found: " + resourcePath);
            }
            raw = in.readAllBytes();
        }
        byte[] odtBytes = resourcePath.toLowerCase(Locale.ROOT).endsWith(".fodt")
                ? fodtToOdt(raw) : raw;

        String stem = defaultTemplateName(baseName(resourcePath)).replaceAll("[^A-Za-z0-9._-]+", "_");
        File tmp = FileDirectoryHandler.getTempFile(stem.isEmpty() ? "plugin-template" : stem, "odt");
        try {
            Files.write(tmp.toPath(), odtBytes);
            String stored = QueryHandler.instanceOf().clone(Context.getFiles()).insertFileNow(tmp);
            if (stored == null || stored.trim().isEmpty()) {
                throw new IOException("Failed to store template file: " + templateName);
            }
            int addedBy = currentUser != null && currentUser.__getIDS() != null && currentUser.__getIDS() > 0
                    ? currentUser.__getIDS() : 1;
            int groupId = currentUser != null && currentUser.__getGroupsids() > 0
                    ? currentUser.__getGroupsids() : 1;

            QueryData x = new QueryData();
            x.add("cname", templateName);
            x.add("filename", stored);
            x.add("description", description == null ? "" : description);
            x.add("dateadded", mpv5.utils.date.DateConverter.getTodayDBDate());
            x.add("intaddedby", addedBy);
            x.add("intsize", tmp.length());
            x.add("mimetype", String.valueOf(documentType));
            x.add("format", (format == null || format.trim().isEmpty()) ? FirstStartSetup.ITEM_FORMAT : format);
            x.add("groupsids", groupId);
            int id = QueryHandler.instanceOf().clone(Context.getTemplate()).insert(x, null);
            if (id <= 0) {
                throw new IOException("Could not create template record: " + templateName);
            }
            DatabaseObject.uncacheObject(Context.getTemplate(), id);
            Template created = (Template) DatabaseObject.getObject(Context.getTemplate(), id);
            boolean assigned = ensureUserAssigned(created, currentUser);
            TemplateHandler.clearCache();
            refreshOpenTabsSafely();
            return new PluginTemplateResult(created, true, assigned);
        } finally {
            if (tmp != null && tmp.exists()) {
                tmp.delete();
            }
        }
    }

    private static void refreshOpenTabsSafely() {
        try {
            if (!mpv5.Main.HEADLESS && !java.awt.GraphicsEnvironment.isHeadless()
                    && mpv5.ui.frames.MPView.identifierView != null) {
                mpv5.YabsViewProxy.instance().refreshOpenTabsTemplates();
            }
        } catch (Throwable ignored) {
        }
    }

    private static final Pattern FODT_PLACEHOLDER = Pattern.compile("\\[([a-zA-Z][a-zA-Z0-9_.]*)\\]");
    private static final Pattern FODT_XTABLE1_PARA = Pattern.compile("<text:p(?:\\s[^>]*)?>\\s*\\[xtable1\\]\\s*</text:p>");
    private static final Pattern FODT_XTABLE0 = Pattern.compile(
            "<table:table[^>]*table:name=\"xtable0\"[^>]*>.*?</table:table>", Pattern.DOTALL);
    private static final Pattern FODT_FIRST_ROW = Pattern.compile(
            "<table:table-row[^>]*>.*?</table:table-row>", Pattern.DOTALL);
    private static final Pattern FODT_STYLES_BLOCK = Pattern.compile(
            "<office:styles[^>]*>(.*?)</office:styles>", Pattern.DOTALL);
    private static final Pattern FODT_AUTO_STYLES_BLOCK = Pattern.compile(
            "<office:automatic-styles[^>]*>(.*?)</office:automatic-styles>", Pattern.DOTALL);
    private static final Pattern FODT_HEADING_NO_STYLE = Pattern.compile("<text:h(?![^>]*text:style-name=)");

    /**
     * Converts a flat OpenDocument text template ({@code .fodt}) using
     * {@code [field.name]} placeholders into a standard ZIP-packaged ODF 1.2
     * {@code .odt} file compatible with OpenYaBS ({@code ODTFile2} / XDocReport
     * and LibreOffice Writer).
     *
     * @param fodtBytes UTF-8 bytes of a {@code .fodt} document
     * @return valid {@code .odt} archive bytes
     * @throws IOException if the input has no {@code <office:body>} or ZIP creation fails
     */
    public static byte[] fodtToOdt(byte[] fodtBytes) throws IOException {
        String xml = new String(fodtBytes, StandardCharsets.UTF_8);
        int bodyStart = xml.indexOf("<office:body");
        int bodyEnd = xml.lastIndexOf("</office:body>");
        if (bodyStart < 0 || bodyEnd < 0 || bodyEnd <= bodyStart) {
            throw new IOException("Invalid FODT template: missing <office:body>");
        }
        String body = xml.substring(bodyStart, bodyEnd + "</office:body>".length());

        String extraStyles = "";
        Matcher sm = FODT_STYLES_BLOCK.matcher(xml);
        if (sm.find()) {
            extraStyles = sm.group(1);
        }
        String extraAutoStyles = "";
        Matcher am = FODT_AUTO_STYLES_BLOCK.matcher(xml);
        if (am.find()) {
            extraAutoStyles = am.group(1);
        }

        boolean english = body.contains("Invoice Date:") || body.contains("Gross Total:")
                || body.contains("Work Summary:") || body.contains("Customer:");

        // 1. Convert <text:p>[xtable1]</text:p> into a header table + OpenYaBS xtable1 position table
        body = FODT_XTABLE1_PARA.matcher(body).replaceAll(Matcher.quoteReplacement(buildPositionTables(english, null)));

        // 2. Convert legacy <table:table table:name="xtable0"> into header_table1 + xtable1
        Matcher tm = FODT_XTABLE0.matcher(body);
        if (tm.find()) {
            String origTable = tm.group(0);
            Matcher rm = FODT_FIRST_ROW.matcher(origTable);
            String customHeaderRow = rm.find() ? rm.group(0) : null;
            body = tm.replaceAll(Matcher.quoteReplacement(buildPositionTables(english, customHeaderRow)));
        }

        // 3. Convert [placeholder] syntax to ODF <text:placeholder> for YabsODTContentHandler
        body = FODT_PLACEHOLDER.matcher(body)
                .replaceAll("<text:placeholder text:placeholder-type=\"text\">&lt;$1&gt;</text:placeholder>");

        // 4. Give unstyled headings the default Heading style
        body = FODT_HEADING_NO_STYLE.matcher(body).replaceAll("<text:h text:style-name=\"Heading\"");

        StringBuilder stylesInner = new StringBuilder();
        if (!extraStyles.contains("style:name=\"Standard\"")) {
            stylesInner.append("<style:style style:name=\"Standard\" style:family=\"paragraph\">")
                    .append("<style:paragraph-properties fo:margin-top=\"0cm\" fo:margin-bottom=\"0.12cm\"/>")
                    .append("<style:text-properties style:font-name=\"Liberation Sans\" fo:font-family=\"Liberation Sans\" fo:font-size=\"10pt\"/>")
                    .append("</style:style>");
        }
        if (!extraStyles.contains("style:name=\"Heading\"")) {
            stylesInner.append("<style:style style:name=\"Heading\" style:family=\"paragraph\" style:parent-style-name=\"Standard\">")
                    .append("<style:paragraph-properties fo:margin-top=\"0.3cm\" fo:margin-bottom=\"0.2cm\"/>")
                    .append("<style:text-properties style:font-name=\"Liberation Sans\" fo:font-family=\"Liberation Sans\" fo:font-size=\"14pt\" fo:font-weight=\"bold\"/>")
                    .append("</style:style>");
        }
        if (!extraStyles.contains("style:name=\"Bold\"")) {
            stylesInner.append("<style:style style:name=\"Bold\" style:family=\"paragraph\" style:parent-style-name=\"Standard\">")
                    .append("<style:text-properties style:font-name=\"Liberation Sans\" fo:font-family=\"Liberation Sans\" fo:font-size=\"10pt\" fo:font-weight=\"bold\"/>")
                    .append("</style:style>");
        }
        if (!extraStyles.contains("style:name=\"Small\"")) {
            stylesInner.append("<style:style style:name=\"Small\" style:family=\"paragraph\" style:parent-style-name=\"Standard\">")
                    .append("<style:text-properties style:font-name=\"Liberation Sans\" fo:font-family=\"Liberation Sans\" fo:font-size=\"8.5pt\"/>")
                    .append("</style:style>");
        }
        stylesInner.append(extraStyles);

        String stylesXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<office:document-styles"
                + " xmlns:office=\"urn:oasis:names:tc:opendocument:xmlns:office:1.0\""
                + " xmlns:style=\"urn:oasis:names:tc:opendocument:xmlns:style:1.0\""
                + " xmlns:text=\"urn:oasis:names:tc:opendocument:xmlns:text:1.0\""
                + " xmlns:table=\"urn:oasis:names:tc:opendocument:xmlns:table:1.0\""
                + " xmlns:fo=\"urn:oasis:names:tc:opendocument:xmlns:xsl-fo-compatible:1.0\""
                + " xmlns:svg=\"urn:oasis:names:tc:opendocument:xmlns:svg-compatible:1.0\""
                + " office:version=\"1.2\">"
                + "<office:font-face-decls>"
                + "<style:font-face style:name=\"Liberation Sans\" svg:font-family=\"&apos;Liberation Sans&apos;, Arial, sans-serif\" style:font-family-generic=\"swiss\" style:font-pitch=\"variable\"/>"
                + "</office:font-face-decls>"
                + "<office:styles>"
                + stylesInner
                + "</office:styles>"
                + "<office:automatic-styles>"
                + "<style:page-layout style:name=\"Mpm1\">"
                + "<style:page-layout-properties fo:page-width=\"21cm\" fo:page-height=\"29.7cm\" style:print-orientation=\"portrait\""
                + " fo:margin-top=\"2cm\" fo:margin-bottom=\"2cm\" fo:margin-left=\"2cm\" fo:margin-right=\"2cm\"/>"
                + "</style:page-layout>"
                + "</office:automatic-styles>"
                + "<office:master-styles>"
                + "<style:master-page style:name=\"Standard\" style:page-layout-name=\"Mpm1\"/>"
                + "</office:master-styles>"
                + "</office:document-styles>";

        String contentXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<office:document-content"
                + " xmlns:office=\"urn:oasis:names:tc:opendocument:xmlns:office:1.0\""
                + " xmlns:style=\"urn:oasis:names:tc:opendocument:xmlns:style:1.0\""
                + " xmlns:text=\"urn:oasis:names:tc:opendocument:xmlns:text:1.0\""
                + " xmlns:table=\"urn:oasis:names:tc:opendocument:xmlns:table:1.0\""
                + " xmlns:fo=\"urn:oasis:names:tc:opendocument:xmlns:xsl-fo-compatible:1.0\""
                + " xmlns:svg=\"urn:oasis:names:tc:opendocument:xmlns:svg-compatible:1.0\""
                + " office:version=\"1.2\">"
                + "<office:font-face-decls>"
                + "<style:font-face style:name=\"Liberation Sans\" svg:font-family=\"&apos;Liberation Sans&apos;, Arial, sans-serif\" style:font-family-generic=\"swiss\" style:font-pitch=\"variable\"/>"
                + "</office:font-face-decls>"
                + "<office:automatic-styles>"
                + "<style:style style:name=\"xtable1\" style:family=\"table\"><style:table-properties style:width=\"17cm\" table:align=\"left\"/></style:style>"
                + "<style:style style:name=\"xtable1.A\" style:family=\"table-column\"><style:table-column-properties style:column-width=\"1.2cm\"/></style:style>"
                + "<style:style style:name=\"xtable1.B\" style:family=\"table-column\"><style:table-column-properties style:column-width=\"1.8cm\"/></style:style>"
                + "<style:style style:name=\"xtable1.C\" style:family=\"table-column\"><style:table-column-properties style:column-width=\"7.2cm\"/></style:style>"
                + "<style:style style:name=\"xtable1.D\" style:family=\"table-column\"><style:table-column-properties style:column-width=\"2.4cm\"/></style:style>"
                + "<style:style style:name=\"xtable1.E\" style:family=\"table-column\"><style:table-column-properties style:column-width=\"1.8cm\"/></style:style>"
                + "<style:style style:name=\"xtable1.F\" style:family=\"table-column\"><style:table-column-properties style:column-width=\"2.6cm\"/></style:style>"
                + "<style:style style:name=\"xtable1.H\" style:family=\"table-cell\"><style:table-cell-properties fo:padding=\"0.1cm\" fo:border-top=\"0.05pt solid #000000\" fo:border-bottom=\"0.05pt solid #000000\"/></style:style>"
                + "<style:style style:name=\"xtable1.Cell\" style:family=\"table-cell\"><style:table-cell-properties fo:padding=\"0.08cm\" fo:border=\"none\"/></style:style>"
                + extraAutoStyles
                + "</office:automatic-styles>"
                + body
                + "</office:document-content>";

        String manifestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<manifest:manifest xmlns:manifest=\"urn:oasis:names:tc:opendocument:xmlns:manifest:1.0\" manifest:version=\"1.2\">"
                + "<manifest:file-entry manifest:media-type=\"application/vnd.oasis.opendocument.text\" manifest:version=\"1.2\" manifest:full-path=\"/\"/>"
                + "<manifest:file-entry manifest:media-type=\"text/xml\" manifest:full-path=\"content.xml\"/>"
                + "<manifest:file-entry manifest:media-type=\"text/xml\" manifest:full-path=\"styles.xml\"/>"
                + "</manifest:manifest>";

        byte[] mime = "application/vnd.oasis.opendocument.text".getBytes(StandardCharsets.UTF_8);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (ZipOutputStream zout = new ZipOutputStream(out)) {
            ZipEntry m = new ZipEntry("mimetype");
            m.setMethod(ZipEntry.STORED);
            m.setSize(mime.length);
            CRC32 crc = new CRC32();
            crc.update(mime);
            m.setCrc(crc.getValue());
            zout.putNextEntry(m);
            zout.write(mime);
            zout.closeEntry();

            writeZipEntry(zout, "META-INF/manifest.xml", manifestXml);
            writeZipEntry(zout, "styles.xml", stylesXml);
            writeZipEntry(zout, "content.xml", contentXml);
        }
        return out.toByteArray();
    }

    private static String buildPositionTables(boolean english, String customHeaderRow) {
        String cols = "<table:table-column table:style-name=\"xtable1.A\"/>"
                + "<table:table-column table:style-name=\"xtable1.B\"/>"
                + "<table:table-column table:style-name=\"xtable1.C\"/>"
                + "<table:table-column table:style-name=\"xtable1.D\"/>"
                + "<table:table-column table:style-name=\"xtable1.E\"/>"
                + "<table:table-column table:style-name=\"xtable1.F\"/>";
        String headerRow;
        if (customHeaderRow != null) {
            headerRow = customHeaderRow;
        } else {
            String[] labels = english
                    ? new String[]{"Pos.", "Qty", "Description", "Unit", "VAT %", "Total"}
                    : new String[]{"Pos.", "Menge", "Bezeichnung", "Einzel", "USt %", "Gesamt"};
            StringBuilder hr = new StringBuilder("<table:table-row>");
            for (String lbl : labels) {
                hr.append("<table:table-cell table:style-name=\"xtable1.H\" office:value-type=\"string\"><text:p text:style-name=\"Bold\">")
                        .append(lbl).append("</text:p></table:table-cell>");
            }
            hr.append("</table:table-row>");
            headerRow = hr.toString();
        }
        // Header table name must NOT start with "xtable" (YabsODTContentHandler matches prefix "xtable")
        StringBuilder sb = new StringBuilder();
        sb.append("<table:table table:name=\"header_table1\" table:style-name=\"xtable1\">")
                .append(cols).append(headerRow).append("</table:table>");
        sb.append("<table:table table:name=\"xtable1\" table:style-name=\"xtable1\">")
                .append(cols).append("<table:table-row>");
        for (int i = 0; i < 6; i++) {
            sb.append("<table:table-cell table:style-name=\"xtable1.Cell\" office:value-type=\"string\"><text:p text:style-name=\"Standard\"/></table:table-cell>");
        }
        sb.append("</table:table-row></table:table>");
        return sb.toString();
    }

    private static void writeZipEntry(ZipOutputStream zout, String name, String content) throws IOException {
        zout.putNextEntry(new ZipEntry(name));
        zout.write(content.getBytes(StandardCharsets.UTF_8));
        zout.closeEntry();
    }

    private static String baseName(String path) {
        int slash = Math.max(path.lastIndexOf('/'), path.lastIndexOf('\\'));
        return slash >= 0 ? path.substring(slash + 1) : path;
    }

    private static String normalize(String text) {
        String lower = text.toLowerCase(Locale.ROOT)
                .replace("\u00e4", "ae")
                .replace("\u00f6", "oe")
                .replace("\u00fc", "ue")
                .replace("\u00df", "ss");
        String decomposed = Normalizer.normalize(lower, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return decomposed;
    }

    private static boolean containsAny(String haystack, String... needles) {
        for (String n : needles) {
            if (haystack.contains(n)) {
                return true;
            }
        }
        return false;
    }
}
