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
import mpv5.db.objects.Expense;
import mpv5.globals.Headers;
import mpv5.utils.models.MPTableModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.*;

class ModernExpensePanelTest {

    @TempDir
    Path tempDir;

    private static final String SAMPLE_CII_XML =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<rsm:CrossIndustryInvoice xmlns:rsm=\"urn:un:unece:uncefact:data:standard:CrossIndustryInvoice:100\"\n"
            + "    xmlns:ram=\"urn:un:unece:uncefact:data:standard:ReusableAggregateBusinessInformationEntity:100\"\n"
            + "    xmlns:udt=\"urn:un:unece:uncefact:data:standard:UnqualifiedDataType:100\">\n"
            + "  <rsm:ExchangedDocument>\n"
            + "    <ram:ID>RE-2026-555</ram:ID>\n"
            + "    <ram:IssueDateTime>\n"
            + "      <udt:DateTimeString format=\"102\">20260410</udt:DateTimeString>\n"
            + "    </ram:IssueDateTime>\n"
            + "  </rsm:ExchangedDocument>\n"
            + "  <rsm:SupplyChainTradeTransaction>\n"
            + "    <ram:IncludedSupplyChainTradeLineItem>\n"
            + "      <ram:SpecifiedTradeProduct>\n"
            + "        <ram:Name>Cloud Server Miete</ram:Name>\n"
            + "      </ram:SpecifiedTradeProduct>\n"
            + "    </ram:IncludedSupplyChainTradeLineItem>\n"
            + "    <ram:ApplicableHeaderTradeAgreement>\n"
            + "      <ram:SellerTradeParty>\n"
            + "        <ram:Name>Rechenzentrum Nord GmbH</ram:Name>\n"
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
            + "          <udt:DateTimeString format=\"102\">20260425</udt:DateTimeString>\n"
            + "        </ram:DueDateDateTime>\n"
            + "      </ram:SpecifiedTradePaymentTerms>\n"
            + "      <ram:SpecifiedTradeSettlementHeaderMonetarySummation>\n"
            + "        <ram:TaxBasisTotalAmount>200.00</ram:TaxBasisTotalAmount>\n"
            + "        <ram:TaxTotalAmount currencyID=\"EUR\">38.00</ram:TaxTotalAmount>\n"
            + "        <ram:GrandTotalAmount>238.00</ram:GrandTotalAmount>\n"
            + "      </ram:SpecifiedTradeSettlementHeaderMonetarySummation>\n"
            + "    </ram:ApplicableHeaderTradeSettlement>\n"
            + "  </rsm:SupplyChainTradeTransaction>\n"
            + "</rsm:CrossIndustryInvoice>\n";

    @Test
    void layoutHasExpenseListOnLeftAndDetailsOnRight() {
        ModernExpensePanel panel = new ModernExpensePanel();
        JSplitPane split = panel.getSplitPane();
        assertNotNull(split);
        assertEquals(JSplitPane.HORIZONTAL_SPLIT, split.getOrientation(),
                "ModernExpensePanel must use a horizontal split (left/right)");
        assertSame(panel.getListPanel(), split.getLeftComponent(),
                "The list of expenses must be on the left");
        assertSame(panel.getDetailPanel(), split.getRightComponent(),
                "The expense details must be on the right");
    }

