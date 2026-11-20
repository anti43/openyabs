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

import java.io.File;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
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
import mpv5.db.objects.User;
import mpv5.globals.Constants;
import mpv5.logging.Log;
import mpv5.plugin.api.ExtensionContext;
import mpv5.plugin.api.YabsExtension;
import mpv5.ui.dialogs.DialogForFile;
import mpv5.ui.panels.ChangeNotApprovedException;
import mpv5.ui.panels.DataPanel;
import org.openyabs.verein.VereinStore.InvoiceData;
import org.openyabs.verein.VereinStore.MemberData;
import org.openyabs.verein.VereinStore.Settings;

/**
 * German Non-Profit Association (e.V.) plugin for OpenYaBS (2026 law):
 * membership tiers, annual SEPA dues batch run, 4-sphere EÜR reporting, BMF
 * donation receipts (§ 10b EStG / § 50 EStDV), and bilingual DE/EN UI via the
 * core plugin i18n API.
 */
public final class VereinPlugin implements YabsExtension {

    public static final String ID = "org.openyabs.verein";
    public static final String I18N_EN = "/org/openyabs/verein/i18n/messages_en.properties";
    public static final String I18N_DE = "/org/openyabs/verein/i18n/messages_de.properties";
    public static final String[] TEMPLATE_RESOURCES = {
        "/org/openyabs/verein/templates/zuwendungsbestaetigung_geld.fodt",
        "/org/openyabs/verein/templates/beitragsrechnung.fodt"
    };

    private ExtensionContext ctx;

    @Override
    public String id() {
        return ID;
    }

    @Override
    public String name() {
        return ctx != null ? ctx.tr("plugin.name") : "Vereinsverwaltung (e.V.)";
    }

    @Override
    public String description() {
        return ctx != null ? ctx.tr("plugin.description")
                : "German non-profit association (e.V.): member tiers, SEPA dues batch run, BMF donation receipts, and 4-sphere tax summary.";
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

        ClassLoader cl = VereinPlugin.class.getClassLoader();
        yabs.addTranslationBundle("en", I18N_EN, cl);
        yabs.addTranslationBundle("de", I18N_DE, cl);

        if (VereinStore.hasDatabase()) {
            VereinStore.initSchema(VereinStore.connection());
        }

        yabs.addSettingsPage(
                yabs.tr("settings.title"),
                yabs.tr("settings.description"),
                () -> VereinUi.settingsPage(yabs));

        yabs.addTemplateFields(obj -> provideTemplateFields(obj, yabs));

        yabs.onView(panel -> {
            if (VereinStore.hasDatabase()) {
                VereinUi.decorate(panel, yabs);
            }
        });

        yabs.addRecordModifier(createSaveValidator(yabs));

        yabs.addCommand(
                yabs.tr("cmd.importTiers.title"),
                yabs.tr("cmd.importTiers.desc"),
                () -> yabs.background(() -> importStarterTiers(yabs), msg -> yabs.popup(msg)));

        yabs.addCommand(
                yabs.tr("cmd.batchRun.title"),
                yabs.tr("cmd.batchRun.desc"),
                () -> runAnnualDuesBatch(yabs));

        yabs.addCommand(
                yabs.tr("cmd.sphereReport.title"),
                yabs.tr("cmd.sphereReport.desc"),
                () -> VereinUi.openSphereOverviewTab(yabs));

        yabs.addCommand(
                yabs.tr("cmd.exportTemplates.title"),
                yabs.tr("cmd.exportTemplates.desc"),
                () -> exportTemplates(yabs));

        yabs.addCommand(
                yabs.tr("cmd.check.title"),
                yabs.tr("cmd.check.desc"),
                () -> checkCurrentInvoice(yabs));
    }

