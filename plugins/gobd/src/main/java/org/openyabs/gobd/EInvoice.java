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
package org.openyabs.gobd;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentNameDictionary;
import org.apache.pdfbox.pdmodel.PDEmbeddedFilesNameTreeNode;
import org.apache.pdfbox.pdmodel.common.filespecification.PDComplexFileSpecification;
import org.apache.pdfbox.pdmodel.common.filespecification.PDEmbeddedFile;
import org.w3c.dom.Document;

/**
 * Reads e-invoices: the XML embedded in a ZUGFeRD / Factur-X PDF and the
 * fields of EN 16931 (CII and UBL) the income import needs
 */
public final class EInvoice {

    private EInvoice() {
    }

    /**
     * The fields read from an e-invoice
     */
    public static final class Parsed {

        public final List<String> errors = new ArrayList<String>();
        public String documentNumber = "";
        public Date issueDate;
        public Date dueDate;
        public String partnerName = "";
        public BigDecimal netAmount;
        public BigDecimal grossAmount;
        public BigDecimal taxPercent;
        /**
         * A credit note (TypeCode 381 / UBL CreditNote): the amounts are
         * negative
         */
        public boolean creditNote;
        /**
         * More than one tax rate: taxPercent is the effective rate
         */
        public boolean mixedTaxRates;
        /**
         * The tax rates of the invoice (header level), e.g. "7.00 %, 19.00 %"
         */
        public String taxRates = "";

        /**
         * @return true if all mandatory fields are there
         */
        public boolean valid() {
            return errors.isEmpty();
        }
    }

