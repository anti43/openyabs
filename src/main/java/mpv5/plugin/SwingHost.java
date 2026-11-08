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

import java.awt.Color;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import mpv5.YabsViewProxy;
import mpv5.globals.Messages;
import mpv5.logging.Log;

/**
 * The plugins in the YaBS window: tabs, messages, errors, the plugin menu
 */
final class SwingHost implements ExtensionManager.Host {

    @Override
    public void openTab(String title, JComponent content) {
        try {
            YabsViewProxy.instance().getIdentifierView().addOrShowTab(content, title);
        } catch (Exception e) {
            Log.Debug(SwingHost.class, e);
        }
    }

    @Override
    public void notify(String message) {
        try {
            YabsViewProxy.instance().addMessage(message);
        } catch (Exception e) {
            Log.Debug(SwingHost.class, e);
        }
    }

    @Override
    public void ui(Runnable r) {
        if (SwingUtilities.isEventDispatchThread()) {
            r.run();
        } else {
            SwingUtilities.invokeLater(r);
        }
    }

    @Override
    public void changed() {
        ui(PluginMenu::refresh);
    }

    @Override
    public void error(String plugin, Throwable t) {
        try {
            YabsViewProxy.instance().addMessage(String.format(Messages.PLUGINS_FAILED.toString(), plugin)
                    + " " + (t.getMessage() == null ? t.getClass().getSimpleName() : t.getMessage()), Color.RED);
        } catch (Exception e) {
            Log.Debug(SwingHost.class, e);
        }
    }
}
