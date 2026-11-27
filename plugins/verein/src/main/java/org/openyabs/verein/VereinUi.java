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
package org.openyabs.verein;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.db.objects.Contact;
import mpv5.db.objects.Item;
import mpv5.globals.Constants;
import mpv5.logging.Log;
import mpv5.plugin.api.ExtensionContext;
import mpv5.ui.dialogs.DialogForFile;
import mpv5.ui.panels.DataPanel;
import org.openyabs.verein.VereinStore.InvoiceData;
import org.openyabs.verein.VereinStore.MemberData;
import org.openyabs.verein.VereinStore.Settings;

/**
 * Localized Swing UI for the German Non-Profit Association (e.V.) plugin:
 * settings page, Contact/Invoice banners, member/SEPA & sphere/donation
 * dialogs, and 4-sphere EÜR overview tab with CSV export.
 */
final class VereinUi {

    private static final Color BANNER_BG = new Color(254, 249, 195);
    private static final Color BANNER_BORDER = new Color(250, 204, 21);
    private static final Color BANNER_FG = new Color(113, 63, 18);

    private VereinUi() {
    }

    static JComponent settingsPage(final ExtensionContext yabs, final VereinPlugin plugin) {
        final Settings s = Settings.load();

        final JTextField creditorField = new JTextField(s.creditorId, 26);
        final JTextField finanzamtField = new JTextField(s.finanzamt, 28);
        final JTextField taxNumField = new JTextField(s.taxNumber, 22);
        final String[] bescheidCodes = {VereinStore.BESCHEID_FREISTELLUNG, VereinStore.BESCHEID_60A};
        final JComboBox<String> bescheidBox = new JComboBox<String>(new String[]{
            yabs.tr("settings.bescheid." + VereinStore.BESCHEID_FREISTELLUNG),
            yabs.tr("settings.bescheid." + VereinStore.BESCHEID_60A)
        });
        bescheidBox.setSelectedIndex(VereinStore.BESCHEID_60A.equalsIgnoreCase(s.bescheidType) ? 1 : 0);
        final JTextField dateField = new JTextField(s.freistellungDate, 14);
        final JTextField periodField = new JTextField(s.freistellungPeriod, 24);
        final JTextField purposeField = new JTextField(s.purposeClause, 36);
        final JCheckBox duesDeductibleBox = new JCheckBox(yabs.tr("settings.duesDeductible"), s.duesTaxDeductible);
        final JCheckBox kuBox = new JCheckBox(yabs.tr("settings.kleinunternehmer"), s.kleinunternehmer);
        final JCheckBox warnBox = new JCheckBox(yabs.tr("settings.warnOnSave"), s.warnOnSave);

        Runnable applySettings = () -> {
            s.creditorId = creditorField.getText().trim();
            s.finanzamt = finanzamtField.getText().trim();
            s.taxNumber = taxNumField.getText().trim();
            s.bescheidType = bescheidCodes[Math.max(0, bescheidBox.getSelectedIndex())];
            s.freistellungDate = dateField.getText().trim();
            s.freistellungPeriod = periodField.getText().trim();
            s.purposeClause = purposeField.getText().trim();
            s.duesTaxDeductible = duesDeductibleBox.isSelected();
            s.kleinunternehmer = kuBox.isSelected();
            s.warnOnSave = warnBox.isSelected();
            s.save();
        };

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(4, 6, 4, 6);
        g.anchor = GridBagConstraints.WEST;
        g.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;
        addRow(form, g, row++, yabs.tr("settings.creditorId"), creditorField);
        addRow(form, g, row++, yabs.tr("settings.finanzamt"), finanzamtField);
        addRow(form, g, row++, yabs.tr("settings.taxNumber"), taxNumField);
        addRow(form, g, row++, yabs.tr("settings.bescheidType"), bescheidBox);
        addRow(form, g, row++, yabs.tr("settings.freistellungDate"), dateField);
        addRow(form, g, row++, yabs.tr("settings.freistellungPeriod"), periodField);
        addRow(form, g, row++, yabs.tr("settings.purposeClause"), purposeField);

        g.gridx = 0;
        g.gridwidth = 2;
        g.gridy = row++;
        form.add(duesDeductibleBox, g);
        g.gridy = row++;
        form.add(kuBox, g);
        g.gridy = row++;
        form.add(warnBox, g);

        if (plugin != null) {
            JPanel setupBox = new JPanel(new GridBagLayout());
            setupBox.setBorder(BorderFactory.createTitledBorder(yabs.tr("setup.imports.title")));
            GridBagConstraints sg = new GridBagConstraints();
            sg.insets = new Insets(4, 6, 4, 6);
            sg.anchor = GridBagConstraints.WEST;
            sg.fill = GridBagConstraints.HORIZONTAL;
            int srow = 0;

            JButton importTiersBtn = new JButton(yabs.tr("cmd.importTiers.title"));
            importTiersBtn.setToolTipText(yabs.tr("cmd.importTiers.desc"));
            importTiersBtn.addActionListener(e -> {
                applySettings.run();
                yabs.background(() -> plugin.importStarterTiers(yabs), msg -> yabs.popup(msg));
            });
            addActionRow(setupBox, sg, srow++, importTiersBtn, yabs.tr("cmd.importTiers.desc"));

            JButton exportTemplatesBtn = new JButton(yabs.tr("cmd.exportTemplates.title"));
            exportTemplatesBtn.setToolTipText(yabs.tr("cmd.exportTemplates.desc"));
            exportTemplatesBtn.addActionListener(e -> plugin.exportTemplates(yabs));
            addActionRow(setupBox, sg, srow++, exportTemplatesBtn, yabs.tr("cmd.exportTemplates.desc"));

            g.gridx = 0;
            g.gridy = row++;
            g.gridwidth = 2;
            g.insets = new Insets(10, 0, 6, 0);
            form.add(setupBox, g);
        }

        g.gridx = 0;
        g.gridwidth = 2;
        g.gridy = row++;
        g.insets = new Insets(10, 6, 6, 6);
        form.add(new JLabel(yabs.tr("settings.info")), g);

        JPanel topWrapper = new JPanel(new BorderLayout());
        topWrapper.add(form, BorderLayout.NORTH);

        JButton saveBtn = new JButton(yabs.tr("common.save"));
        saveBtn.addActionListener(e -> {
            applySettings.run();
            yabs.notify(yabs.tr("settings.saved"));
        });

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        actions.add(saveBtn);

        JPanel root = new JPanel(new BorderLayout());
        root.add(new JScrollPane(topWrapper), BorderLayout.CENTER);
        root.add(actions, BorderLayout.SOUTH);
        return root;
    }

