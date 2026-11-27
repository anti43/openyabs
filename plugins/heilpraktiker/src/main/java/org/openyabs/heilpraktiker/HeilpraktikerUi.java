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
package org.openyabs.heilpraktiker;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.sql.Connection;
import java.time.LocalDate;
import java.util.List;
import mpv5.ui.modern.Card;
import mpv5.ui.modern.ModernTheme;
import mpv5.ui.modern.PrimaryButton;
import mpv5.ui.modern.SetupPage;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import mpv5.db.common.DatabaseObject;
import mpv5.db.objects.Contact;
import mpv5.db.objects.Item;
import mpv5.globals.Constants;
import mpv5.logging.Log;
import mpv5.plugin.api.ExtensionContext;
import mpv5.ui.panels.DataPanel;
import org.openyabs.heilpraktiker.GebuehCatalog.PriceMode;
import org.openyabs.heilpraktiker.HeilpraktikerStore.InvoiceData;
import org.openyabs.heilpraktiker.HeilpraktikerStore.PatientData;
import org.openyabs.heilpraktiker.HeilpraktikerStore.PurgeResult;
import org.openyabs.heilpraktiker.HeilpraktikerStore.Settings;

/**
 * Settings page, Contact/Invoice view banners, and edit dialogs for the
 * Heilpraktiker & GebüH plugin.
 */
final class HeilpraktikerUi {

    private static final Color BANNER_BG = new Color(239, 246, 255);
    private static final Color BANNER_BORDER = new Color(147, 197, 253);
    private static final Color BANNER_FG = new Color(30, 64, 175);

    private HeilpraktikerUi() {
    }

