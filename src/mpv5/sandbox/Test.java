/*
 *  This file is part of MP.
 *
 *      MP is free software: you can redistribute it and/or modify
 *      it under the terms of the GNU General Public License as published by
 *      the Free Software Foundation, either version 3 of the License, or
 *      (at your option) any later version.
 *
 *      MP is distributed in the hope that it will be useful,
 *      but WITHOUT ANY WARRANTY; without even the implied warranty of
 *      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *      GNU General Public License for more details.
 *
 *      You should have received a copy of the GNU General Public License
 *      along with MP.  If not, see <http://www.gnu.org/licenses/>.
 */
package mpv5.sandbox;

import ag.ion.bion.officelayer.filter.IFilter;
import ag.ion.bion.officelayer.filter.PDFFilter;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import mpv5.db.common.Context;
import mpv5.db.common.QueryParameter;
import mpv5.db.objects.Product;


/**
 *
 */
public class Test {

  

    public static void main(String... aArgs)  {

         List<QueryParameter> ps = new ArrayList<QueryParameter>();
            for (Map.Entry<String, Class<?>> en : new Product().getKeySet()) {
                if (en.getValue().isAssignableFrom(String.class)) {
                   ps.add(new QueryParameter(Context.getProduct(), en.getKey(), "f", QueryParameter.LIKE));
                } else System.out.println(en.getKey());
            }


    }

     /**
     * Separator vor list values val1, val2, val3..
     */
    public static final String LIST_SEPARATOR_CHAR = "<###>";

    /**
     *
     * @param values
     * @return
     */
    public static synchronized List<String> stringToList(String values) {
        String[] vals = values.split(LIST_SEPARATOR_CHAR);
        List<String> list = new Vector<String>();
        for (int i = 0; i < vals.length; i++) {
            String string = vals[i];
            list.add(string);
        }

        return list;
    }

    /**
     *
     * @param <T>
     * @param list
     * @return
     */
    public static synchronized <T extends Object> String listToString(List<T> list) {
        String str = new String("");

        if (list != null && list.size() > 0) {
            for (T val : list) {
                str += val.toString() + LIST_SEPARATOR_CHAR;
            }
            return str.substring(0, str.length() - LIST_SEPARATOR_CHAR.length());
        } else {
            return str;
        }
    }

}
