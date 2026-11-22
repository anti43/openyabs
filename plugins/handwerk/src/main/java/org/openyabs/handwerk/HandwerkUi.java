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
package org.openyabs.handwerk;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
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
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import mpv5.db.common.DatabaseObject;
import mpv5.db.objects.Contact;
import mpv5.db.objects.Item;
import mpv5.db.objects.SubItem;
import mpv5.globals.Constants;
import mpv5.handler.ItemService;
import mpv5.logging.Log;
import mpv5.plugin.api.ExtensionContext;
import mpv5.ui.panels.DataPanel;
import org.openyabs.handwerk.BoqDocument.BoqPosition;
import org.openyabs.handwerk.HandwerkStore.ContactMeta;
import org.openyabs.handwerk.HandwerkStore.ItemMeta;
import org.openyabs.handwerk.HandwerkStore.LineCostType;
import org.openyabs.handwerk.HandwerkStore.Measurement;
import org.openyabs.handwerk.HandwerkStore.Settings;
import org.openyabs.handwerk.HandwerkStore.Site;
import org.openyabs.handwerk.HandwerkStore.TimeSheet;
import org.openyabs.handwerk.HandwerkValidator.LaborSplitSummary;
import org.openyabs.handwerk.HandwerkValidator.LineInput;
import org.openyabs.handwerk.HandwerkValidator.RetentionResult;

/**
 * Localized Swing UI for the Skilled Trades &amp; Construction /
 * Handwerksbetriebe plugin: settings page, Contact &amp; Item banners,
 * site/certificate/compliance/§ 35a/BoQ/measurement/time-sheet dialogs, and
 * 4-tab overview dashboard.
 */
final class HandwerkUi {

    private static final Color BANNER_BG = new Color(255, 247, 237);
    private static final Color BANNER_BORDER = new Color(253, 186, 116);
    private static final Color BANNER_FG = new Color(154, 52, 18);

    private HandwerkUi() {
    }

    // --------------------------------------------------------- settings page

    static JComponent settingsPage(final ExtensionContext yabs) {
        final Settings s = Settings.load();

        final JTextField chamberNameField = new JTextField(s.chamberName, 24);
        final JTextField chamberNoField = new JTextField(s.chamberNumber, 16);
        final JTextField freiTaxField = new JTextField(s.ownFreistellungTaxNo, 18);
        final JTextField freiSecField = new JTextField(s.ownFreistellungSecNo, 18);
        final JTextField freiUntilField = new JTextField(s.ownFreistellungValidUntil, 12);
        final JTextField ustUntilField = new JTextField(s.ownUst1TgValidUntil, 12);

        final JTextField rateMasterField = new JTextField(s.rateMaster.toPlainString(), 10);
        final JTextField rateJourneyField = new JTextField(s.rateJourneyman.toPlainString(), 10);
        final JTextField rateApprenticeField = new JTextField(s.rateApprentice.toPlainString(), 10);
        final JTextField rateTravelField = new JTextField(s.rateTravel.toPlainString(), 10);
        final JTextField markupField = new JTextField(s.datanormMarkupPercent.toPlainString(), 8);

        final JComboBox<String> contractBox = new JComboBox<String>(new String[]{
            HandwerkStore.CONTRACT_BGB, HandwerkStore.CONTRACT_VOB_B
        });
        contractBox.setSelectedItem(s.defaultContractType);
        final JTextField retPctField = new JTextField(s.defaultRetentionPercent.toPlainString(), 8);
        final JComboBox<String> retBasisBox = new JComboBox<String>(new String[]{
            HandwerkStore.BASIS_GROSS, HandwerkStore.BASIS_NET
        });
        retBasisBox.setSelectedItem(s.defaultRetentionBasis);
        final JTextField leadDaysField = new JTextField(String.valueOf(s.certLeadDays), 6);
        final JCheckBox warnBox = new JCheckBox(yabs.tr("settings.warnOnSave"), s.warnOnSave);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        GridBagConstraints g = gbc();

        int row = 0;
        addRow(form, g, row++, yabs.tr("settings.chamberName"), chamberNameField);
        addRow(form, g, row++, yabs.tr("settings.chamberNumber"), chamberNoField);
        addRow(form, g, row++, yabs.tr("settings.ownFreistellungTaxNo"), freiTaxField);
        addRow(form, g, row++, yabs.tr("settings.ownFreistellungSecNo"), freiSecField);
        addRow(form, g, row++, yabs.tr("settings.ownFreistellungValidUntil"), freiUntilField);
        addRow(form, g, row++, yabs.tr("settings.ownUst1TgValidUntil"), ustUntilField);
        addRow(form, g, row++, yabs.tr("settings.rateMaster"), rateMasterField);
        addRow(form, g, row++, yabs.tr("settings.rateJourneyman"), rateJourneyField);
        addRow(form, g, row++, yabs.tr("settings.rateApprentice"), rateApprenticeField);
        addRow(form, g, row++, yabs.tr("settings.rateTravel"), rateTravelField);
        addRow(form, g, row++, yabs.tr("settings.datanormMarkup"), markupField);
        addRow(form, g, row++, yabs.tr("settings.defaultContractType"), contractBox);
        addRow(form, g, row++, yabs.tr("settings.defaultRetentionPercent"), retPctField);
        addRow(form, g, row++, yabs.tr("settings.defaultRetentionBasis"), retBasisBox);
        addRow(form, g, row++, yabs.tr("settings.certLeadDays"), leadDaysField);

        g.gridx = 0;
        g.gridy = row++;
        g.gridwidth = 2;
        form.add(warnBox, g);

        g.gridy = row++;
        g.insets = new Insets(10, 6, 6, 6);
        form.add(new JLabel(yabs.tr("settings.info")), g);

        JButton saveBtn = new JButton(yabs.tr("common.save"));
        saveBtn.addActionListener(e -> {
            s.chamberName = chamberNameField.getText().trim();
            s.chamberNumber = chamberNoField.getText().trim();
            s.ownFreistellungTaxNo = freiTaxField.getText().trim();
            s.ownFreistellungSecNo = freiSecField.getText().trim();
            s.ownFreistellungValidUntil = freiUntilField.getText().trim();
            s.ownUst1TgValidUntil = ustUntilField.getText().trim();
            s.rateMaster = HandwerkStore.parseDecimal(rateMasterField.getText(), s.rateMaster);
            s.rateJourneyman = HandwerkStore.parseDecimal(rateJourneyField.getText(), s.rateJourneyman);
            s.rateApprentice = HandwerkStore.parseDecimal(rateApprenticeField.getText(), s.rateApprentice);
            s.rateTravel = HandwerkStore.parseDecimal(rateTravelField.getText(), s.rateTravel);
            s.datanormMarkupPercent = HandwerkStore.parseDecimal(markupField.getText(), s.datanormMarkupPercent);
            s.defaultContractType = String.valueOf(contractBox.getSelectedItem());
            s.defaultRetentionPercent = HandwerkStore.parseDecimal(retPctField.getText(), s.defaultRetentionPercent);
            s.defaultRetentionBasis = String.valueOf(retBasisBox.getSelectedItem());
            s.certLeadDays = HandwerkStore.parseInt(leadDaysField.getText(), s.certLeadDays);
            s.warnOnSave = warnBox.isSelected();
            s.save();
            yabs.notify(yabs.tr("settings.saved"));
        });

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        actions.add(saveBtn);

        JPanel root = new JPanel(new BorderLayout());
        root.add(new JScrollPane(form), BorderLayout.CENTER);
        root.add(actions, BorderLayout.SOUTH);
        return root;
    }

    // --------------------------------------------------------------- banners

    static void decorate(final DataPanel panel, final ExtensionContext yabs) {
        if (panel == null) {
            return;
        }
        DatabaseObject owner = panel.getDataOwner();
        if (owner instanceof Contact && owner.__getIDS() > 0) {
            decorateContact(panel, (Contact) owner, yabs);
        } else if (owner instanceof Item && owner.__getIDS() > 0 && isTradesDocType(((Item) owner).__getInttype())) {
            decorateItem(panel, (Item) owner, yabs);
        }
    }

