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
package mpv5.handler;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import mpv5.db.common.Context;
import mpv5.db.common.QueryHandler;
import mpv5.db.objects.Template;
import mpv5.db.objects.User;
import mpv5.globals.Messages;
import mpv5.logging.Log;
import mpv5.utils.files.FileMonitor;

/**
 * Editing a template file in LibreOffice: the file is written to a fixed,
 * writable working file (folder "templates-edit" in the YaBS folder, with the
 * right extension), opened there and watched. Every save in LibreOffice is
 * stored as the new template file at once, noted in the history and reported
 * to the listeners. A template stays watched while YaBS runs, also when
 * another template is selected.
 */
public final class TemplateEditing {

    /**
     * Told when a new version of a template file was stored (on a background
     * thread)
     */
    public interface Listener {

        void stored(Template template, Date when);
    }

    private static final class Session {

        final Template template;
        final File file;
        final FileMonitor.FileChangeListener watcher;
        byte[] lastHash;
        Date lastStored;

        Session(Template template, File file, FileMonitor.FileChangeListener watcher) {
            this.template = template;
            this.file = file;
            this.watcher = watcher;
        }
    }

    private static final Map<Integer, Session> SESSIONS = new ConcurrentHashMap<Integer, Session>();
    private static final List<Listener> LISTENERS = new CopyOnWriteArrayList<Listener>();
    /**
     * Folder for the working files, null: in the YaBS folder
     */
    private static File folder;

    private TemplateEditing() {
    }

    public static void addListener(Listener l) {
        LISTENERS.add(l);
    }

    public static void removeListener(Listener l) {
        LISTENERS.remove(l);
    }

    /**
     * @param f the folder for the working files (tests), null: in the YaBS
     * folder
     */
    public static void setFolder(File f) {
        folder = f;
    }

    /**
     * @return the folder of the working files (created)
     */
    public static File folder() {
        File f = folder;
        if (f == null) {
            String base = mpv5.Main.MPPATH != null ? mpv5.Main.MPPATH
                    : System.getProperty("user.home") + File.separator + ".yabs";
            f = new File(base, "templates-edit");
        }
        f.mkdirs();
        return f;
    }

    /**
     * @param name the name of the template
     * @param id its id (two templates may have the same name)
     * @param storedName the name of the stored file, for the extension
     * @return e.g. "Rechnung_Standard_12.odt"
     */
    public static String fileName(String name, int id, String storedName) {
        String n = java.text.Normalizer.normalize(name == null ? "" : name, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "").replace("ß", "ss").replaceAll("[^A-Za-z0-9._-]+", "_")
                .replaceAll("^[._]+", "");
        if (n.isEmpty()) {
            n = "Vorlage";
        }
        String ext = "odt";
        String s = storedName == null ? "" : storedName.trim();
        int dot = s.lastIndexOf('.');
        if (dot > 0 && dot < s.length() - 1 && s.length() - dot <= 5) {
            ext = s.substring(dot + 1).toLowerCase(java.util.Locale.ROOT);
        }
        return n + "_" + id + "." + ext;
    }

    /**
     * @param f
     * @return true if LibreOffice has the file open (its lock file exists)
     */
    public static boolean isOpenInOffice(File f) {
        return new File(f.getParentFile(), ".~lock." + f.getName() + "#").exists();
    }

    /**
     * @param template
     * @return true if the template is edited (watched) at the moment
     */
    public static boolean isEditing(Template template) {
        return template != null && template.isExisting() && SESSIONS.containsKey(template.__getIDS());
    }

    /**
     * @return the working file of a template being edited, null if none
     */
    public static File workingFile(Template template) {
        Session s = template == null ? null : SESSIONS.get(template.__getIDS());
        return s == null ? null : s.file;
    }

    /**
     * @return when a change was last taken over, null if none yet
     */
    public static Date lastStored(Template template) {
        Session s = template == null ? null : SESSIONS.get(template.__getIDS());
        return s == null ? null : s.lastStored;
    }

