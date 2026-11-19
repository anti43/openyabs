/*
 *  This file is part of YaBS.
 *
 *  YaBS is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  YaBS is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with YaBS.  If not, see <http://www.gnu.org/licenses/>.
 */
package mpv5.handler;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import mpv5.Main;
import mpv5.db.common.Context;
import mpv5.db.common.QueryHandler;
import mpv5.db.common.ReturnValue;
import mpv5.db.objects.Revenue;
import mpv5.db.objects.User;
import mpv5.globals.LocalSettings;
import mpv5.logging.Log;
import mpv5.usermanagement.MPSecurityManager;
import mpv5.utils.date.DateConverter;

/**
 * Manages receipts (incoming invoices, credit advice, ZUGFeRD PDF/A-3,
 * XRechnung XML, bank vouchers, scans, photos) attached to revenues ({@link Revenue}).
 * <p>
 * Storage design:
 * <ul>
 *   <li>The files are NOT stored inside the database (no large BLOBs).</li>
 *   <li>Each imported receipt is stored byte-identical in a folder per year
 *       ({@code <receipts-dir>/einnahmen/<year>/<hash-prefix>_<filename>}) and set to read-only.</li>
 *   <li>Alongside the files an append-only {@code SHA256SUMS} manifest is maintained.</li>
 *   <li>The database table {@code revenue_receipts} holds only metadata, reference,
 *       file path and the cryptographic SHA-256 checksum.</li>
 *   <li>Also seamlessly surfaces files locked via {@code gobd_income_lock} if present.</li>
 * </ul>
 */
public final class RevenueReceipts {

    public static final String KEY_DIR = "revenuereceiptsdir";
    public static final String DEFAULT_DIR = "[APPDIR]/receipts";
    public static final String VARIABLES = "[HOME] [APPDIR] [DBNAME] [YEAR] [MONTH] [DAY]";

    private RevenueReceipts() {
    }

    /**
     * One receipt record
     */
    public static final class Receipt {
        public final int id;
        public final int revenueId;
        public final String originalName;
        public final String archivePath;
        public final long fileSize;
        public final String sha256;
        public final String mimeType;
        public final Date importedAt;
        public final String importedBy;

        public Receipt(int id, int revenueId, String originalName, String archivePath,
                long fileSize, String sha256, String mimeType, Date importedAt, String importedBy) {
            this.id = id;
            this.revenueId = revenueId;
            this.originalName = originalName == null ? "" : originalName;
            this.archivePath = archivePath == null ? "" : archivePath;
            this.fileSize = fileSize;
            this.sha256 = sha256 == null ? "" : sha256;
            this.mimeType = mimeType == null ? "application/octet-stream" : mimeType;
            this.importedAt = importedAt == null ? new Date() : importedAt;
            this.importedBy = importedBy == null ? "" : importedBy;
        }

        public File file() {
            return new File(archivePath);
        }

        public boolean exists() {
            return file().isFile();
        }

        public String formattedSize() {
            if (fileSize < 1024) {
                return fileSize + " B";
            } else if (fileSize < 1024 * 1024) {
                return String.format(Locale.US, "%.1f KB", fileSize / 1024.0);
            } else {
                return String.format(Locale.US, "%.2f MB", fileSize / (1024.0 * 1024.0));
            }
        }
    }

    public static File resolve(String pattern, String dbname, Date now) {
        String p = pattern == null || pattern.trim().isEmpty() || "null".equals(pattern) ? DEFAULT_DIR : pattern.trim();
        Date d = now == null ? new Date() : now;
        p = p.replace("[HOME]", System.getProperty("user.home"))
                .replace("[APPDIR]", Main.MPPATH == null ? System.getProperty("user.home") : Main.MPPATH)
                .replace("[DBNAME]", dbname == null ? "yabs" : dbname)
                .replace("[YEAR]", new SimpleDateFormat("yyyy").format(d))
                .replace("[MONTH]", new SimpleDateFormat("MM").format(d))
                .replace("[DAY]", new SimpleDateFormat("dd").format(d));
        return new File(p);
    }

    public static String folderPattern() {
        return LocalSettings.hasProperty(KEY_DIR) ? LocalSettings.getProperty(KEY_DIR) : DEFAULT_DIR;
    }

    public static void setFolderPattern(String pattern) {
        LocalSettings.setProperty(KEY_DIR, pattern == null ? DEFAULT_DIR : pattern.trim());
    }

    public static File rootFolder() {
        String dbname = LocalSettings.getProperty(LocalSettings.DBNAME);
        return resolve(folderPattern(), dbname, new Date());
    }

    public static String sha256(File f) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        try (InputStream in = Files.newInputStream(f.toPath())) {
            byte[] buf = new byte[8192];
            int n;
            while ((n = in.read(buf)) > 0) {
                md.update(buf, 0, n);
            }
        }
        StringBuilder sb = new StringBuilder(64);
        for (byte b : md.digest()) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private static String q(String s) {
        return s == null ? "NULL" : "'" + s.replace("'", "''") + "'";
    }

