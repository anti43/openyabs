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
        final JComboBox<String> kontenrahmen = new JComboBox<String>(new String[]{"03 (SKR03)", "04 (SKR04)", ""});
        if ("04".equals(config.kontenrahmen)) {
            kontenrahmen.setSelectedIndex(1);
        } else if ("03".equals(config.kontenrahmen)) {
            kontenrahmen.setSelectedIndex(0);
        } else {
            kontenrahmen.setSelectedIndex(2);
        }
        final JTextField bezeichnung = new JTextField(config.bezeichnung, 24);
        final JTextField diktat = new JTextField(config.diktatKuerzel, 6);
        final JCheckBox festschreibung = new JCheckBox("Stapel beim Import festschreiben (Standard: aus)", config.festschreibung);
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
                "DATEV-Automatikkonten (AM/AV) ohne BU-Schlüssel exportieren (z. B. SKR03 8400/8300/3400/3300)",
                config.useAutomaticAccounts);
        final JCheckBox leistungsdatumExportieren = new JCheckBox(
                "Leistungsdatum / Steuerperiode (Spalten 115–116) exportieren (§ 14 Abs. 4 Nr. 6 UStG)",
                config.leistungsdatumExportieren);

        final DefaultTableModel ratesModel = new DefaultTableModel(
                new String[]{"Steuersatz (%)", "Erlöskonto", "Aufwandskonto", "BU Einnahmen", "BU Ausgaben"}, 0) {
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
        ratesScroll.setPreferredSize(new Dimension(560, 150));

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(4, 6, 4, 6);
        g.anchor = GridBagConstraints.WEST;
        g.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;
        addRow(form, g, row++, "Beraternummer (1001–9999999):", berater);
        addRow(form, g, row++, "Mandantennummer (1–99999):", mandant);
        addRow(form, g, row++, "Wirtschaftsjahresbeginn (JJJJMMTT):", wjBeginn);
        addRow(form, g, row++, "Sachkontenlänge (4–8):", sachkontenLaenge);
        addRow(form, g, row++, "Kontenrahmen:", kontenrahmen);
        addRow(form, g, row++, "Bezeichnung des Stapels (max. 30):", bezeichnung);
        addRow(form, g, row++, "Diktatkürzel (optional, 1–2 Großbuchstaben):", diktat);
        addRow(form, g, row++, "Belegdatum:", belegdatumMode);
        addRow(form, g, row++, "Geldkonto Einnahmen (z. B. 1200 / 1800):", moneyIncome);
        addRow(form, g, row++, "Geldkonto Ausgaben (z. B. 1200 / 1800):", moneyExpense);
        addRow(form, g, row++, "Kassenkonto Barverkauf/POS (optional):", cashAccount);
        addRow(form, g, row++, "EU-Lieferkonto 0 % (USt-Kategorie K, z. B. 8125 / 4125):", euAccount);
        addRow(form, g, row++, "EU-Leistungen, Reverse Charge (Kategorie AE, z. B. 8336 / 4336):", euServiceAccount);
        addRow(form, g, row++, "§ 13b UStG im Inland (Kategorie AE ohne EU-USt-IdNr., z. B. 8337 / 4337):", reverseChargeAccount);
        addRow(form, g, row++, "Ausfuhr Drittland (Kategorie G, z. B. 8120 / 4120):", exportAccount);
        addRow(form, g, row++, "Belegpaket: Rechnungsordner in Belege online (Standard: Ausgangsrechnungen):", invoiceFolder);

        g.gridx = 0;
        g.gridy = row++;
        g.gridwidth = 2;
        form.add(festschreibung, g);

        g.gridy = row++;
        form.add(automaticAccounts, g);

        g.gridy = row++;
        form.add(leistungsdatumExportieren, g);

        g.gridy = row++;
        form.add(new JLabel("Konten & BU-Schlüssel je Steuersatz (Fallback, wenn das Belegkonto nicht passt):"), g);

        g.gridy = row++;
        g.weightx = 1.0;
        g.weighty = 1.0;
        g.fill = GridBagConstraints.BOTH;
        form.add(ratesScroll, g);

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
            if (yabs != null) {
                yabs.notify("DATEV-Einstellungen gespeichert.");
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
