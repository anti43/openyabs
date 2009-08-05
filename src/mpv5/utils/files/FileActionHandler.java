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
package mpv5.utils.files;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import mpv5.logging.Log;
import mpv5.utils.print.PrintJob;

/**
 * This class is designed to have easy and quick, OS independent 
 * access to common desktop actions
 *  
 */
public class FileActionHandler {

    /**
     * Print file f
     * @param f
     */
    public static void print(File f) {
        if (Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().print(f);
            } catch (IOException ex) {
                Log.Debug(FileActionHandler.class, "Print Error (Native printing not supported. Do you have the GNOME libraries installed?): ");
                Log.Debug(FileActionHandler.class, ex);
                alternatePrint(f);
            }
        }
    }

    private static void alternatePrint(File f) {
        try {
            new PrintJob().print(f);
        } catch (Exception ex) {
            Log.Debug(FileActionHandler.class, "Alternative Print Method Error: ");
            Log.Debug(FileActionHandler.class, ex);
        }
    }

    /**
     * Open File f for editing
     * @param f
     */
    public static void edit(File f) {
        if (Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().edit(f);
            } catch (IOException ex) {
                Log.Debug(FileActionHandler.class, "Open for Edit Error: ");
                Log.Debug(FileActionHandler.class, ex);
            }
        }
    }

    /**
     * Open File f in default application
     * @param f
     */
    public static void open(File f) {
        if (Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().open(f);
            } catch (IOException ex) {
                Log.Debug(FileActionHandler.class, "Open File Error: ");
                Log.Debug(FileActionHandler.class, ex);
            }
        }
    }
}
