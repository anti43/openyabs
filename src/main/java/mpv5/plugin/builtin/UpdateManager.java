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
package mpv5.plugin.builtin;

import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;
import mpv5.Main;
import mpv5.globals.Messages;
import mpv5.handler.PluginUpdateCheck;
import mpv5.logging.Log;
import mpv5.plugin.api.ExtensionContext;
import mpv5.plugin.api.YabsExtension;
import mpv5.ui.dialogs.PluginUpdateNotice;
import mpv5.ui.dialogs.UpdateNotice;

/**
 * Built-in plugin: checks GitHub for newer YaBS and plugin versions and fetches
 * the available plugins list ({@value PluginUpdateCheck#INDEX_ASSET}) from the
 * latest release. Users can disable this plugin in Settings &gt; Plugins to
 * turn off all GitHub update and plugin catalog requests.
 */
public final class UpdateManager implements YabsExtension {

    public static final String ID = "yabs.updates";
    private static volatile boolean active;
    private static final AtomicBoolean startupTriggered = new AtomicBoolean(false);

    /**
     * @return true while this built-in plugin is running (not disabled)
     */
    public static boolean isActive() {
        return active;
    }

    @Override
    public String id() {
        return ID;
    }

    @Override
    public String name() {
        return Messages.UPDATES_PLUGIN_NAME.toString();
    }

    @Override
    public String description() {
        return Messages.UPDATES_PLUGIN_DESCRIPTION.toString();
    }

    @Override
    public String author() {
        return "YaBS";
    }

    @Override
    public void start(ExtensionContext ctx) {
        active = true;
        ctx.addCommand(Messages.UPDATE_CHECK_NOW.toString(), Messages.UPDATE_HINT.toString(),
                () -> UpdateNotice.checkNow(null));
        ctx.addCommand(Messages.PLUGIN_UPDATE_CHECK_NOW.toString(), Messages.PLUGIN_UPDATE_HINT.toString(),
                () -> PluginUpdateNotice.checkNow(null));
        if (startupTriggered.compareAndSet(false, true)) {
            if (!Main.HEADLESS && !mpv5.db.common.ConnectionTypeHandler.DEVMODE) {
                UpdateNotice.checkAtStart();
                PluginUpdateNotice.checkAtStart();
            } else if (mpv5.db.common.ConnectionTypeHandler.DEVMODE) {
                Log.Print("Update check: not in developer mode");
            }
        }
        PluginUpdateNotice.notifyChanged();
    }

    @Override
    public void stop() {
        active = false;
        PluginUpdateCheck.setLastResult(Collections.emptyList());
        PluginUpdateCheck.setLastCatalog(null);
        PluginUpdateNotice.resetState();
    }
}