    static boolean isTradesDocType(int type) {
        return type == Constants.TYPE_OFFER
                || type == Constants.TYPE_ORDER
                || type == Constants.TYPE_INVOICE
                || type == Constants.TYPE_CREDIT
                || type == Constants.TYPE_DEPOSIT
                || type == Constants.TYPE_PART_PAYMENT;
    }

    private static void decorateContact(final DataPanel panel, final Contact contact, final ExtensionContext yabs) {
        ContactMeta cm = HandwerkStore.loadContactMetaSafe(contact.__getIDS());
        List<Site> sites = HandwerkStore.loadSitesForContactSafe(contact.__getIDS(), false);
        StringBuilder sb = new StringBuilder();
        sb.append(customerKindLabel(cm.customerKind, yabs));
        sb.append(" \u00b7 ");
        if (sites.isEmpty()) {
            sb.append(yabs.tr("banner.contact.noSites"));
        } else {
            sb.append(yabs.tr("banner.contact.sitesCount", sites.size()));
            sb.append(": ").append(sites.get(0).displayLabel());
            if (sites.size() > 1) {
                sb.append(" (+").append(sites.size() - 1).append(")");
            }
        }
        if (!cm.reverseChargeCertUntil.isEmpty()) {
            boolean exp = HandwerkValidator.isCertificateExpired(cm.reverseChargeCertUntil, LocalDate.now());
            sb.append(" \u00b7 USt 1 TG: ").append(cm.reverseChargeCertUntil)
                    .append(exp ? " [" + yabs.tr("status.expired") + "]" : "");
        }

        JButton manageBtn = new JButton(yabs.tr("banner.contact.btn"));
        manageBtn.addActionListener(e -> {
            openContactSitesDialog(panel instanceof Component ? (Component) panel : null, contact, yabs);
            decorateContact(panel, contact, yabs);
        });
        panel.setPluginBanner(banner(yabs.tr("banner.contact", sb.toString()), manageBtn));
    }

