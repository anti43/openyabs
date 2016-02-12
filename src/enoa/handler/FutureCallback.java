/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package enoa.handler;

/**
 *
 * @author andreas
 */
public interface FutureCallback<T> {

    public void call(T t);
}
