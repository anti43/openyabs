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

import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.openyabs.handwerk.BoqDocument.BoqPosition;
import org.openyabs.handwerk.BoqDocument.PositionType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * GAEB DA XML (3.1 / 3.2 / 3.3) Bill-of-Quantities codec:
 * <ul>
 * <li>Imports {@code .X80}, {@code .X81}, {@code .X82}, {@code .X83},
 * {@code .X84}, {@code .X85}, {@code .X86}, {@code .X89} files into the
 * format-neutral {@link BoqDocument}.</li>
 * <li>Exports lossless {@code .X84} bid files from an imported tender by
 * updating unit prices ({@code <UP>}), line totals ({@code <IT>}), bidder text
 * additions, switching the exchange phase/namespace to {@code DA84}, and
 * regenerating {@code <GAEBInfo>} per GAEB DA XML 3.3 rules.</li>
 * </ul>
 */
public final class GaebXmlCodec implements BoqCodec {

    @Override
    public String id() {
        return "GAEB_XML";
    }

    @Override
    public boolean canRead(String fileName, String content) {
        String lower = fileName == null ? "" : fileName.toLowerCase(Locale.ROOT);
        if (lower.matches(".*\\.x8[0-9]$") || lower.matches(".*\\.x9[3-7]$")) {
            return true;
        }
        return content != null && content.contains("<GAEB");
    }

    @Override
    public boolean canExportBid(String formatCode) {
        return formatCode != null && formatCode.toUpperCase(Locale.ROOT).startsWith("GAEB_XML");
    }

    @Override
    public String bidFileExtension() {
        return "X84";
    }

    @Override
    public BoqDocument read(String fileName, String content) throws Exception {
        Document dom = parseXml(content);
        Element root = dom.getDocumentElement();
        if (root == null || !"GAEB".equals(localName(root))) {
            throw new IllegalArgumentException("Not a valid GAEB DA XML document (missing <GAEB> root element)");
        }

        BoqDocument doc = new BoqDocument();
        doc.rawSource = content;
        doc.importedAt = LocalDate.now().toString();

        String ns = root.getNamespaceURI() == null ? "" : root.getNamespaceURI();
        if (ns.contains("/3.3")) {
            doc.formatCode = "GAEB_XML_33";
        } else if (ns.contains("/3.2")) {
            doc.formatCode = "GAEB_XML_32";
        } else if (ns.contains("/3.1")) {
            doc.formatCode = "GAEB_XML_31";
        } else {
            doc.formatCode = "GAEB_XML_33";
        }

        String dp = firstChildTextDeep(root, "DP");
        if (!dp.isEmpty()) {
            doc.exchangePhase = dp;
        } else if (fileName != null && fileName.toLowerCase(Locale.ROOT).matches(".*\\.x\\d{2}$")) {
            doc.exchangePhase = fileName.substring(fileName.length() - 2);
        }

        String prjName = firstChildTextDeep(root, "NamePrj");
        String lblPrj = firstChildTextDeep(root, "LblPrj");
        doc.projectName = !lblPrj.isEmpty() ? lblPrj : (!prjName.isEmpty() ? prjName : stripExt(fileName));

        String lblBoq = firstChildTextDeep(root, "LblBoQ");
        if (lblBoq.isEmpty()) {
            Element boqInfo = firstElementDeep(root, "BoQInfo");
            if (boqInfo != null) {
                lblBoq = directChildText(boqInfo, "Name");
            }
        }
        doc.boqLabel = !lblBoq.isEmpty() ? lblBoq : doc.projectName;

        String cur = firstChildTextDeep(root, "Cur");
        if (!cur.isEmpty()) {
            doc.currency = cur.toUpperCase(Locale.ROOT);
        }

        Element boqBody = firstElementDeep(root, "BoQBody");
        if (boqBody != null) {
            walkBoqBody(boqBody, "", "", doc.positions);
        }
        return doc;
    }

