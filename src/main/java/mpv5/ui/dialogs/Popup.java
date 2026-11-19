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
package mpv5.ui.dialogs;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GraphicsEnvironment;
import java.awt.KeyboardFocusManager;
import java.awt.Window;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Icon;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.globals.Messages;

/**
 *
 *  
 */
public class Popup {

    public static String WARN = Messages.WARNING.getValue();
    public static String ERROR = Messages.ERROR_OCCURED.getValue();
    public static String NOTICE = Messages.NOTICE.getValue();
    public static String GENERAL_ERROR = Messages.ERROR_OCCURED.getValue();
    /**
     * This identifier is the parent container opf popups, to keep them all in one screen
     */
    private static final ThreadLocal<Boolean> SILENT = ThreadLocal.withInitial(() -> Boolean.FALSE);

    /**
     * Runs work without message boxes on this thread (e.g. a request of a web
     * server): messages go to the log and the status bar, questions are
     * answered with no
     *
     * @param <T>
     * @param work
     * @return the result of the work
     * @throws Exception of the work
     */
    public static <T> T silently(java.util.concurrent.Callable<T> work) throws Exception {
        boolean before = SILENT.get();
        SILENT.set(Boolean.TRUE);
        try {
            return work.call();
        } finally {
            SILENT.set(before);
        }
    }

    private static boolean silent(Object text) {
        if (!SILENT.get()) {
            return false;
        }
        mpv5.logging.Log.Debug(Popup.class, "(no message box) " + text);
        try {
            mpv5.YabsViewProxy.instance().addMessage(String.valueOf(text), java.awt.Color.YELLOW);
        } catch (Exception e) {
            //no view
        }
        return true;
    }

    /**
     * The main window of the application: the fallback parent of popups when
     * no other window of the application is active. No frame without a
     * display (headless start, tests): dialogs then have no parent
     */
    public static Component identifier = GraphicsEnvironment.isHeadless() ? null
            : new Frame(GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration());

    /**
     * Prompts the user with a text box
     * @param message
     * @return
     */
    public static String Enter_Value(Object... message) {
        if (silent(message)) {
            return null;
        }
        String text = "";
        for (int i = 0; i < message.length; i++) {
            if (message[i] != null) {
                text += message[i].toString();
            }
        }
        final String q = text;
        return onEdt(() -> JOptionPane.showInputDialog(owner(), q), null);
    }

    /**
     * A Y_N_dialog
     * @param text
     * @return
     */
    public static boolean Y_N_dialog(Object text) {
        return Y_N_dialog(text, Messages.ARE_YOU_SURE.toString());
    }

    /**
     * A Y_N_dialog
     * @param text
     * @param label 
     * @return
     */
    public static boolean Y_N_dialog(Object text, Object label) {
        return Y_N_dialog(null, text, label);
    }

    /**
     * A Y_N_dialog
     * @param parent
     * @param text
     * @param label
     * @return
     */
    public static boolean Y_N_dialog(Component parent, Object text, Object label) {
        if (silent(text)) {
            return false;
        }
        return onEdt(() -> JOptionPane.showConfirmDialog(owner(parent), prepareText(String.valueOf(text)), label.toString(), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, new javax.swing.ImageIcon(new Popup().getClass().getResource("/images/32/warning.png"))) == JOptionPane.YES_OPTION, false);
    }

    /**
     * A Ok or Cancel -dialog
     * @param text
     * @param label
     * @return
     */
    public static boolean OK_dialog(Object text, String label) {
        if (silent(text)) {
            return false;
        }
        return onEdt(() -> JOptionPane.showConfirmDialog(owner(), prepareText(String.valueOf(text)), label, JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, new javax.swing.ImageIcon(new Popup().getClass().getResource("/images/32/warning.png"))) == JOptionPane.OK_OPTION, false);
    }

