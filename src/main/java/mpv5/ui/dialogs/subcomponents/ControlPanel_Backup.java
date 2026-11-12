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
package mpv5.ui.dialogs.subcomponents;

import java.awt.BorderLayout;
import java.awt.Component;
import java.io.File;
import java.util.Arrays;
import java.util.Date;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingWorker;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import mpv5.data.PropertyStore;
import mpv5.db.common.DatabaseBackup;
import mpv5.globals.LocalSettings;
import mpv5.globals.Messages;
import mpv5.logging.Log;
import mpv5.ui.dialogs.ControlApplet;
import mpv5.ui.dialogs.Popup;
import mpv5.ui.modern.Applets;
import mpv5.ui.modern.Card;
import mpv5.ui.modern.PrimaryButton;

/**
 * Zip backups of the local database: folder (with variables), how many are
 * kept, backup at exit; back up now, restore as a new connection.
 */
public class ControlPanel_Backup extends JPanel implements ControlApplet {

    private static final long serialVersionUID = 1L;
    public final String UNAME = "zipbackup";
    private final JTextField folder = new JTextField(30);
    private final JLabel resolved = new JLabel(" ");
    private final JSpinner keep = new JSpinner(new SpinnerNumberModel(DatabaseBackup.DEFAULT_KEEP, 1, 999, 1));
    private final JCheckBox onExit = new JCheckBox(Messages.ZIPBACKUP_ON_EXIT.toString());
    private final JLabel last = new JLabel(" ");
    private final JPanel actions;
    private final PrimaryButton now;

    public ControlPanel_Backup() {
        super(new BorderLayout());
        setOpaque(false);
        onExit.setOpaque(false);
        folder.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                showFolder();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                showFolder();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                showFolder();
            }
        });
        Card where = new Card(Messages.ZIPBACKUP_CARD_FOLDER.toString())
                .row(Card.field(Messages.ZIPBACKUP_FOLDER.toString(), folder))
                .addFull(resolved)
                .row(Card.field(Messages.ZIPBACKUP_KEEP.toString(), keep))
                .addFull(onExit)
                .addFull(last);
        Card restore = new Card(Messages.ZIPBACKUP_CARD_RESTORE.toString())
                .addFull(Applets.hint(Messages.ZIPBACKUP_RESTORE_HINT.toString()));
        JComponent page = DatabaseBackup.isPossible()
                ? Applets.page(Messages.ZIPBACKUP_INFO.toString(), 420, where, restore)
                : Applets.page(Messages.ZIPBACKUP_INFO.toString() + "\n\n" + Messages.ZIPBACKUP_NOT_LOCAL, 420, where, restore);
        add(page, BorderLayout.CENTER);

        JButton open = new JButton(Messages.ZIPBACKUP_OPEN.toString());
        open.addActionListener(e -> {
            File f = DatabaseBackup.resolve(folder.getText(), LocalSettings.getProperty(LocalSettings.DBNAME), new Date());
            f.mkdirs();
            try {
                java.awt.Desktop.getDesktop().open(f);
            } catch (Exception ex) {
                Popup.notice(f.getPath());
            }
        });
        JButton restoreButton = new JButton(Messages.ZIPBACKUP_RESTORE.toString());
        restoreButton.addActionListener(e -> restore());
        JButton save = new JButton(Messages.ZIPBACKUP_SAVE.toString());
        save.addActionListener(e -> {
            store();
            mpv5.YabsViewProxy.instance().addMessage(Messages.ZIPBACKUP_SAVED.toString());
        });
        now = new PrimaryButton(Messages.ZIPBACKUP_NOW.toString(), "/images/16/filesave.png");
        now.addActionListener(e -> backupNow());
        now.setEnabled(DatabaseBackup.isPossible());
        actions = Applets.actionBar(new JComponent[]{open, restoreButton}, new JComponent[]{save, now});
        reset();
    }

    private void showFolder() {
        File f = DatabaseBackup.resolve(folder.getText(), LocalSettings.getProperty(LocalSettings.DBNAME), new Date());
        resolved.setText("<html><small>" + String.format(Messages.ZIPBACKUP_FOLDER_HINT.toString(), f.getPath()));
    }

    private void store() {
        String f = folder.getText().trim();
        LocalSettings.setProperty(DatabaseBackup.KEY_DIR, f.isEmpty() ? DatabaseBackup.DEFAULT_DIR : f);
        LocalSettings.setProperty(DatabaseBackup.KEY_KEEP, String.valueOf(keep.getValue()));
        LocalSettings.setProperty(DatabaseBackup.KEY_ON_EXIT, String.valueOf(onExit.isSelected()));
        LocalSettings.save();
    }

    private void showLast() {
        File[] zips = DatabaseBackup.folder().listFiles((d, n) -> n.toLowerCase().endsWith(".zip"));
        if (zips == null || zips.length == 0) {
            last.setText(Messages.ZIPBACKUP_NONE.toString());
        } else {
            Arrays.sort(zips, (a, b) -> b.getName().compareTo(a.getName()));
            last.setText(String.format(Messages.ZIPBACKUP_LAST.toString(), zips[0].getName()) + " (" + zips.length + ")");
        }
    }

    private void backupNow() {
        store();
        now.setEnabled(false);
        mpv5.YabsViewProxy.instance().setWaiting(true);
        new SwingWorker<File, Void>() {
            @Override
            protected File doInBackground() throws Exception {
                return DatabaseBackup.backup(true);
            }

            @Override
            protected void done() {
                mpv5.YabsViewProxy.instance().setWaiting(false);
                now.setEnabled(true);
                showLast();
                try {
                    mpv5.YabsViewProxy.instance().addMessage(String.format(Messages.ZIPBACKUP_DONE.toString(), get().getPath()));
                } catch (Exception e) {
                    Log.Debug(this, e);
                    Popup.notice(Messages.ZIPBACKUP_FAILED + "\n" + (e.getCause() != null ? e.getCause().getMessage() : e.getMessage()));
                }
            }
        }.execute();
    }

    private void restore() {
        JFileChooser c = new JFileChooser(DatabaseBackup.folder());
        c.setFileFilter(new FileNameExtensionFilter("Zip", "zip"));
        if (c.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        try {
            String label = Messages.ZIPBACKUP_CONNECTION.toString();
            int id = DatabaseBackup.restore(c.getSelectedFile(), label);
            String name = LocalSettings.getConnectionNames().get(id);
            String folderName = c.getSelectedFile().getName().replaceFirst("(?i)\\.zip$", "");
            if (Popup.Y_N_dialog(String.format(Messages.ZIPBACKUP_RESTORED.toString(),
                    new File(c.getSelectedFile().getParentFile(), folderName).getPath(), name, id), Messages.ZIPBACKUP_TITLE.toString())) {
                mpv5.ui.dialogs.LoginToInstanceScreen.load();
            }
        } catch (Exception e) {
            Log.Debug(this, e);
            Popup.notice(Messages.ZIPBACKUP_FAILED + "\n" + e.getMessage());
        }
    }

    @Override
    public void setValues(PropertyStore values) {
    }

    @Override
    public String getUname() {
        return UNAME;
    }

    @Override
    public final void reset() {
        folder.setText(DatabaseBackup.folderPattern());
        keep.setValue(DatabaseBackup.keep());
        onExit.setSelected(DatabaseBackup.onExit());
        showFolder();
        showLast();
    }

    @Override
    public Component getAndRemoveActionPanel() {
        return actions;
    }
}
