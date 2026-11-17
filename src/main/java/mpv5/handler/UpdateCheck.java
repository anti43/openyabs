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
     * @return true if the check at start is switched on; builds made on
     * the own computer ("2.0-dev") do not check
     */
    public static boolean enabledAtStart() {
        return isSwitchedOn() && !isLocalBuild(VersionInfo.number());
    }

    /**
     * @param version
     * @return true for a build made on the own computer (no version from the
     * build server)
     */
    static boolean isLocalBuild(String version) {
        return version == null || version.trim().endsWith("-dev");
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
        //always in the log (any log level): when YaBS goes online, and what it got
        Log.Print("Update check: GET " + LATEST);
        try {
            con = (HttpURLConnection) new URL(LATEST).openConnection();
            con.setConnectTimeout(5000);
            con.setReadTimeout(5000);
            con.setRequestProperty("Accept", "application/vnd.github+json");
            //a generic client name, nothing about the user
            con.setRequestProperty("User-Agent", "YaBS-update-check");
            if (con.getResponseCode() != HttpURLConnection.HTTP_OK) {
                Log.Print("Update check: GitHub answered HTTP " + con.getResponseCode());
                return null;
            }
            try (InputStream in = con.getInputStream()) {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                byte[] b = new byte[8192];
                int n;
                while ((n = in.read(b)) > 0 && out.size() < 2_000_000) {
                    out.write(b, 0, n);
                }
                Release r = parse(new String(out.toByteArray(), StandardCharsets.UTF_8));
                Log.Print("Update check: latest release " + (r == null ? "none with a version number" : r.version)
                        + ", this version " + VersionInfo.number()
                        + (r != null && isNewer(r.version, VersionInfo.number()) ? " - newer version available" : " - up to date"));
                return r;
            }
        } catch (Exception e) {
            Log.Print("Update check: GitHub not reached (" + e.getClass().getSimpleName() + ": " + e.getMessage() + ")");
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
     * @param current e.g. "2.0.3" or "2.0.4-dev.250"
     * @return true if available is a higher version (semantic versioning)
     */
    public static boolean isNewer(String available, String current) {
        return compare(available, current) > 0;
    }

    /**
     * Compares two versions by semantic versioning: the numbers part by part
     * ("2.0.10" &gt; "2.0.9"), a pre-release before its release
     * ("2.0.4-dev.250" &lt; "2.0.4", but &gt; "2.0.3"); build metadata
     * ("+...") does not count
     */
    public static int compare(String a, String b) {
        String[] x = split(a);
        String[] y = split(b);
        int c = compareCore(x[0], y[0]);
        if (c != 0) {
            return c;
        }
        //same numbers: a version without pre-release is higher
        if (x[1].isEmpty() || y[1].isEmpty()) {
            return x[1].isEmpty() == y[1].isEmpty() ? 0 : (x[1].isEmpty() ? 1 : -1);
        }
        return comparePreRelease(x[1], y[1]);
    }

    /**
     * @return [numbers, pre-release] ("2.0.4-dev.250" -> ["2.0.4", "dev.250"])
     */
    private static String[] split(String v) {
        String s = v == null ? "" : v.trim();
        if (s.startsWith("v") || s.startsWith("V")) {
            s = s.substring(1);
        }
        int plus = s.indexOf('+');
        if (plus >= 0) {
            s = s.substring(0, plus);
        }
        int dash = s.indexOf('-');
        return dash < 0 ? new String[]{s, ""} : new String[]{s.substring(0, dash), s.substring(dash + 1)};
    }

    private static int compareCore(String a, String b) {
        String[] x = a.split("\\.");
        String[] y = b.split("\\.");
        for (int i = 0; i < Math.max(x.length, y.length); i++) {
            long p = i < x.length ? number(x[i]) : 0;
            long q = i < y.length ? number(y[i]) : 0;
            if (p != q) {
                return Long.compare(p, q);
            }
        }
        return 0;
    }

    private static int comparePreRelease(String a, String b) {
        String[] x = a.split("\\.");
        String[] y = b.split("\\.");
        for (int i = 0; i < Math.min(x.length, y.length); i++) {
            boolean xn = x[i].matches("\\d+");
            boolean yn = y[i].matches("\\d+");
            int c;
            if (xn && yn) {
                c = Long.compare(number(x[i]), number(y[i]));
            } else if (xn != yn) {
                //numeric identifiers are lower than alphanumeric ones
                c = xn ? -1 : 1;
            } else {
                c = x[i].compareTo(y[i]);
            }
            if (c != 0) {
                return c;
            }
        }
        return Integer.compare(x.length, y.length);
    }

    private static long number(String s) {
        String digits = s.replaceAll("^(\\d*).*$", "$1");
        try {
            return digits.isEmpty() ? 0 : Long.parseLong(digits);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
