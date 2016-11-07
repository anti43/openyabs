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

import de.frame4j.io.LogHandler;
import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import javax.swing.table.TableModel;
import mpv5.Main;
import mpv5.bugtracker.ExceptionHandler;
import mpv5.db.objects.Contact;
import mpv5.globals.Messages;
import mpv5.utils.files.FileReaderWriter;

/**
 *
 *
 */
public class Log {

   static {

      Logger globalLogger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
      globalLogger.addHandler(new LogHandler(new Formatter() {
         @Override
         public String format(LogRecord record) {
            if (record != null) {
               return record.getMessage();
            } else {
               return "null";
            }
         }
      }) {
         @Override
         public void publish(LogRecord record) {
            Log.Debug(this, record);
         }

         @Override
         public void flush() {
         }

         @Override
         public void close() throws SecurityException {
         }
      });

      Logger.getLogger(Log.class.getName()).log(Level.INFO, "Yabs Logger set!");
   }
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
   private static List<LogConsole> loggers = Collections.synchronizedList(new ArrayList<LogConsole>(Arrays.asList(new LogConsole[]{(LogConsole) new YConsole()})));

   /**
    * Print out a text file
    *
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
    * @deprecated Replaced *
    * with <code>Debug(Object source, Object message)</code>
    */
   public static void Debug(Object source, Object message, boolean alwaysToKonsole) {
      Debug(source, message);
   }

   /**
    * The main debug method. Logs the given message depending on the current Log
    * level.<br/><br/> <li>LOGLEVEL_NONE = no logging</li> <li>LOGLEVEL_HIGH =
    * basic logging</li> <li>LOGLEVEL_DEBUG = verbose logging</li>
    *
    * @param source
    * @param message
    */
   public static synchronized void Debug(Object source, Object message) {
     
      String sourcen;
      if (source instanceof Class) {
         sourcen = ((Class) source).getName();
      } else {
         sourcen = source.getClass().getName();
      }

      if (message instanceof Throwable) {
         try {
            ExceptionHandler.add((Exception) message);
         } catch (Exception e) {
            Print(e);
         }
      }   
      switch (loglevel) {
         case LOGLEVEL_DEBUG:
            writeDirect(sourcen + ": " + message);
            if (message != null && message instanceof Throwable) {
               ((Throwable) message).printStackTrace();
               writeDirect(getStackTrace(((Throwable) message)));
               writeDirect("\nCaused by:\n");
               try {
                  ((Exception) message).getCause().printStackTrace();
                  writeDirect(getStackTrace(((Throwable) message).getCause()));
                  mpv5.YabsViewProxy.instance().addMessage(Messages.ERROR_OCCURED + ". " + Messages.SEE_LOG, Color.RED);
               } catch (Exception e) {
               }
            }
            break;
         case LOGLEVEL_NORMAL:
            if (message != null && (message.toString().contains("Exception") || message.toString().contains("Error"))) {
               write(sourcen + ": " + message);
               mpv5.YabsViewProxy.instance().addMessage(Messages.ERROR_OCCURED + ". " + Messages.SEE_LOG, Color.RED);
            }
            break;
         case LOGLEVEL_NONE:
            break;
         default:
            write(sourcen + ": " + message);
      }
   }

   /**
    * Prints messages, regardless the log level
    *
    * @param message
    */
   public static void Print(Object... message) {
      for (int i = 0; i < message.length; i++) {
         Object object = message[i];
         write(object);
         if (!YConsole.CONSOLE_LOG_ENABLED) {
            System.out.println(object);
         }
      }
   }

   /**
    * Print an array
    *
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
    *
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
    *
    * @param string
    */
   public static void PrintArray(Object[] string) {
      write("Print array: {");
      if (loglevel != LOGLEVEL_NONE) {
         write(Arrays.asList(string));
      }
      write("}//End Print array");
   }

   /**
    * Print a list
    *
    * @param lst
    */
   public static void PrintArray(List lst) {
      for (int idx = 0; idx < lst.size(); idx++) {
         write(lst.get(idx));
      }
   }

   private static synchronized void write(final Object obj) {
      Runnable runnable = new Runnable() {
         public void run() {
            for (int i = 0; i < loggers.size(); i++) {
               loggers.get(i).log(obj);
               ;
            }
         }
      };
      Thread thread = new Thread(runnable);
      thread.start();
   }

   private static synchronized void writeDirect(final Object obj) {
      for (int i = 0; i < loggers.size(); i++) {
         try {
            loggers.get(i).log(obj);
         } catch (Exception e) {
            e.printStackTrace();
         }
      }
   }

   /**
    * Debug an Exception
    *
    * @param ex
    */
   public static void Debug(Throwable ex) {
      Debug(Log.class, ex);
   }

   /**
    *
    * @return
    */
   public static List<LogConsole> getLogger() {
      return Collections.unmodifiableList(loggers);
   }

   /**
    * Set the log level<br/> <li>LOGLEVEL_NONE = no logging</li>
    * <li>LOGLEVEL_HIGH = basic logging</li> <li>LOGLEVEL_DEBUG = verbose
    * logging</li>
    *
    * @param level
    */
   public static synchronized void setLogLevel(int level) {
      setLoglevel(level);
   }

   /**
    * Print a table model
    *
    * @param model
    */
   public static void PrintArray(TableModel model) {
      for (int i = 0; i < model.getRowCount(); i++) {
         for (int j = 0; j < model.getColumnCount(); j++) {
            write(model.getValueAt(i, j));
         }
      }
   }

   /**
    *
    */
   public static void PrintArray(Enumeration<String> keys) {
      while (keys.hasMoreElements()) {
         Print(keys.nextElement());
      }
   }

   /**
    * @param aLoglevel the loglevel to set
    */
   public static void setLoglevel(int aLoglevel) {
      loglevel = aLoglevel;
   }

   /**
    * @param aLogger the logger to set
    */
   public static void setLogger(LogConsole aLogger) {
      loggers.clear();
      loggers.add(aLogger);
   }

   public static void addLogger(LogConsole logConsole) {
      loggers.add(logConsole);
   }

   public static void removeLogger(LogConsole logConsole) {
      loggers.remove(logConsole);
   }

   /**
    *
    * @return
    */
   public static boolean isDebugging() {
      return loglevel == LOGLEVEL_DEBUG;
   }

   public static void throwEx(Object o) {
      throw new RuntimeException(String.valueOf(o));
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
    *
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
