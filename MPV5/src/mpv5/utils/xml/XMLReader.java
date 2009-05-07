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

import javax.xml.bind.JAXBElement.GlobalScope;
import mpv5.data.PropertyStore;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.globals.Messages;
import mpv5.logging.Log;
import mpv5.ui.dialogs.Popup;
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.filter.ContentFilter;
import org.jdom.filter.ElementFilter;
import org.jdom.input.SAXBuilder;

/**
 *
 * @author anti43
 */
public class XMLReader {

    private Element rootElement = new Element(mpv5.globals.Constants.XML_ROOT);
    private Document myDocument = new Document();
    private boolean overwriteExisting = false;

    /**
     *
     * @param name
     * @return
     */
    public Element getSubRootElement(String name) {
        @SuppressWarnings("unchecked")
        List<Element> l = rootElement.getContent(new ElementFilter());

        for (int i = 0; i < l.size(); i++) {
            Element element = l.get(i);
            if (element.getName().equals(name)) {
                return element;
            }
        }

        return new Element(name);
    }

//    /**
//     * Gets a node with the given name and the "ID" attributevalue
//     * @param nodename
//     * @param id
//     * @return The value of the node
//     */
//    public String[] getNode(String nodename, String id) {
//        @SuppressWarnings("unchecked")
//        List<Element> list = (List<Element>) rootElement.getContent();
//        for (int i = 0; i < list.size(); i++) {
//            if (list.get(i) instanceof Element) {
//                if (list.get(i).getName().equals(nodename) && list.get(i).getAttribute("id") != null && list.get(i).getAttribute("id").getValue().equals(id)) {
//                    @SuppressWarnings("unchecked")
//                    List<Element> liste = list.get(i).getChildren();
//                    String[] values = new String[liste.size()];
//                    for (int j = 0; j < liste.size(); j++) {
//                        Element element = liste.get(j);
//                        values[j] = element.getValue();
//                    }
//                }
//            }
//        }
//        return null;
//    }
    /**
     * Parses a XML document
     * @param xmlfile
     * @return The resulting xml document
     * @throws JDOMException
     * @throws IOException 
     */
    public Document newDoc(File xmlfile) throws JDOMException, IOException {
        return createDocument(xmlfile, false);
    }

    /**
     * Parses a XML document
     * @param xmlfile
     * @param validate
     * @return The resulting xml document
     * @throws JDOMException
     * @throws IOException
     */
    public Document newDoc(File xmlfile, boolean validate) throws JDOMException, IOException {
        return createDocument(xmlfile, validate);
    }

