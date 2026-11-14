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
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import mpv5.Main;
import mpv5.data.PropertyStore;
import mpv5.db.common.ConnectionTypeHandler;
import mpv5.db.common.DatabaseConnection;
import mpv5.db.migration.Yabs132Import;
import mpv5.globals.LocalSettings;
import mpv5.globals.Messages;
import mpv5.logging.Log;
import mpv5.ui.dialogs.Popup;
import mpv5.ui.dialogs.WizardMaster;
import mpv5.ui.dialogs.Wizardable;
import mpv5.ui.modern.Applets;
import mpv5.ui.modern.Card;
import mpv5.ui.modern.ModernTheme;
import mpv5.ui.modern.PrimaryButton;

/**
 * Import from YaBS 1.3.2 (Derby) into the open (new) H2 database: choose the
 * folder of the old database, start; progress and result on the same page
 */
public class wizard_Yabs132_Import extends JPanel implements Wizardable {

    private static final long serialVersionUID = 1L;
    private final WizardMaster master;
    private final JComboBox<String> folder = new JComboBox<String>();
    private final JCheckBox backticks = new JCheckBox(Messages.Y132_OPT_BACKTICKS.toString(), true);
    private final JProgressBar progress = new JProgressBar(0, 100);
    private final JTextArea log = new JTextArea(12, 40);
    private final PrimaryButton start = new PrimaryButton(Messages.Y132_START.toString(), null);
    /**
     * database folder - user and password of its connection (old settings)
     */
    private final Map<String, String[]> credentials = new LinkedHashMap<String, String[]>();
    private boolean running;
    private boolean done;

    public wizard_Yabs132_Import(WizardMaster w) {
        this.master = w;
        folder.setEditable(true);
        backticks.setOpaque(false);
        build();
        for (File f : candidates()) {
            folder.addItem(f.getPath());
        }
    }

    private void build() {
        setLayout(new BorderLayout(0, ModernTheme.px(8)));
        setBorder(BorderFactory.createEmptyBorder(ModernTheme.px(8), ModernTheme.px(8), ModernTheme.px(8), ModernTheme.px(8)));
        setPreferredSize(new Dimension(ModernTheme.px(700), ModernTheme.px(720)));

        JButton choose = new JButton(Messages.Y132_CHOOSE.toString());
        choose.addActionListener(e -> chooseFolder());
        JPanel folderRow = new JPanel(new BorderLayout(ModernTheme.px(6), 0));
        folderRow.setOpaque(false);
        folderRow.add(folder, BorderLayout.CENTER);
        folderRow.add(choose, BorderLayout.EAST);

        JPanel options = new JPanel(new GridLayout(0, 1, 0, ModernTheme.px(2)));
        options.setOpaque(false);
        options.add(backticks);

        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new javax.swing.BoxLayout(top, javax.swing.BoxLayout.Y_AXIS));
        Card intro = new Card(Messages.Y132_TITLE.toString()).addFull(Applets.plainHint(Messages.Y132_INTRO.toString()));
        Card where = new Card(Messages.Y132_FOLDER.toString())
                .addFull(folderRow)
                .addFull(Applets.plainHint(Messages.Y132_FOLDER_HINT.toString()));
        Card what = new Card(Messages.Y132_OPTIONS.toString()).addFull(options)
                .addFull(Applets.plainHint(Messages.Y132_OPT_BACKTICKS_HINT.toString()));
        Card into = new Card(Messages.Y132_TARGET.toString()).addFull(new JLabel(targetInfo()));
        for (Card c : new Card[]{intro, where, what, into}) {
            c.setAlignmentX(LEFT_ALIGNMENT);
            top.add(c);
            top.add(javax.swing.Box.createVerticalStrut(ModernTheme.px(6)));
        }

        start.addActionListener(e -> runImport());
        progress.setStringPainted(true);
        progress.setString("");
        JPanel bar = new JPanel(new BorderLayout(ModernTheme.px(8), 0));
        bar.setOpaque(false);
        bar.add(progress, BorderLayout.CENTER);
        bar.add(start, BorderLayout.EAST);

        log.setEditable(false);
        log.setFont(new java.awt.Font(java.awt.Font.MONOSPACED, java.awt.Font.PLAIN, log.getFont().getSize()));
        ModernTheme.tabLeaves(log);
        JScrollPane logScroll = new JScrollPane(log);
        Card result = new Card(Messages.Y132_RESULT.toString()).addFull(bar).addFull(logScroll, 1);