    private static QueryHandler db() {
        return QueryHandler.instanceOf().clone(Context.getRevenue());
    }

    /**
     * @param revenueId
     * @return all receipts linked to the revenue, including any gobd_income_lock file
     */
    public static List<Receipt> of(int revenueId) {
        if (revenueId <= 0) {
            return Collections.emptyList();
        }
        List<Receipt> list = new ArrayList<Receipt>();
        Set<String> seenHashes = new HashSet<String>();
        try {
            ReturnValue rv = db().freeSelectQuery(
                    "SELECT ids, revenuesids, originalname, archivepath, filesize, sha256, mimetype, imported_at, imported_by "
                    + "FROM revenue_receipts WHERE revenuesids = " + revenueId + " ORDER BY ids ASC",
                    MPSecurityManager.VIEW, null);
            if (rv != null && rv.hasData()) {
                for (Object[] row : rv.getData()) {
                    int id = Integer.parseInt(String.valueOf(row[0]));
                    int revId = Integer.parseInt(String.valueOf(row[1]));
                    String orig = row[2] != null ? String.valueOf(row[2]) : "";
                    String path = row[3] != null ? String.valueOf(row[3]) : "";
                    long size = row[4] != null ? Long.parseLong(String.valueOf(row[4])) : 0L;
                    String hash = row[5] != null ? String.valueOf(row[5]) : "";
                    String mime = row[6] != null ? String.valueOf(row[6]) : "";
                    Date at = date(row[7]);
                    String by = row[8] != null ? String.valueOf(row[8]) : "";
                    list.add(new Receipt(id, revId, orig, path, size, hash, mime, at, by));
                    if (!hash.isEmpty()) {
                        seenHashes.add(hash.toLowerCase(Locale.ROOT));
                    }
                }
            }
        } catch (Exception e) {
            Log.Debug(RevenueReceipts.class, e);
        }

        // Also check if gobd_income_lock has an archived file for this revenue
        try {
            ReturnValue rvLock = db().freeSelectQuery(
                    "SELECT revenue_id, original_name, archive_path, file_size, file_sha256, artifact_format, imported_at, imported_by "
                    + "FROM gobd_income_lock WHERE revenue_id = " + revenueId,
                    MPSecurityManager.VIEW, null);
            if (rvLock != null && rvLock.hasData()) {
                for (Object[] row : rvLock.getData()) {
                    int revId = Integer.parseInt(String.valueOf(row[0]));
                    String orig = row[1] != null ? String.valueOf(row[1]) : "";
                    String path = row[2] != null ? String.valueOf(row[2]) : "";
                    long size = row[3] != null ? Long.parseLong(String.valueOf(row[3])) : 0L;
                    String hash = row[4] != null ? String.valueOf(row[4]) : "";
                    String format = row[5] != null ? String.valueOf(row[5]) : "";
                    Date at = date(row[6]);
                    String by = row[7] != null ? String.valueOf(row[7]) : "";
                    if (!hash.isEmpty() && seenHashes.add(hash.toLowerCase(Locale.ROOT))) {
                        String mime = detectMime(orig.isEmpty() ? path : orig);
                        list.add(new Receipt(-revId, revId, orig.isEmpty() ? "GoBD-" + format : orig, path, size, hash, mime, at, by));
                    }
                }
            }
        } catch (Exception ignored) {
            // gobd_income_lock might not exist yet if plugin not installed
        }

        return list;
    }

    public static boolean existsBySha256(String sha256) {
        if (sha256 == null || sha256.trim().isEmpty()) {
            return false;
        }
        try {
            ReturnValue rv = db().freeSelectQuery(
                    "SELECT ids FROM revenue_receipts WHERE sha256 = " + q(sha256.trim().toLowerCase(Locale.ROOT)),
                    MPSecurityManager.VIEW, null);
            if (rv != null && rv.hasData() && rv.getData() != null && rv.getData().length > 0) {
                return true;
            }
        } catch (Exception e) {
            Log.Debug(RevenueReceipts.class, e);
        }
        try {
            ReturnValue rv2 = db().freeSelectQuery(
                    "SELECT revenue_id FROM gobd_income_lock WHERE file_sha256 = " + q(sha256.trim().toLowerCase(Locale.ROOT)),
                    MPSecurityManager.VIEW, null);
            return rv2 != null && rv2.hasData() && rv2.getData() != null && rv2.getData().length > 0;
        } catch (Exception ignored) {
            return false;
        }
    }

    private static Date date(Object o) {
        if (o instanceof Date) {
            return (Date) o;
        }
        Date d = DateConverter.getDate(String.valueOf(o));
        return d == null ? new Date() : d;
    }

    private static String detectMime(String name) {
        String lower = name == null ? "" : name.toLowerCase(Locale.ROOT);
        if (lower.endsWith(".pdf")) return "application/pdf";
        if (lower.endsWith(".xml")) return "application/xml";
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
        if (lower.endsWith(".tif") || lower.endsWith(".tiff")) return "image/tiff";
        if (lower.endsWith(".txt")) return "text/plain";
        return "application/octet-stream";
    }