    /**
     * Gets an element value
     * @param type
     * @param nodename The name of the node
     * @param attributevalue The ID of the node
     * @param name The name of the element
     * @return The value of the element
     */
    public String getElement(String type, String nodename, String attributevalue, String name) {
        @SuppressWarnings("unchecked")
        List<Element> list = (List<Element>) rootElement.getChild(type).getContent();
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i) instanceof Element) {
                if (list.get(i).getName().equals(nodename) && list.get(i).getAttribute("id") != null && list.get(i).getAttribute("id").getValue().equals(attributevalue)) {
                    return list.get(i).getChild(name).getValue();
                }
            }
        }
        return null;
    }

    /**
     * Tries to parse the xml file into a list of matching database objects.
     * @param <T> 
     * @param template
     * @return
     * @throws Exception
     */
    @SuppressWarnings({"unchecked"})
    public <T extends DatabaseObject> ArrayList<T> getObjects(T template) throws Exception {

        Log.Debug(this, "Looking for: " + template.getDbIdentity());
        String ident = template.getType();
        ArrayList<DatabaseObject> arrlist = new ArrayList<DatabaseObject>();
        @SuppressWarnings("unchecked")
        List<Element> list = (List<Element>) rootElement.getChild(template.getDbIdentity()).getContent(new ElementFilter());

        Log.Debug(this, "Found items: " + list.size());
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i) instanceof Element && list.get(i).getName().equalsIgnoreCase(template.getType())) {
//                Log.Debug(this, "Found item: " + list.get(i).getName());
                if (list.get(i).getName().equals(ident)) {
                    Element element = list.get(i);
                    DatabaseObject obj = template.clone();
                    obj.parse(toHashTable(element));
                    if (isOverwriteExisting() && list.get(i).getAttribute("id") != null) {
                       
                        obj.setIDS(Integer.valueOf(list.get(i).getAttribute("id").getValue()));
                        Log.Debug(this, "Overwriting/updating dataset id " + obj.getDbIdentity() + ": " +obj.__getIDS() );
                    }
                    arrlist.add(obj);
                }
            }
        }
        return (ArrayList<T>) arrlist;
    }

    /**
     * Tries to parse the xml file into a list of database objects. <br/>
     * May contain empty lists
     * @return
     */
    public ArrayList<ArrayList<DatabaseObject>> getObjects() {
        ArrayList<Context> c = Context.getContexts();
        ArrayList<ArrayList<DatabaseObject>> t = new ArrayList<ArrayList<DatabaseObject>>();
        DatabaseObject template = null;
        Context context = null;

        for (int i = 0; i < c.size(); i++) {
            try {
                context = c.get(i);
                template = DatabaseObject.getObject(context);
                t.add(getObjects(template));
            } catch (Exception ignore) {
                Log.Debug(this, "Element of typ: " + context.getDbIdentity() + " not found in this document!");
            }
        }
//
//        for (int i = 0; i < t.size(); i++) {
//            ArrayList<DatabaseObject> arrayList = t.get(i);
//            Log.PrintArray(arrayList);
//        }

        return t;
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
     * @param type 
     * @param nodename
     * @param nodeid 
     * @param store
     * @return
     */
    public PropertyStore readInto(String type, String nodename, String nodeid, PropertyStore store) {
        @SuppressWarnings("unchecked")
        List<Element> list = (List<Element>) rootElement.getChild(type).getContent(new ElementFilter());
        for (int i = 0; i < list.size(); i++) {
            Element element = list.get(i);
            if (element.getName().equals(nodename) && element.getAttribute("id").getValue().equals(nodeid)) {
                @SuppressWarnings("unchecked")
                List<Element> list2 = (List<Element>) element.getContent(new ElementFilter());
                for (int j = 0; j < list2.size(); j++) {
                    Element element1 = list2.get(j);
                    store.addProperty(element1.getName(), element1.getValue());
                }
            }
        }

        return store;
    }

    private Document createDocument(File xmlfile, boolean validate) throws JDOMException, IOException {
        //        SAXBuilder parser = new SAXBuilder("org.apache.xerces.parsers.SAXParser", true);
//        parser.setFeature("http://apache.org/xml/features/validation/schema", true);
//        parser.setProperty("http://apache.org/xml/properties/schema/external-schemaLocation",
//                                       "http://www.w3.org/2001/12/soap-envelope contacts.dtd");
//        Document doc = builder.build(xml);
        SAXBuilder parser = new SAXBuilder(validate);
//        try {
        myDocument = parser.build(xmlfile);
        rootElement = myDocument.getRootElement();
        Log.Debug(this, "Document validated: " + xmlfile);
        return myDocument;
//        } catch (Exception jDOMException) {
//            Log.Debug(this, jDOMException.getMessage());
//            Popup.error("", jDOMException);
//        }
//        return null;
    }

    /**
     * Converts a node into a hashtable<br/>
     * {name, value}, will not return any attributes
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

    /**
     * Reads a node into a dynamic String array
     * {name, vale, attribute1, attribute2.. 5}
     * @param node
     * @return
     */
    public String[][] toArray(Element node) {
        @SuppressWarnings("unchecked")
        List<Element> liste = node.getContent(new ElementFilter());
        Log.Debug(this, liste.size() + " elements found in " + node);
        String[][] table = new String[liste.size()][5];

        for (int i = 0; i < liste.size(); i++) {
            Element element = liste.get(i);
            table[i][0] = element.getName();
            table[i][1] = element.getValue();
            @SuppressWarnings("unchecked")
            List<Attribute> atts = element.getAttributes();

            for (int j = 2; j < atts.size() + 2; j++) {
                Attribute a = atts.get(j - 2);
                table[i][j] = a.getValue();
            }
        }
        return table;
    }

    /**
     * @return the overwriteExisting
     */
    public boolean isOverwriteExisting() {
        return overwriteExisting;
    }

    /**
     * @param overwriteExisting the overwriteExisting to set
     */
    public void setOverwriteExisting(boolean overwriteExisting) {
        this.overwriteExisting = overwriteExisting;
    }
}
