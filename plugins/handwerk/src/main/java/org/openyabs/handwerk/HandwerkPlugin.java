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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.File;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.Connection;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import mpv5.YabsViewProxy;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.db.common.DatabaseObjectModifier;
import mpv5.db.objects.Contact;
import mpv5.db.objects.Item;
import mpv5.db.objects.Product;
import mpv5.db.objects.SubItem;
import mpv5.db.objects.Tax;
import mpv5.handler.ItemService;
import mpv5.logging.Log;
import mpv5.plugin.api.ExtensionContext;
import mpv5.plugin.api.PrivacyContributor;
import mpv5.plugin.api.SearchResult;
import mpv5.plugin.api.YabsExtension;
import mpv5.ui.dialogs.DialogForFile;
import mpv5.ui.panels.ChangeNotApprovedException;
import mpv5.ui.panels.DataPanel;
import org.openyabs.handwerk.BoqDocument.BoqPosition;
import org.openyabs.handwerk.BoqDocument.PositionType;
import org.openyabs.handwerk.DatanormImporter.ImportSummary;
import org.openyabs.handwerk.DatanormImporter.ParseResult;
import org.openyabs.handwerk.HandwerkStore.ContactMeta;
import org.openyabs.handwerk.HandwerkStore.ItemMeta;
import org.openyabs.handwerk.HandwerkStore.LineCostType;
import org.openyabs.handwerk.HandwerkStore.Measurement;
import org.openyabs.handwerk.HandwerkStore.Settings;
import org.openyabs.handwerk.HandwerkStore.Site;
import org.openyabs.handwerk.HandwerkStore.TimeSheet;
import org.openyabs.handwerk.HandwerkValidator.LaborSplitSummary;
import org.openyabs.handwerk.HandwerkValidator.LineInput;

/**
 * Bilingual (DE/EN) Skilled Trades &amp; Construction / Handwerksbetriebe
 * plugin for OpenYaBS:
 * <ul>
 * <li>Multi-site management per customer ({@code trades_sites}) with BGB &amp;
 * VOB/B warranty, retention, and maintenance tracking,</li>
 * <li>German &amp; European compliance rules: § 35a EStG labor/material split,
 * § 14b UStG 2-year private notice, § 13b UStG Reverse Charge ({@code AE} /
 * {@code VATEX-EU-AE}) &amp; USt 1 TG tracking, § 48b EStG Bauabzugssteuer
 * exemption tracking, and VOB/B / BGB § 650m retention caps,</li>
 * <li>Format-neutral Bill of Quantities (BoQ) engine with built-in GAEB DA XML
 * (3.1–3.3) lossless {@code .X83 → .X84} round-trip, GAEB 90 {@code .D83 → .D84},
 * and pan-European CSV BoQ codecs,</li>
 * <li>DATANORM 4.0 &amp; 5.0 wholesaler catalog import, site measurements
 * (Aufmaß) with arithmetic formula evaluator, time &amp; material sheets
 * (Regieberichte), and DE/EN {@code .fodt} templates.</li>
 * </ul>
 */
public final class HandwerkPlugin implements YabsExtension {

    public static final String ID = "org.openyabs.handwerk";
    public static final String VERSION = "1.0.0";
    public static final String I18N_EN = "/org/openyabs/handwerk/i18n/messages_en.properties";
    public static final String I18N_DE = "/org/openyabs/handwerk/i18n/messages_de.properties";
    public static final String[] TEMPLATE_RESOURCES = {
        "/org/openyabs/handwerk/templates/trades_invoice_de.fodt",
        "/org/openyabs/handwerk/templates/trades_invoice_en.fodt",
        "/org/openyabs/handwerk/templates/trades_final_invoice_de.fodt",
        "/org/openyabs/handwerk/templates/trades_acceptance_de.fodt",
        "/org/openyabs/handwerk/templates/trades_timesheet_de.fodt"
    };

