/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mpv5.utils.oasis;

/**
 * File: ExampleCreateWriterDoc.java
 *
 * Initial version: Aug 28, 2007
 *
 * Modifications:
 */
import com.sun.star.lang.IllegalArgumentException;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;

import com.sun.star.awt.XWindow;
import com.sun.star.beans.Property;
import com.sun.star.beans.PropertyValue;
import com.sun.star.beans.PropertyVetoException;
import com.sun.star.beans.UnknownPropertyException;
import com.sun.star.beans.XPropertySet;
import com.sun.star.beans.XPropertySetInfo;
import com.sun.star.comp.helper.Bootstrap;
import com.sun.star.comp.helper.BootstrapException;
import com.sun.star.container.NoSuchElementException;
import com.sun.star.container.XNameAccess;
import com.sun.star.frame.XComponentLoader;
import com.sun.star.frame.XController;
import com.sun.star.frame.XFrame;
import com.sun.star.frame.XModel;
import com.sun.star.frame.XStorable;
import com.sun.star.lang.EventObject;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XEventListener;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.style.XStyleFamiliesSupplier;
import com.sun.star.text.XBookmarksSupplier;
import com.sun.star.text.XText;
import com.sun.star.text.XTextContent;
import com.sun.star.text.XTextCursor;
import com.sun.star.text.XTextFieldsSupplier;
import com.sun.star.text.XTextRange;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;
import com.sun.star.util.XRefreshable;
import com.sun.star.xml.dom.XDocument;
import java.util.Hashtable;
import mpv5.logging.Log;
import mpv5.utils.reflection.ClasspathTools;

/**
 * A demo program that creates and OpenOffice Writer document.
 * <p>
 * @author rdg (robg123)
 */
public class CreateWriterDoc {

    private static XComponentContext context;
    private static final int REPLACE_ALL = 0;
    private static final int ADD_SENTENCE = 1;
    private static final int ADD_PARAGRAPH = 2;
    private static final String MS_WORD_SAVE_FORMAT = "MS Word 97";
    private static final String PDF_SAVE_FORMAT = "writer_pdf_Export";
    private Object desktop;
    private XComponentLoader loader;

    /** Constructor. */
    public CreateWriterDoc() {
    }

    /**
     * Create an OpenOffice Writer document and make it visible on the
     * screen. Call the <code>saveDoc</code> method to save the document
     * to a file.
     * <p>
     * @return
     * The 'handle' to the document.
     * @param docTitle
     * The title of the document. This will show up in the title
     * bar of the document window.
     * @param templateFile
     * The absolute path of a template file. If not null, the newly
     * created file will initially be an exact duplicate of this file.
     * Typically, the template file contains bookmarks where this
     * application can insert text, images, charts, etc. (The template
     * can be created using OpenOffice Writer). If this parameter is
     * null, the new document will be blank.
     * @param isUserEditable
     * True = allow user to manipulate/edit the Writer document window.
     * False = make the Writer document window uneditable.
     */
    public void createDoc(
            String docTitle,
            String templateFile,
            boolean isUserEditable) throws java.lang.Exception {

        XComponent document = null;

        try {
// Get remote service manager. We only need one instance regardless
// of the number of documents we create.
            if (context == null) {
                try {
                    context = Bootstrap.bootstrap();
                } catch (BootstrapException e) {
                    throw new java.lang.Exception(e);
                }
            }
            XMultiComponentFactory serviceManager = context.getServiceManager();

// Retrieve the Desktop object and get its XComponentLoader.
            desktop = serviceManager.createInstanceWithContext(
                    "com.sun.star.frame.Desktop", context);
            loader = (XComponentLoader) UnoRuntime.queryInterface(
                    XComponentLoader.class, desktop);

// Define general document properties (see
// com.sun.star.document.MediaDescriptor for the possibilities).
            ArrayList<PropertyValue> props = new ArrayList<PropertyValue>();
            PropertyValue p = null;
            if (templateFile != null) {
                p = new PropertyValue();
                p.Name = "AsTemplate";
                p.Value = new Boolean(true);
                props.add(p);
            }
            if ((docTitle != null) && (docTitle.length() > 0)) {
                p = new PropertyValue();
                p.Name = "DocumentTitle";
                p.Value = docTitle;
                props.add(p);
            }
// The following code will make the document initially invisible.
// Do this if you will be placing lots of data into it, so the
// user doesn't have to watch it being constructed. You can bring
// it into view later.
/*
            p = new PropertyValue();
            p.Name = "Hidden";
            p.Value = new Boolean(true);
            props.add(p);
             */

            Hashtable data = new Hashtable();
            data.put("product1", "andreas");
            data.put("product2", "weber");
            data.put("product3", "strasse");

            PropertyValue[] properties = new PropertyValue[props.size()];
//            props.toArray(properties);

           
            showDoc(document, templateFile, properties);
            fillATemplate(document, templateFile, data);

        } catch (com.sun.star.uno.Exception e) {
            throw new java.lang.Exception(e);
        }

    }

//    create a small hashtable that simulates a rowset with columns
//        Hashtable recipient = new Hashtable();
//
//        recipient.put("firstname", data[0]);
//        recipient.put("lastname", data[1]);
//        recipient.put("street", data[2]);
//        recipient.put("zip", data[3]);
//        recipient.put("city", data[4]);
    public XComponent fillATemplate(XComponent doc, String templateUrl, Hashtable data) throws Exception {

//
        // load template with User fields and bookmark
//        XComponent xTemplateComponent = newDocComponentFromTemplate(templateUrl);


        // get XTextFieldsSupplier interfaces from document component
        XTextFieldsSupplier xTextFieldsSupplier = (XTextFieldsSupplier) UnoRuntime.queryInterface(
                XTextFieldsSupplier.class, doc);

        // access the TextFields and the TextFieldMasters collections
        XNameAccess xNamedFieldMasters = xTextFieldsSupplier.getTextFieldMasters();

        // iterate over hashtable and insert values into field masters
        java.util.Enumeration keys = data.keys();
        String key = null;
        while (keys.hasMoreElements()) {

            try {
                // get column name
                key = (String) keys.nextElement();

                // access corresponding field master
                Object fieldMaster = xNamedFieldMasters.getByName(
                        "com.sun.star.text.FieldMaster.User." + key);

                // query the XPropertySet interface, we need to set the Content property
                XPropertySet xPropertySet = (XPropertySet) UnoRuntime.queryInterface(
                        XPropertySet.class, fieldMaster);

                // insert the column value into field master
                xPropertySet.setPropertyValue("Content", data.get(key));
            } catch (Exception noSuchElementException) {
                Log.Debug(this, "No such element: " + key);
            }
        }

        return doc;

    }