    private Map<String, Object> provideTemplateFields(DatabaseObject obj, ExtensionContext yabs) {
        if (!VereinStore.hasDatabase()) {
            return Collections.emptyMap();
        }
        Settings settings = Settings.load();
        if (obj instanceof Item) {
            Item item = (Item) obj;
            MemberData m = VereinStore.loadMemberSafe(item.__getContactsids());
            InvoiceData d = VereinStore.loadInvoiceSafe(item.__getIDS());
            return VereinValidator.templateFields(m, d, grossTotal(item), settings, yabs);
        }
        if (obj instanceof Contact) {
            MemberData m = VereinStore.loadMemberSafe(obj.__getIDS());
            return VereinValidator.templateFields(m, null, BigDecimal.ZERO, settings, yabs);
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
                if (!(object instanceof Item) || !VereinStore.hasDatabase()) {
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
                    if (!VereinStore.hasInvoice(VereinStore.connection(), item.__getIDS())) {
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
                    Log.Debug(VereinPlugin.class, e);
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
        MemberData m = VereinStore.loadMemberSafe(item.__getContactsids());
        InvoiceData d = VereinStore.loadInvoiceSafe(item.__getIDS());
        List<BigDecimal> rates = extractTaxRates(item);
        return VereinValidator.validate(m, d, rates, grossTotal(item), Settings.load(), LocalDate.now(), yabs);
    }

    static BigDecimal grossTotal(Item item) {
        BigDecimal net = item.__getNetvalue() == null ? BigDecimal.ZERO : item.__getNetvalue();
        BigDecimal disc = item.__getDiscountvalue() == null ? BigDecimal.ZERO : item.__getDiscountvalue();
        BigDecimal tax = item.__getTaxvalue() == null ? BigDecimal.ZERO : item.__getTaxvalue();
        return net.subtract(disc).add(tax);
    }

    private static List<BigDecimal> extractTaxRates(Item item) {
        List<BigDecimal> rates = new ArrayList<BigDecimal>();
        try {
            SubItem[] subs = item.getSubitems();
            if (subs != null) {
                for (SubItem s : subs) {
                    if (s != null) {
                        rates.add(s.__getTaxpercentvalue() == null ? BigDecimal.ZERO : s.__getTaxpercentvalue());
                    }
                }
            }
        } catch (Exception e) {
            Log.Debug(VereinPlugin.class, e.getMessage());
        }
        return rates;
    }

    private String importStarterTiers(ExtensionContext yabs) {
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

            Object[][] rows = {
                {"BEITRAG-AKTIV", yabs.tr("tier.active.name"), new BigDecimal("120.00"), yabs.tr("tier.unit.year")},
                {"BEITRAG-JUGEND", yabs.tr("tier.youth.name"), new BigDecimal("60.00"), yabs.tr("tier.unit.year")},
                {"BEITRAG-FAMILIE", yabs.tr("tier.family.name"), new BigDecimal("200.00"), yabs.tr("tier.unit.year")},
                {"SPENDE", yabs.tr("tier.donation.name"), new BigDecimal("50.00"), yabs.tr("tier.unit.piece")}
            };

            int created = 0;
            int skipped = 0;
            for (Object[] r : rows) {
                String sku = (String) r[0];
                if (existing.contains(sku)) {
                    skipped++;
                    continue;
                }
                Product p = new Product();
                p.setInttype(Product.TYPE_SERVICE);
                p.setCnumber(sku);
                p.setCname((String) r[1]);
                p.setExternalnetvalue((BigDecimal) r[2]);
                p.setTaxids(zeroTaxId);
                p.setMeasure((String) r[3]);
                if (p.save(true)) {
                    created++;
                }
            }
            return yabs.tr("cmd.importTiers.done", created, skipped);
        } catch (Exception ex) {
            Log.Debug(VereinPlugin.class, ex);
            return yabs.tr("cmd.importTiers.failed", ex.getMessage());
        }
    }

    private void runAnnualDuesBatch(ExtensionContext yabs) {
        int currentYear = LocalDate.now().getYear();
        String input = JOptionPane.showInputDialog(null, yabs.tr("cmd.batchRun.prompt", currentYear), String.valueOf(currentYear));
        if (input == null || input.trim().isEmpty()) {
            return;
        }
        final int year;
        try {
            year = Integer.parseInt(input.trim());
        } catch (NumberFormatException e) {
            yabs.popup(yabs.tr("cmd.batchRun.invalidYear", input));
            return;
        }
        yabs.background(() -> executeDuesBatch(year, yabs), msg -> yabs.popup(msg));
    }

    private String executeDuesBatch(int billingYear, ExtensionContext yabs) {
        int created = 0;
        int skipped = 0;
        List<String> errors = new ArrayList<String>();
        try {
            Map<String, Product> productsBySku = new HashMap<String, Product>();
            for (DatabaseObject o : DatabaseObject.getObjects(Context.getProduct())) {
                Product p = (Product) o;
                if (p.__getCnumber() != null) {
                    productsBySku.put(p.__getCnumber().trim().toUpperCase(), p);
                }
            }

            // Track contacts already billed for this fiscal year
            Set<Integer> alreadyBilledContacts = new HashSet<Integer>();
            Map<Integer, InvoiceData> invMeta = new HashMap<Integer, InvoiceData>();
            for (InvoiceData d : VereinStore.loadAllInvoices(VereinStore.connection())) {
                if (String.valueOf(billingYear).equals(d.fiscalYear) && !d.isDonation) {
                    invMeta.put(d.itemId, d);
                }
            }
            for (DatabaseObject o : DatabaseObject.getObjects(Context.getInvoice())) {
                Item it = (Item) o;
                if (invMeta.containsKey(it.__getIDS()) && it.__getIntstatus() != Item.STATUS_CANCELLED) {
                    alreadyBilledContacts.add(it.__getContactsids());
                }
            }

            int groupId = User.getCurrentUser() == null ? 1 : User.getCurrentUser().__getGroupsids();
            int userId = User.getCurrentUser() == null ? 1 : User.getCurrentUser().__getIDS();

            for (MemberData m : VereinStore.loadAllMembers(VereinStore.connection())) {
                if (!VereinValidator.isBillableForYear(m, billingYear) || alreadyBilledContacts.contains(m.contactId)) {
                    skipped++;
                    continue;
                }
                Product tier = productsBySku.get(m.tierSku.trim().toUpperCase());
                if (tier == null) {
                    errors.add("Contact #" + m.contactId + ": SKU '" + m.tierSku + "' not found");
                    continue;
                }
                Item inv = (Item) DatabaseObject.getObject(Context.getInvoice());
                inv.setInttype(Constants.TYPE_INVOICE);
                inv.setContactsids(m.contactId);
                inv.setGroupsids(groupId);
                inv.setAccountsids(1);
                inv.setIntaddedby(userId);
                inv.setIntstatus(Item.STATUS_IN_PROGRESS);
                inv.setCname(tier.__getCname() + " " + billingYear);
                Date now = new Date();
                inv.setDateadded(now);
                inv.setDatetodo(now);
                inv.setDateend(now);
                BigDecimal amount = tier.__getExternalnetvalue() == null ? BigDecimal.ZERO : tier.__getExternalnetvalue();
                inv.setNetvalue(amount);
                inv.setTaxvalue(BigDecimal.ZERO);
                if (inv.save(true) && inv.__getIDS() > 0) {
                    InvoiceData d = new InvoiceData();
                    d.itemId = inv.__getIDS();
                    d.taxSphere = VereinStore.SPHERE_IDEELL;
                    d.fiscalYear = String.valueOf(billingYear);
                    d.isDonation = false;
                    d.sepaDirectDebit = !m.sepaMandateRef.isEmpty();
                    VereinStore.saveInvoice(VereinStore.connection(), d);
                    created++;
                } else {
                    errors.add("Contact #" + m.contactId + ": could not save invoice");
                }
            }
        } catch (Exception ex) {
            Log.Debug(VereinPlugin.class, ex);
            errors.add(ex.getMessage());
        }
        String summary = yabs.tr("cmd.batchRun.done", billingYear, created, skipped);
        if (!errors.isEmpty()) {
            int limit = Math.min(errors.size(), 10);
            summary += yabs.tr("cmd.batchRun.errors", errors.size(), String.join("\n", errors.subList(0, limit)));
        }
        return summary;
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
            zero.setCname("0 % (Ideeller Bereich)");
            zero.setTaxvalue(BigDecimal.ZERO);
            zero.setCountry("DE");
            if (zero.save(true)) {
                return zero.__getIDS();
            }
        } catch (Exception ex) {
            Log.Debug(VereinPlugin.class, ex);
        }
        return 1;
    }

    private void exportTemplates(ExtensionContext yabs) {
        try {
            DialogForFile d = new DialogForFile(DialogForFile.DIRECTORIES_ONLY);
            if (!d.chooseFile()) {
                return;
            }
            File dir = d.getFile();
            if (dir == null) {
                return;
            }
            if (!dir.exists()) {
                dir.mkdirs();
            }
            int count = 0;
            for (String res : TEMPLATE_RESOURCES) {
                String name = res.substring(res.lastIndexOf('/') + 1);
                try (InputStream in = VereinPlugin.class.getResourceAsStream(res)) {
                    if (in == null) {
                        continue;
                    }
                    Files.write(new File(dir, name).toPath(), in.readAllBytes());
                    count++;
                }
            }
            yabs.notify(yabs.tr("cmd.exportTemplates.done", count, dir.getAbsolutePath()));
        } catch (Exception e) {
            Log.Debug(VereinPlugin.class, e);
            yabs.error(yabs.tr("cmd.exportTemplates.failed", e.getMessage()));
        }
    }

    private void checkCurrentInvoice(ExtensionContext yabs) {
        DataPanel view = yabs.currentView();
        if (view == null || !(view.getDataOwner() instanceof Item)) {
            yabs.popup(yabs.tr("cmd.check.noDoc"));
            return;
        }
        Item item = (Item) view.getDataOwner();
        List<String> warnings = validateItem(item, yabs);
        VereinUi.showValidationResult(null, warnings, yabs);
    }

    @Override
    public void stop() {
        this.ctx = null;
    }
}
