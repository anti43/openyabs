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
package org.openyabs.datev;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.DefaultTableModel;
import mpv5.db.objects.Account;
import mpv5.handler.JournalCalc;
import mpv5.logging.Log;
import mpv5.plugin.api.ExtensionContext;

/**
 * Control panel settings page for the DATEV export plugin.
 */
final class DatevSettingsPage {

    private DatevSettingsPage() {
    }

    static JComponent create(final ExtensionContext yabs) {
        final List<Account> accounts = loadAccounts();
        final Set<BigDecimal> taxRates = loadTaxRates();
        //as the export uses them: what was never saved from the chart of accounts
        final DatevConfig config = DatevConfig.loadWithDefaults(null);

        final JTextField berater = new JTextField(config.beraterNummer > 0 ? String.valueOf(config.beraterNummer) : "", 12);
        final JTextField mandant = new JTextField(config.mandantenNummer > 0 ? String.valueOf(config.mandantenNummer) : "", 12);
        final JTextField wjBeginn = new JTextField(config.wjBeginn, 12);
        final JSpinner sachkontenLaenge = new JSpinner(new SpinnerNumberModel(
                Math.max(4, Math.min(8, config.sachkontenLaenge)), 4, 8, 1));
        final JComboBox<String> kontenrahmen = new JComboBox<String>(new String[]{"03 (SKR03)", "04 (SKR04)", "(nicht gesetzt)"});
        if ("04".equals(config.kontenrahmen)) {
            kontenrahmen.setSelectedIndex(1);
        } else if ("03".equals(config.kontenrahmen)) {
            kontenrahmen.setSelectedIndex(0);
        } else {
            kontenrahmen.setSelectedIndex(2);
        }
        final JTextField bezeichnung = new JTextField(config.bezeichnung, 24);
        final JTextField diktat = new JTextField(config.diktatKuerzel, 6);
        final JCheckBox festschreibung = new JCheckBox("Stapel beim Import in DATEV festschreiben", config.festschreibung);
        final JComboBox<String> belegdatumMode = new JComboBox<String>(new String[]{
            "Zahlungsdatum (EÜR / Ist-Versteuerung)",
            "Belegdatum (Rechnungsdatum)"
        });
        belegdatumMode.setSelectedIndex(config.belegdatumMode == DatevConfig.DateMode.DOCUMENT_DATE ? 1 : 0);

        final JTextField moneyIncome = new JTextField(config.moneyAccountIncome, 10);
        final JTextField moneyExpense = new JTextField(config.moneyAccountExpense, 10);
        final JTextField cashAccount = new JTextField(config.cashAccount, 10);
        final JTextField euAccount = new JTextField(config.euDeliveryAccount, 10);
        final JTextField euServiceAccount = new JTextField(config.euServiceAccount, 10);
        final JTextField reverseChargeAccount = new JTextField(config.reverseChargeAccount, 10);
        final JTextField exportAccount = new JTextField(config.exportAccount, 10);
        final JTextField invoiceFolder = new JTextField(config.invoiceFolder, 24);
        final JCheckBox automaticAccounts = new JCheckBox(
                "Auf DATEV-Automatikkonten (AM/AV) keinen BU-Schlüssel exportieren",
                config.useAutomaticAccounts);
        final JCheckBox leistungsdatumExportieren = new JCheckBox(
                "Leistungsdatum und Steuerperiode exportieren (Felder 115–116)",
                config.leistungsdatumExportieren);

        final DefaultTableModel ratesModel = new DefaultTableModel(
                new String[]{"Steuersatz (%)", "Erlöskonto (Einnahmen)", "Aufwandskonto (Ausgaben)", "BU-Schlüssel Einnahmen", "BU-Schlüssel Ausgaben"}, 0) {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean isCellEditable(int row, int col) {
                return col > 0;
            }
        };
        fillRatesTable(ratesModel, config, taxRates);

        JTable ratesTable = new JTable(ratesModel);
        ratesTable.setRowHeight(24);
        JScrollPane ratesScroll = new JScrollPane(ratesTable);
        ratesScroll.setPreferredSize(new Dimension(mpv5.ui.modern.ModernTheme.px(640), mpv5.ui.modern.ModernTheme.px(150)));

        final JTextField customAutomatic = new JTextField(String.join(", ", config.customAutomaticAccounts), 24);
        kontenrahmen.setToolTipText("SKR03: Prozessgliederungsprinzip, SKR04: Abschlussgliederungsprinzip");

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(3, 6, 3, 6);
        g.anchor = GridBagConstraints.WEST;
        g.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;
        row = note(form, g, row, "Die Kontenvorschläge, Steuerschlüssel und UStVA-Kennzahlen auf dieser Seite folgen der "
                + "DATEV-Steuerschlüssel-Tabelle 2026 für SKR03/SKR04 (DATEV Dok.-Nr. 0907048), die Feldregeln der "
                + "Formatbeschreibung „DATEV-Format“ (developer.datev.de). Werte, die nie gespeichert wurden, übernimmt der Export "
                + "aus dem Kontenrahmen; „Standardwerte aus Kontenrahmen“ setzt alle Vorschläge des gewählten Kontenrahmens ein.");

        row = section(form, g, row, "Mandant und Stapel (Kopfzeile der DATEV-Datei)");
        row = field(form, g, row, "Beraternummer:", berater,
                "Nummer der Steuerberatung bei DATEV (1001–9999999). Bei der Kanzlei erfragen – ohne gültige Nummer lehnt DATEV den Import ab.");
        row = field(form, g, row, "Mandantennummer:", mandant,
                "Ihre Mandantennummer bei der Kanzlei (1–99999). Sie muss zum Mandanten in DATEV passen.");
        row = field(form, g, row, "Wirtschaftsjahresbeginn:", wjBeginn,
                "Erster Tag des Wirtschaftsjahres als JJJJMMTT – Kalenderjahr z. B. 20260101, abweichendes Wirtschaftsjahr z. B. 20250701. "
                + "Das Belegdatum einer Buchung steht in der Datei nur als TTMM; das Jahr ermittelt DATEV aus diesem Feld. "
                + "Daher je Wirtschaftsjahr eine eigene Datei exportieren.");
        row = field(form, g, row, "Sachkontenlänge:", sachkontenLaenge,
                "Stellenzahl der Sachkonten im DATEV-Mandanten, meist 4 (also 8400, 1200). Muss mit der Einstellung des Mandanten "
                + "in DATEV übereinstimmen; Personenkonten (Kunden, Lieferanten) haben eine Stelle mehr, bei 4 also 10000–99999.");
        row = field(form, g, row, "Kontenrahmen:", kontenrahmen,
                "SKR03 (Prozessgliederung, Erlöse 8xxx) oder SKR04 (Abschlussgliederung, Erlöse 4xxx). Wird aus den importierten "
                + "Konten erkannt und bestimmt die Vorschläge auf dieser Seite. Die Kanzlei weiß, welcher Kontenrahmen gilt.");
        row = field(form, g, row, "Bezeichnung des Stapels:", bezeichnung,
                "Name des Stapels in DATEV, höchstens 30 Zeichen (Buchstaben, Ziffern, Leerzeichen, Punkt, Schrägstrich), "
                + "z. B. „Rechnungsausgang 03/2026“.");
        row = field(form, g, row, "Diktatkürzel:", diktat,
                "Kürzel der bearbeitenden Person: leer, 2 oder 4 Großbuchstaben (z. B. MM für Max Mustermann). "
                + "DATEV lässt keinen einzelnen Buchstaben zu – ein einzelner Buchstabe wird nicht exportiert.");
        row = check(form, g, row, festschreibung,
                "Festgeschriebene Buchungen kann die Kanzlei nicht mehr ändern (GoBD). DATEV schreibt standardmäßig fest; YaBS "
                + "schreibt nur fest, wenn hier eingeschaltet – so kann die Kanzlei den Stapel vor dem Festschreiben prüfen.");

        row = section(form, g, row, "Datum und Auswahl der Buchungen");
        row = field(form, g, row, "Belegdatum:", belegdatumMode,
                "Zahlungsdatum (Ist-Versteuerung, EÜR): der Stapel enthält die im Zeitraum bezahlten Belege mit ihrem "
                + "Zahlungsdatum, Teilzahlungen einzeln. Belegdatum: die bezahlten Belege, deren Rechnungsdatum im Zeitraum liegt. "
                + "Offene Rechnungen sind in beiden Fällen nicht im Stapel (gebucht wird gegen das Geldkonto); der Exportdialog "
                + "nennt sie. Für die Soll-Versteuerung mit offenen Posten nutzt die Kanzlei besser das Belegpaket.");
        row = check(form, g, row, leistungsdatumExportieren,
                "Weicht das Leistungsdatum vom Buchungsdatum ab, gehen Leistungsdatum (Feld 115) und Datum der Zuordnung zur "
                + "Steuerperiode (Feld 116) mit – wichtig bei Leistungen über einen Monatswechsel und bei Steuersatzänderungen "
                + "(§ 14 Abs. 4 Nr. 6 UStG).");

        row = section(form, g, row, "Geldkonten");
        row = field(form, g, row, "Geldkonto Einnahmen:", moneyIncome,
                "Konto, auf dem Zahlungseingänge gebucht werden (Soll). Vorschlag: SKR03 1200 · SKR04 1800 „Bank“.");
        row = field(form, g, row, "Geldkonto Ausgaben:", moneyExpense,
                "Konto, von dem Ausgaben und Erstattungen (Gutschriften) bezahlt werden (Haben). Vorschlag: SKR03 1200 · SKR04 1800 „Bank“.");
        row = field(form, g, row, "Kassenkonto Barverkauf:", cashAccount,
                "Nur für Barverkäufe aus der Kasse (POS). Vorschlag: SKR03 1000 · SKR04 1600 „Kasse“. Leer: Barverkäufe gehen "
                + "ebenfalls auf das Geldkonto Einnahmen.");

        row = section(form, g, row, "Erlöskonten für Umsätze ohne Umsatzsteuer (nach der Steuerkategorie des Steuersatzes)");
        row = note(form, g, row, "Die Kategorie gehört zum Steuersatz der Positionen (Einstellungen › Steuersätze: K, AE, G, E). "
                + "Kontenvorschlag – DATEV-Steuerschlüssel, Kennzahl der Umsatzsteuer-Voranmeldung (UStVA):");
        row = field(form, g, row, "EU-Lieferung (Kategorie K):", euAccount,
                "Steuerfreie innergemeinschaftliche Lieferung an ein Unternehmen mit USt-IdNr. (§ 4 Nr. 1b UStG). "
                + "Vorschlag: SKR03 8125 · SKR04 4125 – Steuerschlüssel 11, UStVA Kz 41, Zusammenfassende Meldung.");
        row = field(form, g, row, "EU-Leistung (Kategorie AE):", euServiceAccount,
                "Sonstige Leistung an ein Unternehmen mit USt-IdNr. eines anderen EU-Staates (Reverse Charge, § 18b UStG). "
                + "Vorschlag: SKR03 8336 · SKR04 4336 – Steuerschlüssel 47, UStVA Kz 21, Zusammenfassende Meldung.");
        row = field(form, g, row, "§ 13b UStG im Inland (Kategorie AE):", reverseChargeAccount,
                "Leistung an einen deutschen Kunden, der die Steuer schuldet (§ 13b UStG; Kategorie AE ohne EU-USt-IdNr.). "
                + "Vorschlag: SKR03 8337 · SKR04 4337 – Steuerschlüssel 46, UStVA Kz 60.");
        row = field(form, g, row, "Ausfuhr Drittland (Kategorie G):", exportAccount,
                "Steuerfreie Ausfuhrlieferung in ein Land außerhalb der EU (§ 4 Nr. 1a UStG). "
                + "Vorschlag: SKR03 8120 · SKR04 4120 – Steuerschlüssel 173, UStVA Kz 43. Nicht für Inlandsumsätze verwenden.");
        row = note(form, g, row, "0 % ohne Kategorie oder Kategorie E (steuerfreier Inlandsumsatz, § 4 Nr. 8 ff. UStG): Konto des "
                + "Steuersatzes 0 % in der Tabelle unten, Vorschlag SKR03 8100 · SKR04 4100 – Steuerschlüssel 181, UStVA Kz 48. "
                + "Kleinunternehmer (§ 19 UStG): SKR03 8195 · SKR04 4185 „Erlöse als Kleinunternehmer“. Die Kleinunternehmerregelung "
                + "ist in den Firmendaten " + (config.smallBusiness ? "eingeschaltet." : "ausgeschaltet."));

        row = section(form, g, row, "Automatikkonten und BU-Schlüssel");
        row = check(form, g, row, automaticAccounts,
                "Automatikkonten rechnen die Umsatz- bzw. Vorsteuer in DATEV selbst (AM = Umsatzsteuer, AV = Vorsteuer), "
                + "z. B. SKR03 8400/8300 · SKR04 4400/4300 (Erlöse 19 %/7 %) und SKR03 3400/3300 · SKR04 5400/5300 (Wareneingang "
                + "19 %/7 %). Auf ihnen bleibt der BU-Schlüssel leer – ein zusätzlicher Schlüssel führt zu Fehlern beim Import. "
                + "Auf allen anderen Konten setzt der Export den BU-Schlüssel aus der Tabelle unten.");
        row = field(form, g, row, "Eigene Automatikkonten:", customAutomatic,
                "Weitere Konten, die in Ihrem DATEV-Mandanten als Automatikkonto eingerichtet sind, durch Komma getrennt "
                + "(z. B. 8401, 8402). Auf ihnen bleibt der BU-Schlüssel ebenfalls leer. Nur nach Rücksprache mit der Kanzlei eintragen.");

        row = section(form, g, row, "Konten und BU-Schlüssel je Steuersatz");
        row = note(form, g, row, "Erlöskonto (Einnahmen) und Aufwandskonto (Ausgaben) gelten, wenn das Konto des Belegs nicht zum "
                + "Steuersatz passt; der BU-Schlüssel wird nur auf Konten ohne Automatik gesetzt. Vorschläge:<br>"
                + "• 19 %: Erlöse SKR03 8400 · SKR04 4400, BU 3 (Umsatzsteuer, Kz 81) – Aufwand SKR03 3400 · SKR04 5400, BU 9 (Vorsteuer, Kz 66)<br>"
                + "• 7 %: Erlöse SKR03 8300 · SKR04 4300, BU 2 (Kz 86) – Aufwand SKR03 3300 · SKR04 5300, BU 8 (Kz 66)<br>"
                + "• 0 %: Erlöse SKR03 8100 · SKR04 4100 ohne BU (Kz 48; Kleinunternehmer 8195 · 4185) – Aufwand SKR03 4900 · SKR04 6300 "
                + "„Sonstige betriebliche Aufwendungen“ ohne BU<br>"
                + "Die zweistelligen Schlüssel 3, 2, 9, 8 entsprechen den dreistelligen 101, 102, 401, 402 der DATEV-Tabelle; beide sind gültig.");

        g.gridx = 0;
        g.gridy = row++;
        g.gridwidth = 2;
        g.weightx = 1.0;
        g.weighty = 1.0;
        g.fill = GridBagConstraints.BOTH;
        form.add(ratesScroll, g);
        g.weighty = 0;
        g.fill = GridBagConstraints.HORIZONTAL;

        row = section(form, g, row, "Belegpaket (DATEV Unternehmen online)");
        row = field(form, g, row, "Rechnungsordner:", invoiceFolder,
                "Name des Ordners für Ausgangsrechnungen in Belege online, wie er in DATEV Unternehmen online eingerichtet ist "
                + "(DATEV-Standard „Ausgangsrechnungen“). Berater- und Mandantennummer braucht das Belegpaket nicht: den Mandanten "
                + "wählen Sie beim Übertragen in DATEV Belegtransfer.");

        JButton defaultsBtn = new JButton("Standardwerte aus Kontenrahmen");
        defaultsBtn.addActionListener(e -> {
            config.kontenrahmen = selectedKontenrahmen(kontenrahmen);
            config.prefillFromAccounts(accounts, taxRates, true);
            moneyIncome.setText(config.moneyAccountIncome);
            moneyExpense.setText(config.moneyAccountExpense);
            cashAccount.setText(config.cashAccount);
            euAccount.setText(config.euDeliveryAccount);
            euServiceAccount.setText(config.euServiceAccount);
            reverseChargeAccount.setText(config.reverseChargeAccount);
            exportAccount.setText(config.exportAccount);
            fillRatesTable(ratesModel, config, taxRates);
        });

        JButton saveBtn = new JButton("Speichern");
        saveBtn.addActionListener(e -> {
            if (ratesTable.isEditing() && ratesTable.getCellEditor() != null) {
                ratesTable.getCellEditor().stopCellEditing();
            }
            config.beraterNummer = parseInt(berater.getText(), 0);
            config.mandantenNummer = parseInt(mandant.getText(), 0);
            config.wjBeginn = wjBeginn.getText().trim();
            config.sachkontenLaenge = ((Number) sachkontenLaenge.getValue()).intValue();
            config.kontenrahmen = selectedKontenrahmen(kontenrahmen);
            config.bezeichnung = bezeichnung.getText().trim();
            config.diktatKuerzel = diktat.getText().trim().toUpperCase();
            config.festschreibung = festschreibung.isSelected();
            config.belegdatumMode = belegdatumMode.getSelectedIndex() == 1
                    ? DatevConfig.DateMode.DOCUMENT_DATE
                    : DatevConfig.DateMode.PAYMENT_DATE;
            config.moneyAccountIncome = moneyIncome.getText().trim();
            config.moneyAccountExpense = moneyExpense.getText().trim();
            config.cashAccount = cashAccount.getText().trim();
            config.euDeliveryAccount = euAccount.getText().trim();
            config.euServiceAccount = euServiceAccount.getText().trim();
            config.reverseChargeAccount = reverseChargeAccount.getText().trim();
            config.exportAccount = exportAccount.getText().trim();
            config.invoiceFolder = invoiceFolder.getText().trim();
            config.useAutomaticAccounts = automaticAccounts.isSelected();
            config.customAutomaticAccounts.clear();
            for (String part : customAutomatic.getText().split("[,;\\s]+")) {
                String d = DatevFormat.digitsOnly(part);
                if (!d.isEmpty()) {
                    config.customAutomaticAccounts.add(d);
                }
            }
            config.leistungsdatumExportieren = leistungsdatumExportieren.isSelected();

            config.revenueAccounts.clear();
            config.expenseAccounts.clear();
            config.buKeysIncome.clear();
            config.buKeysExpense.clear();
            for (int r = 0; r < ratesModel.getRowCount(); r++) {
                try {
                    BigDecimal rate = JournalCalc.rateKey(new BigDecimal(String.valueOf(ratesModel.getValueAt(r, 0)).replace(',', '.').trim()));
                    config.revenueAccounts.put(rate, str(ratesModel.getValueAt(r, 1)));
                    config.expenseAccounts.put(rate, str(ratesModel.getValueAt(r, 2)));
                    config.buKeysIncome.put(rate, str(ratesModel.getValueAt(r, 3)));
                    config.buKeysExpense.put(rate, str(ratesModel.getValueAt(r, 4)));
                } catch (Exception ex) {
                    Log.Debug(DatevSettingsPage.class, ex.getMessage());
                }
            }
            config.save();
            //what DATEV would not take: said right away, not only when exporting
            java.util.List<String> hints = new java.util.ArrayList<String>();
            String d = diktat.getText().trim();
            if (!d.isEmpty() && !DatevFormat.sanitizeDiktat(d).equals(d.toUpperCase())) {
                hints.add("Diktatkürzel: nur 2 oder 4 Großbuchstaben (exportiert wird „" + DatevFormat.sanitizeDiktat(d) + "“)");
            }
            if (!config.wjBeginn.matches("20\\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\\d|3[01])")) {
                hints.add("Wirtschaftsjahresbeginn: Format JJJJMMTT, z. B. 20260101");
            }
            if (config.beraterNummer < 1001 || config.beraterNummer > 9999999) {
                hints.add("Beraternummer: 1001–9999999");
            }
            if (config.mandantenNummer < 1 || config.mandantenNummer > 99999) {
                hints.add("Mandantennummer: 1–99999");
            }
            if (yabs != null) {
                yabs.notify(hints.isEmpty() ? "DATEV-Einstellungen gespeichert."
                        : "DATEV-Einstellungen gespeichert. Bitte prüfen: " + String.join("; ", hints));
            }
        });

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        actions.add(defaultsBtn);
        actions.add(saveBtn);

        JPanel root = new JPanel(new BorderLayout());
        root.add(new JScrollPane(form), BorderLayout.CENTER);
        root.add(actions, BorderLayout.SOUTH);
        return root;
    }

