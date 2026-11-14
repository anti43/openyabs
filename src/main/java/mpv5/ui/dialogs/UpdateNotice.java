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
package mpv5.ui.dialogs;

import java.awt.Component;
import java.net.URI;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import mpv5.globals.LocalSettings;
import mpv5.globals.Messages;
import mpv5.globals.VersionInfo;
import mpv5.handler.UpdateCheck;
import mpv5.logging.Log;

/**
 * Tells the user about a newer release of YaBS: at start (if switched on,
 * once per version unless "Later") or when asked in the settings
 */
public final class UpdateNotice {

    /**
     * Local setting: the version the user chose to skip
     */
    public static final String SKIPPED = "updateskipped";

    private UpdateNotice() {
    }

    /**
     * In the background a few seconds after the start: asks GitHub and
     * shows a notice if there is a newer release
     */
    public static void checkAtStart() {
        if (!UpdateCheck.enabledAtStart()) {
            return;
        }
        Thread t = new Thread(() -> {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                return;
            }
            UpdateCheck.Release r = UpdateCheck.latest();
            if (r != null && UpdateCheck.isNewer(r.version, VersionInfo.number())
                    && !r.version.equals(LocalSettings.getProperty(SKIPPED))) {
                SwingUtilities.invokeLater(() -> show(null, r, false));
            }
        }, "update check");
        t.setDaemon(true);
        t.start();
    }

    /**
     * Asks GitHub now and says what it found
     *
     * @param parent
     */
    public static void checkNow(final Component parent) {
        Thread t = new Thread(() -> {
            UpdateCheck.Release r = UpdateCheck.latest();
            SwingUtilities.invokeLater(() -> {
                if (r == null) {
                    Popup.notice(parent, Messages.UPDATE_CHECK_FAILED.toString());
                } else if (UpdateCheck.isNewer(r.version, VersionInfo.number())) {
                    show(parent, r, true);
                } else {
                    Popup.notice(parent, String.format(Messages.UPDATE_NONE.toString(), VersionInfo.number()));
                }
            });
        }, "update check");
        t.setDaemon(true);
        t.start();
    }

    private static void show(Component parent, UpdateCheck.Release r, boolean asked) {
        String text = String.format(Messages.UPDATE_FOUND.toString(), r.version, VersionInfo.number());
        String open = Messages.UPDATE_OPEN.toString();
        String later = Messages.UPDATE_LATER.toString();
        String skip = Messages.UPDATE_SKIP.toString();
        Object[] options = asked ? new Object[]{open, later} : new Object[]{open, skip, later};
        int c = JOptionPane.showOptionDialog(parent, text + "\n\n" + Messages.UPDATE_HOW_TO_OFF,
                Messages.UPDATE_TITLE.toString(), JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE,
                null, options, open);
        if (c == 0) {
            try {
                java.awt.Desktop.getDesktop().browse(new URI(r.url));
            } catch (Exception e) {
                Log.Debug(UpdateNotice.class, e);
                Popup.notice(parent, r.url);
            }
        } else if (!asked && c == 1) {
            LocalSettings.setProperty(SKIPPED, r.version);
            LocalSettings.save();
        }
    }
}
