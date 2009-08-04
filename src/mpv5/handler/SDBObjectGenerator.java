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
package mpv5.handler;

import java.lang.reflect.Method;
import java.util.ArrayList;
import mpv5.compiler.RuntimeCompiler;
import mpv5.db.common.DatabaseObject;
import mpv5.logging.Log;




/**
 * This class generates simplified Beans from DatabaseObjects which do only contain their getter/setter Methods
 */
public abstract class SDBObjectGenerator {

    public static String PACKAGE_NAME = "gen.ram.simpleobject";

    public static SimpleDatabaseObject getObjectFrom(DatabaseObject dos) {

        ArrayList<Method> m = dos.setVars();

        String classTemplate =
                "package " + PACKAGE_NAME + ";\n" +
                "import mpv5.db.common.Context;\n" +
                "import mpv5.db.common.DatabaseObject;\n" +
                "import mpv5.utils.images.MPIcon;\n" +
                "import java.util.Date;\n" +
                "\n" +
                "\n" +
                "public final class " + dos.getType() + " implements mpv5.handler.SimpleDatabaseObject{\n\n";

        for (int i = 0; i < m.size(); i++) {
            Method method = m.get(i);
            classTemplate += 
                    "private " + method.getParameterTypes()[0].getCanonicalName() + "  " + method.getName().substring(3).toLowerCase() + ";\n" +
                    "public " + method.getParameterTypes()[0].getCanonicalName() + "  get" + method.getName().substring(3) + "(){\n" +
                    "return " + "this." + method.getName().substring(3).toLowerCase() + ";\n" +
                    "}\n" +
                    "public void " + method.getName().toString() + "(" + method.getParameterTypes()[0].getCanonicalName() + " arg){\n" +
                    "this." + method.getName().substring(3).toLowerCase() + " = arg;\n" +
                    "}" +
                    "\n\n";
        }

        classTemplate += "}\n";
        try {
//            Log.Debug(SDBObjectGenerator.class, classTemplate);
            return (SimpleDatabaseObject) RuntimeCompiler.getObjectFor(RuntimeCompiler.getClassFor(dos.getType(), classTemplate, PACKAGE_NAME));
        } catch (Exception ex) {
            Log.Debug(ex);
            return null;
        }
    }
}
