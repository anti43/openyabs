
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
import mpv5.db.common.NodataFoundException;

import mpv5.globals.Constants;
import mpv5.globals.Messages;

import mpv5.logging.Log;


import mpv5.usermanagement.MPSecurityManager;

import mpv5.utils.files.FileDirectoryHandler;

import org.jdom.DocType;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;
import java.io.FileWriter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 *
 */
public class XMLWriter {

    public static final String rootElementName = Constants.XML_ROOT;
    public static DocType DEFAULT_DOCTYPE = new DocType(rootElementName);
    private Element rootElement = new Element(rootElementName);
    private Document myDocument = new Document();
    private Element defaultSubRoot;

    /**
     * Exports all data in the given context to XML and shows a file save dialog for the created file.
     * @param c
     */
    public static void export(Context c) {
        if (mpv5.usermanagement.MPSecurityManager.check(c, MPSecurityManager.EXPORT)) {
            try {
                XMLWriter xmlw = new XMLWriter();

                xmlw.newDoc(true);

                String name = c.getDbIdentity();
                ArrayList<DatabaseObject> dbobjarr = DatabaseObject.getObjects(c);

                xmlw.add(dbobjarr);
                mpv5.YabsViewProxy.instance().showFilesaveDialogFor(xmlw.createFile(name));
            } catch (NodataFoundException ex) {
                Log.Debug(XMLWriter.class, ex);
            }
        }
    }

    /**
     * Adds all objects
     * @param dbobjarr
     */
    public void add(ArrayList<DatabaseObject> dbobjarr) {
        if ((dbobjarr != null) && (dbobjarr.size() > 0)) {
            DatabaseObject d = dbobjarr.get(0);
            String sident = d.getDbIdentity();
            Element parent = addNode(new Element(sident));

            Log.Debug(this, "Adding root node " + sident);

            for (int i = 0; i < dbobjarr.size(); i++) {
                try {
                    DatabaseObject databaseObject = dbobjarr.get(i);
                    Element ident = new Element(databaseObject.getType());
                    List<String[]> data = databaseObject.getValues();

                    this.addNode(parent, ident, databaseObject.__getIDS().toString());

                    for (int h = 0; h < data.size(); h++) {
                        if (!data.get(h)[0].equals("IDS")) {
                            this.addElement(parent, ident, databaseObject.__getIDS().toString(),
                                    data.get(h)[0].toLowerCase(), data.get(h)[1]);
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
     * @return the generated element
     */
    public Element addNode(Element parent, Element name, String attribute) {
        Element elem = name;

        elem.setAttribute("id", attribute);
        @SuppressWarnings("unchecked")
        List<Element> list = (List<Element>) parent.getContent();

        for (int i = 0; i < list.size(); i++) {
            if (list.get(i) instanceof Element) {
                if (list.get(i).getName().equals(elem.getName()) && (list.get(i).getAttribute("id") != null)
                        && list.get(i).getAttribute("id").getValue().equals(attribute)) {
                    Log.Debug(this, "Node exists: " + elem.getName() + ": " + attribute);

                    return null;
                }
            }
        }

        parent.addContent(elem);

        return elem;
    }

//  /**
//   * Adds a node with the given name to root
//   * @param name
//   * @return
//   */
//  public Element addNode(String name) {
//      Element e = new Element(name);
//      rootElement.addContent(e);
//      return e;
//  }
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

        Log.Debug(this, "Writing Document: " + file + " using encoding: " + fw.getEncoding());
        XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());

        outputter.output(myDocument, fw);
        mpv5.YabsViewProxy.instance().addMessage(Messages.FILE_SAVED + file.getPath());
        Log.Debug(this, Messages.FILE_SAVED + file.getPath());
    }

    /**
     * Creates a new XML document with the root element
     * @param withDocTypeDeclaration
     */
    public void newDoc(boolean withDocTypeDeclaration) {
        if (withDocTypeDeclaration) {
            myDocument = new Document(rootElement, (DocType) DEFAULT_DOCTYPE.clone());
            Log.Debug(this, "Using doctype: " + DEFAULT_DOCTYPE);
        } else {
            myDocument = new Document(rootElement);
        }
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
            FileWriter fw = new FileWriter(f);
            Format format = Format.getPrettyFormat();
            format.setEncoding(fw.getEncoding());
            XMLOutputter outputter = new XMLOutputter(format);

            Log.Debug(this, "Writing Document: " + f + " using encoding: " + fw.getEncoding());
            outputter.output(myDocument, new FileWriter(f));

            return f;
        } catch (java.io.IOException e) {
            Log.Debug(this,
                    e);
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
     * @return
     */
    public boolean addElement(Element parent, Element nodename, String attributevalue, String name, String value) {

        // add some child elements
        Element elem = new Element(name);

        elem.addContent(value);
        @SuppressWarnings("unchecked")
        List<Element> list = (List<Element>) parent.getContent();

        for (int i = 0; i < list.size(); i++) {
            if (list.get(i) instanceof Element) {
                if (list.get(i).getName().equals(nodename.getName()) && (list.get(i).getAttribute("id") != null)
                        && list.get(i).getAttribute("id").getValue().equals(attributevalue)) {
                    list.get(i).addContent(elem);

                    return true;    // only add once
                }
            }
        }

        return false;    // no matching parent node found
    }

    /**
     * Parses a PropertyStore object. "nodeid" properties are ignored.
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

//          Log.Debug(this,((String[]) o)[0]);
            addElement(defaultSubRoot, e, nodeid, ((String[]) o)[0], ((String[]) o)[1]);
        }
    }

    /**
     * Parses a PropertyStore list. "nodeid" property is mandatory!
     * Make sure to call newDoc() first.
     * @param nodename
     * @param cookie
     */
    public void parse(String nodename, List<PropertyStore> cookie) {
        for (int i = 0; i < cookie.size(); i++) {
            PropertyStore propertyStore = cookie.get(i);
            Element e = new Element(nodename);
            Element n = addNode(defaultSubRoot, e, propertyStore.getProperty("nodeid"));

            if (n != null) {
                Iterator list = propertyStore.getList().iterator();

                while (list.hasNext()) {
                    Object o = list.next();

                    if (!((String[]) o)[0].equals("nodeid")) {

//                      addElement(defaultSubRoot, e, propertyStore.getProperty("nodeid"), ((String[]) o)[0], ((String[]) o)[1]);
                        Element en = new Element(((String[]) o)[0]);

                        en.addContent(((String[]) o)[1]);
                        n.addContent(en);
                    }
                }
            }
        }
    }
}


//~ Formatted by Jindent --- http://www.jindent.com

