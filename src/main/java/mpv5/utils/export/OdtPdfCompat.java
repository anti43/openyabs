/*
 *  This file is part of YaBS.
 *
 *      YaBS is free software: you can redistribute it and/or modify
 *      it under the terms of the GNU General Public License as published by
 *      the Free Software Foundation, either version 3 of the License, or
 *      (at your option) any later version.
 *
 *      YaBS is distributed in the hope that it will be useful,
 *      but WITHOUT ANY WARRANTY; without even the implied warranty of
 *      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *      GNU General Public License for more details.
 *
 *      You should have received a copy of the GNU General Public License
 *      along with YaBS.  If not, see <http://www.gnu.org/licenses/>.
 */
package mpv5.utils.export;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Prepares a filled ODT document for the PDF converter (xdocreport / iText 2),
 * which cannot handle everything LibreOffice writes. Only the copy that is
 * converted is changed, never the template.
 * <p>
 * A drawing shape with text (rectangle, ellipse, custom shape ...) inside a
 * table cell made the conversion fail with "Insertion of illegal Element: 12"
 * (a paragraph inside a paragraph of the cell), and the result was an empty,
 * unreadable PDF. The converter does not draw text in shapes at all (also
 * not in text frames there), so the text of such a shape is put into the
 * paragraph of the cell (its paragraphs as lines): the text is in the PDF, the
 * outline of the shape is not.
 */
public final class OdtPdfCompat {

    public static final String DRAW_NS = "urn:oasis:names:tc:opendocument:xmlns:drawing:1.0";
    public static final String TEXT_NS = "urn:oasis:names:tc:opendocument:xmlns:text:1.0";
    public static final String TABLE_NS = "urn:oasis:names:tc:opendocument:xmlns:table:1.0";
    private static final Set<String> SHAPES = new HashSet<String>(Arrays.asList(
            "rect", "ellipse", "circle", "custom-shape", "polygon", "polyline", "path", "regular-polygon", "caption", "measure"));
    private static final Set<String> TEXT = new HashSet<String>(Arrays.asList("p", "h", "list", "section"));

    private OdtPdfCompat() {
    }

    /**
     * @param dom content.xml or styles.xml of the document
     * @return the number of shapes replaced by their text
     */
    public static int shapesInTablesToFrames(Document dom) {
        if (dom == null) {
            return 0;
        }
        List<Element> shapes = new ArrayList<Element>();
        NodeList all = dom.getElementsByTagNameNS(DRAW_NS, "*");
        for (int i = 0; i < all.getLength(); i++) {
            Element e = (Element) all.item(i);
            if (SHAPES.contains(e.getLocalName()) && hasText(e) && inTableCell(e)) {
                shapes.add(e);
            }
        }
        for (Element shape : shapes) {
            Node parent = shape.getParentNode();
            boolean inParagraph = TEXT_NS.equals(parent.getNamespaceURI())
                    && ("p".equals(parent.getLocalName()) || "h".equals(parent.getLocalName()) || "span".equals(parent.getLocalName()));
            //the text of the shape, its paragraphs as spans with line breaks between them
            List<Node> inline = new ArrayList<Node>();
            for (Node c = shape.getFirstChild(); c != null; c = c.getNextSibling()) {
                if (c instanceof Element && TEXT_NS.equals(c.getNamespaceURI()) && TEXT.contains(c.getLocalName())) {
                    collect((Element) c, dom, inline);
                }
            }
            Node target = parent;
            Node before = shape;
            if (!inParagraph) {
                //directly in the cell (or a frame): a paragraph of its own
                Element para = dom.createElementNS(TEXT_NS, "text:p");
                parent.insertBefore(para, shape);
                target = para;
                before = null;
            }
            for (Node n : inline) {
                target.insertBefore(n, before);
            }
            parent.removeChild(shape);
        }
        return shapes.size();
    }

    /**
     * @param text a text:p, text:h, text:list ... of the shape
     * @param out the inline nodes: a span per paragraph, line breaks between
     */
    private static void collect(Element text, Document dom, List<Node> out) {
        if ("p".equals(text.getLocalName()) || "h".equals(text.getLocalName())) {
            if (!out.isEmpty()) {
                out.add(dom.createElementNS(TEXT_NS, "text:line-break"));
            }
            Element span = dom.createElementNS(TEXT_NS, "text:span");
            String style = text.getAttributeNS(TEXT_NS, "style-name");
            if (style != null && !style.isEmpty()) {
                span.setAttributeNS(TEXT_NS, "text:style-name", style);
            }
            List<Node> children = new ArrayList<Node>();
            for (Node c = text.getFirstChild(); c != null; c = c.getNextSibling()) {
                children.add(c);
            }
            for (Node c : children) {
                //no further shapes or frames inside the line
                if (!(c instanceof Element && DRAW_NS.equals(c.getNamespaceURI()))) {
                    span.appendChild(c);
                }
            }
            out.add(span);
        } else {
            //list, section: their paragraphs one after the other
            for (Node c = text.getFirstChild(); c != null; c = c.getNextSibling()) {
                if (c instanceof Element) {
                    Element e = (Element) c;
                    if (TEXT_NS.equals(e.getNamespaceURI())) {
                        if (TEXT.contains(e.getLocalName())) {
                            collect(e, dom, out);
                        } else {
                            //list-item, list-header
                            collect(e, dom, out);
                        }
                    }
                }
            }
        }
    }

    private static boolean hasText(Element shape) {
        for (Node c = shape.getFirstChild(); c != null; c = c.getNextSibling()) {
            if (c instanceof Element && TEXT_NS.equals(c.getNamespaceURI()) && TEXT.contains(c.getLocalName())) {
                return true;
            }
        }
        return false;
    }

    private static boolean inTableCell(Node n) {
        for (Node p = n.getParentNode(); p != null; p = p.getParentNode()) {
            if (TABLE_NS.equals(p.getNamespaceURI()) && ("table-cell".equals(p.getLocalName()) || "covered-table-cell".equals(p.getLocalName()))) {
                return true;
            }
        }
        return false;
    }
}
