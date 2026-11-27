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

import java.io.File;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.JOptionPane;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.db.common.DatabaseObjectModifier;
import mpv5.db.objects.Contact;
import mpv5.db.objects.Item;
import mpv5.db.objects.Product;
import mpv5.globals.Constants;
import mpv5.logging.Log;
import mpv5.plugin.api.ExtensionContext;
import mpv5.plugin.api.YabsExtension;
import mpv5.ui.dialogs.DialogForFile;
import mpv5.ui.panels.ChangeNotApprovedException;
import mpv5.ui.panels.DataPanel;
import org.openyabs.workshop.WorkshopStore.JobData;
import org.openyabs.workshop.WorkshopStore.Settings;
import org.openyabs.workshop.WorkshopStore.TireStorage;
import org.openyabs.workshop.WorkshopStore.Vehicle;

/**
 * Bilingual (DE/EN) Car Repair Shop / Kfz-Werkstatt plugin for OpenYaBS.
 */
public final class WorkshopPlugin implements YabsExtension {

    public static final String ID = "org.openyabs.workshop";
    public static final String I18N_EN = "/org/openyabs/workshop/i18n/messages_en.properties";
    public static final String I18N_DE = "/org/openyabs/workshop/i18n/messages_de.properties";
    public static final String[] TEMPLATE_RESOURCES = {
        "/org/openyabs/workshop/templates/workshop_invoice_en.fodt",
        "/org/openyabs/workshop/templates/workshop_invoice_de.fodt",
        "/org/openyabs/workshop/templates/workshop_order_en.fodt",
        "/org/openyabs/workshop/templates/workshop_order_de.fodt",
        "/org/openyabs/workshop/templates/tire_storage_en.fodt",
        "/org/openyabs/workshop/templates/tire_storage_de.fodt"
    };

    private ExtensionContext ctx;

    @Override
    public String id() {
        return ID;
    }

    @Override
    public String name() {
        return ctx != null ? ctx.tr("plugin.name") : "Car Repair Shop / Kfz-Werkstatt";
    }

    @Override
    public String description() {
        return ctx != null ? ctx.tr("plugin.description")
                : "Vehicle management, repair orders, labor units (AW), tire storage, inspection reminders, and DE/EN templates.";
    }

    @Override
    public String version() {
        return "1.0.0";
    }

    @Override
    public String author() {
        return "OpenYaBS Community";
    }

    @Override
    public String minYabsVersion() {
        return "2.0.10";
    }

    @Override
    public void start(final ExtensionContext yabs) throws Exception {
        this.ctx = yabs;

        ClassLoader cl = WorkshopPlugin.class.getClassLoader();
        yabs.addTranslationBundle("en", I18N_EN, cl);
        yabs.addTranslationBundle("de", I18N_DE, cl);

        if (WorkshopStore.hasDatabase()) {
            WorkshopStore.initSchema(WorkshopStore.connection());
        }

        yabs.addSetup(
                yabs.tr("cmd.setup.title"),
                yabs.tr("settings.title"),
                yabs.tr("settings.description"),
                () -> WorkshopUi.settingsPage(yabs, this));

        yabs.addTemplateFields(obj -> provideTemplateFields(obj, yabs));

        yabs.onView(panel -> {
            if (WorkshopStore.hasDatabase()) {
                WorkshopUi.decorate(panel, yabs);
            }
        });

        yabs.addRecordModifier(createSaveValidator(yabs));

        yabs.addCommand(
                yabs.tr("cmd.overview.title"),
                yabs.tr("cmd.overview.desc"),
                () -> WorkshopUi.openOverviewTab(yabs));

        yabs.addCommand(
                yabs.tr("cmd.check.title"),
                yabs.tr("cmd.check.desc"),
                () -> checkCurrentDocument(yabs));
    }

