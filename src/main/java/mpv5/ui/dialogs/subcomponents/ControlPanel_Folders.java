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
package mpv5.ui.dialogs.subcomponents;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.io.File;
import java.util.Date;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import mpv5.Main;
import mpv5.data.PropertyStore;
import mpv5.db.common.DatabaseBackup;
import mpv5.globals.LocalSettings;
import mpv5.globals.Messages;
import mpv5.handler.ExpenseReceipts;
import mpv5.handler.LocalFiles;
import mpv5.ui.dialogs.ControlApplet;
import mpv5.ui.dialogs.DialogForFile;
import mpv5.ui.dialogs.Popup;
import mpv5.ui.modern.Applets;
import mpv5.ui.modern.Card;
import mpv5.ui.modern.ModernTheme;
import mpv5.ui.modern.PrimaryButton;
import mpv5.utils.files.FileDirectoryHandler;
import mpv5.utils.files.LastDirectory;
import mpv5.utils.images.MPIcon;

/**
 * Central Control Panel applet unifying all path and folder configurations
 * across YaBS:
 * <ul>
 *   <li>Base save directory for exported documents (PDF, ODT, e-invoices)</li>
 *   <li>Expense receipts archive directory (with year structure and SHA256 verification)</li>
 *   <li>Database ZIP backups directory</li>
 *   <li>Temporary files and cache directory</li>
 *   <li>Legacy database backup directory</li>
 * </ul>
 */
public class ControlPanel_Folders extends JPanel implements ControlApplet {

    private static final long serialVersionUID = 1L;
    public static final String UNAME = "folders";

    private final JTextField baseField = new JTextField();
    private final JLabel baseHint = new JLabel();

    private final JTextField receiptsField = new JTextField();
    private final JLabel receiptsHint = new JLabel();

    private final JTextField zipBackupField = new JTextField();
    private final JLabel zipBackupHint = new JLabel();

    private final JTextField cacheField = new JTextField();
    private final JLabel cacheHint = new JLabel();
    private final JButton wipeCacheBtn = Applets.danger(new JButton());

    private final JTextField legacyBackupField = new JTextField();
    private final JLabel legacyBackupHint = new JLabel();

    private final JPanel actions;

