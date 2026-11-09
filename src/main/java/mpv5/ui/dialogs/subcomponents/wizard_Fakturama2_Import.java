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
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;
import mpv5.globals.Messages;
import mpv5.logging.Log;
import mpv5.ui.dialogs.Popup;
import mpv5.ui.dialogs.WizardMaster;
import mpv5.ui.dialogs.Wizardable;
import mpv5.ui.modern.Applets;
import mpv5.ui.modern.Card;
import mpv5.ui.modern.ModernTheme;
import mpv5.ui.modern.PrimaryButton;
import mpv5.utils.importers.Fakturama2Importer;

/**
 * Import from Fakturama 2: choose the database, what to import, start;
 * progress and result on the same page
 */
public class wizard_Fakturama2_Import extends JPanel implements Wizardable {

    private static final long serialVersionUID = 1L;
    private final WizardMaster master;
    private final JTextField file = new JTextField(30);
    private final JCheckBox chkTax = check(Messages.FKT_OPT_TAX);
    private final JCheckBox chkContacts = check(Messages.FKT_OPT_CONTACTS);
    private final JCheckBox chkProducts = check(Messages.FKT_OPT_PRODUCTS);
    private final JCheckBox chkDocuments = check(Messages.FKT_OPT_DOCUMENTS);
    private final JCheckBox chkVouchers = check(Messages.FKT_OPT_VOUCHERS);
    private final JProgressBar progress = new JProgressBar(0, 100);
    private final JTextArea log = new JTextArea(9, 40);
    private final PrimaryButton start = new PrimaryButton(Messages.FKT_START.toString(), null);
    private boolean running;
    private boolean done;

    public wizard_Fakturama2_Import(WizardMaster w) {
        this.master = w;
        build();
        File guess = guessDatabase();
        if (guess != null) {
            file.setText(guess.getPath());
        }
    }

    private static JCheckBox check(Messages m) {
        JCheckBox c = new JCheckBox(m.toString(), true);
        c.setOpaque(false);
        return c;
    }

    private void build() {
        setLayout(new BorderLayout(0, ModernTheme.px(8)));
        setBorder(BorderFactory.createEmptyBorder(ModernTheme.px(8), ModernTheme.px(8), ModernTheme.px(8), ModernTheme.px(8)));
        setPreferredSize(new Dimension(ModernTheme.px(660), ModernTheme.px(680)));

        JButton choose = new JButton(Messages.FKT_CHOOSE.toString());
        choose.addActionListener(e -> chooseFile());
        JPanel fileRow = new JPanel(new BorderLayout(ModernTheme.px(6), 0));
        fileRow.setOpaque(false);
        fileRow.add(file, BorderLayout.CENTER);
        fileRow.add(choose, BorderLayout.EAST);

        JPanel options = new JPanel(new GridLayout(0, 1, 0, ModernTheme.px(2)));
        options.setOpaque(false);
        options.add(chkTax);
        options.add(chkContacts);
        options.add(chkProducts);
        options.add(chkDocuments);
        options.add(chkVouchers);

        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new javax.swing.BoxLayout(top, javax.swing.BoxLayout.Y_AXIS));
        Card intro = new Card(Messages.FKT_TITLE.toString()).addFull(Applets.hint(Messages.FKT_INTRO.toString()));
        Card where = new Card(Messages.FKT_FILE.toString())
                .addFull(fileRow)
                .addFull(Applets.hint(Messages.FKT_FILE_HINT.toString()));
        Card what = new Card(Messages.FKT_WHAT.toString()).addFull(options);
        for (Card c : new Card[]{intro, where, what}) {
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
        log.setLineWrap(true);
        log.setWrapStyleWord(true);
        ModernTheme.tabLeaves(log);
        JScrollPane logScroll = new JScrollPane(log);
        Card result = new Card(Messages.FKT_RESULT.toString()).addFull(bar).addFull(logScroll, 1);

        add(top, BorderLayout.NORTH);
        add(result, BorderLayout.CENTER);
    }

    /**
     * @return the usual place of the Fakturama 2 database, if there
     */
    private static File guessDatabase() {
        String home = System.getProperty("user.home");
        for (String p : new String[]{"Fakturama2/Database/Database.script", "Fakturama/Database/Database.script",
            ".fakturama2/Database/Database.script", "Documents/Fakturama2/Database/Database.script",
            "Dokumente/Fakturama2/Database/Database.script"}) {
            File f = new File(home, p);
            if (f.isFile()) {
                return f;
            }
        }
        return null;
    }

    private void chooseFile() {
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new FileNameExtensionFilter("Fakturama (Database.script)", "script"));
        File cur = new File(file.getText());
        if (cur.getParentFile() != null && cur.getParentFile().isDirectory()) {
            fc.setCurrentDirectory(cur.getParentFile());
        } else {
            fc.setCurrentDirectory(new File(System.getProperty("user.home")));
        }
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            file.setText(fc.getSelectedFile().getPath());
        }
    }

    private void runImport() {
        if (running) {
            return;
        }
        final File f = new File(file.getText().trim());
        if (file.getText().trim().isEmpty() || !f.exists()) {
            Popup.notice(Messages.FKT_CHOOSE_FILE_FIRST.toString());
            return;
        }
        running = true;
        start.setEnabled(false);
        log.setText("");
        master.setCursor(new Cursor(Cursor.WAIT_CURSOR));
        final Fakturama2Importer importer = new Fakturama2Importer();
        importer.setImportTax(chkTax.isSelected());
        importer.setImportContacts(chkContacts.isSelected());
        importer.setImportProducts(chkProducts.isSelected());
        importer.setImportDocuments(chkDocuments.isSelected());
        importer.setImportVouchers(chkVouchers.isSelected());
        Thread t = new Thread(() -> {
            try {
                Fakturama2Importer.ImportResult r = mpv5.ui.dialogs.Popup.silently(() -> importer.importFrom(f,
                        (msg, pct) -> SwingUtilities.invokeLater(() -> {
                            progress.setValue(pct);
                            progress.setString(msg);
                        })));
                SwingUtilities.invokeLater(() -> finished(r, null));
            } catch (Exception ex) {
                Log.Debug(wizard_Fakturama2_Import.class, ex);
                SwingUtilities.invokeLater(() -> finished(null, ex));
            }
        }, "Fakturama import");
        t.setDaemon(true);
        t.start();
    }

    private void finished(Fakturama2Importer.ImportResult r, Exception ex) {
        running = false;
        start.setEnabled(true);
        master.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        if (ex != null) {
            progress.setValue(0);
            progress.setString("");
            log.setText(ex.getMessage() == null ? ex.toString() : ex.getMessage());
            return;
        }
        done = true;
        progress.setValue(100);
        progress.setString(Messages.FKT_DONE.toString());
        StringBuilder b = new StringBuilder(Messages.FKT_DONE.toString()).append("\n\n").append(r.getSummary());
        if (!r.warnings.isEmpty()) {
            b.append('\n').append(Messages.FKT_WARNINGS).append(":\n");
            for (String w : r.warnings) {
                b.append("- ").append(w).append('\n');
            }
        }
        log.setText(b.toString());
        log.setCaretPosition(0);
        master.isEnd(true);
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
