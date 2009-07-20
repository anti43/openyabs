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
import ag.ion.bion.officelayer.desktop.GlobalCommands;
import ag.ion.bion.officelayer.desktop.IFrame;
import ag.ion.bion.officelayer.document.DocumentException;
import ag.ion.bion.officelayer.document.IDocument;
import ag.ion.bion.officelayer.form.IFormComponent;
import ag.ion.bion.officelayer.text.ITextDocument;
import ag.ion.noa.NOAException;
import ag.ion.noa.frame.IDispatchDelegate;
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
import com.sun.star.lang.XServiceInfo;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.util.DateTime;
import com.sun.star.util.URL;
import java.io.*;
import java.net.MalformedURLException;

import java.util.HashMap;
import java.util.Iterator;
import javax.swing.JPanel;
import mpv5.globals.LocalSettings;
import mpv5.logging.Log;
import mpv5.utils.reflection.ClasspathTools;
import ooo.connector.BootstrapSocketConnector;

/**
 *
 *  
 */
public class ODTFile extends Exportable {

    private final String officeVal;
    private final String[] officeVals;

    public ODTFile(String pathToFile) {
        super(pathToFile);

        officeVal = LocalSettings.getProperty(LocalSettings.OFFICE_HOME);
        officeVals = officeVal.split(":");

        try {
            ClasspathTools.addPath(officeVals[1]);
            ClasspathTools.addPath(officeVals[1] + "program");
        } catch (java.lang.Exception exception) {
        }
    }

    @Override
    public void run() {
        throw new UnsupportedOperationException("ODT background export is currently not supported. Use constructOOOPanel(JPanel) instead. ");
    }

    /**
     * Loads the given template into the panel and fills the form fileds with data
     * @param panel
     * @return
     * @throws OfficeApplicationException
     * @throws DesktopException
     * @throws DocumentException
     * @throws Exception
     * @throws NOAException 
     */
    public JPanel constructOOOPanel(final JPanel panel) throws OfficeApplicationException, DesktopException, DocumentException, Exception, NOAException {
        IOfficeApplication officeApplication = null;
        ITextDocument textDocument;


        panel.setVisible(true);
        HashMap<String, String> configuration = new HashMap<String, String>();

        if (!LocalSettings.getProperty(LocalSettings.OFFICE_HOME).equals("null") && LocalSettings.getProperty(LocalSettings.OFFICE_HOME).split(":")[0].equalsIgnoreCase(IOfficeApplication.LOCAL_APPLICATION)) {
            configuration.put(IOfficeApplication.APPLICATION_HOME_KEY, LocalSettings.getProperty(LocalSettings.OFFICE_HOME.split(":")[1]));
            configuration.put(IOfficeApplication.APPLICATION_TYPE_KEY, IOfficeApplication.LOCAL_APPLICATION);
        } else if (!LocalSettings.getProperty(LocalSettings.OFFICE_HOME).equals("null") && LocalSettings.getProperty(LocalSettings.OFFICE_HOME).split(":")[0].equalsIgnoreCase(IOfficeApplication.REMOTE_APPLICATION)) {
            configuration.put(IOfficeApplication.APPLICATION_TYPE_KEY, IOfficeApplication.REMOTE_APPLICATION);
            configuration.put(IOfficeApplication.APPLICATION_HOST_KEY, LocalSettings.getProperty(LocalSettings.OFFICE_HOME.split(":")[1])); //IP des anderen PCs
            configuration.put(IOfficeApplication.APPLICATION_PORT_KEY, LocalSettings.getProperty(LocalSettings.OFFICE_HOME.split(":")[2]));
        } else {
            throw new Exception("OpenOffice installation not configured!");
        }

        officeApplication = OfficeApplicationRuntime.getApplication(configuration);
        officeApplication.setConfiguration(configuration);
        officeApplication.activate();

        IFrame officeFrame = officeApplication.getDesktopService().constructNewOfficeFrame(panel);
        officeFrame.setFocus();
        IDocument document = officeApplication.getDocumentService().loadDocument(officeFrame, getPath());
        textDocument = (ITextDocument) document;

        fillFields(textDocument, getData());

        configureFrame(officeFrame, officeApplication);
        panel.getParent().validate();
        return panel;
    }

    /**
     * Configures the given IFrame to print the closed document's path to System.out on close<br/>
     * and removes the Close and Quit capabilites of the frame
     * @param officeFrame
     * @param officeApplication
     */
    private void configureFrame(final IFrame officeFrame, final IOfficeApplication officeApplication) {

        officeFrame.addDispatchDelegate(GlobalCommands.SAVE, new IDispatchDelegate() {

            @Override
            public void dispatch(Object[] arg0) {
                System.out.println("save");
                try {
                    try {
                        System.out.println(officeApplication.getDocumentService().getCurrentDocuments()[0].getPersistenceService().getLocation());
                    } catch (DocumentException ex) {
                        Log.Debug(ex);
                    }
                } catch (OfficeApplicationException ex) {
                    Log.Debug(ex);
                }
            }
        });

        officeFrame.updateDispatches();
        officeFrame.disableDispatch(GlobalCommands.CLOSE_DOCUMENT);
        officeFrame.disableDispatch(GlobalCommands.QUIT_APPLICATION);
    }

    /**
     * Fill the form fields of the template with values
     * @param textDocument
     * @param data
     * @throws Exception
     * @throws NOAException
     */
    public void fillFields(ITextDocument textDocument, HashMap<String, String> data) throws Exception, NOAException {

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
                        if (n.equalsIgnoreCase(key)) {
                            xTextComponent.setText(data.get(key));
                        }
                    }
                }
            } catch (Exception noSuchElementException) {
                System.err.println(noSuchElementException.getMessage());
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
            Log.Debug(this, "\tfile = " + officeFile.getName());
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
