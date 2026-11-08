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
package mpv5.globals;

import java.io.InputStream;
import java.util.Properties;

/**
 * The version of this build: 2.0.&lt;build number&gt; for the builds of the
 * GitHub workflow, the version of the tag for releases, "2.0-dev" for local
 * builds. Set by the build in mpv5/version.properties.
 */
public final class VersionInfo {

    /**
     * Version line of this code (language and template files in /extra are
     * named after it)
     */
    public static final String LINE = "2.0";
    private static final String FALLBACK = LINE + "-dev";
    private static String number;

    private VersionInfo() {
    }

    /**
     * @return e.g. "2.0.123"
     */
    public static synchronized String number() {
        if (number == null) {
            number = read();
        }
        return number;
    }

    private static String read() {
        try (InputStream in = VersionInfo.class.getResourceAsStream("/mpv5/version.properties")) {
            if (in != null) {
                Properties p = new Properties();
                p.load(in);
                String v = p.getProperty("version", "").trim();
                //not filled in (e.g. started from the sources without Gradle)
                if (!v.isEmpty() && !v.contains("$")) {
                    return v;
                }
            }
        } catch (Exception e) {
            //fallback below
        }
        Package p = VersionInfo.class.getPackage();
        if (p != null && p.getImplementationVersion() != null) {
            return p.getImplementationVersion();
        }
        return FALLBACK;
    }
}
