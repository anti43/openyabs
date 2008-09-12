package mp4.plugin;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import mp4.einstellungen.Programmdaten;
import mp4.frames.mainframe;
import mp4.globals.Constants;
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
        } catch (MalformedURLException ex) {
            Logger.getLogger(pluginHandler.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(pluginHandler.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            Logger.getLogger(pluginHandler.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(pluginHandler.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private mpplugin[] getPlugins() throws MalformedURLException, ClassNotFoundException, InstantiationException, IllegalAccessException {

        ArrayList<mpplugin> list = new ArrayList();
        File[] jars = FileDirectoryHandler.getFilesOfDirectory(Programmdaten.instanceOf().getPLUGIN_FOLDER(), Constants.PLUGIN_IDENTIFIER);

        for (int i = 0; i < jars.length; i++) {
            URL[] urls = {new URL("jar:file:" + jars[i].getPath() + "!/")};
            URLClassLoader loader =  URLClassLoader.newInstance(urls);

            Class c = loader.loadClass("plugin.Main");

            // Create an instance of the class just loaded
            Object o = c.newInstance();
            mpplugin m = (mpplugin) o;


            list.add(m);

        }

        return (mpplugin[]) list.toArray(new mpplugin[0]);
        
    }

    private void loadplugins(mpplugin[] plugins) {
        
        for (int i = 0; i < plugins.length; i++) {
            mpplugin elem = plugins[i];
            elem.load();
            frame.addPluginPanel(elem);
        }
    }
}
