/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package mpv5.db.common;

import mpv5.handler.FormatHandler;

/**
 *
 * @author Andreas
 */
public interface Templateable {

    public int templateType();

    public int templateGroupIds();
    
    public FormatHandler getFormatHandler();
}
