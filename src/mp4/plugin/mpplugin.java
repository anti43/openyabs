
package mp4.plugin;

import mp4.frames.mainframe;
import mp4.items.visual.CommonPanel;

/**
 *
 * @author anti43
 */
public abstract class mpplugin extends CommonPanel {
    public abstract CommonPanel load(mainframe frame);
    public abstract void unload();
    @Override
    public abstract String getName();
    public abstract String getVendor();
    public abstract Long getUID(); 
}