        add(top, BorderLayout.NORTH);
        add(result, BorderLayout.CENTER);
    }

    private static String targetInfo() {
        String type;
        Integer t = ConnectionTypeHandler.getDriverType();
        if (t == null) {
            type = "?";
        } else if (t == ConnectionTypeHandler.H2) {
            type = "H2";
        } else if (t == ConnectionTypeHandler.DERBY) {
            type = "Derby";
        } else if (t == ConnectionTypeHandler.MYSQL) {
            type = "MySQL";
        } else if (t == ConnectionTypeHandler.POSTGRES) {
            type = "PostgreSQL";
        } else {
            type = String.valueOf(t);
        }
        return String.format(Messages.Y132_TARGET_INFO.toString(), ConnectionTypeHandler.getDBNAME(), type);
    }

    /**
     * @return the Derby databases of the connections in the settings (also
     * those of YaBS 1.3.2, same file) and in the YaBS folder, without the
     * open one
     */
    private List<File> candidates() {
        List<File> l = new ArrayList<File>();
        try {
            for (PropertyStore p : LocalSettings.getConnectionStores()) {
                String path = p.getProperty(LocalSettings.DBPATH);
                String name = p.getProperty(LocalSettings.DBNAME);
                if (path == null || name == null) {
                    continue;
                }
                File f = new File(path, name);
                if (Yabs132Import.isDerbyDatabase(f) && !l.contains(f)) {
                    l.add(f);
                    credentials.put(f.getPath(), new String[]{p.getProperty(LocalSettings.DBUSER), p.getProperty(LocalSettings.DBPASSWORD)});
                }
            }
        } catch (Exception e) {
            Log.Debug(wizard_Yabs132_Import.class, e);
        }
        //the YaBS folder (also ~/.yabs if another folder is used now)
        for (File dir : new File[]{Main.MPPATH == null ? null : new File(Main.MPPATH), new File(System.getProperty("user.home"), ".yabs")}) {
            for (File f : Yabs132Import.findDatabases(dir)) {
                if (!l.contains(f)) {
                    l.add(f);
                }
            }
        }
        //not the database open now
        try {
            if (ConnectionTypeHandler.getDriverType() != null && ConnectionTypeHandler.getDriverType() == ConnectionTypeHandler.DERBY) {
                l.remove(new File(DatabaseConnection.instanceOf().getCtype().getURL(), ConnectionTypeHandler.getDBNAME()));
            }
        } catch (Exception e) {
            Log.Debug(wizard_Yabs132_Import.class, e);
        }
        return l;
    }

    private String selected() {
        Object o = folder.isEditable() ? folder.getEditor().getItem() : folder.getSelectedItem();
        return o == null ? "" : o.toString().trim();
    }

    private void chooseFolder() {
        JFileChooser fc = new JFileChooser();
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        File cur = new File(selected());
        if (cur.isDirectory()) {
            fc.setCurrentDirectory(cur.getParentFile() != null ? cur.getParentFile() : cur);
        } else if (Main.MPPATH != null && new File(Main.MPPATH).isDirectory()) {
            fc.setCurrentDirectory(new File(Main.MPPATH));
        } else {
            fc.setCurrentDirectory(new File(System.getProperty("user.home")));
        }
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            setFolder(fc.getSelectedFile());
        }
    }

    /**
     * @param f a Derby database or a folder with some (the first is chosen,
     * all are in the list)
     */
    void setFolder(File f) {
        List<File> found = Yabs132Import.findDatabases(f);
        if (found.isEmpty()) {
            folder.getEditor().setItem(f.getPath());
            folder.setSelectedItem(f.getPath());
            return;
        }
        for (File x : found) {
            boolean have = false;
            for (int i = 0; i < folder.getItemCount(); i++) {
                have |= folder.getItemAt(i).equals(x.getPath());
            }
            if (!have) {
                folder.addItem(x.getPath());
            }
        }
        folder.setSelectedItem(found.get(0).getPath());
    }

    private void runImport() {
        if (running) {
            return;
        }
        if (selected().isEmpty()) {
            Popup.notice(Messages.Y132_CHOOSE_FIRST.toString());
            return;
        }
        List<File> found = Yabs132Import.findDatabases(new File(selected()));
        if (found.size() == 1 && !found.get(0).getPath().equals(selected())) {
            setFolder(found.get(0));
        }
        final File f = new File(selected());
        if (!Yabs132Import.isDerbyDatabase(f)) {
            log.setText(String.format(Messages.Y132_NOT_A_DATABASE.toString(), f));
            return;
        }
        if (!Popup.Y_N_dialog(String.format(Messages.Y132_CONFIRM.toString(), ConnectionTypeHandler.getDBNAME()), Messages.Y132_TITLE.toString())) {
            return;
        }
        running = true;
        start.setEnabled(false);
        log.setText("");
        master.setCursor(new Cursor(Cursor.WAIT_CURSOR));
        final Yabs132Import importer = new Yabs132Import();
        importer.setConvertBackticks(backticks.isSelected());
        String[] cred = credentials.get(f.getPath());
        if (cred != null) {
            importer.setCredentials(cred[0], cred[1]);
        }
        importer.setProgress((msg, pct) -> SwingUtilities.invokeLater(() -> {
            progress.setValue(pct);
            progress.setString(msg);
        }));
        Thread t = new Thread(() -> {
            try (Connection target = targetConnection()) {
                Yabs132Import.Result r = importer.importInto(f, target);
                SwingUtilities.invokeLater(() -> finished(r, null));
            } catch (Exception ex) {
                Log.Debug(wizard_Yabs132_Import.class, ex);
                SwingUtilities.invokeLater(() -> finished(null, ex));
            }
        }, "YaBS 1.3.2 import");
        t.setDaemon(true);
        t.start();
    }

    /**
     * @return a connection of its own to the open database (the import is one
     * transaction)
     */
    private static Connection targetConnection() throws Exception {
        String url = DatabaseConnection.instanceOf().getCtype().getConnectionString(false);
        String user = DatabaseConnection.getUser();
        return user == null ? DriverManager.getConnection(url) : DriverManager.getConnection(url, user, DatabaseConnection.getPassword());
    }

    private void finished(Yabs132Import.Result r, Exception ex) {
        running = false;
        start.setEnabled(true);
        master.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        if (ex != null) {
            progress.setValue(0);
            progress.setString("");
            log.setText(ex.getMessage() == null ? ex.toString() : ex.getMessage());
            log.setCaretPosition(0);
            return;
        }
        done = true;
        start.setEnabled(false);
        clearCaches();
        mpv5.db.objects.User.cacheUser();
        progress.setValue(100);
        progress.setString(Messages.Y132_DONE.toString());
        StringBuilder b = new StringBuilder(Messages.Y132_DONE.toString()).append("\n\n").append(r.getSummary());
        File report = saveReport(r);
        if (report != null) {
            b.append("\n\n").append(String.format(Messages.Y132_REPORT_SAVED.toString(), report));
        }
        log.setText(b.toString());
        log.setCaretPosition(0);
        master.isEnd(true);
        if (Popup.Y_N_dialog(Messages.Y132_RESTART.toString(), Messages.Y132_TITLE.toString())) {
            Main.restart();
        }
    }

    private static File saveReport(Yabs132Import.Result r) {
        return Yabs132Import.saveReport(r, new File(Main.MPPATH != null ? Main.MPPATH : System.getProperty("user.home")));
    }

    /**
     * The data was replaced by an import: nothing cached of the data before
     */
    public static void clearCaches() {
        mpv5.db.common.DatabaseObject.clearCache();
        mpv5.handler.TemplateHandler.clearCache();
        mpv5.handler.FormatHandler.clearCache();
    }

    /**
     * Let the user choose the folder of an old YaBS 1.3.2 database
     *
     * @param parent
     * @return the database folder, null if none was chosen
     */
    public static File chooseDatabase(java.awt.Component parent) {
        JFileChooser fc = new JFileChooser();
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fc.setDialogTitle(Messages.Y132_FOLDER.toString());
        List<File> found = new ArrayList<File>();
        for (File dir : new File[]{Main.MPPATH == null ? null : new File(Main.MPPATH), new File(System.getProperty("user.home"), ".yabs")}) {
            found.addAll(Yabs132Import.findDatabases(dir));
        }
        if (!found.isEmpty()) {
            fc.setCurrentDirectory(found.get(0).getParentFile());
            fc.setSelectedFile(found.get(0));
        } else {
            fc.setCurrentDirectory(new File(System.getProperty("user.home")));
        }
        if (fc.showOpenDialog(parent) != JFileChooser.APPROVE_OPTION) {
            return null;
        }
        List<File> l = Yabs132Import.findDatabases(fc.getSelectedFile());
        if (l.isEmpty()) {
            Popup.notice(String.format(Messages.Y132_NOT_A_DATABASE.toString(), fc.getSelectedFile()));
            return null;
        }
        return l.get(0);
    }

    @Override
    public boolean next() {
        //"Next" starts the import as well; after it the wizard can be closed
        if (done) {
            return true;
        }
        runImport();
        return false;
    }

    @Override
    public boolean back() {
        return false;
    }

    @Override
    public void load() {
    }
}
