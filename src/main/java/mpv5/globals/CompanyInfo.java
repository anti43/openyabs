/*
 *  This file is part of YaBS.
 *
 *      YaBS is free software: you can redistribute it and/or modify
 *      it under the terms of the GNU General Public License as published by
 *      the Free Software Foundation, either version 3 of the License, or
 *      (at your option) any later version.
 *
 *      YaBS is distributed in the hope that it will be useful,
 *      but WITHOUT ANY WARRANTY; without even the implied warranty of
 *      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *      GNU General Public License for more details.
 *
 *      You should have received a copy of the GNU General Public License
 *      along with YaBS.  If not, see <http://www.gnu.org/licenses/>.
 */
package mpv5.globals;

import java.util.List;
import mpv5.data.PropertyStore;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.db.common.QueryHandler;
import mpv5.db.common.ReturnValue;
import mpv5.db.objects.User;
import mpv5.logging.Log;
import mpv5.usermanagement.MPSecurityManager;

/**
 * Shared company information ({@code companyinfo.*}) stored globally for all
 * users in {@link GlobalSettings}.
 * <p>
 * For backward compatibility with databases where company data were previously
 * stored per user, reads fall back to the current user's properties (or any
 * existing user's {@code companyinfo.*} rows if {@link GlobalSettings} has none
 * yet) until saved globally.
 */
public final class CompanyInfo {

    public static final String PREFIX = "companyinfo.";

    private CompanyInfo() {
    }

    /**
     * Reads a company info property (key without {@code "companyinfo."}
     * prefix). Global settings take precedence over legacy per-user values.
     *
     * @param key property name without prefix (e.g. {@code "business"}, {@code "country"})
     * @return the value, or {@code ""} if not set
     */
    public static String getProperty(String key) {
        if (key == null || key.isEmpty()) {
            return "";
        }
        String fullKey = key.startsWith(PREFIX) ? key : PREFIX + key;
        if (GlobalSettings.hasProperty(fullKey)) {
            String v = GlobalSettings.getProperty(fullKey);
            return v == null || "null".equals(v) ? "" : v;
        }
        // Fallback to legacy per-user property (current user or existing DB user)
        try {
            User u = User.getCurrentUser();
            if (u != null && u.getProperties().hasProperty(fullKey)) {
                String v = u.getProperty(fullKey);
                if (v != null && !"null".equals(v)) {
                    return v;
                }
            }
        } catch (Throwable ignored) {
        }
        PropertyStore migrated = loadLegacyUserCompanyInfo();
        String shortKey = fullKey.substring(PREFIX.length());
        if (migrated.hasProperty(shortKey)) {
            String v = migrated.getProperty(shortKey);
            return v == null || "null".equals(v) ? "" : v;
        }
        return "";
    }

    /**
     * Returns all company info properties (keys without {@code "companyinfo."}
     * prefix). Global settings override legacy per-user properties.
     *
     * @return PropertyStore of company info entries
     */
    public static PropertyStore getProperties() {
        PropertyStore result = new PropertyStore();
        PropertyStore global = GlobalSettings.getProperties(PREFIX);
        boolean hasGlobal = global != null && !global.getList().isEmpty();

        // Base: legacy per-user properties (current user, or any existing user if global is empty)
        if (!hasGlobal) {
            PropertyStore legacy = loadLegacyUserCompanyInfo();
            addAll(result, legacy);
        }
        try {
            User u = User.getCurrentUser();
            if (u != null) {
                addAll(result, u.getProperties().getProperties(PREFIX));
            }
        } catch (Throwable ignored) {
        }
        // Overlay global settings (always win)
        addAll(result, global);
        return result;
    }

    /**
     * Sets a global company info property (key with or without
     * {@code "companyinfo."} prefix). Call {@link #save()} to persist.
     */
    public static void setProperty(String key, String value) {
        if (key == null || key.isEmpty()) {
            return;
        }
        String fullKey = key.startsWith(PREFIX) ? key : PREFIX + key;
        GlobalSettings.setProperty(fullKey, value == null ? "" : value);
    }

    /**
     * Persists global company info to the database and invalidates cached form
     * fields so open documents immediately pick up the updated company data.
     */
    public static void save() {
        GlobalSettings.save();
        DatabaseObject.invalidateFormFieldsCache();
    }

    private static void addAll(PropertyStore target, PropertyStore source) {
        if (source == null) {
            return;
        }
        List<String[]> list = source.getList();
        if (list == null) {
            return;
        }
        for (String[] kv : list) {
            if (kv != null && kv.length >= 2 && kv[0] != null && kv[1] != null && !"null".equals(kv[1])) {
                target.changeProperty(kv[0], kv[1]);
            }
        }
    }

    /**
     * Reads legacy {@code companyinfo.*} entries from {@code usersproperties}
     * (preferring the lowest user ID, typically Admin) when no global company
     * info has been saved yet.
     */
    private static PropertyStore loadLegacyUserCompanyInfo() {
        PropertyStore ps = new PropertyStore();
        try {
            if (mpv5.db.common.DatabaseConnection.getConnector() == null) {
                return ps;
            }
            ReturnValue rv = QueryHandler.instanceOf().clone(Context.getUserProperties())
                    .freeSelectQuery("SELECT cname, value FROM userproperties WHERE cname LIKE 'companyinfo.%' ORDER BY usersids DESC",
                            MPSecurityManager.VIEW, null);
            if (rv != null && rv.hasData()) {
                for (Object[] row : rv.getData()) {
                    if (row.length >= 2 && row[0] != null && row[1] != null) {
                        String k = String.valueOf(row[0]);
                        if (k.startsWith(PREFIX)) {
                            String val = String.valueOf(row[1]);
                            if (!"null".equals(val)) {
                                ps.changeProperty(k.substring(PREFIX.length()), val);
                            }
                        }
                    }
                }
            }
        } catch (Throwable t) {
            Log.Debug(CompanyInfo.class, t.getMessage());
        }
        return ps;
    }
}
