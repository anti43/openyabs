package org.openyabs.gmbh;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.xml.parsers.DocumentBuilderFactory;
import mpv5.db.objects.Item;
import mpv5.globals.Constants;
import mpv5.handler.JournalCalc;
import mpv5.handler.JournalCalc.Entry;
import mpv5.plugin.api.YabsExtension;
import mpv5.utils.export.EcSalesListExport;
import mpv5.utils.export.VatCategory;
import org.junit.jupiter.api.Test;

/**
 * The GmbH plugin: mandatory details, demo template, DATEV XML package, VAT
 * return check (without database)
 */
public class GmbhPluginTest {

    private static BigDecimal bd(String s) {
        return new BigDecimal(s);
    }

    private static Date date(String s) throws Exception {
        return new SimpleDateFormat("yyyy-MM-dd").parse(s);
    }

    @Test
    public void pluginIsListedAsService() {
        boolean found = false;
        for (YabsExtension e : ServiceLoader.load(YabsExtension.class)) {
            if (GmbhPlugin.ID.equals(e.id())) {
                found = true;
            }
        }
        assertTrue(found, "META-INF/services lists the GmbH plugin");
    }

    // ------------------------------------------------------------- details
    private static GmbhData data(Map<String, String> m) {
        return GmbhData.of(k -> m.get(k));
    }

    @Test
    public void legalLineHasAllDetails() {
        Map<String, String> m = new HashMap<String, String>();
        m.put("name", "Muster");
        m.put("city", "Berlin");
        m.put(GmbhData.K_LEGALFORM, "GmbH");
        m.put(GmbhData.K_COURT, "Charlottenburg");
        m.put(GmbhData.K_NUMBER, "12345 B");
        m.put(GmbhData.K_DIRECTORS, "Anna Muster\nBen Beispiel; Cem Test");
        GmbhData d = data(m);
        assertTrue(d.missing().isEmpty(), "seat falls back to the city");
        assertEquals("Amtsgericht Charlottenburg, HRB 12345 B", d.registerLine());
        assertEquals("Muster GmbH · Sitz: Berlin · Registergericht: Amtsgericht Charlottenburg, HRB 12345 B"
                + " · Geschäftsführer: Anna Muster, Ben Beispiel, Cem Test",
                d.legalLine("Sitz", "Registergericht", "Geschäftsführer", "AR"));
        Map<String, Object> f = d.templateFields("Sitz", "Registergericht", "Geschäftsführer", "AR");
        assertEquals("HRB 12345 B".length(), ("HRB " + f.get("gmbh.registernumber")).length());
        assertEquals("Anna Muster, Ben Beispiel, Cem Test", f.get("gmbh.managingdirectors"));
    }

    @Test
    public void missingDetailsAndLegalForm() {
        Map<String, String> m = new HashMap<String, String>();
        m.put("name", "Beispiel UG (haftungsbeschränkt)");
        GmbhData d = data(m);
        assertEquals("UG (haftungsbeschränkt)", d.legalForm, "the legal form of the name");
        assertEquals(Arrays.asList(GmbhData.K_SEAT, GmbhData.K_COURT, GmbhData.K_NUMBER, GmbhData.K_DIRECTORS), d.missing());
        //the name holds the legal form already: not twice
        m.put(GmbhData.K_SEAT, "Köln");
        assertTrue(data(m).legalLine("Sitz", "R", "G", "A").startsWith("Beispiel UG (haftungsbeschränkt) · Sitz: Köln"));
        m.put("name", "Alpha & Co");
        m.put(GmbhData.K_LEGALFORM, "GmbH & Co. KG");
        assertTrue(data(m).isCoKg());
    }

