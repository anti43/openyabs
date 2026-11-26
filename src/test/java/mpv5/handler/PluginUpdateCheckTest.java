package mpv5.handler;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.junit.jupiter.api.Test;

class PluginUpdateCheckTest {

    @Test
    void repositoriesAreNormalized() {
        assertEquals("example/yabs-hello", PluginUpdateCheck.normalizeRepo("example/yabs-hello"));
        assertEquals("example/yabs-hello", PluginUpdateCheck.normalizeRepo("https://github.com/example/yabs-hello"));
        assertEquals("example/yabs-hello", PluginUpdateCheck.normalizeRepo("github.com/example/yabs-hello.git"));
        assertEquals("example/yabs-hello", PluginUpdateCheck.normalizeRepo(" https://github.com/example/yabs-hello/ "));
        assertNull(PluginUpdateCheck.normalizeRepo(null));
        assertNull(PluginUpdateCheck.normalizeRepo(""));
        assertNull(PluginUpdateCheck.normalizeRepo("https://example.com/a/b"));
        assertNull(PluginUpdateCheck.normalizeRepo("a/b/../c"));
        assertEquals("anti43/openyabs", PluginUpdateCheck.repoOfYabs());
    }

    @Test
    void releaseWithAssets() {
        String json = "{\"tag_name\":\"v1.2.0\",\"html_url\":\"https://github.com/example/hello/releases/tag/v1.2.0\","
                + "\"assets\":[{\"name\":\"hello.jar\",\"browser_download_url\":\"https://github.com/example/hello/releases/download/v1.2.0/hello.jar\"},"
                + "{\"name\":\"evil.jar\",\"browser_download_url\":\"https://example.com/evil.jar\"}]}";
        PluginUpdateCheck.Release r = PluginUpdateCheck.parseRelease(json);
        assertNotNull(r);
        assertEquals("1.2.0", r.version);
        assertEquals("https://github.com/example/hello/releases/tag/v1.2.0", r.page);
        //only downloads from github.com
        assertEquals(1, r.assets.size());
        assertTrue(r.assets.containsKey("hello.jar"));
    }

    @Test
    void preReleasesAndDraftsAreNoUpdates() {
        assertNull(PluginUpdateCheck.parseRelease("{\"tag_name\":\"v2.0.0\",\"prerelease\":true}"));
        assertNull(PluginUpdateCheck.parseRelease("{\"tag_name\":\"v2.0.0\",\"draft\":true}"));
        assertNull(PluginUpdateCheck.parseRelease("{\"tag_name\":\"nightly\"}"));
        assertNull(PluginUpdateCheck.parseRelease("not json"));
    }

    @Test
    void jarAssetIsPicked() {
        Map<String, String> a = new LinkedHashMap<String, String>();
        a.put("SHA256SUMS", "s");
        a.put("Hello-1.2.jar", "x");
        assertEquals("Hello-1.2.jar", PluginUpdateCheck.pickJar(a, "hello.jar"));
        a.put("hello.jar", "y");
        //the same name as the installed jar first
        assertEquals("hello.jar", PluginUpdateCheck.pickJar(a, "hello.jar"));
        a.remove("hello.jar");
        a.put("other.jar", "z");
        //two jars, none of the name: which one is not clear
        assertNull(PluginUpdateCheck.pickJar(a, "hello.jar"));
    }

    @Test
    void indexIsRead() {
        String json = "{\"yabsVersion\":\"2.1.0\",\"plugins\":["
                + "{\"id\":\"org.openyabs.datev\",\"name\":\"DATEV\",\"version\":\"2.1.0\",\"jar\":\"datev.jar\",\"minYabsVersion\":\"2.1.0\"},"
                + "{\"id\":\"bad\",\"version\":\"1.0\",\"jar\":\"../evil.jar\"},"
                + "{\"id\":\"org.openyabs.gobd\",\"version\":\"1.0\",\"jar\":\"gobd-compliance.jar\"}]}";
        Map<String, PluginUpdateCheck.IndexEntry> m = PluginUpdateCheck.parseIndex(json);
        assertEquals(2, m.size());
        assertEquals("2.1.0", m.get("org.openyabs.datev").version);
        assertEquals("2.1.0", m.get("org.openyabs.datev").minYabsVersion);
        assertEquals("datev.jar", m.get("org.openyabs.datev").jar);
        assertNull(m.get("org.openyabs.gobd").minYabsVersion);
        assertTrue(PluginUpdateCheck.parseIndex("garbage").isEmpty());
    }

    @Test
    void checksumsAndZip() throws Exception {
        byte[] jar = "jar".getBytes(StandardCharsets.UTF_8);
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        try (ZipOutputStream z = new ZipOutputStream(b)) {
            z.putNextEntry(new ZipEntry("other.jar"));
            z.write("other".getBytes(StandardCharsets.UTF_8));
            z.closeEntry();
            z.putNextEntry(new ZipEntry("plugins/datev.jar"));
            z.write(jar);
            z.closeEntry();
        }
        byte[] zip = b.toByteArray();
        assertArrayEquals(jar, PluginUpdateCheck.unzip(zip, "datev.jar"));
        assertNull(PluginUpdateCheck.unzip(zip, "missing.jar"));

        String hash = PluginUpdateCheck.sha256(zip);
        String sums = "0000000000000000000000000000000000000000000000000000000000000000  ./openyabs-2.1.0.zip\n"
                + hash + "  ./openyabs-plugins-2.1.0.zip\n";
        assertEquals(hash, PluginUpdateCheck.checksum(sums, "openyabs-plugins-2.1.0.zip"));
        assertEquals(hash, PluginUpdateCheck.checksum(hash + " *openyabs-plugins-2.1.0.zip", "openyabs-plugins-2.1.0.zip"));
        assertNull(PluginUpdateCheck.checksum(sums, "SHA256SUMS"));
    }
}
