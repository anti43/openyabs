/*
 *  This file is part of YaBS.
 *
 *      YaBS is free software: you can redistribute it and/or modify
 *      it under the terms of the GNU General Public License as published by
 *      the Free Software Foundation, either version 3 of the License, or
 *      (at your option) any later version.
 *
 *      YaBS is distributed in the hope that it will be useful,
 *      but WITHOUT ANY WARRANTY; without even the implied warranty of
 *      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *      GNU General Public License for more details.
 *
 *      You should have received a copy of the GNU General Public License
 *      along with YaBS.  If not, see <http://www.gnu.org/licenses/>.
 */
package mpv5.ui;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.swing.UIManager;

/**
 * The look and feel of YaBS. FlatLaf (light or dark) is the default; it is
 * modern and scales on high resolution displays. The choice is stored per
 * user as look and feel class name ({@link mpv5.db.objects.User#setLaf}).
 */
public final class Appearance {

    public static final String LIGHT = FlatLightLaf.class.getName();
    public static final String DARK = FlatDarkLaf.class.getName();
    /**
     * Stored value for "use the look and feel of the operating system"
     */
    public static final String SYSTEM = "system";
    /**
     * Former defaults - users who never chose a look and feel deliberately
     * have one of these stored and get the new default
     */
    private static final Set<String> LEGACY_DEFAULTS = new HashSet<String>(Arrays.asList(
            "javax.swing.plaf.metal.MetalLookAndFeel",
            "com.sun.java.swing.plaf.windows.WindowsLookAndFeel",
            "com.sun.java.swing.plaf.windows.WindowsClassicLookAndFeel",
            "com.sun.java.swing.plaf.gtk.GTKLookAndFeel",
            "com.sun.java.swing.plaf.motif.MotifLookAndFeel",
            "com.apple.laf.AquaLookAndFeel",
            "apple.laf.AquaLookAndFeel"));
    private static boolean installed;

    private Appearance() {
    }

    /**
     * Make the FlatLaf themes selectable (e.g. in the user settings)
     */
    public static synchronized void install() {
        if (!installed) {
            UIManager.installLookAndFeel("FlatLaf Light", LIGHT);
            UIManager.installLookAndFeel("FlatLaf Dark", DARK);
            installed = true;
        }
    }

    /**
     * @param laf the stored look and feel of the user
     * @return the class name of the look and feel to use
     */
    public static String resolve(String laf) {
        if (laf == null || laf.trim().isEmpty() || LEGACY_DEFAULTS.contains(laf.trim())) {
            return LIGHT;
        }
        if (SYSTEM.equals(laf.trim())) {
            return UIManager.getSystemLookAndFeelClassName();
        }
        return laf.trim();
    }

    /**
     * @param laf the stored look and feel of the user
     * @return true if it resolves to the dark theme
     */
    public static boolean isDark(String laf) {
        return DARK.equals(resolve(laf));
    }
}
