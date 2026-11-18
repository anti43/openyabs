package mpv5.ui.panels;

import java.awt.Component;
import java.awt.Container;
import java.io.File;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javax.swing.AbstractButton;
import javax.swing.JLabel;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import mpv5.db.objects.Account;
import mpv5.db.objects.Revenue;
import mpv5.globals.Headers;
import mpv5.utils.models.MPTableModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.*;

class ModernRevenuePanelTest {

    @TempDir
    Path tempDir;

    private static final String SAMPLE_CII_XML =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<rsm:CrossIndustryInvoice xmlns:rsm=\"urn:un:unece:uncefact:data:standard:CrossIndustryInvoice:100\"\n"
            + "    xmlns:ram=\"urn:un:unece:uncefact:data:standard:ReusableAggregateBusinessInformationEntity:100\"\n"
            + "    xmlns:udt=\"urn:un:unece:uncefact:data:standard:UnqualifiedDataType:100\">\n"
            + "  <rsm:ExchangedDocument>\n"
            + "    <ram:ID>REV-2026-789</ram:ID>\n"
            + "    <ram:IssueDateTime>\n"
            + "      <udt:DateTimeString format=\"102\">20260415</udt:DateTimeString>\n"
            + "    </ram:IssueDateTime>\n"
            + "  </rsm:ExchangedDocument>\n"
            + "  <rsm:SupplyChainTradeTransaction>\n"
            + "    <ram:IncludedSupplyChainTradeLineItem>\n"
            + "      <ram:SpecifiedTradeProduct>\n"
            + "        <ram:Name>Beratungsleistung Consulting</ram:Name>\n"
            + "      </ram:SpecifiedTradeProduct>\n"
            + "    </ram:IncludedSupplyChainTradeLineItem>\n"
            + "    <ram:ApplicableHeaderTradeAgreement>\n"
            + "      <ram:SellerTradeParty>\n"
            + "        <ram:Name>Agentur Sued GmbH</ram:Name>\n"
            + "      </ram:SellerTradeParty>\n"
            + "    </ram:ApplicableHeaderTradeAgreement>\n"
            + "    <ram:ApplicableHeaderTradeSettlement>\n"
            + "      <ram:InvoiceCurrencyCode>EUR</ram:InvoiceCurrencyCode>\n"
            + "      <ram:ApplicableTradeTax>\n"
            + "        <ram:TypeCode>VAT</ram:TypeCode>\n"
            + "        <ram:RateApplicablePercent>19.00</ram:RateApplicablePercent>\n"
            + "      </ram:ApplicableTradeTax>\n"
            + "      <ram:SpecifiedTradePaymentTerms>\n"
            + "        <ram:DueDateDateTime>\n"
            + "          <udt:DateTimeString format=\"102\">20260430</udt:DateTimeString>\n"
            + "        </ram:DueDateDateTime>\n"
            + "      </ram:SpecifiedTradePaymentTerms>\n"
            + "      <ram:SpecifiedTradeSettlementHeaderMonetarySummation>\n"
            + "        <ram:TaxBasisTotalAmount>500.00</ram:TaxBasisTotalAmount>\n"
            + "        <ram:TaxTotalAmount currencyID=\"EUR\">95.00</ram:TaxTotalAmount>\n"
            + "        <ram:GrandTotalAmount>595.00</ram:GrandTotalAmount>\n"
            + "      </ram:SpecifiedTradeSettlementHeaderMonetarySummation>\n"
            + "    </ram:ApplicableHeaderTradeSettlement>\n"
            + "  </rsm:SupplyChainTradeTransaction>\n"
            + "</rsm:CrossIndustryInvoice>\n";

