
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
package mpv5.utils.text;

//~--- JDK imports ------------------------------------------------------------
import java.net.InetAddress;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

/**
 *
 *
 */
public class TypeConversion {

    private static final Pattern rfc2822 = Pattern.compile(
            "^[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?$");

    /**
     * Converts a boolean value into either "0" (false) or "1" (true)
     *
     * @param bool
     * @return The string value
     */
    public static String booleanToString(boolean bool) {
        String booleans = "0";

        if (bool) {
            booleans = "1";
        }

        return booleans;
    }

    /**
     * Converts a String into a boolean value. Everything except the String "1"
     * or "true" will return false.
     *
     * @param string
     * @return
     */
    public static boolean stringToBoolean(String string) {
        boolean val = false;

        if (string != null) {
            if (string.equals("1") || Boolean.valueOf(string)) {
                val = true;
            }
        }

        return val;
    }

    /**
     * Converts a String into a Locale
     *
     * @param localestring In Format "de" or "de_DE"
     * @return
     */
    public static Locale stringToLocale(String localestring) {
        String[] data = localestring.split("_");
        String lang = localestring.substring(0, 2);
        String country = null;

        if ((data != null) && (data.length > 1)) {
            country = data[1];
        }

        if (country != null) {
            return new Locale(lang.toLowerCase(), country.toUpperCase());
        } else {
            return new Locale(lang.toLowerCase());
        }
    }

    /**
     * 
     * @param email
     * @return
     */
    public static InternetAddress stringToMail(String email) {
        if (!rfc2822.matcher(email).matches()) {
            return null;
        }
        try {
            return new InternetAddress(email);
        } catch (AddressException ex) {
            Logger.getLogger(TypeConversion.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
}


//~ Formatted by Jindent --- http://www.jindent.com
