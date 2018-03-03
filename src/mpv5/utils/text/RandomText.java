
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
import java.security.SecureRandom;
import java.util.UUID;

/**
 * A random text generator
 *
 */
public class RandomText {
    private static SecureRandom r;
    static {
        r = new SecureRandom();
        r.setSeed(System.currentTimeMillis());
    }

    private RandomText(){
    }
    
    private RandomText(int lenght){
    }

    /**
     * Generates a 8 digit integer value
     *
     * @return
     */
    public synchronized static Integer getInteger() {
        return r.nextInt();
    }

    /**
     * @return A random text like '2d7428a6-b58c-4008-8575-f05549f16316'
     */
    public static String getString() {
        return UUID.randomUUID().toString();
    }
    
    /**
     * Creates a random string whose length is the number of characters specified.
     * Characters will be chosen from the set of alphabetic characters.
     * @param length
     * @return 
     */
     public static String getText(int length) {
        return RandomStringUtils.randomAlphabetic(length);
    }

   /**
     * Creates a random string whose length 8.
     * Characters will be chosen from the set of alphabetic characters.
     * @return 
     */
    public static String getText() {
        return getText(8);
    }

    /**
     * Generates a random text containing only numbers
     *
     * @return A random 8- char text
     */
    public synchronized static String getNumberText() {
        return String.valueOf(r.nextLong());
    }

}


//~ Formatted by Jindent --- http://www.jindent.com