    protected XComponent newDocComponentFromTemplate(String loadUrl)
            throws java.lang.Exception {
        // retrieve the Desktop object, we need its XComponentLoader
        XComponentLoader xComponentLoader = (XComponentLoader) UnoRuntime.queryInterface(XComponentLoader.class, desktop);

        // define load properties according to com.sun.star.document.MediaDescriptor
        // the boolean property AsTemplate tells the office to create a new document
        // from the given file
        PropertyValue[] loadProps = new PropertyValue[2];
        loadProps[0] = new PropertyValue();
        loadProps[0].Name = "AsTemplate";
        loadProps[0].Value = new Boolean(true);
        loadProps[1] = new PropertyValue();
        loadProps[1].Name = "Hidden";
        loadProps[1].Value = new Boolean(true);
        // load
        return xComponentLoader.loadComponentFromURL(loadUrl, "_blank",
                0, loadProps);
    }

    /**
     * Save a given document to a specified file.
     * <p>
     * @param document
     * The 'handle' to the document.
     * @param saveFile
     * The full path of the file in which to save the document.
     * If null, this method will do nothing.
     * @param overwrite
     * If true, and the file already exists, it will be
     * overwritten. Otherwise an exception will be thrown.
     */
    public void saveDoc(XDocument document, String saveFile, boolean overwrite)
            throws java.lang.Exception, MalformedURLException {

        try {
            if ((saveFile == null) || (saveFile.trim().length() == 0)) {
                return;
            }
            if (!overwrite) {
                File f = new File(saveFile);
                if (f.exists()) {
                    throw new java.lang.Exception(
                            "File " + saveFile + " already exists; overwriting disabled.");
                }
            }

            String saveFileURL = filePathToURL(saveFile);
            XStorable storable = (XStorable) UnoRuntime.queryInterface(
                    XStorable.class, document);

            PropertyValue[] properties = new PropertyValue[1];
            PropertyValue p = new PropertyValue();
            p.Name = "FilterName";
            p.Value = PDF_SAVE_FORMAT;
            properties[0] = p;

            storable.storeAsURL(saveFileURL, properties);

        } catch (com.sun.star.uno.Exception e) {
            throw new java.lang.Exception(e);
        }
    }

