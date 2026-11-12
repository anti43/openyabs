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
package mpv5.db.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import mpv5.Main;
import mpv5.data.PropertyStore;
import mpv5.globals.LocalSettings;
import mpv5.logging.Log;

/**
 * Zip backups of a local database (H2 or Derby): all database connections are
 * closed, the database files are zipped into the backup folder
 * (&lt;name&gt;-&lt;yyyyMMdd-HHmmss&gt;.zip) and the connection is opened again.
 * Only the newest backups are kept. A backup is restored by unzipping it next
 * to the zip and adding a new connection to it.
 */
public final class DatabaseBackup {

    public static final String KEY_DIR = "zipbackupdir";
    public static final String KEY_ON_EXIT = "zipbackuponexit";
    public static final String KEY_KEEP = "zipbackupkeep";
    public static final String DEFAULT_DIR = "[APPDIR]/backups/[DBNAME]";
    public static final int DEFAULT_KEEP = 3;
    public static final String VARIABLES = "[HOME] [APPDIR] [DBNAME] [YEAR] [MONTH] [DAY]";
    private static final String STAMP = "yyyyMMdd-HHmmss";

    private DatabaseBackup() {
    }

    /**
     * @return true if the current database is a local H2 or Derby database
     */
    public static boolean isPossible() {
        return ConnectionTypeHandler.isDerbyLike() && !ConnectionTypeHandler.DEVMODE && !sources(dbPath(), dbName(), isH2()).isEmpty();
    }

    private static boolean isH2() {
        return ConnectionTypeHandler.getDriverType() == ConnectionTypeHandler.H2;
    }

    private static String dbPath() {
        return LocalSettings.getProperty(LocalSettings.DBPATH);
    }

    private static String dbName() {
        return LocalSettings.getProperty(LocalSettings.DBNAME);
    }

    /**
     * @return the configured folder pattern, or the default
     */
    public static String folderPattern() {
        return LocalSettings.hasProperty(KEY_DIR) ? LocalSettings.getProperty(KEY_DIR) : DEFAULT_DIR;
    }

    /**
     * @return how many backups are kept
     */
    public static int keep() {
        try {
            return Math.max(1, Integer.parseInt(LocalSettings.getProperty(KEY_KEEP).trim()));
        } catch (Exception e) {
            return DEFAULT_KEEP;
        }
    }

    public static boolean onExit() {
        return "true".equals(LocalSettings.getProperty(KEY_ON_EXIT));
    }

    /**
     * Replaces the variables of a folder pattern
     *
     * @param pattern e.g. [HOME]/Sicherungen/[DBNAME]/[YEAR]
     * @param dbname
     * @param now
     * @return the folder
     */
    public static File resolve(String pattern, String dbname, Date now) {
        String p = pattern == null || pattern.trim().isEmpty() || "null".equals(pattern) ? DEFAULT_DIR : pattern.trim();
        p = p.replace("[HOME]", System.getProperty("user.home"))
                .replace("[APPDIR]", Main.MPPATH == null ? System.getProperty("user.home") : Main.MPPATH)
                .replace("[DBNAME]", dbname == null ? "yabs" : dbname)
                .replace("[YEAR]", new SimpleDateFormat("yyyy").format(now))
                .replace("[MONTH]", new SimpleDateFormat("MM").format(now))
                .replace("[DAY]", new SimpleDateFormat("dd").format(now));
        return new File(p);
    }

    /**
     * @return the current backup folder
     */
    public static File folder() {
        return resolve(folderPattern(), dbName(), new Date());
    }

    /**
     * @param path the folder of the database
     * @param name the database name
     * @param h2
     * @return the files of the database (H2: name.mv.db, Derby: the folder name)
     */
    static List<File> sources(String path, String name, boolean h2) {
        List<File> l = new ArrayList<File>();
        if (path == null || name == null || "null".equals(path)) {
            return l;
        }
        if (h2) {
            File f = new File(path, name + ".mv.db");
            if (f.isFile()) {
                l.add(f);
            }
        } else {
            File f = new File(path, name);
            if (f.isDirectory()) {
                l.add(f);
            }
        }
        return l;
    }

    /**
     * Closes the database, zips it and opens it again (if reconnect)
     *
     * @param reconnect open the connection again
     * @return the zip
     * @throws Exception
     */
    public static synchronized File backup(boolean reconnect) throws Exception {
        String name = dbName();
        List<File> files = sources(dbPath(), name, isH2());
        if (files.isEmpty()) {
            throw new IOException("No local database: " + dbPath() + File.separator + name);
        }
        File dir = folder();
        if (!dir.isDirectory() && !dir.mkdirs()) {
            throw new IOException("Cannot create " + dir);
        }
        File zip = new File(dir, name + "-" + new SimpleDateFormat(STAMP).format(new Date()) + ".zip");
        DatabaseConnection.shutdown();
        try {
            zip(new File(dbPath()), files, zip);
        } finally {
            if (reconnect) {
                DatabaseConnection.instanceOf().reconnect(false);
            }
        }
        prune(dir, name, keep());
        Log.Debug(DatabaseBackup.class, "Backup written: " + zip);
        return zip;
    }

