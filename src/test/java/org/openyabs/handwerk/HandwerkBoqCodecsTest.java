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
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.openyabs.handwerk.BoqDocument.BoqPosition;
import org.openyabs.handwerk.BoqDocument.PositionType;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Extensive unit tests for {@link BoqDocument}, {@link BoqCodec},
 * {@link GaebXmlCodec}, {@link Gaeb90Codec}, and {@link CsvBoqCodec}.
 */
class HandwerkBoqCodecsTest {

    // -------------------------------------------------------- BoqDocument

    @Test
    void boqDocumentTotalsAndMandatoryUnpricedSemantics() {
        BoqDocument doc = new BoqDocument();

        BoqPosition text = new BoqPosition();
        text.ordinalNo = "01";
        text.positionType = PositionType.TEXT;
        text.quantity = BigDecimal.TEN;
        text.unitPrice = new BigDecimal("99.00");
        assertEquals(new BigDecimal("0.00"), text.lineTotal(), "TEXT positions always have 0.00 line total");
        assertFalse(text.isCountedInTotal());

        BoqPosition normal = new BoqPosition();
        normal.ordinalNo = "01.0010";
        normal.positionType = PositionType.NORMAL;
        normal.quantity = new BigDecimal("10.5");
        normal.unitPrice = new BigDecimal("20.00"); // 210.00

        BoqPosition base = new BoqPosition();
        base.ordinalNo = "01.0020";
        base.positionType = PositionType.BASE;
        base.quantity = new BigDecimal("2");
        base.unitPrice = new BigDecimal("50.00"); // 100.00

        BoqPosition alt = new BoqPosition();
        alt.ordinalNo = "01.0030";
        alt.positionType = PositionType.ALTERNATIVE;
        alt.quantity = new BigDecimal("2");
        alt.unitPrice = new BigDecimal("75.00"); // NOT counted in total

        BoqPosition opt = new BoqPosition();
        opt.ordinalNo = "01.0040";
        opt.positionType = PositionType.OPTIONAL;
        opt.quantity = new BigDecimal("5");
        opt.unitPrice = BigDecimal.ZERO; // NOT mandatory

        BoqPosition unpricedNormal = new BoqPosition();
        unpricedNormal.ordinalNo = "01.0050";
        unpricedNormal.positionType = PositionType.NORMAL;
        unpricedNormal.quantity = new BigDecimal("3");
        unpricedNormal.unitPrice = BigDecimal.ZERO; // mandatory unpriced!

        BoqPosition zeroQtyNormal = new BoqPosition();
        zeroQtyNormal.ordinalNo = "01.0060";
        zeroQtyNormal.positionType = PositionType.NORMAL;
        zeroQtyNormal.quantity = BigDecimal.ZERO;
        zeroQtyNormal.unitPrice = BigDecimal.ZERO; // qty 0 -> not counted as mandatory unpriced

        doc.positions.add(text);
        doc.positions.add(normal);
        doc.positions.add(base);
        doc.positions.add(alt);
        doc.positions.add(opt);
        doc.positions.add(unpricedNormal);
        doc.positions.add(zeroQtyNormal);

        assertEquals(new BigDecimal("310.00"), doc.totalNet(), "Only NORMAL + BASE count toward BoQ total");
        assertEquals(1, doc.countMandatoryUnpriced(), "Only positive-qty NORMAL/BASE with UP <= 0 count");
    }

    // -------------------------------------------------------- GAEB DA XML

