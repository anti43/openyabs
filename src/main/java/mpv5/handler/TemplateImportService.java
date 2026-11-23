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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
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
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
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
