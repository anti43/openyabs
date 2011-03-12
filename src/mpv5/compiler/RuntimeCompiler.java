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
package mpv5.compiler;
/*
 * Original code:
 *
 * Copyright: (c) 2009 Roedy Green, Canadian Mind Products, http://mindprod.com
 *
 * Licence: This software may be copied and used freely for any purpose but military.
 *          http://mindprod.com/contact/nonmil.html

 */
import java.io.File;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.ToolProvider;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import mpv5.globals.LocalSettings;
import mpv5.logging.Log;

/**
 * Writes class files on the fly to disk, and makes them available for the JVM.
 * <li/>Added caching of class files
 */
public final class RuntimeCompiler {

    /**
     * Compile from within this JVM without spawning javac.exe or a separate JVM.
     *
     * @param source points to source, possibly in RAM.
     *
     * @return status of the compile, true all went perfectly without error.
     * @throws java.io.IOException if trouble writing class files.
     */
    private static boolean compile(JavaFileObject... source) {
        final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

        final JavaCompiler.CompilationTask task = compiler.getTask(null /* default System.err */,
                null /* standard file manager,
                If we wrote our own we could control the location
                of the generated  class files. */,
                null /* standard DiagnosticListener */,
                Arrays.asList(new String[]{"-d", LocalSettings.getProperty(LocalSettings.CACHE_DIR)}),// specify dir for class files
                null /* no annotation classes */,
                // we must convert JavaFileObject... to Iterable<? extends JavaFileObject>
                Arrays.asList(source) /* source code */);
        return task.call();
    }

    /**
     * Generate a program on the fly and compile it
     *
     * @param className The name of the class
     * @param classString The string representation of the class
     *
     * @param packageName
     * @param recompile 
     * @return
     * @throws java.io.IOException         if problems writing class files.
     * @throws ClassNotFoundException      if generated class cannot be found.
     * @throws IllegalAccessException      if try to instantiate a class we are not permitted to access.
     * @throws InstantiationException      if cant instantiate class
     * @throws java.net.URISyntaxException if malformed class name.
     */
    public static Class getClassFor(final String className, final String classString, final String packageName, boolean recompile) throws URISyntaxException,
            IOException,
            ClassNotFoundException,
            IllegalAccessException,
            InstantiationException {

        //Do dont recompile the same file over and over again
        if (recompile || !new File(LocalSettings.getProperty(LocalSettings.CACHE_DIR) + File.separator + packageName.replace(".", File.separator)  + File.separator + className + ".class").exists()) {
            final boolean status = compile(new TempResidentJavaFileObject(className, classString));
            Log.Debug(RuntimeCompiler.class, "Compiled class: " + className + " Status: " + status + "\n" +classString);
        }
        ClassLoader oldCL = Thread.currentThread().getContextClassLoader();
        ClassLoader newCL = new URLClassLoader(new URL[]{new File(LocalSettings.getProperty(LocalSettings.CACHE_DIR) + File.separator).toURI().toURL()}, oldCL);
        Class tempFileClass = null;
        try {
            tempFileClass = newCL.loadClass(packageName + "." + className);
        } catch (ClassNotFoundException classNotFoundException) {
            Log.Debug(classNotFoundException);
            throw classNotFoundException;
        }

        return tempFileClass;
    }
    
     public static Class getClassFor(final String className, final String classString, final String packageName) throws URISyntaxException,
            IOException,
            ClassNotFoundException,
            IllegalAccessException,
            InstantiationException {

       

        return getClassFor(className, classString, packageName, false);
    }

    /**
     * Instantiates a new Object for the given class
     * @param clazz
     * @return
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    public static Object getObjectFor(Class clazz) throws InstantiationException, IllegalAccessException {
        return clazz.newInstance();
    }
}

/**
 * Represents the source text of a Java program in a temp dir
 */
class TempResidentJavaFileObject extends SimpleJavaFileObject {

    /**
     * source text of the program to be compiled
     */
    private final String programText;

    /**
     * constructor
     *
     * @param className   class name, without package
     * @param programText text of the program.
     *
     * @throws java.net.URISyntaxException if malformed class name.
     */
    @SuppressWarnings({"SameParameterValue"})
    public TempResidentJavaFileObject(String className, String programText) throws URISyntaxException {
        super(new URI(className + ".java"), Kind.SOURCE);
        this.programText = programText;
    }

    /**
     * Get the text of the java program
     *
     * @param ignoreEncodingErrors ignored.
     */
    @Override
    public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
        return programText;
    }
}
