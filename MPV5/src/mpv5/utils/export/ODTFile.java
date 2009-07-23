/*
 *  This file is part of MP.
 *
 *      MP is free software: you can redistribute it and/or modify
 *      it under the terms of the GNU General Public License as published by
 *      the Free Software Foundation, either version 3 of the License, or
 *      (at your option) any later version.
 *
 *      MP is distributed in the hope that it will be useful,
 *      but WITHOUT ANY WARRANTY; without even the implied warranty of
 *      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *      GNU General Public License for more details.
 *
 *      You should have received a copy of the GNU General Public License
 *      along with MP.  If not, see <http://www.gnu.org/licenses/>.
 */
package mpv5.utils.export;

import ag.ion.bion.officelayer.application.IOfficeApplication;
import ag.ion.bion.officelayer.application.OfficeApplicationException;
import ag.ion.bion.officelayer.application.OfficeApplicationRuntime;
import ag.ion.bion.officelayer.desktop.DesktopException;
import ag.ion.bion.officelayer.desktop.IFrame;
import ag.ion.bion.officelayer.document.DocumentDescriptor;
import ag.ion.bion.officelayer.document.DocumentException;
import ag.ion.bion.officelayer.document.IDocument;
import ag.ion.bion.officelayer.document.IDocumentDescriptor;
import ag.ion.bion.officelayer.filter.PDFFilter;
import ag.ion.bion.officelayer.form.IFormComponent;
import ag.ion.bion.officelayer.text.ITextDocument;
import ag.ion.bion.officelayer.text.ITextField;
import ag.ion.bion.officelayer.text.ITextFieldService;
import ag.ion.bion.officelayer.text.IVariableTextFieldMaster;
import ag.ion.bion.officelayer.text.TextException;
import ag.ion.noa.NOAException;
import com.artofsolving.jodconverter.DocumentConverter;
import com.artofsolving.jodconverter.openoffice.connection.OpenOfficeConnection;
import com.artofsolving.jodconverter.openoffice.connection.SocketOpenOfficeConnection;
import com.artofsolving.jodconverter.openoffice.converter.OpenOfficeDocumentConverter;
import com.sun.star.awt.XTextComponent;
import com.sun.star.beans.*;
import com.sun.star.comp.helper.BootstrapException;
import com.sun.star.document.XDocumentInfoSupplier;
import com.sun.star.io.IOException;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.uno.Exception;
import com.sun.star.uno.XComponentContext;
import com.sun.star.frame.*;
import com.sun.star.text.XTextDocument;
import com.sun.star.form.XFormComponent;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.util.DateTime;
import java.io.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JPanel;
import mpv5.globals.LocalSettings;
import mpv5.logging.Log;
import mpv5.utils.files.FileDirectoryHandler;
import mpv5.utils.reflection.ClasspathTools;
import ooo.connector.BootstrapSocketConnector;
import org.jdom.JDOMException;
import org.jopendocument.dom.OOUtils;
import org.jopendocument.dom.template.RhinoFileTemplate;
import org.jopendocument.dom.template.TemplateException;

/**
 *
 *  
 */
public class ODTFile extends Exportable {

    private String oootype;
    private String ooohome;
    private String ooohost;
    private String oooport;

    public ODTFile(String pathToFile) {
        super(pathToFile);
        String officeVal = LocalSettings.getProperty(LocalSettings.OFFICE_HOME);
        String[] officeVals = officeVal.split(":");
        try {
            oootype = officeVals[0];
            ooohome = officeVals[1];
            ooohost = officeVals[2];
            oooport = officeVals[3];
        } catch (ArrayIndexOutOfBoundsException e) {
        }

        try {
            ClasspathTools.addPath(officeVals[1]);
            ClasspathTools.addPath(officeVals[1] + "program");
        } catch (java.lang.Exception exception) {
        }
    }

