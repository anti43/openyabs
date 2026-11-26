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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import mpv5.globals.VersionInfo;
import mpv5.logging.Log;
import mpv5.plugin.ExtensionManager;
import mpv5.plugin.ExtensionManager.Installed;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Looks for newer versions of the installed plugins on GitHub.
 * <ul>
 * <li>The plugins made with YaBS (plugins/ of the YaBS repository) are
 * compared with the plugin list {@value #INDEX_ASSET} of the latest YaBS
 * release; an update is taken from the plugins zip of that release (checked
 * against its SHA256SUMS).</li>
 * <li>Other plugins name their own GitHub repository
 * ({@link mpv5.plugin.api.YabsExtension#updateSource()}); its latest release
 * tag is the version, its jar asset the update.</li>
 * </ul>
 * Only requests to api.github.com (and the downloads of github.com when an
 * update is installed); nothing about the user or the data is sent. Uses the
 * switch of the YaBS update check ({@link UpdateCheck#isSwitchedOn()}).
 */
public final class PluginUpdateCheck {

    /**
     * The plugin list asset of a YaBS release (written by the build, task
     * pluginIndex)
     */
    public static final String INDEX_ASSET = "openyabs-plugins.json";
    /**
     * The checksums asset of a YaBS release
     */
    public static final String SUMS_ASSET = "SHA256SUMS";
    private static final String API = "https://api.github.com/repos/";
    private static final int MAX_JSON = 2_000_000;
    private static final int MAX_DOWNLOAD = 200_000_000;
    private static volatile List<Update> last = Collections.emptyList();

    private PluginUpdateCheck() {
    }

    /**
     * A newer version of an installed plugin
     */
    public static final class Update {

        public final String id;
        public final String name;
        public final String installedVersion;
        public final String version;
        /**
         * The release on github.com (to read what is new)
         */
        public final String page;
        /**
         * Download of the jar, or of the zip that holds it ({@link #zipEntry})
         */
        public final String download;
        /**
         * The jar in the zip of {@link #download}, null if the download is the
         * jar
         */
        public final String zipEntry;
        /**
         * The download of the checksums file of the release, null if there is
         * none
         */
        public final String sums;
        /**
         * The file name of the downloaded asset (for the checksums)
         */
        public final String assetName;
        /**
         * The YaBS version the update needs, null for any
         */
        public final String minYabsVersion;
        /**
         * The installed jar (the update replaces it)
         */
        public final File jar;

        public Update(String id, String name, String installedVersion, String version, String page, String download,
                String zipEntry, String sums, String assetName, String minYabsVersion, File jar) {
            this.id = id;
            this.name = name;
            this.installedVersion = installedVersion;
            this.version = version;
            this.page = page;
            this.download = download;
            this.zipEntry = zipEntry;
            this.sums = sums;
            this.assetName = assetName;
            this.minYabsVersion = minYabsVersion;
            this.jar = jar;
        }

        /**
         * @return true if this YaBS can run the update
         */
        public boolean installable() {
            return download != null && ExtensionManager.isVersionCompatible(VersionInfo.number(), minYabsVersion);
        }

        /**
         * @return "id@version", e.g. to remember a skipped update
         */
        public String key() {
            return id + "@" + version;
        }
    }

    /**
     * A release of a repository: version, page and assets (name to download
     * url)
     */
    static final class Release {

        final String version;
        final String page;
        final Map<String, String> assets;

        Release(String version, String page, Map<String, String> assets) {
            this.version = version;
            this.page = page;
            this.assets = assets;
        }
    }

    /**
     * An entry of the plugin list of a YaBS release
     */
    static final class IndexEntry {

        final String id;
        final String name;
        final String version;
        final String jar;
        final String minYabsVersion;

        IndexEntry(String id, String name, String version, String jar, String minYabsVersion) {
            this.id = id;
            this.name = name;
            this.version = version;
            this.jar = jar;
            this.minYabsVersion = minYabsVersion;
        }
    }

    /**
     * @return the updates found by the last check (empty before)
     */
    public static List<Update> lastResult() {
        return last;
    }

    /**
     * @param updates what is left of the last check (e.g. after installing)
     */
    public static void setLastResult(List<Update> updates) {
        last = Collections.unmodifiableList(new ArrayList<Update>(updates));
    }

    /**
     * @param id
     * @return the update of the plugin found by the last check, null if none
     */
    public static Update lastFor(String id) {
        for (Update u : last) {
            if (u.id.equals(id)) {
                return u;
            }
        }
        return null;
    }

    /**
     * Asks GitHub for newer versions of the plugins of the plugins folder
     * (not in the Swing thread: it goes online)
     *
     * @param plugins the installed plugins
     * @return the updates, null if GitHub could not be reached at all
     */
    public static List<Update> check(List<Installed> plugins) {
        List<Update> found = new ArrayList<Update>();
        boolean reached = false;
        boolean asked = false;
        //the plugins made with YaBS: one look at the latest YaBS release
        Release yabs = null;
        Map<String, IndexEntry> index = null;
        Map<String, Release> repos = new HashMap<String, Release>();
        for (Installed i : plugins) {
            if (i.jar == null || !i.jar.getName().toLowerCase(Locale.ROOT).endsWith(".jar")) {
                //built into YaBS (updated with YaBS) or a Groovy script
                continue;
            }
            String id = i.id();
            String installed = safe(() -> i.extension.version());
            String name = safe(() -> i.extension.name());
            String source = normalizeRepo(safe(() -> i.extension.updateSource()));
            asked = true;
            if (source != null) {
                Release r;
                if (repos.containsKey(source)) {
                    r = repos.get(source);
                } else {
                    r = release(source);
                    repos.put(source, r);
                }
                if (r == null) {
                    continue;
                }
                reached = true;
                if (UpdateCheck.isNewer(r.version, installed)) {
                    String asset = pickJar(r.assets, i.jar.getName());
                    found.add(new Update(id, name, installed, r.version, r.page,
                            asset == null ? null : r.assets.get(asset), null,
                            r.assets.get(SUMS_ASSET), asset, null, i.jar));
                }
                continue;
            }
            if (index == null) {
                yabs = release(repoOfYabs());
                index = yabs == null ? null : index(yabs);
                if (index == null) {
                    index = Collections.emptyMap();
                }
            }
            if (yabs == null) {
                continue;
            }
            reached = true;
            IndexEntry e = index.get(id);
            if (e != null && UpdateCheck.isNewer(e.version, installed)) {
                String zip = "openyabs-plugins-" + yabs.version + ".zip";
                found.add(new Update(id, e.name == null ? name : e.name, installed, e.version, yabs.page,
                        yabs.assets.get(zip), e.jar, yabs.assets.get(SUMS_ASSET), zip, e.minYabsVersion, i.jar));
            }
        }
        Log.Print("Plugin update check: " + (!asked ? "no plugins from the plugins folder"
                : !reached ? "GitHub not reached" : found.size() + " update(s)"));
        if (asked && !reached) {
            return null;
        }
        last = Collections.unmodifiableList(found);
        return last;
    }

    /**
     * @return "anti43/openyabs", from {@link UpdateCheck#LATEST}
     */
    static String repoOfYabs() {
        String s = UpdateCheck.LATEST.substring(API.length());
        return s.substring(0, s.indexOf("/releases"));
    }

    /**
     * @param s "owner/repo", "https://github.com/owner/repo" or
     * "github.com/owner/repo.git"
     * @return "owner/repo", null if it is no GitHub repository
     */
    static String normalizeRepo(String s) {
        if (s == null) {
            return null;
        }
        String r = s.trim();
        r = r.replaceFirst("^(?i)(https?://)?(www\\.)?github\\.com/", "");
        r = r.replaceFirst("\\.git$", "").replaceFirst("/+$", "");
        return r.matches("[A-Za-z0-9_.-]+/[A-Za-z0-9_.-]+") ? r : null;
    }

    /**
     * @param assets
     * @param installedName the file name of the installed jar
     * @return the asset of the same name, else the only jar, else null
     */
    static String pickJar(Map<String, String> assets, String installedName) {
        List<String> jars = new ArrayList<String>();
        for (String n : assets.keySet()) {
            if (n.equalsIgnoreCase(installedName)) {
                return n;
            }
            if (n.toLowerCase(Locale.ROOT).endsWith(".jar")) {
                jars.add(n);
            }
        }
        return jars.size() == 1 ? jars.get(0) : null;
    }

    private static Release release(String repo) {
        String url = API + repo + "/releases/latest";
        Log.Print("Plugin update check: GET " + url);
        try {
            byte[] b = get(url, "application/vnd.github+json", MAX_JSON);
            return b == null ? null : parseRelease(new String(b, StandardCharsets.UTF_8));
        } catch (Exception e) {
            Log.Print("Plugin update check: " + repo + " not reached (" + e.getClass().getSimpleName() + ": " + e.getMessage() + ")");
            return null;
        }
    }

    /**
     * @param json the answer of GitHub for a release
     * @return the release, null for a draft, a pre-release or a tag without
     * version
     */
    static Release parseRelease(String json) {
        JSONObject o;
        try {
            o = new JSONObject(json);
        } catch (Exception e) {
            Log.Debug(PluginUpdateCheck.class, e.getMessage());
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
        String page = o.optString("html_url", "");
        if (!page.startsWith("https://github.com/")) {
            page = null;
        }
        Map<String, String> assets = new LinkedHashMap<String, String>();
        JSONArray a = o.optJSONArray("assets");
        if (a != null) {
            for (int i = 0; i < a.length(); i++) {
                JSONObject x = a.optJSONObject(i);
                if (x == null) {
                    continue;
                }
                String n = x.optString("name", "");
                String u = x.optString("browser_download_url", "");
                //only downloads from GitHub
                if (!n.isEmpty() && u.startsWith("https://github.com/")) {
                    assets.put(n, u);
                }
            }
        }
        return new Release(version, page, assets);
    }

    private static Map<String, IndexEntry> index(Release yabs) {
        String url = yabs.assets.get(INDEX_ASSET);
        if (url == null) {
            Log.Print("Plugin update check: no " + INDEX_ASSET + " in the YaBS release " + yabs.version);
            return null;
        }
        try {
            byte[] b = get(url, "application/octet-stream", MAX_JSON);
            return b == null ? null : parseIndex(new String(b, StandardCharsets.UTF_8));
        } catch (Exception e) {
            Log.Print("Plugin update check: " + INDEX_ASSET + " not read (" + e.getMessage() + ")");
            return null;
        }
    }

    /**
     * @param json the plugin list of a YaBS release
     * @return the plugins by id
     */
    static Map<String, IndexEntry> parseIndex(String json) {
        Map<String, IndexEntry> m = new LinkedHashMap<String, IndexEntry>();
        JSONArray a;
        try {
            a = new JSONObject(json).optJSONArray("plugins");
        } catch (Exception e) {
            Log.Debug(PluginUpdateCheck.class, e.getMessage());
            return m;
        }
        if (a == null) {
            return m;
        }
        for (int i = 0; i < a.length(); i++) {
            JSONObject x = a.optJSONObject(i);
            if (x == null) {
                continue;
            }
            String id = x.optString("id", "").trim();
            String version = x.optString("version", "").trim();
            String jar = x.optString("jar", "").trim();
            //a plain file name, nothing like "../x.jar"
            if (id.isEmpty() || version.isEmpty() || !jar.matches("[A-Za-z0-9_.-]+\\.jar")) {
                continue;
            }
            String min = x.optString("minYabsVersion", "").trim();
            m.put(id, new IndexEntry(id, x.optString("name", null), version, jar, min.isEmpty() ? null : min));
        }
        return m;
    }

    /**
     * Downloads an update (not in the Swing thread)
     *
     * @param u
     * @param dir an empty folder
     * @return the jar, in dir, with the file name of the installed jar (so
     * installing it replaces the old one)
     * @throws IOException if it can not be downloaded or the checksum does
     * not match
     */
    public static File download(Update u, File dir) throws IOException {
        if (u.download == null) {
            throw new IOException("No download for " + u.name + " " + u.version);
        }
        Log.Print("Plugin update: GET " + u.download);
        byte[] data = get(u.download, "application/octet-stream", MAX_DOWNLOAD);
        if (data == null) {
            throw new IOException("Download failed: " + u.download);
        }
        if (u.sums != null) {
            byte[] sums = get(u.sums, "application/octet-stream", MAX_JSON);
            String expected = sums == null ? null : checksum(new String(sums, StandardCharsets.UTF_8), u.assetName);
            if (expected == null) {
                throw new IOException("No checksum for " + u.assetName + " in " + SUMS_ASSET);
            }
            if (!expected.equalsIgnoreCase(sha256(data))) {
                throw new IOException("Checksum of " + u.assetName + " does not match");
            }
        }
        if (u.zipEntry != null) {
            data = unzip(data, u.zipEntry);
            if (data == null) {
                throw new IOException(u.zipEntry + " not in " + u.assetName);
            }
        }
        File f = new File(dir, u.jar.getName());
        Files.write(f.toPath(), data);
        if (!ExtensionManager.isExtensionFile(f)) {
            Files.deleteIfExists(f.toPath());
            throw new IOException(u.assetName + " is no YaBS plugin");
        }
        return f;
    }

    /**
     * @param sums the lines of sha256sum ("hash  ./name" or "hash *name")
     * @param name
     * @return the hash of the file, null if not listed
     */
    static String checksum(String sums, String name) {
        for (String line : sums.split("\\r?\\n")) {
            String[] p = line.trim().split("\\s+", 2);
            if (p.length == 2) {
                String n = p[1].replaceFirst("^\\*", "").replaceFirst("^\\./", "");
                if (n.equals(name) && p[0].matches("[0-9a-fA-F]{64}")) {
                    return p[0];
                }
            }
        }
        return null;
    }

    static String sha256(byte[] data) {
        try {
            byte[] h = MessageDigest.getInstance("SHA-256").digest(data);
            StringBuilder s = new StringBuilder();
            for (byte b : h) {
                s.append(String.format("%02x", b));
            }
            return s.toString();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * @param zip
     * @param entry the file name of the jar (in any folder of the zip)
     * @return the jar, null if not in the zip
     */
    static byte[] unzip(byte[] zip, String entry) throws IOException {
        try (ZipInputStream z = new ZipInputStream(new ByteArrayInputStream(zip))) {
            ZipEntry e;
            while ((e = z.getNextEntry()) != null) {
                String n = e.getName();
                n = n.substring(n.lastIndexOf('/') + 1);
                if (!e.isDirectory() && n.equals(entry)) {
                    return read(z, MAX_DOWNLOAD);
                }
            }
        }
        return null;
    }

    private static byte[] get(String url, String accept, int max) throws IOException {
        HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
        try {
            con.setConnectTimeout(5000);
            con.setReadTimeout(30000);
            con.setRequestProperty("Accept", accept);
            //a generic client name, nothing about the user
            con.setRequestProperty("User-Agent", "YaBS-update-check");
            //GitHub sends the downloads from another host (https to https is followed)
            con.setInstanceFollowRedirects(true);
            if (con.getResponseCode() != HttpURLConnection.HTTP_OK) {
                Log.Print("Plugin update check: HTTP " + con.getResponseCode() + " for " + url);
                return null;
            }
            try (InputStream in = con.getInputStream()) {
                return read(in, max);
            }
        } finally {
            con.disconnect();
        }
    }

    private static byte[] read(InputStream in, int max) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] b = new byte[8192];
        int n;
        while ((n = in.read(b)) > 0) {
            out.write(b, 0, n);
            if (out.size() > max) {
                throw new IOException("Too large (more than " + max + " bytes)");
            }
        }
        return out.toByteArray();
    }

    private static String safe(java.util.function.Supplier<String> s) {
        try {
            String v = s.get();
            return v == null ? "" : v.trim();
        } catch (Throwable t) {
            return "";
        }
    }
}
