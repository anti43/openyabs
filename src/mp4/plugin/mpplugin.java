
package mp4.plugin;

import mp4.panels.misc.commonPanel;

/**
 *
 * @author anti43
 */
public abstract class mpplugin extends commonPanel {

    public abstract commonPanel load();
    public abstract void unload();
    @Override
    public abstract String getName();
    public abstract String getVendor();
    
}
