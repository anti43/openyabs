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

import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.openyabs.handwerk.DatanormImporter.Article;
import org.openyabs.handwerk.DatanormImporter.ParseResult;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Extensive unit tests for {@link DatanormImporter} (DATANORM 4.0 &amp; 5.0)
 * and {@link MeasurementEvaluator} (site measurement / Aufmaß arithmetic).
 */
class HandwerkDatanormAndMeasurementTest {

    // --------------------------------------------------- DATANORM 4.0 & 5.0

    @Test
    void datanormPriceDivisors0Through3AndDecimalFallback() {
        // 0 = per 1: 12345 cents -> 123.45
        assertEquals(new BigDecimal("123.45"), DatanormImporter.decodeDatanormPrice("12345", 0));
        // 1 = per 10: 12345 cents -> 123.45 / 10 = 12.35
        assertEquals(new BigDecimal("12.35"), DatanormImporter.decodeDatanormPrice("12345", 1));
        // 2 = per 100: 125000 cents -> 1250.00 / 100 = 12.50
        assertEquals(new BigDecimal("12.50"), DatanormImporter.decodeDatanormPrice("125000", 2));
        // 3 = per 1000: 450000 cents -> 4500.00 / 1000 = 4.50
        assertEquals(new BigDecimal("4.50"), DatanormImporter.decodeDatanormPrice("450000", 3));
        // Direct decimal string with German comma
        assertEquals(new BigDecimal("19.95"), DatanormImporter.decodeDatanormPrice("19,95", 0));
        // Empty / invalid gracefully returns 0.00
        assertEquals(new BigDecimal("0.00"), DatanormImporter.decodeDatanormPrice("", 0));
        assertEquals(new BigDecimal("0.00"), DatanormImporter.decodeDatanormPrice(null, 2));
        assertEquals(new BigDecimal("0.00"), DatanormImporter.decodeDatanormPrice("---", 0));
    }

    @Test
    void datanormMultiFileDiscountsNetPricesDeletionAndGroups() {
        String rab = ""
                + "R;RG10;1000;\n"           // 10.00% without action flag
                + "R;N;RG25;25,5;\n"         // 25.5% with action flag and comma decimal
                + "S;WG01;;Sanitär Kupfer;\n";

        String datanorm001 = ""
                + "V;050;S;20261101;CHF;Muster Grosshandel AG;\n"
                // priceFlag=1 (list price), priceUnit=2 (/100), 200000 cents = 20.00 EUR, RG25 (25.5% off) => 14.90
                + "A;N;CU-22;00;Kupferrohr 22mm;Stange 5m;1;2;m;200000;RG25;WG01;\n"
                // priceFlag=2 (net purchase price directly), discount group must NOT be subtracted again!
                + "A;N;NET-01;00;Absperrhahn 1/2;Messing;2;0;Stk;1850;RG25;WG01;\n"
                // Article created then deleted via A;L
                + "A;N;OLD-99;00;Auslaufartikel;;1;0;Stk;990;;;\n"
                + "A;L;OLD-99;00;;;;;;0;;;\n"
                // B record with matchcode & 13-digit EAN
                + "B;N;CU-22;KUPFER22;1;2;3;4;5;4012345678901;\n"
                // Multi-line T long text
                + "T;N;CU-22;1;Zeile 1 nach EN 1057;Zeile 2 DVGW;\n"
                + "T;N;CU-22;2;Zeile 3 R250;\n";

        // DATPREIS without processing flag (P;sku;priceFlag;priceCents) updating NET-01
        String datpreis = "P;NET-01;2;2100;\n";

        ParseResult res = DatanormImporter.parse(rab, datanorm001, datpreis);
        assertEquals("5", res.version);
        assertEquals("CHF", res.currency);
        assertEquals(new BigDecimal("10.00"), res.discountGroups.get("RG10"));
        assertEquals(new BigDecimal("25.5"), res.discountGroups.get("RG25"));
        assertEquals("Sanitär Kupfer", res.productGroups.get("WG01"));

        Article cu = res.articles.get("CU-22");
        assertNotNull(cu);
        assertFalse(cu.deleted);
        assertEquals("Kupferrohr 22mm", cu.name);
        assertEquals("Stange 5m", cu.shortText2);
        assertEquals("KUPFER22", cu.matchcode);
        assertEquals("4012345678901", cu.ean);
        assertTrue(cu.longText.contains("Zeile 1") && cu.longText.contains("Zeile 3"));
        assertEquals(new BigDecimal("20.00"), cu.rawUnitPrice);
        assertEquals(new BigDecimal("14.90"), cu.netPurchasePrice, "20.00 * (1 - 0.255) = 14.90");

        Article net = res.articles.get("NET-01");
        assertNotNull(net);
        assertEquals(new BigDecimal("21.00"), net.netPurchasePrice, "priceFlag=2 must ignore RG25 discount");
        assertEquals(1, res.priceUpdates);

        Article deleted = res.articles.get("OLD-99");
        assertNotNull(deleted);
        assertTrue(deleted.deleted, "A;L must mark article as deleted");
    }

