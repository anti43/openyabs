package mpv5.utils.csv;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.List;
import mpv5.utils.csv.CsvImport.Field;
import mpv5.utils.csv.CsvImport.Kind;
import org.junit.jupiter.api.Test;

class CsvImportTest {

    @Test
    void detectsEncoding() {
        assertEquals(StandardCharsets.UTF_8, CsvImport.detectCharset("Straße;Köln".getBytes(StandardCharsets.UTF_8)));
        assertEquals(Charset.forName("windows-1252"), CsvImport.detectCharset("Straße;Köln".getBytes(Charset.forName("windows-1252"))));
        byte[] bom = "﻿Name;Ort".getBytes(StandardCharsets.UTF_8);
        CsvImport.Data d = CsvImport.read(bom);
        assertEquals("Name", d.rows.get(0)[0], "no byte order mark in the first name");
    }

    @Test
    void detectsSeparator() {
        assertEquals(';', CsvImport.detectSeparator("Name;Ort;Preis\nA;B;1,50\nC;D;2,00\n"));
        assertEquals(',', CsvImport.detectSeparator("Name,City,Price\nA,B,1.50\n\"C, Inc.\",D,2.00\n"));
        assertEquals('\t', CsvImport.detectSeparator("Name\tOrt\nA, B\tC\n"));
        assertEquals('|', CsvImport.detectSeparator("a|b|c\n1|2|3\n"));
    }

    @Test
    void parsesQuotes() {
        List<String[]> rows = CsvImport.parse("a;\"b;c\";\"d \"\"x\"\"\"\n\"multi\nline\";2\r\n;\n", ';', 0);
        assertArrayEquals(new String[]{"a", "b;c", "d \"x\""}, rows.get(0));
        assertArrayEquals(new String[]{"multi\nline", "2"}, rows.get(1));
        assertArrayEquals(new String[]{"", ""}, rows.get(2));
        assertEquals(3, rows.size());
        CsvImport.Data d = CsvImport.read("Name;Ort\r\nA;B\r\n\r\n".getBytes(StandardCharsets.UTF_8));
        assertEquals(2, d.rows.size(), "trailing empty lines are no rows");
    }

    @Test
    void readsNumbersAndDates() throws Exception {
        assertEquals(0, new BigDecimal("1234.56").compareTo(CsvImport.number("1.234,56")));
        assertEquals(0, new BigDecimal("1234.56").compareTo(CsvImport.number("1,234.56")));
        assertEquals(0, new BigDecimal("9.9").compareTo(CsvImport.number("9,90 €")));
        assertEquals(0, new BigDecimal("19").compareTo(CsvImport.number("19%")));
        assertEquals(0, new BigDecimal("1234567").compareTo(CsvImport.number("1.234.567")));
        assertThrows(NumberFormatException.class, () -> CsvImport.number("zwölf"));
        SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd");
        assertEquals("2025-12-31", f.format(CsvImport.date("31.12.2025")));
        assertEquals("2025-12-31", f.format(CsvImport.date("2025-12-31")));
        assertEquals("2025-03-01", f.format(CsvImport.date("1.3.2025")));
        assertThrows(Exception.class, () -> CsvImport.date("31.13.2025"));
    }

    @Test
    void guessesColumns() {
        Field[] m = CsvImport.guess(new String[]{"Kundennummer", "Firma", "Vorname", "Nachname", "Straße", "PLZ", "Ort", "E-Mail", "Telefon", "Mobil", "Unbekannt"}, Kind.CUSTOMERS);
        String[] keys = new String[m.length];
        for (int i = 0; i < m.length; i++) {
            keys[i] = m[i] == null ? null : m[i].key;
        }
        assertArrayEquals(new String[]{"cnumber", "company", "prename", "cname", "street", "zip", "city", "mailaddress", "mainphone", "mobilephone", null}, keys);
        Field[] e = CsvImport.guess(new String[]{"First Name", "Last Name", "Company", "Address", "Postal Code", "City", "Email Address"}, Kind.CUSTOMERS);
        assertEquals("prename", e[0].key);
        assertEquals("cname", e[1].key);
        assertEquals("company", e[2].key);
        assertEquals("street", e[3].key);
        assertEquals("zip", e[4].key);
        assertEquals("city", e[5].key);
        assertEquals("mailaddress", e[6].key);
        Field[] p = CsvImport.guess(new String[]{"Art.-Nr.", "Bezeichnung", "VK netto", "EK", "MwSt", "Einheit", "EAN", "Lieferant", "Warengruppe"}, Kind.PRODUCTS);
        assertEquals("cnumber", p[0].key);
        assertEquals("cname", p[1].key);
        assertEquals("externalnetvalue", p[2].key);
        assertEquals("internalnetvalue", p[3].key);
        assertEquals("TAX", p[4].key);
        assertEquals("measure", p[5].key);
        assertEquals("ean", p[6].key);
        assertEquals("SUPPLIER", p[7].key);
        assertEquals("GROUP", p[8].key);
        //a field once only
        Field[] twice = CsvImport.guess(new String[]{"Name", "Name"}, Kind.CUSTOMERS);
        assertNotNull(twice[0]);
        assertNull(twice[1]);
    }

    @Test
    void checksRows() {
        Field[] m = CsvImport.guess(new String[]{"Name", "Firma", "E-Mail", "Angelegt am"}, Kind.CUSTOMERS);
        assertTrue(CsvImport.check(new String[]{"Muster", "", "a@b.de", "01.02.2024"}, m, Kind.CUSTOMERS).isEmpty());
        assertTrue(CsvImport.check(new String[]{"", "Firma AG", "", ""}, m, Kind.CUSTOMERS).isEmpty(), "a company is enough");
        assertEquals(1, CsvImport.check(new String[]{"", "", "a@b.de", ""}, m, Kind.CUSTOMERS).size(), "no name");
        assertEquals(1, CsvImport.check(new String[]{"X", "", "kein-mail", ""}, m, Kind.CUSTOMERS).size(), "mail");
        assertEquals(1, CsvImport.check(new String[]{"X", "", "", "gestern"}, m, Kind.CUSTOMERS).size(), "date");
        Field[] p = CsvImport.guess(new String[]{"Bezeichnung", "Preis", "MwSt"}, Kind.PRODUCTS);
        assertTrue(CsvImport.check(new String[]{"Kaffee", "2,50", "7"}, p, Kind.PRODUCTS).isEmpty());
        assertEquals(1, CsvImport.check(new String[]{"Kaffee", "zwei", "7"}, p, Kind.PRODUCTS).size());
        assertEquals(1, CsvImport.check(new String[]{"Kaffee", "2", "150"}, p, Kind.PRODUCTS).size(), "tax rate over 100");
        assertNotNull(CsvImport.checkMapping(new Field[]{null, p[1]}, Kind.PRODUCTS), "no name column");
        assertNull(CsvImport.checkMapping(p, Kind.PRODUCTS));
        assertEquals("2,50", CsvImport.display("2.5", p[1]).replace('.', ','));
    }
}
