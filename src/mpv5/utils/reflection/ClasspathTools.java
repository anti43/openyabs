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
package mpv5.utils.reflection;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.logging.Level;
import java.util.logging.Logger;
import mpv5.logging.Log;

/**
 * Useful classpath tools
 * 
 */
public class ClasspathTools {

    public static void addPath(File file) {
        addPath(file.toURI());
    }

    public static void addPath(String s) throws Exception {
        addPath(new File(s));
    }

    public static void addPath(URI s) {

        Log.Debug(ClasspathTools.class, "Adding path: " + s);
        try {
            URL u = s.toURL();
            URLClassLoader urlClassLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
            Class urlClass = URLClassLoader.class;
            @SuppressWarnings(value = "unchecked")
            Method method = urlClass.getDeclaredMethod("addURL", new Class[]{URL.class});
            method.setAccessible(true);
            method.invoke(urlClassLoader, new Object[]{u});
        } catch (IllegalAccessException ex) {
            mpv5.logging.Log.Debug(ex);//Logger.getLogger(ClasspathTools.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalArgumentException ex) {
            mpv5.logging.Log.Debug(ex);//Logger.getLogger(ClasspathTools.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvocationTargetException ex) {
            mpv5.logging.Log.Debug(ex);//Logger.getLogger(ClasspathTools.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchMethodException ex) {
            mpv5.logging.Log.Debug(ex);//Logger.getLogger(ClasspathTools.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SecurityException ex) {
            mpv5.logging.Log.Debug(ex);//Logger.getLogger(ClasspathTools.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MalformedURLException ex) {
            mpv5.logging.Log.Debug(ex);//Logger.getLogger(ClasspathTools.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