    /**
     * Shows a notice
     * @param parent
     * @param text
     */
    public static void notice(Component parent, Object text) {
        if (silent(text)) {
            return;
        }
        message(parent, prepareText(String.valueOf(text)), Popup.NOTICE, JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Shows a warning
     * @param parent
     * @param text
     */
    public static void warn(Component parent, Object text) {
        if (silent(text)) {
            return;
        }
        message(parent, prepareText(String.valueOf(text)), Popup.WARN, JOptionPane.WARNING_MESSAGE);
    }

    /**
     * Shows an error
     * @param parent
     * @param text
     */
    public static void error(Component parent, Object text) {
        if (silent(text)) {
            return;
        }
        message(parent, prepareText(String.valueOf(text)), Popup.ERROR, JOptionPane.ERROR_MESSAGE);
    }

    /**
     * A convenience method to show a notice popup with
     * the mainframe as parent
     * @param text
     */
    public static void warn(Object text) {
        if (silent(text)) {
            return;
        }
        message(null, prepareText(String.valueOf(text)), Popup.WARN, JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * A convenience method to show a notice popup with
     * the mainframe as parent
     * @param text
     */
    public static void notice(Object text) {
        if (silent(text)) {
            return;
        }
        message(null, prepareText(String.valueOf(text)), Popup.NOTICE, JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * A convenience method to show a notice popup with
     * the mainframe as parent
     * @param text
     * @param boxWidth
     * @param boxLength
     */
    public static void notice(Object text, int boxWidth, int boxLength) {
        if (silent(text)) {
            return;
        }
        message(null, prepareText(String.valueOf(text), boxWidth, boxLength), Popup.NOTICE, JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * A convenience method to show an error popup with
     * the mainframe as parent
     * @param x
     */
    public static void error(Exception x) {
//        x.printStackTrace();
        error(null, x.getLocalizedMessage());
    }

    /**
     * The window a popup, file chooser or dialog belongs to, so it opens in
     * front of the window the user works in (a wizard, a dialog, the main
     * window) instead of behind it: the active window of the application if it
     * is the given parent or one of its child windows, else the window of the
     * parent, else the main window
     *
     * @param parent may be null
     * @return the owner, null without a display
     */
    public static Window owner(Component parent) {
        if (GraphicsEnvironment.isHeadless()) {
            return null;
        }
        Window wanted = parent == null ? null
                : parent instanceof Window ? (Window) parent : SwingUtilities.getWindowAncestor(parent);
        if (wanted != null && !wanted.isShowing()) {
            wanted = null;
        }
        Window active = KeyboardFocusManager.getCurrentKeyboardFocusManager().getActiveWindow();
        if (active != null && active.isShowing()) {
            if (wanted == null || isOwnedBy(active, wanted)) {
                return active;
            }
        }
        if (wanted != null) {
            return wanted;
        }
        Component main = identifier;
        if (main != null) {
            Window w = main instanceof Window ? (Window) main : SwingUtilities.getWindowAncestor(main);
            if (w != null && w.isShowing()) {
                return w;
            }
        }
        //nothing of the application on the screen (start, login): the topmost visible window
        for (Window w : Window.getWindows()) {
            if (w.isShowing() && (w instanceof Frame || w instanceof java.awt.Dialog)) {
                return w;
            }
        }
        return main instanceof Window ? (Window) main : null;
    }

    /**
     * @return the owner of popups without a given parent, see {@link #owner(Component)}
     */
    public static Window owner() {
        return owner(null);
    }

    private static boolean isOwnedBy(Window w, Window owner) {
        for (Window x = w; x != null; x = x.getOwner()) {
            if (x == owner) {
                return true;
            }
        }
        return false;
    }

    private static void message(Component parent, Object text, String title, int type) {
        onEdt(() -> {
            JOptionPane.showMessageDialog(owner(parent), text, title, type);
            return null;
        }, null);
    }

    /**
     * Runs a dialog on the event thread: popups of background work then wait
     * for each other and appear one after the other in the order they were
     * asked for
     */
    private static <T> T onEdt(java.util.function.Supplier<T> dialog, T fallback) {
        if (SwingUtilities.isEventDispatchThread() || GraphicsEnvironment.isHeadless()) {
            return dialog.get();
        }
        java.util.concurrent.atomic.AtomicReference<T> result = new java.util.concurrent.atomic.AtomicReference<T>(fallback);
        try {
            SwingUtilities.invokeAndWait(() -> result.set(dialog.get()));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (java.lang.reflect.InvocationTargetException e) {
            mpv5.logging.Log.Debug(Popup.class, e.getCause());
        }
        return result.get();
    }

    private static Object prepareText(String s) {
        return prepareText(s, 300, 80);
    }

    private static Object prepareText(String s, int boxWidth, int boxLength) {
        JTextArea text = new JTextArea(s);
        JScrollPane scroll = new JScrollPane(text);
        text.setLineWrap(true);
        text.setWrapStyleWord(true);
        text.setEditable(false);
        text.setBorder(new EmptyBorder(1, 1, 1, 1));
        text.setBackground(JOptionPane.getRootFrame().getBackground());
        scroll.setBackground(JOptionPane.getRootFrame().getBackground());
        scroll.setBorder(new EmptyBorder(0, 0, 0, 0));
        scroll.setPreferredSize(new Dimension(boxWidth, boxLength));
        return scroll;
    }

    /**
     * Show a selection box
     * @param objects
     * @param message
     * @return the selected object or null
     */
    public static DatabaseObject SelectValue(ArrayList<DatabaseObject> objects, Object message) {
        return onEdt(() -> (DatabaseObject) JOptionPane.showInputDialog(owner(), message, "",
                JOptionPane.PLAIN_MESSAGE, (Icon) null, objects.toArray(), objects.get(0)), null);
    }

    /**
     * Show a search box
     * @param t
     * @return the selected object or null
     */
    public static DatabaseObject SelectValue(Context t) {
        return Search2.showSearchFor(t);
//        return null;
    }

    /**
     * 
     * @param list
     * @param message
     */
    public static void notice(List list, Object message) {
        String t = message.toString() + "\n";

        for (int i = 0; i < list.size(); i++) {
            Object l = list.get(i);
            if (l instanceof DatabaseObject) {
                DatabaseObject databaseObject = (DatabaseObject) l;
                t += databaseObject.__getCname() + " [" + databaseObject.getDbIdentity() + "]" + "\n";
            } else {
                t += list.get(i) + "\n";
            }
        }
        Popup.notice(t, 300, list.size() * 5 + 100);
    }

    private Popup() {
    }
}
