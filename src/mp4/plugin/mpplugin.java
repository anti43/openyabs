
package mp4.plugin;

import mp4.frames.mainframe;
import mp4.panels.misc.commonPanel;

/**
 *
 * @author anti43
 */
public abstract class mpplugin extends commonPanel {

    public abstract commonPanel load(mainframe frame);
    public abstract void unload();
    @Override
    public abstract String getName();
    public abstract String getVendor();
    public abstract Long getUID();
    
}