    private Map<String, Object> provideTemplateFields(DatabaseObject obj, ExtensionContext yabs) {
        if (!WorkshopStore.hasDatabase()) {
            return Collections.emptyMap();
        }
        Settings settings = Settings.load();
        if (obj instanceof Item) {
            Item item = (Item) obj;
            JobData job = WorkshopStore.loadJobSafe(item.__getIDS());
            Vehicle v = WorkshopStore.loadVehicleSafe(job.vehicleId);
            if (v == null && item.__getContactsids() > 0) {
                List<Vehicle> list = WorkshopStore.loadVehiclesForContactSafe(item.__getContactsids(), false);
                if (list.size() == 1) {
                    v = list.get(0);
                }
            }
            TireStorage tire = v != null ? WorkshopStore.loadLatestTireStorageSafe(v.id) : null;
            return WorkshopValidator.templateFields(v, job, tire, settings, yabs);
        }
        if (obj instanceof Contact) {
            List<Vehicle> list = WorkshopStore.loadVehiclesForContactSafe(obj.__getIDS(), false);
            Vehicle v = list.isEmpty() ? null : list.get(0);
            TireStorage tire = v != null ? WorkshopStore.loadLatestTireStorageSafe(v.id) : null;
            return WorkshopValidator.templateFields(v, null, tire, settings, yabs);
        }
        return Collections.emptyMap();
    }

