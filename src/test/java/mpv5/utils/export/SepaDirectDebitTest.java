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
package mpv5.utils.export;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Validates OpenYaBS core SEPA Direct Debit ({@code pain.008.001.08}) XML
 * generation against the official ISO 20022 XSD schema and EPC130-08 rules.
 */
class SepaDirectDebitTest {

    @Test
    void generatedPain00800108PassesOfficialIso20022XsdValidation() {
        SepaDirectDebit.Creditor creditor = new SepaDirectDebit.Creditor(
                "Sportverein M\u00fcnchen e.V. & Co.",
                "DE89 3704 0044 0532 0130 00",
                "COBADEFFXXX",
                "DE98ZZZ09999999999");

        SepaDirectDebit.Debit d1 = new SepaDirectDebit.Debit(
                "/INV//2026-001/",
                "J\u00fcrgen M\u00fcller-Wei\u00df",
                "DE44 5001 0517 5407 3249 31",
                "INGDDEFFXXX",
                "/M//001/",
                LocalDate.of(2024, 1, 15),
                new BigDecimal("120.00"),
                "Mitgliedsbeitrag 2026 \u2013 Aktiv");

        // Second debit without BIC (must use <Othr><Id>NOTPROVIDED</Id></Othr>)
        SepaDirectDebit.Debit d2 = new SepaDirectDebit.Debit(
                "INV-2026-002",
                "Anna Schmidt",
                "DE89370400440532013000",
                "",
                "M-002",
                LocalDate.of(2025, 3, 1),
                new BigDecimal("60.50"),
                "Jugendbeitrag 2026");

        String xml = SepaDirectDebit.toXml(
                creditor,
                Arrays.asList(d1, d2),
                LocalDate.of(2026, 11, 25),
                LocalDateTime.of(2026, 11, 19, 12, 0, 0));

        assertDoesNotThrow(() -> SepaDirectDebit.validateXml(xml),
                "Generated pain.008.001.08 XML must pass strict ISO 20022 XSD validation");
        assertTrue(xml.contains("<CtrlSum>180.50</CtrlSum>"));
        assertTrue(xml.contains("<EndToEndId>INV/2026-001</EndToEndId>"),
                "EPC130-08 forbids leading/trailing slashes and // in EndToEndId");
        assertTrue(xml.contains("<MndtId>M/001</MndtId>"),
                "EPC130-08 forbids leading/trailing slashes and // in MndtId");
        assertTrue(xml.contains("<Othr><Id>NOTPROVIDED</Id></Othr>"));
    }

    @Test
    void xsdValidatorRejectsBrokenPain008Xml() {
        String broken = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<Document xmlns=\"urn:iso:std:iso:20022:tech:xsd:pain.008.001.08\">"
                + "<CstmrDrctDbtInitn><GrpHdr><MsgId>BAD</MsgId></GrpHdr></CstmrDrctDbtInitn>"
                + "</Document>";
        assertThrows(IllegalStateException.class, () -> SepaDirectDebit.validateXml(broken));
    }

    @Test
    void rejectsInvalidCreditorOrDebtorInputs() {
        SepaDirectDebit.Creditor validCreditor = new SepaDirectDebit.Creditor(
                "Club e.V.", "DE89370400440532013000", "", "DE98ZZZ09999999999");
        SepaDirectDebit.Creditor badCreditorId = new SepaDirectDebit.Creditor(
                "Club e.V.", "DE89370400440532013000", "", "DE00ZZZ09999999999");
        SepaDirectDebit.Debit validDebit = new SepaDirectDebit.Debit(
                "E1", "Max", "DE44500105175407324931", "", "M1",
                LocalDate.of(2025, 1, 1), new BigDecimal("10.00"), "Ref");

        assertTrue(SepaDirectDebit.isValidCreditorId("DE98ZZZ09999999999"));
        assertFalse(SepaDirectDebit.isValidCreditorId("DE00ZZZ09999999999"));

        assertThrows(IllegalArgumentException.class, () -> SepaDirectDebit.toXml(
                badCreditorId, Collections.singletonList(validDebit), LocalDate.now(), LocalDateTime.now()));
        assertThrows(IllegalArgumentException.class, () -> SepaDirectDebit.toXml(
                validCreditor, Collections.emptyList(), LocalDate.now(), LocalDateTime.now()));
    }
}
