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
package mp4.logs;

import java.io.IOException;
import java.util.logging.Level;
import mp4.frames.Logger;

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
            try {
                logger.log();
            } catch (IOException ex) {
                java.util.logging.Logger.getLogger(Log.class.getName()).log(Level.SEVERE, null, ex);
            }
            for (int k = 0; k < array[i].length; k++) {
                try {
                    logger.log();
                } catch (IOException ex) {
                    java.util.logging.Logger.getLogger(Log.class.getName()).log(Level.SEVERE, null, ex);
                }
                for (int f = 0; f < array[i][k].length; f++) {
                    if (loglevel != LOGLEVEL_LOW) {
                        try {
                            logger.log(array[i][k][f] + " ");
                        } catch (IOException ex) {
                            java.util.logging.Logger.getLogger(Log.class.getName()).log(Level.SEVERE, null, ex);
                        }
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
                    try {
                        logger.log("[" + i + "]" + " [" + k + "] " + array[i][k]);
                    } catch (IOException ex) {
                        java.util.logging.Logger.getLogger(Log.class.getName()).log(Level.SEVERE, null, ex);
                    }
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
                try {
                    logger.log(string[i]);
                } catch (IOException ex) {
                    java.util.logging.Logger.getLogger(Log.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        }
    }

    public static void Debug(Object obj) {
        if (loglevel != LOGLEVEL_LOW) {
            logger.setVisible(true);
        }
        if (loglevel != LOGLEVEL_LOW) {
            try {
                logger.log(obj);
            } catch (IOException ex) {
                java.util.logging.Logger.getLogger(Log.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public static void Debug(Exception ex) {
        if (loglevel != LOGLEVEL_LOW) {
            logger.setVisible(true);
        }
        if (loglevel != LOGLEVEL_LOW) {
            try {
                logger.log(ex.getMessage() + "\n" + ex.getCause());
            } catch (IOException ex1) {
                java.util.logging.Logger.getLogger(Log.class.getName()).log(Level.SEVERE, null, ex1);
            }
            ex.printStackTrace();
        }
    }

    public static void Debug(Exception ex, boolean konsole) {
        if (konsole) {
            try {
                logger.log(ex.getMessage() + "\n" + ex.getCause());
            } catch (IOException ex1) {
                java.util.logging.Logger.getLogger(Log.class.getName()).log(Level.SEVERE, null, ex1);
            }
            ex.printStackTrace();
        } else {
            Debug(ex.getMessage() + "\n" + ex.getCause());
        }
    }

    public static void Debug(Object string, boolean konsole) {
        if (konsole) {
            try {
                logger.log(string);
            } catch (IOException ex) {
                java.util.logging.Logger.getLogger(Log.class.getName()).log(Level.SEVERE, null, ex);
            }
            if (loglevel != LOGLEVEL_LOW) {
                System.out.println(string);
            }
        } else {
            Debug(string);
        }
    }

    public static void debug(char cChar) {
        if (loglevel != LOGLEVEL_LOW) {
            try {
                logger.log(cChar);
            } catch (IOException ex) {
                java.util.logging.Logger.getLogger(Log.class.getName()).log(Level.SEVERE, null, ex);
            }
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
                    try {
                        logger.log("[" + i + "]" + " [" + k + "] " + array[i][k]);
                    } catch (IOException ex) {
                        java.util.logging.Logger.getLogger(Log.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }
    }

    public static void setLogLevel(int level) {
        Log.loglevel = level;
    }

    private Log() {
    }

    public static int getLoglevel() {
        return loglevel;
    }

}
