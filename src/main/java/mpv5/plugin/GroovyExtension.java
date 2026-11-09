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

import groovy.lang.Binding;
import groovy.lang.Closure;
import groovy.lang.GroovyShell;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import mpv5.plugin.api.ExtensionContext;
import mpv5.plugin.api.YabsExtension;

/**
 * A plugin written in Groovy: a .groovy file in the plugins folder.
 * <p>
 * The script runs when the plugin starts, with {@code yabs} (the
 * {@link ExtensionContext}) bound. Its name and description are comment
 * lines at the top; closures can be used for commands and listeners. A
 * closure {@code onStop} is called when it stops.
 * <pre>
 * // @name Hello
 * // @description Says hello
 * yabs.addCommand("Say hello", "A greeting", { yabs.notify("Hello from Groovy!") })
 * </pre> A script may also return an object implementing
 * {@link YabsExtension}; then that object is the plugin.
 */
public final class GroovyExtension implements YabsExtension {

    private static final Pattern TAG = Pattern.compile("^\\s*//\\s*@(\\w+)\\s+(.*)$");
    private final File file;
    private final Map<String, String> tags = new HashMap<String, String>();
    private Binding binding;
    private YabsExtension delegate;

    /**
     * @param file the script
     * @throws IOException if it can not be read
     */
    public GroovyExtension(File file) throws IOException {
        this.file = file;
        for (String line : Files.readAllLines(file.toPath(), StandardCharsets.UTF_8)) {
            Matcher m = TAG.matcher(line);
            if (m.matches()) {
                tags.put(m.group(1).toLowerCase(), m.group(2).trim());
            } else if (!line.trim().isEmpty() && !line.trim().startsWith("//")) {
                //the tags are at the top
                break;
            }
        }
    }

    private String tag(String key, String fallback) {
        String v = tags.get(key);
        return v == null || v.isEmpty() ? fallback : v;
    }

    private String base() {
        String n = file.getName();
        return n.endsWith(".groovy") ? n.substring(0, n.length() - 7) : n;
    }

    @Override
    public String id() {
        return tag("id", "groovy." + base());
    }

    @Override
    public String name() {
        return delegate != null ? delegate.name() : tag("name", base());
    }

    @Override
    public String description() {
        return delegate != null ? delegate.description() : tag("description", "");
    }

    @Override
    public String version() {
        return delegate != null ? delegate.version() : tag("version", "1.0");
    }

    @Override
    public String author() {
        return delegate != null ? delegate.author() : tag("author", "");
    }

    /**
     * @return the script
     */
    public File file() {
        return file;
    }

    @Override
    public void start(ExtensionContext ctx) throws Exception {
        binding = new Binding();
        binding.setVariable("yabs", ctx);
        binding.setVariable("plugin", this);
        GroovyShell shell = new GroovyShell(GroovyExtension.class.getClassLoader(), binding);
        Object result = shell.evaluate(file);
        if (result instanceof YabsExtension) {
            delegate = (YabsExtension) result;
            delegate.start(ctx);
        }
    }

    @Override
    public void stop() {
        if (delegate != null) {
            delegate.stop();
            delegate = null;
        } else if (binding != null && binding.hasVariable("onStop") && binding.getVariable("onStop") instanceof Closure) {
            ((Closure<?>) binding.getVariable("onStop")).call();
        }
        binding = null;
    }
}