    @Override
    public void run() {
        Log.Debug(this, "run: ");
        try {
            // Load the template.
            RhinoFileTemplate template = new RhinoFileTemplate(this);
            // Fill with sample values.Log.Debug(this, "run: ");Log.Debug(this, "run: ");
            fillFields4(template, getData());

            IOfficeApplication officeApplication = null;
            HashMap<String, String> configuration = new HashMap<String, String>();
            if (oootype.equalsIgnoreCase(IOfficeApplication.LOCAL_APPLICATION)) {
                configuration.put(IOfficeApplication.APPLICATION_HOME_KEY, ooohome);
                configuration.put(IOfficeApplication.APPLICATION_TYPE_KEY, IOfficeApplication.LOCAL_APPLICATION);
            } else if (oootype.equalsIgnoreCase(IOfficeApplication.REMOTE_APPLICATION)) {
                configuration.put(IOfficeApplication.APPLICATION_TYPE_KEY, IOfficeApplication.REMOTE_APPLICATION);
                configuration.put(IOfficeApplication.APPLICATION_HOST_KEY, ooohost); //IP des anderen PCs
                configuration.put(IOfficeApplication.APPLICATION_PORT_KEY, oooport);
//                configuration.put(IOfficeApplication.APPLICATION_HOME_KEY, ooohome);
            }

            Log.Debug(this, "OOO Properties: " + ooohome);
            Log.Debug(this, "OOO Properties: " + ooohost);
            Log.Debug(this, "OOO Properties: " + oooport);
            Log.Debug(this, "OOO Properties: " + oootype);

            officeApplication = OfficeApplicationRuntime.getApplication(configuration);
            officeApplication.activate();

            IDocumentDescriptor d = new DocumentDescriptor(true);
            IDocument document = officeApplication.getDocumentService().loadDocument(getPath(), d);
            fillFields1((ITextDocument) document, getData());
            fillFields2((ITextDocument) document, getData());
            fillFields3((ITextDocument) document, getData());

            File f = FileDirectoryHandler.getTempFile("odt");
            template.saveAs(f);

            File inputFile = f;
            File outputFile = getTarget();

// connect to an OpenOffice.org instance running on port 8100
            OpenOfficeConnection connection = new SocketOpenOfficeConnection(8100);
            connection.connect();

// convert
            DocumentConverter converter = new OpenOfficeDocumentConverter(connection);
            converter.convert(inputFile, outputFile);

// close the connection
            connection.disconnect();

        } catch (IOException ex) {
            Logger.getLogger(ODTFile.class.getName()).log(Level.SEVERE, null, ex);
        } catch (java.lang.Exception ex) {
            Logger.getLogger(ODTFile.class.getName()).log(Level.SEVERE, null, ex);
        }

//        try {
//            IOfficeApplication officeApplication = null;
//            HashMap<String, String> configuration = new HashMap<String, String>();
//            if (oootype.equalsIgnoreCase(IOfficeApplication.LOCAL_APPLICATION)) {
//                configuration.put(IOfficeApplication.APPLICATION_HOME_KEY, ooohome);
//                configuration.put(IOfficeApplication.APPLICATION_TYPE_KEY, IOfficeApplication.LOCAL_APPLICATION);
//            } else if (oootype.equalsIgnoreCase(IOfficeApplication.REMOTE_APPLICATION)) {
//                configuration.put(IOfficeApplication.APPLICATION_TYPE_KEY, IOfficeApplication.REMOTE_APPLICATION);
//                configuration.put(IOfficeApplication.APPLICATION_HOST_KEY, ooohost); //IP des anderen PCs
//                configuration.put(IOfficeApplication.APPLICATION_PORT_KEY, oooport);
//                configuration.put(IOfficeApplication.APPLICATION_HOME_KEY, ooohome);
//            }
//
//            officeApplication = OfficeApplicationRuntime.getApplication(configuration);
//            officeApplication.activate();
//
//            IDocumentDescriptor d = new DocumentDescriptor(true);
//            IDocument document = officeApplication.getDocumentService().loadDocument(getPath(), d);
//            fillFields1((ITextDocument) document, getData());
//            fillFields2((ITextDocument) document, getData());
//            fillFields3((ITextDocument) document, getData());
//
//            document.getPersistenceService().export(getTarget().getPath(), PDFFilter.FILTER);
//            officeApplication.deactivate();
//        } catch (Exception ex) {
//            Log.Debug(ex);
//        } catch (NOAException ex) {
//            Log.Debug(ex);
//        } catch (DocumentException ex) {
//            Log.Debug(ex);
//        } catch (OfficeApplicationException ex) {
//            Log.Debug(ex);
//        }
    }

