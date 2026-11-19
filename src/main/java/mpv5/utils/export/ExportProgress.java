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
package mpv5.utils.export;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Window;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import mpv5.globals.Messages;
import mpv5.logging.Log;

/**
 * The steps of creating a document (form fields and scripts, template, PDF,
 * e-invoice, validation, saving): each step is logged with its duration
 * (debug log), and, if the export was started from the user interface and
 * takes a moment, shown in a small progress window with the steps done so
 * far.
 * <br/>
 * The code doing the work reports its steps with {@link #step(Object)}, which
 * goes to the progress of the current thread (and only logs nothing if there
 * is none), so the steps need not be handed through every method.
 */
public final class ExportProgress {

    /**
     * The progress window is only shown if the export takes longer
     */
    private static final int SHOW_DELAY_MS = 400;
    private static final ThreadLocal<ExportProgress> CURRENT = new ThreadLocal<ExportProgress>();

    private final String title;
    private final long start = System.nanoTime();
    private final List<String> steps = new ArrayList<String>();
    private long stepStart = start;
    private String stepName;
    private boolean finished;
    //the window, EDT only
    private final boolean ui;
    private JDialog dialog;
    private JLabel current;
    private DefaultListModel<String> doneModel;
    private Timer showTimer;
    private boolean suspended;

    private ExportProgress(String title, boolean ui) {
        this.title = title;
        this.ui = ui && !java.awt.GraphicsEnvironment.isHeadless();
        Log.Debug(ExportProgress.class, "[" + title + "] started");
        if (this.ui) {
            onEdt(() -> {
                showTimer = new Timer(SHOW_DELAY_MS, e -> show());
                showTimer.setRepeats(false);
                showTimer.start();
            });
        }
    }

    /**
     * @return the progress of the current thread, or null
     */
    public static ExportProgress current() {
        return CURRENT.get();
    }

    /**
     * The next step of the progress of the current thread: logs the duration
     * of the previous one. Without a progress on this thread, nothing happens.
     *
     * @param name the step (a Messages entry or text)
     */
    public static void step(Object name) {
        ExportProgress p = CURRENT.get();
        if (p != null) {
            p.next(String.valueOf(name));
        }
    }

    /**
     * Runs the work on a background thread, with a progress (window shown
     * after a moment), then the result on the event dispatch thread. If this
     * is not the event dispatch thread (e.g. a mass export), both run here,
     * one after the other, and only the timings are logged.
     *
     * @param <T>
     * @param title the name of the export, e.g. "PDF Invoice R-1001"
     * @param work the long running part, may report steps; must not show
     * dialogs (use {@link #onEdtAndWait(Callable)})
     * @param result gets the result of the work (null if it failed) on the
     * event dispatch thread, may be null
     */
    public static <T> void run(final String title, final Callable<T> work, final Result<T> result) {
        final boolean edt = SwingUtilities.isEventDispatchThread();
        final ExportProgress p = new ExportProgress(title, edt);
        Runnable all = () -> {
            ExportProgress before = CURRENT.get();
            CURRENT.set(p);
            T value = null;
            Exception failure = null;
            try {
                value = work.call();
            } catch (Exception ex) {
                failure = ex;
                Log.Debug(ExportProgress.class, "[" + title + "] failed in step " + p.stepName + ": " + ex);
                Log.Debug(ex);
            }
            final T fvalue = value;
            final Exception ffailure = failure;
            try {
                if (result != null) {
                    p.deliver(edt, () -> result.done(fvalue, ffailure));
                }
            } catch (Exception ex) {
                Log.Debug(ex);
            } finally {
                p.finish();
                if (before == null) {
                    CURRENT.remove();
                } else {
                    CURRENT.set(before);
                }
            }
        };
        if (edt) {
            Thread t = new Thread(all, "export: " + title);
            t.setDaemon(true);
            t.start();
        } else {
            all.run();
        }
    }

    /**
     * Gets the result of {@link #run(String, Callable, Result)}
     *
     * @param <T>
     */
    public interface Result<T> {

        /**
         * On the event dispatch thread (if started from there), steps
         * reported here belong to the progress still
         *
         * @param value
         * @param failure
         * @throws Exception
         */
        void done(T value, Exception failure) throws Exception;
    }

    /**
     * Asks the user (a dialog) from the export: the progress window is hidden
     * meanwhile. From the event dispatch thread the question is asked
     * directly.
     *
     * @param <T>
     * @param question
     * @return the answer
     * @throws Exception
     */
    public static <T> T onEdtAndWait(final Callable<T> question) throws Exception {
        final ExportProgress p = CURRENT.get();
        if (p != null) {
            p.suspend(true);
        }
        try {
            if (SwingUtilities.isEventDispatchThread()) {
                return question.call();
            }
            final AtomicReference<T> answer = new AtomicReference<T>();
            final AtomicReference<Exception> failure = new AtomicReference<Exception>();
            SwingUtilities.invokeAndWait(() -> {
                try {
                    answer.set(question.call());
                } catch (Exception ex) {
                    failure.set(ex);
                }
            });
            if (failure.get() != null) {
                throw failure.get();
            }
            return answer.get();
        } finally {
            if (p != null) {
                p.suspend(false);
            }
        }
    }

