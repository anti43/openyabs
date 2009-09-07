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
package mpv5.logging;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import javax.swing.table.TableModel;
import mpv5.Main;
import mpv5.bugtracker.ExceptionHandler;
import mpv5.globals.Messages;
import mpv5.utils.files.FileReaderWriter;

/**
 *
 * 
 */
public class Log {

    /**
     *
     * Set the logging to NONE
     */
    public static final int LOGLEVEL_NONE = 0;
    /**
     *
     * Gives basic messages 
     */
    public static final int LOGLEVEL_NORMAL = 1;
    /**
     *
     * Produces a huge amount of messages, and error stack traces
     */
    public static final int LOGLEVEL_DEBUG = 2;
    private static int loglevel = 1;
    private static LogConsole logger = new LogConsole();

    /**
     * Print out a text file
     * @param file
     */
    public static void Debug(File file) {
        PrintArray(new FileReaderWriter(file).readLines());
    }

    /**
     * 
     * @param source
     * @param message
     * @param alwaysToKonsole
     * @deprecated Replaced with <code>Debug(Object source, Object message)</code>
     */
    public static void Debug(Object source, Object message, boolean alwaysToKonsole) {
        Debug(source, message);
    }

    /**
     * The main debug method. Logs the given message depending on the current Log level.<br/><br/>
     * <li>LOGLEVEL_NONE = no logging</li>
     * <li>LOGLEVEL_HIGH  = basic logging</li>
     * <li>LOGLEVEL_DEBUG = verbose logging</li>
     * 
     * @param source
     * @param message
     */
    public static void Debug(Object source, Object message) {
        String sourcen;
        if (source instanceof Class) {
            sourcen = ((Class) source).getName();
        } else {
            sourcen = source.getClass().getName();
        }

        if (message instanceof Exception) {
            try {
                ExceptionHandler.add((Exception) message);
            } catch (Exception e) {
            }
        }

        switch (loglevel) {
            case LOGLEVEL_DEBUG:
                write(sourcen + ": " + message);
                if (message != null && message.toString().contains("Exception")) {
                    ((Exception) message).printStackTrace();
                    write(getStackTrace(((Exception) message)));
                    write("\nCaused by:\n");
                    try {
                        ((Exception) message).getCause().printStackTrace();
                        write(getStackTrace(((Exception) message).getCause()));
                        mpv5.ui.frames.MPView.addMessage(Messages.ERROR_OCCURED + ". " + Messages.SEE_LOG);
                    } catch (Exception e) {
                    }
                }
                break;
            case LOGLEVEL_NORMAL:
                if (message != null && message.toString().contains("Exception")) {
                    write(sourcen + ": " + message);
                    mpv5.ui.frames.MPView.addMessage(Messages.ERROR_OCCURED + ". " + Messages.SEE_LOG);
                }
                break;
            case LOGLEVEL_NONE:
                mpv5.ui.frames.MPView.addMessage(Messages.ERROR_OCCURED.getValue());
                break;
            default:
                write(sourcen + ": " + message);
        }
    }

    /**
     * 
     * @param source
     * @param message
     * @param alwaysToKonsole
     * @deprecated  Replaced with <code>Debug(Object source, Object message)</code>
     */
    public static void Debug(Class source, Object message, boolean alwaysToKonsole) {
        Debug(source, message);
    }

    /**
     * Prints a message, regardless the log level
     * @param message
     */
    public static void Print(Object message) {
        write(message);
        if (!LogConsole.CONSOLE_LOG_ENABLED) {
            System.out.println(message);
        }
    }

    /**
     * Print a list
     * @param data
     */
    public static void PrintArray(ArrayList data) {
        PrintArray(data.toArray());
    }

    /**
     * Print an array
     * @param array
     */
    public static void PrintArray(Object[][][] array) {
        write("Print array: {");
        if (loglevel != LOGLEVEL_NONE) {
            for (int i = 0; i < array.length; i++) {
                write("");
                for (int k = 0; k < array[i].length; k++) {
                    write("");
                    for (int f = 0; f < array[i][k].length; f++) {
                        write(array[i][k][f] + " ");
                    }
                }
            }
        }
        write("}//End Print array");
    }

    /**
     * Print an array
     * @param array
     */
    public static void PrintArray(Object[][] array) {
        write("Print array: {");
        for (int i = 0; i < array.length; i++) {
            for (int k = 0; k < array[i].length; k++) {
                if (loglevel != LOGLEVEL_NONE) {
                    write("[" + i + "]" + " [" + k + "] " + array[i][k]);
                }
            }
        }
        write("}//End Print array");
    }

    /**
     * Print an array
     * @param string
     */
    public static void PrintArray(Object[] string) {
        write("Print array: {");
        for (int i = 0; i < string.length; i++) {
            if (loglevel != LOGLEVEL_NONE) {
                write(string[i]);
            }
        }
        write("}//End Print array");
    }

    /**
     * Print a list
     * @param lst
     */
    public static void PrintArray(List lst) {
        for (int idx = 0; idx < lst.size(); idx++) {
            write(lst.get(idx));
        }
    }

    private static synchronized void write(Object obj) {
        try {
            logger.log(obj);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Debug an Exception
     * @param ex
     */
    public static void Debug(Exception ex) {
        Debug(Log.class, ex);
    }

    /**
     *
     * @return
     */
    public static LogConsole getLogger() {
        return logger;
    }

    /**
     * Set the log level<br/>
     * <li>LOGLEVEL_NONE = no logging</li>
     * <li>LOGLEVEL_HIGH  = basic logging</li>
     * <li>LOGLEVEL_DEBUG = verbose logging</li>
     * @param level
     */
    public static void setLogLevel(int level) {
        Log.loglevel = level;
    }

    /**
     * Print a table model
     * @param model
     */
    public static void PrintArray(TableModel model) {
        for (int i = 0; i < model.getRowCount(); i++) {
            for (int j = 0; j < model.getColumnCount(); j++) {
                write(model.getValueAt(i, j));
            }
        }
    }

    private Log() {
    }

    /**
     *
     * @return The current log level
     */
    public static int getLoglevel() {
        return loglevel;
    }

    /**
     * Writes the stacktrace to a String
     * @param t
     * @return
     */
    public static String getStackTrace(Throwable t) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw, true);
        t.printStackTrace(pw);
        pw.flush();
        sw.flush();
        return sw.toString();
    }
}
