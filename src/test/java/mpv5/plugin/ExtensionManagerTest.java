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
        final List<String> tabs = new ArrayList<String>();
        final List<mpv5.ui.panels.DataPanel> views = new ArrayList<mpv5.ui.panels.DataPanel>();

        public void openTab(String title, JComponent content) {
            tabs.add(title);
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

        @Override
        public List<mpv5.ui.panels.DataPanel> openViews() {
            return views;
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

    @Test
    void groovyScriptsArePlugins() throws Exception {
        File plugins = dir.resolve("plugins").toFile();
        plugins.mkdirs();
        File script = new File(dir.toFile(), "hello.groovy");
        Files.write(script.toPath(), ("// @name Hello Groovy\n"
                + "// @description Says hello\n"
                + "def count = 0\n"
                + "yabs.addCommand('Say hello', 'greets', { count++; yabs.notify('Hello ' + count) })\n"
                + "yabs.addSearchProvider({ n -> [new mpv5.plugin.api.SearchResult('Groovy ' + n, '', {})] })\n"
                + "onStop = { yabs.notify('bye') }\n").getBytes(StandardCharsets.UTF_8));
        TestHost h = new TestHost();
        ExtensionManager m = new ExtensionManager(plugins, h);
        ExtensionManager.Installed i = m.install(script).get(0);
        assertEquals("groovy.hello", i.id());
        assertEquals("Hello Groovy", i.extension.name());
        assertEquals("Says hello", i.extension.description());
        assertEquals(ExtensionManager.State.RUNNING, i.getState());
        m.commands().get(0).action.run();
        m.commands().get(0).action.run();
        assertEquals("Hello 2", h.messages.get(1));
        assertEquals("Groovy abc", ((SearchResult) m.search("abc").get(0)[1]).title);
        //edit and reload
        Files.write(new File(plugins, "hello.groovy").toPath(), ("// @name Hello Groovy\n"
                + "yabs.addCommand('Say hello again', '', { })\n").getBytes(StandardCharsets.UTF_8));
        ExtensionManager.Installed again = m.reload(i);
        assertEquals("bye", h.messages.get(h.messages.size() - 1));
        assertEquals(ExtensionManager.State.RUNNING, again.getState());
        assertEquals("Say hello again", m.commands().get(0).title);
        assertTrue(m.search("abc").isEmpty());
        assertTrue(m.uninstall(again));
        assertFalse(new File(plugins, "hello.groovy").exists());
    }

    @Test
    void startAllInBackgroundCompilesAndStartsGroovyPlugins() throws Exception {
        File plugins = dir.resolve("plugins").toFile();
        plugins.mkdirs();
        Files.write(new File(plugins, "bg.groovy").toPath(), ("// @name Background\n"
                + "yabs.addCommand('From the background', '', { })\n").getBytes(StandardCharsets.UTF_8));
        TestHost h = new TestHost();
        ExtensionManager m = new ExtensionManager(plugins, h);
        m.startAllInBackground();
        //the built-in plugins of YaBS start too: wait until the script runs (or failed)
        long until = System.currentTimeMillis() + 30000;
        ExtensionManager.Installed bg = null;
        while (System.currentTimeMillis() < until) {
            bg = m.list().stream().filter(i -> "groovy.bg".equals(i.id())).findFirst().orElse(null);
            if (bg != null && bg.getState() != ExtensionManager.State.STOPPED) {
                break;
            }
            Thread.sleep(50);
        }
        assertNotNull(bg);
        assertEquals(ExtensionManager.State.RUNNING, bg.getState(), String.valueOf(bg.getError()));
        assertTrue(m.commands().stream().anyMatch(c -> "From the background".equals(c.title)));
        //the progress went to the host
        assertTrue(h.messages.stream().anyMatch(x -> x.contains("Background")));
    }

    @Test
    void aPreparedGroovyScriptRunsOnceCompiledAndAgainAfterReload() throws Exception {
        File script = new File(dir.toFile(), "prep.groovy");
        Files.write(script.toPath(), "yabs.notify('run')\n".getBytes(StandardCharsets.UTF_8));
        TestHost h = new TestHost();
        ExtensionManager m = new ExtensionManager(dir.resolve("plugins").toFile(), h);
        ExtensionManager.Installed i = m.install(script).get(0);
        assertEquals(ExtensionManager.State.RUNNING, i.getState());
        ExtensionManager.Installed again = m.reload(i);
        assertEquals(ExtensionManager.State.RUNNING, again.getState());
        assertEquals(2, h.messages.stream().filter("run"::equals).count());
    }

    @Test
    void aBrokenGroovyScriptFails() throws Exception {
        File plugins = dir.resolve("plugins").toFile();
        File script = new File(dir.toFile(), "broken.groovy");
        Files.write(script.toPath(), "yabs.addCommand('x', '', { }\n this is not groovy (".getBytes(StandardCharsets.UTF_8));
        ExtensionManager m = new ExtensionManager(plugins, new TestHost());
        ExtensionManager.Installed i = m.install(script).get(0);
        assertEquals(ExtensionManager.State.FAILED, i.getState());
        assertNotNull(i.getError());
        assertTrue(m.commands().isEmpty());
    }

    static final class DummyDataPanel extends javax.swing.JPanel implements mpv5.ui.panels.DataPanel {
        public boolean collectData() { return true; }
        public mpv5.db.common.DatabaseObject getDataOwner() { return null; }
        public void setDataOwner(mpv5.db.common.DatabaseObject object, boolean populateData) {}
        public void refresh() {}
        public void exposeData() {}
        public void paste(mpv5.db.common.DatabaseObject... dbo) {}
        public void showRequiredFields() {}
        public void showSearchBar(boolean show) {}
        public void actionAfterSave() {}
        public void actionAfterCreate() {}
        public void actionBeforeCreate() {}
        public void actionBeforeSave() {}
        public void mail() {}
        public void print() {}
    }

    @Test
    void viewHooksLockAndDecorateAndCleanUpOnStop() {
        TestHost h = new TestHost();
        DummyDataPanel panel = new DummyDataPanel();
        h.views.add(panel);
        ExtensionManager m = new ExtensionManager(dir.toFile(), h);
        ExtensionManager.Installed i = m.register(new YabsExtension() {
            public String id() { return "lock.ext"; }
            public String name() { return "Lock Ext"; }
            public void start(ExtensionContext ctx) {
                ctx.onView(view -> {
                    view.setContentLocked(true);
                    view.setPluginBadge(new javax.swing.JLabel("LOCKED"));
                    view.setPluginBanner(new javax.swing.JLabel("BANNER"));
                });
            }
        });
        assertTrue(m.start(i));
        assertTrue(panel.isContentLocked());
        assertNotNull(panel.getClientProperty(mpv5.ui.panels.DataPanel.PROPERTY_PLUGIN_BADGE));
        assertNotNull(panel.getClientProperty(mpv5.ui.panels.DataPanel.PROPERTY_PLUGIN_BANNER));
        assertEquals(1, m.contributions("lock.ext"));

        m.stop(i);
        assertFalse(panel.isContentLocked());
        assertEquals(null, panel.getClientProperty(mpv5.ui.panels.DataPanel.PROPERTY_PLUGIN_BADGE));
        assertEquals(null, panel.getClientProperty(mpv5.ui.panels.DataPanel.PROPERTY_PLUGIN_BANNER));
    }

    @Test
    void onStopRunsWhenThePluginStops() {
        TestHost h = new TestHost();
        ExtensionManager m = new ExtensionManager(dir.toFile(), h);
        final int[] stops = {0};
        ExtensionManager.Installed i = m.register(new YabsExtension() {
            public String id() {
                return "test.onstop";
            }

            public String name() {
                return "On stop";
            }

            public void start(ExtensionContext ctx) {
                ctx.onStop(() -> stops[0]++);
            }
        });
        assertTrue(m.start(i));
        m.stop(i);
        assertEquals(1, stops[0]);
        //once: a second stop does not run it again
        m.stop(i);
        assertEquals(1, stops[0]);
    }

    @Test
    void installingAJarReplacesAScriptWithTheSameId() throws Exception {
        File plugins = dir.resolve("plugins").toFile();
        plugins.mkdirs();
        File script = new File(plugins, "jarplugin.groovy");
        Files.write(script.toPath(), ("// @id test.jarplugin\n"
                + "yabs.addCommand('From the script', '', { })\n").getBytes(StandardCharsets.UTF_8));
        ExtensionManager m = new ExtensionManager(plugins, new TestHost());
        m.startAll();
        assertTrue(m.commands().stream().anyMatch(c -> "From the script".equals(c.title)));
        File jar = buildPluginJar(dir.resolve("work").toFile(), "From the jar");
        ExtensionManager.Installed i = m.install(jar).get(0);
        assertEquals("test.jarplugin", i.id());
        assertEquals(ExtensionManager.State.RUNNING, i.getState());
        assertTrue(m.commands().stream().anyMatch(c -> "From the jar".equals(c.title)));
        assertFalse(m.commands().stream().anyMatch(c -> "From the script".equals(c.title)));
        //the script is kept, renamed, and not loaded again
        assertFalse(script.exists());
        assertTrue(new File(plugins, "jarplugin.groovy.replaced").exists());
    }

    @Test
    void revenuePreservesPrepopulatedNumberAndMapsPaidStatus() {
        mpv5.db.objects.Revenue rev = new mpv5.db.objects.Revenue();
        rev.setCnumber("GUT-2026-0042");
        rev.setCname("GUT-2026-0042");
        rev.ensureUniqueness();
        assertEquals("GUT-2026-0042", rev.__getCnumber());
        assertEquals("GUT-2026-0042", rev.__getCname());

        assertFalse(rev.isIspaid());
        rev.setIspaid(true);
        assertTrue(rev.isIspaid());
        assertTrue(rev.__getIspaid());
        assertEquals(mpv5.db.objects.Item.STATUS_PAID, rev.__getStatus());
        rev.setIspaid(false);
        assertFalse(rev.isIspaid());
        assertEquals(mpv5.db.objects.Item.STATUS_QUEUED, rev.__getStatus());
    }

    @Test
    void versionCompatibilityLogic() {
        assertTrue(ExtensionManager.isDevBuild("2.0-dev"));
        assertTrue(ExtensionManager.isDevBuild("2.0.4-dev.250"));
        assertTrue(ExtensionManager.isDevBuild("dev"));
        assertTrue(ExtensionManager.isDevBuild("2.1.0-SNAPSHOT"));
        assertFalse(ExtensionManager.isDevBuild("2.0.8"));
        assertFalse(ExtensionManager.isDevBuild("2.0.9"));

        // No requirement specified
        assertTrue(ExtensionManager.isVersionCompatible("2.0.8", null));
        assertTrue(ExtensionManager.isVersionCompatible("2.0.8", ""));
        assertTrue(ExtensionManager.isVersionCompatible("2.0.8", "   "));

        // Release comparisons
        assertFalse(ExtensionManager.isVersionCompatible("2.0.8", "2.0.9"));
        assertTrue(ExtensionManager.isVersionCompatible("2.0.9", "2.0.9"));
        assertTrue(ExtensionManager.isVersionCompatible("2.0.10", "2.0.9"));
        assertTrue(ExtensionManager.isVersionCompatible("2.1.0", "2.0.9"));

        // Dev build comparisons
        assertTrue(ExtensionManager.isVersionCompatible("2.0-dev", "2.0.9"));
        assertTrue(ExtensionManager.isVersionCompatible("2.0.9-dev.1", "2.0.9"));
        assertTrue(ExtensionManager.isVersionCompatible("3.0-dev", "2.0.9"));
        assertFalse(ExtensionManager.isVersionCompatible("2.0-dev", "3.0.0"));
    }

    @Test
    void pluginFailsWhenMinYabsVersionNotSatisfied() {
        TestHost h = new TestHost();
        ExtensionManager m = new ExtensionManager(dir.toFile(), h);
        ExtensionManager.Installed i = m.register(new YabsExtension() {
            public String id() { return "future.plugin"; }
            public String name() { return "Future Plugin"; }
            @Override
            public String minYabsVersion() { return "99.0.0"; }
            public void start(ExtensionContext ctx) {}
        });

        assertFalse(m.start(i));
        assertEquals(ExtensionManager.State.FAILED, i.getState());
        assertNotNull(i.getError());
        assertTrue(i.getError().contains("Requires YaBS 99.0.0 or newer"));
        assertEquals(1, h.errors.size());
    }

    @Test
    void groovyExtensionExtractsMinYabsVersionTag() throws Exception {
        File f = dir.resolve("sample.groovy").toFile();
        Files.write(f.toPath(), (
            "// @name Sample\n"
            + "// @minYabsVersion 2.0.9\n"
            + "yabs.notify('hello')\n"
        ).getBytes(StandardCharsets.UTF_8));

        GroovyExtension ext = new GroovyExtension(f);
        assertEquals("2.0.9", ext.minYabsVersion());
    }

    /**
     * The classic import (plugin.Main) hands plugin jars to the plugin list
     */
    @Test
    public void pluginFilesAreToldFromClassicPlugins() throws Exception {
        File api = dir.resolve("api.jar").toFile();
        try (JarOutputStream j = new JarOutputStream(new FileOutputStream(api))) {
            j.putNextEntry(new JarEntry("META-INF/services/" + YabsExtension.class.getName()));
            j.write("org.example.Plugin\n".getBytes(StandardCharsets.UTF_8));
            j.closeEntry();
        }
        File classic = dir.resolve("classic.jar").toFile();
        try (JarOutputStream j = new JarOutputStream(new FileOutputStream(classic))) {
            j.putNextEntry(new JarEntry("plugin/Main.class"));
            j.write(new byte[]{1});
            j.closeEntry();
        }
        File groovy = dir.resolve("x.groovy").toFile();
        Files.write(groovy.toPath(), "//".getBytes(StandardCharsets.UTF_8));
        File other = dir.resolve("x.txt").toFile();
        Files.write(other.toPath(), "x".getBytes(StandardCharsets.UTF_8));

        assertTrue(ExtensionManager.isExtensionFile(api));
        assertTrue(ExtensionManager.isExtensionFile(groovy));
        assertFalse(ExtensionManager.isExtensionFile(classic));
        assertFalse(ExtensionManager.isExtensionFile(other));
        assertFalse(ExtensionManager.isExtensionFile(dir.resolve("missing.jar").toFile()));
    }

    @Test
    void stopAllStopsRunningResetsFailedKeepsDisabledAndAllowsRestart() {
        TestHost h = new TestHost();
        ExtensionManager m = new ExtensionManager(dir.toFile(), h);
        final int[] goodStarts = {0};
        final int[] goodStops = {0};
        final int[] goodOnStops = {0};
        final int[] flakyStarts = {0};
        final int[] offStarts = {0};
        mpv5.db.common.DatabaseObjectModifier mod = new mpv5.db.common.DatabaseObjectModifier() {
            @Override
            public mpv5.db.common.DatabaseObject modifyOnExplode(mpv5.db.common.DatabaseObject object) {
                return object;
            }

            @Override
            public mpv5.db.common.DatabaseObject modifyOnSave(mpv5.db.common.DatabaseObject object) {
                return object;
            }

            @Override
            public mpv5.db.common.DatabaseObject executeAfterCreate(mpv5.db.common.DatabaseObject object) {
                return object;
            }

            @Override
            public mpv5.db.common.DatabaseObject executeAfterSave(mpv5.db.common.DatabaseObject object) {
                return object;
            }

            @Override
            public java.util.Map<String, Object> modifyOnResolve(java.util.Map<String, Object> map) {
                return map;
            }

            @Override
            public mpv5.db.common.DatabaseObject modifyOnDelete(mpv5.db.common.DatabaseObject object) {
                return object;
            }
        };

        ExtensionManager.Installed good = m.register(new YabsExtension() {
            public String id() { return "switch.good"; }
            public String name() { return "Good"; }
            public void start(ExtensionContext ctx) {
                goodStarts[0]++;
                ctx.addCommand("Good cmd", "", () -> {});
                ctx.addSettingsPage("Good page", "", () -> null);
                ctx.addRecordModifier(mod);
                ctx.onStop(() -> goodOnStops[0]++);
            }
            public void stop() {
                goodStops[0]++;
            }
        });
        ExtensionManager.Installed flaky = m.register(new YabsExtension() {
            public String id() { return "switch.flaky"; }
            public String name() { return "Flaky"; }
            public void start(ExtensionContext ctx) {
                flakyStarts[0]++;
                if (flakyStarts[0] == 1) {
                    throw new IllegalStateException("missing table");
                }
                ctx.addCommand("Flaky cmd", "", () -> {});
            }
        });
        ExtensionManager.Installed off = m.register(new YabsExtension() {
            public String id() { return "switch.off"; }
            public String name() { return "Off"; }
            public void start(ExtensionContext ctx) {
                offStarts[0]++;
            }
        });
        m.setEnabled(off, false);

        m.startAll();
        assertEquals(ExtensionManager.State.RUNNING, good.getState());
        assertEquals(ExtensionManager.State.FAILED, flaky.getState());
        assertNotNull(flaky.getError());
        assertEquals(ExtensionManager.State.DISABLED, off.getState());
        assertEquals(1, goodStarts[0]);
        assertEquals(1, flakyStarts[0]);
        assertEquals(0, offStarts[0]);
        assertTrue(mpv5.pluginhandling.YabsPluginLoader.registeredModifiers.contains(mod));

        m.stopAll();
        assertEquals(ExtensionManager.State.STOPPED, good.getState());
        assertEquals(ExtensionManager.State.STOPPED, flaky.getState());
        assertEquals(null, flaky.getError());
        assertEquals(ExtensionManager.State.DISABLED, off.getState());
        assertEquals(1, goodStops[0]);
        assertEquals(1, goodOnStops[0]);
        assertEquals(0, m.contributions("switch.good"));
        assertTrue(m.commands().isEmpty());
        assertTrue(m.pages().isEmpty());
        assertFalse(mpv5.pluginhandling.YabsPluginLoader.registeredModifiers.contains(mod));

        // Restarting after switching to a new DB runs start() again (e.g. schema init)
        m.startAll();
        assertEquals(ExtensionManager.State.RUNNING, good.getState());
        assertEquals(ExtensionManager.State.RUNNING, flaky.getState());
        assertEquals(ExtensionManager.State.DISABLED, off.getState());
        assertEquals(2, goodStarts[0]);
        assertEquals(2, flakyStarts[0]);
        assertEquals(0, offStarts[0]);
        assertEquals(2, m.commands().size());
        assertTrue(mpv5.pluginhandling.YabsPluginLoader.registeredModifiers.contains(mod));

        m.stopAll();
        assertFalse(mpv5.pluginhandling.YabsPluginLoader.registeredModifiers.contains(mod));
    }

    @Test
    void stopAllThenStartAllInBackgroundRestartsEnabledPlugins() throws Exception {
        TestHost h = new TestHost();
        ExtensionManager m = new ExtensionManager(dir.toFile(), h);
        final int[] starts = {0};
        final int[] disabledStarts = {0};
        ExtensionManager.Installed active = m.register(new YabsExtension() {
            public String id() { return "bg.restart"; }
            public String name() { return "BG Restart"; }
            public void start(ExtensionContext ctx) {
                starts[0]++;
                ctx.addCommand("BG " + starts[0], "", () -> {});
            }
        });
        ExtensionManager.Installed disabled = m.register(new YabsExtension() {
            public String id() { return "bg.disabled"; }
            public String name() { return "BG Disabled"; }
            public void start(ExtensionContext ctx) {
                disabledStarts[0]++;
            }
        });
        m.setEnabled(disabled, false);

        m.startAll();
        assertEquals(1, starts[0]);
        assertEquals(ExtensionManager.State.RUNNING, active.getState());

        m.stopAll();
        assertEquals(ExtensionManager.State.STOPPED, active.getState());
        assertTrue(m.commands().isEmpty());

        m.startAllInBackground();
        long until = System.currentTimeMillis() + 10000;
        while (System.currentTimeMillis() < until && active.getState() != ExtensionManager.State.RUNNING) {
            Thread.sleep(25);
        }
        assertEquals(ExtensionManager.State.RUNNING, active.getState());
        assertEquals(2, starts[0]);
        assertEquals(ExtensionManager.State.DISABLED, disabled.getState());
        assertEquals(0, disabledStarts[0]);
        assertTrue(m.commands().stream().anyMatch(c -> "BG 2".equals(c.title)));
    }

    @Test
    void stopAllAndRestartCreatesSchemaInNewDatabase() throws Exception {
        String url1 = "jdbc:h2:mem:ext_switch_db1_" + System.nanoTime() + ";DB_CLOSE_DELAY=-1";
        String url2 = "jdbc:h2:mem:ext_switch_db2_" + System.nanoTime() + ";DB_CLOSE_DELAY=-1";
        try (java.sql.Connection c1 = java.sql.DriverManager.getConnection(url1, "sa", "");
             java.sql.Connection c2 = java.sql.DriverManager.getConnection(url2, "sa", "")) {
            final java.sql.Connection[] currentDb = {c1};
            ExtensionManager m = new ExtensionManager(dir.toFile(), new TestHost());
            ExtensionManager.Installed schemaPlugin = m.register(new YabsExtension() {
                public String id() { return "schema.test"; }
                public String name() { return "Schema Test"; }
                public void start(ExtensionContext ctx) throws Exception {
                    try (java.sql.Statement st = currentDb[0].createStatement()) {
                        st.execute("CREATE TABLE IF NOT EXISTS plugin_demo_table (id INT PRIMARY KEY, label VARCHAR(64))");
                        st.execute("INSERT INTO plugin_demo_table (id, label) VALUES (1, 'ok')");
                    }
                }
            });

            m.startAll();
            assertEquals(ExtensionManager.State.RUNNING, schemaPlugin.getState());
            try (java.sql.Statement st = c1.createStatement();
                 java.sql.ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM plugin_demo_table")) {
                assertTrue(rs.next());
                assertEquals(1, rs.getInt(1));
            }

            // Switch to a fresh second database: stop plugins, switch DB, restart plugins
            m.stopAll();
            assertEquals(ExtensionManager.State.STOPPED, schemaPlugin.getState());
            currentDb[0] = c2;
            m.startAll();
            assertEquals(ExtensionManager.State.RUNNING, schemaPlugin.getState());

            // The plugin schema was created in the second database too
            try (java.sql.Statement st = c2.createStatement();
                 java.sql.ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM plugin_demo_table")) {
                assertTrue(rs.next());
                assertEquals(1, rs.getInt(1));
            }
            m.stopAll();
        }
    }

    @Test
    void languageOfTheUser() {
        assertEquals("en", ExtensionManager.languageOf("buildin_en"));
        assertEquals("de", ExtensionManager.languageOf("de"));
        assertEquals("de_DE", ExtensionManager.languageOf("de-DE"));
        assertEquals("de", ExtensionManager.languageOf("DE-Yabs-2.0"));
        assertEquals("fr_FR", ExtensionManager.languageOf("fr_FR"));
    }

    @Test
    void setupOpensOnceOnFirstLoadAndCanBeOpenedFromPluginMenu(@TempDir Path dir) {
        TestHost h = new TestHost();
        ExtensionManager m = new ExtensionManager(dir.toFile(), h);
        final int[] built = {0};
        ExtensionManager.Installed inst = m.register(new YabsExtension() {
            @Override public String id() { return "test.setup"; }
            @Override public String name() { return "Demo"; }
            @Override public void start(ExtensionContext ctx) {
                ctx.addSetup("Demo: Setup", "Demo", "Demo setup", () -> {
                    built[0]++;
                    return new javax.swing.JPanel();
                });
            }
        });

        // First load: opens Setup automatically and registers both menu command and settings page
        assertTrue(m.start(inst));
        assertEquals(1, m.pages().size());
        assertEquals("Demo", m.pages().get(0).title);
        assertEquals(1, m.commands().size());
        assertEquals("Demo: Setup", m.commands().get(0).title);
        assertEquals(1, h.tabs.size());
        assertEquals("Demo \u2013 Setup", h.tabs.get(0));
        assertEquals(1, built[0]);

        // Reload / second load: does NOT auto-open Setup again
        m.reload(inst);
        assertEquals(1, h.tabs.size(), "Setup must not auto-open on subsequent loads");
        assertEquals(1, built[0]);

        // Invoking the Setup menu command opens it again on demand
        m.commands().get(0).action.run();
        assertEquals(2, h.tabs.size());
        assertEquals(2, built[0]);
    }

    @Test
    void setupDoesNotRunOrMarkShownWithoutHost(@TempDir Path dir) {
        ExtensionManager headless = new ExtensionManager(dir.toFile(), null);
        final int[] built = {0};
        YabsExtension plugin = new YabsExtension() {
            @Override public String id() { return "test.setup.headless"; }
            @Override public String name() { return "HeadlessDemo"; }
            @Override public void start(ExtensionContext ctx) {
                ctx.addSetup("HeadlessDemo: Setup", "HeadlessDemo", "desc", () -> {
                    built[0]++;
                    return new javax.swing.JPanel();
                });
            }
        };
        assertTrue(headless.start(headless.register(plugin)));
        assertEquals(0, built[0], "Headless start must not build Setup UI");

        // Later start with a real host still counts as first UI load
        TestHost h = new TestHost();
        ExtensionManager gui = new ExtensionManager(dir.toFile(), h);
        assertTrue(gui.start(gui.register(plugin)));
        assertEquals(1, h.tabs.size());
        assertEquals(1, built[0]);
    }
}