    /**
     * Writes the template file to its working file (unless LibreOffice has it
     * open already) and watches it. The caller opens the file.
     *
     * @param template
     * @return the working file
     * @throws Exception if the template file cannot be read or written
     */
    public static File start(Template template) throws Exception {
        int id = template.__getIDS();
        File work = new File(folder(), fileName(template.__getCname(), id, template.__getFilename()));
        Session old = SESSIONS.get(id);
        if (old != null && old.file.equals(work) && work.exists() && isOpenInOffice(work)) {
            //open in LibreOffice: keep what is there, it is brought to the front
            return work;
        }
        if (!isOpenInOffice(work) || !work.exists()) {
            template.forgetFile();
            Files.copy(template.getFile().toPath(), work.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
        if (!work.canWrite() && !work.setWritable(true)) {
            Log.Debug(TemplateEditing.class, "Cannot make writable: " + work);
        }
        watch(template, work);
        return work;
    }

    private static void watch(Template template, File work) throws Exception {
        stop(template);
        final int id = template.__getIDS();
        FileMonitor.FileChangeListener w = fileName -> changed(id);
        Session s = new Session(template, work, w);
        s.lastHash = hash(work);
        SESSIONS.put(id, s);
        FileMonitor.getInstance().addFileChangeListener(w, work.getPath(), 1000L);
    }

    /**
     * Stops watching the working file of the template (e.g. when it is
     * deleted)
     */
    public static void stop(Template template) {
        Session s = template == null ? null : SESSIONS.remove(template.__getIDS());
        if (s != null) {
            FileMonitor.getInstance().removeFileChangeListener(s.watcher, s.file.getPath());
        }
    }

    private static void changed(int id) {
        Session s = SESSIONS.get(id);
        if (s == null) {
            return;
        }
        synchronized (s) {
            try {
                if (!waitUntilWritten(s.file)) {
                    return;
                }
                byte[] h = hash(s.file);
                if (Arrays.equals(h, s.lastHash)) {
                    return;
                }
                s.lastHash = h;
                Date when = store(s.template, s.file, Messages.TEMPLATES_FILE_CHANGED.toString());
                s.lastStored = when;
                for (Listener l : LISTENERS) {
                    l.stored(s.template, when);
                }
            } catch (Exception e) {
                Log.Debug(TemplateEditing.class, e);
            }
        }
    }

    /**
     * LibreOffice writes the file in steps: wait until it has a size that
     * does not change any more (at most 10 seconds)
     */
    static boolean waitUntilWritten(File f) throws InterruptedException {
        long last = -1;
        for (int i = 0; i < 20; i++) {
            long len = f.exists() ? f.length() : 0;
            if (len > 0 && len == last) {
                return true;
            }
            last = len;
            Thread.sleep(500);
        }
        return false;
    }

    /**
     * Stores a new version of the template file and notes it in the history
     *
     * @param template
     * @param file the new file
     * @param message e.g. "Template file changed: %s"
     * @return when it was stored
     */
    public static Date store(Template template, File file, String message) {
        QueryHandler.instanceOf().clone(Context.getFiles()).updateFileNow(file, template.__getFilename());
        template.forgetFile();
        TemplateHandler.clearCache();
        Date when = new Date();
        try {
            User u = User.getCurrentUser();
            QueryHandler.instanceOf().clone(Context.getHistory()).insertHistoryItem(
                    String.format(message, template.__getCname()), u.__getCname(),
                    template.getDbIdentity(), template.__getIDS(), template.__getGroupsids());
        } catch (Exception e) {
            Log.Debug(TemplateEditing.class, e);
        }
        return when;
    }

    static byte[] hash(File f) throws Exception {
        if (!f.exists()) {
            return new byte[0];
        }
        return MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(f.toPath()));
    }
}