    @Test
    void filtersExpensesByContentDateCurrentYearAndCurrentMonth() throws Exception {
        ModernExpensePanel panel = new ModernExpensePanel();
        JTable table = panel.getExpenseTable();

        Calendar nowCal = Calendar.getInstance();
        int currentYear = nowCal.get(Calendar.YEAR);
        int currentMonth = nowCal.get(Calendar.MONTH);

        // 1. Expense in current month & current year
        Calendar thisMonthCal = Calendar.getInstance();
        thisMonthCal.set(currentYear, currentMonth, 1, 10, 0, 0);
        Date thisMonthDate = thisMonthCal.getTime();

        // 2. Expense in another month of the current year
        int otherMonth = (currentMonth == 0) ? 1 : 0;
        Calendar otherMonthThisYearCal = Calendar.getInstance();
        otherMonthThisYearCal.set(currentYear, otherMonth, 15, 10, 0, 0);
        Date otherMonthThisYearDate = otherMonthThisYearCal.getTime();

        // 3. Expense in previous year
        Calendar lastYearCal = Calendar.getInstance();
        lastYearCal.set(currentYear - 1, 5, 20, 10, 0, 0);
        Date lastYearDate = lastYearCal.getTime();

        Expense e1 = createExpense("INV-101", "Bueromaterial Druckerpapier", new BigDecimal("59.50"), thisMonthDate);
        Expense e2 = createExpense("INV-202", "Hosting Jahresgebuehr", new BigDecimal("119.00"), otherMonthThisYearDate);
        Expense e3 = createExpense("INV-303", "Alte Reparatur", new BigDecimal("250.00"), lastYearDate);

        Object[][] rows = new Object[][]{
            e1.toArray(),
            e2.toArray(),
            e3.toArray()
        };
        MPTableModel model = new MPTableModel(rows, Headers.EXPENSE);
        model.setTypes(Expense.class, String.class, Account.class, String.class, String.class, Boolean.class, Date.class, Date.class);
        table.setModel(model);
        panel.applyExpenseFilter();

        // Initially all 3 expenses are visible
        assertEquals(3, table.getRowCount());

        // Filter by content
        panel.filterByContent("Druckerpapier");
        assertEquals(1, table.getRowCount());
        assertSame(e1, table.getValueAt(0, 0));

        panel.filterByContent("INV-303");
        assertEquals(1, table.getRowCount());
        assertSame(e3, table.getValueAt(0, 0));

        // Clear filter -> all 3 visible again
        panel.clearFilter();
        assertEquals(3, table.getRowCount());

        // Filter by current year -> e1 and e2 visible, e3 (last year) hidden
        panel.filterCurrentYear();
        assertEquals(2, table.getRowCount());

        // Filter by current month -> only e1 visible
        panel.filterCurrentMonth();
        assertEquals(1, table.getRowCount());
        assertSame(e1, table.getValueAt(0, 0));

        // Filter by custom date range (previous year) -> only e3 visible
        Calendar rangeStart = Calendar.getInstance();
        rangeStart.set(currentYear - 1, 0, 1, 0, 0, 0);
        Calendar rangeEnd = Calendar.getInstance();
        rangeEnd.set(currentYear - 1, 11, 31, 23, 59, 59);
        panel.filterByDateRange(rangeStart.getTime(), rangeEnd.getTime());
        assertEquals(1, table.getRowCount());
        assertSame(e3, table.getValueAt(0, 0));
    }

    @Test
    void importingFileChecksForZugferdAndPopulatesExpenseData() throws Exception {
        ModernExpensePanel panel = new ModernExpensePanel();
        File xmlFile = tempDir.resolve("zugferd-invoice.xml").toFile();
        Files.write(xmlFile.toPath(), SAMPLE_CII_XML.getBytes(StandardCharsets.UTF_8));

        panel.importReceiptFile(xmlFile);

        assertEquals("RE-2026-555", panel.cnumber_);
        assertEquals(new BigDecimal("238.00"), panel.brutvalue_);
        assertEquals(new BigDecimal("200.00"), panel.netvalue_);
        assertEquals(new BigDecimal("19.00"), panel.taxpercentvalue_);
        assertNotNull(panel.description_);
        assertTrue(panel.description_.contains("Rechenzentrum Nord GmbH"));
        assertTrue(panel.description_.contains("Cloud Server Miete"));
        assertEquals("2026-04-10", new SimpleDateFormat("yyyy-MM-dd").format(panel.dateadded_));
        assertEquals("2026-04-25", new SimpleDateFormat("yyyy-MM-dd").format(panel.dateend_));

        Expense owner = (Expense) panel.getDataOwner();
        assertNotNull(owner);
        assertEquals("RE-2026-555", owner.__getCnumber());
        assertEquals(new BigDecimal("238.00"), owner.__getBrutvalue());
    }

