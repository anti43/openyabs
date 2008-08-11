/*
 * 
This file is part of MP by anti43 /GPL.

MP is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

MP is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with MP.  If not, see <http://www.gnu.org/licenses/>.

 * 
 */
package mp4.utils.text;

import mp3.classes.utils.Log;

/**
 *
 * @author Administrator
 */
public class TextFormat {

    public static String maxLineLength(String text, int linelength) {

        if (text != null && text.length() > linelength) {
            int k = text.length() / linelength;
            Integer position = 0;
            for (int j = 0; j < k; j++) {
                try {
                    text = text.substring(0, position + linelength) + "\n" + text.substring(position + linelength, text.length());
                    
                } catch (Exception e) {
                    Log.Debug(e.getMessage(), true);
                }
                position += linelength;
            }
        }
        return text;
    }

    public static int verifyTextMaxLength(String text, int maxLegth) {
        try {
            text = text.substring(0, maxLegth);
        } catch (Exception e) {
            return text.length();
        }
        return maxLegth;
    }
}
