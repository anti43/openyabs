/*
 * 
 *  *  This file is part of MP by anti43 /GPL.
 *  *  
 *  *      MP is free software: you can redistribute it and/or modify
 *  *      it under the terms of the GNU General Public License as published by
 *  *      the Free Software Foundation, either version 3 of the License, or
 *  *      (at your option) any later version.
 *  *  
 *  *      MP is distributed in the hope that it will be useful,
 *  *      but WITHOUT ANY WARRANTY; without even the implied warranty of
 * *      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  *      GNU General Public License for more details.
 *  *  
 *  *      You should have received a copy of the GNU General Public License
 *  *      along with MP.  If not, see <http://www.gnu.org/licenses/>.
 *  
 */
package mp4.utils.files;

import java.util.StringTokenizer;
import mp4.logs.Log;

/**
 *
 * @author anti43
 */
public class JarFinder {

    public static String getPathOfJar(String nameOfJar) throws Exception {
        //System.setProperties("java.class.path");
        StringTokenizer st = new StringTokenizer(System.getProperty("java.class.path"), System.getProperty("path.separator"));
        String jarfile = "";
        Log.Debug(JarFinder.class, System.getProperty("java.class.path"));
        while (st.hasMoreTokens()) {

            String token = st.nextToken();
            if (token.indexOf(nameOfJar) > -1) {
                jarfile = token;
                break;
            }
        }
        if (jarfile.equals("")) {
            System.err.println("Jar not found in classpath");
        } else {
            String path = jarfile.substring(0, jarfile.indexOf(nameOfJar));
            return path;
        }
        return null;
    }
}
