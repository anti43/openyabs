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
package mpv5.ui.modern;

import com.formdev.flatlaf.FlatLightLaf;
import javax.swing.UIManager;

/**
 * The modern look and feel of YaBS: FlatLaf with a bit of depth (button
 * gradients and shadows, visible borders, rounded corners, drop shadows on
 * popups), so the classic panels and all dialogs fit the modern views.
 * <p>
 * The colors and sizes are in YabsLookAndFeel.properties (same package).
 */
public class YabsLookAndFeel extends FlatLightLaf {

    public static final String NAME = "YaBS Modern";

    /**
     * Makes the look and feel selectable (e.g. in the user settings)
     */
    public static void register() {
        for (UIManager.LookAndFeelInfo i : UIManager.getInstalledLookAndFeels()) {
            if (i.getClassName().equals(YabsLookAndFeel.class.getName())) {
                return;
            }
        }
        UIManager.installLookAndFeel(NAME, YabsLookAndFeel.class.getName());
    }

    /**
     * @return true if the current look and feel is this one
     */
    public static boolean isActive() {
        return UIManager.getLookAndFeel() instanceof YabsLookAndFeel;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return "Modern look and feel for YaBS (based on FlatLaf)";
    }
}
