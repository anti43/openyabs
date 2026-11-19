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

import java.io.File;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.util.ArrayList;
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
import mpv5.db.objects.SubItem;
import mpv5.db.objects.Tax;
import mpv5.globals.Constants;
import mpv5.logging.Log;
import mpv5.plugin.api.ExtensionContext;
import mpv5.plugin.api.YabsExtension;
import mpv5.ui.dialogs.DialogForFile;
import mpv5.ui.panels.ChangeNotApprovedException;
import mpv5.ui.panels.DataPanel;
import org.openyabs.heilpraktiker.HeilpraktikerStore.InvoiceData;
import org.openyabs.heilpraktiker.HeilpraktikerStore.PatientData;
import org.openyabs.heilpraktiker.HeilpraktikerStore.Settings;
import org.openyabs.heilpraktiker.HeilpraktikerValidator.LineItem;

/**
 * German Heilpraktiker & GebüH 85/2002 plugin for OpenYaBS:
 * <ul>
 * <li>Complete verified GebüH 85/2002 catalog (144 services, Ziffern 1–39.13)
 * with one-click import as 0 % VAT services,</li>
 * <li>Patient metadata (DOB, insurance type, § 630a BGB Honorarvereinbarung,
 * § 630f BGB SOAP notes) and invoice medical metadata (ICD-10-GM, diagnosis,
 * § 4 Nr. 14 lit. a UStG flag, treatment dates),</li>
 * <li>Pre-save GebüH fee-range, analog-billing and combination-rule validation,</li>
 * <li>Template placeholder provider ({@code hp.*}) and bundled {@code .fodt}
 * invoice template.</li>
 * </ul>
 */
public final class HeilpraktikerPlugin implements YabsExtension {

    public static final String ID = "org.openyabs.heilpraktiker";
    public static final String TEMPLATE_RESOURCE = "/org/openyabs/heilpraktiker/heilpraktiker_rechnung.fodt";

    private GebuehCatalog catalog;

    @Override
    public String id() {
        return ID;
    }

    @Override
    public String name() {
        return "Heilpraktiker & Geb\u00fcH";
    }

    @Override
    public String description() {
        return "Offizieller Geb\u00fcH-85/2002-Katalog (144 Ziffern, 1\u201339.13), ICD-10-Diagnosen, PKV/Beihilfe-Pr\u00fcfung, \u00a7 4 Nr. 14 UStG und Rechnungsvorlage.";
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
        catalog = GebuehCatalog.loadDefault();

        if (HeilpraktikerStore.hasDatabase()) {
            HeilpraktikerStore.initSchema(HeilpraktikerStore.connection());
        }

        yabs.addSettingsPage(
                "Heilpraktiker & Geb\u00fcH",
                "Berufsbezeichnung, Gesundheitsamt (\u00a7 1 HeilprG), \u00a7 4 Nr. 14 UStG-Hinweis und Geb\u00fcH-Pr\u00fcfung",
                () -> HeilpraktikerUi.settingsPage(yabs));

        yabs.addTemplateFields(this::provideTemplateFields);

        yabs.onView(panel -> {
            if (HeilpraktikerStore.hasDatabase()) {
                HeilpraktikerUi.decorate(panel, yabs, catalog);
            }
        });

        yabs.addRecordModifier(createSaveValidator(yabs));

        yabs.addCommand(
                "Heilpraktiker: Geb\u00fcH-Katalog (144 Ziffern) importieren",
                "Legt alle 144 offiziellen Geb\u00fcH-85/2002-Leistungen (Ziffern 1\u201339.13) als steuerfreie Leistungen (0 % USt) an",
                () -> yabs.background(this::importGebuehCatalog, msg -> yabs.popup(msg)));

        yabs.addCommand(
                "Heilpraktiker: Rechnungsvorlage (.fodt) speichern",
                "Speichert die vorgefertigte LibreOffice-Rechnungsvorlage mit [hp.*]-Platzhaltern",
                () -> exportTemplate(yabs));

        yabs.addCommand(
                "Heilpraktiker: Aktuelle Rechnung pr\u00fcfen",
                "Pr\u00fcft die ge\u00f6ffnete Rechnung auf Geb\u00fcH-Grenzen, Kombinationsregeln, Diagnose und \u00a7 4 Nr. 14 UStG",
                () -> checkCurrentInvoice(yabs));
    }

