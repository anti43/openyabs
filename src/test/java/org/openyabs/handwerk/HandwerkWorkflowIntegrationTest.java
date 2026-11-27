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

import java.io.File;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import mpv5.db.common.ConnectionTypeHandler;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseConnection;
import mpv5.db.common.DatabaseInstallation;
import mpv5.db.common.DatabaseObject;
import mpv5.db.common.QueryHandler;
import mpv5.db.objects.Contact;
import mpv5.db.objects.Item;
import mpv5.db.objects.Product;
import mpv5.db.objects.SubItem;
import mpv5.db.objects.Tax;
import mpv5.db.objects.User;
import mpv5.db.objects.ValueProperty;
import mpv5.globals.LocalSettings;
import mpv5.handler.ItemService;
import mpv5.handler.Privacy;
import mpv5.plugin.ExtensionManager;
import mpv5.plugin.api.PrivacyContributor;
import mpv5.utils.export.ZugpferdFile;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.io.TempDir;
import org.openyabs.handwerk.BoqDocument.BoqPosition;
import org.openyabs.handwerk.DatanormImporter.ImportSummary;
import org.openyabs.handwerk.DatanormImporter.ParseResult;
import org.openyabs.handwerk.HandwerkStore.ContactMeta;
import org.openyabs.handwerk.HandwerkStore.ItemMeta;
import org.openyabs.handwerk.HandwerkStore.LineCostType;
import org.openyabs.handwerk.HandwerkStore.Measurement;
import org.openyabs.handwerk.HandwerkStore.Settings;
import org.openyabs.handwerk.HandwerkStore.Site;
import org.openyabs.handwerk.HandwerkStore.TimeSheet;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Full end-to-end integration test running {@link HandwerkPlugin} inside a real
 * OpenYaBS H2 database installation (with {@link ExtensionManager},
 * {@link Contact}, {@link Item}, {@link SubItem}, {@link Product}, {@link Tax},
 * {@link ZugpferdFile} XRechnung/ZUGFeRD validation, Global Search, and GDPR
 * {@link Privacy}).
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class HandwerkWorkflowIntegrationTest {

    @TempDir
    static Path tmp;

    private static HandwerkPlugin plugin;
    private static Tax reverseChargeTax;

    @BeforeAll
    static void bootRealOpenYabsWithHandwerkPlugin() throws Exception {
        Path sysTmp = tmp.resolve("systemp");
        Files.createDirectories(sysTmp);
        System.setProperty("java.io.tmpdir", sysTmp.toString());

        File dir = tmp.toFile();
        mpv5.Main.MPPATH = dir.getPath();
        mpv5.Main.SETTINGS_FILE = new File(dir, "settings.yabs").getPath();
        String location = tmp.resolve("h2").toString();
        LocalSettings.setProperty(LocalSettings.DBPATH, location);
        LocalSettings.setProperty(LocalSettings.DBDRIVER, ConnectionTypeHandler.H2_DRIVER);
        LocalSettings.setProperty(LocalSettings.DBNAME, "yabs");
        LocalSettings.setProperty(LocalSettings.DBUSER, "");
        LocalSettings.setProperty(LocalSettings.DBPASSWORD, "");
        LocalSettings.setProperty(LocalSettings.CACHE_DIR, new File(dir, "cache").getPath());
        LocalSettings.save(0);

        DatabaseConnection conn = new DatabaseConnection();
        assertTrue(conn.connect(ConnectionTypeHandler.H2_DRIVER, "", "", location, "yabs", "", true));
        assertTrue(conn.runQueries(new DatabaseInstallation().getStructure()));
        assertTrue(conn.runQueries(new DatabaseInstallation().getInitialData()));
        QueryHandler.instanceOf();

        User admin = (User) DatabaseObject.getObject(Context.getUser(), "admin");
        User._setUser(admin);
        mpv5.handler.FirstStartSetup.prepare(admin, true, null);

        // Configure company info required for Mustangproject XRechnung/ZUGFeRD validation
        String[][] company = {
            {"business", "Meisterbetrieb Elektro & SHK GmbH"},
            {"street", "Werkstattweg 7"},
            {"zip", "50667"},
            {"city", "Köln"},
            {"country", "DE"},
            {"vatid", "DE 999 999 999"},
            {"taxnumber", "214/5678/9012"},
            {"email", "info@meisterbetrieb.example"},
            {"phone", "0221 987654"},
            {"iban", "DE02120300000000202051"},
            {"bic", "BYLADEM1001"},
            {"bankname", "Handwerksbank"}
        };
        for (String[] kv : company) {
            admin.changeProperty("companyinfo." + kv[0], kv[1]);
        }
        admin.saveProperties();

        ExtensionManager mgr = ExtensionManager.instance();
        plugin = new HandwerkPlugin();
        ExtensionManager.Installed inst = mgr.register(plugin);
        assertTrue(mpv5.ui.dialogs.Popup.silently(() -> mgr.start(inst)), String.valueOf(inst.getError()));
        assertTrue(HandwerkStore.hasDatabase());
        assertEquals(HandwerkStore.SCHEMA_VERSION, HandwerkStore.getSchemaVersion(HandwerkStore.connection()));
    }

    @Test
    @Order(1)
    void settingsPersistenceAndStarterCatalogWithReverseChargeTax() throws Exception {
        Settings cfg = Settings.load();
        cfg.chamberName = "Handwerkskammer zu Köln";
        cfg.chamberNumber = "HWK-K-4711";
        cfg.ownFreistellungTaxNo = "214/5678/9012";
        cfg.ownFreistellungSecNo = "FS-2026-99";
        cfg.ownFreistellungValidUntil = "2028-12-31";
        cfg.rateMaster = new BigDecimal("88.00");
        cfg.rateJourneyman = new BigDecimal("72.00");
        cfg.rateApprentice = new BigDecimal("42.00");
        cfg.rateTravel = new BigDecimal("55.00");
        cfg.datanormMarkupPercent = new BigDecimal("35.00");
        cfg.defaultContractType = HandwerkStore.CONTRACT_VOB_B;
        cfg.defaultRetentionPercent = new BigDecimal("5.00");
        cfg.save();

        Settings reloaded = Settings.load();
        assertEquals("Handwerkskammer zu Köln", reloaded.chamberName);
        assertEquals("HWK-K-4711", reloaded.chamberNumber);
        assertEquals(new BigDecimal("88.00"), reloaded.rateMaster);
        assertEquals(new BigDecimal("35.00"), reloaded.datanormMarkupPercent);
        assertEquals(HandwerkStore.CONTRACT_VOB_B, reloaded.defaultContractType);

        // First starter catalog import creates 7 products + 0% AE Reverse Charge tax
        String firstMsg = plugin.importStarterCatalog(plugin.context());
        assertTrue(firstMsg.contains("7"), "Expected 7 starter products created: " + firstMsg);

        // Second run is idempotent (0 created, 7 skipped)
        String secondMsg = plugin.importStarterCatalog(plugin.context());
        assertTrue(secondMsg.contains("0") && secondMsg.contains("7"), "Second run must skip all 7: " + secondMsg);

        for (DatabaseObject o : DatabaseObject.getObjects(Context.getTaxes())) {
            Tax t = (Tax) o;
            if ("AE".equalsIgnoreCase(t.__getCategorycode())
                    && t.__getTaxvalue() != null && t.__getTaxvalue().signum() == 0) {
                reverseChargeTax = t;
                break;
            }
        }
        assertNotNull(reverseChargeTax, "0% AE Reverse Charge tax must exist in database");
        assertEquals("VATEX-EU-AE", reverseChargeTax.__getExemptioncode());
    }

    @Test
    @Order(2)
    void datanormImportCreatesAndUpdatesRealProductsWithMarkup() throws Exception {
        String rab = "R;RG20;2000;\n";
        String dn4 = ""
                + "A;N;DN-VENTIL-1;00;Thermostatventil 1/2 Zoll;Heimeier;1;0;Stk;2500;RG20;SHK;\n"
                + "B;N;DN-VENTIL-1;HEIMEIER12;;;4024052123456;\n"
                + "T;N;DN-VENTIL-1;1;Vernickeltes Messinggehäuse;\n"
                + "A;N;DN-ROHR-22;00;Kupferrohr 22x1mm;Stange;2;2;m;100000;;SHK;\n";

        ParseResult parsed = DatanormImporter.parse(rab, dn4);
        int tax19 = Tax.getTaxId(new BigDecimal("19.00"));
        ImportSummary first = DatanormImporter.importToProducts(parsed, new BigDecimal("30.00"), tax19);
        assertEquals(2, first.created);
        assertEquals(0, first.updated);

        // Verify persisted Product prices:
        // DN-VENTIL-1: 25.00 - 20% = 20.00 buy * 1.30 markup = 26.00 sell
        // DN-ROHR-22: 10.00 buy * 1.30 markup = 13.00 sell
        Product ventil = findProductBySku("DN-VENTIL-1");
        assertNotNull(ventil);
        assertEquals(0, new BigDecimal("20.00").compareTo(ventil.__getInternalnetvalue()));
        assertEquals(0, new BigDecimal("26.00").compareTo(ventil.__getExternalnetvalue()));
        assertEquals("4024052123456", ventil.__getEan());
        assertTrue(ventil.__getDescription().contains("Messinggehäuse"));

        Product rohr = findProductBySku("DN-ROHR-22");
        assertNotNull(rohr);
        assertEquals(0, new BigDecimal("10.00").compareTo(rohr.__getInternalnetvalue()));
        assertEquals(0, new BigDecimal("13.00").compareTo(rohr.__getExternalnetvalue()));

        // Second import with updated price -> updates existing SKUs without duplicating
        String priceUpdate = dn4 + "P;N;DN-ROHR-22;2;120000;\n";
        ImportSummary second = DatanormImporter.importToProducts(
                DatanormImporter.parse(rab, priceUpdate), new BigDecimal("30.00"), tax19);
        assertEquals(0, second.created);
        assertEquals(2, second.updated);

        Product rohrUpdated = findProductBySku("DN-ROHR-22");
        assertEquals(0, new BigDecimal("12.00").compareTo(rohrUpdated.__getInternalnetvalue()));
        assertEquals(0, new BigDecimal("15.60").compareTo(rohrUpdated.__getExternalnetvalue()));
    }

    @Test
    @Order(3)
    void privateInvoiceWithSite35aSplitTemplateFieldsAndValidation() throws Exception {
        Contact homeowner = createContact("Familie Weber", "Gartenweg 9", "50670", "Köln",
                "weber@home.example", "");
        ContactMeta cm = new ContactMeta();
        cm.contactId = homeowner.__getIDS();
        cm.customerKind = HandwerkStore.CUSTOMER_PRIVATE;
        HandwerkStore.saveContactMeta(HandwerkStore.connection(), cm);

        Site site = new Site();
        site.contactId = homeowner.__getIDS();
        site.projectNo = "BV-PRIV-01";
        site.siteName = "Einfamilienhaus Weber";
        site.street = "Gartenweg 9";
        site.zip = "50670";
        site.city = "Köln";
        site.contractType = HandwerkStore.CONTRACT_BGB;
        site.acceptanceDate = "2026-11-15";
        site.warrantyYears = 5;
        site.warrantyUntil = HandwerkValidator.computeWarrantyUntil(site.acceptanceDate, site.contractType, 5);
        HandwerkStore.saveSite(HandwerkStore.connection(), site);

        Item inv = createItem(homeowner, Item.TYPE_INVOICE, "RE-PRIV-001");
        Product master = findProductBySku("HW-MEISTER");
        Product ventil = findProductBySku("DN-VENTIL-1");
        assertNotNull(master);
        assertNotNull(ventil);

        SubItem sLabor = addSubItem(inv, 1, "Meisterstunde Heizungswartung", "Std",
                new BigDecimal("4"), new BigDecimal("88.00"), new BigDecimal("19"), master.__getIDS());
        SubItem sMat = addSubItem(inv, 2, "Thermostatventil 1/2 Zoll", "Stk",
                new BigDecimal("2"), new BigDecimal("26.00"), new BigDecimal("19"), ventil.__getIDS());
        SubItem sPkg = addSubItem(inv, 3, "Hydraulischer Abgleich Komplettpaket", "Psch",
                BigDecimal.ONE, new BigDecimal("200.00"), new BigDecimal("19"), 0);
        ItemService.updateTotals(inv);
        inv = (Item) DatabaseObject.getObject(Context.getInvoice(), inv.__getIDS());

        // Override mixed package as 50% labor
        LineCostType ov = new LineCostType();
        ov.itemId = inv.__getIDS();
        ov.subItemId = sPkg.__getIDS();
        ov.costType = HandwerkStore.COST_LABOR;
        ov.laborSharePercent = new BigDecimal("50.00");
        HandwerkStore.saveLineCostType(HandwerkStore.connection(), ov);

        ItemMeta im = new ItemMeta();
        im.itemId = inv.__getIDS();
        im.siteId = site.id;
        im.siteSnapshot = site.displayLabel();
        im.laborTaxCreditEnabled = true;
        im.privateRetentionNotice = true;
        im.cashPayment = false;
        HandwerkStore.saveItemMeta(HandwerkStore.connection(), im);

        // Clean private invoice -> 0 warnings
        List<String> cleanWarnings = HandwerkPlugin.validateItem(inv, plugin.context());
        assertTrue(cleanWarnings.isEmpty(), "Expected no warnings, got: " + cleanWarnings);

        // Template fields resolved via live ExtensionManager
        Map<String, Object> fields = ExtensionManager.instance().templateFields(inv);
        // Labor net = 4*88 (352) + 50% of 200 (100) = 452.00; VAT = 85.88; Gross = 537.88
        assertEquals("452.00", fields.get("hw.laborNet"));
        assertEquals("537.88", fields.get("hw.laborGross"));
        // Material net = 2*26 (52) + 50% of 200 (100) = 152.00; Gross = 180.88
        assertEquals("152.00", fields.get("hw.materialNet"));
        assertEquals("180.88", fields.get("hw.materialGross"));
        assertEquals("Handwerkskammer zu Köln", fields.get("hw.chamberName"));
        assertEquals("BV-PRIV-01", fields.get("hw.siteProjectNo"));
        assertTrue(String.valueOf(fields.get("hw.notice35a")).contains("537.88"));
        assertTrue(String.valueOf(fields.get("hw.notice14b")).contains("14b"));

        // Toggling cash payment triggers § 35a warning
        im.cashPayment = true;
        HandwerkStore.saveItemMeta(HandwerkStore.connection(), im);
        List<String> cashWarnings = HandwerkPlugin.validateItem(inv, plugin.context());
        assertTrue(cashWarnings.stream().anyMatch(w -> w.contains("35a")));
        assertNotNull(sLabor);
        assertNotNull(sMat);
    }

    @Test
    @Order(4)
    void reverseCharge13bInvoiceValidatesAndExportsValidXRechnungAndZugferd() throws Exception {
        Contact builder = createContact("Generalbau Rhein GmbH", "Baustr. 44", "50668", "Köln",
                "rechnungen@generalbau.example", "DE123456789");
        ValueProperty.addOrUpdateProperty(ZugpferdFile.KEY_BUYER_REFERENCE, "PROJEKT-13B-2026", builder);

        ContactMeta cm = new ContactMeta();
        cm.contactId = builder.__getIDS();
        cm.customerKind = HandwerkStore.CUSTOMER_CONSTRUCTION_13B;
        cm.reverseChargeCertNo = "UST1TG-2026-4455";
        cm.reverseChargeCertUntil = "2028-12-31";
        HandwerkStore.saveContactMeta(HandwerkStore.connection(), cm);

        // Product explicitly linked to the plugin's 0% AE Reverse Charge tax
        Product rcService = new Product();
        rcService.setInttype(Product.TYPE_SERVICE);
        rcService.setCnumber("HW-BAU-13B");
        rcService.setCname("Rohinstallation Bauleistung (§ 13b UStG)");
        rcService.setExternalnetvalue(new BigDecimal("6000.00"));
        rcService.setMeasure("Psch");
        rcService.setTaxids(reverseChargeTax.__getIDS());
        assertTrue(rcService.save(true));

        Item inv = createItem(builder, Item.TYPE_INVOICE, "RE-13B-001");
        addSubItem(inv, 1, "Rohinstallation Bauleistung (§ 13b UStG)", "Psch",
                BigDecimal.ONE, new BigDecimal("6000.00"), BigDecimal.ZERO, rcService.__getIDS());
        ItemService.updateTotals(inv);
        inv = (Item) DatabaseObject.getObject(Context.getInvoice(), inv.__getIDS());

        ItemMeta im = new ItemMeta();
        im.itemId = inv.__getIDS();
        im.reverseChargeEnabled = true;
        im.retentionPercent = new BigDecimal("5.00");
        im.retentionBasis = HandwerkStore.BASIS_NET;
        im.retentionAmount = new BigDecimal("300.00");
        HandwerkStore.saveItemMeta(HandwerkStore.connection(), im);

        // Plugin compliance check passes with zero warnings (own § 48b & customer USt 1 TG both valid!)
        List<String> warnings = HandwerkPlugin.validateItem(inv, plugin.context());
        assertTrue(warnings.isEmpty(), "Compliant § 13b invoice must have 0 warnings: " + warnings);

        // Verify annual volume accumulation includes this EUR 6,000 invoice and ignores cancelled ones
        Item cancelled = createItem(builder, Item.TYPE_INVOICE, "RE-13B-STORNO");
        cancelled.setIntstatus(Item.STATUS_CANCELLED);
        addSubItem(cancelled, 1, "Stornierte Leistung", "Psch",
                BigDecimal.ONE, new BigDecimal("9999.00"), BigDecimal.ZERO, rcService.__getIDS());
        ItemService.updateTotals(cancelled);
        cancelled.setIntstatus(Item.STATUS_CANCELLED);
        cancelled.save(true);
        BigDecimal vol = HandwerkPlugin.annualCustomerVolume(builder.__getIDS(), 0);
        assertEquals(0, new BigDecimal("6000.00").compareTo(vol),
                "Annual volume must include active invoice and exclude cancelled invoice");

        // Export real XRechnung 3.0 XML & ZUGFeRD 2.5.2 PDF and run Mustangproject validator!
        Path out = tmp.resolve("einvoice13b");
        Files.createDirectories(out);
        File xrechnung = out.resolve("RE-13B-001.xml").toFile();
        File zugferd = out.resolve("RE-13B-001.pdf").toFile();

        ZugpferdFile zx = new ZugpferdFile(inv, xrechnung, ZugpferdFile.Format.XRECHNUNG);
        assertNull(zx.waitFor());
        assertNotNull(zx.getValidation());
        assertTrue(zx.getValidation().isValid(), "XRechnung § 13b AE must pass validation: " + zx.getValidation());
        String xmlText = Files.readString(xrechnung.toPath(), StandardCharsets.UTF_8);
        assertTrue(xmlText.contains("<ram:CategoryCode>AE</ram:CategoryCode>"), "Must emit UNCL5305 AE");
        assertTrue(xmlText.contains("VATEX-EU-AE"), "Must emit VATEX-EU-AE exemption reason code");

        ZugpferdFile zz = new ZugpferdFile(inv, zugferd, ZugpferdFile.Format.ZUGFERD);
        assertNull(zz.waitFor());
        assertNotNull(zz.getValidation());
        assertTrue(zz.getValidation().isValid(), "ZUGFeRD § 13b AE must pass validation: " + zz.getValidation());
    }

    @Test
    @Order(5)
    void boqImportPriceSyncExportTimeSheetsAndMeasurementsEndToEnd() throws Exception {
        Contact client = createContact("Stadtwerke Musterstadt", "Rathausplatz 1", "50667", "Köln",
                "vergabe@stadtwerke.example", "DE555666777");
        Site site = new Site();
        site.contactId = client.__getIDS();
        site.projectNo = "BV-SW-2026";
        site.siteName = "Trafostation Nord";
        site.street = "Kraftwerkstr. 8";
        site.zip = "50667";
        site.city = "Köln";
        site.contractType = HandwerkStore.CONTRACT_VOB_B;
        site.acceptanceDate = "2026-11-20";
        site.warrantyYears = 4;
        site.warrantyUntil = "2030-11-20";
        HandwerkStore.saveSite(HandwerkStore.connection(), site);

        Item offer = createItem(client, Item.TYPE_OFFER, "ANG-LV-001");
        ItemMeta im = new ItemMeta();
        im.itemId = offer.__getIDS();
        im.siteId = site.id;
        im.siteSnapshot = site.displayLabel();
        HandwerkStore.saveItemMeta(HandwerkStore.connection(), im);

        String x83 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<GAEB xmlns=\"http://www.gaeb.de/GAEB_DA_XML/DA83/3.3\">\n"
                + "  <GAEBInfo><Version>3.3</Version><VersDate>2021-05</VersDate></GAEBInfo>\n"
                + "  <PrjInfo><NamePrj>Trafostation Nord</NamePrj></PrjInfo>\n"
                + "  <Award><AwardInfo><Cur>EUR</Cur></AwardInfo><BoQ ID=\"B1\"><BoQInfo><Name>Elektro</Name></BoQInfo>\n"
                + "    <BoQBody><BoQCtgy ID=\"C1\" RNoPart=\"01\"><LblTx><p>Kabeltrassen</p></LblTx><BoQBody><Itemlist>\n"
                + "      <Item ID=\"I1\" RNoPart=\"0010\"><Qty>50</Qty><QU>m</QU>\n"
                + "        <Description><CompleteText><OutlineText><OutlTxt><TextOutlTxt>Kabelrinne 200mm montieren</TextOutlTxt></OutlTxt></OutlineText></CompleteText></Description>\n"
                + "      </Item>\n"
                + "    </Itemlist></BoQBody></BoQCtgy></BoQBody>\n"
                + "  </BoQ></Award>\n"
                + "</GAEB>";

        // Import X83 directly into OpenYaBS Offer -> creates SubItems & links BoqDocument
        BoqDocument imported = HandwerkPlugin.importBoqContentIntoItem(offer, "trafo.X83", x83);
        assertEquals(2, imported.positions.size()); // 1 TEXT category + 1 NORMAL item
        assertTrue(imported.positions.get(1).subItemId > 0);
        assertEquals(1, imported.countMandatoryUnpriced(), "Imported tender item starts unpriced");

        // Price position via BoQ and push back to OpenYaBS SubItem + recalculate totals
        imported.positions.get(1).unitPrice = new BigDecimal("35.00");
        imported.positions.get(1).bidderText = "OBO Bettermann RKS 620";
        HandwerkStore.saveBoqDocument(HandwerkStore.connection(), imported);
        HandwerkPlugin.applyBoqPricesToSubItems(imported, offer);

        offer = (Item) DatabaseObject.getObject(Context.getOffer(), offer.__getIDS());
        assertEquals(0, new BigDecimal("1750.00").compareTo(offer.getNetAfterDiscount()),
                "50 m * 35.00 EUR = 1750.00 net");

        // Export X84 bid from live Offer & re-parse
        byte[] x84Bytes = HandwerkPlugin.exportBoqBytesFromItem(offer, "2.1.0");
        String x84 = new String(x84Bytes, StandardCharsets.UTF_8);
        assertTrue(x84.contains("GAEB_DA_XML/DA84/3.3"));
        assertTrue(x84.contains("<UP>35.00</UP>"));
        assertTrue(x84.contains("<IT>1750.00</IT>"));
        assertTrue(x84.contains("OBO Bettermann RKS 620"));

        // Add site measurements & time sheets and transfer unbilled time sheets to invoice lines
        Measurement meas = new Measurement();
        meas.siteId = site.id;
        meas.itemId = offer.__getIDS();
        meas.ordinalNo = "01.0010";
        meas.workDate = "2026-11-20";
        meas.description = "Trasse Ost + West";
        meas.formula = "28,5 + 21,5";
        meas.computedQty = MeasurementEvaluator.evaluate(meas.formula);
        meas.unit = "m";
        HandwerkStore.saveMeasurement(HandwerkStore.connection(), meas);

        TimeSheet ts = new TimeSheet();
        ts.siteId = site.id;
        ts.itemId = offer.__getIDS();
        ts.workDate = "2026-11-20";
        ts.workerName = "Klaus Meister";
        ts.workerRole = HandwerkStore.ROLE_MASTER;
        ts.hours = new BigDecimal("2.50");
        ts.travelKm = new BigDecimal("30.00");
        ts.description = "Zusatzanschluss Schaltschrank";
        ts.signedBy = "Bauleiter Schmidt";
        HandwerkStore.saveTimeSheet(HandwerkStore.connection(), ts);

        int addedLines = HandwerkPlugin.transferTimeSheetsToItem(
                offer, Arrays.asList(ts), Settings.load(), plugin.context());
        assertEquals(2, addedLines, "1 labor line + 1 travel km line added");
        // Calling a second time must transfer 0 lines because ts.billed is now true
        int secondTransfer = HandwerkPlugin.transferTimeSheetsToItem(
                offer, HandwerkStore.loadTimeSheetsForItem(HandwerkStore.connection(), offer.__getIDS()),
                Settings.load(), plugin.context());
        assertEquals(0, secondTransfer, "Already-billed time sheets must not be transferred twice");

        offer = (Item) DatabaseObject.getObject(Context.getOffer(), offer.__getIDS());
        // Net = 1750.00 (BoQ) + 2.5 * 88.00 (220.00 labor) + 30 * 0.65 (19.50 travel) = 1989.50
        assertEquals(0, new BigDecimal("1989.50").compareTo(offer.getNetAfterDiscount()),
                "Offer total after time-sheet transfer: " + offer.getNetAfterDiscount());

        // Global Search finds both the construction site AND the BoQ position
        List<Object[]> siteHits = ExtensionManager.instance().search("Trafostation");
        assertFalse(siteHits.isEmpty(), "Search anything must return matching construction site");
        List<Object[]> boqHits = ExtensionManager.instance().search("Kabelrinne");
        assertFalse(boqHits.isEmpty(), "Search anything must return matching BoQ position");
    }

    @Test
    @Order(6)
    void gdprPrivacyRetentionExportAndCascadeDelete() throws Exception {
        Contact c = createContact("DSGVO Bauherr", "Datenschutzstr. 3", "50667", "Köln",
                "bauherr@privacy.example", "");
        ContactMeta cm = new ContactMeta();
        cm.contactId = c.__getIDS();
        cm.customerKind = HandwerkStore.CUSTOMER_PRIVATE;
        cm.notes = "Privater Bauherr";
        HandwerkStore.saveContactMeta(HandwerkStore.connection(), cm);

        Site site = new Site();
        site.contactId = c.__getIDS();
        site.projectNo = "BV-GDPR-1";
        site.siteName = "Sanierung Dachgeschoss";
        site.acceptanceDate = "2026-06-01";
        site.warrantyYears = 5;
        site.warrantyUntil = "2031-06-01";
        HandwerkStore.saveSite(HandwerkStore.connection(), site);

        TimeSheet ts = new TimeSheet();
        ts.siteId = site.id;
        ts.workDate = "2026-05-15";
        ts.workerName = "Geselle Anton";
        ts.hours = new BigDecimal("8.00");
        ts.description = "Dachdämmung";
        HandwerkStore.saveTimeSheet(HandwerkStore.connection(), ts);

        // 1. Privacy.check honors the plugin's warranty retention deadline (2031-06-01)
        Privacy.Report report = Privacy.check(c);
        assertTrue(report.mustKeep(), "Active site warranty must trigger GDPR retention");
        assertNotNull(report.keepUntil);
        assertEquals("2031-06-01", new java.text.SimpleDateFormat("yyyy-MM-dd").format(report.keepUntil));

        // 2. Privacy.export bundles handwerk_trades_data.json into the GDPR ZIP
        File zipFile = tmp.resolve("gdpr-handwerk.zip").toFile();
        Privacy.export(c, zipFile);
        Set<String> entryNames = new HashSet<String>();
        String handwerkJson = null;
        try (ZipFile zf = new ZipFile(zipFile)) {
            for (ZipEntry entry : java.util.Collections.list(zf.entries())) {
                entryNames.add(entry.getName());
                if ("handwerk_trades_data.json".equals(entry.getName())) {
                    handwerkJson = new String(zf.getInputStream(entry).readAllBytes(), StandardCharsets.UTF_8);
                }
            }
        }
        assertTrue(entryNames.contains("handwerk_trades_data.json"), "ZIP entries: " + entryNames);
        assertNotNull(handwerkJson);
        assertTrue(handwerkJson.contains("BV-GDPR-1"));
        assertTrue(handwerkJson.contains("Sanierung Dachgeschoss"));
        assertTrue(handwerkJson.contains("Geselle Anton"));

        // 3. PrivacyContributor.onDeleteFinally cascades deletion across all plugin tables for the contact
        List<PrivacyContributor> contributors = ExtensionManager.instance().privacyContributors();
        assertFalse(contributors.isEmpty());
        for (PrivacyContributor pc : contributors) {
            pc.onDeleteFinally(c);
        }
        assertTrue(HandwerkStore.loadSitesForContact(HandwerkStore.connection(), c.__getIDS(), true).isEmpty());
        assertTrue(HandwerkStore.loadTimeSheetsForSite(HandwerkStore.connection(), site.id).isEmpty());
        assertEquals("", HandwerkStore.loadContactMeta(HandwerkStore.connection(), c.__getIDS()).notes);
    }

    @Test
    @Order(7)
    void importTemplatesImportsAndAssignsCurrentUser() throws Exception {
        String firstMsg = plugin.importTemplates(plugin.context());
        assertNotNull(firstMsg);
        assertTrue(firstMsg.contains("5"), "Expected 5 imported templates: " + firstMsg);

        User currentUser = User.getCurrentUser();
        assertNotNull(currentUser);
        String[] expectedNames = {
            "Handwerk - Rechnung (DE)",
            "Trades - Invoice (EN)",
            "Handwerk - Schlussrechnung (DE)",
            "Handwerk - Abnahmeprotokoll (DE)",
            "Handwerk - Regiebericht (DE)"
        };
        for (String name : expectedNames) {
            mpv5.db.objects.Template tpl = mpv5.handler.TemplateImportService.findTemplateByName(name);
            assertNotNull(tpl, "Template must exist in DB: " + name);
            List<User> assigned = mpv5.handler.TemplateImportService.loadAssignedUsers(tpl);
            assertTrue(assigned.stream().anyMatch(u -> u.__getIDS().equals(currentUser.__getIDS())),
                    "Current user must be assigned to template: " + name);
            File storedFile = tpl.getFile();
            assertNotNull(storedFile);
            assertTrue(storedFile.isFile() && storedFile.length() > 0);
            assertNotNull(new mpv5.utils.export.ODTFile2(storedFile.getAbsolutePath()));
        }

        // Templates are immediately selectable via TemplateHandler for the current user
        mpv5.db.objects.Template[] invoiceTemplates = mpv5.handler.TemplateHandler.getTemplatesForType(
                currentUser.__getGroupsids(), Constants.TYPE_INVOICE);
        assertTrue(java.util.Arrays.stream(invoiceTemplates)
                .anyMatch(t -> t != null && "Handwerk - Rechnung (DE)".equals(t.__getCname())));

        // Second run is idempotent: 0 newly imported, 5 already existed
        String secondMsg = plugin.importTemplates(plugin.context());
        assertNotNull(secondMsg);
        assertTrue(secondMsg.contains("0") && secondMsg.contains("5"), "Expected idempotent summary: " + secondMsg);
    }

    // --------------------------------------------------------------- helpers

    private static Contact createContact(String name, String street, String zip, String city,
            String email, String vatId) {
        Contact c = (Contact) DatabaseObject.getObject(Context.getContact());
        c.setCname(name);
        c.setCompany(name);
        c.setStreet(street);
        c.setZip(zip);
        c.setCity(city);
        c.setCountry("DE");
        c.setMailaddress(email);
        if (vatId != null && !vatId.isEmpty()) {
            c.settaxnumber(vatId);
        }
        c.setisCustomer(true);
        c.setDateadded(new Date());
        assertTrue(c.save(true));
        return c;
    }

    private static Item createItem(Contact c, int type, String number) {
        Item it = (Item) DatabaseObject.getObject(ItemService.contextOf(type));
        it.setContactsids(c.__getIDS());
        it.setCname(number);
        it.setCnumber(number);
        it.setInttype(type);
        it.setDateadded(new Date());
        it.setDatetodo(new Date());
        it.setDateend(new Date());
        it.setAccountsids(1);
        assertTrue(it.save(true));
        return it;
    }

    private static SubItem addSubItem(Item item, int orderNr, String desc, String unit,
            BigDecimal qty, BigDecimal unitPrice, BigDecimal taxPct, int productId) {
        SubItem si = new SubItem();
        si.setItemsids(item.__getIDS());
        si.setOrdernr(orderNr);
        si.setInttype(SubItem.TYPE_NORMAL);
        si.setCountvalue(BigDecimal.ONE);
        si.setQuantityvalue(qty);
        si.setMeasure(unit);
        si.setDescription(desc);
        si.setExternalvalue(unitPrice);
        si.setInternalvalue(unitPrice);
        si.setTaxpercentvalue(taxPct);
        if (productId > 0) {
            si.setOriginalproductsids(productId);
        }
        si.setDateadded(new Date());
        si.setGroupsids(item.__getGroupsids());
        SubItem.calculate(si);
        assertTrue(si.save(true));
        return si;
    }

    private static Product findProductBySku(String sku) throws Exception {
        for (DatabaseObject o : DatabaseObject.getObjects(Context.getProduct())) {
            Product p = (Product) o;
            if (sku.equalsIgnoreCase(p.__getCnumber())) {
                return p;
            }
        }
        return null;
    }
}