    public ControlPanel_Folders() {
        super(new BorderLayout());
        setOpaque(false);

        DocumentListener dl = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                updateHints();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateHints();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updateHints();
            }
        };

        baseField.getDocument().addDocumentListener(dl);
        receiptsField.getDocument().addDocumentListener(dl);
        zipBackupField.getDocument().addDocumentListener(dl);
        cacheField.getDocument().addDocumentListener(dl);
        legacyBackupField.getDocument().addDocumentListener(dl);

        // Card 1: Documents & Exports
        Card baseCard = new Card(Messages.FOLDERS_CARD_BASE.toString())
                .addFull(Applets.hint(Messages.FILENAME_INFO.toString().split("\n\n")[0]))
                .row(Card.field(Messages.FOLDERS_CARD_BASE.toString(),
                        createPathRow(baseField, () -> browseFolder(baseField), () -> openFolder(resolvedBaseDir()))))
                .addFull(baseHint);

        // Card 2: Expense Receipts Archive
        Card receiptsCard = new Card(Messages.FOLDERS_CARD_RECEIPTS.toString())
                .addFull(Applets.hint(Messages.FOLDERS_RECEIPTS_HINT.toString()))
                .row(Card.field(Messages.FOLDERS_CARD_RECEIPTS.toString(),
                        createPathRow(receiptsField, () -> browseFolder(receiptsField), () -> openFolder(resolvedReceiptsDir()))))
                .addFull(receiptsHint);

        // Card 3: Database Backups (ZIP)
        Card zipCard = new Card(Messages.FOLDERS_CARD_ZIPBACKUP.toString())
                .addFull(Applets.hint(Messages.ZIPBACKUP_DESC.toString()))
                .row(Card.field(Messages.FOLDERS_CARD_ZIPBACKUP.toString(),
                        createPathRow(zipBackupField, () -> browseFolder(zipBackupField), () -> openFolder(resolvedZipBackupDir()))))
                .addFull(zipBackupHint);

        // Card 4: Temporary Files & Cache
        wipeCacheBtn.setText(Messages.LOCALFILES_DELETE.toString());
        wipeCacheBtn.addActionListener(e -> wipeCache());
        JPanel cacheBtnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        cacheBtnRow.setOpaque(false);
        cacheBtnRow.add(wipeCacheBtn);

        Card cacheCard = new Card(Messages.FOLDERS_CARD_CACHE.toString())
                .addFull(Applets.hint(Messages.LOCALFILES_HINT.toString()))
                .row(Card.field(Messages.FOLDERS_CARD_CACHE.toString(),
                        createPathRow(cacheField, () -> browseFolder(cacheField), () -> openFolder(resolvedCacheDir()))))
                .addFull(cacheHint)
                .addFull(cacheBtnRow);

        // Card 5: Legacy Database Backups
        Card legacyCard = new Card(Messages.FOLDERS_CARD_LEGACY_BACKUP.toString())
                .addFull(Applets.hint(Messages.FOLDER_BACKUP_SET.toString()))
                .row(Card.field(Messages.FOLDERS_CARD_LEGACY_BACKUP.toString(),
                        createPathRow(legacyBackupField, () -> browseFolder(legacyBackupField), () -> openFolder(resolvedLegacyBackupDir()))))
                .addFull(legacyBackupHint);

        // Card 6: Supported Path Variables
        Card varsCard = new Card(Messages.FOLDERS_VARS_TITLE.toString())
                .addFull(Applets.hint(Messages.FOLDERS_VARS_HINT.toString()));

        add(Applets.page(Messages.FOLDERS_DESC.toString(), 420,
                baseCard, receiptsCard, zipCard, cacheCard, legacyCard, varsCard), BorderLayout.CENTER);

        // Action bar
        JButton appDirBtn = new JButton(Main.MPPATH == null ? ".yabs" : new File(Main.MPPATH).getName());
        appDirBtn.setToolTipText(Main.MPPATH);
        appDirBtn.setIcon(new MPIcon("/images/22/folder_home.png").getIcon(16));
        appDirBtn.addActionListener(e -> openFolder(new File(Main.MPPATH != null ? Main.MPPATH : System.getProperty("user.home"))));

        JButton resetBtn = new JButton(Messages.USERS_RESET.toString());
        resetBtn.addActionListener(e -> reset());

        PrimaryButton saveBtn = new PrimaryButton(Messages.ZIPBACKUP_SAVE.toString(), "/images/16/filesave.png");
        saveBtn.addActionListener(e -> save());

        actions = Applets.actionBar(new javax.swing.JComponent[]{appDirBtn}, new javax.swing.JComponent[]{resetBtn, saveBtn});

        reset();
    }

    private JPanel createPathRow(JTextField field, Runnable onBrowse, Runnable onOpen) {
        JPanel panel = new JPanel(new BorderLayout(ModernTheme.px(6), 0));
        panel.setOpaque(false);
        panel.add(field, BorderLayout.CENTER);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, ModernTheme.px(4), 0));
        btns.setOpaque(false);

        JButton browseBtn = new JButton("...");
        browseBtn.setToolTipText(Messages.FOLDERS_CHOOSE.toString());
        browseBtn.setPreferredSize(new Dimension(ModernTheme.px(32), field.getPreferredSize().height));
        browseBtn.addActionListener(e -> onBrowse.run());
        btns.add(browseBtn);

        JButton openBtn = new JButton(Messages.FOLDERS_OPEN.toString());
        openBtn.setIcon(new MPIcon("/images/22/folder_open.png").getIcon(16));
        openBtn.addActionListener(e -> onOpen.run());
        btns.add(openBtn);

        panel.add(btns, BorderLayout.EAST);
        return panel;
    }

    private void browseFolder(JTextField target) {
        DialogForFile dialog = new DialogForFile(DialogForFile.DIRECTORIES_ONLY);
        dialog.setModalityParent(this);
        if (dialog.chooseFile()) {
            File f = dialog.getFile();
            if (f != null) {
                target.setText(f.getAbsolutePath());
            }
        }
    }

    private static void openFolder(File dir) {
        if (dir == null) {
            return;
        }
        dir.mkdirs();
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(dir);
            } else {
                Popup.notice(dir.getPath());
            }
        } catch (Exception ex) {
            Popup.notice(dir.getPath());
        }
    }

    private File resolvedBaseDir() {
        String base = baseField.getText().trim();
        if (base.isEmpty()) {
            return LastDirectory.configuredBaseDir() != null ? LastDirectory.configuredBaseDir()
                    : new File(System.getProperty("user.home"));
        }
        return new File(base);
    }

    private File resolvedReceiptsDir() {
        String dbname = LocalSettings.getProperty(LocalSettings.DBNAME);
        return ExpenseReceipts.resolve(receiptsField.getText(), dbname, new Date());
    }

    private File resolvedZipBackupDir() {
        String dbname = LocalSettings.getProperty(LocalSettings.DBNAME);
        return DatabaseBackup.resolve(zipBackupField.getText(), dbname, new Date());
    }

    private File resolvedCacheDir() {
        String cache = cacheField.getText().trim();
        if (cache.isEmpty()) {
            return FileDirectoryHandler.getTempDirAsFile();
        }
        return new File(cache);
    }

    private File resolvedLegacyBackupDir() {
        String leg = legacyBackupField.getText().trim();
        if (leg.isEmpty()) {
            return LastDirectory.get();
        }
        return new File(leg);
    }

    private void updateHints() {
        // Base dir hint
        String base = baseField.getText().trim();
        if (base.isEmpty()) {
            baseHint.setText("<html><small>" + String.format(Messages.FOLDER_BASE_EMPTY.toString(),
                    esc(resolvedBaseDir().getPath())) + "</small></html>");
        } else {
            baseHint.setText("<html><small>" + Messages.FOLDER_BASE_SET.toString()
                    + "<br><b>" + esc(resolvedBaseDir().getPath()) + "</b></small></html>");
        }

        // Receipts hint
        File rDir = resolvedReceiptsDir();
        receiptsHint.setText("<html><small>" + String.format(Messages.ZIPBACKUP_FOLDER_HINT.toString(),
                esc(rDir.getPath())) + "</small></html>");

        // Zip Backup hint
        File zDir = resolvedZipBackupDir();
        zipBackupHint.setText("<html><small>" + String.format(Messages.ZIPBACKUP_FOLDER_HINT.toString(),
                esc(zDir.getPath())) + "</small></html>");

        // Cache hint
        File cDir = resolvedCacheDir();
        LocalFiles.Summary s = LocalFiles.summarize(cDir);
        cacheHint.setText("<html><small><b>" + esc(cDir.getPath()) + "</b><br>"
                + String.format(Messages.LOCALFILES_COUNT.toString(), s.files, LocalFiles.size(s.bytes)) + "</small></html>");

        // Legacy backup hint
        String leg = legacyBackupField.getText().trim();
        if (leg.isEmpty()) {
            legacyBackupHint.setText("<html><small>" + String.format(Messages.FOLDER_BACKUP_EMPTY.toString(),
                    esc(resolvedLegacyBackupDir().getPath())) + "</small></html>");
        } else {
            legacyBackupHint.setText("<html><small>" + Messages.FOLDER_BACKUP_SET.toString()
                    + "<br><b>" + esc(resolvedLegacyBackupDir().getPath()) + "</b></small></html>");
        }
    }

    private void wipeCache() {
        File dir = resolvedCacheDir();
        LocalFiles.Summary s = LocalFiles.summarize(dir);
        if (s.files == 0) {
            Popup.notice(Messages.LOCALFILES_EMPTY.toString());
            return;
        }
        StringBuilder kinds = new StringBuilder();
        for (Map.Entry<String, Integer> k : s.kinds.entrySet()) {
            kinds.append("\n   ").append(k.getValue()).append(" \u00d7 ").append(k.getKey());
        }
        String text = String.format(Messages.LOCALFILES_CONFIRM.toString(), dir.getAbsolutePath(),
                s.files, LocalFiles.size(s.bytes)) + kinds + "\n\n" + Messages.LOCALFILES_STAYS;
        if (Popup.Y_N_dialog(text, Messages.LOCALFILES_DELETE.toString())) {
            int left = LocalFiles.wipe(dir);
            mpv5.YabsViewProxy.instance().addMessage(left == 0 ? Messages.LOCALFILES_DONE.toString()
                    : String.format(Messages.LOCALFILES_LEFT.toString(), left));
            updateHints();
        }
    }

    private static String esc(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    public void save() {
        LocalSettings.setProperty(LocalSettings.BASE_DIR, baseField.getText().trim());
        String receipts = receiptsField.getText().trim();
        LocalSettings.setProperty(ExpenseReceipts.KEY_DIR, receipts.isEmpty() ? ExpenseReceipts.DEFAULT_DIR : receipts);
        String zip = zipBackupField.getText().trim();
        LocalSettings.setProperty(DatabaseBackup.KEY_DIR, zip.isEmpty() ? DatabaseBackup.DEFAULT_DIR : zip);
        String cache = cacheField.getText().trim();
        if (!cache.isEmpty()) {
            LocalSettings.setProperty(LocalSettings.CACHE_DIR, cache);
        }
        LocalSettings.setProperty(LocalSettings.BACKUP_DIR, legacyBackupField.getText().trim());
        LocalSettings.apply();
        LocalSettings.save();
        mpv5.YabsViewProxy.instance().addMessage(Messages.FOLDERS_SAVED.toString(), Color.GREEN);
        updateHints();
    }

    @Override
    public final void reset() {
        baseField.setText(LocalSettings.getProperty(LocalSettings.BASE_DIR));
        receiptsField.setText(ExpenseReceipts.folderPattern());
        zipBackupField.setText(DatabaseBackup.folderPattern());
        cacheField.setText(LocalSettings.hasProperty(LocalSettings.CACHE_DIR)
                ? LocalSettings.getProperty(LocalSettings.CACHE_DIR)
                : (Main.MPPATH != null ? Main.MPPATH + File.separator + "Cache" : ""));
        legacyBackupField.setText(LocalSettings.getProperty(LocalSettings.BACKUP_DIR));
        updateHints();
    }

    @Override
    public Component getAndRemoveActionPanel() {
        return actions;
    }

    @Override
    public void setValues(PropertyStore values) {
        reset();
    }

    @Override
    public String getUname() {
        return UNAME;
    }
}
