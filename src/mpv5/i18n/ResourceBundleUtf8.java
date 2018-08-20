package mpv5.i18n;

import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import mpv5.Main;
import mpv5.logging.Log;

/**
 * Same as {@link ResourceBundle} but is target for UTF- property file resources.
 * May not be used with -bit ASCII - only with UTF-.
 *
 * @author Marc Neumann
 */
public class ResourceBundleUtf8 {

    private static class PropertyResourceBundleUtf extends ResourceBundle {

        private final Map<String, String> valueByKey = new HashMap<String, String>();
        private final PropertyResourceBundle bundle;

        private PropertyResourceBundleUtf(PropertyResourceBundle pBundle) {
            this.bundle = pBundle;
            loadEntries(pBundle, valueByKey);
        }

        /**
         * @see java.util.ResourceBundle#getKeys()
         */
        @Override
        public Enumeration<String> getKeys() {
            return Collections.enumeration(valueByKey.keySet());
        }

        private void loadEntries(PropertyResourceBundle pBundle, Map<String, String> pValueByKey) {
            for (Enumeration<String> keys = pBundle.getKeys(); keys.hasMoreElements();) {
                String key = keys.nextElement();
                String valueRaw = pBundle.getString(key);
                String value = null;

                try {
                    value = new String(valueRaw.getBytes("ISO-8859-1"), "UTF-8");

                } catch (UnsupportedEncodingException e) {
                    Log.Debug(this, "could not load UTF- property resource bundle [" + pBundle + "]");
                }

                if (pValueByKey.put(key, value) != null) {
                    Log.Debug(this, "duplicate key [" + key + "] in UTF- property resource bundle [" + pBundle + "]");
                }
            }
        }

        /**
         * @see java.util.ResourceBundle#handleGetObject(java.lang.String)
         */
        @Override
        protected Object handleGetObject(String pKey) {
            if (valueByKey.containsKey(pKey)) {
                return valueByKey.get(pKey);
            } else {
                try {
                    Log.Debug(this, "Key missing in " + bundle + " : " + pKey);
                    return getBundle(LanguageManager.defLanguageBundleName).getObject(pKey);
                } catch (Exception e) {
                    return pKey;
                }
            }
        }
    }

    /**
     * @see ResourceBundle#getBundle(String)
     */
    public static ResourceBundle getBundle(String pBaseName) {
        ResourceBundle bundle = ResourceBundle.getBundle(pBaseName, Locale.getDefault(), Main.classLoader);
        return createUtfPropertyResourceBundle(bundle);
    }

    /**
     * @see ResourceBundle#getBundle(String, Locale)
     */
    public static final ResourceBundle getBundle(String pBaseName, Locale pLocale) {
        ResourceBundle bundle = ResourceBundle.getBundle(pBaseName, pLocale, Main.classLoader);
        return createUtfPropertyResourceBundle(bundle);
    }

    /**
     * @see ResourceBundle#getBundle(String, Locale, ClassLoader)
     */
    /*  private static Map<ClassLoader, Map<String, Map<Locale, ResourceBundle>>> bundleByClassLoaderByBaseNameByLocale =
            new HashMap<ClassLoader, Map<String, Map<Locale, ResourceBundle>>>();
public static ResourceBundle getBundle(String pBaseName, Locale pLocale, ClassLoader pLoader) {

        Map<String, Map<Locale, ResourceBundle>> bundleByBaseNameByLocale;
        Map<Locale, ResourceBundle> bundleByLocale = null;
        ResourceBundle bundle = null;

        synchronized (bundleByClassLoaderByBaseNameByLocale) {
            bundleByBaseNameByLocale = bundleByClassLoaderByBaseNameByLocale.get(pLoader);
            if (bundleByBaseNameByLocale == null) {
                bundleByBaseNameByLocale = new HashMap<String, Map<Locale, ResourceBundle>>();
                bundleByClassLoaderByBaseNameByLocale.put(pLoader, bundleByBaseNameByLocale);
            }
        }

        synchronized (bundleByBaseNameByLocale) {
            bundleByLocale = bundleByBaseNameByLocale.get(pBaseName);
            if (bundleByLocale == null) {
                bundleByLocale = new HashMap<Locale, ResourceBundle>();
                bundleByBaseNameByLocale.put(pBaseName, bundleByLocale);
            }
        }

        synchronized (bundleByLocale) {
            bundle = bundleByLocale.get(pLocale);
            if (bundle == null) {
                bundle = ResourceBundle.getBundle(pBaseName, pLocale);
                bundle = createUtfPropertyResourceBundle(bundle);
                bundleByLocale.put(pLocale, bundle);
            }
        }

        return bundle;
    }
*/
    private static ResourceBundle createUtfPropertyResourceBundle(ResourceBundle pBundle) {
        if (!(pBundle instanceof PropertyResourceBundle)) {
            Log.Debug(ResourceBundleUtf8.class, "only UTF- property files are supported");
        }

        return new PropertyResourceBundleUtf((PropertyResourceBundle) pBundle);
    }
}
