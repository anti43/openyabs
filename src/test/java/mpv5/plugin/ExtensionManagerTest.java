package mpv5.plugin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import javax.swing.JComponent;
import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import mpv5.plugin.api.ExtensionContext;
import mpv5.plugin.api.SearchResult;
import mpv5.plugin.api.YabsExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ExtensionManagerTest {

    @TempDir
    Path dir;

    static final class TestHost implements ExtensionManager.Host {

        final List<String> messages = new ArrayList<String>();
        final List<String> errors = new ArrayList<String>();

        public void openTab(String title, JComponent content) {
        }

        public void notify(String message) {
            messages.add(message);
        }

        public void ui(Runnable r) {
            r.run();
        }

        public void changed() {
        }

        public void error(String plugin, Throwable t) {
            errors.add(plugin);
        }
    }

    static YabsExtension ext(String id, boolean fail) {
        return new YabsExtension() {
            boolean stopped;

            public String id() {
                return id;
            }

            public String name() {
                return "Ext " + id;
            }

            public void start(ExtensionContext ctx) throws Exception {
                ctx.addCommand("Hello " + id, "greets", () -> ctx.notify("hi " + id));
                ctx.addSettingsPage("Page " + id, "", () -> null);
                ctx.addSearchProvider(n -> n.contains("x") ? Collections.singletonList(new SearchResult("found " + n, "", () -> {
                })) : Collections.<SearchResult>emptyList());
                if (fail) {
                    throw new IllegalStateException("broken");
                }
            }

            public void stop() {
                stopped = true;
            }
        };
    }

    @Test
    void startAddsAndStopRemovesContributions() {
        TestHost h = new TestHost();
        ExtensionManager m = new ExtensionManager(dir.toFile(), h);
        ExtensionManager.Installed i = m.register(ext("a", false));
        assertTrue(m.start(i));
        assertEquals(ExtensionManager.State.RUNNING, i.getState());
        assertEquals(1, m.commands().size());
        assertEquals(1, m.pages().size());
        assertEquals(1, m.search("xy").size());
        m.commands().get(0).action.run();
        assertEquals("hi a", h.messages.get(0));
        m.stop(i);
        assertEquals(0, m.contributions("a"));
        assertTrue(m.commands().isEmpty());
        assertTrue(m.search("xy").isEmpty());
    }

    @Test
    void aFailingPluginIsIsolated() {
        TestHost h = new TestHost();
        ExtensionManager m = new ExtensionManager(dir.toFile(), h);
        ExtensionManager.Installed bad = m.register(ext("bad", true));
        ExtensionManager.Installed good = m.register(ext("good", false));
        m.startAll();
        assertEquals(ExtensionManager.State.FAILED, bad.getState());
        assertTrue(bad.getError().contains("broken"));
        assertEquals(0, m.contributions("bad"));
        assertEquals(ExtensionManager.State.RUNNING, good.getState());
        assertEquals(1, h.errors.size());
    }

    @Test
    void disabledIsRemembered() {
        ExtensionManager m = new ExtensionManager(dir.toFile(), new TestHost());
        ExtensionManager.Installed i = m.register(ext("a", false));
        m.start(i);
        m.setEnabled(i, false);
        assertEquals(ExtensionManager.State.DISABLED, i.getState());
        assertEquals(0, m.contributions("a"));
        ExtensionManager again = new ExtensionManager(dir.toFile(), new TestHost());
        ExtensionManager.Installed j = again.register(ext("a", false));
        assertEquals(ExtensionManager.State.DISABLED, j.getState());
        again.setEnabled(j, true);
        assertEquals(ExtensionManager.State.RUNNING, j.getState());
    }

    @Test
    void installReloadAndUninstallAJar() throws Exception {
        File jar = buildPluginJar(dir.resolve("build").toFile(), "Hello from the jar");
        File plugins = dir.resolve("plugins").toFile();
        ExtensionManager m = new ExtensionManager(plugins, new TestHost());
        List<ExtensionManager.Installed> found = m.install(jar);
        assertEquals(1, found.size());
        ExtensionManager.Installed i = found.get(0);
        assertEquals("test.jarplugin", i.id());
        assertEquals(ExtensionManager.State.RUNNING, i.getState());
        assertEquals("Hello from the jar", m.commands().get(0).title);
        assertTrue(new File(plugins, jar.getName()).isFile());
        //settings of the plugin survive a reload
        ExtensionManager.Installed again = m.reload(i);
        assertNotNull(again);
        assertEquals(ExtensionManager.State.RUNNING, again.getState());
        assertEquals("2", m.commands().get(0).description);
        assertTrue(m.uninstall(again));
        assertTrue(m.list().isEmpty());
        assertTrue(m.commands().isEmpty());
        assertFalse(new File(plugins, jar.getName()).exists());
    }

    @Test
    void aJarWithoutPluginIsRefused() throws Exception {
        File empty = dir.resolve("empty.jar").toFile();
        try (JarOutputStream out = new JarOutputStream(new FileOutputStream(empty))) {
            out.putNextEntry(new JarEntry("readme.txt"));
            out.write("nothing".getBytes(StandardCharsets.UTF_8));
        }
        ExtensionManager m = new ExtensionManager(dir.resolve("plugins").toFile(), new TestHost());
        try {
            m.install(empty);
            throw new AssertionError("should be refused");
        } catch (java.io.IOException expected) {
            assertTrue(expected.getMessage().contains("No YaBS plugin"));
        }
        assertFalse(new File(dir.resolve("plugins").toFile(), "empty.jar").exists());
    }

    /**
     * Compiles a small plugin into a jar: it counts its starts in its settings
     */
    static File buildPluginJar(File work, String title) throws Exception {
        File src = new File(work, "src/test/JarPlugin.java");
        src.getParentFile().mkdirs();
        Files.write(src.toPath(), ("package test;\n"
                + "public class JarPlugin implements mpv5.plugin.api.YabsExtension {\n"
                + " public String id() { return \"test.jarplugin\"; }\n"
                + " public String name() { return \"Jar plugin\"; }\n"
                + " public void start(mpv5.plugin.api.ExtensionContext ctx) {\n"
                + "  int n = Integer.parseInt(ctx.settings().get(\"starts\", \"0\")) + 1;\n"
                + "  ctx.settings().set(\"starts\", String.valueOf(n));\n"
                + "  ctx.addCommand(\"" + title + "\", String.valueOf(n), () -> {});\n"
                + " }\n"
                + "}\n").getBytes(StandardCharsets.UTF_8));
        File classes = new File(work, "classes");
        classes.mkdirs();
        JavaCompiler c = ToolProvider.getSystemJavaCompiler();
        int r = c.run(null, null, null, "-d", classes.getPath(), "-cp", System.getProperty("java.class.path"), src.getPath());
        assertEquals(0, r);
        File jar = new File(work, "jar-plugin.jar");
        try (JarOutputStream out = new JarOutputStream(new FileOutputStream(jar))) {
            out.putNextEntry(new JarEntry("test/JarPlugin.class"));
            out.write(Files.readAllBytes(new File(classes, "test/JarPlugin.class").toPath()));
            out.closeEntry();
            out.putNextEntry(new JarEntry("META-INF/services/mpv5.plugin.api.YabsExtension"));
            out.write("test.JarPlugin\n".getBytes(StandardCharsets.UTF_8));
            out.closeEntry();
        }
        return jar;
    }
}