    static JComponent settingsPage(final ExtensionContext yabs, final HeilpraktikerPlugin plugin) {
        final Settings s = Settings.load();
        final boolean isAdmin = HeilpraktikerSecurity.isCurrentUserAdmin();

        final JTextField titleField = new JTextField(s.practitionerTitle, 28);
        final JTextField licenseField = new JTextField(s.licenseAuthority, 40);
        final JTextArea ustgArea = new JTextArea(s.ustgNotice, 3, 40);
        ustgArea.setLineWrap(true);
        ustgArea.setWrapStyleWord(true);
        final JComboBox<String> priceMode = new JComboBox<String>(new String[]{
            "H\u00f6chstsatz (maxFee)",
            "Mittelwert (min/max)",
            "Mindestsatz (minFee)"
        });
        if (s.defaultPriceMode == PriceMode.MID) {
            priceMode.setSelectedIndex(1);
        } else if (s.defaultPriceMode == PriceMode.MIN) {
            priceMode.setSelectedIndex(2);
        } else {
            priceMode.setSelectedIndex(0);
        }
        final JCheckBox warnOnSave = new JCheckBox(
                "Heilpraktiker-Rechnungen beim Speichern auf Geb\u00fcH- / PKV-Plausibilit\u00e4t pr\u00fcfen",
                s.warnOnSave);
        final JCheckBox skipEInvoice = new JCheckBox(
                "E-Rechnung (ZUGFeRD/XRechnung) bei steuerfreien Heilbehandlungen (\u00a7 4 Nr. 14 / \u00a7 14 Abs. 2 UStG) \u00fcberspringen",
                s.skipEInvoiceForTaxExempt);
        final JCheckBox restrictSoap = new JCheckBox(
                "Zugriff auf SOAP-Notizen einschr\u00e4nken (\u00a7 203 StGB / Art. 9 DSGVO \u2013 nur Admins & freigegebene Benutzer)",
                s.restrictSoapAccess);
        final JTextField soapUsers = new JTextField(s.soapAllowedUsers, 32);
        final JCheckBox encryptSoap = new JCheckBox(
                "SOAP-Notizen verschl\u00fcsselt in der Datenbank speichern (AES-256-GCM, Schl\u00fcssel lokal im Plugin-Ordner)",
                s.encryptSoapNotes);

        restrictSoap.setEnabled(isAdmin);
        soapUsers.setEnabled(isAdmin);
        encryptSoap.setEnabled(isAdmin);

        Runnable applySettings = () -> {
            s.practitionerTitle = titleField.getText().trim();
            s.licenseAuthority = licenseField.getText().trim();
            s.ustgNotice = ustgArea.getText().trim();
            int idx = priceMode.getSelectedIndex();
            s.defaultPriceMode = idx == 1 ? PriceMode.MID : idx == 2 ? PriceMode.MIN : PriceMode.MAX;
            s.warnOnSave = warnOnSave.isSelected();
            s.skipEInvoiceForTaxExempt = skipEInvoice.isSelected();
            if (isAdmin) {
                s.restrictSoapAccess = restrictSoap.isSelected();
                s.soapAllowedUsers = soapUsers.getText().trim();
                s.encryptSoapNotes = encryptSoap.isSelected();
            }
            s.save();
        };

        SetupPage page = new SetupPage("Heilpraktiker-Setup",
                "In wenigen Klicks startklar: Geb\u00fcH-Katalog und Rechnungsvorlage importieren, dann Praxisangaben und Datenschutz festlegen.");
        if (plugin != null && yabs != null) {
            String cardTitle = "Schnellstart: Geb\u00fcH-Katalog & Rechnungsvorlage";
            JButton importCatalogBtn = new PrimaryButton("Katalog importieren", null);
            importCatalogBtn.addActionListener(e -> {
                applySettings.run();
                yabs.background(plugin::importGebuehCatalog, msg -> yabs.popup(msg));
            });
            page.action(cardTitle, importCatalogBtn, "Geb\u00fcH-Katalog (144 Ziffern)",
                    "Legt alle 144 offiziellen Geb\u00fcH-85/2002-Leistungen (Ziffern 1\u201339.13) als steuerfreie Leistungen (0 % USt) an.");

            JButton importTemplateBtn = new JButton("Vorlage importieren");
            importTemplateBtn.addActionListener(e -> yabs.background(plugin::importTemplate, msg -> yabs.popup(msg)));
            page.action(cardTitle, importTemplateBtn, "Rechnungsvorlage",
                    "Importiert die Heilpraktiker-Rechnungsvorlage (.odt) in OpenYaBS und weist sie Ihrem Benutzer zu.");
        }
        JScrollPane ustgScroll = new JScrollPane(ustgArea);
        page.card(new Card("Praxis & Abrechnung")
                .row(Card.field("Berufsbezeichnung ([hp.practitionerTitle])", ModernTheme.roomy(titleField)),
                        Card.field("Erlaubnis / Gesundheitsamt ([hp.licenseAuthority])", ModernTheme.roomy(licenseField)))
                .row(Card.field("USt-Befreiungshinweis ([hp.ustgNotice])", ustgScroll))
                .row(Card.field("Standard-Honorar bei Geb\u00fcH-Import", ModernTheme.roomyCombo(priceMode), 1), Card.field(" ", new JPanel() {{ setOpaque(false); }}, 1))
                .addFull(SetupPage.plain(warnOnSave))
                .addFull(SetupPage.plain(skipEInvoice)));
        page.card(new Card("Datenschutz & SOAP-Notizen")
                .addFull(SetupPage.plain(restrictSoap))
                .row(Card.field("Berechtigte Benutzer f\u00fcr SOAP-Notizen (kommagetrennt)", ModernTheme.roomy(soapUsers)))
                .addFull(SetupPage.plain(encryptSoap)));
        JLabel info = new JLabel("<html><div style='width:560px'>"
                + "<b>Hinweise (Datenschutz &amp; Abrechnung, Stand 2026):</b><br>"
                + "&bull; <b>Art. 9 DSGVO / &sect; 203 StGB:</b> Diagnosen und SOAP-Notizen sind besondere Kategorien "
                + "personenbezogener Daten. F\u00fcr Selbstzahler wird die Diagnose standardm\u00e4\u00dfig nicht auf der Rechnung gedruckt; "
                + "vor dem unverschl\u00fcsselten E-Mail-Versand einer Rechnung mit Diagnose erfolgt eine Warnung.<br>"
                + "&bull; <b>Verschl\u00fcsselung:</b> Der AES-256-Schl\u00fcssel (<code>soap.key</code>) liegt au\u00dferhalb der Datenbank "
                + "im lokalen Plugin-Datenordner. Sichern Sie diese Datei getrennt vom Datenbank-Backup!<br>"
                + "&bull; <b>Aufbewahrungsfristen:</b> Patientenakten / Behandlungsdokumentation 10 Jahre nach Abschluss der "
                + "Behandlung (&sect; 630f Abs. 3 BGB), Rechnungen 8 Jahre (&sect; 14b UStG / &sect; 147 AO).<br>"
                + "&bull; <b>Vorlagen-Platzhalter:</b> <code>[hp.patientDob]</code>, <code>[hp.insuranceType]</code>, "
                + "<code>[hp.printDiagnosis]</code>, <code>[hp.icd10]</code>, <code>[hp.diagnosis]</code>, "
                + "<code>[hp.treatmentDates]</code>, <code>[hp.ustgNotice]</code>, <code>[hp.practitionerTitle]</code>, "
                + "<code>[hp.licenseAuthority]</code>."
                + "</div></html>");
        page.card(new Card("Hinweise").addFull(info));

        PrimaryButton saveBtn = SetupPage.saveButton("Speichern");
        saveBtn.addActionListener(e -> {
            applySettings.run();
            if (yabs != null) {
                yabs.notify("Heilpraktiker-Einstellungen gespeichert.");
            }
        });
        return page.build(saveBtn);
    }

