package mp4.plugin;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import mp4.einstellungen.Programmdaten;
import mp4.frames.mainframe;
import mp4.globals.Constants;
import mp4.items.visual.Popup;
import mp4.logs.Log;
import mp4.utils.files.FileDirectoryHandler;

/**
 *
 * @author anti43
 */
public class pluginHandler {

    private mpplugin[] plugins;
    private mainframe frame;

    public pluginHandler(mainframe frame) {

        this.frame = frame;

        try {
            plugins = getPlugins();
            loadplugins(plugins);
        } catch (Exception ex) {
            Popup.error("Fehler beim Laden des/der Plugin(s)", "Plugin Fehler");
            Log.Debug(this,ex);
        }

    }

    private mpplugin[] getPlugins() throws MalformedURLException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        @SuppressWarnings("unchecked")
        ArrayList<mpplugin> list = new ArrayList();
        File[] jars = FileDirectoryHandler.getFilesOfDirectory(Programmdaten.instanceOf().getPLUGIN_FOLDER(), Constants.PLUGIN_IDENTIFIER);

        for (int i = 0; i < jars.length; i++) {
            URL[] urls = {new URL("jar:file:" + jars[i].getPath() + "!/")};
            URLClassLoader loader = URLClassLoader.newInstance(urls);

            Class c = loader.loadClass(Constants.PLUGIN_LOAD_CLASS);
            Object o = c.newInstance();
            mpplugin m = (mpplugin) o;
            list.add(m);
        }

        return list.toArray(new mpplugin[0]);
    }

    private void loadplugins(mpplugin[] plugins) {

        for (int i = 0; i < plugins.length; i++) {
            mpplugin elem = plugins[i];
//            elem.load(frame);
//            elem.unload();
            frame.addPlugin(elem);
            if (Programmdaten.instanceOf().getONLOAD(elem)) {
                frame.addPluginPanel(elem);
            }
        }
    }
}
