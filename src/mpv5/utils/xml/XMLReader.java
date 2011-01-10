
/*
 *  This file is part of YaBS.
 *
 *  YaBS is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  YaBS is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with YaBS.  If not, see <http://www.gnu.org/licenses/>.
 */
package mpv5.utils.xml;

//~--- non-JDK imports --------------------------------------------------------
import mpv5.data.PropertyStore;

import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.db.objects.Contact;

import mpv5.logging.Log;

import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.filter.ElementFilter;
import org.jdom.input.SAXBuilder;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

/**
 *
 *
 */
public class XMLReader {

    private Element rootElement = new Element(mpv5.globals.Constants.XML_ROOT);
    private boolean overwriteExisting = false;
    private Document myDocument = new Document();

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
     * Parses a XML document. If validate is TRUE, the file will need to have a valid DOCTYPE declaration.
     * The <?xml version="1.0" encoding="UTF-8"?> tag MUST be on the first line!
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
                if (list.get(i).getName().equals(nodename) && (list.get(i).getAttribute("id") != null)
                        && list.get(i).getAttribute("id").getValue().equals(attributevalue)) {
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
    public synchronized <T extends DatabaseObject> ArrayList<T> getObjects(T template) throws Exception {
        Log.Debug(this, "Looking for: " + template.getDbIdentity());

        String ident = template.getType();
        ArrayList<DatabaseObject> arrlist = new ArrayList<DatabaseObject>();
        @SuppressWarnings("unchecked")
        List<Element> list =
                (List<Element>) rootElement.getChild(template.getDbIdentity()).getContent(new ElementFilter());

        Log.Debug(this, "Found items: " + list.size());

        for (int i = 0; i < list.size(); i++) {
            if ((list.get(i) instanceof Element) && list.get(i).getName().equalsIgnoreCase(template.getType())) {

//              Log.Debug(this, "Found item: " + list.get(i).getName());
                if (list.get(i).getName().equals(ident)) {
                    Element element = list.get(i);
                    DatabaseObject obj = template.clone();

                    obj.parse(toHashTable(element));

                    if (isOverwriteExisting() && (list.get(i).getAttribute("id") != null)) {
                        obj.setIDS(Integer.valueOf(list.get(i).getAttribute("id").getValue()));
                        Log.Debug(this,
                                "Overwriting/updating dataset id " + obj.getDbIdentity() + ": " + obj.__getIDS());
                    } else {
                        obj.ensureUniqueness();
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
    public synchronized ArrayList<ArrayList<DatabaseObject>> getObjects() {
        ArrayList<Context> c = Context.getImportableContexts();
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

        return t;
    }

    /**
     * Prints a node to debug out
     * @param nodename
     */
    public void print(String nodename) {
        @SuppressWarnings("unchecked")
        List<Element> list =
                (List<Element>) rootElement.getChild(nodename).getContent(new ElementFilter());

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
    public synchronized PropertyStore readInto(String type, String nodename, String nodeid, PropertyStore store) {
        @SuppressWarnings("unchecked")
        List<Element> list =
                (List<Element>) rootElement.getChild(type).getContent(new ElementFilter());

        for (int i = 0; i < list.size(); i++) {
            Element element = list.get(i);

            if (element.getName().equals(nodename) && element.getAttribute("id").getValue().equals(nodeid)) {
                @SuppressWarnings("unchecked")
                List<Element> list2 =
                        (List<Element>) element.getContent(new ElementFilter());

                for (int j = 0; j < list2.size(); j++) {
                    Element element1 = list2.get(j);

                    store.addProperty(element1.getName(), element1.getValue());
                }
            }
        }

        return store;
    }

    /**
     * Reads all nodes with the given name into a property store list, with additional property "nodeid"
     * @param type
     * @param nodename
     * @return
     */
    public synchronized List<PropertyStore> readInto(String type, String nodename) {
        @SuppressWarnings("unchecked")
        List<Element> list =
                (List<Element>) rootElement.getChild(type).getContent(new ElementFilter());
        List<PropertyStore> plist = new Vector<PropertyStore>();

        for (int i = 0; i < list.size(); i++) {
            Element element = list.get(i);

            if (element.getName().equals(nodename)) {
                PropertyStore st = new PropertyStore();

                st.addProperty("nodeid", element.getAttribute("id").getValue());

                try {
                    st.addProperty("nodename", element.getAttribute("name").getValue());
                } catch (Exception e) {
                    // name not mandatory
                }
                @SuppressWarnings("unchecked")
                List<Element> list2 =
                        (List<Element>) element.getContent(new ElementFilter());

                for (int j = 0; j < list2.size(); j++) {
                    Element element1 = list2.get(j);

                    st.addProperty(element1.getName(), element1.getValue());
                }

                plist.add(st);
            }
        }

        return plist;
    }

    private Document createDocument(File xmlfile, boolean validate) throws JDOMException, IOException {

        SAXBuilder parser = new SAXBuilder(validate);
        FileReader fis = new FileReader(xmlfile);
        Log.Debug(this, "Reading Document: " + xmlfile + " using encoding: " + fis.getEncoding());

        myDocument = parser.build(fis);
        rootElement = myDocument.getRootElement();

        if (validate) {
            Log.Debug(this, "Document validated: " + xmlfile);
        }

        return myDocument;
    }

    /**
     * Converts a node into a hashtable<br/>
     * {name, value}, will not return any attributes
     * @param node
     * @return
     */
    public synchronized Hashtable<String, Object> toHashTable(Element node) {
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


//~ Formatted by Jindent --- http://www.jindent.com