    static void decorate(final DataPanel panel, final ExtensionContext yabs, final GebuehCatalog catalog) {
        if (panel == null) {
            return;
        }
        DatabaseObject owner = panel.getDataOwner();
        if (owner instanceof Contact && owner.__getIDS() > 0) {
            decorateContact(panel, (Contact) owner, yabs);
        } else if (owner instanceof Item && owner.__getIDS() > 0 && isInvoiceType(((Item) owner).__getInttype())) {
            decorateInvoice(panel, (Item) owner, yabs, catalog);
        }
    }

    private static boolean isInvoiceType(int type) {
        return type == Constants.TYPE_INVOICE || type == Constants.TYPE_CREDIT
                || type == Constants.TYPE_DEPOSIT || type == Constants.TYPE_PART_PAYMENT;
    }

    private static void decorateContact(final DataPanel panel, final Contact contact, final ExtensionContext yabs) {
        PatientData p = HeilpraktikerStore.loadPatientSafe(contact.__getIDS());
        StringBuilder sb = new StringBuilder("Heilpraktiker-Patientendaten: Geburtsdatum ");
        sb.append(p.birthDate.isEmpty() ? "\u2013" : p.birthDate);
        sb.append(" \u00b7 ").append(p.insuranceType);
        if (p.honorarvereinbarung) {
            sb.append(" \u00b7 Honorarvereinbarung (\u00a7 630a BGB)");
        }
        if (!p.retentionUntil.isEmpty()) {
            sb.append(" \u00b7 Aufbewahren bis ").append(p.retentionUntil);
            if (HeilpraktikerValidator.isRetentionExpired(p.retentionUntil, LocalDate.now())) {
                sb.append(" (Frist abgelaufen)");
            }
        }
        JButton edit = new JButton("Patientendaten / SOAP");
        edit.addActionListener(e -> {
            if (editPatientDialog(panel instanceof Component ? (Component) panel : null, contact)) {
                decorateContact(panel, contact, yabs);
            }
        });
        panel.setPluginBanner(banner(sb.toString(), edit));
    }

    private static void decorateInvoice(final DataPanel panel, final Item item, final ExtensionContext yabs, final GebuehCatalog catalog) {
        PatientData p = HeilpraktikerStore.loadPatientSafe(item.__getContactsids());
        InvoiceData d = HeilpraktikerStore.loadInvoiceSafe(item.__getIDS());
        boolean printDiag = HeilpraktikerValidator.shouldPrintDiagnosis(p, d);
        StringBuilder sb = new StringBuilder("Heilpraktiker-Rechnung: ");
        sb.append(d.heilbehandlung ? "\u00a7 4 Nr. 14 UStG (0 %)" : "Pr\u00e4vention/Wellness");
        sb.append(" \u00b7 Diagnose: ").append(d.diagnosisText.isEmpty() ? "\u2013" : d.diagnosisText);
        if (!d.icd10Code.isEmpty()) {
            sb.append(" [").append(d.icd10Code).append("]");
        }
        sb.append(printDiag ? " (auf Rechnung)" : " (nicht auf Rechnung)");
        if (!d.retentionUntil.isEmpty() && HeilpraktikerValidator.isRetentionExpired(d.retentionUntil, LocalDate.now())) {
            sb.append(" \u00b7 Aufbewahrungsfrist abgelaufen");
        }
        JButton edit = new JButton("Diagnose & Behandlung");
        edit.addActionListener(e -> {
            if (editInvoiceDialog(panel instanceof Component ? (Component) panel : null, item)) {
                decorateInvoice(panel, item, yabs, catalog);
            }
        });
        JButton check = new JButton("Pr\u00fcfen");
        check.addActionListener(e -> {
            List<String> warnings = HeilpraktikerPlugin.validateItem(item, catalog);
            showValidationResult(panel instanceof Component ? (Component) panel : null, warnings);
        });
        panel.setPluginBanner(banner(sb.toString(), edit, check));
    }