    private void walkBoqBody(Element container, String ozPrefix, String categoryPath, List<BoqPosition> out) {
        NodeList children = container.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node n = children.item(i);
            if (!(n instanceof Element)) {
                continue;
            }
            Element el = (Element) n;
            String name = localName(el);
            if ("BoQCtgy".equals(name)) {
                String rno = attr(el, "RNoPart");
                String nextOz = joinOz(ozPrefix, rno);
                String title = extractCategoryTitle(el);
                String nextPath = categoryPath.isEmpty()
                        ? (nextOz + (title.isEmpty() ? "" : " " + title)).trim()
                        : categoryPath + " / " + (nextOz + (title.isEmpty() ? "" : " " + title)).trim();

                BoqPosition header = new BoqPosition();
                header.ordinalNo = nextOz;
                header.sourceItemId = attr(el, "ID");
                header.categoryPath = categoryPath;
                header.positionType = PositionType.TEXT;
                header.shortText = title.isEmpty() ? nextOz : title;
                out.add(header);

                Element subBody = directChild(el, "BoQBody");
                if (subBody != null) {
                    walkBoqBody(subBody, nextOz, nextPath, out);
                }
            } else if ("Itemlist".equals(name)) {
                walkItemList(el, ozPrefix, categoryPath, out);
            } else if ("BoQBody".equals(name)) {
                walkBoqBody(el, ozPrefix, categoryPath, out);
            }
        }
    }

    private void walkItemList(Element itemList, String ozPrefix, String categoryPath, List<BoqPosition> out) {
        NodeList children = itemList.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node n = children.item(i);
            if (!(n instanceof Element)) {
                continue;
            }
            Element el = (Element) n;
            if (!"Item".equals(localName(el))) {
                continue;
            }
            BoqPosition pos = new BoqPosition();
            String rno = attr(el, "RNoPart");
            pos.ordinalNo = joinOz(ozPrefix, rno);
            pos.sourceItemId = attr(el, "ID");
            pos.categoryPath = categoryPath;
            pos.positionType = detectPositionType(el);
            pos.quantity = parseDecimal(directChildText(el, "Qty"), BigDecimal.ZERO);
            pos.unit = directChildText(el, "QU");
            pos.unitPrice = parseDecimal(directChildText(el, "UP"), BigDecimal.ZERO);

            Element desc = directChild(el, "Description");
            if (desc != null) {
                Element complete = directChild(desc, "CompleteText");
                Element searchRoot = complete != null ? complete : desc;
                Element outl = firstElementDeep(searchRoot, "OutlineText");
                if (outl == null) {
                    outl = firstElementDeep(searchRoot, "OutlTxt");
                }
                Element detail = firstElementDeep(searchRoot, "DetailTxt");
                pos.shortText = collapseWhitespace(outl != null ? outl.getTextContent() : "");
                pos.longText = collapseParagraphs(detail != null ? detail.getTextContent() : "");
                if (pos.shortText.isEmpty() && !pos.longText.isEmpty()) {
                    pos.shortText = pos.longText.length() > 120 ? pos.longText.substring(0, 120) + "…" : pos.longText;
                }
            }
            out.add(pos);
        }
    }

    private static PositionType detectPositionType(Element itemEl) {
        String provis = directChildText(itemEl, "Provis");
        if (!provis.isEmpty() && !"No".equalsIgnoreCase(provis)) {
            return PositionType.OPTIONAL;
        }
        String alnSer = directChildText(itemEl, "ALNSerNo");
        if (!alnSer.isEmpty()) {
            try {
                int ser = Integer.parseInt(alnSer.trim());
                return ser == 0 ? PositionType.BASE : PositionType.ALTERNATIVE;
            } catch (NumberFormatException ignored) {
                return PositionType.ALTERNATIVE;
            }
        }
        String alnGroup = directChildText(itemEl, "ALNGroupNo");
        if (!alnGroup.isEmpty()) {
            return PositionType.ALTERNATIVE;
        }
        return PositionType.NORMAL;
    }

    @Override
    public byte[] exportBid(
            BoqDocument document,
            Map<String, BigDecimal> unitPricesByOrdinal,
            Map<String, String> bidderTextByOrdinal,
            String producerVersion) throws Exception {
        if (document == null) {
            throw new IllegalArgumentException("BoQ document must not be null");
        }
        String xmlSource = document.rawSource;
        if (xmlSource == null || xmlSource.trim().isEmpty()) {
            xmlSource = buildMinimalX84Xml(document);
        }

        // Switch namespace URI DA80..DA86 -> DA84 while preserving DA XML version (3.1/3.2/3.3)
        String switchedSource = xmlSource.replaceAll("GAEB_DA_XML/DA8[0-9]/", "GAEB_DA_XML/DA84/");
        Document dom = parseXml(switchedSource);
        Element root = dom.getDocumentElement();
        String ns = root.getNamespaceURI();

        // 1. Regenerate <GAEBInfo> per GAEB DA XML 3.3 requirement (never copy imported X83 GAEBInfo)
        regenerateGaebInfo(dom, root, ns, producerVersion);

        // 2. Update <DP> exchange phase to 84
        Element dpEl = firstElementDeep(root, "DP");
        if (dpEl != null) {
            dpEl.setTextContent("84");
        }

        // 3. Inject unit prices (<UP>), calculated totals (<IT>), and bidder notes on every <Item>
        Element boqBody = firstElementDeep(root, "BoQBody");
        if (boqBody != null) {
            updatePricesRecursive(dom, ns, boqBody, "", unitPricesByOrdinal, bidderTextByOrdinal);
        }

        // 4. Serialize cleanly as UTF-8 XML
        TransformerFactory tf = TransformerFactory.newInstance();
        try {
            tf.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            tf.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
        } catch (IllegalArgumentException ignored) {
        }
        Transformer t = tf.newTransformer();
        t.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        t.setOutputProperty(OutputKeys.INDENT, "yes");
        t.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        t.transform(new DOMSource(dom), new StreamResult(baos));
        return baos.toByteArray();
    }

    private void regenerateGaebInfo(Document dom, Element root, String ns, String producerVersion) {
        Element existing = directChild(root, "GAEBInfo");
        String version = "3.3";
        String versDate = "2023-01";
        if (existing != null) {
            String v = directChildText(existing, "Version");
            String vd = directChildText(existing, "VersDate");
            if (!v.isEmpty()) {
                version = v;
            }
            if (!vd.isEmpty()) {
                versDate = vd;
            }
        }
        Element fresh = createEl(dom, ns, "GAEBInfo");
        appendTextEl(dom, ns, fresh, "Version", version);
        appendTextEl(dom, ns, fresh, "VersDate", versDate);
        appendTextEl(dom, ns, fresh, "Date", LocalDate.now().toString());
        appendTextEl(dom, ns, fresh, "Time", LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        appendTextEl(dom, ns, fresh, "ProgSystem", "OpenYaBS " + (producerVersion == null ? "" : producerVersion).trim());
        appendTextEl(dom, ns, fresh, "ProgName", "OpenYaBS Handwerk Plugin");

        if (existing != null) {
            root.replaceChild(fresh, existing);
        } else if (root.getFirstChild() != null) {
            root.insertBefore(fresh, root.getFirstChild());
        } else {
            root.appendChild(fresh);
        }
    }

    private BigDecimal updatePricesRecursive(
            Document dom,
            String ns,
            Element container,
            String ozPrefix,
            Map<String, BigDecimal> unitPrices,
            Map<String, String> bidderTexts) {
        BigDecimal total = BigDecimal.ZERO;
        NodeList children = container.getChildNodes();
        List<Element> elems = new ArrayList<Element>();
        for (int i = 0; i < children.getLength(); i++) {
            if (children.item(i) instanceof Element) {
                elems.add((Element) children.item(i));
            }
        }
        for (Element el : elems) {
            String name = localName(el);
            if ("BoQCtgy".equals(name)) {
                String nextOz = joinOz(ozPrefix, attr(el, "RNoPart"));
                Element subBody = directChild(el, "BoQBody");
                BigDecimal ctgyTotal = subBody == null ? BigDecimal.ZERO
                        : updatePricesRecursive(dom, ns, subBody, nextOz, unitPrices, bidderTexts);
                upsertTotal(dom, ns, el, ctgyTotal);
                total = total.add(ctgyTotal);
            } else if ("Itemlist".equals(name)) {
                total = total.add(updateItemListPrices(dom, ns, el, ozPrefix, unitPrices, bidderTexts));
            } else if ("BoQBody".equals(name)) {
                total = total.add(updatePricesRecursive(dom, ns, el, ozPrefix, unitPrices, bidderTexts));
            }
        }
        return total.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal updateItemListPrices(
            Document dom,
            String ns,
            Element itemList,
            String ozPrefix,
            Map<String, BigDecimal> unitPrices,
            Map<String, String> bidderTexts) {
        BigDecimal sum = BigDecimal.ZERO;
        NodeList children = itemList.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            if (!(children.item(i) instanceof Element)) {
                continue;
            }
            Element itemEl = (Element) children.item(i);
            if (!"Item".equals(localName(itemEl))) {
                continue;
            }
            String oz = joinOz(ozPrefix, attr(itemEl, "RNoPart"));
            BigDecimal qty = parseDecimal(directChildText(itemEl, "Qty"), BigDecimal.ZERO);
            BigDecimal up = unitPrices != null && unitPrices.containsKey(oz)
                    ? unitPrices.get(oz)
                    : parseDecimal(directChildText(itemEl, "UP"), BigDecimal.ZERO);
            if (up == null) {
                up = BigDecimal.ZERO;
            }
            up = up.setScale(2, RoundingMode.HALF_UP);
            BigDecimal it = qty.multiply(up).setScale(2, RoundingMode.HALF_UP);

            upsertChildText(dom, ns, itemEl, "UP", up.toPlainString());
            PositionType ptype = detectPositionType(itemEl);
            if (ptype == PositionType.NORMAL || ptype == PositionType.BASE) {
                upsertChildText(dom, ns, itemEl, "IT", it.toPlainString());
                sum = sum.add(it);
            } else {
                // Optional / alternative positions keep UP; IT is not added to the contract total
                upsertChildText(dom, ns, itemEl, "IT", it.toPlainString());
            }

            if (bidderTexts != null) {
                String bt = bidderTexts.get(oz);
                if (bt != null && !bt.trim().isEmpty()) {
                    Element desc = directChild(itemEl, "Description");
                    if (desc == null) {
                        desc = createEl(dom, ns, "Description");
                        itemEl.appendChild(desc);
                    }
                    Element tc = directChild(desc, "TextComplement");
                    if (tc == null) {
                        tc = createEl(dom, ns, "TextComplement");
                        tc.setAttribute("Kind", "Bidder");
                        desc.appendChild(tc);
                    }
                    upsertChildText(dom, ns, tc, "ComplBodyDec", bt.trim());
                }
            }
        }
        return sum.setScale(2, RoundingMode.HALF_UP);
    }

    private static void upsertTotal(Document dom, String ns, Element ctgy, BigDecimal total) {
        Element totals = directChild(ctgy, "Totals");
        if (totals == null) {
            totals = createEl(dom, ns, "Totals");
            ctgy.appendChild(totals);
        }
        upsertChildText(dom, ns, totals, "Total", total.setScale(2, RoundingMode.HALF_UP).toPlainString());
    }

    private static String buildMinimalX84Xml(BoqDocument doc) {
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        sb.append("<GAEB xmlns=\"http://www.gaeb.de/GAEB_DA_XML/DA84/3.3\">\n");
        sb.append("  <GAEBInfo><Version>3.3</Version><VersDate>2023-01</VersDate></GAEBInfo>\n");
        sb.append("  <PrjInfo><NamePrj>").append(escapeXml(doc.projectName)).append("</NamePrj></PrjInfo>\n");
        sb.append("  <Award>\n");
        sb.append("    <DP>84</DP>\n");
        sb.append("    <AwardInfo><Cur>").append(escapeXml(doc.currency)).append("</Cur></AwardInfo>\n");
        sb.append("    <BoQ ID=\"BOQ1\"><BoQBody><Itemlist>\n");
        int idx = 1;
        for (BoqPosition p : doc.positions) {
            if (p.positionType == PositionType.TEXT) {
                continue;
            }
            sb.append("      <Item ID=\"I").append(idx++).append("\" RNoPart=\"").append(escapeXml(p.ordinalNo)).append("\">\n");
            sb.append("        <Qty>").append(p.quantity == null ? "0" : p.quantity.toPlainString()).append("</Qty>\n");
            sb.append("        <QU>").append(escapeXml(p.unit)).append("</QU>\n");
            sb.append("        <UP>").append(p.unitPrice == null ? "0.00" : p.unitPrice.setScale(2, RoundingMode.HALF_UP).toPlainString()).append("</UP>\n");
            sb.append("        <IT>").append(p.lineTotal().toPlainString()).append("</IT>\n");
            sb.append("        <Description><CompleteText><OutlineText><OutlTxt><TextOutlTxt>")
                    .append(escapeXml(p.shortText))
                    .append("</TextOutlTxt></OutlTxt></OutlineText></CompleteText></Description>\n");
            sb.append("      </Item>\n");
        }
        sb.append("    </Itemlist></BoQBody></BoQ>\n");
        sb.append("  </Award>\n");
        sb.append("</GAEB>\n");
        return sb.toString();
    }

    private static Document parseXml(String xml) throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        dbf.setFeature("http://xml.org/sax/features/external-general-entities", false);
        dbf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        dbf.setXIncludeAware(false);
        dbf.setExpandEntityReferences(false);
        DocumentBuilder db = dbf.newDocumentBuilder();
        String cleaned = xml != null && xml.startsWith("\uFEFF") ? xml.substring(1) : xml;
        return db.parse(new InputSource(new StringReader(cleaned)));
    }

    private static Element createEl(Document dom, String ns, String name) {
        return (ns == null || ns.isEmpty()) ? dom.createElement(name) : dom.createElementNS(ns, name);
    }

    private static void appendTextEl(Document dom, String ns, Element parent, String name, String text) {
        Element el = createEl(dom, ns, name);
        el.setTextContent(text == null ? "" : text);
        parent.appendChild(el);
    }

    private static void upsertChildText(Document dom, String ns, Element parent, String name, String text) {
        Element existing = directChild(parent, name);
        if (existing != null) {
            existing.setTextContent(text);
        } else {
            appendTextEl(dom, ns, parent, name, text);
        }
    }

    private static String extractCategoryTitle(Element ctgy) {
        Element lbl = directChild(ctgy, "LblTx");
        if (lbl != null) {
            return collapseWhitespace(lbl.getTextContent());
        }
        return "";
    }

    private static Element directChild(Element parent, String localName) {
        NodeList nl = parent.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n instanceof Element && localName.equals(localName((Element) n))) {
                return (Element) n;
            }
        }
        return null;
    }

    private static String directChildText(Element parent, String localName) {
        Element c = directChild(parent, localName);
        return c == null ? "" : collapseWhitespace(c.getTextContent());
    }

    private static Element firstElementDeep(Element root, String localName) {
        NodeList nl = root.getElementsByTagNameNS("*", localName);
        if (nl.getLength() > 0 && nl.item(0) instanceof Element) {
            return (Element) nl.item(0);
        }
        nl = root.getElementsByTagName(localName);
        return nl.getLength() > 0 && nl.item(0) instanceof Element ? (Element) nl.item(0) : null;
    }

    private static String firstChildTextDeep(Element root, String localName) {
        Element e = firstElementDeep(root, localName);
        return e == null ? "" : collapseWhitespace(e.getTextContent());
    }

    private static String localName(Element e) {
        return e.getLocalName() != null ? e.getLocalName() : e.getTagName();
    }

    private static String attr(Element e, String name) {
        String v = e.getAttribute(name);
        return v == null ? "" : v.trim();
    }

    private static String joinOz(String prefix, String part) {
        String p = part == null ? "" : part.trim();
        if (prefix == null || prefix.isEmpty()) {
            return p;
        }
        if (p.isEmpty()) {
            return prefix;
        }
        return prefix + "." + p;
    }

    private static BigDecimal parseDecimal(String raw, BigDecimal fallback) {
        if (raw == null || raw.trim().isEmpty()) {
            return fallback;
        }
        try {
            return new BigDecimal(raw.trim().replace(',', '.'));
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private static String collapseWhitespace(String s) {
        return s == null ? "" : s.replaceAll("\\s+", " ").trim();
    }

    private static String collapseParagraphs(String s) {
        if (s == null) {
            return "";
        }
        String[] lines = s.replace("\r\n", "\n").split("\n");
        StringBuilder sb = new StringBuilder();
        for (String line : lines) {
            String trimmed = line.replaceAll("\\s+", " ").trim();
            if (!trimmed.isEmpty()) {
                if (sb.length() > 0) {
                    sb.append("\n");
                }
                sb.append(trimmed);
            }
        }
        return sb.toString();
    }

    private static String stripExt(String fileName) {
        if (fileName == null) {
            return "GAEB";
        }
        int dot = fileName.lastIndexOf('.');
        return dot > 0 ? fileName.substring(0, dot) : fileName;
    }

    private static String escapeXml(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}