    /**
     * the width of the explanations (they wrap)
     */
    private static final int HELP_WIDTH = 620;

    /**
     * @param html
     * @return an explanation: muted, smaller, wrapped
     */
    static JLabel help(String html) {
        JLabel l = new JLabel("<html><div style='width:" + mpv5.ui.modern.ModernTheme.px(HELP_WIDTH) + "px'>" + html + "</div></html>");
        l.setForeground(mpv5.ui.modern.ModernTheme.muted());
        l.setFont(l.getFont().deriveFont(l.getFont().getSize2D() - 1f));
        return l;
    }

    private static int section(JPanel panel, GridBagConstraints g, int row, String title) {
        g.gridx = 0;
        g.gridy = row;
        g.gridwidth = 2;
        g.weightx = 1.0;
        g.insets = new Insets(16, 6, 4, 6);
        JLabel l = new JLabel(title);
        l.setFont(l.getFont().deriveFont(java.awt.Font.BOLD, l.getFont().getSize2D() + 1f));
        panel.add(l, g);
        g.insets = new Insets(3, 6, 3, 6);
        g.gridwidth = 1;
        return row + 1;
    }

    private static int note(JPanel panel, GridBagConstraints g, int row, String html) {
        g.gridx = 0;
        g.gridy = row;
        g.gridwidth = 2;
        g.weightx = 1.0;
        panel.add(help(html), g);
        g.gridwidth = 1;
        return row + 1;
    }