    private Map<String, Object> provideTemplateFields(DatabaseObject obj) {
        if (!HeilpraktikerStore.hasDatabase()) {
            return Collections.emptyMap();
        }
        Settings settings = Settings.load();
        if (obj instanceof Item) {
            Item item = (Item) obj;
            PatientData p = HeilpraktikerStore.loadPatientSafe(item.__getContactsids());
            InvoiceData d = HeilpraktikerStore.loadInvoiceSafe(item.__getIDS());
            return HeilpraktikerValidator.templateFields(p, d, settings);
        }
        if (obj instanceof Contact) {
            PatientData p = HeilpraktikerStore.loadPatientSafe(obj.__getIDS());
            return HeilpraktikerValidator.templateFields(p, null, settings);
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
                if (!(object instanceof Item) || !HeilpraktikerStore.hasDatabase()) {
                    return object;
                }
                Item item = (Item) object;
                if (item.__getInttype() != Constants.TYPE_INVOICE && item.__getInttype() != Constants.TYPE_CREDIT) {
                    return object;
                }
                Settings settings = Settings.load();
                if (!settings.warnOnSave) {
                    return object;
                }
                try {
                    boolean hasHpData = HeilpraktikerStore.hasInvoiceData(HeilpraktikerStore.connection(), item.__getIDS());
                    List<LineItem> lines = extractLines(item);
                    boolean hasGebuehLine = false;
                    for (LineItem l : lines) {
                        if (catalog.parse(l.sku) != null || catalog.parse(l.description) != null) {
                            hasGebuehLine = true;
                            break;
                        }
                    }
                    if (!hasHpData && !hasGebuehLine) {
                        return object;
                    }
                    PatientData p = HeilpraktikerStore.loadPatientSafe(item.__getContactsids());
                    InvoiceData d = HeilpraktikerStore.loadInvoiceSafe(item.__getIDS());
                    List<String> warnings = HeilpraktikerValidator.validate(p, d, lines, catalog);
                    if (!warnings.isEmpty()) {
                        StringBuilder sb = new StringBuilder("Heilpraktiker-Pr\u00fcfung hat Hinweise gefunden:\n\n");
                        for (String w : warnings) {
                            sb.append("\u2022 ").append(w).append("\n");
                        }
                        sb.append("\nTrotzdem speichern?");
                        int choice = JOptionPane.showConfirmDialog(null, sb.toString(),
                                "Heilpraktiker & Geb\u00fcH \u2013 Hinweis", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                        if (choice != JOptionPane.YES_OPTION) {
                            throw new ChangeNotApprovedException(item);
                        }
                    }
                } catch (ChangeNotApprovedException cna) {
                    throw cna;
                } catch (Exception e) {
                    Log.Debug(HeilpraktikerPlugin.class, e);
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

    private String importGebuehCatalog() {
        try {
            int zeroTaxId = findOrCreateZeroTaxId();
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

            Settings settings = Settings.load();
            int created = 0;
            int skipped = 0;
            for (GebuehCatalog.Entry e : catalog.all()) {
                String sku = e.sku();
                if (existing.contains(sku.toUpperCase())) {
                    skipped++;
                    continue;
                }
                Product p = new Product();
                p.setInttype(Product.TYPE_SERVICE);
                p.setCnumber(sku);
                p.setCname("Geb\u00fcH " + e.code + ": " + e.title);
                String desc = e.group + " (" + e.minFee.toPlainString() + " \u2013 " + e.maxFee.toPlainString() + " EUR)";
                if (!e.note.isEmpty()) {
                    desc += " \u2013 " + e.note;
                }
                p.setDescription(desc);
                p.setExternalnetvalue(e.feeFor(settings.defaultPriceMode));
                p.setTaxids(zeroTaxId);
                p.setMeasure("Behandlung");
                if (p.save(true)) {
                    created++;
                }
            }
            return "Geb\u00fcH-Import abgeschlossen: " + created + " Leistungen neu angelegt, " + skipped + " bereits vorhanden (Katalog: " + catalog.size() + " Ziffern).";
        } catch (Exception ex) {
            Log.Debug(HeilpraktikerPlugin.class, ex);
            return "Geb\u00fcH-Import fehlgeschlagen: " + ex.getMessage();
        }
    }

    private int findOrCreateZeroTaxId() {
        try {
            for (DatabaseObject o : DatabaseObject.getObjects(Context.getTaxes())) {
                Tax t = (Tax) o;
                if (t.__getTaxvalue() != null && t.__getTaxvalue().signum() == 0) {
                    return t.__getIDS();
                }
            }
        } catch (Exception ignored) {
        }
        try {
            Tax zero = new Tax();
            zero.setCname("0 % (\u00a7 4 Nr. 14 UStG)");
            zero.setTaxvalue(BigDecimal.ZERO);
            zero.setCountry("DE");
            if (zero.save(true)) {
                return zero.__getIDS();
            }
        } catch (Exception ex) {
            Log.Debug(HeilpraktikerPlugin.class, ex);
        }
        return 1;
    }

    private void exportTemplate(ExtensionContext yabs) {
        try {
            DialogForFile d = new DialogForFile(DialogForFile.FILES_ONLY, "heilpraktiker_rechnung.fodt");
            if (!d.saveFile()) {
                return;
            }
            File target = d.getFile();
            if (!target.getName().toLowerCase().endsWith(".fodt")) {
                target = new File(target.getParentFile(), target.getName() + ".fodt");
            }
            try (InputStream in = HeilpraktikerPlugin.class.getResourceAsStream(TEMPLATE_RESOURCE)) {
                if (in == null) {
                    yabs.error("Vorlage nicht gefunden: " + TEMPLATE_RESOURCE);
                    return;
                }
                Files.write(target.toPath(), in.readAllBytes());
            }
            yabs.notify("Heilpraktiker-Rechnungsvorlage gespeichert: " + target.getName());
        } catch (Exception e) {
            Log.Debug(HeilpraktikerPlugin.class, e);
            yabs.error("Vorlage konnte nicht gespeichert werden: " + e.getMessage());
        }
    }

    private void checkCurrentInvoice(ExtensionContext yabs) {
        DataPanel view = yabs.currentView();
        if (view == null || !(view.getDataOwner() instanceof Item)) {
            yabs.popup("Bitte \u00f6ffnen Sie zuerst eine Rechnung.");
            return;
        }
        Item item = (Item) view.getDataOwner();
        List<String> warnings = validateItem(item, catalog);
        HeilpraktikerUi.showValidationResult(null, warnings);
    }

    static List<String> validateItem(Item item, GebuehCatalog catalog) {
        PatientData p = HeilpraktikerStore.loadPatientSafe(item.__getContactsids());
        InvoiceData d = HeilpraktikerStore.loadInvoiceSafe(item.__getIDS());
        return HeilpraktikerValidator.validate(p, d, extractLines(item), catalog);
    }

    static List<LineItem> extractLines(Item item) {
        List<LineItem> lines = new ArrayList<LineItem>();
        try {
            SubItem[] subs = item.getSubitems();
            if (subs != null) {
                for (SubItem s : subs) {
                    if (s == null) {
                        continue;
                    }
                    LineItem l = new LineItem();
                    l.description = s.__getDescription() == null ? "" : s.__getDescription();
                    l.unitPrice = s.__getExternalvalue() == null ? BigDecimal.ZERO : s.__getExternalvalue();
                    l.taxRate = s.__getTaxpercentvalue() == null ? BigDecimal.ZERO : s.__getTaxpercentvalue();
                    l.quantity = s.__getQuantityvalue() == null ? BigDecimal.ONE : s.__getQuantityvalue();
                    if (s.__getOriginalproductsids() > 0) {
                        try {
                            Product prod = (Product) DatabaseObject.getObject(Context.getProduct(), s.__getOriginalproductsids());
                            if (prod != null && prod.__getCnumber() != null) {
                                l.sku = prod.__getCnumber();
                            }
                        } catch (Exception ignored) {
                        }
                    }
                    lines.add(l);
                }
            }
        } catch (Exception e) {
            Log.Debug(HeilpraktikerPlugin.class, e.getMessage());
        }
        return lines;
    }

    @Override
    public void stop() {
        // ExtensionManager removes all registered pages, commands, modifiers, views and template providers
    }
}