    @Test
    void gaebXmlNestedHierarchyVersionsAndAllPositionTypes() throws Exception {
        String xml32 = "\uFEFF<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<GAEB xmlns=\"http://www.gaeb.de/GAEB_DA_XML/DA83/3.2\">\n"
                + "  <GAEBInfo><Version>3.2</Version><VersDate>2013-10</VersDate></GAEBInfo>\n"
                + "  <PrjInfo><LblPrj>Klinikum Süd</LblPrj></PrjInfo>\n"
                + "  <Award>\n"
                + "    <AwardInfo><Cur>CHF</Cur></AwardInfo>\n"
                + "    <DP>83</DP>\n"
                + "    <BoQ ID=\"B1\">\n"
                + "      <BoQInfo><LblBoQ>Heizungstechnik</LblBoQ></BoQInfo>\n"
                + "      <BoQBody>\n"
                + "        <BoQCtgy ID=\"L1\" RNoPart=\"01\">\n"
                + "          <LblTx><p>Los 1 Wärmeerzeugung</p></LblTx>\n"
                + "          <BoQBody>\n"
                + "            <BoQCtgy ID=\"T1\" RNoPart=\"02\">\n"
                + "              <LblTx><p>Wärmepumpen</p></LblTx>\n"
                + "              <BoQBody>\n"
                + "                <Itemlist>\n"
                + "                  <Item ID=\"I1\" RNoPart=\"0010\">\n"
                + "                    <Provis>No</Provis>\n"
                + "                    <Qty>1</Qty><QU>Stk</QU>\n"
                + "                    <Description><CompleteText><OutlineText><OutlTxt><TextOutlTxt>Sole-WP 25 kW</TextOutlTxt></OutlTxt></OutlineText></CompleteText></Description>\n"
                + "                  </Item>\n"
                + "                  <Item ID=\"I2\" RNoPart=\"0020\">\n"
                + "                    <ALNGroupNo>1</ALNGroupNo><ALNSerNo>0</ALNSerNo>\n"
                + "                    <Qty>1</Qty><QU>Stk</QU>\n"
                + "                    <Description><CompleteText><OutlineText><OutlTxt><TextOutlTxt>Pufferspeicher 1000L (Grundposition)</TextOutlTxt></OutlTxt></OutlineText></CompleteText></Description>\n"
                + "                  </Item>\n"
                + "                  <Item ID=\"I3\" RNoPart=\"0030\">\n"
                + "                    <ALNGroupNo>1</ALNGroupNo><ALNSerNo>1</ALNSerNo>\n"
                + "                    <Qty>1</Qty><QU>Stk</QU>\n"
                + "                    <Description><CompleteText><OutlineText><OutlTxt><TextOutlTxt>Pufferspeicher 1500L (Alternative)</TextOutlTxt></OutlTxt></OutlineText></CompleteText></Description>\n"
                + "                  </Item>\n"
                + "                  <Item ID=\"I4\" RNoPart=\"0040\">\n"
                + "                    <Provis>WithTotal</Provis>\n"
                + "                    <Qty>4</Qty><QU>Std</QU>\n"
                + "                    <Description><CompleteText><DetailTxt><Text><p>Zusätzliche Einregulierung nach Aufwand.</p></Text></DetailTxt></CompleteText></Description>\n"
                + "                  </Item>\n"
                + "                </Itemlist>\n"
                + "              </BoQBody>\n"
                + "            </BoQCtgy>\n"
                + "          </BoQBody>\n"
                + "        </BoQCtgy>\n"
                + "      </BoQBody>\n"
                + "    </BoQ>\n"
                + "  </Award>\n"
                + "</GAEB>";

        BoqCodec codec = BoqCodec.findReader("lv.X83", xml32);
        assertNotNull(codec);
        assertTrue(codec.canExportBid("GAEB_XML_32"));
        assertEquals("X84", codec.bidFileExtension());

        BoqDocument doc = codec.read("lv.X83", xml32);
        assertEquals("GAEB_XML_32", doc.formatCode);
        assertEquals("83", doc.exchangePhase);
        assertEquals("Klinikum Süd", doc.projectName);
        assertEquals("Heizungstechnik", doc.boqLabel);
        assertEquals("CHF", doc.currency);
        // 2 category TEXT headers + 4 items = 6
        assertEquals(6, doc.positions.size());

        assertEquals("01", doc.positions.get(0).ordinalNo);
        assertEquals(PositionType.TEXT, doc.positions.get(0).positionType);
        assertEquals("01.02", doc.positions.get(1).ordinalNo);
        assertEquals(PositionType.TEXT, doc.positions.get(1).positionType);

        BoqPosition wp = doc.positions.get(2);
        assertEquals("01.02.0010", wp.ordinalNo);
        assertEquals(PositionType.NORMAL, wp.positionType, "Provis=No must stay NORMAL");
        assertTrue(wp.categoryPath.contains("Los 1") && wp.categoryPath.contains("Wärmepumpen"));

        BoqPosition base = doc.positions.get(3);
        assertEquals("01.02.0020", base.ordinalNo);
        assertEquals(PositionType.BASE, base.positionType, "ALNSerNo=0 must be BASE");

        BoqPosition alt = doc.positions.get(4);
        assertEquals("01.02.0030", alt.ordinalNo);
        assertEquals(PositionType.ALTERNATIVE, alt.positionType, "ALNSerNo=1 must be ALTERNATIVE");

        BoqPosition opt = doc.positions.get(5);
        assertEquals("01.02.0040", opt.ordinalNo);
        assertEquals(PositionType.OPTIONAL, opt.positionType);
        assertTrue(opt.shortText.contains("Einregulierung"), "Fallback shortText from DetailTxt");

        // Export X84: verify DA84 namespace, DP=84, and category Totals
        Map<String, BigDecimal> prices = new LinkedHashMap<String, BigDecimal>();
        prices.put("01.02.0010", new BigDecimal("18500.00"));
        prices.put("01.02.0020", new BigDecimal("2400.00"));
        prices.put("01.02.0030", new BigDecimal("3100.00"));
        prices.put("01.02.0040", new BigDecimal("90.00"));

        String x84 = new String(codec.exportBid(doc, prices, Collections.emptyMap(), "2.1.0"), StandardCharsets.UTF_8);
        assertTrue(x84.contains("GAEB_DA_XML/DA84/3.2"));
        assertTrue(x84.contains("<DP>84</DP>"));
        assertTrue(x84.contains("<Total>20900.00</Total>"), "Category total = 18500 + 2400 (excludes ALT & OPT)");
    }

