/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mpv5.utils.oasis;

/*
 * DocViewer.java
 *
 * Created on 23/07/2007, 16:39:30
 */
import com.sun.star.beans.*;
import com.sun.star.bridge.*;
import com.sun.star.comp.helper.Bootstrap;
import com.sun.star.connection.XConnector;
import com.sun.star.document.XDocumentInfoSupplier;
import com.sun.star.io.IOException;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.uno.XComponentContext;
import com.sun.star.frame.*;
import com.sun.star.text.XTextDocument;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.util.DateTime;
import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.net.*;
import mpv5.utils.reflection.ClasspathTools;

/**
 *
 * @author claudio (claudio(o)claudius.com.br)
 */
public class DocViewer {

    /*
     *  Para carregar o openoffice sem a janela grafica eh necessario um X Virtual Frame Buffer (Xvfb)
     *
     *  - Inicializar um X virtual
     *  Xvfb :5 -screen 0 800x600x16 &
     *
     *  - Inicializar o OpenOffice neste X virtual
     *  /opt/broffice.org2.2/program/soffice -accept="socket,host=127.0.0.1,port=8100;urp;" -display :5 -headless -norestore -invisible &
     *
     *  Informações: http://blog.4e.co.za/?p=14
     *
     */
    public static final String OO_CONNECTION_URL = "socket,host=127.0.0.1,port=8100";
    static Logger logger = Logger.getLogger("global");
    com.sun.star.connection.XConnection connection = null;
    FileFilter officeFilesFilter = new OfficeFileFilter();
    FileFilter dirFilter = new DirFilter();
    public static final String LF = System.getProperty("line.separator");

    public static void main(String[] args) throws Exception {
        logger.setLevel(Level.INFO);
        ClasspathTools.addPath("/opt/openoffice.org3/");
        ClasspathTools.addPath("/opt/openoffice.org3/program");

        File dir = new File("/home/anti/aaa.odt");
        DocViewer officeViewer = null;
        if (dir.isDirectory() || (dir.isFile() && dir.getName().matches(OfficeFileFilter.EXTENSION))) {
            try {
                officeViewer = new DocViewer();
                Object desktop = officeViewer.connect();
                if (dir.isDirectory()) {
                    officeViewer.findOfficeFiles(desktop, dir);
                } else {
                    officeViewer.printDocumentInfo(desktop, dir);
                }
            } catch (Exception ex) {
                logger.log(Level.SEVERE, null, ex);
            } finally {
//                officeViewer.close();
            }
        } 

    }

    public void findOfficeFiles(Object desktop, File dir) throws Exception {
        File[] officeFiles = dir.listFiles(officeFilesFilter);
        File[] subdirs = dir.listFiles(dirFilter);
        logToConsole("current dir = " + dir.getAbsolutePath());
        logToConsole("office files = " + officeFiles.length);
        logToConsole("dir number = " + subdirs.length + LF);
        for (int i = 0; i < officeFiles.length; i++) {
            File officeFile = officeFiles[i];
            printDocumentInfo(desktop, officeFile);
        }
        for (int i = 0; i < subdirs.length; i++) {
            File subdir = subdirs[i];
            findOfficeFiles(desktop, subdir);
        }
    }

    public void printDocumentInfo(Object desktop, File officeFile) throws IOException {
        com.sun.star.lang.XComponent xComponent = null;
        try {
            com.sun.star.frame.XComponentLoader componentLoader = (com.sun.star.frame.XComponentLoader) UnoRuntime.queryInterface(XComponentLoader.class, desktop);
            //XMultiServiceFactory multiServiceFactory = (XMultiServiceFactory) UnoRuntime.queryInterface(XMultiServiceFactory.class, multiComponentFactory);
            // Load the document, which will be displayed.
            String officeFilename = officeFile.toURI().toString().replaceFirst("/", "///");
            logToConsole("\tdir  = " + officeFile.getParent());
            logToConsole("\tfile = " + officeFile.getName());
            xComponent = componentLoader.loadComponentFromURL(officeFilename, "_blank", 0, new com.sun.star.beans.PropertyValue[0]);
            // Get the textdocument
            XTextDocument xtd = (XTextDocument) UnoRuntime.queryInterface(XTextDocument.class, xComponent);
            com.sun.star.document.XDocumentInfoSupplier xdis = (com.sun.star.document.XDocumentInfoSupplier) UnoRuntime.queryInterface(XDocumentInfoSupplier.class, xComponent);
            com.sun.star.document.XDocumentInfo xdi = xdis.getDocumentInfo();
            com.sun.star.beans.XPropertySet xps = (com.sun.star.beans.XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, xdi);
            DateTime dt = (DateTime) xps.getPropertyValue("ModifyDate");
            logToConsole("\tModified by: " + xps.getPropertyValue("ModifiedBy") + " " + dateToString(dt) + LF);
        } catch (Exception ex) {
            logger.log(Level.SEVERE, null, ex);
        } finally {
//            if (xComponent != null) {
//                xComponent.dispose();
//            }

        }
    }

    public Object connect() throws com.sun.star.io.IOException {

        Object defaultContext;
        Object desktop = null;
        XMultiComponentFactory x;

        try {
            XComponentContext xContext = Bootstrap.bootstrap();
            x = xContext.getServiceManager();
            desktop = x.createInstanceWithContext("com.sun.star.frame.Desktop", xContext);
        } catch (Exception ex) {
            Logger.getLogger("global").log(Level.SEVERE, null, ex);
        }
        return desktop;
    }

    private void close() {
        if (connection != null) {
            try {
                connection.close();
            } catch (Exception ex2) {
                ex2.printStackTrace();
            }
        }
    }

    private String dateToString(DateTime dt) {
        return dt.Day + "/" + dt.Month + "/" + dt.Year + " " + dt.Hours + ":" + dt.Minutes + ":" + dt.Seconds;
    }

    private static void logToConsole(String m) {
        //logger.info(m);
        System.out.println(m);
    }
}

class OfficeFileFilter implements FileFilter {

    public static final String EXTENSION = ".*sxw$|.*doc$|.*xls$|.*odt$|.*ods$|.*pps$|.*odt$|.*ppt$|.*odp$";

    public boolean accept(File pathname) {
        return pathname.getName().matches(EXTENSION);
    }
}

class DirFilter implements FileFilter {

    public boolean accept(File pathname) {
        return pathname.isDirectory() && !pathname.getName().startsWith(".");
    }
}
