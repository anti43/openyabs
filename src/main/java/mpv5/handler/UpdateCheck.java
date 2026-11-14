/*
 This file is part of YaBS.

 YaBS is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 YaBS is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with YaBS.  If not, see <http://www.gnu.org/licenses/>.
 */
package mpv5.handler;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import mpv5.globals.LocalSettings;
import mpv5.globals.VersionInfo;
import mpv5.logging.Log;

/**
 * Looks for a newer release of YaBS on GitHub (the latest published
 * release, no pre-releases). One request to api.github.com; nothing about
 * the user or the data is sent. Switched off in the setup assistant or in the
 * local settings ({@link LocalSettings#SUPPRESS_UPDATE_CHECK}).
 */
public final class UpdateCheck {

    /**
     * The latest published release of the project
     */
    public static final String LATEST = "https://api.github.com/repos/anti43/openyabs/releases/latest";
    /**
     * All releases (the download page)
     */
    public static final String RELEASES_PAGE = "https://github.com/anti43/openyabs/releases";

    private UpdateCheck() {
    }

    /**
     * A release on GitHub
     */
    public static final class Release {

        /**
         * e.g. "2.0.3" (the tag without "v")
         */
        public final String version;
        public final String name;
        public final String url;

        public Release(String version, String name, String url) {
            this.version = version;
            this.name = name;
            this.url = url;
        }
    }

    /**
     * @return true if the check at start is switched on and this is a
     * release build (master and local builds have their own numbers)
     */
    public static boolean enabledAtStart() {
        return VersionInfo.isRelease() && isSwitchedOn();
    }

    /**
     * @return the setting of this computer (on unless switched off)
     */
    public static boolean isSwitchedOn() {
        return !LocalSettings.getBooleanProperty(LocalSettings.SUPPRESS_UPDATE_CHECK);
    }

    /**
     * Asks GitHub for the latest release
     *
     * @return the release, null if none could be read (offline, no release)
     */
    public static Release latest() {
        HttpURLConnection con = null;
        try {
            con = (HttpURLConnection) new URL(LATEST).openConnection();
            con.setConnectTimeout(5000);
            con.setReadTimeout(5000);
            con.setRequestProperty("Accept", "application/vnd.github+json");
            //a generic client name, nothing about the user
            con.setRequestProperty("User-Agent", "YaBS-update-check");
            if (con.getResponseCode() != HttpURLConnection.HTTP_OK) {
                Log.Debug(UpdateCheck.class, "Update check: HTTP " + con.getResponseCode());
                return null;
            }
            try (InputStream in = con.getInputStream()) {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                byte[] b = new byte[8192];
                int n;
                while ((n = in.read(b)) > 0 && out.size() < 2_000_000) {
                    out.write(b, 0, n);
                }
                return parse(new String(out.toByteArray(), StandardCharsets.UTF_8));
            }
        } catch (Exception e) {
            Log.Debug(UpdateCheck.class, "Update check: " + e.getMessage());
            return null;
        } finally {
            if (con != null) {
                con.disconnect();
            }
        }
    }

    /**
     * @param json the answer of GitHub for a release
     * @return the release, null if it has no version tag (e.g. "release-1.3.2")
     */
    static Release parse(String json) {
        org.json.JSONObject o;
        try {
            o = new org.json.JSONObject(json);
        } catch (Exception e) {
            Log.Debug(UpdateCheck.class, "Update check: " + e.getMessage());
            return null;
        }
        if (o.optBoolean("draft") || o.optBoolean("prerelease")) {
            return null;
        }
        String tag = o.optString("tag_name", "").trim();
        String version = tag.startsWith("v") || tag.startsWith("V") ? tag.substring(1) : tag;
        if (!version.matches("\\d+(\\.\\d+)*")) {
            return null;
        }
        String url = o.optString("html_url", RELEASES_PAGE);
        if (!url.startsWith("https://github.com/")) {
            url = RELEASES_PAGE;
        }
        return new Release(version, o.optString("name", "YaBS " + version), url);
    }

    /**
     * @param available e.g. "2.1.0"
     * @param current e.g. "2.0.3"
     * @return true if available is a higher version number
     */
    public static boolean isNewer(String available, String current) {
        return compare(available, current) > 0;
    }

    /**
     * Compares version numbers part by part ("2.0.10" &gt; "2.0.9"); text
     * after a number ("2.0-dev") is ignored
     */
    static int compare(String a, String b) {
        int[] x = numbers(a);
        int[] y = numbers(b);
        for (int i = 0; i < Math.max(x.length, y.length); i++) {
            int p = i < x.length ? x[i] : 0;
            int q = i < y.length ? y[i] : 0;
            if (p != q) {
                return Integer.compare(p, q);
            }
        }
        return 0;
    }

    private static int[] numbers(String v) {
        String[] parts = (v == null ? "" : v.trim()).split("\\.");
        int[] n = new int[parts.length];
        for (int i = 0; i < parts.length; i++) {
            String digits = parts[i].replaceAll("^(\\d*).*$", "$1");
            try {
                n[i] = digits.isEmpty() ? 0 : Integer.parseInt(digits);
            } catch (NumberFormatException e) {
                n[i] = 0;
            }
        }
        return n;
    }
}