    /**
     * Runs r on the event dispatch thread (if the export was started there,
     * else here as before) with this progress as current one (for its
     * steps), and waits for it
     */
    private void deliver(boolean edt, final Result0 r) throws Exception {
        final AtomicReference<Exception> failure = new AtomicReference<Exception>();
        Runnable bound = () -> {
            ExportProgress before = CURRENT.get();
            CURRENT.set(this);
            //the result may ask the user (e.g. where to save)
            suspend(true);
            try {
                r.run();
            } catch (Exception ex) {
                failure.set(ex);
            } finally {
                suspend(false);
                if (before == null) {
                    CURRENT.remove();
                } else {
                    CURRENT.set(before);
                }
            }
        };
        if (!edt || SwingUtilities.isEventDispatchThread()) {
            bound.run();
        } else {
            SwingUtilities.invokeAndWait(bound);
        }
        if (failure.get() != null) {
            Log.Debug(failure.get());
        }
    }

    private interface Result0 {

        void run() throws Exception;
    }

    private synchronized void next(String name) {
        long now = System.nanoTime();
        closeStep(now);
        stepName = name;
        stepStart = now;
        final String fname = name;
        if (ui) {
            onEdt(() -> {
                if (current != null) {
                    current.setText(fname + " ...");
                }
            });
        }
    }

    private void closeStep(long now) {
        if (stepName != null) {
            long ms = (now - stepStart) / 1_000_000L;
            Log.Debug(ExportProgress.class, "[" + title + "] " + stepName + ": " + ms + " ms");
            final String line = "✓ " + stepName + "  (" + ms + " ms)";
            steps.add(line);
            if (ui) {
                onEdt(() -> {
                    if (doneModel != null) {
                        doneModel.addElement(line);
                    }
                });
            }
        }
    }

    /**
     * Ends the progress: logs the last step and the total time, closes the
     * window
     */
    private synchronized void finish() {
        if (finished) {
            return;
        }
        finished = true;
        long now = System.nanoTime();
        closeStep(now);
        stepName = null;
        Log.Debug(ExportProgress.class, "[" + title + "] finished, total: " + (now - start) / 1_000_000L + " ms");
        if (ui) {
            onEdt(() -> {
                if (showTimer != null) {
                    showTimer.stop();
                }
                if (dialog != null) {
                    dialog.dispose();
                    dialog = null;
                }
            });
        }
    }

    /**
     * Hides the window while the user is asked something
     */
    private void suspend(final boolean hide) {
        if (!ui) {
            return;
        }
        onEdt(() -> {
            suspended = hide;
            if (dialog != null) {
                dialog.setVisible(!hide && !finished);
            }
        });
    }

    //EDT
    private void show() {
        if (finished || dialog != null) {
            return;
        }
        Window owner = null;
        try {
            owner = mpv5.YabsViewProxy.instance().getIdentifierFrame();
        } catch (Throwable t) {
            //no main window (e.g. tests)
        }
        dialog = new JDialog(owner, Messages.EXPORT_PROGRESS.toString());
        dialog.setModalityType(java.awt.Dialog.ModalityType.MODELESS);
        dialog.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
        JPanel content = new JPanel(new BorderLayout(0, 8));
        content.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));
        JLabel head = new JLabel(title);
        head.setFont(head.getFont().deriveFont(java.awt.Font.BOLD));
        content.add(head, BorderLayout.NORTH);
        doneModel = new DefaultListModel<String>();
        synchronized (this) {
            for (String s : steps) {
                doneModel.addElement(s);
            }
        }
        JList<String> list = new JList<String>(doneModel);
        list.setFocusable(false);
        list.setVisibleRowCount(6);
        JScrollPane scroll = new JScrollPane(list);
        scroll.setPreferredSize(new Dimension(380, 130));
        content.add(scroll, BorderLayout.CENTER);
        JPanel south = new JPanel(new BorderLayout(0, 4));
        current = new JLabel(stepName != null ? stepName + " ..." : " ");
        south.add(current, BorderLayout.NORTH);
        JProgressBar bar = new JProgressBar();
        bar.setIndeterminate(true);
        south.add(bar, BorderLayout.SOUTH);
        content.add(south, BorderLayout.SOUTH);
        dialog.setContentPane(content);
        dialog.pack();
        dialog.setLocationRelativeTo(owner);
        dialog.setFocusableWindowState(false);
        dialog.setVisible(!suspended);
    }

    private static void onEdt(Runnable r) {
        if (SwingUtilities.isEventDispatchThread()) {
            r.run();
        } else {
            SwingUtilities.invokeLater(r);
        }
    }
}
