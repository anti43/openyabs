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
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.Scrollable;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import mpv5.db.objects.Account;
import mpv5.handler.JournalCalc;
import mpv5.logging.Log;
import mpv5.plugin.api.ExtensionContext;
import mpv5.ui.modern.Card;
import mpv5.ui.modern.ModernTheme;
import mpv5.ui.modern.PrimaryButton;

/**
 * Control panel settings page for the DATEV export plugin.
 * Keeps standard SKR03/SKR04 configuration effortless (selecting the chart in
 * the dropdown updates standard DATEV accounts automatically) while tucking
 * manual account overrides into a collapsible advanced section.
 */
final class DatevSettingsPage {

    private static final int CONTENT_WIDTH = 760;

    private DatevSettingsPage() {
    }

    static JComponent create(final ExtensionContext yabs) {
        final List<Account> accounts = loadAccounts();
        final Set<BigDecimal> taxRates = loadTaxRates();
        final String detectedChart = DatevConfig.detectKontenrahmen(accounts);
        // Same defaults the export uses when settings were never saved
        final DatevConfig config = DatevConfig.loadWithDefaults(null);

        final JTextField berater = new JTextField(config.beraterNummer > 0 ? String.valueOf(config.beraterNummer) : "", 12);
        final JTextField mandant = new JTextField(config.mandantenNummer > 0 ? String.valueOf(config.mandantenNummer) : "", 12);
        final JTextField wjBeginn = new JTextField(config.wjBeginn, 12);
        final JSpinner sachkontenLaenge = new JSpinner(new SpinnerNumberModel(
                Math.max(4, Math.min(8, config.sachkontenLaenge)), 4, 8, 1));
        final JComboBox<String> kontenrahmen = new JComboBox<String>(new String[]{
            "03 (SKR03)", "04 (SKR04)", "Anderer / eigener Kontenrahmen"
        });
        if ("04".equals(config.kontenrahmen)) {
            kontenrahmen.setSelectedIndex(1);
        } else if ("03".equals(config.kontenrahmen)) {
            kontenrahmen.setSelectedIndex(0);
        } else {
            kontenrahmen.setSelectedIndex(2);
        }
        final JTextField bezeichnung = new JTextField(config.bezeichnung, 24);
        final JTextField diktat = new JTextField(config.diktatKuerzel, 6);
        final JCheckBox festschreibung = new JCheckBox("Stapel beim Import in DATEV festschreiben (GoBD)", config.festschreibung);
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
        final JTextField customAutomatic = new JTextField(String.join(", ", config.customAutomaticAccounts), 24);

        final DefaultTableModel ratesModel = new DefaultTableModel(
                new String[]{"Steuersatz (%)", "Erlöskonto (Einnahmen)", "Aufwandskonto (Ausgaben)", "BU Einnahmen", "BU Ausgaben"}, 0) {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean isCellEditable(int row, int col) {
                return col > 0;
            }
        };
        fillRatesTable(ratesModel, config, taxRates);

        final JTable ratesTable = new JTable(ratesModel);
        ModernTheme.style(ratesTable);
        ratesTable.setRowHeight(ModernTheme.px(26));
        JScrollPane ratesScroll = new JScrollPane(ratesTable);
        ratesScroll.setPreferredSize(new Dimension(ModernTheme.px(640), ModernTheme.px(132)));

        // Tooltips with detailed context
        tip(berater, "Nummer der Steuerberatung bei DATEV (1001–9999999). Nur für Buchungsstapel und Stammdaten (CSV) nötig – nicht für das Belegpaket.");
        tip(mandant, "Ihre Mandantennummer bei der Kanzlei (1–99999). Nur für Buchungsstapel und Stammdaten (CSV) nötig – nicht für das Belegpaket.");
        tip(wjBeginn, "Erster Tag des Wirtschaftsjahres als JJJJMMTT (z. B. 20260101). DATEV leitet das Jahr jedes Belegdatums (TTMM) aus diesem Feld ab.");
        tip(sachkontenLaenge, "Stellenzahl der Sachkonten im DATEV-Mandanten, meist 4 (z. B. 8400, 1200). Personenkonten haben eine Stelle mehr (10000–99999).");
        tip(kontenrahmen, "SKR03: Erlöse 8xxx, Bank 1200 · SKR04: Erlöse 4xxx, Bank 1800. Beim Umschalten werden die DATEV-Standardkonten automatisch angepasst.");
        tip(bezeichnung, "Name des Buchungsstapels in DATEV (max. 30 Zeichen, z. B. „Rechnungsausgang 03/2026“).");
        tip(diktat, "Kürzel der bearbeitenden Person: leer, 2 oder 4 Großbuchstaben (z. B. MM). Ein einzelner Buchstabe wird von DATEV nicht akzeptiert.");
        tip(festschreibung, "Festgeschriebene Buchungen kann die Kanzlei nicht mehr ändern. Ausgeschaltet lassen, damit die Kanzlei vor dem Festschreiben prüfen kann.");
        tip(belegdatumMode, "Zahlungsdatum (EÜR/Ist): bezahlte Belege nach Zahlungsdatum. Belegdatum: bezahlte Belege nach Rechnungsdatum. Für offene Posten (Soll-Versteuerung) das Belegpaket nutzen.");
        tip(leistungsdatumExportieren, "Exportiert bei abweichendem Leistungsdatum die Felder 115 (Leistungsdatum) und 116 (Steuerperiode), wichtig bei Monatswechsel (§ 14 Abs. 4 Nr. 6 UStG).");
        tip(moneyIncome, "Gegenkonto für Zahlungseingänge (Soll). Standard: SKR03 1200 · SKR04 1800 „Bank“.");
        tip(moneyExpense, "Gegenkonto für Ausgaben und Gutschriften (Haben). Standard: SKR03 1200 · SKR04 1800 „Bank“.");
        tip(cashAccount, "Optionales Konto nur für Barverkäufe (POS). Standard: SKR03 1000 · SKR04 1600 „Kasse“. Leer = Geldkonto Einnahmen.");
        tip(euAccount, "Innergemeinschaftliche Lieferung an Unternehmen mit USt-IdNr. (§ 4 Nr. 1b UStG, Kategorie K). Standard: SKR03 8125 · SKR04 4125 (BU 11, UStVA Kz 41).");
        tip(euServiceAccount, "Sonstige Leistung an EU-Unternehmen mit USt-IdNr. (Reverse Charge, § 18b UStG, Kategorie AE). Standard: SKR03 8336 · SKR04 4336 (BU 47, UStVA Kz 21).");
        tip(reverseChargeAccount, "Inländische Leistung mit Steuerschuldumkehr (§ 13b UStG, Kategorie AE ohne EU-USt-IdNr.). Standard: SKR03 8337 · SKR04 4337 (BU 46, UStVA Kz 60).");
        tip(exportAccount, "Ausfuhrlieferung in Drittland außerhalb der EU (§ 4 Nr. 1a UStG, Kategorie G). Standard: SKR03 8120 · SKR04 4120 (BU 173, UStVA Kz 43). Nicht für Inlandsumsätze.");
        tip(invoiceFolder, "Ordnername in DATEV Belege online (DATEV-Standard: „Ausgangsrechnungen“).");
        tip(automaticAccounts, "Automatikkonten (z. B. SKR03 8400/3400, SKR04 4400/5400) berechnen USt/VSt in DATEV selbst – ein zusätzlicher BU-Schlüssel verursacht Importfehler.");
        tip(customAutomatic, "Weitere in Ihrem DATEV-Mandanten als Automatikkonto eingerichtete Konten, mit Komma getrennt (z. B. 8401, 8402).");

        // Dynamic banner & controls
        final JPanel statusBanner = new JPanel(new BorderLayout(ModernTheme.px(10), ModernTheme.px(4)));
        final JLabel statusLabel = new JLabel();
        final JButton fixMixedBtn = new JButton();
        fixMixedBtn.setVisible(false);
        JPanel fixBtnWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        fixBtnWrap.setOpaque(false);
        fixBtnWrap.add(fixMixedBtn);
        statusBanner.add(statusLabel, BorderLayout.CENTER);
        statusBanner.add(fixBtnWrap, BorderLayout.EAST);

        final JLabel activeStandardSummary = help("");
        final JLabel ratesHintLabel = help("");
        final JButton resetToStandardBtn = new JButton();
        resetToStandardBtn.setVisible(false);

        final AtomicBoolean updatingUi = new AtomicBoolean(false);
        final AtomicReference<String> previousChart = new AtomicReference<String>(selectedKontenrahmen(kontenrahmen));
        final boolean initiallyCustomized = config.hasCustomAccountOverrides(taxRates)
                || !config.chartConsistencyWarnings(false).isEmpty();
        final AtomicBoolean advancedExpanded = new AtomicBoolean(initiallyCustomized);

        // Collapsible advanced accounts panel
        final JButton advancedToggle = new JButton();
        advancedToggle.setHorizontalAlignment(SwingConstants.LEFT);
        advancedToggle.setBorderPainted(false);
        advancedToggle.setContentAreaFilled(false);
        advancedToggle.setFocusPainted(false);
        advancedToggle.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        advancedToggle.setForeground(ModernTheme.accent());
        advancedToggle.setFont(ModernTheme.font(0, Font.BOLD));
        advancedToggle.setBorder(BorderFactory.createEmptyBorder(ModernTheme.px(4), 0, ModernTheme.px(2), 0));

        final Card advancedContent = new Card(null).flat();
        advancedContent.setVisible(initiallyCustomized);

        // Helper: read current form values into a snapshot DatevConfig
        final java.util.function.Supplier<DatevConfig> readFormToConfig = () -> {
            DatevConfig snap = new DatevConfig();
            snap.smallBusiness = config.smallBusiness;
            snap.beraterNummer = parseInt(berater.getText(), 0);
            snap.mandantenNummer = parseInt(mandant.getText(), 0);
            snap.wjBeginn = wjBeginn.getText().trim();
            snap.sachkontenLaenge = ((Number) sachkontenLaenge.getValue()).intValue();
            snap.kontenrahmen = selectedKontenrahmen(kontenrahmen);
            snap.bezeichnung = bezeichnung.getText().trim();
            snap.diktatKuerzel = diktat.getText().trim().toUpperCase();
            snap.festschreibung = festschreibung.isSelected();
            snap.belegdatumMode = belegdatumMode.getSelectedIndex() == 1
                    ? DatevConfig.DateMode.DOCUMENT_DATE
                    : DatevConfig.DateMode.PAYMENT_DATE;
            snap.moneyAccountIncome = moneyIncome.getText().trim();
            snap.moneyAccountExpense = moneyExpense.getText().trim();
            snap.cashAccount = cashAccount.getText().trim();
            snap.euDeliveryAccount = euAccount.getText().trim();
            snap.euServiceAccount = euServiceAccount.getText().trim();
            snap.reverseChargeAccount = reverseChargeAccount.getText().trim();
            snap.exportAccount = exportAccount.getText().trim();
            snap.invoiceFolder = invoiceFolder.getText().trim();
            snap.useAutomaticAccounts = automaticAccounts.isSelected();
            snap.customAutomaticAccounts.clear();
            for (String part : customAutomatic.getText().split("[,;\\s]+")) {
                String d = DatevFormat.digitsOnly(part);
                if (!d.isEmpty()) {
                    snap.customAutomaticAccounts.add(d);
                }
            }
            snap.leistungsdatumExportieren = leistungsdatumExportieren.isSelected();
            snap.revenueAccounts.clear();
            snap.expenseAccounts.clear();
            snap.buKeysIncome.clear();
            snap.buKeysExpense.clear();
            for (int r = 0; r < ratesModel.getRowCount(); r++) {
                try {
                    BigDecimal rate = JournalCalc.rateKey(new BigDecimal(String.valueOf(ratesModel.getValueAt(r, 0)).replace(',', '.').trim()));
                    snap.revenueAccounts.put(rate, str(ratesModel.getValueAt(r, 1)));
                    snap.expenseAccounts.put(rate, str(ratesModel.getValueAt(r, 2)));
                    snap.buKeysIncome.put(rate, str(ratesModel.getValueAt(r, 3)));
                    snap.buKeysExpense.put(rate, str(ratesModel.getValueAt(r, 4)));
                } catch (Exception ex) {
                    Log.Debug(DatevSettingsPage.class, ex.getMessage());
                }
            }
            return snap;
        };

        // Helper: refresh status banner, toggle label, and dynamic summaries
        final Runnable refreshStatus = () -> {
            if (updatingUi.get()) {
                return;
            }
            DatevConfig snap = readFormToConfig.get();
            String chart = snap.kontenrahmen;
            String chartLabel = "04".equals(chart) ? "SKR04" : ("03".equals(chart) ? "SKR03" : "Eigener Kontenrahmen");
            boolean customOverrides = snap.hasCustomAccountOverrides(taxRates);

            String arrow = advancedExpanded.get() ? "▾ " : "▸ ";
            advancedToggle.setText(arrow + "Erweiterte Konten- & BU-Schlüssel-Zuordnung"
                    + (customOverrides ? " (eigene Abweichungen aktiv)" : " (DATEV-Standard – nur bei Sonderwünschen nötig)"));

            boolean canReset = ("03".equals(chart) || "04".equals(chart)) && customOverrides;
            resetToStandardBtn.setText("Auf " + chartLabel + "-Standard zurücksetzen");
            resetToStandardBtn.setVisible(canReset);

            if ("04".equals(chart)) {
                String zeroRev = config.smallBusiness ? "4185" : "4100";
                activeStandardSummary.setText(wrapHtml(
                        (customOverrides ? "<b>Aktive Basis: SKR04 (mit eigenen Abweichungen)</b> · "
                                : "<b>DATEV-Standard für SKR04 aktiv:</b> ")
                        + "Erlöse 19/7/0 %: <b>4400 / 4300 / " + zeroRev + "</b> · "
                        + "Aufwand: <b>5400 / 5300 / 6300</b> · "
                        + "Bank: <b>1800</b> · EU/Export: <b>4125 / 4336 / 4337 / 4120</b>."));
                ratesHintLabel.setText(wrapHtml("DATEV-Standard <b>SKR04</b>: "
                        + "19 % → 4400 / 5400 (BU 3 / 9) · 7 % → 4300 / 5300 (BU 2 / 8) · 0 % → "
                        + zeroRev + " / 6300 ohne BU. BU-Schlüssel werden nur auf Konten ohne Automatik exportiert."));
            } else if ("03".equals(chart)) {
                String zeroRev = config.smallBusiness ? "8195" : "8100";
                activeStandardSummary.setText(wrapHtml(
                        (customOverrides ? "<b>Aktive Basis: SKR03 (mit eigenen Abweichungen)</b> · "
                                : "<b>DATEV-Standard für SKR03 aktiv:</b> ")
                        + "Erlöse 19/7/0 %: <b>8400 / 8300 / " + zeroRev + "</b> · "
                        + "Aufwand: <b>3400 / 3300 / 4900</b> · "
                        + "Bank: <b>1200</b> · EU/Export: <b>8125 / 8336 / 8337 / 8120</b>."));
                ratesHintLabel.setText(wrapHtml("DATEV-Standard <b>SKR03</b>: "
                        + "19 % → 8400 / 3400 (BU 3 / 9) · 7 % → 8300 / 3300 (BU 2 / 8) · 0 % → "
                        + zeroRev + " / 4900 ohne BU. BU-Schlüssel werden nur auf Konten ohne Automatik exportiert."));
            } else {
                activeStandardSummary.setText(wrapHtml(
                        "<b>Eigener Kontenrahmen:</b> Bitte prüfen Sie die Konten und BU-Schlüssel im erweiterten Bereich unten."));
                ratesHintLabel.setText(wrapHtml("Tragen Sie hier die Konten und BU-Schlüssel Ihres Kontenrahmens ein."));
            }

            List<String> chartWarnings = snap.chartConsistencyWarnings(false);
            String dbMismatch = snap.detectChartMismatchWithAccounts(accounts);
            List<String> csvMissing = new ArrayList<String>();
            if (snap.beraterNummer < 1001 || snap.beraterNummer > 9999999) {
                csvMissing.add("Beraternummer");
            }
            if (snap.mandantenNummer < 1 || snap.mandantenNummer > 99999) {
                csvMissing.add("Mandantennummer");
            }
            if (!snap.wjBeginn.matches("20\\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\\d|3[01])")) {
                csvMissing.add("Wirtschaftsjahresbeginn (JJJJMMTT)");
            }

            if (!chartWarnings.isEmpty()) {
                styleBanner(statusBanner, statusLabel, BannerKind.ERROR);
                StringBuilder html = new StringBuilder("<b>Konten-Konflikt erkannt (SKR03/SKR04):</b><br>");
                for (String w : chartWarnings) {
                    html.append("• ").append(escapeHtml(w)).append("<br>");
                }
                if (dbMismatch != null) {
                    html.append("<span style='opacity:0.85'>").append(escapeHtml(dbMismatch)).append("</span>");
                }
                statusLabel.setText(wrapHtml(html.toString(), 520));
                String targetChart = "04".equals(chart) ? "SKR04" : "SKR03";
                fixMixedBtn.setText("Alle Konten auf " + targetChart + " umstellen");
                fixMixedBtn.setVisible(true);
            } else if (dbMismatch != null || !csvMissing.isEmpty()) {
                styleBanner(statusBanner, statusLabel, BannerKind.INFO);
                StringBuilder html = new StringBuilder();
                if (!csvMissing.isEmpty()) {
                    html.append("<b>Belegpaket einsatzbereit</b> (").append(chartLabel).append(") · ")
                            .append("Für <b>Buchungsstapel / Stammdaten (CSV)</b> fehlt noch: ")
                            .append(escapeHtml(String.join(", ", csvMissing))).append(".");
                } else {
                    html.append("<b>Konten konsistent (").append(chartLabel).append(")</b> – bereit für Belegpaket und Buchungsstapel.");
                }
                if (dbMismatch != null) {
                    html.append("<br>Hinweis: ").append(escapeHtml(dbMismatch));
                }
                statusLabel.setText(wrapHtml(html.toString(), 680));
                fixMixedBtn.setVisible(false);
            } else {
                styleBanner(statusBanner, statusLabel, BannerKind.OK);
                statusLabel.setText(wrapHtml("<b>Alles konsistent (" + chartLabel + ")</b> – bereit für DATEV-Belegpaket (Unternehmen online) "
                        + "sowie Buchungsstapel &amp; Stammdaten (EXTF-CSV).", 680));
                fixMixedBtn.setVisible(false);
            }
            statusBanner.revalidate();
            statusBanner.repaint();
        };

        // Helper: apply DATEV standard accounts for the chosen SKR
        final Runnable applyChartDefaults = () -> {
            if (ratesTable.isEditing() && ratesTable.getCellEditor() != null) {
                ratesTable.getCellEditor().stopCellEditing();
            }
            updatingUi.set(true);
            try {
                String chosen = selectedKontenrahmen(kontenrahmen);
                if (DatevConfig.isBlank(chosen)) {
                    chosen = !DatevConfig.isBlank(detectedChart) ? detectedChart : "03";
                    kontenrahmen.setSelectedIndex("04".equals(chosen) ? 1 : 0);
                }
                config.kontenrahmen = chosen;
                config.useAutomaticAccounts = true;
                config.customAutomaticAccounts.clear();
                config.prefillFromAccounts(accounts, taxRates, true);
                moneyIncome.setText(config.moneyAccountIncome);
                moneyExpense.setText(config.moneyAccountExpense);
                cashAccount.setText(config.cashAccount);
                euAccount.setText(config.euDeliveryAccount);
                euServiceAccount.setText(config.euServiceAccount);
                reverseChargeAccount.setText(config.reverseChargeAccount);
                exportAccount.setText(config.exportAccount);
                automaticAccounts.setSelected(true);
                customAutomatic.setText("");
                fillRatesTable(ratesModel, config, taxRates);
                previousChart.set(chosen);
            } finally {
                updatingUi.set(false);
            }
            refreshStatus.run();
        };

        resetToStandardBtn.addActionListener(e -> applyChartDefaults.run());
        fixMixedBtn.addActionListener(e -> applyChartDefaults.run());

        advancedToggle.addActionListener(e -> {
            boolean next = !advancedExpanded.get();
            advancedExpanded.set(next);
            advancedContent.setVisible(next);
            refreshStatus.run();
            advancedContent.revalidate();
            if (advancedContent.getParent() != null) {
                advancedContent.getParent().revalidate();
                advancedContent.getParent().repaint();
            }
        });

        // Dropdown selection automatically switches standard accounts (only asks if user has custom overrides)
        kontenrahmen.addActionListener(e -> {
            if (updatingUi.get()) {
                return;
            }
            String next = selectedKontenrahmen(kontenrahmen);
            String prev = previousChart.get();
            previousChart.set(next);
            if ("03".equals(next) || "04".equals(next)) {
                if (!next.equals(prev)) {
                    DatevConfig prevSnap = readFormToConfig.get();
                    prevSnap.kontenrahmen = prev;
                    boolean hadCustom = prevSnap.hasCustomAccountOverrides(taxRates);
                    if (!hadCustom) {
                        // Standard accounts: switch seamlessly without nagging
                        applyChartDefaults.run();
                        return;
                    }
                    if (kontenrahmen.isShowing()) {
                        String label = "04".equals(next) ? "SKR04" : "SKR03";
                        int answer = JOptionPane.showConfirmDialog(
                                SwingUtilities.getWindowAncestor(kontenrahmen),
                                "Sie haben eigene Konten eingetragen.\n\n"
                                        + "Sollen alle Konten auf die DATEV-Standardkonten für " + label + " umgestellt werden?\n"
                                        + "(Verhindert gemischte SKR03/SKR04-Konten und falsche BU-Schlüssel.)",
                                "Konten auf " + label + " umstellen?",
                                JOptionPane.YES_NO_OPTION,
                                JOptionPane.QUESTION_MESSAGE);
                        if (answer == JOptionPane.YES_OPTION) {
                            applyChartDefaults.run();
                            return;
                        }
                    }
                }
            } else {
                // Custom chart selected: make sure advanced fields are visible
                if (!advancedExpanded.get()) {
                    advancedExpanded.set(true);
                    advancedContent.setVisible(true);
                }
            }
            refreshStatus.run();
        });

        // Live validation listeners
        DocumentListener liveDocListener = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                refreshStatus.run();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                refreshStatus.run();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                refreshStatus.run();
            }
        };
        for (JTextField tf : new JTextField[]{berater, mandant, wjBeginn, moneyIncome, moneyExpense, cashAccount,
                euAccount, euServiceAccount, reverseChargeAccount, exportAccount, customAutomatic}) {
            tf.getDocument().addDocumentListener(liveDocListener);
        }
        automaticAccounts.addActionListener(e -> refreshStatus.run());
        ratesModel.addTableModelListener(e -> refreshStatus.run());

        // ==================== CARD 1: Schnellstart & Status ====================
        Card overviewCard = new Card("Übersicht: Was brauchen Sie für welchen DATEV-Export?");
        JPanel compareGrid = new JPanel(new GridLayout(1, 2, ModernTheme.px(10), 0));
        compareGrid.setOpaque(false);
        compareGrid.add(infoTile(
                "Belegpaket (DATEV Unternehmen online)",
                "<b>Keine Berater- oder Mandantennummer nötig</b> – DATEV empfiehlt, diese wegzulassen "
                        + "(der Mandant wird beim Hochladen in DATEV Belegtransfer gewählt).<br>"
                        + "<b>Benötigt:</b> nur <i>Kontenrahmen</i> + <i>Rechnungsordner</i>."));
        compareGrid.add(infoTile(
                "Buchungsstapel & Stammdaten (EXTF-CSV)",
                "Für den direkten Import in <b>DATEV Kanzlei-Rechnungswesen</b>.<br>"
                        + "<b>Benötigt:</b> <i>Kontenrahmen</i> sowie <i>Berater- &amp; Mandantennummer</i> "
                        + "und <i>Wirtschaftsjahresbeginn</i>."));
        overviewCard.addFull(compareGrid);
        overviewCard.addFull(statusBanner);

        // ==================== CARD 2: Kontenrahmen ====================
        Card accountsCard = new Card("1. Kontenrahmen (SKR03 / SKR04)");

        JPanel chartDecision = infoTile(
                "Wann SKR03 und wann SKR04 wählen?",
                "Entscheidend ist allein der Kontenrahmen Ihres <b>DATEV-Mandanten bei der Steuerberatung</b> "
                        + "(alle DATEV-Standardkonten und BU-Schlüssel stellt YaBS automatisch passend ein).<br>"
                        + "• <b>Schnell-Check (BWA / Summen- und Saldenliste):</b> "
                        + "Erlöse <b>8xxx</b> &amp; Bank <b>1200</b> → <b>SKR03</b> · "
                        + "Erlöse <b>4xxx</b> &amp; Bank <b>1800</b> → <b>SKR04</b>.<br>"
                        + "• <b>In YaBS (Einstellungen › Konten) erkannt:</b> "
                        + (DatevConfig.isBlank(detectedChart)
                                ? "kein SKR03/SKR04-Kontenrahmen importiert"
                                : "<b>SKR" + detectedChart + "</b>")
                        + " · <b>Kleinunternehmer (§ 19 UStG):</b> "
                        + (config.smallBusiness ? "eingeschaltet (Firmendaten)" : "ausgeschaltet (Firmendaten)") + ".");
        accountsCard.addFull(chartDecision);
        accountsCard.row(
                Card.field("Kontenrahmen (DATEV-Mandant)", kontenrahmen, 1.2),
                Card.field("Sachkontenlänge (meist 4)", sachkontenLaenge, 0.5));
        accountsCard.addFull(activeStandardSummary);
        accountsCard.addFull(advancedToggle);

        // Populate collapsible advanced overrides panel
        advancedContent.section("Konten & BU-Schlüssel je Steuersatz");
        advancedContent.addFull(ratesHintLabel);
        advancedContent.addFull(ratesScroll);

        advancedContent.section("Umsätze ohne Umsatzsteuer (Kategorien K, AE, G aus Einstellungen › Steuersätze)");
        advancedContent.row(
                Card.field("EU-Lieferung (K · § 4 Nr. 1b)", euAccount, 1.0),
                Card.field("EU-Leistung (AE · § 18b)", euServiceAccount, 1.0));
        advancedContent.row(
                Card.field("§ 13b UStG Inland (AE)", reverseChargeAccount, 1.0),
                Card.field("Ausfuhr Drittland (G · § 4 Nr. 1a)", exportAccount, 1.0));

        advancedContent.section("Geldkonten (für Buchungsstapel)");
        advancedContent.row(
                Card.field("Geldkonto Einnahmen (Bank)", moneyIncome, 1.0),
                Card.field("Geldkonto Ausgaben (Bank)", moneyExpense, 1.0),
                Card.field("Kassenkonto Barverkauf (optional)", cashAccount, 1.0));

        advancedContent.section("Automatikkonten (AM / AV)");
        advancedContent.addFull(automaticAccounts);
        advancedContent.addFull(help("Auf DATEV-Automatikkonten (z. B. SKR03 8400/3400, SKR04 4400/5400) berechnet DATEV die Steuer selbst – "
                + "dort bleibt der BU-Schlüssel leer."));
        advancedContent.row(Card.field("Eigene Automatikkonten (optional, kommagetrennt, z. B. 8401, 8402)", customAutomatic, 1.0));

        JPanel resetRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        resetRow.setOpaque(false);
        resetRow.add(resetToStandardBtn);
        advancedContent.addFull(resetRow);

        accountsCard.addFull(advancedContent);

        // ==================== CARD 3: Belegpaket ====================
        Card packageCard = new Card("2. Belegpaket (DATEV Unternehmen online / Belegtransfer)");
        packageCard.addFull(help("Für das Belegpaket sind <b>keine</b> Berater- und Mandantennummer erforderlich: "
                + "DATEV empfiehlt für die XML-Schnittstelle online (v6.0), beide wegzulassen, weil Sie den Mandanten "
                + "beim Hochladen in DATEV Belegtransfer wählen (abweichende Nummern führen sonst zum Abbruch)."));
        packageCard.row(Card.field("Rechnungsordner in Belege online (Standard: Ausgangsrechnungen)", invoiceFolder, 1.0));

        // ==================== CARD 4: Buchungsstapel & Stammdaten (CSV) ====================
        Card csvCard = new Card("3. Buchungsstapel & Stammdaten (nur für EXTF-CSV)");
        csvCard.addFull(help("Diese Felder werden <b>nur</b> für den CSV-Export nach DATEV Kanzlei-Rechnungswesen benötigt – nicht für das Belegpaket."));
        csvCard.row(
                Card.field("Beraternummer (1001–9999999)", berater, 1.0),
                Card.field("Mandantennummer (1–99999)", mandant, 1.0));
        csvCard.row(
                Card.field("Wirtschaftsjahresbeginn (JJJJMMTT, z. B. 20260101)", wjBeginn, 1.0),
                Card.field("Belegdatum im Stapel", belegdatumMode, 1.2));
        csvCard.row(
                Card.field("Bezeichnung des Stapels (max. 30 Zeichen)", bezeichnung, 1.4),
                Card.field("Diktatkürzel (leer, 2 oder 4 Buchstaben)", diktat, 0.8));
        csvCard.addFull(leistungsdatumExportieren);
        csvCard.addFull(festschreibung);
        csvCard.addFull(help("Referenz: DATEV-Steuerschlüssel-Tabelle 2026 für SKR03/SKR04 (DATEV Dok.-Nr. 0907048) und developer.datev.de. "
                + "Detailhinweise erscheinen beim Überfahren eines Feldes mit der Maus."));

        // ==================== Layout & Actions ====================
        JPanel form = new ScrollableStackPanel();
        form.setBackground(ModernTheme.background());
        form.setBorder(BorderFactory.createEmptyBorder(ModernTheme.px(12), ModernTheme.px(14), ModernTheme.px(12), ModernTheme.px(14)));
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0;
        g.weightx = 1.0;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.anchor = GridBagConstraints.NORTH;
        int row = 0;
        g.gridy = row++;
        g.insets = new Insets(0, 0, ModernTheme.px(12), 0);
        form.add(overviewCard, g);
        g.gridy = row++;
        form.add(accountsCard, g);
        g.gridy = row++;
        form.add(packageCard, g);
        g.gridy = row++;
        g.insets = new Insets(0, 0, 0, 0);
        form.add(csvCard, g);

        PrimaryButton saveBtn = new PrimaryButton("Speichern", null);
        saveBtn.addActionListener(e -> {
            if (ratesTable.isEditing() && ratesTable.getCellEditor() != null) {
                ratesTable.getCellEditor().stopCellEditing();
            }
            DatevConfig snap = readFormToConfig.get();
            copyInto(snap, config);
            config.save();
            refreshStatus.run();

            List<String> problems = new ArrayList<String>();
            problems.addAll(config.chartConsistencyWarnings(false));
            String d = diktat.getText().trim();
            if (!d.isEmpty() && !DatevFormat.sanitizeDiktat(d).equals(d.toUpperCase())) {
                problems.add("Diktatkürzel: nur 2 oder 4 Großbuchstaben (exportiert wird „" + DatevFormat.sanitizeDiktat(d) + "“)");
            }
            List<String> csvOnly = new ArrayList<String>();
            if (!config.wjBeginn.matches("20\\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\\d|3[01])")) {
                csvOnly.add("Wirtschaftsjahresbeginn (JJJJMMTT)");
            }
            if (config.beraterNummer < 1001 || config.beraterNummer > 9999999) {
                csvOnly.add("Beraternummer (1001–9999999)");
            }
            if (config.mandantenNummer < 1 || config.mandantenNummer > 99999) {
                csvOnly.add("Mandantennummer (1–99999)");
            }

            if (yabs != null) {
                if (problems.isEmpty() && csvOnly.isEmpty()) {
                    yabs.notify("DATEV-Einstellungen gespeichert.");
                } else {
                    StringBuilder msg = new StringBuilder("DATEV-Einstellungen gespeichert.");
                    if (!problems.isEmpty()) {
                        msg.append(" Bitte prüfen: ").append(String.join("; ", problems));
                    }
                    if (!csvOnly.isEmpty()) {
                        msg.append(" Für Buchungsstapel/Stammdaten (CSV) noch offen: ")
                                .append(String.join(", ", csvOnly))
                                .append(" (für das Belegpaket nicht erforderlich).");
                    }
                    yabs.notify(msg.toString());
                }
            }
        });

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, ModernTheme.px(14), ModernTheme.px(8)));
        actions.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, ModernTheme.border()));
        actions.add(saveBtn);

        // Initial status calculation
        refreshStatus.run();

        JScrollPane outerScroll = new JScrollPane(form);
        outerScroll.setBorder(BorderFactory.createEmptyBorder());
        outerScroll.getVerticalScrollBar().setUnitIncrement(ModernTheme.px(16));

        JPanel root = new JPanel(new BorderLayout());
        root.add(outerScroll, BorderLayout.CENTER);
        root.add(actions, BorderLayout.SOUTH);
        return root;
    }

    private enum BannerKind {
        OK, INFO, ERROR
    }

    private static void styleBanner(JPanel banner, JLabel label, BannerKind kind) {
        Color base = ModernTheme.cardBackground();
        Color accent;
        switch (kind) {
            case ERROR:
                accent = ModernTheme.isDark() ? new Color(248, 113, 113) : new Color(185, 28, 28);
                break;
            case OK:
                accent = ModernTheme.isDark() ? new Color(74, 222, 128) : new Color(21, 128, 61);
                break;
            default:
                accent = ModernTheme.accent();
                break;
        }
        Color bg = ModernTheme.blend(accent, base, ModernTheme.isDark() ? 0.80f : 0.90f);
        Color border = ModernTheme.blend(accent, base, ModernTheme.isDark() ? 0.45f : 0.55f);
        banner.setOpaque(true);
        banner.setBackground(bg);
        banner.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(border, 1),
                BorderFactory.createEmptyBorder(ModernTheme.px(8), ModernTheme.px(10), ModernTheme.px(8), ModernTheme.px(10))));
        label.setForeground(ModernTheme.foreground());
        label.setFont(ModernTheme.font(-0.5f, Font.PLAIN));
    }

    private static JPanel infoTile(String title, String bodyHtml) {
        JPanel tile = new JPanel(new BorderLayout(0, ModernTheme.px(4)));
        tile.setOpaque(true);
        tile.setBackground(ModernTheme.blend(ModernTheme.background(), ModernTheme.cardBackground(), 0.45f));
        tile.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ModernTheme.border(), 1),
                BorderFactory.createEmptyBorder(ModernTheme.px(8), ModernTheme.px(10), ModernTheme.px(8), ModernTheme.px(10))));
        JLabel h = new JLabel(title);
        h.setFont(ModernTheme.font(0, Font.BOLD));
        h.setForeground(ModernTheme.foreground());
        JLabel body = new JLabel("<html><div style='width:" + ModernTheme.px(310) + "px'>" + bodyHtml + "</div></html>");
        body.setFont(ModernTheme.font(-1f, Font.PLAIN));
        body.setForeground(ModernTheme.muted());
        tile.add(h, BorderLayout.NORTH);
        tile.add(body, BorderLayout.CENTER);
        return tile;
    }

    private static void copyInto(DatevConfig src, DatevConfig dst) {
        dst.beraterNummer = src.beraterNummer;
        dst.mandantenNummer = src.mandantenNummer;
        dst.wjBeginn = src.wjBeginn;
        dst.sachkontenLaenge = src.sachkontenLaenge;
        dst.kontenrahmen = src.kontenrahmen;
        dst.bezeichnung = src.bezeichnung;
        dst.diktatKuerzel = src.diktatKuerzel;
        dst.festschreibung = src.festschreibung;
        dst.belegdatumMode = src.belegdatumMode;
        dst.moneyAccountIncome = src.moneyAccountIncome;
        dst.moneyAccountExpense = src.moneyAccountExpense;
        dst.cashAccount = src.cashAccount;
        dst.euDeliveryAccount = src.euDeliveryAccount;
        dst.euServiceAccount = src.euServiceAccount;
        dst.reverseChargeAccount = src.reverseChargeAccount;
        dst.exportAccount = src.exportAccount;
        dst.invoiceFolder = src.invoiceFolder;
        dst.useAutomaticAccounts = src.useAutomaticAccounts;
        dst.customAutomaticAccounts.clear();
        dst.customAutomaticAccounts.addAll(src.customAutomaticAccounts);
        dst.leistungsdatumExportieren = src.leistungsdatumExportieren;
        dst.revenueAccounts.clear();
        dst.revenueAccounts.putAll(src.revenueAccounts);
        dst.expenseAccounts.clear();
        dst.expenseAccounts.putAll(src.expenseAccounts);
        dst.buKeysIncome.clear();
        dst.buKeysIncome.putAll(src.buKeysIncome);
        dst.buKeysExpense.clear();
        dst.buKeysExpense.putAll(src.buKeysExpense);
    }

    /**
     * Vertical card stack that tracks the scroll pane viewport width so HTML
     * labels never force horizontal scrollbars while still reporting a sensible
     * preferred width for screenshot/pack layout.
     */
    private static final class ScrollableStackPanel extends JPanel implements Scrollable {

        private static final long serialVersionUID = 1L;

        ScrollableStackPanel() {
            super(new GridBagLayout());
        }

        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return new Dimension(ModernTheme.px(CONTENT_WIDTH), ModernTheme.px(640));
        }

        @Override
        public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
            return ModernTheme.px(24);
        }

        @Override
        public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
            return Math.max(ModernTheme.px(80), visibleRect.height - ModernTheme.px(24));
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true;
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            return false;
        }
    }

    private static void tip(JComponent c, String explanation) {
        c.setToolTipText("<html><div style='width:" + ModernTheme.px(360) + "px'>" + explanation + "</div></html>");
    }

    private static String wrapHtml(String html) {
        return wrapHtml(html, 660);
    }

    private static String wrapHtml(String html, int width) {
        return "<html><div style='width:" + ModernTheme.px(width) + "px'>" + html + "</div></html>";
    }

    private static String escapeHtml(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    /**
     * @param html
     * @return an explanation: muted, smaller, wrapped
     */
    static JLabel help(String html) {
        JLabel l = new JLabel(wrapHtml(html));
        l.setForeground(ModernTheme.muted());
        l.setFont(ModernTheme.font(-1f, Font.PLAIN));
        return l;
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
