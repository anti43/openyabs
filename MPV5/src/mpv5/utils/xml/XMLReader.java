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
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import mpv5.data.PropertyStore;
import mpv5.db.common.DatabaseObject;
import mpv5.logging.Log;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.filter.ElementFilter;
import org.jdom.input.SAXBuilder;

/**
 *
 * @author anti43
 */
public class XMLReader {

    private Element rootElement = new Element("mpv5");
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
     * Parses a XML document
     * @param xmlfile
     * @return The resulting xml document
     */
    public Document newDoc(File xmlfile)  {
        return createDocument(xmlfile, false);
    }

    /**
     * Parses a XML document
     * @param xmlfile
     * @param validate
     * @return The resulting xml document
     */
    public Document newDoc(File xmlfile, boolean validate) {
        return createDocument(xmlfile, validate);
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

    public ArrayList<DatabaseObject> getObjects(DatabaseObject template) {

        String ident = template.getType();
        ArrayList<DatabaseObject> arrlist = new ArrayList<DatabaseObject>();
        @SuppressWarnings("unchecked")
        List<Element> list = (List<Element>) rootElement.getChild(template.getDbIdentity()).getContent(new ElementFilter());

        for (int i = 0; i < list.size(); i++) {
            if (list.get(i) instanceof Element && list.get(i).getName().equalsIgnoreCase(template.getType())) {
                if (list.get(i).getName().equals(ident) && list.get(i).getAttribute("id") != null) {
                        Element element = list.get(i);
                        DatabaseObject obj = template.clone();
                        obj.parse(toHashTable(element));
                        arrlist.add(obj);
                    }
            }
        }
        return arrlist;
    }

    /**
     * Prints a node to debug out
     * @param nodename
     */
    public void print(String nodename) {
        @SuppressWarnings("unchecked")
        List<Element> list = (List<Element>) rootElement.getChild(nodename).getContent(new ElementFilter());
        for (int i = 0; i < list.size(); i++) {
            Element element = list.get(i);
            Log.Debug(this, element.getName() + ": " + element.getValue());
        }
    }

    /**
     * Reads a node with the given name into a property store object
     * @param nodename
     * @param store
     * @return
     */
    public PropertyStore readInto(String nodename, PropertyStore store) {
        @SuppressWarnings("unchecked")
        List<Element> list = (List<Element>) rootElement.getChild(nodename).getContent(new ElementFilter());
        for (int i = 0; i < list.size(); i++) {
            Element element = list.get(i);
            store.addProperty(element.getName(), element.getValue());
        }

        return store;
    }

    private Document createDocument(File xmlfile, boolean validate) {
        //        SAXBuilder parser = new SAXBuilder("org.apache.xerces.parsers.SAXParser", true);
//        parser.setFeature("http://apache.org/xml/features/validation/schema", true);
//        parser.setProperty("http://apache.org/xml/properties/schema/external-schemaLocation",
//                                       "http://www.w3.org/2001/12/soap-envelope contacts.dtd");
//        Document doc = builder.build(xml);
        SAXBuilder parser = new SAXBuilder(validate);
        try {
            myDocument = parser.build(xmlfile);
            rootElement = myDocument.getRootElement();
           return myDocument;
        } catch (JDOMException jDOMException) {
            Log.Debug(this, jDOMException.getMessage());
        } catch (IOException iOException) {
            Log.Debug(this, iOException.getMessage());
        }
        return null;
    }

    /**
     * Converts a node into a hashtable
     * @param node
     * @return
     */
    public Hashtable<String, Object> toHashTable(Element node) {
        @SuppressWarnings("unchecked")
        List<Element> liste = node.getChildren();
        Hashtable<String, Object> table = new Hashtable<String, Object>();

        for (int i = 0; i < liste.size(); i++) {
            Element element = liste.get(i);
            table.put(element.getName(), element.getValue());
        }

        return table;
    }
}