    // ------------------------------------------------------------- template
    @Test
    public void demoTemplateHasTheLegalLine() throws Exception {
        byte[] base = GmbhTemplate.baseTemplate();
        assertNotNull(base, "the demo invoice of YaBS");
        assertFalse(GmbhTemplate.printsDetails(base));
        byte[] odt = GmbhTemplate.withLegalLine(base);
        assertTrue(GmbhTemplate.printsDetails(odt));
        //again: not twice
        byte[] again = GmbhTemplate.withLegalLine(odt);
        String styles = null;
        String first = null;
        try (ZipInputStream z = new ZipInputStream(new ByteArrayInputStream(again))) {
            ZipEntry e;
            while ((e = z.getNextEntry()) != null) {
                if (first == null) {
                    first = e.getName();
                    assertEquals(ZipEntry.STORED, e.getMethod(), "mimetype not compressed");
                }
                if ("styles.xml".equals(e.getName())) {
                    styles = new String(z.readAllBytes(), StandardCharsets.UTF_8);
                }
            }
        }
        assertEquals("mimetype", first);
        assertNotNull(styles);
        assertEquals(1, styles.split("gmbh.legalline", -1).length - 1);
        //still well-formed
        DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(styles.getBytes(StandardCharsets.UTF_8)));
    }

    // ------------------------------------------------------------- VAT return
    private static Entry entry(int type, String number, int itemId, Object[]... rates) {
        Entry e = new Entry();
        e.type = type;
        e.status = Item.STATUS_PAID;
        e.number = number;
        e.itemId = itemId;
        BigDecimal net = BigDecimal.ZERO;
        BigDecimal tax = BigDecimal.ZERO;
        for (Object[] r : rates) {
            e.rates.put((BigDecimal) r[0], new BigDecimal[]{(BigDecimal) r[1], (BigDecimal) r[2]});
            net = net.add((BigDecimal) r[1]);
            tax = tax.add((BigDecimal) r[2]);
        }
        e.net = net;
        e.tax = tax;
        e.gross = net.add(tax);
        return e;
    }

    private static Object[] r(String rate, String net, String tax) {
        return new Object[]{rate == null ? null : bd(rate), bd(net), bd(tax)};
    }

    private static EcSalesListExport.EntryInspector inspector(final Map<String, Map<VatCategory, BigDecimal>> cats,
            final Map<String, String> vatIds) {
        return new EcSalesListExport.EntryInspector() {
            @Override
            public String vatIdOf(Entry entry) {
                return vatIds.getOrDefault(entry.number, "");
            }

            @Override
            public String customerNameOf(Entry entry) {
                return entry.number;
            }

            @Override
            public Map<VatCategory, BigDecimal> netByCategory(Entry entry) {
                return cats.getOrDefault(entry.number, Collections.<VatCategory, BigDecimal>emptyMap());
            }
        };
    }

    @Test
    public void vatReturnOnAccrualBasis() {
        List<Entry> l = new ArrayList<Entry>();
        Entry open = entry(Constants.TYPE_INVOICE, "R1", 1, r("19", "1000.55", "190.10"), r("7", "200.00", "14.00"));
        open.status = Item.STATUS_FINISHED;
        l.add(open);
        l.add(entry(Constants.TYPE_CREDIT, "G1", 2, r("19", "100.00", "19.00")));
        Entry cancelled = entry(Constants.TYPE_INVOICE, "R-X", 3, r("19", "5000.00", "950.00"));
        cancelled.status = Item.STATUS_CANCELLED;
        l.add(cancelled);
        l.add(entry(Constants.TYPE_INVOICE, "R-EU", 4, r("0", "800.00", "0.00")));
        l.add(entry(Constants.TYPE_INVOICE, "R-AE", 5, r("0", "300.00", "0.00"), r("19", "100.00", "19.00")));
        Entry offer = entry(Constants.TYPE_OFFER, "A1", 6, r("19", "9999.00", "1899.81"));
        l.add(offer);
        Entry ex = entry(Constants.TYPE_EXPENSE, "E1", 0, r("19", "50.00", "9.50"));
        ex.status = JournalCalc.EXPENSE_UNPAID;
        l.add(ex);

        Map<String, Map<VatCategory, BigDecimal>> cats = new HashMap<String, Map<VatCategory, BigDecimal>>();
        cats.put("R-EU", Collections.singletonMap(VatCategory.K, bd("800.00")));
        Map<VatCategory, BigDecimal> ae = new LinkedHashMap<VatCategory, BigDecimal>();
        ae.put(VatCategory.AE, bd("300.00"));
        ae.put(VatCategory.S, bd("100.00"));
        cats.put("R-AE", ae);
        Map<String, String> ids = new HashMap<String, String>();
        ids.put("R-EU", "ATU12345678");
        ids.put("R-AE", "FR12345678901");

        UstvaCalc.Result res = UstvaCalc.compute(UstvaCalc.Basis.ACCRUAL, l, l, inspector(cats, ids));
        //1000.55 - 100 + 100 = 1000.55 -> 1000 full Euros, VAT 190.00
        assertEquals(bd("1000.55"), res.line(UstvaCalc.KZ_81).base);
        assertEquals(bd("1000"), res.line(UstvaCalc.KZ_81).formBase());
        assertEquals(bd("190.00"), res.tax81());
        assertEquals(bd("190.10"), res.line(UstvaCalc.KZ_81).bookedTax);
        assertEquals(bd("14.00"), res.tax86());
        assertEquals(bd("800.00"), res.line(UstvaCalc.KZ_41).base);
        assertEquals(bd("300.00"), res.line(UstvaCalc.KZ_21).base);
        assertEquals(bd("9.50"), res.tax66(), "input tax by document date, also unpaid");
        assertEquals(bd("194.50"), res.kz83());
        assertTrue(res.problems.isEmpty(), String.valueOf(res.problems.size()));
        assertTrue(res.line(UstvaCalc.KZ_81).documents.containsKey("G1"));
        assertEquals(bd("-100.00"), res.line(UstvaCalc.KZ_81).documents.get("G1"));
    }

    @Test
    public void vatReturnOnCashBasisAndProblems() {
        List<Entry> l = new ArrayList<Entry>();
        Entry open = entry(Constants.TYPE_INVOICE, "R1", 1, r("19", "1000.00", "190.00"));
        open.status = Item.STATUS_FINISHED;
        l.add(open);
        l.add(entry(Constants.TYPE_INVOICE, "R2", 2, r("19", "500.00", "95.00")));
        l.add(entry(Constants.TYPE_INVOICE, "R3", 3, r("0", "200.00", "0.00")));
        l.add(entry(Constants.TYPE_INVOICE, "R4", 4, r("0", "100.00", "0.00")));
        l.add(entry(Constants.TYPE_REVENUE, "E5", 0, r(null, "40.00", "0.00")));
        JournalCalc.compute(l, null);
        Map<String, Map<VatCategory, BigDecimal>> cats = new HashMap<String, Map<VatCategory, BigDecimal>>();
        cats.put("R3", Collections.singletonMap(VatCategory.K, bd("200.00")));
        cats.put("R4", Collections.singletonMap(VatCategory.AE, bd("100.00")));
        UstvaCalc.Result res = UstvaCalc.compute(UstvaCalc.Basis.CASH, l, Collections.<Entry>emptyList(),
                inspector(cats, Collections.<String, String>emptyMap()));
        assertEquals(bd("500.00"), res.line(UstvaCalc.KZ_81).base, "only what was paid");
        assertEquals(bd("200.00"), res.line(UstvaCalc.KZ_41).base);
        //reverse charge without EU VAT ID: field 45
        assertEquals(bd("140.00"), res.line(UstvaCalc.KZ_45).base);
        List<String> keys = new ArrayList<String>();
        for (String[] p : res.problems) {
            keys.add(p[0]);
        }
        assertTrue(keys.contains("ustva.problem.euvatid"));
        assertTrue(keys.contains("ustva.problem.reversecharge"));
        assertTrue(keys.contains("ustva.problem.nocategory"));
        assertEquals(bd("95.00"), res.kz83());
    }

    @Test
    public void periods() throws Exception {
        Date[] feb = UstvaPanel.period(2024, 1);
        assertEquals("2024-02-01", new SimpleDateFormat("yyyy-MM-dd").format(feb[0]));
        assertEquals("2024-02-29", new SimpleDateFormat("yyyy-MM-dd").format(feb[1]));
        Date[] q4 = UstvaPanel.period(2026, 15);
        assertEquals("2026-10-01", new SimpleDateFormat("yyyy-MM-dd").format(q4[0]));
        assertEquals("2026-12-31", new SimpleDateFormat("yyyy-MM-dd").format(q4[1]));
        Date[] y = UstvaPanel.period(2026, 16);
        assertEquals("2026-01-01", new SimpleDateFormat("yyyy-MM-dd").format(y[0]));
    }

    @Test
    public void translationsHaveTheSameKeys() throws Exception {
        java.util.Properties de = new java.util.Properties();
        java.util.Properties en = new java.util.Properties();
        de.load(new java.io.InputStreamReader(GmbhPlugin.class.getResourceAsStream(GmbhPlugin.I18N_DE), StandardCharsets.UTF_8));
        en.load(new java.io.InputStreamReader(GmbhPlugin.class.getResourceAsStream(GmbhPlugin.I18N_EN), StandardCharsets.UTF_8));
        assertEquals(de.stringPropertyNames(), en.stringPropertyNames());
        for (String k : de.stringPropertyNames()) {
            //patterns with arguments are MessageFormat patterns
            java.text.MessageFormat.format(de.getProperty(k), "a", "b", "c");
            java.text.MessageFormat.format(en.getProperty(k), "a", "b", "c");
        }
    }
}