    private DatabaseObjectModifier createSaveValidator(final ExtensionContext yabs) {
        return new DatabaseObjectModifier() {
            @Override
            public DatabaseObject modifyOnExplode(DatabaseObject object) {
                return object;
            }

            @Override
            public DatabaseObject modifyOnSave(DatabaseObject object) throws ChangeNotApprovedException {
                if (!(object instanceof Item) || !WorkshopStore.hasDatabase()) {
                    return object;
                }
                Item item = (Item) object;
                int type = item.__getInttype();
                if (type != Constants.TYPE_ORDER && type != Constants.TYPE_INVOICE) {
                    return object;
                }
                Settings settings = Settings.load();
                if (!settings.warnOnSave) {
                    return object;
                }
                try {
                    if (!WorkshopStore.hasJob(WorkshopStore.connection(), item.__getIDS())) {
                        return object;
                    }
                    List<String> warnings = validateItem(item, yabs);
                    if (!warnings.isEmpty()) {
                        StringBuilder sb = new StringBuilder(yabs.tr("val.header")).append("\n\n");
                        for (String w : warnings) {
                            sb.append("\u2022 ").append(w).append("\n");
                        }
                        sb.append("\n").append(yabs.tr("val.savePrompt"));
                        int choice = JOptionPane.showConfirmDialog(null, sb.toString(),
                                yabs.tr("val.title"), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                        if (choice != JOptionPane.YES_OPTION) {
                            throw new ChangeNotApprovedException(item);
                        }
                    }
                } catch (ChangeNotApprovedException cna) {
                    throw cna;
                } catch (Exception e) {
                    Log.Debug(WorkshopPlugin.class, e);
                }
                return object;
            }

            @Override
            public DatabaseObject executeAfterCreate(DatabaseObject object) {
                return object;
            }

            @Override
            public DatabaseObject executeAfterSave(DatabaseObject object) {
                return object;
            }

            @Override
            public Map<String, Object> modifyOnResolve(Map<String, Object> map) {
                return map;
            }

            @Override
            public DatabaseObject modifyOnDelete(DatabaseObject object) throws ChangeNotApprovedException {
                return object;
            }
        };
    }

    static List<String> validateItem(Item item, ExtensionContext yabs) {
        JobData job = WorkshopStore.loadJobSafe(item.__getIDS());
        Vehicle v = WorkshopStore.loadVehicleSafe(job.vehicleId);
        Long prevMaxKm = null;
        if (job.vehicleId > 0 && WorkshopStore.hasDatabase()) {
            try {
                prevMaxKm = WorkshopStore.maxRecordedMileageKm(WorkshopStore.connection(), job.vehicleId, item.__getIDS());
            } catch (Exception e) {
                Log.Debug(WorkshopPlugin.class, e);
            }
        }
        BigDecimal total = item.getNetAfterDiscount();
        return WorkshopValidator.validate(job, v, total, prevMaxKm, Settings.load(), yabs);
    }

    String importStarterCatalog(ExtensionContext yabs) {
        try {
            Set<String> existing = new HashSet<String>();
            try {
                for (DatabaseObject o : DatabaseObject.getObjects(Context.getProduct())) {
                    Product p = (Product) o;
                    if (p.__getCnumber() != null) {
                        existing.add(p.__getCnumber().trim().toUpperCase());
                    }
                }
            } catch (Exception ignored) {
            }

            Settings s = Settings.load();
            Object[][] items = {
                {"WS-INSP", yabs.tr("catalog.inspection.name"), yabs.tr("catalog.inspection.desc"), s.rateMechanical.multiply(new BigDecimal("1.5")), yabs.tr("catalog.unit.service")},
                {"WS-OIL", yabs.tr("catalog.oil.name"), yabs.tr("catalog.oil.desc"), s.rateMechanical.multiply(new BigDecimal("0.5")), yabs.tr("catalog.unit.service")},
                {"WS-BRAKE", yabs.tr("catalog.brakes.name"), yabs.tr("catalog.brakes.desc"), s.rateMechanical, yabs.tr("catalog.unit.service")},
                {"WS-TIRE-SWAP", yabs.tr("catalog.tireswap.name"), yabs.tr("catalog.tireswap.desc"), new BigDecimal("49.00"), yabs.tr("catalog.unit.set")},
                {"WS-TIRE-STORE", yabs.tr("catalog.tirestorage.name"), yabs.tr("catalog.tirestorage.desc"), new BigDecimal("59.00"), yabs.tr("catalog.unit.set")},
                {"WS-DIAG", yabs.tr("catalog.diagnostics.name"), yabs.tr("catalog.diagnostics.desc"), s.rateElectrical.multiply(new BigDecimal("0.5")), yabs.tr("catalog.unit.service")},
                {"WS-MOT-PREP", yabs.tr("catalog.motprep.name"), yabs.tr("catalog.motprep.desc"), s.rateMechanical.multiply(new BigDecimal("0.5")), yabs.tr("catalog.unit.service")}
            };

            int created = 0;
            int skipped = 0;
            for (Object[] row : items) {
                String sku = (String) row[0];
                if (existing.contains(sku.toUpperCase())) {
                    skipped++;
                    continue;
                }
                Product p = new Product();
                p.setInttype(Product.TYPE_SERVICE);
                p.setCnumber(sku);
                p.setCname((String) row[1]);
                p.setDescription((String) row[2]);
                p.setExternalnetvalue((BigDecimal) row[3]);
                p.setMeasure((String) row[4]);
                if (p.save(true)) {
                    created++;
                }
            }
            return yabs.tr("cmd.importCatalog.done", created, skipped);
        } catch (Exception ex) {
            Log.Debug(WorkshopPlugin.class, ex);
            return yabs.tr("cmd.importCatalog.failed", ex.getMessage());
        }
    }

    String importTemplates(ExtensionContext yabs) {
        Object[][] specs = {
            {"/org/openyabs/workshop/templates/workshop_invoice_de.fodt", "Werkstatt - Rechnung (DE)", Constants.TYPE_INVOICE},
            {"/org/openyabs/workshop/templates/workshop_invoice_en.fodt", "Workshop - Invoice (EN)", Constants.TYPE_INVOICE},
            {"/org/openyabs/workshop/templates/workshop_order_de.fodt", "Werkstatt - Auftrag (DE)", Constants.TYPE_ORDER},
            {"/org/openyabs/workshop/templates/workshop_order_en.fodt", "Workshop - Order (EN)", Constants.TYPE_ORDER},
            {"/org/openyabs/workshop/templates/tire_storage_de.fodt", "Werkstatt - Reifeneinlagerung (DE)", Constants.TYPE_CONTACT},
            {"/org/openyabs/workshop/templates/tire_storage_en.fodt", "Workshop - Tire Storage (EN)", Constants.TYPE_CONTACT}
        };
        int imported = 0;
        int existed = 0;
        try {
            for (Object[] spec : specs) {
                mpv5.handler.TemplateImportService.PluginTemplateResult r = mpv5.handler.TemplateImportService.importPluginTemplate(
                        WorkshopPlugin.class,
                        (String) spec[0],
                        (String) spec[1],
                        (Integer) spec[2],
                        mpv5.handler.FirstStartSetup.ITEM_FORMAT,
                        "Workshop plugin");
                if (r.created) {
                    imported++;
                } else {
                    existed++;
                }
            }
            return yabs.tr("cmd.importTemplates.done", imported, existed);
        } catch (Exception e) {
            Log.Debug(WorkshopPlugin.class, e);
            return yabs.tr("cmd.importTemplates.failed", e.getMessage());
        }
    }

    private void checkCurrentDocument(ExtensionContext yabs) {
        DataPanel view = yabs.currentView();
        if (view == null || !(view.getDataOwner() instanceof Item)) {
            yabs.popup(yabs.tr("cmd.check.noDoc"));
            return;
        }
        Item item = (Item) view.getDataOwner();
        List<String> warnings = validateItem(item, yabs);
        WorkshopUi.showValidationResult(null, warnings, yabs);
    }

    @Override
    public void stop() {
        this.ctx = null;
    }
}
