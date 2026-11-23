package mpv5.utils.export;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.Component;
import java.awt.Container;
import java.io.File;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JComboBox;
import mpv5.db.common.Context;
import mpv5.db.objects.Contact;
import mpv5.db.objects.Product;
import mpv5.globals.Constants;
import mpv5.handler.ContactService;
import mpv5.handler.JournalCalc;
import mpv5.handler.JournalCalc.Entry;
import mpv5.handler.ProductService;
import mpv5.ui.beans.LabeledTextField;
import mpv5.ui.panels.ModernContactPanel;
import mpv5.ui.panels.ModernProductPanel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class EcSalesListAndCustomsTest {

    @TempDir
    Path tmp;

    private static Entry entry(int itemId, int type, int status, String number, String contact, String net) {
        Entry e = new Entry();
        e.itemId = itemId;
        e.type = type;
        e.status = status;
        e.number = number;
        e.contact = contact;
        e.net = new BigDecimal(net);
        e.gross = new BigDecimal(net);
        e.counted = true;
        return e;
    }

    @Test
    void ecSalesListAggregatesIntraCommunityGoodsAndServicesAndSubtractsCreditNotes() throws Exception {
        Entry invGoods1 = entry(1, Constants.TYPE_INVOICE, mpv5.db.objects.Item.STATUS_PAID, "RE-2026-001", "Alpen Metallbau GmbH", "1250.60");
        Entry invGoods2 = entry(2, Constants.TYPE_INVOICE, mpv5.db.objects.Item.STATUS_PAID, "RE-2026-002", "Alpen Metallbau GmbH", "300.30");
        Entry cnGoods = entry(3, Constants.TYPE_CREDIT, mpv5.db.objects.Item.STATUS_PAID, "GS-2026-001", "Alpen Metallbau GmbH", "50.00");
        Entry invService = entry(4, Constants.TYPE_INVOICE, mpv5.db.objects.Item.STATUS_PAID, "RE-2026-003", "Paris Consulting SAS", "2400.00");
        Entry invCancelled = entry(5, Constants.TYPE_INVOICE, mpv5.db.objects.Item.STATUS_CANCELLED, "RE-2026-004", "Paris Consulting SAS", "999.00");
        Entry invMissingVat = entry(6, Constants.TYPE_INVOICE, mpv5.db.objects.Item.STATUS_PAID, "RE-2026-005", "NoVat BV", "500.00");
        Entry invThirdCountryK = entry(7, Constants.TYPE_INVOICE, mpv5.db.objects.Item.STATUS_PAID, "RE-2026-006", "Swiss Corp", "800.00");

        EcSalesListExport.EntryInspector inspector = new EcSalesListExport.EntryInspector() {
            @Override
            public String vatIdOf(Entry e) {
                switch (e.itemId) {
                    case 1:
                    case 2:
                    case 3:
                        return "AT U12345678";
                    case 4:
                    case 5:
                        return "FR 12 345678901";
                    case 6:
                        return "";
                    case 7:
                        return "DE123456789"; // domestic DE on K -> flagged as non-other-EU
                    default:
                        return "";
                }
            }

            @Override
            public String customerNameOf(Entry e) {
                return e.contact;
            }

            @Override
            public Map<VatCategory, BigDecimal> netByCategory(Entry e) {
                Map<VatCategory, BigDecimal> m = new LinkedHashMap<VatCategory, BigDecimal>();
                BigDecimal sign = e.type == Constants.TYPE_CREDIT ? BigDecimal.ONE.negate() : BigDecimal.ONE;
                if (e.itemId == 4 || e.itemId == 5) {
                    m.put(VatCategory.AE, e.net.multiply(sign));
                } else {
                    m.put(VatCategory.K, e.net.multiply(sign));
                }
                return m;
            }
        };

        List<Entry> entries = Arrays.asList(invGoods1, invGoods2, cnGoods, invService, invCancelled, invMissingVat, invThirdCountryK);
        EcSalesListExport.Result res = EcSalesListExport.build(entries, inspector);

        assertEquals(2, res.lines.size(), "One aggregated row per (VAT ID, SupplyType)");
        EcSalesListExport.Line atLine = res.lines.get(0);
        assertEquals("AT", atLine.countryCode);
        assertEquals("U12345678", atLine.vatNumber);
        assertEquals("ATU12345678", atLine.vatId);
        assertEquals(EcSalesListExport.SupplyType.GOODS, atLine.type);
        assertEquals("L", atLine.type.code());
        assertEquals("0", atLine.type.elsterCode());
        assertEquals(new BigDecimal("1500.90"), atLine.netAmount, "1250.60 + 300.30 - 50.00 = 1500.90");
        assertEquals(new BigDecimal("1500"), atLine.roundedEuroAmount(), "§ 18a Abs. 7 UStG rounds down to whole EUR");
        assertEquals(Arrays.asList("RE-2026-001", "RE-2026-002", "GS-2026-001"), atLine.invoiceNumbers);

        EcSalesListExport.Line frLine = res.lines.get(1);
        assertEquals("FR", frLine.countryCode);
        assertEquals("12345678901", frLine.vatNumber);
        assertEquals("FR12345678901", frLine.vatId);
        assertEquals(EcSalesListExport.SupplyType.SERVICES, frLine.type);
        assertEquals("S", frLine.type.code());
        assertEquals("1", frLine.type.elsterCode());
        assertEquals(new BigDecimal("2400.00"), frLine.netAmount);
        assertEquals(new BigDecimal("2400"), frLine.roundedEuroAmount());

        assertEquals(new BigDecimal("3900.90"), res.totalNet());
        assertEquals(2, res.problems.size(), String.valueOf(res.problems));
        assertTrue(res.problems.get(0).contains("RE-2026-005"), res.problems.get(0));
        assertTrue(res.problems.get(1).contains("RE-2026-006"), res.problems.get(1));

        File csvFile = tmp.resolve("zm.csv").toFile();
        EcSalesListExport.writeCsv(res, csvFile);
        byte[] raw = Files.readAllBytes(csvFile.toPath());
        assertEquals((byte) 0xEF, raw[0], "UTF-8 BOM byte 0");
        assertEquals((byte) 0xBB, raw[1], "UTF-8 BOM byte 1");
        assertEquals((byte) 0xBF, raw[2], "UTF-8 BOM byte 2");
        String csvText = new String(raw, 3, raw.length - 3, StandardCharsets.UTF_8);
        assertTrue(csvText.contains("\"AT\";\"U12345678\";\"ATU12345678\";\"1500\";\"1500,90\";\"L\";\"0\""), csvText);
        assertTrue(csvText.contains("\"FR\";\"12345678901\";\"FR12345678901\";\"2400\";\"2400,00\";\"S\";\"1\""), csvText);
    }

    @Test
    void productCustomsValidationAndHelpers() {
        assertTrue(ProductService.isValidHsCode("84713000"));
        assertTrue(ProductService.isValidHsCode("8471.30.00"));
        assertTrue(ProductService.isValidHsCode("8471 30 00 00"));
        assertEquals("84713000", ProductService.normalizeHsCode("8471.30.00"));
        assertFalse(ProductService.isValidHsCode("123"), "too short");
        assertFalse(ProductService.isValidHsCode("847130001234"), "too long (> 11 digits)");
        assertFalse(ProductService.isValidHsCode("8471AB00"));

        assertTrue(ProductService.isValidOriginCountry("DE"));
        assertTrue(ProductService.isValidOriginCountry("Switzerland"));
        assertFalse(ProductService.isValidOriginCountry(""));
        assertFalse(ProductService.isValidOriginCountry("Atlantis"));

        assertEquals(new BigDecimal("1.25"), ProductService.parseWeightKg("1,25 kg"));
        assertEquals(new BigDecimal("0.5"), ProductService.parseWeightKg("0.5"));
        assertTrue(ProductService.isValidWeightKg("2,50"));
        assertFalse(ProductService.isValidWeightKg("-1"));
        assertFalse(ProductService.isValidWeightKg("abc"));
    }

    @Test
    void modernContactPanelUsesCurrencyDropdownForBankCurrency() throws Exception {
        ModernContactPanel panel = new ModernContactPanel(Context.getCustomer());
        Component comp = findByName(panel, "bankcurrency");
        assertNotNull(comp, "ModernContactPanel must contain bankcurrency component");
        assertTrue(comp instanceof JComboBox, "bankcurrency in ModernContactPanel must be a JComboBox dropdown");

        @SuppressWarnings("unchecked")
        JComboBox<ContactService.CurrencyOption> combo = (JComboBox<ContactService.CurrencyOption>) comp;
        assertTrue(combo.getItemCount() > 10, "Currency dropdown must list CurrencyCode options");

        Contact c = new Contact();
        c.setCname("Swiss Partner AG");
        c.setCompany("Swiss Partner AG");
        c.setisCompany(true);
        c.setBankcurrency("CHF");
        c.setPanelData(panel);
        panel.setUI();

        Object selected = combo.getSelectedItem();
        assertTrue(selected instanceof ContactService.CurrencyOption);
        assertEquals("CHF", ((ContactService.CurrencyOption) selected).code);
        assertEquals(CurrencyCode.CHF, ((ContactService.CurrencyOption) selected).currency);

        // Selecting USD and collecting data updates bankcurrency_
        List<ContactService.CurrencyOption> opts = ContactService.currencyOptions();
        combo.setSelectedItem(ContactService.currencyOption(opts, "USD"));
        assertTrue(panel.collectData());
        assertEquals("USD", panel.bankcurrency_);
    }

    @Test
    void modernProductPanelContainsCustomsFieldsWithValidation() {
        ModernProductPanel panel = new ModernProductPanel(Context.getProduct(), Product.TYPE_PRODUCT);
        Component hs = findByName(panel, ProductService.HS_CODE);
        Component origin = findByName(panel, ProductService.ORIGIN_COUNTRY);
        Component weight = findByName(panel, ProductService.NET_WEIGHT);

        assertTrue(hs instanceof LabeledTextField, "ModernProductPanel must have hsCode field");
        assertTrue(origin instanceof LabeledTextField, "ModernProductPanel must have originCountry field");
        assertTrue(weight instanceof LabeledTextField, "ModernProductPanel must have netWeight field");
    }

    private static Component findByName(Container root, String name) {
        for (Component c : root.getComponents()) {
            if (name.equals(c.getName())) {
                return c;
            }
            if (c instanceof Container) {
                Component found = findByName((Container) c, name);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }
}
