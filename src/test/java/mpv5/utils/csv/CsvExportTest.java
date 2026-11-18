package mpv5.utils.csv;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import mpv5.db.objects.Contact;
import mpv5.db.objects.Product;
import mpv5.utils.csv.CsvImport.Field;
import mpv5.utils.csv.CsvImport.Kind;
import org.junit.jupiter.api.Test;

class CsvExportTest {

    @Test
    void contactsExportRoundtripGerman() {
        List<Contact> contacts = new ArrayList<>();
        Contact c = new Contact();
        c.setCname("Mustermann");
        c.setPrename("Max");
        c.setTitle("Herr");
        c.setCompany("Musterfirma GmbH");
        c.setDepartment("IT");
        c.setStreet("Musterstr. 1");
        c.setZip("12345");
        c.setCity("Musterstadt");
        c.setCountry("Deutschland");
        c.setMainphone("012345678");
        c.setWorkphone("012345679");
        c.setMobilephone("0170123456");
        c.setFax("012345670");
        c.setMailaddress("max@musterfirma.de");
        c.setWebsite("https://musterfirma.de");
        c.setCNumber("K-1001");
        c.settaxnumber("DE123456789");
        c.setBankname("Musterbank");
        c.setBankid("MUBADE11XXX");
        c.setBankaccount("DE89370400440532013000");
        c.setNotes("Wichtiger Kunde; Kontakt mit Sorgfalt pflegen.");
        c.setDateadded(new Date());
        contacts.add(c);

        String csv = CsvExport.toContactsCsv(contacts, true);
        assertNotNull(csv);
        assertTrue(csv.contains("Mustermann"));
        assertTrue(csv.contains("Musterfirma GmbH"));

        CsvImport.Data data = CsvImport.read(csv.getBytes(StandardCharsets.UTF_8));
        assertEquals(2, data.rows.size(), "header + 1 contact row");

        Field[] mapping = CsvImport.guess(data.rows.get(0), Kind.CUSTOMERS);
        assertEquals(CsvExport.CONTACT_HEADERS_DE.length, mapping.length);
        for (int i = 0; i < mapping.length; i++) {
            assertNotNull(mapping[i], "Column " + i + " (" + data.rows.get(0)[i] + ") should be recognized");
        }

        // Verify keys matched
        assertEquals("cname", mapping[0].key);
        assertEquals("prename", mapping[1].key);
        assertEquals("title", mapping[2].key);
        assertEquals("company", mapping[3].key);
        assertEquals("department", mapping[4].key);
        assertEquals("street", mapping[5].key);
        assertEquals("zip", mapping[6].key);
        assertEquals("city", mapping[7].key);
        assertEquals("country", mapping[8].key);
        assertEquals("mainphone", mapping[9].key);
        assertEquals("workphone", mapping[10].key);
        assertEquals("mobilephone", mapping[11].key);
        assertEquals("fax", mapping[12].key);
        assertEquals("mailaddress", mapping[13].key);
        assertEquals("website", mapping[14].key);
        assertEquals("cnumber", mapping[15].key);
        assertEquals("taxnumber", mapping[16].key);
        assertEquals("bankname", mapping[17].key);
        assertEquals("bankid", mapping[18].key);
        assertEquals("bankaccount", mapping[19].key);
        assertEquals("notes", mapping[20].key);
        assertEquals("dateadded", mapping[21].key);
        assertEquals("GROUP", mapping[22].key);

        List<String> problems = CsvImport.check(data.rows.get(1), mapping, Kind.CUSTOMERS, null);
        assertTrue(problems.isEmpty(), "Row should have no validation problems: " + problems);
    }

    @Test
    void contactsExportRoundtripEnglish() {
        List<Contact> contacts = new ArrayList<>();
        Contact c = new Contact();
        c.setCname("Doe");
        c.setPrename("John");
        c.setCompany("Acme Corp");
        c.setMailaddress("john.doe@acme.com");
        c.setCNumber("C-2002");
        c.setDateadded(new Date());
        contacts.add(c);

        String csv = CsvExport.toContactsCsv(contacts, false);
        CsvImport.Data data = CsvImport.read(csv.getBytes(StandardCharsets.UTF_8));
        assertEquals(2, data.rows.size());

        Field[] mapping = CsvImport.guess(data.rows.get(0), Kind.CUSTOMERS);
        for (int i = 0; i < mapping.length; i++) {
            assertNotNull(mapping[i], "Column " + i + " (" + data.rows.get(0)[i] + ") should be recognized");
        }

        List<String> problems = CsvImport.check(data.rows.get(1), mapping, Kind.CUSTOMERS, null);
        assertTrue(problems.isEmpty(), "English exported row should have no validation problems: " + problems);
    }