    static void decorate(final DataPanel panel, final ExtensionContext yabs) {
        if (panel == null) {
            return;
        }
        DatabaseObject owner = panel.getDataOwner();
        if (owner instanceof Contact && owner.__getIDS() > 0) {
            decorateContact(panel, (Contact) owner, yabs);
        } else if (owner instanceof Item && owner.__getIDS() > 0 && isInvoiceType(((Item) owner).__getInttype())) {
            decorateInvoice(panel, (Item) owner, yabs);
        }
    }

    private static boolean isInvoiceType(int type) {
        return type == Constants.TYPE_INVOICE || type == Constants.TYPE_CREDIT
                || type == Constants.TYPE_DEPOSIT || type == Constants.TYPE_PART_PAYMENT;
    }

    private static void decorateContact(final DataPanel panel, final Contact contact, final ExtensionContext yabs) {
        MemberData m = VereinStore.loadMemberSafe(contact.__getIDS());
        String summary;
        if (m == null) {
            summary = yabs.tr("banner.member.empty");
        } else {
            String sepa = m.sepaMandateRef.isEmpty() ? "" : yabs.tr("banner.member.sepa", m.sepaMandateRef);
            summary = yabs.tr("banner.member",
                    m.memberId.isEmpty() ? String.valueOf(contact.__getIDS()) : m.memberId,
                    yabs.tr("status." + m.status),
                    m.tierSku.isEmpty() ? "-" : m.tierSku,
                    sepa);
        }
        JButton edit = new JButton(yabs.tr("banner.member.btn"));
        edit.addActionListener(e -> {
            if (editMemberDialog(panel instanceof Component ? (Component) panel : null, contact, yabs)) {
                decorateContact(panel, contact, yabs);
            }
        });
        panel.setPluginBanner(banner(summary, edit));
    }