    @Test
    void labelsHaveProperGermanUmlautsAndNoGobdMention() throws Exception {
        ModernExpensePanel panel = new ModernExpensePanel();
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

        assertTrue(texts.stream().anyMatch(t -> t.contains("Eingangsbelege & Scans")),
                "Receipts card title should be 'Eingangsbelege & Scans'");
        assertTrue(texts.stream().anyMatch(t -> t.contains("Beleg hinzuf\u00fcgen")),
                "Add receipt button should use proper umlaut 'ü'");
        assertTrue(texts.stream().anyMatch(t -> t.contains("\u00d6ffnen")),
                "Open receipt button should use proper umlaut 'Ö'");
        assertTrue(texts.stream().anyMatch(t -> t.contains("Pr\u00fcfen")),
                "Verify receipt button should use proper umlaut 'ü'");

        // Also check source file directly for any GoBD or ASCII umlaut remnants
        File srcFile = new File("src/main/java/mpv5/ui/panels/ModernExpensePanel.java");
        if (srcFile.isFile()) {
            String src = new String(Files.readAllBytes(srcFile.toPath()), StandardCharsets.UTF_8);
            assertFalse(src.toLowerCase().contains("gobd"),
                    "ModernExpensePanel.java should not contain GoBD references");
            assertFalse(src.contains("Neue Ausgabe"),
                    "ModernExpensePanel.java should not contain the 'Neue Ausgabe' header");
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
        ModernExpensePanel panel = new ModernExpensePanel();
        List<String> texts = new ArrayList<String>();
        collectComponentTexts(panel, texts);

        assertFalse(texts.stream().anyMatch(t -> t.contains("Neue Ausgabe")),
                "Header 'Neue Ausgabe' must be removed");

        assertNotNull(panel.getCommandBar());
        assertNotNull(panel.getCommandBar().getSaveAsNewButton());
        assertTrue(panel.getCommandBar().getSaveAsNewButton().isVisible(),
                "'Save as new' button must be visible in the command bar");
        assertEquals(mpv5.globals.Messages.ACTION_SAVE_AS_NEW.toString(),
                panel.getCommandBar().getSaveAsNewButton().getText());

        // Right detail grid has 4 cards including the PDF preview card
        assertNotNull(panel.getDetailGrid());
        assertEquals(4, panel.getDetailGrid().getComponentCount(),
                "Right detail grid must contain 4 cards");
        Component fourth = panel.getDetailGrid().getComponent(3);
        assertTrue(fourth instanceof mpv5.ui.modern.Card);
        assertEquals(mpv5.globals.Messages.TEMPLATES_PREVIEW.toString(),
                ((mpv5.ui.modern.Card) fourth).getTitle());

        // Render a PDF in the preview card
        File pdfFile = tempDir.resolve("sample-receipt.pdf").toFile();
        try (org.apache.pdfbox.pdmodel.PDDocument doc = new org.apache.pdfbox.pdmodel.PDDocument()) {
            doc.addPage(new org.apache.pdfbox.pdmodel.PDPage());
            doc.save(pdfFile);
        }
        panel.refreshPreview(pdfFile);
        assertEquals(pdfFile, panel.getPreviewPdf());
    }

    private static Expense createExpense(String number, String desc, BigDecimal gross, Date date) {
        Expense e = new Expense();
        e.setCnumber(number);
        e.setCname(number);
        e.setDescription(desc);
        e.setBrutvalue(gross);
        e.setNetvalue(gross);
        e.setTaxpercentvalue(BigDecimal.ZERO);
        e.setDateadded(date);
        e.setDateend(date);
        return e;
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
