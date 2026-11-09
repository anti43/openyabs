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

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import mpv5.globals.Messages;
import mpv5.ui.panels.MPControlPanel;

/**
 * The menu "Plugins": the commands of the running plugins and the plugin
 * manager
 */
public final class PluginMenu {

    private static JMenu menu;

    private PluginMenu() {
    }

    /**
     * Adds the menu to the menu bar (before the last menu, help)
     *
     * @param bar
     */
    public static void install(JMenuBar bar) {
        if (menu != null || bar == null) {
            return;
        }
        menu = new JMenu(Messages.PLUGINS_MENU.toString());
        menu.setName("pluginsMenu");
        int at = Math.max(0, bar.getMenuCount() - 1);
        bar.add(menu, at);
        refresh();
        bar.revalidate();
    }

    /**
     * Builds the menu again (on the Swing thread)
     */
    static void refresh() {
        if (menu == null) {
            return;
        }
        menu.removeAll();
        String plugin = null;
        for (ExtensionManager.Command c : ExtensionManager.instance().commands()) {
            if (!c.plugin.equals(plugin)) {
                if (plugin != null) {
                    menu.addSeparator();
                }
                plugin = c.plugin;
                JMenuItem h = new JMenuItem(c.plugin);
                h.setEnabled(false);
                menu.add(h);
            }
            JMenuItem i = new JMenuItem("   " + c.title);
            if (!c.description.isEmpty()) {
                i.setToolTipText(c.description);
            }
            i.addActionListener(e -> c.action.run());
            menu.add(i);
        }
        if (plugin == null) {
            JMenuItem none = new JMenuItem(Messages.PLUGINS_NO_COMMANDS.toString());
            none.setEnabled(false);
            menu.add(none);
        }
        menu.addSeparator();
        JMenuItem manage = new JMenuItem(Messages.PLUGINS_MANAGE.toString() + "...");
        manage.addActionListener(e -> openManager());
        menu.add(manage);
    }

    /**
     * Opens the plugin manager in the settings
     */
    public static void openManager() {
        for (MPControlPanel.Entry e : MPControlPanel.entries()) {
            if (e.isPanel(mpv5.ui.dialogs.subcomponents.ControlPanel_Plugins.class)) {
                MPControlPanel.show(e);
                return;
            }
        }
    }
}
