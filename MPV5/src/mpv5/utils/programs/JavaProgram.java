/*
 *  This file is part of MP by anti43 /GPL.
 *
 *  MP is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  MP is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with MP.  If not, see <http://www.gnu.org/licenses/>.
 */

package mpv5.utils.programs;

import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import mpv5.logging.Log;

/**
 *
 * @author anti43
 */
public class JavaProgram {

    public static void run(String string, String clazz){
        try {
            ClassLoader loader = URLClassLoader.newInstance(new URL[]{JavaProgram.class.getResource(string)}, null);
            Class<?> mainClass = Class.forName(clazz, false, loader);
            Method mainMethod = mainClass.getMethod("main", String[].class);
            mainMethod.invoke(null, new Object[]{new String[]{ /* args */}});
        } catch (Exception ion) {
            Log.Debug(JavaProgram.class, ion);
        }
    }

}
