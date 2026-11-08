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
package mpv5.utils.files;

import java.io.File;
import mpv5.globals.LocalSettings;
import mpv5.logging.Log;

/**
 * Where file dialogs start: the folder the user chose last (kept in the
 * local settings, so it survives a restart), else the configured base folder
 * for saved documents, else the desktop or the home folder.
 */
public final class LastDirectory {

    /**
     * Local setting with the folder chosen last in a file dialog
     */
    public static final String KEY = "lastfiledir";

    private LastDirectory() {
    }

    /**
     * @return the folder a file dialog starts in; always an existing folder
     */
    public static File get() {
        File last = existingDir(read(KEY));
        if (last != null) {
            return last;
        }
        File base = configuredBaseDir();
        if (base != null) {
            return base;
        }
        return fallback();
    }

    /**
     * Remembers the folder of the given file (or the folder itself)
     *
     * @param fileOrDir
     */
    public static void set(File fileOrDir) {
        if (fileOrDir == null) {
            return;
        }
        File dir = fileOrDir.isDirectory() ? fileOrDir : fileOrDir.getAbsoluteFile().getParentFile();
        if (dir != null && dir.isDirectory()) {
            try {
                LocalSettings.setProperty(KEY, dir.getAbsolutePath());
            } catch (Exception e) {
                Log.Debug(LastDirectory.class, e);
            }
        }
    }

    /**
     * @return the base folder for saved documents from the local settings, or
     * null if none is configured (or it does not exist)
     */
    public static File configuredBaseDir() {
        return existingDir(read(LocalSettings.BASE_DIR));
    }

    /**
     * @return true if a base folder for saved documents is configured
     */
    public static boolean isBaseDirConfigured() {
        String s = read(LocalSettings.BASE_DIR);
        return s != null;
    }

    /**
     * @return the desktop if it exists, else the home folder
     */
    public static File fallback() {
        File home = new File(System.getProperty("user.home"));
        File desktop = new File(home, "Desktop");
        return desktop.isDirectory() ? desktop : home;
    }

    private static String read(String key) {
        try {
            if (LocalSettings.hasProperty(key)) {
                String s = LocalSettings.getProperty(key);
                if (s != null && s.trim().length() > 0 && !s.equals("null")) {
                    return s.trim();
                }
            }
        } catch (Exception e) {
            Log.Debug(LastDirectory.class, e);
        }
        return null;
    }

    private static File existingDir(String path) {
        if (path == null) {
            return null;
        }
        File f = new File(path);
        return f.isDirectory() ? f : null;
    }
}