    @Test
    void productsExportRoundtripGerman() {
        List<Product> products = new ArrayList<>();
        Product p = new Product();
        p.setCname("Premium Kaffeebohnen 1kg");
        p.setCnumber("P-1001");
        p.setDescription("Aromatische Arabica Bohnen; Fair Trade.");
        p.setExternalnetvalue(new BigDecimal("19.99"));
        p.setInternalnetvalue(new BigDecimal("12.50"));
        p.setMeasure("kg");
        p.setEan("4012345678901");
        p.setReference("REF-KAFFEE-01");
        p.setUrl("https://shop.example.com/kaffee");
        p.setStockvalue(new BigDecimal("150"));
        p.setThresholdvalue(new BigDecimal("20"));
        p.setDateadded(new Date());
        products.add(p);

        String csv = CsvExport.toProductsCsv(products, true);
        assertNotNull(csv);
        assertTrue(csv.contains("Premium Kaffeebohnen"));
        assertTrue(csv.contains("19,99") || csv.contains("19.99"));

        CsvImport.Data data = CsvImport.read(csv.getBytes(StandardCharsets.UTF_8));
        assertEquals(2, data.rows.size());

        Field[] mapping = CsvImport.guess(data.rows.get(0), Kind.PRODUCTS);
        assertEquals(CsvExport.PRODUCT_HEADERS_DE.length, mapping.length);
        for (int i = 0; i < mapping.length; i++) {
            assertNotNull(mapping[i], "Column " + i + " (" + data.rows.get(0)[i] + ") should be recognized");
        }

        assertEquals("cname", mapping[0].key);
        assertEquals("cnumber", mapping[1].key);
        assertEquals("description", mapping[2].key);
        assertEquals("externalnetvalue", mapping[3].key);
        assertEquals("internalnetvalue", mapping[4].key);
        assertEquals("TAX", mapping[5].key);
        assertEquals("measure", mapping[6].key);
        assertEquals("ean", mapping[7].key);
        assertEquals("reference", mapping[8].key);
        assertEquals("url", mapping[9].key);
        assertEquals("stockvalue", mapping[10].key);
        assertEquals("thresholdvalue", mapping[11].key);
        assertEquals("SUPPLIER", mapping[12].key);
        assertEquals("MANUFACTURER", mapping[13].key);
        assertEquals("dateadded", mapping[14].key);
        assertEquals("GROUP", mapping[15].key);

        List<String> problems = CsvImport.check(data.rows.get(1), mapping, Kind.PRODUCTS, null);
        assertTrue(problems.isEmpty(), "Product row should have no validation problems: " + problems);
    }

    @Test
    void productsExportRoundtripEnglish() {
        List<Product> products = new ArrayList<>();
        Product p = new Product();
        p.setCname("Mechanical Keyboard");
        p.setCnumber("SKU-9900");
        p.setExternalnetvalue(new BigDecimal("99.99"));
        p.setInternalnetvalue(new BigDecimal("50.00"));
        p.setDateadded(new Date());
        products.add(p);

        String csv = CsvExport.toProductsCsv(products, false);
        CsvImport.Data data = CsvImport.read(csv.getBytes(StandardCharsets.UTF_8));
        assertEquals(2, data.rows.size());

        Field[] mapping = CsvImport.guess(data.rows.get(0), Kind.PRODUCTS);
        for (int i = 0; i < mapping.length; i++) {
            assertNotNull(mapping[i], "Column " + i + " (" + data.rows.get(0)[i] + ") should be recognized");
        }

        List<String> problems = CsvImport.check(data.rows.get(1), mapping, Kind.PRODUCTS, null);
        assertTrue(problems.isEmpty(), "English exported product row should have no validation problems: " + problems);
    }

    @Test
    void escapesSpecialCharactersProperly() {
        assertEquals("hello", CsvExport.escape("hello"));
        assertEquals("\"hello;world\"", CsvExport.escape("hello;world"));
        assertEquals("\"hello \"\"quoted\"\" world\"", CsvExport.escape("hello \"quoted\" world"));
        assertEquals("\"line1\nline2\"", CsvExport.escape("line1\nline2"));

        List<String[]> parsed = CsvImport.parse("foo;" + CsvExport.escape("val;with;semi\nand\"quotes\"") + ";bar\r\n", ';', 0);
        assertEquals(1, parsed.size());
        assertEquals(3, parsed.get(0).length);
        assertEquals("foo", parsed.get(0)[0]);
        assertEquals("val;with;semi\nand\"quotes\"", parsed.get(0)[1]);
        assertEquals("bar", parsed.get(0)[2]);
    }
}
