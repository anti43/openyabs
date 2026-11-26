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
package org.openyabs.gmbh;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.DefaultTableModel;
import mpv5.handler.JournalLoader;
import mpv5.plugin.api.ExtensionContext;
import mpv5.utils.date.DateConverter;
import mpv5.utils.date.vTimeframe;
import mpv5.utils.export.EcSalesListExport;
import mpv5.utils.numberformat.FormatNumber;

/**
 * The tab "USt-Voranmeldung (Kontrolle)" of the journal: the fields of the
 * advance VAT return of a month, quarter or year
 */
final class UstvaPanel {

    private UstvaPanel() {
    }

    /**
     * the periods: 1-12 months, 13-16 quarters, 17 year
     */
    static Date[] period(int year, int index) {
        LocalDate start;
        LocalDate end;
        if (index < 12) {
            start = LocalDate.of(year, index + 1, 1);
            end = start.plusMonths(1).minusDays(1);
        } else if (index < 16) {
            start = LocalDate.of(year, (index - 12) * 3 + 1, 1);
            end = start.plusMonths(3).minusDays(1);
        } else {
            start = LocalDate.of(year, 1, 1);
            end = LocalDate.of(year, 12, 31);
        }
        return new Date[]{Date.from(start.atStartOfDay(ZoneId.systemDefault()).toInstant()),
            DateConverter.getEndOfDay(Date.from(end.atStartOfDay(ZoneId.systemDefault()).toInstant()))};
    }

