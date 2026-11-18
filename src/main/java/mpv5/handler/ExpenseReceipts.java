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
import java.io.OutputStream;
import java.math.RoundingMode;
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
import java.util.List;
import java.util.Locale;
import mpv5.Main;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.db.common.QueryHandler;
import mpv5.db.common.ReturnValue;
import mpv5.db.objects.Expense;
import mpv5.db.objects.User;
import mpv5.globals.LocalSettings;
import mpv5.logging.Log;
import mpv5.usermanagement.MPSecurityManager;
import mpv5.utils.date.DateConverter;

/**
 * Manages receipts (scanned vouchers, PDF invoices, XML e-invoices, photos)
 * attached to expenses (GoBD Rz. 61–64, "Keine Buchung ohne Beleg").
 * <p>
 * Storage design:
 * <ul>
 *   <li>The files are NOT stored inside the database (no large BLOBs in H2/Derby/MySQL).</li>
 *   <li>Each imported receipt is stored byte-identical in a folder per year
 *       ({@code <receipts-dir>/<year>/<hash-prefix>_<filename>}) and set to read-only.</li>
 *   <li>Alongside the files an append-only {@code SHA256SUMS} manifest is updated
 *       in the standard format {@code <sha256>  <filename>}, easily verified via
 *       {@code sha256sum -c SHA256SUMS}.</li>
 *   <li>The database table {@code expense_receipts} holds only metadata, reference,
 *       file path and the cryptographic SHA-256 checksum.</li>
 *   <li>The root receipts directory supports path variables: {@code [HOME]},
 *       {@code [APPDIR]}, {@code [DBNAME]}, {@code [YEAR]}, {@code [MONTH]}, {@code [DAY]}.
 *       Default: {@code [APPDIR]/receipts}.</li>
 * </ul>
 */
public final class ExpenseReceipts {

    public static final String KEY_DIR = "expensereceiptsdir";
    public static final String DEFAULT_DIR = "[APPDIR]/receipts";
    public static final String VARIABLES = "[HOME] [APPDIR] [DBNAME] [YEAR] [MONTH] [DAY]";

    private ExpenseReceipts() {
    }

    /**
     * One receipt record
     */
    public static final class Receipt {
        public final int id;
        public final int expenseId;
        public final String originalName;
        public final String archivePath;
        public final long fileSize;
        public final String sha256;
        public final String mimeType;
        public final Date importedAt;
        public final String importedBy;

        Receipt(int id, int expenseId, String originalName, String archivePath,
                long fileSize, String sha256, String mimeType, Date importedAt, String importedBy) {
            this.id = id;
            this.expenseId = expenseId;
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

    /**
     * Resolves the configured pattern using the given date and database name.
     */
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

    /**
     * @return the configured receipts folder pattern
     */
    public static String folderPattern() {
        return LocalSettings.hasProperty(KEY_DIR) ? LocalSettings.getProperty(KEY_DIR) : DEFAULT_DIR;
    }

    /**
     * Sets the receipts folder pattern in local settings.
     */
    public static void setFolderPattern(String pattern) {
        LocalSettings.setProperty(KEY_DIR, pattern == null ? DEFAULT_DIR : pattern.trim());
    }

    /**
     * @return the current root receipts folder
     */
    public static File rootFolder() {
        String dbname = LocalSettings.getProperty(LocalSettings.DBNAME);
        return resolve(folderPattern(), dbname, new Date());
    }

    /**
     * Computes the SHA-256 of the given file.
     */
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
        return QueryHandler.instanceOf().clone(Context.getExpense());
    }

    private static volatile boolean tableChecked = false;