    /**
     * Fill the Form Fields of the template with values
     * @param textDocument
     * @param data
     * @throws Exception
     * @throws NOAException
     */
    public void fillFields1(ITextDocument textDocument, HashMap<String, String> data) throws Exception, NOAException {

        IFormComponent[] formComponents = textDocument.getFormService().getFormComponents();
        // iterate over hashtable and insert values into field masters
        Iterator<String> keys = data.keySet().iterator();
        String key = null;
        while (keys.hasNext()) {
            try {
                // get column name
                key = keys.next();

                for (int i = 0; i < formComponents.length; i++) {

                    XFormComponent xFormComponent = formComponents[i].getXFormComponent();
                    XTextComponent xTextComponent = formComponents[i].getXTextComponent();
                    XPropertySet propertySet = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class,
                            xFormComponent);

                    if (propertySet != null && propertySet.getPropertySetInfo().hasPropertyByName("Name")) {
                        String n = propertySet.getPropertyValue("Name").toString();
                        if (n.matches(key)) {
                            Log.Debug(this, "Found field: " + key);
                            xTextComponent.setText(data.get(key));
                        }
                    }
                }
                textDocument.getTextFieldService().refresh();
            } catch (TextException ex) {
                Log.Debug(this, ex.getMessage() + " for key: " + key);
            } catch (Exception ex) {
                Log.Debug(this, ex.getMessage() + " for key: " + key);
            }
        }
    }

    /**
     * Fill the Placeholder Fields of the template with values
     * @param textDocument
     * @param data
     * @throws Exception
     * @throws NOAException
     */
    public void fillFields2(ITextDocument textDocument, HashMap<String, String> data) throws Exception, NOAException {
        // iterate over hashtable and insert values into field masters
        Iterator<String> keys = data.keySet().iterator();
        String key = null;
        while (keys.hasNext()) {
            // get column name
            key = keys.next();

            try {
                try {
                    ITextFieldService textFieldService = textDocument.getTextFieldService();
                    ITextField[] placeholders = textFieldService.getPlaceholderFields();
                    for (int i = 0; i < placeholders.length; i++) {
                        String placeholderDisplayText = placeholders[i].getDisplayText();
                        if (placeholderDisplayText.matches(key)) {
                            Log.Debug(this, "Found placeholder: " + key);
                            placeholders[i].getTextRange().setText(data.get(key));
                        }
                    }
                } catch (java.lang.Exception ex) {
                    Log.Debug(this, ex.getMessage() + " for key: " + key);
                }
                textDocument.getTextFieldService().refresh();
            } catch (TextException ex) {
                Log.Debug(this, ex.getMessage() + " for key: " + key);
            }
        }
    }

    /**
     * Fill the Variable Text Fields of the template with values
     * @param textDocument
     * @param data
     * @throws Exception
     * @throws NOAException
     */
    public void fillFields3(ITextDocument textDocument, HashMap<String, String> data) throws Exception, NOAException {
        // iterate over hashtable and insert values into field masters
        Iterator<String> keys = data.keySet().iterator();
        String key = null;
        IVariableTextFieldMaster x;
        while (keys.hasNext()) {
            // get column name
            key = keys.next();
            try {
                ITextFieldService textFieldService = textDocument.getTextFieldService();
                x = textFieldService.getVariableTextFieldMaster(key);
                if (x != null) {
                    ITextField[] variables = x.getVariableTextFields();
                    for (int i = 0; i < variables.length; i++) {
                        XPropertySet xPropertySetField = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, variables[i].getXTextContent());
                        if (xPropertySetField.getPropertyValue("CurrentPresentation").toString().matches(key)) {
                            Log.Debug(this, "Found variable: " + key);
                            xPropertySetField.setPropertyValue("Content", data.get(key));
                        }
                    }
                    textDocument.getTextFieldService().refresh();
                }
            } catch (TextException ex) {
                Log.Debug(this, ex.getMessage() + " for key: " + key);
            } catch (Exception ex) {
                Log.Debug(this, ex.getMessage() + " for key: " + key);
            }
        }
    }

    /**
     * Fill the Inputgra Fields of the template with values
     * @param template
     * @param data
     */
    public void fillFields4(RhinoFileTemplate template, HashMap<String, String> data) {
        Iterator<String> keys = data.keySet().iterator();
        String key = null;

        while (keys.hasNext()) {
            // get column name
            key = keys.next();
            try {
                Log.Debug(this, "Found field: " + key);
                template.setField(key, data.get(key));
            } catch (java.lang.Exception ex) {
                Log.Debug(this, ex.getMessage() + " for key: " + key);
            }
        }
    }

    /**
     *
     * @param dir
     * @return
     * @throws Exception
     */
    public File[] findOfficeFiles(File dir) throws Exception {
        File[] officeFiles = dir.listFiles(new OfficeFileFilter());
        File[] subdirs = dir.listFiles(new DirFilter());
        Log.Debug(this, "current dir = " + dir.getAbsolutePath());
        Log.Debug(this, "office files = " + officeFiles.length);
        Log.Debug(this, "dir number = " + subdirs.length);
        return officeFiles;
    }

    public void printDocumentInfo(Object desktop, File officeFile) throws IOException {
        com.sun.star.lang.XComponent xComponent = null;
        try {

            com.sun.star.frame.XComponentLoader componentLoader = (com.sun.star.frame.XComponentLoader) UnoRuntime.queryInterface(XComponentLoader.class, desktop);
            //XMultiServiceFactory multiServiceFactory = (XMultiServiceFactory) UnoRuntime.queryInterface(XMultiServiceFactory.class, multiComponentFactory);
            // Load the document, which will be displayed.
            String officeFilename = officeFile.toURI().toString().replaceFirst("/", "///");

            Log.Debug(this, "\tdir  = " + officeFile.getParent());
            Log.Debug(
                    this, "\tfile = " + officeFile.getName());
            xComponent = componentLoader.loadComponentFromURL(officeFilename, "_blank", 0, new com.sun.star.beans.PropertyValue[0]);
            // Get the textdocument
            XTextDocument xtd = (XTextDocument) UnoRuntime.queryInterface(XTextDocument.class, xComponent);
            com.sun.star.document.XDocumentInfoSupplier xdis = (com.sun.star.document.XDocumentInfoSupplier) UnoRuntime.queryInterface(XDocumentInfoSupplier.class, xComponent);
            com.sun.star.document.XDocumentInfo xdi = xdis.getDocumentInfo();
            com.sun.star.beans.XPropertySet xps = (com.sun.star.beans.XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, xdi);
            DateTime dt = (DateTime) xps.getPropertyValue("ModifyDate");
        } catch (Exception ex) {
            Log.Debug(ex);
        }

    }

    private Object connect() throws com.sun.star.io.IOException, BootstrapException, Exception {
//            XComponentContext xContext = Bootstrap.bootstrap();
        XComponentContext xContext = BootstrapSocketConnector.bootstrap(LocalSettings.getProperty(LocalSettings.OFFICE_HOME));
        XMultiComponentFactory x = xContext.getServiceManager();
        Object desktop = x.createInstanceWithContext("com.sun.star.frame.Desktop", xContext);
        return desktop;
    }

    class OfficeFileFilter implements FileFilter {

        public static final String EXTENSION = ".*sxw$|.*doc$|.*xls$|.*odt$|.*ods$|.*pps$|.*odt$|.*ppt$|.*odp$";

        @Override
        public boolean accept(File pathname) {
            return pathname.getName().matches(EXTENSION);
        }
    }

    class DirFilter implements FileFilter {

        @Override
        public boolean accept(File pathname) {
            return pathname.isDirectory() && !pathname.getName().startsWith(".");
        }
    }
}
