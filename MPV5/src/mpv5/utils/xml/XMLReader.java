/*
 *  This file is part of MP by anti43 /GPL.
 *
 *  MP is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  MP is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with MP.  If not, see <http://www.gnu.org/licenses/>.
 */
package mpv5.utils.xml;

import java.io.File;
import java.io.IOException;
import java.util.List;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

/**
 *
 * @author Administrator
 */
public class XMLReader {

    private Element rootElement = new Element("defName");
    private Document myDocument = new Document();

    /**
     * Gets a node with the given name and the "ID" attributevalue
     * @param nodename
     * @param id
     * @return The value of the node
     */
    public String[] getNode(String nodename, String id) {
        @SuppressWarnings("unchecked")
        List<Element> list = (List<Element>) rootElement.getContent();
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i) instanceof Element) {
                if (list.get(i).getName().equals(nodename) && list.get(i).getAttribute("id") != null && list.get(i).getAttribute("id").getValue().equals(id)) {
                    @SuppressWarnings("unchecked")
                    List<Element> liste = list.get(i).getChildren();
                    String[] values = new String[liste.size()];
                    for (int j = 0; j < liste.size(); j++) {
                        Element element = liste.get(j);
                        values[j] = element.getValue();
                    }
                }
            }
        }
        return null;
    }

    /**
     * Creates a ned XML document
     * @param doc
     * @throws JDOMException
     * @throws IOException
     */
    public void newDoc(File doc) throws JDOMException, IOException {
        // Create the root element
        SAXBuilder parser = new SAXBuilder();
        myDocument = parser.build(doc);
        rootElement = myDocument.getRootElement();
    }

    /**
     * Gets an element value
     * @param nodename The name of the node
     * @param attributevalue The ID of the node
     * @param name The name of the element
     * @return The value of the element
     */
    public String getElement(String nodename, String attributevalue, String name) {
        @SuppressWarnings("unchecked")
        List<Element> list = (List<Element>) rootElement.getContent();
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i) instanceof Element) {
                if (list.get(i).getName().equals(nodename) && list.get(i).getAttribute("id") != null && list.get(i).getAttribute("id").getValue().equals(attributevalue)) {
                    return list.get(i).getChild(name).getValue();
                }
            }
        }
        return null;
    }
}
