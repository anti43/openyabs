package mpv5.utils.reflection;

import java.net.URL;
import java.net.URLClassLoader;

public class Java10UrlClassloader extends URLClassLoader {

    public Java10UrlClassloader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
    }

    public void addURL(URL url) {
        super.addURL(url);
    }
}