    private static JPanel banner(String text, JButton... buttons) {
        JPanel p = new JPanel(new BorderLayout(8, 0));
        p.setBackground(BANNER_BG);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BANNER_BORDER),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)));
        JLabel label = new JLabel(text);
        label.setForeground(BANNER_FG);
        p.add(label, BorderLayout.CENTER);
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        right.setOpaque(false);
        for (JButton b : buttons) {
            right.add(b);
        }
        p.add(right, BorderLayout.EAST);
        return p;
    }

    static boolean editPatientDialog(Component parent, Contact contact) {
        PatientData p = HeilpraktikerStore.loadPatientSafe(contact.__getIDS());
        Settings settings = Settings.load();
        final boolean canSoap = HeilpraktikerSecurity.canAccessSoapNotes(settings);
        Window owner = parent == null ? null : SwingUtilities.getWindowAncestor(parent);
        final JDialog dialog = new JDialog(owner, "Patientendaten \u2013 " + contact.__getCname(), JDialog.ModalityType.APPLICATION_MODAL);
        final boolean[] saved = {false};

        JTextField dob = new JTextField(p.birthDate, 14);
        JComboBox<String> ins = new JComboBox<String>(new String[]{"Selbstzahler", "PKV", "Beihilfe", "PKV + Beihilfe", "Zusatzversicherung"});
        ins.setEditable(true);
        ins.setSelectedItem(p.insuranceType);
        boolean defaultPrint = p.printDiagnosis != null
                ? p.printDiagnosis.booleanValue()
                : HeilpraktikerValidator.isReimbursementInsurance(p.insuranceType);
        JCheckBox printDiag = new JCheckBox("Standard: Diagnose auf Rechnung drucken (PKV/Beihilfe)", defaultPrint);
        ins.addActionListener(e -> {
            Object sel = ins.getSelectedItem();
            if (sel != null) {
                printDiag.setSelected(HeilpraktikerValidator.isReimbursementInsurance(String.valueOf(sel)));
            }
        });
        JCheckBox honorar = new JCheckBox("Schriftliche Honorarvereinbarung (\u00a7 630a BGB) liegt vor", p.honorarvereinbarung);
        JTextField lastTreatment = new JTextField(p.lastTreatmentDate, 14);
        JTextField retentionUntil = new JTextField(p.retentionUntil, 14);
        retentionUntil.setToolTipText("Wird automatisch auf 10 Jahre nach letzter Behandlung (\u00a7 630f Abs. 3 BGB) berechnet, falls leer.");

        JTextArea soap = new JTextArea(
                canSoap ? p.soapNotes
                        : "Zugriff auf Behandlungsdokumentation (\u00a7 630f BGB / \u00a7 203 StGB) eingeschr\u00e4nkt.\n"
                        + "Nur f\u00fcr Administratoren und freigegebene Behandler sichtbar.",
                8, 42);
        soap.setLineWrap(true);
        soap.setWrapStyleWord(true);
        soap.setEditable(canSoap);
        soap.setEnabled(canSoap);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(4, 6, 4, 6);
        g.anchor = GridBagConstraints.WEST;
        g.fill = GridBagConstraints.HORIZONTAL;
        int row = 0;
        addRow(form, g, row++, "Geburtsdatum (z. B. 14.05.1980):", dob);
        addRow(form, g, row++, "Versicherungsart:", ins);
        g.gridx = 0;
        g.gridy = row++;
        g.gridwidth = 2;
        form.add(printDiag, g);
        g.gridy = row++;
        form.add(honorar, g);
        addRow(form, g, row++, "Letztes Behandlungsdatum (JJJJ-MM-TT):", lastTreatment);
        addRow(form, g, row++, "Aufbewahren bis (\u00a7 630f BGB, 10 J.):", retentionUntil);
        g.gridx = 0;
        g.gridy = row++;
        g.gridwidth = 2;
        form.add(new JLabel("Behandlungsdokumentation / SOAP-Notizen (\u00a7 630f BGB, Art. 9 DSGVO, \u00a7 203 StGB):"), g);
        g.gridy = row++;
        g.weightx = 1.0;
        g.weighty = 1.0;
        g.fill = GridBagConstraints.BOTH;
        JScrollPane sp = new JScrollPane(soap);
        sp.setPreferredSize(new Dimension(480, 160));
        form.add(sp, g);

        JButton cancel = new JButton("Abbrechen");
        cancel.addActionListener(e -> dialog.dispose());
        JButton ok = new JButton("Speichern");
        ok.addActionListener(e -> {
            p.birthDate = dob.getText().trim();
            p.insuranceType = ins.getSelectedItem() == null ? "Selbstzahler" : String.valueOf(ins.getSelectedItem()).trim();
            p.printDiagnosis = printDiag.isSelected();
            p.honorarvereinbarung = honorar.isSelected();
            p.lastTreatmentDate = lastTreatment.getText().trim();
            p.retentionUntil = retentionUntil.getText().trim();
            try {
                if (canSoap) {
                    p.soapNotes = soap.getText().trim();
                    HeilpraktikerStore.savePatient(HeilpraktikerStore.connection(), p);
                } else {
                    HeilpraktikerStore.savePatientWithoutSoap(HeilpraktikerStore.connection(), p);
                }
                saved[0] = true;
                dialog.dispose();
            } catch (Exception ex) {
                Log.Debug(HeilpraktikerUi.class, ex);
                JOptionPane.showMessageDialog(dialog, "Speichern fehlgeschlagen: " + ex.getMessage(), "Fehler", JOptionPane.ERROR_MESSAGE);
            }
        });

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        btns.add(cancel);
        btns.add(ok);

        JPanel root = new JPanel(new BorderLayout());
        root.add(form, BorderLayout.CENTER);
        root.add(btns, BorderLayout.SOUTH);
        dialog.setContentPane(root);
        dialog.pack();
        dialog.setLocationRelativeTo(owner);
        dialog.setVisible(true);
        return saved[0];
    }

    static boolean editInvoiceDialog(Component parent, Item item) {
        PatientData p = HeilpraktikerStore.loadPatientSafe(item.__getContactsids());
        InvoiceData d = HeilpraktikerStore.loadInvoiceSafe(item.__getIDS());
        Window owner = parent == null ? null : SwingUtilities.getWindowAncestor(parent);
        final JDialog dialog = new JDialog(owner, "Heilpraktiker-Rechnungsdaten \u2013 " + item.__getCnumber(), JDialog.ModalityType.APPLICATION_MODAL);
        final boolean[] saved = {false};

        JTextField icd = new JTextField(d.icd10Code, 20);
        JTextField diag = new JTextField(d.diagnosisText, 36);
        JTextField dates = new JTextField(d.treatmentDates, 36);
        JTextField retention = new JTextField(d.retentionUntil, 14);
        retention.setToolTipText("Leer lassen f\u00fcr automatische Berechnung (10 Jahre nach Behandlungsdatum, \u00a7 630f Abs. 3 BGB).");
        JCheckBox printDiag = new JCheckBox(
                "Diagnose auf Rechnung drucken (Art. 9 DSGVO / \u00a7 203 StGB)",
                HeilpraktikerValidator.shouldPrintDiagnosis(p, d));
        JCheckBox heil = new JCheckBox("Medizinisch indizierte Heilbehandlung (\u00a7 4 Nr. 14 Buchst. a UStG, 0 % USt)", d.heilbehandlung);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(4, 6, 4, 6);
        g.anchor = GridBagConstraints.WEST;
        g.fill = GridBagConstraints.HORIZONTAL;
        int row = 0;
        addRow(form, g, row++, "ICD-10-GM Code(s) (z. B. M54.5):", icd);
        addRow(form, g, row++, "Diagnose (Klartext):", diag);
        addRow(form, g, row++, "Behandlungsdatum / -termine:", dates);
        addRow(form, g, row++, "Aufbewahren bis (\u00a7 630f BGB, 10 J.):", retention);
        g.gridx = 0;
        g.gridy = row++;
        g.gridwidth = 2;
        form.add(printDiag, g);
        g.gridy = row++;
        form.add(heil, g);

        JLabel privacyHint = new JLabel("<html><div style='width:460px; color:#555555'>"
                + "Hinweis (Art. 9 DSGVO): Diagnosen sind Gesundheitsdaten. F\u00fcr PKV/Beihilfe ist der Druck "
                + "standardm\u00e4\u00dfig aktiv, f\u00fcr Selbstzahler deaktiviert. Rechnungen mit gedruckter Diagnose "
                + "sollten nicht unverschl\u00fcsselt per E-Mail versendet werden."
                + "</div></html>");
        g.gridy = row++;
        g.insets = new Insets(8, 6, 4, 6);
        form.add(privacyHint, g);

        JButton cancel = new JButton("Abbrechen");
        cancel.addActionListener(e -> dialog.dispose());
        JButton ok = new JButton("Speichern");
        ok.addActionListener(e -> {
            d.icd10Code = icd.getText().trim();
            d.diagnosisText = diag.getText().trim();
            d.treatmentDates = dates.getText().trim();
            d.retentionUntil = retention.getText().trim();
            d.printDiagnosis = printDiag.isSelected();
            d.heilbehandlung = heil.isSelected();
            try {
                Connection conn = HeilpraktikerStore.connection();
                HeilpraktikerStore.saveInvoice(conn, d);
                // Keep patient's last treatment date / 10-year retention in sync without touching SOAP notes
                LocalDate latest = HeilpraktikerValidator.parseLatestTreatmentDate(d.treatmentDates);
                if (latest != null && item.__getContactsids() > 0) {
                    PatientData pat = HeilpraktikerStore.loadPatientSafe(item.__getContactsids());
                    LocalDate prev = HeilpraktikerValidator.parseDate(pat.lastTreatmentDate);
                    if (prev == null || latest.isAfter(prev)) {
                        pat.lastTreatmentDate = latest.toString();
                        pat.retentionUntil = HeilpraktikerValidator.calculateTreatmentRetentionUntil(latest).toString();
                        HeilpraktikerStore.savePatientWithoutSoap(conn, pat);
                    }
                }
                saved[0] = true;
                dialog.dispose();
            } catch (Exception ex) {
                Log.Debug(HeilpraktikerUi.class, ex);
                JOptionPane.showMessageDialog(dialog, "Speichern fehlgeschlagen: " + ex.getMessage(), "Fehler", JOptionPane.ERROR_MESSAGE);
            }
        });

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        btns.add(cancel);
        btns.add(ok);

        JPanel root = new JPanel(new BorderLayout());
        root.add(form, BorderLayout.CENTER);
        root.add(btns, BorderLayout.SOUTH);
        dialog.setContentPane(root);
        dialog.pack();
        dialog.setLocationRelativeTo(owner);
        dialog.setVisible(true);
        return saved[0];
    }

    static void showPurgeExpiredDialog(Component parent, ExtensionContext yabs) {
        if (!HeilpraktikerStore.hasDatabase()) {
            JOptionPane.showMessageDialog(parent, "Keine Datenbankverbindung.", "Heilpraktiker DSGVO", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            Connection conn = HeilpraktikerStore.connection();
            LocalDate today = LocalDate.now();
            List<PatientData> patients = HeilpraktikerStore.findExpiredPatients(conn, today);
            List<InvoiceData> invoices = HeilpraktikerStore.findExpiredInvoices(conn, today);
            if (patients.isEmpty() && invoices.isEmpty()) {
                JOptionPane.showMessageDialog(parent,
                        "Keine abgelaufenen Gesundheitsdaten gefunden.\n"
                        + "(Aufbewahrungsfrist: 10 Jahre nach Behandlung gem\u00e4\u00df \u00a7 630f Abs. 3 BGB.)",
                        "Heilpraktiker \u2013 Aufbewahrungsfristen (DSGVO)",
                        JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            StringBuilder sb = new StringBuilder("Folgende Gesundheitsdaten haben ihre 10-Jahres-Aufbewahrungsfrist (\u00a7 630f Abs. 3 BGB) \u00fcberschritten:\n\n");
            sb.append("\u2022 Patientendatens\u00e4tze (SOAP / Geburtsdatum): ").append(patients.size()).append("\n");
            for (int i = 0; i < Math.min(patients.size(), 10); i++) {
                PatientData p = patients.get(i);
                sb.append("   - Kontakt-ID ").append(p.contactId).append(" (Frist: ").append(p.retentionUntil).append(")\n");
            }
            if (patients.size() > 10) {
                sb.append("   ... und ").append(patients.size() - 10).append(" weitere\n");
            }
            sb.append("\u2022 Rechnungsdiagnosen: ").append(invoices.size()).append("\n");
            for (int i = 0; i < Math.min(invoices.size(), 10); i++) {
                InvoiceData d = invoices.get(i);
                sb.append("   - Beleg-ID ").append(d.itemId).append(" (Frist: ").append(d.retentionUntil).append(")\n");
            }
            if (invoices.size() > 10) {
                sb.append("   ... und ").append(invoices.size() - 10).append(" weitere\n");
            }
            sb.append("\nM\u00f6chten Sie diese Datens\u00e4tze jetzt anonymisieren (Diagnosen/SOAP leeren) oder vollst\u00e4ndig l\u00f6schen?");

            Object[] options = {"Anonymisieren", "Vollst\u00e4ndig l\u00f6schen", "Abbrechen"};
            int choice = JOptionPane.showOptionDialog(parent, sb.toString(),
                    "Heilpraktiker \u2013 Abgelaufene Gesundheitsdaten (DSGVO)",
                    JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[0]);
            if (choice != 0 && choice != 1) {
                return;
            }
            boolean deleteRows = (choice == 1);
            PurgeResult res = HeilpraktikerStore.purgeExpired(conn, today, deleteRows);
            String msg = (deleteRows ? "Gel\u00f6scht: " : "Anonymisiert: ")
                    + res.patientsAffected + " Patientendatens\u00e4tze, "
                    + res.invoicesAffected + " Rechnungsdiagnosen.";
            if (yabs != null) {
                yabs.notify(msg);
            }
            JOptionPane.showMessageDialog(parent, msg, "Heilpraktiker \u2013 DSGVO", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            Log.Debug(HeilpraktikerUi.class, ex);
            JOptionPane.showMessageDialog(parent, "Fehler bei der Bereinigung: " + ex.getMessage(), "Fehler", JOptionPane.ERROR_MESSAGE);
        }
    }

    static void showValidationResult(Component parent, List<String> warnings) {
        if (warnings == null || warnings.isEmpty()) {
            JOptionPane.showMessageDialog(parent,
                    "Keine Geb\u00fcH- / PKV-Hinweise gefunden.",
                    "Heilpraktiker-Rechnungspr\u00fcfung",
                    JOptionPane.INFORMATION_MESSAGE);
        } else {
            StringBuilder sb = new StringBuilder("Gefundene Hinweise:\n\n");
            for (String w : warnings) {
                sb.append("\u2022 ").append(w).append("\n");
            }
            JOptionPane.showMessageDialog(parent,
                    sb.toString(),
                    "Heilpraktiker-Rechnungspr\u00fcfung",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    private static void addRow(JPanel panel, GridBagConstraints g, int row, String label, JComponent field) {
        g.gridwidth = 1;
        g.weighty = 0;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.gridx = 0;
        g.gridy = row;
        g.weightx = 0.0;
        panel.add(new JLabel(label), g);
        g.gridx = 1;
        g.weightx = 1.0;
        panel.add(field, g);
    }

    private static void addActionRow(JPanel panel, GridBagConstraints g, int row, JButton btn, String desc) {
        g.gridwidth = 1;
        g.weighty = 0;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.gridx = 0;
        g.gridy = row;
        g.weightx = 0.0;
        panel.add(btn, g);
        g.gridx = 1;
        g.weightx = 1.0;
        panel.add(new JLabel("<html><div style='width:380px'>" + desc + "</div></html>"), g);
    }
}