    private static void decorateItem(final DataPanel panel, final Item item, final ExtensionContext yabs) {
        ItemMeta im = HandwerkStore.loadItemMetaSafe(item.__getIDS());
        Site site = HandwerkStore.loadSiteSafe(im.siteId);
        BoqDocument boq = HandwerkStore.loadBoqForItemSafe(item.__getIDS());

        StringBuilder sb = new StringBuilder();
        if (site != null) {
            sb.append(site.displayLabel());
        } else if (!im.siteSnapshot.isEmpty()) {
            sb.append(im.siteSnapshot);
        } else {
            sb.append(yabs.tr("banner.item.noSite"));
        }
        if (im.reverseChargeEnabled) {
            sb.append(" \u00b7 \u00a7 13b RC");
        }
        if (im.laborTaxCreditEnabled) {
            LaborSplitSummary sp = HandwerkValidator.computeLaborSplit(
                    HandwerkPlugin.lineInputsOf(item),
                    HandwerkStore.loadLineCostTypesSafe(item.__getIDS()),
                    BigDecimal.ZERO);
            sb.append(" \u00b7 \u00a7 35a: ").append(sp.laborGross.toPlainString());
        }
        if (im.retentionPercent != null && im.retentionPercent.signum() > 0) {
            RetentionResult ret = HandwerkValidator.computeRetention(item.getNetAfterDiscount(), item.getGrossAmount(), im);
            sb.append(" \u00b7 ").append(yabs.tr("banner.item.retention",
                    im.retentionPercent.toPlainString(), ret.retentionAmount.toPlainString()));
        }
        if (boq != null) {
            sb.append(" \u00b7 BoQ: ").append(boq.positions.size()).append(" Pos.");
        }

        Component comp = panel instanceof Component ? (Component) panel : null;

        JButton metaBtn = new JButton(yabs.tr("banner.item.metaBtn"));
        metaBtn.addActionListener(e -> {
            if (editItemMetaDialog(comp, item, yabs)) {
                decorateItem(panel, item, yabs);
            }
        });

        JButton splitBtn = new JButton(yabs.tr("banner.item.splitBtn"));
        splitBtn.addActionListener(e -> {
            editLineCostTypesDialog(comp, item, yabs);
            decorateItem(panel, item, yabs);
        });

        JButton boqBtn = new JButton(yabs.tr("banner.item.boqBtn"));
        boqBtn.addActionListener(e -> {
            openBoqDialog(comp, item, yabs);
            decorateItem(panel, item, yabs);
        });

        JButton measBtn = new JButton(yabs.tr("banner.item.measBtn"));
        measBtn.addActionListener(e -> {
            openMeasurementsDialog(comp, item, yabs);
            decorateItem(panel, item, yabs);
        });

        JButton tsBtn = new JButton(yabs.tr("banner.item.tsBtn"));
        tsBtn.addActionListener(e -> {
            openTimeSheetsDialog(comp, item, yabs);
            decorateItem(panel, item, yabs);
        });

        JButton checkBtn = new JButton(yabs.tr("banner.item.checkBtn"));
        checkBtn.addActionListener(e -> {
            List<String> warnings = HandwerkPlugin.validateItem(item, yabs);
            showValidationResult(comp, warnings, yabs);
        });

        panel.setPluginBanner(banner(yabs.tr("banner.item", sb.toString()),
                metaBtn, splitBtn, boqBtn, measBtn, tsBtn, checkBtn));
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

    // ----------------------------------------- contact sites & certificates

    static void openContactSitesDialog(Component parent, final Contact contact, final ExtensionContext yabs) {
        Window owner = parent == null ? null : SwingUtilities.getWindowAncestor(parent);
        final JDialog dialog = new JDialog(owner, yabs.tr("contact.dialog.title", contact.__getCname()),
                JDialog.ModalityType.APPLICATION_MODAL);

        final ContactMeta cm = HandwerkStore.loadContactMetaSafe(contact.__getIDS());
        final JComboBox<String> kindBox = new JComboBox<String>(new String[]{
            HandwerkStore.CUSTOMER_PRIVATE,
            HandwerkStore.CUSTOMER_COMMERCIAL,
            HandwerkStore.CUSTOMER_CONSTRUCTION_13B,
            HandwerkStore.CUSTOMER_PUBLIC
        });
        kindBox.setSelectedItem(cm.customerKind);
        final JTextField ustNoField = new JTextField(cm.reverseChargeCertNo, 14);
        final JTextField ustUntilField = new JTextField(cm.reverseChargeCertUntil, 10);
        final JTextField freiTaxField = new JTextField(cm.withholdingExemptionTaxNo, 14);
        final JTextField freiSecField = new JTextField(cm.withholdingExemptionSecNo, 14);
        final JTextField freiUntilField = new JTextField(cm.withholdingExemptionUntil, 10);

        JPanel certPanel = new JPanel(new GridBagLayout());
        certPanel.setBorder(BorderFactory.createTitledBorder(yabs.tr("contact.certs.title")));
        GridBagConstraints g = gbc();
        int r = 0;
        addRow(certPanel, g, r++, yabs.tr("contact.customerKind"), kindBox);
        addRow(certPanel, g, r++, yabs.tr("contact.ust1tgNo"), ustNoField);
        addRow(certPanel, g, r++, yabs.tr("contact.ust1tgUntil"), ustUntilField);
        addRow(certPanel, g, r++, yabs.tr("contact.freistellungTaxNo"), freiTaxField);
        addRow(certPanel, g, r++, yabs.tr("contact.freistellungSecNo"), freiSecField);
        addRow(certPanel, g, r++, yabs.tr("contact.freistellungUntil"), freiUntilField);

        JButton saveCertsBtn = new JButton(yabs.tr("contact.certs.save"));
        saveCertsBtn.addActionListener(e -> {
            cm.customerKind = String.valueOf(kindBox.getSelectedItem());
            cm.reverseChargeCertNo = ustNoField.getText().trim();
            cm.reverseChargeCertUntil = ustUntilField.getText().trim();
            cm.withholdingExemptionTaxNo = freiTaxField.getText().trim();
            cm.withholdingExemptionSecNo = freiSecField.getText().trim();
            cm.withholdingExemptionUntil = freiUntilField.getText().trim();
            try {
                HandwerkStore.saveContactMeta(HandwerkStore.connection(), cm);
                yabs.notify(yabs.tr("contact.certs.saved"));
            } catch (Exception ex) {
                Log.Debug(HandwerkUi.class, ex);
                yabs.error(ex.getMessage());
            }
        });
        g.gridx = 1;
        g.gridy = r;
        g.anchor = GridBagConstraints.EAST;
        certPanel.add(saveCertsBtn, g);

        final DefaultTableModel model = new DefaultTableModel(new Object[]{
            yabs.tr("site.col.projectNo"),
            yabs.tr("site.col.name"),
            yabs.tr("site.col.address"),
            yabs.tr("site.col.contract"),
            yabs.tr("site.col.warrantyUntil"),
            yabs.tr("site.col.maintenanceDue"),
            yabs.tr("site.col.status")
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        final JTable table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        @SuppressWarnings("unchecked")
        final List<Site>[] holder = new List[1];

        Runnable reload = () -> {
            model.setRowCount(0);
            holder[0] = HandwerkStore.loadSitesForContactSafe(contact.__getIDS(), true);
            for (Site s : holder[0]) {
                model.addRow(new Object[]{
                    s.projectNo, s.siteName, s.formattedAddress(),
                    s.contractType, s.warrantyUntil, s.maintenanceDue,
                    s.archived ? yabs.tr("status.archived") : yabs.tr("status.active")
                });
            }
        };
        reload.run();

        JButton addSiteBtn = new JButton(yabs.tr("common.add"));
        addSiteBtn.addActionListener(e -> {
            Settings cfg = Settings.load();
            Site s = new Site();
            s.contactId = contact.__getIDS();
            s.contractType = cfg.defaultContractType;
            s.retentionPercent = cfg.defaultRetentionPercent;
            s.retentionBasis = cfg.defaultRetentionBasis;
            s.warrantyYears = HandwerkStore.CONTRACT_VOB_B.equalsIgnoreCase(cfg.defaultContractType) ? 4 : 5;
            if (editSiteDialog(dialog, s, yabs)) {
                reload.run();
            }
        });

        JButton editSiteBtn = new JButton(yabs.tr("common.edit"));
        editSiteBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0 && row < holder[0].size()) {
                if (editSiteDialog(dialog, holder[0].get(row), yabs)) {
                    reload.run();
                }
            }
        });

        JButton deleteSiteBtn = new JButton(yabs.tr("common.delete"));
        deleteSiteBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0 && row < holder[0].size()) {
                Site s = holder[0].get(row);
                int choice = JOptionPane.showConfirmDialog(dialog,
                        yabs.tr("site.delete.confirm", s.displayLabel()),
                        yabs.tr("common.delete"), JOptionPane.YES_NO_OPTION);
                if (choice == JOptionPane.YES_OPTION) {
                    try {
                        HandwerkStore.deleteSite(HandwerkStore.connection(), s.id);
                        reload.run();
                    } catch (Exception ex) {
                        Log.Debug(HandwerkUi.class, ex);
                        yabs.error(ex.getMessage());
                    }
                }
            }
        });

        JButton closeBtn = new JButton(yabs.tr("common.close"));
        closeBtn.addActionListener(e -> dialog.dispose());

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 6));
        btnPanel.add(addSiteBtn);
        btnPanel.add(editSiteBtn);
        btnPanel.add(deleteSiteBtn);
        btnPanel.add(closeBtn);

        JPanel sitesPanel = new JPanel(new BorderLayout(6, 6));
        sitesPanel.setBorder(BorderFactory.createTitledBorder(yabs.tr("site.list.title")));
        sitesPanel.add(new JScrollPane(table), BorderLayout.CENTER);
        sitesPanel.add(btnPanel, BorderLayout.SOUTH);

        JPanel root = new JPanel(new BorderLayout(8, 8));
        root.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        root.add(certPanel, BorderLayout.NORTH);
        root.add(sitesPanel, BorderLayout.CENTER);

        dialog.setContentPane(root);
        dialog.setSize(new Dimension(860, 560));
        dialog.setLocationRelativeTo(owner);
        dialog.setVisible(true);
    }

    private static boolean editSiteDialog(Window owner, final Site site, final ExtensionContext yabs) {
        final JDialog d = new JDialog(owner, yabs.tr("site.edit.title"), JDialog.ModalityType.APPLICATION_MODAL);
        final boolean[] saved = {false};

        final JTextField projectNoField = new JTextField(site.projectNo, 16);
        final JTextField nameField = new JTextField(site.siteName, 24);
        final JTextField streetField = new JTextField(site.street, 24);
        final JTextField zipField = new JTextField(site.zip, 8);
        final JTextField cityField = new JTextField(site.city, 18);
        final JTextField countryField = new JTextField(site.country, 6);
        final JComboBox<String> contractBox = new JComboBox<String>(new String[]{
            HandwerkStore.CONTRACT_BGB, HandwerkStore.CONTRACT_VOB_B
        });
        contractBox.setSelectedItem(site.contractType);
        final JTextField retPctField = new JTextField(site.retentionPercent.toPlainString(), 8);
        final JComboBox<String> retBasisBox = new JComboBox<String>(new String[]{
            HandwerkStore.BASIS_GROSS, HandwerkStore.BASIS_NET
        });
        retBasisBox.setSelectedItem(site.retentionBasis);
        final JTextField accField = new JTextField(site.acceptanceDate, 12);
        final JTextField wYearsField = new JTextField(String.valueOf(site.warrantyYears > 0 ? site.warrantyYears : 5), 6);
        final JTextField wUntilField = new JTextField(site.warrantyUntil, 12);
        final JTextField maintField = new JTextField(site.maintenanceDue, 12);
        final JCheckBox archivedBox = new JCheckBox(yabs.tr("status.archived"), site.archived);
        final JTextArea notesArea = new JTextArea(site.notes, 3, 24);

        JButton calcWarrantyBtn = new JButton(yabs.tr("site.calcWarranty"));
        calcWarrantyBtn.addActionListener(e -> {
            int y = HandwerkStore.parseInt(wYearsField.getText(), 0);
            String until = HandwerkValidator.computeWarrantyUntil(
                    accField.getText().trim(), String.valueOf(contractBox.getSelectedItem()), y);
            if (!until.isEmpty()) {
                wUntilField.setText(until);
            }
        });

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        GridBagConstraints g = gbc();
        int r = 0;
        addRow(form, g, r++, yabs.tr("site.col.projectNo"), projectNoField);
        addRow(form, g, r++, yabs.tr("site.col.name"), nameField);
        addRow(form, g, r++, yabs.tr("site.street"), streetField);
        addRow(form, g, r++, yabs.tr("site.zip"), zipField);
        addRow(form, g, r++, yabs.tr("site.city"), cityField);
        addRow(form, g, r++, yabs.tr("site.country"), countryField);
        addRow(form, g, r++, yabs.tr("site.col.contract"), contractBox);
        addRow(form, g, r++, yabs.tr("site.retentionPercent"), retPctField);
        addRow(form, g, r++, yabs.tr("site.retentionBasis"), retBasisBox);
        addRow(form, g, r++, yabs.tr("site.acceptanceDate"), accField);
        addRow(form, g, r++, yabs.tr("site.warrantyYears"), wYearsField);

        JPanel wPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        wPanel.add(wUntilField);
        wPanel.add(calcWarrantyBtn);
        addRow(form, g, r++, yabs.tr("site.col.warrantyUntil"), wPanel);

        addRow(form, g, r++, yabs.tr("site.col.maintenanceDue"), maintField);
        addRow(form, g, r++, yabs.tr("common.notes"), new JScrollPane(notesArea));
        g.gridx = 1;
        g.gridy = r;
        form.add(archivedBox, g);

        JButton okBtn = new JButton(yabs.tr("common.save"));
        okBtn.addActionListener(e -> {
            site.projectNo = projectNoField.getText().trim();
            site.siteName = nameField.getText().trim();
            site.street = streetField.getText().trim();
            site.zip = zipField.getText().trim();
            site.city = cityField.getText().trim();
            site.country = countryField.getText().trim();
            site.contractType = String.valueOf(contractBox.getSelectedItem());
            site.retentionPercent = HandwerkStore.parseDecimal(retPctField.getText(), BigDecimal.ZERO);
            site.retentionBasis = String.valueOf(retBasisBox.getSelectedItem());
            site.acceptanceDate = accField.getText().trim();
            site.warrantyYears = HandwerkStore.parseInt(wYearsField.getText(),
                    HandwerkStore.CONTRACT_VOB_B.equalsIgnoreCase(site.contractType) ? 4 : 5);
            site.warrantyUntil = wUntilField.getText().trim();
            if (site.warrantyUntil.isEmpty() && !site.acceptanceDate.isEmpty()) {
                site.warrantyUntil = HandwerkValidator.computeWarrantyUntil(
                        site.acceptanceDate, site.contractType, site.warrantyYears);
            }
            site.maintenanceDue = maintField.getText().trim();
            site.archived = archivedBox.isSelected();
            site.notes = notesArea.getText().trim();
            try {
                HandwerkStore.saveSite(HandwerkStore.connection(), site);
                saved[0] = true;
                d.dispose();
            } catch (Exception ex) {
                Log.Debug(HandwerkUi.class, ex);
                yabs.error(ex.getMessage());
            }
        });
        JButton cancelBtn = new JButton(yabs.tr("common.cancel"));
        cancelBtn.addActionListener(e -> d.dispose());

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 6));
        actions.add(cancelBtn);
        actions.add(okBtn);

        d.setLayout(new BorderLayout());
        d.add(new JScrollPane(form), BorderLayout.CENTER);
        d.add(actions, BorderLayout.SOUTH);
        d.setSize(new Dimension(560, 560));
        d.setLocationRelativeTo(owner);
        d.setVisible(true);
        return saved[0];
    }

    // ------------------------------------------- item metadata & compliance

    static boolean editItemMetaDialog(Component parent, final Item item, final ExtensionContext yabs) {
        Window owner = parent == null ? null : SwingUtilities.getWindowAncestor(parent);
        final JDialog d = new JDialog(owner, yabs.tr("item.meta.title", item.__getCnumber()),
                JDialog.ModalityType.APPLICATION_MODAL);
        final boolean[] saved = {false};

        final ItemMeta im = HandwerkStore.loadItemMetaSafe(item.__getIDS());
        final ContactMeta cm = HandwerkStore.loadContactMetaSafe(item.__getContactsids());
        List<Site> sites = HandwerkStore.loadSitesForContactSafe(item.__getContactsids(), false);

        final JComboBox<Site> siteBox = new JComboBox<Site>();
        Site noneSite = new Site();
        noneSite.id = 0;
        noneSite.siteName = yabs.tr("common.none");
        siteBox.addItem(noneSite);
        Site selected = noneSite;
        for (Site s : sites) {
            siteBox.addItem(s);
            if (s.id == im.siteId) {
                selected = s;
            }
        }
        siteBox.setSelectedItem(selected);

        final JTextField snapshotField = new JTextField(im.siteSnapshot, 28);
        boolean defaultPrivate = HandwerkStore.CUSTOMER_PRIVATE.equalsIgnoreCase(cm.customerKind);
        boolean isNewMeta = false;
        try {
            isNewMeta = !HandwerkStore.hasItemMeta(HandwerkStore.connection(), item.__getIDS());
        } catch (Exception ignored) {
        }
        final JCheckBox credit35aBox = new JCheckBox(yabs.tr("item.meta.credit35a"),
                isNewMeta ? defaultPrivate : im.laborTaxCreditEnabled);
        final JCheckBox cashBox = new JCheckBox(yabs.tr("item.meta.cashPayment"), im.cashPayment);
        final JCheckBox notice14bBox = new JCheckBox(yabs.tr("item.meta.notice14b"),
                isNewMeta ? defaultPrivate : im.privateRetentionNotice);
        final JCheckBox rc13bBox = new JCheckBox(yabs.tr("item.meta.reverseCharge"),
                isNewMeta ? HandwerkStore.CUSTOMER_CONSTRUCTION_13B.equalsIgnoreCase(cm.customerKind) : im.reverseChargeEnabled);

        final JTextField retPctField = new JTextField(im.retentionPercent.toPlainString(), 8);
        final JComboBox<String> retBasisBox = new JComboBox<String>(new String[]{
            HandwerkStore.BASIS_GROSS, HandwerkStore.BASIS_NET
        });
        retBasisBox.setSelectedItem(im.retentionBasis);
        final JTextField retReleaseField = new JTextField(im.retentionReleaseDate, 12);
        final JTextField retBondField = new JTextField(im.retentionBondRef, 20);
        final JTextArea notesArea = new JTextArea(im.notes, 3, 28);

        siteBox.addActionListener(e -> {
            Site s = (Site) siteBox.getSelectedItem();
            if (s != null && s.id > 0) {
                snapshotField.setText(s.displayLabel());
                if (s.retentionPercent != null && s.retentionPercent.signum() > 0) {
                    retPctField.setText(s.retentionPercent.toPlainString());
                }
                retBasisBox.setSelectedItem(s.retentionBasis);
                if (!s.warrantyUntil.isEmpty() && retReleaseField.getText().trim().isEmpty()) {
                    retReleaseField.setText(s.warrantyUntil);
                }
            }
        });

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        GridBagConstraints g = gbc();
        int r = 0;
        addRow(form, g, r++, yabs.tr("item.meta.site"), siteBox);
        addRow(form, g, r++, yabs.tr("item.meta.siteSnapshot"), snapshotField);

        g.gridx = 0;
        g.gridy = r++;
        g.gridwidth = 2;
        form.add(credit35aBox, g);
        g.gridy = r++;
        form.add(cashBox, g);
        g.gridy = r++;
        form.add(notice14bBox, g);
        g.gridy = r++;
        form.add(rc13bBox, g);
        g.gridwidth = 1;

        addRow(form, g, r++, yabs.tr("site.retentionPercent"), retPctField);
        addRow(form, g, r++, yabs.tr("site.retentionBasis"), retBasisBox);
        addRow(form, g, r++, yabs.tr("item.meta.retentionRelease"), retReleaseField);
        addRow(form, g, r++, yabs.tr("item.meta.retentionBond"), retBondField);
        addRow(form, g, r++, yabs.tr("common.notes"), new JScrollPane(notesArea));

        JButton okBtn = new JButton(yabs.tr("common.save"));
        okBtn.addActionListener(e -> {
            Site s = (Site) siteBox.getSelectedItem();
            im.siteId = s == null ? 0 : s.id;
            im.siteSnapshot = snapshotField.getText().trim();
            im.laborTaxCreditEnabled = credit35aBox.isSelected();
            im.cashPayment = cashBox.isSelected();
            im.privateRetentionNotice = notice14bBox.isSelected();
            im.reverseChargeEnabled = rc13bBox.isSelected();
            im.retentionPercent = HandwerkStore.parseDecimal(retPctField.getText(), BigDecimal.ZERO);
            im.retentionBasis = String.valueOf(retBasisBox.getSelectedItem());
            RetentionResult ret = HandwerkValidator.computeRetention(
                    item.getNetAfterDiscount(), item.getGrossAmount(), im);
            im.retentionAmount = ret.retentionAmount;
            im.retentionReleaseDate = retReleaseField.getText().trim();
            im.retentionBondRef = retBondField.getText().trim();
            im.notes = notesArea.getText().trim();
            try {
                HandwerkStore.saveItemMeta(HandwerkStore.connection(), im);
                saved[0] = true;
                d.dispose();
            } catch (Exception ex) {
                Log.Debug(HandwerkUi.class, ex);
                yabs.error(ex.getMessage());
            }
        });
        JButton cancelBtn = new JButton(yabs.tr("common.cancel"));
        cancelBtn.addActionListener(e -> d.dispose());

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 6));
        actions.add(cancelBtn);
        actions.add(okBtn);

        d.setLayout(new BorderLayout());
        d.add(new JScrollPane(form), BorderLayout.CENTER);
        d.add(actions, BorderLayout.SOUTH);
        d.setSize(new Dimension(560, 500));
        d.setLocationRelativeTo(owner);
        d.setVisible(true);
        return saved[0];
    }

    // ------------------------------------------- § 35a line cost-type dialog

    static void editLineCostTypesDialog(Component parent, final Item item, final ExtensionContext yabs) {
        Window owner = parent == null ? null : SwingUtilities.getWindowAncestor(parent);
        final JDialog d = new JDialog(owner, yabs.tr("split.dialog.title", item.__getCnumber()),
                JDialog.ModalityType.APPLICATION_MODAL);

        final List<LineInput> lines = HandwerkPlugin.lineInputsOf(item);
        final Map<Integer, LineCostType> overrides = HandwerkStore.loadLineCostTypesSafe(item.__getIDS());

        final DefaultTableModel model = new DefaultTableModel(new Object[]{
            yabs.tr("split.col.desc"),
            yabs.tr("split.col.net"),
            yabs.tr("split.col.type"),
            yabs.tr("split.col.laborShare")
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return col == 2 || col == 3;
            }
        };

        for (LineInput li : lines) {
            LineCostType ov = overrides.get(li.subItemId);
            String type = HandwerkValidator.classifyLine(li, ov);
            BigDecimal share = ov != null && ov.laborSharePercent != null
                    ? ov.laborSharePercent
                    : (HandwerkValidator.isTaxDeductible35a(type) ? new BigDecimal("100.00") : BigDecimal.ZERO);
            model.addRow(new Object[]{
                li.description,
                li.netTotal.setScale(2, RoundingMode.HALF_UP).toPlainString(),
                type,
                share.setScale(2, RoundingMode.HALF_UP).toPlainString()
            });
        }

        final JTable table = new JTable(model);
        JComboBox<String> typeEditor = new JComboBox<String>(new String[]{
            HandwerkStore.COST_LABOR,
            HandwerkStore.COST_TRAVEL,
            HandwerkStore.COST_MACHINE,
            HandwerkStore.COST_MATERIAL,
            HandwerkStore.COST_OTHER
        });
        table.getColumnModel().getColumn(2).setCellEditor(new javax.swing.DefaultCellEditor(typeEditor));

        final JLabel summaryLabel = new JLabel();
        Runnable updateSummary = () -> {
            Map<Integer, LineCostType> tmp = new java.util.HashMap<Integer, LineCostType>();
            for (int i = 0; i < lines.size(); i++) {
                LineCostType ct = new LineCostType();
                ct.itemId = item.__getIDS();
                ct.subItemId = lines.get(i).subItemId;
                ct.costType = String.valueOf(model.getValueAt(i, 2));
                ct.laborSharePercent = HandwerkStore.parseDecimal(String.valueOf(model.getValueAt(i, 3)), BigDecimal.ZERO);
                tmp.put(ct.subItemId, ct);
            }
            LaborSplitSummary sp = HandwerkValidator.computeLaborSplit(lines, tmp, BigDecimal.ZERO);
            summaryLabel.setText(yabs.tr("split.summary",
                    sp.laborNet.toPlainString(), sp.laborGross.toPlainString(),
                    sp.materialGross.toPlainString(), sp.estimatedCredit35a.toPlainString()));
        };
        model.addTableModelListener(e -> updateSummary.run());
        updateSummary.run();

        JButton saveBtn = new JButton(yabs.tr("common.save"));
        saveBtn.addActionListener(e -> {
            if (table.isEditing()) {
                table.getCellEditor().stopCellEditing();
            }
            try {
                for (int i = 0; i < lines.size(); i++) {
                    LineCostType ct = new LineCostType();
                    ct.itemId = item.__getIDS();
                    ct.subItemId = lines.get(i).subItemId;
                    ct.costType = String.valueOf(model.getValueAt(i, 2));
                    ct.laborSharePercent = HandwerkStore.parseDecimal(
                            String.valueOf(model.getValueAt(i, 3)),
                            HandwerkValidator.isTaxDeductible35a(ct.costType) ? new BigDecimal("100.00") : BigDecimal.ZERO);
                    HandwerkStore.saveLineCostType(HandwerkStore.connection(), ct);
                }
                yabs.notify(yabs.tr("split.saved"));
                d.dispose();
            } catch (Exception ex) {
                Log.Debug(HandwerkUi.class, ex);
                yabs.error(ex.getMessage());
            }
        });
        JButton cancelBtn = new JButton(yabs.tr("common.cancel"));
        cancelBtn.addActionListener(e -> d.dispose());

        JPanel south = new JPanel(new BorderLayout(8, 8));
        south.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
        south.add(summaryLabel, BorderLayout.CENTER);
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        btns.add(cancelBtn);
        btns.add(saveBtn);
        south.add(btns, BorderLayout.EAST);

        d.setLayout(new BorderLayout());
        d.add(new JScrollPane(table), BorderLayout.CENTER);
        d.add(south, BorderLayout.SOUTH);
        d.setSize(new Dimension(760, 440));
        d.setLocationRelativeTo(owner);
        d.setVisible(true);
    }

    // --------------------------------------------- BoQ / GAEB pricing dialog

    static void openBoqDialog(Component parent, final Item item, final ExtensionContext yabs) {
        Window owner = parent == null ? null : SwingUtilities.getWindowAncestor(parent);
        final JDialog d = new JDialog(owner, yabs.tr("boq.dialog.title", item.__getCnumber()),
                JDialog.ModalityType.APPLICATION_MODAL);

        final BoqDocument[] boqHolder = {HandwerkStore.loadBoqForItemSafe(item.__getIDS())};
        if (boqHolder[0] != null) {
            HandwerkPlugin.syncBoqPricesFromSubItems(boqHolder[0], item);
        }

        final DefaultTableModel model = new DefaultTableModel(new Object[]{
            yabs.tr("boq.col.oz"),
            yabs.tr("boq.col.type"),
            yabs.tr("boq.col.shortText"),
            yabs.tr("boq.col.qty"),
            yabs.tr("boq.col.unit"),
            yabs.tr("boq.col.unitPrice"),
            yabs.tr("boq.col.total")
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return col == 5;
            }
        };
        final JTable table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        final JTextArea longTextArea = new JTextArea(6, 40);
        longTextArea.setEditable(false);
        longTextArea.setLineWrap(true);
        longTextArea.setWrapStyleWord(true);
        final JTextArea bidderTextArea = new JTextArea(3, 40);
        bidderTextArea.setLineWrap(true);
        bidderTextArea.setWrapStyleWord(true);
        final JLabel totalLabel = new JLabel();

        Runnable reload = () -> {
            model.setRowCount(0);
            BoqDocument boq = boqHolder[0];
            if (boq == null) {
                totalLabel.setText(yabs.tr("boq.none"));
                longTextArea.setText("");
                bidderTextArea.setText("");
                return;
            }
            for (BoqPosition p : boq.positions) {
                model.addRow(new Object[]{
                    p.ordinalNo,
                    p.positionType.name(),
                    p.shortText,
                    p.quantity.stripTrailingZeros().toPlainString(),
                    p.unit,
                    p.unitPrice.setScale(2, RoundingMode.HALF_UP).toPlainString(),
                    p.lineTotal().toPlainString()
                });
            }
            totalLabel.setText(yabs.tr("boq.summary",
                    boq.formatCode, boq.positions.size(), boq.totalNet().toPlainString(), boq.currency,
                    boq.countMandatoryUnpriced()));
        };
        reload.run();

        final int[] lastRow = {-1};
        table.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting() || boqHolder[0] == null) {
                return;
            }
            if (lastRow[0] >= 0 && lastRow[0] < boqHolder[0].positions.size()) {
                boqHolder[0].positions.get(lastRow[0]).bidderText = bidderTextArea.getText().trim();
            }
            int row = table.getSelectedRow();
            lastRow[0] = row;
            if (row >= 0 && row < boqHolder[0].positions.size()) {
                BoqPosition p = boqHolder[0].positions.get(row);
                longTextArea.setText((p.categoryPath.isEmpty() ? "" : "[" + p.categoryPath + "]\n\n") + p.longText);
                bidderTextArea.setText(p.bidderText);
            }
        });

        model.addTableModelListener(e -> {
            if (e.getColumn() == 5 && boqHolder[0] != null) {
                int row = e.getFirstRow();
                if (row >= 0 && row < boqHolder[0].positions.size()) {
                    BoqPosition p = boqHolder[0].positions.get(row);
                    p.unitPrice = HandwerkStore.parseDecimal(String.valueOf(model.getValueAt(row, 5)), BigDecimal.ZERO);
                    model.setValueAt(p.lineTotal().toPlainString(), row, 6);
                    totalLabel.setText(yabs.tr("boq.summary",
                            boqHolder[0].formatCode, boqHolder[0].positions.size(),
                            boqHolder[0].totalNet().toPlainString(), boqHolder[0].currency,
                            boqHolder[0].countMandatoryUnpriced()));
                }
            }
        });

        JButton importBtn = new JButton(yabs.tr("boq.btn.import"));
        importBtn.addActionListener(e -> {
            d.dispose();
            HandwerkPlugin.importBoqIntoItem(item, yabs);
        });

        JButton exportBtn = new JButton(yabs.tr("boq.btn.export"));
        exportBtn.addActionListener(e -> {
            if (table.isEditing()) {
                table.getCellEditor().stopCellEditing();
            }
            if (boqHolder[0] != null) {
                if (lastRow[0] >= 0 && lastRow[0] < boqHolder[0].positions.size()) {
                    boqHolder[0].positions.get(lastRow[0]).bidderText = bidderTextArea.getText().trim();
                }
                try {
                    HandwerkPlugin.applyBoqPricesToSubItems(boqHolder[0], item);
                    HandwerkStore.saveBoqDocument(HandwerkStore.connection(), boqHolder[0]);
                } catch (Exception ex) {
                    Log.Debug(HandwerkUi.class, ex);
                }
            }
            HandwerkPlugin.exportBoqFromItem(item, yabs);
        });

        JButton saveBtn = new JButton(yabs.tr("common.save"));
        saveBtn.addActionListener(e -> {
            if (table.isEditing()) {
                table.getCellEditor().stopCellEditing();
            }
            if (boqHolder[0] != null) {
                if (lastRow[0] >= 0 && lastRow[0] < boqHolder[0].positions.size()) {
                    boqHolder[0].positions.get(lastRow[0]).bidderText = bidderTextArea.getText().trim();
                }
                try {
                    HandwerkPlugin.applyBoqPricesToSubItems(boqHolder[0], item);
                    HandwerkStore.saveBoqDocument(HandwerkStore.connection(), boqHolder[0]);
                    yabs.refreshView(item);
                    yabs.notify(yabs.tr("boq.saved"));
                } catch (Exception ex) {
                    Log.Debug(HandwerkUi.class, ex);
                    yabs.error(ex.getMessage());
                }
            }
            d.dispose();
        });

        JButton closeBtn = new JButton(yabs.tr("common.close"));
        closeBtn.addActionListener(e -> d.dispose());

        JPanel detailPanel = new JPanel(new BorderLayout(6, 6));
        JPanel longPanel = new JPanel(new BorderLayout());
        longPanel.setBorder(BorderFactory.createTitledBorder(yabs.tr("boq.longText")));
        longPanel.add(new JScrollPane(longTextArea), BorderLayout.CENTER);
        JPanel bidderPanel = new JPanel(new BorderLayout());
        bidderPanel.setBorder(BorderFactory.createTitledBorder(yabs.tr("boq.bidderText")));
        bidderPanel.add(new JScrollPane(bidderTextArea), BorderLayout.CENTER);
        detailPanel.add(longPanel, BorderLayout.CENTER);
        detailPanel.add(bidderPanel, BorderLayout.SOUTH);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, new JScrollPane(table), detailPanel);
        splitPane.setDividerLocation(260);

        JPanel south = new JPanel(new BorderLayout(8, 8));
        south.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
        south.add(totalLabel, BorderLayout.CENTER);
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        btns.add(importBtn);
        btns.add(exportBtn);
        btns.add(saveBtn);
        btns.add(closeBtn);
        south.add(btns, BorderLayout.EAST);

        d.setLayout(new BorderLayout());
        d.add(splitPane, BorderLayout.CENTER);
        d.add(south, BorderLayout.SOUTH);
        d.setSize(new Dimension(920, 600));
        d.setLocationRelativeTo(owner);
        d.setVisible(true);
    }

    // ----------------------------------------- site measurements (Aufmaß)

    static void openMeasurementsDialog(Component parent, final Item item, final ExtensionContext yabs) {
        Window owner = parent == null ? null : SwingUtilities.getWindowAncestor(parent);
        final JDialog d = new JDialog(owner, yabs.tr("meas.dialog.title", item.__getCnumber()),
                JDialog.ModalityType.APPLICATION_MODAL);

        final ItemMeta im = HandwerkStore.loadItemMetaSafe(item.__getIDS());
        final DefaultTableModel model = new DefaultTableModel(new Object[]{
            yabs.tr("meas.col.date"),
            yabs.tr("meas.col.oz"),
            yabs.tr("meas.col.desc"),
            yabs.tr("meas.col.formula"),
            yabs.tr("meas.col.qty"),
            yabs.tr("meas.col.unit")
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        final JTable table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        @SuppressWarnings("unchecked")
        final List<Measurement>[] holder = new List[1];
        final JLabel sumLabel = new JLabel();

        Runnable reload = () -> {
            model.setRowCount(0);
            holder[0] = HandwerkStore.loadMeasurementsForItemSafe(item.__getIDS());
            BigDecimal sum = BigDecimal.ZERO;
            for (Measurement m : holder[0]) {
                sum = sum.add(m.computedQty);
                model.addRow(new Object[]{
                    m.workDate, m.ordinalNo, m.description, m.formula,
                    m.computedQty.stripTrailingZeros().toPlainString(), m.unit
                });
            }
            sumLabel.setText(yabs.tr("meas.sum", sum.setScale(3, RoundingMode.HALF_UP).toPlainString()));
        };
        reload.run();

        final JTextField ozField = new JTextField(8);
        final JTextField descField = new JTextField(18);
        final JTextField formulaField = new JTextField(18);
        final JTextField unitField = new JTextField("m2", 6);
        final JLabel previewLabel = new JLabel("= 0");

        JButton evalBtn = new JButton("=");
        evalBtn.addActionListener(e -> {
            try {
                BigDecimal val = MeasurementEvaluator.evaluate(formulaField.getText());
                previewLabel.setText("= " + val.stripTrailingZeros().toPlainString());
            } catch (Exception ex) {
                previewLabel.setText("ERR: " + ex.getMessage());
            }
        });

        JButton addBtn = new JButton(yabs.tr("common.add"));
        addBtn.addActionListener(e -> {
            try {
                BigDecimal val = MeasurementEvaluator.evaluate(formulaField.getText());
                Measurement m = new Measurement();
                m.siteId = im.siteId;
                m.itemId = item.__getIDS();
                m.workDate = LocalDate.now().toString();
                m.ordinalNo = ozField.getText().trim();
                m.description = descField.getText().trim();
                m.formula = formulaField.getText().trim();
                m.computedQty = val;
                m.unit = unitField.getText().trim();
                HandwerkStore.saveMeasurement(HandwerkStore.connection(), m);
                descField.setText("");
                formulaField.setText("");
                previewLabel.setText("= 0");
                reload.run();
            } catch (Exception ex) {
                yabs.error(yabs.tr("meas.formula.invalid", ex.getMessage()));
            }
        });

        JButton delBtn = new JButton(yabs.tr("common.delete"));
        delBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0 && row < holder[0].size()) {
                try {
                    HandwerkStore.deleteMeasurement(HandwerkStore.connection(), holder[0].get(row).id);
                    reload.run();
                } catch (Exception ex) {
                    yabs.error(ex.getMessage());
                }
            }
        });

        JButton applyBtn = new JButton(yabs.tr("meas.applyToSubItem"));
        applyBtn.addActionListener(e -> {
            SubItem[] subs = item.getSubitems();
            if (subs == null || subs.length == 0 || holder[0].isEmpty()) {
                return;
            }
            String[] labels = new String[subs.length];
            for (int i = 0; i < subs.length; i++) {
                labels[i] = (i + 1) + ". " + subs[i].__getDescription();
            }
            Object picked = JOptionPane.showInputDialog(d, yabs.tr("meas.pickSubItem"),
                    yabs.tr("meas.applyToSubItem"), JOptionPane.PLAIN_MESSAGE, null, labels, labels[0]);
            if (picked == null) {
                return;
            }
            int idx = -1;
            for (int i = 0; i < labels.length; i++) {
                if (labels[i].equals(picked)) {
                    idx = i;
                    break;
                }
            }
            if (idx >= 0) {
                BigDecimal total = BigDecimal.ZERO;
                for (Measurement m : holder[0]) {
                    total = total.add(m.computedQty);
                }
                SubItem target = subs[idx];
                target.setQuantityvalue(total.setScale(3, RoundingMode.HALF_UP));
                SubItem.calculate(target);
                target.save(true);
                ItemService.updateTotals(item);
                item.save(true);
                yabs.refreshView(item);
                yabs.notify(yabs.tr("meas.applied", total.setScale(3, RoundingMode.HALF_UP).toPlainString()));
            }
        });

        JPanel addBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 4));
        addBar.add(new JLabel(yabs.tr("meas.col.oz") + ":"));
        addBar.add(ozField);
        addBar.add(new JLabel(yabs.tr("meas.col.desc") + ":"));
        addBar.add(descField);
        addBar.add(new JLabel(yabs.tr("meas.col.formula") + ":"));
        addBar.add(formulaField);
        addBar.add(evalBtn);
        addBar.add(previewLabel);
        addBar.add(unitField);
        addBar.add(addBtn);

        JButton closeBtn = new JButton(yabs.tr("common.close"));
        closeBtn.addActionListener(e -> d.dispose());

        JPanel south = new JPanel(new BorderLayout(8, 6));
        south.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
        south.add(sumLabel, BorderLayout.CENTER);
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        btns.add(delBtn);
        btns.add(applyBtn);
        btns.add(closeBtn);
        south.add(btns, BorderLayout.EAST);

        d.setLayout(new BorderLayout(6, 6));
        d.add(addBar, BorderLayout.NORTH);
        d.add(new JScrollPane(table), BorderLayout.CENTER);
        d.add(south, BorderLayout.SOUTH);
        d.setSize(new Dimension(920, 480));
        d.setLocationRelativeTo(owner);
        d.setVisible(true);
    }

    // ----------------------------------------- time sheets (Regieberichte)

    static void openTimeSheetsDialog(Component parent, final Item item, final ExtensionContext yabs) {
        Window owner = parent == null ? null : SwingUtilities.getWindowAncestor(parent);
        final JDialog d = new JDialog(owner, yabs.tr("ts.dialog.title", item.__getCnumber()),
                JDialog.ModalityType.APPLICATION_MODAL);

        final ItemMeta im = HandwerkStore.loadItemMetaSafe(item.__getIDS());
        final DefaultTableModel model = new DefaultTableModel(new Object[]{
            yabs.tr("ts.col.date"),
            yabs.tr("ts.col.worker"),
            yabs.tr("ts.col.role"),
            yabs.tr("ts.col.hours"),
            yabs.tr("ts.col.km"),
            yabs.tr("ts.col.desc"),
            yabs.tr("ts.col.signedBy"),
            yabs.tr("ts.col.billed")
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        final JTable table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        @SuppressWarnings("unchecked")
        final List<TimeSheet>[] holder = new List[1];

        Runnable reload = () -> {
            model.setRowCount(0);
            holder[0] = HandwerkStore.loadTimeSheetsForItemSafe(item.__getIDS());
            for (TimeSheet t : holder[0]) {
                model.addRow(new Object[]{
                    t.workDate, t.workerName, t.workerRole,
                    t.hours.toPlainString(), t.travelKm.toPlainString(),
                    t.description, t.signedBy,
                    t.billed ? yabs.tr("common.yes") : yabs.tr("common.no")
                });
            }
        };
        reload.run();

        final JTextField dateField = new JTextField(LocalDate.now().toString(), 9);
        final JTextField workerField = new JTextField(12);
        final JComboBox<String> roleBox = new JComboBox<String>(new String[]{
            HandwerkStore.ROLE_JOURNEYMAN, HandwerkStore.ROLE_MASTER, HandwerkStore.ROLE_APPRENTICE
        });
        final JTextField hoursField = new JTextField("1.0", 5);
        final JTextField kmField = new JTextField("0", 5);
        final JTextField descField = new JTextField(16);
        final JTextField signedField = new JTextField(10);

        JButton addBtn = new JButton(yabs.tr("common.add"));
        addBtn.addActionListener(e -> {
            TimeSheet t = new TimeSheet();
            t.siteId = im.siteId;
            t.itemId = item.__getIDS();
            t.workDate = dateField.getText().trim();
            t.workerName = workerField.getText().trim();
            t.workerRole = String.valueOf(roleBox.getSelectedItem());
            t.hours = HandwerkStore.parseDecimal(hoursField.getText(), BigDecimal.ZERO);
            t.travelKm = HandwerkStore.parseDecimal(kmField.getText(), BigDecimal.ZERO);
            t.description = descField.getText().trim();
            t.signedBy = signedField.getText().trim();
            try {
                HandwerkStore.saveTimeSheet(HandwerkStore.connection(), t);
                descField.setText("");
                reload.run();
            } catch (Exception ex) {
                yabs.error(ex.getMessage());
            }
        });

        JButton delBtn = new JButton(yabs.tr("common.delete"));
        delBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0 && row < holder[0].size()) {
                try {
                    HandwerkStore.deleteTimeSheet(HandwerkStore.connection(), holder[0].get(row).id);
                    reload.run();
                } catch (Exception ex) {
                    yabs.error(ex.getMessage());
                }
            }
        });

        JButton transferBtn = new JButton(yabs.tr("ts.transferBtn"));
        transferBtn.addActionListener(e -> {
            int added = HandwerkPlugin.transferTimeSheetsToItem(item, holder[0], Settings.load(), yabs);
            reload.run();
            yabs.notify(yabs.tr("ts.transferred", added));
        });

        JButton closeBtn = new JButton(yabs.tr("common.close"));
        closeBtn.addActionListener(e -> d.dispose());

        JPanel addBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 4));
        addBar.add(dateField);
        addBar.add(new JLabel(yabs.tr("ts.col.worker") + ":"));
        addBar.add(workerField);
        addBar.add(roleBox);
        addBar.add(new JLabel(yabs.tr("ts.col.hours") + ":"));
        addBar.add(hoursField);
        addBar.add(new JLabel("km:"));
        addBar.add(kmField);
        addBar.add(new JLabel(yabs.tr("ts.col.desc") + ":"));
        addBar.add(descField);
        addBar.add(new JLabel(yabs.tr("ts.col.signedBy") + ":"));
        addBar.add(signedField);
        addBar.add(addBtn);

        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 6));
        south.add(delBtn);
        south.add(transferBtn);
        south.add(closeBtn);

        d.setLayout(new BorderLayout(6, 6));
        d.add(addBar, BorderLayout.NORTH);
        d.add(new JScrollPane(table), BorderLayout.CENTER);
        d.add(south, BorderLayout.SOUTH);
        d.setSize(new Dimension(980, 480));
        d.setLocationRelativeTo(owner);
        d.setVisible(true);
    }

    // --------------------------------------------------- 4-tab overview

    static void openOverviewTab(final ExtensionContext yabs) {
        if (!HandwerkStore.hasDatabase()) {
            yabs.error(yabs.tr("overview.noDb"));
            return;
        }
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab(yabs.tr("overview.tab.sites"), buildSitesTab(yabs));
        tabs.addTab(yabs.tr("overview.tab.certs"), buildCertificatesTab(yabs));
        tabs.addTab(yabs.tr("overview.tab.warranties"), buildWarrantiesTab(yabs));
        tabs.addTab(yabs.tr("overview.tab.maintenance"), buildMaintenanceTab(yabs));
        yabs.openTab(yabs.tr("overview.title"), tabs);
    }

    private static JComponent buildSitesTab(ExtensionContext yabs) {
        DefaultTableModel model = new DefaultTableModel(new Object[]{
            yabs.tr("site.col.projectNo"),
            yabs.tr("site.col.name"),
            yabs.tr("site.col.address"),
            yabs.tr("site.col.contract"),
            yabs.tr("site.acceptanceDate"),
            yabs.tr("site.col.warrantyUntil")
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        try {
            for (Site s : HandwerkStore.loadAllSites(HandwerkStore.connection(), false)) {
                model.addRow(new Object[]{
                    s.projectNo, s.siteName, s.formattedAddress(),
                    s.contractType, s.acceptanceDate, s.warrantyUntil
                });
            }
        } catch (Exception e) {
            Log.Debug(HandwerkUi.class, e);
        }
        return new JScrollPane(new JTable(model));
    }

    private static JComponent buildCertificatesTab(ExtensionContext yabs) {
        DefaultTableModel model = new DefaultTableModel(new Object[]{
            yabs.tr("overview.cert.holder"),
            yabs.tr("overview.cert.type"),
            yabs.tr("overview.cert.number"),
            yabs.tr("overview.cert.validUntil"),
            yabs.tr("overview.cert.status")
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        LocalDate today = LocalDate.now();
        Settings cfg = Settings.load();
        if (!cfg.ownFreistellungValidUntil.isEmpty()) {
            model.addRow(new Object[]{
                yabs.tr("overview.cert.own"),
                "\u00a7 48b EStG Freistellung",
                cfg.ownFreistellungTaxNo + " / " + cfg.ownFreistellungSecNo,
                cfg.ownFreistellungValidUntil,
                certStatus(cfg.ownFreistellungValidUntil, cfg.certLeadDays, today, yabs)
            });
        }
        if (!cfg.ownUst1TgValidUntil.isEmpty()) {
            model.addRow(new Object[]{
                yabs.tr("overview.cert.own"),
                "USt 1 TG (\u00a7 13b UStG)",
                "",
                cfg.ownUst1TgValidUntil,
                certStatus(cfg.ownUst1TgValidUntil, cfg.certLeadDays, today, yabs)
            });
        }
        try {
            for (ContactMeta cm : HandwerkStore.loadAllContactMetas(HandwerkStore.connection())) {
                if (!cm.reverseChargeCertUntil.isEmpty()) {
                    model.addRow(new Object[]{
                        "Contact #" + cm.contactId,
                        "USt 1 TG (\u00a7 13b UStG)",
                        cm.reverseChargeCertNo,
                        cm.reverseChargeCertUntil,
                        certStatus(cm.reverseChargeCertUntil, cfg.certLeadDays, today, yabs)
                    });
                }
                if (!cm.withholdingExemptionUntil.isEmpty()) {
                    model.addRow(new Object[]{
                        "Contact #" + cm.contactId,
                        "\u00a7 48b EStG Freistellung",
                        cm.withholdingExemptionTaxNo + " / " + cm.withholdingExemptionSecNo,
                        cm.withholdingExemptionUntil,
                        certStatus(cm.withholdingExemptionUntil, cfg.certLeadDays, today, yabs)
                    });
                }
            }
        } catch (Exception e) {
            Log.Debug(HandwerkUi.class, e);
        }
        return new JScrollPane(new JTable(model));
    }

    private static JComponent buildWarrantiesTab(ExtensionContext yabs) {
        DefaultTableModel model = new DefaultTableModel(new Object[]{
            yabs.tr("overview.war.ref"),
            yabs.tr("overview.war.kind"),
            yabs.tr("overview.war.dueDate"),
            yabs.tr("overview.war.amount"),
            yabs.tr("overview.war.bond")
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        try {
            for (Site s : HandwerkStore.loadAllSites(HandwerkStore.connection(), false)) {
                if (!s.warrantyUntil.isEmpty()) {
                    model.addRow(new Object[]{
                        s.displayLabel(),
                        s.contractType + " (" + s.warrantyYears + "y)",
                        s.warrantyUntil,
                        s.retentionPercent.signum() > 0 ? s.retentionPercent.toPlainString() + " %" : "",
                        ""
                    });
                }
            }
            for (ItemMeta im : HandwerkStore.loadAllItemMetasWithRetention(HandwerkStore.connection())) {
                model.addRow(new Object[]{
                    "Item #" + im.itemId + (im.siteSnapshot.isEmpty() ? "" : " \u2013 " + im.siteSnapshot),
                    yabs.tr("overview.war.retention"),
                    im.retentionReleaseDate,
                    im.retentionAmount.toPlainString() + " (" + im.retentionPercent.toPlainString() + " %)",
                    im.retentionBondRef
                });
            }
        } catch (Exception e) {
            Log.Debug(HandwerkUi.class, e);
        }
        return new JScrollPane(new JTable(model));
    }

    private static JComponent buildMaintenanceTab(ExtensionContext yabs) {
        DefaultTableModel model = new DefaultTableModel(new Object[]{
            yabs.tr("site.col.projectNo"),
            yabs.tr("site.col.name"),
            yabs.tr("site.col.address"),
            yabs.tr("site.col.maintenanceDue"),
            yabs.tr("overview.cert.status")
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        LocalDate today = LocalDate.now();
        int leadDays = Settings.load().certLeadDays;
        try {
            List<Site> dueSites = new ArrayList<Site>();
            for (Site s : HandwerkStore.loadAllSites(HandwerkStore.connection(), false)) {
                if (!s.maintenanceDue.isEmpty()) {
                    dueSites.add(s);
                }
            }
            dueSites.sort((a, b) -> a.maintenanceDue.compareTo(b.maintenanceDue));
            for (Site s : dueSites) {
                model.addRow(new Object[]{
                    s.projectNo, s.siteName, s.formattedAddress(), s.maintenanceDue,
                    certStatus(s.maintenanceDue, leadDays, today, yabs)
                });
            }
        } catch (Exception e) {
            Log.Debug(HandwerkUi.class, e);
        }
        return new JScrollPane(new JTable(model));
    }

    // --------------------------------------------------- validation popup

    static void showValidationResult(Component parent, List<String> warnings, ExtensionContext yabs) {
        if (warnings == null || warnings.isEmpty()) {
            JOptionPane.showMessageDialog(parent, yabs.tr("val.ok"),
                    yabs.tr("val.title"), JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        StringBuilder sb = new StringBuilder(yabs.tr("val.header")).append("\n\n");
        for (String w : warnings) {
            sb.append("\u2022 ").append(w).append("\n");
        }
        JOptionPane.showMessageDialog(parent, sb.toString(),
                yabs.tr("val.title"), JOptionPane.WARNING_MESSAGE);
    }

    // --------------------------------------------------------------- helpers

    private static String certStatus(String validUntil, int leadDays, LocalDate today, ExtensionContext yabs) {
        if (HandwerkValidator.isCertificateExpired(validUntil, today)) {
            return yabs.tr("status.expired");
        }
        if (HandwerkValidator.isCertificateExpiringSoon(validUntil, leadDays, today)) {
            return yabs.tr("status.expiringSoon");
        }
        return yabs.tr("status.valid");
    }

    private static String customerKindLabel(String kind, ExtensionContext yabs) {
        if (HandwerkStore.CUSTOMER_COMMERCIAL.equalsIgnoreCase(kind)) {
            return yabs.tr("kind.commercial");
        }
        if (HandwerkStore.CUSTOMER_CONSTRUCTION_13B.equalsIgnoreCase(kind)) {
            return yabs.tr("kind.construction13b");
        }
        if (HandwerkStore.CUSTOMER_PUBLIC.equalsIgnoreCase(kind)) {
            return yabs.tr("kind.public");
        }
        return yabs.tr("kind.private");
    }

    private static GridBagConstraints gbc() {
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(4, 6, 4, 6);
        g.anchor = GridBagConstraints.WEST;
        g.fill = GridBagConstraints.HORIZONTAL;
        return g;
    }

    private static void addRow(JPanel panel, GridBagConstraints g, int row, String label, Component comp) {
        g.gridx = 0;
        g.gridy = row;
        g.gridwidth = 1;
        g.weightx = 0;
        panel.add(new JLabel(label), g);
        g.gridx = 1;
        g.weightx = 1;
        panel.add(comp, g);
    }
}