    /** Get a bookmarked field's <code>TextRange</code>. */
    private XTextRange getBookmarkTextRange(
            XBookmarksSupplier bookmarksSupplier, String bookmarkName)
            throws java.lang.Exception {

        XTextRange textRange = null;
        try {
// Get the collection of bookmarks in the document.
            XNameAccess bookmarkNames = bookmarksSupplier.getBookmarks();

// Find the bookmark having the given name.
            Object bmk = null;
            try {
                bmk = bookmarkNames.getByName(bookmarkName);
            } catch (NoSuchElementException e) {
            }
            if (bmk == null) {
                throw new java.lang.Exception(
                        "Cannot find bookmark '" + bookmarkName + "'");
            }

// Get the bookmark's XTextContent. It allows objects to be
// inserted into a text and provides their location within a
// text after insertion.

            XTextContent bookmarkContent = (XTextContent) UnoRuntime.queryInterface(XTextContent.class, bmk);

// Get the bookmark's XTextRange. It describes the object's
// position within the text and provides access to the text.

            textRange = bookmarkContent.getAnchor();

        } catch (com.sun.star.uno.Exception e) {
            throw new java.lang.Exception(e);
        }

        return textRange;
    }

    /** Convert a file path to URL format. */
    private String filePathToURL(String file) {
        File f = new File(file);
        StringBuffer sb = new StringBuffer("file:///");
        try {
            sb.append(f.getCanonicalPath().replace('\\', '/'));
        } catch (IOException e) {
        }
        return sb.toString();
    }

    private XComponent showDoc(XComponent document, String templateFile, PropertyValue[] properties) throws com.sun.star.io.IOException, IllegalArgumentException {
        // Create the document
// (see com.sun.star.frame.XComponentLoader for argument details).

        document = null;

        if (templateFile != null) {
// Create a new document that is a duplicate of the template.
            Log.Debug(this, "Try to open file: " + templateFile);
            String templateFileURL = filePathToURL(templateFile);
            document = loader.loadComponentFromURL(
                    templateFileURL, // URL of templateFile.
                    "_blank", // Target frame name (_blank creates new frame).
                    0, // Search flags.
                    properties); // Document attributes.
        } else {
// Create a new document.
            String docType = "swriter";
            String newDocURL = "private:factory/" + docType;
            document = loader.loadComponentFromURL(
                    newDocURL, // URL for creating a new document.
                    "_blank", // Target frame name (_blank creates a new frame).
                    0, // Search flags.
                    properties); // Document properties.
        }



        XRefreshable xRefreshable = (XRefreshable) UnoRuntime.queryInterface(
                XRefreshable.class, document);
        xRefreshable.refresh();

// Fetch field and style information.
// NOTE: I have not found a use for these yet.
//
//            XTextFieldsSupplier fieldSupplier = (XTextFieldsSupplier) UnoRuntime.queryInterface(XTextFieldsSupplier.class, document);
//            XStyleFamiliesSupplier styleFamiliesSupplier = (XStyleFamiliesSupplier) UnoRuntime.queryInterface(XStyleFamiliesSupplier.class, document);
//
//// Get the document's bookmark name supplier.
//
//            XBookmarksSupplier bookmarksSupplier = (XBookmarksSupplier) UnoRuntime.queryInterface(XBookmarksSupplier.class, document);

// Add a document displose listener so we can know if the user
// manually exits the Writer window.

        document.addEventListener(new XEventListener() {

            public void disposing(EventObject e) {
// TBD
                }
        });

// Control whether the user can edit the displayed document.
//            if (!isUserEditable) {
//                XModel model = (XModel) UnoRuntime.queryInterface(
//                        XModel.class, document);
//                XController c = model.getCurrentController();
//                XFrame frame = c.getFrame();
//                XWindow window = frame.getContainerWindow();
//                window.setEnable(isUserEditable);
//            }
        return document;
    }

    /**
     * Dump a given <code>XPropertySet</code>. This comes in handy because
     * it's otherwise hard to know all the properties available within a
     * given UNO component's property set. You can use UnoRuntime.queryInterface
     * to fetch the component's property set, then call this method to dump it.
     */
    private void showProperties(String title, XPropertySet pSet) {
        System.out.println("\n" + title + "\n");
        XPropertySetInfo info = pSet.getPropertySetInfo();
        Property[] props = info.getProperties();
        if (props != null) {
            try {
                for (int i = 0; i < props.length; i++) {
                    Property p = props[i];
                    String value = "<null>";
                    try {
                        Object o = (Object) pSet.getPropertyValue(p.Name);
                        if (o != null) {
                            value = o.toString();
                        }
                    } catch (java.lang.Exception e) {
                        value = "<null>";
                    }
                    System.out.println(
                            " Name = " + p.Name +
                            ", Type = " + p.Type +
                            ", Value = " + value);
                }
            } catch (java.lang.Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * @param args
     */
    public static void main(String[] args) throws Exception {
        CreateWriterDoc doc = new CreateWriterDoc();
        ClasspathTools.addPath("/opt/openoffice.org3/");
        ClasspathTools.addPath("/opt/openoffice.org3/program");

        try {
            doc.createDoc("My Document Title", "/home/anti/MP-4.00/Vorlagen/Vorlagen/angebot.odt", true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}