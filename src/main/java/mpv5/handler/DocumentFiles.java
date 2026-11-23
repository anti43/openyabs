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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import mpv5.db.common.DatabaseObject;
import mpv5.db.common.QueryHandler;
import mpv5.db.common.ReturnValue;
import mpv5.db.objects.Item;
import mpv5.db.objects.User;
import mpv5.globals.Constants;
import mpv5.globals.LocalSettings;
import mpv5.logging.Log;
import mpv5.usermanagement.MPSecurityManager;
import mpv5.utils.date.DateConverter;

/**
 * Checksums of the files YaBS writes for invoices (PDF, XRechnung,
 * ZUGFeRD): path, size and SHA-256 in the table {@code item_files}, so the
 * files can be verified later. Temporary files (previews in the cache or temp
 * folder) are not recorded.
 */
public final class DocumentFiles {

    /**
     * A stored file and its state now
     */
    public static final class Entry {

        public final String path;
        public final long size;
        public final String sha256;
        public final Date created;
        /**
         * OK, CHANGED or MISSING
         */
        public final String state;

        Entry(String path, long size, String sha256, Date created, String state) {
            this.path = path;
            this.size = size;
            this.sha256 = sha256;
            this.created = created;
            this.state = state;
        }

        public boolean ok() {
            return "OK".equals(state);
        }
    }

    private DocumentFiles() {
    }

    /**
     * @param o
     * @return true for invoices, credit notes, deposit and part payment
     * invoices
     */
    public static boolean isInvoice(DatabaseObject o) {
        if (!(o instanceof Item)) {
            return false;
        }
        int t = ((Item) o).__getInttype();
        return t == Constants.TYPE_INVOICE || t == Constants.TYPE_CREDIT || t == Constants.TYPE_DEPOSIT || t == Constants.TYPE_PART_PAYMENT;
    }

    /**
     * @return true if the file lies in the cache or temp folder (a preview, a
     * file before it is copied to its place)
     */
    public static boolean isTemporary(File f) {
        if (f == null) {
            return false;
        }
        try {
            String p = f.getCanonicalPath();
            String cacheDir = LocalSettings.hasProperty(LocalSettings.CACHE_DIR)
                    ? LocalSettings.getProperty(LocalSettings.CACHE_DIR)
                    : Constants.FALLBACK_CACHE_DIR;
            for (String dir : new String[]{cacheDir, Constants.FALLBACK_CACHE_DIR}) {
                if (dir != null && !dir.trim().isEmpty() && !"null".equals(dir.trim())) {
                    String cp = new File(dir.trim()).getCanonicalPath();
                    if (p.equals(cp) || p.startsWith(cp + File.separator)) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            Log.Debug(DocumentFiles.class, e);
        }
        return false;
    }

    /**
     * Records the checksum of a file written for an invoice; other records,
     * temporary and missing files are ignored. Never throws.
     *
     * @param owner the invoice
     * @param f the written file
     */
    public static void record(DatabaseObject owner, File f) {
        if (!isInvoice(owner) || owner.__getIDS() <= 0 || f == null || !f.isFile() || isTemporary(f)) {
            return;
        }
        try {
            String sha = ExpenseReceipts.sha256(f);
            String path = f.getAbsolutePath();
            //the same file written again: the new checksum replaces the old one
            QueryHandler.getConnection().freeUpdateQuery("DELETE FROM item_files WHERE itemsids = " + owner.__getIDS()
                    + " AND filepath = " + q(path), MPSecurityManager.RIGHT_TO_CREATE_OR_DELETE, null);
            String user = "";
            try {
                user = User.getCurrentUser().__getCname();
            } catch (Exception e) {
                //no user
            }
            QueryHandler.getConnection().freeUpdateQuery("INSERT INTO item_files (itemsids, filename, filepath, filesize, sha256, created_at, created_by) VALUES ("
                    + owner.__getIDS() + ", " + q(cut(f.getName(), 500)) + ", " + q(cut(path, 1000)) + ", " + f.length() + ", " + q(sha) + ", "
                    + q(DateConverter.getSQLDateString(new Date())) + ", " + q(cut(user, 100)) + ")",
                    MPSecurityManager.RIGHT_TO_CREATE_OR_DELETE, null);
        } catch (Exception e) {
            Log.Debug(DocumentFiles.class, e);
        }
    }

    /**
     * @param invoice
     * @return the stored files of the invoice, each checked against its
     * checksum
     */
    public static List<Entry> verify(DatabaseObject invoice) {
        List<Entry> l = new ArrayList<Entry>();
        if (invoice == null || invoice.__getIDS() <= 0) {
            return l;
        }
        try {
            ReturnValue rv = QueryHandler.getConnection().freeQuery("SELECT filepath, filesize, sha256, created_at FROM item_files WHERE itemsids = "
                    + invoice.__getIDS() + " ORDER BY ids", MPSecurityManager.VIEW, null);
            if (rv == null || !rv.hasData()) {
                return l;
            }
            for (Object[] r : rv.getData()) {
                String path = String.valueOf(r[0]);
                String sha = String.valueOf(r[2]).trim().toLowerCase(Locale.ROOT);
                File f = new File(path);
                String state;
                if (!f.isFile()) {
                    state = "MISSING";
                } else {
                    state = sha.equals(ExpenseReceipts.sha256(f)) ? "OK" : "CHANGED";
                }
                Date created = r[3] instanceof Date ? (Date) r[3] : DateConverter.getDate(String.valueOf(r[3]));
                l.add(new Entry(path, Long.parseLong(String.valueOf(r[1])), sha, created, state));
            }
        } catch (Exception e) {
            Log.Debug(DocumentFiles.class, e);
        }
        return l;
    }

    private static String q(String s) {
        return "'" + (s == null ? "" : s.replace("'", "''")) + "'";
    }

    private static String cut(String s, int max) {
        return s == null ? "" : s.length() > max ? s.substring(0, max) : s;
    }
}