    /**
     * Backup at exit, if switched on (the connection stays closed)
     */
    public static void backupOnExit() {
        try {
            if (onExit() && isPossible()) {
                backup(false);
            }
        } catch (Exception e) {
            Log.Debug(DatabaseBackup.class, e);
        }
    }

    static void zip(File base, List<File> files, File zip) throws IOException {
        File tmp = new File(zip.getPath() + ".part");
        try (ZipOutputStream out = new ZipOutputStream(new FileOutputStream(tmp))) {
            for (File f : files) {
                add(out, base, f);
            }
        }
        Files.move(tmp.toPath(), zip.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
    }

    private static void add(ZipOutputStream out, File base, File f) throws IOException {
        String name = base.toPath().relativize(f.toPath()).toString().replace(File.separatorChar, '/');
        if (f.isDirectory()) {
            out.putNextEntry(new ZipEntry(name + "/"));
            out.closeEntry();
            File[] children = f.listFiles();
            if (children != null) {
                Arrays.sort(children);
                for (File c : children) {
                    //Derby: the lock files are not needed
                    if (!c.getName().equals("db.lck") && !c.getName().equals("dbex.lck")) {
                        add(out, base, c);
                    }
                }
            }
        } else {
            out.putNextEntry(new ZipEntry(name));
            try (InputStream in = new FileInputStream(f)) {
                in.transferTo(out);
            }
            out.closeEntry();
        }
    }

    /**
     * Deletes the oldest backups of the database
     *
     * @param dir
     * @param name
     * @param keep
     * @return how many were deleted
     */
    static int prune(File dir, String name, int keep) {
        File[] zips = dir.listFiles((d, n) -> n.matches(java.util.regex.Pattern.quote(name) + "-\\d{8}-\\d{6}\\.zip"));
        if (zips == null || zips.length <= keep) {
            return 0;
        }
        Arrays.sort(zips, (a, b) -> b.getName().compareTo(a.getName()));
        int deleted = 0;
        for (int i = keep; i < zips.length; i++) {
            if (zips[i].delete()) {
                deleted++;
            }
        }
        return deleted;
    }

    /**
     * The result of unzipping a backup
     */
    public static final class Restored {

        public File folder;
        public String dbname;
        public boolean h2;
        public String stamp;
    }

    /**
     * Unzips a backup into a folder next to it, named like the zip
     *
     * @param zip
     * @return what was found
     * @throws IOException if it is no database backup
     */
    public static Restored unzip(File zip) throws IOException {
        String base = zip.getName().replaceFirst("(?i)\\.zip$", "");
        File target = new File(zip.getParentFile(), base);
        if (target.exists()) {
            target = new File(zip.getParentFile(), base + "-" + System.currentTimeMillis());
        }
        target.mkdirs();
        Restored r = new Restored();
        r.folder = target;
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("(\\d{8}-\\d{6})$").matcher(base);
        r.stamp = m.find() ? m.group(1) : new SimpleDateFormat(STAMP).format(new Date(zip.lastModified()));
        try (ZipInputStream in = new ZipInputStream(new FileInputStream(zip))) {
            for (ZipEntry e = in.getNextEntry(); e != null; e = in.getNextEntry()) {
                File f = new File(target, e.getName()).getCanonicalFile();
                if (!f.toPath().startsWith(target.getCanonicalFile().toPath())) {
                    throw new IOException("Invalid entry " + e.getName());
                }
                if (e.isDirectory()) {
                    f.mkdirs();
                    continue;
                }
                f.getParentFile().mkdirs();
                try (OutputStream out = new FileOutputStream(f)) {
                    in.transferTo(out);
                }
                String n = e.getName();
                if (n.endsWith(".mv.db") && !n.contains("/")) {
                    r.h2 = true;
                    r.dbname = n.substring(0, n.length() - ".mv.db".length());
                } else if (n.endsWith("/service.properties") && n.indexOf('/') == n.lastIndexOf('/') && r.dbname == null) {
                    r.dbname = n.substring(0, n.indexOf('/'));
                }
            }
        }
        if (r.dbname == null) {
            throw new IOException("No YaBS database in " + zip);
        }
        return r;
    }

    /**
     * Unzips a backup and adds a connection to it, named "Backup &lt;stamp&gt;"
     *
     * @param zip
     * @param label the beginning of the connection name
     * @return the id of the new connection
     * @throws Exception
     */
    public static int restore(File zip, String label) throws Exception {
        Restored r = unzip(zip);
        PropertyStore p = new PropertyStore();
        p.addAll(LocalSettings.getPropertyStore().getList().toArray(new String[][]{}));
        p.changeProperty(LocalSettings.DBPATH, r.folder.getAbsolutePath());
        p.changeProperty(LocalSettings.DBNAME, r.dbname);
        p.changeProperty(LocalSettings.DBDRIVER, r.h2 ? ConnectionTypeHandler.H2_DRIVER : ConnectionTypeHandler.DERBY_DRIVER);
        p.changeProperty(LocalSettings.CONNECTION_NAME, label + " " + r.stamp);
        //no backups of the backup into the same folder
        p.changeProperty(KEY_ON_EXIT, "false");
        return LocalSettings.addConnection(p);
    }
}
