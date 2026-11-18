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
package mpv5.ui.misc;

import java.awt.Component;
import java.awt.Desktop;
import java.awt.KeyboardFocusManager;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.net.URI;
import java.util.Locale;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import mpv5.globals.Constants;
import mpv5.globals.Messages;
import mpv5.logging.Log;

/**
 * The online help (F1): the wiki of the repository. F1 works in every window
 * of YaBS; a view or dialog can name its own wiki page with the client
 * property {@link #PROPERTY_HELP_PAGE}.
 */
public final class Help {

    /**
     * Client property of a component: the wiki page about it, either one page
     * (e.g. "Writing-plugins-(EN)") or "German page|English page" (e.g.
     * "Vorlagen|Templates-(EN)")
     */
    public static final String PROPERTY_HELP_PAGE = "yabs.helpPage";
    /**
     * The German entry page of the wiki
     */
    public static final String START_DE = "Openyabs-Dokumentation-(DE)";
    /**
     * The English entry page of the wiki
     */
    public static final String START_EN = "Openyabs-documentation-(EN)";
    private static boolean installed = false;

    private Help() {
    }

    /**
     * F1 (without modifiers) opens the help in every window of YaBS, also
     * where a component uses the key itself
     */
    public static synchronized void install() {
        if (installed) {
            return;
        }
        installed = true;
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(e -> {
            if (e.getID() == KeyEvent.KEY_PRESSED && e.getKeyCode() == KeyEvent.VK_F1 && e.getModifiersEx() == 0) {
                e.consume();
                Component c = e.getComponent();
                SwingUtilities.invokeLater(() -> show(c));
                return true;
            }
            return false;
        });
    }

    /**
     * Opens the wiki page of the component (or of its parents), else the
     * entry page of the wiki in the language of the user
     *
     * @param c may be null
     */
    public static void show(Component c) {
        browse(url(page(c)));
    }

    /**
     * @param page a wiki page, may be null for the entry page
     * @return the address of the page
     */
    public static String url(String page) {
        String base = Constants.WIKI.endsWith("/") ? Constants.WIKI : Constants.WIKI + "/";
        return base + (page == null || page.trim().isEmpty() ? (german() ? START_DE : START_EN) : page.trim());
    }

    /**
     * @param c may be null
     * @return the wiki page named by the component or its parents (in the
     * language of the user), null if none
     */
    public static String page(Component c) {
        for (Component p = c; p != null; p = p.getParent()) {
            if (p instanceof JComponent) {
                Object v = ((JComponent) p).getClientProperty(PROPERTY_HELP_PAGE);
                if (v instanceof String && !((String) v).trim().isEmpty()) {
                    return pick((String) v);
                }
            }
            if (p instanceof javax.swing.RootPaneContainer) {
                Object v = ((javax.swing.RootPaneContainer) p).getRootPane().getClientProperty(PROPERTY_HELP_PAGE);
                if (v instanceof String && !((String) v).trim().isEmpty()) {
                    return pick((String) v);
                }
            }
        }
        return null;
    }

    /**
     * @param pages "page" or "German page|English page"
     * @return the page in the language of the user
     */
    static String pick(String pages) {
        String[] p = pages.split("\\|", 2);
        if (p.length < 2) {
            return p[0].trim();
        }
        return (german() ? p[0] : p[1]).trim();
    }

    private static boolean german() {
        try {
            String l = mpv5.db.objects.User.getCurrentUser().__getLocale();
            if (l != null && !l.trim().isEmpty()) {
                return l.toLowerCase(Locale.ROOT).startsWith("de");
            }
        } catch (Throwable t) {
            //no user yet
        }
        return Locale.getDefault().getLanguage().equals("de");
    }

    /**
     * Opens the address in the browser: with the desktop of Java, else with
     * xdg-open (Linux) or open (macOS); if nothing works the address is shown
     * to copy it
     *
     * @param url
     */
    public static void browse(String url) {
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(url));
                return;
            }
        } catch (Exception e) {
            Log.Debug(Help.class, e);
        }
        String os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        String[] cmd = os.contains("mac") ? new String[]{"open", url}
                : os.contains("win") ? new String[]{"rundll32", "url.dll,FileProtocolHandler", url}
                : new String[]{"xdg-open", url};
        try {
            new ProcessBuilder(cmd).start();
            return;
        } catch (Exception e) {
            Log.Debug(Help.class, e);
        }
        JTextField f = new JTextField(url);
        f.setEditable(false);
        f.selectAll();
        Window w = KeyboardFocusManager.getCurrentKeyboardFocusManager().getActiveWindow();
        JOptionPane.showMessageDialog(w, new Object[]{Messages.HELP_OPEN_FAILED.toString(), f},
                Messages.HELP_TITLE.toString(), JOptionPane.INFORMATION_MESSAGE);
    }
}