    /**
     * Imports a receipt file for the given revenue:
     * <ul>
     *   <li>Copies the file to {@code <receipts-dir>/einnahmen/<year>/<hash-prefix>_<sanitized-name>}</li>
     *   <li>Appends a line to {@code <receipts-dir>/einnahmen/<year>/SHA256SUMS}</li>
     *   <li>Inserts a row into {@code revenue_receipts}</li>
     *   <li>Sets the archived copy to read-only</li>
     * </ul>
     *
     * @param revenue the existing, saved revenue
     * @param source the file to import
     * @return the created receipt record, or null on failure
     */
    public static Receipt attach(Revenue revenue, File source) throws Exception {
        if (revenue == null || revenue.__getIDS() <= 0) {
            throw new IllegalArgumentException("Revenue must be saved before attaching receipts.");
        }
        if (source == null || !source.isFile()) {
            throw new IllegalArgumentException("Receipt source file does not exist: " + source);
        }


        String sha = sha256(source);
        long size = source.length();
        Date now = new Date();
        int year = Integer.parseInt(new SimpleDateFormat("yyyy").format(now));

        File root = rootFolder();
        File yearDir = new File(new File(root, "einnahmen"), String.valueOf(year));
        if (!yearDir.exists() && !yearDir.mkdirs()) {
            // fallback without einnahmen subfolder
            yearDir = new File(root, String.valueOf(year));
            if (!yearDir.exists() && !yearDir.mkdirs()) {
                throw new IllegalStateException("Could not create receipt directory: " + yearDir);
            }
        }

        String origName = source.getName();
        String safeName = origName.replaceAll("[^a-zA-Z0-9._-]", "_");
        String prefix = sha.substring(0, Math.min(10, sha.length()));
        String destName = prefix + "_" + safeName;
        File destFile = new File(yearDir, destName);

        if (!destFile.exists()) {
            Files.copy(source.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            try {
                destFile.setReadOnly();
            } catch (Exception ignored) {
            }
        }

        File sumsFile = new File(yearDir, "SHA256SUMS");
        String sumLine = sha + "  " + destName + "\n";
        synchronized (RevenueReceipts.class) {
            boolean alreadyLogged = false;
            if (sumsFile.exists()) {
                List<String> lines = Files.readAllLines(sumsFile.toPath(), StandardCharsets.UTF_8);
                for (String l : lines) {
                    if (l.startsWith(sha)) {
                        alreadyLogged = true;
                        break;
                    }
                }
            }
            if (!alreadyLogged) {
                Files.write(sumsFile.toPath(), sumLine.getBytes(StandardCharsets.UTF_8),
                        StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            }
        }

        String user = currentUser();
        String mime = detectMime(origName);
        String sql = "INSERT INTO revenue_receipts "
                + "(revenuesids, originalname, archivepath, filesize, sha256, mimetype, imported_at, imported_by) "
                + "VALUES ("
                + revenue.__getIDS() + ", "
                + q(origName) + ", "
                + q(destFile.getAbsolutePath()) + ", "
                + size + ", "
                + q(sha) + ", "
                + q(mime) + ", "
                + q(DateConverter.getSQLDateString(now)) + ", "
                + q(user)
                + ")";

        ReturnValue rv = db().freeUpdateQuery(sql, MPSecurityManager.RIGHT_TO_CREATE_OR_DELETE, null);
        if (rv == null) {
            throw new IllegalStateException("Failed to insert receipt record into database.");
        }

        // activity log: archived file incl. its path (also written to the activity journal)
        mpv5.handler.Activity.file(destFile, revenue);

        List<Receipt> current = of(revenue.__getIDS());
        return current.isEmpty() ? null : current.get(current.size() - 1);
    }

    public static boolean detach(Revenue revenue, int receiptId) {
        if (revenue == null || revenue.__getIDS() <= 0 || receiptId <= 0) {
            return false;
        }
        ReturnValue rv = db().freeUpdateQuery(
                "DELETE FROM revenue_receipts WHERE ids = " + receiptId + " AND revenuesids = " + revenue.__getIDS(),
                MPSecurityManager.RIGHT_TO_CREATE_OR_DELETE, null);
        return rv != null;
    }

    public static boolean verify(Receipt r) {
        if (r == null || !r.exists()) {
            return false;
        }
        try {
            String actual = sha256(r.file());
            return r.sha256 != null && r.sha256.equalsIgnoreCase(actual);
        } catch (Exception e) {
            Log.Debug(RevenueReceipts.class, e);
            return false;
        }
    }

    private static String currentUser() {
        try {
            User u = User.getCurrentUser();
            return u != null ? u.__getCname() : System.getProperty("user.name", "system");
        } catch (Exception ignored) {
            return System.getProperty("user.name", "system");
        }
    }
}
