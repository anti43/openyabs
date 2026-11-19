package mpv5.db.common;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import mpv5.db.objects.ValueProperty;
import mpv5.globals.GlobalSettings;
import mpv5.logging.Log;

/**
 * The fields of a record in a Groovy script, named as in the templates: in a
 * contact script <code>contact.cname</code>, <code>contact.ismale</code>, in a
 * document script <code>item.cnumber</code>, <code>item.contact.cname</code>,
 * <code>item.contact.property.anrede</code>.
 * <p>
 * Values are the raw values (a boolean is <code>true</code>/<code>false</code>,
 * a number a number), not the formatted text of the template. A referenced
 * record (<code>item.contact</code> for <code>contactsids</code>) is again a
 * ScriptFields, <code>property</code> holds the record's own fields (scripts
 * among them are run).
 */
public class ScriptFields extends AbstractMap<String, Object> {

    /**
     * Nesting of property scripts that read property scripts, against loops
     */
    private static final ThreadLocal<Integer> DEPTH = ThreadLocal.withInitial(() -> 0);
    private static final int MAX_DEPTH = 8;
    private final DatabaseObject owner;

    /**
     * @param owner the record
     */
    public ScriptFields(DatabaseObject owner) {
        this.owner = owner;
    }

    /**
     * @return the current values (not cached: the script shell of a record
     * lives as long as the record, its values change)
     */
    private Map<String, Object> fieldValues() {
        Map<String, Object> values = new HashMap<>();
        for (Object[] v : owner.getValues2()) {
            values.put(String.valueOf(v[0]), v[1]);
        }
        return values;
    }

    @Override
    public boolean containsKey(Object key) {
        return get(key) != null;
    }

    @Override
    public Object get(Object key) {
        if (key == null) {
            return null;
        }
        String k = key.toString().toLowerCase();
        if ("property".equals(k) || "properties".equals(k)) {
            return new Properties(owner);
        }
        Map<String, Object> v = fieldValues();
        if (v.containsKey(k)) {
            return v.get(k);
        }
        //a reference: contact for contactsids, group for groupsids ...
        Object id = v.get(k + "sids");
        if (id instanceof Number && ((Number) id).intValue() > 0) {
            Context c = Context.getMatchingContext(k + "s");
            if (c == null && k.startsWith("original")) {
                c = Context.getMatchingContext(k.substring("original".length()) + "s");
            }
            if (c != null) {
                try {
                    return new ScriptFields(DatabaseObject.getObject(c, ((Number) id).intValue()));
                } catch (Exception e) {
                    Log.Debug(ScriptFields.class, e.getMessage());
                }
            }
        }
        return null;
    }

    @Override
    public Set<Entry<String, Object>> entrySet() {
        return new HashSet<>(fieldValues().entrySet());
    }

    /**
     * @return the record
     */
    public DatabaseObject getDbo() {
        return owner;
    }

    @Override
    public String toString() {
        return String.valueOf(owner);
    }

    /**
     * The variables of the record as a script writes them, e.g.
     * <code>item.cnumber</code>, <code>item.contact.cname</code>,
     * <code>item.contact.property.anrede</code>: the fields, the own fields and
     * one level of referenced records
     *
     * @param prefix e.g. "item"
     * @return rows variable, value, sorted
     */
    public java.util.List<String[]> describe(String prefix) {
        return describe(prefix, 1);
    }

    private java.util.List<String[]> describe(String prefix, int depth) {
        java.util.TreeMap<String, String> rows = new java.util.TreeMap<>();
        Map<String, Object> v = fieldValues();
        for (Map.Entry<String, Object> e : v.entrySet()) {
            String k = e.getKey();
            if (k.endsWith("sids") && e.getValue() instanceof Number && ((Number) e.getValue()).intValue() > 0 && depth > 0) {
                Object ref = get(k.substring(0, k.length() - "sids".length()));
                if (ref instanceof ScriptFields) {
                    for (String[] r : ((ScriptFields) ref).describe(prefix + "." + k.substring(0, k.length() - 4), depth - 1)) {
                        rows.put(r[0], r[1]);
                    }
                }
            }
            rows.put(prefix + "." + k, shorten(e.getValue()));
        }
        Properties p = new Properties(owner);
        for (Map.Entry<String, Object> e : p.entrySet()) {
            rows.put(prefix + ".property." + e.getKey(), shorten(p.get(e.getKey())));
        }
        java.util.List<String[]> l = new java.util.ArrayList<>();
        for (Map.Entry<String, String> e : rows.entrySet()) {
            l.add(new String[]{e.getKey(), e.getValue()});
        }
        return l;
    }

    private static String shorten(Object o) {
        String s = o == null ? "" : String.valueOf(o);
        return s.length() > 200 ? s.substring(0, 200) + "\u2026" : s;
    }

    /**
     * The own fields (properties) of a record: of the record itself and those
     * of the script manager for its kind and group
     */
    static class Properties extends AbstractMap<String, Object> {

        private final DatabaseObject owner;

        Properties(DatabaseObject owner) {
            this.owner = owner;
        }

        private List<ValueProperty> all() {
            //as in the templates: the script manager's fields, then the record's own (they win)
            List<ValueProperty> props = new java.util.ArrayList<>();
            try {
                props.addAll(ScriptScope.scripts(owner));
            } catch (Exception e) {
                Log.Debug(ScriptFields.class, e.getMessage());
            }
            if (owner.__getIDS() > 0) {
                props.addAll(ValueProperty.getProperties(owner));
            }
            java.util.Collections.reverse(props);
            return props;
        }

        @Override
        public boolean containsKey(Object key) {
            return get(key) != null;
        }

        @Override
        public Object get(Object key) {
            if (key == null) {
                return null;
            }
            for (ValueProperty p : all()) {
                if (p.getKey().equalsIgnoreCase(key.toString())) {
                    return value(p);
                }
            }
            return null;
        }

        private Object value(ValueProperty p) {
            Object v = p.getValue();
            String s = String.valueOf(v);
            String sm = GlobalSettings.getProperty("org.openyabs.config.scriptsymbol", "#");
            if (s.length() > 1 && s.startsWith(sm) && s.endsWith(sm)) {
                int d = DEPTH.get();
                if (d >= MAX_DEPTH) {
                    Log.Debug(ScriptFields.class, "Property scripts nested too deep: " + p.getKey());
                    return null;
                }
                DEPTH.set(d + 1);
                try {
                    return owner.evaluate(s);
                } finally {
                    DEPTH.set(d);
                }
            }
            return v;
        }

        @Override
        public Set<Entry<String, Object>> entrySet() {
            Map<String, Object> m = new HashMap<>();
            for (ValueProperty p : all()) {
                m.put(p.getKey(), p.getValue());
            }
            return m.entrySet();
        }
    }
}
