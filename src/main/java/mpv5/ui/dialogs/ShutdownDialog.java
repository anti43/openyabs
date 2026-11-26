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

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.Window;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import mpv5.globals.Messages;
import mpv5.logging.Log;
import mpv5.ui.modern.ModernTheme;

/**
 * Modal progress popup shown while YaBS is shutting down (saving settings,
 * closing the database, writing the exit backup, stopping background processes,
 * and cleaning up temporary files).
 * <p>
 * Running the actual shutdown work on a background thread while this modal
 * dialog pumps the Swing EDT keeps the UI responsive and shows the user why
 * closing may take a few seconds, while still blocking
 * {@code Application.shutdown()} until all shutdown steps have finished.
 */
public final class ShutdownDialog extends JDialog {

    private static final long serialVersionUID = 1L;

    /**
     * Progress callback passed to the shutdown task. Safe to invoke from any thread.
     */
    public interface Progress {

        /**
         * Updates the progress bar to a determinate percentage (0..100) with status and optional detail text.
         */
        void step(int percent, String status, String detail);

        default void step(int percent, String status) {
            step(percent, status, null);
        }

        /**
         * Switches the progress bar to indeterminate mode while displaying status and optional detail text.
         */
        void indeterminate(String status, String detail);

        Progress NOOP = new Progress() {
            @Override
            public void step(int percent, String status, String detail) {
            }

            @Override
            public void indeterminate(String status, String detail) {
            }
        };
    }

    private final JLabel statusLabel;
    private final JLabel detailLabel;
    private final JProgressBar progressBar;
    private boolean closed;

    ShutdownDialog(Window owner) {
        super(owner, Messages.SHUTDOWN_TITLE.toString(), ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        setResizable(false);

        JPanel root = new JPanel(new BorderLayout());
        root.setBorder(ModernTheme.padding(18, 22));
        root.setBackground(ModernTheme.background());

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        JLabel heading = new JLabel(Messages.SHUTDOWN_HEADING.toString());
        heading.setFont(ModernTheme.font(14f, Font.BOLD));
        heading.setForeground(ModernTheme.foreground());
        heading.setAlignmentX(LEFT_ALIGNMENT);

        statusLabel = new JLabel(Messages.SHUTDOWN_STEP_SETTINGS.toString());
        statusLabel.setFont(ModernTheme.baseFont());
        statusLabel.setForeground(ModernTheme.foreground());
        statusLabel.setAlignmentX(LEFT_ALIGNMENT);

        progressBar = new JProgressBar(0, 100);
        progressBar.setValue(0);
        progressBar.setStringPainted(true);
        progressBar.setAlignmentX(LEFT_ALIGNMENT);
        int barW = ModernTheme.px(400);
        int barH = ModernTheme.px(20);
        progressBar.setPreferredSize(new Dimension(barW, barH));
        progressBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, barH));

        detailLabel = new JLabel(" ");
        detailLabel.setFont(ModernTheme.font(11f, Font.PLAIN));
        detailLabel.setForeground(ModernTheme.muted());
        detailLabel.setAlignmentX(LEFT_ALIGNMENT);

        content.add(heading);
        content.add(Box.createVerticalStrut(ModernTheme.px(8)));
        content.add(statusLabel);
        content.add(Box.createVerticalStrut(ModernTheme.px(10)));
        content.add(progressBar);
        content.add(Box.createVerticalStrut(ModernTheme.px(8)));
        content.add(detailLabel);

        root.add(content, BorderLayout.CENTER);
        setContentPane(root);
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        pack();
        setMinimumSize(new Dimension(ModernTheme.px(444), getHeight()));
        setLocationRelativeTo(owner);
    }

    Progress progress() {
        return new Progress() {
            @Override
            public void step(final int percent, final String status, final String detail) {
                final int clamped = Math.max(0, Math.min(100, percent));
                SwingUtilities.invokeLater(() -> {
                    if (closed) {
                        return;
                    }
                    progressBar.setIndeterminate(false);
                    progressBar.setValue(clamped);
                    progressBar.setString(clamped + " %");
                    if (status != null) {
                        statusLabel.setText(status);
                    }
                    detailLabel.setText(detail != null && !detail.isEmpty() ? detail : " ");
                });
            }

            @Override
            public void indeterminate(final String status, final String detail) {
                SwingUtilities.invokeLater(() -> {
                    if (closed) {
                        return;
                    }
                    progressBar.setIndeterminate(true);
                    progressBar.setString("");
                    if (status != null) {
                        statusLabel.setText(status);
                    }
                    detailLabel.setText(detail != null && !detail.isEmpty() ? detail : " ");
                });
            }
        };
    }

    void closeDialog() {
        closed = true;
        setVisible(false);
        dispose();
    }

    /**
     * Runs {@code task} on a background thread while showing a modal closing
     * dialog with live progress on the EDT. In headless mode or when not called
     * on the EDT, executes {@code task} synchronously with {@link Progress#NOOP}.
     *
     * @param owner parent window (may be null)
     * @param task  shutdown routine receiving a thread-safe {@link Progress}
     */
    public static void runWithProgress(Window owner, Consumer<Progress> task) {
        if (task == null) {
            return;
        }
        if (GraphicsEnvironment.isHeadless() || !SwingUtilities.isEventDispatchThread()) {
            task.accept(Progress.NOOP);
            return;
        }

        final ShutdownDialog dialog = new ShutdownDialog(owner);
        final AtomicBoolean finished = new AtomicBoolean(false);
        Thread worker = new Thread(() -> {
            try {
                task.accept(dialog.progress());
            } catch (Throwable t) {
                Log.Debug(ShutdownDialog.class, t);
            } finally {
                finished.set(true);
                SwingUtilities.invokeLater(dialog::closeDialog);
            }
        }, "yabs-shutdown");
        worker.setDaemon(true);
        worker.start();

        if (!finished.get()) {
            dialog.setVisible(true);
        } else {
            dialog.closeDialog();
        }
    }
}