    /**
     * A field with its explanation below it
     */
    private static int field(JPanel panel, GridBagConstraints g, int row, String label, JComponent field, String explanation) {
        g.gridwidth = 1;
        g.weighty = 0;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.gridx = 0;
        g.gridy = row;
        g.weightx = 0.0;
        JLabel l = new JLabel(label);
        l.setLabelFor(field);
        panel.add(l, g);
        g.gridx = 1;
        g.weightx = 1.0;
        g.fill = GridBagConstraints.NONE;
        panel.add(field, g);
        g.fill = GridBagConstraints.HORIZONTAL;
        g.gridy = row + 1;
        g.insets = new Insets(0, 6, 8, 6);
        panel.add(help(explanation), g);
        g.insets = new Insets(3, 6, 3, 6);
        field.setToolTipText("<html><div style='width:360px'>" + explanation + "</div></html>");
        return row + 2;
    }

    /**
     * A check box over the whole width with its explanation below it
     */
    private static int check(JPanel panel, GridBagConstraints g, int row, JCheckBox box, String explanation) {
        g.gridx = 0;
        g.gridy = row;
        g.gridwidth = 2;
        g.weightx = 1.0;
        panel.add(box, g);
        g.gridy = row + 1;
        g.insets = new Insets(0, 30, 8, 6);
        panel.add(help(explanation), g);
        g.insets = new Insets(3, 6, 3, 6);
        g.gridwidth = 1;
        return row + 2;
    }

