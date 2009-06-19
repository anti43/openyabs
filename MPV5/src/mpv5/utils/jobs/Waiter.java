/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package mpv5.utils.jobs;

/**
 *
 *  
 */
public interface Waiter {
    /**
     * 
     * @param object
     * @param exception
     * @throws Exception
     */
    public abstract void set(Object object, Exception exception) throws Exception;
}