    @Test
    void layoutHasRevenueListOnLeftAndDetailsOnRight() {
        ModernRevenuePanel panel = new ModernRevenuePanel();
        JSplitPane split = panel.getSplitPane();
        assertNotNull(split);
        assertEquals(JSplitPane.HORIZONTAL_SPLIT, split.getOrientation(),
                "ModernRevenuePanel must use a horizontal split (left/right)");
        assertSame(panel.getListPanel(), split.getLeftComponent(),
                "The list of revenues must be on the left");
        assertSame(panel.getDetailPanel(), split.getRightComponent(),
                "The revenue details must be on the right");
    }

    @Test
    void filtersRevenuesByContentDateCurrentYearAndCurrentMonth() throws Exception {
        ModernRevenuePanel panel = new ModernRevenuePanel();
        JTable table = panel.getRevenueTable();

        Calendar nowCal = Calendar.getInstance();
        int currentYear = nowCal.get(Calendar.YEAR);
        int currentMonth = nowCal.get(Calendar.MONTH);

        // 1. Revenue in current month & current year
        Calendar thisMonthCal = Calendar.getInstance();
        thisMonthCal.set(currentYear, currentMonth, 1, 10, 0, 0);
        Date thisMonthDate = thisMonthCal.getTime();

        // 2. Revenue in another month of the current year
        int otherMonth = (currentMonth == 0) ? 1 : 0;
        Calendar otherMonthThisYearCal = Calendar.getInstance();
        otherMonthThisYearCal.set(currentYear, otherMonth, 15, 10, 0, 0);
        Date otherMonthThisYearDate = otherMonthThisYearCal.getTime();

        // 3. Revenue in previous year
        Calendar lastYearCal = Calendar.getInstance();
        lastYearCal.set(currentYear - 1, 5, 20, 10, 0, 0);
        Date lastYearDate = lastYearCal.getTime();

        Revenue r1 = createRevenue("REV-101", "Kundenprojekt Consulting", new BigDecimal("595.00"), thisMonthDate);
        Revenue r2 = createRevenue("REV-202", "Lizenzgebuehren", new BigDecimal("1190.00"), otherMonthThisYearDate);
        Revenue r3 = createRevenue("REV-303", "Alte Beratung 2025", new BigDecimal("350.00"), lastYearDate);

        Object[][] rows = new Object[][]{
            r1.toArray(),
            r2.toArray(),
            r3.toArray()
        };
        MPTableModel model = new MPTableModel(rows, Headers.REVENUE);
        model.setTypes(Revenue.class, String.class, Account.class, String.class, String.class, Boolean.class, Date.class, Date.class);
        table.setModel(model);
        panel.applyRevenueFilter();

        // Initially all 3 revenues are visible
        assertEquals(3, table.getRowCount());

        // Filter by content
        panel.filterByContent("Consulting");
        assertEquals(1, table.getRowCount());
        assertSame(r1, table.getValueAt(0, 0));

        panel.filterByContent("REV-303");
        assertEquals(1, table.getRowCount());
        assertSame(r3, table.getValueAt(0, 0));

        // Clear filter -> all 3 visible again
        panel.clearFilter();
        assertEquals(3, table.getRowCount());

        // Filter by current year -> r1 and r2 visible, r3 (last year) hidden
        panel.filterCurrentYear();
        assertEquals(2, table.getRowCount());

        // Filter by current month -> only r1 visible
        panel.filterCurrentMonth();
        assertEquals(1, table.getRowCount());
        assertSame(r1, table.getValueAt(0, 0));

        // Filter by custom date range (previous year) -> only r3 visible
        Calendar rangeStart = Calendar.getInstance();
        rangeStart.set(currentYear - 1, 0, 1, 0, 0, 0);
        Calendar rangeEnd = Calendar.getInstance();
        rangeEnd.set(currentYear - 1, 11, 31, 23, 59, 59);
        panel.filterByDateRange(rangeStart.getTime(), rangeEnd.getTime());
        assertEquals(1, table.getRowCount());
        assertSame(r3, table.getValueAt(0, 0));
    }

