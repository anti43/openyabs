package mpv5.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class UpdateCheckTest {

    @Test
    void versionNumbersPartByPart() {
        assertTrue(UpdateCheck.isNewer("2.0.10", "2.0.9"));
        assertTrue(UpdateCheck.isNewer("2.1.0", "2.0.3"));
        assertTrue(UpdateCheck.isNewer("2.0.1", "2.0"));
        assertFalse(UpdateCheck.isNewer("2.0.3", "2.0.3"));
        assertFalse(UpdateCheck.isNewer("2.0.2", "2.0.3"));
    }

    @Test
    void masterBuildsArePreReleasesOfTheNextPatch() {
        //master build after the release 2.0.3: 2.0.4-dev.250
        assertFalse(UpdateCheck.isNewer("2.0.3", "2.0.4-dev.250"));
        assertTrue(UpdateCheck.isNewer("2.0.4", "2.0.4-dev.250"));
        assertTrue(UpdateCheck.isNewer("2.1.0", "2.0.4-dev.250"));
        //later builds are higher, numerically (dev.10 > dev.9)
        assertTrue(UpdateCheck.compare("2.0.4-dev.10", "2.0.4-dev.9") > 0);
        assertTrue(UpdateCheck.compare("2.0.4-dev", "2.0.4-dev.1") < 0);
        assertTrue(UpdateCheck.compare("2.0.4-alpha", "2.0.4-beta") < 0);
        assertTrue(UpdateCheck.compare("2.0.4-1", "2.0.4-alpha") < 0);
        assertTrue(UpdateCheck.compare("v2.0.4+build.7", "2.0.4") == 0);
    }

    @Test
    void ownBuildsDoNotCheckAtStart() {
        assertTrue(UpdateCheck.isLocalBuild("2.0-dev"));
        assertFalse(UpdateCheck.isLocalBuild("2.0.4-dev.250"));
        assertFalse(UpdateCheck.isLocalBuild("2.0.3"));
    }

    @Test
    void releaseOfGitHub() {
        UpdateCheck.Release r = UpdateCheck.parse("{\"tag_name\":\"v2.0.3\",\"name\":\"YaBS 2.0.3\","
                + "\"html_url\":\"https://github.com/anti43/openyabs/releases/tag/v2.0.3\",\"draft\":false,\"prerelease\":false}");
        assertEquals("2.0.3", r.version);
        assertEquals("YaBS 2.0.3", r.name);
        assertEquals("https://github.com/anti43/openyabs/releases/tag/v2.0.3", r.url);
        //old tags without a version number, pre-releases and other links
        assertNull(UpdateCheck.parse("{\"tag_name\":\"release-1.3.2\"}"));
        assertNull(UpdateCheck.parse("{\"tag_name\":\"v2.1.0\",\"prerelease\":true}"));
        assertNull(UpdateCheck.parse("not json"));
        assertEquals(UpdateCheck.RELEASES_PAGE, UpdateCheck.parse("{\"tag_name\":\"v2.1.0\",\"html_url\":\"https://example.com/x\"}").url);
    }
}
