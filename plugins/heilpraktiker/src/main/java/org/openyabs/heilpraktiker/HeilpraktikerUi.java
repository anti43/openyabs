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
import java.util.List;
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

    static JComponent settingsPage(final ExtensionContext yabs) {
        final Settings s = Settings.load();

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

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(5, 6, 5, 6);
        g.anchor = GridBagConstraints.WEST;
        g.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;
        addRow(form, g, row++, "Berufsbezeichnung ([hp.practitionerTitle]):", titleField);
        addRow(form, g, row++, "Erlaubnis / Gesundheitsamt ([hp.licenseAuthority]):", licenseField);
        addRow(form, g, row++, "USt-Befreiungshinweis ([hp.ustgNotice]):", new JScrollPane(ustgArea));
        addRow(form, g, row++, "Standard-Honorar bei Geb\u00fcH-Import:", priceMode);

        g.gridx = 0;
        g.gridy = row++;
        g.gridwidth = 2;
        form.add(warnOnSave, g);

        JLabel info = new JLabel("<html><div style='width:540px'>"
                + "<b>Hinweise (Stand 2026):</b><br>"
                + "&bull; Katalog: offizielles Geb\u00fcH 85/2002 (144 Ziffern, 1&ndash;39.13). "
                + "Beihilfes\u00e4tze (BBhV / Landes-BhV) k\u00f6nnen niedriger sein als die Geb\u00fcH-H\u00f6chsts\u00e4tze.<br>"
                + "&bull; Aufbewahrungsfristen: Patientenakten 10 Jahre (&sect; 630f Abs. 3 BGB), "
                + "Buchungsbelege / Rechnungen 8 Jahre seit 01.01.2025 (&sect; 14b UStG / &sect; 147 AO, BEG IV).<br>"
                + "&bull; Vorlagen-Platzhalter: <code>[hp.patientDob]</code>, <code>[hp.insuranceType]</code>, "
                + "<code>[hp.icd10]</code>, <code>[hp.diagnosis]</code>, <code>[hp.treatmentDates]</code>, "
                + "<code>[hp.ustgNotice]</code>, <code>[hp.practitionerTitle]</code>, <code>[hp.licenseAuthority]</code>."
                + "</div></html>");
        g.gridy = row++;
        g.insets = new Insets(12, 6, 6, 6);
        form.add(info, g);

        JButton saveBtn = new JButton("Speichern");
        saveBtn.addActionListener(e -> {
            s.practitionerTitle = titleField.getText().trim();
            s.licenseAuthority = licenseField.getText().trim();
            s.ustgNotice = ustgArea.getText().trim();
            int idx = priceMode.getSelectedIndex();
            s.defaultPriceMode = idx == 1 ? PriceMode.MID : idx == 2 ? PriceMode.MIN : PriceMode.MAX;
            s.warnOnSave = warnOnSave.isSelected();
            s.save();
            if (yabs != null) {
                yabs.notify("Heilpraktiker-Einstellungen gespeichert.");
            }
        });

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        actions.add(saveBtn);

        JPanel root = new JPanel(new BorderLayout());
        root.add(new JScrollPane(form), BorderLayout.CENTER);
        root.add(actions, BorderLayout.SOUTH);
        return root;
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
        String summary = "Heilpraktiker-Patientendaten: Geburtsdatum "
                + (p.birthDate.isEmpty() ? "\u2013" : p.birthDate)
                + " \u00b7 " + p.insuranceType
                + (p.honorarvereinbarung ? " \u00b7 Honorarvereinbarung (\u00a7 630a BGB)" : "");
        JButton edit = new JButton("Patientendaten / SOAP");
        edit.addActionListener(e -> {
            if (editPatientDialog(panel instanceof Component ? (Component) panel : null, contact)) {
                decorateContact(panel, contact, yabs);
            }
        });
        panel.setPluginBanner(banner(summary, edit));
    }

    private static void decorateInvoice(final DataPanel panel, final Item item, final ExtensionContext yabs, final GebuehCatalog catalog) {
        InvoiceData d = HeilpraktikerStore.loadInvoiceSafe(item.__getIDS());
        String summary = "Heilpraktiker-Rechnung: "
                + (d.heilbehandlung ? "\u00a7 4 Nr. 14 UStG (0 %)" : "Pr\u00e4vention/Wellness")
                + " \u00b7 Diagnose: " + (d.diagnosisText.isEmpty() ? "\u2013" : d.diagnosisText)
                + (d.icd10Code.isEmpty() ? "" : " [" + d.icd10Code + "]");
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
        panel.setPluginBanner(banner(summary, edit, check));
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
        Window owner = parent == null ? null : SwingUtilities.getWindowAncestor(parent);
        final JDialog dialog = new JDialog(owner, "Patientendaten \u2013 " + contact.__getCname(), JDialog.ModalityType.APPLICATION_MODAL);
        final boolean[] saved = {false};

        JTextField dob = new JTextField(p.birthDate, 14);
        JComboBox<String> ins = new JComboBox<String>(new String[]{"Selbstzahler", "PKV", "Beihilfe", "PKV + Beihilfe", "Zusatzversicherung"});
        ins.setEditable(true);
        ins.setSelectedItem(p.insuranceType);
        JCheckBox honorar = new JCheckBox("Schriftliche Honorarvereinbarung (\u00a7 630a BGB) liegt vor", p.honorarvereinbarung);
        JTextArea soap = new JTextArea(p.soapNotes, 8, 42);
        soap.setLineWrap(true);
        soap.setWrapStyleWord(true);

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
        form.add(honorar, g);
        g.gridy = row++;
        form.add(new JLabel("Behandlungsdokumentation / SOAP-Notizen (\u00a7 630f BGB, 10 Jahre Aufbewahrung):"), g);
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
            p.honorarvereinbarung = honorar.isSelected();
            p.soapNotes = soap.getText().trim();
            try {
                HeilpraktikerStore.savePatient(HeilpraktikerStore.connection(), p);
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
        InvoiceData d = HeilpraktikerStore.loadInvoiceSafe(item.__getIDS());
        Window owner = parent == null ? null : SwingUtilities.getWindowAncestor(parent);
        final JDialog dialog = new JDialog(owner, "Heilpraktiker-Rechnungsdaten \u2013 " + item.__getCnumber(), JDialog.ModalityType.APPLICATION_MODAL);
        final boolean[] saved = {false};

        JTextField icd = new JTextField(d.icd10Code, 20);
        JTextField diag = new JTextField(d.diagnosisText, 36);
        JTextField dates = new JTextField(d.treatmentDates, 36);
        JCheckBox heil = new JCheckBox("Medizinisch indizierte Heilbehandlung (\u00a7 4 Nr. 14 Buchst. a UStG, 0 % USt)", d.heilbehandlung);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(4, 6, 4, 6);
        g.anchor = GridBagConstraints.WEST;
        g.fill = GridBagConstraints.HORIZONTAL;
        int row = 0;
        addRow(form, g, row++, "ICD-10-GM Code(s) (z. B. M54.5):", icd);
        addRow(form, g, row++, "Diagnose (Klartext f\u00fcr Rechnung):", diag);
        addRow(form, g, row++, "Behandlungsdatum / -termine:", dates);
        g.gridx = 0;
        g.gridy = row++;
        g.gridwidth = 2;
        form.add(heil, g);

        JButton cancel = new JButton("Abbrechen");
        cancel.addActionListener(e -> dialog.dispose());
        JButton ok = new JButton("Speichern");
        ok.addActionListener(e -> {
            d.icd10Code = icd.getText().trim();
            d.diagnosisText = diag.getText().trim();
            d.treatmentDates = dates.getText().trim();
            d.heilbehandlung = heil.isSelected();
            try {
                HeilpraktikerStore.saveInvoice(HeilpraktikerStore.connection(), d);
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
}
