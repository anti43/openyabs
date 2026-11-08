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
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.logging.Log;

/**
 * What happened in this session: messages, created files (PDF, ODT,
 * e-invoices) and created, changed or deleted records. Newest first, without
 * Swing; the status bar shows it.
 */
public final class Activity {

    /**
     * How many entries are kept
     */
    public static final int MAX = 500;

    private Activity() {
    }

    /**
     * The kind of an entry
     */
    public enum Kind {
        MESSAGE, WARNING, FILE, CREATED, CHANGED, DELETED
    }

    /**
     * One thing that happened
     */
    public static final class Entry {

        public final Date time = new Date();
        public final Kind kind;
        public final String text;
        /**
         * the type of the record or the owner of a file, may be empty
         */
        public final String detail;
        /**
         * the created file, or null
         */
        public final File file;
        /**
         * the record (for a file: its owner), or null
         */
        public final Context context;
        public final int id;

        Entry(Kind kind, String text, String detail, File file, Context context, int id) {
            this.kind = kind;
            this.text = text == null ? "" : text;
            this.detail = detail == null ? "" : detail;
            this.file = file;
            this.context = context;
            this.id = id;
        }

        /**
         * @return true if a record can be opened (not deleted)
         */
        public boolean hasRecord() {
            return context != null && id > 0 && kind != Kind.DELETED;
        }

        /**
         * @return true if there is a file that still exists
         */
        public boolean hasFile() {
            return file != null && file.exists();
        }

        /**
         * @param needle
         * @return true if every word of the needle is in the text, the detail
         * or the file name
         */
        public boolean matches(String needle) {
            return SearchService.matches(text + " " + detail + " " + (file == null ? "" : file.getName()), needle);
        }

        @Override
        public String toString() {
            return text;
        }
    }

    private static final LinkedList<Entry> entries = new LinkedList<Entry>();
    private static final List<Consumer<Entry>> listeners = new CopyOnWriteArrayList<Consumer<Entry>>();

    /**
     * @return the entries, newest first
     */
    public static List<Entry> entries() {
        synchronized (entries) {
            return new ArrayList<Entry>(entries);
        }
    }

    /**
     * Clears the list
     */
    public static void clear() {
        synchronized (entries) {
            entries.clear();
        }
        fire(null);
    }

    /**
     * @param l told about every new entry (null: the list was changed
     * otherwise), on any thread
     */
    public static void addListener(Consumer<Entry> l) {
        listeners.add(l);
    }

    /**
     * A message of the status bar
     *
     * @param text
     * @param warning shown in red/yellow
     */
    public static void message(String text, boolean warning) {
        if (text == null || text.trim().isEmpty() || "null".equals(text)) {
            return;
        }
        add(new Entry(warning ? Kind.WARNING : Kind.MESSAGE, text.trim(), "", null, null, 0));
    }

    /**
     * A created file
     *
     * @param f
     * @param owner the record it was made for, may be null
     */
    public static void file(File f, DatabaseObject owner) {
        if (f == null) {
            return;
        }
        String detail = "";
        Context c = null;
        int id = 0;
        try {
            if (owner != null) {
                detail = (type(owner) + " " + owner.__getCname()).trim();
                c = owner.getContext();
                id = owner.__getIDS();
            }
        } catch (Exception e) {
            Log.Debug(Activity.class, e);
        }
        synchronized (entries) {
            //the same file again (a new preview, saved again): only the newest
            entries.removeIf(e -> e.file != null && e.file.equals(f));
        }
        add(new Entry(Kind.FILE, f.getName(), detail, f, c, id));
    }

    /**
     * A created, changed or deleted record
     *
     * @param dbo
     * @param kind CREATED, CHANGED or DELETED
     * @param text the message (as in the status bar)
     */
    public static void record(DatabaseObject dbo, Kind kind, String text) {
        if (dbo == null) {
            return;
        }
        try {
            //the login of a user is saved too: not an activity
            if (kind == Kind.CHANGED && dbo.getContext().equals(Context.getUser())) {
                return;
            }
            String t = text == null ? dbo.__getCname() : text;
            add(new Entry(kind, t, type(dbo), null, dbo.getContext(), dbo.__getIDS()));
        } catch (Exception e) {
            Log.Debug(Activity.class, e);
        }
    }

    private static String type(DatabaseObject dbo) {
        try {
            if (dbo instanceof mpv5.db.objects.Item) {
                return mpv5.db.objects.Item.getTypeString(((mpv5.db.objects.Item) dbo).__getInttype());
            }
            Context c = dbo.getContext();
            if (c.equals(Context.getContact())) {
                return mpv5.globals.Messages.SEARCH_KIND_CONTACTS.toString();
            } else if (c.equals(Context.getProduct())) {
                return mpv5.globals.Messages.SEARCH_KIND_PRODUCTS.toString();
            } else if (c.equals(Context.getConversation())) {
                return mpv5.globals.Messages.SEARCH_KIND_CONVERSATIONS.toString();
            } else if (c.equals(Context.getSchedule())) {
                return mpv5.globals.Messages.SEARCH_KIND_SCHEDULE.toString();
            }
            String t = dbo.getContext().getDbIdentity();
            return t.isEmpty() ? "" : Character.toUpperCase(t.charAt(0)) + t.substring(1);
        } catch (Exception e) {
            return "";
        }
    }

    private static void add(Entry e) {
        synchronized (entries) {
            //the status message of a record comes first: the record replaces it
            if (e.context != null && e.kind != Kind.FILE && !entries.isEmpty()) {
                Entry last = entries.getFirst();
                if ((last.kind == Kind.MESSAGE) && last.text.equals(e.text)
                        && e.time.getTime() - last.time.getTime() < 10000) {
                    entries.removeFirst();
                }
            }
            entries.addFirst(e);
            while (entries.size() > MAX) {
                entries.removeLast();
            }
        }
        fire(e);
    }

    private static void fire(Entry e) {
        for (Consumer<Entry> l : listeners) {
            try {
                l.accept(e);
            } catch (Exception ex) {
                Log.Debug(Activity.class, ex);
            }
        }
    }
}
