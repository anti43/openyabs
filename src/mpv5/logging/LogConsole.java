/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mpv5.logging;

import javax.swing.JComponent;

/**
 *
 * @author anti
 */
public interface LogConsole {

    /**
     * Append a new line to the logging object
     */
    public void log();

    /**
     * Log the String value of the given Object
     * @param object Null objects will lead to the String "NULL"
     */
    public void log(final Object object);

    /**
     * Flush the console contents
     */
    public void flush();

    public JComponent open();
}
