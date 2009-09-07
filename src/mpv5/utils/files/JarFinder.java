/*
 * 
 *  *  This file is part of YaBS.
 *  *  
 *  *      YaBS is free software: you can redistribute it and/or modify
 *  *      it under the terms of the GNU General Public License as published by
 *  *      the Free Software Foundation, either version 3 of the License, or
 *  *      (at your option) any later version.
 *  *  
 *  *      YaBS is distributed in the hope that it will be useful,
 *  *      but WITHOUT ANY WARRANTY; without even the implied warranty of
 * *      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  *      GNU General Public License for more details.
 *  *  
 *  *      You should have received a copy of the GNU General Public License
 *  *      along with YaBS.  If not, see <http://www.gnu.org/licenses/>.
 *  
 */
package mpv5.utils.files;

import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;
import mpv5.logging.Log;

public class JarFinder {

    /**
     * Tries to localize a jar file
     * @param nameOfJar
     * @return
     * @throws Exception
     */
    public static String getPathOfJar(String nameOfJar) throws Exception {
        //System.setProperties("java.class.path");
        StringTokenizer st = new StringTokenizer(System.getProperty("java.class.path"), System.getProperty("path.separator"));
        String jarfile = "";
//        Log.Debug(JarFinder.class, System.getProperty("java.class.path"));
        while (st.hasMoreTokens()) {

            String token = st.nextToken();
            if (token.indexOf(nameOfJar) > -1) {
                jarfile = token;
                break;
            }
        }
        if (jarfile.equals("")) {
            throw new Exception("Jar not found in classpath: " + nameOfJar);
        } else {
            String path = jarfile.substring(0, jarfile.indexOf(nameOfJar));
            return path;
        }
    }

    /**
     *
     * @return Th ecurrent classpath entries as list
     */
    public static List<String> getClassPath() {
        StringTokenizer st = new StringTokenizer(System.getProperty("java.class.path"), System.getProperty("path.separator"));
        List<String> list = new Vector<String>();
        while (st.hasMoreTokens()) {
            list.add(st.nextToken());
        }
        return list;
    }
}