    /**
     * Ensures the {@code expense_receipts} table exists across all supported databases.
     */
    public static synchronized void ensureTable() {
        if (tableChecked) {
            return;
        }
        try {
            db().freeQuery(
                    "CREATE TABLE IF NOT EXISTS expense_receipts ("
                    + "ids BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY, "
                    + "expensesids BIGINT NOT NULL, "
                    + "originalname VARCHAR(500) NOT NULL, "
                    + "archivepath VARCHAR(1000) NOT NULL, "
                    + "filesize BIGINT NOT NULL, "
                    + "sha256 CHAR(64) NOT NULL, "
                    + "mimetype VARCHAR(100) NULL, "
                    + "imported_at TIMESTAMP NOT NULL, "
                    + "imported_by VARCHAR(100) NOT NULL"
                    + ")",
                    MPSecurityManager.VIEW, null);
            tableChecked = true;
        } catch (Exception e) {
            // Derby syntax fallback
            try {
                db().freeQuery(
                        "CREATE TABLE expense_receipts ("
                        + "ids BIGINT NOT NULL GENERATED BY DEFAULT AS IDENTITY (START WITH 1, INCREMENT BY 1), "
                        + "expensesids BIGINT NOT NULL, "
                        + "originalname VARCHAR(500) NOT NULL, "
                        + "archivepath VARCHAR(1000) NOT NULL, "
                        + "filesize BIGINT NOT NULL, "
                        + "sha256 CHAR(64) NOT NULL, "
                        + "mimetype VARCHAR(100), "
                        + "imported_at TIMESTAMP NOT NULL, "
                        + "imported_by VARCHAR(100) NOT NULL, "
                        + "PRIMARY KEY (ids))",
                        MPSecurityManager.VIEW, null);
                tableChecked = true;
            } catch (Exception ex2) {
                Log.Debug(ExpenseReceipts.class, "expense_receipts table check: " + ex2.getMessage());
            }
        }
    }

    /**
     * @param expenseId
     * @return all receipts linked to the expense, ordered by import time
     */
    public static List<Receipt> of(int expenseId) {
        if (expenseId <= 0) {
            return Collections.emptyList();
        }
        ensureTable();
        List<Receipt> list = new ArrayList<Receipt>();
        try {
            ReturnValue rv = db().freeSelectQuery(
                    "SELECT ids, expensesids, originalname, archivepath, filesize, sha256, mimetype, imported_at, imported_by "
                    + "FROM expense_receipts WHERE expensesids = " + expenseId + " ORDER BY ids ASC",
                    MPSecurityManager.VIEW, null);
            if (rv != null && rv.hasData()) {
                for (Object[] row : rv.getData()) {
                    int id = Integer.parseInt(String.valueOf(row[0]));
                    int expId = Integer.parseInt(String.valueOf(row[1]));
                    String orig = row[2] != null ? String.valueOf(row[2]) : "";
                    String path = row[3] != null ? String.valueOf(row[3]) : "";
                    long size = row[4] != null ? Long.parseLong(String.valueOf(row[4])) : 0L;
                    String hash = row[5] != null ? String.valueOf(row[5]) : "";
                    String mime = row[6] != null ? String.valueOf(row[6]) : "";
                    Date at = date(row[7]);
                    String by = row[8] != null ? String.valueOf(row[8]) : "";
                    list.add(new Receipt(id, expId, orig, path, size, hash, mime, at, by));
                }
            }
        } catch (Exception e) {
            Log.Debug(ExpenseReceipts.class, e);
        }
        return list;
    }

