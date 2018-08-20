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
package mpv5.utils.reflection;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;

import mpv5.Main;
import mpv5.logging.Log;

/**
 * Useful classpath tools
 *
 */
public class ClasspathTools {


    /**
     * Adds a path temporary to the Classpath
     *
     * @param file
     */
    public static void addPath(File file) {
        addPath(file.toURI());
    }

    /**
     * Adds a path temporary to the Classpath
     *
     * @param s
     * @throws Exception
     */
    public static void addPath(String s) throws Exception {
        addPath(new File(s));
    }

    /**
     * Adds a path temporary to the Classpath
     *
     * @param s
     */
    public static void addPath(URI s) {

        Log.Debug(ClasspathTools.class, "Adding path: " + s);
        try {
            URL u = s.toURL();
            Java10UrlClassloader urlClassLoader = Main.classLoader;
            urlClassLoader.addURL(u);
        } catch (Throwable ex) {
            Logger.getLogger(ClasspathTools.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * findLibsFromManifest where the class comes from
     *
     * @param fromClass
     * @return
     * @throws IOException
     */
    public static String[] findLibsFromManifest(Class fromClass) throws IOException {
        Class clazz = fromClass;
        String className = clazz.getSimpleName() + ".class";
        String classPath = clazz.getResource(className).toString();
        if (!classPath.startsWith("jar")) {
            // Class not from JAR
            Log.Debug(ClasspathTools.class, "Class not loaded from jar: " + fromClass);
            return null;
        }
        String manifestPath = classPath.substring(0, classPath.lastIndexOf("!") + 1)
                + "/META-INF/MANIFEST.MF";
        Manifest manifest = new Manifest(new URL(manifestPath).openStream());
        Attributes attr = manifest.getMainAttributes();

        String value = attr.getValue("Class-Path");
//        Log.Debug(ClasspathTools.class, value);
        return value.split(" ");
    }

    /**
     * Tries to determine the dir we are running in
     *
     * @return
     */
    public static File findUserDir() {
        String classPath = Main.class.getResource("Main.class").toString();
        String app = "";
        if (!classPath.startsWith("jar")) {
            // Class not from JAR
            app = File.separator + "dist";
        }
        return new File(System.getProperty("user.dir") + app);
    }
}
