package mpv5.plugin.builtin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.file.Path;
import java.util.Collections;
import java.util.ServiceLoader;
import javax.swing.JComponent;
import mpv5.Main;
import mpv5.handler.PluginUpdateCheck;
import mpv5.plugin.ExtensionManager;
import mpv5.plugin.api.YabsExtension;
import mpv5.ui.dialogs.PluginUpdateNotice;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class UpdateManagerTest {

    @TempDir
    Path dir;

    private static final ExtensionManager.Host NOOP_HOST = new ExtensionManager.Host() {
        @Override
        public void openTab(String title, JComponent content) {
        }

        @Override
        public void notify(String message) {
        }

        @Override
        public void ui(Runnable r) {
            r.run();
        }

        @Override
        public void changed() {
        }

        @Override
        public void error(String plugin, Throwable t) {
        }
    };

    @Test
    void updateManagerIsRegisteredAsBuiltInService() {
        boolean found = false;
        for (YabsExtension ext : ServiceLoader.load(YabsExtension.class)) {
            if (UpdateManager.ID.equals(ext.id())) {
                found = true;
                assertTrue(ext instanceof UpdateManager);
                assertNotNull(ext.name());
                assertFalse(ext.name().trim().isEmpty());
                assertNotNull(ext.description());
                assertFalse(ext.description().trim().isEmpty());
            }
        }
        assertTrue(found, "UpdateManager must be registered in META-INF/services");
    }

    @Test
    void startAndStopToggleActiveStateCommandsAndCachedResults() {
        boolean prevHeadless = Main.HEADLESS;
        Main.HEADLESS = true;
        ExtensionManager manager = new ExtensionManager(dir.toFile(), NOOP_HOST);
        ExtensionManager.Installed installed = manager.register(new UpdateManager());
        assertNotNull(installed);
        assertTrue(installed.isBuiltIn());

        try {
            assertTrue(manager.start(installed));
            assertTrue(UpdateManager.isActive());
            assertEquals(2, manager.commands().size());

            PluginUpdateCheck.Catalog catalog = new PluginUpdateCheck.Catalog(
                    "2.0.26", "https://github.com/anti43/openyabs/releases/tag/v2.0.26", Collections.emptyList());
            PluginUpdateCheck.setLastCatalog(catalog);
            PluginUpdateCheck.Update update = new PluginUpdateCheck.Update(
                    "org.openyabs.datev", "DATEV", "1.0", "2.0", null, null, null, null, "datev.jar", null, new File("datev.jar"));
            PluginUpdateCheck.setLastResult(Collections.singletonList(update));
            assertNotNull(PluginUpdateCheck.lastCatalog());
            assertEquals(1, PluginUpdateCheck.lastResult().size());

            manager.stop(installed);
            assertFalse(UpdateManager.isActive());
            assertEquals(0, manager.commands().size());
            assertNull(PluginUpdateCheck.lastCatalog());
            assertTrue(PluginUpdateCheck.lastResult().isEmpty());

            //while disabled, loadCatalog must not start a network fetch
            PluginUpdateNotice.loadCatalog(null, false);
            assertFalse(PluginUpdateNotice.isCatalogLoading());
            assertNull(PluginUpdateCheck.lastCatalog());
        } finally {
            manager.stop(installed);
            Main.HEADLESS = prevHeadless;
        }
    }
}
