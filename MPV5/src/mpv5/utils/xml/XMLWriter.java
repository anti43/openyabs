/*
 *  This file is part of MP.
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
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import mpv5.data.PropertyStore;
import mpv5.db.common.DatabaseObject;
import mpv5.globals.Constants;
import mpv5.globals.Messages;
import mpv5.logging.Log;
import mpv5.ui.frames.MPV5View;
import mpv5.utils.files.FileDirectoryHandler;
import org.jdom.DocType;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

/**
 *
 * 
 */
public class XMLWriter {

    public static String rootElementName = Constants.XML_ROOT;
    private Element rootElement = new Element(rootElementName);
    private Document myDocument = new Document();
    public static DocType DEFAULT_DOCTYPE = new DocType(rootElementName, Constants.XML_DOCTYPE_ID, Constants.XML_DOCTYPE_URL);
    private Element defaultSubRoot;

    /**
     * Adds all objects
     * @param dbobjarr
     */
    public void add(ArrayList<DatabaseObject> dbobjarr) {

        if (dbobjarr != null && dbobjarr.size() > 0) {
            DatabaseObject d = dbobjarr.get(0);
            String sident = d.getDbIdentity();
            Element parent = addNode(new Element(sident));
            Log.Debug(this, "Adding root node " + sident);
            for (int i = 0; i < dbobjarr.size(); i++) {
                try {

                    DatabaseObject databaseObject = dbobjarr.get(i);
                    Element ident = new Element(databaseObject.getType());

                    ArrayList<String[]> data = databaseObject.getValues();
                    this.addNode(parent, ident, databaseObject.__getIDS().toString());

                    for (int h = 0; h < data.size(); h++) {
                        if (!data.get(h)[0].equals("IDS")) {
                            this.addElement(parent, ident, databaseObject.__getIDS().toString(), data.get(h)[0].toLowerCase(), data.get(h)[1]);
                        }
                    }
                } catch (Exception ex) {
                    Log.Debug(this, ex.getMessage());
                }
            }
        }
    }

    /**
     * Adds a node to root with the given name
     * @param e
     * @return
     */
    public Element addNode(Element e) {
        rootElement.addContent(e);
        return e;
    }

    /**
     * Adds a node to parent with the given name
     * @param parent
     * @param name The node name
     * @return
     */
    public Element addNode(Element parent, String name) {
        Element elem = new Element(name);
        parent.addContent(elem);
        return elem;
    }

    /**
     * Adds a node to parent with the given name, and an additional attribute "ID" with the attribute value
     * @param parent
     * @param name
     * @param attribute
     * @return
     */
    public Element addNode(Element parent, Element name, String attribute) {
        Element elem = name;
        elem.setAttribute("id", attribute);
        parent.addContent(elem);
        return elem;
    }

//    /**
//     * Adds a node with the given name to root
//     * @param name
//     * @return
//     */
//    public Element addNode(String name) {
//        Element e = new Element(name);
//        rootElement.addContent(e);
//        return e;
//    }
    /**
     * Appends the PropertyStore's data to an existing XML file,
     * or creates one if not existant
     * 
     * @param file
     * @param nodename
     * @param nodeid
     * @param cookie
     */
    public void append(File file, String nodename, String nodeid, PropertyStore cookie) {
        XMLReader reader = new XMLReader();
        try {
            Log.Debug(this, "Reading in " + file);
            myDocument = reader.newDoc(file);
            rootElement = myDocument.getRootElement();
        } catch (Exception ex) {
            newDoc(true);
        }
        parse(nodename, nodeid, cookie);
    }

    public void createOrReplace(File file) throws Exception {
        FileWriter fw = null;
        if (file.exists()) {
            Log.Debug(this, "Updating " + file);
            fw = new FileWriter(file);
        } else {
            file.getParentFile().mkdirs();
            file.createNewFile();
            fw = new FileWriter(file);
        }
        XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
        outputter.output(myDocument, fw);
        MPV5View.addMessage(Messages.FILE_SAVED + file.getPath());
    }

    /**
     * Creates a new XML document with the root element
     * @param withDocTypeDeclaration 
     */
    public void newDoc(boolean withDocTypeDeclaration) {
        // Create the root element
//        rootElement = new Element(rootElementName);
        if (withDocTypeDeclaration) {
            myDocument = new Document(rootElement, (DocType) DEFAULT_DOCTYPE.clone());
        } else {
            myDocument = new Document(rootElement);
        }

    //add an attribute to the root element
//        rootElement.setAttribute(new Attribute("userid", MPV5View.getUser().getID()));
    }

    /**
     * Creates a ned XML document with the sub root element and DocType
     * @param defaultSubRootElementName
     */
    public void newDoc(String defaultSubRootElementName) {
        newDoc(true);
        defaultSubRoot = new Element(defaultSubRootElementName);
        rootElement.addContent(defaultSubRoot);
    }

    public void newDoc(String defaultSubRootElementName, boolean withDocTypeDeclaration) {
        newDoc(withDocTypeDeclaration);
        defaultSubRoot = new Element(defaultSubRootElementName);
        rootElement.addContent(defaultSubRoot);
    }

    public void newDoc() {
        newDoc(true);
    }

    /**
     * Creates a new XML file with the given name
     * 
     * @param filename
     * @return
     */
    public File createFile(String filename) {

        try {
            File f = FileDirectoryHandler.getTempFile(filename, "xml");
            XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
            outputter.output(myDocument, new FileWriter(f));
            return f;
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Adds an element or replaces it if already existing
     * @param parent
     * @param nodename
     * @param attributevalue The ID of the node where this element shall be added
     * @param name The name of the new element
     * @param value The value of the element
     */
    public void addElement(Element parent, Element nodename, String attributevalue, String name, String value) {
        //add some child elements
        Element elem = new Element(name);
        elem.addContent(value);
        @SuppressWarnings("unchecked")
        List<Element> list = (List<Element>) parent.getContent();
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i) instanceof Element) {
                if (list.get(i).getName().equals(nodename.getName()) && list.get(i).getAttribute("id") != null && list.get(i).getAttribute("id").getValue().equals(attributevalue)) {
                    list.get(i).addContent(elem);
                }
            }
        }

    }

    /**
     * Parses a PropertyStore object.
     * Make sure to call newDoc() first!
     * @param nodename
     * @param nodeid
     * @param cookie
     */
    public void parse(String nodename, String nodeid, PropertyStore cookie) {

        Element e = new Element(nodename);
        addNode(defaultSubRoot, e, nodeid);
        Iterator list = cookie.getList().iterator();
        while (list.hasNext()) {
            Object o = list.next();
//            Log.Debug(this,((String[]) o)[0]);
            addElement(defaultSubRoot, e, nodeid, ((String[]) o)[0], ((String[]) o)[1]);
        }
    }
}
