package mpv5.globals;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class CachingSwitchTest {

    @Test
    void cachingIsOnByDefaultAndCanBeSwitchedOff() {
        String before = LocalSettings.hasProperty(LocalSettings.NO_CACHE) ? LocalSettings.getProperty(LocalSettings.NO_CACHE) : "false";
        try {
            LocalSettings.setProperty(LocalSettings.NO_CACHE, "false");
            assertTrue(LocalSettings.isCachingEnabled());
            LocalSettings.setProperty(LocalSettings.NO_CACHE, "true");
            assertFalse(LocalSettings.isCachingEnabled());
            assertFalse(mpv5.db.common.DatabaseObject.isCachingEnabled());
        } finally {
            LocalSettings.setProperty(LocalSettings.NO_CACHE, before);
        }
        assertTrue(LocalSettings.isCachingEnabled() == !"true".equals(before));
    }
}
