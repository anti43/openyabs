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
package mpv5.utils.html;

import mpv5.utils.arrays.ArrayUtilities;

/**
 *
 *  
 */
public class HtmlParser {

    private String htmlstring;
    private String plainstring;
    public static final String htmlRegEx = "</?\\w+((\\s+\\w+(\\s*=\\s*(?:\"(.|\\n)*?\"|'(.|\\n)*?'|[^'\">\\s]+))?)+\\s*|\\s*)/?>";

    public HtmlParser() {
    }

    public HtmlParser(String string) {
            this.plainstring = string;
    }


    public String getHtml() {
        if (htmlstring == null) {
            htmlstring = makeHtml(plainstring);
        }
        return htmlstring;
    }

    public String markHtml(String key) {
        getHtml();
        if (key.length() > 0) {
            htmlstring = makeHtml(makePlain(htmlstring).replaceAll("(?i)"+makePlain(key) , "<b><font color=blue>" + makePlain(key) + "</font></b>"));
        } 
        return htmlstring;
    }

    private String makeHtml(String plainstring) {
        return "<html>" + plainstring.replaceAll("\\n", "<br/>") + "</html>";
    }

    private String makePlain(String htmlstring) {
        return htmlstring.replace("<br/>", "\\n").replaceAll(htmlRegEx, "");
    }

    /**
     * 
     * @param data
     * @param column
     * @param keyToBeMarked
     * @return
     */
    public static String[][] getMarkedHtml(Object[][] data, int column, String keyToBeMarked) {

        for (int i = 0; i < data.length; i++) {
            Object[] objects = data[i];
            for (int j = 0; j < objects.length; j++) {
                if (j == column) {
                    HtmlParser hp = new HtmlParser(String.valueOf(data[i][j]));
                    hp.markHtml(keyToBeMarked);
                    data[i][j] = hp.getHtml();
                }
            }
        }

        return ArrayUtilities.ObjectToStringArray(data);
    }
}