    /**
     * Checks whether a receipt with the given SHA-256 checksum is already
     * recorded in {@code expense_receipts}.
     *
     * @param sha256 64-character hex SHA-256 checksum
     * @return true if already imported
     */
    public static boolean existsBySha256(String sha256) {
        if (sha256 == null || sha256.trim().isEmpty()) {
            return false;
        }
        ensureTable();
        try {
            ReturnValue rv = db().freeSelectQuery(
                    "SELECT ids FROM expense_receipts WHERE sha256 = " + q(sha256.trim().toLowerCase(Locale.ROOT)),
                    MPSecurityManager.VIEW, null);
            return rv != null && rv.hasData() && rv.getData() != null && rv.getData().length > 0;
        } catch (Exception e) {
            Log.Debug(ExpenseReceipts.class, e);
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
        String lower = name == null ? "" : name.toLowerCase();
        if (lower.endsWith(".pdf")) return "application/pdf";
        if (lower.endsWith(".xml")) return "application/xml";
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
        if (lower.endsWith(".tif") || lower.endsWith(".tiff")) return "image/tiff";
        if (lower.endsWith(".txt")) return "text/plain";
        return "application/octet-stream";
    }

    /**
     * Imports a receipt file for the given expense:
     * <ul>
     *   <li>Copies the file to {@code <receipts-dir>/<year>/<hash-prefix>_<sanitized-name>}</li>
     *   <li>Appends a line to {@code <receipts-dir>/<year>/SHA256SUMS}</li>
     *   <li>Inserts a row into {@code expense_receipts}</li>
     *   <li>Sets the archived copy to read-only</li>
     * </ul>
     *
     * @param expense the existing, saved expense
     * @param source the file to import
     * @return the created receipt record, or null on failure
     */
    public static Receipt attach(Expense expense, File source) throws Exception {
        if (expense == null || expense.__getIDS() <= 0) {
            throw new IllegalArgumentException("Expense must be saved before attaching receipts.");
        }
        if (source == null || !source.isFile()) {
            throw new IllegalArgumentException("Receipt source file does not exist: " + source);
        }

        ensureTable();

        String sha = sha256(source);
        long size = source.length();
        Date now = new Date();
        int year = Integer.parseInt(new SimpleDateFormat("yyyy").format(now));

        File root = rootFolder();
        File yearDir = new File(root, String.valueOf(year));
        if (!yearDir.exists() && !yearDir.mkdirs()) {
            throw new IllegalStateException("Could not create receipt directory: " + yearDir);
        }

        String origName = source.getName();
        String safeName = origName.replaceAll("[^a-zA-Z0-9._-]", "_");
        String prefix = sha.substring(0, Math.min(10, sha.length()));
        String destName = prefix + "_" + safeName;
        File destFile = new File(yearDir, destName);

        // If not already archived, copy byte-identical
        if (!destFile.exists()) {
            Files.copy(source.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            try {
                destFile.setReadOnly();
            } catch (Exception ignored) {
            }
        }

        // Append to SHA256SUMS (standard format: <hash>  <filename>)
        File sumsFile = new File(yearDir, "SHA256SUMS");
        String sumLine = sha + "  " + destName + "\n";
        synchronized (ExpenseReceipts.class) {
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
        String sql = "INSERT INTO expense_receipts "
                + "(expensesids, originalname, archivepath, filesize, sha256, mimetype, imported_at, imported_by) "
                + "VALUES ("
                + expense.__getIDS() + ", "
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

        List<Receipt> current = of(expense.__getIDS());
        return current.isEmpty() ? null : current.get(current.size() - 1);
    }

    /**
     * Removes the receipt record from the database. Note: the physical file
     * remains in the archive folder with its SHA256SUMS entry so audit trails
     * and external backups are never altered.
     */
    public static boolean detach(Expense expense, int receiptId) {
        if (expense == null || expense.__getIDS() <= 0 || receiptId <= 0) {
            return false;
        }
        ensureTable();
        ReturnValue rv = db().freeUpdateQuery(
                "DELETE FROM expense_receipts WHERE ids = " + receiptId + " AND expensesids = " + expense.__getIDS(),
                MPSecurityManager.RIGHT_TO_CREATE_OR_DELETE, null);
        return rv != null;
    }

    /**
     * Verifies the receipt against its archived file.
     *
     * @return true if the file exists on disk and its SHA-256 matches the database
     */
    public static boolean verify(Receipt r) {
        if (r == null || !r.exists()) {
            return false;
        }
        try {
            String actual = sha256(r.file());
            return r.sha256 != null && r.sha256.equalsIgnoreCase(actual);
        } catch (Exception e) {
            Log.Debug(ExpenseReceipts.class, e);
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