    @Test
    void gaebXmlMinimalFallbackExportAndSecurityRejections() throws Exception {
        GaebXmlCodec codec = new GaebXmlCodec();

        // Fallback export when rawSource is empty (e.g., programmatically built BoqDocument)
        BoqDocument manual = new BoqDocument();
        manual.formatCode = "GAEB_XML_33";
        manual.projectName = "Manuelles LV & <Test>";
        manual.currency = "EUR";
        BoqPosition p = new BoqPosition();
        p.ordinalNo = "0010";
        p.quantity = new BigDecimal("5");
        p.unit = "m";
        p.shortText = "Kabel verlegen";
        manual.positions.add(p);

        String x84 = new String(
                codec.exportBid(manual, Collections.singletonMap("0010", new BigDecimal("12.00")),
                        Collections.emptyMap(), "2.1.0"),
                StandardCharsets.UTF_8);
        assertTrue(x84.contains("GAEB_DA_XML/DA84/3.3"));
        assertTrue(x84.contains("<UP>12.00</UP>"));
        assertTrue(x84.contains("<IT>60.00</IT>"));

        // XXE / DOCTYPE must be rejected
        String xxe = "<?xml version=\"1.0\"?><!DOCTYPE foo [<!ENTITY xxe SYSTEM \"file:///etc/passwd\">]>"
                + "<GAEB><PrjInfo><NamePrj>&xxe;</NamePrj></PrjInfo></GAEB>";
        assertThrows(Exception.class, () -> codec.read("evil.X83", xxe),
                "DOCTYPE / XXE payloads must be rejected");

        // Non-GAEB root element rejected
        assertThrows(IllegalArgumentException.class, () -> codec.read("other.xml", "<Invoice/>"));
    }

    // ------------------------------------------------------------ GAEB 90

    @Test
    void gaeb90HierarchyOptionalAlternativeAndBidderTextExport() throws Exception {
        String d83 = ""
                + "0083GAEB90                                                                    01\n"
                + "01Umbau Rathaus Musterstadt                                                   02\n"
                + "1101       Rohbauarbeiten                                                     03\n"
                + "21010010   N           00000025500m2                                          04\n"
                + "25Mauerwerk Kalksandstein 24cm                                                05\n"
                + "26Liefern und vermauern von KS-Plansteinen                                    06\n"
                + "26in Dünnbettmörtel nach DIN EN 1996.                                         07\n"
                + "21010020   E           00000005000m                                           08\n"
                + "25Eventualposition Schlitz fräsen                                             09\n"
                + "21010030   A           00000002000Stk                                         10\n"
                + "25Alternativposition Türsturz                                                 11\n";

        Gaeb90Codec codec = new Gaeb90Codec();
        assertTrue(codec.canRead("bau.D83", d83));
        assertTrue(codec.canExportBid("GAEB_90"));
        assertEquals("D84", codec.bidFileExtension());

        BoqDocument doc = codec.read("bau.D83", d83);
        assertEquals("83", doc.exchangePhase);
        assertEquals("Umbau Rathaus Musterstadt", doc.projectName);
        assertEquals(4, doc.positions.size()); // 1 header + 3 items
        assertEquals(PositionType.TEXT, doc.positions.get(0).positionType);
        assertEquals(PositionType.NORMAL, doc.positions.get(1).positionType);
        assertEquals(0, new BigDecimal("25.500").compareTo(doc.positions.get(1).quantity));
        assertTrue(doc.positions.get(1).longText.contains("DIN EN 1996"));
        assertEquals(PositionType.OPTIONAL, doc.positions.get(2).positionType);
        assertEquals(PositionType.ALTERNATIVE, doc.positions.get(3).positionType);

        Map<String, BigDecimal> prices = new LinkedHashMap<String, BigDecimal>();
        prices.put("010010", new BigDecimal("100.00")); // 25.5 * 100 = 2550.00 -> 255000 cents
        prices.put("010020", new BigDecimal("40.00"));  // optional -> excluded from grand total
        prices.put("010030", new BigDecimal("95.00"));  // alternative -> excluded from grand total

        String d84 = new String(
                codec.exportBid(doc, prices, Collections.singletonMap("010010", "Fabrikat Xella"), "2.1.0"),
                StandardCharsets.ISO_8859_1);
        assertTrue(d84.startsWith("0084"));
        assertTrue(d84.contains("26Fabrikat Xella"));
        assertTrue(d84.contains("9900000000255000"), "Grand total record 99 must be 255000 cents: " + d84);
    }