    static JComponent create(final ExtensionContext yabs) {
        final JPanel root = new JPanel(new BorderLayout(0, 8));
        root.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        LocalDate last = LocalDate.now().minusMonths(1);
        final JSpinner year = new JSpinner(new SpinnerNumberModel(last.getYear(), 2000, 2100, 1));
        year.setEditor(new JSpinner.NumberEditor(year, "#"));
        String[] periods = new String[17];
        for (int i = 0; i < 12; i++) {
            periods[i] = yabs.tr("ustva.month." + (i + 1));
        }
        for (int i = 0; i < 4; i++) {
            periods[12 + i] = yabs.tr("ustva.quarter", i + 1);
        }
        periods[16] = yabs.tr("ustva.year");
        final JComboBox<String> period = new JComboBox<String>(periods);
        period.setSelectedIndex(last.getMonthValue() - 1);
        final JComboBox<String> basis = new JComboBox<String>(new String[]{yabs.tr("ustva.basis.accrual"), yabs.tr("ustva.basis.cash")});
        basis.setSelectedIndex("cash".equals(yabs.settings().get("ustva.basis", "accrual")) ? 1 : 0);
        final JButton compute = new JButton(yabs.tr("ustva.compute"));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        top.add(new JLabel(yabs.tr("ustva.label.year")));
        top.add(year);
        top.add(new JLabel(yabs.tr("ustva.label.period")));
        top.add(period);
        top.add(new JLabel(yabs.tr("ustva.label.basis")));
        top.add(basis);
        top.add(compute);

        final DefaultTableModel model = new DefaultTableModel(new String[]{
            yabs.tr("ustva.col.kz"), yabs.tr("ustva.col.field"), yabs.tr("ustva.col.base"), yabs.tr("ustva.col.tax"), yabs.tr("ustva.col.booked")}, 0) {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        final JTable table = new JTable(model);
        table.setRowHeight(Math.max(table.getRowHeight(), mpv5.ui.modern.ModernTheme.px(24)));
        table.getColumnModel().getColumn(0).setMaxWidth(mpv5.ui.modern.ModernTheme.px(60));
        table.getColumnModel().getColumn(1).setPreferredWidth(mpv5.ui.modern.ModernTheme.px(420));

        final JTextArea notes = new JTextArea(6, 60);
        notes.setEditable(false);
        notes.setLineWrap(true);
        notes.setWrapStyleWord(true);
        notes.setText(yabs.tr("ustva.intro"));

        JPanel center = new JPanel(new BorderLayout(0, 8));
        center.add(new JScrollPane(table), BorderLayout.CENTER);
        center.add(new JScrollPane(notes), BorderLayout.SOUTH);

        root.add(top, BorderLayout.NORTH);
        root.add(center, BorderLayout.CENTER);

        final UstvaCalc.Result[] shown = new UstvaCalc.Result[1];
        compute.addActionListener(ev -> {
            final Date[] p = period((Integer) year.getValue(), period.getSelectedIndex());
            final UstvaCalc.Basis b = basis.getSelectedIndex() == 1 ? UstvaCalc.Basis.CASH : UstvaCalc.Basis.ACCRUAL;
            yabs.settings().set("ustva.basis", b == UstvaCalc.Basis.CASH ? "cash" : "accrual");
            compute.setEnabled(false);
            yabs.background(() -> {
                try {
                    vTimeframe t = new vTimeframe(p[0], p[1]);
                    java.util.List<mpv5.handler.JournalCalc.Entry> byDoc = JournalLoader.loadAll(t, false);
                    java.util.List<mpv5.handler.JournalCalc.Entry> income = b == UstvaCalc.Basis.CASH
                            ? JournalLoader.loadAllResult(t, true).entries : byDoc;
                    return UstvaCalc.compute(b, income, byDoc, EcSalesListExport.dbInspector());
                } catch (Exception e) {
                    throw new IllegalStateException(e.getMessage(), e);
                }
            }, r -> {
                compute.setEnabled(true);
                shown[0] = r;
                show(yabs, r, model, notes, (String) period.getSelectedItem() + " " + year.getValue());
            });
        });

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() != 2 || shown[0] == null || table.getSelectedRow() < 0) {
                    return;
                }
                String kz = String.valueOf(model.getValueAt(table.getSelectedRow(), 0));
                UstvaCalc.Line l = kz.startsWith(UstvaCalc.KZ_35) ? shown[0].line(UstvaCalc.KZ_35) : shown[0].line(kz);
                if (l == null || l.documents.isEmpty()) {
                    return;
                }
                StringBuilder b = new StringBuilder();
                for (Map.Entry<String, BigDecimal> d : l.documents.entrySet()) {
                    b.append(d.getKey()).append(": ").append(FormatNumber.formatDezimal(d.getValue())).append('\n');
                }
                JTextArea a = new JTextArea(b.toString(), Math.min(20, l.documents.size() + 1), 40);
                a.setEditable(false);
                JOptionPane.showMessageDialog(root, new JScrollPane(a), yabs.tr("ustva.documents", kz), JOptionPane.PLAIN_MESSAGE);
            }
        });
        return root;
    }

    private static String money(BigDecimal v) {
        return FormatNumber.formatDezimal(v == null ? BigDecimal.ZERO : v);
    }

    private static void show(ExtensionContext yabs, UstvaCalc.Result r, DefaultTableModel model, JTextArea notes, String periodName) {
        model.setRowCount(0);
        row(yabs, model, r, UstvaCalc.KZ_81, money(r.line(UstvaCalc.KZ_81).formBase()), money(r.tax81()), money(r.line(UstvaCalc.KZ_81).bookedTax));
        row(yabs, model, r, UstvaCalc.KZ_86, money(r.line(UstvaCalc.KZ_86).formBase()), money(r.tax86()), money(r.line(UstvaCalc.KZ_86).bookedTax));
        for (Map.Entry<BigDecimal, UstvaCalc.Line> o : r.otherRates.entrySet()) {
            model.addRow(new Object[]{"35/36", yabs.tr("ustva.kz.35", o.getKey().stripTrailingZeros().toPlainString()),
                money(o.getValue().formBase()), money(o.getValue().bookedTax), money(o.getValue().bookedTax)});
        }
        for (String kz : new String[]{UstvaCalc.KZ_87, UstvaCalc.KZ_41, UstvaCalc.KZ_21, UstvaCalc.KZ_43, UstvaCalc.KZ_48, UstvaCalc.KZ_45}) {
            if (r.line(kz).base.signum() != 0) {
                row(yabs, model, r, kz, money(r.line(kz).formBase()), "", "");
            }
        }
        row(yabs, model, r, UstvaCalc.KZ_66, "", money(r.tax66()), money(r.tax66()));
        model.addRow(new Object[]{UstvaCalc.KZ_83, yabs.tr("ustva.kz.83"), "", money(r.kz83()), ""});

        StringBuilder b = new StringBuilder();
        b.append(yabs.tr("ustva.result", periodName, yabs.tr(r.basis == UstvaCalc.Basis.CASH ? "ustva.basis.cash" : "ustva.basis.accrual")));
        b.append("\n").append(yabs.tr("ustva.compare", money(r.bookedVat()), money(r.tax81().add(r.tax86()).add(r.tax36()))));
        java.util.Map<String, java.util.List<String>> byKey = new java.util.LinkedHashMap<String, java.util.List<String>>();
        for (String[] p : r.problems) {
            byKey.computeIfAbsent(p[0], k -> new java.util.ArrayList<String>());
            if (!p[1].isEmpty() && !byKey.get(p[0]).contains(p[1])) {
                byKey.get(p[0]).add(p[1]);
            }
        }
        for (Map.Entry<String, java.util.List<String>> p : byKey.entrySet()) {
            b.append("\n• ").append(yabs.tr(p.getKey())).append(p.getValue().isEmpty() ? "" : " " + String.join(", ", p.getValue()));
        }
        b.append("\n\n").append(yabs.tr("ustva.notcovered"));
        notes.setText(b.toString());
        notes.setCaretPosition(0);
    }

    private static void row(ExtensionContext yabs, DefaultTableModel model, UstvaCalc.Result r, String kz, String base, String tax, String booked) {
        model.addRow(new Object[]{kz, yabs.tr("ustva.kz." + kz), base, tax, booked});
    }
}