    /**
     * Whether a created invoice file shows the number: the number of the
     * embedded / XML e-invoice, else the text of the PDF (blanks ignored)
     *
     * @param file a PDF or XML file
     * @param number
     * @return true if the file shows the number
     */
    public static boolean mentionsNumber(Path file, String number) {
        if (file == null || number == null || number.trim().isEmpty()) {
            return false;
        }
        String wanted = number.replaceAll("\\s+", "");
        try {
            String name = file.getFileName().toString().toLowerCase(Locale.ROOT);
            byte[] xml = name.endsWith(".xml") ? java.nio.file.Files.readAllBytes(file) : name.endsWith(".pdf") ? extractEmbeddedXml(file) : null;
            if (xml != null) {
                Parsed p = parse(xml);
                if (!p.documentNumber.isEmpty()) {
                    return p.documentNumber.replaceAll("\\s+", "").equals(wanted);
                }
            }
            if (name.endsWith(".pdf")) {
                try (PDDocument doc = org.apache.pdfbox.Loader.loadPDF(file.toFile())) {
                    String text = new org.apache.pdfbox.text.PDFTextStripper().getText(doc);
                    return text.replaceAll("\\s+", "").contains(wanted);
                }
            }
            if (xml != null) {
                return new String(xml, java.nio.charset.StandardCharsets.UTF_8).replaceAll("\\s+", "").contains(wanted);
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }

    private static final List<String> PREFERRED = Arrays.asList("factur-x.xml", "zugferd-invoice.xml", "xrechnung.xml");

    /**
     * @param pdfPath
     * @return the e-invoice XML embedded in the PDF, null if there is none
     */
    public static byte[] extractEmbeddedXml(Path pdfPath) {
        //PDFBox 3: Loader.loadPDF (PDDocument.load of PDFBox 2 is gone)
        try (PDDocument doc = org.apache.pdfbox.Loader.loadPDF(pdfPath.toFile())) {
            PDDocumentNameDictionary names = doc.getDocumentCatalog() == null ? null : doc.getDocumentCatalog().getNames();
            PDEmbeddedFilesNameTreeNode tree = names == null ? null : names.getEmbeddedFiles();
            if (tree == null) {
                return null;
            }
            Map<String, PDComplexFileSpecification> all = new LinkedHashMap<String, PDComplexFileSpecification>();
            collect(tree, all);
            PDComplexFileSpecification chosen = null;
            for (String preferred : PREFERRED) {
                for (Map.Entry<String, PDComplexFileSpecification> e : all.entrySet()) {
                    String fn = fileName(e.getKey(), e.getValue());
                    if (fn.equals(preferred) || fn.endsWith("/" + preferred)) {
                        chosen = e.getValue();
                        break;
                    }
                }
                if (chosen != null) {
                    break;
                }
            }
            if (chosen == null) {
                for (Map.Entry<String, PDComplexFileSpecification> e : all.entrySet()) {
                    if (fileName(e.getKey(), e.getValue()).endsWith(".xml")) {
                        chosen = e.getValue();
                        break;
                    }
                }
            }
            if (chosen == null) {
                return null;
            }
            PDEmbeddedFile ef = chosen.getEmbeddedFileUnicode() != null ? chosen.getEmbeddedFileUnicode() : chosen.getEmbeddedFile();
            return ef == null ? null : ef.toByteArray();
        } catch (Exception e) {
            return null;
        }
    }

    private static String fileName(String key, PDComplexFileSpecification v) {
        String fn = null;
        if (v != null) {
            fn = v.getFileUnicode() != null ? v.getFileUnicode() : v.getFile();
        }
        if (fn == null) {
            fn = key == null ? "" : key;
        }
        return fn.toLowerCase(Locale.ROOT);
    }

    private static void collect(org.apache.pdfbox.pdmodel.common.PDNameTreeNode<PDComplexFileSpecification> node,
            Map<String, PDComplexFileSpecification> all) {
        if (node == null) {
            return;
        }
        try {
            Map<String, PDComplexFileSpecification> m = node.getNames();
            if (m != null) {
                all.putAll(m);
            }
        } catch (Exception ignored) {
            //no names on this node
        }
        try {
            List<org.apache.pdfbox.pdmodel.common.PDNameTreeNode<PDComplexFileSpecification>> kids = node.getKids();
            if (kids != null) {
                for (org.apache.pdfbox.pdmodel.common.PDNameTreeNode<PDComplexFileSpecification> k : kids) {
                    collect(k, all);
                }
            }
        } catch (Exception ignored) {
            //no kids
        }
    }

    /**
     * @param raw yyyyMMdd, yyyy-MM-dd(...) or dd.MM.yyyy
     * @return the date, null if none
     */
    public static Date parseDate(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return null;
        }
        String s = raw.trim();
        for (String pattern : new String[]{"yyyyMMdd", "yyyy-MM-dd", "dd.MM.yyyy"}) {
            try {
                SimpleDateFormat df = new SimpleDateFormat(pattern);
                df.setLenient(false);
                return df.parse(s.length() > 10 && pattern.equals("yyyy-MM-dd") ? s.substring(0, 10) : s);
            } catch (Exception ignored) {
                //next pattern
            }
        }
        return null;
    }

    /**
     * @param raw
     * @return the amount with 2 decimals (a comma is a decimal point), null if none
     */
    public static BigDecimal parseDecimal(String raw) {
        if (raw == null) {
            return null;
        }
        String s = raw.trim().replace(",", ".");
        if (s.isEmpty()) {
            return null;
        }
        try {
            return new BigDecimal(s).setScale(2, RoundingMode.HALF_UP);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Reads BT-1, BT-2, BT-9, the partner, BT-109, BT-112 and BT-119 of a CII
     * or UBL invoice
     *
     * @param xmlBytes
     * @return the fields; errors names the missing mandatory ones
     */
    public static Parsed parse(byte[] xmlBytes) {
        Parsed p = new Parsed();
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(false);
            try {
                dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            } catch (Exception ignored) {
                //not supported by this parser
            }
            Document dom = dbf.newDocumentBuilder().parse(new ByteArrayInputStream(xmlBytes));
            XPath xp = XPathFactory.newInstance().newXPath();

            //BT-1: invoice number
            p.documentNumber = eval(xp, dom,
                    "//*[local-name()='ExchangedDocument']/*[local-name()='ID']",
                    "/*[local-name()='Invoice' or local-name()='CreditNote']/*[local-name()='ID']");
            //BT-2: issue date
            p.issueDate = parseDate(eval(xp, dom,
                    "//*[local-name()='ExchangedDocument']/*[local-name()='IssueDateTime']/*[local-name()='DateTimeString']",
                    "/*[local-name()='Invoice' or local-name()='CreditNote']/*[local-name()='IssueDate']"));
            //BT-9: due date (optional)
            Date due = parseDate(eval(xp, dom,
                    "//*[local-name()='SpecifiedTradePaymentTerms']/*[local-name()='DueDateDateTime']/*[local-name()='DateTimeString']",
                    "/*[local-name()='Invoice' or local-name()='CreditNote']/*[local-name()='DueDate']",
                    "//*[local-name()='PaymentMeans']/*[local-name()='PaymentDueDate']"));
            p.dueDate = due != null ? due : p.issueDate;
            //BT-27 / BT-44: seller, else buyer
            String seller = eval(xp, dom,
                    "//*[local-name()='SellerTradeParty']/*[local-name()='Name']",
                    "//*[local-name()='AccountingSupplierParty']//*[local-name()='PartyName']/*[local-name()='Name']",
                    "//*[local-name()='AccountingSupplierParty']//*[local-name()='PartyLegalEntity']/*[local-name()='RegistrationName']");
            String buyer = eval(xp, dom,
                    "//*[local-name()='BuyerTradeParty']/*[local-name()='Name']",
                    "//*[local-name()='AccountingCustomerParty']//*[local-name()='PartyName']/*[local-name()='Name']",
                    "//*[local-name()='AccountingCustomerParty']//*[local-name()='PartyLegalEntity']/*[local-name()='RegistrationName']");
            //income: the own invoice, the partner is the buyer
            p.partnerName = !buyer.isEmpty() ? buyer : seller;
            //BT-3: 381 credit note
            String typeCode = eval(xp, dom,
                    "//*[local-name()='ExchangedDocument']/*[local-name()='TypeCode']",
                    "/*[local-name()='Invoice']/*[local-name()='InvoiceTypeCode']",
                    "/*[local-name()='CreditNote']/*[local-name()='CreditNoteTypeCode']");
            p.creditNote = typeCode.equals("381") || "CreditNote".equals(dom.getDocumentElement().getLocalName())
                    || "CreditNote".equals(dom.getDocumentElement().getNodeName().replaceAll("^.*:", ""));
            //BT-109: net
            p.netAmount = parseDecimal(eval(xp, dom,
                    "//*[local-name()='SpecifiedTradeSettlementHeaderMonetarySummation']/*[local-name()='TaxBasisTotalAmount']",
                    "//*[local-name()='LegalMonetaryTotal']/*[local-name()='TaxExclusiveAmount']",
                    "//*[local-name()='SpecifiedTradeSettlementHeaderMonetarySummation']/*[local-name()='LineTotalAmount']"));
            //BT-112: gross
            p.grossAmount = parseDecimal(eval(xp, dom,
                    "//*[local-name()='SpecifiedTradeSettlementHeaderMonetarySummation']/*[local-name()='GrandTotalAmount']",
                    "//*[local-name()='LegalMonetaryTotal']/*[local-name()='TaxInclusiveAmount']",
                    "//*[local-name()='SpecifiedTradeSettlementHeaderMonetarySummation']/*[local-name()='DuePayableAmount']",
                    "//*[local-name()='LegalMonetaryTotal']/*[local-name()='PayableAmount']"));
            //BT-119: the tax rates of the document level (not of the first line)
            java.util.TreeSet<BigDecimal> rates = new java.util.TreeSet<BigDecimal>();
            for (String expr : new String[]{
                "//*[local-name()='ApplicableHeaderTradeSettlement']/*[local-name()='ApplicableTradeTax']/*[local-name()='RateApplicablePercent']",
                "/*/*[local-name()='TaxTotal']/*[local-name()='TaxSubtotal']/*[local-name()='TaxCategory']/*[local-name()='Percent']"}) {
                rates.addAll(decimals(xp, dom, expr));
            }
            if (rates.isEmpty()) {
                rates.addAll(decimals(xp, dom, "//*[local-name()='ApplicableTradeTax']/*[local-name()='RateApplicablePercent']"));
                rates.addAll(decimals(xp, dom, "//*[local-name()='ClassifiedTaxCategory']/*[local-name()='Percent']"));
            }
            List<String> rateTexts = new ArrayList<String>();
            for (BigDecimal r : rates) {
                rateTexts.add(r.toPlainString() + " %");
            }
            p.taxRates = String.join(", ", rateTexts);
            p.mixedTaxRates = rates.size() > 1;
            p.taxPercent = rates.size() == 1 ? rates.first() : null;
            if (p.taxPercent == null) {
                //none or several: the effective rate
                if (p.netAmount != null && p.grossAmount != null && p.netAmount.signum() != 0) {
                    p.taxPercent = p.grossAmount.subtract(p.netAmount).multiply(new BigDecimal("100"))
                            .divide(p.netAmount, 2, RoundingMode.HALF_UP);
                } else {
                    p.taxPercent = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
                }
            }
            if (p.creditNote) {
                p.netAmount = p.netAmount == null ? null : p.netAmount.abs().negate();
                p.grossAmount = p.grossAmount == null ? null : p.grossAmount.abs().negate();
            }
            if (p.documentNumber.isEmpty()) {
                p.errors.add("Pflichtfeld BT-1 (Rechnungsnummer / ExchangedDocument/ID) fehlt");
            }
            if (p.issueDate == null) {
                p.errors.add("Pflichtfeld BT-2 (Ausstellungsdatum / IssueDateTime) fehlt oder ungueltig");
            }
            if (p.netAmount == null) {
                p.errors.add("Pflichtfeld BT-109 (Nettobetrag / TaxBasisTotalAmount) fehlt");
            }
            if (p.grossAmount == null) {
                p.errors.add("Pflichtfeld BT-112 (Bruttobetrag / GrandTotalAmount) fehlt");
            }
        } catch (Exception ex) {
            p.errors.clear();
            p.errors.add("XML-Struktur konnte nicht nach EN 16931 gelesen werden: " + ex.getMessage());
        }
        return p;
    }

    private static List<BigDecimal> decimals(XPath xp, Document dom, String expr) {
        List<BigDecimal> l = new ArrayList<BigDecimal>();
        try {
            org.w3c.dom.NodeList nodes = (org.w3c.dom.NodeList) xp.evaluate(expr, dom, XPathConstants.NODESET);
            for (int i = 0; i < nodes.getLength(); i++) {
                BigDecimal d = parseDecimal(nodes.item(i).getTextContent());
                if (d != null) {
                    l.add(d);
                }
            }
        } catch (Exception ignored) {
            //none
        }
        return l;
    }

    private static String eval(XPath xp, Document dom, String... exprs) {
        for (String expr : exprs) {
            try {
                String val = (String) xp.evaluate(expr, dom, XPathConstants.STRING);
                if (val != null && !val.trim().isEmpty()) {
                    return val.trim();
                }
            } catch (Exception ignored) {
                //next expression
            }
        }
        return "";
    }
}
