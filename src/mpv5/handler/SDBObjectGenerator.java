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
package mpv5.handler;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import mpv5.compiler.RuntimeCompiler;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.logging.Log;

/**
 * This class generates simplified Beans from DatabaseObjects which do only contain their getter/setter Methods
 */
public abstract class SDBObjectGenerator {


    /**
     * Create an SimpleObject
     * @param dos
     * @param packagename
     * @return
     */
    public static SimpleDatabaseObject getObjectFrom(DatabaseObject dos, String packagename) {

        ArrayList<Method> methods = new ArrayList<Method>(dos.setVars().values());

        String classTemplate =
                "package " + packagename + ";\n" +
                "import mpv5.db.common.Context;\n" +
                "import mpv5.db.common.DatabaseObject;\n" +
                "import mpv5.utils.images.MPIcon;\n" +
                "import java.util.Date;\n" +
                "\n" +
                "\n" +
                "public final class " + dos.getDbIdentity() + " implements mpv5.handler.SimpleDatabaseObject{\n\n";

        for (int i = 0; i < methods.size(); i++) {
            Method method = methods.get(i);
            classTemplate +=
                    "private " + method.getParameterTypes()[0].getCanonicalName() + "  " + method.getName().substring(3).toLowerCase() + ";\n" +
                    "public " + method.getParameterTypes()[0].getCanonicalName() + "  get" + method.getName().substring(3) + "(){\n" +
                    "return " + "this." + method.getName().substring(3).toLowerCase() + ";\n" +
                    "}\n" +
                    "public boolean " + method.getName().toString() + "(" + method.getParameterTypes()[0].getCanonicalName() + " arg){\n" +
                    "this." + method.getName().substring(3).toLowerCase() + " = arg;\n" +
                    "return true;\n" +
                    "}" +
                    "\n\n";
        }

        classTemplate += "" +
                "public boolean persist() throws Exception {" +
                "return DatabaseObject.parse(this).saveImport();" +
                "}" +

                "" +
                "public boolean fetch(int id) throws Exception {" +
                "DatabaseObject.getObject(Context.getMatchingContext(getContext()), id).inject(this);" +
                "return true;" +
                "}" +

                "" +
                "public boolean fetch(String cname) throws Exception {" +
                "DatabaseObject.getObject(Context.getMatchingContext(getContext()), cname).inject(this);" +
                "return true;" +
                "}" +

                "" +
                "public String getContext(){" +
                "return getClass().getSimpleName();" +
                "}" +

                "}\n";
        try {
            return (SimpleDatabaseObject) RuntimeCompiler.getObjectFor(RuntimeCompiler.getClassFor(dos.getDbIdentity(), classTemplate, packagename));
        } catch (Exception ex) {
            Log.Debug(ex);
            return null;
        }
    }
}
