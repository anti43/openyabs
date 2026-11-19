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
package org.openyabs.workshop;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.time.LocalDate;
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
import mpv5.globals.Constants;
import mpv5.logging.Log;
import mpv5.plugin.api.ExtensionContext;
import mpv5.ui.panels.DataPanel;
import org.openyabs.workshop.WorkshopStore.JobData;
import org.openyabs.workshop.WorkshopStore.Settings;
import org.openyabs.workshop.WorkshopStore.TireStorage;
import org.openyabs.workshop.WorkshopStore.Vehicle;

/**
 * Localized Swing UI for the Car Repair Shop / Kfz-Werkstatt plugin: settings
 * page, Contact/Item banners, vehicle/tire/job/history dialogs, and overview
 * tab.
 */
final class WorkshopUi {

    private static final Color BANNER_BG = new Color(240, 253, 244);
    private static final Color BANNER_BORDER = new Color(134, 239, 172);
    private static final Color BANNER_FG = new Color(22, 101, 52);

    private WorkshopUi() {
    }

    static JComponent settingsPage(final ExtensionContext yabs) {
        final Settings s = Settings.load();

        final JComboBox<String> unitBox = new JComboBox<String>(new String[]{"km", "mi"});
        unitBox.setSelectedItem(s.distanceUnit);
        final JTextField awField = new JTextField(String.valueOf(s.awPerHour), 8);
        final JTextField mechField = new JTextField(s.rateMechanical.toPlainString(), 10);
        final JTextField elecField = new JTextField(s.rateElectrical.toPlainString(), 10);
        final JTextField bodyField = new JTextField(s.rateBodywork.toPlainString(), 10);
        final JTextField leadField = new JTextField(String.valueOf(s.inspectionLeadDays), 8);
        final JTextField overrunField = new JTextField(String.valueOf(s.quoteOverrunPercent), 8);
        final JCheckBox warnBox = new JCheckBox(yabs.tr("settings.warnOnSave"), s.warnOnSave);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(5, 6, 5, 6);
        g.anchor = GridBagConstraints.WEST;
        g.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;
        addRow(form, g, row++, yabs.tr("settings.distanceUnit"), unitBox);
        addRow(form, g, row++, yabs.tr("settings.awPerHour"), awField);
        addRow(form, g, row++, yabs.tr("settings.rateMechanical"), mechField);
        addRow(form, g, row++, yabs.tr("settings.rateElectrical"), elecField);
        addRow(form, g, row++, yabs.tr("settings.rateBodywork"), bodyField);
        addRow(form, g, row++, yabs.tr("settings.inspectionLeadDays"), leadField);
        addRow(form, g, row++, yabs.tr("settings.quoteOverrunPercent"), overrunField);

        g.gridx = 0;
        g.gridy = row++;
        g.gridwidth = 2;
        form.add(warnBox, g);

        g.gridy = row++;
        g.insets = new Insets(12, 6, 6, 6);
        form.add(new JLabel(yabs.tr("settings.info")), g);

        JButton saveBtn = new JButton(yabs.tr("common.save"));
        saveBtn.addActionListener(e -> {
            s.distanceUnit = String.valueOf(unitBox.getSelectedItem());
            s.awPerHour = WorkshopStore.parseInt(awField.getText(), s.awPerHour);
            s.rateMechanical = WorkshopStore.parseDecimal(mechField.getText(), s.rateMechanical);
            s.rateElectrical = WorkshopStore.parseDecimal(elecField.getText(), s.rateElectrical);
            s.rateBodywork = WorkshopStore.parseDecimal(bodyField.getText(), s.rateBodywork);
            s.inspectionLeadDays = WorkshopStore.parseInt(leadField.getText(), s.inspectionLeadDays);
            s.quoteOverrunPercent = WorkshopStore.parseInt(overrunField.getText(), s.quoteOverrunPercent);
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

    static void decorate(final DataPanel panel, final ExtensionContext yabs) {
        if (panel == null) {
            return;
        }
        DatabaseObject owner = panel.getDataOwner();
        if (owner instanceof Contact && owner.__getIDS() > 0) {
            decorateContact(panel, (Contact) owner, yabs);
        } else if (owner instanceof Item && owner.__getIDS() > 0 && isWorkshopDocType(((Item) owner).__getInttype())) {
            decorateItem(panel, (Item) owner, yabs);
        }
    }

    static boolean isWorkshopDocType(int type) {
        return type == Constants.TYPE_OFFER
                || type == Constants.TYPE_ORDER
                || type == Constants.TYPE_INVOICE
                || type == Constants.TYPE_CREDIT
                || type == Constants.TYPE_DEPOSIT
                || type == Constants.TYPE_PART_PAYMENT;
    }

    private static void decorateContact(final DataPanel panel, final Contact contact, final ExtensionContext yabs) {
        List<Vehicle> active = WorkshopStore.loadVehiclesForContactSafe(contact.__getIDS(), false);
        String summary;
        if (active.isEmpty()) {
            summary = yabs.tr("banner.contact.empty");
        } else {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < Math.min(active.size(), 3); i++) {
                if (i > 0) {
                    sb.append(", ");
                }
                sb.append(active.get(i).displayLabel());
            }
            if (active.size() > 3) {
                sb.append(" (+").append(active.size() - 3).append(")");
            }
            summary = yabs.tr("banner.contact", sb.toString());
        }
        JButton manageBtn = new JButton(yabs.tr("banner.contact.btn"));
        manageBtn.addActionListener(e -> {
            openVehiclesDialog(panel instanceof Component ? (Component) panel : null, contact, yabs);
            decorateContact(panel, contact, yabs);
        });
        panel.setPluginBanner(banner(summary, manageBtn));
    }

    private static void decorateItem(final DataPanel panel, final Item item, final ExtensionContext yabs) {
        JobData j = WorkshopStore.loadJobSafe(item.__getIDS());
        Vehicle v = WorkshopStore.loadVehicleSafe(j.vehicleId);
        String summary;
        if (j.vehicleId <= 0 && j.plateSnapshot.isEmpty()) {
            summary = yabs.tr("banner.job.empty");
        } else {
            String plate = !j.plateSnapshot.isEmpty() ? j.plateSnapshot : (v == null ? "" : v.licensePlate);
            String model = v == null ? "" : v.makeModel;
            StringBuilder sb = new StringBuilder(plate);
            if (!model.isEmpty()) {
                sb.append(" (").append(model).append(")");
            }
            if (j.mileage > 0) {
                sb.append(" \u00b7 ").append(j.mileage).append(" ").append(j.mileageUnit);
            }
            summary = yabs.tr("banner.job", sb.toString());
        }

        JButton editBtn = new JButton(yabs.tr("banner.job.btn"));
        editBtn.addActionListener(e -> {
            if (editJobDialog(panel instanceof Component ? (Component) panel : null, item, yabs)) {
                decorateItem(panel, item, yabs);
            }
        });
        JButton checkBtn = new JButton(yabs.tr("banner.job.check"));
        checkBtn.addActionListener(e -> {
            List<String> warnings = WorkshopPlugin.validateItem(item, yabs);
            showValidationResult(panel instanceof Component ? (Component) panel : null, warnings, yabs);
        });
        panel.setPluginBanner(banner(summary, editBtn, checkBtn));
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

    // ------------------------------------------------------------- vehicle dialog

    static void openVehiclesDialog(Component parent, final Contact contact, final ExtensionContext yabs) {
        Window owner = parent == null ? null : SwingUtilities.getWindowAncestor(parent);
        final JDialog dialog = new JDialog(owner, yabs.tr("vehicle.dialog.title", contact.__getCname()), JDialog.ModalityType.APPLICATION_MODAL);

        final DefaultTableModel model = new DefaultTableModel(new Object[]{
            yabs.tr("vehicle.col.plate"),
            yabs.tr("vehicle.col.makeModel"),
            yabs.tr("vehicle.col.vin"),
            yabs.tr("vehicle.col.mileage"),
            yabs.tr("vehicle.col.inspection"),
            yabs.tr("vehicle.col.status")
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        final JTable table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        final List<Vehicle>[] holder = new List[1];

        Runnable reload = () -> {
            model.setRowCount(0);
            holder[0] = WorkshopStore.loadVehiclesForContactSafe(contact.__getIDS(), true);
            for (Vehicle v : holder[0]) {
                model.addRow(new Object[]{
                    v.licensePlate,
                    v.makeModel,
                    v.vin,
                    v.mileage > 0 ? v.mileage + " " + v.mileageUnit : "",
                    v.inspectionDue,
                    v.archived ? yabs.tr("vehicle.status.archived") : yabs.tr("vehicle.status.active")
                });
            }
        };
        reload.run();

        JButton addBtn = new JButton(yabs.tr("common.add"));
        addBtn.addActionListener(e -> {
            Vehicle v = new Vehicle();
            v.contactId = contact.__getIDS();
            v.mileageUnit = Settings.load().distanceUnit;
            if (editVehicleDialog(dialog, v, yabs)) {
                reload.run();
            }
        });

        JButton editBtn = new JButton(yabs.tr("common.edit"));
        editBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(dialog, yabs.tr("vehicle.selectFirst"));
                return;
            }
            if (editVehicleDialog(dialog, holder[0].get(row), yabs)) {
                reload.run();
            }
        });

        JButton delBtn = new JButton(yabs.tr("common.delete"));
        delBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(dialog, yabs.tr("vehicle.selectFirst"));
                return;
            }
            Vehicle v = holder[0].get(row);
            int choice = JOptionPane.showConfirmDialog(dialog, yabs.tr("vehicle.confirmDelete", v.displayLabel()),
                    yabs.tr("common.delete"), JOptionPane.YES_NO_OPTION);
            if (choice == JOptionPane.YES_OPTION) {
                try {
                    WorkshopStore.deleteVehicle(WorkshopStore.connection(), v.id);
                    reload.run();
                } catch (Exception ex) {
                    Log.Debug(WorkshopUi.class, ex);
                    JOptionPane.showMessageDialog(dialog, yabs.tr("common.saveFailed", ex.getMessage()),
                            yabs.tr("common.error"), JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        JButton tireBtn = new JButton(yabs.tr("vehicle.btn.tires"));
        tireBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(dialog, yabs.tr("vehicle.selectFirst"));
                return;
            }
            editTireStorageDialog(dialog, holder[0].get(row), yabs);
        });

        JButton histBtn = new JButton(yabs.tr("vehicle.btn.history"));
        histBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(dialog, yabs.tr("vehicle.selectFirst"));
                return;
            }
            openHistoryDialog(dialog, holder[0].get(row), yabs);
        });

        JButton closeBtn = new JButton(yabs.tr("common.close"));
        closeBtn.addActionListener(e -> dialog.dispose());

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 8));
        btns.add(addBtn);
        btns.add(editBtn);
        btns.add(delBtn);
        btns.add(tireBtn);
        btns.add(histBtn);
        btns.add(closeBtn);

        JScrollPane sp = new JScrollPane(table);
        sp.setPreferredSize(new Dimension(760, 260));
        JPanel root = new JPanel(new BorderLayout(8, 8));
        root.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        root.add(sp, BorderLayout.CENTER);
        root.add(btns, BorderLayout.SOUTH);
        dialog.setContentPane(root);
        dialog.pack();
        dialog.setLocationRelativeTo(owner);
        dialog.setVisible(true);
    }

    private static boolean editVehicleDialog(Component parent, final Vehicle v, final ExtensionContext yabs) {
        Window owner = parent instanceof Window ? (Window) parent : SwingUtilities.getWindowAncestor(parent);
        final JDialog dialog = new JDialog(owner, yabs.tr("vehicle.edit.title"), JDialog.ModalityType.APPLICATION_MODAL);
        final boolean[] saved = {false};

        JTextField plate = new JTextField(v.licensePlate, 18);
        JTextField vin = new JTextField(v.vin, 22);
        JTextField model = new JTextField(v.makeModel, 28);
        JTextField hsn = new JTextField(v.hsnTsn, 14);
        JTextField reg = new JTextField(v.firstRegistration, 14);
        JTextField mileage = new JTextField(v.mileage > 0 ? String.valueOf(v.mileage) : "", 12);
        JComboBox<String> unit = new JComboBox<String>(new String[]{"km", "mi"});
        unit.setSelectedItem(v.mileageUnit);
        JTextField insp = new JTextField(v.inspectionDue, 14);
        JTextField tireSize = new JTextField(v.tireSize, 18);
        JCheckBox archived = new JCheckBox(yabs.tr("vehicle.field.archived"), v.archived);
        JTextArea notes = new JTextArea(v.notes, 4, 32);
        notes.setLineWrap(true);
        notes.setWrapStyleWord(true);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(4, 6, 4, 6);
        g.anchor = GridBagConstraints.WEST;
        g.fill = GridBagConstraints.HORIZONTAL;
        int row = 0;
        addRow(form, g, row++, yabs.tr("vehicle.field.plate"), plate);
        addRow(form, g, row++, yabs.tr("vehicle.field.vin"), vin);
        addRow(form, g, row++, yabs.tr("vehicle.field.makeModel"), model);
        addRow(form, g, row++, yabs.tr("vehicle.field.hsnTsn"), hsn);
        addRow(form, g, row++, yabs.tr("vehicle.field.firstReg"), reg);
        addRow(form, g, row++, yabs.tr("vehicle.field.mileage"), mileage);
        addRow(form, g, row++, yabs.tr("vehicle.field.unit"), unit);
        addRow(form, g, row++, yabs.tr("vehicle.field.inspection"), insp);
        addRow(form, g, row++, yabs.tr("vehicle.field.tireSize"), tireSize);
        g.gridx = 0;
        g.gridy = row++;
        g.gridwidth = 2;
        form.add(archived, g);
        addRow(form, g, row++, yabs.tr("vehicle.field.notes"), new JScrollPane(notes));

        JButton cancel = new JButton(yabs.tr("common.cancel"));
        cancel.addActionListener(e -> dialog.dispose());
        JButton ok = new JButton(yabs.tr("common.save"));
        ok.addActionListener(e -> {
            v.licensePlate = plate.getText().trim();
            v.vin = vin.getText().trim().toUpperCase();
            v.makeModel = model.getText().trim();
            v.hsnTsn = hsn.getText().trim();
            v.firstRegistration = reg.getText().trim();
            v.mileage = parseLong(mileage.getText(), 0L);
            v.mileageUnit = String.valueOf(unit.getSelectedItem());
            v.inspectionDue = insp.getText().trim();
            v.tireSize = tireSize.getText().trim();
            v.archived = archived.isSelected();
            v.notes = notes.getText().trim();
            try {
                WorkshopStore.saveVehicle(WorkshopStore.connection(), v);
                saved[0] = true;
                dialog.dispose();
            } catch (Exception ex) {
                Log.Debug(WorkshopUi.class, ex);
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

    private static void editTireStorageDialog(Component parent, Vehicle vehicle, final ExtensionContext yabs) {
        Window owner = parent instanceof Window ? (Window) parent : SwingUtilities.getWindowAncestor(parent);
        TireStorage loaded = WorkshopStore.loadLatestTireStorageSafe(vehicle.id);
        final TireStorage t = loaded != null ? loaded : new TireStorage();
        t.vehicleId = vehicle.id;
        if (t.storedAt.isEmpty()) {
            t.storedAt = LocalDate.now().toString();
        }

        final JDialog dialog = new JDialog(owner, yabs.tr("tire.dialog.title", vehicle.displayLabel()), JDialog.ModalityType.APPLICATION_MODAL);

        String[] seasonCodes = {"SUMMER", "WINTER", "ALL_SEASON"};
        JComboBox<String> season = new JComboBox<String>(new String[]{
            yabs.tr("tire.season.SUMMER"),
            yabs.tr("tire.season.WINTER"),
            yabs.tr("tire.season.ALL_SEASON")
        });
        for (int i = 0; i < seasonCodes.length; i++) {
            if (seasonCodes[i].equalsIgnoreCase(t.season)) {
                season.setSelectedIndex(i);
            }
        }
        String[] rimCodes = {"ALLOY", "STEEL"};
        JComboBox<String> rim = new JComboBox<String>(new String[]{
            yabs.tr("tire.rim.ALLOY"),
            yabs.tr("tire.rim.STEEL")
        });
        for (int i = 0; i < rimCodes.length; i++) {
            if (rimCodes[i].equalsIgnoreCase(t.rimType)) {
                rim.setSelectedIndex(i);
            }
        }

        JTextField loc = new JTextField(t.storageLocation, 20);
        JTextField fl = new JTextField(t.treadFl, 8);
        JTextField fr = new JTextField(t.treadFr, 8);
        JTextField rl = new JTextField(t.treadRl, 8);
        JTextField rr = new JTextField(t.treadRr, 8);
        JTextField dot = new JTextField(t.dotCodes, 22);
        JCheckBox bolts = new JCheckBox(yabs.tr("tire.field.bolts"), t.wheelBoltsIncluded);
        JTextField storedAt = new JTextField(t.storedAt, 14);
        JTextArea cond = new JTextArea(t.conditionNotes, 4, 30);
        cond.setLineWrap(true);
        cond.setWrapStyleWord(true);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(4, 6, 4, 6);
        g.anchor = GridBagConstraints.WEST;
        g.fill = GridBagConstraints.HORIZONTAL;
        int row = 0;
        addRow(form, g, row++, yabs.tr("tire.field.season"), season);
        addRow(form, g, row++, yabs.tr("tire.field.location"), loc);
        addRow(form, g, row++, yabs.tr("tire.field.treadFl"), fl);
        addRow(form, g, row++, yabs.tr("tire.field.treadFr"), fr);
        addRow(form, g, row++, yabs.tr("tire.field.treadRl"), rl);
        addRow(form, g, row++, yabs.tr("tire.field.treadRr"), rr);
        addRow(form, g, row++, yabs.tr("tire.field.dot"), dot);
        addRow(form, g, row++, yabs.tr("tire.field.rimType"), rim);
        g.gridx = 0;
        g.gridy = row++;
        g.gridwidth = 2;
        form.add(bolts, g);
        addRow(form, g, row++, yabs.tr("tire.field.storedAt"), storedAt);
        addRow(form, g, row++, yabs.tr("tire.field.condition"), new JScrollPane(cond));

        JButton cancel = new JButton(yabs.tr("common.cancel"));
        cancel.addActionListener(e -> dialog.dispose());
        JButton ok = new JButton(yabs.tr("common.save"));
        ok.addActionListener(e -> {
            t.season = seasonCodes[Math.max(0, season.getSelectedIndex())];
            t.storageLocation = loc.getText().trim();
            t.treadFl = fl.getText().trim();
            t.treadFr = fr.getText().trim();
            t.treadRl = rl.getText().trim();
            t.treadRr = rr.getText().trim();
            t.dotCodes = dot.getText().trim();
            t.rimType = rimCodes[Math.max(0, rim.getSelectedIndex())];
            t.wheelBoltsIncluded = bolts.isSelected();
            t.storedAt = storedAt.getText().trim();
            t.conditionNotes = cond.getText().trim();
            try {
                WorkshopStore.saveTireStorage(WorkshopStore.connection(), t);
                dialog.dispose();
            } catch (Exception ex) {
                Log.Debug(WorkshopUi.class, ex);
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
    }

    private static void openHistoryDialog(Component parent, Vehicle vehicle, ExtensionContext yabs) {
        Window owner = parent instanceof Window ? (Window) parent : SwingUtilities.getWindowAncestor(parent);
        final JDialog dialog = new JDialog(owner, yabs.tr("history.dialog.title", vehicle.displayLabel()), JDialog.ModalityType.APPLICATION_MODAL);

        DefaultTableModel model = new DefaultTableModel(new Object[]{
            yabs.tr("history.col.itemId"),
            yabs.tr("history.col.plate"),
            yabs.tr("history.col.mileage"),
            yabs.tr("history.col.dropoff"),
            yabs.tr("history.col.pickup"),
            yabs.tr("history.col.summary")
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        try {
            for (JobData j : WorkshopStore.loadHistoryForVehicle(WorkshopStore.connection(), vehicle.id)) {
                model.addRow(new Object[]{
                    j.itemId,
                    j.plateSnapshot,
                    j.mileage > 0 ? j.mileage + " " + j.mileageUnit : "",
                    j.dropoffDate,
                    j.pickupDate,
                    j.workSummary
                });
            }
        } catch (Exception ex) {
            Log.Debug(WorkshopUi.class, ex);
        }

        JTable table = new JTable(model);
        JScrollPane sp = new JScrollPane(table);
        sp.setPreferredSize(new Dimension(720, 240));
        JButton close = new JButton(yabs.tr("common.close"));
        close.addActionListener(e -> dialog.dispose());
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        btns.add(close);
        JPanel root = new JPanel(new BorderLayout(8, 8));
        root.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        root.add(sp, BorderLayout.CENTER);
        root.add(btns, BorderLayout.SOUTH);
        dialog.setContentPane(root);
        dialog.pack();
        dialog.setLocationRelativeTo(owner);
        dialog.setVisible(true);
    }

    // ------------------------------------------------------------- job dialog

    static boolean editJobDialog(Component parent, final Item item, final ExtensionContext yabs) {
        final JobData j = WorkshopStore.loadJobSafe(item.__getIDS());
        final Settings s = Settings.load();
        if (j.mileageUnit.isEmpty()) {
            j.mileageUnit = s.distanceUnit;
        }
        Window owner = parent == null ? null : SwingUtilities.getWindowAncestor(parent);
        final JDialog dialog = new JDialog(owner, yabs.tr("job.dialog.title", item.__getCnumber()), JDialog.ModalityType.APPLICATION_MODAL);
        final boolean[] saved = {false};

        List<Vehicle> vehicles = WorkshopStore.loadVehiclesForContactSafe(item.__getContactsids(), false);
        // Ensure currently linked vehicle stays selectable even if archived later
        if (j.vehicleId > 0) {
            boolean found = false;
            for (Vehicle v : vehicles) {
                if (v.id == j.vehicleId) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                Vehicle linked = WorkshopStore.loadVehicleSafe(j.vehicleId);
                if (linked != null) {
                    vehicles = new java.util.ArrayList<Vehicle>(vehicles);
                    vehicles.add(linked);
                }
            }
        }

        JComboBox<Object> vehicleBox = new JComboBox<Object>();
        vehicleBox.addItem(yabs.tr("common.none"));
        int selectedIdx = 0;
        for (int i = 0; i < vehicles.size(); i++) {
            vehicleBox.addItem(vehicles.get(i));
            if (vehicles.get(i).id == j.vehicleId) {
                selectedIdx = i + 1;
            }
        }
        vehicleBox.setSelectedIndex(selectedIdx);

        JTextField plateSnap = new JTextField(j.plateSnapshot, 18);
        JTextField mileage = new JTextField(j.mileage > 0 ? String.valueOf(j.mileage) : "", 12);
        JComboBox<String> unit = new JComboBox<String>(new String[]{"km", "mi"});
        unit.setSelectedItem(j.mileageUnit);
        vehicleBox.addActionListener(e -> {
            Object sel = vehicleBox.getSelectedItem();
            if (sel instanceof Vehicle) {
                Vehicle v = (Vehicle) sel;
                if (plateSnap.getText().trim().isEmpty()) {
                    plateSnap.setText(v.licensePlate);
                }
                if (mileage.getText().trim().isEmpty() && v.mileage > 0) {
                    mileage.setText(String.valueOf(v.mileage));
                    unit.setSelectedItem(v.mileageUnit);
                }
            }
        });

        JTextField dropoff = new JTextField(j.dropoffDate, 14);
        JTextField pickup = new JTextField(j.pickupDate, 14);
        JTextArea complaint = new JTextArea(j.complaint, 3, 34);
        complaint.setLineWrap(true);
        complaint.setWrapStyleWord(true);
        JTextArea summary = new JTextArea(j.workSummary, 3, 34);
        summary.setLineWrap(true);
        summary.setWrapStyleWord(true);
        JTextArea internal = new JTextArea(j.internalNotes, 3, 34);
        internal.setLineWrap(true);
        internal.setWrapStyleWord(true);

        String[] oldPartsCodes = {"DISPOSE", "RETURN", "NONE"};
        JComboBox<String> oldParts = new JComboBox<String>(new String[]{
            yabs.tr("job.oldParts.DISPOSE"),
            yabs.tr("job.oldParts.RETURN"),
            yabs.tr("job.oldParts.NONE")
        });
        for (int i = 0; i < oldPartsCodes.length; i++) {
            if (oldPartsCodes[i].equalsIgnoreCase(j.oldPartsHandling)) {
                oldParts.setSelectedIndex(i);
            }
        }

        JTextField quoteNum = new JTextField(j.quoteNumberSnapshot, 16);
        JTextField quoteTotal = new JTextField(j.quoteTotalSnapshot, 14);
        JCheckBox overrunApproved = new JCheckBox(yabs.tr("job.field.overrunApproved"), j.overrunApproved);
        JTextField overrunAt = new JTextField(j.overrunApprovedAt, 20);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(4, 6, 4, 6);
        g.anchor = GridBagConstraints.WEST;
        g.fill = GridBagConstraints.HORIZONTAL;
        int row = 0;
        addRow(form, g, row++, yabs.tr("job.field.vehicle"), vehicleBox);
        addRow(form, g, row++, yabs.tr("job.field.plateSnapshot"), plateSnap);
        addRow(form, g, row++, yabs.tr("job.field.mileage"), mileage);
        addRow(form, g, row++, yabs.tr("vehicle.field.unit"), unit);
        addRow(form, g, row++, yabs.tr("job.field.dropoff"), dropoff);
        addRow(form, g, row++, yabs.tr("job.field.pickup"), pickup);
        addRow(form, g, row++, yabs.tr("job.field.complaint"), new JScrollPane(complaint));
        addRow(form, g, row++, yabs.tr("job.field.workSummary"), new JScrollPane(summary));
        addRow(form, g, row++, yabs.tr("job.field.internalNotes"), new JScrollPane(internal));
        addRow(form, g, row++, yabs.tr("job.field.oldParts"), oldParts);
        addRow(form, g, row++, yabs.tr("job.field.quoteNumber"), quoteNum);
        addRow(form, g, row++, yabs.tr("job.field.quoteTotal"), quoteTotal);
        g.gridx = 0;
        g.gridy = row++;
        g.gridwidth = 2;
        form.add(overrunApproved, g);
        addRow(form, g, row++, yabs.tr("job.field.overrunApprovedAt"), overrunAt);

        JButton cancel = new JButton(yabs.tr("common.cancel"));
        cancel.addActionListener(e -> dialog.dispose());
        JButton ok = new JButton(yabs.tr("common.save"));
        ok.addActionListener(e -> {
            Object sel = vehicleBox.getSelectedItem();
            j.vehicleId = sel instanceof Vehicle ? ((Vehicle) sel).id : 0;
            j.plateSnapshot = plateSnap.getText().trim();
            if (j.plateSnapshot.isEmpty() && sel instanceof Vehicle) {
                j.plateSnapshot = ((Vehicle) sel).licensePlate;
            }
            j.mileage = parseLong(mileage.getText(), 0L);
            j.mileageUnit = String.valueOf(unit.getSelectedItem());
            j.dropoffDate = dropoff.getText().trim();
            j.pickupDate = pickup.getText().trim();
            j.complaint = complaint.getText().trim();
            j.workSummary = summary.getText().trim();
            j.internalNotes = internal.getText().trim();
            j.oldPartsHandling = oldPartsCodes[Math.max(0, oldParts.getSelectedIndex())];
            j.quoteNumberSnapshot = quoteNum.getText().trim();
            j.quoteTotalSnapshot = quoteTotal.getText().trim();
            j.overrunApproved = overrunApproved.isSelected();
            j.overrunApprovedAt = overrunAt.getText().trim();
            try {
                WorkshopStore.saveJob(WorkshopStore.connection(), j);
                saved[0] = true;
                dialog.dispose();
            } catch (Exception ex) {
                Log.Debug(WorkshopUi.class, ex);
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

    // ------------------------------------------------------------- overview tab

    static void openOverviewTab(ExtensionContext yabs) {
        JTabbedPane tabs = new JTabbedPane();
        Settings s = Settings.load();
        LocalDate today = LocalDate.now();

        DefaultTableModel inspModel = new DefaultTableModel(new Object[]{
            yabs.tr("overview.col.plate"),
            yabs.tr("overview.col.makeModel"),
            yabs.tr("overview.col.inspectionDue"),
            yabs.tr("overview.col.status")
        }, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        DefaultTableModel tireModel = new DefaultTableModel(new Object[]{
            yabs.tr("overview.col.plate"),
            yabs.tr("overview.col.season"),
            yabs.tr("overview.col.location"),
            yabs.tr("overview.col.tread"),
            yabs.tr("overview.col.rim"),
            yabs.tr("overview.col.condition")
        }, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        try {
            if (WorkshopStore.hasDatabase()) {
                for (Vehicle v : WorkshopStore.loadAllActiveVehicles(WorkshopStore.connection())) {
                    LocalDate due = WorkshopValidator.parseInspectionDue(v.inspectionDue);
                    if (due != null && WorkshopValidator.isInspectionDueSoon(v.inspectionDue, today, s.inspectionLeadDays)) {
                        String status = due.isBefore(today)
                                ? yabs.tr("overview.status.overdue")
                                : yabs.tr("overview.status.dueSoon");
                        inspModel.addRow(new Object[]{v.licensePlate, v.makeModel, v.inspectionDue, status});
                    }
                }
                for (TireStorage t : WorkshopStore.loadAllTireStorage(WorkshopStore.connection())) {
                    Vehicle v = WorkshopStore.loadVehicleSafe(t.vehicleId);
                    String plate = v == null ? "#" + t.vehicleId : v.displayLabel();
                    tireModel.addRow(new Object[]{
                        plate,
                        yabs.tr("tire.season." + t.season),
                        t.storageLocation,
                        t.treadSummary(),
                        yabs.tr("tire.rim." + t.rimType),
                        t.conditionNotes
                    });
                }
            }
        } catch (Exception ex) {
            Log.Debug(WorkshopUi.class, ex);
        }

        tabs.addTab(yabs.tr("overview.tab.inspections"), new JScrollPane(new JTable(inspModel)));
        tabs.addTab(yabs.tr("overview.tab.tires"), new JScrollPane(new JTable(tireModel)));
        yabs.openTab(yabs.tr("overview.tab.title"), tabs);
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

    private static long parseLong(String s, long fallback) {
        try {
            return Long.parseLong(s.trim().replaceAll("[^0-9]", ""));
        } catch (Exception e) {
            return fallback;
        }
    }
}
