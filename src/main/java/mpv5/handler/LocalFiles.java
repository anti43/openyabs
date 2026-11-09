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
import java.util.Map;
import java.util.TreeMap;

/**
 * What "delete local files" deletes: the content of the cache folder
 * (templates and language files fetched from the database, classic plugins,
 * previews and temporary PDFs). The database, the settings, the plugins
 * folder and saved documents are elsewhere and stay.
 */
public final class LocalFiles {

    private LocalFiles() {
    }

    /**
     * What a folder holds
     */
    public static final class Summary {

        public int files;
        public long bytes;
        /**
         * number of files per kind (extension)
         */
        public final Map<String, Integer> kinds = new TreeMap<String, Integer>();
    }

    /**
     * @param dir
     * @return the files below the folder, counted by extension
     */
    public static Summary summarize(File dir) {
        Summary s = new Summary();
        add(dir, s);
        return s;
    }

    private static void add(File f, Summary s) {
        File[] l = f.listFiles();
        if (l == null) {
            return;
        }
        for (File x : l) {
            if (x.isDirectory()) {
                add(x, s);
            } else {
                s.files++;
                s.bytes += x.length();
                String n = x.getName();
                int dot = n.lastIndexOf('.');
                String kind = dot < 0 ? "-" : n.substring(dot + 1).toLowerCase();
                s.kinds.merge(kind, 1, Integer::sum);
            }
        }
    }

    /**
     * @param bytes
     * @return e.g. "3.4 MB"
     */
    public static String size(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        }
        if (bytes < 1024 * 1024) {
            return String.format("%.1f KB", bytes / 1024d);
        }
        return String.format("%.1f MB", bytes / (1024d * 1024d));
    }

    /**
     * Deletes the content of the folder (not the folder)
     *
     * @param dir
     * @return how many files could not be deleted (in use)
     */
    public static int wipe(File dir) {
        int left = 0;
        File[] l = dir.listFiles();
        if (l == null) {
            return 0;
        }
        for (File x : l) {
            if (x.isDirectory()) {
                left += wipe(x);
                x.delete();
            } else if (!x.delete()) {
                left++;
            }
        }
        return left;
    }
}
