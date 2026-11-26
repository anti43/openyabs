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
package mpv5.plugin;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import mpv5.plugin.api.YabsExtension;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Writes the plugin list of a release (openyabs-plugins.json, see
 * {@link mpv5.handler.PluginUpdateCheck}): id, name, version, jar and the
 * YaBS version needed of every plugin in the given jars. Run by the build
 * (task pluginIndex); the plugins are only created, not started.
 * <p>
 * Arguments: the output file, the YaBS version, the plugin jars
 */
public final class PluginIndexWriter {

    private PluginIndexWriter() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            throw new IllegalArgumentException("Arguments: <output file> <YaBS version> <plugin jars...>");
        }
        List<File> jars = new ArrayList<File>();
        for (int i = 2; i < args.length; i++) {
            jars.add(new File(args[i]));
        }
        String json = write(args[1], jars);
        File out = new File(args[0]);
        if (out.getParentFile() != null) {
            out.getParentFile().mkdirs();
        }
        Files.write(out.toPath(), json.getBytes(StandardCharsets.UTF_8));
        System.out.println("Wrote " + out);
    }

    /**
     * @param yabsVersion
     * @param jars
     * @return the plugin list as JSON
     * @throws Exception if a jar has no plugin
     */
    public static String write(String yabsVersion, List<File> jars) throws Exception {
        JSONArray plugins = new JSONArray();
        for (File jar : jars) {
            int n = 0;
            try (URLClassLoader l = new URLClassLoader(new URL[]{jar.toURI().toURL()}, PluginIndexWriter.class.getClassLoader())) {
                for (YabsExtension e : ServiceLoader.load(YabsExtension.class, l)) {
                    if (e.getClass().getClassLoader() != l) {
                        continue;
                    }
                    JSONObject p = new JSONObject();
                    p.put("id", e.id());
                    p.put("name", e.name());
                    p.put("version", e.version());
                    p.put("jar", jar.getName());
                    if (e.minYabsVersion() != null && !e.minYabsVersion().trim().isEmpty()) {
                        p.put("minYabsVersion", e.minYabsVersion().trim());
                    }
                    plugins.put(p);
                    n++;
                }
            }
            if (n == 0) {
                throw new IllegalStateException("No YaBS plugin in " + jar);
            }
        }
        JSONObject o = new JSONObject();
        o.put("yabsVersion", yabsVersion);
        o.put("plugins", plugins);
        return o.toString(2);
    }
}
