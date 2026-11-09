package mpv5.ui.dialogs.subcomponents;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import mpv5.globals.Messages;
import mpv5.i18n.LanguageManager;
import mpv5.logging.Log;
import mpv5.ui.beans.LabeledTextChooser;
import mpv5.ui.dialogs.DialogForFile;
import mpv5.ui.dialogs.Popup;
import mpv5.ui.dialogs.WizardMaster;
import mpv5.ui.dialogs.Wizardable;
import mpv5.utils.importers.Fakturama2Importer;

/**
 * OpenYABS Import Wizard for Fakturama 2 (HSQLDB / .script files)
 * Author: OpenYABS Community & Contributor
 */
public class wizard_Fakturama2_Import extends javax.swing.JPanel implements Wizardable {

    private static final long serialVersionUID = 1L;
    private final WizardMaster master;
    private LabeledTextChooser scriptFilePathChooser;
    private JCheckBox chkContacts;
    private JCheckBox chkProducts;
    private JCheckBox chkDocuments;
    private JCheckBox chkVouchers;
    private JCheckBox chkTax;
    private JTextArea txtLog;
    private JProgressBar progressBar;

    public wizard_Fakturama2_Import(WizardMaster w) {
        this.master = w;
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setPreferredSize(new Dimension(580, 420));

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createTitledBorder("Fakturama 2 Migration Wizard"));

        // File Chooser for Fakturama.script
        scriptFilePathChooser = new LabeledTextChooser();
        scriptFilePathChooser.set_Label("Fakturama.script File:");
        scriptFilePathChooser.setFilter(new FileNameExtensionFilter("Fakturama script files (*.script)", "script"));
        scriptFilePathChooser.setModalityParent(this);
        mainPanel.add(scriptFilePathChooser);

        JLabel lblHelp = new JLabel("<html><small>Located in your Fakturama workspace: <i>~/.fakturama2/Database/Database.script</i> or <i>Fakturama.script</i></small></html>");
        lblHelp.setBorder(BorderFactory.createEmptyBorder(0, 5, 10, 5));
        mainPanel.add(lblHelp);

        // Entity Checkboxes
        JPanel entityPanel = new JPanel();
        entityPanel.setLayout(new BoxLayout(entityPanel, BoxLayout.Y_AXIS));
        entityPanel.setBorder(BorderFactory.createTitledBorder("Entities to Import"));

        chkContacts = new JCheckBox("Contacts & Customers (fkt_contact, fkt_address)", true);
        chkProducts = new JCheckBox("Products & Categories (fkt_product, fkt_category)", true);
        chkDocuments = new JCheckBox("Invoices, Offers & Line Items (fkt_document, fkt_documentitem)", true);
        chkVouchers = new JCheckBox("Expenses & Vouchers (fkt_vouchers)", true);
        chkTax = new JCheckBox("Tax Rates & VAT (fkt_vat)", true);

        entityPanel.add(chkContacts);
        entityPanel.add(chkProducts);
        entityPanel.add(chkDocuments);
        entityPanel.add(chkVouchers);
        entityPanel.add(chkTax);

        mainPanel.add(entityPanel);

        // Progress & Log preview
        progressBar = new JProgressBar();
        progressBar.setStringPainted(true);
        progressBar.setVisible(false);
        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(progressBar);

        txtLog = new JTextArea(5, 40);
        txtLog.setEditable(false);
        JScrollPane scrollLog = new JScrollPane(txtLog);
        scrollLog.setBorder(BorderFactory.createTitledBorder("Import Summary"));
        mainPanel.add(scrollLog);

        add(mainPanel, BorderLayout.CENTER);
    }

    @Override
    public boolean next() {
        if (!scriptFilePathChooser.hasText()) {
            Popup.notice("Please select a valid Fakturama 2 Database.script file.");
            return false;
        }

        File file = new File(scriptFilePathChooser.get_Text(false));
        if (!file.exists() || !file.isFile()) {
            Popup.notice("The selected file does not exist.");
            return false;
        }

        master.setCursor(new Cursor(Cursor.WAIT_CURSOR));
        progressBar.setVisible(true);
        progressBar.setIndeterminate(true);

        Thread importThread = new Thread(() -> {
            try {
                Fakturama2Importer importer = new Fakturama2Importer();
                importer.setImportContacts(chkContacts.isSelected());
                importer.setImportProducts(chkProducts.isSelected());
                importer.setImportDocuments(chkDocuments.isSelected());
                importer.setImportVouchers(chkVouchers.isSelected());
                importer.setImportTax(chkTax.isSelected());

                Fakturama2Importer.ImportResult result = importer.importFromScript(file);

                SwingUtilities.invokeLater(() -> {
                    progressBar.setIndeterminate(false);
                    progressBar.setValue(100);
                    txtLog.setText(result.getSummary());
                    master.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                    Popup.notice("Fakturama 2 import completed!\n\n" + result.getSummary());
                });
            } catch (Exception ex) {
                Log.Debug(wizard_Fakturama2_Import.class, ex);
                SwingUtilities.invokeLater(() -> {
                    progressBar.setVisible(false);
                    master.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                    Popup.error(ex);
                });
            }
        });
        importThread.start();
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
