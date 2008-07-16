/*
 *  This file is part of MP by anti43 /GPL.
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
package mp3.classes.utils;

/**
 *
 * @author anti43
 */
public class Log {

    public static final int LOGLEVEL_LOW = 0;
    public static final int LOGLEVEL_HIGH = 1;
    private static int loglevel = 1;
    private static Logger logger = new Logger();

    public static void PrintArray(Object[][][] array) {

        if (loglevel != LOGLEVEL_LOW) {
            logger.setVisible(true);
        }

        for (int i = 0; i < array.length; i++) {
            logger.log();
            for (int k = 0; k < array[i].length; k++) {
                logger.log();
                for (int f = 0; f < array[i][k].length; f++) {
                    if (loglevel != LOGLEVEL_LOW) {
                        logger.log(array[i][k][f] + " ");
                    }
                }
            }
        }
    }

    public static void PrintArray(Object[][] array) {
        if (loglevel != LOGLEVEL_LOW) {
            logger.setVisible(true);
        }
        for (int i = 0; i < array.length; i++) {
            for (int k = 0; k < array[i].length; k++) {
                if (loglevel != LOGLEVEL_LOW) {
                    logger.log("[" + i + "]" + " [" + k + "] " + array[i][k]);
                }
            }
        }
    }

    public static void PrintArray(Object[] string) {
        if (loglevel != LOGLEVEL_LOW) {
            logger.setVisible(true);
        }
        for (int i = 0; i < string.length; i++) {

            if (loglevel != LOGLEVEL_LOW) {
                logger.log(string[i]);
            }

        }
    }

    public static void Debug(Object string) {
        if (loglevel != LOGLEVEL_LOW) {
            logger.setVisible(true);
        }
        if (loglevel != LOGLEVEL_LOW) {
            logger.log(string);
        }
    }

    public static void Debug(Exception string) {
        if (loglevel != LOGLEVEL_LOW) {
            logger.setVisible(true);
        }
        if (loglevel != LOGLEVEL_LOW) {
            logger.log(string.getMessage() + "\n" + string.getCause());
            string.printStackTrace();
        }
    }

    public static void Debug(Exception ex, boolean konsole) {
        if (konsole) {
            logger.log(ex.getMessage() + "\n" + ex.getCause());
            ex.printStackTrace();
        } else {
            Debug(ex.getMessage() + "\n" + ex.getCause());
        }
    }

    public static void Debug(String string, boolean konsole) {
        if (konsole) {
            logger.log(string);
            System.out.println(string);
        } else {
            Debug(string);
        }
    }

    public static void debug(char cChar) {
        if (loglevel != LOGLEVEL_LOW) {
            logger.log(cChar);
        }
    }

    public static Logger getLogger() {


        return logger;
    }

    public static void Debug(String[][] array) {
        if (loglevel != LOGLEVEL_LOW) {
            logger.setVisible(true);
        }
        for (int i = 0; i < array.length; i++) {
            for (int k = 0; k < array[i].length; k++) {
                if (loglevel != LOGLEVEL_LOW) {
                    logger.log("[" + i + "]" + " [" + k + "] " + array[i][k]);
                }
            }
        }
    }

    public static void setLogLevel(int level) {
        Log.loglevel = level;
    }

    private Log() {
    }

    public int getLoglevel() {
        return loglevel;
    }

    public void setLoglevel(int loglevel) {
        Log.loglevel = loglevel;
    }
}