    private static void fillRatesTable(DefaultTableModel model, DatevConfig config, Set<BigDecimal> rates) {
        model.setRowCount(0);
        Set<BigDecimal> all = new TreeSet<BigDecimal>(JournalCalc.RATE_ORDER);
        all.addAll(rates);
        all.addAll(config.revenueAccounts.keySet());
        all.addAll(config.expenseAccounts.keySet());
        all.addAll(config.buKeysIncome.keySet());
        all.addAll(config.buKeysExpense.keySet());
        for (BigDecimal r : all) {
            if (r == null) {
                continue;
            }
            model.addRow(new Object[]{
                r.toPlainString(),
                str(config.revenueAccounts.get(r)),
                str(config.expenseAccounts.get(r)),
                str(config.buKeysIncome.get(r)),
                str(config.buKeysExpense.get(r))
            });
        }
    }

    private static String selectedKontenrahmen(JComboBox<String> combo) {
        int idx = combo.getSelectedIndex();
        if (idx == 0) {
            return "03";
        }
        if (idx == 1) {
            return "04";
        }
        return "";
    }

    private static List<Account> loadAccounts() {
        return DatevConfig.dbAccounts();
    }

    private static Set<BigDecimal> loadTaxRates() {
        return DatevConfig.dbTaxRates();
    }

    private static int parseInt(String s, int fallback) {
        try {
            return Integer.parseInt(s == null ? "" : s.trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private static String str(Object o) {
        return o == null ? "" : String.valueOf(o).trim();
    }
}