    @Test
    void datanormDecodeBytesSupportsUtf8AndWindows1252() {
        String text = "Wärmepumpe Größe 12 – 150 €";
        assertEquals(text, DatanormImporter.decodeBytes(text.getBytes(StandardCharsets.UTF_8)));

        byte[] win1252 = "Rohrbogen 90° Größe 22mm €".getBytes(Charset.forName("windows-1252"));
        String decoded = DatanormImporter.decodeBytes(win1252);
        assertTrue(decoded.contains("Größe") && decoded.contains("€"), "Windows-1252 fallback: " + decoded);

        assertEquals("", DatanormImporter.decodeBytes(null));
        assertEquals("", DatanormImporter.decodeBytes(new byte[0]));
    }

    // --------------------------------------------------- MeasurementEvaluator

    @Test
    void measurementEvaluatorPrecedenceSymbolsAndPrecision() {
        // Operator precedence (* before +)
        assertEquals(new BigDecimal("14.000"), MeasurementEvaluator.evaluate("2 + 3 * 4"));
        assertEquals(new BigDecimal("20.000"), MeasurementEvaluator.evaluate("(2 + 3) * 4"));

        // Nested parentheses & room wall area minus window and door deductions
        // 2 * ((4.5 + 3.2) * 2.6) - (1.2 * 1.4 + 0.9 * 2.1) = 40.04 - (1.68 + 1.89) = 36.47
        assertEquals(new BigDecimal("36.470"),
                MeasurementEvaluator.evaluate("2 * ((4,50 + 3,20) * 2,60) - (1,20 * 1,40 + 0,90 * 2,10)"));

        // Unicode multiplication symbols × and ·
        assertEquals(new BigDecimal("12.500"), MeasurementEvaluator.evaluate("2,5 × 5"));
        assertEquals(new BigDecimal("7.500"), MeasurementEvaluator.evaluate("3 · 2.5"));

        // Unary signs and repeating decimals rounded to 3 decimal places (HALF_UP)
        assertEquals(new BigDecimal("5.000"), MeasurementEvaluator.evaluate("+2 + +3"));
        assertEquals(new BigDecimal("6.000"), MeasurementEvaluator.evaluate("-2 * -3"));
        assertEquals(new BigDecimal("0.333"), MeasurementEvaluator.evaluate("1 / 3"));
        assertEquals(new BigDecimal("0.667"), MeasurementEvaluator.evaluate("2 / 3"));
    }

    @Test
    void measurementEvaluatorRejectsMalformedAndUnsafeInputs() {
        assertThrows(IllegalArgumentException.class, () -> MeasurementEvaluator.evaluate(null));
        assertThrows(IllegalArgumentException.class, () -> MeasurementEvaluator.evaluate("   "));
        assertThrows(IllegalArgumentException.class, () -> MeasurementEvaluator.evaluate("5 / 0"));
        assertThrows(IllegalArgumentException.class, () -> MeasurementEvaluator.evaluate("5 / (2 - 2)"));
        assertThrows(IllegalArgumentException.class, () -> MeasurementEvaluator.evaluate("(1 + 2"));
        assertThrows(IllegalArgumentException.class, () -> MeasurementEvaluator.evaluate("1 + 2)"));
        assertThrows(IllegalArgumentException.class, () -> MeasurementEvaluator.evaluate("2 * * 3"));
        assertThrows(IllegalArgumentException.class, () -> MeasurementEvaluator.evaluate("1.2.3"));
        assertThrows(IllegalArgumentException.class, () -> MeasurementEvaluator.evaluate("System.exit(0)"));
        assertThrows(IllegalArgumentException.class, () -> MeasurementEvaluator.evaluate("10 + foo"));
    }
}