    // ------------------------------------------------------------ CSV BoQ

    @Test
    void csvBoqDelimitersQuotesGermanDecimalsHeaderlessAndTypeSynonyms() throws Exception {
        CsvBoqCodec codec = new CsvBoqCodec();
        assertTrue(codec.canRead("dpgf.csv", ""));
        assertTrue(codec.canExportBid("CSV"));
        assertEquals("csv", codec.bidFileExtension());

        // Comma-delimited with BOM, quoted embedded commas, escaped quotes, and type synonyms
        String commaCsv = "\uFEFFOrdinal,Category,Art,Menge,Einheit,Kurztext,Langtext,EP\n"
                + "01,Titel 1,TITEL,0,,\"Elektroinstallation, EG\",\"\",0\n"
                + "01.01,Titel 1,NORMAL,10,Stk,\"Steckdose \"\"Busch-Jaeger\"\", UP\",\"Unterputz\",15.50\n"
                + "01.02,Titel 1,EP,2,Stk,\"Bedarfsposition Dimmer\",\"Optional\",45.00\n"
                + "01.03,Titel 1,WAHL,1,Stk,\"Alternativposition Rahmen Glas\",\"Alternative\",30.00\n"
                + "01.04,Titel 1,GRUND,1,Psch,\"Inbetriebnahme\",\"Grundposition\",120.00\n";

        BoqDocument doc = codec.read("dpgf.csv", commaCsv);
        assertEquals(5, doc.positions.size());
        assertEquals(PositionType.TEXT, doc.positions.get(0).positionType);
        assertEquals("Steckdose \"Busch-Jaeger\", UP", doc.positions.get(1).shortText);
        assertEquals(PositionType.OPTIONAL, doc.positions.get(2).positionType);
        assertEquals(PositionType.ALTERNATIVE, doc.positions.get(3).positionType);
        assertEquals(PositionType.BASE, doc.positions.get(4).positionType);
        // Total = 10*15.50 + 1*120.00 = 275.00
        assertEquals(new BigDecimal("275.00"), doc.totalNet());

        // German thousand-dot + comma-decimal format
        String deCsv = "OZ;Menge;Einheit;Kurztext;Preis\n"
                + "10;2;Stk;Heizkessel;1.234,50\n";
        BoqDocument deDoc = codec.read("heizung.csv", deCsv);
        assertEquals(1, deDoc.positions.size());
        assertEquals(0, new BigDecimal("1234.50").compareTo(deDoc.positions.get(0).unitPrice));
        assertEquals("Stk", deDoc.positions.get(0).unit);

        // Headerless CSV fallback
        String noHeader = "01.10;Rohbau;NORMAL;4;m3;Beton C25/30;Transportbeton;145,00\n";
        BoqDocument nhDoc = codec.read("plain.csv", noHeader);
        assertEquals(1, nhDoc.positions.size());
        assertEquals("01.10", nhDoc.positions.get(0).ordinalNo);
        assertEquals(0, new BigDecimal("145.00").compareTo(nhDoc.positions.get(0).unitPrice));
    }
}