    @Test
    void importingFileChecksForZugferdAndPopulatesRevenueData() throws Exception {
        ModernRevenuePanel panel = new ModernRevenuePanel();
        File xmlFile = tempDir.resolve("zugferd-invoice.xml").toFile();
        Files.write(xmlFile.toPath(), SAMPLE_CII_XML.getBytes(StandardCharsets.UTF_8));

        panel.importReceiptFile(xmlFile);

        assertEquals("REV-2026-789", panel.cnumber_);
        assertEquals(new BigDecimal("595.00"), panel.brutvalue_);
        assertEquals(new BigDecimal("500.00"), panel.netvalue_);
        assertEquals(new BigDecimal("19.00"), panel.taxpercentvalue_);
        assertNotNull(panel.description_);
        assertTrue(panel.description_.contains("Agentur Sued GmbH"));
        assertTrue(panel.description_.contains("Beratungsleistung Consulting"));
        assertEquals("2026-04-15", new SimpleDateFormat("yyyy-MM-dd").format(panel.dateadded_));
        assertEquals("2026-04-30", new SimpleDateFormat("yyyy-MM-dd").format(panel.dateend_));

        Revenue owner = (Revenue) panel.getDataOwner();
        assertNotNull(owner);
        assertEquals("REV-2026-789", owner.__getCnumber());
        assertEquals(new BigDecimal("595.00"), owner.__getBrutvalue());
    }

    @Test
    void labelsHaveProperGermanUmlautsAndNoGobdMention() throws Exception {
        ModernRevenuePanel panel = new ModernRevenuePanel();
        List<String> texts = new ArrayList<String>();
        collectComponentTexts(panel, texts);

        for (String t : texts) {
            assertFalse(t.toLowerCase().contains("gobd"),
                    "UI label should not mention GoBD: " + t);
            assertFalse(t.contains("hinzufuegen"), "Broken umlaut in UI text: " + t);
            assertFalse(t.contains("Oeffnen"), "Broken umlaut in UI text: " + t);
            assertFalse(t.contains("Pruefen"), "Broken umlaut in UI text: " + t);
            assertFalse(t.contains("Groesse"), "Broken umlaut in UI text: " + t);
            assertFalse(t.contains("koennen"), "Broken umlaut in UI text: " + t);
            assertFalse(t.contains("unveraenderbar"), "Broken umlaut in UI text: " + t);
        }

        assertTrue(texts.stream().anyMatch(t -> t.contains("Eingangsbelege & Abrechnungen")),
                "Receipts card title should be 'Eingangsbelege & Abrechnungen'");
        assertTrue(texts.stream().anyMatch(t -> t.contains("Beleg verkn\u00fcpfen")),
                "Link receipt button should use proper umlaut 'ü'");
        assertTrue(texts.stream().anyMatch(t -> t.contains("\u00d6ffnen")),
                "Open receipt button should use proper umlaut 'Ö'");
        assertTrue(texts.stream().anyMatch(t -> t.contains("Pr\u00fcfen")),
                "Verify receipt button should use proper umlaut 'ü'");

        // Also check source file directly for any GoBD or ASCII umlaut remnants
        File srcFile = new File("src/main/java/mpv5/ui/panels/ModernRevenuePanel.java");
        if (srcFile.isFile()) {
            String src = new String(Files.readAllBytes(srcFile.toPath()), StandardCharsets.UTF_8);
            assertFalse(src.toLowerCase().contains("gobd"),
                    "ModernRevenuePanel.java should not contain GoBD references");
            assertFalse(src.contains("Neue Einnahme"),
                    "ModernRevenuePanel.java should not contain the 'Neue Einnahme' header");
            assertFalse(src.contains("TEMPLATES_PREVIEW_HINT"),
                    "ModernRevenuePanel.java should not contain the template hint");
            assertFalse(src.contains("anhaengen"));
            assertFalse(src.contains("auswaehlen"));
            assertFalse(src.contains("Verknuepfung"));
            assertFalse(src.contains("geloescht"));
            assertFalse(src.contains("loeschen"));
            assertFalse(src.contains("ueberein"));
            assertFalse(src.contains("Integritaet"));
        }
    }

