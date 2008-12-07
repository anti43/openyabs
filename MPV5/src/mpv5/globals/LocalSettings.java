/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mpv5.globals;

import java.util.Locale;

/**
 *
 * @author Andreas
 */
public class LocalSettings {

    private static LocalSettings cookie;

    public static String getDBDriver() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public static String getDBPassword() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public static String getDBUser() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public static LocalSettings instanceOf() {
        if (cookie == null) {
            cookie = new LocalSettings();
        }
        return cookie;
    }

    public static void setDBDriver(String DERBY_DRIVER) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public static void setDBPath(String string) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public Locale getLocale() {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
