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
package mpv5.globals;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;
import mpv5.Main;
import mpv5.logging.Log;

/**
 * Which database (connection of the settings file) YaBS opens at start: the
 * one used last (default) or always the same one. Kept next to the settings
 * file, as the settings file holds one block per connection.
 */
public final class ConnectionStart {

    /**
     * At start: the connection used last
     */
    public static final int LAST = -1;
    private static final String MODE = "start";
    private static final String LAST_USED = "last";

    private ConnectionStart() {
    }

    private static File file() {
        return new File(Main.MPPATH, "connectionstart.properties");
    }

    private static Properties read() {
        Properties p = new Properties();
        File f = file();
        if (f.isFile()) {
            try (InputStream in = new FileInputStream(f)) {
                p.load(in);
            } catch (Exception e) {
                Log.Debug(ConnectionStart.class, e);
            }
        }
        return p;
    }

    private static void write(Properties p) {
        try (OutputStream out = new FileOutputStream(file())) {
            p.store(out, "YaBS: database at start");
        } catch (Exception e) {
            Log.Debug(ConnectionStart.class, e);
        }
    }

    private static int number(String s, int def) {
        try {
            return s == null ? def : Integer.parseInt(s.trim());
        } catch (NumberFormatException e) {
            return def;
        }
    }

    /**
     * @return {@link #LAST} or the id of the connection that always opens
     */
    public static synchronized int getChoice() {
        return number(read().getProperty(MODE), LAST);
    }

    /**
     * @param choice {@link #LAST} or the id of a connection
     */
    public static synchronized void setChoice(int choice) {
        Properties p = read();
        p.setProperty(MODE, String.valueOf(choice));
        write(p);
    }

    /**
     * Remembers the connection in use (for {@link #LAST})
     *
     * @param id
     */
    public static synchronized void used(int id) {
        Properties p = read();
        if (!String.valueOf(id).equals(p.getProperty(LAST_USED))) {
            p.setProperty(LAST_USED, String.valueOf(id));
            write(p);
        }
    }

    /**
     * @return the connection to open at start, null if none is set (the first
     * connection then)
     */
    public static synchronized Integer atStart() {
        Properties p = read();
        int choice = number(p.getProperty(MODE), LAST);
        if (choice != LAST) {
            return choice;
        }
        int last = number(p.getProperty(LAST_USED), LAST);
        return last == LAST ? null : last;
    }
}