    private static void decorateInvoice(final DataPanel panel, final Item item, final ExtensionContext yabs) {
        InvoiceData d = VereinStore.loadInvoiceSafe(item.__getIDS());
        String don = d.isDonation ? yabs.tr("banner.invoice.donation") : "";
        String summary = yabs.tr("banner.invoice", yabs.tr("sphere." + d.taxSphere), d.fiscalYear, don);

        JButton edit = new JButton(yabs.tr("banner.invoice.btn"));
        edit.addActionListener(e -> {
            if (editInvoiceDialog(panel instanceof Component ? (Component) panel : null, item, yabs)) {
                decorateInvoice(panel, item, yabs);
            }
        });
        JButton check = new JButton(yabs.tr("banner.invoice.check"));
        check.addActionListener(e -> {
            List<String> warnings = VereinPlugin.validateItem(item, yabs);
            showValidationResult(panel instanceof Component ? (Component) panel : null, warnings, yabs);
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

    static boolean editMemberDialog(Component parent, Contact contact, ExtensionContext yabs) {
        MemberData loaded = VereinStore.loadMemberSafe(contact.__getIDS());
        final MemberData m = loaded != null ? loaded : new MemberData();
        m.contactId = contact.__getIDS();

        Window owner = parent == null ? null : SwingUtilities.getWindowAncestor(parent);
        final JDialog dialog = new JDialog(owner, yabs.tr("member.dialog.title", contact.__getCname()), JDialog.ModalityType.APPLICATION_MODAL);
        final boolean[] saved = {false};

        JTextField memberId = new JTextField(m.memberId, 16);
        String[] statusLabels = new String[VereinStore.STATUSES.length];
        int selStatus = 0;
        for (int i = 0; i < VereinStore.STATUSES.length; i++) {
            statusLabels[i] = yabs.tr("status." + VereinStore.STATUSES[i]);
            if (VereinStore.STATUSES[i].equals(m.status)) {
                selStatus = i;
            }
        }
        JComboBox<String> statusBox = new JComboBox<String>(statusLabels);
        statusBox.setSelectedIndex(selStatus);

        JTextField tierSku = new JTextField(m.tierSku, 22);
        JTextField joinDate = new JTextField(m.joinDate, 14);
        JTextField exitDate = new JTextField(m.exitDate, 14);
        JTextField sepaRef = new JTextField(m.sepaMandateRef, 22);
        JTextField sepaDate = new JTextField(m.sepaMandateDate, 14);
        JTextArea notes = new JTextArea(m.notes, 4, 32);
        notes.setLineWrap(true);
        notes.setWrapStyleWord(true);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(4, 6, 4, 6);
        g.anchor = GridBagConstraints.WEST;
        g.fill = GridBagConstraints.HORIZONTAL;
        int row = 0;
        addRow(form, g, row++, yabs.tr("member.field.memberId"), memberId);
        addRow(form, g, row++, yabs.tr("member.field.status"), statusBox);
        addRow(form, g, row++, yabs.tr("member.field.tierSku"), tierSku);
        addRow(form, g, row++, yabs.tr("member.field.joinDate"), joinDate);
        addRow(form, g, row++, yabs.tr("member.field.exitDate"), exitDate);
        addRow(form, g, row++, yabs.tr("member.field.sepaRef"), sepaRef);
        addRow(form, g, row++, yabs.tr("member.field.sepaDate"), sepaDate);
        addRow(form, g, row++, yabs.tr("member.field.notes"), new JScrollPane(notes));

        JButton cancel = new JButton(yabs.tr("common.cancel"));
        cancel.addActionListener(e -> dialog.dispose());
        JButton ok = new JButton(yabs.tr("common.save"));
        ok.addActionListener(e -> {
            m.memberId = memberId.getText().trim();
            m.status = VereinStore.STATUSES[Math.max(0, statusBox.getSelectedIndex())];
            m.tierSku = tierSku.getText().trim();
            m.joinDate = joinDate.getText().trim();
            m.exitDate = exitDate.getText().trim();
            m.sepaMandateRef = sepaRef.getText().trim();
            m.sepaMandateDate = sepaDate.getText().trim();
            m.notes = notes.getText().trim();
            try {
                VereinStore.saveMember(VereinStore.connection(), m);
                saved[0] = true;
                dialog.dispose();
            } catch (Exception ex) {
                Log.Debug(VereinUi.class, ex);
                JOptionPane.showMessageDialog(dialog, yabs.tr("common.saveFailed", ex.getMessage()),
                        yabs.tr("common.error"), JOptionPane.ERROR_MESSAGE);
            }
        });

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        btns.add(cancel);
        btns.add(ok);
        JPanel root = new JPanel(new BorderLayout());
        root.add(new JScrollPane(form), BorderLayout.CENTER);
        root.add(btns, BorderLayout.SOUTH);
        dialog.setContentPane(root);
        dialog.pack();
        dialog.setLocationRelativeTo(owner);
        dialog.setVisible(true);
        return saved[0];
    }

    static boolean editInvoiceDialog(Component parent, Item item, ExtensionContext yabs) {
        final InvoiceData d = VereinStore.loadInvoiceSafe(item.__getIDS());
        Window owner = parent == null ? null : SwingUtilities.getWindowAncestor(parent);
        final JDialog dialog = new JDialog(owner, yabs.tr("invoice.dialog.title", item.__getCnumber()), JDialog.ModalityType.APPLICATION_MODAL);
        final boolean[] saved = {false};

        String[] sphereLabels = new String[VereinStore.SPHERES.length];
        int selSphere = 0;
        for (int i = 0; i < VereinStore.SPHERES.length; i++) {
            sphereLabels[i] = yabs.tr("sphere." + VereinStore.SPHERES[i]);
            if (VereinStore.SPHERES[i].equals(d.taxSphere)) {
                selSphere = i;
            }
        }
        JComboBox<String> sphereBox = new JComboBox<String>(sphereLabels);
        sphereBox.setSelectedIndex(selSphere);

        JTextField yearField = new JTextField(d.fiscalYear, 10);
        JCheckBox donationBox = new JCheckBox(yabs.tr("invoice.field.isDonation"), d.isDonation);
        String[] dkCodes = {VereinStore.DONATION_SPENDE, VereinStore.DONATION_MITGLIEDSBEITRAG};
        JComboBox<String> dkBox = new JComboBox<String>(new String[]{
            yabs.tr("donationKind." + VereinStore.DONATION_SPENDE),
            yabs.tr("donationKind." + VereinStore.DONATION_MITGLIEDSBEITRAG)
        });
        dkBox.setSelectedIndex(VereinStore.DONATION_MITGLIEDSBEITRAG.equalsIgnoreCase(d.donationKind) ? 1 : 0);
        JCheckBox waiverBox = new JCheckBox(yabs.tr("invoice.field.waiver"), d.waiverReimbursement);
        JCheckBox sepaBox = new JCheckBox(yabs.tr("invoice.field.sepa"), d.sepaDirectDebit);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(4, 6, 4, 6);
        g.anchor = GridBagConstraints.WEST;
        g.fill = GridBagConstraints.HORIZONTAL;
        int row = 0;
        addRow(form, g, row++, yabs.tr("invoice.field.sphere"), sphereBox);
        addRow(form, g, row++, yabs.tr("invoice.field.fiscalYear"), yearField);
        g.gridx = 0;
        g.gridwidth = 2;
        g.gridy = row++;
        form.add(donationBox, g);
        addRow(form, g, row++, yabs.tr("invoice.field.donationKind"), dkBox);
        g.gridx = 0;
        g.gridwidth = 2;
        g.gridy = row++;
        form.add(waiverBox, g);
        g.gridy = row++;
        form.add(sepaBox, g);

        JButton cancel = new JButton(yabs.tr("common.cancel"));
        cancel.addActionListener(e -> dialog.dispose());
        JButton ok = new JButton(yabs.tr("common.save"));
        ok.addActionListener(e -> {
            d.taxSphere = VereinStore.SPHERES[Math.max(0, sphereBox.getSelectedIndex())];
            d.fiscalYear = yearField.getText().trim();
            d.isDonation = donationBox.isSelected();
            d.donationKind = dkCodes[Math.max(0, dkBox.getSelectedIndex())];
            d.waiverReimbursement = waiverBox.isSelected();
            d.sepaDirectDebit = sepaBox.isSelected();
            try {
                VereinStore.saveInvoice(VereinStore.connection(), d);
                saved[0] = true;
                dialog.dispose();
            } catch (Exception ex) {
                Log.Debug(VereinUi.class, ex);
                JOptionPane.showMessageDialog(dialog, yabs.tr("common.saveFailed", ex.getMessage()),
                        yabs.tr("common.error"), JOptionPane.ERROR_MESSAGE);
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

    static void openSphereOverviewTab(ExtensionContext yabs) {
        Map<String, BigDecimal> totals = new LinkedHashMap<String, BigDecimal>();
        Map<String, Integer> counts = new LinkedHashMap<String, Integer>();
        for (String sp : VereinStore.SPHERES) {
            totals.put(sp, BigDecimal.ZERO);
            counts.put(sp, 0);
        }

        try {
            if (VereinStore.hasDatabase()) {
                Map<Integer, InvoiceData> meta = new LinkedHashMap<Integer, InvoiceData>();
                for (InvoiceData d : VereinStore.loadAllInvoices(VereinStore.connection())) {
                    meta.put(d.itemId, d);
                }
                for (DatabaseObject o : DatabaseObject.getObjects(Context.getInvoice())) {
                    Item item = (Item) o;
                    if (item.__getIntstatus() == Item.STATUS_CANCELLED) {
                        continue;
                    }
                    InvoiceData d = meta.get(item.__getIDS());
                    String sp = d == null ? VereinStore.SPHERE_IDEELL : d.taxSphere;
                    BigDecimal gross = VereinPlugin.grossTotal(item);
                    totals.put(sp, totals.get(sp).add(gross));
                    counts.put(sp, counts.get(sp) + 1);
                }
            }
        } catch (Exception ex) {
            Log.Debug(VereinUi.class, ex);
        }

        DefaultTableModel model = new DefaultTableModel(new Object[]{
            yabs.tr("overview.col.sphere"),
            yabs.tr("overview.col.count"),
            yabs.tr("overview.col.gross"),
            yabs.tr("overview.col.note")
        }, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        BigDecimal grandTotal = BigDecimal.ZERO;
        for (String sp : VereinStore.SPHERES) {
            BigDecimal sum = totals.get(sp);
            grandTotal = grandTotal.add(sum);
            String note;
            if (VereinStore.SPHERE_WIRTSCHAFTLICH.equals(sp)) {
                note = sum.compareTo(VereinValidator.COMMERCIAL_THRESHOLD_2026) > 0
                        ? yabs.tr("overview.note.WIRTSCHAFTLICH.over")
                        : yabs.tr("overview.note.WIRTSCHAFTLICH.ok");
            } else {
                note = yabs.tr("overview.note." + sp);
            }
            model.addRow(new Object[]{
                yabs.tr("sphere." + sp),
                counts.get(sp),
                sum.setScale(2, RoundingMode.HALF_UP).toPlainString(),
                note
            });
        }

        String summaryText = grandTotal.compareTo(VereinValidator.TIMELY_USE_THRESHOLD_2026) > 0
                ? yabs.tr("overview.summary.timelyOver", grandTotal.setScale(2, RoundingMode.HALF_UP).toPlainString())
                : yabs.tr("overview.summary.timelyOk", grandTotal.setScale(2, RoundingMode.HALF_UP).toPlainString());

        JButton exportBtn = new JButton(yabs.tr("common.exportCsv"));
        exportBtn.addActionListener(e -> {
            try {
                DialogForFile d = new DialogForFile(DialogForFile.FILES_ONLY,
                        "verein_sphaeren_uebersicht_" + LocalDate.now() + ".csv");
                if (!d.saveFile()) {
                    return;
                }
                File target = d.getFile();
                if (!target.getName().toLowerCase().endsWith(".csv")) {
                    target = new File(target.getParentFile(), target.getName() + ".csv");
                }
                Files.writeString(target.toPath(), VereinValidator.buildSphereReportCsv(totals, yabs), StandardCharsets.UTF_8);
                yabs.notify(yabs.tr("overview.csvSaved", target.getAbsolutePath()));
            } catch (Exception ex) {
                Log.Debug(VereinUi.class, ex);
                yabs.error(yabs.tr("common.saveFailed", ex.getMessage()));
            }
        });

        JPanel south = new JPanel(new BorderLayout(8, 0));
        south.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        south.add(new JLabel(summaryText), BorderLayout.CENTER);
        south.add(exportBtn, BorderLayout.EAST);

        JPanel root = new JPanel(new BorderLayout());
        root.add(new JScrollPane(new JTable(model)), BorderLayout.CENTER);
        root.add(south, BorderLayout.SOUTH);
        yabs.openTab(yabs.tr("overview.tab.title"), root);
    }

    static void showValidationResult(Component parent, List<String> warnings, ExtensionContext yabs) {
        if (warnings == null || warnings.isEmpty()) {
            JOptionPane.showMessageDialog(parent, yabs.tr("val.ok"), yabs.tr("val.title"), JOptionPane.INFORMATION_MESSAGE);
        } else {
            StringBuilder sb = new StringBuilder(yabs.tr("val.header")).append("\n\n");
            for (String w : warnings) {
                sb.append("\u2022 ").append(w).append("\n");
            }
            JOptionPane.showMessageDialog(parent, sb.toString(), yabs.tr("val.title"), JOptionPane.WARNING_MESSAGE);
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