    @Test
    void hasNoHeaderAndIncludesSaveAsNewButtonAndFourDetailCardsWithPdfPreview() throws Exception {
        ModernRevenuePanel panel = new ModernRevenuePanel();
        List<String> texts = new ArrayList<String>();
        collectComponentTexts(panel, texts);

        assertFalse(texts.stream().anyMatch(t -> t.contains("Neue Einnahme")),
                "Header 'Neue Einnahme' must be removed");
        assertFalse(texts.stream().anyMatch(t -> t.contains("20 Beispielpositionen")),
                "Template preview hint must be removed from preview card");

        assertNotNull(panel.getCommandBar());
        assertNotNull(panel.getCommandBar().getSaveAsNewButton());
        assertTrue(panel.getCommandBar().getSaveAsNewButton().isVisible(),
                "'Save as new' button must be visible in the command bar");
        assertEquals(mpv5.globals.Messages.ACTION_SAVE_AS_NEW.toString(),
                panel.getCommandBar().getSaveAsNewButton().getText());

        // Import e-invoice button is a large prominent green PrimaryButton
        assertNotNull(panel.getImportZugferdButton());
        assertTrue(panel.getImportZugferdButton() instanceof mpv5.ui.modern.PrimaryButton);

        // Right detail grid has 4 cards including the PDF preview card
        assertNotNull(panel.getDetailGrid());
        assertEquals(4, panel.getDetailGrid().getComponentCount(),
                "Right detail grid must contain 4 cards");
        Component fourth = panel.getDetailGrid().getComponent(3);
        assertTrue(fourth instanceof mpv5.ui.modern.Card);
        assertEquals(mpv5.globals.Messages.TEMPLATES_PREVIEW.toString(),
                ((mpv5.ui.modern.Card) fourth).getTitle());

        // Render a PDF in the preview card
        File pdfFile = tempDir.resolve("sample-revenue.pdf").toFile();
        try (org.apache.pdfbox.pdmodel.PDDocument doc = new org.apache.pdfbox.pdmodel.PDDocument()) {
            doc.addPage(new org.apache.pdfbox.pdmodel.PDPage());
            doc.save(pdfFile);
        }
        panel.refreshPreview(pdfFile);
        assertEquals(pdfFile, panel.getPreviewPdf());
    }

    private static Revenue createRevenue(String number, String desc, BigDecimal gross, Date date) {
        Revenue r = new Revenue();
        r.setCnumber(number);
        r.setCname(number);
        r.setDescription(desc);
        r.setBrutvalue(gross);
        r.setNetvalue(gross);
        r.setTaxpercentvalue(BigDecimal.ZERO);
        r.setDateadded(date);
        r.setDateend(date);
        return r;
    }

    private static void collectComponentTexts(Component comp, List<String> out) {
        if (comp instanceof JLabel) {
            String t = ((JLabel) comp).getText();
            if (t != null && !t.isEmpty()) {
                out.add(t);
            }
        } else if (comp instanceof AbstractButton) {
            String t = ((AbstractButton) comp).getText();
            if (t != null && !t.isEmpty()) {
                out.add(t);
            }
        } else if (comp instanceof JTable) {
            JTable table = (JTable) comp;
            for (int i = 0; i < table.getColumnCount(); i++) {
                Object hv = table.getColumnModel().getColumn(i).getHeaderValue();
                if (hv != null) {
                    out.add(String.valueOf(hv));
                }
            }
        }
        if (comp instanceof Container) {
            for (Component child : ((Container) comp).getComponents()) {
                collectComponentTexts(child, out);
            }
        }
    }
}
