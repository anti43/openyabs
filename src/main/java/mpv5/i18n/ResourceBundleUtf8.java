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
                String value = repair(valueRaw);

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
     * Repairs a text whose UTF-8 bytes were read as ISO-8859-1 ("ZurÃ¼ck" ->
     * "Zur\u00fcck"; old language files and Java 8 read them so). A text that is
     * already correct ("gem\u00e4\u00df") stays as it is: since Java 9 property
     * files are read as UTF-8, and decoding every text again broke the umlauts.
     *
     * @param raw the text as read
     * @return the repaired text
     */
    public static String repair(String raw) {
        if (raw == null) {
            return null;
        }
        boolean candidate = false;
        for (int i = 0; i < raw.length(); i++) {
            char c = raw.charAt(i);
            if (c > 0xFF) {
                return raw;//real characters beyond Latin-1: not misread bytes
            }
            if (c >= 0x80) {
                candidate = true;
            }
        }
        if (!candidate) {
            return raw;
        }
        java.nio.charset.CharsetDecoder d = java.nio.charset.StandardCharsets.UTF_8.newDecoder()
                .onMalformedInput(java.nio.charset.CodingErrorAction.REPORT)
                .onUnmappableCharacter(java.nio.charset.CodingErrorAction.REPORT);
        try {
            return d.decode(java.nio.ByteBuffer.wrap(raw.getBytes(java.nio.charset.StandardCharsets.ISO_8859_1))).toString();
        } catch (java.nio.charset.CharacterCodingException e) {
            return raw;//not UTF-8 bytes: the text is correct
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