    private static final ObjectMapper JSON = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT);

    private ExtensionContext ctx;

    @Override
    public String id() {
        return ID;
    }

    @Override
    public String name() {
        return ctx != null ? ctx.tr("plugin.name") : "Skilled Trades & Construction / Handwerksbetriebe";
    }

    @Override
    public String description() {
        return ctx != null ? ctx.tr("plugin.description")
                : "Construction sites, § 35a/§ 14b/§ 13b/§ 48b compliance, VOB/B & BGB retention/warranty, GAEB DA XML / GAEB 90 / CSV BoQs, DATANORM 4/5, site measurements, and time sheets.";
    }

    @Override
    public String version() {
        return VERSION;
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

        ClassLoader cl = HandwerkPlugin.class.getClassLoader();
        yabs.addTranslationBundle("en", I18N_EN, cl);
        yabs.addTranslationBundle("de", I18N_DE, cl);

        if (HandwerkStore.hasDatabase()) {
            HandwerkStore.initSchema(HandwerkStore.connection());
        }

        yabs.addSettingsPage(
                yabs.tr("settings.title"),
                yabs.tr("settings.description"),
                () -> HandwerkUi.settingsPage(yabs));

        yabs.addTemplateFields(obj -> provideTemplateFields(obj, yabs));

        yabs.onView(panel -> {
            if (HandwerkStore.hasDatabase()) {
                HandwerkUi.decorate(panel, yabs);
            }
        });

        yabs.addRecordModifier(createSaveValidator(yabs));
        yabs.addSearchProvider(query -> search(query, yabs));
        yabs.addPrivacyContributor(createPrivacyContributor());

        yabs.addCommand(
                yabs.tr("cmd.importBoq.title"),
                yabs.tr("cmd.importBoq.desc"),
                () -> importBoqToCurrentItem(yabs));

        yabs.addCommand(
                yabs.tr("cmd.exportBoq.title"),
                yabs.tr("cmd.exportBoq.desc"),
                () -> exportBoqFromCurrentItem(yabs));

        yabs.addCommand(
                yabs.tr("cmd.importDatanorm.title"),
                yabs.tr("cmd.importDatanorm.desc"),
                () -> importDatanorm(yabs));

        yabs.addCommand(
                yabs.tr("cmd.overview.title"),
                yabs.tr("cmd.overview.desc"),
                () -> HandwerkUi.openOverviewTab(yabs));

        yabs.addCommand(
                yabs.tr("cmd.importCatalog.title"),
                yabs.tr("cmd.importCatalog.desc"),
                () -> yabs.background(() -> importStarterCatalog(yabs), msg -> yabs.popup(msg)));

        yabs.addCommand(
                yabs.tr("cmd.check.title"),
                yabs.tr("cmd.check.desc"),
                () -> checkCurrentDocument(yabs));

        yabs.addCommand(
                yabs.tr("cmd.exportTemplates.title"),
                yabs.tr("cmd.exportTemplates.desc"),
                () -> exportTemplates(yabs));
    }

    // --------------------------------------------------- template fields

    private Map<String, Object> provideTemplateFields(DatabaseObject obj, ExtensionContext yabs) {
        if (!HandwerkStore.hasDatabase()) {
            return Collections.emptyMap();
        }
        Settings settings = Settings.load();
        if (obj instanceof Item) {
            Item item = (Item) obj;
            ContactMeta cm = HandwerkStore.loadContactMetaSafe(item.__getContactsids());
            ItemMeta im = HandwerkStore.loadItemMetaSafe(item.__getIDS());
            Site site = HandwerkStore.loadSiteSafe(im.siteId);
            if (site == null && item.__getContactsids() > 0) {
                List<Site> list = HandwerkStore.loadSitesForContactSafe(item.__getContactsids(), false);
                if (list.size() == 1) {
                    site = list.get(0);
                }
            }
            List<LineInput> lines = lineInputsOf(item);
            Map<Integer, LineCostType> overrides = HandwerkStore.loadLineCostTypesSafe(item.__getIDS());
            LaborSplitSummary split = HandwerkValidator.computeLaborSplit(lines, overrides, BigDecimal.ZERO);
            BigDecimal net = item.getNetAfterDiscount();
            BigDecimal gross = item.getGrossAmount();
            BoqDocument boq = HandwerkStore.loadBoqForItemSafe(item.__getIDS());
            List<Measurement> measurements = HandwerkStore.loadMeasurementsForItemSafe(item.__getIDS());
            List<TimeSheet> timeSheets = HandwerkStore.loadTimeSheetsForItemSafe(item.__getIDS());
            return HandwerkValidator.templateFields(
                    cm, site, im, split, net, gross, boq, measurements, timeSheets, settings, yabs);
        }
        if (obj instanceof Contact) {
            ContactMeta cm = HandwerkStore.loadContactMetaSafe(obj.__getIDS());
            List<Site> sites = HandwerkStore.loadSitesForContactSafe(obj.__getIDS(), false);
            Site site = sites.isEmpty() ? null : sites.get(0);
            return HandwerkValidator.templateFields(
                    cm, site, null, null, BigDecimal.ZERO, BigDecimal.ZERO, null, null, null, settings, yabs);
        }
        return Collections.emptyMap();
    }

    // -------------------------------------------------------- save validator

    private DatabaseObjectModifier createSaveValidator(final ExtensionContext yabs) {
        return new DatabaseObjectModifier() {
            @Override
            public DatabaseObject modifyOnExplode(DatabaseObject object) {
                return object;
            }

            @Override
            public DatabaseObject modifyOnSave(DatabaseObject object) throws ChangeNotApprovedException {
                if (!(object instanceof Item) || !HandwerkStore.hasDatabase()) {
                    return object;
                }
                Item item = (Item) object;
                if (!HandwerkUi.isTradesDocType(item.__getInttype())) {
                    return object;
                }
                Settings settings = Settings.load();
                if (!settings.warnOnSave) {
                    return object;
                }
                try {
                    if (!HandwerkStore.hasItemMeta(HandwerkStore.connection(), item.__getIDS())) {
                        return object;
                    }
                    List<String> warnings = validateItem(item, yabs);
                    if (!warnings.isEmpty()) {
                        if (mpv5.ui.dialogs.Popup.isSilent() || java.awt.GraphicsEnvironment.isHeadless()) {
                            Log.Debug(HandwerkPlugin.class, "Compliance warnings on silent save: " + warnings);
                            return object;
                        }
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
                    Log.Debug(HandwerkPlugin.class, e);
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

    // ------------------------------------------------------- search provider

    private List<SearchResult> search(String query, ExtensionContext yabs) {
        List<SearchResult> results = new ArrayList<SearchResult>();
        if (query == null || query.trim().length() < 2 || !HandwerkStore.hasDatabase()) {
            return results;
        }
        try {
            Connection conn = HandwerkStore.connection();
            for (Site s : HandwerkStore.searchSites(conn, query)) {
                final int contactId = s.contactId;
                results.add(new SearchResult(
                        yabs.tr("search.site.title", s.displayLabel()),
                        yabs.tr("search.site.detail", s.contractType, s.warrantyUntil),
                        () -> openRecord(Context.getContact(), contactId)));
            }
            for (BoqPosition p : HandwerkStore.searchBoqPositions(conn, query)) {
                BoqDocument boq = HandwerkStore.loadBoqDocument(conn, p.boqId);
                final int itemId = boq != null ? boq.itemId : 0;
                results.add(new SearchResult(
                        yabs.tr("search.boq.title", p.ordinalNo, p.shortText),
                        yabs.tr("search.boq.detail", boq != null ? boq.projectName : "",
                                p.quantity.stripTrailingZeros().toPlainString(), p.unit),
                        () -> {
                            if (itemId > 0) {
                                openItemById(itemId);
                            }
                        }));
            }
        } catch (Exception e) {
            Log.Debug(HandwerkPlugin.class, e);
        }
        return results;
    }

    private static void openRecord(Context ctx, int id) {
        if (id <= 0) {
            return;
        }
        try {
            DatabaseObject obj = DatabaseObject.getObject(ctx, id);
            if (obj != null) {
                YabsViewProxy.instance().addOrShowTab(obj);
            }
        } catch (Exception e) {
            Log.Debug(HandwerkPlugin.class, e);
        }
    }

    static void openItemById(int itemId) {
        if (itemId <= 0) {
            return;
        }
        for (Context c : new Context[]{
            Context.getInvoice(), Context.getOffer(), Context.getOrder(),
            Context.getPartPayment(), Context.getDeposit(), Context.getCredit()
        }) {
            try {
                DatabaseObject obj = DatabaseObject.getObject(c, itemId);
                if (obj != null) {
                    YabsViewProxy.instance().addOrShowTab(obj);
                    return;
                }
            } catch (Exception ignored) {
            }
        }
    }

    // --------------------------------------------------- privacy contributor

    private PrivacyContributor createPrivacyContributor() {
        return new PrivacyContributor() {
            @Override
            public Date retentionUntil(Contact contact) {
                if (contact == null || contact.__getIDS() <= 0 || !HandwerkStore.hasDatabase()) {
                    return null;
                }
                LocalDate latest = null;
                for (Site s : HandwerkStore.loadSitesForContactSafe(contact.__getIDS(), true)) {
                    LocalDate w = HandwerkValidator.parseDate(s.warrantyUntil);
                    if (w != null && (latest == null || w.isAfter(latest))) {
                        latest = w;
                    }
                }
                return latest == null ? null : Date.from(latest.atStartOfDay(ZoneId.systemDefault()).toInstant());
            }

            @Override
            public Map<String, String> exportData(Contact contact) {
                if (contact == null || contact.__getIDS() <= 0 || !HandwerkStore.hasDatabase()) {
                    return Collections.emptyMap();
                }
                try {
                    Connection conn = HandwerkStore.connection();
                    ContactMeta cm = HandwerkStore.loadContactMeta(conn, contact.__getIDS());
                    List<Site> sites = HandwerkStore.loadSitesForContact(conn, contact.__getIDS(), true);
                    if (sites.isEmpty()
                            && cm.reverseChargeCertNo.isEmpty()
                            && cm.withholdingExemptionTaxNo.isEmpty()
                            && cm.notes.isEmpty()) {
                        return Collections.emptyMap();
                    }
                    Map<String, Object> root = new LinkedHashMap<String, Object>();
                    root.put("contact_meta", cm);
                    root.put("sites", sites);
                    List<TimeSheet> allTimeSheets = new ArrayList<TimeSheet>();
                    for (Site s : sites) {
                        allTimeSheets.addAll(HandwerkStore.loadTimeSheetsForSite(conn, s.id));
                    }
                    root.put("time_sheets", allTimeSheets);
                    Map<String, String> out = new LinkedHashMap<String, String>();
                    out.put("handwerk_trades_data.json", JSON.writeValueAsString(root));
                    return out;
                } catch (Exception e) {
                    Log.Debug(HandwerkPlugin.class, e);
                    return Collections.emptyMap();
                }
            }

            @Override
            public void onDeleteFinally(Contact contact) {
                if (contact == null || contact.__getIDS() <= 0 || !HandwerkStore.hasDatabase()) {
                    return;
                }
                try {
                    HandwerkStore.deleteContactData(HandwerkStore.connection(), contact.__getIDS());
                } catch (Exception e) {
                    Log.Debug(HandwerkPlugin.class, e);
                }
            }
        };
    }

    // --------------------------------------------------- validation helpers

    static List<LineInput> lineInputsOf(Item item) {
        List<LineInput> out = new ArrayList<LineInput>();
        if (item == null) {
            return out;
        }
        try {
            SubItem[] subs = item.getSubitems();
            if (subs != null) {
                for (SubItem si : subs) {
                    if (si != null) {
                        out.add(HandwerkValidator.fromSubItem(si));
                    }
                }
            }
        } catch (Exception e) {
            Log.Debug(HandwerkPlugin.class, e);
        }
        return out;
    }

    static BigDecimal annualCustomerVolume(int contactId, int excludeItemId) {
        BigDecimal total = BigDecimal.ZERO;
        if (contactId <= 0 || !HandwerkStore.hasDatabase()) {
            return total;
        }
        int currentYear = LocalDate.now().getYear();
        Calendar cal = Calendar.getInstance();
        for (Context c : new Context[]{Context.getInvoice(), Context.getPartPayment(), Context.getDeposit()}) {
            try {
                for (DatabaseObject o : DatabaseObject.getObjects(c)) {
                    Item it = (Item) o;
                    if (it.__getContactsids() != contactId || it.__getIDS() == excludeItemId) {
                        continue;
                    }
                    if (it.__getIntstatus() == Item.STATUS_CANCELLED) {
                        continue;
                    }
                    Date d = it.__getDateadded();
                    if (d != null) {
                        cal.setTime(d);
                        if (cal.get(Calendar.YEAR) != currentYear) {
                            continue;
                        }
                    }
                    BigDecimal gross = it.getGrossAmount();
                    if (gross != null) {
                        total = total.add(gross);
                    }
                }
            } catch (Exception ignored) {
            }
        }
        return total;
    }

    static List<String> validateItem(Item item, ExtensionContext yabs) {
        ContactMeta cm = HandwerkStore.loadContactMetaSafe(item.__getContactsids());
        ItemMeta im = HandwerkStore.loadItemMetaSafe(item.__getIDS());
        Site site = HandwerkStore.loadSiteSafe(im.siteId);
        List<LineInput> lines = lineInputsOf(item);
        Map<Integer, LineCostType> overrides = HandwerkStore.loadLineCostTypesSafe(item.__getIDS());
        BoqDocument boq = HandwerkStore.loadBoqForItemSafe(item.__getIDS());
        BigDecimal gross = item.getGrossAmount();
        BigDecimal annual = annualCustomerVolume(item.__getContactsids(), item.__getIDS()).add(gross == null ? BigDecimal.ZERO : gross);
        return HandwerkValidator.validate(
                item.__getInttype(), cm, site, im, lines, overrides, boq, gross, annual,
                Settings.load(), LocalDate.now(), yabs);
    }

    // --------------------------------------------------- BoQ / GAEB commands

    private void importBoqToCurrentItem(ExtensionContext yabs) {
        DataPanel view = yabs.currentView();
        if (view == null || !(view.getDataOwner() instanceof Item) || view.getDataOwner().__getIDS() <= 0) {
            yabs.popup(yabs.tr("cmd.boq.noDoc"));
            return;
        }
        importBoqIntoItem((Item) view.getDataOwner(), yabs);
    }

    static BoqDocument importBoqContentIntoItem(final Item item, final String fileName, final String content) throws Exception {
        BoqCodec codec = BoqCodec.findReader(fileName, content);
        if (codec == null) {
            throw new IllegalArgumentException("Unsupported BoQ file format: " + fileName);
        }
        BoqDocument boq = codec.read(fileName, content);
        boq.itemId = item.__getIDS();
        boq.importedAt = LocalDate.now().toString();

        ItemMeta im = HandwerkStore.loadItemMetaSafe(item.__getIDS());
        BigDecimal defaultTax = im.reverseChargeEnabled ? BigDecimal.ZERO : new BigDecimal("19.00");

        SubItem[] existing = item.getSubitems();
        int orderNr = existing == null ? 0 : existing.length;

        for (BoqPosition pos : boq.positions) {
            SubItem si = new SubItem();
            si.setItemsids(item.__getIDS());
            si.setOrdernr(orderNr++);
            si.setCountvalue(BigDecimal.ONE);
            si.setDateadded(new Date());
            si.setGroupsids(item.__getGroupsids());
            si.setCname(pos.ordinalNo);

            StringBuilder desc = new StringBuilder();
            if (!pos.ordinalNo.isEmpty()) {
                desc.append("OZ ").append(pos.ordinalNo).append(" ");
            }
            if (pos.positionType == PositionType.OPTIONAL) {
                desc.append("[EP/Bedarf] ");
            } else if (pos.positionType == PositionType.ALTERNATIVE) {
                desc.append("[Alt] ");
            }
            desc.append(pos.shortText);
            si.setDescription(desc.toString().trim());

            if (pos.positionType == PositionType.TEXT || !pos.isCountedInTotal()) {
                si.setInttype(pos.positionType == PositionType.TEXT ? SubItem.TYPE_TEXT : SubItem.TYPE_NORMAL);
                si.setQuantityvalue(pos.isCountedInTotal() ? pos.quantity : BigDecimal.ZERO);
            } else {
                si.setInttype(SubItem.TYPE_NORMAL);
                si.setQuantityvalue(pos.quantity);
            }
            si.setMeasure(pos.unit.isEmpty() ? "Stk" : pos.unit);
            si.setExternalvalue(pos.unitPrice);
            si.setInternalvalue(pos.unitPrice);
            si.setTaxpercentvalue(defaultTax);
            SubItem.calculate(si);
            if (si.save(true)) {
                pos.subItemId = si.__getIDS();
            }
        }

        ItemService.updateTotals(item);
        item.save(true);

        HandwerkStore.saveBoqDocument(HandwerkStore.connection(), boq);
        im.boqId = boq.id;
        HandwerkStore.saveItemMeta(HandwerkStore.connection(), im);
        return boq;
    }

    static void importBoqIntoItem(final Item item, final ExtensionContext yabs) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle(yabs.tr("cmd.importBoq.title"));
        if (chooser.showOpenDialog(null) != JFileChooser.APPROVE_OPTION || chooser.getSelectedFile() == null) {
            return;
        }
        final File file = chooser.getSelectedFile();
        yabs.background(() -> {
            try {
                byte[] bytes = Files.readAllBytes(file.toPath());
                String content = DatanormImporter.decodeBytes(bytes);
                if (BoqCodec.findReader(file.getName(), content) == null) {
                    return yabs.tr("cmd.importBoq.unsupported", file.getName());
                }
                BoqDocument boq = importBoqContentIntoItem(item, file.getName(), content);
                yabs.refreshView(item);
                return yabs.tr("cmd.importBoq.done", boq.positions.size(), boq.formatCode, boq.projectName);
            } catch (Exception ex) {
                Log.Debug(HandwerkPlugin.class, ex);
                return yabs.tr("cmd.importBoq.failed", ex.getMessage());
            }
        }, msg -> yabs.popup(msg));
    }

    private void exportBoqFromCurrentItem(ExtensionContext yabs) {
        DataPanel view = yabs.currentView();
        if (view == null || !(view.getDataOwner() instanceof Item) || view.getDataOwner().__getIDS() <= 0) {
            yabs.popup(yabs.tr("cmd.boq.noDoc"));
            return;
        }
        exportBoqFromItem((Item) view.getDataOwner(), yabs);
    }

    static byte[] exportBoqBytesFromItem(final Item item, final String producerVersion) throws Exception {
        BoqDocument boq = HandwerkStore.loadBoqForItemSafe(item.__getIDS());
        if (boq == null) {
            throw new IllegalStateException("No BoQ linked to item " + item.__getIDS());
        }
        syncBoqPricesFromSubItems(boq, item);
        BoqCodec codec = BoqCodec.findExporter(boq.formatCode);
        if (codec == null) {
            codec = new CsvBoqCodec();
        }
        Map<String, BigDecimal> prices = new LinkedHashMap<String, BigDecimal>();
        Map<String, String> bidderTexts = new LinkedHashMap<String, String>();
        for (BoqPosition p : boq.positions) {
            if (!p.ordinalNo.isEmpty()) {
                prices.put(p.ordinalNo, p.unitPrice == null ? BigDecimal.ZERO : p.unitPrice);
                if (p.bidderText != null && !p.bidderText.isEmpty()) {
                    bidderTexts.put(p.ordinalNo, p.bidderText);
                }
            }
        }
        byte[] data = codec.exportBid(boq, prices, bidderTexts, producerVersion);
        HandwerkStore.saveBoqDocument(HandwerkStore.connection(), boq);
        return data;
    }

    static void exportBoqFromItem(final Item item, final ExtensionContext yabs) {
        BoqDocument boq = HandwerkStore.loadBoqForItemSafe(item.__getIDS());
        if (boq == null) {
            yabs.popup(yabs.tr("cmd.exportBoq.noBoq"));
            return;
        }
        BoqCodec codec = BoqCodec.findExporter(boq.formatCode);
        if (codec == null) {
            codec = new CsvBoqCodec();
        }
        String baseName = item.__getCnumber() != null && !item.__getCnumber().trim().isEmpty()
                ? item.__getCnumber().trim().replaceAll("[^A-Za-z0-9._-]", "_")
                : ("bid_" + item.__getIDS());
        String defaultName = baseName + "." + codec.bidFileExtension();
        DialogForFile d = new DialogForFile(DialogForFile.FILES_ONLY, defaultName);
        if (!d.saveFile()) {
            return;
        }
        File target = d.getFile();
        if (target == null) {
            return;
        }
        try {
            byte[] data = exportBoqBytesFromItem(item, yabs.yabsVersion());
            Files.write(target.toPath(), data);
            yabs.notify(yabs.tr("cmd.exportBoq.done", target.getAbsolutePath()));
        } catch (Exception ex) {
            Log.Debug(HandwerkPlugin.class, ex);
            yabs.error(yabs.tr("cmd.exportBoq.failed", ex.getMessage()));
        }
    }

    static void syncBoqPricesFromSubItems(BoqDocument boq, Item item) {
        if (boq == null || item == null) {
            return;
        }
        Map<Integer, SubItem> byId = new HashMap<Integer, SubItem>();
        SubItem[] subs = item.getSubitems();
        if (subs != null) {
            for (SubItem si : subs) {
                if (si != null) {
                    byId.put(si.__getIDS(), si);
                }
            }
        }
        for (BoqPosition p : boq.positions) {
            SubItem si = byId.get(p.subItemId);
            if (si != null && si.__getExternalvalue() != null) {
                p.unitPrice = si.__getExternalvalue();
            }
        }
    }

    static void applyBoqPricesToSubItems(BoqDocument boq, Item item) {
        if (boq == null || item == null) {
            return;
        }
        Map<Integer, SubItem> byId = new HashMap<Integer, SubItem>();
        SubItem[] subs = item.getSubitems();
        if (subs != null) {
            for (SubItem si : subs) {
                if (si != null) {
                    byId.put(si.__getIDS(), si);
                }
            }
        }
        boolean changed = false;
        for (BoqPosition p : boq.positions) {
            SubItem si = byId.get(p.subItemId);
            if (si != null && p.unitPrice != null) {
                si.setExternalvalue(p.unitPrice);
                SubItem.calculate(si);
                si.save(true);
                changed = true;
            }
        }
        if (changed) {
            ItemService.updateTotals(item);
            item.save(true);
        }
    }

    // --------------------------------------------------- DATANORM command

    private void importDatanorm(final ExtensionContext yabs) {
        JFileChooser chooser = new JFileChooser();
        chooser.setMultiSelectionEnabled(true);
        chooser.setDialogTitle(yabs.tr("cmd.importDatanorm.title"));
        if (chooser.showOpenDialog(null) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        final File[] files = chooser.getSelectedFiles();
        if (files == null || files.length == 0) {
            return;
        }
        yabs.background(() -> {
            try {
                String[] payloads = new String[files.length];
                for (int i = 0; i < files.length; i++) {
                    payloads[i] = DatanormImporter.decodeBytes(Files.readAllBytes(files[i].toPath()));
                }
                ParseResult parsed = DatanormImporter.parse(payloads);
                Settings s = Settings.load();
                int defaultTaxId = Tax.getTaxId(new BigDecimal("19.00"));
                ImportSummary summary = DatanormImporter.importToProducts(parsed, s.datanormMarkupPercent, defaultTaxId);
                return yabs.tr("cmd.importDatanorm.done",
                        parsed.version, summary.created, summary.updated, summary.skipped);
            } catch (Exception ex) {
                Log.Debug(HandwerkPlugin.class, ex);
                return yabs.tr("cmd.importDatanorm.failed", ex.getMessage());
            }
        }, msg -> yabs.popup(msg));
    }

    // ---------------------------------------------- time sheet to invoice

    static int transferTimeSheetsToItem(Item item, List<TimeSheet> sheets, Settings settings, ExtensionContext yabs) {
        if (item == null || sheets == null || sheets.isEmpty()) {
            return 0;
        }
        Settings cfg = settings == null ? Settings.load() : settings;
        ItemMeta im = HandwerkStore.loadItemMetaSafe(item.__getIDS());
        BigDecimal taxPct = im.reverseChargeEnabled ? BigDecimal.ZERO : new BigDecimal("19.00");
        SubItem[] existing = item.getSubitems();
        int orderNr = existing == null ? 0 : existing.length;
        int added = 0;

        for (TimeSheet ts : sheets) {
            if (ts.billed) {
                continue;
            }
            if (ts.hours != null && ts.hours.signum() > 0) {
                SubItem si = new SubItem();
                si.setItemsids(item.__getIDS());
                si.setOrdernr(orderNr++);
                si.setInttype(SubItem.TYPE_NORMAL);
                si.setCountvalue(BigDecimal.ONE);
                si.setQuantityvalue(ts.hours);
                si.setMeasure("Std");
                si.setExternalvalue(rateForRole(ts.workerRole, cfg));
                si.setInternalvalue(BigDecimal.ZERO);
                si.setTaxpercentvalue(taxPct);
                si.setDateadded(new Date());
                si.setGroupsids(item.__getGroupsids());
                String desc = yabs.tr("ts.line.labor", ts.workDate, ts.workerName, ts.workerRole, ts.description);
                si.setDescription(desc);
                SubItem.calculate(si);
                if (si.save(true)) {
                    added++;
                    try {
                        LineCostType ct = new LineCostType();
                        ct.itemId = item.__getIDS();
                        ct.subItemId = si.__getIDS();
                        ct.costType = HandwerkStore.COST_LABOR;
                        ct.laborSharePercent = new BigDecimal("100.00");
                        HandwerkStore.saveLineCostType(HandwerkStore.connection(), ct);
                    } catch (Exception ignored) {
                    }
                }
            }
            if (ts.travelKm != null && ts.travelKm.signum() > 0) {
                SubItem si = new SubItem();
                si.setItemsids(item.__getIDS());
                si.setOrdernr(orderNr++);
                si.setInttype(SubItem.TYPE_NORMAL);
                si.setCountvalue(BigDecimal.ONE);
                si.setQuantityvalue(ts.travelKm);
                si.setMeasure("km");
                si.setExternalvalue(new BigDecimal("0.65"));
                si.setInternalvalue(BigDecimal.ZERO);
                si.setTaxpercentvalue(taxPct);
                si.setDateadded(new Date());
                si.setGroupsids(item.__getGroupsids());
                si.setDescription(yabs.tr("ts.line.travel", ts.workDate, ts.workerName));
                SubItem.calculate(si);
                if (si.save(true)) {
                    added++;
                    try {
                        LineCostType ct = new LineCostType();
                        ct.itemId = item.__getIDS();
                        ct.subItemId = si.__getIDS();
                        ct.costType = HandwerkStore.COST_TRAVEL;
                        ct.laborSharePercent = new BigDecimal("100.00");
                        HandwerkStore.saveLineCostType(HandwerkStore.connection(), ct);
                    } catch (Exception ignored) {
                    }
                }
            }
            ts.billed = true;
            try {
                HandwerkStore.saveTimeSheet(HandwerkStore.connection(), ts);
            } catch (Exception ignored) {
            }
        }
        if (added > 0) {
            ItemService.updateTotals(item);
            item.save(true);
            if (yabs != null) {
                yabs.refreshView(item);
            }
        }
        return added;
    }

    private static BigDecimal rateForRole(String role, Settings cfg) {
        if (HandwerkStore.ROLE_MASTER.equalsIgnoreCase(role)) {
            return cfg.rateMaster;
        }
        if (HandwerkStore.ROLE_APPRENTICE.equalsIgnoreCase(role)) {
            return cfg.rateApprentice;
        }
        return cfg.rateJourneyman;
    }

    // ----------------------------------------- starter catalog & § 13b tax

    String importStarterCatalog(ExtensionContext yabs) {
        try {
            boolean taxCreated = ensureReverseChargeTax(yabs);

            Set<String> existing = new HashSet<String>();
            try {
                for (DatabaseObject o : DatabaseObject.getObjects(Context.getProduct())) {
                    Product p = (Product) o;
                    if (p.__getCnumber() != null) {
                        existing.add(p.__getCnumber().trim().toUpperCase(Locale.ROOT));
                    }
                }
            } catch (Exception ignored) {
            }

            Settings s = Settings.load();
            Object[][] items = {
                {"HW-MEISTER", yabs.tr("catalog.master.name"), yabs.tr("catalog.master.desc"), s.rateMaster, "Std"},
                {"HW-GESELLE", yabs.tr("catalog.journeyman.name"), yabs.tr("catalog.journeyman.desc"), s.rateJourneyman, "Std"},
                {"HW-AZUBI", yabs.tr("catalog.apprentice.name"), yabs.tr("catalog.apprentice.desc"), s.rateApprentice, "Std"},
                {"HW-ANFAHRT", yabs.tr("catalog.travel.name"), yabs.tr("catalog.travel.desc"), s.rateTravel, "Psch"},
                {"HW-MASCHINE", yabs.tr("catalog.machine.name"), yabs.tr("catalog.machine.desc"), new BigDecimal("25.00"), "Std"},
                {"HW-WARTUNG", yabs.tr("catalog.maintenance.name"), yabs.tr("catalog.maintenance.desc"), s.rateJourneyman.multiply(new BigDecimal("1.5")).setScale(2, RoundingMode.HALF_UP), "Psch"},
                {"HW-KLEINMAT", yabs.tr("catalog.smallparts.name"), yabs.tr("catalog.smallparts.desc"), new BigDecimal("15.00"), "Psch"}
            };

            int created = 0;
            int skipped = 0;
            for (Object[] row : items) {
                String sku = (String) row[0];
                if (existing.contains(sku.toUpperCase(Locale.ROOT))) {
                    skipped++;
                    continue;
                }
                Product p = new Product();
                p.setInttype("HW-KLEINMAT".equals(sku) ? Product.TYPE_PRODUCT : Product.TYPE_SERVICE);
                p.setCnumber(sku);
                p.setCname((String) row[1]);
                p.setDescription((String) row[2]);
                p.setExternalnetvalue((BigDecimal) row[3]);
                p.setMeasure((String) row[4]);
                if (p.save(true)) {
                    created++;
                }
            }
            return yabs.tr("cmd.importCatalog.done", created, skipped,
                    taxCreated ? yabs.tr("cmd.importCatalog.taxCreated") : yabs.tr("cmd.importCatalog.taxExists"));
        } catch (Exception ex) {
            Log.Debug(HandwerkPlugin.class, ex);
            return yabs.tr("cmd.importCatalog.failed", ex.getMessage());
        }
    }

    private static boolean ensureReverseChargeTax(ExtensionContext yabs) {
        try {
            for (DatabaseObject o : DatabaseObject.getObjects(Context.getTaxes())) {
                Tax t = (Tax) o;
                if ("AE".equalsIgnoreCase(t.__getCategorycode())
                        && t.__getTaxvalue() != null && t.__getTaxvalue().signum() == 0) {
                    return false;
                }
            }
            Tax rc = new Tax();
            rc.setCname(yabs.tr("tax.rc13b.name"));
            rc.setTaxvalue(BigDecimal.ZERO);
            rc.setCountry("DE");
            rc.setCategorycode("AE");
            rc.setExemptioncode("VATEX-EU-AE");
            rc.setExemptionnote(yabs.tr("tpl.notice13b"));
            return rc.save(true);
        } catch (Exception e) {
            Log.Debug(HandwerkPlugin.class, e);
            return false;
        }
    }

    // --------------------------------------------------- check & templates

    private void checkCurrentDocument(ExtensionContext yabs) {
        DataPanel view = yabs.currentView();
        if (view == null || !(view.getDataOwner() instanceof Item)) {
            yabs.popup(yabs.tr("cmd.check.noDoc"));
            return;
        }
        Item item = (Item) view.getDataOwner();
        List<String> warnings = validateItem(item, yabs);
        HandwerkUi.showValidationResult(null, warnings, yabs);
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
                try (InputStream in = HandwerkPlugin.class.getResourceAsStream(res)) {
                    if (in == null) {
                        continue;
                    }
                    Files.write(new File(dir, name).toPath(), in.readAllBytes());
                    count++;
                }
            }
            yabs.notify(yabs.tr("cmd.exportTemplates.done", count, dir.getAbsolutePath()));
        } catch (Exception e) {
            Log.Debug(HandwerkPlugin.class, e);
            yabs.error(yabs.tr("cmd.exportTemplates.failed", e.getMessage()));
        }
    }

    ExtensionContext context() {
        return ctx;
    }

    @Override
    public void stop() {
        this.ctx = null;
    }
}
